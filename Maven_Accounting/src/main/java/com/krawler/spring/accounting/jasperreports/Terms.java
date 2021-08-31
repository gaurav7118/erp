/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class Terms {
    String termName="";
    String termValue="";
    String termCurrency = "";

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public String getTermValue() {
        return termValue;
    }

    public void setTermValue(String termValue) {
        this.termValue = termValue;
    }

    public String getTermCurrency() {
        return termCurrency;
    }

    public void setTermCurrency(String termCurrency) {
        this.termCurrency = termCurrency;
    }

    
}
