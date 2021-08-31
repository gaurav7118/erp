/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.accountservice;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Group;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author krawler
 */
public interface AccAccountService {

    public ModelAndView getTransactionFormFields(HttpServletRequest request, HttpServletResponse response);
    
    public JSONArray getInvoiceTerms(HttpServletRequest request) throws JSONException, SessionExpiredException;
    
    public void UpdateExistingRecordsWithDefaultValue(HashMap<Integer, HashMap<String, Object>> modulerequestParams,ArrayList moduleArr,String companyid) throws ServiceException;
    
    public JSONObject deleteCustomTemplate(JSONObject requestJobj,boolean isAdminSubdomain) throws ServiceException,AccountingException;
}
