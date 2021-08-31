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
package com.krawler.spring.commonlibs.service;

import com.krawler.common.admin.Projreport_Template;
import com.krawler.common.admin.Template_Config;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportPdfTemplateDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.JSONException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public class AccExportpdfServiceImpl implements AccExportpdfService {

    private sessionHandlerImpl sessionHandlerImplObj;
    private exportPdfTemplateDAO exportPdfTemplateDAOObj;

    public void setexportPdfTemplateDAO(exportPdfTemplateDAO exportPdfTemplateDAOObj1) {
        this.exportPdfTemplateDAOObj = exportPdfTemplateDAOObj1;
    }

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    @Override
    public JSONObject getAllReportTemplate(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {

        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {

            String userid = sessionHandlerImplObj.getUserid(request);
            int templatetype = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("templatetype"))) {
                templatetype = Integer.valueOf(request.getParameter("templatetype"));
            }
            kmsg = exportPdfTemplateDAOObj.getAllReportTemplate(userid, templatetype);
            jobj = getReportTemplateJson(kmsg.getEntityList());
            jobj.put("success", kmsg.isSuccessFlag());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

    public JSONObject getReportTemplateJson(List ll) throws ServiceException, com.krawler.utils.json.base.JSONException {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                Projreport_Template obj = (Projreport_Template) ite.next();
                JSONObject jtemp = new JSONObject();

                jtemp.put("tempid", obj.getTempid());
                jtemp.put("tempname", obj.getTempname());
                jtemp.put("description", obj.getDescription());
                jtemp.put("configstr", obj.getConfigstr());
                jtemp.put("letterhead", StringUtil.isNullOrEmpty(obj.getLetterHead()) ? "" : obj.getLetterHead());
                jtemp.put("pretext", StringUtil.isNullOrEmpty(obj.getPreText()) ? "" : obj.getPreText());
                jtemp.put("posttext", StringUtil.isNullOrEmpty(obj.getPostText()) ? "" : obj.getPostText());
                jtemp.put("fieldConfig", getReportTemplateFieldsJson(obj.getTempid()));
                jarr.put(jtemp);
            }
            jobj.put("data", jarr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public JSONObject getReportTemplateFieldsJson(String tempId) throws ServiceException, com.krawler.utils.json.base.JSONException {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        KwlReturnObject kmsg = null;
        try {
            String invoiceFied[] = Constants.INVOICE_PDF_FIELDLIST;
            if (!StringUtil.isNullOrEmpty(tempId)) {
                kmsg = exportPdfTemplateDAOObj.getReportConfigForTemplate(tempId);
                if (!kmsg.getEntityList().isEmpty()) {
                    Template_Config template_Config = (Template_Config) kmsg.getEntityList().get(0);
                    if (StringUtil.isNullOrEmpty(template_Config.getFieldAttribJson())) {
                        jarr = new JSONArray();
                    } else {
                        jarr = new JSONArray(template_Config.getFieldAttribJson());
                    }
                } else {
                    jarr = getReportTemplateFieldsDefaultJson();
                }

            } else {
                jarr = getReportTemplateFieldsDefaultJson();
            }
            jobj.put("data", jarr);

        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public JSONArray getReportTemplateFieldsDefaultJson() throws ServiceException, com.krawler.utils.json.base.JSONException {
        JSONArray jarr = new JSONArray();
        KwlReturnObject kmsg = null;
        try {
            String invoiceFied[] = Constants.INVOICE_PDF_FIELDLIST;
            for (int i = 0; i < invoiceFied.length; i++) {
                JSONObject jtemp = new JSONObject();
                jtemp.put("keyid", i + 1);
                jtemp.put("keyname", invoiceFied[i]);
                jtemp.put("width", 200);
                jarr.put(jtemp);
            }


        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jarr;
    }
}
