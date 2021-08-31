/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.dashboard;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

public interface AccUSDashboardDAO {
    
    public KwlReturnObject saveDashboard(JSONObject dataObj) throws ServiceException,SessionExpiredException;
    
    public KwlReturnObject getDashboard(JSONObject dataObj) throws ServiceException, SessionExpiredException;
    
    public KwlReturnObject setActiveDashboard(JSONObject dataObj) throws ServiceException, SessionExpiredException;

}
