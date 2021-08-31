/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.batchserial;

import com.krawler.inventory.model.stockout.StockAdjustment;

/**
 *
 * @author Vipin Gupta
 */
public class SAAttachedBatch extends AttachedBatch{
    
    private StockAdjustment stockAdjustment;

    public SAAttachedBatch() {
    }

    public SAAttachedBatch(StockAdjustment stockAdjustment) {
        this.stockAdjustment = stockAdjustment;
    }

    public StockAdjustment getStockAdjustment() {
        return stockAdjustment;
    }

    public void setStockAdjustment(StockAdjustment stockAdjustment) {
        this.stockAdjustment = stockAdjustment;
    }
    
    
}
