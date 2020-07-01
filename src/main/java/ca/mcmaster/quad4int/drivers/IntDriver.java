/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.quad4int.drivers;

import static ca.mcmaster.quad4int.Constants.LOGGING_LEVEL;
import static ca.mcmaster.quad4int.Constants.LOG_FILE_EXTENSION;
import static ca.mcmaster.quad4int.Constants.LOG_FOLDER;
import static ca.mcmaster.quad4int.Constants.ONE;
import static ca.mcmaster.quad4int.Constants.SIXTY;
import static ca.mcmaster.quad4int.Parameters.MAX_THREADS;
import static ca.mcmaster.quad4int.Parameters.MIP_FILENAME;
import ca.mcmaster.quad4int.converter.QuadConverter;
import static ca.mcmaster.quad4int.drivers.BaseDriver.logger;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import static java.lang.System.exit;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class IntDriver  extends BaseDriver  {
        
    static {
        logger = org.apache.log4j.Logger.getLogger(IntDriver  .class); 
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+  IntDriver.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    } 
    
    public static void main(String[] args) throws IloException, UnknownHostException {
                 
        IloCplex cplex = new IloCplex ();
        cplex.importModel(  MIP_FILENAME);  
        
        logger.info ("IntDriver Starting solve  integer "+ InetAddress.getLocalHost().getHostName() + " threads " +MAX_THREADS ) ;
        solve(cplex);
               
    }
    
}
