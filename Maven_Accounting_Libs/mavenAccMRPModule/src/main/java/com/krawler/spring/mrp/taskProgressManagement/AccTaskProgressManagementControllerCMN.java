/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.mrp.taskProgressManagement;

import com.krawler.common.util.Constants;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccTaskProgressManagementControllerCMN extends MultiActionController implements MessageSourceAware {
    
    private MessageSource messageSource;
    private String successView;
    private AccTaskProgressManagementServiceDAO accTaskProgressManagementServiceDAOObj;
    
    
    @Override
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

  
    public AccTaskProgressManagementServiceDAO getAccTaskProgressManagementServiceDAOObj() {
        return accTaskProgressManagementServiceDAOObj;
    }

    public void setAccTaskProgressManagementServiceDAOObj(AccTaskProgressManagementServiceDAO accTaskProgressManagementServiceDAOObj) {
        this.accTaskProgressManagementServiceDAOObj = accTaskProgressManagementServiceDAOObj;
    }
    
    
    /**
     * Description: This method is used to get Task Progress Details
     * @param request
     * @param response
     * @return JSONObject
     */
    
    public ModelAndView getTaskProgressDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get Task Progress Related Parameters*/
             Map<String, Object> requestParams = getTaskProgressCommonParameters(request);
             
             /*Get Task Progress Details*/
             
             jobj=accTaskProgressManagementServiceDAOObj.getTaskProgressDetails(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccTaskProgressManagementControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccTaskProgressManagementControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }
    
    
    /**
     * Description: This method is used to get Material Consumed Details
     * @param request
     * @param response
     * @return JSONObject
     */
    
    public ModelAndView getMaterialConsumedDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get Task Progress Related Parameters*/
             Map<String, Object> requestParams = getTaskProgressCommonParameters(request);
             
             /*Get Task Progress Details*/
             
             jobj=accTaskProgressManagementServiceDAOObj.getMaterialConsumedDetails(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccTaskProgressManagementControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccTaskProgressManagementControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }
    
    
    /**
     * Description : Used get Task Progress common parameters
     * @param <request> getting parameters from request object
     * @return Map
     */
    
    public Map<String, Object> getTaskProgressCommonParameters(HttpServletRequest request) {
        Map<String, Object> requestParams = new HashMap<>();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            requestParams.put("billid", request.getParameter("billid"));

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            requestParams.put("start", start);
            requestParams.put("limit", limit);

            requestParams.put("companyid", companyid);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            

        } catch (Exception ex) {
            Logger.getLogger(AccTaskProgressManagementControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }

   
    
}
