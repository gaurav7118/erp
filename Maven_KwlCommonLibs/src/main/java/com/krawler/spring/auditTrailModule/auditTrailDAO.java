/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.auditTrailModule;

import com.krawler.common.admin.AuditTrail;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface auditTrailDAO {

    /**
     * @deprecated use {@link #insertAuditLog(String actionid, String details, Map<String, Object> requestParams, String recid)}
     */
    @Deprecated
    public void insertAuditLog(String actionid, String details, HttpServletRequest request, String recid) throws ServiceException;
    
    //This method used to make entry ento Audit Trail for Recurring documents.
    public void insertRecurringAuditLog(String actionid, String details, HttpServletRequest request, String recid, User userid) throws ServiceException;
    
    public void insertAuditLog(String actionid, String details, Map<String, Object> requestParams, String recid) throws ServiceException;

    public void insertAuditLog(String actionid, String details, HttpServletRequest request, String recid, String extraid) throws ServiceException;
    
    public void indexAuditLogEntry(AuditTrail auditTrail);    

    public KwlReturnObject getRecentActivityDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAuditDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAuditData(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAuditGroupData() throws ServiceException;

    public KwlReturnObject reloadLuceneIndex(Map<String,Object> auditMap) throws ServiceException;
    
    public Map<String, Long> getAuditTrailCount(Map<String, Object> auditMap) throws ServiceException;
}
