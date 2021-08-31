/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import java.util.Date;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class LocationBatchDocumentMapping {

    private String id;
    private NewProductBatch batchmapid;
    private String documentid;
    private int transactiontype;
    private Date mfgdate;
    private Date expdate;
    private double quantity;
    private boolean ispurchasereturn;
    private boolean isconsignment;
    private RequestApprovalStatus requestApprovalStatus;//for Consignment Approval Flow
    private Set<User> approverSet;//for Consignment Approval Flow
    private Set<LocationBatchRejectorMapping> rejectedBy;//for Consignment Approval Flow
    private double approvedQuantity;//for Consignment Approval Flow
    private double rejectedQuantity;//for Consignment Approval Flow
    private int stockType;
    private int selectedsequence; // for user selected sequence of batch while creating DO.
    
    public int getSelectedsequence() {
        return selectedsequence;
    }

    public void setSelectedsequence(int selectedsequence) {
        this.selectedsequence = selectedsequence;
    }
    
    public NewProductBatch getBatchmapid() {
        return batchmapid;
    }

    public void setBatchmapid(NewProductBatch batchmapid) {
        this.batchmapid = batchmapid;
    }

    public String getDocumentid() {
        return documentid;
    }

    public void setDocumentid(String documentid) {
        this.documentid = documentid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTransactiontype() {
        return transactiontype;
    }

    public void setTransactiontype(int transactiontype) {
        this.transactiontype = transactiontype;
    }

    public Date getExpdate() {
        return expdate;
    }

    public void setExpdate(Date expdate) {
        this.expdate = expdate;
    }

    public Date getMfgdate() {
        return mfgdate;
    }

    public void setMfgdate(Date mfgdate) {
        this.mfgdate = mfgdate;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public boolean isIspurchasereturn() {
        return ispurchasereturn;
    }

    public void setIspurchasereturn(boolean ispurchasereturn) {
        this.ispurchasereturn = ispurchasereturn;
    }

    public boolean isIsconsignment() {
        return isconsignment;
    }

    public void setIsconsignment(boolean isconsignment) {
        this.isconsignment = isconsignment;
    }

    public double getApprovedQuantity() {
        return approvedQuantity;
    }

    public void setApprovedQuantity(double approvedQuantity) {
        this.approvedQuantity = approvedQuantity;
    }

    public Set<User> getApproverSet() {
        return approverSet;
    }

    public void setApproverSet(Set<User> approverSet) {
        this.approverSet = approverSet;
    }

    public Set<LocationBatchRejectorMapping> getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(Set<LocationBatchRejectorMapping> rejectedBy) {
        this.rejectedBy = rejectedBy;
    }
    
    public double getRejectedQuantity() {
        return rejectedQuantity;
    }

    public void setRejectedQuantity(double rejectedQuantity) {
        this.rejectedQuantity = rejectedQuantity;
    }

    public RequestApprovalStatus getRequestApprovalStatus() {
        return requestApprovalStatus;
    }

    public void setRequestApprovalStatus(RequestApprovalStatus requestApprovalStatus) {
        this.requestApprovalStatus = requestApprovalStatus;
    }

    public int getStockType() {
        return stockType;
    }

    public void setStockType(int stockType) {
        this.stockType = stockType;
    }
    
    
}
