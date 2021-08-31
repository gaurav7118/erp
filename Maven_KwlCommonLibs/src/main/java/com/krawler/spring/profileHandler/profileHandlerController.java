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
package com.krawler.spring.profileHandler;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.Rolelist;
import com.krawler.common.admin.User;
import com.krawler.common.admin.UserLogin;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.*;
import com.krawler.esp.web.resource.Links;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author Karthik
 */
public class profileHandlerController extends MultiActionController {

    private profileHandlerDAO profileHandlerDAOObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private storageHandlerImpl storageHandlerImplObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private String successView;
    private HibernateTransactionManager txnManager;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private auditTrailDAO auditTrailObj;
    private profileHandlerService profileHandlerServiceObj;

    public void setProfileHandlerServiceObj(profileHandlerService profileHandlerServiceObj) {
        this.profileHandlerServiceObj = profileHandlerServiceObj;
    }
    
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj1) {
        this.profileHandlerDAOObj = profileHandlerDAOObj1;
    }

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public void setStorageHandlerImplObj(storageHandlerImpl storageHandlerImplObj) {
        this.storageHandlerImplObj = storageHandlerImplObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    public ModelAndView getAllUserDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.remove(Constants.lid);
            String lid = StringUtil.checkForNull(request.getParameter("lid"));
            paramJobj.put(Constants.lid, lid);
            jobj = profileHandlerServiceObj.getAllUserDetails(paramJobj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getUserDetailsFromApps(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = profileHandlerServiceObj.getUserDetailsFromApps(paramJobj);
        } catch (Exception e) {
            Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public JSONObject getUserDetailsJson(List ll, HttpServletRequest request, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            Iterator ite = ll.iterator();
            boolean isApprover=false;
            String timeFormatId = sessionHandlerImplObj.getUserTimeFormat(request);
            String timeZoneDiff = sessionHandlerImplObj.getTimeZoneDifference(request);
            while (ite.hasNext()) {
                User user = (User) ite.next();
                UserLogin ul = user.getUserLogin();
                JSONObject obj = new JSONObject();
                obj.put("userid", user.getUserID());
                obj.put("username", ul.getUserName());
                obj.put("fname", user.getFirstName());
                obj.put("lname", user.getLastName());
                obj.put("fullname", user.getFirstName()+" "+user.getLastName());
                obj.put("image", user.getImage());
                obj.put("emailid", user.getEmailID());
                obj.put("lastlogin", (ul.getLastActivityDate() == null ? "" : authHandler.getUserDateFormatterWithoutTimeZone(request).format(ul.getLastActivityDate())));
                obj.put("aboutuser", user.getAboutUser());
                obj.put("address", user.getAddress());
                obj.put("contactno", user.getContactNumber());
                obj.put("formatid", (user.getDateFormat() == null ? "" : user.getDateFormat().getFormatID()));
                obj.put("tzid", (user.getTimeZone() == null ? Constants.NEWYORK_TIMEZONE_ID : user.getTimeZone().getTimeZoneID())); // 23 is id of New York Time Zone. [default]
                obj.put("callwithid", user.getCallwith());
                obj.put("timeformat", (user.getTimeformat() != 1 && user.getTimeformat() != 2) ? 2 : user.getTimeformat()); // 2 is id for '24 hour timeformat'. [default]
                obj.put("fullname", user.getFirstName() + " " + user.getLastName());
                obj.put("department",user.getDepartment() == null ? "":user.getDepartment());
                kmsg = permissionHandlerDAOObj.getRoleofUser(user.getUserID());
                Iterator ite2 = kmsg.getEntityList().iterator();
                while (ite2.hasNext()) {
                    Object[] row = (Object[]) ite2.next();
                    obj.put("roleid", row[0]);
                    obj.put("rolename", row[1]);
                }
                isApprover = profileHandlerDAOObj.checkIsUserApprover(user.getUserID());
                obj.put("isApprover", isApprover);
                jarr.put(obj);
            }
            
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

    public ModelAndView getAllManagers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImplObj.getCompanyid(request));

            kmsg = profileHandlerDAOObj.getAllManagers(requestParams);
            jobj = getUserDetailsJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveDateFormat(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String dateid = request.getParameter("newformat");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("userid", sessionHandlerImplObj.getUserid(request));
            requestParams.put("dateformat", StringUtil.checkForNull(dateid));

            profileHandlerDAOObj.saveUser(requestParams);
            sessionHandlerImpl.updateDatePreferences(request, dateid);
            jobj.put("success", true);
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String[] ids = request.getParameterValues("userids");
            for (int i = 0; i < ids.length; i++) {
                profileHandlerDAOObj.deleteUser(ids[i]);
            }
            jobj.put("msg", "User deleted successfully");
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getUserofCompany(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            String companyid = sessionHandlerImplObj.getCompanyid(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);

            ArrayList filter_names = new ArrayList();
            ArrayList filter_params = new ArrayList();
            filter_names.add("u.company.companyID");
            filter_params.add(companyid);
            filter_names.add("u.deleteflag");
            filter_params.add(0);

            kmsg = profileHandlerDAOObj.getUserDetails(requestParams, filter_names, filter_params);
            jobj = getUserDetailsJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView changePassword(HttpServletRequest request, HttpServletResponse response)
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
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("currentpassword", StringUtil.checkForNull(request.getParameter("currentpassword")));
            requestParams.put("userid", sessionHandlerImplObj.getUserid(request));
            requestParams.put("remoteapikey", storageHandlerImplObj.GetRemoteAPIKey());

            kmsg = profileHandlerDAOObj.changePassword(requestParams);
            jobj = (JSONObject) kmsg.getEntityList().get(0);
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView standAloneSaveUser(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        JSONObject jobj = new JSONObject();
        JSONObject jres = new JSONObject();
        User user=null;
        HashMap<String, Object> standAlonehm = new HashMap<String, Object>();
        boolean isEmailexist = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            boolean isStandAlone = Boolean.parseBoolean(StorageHandler.getStandalone());
            if (isStandAlone) {
//                String platformURL = ConfigReader.getinstance().get("crmURL");
                HashMap hm = null;
                if (ServletFileUpload.isMultipartContent(request)) {
                    hm = new FileUploadHandler().getItems(request);
                }
                if (hm == null) {
                    throw new Exception("Form does not support file upload");
                }
                String id = (String) hm.get("userid");
                boolean createAction = false;
                if (StringUtil.isNullOrEmpty(id)) {
                    createAction = true;
                    id = java.util.UUID.randomUUID().toString();
                }
                String pwdText = StringUtil.generateNewPassword();
                String pwd = StringUtil.getSHA1(pwdText);
                standAlonehm.put("companyid", companyId);
                standAlonehm.put("appid", "3");// ERP Application ID
                standAlonehm.put("username", (String) hm.get("username"));
                standAlonehm.put("firstName", (String) hm.get("fname"));
                standAlonehm.put("lastName", (String) hm.get("lname"));
                standAlonehm.put("emailID", (String) hm.get("emailid"));
                standAlonehm.put("contactNumber", (String) hm.get("contactno"));
                standAlonehm.put("contactno", (String) hm.get("contactno"));
                standAlonehm.put("address", (String) hm.get("address"));
                standAlonehm.put("role", (String) hm.get("roleid"));
                standAlonehm.put("iscommit", true);
                standAlonehm.put("password", pwd);
                standAlonehm.put("timeZone", (String) hm.get("tzid"));
                standAlonehm.put("dateFormat", (String) hm.get("formatid"));
                standAlonehm.put("company", companyId);
                standAlonehm.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
                if (createAction) {
                    boolean isexist = profileHandlerDAOObj.isUserExist((String) hm.get("username"), companyId);
                    if (!isexist) {
                        isEmailexist = profileHandlerDAOObj.checkDuplicateEmailID(hm.get("emailid").toString(), companyId, id);
                        if (!isEmailexist) {
                            //Save user information
                            KwlReturnObject saveuser = profileHandlerDAOObj.saveUser(standAlonehm); 
                            user = (User) saveuser.getEntityList().get(0);

                            //Save user login information
                            HashMap<String, Object> userLoginHashmap = new HashMap<String, Object>();
                            userLoginHashmap.put("userName", hm.get("username"));
                            userLoginHashmap.put("password", pwd);
                            userLoginHashmap.put("user", user);
                            userLoginHashmap.put("userid", user.getUserID());
                            userLoginHashmap.put("saveStandAloneUserLogin", true);
                            String logdate = authHandler.getDateFormatter(request).format(new Date());
                            Date loginDate = authHandler.getGlobalDateFormat().parse(logdate);
                            userLoginHashmap.put("lastlogindate", loginDate);
                            profileHandlerDAOObj.saveUserLogin(userLoginHashmap);

                            //Save user-role mapping
                            HashMap<String, Object> roleusermap = new HashMap<String, Object>();
                            roleusermap.put("user", user);
                            roleusermap.put("roleid", (String) hm.get("roleid"));
                            profileHandlerDAOObj.saveRoleUserMapping(roleusermap);
                            
                            Company companyObj = (Company) kwlCommonTablesDAOObj.getObject(Company.class.getName(), companyId).getEntityList().get(0);
                            User creater = (User) (companyObj.getCreator());
                            String fullnameCreator = StringUtil.getFullName(creater);
                            String uri = URLUtil.getPageURL(request, Links.loginpageFull);
                            String passwordString = "\n\nUsername: " + hm.get("username") + " \nPassword: " + pwdText;
                            String msgMailInvite = "Hi %s,\n\n%s has created an account for you at Deskera Accounting.\n\nDeskera Accounting is an Account Management Tool which you'll love using." + passwordString + "\n\nYou can log in at:\n%s\n\n\nSee you on Deskera Accounting\n\n - %s and The Deskera Acconting Team";
                            String pmsg = String.format(msgMailInvite, user.getFirstName(), fullnameCreator, uri, fullnameCreator);

                            passwordString = "		  <p>Username: <strong>%s</strong> </p>"
                                    + "               <p>Password: <strong>%s</strong></p>";

                            String msgMailInviteUsernamePassword = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
                                    + "a:link, a:visited, a:active {\n"
                                    + " 	color: #03C;"
                                    + "}\n"
                                    + "body {\n"
                                    + "	font-family: Arial, Helvetica, sans-serif;"
                                    + "	color: #000;"
                                    + "	font-size: 13px;"
                                    + "}\n"
                                    + "</style><body>"
                                    + "	<div>"
                                    + "		<p>Hi <strong>%s</strong>,</p>"
                                    + "		<p>%s has created an account for you at %s.</p>"
                                    + "             <p>Deskera Accounting is an Account Management Tool which you'll love using.</p>"
                                    + passwordString
                                    + "		<p>You can log in to Deskera Accounting at: <a href=%s>%s</a>.</p>"
                                    + "		<br/><p>See you on Deskera Accounting!</p><p> - %s and The Deskera Accounting Team</p>"
                                    + "         <br/><p>This is an auto generated email. Do not reply</p>"
                                    + "	</div></body></html>";
                            String htmlmsg = String.format(msgMailInviteUsernamePassword, user.getFirstName(), fullnameCreator, companyObj.getCompanyName(), hm.get("username"), pwdText, uri, uri, fullnameCreator);
                            try{
                                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(companyObj);
                                SendMailHandler.postMail(new String[]{user.getEmailID()}, "[Deskera] Welcome to Deskera Accounting", htmlmsg, pmsg, creater.getEmailID(), smtpConfigMap);
                            } catch(Exception e){
                                Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, e);
                            }


                            jres.put("valid", true);
                            jres.put("success", true);
                            jres.put("duplicateEmail", isEmailexist);
                        } else {
                            jres.put("duplicateEmail", isEmailexist);//messageSource.getMessage("",null, RequestContextUtils.getLocale(request));
                            jres.put("valid", false);
                            jres.put("success", false);
                            throw new Exception("User with same Email id already exists !");
                            
                        }
                    } else {
                        jres.put("valid", false);
                        jres.put("success", false);
                        throw new Exception("User with same Username already exists !");
                    }
                } 
                if (!StringUtil.isNullObject(hm.get("userimage"))) {
                    String imageName = ((FileItem) hm.get("userimage")).getName();
                    if (imageName != null && imageName.length() > 0) {
                        try {
                            String fileName = id + FileUploadHandler.getImageExt();
                            user = (User) kwlCommonTablesDAOObj.getObject(User.class.getName(), id).getEntityList().get(0);
                            user.setImage(com.krawler.common.util.Constants.ImgBasePath + fileName);
                            new FileUploadHandler().uploadImage((FileItem) hm.get("userimage"), fileName, storageHandlerImpl.GetProfileImgStorePath(), 100, 100, false,
                                    false);
                        } catch (Exception e) {
                            Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }
                }

            }
            if (!user.getRoleID().equals(Rolelist.COMPANY_ADMIN)) {
                
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("userid", user.getUserID());
                requestParams.put("roleid", user.getRoleID());
                requestParams.put("companyid",companyId);
                KwlReturnObject kmsg = permissionHandlerDAOObj.getRolePermission(requestParams);
                ArrayList featurelist = new ArrayList();
                ArrayList permissionslist = new ArrayList();
                String[] features = new String[kmsg.getEntityList().size()];
                String[] permissions = new String[kmsg.getEntityList().size()];
                Iterator ite = kmsg.getEntityList().iterator();
                int featurecount = 0;
                while (ite.hasNext()) {
                    Object[] row = (Object[]) ite.next();
                    if (!featurelist.contains(row[2])) {
                        featurelist.add(row[2]);
                        //permissionslist.add(row[1]);
                        features[featurecount]= row[2].toString();
                        permissions[featurecount] = row[1].toString();
                        featurecount++;

                    }
                }

                KwlReturnObject kmsg1 = permissionHandlerDAOObj.setPermissions(requestParams, features, permissions);
            }
            auditTrailObj.insertAuditLog(AuditAction.USER_CREATED, "User " + sessionHandlerImpl.getUserFullName(request)+" Created new user "+user.getFullName(), request,companyId );
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            try {
                jres.put("msg", ex.getMessage());
            } catch (JSONException ex1) {
                Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new ModelAndView("jsonView", "model", jres.toString());
    }

    public ModelAndView standAloneEditUser(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();

        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean iscreator = false;
        User user=null;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            HashMap newhm = new FileUploadHandler().getItems(request);
            HashMap<String, String> hm = new HashMap<String, String>();
            for (Object key : newhm.keySet()) {
                hm.put(key.toString(), new String(newhm.get(key.toString()).toString().getBytes("iso-8859-1"), "UTF-8"));
            }

            String userid = null;
            if (!StringUtil.isNullOrEmpty(hm.get("userid"))) {
                userid = hm.get("userid");

            }
            String companyid = sessionHandlerImplObj.getCompanyid(request);
            Company company = (Company) kwlCommonTablesDAOObj.getObject(Company.class.getName(), companyid).getEntityList().get(0);
             if (userid.equals(company.getCreator().getUserID())) {
                iscreator = true;
            }
            if(!iscreator){                                           // if user to be edited is not company creator
            HashMap<String, Object> editUserHashMap = new HashMap<String, Object>();
            editUserHashMap.put("companyid", companyid);
            editUserHashMap.put("appid", "3");// ERP Application ID
            editUserHashMap.put("userid", userid);
            editUserHashMap.put("username", (String) hm.get("username"));
            editUserHashMap.put("firstName", (String) hm.get("fname"));
            editUserHashMap.put("lastName", (String) hm.get("lname"));
            editUserHashMap.put("emailID", (String) hm.get("emailid"));
            editUserHashMap.put("contactNumber", (String) hm.get("contactno"));
            editUserHashMap.put("contactno", (String) hm.get("contactno"));
            editUserHashMap.put("address", (String) hm.get("address"));
            editUserHashMap.put("role", (String) hm.get("roleid"));
            editUserHashMap.put("timeZone", (String) hm.get("tzid"));
            editUserHashMap.put("dateFormat", (String) hm.get("formatid"));

            KwlReturnObject saveuser = profileHandlerDAOObj.saveUser(editUserHashMap);
            user = (User) saveuser.getEntityList().get(0);
            HashMap<String, Object> roleusermap = new HashMap<String, Object>();
            roleusermap.put("userid", userid);
            roleusermap.put("roleid", (String) hm.get("roleid"));
            profileHandlerDAOObj.saveRoleUserMapping(roleusermap);
            jobj.put("success", true);
            jobj.put("valid", true);
           
            } else {                                                    // Company creator will not be allowed to be edited
                jobj.put("success", false);
                jobj.put("valid", false);
                throw new Exception("Company creator cannot be edited");
            }
            
            auditTrailObj.insertAuditLog(AuditAction.USER_MODIFIED, "User " + sessionHandlerImpl.getUserFullName(request)+" Modified user "+user.getFullName(), request,companyId );
            txnManager.commit(status);
        } catch (Exception e) {
            txnManager.rollback(status);
            e.printStackTrace();
            try {
                jobj.put("msg", e.getMessage());
            } catch (JSONException ex) {
                Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView standAloneDeleteUser(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        boolean success = false;
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        String msg = "";
        boolean isStandAlone = Boolean.parseBoolean(StorageHandler.getStandalone());
        try {
            if (isStandAlone) {
                String companyid = sessionHandlerImplObj.getCompanyid(request);
                User creator = permissionHandlerDAOObj.getCreator(companyid);
                User userToDelete = null;
                String platformURL = StorageHandler.getAccURL();

                String[] userids = request.getParameterValues("userids");

                jobj.put("iscommit", true);
                jobj.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
                jobj.put("companyid", companyid);

                jobj.put("subdomain", URLUtil.getDomainName(request));
                jobj.put("appid", "3");

                for (String userid : userids) {
                    userToDelete = (User) kwlCommonTablesDAOObj.getObject(User.class.getName(), userid).getEntityList().get(0);
                    if (userid.equals(sessionHandlerImpl.getUserid(request))) {
                        throw new Exception("Currently logged in user cannot be deleted !");
                    }
                    if (userid.equals(creator.getUserID())) {
                        throw new Exception("Company Creator cannot be deleted !");
                    }

                    profileHandlerDAOObj.deleteUser(userid);
                }
                success = true;
                msg = "User Deleted Successfully.";
                auditTrailObj.insertAuditLog(AuditAction.USER_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " Deleted user " + userToDelete.getFullName(), request, companyid);
//            if (!success) {
//                for (String userid : userids) {
//                    jobj.put("userid", userid);
//                    JSONObject appdata = null;
//                    appdata = APICallHandler.callApp(HibernateUtil.getCurrentSession(), platformURL, jobj, companyid, "4");
//                }
//            }
            }
            txnManager.commit(status);
        } catch (Exception e) {
            msg = e.getMessage();
            e.printStackTrace();
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put("valid", success);
                jobj.put("success", success);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView standAloneResetUserPassword(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        boolean success = false;
        String msg="";
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
            
            if(StringUtil.isNullOrEmpty(request.getParameter("userId")) || StringUtil.isNullOrEmpty("newPassword")){
                throw new Exception("Userid and password cannot be empty");
            }
            String userId= request.getParameter("userId");
            String newPassword=request.getParameter("newPassword");
            String newPasswordText=newPassword;
            String companyId= sessionHandlerImplObj.getCompanyid(request);
            Company companyObj = (Company) kwlCommonTablesDAOObj.getObject(Company.class.getName(), companyId).getEntityList().get(0);
            User creater = (User) (companyObj.getCreator());
            
            User loggedinUser = (User) kwlCommonTablesDAOObj.getObject(User.class.getName(), sessionHandlerImplObj.getUserid(request)).getEntityList().get(0);
            
            String fullnameCreator = StringUtil.getFullName(creater);
            String fullNameOfLoggedInUser= StringUtil.getFullName(loggedinUser);
            newPassword= StringUtil.getSHA1(newPassword);
            
            KwlReturnObject result= profileHandlerDAOObj.resetUserPassword(userId,newPassword);
            success=true;
            msg="Password has been updated successfully.";
            
            User user=(User)kwlCommonTablesDAOObj.getObject(User.class.getName(), userId).getEntityList().get(0);
            String userFirstName= user.getFirstName();
            String userMailId= user.getEmailID();
            String url = URLUtil.getPageURL(request, Links.loginpageFull);
            String msgMailInvite = "Hi %s,\n\n%s has reset your password for \n %s \n\nYour new password is " + newPasswordText + "\n\n\nSee you on Deskera Accounting\n\n - %s and The Deskera Acconting Team";
            String plainmsg = String.format(msgMailInvite, userFirstName, fullNameOfLoggedInUser,url,fullnameCreator);
            
            String msgMailInviteUsernamePassword = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
                                    + "a:link, a:visited, a:active {\n"
                                    + " 	color: #03C;"
                                    + "}\n"
                                    + "body {\n"
                                    + "	font-family: Arial, Helvetica, sans-serif;"
                                    + "	color: #000;"
                                    + "	font-size: 13px;"
                                    + "}\n"
                                    + "</style><body>"
                                    + "	<div>"
                                    + "		<p>Hi <strong>%s</strong>,</p>"
                                    + "		<p>%s has reset your password for </p>"
                                    + "         <p>%s</p>    "
                                    + "          <p> Your new password is "+"<b>"+newPasswordText+"</b>"
                                    + "		<br/><p>See you on Deskera Accounting!</p><p> - %s and The Deskera Accounting Team</p>"
                                    + "         <br/><p>This is an auto generated email. Do not reply</p>"
                                    + "	</div></body></html>";
            String htmlmsg = String.format(msgMailInviteUsernamePassword, userFirstName, fullNameOfLoggedInUser, url, fullnameCreator);
            
            System.out.println(htmlmsg);
            System.out.println(plainmsg);
            try {
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(companyObj);
                SendMailHandler.postMail(new String[]{userMailId}, "[Deskera] New Password", htmlmsg, plainmsg, creater.getEmailID(), smtpConfigMap);
            } catch (Exception e) {
                Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, e);
            }
        txnManager.commit(status);    
        } catch(Exception e){
            txnManager.rollback(status);
            success=false;
            msg=e.getMessage();
            Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, e);
        } finally{
            try {
                jobj.put("msg", msg);
                jobj.put("success", success);
            } catch (JSONException ex) {
                Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView setUserDepartment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
            User user=null;
         DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String users = request.getParameter("users");
            String[] usersArr = users.split(",");
            String department = request.getParameter("department") ;
            boolean isAllUser = false;

            for (int userCnt = 0; userCnt < usersArr.length; userCnt++) {
                String userID = usersArr[userCnt];
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("userID", userID);                      
                        requestParams.put("companyID", companyid);
                        requestParams.put("department", department);                   
                        KwlReturnObject savedepartment = profileHandlerDAOObj.setUserDepartment(requestParams);                        
                        user = (User) savedepartment.getEntityList().get(0);
                        auditTrailObj.insertAuditLog(AuditAction.USER_MODIFIED, "User " + sessionHandlerImpl.getUserFullName(request)+" changed department of user "+user.getFullName(), request,companyid );
                    }
                

        
            issuccess = true;
            txnManager.commit(status);             
          
        } catch(Exception ex){
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * To get password policy against the company.
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getPasswordPolicy(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = profileHandlerServiceObj.getPasswordPolicy(requestJobj);
            msg = jobj.optString(Constants.RES_msg);
            issuccess = jobj.optBoolean(Constants.RES_success);
        } catch (Exception ex) {
            msg = " "+ ex.getMessage();
            issuccess = false;
            Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch(JSONException ex) {
                Logger.getLogger(profileHandlerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
}
