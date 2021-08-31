/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.mrp.taskProgressManagement;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface  AccTaskProgressManagementServiceDAO {
     /**
     * Description : This method is used to build the Json data of Task Progress
     * @param requestParams
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject getTaskProgressDetails(Map<String, Object> requestParams)throws ServiceException;
    
    public JSONObject getMaterialConsumedDetails(Map<String, Object> requestParams)throws ServiceException;
    
}
