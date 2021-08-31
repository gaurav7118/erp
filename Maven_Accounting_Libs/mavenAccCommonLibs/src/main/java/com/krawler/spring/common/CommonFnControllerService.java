/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.common;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface CommonFnControllerService {

    public boolean checkCompanyAndUserTimezone(JSONObject paramJObj) throws SessionExpiredException, ServiceException;

    public HashMap<String, Object> generateMap(HashMap requestMap) throws UnsupportedEncodingException;
    
    public ArrayList<String> getUserApprovalEmail(HashMap requestMap);
    
    public String getApprovalstatusmsg(HashMap requestMap);

    public HashMap<String, Object> generateMapJSON(JSONObject paramJObj) throws UnsupportedEncodingException, JSONException;

    public JSONObject saveUsers(HashMap<String, Object> requestMap, JSONObject paramJObj,HashMap hm );
    
    public JSONObject getModuleTemplate(JSONObject paramJobj);
}
