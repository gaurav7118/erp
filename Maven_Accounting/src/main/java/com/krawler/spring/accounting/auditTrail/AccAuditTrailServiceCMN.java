/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.auditTrail;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;

/**
 *
 * @author krawler
 */
public interface AccAuditTrailServiceCMN {
    /**
     * Following method is used to return Audit Data in JSON Format.
     * @param auditData
     * @param paramObj
     * @param totalSize
     * @return
     * @throws SessionExpiredException
     * @throws ServiceException 
     */
    public JSONObject getAuditJSONData(List auditData, JSONObject paramObj, int totalSize) throws SessionExpiredException ,ServiceException;
}
