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
package com.krawler.acc.savedsearch.bizservice;

import com.krawler.common.admin.SavedSearchQuery;
import com.krawler.common.service.ServiceException;
import com.krawler.acc.savedsearch.dao.SavedSearchDAO;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Date;

/**
 * @author krawler
 *
 */
public class SavedSearchServiceImpl implements SavedSearchService {

    private SavedSearchDAO SaveSearchDAO;

    public SavedSearchDAO getSaveSearchDAO() {
        return SaveSearchDAO;
    }

    public void setSaveSearchDAO(SavedSearchDAO SaveSearchDAO) {
        this.SaveSearchDAO = SaveSearchDAO;
    }

    @Override
    public List<SavedSearchQuery> getSavedSearchQueries(String userId, int firstResult, int maxResults) throws ServiceException {
        List<SavedSearchQuery> ll = SaveSearchDAO.getSavedSearchQueries(userId, firstResult, maxResults);
        return ll;
    }

    @Override
    public SavedSearchQuery getSavedSearchQuery(String searchId) throws ServiceException {
        SavedSearchQuery as = SaveSearchDAO.getSavedSearchQuery(searchId);
        return as;
    }

    @Override
    public boolean deleteSavedSearchQuery(String searchId) throws ServiceException {
        return SaveSearchDAO.deleteSavedSearchQuery(searchId);
    }

    @Override
    public int getSavedSearchQueries(String userId) throws ServiceException {
        return SaveSearchDAO.getSavedSearchQueries(userId).size();
    }

    public List<SavedSearchQuery> getSavedSearchQueries(String userid, String searchname,String customReportId) throws ServiceException {
        List<SavedSearchQuery> ll = null;
        return ll = SaveSearchDAO.getSavedSearchQuery(userid, searchname,customReportId);
    }

    @Override
    public SavedSearchQuery saveSearchQuery(int module, String userId, String SEARCH_QUERY, String searchname, int filterAppend, String templateid, boolean isCustomLayout,String templatetitle,String customReportId) throws ServiceException {
        SavedSearchQuery SavedSearchQueryObj = null;
        try {
            List<SavedSearchQuery> ll = SaveSearchDAO.getSavedSearchQuery(userId, searchname,customReportId);
            SavedSearchQueryObj = ll.isEmpty() ? new SavedSearchQuery() : ll.get(0);
            SavedSearchQueryObj.setModuleid(module);
            SavedSearchQueryObj.setUser(SaveSearchDAO.getUser(userId));
            SavedSearchQueryObj.setSearchquery(SEARCH_QUERY);
            SavedSearchQueryObj.setSearchName(searchname);
            SavedSearchQueryObj.setFilterAppend(filterAppend);
            SavedSearchQueryObj.setUpdatedon(new Date());
            if(!StringUtil.isNullOrEmpty(templateid)){
                SavedSearchQueryObj.setTemplateid(templateid);
            }
            SavedSearchQueryObj.setIscustomlayout(isCustomLayout);
            SavedSearchQueryObj.setTemplatetitle(templatetitle);
            if (!StringUtil.isNullOrEmpty(customReportId)) {
                SavedSearchQueryObj.setCustomReportId(customReportId);
            }
            SavedSearchQueryObj = SaveSearchDAO.saveSearchQuery(SavedSearchQueryObj);
        } catch (Exception e) {
            throw ServiceException.FAILURE("SaveSearchServiceImpl.saveSearchQuery : " + e.getMessage(), e);
        }
        return SavedSearchQueryObj;
    }

    @Override
    public SavedSearchQuery modifySavedSearchQuery(SavedSearchQuery savedSearchQueryObj) throws ServiceException, JSONException,SessionExpiredException {

        DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        DateFormat datef=authHandler.getDateOnlyFormat();
        JSONArray array = new JSONArray();

        JSONObject jobj = new JSONObject(StringUtil.DecodeText(savedSearchQueryObj.getSearchquery()));
        JSONArray jobjdata = jobj.getJSONArray("data");
        for (int i = 0; i < jobjdata.length(); i++) {
            JSONObject jobj2 = jobjdata.getJSONObject(i);

            if (jobj2.has("isinterval") && jobj2.getBoolean("isinterval") && jobj2.has("interval") && jobj2.has("xtype") && jobj2.getString("xtype").equals("datefield")) {
                int invervalInDays = jobj2.getInt("interval");
                boolean isBefore = jobj2.getBoolean("isbefore");
                String searchTxt = "";
                if (isBefore) { // For Before Date Data Filter

                    Calendar today = Calendar.getInstance();

                    Date startDate = getIntervalDate(invervalInDays, isBefore);
                    Date endDate = today.getTime();
                    String edate;
                    try {
                        edate = datef.format(endDate);
                        endDate=datef.parse(edate);
                    } catch (ParseException ex) {
                        endDate = today.getTime();
                    }
                    searchTxt = df.format(startDate) + " To " + df.format(endDate);


                } else { // For After Date Data Filter

                    Calendar today = Calendar.getInstance();

                    Date startDate = today.getTime();
                    String sdate;
                    try {
                        sdate = datef.format(startDate);
                        startDate=datef.parse(sdate);
                    } catch (ParseException ex) {
                        startDate = today.getTime();
                    }
                    Date endDate = getIntervalDate(invervalInDays, isBefore);

                    searchTxt = df.format(startDate) + " To " + df.format(endDate);
                }

                jobj2.put("search", searchTxt);
                jobj2.put("searchText", searchTxt);
                jobj2.put("id", searchTxt);

            }

            array.put(jobj2);
        }

        JSONObject j = new JSONObject();
        j.put("data", array);

        savedSearchQueryObj.setSearchquery(j.toString());

        return savedSearchQueryObj;
    }

    public Date getIntervalDate(int invervalInDays, boolean isBefore) throws SessionExpiredException{

        Calendar today = Calendar.getInstance();
        DateFormat datef=authHandler.getDateOnlyFormat();
        if (isBefore) {
            today.add(Calendar.DATE, -1 * invervalInDays);
        } else {
            today.add(Calendar.DATE, invervalInDays);
        }

        Date date = today.getTime();
        String tdate;
        try {
            tdate = datef.format(date);
            date=datef.parse(tdate);
        } catch (ParseException ex) {
            date = today.getTime();
        }
        return date;
    }
}
