/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.FieldComboData;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public interface AccOtherReportsService {

    public void getCommissionData(JSONObject params, JSONObject dataObj) throws ServiceException, JSONException, SessionExpiredException, UnsupportedEncodingException;
    
    public JSONObject getSalesCommission(JSONObject params) throws ServiceException, SessionExpiredException, UnsupportedEncodingException;
    
    public JSONObject getAmountSalesCommission(JSONObject params) throws ServiceException, SessionExpiredException, UnsupportedEncodingException;
    
    public JSONObject getPaymentTermCommission(JSONObject params) throws ServiceException, SessionExpiredException, UnsupportedEncodingException;
    
    public JSONObject getBrandCommission(JSONObject params) throws ServiceException, SessionExpiredException, UnsupportedEncodingException;
    
    public List<MasterItem> getCostCategoryMasterItems(JSONObject jobject) throws ServiceException;
            
    public JSONObject getCostOfManufacturingColumnModel(JSONObject jobject,Map<String,Object>dataMap) throws JSONException, ServiceException;

    public JSONObject getCostOfManufacturingData(JSONObject jobject,Map<String,Object>dataMap) throws JSONException, ServiceException;
    
    public List<FieldComboData> getFieldComboData(JSONObject params) throws ServiceException,JSONException;
    
    public int getCustomColumnNoForModuleField(JSONObject params) throws  ServiceException;
    
    public List<String> getFcdIdForField(Map<String, Object> dataMap, JSONObject params) throws JSONException;
}
