/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.stocktransfer;

import com.krawler.common.admin.User;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.ApprovalType;
import com.krawler.inventory.model.stockmovement.TransactionModule;
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
public class StockTransferApproval {

    private String id;
    private String stockTransferId;
    private double quantity;
    private TransactionModule transactionModule;
    private ApprovalType approvalType;
    private ApprovalStatus approvalStatus;
    private Set<StockTransferDetailApproval> stockTransferDetailApprovals;
    private User inspector;
    private Date createdOn;

    public StockTransferApproval() {
        try {
            this.stockTransferDetailApprovals = new HashSet<StockTransferDetailApproval>();
            this.createdOn = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
        } catch (ParseException ex) {
            Logger.getLogger(StockTransferApproval.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockTransferApproval.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    public String getStockTransferId() {
        return stockTransferId;
    }

    public void setStockTransferId(String stockTransferId) {
        this.stockTransferId = stockTransferId;
    }

    public TransactionModule getTransactionModule() {
        return transactionModule;
    }

    public void setTransactionModule(TransactionModule transactionModule) {
        this.transactionModule = transactionModule;
    }

    public Set<StockTransferDetailApproval> getStockTransferDetailApprovals() {
        return stockTransferDetailApprovals;
    }

    public void setStockTransferDetailApprovals(Set<StockTransferDetailApproval> stockTransferDetailApprovals) {
        this.stockTransferDetailApprovals = stockTransferDetailApprovals;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
}
