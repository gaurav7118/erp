/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.defaultfieldsetup;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccFieldSetupServiceDao {
    
    public KwlReturnObject saveMobileFieldsConfigSettings(JSONObject paramJobj)  throws ServiceException,JSONException;
    
    public JSONArray getMobileFieldsConfig(JSONObject paramJobj);
    
    public JSONArray getBatchSerialsFieldsJsonArray(JSONArray ColumnConfigArr, String moduleid,JSONObject paramJobj) throws ServiceException ;
    
    public Map getMandatoryFieldsDetails(String moduleid, String vendordid, String companyid, boolean isrecordflag, JSONObject paramJObj) throws ServiceException, JSONException;
    
}
