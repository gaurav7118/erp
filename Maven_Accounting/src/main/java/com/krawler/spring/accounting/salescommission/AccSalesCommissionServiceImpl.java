/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.salescommission;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;
import java.util.Map;

public class AccSalesCommissionServiceImpl implements AccSalesCommissionService {

    private AccSalesCommissionDAO accSalesCommissionDAO;

    public void setAccSalesCommissionDAO(AccSalesCommissionDAO accSalesCommissionDAO) {
        this.accSalesCommissionDAO = accSalesCommissionDAO;
    }

    /**
     *
     * @param requestParams
     * @return jSONObject
     * @throws ServiceException
     */
    @Override
    public JSONObject saveSalesCommissionSchemaMaster(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jSONObject = new JSONObject();
        try {
            String schemaMasterID = "";
            String companyId = (String) requestParams.get(Constants.companyKey);

            KwlReturnObject kwlReturnObject = accSalesCommissionDAO.saveSalesCommissionSchemaMaster(requestParams);
            SalesCommissionSchemaMaster schemaMaster = (SalesCommissionSchemaMaster) kwlReturnObject.getEntityList().get(0);
            schemaMasterID = schemaMaster.getId();

            String rulesDetail = requestParams.get(SalesCommissionSchemaMaster.RULESDETAIL).toString();
            if (!StringUtil.isNullOrEmpty(rulesDetail)) {
                JSONArray rulesDetailArr = new JSONArray(rulesDetail);
                for (int i = 0; i < rulesDetailArr.length(); i++) {
                    JSONObject ruleObj = rulesDetailArr.getJSONObject(i);
                    ruleObj.put(SalesCommissionRules.SCEHMAMASTER, schemaMaster);
                    ruleObj.put(Constants.companyKey, companyId);
                    KwlReturnObject ruleKwlObj = accSalesCommissionDAO.saveSalesCommissionRules(ruleObj);
                    SalesCommissionRules commissionRules = (SalesCommissionRules) ruleKwlObj.getEntityList().get(0);
                    JSONArray conditionArr = ruleObj.getJSONArray("conditionArr");
                    for(int j=0;j<conditionArr.length();j++){
                        JSONObject conditionObj = conditionArr.getJSONObject(j);
                        conditionObj.put(Constants.companyKey, companyId);
                        conditionObj.put(SalesCommissionRules.RULEID, commissionRules);
                        accSalesCommissionDAO.saveSalesCommissionRuleConditions(conditionObj);
                    }
                }
            }
        } catch (ServiceException | JSONException ex) {
            throw ServiceException.FAILURE("saveSalesCommissionSchemaMaster", ex);
        }
        return jSONObject;
    }

    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    @Override
    public JSONObject getSalesCommissionSchemaMasters(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jObj = new JSONObject();
        try {
            JSONArray jSONArray = new JSONArray();
            KwlReturnObject result = accSalesCommissionDAO.getSalesCommissionSchemaMasters(requestParams);
            List<SalesCommissionSchemaMaster> schemaMasterList = result.getEntityList();

            for (SalesCommissionSchemaMaster schemaMaster : schemaMasterList) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put(SalesCommissionSchemaMaster.ID, schemaMaster.getId());
                jsonObj.put(SalesCommissionSchemaMaster.SCHEMAMASTERNAME, schemaMaster.getSchemaMaster());
                jSONArray.put(jsonObj);
            }
            jObj.put("data", jSONArray);
        } catch (ServiceException | JSONException ex) {
            throw ServiceException.FAILURE("getSalesCommissionSchemaMasters" + ex.getMessage(), ex);
        }
        return jObj;
    }

    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    @Override
    public JSONObject getSalesCommissionRules(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jObj = new JSONObject();
        try {
            JSONArray jSONArray = new JSONArray();
            KwlReturnObject result = accSalesCommissionDAO.getSalesCommissionRules(requestParams);
            List<SalesCommissionRules> commissionRulesList = result.getEntityList();

            for (SalesCommissionRules commissionRules : commissionRulesList) {
                JSONObject jsonObj = new JSONObject();
                //Rules Details
                jsonObj.put(SalesCommissionRules.RULEID, commissionRules.getId());
                jsonObj.put(SalesCommissionRules.ID, commissionRules.getId());
                jsonObj.put(SalesCommissionRules.SCHEMATYPEID, commissionRules.getSchemaType());
                
                if (commissionRules.getSchemaType() == 1) {
                    jsonObj.put(SalesCommissionRules.SCHEMATYPE, "Percentage");
                } else {
                    jsonObj.put(SalesCommissionRules.SCHEMATYPE, "Flat");
                }
                
                jsonObj.put(SalesCommissionRules.AMOUNT, commissionRules.getAmount());
                jsonObj.put(SalesCommissionRules.RULEDESCRIPTION, commissionRules.getRulesDescription());
                //SchemaMaster Details
                jsonObj.put(SalesCommissionSchemaMaster.SCHEMAMASTERID, commissionRules.getSchemaMaster().getId());
                jsonObj.put(SalesCommissionSchemaMaster.SCHEMAMASTERNAME, commissionRules.getSchemaMaster().getSchemaMaster());
                jsonObj.put("schemaMasterName", commissionRules.getSchemaMaster().getSchemaMaster());
                jSONArray.put(jsonObj);
            }
            jObj.put("data", jSONArray);
        } catch (ServiceException | JSONException ex) {
            throw ServiceException.FAILURE("getSalesCommissionSchemaMasters" + ex.getMessage(), ex);
        }
        return jObj;
    }

    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    @Override
    public JSONObject deleteSalesCommissionRules(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jObj = new JSONObject();
        try {
            KwlReturnObject result = accSalesCommissionDAO.deleteSalesCommissionRules(requestParams);
            jObj.put("count", result.getRecordTotalCount());
            jObj.put("success", true);
        } catch (ServiceException | JSONException ex) {
            throw ServiceException.FAILURE("getSalesCommissionSchemaMasters" + ex.getMessage(), ex);
        }
        return jObj;
    }
    
    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    @Override
    public JSONObject saveSalesCommissionMapping(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jObj = new JSONObject();
        try {
            KwlReturnObject result = null;
            String schemaMasterId = "";
            String masterItemArr[] = {};
            if (requestParams.containsKey(SalesCommissionSchemaMaster.SCHEMAMASTERID) && !StringUtil.isNullOrEmpty((String) requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERID))) {
                schemaMasterId = (String) requestParams.get(SalesCommissionSchemaMaster.SCHEMAMASTERID);
            }
            if (requestParams.containsKey("masterItem") && !StringUtil.isNullOrEmpty((String) requestParams.get("masterItem"))) {
                masterItemArr = (String[]) requestParams.get("masterItem").toString().split(",");
            }
            for (String masterItem : masterItemArr) {
                result = accSalesCommissionDAO.saveSalesCommissionMapping(schemaMasterId, masterItem);
            }
            jObj.put("isSuccess", true);
        } catch (ServiceException | JSONException ex) {
            throw ServiceException.FAILURE("saveSalesCommissionMapping" + ex.getMessage(), ex);
        }
        return jObj;
    }
    
    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    @Override
    public JSONObject deleteSalesCommissionSchema(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jObj = new JSONObject();
        try {
            accSalesCommissionDAO.deleteSalesCommissionSchema(requestParams);
            jObj.put("success", true);
        } catch (ServiceException | JSONException ex) {
            throw ServiceException.FAILURE("saveSalesCommissionMapping" + ex.getMessage(), ex);
        }
        return jObj;
    }

}
