/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

import com.krawler.inventory.model.stockout.StockAdjustment;
import java.util.Set;

/**
 * This class is used to store mapping of stock present in repair store.
 * <b>Note:</b> It will be used when QA approval flow is activated for Delivery
 * Order.
 *
 * @author krawler
 */
public class RejectedDODQCISTMapping {

    private String ID;
    private double repairedQty;
    private double rejectedQty;
    private double pickedQty;
    private double quantity;
    private double quantityDue;
    private DODQCISTMapping dodqcistmapping;
    private InterStoreTransferRequest repairInterStoreTransferRequest;
    private Set<StockAdjustment> approvedStockOuts;
    private Set<DeliveryDetailInterStoreLocationMapping> pickedMappings;
    private Set<StockAdjustment> rejectedStockOuts;

    /**
     *
     * @return
     */
    public String getID() {
        return ID;
    }

    /**
     *
     * @param ID
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     *
     * @return
     */
    public double getRepairedQty() {
        return repairedQty;
    }

    /**
     *
     * @param repairedQty
     */
    public void setRepairedQty(double repairedQty) {
        this.repairedQty = repairedQty;
    }

    /**
     *
     * @return
     */
    public double getRejectedQty() {
        return rejectedQty;
    }

    /**
     *
     * @param rejectedQty
     */
    public void setRejectedQty(double rejectedQty) {
        this.rejectedQty = rejectedQty;
    }

    /**
     *
     * @return
     */
    public double getPickedQty() {
        return pickedQty;
    }

    /**
     *
     * @param pickedQty
     */
    public void setPickedQty(double pickedQty) {
        this.pickedQty = pickedQty;
    }

    /**
     *
     * @return
     */
    public DODQCISTMapping getDodqcistmapping() {
        return dodqcistmapping;
    }

    /**
     *
     * @param dodqcistmapping
     */
    public void setDodqcistmapping(DODQCISTMapping dodqcistmapping) {
        this.dodqcistmapping = dodqcistmapping;
    }

    /**
     *
     * @return
     */
    public InterStoreTransferRequest getRepairInterStoreTransferRequest() {
        return repairInterStoreTransferRequest;
    }

    /**
     *
     * @param repairInterStoreTransferRequest
     */
    public void setRepairInterStoreTransferRequest(InterStoreTransferRequest repairInterStoreTransferRequest) {
        this.repairInterStoreTransferRequest = repairInterStoreTransferRequest;
    }

    /**
     *
     * @return
     */
    public Set<StockAdjustment> getApprovedStockOuts() {
        return approvedStockOuts;
    }

    /**
     *
     * @param approvedStockOuts
     */
    public void setApprovedStockOuts(Set<StockAdjustment> approvedStockOuts) {
        this.approvedStockOuts = approvedStockOuts;
    }

    /**
     *
     * @return
     */
    public Set<DeliveryDetailInterStoreLocationMapping> getPickedMappings() {
        return pickedMappings;
    }

    /**
     *
     * @param pickedMappings
     */
    public void setPickedMappings(Set<DeliveryDetailInterStoreLocationMapping> pickedMappings) {
        this.pickedMappings = pickedMappings;
    }

    /**
     *
     * @return
     */
    public Set<StockAdjustment> getRejectedStockOuts() {
        return rejectedStockOuts;
    }

    /**
     *
     * @param rejectedStockOuts
     */
    public void setRejectedStockOuts(Set<StockAdjustment> rejectedStockOuts) {
        this.rejectedStockOuts = rejectedStockOuts;
    }

    /**
     *
     * @return
     */
    public double getQuantity() {
        return quantity;
    }

    /**
     *
     * @param quantity
     */
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    /**
     *
     * @return
     */
    public double getQuantityDue() {
        return quantityDue;
    }

    /**
     * 
     * @param quantityDue
     */
    public void setQuantityDue(double quantityDue) {
        this.quantityDue = quantityDue;
    }
}
