/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.batchserial;

import com.krawler.inventory.model.stockrequest.StockRequest;

/**
 *
 * @author Vipin Gupta
 */
public class SRAttachedBatch extends AttachedBatch{
    
    private StockRequest stockRequest;

    public SRAttachedBatch() {
    }

    public SRAttachedBatch(StockRequest stockRequest) {
        this.stockRequest = stockRequest;
    }

    public StockRequest getStockRequest() {
        return stockRequest;
    }

    public void setStockRequest(StockRequest stockRequest) {
        this.stockRequest = stockRequest;
    }
    
}
