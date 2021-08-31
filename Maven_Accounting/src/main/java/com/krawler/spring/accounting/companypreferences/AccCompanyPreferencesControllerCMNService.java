/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.companypreferences;

import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.YearLock;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.Map;

public interface AccCompanyPreferencesControllerCMNService {

    public JSONObject checkYearEndClosingCheckList(JSONObject requestJSON) throws SessionExpiredException, ParseException, JSONException, ServiceException;
    
    public boolean isSalesSideTransactionPresent(JSONObject requestJSON) throws SessionExpiredException, ParseException, JSONException, ServiceException;
    
    public boolean isPurchaseSideTransactionPresent(JSONObject requestJSON) throws SessionExpiredException, ParseException, JSONException, ServiceException;

    public void calculateAndStoreClosingAccountBalance(YearLock yearLock, JSONObject requestJSON, ExtraCompanyPreferences extraCompanyPreferences, CompanyAccountPreferences companyAccountPreferences) throws ServiceException, SessionExpiredException;
    
}
