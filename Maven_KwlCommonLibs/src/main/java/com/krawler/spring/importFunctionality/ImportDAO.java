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
package com.krawler.spring.importFunctionality;

import com.krawler.common.admin.ImportLog;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;

public interface ImportDAO {

    public List getModuleObject(String moduleName) throws ServiceException;

    public List getModuleColumnConfig(Map<String, Object> importParams) throws ServiceException;
    
    public List getModuleNonEditableFields(String moduleId) throws ServiceException;
    
    public List getCustomModuleColumnConfig(String moduleId, String companyid,boolean isExport) throws ServiceException;

    public List getRefModuleData(HashMap<String, Object> requestParams, String module, String fetchColumn, String comboConfigid, ArrayList<String> filterNames, ArrayList<Object> filterValues) throws ServiceException, DataInvalidateException;
    
    public List getRefModuleDataWithPrimaryKeyValue(HashMap<String, Object> requestParams, String module, String fetchColumn, String comboConfigid, ArrayList<String> filterNames, ArrayList<Object> filterValues) throws ServiceException, DataInvalidateException;

    public boolean isAccountHavingTransactions(String accountId, String companyId) throws ServiceException, DataInvalidateException;

    public List getCustomComboID(String fetchColumn, ArrayList filterNames, ArrayList filterValues) throws ServiceException, DataInvalidateException;
    
    public List getGSTTermDetails(HashMap<String, Object> params) throws ServiceException ;
    
    public List getGSTRuleDetails(Map<String, Object> reqMap) throws ServiceException ;
    public List getStatesFromFieldCombodata(JSONObject jSONObject) throws ServiceException ;
    public KwlReturnObject isGSTRuleusedInTransaction(Map<String, Object> mapData) throws ServiceException;
    public int getColumnFromFieldParams(String fieldLabel, String companyId, int module,int customColumn) throws ServiceException;

    public Object saveRecord(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, Object csvReader, String modeName, String classPath, String primaryKey, Object extraObj, JSONArray customfield) throws ServiceException,DataInvalidateException;

    public Object saveImportLog(HashMap<String, Object> dataMap) throws ServiceException, DataInvalidateException;

    public void saveorupdateObject(ImportLog implg) throws ServiceException, DataInvalidateException;

    public void updateImportLog(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getImportLog(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getImportLogForDate(HashMap<String, Object> requestParams) throws ServiceException;

    public String getTableName(String fileName);

    public int createFileTable(String filename, int cols) throws ServiceException;

    public int removeFileTable(String tablename) throws ServiceException;

    public int removeAllFileTables() throws ServiceException;

    public int dumpFileRow(String filename, Object[] dataArray) throws ServiceException;

    public int makeUploadedFileEntry(String filename, String onlyfilename, String tablename, String companyid) throws ServiceException;

    public KwlReturnObject getFileData(String tablename, HashMap<String, Object> filterParams) throws ServiceException;

    public int markRecordValidation(String tablename, int id, int isvalid, String validateLog, String invalidColumns) throws ServiceException;

    public KwlReturnObject getMaxDisplayOrderOfGroup() throws ServiceException;
    
    public int getNatureByGroupID(String groupID, String companyID) throws ServiceException;
    
    public int updateManualJePostSettingForAccount(String accountID, String usedIn) throws ServiceException;

    public KwlReturnObject getSequenceFormat(Map<String, Object> map) throws ServiceException;

    public KwlReturnObject getMaxSequenceFormatNUmber(Map<String, Object> map)throws ServiceException;

    public KwlReturnObject getTaxbyIDorName(String companyId, String fetchColumnName, String conditionColumnName, String data) throws ServiceException;
    
    public boolean isAssetIDPresent(String assetID,String companyID);

    public List getProductLevelActiveItems(String companyId, String productCode) throws ServiceException;
    
    public boolean isProductUsedInTransaction(String companyId,String productId) throws ServiceException;
    
    public KwlReturnObject getOpeningQuantityAndInitialPrice(String companyId, String productId) throws ServiceException ;
    
    public boolean isPartNumberActivated(String companyId) throws ServiceException;
    
    public boolean isPerpetualInventory(String companyId) throws ServiceException;
    
    public boolean isMinMaxOrdering(String companyId) throws ServiceException;
    
    public boolean isStockUomMatchWithStockUomOfUomSchema(String uomSchema, String stockUOM, String companyId) throws ServiceException;

    public KwlReturnObject getProductByProductCode(String companyid, String productCode)throws ServiceException;

    public KwlReturnObject getMasterItem(String companyid, String categoryName, String masterGroupID)throws ServiceException;

    public KwlReturnObject getTaxByCode(String companyid, String taxcode,boolean isSales)throws ServiceException;
    
    public KwlReturnObject getTax(Map requestMap) throws ServiceException;
    
    public boolean isTaxActivated(String companyId, String taxCode) throws ServiceException;
    
    public KwlReturnObject getAccountByName(String companyid, String accountname, int groupNature)throws ServiceException;
    
    public void createFailureXlsFiles(String filename, List failureArr, String ext, List failureColumnArr);
    
    public KwlReturnObject getExcDetailID(Map<String, Object> map, String accountname, Date transactionDate);
    
    public KwlReturnObject getAccountFromName(String companyid, String payee)throws ServiceException;
    
    public KwlReturnObject getCustomerFromName(String companyid, String payee) throws ServiceException;
    
    public KwlReturnObject getVendorFromName(String companyid, String payee) throws ServiceException;
    
    public KwlReturnObject getVendorFromVendorCode(String companyid, String code) throws ServiceException;
    
    public KwlReturnObject getPaymentMethodDetail(String paymentMethodID,String companyid) throws ServiceException;
   
    public KwlReturnObject getBankNameMasterItemName(String companyid, String bankname) throws ServiceException;
    
    public KwlReturnObject getMasterItemDetails(String companyid, String value, String groupid) throws ServiceException;
    
    public String getMasterTypeID(String companyid, String value,String masterGroupId) throws ServiceException; //For INDIA Compliance get Deductee type ID from Master item
    
    public String getMasterTypeID(String companyid, String value,String masterGroupId,String code) throws ServiceException; //For INDIA Compliance get Deductee type ID from Master item
    
    public KwlReturnObject getProductTypeOfProduct(String product, String companyID) throws ServiceException;
    
    public KwlReturnObject getProductCategoryMappingCount(String productID, String productCategoryID) throws ServiceException;
    
    public KwlReturnObject getCustomerAddressDetailsForCustomerByAliasName(String customerID, String aliasName, boolean isBillingAddress, String companyID) throws ServiceException;
    
    public KwlReturnObject getCustomerCategoryMappingCount(String customerID, String customerCategoryID) throws ServiceException;
    
    public KwlReturnObject getVendorCategoryMappingCount(String customerID, String customerCategoryID) throws ServiceException;
    
    public List<String> getLineLevelTermPresentByName(Map<String, Object> dataMap) throws ServiceException; // While importing product check Input/ Output columns Data present or not in system
    
    public KwlReturnObject getGoodsReceiptOrderCount(String orderno, String companyid) throws ServiceException;
    
    public boolean isChequeNumberAvailable(Map hm) throws ServiceException;
    
    public KwlReturnObject getCompanyAccountPreferences(String companyId) throws ServiceException;
    
    public KwlReturnObject getDeliveryOrderCount(String orderno, String companyid) throws ServiceException;
    
    public KwlReturnObject getVATCommodityCodeByName(Map<String,String> mapData) throws ServiceException;
    
    public KwlReturnObject getFinancialYearDates(Map<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject getStoreMasters(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getProductIdIfLocationWarehouseBatchSerialIsActivated(String id) throws ServiceException;
    
    public boolean isExciseActivated(String companyId) throws ServiceException;
    
    public boolean checkActiveDateRange(Date orderDate, String companyId) throws ServiceException;
    
    public KwlReturnObject getImportFileColumnMapping(JSONObject paramJobj) throws ServiceException;
    
    public KwlReturnObject getImportFileDetails(JSONObject paramJobj) throws ServiceException;
    
    public void saveOrUpdateImportFileDetails(JSONObject paramJobj) throws ServiceException;
    
    public String getValuesForLinkedRecords(JSONObject paramJobj) throws JSONException,ServiceException;
    
    public String getEntityCustomData(JSONObject entityDetails) throws ServiceException,JSONException;    
    
    public KwlReturnObject isCustomDataPresent(JSONObject paramsJobj) throws ServiceException,JSONException;    
    
    public String getProductCurrencyName(String companyId,String productId) throws ServiceException;
    
    public boolean isValidDisplayUOM(String uomSchemaType, String displayUOM, String companyId) throws ServiceException;
    
    public KwlReturnObject getAccountDetailsFromAccountId(String accountid) throws ServiceException;
    
    public List getGstCustomerUsedHistory(Map<String, Object> reqMap) throws ServiceException;
    
    public List getGstVendorUsedHistory(Map<String, Object> reqMap) throws ServiceException;
    
    public KwlReturnObject getGoodsReceiptData(String GRNumber, String companyid, String vendorId) throws ServiceException;
    
    public KwlReturnObject getInvoiceData(String invoiceNumber, String companyid, String customerID) throws ServiceException;
    
    public KwlReturnObject getRefundNameCountForPayment(String refundNo, String companyid, String customerID) throws ServiceException;
    
    public KwlReturnObject getRefundNameCountForReceipt(String refundNo, String companyid, String vendorId) throws ServiceException;
    
    public KwlReturnObject getCustomerCreditNoCount(String creditNoteNo, String companyid, String customerId) throws ServiceException;

    public KwlReturnObject getVendorCreditNoCount(String creditNoteNo, String companyid, String vendorId) throws ServiceException;
    
    public KwlReturnObject getCustomerDebitNoCount(String invoiceNo, String companyid,String customerId) throws ServiceException;
    
    public KwlReturnObject getVendorDebitNoCount(String invoiceNo, String companyid,String vendorId) throws ServiceException;
    
    public KwlReturnObject getDocumentDetailsFromDocumentNo(String documentNo,String companyId,String mode,String doctype)throws ServiceException;
    public List getExtraCompanyPref(String companyId) throws ServiceException;
    
    public KwlReturnObject getStoreDetails(String storeId) throws ServiceException;
    
    public boolean isAccountMappedWithPaymentMethodOrTax(String accountId, String companyId) throws ServiceException, DataInvalidateException;
}
