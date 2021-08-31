/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class MalaysianGSTSummaryTable {
    String summary = "";
    String amount = "";
    String amountmyr = "";
    String tax = "";
    String taxmyr = "";

    public String getAmountmyr() {
        return amountmyr;
    }

    public void setAmountmyr(String amountmyr) {
        this.amountmyr = amountmyr;
    }

    public String getTaxmyr() {
        return taxmyr;
    }

    public void setTaxmyr(String taxmyr) {
        this.taxmyr = taxmyr;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

}
