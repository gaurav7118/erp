/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockout;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author krawler
 */
public class StockAdjustmentCustomData extends AccCustomData {
    
    private String stockAdjustmentId;
    private StockAdjustment stockAdjustment;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public StockAdjustment getStockAdjustment() {
        return stockAdjustment;
    }

    public void setStockAdjustment(StockAdjustment stockAdjustment) {
        this.stockAdjustment = stockAdjustment;
    }

    public String getStockAdjustmentId() {
        return stockAdjustmentId;
    }

    public void setStockAdjustmentId(String stockAdjustmentId) {
        this.stockAdjustmentId = stockAdjustmentId;
    }
   
    
}
