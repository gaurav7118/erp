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
package com.krawler.spring.accounting.customDesign;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface CustomDesignDAO {

    KwlReturnObject getDesignTemplateList(String companyid, int moduleid,String ss,String start, String limit,String isActive, String countryid, String stateid, String sort,String dir) throws ServiceException;
    KwlReturnObject getAllDesignTemplateList(String companyid, int moduleid,String templateid) throws ServiceException;
    KwlReturnObject getDesignTemplate(String templateid) throws ServiceException;
    KwlReturnObject getNewDesignTemplate(String companyid, String templateid, boolean isPreview) throws ServiceException;
    KwlReturnObject getCompanyDetails(String companyid) throws ServiceException;
    KwlReturnObject createTemplate(String companyid, String userid, int moduleid, String templatename, String templatesubtype) throws ServiceException;
    KwlReturnObject copyTemplate(String companyid, String userid, int moduleid, String templatename, String templatesubtype,String  html,String  json,String pagelayoutproperty,String pagefooterhtml,String pagefooterJSON,String pageheaderhtml,String pageheaderJSON,String sqlquery,String pagefootersqlquery,String pageheadersqlquery,String footerheader,String isnewdesign) throws ServiceException;
    boolean isDuplicateTemplate(String companyid, int moduleid, String templatename);
    KwlReturnObject getModuleDefaultTemplate(String companyid, int moduleid) throws ServiceException;
    KwlReturnObject getTemplates(JSONObject jobj) throws ServiceException;
//    KwlReturnObject resetModuleDefaultTemplate(String companyid, int moduleid) throws ServiceException;
//    KwlReturnObject checkIsDefaultTemplateSet(String companyid, int moduleid, String templateid) throws ServiceException;

    KwlReturnObject getDefaultHeaders(String moduleid, String companyid) throws ServiceException;

    KwlReturnObject getGlobalCustomFields(String companyid, int moduleid) throws ServiceException;

    KwlReturnObject getDummyValue(String fieldIDs) throws ServiceException;

    KwlReturnObject getCustomLineFields(String companyid, int moduleid) throws ServiceException;
    
    KwlReturnObject getProductCustomLineFields(String companyid, int moduleid) throws ServiceException;

    KwlReturnObject saveDesignTemplate(String companyid, String userid, String templateid, String templatename, int moduleid, String json, String html, String sqlquery, String pagelayoutproperty, String bandID) throws ServiceException;
    
    KwlReturnObject saveAsDesignTemplate(String companyid,String userid,String templateid,String templatename,int moduleid,String json,String html,String sqlquery,String pagelayoutproperty,String pagefooterhtml,String pagefooterjson,String pagefootersqlquery,String pageheaderhtml,String pageheaderjson,String pageheadersqlquery, int footerheader, String templatesubtype)throws ServiceException;

    KwlReturnObject getSQLNativeQueryResult(String recordId, String filterSubQry) throws ServiceException;
    
    KwlReturnObject getCustomerDetails(String customerid) throws ServiceException;
    
    KwlReturnObject getVendorDetails(String customerid) throws ServiceException;

    KwlReturnObject deleteCustomTemplate(String templateid, int moduleid, String companyid) throws ServiceException;

    KwlReturnObject saveActiveModeTemplate(String templateid, int moduleid, int isactive) throws ServiceException;

    KwlReturnObject getActiveDesignTemplateList(String companyid, int moduleid,String templatesubtype,String countryid,String stateid) throws ServiceException;
    
    KwlReturnObject getSummaryTerms( HashMap<String, Object> hm) throws ServiceException;
    
    KwlReturnObject getFieldParams(HashMap<String, Object> requestParams);
    
    KwlReturnObject getCompanyPreferences(String companyid) throws ServiceException;
    
    KwlReturnObject getCompanyAddress(String companyid,String isBillingAddress,String isDefaultAddress) throws ServiceException;
    
    KwlReturnObject getComboFieldParams(String companyid, int moduleid) throws ServiceException;
}