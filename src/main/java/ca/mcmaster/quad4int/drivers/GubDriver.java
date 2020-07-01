/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.quad4int.drivers;

import static ca.mcmaster.quad4int.Constants.*;
import static ca.mcmaster.quad4int.Parameters.*;
import ca.mcmaster.quad4int.callbacks.GUBBranchCallback;
import ca.mcmaster.quad4int.converter.QuadConverter;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import static java.lang.System.exit;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class GubDriver extends BaseDriver {
    
    static {
        logger = org.apache.log4j.Logger.getLogger(GubDriver  .class); 
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+  GubDriver.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    } 
        
    public static void main(String[] args) throws  Exception {
                 
        IloCplex cplex = new IloCplex ();
        cplex.importModel(  MIP_FILENAME);  
        
        QuadConverter quad = new QuadConverter();
        IloCplex convertedCplex = quad.convert( cplex) ;
       
        logger.info ("Gub driver Starting solve   "+ InetAddress.getLocalHost().getHostName() + " threads " +MAX_THREADS ) ;
        convertedCplex.use (new GUBBranchCallback(quad.injectedVariables, quad.convertedCplex   , 
                                                  quad. identityMap, quad. intToBinMap)) ;
        solve(convertedCplex);        
        
    }
    
}
