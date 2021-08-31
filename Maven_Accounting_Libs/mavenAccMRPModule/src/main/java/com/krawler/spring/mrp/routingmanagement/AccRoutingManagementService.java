/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.routingmanagement;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccRoutingManagementService {
      /**
     * Description : This method is used to get routing template
     * @param requestParams
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject getRoutingtemplates(Map<String, Object> requestParams) throws ServiceException;
  /**
     * Description : This method is used to resource from MRP to PM
     * @param requestParams
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject syncResourceToPM(Map<String, Object> requestParams) throws ServiceException;

      /**
     * Description : This method is used to Create Or Update Project while creating or updating routing templates
     * @param requestMap
     * @return JSONObject 
     */
    public JSONObject createOrUpdateProjectRest(Map<String, Object> requestMap);
    
     /**
     * Description : This method is used to save routing template
     * @param dataMap
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject saveRoutingTemplate(Map<String, Object> dataMap) throws ServiceException;
      /**
     * Description : This method is used to sync project copy to PM
     * @param requestParams
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject syncProjectCopyReqToPM(Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject deleteDirtyProjectRest(Map<String, Object> requestMap);
    
     /**
     * Description : This method is used delete routing template permanantly
     * @param dataMap
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject deleteRoutingTemplate(Map<String, Object> dataMap) throws ServiceException;
/**
 * Description : This function is written to check whether routing template name already  exist or not  in the system in both add and edit case. 
 * @param requestParams
 * @return boolean
 * @throws com.krawler.common.service.ServiceException
 */
    public boolean isROutingTemplateNameAlreadyExist(Map<String, Object> requestParams)throws ServiceException;
}
