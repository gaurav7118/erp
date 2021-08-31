/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.common.util;

import com.krawler.esp.utils.ConfigReader;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;

public class URLUtil {

    public static String baseForumURL = com.krawler.esp.utils.ConfigReader.getinstance().get("forum_base_url");
    private static String baseURLFormat = com.krawler.esp.utils.ConfigReader.getinstance().get("base_urlformat");
    private static String baseURLFormatUnprotected = com.krawler.esp.utils.ConfigReader.getinstance().get("base_urlformat_unprotected");
    private static String rest = "rest/";

    public static String getPageURL(HttpServletRequest request, String pagePathFormat) {
        String subdomain = request.getParameter("cdomain");
        String path = HttpUtils.getRequestURL(request).toString();
        String servPath = request.getServletPath();
        String uri = path.replace(servPath, "/" + String.format(pagePathFormat, subdomain));
        return uri;
    }
    
    public static String getPageURLJson(Map requestParams, String pagePathFormat) {
        String subdomain = (String) requestParams.get("cdomain");
        String path = (String) requestParams.get("path");
        String servPath = (String) requestParams.get("servPath");
        String uri = path.replace(servPath, "/" + String.format(pagePathFormat, subdomain));
        return uri;
    }
    public static String getPageURL(HttpServletRequest request, String pagePathFormat, String subdomain) {
        String path = HttpUtils.getRequestURL(request).toString();
        String servPath = request.getServletPath();
        String uri = path.replace(servPath, "/" + String.format(pagePathFormat, subdomain));
        return uri;
    }

    public static String prepareURL(HttpServletRequest request, String baseURL, String pagePathFormat) {
        String subdomain = request.getParameter("cdomain");
        String uri = baseURL + String.format(pagePathFormat, subdomain);
        return uri;
    }

    public static boolean checkValidURL(String subdomain,
            HttpServletRequest request) {
        String reqSubdomain = getDomainName(request);
        boolean isMatch = reqSubdomain.equalsIgnoreCase(subdomain);
        return isMatch;
    }

    public static String getDomainURL(String replaceStr, boolean Protected) {
        String uri = String.format(baseURLFormatUnprotected, replaceStr);
        if (Protected) {
            uri = String.format(baseURLFormat, replaceStr);
        }
        return uri;
    }

    public static String getDomainName(HttpServletRequest request) {
        String companyName = request.getParameter("cdomain");
        return companyName;
    }
    
    public static String buildRestURL(String application) {
        String accRestURL = ConfigReader.getinstance().get(application);
        if (accRestURL != null) {
            accRestURL += rest;
            String restVersion = ConfigReader.getinstance().get(application+Constants.REST_VERSION);
            if(!StringUtil.isNullOrEmpty(restVersion)){
                accRestURL += restVersion+ "/";
            }
        }
        return accRestURL;
    }
}