/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.auditTrail;

import com.krawler.common.admin.AuditTrail;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author krawler
 */
public class AccAuditTrailServiceImplCMN implements AccAuditTrailServiceCMN {
    
    private AccountingHandlerDAO accountingHandlerDAOobj;
    
    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    
    /**
     * Following method is used to return Audit Data in JSON Format.
     * @param auditData
     * @param paramObj
     * @param totalSize
     * @return
     * @throws SessionExpiredException
     * @throws ServiceException 
     */
    @Override
    public JSONObject getAuditJSONData(List auditData, JSONObject paramObj, int totalSize) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            Iterator itr = auditData.iterator();
            JSONArray jArr = new JSONArray();
            String userdate="";
            while (itr.hasNext()) {
                AuditTrail auditTrail = (AuditTrail) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", auditTrail.getID());
               
                /**ERP-40869
                 * While getting username from AuditTrail obj it throws 
                 * hibernate lazy Initialization exception So, need to get 
                 * username and user fullname using User Object.
                 * 
                 */
                String userid = auditTrail.getUser().getUserID();
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
                User user = (User) returnObject.getEntityList().get(0);
                obj.put("username", user.getUserLogin().getUserName() + " [ " +  user.getFullName() + " ]");
                obj.put("ipaddr", auditTrail.getIPAddress());
                Date auditTime = auditTrail.getAuditTime();
                if (auditTrail.getAuditTime() != null) {
                    userdate = authHandler.getUTCToUserLocalDateFormatter_NEWJson(paramObj, auditTime);
                }
                obj.put("timestamp", userdate);
                userdate = new SimpleDateFormat(Constants.yyyyMMdd).format(auditTime);
                if (auditTrail.getDetails().contains("has logged out")) {
                    obj.put("details", auditTrail.getDetails());
                    obj.put("link", "  <a href='#' onclick='callUserSummaryReport(undefined,undefined,undefined,undefined,undefined,undefined,\""+userdate+"\",\""+userdate+"\",undefined, \"" + auditTrail.getUser().getUserID() +"\")'>" + "View User Transaction Summary Report" + "</a>");
                } else {
                    obj.put("details", auditTrail.getDetails());
                }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", totalSize);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
}
