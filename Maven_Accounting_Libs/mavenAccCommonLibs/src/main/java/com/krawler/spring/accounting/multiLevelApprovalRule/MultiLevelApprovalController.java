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
package com.krawler.spring.accounting.multiLevelApprovalRule;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.ProductBatch;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
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
public class MultiLevelApprovalController extends MultiActionController implements  MessageSourceAware {

    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private HibernateTransactionManager txnManager;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    
//    public ModelAndView getMultiApprovalRuleData(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        String msg = "";
//        boolean issuccess = false;
//        try {
//            JSONArray jArr = new JSONArray();
//            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String moduleid = request.getParameter("moduleid");
//            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
//            qdDataMap.put("companyid", companyid);
//            qdDataMap.put("moduleid", moduleid);
//            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
//            Iterator itr = flowresult.getEntityList().iterator();
//            while (itr.hasNext()) {
//                String creatorName="";
//                String appliedupon="";
//                Object[] row = (Object[]) itr.next();
//                JSONObject obj = new JSONObject();
//                if (row[6] != null && !row[6].toString().equals("")) {
//                    String userIdArray[] = row[6].toString().split(",");
//                    for (int i = 0; i < userIdArray.length; i++) {
//                        KwlReturnObject userobj = accountingHandlerDAOobj.getObject(User.class.getName(), userIdArray[i]);
//                        User user = (User) userobj.getEntityList().get(0);
//                        creatorName += user.getFirstName() + ",";
//                    }
//                    creatorName = creatorName.substring(0, creatorName.length() - 1);
//                }
//                if(row[5]==null){
//                    appliedupon = "-";
//                }else if(row[5].toString().equals("") || Integer.parseInt(row[5].toString())==0){
//                    if(row[2].toString().equals("")){
//                        appliedupon = "All Conditions";
//                    }else{
//                        appliedupon = "-";
//                    }
//                }else if(Integer.parseInt(row[5].toString())==Constants.Total_Amount){
//                    appliedupon = "Total Amount" ;
//                }else if(Integer.parseInt(row[5].toString())==Constants.Profit_Margin_Amount){
//                    appliedupon ="Profit Margin Amount";
//                }else if(Integer.parseInt(row[5].toString())==Constants.Specific_Products){
//                    appliedupon = "Products";
//                }else if(Integer.parseInt(row[5].toString())==Constants.Specific_Products_Discount){
//                    appliedupon = "Products Discount";
//                }else{
//                    appliedupon = "Creator";
//                }
//                String rule="";
//                if(!row[2].toString().equals("")){
//                    if(Integer.parseInt(row[5].toString())==Constants.Specific_Products || Integer.parseInt(row[5].toString())==Constants.Specific_Products_Discount){
////                        String productIds=row[2].toString();
////                        String productIdArr[] = productIds.split(",");
////                        for(int cnt=0; cnt<productIdArr.length;cnt++){
////                            
////                        }
//                        
//                        rule=row[2].toString();
//                    }else{
//                        rule=row[2].toString();
//                    }
//                }else{
//                    if(!row[6].toString().equals("")){
//                        rule="JE Created By "+creatorName;
//                    }else{
//                        rule=row[2].toString();
//                    }
//                }
//                obj.put("id", row[0]);
//                obj.put("level", row[1]);
//                obj.put("rule", rule);
//                obj.put("discountrule", row[7]==null?"":row[7].toString());
//                obj.put("appliedupon", appliedupon);  //appliedUpo= 0 for DO/GR Approval Rule , ==1 for JE approval rule applied upon total amount and ==2 for JE approval rule applied upon Creator
//                String userName = "", userId = "";
//                KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(row[0].toString());
//                Iterator useritr = userResult.getEntityList().iterator();
//                while (useritr.hasNext()) {
//                    Object[] userrow = (Object[]) useritr.next();
//                    userId += userrow[0] + ",";
//                    userName += userrow[1] + ",";
//                }
//                if (!StringUtil.isNullOrEmpty(userName)) {
//                    userName = userName.substring(0, userName.length() - 1);
//                    userId = userId.substring(0, userId.length() - 1);
//                }
//                obj.put("users", userName);
//                obj.put("userids", userId);
//                jArr.put(obj);
//            }
//            jobj.put("data", jArr);
//            issuccess = true;
//        } catch (SessionExpiredException ex) {
//            msg = ex.getMessage();
//            Logger.getLogger(MultiLevelApprovalController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ServiceException ex) {
//            msg = "MultiLevelApprovalController.getMultiApprovalRuleData:" + ex.getMessage();
//            Logger.getLogger(MultiLevelApprovalController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            msg = "MultiLevelApprovalController.getMultiApprovalRuleData:" + ex.getMessage();
//            Logger.getLogger(MultiLevelApprovalController.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//            } catch (JSONException ex) {
//                Logger.getLogger(MultiLevelApprovalController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }

    public ModelAndView saveMultiApprovalRule(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String forTransaction="";
        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String rule = "";
            String discountRule="";
            String creator="";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String approver = request.getParameter("approver");
            String isrule = request.getParameter("isrule");
            String rulelevel = request.getParameter("level");
            String moduleid = request.getParameter("moduleid");
            String productids = request.getParameter("productids");
            String ruleid = request.getParameter("ruleid");
            String productCategory = request.getParameter("productCategory");
            String deptWiseApprover = request.getParameter("deptWiseApprover");
            int appliedUpon = (!StringUtil.isNullOrEmpty(request.getParameter("appliedupon")))?Integer.parseInt(request.getParameter("appliedupon")):0;
                      
            if (!StringUtil.isNullOrEmpty(isrule) && isrule.equals("on")) {
                if (appliedUpon == 1 || appliedUpon == 3) {
                    AccountingManager.BuildAuditTrialMessage(qdDataMap, qdDataMap, appliedUpon, qdDataMap);
                    rule = getRuleValue(request.getParameter("rule"), request.getParameter("limitvalue"), request.getParameter("ulimitvalue"));
                } else if (appliedUpon == 2) {                                // If rule is applied upon 'Creator'
                    creator = (!StringUtil.isNullOrEmpty(request.getParameter("creator"))) ? request.getParameter("creator") : "";
                }else if (appliedUpon == Constants.Specific_Products_Category) {                                // If rule is applied upon 'Creator'
                    rule = !StringUtil.isNullOrEmpty(productCategory) ? productCategory : "";
                } else if (appliedUpon == 4 || appliedUpon == 5) {                                // If rule is applied upon 'Specific Product'
                    rule = !StringUtil.isNullOrEmpty(productids) ? productids : "";
                    if (appliedUpon == 5) {
                        discountRule = getRuleValue(request.getParameter("rule"), request.getParameter("limitvalue"), request.getParameter("ulimitvalue"));
                    }
                }else if (appliedUpon == Constants.SO_CREDIT_LIMIT) {           //ERM-396
                    rule = String.valueOf(Constants.SO_CREDIT_LIMIT);
                }
            }
            
            if (!StringUtil.isNullOrEmpty(deptWiseApprover) && deptWiseApprover.equals("true")) {
                qdDataMap.put("deptwiseapprover", 'T');
            } else {
                qdDataMap.put("deptwiseapprover", 'F');
            }
            qdDataMap.put("level", rulelevel);
            qdDataMap.put("rule", rule);
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("approver", approver);
            qdDataMap.put("hasapprover", !StringUtil.isNullOrEmpty(approver));
            qdDataMap.put("moduleid", moduleid);
            qdDataMap.put("creator", creator);
            qdDataMap.put("appliedupon", appliedUpon);
            qdDataMap.put("discountrule", discountRule);
            qdDataMap.put("ruleid", ruleid);
            if(!StringUtil.isNullOrEmpty(ruleid)){
                boolean ruleResult = false;
                ruleResult=accMultiLevelApprovalDAOObj.checkIfTransactionIsPendingForApproval(Integer.parseInt(moduleid),Integer.parseInt(rulelevel),companyid);
                if(ruleResult){
                    throw new AccountingException(messageSource.getMessage("acc.field.transactionExistsPendingAtlevelSocannotedit", null, RequestContextUtils.getLocale(request)));                        
                }
            }
            KwlReturnObject result = accMultiLevelApprovalDAOObj.saveMultiApprovalRule(qdDataMap);
            issuccess = true;
             if(!StringUtil.isNullOrEmpty(moduleid)) 
           {
               if((Integer.parseInt(moduleid) == Constants.Acc_Goods_Receipt_ModuleId)){
                   msg =messageSource.getMessage("acc.field.SaveGoodsReceiptApprovalRule", null, RequestContextUtils.getLocale(request));   
                   forTransaction="Goods Receipt";
               }
               else if((Integer.parseInt(moduleid) == Constants.Acc_Delivery_Order_ModuleId))
               {
                   msg =messageSource.getMessage("acc.field.SaveDeliveryOrderApprovalRule", null, RequestContextUtils.getLocale(request));
                   forTransaction="Delivery Order";
               } else if((Integer.parseInt(moduleid) == Constants.Acc_GENERAL_LEDGER_ModuleId))
               {
                   msg =messageSource.getMessage("acc.field.SaveJEApprovalRule", null, RequestContextUtils.getLocale(request));
                   forTransaction="Journal Entry";
               } else if((Integer.parseInt(moduleid) == Constants.Acc_Customer_Quotation_ModuleId)){
                   msg =messageSource.getMessage("acc.field.CustomerQuotationApprovalRulehasbeensavedSuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction="Customer Quotation";
               } else if((Integer.parseInt(moduleid) == Constants.Acc_Vendor_Quotation_ModuleId)){
                   msg =messageSource.getMessage("acc.field.VendorQuotationApprovalRulehasbeensavedSuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction="Vendor Quotation";
               } else if ((Integer.parseInt(moduleid) == Constants.Acc_Purchase_Requisition_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.RequisitionApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Purchase Requisition";
               } else if ((Integer.parseInt(moduleid) == Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.assetPurchaseRequisitionApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Asset Purchase Requisition";
               } else if ((Integer.parseInt(moduleid) == Constants.Acc_Sales_Order_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.SalesOrderApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Sales Order";
               } else if ((Integer.parseInt(moduleid) == Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.assetVendorQuotationApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Asset Vendor Quotation";
               } else if ((Integer.parseInt(moduleid) == Constants.Acc_Purchase_Order_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.PurchaseOrderApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Purchase Order";
               } else if ((Integer.parseInt(moduleid) == Constants.Acc_FixedAssets_Purchase_Order_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.AssetPurchaseOrderApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Asset Purchase Order";
               }else if ((Integer.parseInt(moduleid) == Constants.Acc_Invoice_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.InvoiceApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Sales Invoice";
               }else if ((Integer.parseInt(moduleid) == Constants.Acc_Vendor_Invoice_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.VendorInvoiceApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Purchase Invoice";
               }else if ((Integer.parseInt(moduleid) == Constants.Acc_Credit_Note_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.CreditNotyeApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Credit Note";
               }else if ((Integer.parseInt(moduleid) == Constants.Acc_Debit_Note_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.DebitNotyeApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Debit Note";
               }else if ((Integer.parseInt(moduleid) == Constants.Acc_Make_Payment_ModuleId)) {
                    msg = messageSource.getMessage("acc.field.makepaymentApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Make Payment";
               }else if ((Integer.parseInt(moduleid) == Constants.Acc_Receive_Payment_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.receivepaymentApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Receive Payment";
               }
           }
            //msg = (!StringUtil.isNullOrEmpty(moduleid) ? (Integer.parseInt(moduleid) == Constants.Acc_Goods_Receipt_ModuleId) : false) ? messageSource.getMessage("acc.field.SaveGoodsReceiptApprovalRule", null, RequestContextUtils.getLocale(request))  : messageSource.getMessage("acc.field.SaveDeliveryOrderApprovalRule", null, RequestContextUtils.getLocale(request));
             
            if (appliedUpon == Constants.Specific_Products_Category) {
                auditTrailObj.insertAuditLog(AuditAction.APPROVAL_RULE, "User " + sessionHandlerImpl.getUserFullName(request) + " has set approval rule Specific Product(s) Category with level " + rulelevel + " for " + forTransaction, request, result.getEntityList().get(0).toString());
            } else {
                //Edit case audit trail message is handled for ERP-33266
                if (StringUtil.isNullOrEmpty(ruleid)) {
                    auditTrailObj.insertAuditLog(AuditAction.APPROVAL_RULE, "User " + sessionHandlerImpl.getUserFullName(request) + " has set approval rule " + rule + " with level " + rulelevel + " for " + forTransaction, request, result.getEntityList().get(0).toString());
                } else {
                    auditTrailObj.insertAuditLog(AuditAction.APPROVAL_RULE, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated approval rule " + rule + " with level " + rulelevel + " for " + forTransaction, request, result.getEntityList().get(0).toString());
                }
            }
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(MultiLevelApprovalController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(MultiLevelApprovalController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public String getRuleValue(String ruleTypeStr,String limitValue, String ulimitValue) {
        String rule="";
        int ruleType = Integer.parseInt(ruleTypeStr);// 1 - Greater Than Equal To , 2 - Less Than Equal To, 3 - Equal to
        switch (ruleType) {
            case 1:
                rule = "$$>" + limitValue;
                break;
            case 2:
                rule = "$$<" + limitValue;
                break;
            case 3:
                rule = "$$==" + limitValue;
                break;
            case 4:
                rule = limitValue + "<=$$" + " && $$<=" + ulimitValue;
                break;
        }
        return rule;
    }

    public ModelAndView deleteMultiApprovalRule(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            String id = request.getParameter("id");
            String level = request.getParameter("level");
            String rule = request.getParameter("rule");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            qdDataMap.put("id", id);
            qdDataMap.put("companyid", companyid);
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            Iterator itr = flowresult.getEntityList().iterator();
            String moduleid = null;
            if (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                moduleid = row[4].toString();
            }
            if(!StringUtil.isNullOrEmpty(moduleid)) {
            boolean ruleResult = false;
            ruleResult=accMultiLevelApprovalDAOObj.checkIfTransactionIsPendingForApproval(Integer.parseInt(moduleid),Integer.parseInt(level),companyid);
            if(ruleResult){
                throw new AccountingException(messageSource.getMessage("acc.transactionExistsPendingAtlevel", null, RequestContextUtils.getLocale(request)));                        
            }   
            KwlReturnObject result = accMultiLevelApprovalDAOObj.deleteMultiApprovalRule(qdDataMap);
           
            issuccess = true;
            String forTransaction="";

               if((Integer.parseInt(moduleid) == Constants.Acc_Goods_Receipt_ModuleId)){
                   msg =messageSource.getMessage("acc.field.DeleteGoodsReceiptApprovalRule", null, RequestContextUtils.getLocale(request)); 
                   forTransaction ="Goods Receipt";
               } else if((Integer.parseInt(moduleid) == Constants.Acc_Delivery_Order_ModuleId))
               {
                   msg =messageSource.getMessage("acc.field.DeleteDeliveryOrderApprovalRule", null, RequestContextUtils.getLocale(request));
                   forTransaction ="Delivery Order";
               } else if((Integer.parseInt(moduleid) == Constants.Acc_GENERAL_LEDGER_ModuleId)){
                   msg =messageSource.getMessage("acc.field.DeleteJournalEntryApprovalRule", null, RequestContextUtils.getLocale(request));
                   forTransaction="Journal Entry";
               } else if((Integer.parseInt(moduleid) == Constants.Acc_Customer_Quotation_ModuleId)){
                   msg =messageSource.getMessage("acc.field.DeleteCustomerQuotationApprovalRule", null, RequestContextUtils.getLocale(request));
                   forTransaction="Customer Quotation";
               } else if((Integer.parseInt(moduleid) == Constants.Acc_Vendor_Quotation_ModuleId)){
                   msg =messageSource.getMessage("acc.field.DeleteVendorQuotationApprovalRule", null, RequestContextUtils.getLocale(request));
                   forTransaction="Vendor Quotation";
               } else if ((Integer.parseInt(moduleid) == Constants.Acc_Purchase_Requisition_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.RequisitionApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Purchase Requisition";
               } else if ((Integer.parseInt(moduleid) == Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.assetPurchaseRequisitionApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Asset Purchase Requisition";
               } else if ((Integer.parseInt(moduleid) == Constants.Acc_Sales_Order_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.SalesOrderApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Sales Order";
               } else if ((Integer.parseInt(moduleid) == Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.assetVendorQuotationApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Asset Vendor Quotation";
               } else if ((Integer.parseInt(moduleid) == Constants.Acc_Purchase_Order_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.PurchaseOrderApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Purchase Order";
               } else if ((Integer.parseInt(moduleid) == Constants.Acc_FixedAssets_Purchase_Order_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.AssetPurchaseOrderApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Asset Purchase Order";
               }else if ((Integer.parseInt(moduleid) == Constants.Acc_Invoice_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.InvoiceApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Sales Invoice";
               }else if ((Integer.parseInt(moduleid) == Constants.Acc_Vendor_Invoice_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.VendorInvoiceApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Purchase Invoice";
               }else if ((Integer.parseInt(moduleid) == Constants.Acc_Credit_Note_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.creditnoteApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Credit Note";
               }else if ((Integer.parseInt(moduleid) == Constants.Acc_Debit_Note_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.debitnoteApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Debit Note";
               }else if ((Integer.parseInt(moduleid) == Constants.Acc_Make_Payment_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.makepaymentApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Make Payment";               
               }else if ((Integer.parseInt(moduleid) == Constants.Acc_Receive_Payment_ModuleId)) {
                   msg = messageSource.getMessage("acc.field.receivepaymentApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                   forTransaction = "Receive Payment";
               }
                auditTrailObj.insertAuditLog(AuditAction.APPROVAL_RULE, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted approval rule " + rule + " with level " + level + " " + forTransaction, request, result.getEntityList().get(0).toString());
           // msg = "Delivery Order Approval Rule has been deleted successfully";
            txnManager.commit(status);
            }    
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(MultiLevelApprovalController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(MultiLevelApprovalController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
