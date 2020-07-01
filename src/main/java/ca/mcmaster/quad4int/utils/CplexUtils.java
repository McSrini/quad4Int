/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.quad4int.utils;
 
import static ca.mcmaster.quad4int.Constants.BILLION;
import static ca.mcmaster.quad4int.Constants.ZERO;
import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloLinearNumExprIterator;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloObjective;
import ilog.concert.IloObjectiveSense;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tamvadss
 */
public class CplexUtils {
    
    public static void getVariablesWithTypeInformation (IloCplex cplex, Map<String, IloNumVar> integerVars, 
            Map<String, IloNumVar> binaryVars , Map<String, IloNumVar> continuousVars) throws IloException {
         
        IloLPMatrix lpMatrix = (IloLPMatrix)cplex.LPMatrixIterator().next();
        IloNumVar[] variables  =lpMatrix.getNumVars();
        
        for (IloNumVar numVar : variables){
            if( numVar.getType().equals( IloNumVarType.Int)){
                integerVars.put( numVar.getName(),numVar );
            }else if (numVar.getType().equals( IloNumVarType.Bool)) {
                binaryVars.put( numVar.getName(),numVar );
            }else {
                continuousVars.put( numVar.getName(),numVar );
            }
        }
              
    }
    
    //minimization objective
    public static Map<String, Double> getObjective (IloCplex cplex) throws IloException {
        
        Map<String, Double>  objectiveMap = new HashMap<String, Double>();
        
        IloObjective  obj = cplex.getObjective();
        
        IloLinearNumExpr expr = (IloLinearNumExpr) obj.getExpr();
                 
        IloLinearNumExprIterator iter = expr.linearIterator();
        while (iter.hasNext()) {
           IloNumVar var = iter.nextNumVar();
           double val = iter.getValue();
           
           if (Math.abs(val) > ZERO) objectiveMap.put(var.getName(),  val   );            
           
        }
        
        return  objectiveMap ;        
         
    }
    
    public static List<Constraint> getConstraints (IloCplex cplex) throws IloException {
        List<Constraint> constraintList = new ArrayList<Constraint>();
         
        IloLPMatrix lpMatrix = (IloLPMatrix)cplex.LPMatrixIterator().next();        
        final int numConstraints = lpMatrix.getNrows();
        int[][] ind = new int[ numConstraints][];
        double[][] val = new double[ numConstraints][];        
        double[] lb = new double[numConstraints] ;
        double[] ub = new double[numConstraints] ;        
        lpMatrix.getRows(ZERO,   numConstraints, lb, ub, ind, val);
        
        IloRange[] ranges = lpMatrix.getRanges() ;
        for (int index=ZERO; index < numConstraints ; index ++ ){
            
            String thisConstraintname = ranges[index].getName();
            //System.out.println("Constarint is : " + thisConstraintname + " lenght is " +ind[index].length);//k
                        
            boolean isUpperBound = Math.abs(ub[index])< BILLION ;
            boolean isLowerBound = Math.abs(lb[index])<BILLION ;
            
            Constraint constraint = new Constraint ();
            constraint.name = thisConstraintname;
            constraint.direction = BoundDirectionEnum.EQUALITY;
            constraint.bound = lb[index];
            if (isUpperBound && !isLowerBound )  {
                constraint.direction = BoundDirectionEnum.DOWN;
                constraint.bound = ub[index];
            }
            if (isLowerBound && ! isUpperBound)  {
                constraint.direction = BoundDirectionEnum.UP;
                constraint.bound = lb[index];
            }
            
            for (  int varIndex = ZERO;varIndex< ind[index].length;   varIndex ++ ){
                String varName = lpMatrix.getNumVar(ind[index][varIndex]).getName() ;
                Double coeff = val[index][varIndex];
                constraint.constraintExpression.put(varName,  coeff) ;                 
            } 
            
            constraintList.add(constraint);
            //constraint.printMe();
        }
        
        return constraintList;
        
    }
    
}
