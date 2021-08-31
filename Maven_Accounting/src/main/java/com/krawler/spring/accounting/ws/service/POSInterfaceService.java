/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 *
 * @author krawler
 */
public interface POSInterfaceService {

    public JSONObject saveCompanywiseCurrencyDenomination(JSONObject paramJobj) throws ServiceException, JSONException;

    public JSONObject getCompanywiseCurrencyDenomination(JSONObject paramJobj) throws ServiceException, JSONException;

    public JSONObject getRegisterDetails(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException;

    public JSONObject deleteCurrencyDenominations(JSONObject paramJobj) throws ServiceException, JSONException;

    public JSONObject saveOpenandCloseRegisterDetaisls(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException;
    
    public JSONObject saveInvoiceDOandRP(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException,ParseException,AccountingException;
    
    public JSONObject saveCashOutDetails(JSONObject paramJobj) throws ServiceException, JSONException,SessionExpiredException,ParseException;
    
    public JSONObject getERPPOSMappingDetails(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException;
    
    public JSONObject getClosedBalanceDetails(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException;
    
    public JSONObject saveSalesReturnwithCN(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException,ParseException,UnsupportedEncodingException,AccountingException;
    
    public JSONObject getCashoutReports(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException;

    public JSONObject saveSalesOrderLinkedAdvanceReceipts(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException, UnsupportedEncodingException,AccountingException ;
    
     public JSONObject saveReceivePaymentAgainstInvoice(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException;
     
    public JSONObject saveCashOutTransactionDepositType(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException, UnsupportedEncodingException;

    public JSONObject deleteInvoiceDOandRP(JSONObject paramJobj) throws ServiceException, JSONException;
    
    public JSONObject saveAdvanceReceiptPayment(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException, UnsupportedEncodingException;
    
}
