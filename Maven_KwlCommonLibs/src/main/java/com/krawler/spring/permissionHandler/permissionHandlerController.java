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
package com.krawler.spring.permissionHandler;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Rolelist;
import com.krawler.common.admin.User;
import com.krawler.common.admin.UserLogin;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Karthik
 */
public class permissionHandlerController extends MultiActionController implements MessageSourceAware {

    private permissionHandlerDAO permissionHandlerDAOObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private auditTrailDAO auditTrailDAOObj;
    private HibernateTransactionManager txnManager;
    private String successView;
    private MessageSource messageSource;
    private kwlCommonTablesDAO kwlCommonTablesDAOobj;
    private permissionHandlerService permissionHandlerServiceObj;
    
    public void setKwlCommonTablesDAOobj(kwlCommonTablesDAO kwlCommonTablesDAOobj){
        this.kwlCommonTablesDAOobj=kwlCommonTablesDAOobj;
    }
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setAuditTrailDAO(auditTrailDAO auditTrailDAOObj1) {
        this.auditTrailDAOObj = auditTrailDAOObj1;
    }

    public void setPermissionHandlerServiceObj(permissionHandlerService permissionHandlerServiceObj) {
        this.permissionHandlerServiceObj = permissionHandlerServiceObj;
    }
    public JSONObject getActivityJson(List ll, HttpServletRequest request, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        boolean standaloneflag= Boolean.parseBoolean(StorageHandler.getStandalone());
        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                JSONObject obj = new JSONObject();
                //We have remove the reports cash flow statement, sales by item, tex report from featurelist, also hide quality Assurance Module and Cost center Report
                //ff80808122f9dba90122fa4888cf0065=cost center Report
                //ff80808122f9dba90122fa4888cf0069=Quality Assurance
                //ff80808122f9dba90122fa4888cf0049=Sales By Item Report
                //ff80808122f9dba90122fa4888cf0064=Tax Report
                //ff80808122f9dba90122fa4888cf0063=Cash Flow Statement
                //ff80808122f9dba90122fa4888cf0066= tax 1099
                //ff80808122f9dba90122fa4888cf0058= Fixed Asset
                //ff80808122f9dba90122fa305cf30026=Manage users
                if ((row[1].equals("ff80808122f9dba90122fa305cf30026")&&!standaloneflag)||row[0].equals("ff80808122f9dba90122fa4888cf0058")||row[0].equals("ff80808122f9dba90122fa4888cf0066")||row[0].equals("ff80808122f9dba90122fa4888cf0065")||row[0].equals("ff80808122f9dba90122fa4888cf0069") || row[0].equals("ff80808122f9dba90122fa4888cf0049") || row[0].equals("ff80808122f9dba90122fa4888cf0064") || row[0].equals("ff80808122f9dba90122fa4888cf0063")) {  
                    continue;
                }
                obj.put("featureid", row[0]);
                obj.put("activityid", row[1]);
                obj.put("activityname", row[2]);
                obj.put("displayactivityname", messageSource.getMessage("acc.lp." + row[2], null, RequestContextUtils.getLocale(request)));
                obj.put("alignright", row[4] == null ? false : true);
                obj.put("parentid", row[4]);

                jarr.put(obj);
            }
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

    public ModelAndView getActivityList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            kmsg = permissionHandlerDAOObj.getActivityList();
            jobj = getActivityJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }


    /**
     * Business Logic are moved from controller to service New service Layer is
     * created.
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getRoleList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean success = false;
        String msg = "Failure";
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = permissionHandlerServiceObj.getRoles(requestJobj);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(permissionHandlerController.class.getName()).log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_success, success);
            } catch (JSONException ex) {
                Logger.getLogger(permissionHandlerController.class.getName()).log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Checks for is barcode scanning facility/flag is true/false for company.
     * @param companyid
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public boolean isBarcodeScanningForCompany(String companyid) throws ServiceException, JSONException {
        boolean barcodeScanning = false;
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("tableName", "ExtraCompanyPreferences");
        requestParams.put("fetchColumn", "columnPref");
        requestParams.put("companyColumn", "company.companyID");
        requestParams.put(Constants.companyKey, companyid);
        KwlReturnObject kwlObj = kwlCommonTablesDAOobj.populateMasterInformation(requestParams);
        if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
            String columnPref = (String) kwlObj.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(columnPref)) {
                JSONObject columnPrefJSON = new JSONObject(columnPref);
                barcodeScanning = columnPrefJSON.optBoolean("barcodeScanning");
            }
        }
        return barcodeScanning;
    }
    
    public JSONObject getFeatureJson(List ll, HttpServletRequest request, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean barcodeScanning = isBarcodeScanningForCompany(companyid);
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                JSONObject obj = new JSONObject();
                 //We have remove the reports cash flow statement, sales by item, tex report from featurelist, also hide quality Assurance Module and Cost center Report
                //ff80808122f9dba90122fa4888cf0065=cost center Report
                //ff80808122f9dba90122fa4888cf0069=Quality Assurance
                //ff80808122f9dba90122fa4888cf0049=Sales By Item Report
                //ff80808122f9dba90122fa4888cf0064=Tax Report
                //ff80808122f9dba90122fa4888cf0063=Cash Flow Statement
                //ff80808122f9dba90122fa4888cf0066= tax 1099
                //ff80808122f9dba90122fa4888cf0058= Fixed Asset
		//ff80808122f9dba90122fa4888cf0057 = Quantative Analysis        //SDP-10451
                //ff80808122f9dba90122fa4704a51234 = Barcode Scanner
                if(row[0].equals("ff80808122f9dba90122fa4888cf0057")||row[0].equals("ff80808122f9dba90122fa4888cf0058")||row[0].equals("ff80808122f9dba90122fa4888cf0066")||row[0].equals("ff80808122f9dba90122fa4888cf0065")||row[0].equals("ff80808122f9dba90122fa4888cf0069") ||row[0].equals("ff80808122f9dba90122fa4888cf0049")||row[0].equals("ff80808122f9dba90122fa4888cf0064") ||row[0].equals("ff80808122f9dba90122fa4888cf0063") || (!barcodeScanning && row[0].equals("ff80808122f9dba90122fa4704a51234"))){
                    continue;
                }
                obj.put("featureid", row[0]);
                obj.put("featurename", row[1]);
                obj.put("displayfeaturename", messageSource.getMessage("acc.up." + row[3], null, RequestContextUtils.getLocale(request)));

                jarr.put(obj);
            }
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

    public ModelAndView getFeatureList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            kmsg = permissionHandlerDAOObj.getFeatureList();
            jobj = getFeatureJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getPermissions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            String userid = sessionHandlerImplObj.getUserid(request);
            String companyid = sessionHandlerImplObj.getCompanyid(request);

            if (!permissionHandlerDAOObj.isSuperAdmin(userid, companyid)) {
                kmsg = permissionHandlerDAOObj.getActivityFeature();
                jobj = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("userid", userid);

                kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
                jobj = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), jobj);
            } else {
                jobj.put("deskeraadmin", true);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
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

    public ModelAndView getRolePermissions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            String userid = request.getParameter("userid");
            String roleid = request.getParameter("roleid");
            boolean allowedit=Boolean.parseBoolean(request.getParameter("allowedit"));
            String companyid = sessionHandlerImplObj.getCompanyid(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("allowedit",allowedit);
            requestParams.put("userid", userid);
            requestParams.put("roleid", roleid);
            requestParams.put("companyid",companyid);
            kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
            jobj = getRolePermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView setPermissions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
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
            String currentUserId = sessionHandlerImplObj.getUserid(request);
            String[] features = request.getParameterValues("features");
            String[] permissions = request.getParameterValues("permissions");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("userid", userid);
            requestParams.put("roleid", request.getParameter("roleid"));

            kmsg1 = permissionHandlerDAOObj.setPermissions(requestParams, features, permissions);
            UserLogin userLogin = (UserLogin) kmsg1.getEntityList().get(0);

//            if (userid.equals(currentUserId)) {
            kmsg = permissionHandlerDAOObj.getActivityFeature();
            jobj = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());

            requestParams = new HashMap<String, Object>();
            requestParams.put("userid", userid);

            kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
            HttpSession httpsession = request.getSession(true);
            Iterator ite2 = kmsg.getEntityList().iterator();
            while (ite2.hasNext()) {
                Object[] roww = (Object[]) ite2.next();
                httpsession.setAttribute(roww[0].toString(), roww[1]);
            }
            jobj = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), jobj);
//            }
            jobj.put("msg", messageSource.getMessage("acc.rem.172", null, RequestContextUtils.getLocale(request)));
                       
            auditTrailDAOObj.insertAuditLog(AuditAction.PERMISSIONS_MODIFIED, "User "+sessionHandlerImpl.getUserFullName(request)+" updated permissions for user " + userLogin.getUser().getFirstName() + " " + userLogin.getUser().getLastName()+kmsg1.getMsg(),request, userid);
            
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
           
    public ModelAndView saveFeatureList(HttpServletRequest request, HttpServletResponse response)
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
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("featureid", request.getParameter("featureid"));
            requestParams.put("featurename", request.getParameter("featurename"));
            requestParams.put("displayfeaturename", request.getParameter("displayfeaturename"));

            kmsg = permissionHandlerDAOObj.saveFeatureList(requestParams);
            jobj = getFeatureJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveRoleList(HttpServletRequest request, HttpServletResponse response)
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
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("roleid", request.getParameter("roleid"));
            requestParams.put("userid", request.getParameter("userid"));
            requestParams.put("rolename", request.getParameter("rolename"));
            requestParams.put("displayrolename", request.getParameter("displayrolename"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            kmsg = permissionHandlerDAOObj.saveRoleList(requestParams);
            jobj = getFeatureJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
            jobj.put("msg", "Role saved successfully");
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveActivityList(HttpServletRequest request, HttpServletResponse response)
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
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("activityid", request.getParameter("activityid"));
            requestParams.put("activityname", request.getParameter("activityname"));
            requestParams.put("displayactivityname", request.getParameter("displayactivityname"));

            kmsg = permissionHandlerDAOObj.saveActivityList(requestParams);
            jobj = getFeatureJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteFeature(HttpServletRequest request, HttpServletResponse response)
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
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("featureid", request.getParameter("featureid"));

            kmsg = permissionHandlerDAOObj.deleteFeature(requestParams);
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
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
            String msg = "";
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("roleid", roleid);

            kmsg = permissionHandlerDAOObj.deleteRole(requestParams);
            msg = kmsg.getEntityList().get(0).toString();
            jobj.put("msg", msg);
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteActivity(HttpServletRequest request, HttpServletResponse response)
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
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("activityid", request.getParameter("activityid"));

            kmsg = permissionHandlerDAOObj.deleteActivity(requestParams);
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    }
