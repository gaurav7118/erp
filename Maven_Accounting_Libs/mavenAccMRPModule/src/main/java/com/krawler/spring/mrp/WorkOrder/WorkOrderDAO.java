/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.WorkOrder;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface WorkOrderDAO {
    
    public KwlReturnObject saveWorkOrder(JSONObject jobj) throws ServiceException;
    public KwlReturnObject getWorkOrders(Map<String, Object> requestParams) throws ServiceException; 
    public KwlReturnObject getWorkOrderMachineMapping(Map<String, Object> map) throws ServiceException;
    public KwlReturnObject deleteWorkOrders(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getSOSCCombo(Map<String, Object> requestParams) throws ServiceException;
    
    /**
     * Description : This method is used to save labour used in workorder
     * @param requestParams
     * @return JSONObject
     * @throws ServiceException 
     */
    public KwlReturnObject saveWorkOrderLabourMapping(Map<String, Object> LabourMappingDataMap)throws ServiceException;

    public KwlReturnObject deleteWorkOrderMappings(Map<String, Object> deleteParams)throws ServiceException;

    public KwlReturnObject saveWorkOrderMachineMapping(Map<String, Object> machineMappingDataMap)throws ServiceException;

    public KwlReturnObject saveWorkOrderWorkCenterMapping(Map<String, Object> workcenterMappingDataMap)throws ServiceException;
    
    public KwlReturnObject deleteWorkOrderPermanently(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject deleteWorkOrderCustomData(Map<String, Object> deleteParams)throws ServiceException;
    
    public KwlReturnObject deleteWorkOrder(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getProductsForCombo(HashMap<String, Object> dataMap, boolean isSalesOrderLinked) throws ServiceException;
    public KwlReturnObject getMachineForWorkOrder(HashMap<String, Object> dataMap) throws ServiceException ;
    public KwlReturnObject getLabourForWorkOrder(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getWorkCentreForWorkOrder(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject saveWorkOrderComponentDetails(Map<String, Object> requestMap) throws ServiceException;
    public KwlReturnObject getWorkOrderComponentDetails(Map<String, Object> requestParams) throws ServiceException ;
    public KwlReturnObject getPOforWOdetail(HashMap<String, Object> params) throws ServiceException;
    public KwlReturnObject getPReqforWOdetail(HashMap<String, Object> params) throws ServiceException;
    public KwlReturnObject getWorkOrderCombo(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getWODetailfromProductandWO(HashMap<String,Object> reqMap) throws ServiceException;
    public KwlReturnObject getCheckListDetails(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getWOStatusidFromDefaultID(HashMap<String, Object> requestParams) throws ServiceException ;
    public KwlReturnObject deleteWorkOrderBatchSerialDetails(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCostOfManufacturingDetails(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getCostCategoryExpenseAmount(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getWorkorderFromSOandProduct(Map<String, Object> requestParams) throws ServiceException;
    
    
}
