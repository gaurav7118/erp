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
package com.krawler.spring.accounting.creditnote;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CreditNote;
import com.krawler.spring.accounting.creditnote.dm.CreditNoteInfo;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accCreditNoteDAO {

    public KwlReturnObject addCreditNote(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject updateCreditNote(HashMap<String, Object> hm) throws ServiceException;
    
     public KwlReturnObject saveCreditNoteGstDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveCreditNoteLinking(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject deleteCreditNote(String cnid, String companyid) throws ServiceException,AccountingException;

    public KwlReturnObject deleteCreditNoteDetails(String cnid, String companyid) throws ServiceException;

    public KwlReturnObject deleteCreditTaxDetails(String cnid, String companyid) throws ServiceException;

    public KwlReturnObject deleteOpeningCreditNote(String cnid, String companyid) throws ServiceException;

    public KwlReturnObject deleteCreditNotesPermanent(HashMap<String, Object> requestParams) throws ServiceException,AccountingException;
    
    public KwlReturnObject deleteLinkingInformationOfCN(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteLinkingInformationOfCNAgainstDN(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getMakePaymentIdLinkedWithCreditNote(String noteId) throws ServiceException;
    
    public KwlReturnObject getCreditNotelinkedInDebitNote(String creditNoteId, String companyId) throws ServiceException;

    public KwlReturnObject getCreditNoteIdFromPaymentId(String paymentid) throws ServiceException ;

    public KwlReturnObject getInvoicesLinkedWithCreditNote(String noteId, String companyId) throws ServiceException;

    public KwlReturnObject getCreditNoteMerged(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getOpeningBalanceCNs(HashMap<String, Object> request) throws ServiceException;
    
    public int getOpeningBalanceCNCount(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForCNs(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceTotalBaseAmountForCNs(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getOpeningBalanceVendorCNs(HashMap<String, Object> request) throws ServiceException;
    
    public int getOpeningBalanceVendorCNCount(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForVendorCNs(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceTotalBaseAmountForVendorCNs(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getCreaditNote(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getCreaditNoteVendor(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getCNFromNoteNo(String noteno, String companyid) throws ServiceException;

    public KwlReturnObject getCNSequenceNo(String companyid, Date applydate) throws ServiceException;
    
    public KwlReturnObject getCNSequenceNofromsequenceformat(String companyid, String sequenceformatid) throws ServiceException;

    public KwlReturnObject getCreditNoteDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCreditNoteDetailsGst(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCNDetailsCustomData(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject geCreditNoteCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject checkEntryForCreditNoteInLinkingTable(String creditNoteId, String invoiceId) throws ServiceException;

    public KwlReturnObject getCNFromInvoice(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getCNFromInvoiceOtherwise(String invoiceid, String companyid,boolean includeTempDeleted) throws ServiceException;

    public KwlReturnObject getCNDetailsFromOpeningBalanceInvoice(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getCNRowsDiscountFromInvoice(String invoiceid) throws ServiceException;

    public KwlReturnObject getDistinctCNFromInvoice(String invoiceid) throws ServiceException;

    public KwlReturnObject getCNRowsFromInvoice(String invoiceid) throws ServiceException;

    public KwlReturnObject getCNRowsFromInvoice(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject getCNRowsFromDebitNote(HashMap<String, Object> reqParams) throws ServiceException;

    public Map<String, List<CreditNoteInfo>> getCNRowsInfoFromInvoice(List<String> invoiceIDLIST) throws ServiceException;

    public KwlReturnObject getCNRowsOpen_customer(String invoiceid) throws ServiceException;

    public KwlReturnObject getDNRowsOpen_customer(String invoiceid) throws ServiceException;

    public KwlReturnObject getCNFromJE(String jeid, String companyid) throws ServiceException;

    public void deletePartyJournalCN(String cnid, String companyid) throws ServiceException,AccountingException;
    
    public void deletePartyJournalCNDNTemporary(HashMap<String, Object> reqParams)throws ServiceException;
    
    public KwlReturnObject getBCNFromJE(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getJEFromCN(String cnid) throws ServiceException;

    public KwlReturnObject getCNDFromCN(String cnid) throws ServiceException;

    public KwlReturnObject getCNDFromCND(String cnid) throws ServiceException;

    public KwlReturnObject getCNIFromCND(String cnid) throws ServiceException;

    public KwlReturnObject getBillingCreditNoteDet(String bInvid, String companyid) throws ServiceException;

    public KwlReturnObject getBillingCreditNoteDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBCNFromNoteNo(String noteno, String companyid) throws ServiceException;

    public KwlReturnObject getBCNSequenceNo(String companyid, Date applydate) throws ServiceException;

    public KwlReturnObject saveBillingCreditNote(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject getBillingCreaditNote(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getJEFromBCN(String cnid) throws ServiceException;

    public KwlReturnObject getCNDFromBCN(String cnid) throws ServiceException;

    public KwlReturnObject getCNDFromBCND(String cnid) throws ServiceException;

    public KwlReturnObject deleteBillingCreditNote(String cnid, String companyid) throws ServiceException;

    public KwlReturnObject getCNRowsDiscountFromBillingInvoice(String invoiceid) throws ServiceException;

    public KwlReturnObject getTotalTax_TotalDiscount(String cnid) throws ServiceException;

    public KwlReturnObject getTotalTax_TotalDiscount_Billing(String cnid) throws ServiceException;

    public KwlReturnObject getDOIDFromInvoiceDetails(String soid) throws ServiceException;

    public KwlReturnObject getNoteType(HashMap<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject deleteNoteType(int typeid) throws ServiceException;

    public KwlReturnObject saveNoteTypes(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveCreditNoteTermMap(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getCreditNoteTermMap(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getCNFromNoteNoAndId(String entryNumber, String companyid, String creditNoteId) throws ServiceException;
    
    public KwlReturnObject getCNLinkedWithCustomerInvoice(String creditNoteId, String companyid) throws ServiceException;
    
    public KwlReturnObject getCNLinkedWithPayment(String creditNoteId, String companyid) throws ServiceException;
    
    //get creditnoted id generated by sales return
    public KwlReturnObject getCreditNoteIdFromSRId(String srid, String companyid) throws ServiceException;
    
    public KwlReturnObject getinvoicesLinkedWithSR(String srid) throws ServiceException;
    
    public KwlReturnObject getVendorCnPayment(String paymentId) throws ServiceException;
    
    public KwlReturnObject getJEFromCNDetail(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject getCreditTaxDetails(HashMap<String, Object> paramsTaxDetails) throws ServiceException;

    public KwlReturnObject getCreditNoteAgainstVendorGst(String cnid, String companyid) throws ServiceException;

    public KwlReturnObject getCreditNoteIdFromPaymentIdLedger(String paymentid) throws ServiceException ;

    
    public List getForeignGainLossJE(String cnid, String companyid) throws ServiceException;
    
    public String updateCreditNoteEntryNumber(Map<String, Object> seqNumberMap);

    public KwlReturnObject getDeletedLinkedInvoices(CreditNote cn, String linkedDetailIDs, String companyId) throws ServiceException;
    
    public KwlReturnObject getDeletedLinkedDebitNotes(CreditNote cn, String linkedDetailIDs, String companyId) throws ServiceException;
    
    public KwlReturnObject deleteSelectedLinkedInvoices(String cnid, String linkedDetailIDs, String companyid,String unlinkedDetailIDs) throws ServiceException;
    
    public KwlReturnObject deleteSelectedLinkedDebitNotes(String cnid, String linkedDetailIDs, String companyid,String unlinkedDetailIDs) throws ServiceException;
    
    public String updateCNEntryNumberForNA(String prid, String entrynumber);
    
    public KwlReturnObject getCreditNotesForJE(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getNormalCreditNotes(HashMap<String, Object> requestParams) throws ServiceException;
    
     public KwlReturnObject getAdvancePaymentIdLinkedWithCreditNote(String noteId) throws ServiceException;
     
     public KwlReturnObject getLinkDetailPaymentToCreditNote(HashMap<String, Object> reqParams1) throws ServiceException;
     
     public KwlReturnObject getCreditNoteTaxEntryForSalesPersonCommissionDimensionReport(HashMap<String, Object> request) throws ServiceException;
     
    public KwlReturnObject checkEntryForTransactionInLinkingTableForForwardReference(String moduleName, String docid) throws ServiceException; 
    
    public boolean isCreditNoteLinkedToOtherTransaction(String moduleName, String docid) throws ServiceException ;
    
    public KwlReturnObject approvePendingCreditNote(String cnID, String companyid, int status) throws ServiceException;

    public KwlReturnObject rejectPendingCreditNote(String iD, String companyid)throws ServiceException;
    
    public KwlReturnObject getCreditNoteFromJE(String jeid, String companyid) throws ServiceException;
    
     public KwlReturnObject getCNDetailsUsingAdvanceSearch(HashMap<String, Object> requestParams) throws ServiceException;
     
    public boolean checkCNLinking(String creditNoteId) throws ServiceException;

    public KwlReturnObject getCNWithTax(HashMap<String,Object> map) throws ServiceException;

    public KwlReturnObject getCustomerCreditNoCount(String creditNoteNo, String companyid, String customerId) throws ServiceException;

    public KwlReturnObject getVendorCreditNoCount(String creditNoteNo, String companyid, String vendorId) throws ServiceException;
    
    public KwlReturnObject getCreditNoteLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException ;
    
    public KwlReturnObject saveCreditNoteInvoiceMappingInfo(JSONObject json) throws ServiceException;

    public KwlReturnObject getCreditNoteInvoiceMappingInfo(JSONObject json) throws ServiceException;
            
    public KwlReturnObject deleteCreditNoteInvoiceMappingInfo(JSONObject json) throws ServiceException;
    
    public KwlReturnObject saveCreditNoteDetailTermMap(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getCreditNoteDetailTermMap(JSONObject paramObj) throws ServiceException;
    
    public void deleteCreditNoteDetailTermMap(String advanceDetailId) throws ServiceException;
    public void deleteCreditNoteDetailTermMapAgainstDebitNote(String creditNoteID, String companyid) throws ServiceException;
    public void deleteGstTaxClassDetails(String docrefid) throws ServiceException;
    
    public KwlReturnObject deleteCreditNoteForOverchargeDetails(String dnId, String companyId) throws ServiceException;
    
    public void saveCreditNoteOverchargeAmountLinking(JSONObject paramJObj) throws ServiceException;
    
    public List getCreditNoteOverchargeAmountLinking(JSONObject paramJObj) throws ServiceException;
    
    public void deleteCreditNoteOverchargeAmountLinking(JSONObject paramJObj) throws ServiceException;
    public List getCNKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException;
    public List getOpeningCNKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException;
}
