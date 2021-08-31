/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 */
package com.krawler.spring.compliance.indonesia;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

/**
 *
 * @author Rahul A. Bhawar - Indonesia Compliance
 */
public interface AccIndonesiaComplianceService {

    /**
     * Get VAT Out Report Data for INDONESIA country
     * Sales Invoice data details 
     * @param requestParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public JSONObject getVATOutReportData(JSONObject requestParams) throws ServiceException, JSONException;
}
