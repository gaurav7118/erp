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
package com.krawler.spring.accounting.invoice;

import com.krawler.common.admin.AdditionalMemo;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.Projreport_Template;
import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.invoice.dm.InvoiceInfo;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.velocity.exception.ParseErrorException;

/**
 *
 * @author krawler
 */
public interface accInvoiceDAO {
    
    public Map<String, InvoiceInfo> getInvoiceList(List<String> invoiceIDLIST);
    public Map<String, JournalEntry> getInvoiceJEList(List<String> invoiceIDLIST);
    public Map<String, Tax> getInvoiceTaxList(List<String> invoiceIDLIST);
    public Map<String, Term> getInvoiceTermList(List<String> invoiceIDLIST);
    public Map<String, Projreport_Template> getInvoiceTemplateList(List<String> invoiceIDLIST);
    public Map<String, JournalEntryDetail> getInvoiceCustomerEntryList(List<String> invoiceIDLIST);
    KwlReturnObject getCalculatedInvDtlTax(Map<String, Object> filterParams) throws ServiceException;
    @Deprecated
    KwlReturnObject getCalculatedCNTax(Map<String, Object> filterParams) throws ServiceException;
//    public KwlReturnObject getInvoiceObj(String invoiceid);
    public KwlReturnObject saveInvoice(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject saveBadDebtInvoiceMapping(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveTaxAdjustment(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject updateTaxAdjustment(HashMap<String, Object> dataMap) throws ServiceException;
    
    public String updateInvoiceEntryNumberForNewSI(Map<String, Object> seqNumberMap);
    
    public KwlReturnObject deleteTaxAdjustment(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject addInvoice(JSONObject json, HashSet details) throws ServiceException;
    public KwlReturnObject updateInvoice(JSONObject json, HashSet details) throws ServiceException;   
   // public boolean mapProductTaxToInvoiceTax(HashMap<String, String> requestParams,InvoiceDetail invoicedetail) throws ServiceException;
    public KwlReturnObject deleteInvoice(String invoiceid, String companyid) throws ServiceException;
    public KwlReturnObject deleteAssetDetailsLinkedWithInvoice(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteInvoiceContractMappings(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteInvoicePermanent(HashMap<String, Object> requestParams) throws ServiceException,AccountingException;
    public List getCompanyUnit(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteLinkingInformationOfSI(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteLinkingInformationOfDO(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteLinkingInformationOfSR(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteInvoiceDtails(String invoiceid, String companyid) throws ServiceException,AccountingException;
    public KwlReturnObject getInvoices(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getCompanyInvoices(String companyid) throws ServiceException;
    public KwlReturnObject getDeliverOrderDetails(String doId,String companyId) throws ServiceException;
    public KwlReturnObject getDeliverOrderDetailsForBulkInvoices(List doId,String companyId) throws ServiceException;
    public KwlReturnObject getInvoicesForSalesCommission(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getInvoicesForBrandCommission(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getDeliveryOrderForMap(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getInvoicesMerged(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getUninvoicedDos(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject WeeklyCashFlowUnPaidInvoices(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getOpeningBalanceInvoices(HashMap<String, Object> requestParams) throws ServiceException;
    public int getOpeningBalanceInvoiceCount(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForInvoices(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getOpeningBalanceTotalBaseAmountForInvoices(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getOpeningBalanceInvoicesExcludingNormalInvoices(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getRevalFlag(String receiptId) throws ServiceException;
    public KwlReturnObject getSelectedInvoicesMerged(HashMap<String, Object> requestParams, String invoiceIds) throws ServiceException;
    public KwlReturnObject getCustomerLedgerReport(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getCustomerLedgerOpeningTransactionReport(HashMap<String, Object> request) throws ServiceException;
//    public KwlReturnObject getCustomerLedgerReportNew(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getVendorLedgerReport(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getVendorLedgerOpeningTransactionReport(HashMap<String, Object> request) throws ServiceException;
//    public KwlReturnObject getVendorLedgerReportNew(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getInvoiceCount(String invoiceno, String companyid) throws ServiceException;
    public KwlReturnObject deleteInvoiceEntry(String invoiceid, String companyid) throws ServiceException,AccountingException;
    public KwlReturnObject getJEFromInvoice(String receiptid) throws ServiceException;
    public KwlReturnObject getInvoiceDiscount(String invoiceid) throws ServiceException;
    public KwlReturnObject getInvoiceDetailsDiscount(String invoiceid) throws ServiceException;
    public KwlReturnObject getInvoiceInventory(String invoiceid) throws ServiceException;
    public KwlReturnObject getDeliveryOrderInventory(String doid) throws ServiceException;
    public KwlReturnObject getDeliveryOrderBatches(String doid,String companyid) throws ServiceException;
    public KwlReturnObject getRepeateInvoices(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getExcludedInvoices(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject saveRepeateInvoiceInfo(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getRepeateInvoicesDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getRepeateInvoicesDetailsForExpander(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getInvoicesContainingProject(HashMap<String, Object> dataMap) throws ServiceException;
    //Neeraj change-Customer Quotation
    public KwlReturnObject getQuotationDetails(HashMap<String, Object> requestParams) throws ServiceException;    
    public KwlReturnObject getInvoiceDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getSalesInvoiceExciseDetails(String invoiceid) throws ServiceException;
    public KwlReturnObject getSalesInvoiceExciseDetailsHQL(String invoiceid) throws ServiceException;
    public KwlReturnObject getInvoiceDetailsCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getOpeningInvoiceDetailsCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    public int getInvoiceDetailsCustomDataRecCount(String companyid, String recid) throws ServiceException;
    public int getInvoiceDetailsCustomDataJECount(String companyid, String recid) throws ServiceException;
    public KwlReturnObject getInvoiceDetailsCustomDataForProduct(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getInvoiceDetailsCustomDataForProductNew(HashMap<String, Object> requestParams) throws ServiceException;
    KwlReturnObject getCalculatedInvTax(Map<String, Object> requestParams) throws ServiceException;
    KwlReturnObject getCalculatedVHT(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getInvoiceProductDetails(String productid, Date fromDate,Date toDate,HashMap<String, Object> requestParams1) throws ServiceException ;
    public KwlReturnObject getDOByInventoryID(String inventoryid) throws ServiceException ;
    public KwlReturnObject getDOdBySodID(String inventoryid) throws ServiceException ;
    public KwlReturnObject getUsersByProducts(HashMap<String, Object> requestParams) throws ServiceException ;
    public KwlReturnObject getUsersByProductRevenue(HashMap<String, Object> requestParams) throws ServiceException ;
    public KwlReturnObject getProductsByUsers(HashMap<String, Object> requestParams) throws ServiceException ;
    public KwlReturnObject getProductRevenueByUsers(HashMap<String, Object> requestParams) throws ServiceException ;
    
    public KwlReturnObject getBuildAssemblyJEs(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getIDFromSOD(String soid) throws ServiceException;
    
    public KwlReturnObject getInvoicesLinkedInSO(String soid) throws ServiceException;
    
     public KwlReturnObject getfirstPartialInvoiceOfSO(String invoiceIds) throws ServiceException;
    
    public KwlReturnObject getVenInvIDFromPOD(String poid,String comapny) throws ServiceException;
    
    
    public KwlReturnObject getSODFromQD(String soid) throws ServiceException;
    public KwlReturnObject getPODFromSOD(String soid) throws ServiceException;
    public KwlReturnObject getINVDFromQD(String soid) throws ServiceException;
    public KwlReturnObject getDOIDFromSOD(String soid,String doid) throws ServiceException;
    
    public KwlReturnObject getSRFromSOD(String soid) throws ServiceException;
    
    public KwlReturnObject updateSalesReturnStatus(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getProductReplacementIDFromSOD(String soid) throws ServiceException;
    public KwlReturnObject getQuotationDetailsFromProductReplacementID(String productReplacementId) throws ServiceException;
    public KwlReturnObject getDOIDFromInvoiceDetails(String soid) throws ServiceException;
    public KwlReturnObject getSalesReturnIDFromInvoiceDetails(String invId) throws ServiceException;
    public KwlReturnObject getSalesReturnIDFromDODetails(String dodid) throws ServiceException;
    public KwlReturnObject getSalesReturnIDFromDODetailsForFullReturnCase(String dodid) throws ServiceException ;
    public KwlReturnObject getOutstandingBatchSerialDetailBySalesReturnDetailId(Product product,String srdId,String doDetailId) throws ServiceException ;
    public KwlReturnObject getPartialFullSalesReturnDetailsByDOId(String companyID,String dodid) throws ServiceException;
    public KwlReturnObject getInvoiceFromJE(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getDeliveryOrderFromJE(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getINVDFromDOD(String DoDid) throws ServiceException;
    public KwlReturnObject getDODFromINVD(String INVDid) throws ServiceException;
    public double getInvoiceQuantityFromDOD(String dodID) throws ServiceException;
    public KwlReturnObject getInvoice_Product(Map<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject getCalculatedInvDtlTaxDistinct(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCalculatedInvDtlTaxDistinctBilling(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPendingDO(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getAllUninvoicedConsignmentDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public double getDoQuantityWhoseInvoiceCreated(HashMap<String, Object> requestParams) throws ServiceException;
    public double getReturnQuantity(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject approvePendingDO(String doID, String companyid, int status) throws ServiceException;
    public KwlReturnObject rejectPendingDO(String doID, String companyid) throws ServiceException;
    public void releaseBatchSerialData(String doID, String companyid) throws ServiceException;
    public KwlReturnObject getDeliveryOrdersMerged(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteDeliveryOrder(String soid, String companyid) throws ServiceException;
    public KwlReturnObject deleteAssetDetailsLinkedWithDeliveryOrder(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteDOContractMappings(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteDeliveryOrdersPermanent(HashMap<String, Object> requestParams) throws ServiceException, AccountingException ;
    public int deleteDOWithPickPackandShip(HashMap<String, Object> requestParams) throws ServiceException, AccountingException ;
    public void closeDeliveryOrdersPermanent(HashMap<String, Object> requestParams,String company) throws ServiceException;
    public KwlReturnObject deleteDeliveryOrderDetails(String doid, String companyid) throws AccountingException;
    
    public KwlReturnObject getStockStoreTransferFromDO(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteStockStoreLoactionDOMapping(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject updateSOBalanceQtyAfterDO(String doid, String linkedDocumentID, String companyid) throws AccountingException;
    
    public KwlReturnObject updateSOBalanceQtyAfterSR(String srid, String companyid) throws AccountingException;
    
    public KwlReturnObject getCalculatedDODtlTax(Map<String, Object> requestParams) throws ServiceException; 
    
    public KwlReturnObject getDeliveryOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteDeliveryOrdersBatchSerialDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteInvoicesBatchSerialDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getDeliveryOrdersBatchDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteSalesReturnsBatchSerialDetails(HashMap<String, Object> requestParams) throws ServiceException;
    //ERP-38641 :Add proper check while delete Sales return transaction for stock
    public JSONObject getQuantityDueToDeleteSalesReturn(Company company, String referenceId) throws ServiceException; 
    public JSONObject getTransactionsToDeleteSalesReturn(Company company, String referenceId) throws ServiceException;
    public KwlReturnObject getSalesReturnsBatchDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject saveDeliveryOrderStatus(String doId, String status) throws ServiceException;    
    public KwlReturnObject saveDeliveryOrder(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject updateInvoiceUsingSet(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject updateDeliveryOrder(DeliveryOrder deliveryOrder) throws ServiceException;
    public KwlReturnObject updateDeliveryOrderSetNull(DeliveryOrder deliveryOrder) throws ServiceException;
    public KwlReturnObject saveDeliveryOrderDetails(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getDeliveryOrderCount(String orderno, String companyid) throws ServiceException;
    public KwlReturnObject getInvoiceNoCount(String invoiceNo, String companyid,String customerId) throws ServiceException;
    public KwlReturnObject getCustomerDebitNoCount(String invoiceNo, String companyid,String customerId) throws ServiceException;
    public KwlReturnObject getVendorDebitNoCount(String invoiceNo, String companyid,String vendorId) throws ServiceException;
    public KwlReturnObject getDebitNoteLinkedWithInvoice(String invoiceId, String companyId)throws ServiceException;
    public KwlReturnObject getCreditNoteForOverchargeLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException;
    public KwlReturnObject getSalesInvoiceLinkedWithDebitNote(String billid, String companyid)throws ServiceException;
    public KwlReturnObject getInvoiceLinkedWithCreditNote(String billid, String companyid)throws ServiceException;
    public KwlReturnObject getPurchaseInvoiceLinkedWithOverchargeDebitNote(String debitNoteId, String companyId) throws ServiceException;
    public KwlReturnObject getInvoiceLinkedWithOverchargeCreditNote(String creditNoteId, String companyId) throws ServiceException;
    public boolean getSalesorderStatus(String orderno,String companyid) throws ServiceException;
    public boolean getSalesorderBatchStatus(String sodid,String companyid) throws ServiceException;
    public boolean getSalesorderSerialStatus(String sodid,String companyid) throws ServiceException;
    public KwlReturnObject updateSOLockQuantity(String sodid,double dquantity,String companyid) throws ServiceException;
    public KwlReturnObject updateAssemblyLockQuantity(String sodid,double dquantity) throws ServiceException;
    public KwlReturnObject getAssemblySubProductList(String sodProductidid) throws ServiceException;
    public KwlReturnObject getDODetailsFromSR(String doid, String companyid) throws ServiceException;//function to check delivery order used in sales return
    public KwlReturnObject getDOFromInvoice(String invoiceid, String companyid) throws ServiceException; 
    public KwlReturnObject getDOFromInvoiceNew(String invoiceid, String companyid) throws ServiceException; 
    public KwlReturnObject getSRFromInvoice(String invoiceid, String companyid) throws ServiceException; 
    public KwlReturnObject getInvoiceFromDeliveryPlanner(String invoiceid, String companyid) throws ServiceException; 
    
    public KwlReturnObject getBadDebtInvoiceMappingForInvoice(HashMap<String, Object> requestParams) throws ServiceException; 
    
    public KwlReturnObject saveinvoiceDocuments(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getinvoiceDocuments(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject deleteinvoiceDocument(String documentID) throws ServiceException;
    public KwlReturnObject deleteTemporaryAndPermanentInvoiceDocument(JSONObject requestParams) throws ServiceException;
    public KwlReturnObject getAttachedDocumentDetailsFromTransactionId(String transactionID, String companyId) throws ServiceException;
    public KwlReturnObject getIDFromDOD(String soid) throws ServiceException;
    public KwlReturnObject getSalesByCustomer(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getCustomerRevenue(HashMap<String, Object> request) throws ServiceException;
    public int approvePendingInvoice(String qid, boolean isbilling, String companyid, String userid) throws ServiceException;
    public KwlReturnObject rejectPendingInvoice(String invID, String companyid) throws ServiceException;
    public int pendingApprovalInvoicesCount(String companyid) throws ServiceException;
    public KwlReturnObject getSalesReturnCount(String orderno, String companyid) throws ServiceException;
    public KwlReturnObject getSalesReturnInventory(String doid) throws ServiceException;
    public KwlReturnObject getSalesReturnBatches(String srid,String companyid) throws ServiceException;
    public KwlReturnObject deleteSalesReturnDetails(String doid, String companyid) throws ServiceException;
    public KwlReturnObject saveSalesReturn(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject saveSalesReturnDetails(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getSalesReturn(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getSalesReturnDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteSalesReturn(String srid, String companyid) throws ServiceException;
    public KwlReturnObject deleteSalesReturnPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteAssetDetailsLinkedWithSalesReturn(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getIDFromSRD(String soid) throws ServiceException;
    public KwlReturnObject getDODIDFromSRD(String soid,String salesreturnId) throws ServiceException;
    public KwlReturnObject getDOFromSR(String invoiceid, String companyid) throws ServiceException;
    public KwlReturnObject getDOFromInv(String invoiceid, String companyid) throws ServiceException;
    public KwlReturnObject getIDFromDODFORSR(String soid) throws ServiceException;
    public KwlReturnObject getUnInvoicedDeliveryOrders(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject updateDeliveryOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;    
    public KwlReturnObject updateSOLinkflag(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject saveInvoiceLinking(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject checkEntryForInvoiceInLinkingTable(String docid, String linkeddocid) throws ServiceException;
    public KwlReturnObject checkEntryForSalesOrderInLinkingTable(String docid, String linkeddocid) throws ServiceException;
    public KwlReturnObject checkEntryForDeliveryOrderInLinkingTable(String docid, String linkeddocid) throws ServiceException;
    public KwlReturnObject checkEntryForSalesReturnInLinkingTable(String docid, String linkeddocid) throws ServiceException;
    public KwlReturnObject saveDeliveryOrderLinking(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject saveSalesReturnLinking(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getSalesorder(HashMap<String, Object> hm) throws ServiceException; 
    public KwlReturnObject getDeliveryorder(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getGoodsReceiptOrder(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getGoodsReceipt(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getDeliveryorderForSR(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getCompanyList(String[] subdomainArray) throws ServiceException;
    public KwlReturnObject getSalesInvoiceForSR(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject updateInvoiceLinkflag(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject updateQuotationLinkflag(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject updateDeliveryOrderStatus(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject updateIsJobWorkClose(String rowId,String companyId) throws ServiceException;
    public KwlReturnObject updateSalesInvoiceStatus(HashMap<String, Object> requestParams) throws ServiceException ;
    public KwlReturnObject updateGoodsReceiptOrderStatus(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject updateGoodsReceiptStatus(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getInvoiceDetailWithDeferredJE(Invoice invoice) throws ServiceException;
    public KwlReturnObject getDeliveryOrderDFromSOD(String sodid, String companyid) throws ServiceException;
    public double getDeliveryOrderQuantityFromSOD(String sodid, String companyid, boolean inSelectedUOM) throws ServiceException;
    public double getDODetailQuantityFromProduct(HashMap<String, Object> requestParams) throws ServiceException;
    public double getDODetailQuantityForProduct(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getDeliveryOrderFromInvoice(String ciid, String companyid) throws ServiceException;
     public KwlReturnObject getMonthlySalesReport(HashMap<String, Object> requestParams, java.util.Date startdate, java.util.Date enddate) throws ServiceException;
    public KwlReturnObject saveInvoiceTermMap(HashMap<String, Object> hm) throws ServiceException ;
    public KwlReturnObject updateInvoiceTermMap(HashMap<String, Object> dataMap) throws ServiceException;    
    public KwlReturnObject saveSalesReturnTermMap(HashMap<String, Object> hm) throws ServiceException ;
    public KwlReturnObject getInvoiceTermMap(HashMap<String, Object> hm) throws ServiceException ;
    public KwlReturnObject saveInvoiceDetailTermMap(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject saveSupplierExciseDetailMap(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getSupplierDetailsMap(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getInvoicedetailTermMap(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getGenricInvoicedetailTermMap(HashMap<String, Object> hm) throws ServiceException;
    public Map<String, List<InvoiceTermsMap>> getInvoiceTermMapList(List<String> invoiceIDLIST);    
    public KwlReturnObject deleteInvoiceTermMap(HashMap<String, Object> hm) throws ServiceException ;
    public KwlReturnObject deleteSalesReturnTermMap(HashMap<String, Object> hm) throws ServiceException ;
    public KwlReturnObject deletePurchaseReturnTermMap(HashMap<String, Object> hm) throws ServiceException ;
    public boolean deleteInvoiceDetailsTermMap(String invoiceId) throws ServiceException ;
    public KwlReturnObject updateDeliveryOrderCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject updateSalesReturnCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject updateDODetailsCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject updateSRDetailsCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject updateSRDetailsProductCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getReportPerm(HashMap<String, Object> requestParams)throws ServiceException;
    public KwlReturnObject getReports(HashMap<String, Object> requestParams)throws ServiceException;
    public KwlReturnObject getReportsForWidgets(HashMap<String, Object> requestParams)throws ServiceException;
    public List<Object[]> getInvoiceAllList(List<String> invoiceIDLIST);
    public KwlReturnObject getUserForCombo(HashMap<String, Object> requestParams)throws ServiceException;
    public KwlReturnObject AssignUserPerm(HashMap<String, Object> dataMap)throws ServiceException;
    public boolean checkUserPermissionForReport(HashMap<String, Object> dataMap)throws ServiceException;
    public KwlReturnObject AssignAmendingPrice(HashMap<String, Object> dataMap)throws ServiceException;
    public KwlReturnObject savePermissionsforFilteringReportsData(HashMap<String, Object> dataMap)throws ServiceException;
    public JSONObject GetUserAmendingPrice(HashMap<String, Object> dataMap)throws ServiceException;
    public JSONObject getPermissionsforFilteringReportsData(HashMap<String, Object> dataMap)throws ServiceException;
    public JSONObject DeleteUserPerm(HashMap<String, Object> requestParams)throws ServiceException;
    public JSONArray getMissingAutoSequenceNumber(HashMap<String, Object> requestParams)throws ServiceException;
    public String[] columSort(String Col_Name,String Col_Dir) throws ServiceException;
    public String[] columSortSalesReturn(String Col_Name,String Col_Dir) throws ServiceException;
    public String[] columSortDeliveryOrder(String Col_Name,String Col_Dir) throws ServiceException;
    public String columSortSOA(String Col_Name,String Col_Dir,boolean isCustomer) throws ServiceException;
    public KwlReturnObject getDOFromInvoices(String invoiceId, String CompanyId,boolean CallFromCI) throws ServiceException;
    public KwlReturnObject getAutogeneratedDOFromInvoices(String invoiceId, String CompanyId) throws ServiceException;
    
    public KwlReturnObject saveExcludedRecords(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getRecurringInvoices(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getCreditNoteLinkedWithInvoice(String invoiceId, String companyId)throws ServiceException;
    public KwlReturnObject getPaymentReceiptsLinkedWithInvoice(String invoiceId, String companyId)throws ServiceException;
    public KwlReturnObject getSalesReturnLinkedWithInvoice(String invoiceId, String companyId)throws ServiceException;
    
    public KwlReturnObject getPurchaseInvoiceLinkedWithNote(String billid, String companyid)throws ServiceException;
    public KwlReturnObject getCreditNoteLinkedWithDebitNote(String debitNoteId, String companyId) throws ServiceException;
    
    public KwlReturnObject getSalesInvoiceLinkedWithNote(String billid, String companyid)throws ServiceException;
    public KwlReturnObject getDebitNoteLinkedWithCreditNote(String creditNoteId, String companyId) throws ServiceException;
    public Object executeQueryWithProjection(final Class c, final String[] columnNames, final Map<String, Object> paramMap) throws ServiceException;
    
//    public KwlReturnObject getSalesReturnLinkedWithDO(String invoiceId, String companyId)throws ServiceException;
//    public KwlReturnObject getSILinkedWithDO(String invoiceId, String companyId)throws ServiceException;
    public boolean isInvoiceUsedInCreditNote(String invoiceId, String companyId)throws ServiceException;
    public boolean isInvoicehasDepreciatedAsset(String invoiceId, String companyId)throws ServiceException;
    public boolean isDOhasDepreciatedAsset(String invoiceId, String companyId)throws ServiceException;
    public KwlReturnObject getDuplicateInvoiceNumberForEdit(String entryNumber, String companyid, String invoiceid) throws ServiceException;
    
    public KwlReturnObject getInvoiceNumbersOfDO(String deliveryOrderID, String companyid) throws ServiceException;
    public KwlReturnObject getDODuplicateNumberWithID(String entryNumber, String companyid, String doid)throws ServiceException;
    public KwlReturnObject getSalesReturnCountForEdit(String entryNumber, String companyid, String srid)throws ServiceException;
    public KwlReturnObject getQuotationCreditTermDetails(String companyid,int paymentdays) throws ServiceException;
    public KwlReturnObject getDO_Product(Map<String, Object> requestMap) throws ServiceException;
    public KwlReturnObject getSalesReturn_Product(String productid, String companyid) throws ServiceException;
    public KwlReturnObject getCountBuildDettails_Product(String productid, String companyid) throws ServiceException;
    public KwlReturnObject getMaintenanceFromQuotation(String maintenanceId,String companyid) throws ServiceException;
    
    public KwlReturnObject getInvoiceFromCustomerPORefNo(JSONObject reqParams) throws ServiceException;
    public KwlReturnObject getPackingDoListCount(String packNumber, String companyid) throws ServiceException;
    public KwlReturnObject getPackingListCount(String packNumber, String companyid) throws ServiceException;
    public KwlReturnObject getShippingListCount(String shipNumber, String companyid) throws ServiceException;
    public KwlReturnObject savePackingDoList(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject saveShippingDeliveryOrder(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject saveShippingDeliveryOrderDetails(Map<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject savePackingDolistDetails(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject saveShipingDoDetails(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject savePackingDoListPackingDetails(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getDeliveryOrderRowForpackingDoDetails(String productid,String doid, String companyid) throws ServiceException;
    
    // Asset Maintenance Work Order

    public KwlReturnObject deleteAssetMaintenanceScheduleObject(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteAssetMaintenanceScheduleEvent(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteAssetMaintenanceWorkOrder(String workOrderIDS, String companyId) throws ServiceException;
    
    public KwlReturnObject deleteWorkOrdersBatchSerialDetails(String workOrderIDS, String companyId) throws ServiceException;

    public KwlReturnObject deleteAssetMaintenanceWorkOrderofSchedules(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getAssetMaintenanceScheduleReport(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAssetMaintenanceScheduleForCrown(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getMaintenanceSchedules(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getWorkOrderInventory(String doid) throws ServiceException;

    public KwlReturnObject getWorkOrderBatches(String doid, String companyid) throws ServiceException;

    public KwlReturnObject getWorkOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getTaxAdjustments(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getInvoiceForSalesProductCategoryDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getProductCategoryForDetailsReport(String productId) throws ServiceException;
    
    public KwlReturnObject deleteWorkOrdersBatchSerialDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveWorkOrder(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveWorkOrderDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteWorkOrderDetails(String doid, String companyid) throws ServiceException;

    public KwlReturnObject getWorkOrderCount(String orderno, String companyid) throws ServiceException;

    public KwlReturnObject getPackingDoLists(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPackingDoListDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getShipingDoDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getItemPackingDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public String getCustomerId(String companyid, String acccode) throws ServiceException;

    public String getproductId(String companyid, String acccode) throws ServiceException;

    public String getInvoiceId(String companyid, String invoiceno) throws ServiceException;

    public KwlReturnObject getReceiptFromInvoice(String invoiceid, String companyid) throws ServiceException;
    
    public KwlReturnObject deletePackingDoListsPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteCreditNotesPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    
    public String getCreditNoteIdFromSRId(String srid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSerialNoUsedinConsignmentInvoiceFromDO(String invoiceid, String companyid) throws ServiceException;
    
    public KwlReturnObject getbatchNoUsedinConsignmentInvoiceFromDO(String invoiceid, String companyid) throws ServiceException;
    
    public String getCustomerIdForPOS(String companyid) throws ServiceException;
  
    public int getPendingapprovalForVendorInvoice(String companyid,String InvoiceId) throws ServiceException;
    public int updateGoodsReceipt(JSONObject json, HashSet details) throws ServiceException;
    public int updateBillingGoodsReceipt(JSONObject json, HashSet details) throws ServiceException;
    
    public KwlReturnObject getAdvanceReceiptLinkedWithInvoice(String invoiceId, String companyId)throws ServiceException;

    public void updatePreviousIssue(double precnt, String productId, String companyid) throws ServiceException;

    public KwlReturnObject getConsignmentLoanDetails(HashMap requestParams) throws ServiceException;
    
    public KwlReturnObject approvePendingInvoice(String invID, String companyid, int approvalStatus) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderMerged(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getRepeateInvoiceNo(Date prevDate) throws ServiceException;
    
    public KwlReturnObject approveRecurringInvoice(String repeateid, boolean ispendingapproval) throws ServiceException;
    
    public KwlReturnObject activateDeactivateRecurringInvoice(String repeateid, boolean isactivate) throws ServiceException;   
    
    public Object getUserObject(String id) throws ServiceException;   
    
    public KwlReturnObject getForeignCurrencyGainAndLossData(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getDeliveryOrderDFromSODOptimized(String sodid, String companyid) throws ServiceException;

    public String updateDOEntryNumberForNewDO(Map<String, Object> seqNumberMap);
    
    public String updateDOEntryNumberForNA(String prid, String entrynumber);
    
    public KwlReturnObject deleteAssetDetailsLinkedWithAssetSalesReturn(HashMap<String, Object> requestParams) throws ServiceException;
    
    public String updateSIEntryNumberForNA(String prid, String entrynumber);
 
    public KwlReturnObject getInvoicesForJE(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAllCompanyFromDb(String [] subdomain) throws ServiceException;
    
    public KwlReturnObject getLinkedPOWithSO(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getSOForScript(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getSIDForScript(HashMap<String, Object> request) throws ServiceException;

    public int updateToNullRepeatedInvoiceOfInvoice(String invoiceTable, String repeateid, String invoiceid)throws ServiceException;

    public int deleteRepeatedInvoice(String repeateid)throws ServiceException;
    
    public boolean isSOWithQAStockLiesInQARepairOrOriginalStore(SalesReturnDetail srDetail,Store qaStore,Store repairStore)throws ServiceException;
    
    public void moveSRStockFromQARepairOriginalStoreToCustomerStore(SalesReturnDetail srDetail,Store qaStore,Store repairStore)throws ServiceException;
    
    public void deleteSRRelatedBatchSerialQAData(SalesReturn salesReturn) throws ServiceException ;
    
    public boolean saveInvoiceAmountDueZeroDate(Invoice invoice, HashMap<String, Object> dataMap);
    
    public boolean updateInvoiceAmountInBase(Invoice invoice, JSONObject json) throws ServiceException ;
            
    public KwlReturnObject getInvoicesHavingInvoiceAmount(HashMap<String, Object> requestParams) throws ServiceException;
        
    public KwlReturnObject getDeliveryOrderByCompany(HashMap<String, Object> requestParams) throws ServiceException;
    
    public boolean updateDeliveryOrderAmount(DeliveryOrder order, JSONObject json) throws ServiceException ;
    
    public KwlReturnObject getNormalInvoices(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAllGlobalSalesInvoiceOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAllGlobalDeliveryOrderOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveDeliveryOrderTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updateDeliveryOrderTermMap(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getDOTermMap(HashMap<String, Object> hm) throws ServiceException ;
    
    public KwlReturnObject getSRTermMap(HashMap<String, Object> hm) throws ServiceException ;
    
    public KwlReturnObject getPRTermMap(HashMap<String, Object> hm) throws ServiceException ;
    
    public double getPercentageFromTaxid(String taxid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSalesOrderForSalesPerson(HashMap<String, Object> requestParams) throws ServiceException ;
    
    public KwlReturnObject getInvoicesFromSOForSalesCommission(HashMap<String, Object> requestParams) throws ServiceException ;
    
    public KwlReturnObject getInvoiceDetailsForProduct(HashMap<String, Object> requestParams);
    
    public double getDeliveryOrderQuantityFromSI(String invoicedetailid, String companyid, boolean inSelectedUOM) throws ServiceException;
    
    public double getInvoiceFromQuantityDO(String invoicedetailid, String companyid, boolean inSelectedUOM) throws ServiceException;
    
    public KwlReturnObject getInvoicesHavingServiceProduct(HashMap<String, Object> requestParams) throws ServiceException ;
   
    public KwlReturnObject getInvoiceFromDO(String doid, String companyid) throws ServiceException ;
    public KwlReturnObject getSalesOrdersFromDO(String doid, String companyid) throws ServiceException;
    public KwlReturnObject getDODetailsFromCustomerInvoice(String invoiceid, String doid, String companyid) throws ServiceException;
    public KwlReturnObject getDODetailsFromSalesOrder(String invoiceid, String doid, String companyid) throws ServiceException;
    public KwlReturnObject getInvoiceDetailsFromDO(String invoiceid, String doid, String companyid, boolean includeDeletedInvoice) throws ServiceException ;
    public KwlReturnObject getInvoiceLinkedWithCQ(String billid, String quotationid, String companyid, boolean includeDeletedInvoice) throws ServiceException;
    public KwlReturnObject getSalesOrderLinkedWithCQ(String billid, String quotationid, String companyid) throws ServiceException;
    public KwlReturnObject getSalesReturnsFromDO(String doid, String companyid) throws ServiceException;
    public KwlReturnObject getSalesReturnDetailsFromDO(String salesReturnId, String doid, String companyid) throws ServiceException;
    public KwlReturnObject getPOlinkedInSO(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getCQlinkedInSO(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getInvoiceDetailForSalesPersonCommissionDimensionReport(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getDOFromOrToInvoices(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getCustomerQuotationLinkedInInvoice(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getLinkDetailReceipts(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCreditNotesLinkedWithInvoice(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject checkEntryForInvoiceInLinkingTableForForwardReference(String docid) throws ServiceException;
    public KwlReturnObject getAutoDOFromInvoice(String invoiceId, String companyid) throws ServiceException ;

    public KwlReturnObject saveWastageDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getWastageDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getLinkedDebitNoteWithPR(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedCreditNoteWithSR(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedMPWithCN(HashMap<String, Object> hm) throws ServiceException;
     
    public KwlReturnObject getLinkedRPWithDN(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedAdvanceMPWithCN(HashMap<String, Object> hm) throws ServiceException;
     
    public KwlReturnObject getLinkedAdvanceRPWithDN(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getInvoiceDetailfromCrmQuotation(String crmQuoteID) throws ServiceException;
    
    public KwlReturnObject getIDFromInvoiceLinking(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject getInvoicesLinkedInSalesReturn(Map request)throws ServiceException;
    
    public KwlReturnObject getDOLinkedInSalesReturn(Map request)throws ServiceException;
    
    public KwlReturnObject getCredittNoteLinkedInsalesReturn(Map request)throws ServiceException;
    
    public KwlReturnObject getPaymentLinkedInsalesReturn(Map request)throws ServiceException;
 
    public KwlReturnObject getInvoicesWithSearchColumn(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getPurchaseReturnLinkedInDebitNote(Map request)throws ServiceException;
     
    public KwlReturnObject getSalesReturnLinkedInCreditNote(Map request)throws ServiceException;
    
    public KwlReturnObject getLinkedRPWithSI(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject getLinkedAdvanceRPWithSI(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject deleteBadDebtInvoiceMapping(HashMap<String, Object> requestParams) throws ServiceException;
 
    public KwlReturnObject getInvoiceDetailsUsingAdvanceSearch(HashMap<String, Object> requestParams) throws ServiceException;
 
    public KwlReturnObject getInvoiceDetailFromSOD(String soid) throws ServiceException;
    
    public KwlReturnObject getDeliveryOrderIDFromSOD(String sodid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSalesReturnDetailTermMap(HashMap<String, Object> PurchaseReturnDetailTermMap) throws ServiceException;
    
    public KwlReturnObject saveSalesReturnDetailsTermMap(HashMap<String, Object> SalesReturnDetailsTermsMap) throws ServiceException;
    
     public KwlReturnObject saveOrUpdateDODetailsTermsMap(HashMap<String, Object> requestParams) throws ServiceException;
     
    public JSONArray getDODetailsTermMap(String invoiceId) throws ServiceException;

    public boolean deleteDODetailsTermMap(String invoiceId) throws ServiceException;
    
    public KwlReturnObject getDocumentIdFromMappingId(String mappingId,String companyId) throws ServiceException;
    
    public KwlReturnObject saveExciseTemplateMapping(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject checkIdUsedInTranscation(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject deleteExciseTemplateMapping(HashMap<String, Object> dataMap) throws ServiceException;
    
    public List getInvoiceVatDetails(HashMap paramsHM);
    
    public void updateInvoiceTaxPaidFlag(HashMap paramsHM);
    
    public void resetInvoiceTaxPaidFlag(HashMap paramsHM);
    
    public KwlReturnObject getTaxPaymentFromInvoice(String invoiceid, String companyid) throws ServiceException; 
     
    public KwlReturnObject updateDeliveryOrderInventoryJESetNull(DeliveryOrder deliveryorder);

    public KwlReturnObject getDOCountForInventoryJE(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getSRCountForInventoryJE(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject updateSalesReturnInventoryJESetNull(SalesReturn salesReturn);
    
    public KwlReturnObject saveExciseDetails(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject updateRecDetailId(Invoice inv) throws ServiceException;
    
    public KwlReturnObject getExciseComputationReportDetails(Map<String, Object> requestParams)throws ServiceException;
    
    public KwlReturnObject getServiceTaxComputationReportDetails(Map<String, Object> requestParams)throws ServiceException;
    
    public KwlReturnObject getComputationReportDetailsTransactionWise(Map<String, Object> requestParams)throws ServiceException;
    
    public boolean updateInvoiceExciseDuty(Invoice invoice)throws ServiceException;
    
    public KwlReturnObject getSalesInvoiceFromJE(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject getAllInvoices(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getExciseDetails(String receiptId) throws ServiceException;
    
    public KwlReturnObject getDeliveryOrderLinking(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getInvoiceLinking(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateDeliveryPlannerEntry(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDeliveryPlannerForModule(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject createEntryOfDeliveryPlannerForModule(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDeliveryOrderFromDeliveryPlanner(String doID, String companyID) throws ServiceException;
    
    public KwlReturnObject getSalesReturnFromDeliveryPlanner(String srID, String companyID) throws ServiceException;
    
    public KwlReturnObject getDataForCostAndSellingPriceReport(JSONObject requestParams) throws ServiceException;
    
    public KwlReturnObject getSalesReturnDetailsByInvoiceDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public int getTotalSalesReturnDetailsCountByInvoiceDetails(String companyid) throws ServiceException;
    
    public KwlReturnObject getDataSTBillDateWiseReport(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getInvoiceDetailsBetweenDates(HashMap requestParams) throws ServiceException;
    
    public KwlReturnObject getAdvanceMakepaymentOfExcisDuty(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAdvanceJEAdjustmentOfExcisDuty(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject createServiceTaxAccount(Object[] subdomainArray) throws ServiceException;
    
    public KwlReturnObject getGroupId(HashMap hashData) throws ServiceException;
    
    public KwlReturnObject getJEntryBetweenDates(HashMap requestParams) throws ServiceException;
    
    public KwlReturnObject getSaleOfCustomer(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getSaleOfCompany(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getSaleQuantityOfCustomer(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getCustomerSalesbyProduct(HashMap<String, Object> request) throws ServiceException;
    
    public boolean isTaxApplied(HashMap<String,Object> data, int taxtype) throws ServiceException;
    
    public KwlReturnObject getGenricSalesReturndetailTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public List getSalesReturnVatDetails(HashMap paramsHM);
    
    public void updateSalesReturnTaxPaidFlag(HashMap params);
            
    public void resetSalesReturnTaxPaidFlag(HashMap params);
    // Excise Opening Balance check from Vendor Master ERP-27108 : to hide JE POST
    public void permanentDeleteJournalEntryFromInvoice(String invoiceid,String companyid)throws ServiceException;
    
    public KwlReturnObject getAmountFromReceiptDetailOtherwise(String receiptId)throws ServiceException;
    
    public KwlReturnObject getProductsFromDeliveryOrderOrder(String billid, String companyid) throws ServiceException;

    public KwlReturnObject getProductsFromSalesReturn(String billid, String companyid) throws ServiceException;
    
    public KwlReturnObject checkForExistingFreeGiftJEs(Map<String,Object> map) throws ServiceException;
    
    public KwlReturnObject JEForFreeGift(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject checkForLatestFreeGiftJEs(Map<String, Object> map) throws ServiceException;
    
    public KwlReturnObject deleteDeliveryOrdersFreeGiftJEMapping(String DOId,String companyId) throws ServiceException;
    
    public KwlReturnObject getDODetailfromInvoiceDetailID(String invoiceDetailID,String companyId) throws ServiceException;

    public List getTerms(String tax) throws ServiceException;
    
    public List saveGiroFileGenerationHistory(HashMap<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getMaxCountOfGiroFile(HashMap<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getInvoicesForCommissionSchema(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getInvoicesWithGlobalTax(HashMap<String,Object> map) throws ServiceException;
    
    public KwlReturnObject getInvoicesWithLineLevelTax(HashMap<String,Object> map) throws ServiceException;
    
    public KwlReturnObject getDODInterStoreMapping(Map<String, Object> requestParams) throws ServiceException;
    
    public List getDOISTMapping(Map<String, Object> map) throws ServiceException;
    
    public KwlReturnObject getShippingQty(Map<String, Object> map) throws ServiceException;
    public KwlReturnObject getSalesPersonHavingTransactions(Map<String,Object> salesPersonRequestParams)throws ServiceException;
    
    public KwlReturnObject savePacking(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject savePackingDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject savePackingItemDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    /**
     * Method to save/update UPS package details; used in UPS Integration functionality
     */
    public KwlReturnObject saveOrUpdateUpsPackageDetails(Map<String, Object> dataMap) throws ServiceException;
    
    /**
     * Method to get UPS shipment tracking numbers; used in UPS Integration functionality
     */
    public KwlReturnObject getUPSTrackingNumberFromDoDetails(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject savePackingDoDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getPackingLists(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPackingDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getItemDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    /**
     * Method to get UPS package details; used in UPS Integration functionality
     */
    public KwlReturnObject getUpsPackageDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPackingDoDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deletePackingPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    
    public void updateDeliveryOrderStatus(Map<String, Object> map) throws ServiceException;
    
    public KwlReturnObject getShippingDO(Map<String,Object> reqParams) throws ServiceException;
    
    public KwlReturnObject getShippingDODetails(Map<String,Object> reqParams) throws ServiceException;
    
    public List isInvoiceNotLinkedWithAnyDo(Invoice invoice) throws ServiceException;
    

    public boolean isLinkingOfDOInSI(Invoice invoice) throws ServiceException;
    
    public KwlReturnObject getSalesReturnDetails(String id) throws ServiceException;
    
    public KwlReturnObject getCheckListSOSQL(Map<String,Object> reqParams);
    
    public KwlReturnObject getClosedQtyForLoan(String id) throws ServiceException;
    
    public List<InvoiceLinking> getInvoiceLinkingDataToValidateLinkingInfo(Map<String, Object> requestParams) throws ServiceException;
    
    public void updateMemoForIST(String memo, String doid, String companyid);
    
    public List getDeliveryDetailInterStoreLocationMappingList(JSONObject params) throws ServiceException;

    public JSONArray getDOPackedShippedQty (Map<String, String> requestParams) throws ServiceException;

    public double getDOPackedQuantity(String dodetailid) throws ServiceException;
    
    public KwlReturnObject getCalculatedCreditNoteTax(Map<String, Object> requestParams) throws ServiceException;
    
    public void closeDeliveryDetailsOrdersPermanent(String id,String company) throws ServiceException;

    public KwlReturnObject getSalesReturnDFromDOD(String dodid) throws ServiceException ;
    
    public List getInvoiceKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getRepeatePreviousInvoices(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getReceiptDetailsOfSI(String grID) throws ServiceException;

    public KwlReturnObject getLinkedReceiptDetailsOfSI(String grID) throws ServiceException;

    public KwlReturnObject getLinkedCreditNoteDetailsOfSI(String grID) throws ServiceException;
    
    public List getProductBatchQuantity(JSONObject requestParams) throws ServiceException;
    public void updateRepeatInvoice(JSONObject repeatInvoiceParams) throws ServiceException;
    
    public void deleteAttachDocuments(Map<String, Object> paramsMap) throws ServiceException; // Delete previously attach document synchronized from CRM to ERP Quotation

    public boolean isPickPackShipDO(String doid) throws ServiceException;
    
    /**
     * ERP-34156
     * @desc checks if pickpackDO created
     * @param params (companyid)
     * @return true (for Present) and false (for Not Present)
     * @throws ServiceException 
     */
    public KwlReturnObject isPickPackShipDOPresent(Map<String, Object> params) throws ServiceException;
    
    public String getDeliveryOrderId(String companyid, String dono) throws ServiceException;
    public List getReceiptLinkedToInvoice(JSONObject reqParams) throws ServiceException;
    
    public KwlReturnObject getInvoiceTermMapFromInvoiceDetail(String invoiceDetailsID) throws ServiceException;
    
    public KwlReturnObject getSalesInvoiceTermsMapList(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject checklinkingofTransactions(String moduleid, String billids) throws ServiceException ;

    
    /**
     * This method is used to get the InvoiceDetailTermsMap object.
     * @param invoicedetailId This is a invoice detail ID
     * @return List which contains InvoiceDetailTermsMap Object
     */
    public List getInvoiceDetailsTermMap(String invoicedetailId) throws ServiceException;
    
    /**
     * Get Additional memo details from table
     * @param reqParams
     * @return
     * @throws ServiceException 
     */
    public List<AdditionalMemo> getAdditionalMemo(JSONObject reqParams) throws ServiceException;
    
    public double getReturnQuantityofDOorSI(String detailid, String companyid, Boolean isreqfromDO) throws ServiceException;
    
    public double getReturnQuantityofDOorSIForSR(String detailid, String companyid, String batchmapid) throws ServiceException;
    
    public KwlReturnObject getSalesReportMasterData(JSONObject requestParams) throws ServiceException , JSONException , ParseException; 
    
    public KwlReturnObject isPackingStoreUsedBefore(Map<String, Object> params) throws ServiceException;
    
    public void updateMemoForPickPackDOIST(String memo, String doid, String companyid);
    public void saveSalesInvoiceRequestData(JSONObject paramJObj, boolean isBeforeSave);
    public List getGSTDocumentHistory(JSONObject reqParams) throws ServiceException, JSONException;
    public boolean isQCEnabledDO(String doid,String companyid) throws ServiceException;
    
    public double getShippingQuantity(Map<String, Object> map) throws ServiceException;
    
    public void deleteSalesOrderSerialDetails(String sodids, String companyid) throws ServiceException;
    
    public HashSet<String> getDOShippedArray (Map<String, String> requestParams) throws ServiceException;
    
    public double getDOPickedQty(Map<String, Object> map) throws ServiceException;
    
    public KwlReturnObject getExternalCurrencyRateForInvoice(String invId, String companyId) throws ServiceException;
    
    public KwlReturnObject getCreditNoteDetailsForSalesReport(JSONObject reqParams) throws ServiceException;
    
    public List getOpeningInvoiceKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCreditNoteAccountDetailsForSalesReport(JSONObject reqParams) throws ServiceException;
    
   public KwlReturnObject updateSOBalanceQtyAfterDOAfterDelete(String doid, String linkedDocumentID, String companyid) throws AccountingException ;

   public KwlReturnObject getCalculatedInvTaxNew(Map<String, Object> requestParams) throws ServiceException;
}
