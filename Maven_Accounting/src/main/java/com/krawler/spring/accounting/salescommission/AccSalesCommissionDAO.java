/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.salescommission;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;

public interface AccSalesCommissionDAO {
    
    public KwlReturnObject saveSalesCommissionSchemaMaster(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getSalesCommissionSchemaMasters(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveSalesCommissionRules(JSONObject jObj) throws ServiceException;
    
    public KwlReturnObject saveSalesCommissionRuleConditions(JSONObject jObj) throws ServiceException;

    public KwlReturnObject getSalesCommissionRules(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveSalesCommissionMapping(String schemaMasterId,String masterItem) throws ServiceException;
    
    public KwlReturnObject deleteSalesCommissionSchema(Map<String,Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteSalesCommissionRules(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCommossionSchemaTaggedToDimension(Map<String, Object> requestparams) throws ServiceException;

}
