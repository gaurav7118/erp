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

import com.krawler.common.admin.User;
import java.util.List;
import com.krawler.common.service.ServiceException;
import com.krawler.common.admin.SavedSearchQuery;
import com.krawler.acc.savedsearch.web.BuildCriteria;
import com.krawler.common.util.Constants;
import com.krawler.acc.savedsearch.web.SavedSearchConstants;
import com.krawler.common.dao.BaseDAO;
import org.hibernate.Criteria;

public class SavedSearchDAOImpl extends BaseDAO implements SavedSearchDAO {

    private Criteria getSavedSearchCriteria(String userid) {
        Criteria crit = getSession().createCriteria(SavedSearchQuery.class, "c");
        BuildCriteria.buildCriteria(userid, BuildCriteria.EQ, crit, Constants.user_userID);
        return crit;
    }

    @Override
    public List getSavedSearchQueries(String userid, int firstResult, int maxResults) throws ServiceException {
        List ll = null;
        try {
            Criteria crit = getSavedSearchCriteria(userid);
            crit.setFirstResult(firstResult);
            crit.setMaxResults(maxResults);
            BuildCriteria.buildCriteria(null, BuildCriteria.ISNULL, crit, SavedSearchConstants.CUSTOM_REPORT_ID);
            BuildCriteria.buildCriteria(BuildCriteria.OPERATORORDERDESC, BuildCriteria.ORDER, crit, Constants.updatedOn);
            ll = crit.list();
        } catch (Exception e) {
            throw ServiceException.FAILURE("RememberSearchDAOImpl.getSavedSearchQueries : " + e.getMessage(), e);
        }
        return ll;
    }

    @Override
    public List getSavedSearchQueries(String userid) throws ServiceException {
        List ll = null;
        try {
            Criteria crit = getSavedSearchCriteria(userid);
            ll = crit.list();
        } catch (Exception e) {
            throw ServiceException.FAILURE("RememberSearchDAOImpl.getSavedSearchQueries : " + e.getMessage(), e);
        }
        return ll;
    }

    @Override
    public List getSavedSearchQuery(String userid, String searchname,String customReportId) throws ServiceException {
        List<SavedSearchQuery> ll = null;
        try {
            Criteria crit = getSavedSearchCriteria(userid);
            if (customReportId != null && !customReportId.trim().equals("")) {
                BuildCriteria.buildCriteria(customReportId, BuildCriteria.EQ, crit, SavedSearchConstants.CUSTOM_REPORT_ID);
            } else {
                BuildCriteria.buildCriteria(searchname, BuildCriteria.EQ, crit, SavedSearchConstants.JSON_searchName);
                BuildCriteria.buildCriteria(null, BuildCriteria.ISNULL, crit, SavedSearchConstants.CUSTOM_REPORT_ID);
            }
            ll = crit.list();
        } catch (Exception e) {
            throw ServiceException.FAILURE("RememberSearchDAOImpl.getSavedSearchQuery : " + e.getMessage(), e);
        }
        return ll;
    }

    @Override
    public SavedSearchQuery saveSearchQuery(SavedSearchQuery SAVED_SEARCH_QUERYObj) throws ServiceException {
        try {
            saveOrUpdate(SAVED_SEARCH_QUERYObj);
        } catch (Exception e) {
            throw ServiceException.FAILURE("RememberSearchDAOImpl.saveSearchQuery : " + e.getMessage(), e);
        }
        return SAVED_SEARCH_QUERYObj;
    }

    @Override
    public boolean deleteSavedSearchQuery(String searchId) throws ServiceException {
        try {
            SavedSearchQuery SavedSearchQueryObj = getSavedSearchQuery(searchId);
            delete(SavedSearchQueryObj);
        } catch (Exception e) {
            throw ServiceException.FAILURE("RememberSearchDAOImpl.saveSearchQuery : " + e.getMessage(), e);
        }
        return true;
    }

    @Override
    public SavedSearchQuery getSavedSearchQuery(String searchId) {
        return (SavedSearchQuery) get(SavedSearchQuery.class, searchId);
    }

    @Override
    public User getUser(String userId) {
        return (User) get(User.class, userId);
    }
}
