/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class WOCDetailISTMapping {

    private String ID;
    private String wocDetail;
    private double actualQty;
    private double quantityDue;
    private double approvedQty;
    private double rejectedQty;
    private String approvedSerials;
    private String rejectedSerials;
    private InterStoreTransferRequest interStoreTransferRequest;
    private Set<InterStoreTransferRequest> approvedInterStoreTransferRequests;
    private Set<RepairWOCDISTMapping> rejectedInterStoreTransferRequests;
    
    public WOCDetailISTMapping() {
        approvedInterStoreTransferRequests = new HashSet<>();
        rejectedInterStoreTransferRequests = new HashSet<>();
    }
     

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getActualQty() {
        return actualQty;
    }

    public void setActualQty(double actualQty) {
        this.actualQty = actualQty;
    }

    public Set<InterStoreTransferRequest> getApprovedInterStoreTransferRequests() {
        return approvedInterStoreTransferRequests;
    }

    public void setApprovedInterStoreTransferRequests(Set<InterStoreTransferRequest> approvedInterStoreTransferRequests) {
        this.approvedInterStoreTransferRequests = approvedInterStoreTransferRequests;
    }

    public double getApprovedQty() {
        return approvedQty;
    }

    public void setApprovedQty(double approvedQty) {
        this.approvedQty = approvedQty;
    }

    public String getApprovedSerials() {
        return approvedSerials;
    }

    public void setApprovedSerials(String approvedSerials) {
        this.approvedSerials = approvedSerials;
    }

    public InterStoreTransferRequest getInterStoreTransferRequest() {
        return interStoreTransferRequest;
    }

    public void setInterStoreTransferRequest(InterStoreTransferRequest interStoreTransferRequest) {
        this.interStoreTransferRequest = interStoreTransferRequest;
    }

    public double getQuantityDue() {
        return quantityDue;
    }

    public void setQuantityDue(double quantityDue) {
        this.quantityDue = quantityDue;
    }

    public Set<RepairWOCDISTMapping> getRejectedInterStoreTransferRequests() {
        return rejectedInterStoreTransferRequests;
    }

    public void setRejectedInterStoreTransferRequests(Set<RepairWOCDISTMapping> rejectedInterStoreTransferRequests) {
        this.rejectedInterStoreTransferRequests = rejectedInterStoreTransferRequests;
    }

    public double getRejectedQty() {
        return rejectedQty;
    }

    public void setRejectedQty(double rejectedQty) {
        this.rejectedQty = rejectedQty;
    }

    public String getRejectedSerials() {
        return rejectedSerials;
    }

    public void setRejectedSerials(String rejectedSerials) {
        this.rejectedSerials = rejectedSerials;
    }

    public String getWocDetail() {
        return wocDetail;
    }

    public void setWocDetail(String wocDetail) {
        this.wocDetail = wocDetail;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.ID);
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
        final WOCDetailISTMapping other = (WOCDetailISTMapping) obj;
        if (!Objects.equals(this.ID, other.ID)) {
            return false;
        }
        return true;
    }
}
