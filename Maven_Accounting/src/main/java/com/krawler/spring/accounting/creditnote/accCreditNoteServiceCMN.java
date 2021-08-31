/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.creditnote;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CreditNoteDetail;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public interface accCreditNoteServiceCMN {
    
    public JSONObject getCreditNoteRow(HttpServletRequest request, String[] billids) throws SessionExpiredException, ServiceException;

    public JSONObject getCreditNoteRows(JSONObject paramJobj, String[] billids) throws SessionExpiredException, ServiceException;

    public JSONObject deleteCreditNotesPermanentJSON(JSONObject paramJobj);

    public void deleteJEArray(String oldjeid, String companyid) throws ServiceException, AccountingException, SessionExpiredException;

    public JSONArray addTotalsForPrint(HttpServletRequest request, JSONArray DataJArr) throws JSONException, SessionExpiredException;

    public void getCNDetails(HashSet<CreditNoteDetail> cndetails, String companyId) throws ServiceException;

    public List unlinkCreditNoteFromDebitNote(HttpServletRequest request) throws ServiceException, SessionExpiredException;

    public List linkCreditNote(HttpServletRequest request, String creditNoteId, Boolean isInsertAudTrail) throws ServiceException, SessionExpiredException, JSONException, AccountingException;

    public JSONObject getCreditNoteLinkedDocumnets(HttpServletRequest request) throws ServiceException, SessionExpiredException;

    public List unlinkCreditNoteFromTransactions(HttpServletRequest request) throws ServiceException, SessionExpiredException;

    public JSONArray exportCreditNoteWithDetails(HttpServletRequest request, HttpServletResponse response, JSONArray dataArray);

    public HashMap<String, Object> getCreditNoteMap(HttpServletRequest request) throws SessionExpiredException, UnsupportedEncodingException;
    
}
