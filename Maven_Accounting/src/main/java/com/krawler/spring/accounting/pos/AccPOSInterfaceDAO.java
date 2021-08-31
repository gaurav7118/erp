/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.pos;

import java.util.List;
import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
/**
 *
 * @author krawler
 */
public interface AccPOSInterfaceDAO {

    public JSONObject saveCompanywiseCurrencyDenomination(JSONObject paramJobj);

    public List getCompanywiseCurrencyDenomination(JSONObject paramJobj) throws ServiceException, JSONException;

    public KwlReturnObject getRegisterDetails(JSONObject paramJobj) throws ServiceException, JSONException;

    public JSONObject deleteCurrencyDenominations(JSONObject paramJobj) throws ServiceException, JSONException;

    public JSONObject openandCloseRegister(JSONObject paramJobj) throws ServiceException, JSONException;
    
    public JSONObject savePOSCompanyWizardSettings(JSONObject paramJobj) throws ServiceException, JSONException ;
    
    public KwlReturnObject getPOSConfigDetails(JSONObject paramJobj) throws ServiceException, JSONException;
    
    public JSONObject saveCashOutDetails(HashMap<String,Object> reqParams) throws ServiceException, JSONException;
    
    public JSONObject savePaymentMethodType(HashMap<String, Object> reqParams) throws ServiceException, JSONException ;
    
    public JSONObject getInvoiceDetailsid(String invoiceid, String companyid, String productid) throws ServiceException;
    
    public KwlReturnObject getPOSPaymentMethodDetails(JSONObject paramJobj) throws ServiceException, JSONException;
    
    public KwlReturnObject getPOSCashOutDetails(JSONObject paramJobj) throws ServiceException, JSONException;
     
    public KwlReturnObject getPreviousClosedBalanceDetails(JSONObject paramJobj) throws ServiceException, JSONException;
    
    public JSONObject deletePaymentMethodEntry(JSONObject requestParamsJson) throws ServiceException, JSONException;
    
}
