/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 * Table to store tax details received from AvaTax into database against a transaction row
 * Contains tax details of CQ, SO, DO, CI, SR
 * Used only when Avalara Integration is enabled
 */
public class TransactionDetailAvalaraTaxMapping {
    /**
     * ID of record to which tax details correspond
     * Primary key of table
     */
    private String parentRecordID;
    /**
     * JSON (converted into string) containing tax details received from AvaTax Service
     */
    private String avalaraTaxDetails;
    
    public String getParentRecordID() {
        return parentRecordID;
    }

    public void setParentRecordID(String parentRecordID) {
        this.parentRecordID = parentRecordID;
    }

    public String getAvalaraTaxDetails() {
        return avalaraTaxDetails;
    }

    public void setAvalaraTaxDetails(String avalaraTaxDetails) {
        this.avalaraTaxDetails = avalaraTaxDetails;
    }
}
