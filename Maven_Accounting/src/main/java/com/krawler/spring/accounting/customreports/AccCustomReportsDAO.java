/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.GridConfig;
import com.krawler.common.admin.ReportMaster;
import com.krawler.common.admin.SavedSearchQuery;
import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 *
 * @author krawler
 */
public interface AccCustomReportsDAO {

    /**
     * Get the accounts from Account table
     *
     * @param companyId
     * @return KwlReturnObject
     * @throws com.krawler.common.service.ServiceException
     */
    public KwlReturnObject getModuleCategories(String companyId, boolean isPivot) throws ServiceException;

    /**
     * Get the module id and module name for the selected module category
     *
     * @param moduleCatID
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public KwlReturnObject getModules(String moduleCatID) throws ServiceException;
        
    /**
     * Get the default fields for the selected module
     *
     * @param map
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public KwlReturnObject getDefaultFields(Map<String, Object> map) throws ServiceException;

    /**
     * Get the custom fields for the selected module
     *
     * @param map
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public KwlReturnObject getCustomFieldsData(Map<String, Object> map) throws ServiceException;

    /**
     * Save the created report
     *
     * @param accCustomReport
     * @param valueMap
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public KwlReturnObject saveOrUpdateCustomReport(ReportMaster accCustomReport, Map valueMap) throws ServiceException;
    
    /**
     * Fetch the primary key from the information schema for the given table
     * Schema and table name
     *
     * @param tableSchema
     * @param tablename
     * @return Primary Key Column Name
     */
    public String getmoduledataRefPKColName(String tablename);

    /**
     * Get the list of reports created by the user
     *
     * @param moduleID
     * @param companyID
     * @param userId
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public KwlReturnObject getCustomReportList(JSONObject paramJobj) throws ServiceException;

    /**
     * Fetch the details of particular report
     *
     * @param reportID
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public ReportMaster fetchCustomReportDetails(String reportID) throws ServiceException;

    /**
     * Execute the selected report
     *
     * @param paramList
     * @param sql
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public SqlRowSet executeCustomReportSQL(String sql, ArrayList paramList) throws ServiceException;

    public int executeCustomReportCountSQL(String sql, ArrayList paramList) throws ServiceException;

    public KwlReturnObject getPreferences(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getObject(String classpath, String id) throws ServiceException;
    
    public KwlReturnObject getCustomReportByNameAndCompanyId(HashMap<String, Object> queryParams) throws ServiceException;
    
    public KwlReturnObject getCustomReportByReportNoAndCompanyId(HashMap<String, Object> queryParams) throws ServiceException;
    
    /**
     * delete the created report
     *
     * @param valueMap
     * @return boolean
     * @throws com.krawler.common.service.ServiceException
     */
    public boolean deleteCustomReport(Map valueMap) throws ServiceException;
    
    /**
     * update the name and description for the selected report
     *
     * @param accCustomReport
     * @param valueMap
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public KwlReturnObject updateCustomReportNameAndDescription(ReportMaster accCustomReport, Map valueMap) throws ServiceException;
    
    public Map getAdvanceSearchQuery(Map valueMap) throws ServiceException;
    
    public String getDefaultFieldDBColumnName(String fieldid,String moduleId) throws ServiceException;
    
    public KwlReturnObject getMeasureFields(String moduleID) throws ServiceException;
    
    public KwlReturnObject getMeasureFieldMappings(String measureFieldID) throws ServiceException;
    
    public KwlReturnObject getCustomReportsCustomFieldsMapping(String moduleID,boolean isLineItem,String mappingfor) throws ServiceException;
    
    public KwlReturnObject getCurrencyToBaseAmount(Map requestParams, Double amount, String currencyid, Date transactiondate, double rate) throws ServiceException;
    
    public KwlReturnObject getPaymentVendorNames(String companyid,String paymentid) throws ServiceException;
    
    public KwlReturnObject getReceiptCustomerNames(String companyid, String receiptid) throws ServiceException;
    
    public KwlReturnObject getRepaymentSheduleDetails(Map mapForRepaymentDetails) throws ServiceException;
    
    public KwlReturnObject getTaxPercent(String companyid, Date transactiondate, String taxid) throws ServiceException;
    
    public KwlReturnObject getProductPrice(String productid, boolean isPurchase, Date transactiondate, String affecteduser, String forCurrency) throws ServiceException;
    
    public KwlReturnObject getTax(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDefaultFieldMappings(String measureFieldID) throws ServiceException;
    
    public boolean isTermMappedwithTax (HashMap<String, Object> requestParams)  throws ServiceException;
    
    public KwlReturnObject getSalesOrderTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getPurchaseOrderTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getDOTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getGRTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public String getFKofCrossJoinMainTable(String crossJoinMainTable, String crossJoinDetailTable )throws ServiceException;
    
    public KwlReturnObject getMultiApprovalRuleData(HashMap<String, Object> dataMap) throws ServiceException;
 
    public KwlReturnObject getApprovalRuleTargetUsers(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getRoleofUser(String userid) throws ServiceException;
   
    public KwlReturnObject getInvoiceTermMap(HashMap<String, Object> hm) throws ServiceException;
    
    public KwlReturnObject getReceiptTermMap(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject getMeasureFields(Map<String, Object> map) throws ServiceException; 
    
    public KwlReturnObject getCustomReportsDefaultFieldsNeededMapping(String moduleID,boolean isLineItem) throws ServiceException;
    
    public KwlReturnObject getJEForDebitNote(String dnID) throws ServiceException;
    
    public KwlReturnObject getJEForCreditNote(String dnID) throws ServiceException;
    
    public Object getTimzeZoneClassObject(String classpath, String id) throws ServiceException;
    
    public String getSOStatus(SalesOrder so, CompanyAccountPreferences pref,ExtraCompanyPreferences extraCompanyPreferences) throws ServiceException;
    
    public KwlReturnObject getLinkedModules(String module) throws ServiceException;
    
    public KwlReturnObject getLinkedModuleJoinDetails(String module,String linkedModule) throws ServiceException;
    
     /**
     * Get the module id and module name for the Report-List Module Category
     *
     * @param moduleCatID
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public KwlReturnObject getReportsFromReportList(String moduleCatID) throws ServiceException;
    
    /**
     * Get the DefaultHeaders' ID, dummyvalue, xtype and defaultHeader 
     * for selected report from Report-List
     *
     * @param reportId
     * @return KwlReturnObject
     * @throws com.krawler.common.service.ServiceException
     */
    public KwlReturnObject getDefaultHeaderDetailsForReportFromReportList(String reportId) throws ServiceException;
    
    /**
     * Get Report Name from Report-List by ReportID
     *
     * @param reportId
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    public String getReportNameFromReportListByReportId(String reportId) throws ServiceException;
    
    
     /**
     * Get Report Service URL from Report-List by ReportID
     *
     * @param reportId
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    public String getReportURLFromReportList(String reportId) throws ServiceException;
    
    public KwlReturnObject saveCustomWidgetReports(JSONObject dataObj) throws ServiceException;
    
    public KwlReturnObject getCustomWidgetReports(JSONObject dataObj) throws ServiceException;
    
    public boolean deleteCustomWidgetReport(JSONObject paramJobj) throws ServiceException;
    
    public KwlReturnObject getCustomerInvoiceCreationAndEntryDates(String invoiceId) throws ServiceException;
    
    public KwlReturnObject getSalesReturnCreationAndEntryDates(String srId) throws ServiceException;
    
    public KwlReturnObject saveOrUpdateChartDetails(JSONObject paramObj) throws ServiceException;
    
    public KwlReturnObject getTaxList(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getChartDetails(JSONObject paramObj) throws ServiceException;
    
    public KwlReturnObject deleteChartDetails(JSONObject paramObj) throws ServiceException;
    
    public KwlReturnObject getInvoiceForPayment(JSONObject paramObj) throws ServiceException;
    
    public KwlReturnObject getCreditNoteLinkingForPayment(JSONObject paramObj) throws ServiceException;
    
    public KwlReturnObject getInvoiceForReceipt(JSONObject paramObj) throws ServiceException ;
    
    public KwlReturnObject getLinkedInvoiceFromPayment(JSONObject paramObj) throws ServiceException;
    
    public KwlReturnObject getDebitNoteForReceipt(JSONObject paramObj) throws ServiceException;
    
    public KwlReturnObject getLinkedDebitNoteFromReceipt(JSONObject paramObj) throws ServiceException;
    
    public KwlReturnObject getLinkedCreditNoteFromPayment(JSONObject paramObj) throws ServiceException;
    
    public KwlReturnObject getLinkedInvoiceFromReceipt(JSONObject paramObj) throws ServiceException;
    /**
     * save grid state config of custom report for current user
     * @param gridConfig
     * @return saved GridConfig object
     * @throws ServiceException 
     */
    public KwlReturnObject saveGridConfig(GridConfig gridConfig) throws ServiceException;
    /**
     * get saved grid state config of custom report for current user
     * @param paramObj
     * @return GridConfig object
     * @throws ServiceException 
     */
    public KwlReturnObject getGridConfig(HashMap<String, Object> params) throws ServiceException;
    
    public KwlReturnObject getCustomReportsGSTFieldsLineLevelTermsMapping(String gstFieldID) throws ServiceException;    

    public List getDefaultStateValue(JSONObject paramObj) throws ServiceException;
    
    public int updateEwayJSONExportFlag(List updateDocumentList, Map requestParam) throws ServiceException ;
    
    public Map getEntityDetails(Map requestParams)throws ServiceException;
    
    public SqlRowSet getDefaultStates() throws ServiceException;
    
    public KwlReturnObject getCustomReportsDefaults(HashMap<String, String> requestMapParams) throws ServiceException;
    
    public KwlReturnObject getModuleCategoryForModule(String moduleID) throws ServiceException;
    
    public SavedSearchQuery getSaved_Search_Query(String reportId) throws ServiceException; 
}
