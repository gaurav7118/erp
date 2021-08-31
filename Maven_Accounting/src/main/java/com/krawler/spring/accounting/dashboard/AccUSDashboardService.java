/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.dashboard;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

public interface AccUSDashboardService {

    public JSONObject saveDashboard(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
    public JSONObject getDashboard(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONObject setActiveDashboard(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
    public JSONObject getProductViewInvDetails(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
}
