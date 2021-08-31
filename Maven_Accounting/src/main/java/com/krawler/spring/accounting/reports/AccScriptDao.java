/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccScriptDao {

    public Map getDebitNoteAndDntaxEntry(HashMap<String, Object> request) throws ServiceException;

    public Map cleanUpCompanyData(HashMap<String, Object> request) throws ServiceException;

    public Map getCreditNoteAndCNtaxEntry(HashMap<String, Object> request) throws ServiceException;

    public Map getTrnsactionsOtherThanControlAccountForVendor(Map<String, Object> request) throws ServiceException;

    public Map getOpeningTrnsactionsOtherThanControlAccountForVendor(Map<String, Object> request) throws ServiceException;

    public Map getTrnsactionsOtherThanControlAccountForCustomer(Map<String, Object> request) throws ServiceException;

    public Map getOpeningTrnsactionsOtherThanControlAccountForCustomer(Map<String, Object> request) throws ServiceException;

    public Map getOpeningDocumentListForVendor(HashMap<String, Object> request) throws ServiceException;

    public Map getOpeningDocumentListForCustomer(HashMap<String, Object> request) throws ServiceException;

    public List getDNForGainLossNotPosted(Map<String, Object> request) throws ServiceException;

    public List getCNForGainLossNotPosted(Map<String, Object> request) throws ServiceException;

    public List getPaymentForGainLossNotPosted(Map<String, Object> request) throws ServiceException;

    public List getReceiptForGainLossNotPosted(Map<String, Object> request) throws ServiceException;

    public Map getManualJEForControlAccount(Map<String, Object> request) throws ServiceException;

    public List getInvoicesAmountDiffThanJEAmount(Map<String, Object> request) throws ServiceException;

    public List getGoodsReceiptAmountDiffThanJEAmount(Map<String, Object> request) throws ServiceException;

    public List getDifferentPaymentAndGainLossJEAccount(Map<String, Object> request) throws ServiceException;

    public List getDifferentReceiptAndGainLossJEAccount(Map<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getAllCompanyOfBrandDiscount(JSONObject param) throws ServiceException;
    
    public List updateProductCustomData(Map requestMap,List returnlist) throws ServiceException;
    
    public void updateBrandDiscount(JSONObject dataobj) throws ServiceException;
    
    public KwlReturnObject getProductsHavingBrand(JSONObject jSONObject) throws ServiceException;

    public Map updateMailidsLocally(Map<String, Object> request) throws ServiceException;
    public KwlReturnObject getAllCompanyIndiaUS(JSONObject param) throws ServiceException ;   
    public KwlReturnObject getAllCompanisOfIndiaOnly(JSONObject param) throws ServiceException ;   
    public List getCompanyData(JSONObject params) throws ServiceException;

    public List getFieldParamsData(JSONObject params) throws ServiceException;

    public List getProductsForTaxClassHistory(JSONObject params) throws ServiceException;

    public void insertProductTaxClassInHistory(JSONObject params) throws ServiceException;

    public void insertCustomerGSTHistory(JSONObject params) throws ServiceException;

    public void insertVendorGSTHistory(JSONObject params) throws ServiceException;

    public void insertSalesTransactionHistoryData(JSONObject params) throws ServiceException;

    public void insertPurcaseTransactionHistoryData(JSONObject params) throws ServiceException;
    
    public List getCommonQueryResultForDimensionValueScript(JSONObject reqParams , String query , List queryParams, boolean isDelete) throws ServiceException;
    
    public void insertLineDataForTransaction(JSONObject params) throws ServiceException;
    
    public List getForegnReferencesForTax(JSONObject params) throws ServiceException;
    public List getDocumentDetailsForTax(JSONObject params) throws ServiceException;
    
    public List getStateValueForSalesDocument(JSONObject reqParams) throws ServiceException, JSONException;
  
    public void setStateValueToInvoice(JSONObject reqParams) throws ServiceException, JSONException;

    public void setInvoiceDetailsTermAmount(JSONObject reqParams) throws ServiceException;

    public void updateAmountInCustomerJEDetails(JSONObject reqParams) throws ServiceException;

    public void deleteGSTJEDetails(JSONObject reqParams) throws ServiceException;
    
    public List getLinkedDocument(JSONObject reqParams) throws ServiceException;
    
    public List getStateValueForPurchaseDocument(JSONObject reqParams) throws ServiceException, JSONException;

}
