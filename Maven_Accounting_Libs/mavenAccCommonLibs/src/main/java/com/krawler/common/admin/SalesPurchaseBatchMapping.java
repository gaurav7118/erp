/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class SalesPurchaseBatchMapping {

    private String id;
    private ProductBatch purchaseBatch;
    private ProductBatch salesBatch;
    private double quantity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ProductBatch getPurchaseBatch() {
        return purchaseBatch;
    }

    public void setPurchaseBatch(ProductBatch purchaseBatch) {
        this.purchaseBatch = purchaseBatch;
    }

    public ProductBatch getSalesBatch() {
        return salesBatch;
    }

    public void setSalesBatch(ProductBatch salesBatch) {
        this.salesBatch = salesBatch;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
