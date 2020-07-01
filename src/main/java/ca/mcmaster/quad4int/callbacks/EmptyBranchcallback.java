/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.quad4int.callbacks;

import static ca.mcmaster.quad4int.Constants.*;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

/**
 *
 * @author tamvadss
 */
public class EmptyBranchcallback extends IloCplex.BranchCallback {

    protected void main() throws IloException {
        if ( getNbranches()> ZERO ){ 
            
            //get the branches about to be created
            IloNumVar[][] vars = new IloNumVar[TWO][] ;
            double[ ][] bounds = new double[TWO ][];
            IloCplex.BranchDirection[ ][] dirs = new IloCplex.BranchDirection[ TWO][];
            getBranches( vars, bounds, dirs);
            
            //default branch 
        
        }//end if branches > 0
    }//end main
         
     
    
}
