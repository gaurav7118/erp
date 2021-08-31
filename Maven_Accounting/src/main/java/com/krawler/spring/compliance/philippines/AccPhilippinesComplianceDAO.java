/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.compliance.philippines;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;

/**
 *
 * @author krawler
 */
public interface AccPhilippinesComplianceDAO {

    public List getSalesInvoiceListDataInSQL(JSONObject requestParams, JSONObject sectionExtraParams) throws ServiceException, JSONException;

    public List getPurchaseInvoiceListDataInSQL(JSONObject requestParams, JSONObject sectionExtraParams) throws ServiceException, JSONException;

    public List getReceivePaymentAdvanceListDataInSql(JSONObject reqParams, JSONObject sectionExtraParams) throws ServiceException, JSONException;
    
    public List getReceivePaymentInvoiceListDataInSql(JSONObject reqParams, JSONObject sectionExtraParams) throws ServiceException, JSONException;
    
    public List getReceivePaymentOtherWiseListDataInSql(JSONObject reqParams, JSONObject sectionExtraParams) throws ServiceException, JSONException;
}
