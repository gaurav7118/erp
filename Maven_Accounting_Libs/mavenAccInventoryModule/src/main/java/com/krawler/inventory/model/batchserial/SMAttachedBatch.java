/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.batchserial;

import com.krawler.inventory.model.stockmovement.StockMovement;

/**
 *
 * @author Vipin Gupta
 */
public class SMAttachedBatch extends AttachedBatch {

    private StockMovement stockMovement;

    public SMAttachedBatch() {
    }

    public SMAttachedBatch(StockMovement stockMovement) {
        this.stockMovement = stockMovement;
    }

    public StockMovement getStockMovement() {
        return stockMovement;
    }

    public void setStockMovement(StockMovement stockMovement) {
        this.stockMovement = stockMovement;
    }
}
