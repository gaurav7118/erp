/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.common;

import com.krawler.utils.json.base.JSONObject;

public interface KwlCommonBeanUtils {

    public JSONObject getErrorResponse(String errorCode, String errorMessage, String language);

    public String getErrorMessage(String errorCode, String errorMessage, String language);
}
