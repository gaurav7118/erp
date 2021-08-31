/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.entitygst;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.accounting.gst.dto.GSTR2Submission;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Suhas.Chaware
 */
public interface AccEntityGstDao {

    public KwlReturnObject getChildFieldParamsForGSTRule(HashMap RequestParams) throws ServiceException;

    public KwlReturnObject getFieldCombodataForModule(JSONObject reqParams) throws ServiceException, JSONException;
    /**
     * @param reqParams (State : StateID from FieldComboData, Entity : EntityID from FieldComboData)
     * @return res.size() : 1 (for local state), 0 (for other state.)
     * @throws ServiceException
     * @throws JSONException 
     */
    public KwlReturnObject isStateMappedwithEntity(Map<String, Object> reqParams) throws ServiceException, JSONException;

    public List getColumnNumberForDimension(JSONObject reqParams) throws ServiceException;

    public KwlReturnObject saveGSTRuleSetup(Map<String, Object> reqMap) throws ServiceException;
    
    public KwlReturnObject saveGSTRuleProdCategoryMapping(Map<String, Object> reqMap) throws ServiceException;

    public KwlReturnObject getGSTTermDetails(JSONObject jSONObject) throws ServiceException;

    public KwlReturnObject getEntityBasedTermRate(Map<String, Object> mapData) throws ServiceException;
    
    public KwlReturnObject deleteGSTRuleReportItem(Map<String, Object> mapData) throws ServiceException;
    
    public KwlReturnObject checkDetailTermMapForDelete(Map<String, Object> mapData) throws ServiceException;
    
    /**
     * @Desc : Check if particular term can be deleted or not.
     * @param mapData (Map : term Id)
     * @return true (if term can be deleted) or False (if term Cannot be deleted)
     * @throws ServiceException
     */
    public KwlReturnObject checkLineLevelTermUsed(Map<String, Object> mapData) throws ServiceException;
    
    /**
     * @desc : deletes the rule from ProductTermsMap and LineLevelTerms.
     * @param mapData (Map : term Id)
     * @return true (for successfully deleted)
     * @throws ServiceException 
     */
    public KwlReturnObject deleteLineLevelTerm(Map<String, Object> mapData) throws ServiceException;
    
    public KwlReturnObject getGroupByAllEntityBasedTermRate(Map<String, Object> mapData) throws ServiceException;
    
    public List getDimensionValueTaggedtoProd(JSONObject params) throws ServiceException;
    
    public KwlReturnObject getGroupByEntityBasedTermRate(Map<String, Object> mapData) throws ServiceException;
    
    public KwlReturnObject getInvoiceData(JSONObject params) throws ServiceException,JSONException;
    
    public KwlReturnObject getInvoiceDetailData(JSONObject params) throws ServiceException, JSONException;
    
//    public KwlReturnObject getGSTMasterFromInvoiceDetails(JSONObject params) throws ServiceException, JSONException;
    
    public KwlReturnObject getInvoicesForGSTSalesTaxLiabilityReport(JSONObject paramsObj) throws ServiceException, ParseException;
    
    public KwlReturnObject getGSTSalesTaxLiabilityReportData(JSONObject paramsObj) throws ServiceException;
    
    public KwlReturnObject getLocationwiseInvoiceDetailsForProduct(JSONObject params) throws ServiceException, JSONException;
    
    public KwlReturnObject getCreditNoteDetailsForGSTR1(JSONObject params) throws ServiceException,JSONException;

    public KwlReturnObject getGSTDetailsForInvoice(JSONObject params) throws ServiceException, JSONException;
    
    public KwlReturnObject getGSTDetailsForReturn(JSONObject params) throws ServiceException, JSONException;
    
    public List getAssetSpecificCategoryIdFromFCD(JSONObject reqParams) throws ServiceException;
    
    public List getRuleIdInEditCase(Map<String, Object> reqMap) throws ServiceException ;
    
    public List getProductClassValue(JSONObject params) throws ServiceException, JSONException ;
    
    public List getStatesFromFCD(JSONObject jSONObject) throws ServiceException;
    
    public List getInvoiceDataWithDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
    
    public List getCNDNWithInvoiceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
    
    public List getAdvanceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
    
    public List getHSNWiseInvoiceDataWithDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
    
    public KwlReturnObject getProductCatgoryGSTRuleMap(Map<String, Object> mapData) throws ServiceException;
    
    public KwlReturnObject getEntitybasedLineLevelTermRate(Map<String, Object> mapData) throws ServiceException;
    
    public List getNillInvoiceDataWithDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
    
    public List getNillCNDNWithInvoiceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
    
    public List getNillAdvanceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
    
    public List getNonGSTGoodsReceiptDataWithDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
   
    public List getDelieveryOrderData(JSONObject reqParams) throws ServiceException, JSONException;
    
     public List getPaymentData(JSONObject reqParams) throws ServiceException, JSONException;
    
    public JSONObject getinvoiceTermAmtAndTotalAmtforGSTSalesTaxLiabilityReport(JSONObject paramObj) throws ServiceException;
    
    public List getCNAgainstCustomer(JSONObject reqParams) throws ServiceException, JSONException;
    
    public List getGSTMissingInvoice(JSONObject reqParams) throws ServiceException, JSONException;
    
    public List getGSTMissingPurchaseInvoice(JSONObject reqParams) throws ServiceException, JSONException;

    public List getGSTMappingColnumOfLocationDimension(JSONObject reqParams) throws ServiceException, JSONException;
    
    public KwlReturnObject saveGSTR2Submission(JSONObject json) throws ServiceException;
    
    public KwlReturnObject getGSTR2Submission(JSONObject json) throws ServiceException;
    
    public KwlReturnObject getCESSCalculationType(JSONObject json) throws ServiceException;
    
    public GSTR2Submission saveOrGetGSTR2Submission(JSONObject json) throws ServiceException;
    
    public List getDNAgainstCustomer(JSONObject reqParams) throws ServiceException, JSONException;
    
    public JSONObject getProductTaxClassOnDate(JSONObject reqParams) throws JSONException, ServiceException, ParseException;
    
    public List getMasterHistoryForGSTFields(Map<String, Object> reqMap) throws ServiceException;
    
    public List getTaxClassHistoryForGSTFields(Map<String, Object> reqMap) throws ServiceException;
    
    public List getGSTDimensionsDetailsForGSTCalculations(JSONObject params)throws ServiceException,JSONException;
    
    public List getGSTDimensionDataFromCustomTableForGSTCalculations(JSONObject params) throws ServiceException, JSONException;
    
    public List getCashRefundWithInvoiceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
    public List executeCustomBuildQuery(String query, List queryParams)throws ServiceException;
    
    public List getGSTMissingCN(JSONObject reqParams) throws ServiceException, JSONException;
    
    public List getGSTMissingSalesOrder(JSONObject reqParams) throws ServiceException, JSONException;
    
    public List getMissingDelieveryOrderData(JSONObject reqParams) throws ServiceException, JSONException;
}
