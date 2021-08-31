/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.sequence;

/**
 *
 * @author Vipin Gupta
 */
public enum SeqDateFormat {
    
    YYYY, YYYYMM, YYYYMMDD;
    
    public String getStringName(){
        String name = "";
        switch(this){
            case YYYY : name = "yyyy";break;
            case YYYYMM : name = "yyyyMM";break;
            case YYYYMMDD : name = "yyyyMMdd";break;
        }
        return name;
    }
    
}
