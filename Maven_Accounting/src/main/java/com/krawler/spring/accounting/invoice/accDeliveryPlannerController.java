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
package com.krawler.spring.accounting.invoice;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.ServerEventManager;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.reports.AccScriptController;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class accDeliveryPlannerController extends MultiActionController implements MessageSourceAware {

    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    private HibernateTransactionManager txnManager;
    private accDeliveryPlannerDAO accDeliveryPlannerDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accReceiptDAO accReceiptDAOobj;
    private accPaymentDAO accPaymentDAOobj;
    private accDebitNoteDAO accDebitNoteobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private fieldDataManager fieldDataManagercntrl;
    private exportMPXDAOImpl exportDAO;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private AccSalesInvoiceThread accSalesInvoiceThread;
    private AccVendorInvoiceThread accVendorInvoiceThread;
    private AccDebitNoteThread accDebitNoteThread;
    private AccCreditNoteThread accCreditNoteThread;
    private AccPaymentThread accPaymentThread;
    private AccReceivePaymentThread accReceivePaymentThread ;
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }
     public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccDeliveryPlannerDAO(accDeliveryPlannerDAO accDeliveryPlannerDAOobj) {
        this.accDeliveryPlannerDAOobj = accDeliveryPlannerDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setExportDAO(exportMPXDAOImpl exportDAO) {
        this.exportDAO = exportDAO;
    }
    
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setAccSalesInvoiceThread(AccSalesInvoiceThread accSalesInvoiceThread) {
        this.accSalesInvoiceThread = accSalesInvoiceThread;
    }

    public void setAccVendorInvoiceThread(AccVendorInvoiceThread accVendorInvoiceThread) {
        this.accVendorInvoiceThread = accVendorInvoiceThread;
    }

    public void setAccDebitNoteThread(AccDebitNoteThread accDebitNoteThread) {
        this.accDebitNoteThread = accDebitNoteThread;
    }

    public void setAccCreditNoteThread(AccCreditNoteThread accCreditNoteThread) {
        this.accCreditNoteThread = accCreditNoteThread;
    }

    public void setAccPaymentThread(AccPaymentThread accPaymentThread) {
        this.accPaymentThread = accPaymentThread;
    }

    public void setAccReceivePaymentThread(AccReceivePaymentThread accReceivePaymentThread) {
        this.accReceivePaymentThread = accReceivePaymentThread;
    }
    
    public ModelAndView getInsertDataFromOneDatabaseToOtherDatabase(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String subdomain = "";
            String dbName = "";
            List avoidcompanyList = new ArrayList();
            JSONObject jsonParams = StringUtil.convertRequestToJsonObject(request);
//            int start=Integer.parseInt(request.getParameter("start"));
//            int end=Integer.parseInt(request.getParameter("end"));
             String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("dbname"))) {
                dbName = request.getParameter("dbname").toString().trim();
            }
            if (!StringUtil.isNullOrEmpty(subdomain) && !StringUtil.isNullOrEmpty(dbName)) {

//            KwlReturnObject kwlcompany = accountingHandlerDAOobj.getCompanyList(start, end);

//            List<String> campanyList = kwlcompany.getEntityList();

//            avoidcompanyList.add("adityaarch");
//            avoidcompanyList.add("genaxygroup");
//            avoidcompanyList.add("singapore10");
//            avoidcompanyList.add("defencestore");
//            avoidcompanyList.add("devgroup");
//            avoidcompanyList.add("99solution");
//            avoidcompanyList.add("eventosaur");
//            avoidcompanyList.add("839100104635");
//            avoidcompanyList.add("biogem");
//            avoidcompanyList.add("audimotors");
//            avoidcompanyList.add("nainwalassociates");
//            avoidcompanyList.add("audimotors");
//            avoidcompanyList.add("mksales");
//            avoidcompanyList.add("hester");
//            avoidcompanyList.add("organictatvaguj");
//            avoidcompanyList.add("vishalmarketing");
//            avoidcompanyList.add("vijayelectronics");

//            for (String company : campanyList) {
//                subdomain = company;
//                if (avoidcompanyList.contains(subdomain)) {
//                    continue;
//                }
                String filename = request.getParameter("filename");
                String dateFormatId = request.getParameter("dateFormat");
                jsonParams.put("subdomain", subdomain);
                jsonParams.put("dbname", dbName);
                jsonParams.put("filename", filename);
                jsonParams.put("dateFormat", dateFormatId);
                JSONObject cloneJson = new JSONObject(jsonParams.toString());

                accSalesInvoiceThread.add(cloneJson);
                accVendorInvoiceThread.add(cloneJson);
                accCreditNoteThread.add(cloneJson);
                accDebitNoteThread.add(cloneJson);
                accPaymentThread.add(cloneJson);
                accReceivePaymentThread.add(cloneJson);

//            }
                Thread t1 = new Thread(accSalesInvoiceThread);
                Thread t2 = new Thread(accVendorInvoiceThread);
                Thread t3 = new Thread(accCreditNoteThread);
                Thread t4 = new Thread(accDebitNoteThread);
                Thread t5 = new Thread(accPaymentThread);
                Thread t6 = new Thread(accReceivePaymentThread);
                t1.start();
                t2.start();
                t3.start();
                t4.start();
                t5.start();
                t6.start();

                issuccess = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for find Trnsactions and file has been saved in specified Location");

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void getDNDetails(HashSet<DebitNoteDetail> dndetails, String companyId) throws ServiceException {

        KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
        Company company = (Company) result.getEntityList().get(0);

        DebitNoteDetail row = new DebitNoteDetail();
        String DebitNoteDetailID = StringUtil.generateUUID();
        row.setID(DebitNoteDetailID);
        row.setSrno(1);
        row.setTotalDiscount(0.00);
        row.setCompany(company);
        row.setMemo("");
        dndetails.add(row);
    }

    private void getCNDetails(HashSet<CreditNoteDetail> cndetails, String companyId) throws ServiceException {

        KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
        Company company = (Company) result.getEntityList().get(0);

        CreditNoteDetail row = new CreditNoteDetail();
        String CreditNoteDetailID = StringUtil.generateUUID();
        row.setID(CreditNoteDetailID);
        row.setSrno(1);
        row.setTotalDiscount(0.00);
        row.setCompany(company);
        row.setMemo("");
        cndetails.add(row);
    }

    public JSONArray customFieldManupulation(List<Object[]> savedFilesList, String companyId, String jeId,String dbName) {
        KwlReturnObject resultObj = null;
        JSONArray array = new JSONArray();
        try {
            HashMap<String, Object> requestParamsCus = new HashMap<>();
            if (savedFilesList != null && savedFilesList.size() > 0) {
                for (Object[] custRow : savedFilesList) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("filedid", custRow[0]);
                    requestParamsCus.clear();
                    requestParamsCus = new HashMap<>();
                    requestParamsCus.put("colNum", Constants.Custom_column_Prefix + custRow[1].toString());
                    requestParamsCus.put("journalentryId", jeId);
                    requestParamsCus.put("companyid", companyId);
                    String strData = accountingHandlerDAOobj.getCustomDataUsingColNum(requestParamsCus,dbName);
                    jSONObject.put("Col" + custRow[1].toString(), strData);
                    jSONObject.put(custRow[3].toString(), "Col" + custRow[1].toString());
                    jSONObject.put("fieldname", custRow[3].toString());
                    jSONObject.put("xtype", "" + custRow[4].toString());
                    jSONObject.put("refcolumn_name", "Col" + custRow[5].toString());
                    array.put(jSONObject);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return array;
    }
    
    
    
    public ModelAndView savePushToDeliveryPlanner(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true, isCommitEx = false;
        String channelName = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            channelName = "/DeliveryPlanner/gridAutoRefresh";
            
            String auditMsg = "";
            String auditID = "";
            String docNumber = "";
            DateFormat df = (DateFormat) authHandler.getGlobalDateFormat();
            String companyID = sessionHandlerImpl.getCompanyid(request);
            String deliveryPlannerID = request.getParameter("deliveryPlannerID") == null ? "" : request.getParameter("deliveryPlannerID");
            int moduleid = !StringUtil.isNullOrEmpty(request.getParameter(Constants.moduleid)) ? Integer.parseInt(request.getParameter(Constants.moduleid)) : 0;

            HashMap<String, Object> deliveryPlannerParams = new HashMap<>();
            deliveryPlannerParams.put("companyID", companyID);
            
            if (StringUtil.isNullOrEmpty(deliveryPlannerID)) { // Create new entry
                BigInteger docCount = BigInteger.ZERO;
                HashMap<String, Object> requestParams = new HashMap<>();
                requestParams.put("docID", request.getParameter("docID"));
                requestParams.put("companyID", companyID);
                requestParams.put("moduleid", moduleid);
                
                KwlReturnObject invoiceListObj = accDeliveryPlannerDAOobj.getCountOfInvoicesInDeliveryPlanner(requestParams);
                if (invoiceListObj.getEntityList() != null && !invoiceListObj.getEntityList().isEmpty()) {
                    docCount = (BigInteger) invoiceListObj.getEntityList().get(0);
                }
                
//                deliveryPlannerParams.put("invoiceOccurance", docCount.intValue() + 1);
                deliveryPlannerParams.put("docID", request.getParameter("docID"));
                deliveryPlannerParams.put("pushTime", df.parse(request.getParameter("pushTime")));

                if (!StringUtil.isNullOrEmpty(request.getParameter("deliveryDate"))) {
                    deliveryPlannerParams.put("deliveryDate", df.parse(request.getParameter("deliveryDate")));
                }

                deliveryPlannerParams.put("deliveryLocation", request.getParameter("deliveryLocation"));
                deliveryPlannerParams.put("deliveryTime", request.getParameter("deliveryTime"));
                deliveryPlannerParams.put("remarksBySales", request.getParameter("remarksBySales"));
                deliveryPlannerParams.put("fromUser", sessionHandlerImpl.getUserid(request));
                deliveryPlannerParams.put("moduleid", moduleid);
                
                KwlReturnObject extraPrefResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
                ExtraCompanyPreferences extraCompanyPreferences = (extraPrefResult != null) ? (ExtraCompanyPreferences) extraPrefResult.getEntityList().get(0) : null;
                
                if (moduleid == Constants.Acc_Invoice_ModuleId && !StringUtil.isNullOrEmpty(request.getParameter("docID")) && extraCompanyPreferences != null && extraCompanyPreferences.isAutoPopulateFieldsForDeliveryPlanner()) {
                    KwlReturnObject invoiceObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), request.getParameter("docID"));
                    Invoice invoice = (Invoice) invoiceObj.getEntityList().get(0);
                    
                    if (invoice != null && invoice.getCustomer() != null && invoice.getCustomer().getDriver() != null) {
                        deliveryPlannerParams.put("driver", invoice.getCustomer().getDriver().getID());
                    }
                    if (invoice != null && invoice.getCustomer() != null && invoice.getCustomer().getVehicleNo() != null) {
                        deliveryPlannerParams.put("vehicleNo", invoice.getCustomer().getVehicleNo().getID());
                    }
                } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId && !StringUtil.isNullOrEmpty(request.getParameter("docID")) && extraCompanyPreferences != null && extraCompanyPreferences.isAutoPopulateFieldsForDeliveryPlanner()) {
                    KwlReturnObject deliveryOrderObj = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), request.getParameter("docID"));
                    DeliveryOrder deliveryOrder = (DeliveryOrder) deliveryOrderObj.getEntityList().get(0);

                    if (deliveryOrder != null && deliveryOrder.getCustomer() != null && deliveryOrder.getCustomer().getDriver() != null) {
                        deliveryPlannerParams.put("driver", deliveryOrder.getCustomer().getDriver().getID());
                    }
                    if (deliveryOrder != null && deliveryOrder.getCustomer() != null && deliveryOrder.getCustomer().getVehicleNo() != null) {
                        deliveryPlannerParams.put("vehicleNo", deliveryOrder.getCustomer().getVehicleNo().getID());
                    }
                    if (deliveryOrder != null && deliveryOrder.getCustomer() != null) {
                        deliveryPlannerParams.put("deliveryTime", deliveryOrder.getCustomer().getDeliveryTime());
                    }
                }
                
                HashMap<String, Object> filterParamsForLinking = new HashMap<>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("ModuleID");
                filter_params.add(moduleid);
                filter_names.add("SourceFlag");
                filter_params.add(1);
                filter_names.add("LinkedDocID");
                filter_params.add(request.getParameter("docID"));
                filterParamsForLinking.put("filter_names", filter_names);
                filterParamsForLinking.put("filter_params", filter_params);
                saveOrUpdatePushToDeliveryPlanner(deliveryPlannerParams, moduleid, filterParamsForLinking, docCount.intValue());

                if (!StringUtil.isNullOrEmpty(request.getParameter("docID"))) {
                    if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                        KwlReturnObject purchaseOrderObj = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), request.getParameter("docID"));
                        PurchaseOrder purchaseOrder = (PurchaseOrder) purchaseOrderObj.getEntityList().get(0);

                        docNumber = purchaseOrder.getPurchaseOrderNumber();
                    } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                        KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), request.getParameter("docID"));
                        Invoice inv = (Invoice) kdfObj.getEntityList().get(0);

                        docNumber = inv.getInvoiceNumber();
                    } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                        KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), request.getParameter("docID"));
                        DeliveryOrder deliveryOrder = (DeliveryOrder) kdfObj.getEntityList().get(0);

                        docNumber = deliveryOrder.getDeliveryOrderNumber();
                    } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                        KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), request.getParameter("docID"));
                        SalesReturn salesReturn = (SalesReturn) kdfObj.getEntityList().get(0);

                        docNumber = salesReturn.getSalesReturnNumber();
                    }
                }

                auditMsg = "added";
                auditID = AuditAction.DELIVERY_PLANNER_ADDED;
            } else { // Edit existing entry
                String column_Name = request.getParameter("column_Name") == null ? "" : request.getParameter("column_Name");
                String column_Value = request.getParameter("column_Value") == null ? "" : request.getParameter("column_Value");

                deliveryPlannerParams.put("deliveryPlannerID", deliveryPlannerID);

                if (column_Name.equalsIgnoreCase("deliveryDate")) {
                    deliveryPlannerParams.put(column_Name, df.parse(column_Value));
                } else {
                    deliveryPlannerParams.put(column_Name, column_Value);
                }
                
                if (!column_Name.equalsIgnoreCase("driver")) {
                    deliveryPlannerParams.put("driver", request.getParameter("mappedDriver"));
                }

                accDeliveryPlannerDAOobj.saveOrUpdatePushToDeliveryPlanner(deliveryPlannerParams);

                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(DeliveryPlanner.class.getName(), deliveryPlannerID);
                DeliveryPlanner delPlan = (DeliveryPlanner) kdfObj.getEntityList().get(0);
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                    KwlReturnObject purchaseOrderObj = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), delPlan.getDocumentNo());
                    PurchaseOrder purchaseOrder = (PurchaseOrder) purchaseOrderObj.getEntityList().get(0);

                    docNumber = purchaseOrder.getPurchaseOrderNumber();
                } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    docNumber = delPlan.getReferenceNumber() != null ? delPlan.getReferenceNumber().getInvoiceNumber() : "";
                } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    docNumber = delPlan.getDeliveryOrder() != null ? delPlan.getDeliveryOrder().getDeliveryOrderNumber() : "";
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    docNumber = delPlan.getSalesReturn() != null ? delPlan.getSalesReturn().getSalesReturnNumber() : "";
                }

                auditMsg = "updated";
                auditID = AuditAction.DELIVERY_PLANNER_UPDATED;
            }
            String moduleName = "";
            if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                moduleName = " a PO Delivery Planner for ";
            } else if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                moduleName = " a Delivery Planner for ";
            } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                moduleName = " a Sales Return Delivery Planner for ";
            }
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has  " + auditMsg + moduleName + docNumber, request, docNumber);
            
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                
                if (issuccess) {
                    jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void saveOrUpdatePushToDeliveryPlanner(HashMap<String, Object> deliveryPlannerParams, int moduleid, HashMap<String, Object> filterParamsForLinking, int docCount) throws ServiceException {
        // Push to planner from invoice module
        if (moduleid == Constants.Acc_Invoice_ModuleId) {
            KwlReturnObject doLinkingResult = accInvoiceDAOobj.getDeliveryOrderLinking(filterParamsForLinking);
            List<DeliveryOrderLinking> doLinkingList = doLinkingResult.getEntityList();
            if (doLinkingList != null && !doLinkingList.isEmpty()) { // if Invoice linked to DO then push number of entries in delivery planner respect to number of DO created from Invoice
                for (DeliveryOrderLinking doLinking : doLinkingList) {
                    deliveryPlannerParams.put("deliveryOrder", doLinking.getDocID());
                    deliveryPlannerParams.put("invoiceOccurance", ++docCount);
                    accDeliveryPlannerDAOobj.saveOrUpdatePushToDeliveryPlanner(deliveryPlannerParams);
                }
            } else { // if Invoice is not linked to any DO then push entry in delivery planner with blank DO Number
                deliveryPlannerParams.put("invoiceOccurance", ++docCount);
                accDeliveryPlannerDAOobj.saveOrUpdatePushToDeliveryPlanner(deliveryPlannerParams);
            }
        } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) { // Push to planner from delivery order module
            KwlReturnObject doLinkingResult = accInvoiceDAOobj.getInvoiceLinking(filterParamsForLinking);
            List<InvoiceLinking> invLinkingList = doLinkingResult.getEntityList();
            if (invLinkingList != null && !invLinkingList.isEmpty()) { // if DO linked to Invoice then push number of entries in delivery planner respect to number of Invoice created from DO
                for (InvoiceLinking invLinking : invLinkingList) {
                    deliveryPlannerParams.put("invoice", invLinking.getDocID());
                    deliveryPlannerParams.put("invoiceOccurance", ++docCount);
                    accDeliveryPlannerDAOobj.saveOrUpdatePushToDeliveryPlanner(deliveryPlannerParams);
                }
            } else { // if DO is not linked to any Invoice then push entry in delivery planner with blank Invoice Number
                deliveryPlannerParams.put("invoiceOccurance", ++docCount);
                accDeliveryPlannerDAOobj.saveOrUpdatePushToDeliveryPlanner(deliveryPlannerParams);
            }
        } else { // Push to planner from other module e.g. Purchase Order, Sales Return
            deliveryPlannerParams.put("invoiceOccurance", ++docCount);
            accDeliveryPlannerDAOobj.saveOrUpdatePushToDeliveryPlanner(deliveryPlannerParams);
        }
    }

    public ModelAndView saveDliveryPlannerAnnouncement(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true, isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            DateFormat df = (DateFormat) authHandler.getDateOnlyFormat(request);
            String announcementID = request.getParameter("announcementID") == null ? "" : request.getParameter("announcementID");
            HashMap<String, Object> announcementParams = new HashMap<String, Object>();
            announcementParams.put("companyID", sessionHandlerImpl.getCompanyid(request));

            if (StringUtil.isNullOrEmpty(announcementID)) { // Create new entry
                announcementParams.put("announcementTime", df.parse(request.getParameter("announcementTime")));
                announcementParams.put("announcementMsg", request.getParameter("announcementMsg"));

                accDeliveryPlannerDAOobj.saveOrUpdateDeliveryPlannerAnnouncement(announcementParams);
            } else { // Edit existing entry
                announcementParams.put("announcementID", announcementID);
                announcementParams.put("announcementTime", df.parse(request.getParameter("announcementTime")));
                announcementParams.put("announcementMsg", request.getParameter("announcementMsg"));

                accDeliveryPlannerDAOobj.saveOrUpdateDeliveryPlannerAnnouncement(announcementParams);
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getDeliveryPlanner(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        boolean isExport = false;
        String msg = "";
        String view="jsonView";
        try {
            int moduleid = !StringUtil.isNullOrEmpty(request.getParameter(Constants.moduleid)) ? Integer.parseInt(request.getParameter(Constants.moduleid)) : 0;
            String billid = !StringUtil.isNullOrEmpty(request.getParameter("billid")) ? request.getParameter("billid") : "";
            
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.df, authHandler.getGlobalDateFormat());
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put(Constants.REQ_startdate, request.getParameter("startdate"));
            requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
            boolean isFromDeliveryPlannerReportString=!StringUtil.isNullOrEmpty(request.getParameter("isFromDeliveryPlannerReport")) ? true : false;
            requestParams.put("isFromDeliveryPlannerReport", isFromDeliveryPlannerReportString);
            requestParams.put("moduleid", moduleid);
            requestParams.put("billid", billid);
            String exportFileName = request.getParameter("filename"); // for Export
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
            }
            
            if (request.getParameter("dir") != null && !StringUtil.isNullOrEmpty(request.getParameter("dir")) && request.getParameter("sort") != null && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                String orderBy = "";
                String direction = request.getParameter("dir");
                String field = request.getParameter("sort");
                if (StringUtil.equal(field, "driver")) {
                    orderBy = " dplan.driver.value " + direction;
                } else if (StringUtil.equal(field, "vehicleNo")) {
                    orderBy = " dplan.vehicleNumber.value " + direction;
                } else if (StringUtil.equal(field, "tripNo")) {
                    orderBy = " dplan.tripNumber.value " + direction;
                } else if (StringUtil.equal(field, "fromUser")) {
                    orderBy = " dplan.fromUser.firstName " + direction;
                } else if (StringUtil.equal(field, "tripDesc")) {
                    orderBy = " tripDescription " + direction;
                }else if (StringUtil.equal(field, "referenceNumber")) {
                    orderBy = " dplan.referenceNumber.invoiceNumber " + direction;
                } else {
                    orderBy = " " + field + " " + direction;
                }
                requestParams.put("order_by", orderBy);
            }

            KwlReturnObject result = accDeliveryPlannerDAOobj.getDeliveryPlanner(requestParams);

            List<Object[]> list = result.getEntityList();
            DataJArr = getDeliveryPlannerJson(request, list);
            int totalCount = result.getRecordTotalCount();

            jobj.put("data", DataJArr);
            if (isExport) {
                String fileType = request.getParameter("filetype");
                if (StringUtil.equal(fileType, "print")) {
                    String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                    jobj.put("GenerateDate", GenerateDate);
                    view = "jsonView-empty";
                }
                exportDAO.processRequest(request, response, jobj);
            }
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView deleteDeliveryPlannerPermanently(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String billID=""; 
        try {
            JSONArray jArr1 = new JSONArray(request.getParameter("billid"));
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            for (int i = 0; i< jArr1.length(); i++) {
            JSONObject jobj1 = jArr1.getJSONObject(i);
            billID += "'"+jobj1.getString("billid")+"'"+",";
           }
            billID=billID.substring(0, billID.length()-1);
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("id", billID);
            KwlReturnObject result = accDeliveryPlannerDAOobj.deleteDeliveryPlannerPermanently(requestParams);
            int totalCount = result.getRecordTotalCount();
            jobj.put("totalCount", totalCount);
            issuccess = true;
            msg=result.getMsg();
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accDeliveryPlannerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accDeliveryPlannerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accDeliveryPlannerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDeliveryPlannerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getDeliveryPlannerJson(HttpServletRequest request, List<Object[]> list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            int moduleid = !StringUtil.isNullOrEmpty(request.getParameter(Constants.moduleid)) ? Integer.parseInt(request.getParameter(Constants.moduleid)) : 0;
            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
            }
            DateFormat df = authHandler.getGlobalDateFormat();
            DateFormat onlyDateFormat = authHandler.getDateOnlyFormat();
            String companyid = sessionHandlerImpl.getCompanyid(request);

            for (Object[] deliveryPlannerObj : list) {
                DeliveryPlanner deliveryPlanner = (DeliveryPlanner) deliveryPlannerObj[0];
                JSONObject obj = new JSONObject();
                obj.put("occurrenceNo", deliveryPlanner.getInvoiceOccurance() != null ? deliveryPlanner.getInvoiceOccurance() : 1);
                obj.put("id", deliveryPlanner.getID());
                obj.put("pushTime", deliveryPlanner.getPushTime() != null ? df.format(deliveryPlanner.getPushTime()) : "");
                obj.put("fromUser", deliveryPlanner.getFromUser() != null ? deliveryPlanner.getFromUser().getFullName() : "");
                obj.put("deliveryLocation", deliveryPlanner.getDeliveryLocation() != null ? deliveryPlanner.getDeliveryLocation() : "");
                obj.put("deliveryDate", deliveryPlanner.getDeliveryDate() != null ? onlyDateFormat.format(deliveryPlanner.getDeliveryDate()) : "");
                obj.put("deliveryTime", deliveryPlanner.getDeliveryTime() != null ? deliveryPlanner.getDeliveryTime() : "");
                obj.put("remarksBySales", deliveryPlanner.getRemarksBySales() != null ? deliveryPlanner.getRemarksBySales() : "");
                obj.put("printedBy", deliveryPlanner.getPrintedBy() != null ? deliveryPlanner.getPrintedBy() : "");
                obj.put("remarksByPlanner", deliveryPlanner.getRemarksByPlanner() != null ? deliveryPlanner.getRemarksByPlanner() : "");
                if (isExport) {//In case of Export required Values for this fields Ref ERP-16795
                    obj.put("vehicleNo", deliveryPlanner.getVehicleNumber() != null ? deliveryPlanner.getVehicleNumber().getValue() : "");
                    obj.put("driver", deliveryPlanner.getDriver() != null ? deliveryPlanner.getDriver().getValue() : "");
                    obj.put("tripNo", deliveryPlanner.getTripNumber() != null ? deliveryPlanner.getTripNumber().getValue() : "");
                } else {
                    obj.put("vehicleNo", deliveryPlanner.getVehicleNumber() != null ? deliveryPlanner.getVehicleNumber().getID() : "");
                    obj.put("driver", deliveryPlanner.getDriver() != null ? deliveryPlanner.getDriver().getID() : "");
                    obj.put("tripNo", deliveryPlanner.getTripNumber() != null ? deliveryPlanner.getTripNumber().getID() : "");
                }
                obj.put("tripDesc", deliveryPlanner.getTripDescription() != null ? deliveryPlanner.getTripDescription() : "");
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                    KwlReturnObject poObj = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), deliveryPlanner.getDocumentNo());
                    PurchaseOrder purchaseOrder = (PurchaseOrder) poObj.getEntityList().get(0);
                    obj.put("referenceNumber", purchaseOrder != null ? purchaseOrder.getPurchaseOrderNumber() : "");
                    obj.put("invoiceId", purchaseOrder != null ? purchaseOrder.getID() : "");
                } else if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    obj.put("referenceNumber", deliveryPlanner.getReferenceNumber() != null ? deliveryPlanner.getReferenceNumber().getInvoiceNumber() : "");
                    obj.put("invoiceId", deliveryPlanner.getReferenceNumber() != null ? deliveryPlanner.getReferenceNumber().getID() : "");
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    obj.put("referenceNumber", deliveryPlanner.getSalesReturn() != null ? deliveryPlanner.getSalesReturn().getSalesReturnNumber() : "");
                    obj.put("invoiceId", deliveryPlanner.getSalesReturn() != null ? deliveryPlanner.getSalesReturn().getID() : "");
                }
               
                String doNos = "";
                String doIds = "";
                // For getting PO created by using GRN
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("companyid", companyid);
                    params.put("poid", deliveryPlanner.getDocumentNo());
                    KwlReturnObject groResult = accGoodsReceiptobj.getGoodsReceiptOrdersMerged(params);
                    List<Object[]> groList = groResult.getEntityList();
                    for (Object[] objArr : groList) {
                        String groId = objArr[0].toString();
                        KwlReturnObject groObj = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), groId);
                        GoodsReceiptOrder goodsReceiptOrder = (GoodsReceiptOrder) groObj.getEntityList().get(0);

                        doNos += goodsReceiptOrder.getGoodsReceiptOrderNumber() + ", ";
                        doIds += goodsReceiptOrder.getID() + ", ";
                    }
                    if (!StringUtil.isNullOrEmpty(doNos) && !StringUtil.isNullOrEmpty(doIds)) {
                        obj.put("doNo", doNos.substring(0, doNos.length() - 2));
                        obj.put("doId", doIds.substring(0, doIds.length() - 2));
                    }
                } else if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId) { // For getting DO created by using SI and vice versa
                    obj.put("doNo", (deliveryPlanner.getDeliveryOrder() != null) ? deliveryPlanner.getDeliveryOrder().getDeliveryOrderNumber() : "");
                    obj.put("doId", (deliveryPlanner.getDeliveryOrder() != null) ? deliveryPlanner.getDeliveryOrder().getID() : "");
                }
                
                jArr.put(obj);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    public ModelAndView getDliveryPlannerAnnouncement(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put(Constants.REQ_startdate, request.getParameter("startdate"));
            requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject result = accDeliveryPlannerDAOobj.getDliveryPlannerAnnouncement(requestParams);

            List<DeliveryPlannerAnnouncement> list = result.getEntityList();
            DataJArr = getDliveryPlannerAnnouncementJson(request, list);
            int totalCount = result.getRecordTotalCount();

            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getDliveryPlannerAnnouncementJson(HttpServletRequest request, List<DeliveryPlannerAnnouncement> list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);

            for (DeliveryPlannerAnnouncement deliveryPlannerAnnouncement : list) {
                JSONObject obj = new JSONObject();
                obj.put("announcementID", deliveryPlannerAnnouncement.getID());
                obj.put("announcementTime", deliveryPlannerAnnouncement.getAnnouncementTime() != null ? df.format(deliveryPlannerAnnouncement.getAnnouncementTime()) : "");
                obj.put("announcementMsg", deliveryPlannerAnnouncement.getAnnouncementMsg() != null ? deliveryPlannerAnnouncement.getAnnouncementMsg() : "");
                jArr.put(obj);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    public ModelAndView getVehicleDliverySummaryReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
            boolean isVehicleDeliverySummaryReport = request.getParameter("isVehicleDeliverySummaryReport") != null ? Boolean.parseBoolean(request.getParameter("isVehicleDeliverySummaryReport")) : false;
            boolean isDriverDeliverySummaryReport = request.getParameter("isDriverDeliverySummaryReport") != null ? Boolean.parseBoolean(request.getParameter("isDriverDeliverySummaryReport")) : false;
            int moduleid = !StringUtil.isNullOrEmpty(request.getParameter(Constants.moduleid)) ? Integer.parseInt(request.getParameter(Constants.moduleid)) : 0;

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("isVehicleDeliverySummaryReport", isVehicleDeliverySummaryReport);
            requestParams.put("isDriverDeliverySummaryReport", isDriverDeliverySummaryReport);
            requestParams.put(Constants.moduleid, moduleid);

            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate"))) {
                LocalDate localStartDate = dtf.parseLocalDate(request.getParameter("startdate"));
                DateTime date = localStartDate.toDateTime(LocalTime.MIDNIGHT);

                DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();
                Date firstDate = firstDateOfMonth.toDate();

                DateTime lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                Date lastDate = lastDateOfMonth.toDate();

                requestParams.put(Constants.REQ_startdate, firstDate);
                requestParams.put(Constants.REQ_enddate, lastDate);
            }

            KwlReturnObject result = accDeliveryPlannerDAOobj.getVehicleDliverySummaryReport(requestParams);

            List<Object[]> list = result.getEntityList();
            JSONArray DataJArr = getVehicleDliverySummaryReportJson(request, list);
            int totalCount = result.getRecordTotalCount();

            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getVehicleDliverySummaryReportJson(HttpServletRequest request, List<Object[]> list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            boolean isVehicleDeliverySummaryReport = request.getParameter("isVehicleDeliverySummaryReport") != null ? Boolean.parseBoolean(request.getParameter("isVehicleDeliverySummaryReport")) : false;
            boolean isDriverDeliverySummaryReport = request.getParameter("isDriverDeliverySummaryReport") != null ? Boolean.parseBoolean(request.getParameter("isDriverDeliverySummaryReport")) : false;
            int moduleid = !StringUtil.isNullOrEmpty(request.getParameter(Constants.moduleid)) ? Integer.parseInt(request.getParameter(Constants.moduleid)) : 0;

            for (Object[] row : list) {
                String vehicleNo = (row[0] != null) ? (String) row[0] : "";
                BigInteger NoOfTrip = (BigInteger) row[1];
                BigInteger NoOfDoPo = (BigInteger) row[2];
                String id = (String) row[3];
                BigInteger NoOfPo = (BigInteger) row[4];

                JSONObject obj = new JSONObject();
                obj.put("id", id);
                if (isVehicleDeliverySummaryReport) {
                    obj.put("vehicleNo", vehicleNo);
                } else if (isDriverDeliverySummaryReport) {
                    obj.put("driver", vehicleNo);
                }

                obj.put("noOfTrips", NoOfTrip.intValue());
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                    obj.put("noOfDoPo", NoOfPo.intValue());
                } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    obj.put("noOfDoPo", NoOfDoPo.intValue());
                }
                
                jArr.put(obj);
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVehicleDliverySummaryReportJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getIndividualVehicleDliveryReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            boolean isIndividualVehicleDeliveryReport = request.getParameter("isIndividualVehicleDeliveryReport") != null ? Boolean.parseBoolean(request.getParameter("isIndividualVehicleDeliveryReport")) : false;
            boolean isIndividualDriverDeliveryReport = request.getParameter("isIndividualDriverDeliveryReport") != null ? Boolean.parseBoolean(request.getParameter("isIndividualDriverDeliveryReport")) : false;
            int moduleid = !StringUtil.isNullOrEmpty(request.getParameter(Constants.moduleid)) ? Integer.parseInt(request.getParameter(Constants.moduleid)) : 0;

            DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("vehicleNo", request.getParameter("vehicleNo"));
            requestParams.put("isIndividualVehicleDeliveryReport", isIndividualVehicleDeliveryReport);
            requestParams.put("isIndividualDriverDeliveryReport", isIndividualDriverDeliveryReport);
            requestParams.put(Constants.moduleid, moduleid);

            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate"))) {
                LocalDate localStartDate = dtf.parseLocalDate(request.getParameter("startdate"));
                DateTime date = localStartDate.toDateTime(LocalTime.MIDNIGHT);

                DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();
                Date firstDate = firstDateOfMonth.toDate();

                DateTime lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                Date lastDate = lastDateOfMonth.toDate();

                requestParams.put(Constants.REQ_startdate, firstDate);
                requestParams.put(Constants.REQ_enddate, lastDate);
            }

            KwlReturnObject result = accDeliveryPlannerDAOobj.getIndividualVehicleDliveryReport(requestParams);

            List<Object[]> list = result.getEntityList();
            JSONArray DataJArr = getIndividualVehicleDliveryReportJson(request, list);
            int totalCount = result.getRecordTotalCount();

            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getIndividualVehicleDliveryReportJson(HttpServletRequest request, List<Object[]> list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            DateFormat formatter = new SimpleDateFormat("EEEE");
            int moduleid = !StringUtil.isNullOrEmpty(request.getParameter(Constants.moduleid)) ? Integer.parseInt(request.getParameter(Constants.moduleid)) : 0;
            
            for (Object[] row : list) {
                Date pushDate = (Date) row[0];
                String pushDay = (String) row[1];
                BigInteger NoOfTrip = (BigInteger) row[2];
                BigInteger NoOfDoPo = (BigInteger) row[3];
                String id = (String) row[4];
                String driver="";
                if(row[5]!=null){
                    driver=(String) row[5];
                }
                BigInteger NoOfPo = (BigInteger) row[6];
                JSONObject obj = new JSONObject();
                obj.put("id", id);
                obj.put("pushDate", df.format(pushDate));
                obj.put("pushDay", formatter.format(new Date(df.format(pushDate))));
                obj.put("noOfTrips", NoOfTrip.intValue());
                obj.put("driver",driver);         
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                    obj.put("noOfDoPo", NoOfPo.intValue());
                } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    obj.put("noOfDoPo", NoOfDoPo.intValue());
                }
                jArr.put(obj);
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getIndividualVehicleDliveryReportJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getIndividualVehicleDOPOReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            boolean isIndividualVehicleDOPOReport = request.getParameter("isIndividualVehicleDOPOReport") != null ? Boolean.parseBoolean(request.getParameter("isIndividualVehicleDOPOReport")) : false;
            boolean isIndividualDriverDOPOReport = request.getParameter("isIndividualDriverDOPOReport") != null ? Boolean.parseBoolean(request.getParameter("isIndividualDriverDOPOReport")) : false;
            int moduleid = !StringUtil.isNullOrEmpty(request.getParameter(Constants.moduleid)) ? Integer.parseInt(request.getParameter(Constants.moduleid)) : 0;

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("vehicleNo", request.getParameter("vehicleNo"));
            requestParams.put("startDate", request.getParameter("startDate"));
            requestParams.put("isIndividualVehicleDOPOReport", isIndividualVehicleDOPOReport);
            requestParams.put("isIndividualDriverDOPOReport", isIndividualDriverDOPOReport);
            requestParams.put(Constants.moduleid, moduleid);

            KwlReturnObject result = accDeliveryPlannerDAOobj.getIndividualVehicleDOPOReport(requestParams);

            List<Object[]> list = result.getEntityList();
            JSONArray DataJArr = getIndividualVehicleDOPOReportJson(request, list);
            int totalCount = result.getRecordTotalCount();

            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getIndividualVehicleDOPOReportJson(HttpServletRequest request, List<Object[]> list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            boolean isIndividualVehicleDOPOReport = request.getParameter("isIndividualVehicleDOPOReport") != null ? Boolean.parseBoolean(request.getParameter("isIndividualVehicleDOPOReport")) : false;
            boolean isIndividualDriverDOPOReport = request.getParameter("isIndividualDriverDOPOReport") != null ? Boolean.parseBoolean(request.getParameter("isIndividualDriverDOPOReport")) : false;
            int moduleid = !StringUtil.isNullOrEmpty(request.getParameter(Constants.moduleid)) ? Integer.parseInt(request.getParameter(Constants.moduleid)) : 0;

            BigInteger NoOfTrips = BigInteger.ZERO;
            BigInteger NoOfDoPo = BigInteger.ZERO;
            BigInteger NoOfPo = BigInteger.ZERO;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("vehicleNo", request.getParameter("vehicleNo"));
            requestParams.put("startDate", request.getParameter("startDate"));
            requestParams.put("isIndividualVehicleDOPOReport", isIndividualVehicleDOPOReport);
            requestParams.put("isIndividualDriverDOPOReport", isIndividualDriverDOPOReport);
            requestParams.put(Constants.moduleid, moduleid);

            KwlReturnObject result = accDeliveryPlannerDAOobj.getNoOfTripsAndDOPOofVehicleForDay(requestParams);
            if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                Object[] row = (Object[]) result.getEntityList().get(0);
                NoOfTrips = (BigInteger) row[0];
                NoOfDoPo = (BigInteger) row[1];
                NoOfPo = (BigInteger) row[2];
            }

            for (Object[] row : list) {
                String doPoName = (String) row[0];
                String tripNo = (String) row[1];
                String tripDesc = (String) row[2];
                String driver="";
                if(row[3]!=null){
                driver=(String) row[3];
                }

                JSONObject obj = new JSONObject();
                obj.put("doPoName", doPoName);
                obj.put("tripNo", tripNo);
                obj.put("tripDesc", tripDesc);
                obj.put("noOfTrips", NoOfTrips);
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                    obj.put("noOfDoPo", NoOfPo);
                } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    obj.put("noOfDoPo", NoOfDoPo);
                }
                obj.put("driver",driver);
                jArr.put(obj);
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getIndividualVehicleDOPOReportJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getDriversTrackingReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject result = accDeliveryPlannerDAOobj.getDriversTrackingReport(requestParams);

            List<Object[]> list = result.getEntityList();
            DataJArr = getDriversTrackingReportJson(request, list);
            int totalCount = result.getRecordTotalCount();

            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /*
     * Export for Driver's Tracking Report
     */
    public ModelAndView exportDriversTrackingReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String view = "jsonView_ex";
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject result = accDeliveryPlannerDAOobj.getDriversTrackingReport(requestParams);
            String fileType = request.getParameter("filetype");
            List<Object[]> list = result.getEntityList();
            DataJArr = getDriversTrackingReportJson(request, list);
            jobj.put("data", DataJArr);
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDAO.processRequest(request, response, jobj);

        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accDeliveryPlannerController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return new ModelAndView(view, "model", jobj.toString());
    }

    public JSONArray getDriversTrackingReportJson(HttpServletRequest request, List<Object[]> list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {        
            //params to send to get billing address
            HashMap<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            addressParams.put("isDefaultAddress", true); //always true to get defaultaddress
            for (Object[] deliveryOrderObj : list) {
                DeliveryOrder deliveryOrder = (DeliveryOrder) deliveryOrderObj[0];
                JSONObject obj = new JSONObject();
                obj.put("deliveryOrderRef", deliveryOrder.getDeliveryOrderNumber() != null ? deliveryOrder.getDeliveryOrderNumber() : "");
                obj.put("customerName", deliveryOrder.getCustomer() != null ? deliveryOrder.getCustomer().getName() : "");
                obj.put("driverName", deliveryOrder.getDriver() != null ? deliveryOrder.getDriver().getValue() : "");
                
                String DOCustomerAddress;
                if (deliveryOrder.getCustomer() != null) {
                    addressParams.put("isBillingAddress", true); //true to get billing address
                    addressParams.put("customerid", deliveryOrder.getCustomer().getID());
                    DOCustomerAddress = accountingHandlerDAOobj.getCustomerAddress(addressParams);
                } else {
                    DOCustomerAddress = "";
                }
                obj.put("deliveryAddress", DOCustomerAddress);
                
                jArr.put(obj);
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
   
    public void insertSalesInvoiceDataFromOneDBToOtherDB(JSONObject jsonParam){
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject resultObj = null;
        double openingBalanceAmountDue = 0.0;
        double exchangeRateForOpeningTransaction = 0.0;
        String msg = "";
        try {
            String subdomain = "";
            String dbName = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("subdomain"))) {
                subdomain = jsonParam.optString("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("dbname"))) {
                dbName = jsonParam.optString("dbname").toString().trim();
            }
            String filename = "";
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("filename"))) {
                filename = jsonParam.optString("filename");
            }
            String dateFormat = "yyyy-MM-dd", dateFormatId = jsonParam.optString("dateFormat");
            DateFormat df = new SimpleDateFormat(dateFormat);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject sourceDbData = null;
            sourceDbData = accountingHandlerDAOobj.getInvoiceFromFirstDB(subdomainArray,dbName);
            List<Object[]> list = sourceDbData.getEntityList();
            createJsonForSalesInvoice(list,jsonParam);
            list=null;
            sourceDbData=null;
            sourceDbData = accountingHandlerDAOobj.getOpeningInvoiceFromFirstDB(subdomainArray,dbName);
            List<Object[]> list1 = sourceDbData.getEntityList();
            createJsonForSalesInvoice(list1,jsonParam);
            list1=null;
            sourceDbData=null;
            issuccess = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accDeliveryPlannerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void insertVendorInvoiceDataFromOneDBToOtherDB(JSONObject jsonParam) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject resultObj = null;
        double openingBalanceAmountDue = 0.0;
        double exchangeRateForOpeningTransaction = 0.0;
        String msg = "";
        try {
            String subdomain = "";
            String dbName = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("subdomain"))) {
                subdomain = jsonParam.optString("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("dbname"))) {
                dbName = jsonParam.optString("dbname").toString().trim();
            }
            String filename = "";
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("filename"))) {
                filename = jsonParam.optString("filename");
            }
            String dateFormat = "yyyy-MM-dd", dateFormatId = jsonParam.optString("dateFormat");
            DateFormat df = new SimpleDateFormat(dateFormat);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject sourceDbData = null;
            /*
             * Vendor Invoice
             */
            sourceDbData = accountingHandlerDAOobj.getVendorInvoiceFromFirstDB(subdomainArray,dbName);
            List<Object[]> list = sourceDbData.getEntityList();
            list = sourceDbData.getEntityList();
            createJsonForVendorInvoice(list,jsonParam);
            sourceDbData=null;
            list=null;
            sourceDbData = accountingHandlerDAOobj.getVendorOpeningInvoiceFromFirstDB(subdomainArray,dbName);
            List<Object[]> list1 = sourceDbData.getEntityList();
            createJsonForVendorInvoice(list1,jsonParam);
            sourceDbData=null;
            list1=null;
                        
            issuccess = true;
        } catch (Exception ex) {
            System.out.println("Exception for subdomain :"+jsonParam.optString("subdomain"));
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accDeliveryPlannerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void insertCreditNoteDataFromOneDBToOtherDB(JSONObject jsonParam) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject resultObj = null;
        double openingBalanceAmountDue = 0.0;
        double exchangeRateForOpeningTransaction = 0.0;
        String msg = "";
        try {
            String subdomain = "";
            String dbName = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("subdomain"))) {
                subdomain = jsonParam.optString("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("dbname"))) {
                dbName = jsonParam.optString("dbname").toString().trim();
            }
            String filename = "";
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("filename"))) {
                filename = jsonParam.optString("filename");
            }
            String dateFormat = "yyyy-MM-dd", dateFormatId = jsonParam.optString("dateFormat");
            DateFormat df = new SimpleDateFormat(dateFormat);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject sourceDbData = null;
            sourceDbData = accountingHandlerDAOobj.getCustomerCreditNoteFromFirstDB(subdomainArray,dbName);
            List<Object[]> list = sourceDbData.getEntityList();
            createJsonForCreditNote(list,jsonParam);
            sourceDbData = null;
            list = null;
            sourceDbData = accountingHandlerDAOobj.getCustomerOpeningCreditNoteFromFirstDB(subdomainArray,dbName);
            List<Object[]> list1 = sourceDbData.getEntityList();
            createJsonForCreditNote(list1,jsonParam);
            sourceDbData = null;
            list1 = null;
       
            issuccess = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accDeliveryPlannerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    public void insertDebitNoteDataFromOneDBToOtherDB(JSONObject jsonParam) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject resultObj = null;
        double openingBalanceAmountDue = 0.0;
        double exchangeRateForOpeningTransaction = 0.0;
        String msg = "";
        try {
            String subdomain = "";
            String dbName = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("subdomain"))) {
                subdomain = jsonParam.optString("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("dbname"))) {
                dbName = jsonParam.optString("dbname").toString().trim();
            }
            String filename = "";
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("filename"))) {
                filename = jsonParam.optString("filename");
            }
            String dateFormat = "yyyy-MM-dd", dateFormatId = jsonParam.optString("dateFormat");
            DateFormat df = new SimpleDateFormat(dateFormat);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject sourceDbData = null;
            /*
             * Debit Note
             */
            sourceDbData = accountingHandlerDAOobj.getCustomerDebitNoteFromFirstDB(subdomainArray,dbName);
            List<Object[]> list = sourceDbData.getEntityList();
            createJsonForDebitNote(list,jsonParam);
            list = null;
            sourceDbData = null;
            sourceDbData = accountingHandlerDAOobj.getCustomerOpeningDebitNoteFromFirstDB(subdomainArray,dbName);
            List<Object[]> list1 = sourceDbData.getEntityList();
            createJsonForDebitNote(list1,jsonParam);
            sourceDbData = null;
            list1 = null;
            issuccess = true;
        } catch (Exception ex) {
             ex.printStackTrace();
             msg = "" + ex.getMessage();
             if (ex.getMessage() == null) {
                 msg = ex.getCause().getMessage();
             }
             Logger.getLogger(accDeliveryPlannerController.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
     
    public void insertReceivePaymentDataFromOneDBToOtherDB(JSONObject jsonParam) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject resultObj = null;
        double openingBalanceAmountDue = 0.0;
        double exchangeRateForOpeningTransaction = 0.0;
        String msg = "";
        try {
            String subdomain = "";
            String dbName = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("subdomain"))) {
                subdomain = jsonParam.optString("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("dbname"))) {
                dbName = jsonParam.optString("dbname").toString().trim();
            }
            String filename = "";
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("filename"))) {
                filename = jsonParam.optString("filename");
            }
            String dateFormat = "yyyy-MM-dd", dateFormatId = jsonParam.optString("dateFormat");
            DateFormat df = new SimpleDateFormat(dateFormat);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject sourceDbData = null;
            sourceDbData = accountingHandlerDAOobj.getReceiptFromFirstDB(subdomainArray,dbName);
            List<Object[]> list = sourceDbData.getEntityList();
            createJsonForReceipt(list,jsonParam);
            sourceDbData = null;
            list = null;
            sourceDbData = accountingHandlerDAOobj.getOpeningReceiptFromFirstDB(subdomainArray,dbName);
            List<Object[]> list1 = sourceDbData.getEntityList();
            createJsonForReceipt(list1,jsonParam);
            sourceDbData = null;
            list1 = null;
            issuccess = true;
        } catch (Exception ex) {
             System.out.println("Exception For Subdomain : "+jsonParam.optString("subdomain"));
             ex.printStackTrace();
             msg = "" + ex.getMessage();
             if (ex.getMessage() == null) {
                 msg = ex.getCause().getMessage();
             }
             Logger.getLogger(accDeliveryPlannerController.class.getName()).log(Level.SEVERE, null, ex);
         }
    }

    public void insertPaymentDataFromOneDBToOtherDB(JSONObject jsonParam) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject resultObj = null;
        double openingBalanceAmountDue = 0.0;
        double exchangeRateForOpeningTransaction = 0.0;
        String msg = "";
        try {
            String subdomain = "";
            String dbName = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("subdomain"))) {
                subdomain = jsonParam.optString("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("dbname"))) {
                dbName = jsonParam.optString("dbname").toString().trim();
            }
            String filename = "";
            if (!StringUtil.isNullOrEmpty(jsonParam.optString("filename"))) {
                filename = jsonParam.optString("filename");
            }
            String dateFormat = "yyyy-MM-dd", dateFormatId = jsonParam.optString("dateFormat");
            DateFormat df = new SimpleDateFormat(dateFormat);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject sourceDbData = null;
            sourceDbData = accountingHandlerDAOobj.getPaymentFromFirstDB(subdomainArray,dbName);
            List<Object[]> list = sourceDbData.getEntityList();
            createJsonForPayment(list,jsonParam);
            sourceDbData=null;
            list=null;
            sourceDbData = accountingHandlerDAOobj.getOpeningPaymentFromFirstDB(subdomainArray,dbName);
            List<Object[]> list1 = sourceDbData.getEntityList();
            createJsonForPayment(list1,jsonParam);
            issuccess = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accDeliveryPlannerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void createJsonForSalesInvoice(List <Object[]> list,JSONObject jsonParam){
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject resultObj = null;
        double openingBalanceAmountDue = 0.0;
        double exchangeRateForOpeningTransaction = 0.0;
        String msg = "";
        String dateFormat = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(dateFormat);
        try{
            String dbName=jsonParam.optString("dbname").toString().trim();
        if (list != null && list.size() > 0) {
                for (Object[] row : list) {
                    openingBalanceAmountDue = (double) row[12];
                    exchangeRateForOpeningTransaction = (double) row[13];
                    JSONObject invjson = new JSONObject();
                    KwlReturnObject result = accInvoiceDAOobj.getInvoiceCount((String) row[0], (String) row[5]);
                    int nocount = result.getRecordTotalCount();
                    if (nocount > 0) {
                        continue;
                    }

                    invjson.put("entrynumber", row[0]);
                    if (row[22] != null && ((Character) row[22]) == 'T') {
                        invjson.put("autogenerated", true);
                    } else {
                        invjson.put("autogenerated", false);
                    }
                    invjson.put("erdid", row[1]);
                    invjson.put("shipaddress", "");
                    invjson.put("porefno", row[2]);
                    invjson.put("duedate", row[3]);
                    invjson.put("poRefDate", row[4]);
                    invjson.put("companyid", row[5]);
                    invjson.put("currencyid", row[6]);
                    invjson.put("externalCurrencyRate", row[7]);
                    invjson.put("salesPerson", row[8]);
                    invjson.put("partialinv", false);
                    invjson.put("customerid", row[9]);
                    invjson.put("accountid", row[10]);
                    invjson.put("billto", "");
                    invjson.put("creationDate", df.parse(df.format((row[11]))));
                    invjson.put("lastModifiedDate", df.parse(df.format(new Date())));
                    invjson.put("isOpeningBalenceInvoice", true);
                    invjson.put("isNormalInvoice", false);
                    invjson.put("originalOpeningBalanceAmount", row[12]);
                    invjson.put("openingBalanceAmountDue", row[12]);
                    invjson.put("exchangeRateForOpeningTransaction", row[13]);
                    invjson.put("conversionRateFromCurrencyToBase", true);
                    invjson.put("termid", row[14]);
                    invjson.put("memo", row[15]);
                    
                    invjson.put("seqnumber", row[17]);
                    invjson.put("datePreffixValue", row[18]);
                    invjson.put("dateAfterPreffixValue", row[19]);
                    invjson.put("dateSuffixValue", row[20]);
                    if (row[21] != null) {
                        invjson.put("seqformat", row[21]);
                    }
                    
                    invjson.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(openingBalanceAmountDue * exchangeRateForOpeningTransaction, (String) row[5]));
                    invjson.put(Constants.openingBalanceBaseAmountDue, authHandler.round(openingBalanceAmountDue * exchangeRateForOpeningTransaction, (String) row[5]));
                    invjson.put("accopeningbalanceinvoicecustomdataref", row[16]);
                    HashMap<String, Object> requestParamsCus = new HashMap<>();
                    requestParamsCus.put("moduleid", Constants.Acc_Invoice_ModuleId);
                    requestParamsCus.put("companyid", (String) row[5]);
                    String companyId = (String) row[5];
                    String jeId = (String) row[16];
                    KwlReturnObject savedFilesIdResult = accountingHandlerDAOobj.getFieldParamsFromFirstDB(requestParamsCus,dbName);
                    List<Object[]> savedFilesList = savedFilesIdResult.getEntityList();
                    /*
                     * Create Json for Custom Field.
                     */
                    JSONArray array = customFieldManupulation(savedFilesList, companyId, jeId,dbName);


                    resultObj = accInvoiceDAOobj.addInvoice(invjson, new HashSet());
                    Invoice invoice = (Invoice) resultObj.getEntityList().get(0);
                    if (array.length() > 0) {
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", array);
                        customrequestParams.put("modulename", "OpeningBalanceInvoice");
                        customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceInvoiceid);
                        customrequestParams.put("modulerecid", invoice.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                        customrequestParams.put("companyid", (String) row[5]);
                        customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceInvoice_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            invjson.put("invoiceid", invoice.getID());
                            invjson.put("openingBalanceInvoiceCustomData", invoice.getID());
                            accInvoiceDAOobj.updateInvoice(invjson, new HashSet());
                        }
                    }
                }
            }
        }catch(Exception ex){
           Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);  
        }
    }
        
    public void createJsonForVendorInvoice(List <Object[]> list,JSONObject jsonParam){
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject resultObj = null;
        double openingBalanceAmountDue = 0.0;
        double exchangeRateForOpeningTransaction = 0.0;
        String msg = "";
         String dateFormat = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(dateFormat);
        String dbName=jsonParam.optString("dbname").toString().trim();
        try{
            if (list != null && list.size() > 0) {
                for (Object[] row : list) {
                    Map<String, Object> greceipthm = new HashMap<String, Object>();
                    openingBalanceAmountDue = (double) row[12];
                    exchangeRateForOpeningTransaction = (double) row[13];
                    KwlReturnObject result = accGoodsReceiptobj.getReceiptFromNo((String) row[0], (String) row[5]);
                    int nocount = result.getRecordTotalCount();
                    if (nocount > 0) {
                        continue;
                    }
                    greceipthm.put("entrynumber", row[0]);
                    if (row[22] != null && ((Character) row[22]) == 'T') {
                        greceipthm.put("autogenerated", true);
                    } else {
                        greceipthm.put("autogenerated", false);
                    }
                    greceipthm.put("erdid", row[1]);
                    greceipthm.put("shipaddress", "");
                    greceipthm.put("partyInvoiceNumber", row[2]);
                    greceipthm.put("duedate", row[3]);
                    greceipthm.put("partyInvoiceDate", row[4]);
                    greceipthm.put("companyid", row[5]);
                    greceipthm.put("currencyid", row[6]);
                    greceipthm.put("externalCurrencyRate", row[7]);
                    greceipthm.put("salesPerson", row[8]);
                    greceipthm.put("vendorid", row[9]);
                    greceipthm.put("accountid", row[10]);
                    greceipthm.put("billto", "");
                    greceipthm.put("creationDate", df.parse(df.format((row[11]))));
                    greceipthm.put("lastModifiedDate", df.parse(df.format(new Date())));
                    greceipthm.put("isOpeningBalenceInvoice", true);
                    greceipthm.put("isNormalInvoice", false);
                    greceipthm.put("originalOpeningBalanceAmount", row[12]);
                    greceipthm.put("openingBalanceAmountDue", row[12]);
                    greceipthm.put("exchangeRateForOpeningTransaction", row[13]);
                    greceipthm.put("conversionRateFromCurrencyToBase", true);
                    greceipthm.put("termid", row[14]);
                    greceipthm.put("memo", row[15]);
                    greceipthm.put("venbilladdress", "");
                    greceipthm.put("venshipaddress", "");
                    greceipthm.put("seqnumber", row[17]);
                    greceipthm.put("datePreffixValue", row[18]);
                    greceipthm.put("dateAfterPreffixValue", row[19]);
                    greceipthm.put("dateSuffixValue", row[20]);
                    if (row[21] != null) {
                        greceipthm.put("seqformat", row[21]);
                    }
                    greceipthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(openingBalanceAmountDue * exchangeRateForOpeningTransaction, (String) row[5]));
                    greceipthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(openingBalanceAmountDue * exchangeRateForOpeningTransaction, (String) row[5]));
                    greceipthm.put("accopeningbalancevendorinvoicecustomdataref", row[16]);

                    HashMap<String, Object> requestParamsCus = new HashMap<>();
                    requestParamsCus.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                    requestParamsCus.put("companyid", (String) row[5]);
                    String companyId = (String) row[5];
                    String jeId = (String) row[16];
                    KwlReturnObject savedFilesIdResult = accountingHandlerDAOobj.getFieldParamsFromFirstDB(requestParamsCus,dbName);
                    List<Object[]> savedFilesList = savedFilesIdResult.getEntityList();
                    /*
                     * Create Json for Custom Field.
                     */
                    JSONArray array = customFieldManupulation(savedFilesList, companyId, jeId,dbName);
                    resultObj = accGoodsReceiptobj.addGoodsReceipt(greceipthm);
                    GoodsReceipt gr = (GoodsReceipt) resultObj.getEntityList().get(0);
                    if (array.length() > 0) {
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", array);
                        customrequestParams.put("modulename", Constants.Acc_OpeningBalanceVendorInvoice_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceVendorInvoiceid);
                        customrequestParams.put("modulerecid", gr.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                        customrequestParams.put("companyid", (String) row[5]);
                        customrequestParams.put("customdataclasspath", "com.krawler.hql.accounting.OpeningBalanceVendorInvoiceCustomData");
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            greceipthm.put("grid", gr.getID());
                            greceipthm.put("openingBalanceVendorInvoiceCustomData", gr.getID());
                            accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                        }
                    }
                }
            }
        }catch(Exception ex){
             Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }
    
    public void createJsonForCreditNote(List <Object[]> list,JSONObject jsonParam){
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject resultObj = null;
        double openingBalanceAmountDue = 0.0;
        double exchangeRateForOpeningTransaction = 0.0;
        String dateFormat = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(dateFormat);
        String msg = "";
        String dbName=jsonParam.optString("dbname").toString().trim();
        try{
            if (list != null && list.size() > 0) {
                for (Object[] row : list) {
                    openingBalanceAmountDue = (double) row[1];
                    exchangeRateForOpeningTransaction = (double) row[10];
                    HashMap<String, Object> credithm = new HashMap<String, Object>();
                    KwlReturnObject result = accCreditNoteDAOobj.getCNFromNoteNo((String) row[0], (String) row[5]);
                    int count = result.getRecordTotalCount();
                    if (count > 0) {
                        continue;
                    }
                    credithm.put("entrynumber", row[0]);
                    if (row[19] != null && ((Character) row[19]) == 'T') {
                        credithm.put("autogenerated", true);
                    } else {
                        credithm.put("autogenerated", false);
                    }
                    credithm.put("cnamount", row[1]);//
                    credithm.put("currencyid", row[2]);//
                    credithm.put("externalCurrencyRate", row[3]);//
                    credithm.put("memo", row[4]);//
                    credithm.put(Constants.companyKey, row[5]);//
                    credithm.put("narrationValue", row[6]);//
                    credithm.put("creationDate", row[7]);//
                    if (row[8] != null) {
                        credithm.put("customerid", row[8]);//
                        credithm.put("isCNForCustomer", true);//
                    } else {
                        credithm.put("vendorid", row[12]);//
                        credithm.put("isCNForCustomer", false);//
                    }
                    credithm.put("accountId", row[9]);//
                    credithm.put("isOpeningBalenceCN", true);
                    credithm.put("normalCN", false);//
                    credithm.put("openingBalanceAmountDue", row[1]);//
                    credithm.put("exchangeRateForOpeningTransaction", row[10]);//
                    credithm.put("conversionRateFromCurrencyToBase", true);//
                    if (row[13] != null) {
                        credithm.put("salesperson", row[13]);//
                    }
                    // Store CN amount in base currency. conversionRateFromCurrencyToBase is always true for import case
                    credithm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(openingBalanceAmountDue * exchangeRateForOpeningTransaction, (String) row[5]));
                    credithm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(openingBalanceAmountDue * exchangeRateForOpeningTransaction, (String) row[5]));
                    credithm.put("openflag", true);
                    credithm.put("cnamountdue", row[1]);
                    String createdby = jsonParam.optString(Constants.useridKey);
                    String modifiedby = jsonParam.optString(Constants.useridKey);
                    long createdon = System.currentTimeMillis();
                    long updatedon = System.currentTimeMillis();
                    credithm.put("createdby", createdby);
                    credithm.put("modifiedby", modifiedby);
                    credithm.put("createdon", createdon);
                    credithm.put("updatedon", updatedon);
                    credithm.put("approvestatuslevel", 11);
                    credithm.put("seqnumber", row[14]);
                    credithm.put("datePreffixValue", row[15]);
                    credithm.put("dateAfterPreffixValue", row[16]);
                    credithm.put("dateSuffixValue", row[17]);;
                    if (row[18] != null) {
                        credithm.put("seqformat", row[18]);
                    }
                    HashMap<String, Object> requestParamsCus = new HashMap<>();
                    requestParamsCus.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                    requestParamsCus.put("companyid", (String) row[5]);
                    String companyId = (String) row[5];
                    String jeId = (String) row[11];
                    KwlReturnObject savedFilesIdResult = accountingHandlerDAOobj.getFieldParamsFromFirstDB(requestParamsCus,dbName);
                    List<Object[]> savedFilesList = savedFilesIdResult.getEntityList();
                    /*
                     * Create Json for Custom Field.
                     */
                    JSONArray array = customFieldManupulation(savedFilesList, companyId, jeId,dbName);
                    KwlReturnObject resultcn = accCreditNoteDAOobj.addCreditNote(credithm);
                    CreditNote cn = (CreditNote) resultcn.getEntityList().get(0);
                    HashSet<CreditNoteDetail> cndetails = new HashSet<CreditNoteDetail>();

                    getCNDetails(cndetails, companyId);

                    Iterator itr = cndetails.iterator();
                    while (itr.hasNext()) {
                        CreditNoteDetail cnd = (CreditNoteDetail) itr.next();
                        cnd.setCreditNote(cn);
                    }
                    credithm.put("cnid", cn.getID());
                    credithm.put("cndetails", cndetails);
                    credithm.put("otherwise", true);
                    accCreditNoteDAOobj.updateCreditNote(credithm);
                    if (array.length() > 0) {
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", array);
                        customrequestParams.put("modulename", Constants.Acc_OpeningBalanceCreditNote_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceCreditNoteid);
                        customrequestParams.put("modulerecid", cn.getID());
                        customrequestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                        customrequestParams.put(Constants.companyKey, companyId);
                        customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceCreditNote_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            credithm.put("cnid", cn.getID());
                            credithm.put("openingBalanceCreditNoteCustomData", cn.getID());
                            result = accCreditNoteDAOobj.updateCreditNote(credithm);
                        }
                    }
                }
            }
        }catch(Exception ex){
             Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }
    
    public void createJsonForDebitNote(List <Object[]> list,JSONObject jsonParam){
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject resultObj = null;
        double openingBalanceAmountDue = 0.0;
        double exchangeRateForOpeningTransaction = 0.0;
        String dateFormat = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(dateFormat);
        String dbName=jsonParam.optString("dbname").toString().trim();
        String msg = "";
        try{
            if (list != null && list.size() > 0) {
                for (Object[] row : list) {
                    openingBalanceAmountDue = (double) row[1];
                    exchangeRateForOpeningTransaction = (double) row[10];
                    HashMap<String, Object> debithm = new HashMap<String, Object>();
                    KwlReturnObject result = accDebitNoteobj.getDNFromNoteNo((String) row[0], (String) row[5]);
                    int count = result.getRecordTotalCount();
                    if (count > 0) {
                        continue;
                    }

                    HashMap<String, Object> dnhm = new HashMap<String, Object>();
                    debithm.put("entrynumber", row[0]);
                    if (row[19] != null && ((Character) row[19]) == 'T') {
                        debithm.put("autogenerated", true);
                    } else {
                        debithm.put("autogenerated", false);
                    }
                    debithm.put("dnamount", row[1]);//
                    debithm.put("currencyid", row[2]);//
                    debithm.put("externalCurrencyRate", row[3]);//
                    debithm.put("memo", row[4]);//
                    debithm.put(Constants.companyKey, row[5]);//
                    debithm.put("narrationValue", row[6]);//
                    debithm.put("creationDate", row[7]);//
                    if (row[8] != null) {
                        debithm.put("customerid", row[8]);//
                        debithm.put("isDNForVendor", false);//
                    } else {
                        debithm.put("vendorid", row[12]);//
                        debithm.put("isDNForVendor", true);//
                    }
                    debithm.put("accountId", row[9]);//
                    debithm.put("isOpeningBalenceDN", true);
                    debithm.put("normalDN", false);//
                    debithm.put("openingBalanceAmountDue", row[1]);//
                    debithm.put("exchangeRateForOpeningTransaction", row[10]);//
                    debithm.put("conversionRateFromCurrencyToBase", true);//
                    if (row[13] != null) {
                        debithm.put("salesperson", row[13]);//
                    }
                    debithm.put("seqnumber", row[14]);
                    debithm.put("datePreffixValue", row[15]);
                    debithm.put("dateAfterPreffixValue", row[16]);
                    debithm.put("dateSuffixValue", row[17]);
                    if (row[18] != null) {
                        debithm.put("seqformat", row[18]);
                    }
                    // Store CN amount in base currency. conversionRateFromCurrencyToBase is always true for import case
                    debithm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(openingBalanceAmountDue * exchangeRateForOpeningTransaction, (String) row[5]));
                    debithm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(openingBalanceAmountDue * exchangeRateForOpeningTransaction, (String) row[5]));
                    debithm.put("openflag", true);
                    debithm.put("dnamountdue", row[1]);
                    String createdby = jsonParam.optString(Constants.useridKey);
                    String modifiedby = jsonParam.optString(Constants.useridKey);
                    long createdon = System.currentTimeMillis();
                    long updatedon = System.currentTimeMillis();
                    debithm.put("createdby", createdby);
                    debithm.put("modifiedby", modifiedby);
                    debithm.put("createdon", createdon);
                    debithm.put("updatedon", updatedon);
                    debithm.put("approvestatuslevel", 11);
                    HashMap<String, Object> requestParamsCus = new HashMap<>();
                    requestParamsCus.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                    requestParamsCus.put("companyid", (String) row[5]);
                    String companyId = (String) row[5];
                    String jeId = (String) row[11];
                    KwlReturnObject savedFilesIdResult = accountingHandlerDAOobj.getFieldParamsFromFirstDB(requestParamsCus,dbName);
                    List<Object[]> savedFilesList = savedFilesIdResult.getEntityList();
                    /*
                     * Create Json for Custom Field.
                     */
                    JSONArray array = customFieldManupulation(savedFilesList, companyId, jeId,dbName);
                    KwlReturnObject resultdn = accDebitNoteobj.addDebitNote(debithm);
                    DebitNote dn = (DebitNote) resultdn.getEntityList().get(0);

                    HashSet<DebitNoteDetail> dndetails = new HashSet<DebitNoteDetail>();
                    getDNDetails(dndetails, companyId);


                    Iterator itr = dndetails.iterator();
                    while (itr.hasNext()) {
                        DebitNoteDetail dnd = (DebitNoteDetail) itr.next();
                        dnd.setDebitNote(dn);
                    }
                    debithm.put("dnid", dn.getID());
                    debithm.put("dndetails", dndetails);
                    debithm.put("otherwise", true);

                    result = accDebitNoteobj.updateDebitNote(debithm);
                    if (array.length() > 0) {
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", array);
                        customrequestParams.put("modulename", Constants.Acc_OpeningBalanceDebitNote_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceDebitNoteid);
                        customrequestParams.put("modulerecid", dn.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                        customrequestParams.put("companyid", companyId);
                        customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceDebitNote_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            debithm.put("dnid", dn.getID());
                            debithm.put("openingBalanceDebitNoteCustomData", dn.getID());
                            result = accDebitNoteobj.updateDebitNote(debithm);
                        }
                    }
                }
            }
        }catch(Exception ex){
             Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }
    
    public void createJsonForReceipt(List <Object[]> list,JSONObject jsonParam){
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject resultObj = null;
        double openingBalanceAmountDue = 0.0;
        double exchangeRateForOpeningTransaction = 0.0;
        String dateFormat = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(dateFormat);
        String msg = "";
        String dbName=jsonParam.optString("dbname").toString().trim();
        try{
             if (list != null && list.size() > 0) {
                for (Object[] row : list) {

                    openingBalanceAmountDue = (double) row[1];
                    if ((double) row[9] != 0) {
                        exchangeRateForOpeningTransaction = (double) row[9];
                    } else {
                        exchangeRateForOpeningTransaction = 1.0;
                    }
                    HashMap receipthm = new HashMap();
                    KwlReturnObject result1 = accReceiptDAOobj.getReceiptFromBillNo((String) row[0], (String) row[5]);
                    int count = result1.getRecordTotalCount();
                    if (count > 0) {
                        continue;
                    }
                    receipthm.put("entrynumber", row[0]);
                    if (row[23] != null && ((Character) row[23]) == 'T') {
                        receipthm.put("autogenerated", true);
                    } else {
                        receipthm.put("autogenerated", false);
                    }
                    receipthm.put("depositamount", row[1]);//
                    receipthm.put("currencyid", row[2]);//
                    receipthm.put("externalCurrencyRate", row[3]);//
                    receipthm.put("memo", row[4]);//
                    receipthm.put("companyid", row[5]);//
                    receipthm.put("creationDate", row[6]);//
                    if (row[7] == null) {
                        continue;
                    }
                    receipthm.put("customerId", row[7]);//
                    receipthm.put("accountId", row[8]);//
                    receipthm.put("isOpeningBalenceReceipt", true);//
                    receipthm.put("normalReceipt", false);//
                    receipthm.put("openingBalanceAmountDue", row[1]);//
                    receipthm.put("isadvancepayment", true);
                    receipthm.put("contraentry", false);
                    receipthm.put("seqnumber", row[18]);
                    receipthm.put("datePreffixValue", row[19]);
                    receipthm.put("dateAfterPreffixValue", row[20]);
                    receipthm.put("dateSuffixValue", row[21]);
                    if (row[22] != null) {
                        receipthm.put("seqformat", row[22]);
                    }
                    receipthm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);
                    receipthm.put("conversionRateFromCurrencyToBase", true);
                    // Store Receipt amount in base currency
                    receipthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(openingBalanceAmountDue * exchangeRateForOpeningTransaction, (String) row[5]));
                    receipthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(openingBalanceAmountDue * exchangeRateForOpeningTransaction, (String) row[5]));
                    String createdby = jsonParam.optString(Constants.useridKey);
                    String modifiedby = jsonParam.optString(Constants.useridKey);
                    long createdon = System.currentTimeMillis();
                    long updatedon = System.currentTimeMillis();

                    receipthm.put("createdby", createdby);
                    receipthm.put("modifiedby", modifiedby);
                    receipthm.put("createdon", createdon);
                    receipthm.put("updatedon", updatedon);
                    HashMap pdetailhm = new HashMap();
                    pdetailhm.put("paymethodid", row[17]);
                    pdetailhm.put("companyid", row[5]);
                    HashMap<String, Object> requestParamsCus = new HashMap<>();
                    requestParamsCus.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                    requestParamsCus.put("companyid", (String) row[5]);
                    String companyId = (String) row[5];
                    String jeId = (String) row[11];
                    KwlReturnObject savedFilesIdResult = accountingHandlerDAOobj.getFieldParamsFromFirstDB(requestParamsCus,dbName);
                    List<Object[]> savedFilesList = savedFilesIdResult.getEntityList();
                    /*
                     * Create Json for Custom Field.
                     */
                    JSONArray array = customFieldManupulation(savedFilesList, companyId, jeId,dbName);

                    if (row[10] != null && !((String) row[10]).equalsIgnoreCase("Cash")) {
//                            if (row[10] == PaymentMethod.TYPE_BANK) {
                        HashMap chequehm = new HashMap();
                         if (row[11] != null) {
                            chequehm.put("chequeno", row[11]);
                        }
                        if (row[5] != null) {
                            chequehm.put("companyId", row[5]);
                        }
                        chequehm.put("createdFrom", 2);
                        if (row[12] != null) {
                            chequehm.put("bankAccount", row[12]);
                        }
                        if (row[13] != null) {
                            chequehm.put("description", row[13]);
                        }
                        if (row[14] != null) {
                            chequehm.put("bankname", row[14]);
                        }
                        if (row[15] != null) {
                            chequehm.put("duedate", row[15]);

                        }
                        if (row[16] != null) {
                            chequehm.put("bankmasteritemid", row[16]);
                        }
                        KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                        Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                        pdetailhm.put("chequeid", cheque.getID());
//                            } 
                    }

                    KwlReturnObject pdresult = null;

                    pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);
                    PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);

                    receipthm.put("paydetailsid", pdetail.getID());
                    KwlReturnObject result = accReceiptDAOobj.saveReceipt(receipthm);
                    Receipt receipt = (Receipt) result.getEntityList().get(0);
                    if (array.length() > 0) {
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", array);
                        customrequestParams.put("modulename", Constants.Acc_OpeningBalanceReceipt_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceReceiptid);
                        customrequestParams.put("modulerecid", receipt.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                        customrequestParams.put("companyid", companyId);
                        customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceReceipt_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            receipthm.put("receiptid", receipt.getID());
                            receipthm.put("openingBalanceReceiptCustomData", receipt.getID());
                            result = accReceiptDAOobj.saveReceipt(receipthm);
                        }
                    }
                }
            }
        }catch(Exception ex){
             Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }
    
    public void createJsonForPayment(List <Object[]> list,JSONObject jsonParam){
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject resultObj = null;
        double openingBalanceAmountDue = 0.0;
        double exchangeRateForOpeningTransaction = 0.0;
        String dateFormat = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(dateFormat);
        String dbName=jsonParam.optString("dbname").toString().trim();
        String msg = "";
        try{
          if (list != null && list.size() > 0) {
                for (Object[] row : list) {

                    openingBalanceAmountDue = (double) row[1];
                    if ((double) row[9] != 0) {
                        exchangeRateForOpeningTransaction = (double) row[9];
                    } else {
                        exchangeRateForOpeningTransaction = 1.0;
                    }
                    HashMap receipthm = new HashMap();
                    KwlReturnObject result1 = accVendorPaymentobj.getPaymentFromNo((String) row[0], (String) row[5]);
                    int count = result1.getRecordTotalCount();
                    if (count > 0) {
                        continue;
                    }
                    receipthm.put("entrynumber", row[0]);
                    if (row[23] != null && ((Character) row[23]) == 'T') {
                        receipthm.put("autogenerated", true);
                    } else {
                        receipthm.put("autogenerated", false);
                    }
                    receipthm.put("depositamount", row[1]);//
                    receipthm.put("currencyid", row[2]);//
                    receipthm.put("externalCurrencyRate", row[3]);//
                    receipthm.put("memo", row[4]);//
                    receipthm.put("companyid", row[5]);//
                    receipthm.put("creationDate", row[6]);//
                    if (row[7] == null) {
                        continue;
                    }
                    receipthm.put("vendorId", row[7]);//
                    receipthm.put("accountId", row[8]);//
                    receipthm.put("isOpeningBalencePayment", true);//
                    receipthm.put("normalPayment", false);//
                    receipthm.put("openingBalanceAmountDue", row[1]);//
                    receipthm.put("isadvancepayment", true);
                    receipthm.put("contraentry", false);
                    receipthm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);
                    receipthm.put("conversionRateFromCurrencyToBase", true);
                    // Store Receipt amount in base currency
                    receipthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(openingBalanceAmountDue * exchangeRateForOpeningTransaction, (String) row[5]));
                    receipthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(openingBalanceAmountDue * exchangeRateForOpeningTransaction, (String) row[5]));
                    String createdby =jsonParam.optString(Constants.useridKey);
                    String modifiedby =jsonParam.optString(Constants.useridKey);
                    long createdon = System.currentTimeMillis();
                    long updatedon = System.currentTimeMillis();

                    receipthm.put("createdby", createdby);
                    receipthm.put("modifiedby", modifiedby);
                    receipthm.put("createdon", createdon);
                    receipthm.put("updatedon", updatedon);
                    HashMap pdetailhm = new HashMap();
                    pdetailhm.put("paymethodid", row[17]);
                    pdetailhm.put("companyid", row[5]);
                    receipthm.put("seqnumber", row[18]);
                    receipthm.put("datePreffixValue", row[19]);
                    receipthm.put("dateAfterPreffixValue", row[20]);
                    receipthm.put("dateSuffixValue", row[21]);
                      if (row[22] != null) {
                        receipthm.put("seqformat", row[22]);
                    }
                    HashMap<String, Object> requestParamsCus = new HashMap<>();
                    requestParamsCus.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                    requestParamsCus.put("companyid", (String) row[5]);
                    String companyId = (String) row[5];
                    String jeId = (String) row[11];
                    KwlReturnObject savedFilesIdResult = accountingHandlerDAOobj.getFieldParamsFromFirstDB(requestParamsCus,dbName);
                    List<Object[]> savedFilesList = savedFilesIdResult.getEntityList();
                    /*
                     * Create Json for Custom Field.
                     */
                    JSONArray array = customFieldManupulation(savedFilesList, companyId, jeId,dbName);

                    if (row[10]!=null && !((String) row[10]).equalsIgnoreCase("Cash")) {
//                            if (row[10] == PaymentMethod.TYPE_BANK) {
                        HashMap chequehm = new HashMap();
                        
                        if (row[11] != null) {
                            chequehm.put("chequeno", row[11]);
                        }
                        if (row[5] != null) {
                            chequehm.put("companyId", row[5]);
                        }
                        chequehm.put("createdFrom", 2);
                        if (row[12] != null) {
                            chequehm.put("bankAccount", row[12]);
                        }
                        if (row[13] != null) {
                            chequehm.put("description", row[13]);
                        }
                        if (row[14] != null) {
                            chequehm.put("bankname", row[14]);
                        }
                        if (row[15] != null) {
                            chequehm.put("duedate", row[15]);

                        }
                        if (row[16] != null) {
                            chequehm.put("bankmasteritemid", row[16]);
                        }
                        KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                        Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                        pdetailhm.put("chequeid", cheque.getID());
//                            } 
                    }

                    KwlReturnObject pdresult = null;

                    pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);
                    PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);

                    receipthm.put("paydetailsid", pdetail.getID());


                    KwlReturnObject result = accVendorPaymentobj.savePayment(receipthm);
                    Payment payment = (Payment) result.getEntityList().get(0);
                    if (array.length() > 0) {
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", array);
                        customrequestParams.put("modulename", Constants.Acc_OpeningBalanceMakePayment_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceMakePaymentid);
                        customrequestParams.put("modulerecid", payment.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                        customrequestParams.put("companyid", companyId);
                        customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceMakePayment_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            receipthm.put("paymentid", payment.getID());
                            receipthm.put("openingBalanceMakePaymentCustomData", payment.getID());
                            result = accVendorPaymentobj.savePayment(receipthm);
                        }
                    }
                }
            }  
        }catch(Exception ex){
             Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }
    
    

}
