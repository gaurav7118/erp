/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class GSTTransactionListingMapping {
    private int currentChunk;
    private int totalChunk;
    private String messages;
    private String taxRefNo;
    private String gstRegNo;
    private String dtPeriodStart;
    private String dtPeriodEnd;
    private String identifier;
    private String dtIAFCreation;
    private String iafVersion;

    public void setCurrentChunk(int currentChunk) {
        this.currentChunk = currentChunk;
    }

    public void setTotalChunk(int totalChunk) {
        this.totalChunk = totalChunk;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public void setTaxRefNo(String taxRefNo) {
        this.taxRefNo = taxRefNo;
    }

    public void setGstRegNo(String gstRegNo) {
        this.gstRegNo = gstRegNo;
    }

    public void setDtPeriodStart(String dtPeriodStart) {
        this.dtPeriodStart = dtPeriodStart;
    }

    public void setDtPeriodEnd(String dtPeriodEnd) {
        this.dtPeriodEnd = dtPeriodEnd;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setDtIAFCreation(String dtIAFCreation) {
        this.dtIAFCreation = dtIAFCreation;
    }

    public void setIafVersion(String iafVersion) {
        this.iafVersion = iafVersion;
    }
    
    public String getTransactionListingJSONString(){
        return "{"
                + "\"filingInfo\": {"
                    + "\"taxRefNo\": \""+taxRefNo+"\","
                    + "\"gstRegNo\": \""+gstRegNo+"\","
                    + "\"dtPeriodStart\": \""+dtPeriodStart+"\","
                    + "\"dtPeriodEnd\": \""+dtPeriodEnd+"\""
                + "},"
                + "\"data\": {"
                    + "\"identifier\": \""+identifier+"\","
                    + "\"currentChunk\": \""+currentChunk+"\","
                    + "\"totalChunks\": \""+totalChunk+"\","
                    + "\"message\":\""+messages+"\","
                    + "\"dtIAFCreation\": \""+dtIAFCreation+"\","
                    + "\"iafVersion\": \""+iafVersion+"\""
                + "}"
            + "}";
    
    }
    
}
