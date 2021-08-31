/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.pos;

import com.krawler.utils.json.base.JSONObject;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
/**
 *
 * @author krawler
 */
public class accPOSInterfaceController extends MultiActionController{
    
        
    private AccPOSInterfaceService accPOSInterfaceService;

    public void setaccPOSInterfaceService(AccPOSInterfaceService accPOSInterfaceService) {
        this.accPOSInterfaceService = accPOSInterfaceService;
    }

    public ModelAndView savePOSCompanyWizardSettings(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        boolean isSuccess = false;
        try {
            JSONObject reqJson = StringUtil.convertRequestToJsonObject(request);
            returnJobj = accPOSInterfaceService.savePOSCompanyWizardSettings(reqJson);
            isSuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(accPOSInterfaceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnJobj.put("success", isSuccess);
        }
        return new ModelAndView("jsonView", "model", returnJobj.toString());
    }
    //Fetch Details for Mapping of Stores
    public ModelAndView getERPPOSMappingDetails(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        boolean isSuccess = false;
        try {
            JSONObject reqJson = StringUtil.convertRequestToJsonObject(request);
            returnJobj = accPOSInterfaceService.getERPPOSMappingDetails(reqJson);
            isSuccess = true;
        }  catch (Exception ex) {
            Logger.getLogger(accPOSInterfaceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnJobj.put("success", isSuccess);
        }
        return new ModelAndView("jsonView", "model", returnJobj.toString());
    }
    
}
