
package com.krawler.defaultfieldsetup;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class AccFieldSetupController extends MultiActionController implements MessageSourceAware{
    
    private HibernateTransactionManager txnManager;
    private AccFieldSetupServiceDao accFieldSetUpServiceDAOObj;
    private MessageSource messageSource;

    public void setAccFieldSetUpServiceDAOObj(AccFieldSetupServiceDao accFieldSetUpServiceDAOObj) {
        this.accFieldSetUpServiceDAOObj = accFieldSetUpServiceDAOObj;
    }
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public ModelAndView getMobileFieldsConfig(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        String type = paramJobj.optString(Constants.type);
        JSONArray dataArray = accFieldSetUpServiceDAOObj.getMobileFieldsConfig(paramJobj);
        JSONArray jsonArray = dataArray.getJSONObject(0).getJSONArray(type);
        jobj.put(Constants.RES_data, jsonArray);
        jobj.put(Constants.RES_success, true);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveMobileFieldsConfigSettings(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            KwlReturnObject result = accFieldSetUpServiceDAOObj.saveMobileFieldsConfigSettings(paramJobj);
            msg = result.getMsg();
            issuccess = result.isSuccessFlag();
            if (issuccess) {
                txnManager.commit(status);
            } else {
                txnManager.rollback(status);
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(AccFieldSetupController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccFieldSetupController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
}
