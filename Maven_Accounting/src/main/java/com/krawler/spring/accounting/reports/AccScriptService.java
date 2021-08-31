/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 *
 * @author krawler
 */
public interface AccScriptService {

    public Map getCNDNForGainLossNotPosted(Map<String, Object> request) throws ServiceException;

    public Map getTrnsactionsOtherThanControlAccountForVendor(Map<String, Object> request) throws ServiceException;

    public Map getTrnsactionsOtherThanControlAccountForCustomer(Map<String, Object> request) throws ServiceException;

    public Map getPaymentReceiptForGainLossNotPosted(Map<String, Object> request) throws ServiceException;

    public Map getJournalEntryRecordForControlAccounts(Map<String, Object> request) throws ServiceException;

    public Map getInvoicesAmountDiffThanJEAmount(Map<String, Object> request) throws ServiceException;

    public Map getDifferentPaymentReceiptAndGainLossJEAccount(Map<String, Object> request) throws ServiceException;
    
    public JSONObject copyDataFromMasterToDimension(JSONObject params) throws ServiceException;
    
    public JSONObject createEntityAndProductCategory(JSONObject params) throws ServiceException ;
    
    public JSONObject createRemainingCustomFields(JSONObject params) throws ServiceException ;
    
    public JSONObject insertRemainingFieldcomboData(JSONObject params) throws ServiceException ;
            
    public JSONObject DeleteEmptyValuedFieldcomboValuesMappedToEntityCustomField(JSONObject params) throws ServiceException ;
    
    public JSONObject deleteEmptyAndNoneValuesFromCustomDimension(JSONObject params) throws ServiceException ;
    
    public HSSFWorkbook deleteTaxFromIndianCompany(JSONObject jSONObject) throws ServiceException;
    
    public JSONObject insertGSTFieldsData(JSONObject params) throws ServiceException;
      
    public JSONObject updateGSTTransactions(JSONObject reqParams) throws ServiceException, JSONException, SessionExpiredException, ParseException,AccountingException;
            
}
