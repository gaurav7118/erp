/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.entitygst;

import com.krawler.common.admin.FieldParams;
import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.EntitybasedLineLevelTermRate;
import com.krawler.hql.accounting.ProductCategoryGstRulesMappping;
import com.krawler.spring.accounting.gst.dto.GstReturn;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 *
 * @author Suhas.Chaware
 */
public interface AccEntityGstService {
    
    public String deleteGSTRuleReportItem(JSONObject requestParams) throws ServiceException, ParseException, com.krawler.utils.json.base.JSONException;
    
    /**
     * ERP-33630
     * @desc : delete LineLevelTerm
     * @param requestParams (termId: LineLevelTerm's ID, termName : String (Name of Term))
     * @return msg for Delete Operation
     */ 
    public String deleteLineLevelTerm(JSONObject requestParams) throws ServiceException, ParseException, com.krawler.utils.json.base.JSONException;

    public JSONObject getGSTRuleSetup(JSONObject requestParams) throws ServiceException, ParseException, com.krawler.utils.json.base.JSONException;
    
    public JSONObject getGSTRuleReport(JSONObject requestParams) throws ServiceException, ParseException, com.krawler.utils.json.base.JSONException;

    public JSONObject getFieldComboDataForModule(JSONObject reqParams) throws ServiceException, com.krawler.utils.json.base.JSONException;

    public JSONObject saveGSTRuleSetup(JSONObject params) throws JSONException, ServiceException, ParseException;
    
    public KwlReturnObject checkTermsUsed(JSONObject requestParams) throws ServiceException, ParseException, com.krawler.utils.json.base.JSONException;
    
    /**
     * ERP-33643
     *
     * @param params (State : StateID from FieldComboData, Entity : EntityID from FieldComboData)
     * @return (success1 : true (for CGST,SGST), false (for IGST).
     * @desc get State mapped with entity to check for user to show 
     *  either : CGST and SGST Input box
     *  Or     : IGST Input box.
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject isStateMappedwithEntity(JSONObject params) throws JSONException, ServiceException, ParseException;
    
    public JSONObject createJsonObjectToSaveGSTRules(JSONObject requestJobj) throws JSONException, FileNotFoundException, IOException, ServiceException;

    public JSONObject getGSTForProduct(JSONObject params, HashMap<String, Object> requestParams) throws ServiceException, JSONException, ParseException;
    
    public JSONObject getGSTForAdvance(JSONObject params, HashMap<String, Object> requestParams) throws ServiceException, JSONException, ParseException ;

    public JSONArray fetchProductTermEntityRateMapDetails(ArrayList<EntitybasedLineLevelTermRate> productTermDetail, Map<String, Object> reqParams);
    
    public JSONObject getGSTSalesTaxLiabilityReportDetails(JSONObject paramsObj) throws JSONException;
    
    
    public JSONObject getGSTR1Summary(JSONObject params) throws ServiceException, JSONException;
    
    public JSONObject getGSTR1SummaryDetails(JSONObject params) throws ServiceException, JSONException;
    public JSONObject getGSTR2Summary(JSONObject params) throws ServiceException, JSONException;
    
    public JSONObject getGSTR2SummaryDetails(JSONObject params) throws ServiceException, JSONException;
    
    public JSONObject getGSTR2AImportData(JSONObject params) throws ServiceException, JSONException;
    
    public JSONObject getGSTR2AComparisonData(JSONObject params) throws ServiceException, JSONException;
    
    public JSONObject getGSTRMatchAndReconcile(JSONObject params) throws ServiceException, JSONException;
    
    
    public JSONArray fetchEntityBasedLineLevelTermRate(ArrayList<EntitybasedLineLevelTermRate> productTermDetail, Map<String, Object> reqParams);
    
    public JSONArray fetchProductCategoryGSTRuleMapDetails(ArrayList<ProductCategoryGstRulesMappping> productCategoryGSTRuleMap, Map<String, Object> reqParams);
    
    public JSONObject fetchMasterDataForGSTFields(List<FieldParams> list, Map mapData) throws ServiceException, JSONException;

    public HSSFWorkbook exportGSTR1Summary(JSONObject params) throws JSONException,ServiceException;
    
    public HSSFWorkbook exportGSTR3BDetails(JSONObject params) throws JSONException,ServiceException;
    
    public HSSFWorkbook exportGSTR2Summary(JSONObject params) throws JSONException,ServiceException;
    
    public HSSFWorkbook exportGSTR1Efiling(JSONObject params) throws JSONException, ServiceException;

    public HSSFWorkbook exportGSTRComputationReport(JSONObject params) throws JSONException, ServiceException;
    
    public JSONObject getGSTRMisMatchSummary(JSONObject params) throws ServiceException, JSONException;
    
    public JSONObject getGSTRMisMatchSummaryDetails(JSONObject params) throws ServiceException, JSONException;
    
    public HSSFWorkbook exportGSTRMisMatchSummary(JSONObject params) throws JSONException, ServiceException;
    
    public GstReturn uploadGSTR2JSONData(JSONObject params, List fileItems) throws JSONException, ServiceException, AccountingException;
    
    public JSONObject updateGSTR2TransactionFlag(JSONObject params) throws ServiceException, JSONException, AccountingException ;
    
    public JSONObject exportJsonForGSTR2(JSONObject params) throws ServiceException, JSONException ;
    
    public JSONObject getCESSCalculationType(JSONObject params) throws ServiceException, JSONException ;
    
    public JSONObject getGSTR3BSummaryReport(JSONObject params) throws ServiceException, JSONException;
    
    public JSONObject getGSTR3BSummaryDetails(JSONObject params) throws ServiceException, JSONException;
    
    public JSONObject getGSTFieldsChangedStatus(JSONObject params) throws ServiceException, JSONException;
    
    public JSONObject importEwayFieldsData(JSONObject requestJobj) throws JSONException, FileNotFoundException, IOException, ServiceException;
    public JSONObject validateEwayFieldsData(JSONObject requestJobj) throws JSONException, FileNotFoundException, IOException, ServiceException;
    
    public JSONObject getGSTComputationSummaryReport(JSONObject params) throws ServiceException, JSONException;
    
    public JSONObject getGSTComputationDetailReport(JSONObject params) throws ServiceException, JSONException;
    
    public JSONObject getGSTComputationReportSectionCombo(JSONObject requestParams) throws JSONException;
    public JSONObject getGSTR3BReportSectionCombo(JSONObject requestParams) throws JSONException;
    
    public HSSFWorkbook exportGSTComputationDetails(JSONObject params) throws JSONException, ServiceException;
    
}
