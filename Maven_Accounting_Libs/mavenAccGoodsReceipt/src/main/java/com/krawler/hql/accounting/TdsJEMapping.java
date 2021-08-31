/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class TdsJEMapping {
    private String ID;
    private JournalEntry journalEntry;
    private double tdsLineAmount;
    private double tdsRate;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public double getTdsLineAmount() {
        return tdsLineAmount;
    }

    public void setTdsLineAmount(double tdsLineAmount) {
        this.tdsLineAmount = tdsLineAmount;
    }

    public double getTdsRate() {
        return tdsRate;
    }

    public void setTdsRate(double tdsRate) {
        this.tdsRate = tdsRate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
    
}
