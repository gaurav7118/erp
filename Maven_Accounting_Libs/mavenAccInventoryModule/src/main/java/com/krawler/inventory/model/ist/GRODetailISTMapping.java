/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class is used to store mapping of Goods Receipt Order Details with Inter
 * Store Transfer Rejected created when QA approval flow is activated for the
 * company.
 *
 * @author krawler
 */
public class GRODetailISTMapping {

    private String ID;
    private String groDetail;
    private double actualQty;
    private double quantityDue;
    private double approvedQty;
    private double rejectedQty;
    private String approvedSerials;
    private String rejectedSerials;
    private InterStoreTransferRequest interStoreTransferRequest;
    private Set<InterStoreTransferRequest> approvedInterStoreTransferRequests;
    private Set<RepairGRODetailISTMapping> rejectedInterStoreTransferRequests;

    public GRODetailISTMapping() {
        approvedInterStoreTransferRequests = new HashSet<>();
        rejectedInterStoreTransferRequests = new HashSet<>();
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getGroDetail() {
        return groDetail;
    }

    public void setGroDetail(String groDetail) {
        this.groDetail = groDetail;
    }

    public InterStoreTransferRequest getInterStoreTransferRequest() {
        return interStoreTransferRequest;
    }

    public void setInterStoreTransferRequest(InterStoreTransferRequest interStoreTransferRequest) {
        this.interStoreTransferRequest = interStoreTransferRequest;
    }

    public Set<InterStoreTransferRequest> getApprovedInterStoreTransferRequests() {
        return approvedInterStoreTransferRequests;
    }

    public void setApprovedInterStoreTransferRequests(Set<InterStoreTransferRequest> approvedInterStoreTransferRequests) {
        this.approvedInterStoreTransferRequests = approvedInterStoreTransferRequests;
    }

    public Set<RepairGRODetailISTMapping> getRejectedInterStoreTransferRequests() {
        return rejectedInterStoreTransferRequests;
    }

    public void setRejectedInterStoreTransferRequests(Set<RepairGRODetailISTMapping> rejectedInterStoreTransferRequests) {
        this.rejectedInterStoreTransferRequests = rejectedInterStoreTransferRequests;
    }

    public double getActualQty() {
        return actualQty;
    }

    public void setActualQty(double actualQty) {
        this.actualQty = actualQty;
    }

    public double getQuantityDue() {
        return quantityDue;
    }

    public void setQuantityDue(double quantityDue) {
        this.quantityDue = quantityDue;
    }

    public double getApprovedQty() {
        return approvedQty;
    }

    public void setApprovedQty(double approvedQty) {
        this.approvedQty = approvedQty;
    }

    public double getRejectedQty() {
        return rejectedQty;
    }

    public void setRejectedQty(double rejectedQty) {
        this.rejectedQty = rejectedQty;
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
        final GRODetailISTMapping other = (GRODetailISTMapping) obj;
        if (!Objects.equals(this.ID, other.ID)) {
            return false;
        }
        return true;
    }

    public String getApprovedSerials() {
        return approvedSerials;
    }

    public void setApprovedSerials(String approvedSerials) {
        this.approvedSerials = approvedSerials;
    }

    public String getRejectedSerials() {
        return rejectedSerials;
    }

    public void setRejectedSerials(String rejectedSerials) {
        this.rejectedSerials = rejectedSerials;
    }

}
