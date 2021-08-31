/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

import com.krawler.common.admin.GridConfig;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author krawler
 */
public interface AccCustomReportService {

    /**
     * Get the accounts from Account table
     *
     * @param companyId
     * @return KwlReturnObject
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONObject getModuleCategories(Map<String,Object> requestParams) throws ServiceException;

    /**
     * Get the module id and module name for the selected module category
     *
     * @param moduleID
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONObject getModules(String moduleCatID, String moduleCatName) throws ServiceException;

    /**
     * Get the fields for the selected module
     *
     * @param moduleID
     * @param companyID
     * @param userID
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONObject getFields(Map<String,Object> requestParams) throws ServiceException;
    
    /**
     * Save the created report
     *
     * @param selectedRowsJSONData
     * @param companyID
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONObject saveOrUpdateCustomReport(Map valueMap,JSONArray filterArray) throws ServiceException;
        
    /**
     * Get the list of reports created by the user
     *
     * @param moduleCatId
     * @param companyID
     * @param userId
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONObject getCustomReportList(JSONObject paramJobj,DateFormat df) throws ServiceException,SessionExpiredException;

    /**
     * Execute the selected report
     *
     * @param requestParam
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONObject executeCustomReport(Map<String, Object> requestParam) throws ServiceException,ParseException,IOException,SessionExpiredException;

    /**
     * delete the created report
     *
     * @param valueMap
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public boolean deleteCustomReport(HashMap<String, Object> valueMap) throws ServiceException;

    /**
     * creates custom report preview data
     *
     * @param selectedRowsJSONData
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    
    public JSONObject executeCustomReportPreview(JSONArray selectedRowsJSONData, Map valueMap) throws ServiceException,JSONException,IOException, SessionExpiredException, ParseException;
    
    /**
     * update the name and description for the selected report
     *
     * @param valueMap
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONObject updateCustomReportNameAndDescription(HashMap<String, Object> valueMap) throws ServiceException;
    
    public JSONObject getPreferences(HashMap<String, Object> request, String userTimeFormat) throws ServiceException;

    public boolean isCustomReportNameExists(HashMap<String, Object> requestParams) throws ServiceException;
    
    public boolean isCustomReportExists(HashMap<String, Object> requestParams) throws ServiceException;
    
    public String getDateColumn(String mainTable) throws ServiceException;
    
    public Map showRowLevelFieldsJSONArray(JSONArray selectedRowsJSONData, boolean showRowLevelFieldsflag) throws ServiceException;
    
    public double getTaxPercent(String companyid, Date transactiondate, String taxid) throws ServiceException;
    
    public KwlReturnObject getProductPrice(String productid, boolean isPurchase, Date transactiondate, String affecteduser, String forCurrency) throws ServiceException;
    
    public KwlReturnObject getTax(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONArray getCustomReportMeasureFieldJsonArray(JSONArray ColumnConfigArr, String moduleid,JSONObject paramJobj) throws ServiceException;
    
    public String getApprovalStatus(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONArray mapDataArrToSelectedRows(JSONArray selectedRowsJSON , JSONArray dataJArr,Map valueMap) throws ServiceException;
    
    public JSONObject filterAccountsAndInvoiceColumns(JSONArray selectedRowsJSON) throws ServiceException;
    
    public Object getTimzeZoneClassObject(String classpath, String id) throws ServiceException;
    
    public JSONObject saveCustomWidgetReports(JSONObject paramJobj) throws ServiceException;
    
    public JSONObject getCustomWidgetReports(JSONObject paramJobj) throws JSONException, ServiceException;
    
    public boolean deleteCustomWidgetReport(JSONObject paramJobj) throws ServiceException;

        /**
     * processes the data fetched from service for selected report from Report-List
     *
     * @param reportJArr
     * @param valueMap
     * @return JSONArray
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONArray processReportDataFromReportListService(JSONArray reportJArr, Map<String, Object> valueMap) throws ServiceException;
    
    /**
     * processes the columns data for selected report from Report-List
     *
     * @param columnsJArr
     * @param valueMap
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONObject processCustomizedReportColumnsData(JSONArray columnsJArr, Map<String, Object> valueMap) throws ServiceException;
    
    /**
     * processes the columns data for saved customized report from Report-List
     *
     * @param columnsJArr
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONArray processCustomizedColumnsDataForReport(JSONArray columnsJArr) throws ServiceException;
    
    /**
     * Fetches the URL for service of selected report from Report-List
     *
     * @param requestParams
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONObject getCustomizedReportURLandParams(HashMap<String, Object> requestParams) throws ServiceException;
    
    /**
     * Fetches the URL for service of selected report from Report-List
     *
     * @param requestParams
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONObject fetchCustomReportDetails(Map<String, Object> requestParams) throws ServiceException;
    
    /**
     * Fetches and processed the data for customized report preview
     *
     * @param reportJArr
     * @param columnsJArr
     * @param valueMap
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    
    public JSONObject executeCustomizedReportPreview(JSONArray reportJArr,JSONArray columnsJArr, HashMap<String, Object> valueMap) throws ServiceException;
    /**
     * Fetches and processed the data for customized report
     *
     * @param reportJArr
     * @param valueMap
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONObject executeCustomizedReport(JSONArray reportJArr, HashMap<String, Object> valueMap) throws ServiceException;
    
    public JSONObject saveOrUpdateChartDetails(JSONObject paramObj) throws ServiceException;
    
    public JSONObject getChartDetails(JSONObject paramObj) throws ServiceException;
    
    public JSONObject deleteChartDetails(JSONObject paramObj) throws ServiceException;
    
    public JSONObject saveGridConfig(JSONObject paramObj) throws ServiceException;
    
    public JSONObject getGridConfig(JSONObject paramObj) throws ServiceException;
    
    public Map validateEWayBillReport(JSONObject dataArray, HashMap requestParams) throws JSONException, ServiceException, ParseException;

//    public Map separateValidInvalidEWayRecords(JSONObject resultJSON) throws JSONException;

    public JSONObject getJSONtoExport(Map separatedRecords) throws JSONException, ServiceException ;

    public JSONObject revertEWayStatus(Map requestParam)throws ServiceException;
    
    public Map checkIfReportHasAllColumns(List columnList, JSONArray columns) throws JSONException;
    
    public List getMandatoryReportColumn(String moduleid);
    
    public JSONObject isEntityFilterApplied(JSONArray searchJSONArray, String moduleid)throws JSONException;
    
    public boolean setUpCustomReportsDefaultsForNewCompany(Map<String, Object> requestParam) throws ServiceException, JSONException;
    
    public boolean getJSONExportButtonStatus(JSONArray validRecords, Set invalidDocuments)throws JSONException;
    
     public boolean copyCustomReport(Map valueMap) throws ServiceException,JSONException,IOException, SessionExpiredException, ParseException;
}
