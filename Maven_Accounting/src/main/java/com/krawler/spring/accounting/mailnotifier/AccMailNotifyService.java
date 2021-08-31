/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.mailnotifier;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.NotificationRules;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.DocumentException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public interface AccMailNotifyService {

    public JSONObject replacePlaceholdersofEmailContent(JSONObject jsonObj) throws ServiceException, JSONException;

    public JSONObject replacePlaceHolders(JSONObject jsonObj, Company company, KwlReturnObject result, int moduleid) throws ServiceException, JSONException;

    public JSONObject replaceCommonPlaceHoldersforEmailSubjectBody(JSONObject jsonObj, Company company, NotificationRules dft, int moduleid, KwlReturnObject result);

    public String replaceSummaryLevelFields(JSONObject jsonObj, HashMap<String, Object> customParams, JSONArray lineItemsArr) throws JSONException, ServiceException;

    public String replaceSqlQueryLevelFields(JSONObject jsonObj, HashMap<String, Object> customParams) throws JSONException, ServiceException;

    public String replaceGlobalLevelCustomFields(JSONObject jsonObj, AccountingHandlerDAO accountingHandlerDAOobj, accAccountDAO accAccountDAOobj, HashMap<String, Object> customParams) throws JSONException, SessionExpiredException, ServiceException;

    public JSONObject getSelectFieldPlaceholderswithCategories(JSONObject jsonObj);

    public String replaceCustomFieldsforOtherModule(JSONObject jsonObj, HashMap<String, Object> customParams) throws JSONException, ServiceException;

    public JSONObject sendMailNotification(JSONObject jsonObj) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException;
    
    public void sendInvoicesonMail(JSONObject requestParam) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException;
}
