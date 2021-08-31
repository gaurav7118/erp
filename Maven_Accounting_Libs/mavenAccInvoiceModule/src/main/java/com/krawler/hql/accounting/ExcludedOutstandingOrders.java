/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Date;

public class ExcludedOutstandingOrders {

    private String ID;
    private Company company;
    private int excludeOrGenerate;//0-Exclude Record 1-Generate Record
    private Date generatedDate;
    private Invoice invoice;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getExcludeOrGenerate() {
        return excludeOrGenerate;
    }

    public void setExcludeOrGenerate(int excludeOrGenerate) {
        this.excludeOrGenerate = excludeOrGenerate;
    }

    public Date getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(Date generatedDate) {
        this.generatedDate = generatedDate;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
