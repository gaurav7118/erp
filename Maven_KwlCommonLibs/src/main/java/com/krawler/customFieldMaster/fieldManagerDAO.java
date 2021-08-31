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
package com.krawler.customFieldMaster;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface fieldManagerDAO {

    public KwlReturnObject removefield(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject insertfield(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject changefield(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getModules(HashMap<String, Object> requestParams);

    public KwlReturnObject getFieldParams(HashMap<String, Object> requestParams);

    public KwlReturnObject insertdefaultheader(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject insertcolumnheader(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject insertCustomColumnMaping(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deletefield(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject insertfieldcombodata(HashMap<String, Object> comborequestParams) throws ServiceException;

    public KwlReturnObject getCustomCombodata(HashMap<String, Object> requestParams);

    public KwlReturnObject deleteCustomCombodata(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject storeDefaultCstmData(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject insertformule(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updateDefaultheader(HashMap<String, Object> requestParams);

    public KwlReturnObject savedefaultheader(HashMap<String, Object> requestParams);

    public KwlReturnObject getDefaultHeader(HashMap<String, Object> requestParams);

    public List getRefComboNames();

    public KwlReturnObject getCustomColumnFormulae(HashMap<String, Object> requestParams);

    public KwlReturnObject saveCustomColumnFormulae(HashMap<String, Object> requestParams);

    public String getFieldComboDatadata(HashMap<String, Object> requestParams);

    public KwlReturnObject getBatchcount(HashMap<String, Object> requestParams);

    public KwlReturnObject storeDefaultCstmDataPaging(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updateFieldParams(HashMap<String, Object> requestParams);

    public void validateimportrecords(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updateColumnHeader(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updateDefaultheader(Integer fieldtype, Integer fieldmaxlen, String fieldid, String validateType);

    public KwlReturnObject updateFieldParams(Integer fieldtype, Integer fieldmaxlen, String fieldid, String companyid, Integer sendNotification, String notificationDays);

    /**
     * @working function returns the columnheader record entries using filter
     * paramters specified in hashmap
     *
     * @param requestParams hashmap contains filter parameters
     * @return - KwlReturnObject - list of columnheader record entries
     * @throws ServiceException
     */
    KwlReturnObject getColumnHeader(HashMap<String, Object> requestParams) throws ServiceException;

    List<Object[]> getColumnHeader(String companyId, List<String> headerIds);

    /**
     * @working function returns the module custom data entries using filter
     * paramters specified in hashmap
     *
     * @param requestParams hashmap contains filter parameters
     * @return KwlReturnObject - list of module custom data entries
     * @throws ServiceException
     */
    List getCustomData(HashMap<String, Object> requestParams) throws ServiceException;

    /**
     *
     * @param moduleid
     * @param companyid
     * @return
     */
    List<Object[]> getModuleCustomFormulae(int moduleid, String companyid);

    /**
     * @working function stores default value of custom column for given
     * moduleid,column number in parameters
     *
     * @param session,request,requestParams hashmap contains filter parameters
     * @throws ServiceException
     */
    public void storeDefaultCstmData(HttpServletRequest request, HashMap<String, Object> requestParams);

    /**
     * @param jarray
     * @param modulename
     * @param isNew
     * @param modulerecid
     * @return
     */
    public boolean storeCustomFields(JSONArray jarray, String modulename, boolean isNew, String modulerecid);

    /**
     * @param fieldid
     * @param name
     * @return
     */
    public KwlReturnObject addCustomComboData(String fieldid, String name, int sequence);

    /**
     * @param id
     * @param name
     * @return
     */
    public KwlReturnObject editCustomComboData(String id, String name);

    /**
     * @param companyid
     * @return
     * @throws ServiceException
     */
    public List getCustomComboNames(String companyid) throws ServiceException;

    /**
     * @param hql
     * @param params
     * @return
     */
    public List executeNativeQuery(String hql, Object[] params);

    public String getCustomFieldID(String moduleName, String fieldid, String companyid);

    String checkCustomFieldID(String moduleNames, String fieldid, String companyid);

    String checkCustomFieldIDInOppReportConfig(String fieldid, String companyid);

    String checkColumnInDuplicationPolicy(String defaultHeaderID, String companyid);

    public Boolean checkForDuplicateEntryInMasterData(String name, String configid) throws ServiceException;

    public String getMaxAutoNumber(String columnname, String tableName, String companyid, String prefix, String suffix) throws ServiceException;

    /**
     *
     * @param id
     * @return
     * @throws ServiceException
     */
    /**
     *
     * @param field
     * @param moduleid
     * @param dataMap
     * @param companyid
     * @throws ServiceException
     */
    void changeCustomDateToString(String field, int moduleid, Map<String, Object> dataMap, String companyid) throws ServiceException;

    /**
     *
     *
     * @param field
     * @param moduleid
     * @param datepattern
     * @param companyid
     * @return
     * @throws ServiceException
     */
    Map<String, Object> getCustomDataToBeChange(String field, int moduleid, DateFormat datepattern, String companyid) throws ServiceException;
}
