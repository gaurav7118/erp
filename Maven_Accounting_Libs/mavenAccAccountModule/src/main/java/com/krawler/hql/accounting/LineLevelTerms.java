/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import java.util.HashMap;
import java.util.Map;

public class LineLevelTerms{  //At transcation Line Level Terms Table It work as TAX for Indain Company. 
    private String id;
    private String term;
    private Account account;
    private Account payableAccount;
    private Company company;
    private Integer sign;
    private String formula;
    private Integer deleted;
    private Long createdOn;
    private User creator;
    private boolean salesOrPurchase;  // 1- sales and 0 for purchase
    private double percentage;
    private double termAmount;
    private int termType; // term type is uesd in indian TAX 1.VAT ,2.Excise Duty,3.CST,4.Service Tax,5.Swachh Bharat Cess,6.Krishi Kalyan Cess
    private String formulaids;
    private double purchaseValueOrSaleValue;
    private double deductionOrAbatementPercent;
    private int taxType; // Tax Type = 0 for flat amount or = 1 if it is in percentage.
    private boolean isDefault;//For India Country Only.
    private int termSequence;
    private String formType;
    private boolean isAdditionalTax;//For India Country Only.
    private boolean includeInTDSCalculation;//For India Country Only.
    private MasterItem masteritem;
    private boolean OtherTermTaxable;
    private DefaultTerms defaultTerms;
    public static final Map<String, String> GSTName = new HashMap<String, String>();
    static {
        GSTName.put("OutputCGST", "00efad22-5f34-11e7-907b-a6006ad3dba0");
        GSTName.put("OutputSGST", "00efb45c-5f34-11e7-907b-a6006ad3dba0");
        GSTName.put("OutputIGST", "00efb196-5f34-11e7-907b-a6006ad3dba0");
        GSTName.put("OutputUTGST", "00efb75e-5f34-11e7-907b-a6006ad3dba0");
        GSTName.put("OutputCESS", "00efba42-5f34-11e7-907b-a6006ad3dba0");
        GSTName.put("InputCGST", "00efbbfa-5f34-11e7-907b-a6006ad3dba0");
        GSTName.put("InputSGST", "00efbf06-5f34-11e7-907b-a6006ad3dba0");
        GSTName.put("InputIGST", "00efbd8a-5f34-11e7-907b-a6006ad3dba0");
        GSTName.put("InputUTGST", "00efc0aa-5f34-11e7-907b-a6006ad3dba0");
        GSTName.put("InputCESS", "00efc226-5f34-11e7-907b-a6006ad3dba0");
    }
    /**
     * Account to be used when RCM is applicable in purchase invoice.
     */
    private Account creditNotAvailedAccount;
    
    public Account getPayableAccount() {
        return payableAccount;
    }

    public void setPayableAccount(Account payableAccount) {
        this.payableAccount = payableAccount;
    }
    public boolean isOtherTermTaxable() {
        return OtherTermTaxable;
    }

    public void setOtherTermTaxable(boolean OtherTermTaxable) {
        this.OtherTermTaxable = OtherTermTaxable;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public double getDeductionOrAbatementPercent() {
        return deductionOrAbatementPercent;
    }

    public void setDeductionOrAbatementPercent(double deductionOrAbatementPercent) {
        this.deductionOrAbatementPercent = deductionOrAbatementPercent;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getFormulaids() {
        return formulaids;
    }

    public void setFormulaids(String formulaids) {
        this.formulaids = formulaids;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public double getPurchaseValueOrSaleValue() {
        return purchaseValueOrSaleValue;
    }

    public void setPurchaseValueOrSaleValue(double purchaseValueOrSaleValue) {
        this.purchaseValueOrSaleValue = purchaseValueOrSaleValue;
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

    public int getTaxType() {
        return taxType;
    }

    public void setTaxType(int taxType) {
        this.taxType = taxType;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public double getTermAmount() {
        return termAmount;
    }

    public void setTermAmount(double termAmount) {
        this.termAmount = termAmount;
    }

    public int getTermSequence() {
        return termSequence;
    }

    public void setTermSequence(int termSequence) {
        this.termSequence = termSequence;
    }

    public int getTermType() {
        return termType;
    }

    public void setTermType(int termType) {
        this.termType = termType;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }
    public boolean isIsAdditionalTax() {
        return isAdditionalTax;
    }

    public void setIsAdditionalTax(boolean isAdditionalTax) {
        this.isAdditionalTax = isAdditionalTax;
    }

    public MasterItem getMasteritem() {
        return masteritem;
    }

    public void setMasteritem(MasterItem masteritem) {
        this.masteritem = masteritem;
    }

    @Override
    public String toString() {
        return "IndianTermsCompanyLevel{" + "id=" + id + ", term=" + term + ", account=" + account + ", company=" + company + ", sign=" + sign + ", formula=" + formula + ", deleted=" + deleted + ", createdOn=" + createdOn + ", creator=" + creator + ", salesOrPurchase=" + salesOrPurchase + ", percentage=" + percentage + ", termAmount=" + termAmount + ", termType=" + termType + ", formulaids=" + formulaids + ", purchaseValueOrSaleValue=" + purchaseValueOrSaleValue + ", deductionOrAbatementPercent=" + deductionOrAbatementPercent + ", taxType=" + taxType + ", isDefault=" + isDefault + ", termSequence=" + termSequence + '}';
    }

    public boolean isIncludeInTDSCalculation() {
        return includeInTDSCalculation;
    }
    public void setIncludeInTDSCalculation(boolean includeInTDSCalculation) {
        this.includeInTDSCalculation = includeInTDSCalculation;
    }

    public Account getCreditNotAvailedAccount() {
        return creditNotAvailedAccount;
    }

    public void setCreditNotAvailedAccount(Account creditNotAvailedAccount) {
        this.creditNotAvailedAccount = creditNotAvailedAccount;
    }

    public DefaultTerms getDefaultTerms() {
        return defaultTerms;
    }

    public void setDefaultTerms(DefaultTerms defaultTerms) {
        this.defaultTerms = defaultTerms;
    }
}