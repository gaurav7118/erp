/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.common;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
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
public class IntegrationController extends MultiActionController implements MessageSourceAware {

    private IntegrationCommonService integrationCommonService;
    private MessageSource messageSource;

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setIntegrationCommonService(IntegrationCommonService integrationCommonService) {
        this.integrationCommonService = integrationCommonService;
    }

    /**
     * To save or update integration account credentials and config into
     * database
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView saveOrUpdateIntegrationAccountDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
                JSONObject saveDtailsJobj = integrationCommonService.saveOrUpdateIntegrationAccountDetails(requestJobj);
                jobj.put(Constants.RES_data, saveDtailsJobj);
                jobj.put(Constants.RES_success, saveDtailsJobj.optBoolean(Constants.RES_success, false));
                jobj.put(Constants.RES_msg, saveDtailsJobj.optString(Constants.RES_msg));
            } else {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, Constants.RES_timeout);
            }
        } catch (JSONException | ServiceException | SessionExpiredException ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, messageSource.getMessage("acc.field.Erroroccurredwhileprocessing", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    /**
     * To fetch integration account credentials and config from database
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getIntegrationAccountDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
                JSONObject accountDetailsJobj = integrationCommonService.getIntegrationAccountDetails(requestJobj);
                jobj.put(Constants.RES_data, accountDetailsJobj);
                jobj.put(Constants.RES_success, true);
                jobj.put(Constants.RES_msg, Constants.RES_success);
            } else {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, Constants.RES_timeout);
            }
        } catch (JSONException | ServiceException | SessionExpiredException ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, messageSource.getMessage("acc.field.Erroroccurredwhileprocessing", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    /*
     *  Following method validates the Integration Account's Credentials by sending a GET type request to AvaTax REST service which fetches the account details for an accountNumber
     */
    public ModelAndView validateCredentials(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
                requestJobj.put(IntegrationConstants.skipRequestJsonProcessing, true);//This flag is used to check whether processing of data is required or not
                JSONObject responseJobj = integrationCommonService.processIntegrationRequest(requestJobj);
                jobj.put(Constants.RES_data, responseJobj);
                jobj.put(Constants.RES_success, responseJobj.optBoolean(Constants.RES_success, false));
                jobj.put(Constants.RES_msg, responseJobj.optString(Constants.RES_msg));
            } else {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, Constants.RES_timeout);
            }
        } catch (AccountingException ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, ex.getMessage());
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (JSONException | ServiceException | SessionExpiredException ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, messageSource.getMessage("acc.field.Erroroccurredwhileprocessing", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (Exception ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, messageSource.getMessage("acc.field.Erroroccurredwhileprocessing", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    /*
     *  Following method validates an address or some addresses by posting the address details to addresses/resolve method from AvaTax REST Api
     */
    public ModelAndView validateAddress(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
                requestJobj.put(IntegrationConstants.skipRequestJsonProcessing, true);//This flag is used to check whether processing of data is required or not
                JSONObject responseJobj = integrationCommonService.processIntegrationRequest(requestJobj);
                jobj.put(Constants.RES_data, responseJobj);
                jobj.put(Constants.RES_success, responseJobj.optBoolean(Constants.RES_success, false));
                jobj.put(Constants.RES_msg, responseJobj.optString(Constants.RES_msg));
            } else {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, Constants.RES_timeout);
            }
        } catch (AccountingException ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, ex.getMessage());
            } catch (JSONException e) {
            }
        } catch (JSONException | ServiceException | SessionExpiredException ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, messageSource.getMessage("acc.field.Erroroccurredwhileprocessing", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (Exception ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, messageSource.getMessage("acc.field.Erroroccurredwhileprocessing", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView getTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
                JSONObject responseJobj = integrationCommonService.processIntegrationRequest(requestJobj);
                jobj.put(Constants.RES_data, responseJobj);
                jobj.put(Constants.RES_success, responseJobj.optBoolean(Constants.RES_success, false));
                jobj.put(Constants.RES_msg, responseJobj.optString(Constants.RES_msg));
            } else {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, Constants.RES_timeout);
            }
        } catch (AccountingException ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, ex.getMessage());
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (JSONException | ServiceException | SessionExpiredException ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, messageSource.getMessage("acc.field.Erroroccurredwhileprocessing", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (Exception ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, messageSource.getMessage("acc.field.Erroroccurredwhileprocessing", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView getTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
                JSONObject responseJobj = integrationCommonService.processIntegrationRequest(requestJobj);
                jobj.put(Constants.RES_data, responseJobj);
                jobj.put(Constants.RES_success, responseJobj.optBoolean(Constants.RES_success, false));
                jobj.put(Constants.RES_msg, responseJobj.optString(Constants.RES_msg));
            } else {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, Constants.RES_timeout);
            }
        } catch (AccountingException ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, ex.getMessage());
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (JSONException | ServiceException | SessionExpiredException ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, messageSource.getMessage("acc.field.Erroroccurredwhileprocessing", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (Exception ex) {
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, messageSource.getMessage("acc.field.Erroroccurredwhileprocessing", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException e) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView getAddressesForUps(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONObject addressesJson = null;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            addressesJson = integrationCommonService.getAddressesForUps(requestJobj);
            issuccess = true;
        } catch (ServiceException | SessionExpiredException ex) {
            issuccess = false;
            msg = IntegrationController.class.getName() + ".getDetailsForShippingCostCalculation:" + ex.getMessage();
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = IntegrationController.class.getName() + ".getDetailsForShippingCostCalculation:" + ex.getMessage();
            Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("addressesJson", addressesJson);
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(IntegrationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, "model", jobj.toString());
    }

}
