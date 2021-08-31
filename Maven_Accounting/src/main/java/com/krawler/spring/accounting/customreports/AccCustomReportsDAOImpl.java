/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.loan.accLoanDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 *
 * @author krawler
 */
public class AccCustomReportsDAOImpl extends BaseDAO implements AccCustomReportsDAO {

    private SessionFactory sessionFactory;
    private accAccountDAO accAccountDaoObj;
    private JdbcTemplate jdbcTemplate;
    private authHandlerDAO authHandlerDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accReceiptDAO accReceiptDAOobj;
    private accLoanDAO accLoanDAOobj;
    private accProductDAO accProductObj;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;

    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }

    public void setAccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }

    public void setAccAccountDaoObj(accAccountDAO accAccountDaoObj) {
        this.accAccountDaoObj = accAccountDaoObj;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public void setAccTaxObj(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }
    
    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
    
    public void setAccLoanDAOobj(accLoanDAO accLoanDAOobj) {
        this.accLoanDAOobj = accLoanDAOobj;
    }
     
    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    } 
    
    public void setPermissionHandlerDAOObj(permissionHandlerDAO permissionHandlerDAOObj) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj;
    }
    
    public void setaccSalesOrderServiceDAO(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }
    /**
     * This method returns the accounts from Account table.
     *
     * @param companyId
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    @Override
    public KwlReturnObject getModuleCategories(String companyId, boolean isPivot) throws ServiceException {
        //String query = " select acc from Account acc where acc.company.companyID =  ? and acc.deleted = false ";
        //String query = "SELECT moduleCatId,moduleCatname FROM moduleCategory";
        String query = "";
        ArrayList params = new ArrayList();
        if(isPivot) {
            query = "from ModuleCategory order by moduleCatName";            
        } else {
            query = "from ModuleCategory mc where mc.moduleCatName != ? order by moduleCatName";                        
            params.add(CustomReportConstants.Reports_ModuleCategoryName);
        }
        //params.add(companyId);
        List list = executeQuery(query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }

    /**
     * This method returns the module id and module name for the selected module
     * category
     *
     * @param moduleCatID
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    @Override
    public KwlReturnObject getModules(String moduleCatID) throws ServiceException {
        String query = " select id,moduleName from Modules modules where modules.moduleCategory =  ? order by moduleName";
        ArrayList params = new ArrayList();
        params.add(moduleCatID);
        List list = executeQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    
    /**
     * Get the default fields for the selected module
     *
     * @param map
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public KwlReturnObject getDefaultFields(Map<String, Object> map) throws ServiceException {

        KwlReturnObject result = accAccountDaoObj.getDefaultHeaders((HashMap<String, Object>) map);
        return result;

    }

    /**
     * Get the custom fields for the selected module
     *
     * @param map
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public KwlReturnObject getCustomFieldsData(Map<String, Object> map) throws ServiceException {
        KwlReturnObject customFiledParamResult = accAccountDaoObj.getFieldParams((HashMap<String, Object>) map);
        return customFiledParamResult;

    }

    /**
     * Save or Update the created report
     *
     * @param accCustomReport
     * @param valueMap
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public KwlReturnObject saveOrUpdateCustomReport(ReportMaster accCustomReport, Map valueMap) throws ServiceException {
        //To change body of generated methods, choose Tools | Templates.
        KwlReturnObject result = null;
        String userid = (String) valueMap.get("userId");
        Boolean isEdit= (Boolean) valueMap.get("isEdit");
        if (isEdit) {
            accCustomReport.setUsersByUpdatedbyid((User) get(User.class, userid));
        } else {
            accCustomReport.setUsersByCreatedbyid((User) get(User.class, userid));
        }
        saveOrUpdate(accCustomReport);
        List resultList = new ArrayList<String>();
        resultList.add(accCustomReport.getID());
        result = new KwlReturnObject(true, "", "", resultList, resultList.size());
        return result;
    }
    
    /**
     * Fetch the primary key from the information schema for the given table
     * Schema and table name
     *
     * @param tableSchema
     * @param tablename
     * @return Primary Key Column Name
     */
    @Override
    public String getmoduledataRefPKColName(String tablename) {

        ArrayList params = new ArrayList();
        String fkColName = "";
        //String sqLQuery = "SELECT `COLUMN_NAME` FROM `information_schema`.`COLUMNS` WHERE (`TABLE_SCHEMA` = '" + tableSchema + "') AND  (`TABLE_NAME` = '" + tablename + "') AND (`COLUMN_KEY` = 'PRI');";
        String sqLQuery = "SELECT `COLUMN_NAME` FROM `information_schema`.`COLUMNS` WHERE (`TABLE_SCHEMA` = (SELECT  DATABASE()))  and  (`TABLE_NAME` = '" + tablename + "') AND (`COLUMN_KEY` = 'PRI');";
        List list = null;
        try {
            list = executeSQLQuery(sqLQuery, params.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(AccCustomReportsDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (list != null && list.size() > 0) {
            fkColName = (String) list.get(0);
        }
        return fkColName;
    }
    
    @Override
     public String getFKofCrossJoinMainTable(String crossJoinMainTable, String crossJoinDetailTable ) {

        ArrayList params = new ArrayList();
        params.add(crossJoinDetailTable);
        params.add(crossJoinMainTable);
        String fkColName = "";
        //String sqLQuery = "SELECT `COLUMN_NAME` FROM `information_schema`.`COLUMNS` WHERE (`TABLE_SCHEMA` = '" + tableSchema + "') AND  (`TABLE_NAME` = '" + tablename + "') AND (`COLUMN_KEY` = 'PRI');";
        String sqLQuery = "select "
                        + "COLUMN_NAME from "
                        + "INFORMATION_SCHEMA.KEY_COLUMN_USAGE "
                        + "where TABLE_NAME= ? "
                        + "and REFERENCED_TABLE_NAME = ? "
                        + "and REFERENCED_TABLE_SCHEMA=(SELECT DATABASE())";
        List list = null;
        try {
            list = executeSQLQuery(sqLQuery, params.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(AccCustomReportsDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (list != null && list.size() > 0) {
            fkColName = (String) list.get(0);
        }
        return fkColName;
    }

//   
    /**
     * Get the list of reports created by the user
     *
     * @param moduleCatId
     * @param companyID
     * @param userId
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public KwlReturnObject getCustomReportList(JSONObject paramJobj) throws ServiceException {
        KwlReturnObject result;
        String userId = paramJobj.optString(Constants.useridKey);
        String companyID = paramJobj.optString(Constants.companyKey);
        String start = paramJobj.optString("start");
        String limit = paramJobj.optString("limit");
        String filterQuery = paramJobj.optString("filterQuery");
        boolean ignorepivot = paramJobj.optBoolean("ignorepivot",false);
        ArrayList params = new ArrayList();
        List<ReportMaster> list = null;
        int totalCount = 0;
        User user = null;
        boolean isUserAdmin = false;
        String query = "";
        //String query = "from Customreports where reportmodulecategory =? and companyId = ? and usersByCreatedbyid.userID=? ";
        //String query = "from Customreports where companyId = ? and usersByCreatedbyid.userID=? order by reportname";
        //params.add(moduleCatId);
        if (!StringUtil.isNullOrEmpty(userId)) {
            KwlReturnObject userObject = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            if (userObject != null && !userObject.getEntityList().isEmpty() && userObject.getEntityList().get(0) != null) {
                user = (User) userObject.getEntityList().get(0);
                if(user!=null){
                    isUserAdmin = AccountingManager.isCompanyAdmin(user);
                }
            }

        }
        if(!isUserAdmin) {
         query = "select reportmaster.id,reportmaster.name,description,reportjson,reportsql,modulecategory.moduleCatname,reportmaster.moduleid,modules.modulename,reportmaster.createdon,reportmaster.updatedon,ispivotreport,isdefaultreport, reportmaster.widgetURL, reportmaster.parentreportid, reportmaster.createdbyid, reportmaster.filterjson,SAVED_SEARCH_QUERY.searchquery,SAVED_SEARCH_QUERY.filerappend,SAVED_SEARCH_QUERY.searchid, reportmaster.isewayreport,reportmaster.isshowasquicklinks  from reportmaster"
                + " left join modulecategory on reportmaster.reportmodulecategory=modulecategory.moduleCatId"
                + " left join modules on reportmaster.moduleid=modules.id"
                + " left join reportrolemap on  reportrolemap.reportid = reportmaster.id"
                + " left join users on  reportrolemap.userid = users.userid"
                + " left join SAVED_SEARCH_QUERY on  SAVED_SEARCH_QUERY.customreportid = reportmaster.id "
                + " left join (select * from widgetreportmaster where company=?) w on reportmaster.id=w.report"
                + " where  companyId = ? and (users.userid= ? or reportmaster.createdbyid=?)"
                + " and reportmaster.isdefaultreport=? ";
        params.add(companyID);
        params.add(companyID);
        params.add(userId);
        params.add(userId);
        params.add(false);
        } else {
            query = "select reportmaster.id,reportmaster.name,description,reportjson,reportsql,modulecategory.moduleCatname,reportmaster.moduleid,modules.modulename,reportmaster.createdon,reportmaster.updatedon,ispivotreport,isdefaultreport, reportmaster.widgetURL, reportmaster.parentreportid, reportmaster.createdbyid, reportmaster.filterjson,SAVED_SEARCH_QUERY.searchquery,SAVED_SEARCH_QUERY.filerappend,SAVED_SEARCH_QUERY.searchid, reportmaster.isewayreport,reportmaster.isshowasquicklinks  from reportmaster"
                + " left join modulecategory on reportmaster.reportmodulecategory=modulecategory.moduleCatId"
                + " left join modules on reportmaster.moduleid=modules.id"
                + " left join SAVED_SEARCH_QUERY on  SAVED_SEARCH_QUERY.customreportid = reportmaster.id "
                + " where  companyId = ? "
                + " and reportmaster.isdefaultreport=? ";
             params.add(companyID);
             params.add(false);
        }
        if (ignorepivot) {
            query+= "and reportmaster.ispivotreport = ?";
            params.add("F");
        }

        if(!StringUtil.isNullOrEmpty(filterQuery)){
            query += filterQuery;
        }
        

       
        String ss = paramJobj.optString(Constants.ss, null);
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"reportname"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                query += searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(AccCustomReportsDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        query+= " order by name";
        list = executeSQLQuery(query, params.toArray());
        totalCount = list.size();
        if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
            list = executeSQLQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
        }
        result = new KwlReturnObject(true, null, null, list, totalCount);
        return result;
    }

    /**
     * Fetch the details of particular report
     *
     * @param reportID
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public ReportMaster fetchCustomReportDetails(String reportID) throws ServiceException {
        ArrayList params = new ArrayList();
        ReportMaster reportDetails = null;
        params.add(reportID);
        String query = "from ReportMaster where id =?";
        List<ReportMaster> list = executeQuery(query, params.toArray());
        int count = list.size();
        if (count > 0) {
            reportDetails = list.get(count - 1);
        }
        return reportDetails;
    }

    /**
     * Execute the selected report
     *
     * @param companyID
     * @param sql
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public SqlRowSet executeCustomReportSQL(String sql, ArrayList paramList) throws ServiceException {
         SqlRowSet rs= null;
         try{
             ArrayList reportSQLparams = new ArrayList();
        //reportSQLparams.add(companyID);
        reportSQLparams = paramList;
         rs = jdbcTemplate.queryForRowSet(sql, reportSQLparams.toArray());
         }catch(Exception e){
             throw ServiceException.FAILURE("AccCustomReportsDAOImpl.executeCustomReportSQL : "+e.getMessage(), e);
         }
        return rs;
    }

    @Override
    public int executeCustomReportCountSQL(String sql, ArrayList paramList) throws ServiceException {
        int totalCount = 0;
        ArrayList reportSQLparams = new ArrayList();
        //reportSQLparams.add(companyID);
        reportSQLparams = paramList;
//        totalCount = jdbcTemplate.queryForInt(sql, reportSQLparams.toArray());
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, reportSQLparams.toArray());
        rs.last();
        totalCount = rs.getRow();
        return totalCount;
    }

    @Override
    public KwlReturnObject getPreferences(HashMap<String, Object> request) throws ServiceException {
        KwlReturnObject kmsg = authHandlerDAOObj.getPreferences(request);
        return kmsg;
    }

    @Override
    public KwlReturnObject getObject(String classpath, String id) throws ServiceException {
        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(classpath, id);
        return rdresult;
    }

    /**
     * Get the name of reports created by the user
     *
     * @param queryParams
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public KwlReturnObject getCustomReportByNameAndCompanyId(HashMap<String, Object> queryParams) throws ServiceException {
        ArrayList params = new ArrayList();
        List<ReportMaster> list = null;
        int resultCount = 0;
        //String query = "from Customreports where reportmodulecategory =? and companyId = ? and usersByCreatedbyid.userID=? ";
        String query = "from ReportMaster where name=? and companyId = ? and usersByCreatedbyid.userID=? ";
        params.add(queryParams.get("reportName"));
        params.add(queryParams.get("companyID"));
        params.add(queryParams.get("userId"));
        list = executeQuery(query, params.toArray());
        resultCount = list.size();
        return new KwlReturnObject(true, null, null, list, resultCount);
        
    }
    
    /**
     * To check the existence of a report by report number
     *
     * @param queryParams
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public KwlReturnObject getCustomReportByReportNoAndCompanyId(HashMap<String, Object> queryParams) throws ServiceException {
        ArrayList params = new ArrayList();
        List<ReportMaster> list = null;
        int resultCount = 0;
        //String query = "from Customreports where reportmodulecategory =? and companyId = ? and usersByCreatedbyid.userID=? ";
        String query = "from ReportMaster where ID=? and companyId = ?";
        params.add(queryParams.get("reportNo"));
        params.add(queryParams.get("companyID"));
//        params.add(queryParams.get("userId"));
        list = executeQuery(query, params.toArray());
        resultCount = list.size();
        return new KwlReturnObject(true, null, null, list, resultCount);
    }

    /**
     * deletes a reports created by the user
     *
     * @param valueMap
     * @return boolean
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public boolean deleteCustomReport(Map valueMap) throws ServiceException {

        boolean successDelete = false;
        ArrayList params = new ArrayList();
        //String query = "delete from Customreports where reportno in (?)  and companyId = ? and usersByCreatedbyid.userID=? ";
        String reportIDsToDelete = (String) valueMap.get("reportIds");
        String query = "delete from ReportMaster where id in (" + reportIDsToDelete + ")  and companyId = ?  ";
        String reportRoleMapquery = "delete from ReportRoleMap where reportid in (" + reportIDsToDelete + ")   ";        
        String customChartDeleteQuery = "delete from CustomReportCharts where reportid in (" + reportIDsToDelete + ") and companyid = ? ";
        //params.add((String)valueMap.get("reportIds"));        
        int countReportRoleMap = executeUpdate(reportRoleMapquery, params.toArray());        
        params.add(valueMap.get("companyID"));
        int chartDelCount = executeUpdate(customChartDeleteQuery, params.toArray());
        int count = executeUpdate(query, params.toArray());
        if (count > 0) {
            successDelete = true;
        }
        return successDelete;
    }

    @Override
    public KwlReturnObject updateCustomReportNameAndDescription(ReportMaster accCustomReport, Map valueMap) throws ServiceException {

        KwlReturnObject result = null;
        String userid = (String) valueMap.get("userId");
        accCustomReport.setUsersByUpdatedbyid((User) get(User.class, userid));
        saveOrUpdate(accCustomReport);
        return result;
    }

    @Override
    public Map getAdvanceSearchQuery(Map valueMap) throws ServiceException {
        String appendCase = "and";
        Map advanceSearchMap=new HashMap();
        Map requestParams = new HashMap();
        ArrayList params = (ArrayList) valueMap.get("paramList");
        String Searchjson = "";
        String mySearchFilterString = "";
        Map<String, String> mappingCustomResult=null;
        String searchDefaultFieldSQL = "";
        String reportSQLQuery=(String) valueMap.get("reportSQLQuery");
        String reportCountSQLQuery=(String) valueMap.get("reportCountSQLQuery");
        String mainTable = valueMap.get("mainTable").toString();
        String reportSqlSelect = (String) valueMap.get(CustomReportConstants.SELECTQUERY);
        String reportSqlWhereCond = (String) valueMap.get(CustomReportConstants.CONDITIONQUERY);
        String reportJOINQuery = (String) valueMap.get(CustomReportConstants.JOINQUERY);
        String joinString1 = "";
        String moduleid = String.valueOf(valueMap.get("moduleid"));
        String selectCountQuery= (String) valueMap.get("selectCountQuery");
        /*
            In existing system we are using initeger value for customer module
            but in module table uuid is used against Customer.
            We need to pass integer to build query.
        */
        if(moduleid.equals(Constants.CUSTOMER_MODULE_UUID)){
            moduleid = String.valueOf(Constants.Acc_Customer_ModuleId);
        }
        String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
        if (valueMap.containsKey("filterConjuctionCriteria") && valueMap.get("filterConjuctionCriteria") != null) {
            if (valueMap.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                filterConjuctionCriteria = com.krawler.common.util.Constants.or;
            }
        }
        if (valueMap.containsKey("searchJson") && valueMap.get("searchJson") != null) {
            Searchjson = valueMap.get("searchJson").toString();

            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                try {
                    if(valueMap.containsKey("JoinMapKeySet"))
                    {
                        requestParams.put("JoinMapKeySet", valueMap.get("JoinMapKeySet"));
                    }
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray customSearchFieldArray = new JSONArray();
                    JSONArray defaultSearchFieldArray = new JSONArray();
                    StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);
                    if (defaultSearchFieldArray.length() > 0) {
                        /*
                         * Advance Search For Default Form fields
                         */
                        ArrayList tableArray = new ArrayList();
                        tableArray.add("customer"); //this table array used to identified wheather join exists on table or not                         
                        
                        requestParams.put("defaultSearchFieldArray",defaultSearchFieldArray);
                        requestParams.put("params",params);
                        requestParams.put("moduleid",moduleid);
                        requestParams.put("tableArray",tableArray);
                        requestParams.put("filterConjuctionCriteria",filterConjuctionCriteria);
                        requestParams.put("reportSqlSelect",reportSqlSelect);
                        requestParams.put("mainTable",mainTable);
                        requestParams.put("userId",(String)valueMap.get("userId"));
                        
                        Map<String, Object> map = reportBuilderDefaultFieldAdvSearch(requestParams);
                        
                        reportSqlSelect += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        selectCountQuery += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        reportJOINQuery += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                    }

                    if (customSearchFieldArray.length() > 0) {
                        try {
                            //Advance search case for Custome field
                            valueMap.put("isReportBuilder",true);
                            valueMap.put(Constants.Searchjson, Searchjson);
                            valueMap.put(Constants.appendCase, appendCase);
                            valueMap.put("filterConjuctionCriteria", filterConjuctionCriteria);
                            valueMap.put(Constants.moduleid,moduleid);
                            mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(valueMap, true).get(Constants.myResult));

                            LinkedHashSet<String> joinTablesSet = new LinkedHashSet<>();

                            joinTablesSet.add("customdatatable");
                            if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                                joinTablesSet.add("detailsdatatable");
                                if (Integer.valueOf(moduleid) == Constants.Acc_Invoice_ModuleId) {
                                    joinTablesSet.add("jedetailsdatatable");
                                }
                                joinTablesSet.add("AccJEDetailCustomData");
                            }
                            if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                                joinTablesSet.add("detailsdatatable");
                                if (Integer.valueOf(moduleid) == Constants.Acc_Invoice_ModuleId) {
                                    joinTablesSet.add("jedetailsdatatable");
                                }
                                joinTablesSet.add("AccJEDetailsProductCustomData");
                            }

                            if (mySearchFilterString.contains("CustomerCustomData")) {
                                joinTablesSet.add("CustomerCustomData");
                            }

                            if (mySearchFilterString.contains("VendorCustomData")) {
                                joinTablesSet.add("VendorCustomData");
                            }

                            if (mySearchFilterString.contains("accproductcustomdata")) {
                                joinTablesSet.add("detailsdatatable");
                                if (Integer.valueOf(moduleid) == Constants.Acc_Invoice_ModuleId) {
                                    joinTablesSet.add("inventorydatatable");
                                }
                                joinTablesSet.add("productdetails");
                            }

                            String joinTable = joinTablesSet.toString().replace("[", "").replace("]", "");
                            requestParams.clear();
                            requestParams.put("moduleid", moduleid);
                            requestParams.put("mappingfor", joinTable);
                            requestParams.put("mySearchFilterString", mySearchFilterString);
                            requestParams.put("reportSqlSelect", reportSqlSelect);
                            requestParams.put(CustomReportConstants.JOINQUERY, reportJOINQuery);

                            mappingCustomResult = getJoinQueryForCustomFields(requestParams);

                            joinString1 = mappingCustomResult.get("joinQuery");
                            mySearchFilterString = mappingCustomResult.get("mySearchFilterString");

                            if (mySearchFilterString.contains("accproductcustomdata")) {
                                joinString1 += " left join accproductcustomdata on accproductcustomdata.productId=product.id ";
                            }
                            StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                        } catch (JSONException | ParseException ex) {
                            Logger.getLogger(AccCustomReportsDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                } catch (JSONException ex) {
                    Logger.getLogger(AccCustomReportsDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        reportSQLQuery = reportSqlSelect + joinString1 + reportSqlWhereCond + mySearchFilterString;
        if(valueMap.containsKey("selectCountQuery") && valueMap.get("selectCountQuery")!= null)
        {
            reportCountSQLQuery = selectCountQuery +reportSqlWhereCond ;
        }
        reportCountSQLQuery = reportCountSQLQuery + mySearchFilterString;
        advanceSearchMap.put("reportSQLQuery",reportSQLQuery);
        advanceSearchMap.put("reportCountSQLQuery",reportCountSQLQuery);
        advanceSearchMap.put("paramList",params);
        return advanceSearchMap;
    }
    
    public Map<String, Object> reportBuilderDefaultFieldAdvSearch(Map requestParams) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            JSONArray defaultSearchFieldArray =  (JSONArray) requestParams.get("defaultSearchFieldArray");
            ArrayList params = (ArrayList) requestParams.get("params");
            String moduleid = (String) requestParams.get("moduleid");
            String moduleTable = requestParams.get("mainTable").toString();
            ArrayList tableArray = (ArrayList) requestParams.get("tableArray");
            String filterConjuctionCriteria = (String) requestParams.get("filterConjuctionCriteria");
            String reportSqlSelect = (String) requestParams.get("reportSqlSelect");
            
            String conditionSQL = "";
            String searchJoin = "";

            String query = "from DefaultHeaderModuleJoinReference where module=?";
            List<DefaultHeaderModuleJoinReference> refHeader = executeQuery(query, moduleid);

            for (int i = 0; i < defaultSearchFieldArray.length(); i++) {
                JSONObject jsonobj = defaultSearchFieldArray.getJSONObject(i);

                String fieldId = jsonobj.getString("column");
                String searchText = jsonobj.getString(Constants.searchText).trim();
                String headerTableName = "";
                String headerTableNameTemp="";
                String headercolumnName = "";
                String refTableName = "";
                String refTableColumn = "";
                String refModule = "";
                String moduleTablePK = "";
                String xtype="";
                boolean isLineItem = false;
                
                query = "from DefaultHeader where id=?";
                List<DefaultHeader> headerlist = executeQuery(query, fieldId);
                
                if (headerlist.size() > 0) {
                    DefaultHeader header = headerlist.get(0);

                    headerTableName = header.getDbTableName();
                    headercolumnName = header.getDbcolumnname();
                    refTableName = header.getReftablename();
                    refTableColumn = header.getReftabledatacolumn();
                    xtype = header.getXtype();
                    isLineItem = header.isIslineitem();

                    if (StringUtil.isNullOrEmpty(headerTableName)) {   // Search Field from reference module
                        headerTableName = header.getReftablename();
                        headercolumnName = header.getReftabledatacolumn();
                    }

                    if (!StringUtil.isNullOrEmpty(header.getReftablename()) && header.getReftablename().equals("journalentry")) {
                        headerTableName = header.getReftablename();
                        headercolumnName = header.getReftabledatacolumn();
                    }
                    if ((xtype.equalsIgnoreCase("1") && (StringUtil.isNullOrEmpty(headerTableName) || (!StringUtil.isNullOrEmpty(refTableName) && !StringUtil.isNullOrEmpty(refTableColumn)))) || ((!StringUtil.isNullOrEmpty(refTableColumn)) && (refTableColumn.contains("(") && refTableColumn.contains(")")))) {   // Search Field from reference module
                        headerTableName = refTableName;
                        headercolumnName = refTableColumn;
                    }
                    if(requestParams.containsKey("JoinMapKeySet"))
                    {
                        String[] joinmap=requestParams.get("JoinMapKeySet").toString().replaceAll("\\[", "").replaceAll("\\]","").split("\\s");
                        for(int j=0;j<=joinmap.length-1;j++)
                        {
                            if(joinmap[j].matches(headerTableName+"[0-9].") )
                            {
                                headerTableNameTemp=joinmap[j].substring(0,joinmap[j].indexOf(','));
                            }
                            if(headerTableName.equals(CustomReportConstants.PRODUCTCATEGORYMAPPING) && joinmap[j].equals(CustomReportConstants.PRODUCTCATEGORY))
                            {
                                headerTableNameTemp=joinmap[j];
                            }
                        }

                    }

                    /*
                     * added join on details table for searching on line level
                     * default fields(for e.g. Product Id)
                     */
                    if (isLineItem && !reportSqlSelect.contains(header.getDbTableName()) && !searchJoin.contains(header.getDbTableName()) && !refTableName.equalsIgnoreCase(Constants.Acc_Product_modulename)) {
                        searchJoin += " left join " + header.getDbTableName() + " on " + header.getDbTableName() + "." + moduleTable + " = " + moduleTable + "." + header.getReftablefk() + " ";
                    }
                    if (isLineItem && !reportSqlSelect.contains(header.getReftablename() + "." + header.getReftablefk()) && !searchJoin.contains(header.getReftablename() + "." + header.getReftablefk()) && !refTableName.equalsIgnoreCase(Constants.Acc_Product_modulename)) {
                        searchJoin += " left join " + header.getReftablename() + " on " + header.getReftablename() + "." + header.getReftablefk() + " = " + header.getDbTableName() + "." + header.getDbcolumnname() + " ";
                    }
                    if (header.isIsDefaultFieldMappings()) {
                        KwlReturnObject defaultFieldMappingResult = getDefaultFieldMappings(header.getId());
                        if (defaultFieldMappingResult != null) {
                            if (!moduleTable.equals(header.getReftablename()) && !reportSqlSelect.contains(header.getReftablename()) && !searchJoin.contains(header.getReftablename()) && !refTableName.equalsIgnoreCase(Constants.Acc_Product_modulename)) {
                                searchJoin += " left join " + header.getReftablename() + " on " + header.getReftablename() + "." + header.getReftablefk().trim() + " = " + header.getDbTableName().trim() + "." + header.getDbcolumnname().trim() + " ";
                            }
                            List<AccCustomReportsDefaultFieldsMapping> crDefaultFM = defaultFieldMappingResult.getEntityList();
                            Iterator crDefaultFM_itr = crDefaultFM.iterator();
                            while (crDefaultFM_itr.hasNext()) {
                                AccCustomReportsDefaultFieldsMapping obj = (AccCustomReportsDefaultFieldsMapping) crDefaultFM_itr.next();
                                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), obj.getDefaultheaderid());
                                DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                                String joinTableName = "";
                                if (moduleTable.equalsIgnoreCase(mappingDH.getReftablename().trim())) {
                                    joinTableName = mappingDH.getDbTableName().trim();
                                } else {
                                    joinTableName = mappingDH.getReftablename().trim();
                                }
                                if (!moduleTable.equals(joinTableName) && !reportSqlSelect.contains(joinTableName) && !searchJoin.contains(joinTableName)) {
                                    searchJoin += " left join " + joinTableName + " on " + joinTableName + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname().trim() + " ";
                                }
                                if (obj.isIsSelectDataIndex()) {
                                    if (header.getXtype().equals(Constants.four)) {   // Search Field from reference module
                                        headerTableName = mappingDH.getDbTableName();
                                        headercolumnName = mappingDH.getDbcolumnname();
                                    } else {
                                        headerTableName = mappingDH.getReftablename();
                                        headercolumnName = mappingDH.getReftabledatacolumn();
                                        if (mappingDH.getReftabledatacolumn() != null && mappingDH.getReftabledatacolumn().contains("(") && mappingDH.getReftabledatacolumn().contains(")")) {
                                            headerTableName = "";
                                        }
                                    }

                                }
                            }
                        }
                    }

                    refModule = header.getModule().getId();
                    moduleTablePK = header.getModule().getPrimaryKey_MethodName().toLowerCase();

                } else {
                    if (moduleid.equalsIgnoreCase("" + Constants.Acc_Make_Payment_ModuleId) || moduleid.equalsIgnoreCase("" + Constants.Acc_Receive_Payment_ModuleId)) {
                        if (fieldId.equalsIgnoreCase("1234") && moduleid.equalsIgnoreCase("" + Constants.Acc_Make_Payment_ModuleId)) {
                            headercolumnName = "masteragent";
                            headerTableName = "goodsreceipt";
                            if (!reportSqlSelect.contains("paymentdetail") && !searchJoin.contains("paymentdetail")) {
                                searchJoin += "  left join paymentdetail on paymentdetail.payment = payment.id ";
                            }
                            if (!reportSqlSelect.contains("goodsreceipt") && !searchJoin.contains("goodsreceipt")) {
                                searchJoin += "  left join goodsreceipt on goodsreceipt.id = paymentdetail.goodsReceipt ";
                            }
                            refModule = "" + Constants.Acc_Make_Payment_ModuleId;
                            xtype = "" + 4;
                        } else if (fieldId.equalsIgnoreCase("1234") && moduleid.equalsIgnoreCase("" + Constants.Acc_Receive_Payment_ModuleId)) {
                            headercolumnName = "mastersalesperson";
                            headerTableName = "invoice";
                            
                            if (!reportSqlSelect.contains("receiptdetails") && !searchJoin.contains("receiptdetails")) {
                                searchJoin += "  left join receiptdetails on receiptdetails.receipt = receipt.id ";
                            }
                            if (!reportSqlSelect.contains("invoice") && !searchJoin.contains("invoice")) {
                                searchJoin += "  left join invoice on invoice.id = receiptdetails.invoice ";
                            }
                            
                            refModule = "" + Constants.Acc_Receive_Payment_ModuleId;
                            xtype = "" + 4;
                        }
                    }
                }
                
                if (!moduleid.equalsIgnoreCase(refModule) && !refModule.trim().equalsIgnoreCase(Constants.Acc_Product_Master_ModuleId +"") && !refModule.trim().equalsIgnoreCase(Constants.Acc_Product_Category_ModuleId +"") && !refModule.trim().equalsIgnoreCase(Constants.CUSTOMER_CATEGORY_MODULE_ID +"")) { //for same module no need to add any of join 
                    searchJoin += StringUtil.getHeaderReferenceJoin(refHeader, tableArray, refModule);
                    for (DefaultHeaderModuleJoinReference reference : refHeader) { //used for finding where condition
                        if (reference.getRefModule().equalsIgnoreCase(refModule)) {
                            String refTable = reference.getRefModuleTableName();
                            conditionSQL=StringUtil.getDefaultHeaderConditionString(conditionSQL,refTable,headercolumnName,xtype,searchText,filterConjuctionCriteria,params);
                        }
                    }
                } else {
                    if(headerTableName.contains("linking") && !reportSqlSelect.contains(headerTableName) && !searchJoin.contains(headerTableName)){
                        searchJoin += " left join " + headerTableName + " on " + headerTableName + ".docid=" + moduleTable + "."+moduleTablePK+" and " + headerTableName + ".sourceflag = 1 ";
                    }
                    if (searchText.equalsIgnoreCase(Constants.CURRENT_USER)) {
                        searchText = (String) requestParams.get("userId");
                    }
                    if(!headerTableNameTemp.equals(""))
                    {
                        headerTableName=headerTableNameTemp;
                    }
                    conditionSQL=StringUtil.getDefaultHeaderConditionString(conditionSQL,headerTableName,headercolumnName,xtype,searchText,filterConjuctionCriteria,params);
                }
                if (!conditionSQL.equals("")) {         // when conjuction criteria applied for multiple fields then need to append
                conditionSQL += ")";
            }
            }
            if (!conditionSQL.equals("")) {
                conditionSQL += ")";
            }
            map.put("searchjoin", searchJoin);
            map.put("condition", conditionSQL);

        } catch (ServiceException | JSONException | ParseException ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }
    
    
    public Map getJoinQueryForCustomFields(Map requestParams) throws ServiceException {
        String joinQuery = "";
        String joinType = "";
        Map joinQueryMap = new HashMap();
        String moduleid = String.valueOf(requestParams.get("moduleid"));
        String mappingfor = String.valueOf(requestParams.get("mappingfor"));
        String reportSqlSelect = String.valueOf(requestParams.get("reportSqlSelect"));
        String reportJOINQuery = String.valueOf(requestParams.get(CustomReportConstants.JOINQUERY));
        String mySearchFilterString = String.valueOf(requestParams.get("mySearchFilterString"));
        String mappingforArr[] = mappingfor.split(",");

        String incase = "";
        for (int i = 0; i < mappingforArr.length; i++) {
            incase += "'" + mappingforArr[i].trim() + "',";
        }
        incase = incase.substring(0, incase.length() - 1);

        String query = "select defaultheaderid,mappingfor from customreportscustomfieldsmapping where module='" + moduleid + "' and mappingfor in(" + incase + ") ORDER BY FIELD(mappingfor," + incase + ")";

        List<Object[]> list = executeSQLQuery(query);

        for (Object[] obj : list) {
            boolean isAppendJoin = true;
            joinType = " left join ";
            String defaultHeaderId = String.valueOf(obj[0]);
            String mappingTable = String.valueOf(obj[1]);

            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), defaultHeaderId);
            DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
            String refTableName = mappingDH.getReftablename();

            if (mappingTable.equals("detailsdatatable") || mappingTable.equals("customdatatable")) {
                joinType = " inner join ";
            }

            if (mappingTable.equals("customdatatable") && !mySearchFilterString.contains(refTableName)) {
                isAppendJoin = false;
            }

            if (isAppendJoin && !reportJOINQuery.contains(refTableName)&& !joinQuery.contains(refTableName)) {
                joinQuery += joinType + refTableName + " on " + refTableName.trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname() + " ";
            }
            mySearchFilterString = mySearchFilterString.replaceAll(mappingTable, refTableName);

        }
        joinQueryMap.put("joinQuery", joinQuery);
        joinQueryMap.put("mySearchFilterString", mySearchFilterString);
        return joinQueryMap;
    }
    
    @Override
    public String getDefaultFieldDBColumnName(String fieldid, String moduleId) throws ServiceException {
        String columnName = "";
        String query = "from DefaultHeader where id=?";
        List<DefaultHeader> headerlist = executeQuery(query, fieldid);
        DefaultHeader header = headerlist.get(0);
        
        query = "from DefaultHeaderModuleJoinReference where module=?";
        List<DefaultHeaderModuleJoinReference> refHeader = executeQuery(query, moduleId);

        String headerTableName = header.getDbTableName();
        String headercolumnName = header.getDbcolumnname();
        if (StringUtil.isNullOrEmpty(headerTableName)) {   // Search Field from reference module
            headerTableName = header.getReftablename();
            headercolumnName = header.getReftabledatacolumn();
        }
        columnName = headerTableName+"."+headercolumnName;
        String refModule = header.getModule().getId();
        if (!moduleId.equalsIgnoreCase(refModule)) { //for same module no need to add any of join 
            for (DefaultHeaderModuleJoinReference reference : refHeader) { //used for finding where condition
                if (reference.getRefModule().equalsIgnoreCase(refModule)) {
                    String refTableName = reference.getRefModuleTableName();
                    columnName = refTableName+"."+headercolumnName;
                }
            }
        } else {
        }
        return columnName;
    }
    
    /**
     * Get the list of measure fields for the given module
     *
     * @param moduleID
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public KwlReturnObject getMeasureFields(String moduleID) throws ServiceException {
        KwlReturnObject result;
        int totalCount = 0;
        ArrayList params = new ArrayList();
        params.add(moduleID);
        params.add(true);
//        String query = "select customreportsmeasuresfieldsmapping.id,customreportsmeasuresfieldsmapping.defaultheaderid,customreportsmeasuresfields.measurefieldid,customreportsmeasuresfields.measurefieldname,customreportsmeasuresfields.measurefielddisplayname,customreportsmeasuresfields.measurefieldmodulecategory,customreportsmeasuresfields.measurefieldmodule,default_header.dbcolumnname,default_header.dbtabletame,default_header.reftabledatacolumn,\n" +
//                        "default_header.reftablename,default_header.reftablefk,default_header.dummyvalue from  customreportsmeasuresfieldsmapping,customreportsmeasuresfields,default_header\n" +
//                        "where customreportsmeasuresfields.measurefieldid=customreportsmeasuresfieldsmapping.measurefieldid\n" +
//                        "and customreportsmeasuresfieldsmapping.defaultheaderid=default_header.id  \n" +
//                        "and customreportsmeasuresfields.measurefieldmodule=?";
        String query = "from AccCustomReportsMeasuresFields where measurefieldmodule.id=? and iscustomreport = ?";                
        List<AccCustomReportsMeasuresFields> list = executeQuery(query, params.toArray());               
        //params.add(moduleID);
        //list = executeSQLQuery(query, params.toArray());
        totalCount = list.size();
        result = new KwlReturnObject(true, null, null, list, totalCount);
        return result;
    }
    
    @Override
    public KwlReturnObject getMeasureFields(Map<String, Object> requestParams) {
        List list = null;
        try {
            List<Integer> indexList = new ArrayList<Integer>();
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "from AccCustomReportsMeasuresFields ";
            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
                int ind = hql.indexOf("(");

                while (ind > -1) {
                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
                    indexList.add(index);
                    ind = hql.indexOf("(", ind + 1);
                }
                Collections.reverse(indexList);
                for (Integer ctr : indexList) {
                    value.remove(ctr.intValue());
                }
            }

            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }

            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
            list = executeQuery(hql, value.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     
    /**
     * Get the list of measure fields mappings for the given measure
     *
     * @param moduleID
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public KwlReturnObject getMeasureFieldMappings(String measureFieldID) throws ServiceException {
        KwlReturnObject result;
        int totalCount = 0;
        String query = "from AccCustomReportsMeasuresFieldsMapping where measurefieldid=?";                
        List<AccCustomReportsMeasuresFields> list = executeQuery(query, measureFieldID);               
        totalCount = list.size();
        result = new KwlReturnObject(true, null, null, list, totalCount);
        return result;
    }
    @Override
    public KwlReturnObject getTaxPercent(String companyid, Date transactiondate, String taxid) throws ServiceException {
        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, transactiondate, taxid);
        return perresult;
    }
    
    @Override
    public KwlReturnObject getCurrencyToBaseAmount(Map requestParams, Double amount, String currencyid, Date transactiondate, double rate) throws ServiceException {
        KwlReturnObject perresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, currencyid, transactiondate, rate);
        return perresult;
    }
    
    @Override
    public KwlReturnObject getPaymentVendorNames(String companyid, String paymentid) throws ServiceException {
        KwlReturnObject perresult = accVendorPaymentobj.getPaymentVendorNames(companyid, paymentid);
        return perresult;
    }
    
    @Override
    public KwlReturnObject getReceiptCustomerNames(String companyid, String receiptid) throws ServiceException {
        KwlReturnObject result = accReceiptDAOobj.getReceiptCustomerNames(companyid, receiptid);
        return result;
    }
    
    @Override
    public KwlReturnObject getRepaymentSheduleDetails(Map mapForRepaymentDetails) throws ServiceException {
        KwlReturnObject result = accLoanDAOobj.getRepaymentSheduleDetails(mapForRepaymentDetails);
        return result;
    }
    
    @Override
    public KwlReturnObject getProductPrice(String productid, boolean isPurchase, Date transactiondate, String affecteduser, String forCurrency) throws ServiceException {
        KwlReturnObject prdPriceRes = accProductObj.getProductPrice(productid, true, null, "", "");
        return prdPriceRes;
    }
    @Override
    public KwlReturnObject getTax(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject perresult = accTaxObj.getTax(requestParams);
        return perresult;
    }
    @Override
     public KwlReturnObject getCustomReportsCustomFieldsMapping(String moduleID,boolean isLineItem,String mappingfor) throws ServiceException {
        KwlReturnObject result;
        int totalCount = 0;
        ArrayList params = new ArrayList();
        params.add(moduleID);
        params.add(isLineItem);
        String query = "from AccCustomReportsCustomFieldsMapping where module=? and islineitem=?"; 
        if(!StringUtil.isNullOrEmpty(mappingfor)){
            query +=" and mappingFor in(" + mappingfor + ") ORDER BY FIELD(mappingFor," + mappingfor + ")";
//            params.add(mappingfor);
        }
        List<AccCustomReportsMeasuresFields> list = executeQuery(query, params.toArray()); 
        totalCount = list.size();
        result = new KwlReturnObject(true, null, null, list, totalCount);
        return result;
    }
    @Override
    public KwlReturnObject getDefaultFieldMappings(String defaultFieldID) throws ServiceException {
        KwlReturnObject result;
        int totalCount = 0;
        String query = "from AccCustomReportsDefaultFieldsMapping where defaultfieldid=?";                
        List<AccCustomReportsDefaultFieldsMapping> list = executeQuery(query, defaultFieldID);               
        totalCount = list.size();
        result = new KwlReturnObject(true, null, null, list, totalCount);
        return result;
    }
    
    @Override
    public boolean isTermMappedwithTax(HashMap<String, Object> requestParams) throws ServiceException {
        boolean isTermMappedWithTax = accTaxObj.isTermMappedwithTax(requestParams);
        return isTermMappedWithTax;
    }
    
    @Override
    public KwlReturnObject getTaxList(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject taxListResult = accTaxObj.getTaxList(requestParams);
        return taxListResult;
    }
    
    @Override
    public KwlReturnObject getSalesOrderTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String salesOrderID = hm.get("salesOrder").toString();
            String query = "from SalesOrderTermMap where salesOrder.ID = ?";
            list = executeQuery( query, new Object[]{salesOrderID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getSalesOrderTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPurchaseOrderTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String purchaseOrderID = hm.get("purchaseOrder").toString();
            String query = "from PurchaseOrderTermMap where purchaseOrder.ID = ?";
            list = executeQuery( query, new Object[]{purchaseOrderID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderImpl.getPurchaseOrderTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getDOTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String deliveryOrderID = hm.get("deliveryOrderID").toString();
            String query = "from DeliveryOrderTermMap where deliveryOrder.ID = ?";
            list = executeQuery( query, new Object[]{deliveryOrderID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.getDOInvoiceTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getGRTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String goodsReceiptID = hm.get("goodsReceiptID").toString();
            String query = "from GoodsReceiptOrderTermMap where goodsReceiptOrder.ID = ?";
            list = executeQuery( query, new Object[]{goodsReceiptID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getGRTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     
    @Override
    public KwlReturnObject getMultiApprovalRuleData(HashMap<String, Object> dataMap) throws ServiceException {
        KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(dataMap);
        return flowresult;
    }

    @Override
    public KwlReturnObject getApprovalRuleTargetUsers(HashMap<String, Object> dataMap) throws ServiceException {
        KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(dataMap);
        return userResult;
    }

    @Override
    public KwlReturnObject getRoleofUser(String userid) throws ServiceException {
        KwlReturnObject kmsg = permissionHandlerDAOObj.getRoleofUser(userid);
        return kmsg;
    }
    
     public KwlReturnObject getInvoiceTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String invoiceID = hm.get("invoiceid").toString();
            String query = "from InvoiceTermsMap where invoice.ID = ?";
            list = executeQuery( query, new Object[]{invoiceID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCustomReportsDAOImpl.getInvoiceTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
      public KwlReturnObject getReceiptTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String invoiceID = hm.get("invoiceid").toString();
            String query = "from ReceiptTermsMap where goodsreceipt.ID = ?";
            list = executeQuery( query, new Object[]{invoiceID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCustomReportsDAOImpl.getReceiptTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
     public KwlReturnObject getCustomReportsDefaultFieldsNeededMapping(String moduleID,boolean isLineItem) throws ServiceException {
        KwlReturnObject result;
        int totalCount = 0;
        ArrayList params = new ArrayList();
        params.add(moduleID);
        params.add(isLineItem);
        String query = "from AccCustomReportsCustomFieldsMapping where module=? and isdefaultitem=?";                
        List<AccCustomReportsMeasuresFields> list = executeQuery(query, params.toArray()); 
        totalCount = list.size();
        result = new KwlReturnObject(true, null, null, list, totalCount);
        return result;
    }
    
    @Override
    public KwlReturnObject saveCustomWidgetReports(JSONObject dataObj) throws ServiceException {
        List list = new ArrayList();
        try {
            CustomWidgetReports widgetReport = new CustomWidgetReports();
            String incidentId = dataObj.has("id") ? dataObj.optString("id", "") : "";

            if (StringUtil.isNullOrEmpty(incidentId)) {
                if (dataObj.has(Constants.useridKey) && dataObj.get(Constants.useridKey) != null) {
                    User user = (User) get(User.class, dataObj.getString(Constants.useridKey));
                    widgetReport.setCreatedby(user);
                    widgetReport.setModifiedby(user);
                }

                if (dataObj.has("createdon")) {
                    widgetReport.setCreatedon(dataObj.getLong("createdon"));
                }
                if (dataObj.has("updatedon")) {
                    widgetReport.setUpdatedon(dataObj.getLong("updatedon"));
                }
            } else {
                widgetReport = dataObj.get("id") == null ? null : (CustomWidgetReports) get(CustomWidgetReports.class, dataObj.getString("id"));
                if (dataObj.has(Constants.useridKey) && dataObj.get(Constants.useridKey) != null) {
                    User user = (User) get(User.class, dataObj.getString(Constants.useridKey));
                    widgetReport.setModifiedby(user);
                }
                if (dataObj.has("updatedon")) {
                    widgetReport.setUpdatedon(dataObj.getLong("updatedon"));
                }
            }

            widgetReport.setDeleted(false);


            if (dataObj.has("reportname")) {
                widgetReport.setReportName(dataObj.getString("reportname"));
            }
            if (dataObj.has("customreports")) {
                widgetReport.setCustomReports(dataObj.getString("customreports"));
            }
            if (dataObj.has("searchcriteria")) {
                widgetReport.setSearchCriteria(dataObj.getString("searchcriteria"));
            }
            if (dataObj.has("filterappend")) {
                int filterAppend = 1;
                String filterAppendStr = dataObj.optString("filterappend");
                if (!StringUtil.isNullOrEmpty(filterAppendStr) && StringUtil.equal(filterAppendStr, "OR")) {
                    filterAppend = 0;
                }
                widgetReport.setFilterAppend(filterAppend);
            }
            if (dataObj.has(Constants.companyKey) && dataObj.get(Constants.companyKey) != null) {
                Company company = (Company) get(Company.class, dataObj.getString(Constants.companyKey));
                if (company != null) {
                    widgetReport.setCompany(company);
                }
            }

            saveOrUpdate(widgetReport);
            list.add(widgetReport);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveCustomWidgetReports : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Report Saved successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject getCustomWidgetReports(JSONObject dataObj) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        int totalCount = 0;

        String ss = dataObj.optString(Constants.ss);
        String start = dataObj.optString(Constants.start, null);
        String limit = dataObj.optString(Constants.limit, null);

        String selectQuery = " from CustomWidgetReports reports ";

        String conditionQuery = " where reports.company.companyID = ? ";
        params.add(dataObj.optString(Constants.companyKey, ""));

        if (dataObj.has("reportname")) {
            conditionQuery += " and reports.reportName = ? ";
            params.add(dataObj.optString("reportname", ""));
        }

        String orderBy = " order by reports.reportName asc ";

        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"reports.report", "reports.customReports", "reports.searchCriteria"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 3);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                conditionQuery += searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(AccCustomReportsDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        String sqlQuery = selectQuery + conditionQuery + orderBy;

        list = executeQuery(sqlQuery, params.toArray());
        totalCount = list.size();

        if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
            list = executeQueryPaging(sqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
        }

        return new KwlReturnObject(true, null, null, list, totalCount);
    }

    @Override
    public boolean deleteCustomWidgetReport(JSONObject paramJobj) throws ServiceException {

        boolean successDelete = false;
        ArrayList params = new ArrayList();
        String reportIDsToDelete = paramJobj.optString("reportIds");
        String query = "delete from CustomWidgetReports reports where reports.company.companyID = ? ";
        params.add(paramJobj.optString("companyid"));

        if (!StringUtil.isNullOrEmpty(reportIDsToDelete)) {
            query += "and reports.ID in (" + reportIDsToDelete + ") ";

            int count = executeUpdate(query, params.toArray());
            if (count > 0) {
                successDelete = true;
            }
        }

        return successDelete;
    }

    @Override
    public KwlReturnObject getJEForDebitNote(String dnID) throws ServiceException {
        String query = " select journalentry from debitnote where id= ? ";
        ArrayList params = new ArrayList();
        params.add(dnID);
        List list = executeSQLQuery(query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    
    @Override
    public KwlReturnObject getJEForCreditNote(String dnID) throws ServiceException {
        String query = " select journalentry from creditnote where id= ? ";
        ArrayList params = new ArrayList();
        params.add(dnID);
        List list = executeSQLQuery(query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    
    @Override
    public Object getTimzeZoneClassObject(String classpath, String id) throws ServiceException {
        Object obj = null;
        obj =(Object) kwlCommonTablesDAOObj.getClassObject(classpath, id);
        return obj;
    }

    @Override
    public String getSOStatus(SalesOrder so, CompanyAccountPreferences pref,ExtraCompanyPreferences extraCompanyPreferences) throws ServiceException {
        String status ="";
        status = accSalesOrderServiceDAOobj.getSOStatus(so, pref,extraCompanyPreferences);
        return status;
    }
    
     @Override
    public KwlReturnObject getLinkedModules(String module) throws ServiceException {
        KwlReturnObject result;
        int totalCount = 0;
        ArrayList params = new ArrayList();
        params.add(module);
        params.add(true);
        String query = "from CrossModuleLinkingDetails where module=? and allowcrossmodule=?";                
        List<CrossModuleLinkingDetails> list = executeQuery(query, params.toArray()); 
        totalCount = list.size();
        result = new KwlReturnObject(true, null, null, list, totalCount);
        return result;
    }

    @Override
    public KwlReturnObject getLinkedModuleJoinDetails(String module, String linkedModule) throws ServiceException {
        KwlReturnObject result;
        int totalCount = 0;
        ArrayList params = new ArrayList();
        params.add(module);
        params.add(linkedModule);
        String query = "from CrossModuleJoinDetails where module=? and linkedmodule=?";                
        List<CrossModuleJoinDetails> list = executeQuery(query, params.toArray()); 
        totalCount = list.size();
        result = new KwlReturnObject(true, null, null, list, totalCount);
        return result;
    }
    
        /**
     * This method returns the module id and module name for the selected module
     * category
     *
     * @param moduleCatID
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    @Override
    public KwlReturnObject getReportsFromReportList(String moduleCatID) throws ServiceException {
        String query = " select ID, name from ReportMaster rm where rm.showInReportBuilder = true";
        ArrayList params = new ArrayList();
        List list = executeQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
        
    /**
     * Get the DefaultHeaders' ID, dummyvalue, xtype and defaultHeader 
     * for selected report from Report-List
     *
     * @param reportId
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    @Override
    public KwlReturnObject getDefaultHeaderDetailsForReportFromReportList(String reportId) throws ServiceException {
        String query = " select id, dummyvalue, xtype, defaultHeader from default_header dh where dh.module = ?";
        ArrayList params = new ArrayList();
        params.add(reportId);   //reportId is used as module in default_header
        List list = executeSQLQuery(query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    
    /**
     * Get Report Name from Report-List by ReportID
     *
     * @param reportId
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    @Override
    public String getReportNameFromReportListByReportId(String reportId) throws ServiceException {
        String reportName = null;
        String query = "select name from reportmaster where id = ?";
        ArrayList params = new ArrayList();
        params.add(reportId);
        List<String> list = executeSQLQuery(query, params.toArray());
        if(!list.isEmpty()) {
            reportName = (String) list.get(0);
        }
        return reportName;
    }
    
    /**
     * Get Report Service URL from Report-List by ReportID
     *
     * @param reportId
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    @Override
    public String getReportURLFromReportList(String reportId) throws ServiceException {
        String reportUrl = null;
        String query = "select widgetURL from reportmaster where id = ?";
        ArrayList params = new ArrayList();
        params.add(reportId);
        List<String> list = executeSQLQuery(query, params.toArray());
        if(!list.isEmpty()) {
            reportUrl = (String) list.get(0);
        }
        return reportUrl;
    }
    
    @Override
    public KwlReturnObject getCustomerInvoiceCreationAndEntryDates(String invoiceId) throws ServiceException {
        String query = " select journalentry.createdon , journalentry.entrydate   from invoice   left join journalentry on invoice.journalentry = journalentry.id  where  invoice.id = ?";
        ArrayList params = new ArrayList();
        params.add(invoiceId); 
        List list = executeSQLQuery(query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    
    @Override
    public KwlReturnObject getSalesReturnCreationAndEntryDates(String srId) throws ServiceException {
        String query = "  select journalentry.createdon , journalentry.entrydate   from salesreturn   left join journalentry on salesreturn.inventoryje = journalentry.id  where  salesreturn.id = ?";
        ArrayList params = new ArrayList();
        params.add(srId); 
        List list = executeSQLQuery(query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    
    /**
     * Used to save or update custom chart details
     *
     * @param paramObj
     * @return KwlReturnObject
     * @throws ServiceException
     */
    
    @Override
    public KwlReturnObject saveOrUpdateChartDetails(JSONObject paramObj) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String respMsg = "";
        try {
            CustomReportCharts chartDetails;
            //if request contain chart id then apply update
            if (!StringUtil.isNullOrEmpty(paramObj.getString("id"))) {
                chartDetails = (CustomReportCharts) getSession().get(CustomReportCharts.class, paramObj.getString("id"));
                
                if (paramObj.has(Constants.useridKey) && paramObj.get(Constants.useridKey) != null) {
                    User user = (User) get(User.class, paramObj.getString(Constants.useridKey));
                    chartDetails.setModifiedby(user);
                }
                if (paramObj.has("modifiedon")) {
                    chartDetails.setModifiedOn(paramObj.getLong("modifiedon"));
                }
                respMsg = "acc.CustomReport.Chart.updateSuccess.msg";
            } else {
                //if request contain chart id then apply save
                chartDetails = new CustomReportCharts();
                
                if (paramObj.has(CustomReportConstants.REPORT_ID)) {
                    chartDetails.setReportID(paramObj.getString(CustomReportConstants.REPORT_ID));
                }
                
                if (paramObj.has(Constants.useridKey) && paramObj.get(Constants.useridKey) != null) {
                    User user = (User) get(User.class, paramObj.getString(Constants.useridKey));
                    chartDetails.setCreatedby(user);
                    chartDetails.setModifiedby(user);
                }

                if (paramObj.has("createdon")) {
                    chartDetails.setCreatedOn(paramObj.getLong("createdon"));
                }
                
                if (paramObj.has("modifiedon")) {
                    chartDetails.setModifiedOn(paramObj.getLong("modifiedon"));
                }
                
                if (paramObj.has(Constants.companyKey) && paramObj.get(Constants.companyKey) != null) {
                    Company company = (Company) get(Company.class, paramObj.getString(Constants.companyKey));
                    if (company != null) {
                        chartDetails.setCompany(company);
                    }
                }
                
                respMsg = "acc.CustomReport.Chart.saveSuccess.msg";
            }
            
            if(paramObj.has(CustomReportConstants.CHART_NAME)) {
                chartDetails.setChartName(paramObj.getString(CustomReportConstants.CHART_NAME));
            }
            
            if(paramObj.has(CustomReportConstants.CHART_TYPE)) {
                chartDetails.setChartType(paramObj.getString(CustomReportConstants.CHART_TYPE));
            }
            
            if(paramObj.has(CustomReportConstants.TITLE_FIELD)) {
                chartDetails.setTitleField(paramObj.getString(CustomReportConstants.TITLE_FIELD));
            }   
            
            if(paramObj.has(CustomReportConstants.VALUE_FIELD)) {
                chartDetails.setValueField(paramObj.getString(CustomReportConstants.VALUE_FIELD));
            }
            
            if(paramObj.has(CustomReportConstants.GROUP_BY)) {
                chartDetails.setGroupby(paramObj.getString(CustomReportConstants.GROUP_BY));
            }
            
            if(paramObj.has("properties")) {
                chartDetails.setProperties(paramObj.getString("properties"));
            }
            
            saveOrUpdate(chartDetails);
            list.add(chartDetails);
            count = list.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveOrUpdateChartDetails : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, respMsg, null, list, count);
    }
    
    /**
     * Used to get saved chart details
     *
     * @param paramObj
     * @return KwlReturnObject
     * @throws ServiceException
     */
    
    @Override
    public KwlReturnObject getChartDetails(JSONObject paramObj) throws ServiceException {
        List<CustomReportCharts> list = new ArrayList<>();
        int count = 0;
        try {
            String reportId = paramObj.optString("reportId", "");
            ArrayList params = new ArrayList();
            if (!StringUtil.isNullOrEmpty(reportId)) {
                String query = "from CustomReportCharts where reportid = ?";
                params.add(reportId);

                list = executeQuery(query, params.toArray());
                count = list.size();
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("getChartDetails : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    
    /**
     * delete chart using chart id from paramObj
     *
     * @param paramObj
     * @return KwlReturnObject
     * @throws ServiceException
     */
    
    @Override
    public KwlReturnObject deleteChartDetails(JSONObject paramObj) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            String id = paramObj.optString("id", "");
            
            CustomReportCharts chartDetails = new CustomReportCharts();
            chartDetails.setID(id);
            
            Company company = (Company) get(Company.class, paramObj.getString(Constants.companyKey));
            chartDetails.setCompany(company);

            delete(chartDetails);
        } catch (Exception e) {
            throw ServiceException.FAILURE("deleteChartDetails : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Chart deleted", null, list, count);
    }
    
    @Override
    public KwlReturnObject getInvoiceForPayment(JSONObject paramObj) throws ServiceException {
            String query = "  select id from paymentdetail where payment=? and company= ?";
            ArrayList params = new ArrayList();
            params.add(paramObj.optString("payment"));
            params.add(paramObj.optString("companyid"));
            List list = executeSQLQuery(query, params.toArray());
            int count = list.size();
            return new KwlReturnObject(true, "", "", list, count);
    }
    
   @Override
    public KwlReturnObject getInvoiceForReceipt(JSONObject paramObj) throws ServiceException { 
         String query = "  select id from receiptdetails where receipt=? and company=?";
            ArrayList params = new ArrayList();
            params.add(paramObj.optString("receipt"));
            params.add(paramObj.optString("companyid"));
            List list = executeSQLQuery(query, params.toArray());
            int count = list.size();
            return new KwlReturnObject(true, "", "", list, count);
    }
    
    
    @Override
     public KwlReturnObject getLinkedInvoiceFromPayment(JSONObject paramObj) throws ServiceException{
         String query = "  select goodsReceipt from linkdetailpayment where payment=?";
        ArrayList params = new ArrayList();
        params.add(paramObj.optString("payment")); 
        List list = executeSQLQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
     }
     
    @Override
     public KwlReturnObject getLinkedInvoiceFromReceipt(JSONObject paramObj) throws ServiceException{
         String query = "  select invoice from linkdetailreceipt where receipt=?";
        ArrayList params = new ArrayList();
        params.add(paramObj.optString("receipt")); 
        List list = executeSQLQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
     }
     
    @Override
     public KwlReturnObject getCreditNoteLinkingForPayment(JSONObject paramObj) throws ServiceException{
         String query = "  select cnid from creditnotpayment where paymentid=?";
        ArrayList params = new ArrayList();
        params.add(paramObj.optString("payment")); 
        List list = executeSQLQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
     }
     
    @Override
     public KwlReturnObject getDebitNoteForReceipt(JSONObject paramObj) throws ServiceException{
         String query = "  select dnid from debitnotepayment where receiptid=?";
        ArrayList params = new ArrayList();
        params.add(paramObj.optString("receipt")); 
        List list = executeSQLQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
     }
     
     @Override
     public KwlReturnObject getLinkedDebitNoteFromReceipt(JSONObject paramObj) throws ServiceException{
         String query = " select debitnote from linkdetailreceipttodebitnote where receipt=?";
        ArrayList params = new ArrayList();
        params.add(paramObj.optString("receipt")); 
        List list = executeSQLQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
     }
     
      @Override
     public KwlReturnObject getLinkedCreditNoteFromPayment(JSONObject paramObj) throws ServiceException{
         String query = " select creditnote from linkdetailpaymenttocreditnote where payment=?";
        ArrayList params = new ArrayList();
        params.add(paramObj.optString("payment")); 
        List list = executeSQLQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
     }
    /**
     * save grid state config of custom report for current user
     * @param GridConfig
     * @return saved GridConfig object
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject saveGridConfig(GridConfig gridConfig) throws ServiceException {
        List list = new ArrayList();
        try {
            if(!StringUtil.isNullObject(gridConfig)) {
                saveOrUpdate(gridConfig);
                list.add(gridConfig);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveGridConfig : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    /**
     * get saved grid state config of custom report for current user
     * @param paramObj
     * @return GridConfig object
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject getGridConfig(HashMap<String, Object> paramObj) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            Company company = null;
            User user = null;
            String moduleid = null;
            
            if(!StringUtil.isNullObject(paramObj.get("company")) && !StringUtil.isNullObject(paramObj.get("user")) && !StringUtil.isNullObject(paramObj.get("moduleid"))) {
                company = (Company) paramObj.get("company");
                user = (User) paramObj.get("user");
                moduleid = paramObj.get("moduleid").toString();
            }
            
            if(!StringUtil.isNullObject(company) && !StringUtil.isNullObject(user) && !StringUtil.isNullOrEmpty(moduleid)) {
                String query = "FROM GridConfig WHERE moduleid = ? AND user = ? AND company = ?";
                params.add(moduleid);
                params.add(user);
                params.add(company);
                
                list = executeQuery(query, params.toArray());
            }
            
        } catch (Exception e) {
            throw ServiceException.FAILURE("getGridConfig : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
 
    @Override
    public KwlReturnObject getCustomReportsGSTFieldsLineLevelTermsMapping(String gstFieldID) throws ServiceException {
        KwlReturnObject result;
        int totalCount = 0;
        String query = "from CustomReportsGSTFieldsLineLevelTermsMapping where defaultHeaderId in("+gstFieldID+")";
        List<AccCustomReportsMeasuresFields> list = executeQuery(query);
        totalCount = list.size();
        result = new KwlReturnObject(true, null, null, list, totalCount);
        return result;
}
    
    @Override
    public List getDefaultStateValue(JSONObject paramObj) throws ServiceException {
        List returnList = null;
        ArrayList params = new ArrayList();
        params.add(paramObj.optInt("gstinStateCode"));
        params.add(paramObj.optInt("country"));
        String query = "select statename from defaultstatevalues where id =? and country=?";
        returnList = executeSQLQuery(query, params.toArray());
        return returnList;
}

    @Override
    public int updateEwayJSONExportFlag(List updateDocumentList, Map requestParam) throws ServiceException {
        KwlReturnObject result;
        int totalOutput = 0;
        int output = 0;
        String tableName = (String) requestParam.get("tableName");
        String setStatus = (String) requestParam.get("setStatus");
        while (!updateDocumentList.isEmpty()) {
            int cnt = 0;
            String ids = "";
            while (!updateDocumentList.isEmpty() && cnt != 500) {
                if (StringUtil.isNullOrEmptyWithTrim(ids)) {
                    ids = "'" + (String) updateDocumentList.get(0) + "'";
                } else {
                    ids += "," + "'" + (String) updateDocumentList.get(0) + "'";
                }
                updateDocumentList.remove(0);
                cnt++;
            }
            String query = "update " + tableName + " set isewayexported=" + setStatus + " where id in (" + ids + ")";
            output = executeSQLUpdate(query);
            totalOutput += output;
        }
        return output;
    }

    @Override
    public Map getEntityDetails(Map requestParams) throws ServiceException {
        String companyid = requestParams.get("companyID") != null ? (String) requestParams.get("companyID") : " ";
        String entityName = requestParams.get("entityName") == null ? "" : (String) requestParams.get("entityName");
        String moduleid = requestParams.get("moduleID") == null ? "" : (String) requestParams.get("moduleID");
        Map entityDetail = new HashMap();
        ArrayList param = new ArrayList();
        String description = "";
        String stateID = "";
        String gstColNum = "";
        String pinCode = "";
        String gstin = "";
        String State = "";
        String entityID = "";
        String pinCodeColNum = "";
        String city = "";
        String cityColNum = "";
        String docEntity = "";
        String stateColNum = "";
        int EntitycolNum = 0;

        param.add("State");
        param.add(companyid);
        String StateQuery = "select  colnum from fieldparams where moduleid=1200 and fieldlabel=? and companyid=?";
        List resultList = executeSQLQuery(StateQuery, param.toArray());
        if (resultList.size() > 0) {
            stateColNum = "col" + String.valueOf(resultList.get(0));
        }

        param.clear();
        param.add("Pin Code");
        param.add(companyid);
        String pincodeQuery = "select  colnum from fieldparams where moduleid=1200 and fieldlabel=? and companyid=?";
        resultList = executeSQLQuery(pincodeQuery, param.toArray());
        if (resultList.size() > 0) {
            pinCodeColNum = "col" + String.valueOf(resultList.get(0));
        }

        param.clear();
        param.add("City");
        param.add(companyid);
        String cityQuery = "select  colnum from fieldparams where moduleid=1200 and fieldlabel=? and companyid=?";
        resultList = executeSQLQuery(cityQuery, param.toArray());
        if (resultList.size() > 0) {
            cityColNum = "col" + String.valueOf(resultList.get(0));
        }

        param.clear();
        param.add("GSTIN");
        param.add(companyid);
        String gstinQuery = "select  colnum from fieldparams where moduleid=1200 and fieldlabel=? and companyid=?";
        resultList = executeSQLQuery(gstinQuery, param.toArray());
        if (resultList.size() > 0) {

            gstColNum = "col" + String.valueOf(resultList.get(0));
        }

        param.clear();
        param.add(companyid);
        String EntityQuery = "select id, colnum from FieldParams where moduleid=1200 and fieldlabel='Entity' and companyid=?";
        resultList = executeQuery(EntityQuery, param.toArray());
        if (resultList.size() > 0) {
            entityID = (String) ((Object[]) resultList.get(0))[0];
            EntitycolNum = (Integer) ((Object[]) resultList.get(0))[1];
        }

        param.clear();
        param.add(entityID);
        param.add(entityName);
        String entityFCD = "select id, itemdescription from FieldComboData where fieldid=? and value =?";
        resultList = executeQuery(entityFCD, param.toArray());
        if (resultList.size() > 0) {
            description = (String) ((Object[]) resultList.get(0))[1];
            docEntity = (String) ((Object[]) resultList.get(0))[0];
        }

        param.clear();
        param.add(docEntity);
        String multiEntityQuery = "select " + stateColNum + ", " + pinCodeColNum + ", " + gstColNum + ", " + cityColNum + " from MultiEntityDimesionCustomData where id=?";
        resultList = executeQuery(multiEntityQuery, param.toArray());
        if (resultList.size() > 0) {
            stateID = (String) ((Object[]) resultList.get(0))[0];;
            pinCode = (String) ((Object[]) resultList.get(0))[1];;
            gstin = (String) ((Object[]) resultList.get(0))[2];;
            city = (String) ((Object[]) resultList.get(0))[3];;
        }

        param.clear();
        param.add(stateID);
        String stateFetchingQuery = "select value from FieldComboData where id=?";
        resultList = executeQuery(stateFetchingQuery, param.toArray());
        if (resultList.size() > 0) {
            State = (String) resultList.get(0);
        }

        entityDetail.put("description", description);
        entityDetail.put("state", State);
        entityDetail.put("pinCode", pinCode);
        entityDetail.put("gstin", gstin);
        entityDetail.put("city", city);

        return entityDetail;
    }

    public SqlRowSet getDefaultStates() throws ServiceException {
        String query = "select * from defaultstatevalues";
        SqlRowSet rs = executeCustomReportSQL(query, new ArrayList());
        return rs;
    }

    @Override
    public KwlReturnObject getCustomReportsDefaults(HashMap<String, String> requestMapParams) throws ServiceException {
        List list = null;
        int listSize = 0;
        try {
            String countryID = requestMapParams.get("countryid");
            String condition = "";
            String moduleID = requestMapParams.get("moduleid");
            if (requestMapParams.containsKey("moduleid") && !StringUtil.isNullOrEmpty(moduleID)) {
                condition = " and crd.moduleid=?";
            }
            ArrayList params = new ArrayList();
            String hql = "from CustomReportsDefaults crd where crd.countryid.ID=? and crd.deleteflag=0 " + condition;
            if (!StringUtil.isNullOrEmpty(countryID)) {
                params.add(countryID);
                if (!StringUtil.isNullOrEmpty(moduleID)) {
                    params.add(moduleID);
                }
                list = executeQuery(hql, params.toArray());
            }
            if (list != null) {
                listSize = list.size();
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("Gives error while fetching Default Custom Reports.", ex);
        }
            return new KwlReturnObject(true, "", null, list, listSize);        
    }

    @Override
    public KwlReturnObject getModuleCategoryForModule(String moduleID) throws ServiceException {
        String query = " select modulecategory from modules where id =  ? ";
        ArrayList params = new ArrayList();
        params.add(moduleID);
        List list = executeSQLQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    
    public SavedSearchQuery getSaved_Search_Query(String customreportid) throws ServiceException {
        SavedSearchQuery searchdetails=null;
        String query = " from SavedSearchQuery  where customReportId =  ? ";
        ArrayList params = new ArrayList();
        params.add(customreportid);
        List<SavedSearchQuery> list= executeQuery(query, params.toArray());
        int count = list.size();
        if (count > 0) {
            searchdetails = list.get(count - 1);
        }
        return searchdetails;
    }
}
