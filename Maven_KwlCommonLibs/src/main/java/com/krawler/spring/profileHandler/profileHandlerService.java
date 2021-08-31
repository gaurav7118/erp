/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.profileHandler;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import javax.servlet.ServletException;

/**
 *
 * @author krawler
 */
public interface profileHandlerService {
    
    public JSONObject getAllUserDetails(JSONObject paramJobj)throws ServletException ;
    public JSONObject getUserDetailsFromApps(JSONObject paramJobj)throws ServletException ;
    public JSONObject getPasswordPolicy(JSONObject requestJobj) throws ServiceException, JSONException ;
    public JSONObject saveOrUpdatePasswordPolicy(JSONObject paramJobj)throws ServiceException, JSONException ;
}
