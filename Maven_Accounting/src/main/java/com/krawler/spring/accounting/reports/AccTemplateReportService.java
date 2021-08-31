/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface AccTemplateReportService {
     public JSONObject getLedgerInfo(JSONObject requestJobj, JSONObject accountJobj, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException;   
     public JSONArray getSOAInfo(JSONObject requestJobj) throws ServiceException, JSONException;
     public Map<String, Object> getSOAInfoMap(JSONObject requestJobj,int exportType,int templateType) throws ServiceException, JSONException ;
     public JSONArray getCustomerAgedReceivable(JSONObject requestJObj,boolean isAgedReceivables,boolean detailReport) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    
}
