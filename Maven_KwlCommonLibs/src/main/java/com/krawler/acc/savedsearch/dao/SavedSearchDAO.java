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
package com.krawler.acc.savedsearch.dao;

import com.krawler.common.service.ServiceException;
import com.krawler.common.admin.SavedSearchQuery;
import com.krawler.common.admin.User;
import java.util.List;

public interface SavedSearchDAO {

    /**
     *
     * @param SavedSearchQueryObj
     * @return
     * @throws ServiceException
     */
    SavedSearchQuery saveSearchQuery(SavedSearchQuery SavedSearchQueryObj) throws ServiceException;

    /**
     *
     * @param userid
     * @param firstResult
     * @param maxResults
     * @return
     * @throws ServiceException
     */
    List getSavedSearchQueries(String userid, int firstResult, int maxResults) throws ServiceException;

    /**
     *
     * @param userid
     * @return
     * @throws ServiceException
     */
    List getSavedSearchQueries(String userid) throws ServiceException;

    /**
     *
     * @param searchId
     * @return
     */
    SavedSearchQuery getSavedSearchQuery(String searchId);

    /**
     *
     * @param searchId
     * @return
     * @throws ServiceException
     */
    boolean deleteSavedSearchQuery(String searchId) throws ServiceException;

    /**
     *
     * @param userid
     * @param searchname
     * @return
     * @throws ServiceException
     */
    List getSavedSearchQuery(String userid, String searchname,String customReportId) throws ServiceException;

    /**
     *
     * @param userId
     * @return
     */
    User getUser(String userId);
}
