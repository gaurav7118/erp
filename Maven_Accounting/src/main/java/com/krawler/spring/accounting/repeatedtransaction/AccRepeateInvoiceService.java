/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.repeatedtransaction;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author krawler
 */
public interface AccRepeateInvoiceService {

    public String processSalesInvoice(Invoice invoice, HashMap<String, Object> requestParams, Iterator Excludeditr, List ExcludedIDlist) throws ServiceException, SessionExpiredException;

    public String processJournalEntry(JournalEntry JEObj, HashMap<String, Object> requestParams) throws ServiceException;

    public String processSalesOrder(SalesOrder SalesOrderObj, HashMap<String, Object> requestParams) throws ServiceException;

    public void sendMail(HashMap requestParams) throws ServiceException;
    
    public JSONArray createDimensionArrayToCalculateGSTForInvoice(Invoice invoice, String companyid) throws JSONException, ServiceException;
    
    public List mapInvoiceDetailTerms(JSONObject termObj, String userid) throws ServiceException;
}
