/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.account;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface accAccountModuleService {
    
    public JSONObject getGroups(HttpServletRequest request, boolean isForExport) throws ServiceException, SessionExpiredException;
    
    public JSONObject insertFieldsForAvalara (JSONObject requestJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException;
    
    public JSONObject getLinkedTermTax(HashMap<String, Object>  requestParams) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONObject setInvoiceTermsSalesActive(JSONObject paramJObj)throws ServiceException; 
    
    public JSONObject saveBankAccountMappingDetails(JSONObject paramJObj)throws ServiceException; 
    
    public JSONObject getBankAccountMappingDetails(JSONObject paramJObj)throws ServiceException;
    
    public  JSONObject getBankAccountMappingGridInfo(JSONObject paramJObj) throws JSONException, ServiceException;
}
