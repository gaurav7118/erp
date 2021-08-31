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
package com.krawler.spring.accounting.goodsreceipt;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CroneSchedule;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stock.Stock;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.goodsreceipt.dm.GoodsReceiptInfo;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import org.hibernate.SessionFactory;

/**
 *
 * @author krawler
 */
public interface accGoodsReceiptDAO {

    public void saveOrUpdateObj(Object obj) throws ServiceException;
    
    public Map<String, GoodsReceiptInfo> getGoodsReceiptInfoList(List<String> invoiceIDLIST) throws ServiceException;

    public Map<String, JournalEntry> getGRInvoiceJEList(List<String> invoiceIDLIST) throws ServiceException;

    public KwlReturnObject addGoodsReceipt(Map<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject saveURDPurchaseInvoiceMapping(JSONObject pramsObj) throws ServiceException;
    
    public KwlReturnObject updateURDPurchaseInvoiceMapping(JSONObject pramsObj) throws ServiceException;
    
    public KwlReturnObject deleteURDVendorRCMPurchaseInvoice(JSONObject pramsObj) throws ServiceException;
    
    public KwlReturnObject getURDVendorRCMPurchaseInvoice(JSONObject pramsObj) throws ServiceException;
    
    public KwlReturnObject saveVILinking(Map<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject saveGRLinking(Map<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject checkEntryForGoodsReceiptInLinkingTable(String docid, String linkeddocid) throws ServiceException;
    
    public KwlReturnObject checkEntryForGoodsReceiptOrderInLinkingTable(String docid, String linkeddocid) throws ServiceException;
    
    public KwlReturnObject checkEntryForPurchaseReturnInLinkingTable(String docid, String linkeddocid) throws ServiceException;
    
    public KwlReturnObject checkEntryForPurchaseOrderInLinkingTable(String docid, String linkeddocid) throws ServiceException;
    
    public KwlReturnObject savePRLinking(Map<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject saveBadDebtInvoiceMapping(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject deleteBillingGoodsReceiptDetails(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getCalculatedExpenseGRDtlTax(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getCalculatedGRDtlTax(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getCalculatedGRDtlTaxBilling(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getCalculatedGRTax(Map<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject getCalculatedVHT(Map<String, Object> filterParams) throws ServiceException;
    
    @Deprecated
    public KwlReturnObject getCalculatedDNTax(Map<String, Object> requestParams) throws ServiceException;
        
    public KwlReturnObject getCalculatedGRTaxBilling(Map<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject getCalculatedCNTaxGst(Map<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject getCalculatedDNTaxGst(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getExpenseGRDetails(HashMap<String, Object> grRequestParams) throws ServiceException;

    public KwlReturnObject updateGoodsReceipt(Map<String, Object> hm) throws ServiceException;

    public KwlReturnObject getGoodsReceipts(Map<String, Object> request) throws ServiceException;
    public KwlReturnObject getGoodsReceiptOrderIDFromVI(String poid,String company) throws ServiceException;
    
    public KwlReturnObject getGoodsReceiptIDForDiamondAviation(String poid,String company) throws ServiceException;
    
    public KwlReturnObject getCompanyGoodsReceipts(String companyid) throws ServiceException;

    public KwlReturnObject getGoodsReceiptsMerged(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAvailableQtyOfBatchUsedinDOFromGRO(String invoiceid, String companyid) throws ServiceException;
    
    public KwlReturnObject getGoodsReceiptDetailsUsingAdvanceSearch(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPurchaseByVendor(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject WeeklyCashFlowUnPaidInvoices(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getOpeningBalanceInvoices(Map<String, Object> requestParams) throws ServiceException;
    
    public int getOpeningBalanceInvoiceCount(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForInvoices(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceTotalBaseAmountForInvoices(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSelectedGoodsReceiptsMerged(Map<String, Object> requestParams, String invoiceIds) throws ServiceException;

    public KwlReturnObject deleteGoodsReceipts(String receiptid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteOpeningGoodsReceiptsCustomData(String receiptid) throws ServiceException;

    public KwlReturnObject deleteGoodsReceiptPermanent(HashMap<String, Object> requestParams) throws AccountingException;
    
    public KwlReturnObject deleteAssetDetailsLinkedWithGR(HashMap<String, Object> requestParams) throws ServiceException;
//   public KwlReturnObject deleteOpeningBalanceGoodsReceiptPermanent(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteGoodsReceiptOrdersPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    
     public KwlReturnObject deleteLinkingInformationOfPI(HashMap<String, Object> requestParams) throws ServiceException;
     
     public KwlReturnObject deleteLinkingInformationOfGR(HashMap<String, Object> requestParams) throws ServiceException;

     public KwlReturnObject deleteLinkingInformationOfPR(HashMap<String, Object> requestParams) throws ServiceException;
     
    public KwlReturnObject deleteAssetDetailsLinkedWithGROrder(HashMap<String, Object> requestParams) throws ServiceException;
    
//    public KwlReturnObject deletePurchaseorderDetailsLinkedWithGROrder(String doid, String companyid) throws ServiceException;
    
    public KwlReturnObject updateGoodsReceiptOrder(GoodsReceiptOrder goodsReceiptOrder) throws ServiceException;

    public KwlReturnObject deleteGoodsReceiptDetails(String receiptid, String companyid) throws ServiceException, AccountingException;

    public KwlReturnObject deleteExpenseGridDetails(String receiptid, String companyid) throws ServiceException, AccountingException;
    
    public KwlReturnObject deleteExpenseGridDetailsLanded(String receiptid, String companyid) throws ServiceException, AccountingException;

    public KwlReturnObject getReceiptFromNo(String receiptno, String companyid) throws ServiceException;

    public KwlReturnObject getReceiptFromSIN(JSONObject reqParams) throws ServiceException;
    
    public KwlReturnObject getReceiptDFromPOD(String podid) throws ServiceException;
    
    public KwlReturnObject getExpenseGRDetailFromPOD(Map<Object,Object> params) throws ServiceException;
    
    public KwlReturnObject getSGEtDFromPOD(String podid) throws ServiceException ;

    public KwlReturnObject getGRFromJE(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getBGRFromJE(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getJEFromGR(String greceiptid, String companyid) throws ServiceException;

    public KwlReturnObject getGoodsReceiptData(Map requestParam) throws ServiceException;

    public KwlReturnObject getGoodsReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getGRNDetailsFromPR(String doid, String companyid) throws ServiceException;// function to check GR used in purchase return

    public void setSessionFactory(SessionFactory sessionFactory);

    public KwlReturnObject getGoodsReceiptCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getExpensePOCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getBillingGoodsReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteGoodsReceiptEntry(String grid, String companyid) throws ServiceException,AccountingException;

    public KwlReturnObject getGRJournalEntry(String grid) throws ServiceException;

    public KwlReturnObject getGRDiscount(String grid) throws ServiceException;

    public KwlReturnObject getGRDetailsDiscount(String grid) throws ServiceException;

    public KwlReturnObject getGRInventory(String grid) throws ServiceException;

    public KwlReturnObject getBRDFromBPOD(String podid) throws ServiceException;

    public KwlReturnObject getBillingGoodsReceipt(Map requestParam) throws ServiceException;
    
    public KwlReturnObject getGROFromVInvoices(String invoiceId, String CompanyId) throws ServiceException ;
    
    public KwlReturnObject getAutoGeneratedGROFromVInvoices(String invoiceId, String CompanyId) throws ServiceException ;

    public KwlReturnObject saveBillingGoodsReceipt(Map<String, Object> hm) throws ServiceException;

    public KwlReturnObject getBillingGoodsReceiptsData(Map<String, Object> request) throws ServiceException;

    public KwlReturnObject getAmtromBPD(String receiptId) throws ServiceException;

    public KwlReturnObject getRevalFlag(String receiptId) throws ServiceException;

    public KwlReturnObject deleteBillingGoodsReceiptEntry(String grid, String companyid) throws ServiceException;

    public KwlReturnObject getFromBGR(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getBGRDiscount(String bgrid, String companyid) throws ServiceException;

    public KwlReturnObject getBGRDetailsDiscount(String bgrid, String companyid) throws ServiceException;

//    public KwlReturnObject getQtyandUnitCost(String productid, Date endDate) throws ServiceException;
    public KwlReturnObject getGoodsReceipt_Product(Map<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject getGoodsReceiptByInventoryID(String inventoryid) throws ServiceException;

    public KwlReturnObject getGoodsReceiptFormInventory(String inventoryid) throws ServiceException;

    public KwlReturnObject getGoodsReceiptOrderFormInventory(String inventoryid) throws ServiceException;
    public KwlReturnObject getStockAdjustmentFormInventory(String inventoryid) throws ServiceException;
    public KwlReturnObject getPurchaseReturnFormGoodsReceipt(String goodsreceiptid) throws ServiceException;

    public double getPurchaseReturnQtyFormGoodsReceipt(String grid) throws ServiceException;
    
    public KwlReturnObject getGoodsReceipt_Rate(String inventoryid) throws ServiceException;

    public KwlReturnObject getGR_ProductTaxPercent(String inventoryid) throws ServiceException;

    public KwlReturnObject getCalculatedGRDtlTaxDistinct(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCalculatedGRDtlTaxDistinctBilling(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPendingGRO(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject approvePendingGRO(String groID, String companyid, int status) throws ServiceException;

    public KwlReturnObject rejectPendingGRO(String doID, String companyid) throws ServiceException;

    public KwlReturnObject getpendingGROProductIDandSRResult(String pendingDOid, String companyid) throws ServiceException;

    public KwlReturnObject ApproveGROForProductIDandSRno(String pendingDOid, String companyid) throws ServiceException;

//    public KwlReturnObject getGoodsReceipt_Currency(String inventoryid) throws ServiceException;
//    public KwlReturnObject getGR_ProductDiscountPercent(String inventoryid) throws ServiceException;
    public KwlReturnObject getGoodsReceiptOrderCount(String orderno, String companyid) throws ServiceException;

    public KwlReturnObject getGoodsReceiptOrderInventory(String doid) throws ServiceException;

    public KwlReturnObject getGoodsReceiptOrderBatches(String doid, String companyid) throws ServiceException;

    public KwlReturnObject deleteGoodsReceiptOrderDetails(String doid, String companyid) throws ServiceException, AccountingException;

    public KwlReturnObject deleteGoodsReceiptOrdersBatchSerialDetails(HashMap<String, Object> requestParams) throws ServiceException,AccountingException;
    
    public KwlReturnObject getGoodsReceiptOrdersBatchDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveGoodsReceiptOrder(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveGoodsReceiptOrderDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getGoodsReceiptOrdersMerged(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getGR_Crosslinked_PI(HashMap<String, Object> request) throws ServiceException;
    
     public KwlReturnObject getGoodsReceiptLinkedWithPurchaseInvoice(String goodsreceiptOrderId, String companyId) throws ServiceException;

    public KwlReturnObject getGoodsReceiptOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getInvoiceDetailsFromGR(String billid, String invoiceid, String companyid) throws ServiceException;
    
    public KwlReturnObject getGRDetailsFromPI(String billid, String invoiceid, String companyid) throws ServiceException;
    
    public KwlReturnObject getGRDetailsFromPO(String billid, String invoiceid, String companyid) throws ServiceException;
    
    public KwlReturnObject getPIDetailsFromPO(String billid, String invoiceid, String companyid) throws ServiceException;
    
    public KwlReturnObject getExpensePIDetailsFromPO(String billid, String invoiceid, String companyid) throws ServiceException;
    
     public KwlReturnObject getPIDetailsFromVQ(String billid, String invoiceid, String companyid) throws ServiceException;
    
    public KwlReturnObject getPurchaseReturnDetailsFromGR(String billid, String invoiceid, String companyid) throws ServiceException;
    
    public KwlReturnObject checkPOLinkedWithAnotherGR(String billid) throws ServiceException;
    
    public KwlReturnObject checkVQLinkedWithAnotherPO(String billid) throws ServiceException;
    
    public KwlReturnObject checkPOLinkedWithAnotherPI(String billid) throws ServiceException;
    
    public KwlReturnObject checkVQLinkedWithAnotherPI(String billid) throws ServiceException;
    
    public KwlReturnObject getSOLinkedInPO(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteGoodsReceiptOrder(String doid, String companyid) throws ServiceException;

    public KwlReturnObject getGRFromGRInvoice(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getPRFromGReceipt(String greceiptid, String companyid) throws ServiceException;

    public KwlReturnObject getConsignmentNumberFromGReceipt(String greceiptid, String companyid) throws ServiceException;

    public KwlReturnObject getGROFromPR(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getGROFromInv(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getSerialNoUsedinDOFromGRO(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getbatchUsedinDOFromGRO(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject saveGoodsReceiptOrderStatus(String doId, String status) throws ServiceException;

    public KwlReturnObject getGDOIDFromVendorInvoiceDetails(String soid) throws ServiceException;

    public KwlReturnObject getPurchaseReturnIDFromVendorInvoiceDetails(String grId) throws ServiceException;
    
    public KwlReturnObject getCreditNoteAgainstVendorGstDetails(String grId) throws ServiceException;

    public KwlReturnObject getGROIDFromPOD(String soid) throws ServiceException;

    public KwlReturnObject getPRFromPOD(String soid) throws ServiceException;
    
    public KwlReturnObject getGRODIDFromPOD(String soid,String grorderId) throws ServiceException;
    
    public KwlReturnObject getGRODIDFromSGE(String sgeId,String grorderId) throws ServiceException;
    
    public KwlReturnObject getIDFromPRD(String soid) throws ServiceException;
    
    public KwlReturnObject getGRDIDFromPRD(String soid,String purchasereturnId) throws ServiceException;
    
    public KwlReturnObject updatePOBalanceQtyAfterGR(String doid, String linkedDocumentID, String companyid) throws ServiceException;
    
    public KwlReturnObject updatePOBalanceQtyAfterPR(String srid, String companyid) throws ServiceException;
        
    public KwlReturnObject getIDFromGROD(String soid) throws ServiceException;

    public int approvePendingInvoice(String qid, boolean isbilling, String companyid, String userid) throws ServiceException;
    
    public KwlReturnObject rejectPendingGR(String grID, String companyid) throws ServiceException;

    public int pendingApprovalInvoicesCount(String companyid) throws ServiceException;

    public KwlReturnObject getUnInvoicedGoodsReceiptOrders(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject updateGoodsReceiptOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updatePOLinkflag(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateSGELinkflag(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updatePurchaseReturnStatus(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateGRLinkflag(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updatePILinkflag(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updateVQLinkflag(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getReceiptOrderDFromPOD(String podid, String companyid) throws ServiceException;
    
           
    public double getReceiptOrderQuantityFromPOD(String podid, String companyid, boolean inSelectedUOM) throws ServiceException;
    
    public double getGRODetailQuantityFromProduct(HashMap<String, Object> requestParams) throws ServiceException;
    
    public double getGRODetailQuantityForProduct(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updateExchnageforInvoices(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPurchaseReturnCount(String orderno, String companyid) throws ServiceException;
    
    public KwlReturnObject getPurchaseReturnDuplicateSIN(JSONObject reqParams) throws ServiceException;

    public KwlReturnObject getPurchaseReturnInventory(String doid) throws ServiceException;

    public KwlReturnObject getPurchaseReturnBatches(String doid, String companyid) throws ServiceException;

    public KwlReturnObject deletePurchaseReturnDetails(String doid, String companyid) throws AccountingException;

    public KwlReturnObject savePurchaseReturn(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject savePurchaseReturnDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getPurchaseReturn(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getPurchaseReturnDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deletePurchaseReturn(String srid, String companyid) throws ServiceException;

    public KwlReturnObject deletePurchaseReturnPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deletePurchasesBatchSerialDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveInvoiceTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updateGoodsReceiptTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getAllGlobalGoodsReceiptOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject savePurchaseReturnTermMap(HashMap<String, Object> hm) throws ServiceException ;
    
    public KwlReturnObject saveInvoiceDetailTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getPurchaseorder(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getGoodsrecipt(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getGoodsReceiptsAndJE(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCompanyList() throws ServiceException;

    public KwlReturnObject getInvoiceTermMap(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject getGoodsReceiptdetailTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getGenricGoodsReceiptdetailTermMap(HashMap<String, Object> hm) throws ServiceException;

    public Map<String, List<ReceiptTermsMap>> getInvoiceTermMapGRList(List<String> invoiceIDLIST) throws ServiceException;

    public KwlReturnObject deleteInvoiceTermMap(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject updateGoodsReceiptCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateRecDetailId(GoodsReceipt gr) throws ServiceException;

    public KwlReturnObject updatePurchaseReturnCustomData(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updatePRDetailsCustomData(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updatePRDetailsProductCustomData(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updateGRDetailsCustomData(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject updateGRDetailsProductCustomData(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getMasterItemPriceFormulaPrice(String productId, double itemNo) throws ServiceException;

    public KwlReturnObject getGRODetails(String poid, String companyid) throws ServiceException;
    
    public KwlReturnObject getBadDebtPurchaseInvoiceMappingForGoodsReceipt(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getGRDetails(String poid) throws ServiceException;

    public String[] columSort(String Col_Name, String Col_Dir) throws ServiceException;

    public String[] columSortGoodsReceipt(String Col_Name, String Col_Dir) throws ServiceException;

    public String[] columSortPurchaseReturn(String Col_Name, String Col_Dir) throws ServiceException;

    public KwlReturnObject getDebitNoteLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException;
    
    public KwlReturnObject getCreditNoteLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException;
    
    public KwlReturnObject getDebitNoteForOverchargedLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException;
    
    public KwlReturnObject getPurchaseInvoiceLinkedWithGR(String invoiceId, String companyId) throws ServiceException;
    
    public KwlReturnObject getPurchaseReturnLinkedWithGR(String invoiceId, String companyId) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderLinkedWithGR(String invoiceId, String companyId) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderLinkedWithPI(String invoiceId, String companyId) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderLinkedWithExpensePI(String invoiceId, String companyId) throws ServiceException;

    public KwlReturnObject getPaymentVouchersLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException;

    public KwlReturnObject getPurchaseReturnLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException;
    
    public KwlReturnObject getVQLinkedWithPI(String invoiceId, String companyId) throws ServiceException;
    
    public KwlReturnObject getVendorInvoiceInvoiceLinkedWithPI(String billid, String companyid)throws ServiceException;
    
    public KwlReturnObject getInvoicesLinkedInPurchaseReturn(Map request)throws ServiceException;
    
    public KwlReturnObject getGoodsReceiptsLinkedInPurchaseReturn(Map request)throws ServiceException;
    
    public KwlReturnObject getDebitNoteLinkedInPurchaseReturn(Map request)throws ServiceException;
    
    public JSONObject isGRNEditable(Company company, String store,String location, JSONObject detail) throws ServiceException, JSONException;
    
    public boolean isGRUsedInDebitNote(String grId, String companyId) throws ServiceException;
    
    public boolean isGROhasReturned(String grId, String companyId) throws ServiceException;

    public boolean isInvoicehasDepreciatedAsset(String invoiceId, String companyId) throws ServiceException;

    public boolean isInvoicehasSoldAsset(String invoiceId, String companyId) throws ServiceException;

    public boolean isGROhasDepreciatedAsset(String invoiceId, String companyId) throws ServiceException;

    public boolean isGROhasSoldAsset(String invoiceId, String companyId) throws ServiceException;

    public boolean isGROhasLeasedAsset(String invoiceId, String companyId) throws ServiceException;

    public KwlReturnObject getDuplicateGRNumberForEdit(String entryNumber, String companyid, String grid) throws ServiceException;

    public KwlReturnObject getDuplicaeGoodsReceiptOrderNumber(String entryNumber, String companyid, String doid) throws ServiceException;
    
    public KwlReturnObject getDuplicateSupplierInvoiceNumberForGRN(JSONObject reqParams) throws ServiceException;

    public KwlReturnObject getPurchaseReturnCountEdit(String entryNumber, String companyid, String srid) throws ServiceException;

    public KwlReturnObject getGR_Product(Map<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject getPurchaseReturn_Product(String productid, String companyid) throws ServiceException;
 
    public KwlReturnObject deleteDebitNotesPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    
    public String getDebitNoteIdFromPRId(String srid, String companyid) throws ServiceException;
    
    public KwlReturnObject getAllUninvoicedConsignmentDetails(HashMap<String, Object> requestParams) throws ServiceException;
  
    public double getReturnQuantity(HashMap<String, Object> requestParams) throws ServiceException;

    public double getGRQuantityWhoseInvoiceCreated(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAdvancePaymentsLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException;

    public KwlReturnObject getExcludedInvoices(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveRepeateInvoiceInfo(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getRepeateVendorInvoices(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getRepeateBillingGoodsReceipt(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getRepeateVendorInvoicesDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getRepeateVendorInvoicesDetailsForExpander(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getRepeateBillingGoodsReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getGoodsReceiptCount(String invoiceno, String companyid) throws ServiceException;
    
    public KwlReturnObject getSODeatils(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPendingConsignmentRequests(String companyid,String productid) throws ServiceException;
        
    KwlReturnObject getConsignmentRequestApproverList(String ruleid) throws ServiceException;
    
    public KwlReturnObject approvePendinggr(String grID, String companyid, int approvalStatus) throws ServiceException;
    
    public KwlReturnObject getSalesOrderMerged(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getVQlinkedInPO(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getRepeatePurchaseInvoiceNo(Date prevDate) throws ServiceException;
    
    public KwlReturnObject getReceiptOrderDFromPODOptimized(String podid, String companyid) throws ServiceException ;
    
    public String updateGREntryNumberForNewGR(Map<String, Object> seqNumberMap)  throws AccountingException;
    
    public KwlReturnObject deleteAssetDetailsLinkedWithPurchaseReturn(HashMap<String, Object> requestParams) throws ServiceException;

    public String updatePIEntryNumberForNewPI(Map<String, Object> seqNumberMap);

    public String updatePIEntryNumberForNA(String grid, String entrynumber);

    public String updateGREntryNumberForNA(String grid, String GRNumber);
    
    public KwlReturnObject getGoodsReceiptsHavingInvoiceAmount(HashMap<String, Object> requestParams) throws ServiceException;
    
    public boolean saveGoodsReceiptAmountDueZeroDate(GoodsReceipt goodsReceipt, HashMap<String, Object> dataMap); 
    
    public boolean updateInvoiceAmountInBase(GoodsReceipt goodsreceipt, JSONObject json) throws ServiceException;
    
    public KwlReturnObject getGoodsReceiptsOrderByCompany(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAllGlobalGoodsReceiptsOrderOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException;
    
    public boolean updateGoodsReceiptOrderAmount(GoodsReceiptOrder order, JSONObject json) throws ServiceException ;
    
    public KwlReturnObject getNormalGoodsReceipts(HashMap<String, Object> requestParams) throws ServiceException ;
    
    public KwlReturnObject saveGoodsReceiptTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updateGoodsReceiptOrderTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getGRTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedPaymentDetail(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getAutoGRFromInvoice(String invoiceId, String companyid) throws ServiceException;
    
    public KwlReturnObject updateEntryInCreditNoteLinkingTable(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject savePaymentLinking(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject updateEntryInDebitNoteLinkingTable(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject saveReceiptLinking(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject getGoodsReceiptsWithSearchColumn(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getLinkedMPWithPI(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject getLinkedAdvanceMPWithPI(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject deleteBadDebtPurchaseInvoiceMapping(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPurchaseReturnDetailTermMap(HashMap<String, Object> PurchaseReturnDetailTermMap) throws ServiceException;
    
    public KwlReturnObject savePurchaseReturnDetailsTermMap(HashMap<String, Object> PurchaseReturnDetailsTermsMap) throws ServiceException;

    public KwlReturnObject saveOrUpdateGRODetailsTermsMap(HashMap<String, Object> dataMap) throws ServiceException;
    
    public JSONArray getGRODetailsTermMap(String invoiceId) throws ServiceException ;
    
    public KwlReturnObject getGRODetailsTermMap(HashMap<String, Object> hm) throws ServiceException ;
    
    public boolean deleteGRODetailsTermMap(String invoiceId) throws ServiceException ;
    
    public boolean deleteGRDetailsTermMap(String invoiceId) throws ServiceException ;
    
    public KwlReturnObject saveExciseDetails(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject getExciseDetails(String receiptId) throws ServiceException;
    
    public KwlReturnObject getExciseDetailsAssetQuotation(String receiptId) throws ServiceException;
    
    public KwlReturnObject getExciseDetailsAssetPurchaseOrder(String receiptId) throws ServiceException;
    
    public List getStoreManagerListByGROrderId(String companyId,String GROrderId) throws ServiceException;
    
    public List getGoodsRecieptVatDetails(HashMap params);
    
    public List getGoodsRecieptIndiaTaxDetails(HashMap params);
    
    public void updateGoodsRecieptTaxPaidFlag(HashMap params);
    
    public void resetGoodsRecieptTaxPaidFlag(HashMap params);
    
    public void updateGoodsRecieptTDSPaidFlag(HashMap params);
    
    public void resetGoodsRecieptTDSPaidFlag(HashMap params);
    
    public KwlReturnObject getTaxPaymentFromGoodsReciept(String greceiptid, String companyId) throws ServiceException;

    public KwlReturnObject saveExciseTemplateMapping(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject updateGoodsReceiptOrderSetNull(GoodsReceiptOrder grOrder);

    public KwlReturnObject getGROFromJE(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject updatePurchaseReturnSetNull(PurchaseReturn purchaseReturn);

    public KwlReturnObject getPRFromJE(String jeid, String companyid) throws ServiceException;    
    
    public KwlReturnObject getGoodsReceiptFromJE(String jeid, String companyid) throws ServiceException;
    
     public boolean updateGoodsReceiptExciseDuty(GoodsReceipt grId)throws ServiceException;
     
    public KwlReturnObject getGoodsReceiptdetailTermMapForRG(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderInfoUsingGROD(String grodetailId, String companyID);
    
    public KwlReturnObject getDataTDSChallanControlReport(HashMap<String, Object> requestParams) throws ServiceException;
    
    public String getDeducteeTypeForTDSChallanControlReport(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDataSTInputCreditSummaryForReport(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveUpdateDealerExciseDetails(JSONObject jobj) throws ServiceException;
    
    public KwlReturnObject getDealerExciseDetails(HashMap<String, Object>requestParams) throws ServiceException;
    
    public KwlReturnObject getGoodsReceiptsForDealerExciseDetails(HashMap<String, Object>requestParams) throws ServiceException;
    
    public KwlReturnObject getutilizedGoodsReceiptsForDealerExciseDetails(HashMap<String, Object>requestParams) throws ServiceException;
    
    public KwlReturnObject getGRfromPI(String pi) throws ServiceException;
    
    public KwlReturnObject saveUpdateDealerExciseTermDetails(JSONObject jobj,DealerExciseDetails ded) throws ServiceException;
    
    public KwlReturnObject getDealerExciseTermDetails(HashMap<String, Object>requestParams) throws ServiceException;
     
    public KwlReturnObject getSupplierExciseDetailsMapping(String greceiptid, String companyid) throws ServiceException;
    
    public boolean isTaxApplied(HashMap<String,Object> data, int taxtype) throws ServiceException;
    
    public KwlReturnObject getGenricPurchaseReturndetailTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public List getPurchaseReturnVatDetails(HashMap params);
    
    public void updatePurchaseReturnTaxPaidFlag(HashMap params);
    
    public void resetPurchaseReturnTaxPaidFlag(HashMap params);
    
    public KwlReturnObject getProductsFromGoodReceiptOrder(String billid, String companyid) throws ServiceException;

    public KwlReturnObject getProductsFromPurchaseReturn(String billid, String companyid) throws ServiceException;
    public void updateAdvancePaymentTDSUsedFlag(HashMap params);
    
    public boolean deleteGoodsReceiptPaymentMapping(String GoodsReceiptId) throws ServiceException ;
    
    public KwlReturnObject getLandedInviceList(String GoodsReceiptId,String landingCostCategory) throws ServiceException ;
    
    public KwlReturnObject getGoodsReceipt_LandedInvoice(Map<String, Object> requestMap) throws ServiceException; 

    public KwlReturnObject getTDSAppliedVendorInvoices(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAdvancePaymentDetailsUsedInGoodsReceipt(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getNumberEligiableItem(String GoodsReceiptId ,String landingCostCategory) throws ServiceException ;
    
    public KwlReturnObject getManualProductCostLCC(String GoodsReceiptId ,String landingCostCategory) throws ServiceException ;

    public KwlReturnObject deleteGoodsReceiptsLandedInvoice(String receiptid, String companyid) throws ServiceException;

    public KwlReturnObject getGoodsReceiptForCommissionSchema(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getGoodsReceiptsWithGlobalTax(HashMap<String,Object> map) throws ServiceException;

    public KwlReturnObject getGoodsReceiptsWithLineLevelTax(HashMap<String,Object> map) throws ServiceException;

    public KwlReturnObject saveManualLandedCostCategoryDetails(HashMap<String, Object> reqParams) throws ServiceException;
    
    public List getTerms(String tax) throws ServiceException;
    
    public List isInvoiceNotLinkedWithAnyGR(GoodsReceipt invoice) throws ServiceException;
    
    public boolean isLinkingOfGRInPI(GoodsReceipt invoice) throws ServiceException;
    
    public KwlReturnObject getJobOrderSubgredients(Map<String, Object> map) throws ServiceException;
    
    public KwlReturnObject saveGRODetailsStockOutISTMapping(Map<String, Object> mappingParams) throws ServiceException;
    
    public double getSumofChallanUsedQuantity(String interstoretransfer) throws ServiceException;
    
    public KwlReturnObject deleteGRODetailISTMapping(JSONObject json) throws ServiceException;

    public void updateMemoForIST(String memo, String grorderId, String companyid);
    
    public void updateMemoForJWOSA(String stockAdjustmentmemo, String grorderId, String companyid);
 
    public KwlReturnObject getGoodsReceiptsLinkedWithPR(String purchasereturnid) throws ServiceException ;
    
    public KwlReturnObject getStockOutGRNMapping(Map<String, Object> reqMap) throws ServiceException;
    
    public void deleteStockAdjustment(Map<String,Object> reqMap) throws ServiceException;
    
    public void updateQuantityinStock(Map<String,Object> reqMap) throws ServiceException;
    
    public List<GoodsReceiptLinking> getGoodsReceiptLinkingDataToValidateLinkingInfo(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPaymentDetailsOfGR(String grID) throws ServiceException;
    
    public KwlReturnObject getLinkedPaymentDetailsOfGR(String grID) throws ServiceException;
    
    public KwlReturnObject getLinkedDebitNoteDetailsOfGR(String grID) throws ServiceException;
    
    public KwlReturnObject getCalculatedDebitNoteTax(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getGoodsReceiptCountForImport(JSONObject grDetails)  throws ServiceException;
            
    public Map<String,Object> getPurchaseReturnQuantityLinkWithPO(JSONObject requestParams)  throws ServiceException;
            
    public KwlReturnObject getTransactionsForRoundingJE(int moduleid,String companyid)  throws ServiceException;
    
    public List getGoodsReceiptListForLinking(Map<String, Object> requestParams) throws ServiceException;
    
    public List getGoodsReceiptDOLinkingList(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getGoodsReceiptTDSPayment(String greceiptid, String companyid) throws ServiceException;
    
    public KwlReturnObject listLinkedTDSPaymentDetail(String greceiptid, String companyid) throws ServiceException;
    
    public KwlReturnObject getGoodsReceiptDetailsTDSForJE(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject getExpenseReceiptDetailsTDSForJE(HashMap<String, Object> hm) throws ServiceException;
    
    public TdsJEMapping saveTDSJEMapping(JournalEntry je, HashMap<String, Object> paramJobj, double rate, double tdsLineAmount,GoodsReceiptDetail grd, ExpenseGRDetail erd);

    public List<String> getGoodsReceiptOrderWithNonSaleItem(String companyID) throws ServiceException;
    
    public HashSet saveUpdateAdvancePaymentMapping(JSONObject jobjAppliedTDS, GoodsReceiptDetail grd,ExpenseGRDetail erd, Company companyid);
    
    public KwlReturnObject deleteTDSAdvancePaymentMapping(String receiptid, String companyid,boolean isExpenseInv) throws ServiceException, AccountingException;
    
    public KwlReturnObject setLandedInvoiceJEDMappingToNULL(String grid, String companyid);
    
    public KwlReturnObject saveLandingCostDetailMapping(JSONObject json);
    
    public KwlReturnObject deleteLandingCostDetailMapping(String receiptid, String companyid,boolean isExpenseType) throws ServiceException, AccountingException;
    
    public KwlReturnObject getGoodsReceiptDetailForLandingCategory(String goodsReceiptId, String landingCategoryId) throws ServiceException;
    
    public KwlReturnObject getReceiptTermMapFromGRDetail(String goodsReceiptDetailId) throws ServiceException;
    
    public KwlReturnObject getReceiptTermMapList(Map<String, Object> requestParams) throws ServiceException;
    
    public Map<String,Object> getGlobalandLineLevelTaxForGoodsReceipt(String grid, String companyid) throws ServiceException;
    
    public KwlReturnObject getProductDetailsFromGoodsReceipt(JSONObject params) throws ServiceException;
    
    public List<Object> getGRIDfromGROID(String grorderid,String companyid) throws ServiceException;
    
    public KwlReturnObject getManualProductCostLCC(String GoodsReceiptId, String landingCostCategory, String assetDetailId) throws ServiceException;
    
    public KwlReturnObject getAllLandedInvoices(String companyid) throws ServiceException; 
    
    public KwlReturnObject getExternalCurrencyRateForGoodsReceipt(String grId, String companyId) throws ServiceException;
    
    public KwlReturnObject getExpenseInvoiceNumbersFromGoodsReceipt(String invoiceid, String companyid) throws ServiceException; 

    public KwlReturnObject isLandedCostWithTermTransactionsPresent(JSONObject params) throws ServiceException;
    
    public List isAllITCReversal(JSONObject reqParams) throws ServiceException, JSONException;
    
    public List getITCGLForProducts(JSONObject reqParams) throws ServiceException,JSONException;
    
    public KwlReturnObject getLandingCostDetailMapping(HashMap<String, Object> requestParams) throws ServiceException ;
}
