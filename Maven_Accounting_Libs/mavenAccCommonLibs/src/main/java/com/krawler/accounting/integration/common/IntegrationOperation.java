/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.common;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

/**
 *
 * @author krawler
 */
public interface IntegrationOperation {
    
    public JSONObject executeOperation (JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException, ServiceException, AccountingException;
    
}
