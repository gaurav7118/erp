/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 */
package com.krawler.spring.accounting.purchaseorder.service;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderController;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderService;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.CommonFnControllerService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.sun.org.apache.bcel.internal.classfile.Utility;
import java.io.*;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.support.RequestContextUtils;

public class AccPurchaseOrderModuleServiceImpl implements AccPurchaseOrderModuleService, MessageSourceAware {

    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private auditTrailDAO auditTrailObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private fieldDataManager fieldDataManagercntrl;
    private accProductDAO accProductObj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private ImportHandler importHandler;
    private ImportDAO importDao;
    private accSalesOrderService accSalesOrderServiceobj;
    private accVendorDAO accVendorDAOObj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccCostCenterDAO accCostCenterObj;
    private accDiscountDAO accDiscountobj;
    private CommonFnControllerService commonFnControllerService;
    private permissionHandlerDAO permissionHandlerDAOObj;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }

    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setAccSalesOrderServiceobj(accSalesOrderService accSalesOrderServiceobj) {
        this.accSalesOrderServiceobj = accSalesOrderServiceobj;
    }

    public void setAccVendorDAO(accVendorDAO accVendorDAOObj) {
        this.accVendorDAOObj = accVendorDAOObj;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setaccCostCenterDAO(AccCostCenterDAO accCostCenterDAOObj) {
        this.accCostCenterObj = accCostCenterDAOObj;
    }
    
    public void setaccDiscountDAO(accDiscountDAO accDiscountobj) {
        this.accDiscountobj = accDiscountobj;
    }
    
    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }
    
    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }
    
    

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class})
    protected JSONObject checkDuplicateNumber(String entryNumber, String companyid, String sequenceformat, JSONObject paramJobj, boolean accexception) {
        JSONObject returnJobj = new JSONObject();
        boolean accExceptionFlag = accexception;
        try {
            KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Purchase_Order_ModuleId);
            if (resultInv.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                accExceptionFlag = true;
                throw new AccountingException(messageSource.getMessage("acc.PO.selectedPONo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            } else {
                accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Purchase_Order_ModuleId);
            }
        } catch (AccountingException ex) {
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
        } finally {
            try {
                returnJobj.put("isException", accExceptionFlag);
            } catch (JSONException ex) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return returnJobj;
    }
    
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class})
    protected JSONObject updatePOEntryNumber(String poid, String sequenceformat, String companyid, PurchaseOrder purchaseOrder, String billno, String billid, String entryNumber, String currentUser, boolean isEdit, List mailParams, String pageURL, boolean isDraft, boolean isSaveDraftRecord, boolean isAutoSeqForEmptyDraft) {
        JSONObject returnJobj = new JSONObject();
        String updatedPONumber = billno;
        try {
            if (StringUtil.isNullOrEmpty(poid) && (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA"))) { // Post New JE with auto generated Entry No.
                boolean seqformat_oldflag = false; // old flag was used when sequence format not implemented.StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                String nextAutoNo = "";
                Map<String, Object> seqNumberMap = new HashMap<>();
                if (seqformat_oldflag) {
                    nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_PURCHASEORDER, sequenceformat);
                    seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNo);
                } else {
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PURCHASEORDER, sequenceformat, seqformat_oldflag, purchaseOrder.getOrderDate());
                }

                seqNumberMap.put(Constants.DOCUMENTID, billid);
                seqNumberMap.put(Constants.companyKey, companyid);
                seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                updatedPONumber = accPurchaseOrderobj.updatePOEntryNumberForNewPO(seqNumberMap);
            } else if (isSaveDraftRecord && !sequenceformat.equals("NA") && isAutoSeqForEmptyDraft) {  //ERM-1238 (Reference - SDP-13487) : Do not update Draft No. in case of Sequence Format as "NA",  //SDP-13927 : If Draft already having sequence no. then do not update it
                 /*
                 Below piece of code has written to handle Auto-Sequence no.in edit mode.
                 When user open the draft in edit mode, he can save it as a draft or a transaction. If it save as draft again then this code will not be execute.
                 But, if he saves it as a transaction then this code will be execute to get the Auto-Sequence No and set it to transaction no.
                 */
//                String nextAutoNumber = "";
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PURCHASEORDER, sequenceformat, false, purchaseOrder.getOrderDate());
                seqNumberMap.put(Constants.DOCUMENTID, billid);
                seqNumberMap.put(Constants.companyKey, companyid);
                seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                seqNumberMap.put(Constants.isDraft, isDraft);
                updatedPONumber = accPurchaseOrderobj.updatePOEntryNumberForNewPO(seqNumberMap);
            } else if (isDraft && !sequenceformat.equals("NA") && isAutoSeqForEmptyDraft) {
                /* ERM-1238 (Reference - SDP-13923)
                 This piece of code has been written to fix below case.
                 1)Draft has been made with NA. 2)Draft has opened in edit mode and saved as a draft again with Auto-Sequence Format.
                 3)Again draft opened in edit mode then sequence format should be Auto-Sequence Format.
                 */
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PURCHASEORDER, sequenceformat, false, purchaseOrder.getOrderDate());
                seqNumberMap.put(Constants.DOCUMENTID, billid);
                seqNumberMap.put(Constants.companyKey, companyid);
                seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                seqNumberMap.put(Constants.isDraft, isDraft);
                updatedPONumber = accPurchaseOrderobj.updatePOEntryNumberForNewPO(seqNumberMap);
            }

            /*
             * This block is executed if any PO will go for pending approval &
             * mail wil be sent to admin
             */
            if (mailParams != null && !mailParams.isEmpty()) {
                /**
                 * parameters required for sending mail
                 */
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put(Constants.companyid, companyid);
                mailParameters.put(Constants.prNumber, billno);
                mailParameters.put(Constants.moduleid, Constants.Acc_Purchase_Order_ModuleId);
                mailParameters.put(Constants.isCash, false);
                mailParameters.put(Constants.createdBy, currentUser);
                mailParameters.put(Constants.isEdit, isEdit);
                mailParameters.put(Constants.PAGE_URL, pageURL);
                Iterator itr = mailParams.iterator();

                while (itr.hasNext()) {
                    HashMap<String, Object> paramsMap = (HashMap<String, Object>) itr.next();

                    mailParameters.put(Constants.ruleid, (String) paramsMap.get(Constants.ruleid));
                    mailParameters.put(Constants.fromName, (String) paramsMap.get(Constants.fromName));
                    mailParameters.put(Constants.hasApprover, (Boolean) paramsMap.get(Constants.hasApprover));
                    /*
                     * Method is used for sending mail to admin
                     */
                    sendMailToApprover(mailParameters);
                }
            }

        } catch (Exception ex) {
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Purchase_Order_ModuleId);
            } catch (ServiceException e) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                returnJobj.put("billno", updatedPONumber);
            } catch (JSONException ex) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return returnJobj;
    }
    
    @Override
   @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class,Exception.class})
    public JSONObject savePurchaseOrderJSON(JSONObject paramJobj) throws AccountingException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        int approvalStatusLevel = 11;
        String billid = "";
        int nocount = 0;
        String billno = "", moduleName = "Purchase Order";
        String amount = "";
        String channelName = "";
        String butPendingForApproval = "";
        boolean accexception = false;
        boolean isTaxDeactivated = false;
        String entryNumber = paramJobj.optString("number", null);
        String companyid = "";
        KwlReturnObject result = null;
        try {
            String poid = paramJobj.optString("invoiceid", null);
            companyid = paramJobj.getString(Constants.companyKey);
            String sequenceformat = paramJobj.optString("sequenceformat", null);
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv", null)) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString("isFixedAsset", null))) ? Boolean.parseBoolean(paramJobj.getString("isFixedAsset")) : false;
            boolean isConsignment = paramJobj.optString("isConsignment", null) != null ? Boolean.parseBoolean(paramJobj.getString("isConsignment")) : false;
            boolean isMRPJOBWORKOUT = paramJobj.optString("isMRPJOBWORKOUT", null) != null ? Boolean.parseBoolean(paramJobj.getString("isMRPJOBWORKOUT")) : false;
            String currentUser = paramJobj.getString(Constants.useridKey);
            boolean isDraft = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isDraft, null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isDraft)) : false;
            boolean isSaveDraftRecord = (!StringUtil.isNullOrEmpty(paramJobj.optString("isSaveDraftRecord", null))) ? Boolean.parseBoolean(paramJobj.getString("isSaveDraftRecord")) : false; //SDP-13487
            boolean isAutoSeqForEmptyDraft = (!StringUtil.isNullOrEmpty(paramJobj.optString("isAutoSeqForEmptyDraft", null))) ? Boolean.parseBoolean(paramJobj.getString("isAutoSeqForEmptyDraft")) : false; //SDP-13927 : If Draft already having sequence no. then do not update it
            String fromLinkCombo = paramJobj.optString("fromLinkCombo", null) != null ? paramJobj.getString("fromLinkCombo") : "";
            boolean isEditedPendingDocument = StringUtil.isNullOrEmpty(paramJobj.optString("isEditedPendingDocument", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEditedPendingDocument"));
            /**
             * createAsTransactionChkboxwithTemplate- used to create template along with transaction.
             */
            boolean createAsTransactionChkboxwithTemplate = paramJobj.optString("createAsTransactionChkbox").equalsIgnoreCase("on") ? true : false;
            String additionalsauditmessage = "";
            if (!StringUtil.isNullOrEmpty(poid)) { // In edit case checks duplicate number
                result = accPurchaseOrderobj.getPOEditCount(entryNumber, companyid, poid);
                nocount = result.getRecordTotalCount();
                if (nocount > 0 && sequenceformat.equals("NA")) {
                    accexception = true;
                    throw new AccountingException(messageSource.getMessage("acc.PO.purchaseorderno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
            } else { // In add case check duplicate number
                if (!StringUtil.isNullOrEmpty(entryNumber) && entryNumber!="") {
                    result = accPurchaseOrderobj.getPOCount(entryNumber, companyid);
                }
                if (result != null && result.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    accexception = true;
                    throw new AccountingException(messageSource.getMessage("acc.PO.purchaseorderno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
                //Check Deactivate Tax in New Transaction.
                if (!fieldDataManagercntrl.isTaxActivated(paramJobj)) {
                    isTaxDeactivated = true;
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }
            }
            synchronized (this) { // Checks duplicate number for simultaneous transactions
                JSONObject checkDuplicateJson = checkDuplicateNumber(entryNumber, companyid, sequenceformat, paramJobj, accexception);
                if (checkDuplicateJson.has("isException") && checkDuplicateJson.get("isException") != null) {
                    accexception = checkDuplicateJson.optBoolean("isException");
                }
            }
           /**
             * creating template for purchase order.
             * istemplate=2 //creating only template
             * istemplate=0 //creating only transaction
             */
            if (createAsTransactionChkboxwithTemplate) {
                paramJobj.put("istemplate", 2);
                savePurchaseOrder(paramJobj);
                paramJobj.remove("istemplate");
            }
             /**
             * creating purchase order.
             */
            List li = savePurchaseOrder(paramJobj);
            List mailParams = (List) li.get(5);
            PurchaseOrder purchaseOrder = (PurchaseOrder) li.get(0);
            String linkedDocuments = (String) li.get(7);
            String unlinkMessage = (String) li.get(8);
            String roleName = li.get(9) != null ? (String) li.get(9) : "";
            boolean isAuthorityToApprove = li.get(10) != null ? (Boolean) li.get(10) : false;
            boolean sendPendingDocumentsToNextLevel = li.get(11) != null ? (Boolean) li.get(11) : false;

            billid = purchaseOrder.getID();
            billno = purchaseOrder.getPurchaseOrderNumber();
            if (li.get(1) != null) { // fields updated
                additionalsauditmessage = li.get(1).toString();
            }
            issuccess = true;
            double totalAmount = 0;

            if (li.get(2) != null) { // totalAmount
                totalAmount = Double.parseDouble(li.get(2).toString());
            }
            if (li.get(3) != null) {// approvalStatusLevel 
                approvalStatusLevel = Integer.parseInt(li.get(3).toString());
            }
            if (li.get(4) != null) {//butPendingForApproval 
                butPendingForApproval = li.get(4).toString();
            }
            
            //Get mapping details id of invoice documents
            String savedFilesMappingId = paramJobj.optString("savedFilesMappingId", "");
            if(!StringUtil.isNullOrEmpty(savedFilesMappingId)){
                /**
                * Save temporary saved attachment files mapping in permanent table
                */
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("savedFilesMappingId", savedFilesMappingId);
                jsonObj.put("docId", billid);
                jsonObj.put("companyid", companyid);
                accSalesOrderServiceobj.saveDocuments(jsonObj);
            }
            issuccess = true;
            try {
                synchronized (this) {
                    JSONObject updatedPOJson = updatePOEntryNumber(poid, sequenceformat, companyid, purchaseOrder, billno, billid,entryNumber,currentUser,isEdit,mailParams, paramJobj.optString(Constants.PAGE_URL),isDraft,isSaveDraftRecord,isAutoSeqForEmptyDraft);
                    if (updatedPOJson.has("billno") && updatedPOJson.get("billno") != null) {
                        billno = updatedPOJson.optString("billno");
                    }
                }
            } catch (Exception ex) {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Purchase_Order_ModuleId);
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            int istemplate = 0;
            String auditSMS = "";
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("istemplate", null))) {
                istemplate = Integer.parseInt((String) paramJobj.get("istemplate"));
            }
            String template = " template for record ";
            if (istemplate == 0) {
                template = "";
            }
            String action = "added new";
            if (isEdit == true && isCopy == false) {
                action = "updated";
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isConsignment", null))) {
                isConsignment = Boolean.parseBoolean(paramJobj.getString("isConsignment"));
            }
            if (isConsignment) {
                moduleName = "Vendor Consignment Request";
            }
            /*
             * Preparing Audit trial message if document is linking at teh time
             * of creating
             */
            String linkingMessages = "";
            if (!StringUtil.isNullOrEmpty(linkedDocuments) && !StringUtil.isNullOrEmpty(fromLinkCombo)) {
                linkingMessages = " by Linking to " + fromLinkCombo + " " + linkedDocuments;
            }
            
            if (istemplate == 1) {
                msg = messageSource.getMessage("acc.field.PurchaseOrderandTemplatehasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + billno + "</b>";
                auditSMS = " has " + action + template + " " + moduleName + " " + billno + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + additionalsauditmessage;
            } else if (istemplate == 2) {
                msg = messageSource.getMessage("acc.field.PurchaseOrderTemplatehasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                auditSMS = " has " + action + template + " " + moduleName + " " + billno + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + additionalsauditmessage;
            } else if (isConsignment) {
                msg = messageSource.getMessage("acc.venconsignment.order.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + billno + "</b>";   //"consignment saved successfully";
                auditSMS = " has " + action + template + " " + moduleName + " " + billno + linkingMessages + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + additionalsauditmessage;
            } else if (isFixedAsset) {
                moduleName = Constants.ASSET_PURCHASE_ORDER;
                msg = messageSource.getMessage("acc.field.assetPurchaseOrderHasBeenSavedSuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + billno + "</b>";
                auditSMS = " has " + action + template + " " + moduleName + " " + billno + linkingMessages + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + additionalsauditmessage;
            } else if (isMRPJOBWORKOUT) {
                moduleName = Constants.ASSET_PURCHASE_ORDER;
                msg = messageSource.getMessage("acc.field.jobworkoutHasBeenSavedSuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + billno + "</b>";
                auditSMS = " has " + action + template + " " + moduleName + " " + billno + linkingMessages + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + additionalsauditmessage;
            } else if(isDraft){   
                if(StringUtil.isNullOrEmpty(billno)){
                    msg = messageSource.getMessage("acc.field.PurchaseOrderDraft", null, Locale.forLanguageTag(paramJobj.getString("language"))) +" "+messageSource.getMessage("acc.draft.success.msg.hasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));                    
                } else {
                    msg = messageSource.getMessage("acc.field.PurchaseOrderDraft", null, Locale.forLanguageTag(paramJobj.getString("language"))) +" <b>"+billno+"</b> "+messageSource.getMessage("acc.draft.success.msg.hasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));                    
                }
            } else {
                msg = messageSource.getMessage("acc.po.save1", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                msg += (StringUtil.isNullOrEmpty(butPendingForApproval) ? "." : "") + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + billno + "</b>";   //"Purchase order has been saved successfully";
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("createAsTransactionChkbox", "")) && paramJobj.optString("createAsTransactionChkbox").equalsIgnoreCase("on") && !StringUtil.isNullOrEmpty(paramJobj.optString("templatename", ""))) {
                    msg += " Template Name: <b>" + paramJobj.optString("templatename", "") + "</b>";
                }
                auditSMS = " has " + action + template + " " + moduleName + " " + billno + linkingMessages + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + additionalsauditmessage;
            }

            /*
             * Inserting Entry in Audit trial when any document is unlinking
             * through Edit
             */
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
            if (!StringUtil.isNullOrEmpty(unlinkMessage)) {
                auditTrailObj.insertAuditLog(action, "User " + paramJobj.optString(Constants.userfullname) + " has unlinked " + "Purchase Order " + billno + unlinkMessage + ".", auditRequestParams, billno);
            }
            auditTrailObj.insertAuditLog(AuditAction.PURCHASE_REQUISITION_CREATED, "User " + paramJobj.optString(Constants.userfullname) + auditSMS, auditRequestParams, purchaseOrder.getID());
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("createAsTransactionChkbox", "")) && paramJobj.optString("createAsTransactionChkbox").equalsIgnoreCase("on") && !StringUtil.isNullOrEmpty(paramJobj.optString("templatename", ""))) {
                auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has added Document Template "+paramJobj.optString("templatename", "")+ " for record Purchase Order" , auditRequestParams, purchaseOrder.getID());
            }
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Purchase_Order_ModuleId);
            jobj.put("billid", billid);
            jobj.put("billno", billno);
            jobj.put("amount", amount);
            jobj.put("isExpenseInv", purchaseOrder.isIsExpenseType());
            jobj.put("fullShippingAddress", li.get(6));

            if (isFixedAsset) {
                channelName = "/FixedAssetPurchaseOrderList/gridAutoRefresh";
            } else if (!(isConsignment)) { // For normal PO
                channelName = "/PurchaseOrderReport/gridAutoRefresh";
            }
            
           
            /*------Code if we edit pending document---------  */
            if (isEditedPendingDocument) {

                /*--If check "Send pending documents to next level" is activated from system preferences---------  */
                if (sendPendingDocumentsToNextLevel) {

                    if (roleName != "" && isAuthorityToApprove) {

                        /*----Prepare Messages and inset AuditLog for approval document------  */
                        if (isFixedAsset) {
                            msg += "<br>";
                            msg += messageSource.getMessage("acc.field.AssetPurchaseOrderhasbeenapprovedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " by " + roleName + " " + paramJobj.optString(Constants.userfullname) + " at Level " + purchaseOrder.getApprovestatuslevel() + ".";

                            auditTrailObj.insertAuditLog(AuditAction.PURCHASE_ORDER, "User " + paramJobj.optString(Constants.userfullname) + " has Approved a Asset Purchase Order " + purchaseOrder.getPurchaseOrderNumber() + " at Level-" + purchaseOrder.getApprovestatuslevel(), auditRequestParams, purchaseOrder.getID());
                        } else {
                            msg += "<br>";
                            msg += messageSource.getMessage("acc.field.PurchaseOrderhasbeenapprovedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " by " + roleName + " " + paramJobj.optString(Constants.userfullname) + " at Level " + purchaseOrder.getApprovestatuslevel() + ".";

                            auditTrailObj.insertAuditLog(AuditAction.PURCHASE_ORDER, "User " + paramJobj.optString(Constants.userfullname) + " has Approved a Purchase Order " + purchaseOrder.getPurchaseOrderNumber() + " at Level-" + purchaseOrder.getApprovestatuslevel(), auditRequestParams, purchaseOrder.getID());
                        }
                    } else {//If User have no authority to approve the document
                        msg += "<br>";
                        msg += messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + purchaseOrder.getApprovestatuslevel() + ".";
                    }
                } else if(!isAuthorityToApprove && butPendingForApproval==""){//If user have no authority to approve document
                        msg += "<br>";
                        msg += messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + purchaseOrder.getApprovestatuslevel() + " and record will be available at this level for approval"+"."; 
                }
            }
       
                        
        } catch (AccountingException ex) {
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Purchase_Order_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            try {
                paramJobj.put("isAccountingExe", accexception);
            } catch (JSONException ex1) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
            throw new AccountingException(ex.getMessage());
//            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Purchase_Order_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }

            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isAccountingExe", accexception);
                jobj.put("pendingApproval", approvalStatusLevel != 11);
                jobj.put(Constants.channelName, channelName);
                jobj.put(Constants.isTaxDeactivated, isTaxDeactivated);
            } catch (JSONException ex) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }  
    
    @Override
    public JSONObject saveSecurityGateEntryJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        int pendingapproval = 0;
        int approvalStatusLevel = 11;
        String billid = "";
        int nocount = 0;
        String billno = "", moduleName = "Security Gate Entry";
        String amount = "";
        String channelName = "";
        String butPendingForApproval = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        boolean accexception = false;
        String entryNumber = paramJobj.optString("number", null);
        String companyid = "";
        KwlReturnObject result = null;
        try {
            String securityid = paramJobj.optString("invoiceid", null);
            companyid = paramJobj.getString(Constants.companyKey);
            String sequenceformat = paramJobj.optString("sequenceformat", null);
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv", null)) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            String currentUser = paramJobj.getString(Constants.useridKey);
            String fromLinkCombo = paramJobj.optString("fromLinkCombo", null) != null ? paramJobj.getString("fromLinkCombo") : "";
            String additionalsauditmessage = "";
            if (!StringUtil.isNullOrEmpty(securityid)) { // In edit case checks duplicate number
                result = accPurchaseOrderobj.getSGEEditCount(entryNumber, companyid, securityid);
                nocount = result.getRecordTotalCount();
                if (nocount > 0 && sequenceformat.equals("NA")) {
                    accexception = true;
                    throw new AccountingException(messageSource.getMessage("acc.security.securitygateentryno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
            } else { // In add case check duplicate number
                if (!StringUtil.isNullOrEmpty(entryNumber) && entryNumber != "") {
                    result = accPurchaseOrderobj.getSecurityEntryCount(entryNumber, companyid);
                }
                if (result != null && result.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    accexception = true;
                    throw new AccountingException(messageSource.getMessage("acc.security.securitygateentryno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
            }
            synchronized (this) { // Checks duplicate number for simultaneous transactions
                status = txnManager.getTransaction(def);
                KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_SecurityGateEntry_ModuleId);
                if (resultInv.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    accexception = true;
                    throw new AccountingException(messageSource.getMessage("acc.security.selectedSGENo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                } else {
                    accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_SecurityGateEntry_ModuleId);
                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);

            List li = saveSecurityGateEntry(paramJobj);
            List mailParams = (List) li.get(5);
            SecurityGateEntry securityGateEntry = (SecurityGateEntry) li.get(0);
            String linkedDocuments = (String) li.get(7);
            String unlinkMessage = (String) li.get(8);

            billid = securityGateEntry.getID();
            billno = securityGateEntry.getSecurityNumber();
            if (li.get(1) != null) { // fields updated
                additionalsauditmessage = li.get(1).toString();
            }
            issuccess = true;
            double totalAmount = 0;

            if (li.get(2) != null) { // totalAmount
                totalAmount = Double.parseDouble(li.get(2).toString());
            }
            if (li.get(3) != null) {// approvalStatusLevel 
                approvalStatusLevel = Integer.parseInt(li.get(3).toString());
            }
            if (li.get(4) != null) {//butPendingForApproval 
                butPendingForApproval = li.get(4).toString();
            }
            boolean pendingApprovalFlag = false;
            issuccess = true;
            txnManager.commit(status);
            TransactionStatus AutoNoStatus = null;
            try {
                synchronized (this) {
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("AutoNum_Tx");
                    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    AutoNoStatus = txnManager.getTransaction(def1);

                    if (StringUtil.isNullOrEmpty(securityid) && (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA"))) { // Post New JE with auto generated Entry No.
                        boolean seqformat_oldflag = false; // old flag was used when sequence format not implemented.StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                        String nextAutoNo = "";
                        String nextAutoNoInt = "";
                        Map<String, Object> seqNumberMap = new HashMap<>();
                        if (seqformat_oldflag) {
                            nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_SECURITYNO, sequenceformat);
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNo);
                        } else {
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_SECURITYNO, sequenceformat, seqformat_oldflag, securityGateEntry.getSecurityDate());
                        }

                        seqNumberMap.put(Constants.DOCUMENTID, billid);
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        billno = accPurchaseOrderobj.updatePOEntryNumberForNewSecurityGateNo(seqNumberMap);
                    }
                }
                if (mailParams != null && !mailParams.isEmpty()) {
                    Iterator itr = mailParams.iterator();
                    /**
                     * parameters required for sending mail
                     */
                    Map<String, Object> mailParameters = new HashMap();
                    mailParameters.put(Constants.companyid, companyid);
                    mailParameters.put(Constants.prNumber, billno);
                    mailParameters.put(Constants.moduleid, Constants.Acc_SecurityGateEntry_ModuleId);
                    mailParameters.put(Constants.isCash, false);
                    mailParameters.put(Constants.createdBy, currentUser);
                    mailParameters.put(Constants.isEdit, isEdit);
                    mailParameters.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));
                    while (itr.hasNext()) {
                        HashMap<String, Object> paramsMap = (HashMap<String, Object>) itr.next();
                        mailParameters.put(Constants.ruleid, (String) paramsMap.get("ruleid"));
                        mailParameters.put(Constants.fromName, (String) paramsMap.get("fromName"));
                        mailParameters.put(Constants.hasApprover, (Boolean) paramsMap.get("hasApprover"));
                        sendMailToApprover(mailParameters);
                    }
                }
                if (AutoNoStatus != null) {
                    txnManager.commit(AutoNoStatus);
                }

            } catch (Exception ex) {
                if (AutoNoStatus != null) {
                    txnManager.rollback(AutoNoStatus);
                    accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_SecurityGateEntry_ModuleId);
                    Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            int istemplate = 0;
            String auditSMS = "";
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("istemplate", null))) {
                istemplate = Integer.parseInt((String) paramJobj.get("istemplate"));
            }
            String template = " template for record ";
            if (istemplate == 0) {
                template = "";
            }
            String action = "added new";
            if (isEdit == true && isCopy == false) {
                action = "updated";
            }
            String linkingMessages = "";
            if (!StringUtil.isNullOrEmpty(linkedDocuments) && !StringUtil.isNullOrEmpty(fromLinkCombo)) {
                linkingMessages = " by Linking to " + fromLinkCombo + " " + linkedDocuments;
            }
            if (istemplate == 1) {
                msg = messageSource.getMessage("acc.field.SecurityTemplatehasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + billno + "</b>";
                auditSMS = " has " + action + template + " " + moduleName + " " + billno + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + additionalsauditmessage;
            } else if (istemplate == 2) {
                msg = messageSource.getMessage("acc.field.SecurityTemplatehasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                auditSMS = " has " + action + template + " " + moduleName + " " + billno + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + additionalsauditmessage;
            } else {
                msg = messageSource.getMessage("acc.sge.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                msg += (StringUtil.isNullOrEmpty(butPendingForApproval) ? "." : "") + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + billno + "</b>";   //"Purchase order has been saved successfully";
                auditSMS = " has " + action + template + " " + moduleName + " " + billno + linkingMessages + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + additionalsauditmessage;
            }
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(auditSMS, status);
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            if (!StringUtil.isNullOrEmpty(unlinkMessage)) {
                auditTrailObj.insertAuditLog(action, "User " + paramJobj.getString(Constants.userfullname) + " has unlinked " + "Security Gate Entry " + billno + unlinkMessage + ".", auditRequestParams, billno);
            }
            auditTrailObj.insertAuditLog(AuditAction.SECURYGATEENTRYCREATED, "User " + paramJobj.getString(Constants.userfullname) + auditSMS, auditRequestParams, securityGateEntry.getID());
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_SecurityGateEntry_ModuleId);
            jobj.put("billid", billid);
            jobj.put("billno", billno);
            jobj.put("amount", amount);
            jobj.put("fullShippingAddress", li.get(6));

        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_SecurityGateEntry_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_SecurityGateEntry_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }

            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isAccountingExe", accexception);
                jobj.put("pendingApproval", approvalStatusLevel != 11);
                jobj.put(Constants.channelName, channelName);
            } catch (JSONException ex) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    @SuppressWarnings("null")
    public List savePurchaseOrder(JSONObject paramJobj) throws ServiceException, AccountingException, JSONException, ScriptException, MessagingException, SessionExpiredException, ParseException {
        PurchaseOrder purchaseOrder = null;
        List newList = new ArrayList();
        List ll = new ArrayList();
        List mailParams = null;
        int pendingApprovalFlag = 0;
        String unlinkMessage = "";
        try {
            boolean isRoundingAdjustmentApplied = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.IsRoundingAdjustmentApplied, null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.IsRoundingAdjustmentApplied)) : false;                  
            KwlReturnObject result = null;
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv", null)) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            String currentUser = paramJobj.getString(Constants.useridKey);
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString("isLeaseFixedAsset", null))) ? Boolean.parseBoolean(paramJobj.getString("isLeaseFixedAsset")) : false;
            int istemplate = paramJobj.optString("istemplate", null) != null ? Integer.parseInt(paramJobj.getString("istemplate")) : 0;
            String taxid = null;
            if (paramJobj.optString("taxid").equalsIgnoreCase("None")) {
                taxid = null;
            } else {
                taxid = paramJobj.optString("taxid",null);
            }
            boolean isOpeningBalanceOrder = paramJobj.optString("isOpeningBalanceOrder", null) != null ? Boolean.parseBoolean(paramJobj.getString("isOpeningBalanceOrder")) : false;
            boolean isConsignment = false;
            String isConsignmentStr = paramJobj.optString("isConsignment", null);
            if (!StringUtil.isNullOrEmpty(isConsignmentStr)) {
                isConsignment = Boolean.parseBoolean(isConsignmentStr);
            }
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString("isFixedAsset", null))) ? Boolean.parseBoolean(paramJobj.getString("isFixedAsset")) : false;
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
            boolean isLinkedTransaction = StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedTransaction",null)) ? false : Boolean.parseBoolean(paramJobj.getString("isLinkedTransaction"));
            boolean isEditedPendingDocument = StringUtil.isNullOrEmpty(paramJobj.optString("isEditedPendingDocument", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEditedPendingDocument"));
            boolean isExpensePO = StringUtil.isNullOrEmpty(paramJobj.optString("isExpenseInv", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isExpenseInv"));
            boolean isMRPJOBWORKOUT = paramJobj.optString("isMRPJOBWORKOUT", null) != null ? Boolean.parseBoolean(paramJobj.getString("isMRPJOBWORKOUT")) : false;
            boolean isJobWorkOutOrder = paramJobj.optString("isJobWorkOrderReciever", null) != null ? Boolean.parseBoolean(paramJobj.optString("isJobWorkOrderReciever")) : false;
            boolean isPOfromSO = paramJobj.optString("isPOfromSO", null) != null ? Boolean.parseBoolean(paramJobj.optString("isPOfromSO")) : false;
            boolean isdropshipchecked = paramJobj.optString("isdropshipchecked",null) != null ? Boolean.parseBoolean(paramJobj.getString("isdropshipchecked")) : false;
            boolean isDraft = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isDraft,null))) ? Boolean.parseBoolean(paramJobj.optString(Constants.isDraft)) : false;
            String sequenceformat = paramJobj.optString(Constants.sequenceformat, null);
            String customfield = paramJobj.optString(Constants.customfield, null);
            String companyid = paramJobj.getString(Constants.companyKey);

            String createdby = paramJobj.getString(Constants.useridKey);
            String modifiedby = paramJobj.getString(Constants.useridKey);

            long createdon = System.currentTimeMillis();
            long updatedon = createdon;

            String shipLength = paramJobj.optString("shipLength", null);
            String invoicetype = paramJobj.optString("invoicetype", null);
            String deletedLinkedDocumentID = paramJobj.optString("deletedLinkedDocumentId", null);
            String currencyid = (paramJobj.optString(Constants.currencyKey, null) == null ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyKey));
            DateFormat df = authHandler.getDateOnlyFormat();
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate", "0"));
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParamsJson(paramJobj);

            KwlReturnObject extraCap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraPreferences = (ExtraCompanyPreferences) extraCap.getEntityList().get(0);
            
            JSONObject columnprefObj = new JSONObject();
            if (!StringUtil.isNullOrEmpty(extraPreferences.getColumnPref())) {
                 columnprefObj = new JSONObject((String) extraPreferences.getColumnPref());
            }
            
            String entryNumber = paramJobj.optString("number", null);
            String poid = paramJobj.optString("invoiceid", null);
            String costCenterId = paramJobj.optString(Constants.costcenter, null);
            String nextAutoNo = "";
            String auditMessage = "";
            HashMap<String, Object> pohm = new HashMap<>();
            Map<String, Object> oldpo = new HashMap<>();
            Map<String, Object> newAuditKey = new HashMap<>();

            synchronized (this) {
                SequenceFormat prevSeqFormat = null;
                if (!StringUtil.isNullOrEmpty(poid)) { // For edit case
                    HashMap<String, Object> termReqMap = new HashMap<>();
                    KwlReturnObject pocount = accPurchaseOrderobj.getPOEditCount(entryNumber, companyid, poid);
                    if (pocount.getRecordTotalCount() > 0 && istemplate != 2 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.PurchaseOrdernumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        KwlReturnObject rst = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), poid);
                        PurchaseOrder po = (PurchaseOrder) rst.getEntityList().get(0);
                        prevSeqFormat = po.getSeqformat();
                        if (isEdit == true) {
                            setValuesForAuditTrialMessage(po, paramJobj, oldpo, pohm, newAuditKey);
                        }
                        if (!sequenceformat.equals("NA")) {
                            nextAutoNo = entryNumber;
                        }
                        pohm.put("id", poid);
                     
                        /**
                         * * Save Versoning Information for Purchase Order **
                         */
                        if (!isLeaseFixedAsset && columnprefObj.has(Constants.ActiveVersioningInPurchaseOrder) && columnprefObj.get(Constants.ActiveVersioningInPurchaseOrder) != null && columnprefObj.optBoolean(Constants.ActiveVersioningInPurchaseOrder, false)) { // Not Lease Record and Activated Version History
                            savePurchaseOrderVersion(paramJobj, poid);
                        }
                    /**
                     * Check Whether Work Order Used in Stock Transfer or not
                     */
                     KwlReturnObject res = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), poid);
                     purchaseOrder = (PurchaseOrder) res.getEntityList().get(0);
                    if (purchaseOrder!=null && purchaseOrder.isIsJobWorkOutOrder()) {
                        result = accPurchaseOrderobj.getStockTransferFromJobWorkOutOrder(poid, companyid);
                        int count5 = result.getRecordTotalCount();
                        if (count5 > 0) {
                            throw new AccountingException("Cannot Edit Work Order as it is already used in other Transaction.");
                        }
                    }
                        if (isFixedAsset) {
                            HashMap<String, Object> requestMap = new HashMap<>();
                            requestMap.put("companyid", companyid);
                            requestMap.put("poid", poid);
                            accPurchaseOrderobj.deletePurchaseOrderAssetDetails(requestMap);
                        }
                        HashMap<String, Object> filterRequestMap = new HashMap<>();
                        filterRequestMap.put("companyid", companyid);
                        filterRequestMap.put("poid", poid);
                        filterRequestMap.put("isConsignment", isConsignment);
                        filterRequestMap.put("locale", paramJobj.get("locale"));
                        if(isExpensePO){//Delete Purchase Order Expense details
                            accPurchaseOrderobj.deletePurchaseOrderExpenseDetails(filterRequestMap);
                        } else{////Delete Purchase Order details
                            accPurchaseOrderobj.deletePurchaseOrderDetails(filterRequestMap);
                        }

                        // Delete Purchase Order Term Map
                        termReqMap.put("poid", poid);
                        accPurchaseOrderobj.deletePOTermMap(termReqMap);
                    }
                    /*
                     * Deleting Linking information of PO during Editing
                     */
                    accPurchaseOrderobj.deleteLinkingInformationOfPO(termReqMap); // Deleting linking information of PO 

                    /*
                     * Updating Isopen Flag=0 & linkflag=0 of VQ during Editing
                     * PO
                     */
                    if (!StringUtil.isNullOrEmpty(deletedLinkedDocumentID)) {
                        String[] deletedLinkedDocumentIDArr = deletedLinkedDocumentID.split(",");
                        for (int i = 0; i < deletedLinkedDocumentIDArr.length; i++) {
                            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), deletedLinkedDocumentIDArr[i]);
                            VendorQuotation vendorquotation = (VendorQuotation) venresult.getEntityList().get(0);
                            if (vendorquotation != null) {
                                termReqMap.put("quotation", vendorquotation);
                                termReqMap.put("value", "0");
                                termReqMap.put("isOpen", true);
                                accPurchaseOrderobj.updateVQLinkflag(termReqMap);
                                /*
                                 * Preparing audit trial message while unlinking
                                 * document through Edit
                                 */
                                if (i == 0) {
                                    unlinkMessage += " from the Vendor Quotation(s) ";
                                }
                                if (unlinkMessage.indexOf(vendorquotation.getQuotationNumber()) == -1) {
                                    unlinkMessage += vendorquotation.getQuotationNumber() + ", ";
                                }
                            } else {
                                venresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), deletedLinkedDocumentIDArr[i]);
                                SalesOrder salesOrder = (SalesOrder) venresult.getEntityList().get(0);
                                if (salesOrder != null) {
                                    /*
                                     * Preparing audit trial message while
                                     * unlinking document through Edit
                                     */
                                    if (i == 0) {
                                        unlinkMessage += " from the Sales Order(s) ";
                                    }
                                    if (unlinkMessage.indexOf(salesOrder.getSalesOrderNumber()) == -1) {
                                        unlinkMessage += salesOrder.getSalesOrderNumber() + ", ";
                                    }
                                } else {
                                    /*
                                     * Preparing audit trial message while
                                     * unlinking document through Edit
                                     */
                                    venresult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), deletedLinkedDocumentIDArr[i]);
                                    PurchaseRequisition purchaseRequisition = (PurchaseRequisition) venresult.getEntityList().get(0);
                                    /*
                                        Updating isOpenInPO Flag of Purchase Requisition during Editing
                                    */
                                    termReqMap.put("purchaseRequisition", purchaseRequisition);
                                    termReqMap.put("isOpenInPO", true);
                                    accPurchaseOrderobj.updatePRisOpenInPOFlag(termReqMap);
                                    
                                    if (i == 0) {
                                        unlinkMessage += " from the Purchase Requisition(s) ";
                                    }
                                    if (unlinkMessage.indexOf(purchaseRequisition.getPrNumber()) == -1) {
                                        unlinkMessage += purchaseRequisition.getPrNumber() + ", ";
                                    }
                                }
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(unlinkMessage) && unlinkMessage.endsWith(", ")) {
                        unlinkMessage = unlinkMessage.substring(0, unlinkMessage.length() - 2);
                    }
                }

                if (sequenceformat.equals("NA")) { // In case of NA checks wheather this number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Purchase_Order_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        }
                    }
                }
            }

            String vendorId = paramJobj.optString("vendor");
            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorId);
            Vendor vendor = (Vendor) venresult.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(poid)) { // Edit PO Case for updating address detail
                Map<String, Object> addressParams = new HashMap<>();
                String billingAddress = paramJobj.optString(Constants.BILLING_ADDRESS, null);
                if (!StringUtil.isNullOrEmpty(billingAddress)) {
                    
                    if (isdropshipchecked) {
                        addressParams = AccountingAddressManager.getVendorBillingAddressParamsForDropShipTypeDoc(paramJobj);
                    } else if (isPOfromSO) {
                        addressParams = AccountingAddressManager.getCustomerShippingAddressParamsJson(paramJobj, true);
                    } else {
                        addressParams = AccountingAddressManager.getAddressParamsJson(paramJobj, true); // true in case of vendor transaction to include vendor shipping address
                    }
                        
                } else { // handling the cases when no address coming in edit case 
                    if (extraPreferences.isIsAddressFromVendorMaster()) {
                        addressParams = AccountingAddressManager.getDefaultVendorAddressParams(vendorId, companyid, accountingHandlerDAOobj);
                    } else {
                        addressParams = AccountingAddressManager.getDefaultVendorCompanyAddressParams(vendorId, companyid, accountingHandlerDAOobj);
                    }
                    
                    /*
                     Handling the cases when no address coming in edit case 
                     */
                    if (isPOfromSO) {
                        String customerId = paramJobj.optString("customeridforshippingaddress", "");
                        Map<String, Object> addressParams1 = new HashMap<>();
                        if (!StringUtil.isNullOrEmpty(customerId)) {
                            addressParams1 = AccountingAddressManager.getDefaultCustomerAddressParams(customerId, companyid, accountingHandlerDAOobj);
                        }
                        if(addressParams1.size()>0){
                            addDefaultCustomerShippingAddress(addressParams,addressParams1);
                        }
                    }
                }
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), poid);
                PurchaseOrder po = (PurchaseOrder) returnObject.getEntityList().get(0);
                addressParams.put("id", po.getBillingShippingAddresses() == null ? "" : po.getBillingShippingAddresses().getID());
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                pohm.put("billshipAddressid", bsa.getID());
            } else { // Other Cases for saving address detail
                boolean isDefaultAddress = paramJobj.optString("defaultAdress", null) != null ? Boolean.parseBoolean(paramJobj.getString("defaultAdress")) : false;
                Map<String, Object> addressParams = new HashMap<>();
                if (isDefaultAddress) {
                                                                 
                    if (isdropshipchecked) {
                        addressParams = AccountingAddressManager.getDefaultVendorBillingAddressParamsForDropShipTypeDoc(vendorId, companyid, accountingHandlerDAOobj, paramJobj);
                    } else if (extraPreferences.isIsAddressFromVendorMaster()) {
                        addressParams = AccountingAddressManager.getDefaultVendorAddressParams(vendorId, companyid, accountingHandlerDAOobj);
                    } else {
                        addressParams = AccountingAddressManager.getDefaultVendorCompanyAddressParams(vendorId, companyid, accountingHandlerDAOobj);
                    }
                    
                    /*
                     Handling the cases when no address coming in Create case 
                     */
                    if (isPOfromSO && !isdropshipchecked) {
                        String customerId = paramJobj.optString("customeridforshippingaddress", "");
                        Map<String, Object> addressParams1 = new HashMap<>();
                        if (!StringUtil.isNullOrEmpty(customerId)) {
                            addressParams1 = AccountingAddressManager.getDefaultCustomerAddressParams(customerId, companyid, accountingHandlerDAOobj);
                        }
                        if (addressParams1.size() > 0) {
                            addDefaultCustomerShippingAddress(addressParams, addressParams1);
                        }
                    }
                    
                } else {
                    if (isdropshipchecked) {
                        addressParams = AccountingAddressManager.getVendorBillingAddressParamsForDropShipTypeDoc(paramJobj);
                    } else if (isPOfromSO) {
                        addressParams = AccountingAddressManager.getCustomerShippingAddressParamsJson(paramJobj, true);
                    } else {
                        addressParams = AccountingAddressManager.getAddressParamsJson(paramJobj, true); // true in case of vendor transaction to include vendor shipping address
                    }
                }
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                pohm.put("billshipAddressid", bsa.getID());
            }

            if (sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(poid)) {
                pohm.put("entrynumber", entryNumber);
            } else {
                pohm.put("entrynumber", "");
            }

            pohm.put("externalCurrencyRate", externalCurrencyRate);
            pohm.put("autogenerated", sequenceformat.equals("NA") ? false : true);
            pohm.put("isFixedAsset", isFixedAsset);
            pohm.put(Constants.memo, paramJobj.optString(Constants.memo, null));
            if (!StringUtil.isNullOrEmpty(shipLength)) {
                pohm.put("shipLength", shipLength);
            }
            pohm.put("invoicetype", invoicetype);
            pohm.put(Constants.posttext, paramJobj.optString(Constants.posttext, null));
            pohm.put("isOpeningBalanceOrder", isOpeningBalanceOrder);
            pohm.put("vendorid", paramJobj.getString("vendor"));
            pohm.put("formtypeid", paramJobj.has("formtypeid")?paramJobj.getString("formtypeid"):"");
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("GTAApplicable", null))) {
                pohm.put("gtaapplicable",paramJobj.optBoolean("GTAApplicable",false));
            }
            pohm.put("orderdate", df.parse(paramJobj.getString("billdate")));
            pohm.put(Constants.Checklocktransactiondate, paramJobj.getString("billdate")); // ERP-16800-Without parsing date
            pohm.put("duedate", df.parse(paramJobj.getString("duedate")));
            if (paramJobj.optString("shipdate", null) != null && !StringUtil.isNullOrEmpty(paramJobj.getString("shipdate"))) {
                pohm.put("shipdate", df.parse(paramJobj.getString("shipdate")));
            }
            pohm.put(Constants.shipvia, paramJobj.optString(Constants.shipvia, null));
            pohm.put(Constants.fob, paramJobj.optString(Constants.fob, null));
            pohm.put("termid", paramJobj.optString("termid", null));
            pohm.put("currencyid", currencyid);
            pohm.put("venbilladdress", paramJobj.optString("venbilladdress", null));
            pohm.put("venshipaddress", paramJobj.optString("venshipaddress", null));
            pohm.put("isfavourite", paramJobj.optString("isfavourite", null));
            pohm.put("agent", paramJobj.optString("agent", null));
            pohm.put("gstIncluded", paramJobj.optString("includingGST", null) == null ? false : Boolean.parseBoolean(paramJobj.getString("includingGST")));
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("perdiscount", null))) {
                pohm.put("perDiscount", StringUtil.getBoolean(paramJobj.getString("perdiscount")));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("discount", null))) {
                pohm.put("discount", StringUtil.getDouble(paramJobj.getString("discount")));
            }
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                pohm.put("costCenterId", costCenterId);
            }
            if(paramJobj.has(Constants.SUPPLIERINVOICENO) && paramJobj.optString(Constants.SUPPLIERINVOICENO)!=null){   //SDP-10402
                pohm.put(Constants.SUPPLIERINVOICENO, paramJobj.optString(Constants.SUPPLIERINVOICENO));
            }
            
            if(isMRPJOBWORKOUT){
                pohm.put("isMRPJOBWORKOUT", isMRPJOBWORKOUT);
                pohm.put(PurchaseOrder.JOBWORKLOCATIONID, paramJobj.getString(PurchaseOrder.JOBWORKLOCATIONID));
                pohm.put(PurchaseOrder.SHIPMENTROUTE, paramJobj.getString(PurchaseOrder.SHIPMENTROUTE));
                pohm.put(PurchaseOrder.GATEPASS, paramJobj.optString(PurchaseOrder.GATEPASS, null));
                pohm.put(PurchaseOrder.OTHERREMARKS, paramJobj.optString(PurchaseOrder.OTHERREMARKS,null));
                pohm.put(PurchaseOrder.PRODUCTID, paramJobj.optString(PurchaseOrder.PRODUCTID,null));
                pohm.put(PurchaseOrder.WORKORDERID, paramJobj.optString(PurchaseOrder.WORKORDERID,null));  
                 
                if (!StringUtil.isNullOrEmpty(paramJobj.optString(PurchaseOrder.DATEOFDELIVERY, null))) {
                    pohm.put(PurchaseOrder.DATEOFDELIVERY, df.parse(paramJobj.getString(PurchaseOrder.DATEOFDELIVERY)));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString(PurchaseOrder.DATEOFSHIPMENT, null))) {
                    pohm.put(PurchaseOrder.DATEOFSHIPMENT, df.parse(paramJobj.getString(PurchaseOrder.DATEOFSHIPMENT)));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString(PurchaseOrder.EXCISEDUTYCHARGES, null))) {
                    pohm.put(PurchaseOrder.EXCISEDUTYCHARGES, StringUtil.getDouble(paramJobj.getString(PurchaseOrder.EXCISEDUTYCHARGES)));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString(PurchaseOrder.PRODUCTQUANTITY, null))) {
                    pohm.put(PurchaseOrder.PRODUCTQUANTITY, StringUtil.getDouble(paramJobj.getString(PurchaseOrder.PRODUCTQUANTITY)));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString(PurchaseOrder.JOBWORKLOCATIONID, null))) {
                    pohm.put(PurchaseOrder.JOBWORKLOCATIONID,paramJobj.optString(PurchaseOrder.JOBWORKLOCATIONID));
                }
                  
                
            }
            if(isJobWorkOutOrder){
                pohm.put("isJobWorkOutOrder", isJobWorkOutOrder);
            }
            pohm.put("companyid", companyid);
            pohm.put("createdby", createdby);
            pohm.put("modifiedby", modifiedby);
            pohm.put("createdon", createdon);
            pohm.put("updatedon", updatedon);
            pohm.put("isConsignment", isConsignment);
            pohm.put(Constants.isDraft, isDraft);

            double subTotal = 0, taxAmt = 0, totalTermAmt = 0, totalAmt = 0, totalRowDiscount = 0, totalAmountinbase = 0;
            if (isExpensePO) {//**For Expense grid
                JSONArray jArr = new JSONArray(paramJobj.getString("expensedetail"));
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    double discountPerRow = 0;
                    if (jobj.optInt("discountispercent", 0) == 1) {// 1= Percentage; 0=Flat 
                        double rate = authHandler.roundUnitPrice(jobj.getDouble("rate"), companyid);
                        double discountPercentage = authHandler.round(jobj.optDouble("prdiscount", 0), companyid);
                        discountPerRow = authHandler.round((rate * discountPercentage / 100), companyid);
                    } else {
                        discountPerRow = authHandler.round(jobj.optDouble("prdiscount", 0), companyid);;
                    }
                    totalRowDiscount += discountPerRow;
                }
            } else { //**For product grid
                JSONArray jArr = new JSONArray(paramJobj.getString(Constants.detail));
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);

                    double qrate = authHandler.roundUnitPrice(jobj.getDouble("rate"), companyid);
                    double quantity = authHandler.roundQuantity(jobj.getDouble("quantity"), companyid);
                    boolean gstIncluded = paramJobj.optString("includingGST", null) == null ? false : Boolean.parseBoolean(paramJobj.getString("includingGST"));
                    if (gstIncluded) {
                        qrate = authHandler.roundUnitPrice(jobj.getDouble("rateIncludingGst"), companyid);
                    }

                    double quotationPrice = authHandler.round(quantity * qrate, companyid);
                    double discountQD = authHandler.round(jobj.optDouble("prdiscount", 0), companyid);
                    double discountPerRow = 0;

                    if (jobj.optInt("discountispercent", 0) == 1) {
                        discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                    } else {
                        discountPerRow = discountQD;
                    }

                    totalRowDiscount += discountPerRow;

                }
            }
            
            if (isdropshipchecked) {
                pohm.put("isdropshipchecked", isdropshipchecked);
            }

            totalRowDiscount = authHandler.round(totalRowDiscount, companyid);
            pohm.put("totallineleveldiscount", totalRowDiscount);

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("subTotal", null))) {
                subTotal = Double.parseDouble(paramJobj.getString("subTotal"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxamount", null))) {
                taxAmt = Double.parseDouble(paramJobj.getString("taxamount"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("invoicetermsmap", null))) {
                JSONArray termDetailsJArr = new JSONArray(paramJobj.getString("invoicetermsmap"));
                for (int cnt = 0; cnt < termDetailsJArr.length(); cnt++) {
                    JSONObject temp = termDetailsJArr.getJSONObject(cnt);
                    if (temp != null) {
                        double termAmount = Double.parseDouble(temp.getString("termamount"));
                        totalTermAmt += termAmount;
                    }
                }
            }
            totalAmt = subTotal + taxAmt + totalTermAmt;
            
            double roundingadjustmentAmount = 0.0, roundingadjustmentAmountinbase = 0.0;
            String roundingAdjustmentAccountID = "";
            String columnPref = extraPreferences.getColumnPref();
            if (!StringUtil.isNullOrEmpty(columnPref)) {
                JSONObject prefObj = new JSONObject(columnPref);
                roundingAdjustmentAccountID = prefObj.optString(Constants.RoundingAdjustmentAccountID, "");
            }

            if (isRoundingAdjustmentApplied && !StringUtil.isNullOrEmpty(roundingAdjustmentAccountID)) {
                double totalInvAmountAfterRound = Math.round(totalAmt);
                roundingadjustmentAmount = authHandler.round(totalInvAmountAfterRound - totalAmt, companyid);
                if (roundingadjustmentAmount != 0) {
                    totalAmt = totalInvAmountAfterRound;//Now rounded value becomes total invoice amount
                    pohm.put(Constants.roundingadjustmentamount, roundingadjustmentAmount);
                    pohm.put(Constants.roundingadjustmentamountinbase, roundingadjustmentAmount);
                    
                    String globalcurrency= paramJobj.getString(Constants.globalCurrencyKey);
                    if (!globalcurrency.equalsIgnoreCase(currencyid)) {
                        HashMap<String, Object> roundingRequestParams = new HashMap<String, Object>();
                        roundingRequestParams.put("companyid", companyid);
                        roundingRequestParams.put("gcurrencyid", (paramJobj.optString(Constants.currencyKey, null) == null ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyKey)));
                        KwlReturnObject baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(roundingRequestParams, roundingadjustmentAmount, currencyid, df.parse(paramJobj.optString("billdate")), externalCurrencyRate);
                        roundingadjustmentAmountinbase = authHandler.round((Double) baseAmt.getEntityList().get(0), companyid);
                        pohm.put(Constants.roundingadjustmentamountinbase, roundingadjustmentAmountinbase);
                    }
                }
            }
            pohm.put(Constants.IsRoundingAdjustmentApplied, isRoundingAdjustmentApplied);
            pohm.put("totalamount", totalAmt);

            HashMap<String, Object> filterRequestParams = new HashMap<>();
            filterRequestParams.put(Constants.companyKey, companyid);
            filterRequestParams.put(Constants.globalCurrencyKey, paramJobj.getString(Constants.globalCurrencyKey));
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalAmt, currencyid, df.parse(paramJobj.getString("billdate")), externalCurrencyRate);
            totalAmountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
            pohm.put("totalamountinbase", totalAmountinbase);

            KwlReturnObject descbAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalRowDiscount, currencyid, df.parse(paramJobj.getString("billdate")), externalCurrencyRate);
            double descountinBase = authHandler.round((Double) descbAmtTax.getEntityList().get(0), companyid);

            pohm.put("discountinbase", descountinBase);

            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
                pohm.put("taxid", taxid);
            } else {
                pohm.put("taxid", taxid); // Put taxid as null if the PO doesnt have any total tax included. (To avoid problem while editing PO)
            }
            Country country= extraPreferences.getCompany().getCountry();
            if(extraPreferences.isIsNewGST()){
                /**
                 * ERP-32829 
                 */
                pohm.put("taxid", taxid);
                pohm.put("gstapplicable", paramJobj.optBoolean("GSTApplicable", false));
                pohm.put(Constants.isMerchantExporter, paramJobj.optBoolean(Constants.isMerchantExporter, false));
            }
            pohm.put(Constants.isApplyTaxToTerms, paramJobj.optBoolean(Constants.isApplyTaxToTerms,false));
            pohm.put("isEdit", isEdit);
            pohm.put("isLinkedTransaction", isLinkedTransaction);
            pohm.put("isCopy", isCopy);
            KwlReturnObject poresult = accPurchaseOrderobj.savePurchaseOrder(pohm);
            purchaseOrder = (PurchaseOrder) poresult.getEntityList().get(0);
            accPurchaseOrderobj.deletePurchaseOrderOtherDetails(purchaseOrder.getID(), companyid);
            savePurchaseOrderOtherDetails(paramJobj, purchaseOrder.getID(), companyid);
//            if (country != null && Integer.parseInt(country.getID()) == Constants.indian_country_id && vendor != null && vendor.getGSTRegistrationType() != null) {
//                MasterItem gstRegistrationType = vendor.getGSTRegistrationType();
//                if (gstRegistrationType != null && gstRegistrationType.getDefaultMasterItem() != null && !StringUtil.isNullOrEmpty(gstRegistrationType.getDefaultMasterItem().getID())) {
//                    paramJobj.put("isUnRegisteredDealer", gstRegistrationType.getDefaultMasterItem().getID().equals(Constants.GSTRegType.get(Constants.GSTRegType_Unregistered)));;
//                }
//            }
            /**
             * Save GST History Customer/Vendor data.
             */
            if (purchaseOrder.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                paramJobj.put("docid", purchaseOrder.getID());
                paramJobj.put("moduleid", isFixedAsset?Constants.Acc_FixedAssets_Purchase_Order_ModuleId:Constants.Acc_Purchase_Order_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(paramJobj);
            }
            
            Set<PurchaseOrderDetail> podetails = null;
            Set<ExpensePODetail> expenseDetails = null;
            if (isExpensePO) {
                Map<String, Object> expenseParams = new HashMap<>();
                expenseParams.put("poObject", purchaseOrder);
                expenseParams.put("currencyid", currencyid);
                expenseParams.put("companyid", companyid);
                expenseParams.put("externalCurrencyRate", externalCurrencyRate);
                expenseParams.put("expensedetail", paramJobj.getString("expensedetail"));
                List rowDetails = saveExpensePORows(expenseParams);
                expenseDetails = (HashSet) rowDetails.get(0);
                pohm.put("expensedetail", expenseDetails);
                pohm.put("isexpensetype", true);
            } else {
                paramJobj.put("isJobWorkOutOrder",isJobWorkOutOrder);
                List rowDetails = savePurchaseOrderRows(paramJobj, currencyid, purchaseOrder, externalCurrencyRate, companyid, GlobalParams);
                podetails = (HashSet) rowDetails.get(0);
                pohm.put("podetails", (HashSet) rowDetails.get(0));
            }
            pohm.put("id", purchaseOrder.getID());
            
            JSONArray productDiscountJArr = new JSONArray();
            if (podetails != null) {
                for (PurchaseOrderDetail poDetail : podetails) {
                    String productId = poDetail.getProduct().getID();
                    double discountVal = poDetail.getDiscount();
                    int isDiscountPercent = poDetail.getDiscountispercent();
                    if (isDiscountPercent == 1) {
                        discountVal = (poDetail.getQuantity() * poDetail.getRate()) * (discountVal / 100);
                    }
                    KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, df.parse(paramJobj.getString("billdate")), externalCurrencyRate);
                    double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                    discAmountinBase = authHandler.round(discAmountinBase, companyid);
                    JSONObject productDiscountObj = new JSONObject();
                    productDiscountObj.put("productId", productId);
                    productDiscountObj.put("discountAmount", discAmountinBase);
                    productDiscountJArr.put(productDiscountObj);
                }
            }
            
            String purchaseOrderTerms = paramJobj.optString("invoicetermsmap", null);
            if (StringUtil.isAsciiString(purchaseOrderTerms)) {
                if (new JSONArray(purchaseOrderTerms).length() > 0) {
                    pohm.put(Constants.termsincludegst, paramJobj.optBoolean(Constants.termsincludegst,false));
                }
            }
            String disableSOForPO = paramJobj.optString("blockSOPO", "");
            boolean isBlockDocument = false;
            if (!StringUtil.isNullOrEmpty(disableSOForPO) && disableSOForPO.equalsIgnoreCase("on")) {
                isBlockDocument = true;
            }
            pohm.put("isLinkedSOBlocked", isBlockDocument);
            
            
            pohm.put("isEdit", isEdit);
            pohm.put("isLinkedTransaction", isLinkedTransaction);
            pohm.put("isCopy", isCopy);
            result = accPurchaseOrderobj.savePurchaseOrder(pohm);
            if (!isConsignment) { // in case ofconsignment do not need to go for apprval
                purchaseOrder = (PurchaseOrder) result.getEntityList().get(0);
                purchaseOrder.setIstemplate(istemplate);
            }

            String linkMode = paramJobj.optString("fromLinkCombo", null);
            String linkedDocuments = "";
            if (!StringUtil.isNullOrEmpty(linkMode)) {
                if (linkMode.equalsIgnoreCase("Vendor Quotation") || linkMode.equalsIgnoreCase("Asset Vendor Quotation")) {
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("linkNumber", null))) { // When adding Vendor Quotation link for Purchase Order update link flag(2) in Quotation.
                        String[] linkNumbers = paramJobj.getString("linkNumber").split(",");
                        if (linkNumbers.length > 0) {
                            for (int i = 0; i < linkNumbers.length; i++) {
                                if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
                                    updateVQisOpenAndLinking(linkNumbers[i]);

                                    /*
                                     * Saving linking information of Purchase
                                     * Order while linking with Vendor Quotation
                                     */
                                    HashMap<String, Object> requestParams = new HashMap<>();
                                    requestParams.put("linkeddocid", purchaseOrder.getID());
                                    requestParams.put("docid", linkNumbers[i]);
                                    requestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                                    requestParams.put("linkeddocno", entryNumber);
                                    requestParams.put("sourceflag", 0);
                                    result = accPurchaseOrderobj.saveVQLinking(requestParams);

                                    requestParams.put("sourceflag", 1);
                                    requestParams.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), linkNumbers[i]);
                                    VendorQuotation quotation = (VendorQuotation) rdresult.getEntityList().get(0);
                                    String quotationno = quotation.getQuotationNumber();
                                    requestParams.put("linkeddocno", quotationno);
                                    requestParams.put("docid", purchaseOrder.getID());
                                    requestParams.put("linkeddocid", linkNumbers[i]);
                                    result = accPurchaseOrderobj.savePOLinking(requestParams);
                                    linkedDocuments += quotationno + " ,";

                                }
                            }

                        }
                    }
                    if (!StringUtil.isNullOrEmpty(linkedDocuments)) {
                        linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                    }
                } else if (!StringUtil.isNullOrEmpty(linkMode) && linkMode.equalsIgnoreCase("Sales Order")) {
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("linkNumber", null))) {
                        String[] linkNumbers = paramJobj.getString("linkNumber").split(",");
                        if (linkNumbers.length > 0) {
                            for (int i = 0; i < linkNumbers.length; i++) {
                                if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
                                    /*
                                     * Saving linking information of Purchase
                                     * Order while linking with Sales Order
                                     */
                                    HashMap<String, Object> requestParams = new HashMap<>();
                                    requestParams.put("linkeddocid", purchaseOrder.getID());
                                    requestParams.put("docid", linkNumbers[i]);
                                    requestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                                    requestParams.put("linkeddocno", entryNumber);
                                    requestParams.put("sourceflag", 0);
                                    result = accSalesOrderDAOobj.saveSalesOrderLinking(requestParams);

                                    /*
                                     * Saving linking information of Sales Order
                                     * while linking with Purchase Order
                                     */
                                    requestParams.put("sourceflag", 1);
                                    requestParams.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                                    result = accPurchaseOrderobj.getSalesOrderNumber(linkNumbers[i]);
                                    String salesOrderNo = result.getEntityList().get(0).toString();
                                    requestParams.put("linkeddocno", salesOrderNo);
                                    requestParams.put("docid", purchaseOrder.getID());
                                    requestParams.put("linkeddocid", linkNumbers[i]);
                                    result = accPurchaseOrderobj.savePOLinking(requestParams);
                                    linkedDocuments += salesOrderNo + " ,";

                                    /**
                                 * if the blockSOPO check is enabled on UI side
                                 * then updating the value of disabledsoforpo = 'T' of linked SO ERP-35541
                                     */
                                    String salesOrderID = linkNumbers[i];
                                    String status = "closed";
                                    String auditStatus = "Unblocked";
                                    if (isBlockDocument) {
                                        status = "Open";
                                        auditStatus = " Blocked ";
                                    }
                                    HashMap requestparams = new HashMap();
                                    requestparams.put("salesOrderID", salesOrderID);
                                    requestparams.put("salesOrderNo", salesOrderNo);
                                    requestparams.put("status", status);
                                KwlReturnObject statusReturnObj=accSalesOrderDAOobj.saveSalesOrderStatusForPO(requestparams);
                                    if (statusReturnObj.isSuccessFlag()) {
                                        Map<String, Object> auditRequestParams = new HashMap<>();
                                        auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                                        auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                                        auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                                        auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER_BLOCKED_UNBLOCKED, "User " + paramJobj.getString(Constants.userfullname) + " has " + auditStatus + " Sales Order " + salesOrderNo + " For Purchase Order ", auditRequestParams, salesOrderID);
                                    }
                                }
                            }

                        }
                    }
                    if (!StringUtil.isNullOrEmpty(linkedDocuments)) {
                        linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                    }
                } else if (!StringUtil.isNullOrEmpty(linkMode) && linkMode.equalsIgnoreCase("Purchase Requisition")) {
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("linkNumber", null))) { // When adding Vendor Quotation link for Purchase Order update link flag(2) in Quotation.
                        String[] linkNumbers = paramJobj.getString("linkNumber").split(",");
                        if (linkNumbers.length > 0) {
                            for (int i = 0; i < linkNumbers.length; i++) {
                                if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {

                                /*---------Updating isopeninpo flag if Requisition is linking with PO---- */
                                    updateOpenFlagForPOInRequisition(linkNumbers[i]);
                                    /*
                                 * Saving linking information of Purchase Order
                                 * while linking with Vendor Quotation
                                     */
                                    HashMap<String, Object> requestParams = new HashMap<>();
                                    requestParams.put("linkeddocid", purchaseOrder.getID());
                                    requestParams.put("docid", linkNumbers[i]);
                                    requestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                                    requestParams.put("linkeddocno", entryNumber);
                                    requestParams.put("sourceflag", 0);
                                    result = accPurchaseOrderobj.savePurchaseRequisitionLinking(requestParams);

                                    requestParams.put("sourceflag", 1);
                                    requestParams.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), linkNumbers[i]);
                                    PurchaseRequisition purchaseRequisition = (PurchaseRequisition) rdresult.getEntityList().get(0);
                                    String prno = purchaseRequisition.getPrNumber();
                                    requestParams.put("linkeddocno", prno);
                                    requestParams.put("docid", purchaseOrder.getID());
                                    requestParams.put("linkeddocid", linkNumbers[i]);
                                    result = accPurchaseOrderobj.savePOLinking(requestParams);
                                    linkedDocuments += prno + " ,";
                                }
                            }


                        /* ---Update Balance Quantity of Requisition while linking with PO------- */
                            if (podetails != null) {

                                for (PurchaseOrderDetail poDetail : podetails) {

                                    if (poDetail.getPurchaseRequisitionDetailId() != null) {

                                        HashMap poMap = new HashMap();

                                        poMap.put("requisitiondetails", poDetail.getPurchaseRequisitionDetailId());
                                        poMap.put("quantityUsedInpodetail", poDetail.getQuantity());
                                        poMap.put("companyid", companyid);

                                        accPurchaseOrderobj.updateBalanceQuantityOfRequisitionDetail(poMap);

                                    }
                                }
                            }

                        }
                    }
                    if (!StringUtil.isNullOrEmpty(linkedDocuments)) {
                        linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                    }
                }
            }//end of link mode

            if (isEdit == true) { // For Audit Trial-ERP-14034
                // ERP-14034 
                DateFormat sdf = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);
                if (purchaseOrder.getShipdate() != null) {
                    pohm.put("AuditShipDate", sdf.format(purchaseOrder.getShipdate())); // New Ship Date
                } else {
                    pohm.put("AuditShipDate", "");
                }
                if (purchaseOrder.getDueDate() != null) {
                    pohm.put("AuditDuedate", sdf.format(purchaseOrder.getDueDate())); // New Due Date
                } else {
                    pohm.put("AuditDuedate", "");
                }
                if (purchaseOrder.getOrderDate() != null) {
                    pohm.put("AuditOrderDate", sdf.format(purchaseOrder.getOrderDate()));  //New Order Date
                } else {
                    pohm.put("AuditOrderDate", "");
                }
            }

            // Save global-level CUSTOMFIELDS for Purchase Order
            if (!StringUtil.isNullOrEmpty(customfield)) {
                HashMap<String, Object> POMap = new HashMap<>();
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "PurchaseOrder");
                customrequestParams.put("moduleprimarykey", "PoID");
                customrequestParams.put("modulerecid", purchaseOrder.getID());
                customrequestParams.put("moduleid",isJobWorkOutOrder ? Constants.JOB_WORK_OUT_ORDER_MODULEID : isConsignment ? Constants.Acc_ConsignmentVendorRequest_ModuleId
                        : (!isFixedAsset ? Constants.Acc_Purchase_Order_ModuleId : Constants.Acc_FixedAssets_Purchase_Order_ModuleId));
                customrequestParams.put("companyid", companyid);
                POMap.put("poid", purchaseOrder.getID());
                customrequestParams.put("customdataclasspath", Constants.Acc_PurchaseOrder_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    if (customDataresult.getEntityList().get(0) != null) {
                        POMap.put("purchaseordercustomdataref", purchaseOrder.getID());
                        accPurchaseOrderobj.updatePurchaseOrder(POMap);
                    }
                }
            }

            if (isEdit == true) {
                int moduleid = Constants.Acc_Purchase_Order_ModuleId;
                auditMessage = AccountingManager.BuildAuditTrialMessage(pohm, oldpo, moduleid, newAuditKey);
            }

            ll.add(purchaseOrder);
            ll.add(auditMessage);
            // Check for approval rules and apply rules if available
            String butPendingForApproval = "";
            double subTotalAmount = 0;
            double taxAmount = 0;
            double totalTermAmount = 0;
            double totalAmount = 0;

            HashMap<String, Object> poApproveMap = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("subTotal", null))) {
                subTotalAmount = Double.parseDouble(paramJobj.getString("subTotal"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxamount", null))) {
                taxAmount = Double.parseDouble(paramJobj.getString("taxamount"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("invoicetermsmap", null))) {
                JSONArray termDetailsJArr = new JSONArray(paramJobj.getString("invoicetermsmap"));
                for (int cnt = 0; cnt < termDetailsJArr.length(); cnt++) {
                    JSONObject temp = termDetailsJArr.getJSONObject(cnt);
                    if (temp != null) {
                        double termAmount = Double.parseDouble(temp.getString("termamount"));
                        totalTermAmount += termAmount;
                    }
                }
            }
            totalAmount = subTotalAmount + taxAmount + totalTermAmount;
            KwlReturnObject tAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, totalAmount, currencyid, df.parse(paramJobj.getString("billdate")), externalCurrencyRate);
            double totalAmountinBase = 0;
            totalAmountinBase = (Double) tAmount.getEntityList().get(0);
            int approvalStatusLevel = 11;
            List approvedlevel = null;
            int level = (isEdit && !isCopy) ? 0 : purchaseOrder.getApprovestatuslevel();
            boolean ismailApplicable=false;
                  
            poApproveMap.put("companyid", paramJobj.getString(Constants.companyKey));
            poApproveMap.put("level", level);
            poApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmountinBase, companyid)));
            poApproveMap.put("currentUser", currentUser);
            poApproveMap.put("fromCreate", true);
            poApproveMap.put("isEdit", isEdit);
            poApproveMap.put("productDiscountMapList", productDiscountJArr);
            if (isFixedAsset) {
                poApproveMap.put("moduleid", Constants.Acc_FixedAssets_Purchase_Order_ModuleId);
            } else {
                poApproveMap.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
            }
                                 
            boolean sendPendingDocumentsToNextLevel = false;
            boolean pendingMessage = true;

            if (columnprefObj.has("sendPendingDocumentsToNextLevel") && columnprefObj.get("sendPendingDocumentsToNextLevel") != null && (Boolean) columnprefObj.get("sendPendingDocumentsToNextLevel") != false) {
                sendPendingDocumentsToNextLevel = true;
            }
                
            
            /*------If pending document is Edited & Check is activated from system preferences
             *---Then Edit will work same as while approving document
             */
            if (isEditedPendingDocument) {
                level = purchaseOrder.getApprovestatuslevel();
                poApproveMap.put("fromCreate", false);
                poApproveMap.put("documentLevel", level);
                if (sendPendingDocumentsToNextLevel) {
                    ismailApplicable = true;
                    poApproveMap.put("level", level);
                    pendingMessage = false;

                } else {//Sending Parameter in approve function if "Send approval documents to next level" check is disabled from system preferences
                    poApproveMap.put("isEditedPendingDocumentWithCheckOff", true);
                }
            }
            poApproveMap.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));
            /*
             * In case of Job work out order will not go for pending approval
             *
             */
             
            if (!(isLeaseFixedAsset || isConsignment || isJobWorkOutOrder||istemplate == 2 || isDraft) && !isOpeningBalanceOrder) { // !isDraft = if you are saving purchase order as draft then no need to apply aprroval rule because draft means it does not exist in the system.
                approvedlevel = approvePurchaseOrder(purchaseOrder, poApproveMap, ismailApplicable);
                approvalStatusLevel = (Integer) approvedlevel.get(0);
                mailParams = (List) approvedlevel.get(1);
                
                // approvalStatusLevel = approvePurchaseOrder(purchaseOrder, poApproveMap);
            } else {
                purchaseOrder.setApprovestatuslevel(11);
            }
            
            
         
            List approvalHistoryList = null;
            String roleName = "";
            boolean isAuthorityToApprove = false;
         

            /*-----Block is executed when Edited pending Document & Check "Send pending documents to next level" is activated-------*/
              /*------If pending document is Edited & Check is activated from system preferences
             *---Then Edit will work same as while approving document
             */
            if (isEditedPendingDocument && sendPendingDocumentsToNextLevel) {

                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

                /*---Document will approve as approval level -----  */
                if (approvalStatusLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {

                    HashMap emailMap = new HashMap();

                    String userName = paramJobj.optString(Constants.username, null);
                    emailMap.put("userName", userName);
                    KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                    Company company = (Company) returnObject.getEntityList().get(0);
                    emailMap.put("company", company);
                    emailMap.put("purchaseOrder", purchaseOrder);
                    emailMap.put("baseUrl", paramJobj.optString("baseUrl", null));
                    emailMap.put("preferences", preferences);
                    emailMap.put("isFixedAsset", isFixedAsset);
                    
                    sendApprovalMailIfAllowedFromSystemPreferences(emailMap);
                    

                }

                /*--------Save Approval history Code--------  */
                if (approvalStatusLevel != Constants.NoAuthorityToApprove) {

                    HashMap approvalHistoryMap = new HashMap();
                    String userid = paramJobj.optString(Constants.userid, null);

                    approvalHistoryMap.put("userid", userid);

                    String userName = paramJobj.optString(Constants.username, null);
                    approvalHistoryMap.put("userName", userName);
                    KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                    Company company = (Company) returnObject.getEntityList().get(0);
                    approvalHistoryMap.put("company", company);
                    approvalHistoryMap.put("purchaseOrder", purchaseOrder);
                    approvalHistoryMap.put("baseUrl", paramJobj.optString("baseUrl", null));
                    approvalHistoryMap.put("preferences", preferences);
                    approvalHistoryMap.put("isFixedAsset", isFixedAsset);

                    approvalHistoryList = saveApprovalHistory(approvalHistoryMap);
                    roleName = approvalHistoryList != null ? approvalHistoryList.get(0).toString() : "";
                    isAuthorityToApprove = true;

                 

                } else {
                    /*----If User have no authority to approve------  */
                    isAuthorityToApprove = false;
                }
            }
                        
            if (approvalStatusLevel != Constants.APPROVED_STATUS_LEVEL && pendingMessage) {
                butPendingForApproval = " " + messageSource.getMessage("acc.field.butpendingforApproval", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
            }
            
            /*-------If  "Send pending documents to next level" check is OFF and User have no authority to approve--------- */
            if (isEditedPendingDocument && !sendPendingDocumentsToNextLevel && approvalStatusLevel == Constants.NoAuthorityToApprove) {
                isAuthorityToApprove = false;
                butPendingForApproval = "";
            }
            
            String fullShippingAddress = "";
            if (purchaseOrder.getBillingShippingAddresses() != null) {
                fullShippingAddress = purchaseOrder.getBillingShippingAddresses().getFullShippingAddress();
            }
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) rdresult.getEntityList().get(0);
            if (company.getCountry() != null && Constants.indian_country_id == Integer.parseInt(company.getCountry().getID()) && extraPreferences.isExciseApplicable()) {
                HashMap exciseDetails = null;
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("indiaExcise", null))) {
                    JSONObject jobj = new JSONObject(paramJobj.getString("indiaExcise"));
                    if (extraPreferences.isExciseApplicable()) {
                        jobj.put("purchaseorder", purchaseOrder.getID());
                        exciseDetails = mapExciseDetails(jobj, paramJobj);
                        accPurchaseOrderobj.saveAssetExciseDetails(exciseDetails);
                    }
                }
            }

            ll.add(totalAmount);
            ll.add(approvalStatusLevel);
            ll.add(butPendingForApproval);
            ll.add(mailParams);
            ll.add(fullShippingAddress);
            ll.add(linkedDocuments);
            ll.add(unlinkMessage);
            ll.add(roleName);
            ll.add(isAuthorityToApprove);
            ll.add(sendPendingDocumentsToNextLevel);
            // Save record as template
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("templatename", null)) && (istemplate == 1 || istemplate == 2)) {
                HashMap<String, Object> hashMap = new HashMap<>();
                String moduletemplateid = paramJobj.getString("moduletemplateid");
                hashMap.put("templatename", paramJobj.getString("templatename"));
                if (!StringUtil.isNullOrEmpty(moduletemplateid)) {
                    hashMap.put("moduletemplateid", moduletemplateid);
                }
                hashMap.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("populateproducttemplate", null)) && paramJobj.optString("populateproducttemplate").equalsIgnoreCase("on")) {
                    hashMap.put("populateproducttemplate", paramJobj.optBoolean("populateproducttemplate", true));
                } else {
                    hashMap.put("populateproducttemplate", false);
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("populatecustomertemplate", null)) && paramJobj.optString("populatecustomertemplate").equalsIgnoreCase("on")) {
                    hashMap.put("populatecustomertemplate", paramJobj.optBoolean("populatecustomertemplate", true));
                } else {
                    hashMap.put("populatecustomertemplate", false);
                }
                hashMap.put("modulerecordid", purchaseOrder.getID());
                hashMap.put("companyid", companyid);
                if(!StringUtil.isNullOrEmpty(paramJobj.optString("companyunitid",null))){
                    hashMap.put("companyunitid", paramJobj.getString("companyunitid")); // Added Unit ID if it is present in request
                }
                /**
                 * checks the template name is already exist in create and edit template case
                 */
                result = accountingHandlerDAOobj.getModuleTemplateForTemplatename(hashMap);
                int nocount = result.getRecordTotalCount();
                if (nocount > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.tmp.templateNameAlreadyExists", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }

                accountingHandlerDAOobj.saveModuleTemplate(hashMap);
            }

            if (StringUtil.isAsciiString(purchaseOrderTerms)) {
                mapInvoiceTerms(purchaseOrderTerms, purchaseOrder.getID(), paramJobj.getString(Constants.useridKey), false);
            }
            String moduleName = Constants.moduleID_NameMap.get(Constants.Acc_Purchase_Order_ModuleId);
            if (isFixedAsset) {
                moduleName = Constants.ASSET_PURCHASE_ORDER;
            }
            if (isConsignment) {
                moduleName = Constants.moduleID_NameMap.get(Constants.Acc_ConsignmentStockPurchaseRequest_ModuleId);
            }
            // Send Mail when Purchase Order  is generated or modified.
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), paramJobj.getString(Constants.companyKey));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(poid)) {
                    if (isFixedAsset && documentEmailSettings.isAssetPurchaseOrderGenerationMail()) {
                        sendmail = true;
                    } else if (isConsignment && documentEmailSettings.isConsignmentPReqGenerationMail()) {
                        sendmail = true;
                    } else if (documentEmailSettings.isPurchaseOrderGenerationMail()) {
                        sendmail = true;
                    }
                } else {
                    isEditMail = true;
                    if (isFixedAsset && documentEmailSettings.isAssetPurchaseOrderUpdationMail()) {
                        sendmail = true;
                    } else if (isConsignment && documentEmailSettings.isConsignmentPReqUpdationMail()) {
                        sendmail = true;
                    } else if (documentEmailSettings.isPurchaseOrderUpdationMail()) {
                        sendmail = true;
                    }
                }
                if (sendmail) {
                    String userMailId = "", userName = "",currentUserid="";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> requestParams = AccountingManager.getEmailNotificationParamsJson(paramJobj);
                    if (requestParams.containsKey("userfullName") && requestParams.get("userfullName") != null) {
                        userName = (String) requestParams.get("userfullName");
                    }
                    if (requestParams.containsKey("usermailid") && requestParams.get("usermailid") != null) {
                        userMailId = (String) requestParams.get("usermailid");
                    }
                    if(requestParams.containsKey(Constants.useridKey)&& requestParams.get(Constants.useridKey)!=null){
                        currentUserid=(String)requestParams.get(Constants.useridKey);
                    }
                    List<String> mailIds = new ArrayList();
                    if (!StringUtil.isNullOrEmpty(userMailId)) {
                        mailIds.add(userMailId);
                    }
                    /*
                     if Edit mail option is true then get userid and Email id of document creator.
                     */
                    if (isEditMail) {
                        if (purchaseOrder != null && purchaseOrder.getCreatedby() != null) {
                            createdByEmail = purchaseOrder.getCreatedby().getEmailID();
                            createdById = purchaseOrder.getCreatedby().getUserID();
                        }
                        /*
                         if current user userid == document creator userid then don't add creator email ID in List.
                         */
                        if (!StringUtil.isNullOrEmpty(createdByEmail) && !(currentUserid.equalsIgnoreCase(createdById))) {
                            mailIds.add(createdByEmail);
                        }
                    }
                    String[] temp = new String[mailIds.size()];
                    String[] tomailids = mailIds.toArray(temp);
                    String vqNumber = entryNumber;

                    if (((documentEmailSettings.isPurchaseOrderGenerationMail() && !isEditMail) || (documentEmailSettings.isPurchaseOrderUpdationMail() && isEditMail)) && isConsignment) {
                        sendMailOnPOCreationUpdation(companyid, purchaseOrder, isEditMail, tomailids, entryNumber);
                    } else {
                        accountingHandlerDAOobj.sendSaveTransactionEmails(vqNumber, moduleName, tomailids, userName, isEditMail, companyid);
                    }
                }
            }

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("savePurchaseOrder : " + ex.getMessage(), ex);
        }
        return ll;
    }
     
    
    /*---Function to save approval history , If any document is approved at some level------   */
    public List saveApprovalHistory(HashMap approvalHistoryMap) throws ServiceException {
   
        List approvalHistoryList= new ArrayList();

        PurchaseOrder purchaseOrderObj = null;
        Company companyObj = null;
        String userid = "";

        if (approvalHistoryMap.containsKey("purchaseOrder") && approvalHistoryMap.get("purchaseOrder") != null) {
            purchaseOrderObj = (PurchaseOrder) approvalHistoryMap.get("purchaseOrder");
        }

        if (approvalHistoryMap.containsKey("company") && approvalHistoryMap.get("company") != null) {
            companyObj = (Company) approvalHistoryMap.get("company");
        }
      
        if (approvalHistoryMap.containsKey("userid") && approvalHistoryMap.get("userid") != null) {
            userid = (String) approvalHistoryMap.get("userid");
        }

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("transtype", Constants.PURCHASE_ORDER_APPROVAL);
        hashMap.put("transid", purchaseOrderObj.getID());
        hashMap.put("approvallevel", purchaseOrderObj.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
        hashMap.put("remark", "");//I think , it should be blank in edit mode
        hashMap.put("userid", userid);
        hashMap.put("companyid", companyObj.getCompanyID());
        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
        accPurchaseOrderobj.setApproverForPurchaseOrder(purchaseOrderObj.getID(), companyObj.getCompanyID(), userid);//to save current approver for the transaction approval level
        KwlReturnObject kmsg = null;
        String roleName = "Company User";
        kmsg = permissionHandlerDAOObj.getRoleofUser(userid);
        Iterator ite2 = kmsg.getEntityList().iterator();
        while (ite2.hasNext()) {
            Object[] row = (Object[]) ite2.next();
            roleName = row[1].toString();
        }
        
        approvalHistoryList.add(roleName);
        
        return approvalHistoryList;

    }
    
    
    
    
    /*---Function to save approval history , If any document is approved at some level------   */
    public List saveApprovalHistoryForVq(HashMap approvalHistoryMap) throws ServiceException {

        List approvalHistoryList = new ArrayList();

        VendorQuotation vendorQuoationObj = null;
        Company companyObj = null;
        String userid = "";

        if (approvalHistoryMap.containsKey("vendorQuotation") && approvalHistoryMap.get("vendorQuotation") != null) {
            vendorQuoationObj = (VendorQuotation) approvalHistoryMap.get("vendorQuotation");
        }

        if (approvalHistoryMap.containsKey("company") && approvalHistoryMap.get("company") != null) {
            companyObj = (Company) approvalHistoryMap.get("company");
        }

        if (approvalHistoryMap.containsKey("userid") && approvalHistoryMap.get("userid") != null) {
            userid = (String) approvalHistoryMap.get("userid");
        }

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("transtype", Constants.VENDOR_QUOTATION_APPROVAL);
        hashMap.put("transid", vendorQuoationObj.getID());
        hashMap.put("approvallevel", vendorQuoationObj.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
        hashMap.put("remark", "");//I think , it should be blank in edit mode
        hashMap.put("userid", userid);
        hashMap.put("companyid", companyObj.getCompanyID());
        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
       
        KwlReturnObject kmsg = null;
        String roleName = "Company User";
        kmsg = permissionHandlerDAOObj.getRoleofUser(userid);
        Iterator ite2 = kmsg.getEntityList().iterator();
        while (ite2.hasNext()) {
            Object[] row = (Object[]) ite2.next();
            roleName = row[1].toString();
        }

        approvalHistoryList.add(roleName);

        return approvalHistoryList;

    }
    
        
    public List saveSecurityGateEntry(JSONObject paramJobj) throws ServiceException, AccountingException, JSONException, ScriptException, MessagingException, SessionExpiredException, ParseException, UnsupportedEncodingException {
        SecurityGateEntry securityGateEntry = null;
        List newList = new ArrayList();
        List ll = new ArrayList();
        List mailParams = null;
        int pendingApprovalFlag = 0;
        String unlinkMessage = "";
        try {
            KwlReturnObject result = null;
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv", null)) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            String currentUser = paramJobj.getString(Constants.useridKey);
            String taxid = null;
//            taxid = paramJobj.optString("taxid", null);
            if (paramJobj.optString("taxid", null).equalsIgnoreCase("None")) {
                taxid = null;
            } else {
                taxid = paramJobj.optString("taxid", null);
            }
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
            String sequenceformat = paramJobj.optString(Constants.sequenceformat, null);
            String customfield = paramJobj.optString(Constants.customfield, null);
            double taxamount = StringUtil.getDouble(paramJobj.getString("taxamount"));
            String companyid = paramJobj.getString(Constants.companyKey);

            String createdby = paramJobj.getString(Constants.useridKey);
            String modifiedby = paramJobj.getString(Constants.useridKey);

            long createdon = System.currentTimeMillis();
            long updatedon = createdon;

            String shipLength = paramJobj.optString("shipLength", null);
            String deletedLinkedDocumentID = paramJobj.optString("deletedLinkedDocumentId", null);
            String currencyid = (paramJobj.optString(Constants.currencyKey, null) == null ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyKey));
            DateFormat df = authHandler.getDateOnlyFormat();
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate", "0"));
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParamsJson(paramJobj);

            KwlReturnObject extraCap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraPreferences = (ExtraCompanyPreferences) extraCap.getEntityList().get(0);

            String entryNumber = paramJobj.optString("number", null);
            String secGateEntryId = paramJobj.optString("invoiceid", null);
            String costCenterId = paramJobj.optString(Constants.costcenter, null);
            String nextAutoNo = "";
            String auditMessage = "";
            HashMap<String, Object> securityGatehm = new HashMap<>();
            Map<String, Object> oldpo = new HashMap<>();
            Map<String, Object> newAuditKey = new HashMap<>();

            synchronized (this) {
                SequenceFormat prevSeqFormat = null;
                if (!StringUtil.isNullOrEmpty(secGateEntryId)) { // For edit case
                    HashMap<String, Object> termReqMap = new HashMap<>();
                    KwlReturnObject pocount = accPurchaseOrderobj.getSGEEditCount(entryNumber, companyid, secGateEntryId);
                    if (pocount.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.security.securitygateentryno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        KwlReturnObject rst = accountingHandlerDAOobj.getObject(SecurityGateEntry.class.getName(), secGateEntryId);
                        SecurityGateEntry secGate = (SecurityGateEntry) rst.getEntityList().get(0);
                        prevSeqFormat = secGate.getSeqformat();
                        if (!sequenceformat.equals("NA")) {
                            nextAutoNo = entryNumber;
                        }
                        securityGatehm.put("id", secGateEntryId);

                        HashMap<String, Object> filterRequestMap = new HashMap<>();
                        filterRequestMap.put("companyid", companyid);
                        filterRequestMap.put("securityId", secGateEntryId);
                        filterRequestMap.put("locale", paramJobj.get("locale"));
                        Set<SecurityGateDetails> sgdetails = secGate.getRows();
                        if (sgdetails != null && !sgdetails.isEmpty()) {
                            for (SecurityGateDetails sgdetail : sgdetails) {
                                accPurchaseOrderobj.deleteSecurityGateEntryDetailsTermMap(sgdetail.getID(),companyid,filterRequestMap);        
                            }
                        }
//                        accPurchaseOrderobj.deleteSecurityGateEntryDetailsTermMap(filterRequestMap);
                        accPurchaseOrderobj.deleteSecurityGateEntryDetails(filterRequestMap);
                        termReqMap.put("poid", secGateEntryId);
                    }
                   
                }

                if (sequenceformat.equals("NA")) { // In case of NA checks wheather this number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_SecurityGateEntry_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        }
                    }
                }
            }

            String vendorId = paramJobj.getString("vendor");
            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorId);
            Vendor vendor = (Vendor) venresult.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(secGateEntryId)) { // Edit PO Case for updating address detail
                Map<String, Object> addressParams = new HashMap<>();
                String billingAddress = paramJobj.optString(Constants.BILLING_ADDRESS, null);
                if (!StringUtil.isNullOrEmpty(billingAddress)) {
                    addressParams = AccountingAddressManager.getAddressParamsJson(paramJobj, true); // true in case of vendor transaction to include vendor shipping address
                } else { // handling the cases when no address coming in edit case 
                    if (extraPreferences.isIsAddressFromVendorMaster()) {
                        addressParams = AccountingAddressManager.getDefaultVendorAddressParams(vendorId, companyid, accountingHandlerDAOobj);
                    } else {
                        addressParams = AccountingAddressManager.getDefaultVendorCompanyAddressParams(vendorId, companyid, accountingHandlerDAOobj);
                    }
                }
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(SecurityGateEntry.class.getName(), secGateEntryId);
                SecurityGateEntry securityGate = (SecurityGateEntry) returnObject.getEntityList().get(0);
                addressParams.put("id", securityGate.getBillingShippingAddresses() == null ? "" : securityGate.getBillingShippingAddresses().getID());
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                securityGatehm.put("billshipAddressid", bsa.getID());
            } else { // Other Cases for saving address detail
                boolean isDefaultAddress = paramJobj.optString("defaultAdress", null) != null ? Boolean.parseBoolean(paramJobj.getString("defaultAdress")) : false;
                Map<String, Object> addressParams = new HashMap<>();
                if (isDefaultAddress) {
                    if (extraPreferences.isIsAddressFromVendorMaster()) {
                        addressParams = AccountingAddressManager.getDefaultVendorAddressParams(vendorId, companyid, accountingHandlerDAOobj);
                    } else {
                        addressParams = AccountingAddressManager.getDefaultVendorCompanyAddressParams(vendorId, companyid, accountingHandlerDAOobj);
                    }
                } else {
                    addressParams = AccountingAddressManager.getAddressParamsJson(paramJobj, true); // true in case of vendor transaction to include vendor shipping address
                }
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                securityGatehm.put("billshipAddressid", bsa.getID());
            }

            if (sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(secGateEntryId)) {
                securityGatehm.put("entrynumber", entryNumber);
            } else {
                securityGatehm.put("entrynumber", "");
            }

            securityGatehm.put("externalCurrencyRate", externalCurrencyRate);
            securityGatehm.put("autogenerated", sequenceformat.equals("NA") ? false : true);
            securityGatehm.put(Constants.memo, paramJobj.optString(Constants.memo, null));
            if (!StringUtil.isNullOrEmpty(shipLength)) {
                securityGatehm.put("shipLength", shipLength);
            }
            securityGatehm.put(Constants.posttext, paramJobj.optString(Constants.posttext, null));
            securityGatehm.put("vendorid", paramJobj.getString("vendor"));
            securityGatehm.put("formtypeid", paramJobj.has("formtypeid") ? paramJobj.getString("formtypeid") : "");
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("GTAApplicable", null))) {
                securityGatehm.put("gtaapplicable", paramJobj.optBoolean("GTAApplicable", false));
            }
            securityGatehm.put("orderdate", df.parse(paramJobj.getString("billdate")));
            securityGatehm.put(Constants.Checklocktransactiondate, paramJobj.getString("billdate")); // ERP-16800-Without parsing date
            securityGatehm.put("duedate", df.parse(paramJobj.getString("duedate")));
            if (paramJobj.optString("shipdate", null) != null && !StringUtil.isNullOrEmpty(paramJobj.getString("shipdate"))) {
                securityGatehm.put("shipdate", df.parse(paramJobj.getString("shipdate")));
            }
            securityGatehm.put(Constants.shipvia, paramJobj.optString(Constants.shipvia, null));
            securityGatehm.put(Constants.fob, paramJobj.optString(Constants.fob, null));
            securityGatehm.put("termid", paramJobj.optString("termid", null));
            securityGatehm.put("currencyid", currencyid);
            securityGatehm.put("venbilladdress", paramJobj.optString("venbilladdress", null));
            securityGatehm.put("venshipaddress", paramJobj.optString("venshipaddress", null));
            securityGatehm.put("isfavourite", paramJobj.optString("isfavourite", null));
            securityGatehm.put("agent", paramJobj.optString("agent", null));
            securityGatehm.put("gstIncluded", paramJobj.optString("includingGST", null) == null ? false : Boolean.parseBoolean(paramJobj.getString("includingGST")));
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("perdiscount", null))) {
                securityGatehm.put("perDiscount", StringUtil.getBoolean(paramJobj.getString("perdiscount")));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("discount", null))) {
                securityGatehm.put("discount", StringUtil.getDouble(paramJobj.getString("discount")));
            }
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                securityGatehm.put("costCenterId", costCenterId);
            }
            if (paramJobj.has(Constants.SUPPLIERINVOICENO) && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.SUPPLIERINVOICENO))) {
                securityGatehm.put(Constants.SUPPLIERINVOICENO, paramJobj.optString(Constants.SUPPLIERINVOICENO));
            }


            securityGatehm.put("companyid", companyid);
            securityGatehm.put("createdby", createdby);
            securityGatehm.put("modifiedby", modifiedby);
            securityGatehm.put("createdon", createdon);
            securityGatehm.put("updatedon", updatedon);

            double subTotal = 0, taxAmt = 0, totalTermAmt = 0, totalAmt = 0, totalRowDiscount = 0, totalAmountinbase = 0;
            JSONArray jArr = new JSONArray(paramJobj.getString(Constants.detail));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);

                double qrate = authHandler.roundUnitPrice(jobj.getDouble("rate"), companyid);
                double quantity = authHandler.roundQuantity(jobj.getDouble("quantity"), companyid);
                boolean gstIncluded = paramJobj.optString("includingGST", null) == null ? false : Boolean.parseBoolean(paramJobj.getString("includingGST"));
                if (gstIncluded) {
                    qrate = authHandler.roundUnitPrice(jobj.getDouble("rateIncludingGst"), companyid);
                }

                double quotationPrice = authHandler.round(quantity * qrate, companyid);
                double discountQD = authHandler.round(jobj.optDouble("prdiscount", 0), companyid);
                double discountPerRow = 0;

                if (jobj.optInt("discountispercent", 0) == 1) {
                    discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                } else {
                    discountPerRow = discountQD;
                }

                totalRowDiscount += discountPerRow;

            }

            totalRowDiscount = authHandler.round(totalRowDiscount, companyid);
            securityGatehm.put("totallineleveldiscount", totalRowDiscount);

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("subTotal", null))) {
                subTotal = Double.parseDouble(paramJobj.getString("subTotal"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxamount", null))) {
                taxAmt = Double.parseDouble(paramJobj.getString("taxamount"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("invoicetermsmap", null))) {
                JSONArray termDetailsJArr = new JSONArray(paramJobj.getString("invoicetermsmap"));
                for (int cnt = 0; cnt < termDetailsJArr.length(); cnt++) {
                    JSONObject temp = termDetailsJArr.getJSONObject(cnt);
                    if (temp != null) {
                        double termAmount = Double.parseDouble(temp.getString("termamount"));
                        totalTermAmt += termAmount;
                    }
                }
            }
            totalAmt = subTotal + taxAmt + totalTermAmt;

            securityGatehm.put("totalamount", totalAmt);

            HashMap<String, Object> filterRequestParams = new HashMap<>();
            filterRequestParams.put(Constants.companyKey, companyid);
            filterRequestParams.put(Constants.globalCurrencyKey, paramJobj.getString(Constants.globalCurrencyKey));
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalAmt, currencyid, df.parse(paramJobj.getString("billdate")), externalCurrencyRate);
            totalAmountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
            securityGatehm.put("totalamountinbase", totalAmountinbase);

            KwlReturnObject descbAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalRowDiscount, currencyid, df.parse(paramJobj.getString("billdate")), externalCurrencyRate);
            double descountinBase = authHandler.round((Double) descbAmtTax.getEntityList().get(0), companyid);

            securityGatehm.put("discountinbase", descountinBase);

            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
                securityGatehm.put("taxid", taxid);
            } else {
                securityGatehm.put("taxid", taxid); // Put taxid as null if the PO doesnt have any total tax included. (To avoid problem while editing PO)
            }
            Set<SecurityGateDetails> segdetails = null;
            Set<ExpensePODetail> expenseDetails = null;
            
            KwlReturnObject poresult = accPurchaseOrderobj.saveSecurityGateEntry(securityGatehm);
            securityGateEntry = (SecurityGateEntry) poresult.getEntityList().get(0);
            
            /**
             * Save GST History Customer/Vendor data.
             */
            if (securityGateEntry.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                paramJobj.put("docid", securityGateEntry.getID());
                paramJobj.put("moduleid", Constants.Acc_SecurityGateEntry_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(paramJobj);
            }
            /*
            Saving Global level custom fields for Security Gate Entry 
            
            */            
            if (!StringUtil.isNullOrEmpty(customfield)) {
                HashMap<String, Object> SOMap = new HashMap<String, Object>();
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "SecurityGateEntry");
                customrequestParams.put("moduleprimarykey", "SgeID");
                customrequestParams.put("modulerecid", securityGateEntry.getID());;
                customrequestParams.put(Constants.moduleid, Constants.Acc_SecurityGateEntry_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                SOMap.put("id", securityGateEntry.getID());
                SOMap.put(Constants.companyKey, companyid);
                SOMap.put("orderdate", df.parse(paramJobj.getString("billdate")));
                customrequestParams.put("customdataclasspath", Constants.Acc_SecurityGateEntry_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    SOMap.put("securitygateentrycustomdataref", securityGateEntry.getID());
                    accPurchaseOrderobj.saveSecurityGateEntry(SOMap);
                }
            }
            
            List rowDetails = saveSecurityGateEntryRows(paramJobj, currencyid, securityGateEntry, externalCurrencyRate, companyid, GlobalParams);
            segdetails = (HashSet) rowDetails.get(0);
            securityGateEntry.setRows(segdetails);
            JSONArray productDiscountJArr = new JSONArray();
            if (segdetails != null) {
                for (SecurityGateDetails segDetail : segdetails) {
                    String productId = segDetail.getProduct().getID();
                    double discountVal = segDetail.getDiscount();
                    int isDiscountPercent = segDetail.getDiscountispercent();
                    if (isDiscountPercent == 1) {
                        discountVal = (segDetail.getQuantity() * segDetail.getRate()) * (discountVal / 100);
                    }
                    KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, df.parse(paramJobj.getString("billdate")), externalCurrencyRate);
                    double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                    discAmountinBase = authHandler.round(discAmountinBase, companyid);
                    JSONObject productDiscountObj = new JSONObject();
                    productDiscountObj.put("productId", productId);
                    productDiscountObj.put("discountAmount", discAmountinBase);
                    productDiscountJArr.put(productDiscountObj);
                }
            }

            String purchaseOrderTerms = paramJobj.optString("invoicetermsmap", null);
            if (StringUtil.isAsciiString(purchaseOrderTerms)) {
                if (new JSONArray(purchaseOrderTerms).length() > 0) {
                    securityGatehm.put(Constants.termsincludegst, paramJobj.optBoolean(Constants.termsincludegst, false));
                }
            }

            String linkMode = paramJobj.optString("fromLinkCombo", null);
             String linkNumberStr = "";
            if (paramJobj.has("linkNumber") && paramJobj.optString("linkNumber", null) != null) {
                linkNumberStr = (String) paramJobj.get("linkNumber");
            }
            String[] linkNumbers = linkNumberStr.split(",");
            String linkedDocuments = "";
            if (!StringUtil.isNullOrEmpty(linkMode) && linkNumbers.length > 0) {
                    if (linkMode.equalsIgnoreCase("Purchase Order")) {
                        for (int i = 0; i < linkNumbers.length; i++) {
                            if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
                                updatePOisOpenAndLinkingWithSGE(linkNumbers[i], securityGateEntry.getID());
                            }
                        }
                    }
            }
            
            if (isEdit == true) { 
                DateFormat sdf = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);
                if (securityGateEntry.getShipdate() != null) {
                    securityGatehm.put("AuditShipDate", sdf.format(securityGateEntry.getShipdate())); // New Ship Date
                } else {
                    securityGatehm.put("AuditShipDate", "");
                }
                if (securityGateEntry.getDueDate() != null) {
                    securityGatehm.put("AuditDuedate", sdf.format(securityGateEntry.getDueDate())); // New Due Date
                } else {
                    securityGatehm.put("AuditDuedate", "");
                }
                if (securityGateEntry.getSecurityDate() != null) {
                    securityGatehm.put("AuditOrderDate", sdf.format(securityGateEntry.getSecurityDate()));  //New Order Date
                } else {
                    securityGatehm.put("AuditOrderDate", "");
                }
            }
            if (isEdit == true) {
                int moduleid = Constants.Acc_SecurityGateEntry_ModuleId;
                auditMessage = AccountingManager.BuildAuditTrialMessage(securityGatehm, oldpo, moduleid, newAuditKey);
            }
            ll.add(securityGateEntry);
            ll.add(auditMessage);
            String butPendingForApproval = "";
            double subTotalAmount = 0;
            double taxAmount = 0;
            double totalTermAmount = 0;
            double totalAmount = 0;

            HashMap<String, Object> poApproveMap = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("subTotal", null))) {
                subTotalAmount = Double.parseDouble(paramJobj.getString("subTotal"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxamount", null))) {
                taxAmount = Double.parseDouble(paramJobj.getString("taxamount"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("invoicetermsmap", null))) {
                JSONArray termDetailsJArr = new JSONArray(paramJobj.getString("invoicetermsmap"));
                for (int cnt = 0; cnt < termDetailsJArr.length(); cnt++) {
                    JSONObject temp = termDetailsJArr.getJSONObject(cnt);
                    if (temp != null) {
                        double termAmount = Double.parseDouble(temp.getString("termamount"));
                        totalTermAmount += termAmount;
                    }
                }
            }
            totalAmount = subTotalAmount + taxAmount + totalTermAmount;
            KwlReturnObject tAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, totalAmount, currencyid, df.parse(paramJobj.getString("billdate")), externalCurrencyRate);
            double totalAmountinBase = 0;
            totalAmountinBase = (Double) tAmount.getEntityList().get(0);
            int approvalStatusLevel = 11;
            List approvedlevel = null;
            int level = (isEdit && !isCopy) ? 0 : securityGateEntry.getApprovestatuslevel();
            poApproveMap.put("companyid", paramJobj.getString(Constants.companyKey));
            poApproveMap.put("level", level);
            poApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmountinBase, companyid)));
            poApproveMap.put("currentUser", currentUser);
            poApproveMap.put("fromCreate", true);
            poApproveMap.put("isEdit", isEdit);
            poApproveMap.put("productDiscountMapList", productDiscountJArr);
            poApproveMap.put("moduleid", Constants.Acc_SecurityGateEntry_ModuleId);

            securityGateEntry.setApprovestatuslevel(11);
            if (approvalStatusLevel != 11) {
                butPendingForApproval = " " + messageSource.getMessage("acc.field.butpendingforApproval", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
            }

            String fullShippingAddress = "";
            if (securityGateEntry.getBillingShippingAddresses() != null) {
                fullShippingAddress = securityGateEntry.getBillingShippingAddresses().getFullShippingAddress();
            }
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) rdresult.getEntityList().get(0);

            ll.add(totalAmount);
            ll.add(approvalStatusLevel);
            ll.add(butPendingForApproval);
            ll.add(mailParams);
            ll.add(fullShippingAddress);
            ll.add(linkedDocuments);
            ll.add(unlinkMessage);
            String moduleName = Constants.moduleID_NameMap.get(Constants.Acc_SecurityGateEntry_ModuleId);
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), paramJobj.getString(Constants.companyKey));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(secGateEntryId)) {
                    sendmail = true;
                }
                if (sendmail) {
                    String userMailId = "", userName = "", currentUserid = "";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> requestParams = AccountingManager.getEmailNotificationParamsJson(paramJobj);
                    if (requestParams.containsKey("userfullName") && requestParams.get("userfullName") != null) {
                        userName = (String) requestParams.get("userfullName");
                    }
                    if (requestParams.containsKey("usermailid") && requestParams.get("usermailid") != null) {
                        userMailId = (String) requestParams.get("usermailid");
                    }
                    if (requestParams.containsKey(Constants.useridKey) && requestParams.get(Constants.useridKey) != null) {
                        currentUserid = (String) requestParams.get(Constants.useridKey);
                    }
                    List<String> mailIds = new ArrayList();
                    if (!StringUtil.isNullOrEmpty(userMailId)) {
                        mailIds.add(userMailId);
                    }
                    /*
                     * if Edit mail option is true then get userid and Email id
                     * of document creator.
                     */
                    if (isEditMail) {
                        if (securityGateEntry != null && securityGateEntry.getCreatedby() != null) {
                            createdByEmail = securityGateEntry.getCreatedby().getEmailID();
                            createdById = securityGateEntry.getCreatedby().getUserID();
                        }
                        /*
                         * if current user userid == document creator userid
                         * then don't add creator email ID in List.
                         */
                        if (!StringUtil.isNullOrEmpty(createdByEmail) && !(currentUserid.equalsIgnoreCase(createdById))) {
                            mailIds.add(createdByEmail);
                        }
                    }
                    String[] temp = new String[mailIds.size()];
                    String[] tomailids = mailIds.toArray(temp);
                    String vqNumber = entryNumber;
                    accountingHandlerDAOobj.sendSaveTransactionEmails(vqNumber, moduleName, tomailids, userName, isEditMail, companyid);
                }
            }

        }catch (ParseException ex) {
            throw ServiceException.FAILURE("saveSecurityGateEntry : " + ex.getMessage(), ex);
        }
        return ll;
    }
    
    public void updatePOisOpenAndLinkingWithSGE(String linking, String grorderId) throws ServiceException {
        try {
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), linking);
            PurchaseOrder purchaseOrder = (PurchaseOrder) rdresult.getEntityList().get(0);
            HashMap hMap = new HashMap();

            boolean isopen = false;
            Set<PurchaseOrderDetail> rows = purchaseOrder.getRows();
            Iterator itrPOD = rows.iterator();
            while (itrPOD.hasNext()) {
                PurchaseOrderDetail row = (PurchaseOrderDetail) itrPOD.next();
                KwlReturnObject idresult = accPurchaseOrderobj.getSGIDFromPOD(row.getID(), grorderId);
                List list = idresult.getEntityList();
                Iterator iteGRD = list.iterator();
                double qua = 0.0;
                while (iteGRD.hasNext()) {
                    SecurityGateDetails sge = (SecurityGateDetails) iteGRD.next();
                    qua += sge.getQuantity();
                }
                /*
                ERM-1099
                Balance quantity will not be changed when creating SGE 
                if (row != null) {
                    if (row != null && !StringUtil.isNullOrEmpty(row.getID()) && qua > 0) {
                        HashMap poMap = new HashMap();
                        poMap.put("podetails", row.getID());
                        poMap.put("companyid", row.getCompany().getCompanyID());
                        poMap.put("balanceqty", qua);
                        poMap.put("add", false);
                        accCommonTablesDAO.updatePurchaseOrderStatus(poMap);
                    }
                }*/
                double addobj = row.getBalanceqty();
                if (addobj > 0) {
                    isopen = true;
                }
            }
            hMap.put("isOpen", isopen);
            hMap.put("purchaseOrder", purchaseOrder);
            hMap.put("value", "3");
           accPurchaseOrderobj.updatePOLinkflag(hMap);
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updatePOisOpenAndLinkingWithGR : " + ex.getMessage(), ex);
        }
    }

        public List saveExpensePORows(Map<String,Object> expenseParams) throws JSONException, ServiceException{
        Set<ExpensePODetail> expensePODetails=new HashSet<ExpensePODetail>();
        List ll = new ArrayList();
        String companyid=expenseParams.get("companyid").toString();
        PurchaseOrder order=(PurchaseOrder)expenseParams.get("poObject");
        
        KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) cmp.getEntityList().get(0);
        JSONArray jArr = new JSONArray(expenseParams.get("expensedetail").toString());
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            HashMap<String, Object> poExpenseDataMap = new HashMap<>();
            poExpenseDataMap.put("srno",i+1);
            poExpenseDataMap.put("rate",jobj.getDouble("rate"));
            poExpenseDataMap.put("isdebit",jobj.getBoolean("debit"));
            poExpenseDataMap.put("rateincludinggstex",jobj.getDouble("rateIncludingGstEx"));
            poExpenseDataMap.put("calamount",jobj.getDouble("calamount"));
            poExpenseDataMap.put("companyid",companyid);
            poExpenseDataMap.put("poid",order.getID());
            poExpenseDataMap.put(Constants.BALANCE_Amount,jobj.optDouble("calamount",0));
                poExpenseDataMap.put("desc", StringUtil.DecodeText(jobj.optString("desc")));
                poExpenseDataMap.put("desc", jobj.optString("desc"));
            // Account : it is mandatory
            poExpenseDataMap.put("accountid", jobj.getString("accountid"));
           
            //Discount : if given
            Discount discount = null;
            double disc = jobj.getDouble("prdiscount");
            int rowdisc = jobj.getInt("discountispercent");
            if (disc != 0.0) {
                Map<String, Object> discMap = new HashMap();
                discMap.put("discount", disc);
                /*
                 * rowdisc=1 (discountispercent is percentage) rowdisc=0
                 * (discountispercent is flat)
                 */
                discMap.put("inpercent", (rowdisc == 1) ? true : false);
                discMap.put("originalamount", jobj.getDouble("rate"));
                discMap.put("companyid", companyid);
                KwlReturnObject dscresult = accDiscountobj.updateDiscount(discMap);
                discount = (Discount) dscresult.getEntityList().get(0);
                poExpenseDataMap.put("discountid", discount.getID());
            }
            
            //Product Tax : if given
            String rowtaxid = jobj.getString("prtaxid");
            if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                poExpenseDataMap.put("taxid", rowtaxid);
                poExpenseDataMap.put("rowtaxamount", jobj.getDouble("taxamount"));
                poExpenseDataMap.put(Constants.isUserModifiedTaxAmount, jobj.optBoolean(Constants.isUserModifiedTaxAmount, false));//ERM-1085
            }
            KwlReturnObject result = accPurchaseOrderobj.saveExpensePurchaseOrderDetails(poExpenseDataMap);
            ExpensePODetail row = (ExpensePODetail) result.getEntityList().get(0);
            //Saving custom field Data If any
            if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "ExpensePODetail");//this is getter/setter part of pojo class method name  
                customrequestParams.put("moduleprimarykey", "ExpensePODetailID");
                customrequestParams.put("modulerecid", row.getID());  
                customrequestParams.put("recdetailId", row.getID());
                customrequestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId ); // Check
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_ExpensePODetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            }
            expensePODetails.add(row);
        }
        ll.add(expensePODetails);
        return  ll;
    }
        
    @Override
    /**
     * @param mailParameters (String companyid, String ruleId, String prNumber, String fromName, boolean hasApprover, int moduleid, String createdby,boolean isEdit, String PAGE_URL)
     * @throws ServiceException 
     */
    public void sendMailToApprover(Map<String, Object> mailParameters) throws ServiceException {
        KwlReturnObject cap = null;
        if (mailParameters.containsKey(Constants.companyid)) {
            cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String) mailParameters.get(Constants.companyid));
        }
        boolean hasApprover = false;
        boolean isEdit = false;
        int moduleid = 0;
        int level=0;
        String createdby = "";
        if(mailParameters.containsKey(Constants.createdBy)){
            createdby = (String) mailParameters.get(Constants.createdBy);
        }
        if(mailParameters.containsKey(Constants.moduleid)){
            moduleid = (int) mailParameters.get(Constants.moduleid);
        }
        if(mailParameters.containsKey(Constants.hasApprover)){
            hasApprover = (boolean) mailParameters.get(Constants.hasApprover);
        }
        if(mailParameters.containsKey(Constants.isEdit)){
            isEdit = (boolean) mailParameters.get(Constants.isEdit);
        }
        if(mailParameters.containsKey("level")){
            level = (int) mailParameters.get("level");
        }
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
        String transactionName = "";
        String transactionNo = "";
        switch (moduleid) {
            case Constants.Acc_Purchase_Order_ModuleId:
                transactionName = "Purchase Order";
                transactionNo = "Purchase Order Number";
                break;
            case Constants.Acc_Vendor_Quotation_ModuleId:
                transactionName = "Vendor Quotation";
                transactionNo = "Vendor Quotation Number";
                break;
            case Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId:
                transactionName = "Asset Vendor Quotation";
                transactionNo = "Asset Vendor Quotation Number";
                break;
        }
        String requisitionApprovalSubject = transactionName + ": %s - Approval Notification";
        String requisitionApprovalHtmlMsg = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
                + "a:link, a:visited, a:active {\n"
                + " 	color: #03C;"
                + "}\n"
                + "body {\n"
                + "	font-family: Arial, Helvetica, sans-serif;"
                + "	color: #000;"
                + "	font-size: 13px;"
                + "}\n"
                + "</style><body>"
                + "<p>Hi All,</p>"
                + "<p></p>"
                + "<p>%s has "+ (isEdit?"updated ":"created ")+ transactionName + " %s and sent it to you for approval. at level "+(level+1)+"</p>"
                + "<p>Please review and approve it (" + transactionNo + ": %s).</p>"
                + "<p>Company Name:- %s</p>"
                + "<p>Please check on Url:- %s</p>"
                + "<p></p>"
                + "<p>Thanks</p>"
                + "<p>This is an auto generated email. Do not reply<br>";
        String requisitionApprovalPlainMsg = "Hi All,\n\n"
                + "%s has "+ (isEdit?"updated ":"created ")+ transactionName + " %s and sent it to you for approval."+(level+1)+"\n"
                + "Please review and approve it (" + transactionNo + ": %s).\n\n"
                + "Company Name:- %s \n"
                + "Please check on Url:- %s \n"
                + "Thanks\n\n"
                + "This is an auto generated email. Do not reply\n";
        try {
            if (hasApprover && preferences.isSendapprovalmail()) { // If allow to send approval mail in company account preferences
                KwlReturnObject returnObject = null;
                if(mailParameters.containsKey(Constants.companyid)){
                    returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), (String) mailParameters.get(Constants.companyid));
                }
                Company company = (Company) returnObject.getEntityList().get(0);
                String fromEmailId = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String companyName = company.getCompanyName();
                String subject = "";
                String htmlMsg = "";
                String plainMsg = "";
                if (mailParameters.containsKey(Constants.prNumber) ) {
                     subject = String.format(requisitionApprovalSubject, (String) mailParameters.get(Constants.prNumber));
                }
                if (mailParameters.containsKey(Constants.prNumber) && mailParameters.containsKey(Constants.fromName) && mailParameters.containsKey(Constants.PAGE_URL)) {
                     htmlMsg = String.format(requisitionApprovalHtmlMsg, (String) mailParameters.get(Constants.fromName), (String) mailParameters.get(Constants.prNumber),  (String) mailParameters.get(Constants.prNumber), companyName, (String) mailParameters.get(Constants.PAGE_URL));
                     plainMsg = String.format(requisitionApprovalPlainMsg, (String) mailParameters.get(Constants.fromName), (String) mailParameters.get(Constants.prNumber), (String) mailParameters.get(Constants.prNumber), companyName, (String) mailParameters.get(Constants.PAGE_URL));
                }
                ArrayList<String> emailArray = new ArrayList<>();
                String[] emails = {};
                String userDepartment = null;
                KwlReturnObject returnObjectRes = null;

                HashMap<String, Object> dataMap = new HashMap<>();
                if (mailParameters.containsKey(Constants.ruleid)) {
                    dataMap.put(Constants.ruleid, (String) mailParameters.get(Constants.ruleid));
                }
                if(mailParameters.containsKey(Constants.companyid)){
                    dataMap.put(Constants.companyKey, (String) mailParameters.get(Constants.companyid));
                }
                dataMap.put("checkdeptwiseapprover", true);

                KwlReturnObject userResult1 = accMultiLevelApprovalDAOObj.checkDepartmentWiseApprover(dataMap);
                if (userResult1 != null && userResult1.getEntityList() != null && userResult1.getEntityList().size() > 0) {
                    User user = null;
                    if (!StringUtil.isNullObject(createdby)) {
                        returnObjectRes = accountingHandlerDAOobj.getObject(User.class.getName(), createdby);
                        user = (User) returnObjectRes.getEntityList().get(0);
                    }
                    if (user != null && !StringUtil.isNullObject(user.getDepartment())) {
                        userDepartment = user.getDepartment();
                        dataMap.put("userdepartment", userDepartment);
                    }
                }
                KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(dataMap);

                if (userResult.getEntityList() != null && userResult.getEntityList().size() <= 0 && !StringUtil.isNullOrEmpty(userDepartment)) {
                    dataMap.remove("userdepartment");
                    userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(dataMap);
                }
                Iterator useritr = userResult.getEntityList().iterator();
                while (useritr.hasNext()) {
                    Object[] userrow = (Object[]) useritr.next();
                    emailArray.add(userrow[3].toString());
                }
                emails = emailArray.toArray(emails);
                if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                    String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                    emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                }
                if (emails.length > 0) {
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(emails, subject, htmlMsg, plainMsg, fromEmailId, smtpConfigMap);
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    /*-------Function to send approval mail if check "Allow Sending Approval Mail" is activated from system preferences---------*/
    @Override
    public void sendApprovalMailIfAllowedFromSystemPreferences(HashMap emailMap) throws ServiceException {

        String userName = "";
        Company company = null;
        PurchaseOrder cqObj = null;
        String baseUrl = "";
        CompanyAccountPreferences preferences = null;
        boolean isFixedAsset =false;
        String approvalpendingStatusmsg = "";
        HashMap<String, Object> ApproveMap =new HashMap();

        if (emailMap.containsKey("userName") && emailMap.get("userName") != null) {
            userName = (String)emailMap.get("userName");
        }
        if (emailMap.containsKey("company") && emailMap.get("company") != null) {
             company = (Company)emailMap.get("company");
        }

        if (emailMap.containsKey("purchaseOrder") && emailMap.get("purchaseOrder") != null) {
            cqObj = (PurchaseOrder)emailMap.get("purchaseOrder");
        }

        if (emailMap.containsKey("baseUrl") && emailMap.get("baseUrl") != null) {
            baseUrl = (String)emailMap.get("baseUrl");
        }

        if (emailMap.containsKey("preferences") && emailMap.get("preferences") != null) {
            preferences = (CompanyAccountPreferences)emailMap.get("preferences");
        }
        
          if (emailMap.containsKey("isFixedAsset") && emailMap.get("isFixedAsset") != null) {
            isFixedAsset = (boolean)emailMap.get("isFixedAsset");
        }
          if (emailMap.containsKey("ApproveMap") && emailMap.get("ApproveMap") != null) {
                ApproveMap = (HashMap<String, Object>) emailMap.get("ApproveMap");
        }

        int level = cqObj.getApprovestatuslevel();
        String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        String creatormail = company.getCreator().getEmailID();
        String documentcreatoremail = (cqObj != null && cqObj.getCreatedby() != null) ? cqObj.getCreatedby().getEmailID() : "";
        String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
        String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
        String creatorname = fname + " " + lname;
        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
        ArrayList<String> emailArray = new ArrayList<>();
        qdDataMap.put(Constants.companyKey, company.getCompanyID());
        qdDataMap.put("level", level);
        qdDataMap.put(Constants.moduleid, isFixedAsset ? Constants.Acc_FixedAssets_Purchase_Order_ModuleId : Constants.Acc_Purchase_Order_ModuleId);
//        emailArray = commonFnControllerService.getUserApprovalEmail(qdDataMap);
        emailArray.add(creatormail);
        if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
            emailArray.add(documentcreatoremail);
        }
        String[] emails = {};
        emails = emailArray.toArray(emails);
        if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
            String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
            emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
        }
              if (cqObj.getApprovestatuslevel() < 11) {
                  qdDataMap.put("ApproveMap", ApproveMap);
                  approvalpendingStatusmsg = commonFnControllerService.getApprovalstatusmsg(qdDataMap);
              }
        Map<String, Object> mailParameters = new HashMap();
        mailParameters.put("Number", cqObj.getPurchaseOrderNumber());
        mailParameters.put("userName", userName);
        mailParameters.put("emails", emails);
        mailParameters.put("sendorInfo", sendorInfo);
        mailParameters.put("addresseeName", "All");
        mailParameters.put("companyid", company.getCompanyID());
        mailParameters.put("baseUrl", baseUrl);
        mailParameters.put("approvalstatuslevel", level);
        mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
        if (emails.length > 0) {
            if (isFixedAsset) {
                mailParameters.put("moduleName", Constants.ASSET_PURCHASE_ORDER);
                accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
            } else {
                mailParameters.put("moduleName", Constants.ACC_PURCHASE_ORDER);
                accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
            }
        }
    }
    
    
    
    
   //  @Override
    public void sendApprovalMailForVQIfAllowedFromSystemPreferences(HashMap emailMap) throws ServiceException {

        String userName = "";
        Company company = null;
        VendorQuotation vendorQuotationObj = null;
        String baseUrl = "";
        CompanyAccountPreferences preferences = null;
        boolean isFixedAsset =false;
        HashMap<String, Object> soApproveMap =null;
        
        if (emailMap.containsKey("userName") && emailMap.get("userName") != null) {
            userName = (String)emailMap.get("userName");
        }
        if (emailMap.containsKey("company") && emailMap.get("company") != null) {
             company = (Company)emailMap.get("company");
        }

        if (emailMap.containsKey("vendorQuotation") && emailMap.get("vendorQuotation") != null) {
            vendorQuotationObj = (VendorQuotation)emailMap.get("vendorQuotation");
        }

        if (emailMap.containsKey("baseUrl") && emailMap.get("baseUrl") != null) {
            baseUrl = (String)emailMap.get("baseUrl");
        }

        if (emailMap.containsKey("preferences") && emailMap.get("preferences") != null) {
            preferences = (CompanyAccountPreferences)emailMap.get("preferences");
        }
        
          if (emailMap.containsKey("isFixedAsset") && emailMap.get("isFixedAsset") != null) {
            isFixedAsset = (boolean)emailMap.get("isFixedAsset");
        }
        if (emailMap.containsKey("ApproveMap") && emailMap.get("ApproveMap") != null) {
            soApproveMap = (HashMap<String, Object>) emailMap.get("ApproveMap");
        }
        int level = vendorQuotationObj.getApprovestatuslevel();
        String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        String creatormail = company.getCreator().getEmailID();
        String documentcreatoremail = (vendorQuotationObj != null && vendorQuotationObj.getCreatedby() != null) ? vendorQuotationObj.getCreatedby().getEmailID() : "";
        String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
        String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
        String creatorname = fname + " " + lname;
        String approvalpendingStatusmsg = "";
        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
        ArrayList<String> emailArray = new ArrayList<>();
        qdDataMap.put(Constants.companyKey, company.getCompanyID());
        qdDataMap.put("level", level);
        qdDataMap.put(Constants.moduleid, isFixedAsset ? Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId : Constants.Acc_Vendor_Quotation_ModuleId);
//        emailArray = commonFnControllerService.getUserApprovalEmail(qdDataMap);
        emailArray.add(creatormail);
        if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
            emailArray.add(documentcreatoremail);
        }
        String[] emails = {};
        emails = emailArray.toArray(emails);
        if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
            String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
            emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
        }
        if (vendorQuotationObj.getApprovestatuslevel() < 11) {
            qdDataMap.put("ApproveMap", soApproveMap);
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
        Map<String, Object> mailParameters = new HashMap();
        mailParameters.put("Number", vendorQuotationObj.getQuotationNumber());
        mailParameters.put("userName", userName);
        mailParameters.put("emails", emails);
        mailParameters.put("sendorInfo", sendorInfo);
        mailParameters.put("addresseeName", "All");
        mailParameters.put("companyid", company.getCompanyID());
        mailParameters.put("baseUrl", baseUrl);
        mailParameters.put("approvalstatuslevel", vendorQuotationObj.getApprovestatuslevel());
        mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
        if (emails.length > 0) {
            if (isFixedAsset) {
                mailParameters.put("moduleName", Constants.ASSET_VENDOR_QUOTATION);
                accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
            } else {
                mailParameters.put("moduleName", Constants.VENDOR_QUOTATION);
                accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
            }
        }
    }
    
    private void setValuesForAuditTrialMessage(PurchaseOrder po, JSONObject paramJobj, Map<String, Object> oldpo, Map<String, Object> pohm, Map<String, Object> newAuditKey) throws SessionExpiredException, JSONException {
        DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);
        try {
            // Setting values in map for oldpo
            if (po != null) {
                if (po.getTerm().getID() != null) {
                    KwlReturnObject olddebittermresult = accountingHandlerDAOobj.getObject(Term.class.getName(), po.getTerm().getID());
                    Term term = (Term) olddebittermresult.getEntityList().get(0);
                    oldpo.put(Constants.DebitTermName, term.getTermname());
                    newAuditKey.put(Constants.DebitTermName, "Debit Term");
                }
                KwlReturnObject currobretrurnlist = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), po.getCurrency().getCurrencyID());
                KWLCurrency oldcurrencyobj = (KWLCurrency) currobretrurnlist.getEntityList().get(0);
                KwlReturnObject venobretrurnlist = accountingHandlerDAOobj.getObject(Vendor.class.getName(), po.getVendor().getID());
                Vendor oldvendor = (Vendor) venobretrurnlist.getEntityList().get(0);
                oldpo.put(Constants.VendorName, oldvendor.getName());
                newAuditKey.put(Constants.VendorName, "Vendor");
                oldpo.put("entrynumber", po.getPurchaseOrderNumber());
                newAuditKey.put("entrynumber", "Entry Number");
                oldpo.put(Constants.CurrencyName, oldcurrencyobj.getName()); // Currency name
                newAuditKey.put(Constants.CurrencyName, "Currency");
                oldpo.put("memo", StringUtil.isNullOrEmpty(po.getMemo()) ? "" : po.getMemo());
                newAuditKey.put("memo", "Memo");
                oldpo.put("shipvia", StringUtil.isNullOrEmpty(po.getShipvia()) ? "" : po.getShipvia());
                newAuditKey.put("shipvia", "Ship Via");
                oldpo.put("fob", StringUtil.isNullOrEmpty(po.getFob()) ? "" : po.getFob());
                newAuditKey.put("fob", "FOB");
                oldpo.put("AuditDuedate", po.getDueDate() != null ? df.format(po.getDueDate()) : "");
                newAuditKey.put("AuditDuedate", "Due Date");
                oldpo.put("AuditShipDate", po.getShipdate() != null ? df.format(po.getShipdate()) : "");
                newAuditKey.put("AuditShipDate", "Ship Date");
                oldpo.put("AuditOrderDate", po.getOrderDate() != null ? df.format(po.getOrderDate()) : "");
                newAuditKey.put("AuditOrderDate", "Purchase Order Date");
            }

            KwlReturnObject debittermresult = accountingHandlerDAOobj.getObject(Term.class.getName(), paramJobj.getString("termid"));
            Term term = (Term) debittermresult.getEntityList().get(0);
            pohm.put(Constants.DebitTermName, term.getTermname()); // Debit Term Name
            KwlReturnObject newcurrencyreturnobj = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.getString(Constants.currencyKey));
            KWLCurrency newcurrencyobj = (KWLCurrency) newcurrencyreturnobj.getEntityList().get(0);
            pohm.put(Constants.CurrencyName, newcurrencyobj.getName()); // Currencey name
            KwlReturnObject venobretrurnlist = accountingHandlerDAOobj.getObject(Vendor.class.getName(), paramJobj.getString("vendor"));
            Vendor newvendor = (Vendor) venobretrurnlist.getEntityList().get(0);
            pohm.put(Constants.VendorName, newvendor.getName()); // Vendor Name

        } catch (Exception ex) {
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void savePurchaseOrderOtherDetails(JSONObject paramJobj, String purchaseOrderId, String companyid) throws ServiceException, AccountingException {
        try {
            HashMap<String, Object> podDataMap = new HashMap<>();
            podDataMap.put("companyid", companyid);
            podDataMap.put("poid", purchaseOrderId);
            podDataMap.put("poyourref", paramJobj.optString("poyourref", null));
            podDataMap.put("podelyterm", paramJobj.optString("delyterm", null));
            podDataMap.put("poinvoiceto", paramJobj.optString("invoiceto", null));
            podDataMap.put("podelydate", paramJobj.optString("delydate", null));
            podDataMap.put("podept", paramJobj.optString("podept", null));
            podDataMap.put("porequestor", paramJobj.optString("requestor", null));
            podDataMap.put("poproject", paramJobj.optString("project", null));
            podDataMap.put("pomerno", paramJobj.optString("merno", null));
            KwlReturnObject result = accPurchaseOrderobj.savePurchaseOrderOtherDetails(podDataMap);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("savePurchaseOrderOtherDetails : " + ex.getMessage(), ex);
        }
    }

    private List savePurchaseOrderRows(JSONObject paramJobj, String currencyid, PurchaseOrder purchaseOrder, Double externalCurrencyRate, String companyid, HashMap<String, Object> GlobalParams) throws ServiceException, AccountingException, UnsupportedEncodingException {
        HashSet rows = new HashSet();
        List ll = new ArrayList();
        ArrayList<String> prodList = new ArrayList<>();
        try {
            double totalAmount = 0.0;
            boolean isConsignment = false;
            String isConsignmentStr = paramJobj.optString("isConsignment", null);
            if (!StringUtil.isNullOrEmpty(isConsignmentStr)) {
                isConsignment = Boolean.parseBoolean(isConsignmentStr);
            }
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            KwlReturnObject ecpresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) ecpresult.getEntityList().get(0);
            Country country = preferences.getCompany().getCountry();
            String userid = paramJobj.getString(Constants.useridKey);
            DateFormat df = authHandler.getDateOnlyFormat();
            boolean includeProductTax = StringUtil.isNullOrEmpty(paramJobj.optString("includeprotax", null)) ? false : Boolean.parseBoolean(paramJobj.getString("includeprotax"));
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString("isFixedAsset", null))) ? Boolean.parseBoolean(paramJobj.getString("isFixedAsset")) : false;
            boolean isPOFromVQ = false;
            boolean isEdit = Boolean.parseBoolean(paramJobj.optString("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv", null)) ? false : Boolean.parseBoolean(paramJobj.optString("copyInv"));
            boolean isJobWorkOutOrder = Boolean.parseBoolean(paramJobj.optString("isJobWorkOutOrder"));
            JSONArray jArr = new JSONArray(paramJobj.optString(Constants.detail,"[{}]"));
            Set<String> productNameRCMNotActivate = new HashSet<String>();
            for (int i = 0; i < jArr.length(); i++) {
                double rowAmount = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> podDataMap = new HashMap<>();

                if (jobj.has("srno")) {
                    podDataMap.put("srno", jobj.getInt("srno"));
                }
                if(purchaseOrder.isGstIncluded()){
                    if(jobj.has("lineleveltermamount")){
                        podDataMap.put("lineleveltermamount", jobj.optDouble("lineleveltermamount",0));
                    }
                }
                podDataMap.put("companyid", companyid);
                podDataMap.put("poid", purchaseOrder.getID());
                podDataMap.put("productid", jobj.optString("productid"));
                if (country != null && Constants.INDIA_COUNTRYID.equals(country.getID()) && purchaseOrder.isGtaapplicable()) {
                    KwlReturnObject prdresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.optString("productid"));
                    Product product = (Product) prdresult.getEntityList().get(0);
                    if (!paramJobj.optBoolean("isUnRegisteredDealer",false) && paramJobj.optBoolean("GTAApplicable",false) && product != null && !product.isRcmApplicable()) {
                        productNameRCMNotActivate.add(product.getName());
                       // throw new AccountingException(messageSource.getMessage("acc.common.rcmforproductnotactivated.cannotsave.purchaseorder", new Object[]{product.getName()}, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
                prodList.add(jobj.getString("productid"));
                if (!StringUtil.isNullOrEmpty(jobj.optString("bomid",null))) {
                    podDataMap.put("bomid", jobj.optString("bomid"));
                }
                    podDataMap.put("supplierpartnumber", StringUtil.DecodeText(jobj.optString("supplierpartnumber")));
                    podDataMap.put("supplierpartnumber", jobj.optString("supplierpartnumber"));

                podDataMap.put("rate", jobj.getDouble("rate"));
                if (jobj.has("rateIncludingGst")) {
                    podDataMap.put("rateIncludingGst", jobj.optDouble("rateIncludingGst", 0));
                }
                if (jobj.has("priceSource")) {
                    podDataMap.put("priceSource", !StringUtil.isNullOrEmpty(jobj.optString("priceSource")) ? StringUtil.DecodeText(jobj.optString("priceSource")) : "");
                }
                if (jobj.has("pricingbandmasterid")) {
                    podDataMap.put("pricingbandmasterid", !StringUtil.isNullOrEmpty(jobj.optString("pricingbandmasterid")) ? StringUtil.DecodeText(jobj.optString("pricingbandmasterid")) : "");
                }
                podDataMap.put("quantity", jobj.optDouble("quantity",0.0));
                if (jobj.has("uomid")) {
                    podDataMap.put("uomid", jobj.optString("uomid"));
                }
                podDataMap.put("balanceqty", jobj.optDouble("quantity",0.0));
                if (jobj.has("baseuomquantity") && jobj.get("baseuomquantity") != null) {
                    podDataMap.put("baseuomquantity", jobj.optDouble("baseuomquantity",0.0));
                    podDataMap.put("baseuomrate", jobj.optDouble("baseuomrate",0.0));
                } else {
                    podDataMap.put("baseuomquantity", jobj.optDouble("quantity",0.0));
                    podDataMap.put("baseuomrate", jobj.optDouble("baseuomrate",1.0));
                }
                podDataMap.put("remark", jobj.optString("remark"));
                if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) { // This is sats specific code  
                    if (jobj.has("dependentType")) {
                        podDataMap.put("dependentType", StringUtil.isNullOrEmpty(jobj.optString("dependentType")) ? jobj.optString("dependentTypeNo") : jobj.optString("dependentType"));
                    }
                    if (jobj.has("inouttime")) {
                        podDataMap.put("inouttime", !StringUtil.isNullOrEmpty(jobj.optString("inouttime")) ? jobj.optString("inouttime") : "");
                    }
                    if (jobj.has("showquantity")) {
                        podDataMap.put("showquantity", !StringUtil.isNullOrEmpty(jobj.optString("showquantity")) ? jobj.optString("showquantity") : "");
                    }
                }
                if (jobj.has("shelfLocation")) {
                    String shelfLocation = jobj.optString("shelfLocation");
                    if (!StringUtil.isNullOrEmpty("shelfLocation")) {
                       // try {
                            podDataMap.put("shelfLocation", StringUtil.DecodeText(shelfLocation));
                       // } catch (UnsupportedEncodingException ex) {
                            podDataMap.put("shelfLocation", shelfLocation);
                        //}
                    }
                }
                if (jobj.has("permit")) {
                    podDataMap.put("permit", !StringUtil.isNullOrEmpty(jobj.optString("permit")) ? StringUtil.DecodeText(jobj.optString("permit")) : null);
                }
                String rowtaxid = "";
                if (!StringUtil.isNullOrEmpty(jobj.optString("prtaxid", null)) && jobj.optString("prtaxid").equalsIgnoreCase("None")) {
                    rowtaxid = null;    
                } else {
                    rowtaxid = jobj.optString("prtaxid",null);
                }
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.optDouble("rate",1.0), currencyid, df.parse(paramJobj.optString("billdate")), externalCurrencyRate);

                rowAmount = (Double) bAmt.getEntityList().get(0) * jobj.optDouble("quantity",0.0);
                rowAmount = authHandler.round(rowAmount, companyid);

                podDataMap.put("desc", StringUtil.DecodeText(jobj.optString("desc")));

                if (jobj.has("prdiscount") && jobj.get("prdiscount") != null) {
                    podDataMap.put("discount", jobj.getDouble("prdiscount"));
                }
                if (jobj.has("discountispercent") && jobj.get("discountispercent") != null) {
                    podDataMap.put("discountispercent", jobj.getInt("discountispercent"));
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore"))) {
                    podDataMap.put("invstoreid", jobj.optString("invstore"));
                } else {
                    podDataMap.put("invstoreid", "");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation"))) {
                    podDataMap.put("invlocationid", jobj.optString("invlocation"));
                } else {
                    podDataMap.put("invlocationid", "");
                }
                String linkMode = paramJobj.optString("fromLinkCombo", "");
                String linkNumber = paramJobj.optString("linkNumber", null);
                String linkto = jobj.optString("linkto");
                if (isEdit) {
                    /*
                     * If we linking document (that was already linked with
                     * another document) in Edit mode i.e linking VQ->PO(VQ
                     * already linked with PR or RFQ) then linkto is setting
                     * same as while creating document because it is same as
                     * while creating new document by linking
                     *
                     */
                    if ((!StringUtil.isNullOrEmpty(jobj.optString("savedrowid",null)))) {
                        if (linkMode.equalsIgnoreCase("Sales Order")) {
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), jobj.optString("savedrowid"));
                            SalesOrderDetail sodetails = (SalesOrderDetail) rdresult.getEntityList().get(0);
                            if (sodetails == null || StringUtil.isNullObject(sodetails)) {
                                linkto = "";
                            }
                        } else if (linkMode.equalsIgnoreCase("Vendor Quotation") || linkMode.equalsIgnoreCase("Asset Vendor Quotation")) {
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(VendorQuotationDetail.class.getName(), jobj.optString("savedrowid"));
                            VendorQuotationDetail vqdetails = (VendorQuotationDetail) rdresult.getEntityList().get(0);
                            if (vqdetails == null || StringUtil.isNullObject(vqdetails)) {
                                linkto = "";
                            }
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(linkMode) && !StringUtil.isNullOrEmpty(linkNumber) && (!StringUtil.isNullOrEmpty(jobj.optString("rowid",null)))) {
                    if (linkMode.equalsIgnoreCase("Sales Order") && !StringUtil.isNullOrEmpty(jobj.optString("rowid"))) {
                        podDataMap.put("SalesOrderDetailID", (StringUtil.isNullOrEmpty(linkto)) ? jobj.optString("rowid") : jobj.optString("savedrowid"));
                    } else if (linkMode.equalsIgnoreCase("Vendor Quotation") || linkMode.equalsIgnoreCase("Asset Vendor Quotation")) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(VendorQuotationDetail.class.getName(), (StringUtil.isNullOrEmpty(linkto)) ? jobj.optString("rowid") : jobj.optString("savedrowid"));
                        VendorQuotationDetail row = (VendorQuotationDetail) rdresult.getEntityList().get(0);
                        podDataMap.put("VQDetail", row != null ? row.getID() : "");
                        isPOFromVQ = true;
                    } else if (linkMode.equalsIgnoreCase("Purchase Requisition")) {
                        podDataMap.put("PurchaseRequisitionDetailID", (!StringUtil.isNullOrEmpty(jobj.optString("rowid"))) ? jobj.getString("rowid") : jobj.optString("savedrowid"));
                    } else if (linkMode.equalsIgnoreCase("Work Order")) {
                        podDataMap.put("workorderdetailid", (!StringUtil.isNullOrEmpty(jobj.optString("rowid"))) ? jobj.optString("rowid") : jobj.optString("savedrowid"));
                    }
                }
                if (!StringUtil.isNullOrEmpty(rowtaxid) && includeProductTax) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null) {
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        podDataMap.put("rowtaxid", rowtaxid);
                        double rowtaxamount = StringUtil.getDouble(jobj.getString("taxamount"));
                        podDataMap.put("rowTaxAmount", rowtaxamount);
                        bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, rowtaxamount, currencyid, df.parse(paramJobj.getString("billdate")), externalCurrencyRate);
                        rowtaxamount = (Double) bAmt.getEntityList().get(0);

                        rowAmount = rowAmount + rowtaxamount;
                        podDataMap.put(Constants.isUserModifiedTaxAmount, jobj.optBoolean(Constants.isUserModifiedTaxAmount, false));//ERM-1085
                    }

                }
                if (jobj.has("recTermAmount")) {
                    podDataMap.put("recTermAmount", jobj.optDouble("recTermAmount", 0));
                }
                if (jobj.has("OtherTermNonTaxableAmount")) {
                    podDataMap.put("OtherTermNonTaxableAmount", jobj.optDouble("OtherTermNonTaxableAmount", 0));
                }

                // Check QA approval flow 
                if (preferences.isQaApprovalFlow()) {

                    KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.optString("productid"));
                    Product product = (Product) returnObject.getEntityList().get(0);

                    // Check product is QA Enabled
                    if (product.isQaenable()) {
                        podDataMap.put("qastatus", Constants.Pending_QA_Approval);
                    } else {
                        podDataMap.put("qastatus", Constants.APPROVED);
                    }

                } else {
                    podDataMap.put("qastatus", Constants.APPROVED);
                }
                KwlReturnObject result = accPurchaseOrderobj.savePurchaseOrderDetails(podDataMap);
                PurchaseOrderDetail row = (PurchaseOrderDetail) result.getEntityList().get(0);

                // Save line-level CUSTOMFIELDS for Purchase Order
                String customfield = jobj.optString("customfield",null);
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> POMap = new HashMap<>();
                    JSONArray jcustomarray = new JSONArray(customfield);

                    HashMap<String, Object> customrequestParams = new HashMap<>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "PurchaseorderDetail");
                    customrequestParams.put("moduleprimarykey", "PoDetailID");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", isJobWorkOutOrder ? Constants.JOB_WORK_OUT_ORDER_MODULEID : isConsignment ? Constants.Acc_ConsignmentVendorRequest_ModuleId
                            : (!isFixedAsset ? Constants.Acc_Purchase_Order_ModuleId : Constants.Acc_FixedAssets_Purchase_Order_ModuleId));
                    customrequestParams.put("companyid", companyid);
                    POMap.put("id", row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_PurchaseOrderDetails_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        POMap.put("purchaseordercustomdataref", row.getID());
                        accPurchaseOrderobj.savePurchaseOrderDetails(POMap);
                    }
                }

                // Add Custom fields details for Product
                if (!StringUtil.isNullOrEmpty(jobj.optString("productcustomfield", ""))) {
                    JSONArray jcustomarray = new JSONArray(jobj.optString("productcustomfield", "[]"));
                    HashMap<String, Object> customrequestParams = new HashMap<>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "PurchaseorderDetail");
                    customrequestParams.put("moduleprimarykey", "PoDetailID");
                    customrequestParams.put("modulerecid", row.getID());                    
                    customrequestParams.put("moduleid",isJobWorkOutOrder ? Constants.JOB_WORK_OUT_ORDER_MODULEID : !isFixedAsset ? Constants.Acc_Purchase_Order_ModuleId : Constants.Acc_FixedAssets_Purchase_Order_ModuleId); // Check
                    customrequestParams.put("recdetailId", row.getID());
                    customrequestParams.put("productId", row.getProduct().getID());
                    customrequestParams.put("companyid", companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_PODETAIL_Productcustom_data_classpath);
                    /*
                     * Rich Text Area is put in json if User have not selected any data for this field. ERP-ERP-37624
                     */
                    customrequestParams.put("productIdForRichRext", row.getProduct().getID());                    
                    fieldDataManagercntrl.setRichTextAreaForProduct(customrequestParams);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                }
                    if (extraCompanyPreferences.getLineLevelTermFlag()==1) {
                    /**
                     * Save GST History Customer/Vendor data.
                     */
                    jobj.put("detaildocid", row.getID());
                    jobj.put("moduleid", isFixedAsset?Constants.Acc_FixedAssets_Purchase_Order_ModuleId:Constants.Acc_Purchase_Order_ModuleId);
                    fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jobj);
                }
                /*
                 *  isJobWorkOutOrder ? Constants.JOB_WORK_OUT_ORDER_MODULEID
                 *  JOB_WORK_OUT_ORDER_MODULEID is used if call is for Job Work Out Order
                 */
                //  Indian Details Valuation Type -- start                
                if (country.getID() != null && Integer.parseInt(country.getID()) == Constants.indian_country_id) {
                    if (jobj.has("productMRP") && !StringUtil.isNullOrEmpty(jobj.optString("productMRP"))) {
                        row.setMrpIndia(jobj.getDouble("productMRP"));
                    }
                    if (jobj.has("valuationType") && !StringUtil.isNullOrEmpty(jobj.optString("valuationType"))) { // Excise Details
                        row.setExciseValuationType(jobj.getString("valuationType"));
                        if ((Constants.QUENTITY).equals(jobj.getString("valuationType"))) {
                            if (jobj.has("reortingUOMExcise") && !StringUtil.isNullOrEmpty(jobj.optString("reortingUOMExcise"))) {
                                UnitOfMeasure reportingUom = null;
                                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), jobj.optString("reortingUOMExcise"));
                                reportingUom = (UnitOfMeasure) custresult.getEntityList().get(0);
                                row.setReportingUOMExcise(reportingUom);
                            }
                            if (jobj.has("reortingUOMSchemaExcise") && !StringUtil.isNullOrEmpty(jobj.optString("reortingUOMSchemaExcise"))) {
                                UOMschemaType reportingUom = null;
                                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UOMschemaType.class.getName(), jobj.optString("reortingUOMSchemaExcise"));
                                reportingUom = (UOMschemaType) custresult.getEntityList().get(0);
                                row.setReportingSchemaTypeExcise(reportingUom);
                            }
                        }
                    }
                    if (jobj.has("valuationTypeVAT") && !StringUtil.isNullOrEmpty(jobj.optString("valuationTypeVAT"))) { // VAT Details
                        row.setVatValuationType(jobj.optString("valuationTypeVAT"));
                        if ((Constants.QUENTITY).equals(jobj.optString("valuationTypeVAT"))) {
                            if (jobj.has("reportingUOMVAT") && !StringUtil.isNullOrEmpty(jobj.optString("reportingUOMVAT"))) {
                                UnitOfMeasure reportingUom = null;
                                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), jobj.optString("reportingUOMVAT"));
                                reportingUom = (UnitOfMeasure) custresult.getEntityList().get(0);
                                row.setReportingUOMVAT(reportingUom);
                            }
                            if (jobj.has("reportingUOMSchemaVAT") && !StringUtil.isNullOrEmpty(jobj.optString("reportingUOMSchemaVAT"))) {
                                UOMschemaType reportingUom = null;
                                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UOMschemaType.class.getName(), jobj.optString("reportingUOMSchemaVAT"));
                                reportingUom = (UOMschemaType) custresult.getEntityList().get(0);
                                row.setReportingSchemaVAT(reportingUom);
                            }
                        }
                    }
                }
                // Indian Details Valuation Type -- End 

                rows.add(row);
                totalAmount += rowAmount;

                if (isFixedAsset) {
                    if (jobj.has("assetDetails") && jobj.optString("assetDetails") != null) {
                        String assetDetails = jobj.optString("assetDetails");
                        if (!StringUtil.isNullOrEmpty(assetDetails)) {
                            Set<PurchaseRequisitionAssetDetails> assetDetailsSet = savePurchaseRequisitionAssetDetails(paramJobj, jobj.optString("productid"), assetDetails, false, false, isPOFromVQ);
                            Set<AssetPurchaseRequisitionDetailMapping> assetInvoiceDetailMappings = saveAssetPurchaseRequisitionDetailMapping(row.getID(), assetDetailsSet, companyid, Constants.Acc_FixedAssets_Purchase_Order_ModuleId);
                        }
                    }
                }
                if (extraCompanyPreferences.getLineLevelTermFlag()==1 && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                    JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                    for (int j = 0; j < termsArray.length(); j++) {
                        HashMap<String, Object> purchaseOrderDetailsTermsMap = new HashMap<>();
                        JSONObject termObject = termsArray.getJSONObject(j);
                        if (termObject.has("termid")) {
                            purchaseOrderDetailsTermsMap.put("term", termObject.get("termid"));
                        }
                        if (termObject.has("termamount")) {
                            purchaseOrderDetailsTermsMap.put("termamount", termObject.get("termamount"));
                        }
                        if (termObject.has("termpercentage")) {
                            purchaseOrderDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                        }
                        if (termObject.has("purchasevalueorsalevalue")) {
                            purchaseOrderDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                        }
                        if (termObject.has("deductionorabatementpercent")) {
                            purchaseOrderDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                        }
                        if (termObject.has("assessablevalue")) {
                            purchaseOrderDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                        }
                        if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.optString("taxtype"))) {
                            purchaseOrderDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                            if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.optString("taxvalue"))) {
                                if (termObject.getInt("taxtype") == 0) { // If Flat
                                    purchaseOrderDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                } else { // Else Percentage
                                    purchaseOrderDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                }
                            }
                        }
                        if (termObject.has("id") && !isCopy) {
                            purchaseOrderDetailsTermsMap.put("id", termObject.get("id"));
                        }
                        purchaseOrderDetailsTermsMap.put("podetails", row.getID());
                        /**
                         * ERP-32829 
                         */
                        purchaseOrderDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                        purchaseOrderDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                        purchaseOrderDetailsTermsMap.put("userid", userid);
                        purchaseOrderDetailsTermsMap.put("product", termObject.get("productid"));
                        purchaseOrderDetailsTermsMap.put("createdOn", new Date());

                        accPurchaseOrderobj.savePurchaseOrderDetailsTermMap(purchaseOrderDetailsTermsMap);
                    }
                }
            }
            if (country != null && Constants.INDIA_COUNTRYID.equals(country.getID()) && purchaseOrder.isGtaapplicable()) {
                if (!paramJobj.optBoolean("isUnRegisteredDealer",false) && paramJobj.optBoolean("GTAApplicable",false) && !productNameRCMNotActivate.isEmpty()) {
                    throw new AccountingException(messageSource.getMessage("acc.common.rcmforproductnotactivated.cannotsave.purchaseorder", new Object[]{StringUtils.join(productNameRCMNotActivate, ", ")}, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
            }
            ll.add(rows);
            ll.add(totalAmount);
            ll.add(prodList);
        } catch (ParseException | SessionExpiredException | JSONException ex) {
            throw ServiceException.FAILURE("savePurchaseOrderRows : " + ex.getMessage(), ex);
        }
        return ll;
    }
    private List saveSecurityGateEntryRows(JSONObject paramJobj, String currencyid, SecurityGateEntry securityGateEntry, Double externalCurrencyRate, String companyid, HashMap<String, Object> GlobalParams) throws ServiceException, AccountingException, UnsupportedEncodingException {
        HashSet rows = new HashSet();
        List ll = new ArrayList();
        ArrayList<String> prodList = new ArrayList<>();
        try {
            double totalAmount = 0.0;
            String linkNumber="";
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            KwlReturnObject ecpresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) ecpresult.getEntityList().get(0);
            Country country = preferences.getCompany().getCountry();
            String userid = paramJobj.getString(Constants.useridKey);
            DateFormat df = authHandler.getDateOnlyFormat();
            boolean includeProductTax = StringUtil.isNullOrEmpty(paramJobj.optString("includeprotax", null)) ? false : Boolean.parseBoolean(paramJobj.getString("includeprotax"));
            boolean isEdit = Boolean.parseBoolean(paramJobj.getString("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv", null)) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            JSONArray jArr = new JSONArray(paramJobj.getString(Constants.detail));

            for (int i = 0; i < jArr.length(); i++) {
                double rowAmount = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> podDataMap = new HashMap<>();

                if (jobj.has("srno")) {
                    podDataMap.put("srno", jobj.getInt("srno"));
                }
                if (securityGateEntry.isGstIncluded()) {
                    if (jobj.has("lineleveltermamount")) {
                        podDataMap.put("lineleveltermamount", jobj.optDouble("lineleveltermamount", 0));
                    }
                }
                podDataMap.put("companyid", companyid);
                podDataMap.put("poid", securityGateEntry.getID());
                podDataMap.put("productid", jobj.getString("productid"));

                prodList.add(jobj.getString("productid"));

                try {
                    podDataMap.put("supplierpartnumber", URLDecoder.decode(jobj.optString("supplierpartnumber"), "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    podDataMap.put("supplierpartnumber", jobj.optString("supplierpartnumber"));
                }

                podDataMap.put("rate", jobj.getDouble("rate"));
                if (jobj.has("rateIncludingGst")) {
                    podDataMap.put("rateIncludingGst", jobj.optDouble("rateIncludingGst", 0));
                }
                if (jobj.has("priceSource")) {
                    podDataMap.put("priceSource", !StringUtil.isNullOrEmpty(jobj.optString("priceSource")) ? URLDecoder.decode(jobj.getString("priceSource")) : "");
                }
                podDataMap.put("quantity", jobj.getDouble("quantity"));
                if (jobj.has("uomid")) {
                    podDataMap.put("uomid", jobj.getString("uomid"));
                }
                podDataMap.put("balanceqty", jobj.getDouble("quantity"));
                if (jobj.has("baseuomquantity") && jobj.get("baseuomquantity") != null) {
                    podDataMap.put("baseuomquantity", jobj.getDouble("baseuomquantity"));
                    podDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                } else {
                    podDataMap.put("baseuomquantity", jobj.getDouble("quantity"));
                    podDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                }
                podDataMap.put("remark", jobj.optString("remark"));
                if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) { // This is sats specific code  
                    if (jobj.has("showquantity")) {
                        podDataMap.put("showquantity", !StringUtil.isNullOrEmpty(jobj.getString("showquantity")) ? jobj.getString("showquantity") : "");
                    }
                }
                if (jobj.has("permit")) {
                    podDataMap.put("permit", !StringUtil.isNullOrEmpty(jobj.optString("permit")) ? URLDecoder.decode(jobj.getString("permit"), Constants.DECODE_ENCODE_FORMAT) : null);
                }
                String rowtaxid = "";
                if (!StringUtil.isNullOrEmpty(jobj.optString("prtaxid", null)) && jobj.optString("prtaxid", null).equalsIgnoreCase("None")) {
                    rowtaxid = null;
                } else {
                    rowtaxid = jobj.optString("prtaxid", null);
                }
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("rate"), currencyid, df.parse(paramJobj.getString("billdate")), externalCurrencyRate);

                rowAmount = (Double) bAmt.getEntityList().get(0) * jobj.getDouble("quantity");
                rowAmount = authHandler.round(rowAmount, companyid);

                try {
                    podDataMap.put("desc", URLDecoder.decode(jobj.optString("desc"), "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    podDataMap.put("desc", jobj.optString("desc"));
                }

                if (jobj.has("prdiscount") && jobj.get("prdiscount") != null) {
                    podDataMap.put("discount", jobj.getDouble("prdiscount"));
                }
                if (jobj.has("discountispercent") && jobj.get("discountispercent") != null) {
                    podDataMap.put("discountispercent", jobj.getInt("discountispercent"));
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore"))) {
                    podDataMap.put("invstoreid", jobj.optString("invstore"));
                } else {
                    podDataMap.put("invstoreid", "");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation"))) {
                    podDataMap.put("invlocationid", jobj.optString("invlocation"));
                } else {
                    podDataMap.put("invlocationid", "");
                }
                String linkMode = paramJobj.optString("fromLinkCombo", "");
                linkNumber = paramJobj.optString("linkNumber", null);
                String linkto = jobj.getString("linkto");
                if (isEdit) {
                    if ((!StringUtil.isNullOrEmpty(jobj.getString("savedrowid")))) {
                        if (linkMode.equalsIgnoreCase("Purchase Order")) {
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), jobj.getString("savedrowid"));
                            PurchaseOrderDetail podetails = (PurchaseOrderDetail) rdresult.getEntityList().get(0);
                            if (podetails == null || StringUtil.isNullObject(podetails)) {
                                linkto = "";
                            }
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(linkMode) && !StringUtil.isNullOrEmpty(linkNumber) && (!StringUtil.isNullOrEmpty(jobj.getString("rowid")))) {
                    if (linkMode.equalsIgnoreCase("Purchase Order") && !StringUtil.isNullOrEmpty(jobj.getString("rowid"))) {
                        podDataMap.put("PurchaseOrderDetailID", (StringUtil.isNullOrEmpty(linkto)) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                    }
                }
                if (!StringUtil.isNullOrEmpty(rowtaxid) && includeProductTax) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null) {
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        podDataMap.put("rowtaxid", rowtaxid);
                        double rowtaxamount = StringUtil.getDouble(jobj.getString("taxamount"));
                        podDataMap.put("rowTaxAmount", rowtaxamount);
                        bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, rowtaxamount, currencyid, df.parse(paramJobj.getString("billdate")), externalCurrencyRate);
                        rowtaxamount = (Double) bAmt.getEntityList().get(0);

                        rowAmount = rowAmount + rowtaxamount;
                    }

                }
                if (jobj.has("recTermAmount")) {
                    podDataMap.put("recTermAmount", jobj.optDouble("recTermAmount", 0));
                }
                if (jobj.has("OtherTermNonTaxableAmount")) {
                    podDataMap.put("OtherTermNonTaxableAmount", jobj.optDouble("OtherTermNonTaxableAmount", 0));
                }

                if (preferences.isQaApprovalFlow()) {
                    KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString("productid"));
                    Product product = (Product) returnObject.getEntityList().get(0);
                    if (product.isQaenable()) {
                        podDataMap.put("qastatus", Constants.Pending_QA_Approval);
                    } else {
                        podDataMap.put("qastatus", Constants.APPROVED);
                    }

                } else {
                    podDataMap.put("qastatus", Constants.APPROVED);
                }
                KwlReturnObject result = accPurchaseOrderobj.saveSecurityGateEntryDetails(podDataMap);
                SecurityGateDetails row = (SecurityGateDetails) result.getEntityList().get(0);
                
                  // Saving Security Gate Details Custom Data
                String customfield = jobj.optString(Constants.customfield,null);
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> SOMap = new HashMap<String, Object>();
                    JSONArray jcustomarray = new JSONArray(customfield);

                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "SecurityGateDetails");
                    customrequestParams.put("moduleprimarykey", "SgeDetailID");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", Constants.Acc_SecurityGateEntry_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    SOMap.put("id", row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_SecurityGateDetails_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        SOMap.put("securitygateentrycustomdataref", row.getID());
                        accPurchaseOrderobj.saveSecurityGateEntryDetails(SOMap);
                    }
                }
                
                if (extraCompanyPreferences.getLineLevelTermFlag()==1) {
                    /**
                     * Save GST History Customer/Vendor data.
                     */
                    jobj.put("detaildocid", row.getID());
                    jobj.put("moduleid",Constants.Acc_SecurityGateEntry_ModuleId);
                    fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jobj);
                }
                
                if (extraCompanyPreferences.getLineLevelTermFlag()==1 && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                    JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                    for (int j = 0; j < termsArray.length(); j++) {
                        HashMap<String, Object> SGEDetailsTermMap = new HashMap<>();
                        JSONObject termObject = termsArray.getJSONObject(j);
                        if (termObject.has("termid")) {
                            SGEDetailsTermMap.put("term", termObject.get("termid"));
                        }
                        if (termObject.has("termamount")) {
                            SGEDetailsTermMap.put("termamount", termObject.get("termamount"));
                        }
                        if (termObject.has("termpercentage")) {
                            SGEDetailsTermMap.put("termpercentage", termObject.get("termpercentage"));
                        }
                        if (termObject.has("purchasevalueorsalevalue")) {
                            SGEDetailsTermMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                        }
                        if (termObject.has("deductionorabatementpercent")) {
                            SGEDetailsTermMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                        }
                        if (termObject.has("assessablevalue")) {
                            SGEDetailsTermMap.put("assessablevalue", termObject.get("assessablevalue"));
                        }
                        if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.optString("taxtype"))) {
                            SGEDetailsTermMap.put("taxtype", termObject.getInt("taxtype"));
                            if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.optString("taxvalue"))) {
                                if (termObject.getInt("taxtype") == 0) { // If Flat
                                    SGEDetailsTermMap.put("termamount", termObject.getDouble("taxvalue"));
                                } else { // Else Percentage
                                    SGEDetailsTermMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                }
                            }
                        }
                        if (termObject.has("id") && !isCopy) {
                            SGEDetailsTermMap.put("id", termObject.get("id"));
                        }
                        SGEDetailsTermMap.put("sgedetails", row.getID());                       
                        SGEDetailsTermMap.put("isDefault", termObject.optString("isDefault", "false"));
                        SGEDetailsTermMap.put("productentitytermid", termObject.optString("productentitytermid"));
                        SGEDetailsTermMap.put("userid", userid);
                        SGEDetailsTermMap.put("product", termObject.get("productid"));
                        SGEDetailsTermMap.put("createdOn", new Date());

                        accPurchaseOrderobj.saveSGEDetailsTermMap(SGEDetailsTermMap);
                    }
                }
                
                rows.add(row);
                totalAmount += rowAmount;
            }
            accPurchaseOrderobj.updatePoIsUsedForSecurityGateEntry(linkNumber,companyid,true);
            ll.add(rows);
            ll.add(totalAmount);
            ll.add(prodList);
        } catch (ParseException | SessionExpiredException | JSONException ex) {
            throw ServiceException.FAILURE("saveSecurityGateEntryRows : " + ex.getMessage(), ex);
        }
        return ll;
    }

    @Override
    public Set<PurchaseRequisitionAssetDetails> savePurchaseRequisitionAssetDetails(JSONObject paramJobj, String productId, String assetDetails, boolean invrecord, boolean isQuotationFromPR, boolean isPOFromVQ) throws SessionExpiredException, AccountingException, UnsupportedEncodingException {
        Set<PurchaseRequisitionAssetDetails> assetDetailsSet = new HashSet<>();
        try {
            JSONArray jArr = new JSONArray(assetDetails);
            String companyId = paramJobj.getString(Constants.companyKey);
            DateFormat df = authHandler.getDateOnlyFormat();

            // In case of linking no need to check for duplicacy at here, as duplicacy check
            // is implemented on js side.
            // but while creating new asset either by Purchase Invoice or by Goods Receipt it is need to check duplicacy.
            boolean isbeingCreateFromLinking = false;

            if (isQuotationFromPR || isPOFromVQ) {
                isbeingCreateFromLinking = true;
            }

            HashMap<String, Object> assetParams = new HashMap<>();
            assetParams.put("companyId", companyId);
//            KwlReturnObject assetResult = accProductObj.getAssetDetails(assetParams);
//            KwlReturnObject purchaseRequistionAssetResult= accProductObj.getPurchaseRequisitionAssetDetails(assetParams);
//            
//            List assetList = assetResult.getEntityList();
//            List assetPurchaseRequisitionList = purchaseRequistionAssetResult.getEntityList();
//            
//            List<String> assetNameList = new ArrayList<>();
//            
//            Iterator it = assetList.iterator();
//            while (it.hasNext()) {
//                AssetDetails ad = (AssetDetails) it.next();
//                assetNameList.add(ad.getAssetId().toLowerCase());
//            }
//             
//            Iterator it1 = assetPurchaseRequisitionList.iterator();
//            while (it1.hasNext()) {
//                PurchaseRequisitionAssetDetails detail = (PurchaseRequisitionAssetDetails) it1.next();
//                assetNameList.add(detail.getAssetId().toLowerCase());
//            }
            StringBuilder AssetId = new StringBuilder();
            AssetId.append("('");
            for (int i = 0; i < jArr.length(); i++) {           //Created Comma separated string
                JSONObject jobj = jArr.getJSONObject(i);
                String assetId = jobj.optString("assetId");
                AssetId.append(assetId);
                if (i != jArr.length() - 1) {
                    AssetId.append("','");
                }
            }
            AssetId.append("')");
            assetParams.put("assetId", AssetId.toString());
            KwlReturnObject assetResult = accProductObj.getAssetDetailsById(assetParams); // get Asset id 
            List assetIdList = assetResult.getEntityList();
            int count = assetResult.getRecordTotalCount();
            StringBuilder duplicateAssetid= new StringBuilder();
           
            if (!isbeingCreateFromLinking) {                   // In case of linking no need to check for duplicacy at here, as duplicacy check
                if (count > 0) {
                    for (Object value : assetIdList) {
                        duplicateAssetid.append(" " + value);
                        duplicateAssetid.append(",");
                    }
                    duplicateAssetid = duplicateAssetid.deleteCharAt(duplicateAssetid.length() - 1);
                    throw new AccountingException(messageSource.getMessage("acc.fixed.asset.id", null, StringUtil.getLocale(paramJobj.getString(Constants.language))) + "(s) " + "<b>" + duplicateAssetid + "</b>"+ " " + messageSource.getMessage("acc.po.assetalreadygenerated", null, StringUtil.getLocale(paramJobj.getString(Constants.language))));
                }
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                String assetId = StringUtil.DecodeText(jobj.optString("assetId"));
                String assetName = StringUtil.DecodeText(jobj.optString("assetName"));
                String location = jobj.getString("location");
                String department = jobj.getString("department");
                String assetdescription = StringUtil.DecodeText(jobj.optString("assetdescription"));
                String assetUser = jobj.getString("assetUser");

                double costInForeignCurrency = jobj.optDouble("costInForeignCurrency",0);
                double externalcurrencyrate = paramJobj.optDouble("externalcurrencyrate",1)==0.0?1.0:paramJobj.optDouble("externalcurrencyrate",1);
                double cost =  costInForeignCurrency / externalcurrencyrate;
                double salvageRate = jobj.optDouble("salvageRate",0);
                double salvageValue = jobj.optDouble("salvageValue",0);
                double salvageValueInForeignCurrency = jobj.optDouble("salvageValueInForeignCurrency",0);
                double accumulatedDepreciation = jobj.optDouble("accumulatedDepreciation",0);
                double wdv = jobj.has("wdv") ? jobj.optDouble("wdv") : 0;
                double assetLife = jobj.optDouble("assetLife",0);
                double elapsedLife = jobj.optDouble("elapsedLife",0);
                double nominalValue = jobj.optDouble("nominalValue",0);

                String installationDateStr = jobj.getString("installationDate");
                Date installationDate = df.parse(installationDateStr);

                String purchaseDateStr = jobj.getString("purchaseDate");
                Date purchaseDate = df.parse(purchaseDateStr);

                // Check Whether asset of this name exist or not in case of GRO -
//                if (!isbeingCreateFromLinking && assetNameList.contains(assetId.toLowerCase())) { // comparing two assetids in case insensitive manner                    
//                    throw new AccountingException(messageSource.getMessage("acc.fixed.asset.id",null,  StringUtil.getLocale(paramJobj.getString(Constants.language)))+" "+"<b>" + assetId  +" "+"</b>"+messageSource.getMessage("acc.po.assetalreadygenerated", null, StringUtil.getLocale(paramJobj.getString(Constants.language))));
//                }

                HashMap<String, Object> dataMap = new HashMap<>();

                if (isQuotationFromPR || isPOFromVQ) {
                    dataMap.put("assetId", assetName);
                } else {
                    dataMap.put("assetId", assetId);
                }

                dataMap.put("location", location);
                dataMap.put("department", department);
                dataMap.put("assetdescription", assetdescription);
                dataMap.put("assetUser", assetUser);
                dataMap.put("cost", cost);
                dataMap.put("costInForeignCurrency", costInForeignCurrency);
                dataMap.put("salvageRate", salvageRate);
                dataMap.put("salvageValue", salvageValue);
                dataMap.put("salvageValueInForeignCurrency", salvageValueInForeignCurrency);
                dataMap.put("accumulatedDepreciation", accumulatedDepreciation);
                dataMap.put("wdv", wdv);
                dataMap.put("assetLife", assetLife);
                dataMap.put("elapsedLife", elapsedLife);
                dataMap.put("nominalValue", nominalValue);
                dataMap.put("productId", productId);
                dataMap.put("installationDate", installationDate);
                dataMap.put("purchaseDate", purchaseDate);
                dataMap.put("companyId", companyId);
                dataMap.put("invrecord", invrecord);
                dataMap.put("assetSoldFlag", 0);

                KwlReturnObject result = accProductObj.savePurchaseRequisitionAssetDetails(dataMap);
                PurchaseRequisitionAssetDetails assetDetail = (PurchaseRequisitionAssetDetails) result.getEntityList().get(0);

                assetDetailsSet.add(assetDetail);
            }
        } catch ( ServiceException | ParseException | JSONException ex) {
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            try {
                throw new AccountingException(messageSource.getMessage("acc.commom.ErrorWhileProcessingData",null,  StringUtil.getLocale(paramJobj.getString(Constants.language))));
            } catch (JSONException ex1) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return assetDetailsSet;
    }

    @Override
    public Set<AssetPurchaseRequisitionDetailMapping> saveAssetPurchaseRequisitionDetailMapping(String purchaseRequisitionDetailId, Set<PurchaseRequisitionAssetDetails> assetDetailsSet, String companyId, int moduleId) throws AccountingException {
        Set<AssetPurchaseRequisitionDetailMapping> assetInvoiceDetailMappings = new HashSet<>();
        try {
            for (PurchaseRequisitionAssetDetails assetDetails : assetDetailsSet) {
                HashMap<String, Object> dataMap = new HashMap<>();
                dataMap.put("purchaseRequisitionDetail", purchaseRequisitionDetailId);
                dataMap.put("moduleId", moduleId);
                dataMap.put("assetDetails", assetDetails.getId());
                dataMap.put("company", companyId);
                KwlReturnObject object = accProductObj.saveAssetPurchaseRequisitionDetailMapping(dataMap);

                AssetPurchaseRequisitionDetailMapping detailMapping = (AssetPurchaseRequisitionDetailMapping) object.getEntityList().get(0);
                assetInvoiceDetailMappings.add(detailMapping);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new AccountingException("Error while processing data.");
        }
        return assetInvoiceDetailMappings;
    }

    @Override
    public void updateVQisOpenAndLinking(String linkNumbers) throws ServiceException {
        try {
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), linkNumbers);
            VendorQuotation quotation = (VendorQuotation) rdresult.getEntityList().get(0);
            Set<VendorQuotationDetail> rows = quotation.getRows();
            Iterator itrVQD = rows.iterator();
            boolean isopen = false;
            while (itrVQD.hasNext()) {
                VendorQuotationDetail row = (VendorQuotationDetail) itrVQD.next();
                KwlReturnObject idresult = accPurchaseOrderobj.getPODFromVQD(row.getID());
                List list = idresult.getEntityList();
                Iterator itePOD = list.iterator();
                double qua = 0.0;
                while (itePOD.hasNext()) {
                    PurchaseOrderDetail pod = (PurchaseOrderDetail) itePOD.next();
                    qua += pod.getQuantity();
                }
                double addobj = row.getQuantity() - qua;
                if (addobj > 0) {
                    isopen = true;
                    break;
                }
            }
            HashMap hMap = new HashMap();
            hMap.put("isOpen", isopen);
            hMap.put("quotation", quotation);
            hMap.put("value", "2");
            accPurchaseOrderobj.updateVQLinkflag(hMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateVQisOpenAndLinking : " + ex.getMessage(), ex);
        }
    }
    
    public void savePurchaseOrderVersion(JSONObject paramJobj, String poid) throws ServiceException, AccountingException {
        try {
            HashMap<String, Object> qDataMap = new HashMap<String, Object>();
            KwlReturnObject purchaseOrderResponse = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), poid);
            boolean isExpensePO = StringUtil.isNullOrEmpty(paramJobj.optString("isExpenseInv", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isExpenseInv"));
            PurchaseOrder purchaseorder = (PurchaseOrder) purchaseOrderResponse.getEntityList().get(0);
            if (purchaseorder.getSeqformat() != null) {
                qDataMap.put(Constants.SEQFORMAT, purchaseorder.getSeqformat().getID());
            }
            qDataMap.put(Constants.SEQNUMBER, purchaseorder.getSeqnumber());
            qDataMap.put(Constants.DATEPREFIX, purchaseorder.getDatePreffixValue());
            qDataMap.put(Constants.DATESUFFIX, purchaseorder.getDateSuffixValue());
            if (purchaseorder.getBillingShippingAddresses() != null) {
                qDataMap.put("billshipAddressid", purchaseorder.getBillingShippingAddresses().getID());
            }
            qDataMap.put("externalCurrencyRate", purchaseorder.getExternalCurrencyRate());
            qDataMap.put("entrynumber", purchaseorder.getPurchaseOrderNumber());
            qDataMap.put("autogenerated", purchaseorder.isAutoGenerated());
            qDataMap.put("memo", purchaseorder.getMemo());
            qDataMap.put("posttext", purchaseorder.getPostText());
            if (purchaseorder.getVendor() != null) {
                qDataMap.put("vendorid", purchaseorder.getVendor().getID());
            }
            qDataMap.put("orderdate", purchaseorder.getOrderDate());
            qDataMap.put("duedate", purchaseorder.getDueDate());
            qDataMap.put("perDiscount", purchaseorder.isPerDiscount());
            qDataMap.put("discount", purchaseorder.getDiscount());
            qDataMap.put("gstIncluded", purchaseorder.isGstIncluded());
            qDataMap.put("shipdate", purchaseorder.getShipdate());
            qDataMap.put("shipvia", purchaseorder.getShipvia());
            qDataMap.put("fob", purchaseorder.getFob());
            if (purchaseorder.getCurrency() != null) {
                qDataMap.put("currencyid", purchaseorder.getCurrency().getCurrencyID());
            }
            isExpensePO = purchaseorder.isIsExpenseType();
            qDataMap.put("isExpenseType", purchaseorder.isIsExpenseType());
            qDataMap.put("isfavourite", purchaseorder.isFavourite());
            qDataMap.put("term", purchaseorder.getTerm());
            qDataMap.put("shipaddress", purchaseorder.getShipTo());
            qDataMap.put("billto", purchaseorder.getBillTo());
            qDataMap.put("istemplate", purchaseorder.getIstemplate());
            qDataMap.put("agent", purchaseorder.getMasteragent() != null ? purchaseorder.getMasteragent().getID() : null);
            qDataMap.put("companyid", purchaseorder.getCompany().getCompanyID());
            qDataMap.put("createdby", purchaseorder.getCreatedby().getUserID());
            qDataMap.put("modifiedby", purchaseorder.getModifiedby().getUserID());
            qDataMap.put("createdon", purchaseorder.getCreatedon());
            qDataMap.put("updatedon", purchaseorder.getUpdatedon());
            qDataMap.put("shipLength", purchaseorder.getShiplength());
            qDataMap.put("invoicetype", purchaseorder.getInvoicetype());
            qDataMap.put("purchaseOrderID", purchaseorder.getID());
            qDataMap.put("isopen", purchaseorder.isIsOpen());
            qDataMap.put("totalamount", purchaseorder.getTotalamount());
            qDataMap.put("totalamountinbase", purchaseorder.getTotalamountinbase());
            qDataMap.put("isLinkedFromReplacementNumber", purchaseorder.getLinkflag());
            qDataMap.put("taxid", purchaseorder.getTax() == null ? null : purchaseorder.getTax().getID());     // Put taxid as null if the CQ doesnt have any total tax included. (To avoid problem while editing CQ)
            qDataMap.put(Constants.isApplyTaxToTerms, purchaseorder.isApplyTaxToTerms());
            qDataMap.put("costCenterId", purchaseorder.getCostcenter());
            qDataMap.put("shipLength", purchaseorder.getShiplength());
            qDataMap.put("invoicetype", purchaseorder.getInvoicetype());
            qDataMap.put("isConsignment", purchaseorder.isIsconsignment());
            qDataMap.put(Constants.roundingadjustmentamountinbase, purchaseorder.getRoundingadjustmentamountinbase());
            qDataMap.put(Constants.IsRoundingAdjustmentApplied, purchaseorder.isIsRoundingAdjustmentApplied());
            qDataMap.put("totallineleveldiscount", purchaseorder.getTotallineleveldiscount());
            qDataMap.put(Constants.termsincludegst, purchaseorder.getTermsincludegst());
            qDataMap.put(PurchaseOrder.DATEOFSHIPMENT, purchaseorder.getDateofshipment());
            qDataMap.put(PurchaseOrder.EXCISEDUTYCHARGES, purchaseorder.getExcisedutychargees());
            qDataMap.put(PurchaseOrder.JOBWORKLOCATIONID, purchaseorder.getJobworklocation());
            qDataMap.put(PurchaseOrder.SHIPMENTROUTE, purchaseorder.getShipmentroute());
            qDataMap.put(PurchaseOrder.GATEPASS, purchaseorder.getGatepass());
            qDataMap.put(PurchaseOrder.OTHERREMARKS, purchaseorder.getOtherremarks());
            qDataMap.put(PurchaseOrder.PRODUCTID, purchaseorder.getProduct());
            qDataMap.put(PurchaseOrder.WORKORDERID, purchaseorder.getWorkorderid());
            qDataMap.put(PurchaseOrder.PRODUCTQUANTITY, purchaseorder.getProductquantity());
            qDataMap.put("isJobWorkOutOrder", purchaseorder.isIsJobWorkOutOrder());
            qDataMap.put("gstapplicable", purchaseorder.isIsIndGSTApplied());
            qDataMap.put("isLinkedSOBlocked", purchaseorder.isLinkedSOBlocked());
            qDataMap.put("gstIncluded", purchaseorder.isGstIncluded());
            qDataMap.put("supplierInvoiceNo", purchaseorder.getSupplierInvoiceNo());
            String version = "PO00000";
            KwlReturnObject socnt = accPurchaseOrderobj.getPurchaseOrderVersionCount(purchaseorder.getID(), purchaseorder.getCompany().getCompanyID());
            int count = socnt.getRecordTotalCount();
            qDataMap.put("version", version + (count + 1));
            
            /*** Save document level data ***/
            
            
            KwlReturnObject soresult = accPurchaseOrderobj.savePurchaseOrderVersion(qDataMap);
            PurchaseOrderVersion purchaseOrderVersion = (PurchaseOrderVersion) soresult.getEntityList().get(0);
            qDataMap.put("id", purchaseOrderVersion.getID());
            
            
            /*** Save Line Level Details ***/
            
            if (isExpensePO) {
                Map<String, Object> expenseParams = new HashMap<>();
                expenseParams.put("poObject", purchaseOrderVersion);
                expenseParams.put("currencyid", purchaseorder.getCurrency().getCurrencyID());
                expenseParams.put("companyid", purchaseorder.getCompany().getCompanyID());
                expenseParams.put("externalCurrencyRate", purchaseorder.getExternalCurrencyRate());
                expenseParams.put("expensedetail", paramJobj.getString("expensedetail"));
                HashSet qoversiondetails = saveExpensePOVersionRows(paramJobj, purchaseorder, purchaseorder.getCompany().getCompanyID(), purchaseOrderVersion);
                purchaseOrderVersion.setExpenserows(qoversiondetails);
            }else{
                HashSet qoversiondetails = savePurchaseOrderVersionRows(paramJobj, purchaseorder, purchaseorder.getCompany().getCompanyID(), purchaseOrderVersion);
                purchaseOrderVersion.setRows(qoversiondetails);
            }
            
            
            /*** Custom column Version Map ***/
            
            String customfield = paramJobj.optString("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "PurchaseOrderVersion");
                customrequestParams.put("moduleprimarykey", "PoID");
                customrequestParams.put("modulerecid", purchaseOrderVersion.getID());
                customrequestParams.put("moduleid", purchaseorder.isIsJobWorkOutOrder() ? Constants.JOB_WORK_OUT_ORDER_MODULEID : purchaseorder.isIsconsignment() ? Constants.Acc_ConsignmentVendorRequest_ModuleId
                        : (!purchaseorder.isFixedAssetPO() ? Constants.Acc_Purchase_Order_ModuleId : Constants.Acc_FixedAssets_Purchase_Order_ModuleId));
                customrequestParams.put("companyid", purchaseOrderVersion.getCompany().getCompanyID());
                customrequestParams.put("customdataclasspath", Constants.Acc_PurchaseOrderVersion_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    qDataMap.put("purchaseordercustomdataref", purchaseOrderVersion.getID());
                    KwlReturnObject accresult = accPurchaseOrderobj.updatePurchaseOrderVersionCustomData(qDataMap);
                }
            }
            
            
            /***  Document Level Term Map  ***/
            
            KwlReturnObject poTermDetails = accPurchaseOrderobj.getPurchaseOrderTermDetails(purchaseorder.getID());
            if (poTermDetails != null && poTermDetails.getEntityList() != null && !poTermDetails.getEntityList().isEmpty()) {
                List<PurchaseOrderTermMap> potmList = (List<PurchaseOrderTermMap>) poTermDetails.getEntityList();
                for (PurchaseOrderTermMap potm : potmList) {
                    accPurchaseOrderobj.savePurchaseOrderVersionTermMap(potm, purchaseOrderVersion);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashSet savePurchaseOrderVersionRows(JSONObject paramJobj, PurchaseOrder purchaseOrder, String companyid, PurchaseOrderVersion purchaseOrderVersion) throws ServiceException, AccountingException, SessionExpiredException {
        HashSet rows = new HashSet();
        HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("purchaseOrder.ID");
        order_by.add("srno");
        order_type.add("asc");
        soRequestParams.put("filter_names", filter_names);
        soRequestParams.put("filter_params", filter_params);
        soRequestParams.put("order_by", order_by);
        soRequestParams.put("order_type", order_type);
        filter_params.clear();
        filter_params.add(purchaseOrder.getID());
        KwlReturnObject podresult = accPurchaseOrderobj.getPurchaseOrderDetailsForVersion(soRequestParams);
        Iterator itr = podresult.getEntityList().iterator();
        int i = 0;
        while (itr.hasNext()) {
            try {
                PurchaseOrderDetail row = (PurchaseOrderDetail) itr.next();
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put("srno", i + 1);
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("soid", purchaseOrderVersion.getID());
                qdDataMap.put("productid", row.getProduct().getID());
                qdDataMap.put("rate", row.getRate());//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                qdDataMap.put("quantity", row.getQuantity());
                if (row.getUom() != null) {
                    qdDataMap.put("uomid", row.getUom().getID());
                }
                qdDataMap.put("baseuomquantity", row.getBaseuomquantity());
                qdDataMap.put("baseuomrate", row.getBaseuomrate());
                qdDataMap.put("remark", row.getRemark());
                qdDataMap.put("dependentType", row.getDependentType());
                qdDataMap.put("inouttime", row.getInouttime());
                qdDataMap.put("showquantity", row.getShowquantity());
                qdDataMap.put("desc", row.getDescription());
                qdDataMap.put("invstoreid", row.getInvstoreid());
                qdDataMap.put("invlocationid", row.getInvlocid());
                qdDataMap.put("discount", row.getDiscount());
                qdDataMap.put("discountispercent", row.getDiscountispercent());
                qdDataMap.put("rowTaxAmount", row.getRowTaxAmount());
                qdDataMap.put("rowtax", row.getTax());
                qdDataMap.put("rowTermAmount", row.getRowTermAmount());
                qdDataMap.put("OtherTermNonTaxableAmount", row.getOtherTermNonTaxableAmount());
                qdDataMap.put("OtherTermNonTaxableAmount", row.getOtherTermNonTaxableAmount());
                qdDataMap.put("vqdetail", row.getVqdetail());
                qdDataMap.put("purchaseRequisition", row.getPurchaseRequisitionDetailId());
                qdDataMap.put("salesOrderDetailid", row.getSalesorderdetailid());
                qdDataMap.put("balanceqty", row.getBalanceqty());
                qdDataMap.put("rateincludegst", row.getRateincludegst());
                
                
                /***  Save Line Items ***/
                
                KwlReturnObject result = accPurchaseOrderobj.savePurchaseOrderVersionDetails(qdDataMap);
                PurchaseOrderVersionDetails qvd = (PurchaseOrderVersionDetails) result.getEntityList().get(0);

                
                /***  Save Line Level Terms ***/
                
                HashMap<String, Object> PurchaseReturnDetailParams = new HashMap();
                PurchaseReturnDetailParams.put("podetails", row.getID());
                KwlReturnObject poTermDetails = accPurchaseOrderobj.getPurchaseOrderDetailsTermMapForVersion(PurchaseReturnDetailParams);
                if (poTermDetails!=null && poTermDetails.getEntityList() != null && !poTermDetails.getEntityList().isEmpty()) {
                    List<String> podtmList = (List<String>) poTermDetails.getEntityList();
                    for (Object podtmstr : podtmList) {
                        Object[] objects = (Object[]) podtmstr;
                        HashMap<String, Object> povdtmMap = new HashMap<String, Object>();
                        povdtmMap.put("term", (String) objects[1]);
                        povdtmMap.put("product", (String) objects[3]);
                        povdtmMap.put("termAmount", objects[4] != null ? (double) objects[4] : 0.0);
                        povdtmMap.put("percentage", objects[5] != null ? (double) objects[5] : 0.0);
                        povdtmMap.put("povdetails", qvd.getID());
                        povdtmMap.put("creator", (String) objects[7]);
                        povdtmMap.put("createon", objects[8] != null ? objects[8].toString() : null);
                        povdtmMap.put("purchseorsales", objects[9] != null ? (double) objects[9] : 0.0);
                        povdtmMap.put("decutionorAbt", objects[9] != null ? (double) objects[10] : 0.0);
                        povdtmMap.put("taxtype", objects[11] != null ? (int) objects[11] : 1);
                        povdtmMap.put("assessablevalue", objects[12] != null ? (double) objects[12] : 0.0);
                        povdtmMap.put("isgstapplied", objects[13] != null ? ((Character) objects[13] == 'T' ? true : false) : false);
                        povdtmMap.put("entitybasedLinelevelTerm", objects[13] != null ? (String) objects[14] : null);
                        accPurchaseOrderobj.savePurchaseOrderVersionTermDetails(povdtmMap);
                    }
                }
                
                
                 /***  Save Line Level Custom fields ***/
                
                JSONArray jArr = new JSONArray(paramJobj.optString("detail"));
                JSONObject jobj = jArr.optJSONObject(i);
                String customfield = jobj.getString("customfield");
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> DOMap = new HashMap<String, Object>();
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "PurchaseorderversionDetail");
                customrequestParams.put("moduleprimarykey", "PoversionDetailID");
                customrequestParams.put("modulerecid", qvd.getID());
                customrequestParams.put("moduleid", row.getPurchaseOrder().isIsJobWorkOutOrder() ? Constants.JOB_WORK_OUT_ORDER_MODULEID : row.getPurchaseOrder().isIsconsignment() ? Constants.Acc_ConsignmentVendorRequest_ModuleId
                        : (!row.getPurchaseOrder().isFixedAssetPO() ? Constants.Acc_Purchase_Order_ModuleId : Constants.Acc_FixedAssets_Purchase_Order_ModuleId));
                customrequestParams.put("companyid", companyid);
                DOMap.put("id", qvd.getID());
                customrequestParams.put("customdataclasspath", Constants.Acc_PurchaseOrderVersionDetails_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    DOMap.put("podetailscustomdataref", qvd.getID());
                    accPurchaseOrderobj.updatePurchaseOrderVersionDetailsCustomData(DOMap);
                }
                rows.add(qvd);
                i++;
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return rows;
    }
    
    public HashSet saveExpensePOVersionRows(JSONObject paramJobj, PurchaseOrder purchaseOrder, String companyid, PurchaseOrderVersion purchaseOrderVersion) throws JSONException, ServiceException {
        HashSet expensePODetails = new HashSet();
        List ll = new ArrayList();
        KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("purchaseOrder.ID");
        order_by.add("srno");
        order_type.add("asc");
        soRequestParams.put("filter_names", filter_names);
        soRequestParams.put("filter_params", filter_params);
        soRequestParams.put("order_by", order_by);
        soRequestParams.put("order_type", order_type);
        filter_params.clear();
        filter_params.add(purchaseOrder.getID());
        KwlReturnObject podresult = accPurchaseOrderobj.getExpensePODetailsForVersion(soRequestParams);
        Iterator itr = podresult.getEntityList().iterator();
        int i = 0;
        while (itr.hasNext()) {
            ExpensePODetail row = (ExpensePODetail) itr.next();
            HashMap<String, Object> poExpenseDataMap = new HashMap<>();
            poExpenseDataMap.put("srno", i + 1);
            poExpenseDataMap.put("rate", row.getRate());
            poExpenseDataMap.put("isdebit", row.isIsdebit());
            poExpenseDataMap.put("rateincludinggstex", row.getRateIncludingGst());
            poExpenseDataMap.put("calamount", row.getAmount());
            poExpenseDataMap.put("companyid", companyid);
            poExpenseDataMap.put("poid", purchaseOrderVersion.getID());
            poExpenseDataMap.put("desc", row.getDescription());
            poExpenseDataMap.put("accountid", row.getAccount().getID());
            if (row.getTax() != null) {
                poExpenseDataMap.put("taxid", row.getTax().getID());
            }
            poExpenseDataMap.put("rowtaxamount", row.getRowTaxAmount());
            Discount discount = null;
            if (row.getDiscount() != null) {
                Map<String, Object> discMap = new HashMap();
                discMap.put("discount", row.getDiscount().getDiscount());
                discMap.put("inpercent", row.getDiscount().isInPercent());
                discMap.put("originalamount", row.getDiscount().getOriginalAmount());
                discMap.put("companyid", companyid);
                discMap.put("amountinInvCurrency", row.getDiscount().getAmountinInvCurrency());
                KwlReturnObject dscresult = accDiscountobj.updateDiscount(discMap);
                discount = (Discount) dscresult.getEntityList().get(0);
                poExpenseDataMap.put("discountid", discount.getID());
            }
            
            
            /***  Save Line Items ***/
            
            KwlReturnObject result = accPurchaseOrderobj.saveExpensePurchaseOrderVersionDetails(poExpenseDataMap);
            ExpensePOVersionDetails rowExpense = (ExpensePOVersionDetails) result.getEntityList().get(0);
            
            
             /***  Save Line Level Custom Fields ***/
            
            JSONArray jArr = new JSONArray(paramJobj.optString("expensedetail"));
            JSONObject jobj = jArr.optJSONObject(i);
            String customfield = jobj.getString("customfield");
            JSONArray jcustomarray = new JSONArray(customfield);
            HashMap<String, Object> DOMap = new HashMap<String, Object>();
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", "ExpensePOVersionDetail");//this is getter/setter part of pojo class method name  
            customrequestParams.put("moduleprimarykey", "ExpensePOVersionDetailID");
            customrequestParams.put("modulerecid", rowExpense.getID());
            customrequestParams.put("recdetailId", rowExpense.getID());
            customrequestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId); // Check
            customrequestParams.put("companyid", companyid);
            customrequestParams.put("customdataclasspath", Constants.Acc_ExpensePOVersionDetail_custom_data_classpath);
            DOMap.put("id", row.getID());
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                DOMap.put("expensepodetailscustomdataref", rowExpense.getID());
                accPurchaseOrderobj.updateExpensePOVersionDetailsCustomData(DOMap);
            }
            expensePODetails.add(row);
            i++;
        }
        ll.add(expensePODetails);
        return expensePODetails;
    }
    
    /*Updating isopeninpo flag of requisition
    
     while linking with PO based on used quantity of Requisition in PO
    
     If fully used, isopeninpo->false else true
    
     */
    public void updateOpenFlagForPOInRequisition(String linkNumbers) throws ServiceException {
        try {
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), linkNumbers);
            PurchaseRequisition requisition = (PurchaseRequisition) rdresult.getEntityList().get(0);
            Set<PurchaseRequisitionDetail> requisitionRows = requisition.getRows();
            Iterator requisitionDetailItr = requisitionRows.iterator();
            boolean isopen = false;
            HashMap requestParams = new HashMap();

            /*-------- Iterating on Requistion detail------- */
            while (requisitionDetailItr.hasNext()) {
                PurchaseRequisitionDetail row = (PurchaseRequisitionDetail) requisitionDetailItr.next();
                requestParams.put("requisitionDetailId", row.getID());
                requestParams.put("quantityOfRequisitionDetail", row.getQuantity());
                double addobj = calCulateBalanceQtyOfRequisitionForPO(requestParams);
                
                if (addobj > 0) {
                    isopen = true;
                    break;
                }
            }

            /* ---------Updating isopeninpo falg----- */
            requisition.setIsOpenInPO(isopen);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateOpenFlagForPOInRequisition : " + ex.getMessage(), ex);
        }
    }
    
    

    public double calCulateBalanceQtyOfRequisitionForPO(HashMap request) throws ServiceException {
        double returnQty = 0;
        double quantityOfRequisitionDetail = 0;
        String purchaseOrderDetailId="";
         String requisitionDetailId="";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            if (request.containsKey("requisitionDetailId")) {
                requisitionDetailId=request.get("requisitionDetailId").toString();
                requestParams.put("requisitionDetailId", requisitionDetailId);
            }
            if (request.containsKey("purchaseOrderDetailId")) {
                purchaseOrderDetailId=request.get("purchaseOrderDetailId").toString();
                requestParams.put("purchaseOrderDetailId", request.get("purchaseOrderDetailId"));
            }

            if (request.containsKey("quantityOfRequisitionDetail")) {
                quantityOfRequisitionDetail = (Double) request.get("quantityOfRequisitionDetail");
            }

            KwlReturnObject idresult = accPurchaseOrderobj.getPurchaseOrderDetailLinkedWithRequisitionDetail(requestParams);
            List list = idresult.getEntityList();

            Iterator poDetailItr = list.iterator();
            double quantityUsedInpodetail = 0.0;

            /*-------  Calculating POdetail Quantity linked with Requisition Detail --------*/
            while (poDetailItr.hasNext()) {
                PurchaseOrderDetail pod = (PurchaseOrderDetail) poDetailItr.next();
                quantityUsedInpodetail += pod.getQuantity();
            }

            if (quantityOfRequisitionDetail > 0) {//If Called from Requisition rows or at the time of Save PO

                /*---( RequisitionDetail Quantity -  RequisitionDetail Quantity used in PO)---*/
                returnQty = quantityOfRequisitionDetail - quantityUsedInpodetail;
            } else if (!purchaseOrderDetailId.isEmpty() || !requisitionDetailId.isEmpty()) {//If called from Unlink or Delete Case

                returnQty = quantityUsedInpodetail;
            } 

        } catch (Exception ex) {
            throw ServiceException.FAILURE("calCulateBalanceQtyOfRequisitionForPO : " + ex.getMessage(), ex);
        }
        return returnQty;
    }
    

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<String> approvePurchaseOrder(PurchaseOrder poObj, HashMap<String, Object> poApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException {
        boolean hasAuthority = false;
        String companyid = "";
        List returnList = new ArrayList();
        List mailParamList = new ArrayList();
        int returnStatus;
        if (poApproveMap.containsKey("companyid") && poApproveMap.get("companyid") != null) {
            companyid = poApproveMap.get("companyid").toString();
        }
        String currentUser = "";
        if (poApproveMap.containsKey("currentUser") && poApproveMap.get("currentUser") != null) {
            currentUser = poApproveMap.get("currentUser").toString();
        }
        int level = 0;
        if (poApproveMap.containsKey("level") && poApproveMap.get("level") != null) {
            level = Integer.parseInt(poApproveMap.get("level").toString());
        }
        String amount = "";
        if (poApproveMap.containsKey("totalAmount") && poApproveMap.get("totalAmount") != null) {
            amount = poApproveMap.get("totalAmount").toString();
        }
        boolean fromCreate = false;
        if (poApproveMap.containsKey("fromCreate") && poApproveMap.get("fromCreate") != null) {
            fromCreate = Boolean.parseBoolean(poApproveMap.get("fromCreate").toString());
        }
        JSONArray productDiscountMapList = null;
        if (poApproveMap.containsKey("productDiscountMapList") && poApproveMap.get("productDiscountMapList") != null) {
            productDiscountMapList = new JSONArray(poApproveMap.get("productDiscountMapList").toString());
        }
        int moduleid = 0;
        if (poApproveMap.containsKey("moduleid") && poApproveMap.get("moduleid") != null) {
            moduleid = Integer.parseInt(poApproveMap.get("moduleid").toString());
        }
        boolean isEdit = false;
        if (poApproveMap.containsKey("isEdit") && poApproveMap.get("isEdit") != null) {
            isEdit = Boolean.parseBoolean(poApproveMap.get("isEdit").toString());
        }
        if (!fromCreate) {
            String thisUser = currentUser;
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);

            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                /*
                 If "Send approval documents to next level" is disabled from system preferences & pending document is edited then
                 1. When user is authorised then document is always goes at first level
                 2. When user is not authorised then document remains at same level
                 
                 */
                boolean isEditedPendingDocumentWithCheckOff = false;
                if (poApproveMap.containsKey("isEditedPendingDocumentWithCheckOff") && poApproveMap.get("isEditedPendingDocumentWithCheckOff") != null) {
                    level = Integer.parseInt(poApproveMap.get("documentLevel").toString());//Actual level of document for fetching rule at that level for the user
                    poApproveMap.put("level", level);
                    isEditedPendingDocumentWithCheckOff = true;
                }

                hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(poApproveMap);

                /*---If User is authorised at this level then sending document to first level that's why assigning "level=0" ------ */
                if (isEditedPendingDocumentWithCheckOff && hasAuthority) {
                    level = 0;
                }
            }
        } else {
            hasAuthority = true;
        }
        if (hasAuthority) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            int approvalStatus = 11;
            String prNumber = poObj.getPurchaseOrderNumber();
            String cqID = poObj.getID();
            HashMap<String, Object> qdDataMap = new HashMap<>();
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("level", level + 1);
            qdDataMap.put("moduleid", moduleid);
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            Iterator itr = flowresult.getEntityList().iterator();
            String fromName = "User";
            fromName = poObj.getCreatedby().getFirstName().concat(" ").concat(poObj.getCreatedby().getLastName());
            /**
             * parameters required for sending mail
             */
            Map<String, Object> mailParameters = new HashMap();
            mailParameters.put(Constants.companyid, companyid);
            mailParameters.put(Constants.prNumber, prNumber);
            mailParameters.put(Constants.fromName, fromName);
            mailParameters.put(Constants.moduleid, Constants.Acc_Purchase_Order_ModuleId);
            mailParameters.put(Constants.isCash, false);
            mailParameters.put(Constants.isEdit, isEdit);
            mailParameters.put(Constants.createdBy, poObj.getCreatedby().getUserID());
            mailParameters.put("level", poObj.getApprovestatuslevel());
            if (poApproveMap.containsKey(Constants.PAGE_URL)) {
                mailParameters.put(Constants.PAGE_URL, (String) poApproveMap.get(Constants.PAGE_URL));
            }
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                mailParameters.put(Constants.ruleid, row[0].toString());
                String rule = "";
                HashMap<String, Object> recMap = new HashMap();
                if (row[2] != null) {
                    rule = row[2].toString();
                }
                String discountRule = "";
                if (row[7] != null) {
                    discountRule = row[7].toString();
                }
                boolean sendForApproval = false;
                int appliedUpon = Integer.parseInt(row[5].toString());
                if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                    if (productDiscountMapList != null) {
                        sendForApproval = AccountingManager.checkForProductAndProductDiscountRule(productDiscountMapList, appliedUpon, rule, discountRule);
                    }
                }else if(appliedUpon ==Constants.Specific_Products_Category){
                    /*
                     * Check If Rule is apply on product
                     * category from multiapproverule window
                     */
                    sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productDiscountMapList, appliedUpon, rule);
                } else {
                    rule = rule.replaceAll("[$$]+", amount);
                }
                if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon !=Constants.Specific_Products_Category&& Boolean.parseBoolean(engine.eval(rule).toString())) || sendForApproval) {
                    // send emails
                    boolean hasApprover = Boolean.parseBoolean(row[3].toString());
                    mailParameters.put(Constants.hasApprover, hasApprover);
                    if (isMailApplicable) {
                        sendMailToApprover(mailParameters);
                        approvalStatus = level + 1;
                    } else {
                        /*
                         * this block will be executed if any PO will go for
                         * pending approval
                         */
                        approvalStatus = level + 1;
                         recMap.put("ruleid", row[0].toString());
                        recMap.put("fromName", fromName);
                        recMap.put("hasApprover", hasApprover);

                        mailParamList.add(recMap);
                    }
                }
            }
            accPurchaseOrderobj.approvePendingPurchaseOrder(cqID, companyid, approvalStatus);
            returnStatus = approvalStatus;
        } else {
            returnStatus = Constants.NoAuthorityToApprove; // if not have approval permission then return one fix value like 999
        }
        returnList.add(returnStatus);
        returnList.add(mailParamList);

        return returnList;
    }

    @Override
    public HashMap mapExciseDetails(JSONObject temp, JSONObject paramJobj) throws ServiceException { // in common use of Vendor Quotation, Purchase Order
        HashMap<String, Object> exciseMap = new HashMap<>();
        try {
            if (temp.has("purchaseorder")) {
                exciseMap.put("purchaseorder", temp.getString("purchaseorder"));
            }
            if (temp.has("quatationid")) {
                exciseMap.put("quatationid", temp.getString("quatationid"));
            }
            exciseMap.put("id", temp.has("id") ? temp.getString("id") : "");
            exciseMap.put("suppliers", temp.has("suppliers") ? temp.getString("suppliers") : "");
            exciseMap.put("supplierTINSalesTAXNo", temp.has("supplierTINSalesTAXNo") ? temp.getString("supplierTINSalesTAXNo") : "");
            exciseMap.put("supplierExciseRegnNo", temp.has("supplierExciseRegnNo") ? temp.getString("supplierExciseRegnNo") : "");
            exciseMap.put("cstnumber", temp.has("cstnumber") ? temp.getString("cstnumber") : "");
            exciseMap.put("supplierRange", temp.has("supplierRange") ? temp.getString("supplierRange") : "");
            exciseMap.put("supplierCommissionerate", temp.has("supplierCommissionerate") ? temp.getString("supplierCommissionerate") : "");
            exciseMap.put("supplierAddress", temp.has("supplierAddress") ? temp.getString("supplierAddress") : "");
            if (temp.has("supplierState") && !temp.getString("supplierState").equals("")) {
                exciseMap.put("supplierState", temp.has("supplierState") ? temp.getString("supplierState") : "");
            }
            exciseMap.put("supplierImporterExporterCode", temp.has("supplierImporterExporterCode") ? temp.getString("supplierImporterExporterCode") : "");
            exciseMap.put("supplierDivision", temp.has("supplierDivision") ? temp.getString("supplierDivision") : "");
            exciseMap.put("manufacturername", temp.has("manufacturername") ? temp.getString("manufacturername") : "");
            exciseMap.put("manufacturerExciseRegnNo", temp.has("manufacturerExciseRegnNo") ? temp.getString("manufacturerExciseRegnNo") : "");
            exciseMap.put("manufacturerRange", temp.has("manufacturerRange") ? temp.getString("manufacturerRange") : "");
            exciseMap.put("manufacturerCommissionerate", temp.has("manufacturerCommissionerate") ? temp.getString("manufacturerCommissionerate") : "");
            exciseMap.put("manufacturerDivision", temp.has("manufacturerDivision") ? temp.getString("manufacturerDivision") : "");
            exciseMap.put("manufacturerAddress", temp.has("manufacturerAddress") ? temp.getString("manufacturerAddress") : "");
            exciseMap.put("manufacturerImporterExporterCode", temp.has("manufacturerImporterExporterCode") ? temp.getString("manufacturerImporterExporterCode") : "");
            exciseMap.put("InvoicenoManuFacture", temp.has("InvoicenoManuFacture") ? temp.getString("InvoicenoManuFacture") : "");
            if (temp.has("InvoiceDateManuFacture") && temp.get("InvoiceDateManuFacture") != null && !temp.get("InvoiceDateManuFacture").equals("")) {
                exciseMap.put("InvoiceDateManuFacture", authHandler.getDateOnlyFormat().parse(temp.getString("InvoiceDateManuFacture")));
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return exciseMap;
    }

    @Override
    public List mapInvoiceTerms(String InvoiceTerms, String id, String userid, boolean isQuotation) throws ServiceException {
        List ll = new ArrayList();
        try {
            JSONArray termsArr = new JSONArray(InvoiceTerms);
            for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                JSONObject temp = termsArr.getJSONObject(cnt);
                HashMap<String, Object> termMap = new HashMap<>();
                termMap.put("term", temp.getString("id"));
                termMap.put("termamount", Double.parseDouble(temp.getString("termamount")));
                termMap.put("termtaxamount", temp.optDouble("termtaxamount",0));
                termMap.put("termtaxamountinbase", temp.optDouble("termtaxamountinbase",0));
                termMap.put("termtax", temp.optString("termtax",null));
                termMap.put("termAmountExcludingTax", temp.optDouble("termAmountExcludingTax",0));
                termMap.put("termAmountExcludingTaxInBase", temp.optDouble("termAmountExcludingTaxInBase",0));
                termMap.put("termamountinbase", temp.optDouble("termamountinbase",0));
                double percentage = 0;
                if (!StringUtil.isNullOrEmpty(temp.getString("termpercentage"))) {
                    percentage = Double.parseDouble(temp.getString("termpercentage"));
                }
                termMap.put("termpercentage", percentage);
                termMap.put("creationdate", new Date());
                termMap.put("userid", userid);
                if (isQuotation) {
                    termMap.put("vendorQuotationID", id);
                    accPurchaseOrderobj.saveVendorQuotationTermMap(termMap);
                } else {
                    termMap.put("purchaseOrderID", id);
                    accPurchaseOrderobj.savePurchaseOrderTermMap(termMap);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }

    private void sendMailOnPOCreationUpdation(String companyId, PurchaseOrder purchaseOrder, boolean isEdit, String[] toEmailIds, String poNumber) throws ServiceException {
        String htmlTextC = "", subject = "";
        String poCreatorName = purchaseOrder.getCreatedby().getFullName();
        htmlTextC += "<br/>Hi,<br/>";
        if (!isEdit) {
            htmlTextC += "<br/>User <b>" + poCreatorName + "</b> has created new Purchase Order  <b>" + poNumber + "</b>.<br/>";
        } else {
            htmlTextC += "<br/>User <b>" + poCreatorName + "</b> has edited Purchase Order <b>" + poNumber + "</b>.<br/>";
        }

        KwlReturnObject result = accountingHandlerDAOobj.getNotifications(companyId);
        List<NotificationRules> list = result.getEntityList();

        for (NotificationRules nr : list) {
            if (nr != null && nr.getModuleId() == 202) {
                if (Integer.parseInt(nr.getFieldid()) == 30) {
                    subject = nr.getMailsubject();
                    htmlTextC = nr.getMailcontent();

                    subject = subject.replaceAll("#Vendor_Alias#", purchaseOrder.getVendor().getAliasname());
                    subject = subject.replaceAll("#Document_Number#", poNumber);
                    htmlTextC = htmlTextC.replaceAll("#Document_Number#", poNumber);
                    htmlTextC = htmlTextC.replaceAll("#User_Name#", poCreatorName);

                    if (isEdit) {
                        subject = subject.replaceAll("Creation", "updation");
                        subject = subject.replaceAll("generation", "updation");
                        htmlTextC = htmlTextC.replaceAll("added", "updated");
                        htmlTextC = htmlTextC.replaceAll("created", "updated");
                    }
                    break;
                }
            }
        }
        accountingHandlerDAOobj.sendTransactionEmails(toEmailIds, "", subject, htmlTextC, htmlTextC, companyId);
    }

    @Override
    public JSONObject importPurchaseOrderJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        try {
            String doAction = paramJobj.getString("do");

            if (doAction.compareToIgnoreCase("import") == 0) {
                jobj = importPurchaseOrderRecordsForCSV(paramJobj);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                String eParams = paramJobj.getString("extraParams");
                JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);

                HashMap<String, Object> requestParams = importHandler.getImportRequestParams(paramJobj);
                requestParams.put("extraParams", extraParams);
                requestParams.put("extraObj", null);
                requestParams.put("servletContext", paramJobj.get("servletContext"));
                /*
                 While importing opening SO need some parameters that's why below if block is used.
                */
                if (paramJobj.has("isOpeningOrder") && paramJobj.optBoolean("isOpeningOrder")) {
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                    requestParams.put("bookbeginning", preferences.getBookBeginningFrom());
                    requestParams.put("isOpeningOrder", true);
                }

                jobj = importHandler.validateFileData(requestParams);
                jobj.put("success", true);
            }
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return jobj;
    }
    
    /**
     * Description: Validate and Import VQ data
     * @param paramJobj
     * @return JSONObject
     */
    @Override
    public JSONObject importVendorQuotationJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        try {
            String doAction = paramJobj.getString("do");

            if (doAction.compareToIgnoreCase("import") == 0) {
                jobj = importVendorQuotationRecordsForCSV(paramJobj);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                String eParams = paramJobj.getString("extraParams");
                JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);

                HashMap<String, Object> requestParams = importHandler.getImportRequestParams(paramJobj);
                requestParams.put("extraParams", extraParams);
                requestParams.put("extraObj", null);
                requestParams.put("servletContext", paramJobj.get("servletContext"));

                jobj = importHandler.validateFileData(requestParams);
                jobj.put("success", true);
            }
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return jobj;
    }

    public JSONObject importPurchaseOrderRecordsForCSV(JSONObject requestJobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        JSONObject returnObj = new JSONObject();
        String msg = "";
        int total = 0, failed = 0;
        String fileName = requestJobj.getString("filename");
        String companyID = requestJobj.getString(Constants.companyKey);
        String masterPreference = requestJobj.getString("masterPreference");
        boolean issuccess = true;
        boolean isAlreadyExist = false;
        boolean isRecordFailed = false;
        boolean isOpeningDocImport = false;
        FileInputStream fileInputStream = null;
        CsvReader csvReader = null;
        JSONObject paramJobj = new JSONObject();
        JSONArray rows = new JSONArray();
        String prevInvNo = "";
        StringBuffer globalDatakey = new StringBuffer();
        Set<String> globalDatakeySet = new HashSet<>();

        try {
            String dateFormat = null, dateFormatId = requestJobj.getString("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");

            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode = extrareferences.isCurrencyCode();
            
            if (requestJobj.has("isOpeningOrder") && requestJobj.optBoolean("isOpeningOrder")) {
               isOpeningDocImport=true;
            }
            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            File filePath = new File(destinationDirectory + File.separator + fileName);
            fileInputStream = new FileInputStream(filePath);
            String delimiterType = requestJobj.getString("delimiterType");
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);

            JSONObject resjson = new JSONObject(requestJobj.getString("resjson"));
            JSONArray jSONArray = resjson.getJSONArray("root");
            HashMap<String, Integer> columnConfig = new HashMap<>();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            int cnt = 0;
            StringBuilder failedRecords = new StringBuilder();
            HashMap currencyMap = accSalesOrderServiceobj.getCurrencyMap(isCurrencyCode);

            while (csvReader.readRecord()) {
                String failureMsg = "";
                String[] recarr = csvReader.getValues();

                if (cnt == 0) {
                    failedRecords.append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\" \"");
                } else if (cnt == 1) {
                    failedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"Error Message\"");
                } else {
                    try {
                        String currencyID = requestJobj.getString(Constants.globalCurrencyKey);

                        String entryNumber = "";
                        globalDatakey = new StringBuffer();
                        if (columnConfig.containsKey("number")) {
                            entryNumber = recarr[(Integer) columnConfig.get("number")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(entryNumber)) {
                                failureMsg += "Purchase Order Number is not available. ";
                            }
                            globalDatakey.append(entryNumber);
                        } else {
                            failureMsg += "Purchase Order Number column is not found. ";
                        }

                        Date billDate = null,bookbeginningdate = null;
                        if (columnConfig.containsKey("billdate")) {
                            String customerQutationDateStr = recarr[(Integer) columnConfig.get("billdate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(customerQutationDateStr)) {
                                failureMsg += "Purchase Order Date is not available. ";
                            } else {
                                try {
                                    billDate = df.parse(customerQutationDateStr);
                                    globalDatakey.append(customerQutationDateStr);
                                    /* In UI we are not allowing user to give transaction date  on or after book beginning date
                                     below code is for the same purpose */
                                    if (isOpeningDocImport) {
                                        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
                                        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                                        billDate = CompanyPreferencesCMN.removeTimefromDate(billDate);
                                        bookbeginningdate = CompanyPreferencesCMN.removeTimefromDate(preferences.getBookBeginningFrom());
                                        if (billDate.after(bookbeginningdate)) {
                                            failureMsg += messageSource.getMessage("acc.transactiondate.beforebbdate", null, Locale.forLanguageTag(requestJobj.getString(Constants.language)));
                                        }
                                    }
                                    
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Purchase Order Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        } else {
                            failureMsg += "Purchase Order Date column is not found. ";
                        }
                        
                        String supplierInvoiceNo = "";
                        if (columnConfig.containsKey(Constants.SUPPLIERINVOICENO)) {
                            supplierInvoiceNo = recarr[(Integer) columnConfig.get(Constants.SUPPLIERINVOICENO)].replaceAll("\"", "").trim();
                            globalDatakey.append(supplierInvoiceNo);
                        }

                        String costCenterID = "";
                        if (columnConfig.containsKey("costcenter")) {
                            String costCenterName = recarr[(Integer) columnConfig.get("costcenter")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(costCenterName)) {
                                costCenterID = getCostCenterIDByName(costCenterName, companyID);
                                globalDatakey.append(costCenterID);
                                if (StringUtil.isNullOrEmpty(costCenterID)) {
                                    failureMsg += "Cost Center is not found for name " + costCenterName + ". ";
                                }
                            }
                        }

                        Date shipDate = null;
                        if (columnConfig.containsKey("shipdate")) {
                            String shipDateStr = recarr[(Integer) columnConfig.get("shipdate")].replaceAll("\"", "").trim();

                            if (!StringUtil.isNullOrEmpty(shipDateStr)) {
                                try {
                                    shipDate = df.parse(shipDateStr);
                                    globalDatakey.append(shipDateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Ship Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        }

                        String vendorID = "";
                        /*
                         * 1. Vendor Code
                         */
                        if (columnConfig.containsKey("vendorCode")) {
                            String vendorCode = recarr[(Integer) columnConfig.get("vendorCode")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(vendorCode)) {
                                Vendor vendor = getVendorByCode(vendorCode, companyID);
                                if (vendor != null) {
                                    vendorID = vendor.getID();
                                    globalDatakey.append(vendorID);
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.VendorCodeisnotavailable", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + vendorCode + ". ";
                                }
                            }
                        }

                        /*
                         * 2. Vendor Name if customerID is empty it means Vendor
                         * is not found for given code. so need to search data
                         * on name
                         */
                        if (StringUtil.isNullOrEmpty(vendorID)) {
                            if (columnConfig.containsKey("vendor")) {
                                String vendorName = recarr[(Integer) columnConfig.get("vendor")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(vendorName)) {
                                    Vendor vendor = getVendorByName(vendorName, companyID);
                                    if (vendor != null) {
                                        vendorID = vendor.getID();
                                        globalDatakey.append(vendorID);
                                    } else {
                                        failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ". ";
                                    }
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ".";
                                }
                            } else {
                                failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ".";
                            }
                        }

                        String termID = "";
                        if (columnConfig.containsKey("termid")) {
                            String termName = recarr[(Integer) columnConfig.get("termid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(termName)) {
                                termID = accSalesOrderServiceobj.getTermIDByName(termName, companyID);
                                if (StringUtil.isNullOrEmpty(termID)) {
                                    failureMsg += "Debit Term is not found for name " + termName + ". ";
                                } else {
                                    globalDatakey.append(termID);
                                }
                            } else {
                                failureMsg += "Debit Term is not available. ";
                            }
                        } else {
                            failureMsg += "Debit Term column is not found. ";
                        }

                        String memo = "";
                        if (columnConfig.containsKey("memo")) {
                            memo = recarr[(Integer) columnConfig.get("memo")].replaceAll("\"", "").trim();
                            globalDatakey.append(memo);
                        }

                        String shipVia = "";
                        if (columnConfig.containsKey("shipvia")) {
                            shipVia = recarr[(Integer) columnConfig.get("shipvia")].replaceAll("\"", "").trim();
                            globalDatakey.append(shipVia);
                        }

                        String fob = "";
                        if (columnConfig.containsKey("fob")) {
                            fob = recarr[(Integer) columnConfig.get("fob")].replaceAll("\"", "").trim();
                            globalDatakey.append(fob);
                        }

                        String agentID = "";
                        if (columnConfig.containsKey("agent")) {
                            String agentName = recarr[(Integer) columnConfig.get("agent")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(agentName)) {
                                agentID = getAgentIDByName(agentName, companyID);
                                globalDatakey.append(agentID);
                                if (StringUtil.isNullOrEmpty(agentID)) {
                                    failureMsg += "Agent is not found for name " + agentName + ". ";
                                }
                            }
                        }

                        if (isCurrencyCode ? columnConfig.containsKey("currencyCode") : columnConfig.containsKey("currencyid")) {
                            String currencyStr = isCurrencyCode ? recarr[(Integer) columnConfig.get("currencyCode")].replaceAll("\"", "").trim() : recarr[(Integer) columnConfig.get("currencyid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(currencyStr)) {
                                currencyID = accSalesOrderServiceobj.getCurrencyId(currencyStr, currencyMap);
                                globalDatakey.append(currencyID);
                                
                                if (StringUtil.isNullOrEmpty(currencyID)) {
                                    failureMsg += messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ". ";
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Currency is not available. ";
                                }
                            }
                        }
                        
                        String tempString=globalDatakey.toString();
                        boolean added=globalDatakeySet.add(tempString);
                        /*
                        If check all Global level Data is same for each document.
                        */
                        if(!prevInvNo.isEmpty() && prevInvNo.equalsIgnoreCase(entryNumber) && added){
                            failureMsg += "Global level data are not same for document no "+entryNumber ;
                        }

                        Product product = null;
                        if (columnConfig.containsKey("productid")) {
                            String productID = recarr[(Integer) columnConfig.get("productid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productID)) {
                                product = accSalesOrderServiceobj.getProductByProductID(productID, companyID);
                                if (product == null) {
                                    failureMsg += "Product ID is not found for " + productID + ". ";
                                }
                            } else {
                                failureMsg += "Product ID is not available. ";
                            }
                        } else {
                            failureMsg += "Product ID column is not found. ";
                        }

                        double quantity = 0;
                        if (columnConfig.containsKey("quantity")) {
                            String quantityStr = recarr[(Integer) columnConfig.get("quantity")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(quantityStr)) {
                                failureMsg += "Quantity is not available. ";
                            } else {
                                try {
                                    quantity = authHandler.roundQuantity(Double.parseDouble(quantityStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Quantity, Please ensure that value type of Quantity matches with the Quantity. ";
                                }
                            }
                        } else {
                            failureMsg += "Quantity column is not found. ";
                        }

                        double unitPrice = 0;
                        if (columnConfig.containsKey("rate")) {
                            String unitPriceStr = recarr[(Integer) columnConfig.get("rate")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(unitPriceStr)) {
                                failureMsg += "Unit Price is not available. ";
                            } else {
                                try {
                                    unitPrice = authHandler.roundUnitPrice(Double.parseDouble(unitPriceStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Unit Price, Please ensure that value type of Unit Price matches with the Unit Price. ";
                                }
                            }
                        } else {
                            failureMsg += "Unit Price column is not found. ";
                        }

                        UnitOfMeasure uom = null;
                        if (columnConfig.containsKey("uomid")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("uomid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                uom = accSalesOrderServiceobj.getUOMByName(productUOMName, companyID);
                                if (uom != null) {
                                } else {
                                    if (!masterPreference.equalsIgnoreCase("1")) {
                                        failureMsg += "Product Unit Of Measure is not found for " + productUOMName + ". ";
                                    }
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Product Unit Of Measure is not available. ";
                                }
                            }
                        }

                        int discountType = 1;
                        if (columnConfig.containsKey("discountispercent")) {
                            String discountTypeStr = recarr[(Integer) columnConfig.get("discountispercent")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(discountTypeStr)) {
                                if (discountTypeStr.equalsIgnoreCase("Percentage")) {
                                    discountType = 1;
                                } else if (discountTypeStr.equalsIgnoreCase("Flat")) {
                                    discountType = 0;
                                } else {
                                    failureMsg += "Format you entered is not correct. It should be like \"Percentage\" or \"Flat\". ";
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Discount Type is not available. ";
                                }
                            }
                        }

                        double discount = 0;
                        if (columnConfig.containsKey("prdiscount")) {
                            String discountStr = recarr[(Integer) columnConfig.get("prdiscount")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(discountStr)) {
                                failureMsg += "Dicount is not available. ";
                            } else {
                                try {
                                    discount = authHandler.roundQuantity(Double.parseDouble(discountStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Discount, Please ensure that value type of Discount matches with the Discount. ";
                                }
                            }
                        }

                        // creating PO json
                        if (!prevInvNo.equalsIgnoreCase(entryNumber) || entryNumber.equalsIgnoreCase("")) {
                            prevInvNo = entryNumber;
                            if (rows.length() > 0 && !isRecordFailed) {
                                paramJobj.put(Constants.detail, rows.toString());
                                paramJobj.put(Constants.PAGE_URL, requestJobj.optString(Constants.PAGE_URL));
                                // for save PO
                                savePurchaseOrderJSON(paramJobj);
                            }
                            // reset variables
                            paramJobj = new JSONObject();
                            rows = new JSONArray();
                            isRecordFailed = false;
                            isAlreadyExist = false;
                            globalDatakeySet=new  HashSet<>();

                            KwlReturnObject result = accPurchaseOrderobj.getPOCount(entryNumber, companyID);
                            int nocount = result.getRecordTotalCount();
                            if (nocount > 0) {
                                isAlreadyExist = true;
                                throw new AccountingException("Purchase Order number'" + entryNumber + "' already exists.");
                            }

                           

                            // For create custom field array
                            JSONArray customJArr = createGlobalCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Purchase_Order_ModuleId);

                            // For adding due date
                            Date dueDate = getDueDateFromTermAndBillDate(termID, billDate);

                            // For getting exchange rate
                            double exchangeRateForTransaction = getExchangeRateForTransaction(requestJobj, billDate, currencyID);

                            String sequenceFormatID = "NA";
                            boolean autogenerated = false;
                            if (!StringUtil.isNullOrEmpty(entryNumber)) {
                                Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                                sequenceNumberDataMap.put("moduleID", String.valueOf(Constants.Acc_Purchase_Order_ModuleId));
                                sequenceNumberDataMap.put("entryNumber", entryNumber);
                                sequenceNumberDataMap.put("companyID", companyID);
                                List list = importHandler.checksEntryNumberForSequenceNumber(sequenceNumberDataMap);
                                if (!list.isEmpty()) {
                                    boolean isvalidEntryNumber = (Boolean) list.get(0);
                                    if (!isvalidEntryNumber) {
                                        String formatID = (String) list.get(2);
                                        int intSeq = (Integer) list.get(3);
                                        paramJobj.put(Constants.SEQNUMBER, intSeq);
                                        paramJobj.put(Constants.SEQFORMAT, formatID);
                                        autogenerated = true;
                                        sequenceFormatID = formatID;
                                    }
                                }
                            }

                            // param obj for save PO
                            paramJobj.put(Constants.companyKey, companyID);
                            paramJobj.put(Constants.globalCurrencyKey, requestJobj.getString(Constants.globalCurrencyKey));
                            paramJobj.put(Constants.useridKey, requestJobj.getString(Constants.useridKey));
                            paramJobj.put(Constants.userfullname, requestJobj.getString(Constants.userfullname));
                            paramJobj.put(Constants.reqHeader, requestJobj.getString(Constants.reqHeader));
                            paramJobj.put(Constants.remoteIPAddress, requestJobj.getString(Constants.remoteIPAddress));
                            paramJobj.put(Constants.language, requestJobj.getString(Constants.language));
                            paramJobj.put(Constants.currencyKey, currencyID);
                            paramJobj.put("number", entryNumber);
                            paramJobj.put("sequenceformat", sequenceFormatID);
                            paramJobj.put("autogenerated", autogenerated);
                            paramJobj.put("vendor", vendorID);
                            paramJobj.put("defaultAdress", "true");
                            paramJobj.put("memo", memo);
                            paramJobj.put("posttext", "");
                            paramJobj.put("termid", termID);
                            paramJobj.put("billdate", sdf.format(billDate));
                            paramJobj.put("duedate", sdf.format(dueDate));
                            paramJobj.put("perdiscount", "false");
                            paramJobj.put("discount", "0");
                            paramJobj.put("includingGST", "false");
                            if (shipDate != null) {
                                paramJobj.put("shipdate", sdf.format(shipDate));
                            }
                            paramJobj.put("shipvia", shipVia);
                            paramJobj.put("fob", fob);
                            paramJobj.put("isfavourite", "false");
                            paramJobj.put("agent", agentID);
                            paramJobj.put("externalcurrencyrate", String.valueOf(exchangeRateForTransaction));
                            paramJobj.put("istemplate", "0");
                            paramJobj.put("taxamount", "0");
                            paramJobj.put("invoicetermsmap", "[]");
                            paramJobj.put("termsincludegst", "false");
                            paramJobj.put("fromLinkCombo", "");
                            paramJobj.put("linkFrom", "");
                            paramJobj.put("linkNumber", "");
                            paramJobj.put("templatename", "");
                            paramJobj.put("customfield", customJArr.toString());
                            paramJobj.put("isEdit", "false");
                            paramJobj.put("copyInv", "false");
                            paramJobj.put("isDraft", "false");
                            paramJobj.put("includeprotax", "false");
                            paramJobj.put("shipLength", "1");
                            paramJobj.put("taxid", "");
                            paramJobj.put("deletedLinkedDocumentId", "");
                            paramJobj.put("invoicetype", "");
                            paramJobj.put("seqformat_oldflag", "false");
                            /*
                             Put 'isOpeningBalanceOrder' key to identify the Opening Transaction 
                            */
                            paramJobj.put("isOpeningBalanceOrder",isOpeningDocImport);
                            paramJobj.put(Constants.SUPPLIERINVOICENO, supplierInvoiceNo);
                            Map<String, Object> requestParams = new HashMap<>();
                            requestParams.put(Constants.companyKey, companyID);
                            requestParams.put("isOpeningBalanceOrder", isOpeningDocImport);
                            CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, billDate, false);
                            CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, requestJobj, billDate);
                        } // end global details
                        
                         if (!StringUtil.isNullOrEmpty(failureMsg)) {
                            throw new AccountingException(failureMsg);
                        }

                        // For Line level details
                        double conversionFactor = 1;
                        // Add Custom fields details of line items
                        JSONArray lineCustomJArr = createLineLevelCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Purchase_Order_ModuleId);

                        JSONObject detailData = new JSONObject();
                        detailData.put("productid", (product != null) ? product.getID() : "");
                        detailData.put("rate", String.valueOf(unitPrice));
                        detailData.put("priceSource", "");
//                        detailData.put("rateIncludingGst", String.valueOf(unitPrice));
                        detailData.put("quantity", String.valueOf(quantity));
                        detailData.put("uomid", (uom != null) ? uom.getID() : "");
                        detailData.put("baseuomquantity", String.valueOf(quantity * conversionFactor));
                        detailData.put("baseuomrate", String.valueOf(conversionFactor));
                        detailData.put("dependentType", "");
                        detailData.put("inouttime", "");
                        detailData.put("showquantity", "");
                        detailData.put("desc", (product != null) ? product.getDescription() : "");
                        detailData.put("invstore", "");
                        detailData.put("invlocation", "");
                        detailData.put("rowid", "");
                        detailData.put("prdiscount", String.valueOf(discount));
                        detailData.put("discountispercent", String.valueOf(discountType));
                        detailData.put("prtaxid", "");
                        detailData.put("taxamount", "0");
                        detailData.put("linkto", "");
                        detailData.put("savedrowid", "");
                        detailData.put("recTermAmount", "");
                        detailData.put("OtherTermNonTaxableAmount", "");
                        detailData.put("productcustomfield", "[{}]");
                        detailData.put("LineTermdetails", "");
                        detailData.put("productMRP", "");
                        detailData.put("valuationType", "");
                        detailData.put("reortingUOMExcise", "");
                        detailData.put("reortingUOMSchemaExcise", "");
                        detailData.put("valuationTypeVAT", "");
                        detailData.put("reportingUOMVAT", "");
                        detailData.put("reportingUOMSchemaVAT", "");
                        detailData.put("customfield", lineCustomJArr.toString());

                        rows.put(detailData);

                    } catch (Exception ex) {
                        failed++;
                        isRecordFailed = true;
                        String errorMsg = ex.getMessage();
                        if (ex.getMessage() != null) {
                            errorMsg = ex.getMessage();
                        } else if (ex.getCause() != null) {
                            errorMsg = ex.getCause().getMessage();
                        }

                        failedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"").append(errorMsg.replaceAll("\"", "")).append("\"");
                    }
                    total++;
                }
                cnt++;
            }

            // save PO for last record
            if (!isAlreadyExist && !isRecordFailed) {
                paramJobj.put(Constants.detail, rows.toString());
                paramJobj.put(Constants.PAGE_URL, requestJobj.optString(Constants.PAGE_URL));
                savePurchaseOrderJSON(paramJobj);
            }

            if (failed > 0) {
                importHandler.createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
                msg = "Failed to import all the records.";
            } else if (success == total) {
                msg = "All records are imported successfully.";
            } else {
                msg = "Imported " + success + " record" + (success > 1 ? "s" : "") + " successfully";
                msg += (failed == 0 ? "." : " and failed to import " + failed + " record" + (failed > 1 ? "s" : "") + ".");
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();

            // For saving import log
            saveImportLog(requestJobj, msg, total, failed, Constants.Acc_Purchase_Order_ModuleId);

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", ImportLog.getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Purchase_Order_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
    /**
     * Description: Method is used to create record map and save records in DB.
     * @param requestJobj
     * @return
     * @throws AccountingException
     * @throws IOException
     * @throws SessionExpiredException
     * @throws JSONException 
     */
    public JSONObject importVendorQuotationRecordsForCSV(JSONObject requestJobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        JSONObject returnObj = new JSONObject();
        String msg = "";
        int total = 0, failed = 0;
        String fileName = requestJobj.getString("filename");
        String companyID = requestJobj.getString(Constants.companyKey);
        String masterPreference = requestJobj.getString("masterPreference");
        boolean issuccess = true;
        boolean isAlreadyExist = false;
        boolean isRecordFailed = false;
        FileInputStream fileInputStream = null;
        CsvReader csvReader = null;
        JSONObject paramJobj = new JSONObject();
        JSONArray rows = new JSONArray();
        String prevInvNo = "";
        String entryNumber = "";
        try {
            String dateFormat = null, dateFormatId = requestJobj.getString("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");

            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode = extrareferences.isCurrencyCode();

            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            File filePath = new File(destinationDirectory + File.separator + fileName);
            fileInputStream = new FileInputStream(filePath);
            String delimiterType = requestJobj.getString("delimiterType");
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);

            JSONObject resjson = new JSONObject(requestJobj.getString("resjson"));
            JSONArray jSONArray = resjson.getJSONArray("root");
            HashMap<String, Integer> columnConfig = new HashMap<>();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            int cnt = 0;
            StringBuilder failedRecords = new StringBuilder();
            HashMap currencyMap = accSalesOrderServiceobj.getCurrencyMap(isCurrencyCode);

            while (csvReader.readRecord()) {
                String failureMsg = "";
                String[] recarr = csvReader.getValues();

                if (cnt == 0) {
                    failedRecords.append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\" \"");
                } else if (cnt == 1) {
                    failedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"Error Message\"");
                } else {
                    try {
                        String currencyID = requestJobj.getString(Constants.globalCurrencyKey);
                         entryNumber = "";
                        if (columnConfig.containsKey("number")) {
                            entryNumber = recarr[(Integer) columnConfig.get("number")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(entryNumber)) {
                                failureMsg += "Vendor Quotation Number is not available. ";
                            }
                        } else {
                            failureMsg += "Vendor Quotation Number column is not found. ";
                        }

                        Date billDate = null;
                        if (columnConfig.containsKey("billdate")) {
                            String customerQutationDateStr = recarr[(Integer) columnConfig.get("billdate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(customerQutationDateStr)) {
                                failureMsg += "Vendor Quotation Date is not available. ";
                            } else {
                                try {
                                    billDate = df.parse(customerQutationDateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Vendor Quotation Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        } else {
                            failureMsg += "Vendor Quotation Date column is not found. ";
                        }
                        
                        String supplierInvoiceNo = "";
                        if (columnConfig.containsKey(Constants.SUPPLIERINVOICENO)) {
                            supplierInvoiceNo = recarr[(Integer) columnConfig.get(Constants.SUPPLIERINVOICENO)].replaceAll("\"", "").trim();
                        }

                        String costCenterID = "";
                        if (columnConfig.containsKey("costcenter")) {
                            String costCenterName = recarr[(Integer) columnConfig.get("costcenter")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(costCenterName)) {
                                costCenterID = getCostCenterIDByName(costCenterName, companyID);
                                if (StringUtil.isNullOrEmpty(costCenterID)) {
                                    failureMsg += "Cost Center is not found for name " + costCenterName + ". ";
                                }
                            }
                        }

                        Date shipDate = null;
                        if (columnConfig.containsKey("shipdate")) {
                            String shipDateStr = recarr[(Integer) columnConfig.get("shipdate")].replaceAll("\"", "").trim();

                            if (!StringUtil.isNullOrEmpty(shipDateStr)) {
                                try {
                                    shipDate = df.parse(shipDateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Ship Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        }

                        String vendorID = "";
                        /*
                         * 1. Vendor Code
                         */
                        if (columnConfig.containsKey("vendorCode")) {
                            String vendorCode = recarr[(Integer) columnConfig.get("vendorCode")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(vendorCode)) {
                                Vendor vendor = getVendorByCode(vendorCode, companyID);
                                if (vendor != null) {
                                    vendorID = vendor.getID();
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.VendorCodeisnotavailable", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + vendorCode + ". ";
                                }
                            }
                        }

                        /*
                         * 2. Vendor Name if customerID is empty it means Vendor
                         * is not found for given code. so need to search data
                         * on name
                         */
                        if (StringUtil.isNullOrEmpty(vendorID)) {
                            if (columnConfig.containsKey("vendor")) {
                                String vendorName = recarr[(Integer) columnConfig.get("vendor")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(vendorName)) {
                                    Vendor vendor = getVendorByName(vendorName, companyID);
                                    if (vendor != null) {
                                        vendorID = vendor.getID();
                                    } else {
                                        failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ". ";
                                    }
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ".";
                                }
                            } else {
                                failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ".";
                            }
                        }

                        String termID = "";
                        if (columnConfig.containsKey("termid")) {
                            String termName = recarr[(Integer) columnConfig.get("termid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(termName)) {
                                termID = accSalesOrderServiceobj.getTermIDByName(termName, companyID);
                                if (StringUtil.isNullOrEmpty(termID)) {
                                    failureMsg += "Debit Term is not found for name " + termName + ". ";
                                }
                            } else {
                                failureMsg += "Debit Term is not available. ";
                            }
                        } else {
                            failureMsg += "Debit Term column is not found. ";
                        }

                        String memo = "";
                        if (columnConfig.containsKey("memo")) {
                            memo = recarr[(Integer) columnConfig.get("memo")].replaceAll("\"", "").trim();
                        }

                        String shipVia = "";
                        if (columnConfig.containsKey("shipvia")) {
                            shipVia = recarr[(Integer) columnConfig.get("shipvia")].replaceAll("\"", "").trim();
                        }

                        String fob = "";
                        if (columnConfig.containsKey("fob")) {
                            fob = recarr[(Integer) columnConfig.get("fob")].replaceAll("\"", "").trim();
                        }
                        
                        Date validTill = null;
                        if (columnConfig.containsKey("validdate")) {
                            String validTillStr = recarr[(Integer) columnConfig.get("validdate")].replaceAll("\"", "").trim();

                            if (!StringUtil.isNullOrEmpty(validTillStr)) {
                                try {
                                    validTill = df.parse(validTillStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Valid Till, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        }

                        String agentID = "";
                        if (columnConfig.containsKey("agent")) {
                            String agentName = recarr[(Integer) columnConfig.get("agent")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(agentName)) {
                                agentID = getAgentIDByName(agentName, companyID);
                                if (StringUtil.isNullOrEmpty(agentID)) {
                                    failureMsg += "Agent is not found for name " + agentName + ". ";
                                }
                            }
                        }

                        if (isCurrencyCode ? columnConfig.containsKey("currencyCode") : columnConfig.containsKey("currencyid")) {
                            String currencyStr = isCurrencyCode ? recarr[(Integer) columnConfig.get("currencyCode")].replaceAll("\"", "").trim() : recarr[(Integer) columnConfig.get("currencyid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(currencyStr)) {
                                currencyID = accSalesOrderServiceobj.getCurrencyId(currencyStr, currencyMap);

                                if (StringUtil.isNullOrEmpty(currencyID)) {
                                    failureMsg += messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ". ";
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Currency is not available. ";
                                }
                            }
                        }

                        Product product = null;
                        if (columnConfig.containsKey("productid")) {
                            String productID = recarr[(Integer) columnConfig.get("productid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productID)) {
                                product = accSalesOrderServiceobj.getProductByProductID(productID, companyID);
                                if (product == null) {
                                    failureMsg += "Product ID is not found for " + productID + ". ";
                                }
                            } else {
                                failureMsg += "Product ID is not available. ";
                            }
                        } else {
                            failureMsg += "Product ID column is not found. ";
                        }

                        double quantity = 0;
                        if (columnConfig.containsKey("quantity")) {
                            String quantityStr = recarr[(Integer) columnConfig.get("quantity")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(quantityStr)) {
                                failureMsg += "Quantity is not available. ";
                            } else {
                                try {
                                    quantity = authHandler.roundQuantity(Double.parseDouble(quantityStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Quantity, Please ensure that value type of Quantity matches with the Quantity. ";
                                }
                            }
                        } else {
                            failureMsg += "Quantity column is not found. ";
                        }

                        double unitPrice = 0;
                        if (columnConfig.containsKey("rate")) {
                            String unitPriceStr = recarr[(Integer) columnConfig.get("rate")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(unitPriceStr)) {
                                failureMsg += "Unit Price is not available. ";
                            } else {
                                try {
                                    unitPrice = authHandler.roundQuantity(Double.parseDouble(unitPriceStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Unit Price, Please ensure that value type of Unit Price matches with the Unit Price. ";
                                }
                            }
                        } else {
                            failureMsg += "Unit Price column is not found. ";
                        }

                        UnitOfMeasure uom = null;
                        if (columnConfig.containsKey("uomid")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("uomid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                uom = accSalesOrderServiceobj.getUOMByName(productUOMName, companyID);
                                if (uom != null) {
                                } else {
                                    if (!masterPreference.equalsIgnoreCase("1")) {
                                        failureMsg += "Product Unit Of Measure is not found for " + productUOMName + ". ";
                                    }
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Product Unit Of Measure is not available. ";
                                }
                            }
                        }

                        int discountType = 1;
                        if (columnConfig.containsKey("discountispercent")) {
                            String discountTypeStr = recarr[(Integer) columnConfig.get("discountispercent")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(discountTypeStr)) {
                                if (discountTypeStr.equalsIgnoreCase("Percentage")) {
                                    discountType = 1;
                                } else if (discountTypeStr.equalsIgnoreCase("Flat")) {
                                    discountType = 0;
                                } else {
                                    failureMsg += "Format you entered is not correct. It should be like \"Percentage\" or \"Flat\". ";
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Discount Type is not available. ";
                                }
                            }
                        }

                        double discount = 0;
                        if (columnConfig.containsKey("prdiscount")) {
                            String discountStr = recarr[(Integer) columnConfig.get("prdiscount")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(discountStr)) {
                                failureMsg += "Dicount is not available. ";
                            } else {
                                try {
                                    discount = authHandler.roundQuantity(Double.parseDouble(discountStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Discount, Please ensure that value type of Discount matches with the Discount. ";
                                }
                            }
                        }
                        
                        // Creating Vendor Quotation JSON
                        if (!prevInvNo.equalsIgnoreCase(entryNumber) || entryNumber.equalsIgnoreCase("")) {
                            prevInvNo = entryNumber;

                            if (rows.length() > 0 && !isRecordFailed) {
                                paramJobj.put(Constants.detail, rows.toString());
                                paramJobj.put(Constants.PAGE_URL, requestJobj.optString(Constants.PAGE_URL));
                                // for Vendor Quotation 
                                saveVendorQuotationJSON(paramJobj);
                            }
                            // reset variables
                            paramJobj = new JSONObject();
                            rows = new JSONArray();
                            isRecordFailed = false;
                            isAlreadyExist = false;

                            KwlReturnObject result = accPurchaseOrderobj.getQuotationCount(entryNumber, companyID);
                            int nocount = result.getRecordTotalCount();
                            if (nocount > 0) {
                                isAlreadyExist = true;
                                throw new AccountingException("Vendor Quotation number'" + entryNumber + "' already exists.");
                            }

                            // For create custom field array
                            JSONArray customJArr=new JSONArray();
                            try {
                                customJArr = createGlobalCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Vendor_Quotation_ModuleId);
                            } catch (Exception ex) {
                               failureMsg += "Invalid data entered in custom field.Please check date format,numeric value etc.";
                            }

                            // For adding due date
                            Date dueDate=null;
                            if (!StringUtil.isNullOrEmpty(termID) && billDate!= null) {
                                 dueDate = getDueDateFromTermAndBillDate(termID, billDate);
                            }

                            double exchangeRateForTransaction = 1;
                            if (billDate != null) {
                                 // For getting exchange rate
                                exchangeRateForTransaction = getExchangeRateForTransaction(requestJobj, billDate, currencyID);
                                Map<String, Object> requestParams = new HashMap<>();
                               requestParams.put(Constants.companyKey, companyID);
                                CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, billDate, false);
                            }

                            String sequenceFormatID = "NA";
                            boolean autogenerated = false;
                            if (!StringUtil.isNullOrEmpty(entryNumber)) {
                                Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                                sequenceNumberDataMap.put("moduleID", String.valueOf(Constants.Acc_Vendor_Quotation_ModuleId));
                                sequenceNumberDataMap.put("entryNumber", entryNumber);
                                sequenceNumberDataMap.put("companyID", companyID);
                                List list = importHandler.checksEntryNumberForSequenceNumber(sequenceNumberDataMap);
                                if (!list.isEmpty()) {
                                    boolean isvalidEntryNumber = (Boolean) list.get(0);
                                    if (!isvalidEntryNumber) {
                                        String formatID = (String) list.get(2);
                                        int intSeq = (Integer) list.get(3);
                                        paramJobj.put(Constants.SEQNUMBER, intSeq);
                                        paramJobj.put(Constants.SEQFORMAT, formatID);
                                        autogenerated = true;
                                        sequenceFormatID = formatID;
                                    }
                                }
                            }

                            // param obj for save Vendor Quotation
                            paramJobj.put(Constants.companyKey, companyID);
                            paramJobj.put(Constants.globalCurrencyKey, requestJobj.getString(Constants.globalCurrencyKey));
                            paramJobj.put(Constants.useridKey, requestJobj.getString(Constants.useridKey));
                            paramJobj.put(Constants.userfullname, requestJobj.getString(Constants.userfullname));
                            paramJobj.put(Constants.reqHeader, requestJobj.getString(Constants.reqHeader));
                            paramJobj.put(Constants.remoteIPAddress, requestJobj.getString(Constants.remoteIPAddress));
                            paramJobj.put(Constants.language, requestJobj.getString(Constants.language));
                            paramJobj.put(Constants.currencyKey, currencyID);
                            paramJobj.put("number", entryNumber);
                            paramJobj.put("sequenceformat", sequenceFormatID);
                            paramJobj.put("autogenerated", autogenerated);
                            paramJobj.put("vendor", vendorID);
                            paramJobj.put("defaultAdress", "true");
                            paramJobj.put("memo", memo);
                            paramJobj.put("posttext", "");
                            paramJobj.put("termid", termID);
                            paramJobj.put("billdate", (billDate!=null)?sdf.format(billDate):billDate);
                            paramJobj.put("duedate", (dueDate!=null)?sdf.format(dueDate):dueDate);
                            paramJobj.put("perdiscount", "false");
                            paramJobj.put("discount", "0");
                            paramJobj.put("includingGST", "false");
                            if (shipDate != null) {
                                paramJobj.put("shipdate", sdf.format(shipDate));
                            }
                            if (validTill != null) {
                                paramJobj.put("validdate", sdf.format(validTill));
                            }
                            paramJobj.put("shipvia", shipVia);
                            paramJobj.put("fob", fob);
                            paramJobj.put("isfavourite", "false");
                            paramJobj.put("agent", agentID);
                            paramJobj.put("externalcurrencyrate", String.valueOf(exchangeRateForTransaction));
                            paramJobj.put("istemplate", "0");
                            paramJobj.put("taxamount", "0");
                            paramJobj.put("invoicetermsmap", "[]");
                            paramJobj.put("termsincludegst", "false");
                            paramJobj.put("fromLinkCombo", "");
                            paramJobj.put("linkFrom", "");
                            paramJobj.put("linkNumber", "");
                            paramJobj.put("templatename", "");
                            paramJobj.put("customfield", customJArr.toString());
                            paramJobj.put("isEdit", "false");
                            paramJobj.put("copyInv", "false");
                            paramJobj.put("isDraft", "false");
                            paramJobj.put("includeprotax", "false");
                            paramJobj.put("shipLength", "1");
                            paramJobj.put("taxid", "");
                            paramJobj.put("deletedLinkedDocumentId", "");
                            paramJobj.put("invoicetype", "");
                            paramJobj.put("seqformat_oldflag", "false");
                            paramJobj.put(Constants.SUPPLIERINVOICENO, supplierInvoiceNo);
                            
                        } // end global details
                        
                        if (!StringUtil.isNullOrEmpty(failureMsg)) {
                            throw new AccountingException(failureMsg);
                        }

                        // For Line level details
                        double conversionFactor = 1;
                        // Add Custom fields details of line items
                        JSONArray lineCustomJArr = createLineLevelCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Vendor_Quotation_ModuleId);

                        JSONObject detailData = new JSONObject();
                        detailData.put("productid", (product != null) ? product.getID() : "");
                        detailData.put("rate", String.valueOf(unitPrice));
                        detailData.put("priceSource", "");
                        detailData.put("quantity", String.valueOf(quantity));
                        detailData.put("uomid", (uom != null) ? uom.getID() : "");
                        detailData.put("baseuomquantity", String.valueOf(quantity * conversionFactor));
                        detailData.put("baseuomrate", String.valueOf(conversionFactor));
                        detailData.put("dependentType", "");
                        detailData.put("inouttime", "");
                        detailData.put("showquantity", "");
                        detailData.put("desc", (product != null) ? product.getDescription() : "");
                        detailData.put("invstore", "");
                        detailData.put("invlocation", "");
                        detailData.put("rowid", "");
                        detailData.put("prdiscount", String.valueOf(discount));
                        detailData.put("discountispercent", String.valueOf(discountType));
                        detailData.put("prtaxid", "");
                        detailData.put("taxamount", "0");
                        detailData.put("linkto", "");
                        detailData.put("savedrowid", "");
                        detailData.put("recTermAmount", "");
                        detailData.put("OtherTermNonTaxableAmount", "");
                        detailData.put("productcustomfield", "[{}]");
                        detailData.put("LineTermdetails", "");
                        detailData.put("productMRP", "");
                        detailData.put("valuationType", "");
                        detailData.put("reortingUOMExcise", "");
                        detailData.put("reortingUOMSchemaExcise", "");
                        detailData.put("valuationTypeVAT", "");
                        detailData.put("reportingUOMVAT", "");
                        detailData.put("reportingUOMSchemaVAT", "");
                        detailData.put("customfield", lineCustomJArr.toString());

                        rows.put(detailData);

                    } catch (Exception ex) {
                        failed++;
                        isRecordFailed = true;
                        String errorMsg = ex.getMessage();
                        if (ex.getMessage() != null) {
                            errorMsg = ex.getMessage();
                        } else if (ex.getCause() != null) {
                            errorMsg = ex.getCause().getMessage();
                        }

                        failedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"").append(errorMsg.replaceAll("\"", "")).append("\"");
                    }
                    total++;
                }
                cnt++;
            }

            // save Vendor Quotation for last record
            if (!isAlreadyExist && !isRecordFailed) {
                paramJobj.put(Constants.detail, rows.toString());
                paramJobj.put(Constants.PAGE_URL, requestJobj.optString(Constants.PAGE_URL));
                saveVendorQuotationJSON(paramJobj);
            }

            if (failed > 0) {
                importHandler.createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
                msg = "Failed to import all the records.";
            } else if (success == total) {
                msg = "All records are imported successfully.";
            } else {
                msg = "Imported " + success + " record" + (success > 1 ? "s" : "") + " successfully";
                msg += (failed == 0 ? "." : " and failed to import " + failed + " record" + (failed > 1 ? "s" : "") + ".");
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();

            // For saving import log
            saveImportLog(requestJobj, msg, total, failed, Constants.Acc_Vendor_Quotation_ModuleId);

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", ImportLog.getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Vendor_Quotation_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    @Override
    public Vendor getVendorByCode(String vendorCode, String companyID) throws AccountingException {
        Vendor vendor = null;
        try {
            if (!StringUtil.isNullOrEmpty(vendorCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accVendorDAOObj.getVendorByCode(vendorCode, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    vendor = (Vendor) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Vendor");
        }
        return vendor;
    }

    @Override
    public Vendor getVendorByName(String vendorName, String companyID) throws AccountingException {
        Vendor vendor = null;
        try {
            if (!StringUtil.isNullOrEmpty(vendorName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accVendorDAOObj.getVendorByName(vendorName, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    vendor = (Vendor) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Vendor");
        }
        return vendor;
    }

    @Override
    public String getAgentIDByName(String agentName, String companyID) throws AccountingException {
        String agentID = "";
        try {
            if (!StringUtil.isNullOrEmpty(agentName) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> filterRequestParams = new HashMap<>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyID);
                filter_names.add("masterGroup.ID");
                filter_params.add(Constants.AGENT_ID); // For Geting Agent
                filter_names.add("value");
                filter_params.add(agentName);
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);

                KwlReturnObject retObj = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    MasterItem agent = (MasterItem) retObj.getEntityList().get(0);
                    agentID = agent.getID();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Agent.");
        }
        return agentID;
    }
    @Override
    public String getTypeOfSalesIDByName(String typeOfSalesName, String companyID) throws AccountingException {
        String agentID = "";
        try {
            if (!StringUtil.isNullOrEmpty(typeOfSalesName) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> filterRequestParams = new HashMap<>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyID);
                filter_names.add("masterGroup.ID");
                filter_params.add("47"); // For Type Of Sales
                filter_names.add("value");
                filter_params.add(typeOfSalesName);
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);

                KwlReturnObject retObj = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    MasterItem agent = (MasterItem) retObj.getEntityList().get(0);
                    agentID = agent.getID();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Type Of Sales.");
        }
        return agentID;
    }

    @Override
    public String getCostCenterIDByName(String costCenterName, String companyID) throws AccountingException {
        String costCenterID = "";
        try {
            if (!StringUtil.isNullOrEmpty(costCenterName) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> requestParams = new HashMap<>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyID);
                filter_names.add("name");
                filter_params.add(costCenterName);
                requestParams.put(Constants.filterNamesKey, filter_names);
                requestParams.put(Constants.filterParamsKey, filter_params);

                KwlReturnObject retObj = accCostCenterObj.getCostCenter(requestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    CostCenter costCenter = (CostCenter) retObj.getEntityList().get(0);
                    costCenterID = costCenter.getID();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Cost Center.");
        }
        return costCenterID;
    }

    @Override
    public JSONArray createGlobalCustomFieldArrayForImport(JSONObject requestJobj, JSONArray jSONArray, String[] recarr, DateFormat df, int moduleID) throws JSONException, ParseException {
        JSONArray customJArr = new JSONArray();

        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jSONObject = jSONArray.getJSONObject(i);

            if (jSONObject.optBoolean("customflag", false) && !jSONObject.optBoolean("isLineItem", false)) {
                createCustomFieldArrayForImport(requestJobj, recarr, df, jSONObject, customJArr, moduleID);
            }
        }

        return customJArr;
    }

    @Override
    public JSONArray createLineLevelCustomFieldArrayForImport(JSONObject requestJobj, JSONArray jSONArray, String[] recarr, DateFormat df, int moduleID) throws JSONException, ParseException {
        JSONArray customJArr = new JSONArray();

        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jSONObject = jSONArray.getJSONObject(i);

            if (jSONObject.optBoolean("customflag", false) && jSONObject.optBoolean("isLineItem", false)) {
                createCustomFieldArrayForImport(requestJobj, recarr, df, jSONObject, customJArr, moduleID);
            }
        }

        return customJArr;
    }

    public void createCustomFieldArrayForImport(JSONObject requestJobj, String[] recarr, DateFormat df, JSONObject jSONObject, JSONArray customJArr, int moduleID) throws JSONException, ParseException {
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
        requestParams.put(Constants.filter_values, Arrays.asList(requestJobj.getString(Constants.companyKey), moduleID, jSONObject.getString("columnname")));

        KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
        FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);

        if (!StringUtil.isNullOrEmpty(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim())) {
            JSONObject customJObj = new JSONObject();
            customJObj.put("fieldid", params.getId());
            customJObj.put("filedid", params.getId());
            customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
            customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
            customJObj.put("xtype", params.getFieldtype());

            String fieldComboDataStr = "";
            if (params.getFieldtype() == 3) { // if field of date type
                String dateStr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim();
                customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
            } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                    requestParams = new HashMap<>();
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                    requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));


                    fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                    if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                        FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                        fieldComboDataStr += fieldComboData.getId() + ",";
                    }
                }

                if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                    customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                    customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                } else {
                    return;
                }
            } else if (params.getFieldtype() == 11) { // if field of check box type 
                customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
            } else if (params.getFieldtype() == 12) { // if field of check list type
                requestParams = new HashMap<>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));


                fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();

                String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                for (FieldComboData fieldComboData : fieldComboDataList) {
                    for (String fieldComboArrData : fieldComboDataArr) {
                        if (fieldComboArrData != null && fieldComboArrData.replaceAll("\"", "").trim().equalsIgnoreCase(fieldComboData.getValue())) {
                            fieldComboDataStr += fieldComboData.getId() + ",";
                        }
                    }
                }
                
                if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                    customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                    customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                } else {
                    return;
                }
            } else {
                customJObj.put("Col" + params.getColnum(), recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                customJObj.put("fieldDataVal", recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
            }

            customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

            customJArr.put(customJObj);
        }
    }

    @Override
    public double getExchangeRateForTransaction(JSONObject requestJobj, Date billDate, String currencyID) throws JSONException, ServiceException {
        double exchangeRateForTransaction = 1;

        Map<String, Object> currMap = new HashMap<>();
        currMap.put("applydate", billDate);
        currMap.put("gcurrencyid", requestJobj.getString(Constants.globalCurrencyKey));
        currMap.put("companyid", requestJobj.getString(Constants.companyKey));
        KwlReturnObject retObj = accCurrencyDAOobj.getExcDetailID(currMap, currencyID, billDate, null);
        if (retObj != null && !retObj.getEntityList().isEmpty()) {
            List li = retObj.getEntityList();
            Iterator itr = li.iterator();
            ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
            if (erd != null) {
                exchangeRateForTransaction = erd.getExchangeRate();
            }
        }

        return exchangeRateForTransaction;
    }

    public Date getDueDateFromTermAndBillDate(String termID, Date billDate) throws ServiceException {
        Date dueDate;
        KwlReturnObject termObj = accountingHandlerDAOobj.getObject(Term.class.getName(), termID);
        Term term = (Term) termObj.getEntityList().get(0);

        Calendar cal = Calendar.getInstance();
        cal.setTime(billDate);
        cal.add(Calendar.DAY_OF_MONTH, term.getTermdays());
        dueDate = cal.getTime();

        return dueDate;
    }

    @Override
    public void saveImportLog(JSONObject requestJobj, String msg, int total, int failed, int moduleID) {
        DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
        ldef.setName("import_Tx");
        ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus lstatus = txnManager.getTransaction(ldef);

        try {
            HashMap<String, Object> logDataMap = new HashMap<>();
            logDataMap.put("FileName", ImportLog.getActualFileName(requestJobj.getString("filename")));
            logDataMap.put("StorageName", requestJobj.getString("filename"));
            logDataMap.put("Log", msg);
            logDataMap.put("Type", "csv");
            logDataMap.put("FailureFileType", failed > 0 ? "csv" : "");
            logDataMap.put("TotalRecs", total);
            logDataMap.put("Rejected", failed);
            logDataMap.put("Module", moduleID);
            logDataMap.put("ImportDate", new Date());
            logDataMap.put("User", requestJobj.getString(Constants.useridKey));
            logDataMap.put("Company", requestJobj.getString(Constants.companyKey));
            if(requestJobj.has("logId") && !requestJobj.isNull("logId")){
                logDataMap.put("Id", requestJobj.getString("logId"));
            }
            importDao.saveImportLog(logDataMap);
            txnManager.commit(lstatus);
        } catch (JSONException | ServiceException | DataInvalidateException | TransactionException ex) {
            txnManager.rollback(lstatus);
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Description: Save the Vendor Quotation
     * @param paramJobj
     * @return 
     */
    @Override
    public JSONObject saveVendorQuotationJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        String billid = "", moduleName = "";
        String channelName = "", entryNumber = "", companyid = "";
        String butPendingForApproval = "", additionalsauditmessage = "";
        boolean issuccess = false;
        boolean accexception = false;
        boolean isTaxDeactivated = false;
        int approvalStatusLevel = 11;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;//txnManager.getTransaction(def);
        try {
            entryNumber = paramJobj.optString("number");
            companyid = paramJobj.getString(Constants.companyKey);
            int istemplate = paramJobj.optInt("istemplate",0);
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv",null)) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            boolean isLeaseFixedAsset = (StringUtil.isNullOrEmpty(paramJobj.optString("isLeaseFixedAsset",null))) ? false: Boolean.parseBoolean(paramJobj.getString("isLeaseFixedAsset"));
            boolean isFixedAsset = StringUtil.isNullOrEmpty(paramJobj.optString("isFixedAsset",null)) ? false:Boolean.parseBoolean(paramJobj.getString("isFixedAsset"));
            boolean isEditedPendingDocument = StringUtil.isNullOrEmpty(paramJobj.optString("isEditedPendingDocument", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEditedPendingDocument"));
            String currentUser = paramJobj.getString(Constants.useridKey);
            String quotationId = paramJobj.optString("invoiceid");
            String sequenceformat = paramJobj.optString("sequenceformat");
            String fromLinkCombo = paramJobj.optString("fromLinkCombo",null) != null ? paramJobj.getString("fromLinkCombo") : "";
            KwlReturnObject socnt = null;
            if (!StringUtil.isNullOrEmpty(quotationId)) {//In edit case checks duplicate number
                socnt = accPurchaseOrderobj.getQuotationEditCount(entryNumber, companyid, quotationId);
                int count = socnt.getRecordTotalCount();
                if (count > 0 && istemplate != 2 && sequenceformat.equals("NA")) {
                    accexception = true;
                    throw new AccountingException(messageSource.getMessage("acc.QUO.vendorquono", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
            } else {//In add case checks duplicate number
                if (!StringUtil.isNullOrEmpty(entryNumber) && entryNumber!="") {
                    socnt = accPurchaseOrderobj.getQuotationCount(entryNumber, companyid);
                }
                if (socnt != null && socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    accexception = true;
                    throw new AccountingException(messageSource.getMessage("acc.QUO.vendorquono", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
                //Check Deactivate Tax in New Transaction.
                if (!fieldDataManagercntrl.isTaxActivated(paramJobj)) {
                    isTaxDeactivated = true;
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }
            }
            synchronized (this) {//Checks duplicate number for simultaneous transactions
                status = txnManager.getTransaction(def);
                if (sequenceformat.equals("NA")) {
                    KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Vendor_Quotation_ModuleId);
                    if (resultInv.getRecordTotalCount() > 0) {
                        accexception = true;
                        throw new AccountingException(messageSource.getMessage("acc.QUO.selectedvendorquono", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Vendor_Quotation_ModuleId);
                    }
                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            List quotationList = saveQuotation(paramJobj);
            List mailParams = (List) quotationList.get(5);
            String linkedDocuments = (String) quotationList.get(6);
            String unlinkMessage = (String) quotationList.get(7);
            VendorQuotation quotation = (VendorQuotation) quotationList.get(0);
            if (quotationList.get(1) != null) {//fields updated
                additionalsauditmessage = quotationList.get(1).toString();
            }
            billid = quotation.getID();
            billno = quotation.getQuotationNumber();
            double totalAmount = 0;

//            double subTotalAmount=0 ; 
//            double taxAmount=0;
//            double totalTermAmount=0;
//            if (!StringUtil.isNullOrEmpty(request.getParameter("subTotal"))) {
//                subTotalAmount = Double.parseDouble(request.getParameter("subTotal"));
//            }
//            if (!StringUtil.isNullOrEmpty(request.getParameter("taxamount"))) {
//                taxAmount = Double.parseDouble(request.getParameter("taxamount"));
//            }
//            if(!StringUtil.isNullOrEmpty(request.getParameter("invoicetermsmap"))){
//                JSONArray termDetailsJArr=new JSONArray(request.getParameter("invoicetermsmap"));
//                for (int cnt = 0; cnt < termDetailsJArr.length(); cnt++) {
//                    JSONObject temp = termDetailsJArr.getJSONObject(cnt);
//                    if(temp!=null){
//                        double termAmount = Double.parseDouble(temp.getString("termamount"));
//                        totalTermAmount += termAmount;
//                    }
//                }
//            }
//            totalAmount = subTotalAmount + taxAmount + totalTermAmount ; 
//            issuccess = true;
//            int level = (isEdit && !isCopy) ? 0 : quotation.getApprovestatuslevel();
//            if(!isLeaseFixedAsset){
//                approvalStatusLevel = approveVendorQuotation(quotation, sessionHandlerImpl.getCompanyid(request), level, String.valueOf(totalAmount),currentUser,true,isFixedAsset);
//            }            
//            if (approvalStatusLevel != 11) {
//                butPendingForApproval = messageSource.getMessage("acc.field.butpendingforApproval", null, RequestContextUtils.getLocale(request));
//            }
            if (quotationList.get(2) != null) { // totalAmount
                totalAmount = Double.parseDouble(quotationList.get(2).toString());
            }
            if (quotationList.get(3) != null) {// approvalStatusLevel 
                approvalStatusLevel = Integer.parseInt(quotationList.get(3).toString());
            }
            if (quotationList.get(4) != null) {//butPendingForApproval 
                butPendingForApproval = quotationList.get(4).toString();
            }
            
            txnManager.commit(status);
            status = null;
            TransactionStatus AutoNoStatus = null;
            try {
                synchronized (this) {

                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("AutoNum_Tx");
                    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    AutoNoStatus = txnManager.getTransaction(def1);
                    /*if (StringUtil.isNullOrEmpty(quotationId) && sequenceformat.equals("NA")) {
                     KwlReturnObject pocnt = accPurchaseOrderobj.getQuotationCount(entryNumber, companyid);
                     while (pocnt.getRecordTotalCount() > 0) {
                     entryNumber=entryNumber+"-1";
                     pocnt = accPurchaseOrderobj.getQuotationCount(entryNumber, companyid);
                     }
                     billno = accPurchaseOrderobj.updateVQEntryNumberForNA(billid,entryNumber);
                     }*/
                    if (StringUtil.isNullOrEmpty(quotationId) && (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA"))) {
                        boolean seqformat_oldflag = false;//    old flag was used when sequence format not implemented.
                        String nextAutoNoInt = "", nextAutoNumber = "";
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_VENQUOTATION, sequenceformat);
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNumber);
                        } else {
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_VENQUOTATION, sequenceformat, seqformat_oldflag, quotation.getQuotationDate());
                        }
                        seqNumberMap.put(Constants.DOCUMENTID, billid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        billno = accPurchaseOrderobj.updateVQEntryNumberForNewVQ(seqNumberMap);
                    }
                    txnManager.commit(AutoNoStatus);
                }

                /*This block is executed if any VQ will go for pending approval 
                 * & mail wil be sent to admin 
                 */
                if (mailParams != null && !mailParams.isEmpty()) {
                    /**
                     * parameters required for sending mail
                     */
                    Map<String, Object> mailParameters = new HashMap();
                    mailParameters.put(Constants.companyid, companyid);
                    mailParameters.put(Constants.prNumber, billno);
                    
                    mailParameters.put(Constants.isCash, false);
                    mailParameters.put(Constants.createdBy, currentUser);
                    mailParameters.put(Constants.isEdit, isEdit);
                    mailParameters.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));
                    Iterator itr = mailParams.iterator();

                    while (itr.hasNext()) {
                        HashMap<String, Object> paramsMap = (HashMap<String, Object>) itr.next();
                        
                        mailParameters.put(Constants.ruleid, (String) paramsMap.get("ruleid"));
                        mailParameters.put(Constants.fromName, (String) paramsMap.get("fromName"));
                        mailParameters.put(Constants.hasApprover, (Boolean) paramsMap.get("hasApprover"));
                        if (isFixedAsset) {
                            
                            mailParameters.put(Constants.moduleid, Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
                            /* Method is used for sending mail to admin in case of Asset VQ */
                            sendMailToApprover(mailParameters);
                        } else {
                            mailParameters.put(Constants.moduleid, Constants.Acc_Vendor_Quotation_ModuleId);
                            /* Method is used for sending mail to admin in case of VQ*/
                            sendMailToApprover(mailParameters);
                        }

                    }

                }

            } catch (Exception ex) {
                if (AutoNoStatus != null) {
                    txnManager.rollback(AutoNoStatus);
                }
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Vendor_Quotation_ModuleId);
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
            /* Preparing Audit trial message if document is linking at the time of creating */
            String linkingMessages = "";
            if (!StringUtil.isNullOrEmpty(linkedDocuments) && !StringUtil.isNullOrEmpty(fromLinkCombo)) {
                linkingMessages = " by Linking to " + fromLinkCombo + " " + linkedDocuments;
            }

//        	msg = messageSource.getMessage("acc.so.save1", null, RequestContextUtils.getLocale(request));   //"Quotation has been saved successfully";
            if (isFixedAsset) {
                moduleName = Constants.ASSET_VENDOR_QUOTATION;
                msg = messageSource.getMessage("acc.field.assetVendorQuotationhasbeensavedsuccessfully", null, StringUtil.getLocale(paramJobj.getString(Constants.language))) + " " + butPendingForApproval + "<br/>"+messageSource.getMessage("acc.field.DocumentNo", null, StringUtil.getLocale(paramJobj.getString(Constants.language)))+": <b>" + billno + "</b>";
            } else {
                moduleName = Constants.VENDOR_QUOTATION;
                msg = messageSource.getMessage("acc.field.VendorQuotationhasbeensavedsuccessfully", null, StringUtil.getLocale(paramJobj.getString(Constants.language)));
                msg += (StringUtil.isNullOrEmpty(butPendingForApproval) ? "." : " ") + butPendingForApproval + "<br/>"+messageSource.getMessage("acc.field.DocumentNo", null, StringUtil.getLocale(paramJobj.getString(Constants.language)))+": <b>" + billno + "</b>";
            }

            if (!(isLeaseFixedAsset)) {//For normal VQ
                channelName = "/PurchaseQuotationReport/gridAutoRefresh";
            }

            String action = "added new";
            if (isEdit == true && isCopy == false) {
                action = "updated";
            }
            /*Inserting Entry in Audit trial when any document is unlinking through Edit*/
            
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(msg, status);
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
           
            if (!StringUtil.isNullOrEmpty(unlinkMessage)) {
                auditTrailObj.insertAuditLog(action, "User " + paramJobj.getString(Constants.userfullname) + " has unlinked " + "Vendor Quotation " + billno + unlinkMessage + ".", auditRequestParams, billno);
            }
            auditTrailObj.insertAuditLog(AuditAction.Vendor_Quotation, "User " + paramJobj.getString(Constants.userfullname) + " has " + action + " " + moduleName + " " + billno + linkingMessages + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + additionalsauditmessage, auditRequestParams, quotation.getID());
                      
            
            /*------Code if we edit pending document---------  */
            if (isEditedPendingDocument) {

                String roleName = quotationList.get(8) != null ? (String) quotationList.get(8) : "";
                boolean isAuthorityToApprove = quotationList.get(9) != null ? (Boolean) quotationList.get(9) : false;
                boolean sendPendingDocumentsToNextLevel = quotationList.get(10) != null ? (Boolean) quotationList.get(10) : false;

                /*--If check "Send pending documents to next level" is activated from system preferences---------  */
                if (sendPendingDocumentsToNextLevel) {

                    if (roleName != "" && isAuthorityToApprove) {

                        /*----Prepare Messages and inset AuditLog for approval document------  */
                        if (isFixedAsset) {
                            msg += "<br>";
                            msg += messageSource.getMessage("acc.field.assetVendorQuotationhasbeenapprovedsuccessfully", null, StringUtil.getLocale(paramJobj.getString(Constants.language)))+" by "+roleName+" "+paramJobj.getString(Constants.userfullname)+" at Level "+quotation.getApprovestatuslevel()+".";

                            auditTrailObj.insertAuditLog(AuditAction.Vendor_Quotation, "User " + paramJobj.getString(Constants.userfullname) + " has Approved a "+moduleName+" " + quotation.getQuotationNumber()+" at Level-"+quotation.getApprovestatuslevel(), auditRequestParams, quotation.getID());
                        } else {
                            msg += "<br>";
                            msg += messageSource.getMessage("acc.field.VendorQuotationhasbeenapprovedsuccessfully", null, StringUtil.getLocale(paramJobj.getString(Constants.language)))+" by "+roleName+" "+paramJobj.getString(Constants.userfullname)+" at Level "+quotation.getApprovestatuslevel()+".";

                            auditTrailObj.insertAuditLog(AuditAction.Vendor_Quotation, "User " + paramJobj.getString(Constants.userfullname) + " has Approved a "+moduleName+" " + quotation.getQuotationNumber()+" at Level-"+quotation.getApprovestatuslevel(), auditRequestParams, quotation.getID());
                        }
                    } else {//If User have no authority to approve the document
                        msg += "<br>";
                        msg += messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, StringUtil.getLocale(paramJobj.getString(Constants.language)))+ quotation.getApprovestatuslevel()+".";
                    }
                } else if (!isAuthorityToApprove && butPendingForApproval == "") {//If user have no authority to approve document
                    msg += "<br>";
                    msg += messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, StringUtil.getLocale(paramJobj.getString(Constants.language)))+ quotation.getApprovestatuslevel() + " and record will be available at this level for approval" + ".";
                }
            }
                                    
            issuccess = true;
            status = txnManager.getTransaction(def);
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Vendor_Quotation_ModuleId);
            txnManager.commit(status);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Vendor_Quotation_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Vendor_Quotation_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("billid", billid);
                jobj.put("billno", billno);
                jobj.put("isAccountingExe", accexception);
                jobj.put("pendingApproval", approvalStatusLevel != 11);
                jobj.put("channelName", channelName);
                jobj.put(Constants.isTaxDeactivated, isTaxDeactivated);

            } catch (JSONException ex) {
                Logger.getLogger(AccPurchaseOrderModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return  jobj;
    }

    public List saveQuotation(JSONObject paramJobj) throws SessionExpiredException, ServiceException, AccountingException, ScriptException, UnsupportedEncodingException,MessagingException {
        VendorQuotation quotation = null;
        List ll = new ArrayList();
        List mailParams = null;
        String unlinkMessage = "";
        try {
            int istemplate = paramJobj.optInt("istemplate",0);
            boolean isRoundingAdjustmentApplied = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.IsRoundingAdjustmentApplied, null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.IsRoundingAdjustmentApplied)) : false;
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv",null)) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            boolean isLeaseFixedAsset = (StringUtil.isNullOrEmpty(paramJobj.optString("isLeaseFixedAsset",null))) ? false:Boolean.parseBoolean(paramJobj.getString("isLeaseFixedAsset"));
            boolean isFixedAsset = StringUtil.isNullOrEmpty(paramJobj.optString("isFixedAsset",null)) ? false:Boolean.parseBoolean(paramJobj.getString("isFixedAsset"));
            boolean isEditedPendingDocument = StringUtil.isNullOrEmpty(paramJobj.optString("isEditedPendingDocument", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEditedPendingDocument"));
            
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParamsJson(paramJobj);
            String currentUser = paramJobj.getString(Constants.useridKey);
            String taxid = null;
//            taxid = paramJobj.optString("taxid",null);
            if (paramJobj.optString("taxid").equalsIgnoreCase("None")) {
                taxid = null;
            } else {
                taxid = paramJobj.optString("taxid", null);
            }
            double taxamount = StringUtil.getDouble(paramJobj.optString("taxamount"));
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate"));
            String sequenceformat = paramJobj.optString(Constants.sequenceformat, null);
            String companyid = paramJobj.getString(Constants.companyKey);

            String createdby = paramJobj.getString(Constants.useridKey);
            String modifiedby = paramJobj.getString(Constants.useridKey);
            long createdon = System.currentTimeMillis();
            long updatedon = createdon;

            String entryNumber = paramJobj.optString("number");
            String costCenterId = paramJobj.optString("costcenter");
            String quotationId = paramJobj.optString("invoiceid");
            String shipLength = paramJobj.optString("shipLength");
            String invoicetype = paramJobj.optString("invoicetype");
            String deletedLinkedDocumentID = paramJobj.optString("deletedLinkedDocumentId");
            String nextAutoNumber = "";
            String auditMessage = "";
            Map<String, Object> oldvq = new HashMap<String, Object>();
            Map<String, Object> newAuditKey = new HashMap<String, Object>();
            HashMap<String, Object> qDataMap = new HashMap<String, Object>();
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            String currencyid = (paramJobj.optString(Constants.currencyKey, null) == null ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyKey));
            Country country = extraCompanyPreferences.getCompany().getCountry();
            synchronized (this) {
                SequenceFormat prevSeqFormat = null;
                if (!StringUtil.isNullOrEmpty(quotationId)) { //For edit case
                    HashMap<String, Object> termReqMap = new HashMap<String, Object>();
                    KwlReturnObject pocount = accPurchaseOrderobj.getQuotationEditCount(entryNumber, companyid, quotationId);
                    if (pocount.getRecordTotalCount() > 0 && istemplate != 2 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Quotationnumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        KwlReturnObject rst = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), quotationId);
                        VendorQuotation vq = (VendorQuotation) rst.getEntityList().get(0);
                        prevSeqFormat = vq.getSeqformat();
                        
                        if (isEdit == true) {
                            setValuesForAuditTrialMessageForVQ(vq, paramJobj, oldvq, qDataMap, newAuditKey);
                        }
                        if (!sequenceformat.equals("NA")) {
                            nextAutoNumber = entryNumber;
                        }
                        qDataMap.put("id", quotationId);
                        if (!isFixedAsset && extraCompanyPreferences.isVersionslist()) {// Not FixedAsset Record and Activated Version History
                            saveQuotationVersion(paramJobj, quotationId);
                        }
                        if (isFixedAsset) {
                            HashMap<String, Object> requestMap = new HashMap<String, Object>();
                            requestMap.put("companyid", companyid);
                            requestMap.put("qid", quotationId);
                            accPurchaseOrderobj.deleteVendorQuotationAssetDetails(requestMap);
                        }
                        accPurchaseOrderobj.deleteQuotationDetails(quotationId, companyid);

                        //Delete Quotation Term Map
                        termReqMap.put("quotationid", quotationId);
                        if (!country.getID().equals(String.valueOf(Constants.indian_country_id))) {
                            accPurchaseOrderobj.deleteVendorQuotationTermMap(termReqMap);
                        }
                    }
                    /*Deleting linking information from VQ & Purchase Requisition Linking table during Editing VQ*/
                    termReqMap.put("qid", quotationId);
                    accPurchaseOrderobj.deleteLinkingInformationOfVQ(termReqMap);
                    /* Preparing Audit trial message while Editing VQ & unlinking document Purchase Requisition or RFQ */
                    if (!StringUtil.isNullOrEmpty(deletedLinkedDocumentID)) {
                        String[] deletedLinkedDocumentIDArr = deletedLinkedDocumentID.split(",");
                        for (int i = 0; i < deletedLinkedDocumentIDArr.length; i++) {
                            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), deletedLinkedDocumentIDArr[i]);
                            PurchaseRequisition purchaseRequisition = (PurchaseRequisition) venresult.getEntityList().get(0);
                            if (purchaseRequisition != null) {
                                if (i == 0) {
                                    unlinkMessage += " from the Purchase Requisiton(s) ";
                                }
                                if (unlinkMessage.indexOf(purchaseRequisition.getPrNumber()) == -1) {
                                    unlinkMessage += purchaseRequisition.getPrNumber() + ", ";
                                }
                            } else {
                                venresult = accountingHandlerDAOobj.getObject(RequestForQuotation.class.getName(), deletedLinkedDocumentIDArr[i]);
                                RequestForQuotation requestForQuotation = (RequestForQuotation) venresult.getEntityList().get(0);
                                if (requestForQuotation != null) {
                                    if (i == 0) {
                                        unlinkMessage += " from the RFQ(s) ";
                                    }
                                    if (unlinkMessage.indexOf(requestForQuotation.getRfqNumber()) == -1) {
                                        unlinkMessage += requestForQuotation.getRfqNumber() + ", ";
                                    }
                                }
                            }
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(unlinkMessage) && unlinkMessage.endsWith(", ")) {
                        unlinkMessage = unlinkMessage.substring(0, unlinkMessage.length() - 2);
                    }

                } /*else { //create new case
                 KwlReturnObject pocnt = accPurchaseOrderobj.getQuotationCount(entryNumber, companyid);
                 if (pocnt.getRecordTotalCount() > 0 && istemplate != 2 && sequenceformat.equals("NA")) {
                 throw new AccountingException(messageSource.getMessage("acc.field.Quotationnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                 }
                 }*/

                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Vendor_Quotation_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null,Locale.forLanguageTag(paramJobj.getString(Constants.language)))+ " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        }
                    }
                }

                /*if (!sequenceformat.equals("NA") && prevSeqFormat == null) { //case of gnerating sequence number
                 boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                 String nextAutoNoInt = "";
                 if (seqformat_oldflag) {
                 nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_VENQUOTATION, sequenceformat);
                 } else {
                 String[] nextAutoNoTemp = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_VENQUOTATION, sequenceformat, seqformat_oldflag);
                 nextAutoNumber = nextAutoNoTemp[0];
                 nextAutoNoInt = nextAutoNoTemp[1];
                 qDataMap.put(Constants.SEQFORMAT, sequenceformat);
                 qDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                 }
                 entryNumber = nextAutoNumber;
                 }*/
            }

            DateFormat df = authHandler.getDateOnlyFormat();
            String vendorId = paramJobj.optString("vendor");
            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorId);
            Vendor vendor = (Vendor) venresult.getEntityList().get(0);

            KwlReturnObject compResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences preferences = (ExtraCompanyPreferences) compResult.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(quotationId)) {//Edit PO Case for updating address detail
                Map<String, Object> addressParams = new HashMap<String, Object>();
                String billingAddress = paramJobj.optString(Constants.BILLING_ADDRESS);
                if (!StringUtil.isNullOrEmpty(billingAddress)) {
                    addressParams = AccountingAddressManager.getAddressParamsJson(paramJobj, true);
                } else { //handling the cases when no address coming in edit case 
                    if (preferences.isIsAddressFromVendorMaster()) {
                        addressParams = AccountingAddressManager.getDefaultVendorAddressParams(vendorId, companyid, accountingHandlerDAOobj);
                    } else {
                        addressParams = AccountingAddressManager.getDefaultVendorCompanyAddressParams(vendorId, companyid, accountingHandlerDAOobj);
                    }
                }
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), quotationId);
                VendorQuotation vq = (VendorQuotation) returnObject.getEntityList().get(0);
                addressParams.put("id", vq.getBillingShippingAddresses() == null ? "" : vq.getBillingShippingAddresses().getID());
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                qDataMap.put("billshipAddressid", bsa.getID());
            } else { //Other Cases for saving address detail
                boolean isDefaultAddress = paramJobj.optString("defaultAdress") != null ? Boolean.parseBoolean(paramJobj.optString("defaultAdress")) : false;
                Map<String, Object> addressParams = new HashMap<String, Object>();
                if (isDefaultAddress) {
                    if (preferences.isIsAddressFromVendorMaster()) {
                        addressParams = AccountingAddressManager.getDefaultVendorAddressParams(vendorId, companyid, accountingHandlerDAOobj);
                    } else {
                        addressParams = AccountingAddressManager.getDefaultVendorCompanyAddressParams(vendorId, companyid, accountingHandlerDAOobj);
                    }
                } else {
                    addressParams = AccountingAddressManager.getAddressParamsJson(paramJobj, true);
                }
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                qDataMap.put("billshipAddressid", bsa.getID());
            }
            qDataMap.put("externalCurrencyRate", externalCurrencyRate);
            if (sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(quotationId)) {
                qDataMap.put("entrynumber", entryNumber);
            } else {
                qDataMap.put("entrynumber", "");
            }
            qDataMap.put("autogenerated", sequenceformat.equals("NA") ? false : true);
            qDataMap.put("isFixedAsset", isFixedAsset);
            qDataMap.put("memo", paramJobj.optString("memo"));
            qDataMap.put("posttext", paramJobj.optString("posttext"));
            qDataMap.put("vendorid", paramJobj.optString("vendor"));
            qDataMap.put("orderdate", df.parse(paramJobj.optString("billdate")));
            qDataMap.put(Constants.Checklocktransactiondate, paramJobj.optString("billdate"));//ERP-16800-Without parsing date
            qDataMap.put("duedate", df.parse(paramJobj.optString("duedate")));
            qDataMap.put("perDiscount", StringUtil.getBoolean(paramJobj.optString("perdiscount")));
            qDataMap.put("discount", StringUtil.getDouble(paramJobj.optString("discount")));
            if (paramJobj.optString("shipdate") != null && !StringUtil.isNullOrEmpty(paramJobj.optString("shipdate"))) {
                qDataMap.put("shipdate", df.parse(paramJobj.optString("shipdate")));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("validdate"))) {
                qDataMap.put("validdate", df.parse(paramJobj.optString("validdate")));
            }
            qDataMap.put("shipvia", paramJobj.optString("shipvia"));
            qDataMap.put("termid", paramJobj.optString("termid"));
            qDataMap.put("fob", paramJobj.optString("fob"));
            qDataMap.put("currencyid", currencyid);
            qDataMap.put("isfavourite", paramJobj.optString("isfavourite"));
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                qDataMap.put("costCenterId", costCenterId);
            }
            qDataMap.put("companyid", companyid);
            qDataMap.put("createdby", createdby);
            qDataMap.put("modifiedby", modifiedby);
            qDataMap.put("createdon", createdon);
            qDataMap.put("updatedon", updatedon);
            qDataMap.put("formtypeid", paramJobj.optString("formtypeid"));
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("GTAApplicable", ""))) {
                qDataMap.put("gtaapplicable", paramJobj.optBoolean("GTAApplicable", false));
            }
            
            if (!StringUtil.isNullOrEmpty(shipLength)) {
                qDataMap.put("shipLength", shipLength);
            }
            if (!StringUtil.isNullOrEmpty(invoicetype)) {
                qDataMap.put("invoicetype", invoicetype);
            }
            qDataMap.put("agent", paramJobj.optString("agent"));
            qDataMap.put("istemplate", istemplate);
            qDataMap.put("venbilladdress", paramJobj.optString("venbilladdress",null) == null ? "" : paramJobj.getString("venbilladdress"));
            qDataMap.put("venshipaddress", paramJobj.optString("venshipaddress",null) == null ? "" : paramJobj.getString("venshipaddress"));
            qDataMap.put("gstIncluded", paramJobj.optString("includingGST",null) == null ? false : Boolean.parseBoolean(paramJobj.getString("includingGST")));
            
            if (paramJobj.has(Constants.SUPPLIERINVOICENO) && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.SUPPLIERINVOICENO))) {
                qDataMap.put(Constants.SUPPLIERINVOICENO, paramJobj.optString(Constants.SUPPLIERINVOICENO));
            }
            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
                qDataMap.put("taxid", taxid);
            } else {
                qDataMap.put("taxid", taxid);     // Put taxid as null if the VQ doesnt have any total tax included. (To avoid problem while editing VQ)
            }

            double subTotalAmount = 0, taxAmount = 0, totalTermAmount = 0, totalAmount = 0, totalAmountinbase = 0, totalRowDiscount = 0;

            JSONArray jArr = new JSONArray(paramJobj.optString("detail"));

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);

                double qrate = authHandler.roundUnitPrice(jobj.getDouble("rate"), companyid);
                double quantity = authHandler.roundQuantity(jobj.getDouble("quantity"), companyid);
                boolean gstIncluded = paramJobj.optString("includingGST",null) == null ? false : Boolean.parseBoolean(paramJobj.getString("includingGST"));
                if (gstIncluded) {
                    qrate = authHandler.roundUnitPrice(jobj.getDouble("rateIncludingGst"), companyid);
                }

                double quotationPrice = authHandler.round(quantity * qrate, companyid);
                double discountQD = authHandler.round(jobj.optDouble("prdiscount", 0), companyid);
                double discountPerRow = 0;

                if (jobj.optInt("discountispercent", 0) == 1) {
                    discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                } else {
                    discountPerRow = discountQD;
                }

                totalRowDiscount += discountPerRow;

            }
            totalRowDiscount = authHandler.round(totalRowDiscount, companyid);
            qDataMap.put("totallineleveldiscount", totalRowDiscount);

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("subTotal"))) {
                subTotalAmount = Double.parseDouble(paramJobj.getString("subTotal"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxamount"))) {
                taxAmount = Double.parseDouble(paramJobj.getString("taxamount"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("invoicetermsmap"))) {
                JSONArray termDetailsJArr = new JSONArray(paramJobj.optString("invoicetermsmap"));
                for (int cnt = 0; cnt < termDetailsJArr.length(); cnt++) {
                    JSONObject temp = termDetailsJArr.getJSONObject(cnt);
                    if (temp != null) {
                        double termAmount = Double.parseDouble(temp.getString("termamount"));
                        totalTermAmount += termAmount;
                    }
                }
            }
            totalAmount = subTotalAmount + taxAmount + totalTermAmount;

            //Rounding Adjustment will always calculated after calculation of totalInvAmount
            double roundingadjustmentAmount = 0.0, roundingadjustmentAmountinbase = 0.0;
            String roundingAdjustmentAccountID = "";
            String columnPref = extraCompanyPreferences.getColumnPref();
            if (!StringUtil.isNullOrEmpty(columnPref)) {
                JSONObject prefObj = new JSONObject(columnPref);
                roundingAdjustmentAccountID = prefObj.optString(Constants.RoundingAdjustmentAccountID, "");
            }

            if (isRoundingAdjustmentApplied && !StringUtil.isNullOrEmpty(roundingAdjustmentAccountID)) {
                double totalInvAmountAfterRound = Math.round(totalAmount);
                roundingadjustmentAmount = authHandler.round(totalInvAmountAfterRound - totalAmount, companyid);
                if (roundingadjustmentAmount != 0) {
                    totalAmount = totalInvAmountAfterRound;//Now rounded value becomes total quotation amount
                    qDataMap.put(Constants.roundingadjustmentamount, roundingadjustmentAmount);
                    qDataMap.put(Constants.roundingadjustmentamountinbase, roundingadjustmentAmount);

                    String globalcurrency = paramJobj.getString(Constants.globalCurrencyKey);
                    if (!globalcurrency.equalsIgnoreCase(currencyid)) {
                        HashMap<String, Object> roundingRequestParams = new HashMap<String, Object>();
                        roundingRequestParams.put("companyid", companyid);
                        roundingRequestParams.put("gcurrencyid", (paramJobj.optString(Constants.currencyKey, null) == null ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyKey)));
                        KwlReturnObject baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(roundingRequestParams, roundingadjustmentAmount, currencyid, df.parse(paramJobj.optString("billdate")), externalCurrencyRate);
                        roundingadjustmentAmountinbase = authHandler.round((Double) baseAmt.getEntityList().get(0), companyid);
                        qDataMap.put(Constants.roundingadjustmentamountinbase, roundingadjustmentAmountinbase);
                    }
                }
            }
            qDataMap.put(Constants.IsRoundingAdjustmentApplied, isRoundingAdjustmentApplied);
            //Rounding Adjustment Code End
            qDataMap.put("quotationamount", totalAmount);

            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            filterRequestParams.put("companyid", companyid);
            filterRequestParams.put("gcurrencyid", (paramJobj.optString(Constants.currencyKey, null) == null ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyKey)));
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalAmount, currencyid, df.parse(paramJobj.optString("billdate")), externalCurrencyRate);
            totalAmountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
            qDataMap.put("quotationamountinbase", totalAmountinbase);

            KwlReturnObject discAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalRowDiscount, currencyid, df.parse(paramJobj.optString("billdate")), externalCurrencyRate);
            double descountinBase = authHandler.round((Double) discAmt.getEntityList().get(0), companyid);

            qDataMap.put("discountinbase", descountinBase);

            String vendorQuotationTerms = paramJobj.optString("invoicetermsmap");
            if (StringUtil.isAsciiString(vendorQuotationTerms)) {
                if (new JSONArray(vendorQuotationTerms).length() > 0) {
                    qDataMap.put(Constants.termsincludegst, Boolean.parseBoolean(paramJobj.optString(Constants.termsincludegst)));
                }
            }

            if(extraCompanyPreferences.isIsNewGST()){
                /**
                 * ERP-32829 
                 */
                boolean gstapplicable = paramJobj.optBoolean("GSTApplicable",false);
                qDataMap.put("gstapplicable", gstapplicable);
                qDataMap.put(Constants.isMerchantExporter, paramJobj.optBoolean(Constants.isMerchantExporter,false));
            }
            qDataMap.put(Constants.isApplyTaxToTerms, paramJobj.optBoolean(Constants.isApplyTaxToTerms,false));
            KwlReturnObject soresult = accPurchaseOrderobj.saveVendorQuotation(qDataMap);
            quotation = (VendorQuotation) soresult.getEntityList().get(0);
            /**
             * Save GST History Customer/Vendor data.
             */
            if (quotation.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                paramJobj.put("docid", quotation.getID());
                paramJobj.put("moduleid", isFixedAsset?Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId:Constants.Acc_Vendor_Quotation_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(paramJobj);
            }
            /*
             * Saving Entry in Linking information table of PR & VQ while saving
             * VQ linked with PR
             */
            String linkmode = paramJobj.optString("fromLinkCombo");
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            String linkedDocuments = "";
            if (!StringUtil.isNullOrEmpty(linkmode) && (linkmode.equalsIgnoreCase("Purchase Requisition") || linkmode.equalsIgnoreCase("Asset Purchase Requisition"))) {
                /*
                 * ID of PR while linking with VQ at the time of creating VQ
                 */
                String[] linkNumbers = paramJobj.optString("linkNumber").split(",");
                if (linkNumbers.length > 0) {
                    for (int i = 0; i < linkNumbers.length; i++) {
                        if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {

                            /*
                             * Saving linking information in Purchase
                             * Requisition linking table
                             */
                            requestParams1.put("linkeddocid", quotation.getID());
                            requestParams1.put("docid", linkNumbers[i]);
                            requestParams1.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                            requestParams1.put("linkeddocno", entryNumber);
                            requestParams1.put("sourceflag", 0);
                            KwlReturnObject result = accPurchaseOrderobj.savePurchaseRequisitionLinking(requestParams1);

                            /*
                             * Saving linking information in Vendor
                             * Quotation linking table
                             */
                            requestParams1.clear();
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), linkNumbers[i]);
                            PurchaseRequisition purchaseRequisition = (PurchaseRequisition) rdresult.getEntityList().get(0);
                            requestParams1.put("linkeddocid", linkNumbers[i]);
                            requestParams1.put("docid", quotation.getID());
                            requestParams1.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                            requestParams1.put("linkeddocno", purchaseRequisition.getPrNumber());
                            requestParams1.put("sourceflag", 1);
                            result = accPurchaseOrderobj.saveVQLinking(requestParams1);
                            linkedDocuments += purchaseRequisition.getPrNumber() + " ,";

                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(linkedDocuments)) {
                    linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                }
            }
            if (!StringUtil.isNullOrEmpty(linkmode) && linkmode.equalsIgnoreCase("RFQ")) {
                /*
                 * ID of RFQ while linking with VQ at the time of creating VQ
                 */
                String[] linkNumbers = paramJobj.optString("linkNumber").split(",");
                if (linkNumbers.length > 0) {
                    for (int i = 0; i < linkNumbers.length; i++) {
                        if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
                            /*
                             * Saving linking information in RFQ
                             *  linking table
                             */
                            requestParams1.put("linkeddocid", quotation.getID());
                            requestParams1.put("docid", linkNumbers[i]);
                            requestParams1.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                            requestParams1.put("linkeddocno", entryNumber);
                            requestParams1.put("sourceflag", 0);
                            KwlReturnObject result = accPurchaseOrderobj.saveRFQLinking(requestParams1);
                            /*
                             * Saving linking information in Vendor
                             * Quotation linking table
                             */
                            requestParams1.clear();
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(RequestForQuotation.class.getName(), linkNumbers[i]);
                            RequestForQuotation requestForQuotation = (RequestForQuotation) rdresult.getEntityList().get(0);
                            requestParams1.put("linkeddocid", linkNumbers[i]);
                            requestParams1.put("docid", quotation.getID());
                            requestParams1.put("moduleid", Constants.Acc_RFQ_ModuleId);
                            requestParams1.put("linkeddocno", requestForQuotation.getRfqNumber());
                            requestParams1.put("sourceflag", 1);
                            result = accPurchaseOrderobj.saveVQLinking(requestParams1);
                        }
                    }
                }
            }

            qDataMap.put("id", quotation.getID());
//            if (country != null && Integer.parseInt(country.getID()) == Constants.indian_country_id && vendor != null && vendor.getGSTRegistrationType() != null) {
//                MasterItem gstRegistrationType = vendor.getGSTRegistrationType();
//                if (gstRegistrationType != null && gstRegistrationType.getDefaultMasterItem() != null && !StringUtil.isNullOrEmpty(gstRegistrationType.getDefaultMasterItem().getID())) {
//                    paramJobj.put("isUnRegisteredDealer", gstRegistrationType.getDefaultMasterItem().getID().equals(Constants.GSTRegType.get(Constants.GSTRegType_Unregistered)));;
//                }
//            }
            HashSet sodetails = saveQuotationRows(paramJobj, quotation, companyid);
            quotation.setRows(sodetails);
            //Save record as template
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("templatename")) && (istemplate == 1 || istemplate == 2)) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("templatename", paramJobj.optString("templatename"));
                hashMap.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                hashMap.put("modulerecordid", quotation.getID());
                hashMap.put("companyid", companyid);
                if(!StringUtil.isNullOrEmpty(paramJobj.optString("companyunitid",null))){
                    hashMap.put("companyunitid", paramJobj.getString("companyunitid")); // Added Unit ID if it is present in request
                }
                accountingHandlerDAOobj.saveModuleTemplate(hashMap);
            }

            if (StringUtil.isAsciiString(vendorQuotationTerms)) {
                mapInvoiceTerms(vendorQuotationTerms, quotation.getID(), currentUser, true);
            }
            if (country.getID() != null && Constants.indian_country_id == Integer.parseInt(country.getID()) && extraCompanyPreferences.isExciseApplicable()) {
                HashMap exciseDetails = null;
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("indiaExcise",null))) {
                    JSONObject jobj = new JSONObject(paramJobj.optString("indiaExcise"));
                    if (extraCompanyPreferences.isExciseApplicable()) {
                        jobj.put("quatationid", quotation.getID());
                        exciseDetails = mapExciseDetails(jobj, paramJobj);
                        accPurchaseOrderobj.saveAssetExciseDetails(exciseDetails);
                    }
                }
            }
            if (isEdit == true) { //For Audit Trial-ERP-14034
                //ERP-14034 
                DateFormat sdf = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);
                if (quotation.getShipdate() != null) {
                    qDataMap.put("AuditShipDate", sdf.format(quotation.getShipdate()));  //New Ship Date
                } else {
                    qDataMap.put("AuditShipDate", "");
                }
                if (quotation.getDueDate() != null) {
                    qDataMap.put("AuditDuedate", sdf.format(quotation.getDueDate()));  //New Due Date
                } else {
                    qDataMap.put("AuditDuedate", "");
                }
                if (quotation.getQuotationDate() != null) {
                    qDataMap.put("AuditOrderDate", sdf.format(quotation.getQuotationDate()));  //New Order Date
                } else {
                    qDataMap.put("AuditOrderDate", "");
                }

                if (quotation.getValiddate() != null) {
                    qDataMap.put("AuditValiddate", sdf.format(quotation.getValiddate()));  //New Valid till
                } else {
                    qDataMap.put("AuditValiddate", "");
                }

                if (quotation.getMasteragent() != null) {
                    KwlReturnObject newmasteritemobretrurnlist = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), quotation.getMasteragent().getID());
                    MasterItem newsalesPerson = (MasterItem) newmasteritemobretrurnlist.getEntityList().get(0);
                    qDataMap.put("Sales Person", newsalesPerson != null ? newsalesPerson.getValue() : "");

                } else {
                    qDataMap.put("Sales Person", "");
                }
            }
            // Product to Discount Mapping
            Set<VendorQuotationDetail> qdetails = (Set<VendorQuotationDetail>) sodetails;
            JSONArray productDiscountJArr = new JSONArray();
            for (VendorQuotationDetail qDetail : qdetails) {
                String productId = qDetail.getProduct().getID();
                double discountVal = qDetail.getDiscount();
                int isDiscountPercent = qDetail.getDiscountispercent();
                if (isDiscountPercent == 1) {
                    discountVal = (qDetail.getQuantity() * qDetail.getRate()) * (discountVal / 100);
                }
                KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, df.parse(paramJobj.optString("billdate")), externalCurrencyRate);
                double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                discAmountinBase = authHandler.round(discAmountinBase, companyid);
                JSONObject productDiscountObj = new JSONObject();
                productDiscountObj.put("productId", productId);
                productDiscountObj.put("discountAmount", discAmountinBase);
                productDiscountJArr.put(productDiscountObj);
            }

            // Save global-level CUSTOMFIELDS in VendorQuotation
            String customfield = paramJobj.optString("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_VendorQuotation_modulename);
                customrequestParams.put("moduleprimarykey", "VendorQuotationId");
                customrequestParams.put("modulerecid", quotation.getID());
                customrequestParams.put("moduleid", !isFixedAsset ? Constants.Acc_Vendor_Quotation_ModuleId : Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_VendorQuotation_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    qDataMap.put("accvendorquotationcustomdataref", quotation.getID());
                    KwlReturnObject accresult = accPurchaseOrderobj.updateVendorQuotationCustomData(qDataMap);
                }
            }

            // Check for approval rules and apply rules if available
            String butPendingForApproval = "";
            int approvalStatusLevel = 11;
            List approvedlevel = null;
            HashMap<String, Object> qApproveMap = new HashMap<String, Object>();
            int level = (isEdit && !isCopy) ? 0 : quotation.getApprovestatuslevel();
            qApproveMap.put("companyid", paramJobj.getString(Constants.companyKey));
            qApproveMap.put("level", level);
            qApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmountinbase, companyid)));
            qApproveMap.put("currentUser", currentUser);
            qApproveMap.put("fromCreate", true);
            qApproveMap.put("isFixedAsset", isFixedAsset);
            qApproveMap.put("isEdit", isEdit);
            qApproveMap.put("productDiscountMapList", productDiscountJArr);
            if (isFixedAsset) {
                qApproveMap.put("moduleid", Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
            } else {
                qApproveMap.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
            }
            boolean ismailApplicable = false;

            boolean sendPendingDocumentsToNextLevel = false;
            boolean pendingMessage = true;
           
            JSONObject columnprefObj = new JSONObject();
            if (!StringUtil.isNullOrEmpty(preferences.getColumnPref())) {
                columnprefObj = new JSONObject((String) preferences.getColumnPref());
            }

            if (columnprefObj.has("sendPendingDocumentsToNextLevel") && columnprefObj.get("sendPendingDocumentsToNextLevel") != null && (Boolean) columnprefObj.get("sendPendingDocumentsToNextLevel") != false) {
                sendPendingDocumentsToNextLevel = true;
            }
                
            
            /*------If pending document is Edited & Check is activated from system preferences
             *---Then Edit will work same as while approving document
             */
            if (isEditedPendingDocument) {
                level = quotation.getApprovestatuslevel();
                qApproveMap.put("fromCreate", false);
                qApproveMap.put("documentLevel", level);
                qApproveMap.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));

                if (sendPendingDocumentsToNextLevel) {

                    ismailApplicable = true;
                    qApproveMap.put("level", level);
                    pendingMessage=false;

                } else {
                    qApproveMap.put("isEditedPendingDocumentWithCheckOff", true);
                }
            }
            
            if (!isLeaseFixedAsset) {

                approvedlevel = approveVendorQuotation(quotation, qApproveMap, ismailApplicable);
                approvalStatusLevel = (Integer) approvedlevel.get(0);
                mailParams = (List) approvedlevel.get(1);

//                approvalStatusLevel = approveVendorQuotation(quotation, qApproveMap);
            } else {
                quotation.setApprovestatuslevel(11);
            }
            
            
            List approvalHistoryList = null;
            String roleName = "";
            boolean isAuthorityToApprove = false;


            /*-----Block is executed when Edited pending Document & Check "Send pending documents to next level" is activated-------*/
            /*------If pending document is Edited & Check is activated from system preferences
             *---Then Edit will work same as while approving document
             */
            if (isEditedPendingDocument && sendPendingDocumentsToNextLevel) {

                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences compreferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                /*---Document will approve as approval level -----  */
                if (approvalStatusLevel != Constants.NoAuthorityToApprove && compreferences.isSendapprovalmail()) {

                    HashMap emailMap = new HashMap();

                    String userName = paramJobj.optString(Constants.username, null);
                    emailMap.put("userName", userName);
             
                    emailMap.put("company", company);
                    emailMap.put("vendorQuotation", quotation);
                    emailMap.put("baseUrl", paramJobj.optString("baseUrl", null));
                    emailMap.put("preferences", compreferences);
                    emailMap.put("isFixedAsset", isFixedAsset);
                    emailMap.put("ApproveMap", qApproveMap);

                    sendApprovalMailForVQIfAllowedFromSystemPreferences(emailMap);

                }

                /*--------Save Approval history Code--------  */
                if (approvalStatusLevel != Constants.NoAuthorityToApprove) {

                    HashMap approvalHistoryMap = new HashMap();
                    String userid = paramJobj.optString(Constants.userid, null);
                    approvalHistoryMap.put("userid", userid);
                    approvalHistoryMap.put("company", company);
                    approvalHistoryMap.put("vendorQuotation", quotation);

                    approvalHistoryList = saveApprovalHistoryForVq(approvalHistoryMap);
                    roleName = approvalHistoryList != null ? approvalHistoryList.get(0).toString() : "";
                    isAuthorityToApprove = true;

                } else {
                    /*----If User have no authority to approve------  */
                    isAuthorityToApprove = false;
                }
            }
            
            
            
            if (approvalStatusLevel != 11 && pendingMessage) {
                butPendingForApproval = messageSource.getMessage("acc.field.butpendingforApproval", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
            }
            
            /*-------If  "Send pending documents to next level" check is OFF and User have no authority to approve--------- */
            if (isEditedPendingDocument && !sendPendingDocumentsToNextLevel && approvalStatusLevel == Constants.NoAuthorityToApprove) {
                isAuthorityToApprove = false;
                butPendingForApproval = "";
            }

            String moduleName = Constants.VENDOR_QUOTATION;
            if (isFixedAsset) {
                moduleName = Constants.ASSET_VENDOR_QUOTATION;
            }
            //Send Mail when Vendor Quotation is generated or modified.
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), paramJobj.getString(Constants.companyKey));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(quotationId)) {
                    if (isFixedAsset && documentEmailSettings.isAssetVendorQuotationGenerationMail()) {
                        sendmail = true;
                    } else if (documentEmailSettings.isVendorQuotationGenerationMail()) {
                        sendmail = true;
                    }
                } else {
                    isEditMail = true;
                    if (isFixedAsset && documentEmailSettings.isAssetVendorQuotationUpdationMail()) {
                        sendmail = true;
                    } else if (documentEmailSettings.isVendorQuotationUpdationMail()) {
                        sendmail = true;
                    }
                }
                 if (sendmail) {//if allow to send alert mail when option selected in companypreferences
                    String userMailId = "", userName = "",currentUserid="";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> requestParams = AccountingManager.getEmailNotificationParamsJson(paramJobj);
                    if (requestParams.containsKey("userfullName") && requestParams.get("userfullName") != null) {
                        userName = (String) requestParams.get("userfullName");
                    }
                    if (requestParams.containsKey("usermailid") && requestParams.get("usermailid") != null) {
                        userMailId = (String) requestParams.get("usermailid");
                    }
                     if(requestParams1.containsKey(Constants.useridKey)&& requestParams1.get(Constants.useridKey)!=null){
                        currentUserid=(String)requestParams1.get(Constants.useridKey);
                    }
                    List<String> mailIds = new ArrayList();
                    if (!StringUtil.isNullOrEmpty(userMailId)) {
                        mailIds.add(userMailId);
                    }
                     /*
                      if Edit mail option is true then get userid and Email id of document creator.
                      */
                     if (isEditMail) {
                         if (quotation != null && quotation.getCreatedby() != null) {
                             createdByEmail = quotation.getCreatedby().getEmailID();
                             createdById = quotation.getCreatedby().getUserID();
                         }
                         /*
                          if current user userid == document creator userid then don't add creator email ID in List.
                          */
                         if (!StringUtil.isNullOrEmpty(createdByEmail) && !(currentUserid.equalsIgnoreCase(createdById))) {
                             mailIds.add(createdByEmail);
                         }
                     }             
                    String[] temp = new String[mailIds.size()];
                    String[] tomailids = mailIds.toArray(temp);
                    String vqNumber = entryNumber;
                    accountingHandlerDAOobj.sendSaveTransactionEmails(vqNumber, moduleName, tomailids, userName, isEditMail, companyid);
                }
            }

            if (isEdit == true) {
                int moduleid = Constants.Acc_Vendor_Quotation_ModuleId;
                auditMessage = AccountingManager.BuildAuditTrialMessage(qDataMap, oldvq, moduleid, newAuditKey);
            }
            ll.add(quotation);
            ll.add(auditMessage);
            ll.add(totalAmount);
            ll.add(approvalStatusLevel);
            ll.add(butPendingForApproval);
            ll.add(mailParams);
            ll.add(linkedDocuments);
            ll.add(unlinkMessage);
            ll.add(roleName);
            ll.add(isAuthorityToApprove);
            ll.add(sendPendingDocumentsToNextLevel);
        } catch (JSONException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveQuotation : " + ex.getMessage(), ex);
        }
        return ll;
    }
    public List<String> approveVendorQuotation(VendorQuotation doObj, HashMap<String, Object> qApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException {
        boolean hasAuthority = false;
        String companyid = "";
        List returnList = new ArrayList();
        List mailParamList = new ArrayList();
        int returnStatus;
        if (qApproveMap.containsKey("companyid") && qApproveMap.get("companyid") != null) {
            companyid = qApproveMap.get("companyid").toString();
        }
        String currentUser = "";
        if (qApproveMap.containsKey("currentUser") && qApproveMap.get("currentUser") != null) {
            currentUser = qApproveMap.get("currentUser").toString();
        }
        int level = 0;
        if (qApproveMap.containsKey("level") && qApproveMap.get("level") != null) {
            level = Integer.parseInt(qApproveMap.get("level").toString());
        }
        String amount = "";
        if (qApproveMap.containsKey("totalAmount") && qApproveMap.get("totalAmount") != null) {
            amount = qApproveMap.get("totalAmount").toString();
        }
        boolean fromCreate = false;
        if (qApproveMap.containsKey("fromCreate") && qApproveMap.get("fromCreate") != null) {
            fromCreate = Boolean.parseBoolean(qApproveMap.get("fromCreate").toString());
        }
        boolean isFixedAsset = false;
        if (qApproveMap.containsKey("isFixedAsset") && qApproveMap.get("isFixedAsset") != null) {
            isFixedAsset = Boolean.parseBoolean(qApproveMap.get("isFixedAsset").toString());
        }
        boolean isEdit = false;
        if (qApproveMap.containsKey("isEdit") && qApproveMap.get("isEdit") != null) {
            isEdit = Boolean.parseBoolean(qApproveMap.get("isEdit").toString());
        }
        JSONArray productDiscountMapList = null;
        if (qApproveMap.containsKey("productDiscountMapList") && qApproveMap.get("productDiscountMapList") != null) {
            productDiscountMapList = new JSONArray(qApproveMap.get("productDiscountMapList").toString());
        }
        if (!fromCreate) {
            String thisUser = currentUser;
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);

            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                
                /*
                 If "Send approval documents to next level" is disabled from system preferences & pending document is edited then
                 1. When user is authorised then document is always goes at first level
                 2. When user is not authorised then document remains at same level
                 
                 */
                boolean isEditedPendingDocumentWithCheckOff = false;
                if (qApproveMap.containsKey("isEditedPendingDocumentWithCheckOff") && qApproveMap.get("isEditedPendingDocumentWithCheckOff") != null) {
                    level = Integer.parseInt(qApproveMap.get("documentLevel").toString());//Actual level of document for fetching rule at that level for the user
                    isEditedPendingDocumentWithCheckOff = true;
                }
                if (isFixedAsset) {
                    hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRule(level, companyid, amount, thisUser, Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
                } else {
                    hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRule(level, companyid, amount, thisUser, Constants.Acc_Vendor_Quotation_ModuleId);
                }

                /*---If User is authorised at this level then sending document to first level that's why assigning "level=0" ------ */
                if (isEditedPendingDocumentWithCheckOff && hasAuthority) {
                    level = 0;
                }
            }
        } else {
            hasAuthority = true;
        }
        if (hasAuthority) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            int approvalStatus = 11;
            String prNumber = doObj.getQuotationNumber();
            String vqID = doObj.getID();
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("level", level + 1);

            if (isFixedAsset) {
                qdDataMap.put("moduleid", Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
            } else {
                qdDataMap.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
            }

            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            String fromName = "User";
            fromName = doObj.getCreatedby().getFirstName().concat(" ").concat(doObj.getCreatedby().getLastName());
            /**
             * parameters required for sending mail
             */
            Map<String, Object> mailParameters = new HashMap();
            mailParameters.put(Constants.companyid, companyid);
            mailParameters.put(Constants.prNumber, prNumber);
            mailParameters.put(Constants.fromName, fromName);
            mailParameters.put(Constants.isCash, false);
            mailParameters.put(Constants.isEdit, isEdit);
            mailParameters.put(Constants.createdBy, doObj.getCreatedby().getUserID());
            if (qApproveMap.containsKey(Constants.PAGE_URL)) {
                mailParameters.put(Constants.PAGE_URL, (String) qApproveMap.get(Constants.PAGE_URL));
            }
            Iterator itr = flowresult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                mailParameters.put(Constants.ruleid, row[0].toString());
                //            JSONObject obj = new JSONObject();
                String rule = "";
                HashMap<String, Object> recMap = new HashMap();
                if (row[2] != null) {
                    rule = row[2].toString();
                }
                String discountRule = "";
                if (row[7] != null) {
                    discountRule = row[7].toString();
                }

                boolean sendForApproval = false;
                int appliedUpon = Integer.parseInt(row[5].toString());
                if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                    if (productDiscountMapList != null) {
                        sendForApproval = AccountingManager.checkForProductAndProductDiscountRule(productDiscountMapList, appliedUpon, rule, discountRule);
                    }
                }else if(appliedUpon ==Constants.Specific_Products_Category){
                    /*
                     * Check If Rule is apply on product
                     * category from multiapproverule window
                     */
                    sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productDiscountMapList, appliedUpon, rule);
                }else {
                    rule = rule.replaceAll("[$$]+", amount);
                }
                if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon != Constants.Specific_Products_Category && Boolean.parseBoolean(engine.eval(rule).toString())) || sendForApproval) {
                    // send emails
                    boolean hasApprover = Boolean.parseBoolean(row[3].toString());
                    mailParameters.put(Constants.hasApprover, hasApprover);
                    mailParameters.put("level", level);
                    if (isFixedAsset) {
                        if (isMailApplicable) {
                            mailParameters.put(Constants.moduleid, Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
                            sendMailToApprover(mailParameters);

                        } else {
                            
                          /* This block will be executed 
                         * if any Asset VQ will go for pending approval*/
                            
                            recMap.put("ruleid", row[0].toString());
                            recMap.put("fromName", fromName);
                            recMap.put("hasApprover", hasApprover);
                            
                            mailParamList.add(recMap);
                        }
                        approvalStatus = level + 1;
                    } else {
                        if (isMailApplicable) {
                            mailParameters.put(Constants.moduleid, Constants.Acc_Vendor_Quotation_ModuleId);
                            sendMailToApprover(mailParameters);
                        } else {
                            
                        /* This block will be executed 
                         * if any VQ will go for pending approval*/
                            
                            recMap.put("ruleid", row[0].toString());
                            recMap.put("fromName", fromName);
                            recMap.put("hasApprover", hasApprover);

                            mailParamList.add(recMap);
                        }
                        approvalStatus = level + 1;
                    }

                }
            }
            accPurchaseOrderobj.approvePendingVendorQuotation(vqID, companyid, approvalStatus);
            returnStatus = approvalStatus;
        } else {
            returnStatus = Constants.NoAuthorityToApprove; //if not have approval permission then return one fix value like 999
        }
        returnList.add(returnStatus);
        returnList.add(mailParamList);

        return returnList;

    }
    private void setValuesForAuditTrialMessageForVQ(VendorQuotation vq, JSONObject paramJobj, Map<String, Object> oldvq, Map<String, Object> qDataMap, Map<String, Object> newAuditKey) throws SessionExpiredException {
        try {
          DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);
            //Setting values in map for oldpo
            if (vq != null) {
                if (vq.getTerm().getID() != null) {
                    KwlReturnObject olddebittermresult = accountingHandlerDAOobj.getObject(Term.class.getName(), vq.getTerm().getID());
                    Term term = (Term) olddebittermresult.getEntityList().get(0);
                    oldvq.put(Constants.DebitTermName, term.getTermname());
                    newAuditKey.put(Constants.DebitTermName, "Debit Term");
                }
                
                if (vq.getMasteragent() != null) {
                    KwlReturnObject oldmasteritemobretrurnlist = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), vq.getMasteragent().getID());
                    MasterItem oldsalesPerson = (MasterItem) oldmasteritemobretrurnlist.getEntityList().get(0);
                    oldvq.put("Sales Person", oldsalesPerson != null ? oldsalesPerson.getValue() : "");

                } else {
                    oldvq.put("Sales Person", "");
                }
                newAuditKey.put("Sales Person", "Agent");
                KwlReturnObject currobretrurnlist = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), vq.getCurrency().getCurrencyID());
                KWLCurrency oldcurrencyobj = (KWLCurrency) currobretrurnlist.getEntityList().get(0);
                KwlReturnObject venobretrurnlist = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vq.getVendor().getID());
                Vendor oldvendor = (Vendor) venobretrurnlist.getEntityList().get(0);
                oldvq.put(Constants.VendorName, oldvendor.getName());
                newAuditKey.put(Constants.VendorName, "Vendor");
                oldvq.put("entrynumber", vq.getQuotationNumber());
                newAuditKey.put("entrynumber", "Entry Number");
                oldvq.put(Constants.CurrencyName, oldcurrencyobj.getName());//Currency name
                newAuditKey.put(Constants.CurrencyName, "Currency");
                oldvq.put("memo", StringUtil.isNullOrEmpty(vq.getMemo()) ? "" : vq.getMemo());
                newAuditKey.put("memo", "Memo");
                oldvq.put("shipvia", StringUtil.isNullOrEmpty(vq.getShipvia()) ? "" : vq.getShipvia());
                newAuditKey.put("shipvia", "Ship Via");
                oldvq.put("fob", StringUtil.isNullOrEmpty(vq.getFob()) ? "" : vq.getFob());
                newAuditKey.put("fob", "FOB");
                oldvq.put("AuditDuedate", vq.getDueDate() != null ? df.format(vq.getDueDate()) : "");
                newAuditKey.put("AuditDuedate", "Due Date");
                oldvq.put("AuditShipDate", vq.getShipdate() != null ? df.format(vq.getShipdate()) : "");
                newAuditKey.put("AuditShipDate", "Ship Date");
                oldvq.put("AuditOrderDate", vq.getQuotationDate()!= null ? df.format(vq.getQuotationDate()) : "");
                newAuditKey.put("AuditOrderDate", "Vendor Quotation Date");
                oldvq.put("AuditValiddate", vq.getValiddate() != null ? df.format(vq.getValiddate()) : "");
                newAuditKey.put("AuditValiddate", "Valid Till");
            }
            
            KwlReturnObject debittermresult = accountingHandlerDAOobj.getObject(Term.class.getName(), paramJobj.optString("termid"));
            Term term = (Term) debittermresult.getEntityList().get(0);
            qDataMap.put(Constants.DebitTermName, term.getTermname());//Debit Term Name
            KwlReturnObject newcurrencyreturnobj = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString(Constants.currencyKey));
            KWLCurrency newcurrencyobj = (KWLCurrency) newcurrencyreturnobj.getEntityList().get(0);
            qDataMap.put(Constants.CurrencyName, newcurrencyobj.getName());//Currencey name
            KwlReturnObject venobretrurnlist = accountingHandlerDAOobj.getObject(Vendor.class.getName(), paramJobj.optString("vendor"));
            Vendor newvendor = (Vendor) venobretrurnlist.getEntityList().get(0);
            qDataMap.put(Constants.VendorName, newvendor.getName());//Vendor Name

        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
 }
    public HashSet saveQuotationRows(JSONObject paramJobj, VendorQuotation quotation, String companyid) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        HashSet rows = new HashSet();
        try {
            boolean includeProductTax = StringUtil.isNullOrEmpty(paramJobj.optString("includeprotax",null)) ? false : Boolean.parseBoolean(paramJobj.getString("includeprotax"));
            boolean isFixedAsset = (StringUtil.isNullOrEmpty(paramJobj.optString("isFixedAsset",null))) ? false:Boolean.parseBoolean(paramJobj.getString("isFixedAsset"));
            boolean isQuotationFromPR = false;
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            Country country = extraCompanyPreferences.getCompany().getCountry();
            String userid = paramJobj.getString(Constants.useridKey);
            DateFormat df = authHandler.getDateOnlyFormat();
            JSONArray jArr = new JSONArray(paramJobj.optString("detail"));
            Set<String> productNameRCMNotActivate = new HashSet<String>();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                
                if(jobj.has("srno")) {
                    qdDataMap.put("srno", jobj.getInt("srno"));
                }
                
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("vendorquotationid", quotation.getID());
                qdDataMap.put("productid", jobj.getString("productid"));
                if (country != null && Constants.INDIA_COUNTRYID.equals(country.getID()) && quotation.isGtaapplicable()) {
                    KwlReturnObject prdresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.optString("productid"));
                    Product product = (Product) prdresult.getEntityList().get(0);
                    if (!paramJobj.optBoolean("isUnRegisteredDealer", false) && paramJobj.optBoolean("GTAApplicable", false) && product != null && !product.isRcmApplicable()) {
                          productNameRCMNotActivate.add(product.getName());
                       // throw new AccountingException(messageSource.getMessage("acc.common.rcmforproductnotactivated.cannotsave.vendorquotation", new Object[]{product.getName()},Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
                qdDataMap.put("rate", jobj.getDouble("rate"));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                if (jobj.has("priceSource")) {
                    qdDataMap.put("priceSource", !StringUtil.isNullOrEmpty(jobj.optString("priceSource")) ? StringUtil.DecodeText(jobj.optString("priceSource")) : "");
                }
                if (jobj.has("pricingbandmasterid")) {
                    qdDataMap.put("pricingbandmasterid", !StringUtil.isNullOrEmpty(jobj.optString("pricingbandmasterid")) ? StringUtil.DecodeText(jobj.optString("pricingbandmasterid")) : "");
                }
                if(jobj.has("rateIncludingGst")) {
                    qdDataMap.put("rateIncludingGst",jobj.optDouble("rateIncludingGst",0));
                }
                qdDataMap.put("quantity", jobj.getDouble("quantity"));
                if (jobj.has("uomid")) {
                    qdDataMap.put("uomid", jobj.getString("uomid"));
                }
                if (jobj.has("baseuomquantity") && jobj.get("baseuomquantity") != null) {
                    qdDataMap.put("baseuomquantity", jobj.getDouble("baseuomquantity"));
                    qdDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                } else {
                    qdDataMap.put("baseuomquantity", jobj.getDouble("quantity"));
                    qdDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                }
                qdDataMap.put("remark", jobj.optString("remark"));
                if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {  //This is sats specific code  
                    if (jobj.has("dependentType")) {
                        qdDataMap.put("dependentType", StringUtil.isNullOrEmpty(jobj.getString("dependentType")) ? jobj.getString("dependentTypeNo") : jobj.getString("dependentType"));
                    }
                    if (jobj.has("inouttime")) {
                        qdDataMap.put("inouttime", !StringUtil.isNullOrEmpty(jobj.getString("inouttime")) ? jobj.getString("inouttime") : "");
                    }
                    if (jobj.has("showquantity")) {
                        qdDataMap.put("showquantity", !StringUtil.isNullOrEmpty(jobj.getString("showquantity")) ? jobj.getString("showquantity") : "");
                    }
                }
                //try {
                    qdDataMap.put("desc", StringUtil.DecodeText(jobj.optString("desc")));
                /*} catch (UnsupportedEncodingException ex) {
                    qdDataMap.put("desc", jobj.optString("desc"));
                }*/
                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore"))) {
                    qdDataMap.put("invstoreid", jobj.optString("invstore"));
                } else {
                    qdDataMap.put("invstoreid", "");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation"))) {
                    qdDataMap.put("invlocationid", jobj.optString("invlocation"));
                } else {
                    qdDataMap.put("invlocationid", "");
                }
                qdDataMap.put("discount", jobj.getDouble("prdiscount"));
                qdDataMap.put("discountispercent", jobj.getInt("discountispercent"));
                String rowtaxid = "";
                if (!StringUtil.isNullOrEmpty(jobj.optString("prtaxid", null)) && jobj.optString("prtaxid").equalsIgnoreCase("None")) {
                    rowtaxid = null;
                } else {
                    rowtaxid = jobj.optString("prtaxid",null); 
                }
                String linkmode = paramJobj.optString("fromLinkCombo");
                String linkNumber = paramJobj.optString("linkNumber");
                if (!StringUtil.isNullOrEmpty(linkmode) && !StringUtil.isNullOrEmpty(linkNumber) && (!StringUtil.isNullOrEmpty(jobj.getString("rowid")))) {
                    if ((linkmode.equalsIgnoreCase("Purchase Requisition") || linkmode.equalsIgnoreCase("Asset Purchase Requisition")) && !StringUtil.isNullOrEmpty(jobj.getString("rowid"))) {
                        isQuotationFromPR = true;
                        qdDataMap.put("PurchaseRequisitionDetailsID", jobj.getString("rowid"));
                    } else if (linkmode.equalsIgnoreCase("RFQ")){
                        qdDataMap.put("RFQDetailsID", jobj.getString("rowid"));
                    }
                }
                if (!StringUtil.isNullOrEmpty(rowtaxid) && includeProductTax) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    double rowtaxamount = StringUtil.getDouble(jobj.getString("taxamount"));
                    if (rowtax == null) {
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        qdDataMap.put("rowtaxid", rowtaxid);
                        qdDataMap.put("rowTaxAmount", rowtaxamount);
                        qdDataMap.put(Constants.isUserModifiedTaxAmount, jobj.optBoolean(Constants.isUserModifiedTaxAmount, false));
                    }
                }
                if (jobj.has("recTermAmount")) {
                    qdDataMap.put("recTermAmount", jobj.optDouble("recTermAmount", 0));
                }
                if (jobj.has("OtherTermNonTaxableAmount")) {
                    qdDataMap.put("OtherTermNonTaxableAmount", jobj.optDouble("OtherTermNonTaxableAmount", 0));
                }
                //  row.setTax(rowtax);

                KwlReturnObject result = accPurchaseOrderobj.saveQuotationDetails(qdDataMap);
                VendorQuotationDetail row = (VendorQuotationDetail) result.getEntityList().get(0);
                
                // Save Line-level CUSTOMFIELDS in Vendor Quotation
                String customfield = jobj.getString("customfield");
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> DOMap = new HashMap<String, Object>();
                    JSONArray jcustomarray = new JSONArray(customfield);

                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "VendorQuotationDetail");
                    customrequestParams.put("moduleprimarykey", "VendorQuotationDetailId");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", !isFixedAsset?Constants.Acc_Vendor_Quotation_ModuleId:Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    DOMap.put("id", row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_VendorQuotationDetails_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        DOMap.put("vqdetailscustomdataref", row.getID());
                        accPurchaseOrderobj.updateVQuotationDetailsCustomData(DOMap);
                    }
                }
                    if (extraCompanyPreferences.getLineLevelTermFlag()==1) {
                    /**
                     * Save GST History Customer/Vendor data.
                     */
                    jobj.put("detaildocid", row.getID());
                    jobj.put("moduleid", isFixedAsset?Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId:Constants.Acc_Vendor_Quotation_ModuleId);
                    fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jobj);
                }
                    // Add Custom fields details for Product
                if (!StringUtil.isNullOrEmpty(jobj.optString("productcustomfield", ""))) {
                    JSONArray jcustomarray = new JSONArray(jobj.optString("productcustomfield", "[]"));
                    HashMap<String, Object> quotationMap = new HashMap<String, Object>();
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "VqProductCustomData");
                    customrequestParams.put("moduleprimarykey", "VqDetailID");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    quotationMap.put("id", row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_VQDetail_Productcustom_data_classpath);
                    /*
                     * Rich Text Area is put in json if User have not selected any data for this field. ERP-ERP-37624
                     */
                    customrequestParams.put("productIdForRichRext", row.getProduct().getID());                    
                    fieldDataManagercntrl.setRichTextAreaForProduct(customrequestParams);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        quotationMap.put("vqdetailscustomdataref", row.getID());
                        accPurchaseOrderobj.updateVQuotationDetailsProductCustomData(quotationMap);
                    }
                }

                //  Indian Details Valuation Type -- start   
                KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) companyObj.getEntityList().get(0);
                if (company.getCountry() != null && (Constants.indian_country_id) == Integer.parseInt(company.getCountry().getID())) {
                    if (jobj.has("productMRP") && !StringUtil.isNullOrEmpty(jobj.getString("productMRP"))) {
                        row.setMrpIndia(jobj.getDouble("productMRP"));
                    }
                    if (jobj.has("valuationType") && !StringUtil.isNullOrEmpty(jobj.getString("valuationType"))) { // Excise Details
                        row.setExciseValuationType(jobj.getString("valuationType"));
                        if ((Constants.QUENTITY).equals(jobj.getString("valuationType"))) {
                            if (jobj.has("reortingUOMExcise") && !StringUtil.isNullOrEmpty(jobj.getString("reortingUOMExcise"))) {
                                UnitOfMeasure reportingUom = null;
                                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), jobj.getString("reortingUOMExcise"));
                                reportingUom = (UnitOfMeasure) custresult.getEntityList().get(0);
                                row.setReportingUOMExcise(reportingUom);
                            }
                            if (jobj.has("reortingUOMSchemaExcise") && !StringUtil.isNullOrEmpty(jobj.getString("reortingUOMSchemaExcise"))) {
                                UOMschemaType reportingUom = null;
                                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UOMschemaType.class.getName(), jobj.getString("reortingUOMSchemaExcise"));
                                reportingUom = (UOMschemaType) custresult.getEntityList().get(0);
                                row.setReportingSchemaTypeExcise(reportingUom);
                            }
                        }
                    }
                    if (jobj.has("valuationTypeVAT") && !StringUtil.isNullOrEmpty(jobj.getString("valuationTypeVAT"))) { // VAT Details
                        row.setVatValuationType(jobj.getString("valuationTypeVAT"));
                        if ((Constants.QUENTITY).equals(jobj.getString("valuationTypeVAT"))) {
                            if (jobj.has("reportingUOMVAT") && !StringUtil.isNullOrEmpty(jobj.getString("reportingUOMVAT"))) {
                                UnitOfMeasure reportingUom = null;
                                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), jobj.getString("reportingUOMVAT"));
                                reportingUom = (UnitOfMeasure) custresult.getEntityList().get(0);
                                row.setReportingUOMVAT(reportingUom);
                            }
                            if (jobj.has("reportingUOMSchemaVAT") && !StringUtil.isNullOrEmpty(jobj.getString("reportingUOMSchemaVAT"))) {
                                UOMschemaType reportingUom = null;
                                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UOMschemaType.class.getName(), jobj.getString("reportingUOMSchemaVAT"));
                                reportingUom = (UOMschemaType) custresult.getEntityList().get(0);
                                row.setReportingSchemaVAT(reportingUom);
                            }
                        }
                    }
                }
                // Indian Details Valuation Type -- End     
                
                rows.add(row);
                
                if (isFixedAsset) {
                    if (jobj.has("assetDetails") && jobj.getString("assetDetails") != null) {
                        String assetDetails = jobj.getString("assetDetails");
                        if (!StringUtil.isNullOrEmpty(assetDetails)) {
                            Set<PurchaseRequisitionAssetDetails> assetDetailsSet = savePurchaseRequisitionAssetDetails(paramJobj, jobj.getString("productid"), assetDetails, false, isQuotationFromPR, false);
                            Set<AssetPurchaseRequisitionDetailMapping> assetInvoiceDetailMappings = saveAssetPurchaseRequisitionDetailMapping(row.getID(), assetDetailsSet, companyid, Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
                        }
                    }
                }
                if (extraCompanyPreferences.getLineLevelTermFlag()==1 && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                    JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                    for (int j = 0; j < termsArray.length(); j++) {
                        HashMap<String, Object> vendorQDetailsTermsMap = new HashMap<String, Object>();
                        JSONObject termObject = termsArray.getJSONObject(j);
                        if (termObject.has("termid")) {
                            vendorQDetailsTermsMap.put("term", termObject.get("termid"));
                        }
                        if (termObject.has("termamount")) {
                            vendorQDetailsTermsMap.put("termamount", termObject.get("termamount"));
                        }
                        if (termObject.has("termpercentage")) {
                            vendorQDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                        }
                        if (termObject.has("purchasevalueorsalevalue")) {
                            vendorQDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                        }
                        if (termObject.has("deductionorabatementpercent")) {
                            vendorQDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                        }
                        if (termObject.has("assessablevalue")) {
                            vendorQDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                        }
                        if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                            vendorQDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                            if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                if(termObject.getInt("taxtype")==0){ // If Flat
                                    vendorQDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                } else { // Else Percentage
                                    vendorQDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                }
                            }
                        }
                        /**
                         * ERP-32829 
                         */
                        vendorQDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                        vendorQDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                        if (termObject.has("id")) {
                            vendorQDetailsTermsMap.put("id", termObject.get("id"));
                        }
                        vendorQDetailsTermsMap.put("vendorquotationdetails", row.getID());
                        vendorQDetailsTermsMap.put("userid",userid );
                        vendorQDetailsTermsMap.put("product", termObject.get("productid"));
                        vendorQDetailsTermsMap.put("createdOn", new Date());
                        
                        accPurchaseOrderobj.saveVendorQuotationDetailsTermMap(vendorQDetailsTermsMap);
                    }
                }
            }
            if (country != null && Constants.INDIA_COUNTRYID.equals(country.getID()) && quotation.isGtaapplicable()) {
                if (!paramJobj.optBoolean("isUnRegisteredDealer", false) && paramJobj.optBoolean("GTAApplicable", false) && !productNameRCMNotActivate.isEmpty()) {
                   throw new AccountingException(messageSource.getMessage("acc.common.rcmforproductnotactivated.cannotsave.vendorquotation", new Object[]{StringUtils.join(productNameRCMNotActivate, ", ")},Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveQuotationRows : " + ex.getMessage(), ex);
        }
        return rows;
    }
    public void saveQuotationVersion(JSONObject paramJobj, String quotationid) throws ServiceException, AccountingException {
        try {
            HashMap<String, Object> qDataMap = new HashMap<String, Object>();
            KwlReturnObject quotaionres = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), quotationid);
            VendorQuotation quotation = (VendorQuotation) quotaionres.getEntityList().get(0);
            if (quotation.getSeqformat() != null) {
                qDataMap.put(Constants.SEQFORMAT, quotation.getSeqformat().getID());
            }
            qDataMap.put(Constants.SEQNUMBER, quotation.getSeqnumber());
            qDataMap.put(Constants.DATEPREFIX, quotation.getDatePreffixValue());
            qDataMap.put(Constants.DATESUFFIX, quotation.getDateSuffixValue());
            if (quotation.getBillingShippingAddresses() != null) {
                qDataMap.put("billshipAddressid", quotation.getBillingShippingAddresses().getID());
            }
            qDataMap.put("externalCurrencyRate", quotation.getExternalCurrencyRate());
            qDataMap.put("entrynumber", quotation.getQuotationNumber());
            qDataMap.put("autogenerated", quotation.isAutoGenerated());
            qDataMap.put("memo", quotation.getMemo());
            qDataMap.put("posttext", quotation.getPostText());
            if (quotation.getVendor() != null) {
                qDataMap.put("vendorid", quotation.getVendor().getID());
            }
            qDataMap.put("orderdate", quotation.getQuotationDate());
            qDataMap.put("duedate", quotation.getDueDate());
            qDataMap.put("perDiscount", quotation.isPerDiscount());
            qDataMap.put("discount", quotation.getDiscount());
            qDataMap.put("gstIncluded", quotation.isGstIncluded());
            qDataMap.put("shipdate", quotation.getShipdate());
            qDataMap.put("validdate", quotation.getValiddate());
            qDataMap.put("shipvia", quotation.getShipvia());
            qDataMap.put("fob", quotation.getFob());
            if (quotation.getCurrency() != null) {
                qDataMap.put("currencyid", quotation.getCurrency().getCurrencyID());
            }
            qDataMap.put("isfavourite", quotation.isFavourite());
            qDataMap.put("shipaddress", quotation.getShipTo());
            qDataMap.put("billto", quotation.getBillTo());
            qDataMap.put("istemplate", quotation.getIstemplate());
            qDataMap.put("agent", quotation.getMasteragent() != null ? quotation.getMasteragent().getID() : null);
            qDataMap.put("companyid", quotation.getCompany().getCompanyID());
            qDataMap.put("createdby", quotation.getCreatedby().getUserID());
            qDataMap.put("modifiedby", quotation.getModifiedby().getUserID());
            qDataMap.put("createdon", quotation.getCreatedon());
            qDataMap.put("updatedon", quotation.getUpdatedon());
            qDataMap.put("shipLength", quotation.getShiplength());
            qDataMap.put("invoicetype", quotation.getInvoicetype());
            qDataMap.put("quotationID", quotation.getID());
            qDataMap.put("isLinkedFromReplacementNumber", quotation.getLinkflag());
            qDataMap.put("taxid", quotation.getTax() == null ? null : quotation.getTax().getID());     // Put taxid as null if the CQ doesnt have any total tax included. (To avoid problem while editing CQ)
            String version = "VN00000";
            KwlReturnObject socnt = accPurchaseOrderobj.getQuotationVersionCount(quotation.getID(), quotation.getCompany().getCompanyID());
            int count = socnt.getRecordTotalCount();
            qDataMap.put("version", version + (count + 1));
            KwlReturnObject soresult = accPurchaseOrderobj.saveQuotationVersion(qDataMap);
            VendorQuotationVersion quotationVersion = (VendorQuotationVersion) soresult.getEntityList().get(0);
            qDataMap.put("id", quotationVersion.getID());
            HashSet qoversiondetails = saveQuotationVersionRows(paramJobj, quotation, quotation.getCompany().getCompanyID(), quotationVersion);
            quotationVersion.setRows(qoversiondetails);
            String customfield = paramJobj.optString("customfield");
            JSONArray jcustomarray = new JSONArray(customfield);
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", Constants.Acc_Quotation_modulename);
            customrequestParams.put("moduleprimarykey", "QuotationId");
            customrequestParams.put("modulerecid", quotationVersion.getID());
            customrequestParams.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
            customrequestParams.put("companyid", quotationVersion.getCompany().getCompanyID());
            customrequestParams.put("customdataclasspath", Constants.Acc_VendorQuotationVersion_custom_data_classpath);
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                qDataMap.put("accquotationcustomdataref", quotationVersion.getID());
                KwlReturnObject accresult = accPurchaseOrderobj.updateQuotationVersionCustomData(qDataMap);
            }

        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public HashSet saveQuotationVersionRows(JSONObject paramJobj, VendorQuotation quotation, String companyid, VendorQuotationVersion quotationVersion) throws ServiceException, AccountingException, SessionExpiredException {
        HashSet rows = new HashSet();
        HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("vendorquotation.ID");
        order_by.add("srno");
        order_type.add("asc");
        soRequestParams.put("filter_names", filter_names);
        soRequestParams.put("filter_params", filter_params);
        soRequestParams.put("order_by", order_by);
        soRequestParams.put("order_type", order_type);
        filter_params.clear();
        filter_params.add(quotation.getID());
        KwlReturnObject podresult = accPurchaseOrderobj.getQuotationDetails(soRequestParams);
        Iterator itr = podresult.getEntityList().iterator();
        int i = 0;
        while (itr.hasNext()) {
            try {
                VendorQuotationDetail row = (VendorQuotationDetail) itr.next();
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put("srno", i + 1);
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("soid", quotationVersion.getID());
                qdDataMap.put("productid", row.getProduct().getID());
                qdDataMap.put("rate", row.getRate());//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                qdDataMap.put("quantity", row.getQuantity());
                if (row.getUom() != null) {
                    qdDataMap.put("uomid", row.getUom().getID());
                }
                qdDataMap.put("baseuomquantity", row.getBaseuomquantity());
                qdDataMap.put("baseuomrate", row.getBaseuomrate());
                qdDataMap.put("remark", row.getRemark());
                qdDataMap.put("dependentType", row.getDependentType());
                qdDataMap.put("inouttime", row.getInouttime());
                qdDataMap.put("showquantity", row.getShowquantity());
                qdDataMap.put("desc", row.getDescription());
                qdDataMap.put("invstoreid", row.getInvstoreid());
                qdDataMap.put("invlocationid", row.getInvlocid());
                qdDataMap.put("discount", row.getDiscount());
                qdDataMap.put("discountispercent", row.getDiscountispercent());
                if (row.getTax() != null) {
                    qdDataMap.put("rowtaxid", row.getTax().getID());
                }
                qdDataMap.put("rowTaxAmount", row.getRowTaxAmount());
                KwlReturnObject result = accPurchaseOrderobj.saveQuotationVersionDetails(qdDataMap);
                VendorQuotationVersionDetail qvd = (VendorQuotationVersionDetail) result.getEntityList().get(0);
                JSONArray jArr = new JSONArray(paramJobj.optString("detail"));
                JSONObject jobj = jArr.optJSONObject(0);
                String customfield = jobj.getString("customfield");
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> DOMap = new HashMap<String, Object>();
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "QuotationVersionDetail");
                customrequestParams.put("moduleprimarykey", "QuotationDetailId");
                customrequestParams.put("modulerecid", qvd.getID());
                customrequestParams.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                customrequestParams.put("companyid", companyid);
                DOMap.put("id", qvd.getID());
                customrequestParams.put("customdataclasspath", Constants.Acc_VendorQuotationVersionDetails_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    DOMap.put("qdetailscustomdataref", qvd.getID());
                    accPurchaseOrderobj.updateQuotationVersionDetailsCustomData(DOMap);
                }
                rows.add(qvd);
                i++;
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return rows;
    }
   
    public void addDefaultCustomerShippingAddress(Map<String, Object> dataMap,Map<String, Object> inputMap) {

        if (inputMap.containsKey(Constants.SHIPPING_ADDRESS)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_ADDRESS, inputMap.get(Constants.SHIPPING_ADDRESS));
        }
        if (inputMap.containsKey(Constants.SHIPPING_STATE)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_STATE, inputMap.get(Constants.SHIPPING_STATE));
        }
        if (inputMap.containsKey(Constants.SHIPPING_COUNTRY)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_COUNTRY, inputMap.get(Constants.SHIPPING_COUNTRY));
        }
        if (inputMap.containsKey(Constants.SHIPPING_CITY)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_CITY, inputMap.get(Constants.SHIPPING_CITY));
        }
        if (inputMap.containsKey(Constants.SHIPPING_EMAIL)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_EMAIL, inputMap.get(Constants.SHIPPING_EMAIL));
        }
        if (inputMap.containsKey(Constants.SHIPPING_FAX)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_FAX, inputMap.get(Constants.SHIPPING_FAX));
        }
        if (inputMap.containsKey(Constants.SHIPPING_POSTAL)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_POSTAL, inputMap.get(Constants.SHIPPING_POSTAL));
        }
        if (inputMap.containsKey(Constants.SHIPPING_MOBILE)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_MOBILE, inputMap.get(Constants.SHIPPING_MOBILE));
        }
        if (inputMap.containsKey(Constants.SHIPPING_PHONE)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_PHONE, inputMap.get(Constants.SHIPPING_PHONE));
        }
        if (inputMap.containsKey(Constants.SHIPPING_RECIPIENT_NAME)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME, inputMap.get(Constants.SHIPPING_RECIPIENT_NAME));
        }
        if (inputMap.containsKey(Constants.SHIPPING_CONTACT_PERSON_NUMBER)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER, inputMap.get(Constants.SHIPPING_CONTACT_PERSON_NUMBER));
        }
        if (inputMap.containsKey(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION, inputMap.get(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION));
        }
        if (inputMap.containsKey(Constants.SHIPPING_CONTACT_PERSON)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON, inputMap.get(Constants.SHIPPING_CONTACT_PERSON));
        }
        if (inputMap.containsKey(Constants.SHIPPING_WEBSITE)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_WEBSITE, inputMap.get(Constants.SHIPPING_WEBSITE));
        }
        if (inputMap.containsKey(Constants.SHIPPING_ADDRESS_TYPE)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE, inputMap.get(Constants.SHIPPING_ADDRESS_TYPE));
        }
        if (inputMap.containsKey(Constants.SHIPPING_ROUTE)) {
            dataMap.put(Constants.CUSTOMER_SHIPPING_ROUTE, inputMap.get(Constants.SHIPPING_ROUTE));
        }

    }
    
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class, SessionExpiredException.class})
    public String deletePurchaseRequisition(JSONObject jobj, JSONObject requestJobj, String linkedTransactions, String companyid, boolean isFixedAsset) throws ServiceException {
        String actionId = "64", actionMsg = Constants.deleted;
        String poid = StringUtil.DecodeText(jobj.optString(Constants.billid));
        PurchaseRequisition requisition = (PurchaseRequisition) kwlCommonTablesDAOObj.getClassObject(PurchaseRequisition.class.getName(), poid);
        boolean isLinked = accPurchaseOrderobj.checkIfRequisitionLinkedInVendorQuotation(poid, companyid);
        if (isLinked) {
            linkedTransactions += requisition.getPrNumber() + " ,";
            return linkedTransactions;
        }
        isLinked = accPurchaseOrderobj.checkIfRequisitionLinkedInPurchaseOrder(poid, companyid);
        if (isLinked) {
            linkedTransactions += requisition.getPrNumber() + " ,";
            return linkedTransactions;
        }
        //  Check if purchase requisition is linked to any RFQ
        KwlReturnObject checkForRFQ = accPurchaseOrderobj.getRFQLinkedWithPR(poid, companyid);
        List rfqList = checkForRFQ.getEntityList();
        if (rfqList != null && rfqList.size() > 0) {
            linkedTransactions += requisition.getPrNumber() + " ,";
            return linkedTransactions;
        }
        String audtmsg = "";
        if (isFixedAsset) {
            audtmsg = " " + messageSource.getMessage("acc.up.10", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) + " ";
        } else {
            audtmsg = " ";
        }
        accPurchaseOrderobj.deletePurchaseRequisition(poid, companyid);
        Map<String, Object> auditParamsMap = new HashMap();
        auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
        auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
        auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
        auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
        auditTrailObj.insertAuditLog(actionId, "User " + requestJobj.optString(Constants.userfullname) + " has " + actionMsg + audtmsg + "Purchase Requisition " + requisition.getPrNumber(), auditParamsMap, requisition.getID());

        return linkedTransactions;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class, SessionExpiredException.class})
    public String deletePurchaseRequisitionPermanent(JSONObject jobj, JSONObject requestJobj, String linkedTransactions, String companyid, boolean isFixedAsset) throws ServiceException {
        String actionId = "64", actionMsg = "deleted";
        String reqid = StringUtil.DecodeText(jobj.optString(Constants.billid));

        KwlReturnObject res = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), reqid);
        PurchaseRequisition purchaseRequisition = (PurchaseRequisition) res.getEntityList().get(0);

        String reqno = purchaseRequisition.getPrNumber();//jobj.getString("billno");
        boolean isLinked = accPurchaseOrderobj.checkIfRequisitionLinkedInVendorQuotation(reqid, companyid);
        if (isLinked) {
            linkedTransactions += reqno + " ,";
            return linkedTransactions;
        }
        isLinked = accPurchaseOrderobj.checkIfRequisitionLinkedInPurchaseOrder(reqid, companyid);
        if (isLinked) {
            linkedTransactions += purchaseRequisition.getPrNumber() + " ,";
            return linkedTransactions;
        }
        //  Check if purchase requisition is linked to any RFQ
        KwlReturnObject checkForRFQ = accPurchaseOrderobj.getRFQLinkedWithPR(reqid, companyid);
        List rfqList = checkForRFQ.getEntityList();
        if (rfqList != null && rfqList.size() > 0) {
            linkedTransactions += purchaseRequisition.getPrNumber() + " ,";
            return linkedTransactions;
        }
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("reqid", reqid);
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put("reqno", reqno);
        requestParams.put(Constants.isFixedAsset, isFixedAsset);
        String audtmsg = "";
        if (isFixedAsset) {
            audtmsg = " " + messageSource.getMessage("acc.up.10", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) + " ";
        } else {
            audtmsg = " ";
        }
        accPurchaseOrderobj.deletePurchaseRequisitionPermanent(requestParams);

        Map<String, Object> auditParamsMap = new HashMap();
        auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
        auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
        auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
        auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
        auditTrailObj.insertAuditLog(actionId, "User " + requestJobj.optString(Constants.userfullname) + " has " + actionMsg + audtmsg + "Purchase Requisition Permanently " + reqno, auditParamsMap, reqid);

        return linkedTransactions;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class, SessionExpiredException.class})
    public String deleteQuotation(JSONObject jobj, JSONObject requestJobj, String linkedTransaction, String companyid, String audtmsg) throws ServiceException {
        String qid = StringUtil.DecodeText(jobj.optString(Constants.billid));

        KwlReturnObject res = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), qid);

        VendorQuotation quotation = (VendorQuotation) res.getEntityList().get(0);

        String qno = quotation.getQuotationNumber();//jobj.getString("billno");
        KwlReturnObject result = accPurchaseOrderobj.getVQforinvoice(qid, companyid);
        int count1 = result.getRecordTotalCount();   ////issue 32010 [Delete PO/SO/VQ/CQ]such PO/SO/VQ/CQ should not get deleted if it is used in some VI/CI. system should get alert "Selected VQ/PO/CQ/SO are used in transaction, so cannot be deleted" 
        KwlReturnObject result1 = accPurchaseOrderobj.getVQforPO(qid, companyid);
        int count2 = result1.getRecordTotalCount();
        KwlReturnObject vqresult = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), qid);
        VendorQuotation vendorQuotation = (VendorQuotation) vqresult.getEntityList().get(0);
        Set row = vendorQuotation.getRows();
        Iterator itr = row.iterator();
        String vqdids = "";
        while (itr.hasNext()) {
            VendorQuotationDetail Vqd = (VendorQuotationDetail) itr.next();
            vqdids += "'" + Vqd.getID() + "',";
        }
        if (vqdids.length() > 1) {
            vqdids = vqdids.substring(0, vqdids.length() - 1);
        }
        KwlReturnObject result2 = accPurchaseOrderobj.getVQforCQ(vqdids, companyid);
        int count3 = result2.getRecordTotalCount();
        if (count1 > 0) {
            linkedTransaction += qno + ", ";
            return linkedTransaction;
        } else if (count3 > 0) {
            linkedTransaction += qno + ", ";
            return linkedTransaction;
        } else if (count2 > 0) {
            linkedTransaction += qno + ", ";
            return linkedTransaction;
        } else {
            accPurchaseOrderobj.deleteQuotation(qid, companyid);

            Map<String, Object> auditParamsMap = new HashMap();
            auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
            auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
            auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
            auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
            auditTrailObj.insertAuditLog(AuditAction.Vendor_Quotation, " User " + requestJobj.optString(Constants.userfullname) + " has deleted" + audtmsg + "Vendor Quotation " + qno, auditParamsMap, qid);
        }

        return linkedTransaction;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class, SessionExpiredException.class})
    public String deleteQuotationPermanent(JSONObject jobj, JSONObject requestJobj, String linkedTransaction, String companyid, boolean isFixedAsset) throws ServiceException, AccountingException {
        String qid = StringUtil.DecodeText(jobj.optString(Constants.billid));

        KwlReturnObject res = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), qid);
        VendorQuotation quotation = (VendorQuotation) res.getEntityList().get(0);
        String qno = quotation.getQuotationNumber();//jobj.getString("billno");

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("qid", qid);
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put("qno", qno);
        requestParams.put(Constants.isFixedAsset, isFixedAsset);
        requestParams.put("versionid", qid);

        KwlReturnObject result = accPurchaseOrderobj.getVQforinvoice(qid, companyid);
        int count1 = result.getRecordTotalCount();
        if (count1 > 0) {
            linkedTransaction += qno + ", ";
            return linkedTransaction;
        }
        KwlReturnObject result1 = accPurchaseOrderobj.getVQforPO(qid, companyid);
        int count2 = result1.getRecordTotalCount();
        if (count2 > 0) {
            linkedTransaction += qno + ", ";
            return linkedTransaction;
        }
        KwlReturnObject vqresult = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), qid);
        VendorQuotation vendorQuotation = (VendorQuotation) vqresult.getEntityList().get(0);
        Set row = vendorQuotation.getRows();
        Iterator itr = row.iterator();
        String vqdids = "";
        while (itr.hasNext()) {
            VendorQuotationDetail Vqd = (VendorQuotationDetail) itr.next();
            vqdids += "'" + Vqd.getID() + "',";
        }
        if (vqdids.length() > 1) {
            vqdids = vqdids.substring(0, vqdids.length() - 1);
        }
        KwlReturnObject result2 = accPurchaseOrderobj.getVQforCQ(vqdids, companyid);
        int count3 = result2.getRecordTotalCount();
        if (count3 > 0) {
            linkedTransaction += qno + ", ";
            return linkedTransaction;
        }
        if (!isFixedAsset) {
            KwlReturnObject result3 = accPurchaseOrderobj.getVersionQuotations(requestParams);  //for checking Vendor Quotation has any Version or not
            int count4 = result3.getRecordTotalCount();
            if (count4 > 0) {
                throw new AccountingException("Selected quotation(s) is having Version History. So it cannot be deleted.");
            }
        }
        String audtmsg = "";
        if (isFixedAsset) {
            audtmsg = " " + messageSource.getMessage("acc.up.10", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) + " ";
        } else {
            audtmsg = " ";
        }
        /*
         * deleting linking information of VQ while linked with
         * Purchase Requisition
         */
        accPurchaseOrderobj.deleteLinkingInformationOfVQ(requestParams);
        accPurchaseOrderobj.deleteQuotationsPermanent(requestParams);

        Map<String, Object> auditParamsMap = new HashMap();
        auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
        auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
        auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
        auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
        auditTrailObj.insertAuditLog(AuditAction.Vendor_Quotation, " User " + requestJobj.optString(Constants.userfullname) + " has deleted" + audtmsg + "Vendor Quotation Permanently " + qno, auditParamsMap, qid);

        return linkedTransaction;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class, SessionExpiredException.class})
    public String deleteQuotationVersion(JSONObject jobj, JSONObject requestJobj, String quotationVersions, String companyid) throws ServiceException, JSONException {
        if (StringUtil.isNullOrEmpty(jobj.getString(Constants.billid))) {
            quotationVersions += "Quotation Version ID is null or empty" + ", ";
            return quotationVersions;
        } else {
            String versionID = StringUtil.DecodeText(jobj.optString(Constants.billid));
            accPurchaseOrderobj.deleteQuotationVersion(versionID, companyid);

            Map<String, Object> auditParamsMap = new HashMap();
            auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
            auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
            auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
            auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
            auditTrailObj.insertAuditLog("74", " User " + requestJobj.optString(Constants.userfullname) + " has deleted a Vendor Quotation Version's " + versionID, auditParamsMap, versionID);
        }
        return quotationVersions;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class, SessionExpiredException.class})
    public String deleteQuotationVersionPermanent(JSONObject jobj, JSONObject requestJobj, String quotationVersion, String companyid) throws ServiceException, JSONException {
        String versionID = StringUtil.DecodeText(jobj.optString(Constants.billid));

        if (StringUtil.isNullOrEmpty(versionID)) {
            quotationVersion += "Quotation Version ID is null or empty" + ", ";
            return quotationVersion;
        }

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put("versionid", versionID);
        if (!StringUtil.isNullOrEmpty(versionID)) {
            accPurchaseOrderobj.deleteQuotationVersionsPermanent(requestParams);

            Map<String, Object> auditParamsMap = new HashMap();
            auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
            auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
            auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
            auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
            auditTrailObj.insertAuditLog("74", " User " + requestJobj.optString(Constants.userfullname) + " has deleted a Vendor Quotation Version Permanently " + versionID, auditParamsMap, versionID);
        }

        return quotationVersion;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class, SessionExpiredException.class})
    public String deleteRFQ(JSONObject jobj, JSONObject requestJobj, String linkedTransactions, String companyid) throws ServiceException {
        String poid = StringUtil.DecodeText(jobj.optString(Constants.billid));
        RequestForQuotation RFQ = (RequestForQuotation) kwlCommonTablesDAOObj.getClassObject(RequestForQuotation.class.getName(), poid);
        boolean isLinked = accPurchaseOrderobj.checkIfRFQLinkedInVendorQuotation(poid);
        if (isLinked) {
            linkedTransactions += RFQ.getRfqNumber() + ",";
            return linkedTransactions;
        }
        accPurchaseOrderobj.deleteRFQ(poid, companyid);

        Map<String, Object> auditParamsMap = new HashMap();
        auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
        auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
        auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
        auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
        auditTrailObj.insertAuditLog(AuditAction.RFQ_DELETED, "User " + requestJobj.optString(Constants.userfullname) + " has deleted RFQ " + RFQ.getRfqNumber(), auditParamsMap, RFQ.getID());

        return linkedTransactions;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class, SessionExpiredException.class})
    public String deleteRFQPermanent(JSONObject jobj, JSONObject requestJobj, String linkedTransactions, String companyid, boolean isFixedAsset) throws ServiceException {
        String rfqid = StringUtil.DecodeText(jobj.optString(Constants.billid));

        RequestForQuotation RFQ = (RequestForQuotation) kwlCommonTablesDAOObj.getClassObject(RequestForQuotation.class.getName(), rfqid);
        boolean isLinked = accPurchaseOrderobj.checkIfRFQLinkedInVendorQuotation(rfqid);
        if (isLinked) {
            linkedTransactions += RFQ.getRfqNumber() + ",";
            return linkedTransactions;
        }
        String rfqno = RFQ.getRfqNumber();//jobj.getString("billno");

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("rfqid", rfqid);
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put("rfqno", rfqno);
        requestParams.put(Constants.isFixedAsset, isFixedAsset);
        if (!StringUtil.isNullOrEmpty(rfqid)) {
            accPurchaseOrderobj.deleteLinkingInformationOfRFQ(requestParams);
            accPurchaseOrderobj.deleteRFQPermanent(requestParams);

            Map<String, Object> auditParamsMap = new HashMap();
            auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
            auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
            auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
            auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
            auditTrailObj.insertAuditLog(AuditAction.RFQ_DELETED_PERMANENT, "User " + requestJobj.optString(Constants.userfullname) + " has deleted RFQ " + RFQ.getRfqNumber() + " permanently ", auditParamsMap, RFQ.getID());
        }

        return linkedTransactions;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class, SessionExpiredException.class})
    public String deletePurchaseOrder(JSONObject jobj, JSONObject requestJobj, String linkedTransaction, String companyid, String modulename) throws ServiceException {
        try{
        String poid = StringUtil.DecodeText(jobj.optString(Constants.billid));

        KwlReturnObject res = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), poid);
        PurchaseOrder purchaseOrder = (PurchaseOrder) res.getEntityList().get(0);

        String pono = purchaseOrder.getPurchaseOrderNumber();//jobj.getString("billno");
        KwlReturnObject result = accPurchaseOrderobj.getPOforinvoice(poid, companyid);
        int count1 = result.getRecordTotalCount();
        if (count1 > 0) {
            linkedTransaction += pono + ", ";
            return linkedTransaction;
        }
        KwlReturnObject resultd = accPurchaseOrderobj.getGROforinvoice(poid, companyid);  //for cheching SO is used in DO or not
        int count2 = resultd.getRecordTotalCount();
        if (count2 > 0) {
            linkedTransaction += pono + ", ";
            return linkedTransaction;
        }
        KwlReturnObject resultp = accPurchaseOrderobj.getSOforPO(poid, companyid);  //for checking SO is used in PO or not
        int count3 = resultp.getRecordTotalCount();
        if (count3 > 0) {
            linkedTransaction += pono + ", ";
            return linkedTransaction;
        }

        // check the pushed PO in delivery Planner
        result = accPurchaseOrderobj.getPurchaseOrderFromDeliveryPlanner(poid, companyid);
        int count4 = resultp.getRecordTotalCount();
        if (count4 > 0) {
            linkedTransaction += pono + ", ";
            return linkedTransaction;
        }
        /**
         * Check Whether Work Order Used in Stock Transfer or not
         */
        if (purchaseOrder.isIsJobWorkOutOrder()) {
            result = accPurchaseOrderobj.getStockTransferFromJobWorkOutOrder(poid, companyid);
            int count5 = result.getRecordTotalCount();
            if (count5 > 0) {
                linkedTransaction += pono + ", ";
                return linkedTransaction;
            }
        }
        
        JSONObject reqParamJobj = new JSONObject();
        reqParamJobj.put("docid", poid);
        KwlReturnObject soReturnObj = accPurchaseOrderobj.getLinkedSO(reqParamJobj);
        List soList = soReturnObj.getEntityList();
        if (soReturnObj.getRecordTotalCount() > 0 && soReturnObj.getEntityList().get(0) != null) {
            for (int poCount = 0; poCount < soReturnObj.getRecordTotalCount(); poCount++) {
                HashMap<String, Object> reqParam = new HashMap<>();
                reqParam.put("salesOrderID", (String) soList.get(poCount));
                reqParam.put("status", "closed");
                accSalesOrderDAOobj.saveSalesOrderStatusForPO(reqParam);
            }
        }
        
        String actionMsg = Constants.deleted;
        boolean isReject = StringUtil.isNullOrEmpty(requestJobj.optString("isReject")) ? false : Boolean.parseBoolean(requestJobj.optString("isReject"));

        if (isReject == true) {
            actionMsg = "rejected";
        }

        accPurchaseOrderobj.deletePurchaseOrder(poid, companyid);

        Map<String, Object> auditParamsMap = new HashMap();
        auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
        auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
        auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
        auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
        auditTrailObj.insertAuditLog("86", " User " + requestJobj.optString(Constants.userfullname) + " has " + actionMsg + modulename + pono, auditParamsMap, poid);
        } catch (JSONException jex) {
            throw ServiceException.FAILURE("deletePurchaseOrder : " + jex.getMessage(), jex);
        }
        return linkedTransaction;
    }

    @Override
    public String deletePurchaseOrdersPermanent(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString(Constants.RES_data));
            String companyid = requestJobj.optString(Constants.companyKey);
            boolean isConsignment = false;
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.isConsignment))) {
                isConsignment = Boolean.parseBoolean(requestJobj.optString(Constants.isConsignment));
            }
            boolean isFixedAsset = false;
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.isFixedAsset))) {
                isFixedAsset = Boolean.parseBoolean(requestJobj.optString(Constants.isFixedAsset));
            }

            String modulename = "";
            if (isConsignment) {
                modulename = " " + messageSource.getMessage("acc.venconsignment.order", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) + " ";
            } else if (isFixedAsset) {
                modulename = " " + messageSource.getMessage("acc.field.assetPurchaseOrder", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) + " ";
            } else {
                modulename = " " + messageSource.getMessage("acc.dimension.module.10", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) + " ";
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.billid))) {
                    linkedTransaction = deletePurchaseOrderPermanent(jobj, requestJobj, linkedTransaction, companyid, isFixedAsset, modulename);
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
        return linkedTransaction;
    }
    
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class, SessionExpiredException.class})
    public String deletePurchaseOrderPermanent(JSONObject jobj, JSONObject requestJobj, String linkedTransaction, String companyid, boolean isFixedAsset, String modulename) throws JSONException,ServiceException {
        String poid = StringUtil.DecodeText(jobj.optString(Constants.billid));

        KwlReturnObject res = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), poid);
        PurchaseOrder purchaseOrder = (PurchaseOrder) res.getEntityList().get(0);

        String pono = purchaseOrder.getPurchaseOrderNumber();//jobj.getString("billno");

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("poid", poid);
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put("pono", pono);
        requestParams.put("isExpensePO", purchaseOrder.isIsExpenseType());
        requestParams.put(Constants.isFixedAsset, isFixedAsset);//SDP-8170
        if (!StringUtil.isNullOrEmpty(poid)) {
            KwlReturnObject result = null;
            if (purchaseOrder.isIsExpenseType()) {
                result = accPurchaseOrderobj.getExpensePOforinvoice(poid, companyid); //for cheching PO is used in invoice or not
            } else {
                result = accPurchaseOrderobj.getPOforinvoice(poid, companyid); //for cheching PO is used in invoice or not
            }

            int count1 = 0;
            if (result != null) {
                count1 = result.getRecordTotalCount();
            }
            if (count1 > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the Vendor Invoices(s). So it cannot be deleted.");
                linkedTransaction += pono + ", ";
                return linkedTransaction;
            }
            KwlReturnObject resultd = accPurchaseOrderobj.getGROforinvoice(poid, companyid);  //for cheching SO is used in DO or not
            int count2 = resultd.getRecordTotalCount();
            if (count2 > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the Goods Receipt Order(s). So it cannot be deleted.");
                linkedTransaction += pono + ", ";
                return linkedTransaction;
            }
            KwlReturnObject resultp = accPurchaseOrderobj.getSOforPO(poid, companyid);  //for checking SO is used in PO or not
            int count3 = resultp.getRecordTotalCount();
            if (count3 > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the purchase Order(s). So it cannot be deleted.");
                linkedTransaction += pono + ", ";
                return linkedTransaction;
            }

            // check the pushed PO in delivery Planner
            result = accPurchaseOrderobj.getPurchaseOrderFromDeliveryPlanner(poid, companyid);
            int count4 = resultp.getRecordTotalCount();
            if (count4 > 0) {
                linkedTransaction += pono + ", ";
                return linkedTransaction;
            }
            /**
             * Check Whether Work Order Used in Stock Transfer or not
             */
            if (purchaseOrder.isIsJobWorkOutOrder()) {
                result = accPurchaseOrderobj.getStockTransferFromJobWorkOutOrder(poid, companyid);
                int count5 = result.getRecordTotalCount();
                if (count5 > 0) {
                    linkedTransaction += pono + ", ";
                    return linkedTransaction;
                }
            }
            
            JSONObject reqParamJobj = new JSONObject();
            reqParamJobj.put("docid", poid);
            KwlReturnObject soReturnObj = accPurchaseOrderobj.getLinkedSO(reqParamJobj);
            List soList = soReturnObj.getEntityList();
            if (soReturnObj.getRecordTotalCount() > 0 && soReturnObj.getEntityList().get(0) != null) {
                for (int poCount = 0; poCount < soReturnObj.getRecordTotalCount(); poCount++) {
                    HashMap<String, Object> reqParam = new HashMap<>();
                    reqParam.put("salesOrderID", (String) soList.get(poCount));
                    reqParam.put("status", "closed");
                    accSalesOrderDAOobj.saveSalesOrderStatusForPO(reqParam);
                }
            }
            /*
             Updating Open PO Flag of Requisition while Deleting PO 
            
             i.e Releasing Requisition from PO  
            
             */
            Set<PurchaseOrderDetail> rows = purchaseOrder.getRows();
            Iterator itr = rows.iterator();
            while (itr.hasNext()) {
                PurchaseOrderDetail row = (PurchaseOrderDetail) itr.next();
                if (row.getPurchaseRequisitionDetailId() != null) {

                    KwlReturnObject curresult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), row.getPurchaseRequisitionDetailId());
                    PurchaseRequisitionDetail object = (PurchaseRequisitionDetail) curresult.getEntityList().get(0);

                    /*------ Updating isopeninpo flag while unlinking PO from requisition--------- */
                    requestParams.put("requisitionDetailId", row.getPurchaseRequisitionDetailId());
                    requestParams.put("purchaseOrderDetailId", row.getID());
                    
                    double resultQuantity = getQuantityStatusOfRequisition(requestParams);
                   
                    /*  */
                    if (object != null) {
                        if (object.getQuantity() > resultQuantity) {
                            /*------ Updating openinpo flag of Requisition to true-----*/
                            object.getPurchaserequisition().setIsOpenInPO(true);

                        }

                        /*------Update Balance Quantity of Requisition Detail------------ */
                        object.setBalanceqty(object.getBalanceqty()+row.getQuantity());
                    }
                }
            }
            
            /*  Deleting Version Details - Start */
            accPurchaseOrderobj.deletePurchaseOrderVersoning(requestParams);
            /* Deleting Version Details - End */
            
            accPurchaseOrderobj.deleteLinkingInformationOfPO(requestParams);//deleting linking information of PO 
            accPurchaseOrderobj.deletePurchaseOrdersPermanent(requestParams);
            
            Map<String, Object> auditParamsMap = new HashMap();
            auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
            auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
            auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
            auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
            auditTrailObj.insertAuditLog("86", " User " + requestJobj.optString(Constants.userfullname) + " has deleted" + modulename + " Permanently " + pono, auditParamsMap, poid);
        }

        return linkedTransaction;
    }
    
    /*------- Called in case of Unlink Or delete PO, linked with Requisition-------- */
    public double getQuantityStatusOfRequisition(HashMap reqHashMap) throws ServiceException {
        double resultQuantity = 0;
        HashMap requestParams = new HashMap();
        try {
            String requisitionDetailId = reqHashMap.containsKey("requisitionDetailId") ? reqHashMap.get("requisitionDetailId").toString() : "";
            String purchaseOrderDetailId = reqHashMap.containsKey("purchaseOrderDetailId") ? reqHashMap.get("purchaseOrderDetailId").toString() : "";

            requestParams.put("requisitionDetailId", requisitionDetailId);
            
            /*-------- Return total quantity of Requisition Detail Used in PO-------*/
            double totalQuantityOfRequisitionDetailUsedInPO = calCulateBalanceQtyOfRequisitionForPO(requestParams);

            requestParams.put("purchaseOrderDetailId", purchaseOrderDetailId);
            
            /*----Return total quantity of Requisition Detail Used in  particular Purchase Order Detail---------- */
            double quantityusedForParticularPODetail = calCulateBalanceQtyOfRequisitionForPO(requestParams);
            
            /*---- Returning total Used Quantity -particulear PO Detail quanity--- */
            resultQuantity = totalQuantityOfRequisitionDetailUsedInPO - quantityusedForParticularPODetail;
        } catch (Exception ex) {

        }
        return resultQuantity;
    }
}