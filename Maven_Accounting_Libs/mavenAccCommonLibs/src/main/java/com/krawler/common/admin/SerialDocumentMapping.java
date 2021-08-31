/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class SerialDocumentMapping {

    private String id;
    private NewBatchSerial serialid;
    private String documentid;
    private int transactiontype;
    private Date expfromdate;
    private Date exptodate;
    private boolean ispurchasereturn;
    private boolean isconsignment;
    SerialCustomData serialCustomData;
    private RequestApprovalStatus requestApprovalStatus;//for Consignment Approval Flow
    private Set<User> approverSet;//for Consignment Approval Flow
    private double reusablecount;
    private User rejectedby;
    private int stockType;
    private int selectedsequence; // for user selected sequence of Serial while creating DO.

    public int getSelectedsequence() {
        return selectedsequence;
    }

    public void setSelectedsequence(int selectedsequence) {
        this.selectedsequence = selectedsequence;
    }

    public SerialDocumentMapping() {
        approverSet = new HashSet<User>();
    }

    public SerialCustomData getSerialCustomData() {
        return serialCustomData;
    }

    public void setSerialCustomData(SerialCustomData serialCustomData) {
        this.serialCustomData = serialCustomData;
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

    public NewBatchSerial getSerialid() {
        return serialid;
    }

    public void setSerialid(NewBatchSerial serialid) {
        this.serialid = serialid;
    }

    public int getTransactiontype() {
        return transactiontype;
    }

    public void setTransactiontype(int transactiontype) {
        this.transactiontype = transactiontype;
    }

    public Date getExpfromdate() {
        return expfromdate;
    }

    public void setExpfromdate(Date expfromdate) {
        this.expfromdate = expfromdate;
    }

    public Date getExptodate() {
        return exptodate;
    }

    public void setExptodate(Date exptodate) {
        this.exptodate = exptodate;
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

    public Set<User> getApproverSet() {
        return approverSet;
    }

    public void setApproverSet(Set<User> approverSet) {
        this.approverSet = approverSet;
    }

    public RequestApprovalStatus getRequestApprovalStatus() {
        return requestApprovalStatus;
    }

    public void setRequestApprovalStatus(RequestApprovalStatus requestApprovalStatus) {
        this.requestApprovalStatus = requestApprovalStatus;
    }

    public double getReusablecount() {
        return reusablecount;
    }

    public void setReusablecount(double reusablecount) {
        this.reusablecount = reusablecount;
    }

    public User getRejectedby() {
        return rejectedby;
    }

    public void setRejectedby(User rejectedby) {
        this.rejectedby = rejectedby;
    }

    public int getStockType() {
        return stockType;
    }

    public void setStockType(int stockType) {
        this.stockType = stockType;
    }
    
    }
