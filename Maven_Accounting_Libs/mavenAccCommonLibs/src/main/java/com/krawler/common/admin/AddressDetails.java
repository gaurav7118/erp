/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class AddressDetails {
    private String ID;
    private String aliasName;
    private String address;
    private String county;
    private String city;
    private String state;
    private String stateCode;
    private String country;
    private String postalCode;
    private String phone;
    private String mobileNumber;
    private String fax;
    private String emailID;
    private String recipientName;
    private String contactPerson;
    private String contactPersonNumber;    
    private String contactPersonDesignation;  
    private String website;  
    private boolean isBillingAddress;
    private boolean isDefaultAddress;
    private Company company;
    
    public AddressDetails() {
        this.aliasName = "";
        this.address = "";
        this.county = "";
        this.city = "";
        this.state = "";
        this.stateCode = "";
        this.country = "";
        this.postalCode = "";
        this.phone = "";
        this.mobileNumber = "";
        this.fax = "";
        this.emailID = "";
        this.recipientName = "";
        this.contactPerson = "";
        this.contactPersonNumber = "";
        this.contactPersonDesignation="";
        this.website="";
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
    
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
    
    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPersonNumber() {
        return contactPersonNumber;
    }

    public void setContactPersonNumber(String contactPersonNumber) {
        this.contactPersonNumber = contactPersonNumber;
    }
    public String getContactPersonDesignation() {
        return contactPersonDesignation;
    }

    public void setContactPersonDesignation(String contactPersonDesignation) {
        this.contactPersonDesignation = contactPersonDesignation;
    }
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmailID() {
        return emailID;
    }

    public void setEmailID(String emailID) {
        this.emailID = emailID;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public boolean isIsBillingAddress() {
        return isBillingAddress;
    }

    public void setIsBillingAddress(boolean isBillingAddress) {
        this.isBillingAddress = isBillingAddress;
    }

    public boolean isIsDefaultAddress() {
        return isDefaultAddress;
    }

    public void setIsDefaultAddress(boolean isDefaultAddress) {
        this.isDefaultAddress = isDefaultAddress;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    public String getStateCode() {
        return stateCode;
    }
    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

}
