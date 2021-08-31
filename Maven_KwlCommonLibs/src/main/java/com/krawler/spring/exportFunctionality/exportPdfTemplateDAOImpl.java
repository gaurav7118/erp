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
package com.krawler.spring.exportFunctionality;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.hibernate.HibernateException;

/**
 *
 * @author Karthik
 */
public class exportPdfTemplateDAOImpl extends BaseDAO implements exportPdfTemplateDAO {

    public KwlReturnObject saveReportTemplate(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        try {

            Projreport_Template proj_temp = new Projreport_Template();
            if (requestParams.containsKey("tempid") && !requestParams.get("tempid").toString().equals("")) {
                proj_temp.setTempid(requestParams.get("tempid").toString());
            }
            if (requestParams.containsKey("name") && !requestParams.get("name").toString().equals("")) {
                proj_temp.setTempname(requestParams.get("name").toString());
            }
            if (requestParams.containsKey("desc") && !requestParams.get("desc").toString().equals("")) {
                proj_temp.setDescription(requestParams.get("desc").toString());
            }
            if (requestParams.containsKey("jsondata") && !requestParams.get("jsondata").toString().equals("")) {
                proj_temp.setConfigstr(requestParams.get("jsondata").toString());
            }
            if (requestParams.containsKey("userid") && !requestParams.get("userid").toString().equals("")) {
                proj_temp.setUserid((User) get(User.class, requestParams.get("userid").toString()));
            }
            if (requestParams.containsKey("templatetype") && !requestParams.get("templatetype").toString().equals("")) {
                proj_temp.setType(Integer.valueOf(requestParams.get("templatetype").toString()));
            }
            if (requestParams.containsKey("pretext") && !requestParams.get("pretext").toString().equals("")) {
                proj_temp.setPreText(requestParams.get("pretext").toString());
            }
            if (requestParams.containsKey("posttext") && !requestParams.get("posttext").toString().equals("")) {
                proj_temp.setPostText(requestParams.get("posttext").toString());
            }
            if (requestParams.containsKey("letterhead") && !requestParams.get("letterhead").toString().equals("")) {
                proj_temp.setLetterHead(requestParams.get("letterhead").toString());
            }
            save(proj_temp);

            ll.add(proj_temp);
        } catch (HibernateException ex) {
            throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.saveReportTemplate", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public void deleteReportTemplateConfig(String templateId) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        try {

            try {
                String Hql = "delete from Template_Config where TemplateId= ?";
                executeQuery( Hql, new Object[]{(Projreport_Template) get(Projreport_Template.class, templateId)});
            } catch (ServiceException ex) {
                throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.deleteReportTemplateConfig", ex);
            }

        } catch (HibernateException ex) {
            throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.deleteReportTemplateConfig", ex);
        }
    }

    public void saveReportTemplateConfig(String templateId, String configjson) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        try {

            Template_Config temp_Config = new Template_Config();
            if (!StringUtil.isNullOrEmpty(templateId)) {
                temp_Config.setTemplateId((Projreport_Template) get(Projreport_Template.class, templateId));
            }
            if (!StringUtil.isNullOrEmpty(configjson)) {
                temp_Config.setFieldAttribJson(configjson);
            }
            saveOrUpdate(temp_Config);

            ll.add(temp_Config);
        } catch (HibernateException ex) {
            throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.saveReportTemplateConfig", ex);
        }
    }

    public KwlReturnObject getAllReportTemplate(String userid, int templateType) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String Hql = "select p from com.krawler.common.admin.Projreport_Template p where p.userid.userID=? and p.deleteflag=0 and p.type = ? ";
            if (templateType == 1) {
                Hql = "select p from com.krawler.common.admin.Projreport_Template p where (p.userid=null or p.userid.userID = ?) and p.deleteflag=0 and p.type = ? ";
            }

            ll = executeQuery( Hql, new Object[]{userid, templateType});
            dl = ll.size();
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.getAllReportTemplate", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getReportConfigForTemplate(String templateId) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String Hql = "select p from com.krawler.common.admin.Template_Config p where p.templateId.tempid=? ";
            ll = executeQuery( Hql, new Object[]{templateId});
            dl = ll.size();
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.getAllReportTemplate", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject deleteReportTemplate(String tempid) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        try {
            if (!StringUtil.isNullOrEmpty(tempid)) {
                Projreport_Template proj_temp = (Projreport_Template) load(Projreport_Template.class, tempid);
                proj_temp.setDeleteflag(1);
                save(proj_temp);

                ll.add(proj_temp);
            }
        } catch (HibernateException e) {
            throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.deleteReportTemplate", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject editReportTemplate(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        try {
            if (requestParams.containsKey("tempid") && !requestParams.get("tempid").toString().equals("")) {
                Projreport_Template proj_temp = (Projreport_Template) load(Projreport_Template.class, requestParams.get("tempid").toString());
                if (requestParams.containsKey("newconfig") && !requestParams.get("newconfig").toString().equals("")) {
                    proj_temp.setConfigstr(requestParams.get("newconfig").toString());
                }
                if (requestParams.containsKey("pretext") && !requestParams.get("pretext").toString().equals("")) {
                    proj_temp.setPreText(requestParams.get("pretext").toString());
                }
                if (requestParams.containsKey("posttext") && !requestParams.get("posttext").toString().equals("")) {
                    proj_temp.setPostText(requestParams.get("posttext").toString());
                }
                if (requestParams.containsKey("letterhead") && !requestParams.get("letterhead").toString().equals("")) {
                    proj_temp.setLetterHead(requestParams.get("letterhead").toString());
                }

                save(proj_temp);
                ll.add(proj_temp);
            }
        } catch (HibernateException e) {
            throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.editReportTemplate", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    @Override
    public KwlReturnObject getEmailTemplateFiles(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        String fType = "";
        String companyid = "";
        int file_type = 1;
        int dl = 0;
        try {
            if (requestParams.containsKey("type") && requestParams.get("type") != null) {
                fType = requestParams.get("type").toString();
                if (fType != null && fType.compareTo("img") == 0) {
                    file_type = 0;
                }
            }
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            String query = "FROM EmailTemplateFiles AS et WHERE et.type = ? AND et.creator.company.companyID = ?";
            ll = executeQuery(query, new Object[]{file_type, companyid});
            dl = ll.size();
        } catch (HibernateException ex) {

            throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.getEmailTemplateFiles", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    @Override
    public KwlReturnObject getContractFiles(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        String fileid = "";
        int dl = 0;
        try {

            if (requestParams.containsKey("fileid") && requestParams.get("fileid") != null) {
                fileid = requestParams.get("fileid").toString();
            }
            String query = "FROM ContractFiles AS cf where cf.id in (" + fileid + ")";
            ll = executeQuery(query);
            dl = ll.size();
        } catch (HibernateException ex) {

            throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.getEmailTemplateFiles", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    @Override
    public void saveEmailTemplateFile(String id, String name, String extn, Date createdOn, int type, String creatorId) throws ServiceException {
        try {
            EmailTemplateFiles fileEntry = new EmailTemplateFiles();
            fileEntry.setId(id);
            fileEntry.setCreatedon(new Date());
            fileEntry.setCreator((User) get(User.class, creatorId));
            fileEntry.setExtn(extn);
            fileEntry.setName(name);
            fileEntry.setType(type);
            save(fileEntry);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.saveEmailTemplateFiles", ex);
        }
    }

    public void saveContractsFile(String id, String name, String extn, Date createdOn, int type, String creatorId, String contractId) throws ServiceException {
        try {
            ContractFiles fileEntry = new ContractFiles();
            fileEntry.setId(id);
            fileEntry.setCreatedon(new Date());
            fileEntry.setCreator((User) get(User.class, creatorId));
            fileEntry.setExtn(extn);
            fileEntry.setName(name);
            fileEntry.setType(type);
            if (!StringUtil.isNullOrEmpty(contractId)) {
                fileEntry.setContractid(contractId);
            }
            save(fileEntry);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.saveEmailTemplateFiles", ex);
        }
    }

    @Override
    public KwlReturnObject getDuplicateTemplateName(String name, String compId) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            String new_query = "from Projreport_Template p where p.tempname = ? and p.userid.company.companyID= ? and p.deleteflag = 0 ";
            list = executeQuery(new_query, new Object[]{name, compId});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("exportPdfTemplateDAOImpl.getDuplicateTemplateName", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, null, list, count);
    }
}
