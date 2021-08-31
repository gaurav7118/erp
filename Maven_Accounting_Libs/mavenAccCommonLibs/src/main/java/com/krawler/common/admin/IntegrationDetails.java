/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 * Table to store company-specific credentials and configuration for a third
 * party service integration
 *
 * @author krawler
 */
public class IntegrationDetails {

    //UUID - primary key
    private String ID;

    //companyID for subdomain on which credentials are to be used
    private Company company;

    /**
     * userName column stores following values:
     * UPS Integration -> UPS Account Username
     * Avalara Integration -> Company Code of Avalara Account
     */
    private String userName;

    /**
     * passKey column stores following values:
     * UPS Integration -> UPS Account Password
     * Avalara Integration -> NULL/Blank (preferably Null)
     */
    private String passKey;

    /**
     * licenseKey column stores following values:
     * UPS Integration -> Access Key provided by UPS for REST service authentication
     * Avalara Integration -> License Key provided by Avalara for authentication
     */
    private String licenseKey;

    /**
     * accountNumber column stores following values:
     * UPS Integration -> Shipping Account Number for UPS Account
     * Avalara Integration -> Account Number of Avalara Account
     */
    private String accountNumber;

    /**
     * restServiceUrl column stores following values:
     * UPS Integration -> NULL/Blank (preferably Null) because URL is declared in a variable in Integration class
     * Avalara Integration -> URL for REST API methods
     */
    private String restServiceUrl;

    /**
     * configJson column stores configuration details for integration such as enabled/disabled points of integration
     * UPS Integration -> NULL/Blank (preferably Null)
     * Avalara Integration -> Account Number of Avalara Account
     */
    private String configJson;

    /**
     * integrationPartyId column stores integer ID assigned to each integration party by Deskera for reference. These IDs have been defined in Constants.java
     * UPS Integration -> 1
     * Avalara Integration -> 2
     * DBS -> 5
     * IRAS -> 7
     */
    private IntegrationParty integrationParty;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassKey() {
        return passKey;
    }

    public void setPassKey(String passKey) {
        this.passKey = passKey;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getRestServiceUrl() {
        return restServiceUrl;
    }

    public void setRestServiceUrl(String restServiceUrl) {
        this.restServiceUrl = restServiceUrl;
    }

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public IntegrationParty getIntegrationParty() {
        return integrationParty;
    }

    public void setIntegrationParty(IntegrationParty integrationParty) {
        this.integrationParty = integrationParty;
    }
}
