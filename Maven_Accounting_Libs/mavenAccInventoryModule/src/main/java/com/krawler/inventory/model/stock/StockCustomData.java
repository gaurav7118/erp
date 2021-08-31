/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stock;

import com.krawler.common.admin.AccCustomData;
import com.krawler.inventory.model.stockrequest.StockRequest;

/**
 *
 * @author krawler
 */
public class StockCustomData extends AccCustomData {

    private String stockId;
    private StockRequest stockRequest;
    private String moduleId;

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public StockRequest getStockRequest() {
        return stockRequest;
    }

    public void setStockRequest(StockRequest stockRequest) {
        this.stockRequest = stockRequest;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
