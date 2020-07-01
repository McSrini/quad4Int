/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.quad4int.utils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tamvadss
 */
public class Constraint {
    
    //var name and coeff in constraint
    public Map<String,Double>  constraintExpression = new HashMap <String,Double> ();
    public double bound;   
    public  BoundDirectionEnum direction;   
    public String name ;
    
    public void printMe (){
        System.out.println(name ) ;
        System.out.println(bound ) ;
        System.out.println(direction ) ;
        for (Map.Entry<String,Double> entry : constraintExpression.entrySet() ){
            System.out.println(entry.getKey() + ", "+ entry.getValue() ) ;
        }
    }
    
}
