/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.receivepayment.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Receipt;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author krawler
 */
public interface AccReceivePaymentModuleService {
     public HashMap<String, Object> saveCustomerReceipt(JSONObject paramJobj);
     
     public JSONObject importReceivePaymentJSON(JSONObject paramJobj);          
     public List approveReceivePayment(List receipt, HashMap<String, Object> cnApproveMap, boolean isMailApplicable) throws ServiceException;
     public JSONObject getReceivePaymentApprovalPendingJsonData(JSONObject obj, Double totalAmount, int approvallevel, String companyid, String userid, String userName) throws ServiceException;     
     public List getAdvancePaymentDetails(String receiptadvancedetailid) throws ServiceException;
     public void updateAdvanceDetailAmountDueOnAmountReceived(String receiptadvancedetailid,double amount)  throws ServiceException ;
     public List approvePendingReceivePayment(Map<String, Object> requestParams) throws ServiceException,AccountingException;
     public boolean rejectPendingReceivePayment(Map<String, Object> requestParams, JSONArray jArr) throws ServiceException;
     public JSONObject checkInvoiceKnockedOffDuringReceivePaymentPending(Map<String, Object> requestParams);
     public JSONObject deleteReceiptForEdit(JSONObject paramJObj) throws ServiceException, JSONException, AccountingException;
     public String deleteReceiptPermanent(JSONObject paramJObj) throws AccountingException, SessionExpiredException, ServiceException, ParseException, JSONException;
     public void updateOpeningBalance(Receipt receipt, String companyId) throws JSONException, ServiceException;
     public void updateReceiptAdvancePaymentAmountDue(Receipt receipt, String companyId,JSONObject params) throws JSONException, ServiceException;
     public void updateReceiptLoanAmountDue(Receipt receipt, String companyId, boolean isPermanentDelete) throws JSONException, ServiceException;
     public void updateDNAmountDueForReceipt(Receipt receipt, String companyId) throws ServiceException;
     
     public void postRoundingJEAfterLinkingInvoiceInReceipt(JSONObject paramJobj) throws ServiceException;

     public void postRoundingJEOnReceiptSave(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException;

     public void postRoundingJEAfterReceiptApprove(JSONObject paramJobj) throws ServiceException;
     
     public void postRoundingJEOnRevertDishonouredReceipt(JSONObject paramJobj,Set<String> amountDueUpdatedInvoiceIDSet) throws ServiceException;
     
     public HashMap<String, Object> saveBulkCustomerReceipt(JSONObject paramJobj);
     
     public JSONObject linkReceiptToDocumentsJSON(JSONObject paramJobj) ;
     
     public KwlReturnObject checkTransactionsForDiscountOnPaymentTerms(JSONObject paramJobj)  throws ServiceException ;
     
     public JSONObject deleteReceiptMerged(JSONObject paramJObj);
     
     public String deleteReceiptTemporary(JSONObject paramJObj) throws AccountingException, SessionExpiredException, ServiceException,JSONException;
}
