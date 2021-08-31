/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.authHandler.authHandler;

import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;

public class WSServiceUtil implements MessageSourceAware {

    private static MessageSource messageSource;

    public static JSONObject getMessage(int type, int mode) throws JSONException {
        String r = "";
        String temp = "";
        switch (type) {
            case 1:     // success messages
                temp = "m" + String.format("%02d", mode);
                r = "{\"success\": true, \"infocode\": \"" + temp + "\"}";
                break;
            case 2:     // error messages
                temp = "e" + String.format("%02d", mode);

                r = "{\"success\": false, \"errorcode\": \"" + temp + "\"}";
                break;
        }
        return new JSONObject(r);
    }

    public static String getMessageStr(int type, int mode) {
        String r = "";
        String temp = "";
        switch (type) {
            case 1:     // success messages
                temp = "m" + String.format("%02d", mode);
                r = "{\"success\": true, \"infocode\": \"" + temp + "\"}";
                break;
            case 2:     // error messages
                temp = "e" + String.format("%02d", mode);

                r = "{\"success\": false, \"errorcode\": \"" + temp + "\"}";
                break;
        }
        return r;
    }

    public static void main(String[] args) {

        try {
            JSONObject j = new JSONObject();
            j.put("companyid", "8d28ccdb-c7f5-4b03-a984-3c93c3128b1c");
            j.put("contractid", "4028e4d34a4b2b87014a4b7d84630032");
            j.put("pid", "4028e4d34e3caba4014e3ee8aabe1fd7");
            j.put("doid", "4028e4d34e3caba4014e3f1bcf204cbc");
            System.out.println(j);
        } catch (JSONException ex) {
            Logger.getLogger(WSServiceUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public static String getGlobalFormattedDate(String originalDate, String originalFormat) throws SessionExpiredException, ServiceException{
        String formattedDate = null;
        try {
            DateFormat originalDF = new SimpleDateFormat(originalFormat);
            DateFormat targetDF = authHandler.getDateOnlyFormat();
            Date date = originalDF.parse(originalDate);
            formattedDate = targetDF.format(date);
            
        } catch (ParseException ex) {
            Logger.getLogger(WSServiceUtil.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return formattedDate;
    }
}
