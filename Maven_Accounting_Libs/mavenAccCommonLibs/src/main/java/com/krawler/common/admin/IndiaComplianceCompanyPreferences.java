/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class IndiaComplianceCompanyPreferences {
    
    private String id;
    private Company company;
    private boolean isaddresschanged;
    private boolean resposiblePersonAddChanged;
    private String resposiblePersonPAN;
    private String resposiblePersonPostal;
    private String resposiblePersonEmail;
    private String resposiblePersonMobNumber;
    private String resposiblePersonTeleNumber;
    private String resposiblePersonAddress;
    private String resposiblePersonstate ;
    private String AssessmentYear ;
    private String GTAKKCPaybleAccount ;
    private String GTASBCPaybleAccount ;
    private String CINnumber ;
    private boolean isGSTApplicable ;
    private boolean showIndiaCompanyPreferencesTab ;
    private String gstin ;
    private boolean istaxonadvancereceipt;
    private boolean istcsapplicable;
    private boolean istdsapplicable;
    private double tdsInterestRate;//To calculate Interest on TDS in NOP wise Report.
    /**
     * This will be used to configure RCM applicability for India country. Once
     * we activated this functionality, then "RCM applicable" will be shown in
     * Payment Against Vendor and "Advance To Vendor" will be shown in Payment
     * against GL form.
     */
    private boolean rcmApplicable; 
    private String IGSTAccount;// to be used for custom duty ERM-1011
    private String customDutyAccount; // to be used for custom duty ERM-1011

    private boolean isitcapplicable;

    public boolean isIsitcapplicable() {
        return isitcapplicable;
    }

    public void setIsitcapplicable(boolean isitcapplicable) {
        this.isitcapplicable = isitcapplicable;
    }
    
    public boolean isIstaxonadvancereceipt() {
        return istaxonadvancereceipt;
    }

    public void setIstaxonadvancereceipt(boolean istaxonadvancereceipt) {
        this.istaxonadvancereceipt = istaxonadvancereceipt;
    }

    public boolean isIstcsapplicable() {
        return istcsapplicable;
    }

    public void setIstcsapplicable(boolean istcsapplicable) {
        this.istcsapplicable = istcsapplicable;
    }

    public boolean isIstdsapplicable() {
        return istdsapplicable;
    }

    public void setIstdsapplicable(boolean istdsapplicable) {
        this.istdsapplicable = istdsapplicable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
    public boolean isIsaddresschanged() {
         return isaddresschanged;
    }

    public void setIsaddresschanged(boolean isaddresschanged) {
        this.isaddresschanged = isaddresschanged;
    }

    public String getResposiblePersonPAN() {
        return resposiblePersonPAN;
    }

    public void setResposiblePersonPAN(String resposiblePersonPAN) {
        this.resposiblePersonPAN = resposiblePersonPAN;
    }

    public String getResposiblePersonPostal() {
        return resposiblePersonPostal;
    }

    public void setResposiblePersonPostal(String resposiblePersonPostal) {
        this.resposiblePersonPostal = resposiblePersonPostal;
    }

    public String getResposiblePersonEmail() {
        return resposiblePersonEmail;
    }

    public void setResposiblePersonEmail(String resposiblePersonEmail) {
        this.resposiblePersonEmail = resposiblePersonEmail;
    }

    public String getResposiblePersonMobNumber() {
        return resposiblePersonMobNumber;
    }

    public void setResposiblePersonMobNumber(String resposiblePersonMobNumber) {
        this.resposiblePersonMobNumber = resposiblePersonMobNumber;
    }

    public String getResposiblePersonTeleNumber() {
        return resposiblePersonTeleNumber;
    }

    public void setResposiblePersonTeleNumber(String resposiblePersonTeleNumber) {
        this.resposiblePersonTeleNumber = resposiblePersonTeleNumber;
    }

    public String getResposiblePersonAddress() {
        return resposiblePersonAddress;
    }

    public void setResposiblePersonAddress(String resposiblePersonAddress) {
        this.resposiblePersonAddress = resposiblePersonAddress;
    }

    public String getResposiblePersonstate() {
        return resposiblePersonstate;
    }

    public void setResposiblePersonstate(String resposiblePersonstate) {
        this.resposiblePersonstate = resposiblePersonstate;
    }

    public boolean isResposiblePersonAddChanged() {
        return resposiblePersonAddChanged;
    }

    public void setResposiblePersonAddChanged(boolean resposiblePersonAddChanged) {
        this.resposiblePersonAddChanged = resposiblePersonAddChanged;
    }

    public String getAssessmentYear() {
        return AssessmentYear;
    }

    public void setAssessmentYear(String AssessmentYear) {
        this.AssessmentYear = AssessmentYear;
    }
    
    public String getGTAKKCPaybleAccount() {
        return GTAKKCPaybleAccount;
    }

    public void setGTAKKCPaybleAccount(String GTAKKCPaybleAccount) {
        this.GTAKKCPaybleAccount = GTAKKCPaybleAccount;
    }

    public String getGTASBCPaybleAccount() {
        return GTASBCPaybleAccount;
    }

    public void setGTASBCPaybleAccount(String GTASBCPaybleAccount) {
        this.GTASBCPaybleAccount = GTASBCPaybleAccount;
    }

    public String getCINnumber() {
        return CINnumber;
    }

    public void setCINnumber(String CINnumber) {
        this.CINnumber = CINnumber;
    }

    public boolean isIsGSTApplicable() {
        return isGSTApplicable;
    }

    public void setIsGSTApplicable(boolean isGSTApplicable) {
        this.isGSTApplicable = isGSTApplicable;
    }

    public boolean isShowIndiaCompanyPreferencesTab() {
        return showIndiaCompanyPreferencesTab;
    }

    public void setShowIndiaCompanyPreferencesTab(boolean showIndiaCompanyPreferencesTab) {
        this.showIndiaCompanyPreferencesTab = showIndiaCompanyPreferencesTab;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public double getTdsInterestRate() {
        return tdsInterestRate;
    }
    public void setTdsInterestRate(double tdsInterestRate) {
        this.tdsInterestRate = tdsInterestRate;
    }

    public boolean isRcmApplicable() {
        return rcmApplicable;
    }

    public void setRcmApplicable(boolean rcmApplicable) {
        this.rcmApplicable = rcmApplicable;
    }

    public String getIGSTAccount() {
        return IGSTAccount;
    }

    public void setIGSTAccount(String IGSTAccount) {
        this.IGSTAccount = IGSTAccount;
    }

    public String getCustomDutyAccount() {
        return customDutyAccount;
    }

    public void setCustomDutyAccount(String customDutyAccount) {
        this.customDutyAccount = customDutyAccount;
    }
    
}
