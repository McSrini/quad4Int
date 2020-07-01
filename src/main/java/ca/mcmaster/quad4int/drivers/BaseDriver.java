/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.quad4int.drivers;

import static ca.mcmaster.quad4int.Constants.*;
import static ca.mcmaster.quad4int.Parameters.MAX_THREADS;
import ca.mcmaster.quad4int.callbacks.EmptyBranchcallback;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author tamvadss
 */
public abstract class BaseDriver {
    protected static org.apache.log4j.Logger logger=null;
    
    public static void solve (IloCplex cplex) throws IloException, UnknownHostException {
       cplex.setParam( IloCplex.Param.TimeLimit, SIXTY*SIXTY);
       cplex.setParam( IloCplex.Param.Threads, MAX_THREADS);   
             
       for (int hour = ZERO; hour < SIXTY ; hour ++) {
           cplex.solve();
           double lpRelax = cplex.getBestObjValue();
           Double soln =  BILLION;
           if (cplex.getStatus().equals( IloCplex.Status.Optimal)  || cplex.getStatus().equals( IloCplex.Status.Feasible)  ){
               soln = cplex.getObjValue();
           }
            
           logger.info(" Soln and bound " + soln + "," + lpRelax);
       }
       logger.info("Solve completed.");
   } 
    
}
