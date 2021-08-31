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
package com.krawler.common.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimplePaging implements Paging {

    private int pageStart;
    private int pageSize;
    private long totalCount = 0;

    public SimplePaging(int pageStart, int pageSize) {
        this.pageStart = pageStart;
        this.pageSize = pageSize;
    }

    @Override
    public int getPageStart() {
        return pageStart;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public long getTotalCount() {
        return totalCount;
    }

    private String setQuery(String query) {
        String returnQuery = "";
        int k = -1;
        k = query.indexOf(" where ");
        if (k != -1) {
            String qry = query.substring(query.indexOf(" where "));
            String tempQuery = " " + query.toLowerCase();
            query.replaceAll(query.substring(query.indexOf(" where ")), qry);
            returnQuery = "select count(*) " + query.substring(tempQuery.indexOf(" from "));
        } else {
            String tempQuery = " " + query.toLowerCase();
            returnQuery = "select count(*) " + query.substring(tempQuery.indexOf(" from "));
        }
        return returnQuery;
    }

    @Override
    public long updateTotalCount(BaseDAO baseDao, String query, Object[] positionalparams, Map namedparams) {
        try {
            List l = baseDao.executeQuery(setQuery(query), positionalparams, namedparams);
            if (!l.isEmpty()) {
                totalCount = ((Number) l.get(0)).longValue();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(SimplePaging.class.getName() + ".updateTotalCount").log(Level.SEVERE, null, ex);
        }
        return this.totalCount;

    }
}
