/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.model.stockout.StockAdjustment;

/**
 *
 * @author krawler
 */
public class GRODetailsStockOutISTMapping {

    private String id;
    private GoodsReceiptOrderDetails goodsReceiptOrderDetails;
    private StockAdjustment stockAdjustment;
    private InterStoreTransferRequest interStoreTransferRequest;
    private double outQty;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GoodsReceiptOrderDetails getGoodsReceiptOrderDetails() {
        return goodsReceiptOrderDetails;
    }

    public void setGoodsReceiptOrderDetails(GoodsReceiptOrderDetails goodsReceiptOrderDetails) {
        this.goodsReceiptOrderDetails = goodsReceiptOrderDetails;
    }

    public StockAdjustment getStockAdjustment() {
        return stockAdjustment;
    }

    public void setStockAdjustment(StockAdjustment stockAdjustment) {
        this.stockAdjustment = stockAdjustment;
    }

    public InterStoreTransferRequest getInterStoreTransferRequest() {
        return interStoreTransferRequest;
    }

    public void setInterStoreTransferRequest(InterStoreTransferRequest interStoreTransferRequest) {
        this.interStoreTransferRequest = interStoreTransferRequest;
    }

    public double getOutQty() {
        return outQty;
    }

    public void setOutQty(double outQty) {
        this.outQty = outQty;
    }

}
