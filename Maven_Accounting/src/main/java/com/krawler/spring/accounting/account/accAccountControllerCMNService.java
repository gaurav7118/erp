/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.account;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;


public interface accAccountControllerCMNService {
    
    public JSONArray saveCustomerDataFromLMS(HashMap<String, Object> params, JSONObject resObj) throws ServiceException;
    
    public void saveOrUpdateAddressFieldForGSTDimension(HashMap<String, Object> paramMap) throws ServiceException, JSONException;
    
    public JSONArray getTaxJson(HttpServletRequest request) throws SessionExpiredException, ServiceException;
    
    public void  syncCustomFieldDataFromOtherProjects(HashMap<String, Object> requestParams) throws SessionExpiredException,ServiceException;
    
    public void  getLMSCourcesAsProducts(HashMap<String, Object> requestParams) throws SessionExpiredException,ServiceException;
    
    public void getCustomerFromLMS(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONObject deleteAccount(JSONObject requestJobj) throws JSONException, ServiceException, AccountingException;
    
}   

