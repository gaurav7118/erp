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
package com.krawler.spring.accounting.purchaseorder;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.PurchaseOrder;
import com.krawler.hql.accounting.PurchaseOrderTermMap;
import com.krawler.hql.accounting.PurchaseOrderVersion;
import com.krawler.hql.accounting.VendorQuotation;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.ScriptException;

/**
 *
 * @author krawler
 */
public interface accPurchaseOrderDAO {

    public KwlReturnObject addPurchaseOrder(HashMap hm) throws ServiceException;

    public KwlReturnObject updatePurchaseOrder(HashMap hm) throws ServiceException;

    public KwlReturnObject deletePurchaseOrder(String poid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteSecurityGate(String securityId, String companyid) throws ServiceException;

    public KwlReturnObject deletePurchaseOrdersPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deletePurchaseOrderVersoning(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteSecurityGateEntryPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updatePoIsUsedDeleteSGE(String sgeID, String companyId,boolean isPoUsed);
    
    public KwlReturnObject getPurchaseOrders(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getCheckPOForSGE(String poid, String companyid) throws ServiceException;
    
    public KwlReturnObject updatePOLinkflag(HashMap<String, Object> requestParams) throws ServiceException ;
    
    public KwlReturnObject getSGIDFromPOD(String poid, String secID) throws ServiceException;
    
    public KwlReturnObject getSecurityGateEntry(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrdersIDForDiamondAviation(String vquotationID,String company) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrdersDetailsIDForDiamondAviation(String vquotationID,String company) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrdersDetailsIDFromPO(String vpoID,String company) throws ServiceException;

    public KwlReturnObject getPurchaseOrdersMerged(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getSecurityGateEntryMerged(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getOpeningBalancePurchaseOrders(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPOCount(String pono, String companyid) throws ServiceException;
    
    public KwlReturnObject getSecurityEntryCount(String securityNo, String companyid) throws ServiceException;
    
    public KwlReturnObject updatePoIsUsedForSecurityGateEntry(String poId, String companyid,boolean isPoUsed) throws ServiceException;

//    public KwlReturnObject savePODetails(JSONArray podjarr, String poid, String companyid, boolean issave) throws ServiceException;

    public KwlReturnObject savePurchaseOrder(HashMap<String, Object> dataMap) throws ServiceException,AccountingException;
    
    public KwlReturnObject saveSecurityGateEntry(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveVQLinking(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject savePurchaseRequisitionLinking(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveRFQLinking(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject savePOLinking(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject checkPOIsPresentInLinkingTable(String docid, String linkeddocid) throws ServiceException;
    
    public KwlReturnObject getSalesOrderNumber(String salesOrderID) throws ServiceException;
    
    public KwlReturnObject deleteLinkingInformationOfPO(HashMap<String, Object> dataMap) throws ServiceException;
    
   public KwlReturnObject deleteLinkingInformationOfVQ(HashMap<String, Object> dataMap) throws ServiceException;
   
    public KwlReturnObject deleteLinkingInformationOfRFQ(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject savePurchaseOrderDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveSecurityGateEntryDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveExpensePurchaseOrderDetails(Map<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject savePurchaseOrderOtherDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getPO_Product(Map<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getCQ_Product(Map<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getVQ_Product(Map<String, Object> requestMap) throws ServiceException; 
    
    public KwlReturnObject getPReq_Product(Map<String, Object> requestMap) throws ServiceException; 
    
    public KwlReturnObject getRFQ_Product(Map<String, Object> requestMap) throws ServiceException; 
    
    public KwlReturnObject getSR_Product(Map<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getPR_Product(Map<String, Object> requestMap) throws ServiceException;
     
    public KwlReturnObject getPodForProduct(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getPOforinvoice(String poid, String companyid) throws ServiceException;
    
    public KwlReturnObject getExpensePOforinvoice(String poid, String companyid) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderByProduct(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getGROforinvoice(String poid, String companyid) throws ServiceException;
    
    public KwlReturnObject getGROforSGE(String poid, String companyid) throws ServiceException;

    public KwlReturnObject getSOforPO(String poid, String companyid) throws ServiceException;
    
    public KwlReturnObject getPOforSecurityGate(String poid, String companyid) throws ServiceException;

    public KwlReturnObject getPurchaseOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getSelectedPurchaseOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getSecurityGateDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getExpensePurchaseOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderOtherDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPODetailsCustomDataForProduct(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBillingPurchaseOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBPOCount(String pono, String companyid) throws ServiceException;

    public KwlReturnObject saveBillingPurchaseOrder(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveBillingPurchaseOrderDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getBillingPurchaseOrders(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject deleteBillingPurchaseOrder(String poid, String companyid) throws ServiceException;

    public KwlReturnObject deletePurchaseOrderDetails(HashMap<String, Object> requestParams) throws AccountingException;
    
    public KwlReturnObject deleteSecurityGateEntryDetails(HashMap<String, Object> requestParams) throws AccountingException;
    
    public KwlReturnObject deletePurchaseOrderExpenseDetails(HashMap<String, Object> requestParams) throws AccountingException;

    public KwlReturnObject deletePurchaseOrderOtherDetails(String poid, String companyid) throws AccountingException;

    public KwlReturnObject deleteBillingPurchaseOrderDetails(String poid, String companyid) throws AccountingException;

    public int approvePendingOrder(String qid, boolean isbilling, String companyid, String userid) throws ServiceException;
    
    public KwlReturnObject approvePendingPurchaseOrder(String poID, String companyid, int status) throws ServiceException;
   
    public KwlReturnObject setApproverForPurchaseOrder(String poID, String companyid,String approverID) throws ServiceException;
    
    public KwlReturnObject rejectPendingPurchaseOrder(String poID, String companyid) throws ServiceException;

    public int pendingApprovalOrdersCount(String companyid) throws ServiceException;

    public KwlReturnObject saveVendorQuotation(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveQuotationDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getQuotationCount(String qno, String companyid) throws ServiceException;

    public KwlReturnObject getQuotations(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getQuotationsForScript(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getGRForScript(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getAllCompanyFromDb(String [] subdomain) throws ServiceException;

    public KwlReturnObject updateVQLinkflag(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updatePRisOpenInPOFlag(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getQuotationDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteQuotation(String qid, String companyid) throws ServiceException;

    public KwlReturnObject archieveQuotation(String qid, String companyid) throws ServiceException;

    public KwlReturnObject unArchieveQuotation(String qid, String companyid) throws ServiceException;

    public KwlReturnObject savePurchaseRequisition(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject savePurchaseRequisitionDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deletePurchaseRequisition(String prid, String companyid) throws ServiceException;

    public KwlReturnObject deletePurchaseRequisitionPermanent(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject rejectPurchaseRequisition(String prid, String companyid) throws ServiceException;

    public KwlReturnObject deletePurchaseRequisitionDetails(String poid, String companyid) throws ServiceException;

    public KwlReturnObject getPurchaseRequisitionCount(String qno, String companyid) throws ServiceException;

    public KwlReturnObject getPurchaseRequisition(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getPendingPurchaseRequisition(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getPurchaseRequisitionDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject savePurchaseRequisitionFlow(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deletePurchaseRequisitionFlow(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getRequisitionFlowData(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getRequisitionFlowTargetUsers(String flowid) throws ServiceException;

    public KwlReturnObject approvePendingRequisition(String prID, String companyid, int status) throws ServiceException;

    public KwlReturnObject deleteQuotationsPermanent(HashMap<String, Object> requestParams) throws ServiceException;

    // RFQ
    public KwlReturnObject getRequestForQuotations(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getRFQDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getRFQCount(String rfqno, String companyid) throws ServiceException;

    public KwlReturnObject saveRFQDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveRFQ(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteRFQ(String prid, String companyid) throws ServiceException;

    public KwlReturnObject deleteRFQPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteRequestForQuotationAssetDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getOutstandingPurchaseOrders(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject saveVendorQuotationTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updateVendorQuotationTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject saveVendorQuotationDetailsTermMap(HashMap<String, Object> hm) throws ServiceException; //For India Country terms used at product level
    
    public KwlReturnObject getVendorQuotationProductTermDetails(Map<String,Object> mapData) throws ServiceException;
    
    public KwlReturnObject getVQforinvoice(String qid, String companyid) throws ServiceException;
    
    public KwlReturnObject getVQforCQ(String qid, String companyid) throws ServiceException;

    public KwlReturnObject getVQforPO(String qid, String companyid) throws ServiceException;

    public KwlReturnObject getGRDFromVQD(String poid) throws ServiceException;

    public KwlReturnObject getPODFromVQD(String poid) throws ServiceException;

    public KwlReturnObject getVendorQuotationTermMap(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject savePurchaseOrderTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updatePurchaseOrderTermMap(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject savePurchaseOrderDetailsTermMap(HashMap<String, Object> hm) throws ServiceException; //For India Country terms used at product level
    
    public KwlReturnObject saveSGEDetailsTermMap(HashMap<String, Object> hm) throws ServiceException; //For India Country terms used at product level
    
     public KwlReturnObject getPurchaseOrderDetailsTermMap(Map<String,Object> mapData) throws ServiceException;
     
    public KwlReturnObject getSGEDetailsTermMap(Map<String,Object> mapData) throws ServiceException;
     
    public KwlReturnObject getPurchaseOrderTermMap(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject updateVendorQuotationCustomData(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject updatePurchaseRequisitionCustomData(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updateRFQCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updatePurchaseRequisitionDetailCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateRFQDetailCustomData(HashMap<String, Object> requestParams) throws ServiceException ;

    public KwlReturnObject updateVQuotationDetailsCustomData(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updateVQuotationDetailsProductCustomData(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject deleteQuotationDetails(String qid, String companyid) throws ServiceException, AccountingException;

    //QA Approval Functions
    public KwlReturnObject getQAApprovalItems(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject approvePendingVendorQuotation(String vqID, String companyid, int status) throws ServiceException;

    public KwlReturnObject rejectPendingVendorQuotation(String vqID, String companyid) throws ServiceException;
    
    public KwlReturnObject updateQAApprovalItems(HashMap<String, Object> request) throws ServiceException;

    public String[] columSort(String Col_Name, String Col_Dir) throws ServiceException;

    public String columSortVendorQutation(String Col_Name, String Col_Dir) throws ServiceException;

    public KwlReturnObject getPOEditCount(String entryNumber, String companyid, String poid) throws ServiceException;
    
    public KwlReturnObject getSGEEditCount(String entryNumber, String companyid, String securityId) throws ServiceException;

    public KwlReturnObject getQuotationEditCount(String entryNumber, String companyid, String quotationId) throws ServiceException;

    public KwlReturnObject getEditPurchaseRequisitionCount(String entryNumber, String companyid, String poid) throws ServiceException;
    
    public KwlReturnObject getEditRFQCount(String entryNumber, String companyid, String poid) throws ServiceException;

    public KwlReturnObject getPR_Product(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getVQ_Product(String productid, String companyid) throws ServiceException;
    
    public Boolean checkForRule(int level, String companyid, String amount, String userid) throws AccountingException, ServiceException, ScriptException;
    
    public boolean checkIfRequisitionLinkedInVendorQuotation(String prid,String companyid) throws ServiceException;
    
    public boolean checkIfRequisitionLinkedInPurchaseOrder(String prid,String companyid) throws ServiceException;
    
    public boolean checkIfRFQLinkedInVendorQuotation(String prid) throws ServiceException;
    
    public KwlReturnObject getBudgeting(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveBudgeting(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPuchaseRequisitionInvoiceAmount(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getApprovedPurchaseRequisitionAmountWhoseInvoiceIsNotCreated(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveApprovalDocMap(HashMap<String, Object> approvalDocMap) throws ServiceException;
    
    public KwlReturnObject deletePurchaseRequisitionAssetDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteVendorQuotationAssetDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deletePurchaseOrderAssetDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getIDFromPurchaseRequisitionDetails(String pReqID) throws ServiceException;
    
    public KwlReturnObject getJobWorkOutPurchaseOrder(String companyid) throws ServiceException;
    
    public KwlReturnObject getJobWorkOutPurchaseOrderSinglePO(String companyid,String id) throws ServiceException;

    //ERP-10941
    public KwlReturnObject getPR_AssetProduct(String productid, String companyid) throws ServiceException;;

    public KwlReturnObject getPR_AssetRequisitionMappingProduct(String productid, String companyid)throws ServiceException ;

    public KwlReturnObject getRFQDetails(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getPurchaseOrderLinkedWithVQ(String billid, String companyid)throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderLinkedWithPR(String billid, String companyid)throws ServiceException;
    
    public KwlReturnObject getPurchaseInvoiceLinkedWithVQ(String billid, String companyid)throws ServiceException;
    
    public KwlReturnObject getCQLinkedWithVQ(Map requestparams) throws ServiceException;
    
    public KwlReturnObject getPurchaseRequisitionsLinkedWithVQ(Map requestparams) throws ServiceException;
    
    public KwlReturnObject getPurchaseRequisitionsLinkedWithPO(Map requestparams) throws ServiceException;
    
    public KwlReturnObject getRFQLinkedWithVQ(Map requestparams) throws ServiceException;
    
    public KwlReturnObject getVendorQuotationLinkedWithPR(String billid, String companyid)throws ServiceException;

    public String updatePOEntryNumberForNewPO(Map<String, Object> seqNumberMap);
    
    public String updatePOEntryNumberForNewSecurityGateNo(Map<String, Object> seqNumberMap);

    public String updateVQEntryNumberForNewVQ(Map<String, Object> seqNumberMap);

    public String updateVQEntryNumberForNA(String vqidm,String entrynumber);

    public String updatePOEntryNumberForNA(String poid,String entrynumber);
    
    public KwlReturnObject getAllVendorQuotaionsByCompanyid(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAllGlobalVendorQuotaionsOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAllGlobalPurcahseOrderOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException;
    
    public boolean updateVendorQuotationAmount(VendorQuotation quotation, JSONObject json) throws ServiceException ;
    
    public KwlReturnObject getAllPurchaseOrderByCompanyid(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAllPurchaseReturnByCompanyid(HashMap<String, Object> requestParams) throws ServiceException;
     
    public KwlReturnObject getAllSalesReturnByCompanyid(HashMap<String, Object> requestParams) throws ServiceException;
    
    public boolean updatePurchaseOrderAmount(PurchaseOrder po, JSONObject json) throws ServiceException ;
    
    public KwlReturnObject deleteVendorQuotationTermMap(HashMap<String, Object> hm) throws ServiceException ;
    
    public KwlReturnObject deletePOTermMap(HashMap<String, Object> hm) throws ServiceException ;
    
    public KwlReturnObject getQuotationVersionCount(String quotationid, String companyid) throws ServiceException;
    
    public KwlReturnObject saveQuotationVersion(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject updateQuotationVersionCustomData(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject saveQuotationVersionDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject updateQuotationVersionDetailsCustomData(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getVersionQuotations(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getQuotationVersionDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteQuotationVersion(String vid, String companyid) throws ServiceException;
    
    public KwlReturnObject getLinkedVendorInvoiceWithPO(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedGoodsReceiptWithPO(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedPurchaseOrderWithGR(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedGoodsReceiptWithPI(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedPurchaseReturnWithPI(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedPurchaseReturnWithGR(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedVendorInvoiceWithGR(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedVendorInvoiceWithVQ(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedPurchaseOrderWithVQ(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject deleteQuotationVersionsPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getLinkedDebitNoteWithPI(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedVQWithPurchaseRequisition(HashMap<String, Object> hm) throws ServiceException;
    
     public KwlReturnObject getLinkedRFQWithPurchaseRequisition(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedSOWithPO(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject checkEntryForVendorQuotationLinkingTable(String vendorQuotationID, String PurchaseRequisitionID) throws ServiceException;
    
    public KwlReturnObject checkEntryForRFQLinkingTable(String vendorQuotationID, String PurchaseRequisitionID) throws ServiceException;
    
    public void updateRequisitionEntryNumber(Map<String, Object> seqNumberMap);
    
    public KwlReturnObject getLinkedCQWithVQ(HashMap<String, Object> hm) throws ServiceException;
    
     public KwlReturnObject savePurchaseOrderStatusForSO(HashMap<String, Object> dataMap) throws ServiceException;
     
    public void updateRFQEntryNumber(Map<String, Object> seqNumberMap) throws ServiceException;; 
    
    public KwlReturnObject getPurchaseOrderFromDeliveryPlanner(String poid, String companyid) throws ServiceException;
    
    public KwlReturnObject getRFQLinkedWithPR(String billid, String companyid)throws ServiceException;
    
    public KwlReturnObject getPRLinkedInRFQ(Map request)throws ServiceException;
     
    public KwlReturnObject getVQLinkedInRFQ(Map request)throws ServiceException;
    
    public KwlReturnObject closeDocument(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject closeLineItem(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject checkWhetherPOIsUsedInGROrNot(String poDetailID, String companyID) throws ServiceException;
    
    public KwlReturnObject saveAssetExciseDetails(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject getRelevantPurchaseOrderLinkingWise(HashMap<String, Object> request) throws ServiceException;//fro filtering outstanding sales order

    //public List getPurchaseRerturnVatDetails(String companyId);
    
    public KwlReturnObject getPurchaseOrderDetailsForBulkInvoices(List soId,String companyId) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderDetails(String doId,String companyId) throws ServiceException;
    
    public KwlReturnObject getStockTransferFromJobWorkOutOrder(String poid, String companyid) throws ServiceException;
    
    
    public double checkPurchaseReturnAndgetReturnQuantity(JSONObject requestParams) throws ServiceException;
    
     public KwlReturnObject getPurchaseOrderDetailLinkedWithRequisitionDetail(HashMap<String, Object> request) throws ServiceException;
     
     public KwlReturnObject updateBalanceQuantityOfRequisitionDetail(HashMap<String, Object> requestParams) throws ServiceException;
     
     public KwlReturnObject getLinkedSO(JSONObject request) throws ServiceException;
         
    public KwlReturnObject getPurchaseOrderVersionCount(String poid, String companyid) throws ServiceException;

    public KwlReturnObject savePurchaseOrderVersion(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject savePurchaseOrderVersionDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject updatePurchaseOrderVersionDetailsCustomData(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updateExpensePOVersionDetailsCustomData(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updatePurchaseOrderVersionCustomData(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderTermDetails(String podId) throws ServiceException;
    
    public void savePurchaseOrderVersionTermDetails(HashMap<String, Object> povdtmMap) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderVersionDetailsTermMap(Map<String, Object> mapData) throws ServiceException;
    
    public void savePurchaseOrderVersionTermMap(PurchaseOrderTermMap potm, PurchaseOrderVersion pov) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderVersionTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderDetailsTermMapForVersion(Map<String, Object> mapData) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderDetailsForVersion(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getExpensePODetailsForVersion(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPurchaseOrderVersionDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getExpensePurchaseOrderVersionDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveExpensePurchaseOrderVersionDetails(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderVersions(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject deleteSecurityGateEntryDetailsTermMap(String securityDetailId, String companyId, Map<String, Object> requestParams) throws AccountingException;

    public double getPOStatusOnBalanceQty(String poid, String companyId) throws ServiceException;
}
