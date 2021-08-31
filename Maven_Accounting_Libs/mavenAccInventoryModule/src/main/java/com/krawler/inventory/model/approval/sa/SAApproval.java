/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.sa;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.Customer;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.ApprovalType;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.spring.authHandler.authHandler;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vipin Gupta
 */
public class SAApproval {

    private String id;
    private StockAdjustment stockAdjustment;
    private double quantity;
    private ApprovalType approvalType;
    private ApprovalStatus approvalStatus;
    private Set<SADetailApproval> SADetailApprovalSet;
    private User inspector;
    private Customer customer; // customer field to fetch customer details in case job Work Stock In
    private Date createdOn;

    public SAApproval() {
        try {
            this.createdOn = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            SADetailApprovalSet = new HashSet<SADetailApproval>();
        } catch (ParseException ex) {
            Logger.getLogger(SAApproval.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(SAApproval.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Set<SADetailApproval> getSADetailApprovalSet() {
        return SADetailApprovalSet;
    }

    public void setSADetailApprovalSet(Set<SADetailApproval> SADetailApprovalSet) {
        this.SADetailApprovalSet = SADetailApprovalSet;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public ApprovalType getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(ApprovalType approvalType) {
        this.approvalType = approvalType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getInspector() {
        return inspector;
    }

    public void setInspector(User inspector) {
        this.inspector = inspector;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public StockAdjustment getStockAdjustment() {
        return stockAdjustment;
    }

    public void setStockAdjustment(StockAdjustment stockAdjustment) {
        this.stockAdjustment = stockAdjustment;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
}
