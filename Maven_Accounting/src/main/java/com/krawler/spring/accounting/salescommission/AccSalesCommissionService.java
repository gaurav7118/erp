/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.salescommission;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;

public interface AccSalesCommissionService {
    
    public JSONObject saveSalesCommissionSchemaMaster(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject getSalesCommissionSchemaMasters(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject getSalesCommissionRules(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject saveSalesCommissionMapping(Map<String, Object> requestParams) throws ServiceException;

    public JSONObject deleteSalesCommissionSchema(Map<String,Object> requestParams) throws ServiceException;
    
    public JSONObject deleteSalesCommissionRules(Map<String, Object> requestParams) throws ServiceException;
}
