/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting.journalentry.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;
import javax.mail.MessagingException;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public interface AccJournalEntryModuleService {

    public JSONObject saveJournalEntry(HttpServletRequest request, HttpServletResponse response);

    public KwlReturnObject updateJEEntryNumberForNewJE(Map<String, Object> jeDataMap, JournalEntry JE, String companyid, String sequenceFormat, int approvedLevel);

    public String getNextChequeNumberForRecurredJE(String companyId, String bankAccountId);

    public KwlReturnObject addCheque(HashMap<String, Object> checkHM);

    public JSONObject saveJournalEntryRemoteApplication(HttpServletRequest request, HttpServletResponse response);

    public JSONObject saveJournalEntryJson(JSONObject paramJobj) throws JSONException, SessionExpiredException, ServiceException;
    
    public JournalEntry saveJournalEntryRemoteApplicationJson(JSONObject paramJobj, JSONObject dataMap) throws SessionExpiredException, ServiceException, AccountingException;

    public JSONObject saveJournalEntryRemoteApplicationJson(JSONObject paramJobj) throws SessionExpiredException, ServiceException, JSONException, AccountingException;

    public JSONObject updateJournalEntry(JSONObject requestparams);

    //Web Application call
    public JSONObject saveJournalEntry(JSONObject paramJObj);

    public int approveJE(JournalEntry JE, String companyid, int level, String amount, JSONObject paramJobj, boolean fromCreate, String currentUser) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException;
}
