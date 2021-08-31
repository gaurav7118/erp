/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.Date;

public interface WSUtilService {
    public JSONObject populateAdditionalInformation(JSONObject jobj) throws ServiceException, JSONException;
    public JSONObject getErrorResponse(String errorCode, JSONObject jobj, String errorMsg);
    public boolean isCompanyExists( JSONObject jobj) throws ServiceException;
    public JSONObject populateMastersInformation(JSONObject jobj) throws ServiceException;
    public JSONArray createJSONForCustomField(String customField, String companyid, int moduleid) throws JSONException;
    public JSONObject buildLineLevelTerms(String linelevel,JSONObject detailObj,boolean isReceivePayment) throws JSONException ;
    public JSONObject manipulateGlobalLevelFieldsNew(JSONObject paramJObj, String companyid) throws ServiceException, ParseException; 
    public JSONObject replaceBooleanwithStringValues(JSONObject paramJobj) throws JSONException;
    public JSONObject getSequenceFormatId(JSONObject paramJobj,String moduleid) throws JSONException,ServiceException ;
    public JSONObject manipulateAccountandInvoiceDetails(JSONObject paramJObj) throws ServiceException, ParseException, SessionExpiredException;
    public JSONObject manipulatePaymentOrReceiptDetails(JSONObject paramJObj, int moduleid) throws ServiceException, ParseException, SessionExpiredException ;
    public JSONObject buildAdvanceSearchJson(JSONObject paramJObj) throws ServiceException, JSONException ;
    public void checkUserActivePeriodRange(String companyid, String userid, Date transactionDate,int moduleid) throws ServiceException;
    public JSONObject getUserPermissionsforUnitPriceAndAmount(JSONObject paramJObj) throws ServiceException;
}
