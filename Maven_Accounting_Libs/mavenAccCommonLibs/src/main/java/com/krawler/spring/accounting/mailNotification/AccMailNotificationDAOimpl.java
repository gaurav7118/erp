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
package com.krawler.spring.accounting.mailNotification;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.NotificationRules;
import com.krawler.common.admin.NotifictionRulesRecurringDetail;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.handler.AccountingHandlerDAOImpl;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AccMailNotificationDAOimpl extends BaseDAO implements AccMailNotificationDAO {

    @Override
    public KwlReturnObject getMailNotifications(HashMap<String, Object> reHashMap) throws ServiceException {
        int totalCount = 0;
        String companyid = "", start = "", limit = "";
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        try {

            if (reHashMap.containsKey("companyid")) {
                companyid = (String) reHashMap.get("companyid");
                params.add(companyid);
                params.add(companyid);
                params.add(companyid);
            }
            if (reHashMap.containsKey("start")) {
                start = (String) reHashMap.get("start");
            }
            if (reHashMap.containsKey("limit")) {
                limit = (String) reHashMap.get("limit");
            }

            //"from NotificationRules nr where  ((nr.company.companyID=? ) "  - returns company specific record.
            //or ((nr.moduleId not in (select t.moduleId from NotificationRules t where t.company.companyID = ? ) "
//                    + " or nr.fieldid not in ( select w.fieldid from NotificationRules w where w.company.companyID = ? )) "
//                    + " and nr.company.companyID is null)) "     - Returns default records 
//           note -if default record is edited then company specific copy will be showed (at a time inly one colpy will be showed Default/Company specific)
            String query = "from NotificationRules nr where  ((nr.company.companyID=? ) "
                    + " or ((nr.moduleId not in (select t.moduleId from NotificationRules t where t.company.companyID = ? ) "
                    + " or nr.fieldid not in ( select w.fieldid from NotificationRules w where w.company.companyID = ? )) "
                    + " and nr.company.companyID is null)) ";
//            String query = "from NotificationRules nr where nr.company.companyID=?";
            list = executeQuery(query, params.toArray());
            totalCount = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, totalCount);
        }
    }

    @Override
    public KwlReturnObject getMailNotification(String companyid, String fieldid) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from NotificationRules nr where nr.company.companyID=? and nr.fieldid=?";
            list = executeQuery(query, new String[]{companyid, fieldid});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject saveMailNotification(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            NotificationRules nr = null;
            
            if (hm.containsKey("fieldid") && hm.get("fieldid")!=null && (hm.get("fieldid").toString().equals(Constants.Email_Button_From_Report_fieldid) || hm.get("fieldid").toString().equals(Constants.APPROVAL_EMAIL) || hm.get("fieldid").toString().equals(Constants.REJECTION_EMAIL))) {

                nr = (NotificationRules) get(NotificationRules.class, (String) hm.get("ID"));
                if (nr != null && nr.getCompany() == null) {
                    nr = new NotificationRules();
                    String id = StringUtil.generateUUID();
                    nr.setID(id);
                }
            } else {
                nr = new NotificationRules();
                if (hm.containsKey("ID")) {
                    nr.setID((String) hm.get("ID"));
                }
            }
            
            if (hm.containsKey("mailsubjectsqlquery")) {
                nr.setMailsubjectsqlquery(hm.get("mailsubjectsqlquery").toString());
            }
            if (hm.containsKey("mailsubjectjson")) {
                nr.setMailsubjectjson(hm.get("mailsubjectjson").toString());
            }
            
            if (hm.containsKey("mailbodysqlquery")) {
                nr.setMailbodysqlquery(hm.get("mailbodysqlquery").toString());
            }
            if (hm.containsKey("mailbodyjson")) {
                nr.setMailbodyjson(hm.get("mailbodyjson").toString());
            }

            if (hm.containsKey("moduleid")) {
                nr.setModuleId((Integer) hm.get("moduleid"));
            }
            if (hm.containsKey("beforeafter")) {
                nr.setBeforeafter((Integer) hm.get("beforeafter"));
            }
            if (hm.containsKey("days")) {
                nr.setDays((Integer) hm.get("days"));
            }
            if (hm.containsKey("fieldid")) {
                nr.setFieldid(hm.get("fieldid").toString());
            }
            if (hm.containsKey("templateid")) {
                nr.setTemplateid(hm.get("templateid").toString());
            }
            if (hm.containsKey("emailids")) {
                nr.setEmailids(hm.get("emailids").toString());
            }
            if (hm.containsKey("mailcontent")) {
                nr.setMailcontent(hm.get("mailcontent").toString());
            }
            if (hm.containsKey("mailsubject")) {
                nr.setMailsubject(hm.get("mailsubject").toString());
            }
            if (hm.containsKey("companyid")) {
                Company company = hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid"));
                nr.setCompany(company);
            }
            if (hm.containsKey("recurringruleid") && !StringUtil.isNullOrEmpty((String)hm.get("recurringruleid"))) {
                NotifictionRulesRecurringDetail detail = (NotifictionRulesRecurringDetail) get(NotifictionRulesRecurringDetail.class, (String) hm.get("recurringruleid"));
                nr.setRecurringDetail(detail);
            }
            if (hm.containsKey("users")) {
                nr.setUsers((String) hm.get("users"));
            }
            if (hm.containsKey("isMailToSalesPerson") && hm.get("isMailToSalesPerson") != null) {
                nr.setMailToSalesPerson((Boolean) hm.get("isMailToSalesPerson"));
            }
             if (hm.containsKey("isMailToContactPerson") && hm.get("isMailToContactPerson") != null) {
                nr.setMailToContactPerson((Boolean) hm.get("isMailToContactPerson"));
            }
            if (hm.containsKey("isMailToStoreManager") && hm.get("isMailToStoreManager") != null) {
                nr.setMailToStoreManager((Boolean) hm.get("isMailToStoreManager"));
            }
            if (hm.containsKey("ismailtoassignedperson") && hm.get("ismailtoassignedperson") != null) {
                nr.setMailToAssignedTo((Boolean) hm.get("ismailtoassignedperson"));
            }
             if (hm.containsKey("isSendMailToCreator") && hm.get("isSendMailToCreator") != null) {
                nr.setMailToCreator((Boolean) hm.get("isSendMailToCreator"));
            }
              if (hm.containsKey("isSendMailToAssignee") && hm.get("isSendMailToAssignee") != null) {
                nr.setMailToAssignedTo((Boolean) hm.get("isSendMailToAssignee"));
            }
            if (hm.containsKey("isMailToShippingEmail") && hm.get("isMailToShippingEmail") != null) {
                nr.setMailtoshippingemail((Boolean) hm.get("isMailToShippingEmail"));
            }
            if (hm.containsKey("hyperlinkText") && hm.get("hyperlinkText") != null) {
                nr.setHyperlinkText(hm.get("hyperlinkText").toString());
            }
            save(nr);
            list.add(nr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccMailNotificationDAOimpl.saveMailNotification : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Mail notification rule has been saved successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject editMailNotification(HashMap<String, Object> map) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = (String) map.get("ID");
            NotificationRules nr = (NotificationRules) get(NotificationRules.class, id);
            if (nr != null && nr.getCompany() == null) {
                nr = new NotificationRules();
                id = StringUtil.generateUUID();
                nr.setID(id);
                if (map.containsKey("companyid")) {
                    Company company = map.get("companyid") == null ? null : (Company) get(Company.class, (String) map.get("companyid"));
                    nr.setCompany(company);
                }
            }
//            NotificationRules nr = (NotificationRules) get(NotificationRules.class, id);
            if (map.containsKey("module")) {
                nr.setModuleId((Integer) map.get("module"));
            }
            if (map.containsKey("beforeAfter")) {
                nr.setBeforeafter((Integer) map.get("beforeAfter"));
            }
            if (map.containsKey("days")) {
                nr.setDays((Integer) map.get("days"));
            }
            if (map.containsKey("users")) {
                nr.setUsers((String) map.get("users"));
            }
            if (map.containsKey("fieldid")) {
                nr.setFieldid(map.get("fieldid").toString());
            }
            if (map.containsKey("templateid")) {
                nr.setTemplateid(map.get("templateid").toString());
            }
            if (map.containsKey("emailids")) {
                nr.setEmailids((String)map.get("emailids"));
            }
            if (map.containsKey("senderid")) {
                nr.setSenderid(map.get("senderid").toString());
            }
            if (map.containsKey("mailcontent")) {
                nr.setMailcontent(map.get("mailcontent").toString());
            }
            if (map.containsKey("mailsubject")) {
                nr.setMailsubject(map.get("mailsubject").toString());
            }
            if (map.containsKey("isMailToSalesPerson") && map.get("isMailToSalesPerson") != null) {
                nr.setMailToSalesPerson((Boolean) map.get("isMailToSalesPerson"));
            }
            if (map.containsKey("isMailToStoreManager") && map.get("isMailToStoreManager") != null) {
                nr.setMailToStoreManager((Boolean) map.get("isMailToStoreManager"));
            }
             if (map.containsKey("isSendMailToCreator") && map.get("isSendMailToCreator") != null) {
                nr.setMailToCreator((Boolean) map.get("isSendMailToCreator"));
            }
            if (map.containsKey("isSendMailToAssignee") && map.get("isSendMailToAssignee") != null) {
                nr.setMailToAssignedTo((Boolean) map.get("isSendMailToAssignee"));
            }
            if (map.containsKey("recurringruleid") && !StringUtil.isNullOrEmpty((String) map.get("recurringruleid"))) {
                NotifictionRulesRecurringDetail detials = (NotifictionRulesRecurringDetail) get(NotifictionRulesRecurringDetail.class, (String) map.get("recurringruleid"));
                nr.setRecurringDetail(detials);
            }

            if (map.containsKey("mailsubjectsqlquery")) {
                nr.setMailsubjectsqlquery(map.get("mailsubjectsqlquery").toString());
            }
            if (map.containsKey("mailsubjectjson")) {
                nr.setMailsubjectjson(map.get("mailsubjectjson").toString());
            }

            if (map.containsKey("mailbodysqlquery")) {
                nr.setMailbodysqlquery(map.get("mailbodysqlquery").toString());
            }
            if (map.containsKey("mailbodyjson")) {
                nr.setMailbodyjson(map.get("mailbodyjson").toString());
            }
            if (map.containsKey("isMailToShippingEmail") && map.get("isMailToShippingEmail") != null) {
                nr.setMailtoshippingemail((Boolean) map.get("isMailToShippingEmail"));
            }
            if (map.containsKey("hyperlinkText") && map.get("hyperlinkText") != null) {
                nr.setHyperlinkText(map.get("hyperlinkText").toString());
            }
            if (map.containsKey("isMailToContactPerson") && map.get("isMailToContactPerson") != null) {
                nr.setMailToContactPerson((Boolean) map.get("isMailToContactPerson"));
            }
            saveOrUpdate(nr);
            list.add(nr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccMailNotificationDAOimpl.saveMailNotification : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Mail notification rule has been edited successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteMailNotification(String companyid, String id) throws ServiceException {
        int numRows = 0;
        String msg = null;
        try {
            String delQuery = "delete from NotificationRules nr where nr.ID=? and nr.company.companyID=?";
            numRows = executeUpdate(delQuery, new String[]{id, companyid});
            msg = "acc.field.mailnotificationruledeletedsuccessfully";
        } catch (Exception ex) {
            msg = ex.getMessage();
        }
        return new KwlReturnObject(true, msg, null, null, numRows);
    }

    @Override
    public String getUsersFullName(String[] users) throws ServiceException {
        String userName = "";
        try {
            if (users.length > 0) {
                int size = 0;
                for (int i = 0; i < users.length; i++) {
                    if (!StringUtil.isNullOrEmpty(users[i])) {
                        String userid = users[i];
                        User ur = (User) get(User.class, userid);
                        if (size == 0) {
                            userName = ur.getFirstName() + " " + ur.getLastName();
                            size++;
                        } else {
                            userName = userName + ", " + ur.getFirstName() + " " + ur.getLastName();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccMailNotificationDAOimpl.getUserFullName : " + ex.getMessage(), ex);
        }
        return userName;
    }


    @Override //to get email single wmail template to email
    public KwlReturnObject getEmailTemplateToEdit(String companyid, Integer moduleID, String fieldid) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from NotificationRules where moduleId=?  and fieldid= ? and company.companyID=?";
            list = executeQuery(query, new Object[]{moduleID, fieldid, companyid});
            if (list.isEmpty()) {
                query = "from NotificationRules where moduleId=?  and fieldid= ? and company.companyID IS NULL";
                list = executeQuery(query, new Object[]{moduleID, fieldid});
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccMailNotificationDAOimpl.getEmailTemplateToEdit : " + ex.getMessage(), ex);
        } finally {
            return new KwlReturnObject(true, "AccMailNotificationDAOimpl.getEmailTemplateToEdit", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject saveMailNotificationRecurringDetails(HashMap<String, Object> requestParam) throws ServiceException {
        List list=new ArrayList();
        try{
            NotifictionRulesRecurringDetail detail=new NotifictionRulesRecurringDetail();
            if(requestParam.containsKey("recurringruleid") && !StringUtil.isNullOrEmpty((String)requestParam.get("recurringruleid"))){
                 detail = (NotifictionRulesRecurringDetail) get(NotifictionRulesRecurringDetail.class, (String)requestParam.get("recurringruleid"));
            }
            if(requestParam.containsKey("repeatTime") && !StringUtil.isNullOrEmpty(requestParam.get("repeatTime").toString())){
                 int  interval= Integer.parseInt(requestParam.get("repeatTime").toString());
                 detail.setRepeatTime(interval);
            }
            if(requestParam.containsKey("repeatTimeType") && !StringUtil.isNullOrEmpty((String)requestParam.get("repeatTimeType").toString())){
                 int  intervalType= Integer.parseInt(requestParam.get("repeatTimeType").toString());
                 detail.setRepeatTimeType(intervalType);
            }
            if(requestParam.containsKey("endType") && !StringUtil.isNullOrEmpty((String)requestParam.get("endType").toString())){
                 int  endType= Integer.parseInt(requestParam.get("endType").toString());
                 detail.setEndType(endType);
            }
            if(requestParam.containsKey("endInterval") && !StringUtil.isNullOrEmpty((String)requestParam.get("endInterval").toString())){
                 int  endInterval= Integer.parseInt(requestParam.get("endInterval").toString());
                 detail.setEndInterval(endInterval);
            }
            if(requestParam.containsKey(Constants.companyKey)){
                 Company company = (Company) get(Company.class, (String)requestParam.get(Constants.companyKey));
                 detail.setCompany(company);
            }
            saveOrUpdate(detail);
            list.add(detail);
            
        } catch(Exception ex){
            throw ServiceException.FAILURE("AccMailNotificationDAOimpl.saveMailNotificationRecurringDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Mail notification recurring detail rule has been saved successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject getMailNotificationRecurringDetail(String companyid, String recurringruleid) throws ServiceException{
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from NotifictionRulesRecurringDetail where ID=? and company.companyID=?";
            list = executeQuery(query, new Object[]{recurringruleid, companyid});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccMailNotificationDAOimpl.getEmailTemplateToEdit : " + ex.getMessage(), ex);
        } finally {
            return new KwlReturnObject(true, "AccMailNotificationDAOimpl.getEmailTemplateToEdit", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject deleteRecurringMailDetails(String companyid, String recurringruleid) throws ServiceException{
        int numRows = 0;
        String msg = null;
        try {
            String delQuery = "delete from NotifictionRulesRecurringDetail nr where nr.ID=? and nr.company.companyID=?";
            numRows = executeUpdate(delQuery, new String[]{recurringruleid, companyid});
            msg = "Mail notification rule has been deleted successfully.";
        } catch (Exception ex) {
            msg = ex.getMessage();
        }
        return new KwlReturnObject(true, msg, null, null, numRows);
    }

    @Override
    public int updateMailNotificationRecurringDetail(String recurringruleid, String ruleid) throws ServiceException {
        String query = "update mailnotification set recurringdetail=null where id=? and recurringdetail=?";
        int numRows = executeSQLUpdate(query, new Object[]{ruleid,recurringruleid});
        return numRows;
    }
}
