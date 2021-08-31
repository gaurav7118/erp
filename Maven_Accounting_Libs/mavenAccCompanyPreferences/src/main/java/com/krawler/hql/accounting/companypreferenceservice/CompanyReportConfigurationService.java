/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting.companypreferenceservice;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface CompanyReportConfigurationService {

    public int showCustomFieldsDimensionInDescForGL(String companyid) throws ServiceException, JSONException;

    public JSONObject getTypeFormatByReportType(HashMap<String,Object> paramsMap) throws ServiceException, JSONException;

    public JSONObject pouplateSelectStatementForGL(JSONObject jobj, String userSessionId, String companyId) throws JSONException, ServiceException;

    public Map<String, String> getPropertiesForExport(String companyid, String reportType, String groupType, boolean configuredCustomData,boolean isExport,boolean iSFromExpander) throws JSONException, ServiceException;

    public void saveGLConfiguration(JSONObject paramObj, String userSessionId, String companyId) throws ServiceException, JSONException;
    
    public JSONObject getExportConfigData(String companyid, String reportType, boolean onlyVisible,boolean isFromExpander, Map params) throws ServiceException, JSONException;
    
    public JSONObject pouplateSelectStatementForSOA(JSONObject jobj, String companyid) throws JSONException, ServiceException ;    

    public JSONObject pouplateSelectStatementForAR(JSONObject jobj, String companyid) throws JSONException, ServiceException ;    
}
