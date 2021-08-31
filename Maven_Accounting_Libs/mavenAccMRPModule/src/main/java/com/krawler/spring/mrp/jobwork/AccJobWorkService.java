/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.jobwork;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.Map;
import net.sf.jasperreports.engine.JasperPrint;

/**
 *
 * @author krawler
 */
public interface AccJobWorkService {

    JSONObject saveJobWork(Map<String, Object> dataMap) throws ServiceException;
    
    JSONObject saveForecastTemplate(Map<String, Object> dataMap) throws ServiceException;
    
    public void getColumnModelAndRecordDataForForecast(Map<String, Object> requestParams, JSONObject jSONObject) throws ServiceException;
    
    public void getForecastDetails(Map<String, Object> requestParams, JSONObject object) throws ServiceException;

    public void getChallanReport(Map<String, Object> requestParams, JSONObject object) throws ServiceException;

    public void getJWProductSummaryReport(Map<String, Object> requestParams, JSONObject object) throws ServiceException;

    public JSONObject getColumnModelForJobOrderReport(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteJobWork(Map<String, Object> dataMap) throws ServiceException;

    public boolean isJobWorkIDAlreadyPresent(Map<String, Object> dataMap) throws ServiceException;

    public JSONObject getWorkOrdersForCombo(Map<String, Object> requestParms)throws ServiceException;
    
    public JSONObject getSingleForecastToLoad(Map<String, Object> requestParams)throws ServiceException;
    
    public void deleteForecast(Map<String, Object> requestParams) throws ServiceException;

//    public JasperPrint exportChallanJasperReport(JSONObject request) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    
    public  Map<String, Object>  exportJWProductSummaryjasperreport(Map<String, Object> requestParams,JSONObject request) throws ServiceException, JSONException, SessionExpiredException, ParseException;

}
