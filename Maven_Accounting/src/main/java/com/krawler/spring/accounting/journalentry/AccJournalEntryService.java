/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.journalentry;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

/**
 *
 * @author krawler
 */
public interface AccJournalEntryService {

    //delete single journal entry
    public void deleteJournalEntry (JSONObject paramJobj, JSONObject jobj, int countryid, String companyid, Boolean flag) throws SessionExpiredException, AccountingException, ServiceException, JSONException;
    
    public JSONObject postJournalEntry(JSONObject reqParams) throws JSONException, ServiceException,SessionExpiredException,AccountingException;
}
