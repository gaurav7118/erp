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
package com.krawler.spring.accounting.mailNotification;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccMailNotificationDAO {

    public KwlReturnObject getMailNotifications(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject getMailNotification(String companyid, String fieldid) throws ServiceException;

    public KwlReturnObject saveMailNotification(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject editMailNotification(HashMap<String, Object> map) throws ServiceException;

    public KwlReturnObject deleteMailNotification(String companyid, String id) throws ServiceException;

    public String getUsersFullName(String[] users) throws ServiceException;

    public KwlReturnObject getEmailTemplateToEdit(String companyid, Integer moduleID, String fieldid)throws ServiceException;

    public KwlReturnObject saveMailNotificationRecurringDetails(HashMap<String, Object> requestParam) throws ServiceException;

    public KwlReturnObject getMailNotificationRecurringDetail(String companyid, String recurringruleid) throws ServiceException;

    public int updateMailNotificationRecurringDetail(String recurringruleid, String ruleid) throws ServiceException;
    
    public KwlReturnObject deleteRecurringMailDetails(String companyid, String recurringruleid) throws ServiceException;

}
