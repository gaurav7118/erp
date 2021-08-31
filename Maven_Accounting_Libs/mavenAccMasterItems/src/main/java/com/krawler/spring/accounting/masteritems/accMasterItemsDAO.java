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
package com.krawler.spring.accounting.masteritems;

import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Packages;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface accMasterItemsDAO {

    public KwlReturnObject addMasterItem(HashMap<String, Object> mitemmap) throws ServiceException;

    public KwlReturnObject addMasterPriceDependentItem(HashMap<String, Object> mitemmap) throws ServiceException;

    public KwlReturnObject addMasterItemPrice(HashMap<String, Object> mitemmap) throws ServiceException;
    
    public KwlReturnObject saveActivateDeactivateDimensionFields(HashMap<String, Object> mitemmap) throws ServiceException;

    public KwlReturnObject addLocationItem(HashMap<String, Object> mitemmap) throws ServiceException;

    public KwlReturnObject saveDepartmentItem(HashMap<String, Object> mitemmap) throws ServiceException;

    public KwlReturnObject addWarehouseItem(HashMap<String, Object> mitemmap) throws ServiceException;

    public KwlReturnObject getMasterPriceDependentItem(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getMasterItemPrice(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getMasterItemPriceFormula(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveMasterItemPriceFormula(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject addUpdateMasterCustomItem(HashMap<String, Object> mitemmap, boolean isEdit) throws ServiceException;

    public KwlReturnObject updateMasterItem(HashMap<String, Object> itemmap) throws ServiceException;

    public KwlReturnObject updateMasterPriceDependentItem(HashMap<String, Object> itemmap) throws ServiceException;

    public String daleteMasterItem(String itemid) throws ServiceException,AccountingException;
    
    public KwlReturnObject getSalesPersonMappedWithCustomer(String itemid) throws ServiceException;
    
    public KwlReturnObject getIndustryCodesMappedWithProduct(String itemid) throws ServiceException;
    
    public KwlReturnObject getIndustryCodesMappedWithMultiEntity(String itemId,String companyId) throws ServiceException;
    
    public KwlReturnObject getProductMappedWithProductCategory(String itemId) throws ServiceException,AccountingException;
    
    public KwlReturnObject getGstRegistrationTypeMappedWithCustomersAndVendors(String itemId) throws ServiceException,AccountingException;
    
    public KwlReturnObject getGstCustomerVendorTypeMappedWithCustomersAndVendors(String itemId) throws ServiceException,AccountingException;
    
    public KwlReturnObject getMasterItemsForProductCategory(String itemid, String companyId, String groupId) throws ServiceException ;
    
    public KwlReturnObject deleteMasterItemPrice(String itemid) throws ServiceException;

    public KwlReturnObject deleteMasterItemPriceFormula(String itemid) throws ServiceException;

    public KwlReturnObject deleteLocationItem(String itemid) throws ServiceException;

    public KwlReturnObject deleteWarehouseItem(String itemid) throws ServiceException;
        //    public KwlReturnObject getMasterItems(String groupid, String companyid) throws ServiceException;

    public KwlReturnObject getMasterItems(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getMasterItem(String masteritemid) throws ServiceException;
    
    public KwlReturnObject getLocationItems(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getDepartments(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getWarehouseItems(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getLocationsFromStore(String storeid,String companyid);
    
    public KwlReturnObject CheckRuleForPendingApproval(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBatches(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getNewBatches(HashMap<String, Object> requestParams,boolean isOnlyBatchflag, boolean isOnlyStockflag) throws ServiceException;
    public KwlReturnObject getLocationBatchDocumentMp(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getNewBatchesForVendor(HashMap<String, Object> requestParams,boolean isOnlyBatchflag, boolean isOnlyStockflag) throws ServiceException;
    public KwlReturnObject getBatchesForDocuments(HashMap<String, Object> requestParams) throws ServiceException;
    public double getBatcheQuantityForreturn(String documentId,String batchMapId) throws ServiceException;
    public KwlReturnObject getPRBatchQuantity(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getBatchRemainingQtyFromIST(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSerials(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getNewSerials(HashMap<String, Object> requestParams,String companyId,String productid,String batchId,boolean requestRejectedCheck) throws ServiceException;
    
    public KwlReturnObject getNewSerials(HashMap<String, Object> requestParams,String companyId,String productid,String batchId,boolean requestRejectedCheck,boolean fromVend) throws ServiceException;
    
    public KwlReturnObject getNewSerialsForConsignmentReturn(String doId,String batchId) throws ServiceException;
    public KwlReturnObject getNewBatchForConsignmentReturn(String doId) throws ServiceException;
    public KwlReturnObject getDeliveryOrderDetails(String doId,String productId) throws ServiceException;
    public KwlReturnObject getSerialPurchaseDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getSerialsForDocuments(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getSerialsForDocumentsSql(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getNewSerialsForDocuments(String documentId,String batchId,int transType) throws ServiceException;
    public KwlReturnObject getNewSerialDocumentMapping(String doId, String batchId, int transType) throws ServiceException;

    public KwlReturnObject getSerialsinEdit(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject checkDuplicateSerialforProduct(String productid,String batchid,String serialName,String company) throws ServiceException;

    public KwlReturnObject getSerialItems(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getMasterItemsHire(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getMasterItemsForCustom(HashMap<String, Object> requestParams) throws ServiceException;
    
    public List getUniqueDimensionValues(Map<String, Object> paramMap) throws ServiceException;
    
    public List getMasterItemsForRemoteAPI(ArrayList value, boolean callForTasks) throws ServiceException;

    public KwlReturnObject getMasterItemsForCustomHire(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getUserGroupmappingId(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getParentMappingToChild(HashMap<String, Object> Params) throws ServiceException;
    
    public boolean SaveProjectTaskMapping(HashMap<String, String> itemmap) throws ServiceException;

    public KwlReturnObject getMasterItemsParentDimensionValue(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject addUpdateMasterCustomItemMapping(HashMap<String, Object> mitemmap) throws ServiceException;

    public KwlReturnObject addMasterGroup(HashMap<String, Object> groupmap) throws ServiceException;

    public KwlReturnObject updateMasterGroup(HashMap<String, Object> groupmap) throws ServiceException;

    public KwlReturnObject getMasterGroups() throws ServiceException;

    public KwlReturnObject deleteMasterGroup(String groupid) throws ServiceException;

    public KwlReturnObject deleteMasterCustomItemMapping(String childid) throws ServiceException;

    public KwlReturnObject daleteMasterCustomItem(String groupid) throws ServiceException;

    public boolean isUsedMasterCustomItem(String groupid, String grpId) throws ServiceException;
    
    public boolean isUsedMasterCustomItemForEntity(JSONObject paramObj) throws ServiceException,JSONException;

    public boolean isUsedMasterCustomField(String grpId) throws ServiceException;

    public KwlReturnObject getFieldParamsUsingSql(HashMap<String, Object> requestParams);

    public HashMap<String, String> getParentIds(String comIds[], String parentId);

    public KwlReturnObject getValueIfFieldInOtherModule(List list, String groupBy);

    public void copyMasterItems(String companyid, HashMap hmAcc) throws ServiceException;
    
    public void mapMasterItemWithAccount(String companyid, HashMap hmAcc,String masterGroup) throws ServiceException;
    
    public void copyMasterItemsCountrySpecifics(HashMap<String, Object> requestParams) throws ServiceException;
    
    public void copyDefaultTDSRates(HashMap<String, Object> defaultCompSetupMap)throws ServiceException;

    public KwlReturnObject getFieldParams(HashMap<String, Object> requestParams);

    public KwlReturnObject deleteDimension(String groupid) throws ServiceException;
    
    public KwlReturnObject deleteAddressFieldAgainstDimension(String groupid) throws ServiceException;

    public KwlReturnObject deleteNotificationRuleOnDimensionDelete(String groupid) throws ServiceException;

    public KwlReturnObject getallModuleNamesUsingSql(String toString);

    public String getParentFieldId(String companyid, String parentid, int moduleid);

    public void insertNewValues(HashSet fieldIds, String values, String oldValue);

    public KwlReturnObject saveSalesComissionScehma(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteSalesComissionScehma(String itemid) throws ServiceException;

    public KwlReturnObject getsalesComissionScehma(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteDepartmentItem(String itemid) throws ServiceException;
    
    public KwlReturnObject getPackages(HashMap<String, Object> filterParams) throws ServiceException;
  
    public Packages buildPackage(Packages packages, HashMap<String, Object> packageMap);
    
    public KwlReturnObject updatePackages(HashMap<String, Object> packageMap) throws ServiceException;
    
    public KwlReturnObject addPackages(HashMap<String, Object> packageMap) throws ServiceException;
    
    public KwlReturnObject deletePackage(String packageid, String companyid) throws ServiceException;
    
    public KwlReturnObject addPricingBandItem(HashMap<String, Object> mitemmap) throws ServiceException;
    
    public KwlReturnObject getPricingBandItems(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getProductsForPricingBandMasterDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveOrUpdatePricingBandMasterDetails(HashMap<String, Object> deliveryPlannerParams) throws ServiceException;
    
    public KwlReturnObject getPricingBandFromCustomer(String pricingBandID, String companyID) throws ServiceException;
    
    public KwlReturnObject getPricingBandFromVendor(String pricingBandID, String companyID) throws ServiceException;
    
    public KwlReturnObject deletePricingBandDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deletePricingBand(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPriceOfProductForPricingBandAndCurrency(HashMap<String, Object> requestParams) throws ServiceException;
    
    public List getExistingVolumes(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPriceOfBandForProductAndCurrency(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getPOSProductsPrice(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getLocationLevelMapping(String companyId) throws ServiceException;
    
    public KwlReturnObject getLLevelMappingFrmLevlId(String companyId,int levelid) throws ServiceException;
    
    public KwlReturnObject getLocationLevel() throws ServiceException;
        
    public KwlReturnObject getPriceListVolumeDiscountItems(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deletePricingListBandMapping(String customerid) throws ServiceException;

    public KwlReturnObject savePricingListBandMapping(Map<String, Object> volParams) throws ServiceException;

    public KwlReturnObject getPricingVolumeDiscountMapped(String pricingVolumelistID) throws ServiceException;

    public String getPricingbandMappedwithvolumeDisc(String pricingVolumelistID) throws ServiceException;

    public KwlReturnObject addPriceListVolumeDiscountItem(HashMap<String, Object> mitemmap) throws ServiceException;
    
    public KwlReturnObject deletePriceListVolumeDiscount(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateMasterSetting(HashMap<String,Object> requestParams) throws ServiceException;
    
    public KwlReturnObject buildQueryForgetStoreMasters(Map<String, Object> requestParams) throws ServiceException ;
    
    public KwlReturnObject getStoreMasters(HashMap<String, Object> requestParams) throws ServiceException;
    
     public KwlReturnObject deleteStoreMasterItem(String itemid) throws ServiceException;
     
     public KwlReturnObject addUpdateStoreMasterItem(HashMap<String, Object> mitemmap) throws ServiceException;

    public KwlReturnObject getCompanPreferencesSql(String companyID) throws ServiceException;
    
    public KwlReturnObject deletePriceListVolumeDiscountDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
     public KwlReturnObject getMasterItemFromCustomerID(String customerid) throws ServiceException;
     
     public KwlReturnObject getMasterItemFromVendorID(String vendorid) throws ServiceException;
     
     public KwlReturnObject isStoreMasterSettingUsed(String companyID) throws ServiceException;

     public KwlReturnObject getdefault_warehouse(String warehouseid, String companyid) throws ServiceException;

    public KwlReturnObject getProductsusedinWarehouses(String warehouseid, String companyid) throws ServiceException;

    public KwlReturnObject getBatches_warehouses(String warehouseid, String companyid) throws ServiceException;
    
    public KwlReturnObject getInvoice_warehouses(String warehouseid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSO_warehouses(String warehouseid, String companyid) throws ServiceException;
    
    public KwlReturnObject getDO_warehouses(String warehouseid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSR_warehouses(String warehouseid, String companyid) throws ServiceException;
    
    public KwlReturnObject getcustomer_warehouses(String warehouseid, String companyid) throws ServiceException;
    
     public KwlReturnObject getdefault_location(String locationid, String companyid) throws ServiceException;

    public KwlReturnObject getProductsusedinlocations(String locationid, String companyid) throws ServiceException;

    public KwlReturnObject getBatches_locations(String locationid, String companyid) throws ServiceException;
    
    public KwlReturnObject getWarehouses_locations(String locationid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSO_locations(String locationid, String companyid) throws ServiceException;
    
    public KwlReturnObject getConsignmentRequest_locations(String locationid, String companyid) throws ServiceException;
    
    public KwlReturnObject getIBGReceivingBankDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteIBGReceivingBankDetails(HashMap<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject deleteCIMBReceivingBankDetails(HashMap<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getDeliveryPlanner(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSalesOrdersBySalesPerson(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getWCByWCMasterItem(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getQualityByWCMasterItem(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCustomerQuotationsBySalesPerson(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getSalesInvoicesBySalesPerson(HashMap<String, Object> requestParams) throws ServiceException;
       
    public KwlReturnObject getDeliveryOrdersBySalesPerson(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCreditNotesBySalesPerson(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject checkSalesPersonUsedInAnyTransaction(HashMap<String, Object> requestMap) throws ServiceException,AccountingException;
    
    public KwlReturnObject getCIMBReceivingBankDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject activateDeactivateSalesperson(HashMap<String, Object> requestParams)throws ServiceException;
    
    public KwlReturnObject checkAgentUsedInAnyTransaction(HashMap<String,Object> requestParams)throws ServiceException,AccountingException;
    
    public KwlReturnObject checkDOGRStatusUsedInAnyTransaction(Map<String,Object> requestParams)throws ServiceException,AccountingException;
    
    public KwlReturnObject getPaymentsWithCimb(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getFieldComboDataByFieldName(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getMasterItemByNameorID(String companyid, String value, String masterGroupID,String fetchColumn,String conditionColumn)throws ServiceException;
     
    public KwlReturnObject getTermForCompany(String companyid) throws ServiceException;
    
    public KwlReturnObject getPricingBandMasterDetailsList(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteProductBrandDiscountDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject checkProductBrandUsedInAnyTransaction(HashMap<String, Object> requestParams) throws ServiceException, AccountingException;
    
    public KwlReturnObject getPaymentMethodIdFromName(String methodName, String companyId) throws ServiceException;
    
    public KwlReturnObject getCustomerList(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDeliveryOrderList(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject checkVehicleNumberUsedInAnyTransaction(HashMap<String, Object> requestMap) throws ServiceException, AccountingException;
    
    public KwlReturnObject getStateIdByName(String name) throws ServiceException;
    
    public KwlReturnObject getUserIdByName(String name,String company) throws ServiceException;
    /**
     * Description:This method is used to check if Package is used in any Transaction or not
     * @param filterParams
     * @return KwlReturnObject
     * @throws ServiceException
     */
    public KwlReturnObject isPackageUsedInTransaction(HashMap<String, Object> filterParams) throws ServiceException;
    
    /**
     * Description:This method is used to get used Batch Details in Assembly Product.
     * @param HashMap<String, Object> requestParams
     * @return List
     * @throws ServiceException
     */
    public List getUsedBatchAssemblyProduct(HashMap<String, Object> requestParams) throws ServiceException;
    
    /**
     * Description:This method is used to get used Serial Number Details in Assembly Product.
     * @param HashMap<String, Object> requestParams
     * @return List
     * @throws ServiceException
     */
    public List getUsedSerialNoAssemblyProduct(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject checkreceivedFromUsedInAnyTransaction(HashMap<String, Object> requestParams) throws ServiceException, AccountingException;
    
    public KwlReturnObject checkpaidToUsedInAnyTransaction(HashMap<String, Object> requestParams) throws ServiceException, AccountingException;
    
    public KwlReturnObject checkBankNameUsedInAnyTransaction(HashMap<String, Object> requestParams) throws ServiceException, AccountingException;
    
    /**
     * To Check Reason Used in Any Transaction (CN/DN)
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     * @throws AccountingException
     */
    public KwlReturnObject checkReasonUsedInAnyTransaction(HashMap<String, Object> requestParams) throws ServiceException, AccountingException;

    public KwlReturnObject checkDefaultMasterItemsNOPlist(HashMap<String, Object> requestParams) throws ServiceException, AccountingException;
    
    public KwlReturnObject getLBDMforConsumedUnfreeWODetail(HashMap<String, Object> requestParams) throws ServiceException, AccountingException;
    
    public void copyMasterItemsNopList(HashMap<String, Object> requestParams) throws ServiceException;

    public List getSerialsForConsignmentEdit(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getMasterItemFromLandingCostCategory(String id,String company) throws ServiceException;
    
    public KwlReturnObject getMasterItemFromLandingCostCategory(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getLandingCostCategoryInTranscation(String id,String company) throws ServiceException;
    
    public KwlReturnObject getLandingCostCategoryInProduct(String id,String company) throws ServiceException;
    
    public KwlReturnObject checkLandingCostCategoryRec(HashMap<String, Object> mitemmap) throws ServiceException;
    
    public KwlReturnObject addLandingCostOfCategory(HashMap<String, Object> mitemmap) throws ServiceException;
    
    public KwlReturnObject daleteLandingCostCategoryItem(String id,String companyid) throws ServiceException;
    
    
    public KwlReturnObject checkCustomerBankAccountTypeUsedInAnyTransaction(HashMap<String,Object> map) throws ServiceException, AccountingException;

    public KwlReturnObject getSalesCommissionSchema(String salesCommissionSchemaId, String companyId) throws ServiceException;
    
    public Map<String, Object> getNextAutoNumber_Modified(String companyid, int from, String format, boolean oldflag, Date creationDate) throws ServiceException, AccountingException;

    public NewProductBatch getERPProductBatch(String productId, String storeId, String locationId, String rowId, String rackId, String binId, String batchName) throws ServiceException;

    public NewBatchSerial getERPBatchSerial(String productId, NewProductBatch productBatch, String serialName, String companyid) throws ServiceException;
    
    public List checksEntryNumberForSequenceNumber(int mduleid, String entryNumber, String custom)throws ServiceException;
    
    public KwlReturnObject getSequenceFormat(Map<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject addUsersGroup(Map<String, Object> itemmap) throws ServiceException;
    
    public KwlReturnObject addUsersGroupMapping(Map<String, Object> itemmap) throws ServiceException;
    
    public KwlReturnObject addUsersGroupFCDMapping(Map<String, Object> itemmap) throws ServiceException;
    
    public KwlReturnObject getUsersGroup(Map<String, Object> itemmap) throws ServiceException;
    
    public KwlReturnObject deleteUsersGroupMapping(Map<String, Object> itemmap) throws ServiceException;
    
    public KwlReturnObject deleteUsersGroupFieldComboMapping(Map<String, Object> itemmap) throws ServiceException;
    
    public KwlReturnObject deleteUsersGroupFieldComboMappingUsingFCD(Map<String, Object> itemmap) throws ServiceException;
    
    public KwlReturnObject deleteUsersGroup(Map<String, Object> itemmap) throws ServiceException;
    
    public KwlReturnObject getFieldIdForMaster(Map<String,Object>map) throws ServiceException;
    
    public KwlReturnObject getBatchDocuments(HashMap<String, Object> dataMap) throws ServiceException;
    
    /**
     * Description:This method is used to Add OR Update Location with Warehouse imported from Import Assembly Functionality
     * @param HashMap<String, Object> requestParams
     * @return KwlReturnObject
     * @throws ServiceException
     */
    public KwlReturnObject addUpdateLocation(HashMap<String, Object> map) throws ServiceException;
    
    public KwlReturnObject getDiscountOfProductForPricingBand(JSONObject requestParamsJson) throws ServiceException;
    
    public KwlReturnObject setProductDiscountMapping(JSONObject paramObj) throws ServiceException;
    
    public KwlReturnObject deleteProductDiscountMapping(JSONObject paramObj) throws ServiceException;
    
    public KwlReturnObject deleteProductDiscountMapping(String productId, String companyId) throws ServiceException;
    
    public KwlReturnObject getAddressMappingForDimension(com.krawler.utils.json.base.JSONObject reqParams) throws JSONException, ServiceException;
    
    public KwlReturnObject getFieldParamsForDimension(com.krawler.utils.json.base.JSONObject reqParams) throws JSONException, ServiceException;
         
    public KwlReturnObject getserialsforPickPackShipDO(String dodetailid,String productid) throws ServiceException;

    public boolean isSameDefaultValueForTransaction(String grpId) throws ServiceException;

    public KwlReturnObject unlinkcustomFieldFromTransaction(String groupid, JSONArray array) throws ServiceException;

    public KwlReturnObject getCustomTableName(com.krawler.utils.json.base.JSONObject reqParams) throws JSONException, ServiceException;

    public JSONArray createJsonForCustomTableList(List<Object> l) throws JSONException;
    
    List<NewBatchSerial> getExcludeSerials(JSONObject jobj) throws JSONException,ServiceException;
    
    public KwlReturnObject getChallanForJobWorkAssembly(JSONObject requestParams) throws ServiceException;
    
    public double getBOMRemainingQuantityForJobWorkAssembly(String productid, String companyid, String warehouseid, String locationid, String batch, String bomid) throws ServiceException;
    
    public KwlReturnObject checkVendorCategoryUsedInAnyTransaction(String vendorCategoryId) throws ServiceException, AccountingException;
    
    public KwlReturnObject checkCustomerCategoryUsedInAnyTransaction(String customerCategoryId) throws ServiceException, AccountingException;
    
    public String getDocumentsIdForInvoice(HashMap<String, Object> map) throws ServiceException;
    
    public KwlReturnObject checkTaxTypeUsedInAnyTransaction(HashMap<String, Object> requestParams) throws ServiceException, AccountingException;
    
}
