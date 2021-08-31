/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.labormanagement;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccLabourServiceDAO {

    public JSONObject saveLabour(HashMap<String, Object> requestParams)throws ServiceException;
    
    public JSONObject deleteLabours(HashMap<String, Object> requestParams) throws ServiceException;
            
    public void getColumnModelAndRecordDataForLabours(HashMap<String, Object> requestParams, JSONObject jSONObject) throws ServiceException;

    public void getColumnModelForAssignTaskList(HashMap<String, Object> requestParams, JSONObject jSONObject) throws ServiceException;

    public void getColumnModelForResourceList(HashMap<String, Object> requestParams, JSONObject jSONObject) throws ServiceException;

    public void getResolveConflictResourcesColumnModel(HashMap<String, Object> requestParams, JSONObject jSONObject) throws ServiceException;

    public JSONObject getSingleLabourToLoad(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONObject syncLabour(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject syncLabourCost(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONArray getLabourCombo(Map<String, Object> map) throws ServiceException ;
    
    public JSONObject saveLabourCost(Map<String, Object> requestParams) throws ServiceException;
    
    public void getResourceCostList(Map<String, Object> requestParams, JSONObject object) throws ServiceException;
    
    public JSONObject updateLabourFlag(Map <String,Object>map)throws ServiceException;
    
    public void deleteLaboursCost(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONObject exportLabourAllocationReportXlsx(Map<String, Object> requestParams) throws ServiceException;

    
}
