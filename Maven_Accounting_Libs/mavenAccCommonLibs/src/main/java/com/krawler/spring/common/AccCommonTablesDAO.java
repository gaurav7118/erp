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
package com.krawler.spring.common;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CroneSchedule;
import com.krawler.common.admin.GridConfig;
import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
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
public interface AccCommonTablesDAO {

    public KwlReturnObject getCompanyTypes() throws ServiceException;

    public KwlReturnObject getFieldParams(HashMap<String, Object> requestParams);
    
    public KwlReturnObject getFieldParamsforSpecificFields(HashMap<String, Object> requestParams);

    public KwlReturnObject getCustomCombodata(HashMap<String, Object> requestParams);

    public KwlReturnObject setPDFTemplate(HashMap<String, Object> pdfTemplateMap) throws ServiceException;

    public KwlReturnObject getPDFTemplateRow(String companyid, int module) throws ServiceException;

    public KwlReturnObject deleteSalesPurchaseSerialMapping(String doserialid) throws ServiceException;

    public KwlReturnObject deleteReturnbatchSerialMapping(String doserialid) throws ServiceException;

    public KwlReturnObject getPDFTemplateConfig(String companyid) throws ServiceException;

    public KwlReturnObject updatePDFTemplate(HashMap<String, Object> pdfTemplateMap) throws ServiceException;

    public KwlReturnObject saveBatchForProduct(HashMap<String, Object> pdfTemplateMap) throws ServiceException;
    public void saveBatchAmountDue(HashMap<String, Object> pdfTemplateMap) throws ServiceException;
    public void saveSerialAmountDue(HashMap<String, Object> pdfTemplateMap) throws ServiceException;
    public KwlReturnObject saveNewBatchForProduct(HashMap<String, Object> pdfTemplateMap) throws ServiceException;
    
    public KwlReturnObject updateNewBatchProductIfPresent(HashMap<String, Object> pdfTemplateMap) throws ServiceException;
    
    public KwlReturnObject saveBatchDocumentMapping(HashMap<String, Object> pdfTemplateMap) throws AccountingException,ServiceException;
    public KwlReturnObject saveSerialDocumentMapping(HashMap<String, Object> pdfTemplateMap) throws ServiceException;
    public KwlReturnObject updateserialcustomdata(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject saveSalesPurchaseSerialMapping(HashMap<String, Object> pdfTemplateMap) throws ServiceException;

    public KwlReturnObject saveReturnSerialMapping(HashMap<String, Object> pdfTemplateMap) throws ServiceException;

    public KwlReturnObject saveBatchMapping(HashMap<String, Object> pdfTemplateMap) throws ServiceException;

    public KwlReturnObject saveReturnBatchMapping(HashMap<String, Object> pdfTemplateMap) throws ServiceException;

    public KwlReturnObject saveSerialForBatch(HashMap<String, Object> pdfTemplateMap) throws ServiceException;
    public KwlReturnObject saveNewSerialForBatch(HashMap<String, Object> pdfTemplateMap) throws ServiceException;

    public KwlReturnObject getNewSerialForBatch(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getSerialForBatch(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getBatchSerialDetails(String documentid,boolean addlocationmappcheck,boolean linkingFlag,String moduleID,boolean isConsignment,boolean isEdit,String documentIDs) throws ServiceException;
    public KwlReturnObject getConsignmentBatchSerialDetails(String documentid, boolean addlocationmappcheck, boolean linkingFlag, String moduleID, boolean isConsignment, boolean isEdit,String documentIDs) throws ServiceException;
    public KwlReturnObject getBatchDetails(String documentid,boolean linkingFlag,String moduleID,boolean isConsignment,boolean isEdit,String documentIDs) throws ServiceException;
    
    public KwlReturnObject getBatchSerialDetailsforProduct(String productId,boolean isSerialForProduct,boolean isEdit,JSONObject paramJobj) throws ServiceException,SessionExpiredException;
    
    public KwlReturnObject getBatchSerialDetailsforProduct(Company company,String productId,boolean isSerialForProduct,String requestTypeId,boolean isEdit,String requestWarehouse,String requestLocation) throws ServiceException;
    
    public KwlReturnObject getPendingConsignmentRequests(Company company) throws ServiceException;
    
    public KwlReturnObject getBatchSerialDetailsforProductAccordingToRequestType(String companyid,String productId,String requestTypeId) throws ServiceException,SessionExpiredException;
    
    public KwlReturnObject updateSOLockQuantity(String sodid,double dquantity,String companyid) throws ServiceException; 
    
    public KwlReturnObject updatePrintFlag(Integer moduleid, String billid, String companyid) throws ServiceException; 
    
    public KwlReturnObject updateSentEmailFlag(Integer moduleid, String billid, String companyid) throws ServiceException;
    
    public KwlReturnObject updateSOLockQuantitydue(String sodid,double dquantity,String companyid) throws ServiceException;    
    //public KwlReturnObject updateSOBalQuantity(String sodid,double  balQty,String companyid) throws ServiceException;    
    
    public KwlReturnObject getOnlySerialDetails(String documentid,boolean linkingFlag,String moduleID,boolean isConsignment,boolean isEdit) throws ServiceException;
    
    public KwlReturnObject getOnlyInventoryDetails(String documentid,String moduleID,boolean isEdit) throws ServiceException;
    
    public Double getInvQuantity(String docid, String productid,boolean isVenQty) throws ServiceException; 
     
    public List getBatchSerialDetailsForRemoteAPI(String documentid, boolean addlocationmappcheck) throws ServiceException;
    
    public List getOnlySerialDetailsForRemoteAPI(String documentid) throws ServiceException;

    public Double getBatchQuantity(String documentid,String batchmapId) throws ServiceException;
    
    public Double getBatchQuantity(String documentid,String batchmapId,int type) throws ServiceException;
    
    public Double getApprovedSerialQty(String documentid,String batchmapId,boolean isEdit) throws ServiceException;
    
    public Double getserialAssignedQty(String documentid) throws ServiceException;
    
    public Double getbatchAssignedQty(String documentid) throws ServiceException;

    public Date getVendorExpDateForSerial(String serialnoid,boolean ispurchase) throws ServiceException;

    public String getDefaultLocation(String companyid) throws ServiceException;

    public String getDefaultWarehouse(String companyid) throws ServiceException;
    
    public KwlReturnObject getBatchMappingDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSerialMappingDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBatch(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveAssembySerialBatchMapping(HashMap<String, Object> batchMap) throws ServiceException;

    public double getBatchRemainingQuantity(String purchasebatchid, int moduleid, String companyId) throws ServiceException;

    public KwlReturnObject deleteBatches(String batchid, String companyid) throws ServiceException;

    public KwlReturnObject deleteSerial(String batchid, String companyid) throws ServiceException;
    //    public KwlReturnObject saveAssembyProductBatchMapping(HashMap<String, Object> batchMap)throws ServiceException;
    
    public KwlReturnObject getGridConfig(HashMap<String, Object> requestParams) throws ServiceException;
    
    public GridConfig saveGridConfig(JSONObject jobj) throws ServiceException;

    public String getPurchaseBatchId(String id) throws ServiceException;

    public String getPurchaseSerialId(String id) throws ServiceException;

    public boolean isserialusedinDOandPR(String id) throws ServiceException;

    public boolean isserialusedinDOandSR(String id) throws ServiceException;
    
    public String isbatchExsistOrNot(String batchName,String custWarehouse,String productId,String companyid) throws ServiceException;
    
    public String isSerialExsistOrNot(String serialName,String custWarehouse,String productId,String companyid) throws ServiceException;
    
    public String getpurchaseBatchIdForSR(String batchid, String batchName) throws ServiceException;

    public String getpurchaseBatchIdForSRByRowId(String docId, String batchName,String batchmapid) throws ServiceException;
    
    public String getpurchaseBatchIdForDo(String batchid,String documentid) throws ServiceException;
    
    public String getpurchaseBatchIdForNonbatch(String id, String batchName, String pId) throws ServiceException;

    public String getpurchaseBatchIdForNonbatchByRowId(String docID, String batchName) throws ServiceException;
    
    @Deprecated // this method is now deprecated as row rack bin is now included in newproductbatch
    public String getpurchaseBatchIdForLocationWarehouse(String productid,String location,String warehouse) throws ServiceException;
    
    public String getpurchaseBatchIdForLocationWarehouseRowRackBin(String productid,String location,String warehouse,String rowId,String rackId,String binId,String batchName) throws ServiceException;
    
    public KwlReturnObject getStockAdjustmentSerialData(String companyId,NewProductBatch newProdBatch,String stockAdjId,boolean isFetchOnlyAvailableSerials) throws ServiceException;
    
    public String getpurchaseSerialIdForSR(String serialid,String serialName) throws ServiceException;
    
    public String getStoreIdForNonbatchSerialByDODetailId(String companyId,String DODetailId) throws ServiceException;
    
    public String getLocationIdForNonbatchSerialByDODetailId(String companyId,String DODetailId) throws ServiceException;
    
    public String getBatchIdForNonbatchSerialByDODetailId(String companyId,String DODetailId) throws ServiceException;
    
    public KwlReturnObject getBatchMapIdQtyByDocumentId(String companyId,String documentId) throws ServiceException;
    
    public boolean isCroneExecutedForCurrentDay(String croneID, Date executionDate) throws ServiceException;

    public CroneSchedule saveCroneDetails(String croneID, String croneName, Date executionDate) throws ServiceException;
    
    public KwlReturnObject getCompany(HashMap<String, Object> requestParams) throws ServiceException;
    
    public int executeSQLUpdate(String query, Object[] params) throws ServiceException;
    
    public List executeSQLQuery(String query, Object[] params) throws ServiceException;
    
    public KwlReturnObject getOnlySerialDetailsForConsignmentLoan(String documentid) throws ServiceException;
    
    public KwlReturnObject getOnlySerialDetailsForConsignmentLoanReport(String documentid) throws ServiceException;

    public KwlReturnObject getBatchSerialDetailsForConsignmentLoan(String documentid, boolean addlocationmappcheck) throws ServiceException;
 
    public KwlReturnObject getBatchSerialDetailsForLoanReport(String documentid, boolean addlocationmappcheck,String company) throws ServiceException;
    
    public KwlReturnObject getBatchSerialDetailsForReturnReport(String documentid, boolean addlocationmappcheck,String company) throws ServiceException;
 
    public KwlReturnObject getSerialsReusableCount(String productId,String serialName,String companyId, int transType,boolean viewOnly,String documentid,String batchID) throws ServiceException;
    
    public KwlReturnObject updatePurchaseOrderStatus(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateExpensePurchaseOrderStatus(Map<Object, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateSecurityGateStatus(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateSalesorderOrderStatus(HashMap<String, Object> requestParams) throws ServiceException;
        
    public KwlReturnObject setRolePermissions(HashMap<String, Object> requestParams, String[] features, String[] permissions) throws ServiceException;
     
     public KwlReturnObject getRolePermission(HashMap<String, Object> requestParams) throws ServiceException;
     
     public KwlReturnObject getAllUserPermission(HashMap<String, Object> requestParams) throws ServiceException;
     
     public KwlReturnObject isUsernameExitornot(HashMap<String, Object> requestParams) throws ServiceException;
     
     public KwlReturnObject deleteRole(HashMap<String, Object> requestParams) throws ServiceException;
       
     public KwlReturnObject getRoleList(String companyid,String ss) throws ServiceException;
     
     public KwlReturnObject getUserList(String companyid,String roleid) throws ServiceException;
     
     public KwlReturnObject getTransactionInTemp(String documentno, String companyId,int moduleId) throws ServiceException;
     
     public int insertTransactionInTemp(String documentno, String companyId,int moduleId) throws ServiceException;
    
     public int deleteTransactionInTemp(String documentno, String companyId,int moduleId) throws ServiceException;
     
     public KwlReturnObject saveWastageDetailsForBatch(Map<String, Object> requestParams) throws ServiceException;
     
     public KwlReturnObject saveWastageDetailsForSerial(Map<String, Object> requestParams) throws ServiceException;
     
     public List getActiveUsersOfCompany(String companyID) throws ServiceException;
     
    public KwlReturnObject getTDSRate(HashMap<String, Object> TDSParams) throws ServiceException;

    public List getTDSMasterRates(HashMap<String, Object> TDSParams) throws ServiceException;
    
    public List getMasterItemsForNatureOfPayment(HashMap<String, Object> TDSParams) throws ServiceException;
    
    public KwlReturnObject AddTDSRate(Map<String, Object> TDSParams) throws ServiceException;
    
    public List CheckDuplicateTDSMasterRate(HashMap<String, Object> TDSParams) throws ServiceException;
    
    public String getCustomCurrencySymbol( String currencysymbol, String companyid) throws ServiceException;
    
    public String getCustomCurrencyCode( String currencyid, String companyid) throws ServiceException;
    
    public void releaseSerialFromOtherSo(NewBatchSerial newbatchseria, String documentId) throws ServiceException;

    public void updateBatchExpDate(HashMap<String, Object> BatchParams) throws ServiceException;
    
    public void UpdateDocuments(String productBatchId, String attachmentids) throws ServiceException;
    
    public KwlReturnObject getDistributedOpeningBalance(HashMap<String, Object> requestParams);
    
    public JSONObject totalTDSAssessableAmountForExemptLimit(HashMap<String, Object> requestParams);
    
    public String getTransactionId(String companyid, String cnnumber,String moduleid) throws ServiceException ;
    
    public boolean isUnitPriceHiddenForPR(String companyId) throws ServiceException;
    
    public KwlReturnObject saveOrUpdateInspectionForm(JSONObject params) throws ServiceException;
    
    public KwlReturnObject deleteInspectionFormDetails(String InspectionFormId) throws ServiceException;

    public KwlReturnObject saveInspectionFormDetails(HashMap<String, Object> inspectionFormDetailsMap) throws ServiceException;
}
