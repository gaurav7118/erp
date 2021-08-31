/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.entitygst;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.EntitybasedLineLevelTermRate;
import com.krawler.hql.accounting.GSTCessRuleType;
import com.krawler.hql.accounting.LineLevelTerms;
import com.krawler.hql.accounting.ProductCategoryGstRulesMappping;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.gst.dto.GSTR2Submission;
import com.krawler.spring.accounting.gst.services.GSTRConstants;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Suhas.Chaware
 */
public class AccEntityGstImpl extends BaseDAO implements AccEntityGstDao {

    /**
     *
     * @param requestParams
     */
    public KwlReturnObject getChildFieldParamsForGSTRule(HashMap requestParams) throws ServiceException {
        String query = "from FieldParams";
        return buildNExecuteQuery(query, requestParams);
    }

    public KwlReturnObject getFieldCombodataForModule(JSONObject reqParams) throws ServiceException, JSONException {
        String condition = "";
        ArrayList params = new ArrayList();
        if (reqParams.has("companyid")) {
            condition += " where fp.companyid=?";
            params.add(reqParams.optString("companyid"));
        }
        if (reqParams.has("moduleid")) {
            condition += " and fp.moduleid=?";
            params.add(reqParams.optInt("moduleid"));
        }
        if (reqParams.has("fieldlable")) {
            condition += " and fp.fieldlabel=?";
            params.add(reqParams.optString("fieldlable"));
        }
        String ss = reqParams.optString("query");
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"fcd.value"};
                
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.has("fieldlable") || !StringUtil.isNullOrEmpty(ss)) {
            condition += " and fcd.value is not null and fcd.value <> '' ";
            condition += " order by fcd.itemsequence";
        }
        if (reqParams.has("fieldid")) {
            condition += " and fp.id=?";
            params.add(reqParams.optString("fieldid"));
        }
        if (reqParams.has("isMultiEntity")) {
            condition += " and fp.GSTConfigType=?";
            params.add(reqParams.optBoolean("isMultiEntity")?Constants.GST_CONFIG_ISFORMULTIENTITY:0);
        }
        String query = "select fcd From FieldComboData fcd inner join fcd.field fp" + condition;
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    /**
     * ERP-33643
     * @param params (State : StateID from FieldComboData, Entity : EntityID from FieldComboData)
     * @return
     * @Desc : works as gives res.size is 1 for local state or 0 for other state.
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject isStateMappedwithEntity(Map<String,Object> params) throws ServiceException{
        /**
         * Take record for which entity and state 'match' for JS side to auto-populate components in form.
         * components will be CGST, SGST and CESS Number field for record found,
         *  and components will be IGST and CESS for Record not found.
         */
        String defaultQuery = "SELECT fcdid FROM multientitydimesioncustomdata";
        List listData = new ArrayList();
        String conditions = " WHERE ";
        if(params.containsKey(Constants.entity)){
            conditions += " fcdid=? ";
            listData.add(params.get(Constants.entity));
        }
        if(params.containsKey(Constants.STATE)&&params.containsKey(Constants.column)){
            String column = (String)params.get(Constants.column);
            conditions += " and "+column+"=? ";
            listData.add(params.get(Constants.STATE));
        }
        String q = defaultQuery + conditions;
        List res = executeSQLQuery(q,listData.toArray());
        return new KwlReturnObject(true, q, "", res, res.size());
    }
    /**
     * ERP-32829
     *
     * @param reqParams
     * @return
     * @Desc : Get column number for any dimension
     * @throws ServiceException
     */
    public List getColumnNumberForDimension(JSONObject reqParams) throws ServiceException {
        List list = null;
        List params = new ArrayList();
        String condition = "";
        if (!StringUtil.isNullOrEmpty(reqParams.optString("fieldlabel"))) {
            condition += " fieldlabel=?";
            params.add(reqParams.optString("fieldlabel"));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("moduleid"))) {
            condition += " and moduleid=?";
            params.add(reqParams.optInt("moduleid"));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("companyid"))) {
            condition += " and companyid=?";
            params.add(reqParams.optString("companyid"));
        }
        String query = "select colnum from fieldparams where " + condition;
        list = executeSQLQuery(query, params.toArray());
        return list;
    }

    /**
     * ERP-32829
     *
     * @param reqMap
     * @return
     * @Desc : Save GST rule set up
     * @throws ServiceException
     */
    public KwlReturnObject saveGSTRuleSetup(Map<String, Object> reqMap) throws ServiceException {
        List list = new ArrayList();
        try {
            EntitybasedLineLevelTermRate entitybasedLineLevelTermRate = null;
            if (reqMap.containsKey("id") && reqMap.get("id") != null) {
                entitybasedLineLevelTermRate = (EntitybasedLineLevelTermRate) get(EntitybasedLineLevelTermRate.class, (String) reqMap.get("id"));
            } else {
                entitybasedLineLevelTermRate = new EntitybasedLineLevelTermRate();
            }
            if (reqMap.containsKey("term") && reqMap.get("term") != null) {
                LineLevelTerms levelTerms = (LineLevelTerms) get(LineLevelTerms.class, (String) reqMap.get("term"));
                entitybasedLineLevelTermRate.setLineLevelTerms(levelTerms);
            }
            if (reqMap.containsKey("entity") && reqMap.get("entity") != null) {
                FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("entity"));
                entitybasedLineLevelTermRate.setEntity(comboData);
            }
            if (reqMap.containsKey("shiplocation1") && reqMap.get("shiplocation1") != null) {
                FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("shiplocation1"));
                entitybasedLineLevelTermRate.setShippedLoc1(comboData);
            }
            if (reqMap.containsKey("shiplocation2") && reqMap.get("shiplocation2") != null) {
                FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("shiplocation2"));
                entitybasedLineLevelTermRate.setShippedLoc2(comboData);
            }
            if (reqMap.containsKey("shiplocation3") && reqMap.get("shiplocation3") != null) {
                FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("shiplocation3"));
                entitybasedLineLevelTermRate.setShippedLoc3(comboData);
            }
            if (reqMap.containsKey("shiplocation4") && reqMap.get("shiplocation4") != null) {
                FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("shiplocation4"));
                entitybasedLineLevelTermRate.setShippedLoc4(comboData);
            }
            if (reqMap.containsKey("shiplocation5") && reqMap.get("shiplocation5") != null) {
                FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("shiplocation5"));
                entitybasedLineLevelTermRate.setShippedLoc5(comboData);
            }
            if (reqMap.containsKey("applieddate") && reqMap.get("applieddate") != null) {
                entitybasedLineLevelTermRate.setAppliedDate((Date) reqMap.get("applieddate"));
            }
            if (reqMap.containsKey("percentage") && reqMap.get("percentage") != null) {
                entitybasedLineLevelTermRate.setPercentage((Double) reqMap.get("percentage"));
            }
            if (reqMap.containsKey("amount") && reqMap.get("amount") != null) {
                entitybasedLineLevelTermRate.setTermAmount((Double) reqMap.get("amount"));
            }
            if (reqMap.containsKey("type") && reqMap.get("type") != null) {
                entitybasedLineLevelTermRate.setTaxType((Integer) reqMap.get("type"));
            }
            /**
             * INDIA GST CESS Calculation Type Column data
             */
            if (reqMap.containsKey(IndiaComplianceConstants.GST_CESS_TYPE) && reqMap.get(IndiaComplianceConstants.GST_CESS_TYPE) != null && !StringUtil.isNullOrEmpty((String) reqMap.get(IndiaComplianceConstants.GST_CESS_TYPE))) {
                GSTCessRuleType cessRuleType = (GSTCessRuleType) get(GSTCessRuleType.class, (String) reqMap.get(IndiaComplianceConstants.GST_CESS_TYPE));
                entitybasedLineLevelTermRate.setCessType(cessRuleType);
            }
            if (reqMap.containsKey(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT) && reqMap.get(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT) != null) {
                entitybasedLineLevelTermRate.setValuationAmount((Double) reqMap.get(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT));
            }
            /**
             * Save IsMerchant Export in GST Rules 
             * only for IDNIA country
             */
            if (reqMap.containsKey(Constants.COUNTRY_ID) && reqMap.get(Constants.COUNTRY_ID) != null && !StringUtil.isNullOrEmpty(reqMap.get(Constants.COUNTRY_ID).toString())
                    && Integer.valueOf(reqMap.get(Constants.COUNTRY_ID).toString()) == Constants.indian_country_id) {
                if (reqMap.containsKey(Constants.isMerchantExporter) && reqMap.get(Constants.isMerchantExporter) != null) {
                    boolean isMerchantExporter = reqMap.containsKey(Constants.isMerchantExporter) && reqMap.get(Constants.isMerchantExporter) != null ? (Boolean) reqMap.get(Constants.isMerchantExporter) : false;
                    entitybasedLineLevelTermRate.setIsMerchantExporter(isMerchantExporter);
                }
            }
            saveOrUpdate(entitybasedLineLevelTermRate);

            list.add(entitybasedLineLevelTermRate);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    /**
     * @desc : Check if particular term can be deleted or not.
     * @param mapData (Map : term Id)
     * @return true (if term can be deleted) or False (if term Cannot be
     * deleted)
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject checkLineLevelTermUsed(Map<String, Object> mapData) throws ServiceException {
        List<Object> listData = new ArrayList<>();
        boolean isSuccess = true;
        String query = "select linelevelterms from entitybasedlineleveltermsrate WHERE linelevelterms=?";
        listData.add(mapData.get(Constants.termId).toString());
        List<Object[]> res = executeSQLQuery(query, listData.toArray());
        /**
         * (res.size()>0) Means Entry is Present in the table and cannot delete
         * particular rule.
         */
        if (res.size() > 0) {
            isSuccess = false;
        }
        return new KwlReturnObject(isSuccess, "", null, null, res.size());
    }
    /**
     * @Desc : Check if particular rule can be deleted or not.
     * @param mapData
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject checkDetailTermMapForDelete(Map<String, Object> mapData) throws ServiceException {
        List<Object> listData = new ArrayList<Object>();
        StringBuilder query = new StringBuilder();
        List<String> listTables = (List) mapData.get(Constants.tableName);
        String groupByUnion = " group by entityterm having count > 0 UNION ";
        String conditions = " WHERE ";
        StringBuilder defaultQuery = new StringBuilder();
        Map forModuleNames = (Map)mapData.get(Constants.modulename);
        boolean isGetDocumentNumber =mapData.containsKey("isGetDocumentNumber") && mapData.get("isGetDocumentNumber")!=null ? (Boolean)mapData.get("isGetDocumentNumber") : false;
        List<String> conditionsList = new ArrayList<String>();
        if (mapData.containsKey(Constants.id1)) {
            conditions += " entityterm=? ";
            conditionsList.add(mapData.get(Constants.id1).toString());
        }
        if (mapData.containsKey(Constants.id2)) {
            conditions += "OR entityterm=? ";
            conditionsList.add(mapData.get(Constants.id2).toString());
        }
        if (mapData.containsKey(Constants.id3)) {
            conditions += "OR entityterm=? ";
            conditionsList.add(mapData.get(Constants.id3).toString());
        }

        for (String tableName : listTables) {
            defaultQuery.setLength(0);
            if(!isGetDocumentNumber){
                defaultQuery.insert(0, "select count(entityterm) as count,? as moduleName from " + tableName + " ");
            } else{
                defaultQuery.insert(0, "select count(entityterm) as count,? as moduleName "
                        + " " + IndiaComplianceConstants.forModuleGroupConcat.get(tableName) + " from " + tableName + " " + IndiaComplianceConstants.forModuleJoins.get(tableName));
            }
            listData.add(forModuleNames.get(tableName));
            listData.addAll(conditionsList);
            query.append(defaultQuery.toString()).append(conditions).append(groupByUnion);
        }
        String q =  query.substring(0, query.lastIndexOf(" UNION "));
        List<Object[]> res = executeSQLQuery(q, listData.toArray());
        /**
         * (res.size()>0) Means Entry is Present in the table and cannot delete
         * particular rule.
         */
        if (res.size() > 0) {
            StringBuilder val = new StringBuilder();
            for(Object[] row : res){
                if(val.indexOf(row[1].toString())==-1){
                    val.append(row[1].toString()).append(", ");
                }
            }
            return new KwlReturnObject(false, val.substring(0,val.lastIndexOf(", ")), null, res, res.size());
        }
        return new KwlReturnObject(true, "", null, res, res.size());
    }
        /**
     * @desc : deletes the rule from pcgm and eltr.
     * @param mapData
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject deleteGSTRuleReportItem(Map<String, Object> mapData) throws ServiceException {
        List<Object> listData = new ArrayList<Object>();
        String query = "";
        String conditions = " WHERE ";
        String defaultQuery = "delete from prodcategorygstmapping ";

        if (mapData.containsKey("id1")) {
            conditions += " entitytermrate=? ";
            listData.add(mapData.get("id1").toString());
        }

        if (mapData.containsKey("id2")) {
            conditions += "OR entitytermrate=? ";
            listData.add(mapData.get("id2").toString());
        }
        if (mapData.containsKey("id3")) {
            conditions += "OR entitytermrate=? ";
            listData.add(mapData.get("id3").toString());
        }

        query = defaultQuery + conditions;

        int res = executeSQLUpdate(query, listData.toArray());
        /**
         * Delete from entitybasedlineleveltermsrate.
         */
        defaultQuery = "delete from entitybasedlineleveltermsrate ";
        conditions = " WHERE ";

        if (mapData.containsKey("id1")) {
            conditions += " id=? ";
        }

        if (mapData.containsKey("id2")) {
            conditions += "OR id=? ";
        }
        if (mapData.containsKey("id3")) {
            conditions += "OR id=? ";
        }

        query = defaultQuery + conditions;
        res = executeSQLUpdate(query, listData.toArray());
        /**
         * if res == 0 then Delete Operation did not affected, if res == 1 then
         * delete Operation successful.
         */
        if (res == 0) {
            return new KwlReturnObject(false, "This perticular Record is not deleted.", null, null, res);
        }
        return new KwlReturnObject(true, "Successfully Deleted Record.", null, null, res);
    }
    
    /**
     * @desc : deletes the rule from ProductTermsMap and LineLevelTerms.
     * @param mapData (Map : term Id)
     * @return true (for successfully deleted)
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject deleteLineLevelTerm(Map<String, Object> mapData) throws ServiceException {
        List<Object> listData = new ArrayList();
        boolean isSuccess = true;
        /**
         * Delete From Product Terms map table.
         */
        String query = "delete from producttermsmap WHERE term=?";
        listData.add(mapData.get(Constants.termId).toString());

        int res = executeSQLUpdate(query, listData.toArray());
        
        /**
         * Delete from LineLevelTerms table.
         */                
        query = "delete from linelevelterms WHERE id=?";
        res = executeSQLUpdate(query, listData.toArray());
        /**
         * if res == 0 then Delete Operation did not affected, if res == 1 then
         * delete Operation successful.
         */
        if (res == 0) {
            isSuccess = false;
        }
        return new KwlReturnObject(isSuccess, "Successfully Deleted Record.", null, null, res);
    }
    /**
     * ERP-32829
     *
     * @param reqMap
     * @return
     * @desc : save GST and Product category mapping
     * @throws ServiceException
     */
    public KwlReturnObject saveGSTRuleProdCategoryMapping(Map<String, Object> reqMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ProductCategoryGstRulesMappping categoryGstRulesMappping = null;
            if (reqMap.containsKey("id") && reqMap.get("id") != null) {
                categoryGstRulesMappping = (ProductCategoryGstRulesMappping) get(ProductCategoryGstRulesMappping.class, (String) reqMap.get("id"));
            } else {
                categoryGstRulesMappping = new ProductCategoryGstRulesMappping();
            }
            if (reqMap.containsKey("entityterm") && reqMap.get("entityterm") != null) {
                EntitybasedLineLevelTermRate levelTerms = (EntitybasedLineLevelTermRate) reqMap.get("entityterm");
                categoryGstRulesMappping.setEntitybasedLineLevelTermRate(levelTerms);
            }
            if (reqMap.containsKey("prodcategory") && reqMap.get("prodcategory") != null) {
                FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("prodcategory"));
                categoryGstRulesMappping.setProdCategory(comboData);
            }
            saveOrUpdate(categoryGstRulesMappping);
            list.add(categoryGstRulesMappping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    /**
     * ERP-32829
     *
     * @param nObject
     * @return
     * @Desc : Get GST Term information
     * @throws ServiceException
     */
    public KwlReturnObject getGSTTermDetails(JSONObject nObject) throws ServiceException {
        ArrayList params = new ArrayList();
        String company = nObject.optString("companyid");
        params.add(company);
        String condition = "";
        
        if (nObject.has("termname")) {
            condition += " AND term = ? ";
            params.add(nObject.optString("termname"));
        }
        if (nObject.has("termType")) {
            condition += " AND termType = ? ";
            params.add(nObject.optInt("termType"));
        }
        if (nObject.has("isInput")) {
            condition += " AND salesOrPurchase = ? ";
            params.add(nObject.optBoolean("isInput"));
        }
        
        String ss = nObject.optString(Constants.ss, "");
        if (!StringUtil.isNullOrEmpty(ss)) {
            String[] searchcol = new String[]{"term"};
            String searchQuery = StringUtil.getSearchquery(ss, searchcol, params);
            condition += searchQuery;
        }
        
        String query = " FROM LineLevelTerms WHERE company.companyID = ? " + condition;
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    /**
     * ERP-32829
     *
     * @Desc : Get Entity Rule set up data
     * @param nObject
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject getGSTEntityRuleTermDetails(JSONObject nObject) throws ServiceException {
        List params = new ArrayList();
        String company = nObject.optString("companyid");
        params.add(company);
        String condition = "";
        if (nObject.has("termname")) {
            condition += " and term=?";
            params.add(nObject.optString("termname"));
        }
        if (nObject.has("termType")) {
            condition += " and termType=?";
            params.add(nObject.optInt("termType"));
        }
        if (nObject.has("isInput")) {
            condition += " and salesOrPurchase=?";
            params.add(nObject.optBoolean("isInput"));
        }
        String query = " from LineLevelTerms where company.companyID = ? " + condition;
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    /**
     * ERP-32829
     *
     * @param mapData
     * @return
     * @Desc : Get GST rate for Product category OR based on Entity dimension
     * selection from Rules
     * @throws ServiceException
     */
    public KwlReturnObject getEntityBasedTermRate(Map<String, Object> mapData) throws ServiceException {
        List<Object> listData = new ArrayList<Object>();
        List<Object> dateData = new ArrayList<Object>();
        String productcategory = "", condtion = "";
        String datecondition="";
        boolean salesOrPurchase = false;
        boolean isAdditional = false;
        boolean isDefault = false;
        String defaultquery = "From EntitybasedLineLevelTermRate eltr ";
        if (mapData.containsKey("isProdCategoryPresent") && mapData.containsKey("productcategory")) {
            /**
             * If Product category present then fetch data using mapping table
             */
            boolean isProdCategoryPresent = (Boolean) mapData.get("isProdCategoryPresent");
            if (isProdCategoryPresent) {
                defaultquery = "select eltr From ProductCategoryGstRulesMappping pcgm inner join pcgm.entitybasedLineLevelTermRate eltr ";
                condtion += " where pcgm.prodCategory.id = ? ";
                productcategory = mapData.get("productcategory").toString();
                listData.add(productcategory);
            }
        }
        if (mapData.containsKey("salesOrPurchase")) {

            if (condtion.trim().length() > 1) {
                condtion += " and eltr.lineLevelTerms.salesOrPurchase= ? ";
            } else {
                condtion = " where eltr.lineLevelTerms.salesOrPurchase= ? ";
            }
            salesOrPurchase = Boolean.parseBoolean(mapData.get("salesOrPurchase").toString());
            listData.add(salesOrPurchase);

        }

        if (mapData.containsKey("entity")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.entity.id=? ";
            } else {
                condtion += " where eltr.entity.id=? ";
            }
            listData.add((String) mapData.get("entity"));
        }
        if (mapData.containsKey("todimension1")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.shippedLoc1.id=? ";
                datecondition += " and eltr1.shippedLoc1.id=? ";
            }
            listData.add((String) mapData.get("todimension1"));
            dateData.add((String) mapData.get("todimension1"));
        }
        if (mapData.containsKey("todimension2")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.shippedLoc2.id=? ";
                datecondition += " and eltr1.shippedLoc2.id=? ";
            }
            listData.add((String) mapData.get("todimension2"));
            dateData.add((String) mapData.get("todimension2"));
        }
        if (mapData.containsKey("todimension3")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.shippedLoc3.id=? ";
                datecondition += " and eltr1.shippedLoc3.id=? ";
            }
            listData.add((String) mapData.get("todimension3"));
            dateData.add((String) mapData.get("todimension3"));
        }
        if (mapData.containsKey("todimension4")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.shippedLoc4.id=? ";
                datecondition += " and eltr1.shippedLoc4.id=? ";
            }
            listData.add((String) mapData.get("todimension4"));
            dateData.add((String) mapData.get("todimension4"));
        }
        if (mapData.containsKey("todimension5")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.shippedLoc5.id=? ";
                datecondition += " and eltr1.shippedLoc5.id=? ";
            }
            listData.add((String) mapData.get("todimension5"));
            dateData.add((String) mapData.get("todimension5"));
        }
        /**
         * Get GST rules by isMerchantExporter check.
         * This check will true only for INDIA country only
         */
        if (mapData.containsKey(Constants.isMerchantExporter)) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.isMerchantExporter=? ";
                datecondition += " and eltr1.isMerchantExporter=? ";
            }
            listData.add((Boolean) mapData.get(Constants.isMerchantExporter));
            dateData.add((Boolean) mapData.get(Constants.isMerchantExporter));
        }
        if (mapData.containsKey("applieddate")) {
            if (condtion.trim().length() > 1) {
//                condtion += " and eltr.appliedDate in (select max(eltr1.appliedDate) from EntitybasedLineLevelTermRate eltr1 where eltr1.appliedDate<=?) ";
                condtion += " and eltr.appliedDate = (select max(eltr1.appliedDate) from ProductCategoryGstRulesMappping pcd1 inner join  "
                        + "pcd1.entitybasedLineLevelTermRate eltr1 where eltr1.appliedDate<=? and eltr1.id=pcd1.entitybasedLineLevelTermRate.id and eltr1.lineLevelTerms.company.companyID=? "
                        + "and eltr1.entity.id=? and pcd1.prodCategory.id = ? and eltr1.lineLevelTerms.salesOrPurchase= ? "
                        //+ " and eltr1.shippedLoc1.id=? "+datecondition+") ";
                        + datecondition + ") ";
            }
            listData.add((Date) mapData.get("applieddate"));
            listData.add((String) mapData.get("companyid"));
            listData.add((String) mapData.get("entity"));
            listData.add(productcategory);
            listData.add(salesOrPurchase);
            //listData.add((String) mapData.get("todimension1"));
            if(!StringUtil.isNullOrEmpty(datecondition)){
                listData.addAll(dateData);
            }
        }
        if (mapData.containsKey("defaulttermid")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.lineLevelTerms.defaultTerms.id= ? ";
            } else {
                condtion = " where eltr.lineLevelTerms.defaultTerms.id= ? ";
            }
            listData.add((String) mapData.get("defaulttermid"));
        }
        String orderby = "";
        if (mapData.containsKey("colnum")) {
            int colnum = (int) mapData.get("colnum");
            condtion += " group by eltr.id order by eltr.shippedLoc" + colnum;
        } else {
            condtion += " group by eltr.id order by eltr.lineLevelTerms.termSequence ASC ";
        }
        String q = defaultquery + condtion;
        List list = executeQuery(q, listData.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * ERP-32829
     *
     * @param mapData
     * @return
     * @Desc : Get GST rate for Product category OR based on Entity dimension
     * selection from Rules
     * @throws ServiceException
     */
    public KwlReturnObject getGroupByEntityBasedTermRate(Map<String, Object> mapData) throws ServiceException {
        List<Object> listData = new ArrayList<Object>();
        String productcategory = "", condtion = "";
        boolean salesOrPurchase = false;
        boolean isAdditional = false;
        boolean isDefault = false;
        String defaultquery = "select cast(group_concat(concat(eltr.linelevelterms,':',eltr.percentage) separator ',') as CHAR),"
                + "eltr.shippedloc1,eltr.shippedloc2,eltr.shippedloc3,eltr.shippedloc4,eltr.shippedloc5 "
                + "From entitybasedlineleveltermsrate eltr on pcgm.entitytermrate=eltr.id inner join linelevelterms lt"
                + " on lt.id=eltr.linelevelterms ";
        if (mapData.containsKey("isProdCategoryPresent") && mapData.containsKey("productcategory")) {
            /**
             * If Product category present then fetch data using mapping table
             */
            boolean isProdCategoryPresent = (Boolean) mapData.get("isProdCategoryPresent");
            if (isProdCategoryPresent) {
                defaultquery = "select cast(group_concat(concat(eltr.linelevelterms,':',eltr.percentage) separator ',') as CHAR),eltr.shippedloc1,eltr.shippedloc2,eltr.shippedloc3,eltr.shippedloc4,eltr.shippedloc5 From prodcategorygstmapping pcgm inner join entitybasedlineleveltermsrate eltr on pcgm.entitytermrate=eltr.id inner join linelevelterms lt on lt.id=eltr.linelevelterms ";
                condtion += " where pcgm.prodcategory= ? ";
                productcategory = mapData.get("productcategory").toString();
                listData.add(productcategory);
            }
        }
        if (mapData.containsKey("salesOrPurchase")) {

            if (condtion.trim().length() > 1) {
                condtion += " and lt.salesorpurchase= ? ";
            } else {
                condtion = " where lt.salesorpurchase= ? ";
            }
            salesOrPurchase = Boolean.parseBoolean(mapData.get("salesOrPurchase").toString());
            listData.add(salesOrPurchase);

        }

        if (mapData.containsKey("entity")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.entity=? ";
            } else {
                condtion += " where eltr.entity=? ";
            }
            listData.add((String) mapData.get("entity"));
        }

        if (mapData.containsKey("applieddate")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.applieddate = (select max(eltr1.applieddate) from entitybasedlineleveltermsrate eltr1 inner join "
                        + "  linelevelterms lt1 on lt1.id=eltr1.linelevelterms inner join prodcategorygstmapping pcd1 "
                        + "on pcd1.entitytermrate=eltr1.id  where eltr1.applieddate<=? and lt1.company=? and eltr1.entity=? and pcd1.prodcategory=? and lt1.salesorpurchase=?) ";
            }
            listData.add((Date) mapData.get("applieddate"));
            listData.add((String)mapData.get("companyid"));
            listData.add((String) mapData.get("entity"));
            listData.add(productcategory);
            listData.add(salesOrPurchase);
        }

        String q = defaultquery + condtion + " group by shippedLoc1,shippedLoc2,shippedLoc3,entity";
        List list = executeSQLQuery(q, listData.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * ERP-34607
     *
     * @param mapData
     * @return
     * @Desc : Get GST rate for Product category OR based on Entity dimension
     * selection from Rules
     * @throws ServiceException
     */
    public KwlReturnObject getGroupByAllEntityBasedTermRate(Map<String, Object> mapData) throws ServiceException {
        ArrayList<Object> listData = new ArrayList<Object>();
        String condition = "";
        boolean isSales = false;

        String conditionSql = "";

        /**
         * If Product category present then fetch data using mapping table
         */
        String defaultquery = "select cast(group_concat(concat(eltr.linelevelterms,':',eltr.percentage) separator ',') as CHAR),cast(group_concat(concat('',':',eltr.id) separator ',') as CHAR),eltr.shippedloc1,eltr.shippedloc2,eltr.shippedloc3,eltr.shippedloc4,eltr.shippedloc5,fcd.value,eltr.applieddate,"
                + " cast(group_concat(concat(eltr.cesstype,':',eltr.valuationamount) separator ',') as CHAR),"
                + " cast(group_concat(eltr.ismerchantexporter separator ',') as CHAR)  From prodcategorygstmapping pcgm "
                + "INNER JOIN entitybasedlineleveltermsrate eltr on pcgm.entitytermrate=eltr.id "
                + "INNER JOIN linelevelterms lt on lt.id=eltr.linelevelterms "
                + "INNER JOIN fieldcombodata fcd on fcd.id=pcgm.prodcategory "
                + "LEFT JOIN fieldcombodata fcd1 on fcd1.id=eltr.shippedloc1 " //These JOIN are to get 'Location_Name' For Search by String
                + "LEFT JOIN fieldcombodata fcd2 on fcd2.id=eltr.shippedloc2 "
                + "LEFT JOIN fieldcombodata fcd3 on fcd3.id=eltr.shippedloc3 "
                + "LEFT JOIN fieldcombodata fcd4 on fcd4.id=eltr.shippedloc4 "
                + "LEFT JOIN fieldcombodata fcd5 on fcd5.id=eltr.shippedloc5 ";
        /**
         * IF no GSt rule imported and also No product tax class entry the 
         * GST rule window grid not showing, Handle empty GST rule case
         */
        int productTaxClassCount = Integer.parseInt(mapData.get("productTaxClassCount").toString());
        if(productTaxClassCount>0){
            condition += " where pcgm.prodcategory IN (";
            for (int product = 1; product <= productTaxClassCount; product++) {
                condition += "?,";
                listData.add((mapData.get(("prod" + product)).toString()));
            }
            condition = condition.substring(0, condition.length() - 1) + ") ";
        }
        if (mapData.containsKey("isSales")) {
            if (condition.trim().length() > 1) {
                condition += " and lt.salesorpurchase= ? ";
            } else {
                condition = " where lt.salesorpurchase= ? ";
            }
            isSales = Boolean.parseBoolean(mapData.get("isSales").toString());
            listData.add(isSales);
        }

        if (mapData.containsKey("entity")) {
            if (condition.trim().length() > 1) {
                condition += " and eltr.entity=? ";
            } else {
                condition += " where eltr.entity=? ";
            }
            listData.add((String) mapData.get("entity"));
        }

        condition += " and (eltr.applieddate>=? and eltr.applieddate<= ?) ";
        listData.add((Date) mapData.get("startDate"));
        listData.add((Date) mapData.get("endDate"));

        String ss = mapData.get(Constants.ss) != null ? (mapData.get(Constants.ss).toString()) : "";
        if (!StringUtil.isNullOrEmpty(ss)) {
            String[] searchcol = new String[]{"fcd.value", "fcd1.value", "fcd2.value", "fcd3.value", "fcd4.value", "fcd5.value"};
            String searchQuery = StringUtil.getSearchquery(ss, searchcol, listData);
            condition += searchQuery;
        }

        String start = mapData.get("start").toString();
        String limit = mapData.get("limit").toString();
        if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
            conditionSql = " limit " + limit + " offset " + start;
        }
        String q = "";
        String q1 = "";
        /**
         * Added isMerchantExport check in group by to separate GST rules
         * 
         */
        q = defaultquery + condition + " group by pcgm.prodcategory,applieddate,shippedLoc1,shippedLoc2,shippedLoc3,entity,eltr.ismerchantexporter";
        q1 = defaultquery + condition + " group by pcgm.prodcategory,applieddate,shippedLoc1,shippedLoc2,shippedLoc3,entity,eltr.ismerchantexporter" + conditionSql;

        List list = executeSQLQuery(q, listData.toArray());
        int count = list.size();
        list = executeSQLQuery(q1, listData.toArray());
        return new KwlReturnObject(true, "", null, list, count);
    }

    /**
     * ERP-33784
     *
     * @param paramsObj
     * @return
     * @Desc Get Invoice Terms details for GST sales Tax Liability Report
     * @throws ServiceException
     */
    public KwlReturnObject getInvoicesForGSTSalesTaxLiabilityReport(JSONObject paramsObj) throws ServiceException, ParseException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            String condition = "";
            String joinString = "";
            
            String companyid = paramsObj.optString(Constants.companyKey, "");
            params.add(companyid);
            
            String ss = paramsObj.optString(Constants.ss, "");
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"llt.term"};
                String searchQuery = StringUtil.getSearchquery(ss, searchcol, params);
                condition += searchQuery;
            }
            
            if(paramsObj.has("taxid")) {
                condition += " AND llt.id = ? ";
                params.add(paramsObj.getString("taxid"));
            }
            
            String startDate = paramsObj.optString("startDate", "");
            String endDate = paramsObj.optString("endDate", "");
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                DateFormat df = (DateFormat) paramsObj.opt(Constants.df);
                condition += " AND (je.entrydate >= ? AND je.entrydate <= ?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            
            if (paramsObj.has("entityId") && !StringUtil.isNullOrEmpty(paramsObj.optString("entityId", ""))) {
                condition += " AND etr.entity = ? ";
                joinString += " INNER JOIN entitybasedlineleveltermsrate etr on etr.id = invtm.entityterm ";
                params.add(paramsObj.optString("entityId"));
            }
            
            String query = "SELECT inv.id AS 'InvoiceID', inv.invoicenumber AS 'InvoiceNumber', inv.invoiceamountinbase AS 'InvoiceAmount', prod.id AS 'ProductID', prod.productid AS 'ProductCode', prod.name AS 'ProductName', invd.rate AS 'UnitPrice', invtr.quantity AS Quantity, invtm.percentage AS 'TaxRate', invtm.termamount AS 'TaxAmount', inv.currency AS 'CurrencyID', inv.externalcurrencyrate AS 'ExchangeRate', inv.exchangeratedetail AS 'ExchangeRateDetailID', inv.createdon AS 'CreatedDate', invd.rowexcludinggstamountinbase AS taxableamount "
                    + " FROM invoice inv "
                    + " INNER JOIN invoicedetails invd ON invd.invoice = inv.id "
                    + " INNER JOIN journalentry je ON je.id = inv.journalentry "
                    + " INNER JOIN inventory invtr ON invtr.id = invd.id "
                    + " INNER JOIN product prod ON prod.id = invtr.product "
                    + " LEFT JOIN invoicedetailtermsmap invtm ON invtm.invoicedetail = invd.id "
                    + " LEFT JOIN linelevelterms llt ON llt.id = invtm.term "
                    + joinString
                    + " WHERE inv.company = ? AND inv.pendingapproval = 0 AND inv.deleteflag = 'F' "
                    + condition
                    + " ORDER BY inv.invoicenumber";
            
            list = executeSQLQuery(query, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
     /**
     * returns the array of total invoice term amount and total invoice amount of sales invoice from given date range
     * @param paramObj
     * @return invoiceTermTotal
     * @throws ServiceException 
     */
    public JSONObject getinvoiceTermAmtAndTotalAmtforGSTSalesTaxLiabilityReport(JSONObject paramObj) throws ServiceException {
        JSONObject invoiceTermAmtAndTotalAmt = new JSONObject();
        try {
            List<Double> list = new ArrayList();
            ArrayList params = new ArrayList();
            
            String companyid = paramObj.optString(Constants.companyKey, "");
            params.add(companyid);
            
            String condition = "";
            String startDate = paramObj.optString("startDate", "");
            String endDate = paramObj.optString("endDate", "");
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                DateFormat df = (DateFormat) paramObj.opt(Constants.df);
                condition += " AND (je.entrydate >= ? AND je.entrydate <= ?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            
            String joinString = "";
            if (paramObj.has("entityId") && !StringUtil.isNullOrEmpty(paramObj.optString("entityId", ""))) {
                condition += " AND etr.entity = ? ";
                joinString += " INNER JOIN entitybasedlineleveltermsrate etr ON etr.id = invtm.entityterm ";
                params.add(paramObj.optString("entityId"));
            }
            
            if(paramObj.has("taxid")) {
                condition += " AND llt.id = ? ";
                params.add(paramObj.getString("taxid"));
            }
            
            String InvTermAmtTotalQuery = "SELECT SUM(TermAmount) AS 'Term Amount' FROM ( "
                    + " SELECT IF(inv.externalcurrencyrate IS NULL || inv.externalcurrencyrate = 0 || inv.externalcurrencyrate = 1, term.termamount, (term.termamount/inv.externalcurrencyrate)) AS 'TermAmount' "
                    + " FROM invoice inv "
                    + " INNER JOIN invoicedetails invd ON invd.invoice = inv.id "
                    + " INNER JOIN journalentry je ON je.id = inv.journalentry "
                    + " INNER JOIN invoicetermsmap term ON term.invoice = inv.id "
                    + " INNER JOIN invoicedetailtermsmap invtm ON invtm.invoicedetail = invd.id "
                    + " INNER JOIN linelevelterms llt ON llt.id = invtm.term "
                    + joinString
                    + " WHERE inv.company = ? "
                    + condition
                    + " GROUP BY inv.invoicenumber "
                    + " ORDER BY inv.invoicenumber "
                    + " ) AS InvTermAmtTotal";
            
            list = executeSQLQuery(InvTermAmtTotalQuery, params.toArray());
            
            if(list.size() > 0 && list.get(0) != null) {
                invoiceTermAmtAndTotalAmt.put("InvoiceTermTotal", authHandler.round(list.get(0), companyid));
            }
            
            String totalInvoiceAmtQuery = "SELECT SUM(InvoiceAmount) FROM ( "
                    + " SELECT inv.invoiceamountinbase AS 'InvoiceAmount' "
                    + " FROM invoice inv "
                    + " INNER JOIN invoicedetails invd ON invd.invoice = inv.id "
                    + " INNER JOIN journalentry je ON je.id = inv.journalentry "
                    + " INNER JOIN inventory invtr ON invtr.id = invd.id "
                    + " INNER JOIN product prod ON prod.id = invtr.product "
                    + " LEFT JOIN invoicedetailtermsmap invtm ON invtm.invoicedetail = invd.id "
                    + " LEFT JOIN linelevelterms llt ON llt.id = invtm.term "
                    + joinString
                    + " WHERE inv.company = ? AND inv.pendingapproval = 0 AND inv.deleteflag = 'F' "
                    + condition
                    + " GROUP BY inv.id "
                    + " ) AS totalInvoiceAmt";
                    
            list = executeSQLQuery(totalInvoiceAmtQuery, params.toArray());
            
            if(list.size() > 0 && list.get(0) != null) {
                invoiceTermAmtAndTotalAmt.put("InvoiceAmountTotal", authHandler.round(list.get(0), companyid));
            }
            
        } catch (Exception ex) {
            Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return invoiceTermAmtAndTotalAmt;
    }
    
    /**
     *ERP-33784
     * @param paramsObj
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject getGSTSalesTaxLiabilityReportData(JSONObject paramsObj) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            String companyid = paramsObj.getString(Constants.companyKey);
            
            String condition = " WHERE inv.company = ?";
            params.add(companyid);
            
            String startDate = paramsObj.optString("startDate", null);
            String endDate = paramsObj.optString("endDate", null);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                DateFormat df = (DateFormat) paramsObj.opt(Constants.df);
                condition += " AND (je.entrydate >= ? AND je.entrydate <= ?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            
            if (!StringUtil.isNullOrEmpty(paramsObj.optString("entityId", ""))) {
                condition += " AND etr.entity = ? ";
                params.add(paramsObj.optString("entityId"));
            }
            
            String ss = paramsObj.optString(Constants.ss, "");
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"fploc1.fieldlabel", "fploc2.fieldlabel", "fploc3.fieldlabel", "fploc4.fieldlabel", "fploc5.fieldlabel", "loc1.value", "loc2.value", "loc3.value", "loc4.value", "loc5.value"};
                String searchQuery = StringUtil.getSearchquery(ss, searchcol, params);
                condition += searchQuery;
            }

            String groupby = " GROUP BY invoiceid";
            String orderby = " ORDER BY shippedloc1, shippedloc2, shippedloc3, shippedloc4, shippedloc5 ASC";

            String query = "SELECT shippedloc1, shippedloc1Name, shippedloc2, shippedloc2Name, shippedloc3, shippedloc3Name, shippedloc4, shippedloc4Name, shippedloc5, shippedloc5Name,"
                    + " invoiceid, invoicenumber,"
                    + " invoiceamount AS 'Total Sales',"
                    + " SUM(nontaxableamount) AS 'Non-Taxable Sales',"
                    + " SUM(taxableamount) AS 'Taxable Sales',"
                    + " IF(SUM(taxamount) IS NOT NULL, SUM(taxamount), 0) AS 'Tax Charged', "
                    + " CurrencyID, ExchangeRate, ExchangeRateDetailID, CreatedDate, InvoiceTermAmount "
                    + " FROM "
                    + "("
                    + " SELECT etr.shippedloc1, loc1.value AS shippedloc1Name, etr.shippedloc3, loc2.value AS shippedloc3Name, etr.shippedloc2, loc3.value AS shippedloc2Name, etr.shippedloc4, loc4.value AS shippedloc4Name, etr.shippedloc5, loc5.value AS shippedloc5Name,"
                    + " inv.id AS invoiceid, inv.invoicenumber, inv.invoiceamount AS invoiceamount,"
                    + " term.termamount AS InvoiceTermAmount,"
                    + " SUM(invtm.termamount) AS taxamount,"
                    + " IF(SUM(invtm.termamount) IS NULL OR SUM(invtm.termamount) = 0, 0, invd.rowexcludinggstamount) AS taxableamount,"
                    + " IF(invtm.termamount IS NULL OR invtm.termamount = 0, invd.rowexcludinggstamount, 0) AS nontaxableamount, "
                    + " inv.currency AS 'CurrencyID', inv.externalcurrencyrate AS 'ExchangeRate', inv.exchangeratedetail AS 'ExchangeRateDetailID', inv.createdon AS 'CreatedDate' "
                    + " FROM invoice inv"
                    + " INNER JOIN invoicedetails invd ON invd.invoice = inv.id"
                    + " INNER JOIN journalentry je ON je.id = inv.journalentry"
                    + " LEFT JOIN invoicetermsmap term ON term.invoice = inv.id"
                    + " LEFT JOIN invoicedetailtermsmap invtm ON invd.id = invtm.invoicedetail"
                    + " LEFT JOIN entitybasedlineleveltermsrate etr ON etr.id = invtm.entityterm"
                    + " LEFT JOIN fieldcombodata loc1 ON loc1.id = etr.shippedloc1"
                    + " LEFT JOIN fieldcombodata loc2 ON loc2.id = etr.shippedloc2"
                    + " LEFT JOIN fieldcombodata loc3 ON loc3.id = etr.shippedloc3"
                    + " LEFT JOIN fieldcombodata loc4 ON loc4.id = etr.shippedloc4"
                    + " LEFT JOIN fieldcombodata loc5 ON loc5.id = etr.shippedloc5"
                    + " LEFT JOIN fieldparams fploc1 ON fploc1.id = loc1.fieldid"
                    + " LEFT JOIN fieldparams fploc2 ON fploc2.id = loc2.fieldid"
                    + " LEFT JOIN fieldparams fploc3 ON fploc3.id = loc3.fieldid"
                    + " LEFT JOIN fieldparams fploc4 ON fploc4.id = loc4.fieldid"
                    + " LEFT JOIN fieldparams fploc5 ON fploc5.id = loc5.fieldid"
                    + condition
                    + " GROUP BY invd.id ORDER BY taxamount DESC"
                    + ") AS GSTSalesTaxLiabilityReport "
                    + groupby + orderby;
            
            list = executeSQLQuery(query, params.toArray());
            
        } catch (Exception ex) {
            Logger.getLogger(AccEntityGstController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    /**
     * ERP-32829
     *
     * @param params
     * @return
     * @Desc : Get dimension value for product
     * @throws ServiceException
     */
    public List getDimensionValueTaggedtoProd(JSONObject params) throws ServiceException {
        String productid = params.optString("productid");
        String colnum = params.optString("colnum");
        String query = "select col" + colnum + " from accproductcustomdata where productId=?";
        List list = executeSQLQuery(query, new Object[]{productid});
        return list;
    }
     /**
     * Function to get Product tax class assign to product based on date
     * @param reqParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     * @throws ParseException 
     */
    @Override
    public JSONObject getProductTaxClassOnDate(JSONObject reqParams) throws JSONException, ServiceException, ParseException {
        boolean isFixedAsset=reqParams.optBoolean("isFixedAsset");
        reqParams.put("moduleid", isFixedAsset?Constants.Acc_FixedAssets_AssetsGroups_ModuleId:Constants.Acc_Product_Master_ModuleId);
        reqParams.put("fieldlabel", Constants.GSTProdCategory);
        /*
         Select product tax class and field-combo data value type (i.e.1,2..etc) on selected date 
         */
        List li = getProductTaxClassOnDateSQL(reqParams);
        if (li != null && !li.isEmpty() && li.get(0) != null) {
            for (Object object : li) {
                Object[] data = (Object[]) object;
                reqParams.put("productcategory", (String) data[0]);
                reqParams.put("assetproductcategory", (String) data[0]);
                reqParams.put("valuetype", data[1]);
                /**
                 * for Asset Need to get Asset specific id from FCD
                 */
                if (isFixedAsset) {
                    reqParams.put("fcdid", (String) data[0]);
                    reqParams.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                    li = getAssetSpecificCategoryIdFromFCD(reqParams);
                    if (!li.isEmpty() && data[0] != null) {
                        reqParams.put("productcategory", (String) li.get(0));
                    }
                }
            }
        }
        return reqParams;
    }
    /**
     * Function to get product tax class assign to product based on transaction date.
     * @param params
     * @return
     * @throws ServiceException
     * @throws ParseException 
     */
    private List getProductTaxClassOnDateSQL(JSONObject params) throws ServiceException, ParseException {
        ArrayList al = new ArrayList();
        DateFormat df = null;
        if (params.opt(Constants.df) != null) {
            df = (DateFormat) params.opt(Constants.df);
        }
        String productid = params.optString("productid");
        String fieldlabel = params.optString("fieldlabel");
        String companyid = params.optString("companyid");
        int moduleid = params.optInt("moduleid");
        al.add(fieldlabel);
        al.add(moduleid);
        al.add(companyid);
        al.add(productid);
        al.add(fieldlabel);
        al.add(moduleid);
        al.add(companyid);
        al.add(productid);
        al.add(df.parse(params.optString("applieddate")));
        String query = "select pch.value,fcd.valuetype from productcustomfieldhistory pch inner join fieldparams fp on fp.id=pch.fieldparams inner join fieldcombodata fcd on fcd.id=pch.value "
                + " where fp.fieldlabel=? and fp.moduleid=? and fp.companyid=? and pch.product=? "
                + "and pch.applydate=(select max(pch1.applydate) from productcustomfieldhistory pch1 inner join fieldparams fp1 on fp1.id=pch1.fieldparams "
                + "where "
                + "fp1.fieldlabel=? and fp1.moduleid=? and fp1.companyid=? and pch1.product=? and pch1.applydate<=?)";
        List list = executeSQLQuery(query, al.toArray());
        return list;
    }
    
    /**
     * @Desc : Get Invoice data for GSTR1 Report
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public KwlReturnObject getInvoiceData(JSONObject params) throws ServiceException, JSONException {
        List list = new ArrayList();
        DateFormat df = null;
        if (params.opt(Constants.df) != null) {
            df = (DateFormat) params.opt(Constants.df);
        }
        list.add(params.optString("companyid"));
        String condition = " where inv.company.companyID=? ";
        if (params.has("isGSTINnull")) {
            boolean isGSTINnull = params.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                condition += " and inv.customer.GSTIN ='' ";
            } else {
                condition += " and inv.customer.GSTIN <>'' ";
//                condition += " and c.GSTN IS NOT NULL ";
            }
        }
        if (params.has("entitycolnum")) {
            int colnum = params.optInt("entitycolnum");
            condition += " and je.accBillInvCustomData.col" + colnum + "=?";
            list.add(params.optString("entityValue"));
        }
        if (params.has("customerid")) {
            condition += " and inv.customer.ID=?";
            list.add(params.optString("customerid"));
        }
        if (params.has("isRevised")) {
            boolean isRevised = params.optBoolean("isRevised");
            if (isRevised) {
                condition += " and inv.isSupplementary=1";
            } else {
                condition += " and inv.isSupplementary=0";
            }
        }
        if (params.has("startdate") && df != null) {
            try {
                if (condition.trim().length() > 1) {
                    condition += " and (je.entryDate>=? and je.entryDate<=?)";
                }
                list.add(df.parse(params.optString("startdate")));
                list.add(df.parse(params.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (params.has("greaterlimit") && params.has("limitamount")) {
            if (params.optBoolean("greaterlimit")) {
                double limit = params.optDouble("limitamount");
                condition += " and inv.invoiceamount>?";
                list.add(limit);
            } else {
                double limit = params.optDouble("limitamount");
                condition += " and inv.invoiceamount<=?";
                list.add(limit);
            }
        }
        List returnList = null;
        if (params.optBoolean("isReturnCustomer", false)) {
            String query = " select distinct c from Invoice inv "
                    + "inner join inv.journalEntry je  inner join inv.customer c " + condition;
            returnList = executeQuery(query, list.toArray());
        } else {
            String query = " select inv from Invoice inv "
                    + "inner join inv.journalEntry je  " + condition;
            returnList = executeQuery(query, list.toArray());
        }

        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    /**
     *
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public KwlReturnObject getInvoiceDetailData(JSONObject params) throws ServiceException, JSONException {
        List list = new ArrayList();
        int colnum=params.optInt("hsncolnum");
        String colcondition="p.productCustomData.col"+colnum;
        list.add(params.optString("companyid"));
        String join = "";
        String condition = " where invd.company.companyID=? and "+colcondition+" <>'' ";
        String groupby = "";
        DateFormat df = null;
        if (params.opt(Constants.df) != null) {
            df = (DateFormat) params.opt(Constants.df);
        }
        if (params.has("invoiceid")) {
            condition += " and invd.invoice.ID=? ";
            list.add(params.optString("invoiceid"));
        }
        if (params.has("aggregateinvoicedata") && params.optBoolean("aggregateinvoicedata")) {
            join += " inner join invd.invoice inv inner join inv.customer c ";
            if (params.has("greaterlimit") && params.has("limitamount")) {
                if (params.optBoolean("greaterlimit")) {
                    double limit = params.optDouble("limitamount");
                    condition += " and inv.invoiceamount>?";
                    list.add(limit);
                } else {
                    double limit = params.optDouble("limitamount");
                    condition += " and inv.invoiceamount<=?";
                    list.add(limit);
                }
            }
            if (params.has("isRevised")) {
                boolean isRevised = params.optBoolean("isRevised");
                if (isRevised) {
                    condition += " and inv.isSupplementary=1";
                } else {
                    condition += " and inv.isSupplementary=0";
                }
            }
            if (params.has("startdate") && df != null) {
                try {
                    if (condition.trim().length() > 1) {
//                        condition += " and (inv.journalEntry.entryDate>=? and inv.journalEntry.entryDate<=?)";
                        condition += " and (inv.creationDate>=? and inv.creationDate<=?)";
                    }
                    list.add(df.parse(params.optString("startdate")));
                    list.add(df.parse(params.optString("enddate")));
                } catch (ParseException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (params.has("isGSTINnull")) {
                boolean isGSTINnull = params.optBoolean("isGSTINnull");
                if (isGSTINnull) {
                    condition += " and inv.customer.GSTIN ='' ";
                } else {
                    condition += " and inv.customer.GSTIN <>'' ";
                }
            }
            groupby += " Group by "+colcondition;
        }

        String query = " select invd from InvoiceDetail invd inner join invd.inventory invt "
                + "inner join invt.product p " + join + condition + "  order by "+colcondition;
        List returnList = executeQuery(query, list.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    /**
     * @Desc : Get Shipped Location wise Invoice details
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public KwlReturnObject getLocationwiseInvoiceDetailsForProduct(JSONObject params) throws ServiceException, JSONException {
        List list = new ArrayList();
        int colnum=params.optInt("hsncolnum");
        String colcondition="invd.inventory.product.productCustomData.col"+colnum;
        list.add(params.optString("companyid"));
        list.add(params.optString("HSNCode"));
        String join = "";
        DateFormat df = null;
        if (params.opt(Constants.df) != null) {
            df = (DateFormat) params.opt(Constants.df);
        }
        String condition = " where invd.company.companyID=? and "+colcondition+"=? ";
        if (params.has("aggregateinvoicedata") && params.optBoolean("aggregateinvoicedata")) {
            join += " inner join invd.invoice inv inner join inv.customer c ";
            if (params.has("greaterlimit") && params.has("limitamount")) {
                if (params.optBoolean("greaterlimit")) {
                    double limit = params.optDouble("limitamount");
                    condition += " and inv.invoiceamount>?";
                    list.add(limit);
                } else {
                    double limit = params.optDouble("limitamount");
                    condition += " and inv.invoiceamount<=?";
                    list.add(limit);
                }
            }
            if (params.has("isRevised")) {
                boolean isRevised = params.optBoolean("isRevised");
                if (isRevised) {
                    condition += " and inv.isSupplementary=1";
                } else {
                    condition += " and inv.isSupplementary=0";
                }
            }
            if (params.has("startdate") && df != null) {
                try {
                    if (condition.trim().length() > 1) {
//                        condition += " and (inv.journalEntry.entryDate>=? and inv.journalEntry.entryDate<=?)";
                        condition += " and (inv.creationDate>=? and inv.creationDate<=?)";
                    }
                    list.add(df.parse(params.optString("startdate")));
                    list.add(df.parse(params.optString("enddate")));
                } catch (ParseException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (params.has("isGSTINnull")) {
                boolean isGSTINnull = params.optBoolean("isGSTINnull");
                if (isGSTINnull) {
                    condition += " and inv.customer.GSTIN ='' ";
                } else {
                    condition += " and inv.customer.GSTIN <>'' ";
                }
            }
        }
        String query = " select  idtm from InvoiceDetailTermsMap idtm inner join  idtm.invoicedetail invd"
                +join+condition+ "  and idtm.entitybasedLineLevelTermRate IS NOT NULL group by invd.ID order by idtm.entitybasedLineLevelTermRate.shippedLoc1.id";
//        + " where invd.inventory.product.HSNCode=? and idtm.entitybasedLineLevelTermRate IS NOT NULL order by idtm.entitybasedLineLevelTermRate.shippedLoc1.id group by invd.ID";
        List returnList = executeQuery(query, list.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    /**
     * @Desc : Get Invoice terms map
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public KwlReturnObject getGSTDetailsForInvoice(JSONObject params) throws ServiceException, JSONException {
        List list = new ArrayList();
        list.add(params.optString("invoicedetail"));
        String query = " From InvoiceDetailTermsMap tdm where tdm.invoicedetail.ID=?";
        List returnList = executeQuery(query, list.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    /**
     * @Desc : Get Sales Return term data
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public KwlReturnObject getGSTDetailsForReturn(JSONObject params) throws ServiceException, JSONException {
        List list = new ArrayList();
        list.add(params.optString("srdetail"));
        String query = " From SalesReturnDetailsTermMap srtdm where srtdm.salesreturndetail.ID=?";
        List returnList = executeQuery(query, list.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    /**
     * @Dsec : Get credit note details for GSTR1 report
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public KwlReturnObject getCreditNoteDetailsForGSTR1(JSONObject params) throws ServiceException, JSONException {
        List list = new ArrayList();
        list.add(params.optString("companyid"));
        String condition = " where cid.company.companyID=?  ";
        DateFormat df = null;
        if (params.opt(Constants.df) != null) {
            df = (DateFormat) params.opt(Constants.df);
        }
        if (params.has("startdate") && df != null) {
            try {
                if (condition.trim().length() > 1) {
//                    condition += " and (cn.journalEntry.entryDate>=? and cn.journalEntry.entryDate<=?)";
                    condition += " and (cn.creationDate>=? and cn.creationDate<=?)";
                }
                list.add(df.parse(params.optString("startdate")));
                list.add(df.parse(params.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (params.has("isGSTINnull")) {
            boolean isGSTINnull = params.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                condition += " and cn.customer.GSTIN ='' ";
            } else {
                condition += " and cn.customer.GSTIN <>'' ";
            }
        }
        String query = " select cnd from CreditNoteDetail cnd inner join cnd.creditNote cn"
                + " inner join cn.salesReturn sr inner join sr.rows srd"
                + " inner join srd.cidetails cid "+condition;
        List returnList = executeQuery(query, list.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());

    }
    /**
     * @Desc : Convert Asset to Product custom data
     * @param reqParams
     * @return
     * @throws ServiceException
     */
    public List getAssetSpecificCategoryIdFromFCD(JSONObject reqParams) throws ServiceException {
        List list = null;
        List params = new ArrayList();
        String condition = "";
        if (!StringUtil.isNullOrEmpty(reqParams.optString("fcdid"))) {
            params.add(reqParams.optString("fcdid"));
        }
        String queryforfcdid = "select value from fieldcombodata where id=?";
        list = executeSQLQuery(queryforfcdid, params.toArray());
        params = new ArrayList();
        if (!list.isEmpty() && list.get(0) != null) {
            params.add((String) list.get(0));
        }

        if (!StringUtil.isNullOrEmpty(reqParams.optString("fieldlabel"))) {
            params.add(reqParams.optString("fieldlabel"));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("companyid"))) {
            params.add(reqParams.optString("companyid"));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("moduleid"))) {
            params.add(reqParams.optInt("moduleid"));
        }
        String query = "select fcd.id from FieldComboData fcd where  fcd.value=? and fcd.field.fieldlabel=? and fcd.field.companyid=? and fcd.field.moduleid=?";
        list = executeQuery(query, params.toArray());
        return list;
    }
    /**
     * @Desc : Check whether rule already exist for entity or not
     * @param reqMap
     * @return
     * @throws ServiceException
     */
    public List getRuleIdInEditCase(Map<String, Object> reqMap) throws ServiceException {
        List params = new ArrayList();
        String condition = "";
        String subquery = "";
        if (reqMap.containsKey("prodcategory") && reqMap.get("prodcategory") != null) {
            FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("prodcategory"));
            if (comboData != null) {
                subquery = " select etr.id from ProductCategoryGstRulesMappping petr inner join petr.entitybasedLineLevelTermRate etr ";
                params.add(comboData.getId());
                condition += " petr.prodCategory.id=?";
            }
        } else {
            subquery = " select etr.id from EntitybasedLineLevelTermRate etr ";
        }
        if (reqMap.containsKey("term") && reqMap.get("term") != null) {
            LineLevelTerms levelTerms = (LineLevelTerms) get(LineLevelTerms.class, (String) reqMap.get("term"));
            params.add(levelTerms.getId());
            if (condition.length() > 0) {
                condition += " and etr.lineLevelTerms.id=?";
            } else {
                condition += " etr.lineLevelTerms.id=?";
            }

        }
        if (reqMap.containsKey("entity") && reqMap.get("entity") != null) {
            FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("entity"));
            if (comboData != null) {
                params.add(comboData.getId());
                condition += " and etr.entity.id=?";
            }
        }
        if (reqMap.containsKey("shiplocation1") && reqMap.get("shiplocation1") != null) {
            FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("shiplocation1"));
            if (comboData != null) {
                params.add(comboData.getId());
                condition += " and etr.shippedLoc1.id=?";
            }
        }
        if (reqMap.containsKey("shiplocation2") && reqMap.get("shiplocation2") != null) {
            FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("shiplocation2"));
            if (comboData != null) {
                params.add(comboData.getId());
                condition += " and etr.shippedLoc2.id=?";
            }
        }
        if (reqMap.containsKey("shiplocation3") && reqMap.get("shiplocation3") != null) {
            FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("shiplocation3"));
            if (comboData != null) {
                params.add(comboData.getId());
                condition += " and etr.shippedLoc3.id=?";
            }
        }
        if (reqMap.containsKey("shiplocation4") && reqMap.get("shiplocation4") != null) {
            FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("shiplocation4"));
            if (comboData != null) {
                params.add(comboData.getId());
                condition += " and etr.shippedLoc4.id=?";
            }
        }
        if (reqMap.containsKey("shiplocation5") && reqMap.get("shiplocation5") != null) {
            FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String) reqMap.get("shiplocation5"));
            if (comboData != null) {
                params.add(comboData.getId());
                condition += " and etr.shippedLoc5.id=?";
            }
        }
        if (reqMap.containsKey("type") && reqMap.get("type") != null) {
            params.add((Integer) reqMap.get("type"));
            condition += " and etr.taxType=?";
        }
        if (reqMap.containsKey("applieddate")) {
            condition += " and etr.appliedDate= ?";
            params.add((Date) reqMap.get("applieddate"));
        }
        /**
         * While checking GST rule in Add/Edit/ Import rule.
         * if this is true then check true value rule is present or not
         */
        if (reqMap.containsKey(Constants.COUNTRY_ID) && reqMap.get(Constants.COUNTRY_ID) != null && !StringUtil.isNullOrEmpty(reqMap.get(Constants.COUNTRY_ID).toString())
                 && Integer.valueOf(reqMap.get(Constants.COUNTRY_ID).toString())==Constants.indian_country_id) {
            boolean isMerchantExporter = reqMap.containsKey(Constants.isMerchantExporter) && reqMap.get(Constants.isMerchantExporter) != null ? (Boolean) reqMap.get(Constants.isMerchantExporter) : false;
            condition += " and etr.isMerchantExporter= ?";
            params.add(isMerchantExporter);
        }
        
        String query = subquery + " where " + condition;
        List list = executeQuery(query, params.toArray());
        return list;
    }
    /**
     * @Desc : Get Product Class Value i.e. HSN
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getProductClassValue(JSONObject params) throws ServiceException, JSONException {
        List list = new ArrayList();
        int colnum = params.optInt("hsncolnum");
        list.add(params.optString("productidforclass"));
        String query = " select fcd.value,fcd.id from fieldcombodata fcd inner join accproductcustomdata pcd on fcd.id=pcd.col" + colnum + " where pcd.productId=?";
        List returnList = executeSQLQuery(query, list.toArray());
        return returnList;
    }
    public List getStatesFromFCD(JSONObject jSONObject) throws ServiceException{
        List list = new ArrayList();
        list.add("State");
        list.add(1200);
        list.add(jSONObject.optString("companyid"));
        /**
         * Don't take empty result for combodata value
         */
        String query = " select fcd.value from fieldcombodata fcd where fcd.fieldid in (select fp.id from fieldparams fp where fp.fieldlabel=? and fp.moduleid=? and fp.companyid=?) and fcd.value!='' and fcd.value is not null ";
        List returnList = executeSQLQuery(query, list.toArray());
        return returnList;
    }
    /**
     * @Desc : Get Invoice and its related all detail table data in bulk for
     * GSTR1 reporting sections are B2B,B2CL,B2CS
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getInvoiceDataWithDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        String having ="";
        String orderby = "";
        String additionalSelectCol="";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }

        builder.append(" where inv.company=? and inv.deleteflag='F' and inv.isdraft = false and inv.approvestatuslevel = 11 and inv.istemplate!=2 ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is Unregistered i.e. GSTIN is not present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype ";

            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("entitycolnum") && reqParams.has(GSTRConstants.ASSET_SALES_INVOICE_ENTITYCOLUMN) && reqParams.has(GSTRConstants.Lease_SALES_INVOICE_ENTITYCOLUMN)) {
            /**
             * Column no of Entity dimension's value stored in Invoice
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            int assetColnum = reqParams.optInt(GSTRConstants.ASSET_SALES_INVOICE_ENTITYCOLUMN);
            int leaseColumn = reqParams.optInt(GSTRConstants.Lease_SALES_INVOICE_ENTITYCOLUMN);
            builder.append(" and (jecust.col" + colnum + "=? or jecust.col" + assetColnum + "=? " + " or jecust.col" + leaseColumn + "=? )");
            params.add(reqParams.optString("entityValue"));
            params.add(reqParams.optString(GSTRConstants.ASSET_SALES_INVOICE_ENTIYVALUE));
             params.add(reqParams.optString(GSTRConstants.Lease_SALES_INVOICE_ENTIYVALUE));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.has("greaterlimit") && reqParams.has("limitamount")) {
            /**
             * Invoice taxable value limit
             */
            if (reqParams.optBoolean("greaterlimit")) {
                /**
                 * Invoice Amount greater than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and inv.invoiceamountinbase>? ");
                params.add(limit);
            } else {
                /**
                 * Invoice Amount less than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and inv.invoiceamountinbase<=? ");
                params.add(limit);
            }
        }
        /*
          Flag for GST-calculation is based on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping= true;
        }
        String billingShippingPOS = (isShipping==false)?"bst.billingstate":"bst.shippingstate" ;
//        if (false && reqParams.has("salesstatecolnum") && (reqParams.has("interstate") || reqParams.has("intrastate"))) {
//            int statecolnum = reqParams.optInt("salesstatecolnum");
//            joinSql += " inner join fieldcombodata stfcd on jecust.col" + statecolnum + "=stfcd.id ";
//            String localState = reqParams.optString("localState");
//            params.add(localState);
//            if (reqParams.optBoolean("intrastate")) {
//                builder.append(" and stfcd.value=?");
//            } else {
//                builder.append(" and stfcd.value!=?");
//            }
//        } else {
            String localState = (!StringUtil.isNullOrEmpty(reqParams.optString("localState")) ? reqParams.optString("localState") : "");

            if (reqParams.has("interstate")) {
                /**
                 * Return only Inter state i.e. IGST records
                 */
                 builder.append(" and ").append(billingShippingPOS).append("<>? ");
                params.add(localState);
            } else if (reqParams.has("intrastate")) {
                /**
                 * Return only Intra state i.e. CGST and SGST records
                 */
                builder.append(" and ").append(billingShippingPOS).append("=? ");
                params.add(localState);
            }
//        }

        boolean isRefund = reqParams.optBoolean("isSalesRefund", false);
        boolean isdebitnote = reqParams.optBoolean("isdebitnote", false);
        if (reqParams.has("isb2cs")) {
            /**
             * a) Intra-State: any value b) Inter-State: Invoice value Rs 2.5
             * lakh or less
             */
            double limit = reqParams.optDouble("limitamount");
            builder.append(" and ((" + billingShippingPOS + "<>? and inv.invoiceamountinbase<=? )or (" + billingShippingPOS + "=?))");
//            builder.append(" and ((lt.defaultterms=? and inv.invoiceamountinbase<=? )or (lt.defaultterms=? or lt.defaultterms=? or lt.defaultterms=? or lt.defaultterms=?))");
            params.add(localState);
            params.add(limit);
            params.add(localState);


        }
        int hsncolnum = reqParams.optInt("hsncolnum");
        int taxclasscolnum = reqParams.optInt("taxclasscolnum");
        int assetHSNcolnum = reqParams.optInt(GSTRConstants.ASSET_HSNCOLUMN);
        int assetTaxclasscolnum = reqParams.optInt(GSTRConstants.ASSET_TAXCLASSCOLUMN);
        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            String Type = reqParams.optString("taxClassType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and  ptaxfcd.valuetype in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    } else {
                        conditionBuilder.append(",?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and ptaxfcd.valuetype=?");
                params.add((int) FieldComboData.ValueTypeMap.get(Type));
            }

        }
        if (reqParams.has("isRCMApplicable")) {
            /**
             * Append condition for RCM Applicable or not
             */
            builder.append(" and inv.rcmapplicable=?");
            boolean InvoiceType = reqParams.optBoolean("isRCMApplicable");
            if (InvoiceType) {
                params.add('T');
            } else {
                params.add('F');
            }
        }
        
        if(reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))){
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
         To perform Quick search by Inv.No or customer name
         */
        if(reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))){
            String searchString = reqParams.optString("ss");
            builder.append(" and ( inv.invoicenumber like '%").append(searchString).append("%' or c.name like '%").append(searchString).append("%')");
                }
        /**
         * No Of table with its aliases 
         * Invoice = inv 
         * Invoice Detail = invd
         * Journal entry = je 
         * Inventory = it
         * Product = p 
         * Invoice Detail term Map = ivtd 
         * EntitybasedLineLevelTermRate= eltr
         * LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String groupby = "";
        String selectCol = " c.id as customerid,c.gstin,inv.id as invoiceid,inv.invoicenumber,je.id as jeid,je.entrydate,"
                + "invd.id as invoicedetailid,IF(je.externalcurrencyrate>0,(invd.rate/je.externalcurrencyrate),invd.rate ),it.quantity,p.id as productid,lt.term,IF(je.externalcurrencyrate>0,(ivtd.termamount/je.externalcurrencyrate),ivtd.termamount),"
                + "inv.invoiceamountinbase,IFNULL(pfcd.value,pfcd1.value) as hsncode,"+ billingShippingPOS +" as pos,p.description,shl1.id as posid,"
                + "ivtd.percentage as taxrate,lt.defaultterms,d.inpercent as discPercentage,IF(d.inpercent='T', d.discount,IF(je.externalcurrencyrate>0,(d.discount/je.externalcurrencyrate),d.discount)) as discountValue,shl1.itemdescription as stcode,c.name as customerName";
        if (reqParams.has("excludeTermAmountForRCMInvoices") && reqParams.optBoolean("excludeTermAmountForRCMInvoices", false)) {
            /**
             * Don't show term amount for RCM sales invoices.
             */
            selectCol = " c.id as customerid,c.gstin,inv.id as invoiceid,inv.invoicenumber,je.id as jeid,je.entrydate,"
                    + "invd.id as invoicedetailid,IF(je.externalcurrencyrate>0,(invd.rate/je.externalcurrencyrate),invd.rate ),it.quantity,p.id as productid,lt.term,(case when inv.rcmapplicable = 'F' then (IF(je.externalcurrencyrate>0,(ivtd.termamount/je.externalcurrencyrate),ivtd.termamount)) else 0.0 end),"
                    + "inv.invoiceamountinbase,pfcd.value as hsncode,"+ billingShippingPOS +" as pos,p.description,shl1.id as posid,"
                    + "ivtd.percentage as taxrate,lt.defaultterms,d.inpercent as discPercentage,IF(d.inpercent='T', d.discount,IF(je.externalcurrencyrate>0,(d.discount/je.externalcurrencyrate),d.discount)) as discountValue,shl1.itemdescription as stcode,c.name as customerName";
        }
        if (reqParams.optBoolean("b2bCustVenType", false)) {
            selectCol += " ,mitm.value as gstregtype ";
            selectCol += " ,mstrItem.value as gstcusttype ";
        }

        if (reqParams.optBoolean("export", false)) {
            selectCol += " ,mstrItem.value as gstcusttype ";
            if (!StringUtil.isNullOrEmpty(reqParams.optString(Constants.GSTR1_SHIPPING_PORT + "colnum"))) {
                selectCol += " ,jecust.col" + reqParams.optString(Constants.GSTR1_SHIPPING_PORT + "colnum");
            }
            if (!StringUtil.isNullOrEmpty(reqParams.optString(Constants.GSTR1_SHIPPING_DATE + "colnum"))) {
                selectCol += " ,jecust.col" + reqParams.optString(Constants.GSTR1_SHIPPING_DATE + "colnum");
            }
            if (!StringUtil.isNullOrEmpty(reqParams.optString(Constants.GSTR1_SHIPPING_BILL_NO + "colnum"))) {
                selectCol += " ,jecust.col" + reqParams.optString(Constants.GSTR1_SHIPPING_BILL_NO + "colnum");
            }
        }

        if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
            /**
             * Used for GST3B report 
             * i.e. select sum of all Invoice taxable and Tax amount (Separate for each GST type i.e. CGST,IGST etc)
             */
            selectCol = "sum(IF(je.externalcurrencyrate>0,(ivtd.termamount/je.externalcurrencyrate),ivtd.termamount)),lt.defaultterms,sum((IF(je.externalcurrencyrate>0,(invd.rate/je.externalcurrencyrate),invd.rate ) * it.quantity ) - IF(je.externalcurrencyrate>0,(ifNull(case when d.inpercent='T' then (d.discount*(invd.rate*it.quantity)/100) else d.discount end,0)/je.externalcurrencyrate), ifNull(case when d.inpercent='T' then (d.discount*(invd.rate*it.quantity)/100) else d.discount end,0))), sum(inv.invoiceamountinbase),count(distinct inv.id)";
            if (reqParams.has("excludeTermAmountForRCMInvoices") && reqParams.optBoolean("excludeTermAmountForRCMInvoices", false)) {
                selectCol = "sum((case when inv.rcmapplicable = 'F' then (IF(je.externalcurrencyrate>0,(ivtd.termamount/je.externalcurrencyrate),ivtd.termamount)) else 0.0 end)),lt.defaultterms,sum((IF(je.externalcurrencyrate>0,(invd.rate/je.externalcurrencyrate),invd.rate ) * it.quantity ) - IF(je.externalcurrencyrate>0,(ifNull(case when d.inpercent='T' then (d.discount*(invd.rate*it.quantity)/100) else d.discount end,0)/je.externalcurrencyrate), ifNull(case when d.inpercent='T' then (d.discount*(invd.rate*it.quantity)/100) else d.discount end,0))), sum(inv.invoiceamountinbase),count(distinct inv.id)";
            }
            groupby = " group by lt.defaultterms ";

            /**
             * If request for POS wise details
             */
            if (reqParams.optBoolean("posdetails")) {
                selectCol += ",shl1.value,shl1.id";
                groupby = " group by shl1.id,lt.defaultterms ";
            }
        }
        if (reqParams.has("isDocumentDetails") && reqParams.optBoolean("isDocumentDetails")) {
            /**
             * Used for Document Details report
             *
             */
            selectCol = " inv.invoicenumber,inv.seqformat,inv.seqnumber";
            groupby += " group by inv.id ";
            joinSql += " inner join sequenceformat sq on inv.seqformat=sq.id ";
            orderby += " ORDER BY inv.seqformat,inv.seqnumber ";
        }
        if (reqParams.has("zerorated")) {
            if (reqParams.optBoolean("zerorated")) {
                /**
                 * Condition for such Invoice having GST rates Zero
                 */
                builder.append(" and ivtd.termamount=0");
            } else {
                /**
                 * Condition for such CN having GST rates Non Zero
                 */
                builder.append(" and ivtd.termamount>0");
            }
        }
        int Ecomcolnum = reqParams.optInt("ecom", 0);
        if (Ecomcolnum != 0) {
            /*
             For E-Commerce GSTIN number
             */
            if (reqParams.has("isb2cs")) {
                additionalSelectCol += " ,ecom.itemdescription as ecomgstin ";
            } else {
                selectCol += " ,ecom.itemdescription as ecomgstin ";
            }
            joinSql += " left join fieldcombodata ecom on ecom.id=jecust.col" + Ecomcolnum + "";

        }               
        String typeofjoin=" inner ";
        if(reqParams.has("typeofjoinisleft") && reqParams.optBoolean("typeofjoinisleft")){
            typeofjoin=" left ";
        }
        joinSql += " left join discount d on d.id = invd.discount ";

        String query = " select " + selectCol + additionalSelectCol+" from invoice inv inner join invoicedetails invd on inv.id=invd.invoice and inv.company=?"
                + " inner join gstdocumenthistory gdh on inv.id=gdh.refdocid and gdh.moduleid in ('2','38','93') "
                + " inner join gsttaxclasshistory gtch on invd.id=gtch.refdocid and gtch.moduleid in ('2','38','93') "
                + " inner join journalentry je on je.id=inv.journalentry "
                + " inner join customer c on c.id=inv.customer "
                + " inner join inventory it on it.id=invd.id "
                + " inner join product p on p.id=it.product "
                + typeofjoin+" join invoicedetailtermsmap ivtd on ivtd.invoicedetail=invd.id "
                + typeofjoin+"  join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + typeofjoin+"  join linelevelterms lt on lt.id=eltr.linelevelterms"
                + typeofjoin+"  join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 "
                + " inner join accproductcustomdata pcd on pcd.productId=p.id "
                + " left join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + ""
                + " left join fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass"             
                + " left join fieldcombodata pfcd1 on pfcd1.id=pcd.col" + assetHSNcolnum + ""
                + " inner join billingshippingaddresses bst on bst.id=inv.billingshippingaddresses "
//                + " left join fieldcombodata ptaxfcd1 on ptaxfcd1.id=pcd.col" + assetTaxclasscolnum + ""
                + " " + joinSql + builder+groupby+having+orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    
    /**
     * @Desc : method used to retrieve data from Credit Note created using sales
     * return with invoice
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getCNDNWithInvoiceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where sr.company=? and sr.deleteflag='F' and cn.approvestatuslevel=11 ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Unregistered i.e. GSTIN is present
                 */

                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("entitycolnum")) {
            /**
             * Column no of Entity Value stored in CN
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=cnje.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (cnje.entrydate>=? and cnje.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.has("greaterlimit") && reqParams.has("limitamount")) {
            /**
             * CN taxable value limit
             */
            if (reqParams.optBoolean("greaterlimit") && reqParams.optBoolean("exportwithoutlimit")) {
                /**
                 * CN Amount greater than limit i.e 2.5 Lac excluding export.
                 * No Amount limit for export type of CN
                 * type (No amount limit for Export type)
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and (cn.cnamountinbase>? or mstrItem.defaultmasteritem in (?,?)) ");
                params.add(limit);
                params.add(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Export).toString());
                params.add(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_ExportWOPAY).toString());
            } else if (reqParams.optBoolean("greaterlimit")) {
                /**
                 * CN Amount greater than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and cn.cnamountinbase>? ");
                params.add(limit);
            } else {
                /**
                 * CN Amount less than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and cn.cnamountinbase<=? ");
                params.add(limit);
            }
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype ";

            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            String Type = reqParams.optString("taxClassType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and  ptaxfcd.valuetype in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    } else {
                        conditionBuilder.append(",?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and ptaxfcd.valuetype=?");
                params.add((int) FieldComboData.ValueTypeMap.get(Type));
            }

        }
        /*
          Flag for GST-calculation is based on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping = true;
        }
        String billingShippingPOS = (isShipping == false) ? "bst.billingstate" : "bst.shippingState";

        String localState = (!StringUtil.isNullOrEmpty(reqParams.optString("localState")) ? reqParams.optString("localState") : "");

        if (reqParams.has("interstate")) {
            /**
             * Return only Inter state i.e. IGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("<>? ");
            params.add(localState);
        } else if (reqParams.has("intrastate")) {
            /**
             * Return only Intra state i.e. CGST and SGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("=? ");
            params.add(localState);
        }
        
        if (reqParams.optBoolean("isb2cs")) {
            /**
             * a) Intra-State: any value b) Inter-State: Invoice value Rs 2.5
             * lakh or less
             */
            double limit = reqParams.optDouble("limitamount");
            builder.append(" and ((" + billingShippingPOS + "<>? and cn.cnamountinbase<=? )or (" + billingShippingPOS + "=?))");
            params.add(localState);
            params.add(limit);
            params.add(localState);
        }
        int hsncolnum = reqParams.optInt("hsncolnum");

        if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
            To perform Quick search by Inv.No or customer name
        */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            builder.append(" and ( cn.cnnumber like '%").append(searchString).append("%' or c.name like '%").append(searchString).append("%')");
        }
        /**
         * No Of table with its aliases 
         * Invoice = inv 
         * Invoice Detail = invd
         * Sales Return Detail=srd
         * Credit Note = cn
         * Credit Note details = cnd
         * Journal entry = je 
         * Inventory = it
         * Product = p 
         * Invoice Detail term Map = ivtd 
         * EntitybasedLineLevelTermRate= eltr
         * LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String selectCol = "";
        if (reqParams.has("cdnur") && reqParams.optBoolean("cdnur")) {
            selectCol = "c.id as customerid,c.gstin,sr.id as invoiceid,inv.invoicenumber,cnje.id as jeid,inv.creationdate,"
                    + "srd.id as invoicedetailid,(IF(cnje.externalcurrencyrate>0,(srd.rate/cnje.externalcurrencyrate),srd.rate)),srd.returnquantity,p.id as productid,lt.term,(IF(cnje.externalcurrencyrate>0,(ivtd.termamount/cnje.externalcurrencyrate),ivtd.termamount)),"
                    + "cn.cnamountinbase,pfcd.value as hsncode,"+ billingShippingPOS +" as pos,p.description,shl1.id as posid,"
                    + "ivtd.percentage as taxrate,lt.defaultterms,cn.cnnumber,sr.orderdate,srd.discountispercent as discpercentage,"
                    + "IF(srd.discountispercent=1,srd.discount,IF(cnje.externalcurrencyrate>0,(srd.discount/cnje.externalcurrencyrate),srd.discount)) as discountvalue,cn.id as cnid,cn.cnamountinbase as cnamountinbase, shl1.itemdescription as stcode, c.name as customername,mstrItem.value as gstcusttype,'' ";
        } else if (reqParams.has("isb2cs") && reqParams.optBoolean("isb2cs")) {
            selectCol = "c.id as customerid,c.gstin,sr.id as invoiceid,inv.invoicenumber,cnje.id as jeid,inv.creationdate,"
                    + "srd.id as invoicedetailid,(IF(cnje.externalcurrencyrate>0,(srd.rate/cnje.externalcurrencyrate),srd.rate)),srd.returnquantity,p.id as productid,lt.term,(IF(cnje.externalcurrencyrate>0,(ivtd.termamount/cnje.externalcurrencyrate),ivtd.termamount)),"
                    + "cn.cnamountinbase,pfcd.value as hsncode,"+ billingShippingPOS +" as pos,p.description,shl1.id as posid,"
                    + "ivtd.percentage as taxrate,lt.defaultterms,cn.cnnumber,sr.orderdate,srd.discountispercent as discpercentage,"
                    + "IF(srd.discountispercent=1,srd.discount,IF(cnje.externalcurrencyrate>0,(srd.discount/cnje.externalcurrencyrate),srd.discount)) as discountvalue,cn.id as cnid,cn.cnamountinbase as cnamountinbase, shl1.itemdescription as stcode, c.name as customername,mstrItem.value as gstcusttype,'' ";
        } else {
            selectCol = "c.id as customerid,c.gstin,cn.id as invoiceid,inv.invoicenumber,cnje.id as jeid,inv.creationdate,"
                    + "srd.id as invoicedetailid,(IF(cnje.externalcurrencyrate>0,(srd.rate/cnje.externalcurrencyrate),srd.rate)),srd.returnquantity,p.id as productid,lt.term,(IF(cnje.externalcurrencyrate>0,(ivtd.termamount/cnje.externalcurrencyrate),ivtd.termamount)),"
                    + "cn.cnamountinbase,pfcd.value as hsncode," + billingShippingPOS + " as pos,p.description,shl1.id as posid,"
                    + "ivtd.percentage as taxrate,lt.defaultterms,cn.cnnumber,sr.orderdate,srd.discountispercent as discpercentage,IF(srd.discountispercent=1,srd.discount,IF(cnje.externalcurrencyrate>0,(srd.discount/cnje.externalcurrencyrate),srd.discount)) as discountvalue , shl1.itemdescription as stcode, c.name as customername,'' ";
        }
        String groupby = "",orderby="";
        if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
            /**
             * Used for GST3B report 
             * i.e. select sum of all CN taxable and Tax amount (Separate for each GST type i.e. CGST,IGST etc)
             *
             */
            selectCol = "sum(IF(je.externalcurrencyrate>0,(ivtd.termamount/je.externalcurrencyrate),ivtd.termamount)),lt.defaultterms,"
                    + "sum(((IF(je.externalcurrencyrate>0,(srd.rate/je.externalcurrencyrate),srd.rate))*srd.returnquantity)- IF(je.externalcurrencyrate>0,(case when srd.discountispercent=1 then (srd.discount*(srd.rate*srd.returnquantity)/100) else srd.discount end/je.externalcurrencyrate), case when srd.discountispercent=1 then (srd.discount*(srd.rate*srd.returnquantity)/100) else srd.discount end)) "
                    + ",sum(sr.totalamountinbase)";
            groupby = " group by lt.defaultterms ";

        }
        if (reqParams.has("isDocumentDetails") && reqParams.optBoolean("isDocumentDetails")) {
            /**
             * Used for Document Details report
             *
             */
            selectCol = " cn.cnnumber,cn.seqformat,cn.seqnumber";
            groupby += " group by cn.id ";
            joinSql += " inner join sequenceformat sq on cn.seqformat=sq.id ";
            orderby += " ORDER BY cn.seqformat,cn.seqnumber ";

        }
        int cnreasoncol = reqParams.optInt("cnreasoncol", 0);
        if (cnreasoncol != 0) {
            selectCol += " ,cnreasonfcd.value as cnreason ";
            joinSql += " left join fieldcombodata cnreasonfcd on cnreasonfcd.id=jecust.col" + cnreasoncol + "";

        }
        if (reqParams.has("zerorated")) {
            if (reqParams.optBoolean("zerorated")) {
                /**
                 * Condition for such CN having GST rates Zero
                 */
                builder.append(" and ivtd.termamount=0");
            } else {
                /**
                 * Condition for such CN having GST rates Zero
                 */
                builder.append(" and ivtd.termamount>0");
            }
        }       
        String typeofjoin = " inner ";
        if (reqParams.has("typeofjoinisleft") && reqParams.optBoolean("typeofjoinisleft")) {
            typeofjoin = " left ";
        }

        String query = " select " + selectCol + " from salesreturn sr "
                + " inner join srdetails srd on sr.id=srd.salesreturn and sr.company=?"
                + " inner join gstdocumenthistory gdh on sr.id=gdh.refdocid "
                + " inner join gsttaxclasshistory gtch on srd.id=gtch.refdocid "
                + " inner join creditnote cn on cn.salesreturn=sr.id "
                + " inner join journalentry cnje on cnje.id=cn.journalentry "
                + " inner join customer c on c.id=cn.customer "
                + " inner join product p on p.id=srd.product "
                + " left join invoicedetails invd on srd.cidetails=invd.id "
                + " left join invoice inv on inv.id=invd.invoice "
                + typeofjoin + " join salesreturndetailtermmap ivtd on ivtd.salesreturndetail=srd.id "
                + typeofjoin + " join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + typeofjoin + " join linelevelterms lt on lt.id=eltr.linelevelterms"
                + typeofjoin + " join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 "
                + typeofjoin + " join accproductcustomdata pcd on pcd.productId=p.id "
                + typeofjoin + " join fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass"
                + " inner join billingshippingaddresses bst on bst.id=cn.billingshippingaddresses "
                + typeofjoin + " join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + "" + joinSql + builder + groupby + orderby;

        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * Function to get data for Sales Return as Refund case by linking cash
     * sales
     *
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getCashRefundWithInvoiceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where sr.company=? and sr.deleteflag='F' and mp.approvestatuslevel=11 ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Unregistered i.e. GSTIN is present
                 */

                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("entitycolnum")) {
            /**
             * Column no of Entity Value stored in CN
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=mpje.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (mpje.entrydate>=? and mpje.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.has("greaterlimit") && reqParams.has("limitamount")) {
            /**
             * CN taxable value limit
             */
            if (reqParams.optBoolean("greaterlimit") && reqParams.optBoolean("exportwithoutlimit")) {
                /**
                 * CN Amount greater than limit i.e 2.5 Lac excluding export.
                 * No Amount limit for export type of CN
                 * type (No amount limit for Export type)
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and (mp.depositamountinbase>? or mstrItem.defaultmasteritem in (?,?)) ");
                params.add(limit);
                params.add(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Export).toString());
                params.add(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_ExportWOPAY).toString());
            } else if (reqParams.optBoolean("greaterlimit")) {
                /**
                 * CN Amount greater than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and mp.depositamountinbase>? ");
                params.add(limit);
            } else {
                /**
                 * CN Amount less than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and mp.depositamountinbase<=? ");
                params.add(limit);
            }
        }
        joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype ";
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            
            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            String Type = reqParams.optString("taxClassType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and  ptaxfcd.valuetype in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    } else {
                        conditionBuilder.append(",?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and ptaxfcd.valuetype=?");
                params.add((int) FieldComboData.ValueTypeMap.get(Type));
            }

        }
        /*
         Flag for GST-calculation is based on Billing Address or Shipping Address
         */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping = true;
        }
        /**
         * If Invoice is not linked then take address field from Customer.
         */
        String billingShippingPOS = (isShipping == false) ? "IFNULL(bst.billingstate,cad.state)" : "bst.shippingState";

        String localState = (!StringUtil.isNullOrEmpty(reqParams.optString("localState")) ? reqParams.optString("localState") : "");

        if (reqParams.has("interstate")) {
            /**
             * Return only Inter state i.e. IGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("<>? ");
            params.add(localState);
        } else if (reqParams.has("intrastate")) {
            /**
             * Return only Intra state i.e. CGST and SGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("=? ");
            params.add(localState);
        }
        if (reqParams.optBoolean("isb2cs")) {
            /**
             * a) Intra-State: any value b) Inter-State: Invoice value Rs 2.5
             * lakh or less
             */
            double limit = reqParams.optDouble("limitamount");
            builder.append(" and ((" + billingShippingPOS + "<>? and mp.depositamountinbase<=? )or (" + billingShippingPOS + "=?))");
            params.add(localState);
            params.add(limit);
            params.add(localState);
        }
        int hsncolnum = reqParams.optInt("hsncolnum");

        if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
         To perform Quick search by Inv.No or customer name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            builder.append(" and ( mp.paymentnumber like '%").append(searchString).append("%' or c.name like '%").append(searchString).append("%')");
        }
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Sales Return Detail=srd Credit Note = cn Credit Note details = cnd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String selectCol = "";
        if (reqParams.has("cdnur") && reqParams.optBoolean("cdnur")) {
            selectCol = "c.id as customerid,c.gstin,sr.id as invoiceid,inv.invoicenumber,mpje.id as jeid,inv.creationdate,"
                    + "srd.id as invoicedetailid,(IF(mpje.externalcurrencyrate>0,(srd.rate/mpje.externalcurrencyrate),srd.rate)),srd.returnquantity,p.id as productid,lt.term,(IF(mpje.externalcurrencyrate>0,(ivtd.termamount/mpje.externalcurrencyrate),ivtd.termamount)),"
                    + "mp.depositamountinbase,pfcd.value as hsncode," + billingShippingPOS + " as pos,p.description,shl1.id as posid,"
                    + "ivtd.percentage as taxrate,lt.defaultterms,mp.paymentnumber,sr.orderdate,srd.discountispercent as discpercentage,"
                    + "IF(srd.discountispercent=1,srd.discount,IF(mpje.externalcurrencyrate>0,(srd.discount/mpje.externalcurrencyrate),srd.discount)) as discountvalue,mp.id as mpid,mp.depositamountinbase as depositamountinbase, shl1.itemdescription as stcode, c.name as customername,mstrItem.value as gstcusttype,'R' as doctype ";
        } else if (reqParams.has("isb2cs") && reqParams.optBoolean("isb2cs")){
            selectCol = "c.id as customerid,c.gstin,sr.id as invoiceid,inv.invoicenumber,mpje.id as jeid,inv.creationdate,"
                    + "srd.id as invoicedetailid,(IF(mpje.externalcurrencyrate>0,(srd.rate/mpje.externalcurrencyrate),srd.rate)),srd.returnquantity,p.id as productid,lt.term,(IF(mpje.externalcurrencyrate>0,(ivtd.termamount/mpje.externalcurrencyrate),ivtd.termamount)),"
                    + "mp.depositamountinbase,pfcd.value as hsncode," + billingShippingPOS + " as pos,p.description,shl1.id as posid,"
                    + "ivtd.percentage as taxrate,lt.defaultterms,mp.paymentnumber,sr.orderdate,srd.discountispercent as discpercentage,"
                    + "IF(srd.discountispercent=1,srd.discount,IF(mpje.externalcurrencyrate>0,(srd.discount/mpje.externalcurrencyrate),srd.discount)) as discountvalue,mp.id as mpid,mp.depositamountinbase as depositamountinbase, shl1.itemdescription as stcode, c.name as customername,mstrItem.value as gstcusttype,'R' as doctype ";
        }else {
            selectCol = "c.id as customerid,c.gstin,sr.id as invoiceid,inv.invoicenumber,mpje.id as jeid,inv.creationdate,"
                    + "srd.id as invoicedetailid,(IF(mpje.externalcurrencyrate>0,(srd.rate/mpje.externalcurrencyrate),srd.rate)),srd.returnquantity,p.id as productid,lt.term,(IF(mpje.externalcurrencyrate>0,(ivtd.termamount/mpje.externalcurrencyrate),ivtd.termamount)),"
                    + "mp.depositamountinbase,pfcd.value as hsncode," + billingShippingPOS + " as pos,p.description,shl1.id as posid,"
                    + "ivtd.percentage as taxrate,lt.defaultterms,mp.paymentnumber,sr.orderdate,srd.discountispercent as discpercentage,IF(srd.discountispercent=1,srd.discount,IF(mpje.externalcurrencyrate>0,(srd.discount/mpje.externalcurrencyrate),srd.discount)) as discountvalue , shl1.itemdescription as stcode, c.name as customername,'R' as doctype ";
        }
        String groupby = "", orderby = "";
//
        if (reqParams.has("isDocumentDetails") && reqParams.optBoolean("isDocumentDetails")) {
            /**
             * Used for Document Details report
             *
             */
            selectCol = " mp.paymentnumber,mp.seqformat,mp.seqnumber";
            groupby += " group by mp.id ";
            joinSql += " inner join sequenceformat sq on mp.seqformat=sq.id ";
            orderby += " ORDER BY mp.seqformat,mp.seqnumber ";

        }

        String typeofjoin = " inner ";
        if (reqParams.has("typeofjoinisleft") && reqParams.optBoolean("typeofjoinisleft")) {
            typeofjoin = " left ";
        }

        String query = " select " + selectCol + " from salesreturn sr "
                + " inner join srdetails srd on sr.id=srd.salesreturn and sr.company=?"
                + " inner join gstdocumenthistory gdh on sr.id=gdh.refdocid "
                + " inner join gsttaxclasshistory gtch on srd.id=gtch.refdocid "
                + " inner join payment mp on mp.salesreturn=sr.id "
                + " inner join journalentry mpje on mpje.id=mp.journalentry"
                + " inner join customer c on c.id=sr.customer "
                + " inner join product p on p.id=srd.product "
                + " inner join customeraddressdetails cad on cad.customerid=c.id "
                + " left join invoicedetails invd on srd.cidetails=invd.id "
                + " left join invoice inv on inv.id=invd.invoice "
                + typeofjoin + " join salesreturndetailtermmap ivtd on ivtd.salesreturndetail=srd.id "
                + typeofjoin + " join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + typeofjoin + " join linelevelterms lt on lt.id=eltr.linelevelterms"
                + typeofjoin + " join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 "
                + typeofjoin + " join accproductcustomdata pcd on pcd.productId=p.id "
                + typeofjoin + " join fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass"
                + " left join billingshippingaddresses bst on bst.id=inv.billingshippingaddresses "
                + typeofjoin + " join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + "" + joinSql + builder + groupby + orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * @Desc : Get Advance Receipt data created for GST India i.e. against HSN
     * for GSTR1 report
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getAdvanceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
          List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where r.company=? and r.deleteflag='F' and r.approvestatuslevel=11 ");
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Unregistered i.e. GSTIN is not present
                 */
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql += " inner join masteritem mitm on mitm.id = gdh.gstrtype ";

            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            /**
             * Append condition for customer Type
             */
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            String Type = reqParams.optString("taxClassType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and  ptaxfcd.valuetype in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    } else {
                        conditionBuilder.append(",?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and ptaxfcd.valuetype=?");
                params.add((int) FieldComboData.ValueTypeMap.get(Type));
            }

        }
        if (reqParams.has("entitycolnum")) {
            /**
             * Column no of Entity Value stored in Receipt
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    if (reqParams.optBoolean("atadj")) {
                        builder.append(" and (ldr.receiptlinkdate>=? and ldr.receiptlinkdate<=?) and je.entrydate<=? ");
                        params.add(df.parse(reqParams.optString("startdate")));
                        params.add(df.parse(reqParams.optString("enddate")));
                        params.add(df.parse(reqParams.optString("startdate")));
                    } else {
                        builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                        params.add(df.parse(reqParams.optString("startdate")));
                        params.add(df.parse(reqParams.optString("enddate")));
                    }
                }

            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
            builder.append(" and shl2.state=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
            To perform Quick search by Inv.No or customer name
        */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            builder.append(" and ( r.receiptnumber like '%").append(searchString).append("%' or c.name like '%").append(searchString).append("%')");
        }
        /**
         * No Of table with its aliases *
         *
         */
        String selectCol = " shl2.state as pos ,shl2.id as posid,(IF(je.externalcurrencyrate>0,(rad.amountdue/je.externalcurrencyrate),rad.amountdue)),(IF(je.externalcurrencyrate>0,(rad.taxamount/je.externalcurrencyrate),rad.taxamount)),radtm.percentage,(IF(je.externalcurrencyrate>0,(radtm.termamount/je.externalcurrencyrate),radtm.termamount)),lt.defaultterms,rad.id,"
                + "(IF(je.externalcurrencyrate>0,(ldr.amount/je.externalcurrencyrate),ldr.amount)) as adjustamt,(IF(je.externalcurrencyrate>0,(rad.amount/je.externalcurrencyrate),rad.amount)) as amount,ldr.receiptlinkdate,je.entrydate as adt,r.receiptnumber,r.id as rid,c.gstin, shl1.itemdescription as stcode,c.name as customername";
        
        if (reqParams.optBoolean("at")) {
            /**
             * Includes such Advance which is not linked to any invoice
             */
            joinSql += " left join linkdetailreceipt ldr on ldr.receipt=r.id left join advancedetail ad on ad.receiptadvancedetail=rad.id "
                    + " left join payment p on p.id=ad.payment left join journalentry mje on p.journalentry=mje.id ";
            selectCol += ",(IF(je.externalcurrencyrate>0,(ad.amount/je.externalcurrencyrate),ad.amount)) as refund, mje.entrydate ";
        }
        if (reqParams.optBoolean("atadj")) {
            /**
             * Includes such Advance which is linked to any invoice
             */
            joinSql += " inner join linkdetailreceipt ldr on ldr.receipt=r.id ";
//            builder.append(" and linkedgstje IS NOT NULL ");
//            selectCol += " ,ldr.amount as adjustamt";
        }
        String groupby = "",orderby="";
        if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
            /**
             * Used for GST3B report 
             * i.e. select sum of all Advance Receipt taxable and Tax amount (Separate for each GST type i.e. CGST,IGST etc)
             * Includes such Advance which is not linked to any invoice
             */
            selectCol = "sum((IF(je.externalcurrencyrate>0,(radtm.termamount/je.externalcurrencyrate),radtm.termamount))),lt.defaultterms,sum((IF(je.externalcurrencyrate>0,(rad.amountdue/je.externalcurrencyrate),rad.amountdue))) ";
            groupby = " group by lt.defaultterms ";
            if (reqParams.optBoolean("atadj")) {
                /**
                 * Includes such Advance which is linked to any invoice
                 */
                selectCol = "sum((IF(je.externalcurrencyrate>0,(radtm.termamount/je.externalcurrencyrate),radtm.termamount))),lt.defaultterms,sum((IF(je.externalcurrencyrate>0,(ldr.amount/je.externalcurrencyrate),ldr.amount))) ";
            }
            if (reqParams.optBoolean("zerorated")) {
                /**
                 * Condition for such receipt having GST rates Zero
                 */
                builder.append(" and radtm.termamount=0");
            } else {
                /**
                 * Condition for such receipt having GST rates non Zero
                 */
                builder.append(" and radtm.termamount>0");
            }
        }
        boolean isShipping = reqParams.optBoolean("isShipping",false);
        String isbillingaddress = "T";
        if (isShipping) {
            isbillingaddress = "F";
        }
        String billingShippingPOS = " shl2.state ";
        String localState = reqParams.optString("localState","");

        if (reqParams.has("interstate")) {
            /**
             * Return only Inter state i.e. IGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("<>? ");
            params.add(localState);
        } else if (reqParams.has("intrastate")) {
            /**
             * Return only Intra state i.e. CGST and SGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("=? ");
            params.add(localState);
        }
//        if (reqParams.has("interstate")) {
//            /**
//             * Return only Inter state i.e. IGST records
//             */
//            builder.append(" and (lt.defaultterms=? or lt.defaultterms=?)");
//            params.add(LineLevelTerms.GSTName.get("OutputIGST").toString());
//            params.add(LineLevelTerms.GSTName.get("OutputCESS").toString());
//        } else if (reqParams.has("intrastate")) {
//            builder.append(" and (lt.defaultterms=? or lt.defaultterms=? or lt.defaultterms=? or lt.defaultterms=?)");
//            params.add((String) LineLevelTerms.GSTName.get("OutputCGST"));
//            params.add((String) LineLevelTerms.GSTName.get("OutputSGST"));
//            params.add((String) LineLevelTerms.GSTName.get("OutputUTGST"));
//            params.add(LineLevelTerms.GSTName.get("OutputCESS").toString());
//            }
        if (reqParams.has("isDocumentDetails") && reqParams.optBoolean("isDocumentDetails")) {
            /**
             * Used for Document Details report
             *
             */
            selectCol = " r.receiptnumber,r.seqformat,r.seqnumber";
            groupby += " group by r.id ";
            joinSql += " inner join sequenceformat sq on r.seqformat=sq.id ";
            orderby += " ORDER BY r.seqformat,r.seqnumber ";
        }
        String query = " select " + selectCol + " from receipt r inner join receiptadvancedetail rad on rad.receipt=r.id "
                + " inner join gstdocumenthistory gdh on r.id=gdh.refdocid and gdh.moduleid=16 "
                + " inner join gsttaxclasshistory gtch on rad.id=gtch.refdocid and gtch.moduleid=16 "
                + " inner join journalentry je on je.id=r.journalentry "
                + " inner join customer c on c.id=r.customer "
                + " inner join customeraddressdetails shl2 on shl2.customerid=c.id and shl2.isdefaultaddress='T' and shl2.isbillingaddress='"+ isbillingaddress  + "' "
                + " left join receiptadvancedetailstermmap radtm on radtm.receiptadvancedetail=rad.id "
                + " left join entitybasedlineleveltermsrate eltr on eltr.id=radtm.entityterm"
                + " left join linelevelterms lt on lt.id=eltr.linelevelterms"
                + " left join product pr on pr.id=rad.product "
                + " left join accproductcustomdata pcd on pcd.productId=pr.id "
                + " left join fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass "
                + " left join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 " + joinSql + builder + groupby + orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
      public List getDelieveryOrderData(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where do.company=? and do.deleteflag='F' and dodetails.cidetails!=''"); 
        params.add(reqParams.optString("companyid"));
         if (reqParams.has("entitycolnum")) {
    /**
             * Column no of Entity Value stored in Receipt
             */
            joinSql += " inner join deliveryordercustomdata docust on docust.deliveryOrderId=do.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and docust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
         if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (do.orderdate>=? and do.orderdate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
        String groupby="",orderby="";
        String selectCol = "do.donumber,do.seqformat,do.seqnumber";

          groupby += " group by do.id ";
          joinSql += " inner join sequenceformat sq on do.seqformat=sq.id ";
          orderby += " ORDER BY do.seqformat,do.seqnumber ";
        String query = " select " + selectCol + " from dodetails inner join deliveryorder do on dodetails.deliveryorder=do.id "+joinSql+builder+groupby+orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
     public List getPaymentData(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where pay.company=? and pay.deleteflag='F' and advnc.receiptadvancedetail!=''"); 
        params.add(reqParams.optString("companyid"));
         if (reqParams.has("entitycolnum")) {
    /**
             * Column no of Entity Value stored in Receipt
             */
             joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
         if (reqParams.has("startdate") && df != null) {
             try {
                 if (builder.length() > 1) {

                     builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");

                 }
                 params.add(df.parse(reqParams.optString("startdate")));
                 params.add(df.parse(reqParams.optString("enddate")));
             } catch (ParseException ex) {
                 Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
             }
         }

    
        String groupby="",orderby="";
        String selectCol = "pay.paymentnumber,pay.seqformat,pay.seqnumber";

          groupby += " group by pay.id ";
          joinSql += " inner join sequenceformat sq on pay.seqformat=sq.id ";
          orderby += " ORDER BY pay.seqformat,pay.seqnumber ";
        String query = " select " + selectCol + " from advancedetail as advnc inner join  payment pay on pay.id=advnc.payment  inner join journalentry je on je.id=pay.journalentry "+joinSql+builder+groupby+orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * @Desc : HSN wise invoice for GSTR1 report
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getHSNWiseInvoiceDataWithDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where inv.company=? and inv.deleteflag='F' and inv.isdraft = false and  inv.approvestatuslevel = 11 and inv.istemplate!=2  ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
//        if (reqParams.has("isGSTINnull")) {
//            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
//            if (isGSTINnull) {
//                builder.append(" and c.gstin ='' ");
//            } else {
//                builder.append(" and c.gstin <>'' ");
//            }
//        }
        if (reqParams.has("entitycolnum") && reqParams.has(GSTRConstants.ASSET_SALES_INVOICE_ENTITYCOLUMN)) {
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            int assetColnum = reqParams.optInt(GSTRConstants.ASSET_SALES_INVOICE_ENTITYCOLUMN);
            builder.append(" and (jecust.col" + colnum + "=? or jecust.col" + assetColnum + "=? )");
            params.add(reqParams.optString("entityValue"));
            params.add(reqParams.optString(GSTRConstants.ASSET_SALES_INVOICE_ENTIYVALUE));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        int hsncolnum = reqParams.optInt("hsncolnum");
        int assetHsncolnum = reqParams.optInt(GSTRConstants.ASSET_HSNCOLUMN);

        if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
            builder.append(" and shl1.value=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
            To perform Quick search by Inv.No or customer name
        */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            builder.append(" and ( pfcd.value like '%").append(searchString).append("%' or pfcd1.value like '%").append(searchString).append("%')");
        }
        /**
         * No Of table with its aliases 
         * Invoice = inv 
         * Invoice Detail = invd
         * Journal entry = je 
         * Inventory = it
         * Product = p 
         * Invoice Detail term Map = ivtd 
         * EntitybasedLineLevelTermRate= eltr
         * LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String selectCol = "inv.id as invoiceid,inv.invoicenumber,"
                + "invd.id as invoicedetailid,IF(je.externalcurrencyrate>0,(invd.rate/je.externalcurrencyrate),invd.rate ),it.quantity,lt.term,IF(je.externalcurrencyrate>0,(ivtd.termamount/je.externalcurrencyrate),ivtd.termamount),"
                + "inv.invoiceamountinbase,IFNULL(pfcd.value,pfcd1.value) as hsncode,ivtd.percentage as taxrate,lt.defaultterms,IFNULL(pfcd.id,pfcd1.id) as hsnid"
                + ",d.inpercent as discPercentage,IF(d.inpercent='T', d.discount,IF(je.externalcurrencyrate>0,(d.discount/je.externalcurrencyrate),d.discount)) as discountValue,pfcd.itemdescription as description ";
        joinSql += " left join discount d on d.id = invd.discount ";
        
        int uqccolnum = reqParams.optInt("uqccolnum", 0);
        if (uqccolnum != 0) {
            /*
             For Product specific UQC codes 
             */

            selectCol += " ,IF(inv.isfixedassetinvoice=1,'',puqc.value) as uqc ";
            joinSql += " left join fieldcombodata puqc on puqc.id=pcd.col" + uqccolnum + "";

        }
        
        String query = " select " + selectCol + " from invoice inv inner join invoicedetails invd on inv.id=invd.invoice and inv.company=? "
                + " inner join journalentry je on je.id=inv.journalentry "
                + " inner join customer c on c.id=inv.customer "
                + " inner join inventory it on it.id=invd.id "
                + " inner join product p on p.id=it.product "
                + " left join invoicedetailtermsmap ivtd on ivtd.invoicedetail=invd.id "
                + " left join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + " left join linelevelterms lt on lt.id=eltr.linelevelterms"
                + " left join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 "
                + " inner join accproductcustomdata pcd on pcd.productId=p.id "
                + " left join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + "" 
                + " left join fieldcombodata pfcd1 on pfcd1.id=pcd.col" + assetHsncolnum + "" 
                + joinSql + builder;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }

    /**
     * @Desc Entity Based Line Level Term whose Term Type 7 (for GST)
     * @param mapData
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getEntitybasedLineLevelTermRate(Map<String, Object> mapData) throws ServiceException {
        List params = new ArrayList();
        String defaultquery = "From EntitybasedLineLevelTermRate  where lineLevelTerms.company.companyID = ? and lineLevelTerms.termType = ? and lineLevelTerms.salesOrPurchase = ?";
        params.add(mapData.get(Constants.companyid));
        params.add(Constants.GST_TERM_TYPE);
        boolean salesOrPurchaseFlag = true;
        if (mapData.containsKey("salesOrPurchaseFlag")) {
            salesOrPurchaseFlag = (boolean) mapData.get("salesOrPurchaseFlag");
        }
        params.add(salesOrPurchaseFlag);
        List list = executeQuery(defaultquery,params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    /**
     * @Desc Product Category Mapping, for GST Rule Map
     * @param mapData
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getProductCatgoryGSTRuleMap(Map<String, Object> mapData) throws ServiceException {
        List params = new ArrayList();
        String defaultquery = "select pcgm.* from prodcategorygstmapping pcgm " +
            " inner join fieldcombodata fcd on pcgm.prodcategory = fcd.id " +
            " inner join fieldparams fp on fcd.fieldid = fp.id " +
            " inner join entitybasedlineleveltermsrate ebllt on pcgm.entitytermrate = ebllt.id " +
            " inner join linelevelterms llt on ebllt.linelevelterms = llt.id " +
            " where fp.companyid = ? and llt.salesorpurchase = ? and llt.termtype = ?";

        params.add(mapData.get(Constants.companyid));
        boolean salesOrPurchaseFlag = true;
        if (mapData.containsKey("salesOrPurchaseFlag")) {
            salesOrPurchaseFlag = (boolean) mapData.get("salesOrPurchaseFlag");
        }
        params.add(salesOrPurchaseFlag);
        params.add(Constants.GST_TERM_TYPE);
        List list = executeSQLQuery(defaultquery,params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public List getNillInvoiceDataWithDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where inv.company=? and inv.deleteflag='F' and inv.isdraft = false and inv.approvestatuslevel = 11 and inv.istemplate!=2 ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                builder.append(" and gdh.gstin ='' ");
            } else {
                builder.append(" and gdh.gstin <>'' ");
            }
        }
         if (reqParams.has("entitycolnum")) {
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            int assetColnum = reqParams.optInt(GSTRConstants.ASSET_SALES_INVOICE_ENTITYCOLUMN);
            builder.append(" and (jecust.col" + colnum + "=? or jecust.col" + assetColnum + "=? )");
            //builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
            params.add(reqParams.optString(GSTRConstants.ASSET_SALES_INVOICE_ENTIYVALUE));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.has("greaterlimit") && reqParams.has("limitamount")) {
            if (reqParams.optBoolean("greaterlimit")) {
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and inv.invoiceamountinbase>? ");
                params.add(limit);
            } else {
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and inv.invoiceamountinbase<=? ");
                params.add(limit);
            }
        }
        /*
         Flag for GST-calculation is based on Billing Address or Shipping Address
         */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping = true;
        }
        String billingShippingPOS = (isShipping == false) ? "bst.billingstate" : "bst.shippingstate";
        if (reqParams.has("interstate") || reqParams.has("intrastate")) {

            String localState = reqParams.optString("localState");
            params.add(localState);
            if (reqParams.optBoolean("intrastate")) {
                builder.append(" and ").append(billingShippingPOS).append("=? ");
            } else {
                builder.append(" and ").append(billingShippingPOS).append("<>? ");
            }

//            params.add(LineLevelTerms.GSTName.get("OutputIGST").toString());
//            params.add("00efb196-5f34-11e7-907b-a6006ad3dba0");
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype ";

            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("isb2cs")) {
            /**
             * a) Intra-State: any value b) Inter-State: Invoice value Rs 2.5
             * lakh or less
             */
            double limit = reqParams.optDouble("limitamount");
            builder.append(" and ((lt.defaultterms=? and inv.invoiceamountinbase<=? )or (lt.defaultterms=? or lt.defaultterms=? or lt.defaultterms=? or lt.defaultterms=?))");
            params.add((String) LineLevelTerms.GSTName.get("OutputIGST"));
            params.add(limit);
            params.add((String) LineLevelTerms.GSTName.get("OutputCGST"));
            params.add((String) LineLevelTerms.GSTName.get("OutputSGST"));
            params.add((String) LineLevelTerms.GSTName.get("OutputUTGST"));
            params.add((String) LineLevelTerms.GSTName.get("OutputCESS"));
        }

        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            builder.append(" and ptaxfcd.valuetype=?");
            String Type = reqParams.optString("taxClassType");
            params.add((int) FieldComboData.ValueTypeMap.get(Type));
        } else {
            builder.append(" and ptaxfcd.valuetype=0");;
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");

            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        int hsncolnum = reqParams.optInt("hsncolnum");
        int taxclasscolnum = reqParams.optInt("taxclasscolnum");
        int assetHsncolnum = reqParams.optInt(GSTRConstants.ASSET_HSNCOLUMN);
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String groupby = "";
        String selectCol = "";

        if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
            selectCol = "sum((IF(je.externalcurrencyrate>0,(invd.rate/je.externalcurrencyrate),invd.rate ) * it.quantity ) - IF(je.externalcurrencyrate>0,(ifNull(case when d.inpercent='T' then (d.discount*(invd.rate*it.quantity)/100) else d.discount end,0)/je.externalcurrencyrate),ifNull(case when d.inpercent='T' then (d.discount*(invd.rate*it.quantity)/100) else d.discount end,0))),count(distinct inv.id)";
            builder.append(" and invd.rowtermamount=0 ");
            builder.append(" and invd.id not in (select ivdtm.invoicedetail from invoicedetailtermsmap ivdtm "
                    + "inner join linelevelterms lt on lt.id=ivdtm.term where lt.company=?) ");
            params.add(reqParams.optString("companyid"));
        } else {
            selectCol = "c.id as customerid,c.gstin,inv.id as invoiceid,inv.invoicenumber,je.id as jeid,je.entrydate,invd.id as invoicedetailid,IF(je.externalcurrencyrate>0,(invd.rate/je.externalcurrencyrate),invd.rate ),it.quantity,p.id as productid,'' as term,IF(je.externalcurrencyrate>0,(ifNull(case when d.inpercent='T' then (d.discount*(invd.rate*it.quantity)/100) else d.discount end,0)/je.externalcurrencyrate),ifNull(case when d.inpercent='T' then (d.discount*(invd.rate*it.quantity)/100) else d.discount end,0)),inv.invoiceamountinbase,IFNULL(pfcd.value,pfcd1.value) as hsncode,"+ billingShippingPOS +" as pos,p.description,'' as posid,1.0 as taxrate,'' as defaultterms,d.inpercent as discPercentage,IF(d.inpercent='T', d.discount,IF(je.externalcurrencyrate>0,(d.discount/je.externalcurrencyrate),d.discount)) as discountValue,'' as stcode,c.name as customerName";
        }
        joinSql +=" left join discount d on d.id = invd.discount ";
        String query = " select " + selectCol + " from invoice inv inner join invoicedetails invd on inv.id=invd.invoice and inv.company=? "
                + " inner join gstdocumenthistory gdh on inv.id=gdh.refdocid and gdh.moduleid in ('2','38') "
                + " inner join gsttaxclasshistory gtch on invd.id=gtch.refdocid and gtch.moduleid in ('2','38') "
                + " inner join journalentry je on je.id=inv.journalentry "
                + " inner join customer c on c.id=inv.customer "
                + " inner join inventory it on it.id=invd.id "
                + " inner join product p on p.id=it.product "
                + " inner join accproductcustomdata pcd on pcd.productId=p.id "
                + " inner join billingshippingaddresses bst on bst.id=inv.billingshippingaddresses "
                + " left join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + ""
                + " left join fieldcombodata pfcd1 on pfcd1.id=pcd.col" + assetHsncolnum + ""
                + " inner join fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass " + joinSql + builder;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }

    public List getNillCNDNWithInvoiceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where inv.company=? and inv.deleteflag='F' ");
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                builder.append(" and gdh.gstin ='' ");
            } else {
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("entitycolnum")) {
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=cnje.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (cnje.entrydate>=? and cnje.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.has("greaterlimit") && reqParams.has("limitamount")) {
            if (reqParams.optBoolean("greaterlimit")) {
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and inv.invoiceamount>? ");
                params.add(limit);
            } else {
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and inv.invoiceamount<=? ");
                params.add(limit);
            }
        }
        if (reqParams.has("interstate")) {
            builder.append(" and (lt.defaultterms=? or lt.defaultterms=?) ");
            params.add(LineLevelTerms.GSTName.get("OutputIGST").toString());
            params.add(LineLevelTerms.GSTName.get("OutputCESS").toString());
//            params.add("00efb196-5f34-11e7-907b-a6006ad3dba0");
        }

        int hsncolnum = reqParams.optInt("hsncolnum");
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Sales Return Detail=srd Credit Note = cn Credit Note details = cnd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String selectCol = "";
        if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
            selectCol = "sum(inv.excludinggstamount) ";
            builder.append(" and srd.rowtermamount=0 ");
            builder.append(" and srd.id not in (select ivdtm.salesreturndetail from salesreturndetailtermmap ivdtm "
                    + "inner join linelevelterms lt on lt.id=ivdtm.term where lt.company=?) ");
            params.add(reqParams.optString("companyid"));
        }
        String query = " select " + selectCol + " from invoice inv inner join invoicedetails invd on inv.id=invd.invoice "
                + " inner join srdetails srd on srd.cidetails=invd.id "
                + " inner join salesreturn sr on sr.id=srd.salesreturn "
                + " inner join creditnote cn on cn.salesreturn=sr.id "
                + " inner join cndetails cnd on cnd.creditnote=cn.id "
                + " inner join journalentry je on je.id=inv.journalentry "
                + " inner join journalentry cnje on cnje.id=cn.journalentry"
                + " inner join customer c on c.id=inv.customer "
                + " inner join inventory it on it.id=invd.id "
                + " inner join product p on p.id=srd.product "
                + " inner join accproductcustomdata pcd on pcd.productId=p.id "
                + " inner join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + "" + joinSql + builder;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }

    public List getNillAdvanceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where r.company=? and r.deleteflag='F' ");
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                builder.append(" and c.gstin ='' ");
            } else {
                builder.append(" and c.gstin <>'' ");
            }
        }
        if (reqParams.has("entitycolnum")) {
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    if (reqParams.optBoolean("atadj")) {
                        builder.append(" and (ldr.receiptlinkdate>=? and ldr.receiptlinkdate<=?) ");
                    } else {
                        builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                    }

                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.optBoolean("at")) {
            builder.append(" and (rad.amount+rad.taxamount = rad.amountdue)");
        }

        /**
         * No Of table with its aliases *
         *
         */
        if (reqParams.optBoolean("atadj")) {
            joinSql += " inner join linkdetailreceipt ldr on ldr.receipt=r.id ";
        }
        String selectCol = "";
        if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
            selectCol = "sum(rad.amount) ";
            if (reqParams.optBoolean("atadj")) {
                selectCol = "sum(ldr.amount) ";
            }
            builder.append(" and rad.taxamount=0 ");
            builder.append(" and rad.id not in (select ivdtm.receiptadvancedetail from receiptadvancedetailstermmap ivdtm"
                    + " inner join entitybasedlineleveltermsrate eltr on eltr.id=ivdtm.entityterm  "
                    + "inner join linelevelterms lt on lt.id=eltr.linelevelterms where lt.company=?) ");
            params.add(reqParams.optString("companyid"));
        }
        String query = " select " + selectCol + " from receipt r inner join receiptadvancedetail rad on rad.receipt=r.id "
                + " inner join journalentry je on je.id=r.journalentry "
                + " inner join customer c on c.id=r.customer " + joinSql + builder;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    @Override
    public List getNonGSTGoodsReceiptDataWithDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where gr.company=? and gr.deleteflag='F' and gr.approvestatuslevel = 11 and gr.istemplate!=2 "); // advance paid 10 A
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                builder.append(" and gdh.gstin ='' ");
            } else {
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("entitycolnum")) {
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            int assetColnum = reqParams.optInt(GSTRConstants.ASSET_PURCHASE_INVOICE_ENTITYCOLUMN);
            builder.append(" and (jecust.col" + colnum + "=? or jecust.col" + assetColnum + "=? )");
            params.add(reqParams.optString("entityValue"));
            params.add(reqParams.optString(GSTRConstants.ASSET_PURCHASE_INVOICE_ENTIYVALUE));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.has("greaterlimit") && reqParams.has("limitamount")) {
            if (reqParams.optBoolean("greaterlimit")) {
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and gr.invoiceamount>? ");
                params.add(limit);
            } else {
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and gr.invoiceamount<=? ");
                params.add(limit);
            }
        }
          /*
          Flag for GST-calculation is based on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping= true;
        }
        String billingShippingPOS = (isShipping == false) ? " bsad.billingstate " : " bsad.shippingstate " ;
       /**
         * For india country and vendor transactions and
         * isAddressFromVendorMaster is off then vendor billing address is store
         * in seperate key pair
         */
        boolean isAddressNotFromVendorMaster = reqParams.optBoolean(GSTRConstants.isAddressNotFromVendorMaster, false);
        if(isAddressNotFromVendorMaster){
            billingShippingPOS = (isShipping == false) ? " bsad.vendorbillingstateforindia " : " bsad.vendcustshippingstate ";
        }
        String localState = (!StringUtil.isNullOrEmpty(reqParams.optString("localState")) ? reqParams.optString("localState") : "");
        if (reqParams.has("interstate")) {
            /**
             * Return only Inter state i.e. IGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("<>? ");
            params.add(localState);
        } else if (reqParams.has("intrastate")) {
            builder.append(" and ").append(billingShippingPOS).append("=? ");
            params.add(localState);
        }
//        if (reqParams.has("interstate")) {
//            builder.append(" and (lt.defaultterms=? or lt.defaultterms=?) ");
//            params.add(LineLevelTerms.GSTName.get("InputIGST").toString());
//            params.add(LineLevelTerms.GSTName.get("InputCESS").toString());
////            params.add("00efb196-5f34-11e7-907b-a6006ad3dba0");
//        }
        if (reqParams.has("isb2cs")) {
            /**
             * a) Intra-State: any value b) Inter-State: Invoice value Rs 2.5
             * lakh or less
             */
            double limit = reqParams.optDouble("limitamount");
            builder.append(" and ((lt.defaultterms=? and inv.invoiceamountinbase<=? )or (lt.defaultterms=? or lt.defaultterms=? or lt.defaultterms=? or lt.defaultterms=?))");
            params.add((String) LineLevelTerms.GSTName.get("InputIGST"));
            params.add(limit);
            params.add((String) LineLevelTerms.GSTName.get("InputCGST"));
            params.add((String) LineLevelTerms.GSTName.get("InputSGST"));
            params.add((String) LineLevelTerms.GSTName.get("InputUTGST"));
            params.add((String) LineLevelTerms.GSTName.get("InputCESS"));
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
           

            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for vendor Type
             */
            joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");

            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        int hsncolnum = reqParams.optInt("hsncolnum");
        int taxclasscolnum = reqParams.optInt("taxclasscolnum");
        int assetHsncolnum = reqParams.optInt(GSTRConstants.ASSET_HSNCOLUMN);
        int assetTaxclasscolnum = reqParams.optInt(GSTRConstants.ASSET_TAXCLASSCOLUMN);
        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             * 
             */
            String Type = reqParams.optString("taxClassType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and ptaxfcd.valuetype in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(FieldComboData.ValueTypeMap.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(FieldComboData.ValueTypeMap.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else{
            builder.append(" and (ptaxfcd.valuetype=? ) ");
            params.add((int) FieldComboData.ValueTypeMap.get(Type));
           }
//            params.add((int) FieldComboData.ValueTypeMap.get(Type));
        }
        if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
//            builder.append(" and bsad.billingstate=?");
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
         To perform Quick search by Inv.No or customer name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"gr.grnumber","v.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 2);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    builder.append(searchQuery);                    
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (reqParams.has("itctype") && !StringUtil.isNullOrEmpty(reqParams.optString("itctype"))) {
            /**
             * Condition to filter details on the basis of ITC types.
             */
            builder.append(" and grd.itctype in (?) ");
            params.add(reqParams.optString("itctype"));
        }     
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String groupby = "";
        String selectCol = "";
        String typeofjoin = " inner ";
        if (reqParams.has("typeofjoinisleft") && reqParams.optBoolean("typeofjoinisleft")) {
            typeofjoin = " left ";
        }

        if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
            selectCol = "sum((IF(je.externalcurrencyrate>0,(grd.rate/je.externalcurrencyrate),grd.rate ) * it.quantity ) - IF(je.externalcurrencyrate>0,(ifNull(case when d.inpercent='T' then (d.discount*(grd.rate*it.quantity)/100) else d.discount end,0)/je.externalcurrencyrate), ifNull(case when d.inpercent='T' then (d.discount*(grd.rate*it.quantity)/100) else d.discount end,0))),count(distinct gr.id) ";
            builder.append(" and grd.rowtermamount=0 ");
            builder.append(" and grd.id not in (select ivdtm.goodsreceiptdetail from receiptdetailtermsmap ivdtm "
                    + "inner join linelevelterms lt on lt.id=ivdtm.term where lt.company=?) ");
            params.add(reqParams.optString("companyid"));
        } else {
            selectCol = "v.id as customerid,v.gstin,gr.id as invoiceid,gr.grnumber,je.id as jeid,je.entrydate,grd.id as invoicedetailid,IF(je.externalcurrencyrate>0,(grd.rate/je.externalcurrencyrate),grd.rate ),it.quantity,p.id as productid,'' as term,IF(je.externalcurrencyrate>0,(termmap.termamount/je.externalcurrencyrate),termmap.termamount),gr.invoiceamountinbase,IFNULL(pfcd.value,pfcd1.value) as hsncode," + billingShippingPOS + " as pos,p.description,shl1.id as posid,termmap.percentage as taxrate,'' as defaultterms,d.inpercent as discPercentage,IF(d.inpercent='T', d.discount,IF(je.externalcurrencyrate>0,(d.discount/je.externalcurrencyrate),d.discount)) as discountValue,gr.supplierinvoiceno,v.name as vendorname  ";
        }/*
         For GSTR2 Exempt (Nil Rated Section) select columns 
        */
          if (reqParams.has("isExempt") && reqParams.optBoolean("isExempt")) {
             selectCol = " gr.grnumber, " + billingShippingPOS + " as pos,mitm.value,v.gstin,je.entrydate,(IF(je.externalcurrencyrate>0,(grd.rate/je.externalcurrencyrate),grd.rate ) * it.quantity ) - IF(je.externalcurrencyrate>0,(ifNull(case when d.inpercent='T' then (d.discount*(grd.rate*it.quantity)/100) else d.discount end,0)/je.externalcurrencyrate),ifNull(case when d.inpercent='T' then (d.discount*(grd.rate*it.quantity)/100) else d.discount end,0)),ptaxfcd.value,gr.supplierinvoiceno";
             builder.append(" and grd.rowtermamount=0 ");
            String inQuery = " not in ";
            String groupByQuery = "  ";
            if (reqParams.optBoolean(GSTRConstants.IS_PRODUCT_TAX_ZERO, false)) {
                inQuery = " in ";
                groupByQuery = " group by gr.id ";
            }
                builder.append(" and grd.id " + inQuery + " (select ivdtm.goodsreceiptdetail from receiptdetailtermsmap ivdtm "
                        + "inner join linelevelterms lt on lt.id=ivdtm.term where lt.company=?) ");
                params.add(reqParams.optString("companyid"));
                builder.append(groupByQuery);
            }
        joinSql +=" left join discount d on d.id = grd.discount ";
        String query = " select " + selectCol + " from goodsreceipt gr inner join grdetails grd on gr.id=grd.goodsreceipt and gr.company=? "
                + " inner join gstdocumenthistory gdh on gr.id=gdh.refdocid and gdh.moduleid in ('6','39') "
                + " inner join gsttaxclasshistory gtch on grd.id=gtch.refdocid and gtch.moduleid in ('6','39') "
                + " inner join journalentry je on je.id=gr.journalentry "
                + " inner join vendor v on v.id=gr.vendor "
                + " inner join inventory it on it.id=grd.id "
                + " inner join product p on p.id=it.product "
                + " inner join masteritem mitm on mitm.id=gdh.gstrtype "
                + " inner join accproductcustomdata pcd on pcd.productId=p.id "
                + typeofjoin + " join receiptdetailtermsmap termmap on grd.id = termmap.goodsreceiptdetail "
                + typeofjoin +" join entitybasedlineleveltermsrate eltr on eltr.id=termmap.entityterm "
                + typeofjoin + " join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 "
                + " inner join billingshippingaddresses bsad on bsad.id=gr.billingshippingaddresses "
                + " left join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + ""
                + " left join fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass"
                + " left join fieldcombodata pfcd1 on pfcd1.id=pcd.col" + assetHsncolnum + ""
//                + " left join fieldcombodata ptaxfcd1 on ptaxfcd1.id=pcd.col" + assetTaxclasscolnum + ""
                + " " + joinSql + builder;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * Get Corrupted invoices i.e. which are missing in GST filling
     *
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getGSTMissingInvoice(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        String having = "";
        String orderby = "";
        String additionalSelectCol = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        String col = "p.description as description";
        int hsncolnum = reqParams.optInt("hsncolnum");
        int uqccolnum = reqParams.optInt("uqccolnum");
        int taxclasscolnum = reqParams.optInt("taxclasscolnum");
        builder.append(" where inv.company=? and inv.deleteflag='F' and inv.isdraft = false  and inv.approvestatuslevel = 11 and inv.istemplate!=2 ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("ishsnblank") && reqParams.optBoolean("ishsnblank")) {
            builder.append(" and (pcd.col" + hsncolnum + " is NULL or pcd.col" + hsncolnum + "='' or (LENGTH(pfcd.value) > 8))");
            col = "pcd.col" + hsncolnum;
        }
       // provide check for HSN/SAC Code should not be greater than 8 digits (For India) ERM-1092
//        if (reqParams.has("ishsninvalid") && reqParams.optBoolean("ishsninvalid")) {
//            builder.append(" or (LENGTH(pfcd.value) > 8) ");            
//        }
        if (reqParams.has("isuqcblank") && reqParams.optBoolean("isuqcblank")) {
            builder.append(" and (pcd.col" + uqccolnum + " is NULL or pcd.col" + uqccolnum + "='' )");
            col = "pcd.col" + uqccolnum;
        }
        /**
         * Below query for GST Registration blank and Wrong GST registration
         * tagged. Example - Other than Regular, Composition and Unregistered is
         * wrong GST Registration type ,Please ref.ERP-35464 for more details
         */
        String GSTRegTypeSelectColumn = " ,'' as gstrtype";
        String GSTTypeName = " ,'' as gsttypename";
        if (reqParams.has("isgstregtypeblank") && reqParams.optBoolean("isgstregtypeblank")) {
            if(!reqParams.has("registrationType")){
                joinSql+= " left join masteritem mitm on mitm.id=gdh.gstrtype ";
            }
            String GSTRegTypeQuery = "'"+Constants.GSTRegType.get(Constants.GSTRegType_Composition)+"',";
            GSTRegTypeQuery += "'"+Constants.GSTRegType.get(Constants.GSTRegType_Regular)+"',";
            GSTRegTypeQuery += "'"+Constants.GSTRegType.get(Constants.GSTRegType_Unregistered)+"'";
            builder.append(" and ((gdh.gstrtype IS NULL or gdh.gstrtype='') or (mitm.defaultmasteritem not in(" + GSTRegTypeQuery + ")))");
            GSTRegTypeSelectColumn = " ,gdh.gstrtype as gstrtype ";
            GSTTypeName = " ,mitm.value as gsttypename";
        }
        /**
         * Below query for GST Customer blank and Wrong GST registration
         * Please ref.ERP-35464 for more details
         */
        String GSTCustVendTypeSelectColumn = " ,'' as custventypeid";
        if (reqParams.has("iscusttypeblank") && reqParams.optBoolean("iscusttypeblank")) {
            if (!reqParams.has("CustomerType") && !reqParams.has("registrationType")) {
                joinSql += " left join masteritem mitm on mitm.id=gdh.gstrtype ";
                joinSql += " left join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            }
            String GSTCustomerTypeQuery = " (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Composition) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Regular) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZ) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZWOPAY) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Unregistered) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Export) + "','" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_ExportWOPAY) + "')) ";
            builder.append(" and ((gdh.custventypeid IS NULL or gdh.custventypeid='') or (" + GSTCustomerTypeQuery+"))");
            GSTCustVendTypeSelectColumn = " ,gdh.custventypeid as custventypeid ";
            GSTTypeName = " ,mstrItem.value as gsttypename";
        }
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is Unregistered i.e. GSTIN is not present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("ishistoryblank") && reqParams.optBoolean("ishistoryblank")) {
            builder.append(" and inv.id not in (select refdocid from gstdocumenthistory where moduleid in ('2','38'))");
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql+= " inner join masteritem mitm on mitm.id=gdh.gstrtype";
            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql+= " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("invoiceentitycolnum")) {
            /**
             * Column no of Entity dimension's value stored in Invoice
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("invoiceentitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("invoiceentityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            builder.append(" and ptaxfcd.valuetype=?");
            String Type = reqParams.optString("taxClassType");
            params.add((int) FieldComboData.ValueTypeMap.get(Type));
        }
        if (reqParams.has("isRCMApplicable")) {
            /**
             * Append condition for RCM Applicable or not
             */
            builder.append(" and inv.rcmapplicable=?");
            boolean InvoiceType = reqParams.optBoolean("isRCMApplicable");
            if (InvoiceType) {
                params.add('T');
            } else {
                params.add('F');
            }
        }
         /*
          Flag for GST-calculation is based on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping= true;
        }
        String billingShippingPOS = (isShipping==false)?"bst.billingstate":"bst.shippingstate" ;
        if (reqParams.has("statemismatch") && reqParams.optBoolean("statemismatch")) {
            int invoicestatecolnum = reqParams.optInt("invoicestatecolnum");
            builder.append(" and ((jecust.col" + invoicestatecolnum + " is NULL or jecust.col" + invoicestatecolnum + "='')");
            builder.append(" or (").append(billingShippingPOS).append(" IS NULL or ").append(billingShippingPOS).append(" ='' ))");

        }
        if (reqParams.has("withoutseqformat") && reqParams.optBoolean("withoutseqformat")) {
            builder.append(" and inv.seqformat is NULL");

        }
        if(reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))){
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
         To perform Quick search by Inv.No or customer name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"inv.invoicenumber","c.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 2);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    builder.append(searchQuery);                    
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String groupby = "";
        String selectCol = " c.name as customerid,gdh.gstin,inv.id as invoiceid,inv.invoicenumber,je.id as jeid,je.entrydate,"
                + "invd.id as invoicedetailid,invd.rate,it.quantity,p.productid as productid,lt.term,ivtd.termamount,"
                + "inv.invoiceamountinbase,pfcd.value as hsncode,ptaxfcd.value as pos," + col + ",sr.srnumber as posid,"
                + "ivtd.percentage as taxrate,lt.defaultterms,d.inpercent as discPercentage,d.discount as discountValue, '1' as code,"
                + "ivtd.percentage as termpercentage,eltr.percentage as rulepercentage "
                + ",ivtd.taxtype "+ GSTRegTypeSelectColumn + GSTCustVendTypeSelectColumn + GSTTypeName;

        joinSql += " left join discount d on d.id = invd.discount ";
   
        String query = " select " + selectCol + " from invoice inv inner join invoicedetails invd on inv.id=invd.invoice and inv.company=?"
                + " inner join journalentry je on je.id=inv.journalentry "
                + " left join gstdocumenthistory gdh on inv.id=gdh.refdocid and gdh.moduleid in ('2','38') "
                + " inner join gsttaxclasshistory gtch on invd.id=gtch.refdocid and gtch.moduleid in ('2','38') "             
                + " inner join customer c on c.id=inv.customer "   
//                + " inner join gstcustomerhistory gch on c.id=gch.customer"
                + " left join srdetails srd on srd.cidetails=invd.id "
                + " left join salesreturn sr on sr.id=srd.salesreturn "
                + " inner join inventory it on it.id=invd.id "
                + " inner join product p on p.id=it.product "
                + " left join invoicedetailtermsmap ivtd on ivtd.invoicedetail=invd.id "
                + " left join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
//                + " left join prodcategorygstmapping pcgm on eltr.id=pcgm.entitytermrate"
                + " left join linelevelterms lt on lt.id=eltr.linelevelterms"
                + " left join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 "
                + " inner join billingshippingaddresses bst on bst.id=inv.billingshippingaddresses"
                + " left join accproductcustomdata pcd on pcd.productId=p.id "
                + " left join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + ""
                + " left join fieldcombodata ptaxfcd on ptaxfcd.id=pcd.col" + taxclasscolnum + ""
                + " " + joinSql + builder + groupby + having + orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * Function to get Credit Note in Mismatch report
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public List getGSTMissingCN(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        String having = "";
        String orderby = "";
        String additionalSelectCol = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        String col = " '' ";
        builder.append(" where cn.company=? and cn.deleteflag='F' and cn.approvestatuslevel=11 ");
        params.add(reqParams.optString("companyid"));
        int hsncolnum = reqParams.optInt("hsncolnum");
        int uqccolnum = reqParams.optInt("uqccolnum");
        int taxclasscolnum = reqParams.optInt("taxclasscolnum");
        String joinForHSNandUQCCode = " left ";
        if (reqParams.has("ishsnblank") && reqParams.optBoolean("ishsnblank")) {
            builder.append(" and (pcd.col" + hsncolnum + " is NULL or pcd.col" + hsncolnum + "='' or (LENGTH(pfcd.value) > 8))");
            col = "pcd.col" + hsncolnum;
            joinForHSNandUQCCode = " inner ";
        }
        if (reqParams.has("isuqcblank") && reqParams.optBoolean("isuqcblank")) {
            builder.append(" and (pcd.col" + uqccolnum + " is NULL or pcd.col" + uqccolnum + "='' )");
            col = "pcd.col" + uqccolnum;
            joinForHSNandUQCCode = " inner ";
        }
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is Unregistered i.e. GSTIN is not present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin <>'' ");
            }
        }
//        if (reqParams.has("iscnwithoutinvoice") && reqParams.optBoolean("iscnwithoutinvoice")) {
//            builder.append(" and srd.cidetails is NULL ");
//        }

        if (reqParams.has("entitycolnum")) {
            /**
             * Column no of Entity dimension's value stored in Invoice
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /**
         * Below query for GST Registration blank and Wrong GST registration
         * tagged. Example - Other than Regular, Composition and Unregistered is
         * wrong GST Registration type ,Please ref.ERP-35464 for more details
         */
        String GSTRegTypeSelectColumn = " ,'' as gstrtype";
        String GSTTypeName = " ,'' as gsttypename";
        if (reqParams.has("isgstregtypeblank") && reqParams.optBoolean("isgstregtypeblank")) {
            if(!reqParams.has("registrationType")){
                joinSql+= " left join masteritem mitm on mitm.id=gdh.gstrtype ";
            }
            String GSTRegTypeQuery = "'"+Constants.GSTRegType.get(Constants.GSTRegType_Composition)+"',";
            GSTRegTypeQuery += "'"+Constants.GSTRegType.get(Constants.GSTRegType_Regular)+"',";
            GSTRegTypeQuery += "'"+Constants.GSTRegType.get(Constants.GSTRegType_Unregistered)+"'";
            builder.append(" and ((gdh.gstrtype IS NULL or gdh.gstrtype='') or (mitm.defaultmasteritem not in(" + GSTRegTypeQuery + ")))");
            GSTRegTypeSelectColumn = " ,gdh.gstrtype as gstrtype ";
            GSTTypeName = " ,mitm.value as gsttypename";
        }
        /**
         * Below query for GST Customer blank and Wrong GST registration
         * Please ref.ERP-35464 for more details
         */
        String GSTCustVendTypeSelectColumn = " ,'' as custventypeid";
        if (reqParams.has("iscusttypeblank") && reqParams.optBoolean("iscusttypeblank")) {
            if (!reqParams.has("CustomerType") && !reqParams.has("registrationType")) {
                joinSql += " left join masteritem mitm on mitm.id=gdh.gstrtype ";
                joinSql += " left join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            }
            String GSTCustomerTypeQuery = " (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Composition) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Regular) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZ) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZWOPAY) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Unregistered) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Export) + "','" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_ExportWOPAY) + "')) ";
            builder.append(" and ((gdh.custventypeid IS NULL or gdh.custventypeid='') or (" + GSTCustomerTypeQuery+"))");
            GSTCustVendTypeSelectColumn = " ,gdh.custventypeid as custventypeid ";
            GSTTypeName = " ,mstrItem.value as gsttypename";
        }
         if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql+= " inner join masteritem mitm on mitm.id=gdh.gstrtype";
            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql+= " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("ishistoryblank") && reqParams.optBoolean("ishistoryblank")) {
            builder.append(" and cn.id not in (select refdocid from gstdocumenthistory where moduleid in ('12'))");
        }
         /*
          Flag for GST-calculation is based on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping= true;
        }
        String billingShippingPOS = (isShipping==false)?"bst.billingstate":"bst.shippingstate" ;
        if (reqParams.has("statemismatch") && reqParams.optBoolean("statemismatch")) {
            int CNstatecolnum = reqParams.optInt(GSTRConstants.CN_STATE_COLUMN);
            builder.append(" and ((jecust.col" + CNstatecolnum + " is NULL or jecust.col" + CNstatecolnum + "='')");
            builder.append(" or (").append(billingShippingPOS).append(" IS NULL or ").append(billingShippingPOS).append(" ='' ))");

        }
        if (reqParams.has("withoutseqformat") && reqParams.optBoolean("withoutseqformat")) {
            builder.append(" and cn.seqformat is NULL");

        }
        if(reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))){
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
         To perform Quick search by CN.No or customer name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"cn.cnnumber", "c.name", "v.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 3);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    builder.append(searchQuery);
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String groupby = " group by ivtd.id ";
        String selectCol = " IFNULL(c.name,v.name) as customerid, gdh.gstin ,cn.id as cnid,cn.cnnumber,je.id as jeid,je.entrydate,"
                + "cnd.id as cntid,cnt.amount as rate, 1.0 as quantity,p.productid as productid,lt.term,ivtd.termamount,"
                + "cn.cnamountinbase,pfcd.value as hsncode,'' as pos, " + col + " as col,'' as posid,"
                + "ivtd.percentage as taxrate,lt.defaultterms,'' as discPercentage,'' as discountValue, '3' as code,"
                + "ivtd.percentage as termpercentage,eltr.percentage as rulepercentage "
                + ",ivtd.taxtype " + GSTRegTypeSelectColumn + GSTCustVendTypeSelectColumn + GSTTypeName;

        String typeofjoin = " left ";
        String query = " select " + selectCol + " from cntaxentry cnt "
                + " inner join creditnote cn on cn.id=cnt.creditnote and (cn.salesreturn is null) "
                + " inner join cndetails cnd on cn.id=cnd.creditnote "
                + joinForHSNandUQCCode + " join product p on p.id=cnt.productid "
                + joinForHSNandUQCCode + " join accproductcustomdata pcd on pcd.productId=p.id "
                + " left join gstdocumenthistory gdh on cn.id=gdh.refdocid and gdh.moduleid in ('12') "
                + " left join gsttaxclasshistory gtch on cnd.id=gtch.refdocid and gtch.moduleid in ('12') "             
//                + typeofjoin + " join salesreturn sr on sr.id=cn.salesreturn "
//                + typeofjoin + " join srdetails srd on srd.salesreturn=sr.id "
                + " inner join journalentry je on cn.journalentry=je.id "
                + " inner join jedetail jed on jed.journalentry=je.id "
                + typeofjoin + " join customer c on c.id=cn.customer  "
                + typeofjoin + " join vendor v on v.id=cn.vendor "
                + " inner join billingshippingaddresses bst on bst.id=cn.billingshippingaddresses"
                + joinSql
                + typeofjoin + " join creditnotedetailtermmap ivtd on ivtd.creditnotetaxentry=cnt.id "
                + typeofjoin + " join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + typeofjoin + " join linelevelterms lt on lt.id=eltr.linelevelterms"
                + typeofjoin + " join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 " 
                + typeofjoin + " join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + ""
                + typeofjoin + " join fieldcombodata ptaxfcd on ptaxfcd.id=pcd.col" + taxclasscolnum + ""
                + builder + groupby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    public List getGSTMissingPurchaseInvoice(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        String having = "";
        String orderby = "";
        String additionalSelectCol = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        String col = "p.description as description";
        int hsncolnum = reqParams.optInt("hsncolnum");
        int uqccolnum = reqParams.optInt("uqccolnum");
        int taxclasscolnum = reqParams.optInt("taxclasscolnum");
        builder.append(" where inv.company=? and inv.deleteflag='F'  and inv.approvestatuslevel = 11 and inv.istemplate!=2 ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("ishsnblank") && reqParams.optBoolean("ishsnblank")) {
            builder.append(" and (pcd.col" + hsncolnum + " is NULL or pcd.col" + hsncolnum + "='' or (LENGTH(pfcd.value) > 8))");
            col = "pcd.col" + hsncolnum;
        }
        // Provide check for HSN/SAC Code should not be greater than 8 digits (For India) ERM-1092
//        if (reqParams.has("ishsninvalid") && reqParams.optBoolean("ishsninvalid")) {
//            builder.append(" or (LENGTH(pfcd.value) > 8) ");
//            
//        }
        if (reqParams.has("isuqcblank") && reqParams.optBoolean("isuqcblank")) {
            builder.append(" and (pcd.col" + uqccolnum + " is NULL or pcd.col" + uqccolnum + "='' )");
            col = "pcd.col" + uqccolnum;
        }
        /**
         * Below query for GST Registration blank and Wrong GST registration tagged.
         * Example - Other than Regular, Composition and Unregistered is wrong GST Registration type.
         * Please ref.ERP-35464 for more details 
         */
        String GSTRegTypeSelectColumn = " ,'' as gstrtype";
        String GSTTypeName = " ,'' as gsttypename";
        if (reqParams.has("isgstregtypeblank") && reqParams.optBoolean("isgstregtypeblank")) {
            if(!reqParams.has("registrationType")){
                joinSql+= " left join masteritem mitm on mitm.id=gdh.gstrtype ";
            }
            String GSTRegTypeQuery = "'"+Constants.GSTRegType.get(Constants.GSTRegType_Composition)+"',";
            GSTRegTypeQuery += "'"+Constants.GSTRegType.get(Constants.GSTRegType_Regular)+"',";
            GSTRegTypeQuery += "'"+Constants.GSTRegType.get(Constants.GSTRegType_Unregistered)+"'";
            builder.append(" and ((gdh.gstrtype IS NULL or gdh.gstrtype='') or (mitm.defaultmasteritem not in(" + GSTRegTypeQuery + ")))");
            GSTRegTypeSelectColumn = " ,gdh.gstrtype as gstrtype ";
            GSTTypeName = " ,mitm.value as gsttypename";
        }
        /**
         * Below query for GST Customer blank and Wrong GST registration Please
         * ref.ERP-35464 for more details
         */
        String GSTCustVendTypeSelectColumn = " ,'' as custventypeid";
        if (reqParams.has("iscusttypeblank") && reqParams.optBoolean("iscusttypeblank")) {
            if (!reqParams.has("CustomerType") && !reqParams.has("registrationType")) {
                joinSql += "  left join masteritem mitm on mitm.id=gdh.gstrtype ";
                joinSql += "  left join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            }
            String GSTVendorTypeQuery = " (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Composition) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Regular) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZ) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Unregistered) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Import) + "')) ";
            builder.append(" and ((gdh.custventypeid IS NULL or gdh.custventypeid='') or (" + GSTVendorTypeQuery+"))");
            GSTCustVendTypeSelectColumn = " ,gdh.custventypeid as custventypeid ";
            GSTTypeName = " ,mstrItem.value as gsttypename";
        }
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If vendor is Unregistered i.e. GSTIN is not present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If vendor is Registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("ishistoryblank") && reqParams.optBoolean("ishistoryblank")) {
            builder.append(" and inv.id NOT IN (select refdocid from gstdocumenthistory)");
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype";
            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("goodsreceiptentitycolnum")) {
            /**
             * Column no of Entity dimension's value stored in Invoice
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("goodsreceiptentitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("goodsreceiptentityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            builder.append(" and ptaxfcd.valuetype=?");
            String Type = reqParams.optString("taxClassType");
            params.add((int) FieldComboData.ValueTypeMap.get(Type));
        }
//        if (reqParams.has("isRCMApplicable")) {
//            /**
//             * Append condition for RCM Applicable or not
//             */
//            builder.append(" and inv.rcmapplicable=?");
//            boolean InvoiceType = reqParams.optBoolean("isRCMApplicable");
//            if (InvoiceType) {
//                params.add('T');
//            } else {
//                params.add('F');
//            }
//        }
        /*
          Flag for GST-calculation is based on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping= true;
        }
        String billingShippingPOS = (isShipping==false)?"bst.billingstate":"bst.shippingstate" ;
         /**
         * For India country and vendor transactions and
         * isAddressFromVendorMaster is off then vendor billing address is store
         * in separate key pair
         */
        boolean isAddressNotFromVendorMaster = reqParams.optBoolean(GSTRConstants.isAddressNotFromVendorMaster, false);
        if(isAddressNotFromVendorMaster){
            billingShippingPOS = (isShipping==false) ? " bst.vendorbillingstateforindia "  : " bst.vendcustshippingstate ";
        }
        if (reqParams.has("statemismatch") && reqParams.optBoolean("statemismatch")) {
            int goodsreceiptstatecolnum = reqParams.optInt("goodsreceiptstatecolnum");
            builder.append(" and ((jecust.col" + goodsreceiptstatecolnum + " is NULL or jecust.col" + goodsreceiptstatecolnum + "='')");
            builder.append(" or (").append(billingShippingPOS).append(" IS NULL or ").append(billingShippingPOS).append(" ='' ))");

        }
        if (reqParams.has("withoutseqformat") && reqParams.optBoolean("withoutseqformat")) {
            builder.append(" and inv.seqformat is NULL");

        }
         if(reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))){
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
         To perform Quick search by Inv.No or customer name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"inv.grnumber","c.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 2);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString,"and", searchcol);
                    builder.append(searchQuery);                 
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String groupby = "";
        String selectCol = " c.name as customerid,gdh.gstin,inv.id as invoiceid,inv.grnumber,je.id as jeid,je.entrydate,"
                + "invd.id as invoicedetailid,invd.rate,it.quantity,p.productid as productid,lt.term,ivtd.termamount,"
                + "inv.invoiceamountinbase,pfcd.value as hsncode,ptaxfcd.value as pos," + col + ",sr.prnumber as posid,"
                + "ivtd.percentage as taxrate,lt.defaultterms,d.inpercent as discPercentage,d.discount as discountValue, '0' as code,"
                + "ivtd.percentage as termpercentage,eltr.percentage as rulepercentage,"
                + "ivtd.taxtype " + GSTRegTypeSelectColumn + GSTCustVendTypeSelectColumn + GSTTypeName;

        joinSql += " left join discount d on d.id = invd.discount ";
        
        String query = " select " + selectCol + " from goodsreceipt inv inner join grdetails invd on inv.id=invd.goodsreceipt and inv.company=?"
                + " inner join journalentry je on je.id=inv.journalentry "
                + " inner join vendor c on c.id=inv.vendor "
                + " left join gstdocumenthistory gdh on inv.id=gdh.refdocid and gdh.moduleid in ('6','39') \n "
                + " inner join gsttaxclasshistory gtch on invd.id=gtch.refdocid and gtch.moduleid in ('6','39') \n"
//                + " inner join gstvendorhistory gvh on c.id=gvh.vendor"
                + " left join prdetails srd on srd.videtails=invd.id "
                + " left join purchasereturn sr on sr.id=srd.purchasereturn "
                + " inner join inventory it on it.id=invd.id "
                + " inner join product p on p.id=it.product "
                + " left join receiptdetailtermsmap ivtd on ivtd.goodsreceiptdetail=invd.id "
                + " left join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + " inner join billingshippingaddresses bst on bst.id=inv.billingshippingaddresses"
                + "  left join linelevelterms lt on lt.id=eltr.linelevelterms"
                + " left join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 "
                + " left join accproductcustomdata pcd on pcd.productId=p.id "
                + " left join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + ""
                + " left join fieldcombodata ptaxfcd on ptaxfcd.id=pcd.col" + taxclasscolnum + ""
                + " " + joinSql + builder + groupby + having + orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    public List getCNAgainstCustomer(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where cn.company=? and cn.deleteflag='F' and cn.approvestatuslevel=11 ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Unregistered i.e. GSTIN is present
                 */

                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("entitycolnum")) {
            /**
             * Column no of Entity Value stored in CN
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=cnje.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (cnje.entrydate>=? and cnje.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype ";
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            
            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            String Type = reqParams.optString("taxClassType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and  ptaxfcd.valuetype in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    } else {
                        conditionBuilder.append(",?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and ptaxfcd.valuetype=?");
                params.add((int) FieldComboData.ValueTypeMap.get(Type));
            }
        }   
        /*
          Flag for GST-calculation on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping= true;
        }
        String billingShippingPOS = (isShipping==false)?"bst.billingstate":"bst.shippingstate" ;
        String localState = (!StringUtil.isNullOrEmpty(reqParams.optString("localState")) ? reqParams.optString("localState") : "");
            
            if (reqParams.has("interstate")) {
                /**
                 * Return only Inter state i.e. IGST records
                 */
                 builder.append(" and ").append(billingShippingPOS).append("<>? ");
                params.add(localState);
            } else if (reqParams.has("intrastate")) {
                /**
                 * Return only Intra state i.e. CGST and SGST records
                 */
                builder.append(" and ").append(billingShippingPOS).append("=? ");
                params.add(localState);
            }
            /*
                State-wise record fetching in GSTR1-Detail Level  
            */
        if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        if (reqParams.has("greaterlimit") && reqParams.has("limitamount")) {
            /**
             * CN taxable value limit
             */
            if (reqParams.optBoolean("greaterlimit") && reqParams.optBoolean("exportwithoutlimit")) {
                /**
                 * CN Amount greater than limit i.e 2.5 Lac excluding export.
                 * No Amount limit for export type of CN
                 * type (No amount limit for Export type)
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and (cn.cnamountinbase>? or mstrItem.defaultmasteritem in (?,?)) ");
                params.add(limit);
                params.add(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Export).toString());
                params.add(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_ExportWOPAY).toString());
            } else if (reqParams.optBoolean("greaterlimit")) {
                /**
                 * CN Amount greater than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and cn.cnamountinbase > ? ");
                params.add(limit);
            } else {
                /**
                 * CN Amount less than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and cn.cnamountinbase <=? ");
                params.add(limit);
            }
        }
        if (reqParams.optBoolean("isb2cs")) {
            /**
             * a) Intra-State: any value b) Inter-State: Invoice value Rs 2.5
             * lakh or less
             */
            double limit = reqParams.optDouble("limitamount");
            builder.append(" and ((" + billingShippingPOS + "<>? and cn.cnamountinbase<=? )or (" + billingShippingPOS + "=?))");
            params.add(localState);
            params.add(limit);
            params.add(localState);
        }
        int hsncolnum = reqParams.optInt("hsncolnum");
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Sales Return Detail=srd Credit Note = cn Credit Note details = cnd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String selectCol = "";
        if (reqParams.has("cdnur") && reqParams.optBoolean("cdnur")) {
            selectCol = " c.id as customerid,c.gstin,cn.id as invoiceid,GROUP_CONCAT(inv.invoicenumber separator ','),"
                    + "je.id as jeid,je.entrydate,cnt.id as invoicedetailid,(IF(cnje.externalcurrencyrate>0,(cnt.amount/cnje.externalcurrencyrate),cnt.amount)) as rate,1.0 as quantity,"
                    + "'' as productid,lt.term,(IF(cnje.externalcurrencyrate>0,(cndtm.termamount/cnje.externalcurrencyrate),cndtm.termamount)), cn.cnamountinbase, "
                    + "'' as hsncode,"+ billingShippingPOS +" as pos,'' as productdescription,shl1.id as posid,"
                    + "cndtm.percentage as taxrate,lt.defaultterms,cn.cnnumber,cnje.entrydate as cndndate,0 as discpercentage,"
                    + "0.0 as discountvalue,cn.id as cnid,cn.cnamountinbase as cnamountinbase, shl1.itemdescription,c.name as customername,mstrItem.value as gstcusttype,''";
        } else if (reqParams.has("isb2cs") && reqParams.optBoolean("isb2cs")) {
            selectCol = " c.id as customerid,c.gstin,cn.id as invoiceid,GROUP_CONCAT(inv.invoicenumber separator ','),"
                    + "je.id as jeid,je.entrydate,cnt.id as invoicedetailid,(IF(cnje.externalcurrencyrate>0,(cnt.amount/cnje.externalcurrencyrate),cnt.amount)) as rate,1.0 as quantity,"
                    + "'' as productid,lt.term,(IF(cnje.externalcurrencyrate>0,(cndtm.termamount/cnje.externalcurrencyrate),cndtm.termamount)), cn.cnamountinbase, "
                    + "'' as hsncode," + billingShippingPOS + " as pos,'' as productdescription,shl1.id as posid,"
                    + "cndtm.percentage as taxrate,lt.defaultterms,cn.cnnumber,cnje.entrydate as cndndate,0 as discpercentage,"
                    + "0.0 as discountvalue,cn.id as cnid,cn.cnamountinbase as cnamountinbase, shl1.itemdescription,c.name as customername,mstrItem.value as gstcusttype,''";
        } else {
            selectCol = " c.id as customerid,c.gstin,cn.id as invoiceid,GROUP_CONCAT(inv.invoicenumber separator ','),"
                    + "je.id as jeid,je.entrydate,cnt.id as invoicedetailid,(IF(cnje.externalcurrencyrate>0,(cnt.amount/cnje.externalcurrencyrate),cnt.amount)) as rate,1.0 as quantity,"
                    + "'' as productid,lt.term,(IF(cnje.externalcurrencyrate>0,(cndtm.termamount/cnje.externalcurrencyrate),cndtm.termamount)), cn.cnamountinbase, "
                    + "'' as hsncode," + billingShippingPOS + " as pos,'' as productdescription,shl1.id as posid,"
                    + "cndtm.percentage as taxrate,lt.defaultterms,cn.cnnumber,cnje.entrydate as cndndate,0 as discpercentage,"
                    + "0.0 as discountvalue, shl1.itemdescription,c.name as customername,''";
        }
        String groupby = "", orderby = " ORDER by cn.cnnumber ";
        String cnInvoiceMappingInfo = " left join creditnoteinvoicemappinginfo cnmi on cnmi.creditnote = cn.id \n"
                + "left join invoice inv on inv.id = cnmi.invoice\n"
                + "left join journalentry je on je.id = inv.journalentry ";
        if (reqParams.has("isDocumentDetails") && reqParams.optBoolean("isDocumentDetails")) {
            /**
             * Used for Document Details report
             *
             */
            selectCol = " cn.cnnumber,cn.seqformat,cn.seqnumber";
            groupby += " group by cn.id ";
            joinSql += " inner join sequenceformat sq on cn.seqformat=sq.id ";
            orderby = " ORDER BY cn.seqformat,cn.seqnumber ";

        } else if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
            /**
             * Used for GST3B report i.e. select sum of all CN taxable and Tax
             * amount (Separate for each GST type i.e. CGST,IGST etc)
             *
             */
            selectCol = "sum((IF(cnje.externalcurrencyrate>0,(cndtm.termamount/cnje.externalcurrencyrate),cndtm.termamount))),lt.defaultterms,"
                    + "sum((IF(cnje.externalcurrencyrate>0,(cnt.amount/cnje.externalcurrencyrate),cnt.amount))) "
                    + ",sum(cn.cnamountinbase)";
            groupby = " group by lt.defaultterms ";
            cnInvoiceMappingInfo = "";
        } else {
            groupby += " group by lt.id,cnt.id,cn.id ";
        }
        if (reqParams.has("zerorated")) {
            if (reqParams.optBoolean("zerorated")) {
                /**
                 * Condition for such CN having GST rates Zero
                 */
                builder.append(" and cndtm.termamount=0");
            } else {
                /**
                 * Condition for such CN having GST rates Zero
                 */
                builder.append(" and cndtm.termamount>0");
            }
        }        
        builder.append(" and cn.salesreturn is null ");
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            builder.append(" and ( cn.cnnumber like '%").append(searchString).append("%' or c.name like '%").append(searchString).append("%')");
        }
        int cnreasoncol = reqParams.optInt("cnreasoncol", 0);
        if (cnreasoncol != 0) {
            selectCol += " ,cnreasonfcd.value as cnreason ";
            joinSql += " left join fieldcombodata cnreasonfcd on cnreasonfcd.id=jecust.col" + cnreasoncol + "";

        }
        String query = " select " + selectCol + " from cntaxentry cnt \n"
                + "inner join creditnote cn on cn.id = cnt.creditnote and cn.company=? \n"
                + " inner join gsttaxclasshistory gtch on cnt.id=gtch.refdocid and gtch.moduleid=12 \n"
                + " inner join gstdocumenthistory gdh on cn.id=gdh.refdocid and gdh.moduleid=12 \n"
                + "inner join journalentry cnje on cnje.id = cn.journalentry\n"
                + "left join creditnotedetailtermmap cndtm on cndtm.creditnotetaxentry= cnt.id \n"
                + "left join entitybasedlineleveltermsrate eltr on eltr.id=cndtm.entityterm\n"
                + "left join linelevelterms lt on lt.id=eltr.linelevelterms \n"
                + "left join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 \n"
                + " left join product p on p.id=cnt.productid "
                + " left join accproductcustomdata pcd on pcd.productId=p.id "
                + " left join fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass "
                + "inner join customer c on c.id = cn.customer \n"
                + " inner join billingshippingaddresses bst on bst.id=cn.billingshippingaddresses "
                + cnInvoiceMappingInfo + joinSql + builder + groupby + orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * Get GST Column mapping number for Location Dimensions
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    @Override
    public List getGSTMappingColnumOfLocationDimension(JSONObject paramsObj) throws ServiceException {
        List list = null;
        int moduleid = paramsObj.optInt(Constants.moduleid);
        String fieldlabel = paramsObj.optString(Constants.fieldlabel, "");
        String companyid = paramsObj.optString(Constants.companyid, "");
        int customcolumn = paramsObj.optInt(Constants.customcolumn, 0);
        List params = new ArrayList();
        String hql = "select fieldlabel,GSTMappingColnum from FieldParams where moduleid=? and fieldlabel in (" + fieldlabel + ") and company.companyID=? and customcolumn=? ";
        params.add(moduleid);
        params.add(companyid);
        params.add(customcolumn);
        list = executeQuery(hql, params.toArray());
        return list;
    }//
    /**
     * @param json
     * @return
     * @Desc : Method to save GSTR2 submission data from GSTR2A-Match And
     * Reconcile Window
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject saveGSTR2Submission(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        try {
            GSTR2Submission gSTR2Submission = new GSTR2Submission();
            if (!StringUtil.isNullOrEmpty(json.optString("id"))) {
                gSTR2Submission = (GSTR2Submission) get(GSTR2Submission.class, json.optString("id"));
            }
            if (json.has("creationDate") && json.get("creationDate") != null) {
                gSTR2Submission.setCreationDate((Date) json.opt("creationDate"));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("flag"))) {
                gSTR2Submission.setFlag(json.optString("flag"));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("gstRegNumber"))) {
                gSTR2Submission.setGstRegNumber(json.optString("gstRegNumber"));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("invoiceid"))) {
                gSTR2Submission.setInvoiceid(json.optString("invoiceid"));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("transactionJson"))) {
                gSTR2Submission.setTransactionJson(json.optString("transactionJson"));
            }
            if (json.has("supplierInvoiceNo")) {
                gSTR2Submission.setSupplierInvoiceNo(json.optString("supplierInvoiceNo"));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("entityid"))) {
                gSTR2Submission.setEntityid(json.optString("entityid"));
            }
            if (json.has("type")) {
                gSTR2Submission.setType(json.optInt("type", 0));
            }
            if (json.has("month")) {
                gSTR2Submission.setMonth(json.optInt("month", 0));
            }
            if (json.has("year")) {
                gSTR2Submission.setYear(json.optInt("year", 0));
            }
            if (json.has("systemTransaction")) {
                gSTR2Submission.setSystemTransaction((Boolean) json.get("systemTransaction"));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("companyid"))) {
                gSTR2Submission.setCompany((Company) get(Company.class, json.optString("companyid")));
            }
            saveOrUpdate(gSTR2Submission);
            list.add(gSTR2Submission);
        } catch (JSONException | ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    /**
     * @param json
     * @return
     * @Desc : Method to save GSTR2 submission data from GSTR2A-Match And
     * Reconcile Window
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getGSTR2Submission(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        try {
            List params = new ArrayList();
            StringBuilder hqlQuery = new StringBuilder();
            hqlQuery.append(" from GSTR2Submission where company.companyID = ? ");
            params.add(json.optString("companyid"));
            if (json.has("month")) {
                hqlQuery.append(" and month = ? ");
                params.add(json.optInt("month", 0));
            }
            if (json.has("year")) {
                hqlQuery.append(" and year = ? ");
                params.add(json.optInt("year", 0));
            }
            if (json.has("type")) {
                hqlQuery.append(" and type = ? ");
                params.add(json.optInt("type", 0));
            }
            if (json.has("supplierInvoiceNo")) {
                hqlQuery.append(" and supplierInvoiceNo = ? ");
                params.add(json.optString("supplierInvoiceNo", ""));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("invoiceid", ""))) {
                hqlQuery.append(" and invoiceid = ? ");
                params.add(json.optString("invoiceid", ""));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("gstRegNumber", ""))) {
                hqlQuery.append(" and gstRegNumber = ? ");
                params.add(json.optString("gstRegNumber", ""));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("entityid", ""))) {
                hqlQuery.append(" and entityid = ? ");
                params.add(json.optString("entityid", ""));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("flag", ""))) {
                hqlQuery.append(" and flag = ? ");
                params.add(json.optString("flag", ""));
            }
            if (json.has("systemTransaction")) {
                hqlQuery.append(" and systemTransaction = ? ");
                params.add((Boolean) json.get("systemTransaction"));
            }
            if (json.optBoolean("isNotNulljsontobeuploaded", false)) {
                hqlQuery.append(" and jsonToBeUploaded is not null ");
            }
            list = executeQuery(hqlQuery.toString(), params.toArray());
        } catch (ServiceException|JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    /**
     * Get GST CESS Calculation type
     * @param json
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject getCESSCalculationType(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        try {
            List params = new ArrayList();
            StringBuilder hqlQuery = new StringBuilder();
            String condition = "";
            String name =  json.optString("name", "");
            if (!StringUtil.isNullOrEmpty(name)) {
                if (condition.trim().length() > 0) {
                    condition += " and name= ? ";
                } else {
                    condition = " where name= ? ";
                }
                params.add(name);
            }
            hqlQuery.append(" from GSTCessRuleType " + condition);
            list = executeQuery(hqlQuery.toString(), params.toArray());
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public GSTR2Submission saveOrGetGSTR2Submission(JSONObject json) throws ServiceException {
        GSTR2Submission gstr2Submission = null;
        try {
            KwlReturnObject submissionResult = getGSTR2Submission(json);
            if (submissionResult != null && submissionResult.getEntityList() != null && !submissionResult.getEntityList().isEmpty() && submissionResult.getEntityList().get(0) != null) {
                gstr2Submission = (GSTR2Submission)submissionResult.getEntityList().get(0);
            }
            if (gstr2Submission == null) {
                submissionResult = saveGSTR2Submission(json);
                if (submissionResult != null && submissionResult.getEntityList() != null && !submissionResult.getEntityList().isEmpty() && submissionResult.getEntityList().get(0) != null) {
                    gstr2Submission = (GSTR2Submission) submissionResult.getEntityList().get(0);
                }
            } else {
                if (!StringUtil.isNullOrEmpty(json.optString("transactionJson"))) {
                    gstr2Submission.setTransactionJson(json.optString("transactionJson"));
                }  
                if (!StringUtil.isNullOrEmpty(json.optString("supplierInvoiceNo"))) {
                    gstr2Submission.setSupplierInvoiceNo(json.optString("supplierInvoiceNo"));
                }  
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return gstr2Submission;
    }
    

    @Override
    public List getDNAgainstCustomer(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where dn.company=? and dn.deleteflag='F' and dn.approvestatuslevel=11 ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Unregistered i.e. GSTIN is present
                 */

                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("entitycolnum")) {
            /**
             * Column no of Entity Value stored in CN
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=dnje.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (dnje.entrydate>=? and dnje.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype ";
         if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            
            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
         joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            String Type = reqParams.optString("taxClassType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and  ptaxfcd.valuetype in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    } else {
                        conditionBuilder.append(",?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and ptaxfcd.valuetype=?");
                params.add((int) FieldComboData.ValueTypeMap.get(Type));
            }

        }
        /*
          Flag for GST-calculation on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping = true;
        }
        String billingShippingPOS = (isShipping == false) ? "bst.billingstate" : "bst.shippingstate";
        String localState = (!StringUtil.isNullOrEmpty(reqParams.optString("localState")) ? reqParams.optString("localState") : "");

        if (reqParams.has("interstate")) {
            /**
             * Return only Inter state i.e. IGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("<>? ");
            params.add(localState);
        } else if (reqParams.has("intrastate")) {
            /**
             * Return only Intra state i.e. CGST and SGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("=? ");
            params.add(localState);
        }
            /*
                State-wise record fetching in GSTR1-Detail Level  
            */
        if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        if (reqParams.has("greaterlimit") && reqParams.has("limitamount")) {
            /**
             * CN taxable value limit
             */
             if (reqParams.optBoolean("greaterlimit") && reqParams.optBoolean("exportwithoutlimit")) {
                /**
                 * CN Amount greater than limit i.e 2.5 Lac excluding export.
                 * No Amount limit for export type of CN
                 * type (No amount limit for Export type)
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and (dn.dnamountinbase>? or mstrItem.defaultmasteritem in (?,?)) ");
                params.add(limit);
                params.add(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Export).toString());
                params.add(Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_ExportWOPAY).toString());
            } else if (reqParams.optBoolean("greaterlimit")) {
                /**
                 * CN Amount greater than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and dn.dnamountinbase>? ");
                params.add(limit);
            } else {
                /**
                 * CN Amount less than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and dn.dnamountinbase<=? ");
                params.add(limit);
            }
        }
        if (reqParams.optBoolean("isb2cs")) {
            /**
             * a) Intra-State: any value b) Inter-State: Invoice value Rs 2.5
             * lakh or less
             */
            double limit = reqParams.optDouble("limitamount");
            builder.append(" and ((" + billingShippingPOS + "<>? and dn.dnamountinbase<=? )or (" + billingShippingPOS + "=?))");
            params.add(localState);
            params.add(limit);
            params.add(localState);
        }        
        int hsncolnum = reqParams.optInt("hsncolnum");
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Sales Return Detail=srd Credit Note = cn Credit Note details = cnd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String selectCol = "";
        if (reqParams.has("cdnur") && reqParams.optBoolean("cdnur")) {
            selectCol = "c.id as vendorid,c.gstin,dn.id as invoiceid,"
                    + "GROUP_CONCAT(inv.invoicenumber separator ','),je.id as jeid,je.entrydate,dnt.id as grdetailid,"
                    + "(IF(dnje.externalcurrencyrate>0,(dnt.amount/dnje.externalcurrencyrate),dnt.amount)) as rate,1.0 as quantity,'' as productid,lt.term,(IF(dnje.externalcurrencyrate>0,(dndtm.termamount/dnje.externalcurrencyrate),dndtm.termamount)), dn.dnamountinbase, "
                    + "'' as hsncode,"+ billingShippingPOS +" as pos,'' as productdescription,shl1.id as posid,dndtm.percentage as taxrate,"
                    + "lt.defaultterms,dn.dnnumber,dnje.entrydate as cndndate,0 as discpercentage,0.0 as discountvalue,"
                    + "dn.id as cnid,dn.dnamountinbase as cnamountinbase, shl1.itemdescription,c.name as customername,mstrItem.value as gstcusttype,'D' as doctype";
        } else if (reqParams.has("isb2cs") && reqParams.optBoolean("isb2cs")) {
            selectCol = "c.id as vendorid,c.gstin,dn.id as invoiceid,"
                    + "GROUP_CONCAT(inv.invoicenumber separator ','),je.id as jeid,je.entrydate,dnt.id as grdetailid,"
                    + "(IF(dnje.externalcurrencyrate>0,(dnt.amount/dnje.externalcurrencyrate),dnt.amount)) as rate,1.0 as quantity,'' as productid,lt.term,(IF(dnje.externalcurrencyrate>0,(dndtm.termamount/dnje.externalcurrencyrate),dndtm.termamount)), dn.dnamountinbase, "
                    + "'' as hsncode,"+ billingShippingPOS +" as pos,'' as productdescription,shl1.id as posid,dndtm.percentage as taxrate,"
                    + "lt.defaultterms,dn.dnnumber,dnje.entrydate as cndndate,0 as discpercentage,0.0 as discountvalue,"
                    + "dn.id as cnid,dn.dnamountinbase as cnamountinbase, shl1.itemdescription,c.name as customername,mstrItem.value as gstcusttype,'D' as doctype";
        } else {
            selectCol = "c.id as vendorid,c.gstin,dn.id as invoiceid,GROUP_CONCAT(inv.invoicenumber separator ','),"
                    + "je.id as jeid,je.entrydate,dnt.id as grdetailid,(IF(dnje.externalcurrencyrate>0,(dnt.amount/dnje.externalcurrencyrate),dnt.amount)) as rate,1.0 as quantity,"
                    + "'' as productid,lt.term,(IF(dnje.externalcurrencyrate>0,(dndtm.termamount/dnje.externalcurrencyrate),dndtm.termamount)), dn.dnamountinbase, '' as hsncode,"+ billingShippingPOS +" as pos,"
                    + "'' as productdescription,shl1.id as posid,dndtm.percentage as taxrate,lt.defaultterms,"
                    + "dn.dnnumber,dnje.entrydate as cndndate,0 as discpercentage,0.0 as discountvalue, shl1.itemdescription,c.name as customername,'D' as doctype";
        }
        String groupby = "", orderby = " ORDER by dn.dnnumber";
        String dnInvoiceMappingInfo = " left join debitnoteinvoicemappinginfo dnmi on dnmi.debitnote = dn.id \n"
                + "left join invoice inv on inv.id = dnmi.invoice\n"
                + "left join journalentry je on je.id = inv.journalentry ";
        if (reqParams.has("isDocumentDetails") && reqParams.optBoolean("isDocumentDetails")) {
            /**
             * Used for Document Details report
             *
             */
            selectCol = " dn.dnnumber,dn.seqformat,dn.seqnumber";
            groupby += " group by dn.id ";
            joinSql += " inner join sequenceformat sq on dn.seqformat=sq.id ";
            orderby = " ORDER BY dn.seqformat,dn.seqnumber ";

        } else if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
            /**
             * Used for GST3B report i.e. select sum of all CN taxable and Tax
             * amount (Separate for each GST type i.e. CGST,IGST etc)
             *
             */
//            selectCol = "sum(ivtd.termamount),lt.defaultterms,sum((prd.rate*prd.returnquantity)) ";

            selectCol = "sum((IF(dnje.externalcurrencyrate>0,(dndtm.termamount/dnje.externalcurrencyrate),dndtm.termamount))),lt.defaultterms,"
                    + "sum((IF(dnje.externalcurrencyrate>0,(dnt.amount/dnje.externalcurrencyrate),dnt.amount))) "
                    + ",sum(dn.dnamountinbase)";

            groupby = " group by lt.defaultterms ";

            dnInvoiceMappingInfo = "";
        } else {
            groupby += " group by lt.id,dnt.id,dn.id ";
        }
        if (reqParams.has("zerorated")) {
            if (reqParams.optBoolean("zerorated")) {
                /**
                 * Condition for such CN having GST rates Zero
                 */
                builder.append(" and dndtm.termamount=0");
            } else {
                /**
                 * Condition for such CN having GST rates Zero
                 */
                builder.append(" and dndtm.termamount>0");
            }
        }
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            builder.append(" and ( dn.dnnumber like '%").append(searchString).append("%' or c.name like '%").append(searchString).append("%')");
        }
        int dnreasoncol = reqParams.optInt("dnreasoncol", 0);
        if (dnreasoncol != 0) {
            selectCol += " ,dnreasonfcd.value as dnreason ";
            joinSql += " left join fieldcombodata dnreasonfcd on dnreasonfcd.id=jecust.col" + dnreasoncol + "";

        }
        String query = " select " + selectCol + " from dntaxentry dnt \n"
                + "inner join debitnote dn on dn.id = dnt.debitnote  and dn.company=? \n"
                + " inner join gsttaxclasshistory gtch on dnt.id=gtch.refdocid and gtch.moduleid in ('10','12') \n"
                + " inner join gstdocumenthistory gdh on dn.id=gdh.refdocid and gdh.moduleid in ('10','12') \n"
                + "inner join journalentry dnje on dnje.id = dn.journalentry\n"
                + "left join debitnotedetailtermmap dndtm on dndtm.debitnotetaxentry= dnt.id \n"
                + "left join entitybasedlineleveltermsrate eltr on eltr.id=dndtm.entityterm\n"
                + "left join linelevelterms lt on lt.id=eltr.linelevelterms \n"
                + "left join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 \n"
                + " left join product p on p.id=dnt.productid "
                + " left join accproductcustomdata pcd on pcd.productId=p.id "
                + " left join fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass "
                + "inner join customer c on c.id = dn.customer \n"
                + " inner join billingshippingaddresses bst on bst.id=dn.billingshippingaddresses "
                + dnInvoiceMappingInfo + joinSql + builder + groupby + orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * Function to get Customer/Vendor GST fields History based on transaction date.
     * @param reqMap
     * @return
     * @throws ServiceException 
     */
    public List getMasterHistoryForGSTFields(Map<String, Object> reqMap) throws ServiceException {
        String query = "select id from ";
        List params = new ArrayList();
        String condition = "";
        if (reqMap.containsKey("isCustomer")) {
            boolean isCustomer = (boolean) reqMap.get("isCustomer");
            if (isCustomer) {
                params.add(reqMap.get("masterid").toString());
                condition += " where customer=?";
                query += " gstcustomerhistory";
            } else {
                params.add(reqMap.get("masterid").toString());
                condition += " where vendor=?";
                query += " gstvendorhistory";
            }
        }

        if (reqMap.containsKey("applydate")) {
            condition += " and ((applydate>? and applydate<=? ) or (applydate>? and applydate<=?))";
            Date applyDate = (Date) reqMap.get("applydate");
            Date transactiondate = (Date) reqMap.get("transactiondate");
            params.add(applyDate);
            params.add(transactiondate);
            params.add(transactiondate);
            params.add(applyDate);
        }
        condition += " limit 1 ";
        List returnList = executeSQLQuery(query + condition, params.toArray());
        return returnList;
    }
/**
 * Function to get Product tax class history within date range
 * @param reqMap
 * @return
 * @throws ServiceException 
 */
    public List getTaxClassHistoryForGSTFields(Map<String, Object> reqMap) throws ServiceException {
        String query = "select id from productcustomfieldhistory ";
        List params = new ArrayList();
        String condition = "";

        if (reqMap.containsKey("applydate")) {
            condition += " where ((applydate>? and applydate<=? ) or (applydate>? and applydate<=?))";
            Date applyDate = (Date) reqMap.get("applydate");
            Date transactiondate = (Date) reqMap.get("transactiondate");
            params.add(applyDate);
            params.add(transactiondate);
            params.add(transactiondate);
            params.add(applyDate);
        }
        if (reqMap.containsKey("productids")) {
            String ids = reqMap.get("productids").toString();
            condition += " and product in (" + ids + ")";
        }
        condition += " limit 1 ";
        List returnList = executeSQLQuery(query + condition, params.toArray());
        return returnList;
    }
    /**
     * Function to get field name,column no gstconfig and other gst ralated data
     * from field params
     *
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getGSTDimensionsDetailsForGSTCalculations(JSONObject params) throws ServiceException, JSONException {
        String companyid = params.optString("companyid");
        int moduleid = params.optInt("moduleid");
        String query = " select fieldname,colnum,gstconfigtype,gstmappingcolnum from fieldparams where companyid='" + companyid + "' and gstconfigtype in ('1','3') and moduleid=" + moduleid;
        List returnList = executeSQLQuery(query);
        return returnList;
    }

    /**
     * Function to get custom data from documents custom table.
     *
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getGSTDimensionDataFromCustomTableForGSTCalculations(JSONObject params) throws ServiceException, JSONException {
        String companyid = params.optString("companyid");
        String selectstate = params.optString("selectstate");
        String selectentity = params.optString("selectentity");
        String customtable = params.optString("customtable");
        String primarykey = params.optString("primarykey");
        String primaryid = params.optString("primaryid");
        String query = " select enfcd.value as entity,stfcd.value as state from " + customtable + " acd inner join fieldcombodata enfcd on enfcd.id=acd." + selectentity + " "
                + " inner join fieldcombodata stfcd on stfcd.id=acd." + selectstate + " where " + primarykey + " ='" + primaryid + "'";
        List returnList = executeSQLQuery(query);
        return returnList;
    }
    /**
     * Execute custom build  select query 
     * @param query
     * @param queryParams
     * @return 
     */
    public List executeCustomBuildQuery(String query, List queryParams) throws ServiceException{
        List returnList = executeSQLQuery(query, queryParams.toArray());
        return returnList;
    }
    /**
     * Function to get Sales order in mismatch report.
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getGSTMissingSalesOrder(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        String having = "";
        String orderby = "";
        String additionalSelectCol = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        String col = "p.description as description";
        int hsncolnum = reqParams.optInt("hsncolnum");
        int uqccolnum = reqParams.optInt("uqccolnum");
        int taxclasscolnum = reqParams.optInt("taxclasscolnum");
        builder.append(" where so.company=? and so.deleteflag='F' and so.isdraft = false  and so.approvestatuslevel = 11 and so.istemplate!=2 ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("ishsnblank") && reqParams.optBoolean("ishsnblank")) {
            builder.append(" and (pcd.col" + hsncolnum + " is NULL or pcd.col" + hsncolnum + "='' or (LENGTH(pfcd.value) > 8))");
            col = "pcd.col" + hsncolnum;
        }
        if (reqParams.has("isuqcblank") && reqParams.optBoolean("isuqcblank")) {
            builder.append(" and (pcd.col" + uqccolnum + " is NULL or pcd.col" + uqccolnum + "='' )");
            col = "pcd.col" + uqccolnum;
        }
        /**
         * Below query for GST Registration blank and Wrong GST registration
         * tagged. Example - Other than Regular, Composition and Unregistered is
         * wrong GST Registration type ,Please ref.ERP-35464 for more details
         */
        String GSTRegTypeSelectColumn = " ,'' as gstrtype";
        String GSTTypeName = " ,'' as gsttypename";
        if (reqParams.has("isgstregtypeblank") && reqParams.optBoolean("isgstregtypeblank")) {
            if (!reqParams.has("registrationType")) {
                joinSql += " left join masteritem mitm on mitm.id=gdh.gstrtype ";
            }
            String GSTRegTypeQuery = "'" + Constants.GSTRegType.get(Constants.GSTRegType_Composition) + "',";
            GSTRegTypeQuery += "'" + Constants.GSTRegType.get(Constants.GSTRegType_Regular) + "',";
            GSTRegTypeQuery += "'" + Constants.GSTRegType.get(Constants.GSTRegType_Unregistered) + "'";
            builder.append(" and ((gdh.gstrtype IS NULL or gdh.gstrtype='') or (mitm.defaultmasteritem not in(" + GSTRegTypeQuery + ")))");
            GSTRegTypeSelectColumn = " ,gdh.gstrtype as gstrtype ";
            GSTTypeName = " ,mitm.value as gsttypename";
        }
        /**
         * Below query for GST Customer blank and Wrong GST registration Please
         * ref.ERP-35464 for more details
         */
        String GSTCustVendTypeSelectColumn = " ,'' as custventypeid";
        if (reqParams.has("iscusttypeblank") && reqParams.optBoolean("iscusttypeblank")) {
            if (!reqParams.has("CustomerType") && !reqParams.has("registrationType")) {
                joinSql += " left join masteritem mitm on mitm.id=gdh.gstrtype ";
                joinSql += " left join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            }
            String GSTCustomerTypeQuery = " (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Composition) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Regular) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZ) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZWOPAY) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Unregistered) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Export) + "','" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_ExportWOPAY) + "')) ";
            builder.append(" and ((gdh.custventypeid IS NULL or gdh.custventypeid='') or (" + GSTCustomerTypeQuery + "))");
            GSTCustVendTypeSelectColumn = " ,gdh.custventypeid as custventypeid ";
            GSTTypeName = " ,mstrItem.value as gsttypename";
        }
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is Unregistered i.e. GSTIN is not present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("ishistoryblank") && reqParams.optBoolean("ishistoryblank")) {
            builder.append(" and so.id not in (select refdocid from gstdocumenthistory where moduleid in ('20'))");
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype";
            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("soentitycolnum")) {
            /**
             * Column no of Entity dimension's value stored in Invoice
             */
            joinSql += " inner join salesordercustomdata socust on socust.soID=so.id ";
            int colnum = reqParams.optInt("soentitycolnum");
            builder.append(" and socust.col" + colnum + "=?");
            params.add(reqParams.optString("soentityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (so.orderdate>=? and so.orderdate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            builder.append(" and ptaxfcd.valuetype=?");
            String Type = reqParams.optString("taxClassType");
            params.add((int) FieldComboData.ValueTypeMap.get(Type));
        }

        /*
         Flag for GST-calculation is based on Billing Address or Shipping Address
         */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping = true;
        }
        String billingShippingPOS = (isShipping == false) ? "bst.billingstate" : "bst.shippingstate";
        if (reqParams.has("statemismatch") && reqParams.optBoolean("statemismatch")) {
            int sostatecolnum = reqParams.optInt("sostatecolnum");
            builder.append(" and ((socust.col" + sostatecolnum + " is NULL or socust.col" + sostatecolnum + "='')");
            builder.append(" or (").append(billingShippingPOS).append(" IS NULL or ").append(billingShippingPOS).append(" ='' ))");

        }
        if (reqParams.has("withoutseqformat") && reqParams.optBoolean("withoutseqformat")) {
            builder.append(" and so.seqformat is NULL");

        }
        if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
         To perform Quick search by Inv.No or customer name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"so.sonumber", "c.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 2);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    builder.append(searchQuery);
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        String groupby = "";
        String selectCol = " c.name as customerid,gdh.gstin,so.id as invoiceid,so.sonumber,'' as jeid,so.orderdate,"
                + "sod.id as invoicedetailid,sod.rate,sod.quantity,p.productid as productid,lt.term,ivtd.termamount,"
                + "so.totalamountinbase,pfcd.value as hsncode,ptaxfcd.value as pos," + col + ",'' as posid,"
                + "ivtd.percentage as taxrate,lt.defaultterms,'' as discPercentage,sod.discount as discountValue, '20' as code,"
                + "ivtd.percentage as termpercentage,eltr.percentage as rulepercentage "
                + ",ivtd.taxtype " + GSTRegTypeSelectColumn + GSTCustVendTypeSelectColumn + GSTTypeName;

        String query = " select " + selectCol + " from salesorder so inner join sodetails sod on so.id=sod.salesorder and so.company=?"
                + " left join gstdocumenthistory gdh on so.id=gdh.refdocid and gdh.moduleid in ('20') "
                + " inner join gsttaxclasshistory gtch on sod.id=gtch.refdocid and gtch.moduleid in ('20') "
                + " inner join customer c on c.id=so.customer "
                + " inner join product p on p.id=sod.product "
                + " inner join billingshippingaddresses bst on bst.id=so.billingshippingaddresses"
                + " left join salesorderdetailtermmap ivtd on ivtd.salesorderdetail=sod.id "
                + " left join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + " left join linelevelterms lt on lt.id=eltr.linelevelterms"
                + " left join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 "
                + " left join accproductcustomdata pcd on pcd.productId=p.id "
                + " left join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + ""
                + " left join fieldcombodata ptaxfcd on ptaxfcd.id=pcd.col" + taxclasscolnum + ""
                + " " + joinSql + builder + groupby + having + orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * Function to get Delivery order in mismatch report.
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public List getMissingDelieveryOrderData(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        String col = "p.description as description";
        int hsncolnum = reqParams.optInt("hsncolnum");
        int uqccolnum = reqParams.optInt("uqccolnum");
        int taxclasscolnum = reqParams.optInt("taxclasscolnum");
        builder.append(" where do.company=? and do.deleteflag='F'"); 
        params.add(reqParams.optString("companyid"));
                if (reqParams.has("ishsnblank") && reqParams.optBoolean("ishsnblank")) {
            builder.append(" and (pcd.col" + hsncolnum + " is NULL or pcd.col" + hsncolnum + "='' or (LENGTH(pfcd.value) > 8))");
            col = "pcd.col" + hsncolnum;
        }      
        if (reqParams.has("isuqcblank") && reqParams.optBoolean("isuqcblank")) {
            builder.append(" and (pcd.col" + uqccolnum + " is NULL or pcd.col" + uqccolnum + "='' )");
            col = "pcd.col" + uqccolnum;
        }
        /**
         * Below query for GST Registration blank and Wrong GST registration
         * tagged. Example - Other than Regular, Composition and Unregistered is
         * wrong GST Registration type ,Please ref.ERP-35464 for more details
         */
        String GSTRegTypeSelectColumn = " ,'' as gstrtype";
        String GSTTypeName = " ,'' as gsttypename";
        if (reqParams.has("isgstregtypeblank") && reqParams.optBoolean("isgstregtypeblank")) {
            if(!reqParams.has("registrationType")){
                joinSql+= " left join masteritem mitm on mitm.id=gdh.gstrtype ";
            }
            String GSTRegTypeQuery = "'"+Constants.GSTRegType.get(Constants.GSTRegType_Composition)+"',";
            GSTRegTypeQuery += "'"+Constants.GSTRegType.get(Constants.GSTRegType_Regular)+"',";
            GSTRegTypeQuery += "'"+Constants.GSTRegType.get(Constants.GSTRegType_Unregistered)+"'";
            builder.append(" and ((gdh.gstrtype IS NULL or gdh.gstrtype='') or (mitm.defaultmasteritem not in(" + GSTRegTypeQuery + ")))");
            GSTRegTypeSelectColumn = " ,gdh.gstrtype as gstrtype ";
            GSTTypeName = " ,mitm.value as gsttypename";
        }
        /**
         * Below query for GST Customer blank and Wrong GST registration
         * Please ref.ERP-35464 for more details
         */
        String GSTCustVendTypeSelectColumn = " ,'' as custventypeid";
        if (reqParams.has("iscusttypeblank") && reqParams.optBoolean("iscusttypeblank")) {
            if (!reqParams.has("CustomerType") && !reqParams.has("registrationType")) {
                joinSql += " left join masteritem mitm on mitm.id=gdh.gstrtype ";
                joinSql += " left join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            }
            String GSTCustomerTypeQuery = " (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Composition) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Regular) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZ) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZWOPAY) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Unregistered) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Export) + "','" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_ExportWOPAY) + "')) ";
            builder.append(" and ((gdh.custventypeid IS NULL or gdh.custventypeid='') or (" + GSTCustomerTypeQuery+"))");
            GSTCustVendTypeSelectColumn = " ,gdh.custventypeid as custventypeid ";
            GSTTypeName = " ,mstrItem.value as gsttypename";
        }
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is Unregistered i.e. GSTIN is not present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("ishistoryblank") && reqParams.optBoolean("ishistoryblank")) {
            builder.append(" and do.id not in (select refdocid from gstdocumenthistory where moduleid in ('27'))");
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql+= " inner join masteritem mitm on mitm.id=gdh.gstrtype";
            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql+= " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }       

        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            builder.append(" and ptaxfcd.valuetype=?");
            String Type = reqParams.optString("taxClassType");
            params.add((int) FieldComboData.ValueTypeMap.get(Type));
        }
        if (reqParams.has("isRCMApplicable")) {
            /**
             * Append condition for RCM Applicable or not
             */
            builder.append(" and do.rcmapplicable=?");
            boolean InvoiceType = reqParams.optBoolean("isRCMApplicable");
            if (InvoiceType) {
                params.add('T');
            } else {
                params.add('F');
            }
        }
         /*
          Flag for GST-calculation is based on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping= true;
        }
        String billingShippingPOS = (isShipping==false)?"bst.billingstate":"bst.shippingstate" ;
        if (reqParams.has("statemismatch") && reqParams.optBoolean("statemismatch")) {
            int dostatecolnum = reqParams.optInt("dostatecolnum");
            builder.append(" and ((docust.col" + dostatecolnum + " is NULL or docust.col" + dostatecolnum + "='')");
            builder.append(" or (").append(billingShippingPOS).append(" IS NULL or ").append(billingShippingPOS).append(" ='' ))");

        }
        if (reqParams.has("withoutseqformat") && reqParams.optBoolean("withoutseqformat")) {
            builder.append(" and do.seqformat is NULL");

        }
        if(reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))){
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
         To perform Quick search by Inv.No or customer name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"do.donumber","c.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 2);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    builder.append(searchQuery);                    
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
         if (reqParams.has("doentitycolnum")) {
    /**
             * Column no of Entity Value stored in Receipt
             */
            joinSql += " left join deliveryordercustomdata docust on docust.deliveryOrderId=do.id ";
            int colnum = reqParams.optInt("doentitycolnum");
            builder.append(" and docust.col" + colnum + "=?");
            params.add(reqParams.optString("doentityValue"));
        }
         if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (do.orderdate>=? and do.orderdate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         String typeofjoin = " inner ";
        if (reqParams.has("typeofjoinisleft") && reqParams.optBoolean("typeofjoinisleft")) {
            typeofjoin = " left ";
        }
    
        String groupby="",orderby="";
        String selectCol = " c.name as customerid,gdh.gstin,do.id as invoiceid,do.donumber,'' as jeid,do.orderdate,dodtl.id as invoicedetailid,dodtl.rate,dodtl.deliveredquantity,"
                +"p.productid as productid,lt.term,ivtd.termamount,do.totalamountinbase,pfcd.value as hsncode,ptaxfcd.value as pos,pcd.col4,"
                +"'' as posid,ivtd.percentage as taxrate,lt.defaultterms,'' as discPercentage,dodtl.discount as discountValue, '27' as code,"
                +"ivtd.percentage as termpercentage,eltr.percentage as rulepercentage ,ivtd.taxtype  ,'' as gstrtype ,'' as custventypeid ,'' as gsttypename  " 
                + GSTRegTypeSelectColumn + GSTCustVendTypeSelectColumn + GSTTypeName;

          joinSql += " left join sequenceformat sq on do.seqformat=sq.id ";
        String query = " select " + selectCol + " from dodetails dodtl inner join deliveryorder do on dodtl.deliveryorder=do.id "  
                + " left join gstdocumenthistory gdh on do.id=gdh.refdocid and gdh.moduleid in ('27') "
                + " inner join gsttaxclasshistory gtch on dodtl.id=gtch.refdocid and gtch.moduleid in ('27') " 
                + " inner join customer c on c.id=do.customer "
                + " inner join product p on p.id=dodtl.product "
                + " left  join deliveryorderdetailtermsmap ivtd on ivtd.dodetail=dodtl.id "
                + " left  join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + " left  join linelevelterms lt on lt.id=eltr.linelevelterms"
                + " left  join billingshippingaddresses bst on bst.id=do.billingshippingaddresses"
                + " left  join accproductcustomdata pcd on pcd.productId=p.id "
                + " left  join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + ""
                + " left  join fieldcombodata ptaxfcd on ptaxfcd.id=pcd.col" + taxclasscolnum + ""
                +joinSql+builder+groupby+orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
}
