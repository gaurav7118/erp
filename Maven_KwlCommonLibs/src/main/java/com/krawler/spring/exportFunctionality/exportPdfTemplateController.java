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
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.web.resource.Links;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import com.krawler.common.util.Constants;

/**
 *
 * @author Karthik
 */
public class exportPdfTemplateController extends MultiActionController {

    private exportPdfTemplateDAO exportPdfTemplateDAOObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private String successView;
    private HibernateTransactionManager txnManager;

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setexportPdfTemplateDAO(exportPdfTemplateDAO exportPdfTemplateDAOObj1) {
        this.exportPdfTemplateDAOObj = exportPdfTemplateDAOObj1;
    }

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public ModelAndView saveReportTemplate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        TransactionStatus status = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String jsondata = request.getParameter("data");
            String userid = request.getParameter("userid");
            String name = request.getParameter("name");
            String desc = request.getParameter("desc");
            String preText = request.getParameter("pretext");
            String postText = request.getParameter("posttext");
            String letterHead = request.getParameter("letterhead");
            String configJson = request.getParameter("templateCongdata");
            String tempId = java.util.UUID.randomUUID().toString();
            int templatetype = 0;
            if (request.getParameter("templatetype") != null && !StringUtil.isNullOrEmpty(request.getParameter("templatetype"))) {
                templatetype = Integer.valueOf(request.getParameter("templatetype"));
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("jsondata", StringUtil.checkForNull(jsondata));
            requestParams.put("tempid", StringUtil.checkForNull(tempId));
            requestParams.put("userid", StringUtil.checkForNull(userid));
            requestParams.put("name", StringUtil.checkForNull(name));
            requestParams.put("desc", StringUtil.checkForNull(desc));
            requestParams.put("templatetype", templatetype);
            requestParams.put("pretext", StringUtil.checkForNull(preText));
            requestParams.put("posttext", StringUtil.checkForNull(postText));
            requestParams.put("letterhead", StringUtil.checkForNull(letterHead));

            KwlReturnObject tempcnt = exportPdfTemplateDAOObj.getDuplicateTemplateName(name, companyid);
            if (tempcnt.getEntityList().size() > 0) {
                //throw new AccountingException("Sales receipt number '" + entryNumber + "' already exists.");
                jobj.put("success", tempcnt.isSuccessFlag());
                jobj.put("duplicate", true);
                throw new ServletException();
            } else {
                //Create transaction
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setName("JE_Tx");
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
                status = txnManager.getTransaction(def);
                
                kmsg = exportPdfTemplateDAOObj.saveReportTemplate(requestParams);
                Projreport_Template projreport_Template = (Projreport_Template) kmsg.getEntityList().get(0);
                exportPdfTemplateDAOObj.saveReportTemplateConfig(projreport_Template.getTempid(), configJson);
                jobj.put("success", kmsg.isSuccessFlag());
                txnManager.commit(status);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            if(status!=null) {
                txnManager.rollback(status);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getReportTemplateJson(List ll) throws ServiceException {
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

    public ModelAndView getTemplateConfig(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        KwlReturnObject kmsg = null;
        try {
            jarr = getReportTemplateFieldsDefaultJson();
            jobj.put("data", jarr);
            jobj.put("success", true);

        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAllReportTemplate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getReportTemplateFieldsDefaultJson() throws ServiceException {
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

    public JSONObject getReportTemplateFieldsJson(String tempId) throws ServiceException {
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

    public ModelAndView deleteReportTemplate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String tempid = StringUtil.checkForNull(request.getParameter("deleteflag"));
            kmsg = exportPdfTemplateDAOObj.deleteReportTemplate(tempid);

            jobj.put("success", kmsg.isSuccessFlag());
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView editReportTemplate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        KwlReturnObject kmsg1 = null;
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String tempid = request.getParameter("edit");
            String newconfig = request.getParameter("data");
            String preText = request.getParameter("pretext");
            String configJson = request.getParameter("templateCongdata");
            String postText = request.getParameter("posttext");
            String letterHead = request.getParameter("letterhead");

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("tempid", StringUtil.checkForNull(tempid));
            requestParams.put("newconfig", StringUtil.checkForNull(newconfig));
            requestParams.put("pretext", StringUtil.checkForNull(preText));
            requestParams.put("posttext", StringUtil.checkForNull(postText));
            requestParams.put("letterhead", StringUtil.checkForNull(letterHead));

            kmsg = exportPdfTemplateDAOObj.editReportTemplate(requestParams);

            Projreport_Template projreport_Template = (Projreport_Template) kmsg.getEntityList().get(0);

            // exportPdfTemplateDAOObj.deleteReportTemplateConfig(projreport_Template.getTempid());
            exportPdfTemplateDAOObj.saveReportTemplateConfig(projreport_Template.getTempid(), configJson);
            jobj.put("success", kmsg.isSuccessFlag());
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getEmailTemplateFiles(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("type", request.getParameter("type"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject kmsg = exportPdfTemplateDAOObj.getEmailTemplateFiles(requestParams);
            jobj = getEmailTemplateFilesJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getContractFiles(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String fileId = request.getParameter("fileid");
            String fileidStr = "";
            if (!StringUtil.isNullOrEmpty(fileId)) {
                String fileIdarr[] = fileId.split(",");
                for (int i = 0; i < fileIdarr.length; i++) {
                    fileidStr += "'" + fileIdarr[i] + "',";
                }
                if (!StringUtil.isNullOrEmpty(fileidStr)) {
                    fileidStr = fileidStr.substring(0, fileidStr.length() - 1);
                }
            }
            requestParams.put("fileid", fileidStr);
            KwlReturnObject kmsg = exportPdfTemplateDAOObj.getContractFiles(requestParams);
            jobj = getContractFilesjson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getContractFilesjson(List ll, HttpServletRequest request, int totalSize) {
        JSONObject jobj = new JSONObject();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String domainURL = URLUtil.getPageURL(request, Links.loginpageFull);
            String fType = request.getParameter("type");

            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                JSONObject temp = new JSONObject();
                ContractFiles obj = (ContractFiles) ite.next();
                temp.put("id", obj.getId());
                temp.put("filename", obj.getName());
                temp.put("imgname", obj.getName());
                jobj.append("data", temp);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

    public JSONObject getEmailTemplateFilesJson(List ll, HttpServletRequest request, int totalSize) {
        JSONObject jobj = new JSONObject();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String domainURL = URLUtil.getPageURL(request, Links.loginpageFull);
            String fType = request.getParameter("type");

            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                JSONObject temp = new JSONObject();
                EmailTemplateFiles obj = (EmailTemplateFiles) ite.next();
                temp.put("id", obj.getId());
                temp.put("imgname", obj.getName());
                String url = domainURL + "video.jsp?c=" + companyid + "&f=" + obj.getId().concat(obj.getExtn()) + "&t=" + fType;
                temp.put("url", url);
                jobj.append("data", temp);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

    public ModelAndView saveEmailTemplateFiles(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        JSONObject result = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            result.put("success", false);
            int file_type = 1;
            String fType = request.getParameter("type");
            if (fType != null && fType.compareTo("img") == 0) {
                file_type = 0;
            }
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String filename = "";
            String tempPath = System.getProperty("java.io.tmpdir");
            ServletFileUpload fu = new ServletFileUpload(new DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, new File(tempPath)));
            if (fu.isMultipartContent(request)) {
                List<FileItem> fileItems = fu.parseRequest(request);
                for (FileItem field : fileItems) {
                    if (!field.isFormField()) {
                        String fname = new String(field.getName().getBytes(), "UTF8");
                        String file_id = java.util.UUID.randomUUID().toString();
                        String file_extn = fname.substring(fname.lastIndexOf("."));
                        filename = file_id.concat(file_extn);
                        boolean isUploaded = false;
                        fname = fname.substring(fname.lastIndexOf("\\") + 1);
                        if (field.getSize() != 0) {
                            String basePath = StorageHandler.GetDocStorePath() + companyid + "/" + fType;
                            File destDir = new File(basePath);
                            if (!destDir.exists()) {
                                destDir.mkdirs();
                            }

                            File uploadFile = new File(basePath + "/temp_" + filename);
                            field.write(uploadFile);

                            String path = uploadFile.getPath().toString();
                            int width = 0;
                            int height = 0;
                            BufferedImage img = null;
                            try {
                                img = ImageIO.read(new File(path));
                                width = img.getWidth();
                                height = img.getHeight();
                                if (width <= 600 && height <= 500) {
                                    File uploadFile1 = new File(basePath + "/" + filename);
                                    uploadFile.renameTo(uploadFile1);
                                } else {
                                    uploadFile.delete();
                                    result.put("success", false);
                                    result.put("msg", "Image dimension cannot exceed more than 600x500 pixel");
                                    txnManager.rollback(status);
                                    return new ModelAndView("jsonView", "model", result.toString());

                                }
                            } catch (Exception e) {
                            }

                            isUploaded = true;
                            String id = request.getParameter("fileid");
                            if (StringUtil.isNullOrEmpty(id)) {
                                id = file_id;
                            }

                            exportPdfTemplateDAOObj.saveEmailTemplateFile(id, fname, file_extn, new Date(), file_type, sessionHandlerImplObj.getUserid(request));
                        }
                    }
                }
            }
            txnManager.commit(status);
            result.put("success", true);
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
            try {
                result.put("msg", e.getMessage());
            } catch (Exception je) {
            }
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
            try {
                result.put("msg", e.getMessage());
            } catch (Exception je) {
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
            try {
                result.put("msg", e.getMessage());
            } catch (Exception je) {
            }
        }
        return new ModelAndView("jsonView", "model", result.toString());
    }

    public ModelAndView saveContractsFiles(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        JSONObject result = new JSONObject();
        String uploadFileId = "";
        JSONArray fileIdArray = new JSONArray();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            result.put("success", false);
            int file_type = 1;
            String fType = request.getParameter("type");
            String contractId = request.getParameter("contractid");
            if (fType != null && fType.compareTo("img") == 0) {
                file_type = 0;
            }
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String filename = "";
            String tempPath = System.getProperty("java.io.tmpdir");
            ServletFileUpload fu = new ServletFileUpload(new DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, new File(tempPath)));
            if (fu.isMultipartContent(request)) {
                List<FileItem> fileItems = fu.parseRequest(request);
                for (FileItem field : fileItems) {
                    if (!field.isFormField()) {
                        String fname = new String(field.getName().getBytes(), "UTF8");
                        String file_id = java.util.UUID.randomUUID().toString();
                        String file_extn = fname.substring(fname.lastIndexOf("."));
                        filename = file_id.concat(file_extn);
                        boolean isUploaded = false;
                        fname = fname.substring(fname.lastIndexOf("\\") + 1);
                        if (field.getSize() != 0) {
                            String basePath = StorageHandler.GetDocStorePath() + companyid + "/" + fType;
                            File destDir = new File(basePath);
                            if (!destDir.exists()) {
                                destDir.mkdirs();
                            }

                            File uploadFile = new File(basePath + "/temp_" + filename);
                            field.write(uploadFile);

                            String path = uploadFile.getPath().toString();
                            int width = 0;
                            int height = 0;
                            BufferedImage img = null;
                            try {
                                img = ImageIO.read(new File(path));
                                width = img.getWidth();
                                height = img.getHeight();
                                if (width <= 600 && height <= 500) {
                                    File uploadFile1 = new File(basePath + "/" + filename);
                                    uploadFile.renameTo(uploadFile1);
                                } else {
                                    uploadFile.delete();
                                    result.put("success", false);
                                    result.put("msg", "Image dimension cannot exceed more than 600x500 pixel");
                                    txnManager.rollback(status);
                                    return new ModelAndView("jsonView", "model", result.toString());

                                }
                            } catch (Exception e) {
                            }

                            isUploaded = true;
                            String id = request.getParameter("fileid");
                            if (StringUtil.isNullOrEmpty(id)) {
                                id = file_id;
                            }

                            exportPdfTemplateDAOObj.saveContractsFile(id, fname, file_extn, new Date(), file_type, sessionHandlerImplObj.getUserid(request), contractId);
                            uploadFileId = file_id;
                        }
                    }
                }
            }
            txnManager.commit(status);
            result.put("success", true);
            result.put("file", uploadFileId);
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
            try {
                result.put("msg", e.getMessage());
            } catch (Exception je) {
            }
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
            try {
                result.put("msg", e.getMessage());
            } catch (Exception je) {
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
            try {
                result.put("msg", e.getMessage());
            } catch (Exception je) {
            }
        }
        return new ModelAndView("jsonView", "model", result.toString());
    }
}
