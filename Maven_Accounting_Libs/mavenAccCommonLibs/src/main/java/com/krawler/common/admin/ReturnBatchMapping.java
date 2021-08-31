/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

public class ReturnBatchMapping {

    private String id;
    private ProductBatch batchtomap;
    private ProductBatch batchmap;
    private int returntype;
    private double quantity;

    public ProductBatch getBatchmap() {
        return batchmap;
    }

    public void setBatchmap(ProductBatch batchmap) {
        this.batchmap = batchmap;
    }

    public ProductBatch getBatchtomap() {
        return batchtomap;
    }

    public void setBatchtomap(ProductBatch batchtomap) {
        this.batchtomap = batchtomap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getReturntype() {
        return returntype;
    }

    public void setReturntype(int returntype) {
        this.returntype = returntype;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
