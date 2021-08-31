/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.routingmanagement;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccRoutingManagementDao {

    public KwlReturnObject saveRoutingTemplate(Map<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveRoutingTemplateMachineMapping(Map<String, Object> machineMappingDataMap) throws ServiceException;    

    public KwlReturnObject saveRoutingTemplateLabourMapping(Map<String, Object> LabourMappingDataMap)throws ServiceException;    

    public KwlReturnObject deleteRoutingTemplateMappings(Map<String, Object> deleteParams)throws ServiceException;    

    public KwlReturnObject getRoutingTemplates(Map<String, Object> requestParams)throws ServiceException;    
    
    public KwlReturnObject getRCNumberCount(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteRoutingTemplatePerm(Map<String, Object> requestParams)throws ServiceException;    
    
    public KwlReturnObject getChildTemplate(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteRoutingTemplateCustomData(Map<String, Object> requestParams)throws ServiceException;    
}
