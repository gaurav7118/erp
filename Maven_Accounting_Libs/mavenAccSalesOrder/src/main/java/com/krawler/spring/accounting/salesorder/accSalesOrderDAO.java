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
package com.krawler.spring.accounting.salesorder;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ConsignmentRequestApprovalRule;
import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.*;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;

/**
 *
 * @author krawler
 */
public interface accSalesOrderDAO {
// Sales Order

    public KwlReturnObject getSalesOrders(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getReplacementRequests(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getMaintenanceRequests(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getSalesOrdersMerged(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getOpeningBalanceSalesOrders(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSalesOrderCount(String orderno, String companyid) throws ServiceException;

    public KwlReturnObject deleteSalesOrder(String soid, String companyid) throws ServiceException;

    public KwlReturnObject deleteSalesOrdersPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteLinkingInformationOfSO(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSOforinvoice(String soid, String companyid, boolean includeSoftDeletedSO) throws ServiceException;//for checking SO Used in invoice or not

    public KwlReturnObject getDOforinvoice(String soid, String companyid, boolean includeSoftDeletedDO) throws ServiceException;//for checking SO Used in delivery order or not

    public KwlReturnObject getOutstandingSalesOrders(HashMap<String, Object> request) throws ServiceException;//fro filtering outstanding sales order

    public KwlReturnObject saveSalesOrder(HashMap<String, Object> dataMap) throws ServiceException,AccountingException;
    
    public KwlReturnObject saveSalesOrderLinking(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getPODetail(String poDetailID) throws ServiceException;
    
    public KwlReturnObject updateEntryInPurchaseOrderLinkingTable(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderNumber(String purchaseOrderID) throws ServiceException;

    public KwlReturnObject saveAssemblySubProdmapping(HashMap<String, Object> assemblyMap) throws ServiceException;

    public KwlReturnObject saveSalesOrderDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveSalesOrderDetailsVendorMapping(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveQuotationDetailsVendorMapping(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getSO_Product(Map<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject getSalesOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getSodForProduct(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getCRPendingApprovalSalesOrderDetails(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getCRPendingApprovalSalesOrderDetailsModified(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getPendingConsignmentRequests(String companyid,String prodidStrings) throws ServiceException;
    
    public KwlReturnObject  UpdateObject(Object object) throws ServiceException;
    
//    public KwlReturnObject  UpdateConsignmentRequestLockQuantity(SalesOrderDetail salesOrderDetail) throws ServiceException;
    
    public KwlReturnObject getSerialsFormDocumentid(String soid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSalesOrderDetailsVendorMapping(String sodid) throws ServiceException;
    
    public KwlReturnObject getQuotationDetailsVendorMapping(String qdid) throws ServiceException;

    public KwlReturnObject getSOContainingProject(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getSerialForBatch(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAssemblySubProductlist(String productid, String companyid) throws ServiceException;

    public int saveCustomDataForRecurringSO(String New_JE_ID, String Old_JE_ID, boolean JE_OR_JED) throws ServiceException;
// Billing Sales Order

    public KwlReturnObject getBillingSalesOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBillingSalesOrderCount(String orderno, String companyid) throws ServiceException;

    public KwlReturnObject saveBillingSalesOrder(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveBillingSalesOrderDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getBillingSalesOrders(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject deleteBillingSalesOrder(String soid, String companyid) throws ServiceException;

    public KwlReturnObject deleteSalesOrderDetails(String soid, String companyid,boolean isLeaseFixedAsset,boolean isConsignment,boolean isJobWorkOrderReciever) throws ServiceException,AccountingException;

    public KwlReturnObject deleteBillingSalesOrderDetails(String soid, String companyid) throws ServiceException;

// Quotation
    public KwlReturnObject saveQuotationDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveQuotationVersionDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveQuotation(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveQuotationLinking(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveQuotationVersion(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getQuotations(HashMap<String, Object> request) throws ServiceException;
            
    public KwlReturnObject getVersionQuotations(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getQuotationDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject checkQuotationLinkedWithAnotherInvoice(String quotationid) throws ServiceException;
    
    public KwlReturnObject checkQuotationLinkedWithAnotherSalesOrder(String quotationid) throws ServiceException;
    
    public KwlReturnObject getLinkedInvoiceWithCQ(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedInvoiceWithSO(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedDeliveryOrderWithSO(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedDeliveryOrderWithInvoice(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedSalesReturnWithInvoice(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getCreditNoteLinkedWithInvoice(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedInvoicesWithDeliveryOrder(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedSalesReturnWithDeliveryOrder(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getLinkedSalesOrderWithCQ(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getQuotationVersionDetails(HashMap<String, Object> requestParams) throws ServiceException;
            
    public KwlReturnObject getVendorQuotationDetails(String vqid, String companyid) throws ServiceException;

    public KwlReturnObject deleteQuotation(String qid, String companyid) throws ServiceException;
    
    public KwlReturnObject updateCQLinkflag(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject archieveQuotation(String qid, String companyid) throws ServiceException;

    public KwlReturnObject unArchieveQuotation(String qid, String companyid) throws ServiceException;

    public KwlReturnObject getQuotationCount(String qno, String companyid) throws ServiceException;

    public int approvePendingOrder(String qid, boolean isbilling, String companyid, String userid) throws ServiceException;

    public KwlReturnObject approvePendingCustomerQuotation(String cqID, String companyid, int status) throws ServiceException;
    
    public KwlReturnObject approvePendingSalesOrder(String soID, String companyid, int status) throws ServiceException;

    public KwlReturnObject rejectPendingCustomerQuotation(String cqID, String companyid) throws ServiceException;
    
    public KwlReturnObject rejectPendingSalesOrder(String soID, String companyid) throws ServiceException;

    public KwlReturnObject deleteBatchDetailsAfterRejectPendingSalesOrder(String soID, String companyid) throws ServiceException;

    public int pendingApprovalOrdersCount(String companyid) throws ServiceException;

    public KwlReturnObject saveQuotationTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updateQuotationTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject deleteQuotationTermMap(HashMap<String, Object> hm) throws ServiceException ;
    
    public KwlReturnObject deleteSOTermMap(HashMap<String, Object> hm) throws ServiceException ;
    
    public KwlReturnObject getQuotationTermMap(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject saveSalesOrderTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updateSalesOrderTermMap(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject getSalesOrderTermMap(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject updateQuotationCustomData(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updateQuotationVersionCustomData(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject updateQuotationDetailsCustomData(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updateQuotationDetailsProductCustomData(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject updateQuotationVersionDetailsCustomData(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getQTforinvoice(String qid, String companyid) throws ServiceException;

    public KwlReturnObject getSOforQT(String qid, String companyid) throws ServiceException;

    public KwlReturnObject getPOforSO(String qid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSalesOrderByProduct(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject deleteQuotationsPermanent(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteQuotationVersion(String vid, String companyid) throws ServiceException;
    public KwlReturnObject deleteQuotationVersionsPermanent(HashMap<String, Object> requestParams) throws ServiceException;

    // Asset
    public KwlReturnObject deleteAssetDetailsLinkedWithSO(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteQuotationDetails(String qid, String companyid) throws ServiceException;

    public String[] columSort(String Col_Name, String Col_Dir) throws ServiceException;

    public String columSortCustomerQutation(String Col_Name, String Col_Dir) throws ServiceException;

    public KwlReturnObject saveRepeateSalesOrderInfo(HashMap<String, Object> dataMap) throws ServiceException;
  
    public int checkConsignmentApprovalRules(String ruleId, String requestorid, String warehouseid, String locations, String approverid, String companyid) throws ServiceException;
    
    public void saveConsignmentApprovalRules(ConsignmentRequestApprovalRule approvalRule, String id, String locations) throws ServiceException;
    
    KwlReturnObject getConsignmentApprovalRules(HashMap<String, Object> requestParams) throws ServiceException;
    
    KwlReturnObject getConsignmentRequestLocationMapping(HashMap<String, Object> requestParams) throws ServiceException;
    
    KwlReturnObject getConsignmentRequestApproverList(String ruleid) throws ServiceException;
    
    public KwlReturnObject deleteConsignmentRequestApproverMapping(String ruleid) throws ServiceException;
    
    KwlReturnObject deleteConsignmentApprovalRules(String companyid, String ruleid) throws ServiceException;
             
    public KwlReturnObject updateSalesOrder(JSONObject json, HashSet details) throws ServiceException;

    public KwlReturnObject saveRepeateSOMemo(HashMap<String, Object> dataMap) throws ServiceException;

    public int DelRepeateSOMemo(String repeateid) throws ServiceException;

    public KwlReturnObject getRepeateSalesOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getRepeateSalesOrderDetailsForExpander(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getRepeateSalesOrder(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSalesOrderEditCount(String entryNumber, String companyid, String soid) throws ServiceException;

    public KwlReturnObject getEditQuotationCount(String entryNumber, String companyid, String quotationId) throws ServiceException;
    
    public KwlReturnObject getQuotationVersionCount(String quotationid, String companyid) throws ServiceException;

    public KwlReturnObject getCQ_Product(String productid, String companyid) throws ServiceException;

    // For Contract Report
    public KwlReturnObject getContractOrders(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractAgreedServices(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractFiles(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractOtherDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractInvoiceDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractNormalInvoiceDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractReplacementInvoiceDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractMaintenanceInvoiceDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractNormalDOItemDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractNormalDOItemDetailsRow(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractReplacementDOItemDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractReplacementDOItemDetailsRow(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractSalesReturnDetails(HashMap<String, Object> requestParams) throws ServiceException;

    //For Cusotmer Contract Details
    public KwlReturnObject getCustomerContractsAgreementDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCustomerContractCostAgreementDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractsOfCompany(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getNextServiceDateOfContract(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPreviousServiceDateOfContract(HashMap<String, Object> requestParams) throws ServiceException;

    //Contract
    public KwlReturnObject getContractCount(String orderno, String companyid,boolean isEdit,String contractId) throws ServiceException;

    public KwlReturnObject saveContract(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveContractDates(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getContractDates(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject deleteContractDates(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getContractInvoice(Contract contract) throws ServiceException;

    public KwlReturnObject getContractDO(Contract contract) throws ServiceException;

    public KwlReturnObject getContractStrtendDates(String contractid) throws ServiceException;

    public KwlReturnObject saveContractDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject updateContractReference(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveContractServiceDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteContractDetails(String coid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteContractDetailTermsMap(String coid, String companyid) throws ServiceException;
    
    public List selectContractDetails(String coid, String companyid) throws ServiceException ;

    public KwlReturnObject deleteServiceDetails(String coid, String companyid, String deletedRecs) throws ServiceException;

    public KwlReturnObject deletecontractMaintenanceSchedule(String coid, String companyid) throws ServiceException;
    public KwlReturnObject deletecontractFiles(String coid) throws ServiceException;
    public KwlReturnObject getReplacementAndMaintenance(String coid) throws ServiceException;   
    public KwlReturnObject getInvoiceAndDeliveryOrderOfContract(String coid) throws ServiceException;   
    
    // Product Replacement
    public KwlReturnObject saveProductReplacement(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject updateProductReplacement(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject updateProductReplacementDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject updateContractFiles(String contractid, String fileid) throws ServiceException;

    public KwlReturnObject getProductReplacementsLinkedWithSalesOrder(HashMap<String, Object> dataMap) throws ServiceException;

    // Product Maintenance
    public KwlReturnObject saveProductMaintenance(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject updateProductMaintenance(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject changeContractStatus(String contractid, String companyid) throws ServiceException;

    public KwlReturnObject changeContractSRStatus(String contractid, int statusid) throws ServiceException;

    // Contract Expiry Notification
    public KwlReturnObject getSalesPersons(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getActiveContracts(HashMap<String, Object> requestParams) throws ServiceException;

    public int setContractExpiryStatus(String contractIDs, String companyid) throws ServiceException;

    public boolean checkQuotationLinkedInSo(String companyid, String qid) throws ServiceException;

    public boolean checkSoLinkedInContract(String companyid, String soid) throws ServiceException;

    public void openMaintenance(String soid, String companyid,String maintenanceId) throws ServiceException;
 
    public KwlReturnObject deleteSalesOrdersBatchSerialDetails(HashMap<String, Object> requestParams) throws ServiceException; 
    
    public void releseSODBatchLockQuantity(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updatebatchlockQuantity(String productid,String documentid,String companyid) throws ServiceException; 
    
    public KwlReturnObject updateSerialslockQuantity(String productid,String documentid,String companyid) throws ServiceException; 
    
    public KwlReturnObject getCutomer(String productid, String companyid) throws ServiceException;
    
    public KwlReturnObject getTerm(String productid, String companyid) throws ServiceException;
    
    public KwlReturnObject getTermMappedToCrmQuotation(HashMap<String, Object> params) throws ServiceException;
    
    public KwlReturnObject getProductReplacement(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getSalesOrderLinkedWithCQ(String billid, String companyid) throws ServiceException;

    public KwlReturnObject getSILinkedWithCQ(String billid, String companyid) throws ServiceException;
    
     public KwlReturnObject getVQLinkedWithCQ(HashMap<String, Object> request) throws ServiceException;
     
    public KwlReturnObject activateDeactivateRecurringSO(String repeateid, boolean isactivate) throws ServiceException;
    
    public KwlReturnObject approveRecurringSO(String repeateid, boolean ispendingapproval) throws ServiceException;
    
    public Object getUserObject(String id) throws ServiceException;   
    
    public KwlReturnObject getRepeateSONo(Date prevDate) throws ServiceException;
    
    public KwlReturnObject updateLeaseOrder(String contractid, String companyid) throws ServiceException;

    public KwlReturnObject deleteContracts(String contractid, String companyid) throws ServiceException;

    public String updateSOEntryNumberForNewSO(Map<String, Object> seqNumberMap);

    public String updateCQEntryNumberForNewCQ(Map<String, Object> seqNumberMap);

    public String updatePREntryNumberForNewPR(Map<String, Object> seqNumberMap);

    public String updateDNEntryNumberForNewPR(Map<String, Object> seqNumberMap);

    public String updateSREntryNumberForNewSR(Map<String, Object> seqNumberMap);

    public void updateConsignmentEntryNumber(String billid,String billno, String companyid);
    
    public String updateCNEntryNumberForNewSR(Map<String, Object> seqNumberMap);
    
    
//    public String updateJournalEntryNumberForNewSI(String sequenceformat,String nextAutoNo,String nextAutoNoInt,String jeId, String companyid);

    public KwlReturnObject getSalesByCustomer(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getSODFromQD(String soid) throws ServiceException;
    
    public KwlReturnObject getQuotationsForCQScript(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCompanyList() throws ServiceException;

    public String updatePREntryNumberForNA(String prid, String entrynumber);

    public String updateDNEntryNumberForNA(String dnid, String dnnumber);

    public String updateCQEntryNumberForNA(String cqid,String entrynumber);

    public String updateSREntryNumberForNA(String srid, String entrynumber);

    public String updateCNEntryNumberForNA(String cnid, String cnnumber);

    public String updateSOEntryNumberForNA(String soid,String entrynumber);
    
    public KwlReturnObject getAllQuotaionsByCompanyid(HashMap<String, Object> requestParams) throws ServiceException;
    
    public boolean updateQuotationAmount(Quotation quotation, JSONObject json) throws ServiceException ;
    
    public KwlReturnObject getAllSalesOrderByCompanyid(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAllGlobalQuotaionsOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAllGlobalSalesOrderOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAllLineLevelQuotaionsOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException;
    
    public boolean updateSalesOrderAmount(SalesOrder so, JSONObject json) throws ServiceException ;
    
    public boolean getSalesorderBatchStatus(String sodid,String companyid) throws ServiceException;
    
    public boolean getSalesorderSerialStatus(String sodid,String companyid) throws ServiceException;
    public KwlReturnObject getPurchaseOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getSalesOrdersLinkedInInvoice(String invoiceid, String companyid) throws ServiceException;
    
    public KwlReturnObject getContractsDO(Map<String, Object> params) throws ServiceException;
    
    public int updateToNullRepeatedSOOfSalesOrder(String invoiceid, String repeateid)throws ServiceException;
    
    public int deleteRepeatedSO(String repeateid)throws ServiceException; 
    
    public KwlReturnObject updateEntryInVendorQuotationLinkingTable(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getVendorQuotationNumber(String vendorQuotationID) throws ServiceException;
    
    public KwlReturnObject deleteLinkingInformationOfCQ(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveSalesOrderStatusForPO(HashMap<String, Object> dataMap) throws ServiceException;
     
    public KwlReturnObject closeDocument(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject closeLineItem(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject rejectLineItem(HashMap<String, Object> requestParams) throws ServiceException;
    
   public KwlReturnObject checkWhetherSOIsUsedInDOOrNot(String soDetailID, String companyID) throws ServiceException;
    
    public KwlReturnObject getCustomerContractsFromCRMAccountID(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getContractFromDOContractMapping(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAccountContractDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getContractProductList(String contractid, String companyid) throws ServiceException;
    
    public JSONArray getBatchSerialByProductID(String productId, String contractid) throws ServiceException;
    
    public Map<String, Double> getOutstandingQuantityCountForProductMap(Map<String, Object> request) throws ServiceException;
    
    /**
     * Following method return id's of DeliveryOrder by companyid and contractid
     *
     * @param productid
     * @param companyid
     * @return
     * @throws ServiceException
     */
    public List<String> getDelivereyOrderID(String contractid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSalesOrderDetailsForPriceVariance(HashMap<String, Object> request) throws ServiceException;
    
    public ProductReplacement buildProductReplacement(ProductReplacement productReplacement, HashMap<String, Object> requestMap);
    
    public NewBatchSerial getBatchSerialByName(String productId, String SerialNumber) throws ServiceException;
    
    public List deleteProductReplacement(String companyID, String replacementid, String replacementNumber) throws ServiceException;
    
    public List getProductReplacementByReplacementNumber(String replacementNumber, String companyId) throws ServiceException;
    
    public List getProductMaintenanceByReplacementNumber(String maintenanceNumber, String companyId) throws ServiceException;
    
    public List deleteProductMaintenence(String companyID, String maintainanceid, String maintenanceNumber) throws ServiceException;
     
    public List getVQdetails(String vqdetailID,String companyid) throws ServiceException;
    
    public List getPOdetails(String poDetailsID,String companyid) throws ServiceException;
    
    public KwlReturnObject getLinkedSalesReturnQuantityWithInvoice(String company,String rowId) throws ServiceException;
    
    public KwlReturnObject saveQuotationDetailsTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getQuotationDetailTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject saveSalesOrderDetailsTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject saveContractDetailsTermsMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getSalesOrderDetailTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getContractDetailTermsMap(HashMap<String, Object> hm) throws ServiceException;
    /**
     * Get GST Rule Is present or not
     * @param hm
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject getGSTRuleSetupForImportSalesOrder(HashMap<String, Object> hm) throws ServiceException;
    
   // public List getSalesReturnVatDetails(String companyId);
    
    public Map<String, SalesOrderInfo> getSalesOrderList(List<String> invoiceIDLIST);
    
    public KwlReturnObject getRelevantSalesOrderLinkingWise(HashMap<String, Object> request) throws ServiceException;//fro filtering outstanding sales order
    
    public KwlReturnObject getMarginCostForCrossLinkedTransactions(HashMap<String, Object> requestParams) throws ServiceException;

    public Map<String, String> getApprovalStatusofSO(String company, Date frmDate, Date toDate,String requestStatus) throws ServiceException;

    public String validateToedit(String formRecord, String billid, boolean isConsignment,Company company);
    
    public KwlReturnObject getSalesOrderDetailsForBulkInvoices(List soId,String companyId) throws ServiceException;
    
    public KwlReturnObject getSalesOrderDetails(String doId,String companyId) throws ServiceException;
    
    public KwlReturnObject getUnInvoicedSalesOrders(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDeliveredQuantityForSalesOrder(String salesOrderDetailID, String companyID) throws ServiceException;
       
    public List getSalesOrderLinkedWithQuotation(String ID) throws ServiceException;

    public List getDeliveryOrderLinkedWithSourceDocument(String ID) throws ServiceException;

    public List getInvoiceLinkedWithSourceDocument(String ID) throws ServiceException;
    
    public KwlReturnObject saveContractService(HashMap<String, Object> hashMap) throws ServiceException;
    
    public KwlReturnObject getMappedFilesResult(Map<String,Object> filemap) throws ServiceException;
    
    public KwlReturnObject  SaveUpdateObject(Object object) throws ServiceException;
    
    public void deleteTemporaryMappedFiles(String savedFilesMappingId,String companyid) throws ServiceException;
    
    public int savePurchaseOrderStatusForSO(JSONObject params) throws ServiceException;
    
    public KwlReturnObject getLinkedPO(JSONObject params) throws ServiceException;
    
    public KwlReturnObject getLinkedDocByModuleId(JSONObject paramJobj) throws ServiceException;
    
    public KwlReturnObject checklinkingofTransactions(String accid,String companyid) throws ServiceException;
    
    public KwlReturnObject checkEntryForReceiptInLinking(String moduleName, String docid) throws ServiceException;
    
    public KwlReturnObject getLineLevelDiscountSumOfPartialInvoice(String linkdocid, String companyid) throws ServiceException;
    
    public double getSOStatusOnBalanceQty(String soid, String companyId) throws ServiceException;
}
