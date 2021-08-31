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
package com.krawler.spring.common;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.FileUploadHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.profileHandler.profileHandlerDAO;

import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import java.math.BigInteger;
import javax.servlet.ServletException;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.hql.accounting.AccountingException;
import org.springframework.context.NoSuchMessageException;
import org.springframework.transaction.TransactionException;
import com.krawler.spring.importFunctionality.ImportHandler;
import java.util.*;

/**
 *
 * @author krawler
 */
public class CommonFnController extends MultiActionController implements MessageSourceAware {

    private profileHandlerDAO profileHandlerDAOObj;
    private HibernateTransactionManager txnManager;
    private AccCommonTablesDAO accCommonTablesDAO;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private auditTrailDAO auditTrailObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private APICallHandlerService apiCallHandlerService;    
    private CommonFnControllerService commonFnControllerService;
    private ImportHandler importHandler;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }

    public void setPermissionHandlerDAOObj(permissionHandlerDAO permissionHandlerDAOObj) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj;
    }

    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj1) {
        this.profileHandlerDAOObj = profileHandlerDAOObj1;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }
    

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
        
    public ModelAndView saveUsers(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, JSONException, SessionExpiredException, ServiceException, ServletException {
        HashMap hm = null;
        JSONObject jobj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            hm = new FileUploadHandler().getItems(request);
            HashMap<String, Object> requestMap = commonFnControllerService.generateMap(hm);
            jobj = commonFnControllerService.saveUsers(requestMap, paramJobj,hm);
        } catch (Exception ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public ModelAndView saveTemplateImageUsers(HttpServletRequest request, HttpServletResponse response) {
        HashMap hm = null;
        String msg = "";
        String companyid = "";

        Boolean success = false;
        JSONObject jobj = new JSONObject();

        try {
            hm = new FileUploadHandler().getItems(request);
            companyid = sessionHandlerImpl.getCompanyid(request);
            if (hm.containsKey("logo") && hm.get("logo") != null) {
                String imageName = ((FileItem) (hm.get("logo"))).getName();
                if (imageName != null && imageName.length() > 0) {

                    FileItem fileItem = (FileItem) hm.get("logo");
                    if (fileItem.getSize() > 0) {
                        String fileName1 = companyid + "_template_temp" + FileUploadHandler.getCompanyImageExt();
                        String fileName = companyid + "_template" + FileUploadHandler.getCompanyImageExt();
                        new FileUploadHandler().uploadFile((FileItem) hm.get("logo"), fileName1, StorageHandler.GetDocStorePath());

                        File uploadFile = new File(StorageHandler.GetDocStorePath() + fileName1);


                        String path = uploadFile.getPath().toString();
                        int width = 0;
                        int height = 0;
                        BufferedImage img = null;
                        try {
                            img = ImageIO.read(new File(path));
                            width = img.getWidth();
                            height = img.getHeight();
                            if (width < 400 || height < 80) {
                                uploadFile.delete();
                                jobj.put("success", false);
                                jobj.put("msg", messageSource.getMessage("acc.common.imagedimension", null, RequestContextUtils.getLocale(request)));
                                return new ModelAndView("jsonView", "model", jobj.toString());
                            } else {
                                File uploadFile1 = new File(StorageHandler.GetDocStorePath() + fileName);
                                uploadFile.renameTo(uploadFile1);
                            }
                        } catch (Exception e) {
                            jobj.put("success", false);
                            jobj.put("msg", messageSource.getMessage("acc.common.invalidfiletype", null, RequestContextUtils.getLocale(request)));
                            return new ModelAndView("jsonView", "model", jobj.toString());
                        }
                    } else {
                        jobj.put("success", false);
                        jobj.put("msg", messageSource.getMessage("acc.common.Imageshouldnotempty", null, RequestContextUtils.getLocale(request)));
                        return new ModelAndView("jsonView", "model", jobj.toString());
                    }
                }
            }
            success = true;
            msg = messageSource.getMessage("acc.common.savetemplateheader", null, RequestContextUtils.getLocale(request));
            auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_UPLOAD, "User " + sessionHandlerImpl.getUserFullName(request) + " has uploaded template logo ", request, "12");

        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "saveTemplateImageUsers", ex);
        } catch (ServletException | SessionExpiredException | JSONException | NoSuchMessageException ex) {
            msg = "" + ex.getMessage();
            try {
                msg = messageSource.getMessage(msg, null, RequestContextUtils.getLocale(request));
            } catch (Exception e) {
                msg = msg;
            }
            success = false;
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "saveTemplateImageUsers", ex);
        } finally {
            try {
                jobj.put("success", success);
                jobj.put("msg", msg);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveDesignTemplateImage(HttpServletRequest request, HttpServletResponse response) {
        HashMap hm = null;
        String msg = "";
        Boolean success = false;
        JSONObject jobj = new JSONObject();

        try {
            hm = new FileUploadHandler().getItems(request);
            if (hm.containsKey("logo") && hm.get("logo") != null) {
                String imageName = ((FileItem) (hm.get("logo"))).getName();
                if (imageName != null && imageName.length() > 0) {

                    FileItem fileItem = (FileItem) hm.get("logo");
                    if (fileItem.getSize() > 0) {
                        String uuid = StringUtil.generateUUID();
                        String Ext = "";
                        String fileName = imageName;
                        if (fileName.contains(".")) {
                            Ext = fileName.substring(fileName.lastIndexOf("."));
                        }
                        fileName = uuid + Ext;
                        new FileUploadHandler().uploadFile((FileItem) hm.get("logo"), fileName, StorageHandler.GetDocStorePath());

                        jobj.put("path", fileName);
                    } else {
                        jobj.put("success", false);
                        jobj.put("msg", "Image should not empty.");
                        return new ModelAndView("jsonView", "model", jobj.toString());
                    }
                }
            }
            success = true;
            msg = "Template header image uploaded successfully.";


        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "saveTemplateImageUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "saveTemplateImageUsers", ex);
        } finally {
            try {
                jobj.put("success", success);
                jobj.put("msg", msg);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

 public ModelAndView saveDashboardImage(HttpServletRequest request, HttpServletResponse response) {
        HashMap hm = null;
        String msg = "";
        String companyid = "";

        Boolean success = false;
        JSONObject jobj = new JSONObject();

        try {
            hm = new FileUploadHandler().getItems(request);
            companyid = sessionHandlerImpl.getCompanyid(request);
            if (hm.containsKey("logo") && hm.get("logo") != null) {
                String imageName = ((FileItem) (hm.get("logo"))).getName();
                if (imageName != null && imageName.length() > 0) {

                    FileItem fileItem = (FileItem) hm.get("logo");
                    if (fileItem.getSize() > 0) {
                        String fileName1 = companyid + "_dashboard_temp" + FileUploadHandler.getCompanyImageExt();
                        String fileName = companyid + "_dashboard" + FileUploadHandler.getCompanyImageExt();
                        new FileUploadHandler().uploadFile((FileItem) hm.get("logo"), fileName1, StorageHandler.GetDocStorePath());

                        File uploadFile = new File(StorageHandler.GetDocStorePath() + fileName1);
                    

                        String path = uploadFile.getPath().toString();
                        int width = 0;
                        int height = 0;
                        BufferedImage img = null;
                        try {
                            img = ImageIO.read(new File(path));
                            width = img.getWidth();
                            height = img.getHeight();
                            if (width < 800|| height < 600) {
                                uploadFile.delete();
                                jobj.put("success", false);
                                jobj.put("msg", "Image dimension cannot less than 800x600 pixel");
                                return new ModelAndView("jsonView", "model", jobj.toString());
                            }else{
                                File uploadFile1 = new File(StorageHandler.GetDocStorePath() + fileName);
                                uploadFile.renameTo(uploadFile1);
                            }
                        } catch (Exception e) {
                            jobj.put("success", false);
                            jobj.put("msg", "Error occurred while uploading template image.");
                            return new ModelAndView("jsonView", "model", jobj.toString());
                        }
                    } else {
                        jobj.put("success", false);
                        jobj.put("msg", "Image should not empty.");
                        return new ModelAndView("jsonView", "model", jobj.toString());
                    }
                }
            }
            success = true;
            msg = messageSource.getMessage("acc.companypreferences.dashboardBackImage.success", null, RequestContextUtils.getLocale(request));


        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "saveTemplateImageUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "saveTemplateImageUsers", ex);
        } finally {
            try {
                jobj.put("success", success);
                jobj.put("msg", msg);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView setPDFTemplate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String header = (StringUtil.isNullOrEmpty(request.getParameter("pdfheader"))) ? "" : request.getParameter("pdfheader");
            String pretext = (StringUtil.isNullOrEmpty(request.getParameter("pdfpretext"))) ? "" : request.getParameter("pdfpretext");
            String posttext = (StringUtil.isNullOrEmpty(request.getParameter("pdfposttext"))) ? "" : request.getParameter("pdfposttext");
            String footer = request.getParameter("pdffooter");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            int module = Integer.parseInt(request.getParameter("module"));
            String id = StringUtil.generateUUID();
            if (StringUtil.isNullOrEmpty(footer)) {
                footer = "";
            }
            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();

            pdfTemplateMap.put("companyid", companyid);
            pdfTemplateMap.put("module", module);
            pdfTemplateMap.put("pdfheader", header);
            pdfTemplateMap.put("pdffooter", footer);
            pdfTemplateMap.put("pdfpretext", pretext);
            pdfTemplateMap.put("pdfposttext", posttext);
            JSONObject obj = new JSONObject();
            obj = getPDFConfigDuplicate(module, companyid);
            if (obj.getBoolean("duplicate") == true) {
                pdfTemplateMap.put("ID", obj.getString("id"));
                kmsg = accCommonTablesDAO.updatePDFTemplate(pdfTemplateMap);
            } else {
                pdfTemplateMap.put("ID", id);
                kmsg = accCommonTablesDAO.setPDFTemplate(pdfTemplateMap);
            }

            jobj.put("success", true);
            jobj.put("msg", "acc.field.PDFTemplateinformationupdatedsuccessfully");
            String modulename = request.getParameter("modulename");
            auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CUSTOMIZE, "User " + sessionHandlerImpl.getUserFullName(request) + " has customize PDF template for module " + modulename, request, "12");
            txnManager.commit(status);
        } catch (Exception e) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "Error occurred while saving PDF Footer.");
            } catch (JSONException e1) {
                System.out.println(e1.getMessage());
            }
            System.out.println(e.getMessage());
            txnManager.rollback(status);

        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private JSONObject getPDFConfigDuplicate(int module, String companyid) throws JSONException {
        JSONObject obj = new JSONObject();
        boolean duplicate = false;
        String id = null;
        try {
            List<PdfTemplateConfig> ll = new ArrayList();
            KwlReturnObject templateConfig = null;
            int tempmodule;
            templateConfig = accCommonTablesDAO.getPDFTemplateConfig(companyid);
            ll = templateConfig.getEntityList();
            for (PdfTemplateConfig config : ll) {
                id = config.getID();
                tempmodule = config.getModule();
                if (tempmodule == module) {
                    duplicate = true;
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            obj.put("duplicate", duplicate);
            obj.put("id", id);
        }
        return obj;
    }

    public ModelAndView getPDFTemplateRow(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String footer = "", header = "", preText = "", postText = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            int module = Integer.parseInt(request.getParameter("module"));
            KwlReturnObject returnObject = accCommonTablesDAO.getPDFTemplateRow(companyid, module);
            List<PdfTemplateConfig> list = returnObject.getEntityList();

            for (PdfTemplateConfig config : list) {
                footer = config.getPdfFooter() == null ? "" : config.getPdfFooter();
                header = config.getPdfHeader() == null ? "" : config.getPdfHeader();
                postText = config.getPdfPostText() == null ? "" : config.getPdfPostText();
                preText = config.getPdfPreText() == null ? "" : config.getPdfPreText();
            }
            jobj.put("success", true);
            jobj.put("footer", footer);
            jobj.put("header", header);
            jobj.put("pretext", preText);
            jobj.put("posttext", postText);

            txnManager.commit(status);
        } catch (Exception e) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "Error occurred while saving PDF Footer.");
            } catch (JSONException e1) {
                System.out.println(e1.getMessage());
            }
            System.out.println(e.getMessage());
            txnManager.rollback(status);

        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getGridConfig(HttpServletRequest request, HttpServletResponse response)throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            JSONArray jarr = new JSONArray();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);
            String moduleid = request.getParameter("moduleid");
            boolean isdocumentEntryForm = StringUtil.isNullOrEmpty(request.getParameter("isdocumentEntryForm")) ? false : Boolean.parseBoolean(request.getParameter("isdocumentEntryForm").toString());
            HashMap<String, Object> configParam = new HashMap<>();
            configParam.put("companyid", companyid);
            configParam.put("moduleid", moduleid);
            configParam.put("userid", userid);
            configParam.put("isdocumentEntryForm", isdocumentEntryForm);
            kmsg = accCommonTablesDAO.getGridConfig(configParam);
            String rules = "{rules:[]}";
            String states = "{columns:false}";
            Iterator ite = kmsg.getEntityList().iterator();
            if (ite.hasNext()) {
                GridConfig gc = (GridConfig) ite.next();
                JSONObject obj = new JSONObject();
                if (gc.getRules() != null && !gc.getRules().equals("")) {
                    rules = gc.getRules();
                }
                if (gc.getState() != null && !gc.getState().equals("")) {
                    states = gc.getState();
                }

                obj.put("cid", gc.getCid());
                obj.put("state", new JSONObject(states));
                obj.put("rules", new JSONObject(rules));
//                obj.put("isnewconfigsaved", gc.isIsNewConfigSaved());
                jarr.put(obj);
            } else {
                JSONObject obj = new JSONObject();
                obj.put("cid", "");
                obj.put("state", new JSONObject(states));
                obj.put("rules", new JSONObject(rules));
//                obj.put("isnewconfigsaved", false);
                jarr.put(obj);
            }
            jobj.put("data", jarr);
            jobj.put("success", true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveGridConfig(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        synchronized (this) {
            JSONObject jobj1 = new JSONObject();
            JSONArray jarr = new JSONArray();
            //Create transaction
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("JE_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);

            TransactionStatus status = txnManager.getTransaction(def);
            try {
                JSONObject jobj = new JSONObject();
                GridConfig cm = null;
                GridConfig gridconfig = null;
                String cmuuid = "";
                String cid = ""; //= request.getParameter("cid");
                String companyid=sessionHandlerImpl.getCompanyid(request);
                String userid=sessionHandlerImpl.getUserid(request);
                String moduleid=request.getParameter("moduleid");
//                String isNewConfigSaved = request.getParameter("isnewconfigsaved");
                boolean isdocumentEntryForm = StringUtil.isNullOrEmpty(request.getParameter("isdocumentEntryForm"))?false:Boolean.parseBoolean(request.getParameter("isdocumentEntryForm").toString());
                HashMap<String,Object> configParam=new HashMap<>();
                configParam.put("companyid", companyid);
                configParam.put("moduleid", moduleid);
                configParam.put("userid", userid);
                configParam.put("isdocumentEntryForm", isdocumentEntryForm);
                KwlReturnObject kmsg = accCommonTablesDAO.getGridConfig(configParam);
                Iterator<GridConfig> ite = kmsg.getEntityList().iterator();
                gridconfig = ite.hasNext() ? ite.next() : null;
                if (gridconfig == null) {
                    cid = "";
                    cmuuid = java.util.UUID.randomUUID().toString();
                    jobj.put("cmuuid", cmuuid);
                    jobj.put("moduleid", moduleid);
                    jobj.put("userid", userid);
                    jobj.put("companyid", companyid);
                }
                jobj.put("cid", gridconfig != null ? gridconfig.getCid() : cid);
                if (request.getParameter("rules") != null) {
                    String rule = request.getParameter("rules");
                    jobj.put("rule", rule);
                }
                if (request.getParameter("state") != null) {
                    String state = request.getParameter("state");
                    jobj.put("state", state);
                }
                jobj.put("isdocumentEntryForm", isdocumentEntryForm);
                
                jobj.put("updatedon", System.currentTimeMillis());
//                jobj.put("isnewconfigsaved", isNewConfigSaved);
                cm = accCommonTablesDAO.saveGridConfig(jobj);
                JSONObject obj = new JSONObject();

//            String states = "{columns:false}"; //Used for saveGridState
                String rules = "{rules:[]}";
                String states = "{state:{}}";
                if (!StringUtil.isNullOrEmpty(cm.getRules())) {
                    rules = cm.getRules();
                }
                if (!StringUtil.isNullOrEmpty(cm.getState())) {
                    states = cm.getState();
                }

                obj.put("cid", cm.getCid());
                obj.put("state", new JSONObject(states));
                obj.put("rules", new JSONObject(rules));
                jarr.put(obj);

                jobj1.put("data", jarr);
                jobj1.put("success", true);
                txnManager.commit(status);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                txnManager.rollback(status);
            }
            return new ModelAndView("jsonView", "model", jobj1.toString());
        }
    }

    public ModelAndView changeUserPassword(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
//            String platformURL = this.getServletContext().getInitParameter("platformURL");
            String platformURL = URLUtil.buildRestURL("platformURL");
            platformURL = platformURL + "company/userpassword";
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("currentpassword", StringUtil.checkForNull(request.getParameter("currentpassword")));
            requestParams.put("changepassword", StringUtil.checkForNull(request.getParameter("changepassword")));
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("remoteapikey", ConfigReader.getinstance().get("remoteapikey"));


            kmsg = profileHandlerDAOObj.changeUserPassword(platformURL, requestParams);
            jobj = (JSONObject) kmsg.getEntityList().get(0);
            txnManager.commit(status);
            auditTrailObj.insertAuditLog(AuditAction.PASSWORD_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has changed password ", request, "12");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCompanyTypes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            KwlReturnObject result = accCommonTablesDAO.getCompanyTypes();
            List ll = result.getEntityList();
            Iterator itr = ll.iterator();

            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                CompanyType cType = (CompanyType) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", cType.getID());
                obj.put("name", messageSource.getMessage("acc.ct." + cType.getID(), null, RequestContextUtils.getLocale(request)));
                obj.put("details", cType.getDetails());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (Exception ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "CommonFnController.getCompanyType : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * @deprecated use {@link #updateInventoryLevel(JSONObject paramjobj, JSONObject jSONObject, String url, String action)}
     */
    @Deprecated
    public ModelAndView updateInventoryLevel(HttpServletRequest request, JSONObject jSONObject, String url, String action) {
        JSONObject jobj = new JSONObject();
        
        try {
            /*Get request parameters */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            updateInventoryLevel(paramJobj, jSONObject, url, action, jobj);
        } catch (Exception ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "InventoryManagement.InvProductSync", ex);
        }
        
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView updateInventoryLevel(JSONObject paramjobj, JSONObject jSONObject, String url, String action) {
        JSONObject jobj = new JSONObject();

        try {
            updateInventoryLevel(paramjobj, jSONObject, url, action, jobj);
        } catch (Exception ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "InventoryManagement.InvProductSync", ex);
        }

        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public void updateInventoryLevel(JSONObject paramJobj, JSONObject jSONObject, String url, String action, JSONObject jobj) {
        String msg = "";
        boolean issuccess = false;
        try {
            String companyid = "";
            if (paramJobj.has(Constants.companyKey) && paramJobj.get(Constants.companyKey) != null) {
                companyid = (String) paramJobj.get(Constants.companyKey);
            }
            
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            if (paramJobj.has("userid") && paramJobj.get("userid") != null) {
                userData.put("userid", (String) paramJobj.get("userid"));
            }
            userData.put("companyid", companyid);

            userData.put("data", jSONObject);
            JSONObject resObj = apiCallHandlerService.callApp(url, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                issuccess = resObj.getBoolean("success");
                jobj.put("success", true);
                jobj.put("msg", msg);
                jobj.put("companyexist", resObj.optBoolean("companyexist"));
            }
        } catch (Exception ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "InventoryManagement.InvProductSync", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "InventoryManagement.InvProductSync", ex);
            }
        }
    }

    public ModelAndView updateInventoryLevelJson(JSONObject paramJobj, JSONObject jSONObject, String url, String action) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        //Session  session =null;
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", paramJobj.getString("lid"));
            userData.put("companyid", companyid);

            userData.put("data", jSONObject);
            JSONObject resObj = apiCallHandlerService.callApp(url, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                issuccess = resObj.getBoolean("success");
                //msg=resObj.getString("msg");
                jobj.put("success", true);
                jobj.put("msg", msg);
                jobj.put("companyexist", resObj.optBoolean("companyexist"));
            }
        } catch (Exception ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "InventoryManagement.InvProductSync", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "InventoryManagement.InvProductSync", ex);
            }
//            HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    

    public ModelAndView setDefaultModuleTemplate(HttpServletRequest request, HttpServletResponse response) {
        HashMap hashMap = new HashMap();
        JSONObject jobj = new JSONObject();
        Boolean isSuccess = true;
        String defaultId = "";
        String defaultTemplateName = "";
        String msg = "";
        try {
            hashMap.put("companyid", sessionHandlerImpl.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleId"))) {
                int moduleId = Integer.parseInt(request.getParameter("moduleId"));
                hashMap.put("moduleId", moduleId);
            }

            /*
             * will give list of templates and the defaulttemplateId for the
             * given module
             */
            KwlReturnObject result = accountingHandlerDAOobj.getModuleTemplates(hashMap);
            List list = result.getEntityList();
            Iterator it = list.iterator();
            while (it.hasNext()) {
                ModuleTemplate mt = (ModuleTemplate) it.next();
                if (mt.isIsdefaulttemplate()) {
                    defaultId = mt.getTemplateId();
                    defaultTemplateName = mt.getTemplateName();
                }
            }
            /*
             * Will check if we are editing the default template's value or
             * template other than default template
             *
             */
            if ((defaultId != null && defaultId.equals(request.getParameter("templateId").toString())) || StringUtil.isNullOrEmpty(defaultId)) {
                jobj = setDefaultModuleTemplate(request);
            } else {
                jobj.put("defaultTemplateName", defaultTemplateName);
                jobj.put("defaultTemplateAlreadySet", true);
            }
        } catch (Exception ex) {
            isSuccess = false;
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            msg = messageSource.getMessage("acc.nee.template.update.ErrorUpdating", null, RequestContextUtils.getLocale(request));
        } finally {
            try {
                jobj.put("success", isSuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     
    public ModelAndView getModuleTemplate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject paramJobj = new JSONObject();
        boolean isSuccess = false;
        try {
            paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj = commonFnControllerService.getModuleTemplate(paramJobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                paramJobj.put("success", isSuccess);
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", paramJobj.toString());
    }
    public JSONObject setDefaultModuleTemplate(HttpServletRequest request) {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap hashMap = new HashMap();
            hashMap.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleId"))) {
                int moduleId = Integer.parseInt(request.getParameter("moduleId"));
                hashMap.put("moduleId", moduleId);
            }
            String templateId = request.getParameter("templateId");
            String isdefaulttemplate = request.getParameter("isdefaulttemplate");
            hashMap.put("templateId", templateId);
            hashMap.put("isdefaulttemplate", isdefaulttemplate);
            accountingHandlerDAOobj.setDefaultModuleTemplates(hashMap);
            isSuccess = true;
        } catch (SessionExpiredException ex) {
            isSuccess = false;
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", isSuccess);
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return jobj;
        }
    }

    
    public ModelAndView getGlobalRolePermissions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String roleid = request.getParameter("roleid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyId);
            requestParams.put("roleid", roleid);
            kmsg = accCommonTablesDAO.getRolePermission(requestParams);
            jobj = getRolePermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView setRolePermissions(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        KwlReturnObject kmsg = null;
        KwlReturnObject kmsg1 = null;
        JSONObject jobj = new JSONObject();
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String userid = request.getParameter("userid");
            String rolename = request.getParameter("rolename");
            String desc = request.getParameter("description");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String[] features = request.getParameterValues("features");
            String[] permissions = request.getParameterValues("permissions");
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyId);
            requestParams.put("userid", userid);
            requestParams.put("roleid", request.getParameter("roleid"));
            requestParams.put("rolename", rolename);
            requestParams.put("desc", desc);
            requestParams.put("isEdit", isEdit);
            boolean checkdefaultpermission = true;
            boolean isUsernameExit = false;
            boolean msgflag = true;
            kmsg = accCommonTablesDAO.isUsernameExitornot(requestParams);
            if (kmsg.getRecordTotalCount() != 0) {
                String msg = "Provided rolename" + " " + "<b>" + rolename + "</b>" + " " + "already exists !";
                jobj.put("isExitusername", true);
                jobj.put("msg", msg);

                throw new Exception(msg);
            } else if (isEdit) {
                String roleid = request.getParameter("roleid").toString();
                if (!StringUtil.isNullOrEmpty(roleid) && (roleid.equals("1"))) {
                    String msg =  messageSource.getMessage("acc.rolemanagement.ThisistheDefaultRolename", null, RequestContextUtils.getLocale(request))+ " " + "<b>" + rolename + ".</b>" + " " + messageSource.getMessage("acc.rolemanagement.Youcan'tupdatedefaultrolename", null, RequestContextUtils.getLocale(request));
                    jobj.put("isExitadmin", true);
                    jobj.put("msg", msg);
                    throw new Exception(msg);
                }

                KwlReturnObject kmsg2 = accCommonTablesDAO.getRolePermission(requestParams);
                KwlReturnObject kmsgusers = accCommonTablesDAO.getUserList(companyId, roleid);
                if (kmsg2.getRecordTotalCount() != 0 && kmsgusers.getRecordTotalCount() != 0) {

                    ArrayList featurelist = new ArrayList();
                    ArrayList permissionslist = new ArrayList();
                    String[] featuresold = new String[kmsg2.getRecordTotalCount()];
                    String[] permissionsold = new String[kmsg2.getRecordTotalCount()];
                    Iterator iteuserid = kmsgusers.getEntityList().iterator();
                    while (iteuserid.hasNext()) {
                        checkdefaultpermission = true;
                        HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                        User rowuserid = (User) iteuserid.next();
                        String alluserid = rowuserid.getUserID();
                        Iterator ite = kmsg2.getEntityList().iterator();
                        int featurecount = 0;
                        while (ite.hasNext()) {
                            Object[] row = (Object[]) ite.next();
                            if (!featurelist.contains(row[2])) {

                                featuresold[featurecount] = row[2].toString();
                                permissionsold[featurecount] = row[1].toString();
                                requestParams1.put("featureid", featuresold[featurecount]);
                                requestParams1.put("userid", alluserid);
                                requestParams1.put("companyid", companyId);
                                requestParams1.put("roleid", request.getParameter("roleid"));
                                kmsg = accCommonTablesDAO.getAllUserPermission(requestParams1);
                                Iterator itr2 = kmsg.getEntityList().iterator();
                                while (itr2.hasNext()) {
                                    BigInteger row1 = (BigInteger) itr2.next();
                                    if ((row1.longValue() != Long.parseLong(permissionsold[featurecount]))) {
                                        checkdefaultpermission = false;
                                        msgflag = false;
                                        break;
                                    }
                                }

                                featurecount++;

                            }

                        }
                        if (checkdefaultpermission) {
                            KwlReturnObject kmsgup = permissionHandlerDAOObj.setPermissions(requestParams1, features, permissions);
                        }
                    }

                }
            }

            kmsg1 = accCommonTablesDAO.setRolePermissions(requestParams, features, permissions);

            if (msgflag) {
                jobj.put("msg", isEdit ? messageSource.getMessage("acc.rem.perupdate", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.rem.172", null, RequestContextUtils.getLocale(request)));
            } else {
                jobj.put("msg", messageSource.getMessage("acc.rem.defaultrolepermission", null, RequestContextUtils.getLocale(request)));
            }

            //auditTrailDAOObj.insertAuditLog(AuditAction.ADMIN_Permission, "User "+sessionHandlerImpl.getUserFullName(request)+" updated permissions for user " + userLogin.getUser().getFirstName() + " " + userLogin.getUser().getLastName()+kmsg1.getMsg(),request, userid);
            txnManager.commit(status);
            
            String auditMsg = isEdit ? " has updated Role Permission " : " has added Role Permission ";
            auditTrailObj.insertAuditLog(AuditAction.SETROLE_PERMISSION, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg, request, "254");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
      public JSONObject getRolePermissionJson(List ll, HttpServletRequest request, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        ArrayList featurelist = new ArrayList();

        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                if (!featurelist.contains(row[2])) {
                    featurelist.add(row[2]);

                    JSONObject obj = new JSONObject();
                    obj.put("permission", row[1]);
                    obj.put("featureid", row[2]);

                    jarr.put(obj);
                }
            }
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
     public JSONObject getRoleJson(List ll, HttpServletRequest request, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                Rolelist rl = (Rolelist) ite.next();
                JSONObject obj = new JSONObject();
                obj.put("roleid", rl.getRoleid());
                obj.put("rolename", rl.getRolename());
                obj.put("displayrolename", rl.getDisplayrolename());
                obj.put("desc", rl.getDescription());
                jarr.put(obj);
            }
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
       public ModelAndView deleteRole(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String roleid = request.getParameter("roleid");
            String rolename=request.getParameter("rolename");
            String msg = "";
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("roleid", roleid);
            requestParams.put("locale", RequestContextUtils.getLocale(request));

            kmsg = accCommonTablesDAO.deleteRole(requestParams);
            msg = kmsg.getMsg();

//            RoleUserMapping rolelistMapping = (RoleUserMapping) kmsg.getEntityList().get(0);
//            String role = rolelistMapping.getRoleId().getRolename();
            jobj.put("msg", msg);
            txnManager.commit(status);
            auditTrailObj.insertAuditLog(AuditAction.SETROLE_PERMISSION, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted " + rolename + " Role from Role Management", request,rolename);
        } catch (ServiceException | JSONException | TransactionException | SessionExpiredException e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }   
    public ModelAndView getRoleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        String ss = request.getParameter("ss");
        try {
            kmsg = accCommonTablesDAO.getRoleList(sessionHandlerImpl.getCompanyid(request),ss);
            jobj = getRoleJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
            JSONArray DataJArr = jobj.getJSONArray("data");
            int count = DataJArr.length();
            JSONArray pagedJson = DataJArr;

            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", count);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
    
      /*------- Function to Import User Permission Role wise------ */
     public ModelAndView importPermissionsRoleWise(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
         String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
           extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            
            boolean updateExistingRecordFlag = false;
            if(!StringUtil.isNullOrEmpty(request.getParameter("updateExistingRecordFlag"))){
                updateExistingRecordFlag = Boolean.FALSE.parseBoolean(request.getParameter("updateExistingRecordFlag"));
 }
             ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);

            extraParams.put("Life", 10.0);
            extraParams.put("Salvage", 0.0);
          
            
            
            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
           requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("companyid", companyid);
            requestParams.put("moduleid", Constants.Account_ModuleId);
            int countryId = 0;
            if (extraPref != null) {
                requestParams.put("isCurrencyCode", extraPref.isCurrencyCode());
                requestParams.put("isActivateIBG", extraPref.isActivateIBG());
                if(extraPref.getCompany() != null && extraPref.getCompany().getCountry()!=null && !StringUtil.isNullObject(extraPref.getCompany().getCountry().getID())){
                    countryId = Integer.parseInt(extraPref.getCompany().getCountry().getID());
                }
            }
            requestParams.put("countryid", countryId);
            requestParams.put("companyid",  sessionHandlerImpl.getCompanyid(request));
            if(updateExistingRecordFlag){
                requestParams.put("allowDuplcateRecord", updateExistingRecordFlag);
            }
     
            
      
            /*Code for importing Records */
            if (doAction.compareToIgnoreCase("import") == 0 || doAction.compareToIgnoreCase("xlsImport") == 0) {

                System.out.println("A(( Import start : " + new Date());

                jobj = importHandler.importPermissionsRoleWise(requestParams);

                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
                jobj.put("success", true);
            }
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}