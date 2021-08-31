/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.accounting.handler;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author k3
 */
public interface AccLinkDataDao {
    
    public KwlReturnObject getPurchaseRequisition(HashMap<String, Object> requestParams,HashMap<String, Object> params) throws ServiceException;
    
    public KwlReturnObject getRFQ(HashMap<String, Object> requestParams,HashMap<String, Object> params ) throws ServiceException;
    
    public KwlReturnObject getVendorQuotation(HashMap<String, Object> requestParams,HashMap<String, Object> params ) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrder(HashMap<String, Object> requestParams,HashMap<String, Object> params) throws ServiceException;
   
    public KwlReturnObject getPurchaseInvoice(HashMap<String, Object> requestParams,HashMap<String, Object> params) throws ServiceException;
    
    public KwlReturnObject getGoodsReceipt(HashMap<String, Object> requestParams,HashMap<String, Object> params) throws ServiceException;

    public KwlReturnObject getPaymentInformation(HashMap<String, Object> requestParams,HashMap<String, Object> params) throws ServiceException;
    
    public KwlReturnObject getDebitNote(HashMap<String, Object> requestParams,HashMap<String, Object> params) throws ServiceException;
    
    public KwlReturnObject getInvoiceJE(HashMap<String, Object> requestParams,HashMap<String, Object> params) throws ServiceException;
 
    public KwlReturnObject getPaymentJE(HashMap<String, Object> requestParams,HashMap<String, Object> params) throws ServiceException;
    
    public KwlReturnObject getDebitNoteJE(HashMap<String, Object> requestParams,HashMap<String, Object> params) throws ServiceException;
    
    public KwlReturnObject getCqlinked(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    
    public KwlReturnObject getSalesOrder(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    
    public KwlReturnObject getReceivedPayments(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    
    public KwlReturnObject getSalesInvoiceJE(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    
    public KwlReturnObject getSalesPaymentJE(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    
    public KwlReturnObject getCreditNoteJE(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    
    public KwlReturnObject getDeliveryOrder(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    /**
     * To Get Delivery Order JE
     * 
     * @param requestParams CompanyID, Document No. 
     * @param dateparams
     * @return KwlReturnObject
     * @throws ServiceException
     */
    public KwlReturnObject getDeliveryOrderJE(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    
    public KwlReturnObject getInvoices(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    
    public KwlReturnObject getCreditNote(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    
    public KwlReturnObject getPurchaseReturn(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    
    public KwlReturnObject getSalesReturn(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    
    public KwlReturnObject checkEntryForTransactionInLinkingTableForForwardReference(String moduleName, String docid) throws ServiceException;
    
    public KwlReturnObject getDOLinkingInfo(HashMap<String, Object> requestParams,HashMap<String, Object> dateparams ) throws ServiceException;
    
    public KwlReturnObject getSOLinkingInfo(HashMap<String, Object> requestParams) throws ServiceException;
   
    public KwlReturnObject getCQLinkingInfo(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSILinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException;

    public KwlReturnObject getSRLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException;

    public KwlReturnObject getCNLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException;

    public KwlReturnObject getDNLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException;

    public KwlReturnObject getRPLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException;
    
    public KwlReturnObject getPOLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException;
    
    public KwlReturnObject getPILinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException;
     
    public KwlReturnObject getGRLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException;
    
    public KwlReturnObject getVQLinkingInfo(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getRequisitionLinkingInfo(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPRLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException;
    
    public KwlReturnObject getMPLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException;
    
    public KwlReturnObject getRFQLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException;
    
    public KwlReturnObject checkFetchedPIhavePredecessor(HashMap<String, Object> requestParams) throws ServiceException;
     
    public KwlReturnObject checkFetchedSIhavePredecessor(HashMap<String, Object> requestParams) throws ServiceException;
    
}
