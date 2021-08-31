/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

import com.krawler.inventory.model.stockout.StockAdjustment;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class RepairWOCDISTMapping {

    private String ID;
    private double rejectedQty;
    private double rejectedQuantityDue;
    private Set<StockAdjustment> rejectedStockOuts;
    private Set<InterStoreTransferRequest> repairRejectedISTRequest;
    private InterStoreTransferRequest interStoreTransferRequest;
    private WOCDetailISTMapping wocdistmapping;
    
    public RepairWOCDISTMapping() {
        this.rejectedStockOuts = new HashSet<>();
        this.repairRejectedISTRequest = new HashSet<>();
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public InterStoreTransferRequest getInterStoreTransferRequest() {
        return interStoreTransferRequest;
    }

    public void setInterStoreTransferRequest(InterStoreTransferRequest interStoreTransferRequest) {
        this.interStoreTransferRequest = interStoreTransferRequest;
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

    public WOCDetailISTMapping getWocdistmapping() {
        return wocdistmapping;
    }

    public void setWocdistmapping(WOCDetailISTMapping wocdistmapping) {
        this.wocdistmapping = wocdistmapping;
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
        final RepairWOCDISTMapping other = (RepairWOCDISTMapping) obj;
        if (!Objects.equals(this.ID, other.ID)) {
            return false;
        }
        return true;
    }
   
}
