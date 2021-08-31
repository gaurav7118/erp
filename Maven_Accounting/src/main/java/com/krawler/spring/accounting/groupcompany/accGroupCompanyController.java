/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.groupcompany;

import com.krawler.utils.json.base.JSONObject;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
/**
 *
 * @author krawler
 */
public class accGroupCompanyController extends MultiActionController{
    
    private AccGroupCompanyService accGroupCompanyService;

    public void setaccGroupCompanyService(AccGroupCompanyService accGroupCompanyService) {
        this.accGroupCompanyService = accGroupCompanyService;
    }

    public ModelAndView getPurchaseAndSalesModules(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        boolean isSuccess = false;
        try {
            JSONObject reqJson = StringUtil.convertRequestToJsonObject(request);
            returnJobj = accGroupCompanyService.getPurchaseAndSalesModules(reqJson);
            isSuccess = true;
        } catch (Exception ex) {
        } finally {
            returnJobj.put("success", isSuccess);
        }
        return new ModelAndView("jsonView", "model", returnJobj.toString());
    } 
    
    
    public ModelAndView getTax(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        boolean isSuccess = false;
        try {
            JSONObject reqJson = StringUtil.convertRequestToJsonObject(request);
            returnJobj = accGroupCompanyService.getTax(reqJson);
            isSuccess = true;
        } catch (Exception ex) {
        } finally {
            returnJobj.put("success", isSuccess);
        }
        return new ModelAndView("jsonView", "model", returnJobj.toString());
    }

    public ModelAndView getSubdomains(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        boolean isSuccess = false;
        try {
            JSONObject reqJson = StringUtil.convertRequestToJsonObject(request);
            returnJobj = accGroupCompanyService.getSubdomains(reqJson);
            isSuccess = true;
        } catch (Exception ex) {
        } finally {
            returnJobj.put("success", isSuccess);
        }
        return new ModelAndView("jsonView", "model", returnJobj.toString());
    }
   
    public ModelAndView getCustomerVendorMappingFields(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        boolean isSuccess = false;
        try {
            JSONObject reqJson = StringUtil.convertRequestToJsonObject(request);
            returnJobj = accGroupCompanyService.getCustomerVendorRecords(reqJson);
            isSuccess = true;
        } catch (Exception ex) {
        } finally {
            returnJobj.put("success", isSuccess);
        }
        return new ModelAndView("jsonView", "model", returnJobj.toString());
    }

    public ModelAndView getInvoiceTermsMappingFields(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        boolean isSuccess = false;
        try {
            JSONObject reqJson = StringUtil.convertRequestToJsonObject(request);
            returnJobj = accGroupCompanyService.getInvoiceTermsRecords(reqJson);
            isSuccess = true;
        } catch (Exception ex) {
        } finally {
            returnJobj.put("success", isSuccess);
        }
        return new ModelAndView("jsonView", "model", returnJobj.toString());
    }
    
    public ModelAndView saveGroupCompanyWizardSettings(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        boolean isSuccess = false;
        try {
            JSONObject reqJson = StringUtil.convertRequestToJsonObject(request);
            returnJobj = accGroupCompanyService.saveGroupCompanyWizardSettings(reqJson);
            isSuccess = true;
        } catch (Exception ex) {
        } finally {
            returnJobj.put("success", isSuccess);
        }
        return new ModelAndView("jsonView", "model", returnJobj.toString());
    }
}
