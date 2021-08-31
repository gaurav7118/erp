/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.mrp.machinemanagement;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.mrp.labormanagement.AccLabourControllerCMN;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccMachineManagementControllerCMN extends MultiActionController implements MessageSourceAware {
    
    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private String successView;
    private AccMachineManagementServiceDAO accMachineManagementServiceDAOObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    
    
    @Override
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

  
    public AccMachineManagementServiceDAO getAccMachineManagementServiceDAOObj() {
        return accMachineManagementServiceDAOObj;
    }

    public void setAccMachineManagementServiceDAOObj(AccMachineManagementServiceDAO accMachineManagementServiceDAOObj) {
        this.accMachineManagementServiceDAOObj = accMachineManagementServiceDAOObj;
    }
    
     public accCompanyPreferencesDAO getAccCompanyPreferencesObj() {
        return accCompanyPreferencesObj;
    }

    public void setAccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    
    
    
}
