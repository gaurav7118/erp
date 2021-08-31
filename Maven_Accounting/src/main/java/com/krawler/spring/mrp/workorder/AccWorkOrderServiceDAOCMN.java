/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.workorder;


import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.WorkOrder.WorkOrderComponentDetails;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface AccWorkOrderServiceDAOCMN {
    
    public JSONObject sendWOCloseReq(Map<String, Object> requestParams) throws ServiceException, IOException, JSONException;
    
    public JSONObject getWorkOrderProductDetail(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject updateInventoryForFinishedGood(Map<String, Object> requestParams, Map<String, Object> jeDataMap) throws ServiceException, IOException, JSONException;
    
    public void updateTaskLevelInventory(Map<String, Object> requestParams, WorkOrderComponentDetails workOrderComponentDetails,List<StockMovement> stockMovementsList) throws ServiceException;
    
    public JSONObject calCulateQuantityToBuild(Map<String, Object> requestParams,WorkOrder workOrder, List<StockMovement> stockMovementsList) throws ServiceException, IOException, JSONException;
    
    public void saveorUpdateLockedBatchDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONObject getJOBWOKINProductDetails(HashMap<String,Object> requestMap) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONArray getWODetailsItemJSON(HttpServletRequest request, String companyid, String WOID, HashMap<String, Integer> FieldMap,
            HashMap<String, String> replaceFieldMap, HashMap<String, Integer> DimensionFieldMap) throws ServiceException;

//    public JSONObject getWorkOrderProducedQtyDetails(Map<String, Object> requestParams) throws ServiceException ;
}
