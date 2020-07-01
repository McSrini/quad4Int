/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.quad4int;

import static ca.mcmaster.quad4int.Constants.PURE_BINARY_CONVERTED;

/**
 *
 * @author tamvadss
 */
public class Parameters {
    
    public final static String MIP_FILENAME =   "F:\\temporary files here\\test.lp";
    //public final static String MIP_FILENAME =   "nursehint03.pre";
    public final static String DESTINATION_MIP_FILENAME = MIP_FILENAME+ PURE_BINARY_CONVERTED + ".lp" ;    
    public   static Integer MAX_THREADS=  null;
    
    static {
        MAX_THREADS =  System.getProperty("os.name").contains("Windows") ? 4 : 32;
    }
}
