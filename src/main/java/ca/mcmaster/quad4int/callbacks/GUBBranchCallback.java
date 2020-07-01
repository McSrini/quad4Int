/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.quad4int.callbacks;

import static ca.mcmaster.quad4int.Constants.*;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import java.util.Map;

/**
 *
 * @author tamvadss
 */
public class GUBBranchCallback extends IloCplex.BranchCallback {
    
    private Map <String, String> injectedVariables;
    private IloCplex newCplex;
    private  Map <String, IloNumExpr> identityMap;
    private Map <String,IloNumVar[] > intToBinMap;
    
    public GUBBranchCallback (Map <String, String> injectedVariables, IloCplex newCplex , 
                              Map <String, IloNumExpr> identityMap,
                              Map <String,IloNumVar[] > intToBinMap) {
        this. injectedVariables= injectedVariables;
        this. newCplex = newCplex;
        this.identityMap = identityMap;
        this.intToBinMap = intToBinMap;
    }
    
        
    protected void main() throws IloException {
        
        if ( getNbranches()> ZERO ){ 
            
            //get the branches about to be created
            IloNumVar[][] vars = new IloNumVar[TWO][] ;
            double[ ][] bounds = new double[TWO ][];
            IloCplex.BranchDirection[ ][] dirs = new IloCplex.BranchDirection[ TWO][];
            getBranches( vars, bounds, dirs);
            
            //if an injected binary variable is being branched on, find the corresponding integer variable
            //branch on integer variable using GUB dichotomy

            IloNumVar var = vars[ZERO][ZERO];
            String intVarName = injectedVariables.get(var.getName());
            if ( null!=intVarName  ){
                //find corresponding integer variable , which of course is no longer the model

                double intVariableValue = getValue (identityMap.get(intVarName));
                int floor = (int) Math.floor( intVariableValue);

                IloRange [] ranges = getGUBConditions (intVarName,floor) ;
                makeBranch(ranges[ZERO],getObjValue());
                makeBranch(ranges[ONE],getObjValue());

            }
             
        }//end if branches > 0
    }//end main
         
    private IloRange [] getGUBConditions (String intVarName, int floor) throws IloException{
        IloRange [ ] ranges = new IloRange [TWO];
        IloNumExpr expr =  this.identityMap.get (intVarName) ;
                 
        ranges[ZERO]= this.newCplex.le(expr , floor);
        ranges[ONE]= this.newCplex.ge( expr, ONE+floor);
        
        return ranges;
        
    }
    
}// end callback class
