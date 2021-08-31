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
import com.sun.jersey.core.header.FormDataContentDisposition;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Map;

public interface MasterService {

    public JSONObject saveTax(JSONObject jobjParam) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException, JSONException;

    public JSONObject getProduct(JSONObject jobj) throws ServiceException, JSONException,SessionExpiredException;
    
    public JSONObject getProductCategory(JSONObject jobj) throws ServiceException, JSONException,SessionExpiredException;
    
    public JSONObject getWarehouse(JSONObject jobj) throws ServiceException, JSONException ,SessionExpiredException;
    
    public JSONObject getLocation(JSONObject jobj) throws ServiceException, JSONException ,SessionExpiredException;
    
    public JSONObject getNewBatches(JSONObject jobj) throws ServiceException, JSONException ,SessionExpiredException;
    
    public JSONObject getLevels(JSONObject jobj) throws ServiceException, JSONException ,SessionExpiredException;
    
    public JSONObject getStoreMasters(JSONObject jobj) throws ServiceException, JSONException ,SessionExpiredException;
    
    public JSONObject getNewSerials(JSONObject jobj) throws ServiceException, JSONException, SessionExpiredException;
    
    public JSONObject getBatchRemaningQuantity(JSONObject jobj) throws ServiceException, JSONException, SessionExpiredException;

    public JSONObject getProductByContract(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject getCustomers(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject getAsset(JSONObject jobject) throws ServiceException, JSONException;

    public JSONObject getTax(JSONObject jobj) throws ServiceException, JSONException,SessionExpiredException,UnsupportedEncodingException;

    public JSONObject getCurrencyExchange(JSONObject jobj) throws JSONException, ParseException, SessionExpiredException, ServiceException;

    public JSONObject saveProjectDetails(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject deleteProjectDetails(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject deleteCostCenter(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject getCostCenter(JSONObject jobj) throws ServiceException, SessionExpiredException, JSONException;
    
    public JSONObject getUnitOfMeasure(JSONObject jobj) throws ServiceException, SessionExpiredException, JSONException;

    public JSONObject saveTerm(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject getTerm(JSONObject paramJobj) throws ServiceException, JSONException;
    
    public JSONArray getInvoiceTerms(JSONObject paramJobj) throws JSONException, SessionExpiredException;

    public JSONObject getAllCurrency(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject getAllCountry() throws ServiceException, JSONException;

    public JSONObject getAllTimeZone() throws ServiceException, JSONException;

    public JSONObject getAllStates(String countryid) throws ServiceException, JSONException;

    public JSONObject getDateFormat() throws ServiceException, JSONException;

    public JSONObject saveCustomer(JSONObject jobj) throws JSONException, ServiceException;
    
    public JSONObject deleteCustomer(JSONObject jobject) throws ServiceException, JSONException;

    public JSONObject saveProductReplacement(JSONObject jobject) throws JSONException, ServiceException;

    public JSONObject deleteProductReplacement(JSONObject jobject) throws AccountingException, ServiceException, JSONException;

    public JSONObject saveProductMaintenance(JSONObject jobject) throws ServiceException, JSONException;
    
    public JSONObject saveInventoryConsumption(JSONObject jobject) throws ServiceException, JSONException, AccountingException;

    public JSONObject deleteProductMaintenance(JSONObject jobject) throws ServiceException, JSONException;
    
    public JSONObject getProductsforID(Map<String,Object> requestParams) throws ServiceException, JSONException, SessionExpiredException;

    public JSONObject getMasterItems(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject getCustomCombodata(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject getDefaultColumns(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject savePaymentMileStoneDetails(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject uploadImage(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws ServiceException, JSONException;
    
    public JSONObject deleteUploadedFile(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject getSalesAnalysisChart(JSONObject paramsjobj) throws ServiceException, JSONException, SessionExpiredException;
    
    public JSONObject saveCustomerCheckInOut(JSONObject jobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONObject getCustomerCheckIn(JSONObject paramsjobj) throws ServiceException, JSONException, SessionExpiredException;
    
    public JSONObject getSalesSummaryReport(JSONObject paramsjobj) throws ServiceException, JSONException, SessionExpiredException;
    
    public JSONObject getCostCenterFromFieldParams(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException;
    
    public JSONObject getMasterGroups(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException;
    
    public JSONObject getMasterItemsForCustomFoHire(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException ;
    
    public JSONObject getEntityCustomData(JSONObject paramJobj) throws  ServiceException,JSONException;
    
    public JSONObject getInspectionTemplateList(JSONObject jobj) throws ServiceException,JSONException;
}
