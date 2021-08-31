/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.accounting.receipt;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.*;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accReceiptDAO {

//    public KwlReturnObject getReceiptObj(String receiptid);
    public KwlReturnObject saveReceipt(HashMap<String, Object> hm) throws ServiceException;
    public Receipt getReceiptObj(HashMap<String, Object> hm) throws ServiceException,AccountingException;
    public KwlReturnObject saveReceiptObject(List<Receipt> receiptList) throws ServiceException;
    
    public KwlReturnObject deleteReceiptAdvanceDetails(String receiptid, String companyid) throws ServiceException ;
    
    public KwlReturnObject getCustomerDnPayment(String receiptId,String dnnoteid) throws ServiceException;
    
    public KwlReturnObject getCustomerDnPayment(String receiptId) throws ServiceException;
    
    public KwlReturnObject deleteReceiptsDetailsAndUpdateAmountDue(String receiptid, String companyid,int beforeEditApprovalStatus) throws ServiceException;
    
    public KwlReturnObject deleteReceiptsAgainstCNDN(String receiptid, String companyid,int beforeEditApprovalStatus) throws ServiceException ;
    
    public KwlReturnObject saveCustomerDnPaymenyHistory(HashMap<String,String> hashMap) throws ServiceException ;
            
    public KwlReturnObject saveReceiptObject(Receipt receipt) throws ServiceException;

    public KwlReturnObject getReceipts(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getReceiptAdvanceAmountDueDetails(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getOpeningBalanceReceipts(HashMap<String, Object> request) throws ServiceException;
    
    public int getOpeningBalanceReceiptCount(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForReceipts(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceTotalBaseAmountForReceipts(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getAccountNameCount(String invoiceNo, String companyid) throws ServiceException;
    
    public KwlReturnObject getRefundNameCount(String refundNo, String companyid,String vendorId) throws ServiceException;
    
    public KwlReturnObject getCurrency(String currencyname) throws ServiceException;
    
    public KwlReturnObject getPaymentMethodCount(String paymentMethodStr, String companyid) throws ServiceException;
    
    public KwlReturnObject getAllOpeningBalanceReceipts(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getReceiptFromBillNo(String billno, String companyid) throws ServiceException;
    
    public KwlReturnObject getReceiptEditCount(String entryNumber, String companyid, String receiptId) throws ServiceException;

    public KwlReturnObject getContraPaymentFromInvoice(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getReceiptFromInvoice(HashMap<String, Object> receiptMap) throws ServiceException;

    public KwlReturnObject getReceiptFromInvoice(String invoiceid) throws ServiceException;
    
    public KwlReturnObject getReceiptFromInvoiceNewUI(String invoiceid) throws ServiceException;

    public Map<String, List<ReceiptDetail>> getContraPayReceiptFromGReceiptList(List<String> invoiceIDLIST) throws ServiceException;

    public KwlReturnObject getReceiptAmountFromInvoice(String invoiceid) throws ServiceException;
    
    public KwlReturnObject getReceiptAmountFromInvoiceNewUI(String invoiceid) throws ServiceException;
    
    public KwlReturnObject getReceiptAmountFromBadDebtClaimedInvoice(String invoiceid, boolean isBeforeClaimed) throws ServiceException;
    
    public KwlReturnObject getReceiptFromBadDebtClaimedInvoice(String invoiceid, boolean isBeforeClaimed, Date badDebtCalculationToDate) throws ServiceException;
    
    public KwlReturnObject getReceiptFromBadDebtClaimedInvoiceForNewUI(String invoiceid, boolean isBeforeClaimed) throws ServiceException;

    public KwlReturnObject deleteReceiptDetails(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject deleteReceiptDetailsAndUpdateAmountDue(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject deleteReceiptDetailsOtherwise(String receiptid) throws ServiceException;

    public KwlReturnObject deleteReceipt(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject deleteReceiptPermanent(HashMap<String, Object> requestParams) throws ServiceException,AccountingException;
    
    public KwlReturnObject deleteJEEntry(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject deleteJEDtails(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getDebitNotePaymentDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteLinkReceiptsDetailsAndUpdateAmountDue(Map<String, Object> requestMap,String receiptid, String companyid,boolean tempCheck) throws ServiceException ;
    public KwlReturnObject deleteLinkReceiptDetails(String receiptid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteLinkReceiptToDebitNoteDetails(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject deleteLinkReceiptToAdvancePaymentDetails(String receiptid, String companyid) throws ServiceException;
    
     public KwlReturnObject deleteLinkReceiptToSalesOrder(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject getBillingReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveReceiptDetailOtherwise(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getReceiptDetailOtherwise(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCalculatedReceivePaymentOtherwiseTax(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAdvanceReceiptTax(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCalculatedCreditNoteOtherwiseTax(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteReceiptEntry(String receiptid, String companyid) throws ServiceException,AccountingException;

    public KwlReturnObject getJEFromReceipt(String receiptid) throws ServiceException;
    
    public KwlReturnObject getDisHonouredJEFromReceipt(String receiptid) throws ServiceException;
    
    public KwlReturnObject updateDisHonouredJEFromReceipt(String receiptid,String company) throws ServiceException;
    
//    public KwlReturnObject getBillingReceiptObj(String receiptid);

public KwlReturnObject getBankChargeJEFromReceipt(String receiptid) throws ServiceException;

    public KwlReturnObject getBankInterestJEFromReceipt(String receiptid) throws ServiceException;

    public KwlReturnObject saveBillingReceipt(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject saveBillingReceiptDetailOtherwise(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBillingReceiptDetailOtherwise(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBillingReceipts(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getBillingReceiptFromBillNo(String billno, String companyid) throws ServiceException;

    public KwlReturnObject getBillingReceiptAmountFromInvoice(String invoiceid) throws ServiceException;

    public KwlReturnObject deleteBillingReceiptDetails(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject deleteBillingReceiptDetailsOtherwise(String receiptid) throws ServiceException;

    public KwlReturnObject deleteBillingReceipt(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject deleteBillingReceiptEntry(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject getBReceiptFromBInvoice(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getReceiptsContainingProject(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getBillingReceiptsContainingProject(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getReciptPaymentCustomData(HashMap<String, Object> requestParams) throws ServiceException;
   
    public KwlReturnObject getReciptPaymentGlobalCustomData(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getReciptFromJE(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject getReciptFromDisHonouredJE(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject getAdvanceReceiptLinkedInvoiceJE(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject getBReciptFromJE(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getReceiptLinkedInvoiceJE(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject getJEFromBR(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject getBillingReceiptDetail(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject getReceiptCustomerNames(String companyid, String paymentid) throws ServiceException;

    public KwlReturnObject getBillingReceiptCustomerNames(String companyid, String paymentid) throws ServiceException;

    public KwlReturnObject updateDnAmount(String iD, double amount) throws ServiceException;
    
    public KwlReturnObject updateAdvancePaymentAmountDue(JSONObject params) throws ServiceException;
    
    public KwlReturnObject updateAdvancePaymentAmountDueLinkedExternally(JSONObject params) throws ServiceException;

    public KwlReturnObject updateDnOpeningAmountDue(String iD, double amount) throws ServiceException;

    public KwlReturnObject saveCustomerDnPaymenyHistory(String cnnoteid, double paidncamount, double originalamountdue, String paymentId) throws ServiceException;

    public KwlReturnObject getPaymentIdLinkedWithNote(String noteId) throws ServiceException;

    public KwlReturnObject updateCnAmount(String iD, double amount) throws ServiceException;

    public KwlReturnObject updateCnOpeningAmountDue(String iD, double amount) throws ServiceException;

    public KwlReturnObject saveCustomerCnPaymenyHistory(String cnnoteid, double paidncamount, double originalamountdue, String paymentId) throws ServiceException;

    public KwlReturnObject getreceipthistory(String receiptid) throws ServiceException;

    public KwlReturnObject getaccounthistory(String debitnoteid) throws ServiceException;

    public KwlReturnObject updateDnUpAmount(String noteid, double amount) throws ServiceException;

    public KwlReturnObject getaccountdetailsReceipt(String accid) throws ServiceException;
    
    public KwlReturnObject getDuplicateForNormalReceipt(String entryNumber, String companyid, String receiptid, String advanceId,Receipt receipt)throws ServiceException;
    
    public KwlReturnObject getCurrentSeqNumberForAdvance(String sequenceformat, String companyid)throws ServiceException;
    
    public KwlReturnObject gettotalrecordOfreceiptno(String receiptno) throws ServiceException;
    
    public KwlReturnObject getInvoiceAdvPaymentList(HashMap <String,String> payHashMap) throws ServiceException;
    
    public Receipt getReceiptObject(Receipt receipt) throws ServiceException;

    public KwlReturnObject getCustomerDnPaymenyHistory(String cnnoteid, double paidncamount,double originalamountdue,String paymentId) throws ServiceException;
    
    public List getAdvanceReceiptUsedInRefundPayment(String receiptadvancedetailid) throws ServiceException;
    
    public List getAdvanceReceiptUsedSalesOrder(JSONObject params) throws ServiceException ;
    
    public List getAdvancePaymentDetails(String receiptadvancedetailid) throws ServiceException;
    
    public KwlReturnObject getLinkDetailReceipt(HashMap<String, Object> reqParams1) throws ServiceException;
    
    public KwlReturnObject getCashSalesFOrDayEndCollection(HashMap<String, Object> requestParams) throws ServiceException ;
    
    public KwlReturnObject getReceiptsFOrDayEndCollection(HashMap<String, Object> requestParams) throws ServiceException ;
   
    public List<LinkDetailReceipt> getDeletedLinkedReceiptInvoices(Receipt receipt,List<String> linkedDetailInvoice, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteSelectedLinkedReceiptInvoices(String receiptid, String linkedDetailIDs, String companyid,String unlinkedDetailIDs) throws ServiceException;
    
    public KwlReturnObject getReceiptAdvanceDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPayDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public List getMulticurrencyReceiptsWithPCToPMCRateOne(String companyId) throws ServiceException;   
    
    public KwlReturnObject updateDnOpeningBaseAmountDue(String Id, double amount) throws ServiceException;
    
    public KwlReturnObject getPaymentInformationFromAdvanceDetailId(String documentId,String companyId) throws ServiceException;
    
    public KwlReturnObject getReceiptsForVendor(String personId,String companyId) throws ServiceException;
    
    public KwlReturnObject getDnReceiptHistory(String companyid) throws ServiceException;
    
    public KwlReturnObject saveDebitNotePaymentDetails(HashMap<String,Object> datamap) throws ServiceException;
    
    public List<String> getTotalJEDIDReceiptAdvanceDetails(String receiptid,String companyid) throws ServiceException ;
    
    public List<String> getTotalJEDIDReceiptDetails(String receiptid,String companyid) throws ServiceException;
    
    public List<String> getTotalJEDIDDebitNotePaymentDetails(String receiptid,String companyid) throws ServiceException;

    public String UpdateReceiptEntry(Map<String, Object> seqNumberMap);

    public String UpdateReceiptEntryForNA(String recid, String entrynumber);
    
    public KwlReturnObject getPaymentReceiptsForJE(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getNormalReceipts(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateReceipt(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getReceiptLinkedDebitNoteJE(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteLinkReceiptsDetailsToDebitNoteAndUpdateAmountDue(String receiptid, String companyid,boolean tempCheck) throws ServiceException ;
    
    public List<LinkDetailReceiptToDebitNote> getDeletedLinkedReceiptDebitNotes(Receipt receipt,String linkedDetailIDs, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteSelectedLinkedReceiptDebitNotes(String receiptid, String linkedDetailIDs, String companyid) throws ServiceException;
    
    public KwlReturnObject getAdvanceReceiptIdLinkedWithNote(String noteId) throws ServiceException;
    
    public KwlReturnObject getReceiptWriteOffJEs(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getReverseReceiptWriteOffJEs(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getLinkDetailReceiptToDebitNote(HashMap<String, Object> reqParams1) throws ServiceException;
    
    public KwlReturnObject getLinkDetailAdvanceReceiptToRefundPayment(HashMap<String, Object> reqParams1) throws ServiceException;
    
    public KwlReturnObject getLinkDetailReceiptToAdvancePayment(HashMap<String, Object> reqParams1) throws ServiceException;
    
    public KwlReturnObject getReceiptWriteOffEntries(HashMap<String, Object> reqParams1) throws ServiceException;
    
    public KwlReturnObject deleteReceiptsDetailsLoanAndUpdateAmountDue(String receiptid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteReceiptDetailsLoan(String receiptid, String companyid) throws ServiceException;
    
    public KwlReturnObject getAdvanceDetailsByReceipt(HashMap<String, Object> reqParams1) throws ServiceException;
    
    public List getAdvancePaymentUsedInRefundReceipt(String advancedetailid) throws ServiceException;
    
    public KwlReturnObject saveReceiptLinking(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject updateEntryInDebitNoteLinkingTable(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject deleteLinkingInformationOfRP(HashMap<String, Object> requestParams) throws ServiceException;
    
    public List<LinkDetailReceiptToAdvancePayment> getDeletedLinkedReceiptAdvancePayment(Receipt receipt,String linkedDetailIDs, String companyid) throws ServiceException;
    
    public KwlReturnObject getPaymentInformationFromPaymentId(String paymentId,String companyId) throws ServiceException;
    
    public KwlReturnObject deleteSelectedLinkedReceiptAdvanceDetails(String receiptid, String linkedDetailIDs, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteLinkReceiptsDetailsToAdvancePaymentAndUpdateAmountDue(String receiptid, String companyid,boolean tempCheck) throws ServiceException;
    
    public List getAdvanceReceiptLinkedWithRefundPayment(String receiptid,String companyid) throws ServiceException;
    
    public KwlReturnObject getDataSTRealisationDateWiseReport(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDebitNotePaymentDetail(HashMap<String, Object> reqMap) throws ServiceException;
    
    public KwlReturnObject getReceiptDetailsLinkedWithInvoices(HashMap<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject approvePendingReceivePayment(String cnID, String companyid, int status) throws ServiceException;
    
    public KwlReturnObject rejectPendingReceivePayment(String cnid, String companyid) throws ServiceException;
    
    public KwlReturnObject saveAdvanceDetailsTermMap(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getAdvanceDetailsTerm(JSONObject paramObj) throws ServiceException, JSONException;
    
    public int updateRefundPaymentLinkedWithAdvance(JSONObject paramJobj) throws ServiceException;
    
    public int updateRefundPaymentExternallyLinkedWithAdvance(JSONObject paramJobj) throws ServiceException;
    
    public KwlReturnObject getRefundPaymentLinkDetailsLinkedWithAdvance(JSONObject paramJobj) throws ServiceException;

    public KwlReturnObject getRefundPaymentDetailsLinkedToAdvance(JSONObject paramJobj) throws ServiceException;
    
    public KwlReturnObject getAdvanceDetailInformationFromPaymentId(JSONObject paramJobj) throws ServiceException;

    public KwlReturnObject getTDSJEmappingTerm(String jeid, String companyid) throws ServiceException, JSONException;
    
    public KwlReturnObject saveReceiptInvoiceJEMapping(JSONObject params) throws ServiceException;

    public KwlReturnObject getReceiptInvoiceJEMapping(JSONObject paramObj) throws ServiceException, JSONException;

    public int deleteReceiptInvoiceJEMapping(JSONObject paramObj) throws ServiceException, JSONException;
    
    public KwlReturnObject checkTransactionsForDiscountOnPaymentTerms(JSONObject paramJobj) throws ServiceException;
    
    public List getOpeningSalesReceiptKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException;
    public List getSalesReceiptKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException;
}
