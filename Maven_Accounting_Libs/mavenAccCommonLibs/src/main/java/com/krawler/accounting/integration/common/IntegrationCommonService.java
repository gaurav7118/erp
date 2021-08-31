/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.common;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;

/**
 *
 * @author krawler
 */
public interface IntegrationCommonService {

    public JSONObject processIntegrationRequest(JSONObject requestJobj) throws ServiceException, JSONException, AccountingException;
    
    /**
     * Following method saves/updates the Integration Account's Details and
     * other Integration settings for a company into the database
     *
     * @params: integrationPartyId -> identifier for Integration Party
     * @params: companyid -> Deskera companyID
     * @params: Integration Account details in a JSONObject which resides in
     * requestJobj under the key 'detail'
     * @params: Integration Account Settings in a JSONObject which resides in
     * requestJobj under the key 'configJson'
     * @param requestJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject saveOrUpdateIntegrationAccountDetails(JSONObject requestJobj) throws ServiceException, JSONException;

    /**
     * Following method fetched the Integration Account Details from database,
     * and returns the details in a JSONObject
     *
     * @params: integrationPartyId -> identifier for Integration Party
     * @params: companyid -> Deskera companyID
     * @param requestJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject getIntegrationAccountDetails(JSONObject requestJobj) throws ServiceException, JSONException;

    /**
     * fetch configJson from table IntegrationDetails
     *
     * @param paramsJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject getIntegrationConfig(JSONObject paramsJobj) throws ServiceException, JSONException;
    
    /**
     * Method to check if Tax committing is enabled for a company
     * @param paramsJobj
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public boolean isTaxCommittingEnabled(JSONObject paramsJobj) throws ServiceException, JSONException;
    
    /**
     * Method to check if Tax calculation is enabled for a company
     * @param paramsJobj
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public boolean isTaxCalculationEnabled(JSONObject paramsJobj) throws ServiceException, JSONException;
    
    /**
     * Method to create and add tax details in requestJson for saving a
     * transaction The method is used to add tax details when save call comes
     * from other than UI; for example import, REST service etc
     *
     * @param paramJobj
     * @param moduleid
     * @return
     * @throws JSONException
     * @throws ServiceException
     * @throws AccountingException
     * @throws SessionExpiredException
     * @throws ParseException
     */
    public JSONObject createAvalaraTaxDetails(JSONObject paramJobj, int moduleid) throws JSONException, ServiceException, AccountingException, SessionExpiredException, ParseException;
    
    /**
     * Method to get tax details from avalara service and to add in line details
     * Json
     *
     * @param paramsJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws AccountingException
     * @throws SessionExpiredException
     * @throws ParseException
     */
    public JSONObject createAvalaraTaxDetailsForTransactionEntry(JSONObject paramsJobj) throws ServiceException, JSONException, AccountingException, SessionExpiredException, ParseException;
    
    /**
     * Method to get value of AvaTax Exemption Code dimension from customfield
     * Json 
     * This method must be used only when AvaTax Exemption Code dimension is
     * present in customfield Jsons
     *
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public String getExemptionCodeFromCustomFieldsJson(JSONObject paramJobj) throws ServiceException, JSONException;
    
    /**
     * Method to get value of AvaTax Exemption Code dimension from customfield
     * Json 
     * If AvaTax Exemption Code dimension's details are not presnet in
     * customfield Json, then the method fetches value from Customer master and
     * puts in customfield Json
     *
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject getAndPutExemptionCode(JSONObject paramJobj) throws ServiceException, JSONException;
    
    /**
     * Below method fetches tax details of a transaction row saved in the
     * database using the ID of transaction row
     *
     * @param paramsJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONArray getTransactionDetailTaxMapping(JSONObject paramsJobj) throws ServiceException, JSONException;
    
    /**
     * Below method saves tax details of a transaction row into the database
     * against the ID of transaction row
     *
     * @param paramsJobj
     * @throws ServiceException
     */
    public void saveTransactionDetailTaxMapping(JSONObject paramsJobj) throws ServiceException;
    
    /**
     * Below method deletes tax details of a transaction row from the database
     * using the ID of transaction row
     *
     * @param paramsJobj
     * @throws ServiceException
     */
    public void deleteTransactionDetailTaxMapping(JSONObject paramsJobj) throws ServiceException;
    
    /**
     * Below method commits the tax details for a transaction on AvaTax side and
     * saves the tax details into our database
     *
     * @param paramsJobj
     * @return
     * @throws JSONException
     * @throws AccountingException
     */
     public JSONObject commitAndSaveTax(JSONObject paramsJobj) throws AccountingException, JSONException;
    
    /**
     * This method fetches the value of exemption code from a transaction using moduleid and document's ID
     * This method is used to get exemption code in case of tax override (link case)
     * In override case, linked module's exemption code is used for tax calculation
     *
     * @param requestJobj
     * @param companyid
     * @param customerid
     * @param exemptionCodeColNum
     * @param refModuleId
     * @param refDocId
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public String getExemptionCodeFromRefModule(String companyid, String exemptionCodeColNum, int refModuleId, String refDocId) throws JSONException, ServiceException;
    
    public JSONObject getAddressesForUps(JSONObject requestJobj) throws ServiceException, JSONException, SessionExpiredException;
}
