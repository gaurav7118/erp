/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.labormanagement;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author krawler
 */
public interface accLabourDAO {

    public KwlReturnObject addLabour(HashMap request) throws ServiceException;

    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject getLabour(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteLabour(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getLabourNumberCount(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveLabourSkillMapping(Map<String, Object> dataMap) throws ServiceException;
    
    public Set<LabourWorkCentreMapping> getLabourWCMapping(HashMap request) throws ServiceException;
    
    public KwlReturnObject getLabourCombo(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveResourceCost(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getResourceCost(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getResourceCostSQL(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateLabourFlag(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteLabourCost(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteLabourSkillMapping(Map<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject savelabourWorkCentrmapping(Labour labour, Set<LabourWorkCentreMapping> labourWorkCentreMapping) throws ServiceException;
    
    public KwlReturnObject getWCforLabour(Map<String, Object> dataMap) throws ServiceException;
     
    public KwlReturnObject getWOforLabour(Map<String, Object> dataMap) throws ServiceException;
     
    public KwlReturnObject getRTforLabour(Map<String, Object> dataMap) throws ServiceException;
}
