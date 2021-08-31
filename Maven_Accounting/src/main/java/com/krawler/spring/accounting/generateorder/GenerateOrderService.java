/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.generateorder;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;

/**
 *
 * @author krawler
 */
public interface GenerateOrderService {

    public JSONObject validateAndSaveDoc(JSONObject requestJobj)throws ServiceException,JSONException, SessionExpiredException, ParseException, AccountingException;

    public JSONObject validateLinkDocNumber(JSONObject requestJobj)throws ServiceException, JSONException, AccountingException;
    
    
}
