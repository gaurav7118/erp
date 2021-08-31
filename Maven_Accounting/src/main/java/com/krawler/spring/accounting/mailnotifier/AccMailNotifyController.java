/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.mailnotifier;
 
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
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
public class AccMailNotifyController extends MultiActionController {
    private AccMailNotifyService accMailNotifyServiceImplobj;
   
    public void setAccMailNotifyServiceImplobj(AccMailNotifyService accMailNotifyServiceImplobj) {
        this.accMailNotifyServiceImplobj = accMailNotifyServiceImplobj;
    }
    //get email template  and replace all placeholders to send email

    public ModelAndView ReplacePlaceholdersofEmailContent(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj= StringUtil.convertRequestToJsonObject(request);
            jsonObj=accMailNotifyServiceImplobj.replacePlaceholdersofEmailContent(jsonObj);
        
        } catch (Exception ex) {
            jsonObj.put("msg", ex.getMessage());
            Logger.getLogger(AccMailNotifyController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jsonObj.toString());
    }

    /*get Place holders for Email Notifications*/
    public ModelAndView getSelectFieldPlaceholderswithCategories(HttpServletRequest request, HttpServletResponse response)throws ServiceException, JSONException {
        JSONObject jsonObj = new JSONObject();
         try {
              jsonObj= StringUtil.convertRequestToJsonObject(request);
             jsonObj=accMailNotifyServiceImplobj.getSelectFieldPlaceholderswithCategories(jsonObj);
          
        }
         catch (Exception ex) {
            jsonObj.put("msg", ex.getMessage());
            Logger.getLogger(AccMailNotifyController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jsonObj.toString());
    }
    
}
