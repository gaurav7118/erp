/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.handler;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author krawler
 */
public interface AccDashboardService {
    public String getDashboardData(HttpServletRequest request) throws ServiceException, SessionExpiredException;
    public String getDashboardUpdateDataIphone(HttpServletRequest request) throws ServiceException, SessionExpiredException;
    public String getDashboardUpdates(HttpServletRequest request, HttpServletResponse response) throws ServiceException;
    public boolean checkCompanyAndUserTimezone(HttpServletRequest request, HttpServletResponse response) throws  SessionExpiredException, ServiceException;
    public ModelAndView getDashboardLinks(HttpServletRequest request, HttpServletResponse response);
    public ModelAndView getMaintainanceDetails(HttpServletRequest request, HttpServletResponse response);
    public JSONObject getPendingApprovalsForAllModulesJson(HashMap<String, Object> requestParams) throws ServiceException,JSONException;
    public void setMessageSource(MessageSource msg);
    public boolean saveUserPreferencesOptions(JSONObject paramObj) throws ServiceException,JSONException;
    public String getDashboardWelcomeMsg(HttpServletRequest request, int PendingApprovalCount);
}
