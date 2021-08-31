/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

import com.krawler.inventory.model.stockout.StockAdjustment;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class is used to store mapping of stock present in repair store. It will
 * be used if QA approval flow is activated.
 *
 * @see GRODetailISTMapping
 * @author krawler
 */
public class RepairGRODetailISTMapping {

    private String ID;
    private double rejectedQty;
    private double rejectedQuantityDue;
    private Set<StockAdjustment> rejectedStockOuts;
    private Set<InterStoreTransferRequest> repairRejectedISTRequest;
    private InterStoreTransferRequest interStoreTransferRequest;
    private GRODetailISTMapping grodistmapping;
    
    public RepairGRODetailISTMapping() {
        this.rejectedStockOuts = new HashSet<>();
        this.repairRejectedISTRequest = new HashSet<>();
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getRejectedQty() {
        return rejectedQty;
    }

    public void setRejectedQty(double rejectedQty) {
        this.rejectedQty = rejectedQty;
    }

    public double getRejectedQuantityDue() {
        return rejectedQuantityDue;
    }

    public void setRejectedQuantityDue(double rejectedQuantityDue) {
        this.rejectedQuantityDue = rejectedQuantityDue;
    }

    public Set<StockAdjustment> getRejectedStockOuts() {
        return rejectedStockOuts;
    }

    public void setRejectedStockOuts(Set<StockAdjustment> rejectedStockOuts) {
        this.rejectedStockOuts = rejectedStockOuts;
    }

    public Set<InterStoreTransferRequest> getRepairRejectedISTRequest() {
        return repairRejectedISTRequest;
    }

    public void setRepairRejectedISTRequest(Set<InterStoreTransferRequest> repairRejectedISTRequest) {
        this.repairRejectedISTRequest = repairRejectedISTRequest;
    }

    public InterStoreTransferRequest getInterStoreTransferRequest() {
        return interStoreTransferRequest;
    }

    public void setInterStoreTransferRequest(InterStoreTransferRequest interStoreTransferRequest) {
        this.interStoreTransferRequest = interStoreTransferRequest;
    }

    public GRODetailISTMapping getGrodistmapping() {
        return grodistmapping;
    }

    public void setGrodistmapping(GRODetailISTMapping grodistmapping) {
        this.grodistmapping = grodistmapping;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.ID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RepairGRODetailISTMapping other = (RepairGRODetailISTMapping) obj;
        if (!Objects.equals(this.ID, other.ID)) {
            return false;
        }
        return true;
    }

}
