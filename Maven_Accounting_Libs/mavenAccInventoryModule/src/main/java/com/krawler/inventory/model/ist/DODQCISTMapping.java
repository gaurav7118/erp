/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

import com.krawler.inventory.model.stockout.StockAdjustment;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to store mapping of Delivery Order Details with Inter
 * Store Transfer Rejected created when QA approval flow is activated for the
 * company for Delivery Order Module.
 *
 * @author krawler
 */
public class DODQCISTMapping {

    private String ID;
    private String dodetailID;
    private String approvedSerials;
    private String rejectedSerials;
    private double approvedQty;
    private double rejectedQty;
    private double pickedQty;
    private double quantity;
    private double quantityDue;
    private InterStoreTransferRequest qcInterStoreTransferRequest;
    private Set<StockAdjustment> approvedStockOuts;
    private Set<DeliveryDetailInterStoreLocationMapping> pickedMapping;
    private Set<RejectedDODQCISTMapping> rejectedDODQCISTMappings;

    /**
     *
     */
    public DODQCISTMapping() {
        approvedStockOuts = new HashSet<>();
        pickedMapping = new HashSet<>();
        approvedStockOuts = new HashSet<>();
    }

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
    public String getDodetailID() {
        return dodetailID;
    }

    /**
     *
     * @param dodetailID
     */
    public void setDodetailID(String dodetailID) {
        this.dodetailID = dodetailID;
    }

    /**
     *
     * @return
     */
    public InterStoreTransferRequest getQcInterStoreTransferRequest() {
        return qcInterStoreTransferRequest;
    }

    /**
     *
     * @param qcInterStoreTransferRequest
     */
    public void setQcInterStoreTransferRequest(InterStoreTransferRequest qcInterStoreTransferRequest) {
        this.qcInterStoreTransferRequest = qcInterStoreTransferRequest;
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
    public Set<DeliveryDetailInterStoreLocationMapping> getPickedMapping() {
        return pickedMapping;
    }

    /**
     *
     * @param pickedMapping
     */
    public void setPickedMapping(Set<DeliveryDetailInterStoreLocationMapping> pickedMapping) {
        this.pickedMapping = pickedMapping;
    }

    /**
     *
     * @return
     */
    public Set<RejectedDODQCISTMapping> getRejectedDODQCISTMappings() {
        return rejectedDODQCISTMappings;
    }

    /**
     *
     * @param rejectedDODQCISTMappings
     */
    public void setRejectedDODQCISTMappings(Set<RejectedDODQCISTMapping> rejectedDODQCISTMappings) {
        this.rejectedDODQCISTMappings = rejectedDODQCISTMappings;
    }

    /**
     *
     * @return
     */
    public double getApprovedQty() {
        return approvedQty;
    }

    /**
     *
     * @param approvedQty
     */
    public void setApprovedQty(double approvedQty) {
        this.approvedQty = approvedQty;
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

    /**
     *
     * @return
     */
    public String getApprovedSerials() {
        return approvedSerials;
    }

    /**
     *
     * @param approvedSerials
     */
    public void setApprovedSerials(String approvedSerials) {
        this.approvedSerials = approvedSerials;
    }

    /**
     *
     * @return
     */
    public String getRejectedSerials() {
        return rejectedSerials;
    }

    /**
     *
     * @param rejectedSerials
     */
    public void setRejectedSerials(String rejectedSerials) {
        this.rejectedSerials = rejectedSerials;
    }
}
