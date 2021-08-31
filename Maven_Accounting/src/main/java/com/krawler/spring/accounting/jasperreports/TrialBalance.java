/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class TrialBalance {
    private String code;
    private String description;
    private String debit;
    private String credit;
    private String copening;
    private String dopening;
    private String cending;
    private String dending;

    public String getCending() {
        return cending;
    }

    public void setCending(String cending) {
        this.cending = cending;
    }

    public String getCopening() {
        return copening;
    }

    public void setCopening(String copening) {
        this.copening = copening;
    }

    public String getDending() {
        return dending;
    }

    public void setDending(String dending) {
        this.dending = dending;
    }

    public String getDopening() {
        return dopening;
    }

    public void setDopening(String dopening) {
        this.dopening = dopening;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getDebit() {
        return debit;
    }

    public void setDebit(String debit) {
        this.debit = debit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
