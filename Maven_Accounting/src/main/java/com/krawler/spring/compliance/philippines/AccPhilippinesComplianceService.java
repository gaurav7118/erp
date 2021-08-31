/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.compliance.philippines;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

/**
 *
 * @author krawler
 */
public interface AccPhilippinesComplianceService {
    
    public JSONObject getPurchaseReliefReportSummary(JSONObject params) throws ServiceException,JSONException;

    public JSONObject getVATSummaryReportData(JSONObject requestParams) throws ServiceException, JSONException;
    
    public JSONObject getVATDetailReportData(JSONObject requestParams) throws ServiceException, JSONException;
    
    public JSONObject getVATReportSectionData(JSONObject requestParams) throws ServiceException, JSONException;
    
    public JSONArray getSalesReliefReportData(JSONObject requestParams) throws ServiceException,JSONException;
    
    public JSONObject getSalesReliefSummaryReport(JSONObject requestParams) throws ServiceException,JSONException;
        
}
