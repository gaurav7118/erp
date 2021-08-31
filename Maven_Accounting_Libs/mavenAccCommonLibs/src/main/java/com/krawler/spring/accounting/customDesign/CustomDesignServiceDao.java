/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customDesign;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface CustomDesignServiceDao {

    public JSONObject getDesignTemplateList(JSONObject paramJobj);

    public JSONObject getDesignTemplate(JSONObject paramJobj);
    
    public JSONObject getJobOrderDesignTemplate(JSONObject paramJobj);
    
    public JSONObject getQAApprovalDesignTemplate(JSONObject paramJobj);

    public HashMap<String, String> getExtraFieldsForModule(int moduleid, int countryid, String templatesubtype);

    public HashMap<String, String> getdbCustomFieldColumns(int moduleid, String companyid, HashMap<String, String> extraCols) throws ServiceException;

    public JSONObject getCustomFieldsForSOA(String companyid, int moduleid);

    public JSONArray sortJsonArrayOnTransaction(JSONArray array) throws JSONException;

    public JSONObject deleteCustomTemplatemodule(JSONObject paramJObj) throws SecurityException, ServiceException, SessionExpiredException;

    public JSONObject getActiveDesignTemplateList(JSONObject paramJobj);

    public JSONObject copyTemplate(JSONObject paramJobj);

    public JSONObject saveDesignTemplate(JSONObject paramJObj) throws ServiceException;

    public JSONObject saveActiveModeTemplate(JSONObject paramJobj);

    public JSONObject createTemplate(JSONObject paramJobj);

    public JSONObject getFields(Map<String, Object> requestMap) throws ServiceException;

    public JSONArray getGroupingFields(JSONObject paramJobj);

    public JSONObject getLineFieldsData(JSONObject paramJobj) throws SessionExpiredException, ServiceException, JSONException;

    public JSONObject getGlobalFieldsData(JSONObject paramJobj) throws SessionExpiredException, ServiceException, JSONException;
    
    public JSONArray getDetailsTableFields(JSONObject paramJobj);
}
