/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class PartyLedger {
    private String person = "";
    private String sp_agent = "";
    private String code = "";
    private String propaddr ="";
    private String date = "";
    private String entryNumber = "";
    private String entryType = "";
    private String personid = "";
    private String currency = "";
    private String basecurr = "";
    private double open_debit = 0;
    private double open_credit = 0;
    private double debit = 0;
    private double credit = 0;
    private double closing = 0;

    public String getBasecurr() {
        return basecurr;
    }

    public void setBasecurr(String basecurr) {
        this.basecurr = basecurr;
    }
    
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getPersonid() {
        return personid;
    }

    public void setPersonid(String personid) {
        this.personid = personid;
    }
    
    public double getClosing() {
        return closing;
    }

    public void setClosing(double closing) {
        this.closing = closing;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getDebit() {
        return debit;
    }

    public void setDebit(double debit) {
        this.debit = debit;
    }

    public String getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(String entryNumber) {
        this.entryNumber = entryNumber;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public double getOpen_credit() {
        return open_credit;
    }

    public void setOpen_credit(double open_credit) {
        this.open_credit = open_credit;
    }

    public double getOpen_debit() {
        return open_debit;
    }

    public void setOpen_debit(double open_debit) {
        this.open_debit = open_debit;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getPropaddr() {
        return propaddr;
    }

    public void setPropaddr(String propaddr) {
        this.propaddr = propaddr;
    }

    public String getSp_agent() {
        return sp_agent;
    }

    public void setSp_agent(String sp_agent) {
        this.sp_agent = sp_agent;
    }

}
