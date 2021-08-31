/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

/**
 *
 * @author krawler
 */
public class IntegrationParty {

    /**
     * Unique integer Id assigned by us to each third party Referred to as
     * 'integrationPartyId'
     */
    private int ID;

    /**
     * Name of integration party
     */
    private String integrationPartyName;

    /**
     * Unique identifier of integration for use in code only Key against which
     * value of activation/deactivation flag is kept in company preferences
     */
    private String integrationPartyHiddenName;

    /**
     * JSON containing configuration of integration fields Used to create
     * integration related fields in company preferences tab
     */
    private String integrationConfig;

    /**
     * Some integration provide some details which are required in integration
     * but are supposed to be same for all companies This JSON contains details
     * of third party which are not company specific
     */
    private String integrationGlobalSettings;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getIntegrationPartyName() {
        return integrationPartyName;
    }

    public void setIntegrationPartyName(String integrationPartyName) {
        this.integrationPartyName = integrationPartyName;
    }

    public String getIntegrationPartyHiddenName() {
        return integrationPartyHiddenName;
    }

    public void setIntegrationPartyHiddenName(String integrationPartyHiddenName) {
        this.integrationPartyHiddenName = integrationPartyHiddenName;
    }

    public String getIntegrationConfig() {
        return integrationConfig;
    }

    public void setIntegrationConfig(String integrationConfig) {
        this.integrationConfig = integrationConfig;
    }

    public JSONObject getIntegrationConfigJson() throws JSONException {
        JSONObject integrationConfigJobj = null;
        if (!StringUtil.isNullOrEmpty(this.integrationConfig)) {
            integrationConfigJobj = new JSONObject(this.integrationConfig);
        }
        return integrationConfigJobj;
    }

    public void setIntegrationConfigJson(JSONObject integrationConfigJson) {
        this.integrationConfig = integrationConfigJson != null ? integrationConfigJson.toString() : null;
    }

    public String getIntegrationGlobalSettings() {
        return integrationGlobalSettings;
    }

    public JSONObject getIntegrationGlobalSettingsJson() throws JSONException {
        JSONObject integrationConfigJobj = null;
        if (!StringUtil.isNullOrEmpty(this.integrationGlobalSettings)) {
            integrationConfigJobj = new JSONObject(this.integrationGlobalSettings);
        }
        return integrationConfigJobj;
    }

    public void setIntegrationGlobalSettings(String integrationGlobalSettings) {
        this.integrationGlobalSettings = integrationGlobalSettings;
    }

}
