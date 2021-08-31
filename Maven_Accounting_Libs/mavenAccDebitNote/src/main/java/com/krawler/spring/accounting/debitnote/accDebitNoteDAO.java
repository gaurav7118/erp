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
package com.krawler.spring.accounting.debitnote;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.DebitNote;
import com.krawler.hql.accounting.DebitNoteDetail;
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
public interface accDebitNoteDAO {

    public KwlReturnObject addDebitNote(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject saveDebitNoteGstDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject updateDebitNote(HashMap<String, Object> hm) throws ServiceException;
    
     public KwlReturnObject saveDebitNoteLinking(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject deleteDebitNote(String dnid, String companyid) throws ServiceException,AccountingException;

    public KwlReturnObject deleteOpeningDebitNote(String dnid, String companyid) throws ServiceException;

    public KwlReturnObject deleteDebitNoteDetails(String dnid, String companyid) throws ServiceException;

    public KwlReturnObject deleteDebitTaxDetails(String dnid, String companyid) throws ServiceException;

    public KwlReturnObject deleteDebitNotesPermanent(HashMap<String, Object> requestParams) throws ServiceException,AccountingException;
    
    public KwlReturnObject deleteLinkingInformationOfDN(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteLinkingInformationOfDNAginstCN(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getDebitNoteDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDebitNoteDetailsGst(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject geDebitNoteCustomData(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBillingDebitNoteDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getDebitNoteMerged(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getOpeningBalanceCustomerDNs(HashMap<String, Object> request) throws ServiceException;
    
    public int getOpeningBalanceCustomerDNCount(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForCustomerDNs(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getOpeningBalanceTotalBaseAmountForCustomerDNs(HashMap<String, Object> request) throws ServiceException;
                
    public KwlReturnObject getOpeningBalanceDNs(HashMap<String, Object> request) throws ServiceException;
    
    public int getOpeningBalanceDNCount(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForDNs(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getOpeningBalanceTotalBaseAmountForDNs(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getDebitNotes(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getDebitNotesCustomers(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getDNFromNoteNo(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject getDNSequenceNo(String companyid, Date applydate) throws ServiceException;

    public KwlReturnObject getDNFromGReceipt(String receiptid) throws ServiceException;
    
    public KwlReturnObject getDNFromGoodsReceiptOtherwise(String goodsReceiptId, String companyid) throws ServiceException;
    
    public KwlReturnObject getDNLinkedWithGoodsReceipts(String debitNoteId, String companyid) throws ServiceException;

    public KwlReturnObject getDNLinkedWithPayment(String debitNoteId, String companyid) throws ServiceException;

    public KwlReturnObject getDistintDNFromGReceipt(String receiptid) throws ServiceException;

    public KwlReturnObject getDNDetailsFromGReceipt(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject getDNDetailsFromGReceiptOtherwise(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject getDNDetailsFromOpeningBalanceGR(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject getDNFromJE(String jeid, String companyid) throws ServiceException;

    public void deletePartyJournalDN(String dnid, String companyid) throws ServiceException,AccountingException;

    public KwlReturnObject getBDNFromJE(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getJEFromDN(String dnid) throws ServiceException;

    public KwlReturnObject getDNDFromDN(String dnid) throws ServiceException;

    public KwlReturnObject getDNDIFromDN(String dnid) throws ServiceException;

    public KwlReturnObject getDNDInvFromDN(String dnid) throws ServiceException;

    public KwlReturnObject getDNRFromBDN(String receiptid) throws ServiceException;

    public KwlReturnObject getBDNDetailsFromGReceipt(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject getBDNFromNoteNo(String noteno, String companyid) throws ServiceException;

    public KwlReturnObject getBDNSequenceNo(String companyid, Date applydate) throws ServiceException;

    public KwlReturnObject saveBillingDebitNote(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject getBillingDebitNotes(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject deleteBillingDebitNote(String bdnid, String companyid) throws ServiceException;

    public KwlReturnObject getJEFromBDN(String dnid, String companyid) throws ServiceException;

    public KwlReturnObject getDNDFromBDN(String dnid, String companyid) throws ServiceException;

    public KwlReturnObject getDNDFromBDND(String bdnid, String companyid) throws ServiceException;

    public KwlReturnObject getTotalDiscountAndQty(String receiptid) throws ServiceException;
//    public KwlReturnObject getTotalQty(String invId) throws ServiceException;

    public KwlReturnObject getTotalTax_TotalDiscount(String dnid) throws ServiceException;

    public KwlReturnObject getTotalTax_TotalDiscount_Billing(String dnid) throws ServiceException;

    public KwlReturnObject getGDOIDFromVendorInvoiceDetails(String soid) throws ServiceException;

    public KwlReturnObject getDNRowsOpen_vendor(String vendorid) throws ServiceException;

    public KwlReturnObject getCNRowsOpen_vendor(String vendorid) throws ServiceException;

    public KwlReturnObject getDNRowsFromVendorInvoice(String veninvoiceid) throws ServiceException;

    public KwlReturnObject getDNRowsFromVendorInvoice(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject getDNRowsFromCreditNote(HashMap<String, Object> reqParams) throws ServiceException;

    public Map<String, List<DebitNoteDetail>> getDNRowsFromVendorInvoiceList(List<String> invoiceIDLIST) throws ServiceException;

    public KwlReturnObject saveDebitNoteTermMap(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getDebitNoteTermMap(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getReceivePaymentIdLinkedWithDebitNote(String noteId) throws ServiceException;
    
    public KwlReturnObject getDebitNotelinkedInCreditNote(String creditNoteId, String companyId) throws ServiceException;

    public KwlReturnObject getDebitNoteIdFromReceiptId(String receiptid) throws ServiceException;

    public KwlReturnObject getVendorInvoicesLinkedWithDebitNote(String noteId, String companyId) throws ServiceException;

    public KwlReturnObject getDNFromNoteNoAndId(String entryNumber, String companyid, String debitNoteId) throws ServiceException;
    
    public KwlReturnObject getDebitNoteIdFromPRId(String prid, String companyid) throws ServiceException;
    
    public KwlReturnObject getCustomerDnPayment(String paymentId) throws ServiceException;
    
    public KwlReturnObject getJEFromDNDetail(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject getDebitTaxDetails(HashMap<String, Object> paramsTaxDetails) throws ServiceException;

    public KwlReturnObject getDebitNoteAgainstCustomerGst(String dnid, String companyid) throws ServiceException;

    public KwlReturnObject getDebitNoteIdFromReceiptIdLedger(String receiptid) throws ServiceException;
    
    public List getForeignGainLossJE(String dnid, String companyid) throws ServiceException;
    
    public List getCreditNoteDetailsForDebitNote(String cnId, String companyId) throws ServiceException;
    
    public KwlReturnObject checkEntryForTransactionInLinkingTableForForwardReference(String moduleName, String docid) throws ServiceException; 

    public boolean isDebitNoteLinkedToOtherTransaction(String moduleName, String docid) throws ServiceException ;

    public String updateDeditEntryNumber(Map<String, Object> seqNumberMap);
    
    public String updateDNEntryNumberForNA(String prid, String entrynumber) ;
    
    public KwlReturnObject getDebitNotesForJE(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDeletedLinkedInvoices(DebitNote dn, String linkedDetailIDs, String companyId) throws ServiceException;
    
    public KwlReturnObject getDeletedLinkedCreditNotes(DebitNote dn, String linkedDetailIDs, String companyId) throws ServiceException;
    
    public KwlReturnObject deleteSelectedLinkedInvoices(String dnid, String linkedDetailIDs, String companyid, String unlinkedDetailIDs) throws ServiceException;
    
    public KwlReturnObject deleteSelectedLinkedCreditNotes(String dnid, String linkedDetailIDs, String companyid, String unlinkedDetailIDs) throws ServiceException;
    
    public KwlReturnObject getNormalDebitNotes(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAdvanceReceivePaymentIdLinkedWithDebitNote(String noteId) throws ServiceException;
    
    public KwlReturnObject getLinkDetailReceiptToDebitNote(HashMap<String, Object> reqParams1) throws ServiceException;
    
    public KwlReturnObject checkEntryForDebitNoteInLinkingTable(String debitNoteId, String invoiceId) throws ServiceException;
    
    public KwlReturnObject approvePendingDebitNote(String cnID, String companyid, int status) throws ServiceException;

    public KwlReturnObject rejectPendingDebitNote(String iD, String companyid)throws ServiceException;
    public KwlReturnObject getDebitNoteFromJE(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject getDNDetailsUsingAdvanceSearch(HashMap<String, Object> requestParams) throws ServiceException;
    
    public boolean checkDNLinking(String DebitnoteID) throws ServiceException;
    
    public KwlReturnObject getDNWithTax(HashMap<String,Object> map) throws ServiceException;
    
    public KwlReturnObject getTDSAppliedDebitNote(Map<String, Object> requestParams) throws ServiceException;
    
    public void updateDebitNoteTDSPaidFlag(HashMap params);
    
    public void resetDebitNoteTDSPaidFlag(HashMap params);
    
    public KwlReturnObject saveDebitNoteInvoiceMappingInfo(JSONObject json) throws ServiceException;
    
    public KwlReturnObject getDebitNoteInvoiceMappingInfo(JSONObject json) throws ServiceException;
    
    public KwlReturnObject deleteDebitNoteInvoiceMappingInfo(JSONObject json) throws ServiceException;
    
    public KwlReturnObject saveDebitNoteDetailTermMap(Map<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getDebitNoteDetailTermMap(JSONObject paramObj) throws ServiceException;

    public void deleteDebitNoteDetailTermMap(String creditNoteTaxEntry) throws ServiceException;
    public void deleteDebitNoteDetailTermMapAgainstDebitNote(String debitNoteID, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteDebitNoteForOverchargeDetails(String dnId, String companyId) throws ServiceException;
    
    public void deleteGstTaxClassDetails(String docrefid) throws ServiceException;

    public List getDNKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException;
    public List getOpeningDNKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException;
}
