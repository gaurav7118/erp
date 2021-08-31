/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.pos;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 *
 * @author krawler
 */
public class AccPOSInterfaceServiceImpl implements AccPOSInterfaceService {
    
    private AccPOSInterfaceDAO accPOSInterfaceDAO;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    
    public void setaccPOSInterfaceDAO(AccPOSInterfaceDAO accPOSInterfaceDAO) {
        this.accPOSInterfaceDAO = accPOSInterfaceDAO;
    }
    
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
         
    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
 
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, SessionExpiredException.class, JSONException.class, AccountingException.class})
      public JSONObject savePOSCompanyWizardSettings(JSONObject paramJobj) throws JSONException, SessionExpiredException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;
        String auditID="";
        String sms=" created ";
        String message = messageSource.getMessage("acc.RestSerivce.transactionError", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
        StringBuilder messagebuildString = new StringBuilder();
        messagebuildString.append(" "+messageSource.getMessage("acc.RestSerivce.auditTrialPOSSavedMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
        boolean isEdit = false;
        String transactionId = null;
        String auditMessage = "";
        try {
            auditID = AuditAction.POS_STORE_ADD; 
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            paramJobj.put(POSERPMapping.CREATED_ON, createdon);
            paramJobj.put(POSERPMapping.UPDATED_ON, updatedon);
            String retailStoreId = paramJobj.optString("storeid");
            if (!StringUtil.isNullOrEmpty(retailStoreId)) {
                
                paramJobj.put(POSERPMapping.IS_SAVE, true);
                // get POS details
                KwlReturnObject returnPOSObj = accPOSInterfaceDAO.getPOSConfigDetails(paramJobj);
                if (returnPOSObj.getEntityList() != null && returnPOSObj.getRecordTotalCount() > 0 && returnPOSObj.getEntityList().size() == 1 && returnPOSObj.getEntityList().get(0) != null) {
                    List<Object[]> detailList = returnPOSObj.getEntityList();
                    for (Object[] row : detailList) {
                        if (row.length > 1) {
                            if (row[0] != null) {
                                transactionId = (String) row[0];
                                paramJobj.put(Constants.Acc_id, transactionId);
                                paramJobj.put(Constants.isEdit, true);
                                KwlReturnObject doObj = accountingHandlerDAOobj.getObject(POSERPMapping.class.getName(), transactionId);
                                POSERPMapping poserpObj = (POSERPMapping) doObj.getEntityList().get(0);
                                JSONObject retObj = setValuesForAuditTrialForPOS_ERPMapping(poserpObj, paramJobj);
                                if (retObj.has(Constants.RES_msg) && retObj.get(Constants.RES_msg) != null) {
                                    auditMessage = retObj.optString(Constants.RES_msg);
                                }
                                isEdit = true;
                            }
                        }
                    }
                }
                
                JSONObject returnObj = accPOSInterfaceDAO.savePOSCompanyWizardSettings(paramJobj);
                if (returnObj.optBoolean(Constants.RES_success)) {
                    message =  messageSource.getMessage("acc.RestSerivce.configurationcreationSuccessMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                    if (isEdit) {
                        auditID = AuditAction.POS_STORE_EDIT;
                        sms = "updated ";
                        messagebuildString = new StringBuilder();
                        messagebuildString.append(" POS Store mapping configuration " +auditMessage);
                        message = messageSource.getMessage("acc.RestSerivce.configurationupdatedSuccessMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                    }
                    Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                    auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                    auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                    auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                    String additionalsauditmessage = "User " + paramJobj.optString(Constants.userfullname) + " has " + sms + messagebuildString.toString();
                    auditTrailObj.insertAuditLog(auditID, additionalsauditmessage, auditRequestParams, "");
                }
            }
            
        } catch (Exception ex) {
            try {
                isSuccess = false;
                message=messageSource.getMessage("acc.RestSerivce.transactionError", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                throw ServiceException.FAILURE(message, "", false);
            } catch (ServiceException ex1) {
                Logger.getLogger(AccPOSInterfaceServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } finally {
            response.put(Constants.RES_msg, message);
            response.put(Constants.RES_success, isSuccess);
        }
        return response;
    }

    @Override
    public JSONObject getERPPOSMappingDetails(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        JSONArray jArray = new JSONArray();
        try {
            KwlReturnObject returnObj = accPOSInterfaceDAO.getPOSConfigDetails(paramJobj);
            if (returnObj.getEntityList() != null && returnObj.getRecordTotalCount() > 0 && returnObj.getEntityList().size() == 1 && returnObj.getEntityList().get(0) != null) {
                List<Object[]> detailList = returnObj.getEntityList();
                for (Object[] row : detailList) {
                    JSONObject jobj = new JSONObject();
                    //Transaction id
                    if (row.length >= 1) {
                        if (row[0] != null) {
                            String id = (String) row[0];
                            jobj.put(Constants.Acc_id, id);
                        }
                    }
                    //Walkin Customer id and name
                    if (row.length >= 2) {
                        if (row[1] != null) {
                            String walkinCustomer = (String) row[1];
                            jobj.put(POSERPMapping.WalkinCustomer, walkinCustomer);
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Customer.class.getName(), walkinCustomer);
                            Customer cusObj = (Customer) curreslt.getEntityList().get(0);
                            if (cusObj != null) {
                                jobj.put(POSERPMapping.WalkinCustomer_Name, cusObj.getName());
                            }
                        }
                    }

                    //Cash Out id and Name
                    if (row.length >= 3) {
                        if (row[2] != null) {
                            String cashOutAccount = (String) row[2];
                            jobj.put(POSERPMapping.CASHOUT_ACCOUNT_ID, cashOutAccount);
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Account.class.getName(), cashOutAccount);
                            Account accObj = (Account) curreslt.getEntityList().get(0);
                            if (accObj != null) {
                                jobj.put(POSERPMapping.CASHOUT_ACCOUNT_Name, accObj.getAccountName());
                            }
                        }
                    }

                    //Payment Method id and name
                    if (row.length >= 4) {
                        if (row[3] != null) {
                            String paymentid = (String) row[3];
                            String paymentMethodName = null;
                            String paymentMethodType = null;
                            jobj.put(POSERPMapping.PAYMENT_METHOD_ID, paymentid);
                            String[] paymentidArray = paymentid.split(",");
                            StringBuilder pmStringBuilder = new StringBuilder();
                            StringBuilder pmTypeBuilder = new StringBuilder();
                            if (paymentidArray.length > 0) {
                                for (String paymentMthd : paymentidArray) {
                                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paymentMthd);
                                    PaymentMethod pmObj = (PaymentMethod) curreslt.getEntityList().get(0);
                                    if (pmObj != null) {
                                        if (pmStringBuilder.length() > 0) {
                                            pmStringBuilder.append(",");
                                        }
                                        pmStringBuilder.append(pmObj.getMethodName());
                                        
                                        if (pmTypeBuilder.length() > 0) {
                                            pmTypeBuilder.append(",");
                                        }
                                        pmTypeBuilder.append(pmObj.getDetailType());
                                    }
                                }

                                paymentMethodName = pmStringBuilder.toString();
                                paymentMethodType=pmTypeBuilder.toString();
                            }
                            jobj.put(POSERPMapping.PAYMENT_METHOD_NAME, paymentMethodName);
                            jobj.put(POSERPMapping.PAYMENT_METHOD_TYPE, paymentMethodType);
                        }
                    }
                    //Store Id
                    if (row.length >= 5) {
                        if (row[4] != null) {
                            String storeid = (String) row[4];
                            jobj.put("storeid", storeid);
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Store.class.getName(), storeid);
                            Store storeObj = (Store) curreslt.getEntityList().get(0);
                            if (storeObj != null) {
                                jobj.put(POSERPMapping.Store_Name, storeObj.getAbbreviation());
                            }
                        }
                    }

                    //Allow register to close multiple types flag
                    if (row.length >= 6) {
                        if (row[5] != null) {
                            char iscloseregistermultipletimes = (char) row[5];
                            jobj.put(POSERPMapping.isAllowCloseRegisterMultipleTimes, iscloseregistermultipletimes == 'T' ? true : false);
                        }
                    }

                      //Delivery Order Sequence Format
                    if (row.length >= 7) {
                        if (row[6] != null) {
                            String doSeq = (String) row[6];
                            jobj.put(POSERPMapping.DO_SEQUENCEFORMAT, doSeq);
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), doSeq);
                            SequenceFormat doSeqObj = (SequenceFormat) curreslt.getEntityList().get(0);
                            if (doSeqObj != null) {
                                jobj.put(POSERPMapping.DO_SEQUENCEFORMAT_NAME, doSeqObj.getName());
                            }
                        }
                    }

                    //Invoice SequenceFormat
                    if (row.length >= 8) {
                        if (row[7] != null) {
                            String invSeq = (String) row[7];
                            jobj.put(POSERPMapping.INVOICE_SEQUENCEFORMAT, invSeq);
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), invSeq);
                            SequenceFormat invSeqObj = (SequenceFormat) curreslt.getEntityList().get(0);
                            if (invSeqObj != null) {
                                jobj.put(POSERPMapping.INVOICE_SEQUENCEFORMAT_NAME, invSeqObj.getName());
                            }
                        }
                    }

                    //Sales Return SequenceFormat
                    if (row.length >= 9) {
                        if (row[8] != null) {
                            String srSeq = (String) row[8];
                            jobj.put(POSERPMapping.SALESRETRUN_SEQUENCEFORMAT, srSeq);
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), srSeq);
                            SequenceFormat srSeqObj = (SequenceFormat) curreslt.getEntityList().get(0);
                            if (srSeqObj != null) {
                                jobj.put(POSERPMapping.SALESRETRUN_SEQUENCEFORMAT_NAME, srSeqObj.getName());
                            }
                        }
                    }

                    //Credit Note Sequenceformat
                    if (row.length >= 10) {
                        if (row[9] != null) {
                            String cnSeq = (String) row[9];
                            jobj.put(POSERPMapping.CN_SEQUENCEFORMAT, cnSeq);
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), cnSeq);
                            SequenceFormat cnSeqObj = (SequenceFormat) curreslt.getEntityList().get(0);
                            if (cnSeqObj != null) {
                                jobj.put(POSERPMapping.CN_SEQUENCEFORMAT_NAME, cnSeqObj.getName());
                            }
                        }
                    }
                    //Make Payment Sequenceformat
                    if (row.length >= 11) {
                        if (row[10] != null) {
                            String mpSeq = (String) row[10];
                            jobj.put(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT, mpSeq);
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), mpSeq);
                            SequenceFormat cnSeqObj = (SequenceFormat) curreslt.getEntityList().get(0);
                            if (cnSeqObj != null) {
                                jobj.put(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT_NAME, cnSeqObj.getName());
                            }
                        }
                    }
                    //Receive Payment Sequence Format
                    if (row.length >= 12) {
                        if (row[11] != null) {
                            String cnSeq = (String) row[11];
                            jobj.put(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT, cnSeq);
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), cnSeq);
                            SequenceFormat cnSeqObj = (SequenceFormat) curreslt.getEntityList().get(0);
                            if (cnSeqObj != null) {
                                jobj.put(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT_NAME, cnSeqObj.getName());
                            }
                        }
                    }
                    
                    //Sales Order SequenceFormat
                    if (row.length >= 13) {
                        if (row[12] != null) {
                            String soSeq = (String) row[12];
                            jobj.put(POSERPMapping.SALESORDER_SEQUENCEFORMAT, soSeq);
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), soSeq);
                            SequenceFormat cnSeqObj = (SequenceFormat) curreslt.getEntityList().get(0);
                            if (cnSeqObj != null) {
                                jobj.put(POSERPMapping.SALESORDER_SEQUENCEFORMAT_NAME, cnSeqObj.getName());
                            }
                        }
                    }
                    
                    //Deposit Account Id and name
                    if (row.length >= 14) {
                        if (row[13] != null) {
                             String depositAccount = (String) row[13];
                            jobj.put(POSERPMapping.DEPOSIT_ACCOUNT_ID, depositAccount);
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Account.class.getName(), depositAccount);
                            Account accObj = (Account) curreslt.getEntityList().get(0);
                            if (accObj != null) {
                                jobj.put(POSERPMapping.DEPOSIT_ACCOUNT_Name, accObj.getAccountName());
                            }
                        }
                    }
                    
                    jArray.put(jobj);
                }
                
//                List<POSERPMapping> mappingList = new ArrayList<POSERPMapping>();
//                for (POSERPMapping posmapObj : mappingList) {
//                    String id = posmapObj.getID();
//                    JSONObject jobj = new JSONObject();
//                    jobj.put(Constants.Acc_id, id);
//                    jobj.put(POSERPMapping.CASHOUT_ACCOUNT_ID, posmapObj.getCashOutAccountId());
//                    jobj.put(POSERPMapping.PAYMENT_METHOD_ID, posmapObj.getPaymentMethodId());
//                    jobj.put("storeid", posmapObj.getStoreid());
//                    jobj.put(Constants.useridKey, posmapObj.getUserid());
//                    jobj.put(POSERPMapping.WalkinCustomer, posmapObj.getWalkinCustomer());
//                    jobj.put(POSERPMapping.isAllowCloseRegisterMultipleTimes, posmapObj.isIsCloseRegisterMultipleTimes());
//
//                    jArray.put(jobj);
//                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
        } finally {
            response.put(Constants.RES_MESSAGE, msg);
            response.put(Constants.RES_success, issuccess);
            response.put(Constants.RES_data, jArray);
            response.put(Constants.RES_TOTALCOUNT, jArray.length());
        }
        return response;
    }
    
    public JSONObject setValuesForAuditTrialForPOS_ERPMapping(POSERPMapping oldposObj, JSONObject paramJobj) throws SessionExpiredException {
        JSONObject returnObj=new JSONObject();
        HashMap<String, Object> posDataMap = new HashMap<String, Object>();
        Map<String, Object> oldpos = new HashMap<String, Object>();
        Map<String, Object> newAuditKey = new HashMap<String, Object>();
        String auditMessage=null;
        try {
            
            //Setting values in map for old POS_ERP Mapping
            if (oldposObj != null) {
                
                //Cash OUt account
                if (!StringUtil.isNullOrEmpty(oldposObj.getCashOutAccountId())) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Account.class.getName(), oldposObj.getCashOutAccountId());
                    Account accObj = (Account) curreslt.getEntityList().get(0);
                    oldpos.put(POSERPMapping.CASHOUT_ACCOUNT_Name, accObj != null ? accObj.getName() : "");
                    newAuditKey.put(POSERPMapping.CASHOUT_ACCOUNT_Name,  messageSource.getMessage("acc.RestSerivce.CashAccountLabel", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                //walkin customer
                if (!StringUtil.isNullOrEmpty(oldposObj.getWalkinCustomer())) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Customer.class.getName(), oldposObj.getWalkinCustomer());
                    Customer accObj = (Customer) curreslt.getEntityList().get(0);
                    oldpos.put(POSERPMapping.WalkinCustomer_Name, accObj != null ? accObj.getName() : "");
                    newAuditKey.put(POSERPMapping.WalkinCustomer_Name,  messageSource.getMessage("acc.accPref.walk-inCustomer", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }

                //deposit accountid
                if (!StringUtil.isNullOrEmpty(oldposObj.getDepositAccountId())) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Account.class.getName(), oldposObj.getDepositAccountId());
                    Account accObj = (Account) curreslt.getEntityList().get(0);
                    oldpos.put(POSERPMapping.DEPOSIT_ACCOUNT_Name, accObj != null ? accObj.getName() : "");
                    newAuditKey.put(POSERPMapping.DEPOSIT_ACCOUNT_Name,  messageSource.getMessage("acc.RestSerivce.depositAccountLabel", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                
                //payment method id
                if (!StringUtil.isNullOrEmpty(oldposObj.getPaymentMethodId())) {
                    String paymentMethodName = null;
                    String[] paymentidArray = oldposObj.getPaymentMethodId().split(",");
                    StringBuilder pmStringBuilder = new StringBuilder();
                    StringBuilder pmTypeBuilder = new StringBuilder();
                    if (paymentidArray.length > 0) {
                        for (String paymentMthd : paymentidArray) {
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paymentMthd);
                            PaymentMethod pmObj = (PaymentMethod) curreslt.getEntityList().get(0);
                            if (pmObj != null) {
                                if (pmStringBuilder.length() > 0) {
                                    pmStringBuilder.append(",");
                                }
                                pmStringBuilder.append(pmObj.getMethodName());

                                if (pmTypeBuilder.length() > 0) {
                                    pmTypeBuilder.append(",");
                                }
                                pmTypeBuilder.append(pmObj.getDetailType());
                            }
                        }
                        paymentMethodName = pmStringBuilder.toString();
                    }
                    oldpos.put(POSERPMapping.PAYMENT_METHOD_NAME, StringUtil.isNullOrEmpty(paymentMethodName) ? "" : paymentMethodName);
                    newAuditKey.put(POSERPMapping.PAYMENT_METHOD_NAME, messageSource.getMessage("acc.pmList.gridPaymentMethod", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                //Checking old value of CN
                if (!StringUtil.isNullOrEmpty(oldposObj.getCreditnoteSequenceFormat())) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), oldposObj.getCreditnoteSequenceFormat());
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    oldpos.put(POSERPMapping.CN_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                    newAuditKey.put(POSERPMapping.CN_SEQUENCEFORMAT_NAME, messageSource.getMessage("acc.RestSerivce.creditNoteSequenceFormat", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                
               //Checking old value of DO
                if (!StringUtil.isNullOrEmpty(oldposObj.getDeliveryOrderSequenceFormat())) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), oldposObj.getDeliveryOrderSequenceFormat());
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    oldpos.put(POSERPMapping.DO_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                    newAuditKey.put(POSERPMapping.DO_SEQUENCEFORMAT_NAME, messageSource.getMessage("acc.RestSerivce.doSequenceFormat", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                
                //Checking old value of Invoice
                if (!StringUtil.isNullOrEmpty(oldposObj.getInvoiceSequenceFormat())) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), oldposObj.getInvoiceSequenceFormat());
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    oldpos.put(POSERPMapping.INVOICE_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                    newAuditKey.put(POSERPMapping.INVOICE_SEQUENCEFORMAT_NAME, messageSource.getMessage("acc.RestSerivce.invSequenceFormat", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                //Checking old value of Make PAYMENT
                if (!StringUtil.isNullOrEmpty(oldposObj.getMakePaymentSequenceFormat())) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), oldposObj.getMakePaymentSequenceFormat());
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    oldpos.put(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                    newAuditKey.put(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT_NAME, messageSource.getMessage("acc.RestSerivce.mpSequenceFormat", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                //Checking old value of Receive Payment
                if (!StringUtil.isNullOrEmpty(oldposObj.getReceivePaymentSequenceFormat())) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), oldposObj.getReceivePaymentSequenceFormat());
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    oldpos.put(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                    newAuditKey.put(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT_NAME, messageSource.getMessage("acc.RestSerivce.rpSequenceFormat", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                //Checking old value of Sales Order Sequenceformat
                if (!StringUtil.isNullOrEmpty(oldposObj.getSalesOrderSequenceFormat())) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), oldposObj.getSalesOrderSequenceFormat());
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    oldpos.put(POSERPMapping.SALESORDER_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                    newAuditKey.put(POSERPMapping.SALESORDER_SEQUENCEFORMAT_NAME, messageSource.getMessage("acc.RestSerivce.soSequenceFormat", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                //Checking old value of Sales Return Sequenceformat
                if (!StringUtil.isNullOrEmpty(oldposObj.getSalesreturnSequenceFormat())) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), oldposObj.getSalesreturnSequenceFormat());
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    oldpos.put(POSERPMapping.SALESRETRUN_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                    newAuditKey.put(POSERPMapping.SALESRETRUN_SEQUENCEFORMAT_NAME, messageSource.getMessage("acc.RestSerivce.srSequenceFormat", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }

                
                /*---------------------Newly changed configurations-------------------*/
                String walkinCustomer = paramJobj.optString(POSERPMapping.WalkinCustomer);
                String cashOutAccountId = paramJobj.optString("cashOutAccountId");
                String paymentMethodId = paramJobj.optString("paymentMethodId");
                String invoiceSeq = paramJobj.optString(POSERPMapping.INVOICE_SEQUENCEFORMAT);
                String doSeq = paramJobj.optString(POSERPMapping.DO_SEQUENCEFORMAT);
                String srSeq = paramJobj.optString(POSERPMapping.SALESRETRUN_SEQUENCEFORMAT);
                String cnSeq = paramJobj.optString(POSERPMapping.CN_SEQUENCEFORMAT);
                String mpSeq = paramJobj.optString(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT);
                String rpSeq = paramJobj.optString(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT);
                String soSeq = paramJobj.optString(POSERPMapping.SALESORDER_SEQUENCEFORMAT);
                String depositAccountId = paramJobj.optString(POSERPMapping.DEPOSIT_ACCOUNT_ID);
                
                //New json updated key
          
                if (!StringUtil.isNullOrEmpty(cashOutAccountId)) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Account.class.getName(), cashOutAccountId);
                    Account accObj = (Account) curreslt.getEntityList().get(0);
                    posDataMap.put(POSERPMapping.CASHOUT_ACCOUNT_Name, accObj != null ? accObj.getName() : "");

                }
                if (!StringUtil.isNullOrEmpty(walkinCustomer)) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Customer.class.getName(), walkinCustomer);
                    Customer accObj = (Customer) curreslt.getEntityList().get(0);
                    posDataMap.put(POSERPMapping.WalkinCustomer_Name, accObj != null ? accObj.getName() : "");
                }

                if (!StringUtil.isNullOrEmpty(depositAccountId)) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Account.class.getName(), depositAccountId);
                    Account accObj = (Account) curreslt.getEntityList().get(0);
                    posDataMap.put(POSERPMapping.DEPOSIT_ACCOUNT_Name, accObj != null ? accObj.getName() : "");
                }
                if (!StringUtil.isNullOrEmpty(paymentMethodId)) {
                    String paymentMethodName = null;
                    String[] paymentidArray = paymentMethodId.split(",");
                    StringBuilder pmStringBuilder = new StringBuilder();
                    StringBuilder pmTypeBuilder = new StringBuilder();
                    if (paymentidArray.length > 0) {
                        for (String paymentMthd : paymentidArray) {
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paymentMthd);
                            PaymentMethod pmObj = (PaymentMethod) curreslt.getEntityList().get(0);
                            if (pmObj != null) {
                                if (pmStringBuilder.length() > 0) {
                                    pmStringBuilder.append(",");
                                }
                                pmStringBuilder.append(pmObj.getMethodName());

                                if (pmTypeBuilder.length() > 0) {
                                    pmTypeBuilder.append(",");
                                }
                                pmTypeBuilder.append(pmObj.getDetailType());
                            }
                        }
                        paymentMethodName = pmStringBuilder.toString();
                    }
                    posDataMap.put(POSERPMapping.PAYMENT_METHOD_NAME, StringUtil.isNullOrEmpty(paymentMethodName) ? "" : paymentMethodName);
                }
                if (!StringUtil.isNullOrEmpty(cnSeq)) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), cnSeq);
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    posDataMap.put(POSERPMapping.CN_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                }
                if (!StringUtil.isNullOrEmpty(doSeq)) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), doSeq);
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    posDataMap.put(POSERPMapping.DO_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                }
                if (!StringUtil.isNullOrEmpty(invoiceSeq)) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), invoiceSeq);
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    posDataMap.put(POSERPMapping.INVOICE_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                }
                if (!StringUtil.isNullOrEmpty(mpSeq)) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), mpSeq);
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    posDataMap.put(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                }
                if (!StringUtil.isNullOrEmpty(rpSeq)) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), rpSeq);
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    posDataMap.put(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                }
                if (!StringUtil.isNullOrEmpty(soSeq)) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), soSeq);
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    posDataMap.put(POSERPMapping.SALESORDER_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                }
                if (!StringUtil.isNullOrEmpty(srSeq)) {
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), srSeq);
                    SequenceFormat accObj = (SequenceFormat) curreslt.getEntityList().get(0);
                    posDataMap.put(POSERPMapping.SALESRETRUN_SEQUENCEFORMAT_NAME, accObj != null ? accObj.getName() : "");
                }
                auditMessage = AccountingManager.BuildAuditTrialMessage(posDataMap, oldpos,Constants.POS_MODULEID, newAuditKey);
               returnObj.put(Constants.RES_msg,auditMessage);
            }

        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnObj;
    }
    
    
}
