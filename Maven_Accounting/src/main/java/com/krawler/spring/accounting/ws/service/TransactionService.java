/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.ws.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.Receipt;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import javax.servlet.http.HttpServletResponse;

public interface TransactionService {
    
    //GET
    public JSONObject getAccountList(JSONObject paramJobj) throws JSONException,ServiceException,SessionExpiredException;
    public JSONObject getAccountsIdNameList(JSONObject paramJobj) throws JSONException,ServiceException,SessionExpiredException;
    public JSONObject getInvoice(JSONObject paramJObj);
    public JSONObject getIndividualProductPrice(JSONObject paramJObj);
    public JSONObject getPaymentMethod(JSONObject paramJobj)throws JSONException,ServiceException,SessionExpiredException;
    public JSONObject getSalesOrder(JSONObject paramJobj) throws JSONException, ServiceException,SessionExpiredException,UnsupportedEncodingException;
    public JSONObject getJournalEntry(JSONObject paramJObj) throws JSONException, ServiceException, SessionExpiredException;
    public JSONObject getInvoiceDetailfromCRMQuotation(JSONObject paramJobj) throws ServiceException, JSONException;
    public JSONObject getQuotations(JSONObject paramJobj) throws ServiceException, JSONException;
    public JSONObject getCQLinkedInTransaction(JSONObject paramJobj) throws JSONException, ServiceException ;
    public JSONObject getVendorQuotations(JSONObject paramJobj) throws ServiceException, JSONException;
    public JSONObject getSalesReturn(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException ,UnsupportedEncodingException;
    public JSONObject getCreditNote(JSONObject paramJobj) throws JSONException, ServiceException,SessionExpiredException,UnsupportedEncodingException;
    public JSONObject getCashRevenueTask(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    public JSONObject getCashAndPurchaseRevenue(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    public JSONObject getVendorInvoicesReport(JSONObject jobj) throws ServiceException, JSONException, SessionExpiredException;
    public JSONObject getIncidentCase(JSONObject paramJobj) throws JSONException, ServiceException,SessionExpiredException;
    public JSONObject getIncidentChart(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    public JSONObject getCustomerInvoicesReport(JSONObject jobj) throws  ServiceException, JSONException, SessionExpiredException, SessionExpiredException ;
    public JSONObject getSalesReturnSummaryReport(JSONObject paramJobj) throws JSONException,ServiceException,SessionExpiredException;
    public JSONObject getSalesByCustomer(JSONObject paramJobj);
    public JSONObject getReceipts(JSONObject paramJobj)throws JSONException, ServiceException, SessionExpiredException ,UnsupportedEncodingException,ParseException;
    public JSONObject getPayments(JSONObject paramJobj)throws JSONException, ServiceException, SessionExpiredException ,UnsupportedEncodingException,ParseException;
    public JSONObject getDesignTemplateList(JSONObject paramJobj)throws JSONException, ServiceException, SessionExpiredException ,UnsupportedEncodingException;
    public JSONObject getDeliveryOrderMerged(JSONObject paramJobj);
    public JSONObject getPurchaseOrder(JSONObject paramJobj)throws JSONException, ServiceException,SessionExpiredException;
    
    //Save 
    public JSONObject saveJournalEntry(JSONObject paramJobj)throws JSONException,ServiceException, SessionExpiredException, AccountingException,ParseException;
    public JSONObject saveInvoice(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException;
    public JSONObject saveReceiptPayment(JSONObject paramJobj) throws JSONException, ServiceException,ParseException,SessionExpiredException;
    public JSONObject saveSalesOrder(JSONObject paramJobj) throws JSONException, ServiceException,SessionExpiredException,UnsupportedEncodingException,ParseException;
    public JSONObject saveIncidentCase(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    public JSONObject savePayment(JSONObject paramJobj) throws JSONException, ServiceException,SessionExpiredException,UnsupportedEncodingException,ParseException;
    public JSONObject savelinkAdvanceReceiptToSalesOrder(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException ;
    public JSONObject saveDeliveryOrder(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException;
    public JSONObject saveGoodsReceipt(JSONObject paramJobj) throws JSONException, AccountingException, ServiceException, SessionExpiredException, ParseException;
    public JSONObject saveTransactions(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException, ParseException ;
    public JSONObject postAmountJE(JSONObject jobj) throws JSONException, ServiceException, AccountingException;
    public JSONObject saveSalesReturn(JSONObject paramJobj) throws JSONException, ServiceException,SessionExpiredException,AccountingException,ParseException;
    public JSONObject saveCreditNote(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException;
    public JSONObject postSalaryJE(JSONObject jobj) throws JSONException, ServiceException,AccountingException;
    public JSONObject postReverseSalaryJE(JSONObject jobj) throws JSONException, ServiceException,AccountingException; 
    
    //Delete
    public JSONObject deleteInvoice(JSONObject paramJobj)throws JSONException,ServiceException, SessionExpiredException;
    public JSONObject deleteQuotation(JSONObject paramJobj) throws JSONException, ServiceException;
    public JSONObject deleteReceivePayment(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException, ParseException;
    public JSONObject deleteMakePaymentJSON(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException,AccountingException,ParseException ;
    public JSONObject deleteSalesReturn(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    public JSONObject deleteSalesReturnTemporary(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    public JSONObject deleteDeliveryOrdersJSON(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    public JSONObject deleteCreditNoteJSON(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException, ParseException ;
    public JSONObject deleteIncidentCase(JSONObject paramJobj)throws JSONException,ServiceException, SessionExpiredException;
    public JSONObject deleteSalesOrder(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException; 
    
    //Create
    public JSONObject jsonCreatelinkInvoicesToReceivePayment(JSONObject paramJobj,JSONObject responseObj, Invoice invObj, Receipt receiptObj) throws JSONException;
    public JSONObject jsonCreateReceivePayment(JSONObject paramJobj, JSONObject responseObj) throws JSONException;
    public JSONObject jsonDeleteInvoice(String invid, JSONObject paramJobj) throws JSONException, AccountingException, ParseException, SessionExpiredException;
    public JSONObject jsonDeleteReceivePayment(String receipid, JSONObject paramJobj, String receiptno) throws JSONException;
    
    public JSONObject printDocumentDesignerTemplate(JSONObject paramJobj, HttpServletResponse servletresponse)  throws JSONException, ServiceException;
    public ByteArrayOutputStream exportMobilePDFImages(JSONObject paramJobj) throws JSONException,ServiceException,SessionExpiredException;
}

