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
package com.krawler.spring.auditTrailModule;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.AuditTrail;
import com.krawler.common.admin.AuditTrailDetails;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.Search.SearchBean;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.lowagie.text.Document;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class auditTrailDAOImpl extends BaseDAO implements auditTrailDAO {

    private storageHandlerImpl storageHandlerImplObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private AuditIndex auditIndex;

    public void setstorageHandlerImpl(storageHandlerImpl storageHandlerImplObj1) {
        this.storageHandlerImplObj = storageHandlerImplObj1;
    }

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public void setAuditIndex(AuditIndex auditIndex) {
        this.auditIndex = auditIndex;
    }
    

    public void insertAuditLog(String actionid, String details, HttpServletRequest request, String recid, String extraid) throws ServiceException {
        try {
            AuditAction action = (AuditAction) load(AuditAction.class, actionid);
            insertAuditLog(action, details, request, recid, extraid);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    /**
     * @deprecated use {@link #insertAuditLog(String actionid, String details, Map<String, Object> requestParams, String recid)}
     */
    @Deprecated
    public void insertAuditLog(String actionid, String details, HttpServletRequest request, String recid) throws ServiceException {
        try {
            AuditAction action = (AuditAction) load(AuditAction.class, actionid);
            insertAuditLog(action, details, request, recid, "0");
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }    
    //This method used to make entry ento Audit Trail for Recurring documents.
    public void insertRecurringAuditLog(String actionid, String details, HttpServletRequest request, String recid, User user) throws ServiceException {
        try {
            AuditAction action = (AuditAction) load(AuditAction.class, actionid);
            insertRecurringAuditLog(action, details, request, recid, "0", user);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }
    
    //This method used to get Remote Machine IP Address.
    public void insertRecurringAuditLog(AuditAction action, String details, HttpServletRequest request, String recid, String extraid, User user) throws ServiceException {
        String ipAddress = "";
        try {
//            InetAddress ipAddr = InetAddress.getLocalHost();
//            serverip = ipAddr.getHostAddress();  //Get Server IP Address in case of Recurring
            if (StringUtil.isNullOrEmpty(request.getHeader("x-real-ip"))) {
                ipAddress = request.getRemoteAddr();
            } else {
                ipAddress = request.getHeader("x-real-ip");
            }
            insertAuditLog(action, details, ipAddress, user, recid, extraid, "");
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
//        catch (UnknownHostException ex) {
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
//        } 
    }
    
    public void insertAuditLog(String actionid, String details, Map<String, Object> requestParams, String recid) throws ServiceException {
        try {
            AuditAction action = (AuditAction) load(AuditAction.class, actionid);
            insertAuditLog(action, details, requestParams, recid, "0");
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public void insertAuditLog(String actionid, String details, String ipAddress, String userid, String recid) throws ServiceException {
        try {
            AuditAction action = (AuditAction) load(AuditAction.class, actionid);
            User user = (User) load(User.class, userid);
            insertAuditLog(action, details, ipAddress, user, recid, "0",null); //ERP-17605
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public void insertAuditLog(AuditAction action, String details, HttpServletRequest request, String recid, String extraid) throws ServiceException {
        try {
            User user = (User) load(User.class, sessionHandlerImplObj.getUserid(request));
            String ipaddr = null;
            String prdjsondtls = null;//ERP-17605
            if (StringUtil.isNullOrEmpty(request.getHeader("x-real-ip"))) {
                ipaddr = request.getRemoteAddr();
            } else {
                ipaddr = request.getHeader("x-real-ip");
            }
            prdjsondtls=request.getParameter("detail");//ERP-17605            
            
            //ERP-25134 : Audit Trail Descriptive information will be saved only if changes happen in Company Settings.
            if(action.getID().equals(AuditAction.COMPANY_ACCOUNT_PREFERENCES_UPDATE)){ 
                String companyprefdetails = request.getAttribute("companyprefdetails")!=null ? (String)request.getAttribute("companyprefdetails") : "";            
                insertAuditLogWithAuditDetails(action, details, ipaddr, user, recid, extraid,prdjsondtls, companyprefdetails);                
            } else {
                insertAuditLog(action, details, ipaddr, user, recid, extraid,prdjsondtls);//ERP-17605                    
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }    
        
    /*
        ERP-25134 - Below method has been used to save Audit Trail entry along with descriptive information about user action.
        As of now, AuditTrailDetails Table save changes done in Company Settings. For every audit trail record there will be one unique entry in audit trail along with details for Company Setting changes.
    */
    public void insertAuditLogWithAuditDetails(AuditAction action, String details, String ipAddress, User user, String recid, String extraid,String prdjsondtls, String companyprefdetails) throws ServiceException { //ERP-17605
        try {
            String aid = UUID.randomUUID().toString();
            
            SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date newdate=new Date();
            sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date newcreatedate=authHandler.getDateWithTimeFormat().parse(sdf1.format(newdate));
            
            //Save Audit Trail Entry here.
            AuditTrail auditTrail = new AuditTrail();
            auditTrail.setID(aid);
            auditTrail.setAction(action);
            auditTrail.setAuditTime(newcreatedate);
            auditTrail.setDetails(details);
            auditTrail.setIPAddress(ipAddress);
            auditTrail.setRecid(recid);
            auditTrail.setPrdjsondtls(prdjsondtls); //ERP-17605
            auditTrail.setUser(user);
            auditTrail.setExtraid(extraid);
            save(auditTrail);
            
            
            //ERP-25134 - Save Audit Trail Detail information here.
            AuditTrailDetails auditTrailDetails = new AuditTrailDetails();
            auditTrailDetails.setAuditDetails(companyprefdetails);
            auditTrailDetails.setAuditTime(newcreatedate);
            auditTrailDetails.setAction(action);
            auditTrailDetails.setAudittrail(auditTrail);
            save(auditTrailDetails);
            
            indexAuditLogEntry(auditTrail);
            
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }
    
    public void insertAuditLog(AuditAction action, String details, Map<String, Object> requestParams, String recid, String extraid) throws ServiceException {
        try {
            String reqHeader=(requestParams.get("reqHeader")!=null)?(String)requestParams.get("reqHeader"):"";
            String userID=(requestParams.get("userid")!=null)?(String)requestParams.get("userid"):"";
            String prdjsondtls = requestParams.containsKey("prdjsondtls") ? (String) requestParams.get("prdjsondtls") : null;//Deprication of method with HttpServleRrequest
            
            User user = (User) get(User.class, userID);
            String ipaddr = null;
            if (StringUtil.isNullOrEmpty(reqHeader)) {
                ipaddr = (String)requestParams.get("remoteAddress");
            } else {
                ipaddr = (String)requestParams.get("reqHeader");
            }

            //ERP-25134 : Audit Trail Description information will be saved only if changes happen in Company Settings.
            if(action.getID().equals(AuditAction.COMPANY_ACCOUNT_PREFERENCES_UPDATE)){ 
                String companyprefdetails = (requestParams.containsKey("companyprefdetails") && !StringUtil.isNullOrEmpty((String)requestParams.get("companyprefdetails"))) ? (String)requestParams.get("companyprefdetails") : "";
                insertAuditLogWithAuditDetails(action, details, ipaddr, user, recid, extraid,prdjsondtls, companyprefdetails);                
            } else {
                insertAuditLog(action, details, ipaddr, user, recid, extraid,prdjsondtls);//ERP-17605                    
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public void insertAuditLog(AuditAction action, String details, String ipAddress, User user, String recid, String extraid,String prdjsondtls) throws ServiceException { //ERP-17605
        try {
            String aid = UUID.randomUUID().toString();
            
            SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date newdate=new Date();
            sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date newcreatedate=authHandler.getDateWithTimeFormat().parse(sdf1.format(newdate));
            
            AuditTrail auditTrail = new AuditTrail();
            auditTrail.setID(aid);
            auditTrail.setAction(action);
            auditTrail.setAuditTime(newcreatedate);
            auditTrail.setDetails(details);
            auditTrail.setIPAddress(ipAddress);
            auditTrail.setRecid(recid);
            auditTrail.setPrdjsondtls(prdjsondtls); //ERP-17605
            auditTrail.setUser(user);
            auditTrail.setExtraid(extraid);
            save(auditTrail);
            
            indexAuditLogEntry(auditTrail);
            
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }
    public void indexAuditLogEntry(AuditTrail auditTrail) {
        try {
            
            List<Object> indexFieldDetails = new ArrayList<Object>();
            List<String> indexFieldName = new ArrayList<String>();
            indexFieldDetails.add(auditTrail.getDetails());
            indexFieldName.add("details");
            indexFieldDetails.add(auditTrail.getID());
            indexFieldName.add("transactionid");
            indexFieldDetails.add(auditTrail.getAction().getID());
            indexFieldName.add("actionid");
            indexFieldDetails.add(auditTrail.getIPAddress());
            indexFieldName.add("ipaddr");
            User user = auditTrail.getUser();
            String userName = user.getUserLogin().getUserName() + " " + user.getFirstName() + " " + user.getLastName();
            indexFieldDetails.add(userName);
            indexFieldName.add("username");
            indexFieldDetails.add(auditTrail.getAuditTime());
            indexFieldName.add("timestamp");
            indexFieldDetails.add(auditTrail.getUser().getCompany().getCompanyID());
            indexFieldName.add("companyid");
            
            String indexPath = storageHandlerImplObj.GetAuditTrailIndexPath();
            
            auditIndex.addAuditIndexData(indexFieldName, indexFieldDetails, indexPath);
            if(!auditIndex.isWorking()){
                Thread auditIndexWriteThread = new Thread(auditIndex);
                auditIndexWriteThread.start();
            }
        } catch (Exception ex) {
            logger.warn("indexAuditLogEntry: " + ex.getMessage(), ex);
        }
    }
    
    @Override
    public KwlReturnObject reloadLuceneIndex(Map<String,Object> auditMap) throws ServiceException {
        List ll = null;
        int dl = 0,start=0,limit=0;
        String subdomain="";
        String query="";
        try {
            
            if(auditMap.containsKey("subdomain") && auditMap.get("subdomain")!=null){
                subdomain=(String)auditMap.get("subdomain");
            }
            
            if (auditMap.containsKey("start") && auditMap.containsKey("limit") && !StringUtil.isNullOrEmpty(auditMap.get("start").toString())) {
                start = Integer.parseInt(auditMap.get("start").toString());
                limit = Integer.parseInt(auditMap.get("limit").toString());
            }
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                 query = "select at from AuditTrail at INNER JOIN at.user users INNER JOIN users.company uc where uc.subDomain=? order by at.auditTime";
                ll = executeQueryPaging(query,new Object[]{subdomain},new Integer[]{start,limit});
                dl = ll.size();
            }else{
                 query = "from AuditTrail order by auditTime ";
                 ll = executeQueryPaging(query,new Integer[]{start,limit});
                 dl = ll.size();
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }
    
    @Override
    public Map<String, Long> getAuditTrailCount(Map<String, Object> auditMap) throws ServiceException {
        List ll = new ArrayList();
        long count = 0;
        String subdomain = "";
        String query = "", where = "";
        Map<String, Long> responseMap = null;
        try {
            if (auditMap.containsKey("subdomain") && auditMap.get("subdomain") != null) {
                subdomain = (String) auditMap.get("subdomain");
                ll.add(subdomain);
                where = " where uc.subDomain=? ";
            }
                query = "select uc.subDomain, uc.companyID, count(at.ID)  from AuditTrail at INNER JOIN at.user users INNER JOIN users.company uc "+where+" group by uc.companyID order by at.auditTime";
                if(ll.size() > 0 ){
                    ll = executeQuery(query, ll.toArray());
            } else {
                ll = executeQuery(query);
                }
                if (ll != null && !ll.isEmpty()) {
                    responseMap = new HashMap<String, Long>();
//                    count = (Long) ll.get(0);
                    for (int i = 0; i < ll.size(); i++) {
                        Object[] arr = (Object[])ll.get(i);
                        String str = (String)arr[0] + "=" +(String)arr[1];
                        responseMap.put(str, (Long)arr[2]);
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return responseMap;
    }

    public KwlReturnObject getRecentActivityDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        String recid = "";
        String companyid = "";
        try {
            if (requestParams.containsKey("recid") && requestParams.get("recid") != null) {
                recid = requestParams.get("recid").toString();
            }
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            String query = "from AuditTrail where user.company.companyID=? and recid=? order by auditTime desc";
            ll = executeQueryPaging( query, new Object[]{companyid, recid}, new Integer[]{0, 15});
            dl = ll.size();

        } catch (Exception e) {
            throw ServiceException.FAILURE("detailPanelDAOImpl.getRecentActivityDetails : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getAuditData(HashMap<String, Object> requestParams) throws ServiceException {
        int start = 0;
        int limit = 30;
        String groupid = "";
        String searchtext = "";
        String companyid = "";
        String userid = null;
        List ll = null;
        int dl = 0;
        String sortField = "auditTime";
        String sortDirection = "DESC";
        try {
            if (requestParams.containsKey("start") && requestParams.containsKey("limit") && !StringUtil.isNullOrEmpty(requestParams.get("start").toString())) {
                start = Integer.parseInt(requestParams.get("start").toString());
                limit = Integer.parseInt(requestParams.get("limit").toString());
            }
            if (requestParams.containsKey("groupid") && requestParams.get("groupid") != null) {
                groupid = requestParams.get("groupid").toString();
            }
            if (requestParams.containsKey("search") && requestParams.get("search") != null) {
                searchtext = requestParams.get("search").toString();
            }
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            
            /**
             * Variable 'userid' to add user.userID condition in final query.  
             */
            if (requestParams.containsKey(Constants.useridKey)) {
                userid = (String) requestParams.get(Constants.useridKey);
            }
            Date stDate = null;
            Date endDate = null;
            if (requestParams.containsKey("stDate") && requestParams.get("stDate") != null) {
                stDate = (Date) requestParams.get("stDate");
            }
            if (requestParams.containsKey("eDate") && requestParams.get("eDate") != null) {
                endDate = (Date) requestParams.get("eDate");
            }
            if (requestParams.containsKey("direction") && requestParams.get("direction") != null && !requestParams.get("direction").toString().isEmpty()) {
                sortDirection = requestParams.get("direction").toString();
            }
            if (requestParams.containsKey("sort") && requestParams.get("sort") != null && !requestParams.get("sort").toString().isEmpty()) {
                if("timestamp".equals(requestParams.get("sort").toString())){
                    sortField = "auditTime";
                } else if("username".equals(requestParams.get("sort").toString())) {
                    sortField ="user.userLogin.userName";
                } else {
                    sortField = requestParams.get("sort").toString();
                }
            }
            String userDepartment = "";
            if (requestParams.containsKey("userDepartment") && requestParams.get("userDepartment") != null) {
                userDepartment = (String) requestParams.get("userDepartment");
            }
            ArrayList al = new ArrayList();
            al.add(companyid);
            String condition = "";
            if (stDate != null) {
                condition += " and auditTime >= ?";
                al.add(stDate);
            }
            if (endDate != null) {
                condition += " and auditTime < ?";
                al.add(endDate);
            }
            
            /**
             * Adding user.userID condition in final query.  
             */
            if (!StringUtil.isNullOrEmpty(userid)) {
                condition+=" and user.userID=?";
                al.add(userid);
            }
            
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                condition += " and user.department = ? ";
                al.add(userDepartment);
            }

            String auditID = "";
            StringBuilder sb = new StringBuilder();
            if (searchtext.compareTo("") != 0) {
                StringBuilder query2 = new StringBuilder();
                if (searchtext.length() > 0) {
                    searchtext = QueryParser.escape(searchtext);
                    if (searchtext.length() > 2) {
                        searchtext = !searchtext.contains(" ") ? searchtext + "*" : searchtext; //Add '*'(wildcard) for searching on single token
                    }
                    query2.append(searchtext);
                }
                SearchBean bean = new SearchBean();
                String indexPath = storageHandlerImpl.GetAuditTrailIndexPath();
                ArrayList<String> searchFieldArray = new ArrayList<String>();
                searchFieldArray.add("details");
                searchFieldArray.add("ipaddr");
                searchFieldArray.add("username");
                if (!StringUtil.isNullOrEmpty(companyid)) {
                    query2.append(query2.length() > 0 ? " AND " : "");
                    query2.append("(");
                    query2.append("companyid");
                    query2.append(":");
                    query2.append(companyid);
                    query2.append(")");

                }
                String[] searchWithIndex = searchFieldArray.toArray(new String[searchFieldArray.size()]);// {"details", "ipaddr", "username"};
                Hits hitResult = bean.searchIndexWithSort(query2.toString(), searchWithIndex, indexPath,null);
                if (hitResult != null) {
                    for (int i = 0; i < hitResult.length(); i++) {
                       org.apache.lucene.document.Document doc = hitResult.doc(i);
                        sb.append("'");
                        sb.append(doc.get("transactionid"));
                        sb.append("',");
                    }
                    auditID = sb.toString();
                    if (auditID.length() > 0) {
                        auditID = auditID.substring(0, auditID.length() - 1);
                    }
                }
            }

            if (groupid.compareTo("") != 0 && searchtext.compareTo("") != 0) {  /*
                 * query for both gid and search
                 */
                if (auditID.length() > 0) {
                    al.add(groupid);
                    String query = "from AuditTrail where user.company.companyID=? " + condition + " and ID in (" + auditID + ") and action.auditGroup.ID = ? order by "+ sortField + " "  + sortDirection;
                    String selectQuery = "select count(ID) " +query;
                    ll = executeQuery( selectQuery, al.toArray());
                    if(!StringUtil.isNullObject(ll) ){
                        dl = ((Long)ll.get(0)).intValue();
                    }
                    ll = executeQueryPaging( query, al.toArray(), new Integer[]{start, limit});
                } else {
                    dl = 0;
                    ll = new ArrayList();
                }
            } else if (groupid.compareTo("") != 0 && searchtext.compareTo("") == 0) { /*
                 * query only for gid
                 */
                al.add(groupid);
                String query = "from AuditTrail where user.company.companyID=? " + condition + " and action.auditGroup.ID = ? order by "+ sortField + " " + sortDirection;
                String selectQuery = "select count(ID) " +query;
                    ll = executeQuery( selectQuery, al.toArray());
                    if(!StringUtil.isNullObject(ll) ){
                        dl = ((Long)ll.get(0)).intValue();
                    }
                ll = executeQueryPaging( query, al.toArray(), new Integer[]{start, limit});
            } else if (groupid.compareTo("") == 0 && searchtext.compareTo("") != 0) {  /*
                 * query only for search
                 */
                if (auditID.length() > 0) {
                    String query = "from AuditTrail where user.company.companyID=? " + condition + " and ID in (" + auditID + ")  order by "+ sortField + " " + sortDirection;
                    String selectQuery = "select count(ID) " +query;
                    ll = executeQuery( selectQuery, al.toArray());
                    if(!StringUtil.isNullObject(ll) ){
                        dl = ((Long)ll.get(0)).intValue();
                    }
                    ll = executeQueryPaging( query, al.toArray(), new Integer[]{start, limit});
                } else {
                    dl = 0;
                    ll = new ArrayList();
                }
            } else {        /*
                 * query for all
                 */
                String query = "from AuditTrail where user.company.companyID=?  " + condition + " order by "+ sortField + " " + sortDirection;
                String selectQuery = "select count(ID) " +query;
                    ll = executeQuery( selectQuery, al.toArray());
                    if(!StringUtil.isNullObject(ll) ){
                        dl = ((Long)ll.get(0)).intValue();
                    }
                ll = executeQueryPaging( query, al.toArray(), new Integer[]{start, limit});
            }
        } catch (IOException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getAuditGroupData() throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String query = "from AuditGroup  order by groupName";
            ll = executeQuery( query);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getAuditDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            StringBuffer usersList = null;
            if (requestParams.containsKey("userslist")) {
                usersList = (StringBuffer) requestParams.get("userslist");
            }
            String groups = requestParams.get("groups").toString();
            if (StringUtil.isNullOrEmpty(groups)) {
                groups = "null";
            }
            int start = Integer.parseInt(requestParams.get("start").toString());
            int limit = Integer.parseInt(requestParams.get("limit").toString());
            int interval = Integer.parseInt(requestParams.get("interval").toString());
            String query = "from AuditTrail at where";
            if (!StringUtil.isNullOrEmpty(usersList.toString())) {
                query += " at.user.userID in (" + usersList + ")  and";
            }
            query += " DATEDIFF(date(now()),date(at.auditTime)) <= ? and "
                    + "at.action.auditGroup.groupName in (" + groups + ") order by at.auditTime desc";
            ll = executeQuery( query, interval);
            dl = ll.size();
            ll = executeQueryPaging( query, new Object[]{interval}, new Integer[]{start, limit});
        } catch (Exception e) {
            throw ServiceException.FAILURE("auditTrailDAOImpl.getRecentActivityDetails : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "002", "", ll, dl);
    }
}
