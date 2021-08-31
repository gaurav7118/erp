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

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.ON_APPROVAL_EMAIL;
import static com.krawler.common.util.Constants.ON_REJECTION_EMAIL;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccMailNotificationController extends MultiActionController implements MessageSourceAware{

    private AccMailNotificationDAO accMailNotificationDAOObj;
    private HibernateTransactionManager txnManager;
    private AccCommonTablesDAO accCommonTablesDAO;
    private auditTrailDAO auditTrailObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private MessageSource messageSource;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    } 
    
    public void setaccMailNotificationDAOObj(AccMailNotificationDAO accMailNotificationDAO) {
        this.accMailNotificationDAOObj = accMailNotificationDAO;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public ModelAndView getMailNotificationData(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        KwlReturnObject fieldParamResult = null;
        boolean issuccess = false;
        try {
            JSONArray jSONArray = new JSONArray();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("companyid", companyid);
            result = accMailNotificationDAOObj.getMailNotifications(requestParams);
            int count = result.getRecordTotalCount();
             List<NotificationRules> list = result.getEntityList();
            for (NotificationRules nr : list) {
                JSONObject job = new JSONObject();
                String usersName = "";
                job.put("id", nr.getID());
                job.put("module", nr.getModuleId());
                String fieldId = nr.getFieldid();
                String dependantOn = "";
                boolean islineitem = false;
                if (fieldId.equals("1")) {
                    if (nr.getModuleId() == Constants.Acc_Invoice_ModuleId) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.dueDate)).optString("fieldlabel", "");
                    } else if (nr.getModuleId() == Constants.Acc_Vendor_Invoice_ModuleId) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.dueDate)).optString("fieldlabel", "");
                    } else if (nr.getModuleId() == Constants.Acc_GENERAL_LEDGER_ModuleId) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.JE_Date)).optString("fieldlabel", "");
                    }
                } else if (nr.getModuleId() == Constants.Acc_Sales_Order_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.SO_Date)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.SO_Date)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Acc_Sales_Order_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ON_APPROVAL_EMAIL)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ON_APPROVAL_EMAIL)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Acc_Sales_Order_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ON_REJECTION_EMAIL)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ON_REJECTION_EMAIL)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Acc_Purchase_Order_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.PO_Date)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.PO_Date)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Acc_Delivery_Order_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.DO_Date)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.DO_Date)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Acc_Goods_Receipt_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.GRO_Date)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.GRO_Date)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Acc_Goods_Receipt_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.GR_DO_Sr_Check_Date)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.GR_DO_Sr_Check_Date)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Acc_Sales_Return_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.SR_Date)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.SR_Date)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Acc_Purchase_Return_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.PR_Date)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.PR_Date)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Acc_Customer_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.CUST_CREATION_Date)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.CUST_CREATION_Date)).optString("fieldlabel", "");
                }else if (nr.getModuleId() == Constants.Acc_Vendor_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report)).optString("fieldlabel", "");
                }
                else if (nr.getModuleId() == Constants.Acc_Vendor_ModuleId) {
                    if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.VEND_CREATION_Date)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.VEND_CREATION_Date)).optString("fieldlabel", "");
                    } else if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.VEND_Self_Billed_Approval_Expiry_Date)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.VEND_Self_Billed_Approval_Expiry_Date)).optString("fieldlabel", "");
                    }
                }

                else if (nr.getModuleId() == Constants.CONSIGNMENT_SALES_MODULE) {
                    if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_Request_Creation)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_Request_Creation)).optString("fieldlabel", "");
                    } else if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_Request_Edition)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_Request_Edition)).optString("fieldlabel", "");
                    } else if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_Request_Approval)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_Request_Approval)).optString("fieldlabel", "");
                    } else if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_DO_Creation)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_DO_Creation)).optString("fieldlabel", "");
                    } else if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_Return_Creation)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_Return_Creation)).optString("fieldlabel", "");
                    }else if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_DueDate_Passed)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_DueDate_Passed)).optString("fieldlabel", "");
                    }
                } else if (nr.getModuleId() == Constants.CONSIGNMENT_PURCHASE_MODULE) {
                    if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_Request_Creation)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_Request_Creation)).optString("fieldlabel", "");
                    } else if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_Request_Edition)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_Request_Edition)).optString("fieldlabel", "");
                    } else if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_GR_Creation)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_GR_Creation)).optString("fieldlabel", "");
                    } else if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_Invoice_Creation)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_Invoice_Creation)).optString("fieldlabel", "");
                    } else if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_Return_Creation)).optString("fieldid", ""))) {
                        dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_Return_Creation)).optString("fieldlabel", "");
                    }
                }
                
                else if (nr.getModuleId() == Constants.Acc_Contract_Order_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.CONTRACT_EXPIRY_DATE)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.CONTRACT_EXPIRY_DATE)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Asset_Maintenance_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.Asset_Schedule_Start_Date)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Asset_Schedule_Start_Date)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Asset_Maintenance_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.Asset_Schedule_End_Date)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Asset_Schedule_End_Date)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Acc_Product_Master_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_Purchase_Date)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_Purchase_Date)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Acc_Product_Master_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_Expiry_Date)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_Expiry_Date)).optString("fieldlabel", "");
                } else if (nr.getModuleId() == Constants.Acc_Product_Master_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_QA_Inspection_Rejection)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_QA_Inspection_Rejection)).optString("fieldlabel", "");
                }else if (nr.getModuleId() == Constants.Acc_Product_Master_ModuleId && fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_QA_Inspection_Approval)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_QA_Inspection_Approval)).optString("fieldlabel", "");
                }else if (fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report)).optString("fieldid", ""))) {
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report)).optString("fieldlabel", "");
                } else if(fieldId.equals(new JSONObject(Constants.staticGlobalDateFields.get(Constants.Invoice_Date)).optString("fieldid", ""))){
                    dependantOn = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Invoice_Date)).optString("fieldlabel", "");
                } else {
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.Acc_id));
                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, nr.getModuleId(), nr.getFieldid()));
                    fieldParamResult = accCommonTablesDAO.getFieldParams(requestParams);
                    List<FieldParams> fields = fieldParamResult.getEntityList();
                    for (FieldParams field : fields) {
                        dependantOn = field.getFieldlabel();
                        islineitem = field.getCustomcolumn() == 1 ? true : false;
                    }

                }
                job.put("fieldid", nr.getFieldid());
                job.put("fieldname", dependantOn);
                job.put("modulename", Constants.moduleID_NameMap.get(nr.getModuleId()));
                job.put("beforeafter", nr.getBeforeafter());
                job.put("days", nr.getDays());
                job.put("emailids", nr.getEmailids());
                job.put("senderid", nr.getSenderid());
                job.put("mailsubject", nr.getMailsubject());
                job.put("mailcontent", nr.getMailcontent());
                job.put("islineitem", islineitem);
                job.put("isMailToSalesPerson", nr.isMailToSalesPerson());
                job.put("isMailToContactPerson", nr.isMailToContactPerson());
                job.put("ismailtoshippingemail", nr.isMailtoshippingemail());
                job.put("isMailToStoreManager", nr.isMailToStoreManager());
                job.put("mailToCreator", nr.isMailToCreator());
                job.put("mailtoassignedpersons", nr.isMailToAssignedTo());
                job.put("recurringruleid", nr.getRecurringDetail()!=null?nr.getRecurringDetail().getID():"");
                job.put("templateid", nr.getTemplateid()!=null?nr.getTemplateid():"");
                job.put("hyperlinkText", nr.getHyperlinkText()!=null?nr.getHyperlinkText():"");
                if (!StringUtil.isNullOrEmpty(nr.getUsers())) {
                    String[] users = nr.getUsers().split(",");
                    usersName = accMailNotificationDAOObj.getUsersFullName(users);
                    job.put("userids", nr.getUsers());
                }
                job.put("users", usersName);

                jSONArray.put(job);
            }
            jobj.put("data", jSONArray);
            jobj.put("count", count);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccMailNotificationController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveNotificationRules : " + ex.getMessage(), ex);
        } finally {
            jobj.put("success", issuccess);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveMailNotification(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, duplicate = false;
        KwlReturnObject result = null;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BRecnl_Tx");
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String fieldid = request.getParameter("fieldid");

            int module = Integer.parseInt(request.getParameter("modules"));
            int days = 0;
            int beforeAfter = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("days"))) {
                days = Integer.parseInt(request.getParameter("days"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("beforeAfter"))) {
                beforeAfter = Integer.parseInt(request.getParameter("beforeAfter"));
            }
            duplicate = getMailNotificationDuplicate(module, beforeAfter, companyid, days, fieldid);
            if (duplicate ) {
                if (fieldid.equals(Constants.Email_Button_From_Report_fieldid) || fieldid.equals(Constants.APPROVAL_EMAIL) ||  fieldid.equals(Constants.REJECTION_EMAIL)  ) {
                    result = saveMailNotification(request);
                    if (result.isSuccessFlag()) {
                        jobj.put("msg", messageSource.getMessage("acc.field.mailnotificationrulesavedsuccessfully", null, RequestContextUtils.getLocale(request)));
                        issuccess = result.isSuccessFlag();
                    }
                } else {
                    jobj.put("msg", messageSource.getMessage("acc.field.Rulealreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
            } else {
                result = saveMailNotification(request);
                if (result.isSuccessFlag()) {
                    jobj.put("msg", messageSource.getMessage("acc.field.mailnotificationrulesavedsuccessfully", null, RequestContextUtils.getLocale(request)));
                    issuccess = result.isSuccessFlag();
                }
            }
            String modulename = request.getParameter("modulename");
            auditTrailObj.insertAuditLog(AuditAction.EMAIL_NOTIFICATION_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has added Email notification for " + modulename, request, "0");
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            jobj.put("msg", ex.getMessage());
            Logger.getLogger(AccMailNotificationController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveNotificationRules : " + ex.getMessage(), ex);
        } finally {
            jobj.put("duplicate", duplicate);
            jobj.put("success", issuccess);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public KwlReturnObject saveMailNotification(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        try {
            int days=0;
            int beforeafter=0;
            String mailbodyjson="",mailbodysqlquery="",mailsubjectsqlquery="",mailsubjectjson="";
            String id = StringUtil.generateUUID();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            int module = Integer.parseInt(request.getParameter("modules"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("beforeAfter"))) {
                beforeafter = Integer.parseInt(request.getParameter("beforeAfter"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("days"))) {
                days = Integer.parseInt(request.getParameter("days"));
            }
            String fieldid = request.getParameter("fieldid");
            String emailids = request.getParameter("emailids");
            String mailcontent = request.getParameter("mailcontent");
            String mailsubject = request.getParameter("mailsubject");
            String users = request.getParameter("users");
            String templateid = request.getParameter("templateid");
            HashMap<String, Object> notificationRulesMap = new HashMap<String, Object>();
            if (fieldid.equals(Constants.Email_Button_From_Report_fieldid) || fieldid.equals(Constants.APPROVAL_EMAIL) || fieldid.equals(Constants.REJECTION_EMAIL) ) {
                notificationRulesMap.put("ID", request.getParameter("id"));
            } else {
            notificationRulesMap.put("ID", id);
            }
            notificationRulesMap.put("companyid", companyid);
            notificationRulesMap.put("moduleid", module);
            notificationRulesMap.put("beforeafter", beforeafter);
            notificationRulesMap.put("days", days);
            notificationRulesMap.put("users", users);
            notificationRulesMap.put("fieldid", fieldid);
            notificationRulesMap.put("emailids", emailids);
            notificationRulesMap.put("mailcontent", mailcontent);
            notificationRulesMap.put("mailsubject", mailsubject);
            notificationRulesMap.put("templateid", templateid);
            
            if (fieldid.equals(Constants.Email_Button_From_Report_fieldid) && (module == Constants.Acc_Purchase_Order_ModuleId || module == Constants.Acc_Sales_Order_ModuleId || module == Constants.Acc_Make_Payment_ModuleId
                    || module == Constants.Acc_Receive_Payment_ModuleId ||module == Constants.Acc_Invoice_ModuleId || module == Constants.Acc_Vendor_Invoice_ModuleId
                    || module == Constants.Acc_Delivery_Order_ModuleId || module == Constants.Acc_Goods_Receipt_ModuleId || module == Constants.Acc_Sales_Return_ModuleId || module == Constants.Acc_Purchase_Return_ModuleId 
                    || module == Constants.Acc_Customer_Quotation_ModuleId || module == Constants.Acc_Vendor_Quotation_ModuleId ||module == Constants.Acc_Purchase_Requisition_ModuleId)) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("mailsubjectjson"))) {
                    mailsubjectjson = request.getParameter("mailsubjectjson");//Mail Subject Json
                    if (module == Constants.Acc_Purchase_Order_ModuleId) {
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "purchaseorder");
                    } else if (module == Constants.Acc_Sales_Order_ModuleId) {
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "salesorder");
                    }else if (module == Constants.Acc_Make_Payment_ModuleId) {//Mail Subject Query for Make Payment
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "payment");
                    }else if (module == Constants.Acc_Receive_Payment_ModuleId) {//Mail Subject Query for Receipt Payment
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "receipt");
                    }else if (module == Constants.Acc_Invoice_ModuleId) {//Mail Subject Query for Sales Invoice
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "invoice");
                    }else if (module == Constants.Acc_Vendor_Invoice_ModuleId) {//Mail Subject Query for Purchase Invoice
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "goodsreceipt");
                    }else if (module == Constants.Acc_Delivery_Order_ModuleId) {//Mail Subject Query for Delivery order
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "deliveryorder");
                    }else if (module == Constants.Acc_Goods_Receipt_ModuleId) {//Mail Subject Query for Goods Receipt Order
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "grorder");
                    }else if (module == Constants.Acc_Sales_Return_ModuleId) {//Mail Subject Query for Sales Return
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "salesreturn");
                    }else if (module == Constants.Acc_Purchase_Return_ModuleId) {//Mail Subject Query for Purchase Return
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "purchasereturn");
                    }else if (module == Constants.Acc_Customer_Quotation_ModuleId) {//Mail Subject Query for Customer Quotation
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "quotation");
                    }else if (module == Constants.Acc_Vendor_Quotation_ModuleId) {//Mail Subject Query for Vendor Quotation
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "vendorquotation");
                    }else if (module == Constants.Acc_Purchase_Requisition_ModuleId){//Mail Subject Query for Purchase Requisition
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "purchaserequisition");
                    }
                    notificationRulesMap.put("mailsubjectsqlquery", mailsubjectsqlquery);
                    notificationRulesMap.put("mailsubjectjson", mailsubjectjson);
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("mailbodyjson"))) {
                    mailbodyjson = request.getParameter("mailbodyjson");  //Mail body json
                    if (module == Constants.Acc_Purchase_Order_ModuleId) {
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "purchaseorder");
                    } else if (module == Constants.Acc_Sales_Order_ModuleId) {
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "salesorder");
                    }else if (module == Constants.Acc_Make_Payment_ModuleId) {//Mail body Query for Make Payment
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "payment");
                    }else if (module == Constants.Acc_Receive_Payment_ModuleId) {//Mail body Query for Receipt Payment
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "receipt");
                    }else if (module == Constants.Acc_Invoice_ModuleId) {//Mail body Query for Sales Invoice
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "invoice");
                    }else if (module == Constants.Acc_Vendor_Invoice_ModuleId) {//Mail body Query for Purchase Invoice
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "goodsreceipt");
                    }else if (module == Constants.Acc_Delivery_Order_ModuleId) {//Mail body Query for Delivery Order
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "deliveryorder");
                    }else if (module == Constants.Acc_Goods_Receipt_ModuleId) {//Mail body Query for Goods Receipt Order
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "grorder");
                    }else if (module == Constants.Acc_Sales_Return_ModuleId) {//Mail body Query for Sales Return
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "salesreturn");
                    }else if (module == Constants.Acc_Purchase_Return_ModuleId) {//Mail body Query for Purchase Return
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "purchasereturn");
                    }else if (module == Constants.Acc_Customer_Quotation_ModuleId) {//Mail body Query for Customer Quotation
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "quotation");
                    }else if (module == Constants.Acc_Vendor_Quotation_ModuleId) {//Mail body Query for Vendor Quotation
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "vendorquotation");
                    }else if (module == Constants.Acc_Purchase_Requisition_ModuleId){//Mail Subject Query for Purchase Requisition
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "purchaserequisition");
                    }
                    notificationRulesMap.put("mailbodysqlquery", mailbodysqlquery);
                    notificationRulesMap.put("mailbodyjson", mailbodyjson);
                }
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("recurringruleid"))) {
                notificationRulesMap.put("recurringruleid", request.getParameter("recurringruleid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isMailToSalesPerson"))) {
                notificationRulesMap.put("isMailToSalesPerson", Boolean.parseBoolean(request.getParameter("isMailToSalesPerson")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isMailToContactPerson"))) {
                notificationRulesMap.put("isMailToContactPerson", Boolean.parseBoolean(request.getParameter("isMailToContactPerson")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isMailToStoreManager"))) {
                notificationRulesMap.put("isMailToStoreManager", Boolean.parseBoolean(request.getParameter("isMailToStoreManager")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("ismailtoassignedperson"))) {
                notificationRulesMap.put("ismailtoassignedperson", Boolean.parseBoolean(request.getParameter("ismailtoassignedperson")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isSendMailToCreator"))) {
                notificationRulesMap.put("isSendMailToCreator", Boolean.parseBoolean(request.getParameter("isSendMailToCreator")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isSendMailToAssignee"))) {
                notificationRulesMap.put("isSendMailToAssignee", Boolean.parseBoolean(request.getParameter("isSendMailToAssignee")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isMailToShippingEmail"))) {
                notificationRulesMap.put("isMailToShippingEmail", Boolean.parseBoolean(request.getParameter("isMailToShippingEmail")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("hyperlinkText"))) {
                notificationRulesMap.put("hyperlinkText", request.getParameter("hyperlinkText"));
            }
            result = accMailNotificationDAOObj.saveMailNotification(notificationRulesMap);
        } catch (Exception ex) {
            Logger.getLogger(AccMailNotificationController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return result;
    }

    private boolean getMailNotificationDuplicate(int module, int beforeAfter, String companyid, int days, String fieldid) {
        boolean duplicate = false;
        try {
            List<NotificationRules> ll = new ArrayList();
            KwlReturnObject mailNotification = null;
            int tempmodule, tempdays, tempbeforeAfter;
            mailNotification = accMailNotificationDAOObj.getMailNotification(companyid, fieldid);
            ll = mailNotification.getEntityList();
            for (NotificationRules nr : ll) {
                tempmodule = nr.getModuleId();
                tempbeforeAfter = nr.getBeforeafter();
                tempdays = nr.getDays();
                if (tempmodule == module && beforeAfter == tempbeforeAfter && tempdays == days) {
                    duplicate = true;
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccMailNotificationController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return duplicate;
    }

    public ModelAndView editMailNotificationData(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        int count = 0,moduleid=0;
        KwlReturnObject result = null;
        String mailbodyjson="",mailbodysqlquery="",mailsubjectsqlquery="",mailsubjectjson="",fieldid ="";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);

        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("companyid", companyid);
            boolean repeatNotification=false;
            String ruleid=StringUtil.isNullOrEmpty(request.getParameter("id"))?"":request.getParameter("id");
            String recurringruleid="";
            if (!StringUtil.isNullOrEmpty(request.getParameter("repeatNotification"))) {
                repeatNotification= Boolean.parseBoolean(request.getParameter("repeatNotification").toString());
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("recurringruleid"))) {
                recurringruleid=request.getParameter("recurringruleid");
                if(repeatNotification){ //when user selected recurring 
                     map.put("recurringruleid", recurringruleid);
                } else {//when user deselected recurring 
                    //first updating Notificationruletable column recurringdetails to null and the deleteing recurring rule
                     accMailNotificationDAOObj.updateMailNotificationRecurringDetail(recurringruleid,ruleid);
                     accMailNotificationDAOObj.deleteRecurringMailDetails(companyid,recurringruleid);
                }
            }
            map.put("ID", ruleid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("modules"))) {
                map.put("module", Integer.parseInt(request.getParameter("modules")));
                moduleid=Integer.parseInt(request.getParameter("modules"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("beforeAfter"))) {
                map.put("beforeAfter", Integer.parseInt(request.getParameter("beforeAfter")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("days"))) {
                map.put("days", Integer.parseInt(request.getParameter("days")));
            }
            if (request.getParameter("users")!=null) {
                map.put("users", request.getParameter("users"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("fieldid"))) {
                map.put("fieldid", request.getParameter("fieldid"));
                fieldid = request.getParameter("fieldid");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("templateid"))) {
                map.put("templateid", request.getParameter("templateid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("emailids"))) {
                map.put("emailids", request.getParameter("emailids"));
            }else{
                map.put("emailids", "");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("senderid"))) {
                map.put("senderid", request.getParameter("senderid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("mailcontent"))) {
                map.put("mailcontent", request.getParameter("mailcontent"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("mailsubject"))) {
                map.put("mailsubject", request.getParameter("mailsubject"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isMailToSalesPerson"))) {
                map.put("isMailToSalesPerson", Boolean.parseBoolean(request.getParameter("isMailToSalesPerson")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isMailToStoreManager"))) {
                map.put("isMailToStoreManager", Boolean.parseBoolean(request.getParameter("isMailToStoreManager")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isSendMailToCreator"))) {
                map.put("isSendMailToCreator", Boolean.parseBoolean(request.getParameter("isSendMailToCreator")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isSendMailToAssignee"))) {
                map.put("isSendMailToAssignee", Boolean.parseBoolean(request.getParameter("isSendMailToAssignee")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isMailToShippingEmail"))) {
                map.put("isMailToShippingEmail", Boolean.parseBoolean(request.getParameter("isMailToShippingEmail")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isMailToContactPerson"))) {
                map.put("isMailToContactPerson", Boolean.parseBoolean(request.getParameter("isMailToContactPerson")));
            }
            map.put("hyperlinkText", !StringUtil.isNullOrEmpty(request.getParameter("hyperlinkText")) ? request.getParameter("hyperlinkText") : "");
            
            if (fieldid.equals(Constants.Email_Button_From_Report_fieldid) ) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("mailsubjectjson"))) {
                    mailsubjectjson = request.getParameter("mailsubjectjson");
                    if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "purchaseorder");
                    } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "salesorder");
                    } else if (moduleid == Constants.Acc_Make_Payment_ModuleId) {//Mail Subject Query for Make Payment
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "payment");
                    } else if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {//Mail Subject Query for Receipt Payment
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "receipt");
                    }else if (moduleid == Constants.Acc_Invoice_ModuleId) {//Mail Subject Query for Sales Invoice
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "invoice");
                    }else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {//Mail Subject Query for Purchase Invoice
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "goodsreceipt");
                    }else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {//Mail Subject Query for Delivery Order
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "deliveryorder");
                    }else if (moduleid == Constants.Acc_Goods_Receipt_ModuleId) {//Mail Subject Query for Goods Receipt Order
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "grorder");
                    }else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {//Mail Subject Query for Sales Return
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "salesreturn");
                    }else if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {//Mail Subject Query for Purchase Return
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "purchasereturn");
                    }else if (moduleid == Constants.Acc_Customer_Quotation_ModuleId) {//Mail Subject Query for Customer Quotation
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "quotation");
                    }else if (moduleid == Constants.Acc_Vendor_Quotation_ModuleId) {//Mail Subject Query for Vendor Quotation
                        mailsubjectsqlquery = CustomDesignHandler.buildSqlQuery(mailsubjectjson, "vendorquotation");
                    }
                    map.put("mailsubjectsqlquery", mailsubjectsqlquery);
                    map.put("mailsubjectjson", mailsubjectjson);
                }

                if (!StringUtil.isNullOrEmpty(request.getParameter("mailbodyjson"))) {
                    mailbodyjson = request.getParameter("mailbodyjson");
                    if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "purchaseorder");
                    } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "salesorder");
                    }else if (moduleid == Constants.Acc_Make_Payment_ModuleId) {//Mail body query for Make Payment
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "payment");
                    }else if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {//Mail body query for Receipt Payment
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "receipt");
                    }else if (moduleid == Constants.Acc_Invoice_ModuleId) {//Mail body query for Sales Invoice
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "invoice");
                    }else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {//Mail body query for Purchase Invoice
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "goodsreceipt");
                    }else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {//Mail body query for Delivery Order
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "deliveryorder");
                    }else if (moduleid == Constants.Acc_Goods_Receipt_ModuleId) {//Mail body query for Goods Receipt Order
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "grorder");
                    }else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {//Mail body query for Sales Return
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "salesreturn");
                    }else if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {//Mail body query for Purchase Return
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "purchasereturn");
                    }else if (moduleid == Constants.Acc_Customer_Quotation_ModuleId) {//Mail body query for Customer Quotation
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "quotation");
                    }else if (moduleid == Constants.Acc_Vendor_Quotation_ModuleId) {//Mail body query for Vendor Quotation
                        mailbodysqlquery = CustomDesignHandler.buildSqlQuery(mailbodyjson, "vendorquotation");
                    }
                    map.put("mailbodysqlquery", mailbodysqlquery);
                    map.put("mailbodyjson", mailbodyjson);
                }
            }
            result = accMailNotificationDAOObj.editMailNotification(map);
            count = result.getRecordTotalCount();
            issuccess = result.isSuccessFlag();
            msg = result.getMsg();
            String modulename = request.getParameter("modulename");
            auditTrailObj.insertAuditLog(AuditAction.EMAIL_NOTIFICATION_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated Email notification for " + modulename, request, "0");
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } finally {
            jobj.put("success", issuccess);
            jobj.put("count", count);
            jobj.put("msg", msg);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteMailNotificationData(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        int count = 0;
        KwlReturnObject result = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String id = request.getParameter("id");
            result = accMailNotificationDAOObj.deleteMailNotification(companyid, id);
            issuccess = result.isSuccessFlag();
            msg = result.getMsg();
            String modulename = request.getParameter("modulename");
            auditTrailObj.insertAuditLog(AuditAction.EMAIL_NOTIFICATION_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Email notification for " + modulename, request, "0");
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            jobj.put("success", issuccess);
            jobj.put("count", count);
            jobj.put("msg", messageSource.getMessage(msg, null, RequestContextUtils.getLocale(request)));
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //populate single emailtemplate to edit in htmleditor on gridcellclick
    public ModelAndView getEmailTemplateToEdit(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String fieldid = request.getParameter("fieldid");
            Integer moduleID =Integer.parseInt(request.getParameter("moduleID"));
            KwlReturnObject kwl = accountingHandlerDAOobj.getObject("com.krawler.common.admin.Company", companyid);
            Company company = (Company) kwl.getEntityList().get(0);
            result = accMailNotificationDAOObj.getEmailTemplateToEdit(companyid,moduleID,fieldid);
            Iterator<NotificationRules> ite = result.getEntityList().iterator();
            NotificationRules nr = ite.next();
            jobj.put("id", nr.getID());
            jobj.put("moduleid", nr.getModuleId());
            jobj.put("fieldid", nr.getFieldid());
            jobj.put("subject", nr.getMailsubject());
            jobj.put("message", nr.getMailcontent());
            jobj.put("mailToCreator", nr.isMailToCreator());
            jobj.put("mailtoassignedpersons", nr.isMailToAssignedTo());

            if (!StringUtil.isNullOrEmpty(nr.getUsers())) {
                jobj.put("userids", nr.getUsers());
            }
            if (!StringUtil.isNullOrEmpty(nr.getEmailids())) {
                jobj.put("emailids", nr.getEmailids());
            }

            
            jobj.put("success", true);

        } catch (Exception ex) {
            jobj.put("msg", ex.getMessage());
            Logger.getLogger(AccMailNotificationController.class.getName()).log(Level.SEVERE, null, ex);
}
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //get email template  and replace all placeholders to send email
    public ModelAndView getEmailTemplateToSendMail(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String useidid = sessionHandlerImpl.getUserid(request);
            String fieldid = request.getParameter("fieldid");
            Integer moduleid = Integer.parseInt(request.getParameter("moduleid"));
            
            KwlReturnObject user = accountingHandlerDAOobj.getObject("com.krawler.common.admin.User", useidid);
            User userobj = (User) user.getEntityList().get(0);
            
            KwlReturnObject kwl = accountingHandlerDAOobj.getObject("com.krawler.common.admin.Company", companyid);
            Company company = (Company) kwl.getEntityList().get(0);
            result = accMailNotificationDAOObj.getEmailTemplateToEdit(companyid, moduleid, fieldid);
            jobj = replacePlaceHolders(request, company, result, moduleid);
            
            
            jobj.put("emailid", !StringUtil.isNullOrEmpty(userobj.getEmailID()) ? userobj.getEmailID() :(!StringUtil.isNullOrEmpty(company.getEmailID()) ? company.getEmailID() : ""));
        } catch (Exception ex) {
            jobj.put("msg", ex.getMessage());
            Logger.getLogger(AccMailNotificationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    
    private JSONObject replacePlaceHolders(HttpServletRequest request, Company company, KwlReturnObject result,int moduleid) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        try {
            Iterator<NotificationRules> ite = result.getEntityList().iterator();
            NotificationRules dft = ite.next();
            String emailBody=replaceCommonPlaceHolders(request,company,dft);
            
            String subject=dft.getMailsubject(); 
            String accountname = !StringUtil.isNullOrEmpty(request.getParameter("personname")) ? request.getParameter("personname") : "";
            String billid = !StringUtil.isNullOrEmpty(request.getParameter("billid")) ? request.getParameter("billid") : "";
            
            
            if (moduleid == Constants.Acc_Customer_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId) {
                subject = subject.replaceAll("#companyname#", (!StringUtil.isNullOrEmpty(company.getCompanyName()) ? company.getCompanyName() : ""));
            }
            if (moduleid == Constants.Account_Statement_ModuleId) {
                subject = subject.replaceAll("#companyname#", (!StringUtil.isNullOrEmpty(company.getCompanyName()) ? company.getCompanyName() : ""));
                subject = subject.replaceAll("#CustomerName#", accountname);
            }
            jobj.put("subject",subject);
            jobj.put("isMailToShippingEmail", dft.isMailtoshippingemail());
            jobj.put("message", emailBody);
            jobj.put("success", true);
        } catch (Exception ex) {
            Logger.getLogger(AccMailNotificationController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jobj;
    }
    
    public String replaceCommonPlaceHolders(HttpServletRequest request, Company company, NotificationRules dft) {
        String emailBody = "";
        try {
            String usersName = sessionHandlerImpl.getUserFullName(request);
            emailBody = dft.getMailcontent();
            String accountname = !StringUtil.isNullOrEmpty(request.getParameter("personname")) ? request.getParameter("personname") : "";
            String companyPhoneNo = !StringUtil.isNullOrEmpty(company.getPhoneNumber()) ? company.getPhoneNumber() : "";
            String CompanyEmailId = !StringUtil.isNullOrEmpty(company.getEmailID()) ? company.getEmailID() : "";
            String billid = !StringUtil.isNullOrEmpty(request.getParameter("billid")) ? request.getParameter("billid") : "";
            String date = !StringUtil.isNullOrEmpty(request.getParameter("date")) ? request.getParameter("date") : "";
            
            emailBody = emailBody.replaceAll("#CustomerName#", accountname); 
            emailBody = emailBody.replaceAll("#VendorName#", accountname); //ERP-13603 [SJ]
            emailBody = emailBody.replaceAll("#Vendor/CustomerName#", accountname);  //ERP-13603 [SJ]
            emailBody = emailBody.replaceAll("#Date#",date );
            emailBody = emailBody.replaceAll("#Phone#/#Email#", !StringUtil.isNullOrEmpty(companyPhoneNo) ? companyPhoneNo + " / " : CompanyEmailId);
            emailBody = emailBody.replaceAll("#FullName#", !StringUtil.isNullOrEmpty(usersName) ? usersName : "");

        } catch (Exception ex) {
            Logger.getLogger(AccMailNotificationController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return emailBody;
    }
    
    public ModelAndView saveMailNotificationRecurringDetails(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        String msg = "";
        boolean success = true;
        JSONObject obj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MNR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            HashMap<String, Object> requestParam = AccountingManager.getGlobalParams(request);
            String recurringruleid = StringUtil.isNullOrEmpty(request.getParameter("recuringruleid")) ? "" : request.getParameter("recuringruleid");
            String modulename = StringUtil.isNullOrEmpty(request.getParameter("modulename")) ? "" : request.getParameter("modulename");
            int repeatTime = StringUtil.isNullOrEmpty(request.getParameter("repeatTime")) ? 1 : Integer.parseInt(request.getParameter("repeatTime"));//default 1 day
            int repeatTimeType = StringUtil.isNullOrEmpty(request.getParameter("repeatTimeType")) ? 1 : Integer.parseInt(request.getParameter("repeatTimeType"));//defaulr day type
            int endType = StringUtil.isNullOrEmpty(request.getParameter("endType")) ? 1 : Integer.parseInt(request.getParameter("endType"));//default never end
            int endInterval = StringUtil.isNullOrEmpty(request.getParameter("endInterval")) ? 0 : Integer.parseInt(request.getParameter("endInterval"));//default end interval is zero

            requestParam.put("recurringruleid", recurringruleid);
            requestParam.put("repeatTime", repeatTime);
            requestParam.put("repeatTimeType", repeatTimeType);
            requestParam.put("endType", endType);
            requestParam.put("endInterval", endInterval);

            String ruleid = saveMailNotificationRecurringDetails(requestParam);
            if(StringUtil.isNullOrEmpty(recurringruleid)){
                auditTrailObj.insertAuditLog(AuditAction.ADD_RECURRING_MAIL_DETAIL, "User " + sessionHandlerImpl.getUserFullName(request) + " has added Email notification recurring details for "+modulename, request, ruleid);
            } else {
                auditTrailObj.insertAuditLog(AuditAction.UPDATE_RECURRING_MAIL_DETAIL, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated Email notification recurring details for " + modulename, request, ruleid);
            }
            txnManager.commit(status);
            obj.put("recurringruleid", ruleid);
        } catch (Exception ex) {
            success=false;
            txnManager.rollback(status);
            msg=ex.getMessage();
            Logger.getLogger(AccMailNotificationController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            obj.put("msg", msg);
            obj.put("success", success);
        }
        return new ModelAndView("jsonView", "model", obj.toString());
    }

    public String saveMailNotificationRecurringDetails(HashMap<String, Object> requestParam) throws ServiceException {
        String recurringruleid = "";
        KwlReturnObject result = accMailNotificationDAOObj.saveMailNotificationRecurringDetails(requestParam);
        NotifictionRulesRecurringDetail detail = (NotifictionRulesRecurringDetail) result.getEntityList().get(0);
        if (detail != null) {
            recurringruleid = detail.getID();
        }
        return recurringruleid;
    }
    
    public ModelAndView getMailNotificationRecurringDetails(HttpServletRequest request,HttpServletResponse response)throws ServiceException, JSONException{
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        boolean success=true;
        String msg="";
        try{
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String recurringruleid=request.getParameter("recurringruleid");
            result = accMailNotificationDAOObj.getMailNotificationRecurringDetail(companyid,recurringruleid);
            NotifictionRulesRecurringDetail detail=(NotifictionRulesRecurringDetail)result.getEntityList().get(0);
            if(detail!=null){
                jobj.put("id", detail.getID());
                jobj.put("repeatedTime", detail.getRepeatTime());
                jobj.put("repeatedTimeType", detail.getRepeatTimeType());
                jobj.put("endType", detail.getEndType());
                jobj.put("endInterval", detail.getEndInterval());
            }
        }catch(Exception ex){
            success=false;
            msg=ex.getMessage();
            Logger.getLogger(AccMailNotificationController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally{
            jobj.put("msg", msg);
            jobj.put("success", success);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
 }
