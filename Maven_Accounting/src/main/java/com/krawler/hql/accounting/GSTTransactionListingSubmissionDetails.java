/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class GSTTransactionListingSubmissionDetails {
    private String ID;
    private String identifier;// This it identifier which is remain same for every chunk of single request
    private int totalChunk; // total number of chunk 
    private int currentChunk; // current chunk
    private String taxRefNo;
    private String gstRegNo;
    private Date dtPeriodStart;// Submission data from 
    private Date dtPeriodEnd;// Submission data to
    private Date dtIAFCreation; // the Date submission is initiated 
    private String iafVersion; 
    private Date chunkResponseDateTime;// Response we got on which date and time
    private String requestPayload; // Actual Requst JSON for each chunk
    private int status; // 0 - Pending for authentication, 1 - for successful submission, 2 - Failure , 3 - Pending(under submission) 
    private String responseCode;// 10 for success 30 for failure
    private String messageCode;// If status is failure - ERROR flag 
    private String responsePayload;// Response from IRAS
    private Company company; 
    

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getTotalChunk() {
        return totalChunk;
    }

    public void setTotalChunk(int totalChunk) {
        this.totalChunk = totalChunk;
    }

    public int getCurrentChunk() {
        return currentChunk;
    }

    public void setCurrentChunk(int currentChunk) {
        this.currentChunk = currentChunk;
    }

    public String getTaxRefNo() {
        return taxRefNo;
    }

    public void setTaxRefNo(String taxRefNo) {
        this.taxRefNo = taxRefNo;
    }

    public String getGstRegNo() {
        return gstRegNo;
    }

    public void setGstRegNo(String gstRegNo) {
        this.gstRegNo = gstRegNo;
    }

    public Date getDtPeriodStart() {
        return dtPeriodStart;
    }

    public void setDtPeriodStart(Date dtPeriodStart) {
        this.dtPeriodStart = dtPeriodStart;
    }

    public Date getDtPeriodEnd() {
        return dtPeriodEnd;
    }

    public void setDtPeriodEnd(Date dtPeriodEnd) {
        this.dtPeriodEnd = dtPeriodEnd;
    }

    public Date getDtIAFCreation() {
        return dtIAFCreation;
    }

    public void setDtIAFCreation(Date dtIAFCreation) {
        this.dtIAFCreation = dtIAFCreation;
    }

    public String getIafVersion() {
        return iafVersion;
    }

    public void setIafVersion(String iafVersion) {
        this.iafVersion = iafVersion;
    }

    public Date getChunkResponseDateTime() {
        return chunkResponseDateTime;
    }

    public void setChunkResponseDateTime(Date chunkResponseDateTime) {
        this.chunkResponseDateTime = chunkResponseDateTime;
    }
    
    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    
    
}
