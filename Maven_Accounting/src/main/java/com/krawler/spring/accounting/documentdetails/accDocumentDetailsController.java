package com.krawler.spring.accounting.documentdetails;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.CompanySessionClass;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.esp.handlers.APICallHandlerService;

/*
 * Copyright (C) 2012 Krawler Information Systems Pvt Ltd All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA. package
 * com.krawler.spring.accounting.documentdetails;
 *
 * /**
 *
 * @author Pandurang
 */
public class accDocumentDetailsController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private String successView;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accDetailsDAO accDetailsDao;
    private APICallHandlerService apiCallHandlerService;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setAccDetailsDAO(accDetailsDAO accDetailsDao) {
        this.accDetailsDao = accDetailsDao;
    }
    
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }

    /*
     *
     * public JSONObject getDetailPanelJson(List ll, HttpServletRequest request)
     * { JSONObject jobj = new JSONObject(); JSONArray jArr = new JSONArray();
     * JSONArray tempjArr = new JSONArray(); JSONArray tempjArray = new
     * JSONArray(); try { // StringBuffer usersList =
     * sessionHandlerImpl.getRecursiveUsersList(request); Iterator itr =
     * ll.iterator(); while (itr.hasNext()) { AuditTrail auditTrail =
     * (AuditTrail) itr.next(); JSONObject obj = new JSONObject(); if
     * (StringUtil.equal(auditTrail.getAction().getID(), "88")) { JSONObject
     * tempObj = new JSONObject(); tempObj.put("id", auditTrail.getID()); //
     * tempObj.put("user",
     * profileHandlerDAOObj.getUserFullName(auditTrail.getUser().getUserID()));
     * tempObj.put("ipaddr", auditTrail.getIPAddress()); tempObj.put("details",
     * auditTrail.getDetails()); tempObj.put("action",
     * auditTrail.getAction().getActionName()); // tempObj.put("time",
     * auditTrail.getAudittime()); tempObj.put("imgsrc",
     * "../../images/activity1.gif"); tempObj.put("marginbottom", "17");
     * tempObj.put("width", "20px"); tempObj.put("height", "25px");
     * tempjArr.put(tempObj); } else if
     * (StringUtil.equal(auditTrail.getAction().getID(), "89")) { JSONObject
     * tempObject = new JSONObject(); tempObject.put("id", auditTrail.getID());
     * // tempObject.put("user",
     * profileHandlerDAOObj.getUserFullName(auditTrail.getUser().getUserID()));
     * tempObject.put("ipaddr", auditTrail.getIPAddress());
     * tempObject.put("details", auditTrail.getDetails());
     * tempObject.put("action", auditTrail.getAction().getActionName()); //
     * tempObject.put("time", auditTrail.getAudittime());
     * tempObject.put("imgsrc", "../../images/activity1.gif");
     * tempObject.put("marginbottom", "17"); tempObject.put("width", "20px");
     * tempObject.put("height", "25px"); tempjArray.put(tempObject); } else {
     * obj.put("id", auditTrail.getID()); if
     * (StringUtil.equal(auditTrail.getAction().getID(), "87")) {
     * obj.put("user", "-"); } else { // obj.put("user",
     * profileHandlerDAOObj.getUserFullName(auditTrail.getUser().getUserID()));
     * } obj.put("ipaddr", auditTrail.getIPAddress()); obj.put("details",
     * auditTrail.getDetails()); obj.put("action",
     * auditTrail.getAction().getActionName()); // obj.put("time",
     * auditTrail.getAudittime()); if (auditTrail.getDetails().contains("Task
     * for")) { obj.put("imgsrc", "../../images/task.gif");
     * obj.put("marginbottom", "19"); obj.put("width", "20px");
     * obj.put("height", "20px"); } else if
     * (auditTrail.getDetails().contains("Event for")) { obj.put("imgsrc",
     * "../../images/event.gif"); obj.put("marginbottom", "19");
     * obj.put("width", "20px"); obj.put("height", "20px"); } else if
     * (auditTrail.getAction().getActionName().contains("Activity")) {
     * obj.put("imgsrc", "../../images/activity1.gif"); obj.put("marginbottom",
     * "17"); obj.put("width", "20px"); obj.put("height", "25px");
     *
     * } else { obj.put("imgsrc", "../../images/activity1.gif");
     * obj.put("marginbottom", "17"); obj.put("width", "20px");
     * obj.put("height", "25px"); } jArr.put(obj); } }
     * jobj.put("AuditChecklist", tempjArr); jobj.put("AuditAssignlist",
     * tempjArray);
     *
     * // recent activities // Date today = new Date(); // // GregorianCalendar
     * tempCal = new GregorianCalendar(); // tempCal.add(Calendar.MONTH, 1); //
     * Date maxDateUpdate = tempCal.getTime(); // // String module =
     * request.getParameter("module"); // String recid =
     * request.getParameter("recid"); // HashMap<String, Object> requestParams =
     * new HashMap<String, Object>(); // requestParams.put("module", module); //
     * requestParams.put("recid", recid); // requestParams.put("OneTimeFlag",
     * false); // requestParams.put("usersList", usersList); //
     * requestParams.put("today", today); // requestParams.put("maxDateUpdate",
     * maxDateUpdate);
     *
     * // List list = null; // list =
     * crmActivityDAOObj.getDetailPanelRecentActivity(requestParams).getEntityList();
     * // // if(list != null){ // itr = list.iterator(); // while
     * (itr.hasNext()) { // Object[] rows = (Object[]) itr.next(); //
     * CrmActivityMaster actMasterobj = (CrmActivityMaster) rows[0]; //
     * JSONObject obj = new JSONObject(); // // Date eventDate = null; // Date
     * eventNxtDate = null; // // Date tillDate = tempCal.getTime(); //
     * if(actMasterobj.getTilldate()!=null) // tillDate =
     * actMasterobj.getTilldate(); // Date actualStDt =
     * actMasterobj.getStartdate(); // Date tmpDate = actualStDt; // int
     * scheduleType = actMasterobj.getScheduleType(); // if(scheduleType!=0) {
     * // Date currDate = new Date(); // long datediff = ((currDate.getTime() -
     * actualStDt.getTime()) / DAY_MILLI); // int days = 0; //
     * switch(scheduleType) { // case 1 : // //
     * if(tmpDate.compareTo(tillDate)<=0) { // eventDate = tmpDate; // Date of
     * event when it occurs first time. // } // // Date of event when it will be
     * occur next time // GregorianCalendar nextEventDate = new
     * GregorianCalendar(); // nextEventDate.setTime(actualStDt); //
     * nextEventDate.set(Calendar.DAY_OF_MONTH, currDate.getDate()); //
     * nextEventDate.set(Calendar.MONTH, currDate.getMonth()); //
     * nextEventDate.set(Calendar.YEAR, 1900+currDate.getYear()); //
     * if(nextEventDate.getTimeInMillis()>currDate.getTime()){ //
     * if(eventDate.getTime()==nextEventDate.getTimeInMillis()){ //
     * nextEventDate.add(Calendar.DATE, 1); // tmpDate =
     * nextEventDate.getTime(); // // } else{ // tmpDate =
     * nextEventDate.getTime(); // } // }else{ //
     * nextEventDate.add(Calendar.DATE, 1); // tmpDate =
     * nextEventDate.getTime(); // } // // if(tmpDate.compareTo(tillDate)<=0) {
     * // eventNxtDate = tmpDate; // } // // break; // case 2 : // days = (int)
     * ((datediff / 7) + (datediff % 7 == 0 ? 0 : 1)) * 7; // tmpDate = new
     * Date(actualStDt.getTime()); // if (tmpDate.compareTo(tillDate) <= 0) { //
     * eventDate = tmpDate; // } // GregorianCalendar weeklyEvent = new
     * GregorianCalendar(); // weeklyEvent.setTime(eventDate); // //
     * GregorianCalendar weeklyNextEvent = new GregorianCalendar(); //
     * weeklyNextEvent.setTime(currDate); // int addDays =0; //
     * if(weeklyNextEvent.DAY_OF_WEEK >= weeklyEvent.DAY_OF_WEEK){ // addDays =
     * 7-(weeklyNextEvent.DAY_OF_WEEK-weeklyEvent.DAY_OF_WEEK); // // }else{ //
     * addDays = weeklyEvent.DAY_OF_WEEK-weeklyNextEvent.DAY_OF_WEEK; // } //
     * weeklyNextEvent.add(Calendar.DATE, addDays); // tmpDate = new
     * Date(weeklyNextEvent.getTimeInMillis() + DAY_MILLI * days); // if
     * (tmpDate.compareTo(tillDate) <= 0) { // eventNxtDate =
     * weeklyNextEvent.getTime(); // } // // break; // case 3 : //
     * GregorianCalendar monthlyEvent = new GregorianCalendar(); //
     * monthlyEvent.setTime(actualStDt); // // GregorianCalendar
     * monthlyNextEvent = new GregorianCalendar(); //
     * monthlyNextEvent.setTime(actualStDt); //
     * monthlyNextEvent.set(Calendar.MONTH, currDate.getMonth()); // //
     * if(monthlyNextEvent.getTimeInMillis()<currDate.getTime()){ //
     * monthlyNextEvent.add(Calendar.MONTH, 1); // } // eventDate =
     * monthlyEvent.getTime(); // eventNxtDate = monthlyNextEvent.getTime(); //
     * // break; // } // String details = ""; // String fullDetails=""; //
     * if(actMasterobj.getFlag().equals("Task")) { // obj.put("imgsrc",
     * "../../images/task.gif"); // obj.put("marginbottom", "16"); // details =
     * "Task "; // } else if(actMasterobj.getFlag().equals("Event")) { //
     * obj.put("imgsrc", "../../images/event.gif"); // obj.put("marginbottom",
     * "15"); // details = "Event"; // } else
     * if(actMasterobj.getFlag().equals("Phone Call")){ // obj.put("imgsrc",
     * "../../images/phone_call.gif"); // obj.put("marginbottom", "16"); //
     * details = "Phone Call"; // } else { // obj.put("imgsrc",
     * "../../images/activity1.gif"); // obj.put("marginbottom", "17"); // } //
     * if(!StringUtil.isNullOrEmpty(actMasterobj.getSubject())) // fullDetails =
     * details+" \""+actMasterobj.getSubject()+"\""; // if(eventDate!=null) { //
     * obj.put("id", actMasterobj.getActivityid()); // obj.put("user",
     * StringUtil.getFullName(actMasterobj.getUsersByUserid())); //
     * obj.put("action", details + " scheduled "); // obj.put("details",
     * fullDetails ); // obj.put("time", eventDate.getTime()); // jArr.put(obj);
     * // } // if(eventNxtDate!=null) { // JSONObject nextEvent = new
     * JSONObject(); // nextEvent.put("id", actMasterobj.getActivityid()); //
     * nextEvent.put("user",
     * StringUtil.getFullName(actMasterobj.getUsersByUserid())); //
     * nextEvent.put("action", details + " scheduled "); //
     * nextEvent.put("imgsrc", obj.getString("imgsrc")); //
     * nextEvent.put("marginbottom", obj.getString("marginbottom")); //
     * nextEvent.put("details", fullDetails ); // nextEvent.put("time",
     * eventNxtDate.getTime()); // jArr.put(nextEvent); // } // // } // } // //
     * } // } // } // One Time event //
     * if(Constants.moduleMap.containsKey(module)) { // int temp =
     * Constants.moduleMap.get(module); // if(temp == 1 || temp == 2 || temp ==
     * 3 || temp == 4 || temp == 6 || temp == 8) { // GregorianCalendar
     * lastMaxDtCal = new GregorianCalendar(); //
     * lastMaxDtCal.add(Calendar.DATE, -5); // Date lastMaxDateUpdate =
     * lastMaxDtCal.getTime(); // // requestParams.put("lastMaxDateUpdate",
     * lastMaxDateUpdate); // requestParams.put("OneTimeFlag", true); // list =
     * crmActivityDAOObj.getDetailPanelRecentActivity(requestParams).getEntityList();
     * // // Iterator ite = list.iterator(); // while (ite.hasNext()) { // //
     * Object[] rows = (Object[]) ite.next(); // CrmActivityMaster actMasterobj
     * = (CrmActivityMaster) rows[0]; // JSONObject obj = new JSONObject(); //
     * String details = ""; // if(actMasterobj.getFlag().equals("Task")) { //
     * obj.put("imgsrc", "../../images/task.gif"); // obj.put("marginbottom",
     * "16"); // details = "Task "; // } else
     * if(actMasterobj.getFlag().equals("Event")) { // obj.put("imgsrc",
     * "../../images/event.gif"); // obj.put("marginbottom", "15"); // details =
     * "Event"; // } else if(actMasterobj.getFlag().equals("Phone Call")){ //
     * obj.put("imgsrc", "../../images/phone_call.gif"); //
     * obj.put("marginbottom", "16"); // details = "Phone Call"; // } else { //
     * obj.put("imgsrc", "../../images/activity1.gif"); //
     * obj.put("marginbottom", "17"); // } //
     * if(!StringUtil.isNullOrEmpty(actMasterobj.getSubject())) // details += "
     * \""+actMasterobj.getSubject()+"\""; // obj.put("id",
     * actMasterobj.getActivityid()); // obj.put("user",
     * StringUtil.getFullName(actMasterobj.getUsersByUserid())); //
     * obj.put("action", details + " scheduled "); // obj.put("details", details
     * + (!StringUtil.isNullOrEmpty(actMasterobj.getPhone())?" on "
     * +actMasterobj.getPhone():"")); // obj.put("time",
     * actMasterobj.getStartDate()); // jArr.put(obj); // } // } // } // //
     * if(module.equals("campaign")) { // requestParams.clear(); //
     * requestParams.put("recid", recid); // int
     * tzdiff=TimeZone.getTimeZone("GMT"+sessionHandlerImpl.getTimeZoneDifference(request)).getOffset(System.currentTimeMillis());
     * // requestParams.put("tzdiff", tzdiff); // List CLll =
     * crmCampaignDAOObj.getDetailPanelRecentCampaign(requestParams).getEntityList();
     * // Iterator campaignLogList = CLll.iterator(); // while
     * (campaignLogList.hasNext()) { // JSONObject tempCampaignLogData = new
     * JSONObject(); // JSONObject obj = new JSONObject(); // Object[] logObj =
     * (Object[])campaignLogList.next(); // // obj.put("id", logObj[4]); //
     * CrmCampaign crmCamp = (CrmCampaign) logObj[5]; //// String eventDate =
     * dateFmt.format((Date)logObj[0]); //
     * //crmManagerDAOObj.preferenceDatejsformat(timeZoneDiff, (Date)logObj[0],
     * dateFmt); // // String details = "Email Marketing Campaign"; //
     * obj.put("imgsrc", "../../images/activity1.gif"); //
     * obj.put("marginbottom", "17"); // obj.put("user",
     * StringUtil.getFullName(crmCamp.getUsersByUserid())); // obj.put("action",
     * details + " scheduled "); // obj.put("details", details + " on "); //
     * obj.put("sent",logObj[1]); // obj.put("viewed",logObj[2]); //
     * obj.put("failed",logObj[3]); // obj.put("time",logObj[0]==null? null:
     * ((Date)logObj[0]).getTime()); // jArr.put(obj); // } // }
     *
     * // for(int i = 0; i < jArr.length(); i++) { // for(int j =0; j <
     * jArr.length(); j++) { //
     * if(jArr.getJSONObject(i).optLong("time")>jArr.getJSONObject(j).optLong("time")){
     * // JSONObject jobj1 = jArr.getJSONObject(i); // jArr.put(i,
     * jArr.getJSONObject(j)); // jArr.put(j, jobj1); // } // } // } // //
     * jobj.put("auditList", jArr); } catch (Exception e) {
     * logger.warn(e.getMessage(), e); } return jobj; }
     *
     * public static int getMonthDifference(GregorianCalendar fromCalendar,
     * GregorianCalendar toCalendar) { int count = 0;
     * for(fromCalendar.add(Calendar.MONTH, 1);
     * fromCalendar.compareTo(toCalendar) <= 0; fromCalendar.add(
     * Calendar.MONTH, 1)) { count++; } return count; }
     *
     * private Date addTimePart(Date date, String timePart) throws
     * ParseException { Date datePart = new Date(); SimpleDateFormat timeformat
     * = new SimpleDateFormat("h:mm a"); SimpleDateFormat dfWithNoTime = new
     * SimpleDateFormat("yyyy-MM-dd 00:00:00"); SimpleDateFormat dtf = new
     * SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); try { Calendar cal = new
     * GregorianCalendar(); datePart =
     * dfWithNoTime.parse(dfWithNoTime.format(date)); cal.setTime(datePart);
     * Date sttime = timeformat.parse(timePart); cal.add(Calendar.HOUR,
     * sttime.getHours()); cal.add(Calendar.MINUTE, sttime.getMinutes());
     * datePart = cal.getTime(); } catch(ParseException ex) {
     * logger.warn(ex.getMessage(), ex); } return datePart; }
     */
    public JSONObject getCommentJson(List ll, HttpServletRequest request) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();

        KwlReturnObject kmsg = null;
        try {
            String userid = sessionHandlerImpl.getUserid(request);
            DateFormat df = authHandler.getDateFormatter(request);
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                JSONObject temp = new JSONObject();
                Comment t = (Comment) ite.next();
                temp.put("comment", t.getComment());
                if (t.getPostedon() != null) {
                    temp.put("postedon", df.format(t.getPostedon()));
                }
                if (t.getUpdatedon() != null) {
                    temp.put("updatedon", df.format(t.getUpdatedon()));
                }
                temp.put("addedby", t.getUser().getFullName());//profileHandlerDAOObj.getUserFullName(t.getuserId().getUserID())
                temp.put("commentid", t.getId());
                temp.put("deleteflag", true);
                jarr.put(temp);
            }
            jobj.put("commList", jarr);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return jobj;
    }
//    public JSONObject getCaseCommentJson(List ll, HttpServletRequest request) {
//        JSONObject jobj = new JSONObject();
//        JSONArray jarr = new JSONArray();
//        KwlReturnObject kmsg = null;
//        String firstName="";
//        String lastName="";
//        try {
//            String userid = sessionHandlerImpl.getUserid(request);
//            Iterator ite = ll.iterator();
//            while (ite.hasNext()) {
//                JSONObject temp = new JSONObject();
//                CaseComment t = (CaseComment) ite.next();
//                temp.put("comment", t.getComment());
//                temp.put("postedon", crmManagerDAOObj.userPreferenceDate(request, new Date(t.getPostedon()), 0));
//                if(t.getUserflag()==com.krawler.crm.utils.Constants.CASE_COMMENT_USERFLAG){
//                	String contactId=t.getuserId().toString();
//                	Object[] row =crmCommentDAOObj.getCustomerName(contactId);
//            		if (row!=null) {
//        				firstName = StringUtil.isNullOrEmpty((String) row[0])?"":(String) row[0];;
//        				lastName = StringUtil.isNullOrEmpty((String) row[1])?"":(String) row[1];;
//        			}
//            		temp.put("addedby", firstName+" "+lastName.trim());
//                }else{
//            		temp.put("addedby", profileHandlerDAOObj.getUserFullName(t.getuserId()));
//                }
//                temp.put("commentid", t.getId());
//                temp.put("deleteflag", userid.equals(t.getuserId()));
//                jarr.put(temp);
//               // kmsg = crmCommentDAOObj.deleteComments(userid, t.getId());
//            }
//            jobj.put("commPerm", ((sessionHandlerImpl.getPerms(request, "Comments") & 2) == 2));
//            jobj.put("commList", jarr);
//        } catch (Exception e) {
//            logger.warn(e.getMessage(),e);
//        }
//        return jobj;
//    }

    public JSONObject getDocumentJson(List ll, HttpServletRequest request) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        int count = 1;
        try {
            DateFormat dateFormat = authHandler.getDateFormatter(request);
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                JSONObject temp = new JSONObject();
                Docs t = (Docs) ite.next();
                temp.put("srno", count++);
                temp.put("docid", t.getId());
                temp.put("Name", t.getDocname());
                temp.put("Size", t.getDocsize());
                temp.put("Type", t.getDoctype());
                temp.put("uploadedby", t.getUser().getFullName());
                temp.put("isshared", t.getIsshared());
                temp.put("uploadedon", dateFormat.format(t.getUploadedon()));
                jarr.put(temp);
            }
            jobj.put("docList", jarr);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return jobj;
    }

    public ModelAndView getDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        JSONObject jobj2 = new JSONObject();
        JSONObject jobj3 = new JSONObject();
        JSONArray FinalArr = new JSONArray();
        KwlReturnObject kmsg = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String recid = request.getParameter("recid");
            String detailFlag = request.getParameter("detailFlag");
            String module = request.getParameter("module");
//            requestParams.put("recid", StringUtil.checkForNull(recid));
//            requestParams.put("detailFlag", StringUtil.checkForNull(detailFlag));
//            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
//            StringBuffer usersList = sessionHandlerImpl.getRecursiveUsersList(request);
//            kmsg = auditTrailDAOObj.getRecentActivityDetails(requestParams);
//            jobj3 = getDetailPanelJson(kmsg.getEntityList(), request);       //used for Recent Activity
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            requestParams1.put("recid", recid);
            requestParams1.put("module", module);
            
            kmsg = accDetailsDao.getComments(requestParams1);
            jobj1 = getCommentJson(kmsg.getEntityList(), request);

            kmsg = accDetailsDao.getDocuments(requestParams1);
            jobj2 = getDocumentJson(kmsg.getEntityList(), request);
//            jobj.put("emailData",new JSONObject().put("emailList", new JSONArray()));
//            if (!StringUtil.isStandAlone()) {
//                if(module.equals("Case")) {
//                    boolean flag = false;
//                    CrmCase obj = (CrmCase) KwlCommonTablesDAOObj.getObject("com.krawler.crm.database.tables.CrmCase", StringUtil.checkForNull(recid));
//                    if (obj.getCrmAccount() != null) {
//                        String email = obj.getCrmAccount().getEmail();
//                        if (!StringUtil.isNullOrEmpty(email)) {
//                            JSONObject jobjEmail = new JSONObject();
//                            jobjEmail=mailIntDAOObj.getRecentEmailDetails(request, recid,email);
//                            FinalArr=jobjEmail.getJSONArray("emailList");
//                            flag=true;
//                        }
//                    }
//                    
//                    if(flag) {
//                        JSONObject jobjEmail = new JSONObject();
//                        jobjEmail.put("emailList", FinalArr);
//                        jobj.put("emailData", jobjEmail);
//                    }
//                }else{
//                    String email = request.getParameter("email");
//                    if(!StringUtil.isNullOrEmpty(email)){
//                        jobj.put("emailData", mailIntDAOObj.getRecentEmailDetails(request, recid,email));
//                    }
//                }
//            }
//            
            //String id="";

//            JSONObject jobj6 = getWorkedTime(recid, companyid, usersList,requestParams);

            jobj.put("commData", jobj1);
            jobj.put("docData", jobj2);
//            JSONObject jobj6 = new JSONObject();
//            jobj6.put("auditlistList", jobj3.get("auditList"));
//            jobj.put("auditData", jobj3);
//            JSONObject jobj4 = new JSONObject();
//            jobj4.put("auditChecklistList", jobj3.get("AuditChecklist"));
//            jobj.put("auditChecklist", jobj4);
//            JSONObject jobj5 = new JSONObject();
//            jobj5.put("auditAssignList", jobj3.get("AuditAssignlist"));
//            jobj.put("auditAssign", jobj5);
//            jobj.put("totalTime", jobj6);
            //jobj.
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getComment(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        JSONObject jobj = new JSONObject();
        List<String> idsList = new ArrayList<String>();
        Map kwlcommentz = new HashMap();
        String id = request.getParameter("id");
        String module = request.getParameter("module");
        KwlReturnObject kmsg = null;
        String view = "jsonView";
        try {
            idsList.add(id);
            String comment = "";

            StringBuilder sb = new StringBuilder();
            List commentList = (List) kwlcommentz.get(id);
            if (commentList != null && !commentList.isEmpty()) {
                Iterator ite = commentList.iterator();
                int count = 1;
                while (ite.hasNext()) {
                    String cmo = "";
                    try {
                        cmo = (String) ite.next();
                    } catch (Exception ex) {
                        cmo = "Can't decode comments for record : " + id + "<br>" + ex.getMessage();
                    }
                    cmo = " " + count + ")" + cmo + "\n";
                    count++;
                    sb.append(cmo);
                }
            }
            if (!StringUtil.isNullOrEmpty(sb.toString())) {
                comment = sb.toString();
            }

            jobj.put("comment", comment);
            jobj.put("success", true);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView attachDocuments(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        Boolean success = false;
        JSONObject jobj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            uploadDoc(request);
            success = true;
            msg = messageSource.getMessage("acc.invoiceList.bt.fileUploadedSuccess", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(Docs.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            txnManager.rollback(status);
            Logger.getLogger(Docs.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {
                jobj.put("success", success);
                jobj.put("msg", msg);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(Docs.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public void uploadDoc(HttpServletRequest request)
            throws ServiceException {
        try {
            String result = "";
            Boolean fileflag = false;
            String fileName = "";
            boolean isUploaded;
            String Ext;
            final String sep = StorageHandler.GetFileSeparator();
            DiskFileUpload fu = new DiskFileUpload();
            java.util.List fileItems = null;
            FileItem fi = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            try {
                fileItems = fu.parseRequest(request);
            } catch (FileUploadException e) {
                throw ServiceException.FAILURE("accDocumentDetailsController.updateDoc", e);
            }
            java.util.HashMap arrParam = new java.util.HashMap();
            for (java.util.Iterator k = fileItems.iterator(); k.hasNext();) {
                fi = (FileItem) k.next();
                arrParam.put(fi.getFieldName(), fi.getString());
                if (!fi.isFormField()) {
                    if (fi.getSize() != 0) {
                        fileflag = true;
                        fileName = new String(fi.getName().getBytes());
                    }
                }
            }

            if (fileflag) {
                try {
                    String storePath = StorageHandler.GetDocStorePath();
                    File destDir = new File(storePath);
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                    }
                    int doccount = 0;
                    fu = new DiskFileUpload();
                    fu.setSizeMax(-1);
                    fu.setSizeThreshold(4096);
                    fu.setRepositoryPath(storePath);
                    for (Iterator i = fileItems.iterator(); i.hasNext();) {
                        fi = (FileItem) i.next();
                        if (!fi.isFormField() && fi.getSize() != 0 && doccount < 3) {
                            Ext = "";
                            doccount++;//ie 8 fourth file gets attached				
                            String filename = UUID.randomUUID().toString();
                            try {
                                fileName = new String(fi.getName().getBytes(), "UTF8");
                                if (fileName.contains(".")) {
                                    Ext = fileName.substring(fileName.lastIndexOf("."));
                                }
                                if (fi.getSize() != 0) {
                                    isUploaded = true;
                                    File uploadFile = new File(storePath + sep
                                            + filename + Ext);
                                    fi.write(uploadFile);



                                    KwlReturnObject curresult = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
                                    Company company = (Company) curresult.getEntityList().get(0);

                                    curresult = accountingHandlerDAOobj.getObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
                                    User user = (User) curresult.getEntityList().get(0);

                                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                    hashMap.put("DocumentId", filename);    //UUID
                                    hashMap.put("DocumentNm", fileName);    //Actual File Name
                                    hashMap.put("DocumentType", Ext);
                                    hashMap.put("DocumentSize", Float.valueOf(fi.getSize()) / 1000);
                                    hashMap.put("User", user);
                                    hashMap.put("recordId", request.getParameter("recid"));     //Associated ProductID
                                    hashMap.put("moduleId", request.getParameter("moduleid"));
                                    hashMap.put("Company", company);
                                    accDetailsDao.saveDocuments(hashMap);
                                } else {
                                    isUploaded = false;
                                }
                            } catch (Exception e) {
                                Logger.getLogger(Docs.class.getName()).log(Level.SEVERE, null, e);
                                throw ServiceException.FAILURE("accDocumentDetailsController.uploadDoc", e);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(accDocumentDetailsController.class.getName()).log(Level.SEVERE, null, ex);
                    throw ServiceException.FAILURE("accDocumentDetailsController", ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accDocumentDetailsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accDocumentDetailsController.uploadDoc", ex);
        }
    }

    public ModelAndView addComments(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        HashMap<String, Object> myjobj = new HashMap<String, Object>();
        JSONObject rjobj = new JSONObject();
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jobj = new JSONObject();
        String map = "";
        String refid = "";
        boolean editMode = false;
        String auditAction = "";
        String details = "";
        String id;
        try {

            KwlReturnObject kmsg = null;
            refid = request.getParameter("recid");
            if(!refid.equals("")){
            String moduleId = request.getParameter("moduleId");
            String moduleName = request.getParameter("moduleName");
            String jsondata = request.getParameter("jsondata");
            
            String reccode=request.getParameter("custcode");
            JSONArray jarr = new JSONArray("[" + jsondata + "]");

            jobj = jarr.getJSONObject(0);
            map = jobj.getString("mapid");
            String commStrAudit = jobj.getString("comment");
            commStrAudit = commStrAudit.replaceAll("&nbsp;", "");
            String commentid = jobj.getString("commentid");

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
            Company company = (Company) curresult.getEntityList().get(0);

            curresult = accountingHandlerDAOobj.getObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
            User user = (User) curresult.getEntityList().get(0);

            myjobj.put("User", user);
            myjobj.put("moduleId", moduleId);
            myjobj.put("refid", refid);
            myjobj.put("comment", commStrAudit);
            myjobj.put("mapid", map);
            myjobj.put("Company", company);


            if (StringUtil.isNullOrEmpty(commentid)) { // Add Mode
                id = java.util.UUID.randomUUID().toString();
                myjobj.put("id", id);
                myjobj.put("postedon", new Date());
                kmsg = accDetailsDao.addComments(myjobj);
                auditTrailObj.insertAuditLog(auditAction, user.getFullName() + " added Comment for " + moduleName +" : "+reccode +" Comment: '" + commStrAudit, request, refid, id);
            } else { // Edit Mode
                curresult = accountingHandlerDAOobj.getObject(Comment.class.getName(), commentid);
                Comment comment = (Comment) curresult.getEntityList().get(0);
                myjobj.put("id", comment.getId());
                myjobj.put("postedon", comment.getPostedon());
                myjobj.put("updatedon", new Date());
                kmsg = accDetailsDao.editComments(myjobj);
                editMode = true;
                auditTrailObj.insertAuditLog(auditAction, user.getFullName() + " edited Comment for " + moduleName +" : "+reccode + " Comment: '" + commStrAudit, request, refid, java.util.UUID.randomUUID().toString());
            }
            txnManager.commit(status);
          }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }

        return new ModelAndView("jsonView", "model", rjobj.toString());
    }

    public ModelAndView deleteComment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject rjobj = new JSONObject();
        boolean sucessflag = false;
        String msg = "";
        String auditAction = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Comment_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String commentId = request.getParameter("id");
            String moduleName = request.getParameter("moduleName");
            String custcode = request.getParameter("custcode");
            accDetailsDao.deleteComments(commentId);
            sucessflag = true;
            msg = messageSource.getMessage("acc.comment.deleted", null, RequestContextUtils.getLocale(request));

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
            User user = (User) curresult.getEntityList().get(0);

            auditTrailObj.insertAuditLog(auditAction, user.getFullName() + " deleted Comment of module " + moduleName
                    + ", Customer Code: " + custcode, request, commentId);
            
            txnManager.commit(status);
        } catch (Exception e) {
            msg = e.getMessage();
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        } finally {
            try {
                rjobj.put("success", sucessflag);
                rjobj.put("msg", msg);
            } catch (JSONException je) {
                msg = je.getMessage();
                System.out.println(je.getMessage());
            }
        }

        return new ModelAndView("jsonView", "model", rjobj.toString());
    }

    public ModelAndView deleteDocument(HttpServletRequest request, HttpServletResponse response) {
        JSONObject rjobj = new JSONObject();
        boolean sucessflag = false;
        String msg = "";
        String auditAction = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Document_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String docsId = request.getParameter("docid");
            String moduleName = request.getParameter("moduleName");
            String custcode = request.getParameter("custcode");
            accDetailsDao.deleteDocument(docsId);
            sucessflag = true;
            msg = messageSource.getMessage("acc.document.deleted", null, RequestContextUtils.getLocale(request));

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
            User user = (User) curresult.getEntityList().get(0);

            auditTrailObj.insertAuditLog(auditAction, user.getFullName() + " deleted document of module " + moduleName
                    + ", Customer Code: " + custcode, request, docsId);
            txnManager.commit(status);
        } catch (Exception e) {
            msg = e.getMessage();
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        } finally {
            try {
                rjobj.put("success", sucessflag);
                rjobj.put("msg", msg);
            } catch (JSONException je) {
                msg = je.getMessage();
                System.out.println(je.getMessage());
            }
        }

        return new ModelAndView("jsonView", "model", rjobj.toString());
    }

    public ModelAndView downloadDocuments(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        JSONObject myjobj = new JSONObject();
        KwlReturnObject kmsg = null;
        String details = "", storepath="";
        String auditAction = "";
        try {
            String url = request.getParameter("url");   //Docs Table Primary key
            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Docs.class.getName(), url);
            Docs document = (Docs) curreslt.getEntityList().get(0);
            if(document.getIsshared()==1){
                storepath = StorageHandler.GetSharedDocStorePath(); //Get Shared Documents from this location
            } else {
                storepath = StorageHandler.GetDocStorePath();
            }
            
            String ext = "";
            if (document.getDocname().indexOf('.') != -1) {
                ext = document.getDocname().substring(document.getDocname().indexOf('.'));
            }
            File fp = new File(storepath + url + ext);
            byte[] buff = new byte[(int) fp.length()];
            FileInputStream fis = new FileInputStream(fp);
            int read = fis.read(buff);
            javax.activation.FileTypeMap mmap = new javax.activation.MimetypesFileTypeMap();
            response.setContentType(mmap.getContentType(storepath + "/" + url + ".csv"));
            response.setContentLength((int) fp.length());
            response.setHeader("Content-Disposition", request.getParameter("dtype") + "; filename=\"" + document.getDocname() + "\";");
            response.getOutputStream().write(buff);
            response.getOutputStream().flush();
            response.getOutputStream().close();
            myjobj.put("success", true);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return new ModelAndView("jsonView", "model", myjobj.toString());
    }
    
    public ModelAndView getQuotationDocumentList(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        KwlReturnObject kmsgShared = null;
        String direction = "";
        String field = "" ;
        try {
            String tag = request.getParameter("tag");
            String ss = StringUtil.checkForNull(request.getParameter("ss"));
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("tag", StringUtil.checkForNull(tag));
            requestParams.put("ss", ss);
            boolean tagSearch = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("tagSearch"))) {
                requestParams.put("tagSearch", request.getParameter("tagSearch"));
                tagSearch = request.getParameter("tagSearch").equalsIgnoreCase("true");
            }
            String start = StringUtil.checkForNull(request.getParameter("start"));
            String limit = StringUtil.checkForNull(request.getParameter("limit"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("quotationID"))) {
                requestParams.put("quotationid", request.getParameter("quotationID"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleid"))) {
                requestParams.put("moduleid", request.getParameter("moduleid"));
            }
            
            kmsg = accDetailsDao.getQuotationDocumentList(requestParams);
            JSONArray jarr = getProductDocumentListJson(kmsg.getEntityList(), request);
            kmsg = accDetailsDao.getTransactionDocuments(requestParams);
            List<Object[]> transactiondoclist=kmsg.getEntityList();
            jarr = getTransactionDocumentListJson(transactiondoclist, jarr);
            JSONArray pagedJson = jarr;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.start)) && !StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) {
                pagedJson = StringUtil.getPagedJSON(jarr, Integer.parseInt(request.getParameter(Constants.start)), Integer.parseInt(request.getParameter(Constants.limit)));
            }
            jobj.put("success", true);
            jobj.put("data", pagedJson);
            jobj.put("totalCount", jarr.length());
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getProductDocumentListJson(List ll, HttpServletRequest request) {
        JSONArray jarr = new JSONArray();
        try {
            DateFormat dateFormat = authHandler.getDateOnlyFormat(request);
            for (int i = 0; i < ll.size(); i++) {
                try {
                    JSONObject temp = new JSONObject();
                    if (ll.get(i) != null) {
                        Docs cd = (Docs)ll.get(i);
                        temp.put("docid", cd.getId());
                        temp.put("name", cd.getDocname());
                        temp.put("size", cd.getDocsize());
                        temp.put("uploadeddate", cd.getUploadedon()!= null ? dateFormat.format(cd.getUploadedon()) : "");
                        temp.put("uploadedOn", cd.getUploadedon());
                        temp.put("docname", cd.getDocname());
                        temp.put("docsize", cd.getDocsize());
                        temp.put("docSizeString", StringUtil.sizeRenderer(cd.getDocsize()));
                        
                        if (cd.getUser() != null) {
                            temp.put("uploadername", cd.getUser().getFullName());
                            temp.put("userid", cd.getUser().getUserID());
                            temp.put("uploadedBy", cd.getUser().getUserID());
                            temp.put("uploadedById", cd.getUser().getUserID());
                        }
                    }
                    jarr.put(temp);
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return jarr;
    }
    /*
        Function to get JSONArray of documents attached to transactions
    */
    public JSONArray getTransactionDocumentListJson(List<Object[]> ll, JSONArray jarr) {
        try {
            for (Object obj[]:ll) {
                    JSONObject temp = new JSONObject();
                    if (obj != null) {
                        temp.put("docid", obj[0]);
                        temp.put("name", obj[1]);
                        temp.put("docname", obj[1]);
                        temp.put("size", "");
                        temp.put("uploadeddate", "");
                        temp.put("uploadername", "-");
                    }
                    jarr.put(temp);
            }
        } catch(JSONException je){
            logger.warn(je.getMessage(), je);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return jarr;
    }
   
     /*
     *  Delete Shared documents & highlight to CRM
     */
    public ModelAndView deleteSharedDocuments(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        //Session session=null;
        String auditAction = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            String cdomain = URLUtil.getDomainName(request);
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("cdomain", StringUtil.isNullOrEmpty(cdomain) ? "" : cdomain);

            String action = "225";
            //Delete Shared record
            String docid = request.getParameter("docid");
            KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Docs.class.getName(), docid);
            Docs docs = (Docs) cap.getEntityList().get(0);
            String docname = docs.getDocname();
            String recid = docs.getRecordId();
            JSONObject pjobj = new JSONObject();
            pjobj.put("userid", sessionHandlerImpl.getUserid(request));
            pjobj.put("docid", docid);
            pjobj.put("docname", docname);
            pjobj.put("recid", recid);
            boolean isdeleted = accDetailsDao.deleteSharedDocuments(docid);
            if (isdeleted) {
                auditTrailObj.insertAuditLog(auditAction, sessionHandlerImpl.getUserFullName(request) + " deleted document '" + docname +"' from Accounting.", request, docid, java.util.UUID.randomUUID().toString());
                issuccess = true;
                msg = "Document deleted successfully from Deskera Accounting.";
            }
            pjobj.put("deleteflag", isdeleted);
            userData.put("docdata", pjobj);
            
            String crmURL = URLUtil.buildRestURL("crmURL");
            String endpoint = crmURL + "master/syncdocument";
            JSONObject resObj = apiCallHandlerService.restDeleteMethod(endpoint, userData.toString());
//            JSONObject resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                issuccess = resObj.getBoolean("success");
                msg = resObj.getString("msg");
                jobj.put("success", true);
                jobj.put("msg", msg);
                jobj.put("companyexist", resObj.optBoolean("companyexist"));
            }
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            throw ServiceException.FAILURE("accDocumentDetailsController.deleteSharedDocuments", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDocumentDetailsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
