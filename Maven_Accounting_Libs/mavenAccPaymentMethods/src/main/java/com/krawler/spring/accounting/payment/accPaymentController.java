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
package com.krawler.spring.accounting.payment;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.ChequeLayout;
import com.krawler.hql.accounting.PaymentMethod;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.UOBBankDetails;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
public class accPaymentController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accPaymentDAO accPaymentDAOobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public ModelAndView savePaymentMethod(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PaymentMethods_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            msg=savePaymentMethod(request);
            issuccess = true;
             if(!StringUtil.isNullOrEmpty(msg)){
                msg = msg.substring(0, (msg.lastIndexOf(",")));
                msg = messageSource.getMessage( "acc.pay1.methodupdate", null, RequestContextUtils.getLocale(request))+"<br> <b> Except&nbsp: </b>"+msg;
            }else{
                msg = messageSource.getMessage("acc.pay1.methodupdate", null, RequestContextUtils.getLocale(request));   //"Payment method has been Updated successfully";
            }
            
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String savePaymentMethod(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        String msg = "";
        try {
            int delCount = 0;
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String methodid = "";
            List PayMethodList = new ArrayList();
            KwlReturnObject methodresult;
            int count = 0;
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("methodid")) == false) {
                    methodid = jobj.getString("methodid");
                    String methodname = jobj.getString("methodname");
                    try {
                        methodresult = accPaymentDAOobj.searchPaymentMethod(methodid, companyid);
                        count = methodresult.getRecordTotalCount();
                        methodresult = accPaymentDAOobj.searchPaymentMethodInFundTransferJE(methodid, companyid);
                        count += methodresult.getRecordTotalCount();
                        if (count > 0) {
                            if (!PayMethodList.contains(methodname)) {
                                PayMethodList.add(methodname);
                                msg += methodname + ",";
                            }
                            continue;
                        } else {
                            methodresult = accPaymentDAOobj.deletePaymentMethod(methodid, companyid);
                        }

                        delCount += methodresult.getRecordTotalCount();
                        auditTrailObj.insertAuditLog(AuditAction.PAYMENT_METHOD_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Payment Method " + methodname, request, "0");
                    } catch (ServiceException ex) {
                        throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");  ///messageSource.getMessage("acc.pay1.excp1", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            PaymentMethod pom;
            String auditMsg;
            String auditID;
//            String fullName = AuthHandler.getFullName(session, AuthHandler.getUserid(request));
//            if (delCount > 0) {
//                auditTrailObj.insertAuditLog(AuditAction.PAYMENT_METHOD_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Payment Method "+ methodname, request, "0");
//            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                Map<String, Object> methodMap = new HashMap<>();
                String methodId = StringUtil.DecodeText(jobj.optString("methodid"));
                methodMap.put("methodid", methodId);
                if (jobj.has("srno")) {
                    methodMap.put("srno", jobj.optInt("srno", 0));
                }
                if (!StringUtil.isNullOrEmpty(methodId)) {
                    accPaymentDAOobj.updatePaymentMethod(methodMap); // update srno for sequence payment method.
                }
                if (jobj.getBoolean("modified") == false) {// modified==true when user modified/add record in payment method
                    continue;
                }
                
                methodMap.put("methodname", StringUtil.DecodeText(jobj.optString("methodname")));
                methodMap.put("accountid", StringUtil.DecodeText(jobj.optString("accountid")));
                methodMap.put("detailtype", jobj.getInt("detailtype"));
                methodMap.put("autopopulate", jobj.optBoolean("autopopulate",false));
                methodMap.put("autopopulateincpcs", jobj.optBoolean("autopopulateincpcs",false));
                methodMap.put("autopopulateinloan", jobj.optBoolean("autopopulateinloan",false));
                methodMap.put("autoPopulateInIBGGeneration", jobj.optBoolean("autoPopulateInIBGGeneration",false));
                methodMap.put("companyid", companyid);

                if (StringUtil.isNullOrEmpty(jobj.getString("methodid"))) {
                    auditMsg = "added";
                    auditID = AuditAction.PAYMENT_METHOD_ADDED;
                    methodresult = accPaymentDAOobj.addPaymentMethod(methodMap);
                } else {
                    auditMsg = "updated";
                    auditID = AuditAction.PAYMENT_METHOD_CHANGED;
                    methodMap.put("methodid", StringUtil.DecodeText(jobj.optString("methodid")));
                    methodresult = accPaymentDAOobj.updatePaymentMethod(methodMap);
                }
                pom = (PaymentMethod) methodresult.getEntityList().get(0);

                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " Payment Method " + pom.getMethodName(), request, pom.getID());
            }
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } */catch (JSONException ex) {
            throw ServiceException.FAILURE("savePaymentMethod : " + ex.getMessage(), ex);
        }
        return msg;
    }

    public ModelAndView getPaymentMethods(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String paymentAccountType = request.getParameter("paymentAccountType");
            String accountID = request.getParameter("accountid");
             String start=request.getParameter(Constants.start);   //ERP-13660 [SJ]
            String limit=request.getParameter(Constants.limit);
            boolean loanFlag= request.getParameter("loanFlag")!=null?Boolean.parseBoolean(request.getParameter("loanFlag")):false;
            boolean populateincpcs= request.getParameter("populateincpcs")!=null?Boolean.parseBoolean(request.getParameter("populateincpcs")):false;
            boolean onlyIBGAccounts= request.getParameter("onlyIBGAccounts")!=null?Boolean.parseBoolean(request.getParameter("onlyIBGAccounts")):false;
            int IBGBankType = request.getParameter("IBGBankType")!=null?Integer.parseInt(request.getParameter("IBGBankType")):1;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(paymentAccountType)) {
                requestParams.put("paymentAccountType", paymentAccountType);
            }
            if (!StringUtil.isNullOrEmpty(accountID)) {
                requestParams.put("accountid", accountID);
            }
            if(populateincpcs){
                requestParams.put("populateincpcs", populateincpcs);
            }
            if(loanFlag){
                requestParams.put("loanFlag", loanFlag);
            }
            if(onlyIBGAccounts){
                requestParams.put("onlyIBGAccounts", onlyIBGAccounts);
                requestParams.put("IBGBankType", IBGBankType);
                requestParams.put("populateInIBGGeneration", true);
            }
            KwlReturnObject result = accPaymentDAOobj.getPaymentMethod(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = getPaymentMethodsJson(list, request);
            JSONArray pagedJson = jArr;//ERP-13660 [SJ]
                int cunt=pagedJson.length();
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(jArr, Integer.parseInt(start), Integer.parseInt(limit));
                }
            jobj.put("data", pagedJson);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getPaymentMethodsJson(List list, HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String methodid = sessionHandlerImpl.getPaymentMethodID(request);
            boolean onlyIBGAccounts = request.getParameter("onlyIBGAccounts") != null ? Boolean.parseBoolean(request.getParameter("onlyIBGAccounts")) : false;
            boolean methodFlag = true;
            if (methodid == null) {
                methodFlag = false;
            }
            HashMap<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put("companyid",StringUtil.isNullOrEmpty(companyid)?"":companyid);
            
            KwlReturnObject resultData=accPaymentDAOobj.getPayMtdMappedToCustomer(paramsMap);
            List payMtdList = resultData.getEntityList();
            boolean pmtMtdMappedToCustomer=false;
            KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
            while (itr.hasNext()) {
                PaymentMethod paymethod = (PaymentMethod) itr.next();
                if(!paymethod.getAccount().isActivate()){   //ERP-26104
                    continue;   //If Account is deactivate / dormant then we cannot use it in any type of transaction.
                }
                if (!extraCompanyPreferences.isPaymentMethodAsCard()) {
                    /*
                     * If payment method as card type then it invisible. 
                     */
                    if(paymethod.getDetailType()==1){
                        continue;
                    }
                    
                }
                
                JSONObject obj = new JSONObject();
                obj.put("methodid", paymethod.getID());
                obj.put("methodname", paymethod.getMethodName());
                obj.put("accountid", paymethod.getAccount().getID());
                obj.put("accountname", paymethod.getAccount().getName());
                obj.put("isIBGBankAccount", paymethod.getAccount().isIBGBank());    
                if(paymethod.getAccount().isIBGBank()){
                    obj.put("bankType", paymethod.getAccount().getIbgBankType());    
                }
                obj.put("detailtype", paymethod.getDetailType());
                obj.put("acccurrency", paymethod.getAccount().getCurrency().getCurrencyID());
                obj.put("acccurrencysymbol", paymethod.getAccount().getCurrency().getSymbol());
                obj.put("acccustminbudget", paymethod.getAccount().getCustMinBudget());
                obj.put("autopopulate", paymethod.isAutoPopulate());
                obj.put("autopopulateinloan", paymethod.isAutoPopulateInLoan());
                obj.put("autopopulateincpcs", paymethod.isAutoPopulateInCPCS());
                obj.put("autoPopulateInIBGGeneration", paymethod.isAutoPopulateInIBGGeneration());
                if (!methodFlag) {
                    if ((StringUtil.equal(paymethod.getAccount().getName(), "Cash in hand")) && (paymethod.getDetailType() == 0) && (StringUtil.equal(paymethod.getMethodName(), "Cash"))) {
                        obj.put("isdefault", "true");
                        sessionHandlerImpl.updatePaymentMethodID(request, paymethod.getID());
                    }
                } else if (StringUtil.equal(paymethod.getID(), methodid.toString())) {
                    obj.put("isdefault", "true");
                } else {
                    obj.put("isdefault", "false");
                }
                if (payMtdList.contains(paymethod.getID())) {
                    obj.put("isMappedToCustomer", "true");
                } else {
                    obj.put("isMappedToCustomer", "false");
                }
                if (onlyIBGAccounts) {
                    /*
                     *ERP-31229
                     */
                    Map<String, Object> uobMap = new HashMap<>();
                    uobMap.put(Constants.companyKey, companyid);
                    uobMap.put(Constants.Acc_Accountid, paymethod.getAccount().getID());
                    KwlReturnObject result = accPaymentDAOobj.getUOBBankDetails(uobMap);
                    if (result != null && result.getEntityList().size() > 0) {
                        UOBBankDetails uOBBankDetails = (UOBBankDetails) result.getEntityList().get(0);
                        obj.put(Constants.UOB_CompanyID, uOBBankDetails.getUOBCompanyID() != null ? uOBBankDetails.getUOBCompanyID() : "");
                    }
                }

                KwlReturnObject result = accPaymentDAOobj.getTransactionCountForPayment(paymethod.getID(), companyid);
                if (result.getRecordTotalCount() > 0) {
                    obj.put("isChangableAccount", false);
                } else {
                    obj.put("isChangableAccount", true);
                }

                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getPaymentMethodsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView setChequeLayout(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        KwlReturnObject methodresult = null;
        String auditmessage=null;
        TransactionStatus status = txnManager.getTransaction(def);

        try {
            String bankid = StringUtil.isNullOrEmpty(request.getParameter("bankid")) ? "" : request.getParameter("bankid");
            String dateformat = StringUtil.isNullOrEmpty(request.getParameter("dateformat")) ? Constants.DEFAULT_FORMATID_CHECK : request.getParameter("dateformat");
            String appendcharacter = StringUtil.isNullOrEmpty(request.getParameter("appendcharacter")) ? "" : request.getParameter("appendcharacter");
            double dateLeft = StringUtil.isNullOrEmpty(request.getParameter("dateLeft")) ? 0 : Double.parseDouble(request.getParameter("dateLeft"));
            double nameLeft = StringUtil.isNullOrEmpty(request.getParameter("nameLeft")) ? 0 : Double.parseDouble(request.getParameter("nameLeft"));
            double amtinwordLeft = StringUtil.isNullOrEmpty(request.getParameter("amtinwordLeft")) ? 0 : Double.parseDouble(request.getParameter("amtinwordLeft"));
            double amtLeft = StringUtil.isNullOrEmpty(request.getParameter("amtLeft")) ? 0 : Double.parseDouble(request.getParameter("amtLeft"));
            double dateTop = StringUtil.isNullOrEmpty(request.getParameter("dateTop")) ? 0 : Double.parseDouble(request.getParameter("dateTop"));
            double nameTop = StringUtil.isNullOrEmpty(request.getParameter("nameTop")) ? 0 : Double.parseDouble(request.getParameter("nameTop"));
            double amtinwordTop = StringUtil.isNullOrEmpty(request.getParameter("amtinwordTop")) ? 0 : Double.parseDouble(request.getParameter("amtinwordTop"));
            double amtTop = StringUtil.isNullOrEmpty(request.getParameter("amtTop")) ? 0 : Double.parseDouble(request.getParameter("amtTop"));
            double amtinwordLeftLine2 = StringUtil.isNullOrEmpty(request.getParameter("amtinwordLeftLine2")) ? 0 : Double.parseDouble(request.getParameter("amtinwordLeftLine2"));
            double amtinwordTopLine2 = StringUtil.isNullOrEmpty(request.getParameter("amtinwordTopLine2")) ? 0 : Double.parseDouble(request.getParameter("amtinwordTopLine2"));
            
            /*
             1.US related cheque variable
             2. If other fields co-ordinates blank then we are saving '-1' co-ordinates means '-1' fields will not display on check
            */
            boolean activateExtraFields= request.getParameter("activateExtraFields")!=null?Boolean.parseBoolean(request.getParameter("activateExtraFields")):false;
            boolean addCharacterInCheckDate= !StringUtil.isNullOrEmpty(request.getParameter("addCharacterInCheckDate"));
            double memoLeft = StringUtil.isNullOrEmpty(request.getParameter("memoLeft")) ? -1 : Double.parseDouble(request.getParameter("memoLeft"));
            double memoTop = StringUtil.isNullOrEmpty(request.getParameter("memoTop")) ? -1 : Double.parseDouble(request.getParameter("memoTop"));
            double addressLine1Left = StringUtil.isNullOrEmpty(request.getParameter("addressLine1Left")) ? -1 : Double.parseDouble(request.getParameter("addressLine1Left"));
            double addressLine1Top = StringUtil.isNullOrEmpty(request.getParameter("addressLine1Top")) ? -1 : Double.parseDouble(request.getParameter("addressLine1Top"));
            double addressLine2Left = StringUtil.isNullOrEmpty(request.getParameter("addressLine2Left")) ? -1 : Double.parseDouble(request.getParameter("addressLine2Left"));
            double addressLine2Top = StringUtil.isNullOrEmpty(request.getParameter("addressLine2Top")) ? -1 : Double.parseDouble(request.getParameter("addressLine2Top"));
            double addressLine3Left = StringUtil.isNullOrEmpty(request.getParameter("addressLine3Left")) ? -1 : Double.parseDouble(request.getParameter("addressLine3Left"));
            double addressLine3Top = StringUtil.isNullOrEmpty(request.getParameter("addressLine3Top")) ? -1 : Double.parseDouble(request.getParameter("addressLine3Top"));
            double addressLine4Left = StringUtil.isNullOrEmpty(request.getParameter("addressLine4Left")) ? -1 : Double.parseDouble(request.getParameter("addressLine4Left"));
            double addressLine4Top = StringUtil.isNullOrEmpty(request.getParameter("addressLine4Top")) ? -1 : Double.parseDouble(request.getParameter("addressLine4Top"));
            
            String selectFontSizeForMemoTxt=StringUtil.isNullOrEmpty(request.getParameter("selectFontSizeForMemoTxt")) ? "" : request.getParameter("selectFontSizeForMemoTxt");
            String selectFontSizeForAddressLine1Txt=StringUtil.isNullOrEmpty(request.getParameter("selectFontSizeForAddressLine1Txt")) ? "" : request.getParameter("selectFontSizeForAddressLine1Txt");
            String selectFontSizeForAddressLine2Txt=StringUtil.isNullOrEmpty(request.getParameter("selectFontSizeForAddressLine2Txt")) ? "" : request.getParameter("selectFontSizeForAddressLine2Txt");
            String selectFontSizeForAddressLine3Txt=StringUtil.isNullOrEmpty(request.getParameter("selectFontSizeForAddressLine3Txt")) ? "" : request.getParameter("selectFontSizeForAddressLine3Txt");
            String selectFontSizeForAddressLine4Txt=StringUtil.isNullOrEmpty(request.getParameter("selectFontSizeForAddressLine4Txt")) ? "" : request.getParameter("selectFontSizeForAddressLine4Txt");
            
                        
            String fontStyle=StringUtil.isNullOrEmpty(request.getParameter("fontStyle")) ? "" : request.getParameter("fontStyle");
            String fontSizeOfDate=StringUtil.isNullOrEmpty(request.getParameter("fontSizeOfDate")) ? "" : request.getParameter("fontSizeOfDate");
            String fontSizeOfName=StringUtil.isNullOrEmpty(request.getParameter("fontSizeOfName")) ? "" : request.getParameter("fontSizeOfName");
            String fontSizeofAmountInWords=StringUtil.isNullOrEmpty(request.getParameter("fontSizeofAmountInWords")) ? "" : request.getParameter("fontSizeofAmountInWords");
            String fontSizeofAmount=StringUtil.isNullOrEmpty(request.getParameter("fontSizeofAmount")) ? "" : request.getParameter("fontSizeofAmount");
//            String query = "select id from ChequeLayout where paymentmethod='" + bankid + "'";
            KwlReturnObject chequeObj = accPaymentDAOobj.getChequeLayoutPaymentMethod(bankid);
            List listObj=chequeObj.getEntityList();

            JSONObject obj = new JSONObject();
            obj.put("dateLeft", dateLeft);
            obj.put("nameLeft", nameLeft);
            obj.put("amtinwordLeft", amtinwordLeft);
            obj.put("amtinwordLeftLine2", amtinwordLeftLine2);
            obj.put("amtLeft", amtLeft);
            obj.put("dateTop", dateTop);
            obj.put("nameTop", nameTop);
            obj.put("amtinwordTop", amtinwordTop);
            obj.put("amtinwordTopLine2", amtinwordTopLine2);
            obj.put("amtTop", amtTop);
            
            obj.put("fontStyle", fontStyle);
            obj.put("dateFontSize", fontSizeOfDate);
            obj.put("nameFontSize", fontSizeOfName);
            obj.put("amountInWordsFontSize", fontSizeofAmountInWords);
            obj.put("amountFontSize", fontSizeofAmount);
            
            /*
             Put US related co-ordinates in JSON object
            */
            if (activateExtraFields) {
                obj.put("memoLeft", memoLeft);
                obj.put("memoTop", memoTop);
                obj.put("addressLine1Left", addressLine1Left);
                obj.put("addressLine1Top", addressLine1Top);
                obj.put("addressLine2Left", addressLine2Left);
                obj.put("addressLine2Top", addressLine2Top);
                obj.put("addressLine3Left", addressLine3Left);
                obj.put("addressLine3Top", addressLine3Top);
                obj.put("addressLine4Left", addressLine4Left);
                obj.put("addressLine4Top", addressLine4Top);

                obj.put("selectFontSizeForMemoTxt", selectFontSizeForMemoTxt);
                obj.put("selectFontSizeForAddressLine1Txt", selectFontSizeForAddressLine1Txt);
                obj.put("selectFontSizeForAddressLine2Txt", selectFontSizeForAddressLine2Txt);
                obj.put("selectFontSizeForAddressLine3Txt", selectFontSizeForAddressLine3Txt);
                obj.put("selectFontSizeForAddressLine4Txt", selectFontSizeForAddressLine4Txt);
            }
            String coordinates = obj.toString();
            HashMap<String, Object> chequeLayoutMap = new HashMap<String, Object>();
            chequeLayoutMap.put("bankid", bankid);
            chequeLayoutMap.put("dateformat", dateformat);
            chequeLayoutMap.put("appendcharacter", appendcharacter);
            chequeLayoutMap.put("coordinates", coordinates);
            chequeLayoutMap.put("locale" , RequestContextUtils.getLocale(request));
            chequeLayoutMap.put("activateExtraFields" , activateExtraFields);
            chequeLayoutMap.put("addCharacterInCheckDate" , addCharacterInCheckDate);
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), bankid);
            PaymentMethod paymentmethodname = (PaymentMethod) objItr.getEntityList().get(0);
            if (listObj.isEmpty()) {
                chequeLayoutMap.put("isnewlayout",true);
                methodresult = accPaymentDAOobj.addChequeLayout(chequeLayoutMap);
                auditmessage=" has added a Cheque layout for ";
            } else {
                chequeLayoutMap.put("id", listObj.get(0));
                methodresult = accPaymentDAOobj.updateChequeLayout(chequeLayoutMap);
                auditmessage=" has updated a Cheque layout for ";
            }
            jobj.put("success", true);
            jobj.put("msg", methodresult.getMsg());
            auditTrailObj.insertAuditLog(AuditAction.CHEQUE_LAYOUT, "User " + sessionHandlerImpl.getUserFullName(request) + auditmessage+paymentmethodname.getMethodName()+" Bank", request, "0");
            txnManager.commit(status);
        } catch (Exception e) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "Error occurred while adding Cheque Layout.");
            } catch (JSONException e1) {
                System.out.println(e1.getMessage());
            }
            System.out.println(e.getMessage());
            txnManager.rollback(status);

        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getChequeLayout(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String bankid = request.getParameter("bankid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            if (!StringUtil.isNullOrEmpty(bankid)) {
                requestParams.put("bankid", bankid);
            }
            
            KwlReturnObject result = accPaymentDAOobj.getChequeLayout(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = getChequeLayoutJson(list, request);
            jobj.put("data", jArr);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getChequeLayoutJson(List list, HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            if (list.isEmpty()) {
                JSONObject obj = new JSONObject();
                obj.put("dateLeft", "0");
                obj.put("nameLeft", "0");
                obj.put("amtinwordLeft", "0");
                obj.put("amtinwordLeftLine2", "0");
                obj.put("amtLeft", "0");
                obj.put("dateTop", "0");
                obj.put("nameTop", "0");
                obj.put("amtinwordTop", "0");
                obj.put("amtinwordTopLine2", "0");
                obj.put("amtTop", "0");
                obj.put("appendcharacter", "");
                obj.put("dateformat",Constants.DEFAULT_FORMATID_CHECK);
                jArr.put(obj);
            } else {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    ChequeLayout chequeLayout = (ChequeLayout) itr.next();
                    JSONObject obj = new JSONObject();
                    JSONObject jobj = new JSONObject(chequeLayout.getCoordinateinfo());
                    obj.put("appendcharacter", chequeLayout.getAppendcharacter());
                    obj.put("dateformat", chequeLayout.getDateFormat().getFormatID());
                    obj.put("addCharacterInCheckDate", chequeLayout.isAddCharacterInCheckDate());
                    obj.put("isnewlayout", chequeLayout.isIsnewlayout());
                    obj.put("activateExtraFields", chequeLayout.isActivateExtraFields());
                    obj.put("dateLeft", jobj.getString("dateLeft"));
                    obj.put("nameLeft", jobj.getString("nameLeft"));                   
                    obj.put("amtinwordLeft", jobj.getString("amtinwordLeft"));
                    obj.put("amtinwordLeftLine2", jobj.has("amtinwordLeftLine2")?jobj.getString("amtinwordLeftLine2"):"0");
                    obj.put("amtLeft", jobj.getString("amtLeft"));
                    obj.put("dateTop", jobj.getString("dateTop"));
                    obj.put("nameTop", jobj.getString("nameTop"));
                    obj.put("amtinwordTop", jobj.getString("amtinwordTop"));
                    obj.put("amtinwordTopLine2", jobj.has("amtinwordTopLine2")?jobj.getString("amtinwordTopLine2"):"0");
                    obj.put("amtTop", jobj.getString("amtTop"));
                    
                    obj.put("fontStyle", jobj.has("fontStyle") ? jobj.getString("fontStyle") : "");
                    obj.put("dateFontSize", jobj.has("dateFontSize") ? jobj.getString("dateFontSize") : "");
                    obj.put("nameFontSize", jobj.has("nameFontSize") ? jobj.getString("nameFontSize") : "");
                    obj.put("amountInWordsFontSize", jobj.has("amountInWordsFontSize") ? jobj.getString("amountInWordsFontSize") : "");
                    obj.put("amountFontSize", jobj.has("amountFontSize") ? jobj.getString("amountFontSize") : "");
                    
                    /*
                     Get US related additional cheque co-ordinates                    */
                    if (chequeLayout.isActivateExtraFields()) {

                        if (!jobj.optString("memoLeft").equals("-1")) {
                            obj.put("memoLeft", jobj.optString("memoLeft", "0"));
                        }
                        if (!jobj.optString("memoTop").equals("-1")) {
                            obj.put("memoTop", jobj.optString("memoTop", "0"));
                        }
                        if (!jobj.optString("addressLine1Left").equals("-1")) {
                            obj.put("addressLine1Left", jobj.optString("addressLine1Left", "0"));
                        }
                        if (!jobj.optString("addressLine1Top").equals("-1")) {

                            obj.put("addressLine1Top", jobj.optString("addressLine1Top", "0"));
                        }
                        if (!jobj.optString("addressLine2Left").equals("-1")) {
                            obj.put("addressLine2Left", jobj.optString("addressLine2Left", "0"));
                        }
                        if (!jobj.optString("addressLine2Top").equals("-1")) {
                            obj.put("addressLine2Top", jobj.optString("addressLine2Top", "0"));
                        }
                        if (!jobj.optString("addressLine3Left").equals("-1")) {
                            obj.put("addressLine3Left", jobj.optString("addressLine3Left", "0"));
                        }
                        if (!jobj.optString("addressLine3Top").equals("-1")) {
                            obj.put("addressLine3Top", jobj.optString("addressLine3Top", "0"));
                        }
                        if (!jobj.optString("addressLine4Left").equals("-1")) {
                            obj.put("addressLine4Left", jobj.optString("addressLine4Left", "0"));
                        }
                        if (!jobj.optString("addressLine4Top").equals("-1")) {
                            obj.put("addressLine4Top", jobj.optString("addressLine4Top", "0"));
                        }
                        if (!jobj.optString("selectFontSizeForMemoTxt").equals("-1")) {
                            obj.put("selectFontSizeForMemoTxt", jobj.optString("selectFontSizeForMemoTxt", ""));
                        }
                        if (!jobj.optString("selectFontSizeForAddressLine1Txt").equals("-1")) {
                            obj.put("selectFontSizeForAddressLine1Txt", jobj.optString("selectFontSizeForAddressLine1Txt", ""));
                        }
                        if (!jobj.optString("selectFontSizeForAddressLine2Txt").equals("-1")) {
                            obj.put("selectFontSizeForAddressLine2Txt", jobj.optString("selectFontSizeForAddressLine2Txt", ""));
                        }
                        if (!jobj.optString("selectFontSizeForAddressLine3Txt").equals("-1")) {
                            obj.put("selectFontSizeForAddressLine3Txt", jobj.optString("selectFontSizeForAddressLine3Txt", ""));
                        }
                        if (!jobj.optString("selectFontSizeForAddressLine4Txt").equals("-1")) {
                            obj.put("selectFontSizeForAddressLine4Txt", jobj.optString("selectFontSizeForAddressLine4Txt", ""));
                        }
                    }
                    
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getPaymentMethodsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
        }
