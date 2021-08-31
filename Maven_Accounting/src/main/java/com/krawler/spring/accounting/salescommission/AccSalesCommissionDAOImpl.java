/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.salescommission;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccSalesCommissionDAOImpl extends BaseDAO implements AccSalesCommissionDAO {

    /**
     *
     * @param requestParams
     * @return KwlReturnObject
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject saveSalesCommissionSchemaMaster(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            SalesCommissionSchemaMaster schemaMaster = null;
            if (requestParams.containsKey(SalesCommissionSchemaMaster.SCHEMAMASTERID) && requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERID) != null) {
                schemaMaster = (SalesCommissionSchemaMaster) get(SalesCommissionSchemaMaster.class, requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERID).toString());
            } else {
                schemaMaster = new SalesCommissionSchemaMaster();
            }
            if (requestParams.containsKey(Constants.companyKey) && requestParams.get(Constants.companyKey) != null) {
                Company company = (Company) get(Company.class, requestParams.get(Constants.companyKey).toString());
                schemaMaster.setCompany(company);
            }
            if (requestParams.containsKey(SalesCommissionSchemaMaster.SCHEMAMASTERNAME) && requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERNAME) != null) {
                schemaMaster.setSchemaMaster(requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERNAME).toString());
            }
            saveOrUpdate(schemaMaster);
            List resultlist = new ArrayList();
            resultlist.add(schemaMaster);
            result = new KwlReturnObject(true, "", "", resultlist, resultlist.size());
        } catch (NumberFormatException | ServiceException ex) {
            throw ServiceException.FAILURE("saveSalesCommissionSchemaMaster" + ex.getMessage(), ex);
        }
        return result;
    }

    /**
     *
     * @param requestParams
     * @return SalesCommissionSchemaMasters
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getSalesCommissionSchemaMasters(Map<String, Object> requestParams) throws ServiceException {
        List schemaMasterList = null;
        try {
            ArrayList params = new ArrayList();
            String condition = "";
            if (requestParams.containsKey(Constants.companyKey) && requestParams.get(Constants.companyKey) != null) {
                params.add(requestParams.get(Constants.companyKey).toString());
            }
            if (requestParams.containsKey(SalesCommissionSchemaMaster.SCHEMAMASTERID) && requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERID) != null) {
                condition += " and id = ?";
                params.add(requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERID).toString());
            }
            if (requestParams.containsKey(SalesCommissionSchemaMaster.SCHEMAMASTERNAME) && requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERNAME) != null) {
                condition += " and schemaMaster = ?";
                params.add(Integer.parseInt(requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERNAME).toString()));
            }
            String hql = " from SalesCommissionSchemaMaster where company.companyID = ? " + condition;
            schemaMasterList = executeQuery(hql, params.toArray());
        } catch (NumberFormatException | ServiceException ex) {
            throw ServiceException.FAILURE("getSalesCommissionSchemaMasters" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, schemaMasterList, schemaMasterList.size());
    }

    /**
     *
     * @param jObj
     * @return KwlReturnObject of SalesCommissionRules
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject saveSalesCommissionRules(JSONObject jObj) throws ServiceException {
        KwlReturnObject result;
        try {
            SalesCommissionRules commissionRules = null;
            if (jObj.has(SalesCommissionRules.ID) && !StringUtil.isNullOrEmpty(jObj.optString(SalesCommissionRules.ID))) {
                commissionRules = (SalesCommissionRules) get(SalesCommissionRules.class, jObj.getString(SalesCommissionRules.ID));
            } else {
                commissionRules = new SalesCommissionRules();
            }
            if (jObj.has(Constants.companyKey) && !StringUtil.isNullOrEmpty(jObj.optString(Constants.companyKey))) {
                Company company = (Company) get(Company.class, jObj.getString(Constants.companyKey));
                commissionRules.setCompany(company);
            }
            if (jObj.has(SalesCommissionRules.AMOUNT) && !StringUtil.isNullOrEmpty(jObj.optString(SalesCommissionRules.AMOUNT))) {
                commissionRules.setAmount(jObj.getDouble(SalesCommissionRules.AMOUNT));
            }
            if (jObj.has(SalesCommissionRules.SCHEMATYPE) && !StringUtil.isNullOrEmpty(jObj.optString(SalesCommissionRules.SCHEMATYPE))) {
                commissionRules.setSchemaType(jObj.getInt(SalesCommissionRules.SCHEMATYPE));
            }
            if (jObj.has(SalesCommissionRules.RULEDESCRIPTION) && !StringUtil.isNullOrEmpty(jObj.optString(SalesCommissionRules.RULEDESCRIPTION))) {
                commissionRules.setRulesDescription(jObj.getString(SalesCommissionRules.RULEDESCRIPTION));
            }
            if (jObj.has(SalesCommissionRules.SCEHMAMASTER) && !StringUtil.isNullOrEmpty(jObj.optString(SalesCommissionRules.SCEHMAMASTER))) {
                SalesCommissionSchemaMaster schemaMaster = (SalesCommissionSchemaMaster) jObj.get(SalesCommissionRules.SCEHMAMASTER);
                commissionRules.setSchemaMaster(schemaMaster);
            }
            saveOrUpdate(commissionRules);
            List resultlist = new ArrayList();
            resultlist.add(commissionRules);
            result = new KwlReturnObject(true, "", "", resultlist, resultlist.size());
        } catch (JSONException | ServiceException ex) {
            throw ServiceException.FAILURE("saveSalesCommissionRules" + ex.getMessage(), ex);
        }
        return result;
    }

    /**
     *
     * @param jObj
     * @return KwlReturnObject of SalesCommissionRuleConditions
     * @throws ServiceException
     * @desc save SalesCommissionRuleConditions
     */
    @Override
    public KwlReturnObject saveSalesCommissionRuleConditions(JSONObject jObj) throws ServiceException {
        KwlReturnObject result;
        try {
            SalesCommissionRuleCondition ruleCondition = null;
            if (jObj.has(SalesCommissionRuleCondition.ID) && !StringUtil.isNullOrEmpty(jObj.optString(SalesCommissionRuleCondition.ID))) {
                ruleCondition = (SalesCommissionRuleCondition) get(SalesCommissionRuleCondition.class, jObj.getString(SalesCommissionRuleCondition.ID));
            }else{
                ruleCondition = new SalesCommissionRuleCondition();
            }
            if (jObj.has(SalesCommissionRuleCondition.COMMISSIONTYPE) && !StringUtil.isNullOrEmpty(jObj.optString(SalesCommissionRuleCondition.COMMISSIONTYPE))) {
                ruleCondition.setCommissionType(jObj.getInt(SalesCommissionRuleCondition.COMMISSIONTYPE));
            }
            if (jObj.has(SalesCommissionRuleCondition.CATEGORYID) && !StringUtil.isNullOrEmpty(jObj.optString(SalesCommissionRuleCondition.CATEGORYID))) {
                ruleCondition.setCategoryId(jObj.getString(SalesCommissionRuleCondition.CATEGORYID));
            }
            if (jObj.has(SalesCommissionRuleCondition.LOWERLIMIT) && !StringUtil.isNullOrEmpty(jObj.optString(SalesCommissionRuleCondition.LOWERLIMIT))) {
                ruleCondition.setLowerLimit(jObj.optDouble(SalesCommissionRuleCondition.LOWERLIMIT));
            }
            if (jObj.has(SalesCommissionRuleCondition.UPPERLIMIT) && !StringUtil.isNullOrEmpty(jObj.optString(SalesCommissionRuleCondition.UPPERLIMIT))) {
                ruleCondition.setUpperLimit(jObj.optDouble(SalesCommissionRuleCondition.UPPERLIMIT));
            }
            if (jObj.has(SalesCommissionRuleCondition.MARGINCONDITION) && !StringUtil.isNullOrEmpty(jObj.optString(SalesCommissionRuleCondition.MARGINCONDITION))) {
                ruleCondition.setMarginCondition(jObj.getInt(SalesCommissionRuleCondition.MARGINCONDITION));
            }
            if (jObj.has(SalesCommissionRules.RULEID) && !StringUtil.isNullOrEmpty(jObj.optString(SalesCommissionRules.RULEID))) {
                SalesCommissionRules commissionRules = (SalesCommissionRules) jObj.get(SalesCommissionRules.RULEID);
                ruleCondition.setCommissionRules(commissionRules);
            }
            if(jObj.has(Constants.companyKey) && !StringUtil.isNullOrEmpty(jObj.optString(Constants.companyKey))){
                Company company = (Company)get(Company.class, jObj.getString(Constants.companyKey));
                ruleCondition.setCompany(company);
            }

            saveOrUpdate(ruleCondition);
            List resultlist = new ArrayList();
            resultlist.add(ruleCondition);
            result = new KwlReturnObject(true, "", "", resultlist, resultlist.size());
        } catch (JSONException | ServiceException ex) {
            throw ServiceException.FAILURE("saveSalesCommissionRuleConditions" + ex.getMessage(), ex);
        }
        return result;
    }

    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getSalesCommissionRules(Map<String, Object> requestParams) throws ServiceException {
        List commissionRulesList = null;
        try {
            ArrayList params = new ArrayList();
            String condition = "";
            if (requestParams.containsKey(Constants.companyKey) && requestParams.get(Constants.companyKey) != null) {
                params.add(requestParams.get(Constants.companyKey).toString());
            }
            if (requestParams.containsKey(SalesCommissionRules.RULEID) && requestParams.get(SalesCommissionRules.RULEID) != null) {
                condition += " and id = ?";
                params.add(requestParams.get(SalesCommissionRules.RULEID).toString());
            }
            if (requestParams.containsKey(SalesCommissionSchemaMaster.SCHEMAMASTERNAME) && requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERNAME) != null) {
                condition += " and schemaMaster.schemaMaster = ?";
                params.add(requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERNAME).toString());
            }
            if (requestParams.containsKey(SalesCommissionSchemaMaster.SCHEMAMASTERID) && requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERID) != null) {
                condition += " and schemaMaster.id = ?";
                params.add(requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERID).toString());
            }
            String hql = " from SalesCommissionRules where company.companyID = ? " + condition;
            commissionRulesList = executeQuery(hql, params.toArray());
        } catch (NumberFormatException | ServiceException ex) {
            throw ServiceException.FAILURE("getSalesCommissionRules" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", "", commissionRulesList, commissionRulesList.size());
    }
    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject deleteSalesCommissionSchema(Map<String, Object> requestParams) throws ServiceException {
        int count = 0;
        try {
            ArrayList params1 = new ArrayList();
            ArrayList params3 = new ArrayList();
            ArrayList params2 = new ArrayList();
            String companyId = "";

            if (requestParams.containsKey(Constants.companyKey) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.companyKey))) {
                companyId = (String) requestParams.get(Constants.companyKey);
                params1.add(companyId);
            }
            if (requestParams.containsKey(SalesCommissionSchemaMaster.SCHEMAMASTERID) && !StringUtil.isNullOrEmpty((String) requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERID))) {
                params1.add(requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERID));
                params3.add(requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERID));
            }
            /*
             *Check selected schema is Mapped with dimension or not
             */
            String mappingQuery = " from FieldComboData where salesCommissionSchemaMaster = ?";
            List<FieldComboData> mappingData = executeQuery(mappingQuery, params3.toArray());
            if(mappingData.size()>0){
                throw new AccountingException("Selected Sales Commission Schema Master used in other transaction(s) so you can not delete it.");
            }
            
            String query = " from SalesCommissionRules where company.companyID = ? and schemaMaster.id = ?";
            List<SalesCommissionRules> rulesId = executeQuery(query, params1.toArray());

            Map<String, Object> rulesMap = new HashMap<>();
            rulesMap.put(Constants.companyKey, companyId);

            for (SalesCommissionRules rules : rulesId) {
                rulesMap.put(SalesCommissionRules.RULEID, rules.getId());
                deleteSalesCommissionRules(rulesMap);
            }
            String query2 = " delete from SalesCommissionSchemaMaster where company.companyID = ? and id = ?";

            executeUpdate(query2, params1.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteSalesCommissionSchema" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, null, count);
    }

    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject deleteSalesCommissionRules(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        try {
            ArrayList params = new ArrayList();
            String condition = "";
            String companyId = "";
            if (requestParams.containsKey(Constants.companyKey) && requestParams.get(Constants.companyKey) != null) {
                companyId = (String) requestParams.get(Constants.companyKey);
                params.add(companyId);
            }
            if (requestParams.containsKey(SalesCommissionRules.RULEID) && requestParams.get(SalesCommissionRules.RULEID) != null) {
                condition += " and id = ?";
                params.add(requestParams.get(SalesCommissionRules.RULEID).toString());
            }
            String query = " from SalesCommissionRuleCondition where company.companyID = ? and commissionRules.id = ?";

            List<SalesCommissionRuleCondition> ruleConditionId = executeQuery(query, params.toArray());

            Map<String, Object> conditionMap = new HashMap<>();
            conditionMap.put(Constants.companyKey, companyId);

            for (SalesCommissionRuleCondition ruleConditionObj : ruleConditionId) {
                conditionMap.put(SalesCommissionRuleCondition.CONDITIONID, ruleConditionObj.getId());
                deleteSalesCommissionRuleConditions(conditionMap);
            }

            String hql = "delete from SalesCommissionRules where company.companyID = ? " + condition;
            int num = executeUpdate(hql, params.toArray());
            result = new KwlReturnObject(true, null, null, null, num);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteSalesCommissionRules" + ex.getMessage(), ex);
        }
        return result;
    }

    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject deleteSalesCommissionRuleConditions(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        try {
            ArrayList paramsList = new ArrayList();
            if (requestParams.containsKey(Constants.companyKey) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.companyKey))) {
                paramsList.add(requestParams.get(Constants.companyKey));
            }
            if (requestParams.containsKey(SalesCommissionRuleCondition.CONDITIONID) && !StringUtil.isNullOrEmpty((String) requestParams.get(SalesCommissionRuleCondition.CONDITIONID))) {
                paramsList.add(requestParams.get(SalesCommissionRuleCondition.CONDITIONID));
            }
            String query = " delete from SalesCommissionRuleCondition where company.companyID = ? and id = ?";
            int num = executeUpdate(query, paramsList.toArray());
            result = new KwlReturnObject(true, null, null, null, num);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteSalesCommissionRuleConditions" + ex.getMessage(), ex);
        }
        return result;
    }
    
    /**
     *
     * @param schemaMasterId
     * @param masterItem
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject saveSalesCommissionMapping(String schemaMasterId, String masterItem) throws ServiceException {
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            if (!StringUtil.isNullOrEmpty(schemaMasterId)) {
                SalesCommissionSchemaMaster schemaMaster = (SalesCommissionSchemaMaster) get(SalesCommissionSchemaMaster.class, schemaMasterId);
                params.add(schemaMaster.getId());
            }
            if (!StringUtil.isNullOrEmpty(masterItem)) {
                FieldComboData comboData = (FieldComboData) get(FieldComboData.class, masterItem);
                params.add(comboData.getField().getFieldlabel());
                params.add(comboData.getField().getCompanyid());
                params.add(comboData.getValue());
            }
            String query = "update fieldcombodata fcd inner join fieldparams fp on fcd.fieldid = fp.id set fcd.salescommissionschemamaster = ? where fp.fieldlabel = ? and fp.companyid = ? and fcd.value = ?";
            count = executeSQLUpdate(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveSalesCommissionMapping" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, null, count);
    }

    public KwlReturnObject getCommossionSchemaTaggedToDimension(Map<String, Object> requestparams) throws ServiceException {
        int count = 0;
        List l = null;
        try {
            ArrayList params = new ArrayList();

            if (requestparams.containsKey("schemamaster")) {
                params.add((String)requestparams.get("schemamaster"));
}
            if (requestparams.containsKey("companyid")) {
                params.add((String)requestparams.get("companyid").toString());
            }
//            String query = "select id from salescommissionrulecondition scrc inner join salescommissionrules scr on scr.id=salescommissionrules scr inner join salescommissionschemamaster sm on sm.id=scr.schemamaster "
//                    + "inner join salescommissionschemamapping scsm on scsm.schemamaster=sm.id where scsm.masteritem=?";
            
            String hql=" select scrc From SalesCommissionRuleCondition scrc inner join scrc.commissionRules scr inner join scr.schemaMaster sm where sm.id=? and sm.company.companyID=?";
            l = executeQuery(hql, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveSalesCommissionMapping" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, l, l.size());
    }

}
