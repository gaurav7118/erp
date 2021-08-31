/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.inventory.model.ist;

import com.krawler.inventory.model.stockout.StockAdjustment;

public class DeliveryDetailInterStoreLocationMapping {

    private String ID;
    private String deliveryOrderDetail;
    private InterStoreTransferRequest interStoreTransferRequest;
    private StockAdjustment stockAdjustment;
    private double pickedQty;
    private double packedQty;
    private double shippedQty;
    private DODQCISTMapping dodqcistmapping;
    private RejectedDODQCISTMapping pickRejectedDODQCISTMapping;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public StockAdjustment getStockAdjustment() {
        return stockAdjustment;
    }

    public void setStockAdjustment(StockAdjustment stockAdjustment) {
        this.stockAdjustment = stockAdjustment;
    }

    public String getDeliveryOrderDetail() {
        return deliveryOrderDetail;
    }

    public void setDeliveryOrderDetail(String deliveryOrderDetail) {
        this.deliveryOrderDetail = deliveryOrderDetail;
    }

    public InterStoreTransferRequest getInterStoreTransferRequest() {
        return interStoreTransferRequest;
    }

    public void setInterStoreTransferRequest(InterStoreTransferRequest interStoreTransferRequest) {
        this.interStoreTransferRequest = interStoreTransferRequest;
    }

    public double getPickedQty() {
        return pickedQty;
    }

    public void setPickedQty(double pickedQty) {
        this.pickedQty = pickedQty;
    }

    public double getPackedQty() {
        return packedQty;
    }

    public void setPackedQty(double packedQty) {
        this.packedQty = packedQty;
    }

    public double getShippedQty() {
        return shippedQty;
    }

    public void setShippedQty(double shippedQty) {
        this.shippedQty = shippedQty;
    }

    public DODQCISTMapping getDodqcistmapping() {
        return dodqcistmapping;
    }

    public void setDodqcistmapping(DODQCISTMapping dodqcistmapping) {
        this.dodqcistmapping = dodqcistmapping;
    }

    public RejectedDODQCISTMapping getPickRejectedDODQCISTMapping() {
        return pickRejectedDODQCISTMapping;
    }

    public void setPickRejectedDODQCISTMapping(RejectedDODQCISTMapping pickRejectedDODQCISTMapping) {
        this.pickRejectedDODQCISTMapping = pickRejectedDODQCISTMapping;
    }
}
