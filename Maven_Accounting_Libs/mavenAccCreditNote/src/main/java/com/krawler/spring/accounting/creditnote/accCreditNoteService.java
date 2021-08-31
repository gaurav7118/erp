/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.creditnote;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CreditNote;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public interface accCreditNoteService {
    
    /**
     * Description: This method is used to save credit note 
     * @param paramJobj
     * @return JSONObject
     */
    public JSONObject saveCreditNoteJSON(JSONObject paramJobj);
    
    public JSONObject importCreditNotesJSON(JSONObject paramJobj);
    
    public String getReasonIDByName(String salesPersonName, String companyID) throws AccountingException;
    
    public void deleteJEArray(String oldjeid, String companyid) throws ServiceException, AccountingException, SessionExpiredException;
    
    public void updateOpeningInvoiceAmountDue(String creditNoteId, String companyId) throws JSONException, ServiceException;
    
    public List mapDebitTerms(String cnTerms, String creditNoteId, String userid) throws ServiceException;
    
    public boolean getInvoiceStatusForDO(InvoiceDetail iDetail) throws ServiceException;
    
    public void deleteJEDetailsCustomData(String jeid) throws ServiceException;

    public KwlReturnObject deleteCreditNotePartialy(HashMap<String, Object> dataMap) throws JSONException, ServiceException,AccountingException;
    
    public HashMap getCreditNoteCommonCode(HttpServletRequest request, HttpServletResponse response);
    
    public HashMap getCreditNoteCommonCode(JSONObject paramJobj);
    
    public JSONArray getCreditNoteMergedJson(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException;
    
    public JSONArray getOpeningCreditNotesJson(HashMap<String, Object> requestParams, List<CreditNote> list, JSONArray JArr) throws ServiceException;
      
    public List linkCreditNote(JSONObject paramJobj, String debitNoteId, Boolean isInsertAudTrail) throws ServiceException, SessionExpiredException, JSONException, AccountingException,ParseException;
    
    public List linkCreditNotewithoutRequest(JSONObject paramJobj, String creditNoteId, Boolean isInsertAudTrail,Map<String, Object> requestParams) throws ServiceException, SessionExpiredException, JSONException, AccountingException,ParseException;
    
    public void updateLinkingInformationOfCreditNote(String creditNoteDetailID) throws ServiceException, SessionExpiredException, JSONException, AccountingException;
  
    public JSONObject getColumnsForCreditNoteWithAccounts(HttpServletRequest request, boolean isExport)throws ServiceException;

    public List approvePendingCreditNote(Map<String, Object> requestParams)throws ServiceException,AccountingException;
    
    public boolean rejectPendingCreditNote(Map<String, Object> requestParams,JSONArray jArr)throws ServiceException;
    
    public List approveCreditNote(CreditNote creditnote, HashMap<String, Object> cnApproveMap, boolean isMailApplicable)throws ServiceException;
    
    /**
     * @param mailParameters (String companyid, String ruleId, String documentNumber, String fromName, boolean hasApprover, int moduleid,String createdby, String PAGE_URL)
     * @throws ServiceException
     * @throws MessagingException 
     */
    public void sendMailToApprover(Map<String, Object> mailParameters) throws ServiceException, MessagingException;

    public JSONObject checkInvoiceKnockedOffDuringCreditNotePending(Map<String, Object> requestParams);

    public List approvePendingCreditNoteAgainstInvoiceAsCNOtherwise(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONObject getCreditNoteApprovalPendingJsonData(JSONObject obj, String noteid, String companyid, String userid, String userName) throws  ServiceException;
    
    public boolean isNoteLinkedWithPayment(String noteId);
    
    public boolean isNoteLinkedWithAdvancePayment(String noteId);
    
    public boolean isCreditNotelinkedInDebitNote(String noteId,String companyId);
    
    void postRoundingJEAfterLinkingInvoiceInCreditNote(JSONObject paramJobj) throws ServiceException;
    
    void postRoundingJEOnCreditNoteSave(JSONObject paramJobj) throws ServiceException;
    
    void postRoundingJEAfterApproveCreditNote(JSONObject paramJobj) throws ServiceException;
    
    public void getCreditNoteCustomDataForPayment(HashMap<String, Object> request, JSONObject obj, CreditNote creditMemo, JournalEntry je) throws ServiceException;
    
    public JSONObject deleteCreditNoteTemporary(JSONObject paramJobj);
    public JSONArray getCNKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException;
    public JSONArray getAllCNKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException;
    public JSONArray getOpeningCNKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException;
}
