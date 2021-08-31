/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.store;

/**
 *
 * @author Vipin Gupta
 */
public enum StoreType {

    WAREHOUSE("Warehouse"),
    RETAIL("Retail"),
    HEADQUARTER("Headquarter"),
    REPAIR("Repair"),
    SCRAP("Scrap");
    
    private String name;

    private StoreType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
