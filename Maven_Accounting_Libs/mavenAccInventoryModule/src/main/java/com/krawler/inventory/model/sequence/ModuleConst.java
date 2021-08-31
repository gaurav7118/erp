/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.sequence;

/**
 *
 * @author Vipin Gupta
 */
public enum ModuleConst {

    STOCK_REQUEST("Stock Request"),
    ISSUE_NOTE("Issue Note"),
    INTER_STORE_TRANSFER("Inter Store Transfer"),
    STOCK_ADJUSTMENT("Stock Adjustment"),
    CYCLE_COUNT("Cycle Count"),
    INTER_LOCATION_TRANSFER("Inter Location Transfer"),
    Asset_Module("Asset Id");
    private String name;

    private ModuleConst(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
