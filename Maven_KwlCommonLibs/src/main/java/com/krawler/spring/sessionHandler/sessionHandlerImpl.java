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
package com.krawler.spring.sessionHandler;

import com.googlecode.cqengine.IndexedCollection;
import com.krawler.acc.dm.ExchangeRateDetailInfo;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author karthik
 */
public class sessionHandlerImpl implements java.io.Serializable {
    
    private sessionHandlerImpl sessionHandlerImplObj;
    private static MessageSource messageSource;

    public sessionHandlerImpl() {
    }

     public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public static boolean isValidSession(HttpServletRequest request,
            HttpServletResponse response) {
        boolean bSuccess = false;
        try {
            String subdomain = URLUtil.getDomainName(request);
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
                if (companySessionObj.getInitialized() != null) {
                    bSuccess = true;
                }
            } else {
                if (request.getSession().getAttribute("initialized") != null) {
                    bSuccess = true;
                }
            }
        } catch (Exception ex) {
        }
        return bSuccess;
    }

    public static void updatePreferences(HttpServletRequest request,
            String currencyid, String dateformatid, String timezoneid,
            String tzdiff) throws SessionExpiredException {
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (currencyid != null) {
                companySessionObj.setCurrencyid(currencyid);
            }
            if (timezoneid != null) {
                companySessionObj.setTimezoneid(timezoneid);
                companySessionObj.setTzdiff(tzdiff);
            }
            if (dateformatid != null) {
                companySessionObj.setDateformatid(dateformatid);
            }
        } else {
            if (currencyid != null) {
                request.getSession().setAttribute("currencyid", currencyid);
            }
            if (timezoneid != null) {
                request.getSession().setAttribute("timezoneid", timezoneid);
                request.getSession().setAttribute("tzdiff", tzdiff);
            }
            if (dateformatid != null) {
                request.getSession().setAttribute("dateformatid", dateformatid);
            }
        }
    }

    /*
     * Update date preference only.
     */
    public static void updateDatePreferences(HttpServletRequest request, String dateformatid) throws SessionExpiredException {
        if (dateformatid != null) {
            String subdomain = URLUtil.getDomainName(request);
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
                companySessionObj.setDateformatid(dateformatid);
            } else {
                request.getSession().setAttribute("dateformatid", dateformatid);
            }
        }
    }

    /*
     * Time Format included here.
     */
    public static void updatePreferences(HttpServletRequest request,
            String currencyid, String dateformatid, String timezoneid,
            String tzdiff, String timeformat) throws SessionExpiredException {
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (currencyid != null) {
                companySessionObj.setCurrencyid(currencyid);
            }
            if (timezoneid != null) {
                companySessionObj.setTimezoneid(timezoneid);
                companySessionObj.setTzdiff(tzdiff);
            }
            if (dateformatid != null) {
                companySessionObj.setDateformatid(dateformatid);
            }
            if (timeformat != null) {
                companySessionObj.setTimeformat(timeformat);
            }
        } else {
            if (currencyid != null) {
                request.getSession().setAttribute("currencyid", currencyid);
            }
            if (timezoneid != null) {
                request.getSession().setAttribute("timezoneid", timezoneid);
                request.getSession().setAttribute("tzdiff", tzdiff);
            }
            if (dateformatid != null) {
                request.getSession().setAttribute("dateformatid", dateformatid);
            }
            if (timeformat != null) {
                request.getSession().setAttribute("timeformat", timeformat);
            }
        }
    }

    public static void updatePaymentMethodID(HttpServletRequest request, String paymentmethodid) throws SessionExpiredException {
        if (paymentmethodid != null) {
            String subdomain = URLUtil.getDomainName(request);
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
                companySessionObj.setPaymentmethodid(paymentmethodid);
            } else {
                request.getSession().setAttribute("methodid", paymentmethodid);
            }
        }
    }
    
     /**
     * Description: This Method is used to Update Payment method id.
     * @param paramJobj
     * @param paymentmethodid
     */
    public void  updatePaymentMethodIDForPayment(JSONObject paramJobj,String paymentmethodid) throws SessionExpiredException {
        if (paymentmethodid != null) {
            String subdomain = paramJobj.optString(Constants.COMPANY_PARAM);
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                CompanySessionClass companySessionObj = getCompanySessionObj();
                companySessionObj.setPaymentmethodid(paymentmethodid);
            } 
        }
    }

//    public static void updateExchangeRateDetails(HttpServletRequest request, IndexedCollection<ExchangeRateDetailInfo> exchangeRateDetailInfo) throws SessionExpiredException {
//        if (exchangeRateDetailInfo != null) {
//            String subdomain = URLUtil.getDomainName(request);
//            if (!StringUtil.isNullOrEmpty(subdomain)) {
//                CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
//                companySessionObj.setExchangeRateDetails(exchangeRateDetailInfo);
//            } else {
//                request.getSession().setAttribute("exchangeratedetailinfo", exchangeRateDetailInfo);
//            }
//        }
//    }

    public static void updateCurrencyID(HttpServletRequest request, String currencyid) throws SessionExpiredException {
        if (currencyid != null) {
            String subdomain = URLUtil.getDomainName(request);
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
                companySessionObj.setCurrencyid(currencyid);
            } else {
                request.getSession().setAttribute("currencyid", currencyid);
            }
        }
    }

    public static String getPaymentMethodID(HttpServletRequest request)
            throws SessionExpiredException {
        String userName = null;
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getPaymentmethodid())) {
                userName = companySessionObj.getPaymentmethodid();
            }
            return userName;
        } else {
            if (request.getSession().getAttribute("methodid") != null) {
                userName = request.getSession().getAttribute("methodid").toString();
            }
            return userName;
        }
    }

    public boolean validateSession(HttpServletRequest request,
            HttpServletResponse response) {
        return sessionHandlerImpl.isValidSession(request, response);
    }

    public boolean validateSessionFromRequest(HttpServletRequest request,
            HttpServletResponse response) {
        boolean bSuccess = false;
        try {
            if (request.getSession().getAttribute("initialized") != null) {
                bSuccess = true;
            }
        } catch (Exception ex) {
        }
        return bSuccess;
    }

    public void createUserSession(HttpServletRequest request, JSONObject jObj) throws ServiceException {
        HttpSession session = request.getSession(true);
        try {
            String subdomain = URLUtil.getDomainName(request);
            RequestAttributes attribs = getAttributeHolder();
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                JSONArray jarr = jObj.getJSONArray("perms");
                CompanySessionClass companySessionObj = new CompanySessionClass();
                companySessionObj.setUsername(jObj.getString("username"));
                companySessionObj.setUserid(jObj.getString("lid"));
                companySessionObj.setCompanyid(jObj.getString("companyid"));
                companySessionObj.setCompany(jObj.getString("company"));
                companySessionObj.setTimezoneid(jObj.getString("timezoneid"));
                companySessionObj.setTzdiff(jObj.getString("tzdiff"));
                companySessionObj.setCompanyTZDiff(jObj.getString("tzdiff"));
                companySessionObj.setDateformatid(jObj.getString("dateformatid"));
                companySessionObj.setCurrencyid(jObj.getString("currencyid"));
                companySessionObj.setCallwith(jObj.getString("callwith"));
                companySessionObj.setTimeformat(jObj.getString("timeformat"));
                companySessionObj.setCdomain(subdomain);
                companySessionObj.setCompanyPreferences(jObj.getString("companyPreferences"));
                companySessionObj.setRoleid(jObj.getString("roleid"));
                companySessionObj.setInitialized("true");
                companySessionObj.setUserdateformat(jObj.getString("userdateformat"));
                companySessionObj.setUserfullname(jObj.getString("userfullname"));
                companySessionObj.setUserEmailid(jObj.getString("usermailid"));
                companySessionObj.setUserSessionId(jObj.getString(Constants.userSessionId));
                companySessionObj.setCountryId(jObj.optString("countryid"));
//                companySessionObj.setPerms(jarr);

                TreeMap<String,Long> permissions= new TreeMap<String,Long>();
                for (int l = 0; l < jarr.length(); l++) {
                    String keyName = jarr.getJSONObject(l).names().get(0).toString();
                    Long permCode = (Long)jarr.getJSONObject(l).get(keyName);
                    permissions.put(keyName, permCode);
                }
                companySessionObj.setPermissions(permissions);
                attribs.setAttribute(subdomain, companySessionObj, RequestAttributes.SCOPE_SESSION);
                session.setAttribute(subdomain, companySessionObj);
                session.setAttribute("companyid", jObj.getString("companyid"));
                session.setAttribute("countryid", jObj.optString("countryid"));
                session.setAttribute("username", jObj.getString("username"));
                attribs.setAttribute("cdomain", subdomain, RequestAttributes.SCOPE_SESSION);
                session.setAttribute("cdomain", subdomain);

                attribs.setAttribute("initialized", "true", RequestAttributes.SCOPE_SESSION);
                session.setAttribute("initialized", "true");
            } else {
                session.setAttribute("username", jObj.getString("username"));
                session.setAttribute("userid", jObj.getString("lid"));
                session.setAttribute("companyid", jObj.getString("companyid"));
                session.setAttribute("company", jObj.getString("company"));
                session.setAttribute("timezoneid", jObj.getString("timezoneid"));
                session.setAttribute("tzdiff", jObj.getString("tzdiff"));
                session.setAttribute("dateformatid", jObj.getString("dateformatid"));
                session.setAttribute("currencyid", jObj.getString("currencyid"));
                session.setAttribute("callwith", jObj.getString("callwith"));
                session.setAttribute("timeformat", jObj.getString("timeformat"));
                session.setAttribute("companyPreferences", jObj.getString("companyPreferences"));
                session.setAttribute("roleid", jObj.getString("roleid"));
                session.setAttribute("initialized", "true");
                session.setAttribute("userfullname", jObj.getString("userfullname"));
                session.setAttribute("usermailid", jObj.getString("usermailid"));
                session.setAttribute("userdateformat", jObj.getString("userdateformat"));
                session.setAttribute("countryid", jObj.optString("countryid"));
                JSONArray jarr = jObj.getJSONArray("perms");
                for (int l = 0; l < jarr.length(); l++) {
                    String keyName = jarr.getJSONObject(l).names().get(0).toString();
                    session.setAttribute(keyName, jarr.getJSONObject(l).get(keyName));
                }
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("sessionHandlerImpl.createUserSession", e);
        }
    }

    public void destroyUserSession(HttpServletRequest request,
            HttpServletResponse response) {
        request.getSession().invalidate();
    }

    public static CompanySessionClass getCompanySessionObj(HttpServletRequest request)
            throws SessionExpiredException {
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            Object objToCheck = request.getSession().getAttribute(subdomain);
            if (objToCheck != null) {
                CompanySessionClass companySessionObj = (CompanySessionClass) objToCheck;
                return companySessionObj;
            }
        }
        throw new SessionExpiredException(messageSource.getMessage("acc.common.sessioninvalidated",null, RequestContextUtils.getLocale(request)), SessionExpiredException.COMPANYOBJECT_NULL);
    }

    public CompanySessionClass getCompanySessionObj()
            throws SessionExpiredException {
        String subdomain = NullCheckAndThrow(getAttributeHolder().getAttribute(
                "cdomain", RequestAttributes.SCOPE_SESSION), SessionExpiredException.COMPANYOBJECT_NULL);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            Object objToCheck = NullCheckAndThrowObject(getAttributeHolder().getAttribute(
                    subdomain, RequestAttributes.SCOPE_SESSION), SessionExpiredException.COMPANYOBJECT_NULL);
            if (objToCheck != null) {
                CompanySessionClass companySessionObj = (CompanySessionClass) objToCheck;
                return companySessionObj;
            }
        }
        throw new SessionExpiredException("Session Invalidated", SessionExpiredException.COMPANYOBJECT_NULL);
    }

//     public static Object getInitialized(HttpServletRequest request)
//            throws SessionExpiredException {
//        Object userId = null;
//        String subdomain = URLUtil.getDomainName(request);
//        if(!StringUtil.isNullOrEmpty(subdomain)) {
//            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
//            if(!StringUtil.isNullOrEmpty(companySessionObj.getInitialized())) {
//                userId = companySessionObj.getInitialized();
//            }
//        } else {
//            userId = request.getSession().getAttribute("initialized");
//        }
//        return userId;
//    }
    
    public RequestAttributes getAttributeHolder() {
        return RequestContextHolder.getRequestAttributes();
    }

    public static String getUserid(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getUserid())) {
                userId = companySessionObj.getUserid();
                return userId;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userId = NullCheckAndThrow(request.getSession().getAttribute(
                    "userid"), SessionExpiredException.USERID_NULL);
            return userId;
        }
    }

    public static String getBrowserTZ(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getBrowsertz())) {
                userId = companySessionObj.getBrowsertz();
                return userId;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userId = NullCheckAndThrow(request.getSession().getAttribute(
                    "userid"), SessionExpiredException.USERID_NULL);
            return userId;
        }
    }

//    public IndexedCollection<ExchangeRateDetailInfo> getExchangeRateDetailInfo()
//            throws SessionExpiredException {
//        CompanySessionClass companySessionObj = getCompanySessionObj();
//        if (companySessionObj.getExchangeRateDetails()!=null) {
//            return companySessionObj.getExchangeRateDetails();
//        }
//        throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
//    }

    public String getUserid()
            throws SessionExpiredException {
        String userId = "";
        CompanySessionClass companySessionObj = getCompanySessionObj();
        if (!StringUtil.isNullOrEmpty(companySessionObj.getUserid())) {
            userId = companySessionObj.getUserid();
            return userId;
        }
        throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
    }

    public static String getTimeZoneID(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getTimezoneid())) {
                userId = companySessionObj.getTimezoneid();
                return userId;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userId = NullCheckAndThrow(request.getSession().getAttribute(
                    "timezoneid"), SessionExpiredException.USERID_NULL);
            return userId;
        }
    }

    public static String getTimeZoneDifference(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getTzdiff())) {
                userId = companySessionObj.getTzdiff();
                return userId;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userId = NullCheckAndThrow(request.getSession().getAttribute(
                    "tzdiff"), SessionExpiredException.USERID_NULL);
            return userId;
        }
    }

    public static String getCompanyTZDiff(HttpServletRequest request) throws SessionExpiredException {
        String companyTZDiff = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getCompanyTZDiff())) {
                companyTZDiff = companySessionObj.getCompanyTZDiff();   //Company Date Formatter
                return companyTZDiff;
            } else {
                companyTZDiff = companySessionObj.getTzdiff();  //Current User Date Formatter
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            companyTZDiff = NullCheckAndThrow(request.getSession().getAttribute(
                    "company"), SessionExpiredException.USERID_NULL);
            return companyTZDiff;
        }
    }

    public static String getUserCallWith(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getCallwith())) {
                userId = companySessionObj.getCallwith();
                return userId;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userId = NullCheckAndThrow(request.getSession().getAttribute(
                    "callwith"), SessionExpiredException.USERID_NULL);
            return userId;
        }
    }

    public static String getUserTimeFormat(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getTimeformat())) {
                userId = companySessionObj.getTimeformat();
                return userId;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userId = NullCheckAndThrow(request.getSession().getAttribute(
                    "timeformat"), SessionExpiredException.USERID_NULL);
            return userId;
        }
    }

    public static String getUserName(HttpServletRequest request)
            throws SessionExpiredException {
        String userName = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getUsername())) {
                userName = companySessionObj.getUsername();
                return userName;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userName = NullCheckAndThrow(request.getSession().getAttribute(
                    "username"), SessionExpiredException.USERNAME_NULL);
            return userName;
        }
    }
    public static String getUserMailId(HttpServletRequest request)
            throws SessionExpiredException {
        String userEmail = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getUserEmailid())) {
                userEmail = companySessionObj.getUserEmailid();
                return userEmail;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userEmail = NullCheckAndThrow(request.getSession().getAttribute(
                    "userEmail"), SessionExpiredException.USERNAME_NULL);
            return userEmail;
        }
    }

    public static String getUserNameFromRequest(HttpServletRequest request)
            throws SessionExpiredException {
        String userName = "";
        userName = NullCheckAndThrow(request.getSession().getAttribute(
                "username"), SessionExpiredException.USERNAME_NULL);
        return userName;
    }

    public static String getUserDateFormat(HttpServletRequest request)
            throws SessionExpiredException {
        String userdateformat = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getUserdateformat())) {
                userdateformat = companySessionObj.getUserdateformat();
                return userdateformat;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userdateformat = NullCheckAndThrow(request.getSession().getAttribute(
                    "userdateformat"), SessionExpiredException.USERFULLNAME_NULL);
            return userdateformat;
        }
    }

    public static String getUserFullName(HttpServletRequest request)
            throws SessionExpiredException {
        String userfullname = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getUserfullname())) {
                userfullname = companySessionObj.getUserfullname();
                return userfullname;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userfullname = NullCheckAndThrow(request.getSession().getAttribute(
                    "userfullname"), SessionExpiredException.USERFULLNAME_NULL);
            return userfullname;
        }
    }

    public static String getRole(HttpServletRequest request)
            throws SessionExpiredException {
        String roleid = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getRoleid())) {
                roleid = companySessionObj.getRoleid();
                return roleid;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            roleid = NullCheckAndThrow(request.getSession().getAttribute(
                    "roleid"), SessionExpiredException.USERID_NULL);
            return roleid;
        }
    }

    public static String getDateFormatID(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getDateformatid())) {
                userId = companySessionObj.getDateformatid();
                return userId;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userId = NullCheckAndThrow(request.getSession().getAttribute(
                    "dateformatid"), SessionExpiredException.USERID_NULL);
            return userId;
        }
    }

    public static String getCompanyid(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getCompanyid())) {
                userId = companySessionObj.getCompanyid();
                return userId;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userId = NullCheckAndThrow(request.getSession().getAttribute(
                    "companyid"), SessionExpiredException.USERID_NULL);
            return userId;
        }
    }
    
    public static String getCountryId(HttpServletRequest request)
            throws SessionExpiredException {
        String countryId = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getCountryId())) {
                countryId = companySessionObj.getCountryId();
                return countryId;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            countryId = NullCheckAndThrow(request.getSession().getAttribute(
                    "countryid"), SessionExpiredException.USERID_NULL);
            return countryId;
        }
    }

    public static String getCompanyidFromRequest(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = "";
        userId = NullCheckAndThrow(request.getSession().getAttribute(
                "companyid"), SessionExpiredException.USERID_NULL);
        return userId;
    }

    public static String getCompanyName(HttpServletRequest request)
            throws SessionExpiredException {
        String userName = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getCompany())) {
                userName = companySessionObj.getCompany();
                return userName;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userName = NullCheckAndThrow(request.getSession().getAttribute(
                    "company"), SessionExpiredException.USERNAME_NULL);
            return userName;
        }
    }

    public static String getCurrencyID(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = "";
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (!StringUtil.isNullOrEmpty(companySessionObj.getCurrencyid())) {
                userId = companySessionObj.getCurrencyid();
                return userId;
            }
            throw new SessionExpiredException("Session Invalidated", SessionExpiredException.USERID_NULL);
        } else {
            userId = NullCheckAndThrow(request.getSession().getAttribute(
                    "currencyid"), SessionExpiredException.USERID_NULL);
            return userId;
        }
    }

//    public Integer getPerms(HttpServletRequest request, String keyName)
//            throws SessionExpiredException {
//        long perl = 0;
//        int per = 0;
//        try {
//            if (request.getSession().getAttribute(keyName) != null) {
//                perl = (Long) request.getSession().getAttribute(keyName);
//            }
//            per = (int) perl;
//        } catch (Exception e) {
//            per = 0;
//        }
//        return per;
//    }
    public static String NullCheckAndThrow(Object objToCheck, String errorCode)
            throws SessionExpiredException {
        if (objToCheck != null) {
            String oStr = objToCheck.toString();
            if (!StringUtil.isNullOrEmpty(oStr)) {
                return oStr;
            }
        }
        throw new SessionExpiredException("Session Invalidated", errorCode);
    }

    public static Object NullCheckAndThrowObject(Object objToCheck, String errorCode)
            throws SessionExpiredException {
        if (objToCheck != null) {
            return objToCheck;
        }
        throw new SessionExpiredException("Session Invalidated", errorCode);
    }
    public static Integer getPerms(HttpServletRequest request, String keyName) throws SessionExpiredException {
        long perl = 0;
        int per = 0;
        try {
            String subdomain = URLUtil.getDomainName(request);
            if ((!StringUtil.isNullOrEmpty(subdomain))) {
                CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
                if (!StringUtil.isNullOrEmpty(companySessionObj.getPermissions().get(keyName).toString())) {
                    perl = (Long) companySessionObj.getPermissions().get(keyName);
                }
            } else {
                if (request.getSession().getAttribute(keyName) != null) {
                    perl = (Long) request.getSession().getAttribute(keyName);
                }
            }
            per = (int) perl;
        } catch (Exception e) {
            per = 0;
        }
        return per;
    }

    public static DateFormat getDateFormatterWithUserTimeFormat(HttpServletRequest request)
            throws SessionExpiredException {
        String dateformat = "";
        String timeformat = getUserTimeFormat(request);
        if (timeformat.equals("1")) {
            dateformat = "MMMM d, yyyy hh:mm:ss aa";
        } else {
            dateformat = "MMMM d, yyyy HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + getTimeZoneDifference(request)));
        return sdf;
    }

    public static DateFormat getPrefDateFormatter(HttpServletRequest request, String pref)
            throws SessionExpiredException {
        String dateformat = "";
        String timeformat = getUserTimeFormat(request);
        if (timeformat.equals("1")) {
            dateformat = pref.replace('H', 'h');
            if (!dateformat.equals(pref)) {
                dateformat += " a";
            }
        } else {
            dateformat = pref;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + getTimeZoneDifference(request)));
        return sdf;
    }
}
