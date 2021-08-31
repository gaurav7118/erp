/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

/**
 *
 * @author krawler
 */
public class MRPAddressDetails {

    private String id;
    private MRPContract mrpcontract;
    private MRPContractDetails mrpcontractdetails;
    private String addresscombo;
    private String aliasname;
    private String address;
    private String county;
    private String city;
    private String state;
    private String country;
    private String postalcode;
    private String phone;
    private String mobilenumber;
    private String fax;
    private String emailid;
    private String recipientname;
    private String contactperson;
    private String contactpersonnumber;
    private String contactpersondesignation;
    private String website;
    private String route;
    private boolean isbilling;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getAddresscombo() {
        return addresscombo;
    }

    public void setAddresscombo(String addresscombo) {
        this.addresscombo = addresscombo;
    }

    public String getAliasname() {
        return aliasname;
    }

    public void setAliasname(String aliasname) {
        this.aliasname = aliasname;
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

    public String getContactperson() {
        return contactperson;
    }

    public void setContactperson(String contactperson) {
        this.contactperson = contactperson;
    }

    public String getContactpersondesignation() {
        return contactpersondesignation;
    }

    public void setContactpersondesignation(String contactpersondesignation) {
        this.contactpersondesignation = contactpersondesignation;
    }

    public String getContactpersonnumber() {
        return contactpersonnumber;
    }

    public void setContactpersonnumber(String contactpersonnumber) {
        this.contactpersonnumber = contactpersonnumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmailid() {
        return emailid;
    }

    public void setEmailid(String emailid) {
        this.emailid = emailid;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIsbilling() {
        return isbilling;
    }

    public void setIsbilling(boolean isbilling) {
        this.isbilling = isbilling;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public MRPContract getMrpcontract() {
        return mrpcontract;
    }

    public void setMrpcontract(MRPContract mrpcontract) {
        this.mrpcontract = mrpcontract;
    }

    public MRPContractDetails getMrpcontractdetails() {
        return mrpcontractdetails;
    }

    public void setMrpcontractdetails(MRPContractDetails mrpcontractdetails) {
        this.mrpcontractdetails = mrpcontractdetails;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getRecipientname() {
        return recipientname;
    }

    public void setRecipientname(String recipientname) {
        this.recipientname = recipientname;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}