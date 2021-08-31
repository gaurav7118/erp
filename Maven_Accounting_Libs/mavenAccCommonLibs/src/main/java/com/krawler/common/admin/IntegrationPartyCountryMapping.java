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
public class IntegrationPartyCountryMapping {

    /**
     * UUID
     */
    private String ID;
    
    /**
     * Integration party
     */
    private IntegrationParty integrationParty;
    
    /**
     * Country
     */
    private Country country;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public IntegrationParty getIntegrationParty() {
        return integrationParty;
    }

    public void setIntegrationParty(IntegrationParty integrationParty) {
        this.integrationParty = integrationParty;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

}
