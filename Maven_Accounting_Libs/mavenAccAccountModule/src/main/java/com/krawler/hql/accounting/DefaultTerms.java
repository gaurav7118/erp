/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Country;
import com.krawler.common.admin.State;



public class DefaultTerms {

    private String id;
    private String term;
    private String formula;
    private Integer sign;
   // private String account;
    private boolean salesOrPurchase;
    private Double percentage;
    private Integer termtype;
    private String accountname;
    private Country country;
    private State state;
    private int taxType; // Tax Type = 0 for flat amount or = 1 if it is in percentage.
    private boolean isDefault;//For India Country Only.
    private int termSequence;
    private String creditNotAvailedAccountName;
    private String advancPayableAccountName;     //name of account to be mapped to term as
    private String oppositeTermId;
    
    public String getAdvancPayableAccountName() {
        return advancPayableAccountName;
    }

    public void setAdvancPayableAccountName(String advancPayableAccountName) {
        this.advancPayableAccountName = advancPayableAccountName;
    }
    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
    
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getAccountname() {
        return accountname;
    }

    public void setAccountname(String accountname) {
        this.accountname = accountname;
    }
    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public boolean isSalesOrPurchase() {
        return salesOrPurchase;
    }

    public void setSalesOrPurchase(boolean salesOrPurchase) {
        this.salesOrPurchase = salesOrPurchase;
    }

    public Integer getSign() {
        return sign;
    }

    public void setSign(Integer sign) {
        this.sign = sign;
    }


    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Integer getTermtype() {
        return termtype;
    }

    public void setTermtype(Integer termtype) {
        this.termtype = termtype;
    }
    public int getTaxType() {
        return taxType;
    }

    public void setTaxType(int taxType) {
        this.taxType = taxType;
    }
    
    public boolean isIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public int getTermSequence() {
        return termSequence;
    }

    public void setTermSequence(int termSequence) {
        this.termSequence = termSequence;
    }

    public String getCreditNotAvailedAccountName() {
        return creditNotAvailedAccountName;
    }

    public void setCreditNotAvailedAccountName(String creditNotAvailedAccountName) {
        this.creditNotAvailedAccountName = creditNotAvailedAccountName;
    }

    public String getOppositeTermId() {
        return oppositeTermId;
    }

    public void setOppositeTermId(String oppositeTermId) {
        this.oppositeTermId = oppositeTermId;
    }
}
