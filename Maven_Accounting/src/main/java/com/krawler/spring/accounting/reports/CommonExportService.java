/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.exportFunctionality.ExportLog;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.*;
import org.hibernate.QueryException;

/**
 *
 * @author krawler
 */
public interface CommonExportService {

    
    public void exportFileService(JSONObject requestJSON) throws JSONException;
    
    public String addOrRemoveExportLog (JSONObject requestJSON) throws JSONException, ServiceException;
    
    public List<ExportLog> getPendingExports() throws ServiceException;
    
    public JSONObject getExportLog(Map requestParams) throws ServiceException, QueryException, JSONException, SessionExpiredException, ParseException;

    public boolean updateRequestStatus(int i, Map params) throws ServiceException;
    
}
