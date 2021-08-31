/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.common.admin;

import java.util.Date;
import java.util.Set;

/**
 *
 * @author krawler-user
 */
public class Company {

    private String companyID;
    private String companyLogo;
    private String companyName;
    private String subDomain;
    private String address;
    private String city;
    private State state;
    private String zipCode;
    private String phoneNumber;
    private String faxNumber;
    private String website;
    private User creator;
    private int deleted;
    private boolean optimizedflag;//true if data is fetching from jedetail_optimized table for balance sheet and P&L report.
    private String emailID;
    private Date createdOn;
    private Date modifiedOn;
    private KWLTimeZone timeZone;
    private KWLCurrency currency;
    private Country country;
    private Set<CompanyHoliday> holidays;
    private Language language;
    private boolean activated;
    private boolean emailFromCompanyCreator;
    private int switchpref;
    private boolean storeinvoiceamountdue;
    private int templateflag;// Used this flag to call diff function for single record export. 0 - default template, 1 - Fact template
    private int referralkey;
    private int smtpflow;
    private String smtppassword;
    private String mailserveraddress;
    private String mailserverport;
    private String replytoemail;
    private int isSelfService;

    public String getReplytoemail() {
        return replytoemail;
    }

    public void setReplytoemail(String replytoemail) {
        this.replytoemail = replytoemail;
    }
    
    
    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public KWLTimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(KWLTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public String getSubDomain() {
        return subDomain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getEmailID() {
        return emailID;
    }

    public void setEmailID(String emailID) {
        this.emailID = emailID;
    }

    public Set<CompanyHoliday> getHolidays() {
        return holidays;
    }

    public void setHolidays(Set<CompanyHoliday> holidays) {
        this.holidays = holidays;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public int isDeleted() {
        return deleted;
    }

    public boolean isOptimizedflag() {
        return optimizedflag;
    }

    public void setOptimizedflag(boolean optimizedflag) {
        this.optimizedflag = optimizedflag;
    }

    public boolean getActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public int getSwitchpref() {
        return switchpref;
    }
    
    public int getTemplateflag() {
        return templateflag;
    }

    public void setTemplateflag(int templateflag) {
        this.templateflag = templateflag;
    }

    public void setSwitchpref(int switchpref) {
        this.switchpref = switchpref;
    }

    public int getReferralkey() {
        return referralkey;
    }

    public void setReferralkey(int referralkey) {
        this.referralkey = referralkey;
    }
    
    public boolean isStoreinvoiceamountdue() {
        return storeinvoiceamountdue;
    }

    public void setStoreinvoiceamountdue(boolean storeinvoiceamountdue) {
        this.storeinvoiceamountdue = storeinvoiceamountdue;
    }

    public boolean isEmailFromCompanyCreator() {
        return emailFromCompanyCreator;
    }

    public void setEmailFromCompanyCreator(boolean emailFromCompanyCreator) {
        this.emailFromCompanyCreator = emailFromCompanyCreator;
    }

    public int getSmtpflow() {
        return smtpflow;
    }

    public void setSmtpflow(int smtpflow) {
        this.smtpflow = smtpflow;
    }

    public String getSmtppassword() {
        return smtppassword;
    }

    public void setSmtppassword(String smtppassword) {
        this.smtppassword = smtppassword;
    }

    public String getMailserveraddress() {
        return mailserveraddress;
    }

    public void setMailserveraddress(String mailserveraddress) {
        this.mailserveraddress = mailserveraddress;
    }

    public String getMailserverport() {
        return mailserverport;
    }

    public void setMailserverport(String mailserverport) {
        this.mailserverport = mailserverport;
    }
    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }
    public int getIsSelfService() {
        return isSelfService;
    }
    public void setIsSelfService(int isSelfService) {
        this.isSelfService = isSelfService;
    }    
}
