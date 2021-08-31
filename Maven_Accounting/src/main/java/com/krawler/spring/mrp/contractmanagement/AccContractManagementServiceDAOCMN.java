/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccContractManagementServiceDAOCMN {

    public JSONObject getMasterContractRows(Map<String, Object> requestParams) throws SessionExpiredException, ServiceException;
    
    public int saveFileMapping(Map<String, Object> filemap) throws ServiceException;
    
    public JSONObject saveMasterContract(Map<String, Object> requestParams) throws ServiceException;
}
