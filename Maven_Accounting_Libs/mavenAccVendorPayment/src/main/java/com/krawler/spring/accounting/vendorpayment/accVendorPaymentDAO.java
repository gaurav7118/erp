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
package com.krawler.spring.accounting.vendorpayment;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.*;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface accVendorPaymentDAO {

    public KwlReturnObject deleteBillingPaymentsDetails(String receiptid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteJEEntry(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteJEDtails(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject deleteBillingPaymentsDetailsOtherwise(String receiptid) throws ServiceException;

    public KwlReturnObject getBillingPaymentVendorNames(String companyid, String iD) throws ServiceException;

    public KwlReturnObject getPaymentVendorNames(String companyid, String iD) throws ServiceException;
    
    public int isFromTaxPayment(String companyid, String paymentNumber) throws ServiceException;

    public KwlReturnObject savePayment(HashMap<String, Object> hm) throws ServiceException;
    
    public Payment getPaymentObj(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject savePaymentObject(List<Payment> paymentList) throws ServiceException;
    
    public PayDetail saveOrUpdatePayDetail(HashMap hm) throws ServiceException;
    
    public KwlReturnObject getPaymentIdFromSRId(String srid, String companyid) throws ServiceException;
    
    public KwlReturnObject getPayments(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getOpeningBalancePayments(HashMap<String, Object> request) throws ServiceException;
    
    public int getOpeningBalancePaymentCount(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForPayments(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceTotalBaseAmountForPayments(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getAllOpeningBalancePayments(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject deletePayments(String paymentid, String companyid) throws ServiceException;

    public KwlReturnObject deletePaymentsDetails(String paymentid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteLinkPaymentsDetails(String paymentid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteLinkPaymentToCreditNoteDetails(String paymentid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteLinkPaymentToAdvancePaymentDetails(String paymentid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteAdvancePaymentsDetails(String paymentid, String companyid) throws ServiceException;
    
    public KwlReturnObject deletePaymentsDetailsAndUpdateAmountDue(String paymentid, String companyid, int editApprovalStatus) throws ServiceException;
    
    public KwlReturnObject deletePaymentsAgainstCNDN(String paymentid, String companyid,int beforeEditApprovalStatus) throws ServiceException;
    
    public KwlReturnObject deleteLinkDetailsAndUpdateAmountDue(Map<String, Object> requestMap,String paymentid, String companyid,boolean tempCheck) throws ServiceException;
    
    public KwlReturnObject deletePaymentsDetailsOtherwise(String paymentid) throws ServiceException;

    public KwlReturnObject savePaymentDetailOtherwise(HashMap<String, Object> requestParams) throws ServiceException;
    
    public PaymentDetailOtherwise getPaymentDetailOtherwiseObject(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deletePaymentEntry(String paymentid, String companyid) throws ServiceException,AccountingException;

    public KwlReturnObject getJEFromPayment(String paymentid) throws ServiceException;

    public KwlReturnObject deletePaymentPermanent(HashMap<String, Object> requestParams) throws AccountingException , ServiceException;

    public KwlReturnObject getPaymentsFromGReceipt(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject getPaymentsFromGReceipt(HashMap<String, Object> reqParams) throws ServiceException;
    
    public Map<String, List<PaymentDetail>> getPaymentsInfoFromGReceiptList(List<String> grlist) throws ServiceException;

    public KwlReturnObject getContraPayReceiptFromGReceipt(String receiptid, String companyid) throws ServiceException;
    
    public KwlReturnObject getPaymentFromNo(String pno, String companyid) throws ServiceException;

    public KwlReturnObject getPaymentEditCount(String entryNumber, String companyid, String paymentId) throws ServiceException;
    
    public KwlReturnObject getPaymentDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getVendorPaymentGlobalCustomData(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBillingPaymentDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPaymentDetailOtherwise(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCreditNotePaymentDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCalculatedMakePaymentOtherwiseTax(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCalculatedDebitNoteOtherwiseTax(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getVendorPaymentCustomData(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPaymentFromJE(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getPaymentLinkedInvoiceJE(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getBillingPaymentFromJE(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getBillingPaymentsFromGReceipt(String receiptid, String companyid) throws ServiceException;
    
    public KwlReturnObject saveVendorCnPaymenyHistory(String cnnoteid, double paidncamount,double originalamountdue,String paymentId) throws ServiceException;
    
    public KwlReturnObject saveVendorCnPaymenyHistory(HashMap<String,String> hashMap) throws ServiceException;
    
    public KwlReturnObject saveTDSdetailsRow(HashMap<String, Object> tdsRequestParams) throws ServiceException;
    
    public KwlReturnObject getVendorCnPaymenyHistory(String cnnoteid, double paidncamount,double originalamountdue,String paymentId) throws ServiceException;
    
    public KwlReturnObject getPaymentIdLinkedWithNote(String noteId) throws ServiceException;

    public KwlReturnObject getBillingPaymentFromNo(String pno, String companyid) throws ServiceException;

    public KwlReturnObject saveBillingPayment(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject getBillingPayments(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getJEBRMap(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getJEBURMap(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject deleteBillingPaymentEntry(String paymentid, String companyid) throws ServiceException;

    public KwlReturnObject getJEFromBillingPayment(String paymentid, String companyid) throws ServiceException;

    public KwlReturnObject saveBillingPaymentDetailOtherwise(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBillingPaymentDetailOtherwise(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getpaymenthistory(String paymentid) throws ServiceException;

    public KwlReturnObject updateCnUpAmount(String noteid, Double amount) throws ServiceException;

    public KwlReturnObject getaccountdetailsPayment(String accid) throws ServiceException;
    
    public KwlReturnObject getDuplicatePNforNormal(String entryNumber, String companyid, String receiptid, String advanceId,Payment payment)throws ServiceException;
    
    public KwlReturnObject getCurrentSeqNumberForAdvance(String sequenceformat, String companyid)throws ServiceException;
    
    public KwlReturnObject getVendorCnPayment(String paymentId) throws ServiceException;
    
    public KwlReturnObject getVendorCnPayment(String paymentId,String cnnoteid) throws ServiceException;
    
    public KwlReturnObject getVendorDnPayment(String paymentId) throws ServiceException;

    public KwlReturnObject saveGIROFileGenerationLog(HashMap<String, Object> dataMap) throws ServiceException;

    public Integer getSequenceNumberForGiro(String accCompanyId);

    public KwlReturnObject updatePaymentsIBGFlag(String companyId, String paymentId) throws ServiceException;
    
    public KwlReturnObject getAllCompanyPayments(String companyid) throws ServiceException;

    public KwlReturnObject getVendorDnPaymentWithAdvance(String paymentId) throws ServiceException;
    
    public Payment getPaymentObject(Payment payment) throws ServiceException;
    
    public KwlReturnObject getInvoiceAdvPaymentList(HashMap <String,String> payHashMap) throws ServiceException;
    
    public KwlReturnObject getVendorCnPaymentWithAdvance(String paymentId) throws ServiceException;
    
    public KwlReturnObject saveVendorDnPaymenyHistory(String cnnoteid, double paidncamount,double originalamountdue,String paymentId) throws ServiceException;
    
    public List<Payment> getPaymentList(String payments) throws ServiceException;

    public List<Payment> getPaymentListFromCompany(String companyId) throws ServiceException;
    
    public KwlReturnObject getPaymentAdvanceAmountDueDetails(HashMap<String, Object> request) throws ServiceException;
    
    public List getAdvancePaymentUsedInRefundReceipt(String advancedetailid) throws ServiceException;
    
    public List getAdvanceReceiptUsedInRefundPayment(String advancedetailid) throws ServiceException;
    
    public KwlReturnObject getBankChargeJEFromPayment(String paymentid) throws ServiceException;
   
    public KwlReturnObject getBankInterestJEFromPayment(String paymentid) throws ServiceException;
    
    public KwlReturnObject getLinkedDetailsPayment(HashMap<String, Object> reqParams1) throws ServiceException;
    
    public List<LinkDetailPayment> getDeletedLinkedPaymentInvoices(Payment payment,List<String> linkedDetailInvoice, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteSelectedLinkedPaymentInvoices(String paymentid, String linkedDetailIDs, String companyid, String unlinkedDetailIDs) throws ServiceException;
    
    public List<PayDetail> getPaymentDetails(String companyid) throws ServiceException;
    
    public List<PaymentDetailOtherwise> getPaymentDetailOtherwise(String paymentid) throws ServiceException;

    public List<AdvanceDetail> getAdvanceDetailsAgainstVendorForTDS(HashMap<String, Object> requestParams) throws ServiceException;

    public List<AdvanceDetail> getPaymentDetailAdvanced(String paymentid) throws ServiceException;
    public List<String> getTotalJEDIDPaymentDetailAdvanced(String paymentid,String companyid) throws ServiceException;
    public List<String> getTotalJEDIDPaymentDetails(String paymentid,String companyid) throws ServiceException;
    public List<String> getTotalJEDIDCreditNotePaymentDetails(String paymentid,String companyid) throws ServiceException;
    public KwlReturnObject getinvoiceDocuments(HashMap<String, Object> dataMap) throws ServiceException;
    
    
    public List getMulticurrencyPaymentsWithPCToPMCRateOne(String companyId) throws ServiceException;
    
    public KwlReturnObject updateCnAmount(String noteid, double amount) throws ServiceException;
    
    public KwlReturnObject saveRepeatMPInfo(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getRepeatePaymentDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject activateDeactivatePayment(String repeateid, boolean isactivate) throws ServiceException;
    
    public int DelRepeateJEMemo(String repeateid, String column) throws ServiceException;
    
    public KwlReturnObject getMPCount(String mpno, String companyid) throws ServiceException;
    
    public KwlReturnObject getRepeatPayment(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject savePaymentDetailOtherwise(List<PaymentDetailOtherwise> pdoList) throws ServiceException;
    
    public KwlReturnObject getRepeatePaymentNo(Date prevDate) throws ServiceException;
    
    public KwlReturnObject getCnPaymenyHistory(String companyid) throws ServiceException;
    
    public KwlReturnObject saveCreditNotePaymentDetails(HashMap<String,Object> datamap) throws ServiceException;
    
    public int DelRepeatePaymentChequeDetails(String repeateid) throws ServiceException;
    
    public KwlReturnObject getChequeDetailsForRepeatedPayment(String repeatPaymentId) throws ServiceException;

    public String UpdatePaymentEntry(Map<String, Object> seqNumberMap);

    public String UpdatePaymentEntryForNA(String pyid, String entrynumber);
    
    public KwlReturnObject getNormalPayments(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updatePayment(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPaymentLinkedCreditNoteJE(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteLinkPaymentDetailsToCreditNoteAndUpdateAmountDue(String paymentid, String companyid,boolean tempCheck) throws ServiceException ;
    
    public List<LinkDetailPaymentToCreditNote> getDeletedLinkedPaymentCreditNotes(Payment payment,String linkedDetailIDs, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteSelectedLinkedPaymentCreditNotes(String paymentid, String linkedDetailIDs, String companyid) throws ServiceException;
    
    public KwlReturnObject getAdvancePaymentIdLinkedWithNote(String noteId) throws ServiceException;
    
    public KwlReturnObject getLinkDetailPaymentToCreditNote(HashMap<String, Object> reqParams1) throws ServiceException;
    
    public KwlReturnObject getLinkDetailAdvanceReceiptToRefundPayment(HashMap<String, Object> reqParams1) throws ServiceException;
    
    public KwlReturnObject getLinkDetailReceiptToAdvancePayment(HashMap<String, Object> reqParams1) throws ServiceException;
    
    public KwlReturnObject approveRecurringMakePayment(String repeateid, boolean ispendingapproval) throws ServiceException;

    public KwlReturnObject activateDeactivateMakePayment(String repeateid, boolean isactivate) throws ServiceException;
    
    public int updateToNullRepeatedMPOfMakePayment(String invoiceid, String repeateid)throws ServiceException;
    
    public int deleteRepeatedMP(String repeateid)throws ServiceException;    
    
    public KwlReturnObject getAdvanceReceiptDetailsByPayment(HashMap<String, Object> reqParams1) throws ServiceException;
    
    public KwlReturnObject updateEntryInCreditNoteLinkingTable(HashMap<String, Object> reqParams) throws ServiceException;
     
    public KwlReturnObject savePaymentLinking(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject deleteLinkingInformationOfMP(HashMap<String, Object> requestParams) throws ServiceException;
       
    public KwlReturnObject getContraPayReceiptIDFromGReceipt(String receiptid, String companyid) throws ServiceException;
    
    public List<LinkDetailPaymentToAdvancePayment> getDeletedLinkedPaymentAdvancePayment(Payment payment,String linkedDetailIDs, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteSelectedLinkedPaymentAdvanceDetails(String paymentid, String linkedDetailIDs, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteLinkPaymentDetailsToAdvancePaymentAndUpdateAmountDue(String paymentid, String companyid,boolean tempCheck) throws ServiceException;

    public KwlReturnObject getTotalAmountofVendorpayment(HashMap<String, Object> requestParams) throws ServiceException;

    public List getAdvancePaymentLinkedWithRefundReceipt(String paymentid,String companyid) throws ServiceException;
 
    public KwlReturnObject updateCnOpeningAmountDue(String noteid, double amount) throws ServiceException ;
     
    public KwlReturnObject getInvoiceDetailsAndTDSDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getInvoiceDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteTDSDetails(HashMap<String, String> dataMap) throws ServiceException;
    
    public KwlReturnObject deleteTDSMasterRates(HashMap<String, String> dataMap) throws ServiceException;
    
    public KwlReturnObject ISTDSMasterRatesUsedInAdvancePayment(HashMap<String, String> dataMap) throws ServiceException;
    
    public KwlReturnObject ISTDSMasterRatesUsedInPI(HashMap<String, String> dataMap) throws ServiceException;
    
    public KwlReturnObject getTDSDeductionDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getTDSReportTransactionWise(HashMap<String, Object> requestParams) throws ServiceException;
    
    public int getTDSPaymentType(HashMap requestParams) throws ServiceException;
    
    public String getTDSPaymentNOP(HashMap requestParams) throws ServiceException;
    
    public KwlReturnObject getAccountOpeningBalanceTransactionFromNo(String transactionNumber, String companyid) throws ServiceException;
    
    public KwlReturnObject saveAccountOpeningTransaction(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getAccountOpeningBalanceTransaction(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject deleteAccountOpeningTransactionPemanently(String transactionID, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteChequeNumberPemanently(String chequeid, String companyid) throws ServiceException;
    
    public KwlReturnObject updateChequeNumber(String oldchequenumber, String newchequenumber, String companyid) throws ServiceException;
    
    public KwlReturnObject getChequeIDByNumber(String chequenumber, String companyid) throws ServiceException;
    
    public KwlReturnObject getPayDetailIDByChequeID(String chequeid, String companyid) throws ServiceException;
    
    public KwlReturnObject getPaymentMadeFromJE(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject JEForPaymentOfImportServiceInvoices(String jeid, String companyid) throws ServiceException;
    
    public void updateAdvancePaymentTDSPaidFlag(HashMap params);
    
    public void resetAdvancePaymentTDSPaidFlag(HashMap params);
    
    public double tdsCalculation (HashMap requestParams ) throws ServiceException;
    
    public int isFromTDSPayment(HashMap requestParams) throws ServiceException;
    
    public int CalculateOverDueByMonths(HashMap requestParams) throws ServiceException;
    
    public KwlReturnObject saveAdvanceDetailsTermMap(Map<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getAdvanceDetailsTerm(JSONObject paramObj) throws ServiceException;

    public void deleteAdvanceDetailsTerm(JSONObject reqParams) throws ServiceException;
}
