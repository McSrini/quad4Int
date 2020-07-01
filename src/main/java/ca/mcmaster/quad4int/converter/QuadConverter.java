/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.quad4int.converter;

import ca.mcmaster.quad4int.Constants;
import static ca.mcmaster.quad4int.Constants.*;
import static ca.mcmaster.quad4int.Parameters.*;
import ca.mcmaster.quad4int.utils.*;
import static ca.mcmaster.quad4int.utils.BoundDirectionEnum.EQUALITY;
import static ca.mcmaster.quad4int.utils.BoundDirectionEnum.UP;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import static java.lang.System.exit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author tamvadss
 */
public class QuadConverter {
    
    private  Map<String, IloNumVar> integerVars = new HashMap<String, IloNumVar>  ();
    private   Map<String, IloNumVar> continuousVars = new HashMap<String, IloNumVar>() ;
    private   Map<String, IloNumVar> binaryVars = new HashMap<String, IloNumVar>() ;
    
    private Map<String, Double> objectiveFunction = null;
    private List<Constraint> constraintList = null;
    
    //for each int var, which binary vars have been injected ?
    public Map <String,IloNumVar[] > intToBinMap = new HashMap<String, IloNumVar[]>() ;
    
    //new binary var, and the corresponding integer var it is part of . Include aux vars
    public Map <String, String> injectedVariables  = new  TreeMap <String, String>();
            
    //integer var, and the identity is should be replaced with
    public Map <String, IloNumExpr> identityMap = new HashMap <String, IloNumExpr> ();
        
    public IloCplex convertedCplex =null ;
    
    //y Lb to Lb+ R
    /**
     *  y = Lb -1 + x1 + x1x2 + 3x2x3 + 4x3x4 + ... +Rxr + xr(1-x(r-1))
     *
     */
    
    public   IloCplex convert (IloCplex cplex ) throws Exception {
        //
        CplexUtils.getVariablesWithTypeInformation(cplex, integerVars,binaryVars, continuousVars ) ;
        //get obj
        objectiveFunction = CplexUtils.getObjective( cplex);
        //get constraints
        constraintList = CplexUtils.getConstraints (cplex);
        
        //original model is no longer needed
        cplex.clearModel();
              
        if (integerVars.size()==ZERO){
           System.err.println("no integer vars for conversion") ;
           exit(ONE);
        }
        
        convertedCplex = new IloCplex ();
         //add vars  
        convertedCplex.add(binaryVars   .values().toArray(new IloNumVar [ZERO]));
        convertedCplex.add(continuousVars.values().toArray(new IloNumVar [ZERO])); 
        
        //convert each integer var into a set of binary vars
        for (   IloNumVar intVar : integerVars.values()) {
            //
            IloNumVar[] newBinaryVars = convertIntToBinary (  intVar);
            convertedCplex.add(newBinaryVars   );
            this.recordInjectedBinvars(  newBinaryVars,  intVar  );
            this.intToBinMap.put( intVar.getName(), newBinaryVars);
            
            //add constraints for these injected binary vars
            addConstraintsOnInjectedBinaryVars (newBinaryVars) ;
            
            createIndentityForIntVariable (intVar, newBinaryVars) ;            
        }
                                
        //insert new objective and constraints       
        addObjectiveFunction (    );
        addConstraints(  );
        convertedCplex.exportModel( DESTINATION_MIP_FILENAME );
        return convertedCplex;
    }
    
    //create aux vars, identity for each int var, and aux constraints, add aux vars to model
    private IloNumVar[]  createIndentityForIntVariable (IloNumVar intVar,  IloNumVar[] newBinaryVars) throws IloException {
        IloNumExpr idExpr = convertedCplex.numExpr()  ;
        
        int lowerBound =   (int) Math.round( intVar.getLB());
        int upperBound =   (int) Math.round( intVar.getUB());
        int NUM_VARS = upperBound-lowerBound;
        IloNumVar[] auxilliaryVariables =  null;
        
        String[] xName = new String[NUM_VARS];
        for (int index = ONE ; index <= NUM_VARS ; index ++) {
            xName[index -ONE]= Constants.AUXILLARY + intVar.getName()     + index  ;
        }
        auxilliaryVariables=        convertedCplex.boolVarArray (NUM_VARS,    xName) ;   
        this.convertedCplex.add(auxilliaryVariables);
        
        this.recordInjectedBinvars(auxilliaryVariables, intVar);
        
        //now create the identity used to replace the int var with binary vars
        idExpr= convertedCplex.sum(idExpr, lowerBound);
        for (int index = ONE ; index <= NUM_VARS  ; index ++) {
            //
            idExpr= convertedCplex.sum(idExpr, convertedCplex.prod(index,  auxilliaryVariables[index-ONE]));
        }
        this.identityMap.put( intVar.getName(), idExpr);
        
        //create the auxilliary constraints, last one contains a negation
        for (int index = ONE ; index <= NUM_VARS -ONE ; index ++) {
            //
            // auxilliaryVariables[0] < = x0, x1 and >= x0 +x1 -1
            IloNumExpr  onstraintExprOne  = convertedCplex.numExpr();
            onstraintExprOne = convertedCplex.sum (onstraintExprOne, auxilliaryVariables[index-ONE]) ;
            convertedCplex.addLe(onstraintExprOne , newBinaryVars[index-ONE]);
            
            IloNumExpr constraintExprTwo =  convertedCplex.numExpr();
            constraintExprTwo = convertedCplex.sum (constraintExprTwo, auxilliaryVariables[index-ONE]);
            convertedCplex.addLe(constraintExprTwo, newBinaryVars[index]);
            
            IloNumExpr  onstraintExprThree  = convertedCplex.numExpr();
            onstraintExprThree = convertedCplex.sum (onstraintExprThree, auxilliaryVariables[index-ONE]); 
            IloNumExpr  rhs  = convertedCplex.numExpr();
            rhs = convertedCplex.sum( rhs,newBinaryVars[index-ONE] )  ;
            rhs = convertedCplex.sum( rhs,newBinaryVars[index] )  ;
            rhs = convertedCplex.sum( rhs, -ONE )  ;
            convertedCplex.addGe(onstraintExprThree , rhs );
            
        }
        //last triplet
        IloNumExpr  onstraintExprOne  = convertedCplex.numExpr();
        onstraintExprOne = convertedCplex.sum (onstraintExprOne, auxilliaryVariables[NUM_VARS -ONE]) ;
        convertedCplex.addLe(onstraintExprOne , newBinaryVars[NUM_VARS-ONE]);

        IloNumExpr constraintExprTwo =  convertedCplex.numExpr();
        constraintExprTwo = convertedCplex.sum (constraintExprTwo, auxilliaryVariables[NUM_VARS-ONE]);
        convertedCplex.addLe(constraintExprTwo, convertedCplex.prod(-ONE, convertedCplex.sum( -ONE, newBinaryVars[NUM_VARS-TWO])) );

        IloNumExpr  onstraintExprThree  = convertedCplex.numExpr();
        onstraintExprThree = convertedCplex.sum (onstraintExprThree, auxilliaryVariables[NUM_VARS-ONE]); 
        IloNumExpr  rhs  = convertedCplex.numExpr();
        rhs = convertedCplex.sum( rhs,newBinaryVars[NUM_VARS-ONE] )  ;
        rhs = convertedCplex.sum( rhs, convertedCplex.prod(-ONE, convertedCplex.sum( -ONE, newBinaryVars[NUM_VARS-TWO])))  ;
        rhs = convertedCplex.sum( rhs, -ONE )  ;
        convertedCplex.addGe(onstraintExprThree , rhs );
         
        return auxilliaryVariables;
        
    }
    
    private IloNumVar[]  convertIntToBinary (  IloNumVar intVar) throws IloException{
        //
        int lowerBound = (int) Math.round( intVar.getLB());
        int upperBound = (int) Math.round( intVar.getUB());
       
        int NUM_VARS = upperBound - lowerBound  ;       
       
        //double[]        xLB = new double[NUM_VARS] ;
        //double[]        xUB = new double[NUM_VARS] ;       
        String[] xName = new String[NUM_VARS];
        for (int num = ONE ; num <= NUM_VARS; num ++){
            //xLB[num] = ZERO;
            //xUB[num] = ONE;
            xName[num-ONE]= intVar.getName() + PURE_BINARY_CONVERTED + num ;
        }

        //create the variables
        return convertedCplex.boolVarArray (NUM_VARS,    xName) ;       
    }
    
    private IloNumExpr insertVariablesIntoExpression ( IloNumExpr expr, 
                                   Map.Entry<String, Double> entry) throws IloException {
       
       if (this.integerVars.containsKey(entry.getKey())){
           expr = convertedCplex. sum(expr , convertedCplex.prod(entry.getValue(), this.identityMap.get(entry.getKey())));
       }else {
           if (binaryVars.keySet().contains(entry.getKey() )) {
               expr = convertedCplex. sum(expr , convertedCplex.prod(binaryVars.get(entry.getKey() ),  entry.getValue())) ;
           }else {
               expr = convertedCplex. sum(expr , convertedCplex.prod(continuousVars.get(entry.getKey() ),  entry.getValue())) ;
           }
       }
       
       return expr;
    }
    
    private void addConstraints (  ) throws IloException {
       
        for (Constraint constraint : this.constraintList ){
           IloNumExpr  onstraintExpr  = convertedCplex.numExpr();
           for (Map.Entry<String, Double> entry : constraint.constraintExpression.entrySet()){
               onstraintExpr =this.insertVariablesIntoExpression(onstraintExpr, entry);
           }
           
           if (constraint.direction.equals(EQUALITY)) {
               convertedCplex.addEq(onstraintExpr, constraint.bound );
           } else            if (constraint.direction.equals(UP)){
               convertedCplex.addGe(onstraintExpr, constraint.bound );
           } else           {
               convertedCplex.addLe(onstraintExpr, constraint.bound );
           }
        }
    }
    
    private void addObjectiveFunction (  ) throws IloException {
       //
       IloNumExpr objFunction  = convertedCplex.numExpr();
       for (Map.Entry<String, Double> entry: this.objectiveFunction.entrySet()  ){
           objFunction = this.insertVariablesIntoExpression(objFunction, entry);
       }       
       this.convertedCplex.addMinimize( objFunction);
    }
    
    private void addConstraintsOnInjectedBinaryVars ( IloNumVar[] injectedBinaryVars) throws IloException{
        //x1+x2+...+xR between 1 and 2
        //
        //if m not 1 or R 
        //xm <= xm-1 + xm+1
        //
        //2 big M constraints
        final int BIG_M = injectedBinaryVars.length-TWO;
        IloNumExpr  constraintExprOne  =null;
        IloNumExpr constraintExprTwo_Up = convertedCplex.numExpr();
        IloNumExpr constraintExprTwo_Down = convertedCplex.numExpr();
        IloNumExpr  constraintExprThree  = convertedCplex.numExpr();
        IloNumExpr   constraintExprFour  = convertedCplex.numExpr();
        for (int index = ONE; index <=injectedBinaryVars.length ; index ++ ){
            //           
            if (!(ONE==index || injectedBinaryVars.length ==index)){
                //
                constraintExprOne  = convertedCplex.numExpr();
                constraintExprOne = convertedCplex. sum(constraintExprOne, injectedBinaryVars[index-TWO]) ;
                constraintExprOne = convertedCplex. sum(constraintExprOne, injectedBinaryVars[index]) ;
                constraintExprOne = convertedCplex. sum(constraintExprOne, convertedCplex.prod(injectedBinaryVars[index-ONE], -ONE) ) ;
                convertedCplex.addGe(constraintExprOne, ZERO);                
            }
            
            constraintExprTwo_Up = convertedCplex. sum (constraintExprTwo_Up, injectedBinaryVars[index-ONE]) ;
            constraintExprTwo_Down = convertedCplex. sum (constraintExprTwo_Down, injectedBinaryVars[index-ONE]) ;
            
            if (index <injectedBinaryVars.length-ONE ){
                constraintExprThree = convertedCplex.sum (constraintExprThree, injectedBinaryVars[index-ONE]) ;
            }
            if (index > TWO){
                constraintExprFour = convertedCplex.sum (constraintExprFour, injectedBinaryVars[index-ONE]) ;
            }
           
        }
        
        convertedCplex.addGe(constraintExprTwo_Up ,  ONE);
        convertedCplex.addLe( constraintExprTwo_Down,  TWO);
        
        if (BIG_M> ZERO){
            constraintExprThree = convertedCplex.sum (constraintExprThree, convertedCplex.prod(BIG_M, injectedBinaryVars [injectedBinaryVars.length-ONE] ));
            convertedCplex.addLe(constraintExprThree, BIG_M ) ;
            constraintExprFour = convertedCplex.sum (constraintExprFour, convertedCplex.prod(BIG_M, injectedBinaryVars [ZERO] ));
            convertedCplex.addLe(constraintExprFour, BIG_M ) ;
        }        
         
    }
    
    private void recordInjectedBinvars (IloNumVar[] injectedVar,IloNumVar intVar ) {
        for (IloNumVar binvar : injectedVar){
            this.injectedVariables .put(binvar.getName(), intVar.getName()) ;
        }
    }
    
}
