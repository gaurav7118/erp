/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.debitnote;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.DebitNote;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public interface accDebitNoteService {

    public void updateOpeningInvoiceAmountDue(String debitNoteId, String companyId) throws JSONException, ServiceException;

    public KwlReturnObject deleteDebitNotePartialy(HashMap<String, Object> dataMap) throws ServiceException, JSONException,AccountingException;
    
    public HashMap getDebitNoteCommonCode(HttpServletRequest request, HttpServletResponse response);
    
    public JSONArray getOpeningDebitNotesJson(HashMap<String, Object> requestParams, List list, JSONArray JArr) throws ServiceException;
    
    public JSONArray getDebitNotesMergedJson(HashMap<String, Object> requestParams, List list, JSONArray JArr) throws ServiceException;
    
    public JSONArray getDebitNoteRowsJson(HashMap<String, Object> requestParams) throws ServiceException;
    
    public List linkDebitNote(HttpServletRequest request, String debitNoteId, Boolean isInsertAudTrail,Map gramounts) throws ServiceException, SessionExpiredException, JSONException, AccountingException;
    
    public void updateLinkingInformationOfDebitNote(String creditNoteDetailID) throws ServiceException, SessionExpiredException, JSONException, AccountingException;
    
    public List approvePendingDebitNote(Map<String, Object> requestParams) throws ServiceException, AccountingException;
    
    public boolean rejectPendingDebitNote(Map<String, Object> requestParams,JSONArray jArr)throws ServiceException;
    
    public List approveDebitNote(DebitNote creditnote, HashMap<String, Object> cnApproveMap, boolean isMailApplicable)throws ServiceException;
    
    /**
     * @param mailParameters(String companyid, String ruleId, String documentNumber, String fromName, boolean hasApprover, int moduleid,String createdby, String PAGE_URL)
     * @throws ServiceException
     * @throws MessagingException 
     */
    public void sendMailToApprover(Map<String, Object> mailParameters) throws ServiceException, MessagingException;

    public JSONObject checkInvoiceKnockedOffDuringDebitNotePending(Map<String, Object> requestParams);

    public List approvePendingDebitNoteAgainstInvoiceAsDNOtherwise(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONObject getDebitNoteApprovalPendingJsonData(JSONObject obj, String noteid, String companyid, String userid, String userName) throws ServiceException;

    public boolean isNoteLinkedWithPayment(String noteId);
    
    public boolean isDebitNoteLinkedWithCreditNote(String noteId,String companyId);
    
    public boolean isNoteLinkedWithAdvancePayment(String noteId);
    
    public boolean isNoteLinkedWithInvoice(String noteId, String comapnyId);
    
    public void deleteJEArray(String oldjeid, String companyid) throws ServiceException, AccountingException, SessionExpiredException;
    
    public void postRoundingJEAfterLinkingInvoiceInDebitNote(JSONObject paramJobj) throws ServiceException;
    
    public void postRoundingJEAfterApproveDebitNote(JSONObject paramJobj) throws ServiceException;
    
    public void postRoundingJEOnDebitNoteSave(JSONObject paramJobj) throws ServiceException;
    
    public void getDebitNoteCustomDataForPayment(HashMap<String, Object> request, JSONObject obj, DebitNote debitMemo, JournalEntry je) throws ServiceException;

    public JSONArray getDNKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException;
    public JSONArray getAllDNKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException;
    public JSONArray getOpeningDNKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException;
}
