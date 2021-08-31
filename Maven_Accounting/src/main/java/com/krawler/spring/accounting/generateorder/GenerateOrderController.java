/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.generateorder;


import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
/**
 *
 * @author krawler
 */
public class GenerateOrderController extends MultiActionController {

    GenerateOrderService generateOrderServiceobj;
    
    public void setgenerateOrderServiceobj(GenerateOrderService generateOrderServiceobj){
        this.generateOrderServiceobj=generateOrderServiceobj;
    }
    
    /**
     * provide the product details that are linked to the SO/SI/PO.  
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView validateLinkDocNumber(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            JSONObject responseJobj = generateOrderServiceobj.validateLinkDocNumber(requestJobj);
            jobj.put(Constants.RES_data, responseJobj);
            jobj.put(Constants.RES_success, responseJobj.optBoolean(Constants.RES_success, false));
            jobj.put(Constants.RES_msg, responseJobj.optString(Constants.RES_msg));
        } catch (JSONException | SessionExpiredException | ServiceException | AccountingException ex) {
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, ex.getMessage() != null ? ex.getMessage() : "Failure");
            } catch (JSONException e) {
                Logger.getLogger(GenerateOrderController.class.getName()).log(Level.SEVERE, null, e);
            }
            Logger.getLogger(GenerateOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    /**
     * called while saving the GRN/DO from jsp page.
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView validateAndSaveDoc(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            JSONObject responseJobj = generateOrderServiceobj.validateAndSaveDoc(requestJobj);
            jobj.put(Constants.RES_data, responseJobj);
            jobj.put(Constants.RES_success, responseJobj.optBoolean(Constants.RES_success, false));
            String msg=Jsoup.parse(responseJobj.optString(Constants.RES_msg)).text();
            jobj.put(Constants.RES_msg, msg);
        } catch (JSONException | SessionExpiredException | ServiceException | ParseException | AccountingException ex ) {
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, ex.getMessage() != null ? ex.getMessage() : "Failure");
            } catch (JSONException e) {
                Logger.getLogger(GenerateOrderController.class.getName()).log(Level.SEVERE, null, e);
            }
            Logger.getLogger(GenerateOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
}

