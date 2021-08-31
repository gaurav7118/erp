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
public class RevenueJEInvoiceMapping {
        private String id;
    private String InvoiceId;
    private String JournalEntryId;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInvoiceId() {
        return InvoiceId;
    }

    public void setInvoiceId(String InvoiceId) {
        this.InvoiceId = InvoiceId;
    }

    public String getJournalEntryId() {
        return JournalEntryId;
    }

    public void setJournalEntryId(String JournalEntryId) {
        this.JournalEntryId = JournalEntryId;
    }

    


}
