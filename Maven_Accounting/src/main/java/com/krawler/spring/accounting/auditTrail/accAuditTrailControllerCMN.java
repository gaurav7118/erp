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
package com.krawler.spring.accounting.auditTrail;

import com.krawler.common.admin.AuditGroup;
import com.krawler.common.admin.AuditTrail;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author Karthik
 */
public class accAuditTrailControllerCMN extends MultiActionController {

    private auditTrailDAO auditTrailDAOObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private String successView;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccAuditTrailServiceCMN accAuditTrialServiceCMN;
    private static long progressCount=0;
    

    public void setAuditTrailDAO(auditTrailDAO auditTrailDAOObj1) {
        this.auditTrailDAOObj = auditTrailDAOObj1;
    }

    public void setAccAuditTrialServiceCMN(AccAuditTrailServiceCMN accAuditTrialServiceCMN) {
        this.accAuditTrialServiceCMN = accAuditTrialServiceCMN;
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
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    /**
     * Commenting getAuditJSONData() method as it is moved to service AccAuditTrailServiceCMN.java.
     * 
     */
//    public JSONObject getAuditJSONData(List ll, HttpServletRequest request, int totalSize) throws ServiceException, SessionExpiredException,ParseException {
//        JSONObject jobj = new JSONObject();
//        try {
//            Iterator itr = ll.iterator();
//            JSONArray jArr = new JSONArray();
//            String userdate="";
//            while (itr.hasNext()) {
//                AuditTrail auditTrail = (AuditTrail) itr.next();
//                JSONObject obj = new JSONObject();
//                obj.put("id", auditTrail.getID());
//                obj.put("username", auditTrail.getUser().getUserLogin().getUserName() + " [ " +  auditTrail.getUser().getFullName() + " ]");
//                obj.put("ipaddr", auditTrail.getIPAddress());
//                Date auditTime = auditTrail.getAuditTime();
//                if (auditTrail.getAuditTime() != null) {
//                    userdate = authHandler.getUTCToUserLocalDateFormatter_NEW(request, auditTime);
//                }
//                obj.put("timestamp", userdate);
//                userdate = new SimpleDateFormat(Constants.yyyyMMdd).format(auditTime);
//                if (auditTrail.getDetails().contains("has logged out")) {
//                    obj.put("details", auditTrail.getDetails());
//                    obj.put("link", "  <a href='#' onclick='callUserSummaryReport(undefined,undefined,undefined,undefined,undefined,undefined,\""+userdate+"\",\""+userdate+"\",undefined, \"" + auditTrail.getUser().getUserID() +"\")'>" + "View User Transaction Summary Report" + "</a>");
//                } else {
//                    obj.put("details", auditTrail.getDetails());
//                }
//                jArr.put(obj);
//            }
//            jobj.put("data", jArr);
//            jobj.put("count", totalSize);
//
//        } catch (JSONException e) {
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        }
//        return jobj;
//    }

    public JSONObject getAuditGroupJsonData(List ll, HttpServletRequest request, int totalSize) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            JSONObject objN = new JSONObject();
            objN.put("groupid", "");
            objN.put("groupname", "--All--");
            jArr.put(objN);
            Iterator itr = ll.iterator();
            while (itr.hasNext()) {
                AuditGroup auditGroup = (AuditGroup) itr.next();
                JSONObject obj = new JSONObject();
                if(auditGroup.getGroupName().equalsIgnoreCase("Company")){
                    continue;
                }
                obj.put("groupid", auditGroup.getID());
                obj.put("groupname", auditGroup.getGroupName());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", totalSize);

        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public ModelAndView getAuditData(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
            User user = (User) userResult.getEntityList().get(0);
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", StringUtil.checkForNull(request.getParameter("start")));
            requestParams.put("limit", StringUtil.checkForNull(request.getParameter("limit")));
            requestParams.put("groupid", StringUtil.checkForNull(request.getParameter("groupid")));
            requestParams.put("search", StringUtil.checkForNull(request.getParameter("search")));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }
            Date stDate=null,endDate=null;
            DateFormat df=authHandler.getDateOnlyFormat(request);
            try{
                if(!StringUtil.isNullOrEmpty(request.getParameter("startdate")))stDate = df.parse(request.getParameter("startdate"));
                if(!StringUtil.isNullOrEmpty(request.getParameter("enddate")))endDate = df.parse(request.getParameter("enddate"));
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            requestParams.put("stDate", stDate);
            requestParams.put("eDate", endDate);
            requestParams.put("direction", StringUtil.checkForNull(request.getParameter("dir")));
            requestParams.put("sort", StringUtil.checkForNull(request.getParameter("sort")));
            kmsg = auditTrailDAOObj.getAuditData(requestParams);
            
            /**
             * Prepared JSON to get AuditData from getAuditJSONData() method.
             */
            JSONObject paramObj=new JSONObject();
            String timeZoneDiff=sessionHandlerImpl.getTimeZoneDifference(request);
            paramObj.put(Constants.timezonedifference,timeZoneDiff);
            String userDateFormat=sessionHandlerImpl.getUserDateFormat(request);
            paramObj.put(Constants.userdateformat,userDateFormat);
            jobj=accAuditTrialServiceCMN.getAuditJSONData(kmsg.getEntityList(), paramObj, kmsg.getRecordTotalCount());
            
            /**
             * Commenting getAuditJSONData() method as it is moved to service
             * AccAuditTrailServiceCMN.java.
             * And also removed dependency for request object.
             */
//            jobj = getAuditJSONData(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAuditGroupData(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            kmsg = auditTrailDAOObj.getAuditGroupData();
            jobj = getAuditGroupJsonData(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView reloadLuceneIndex(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        
        boolean issuccess = false;
        String msg = "";
        long count = 0, start = 0, limit = 5000;
        JSONObject jobj = new JSONObject();
        try {
            if (!StringUtil.isNullOrEmpty(request.getParameter("limit"))) {
                limit = Long.parseLong(request.getParameter("limit"));
            }
            String subdomainList = request.getParameter("subdomain");
            String successMessageDetails = "";
            if (StringUtil.isNullOrEmptyWithTrim(subdomainList)) {
                successMessageDetails = process(null, start, count, limit);
            } else {
                for (StringTokenizer stringTokenizer1 = new StringTokenizer(subdomainList, ","); stringTokenizer1.hasMoreTokens();) {
                    String subdomainListObj = stringTokenizer1.nextToken();
                    successMessageDetails += process(subdomainListObj, start, count, limit);
                }
            }
            issuccess = true;
            msg = "Realoading lucene completed:= " + successMessageDetails;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null && ex.getCause() != null) {
                msg = "" + ex.getCause().getMessage();
            }
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException jsExp) {
                Logger.getLogger(accAuditTrailControllerCMN.class.getName()).log(Level.SEVERE, null, jsExp);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    private String process(String subdomainListObj, long start, long count, long limit) throws ServiceException {
        KwlReturnObject kmsg = null;
        List list = null;
        String successMessageDetails = "";
            Map<String, Object> auditMap = new HashMap<String, Object>();
        auditMap.put("subdomain", subdomainListObj);
            Map<String, Long> countMap = auditTrailDAOObj.getAuditTrailCount(auditMap);
            progressCount = 0;
//            String indexPath = storageHandlerImplObj.GetAuditTrailIndexPath();
        if (!StringUtil.isNullObject(countMap)) {
            for (Map.Entry<String, Long> countMapObject : countMap.entrySet()) {
                start = 0;
                count = countMapObject.getValue();
                String str = countMapObject.getKey();
                String subdomain = str.substring(0, str.indexOf("="));
                String companyid = str.substring(str.indexOf("=") + 1, str.length());

//                // delete folder 
//                String indexCompletePath = indexPath + File.separator + companyid;
//                FileUtils.deleteDirectory(new File(indexCompletePath));
                auditMap.put("subdomain", subdomain);
            while (count > 0 && start <= count) {
                auditMap.put("start", start);
                auditMap.put("limit", limit);
                kmsg = auditTrailDAOObj.reloadLuceneIndex(auditMap);
                list = kmsg.getEntityList();
                /*Do indexing of Audit Trail data*/
                doIndexing(list, count);
                start = start + limit;
                list = null;
                kmsg = null;
            }//End While loop
                successMessageDetails += "{" + subdomain + ":=>" + count + "}";
            }
        } else {
            successMessageDetails += "{" + subdomainListObj + ":=>" + 0 + "}";
            }
        return successMessageDetails;
            }
    
    public void doIndexing(List<AuditTrail> list,long count) throws ServiceException {
        int subCount=0;
        String msg="";
        for (AuditTrail auditTrail : list) {
            if (subCount==50) {
                progressCount += subCount;
                msg = "Indexing has been completed : " + progressCount + " out of " + count;
                Logger.getLogger(accAuditTrailControllerCMN.class.getName()).log(Level.SEVERE, msg);
                subCount = 0;
            }            
            auditTrailDAOObj.indexAuditLogEntry(auditTrail);
            subCount++;
        }
    }
}
