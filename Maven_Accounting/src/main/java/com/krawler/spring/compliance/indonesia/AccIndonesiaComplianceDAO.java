/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 */
package com.krawler.spring.compliance.indonesia;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;

/**
 *
 * @author Rahul A. Bhawar - Indonesia Compliance
 */
public interface AccIndonesiaComplianceDAO {

    public List getSalesInvoiceListDataInSQL(JSONObject requestParams, JSONObject sectionExtraParams) throws ServiceException, JSONException;
}
