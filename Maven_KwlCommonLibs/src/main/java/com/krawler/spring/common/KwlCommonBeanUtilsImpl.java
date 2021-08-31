/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.common;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;

public class KwlCommonBeanUtilsImpl implements KwlCommonBeanUtils {

    private MessageSource messageSource;

    public void setmessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public JSONObject getErrorResponse(String errorCode, String errorMessage, String language) {
        JSONObject response = new JSONObject();
        try {

            if (errorCode != null && errorCode.equals(ServiceException.FAILURE)) {
                response.put(Constants.RES_MESSAGE, errorMessage);
            } else {
                if (StringUtil.isNullOrEmpty(language)) {
                    language = Constants.RES_DEF_LANGUAGE;
                }
                Object[] paramValues = null;
                if (errorCode != null && errorCode.contains("{") && errorCode.contains("}")) {
                    String paramValue = errorCode.substring(errorCode.indexOf("{") + 1, errorCode.indexOf("}"));
                    errorCode = errorCode.substring(0, errorCode.indexOf("{"));
                    List<String> params = new ArrayList<String>();
                    for (String param : paramValue.split(";")) {
                        params.add(param);
                    }
                    paramValues = params.toArray();
                }
                response.put(Constants.RES_MESSAGE, messageSource.getMessage(errorCode, paramValues, Locale.forLanguageTag(language)));
            }
            response.put(Constants.RES_success, false);
            response.put(Constants.RES_ERROR_CODE, errorCode);
        } catch (JSONException ex1) {
            Logger.getLogger(KwlCommonBeanUtilsImpl.class.getName()).log(Level.SEVERE, null, ex1);
        }
        return response;
    }

    @Override
    public String getErrorMessage(String errorCode, String errorMessage, String language) {
        String response = null;
        try {
            JSONObject errorResponse = getErrorResponse(errorCode, errorMessage, language);
            if (errorResponse != null && errorResponse.has(Constants.RES_MESSAGE)) {
                response = errorResponse.getString(Constants.RES_MESSAGE);
            }
        } catch (JSONException ex1) {
            Logger.getLogger(KwlCommonBeanUtilsImpl.class.getName()).log(Level.SEVERE, null, ex1);
        }
        return response;
    }

}
