/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.vendorpayment.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.Receipt;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AccVendorPaymentModuleService {
    
public HashMap<String, Object> saveVendorPayment(JSONObject paramJobj);
 public double RefundPaymentForexGailLossAmount(JSONObject paramJobj, JSONObject jobj, Payment payment, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException, JSONException, ParseException ;
 public JSONObject importMakePaymentJSON(JSONObject paramObj);
 public void postRoundingJEAfterLinkingInvoiceInPayment(JSONObject paramJobj) throws ServiceException;
 public void postRoundingJEAfterPaymentApprove(JSONObject paramJobj) throws ServiceException;
 public void postRoundingJEOnRevertDishonouredPayment(JSONObject paramJobj, Set<String> amountDueUpdatedInvoiceIDSet) throws ServiceException;

 public HashMap<String, Object> saveBulkVendorPayment(JSONObject paramJobj) throws ServiceException  , SessionExpiredException;
 public List<JSONObject> getBillingPaymentsJson(HashMap<String, Object> requestParams, List list, List<JSONObject> jsonlist) throws ServiceException;
 public List<JSONObject> getOpeningBalanceReceiptJsonForReport(JSONObject paramJobj, List list, List<JSONObject> jsonObjectlist);
 public List<JSONObject> getPaymentsJson(HashMap<String, Object> requestParams, List list, List<JSONObject> jsonlist) throws ServiceException;
 public double getPaymentAmountDue(Payment payment);
 public double getTotalTDSAmount(Payment payment) throws ServiceException;
    public JSONObject deletePaymentPermanentJSON(JSONObject paramJobj);

    public String deletePaymentPermanent(JSONObject paramJobj) throws AccountingException, SessionExpiredException, ServiceException, ParseException;

    public void updateReceiptAdvancePaymentAmountDue(Payment payment, String companyId, String countryId) throws JSONException, ServiceException;

    public void updateOpeningBalance(Payment payment, String companyId) throws JSONException, ServiceException;

    public void updateCNAmountDueForReceipt(Payment payment, String companyId) throws ServiceException;

    public JSONObject deletePaymentTemporaryJSON(JSONObject paramJobj);
    
    public JSONObject rejectPaymentTemporaryJSON(JSONObject paramJobj) throws ServiceException,AccountingException;

    public JSONObject deleteOpeningPaymentPermanent(JSONObject paramJobj,String[] paymentid,String[] paymentno) throws ServiceException,AccountingException;
    
    public String deletePaymentTemporary(JSONObject paramJobj) throws AccountingException, SessionExpiredException, ServiceException;
    public JSONObject getReceiptJSON(Receipt receipt, Map<String, Object> requestParams) throws JSONException, ServiceException,SessionExpiredException;
}
