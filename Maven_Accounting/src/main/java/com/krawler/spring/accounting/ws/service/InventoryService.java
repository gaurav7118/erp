/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

/**
 *
 * @author krawler
 */
public interface InventoryService {
    
    public JSONObject getSequenceFormats(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject getStoreList(JSONObject paramJobj) throws ServiceException, JSONException;

    public JSONObject getCycleCountList(JSONObject paramJobj) throws ServiceException, JSONException;

    public JSONObject getStoreLocations(JSONObject paramJobj) throws ServiceException, JSONException;
    
    public JSONObject validateCycleCountDate(JSONObject paramJobj) throws ServiceException, JSONException;
    
    public JSONObject saveCycleCount(JSONObject paramJobj) throws ServiceException, JSONException ;
    
    public JSONObject getCycleCountReport(JSONObject paramJobj) throws ServiceException, JSONException ;
    
    public JSONObject getStoreProductWiseAllAvailableStockDetailList(JSONObject paramJobj) throws ServiceException, JSONException;
    
      public JSONObject getUsersfromStore(JSONObject paramJobj) throws ServiceException, JSONException;
      
    //    public JSONObject getExtraItemList(JSONObject paramJobj) throws ServiceException, JSONException;
    
}
