/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.mrp.machinemanagement;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface  AccMachineManagementServiceDAO {
     /**
     * Description : This method is used to build the Json data of Machine Master
     * @param requestParams
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject getMachineMasterDetails(Map<String, Object> requestParams)throws ServiceException;
    /**
     *  Description: This method is used to get Given  Machine ID is present or not
     * @param requestParams
     * @return
     * @throws ServiceException 
     */    
    public boolean isMachineIDAlreadyPresent(Map<String, Object> requestParams)throws ServiceException;
    /**
     * Description :This method is used to save Machine Master Details
     * @param requestParams
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject saveMachineMaster(Map<String, Object> requestParams)throws ServiceException;
    
    /**
     * Description :This method is used to Delete Machine Master Details Permanently
     * @param requestParams
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject deleteMachineMasterPermanently(Map<String, Object> requestParams)throws ServiceException;
    /**
     * Description :This method is used to Delete Machine Master Details Temporarily
     * @param requestParams
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject deleteMachineMaster(Map<String, Object> requestParams)throws ServiceException;
    /**
     * Description :This method is used to get Active Machine details
     * @param requestParams
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject getActiveSubstituteMachines(Map<String, Object> requestParams)throws ServiceException;
    /**
     * Description :This method is used to  Export Machine details
     * @param requestParams
     * @return
     * @throws ServiceException 
     */
    public JSONObject exportMachineMaster(Map<String, Object> requestParams)throws ServiceException;
    public JSONArray getMachineCombo(Map <String,Object> map) throws ServiceException;
    
    // Machine Man ratio 
    /**
     * Description : Function to Save/Update Machine Man Ratio
     * @param dataMap
     * @return
     * @throws ServiceException 
     */
     public JSONObject saveMachineManRatio(Map<String, Object> dataMap) throws ServiceException;
     
     /**
      * Description :Function to defined Machine Man details
      * @param dataMap
      * @return
      * @throws ServiceException 
      */
     public JSONObject getMachineManRatio(HashMap<String, Object> dataMap) throws ServiceException;
     
     /**
      * Description : Function to Delete Machine Man Ratio
      * @param dataMap
      * @return
      * @throws ServiceException 
      */
     public JSONObject deleteMachineManRatio(Map<String, Object> dataMap) throws ServiceException;
     /**
      * Description : Function to Sync Machine Data to PM
      * @param requestParams
      * @return
      * @throws ServiceException 
      */
     public JSONObject syncMachineDataToPM(Map<String, Object> requestParams) throws ServiceException;
     
     public JSONObject exportMachineAllocationReportXlsx(Map<String, Object> requestParams) throws ServiceException;
     
     public JSONObject getExpanderDetails(HashMap<String, Object> requestParams )  throws ServiceException;
    
     public void getColumnModelForMachineList(HashMap<String, Object> requestParams, JSONObject object) throws ServiceException, SessionExpiredException;
    
     public JSONObject saveMachineCost(Map<String, Object> requestParams) throws ServiceException;
    
     public void deleteMachineCost(HashMap<String, Object> requestParams) throws ServiceException;
    
     public void getMachineCostList(Map<String, Object> requestParams, JSONObject object) throws ServiceException;
    
     public JSONObject syncMachineCost(Map<String, Object> requestParams) throws ServiceException;
    
}
