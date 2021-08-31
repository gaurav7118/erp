/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 */
package com.krawler.spring.accounting.purchaseorder.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.*;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import javax.script.ScriptException;

public interface AccPurchaseOrderModuleService {

    public JSONObject savePurchaseOrderJSON(JSONObject paramJobj) throws AccountingException;
    
    public JSONObject saveSecurityGateEntryJSON(JSONObject paramJobj);

    /**
     * @param mailParameters (String companyid, String ruleId, String prNumber, String fromName, boolean hasApprover, int moduleid, String createdby,boolean isEdit, String PAGE_URL)
     * @throws ServiceException 
     */
    public void sendMailToApprover(Map<String, Object> mailParameters) throws ServiceException;
    
    public void sendApprovalMailIfAllowedFromSystemPreferences(HashMap emailMap) throws ServiceException;
    
    public void savePurchaseOrderOtherDetails(JSONObject paramJobj, String purchaseOrderId, String companyid) throws ServiceException, AccountingException;

    public Set<PurchaseRequisitionAssetDetails> savePurchaseRequisitionAssetDetails(JSONObject paramJobj, String productId, String assetDetails, boolean invrecord, boolean isQuotationFromPR, boolean isPOFromVQ) throws SessionExpiredException, AccountingException, UnsupportedEncodingException;

    public Set<AssetPurchaseRequisitionDetailMapping> saveAssetPurchaseRequisitionDetailMapping(String purchaseRequisitionDetailId, Set<PurchaseRequisitionAssetDetails> assetDetailsSet, String companyId, int moduleId) throws AccountingException;

    public void updateVQisOpenAndLinking(String linkNumbers) throws ServiceException;

    public List<String> approvePurchaseOrder(PurchaseOrder poObj, HashMap<String, Object> poApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException;

    public HashMap mapExciseDetails(JSONObject temp, JSONObject paramJobj) throws ServiceException;

    public List mapInvoiceTerms(String InvoiceTerms, String id, String userid, boolean isQuotation) throws ServiceException;

    public JSONObject importPurchaseOrderJSON(JSONObject paramJobj);
    
    public void saveImportLog(JSONObject requestJobj, String msg, int total, int failed, int moduleID);
    
    public String getAgentIDByName(String agentName, String companyID) throws AccountingException;
    
    public String getTypeOfSalesIDByName(String agentName, String companyID) throws AccountingException;
    
    public String getCostCenterIDByName(String costCenterName, String companyID) throws AccountingException;
    
    public Vendor getVendorByCode(String vendorCode, String companyID) throws AccountingException;
    
    public Vendor getVendorByName(String vendorName, String companyID) throws AccountingException;
    
    public JSONArray createGlobalCustomFieldArrayForImport(JSONObject requestJobj, JSONArray jSONArray, String[] recarr, DateFormat df, int moduleID) throws JSONException, ParseException;
    
    public double getExchangeRateForTransaction(JSONObject requestJobj, Date billDate, String currencyID) throws JSONException, ServiceException;
    
    public JSONArray createLineLevelCustomFieldArrayForImport(JSONObject requestJobj, JSONArray jSONArray, String[] recarr, DateFormat df, int moduleID) throws JSONException, ParseException;
    
    public JSONObject saveVendorQuotationJSON(JSONObject paramJobj);
    
    public JSONObject importVendorQuotationJSON(JSONObject paramJobj);

    public String deletePurchaseRequisition(JSONObject jobj, JSONObject requestJobj, String linkedTransactions, String companyid, boolean isFixedAsset) throws ServiceException;

    public String deletePurchaseRequisitionPermanent(JSONObject jobj, JSONObject requestJobj, String linkedTransactions, String companyid, boolean isFixedAsset) throws ServiceException;

    public String deleteQuotation(JSONObject jobj, JSONObject requestJobj, String linkedTransaction, String companyid, String audtmsg) throws ServiceException;

    public String deleteQuotationPermanent(JSONObject jobj, JSONObject requestJobj, String linkedTransaction, String companyid, boolean isFixedAsset) throws ServiceException, AccountingException;

    public String deleteQuotationVersion(JSONObject jobj, JSONObject requestJobj, String quotationVersions, String companyid) throws ServiceException, JSONException;

    public String deleteQuotationVersionPermanent(JSONObject jobj, JSONObject requestJobj, String quotationVersion, String companyid) throws ServiceException, JSONException;

    public String deleteRFQ(JSONObject jobj, JSONObject requestJobj, String linkedTransactions, String companyid) throws ServiceException;

    public String deleteRFQPermanent(JSONObject jobj, JSONObject requestJobj, String linkedTransactions, String companyid, boolean isFixedAsset) throws ServiceException;

    public String deletePurchaseOrder(JSONObject jobj, JSONObject requestJobj, String linkedTransaction, String companyid, String modulename) throws ServiceException;

    public String deletePurchaseOrderPermanent(JSONObject jobj, JSONObject requestJobj, String linkedTransaction, String companyid, boolean isFixedAsset, String modulename) throws ServiceException,JSONException;
    
    public String deletePurchaseOrdersPermanent(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException;
    
    public double calCulateBalanceQtyOfRequisitionForPO(HashMap request) throws ServiceException;
    
    public double getQuantityStatusOfRequisition(HashMap request) throws ServiceException;
    
}
