/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.util;

/**
 *
 * @author krawler
 */
public enum ValuationMethod {
    
    STANDARD , FIFO , AVERAGE ;
    
    public static ValuationMethod getValue(int i){
        if(i == 0) {
            return ValuationMethod.STANDARD;
        } else if(i == 1) {
            return ValuationMethod.FIFO;
        } else{
            return ValuationMethod.AVERAGE;
        }
    }
}
