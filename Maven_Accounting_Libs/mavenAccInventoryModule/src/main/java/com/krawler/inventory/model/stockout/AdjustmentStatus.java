/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockout;

/**
 *
 * @author Vipin Gupta
 */
public enum AdjustmentStatus {

    REQUESTED("Requested"),
    DRAFT("Draft"),
    REJECTED("Rejected"),
    COMPLETED("Completed");
    String name;

    private AdjustmentStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
