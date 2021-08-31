/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.common;

import com.krawler.common.admin.BillingShippingAddresses;
import com.krawler.common.admin.CompanyAddressDetails;
import com.krawler.common.admin.CustomerAddressDetails;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.admin.IntegrationDetails;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.TransactionDetailAvalaraTaxMapping;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class IntegrationCommonServiceImpl extends IntegrationUtil implements IntegrationCommonService, MessageSourceAware {

    private IntegrationDAO integrationDAO;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAO;
    private AccCommonTablesDAO accCommonTablesDAO;

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setIntegrationDAO(IntegrationDAO integrationDAO) {
        this.integrationDAO = integrationDAO;
    }

    @Override
    public JSONObject processIntegrationRequest(JSONObject requestJobj) throws ServiceException, JSONException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        int integrationPartyId = requestJobj.optInt(IntegrationConstants.integrationPartyIdKey, 0);
        if (integrationPartyId != 0) {
            JSONObject integrationAccountDetails = getIntegrationAccountDetails(requestJobj);
            if (!requestJobj.optBoolean(IntegrationConstants.skipRequestJsonProcessing)) {
                requestJobj = processRequestJson(requestJobj);
            }
            returnJobj = IntegrationConstants.integrationServiceMap.get(integrationPartyId).processRequest(requestJobj, integrationAccountDetails);
        } else {
            throw new AccountingException("Integration party ID could not be found or is not valid.");
        }
        return returnJobj;
    }

    private JSONObject processRequestJson(JSONObject requestJobj) throws JSONException, ServiceException, AccountingException {
        int integrationPartyId = requestJobj.optInt(IntegrationConstants.integrationPartyIdKey, 0);
        switch (integrationPartyId) {
            case IntegrationConstants.integrationPartyId_AVALARA:
                requestJobj = processRequestJsonForAvalara(requestJobj);
                break;
        }
        return requestJobj;
    }

    private JSONObject processRequestJsonForAvalara(JSONObject requestJobj) throws JSONException, ServiceException, AccountingException {
        String companyid = requestJobj.getString(Constants.companyKey);
        String moduleid = requestJobj.optString(Constants.moduleid);
        String customerid = getCustomerId(requestJobj);

        /**
         * Manipulate global fields
         */
        requestJobj.put("currencyCode", getCurrencyCode(requestJobj));
        requestJobj.put(Constants.customerid, customerid);
        requestJobj.put("customerCode", getCustomerCode(requestJobj, companyid));
        requestJobj.put("salesPersonCode", getSalesPersonCode(requestJobj, companyid));
        requestJobj.put("addressesJson", getAddresses(requestJobj, companyid));
        if (!StringUtil.isNullOrEmpty(requestJobj.optString(IntegrationConstants.taxOverrideDocId))) {
            requestJobj.put(IntegrationConstants.taxOverrideDate, getTaxOverrideDateForAvalara(requestJobj, moduleid));
        }

        /**
         * Manipulate line level fields
         */
        String productRecord = requestJobj.optString(Constants.detail);
        if (!StringUtil.isNullOrEmpty(productRecord)) {
            JSONArray productRecordJarr = new JSONArray(productRecord);
            JSONArray productRecordJarrNew = new JSONArray();
            for (int i = 0; i < productRecordJarr.length(); i++) {
                JSONObject productRecordJobj = productRecordJarr.optJSONObject(i);
                productRecordJobj.put("pid", getProductCode(productRecordJobj, companyid));
                productRecordJobj.put(IntegrationConstants.avalaraProductTaxCode, getProductTaxCode(productRecordJobj, companyid));
                if (!StringUtil.isNullOrEmpty(productRecordJobj.optString(IntegrationConstants.taxOverrideDocId))) {
                    productRecordJobj.put("addressesJson", getAddresses(productRecordJobj, companyid));
                    productRecordJobj.put(IntegrationConstants.taxOverrideDate, getTaxOverrideDateForAvalara(productRecordJobj, moduleid));
                    productRecordJobj.put(IntegrationConstants.avalaraExemptionCode, getTaxOverrideExemptionCode(productRecordJobj, companyid, customerid));
                }
                productRecordJarrNew.put(i, productRecordJobj);
            }
            requestJobj.put(Constants.detail, productRecordJarrNew.toString());
        }
        return requestJobj;
    }

    /**
     * Following method saves/updates the Integration Account's Details and
     * other Integration settings for a company into the database
     *
     * @params: integrationPartyId -> identifier for Integration Party
     * @params: companyid -> Deskera companyID
     * @params: Integration Account details in a JSONObject which resides in
     * requestJobj under the key 'credentialsData'
     * @params: Integration Account Settings in a JSONObject which resides in
     * requestJobj under the key 'configJson'
     * @param requestJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public JSONObject saveOrUpdateIntegrationAccountDetails(JSONObject requestJobj) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        if (!StringUtil.isNullOrEmpty(requestJobj.optString(IntegrationConstants.credentialsData)) || !StringUtil.isNullOrEmpty(requestJobj.optString(IntegrationConstants.configJson))) {
            JSONObject paramJobj = new JSONObject();
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.companyKey))) {
                paramJobj.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
            }
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(IntegrationConstants.integrationPartyIdKey))) {
                paramJobj.put(IntegrationConstants.integrationPartyIdKey, requestJobj.optInt(IntegrationConstants.integrationPartyIdKey));
            }
            KwlReturnObject resultKwlObj = integrationDAO.getIntegrationAccountDetails(paramJobj);
            List<IntegrationDetails> integrationDetailsList = resultKwlObj.getEntityList();
            if (integrationDetailsList != null && !integrationDetailsList.isEmpty()) {
                IntegrationDetails integrationDetails = integrationDetailsList.get(0);
                if (integrationDetails != null && !StringUtil.isNullOrEmpty(integrationDetails.getID())) {
                    paramJobj.put(Constants.ID, integrationDetails.getID());
                }
            }
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(IntegrationConstants.credentialsData))) {
                JSONObject detailsJobj = new JSONObject(requestJobj.optString(IntegrationConstants.credentialsData));
                if (!StringUtil.isNullOrEmpty(detailsJobj.optString(IntegrationConstants.userName))) {
                    paramJobj.put(IntegrationConstants.userName, detailsJobj.optString(IntegrationConstants.userName));
                }
                if (!StringUtil.isNullOrEmpty(detailsJobj.optString(IntegrationConstants.passKey))) {
                    paramJobj.put(IntegrationConstants.passKey, detailsJobj.optString(IntegrationConstants.passKey));
                }
                if (!StringUtil.isNullOrEmpty(detailsJobj.optString(IntegrationConstants.accountNumber))) {
                    paramJobj.put(IntegrationConstants.accountNumber, detailsJobj.optString(IntegrationConstants.accountNumber));
                }
                if (!StringUtil.isNullOrEmpty(detailsJobj.optString(IntegrationConstants.licenseKey))) {
                    paramJobj.put(IntegrationConstants.licenseKey, detailsJobj.optString(IntegrationConstants.licenseKey));
                }
                if (!StringUtil.isNullOrEmpty(detailsJobj.optString(IntegrationConstants.restServiceUrl))) {
                    paramJobj.put(IntegrationConstants.restServiceUrl, detailsJobj.optString(IntegrationConstants.restServiceUrl));
                }
            }
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(IntegrationConstants.configJson))) {
                paramJobj.put(IntegrationConstants.configJson, requestJobj.optString(IntegrationConstants.configJson));
            }
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.ID))) {
                paramJobj.put(Constants.ID, requestJobj.optString(Constants.ID));
            }
            integrationDAO.saveOrUpdateIntegrationAccountDetails(paramJobj);
            jobj.put(Constants.RES_success, true);
            jobj.put(Constants.RES_msg, messageSource.getMessage("acc.integration.settingsSaveSuccess", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        } else {
            jobj.put(Constants.RES_success, false);
            jobj.put(Constants.RES_msg, messageSource.getMessage("acc.integration.settingsSaveFailure", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
        return jobj;
    }

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
    @Override
    public JSONObject getIntegrationAccountDetails(JSONObject requestJobj) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        JSONObject paramJobj = new JSONObject();
        int integrationPartyId = requestJobj.optInt(IntegrationConstants.integrationPartyIdKey);
        paramJobj.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
        paramJobj.put(IntegrationConstants.integrationPartyIdKey, integrationPartyId);
        KwlReturnObject resultKwlObj = integrationDAO.getIntegrationAccountDetails(paramJobj);
        List<IntegrationDetails> integrationDetailsList = resultKwlObj.getEntityList();
        if (integrationDetailsList != null && !integrationDetailsList.isEmpty()) {
            IntegrationDetails integrationDetails = integrationDetailsList.get(0);
            if (integrationDetails != null && !StringUtil.isNullOrEmpty(integrationDetails.getID())) {
                returnJobj.put(Constants.ID, integrationDetails.getID());
                returnJobj.put(IntegrationConstants.integrationPartyIdKey, integrationDetails.getIntegrationParty().getID());
                returnJobj.put(IntegrationConstants.integrationGlobalSettingsJson, integrationDetails.getIntegrationParty().getIntegrationGlobalSettingsJson());
                returnJobj.put(IntegrationConstants.userName, integrationDetails.getUserName());
                returnJobj.put(IntegrationConstants.passKey, integrationDetails.getPassKey());
                returnJobj.put(IntegrationConstants.licenseKey, integrationDetails.getLicenseKey());
                returnJobj.put(IntegrationConstants.accountNumber, integrationDetails.getAccountNumber());
                returnJobj.put(IntegrationConstants.restServiceUrl, integrationDetails.getRestServiceUrl());
                returnJobj.put(IntegrationConstants.configJson, !StringUtil.isNullOrEmpty(integrationDetails.getConfigJson()) ? new JSONObject(integrationDetails.getConfigJson()) : new JSONObject());
            }
        }
        return returnJobj;
    }

    /**
     * fetch configJson from table IntegrationDetails
     *
     * @param paramsJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public JSONObject getIntegrationConfig(JSONObject paramsJobj) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        JSONObject integrationAccountDetails = getIntegrationAccountDetails(paramsJobj);
        if (integrationAccountDetails.has(IntegrationConstants.configJson)) {
            returnJobj = integrationAccountDetails.optJSONObject(IntegrationConstants.configJson);
        }
        return returnJobj;
    }

    /**
     * Method to check if Tax committing is enabled for a company
     *
     * @param paramsJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public boolean isTaxCommittingEnabled(JSONObject paramsJobj) throws ServiceException, JSONException {
        boolean isTaxCommit = false;
        JSONObject integrationAccountDetails = getIntegrationAccountDetails(paramsJobj);
        if (integrationAccountDetails.has(IntegrationConstants.configJson)) {
            JSONObject configJson = integrationAccountDetails.optJSONObject(IntegrationConstants.configJson);
            if (StringUtil.equalIgnoreCase(configJson.optString(IntegrationConstants.taxCommitting), "on")) {
                isTaxCommit = true;
            }
        }
        return isTaxCommit;
    }

    /**
     * Method to check if Tax calculation is enabled for a company
     *
     * @param paramsJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public boolean isTaxCalculationEnabled(JSONObject paramsJobj) throws ServiceException, JSONException {
        boolean isTaxCalculation = false;
        JSONObject integrationAccountDetails = getIntegrationAccountDetails(paramsJobj);
        if (integrationAccountDetails.has(IntegrationConstants.configJson)) {
            JSONObject configJson = integrationAccountDetails.optJSONObject(IntegrationConstants.configJson);
            if (StringUtil.equalIgnoreCase(configJson.optString(IntegrationConstants.taxCalculation), "on")) {
                isTaxCalculation = true;
            }
        }
        return isTaxCalculation;
    }

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
     */
    @Override
    public JSONObject createAvalaraTaxDetails(JSONObject paramJobj, int moduleid) throws JSONException, ServiceException, AccountingException {
        JSONArray linesJarr = !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.detail)) ? new JSONArray(paramJobj.optString(Constants.detail)) : new JSONArray();
        boolean isTaxCalculationRequired = true;
        /**
         * Loop to check if line term details are already present in paramJobj
         * If details are found then tax calculation is not required and
         * therefore calculation is not done
         */
        for (int i = 0; i < linesJarr.length(); i++) {
            JSONObject lineJobj = linesJarr.getJSONObject(i);
            String lineTermDetailsStr = lineJobj.optString("LineTermdetails", null);
            if (!StringUtil.isNullOrEmpty(lineTermDetailsStr)) {
                JSONArray lineTermDetailsJarr = new JSONArray(StringUtil.DecodeText(lineTermDetailsStr));
                if (lineTermDetailsJarr.length() != 0) {
                    isTaxCalculationRequired = false;
                }
            }
        }
        paramJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
        paramJobj.put(Constants.moduleid, moduleid);
        /**
         * Get Avatax Exemption Code value which is used during tax calculation
         * with Avalara service
         */
        paramJobj = getAndPutExemptionCode(paramJobj);
        if (isTaxCalculationRequired) {
            /**
             * method to get and put tax details fetched from Avalara service
             */
            paramJobj = createAvalaraTaxDetailsForTransactionEntry(paramJobj);
        }
        return paramJobj;
    }

    /**
     * Method to get tax details from avalara service and to add in line details
     * Json
     *
     * @param paramsJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws AccountingException
     */
    @Override
    public JSONObject createAvalaraTaxDetailsForTransactionEntry(JSONObject paramsJobj) throws ServiceException, JSONException, AccountingException {
        paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
        paramsJobj.put(IntegrationConstants.integrationOperationIdKey, IntegrationConstants.avalara_createOrAdjustTransaction);
        JSONObject taxDetailsJobj = processIntegrationRequest(paramsJobj);
        JSONArray taxDetailsJarr = taxDetailsJobj.optJSONArray("prodTermArray");
        String productRecord = paramsJobj.optString(Constants.detail);
        JSONArray productRecordJarr = new JSONArray(productRecord);
        JSONArray productRecordJarrNew = new JSONArray();
        double totalTaxAmount = 0;
        /**
         * ERM-294
         * added check for taxDetailsJarr for null and non zero
         */
        if (taxDetailsJarr != null && taxDetailsJarr.length() != 0) {
            for (int i = 0; i < taxDetailsJarr.length(); i++) {
                JSONObject taxJobj = taxDetailsJarr.optJSONObject(i);
                int lineNumber = taxJobj.getInt("lineNumber");
                JSONObject productRecordJobj = productRecordJarr.optJSONObject(lineNumber - 1);
                productRecordJobj.put("LineTermdetails", taxJobj.optJSONArray("LineTermdetails").toString());
                double lineTermAmount = taxJobj.optDouble("LineTermAmount", 0);
                productRecordJobj.put("recTermAmount", lineTermAmount);
                totalTaxAmount += lineTermAmount;
                productRecordJarrNew.put(productRecordJobj);
            }
            paramsJobj.put(Constants.detail, productRecordJarrNew.toString());
        }
        paramsJobj.put("taxamount", totalTaxAmount);
        return paramsJobj;
    }

    /**
     * Method to get value of AvaTax Exemption Code dimension from customfield
     * Json This method must be used only when AvaTax Exemption Code dimension
     * is present in customfield Jsons
     *
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public String getExemptionCodeFromCustomFieldsJson(JSONObject paramJobj) throws ServiceException, JSONException {
        String exemptionCode = null;
        String customfield = paramJobj.optString(Constants.customfield, null);
        if (!StringUtil.isNullOrEmpty(customfield)) {
            JSONArray customFieldsJarr = new JSONArray(customfield);
            JSONObject customFieldJobj = new JSONObject();
            for (int i = 0; i < customFieldsJarr.length(); i++) {
                customFieldJobj = customFieldsJarr.optJSONObject(i);
                if (customFieldJobj != null && StringUtil.equal(customFieldJobj.optString(Constants.Acc_custom_field), Constants.Custom_Record_Prefix + IntegrationConstants.avataxExemptionCode)) {
                    exemptionCode = Constants.NONE;
                    String customFieldColNumKey = customFieldJobj.optString(Constants.Custom_Record_Prefix + IntegrationConstants.avataxExemptionCode);
                    String exemptionCodeId = customFieldJobj.optString(customFieldColNumKey, null);
                    if (!StringUtil.isNullOrEmpty(exemptionCodeId)) {
                        FieldComboData fieldComboData = getFieldComboDataById(exemptionCodeId);
                        if (fieldComboData != null) {
                            exemptionCode = fieldComboData.getValue();
                        }
                    }
                    break;
                }
            }
        }
        return exemptionCode;
    }

    /**
     * Method to get value of AvaTax Exemption Code dimension from customfield
     * Json If AvaTax Exemption Code dimension's details are not present in
     * customfield Json, then the method fetches value from Customer master and
     * puts in customfield Json
     *
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public JSONObject getAndPutExemptionCode(JSONObject paramJobj) throws ServiceException, JSONException {
        String companyid = paramJobj.optString(Constants.companyKey);
        String customerid = getCustomerId(paramJobj);
        int moduleid = paramJobj.optInt(Constants.moduleid);
        String exemptionCode = null;

        String exemptionCodeId = null;
        FieldParams fieldParam = null;
        FieldComboData fieldComboData = null;

        if (paramJobj.has(IntegrationConstants.avalaraExemptionCode)) {
            exemptionCode = paramJobj.getString(IntegrationConstants.avalaraExemptionCode);
        } else {
            /**
             * Call to get exemption code from customfield details json Another
             * purpose of below call is to check whether exemption code details are
             * present in customfield json or not If exemption code details are not
             * present then below method returns null
             */
            exemptionCode = getExemptionCodeFromCustomFieldsJson(paramJobj);
        }

        /**
         * If exemption Code is not found in customfield details then, fetch
         * value from custom master and put in customfield json
         */
        if (exemptionCode == null) {
            fieldParam = getFieldParam(companyid, Constants.Acc_Customer_ModuleId, IntegrationConstants.avataxExemptionCode);
            if (fieldParam != null) {
                exemptionCodeId = (String) populateMasterInformation(getCustomDataTableNameByModuleId(Constants.Acc_Customer_ModuleId), Constants.Custom_column_Prefix + fieldParam.getColnum(), "company.companyID", companyid, getCustomDataTableRefColumnNameByModuleId(Constants.Acc_Customer_ModuleId), customerid);
                if (exemptionCodeId != null) {
                    fieldComboData = getFieldComboDataById(exemptionCodeId);
                    if (fieldComboData != null) {
                        exemptionCode = fieldComboData.getValue();
                        if (exemptionCode != null) {
                            fieldParam = getFieldParam(companyid, moduleid, IntegrationConstants.avataxExemptionCode);
                            if (fieldParam != null) {
                                exemptionCodeId = (String) populateMasterInformation(Constants.Acc_FieldComboData, Constants.Acc_id, Constants.Acc_fieldid, fieldParam.getId(), Constants.VALUE, exemptionCode);
                                if (exemptionCodeId != null) {
                                    JSONArray customFieldsJarr = new JSONArray();
                                    String customfield = paramJobj.optString(Constants.customfield, null);
                                    if (!StringUtil.isNullOrEmpty(customfield)) {
                                        customFieldsJarr = new JSONArray(customfield);
                                    }
                                    JSONObject customFieldJobj = new JSONObject();
                                    customFieldJobj.put(Constants.Acc_custom_fieldId, fieldParam.getId());
                                    customFieldJobj.put("filedid", fieldParam.getId());
                                    customFieldJobj.put("refcolumn_name", Constants.Custom_Column_Prefix + fieldParam.getRefcolnum());
                                    customFieldJobj.put(Constants.Acc_custom_field, Constants.Custom_Record_Prefix + IntegrationConstants.avataxExemptionCode);
                                    customFieldJobj.put(Constants.xtype, fieldParam.getFieldtype());
                                    customFieldJobj.put(Constants.Custom_Column_Prefix + fieldParam.getColnum(), exemptionCodeId);
                                    customFieldJobj.put("fieldDataVal", exemptionCodeId);
                                    customFieldJobj.put(Constants.Custom_Record_Prefix + IntegrationConstants.avataxExemptionCode, Constants.Custom_Column_Prefix + fieldParam.getColnum());
                                    customFieldsJarr.put(customFieldJobj);
                                    paramJobj.put(Constants.customfield, customFieldsJarr.toString());
                                }
                            }
                        }
                    }
                }
            }
        }

        paramJobj.put(IntegrationConstants.avalaraExemptionCode, exemptionCode);
        return paramJobj;
    }

    /**
     * Below method fetches tax details of a transaction row saved in the
     * database using the ID of transaction row used in Avalara integration
     *
     * @param paramsJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public JSONArray getTransactionDetailTaxMapping(JSONObject paramsJobj) throws ServiceException, JSONException {
        JSONArray returnJarr = new JSONArray();
        List<TransactionDetailAvalaraTaxMapping> list = integrationDAO.getTransactionDetailTaxMapping(paramsJobj);
        if (list != null && !list.isEmpty()) {
            TransactionDetailAvalaraTaxMapping transactionDetailAvalaraTaxMapping = (TransactionDetailAvalaraTaxMapping) list.get(0);
            if (transactionDetailAvalaraTaxMapping != null) {
                String taxDetails = transactionDetailAvalaraTaxMapping.getAvalaraTaxDetails();
                if (!StringUtil.isNullOrEmpty(taxDetails)) {
                    returnJarr = new JSONArray(taxDetails);
                }
            }
        }
        return returnJarr;
    }

    /**
     * Below method saves tax details of a transaction row into the database
     * against the ID of transaction row used in Avalara integration
     *
     * @param paramsJobj
     * @throws ServiceException
     */
    @Override
    public void saveTransactionDetailTaxMapping(JSONObject paramsJobj) throws ServiceException {
        integrationDAO.saveTransactionDetailTaxMapping(paramsJobj);
    }

    /**
     * Below method deletes tax details of a transaction row from the database
     * using the ID of transaction row used in Avalara integration
     *
     * @param paramsJobj
     * @throws ServiceException
     */
    @Override
    public void deleteTransactionDetailTaxMapping(JSONObject paramsJobj) throws ServiceException {
        integrationDAO.deleteTransactionDetailTaxMapping(paramsJobj);
    }

    /**
     * Below method commits the tax details for a transaction on AvaTax side and
     * saves the tax details into our database
     *
     * @param paramsJobj
     * @return
     * @throws JSONException
     * @throws AccountingException
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {AccountingException.class, JSONException.class})
    public JSONObject commitAndSaveTax(JSONObject paramsJobj) throws AccountingException, JSONException {
        JSONObject returnJobj = new JSONObject();
        boolean success = false;
        String msg = null;
        boolean isCommit = paramsJobj.optBoolean(IntegrationConstants.commit, false);
        String productRecord = paramsJobj.optString(Constants.detail);
        try {
            if (!StringUtil.isNullOrEmpty(productRecord)) {
                paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                paramsJobj.put(IntegrationConstants.integrationOperationIdKey, IntegrationConstants.avalara_createOrAdjustTransaction);
                JSONObject taxDetailsJobj = processIntegrationRequest(paramsJobj);
                if (taxDetailsJobj.optBoolean(Constants.RES_success, false)) {
                    JSONObject rowIdTaxAmountMappingJobj = new JSONObject();//JSON to keep rowTaxAmount mapped with row's ID
                    if (isCommit) {
                        msg = messageSource.getMessage("acc.integration.taxSaveFailure", null, Locale.forLanguageTag(paramsJobj.optString(Constants.language)));
                    } else {
                        msg = messageSource.getMessage("acc.integration.taxSaveFailure1", null, Locale.forLanguageTag(paramsJobj.optString(Constants.language)));
                    }
                    returnJobj.put(IntegrationConstants.avalaraDocCode, taxDetailsJobj.optString(IntegrationConstants.avalaraDocCode));
                    JSONArray taxDetailsJarr = taxDetailsJobj.optJSONArray("prodTermArray");
                    JSONArray productRecordJarr = new JSONArray(productRecord);
                    for (int i = 0; i < taxDetailsJarr.length(); i++) {
                        JSONObject taxJobj = taxDetailsJarr.optJSONObject(i);
                        int lineNumber = taxJobj.getInt("lineNumber");
                        JSONObject productRecordJobj = productRecordJarr.optJSONObject(lineNumber - 1);
                        String parentRecordID = productRecordJobj.optString(IntegrationConstants.parentRecordID);
                        JSONObject saveTaxParamsJobj = new JSONObject();
                        saveTaxParamsJobj.put(IntegrationConstants.parentRecordID, parentRecordID);
                        saveTaxParamsJobj.put(IntegrationConstants.avalaraTaxDetails, taxJobj.optJSONArray("LineTermdetails").toString());
                        saveTransactionDetailTaxMapping(saveTaxParamsJobj);
                        rowIdTaxAmountMappingJobj.put(parentRecordID, taxJobj.optDouble("LineTermAmount"));
                    }
                    if (isCommit) {
                        msg = messageSource.getMessage("acc.integration.taxCommitAndSaveSuccess", null, Locale.forLanguageTag(paramsJobj.optString(Constants.language)));
                    } else {
                        msg = messageSource.getMessage("acc.integration.taxNotCommitted", null, Locale.forLanguageTag(paramsJobj.optString(Constants.language)));
                    }
                    success = true;
                    returnJobj.put("rowIdTaxAmountMapping", rowIdTaxAmountMappingJobj);
                } else {
                    if (isCommit) {
                        msg = messageSource.getMessage("acc.integration.taxCommitAndSaveFailure", null, Locale.forLanguageTag(paramsJobj.optString(Constants.language)));
                    } else {
                        msg = messageSource.getMessage("acc.integration.taxSaveFailure1", null, Locale.forLanguageTag(paramsJobj.optString(Constants.language)));
                    }
                }
            }
        } catch (ServiceException | JSONException | AccountingException ex) {
            Logger.getLogger(IntegrationCommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new AccountingException(msg);
        } finally {
            returnJobj.put(Constants.RES_msg, msg);
            returnJobj.put(Constants.RES_success, success);
        }
        return returnJobj;
    }

    /**
     * Get FieldParams Class object with moduleid and fieldlabel
     *
     * @param companyid
     * @param moduleid
     * @param fieldlabel
     * @return
     */
    public FieldParams getFieldParam(String companyid, int moduleid, String fieldlabel) {
        FieldParams fieldParam = null;
        HashMap paramsMap = new HashMap<String, Object>();
        List filter_names = new ArrayList<String>();
        List filter_values = new ArrayList<Object>();
        filter_names.add(Constants.fieldlabel);
        filter_names.add("company.companyID");
        filter_names.add(Constants.moduleid);
        filter_values.add(fieldlabel);
        filter_values.add(companyid);
        filter_values.add(moduleid);
        paramsMap.put("filter_names", filter_names);
        paramsMap.put("filter_values", filter_values);
        KwlReturnObject kwlObj = accCommonTablesDAO.getFieldParams(paramsMap);
        if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty() && kwlObj.getEntityList().get(0) != null) {
            fieldParam = (FieldParams) kwlObj.getEntityList().get(0);
        }
        return fieldParam;
    }

    /**
     * Get FieldComboData class Object with primary key value
     *
     * @param id
     * @return
     * @throws ServiceException
     */
    public FieldComboData getFieldComboDataById(String id) throws ServiceException {
        FieldComboData fieldComboData = null;
        if (id != null) {
            KwlReturnObject kwlObj = accountingHandlerDAO.getObject(FieldComboData.class.getName(), id);
            if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty() && kwlObj.getEntityList().get(0) != null) {
                fieldComboData = (FieldComboData) kwlObj.getEntityList().get(0);
            }
        }
        return fieldComboData;
    }

    /**
     * Method to execute hibernate query on a database table with passed
     * parameters This is used to fetch specific column from a table using some
     * other columns' values
     *
     * @param tableName
     * @param fetchColumn
     * @param conditionCol1Name
     * @param conditionCol1Value
     * @param conditionCol2Name
     * @param conditionCol2Value
     * @return
     * @throws ServiceException
     */
    public Object populateMasterInformation(String tableName, String fetchColumn, String conditionCol1Name, String conditionCol1Value, String conditionCol2Name, String conditionCol2Value) throws ServiceException {
        Object returnObj = null;
        Map paramsMap = new HashMap<String, String>();
        paramsMap.put(Constants.tableName, tableName);
        if (!StringUtil.isNullOrEmpty(fetchColumn)) {
            paramsMap.put(Constants.fetchColumn, fetchColumn);
        }
        paramsMap.put(Constants.companyColumn, conditionCol1Name);
        paramsMap.put(Constants.companyKey, conditionCol1Value);
        if (!StringUtil.isNullOrEmpty(conditionCol2Name) && !StringUtil.isNullObject(conditionCol2Value)) {
            paramsMap.put("condtionColumn", conditionCol2Name);
            paramsMap.put("condtionColumnvalue", conditionCol2Value);
        }
        KwlReturnObject kwlObj = accountingHandlerDAO.populateMasterInformation(paramsMap);
        if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty() && kwlObj.getEntityList().get(0) != null) {
            returnObj = kwlObj.getEntityList().get(0);
        }
        return returnObj;
    }

    /**
     * Method to get currency-code using currency-id If currency-id is not found
     * then default value 'USD' is returned
     *
     * @param requestJobj
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public String getCurrencyCode(JSONObject requestJobj) throws JSONException, ServiceException {
        String currencyCode = requestJobj.optString("currencyCode");
        String currencyid = requestJobj.optString(Constants.currencyKey, null);
        if (StringUtil.isNullOrEmpty(currencyCode) && !StringUtil.isNullOrEmpty(currencyid)) {
            KwlReturnObject kwlObj = accountingHandlerDAO.getObject(KWLCurrency.class.getName(), currencyid);
            if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
                KWLCurrency kwlCurrency = (KWLCurrency) kwlObj.getEntityList().get(0);
                if (kwlCurrency != null) {
                    currencyCode = kwlCurrency.getCurrencyCode();
                }
            }
        }
        if (StringUtil.isNullOrEmpty(currencyCode)) {//use USD by default if currency code is found to be null or empty
            currencyCode = "USD";
        }
        return currencyCode;
    }

    /**
     * Get customerid from Json checks for value against 'customerid' and
     * 'customer' keys
     *
     * @param requestJobj
     * @return
     */
    public String getCustomerId(JSONObject requestJobj) {
        String customerid = requestJobj.optString(Constants.customerid);
        if (StringUtil.isNullOrEmpty(customerid)) {
            customerid = requestJobj.optString("customer");
        }
        return customerid;
    }

    /**
     * Get customer-code from Json Checks for value against 'customerCode' key
     * and if not found then uses customer-id to get customer-code from database
     *
     * @param requestJobj
     * @param companyid
     * @return
     * @throws ServiceException
     */
    public String getCustomerCode(JSONObject requestJobj, String companyid) throws ServiceException {
        String customerCode = requestJobj.optString("customerCode");
        if (StringUtil.isNullOrEmpty(customerCode)) {
            String customerid = getCustomerId(requestJobj);
            if (!StringUtil.isNullOrEmpty(customerid)) {
                customerCode = (String) populateMasterInformation("Customer", "acccode", "company.companyID", companyid, Constants.ID, customerid);
            }
        }
        return customerCode;
    }

    /**
     * Method to get customer's default shipping address using customer-id and
     * company-id
     *
     * @param customerid
     * @param companyid
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public KwlReturnObject getCustomerShippingAddress(String customerid, String companyid) throws JSONException, ServiceException {
        HashMap requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.customerid, customerid);
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put("isDefaultAddress", "true");
        requestParams.put("isBillingAddress", "false");
        KwlReturnObject addressResult = accountingHandlerDAO.getCustomerAddressDetails(requestParams);
        return addressResult;
    }

    /**
     * Method to get company's shipping address using company-id
     *
     * @param companyid
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public KwlReturnObject getCompanyShippingAddress(String companyid) throws JSONException, ServiceException {
        HashMap requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put("isDefaultAddress", "true");
        requestParams.put("isBillingAddress", "false");
        KwlReturnObject kwlObj = accountingHandlerDAO.getCompanyAddressDetails(requestParams);
        return kwlObj;
    }

    /**
     * Get product code from Json Checks value against 'pid' key, and if not
     * found then uses product id to fetch code from database
     *
     * @param productRecordJobj
     * @param companyid
     * @return
     * @throws ServiceException
     */
    public String getProductCode(JSONObject productRecordJobj, String companyid) throws ServiceException {
        String productCode = productRecordJobj.optString("pid");
        if (StringUtil.isNullOrEmpty(productCode)) {
            String productid = productRecordJobj.optString(Constants.productid);
            if (!StringUtil.isNullOrEmpty(productid)) {
                productCode = (String) populateMasterInformation("Product", Constants.productid, "company.companyID", companyid, Constants.ID, productid);
            }
        }
        return productCode;
    }

    /**
     * Method to get product tax code Tax code is value of 'Product Tax Class'
     * dimension in product master
     *
     * @param productRecordJobj
     * @param companyid
     * @return
     * @throws ServiceException
     */
    public String getProductTaxCode(JSONObject productRecordJobj, String companyid) throws ServiceException {
        String productTaxCode = productRecordJobj.optString(IntegrationConstants.avalaraProductTaxCode, null);
        if (StringUtil.isNullOrEmpty(productTaxCode)) {
            String productid = productRecordJobj.optString(Constants.productid);
            FieldParams fieldParams = getFieldParam(companyid, Constants.Acc_Product_Master_ModuleId, Constants.GSTProdCategory);
            if (fieldParams != null && !StringUtil.isNullOrEmpty(productid)) {
                String productTaxCodeID = (String) populateMasterInformation(Constants.AccProductCustomData, Constants.Custom_column_Prefix + fieldParams.getColnum(), "company.companyID", companyid, "productId", productid);
                if (productTaxCodeID != null) {
                    FieldComboData fieldComboData = getFieldComboDataById(productTaxCodeID);
                    if (fieldComboData != null) {
                        productTaxCode = fieldComboData.getValue();
                    }
                }
            }
        }
        return productTaxCode;
    }

    /**
     * Get sales person code from Json Checks value against 'salesPersonCode'
     * key, and if not found then uses sales person id to fetch code from
     * database
     *
     * @param requestJobj
     * @param companyid
     * @return
     * @throws ServiceException
     */
    public String getSalesPersonCode(JSONObject requestJobj, String companyid) throws ServiceException {
        String salesPersonCode = requestJobj.optString("salesPersonCode");
        if (StringUtil.isNullOrEmpty(salesPersonCode)) {
            String salespersonid = requestJobj.optString("salespersonid");
            if (!StringUtil.isNullOrEmpty(salespersonid)) {
                salesPersonCode = (String) populateMasterInformation("MasterItem", IntegrationConstants.code, "company.companyID", companyid, Constants.ID, salespersonid);
            }
        }
        return salesPersonCode;
    }

    /**
     * Method to get CustomData table name for a module
     *
     * @param moduleid
     * @return
     */
    public String getCustomDataTableNameByModuleId(int moduleid) {
        String tableName = "";
        if (moduleid == Constants.Acc_Customer_ModuleId) {
            tableName = Constants.CustomerCustomData;
        } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
            tableName = Constants.AccJECustomData;
        } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
            tableName = Constants.SalesOrderCustomData;
        } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
            tableName = Constants.DeliveryOrderCustomData;
        } else if (moduleid == Constants.Acc_Customer_Quotation_ModuleId) {
            tableName = Constants.QuotationCustomData;
        } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
            tableName = Constants.SalesReturnCustomData;
        }
        return tableName;
    }

    /**
     * Method to get column-name under which document's Id is stored in
     * CustomData table for a module
     *
     * @param moduleid
     * @return
     */
    public String getCustomDataTableRefColumnNameByModuleId(int moduleid) {
        String colName = "";
        if (moduleid == Constants.Acc_Customer_ModuleId) {
            colName = "customerId";
        } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
            colName = "journalentryId";
        } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
            colName = "soID";
        } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
            colName = "deliveryOrderId";
        } else if (moduleid == Constants.Acc_Customer_Quotation_ModuleId) {
            colName = "quotationId";
        } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
            colName = "salesReturnId";
        }
        return colName;
    }

    /**
     * Method to create Addresses JSON for request payload to be sent to AvaTax
     * service
     *
     * @param requestJobj
     * @param companyid
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getAddresses(JSONObject requestJobj, String companyid) throws JSONException, ServiceException, AccountingException {
        JSONObject addressesJobj = new JSONObject();

        JSONObject addressJobj = getCompanyShippingAddressForAvalara(companyid);
        addressesJobj.put("shipFrom", addressJobj);

        addressJobj = new JSONObject();
        if (!StringUtil.isNullOrEmpty(requestJobj.optString(IntegrationConstants.shipToAddressForAvalara))) {
            JSONObject shippingAddressJobj = new JSONObject(requestJobj.optString(IntegrationConstants.shipToAddressForAvalara));
            addressJobj = getAddressJson(shippingAddressJobj);
        } else if (!StringUtil.isNullOrEmpty(requestJobj.optString(IntegrationConstants.taxOverrideDocId))) {//To handle tax override call from front-end
            String taxOverrideDocId = requestJobj.optString(IntegrationConstants.taxOverrideDocId);
            BillingShippingAddresses billingShippingAddresses = (BillingShippingAddresses) populateMasterInformation("Invoice", "billingShippingAddresses", Constants.ID, taxOverrideDocId, null, null);
            if (billingShippingAddresses != null) {
                addressJobj = getAddressJson(billingShippingAddresses);
            }
        } else {//If no address is found, then use customer's default shipping address as shipTo address
            String customerid = requestJobj.optString(Constants.customerid);
            addressJobj = getCustomerShippingAddressForAvalara(customerid, companyid);
        }
        addressesJobj.put("shipTo", addressJobj);

        return addressesJobj;
    }

    /**
     * Method to create address Json for Avalara Service from JSONObject
     * containing address
     *
     * @param addrJobj
     * @return
     * @throws JSONException
     */
    public JSONObject getAddressJson(JSONObject addrJobj) throws JSONException {
        JSONObject addressJobj = new JSONObject();

        if (addrJobj != null) {
            addressJobj.put("Line1", addrJobj.optString("recipientName"));
            addressJobj.put("Line2", addrJobj.optString("address").replaceAll("\n", ", "));
            addressJobj.put("City", addrJobj.optString("city"));
            addressJobj.put("Region", addrJobj.optString("state"));
            addressJobj.put("Country", addrJobj.optString("country"));
            addressJobj.put("PostalCode", addrJobj.optString("postalCode"));
        }

        return addressJobj;
    }

    /**
     * Method to create address Json for Avalara Service from
     * BillingShippingAddresses class object
     *
     * @param billingShippingAddresses
     * @return
     * @throws JSONException
     */
    public JSONObject getAddressJson(BillingShippingAddresses billingShippingAddresses) throws JSONException {
        JSONObject addressJobj = new JSONObject();

        if (billingShippingAddresses != null) {
            addressJobj.put("Line1", billingShippingAddresses.getShippingRecipientName() != null ? billingShippingAddresses.getShippingRecipientName() : "");
            addressJobj.put("Line2", billingShippingAddresses.getShippingAddress() != null ? billingShippingAddresses.getShippingAddress() : "");
            addressJobj.put("City", billingShippingAddresses.getShippingCity() != null ? billingShippingAddresses.getShippingCity() : "");
            addressJobj.put("Region", billingShippingAddresses.getShippingState() != null ? billingShippingAddresses.getShippingState() : "");
            addressJobj.put("Country", billingShippingAddresses.getShippingCountry() != null ? billingShippingAddresses.getShippingCountry() : "");
            addressJobj.put("PostalCode", billingShippingAddresses.getShippingPostal() != null ? billingShippingAddresses.getShippingPostal() : "");
        }

        return addressJobj;
    }

    /**
     * Method to get customer's default shipping address using customer-id and
     * company-id
     *
     * @param customerid
     * @param companyid
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getCustomerShippingAddressForAvalara(String customerid, String companyid) throws JSONException, ServiceException {
        JSONObject addressJobj = new JSONObject();
        KwlReturnObject addressResult = getCustomerShippingAddress(customerid, companyid);
        if (addressResult != null && addressResult.getEntityList() != null && !addressResult.getEntityList().isEmpty()) {
            CustomerAddressDetails customerAddress = (CustomerAddressDetails) addressResult.getEntityList().get(0);
            addressJobj.put("Line1", customerAddress.getRecipientName() != null ? customerAddress.getRecipientName() : "");
            addressJobj.put("Line2", customerAddress.getAddress() != null ? customerAddress.getAddress() : "");
            addressJobj.put("City", customerAddress.getCity() != null ? customerAddress.getCity() : "");
            addressJobj.put("Region", customerAddress.getState() != null ? customerAddress.getState() : "");
            addressJobj.put("Country", customerAddress.getCountry() != null ? customerAddress.getCountry() : "");
            addressJobj.put("PostalCode", customerAddress.getPostalCode() != null ? customerAddress.getPostalCode() : "");
        }

        return addressJobj;
    }

    /**
     * Method to get company's shipping address using company-id
     *
     * @param companyid
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getCompanyShippingAddressForAvalara(String companyid) throws JSONException, ServiceException, AccountingException {
        JSONObject addressJobj = new JSONObject();
        KwlReturnObject kwlObj = getCompanyShippingAddress(companyid);
        if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty() && kwlObj.getEntityList().get(0) != null) {
            CompanyAddressDetails companyAddressDetails = (CompanyAddressDetails) kwlObj.getEntityList().get(0);

            addressJobj.put("Line1", companyAddressDetails.getRecipientName());
            addressJobj.put("Line2", companyAddressDetails.getAddress().replaceAll("\n", ", "));
            addressJobj.put("City", companyAddressDetails.getCity());
            addressJobj.put("Region", companyAddressDetails.getState());
            addressJobj.put("Country", companyAddressDetails.getCountry());
            addressJobj.put("PostalCode", companyAddressDetails.getPostalCode());
        } else {
            throw new AccountingException("Company address is not set. Please enter company address in System Controls.");
        }

        return addressJobj;
    }

    private String getTaxOverrideDateForAvalara(JSONObject requestJobj, String moduleid) throws ServiceException, AccountingException {
        String taxOverrideDate = null;
        String taxOverrideDocModuleId = requestJobj.optString(IntegrationConstants.taxOverrideDocModuleId);
        if (StringUtil.equal(moduleid, String.valueOf(Constants.Acc_Sales_Return_ModuleId)) && StringUtil.equal(taxOverrideDocModuleId, String.valueOf(Constants.Acc_Invoice_ModuleId))) {
            String taxOverrideDocId = requestJobj.optString(IntegrationConstants.taxOverrideDocId);
            if (!StringUtil.isNullOrEmpty(taxOverrideDocId)) {
                Date linkedDocDate = (Date) populateMasterInformation("Invoice", "journalEntry.entryDate", Constants.ID, taxOverrideDocId, null, null);
                if (linkedDocDate != null) {
                    try {
                        taxOverrideDate = authHandler.getDateOnlyFormat().format(linkedDocDate);
                    } catch (SessionExpiredException ex) {
                        Logger.getLogger(IntegrationCommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        throw new AccountingException("Error while processing data before Avalara Operation call.");
                    }
                }
            }
        }
        return taxOverrideDate;
    }

    /**
     * Method to get exemption code from Json for Avalara service payload
     *
     * @param requestJobj
     * @param companyid
     * @param moduleid
     * @param customerid
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    private String getTaxOverrideExemptionCode(JSONObject requestJobj, String companyid, String customerid) throws JSONException, ServiceException {
        String exemptionCode = null;
        if (requestJobj.has(IntegrationConstants.avalaraExemptionCode)) {
            exemptionCode = requestJobj.getString(IntegrationConstants.avalaraExemptionCode);
        } else {
            String exemptionCodeColNum = requestJobj.optString("exemptionCodeColNum", null);
            int taxOverrideDocModuleId = requestJobj.optInt(IntegrationConstants.taxOverrideDocModuleId, 0);
            String taxOverrideDocId = requestJobj.optString(IntegrationConstants.taxOverrideDocId, null);
            exemptionCode = getExemptionCodeFromRefModule(companyid, exemptionCodeColNum, taxOverrideDocModuleId, taxOverrideDocId);
        }
        return exemptionCode;
    }

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
    @Override
    public String getExemptionCodeFromRefModule(String companyid, String exemptionCodeColNum, int refModuleId, String refDocId) throws JSONException, ServiceException {
        String exemptionCode = null;
        if (StringUtil.isNullOrEmpty(exemptionCodeColNum)) {
            FieldParams fieldParams = getFieldParam(companyid, refModuleId, IntegrationConstants.avataxExemptionCode);
            if (fieldParams != null) {
                exemptionCodeColNum = Constants.Custom_column_Prefix + fieldParams.getColnum();
            }
        }
        if (!StringUtil.isNullOrEmpty(exemptionCodeColNum)) {
            if (refModuleId == Constants.Acc_Invoice_ModuleId) {//For invoice, custom data is stored in AccJECustomData table against corresponding JE's id
                refDocId = (String) populateMasterInformation("Invoice", "journalEntry.ID", Constants.ID, refDocId, null, null);
            }

            if (!StringUtil.isNullOrEmpty(refDocId)) {
                String refTableName = getCustomDataTableNameByModuleId(refModuleId);
                String refColumn = getCustomDataTableRefColumnNameByModuleId(refModuleId);
                String exemptionCodeId = (String) populateMasterInformation(refTableName, exemptionCodeColNum.toLowerCase(), "company.companyID", companyid, refColumn, refDocId);
                if (exemptionCodeId != null) {
                    FieldComboData fieldComboData = getFieldComboDataById(exemptionCodeId);
                    if (fieldComboData != null) {
                        exemptionCode = fieldComboData.getValue();
                    }
                }
            }
        }
        return exemptionCode;
    }

    @Override
    public JSONObject getAddressesForUps(JSONObject requestJobj) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject returnJobj = new JSONObject();
        String companyid = requestJobj.optString(Constants.companyKey);

        String recordIDForCostCalculation = "";//Id of packing record selected for cost calculation
        if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.billid))) {
            recordIDForCostCalculation = requestJobj.optString(Constants.billid);
        }
        boolean salesOrderCostEstimationFlag = false;
        if (!StringUtil.isNullOrEmpty(requestJobj.optString(IntegrationConstants.salesOrderCostEstimationFlag))) {
            salesOrderCostEstimationFlag = Boolean.parseBoolean(requestJobj.optString(IntegrationConstants.salesOrderCostEstimationFlag));
        }

        Map requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.companyKey, companyid);
        KwlReturnObject addressObj = accountingHandlerDAO.getAddressDetailsFromCompanyId(requestParams);
        if (addressObj != null && addressObj.getEntityList() != null && !addressObj.getEntityList().isEmpty() && addressObj.getEntityList().get(0) != null) {
            CompanyAddressDetails companyAddressDetails = (CompanyAddressDetails) addressObj.getEntityList().get(0);
            returnJobj.put(IntegrationConstants.shipFrom_contactPersonName, companyAddressDetails.getContactPerson());
            returnJobj.put(IntegrationConstants.shipFrom_Name, companyAddressDetails.getAliasName());
            returnJobj.put(IntegrationConstants.shipFrom_AddressLine, companyAddressDetails.getAddress());
            returnJobj.put(IntegrationConstants.shipFrom_City, companyAddressDetails.getCity());
            returnJobj.put(IntegrationConstants.shipFrom_StateProvinceCode, companyAddressDetails.getState());
            returnJobj.put(IntegrationConstants.shipFrom_PostalCode, companyAddressDetails.getPostalCode());
            returnJobj.put(IntegrationConstants.shipFrom_CountryCode, companyAddressDetails.getCountry());
            returnJobj.put(IntegrationConstants.shipFrom_PhoneNumber, companyAddressDetails.getPhone());
        }

        BillingShippingAddresses billingShippingAddresses = null;
        if (salesOrderCostEstimationFlag) {  //call from sales order report
            billingShippingAddresses = (BillingShippingAddresses) populateMasterInformation("SalesOrder so", "so.billingShippingAddresses", "so.ID", recordIDForCostCalculation, null, null);
        } else {    //call from packing report
            billingShippingAddresses = (BillingShippingAddresses) populateMasterInformation("PackingDetail pd", "pd.dodetailid.deliveryOrder.billingShippingAddresses", "pd.packing.ID", recordIDForCostCalculation, null, null);
        }
        if (billingShippingAddresses != null) {
            returnJobj = createShipToAddressJsonForUps(returnJobj, billingShippingAddresses);
        }

        return returnJobj;
    }

    private JSONObject createShipToAddressJsonForUps(JSONObject addressJobj, BillingShippingAddresses billingShippingAddresses) throws JSONException {
        if (addressJobj != null && billingShippingAddresses != null) {
            addressJobj.put(IntegrationConstants.shipTo_contactPersonName, billingShippingAddresses.getShippingContactPerson());
            addressJobj.put(IntegrationConstants.shipTo_Name, billingShippingAddresses.getShippingAddressType());
            addressJobj.put(IntegrationConstants.shipTo_AddressLine, billingShippingAddresses.getShippingAddress());
            addressJobj.put(IntegrationConstants.shipTo_City, billingShippingAddresses.getShippingCity());
            addressJobj.put(IntegrationConstants.shipTo_StateProvinceCode, billingShippingAddresses.getShippingState());
            addressJobj.put(IntegrationConstants.shipTo_PostalCode, billingShippingAddresses.getShippingPostal());
            addressJobj.put(IntegrationConstants.shipTo_CountryCode, billingShippingAddresses.getShippingCountry());
            addressJobj.put(IntegrationConstants.shipTo_PhoneNumber, billingShippingAddresses.getShippingPhone());
        }
        return addressJobj;
    }
}
