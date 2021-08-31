/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.gst;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.CreditNote;
import com.krawler.hql.accounting.DebitNote;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.JSONException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;

/**
 *
 * @author krawler
 */
public interface AccGstService {
    
    public JSONObject getGSTFormGenerationHistoryConfig(Map<String, Object> map);

    public JSONObject getGSTFormGenerationHistoryData(Map<String, Object> requestMap);
    
    public void exportAndSaveGSTFormGenerationHistory(ByteArrayOutputStream outputStream,Map<String,Object> requestMap);
    
    public JSONObject getValidDateRangeForFileGeneration(Map<String,Object> requestParams) throws ServiceException;
    
    public JSONObject getValidDateRangeForTAPFileGeneration(Map<String,Object> requestParams) throws ServiceException;
    
    public boolean checkForClaimableSalesInvoices(HashMap<String, Object> requestParams);
    
    public boolean checkForClaimablePurchaseInvoices(HashMap<String, Object> requestParams);
    
    public boolean checkForUnInvoicedDOs(Map<String, Object> requestMap);
    
    public KwlReturnObject saveEntityMapping(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject getEntityDetails(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject getMultiEntityForCombo(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteEntityMapping(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject getLatestDateOfFileGeneration(Map<String,Object> requestParams);
        
    public Map getEntityMSICCode(Map<String, Object> requestParams);
    
    public JSONObject checkForPendingTransactions(HashMap<String, Object> requestParams);

    public StringBuilder generateTXTGAFV1(JSONObject paramsJObj) throws ServiceException;

    public StringBuilder generateTXTGAFV2(JSONObject paramsJObj) throws ServiceException;

    public Document generateXMLGAFV1(JSONObject paramsJObj) throws ServiceException;

    public Document generateXMLGAFV2(JSONObject paramsJObj) throws ServiceException;
    
    public int getGSTGuideVersion(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteGSTFileGenerationHistory(JSONObject paramsJObj) throws ServiceException, Exception;
    
    public JSONObject getGSTForm5SubmissionData(Map<String, Object> requestMap);
    
    public JSONObject gstTransactionListingSubmissionDetails(Map<String, Object> requestMap);
    
    public JSONObject gstTransactionListingSubmissioncheck(Map<String, Object> requestMap);
    
    /**
     * @param requestParams
     * @param list
     * @param dataArr
     * @throws ServiceException
     * @desc Distribute global level tax at line level for Purchase Invoice & Expense Invoice.
     */
    public void getGoodsReceiptRowsForAuditFile(Map<String, Object> requestParams, List<GoodsReceipt> list, JSONArray dataArr) throws JSONException, ServiceException;

    /**
     * @param requestParams
     * @param list
     * @param dataArr
     * @throws ServiceException
     * @desc Distribute global level tax at line level for Sales Invoice.
     */
    public void getInvoiceRowsForAuditFile(Map<String, Object> requestParams, List list, JSONArray dataArr) throws JSONException, ServiceException;
    
    /**
     *
     * @param requestParams
     * @param list
     * @param dataArr
     * @throws ServiceException
     * @desc Distribute global level tax at line level for Debit Note for
     * Undercharged Sales Invoice.
     */
    public void getDNUnderchargeRowsForAuditFile(Map<String, Object> requestParams, List<DebitNote> list, JSONArray dataArr) throws ServiceException;

    /**
     *
     * @param requestParams
     * @param list
     * @param dataArr
     * @throws ServiceException
     * @desc Distribute global level tax at line level for Credit Note for
     * Undercharged Purchase Invoice.
     */
    public void getCNUnderChargeRowsForAuditFile(Map<String, Object> requestParams, List<CreditNote> list, JSONArray dataArr) throws ServiceException;

    /**
     *
     * @param requestParams
     * @param list
     * @param dataArr
     * @throws ServiceException
     * @desc Distribute global level tax at line level for Debit Note for
     * Overcharged Purchase Invoice.
     */
    public void getDNOverchargeRowsForAuditFile(Map<String, Object> requestParams, List<DebitNote> list, JSONArray dataArr) throws ServiceException;

    /**
     *
     * @param requestParams
     * @param list
     * @param dataArr
     * @throws ServiceException
     * @desc Distribute global level tax at line level for Credit Note for
     * Overcharged Sales Invoice.
     */
    public void getCNOverchargeRowsForAuditFile(Map<String, Object> requestParams, List<CreditNote> list, JSONArray dataArr) throws ServiceException;

    public JSONObject gstTransactionListingChunkData(JSONObject jobj);
}
