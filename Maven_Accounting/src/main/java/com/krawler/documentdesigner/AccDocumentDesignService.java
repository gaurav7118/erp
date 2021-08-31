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
package com.krawler.documentdesigner;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccDocumentDesignService {

    public String getHTMLContentForEmailWithDDTemplate(JSONObject requestParams);

    public JSONObject getDocumentDesignerEmailTemplateJson(JSONObject requestParams);
    
    public String PrintTemplateRestUrl(JSONObject paramJobj) throws JSONException;
    
    public JSONObject getCustomTemplatesJsonForExport(Map requestParams);

    public JSONObject ImportCustomTemplates(JSONObject paramJobj, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException;

    public String getModuleName(int moduleid);

    public void ValidateHeadersCustomTemplates(JSONObject paramJobj, JSONArray validateJArray) throws AccountingException, ServiceException;

    public JSONObject modifyHtmlJsonofTemplate(JSONObject paramJobj, JSONObject jobj);

    public JSONObject createCustomFields(JSONObject paramJobj, JSONObject datajobj);
}
