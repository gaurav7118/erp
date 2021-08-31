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
package com.krawler.acc.savedsearch.web;

import com.krawler.common.admin.SavedSearchQuery;
import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;
import static com.krawler.acc.savedsearch.web.SavedSearchConstants.*;
import com.krawler.common.util.StringUtil;

/**
 *
 * @author krawler
 */
public class SavedSearchJson {

    public JSONObject getSavedSearchQueries(List<SavedSearchQuery> ll, int totalCount) throws ServiceException, JSONException {
        JSONObject jobjdata = new JSONObject();

        for (SavedSearchQuery as : ll) {
            JSONObject jobj = new JSONObject();
            jobj.put(JSON_searchid, as.getSearchId());
            jobj.put(JSON_searchname, as.getSearchName());
            jobj.put(JSON_searchstate, as.getSearchquery());
            jobj.put(JSON_module, as.getModuleid());
            jobj.put(JSON_FILTER_APPEND, as.getFilterAppend() == 0 ? JSON_FILTER_APPEND_OR : JSON_FILTER_APPEND_AND);
            jobj.put("templateid", !StringUtil.isNullOrEmpty(as.getTemplateid()) ? as.getTemplateid() : "");
            jobj.put("isCustomLayout", as.isIscustomlayout());
            jobj.put("templatetitle", as.getTemplatetitle());
            jobjdata.append(JSON_data, jobj);
        }
        if (ll.isEmpty()) {
            jobjdata.put(JSON_data, new JSONArray());
        }
        jobjdata.put(JSON_count, totalCount);
        jobjdata.put(JSON_success, true);
        return jobjdata;
    }

    public JSONObject getSavedSearchQuery(SavedSearchQuery as) throws ServiceException, JSONException {
        JSONObject jobjdata = new JSONObject();
        if (as != null) {
            JSONObject jobj = new JSONObject();
            jobj.put(JSON_searchid, as.getSearchId());
            jobj.put(JSON_searchname, as.getSearchName());
            jobj.put(JSON_searchstate, as.getSearchquery());
            jobj.put(JSON_module, as.getModuleid());
            jobj.put(JSON_FILTER_APPEND, as.getFilterAppend() == 0 ? JSON_FILTER_APPEND_OR : JSON_FILTER_APPEND_AND);
            jobjdata.append(JSON_data, jobj);
        } else {
            jobjdata.put(JSON_data, new JSONArray());
        }
        jobjdata.put(JSON_success, true);
        return jobjdata;
    }

    public JSONObject deleteSavedSearchQuery(boolean success) throws JSONException {
        JSONObject jobjdata = new JSONObject();
        jobjdata.put(JSON_success, success);
        return jobjdata;
    }

    public JSONObject saveSearchQuery(SavedSearchQuery SAVED_SEARCH_QUERYObj) throws ServiceException, JSONException {
        JSONObject jobjdata = new JSONObject();
        JSONObject jobj = new JSONObject();
        jobj.put(JSON_searchid, SAVED_SEARCH_QUERYObj.getSearchId());
        jobj.put(JSON_searchname, SAVED_SEARCH_QUERYObj.getSearchName());
        jobj.put(JSON_searchstate, SAVED_SEARCH_QUERYObj.getSearchquery());
        jobj.put(JSON_module, SAVED_SEARCH_QUERYObj.getModuleid());
        jobjdata.append(JSON_data, jobj);
        jobjdata.put(JSON_success, true);
        return jobjdata;
    }
}
