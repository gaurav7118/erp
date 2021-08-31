/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.util;

/**
 *
 * @author Vipin Gupta
 */
public enum InventoryCheck {

    ALLOW("Allow"), //0
    WARN("Warn"), //1
    BLOCK("Block");//2
    
    String name;

    private InventoryCheck(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
