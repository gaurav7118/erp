    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.WorkOrder;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author krawler
 */
public interface AccWorkOrderServiceDAO {
    
     public JSONObject getWorkOrderDataandColumnModel(Map<String, Object> requestParams);
     public KwlReturnObject saveWorkOrder(Map<String, Object> requestParams) throws AccountingException,ServiceException;
     public KwlReturnObject updateMassStatus(Map<String, Object> requestParams) throws AccountingException,ServiceException;
     public KwlReturnObject deleteWorkOrders(Map<String, Object> dataMap) throws ServiceException;
     public JSONObject getSOSCCombo(Map<String, Object> map) throws ServiceException;
     public JSONObject deleteWorkOrderPermanently(Map<String, Object> requestParams) throws ServiceException;
     public JSONObject deleteWorkOrder(Map<String, Object> requestParams) throws AccountingException, ServiceException;
     public JSONObject exportWorkOrder(Map<String, Object> requestParams) throws ServiceException;
     public JSONObject getProductsForCombo(HashMap<String, Object> requestParams) throws ServiceException;
     public JSONObject getExpanderDetails(HashMap<String, Object> requestParams )  throws ServiceException;
     public JSONArray getWorkOrderComponentDetails(Map<String, Object> requestParams) throws ServiceException;
     public JSONArray getShortFallProductsDetails(Map<String, Object> requestParams) throws ServiceException;
     //public JSONObject sendWOCloseReq(Map<String, Object> requestParams) throws ServiceException, IOException, com.krawler.utils.json.base.JSONException,AccountingException;
     public JSONArray getWOCombo(Map<String, Object> map) throws ServiceException;
     public JSONObject checkComponentAvailability(HashMap<String, Object> map) throws ServiceException;
     public JSONObject changeStatustoInProcess(HashMap<String, Object> map) throws ServiceException;
     public JSONObject changeWOStatus(HashMap<String, Object> map) throws ServiceException;  
     
    /**
     * description : This service is used to sync product with thier checklist to PM
     * @param requestParams
     * @return throws ServiceException
     * @throws com.krawler.common.service.ServiceException
     */
    public JSONObject syncBOmWithChecklistToPM(Map<String, Object> requestParams)throws ServiceException;
    
    /**
     * description : This service is used to sync work order date (i.e project date) to PM.
     * @param requestParams The map should contain companyid,userid,projectid,workorderdate,isshiftprojectstartdate
     * @return  JSONObject Contain Success or False 
     */
    
    public JSONObject syncWorkOrderDateToPM(Map<String,Object> requestParams)throws ServiceException;
    /**
     * 
     * @param requestParams
     * @return
     * @throws ServiceException 
     */
    public JSONObject getRejectedItemListReport(Map<String, Object> requestParams) throws ServiceException;
/**
 * 
 * @param requestParams
 * throws ServiceException
 * @return 
     * @throws com.krawler.common.service.ServiceException 
 */
    public JSONObject exportRejectedItemsList(Map<String, Object> requestParams) throws ServiceException;
    
    /**
     *  Function : Lock Quantity for Work order Details
     * @param batchJSON
     * @param productId
     * @param paramJobj
     * @param documentId
     * @throws com.krawler.utils.json.base.JSONException
     * @throws ParseException
     * @throws SessionExpiredException
     * @throws ServiceException
     * @throws UnsupportedEncodingException 
     */
    public void lockWorkOrderDeatilsBatch(String batchJSON, String productId, JSONObject paramJobj, String documentId) throws com.krawler.utils.json.base.JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException;

    /**
     * @author- SHarad Pawar - 2016
     * @param requestParams
     * @return 
     * @throws com.krawler.common.service.ServiceException 
     */
    public JSONObject getQualityControlParameters(Map<String, Object> requestParams) throws ServiceException;
    /**
     * 
     * @param requestParams
     * @return 
     * @throws com.krawler.common.service.ServiceException 
     */
    public JSONObject exportMRPQCReportList(Map<String, Object> requestParams) throws ServiceException;
    public JSONObject getWorkOrderShortFallReport(Map<String, Object> requestParams) throws ServiceException;
/*
    @author- SHarad Pawar - 2016
    
    */
    public JSONObject getTaskDetailsOfworkOrder(Map<String, Object> requestParams)throws ServiceException;

    /**
     *@param requestParams 
     * @return  
     * @throws com.krawler.common.service.ServiceException 
    
    */
    public JSONObject exportWorkOrdersTask(Map<String, Object> requestParams)throws ServiceException;

    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    public JSONObject getWorkOrderCosting(Map<String, Object> requestParams)throws ServiceException;

    public JSONObject getExpanderWOStockDetails(HashMap<String , Object> requestParams) throws ServiceException;
}
