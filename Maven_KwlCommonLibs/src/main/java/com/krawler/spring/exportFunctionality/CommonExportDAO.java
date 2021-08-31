/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.exportFunctionality;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import org.hibernate.QueryException;

/**
 *
 * @author krawler
 */
public interface CommonExportDAO {
    
    public KwlReturnObject addOrRemoveExportLog(Map<String, Object> dataMap) throws ServiceException;
    
    public List<ExportLog> getPendingExports() throws ServiceException;

    public KwlReturnObject getExportLog(Map requestParams) throws ServiceException, QueryException;
    
    public boolean updateRequestStatus(int i, Map params) throws ServiceException;
}
