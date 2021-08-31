/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccContractManagementServiceDAO {

    public JSONObject getContractMasterDetails(Map<String, Object> requestParams) throws ServiceException;

    public JSONObject getTemporarySavedFiles(Map<String, Object> requestParams);

    public JSONObject getMasterContracts(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject exportContractMaster(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject deleteMasterContracts(Map<String, Object> requestParams)throws ServiceException;
    
    public void deleteMasterContractData(Map<String, Object> requestParams)throws ServiceException;
   
    public JSONObject deleteMasterContractsPermanently(Map<String, Object> requestParams)throws ServiceException;
    
    public JSONArray getMasterContractLinkingInformation(Map<String, Object> requestParams,JSONArray jArr) throws ServiceException;
    
    public JSONObject getMasterContractRows(Map<String, Object> requestParams) throws ServiceException;
}
