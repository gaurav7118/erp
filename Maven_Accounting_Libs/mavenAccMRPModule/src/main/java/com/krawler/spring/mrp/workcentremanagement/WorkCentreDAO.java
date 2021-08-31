/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.workcentremanagement;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.mrp.labormanagement.LabourWorkCentreMapping;
import com.krawler.spring.mrp.machinemanagement.MachineWorkCenterMapping;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author krawler
 */
public interface WorkCentreDAO {
    public KwlReturnObject saveWorkCentre(JSONObject jobj) throws ServiceException;
    public KwlReturnObject getWorkCentres(Map<String, Object> requestParams) throws ServiceException; 
    public KwlReturnObject deleteWorkCentres(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getWorkCentreCombo(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deletelabourWorkCentreMappings(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteWorkCentreCustomData(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteMachineWorkCentreMappings(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteProductWorkCentreMappings(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteMaterialWorkCentreMappings(Map<String, Object> requestParams) throws ServiceException;
    public Set<LabourWorkCentreMapping> getLabourWCMapping(Map<String, Object>  request) throws ServiceException;
    public Set<MachineWorkCenterMapping> getMachineWCMapping(Map<String, Object>  request) throws ServiceException;
    public Set<ProductWorkCentreMapping> getProductWCMapping(Map<String, Object>  request) throws ServiceException;
    public Set<MaterialWorkCentreMapping> getMaterialWCMapping(Map<String, Object>  request) throws ServiceException;
    public KwlReturnObject saveWorkCentreMappings(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteWorkCentre(Map<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject deleteWorkCentrePermanently(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getWOforWorkCentre(Map<String, Object> dataMap) throws ServiceException;    
    public KwlReturnObject getRTforWorkCentre(Map<String, Object> dataMap) throws ServiceException;    
}
