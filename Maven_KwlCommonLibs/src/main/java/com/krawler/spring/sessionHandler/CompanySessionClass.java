/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.sessionHandler;

import java.io.Serializable;
import java.util.TreeMap;

/**
 *
 * @author sagar
 */
public class CompanySessionClass implements Serializable {

    private String username;
    private String userid;
    private String userEmailid;
    private String companyid;
    private String company;
    private String timezoneid;
    private String tzdiff;
    private String companyTZDiff;
    private String dateformatid;
    private String currencyid;
    private String callwith;
    private String timeformat;
    private String companyPreferences;
    private String roleid;
    private String initialized;
    private String userfullname;
    private String paymentmethodid;
    private String cdomain;
    private String userdateformat;
    private String browsertz;
    private String userSessionId;
    private String countryId;
//    IndexedCollection<ExchangeRateDetailInfo> exchangeRateDetails;
    private TreeMap<String,Long> permissions= new TreeMap<String,Long>();

    public TreeMap<String, Long> getPermissions() {
        return permissions;
    }

    public String getUserSessionId() {
        return userSessionId;
    }

    public void setUserSessionId(String userSessionId) {
        this.userSessionId = userSessionId;
    }

    public void setPermissions(TreeMap<String, Long> permissions) {
        this.permissions = permissions;
    }

//    private JSONArray perms;

    public String getPaymentmethodid() {
        return paymentmethodid;
    }

    public void setPaymentmethodid(String paymentmethodid) {
        this.paymentmethodid = paymentmethodid;
    }

    public String getCallwith() {
        return callwith;
    }

    public void setCallwith(String callwith) {
        this.callwith = callwith;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompanyPreferences() {
        return companyPreferences;
    }

    public void setCompanyPreferences(String companyPreferences) {
        this.companyPreferences = companyPreferences;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    public String getCurrencyid() {
        return currencyid;
    }

    public void setCurrencyid(String currencyid) {
        this.currencyid = currencyid;
    }

    public String getDateformatid() {
        return dateformatid;
    }

    public void setDateformatid(String dateformatid) {
        this.dateformatid = dateformatid;
    }

    public String getInitialized() {
        return initialized;
    }

    public void setInitialized(String initialized) {
        this.initialized = initialized;
    }

//    public JSONArray getPerms() {
//        return perms;
//    }
//
//    public void setPerms(JSONArray perms) {
//        this.perms = perms;
//    }
    public String getRoleid() {
        return roleid;
    }

    public void setRoleid(String roleid) {
        this.roleid = roleid;
    }

    public String getTimeformat() {
        return timeformat;
    }

    public void setTimeformat(String timeformat) {
        this.timeformat = timeformat;
    }

    public String getTimezoneid() {
        return timezoneid;
    }

    public void setTimezoneid(String timezoneid) {
        this.timezoneid = timezoneid;
    }

    public String getTzdiff() {
        return tzdiff;
    }

    public void setTzdiff(String tzdiff) {
        this.tzdiff = tzdiff;
    }

    public String getUserfullname() {
        return userfullname;
    }

    public void setUserfullname(String userfullname) {
        this.userfullname = userfullname;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCdomain() {
        return cdomain;
    }

    public void setCdomain(String cdomain) {
        this.cdomain = cdomain;
    }

    public String getUserdateformat() {
        return userdateformat;
    }

    public void setUserdateformat(String userdateformat) {
        this.userdateformat = userdateformat;
    }

//    public IndexedCollection<ExchangeRateDetailInfo> getExchangeRateDetails() {
//        return exchangeRateDetails;
//    }
//
//    public void setExchangeRateDetails(IndexedCollection<ExchangeRateDetailInfo> exchangeRateDetails) {
//        this.exchangeRateDetails = exchangeRateDetails;
//    }

    public String getUserEmailid() {
        return userEmailid;
    }

    public void setUserEmailid(String userEmailid) {
        this.userEmailid = userEmailid;
    }
    
    public String getCompanyTZDiff() {
        return companyTZDiff;
    }

    public void setCompanyTZDiff(String companyTZDiff) {
        this.companyTZDiff = companyTZDiff;
    }

    public String getBrowsertz() {
        return browsertz;
    }

    public void setBrowsertz(String browsertz) {
        this.browsertz = browsertz;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }
}
