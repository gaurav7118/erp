/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.mrp.machinemanagement;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccMachineManagementDAO {
    
     public KwlReturnObject getMachineMasterData(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject getActiveSubstituteMachines(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject isMachineIDAlreadyPresent(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject getWCforMachine(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject getWOforMachine(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject getRTforMachine(Map<String, Object> dataMap) throws ServiceException;
     
     
     public KwlReturnObject saveMachineMaster(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject saveMachineProcessMapping(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject saveSubstituteMachineMapping(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject saveMachineWorkCenterMapping(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject updateMachineSyncableFlag(Map<String, Object> dataMap) throws ServiceException;
     
     public KwlReturnObject checkActiveMachineMapping(Map<String, Object> dataMap) throws ServiceException;
     
     public KwlReturnObject deleteMachineMasterPermanently(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject deleteMachineMaster(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject deleteMachineProcessMapping(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject deleteSubstituteMachineMapping(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject deleteMachineWorkCenterMapping(Map<String, Object> dataMap) throws ServiceException;
     
     public KwlReturnObject deleteMachineAssetMapping(Map<String, Object> dataMap) throws ServiceException;
     
     public KwlReturnObject saveMachineAssetMapping(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject getMachineAssetMaintenaceDetails(Map<String, Object> dataMap) throws ServiceException;

     public KwlReturnObject getMachineCombo(Map<String, Object> requestParams) throws ServiceException;
     
       // Machine Man ratio 
     public KwlReturnObject saveMachineManRatio(MachineManRatio machineManRatio) throws ServiceException;
     public KwlReturnObject getMachineManRatio(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject deleteMachineManRatio(String companyID , String MachineManRatioId) throws ServiceException;
    
     //Machine Cost
     public KwlReturnObject saveMachineCost(Map<String, Object> dataMap) throws ServiceException;
     public KwlReturnObject deleteMachineCost(HashMap<String, Object> requestParams) throws ServiceException;
     public KwlReturnObject getMachineCostSQL(Map<String, Object> requestParams) throws ServiceException;
     public KwlReturnObject getMachineCost(Map<String, Object> requestParams) throws ServiceException;
    
}
