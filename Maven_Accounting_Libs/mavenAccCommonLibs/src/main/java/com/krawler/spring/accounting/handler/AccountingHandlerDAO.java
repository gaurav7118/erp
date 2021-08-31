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
package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.CustomerAddressDetails;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.admin.VendorAddressDetails;
import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.*;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface AccountingHandlerDAO {

    public KwlReturnObject getObject(String classpath, String id) throws ServiceException;
    
    public boolean checkForProductCategoryForProduct(JSONArray productDiscountMapList, int appliedUpon, String rule);
    
    public KwlReturnObject loadObject(String classpath, String id) throws ServiceException;
    
     public KwlReturnObject getInvoiceFromFirstDB(String [] subdomain,String dbName) throws ServiceException;
    
    public KwlReturnObject getVendorInvoiceFromFirstDB(String [] subdomain,String dbName) throws ServiceException;
    
    public KwlReturnObject getCustomerCreditNoteFromFirstDB(String [] subdomain,String dbName) throws ServiceException;
    
    public KwlReturnObject getCustomerDebitNoteFromFirstDB(String [] subdomain,String dbName) throws ServiceException;
    
    public KwlReturnObject getFieldParamsFromFirstDB(HashMap<String, Object> requestParams,String dbName);
    
    public KwlReturnObject getReceiptFromFirstDB(String [] subdomain,String dbName) throws ServiceException ;
    
    public KwlReturnObject getOpeningInvoiceFromFirstDB(String[] subdomain,String dbName) throws ServiceException;

    public KwlReturnObject getVendorOpeningInvoiceFromFirstDB(String[] subdomain,String dbName) throws ServiceException;

    public KwlReturnObject getCustomerOpeningCreditNoteFromFirstDB(String[] subdomain,String dbName) throws ServiceException;

    public KwlReturnObject getCustomerOpeningDebitNoteFromFirstDB(String[] subdomain,String dbName) throws ServiceException;

    public KwlReturnObject getOpeningReceiptFromFirstDB(String[] subdomain,String dbName) throws ServiceException;

    public KwlReturnObject getOpeningPaymentFromFirstDB(String[] subdomain,String dbName) throws ServiceException;
    
    public KwlReturnObject getPaymentFromFirstDB(String [] subdomain,String dbName) throws ServiceException;
    
    public String getCustomDataUsingColNumInv(HashMap<String, Object> requestParams) ;
    public String getCustomDataUsingColNumRec(HashMap<String, Object> requestParams) ;
    public String getCustomDataUsingColNumVal(String id);
    public String getCustomDataUsingColNum(HashMap<String, Object> requestParams,String dbName) ;
    
    
    public KwlReturnObject getObject(String classpath, Integer id) throws ServiceException;
    
    public KwlReturnObject  saveOrUpdateObject(Object object) throws ServiceException;
    
    public void evictObj(Object currentSessionObject) ;
    
    //Synchronizes hibernate session with database, i.e. saves any unsaved objects/operations in database
    public void flushHibernateSession();
    
    public Boolean checkSecurityGateFunctionalityisusedornot(String companyid) throws ServiceException;
    
    public Boolean checkIsVendorAsCustomer(String companyid,String customerId) throws ServiceException;

    public ArrayList getApprovalFlagForAmount(Double invoiceamount, String typeid, String fieldtype, String companyid) throws ServiceException;

    public ArrayList getApprovalFlagForProductsDiscount(Double discamount, String productid, String typeid, String fieldtype, String companyid, boolean approvalFlag, int approvallevel) throws ServiceException;

    public ArrayList getApprovalFlagForProducts(ArrayList productlist, String typeid, String fieldtype, String companyid) throws ServiceException;
//    public Object invokeMethod(String modulename, String method, Object[] params) throws ServiceException;

    public KwlReturnObject updateApprovalHistory(HashMap hm) throws ServiceException;

    public String getApprovalHistory(String billid, String companyid, DateFormat df,HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getApprovalHistoryForExport(String billid, String companyid) throws ServiceException;
    
    public KwlReturnObject getCustomizeReportViewMappingField(HashMap hashMap) throws ServiceException;
    
    public KwlReturnObject deleteCustomizeReportColumn(String id,String companyId, int reportId) throws ServiceException;

    /**
     * @param mailParameters (String Number, String fromName, String[] emails, String fromEmailId, String moduleName, String companyid, String PAGE_URL)
     */
    public void sendApprovalEmails(Map<String, Object> mailParameters);

    public void sendApprovedEmails(Map<String, Object> mailParameters);
    
    public void sendReorderLevelEmails(String Sender,String[] emails,String moduleName, HashMap<String,String> data)throws ServiceException;
    
    public void sendTransactionEmails(String[] toEmailIds,String ccmailids,String subject,String htmlMsg,String plainMsg,String companyid) throws ServiceException ;
    
    public String getTabularFormatHTMLForNotificationMail(List<String> headerItemsList, List rowDetailMapList);
    
    public Boolean checkForMultiLevelApprovalRule(int level, String companyid, String amount, String userid, int moduleid) throws AccountingException, ServiceException, ScriptException;
    
    public Boolean checkForMultiLevelApprovalRules(HashMap<String, Object> requestParams) throws AccountingException, ServiceException, ScriptException;

    public String[] getApprovalUserList(HttpServletRequest request, String moduleName, int approvalLevel);
    
    public String[] getApprovalUserListJson(JSONObject jobj, String moduleName, int approvalLevel);
    
    public KwlReturnObject saveModuleTemplate(HashMap hm) throws ServiceException;

    public KwlReturnObject getModuleTemplates(HashMap hm);
    
    public void setDefaultModuleTemplates(HashMap hm) throws ServiceException;

    public KwlReturnObject getModuleTemplateForTemplatename(HashMap hm);
    
    public KwlReturnObject deleteModuleTemplates(String companyId, String templateId) throws ServiceException;

    public KwlReturnObject getDuedateCustomerInvoiceInfoList() throws ServiceException;

    public KwlReturnObject getDuedateVendorInvoiceList(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getDuedateVendorBillingInvoiceList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getDuedateCustomerInvoiceList(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSOdateSalesOrderList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getSOdateBillingSalesOrderList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getPOdatePurchaseOrderList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getPOdateBillingPurchaseOrderList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getDOdateDeliveryOrderList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getProductExpdateDeliveryOrderList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getProductExpdateGoodsReceiptOrderList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getGRODateGoodsReceiptOrderList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getSRDateSalesReturnList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getPRDatePurchaseReturnList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getCustomerCreationDateCustomerList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getVendorCreationDateVendorList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getInvoiceCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getVendorInvoiceCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCustomerQuotationCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getVendorQuotationCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSalesOrderDateCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPurchaseOrderCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getDeliveryOrderCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getGoodsReceiptOrderCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getJournalEntryCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSalesReturnCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPurchaseReturnCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCustomerCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getVendorCustomFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCustomerInvoiceLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getVendorInvoiceLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCustomerQuotationDetailsLineCustomDateFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getVendorQuotationDetailsLineCustomDateFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSalesOrderDetailsLineCustomDateFields(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPurchaseOrderDetailLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getDeliveryOrderDetailLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getGoodsReceiptOrderDetailLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSalesReturnDetailLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPurchaseReturnDetailLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getDuedateCustomerBillingInvoiceList(String companyId, String date) throws ServiceException;

    public KwlReturnObject getCompanyList() throws ServiceException;
    
    public KwlReturnObject getCompanyList(int offset,int limit) throws ServiceException;
    
    public List getPOSCompanyList() throws ServiceException;

    public KwlReturnObject getFieldParamsForProject(String companyid, String projectid) throws ServiceException;
    
    public KwlReturnObject getFieldParamsForLinkProjectToInvoice(HashMap<String, Object> requestParams);
    
    public KwlReturnObject getReceiptDetails(String companyId,String receiptId) throws ServiceException ;
    
    public KwlReturnObject getFieldParamsForTask(String companyid) throws ServiceException;

    public KwlReturnObject getFieldComboDataForProject(String fieldId, String projectid) throws ServiceException;
    
    public KwlReturnObject getFieldComboDataForTask(String fieldId, String projectid, String taskid) throws ServiceException;

    public KwlReturnObject getNotifications(String companyId) throws ServiceException;

    public KwlReturnObject getDueJournalEntryList(String companyId, Long dbDuedate1, Long dbDuedate2, String columnname) throws ServiceException;

    public KwlReturnObject getDuedateCustomefield(String companyId) throws ServiceException;

    public KwlReturnObject getUserDetailObj(String[] userids) throws ServiceException;

    public KwlReturnObject saveAddressDetail(Map<String, Object> addressParams, String companyid) throws ServiceException;

    public KwlReturnObject saveVendorAddressesDetails(HashMap<String, Object> addressParams, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteVendorAddressDetails(String vendorID, String companyid)throws ServiceException;
    
    public KwlReturnObject getVendorAddressDetails(HashMap<String, Object> requestParams)throws ServiceException;

    public KwlReturnObject saveCustomerAddressesDetails(HashMap<String, Object> addressesMap, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteCustomerAddressDetails(String customerID, String companyid)throws ServiceException;
    
    public KwlReturnObject deleteCustomerAddressByID(String id, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteVendorAddressByID(String id, String companyid) throws ServiceException;
    
    public KwlReturnObject getCustomerAddressDetails(HashMap<String, Object> requestParams)throws ServiceException;
    
    public KwlReturnObject getCustomerAddressDetailsMap(HashMap<String, Object> addrRequestParams) throws ServiceException;
    
    public void updateCustomerAddressDefaultValueToFalse(String customerid,boolean isBillingAddress)throws ServiceException;

    public void sendSaveTransactionEmails(String documentNumber, String moduleName, String[] tomailids, String userName, boolean isEditMail, String companyid)throws ServiceException;
    
    public void sendSaveTransactionEmails(String documentNumber, String moduleName, String[] tomailids,String ccmailids, String userName, boolean isEditMail, String companyid)throws ServiceException;
    
    public int getNature(String grpname, String companyid) throws ServiceException;
    
    public void updateAdvanceDetailAmountDueOnAmountReceived(String advancedetailid, double amountreceived) throws ServiceException;
    
    public List getPaymentAdvanceDetailsInRefundCase(String advancedetailid) throws ServiceException;
    
    public KwlReturnObject getProductExpiryList(String companyId) throws ServiceException;    
    
    public List getDescriptionConfig(HashMap<String, Object> requestParams) throws ServiceException;
    
    public String getCustomerAddress(HashMap<String, Object> addrRequestParams);
    
    public String getVendorAddress(HashMap<String, Object> addrRequestParams);
    
    public VendorAddressDetails getVendorAddressObj(HashMap<String, Object> addrRequestParams);
    
    public String getVendorAddressForSenwanTec(HashMap<String, Object> addrRequestParams);
    
    public String getTotalVendorAddress(HashMap<String, Object> addrRequestParams);
    
    public  CustomerAddressDetails getCustomerAddressobj(HashMap<String, Object> addrRequestParams);
    
    public String getCustomerAddressForSenwanTec(HashMap<String, Object> addrRequestParams);
    
    public String getTotalCustomerAddress(HashMap<String, Object> addrRequestParams);
    
    public KwlReturnObject getCompanyAddressDetails(HashMap<String, Object> requestParams)throws ServiceException;

    public KwlReturnObject getEmailTemplateTosendApprovalMail(String companyid, String fieldid, int moduleid)throws ServiceException;

    public void insertDocumentMailMapping(String documentid, int moduleid, String ruleid, String companyid) throws ServiceException;

    public int getDocumentMailCount(String documentid, String companyid, String ruleid) throws ServiceException;
    
    public KwlReturnObject getApprovalHistory(HashMap<String, Object> approvalHisMap) throws ServiceException;
    
    public KwlReturnObject getProductMasterFieldsToShowAtLineLevel(Map<String, Object> ProductFieldsRequestParams) throws ServiceException;
    
    public Boolean checkInventoryModuleFunctionalityIsUsedOrNot(String companyid) throws ServiceException;

    public Boolean checkSerialNoFunctionalityisusedornot(String companyid, String checkColumn) throws ServiceException;

    public KwlReturnObject getMasterItemByUserID(Map<String,Object> salesPersonParams) throws ServiceException;
    
    public Map<String, String> getMasterItemByCompanyID(Map<String,Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getExciseTemplatesMap(HashMap hm);
    
    public KwlReturnObject getExciseDetails(String receiptId) throws ServiceException;
    
    public KwlReturnObject getSMTPAuthenticationDetails(HashMap<String, Object> requestParams)throws ServiceException;
    
    public KwlReturnObject getAdvancePayDetails(String paymentid)throws ServiceException;
    
    public KwlReturnObject getinvoiceDocuments(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getAddressDetailsFromCompanyId(Map<String,Object> reqParams) throws ServiceException;
    
    public KwlReturnObject populateMasterInformation(Map<String, String> requestParams) throws ServiceException ;
    
    public KwlReturnObject getAccountidforDummyAccount(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getEntityDetails(Map<String, Object> paramsMap) throws ServiceException;
    /**
     * Desc:This function is used to get NewProductBatch object from warehouse,location,row,rack,bin,batch name.
     * @param requestParams
     * @return : NewProductBatch Object
     * @throws ServiceException 
     */
    public NewProductBatch getERPProductBatch(Map<String, Object> requestParams) throws ServiceException;
    
    /**
     * Desc:This method is used to get warehouse from warehouse name.
     * @param requestParams
     * @return String
     * @throws ServiceException 
     */
    public String getStoreByTypes(Map<String, Object> requestParams) throws ServiceException;
    
    /**
     * Format double value to remove trailing zeros. 
     * Example: 0.2500 -> 0.25, 1.0026500 -> 1.0265, 1.0 -> 1, 0.1260000 -> 0.126
     * 
     * @param double 
     * @return String
     */
    public String formatDouble(double d);
    
    public JSONObject getUserPermissionForFeature(HashMap<String, Object> params) throws ServiceException;
}
