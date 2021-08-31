/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.companypreferences;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.YearEndCheckList;
import com.krawler.hql.accounting.YearLock;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.utils.json.base.JSONObject;
import java.net.URLDecoder;
import javax.mail.MessagingException;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class used to calculate closing balances for account such as Net
 * Profit/Loss (With and Without Stock Valuation In Financial Report), Stock In
 * Hand and store the balances in ClosingAccountBalances.
 *
 * @author krawler
 */
public class YearEndClosingProcess implements Runnable {

    private List<Map<String, Object>> requestList = new ArrayList<>();
    private boolean isWorking = false;
    AccCompanyPreferencesControllerCMNService accCompanyPreferencesControllerCMNService;
    private auditTrailDAO auditTrailDAO;
    private accCompanyPreferencesDAO accCompanyPreferencesDAO;
    private authHandlerDAO authHandlerDAO;
    private AccountingHandlerDAO accountingHandlerDAO;
    private HibernateTransactionManager txnManager;

    public void add(Map<String, Object> requestMap) {
        requestList.add(requestMap);
    }

    public void setAccCompanyPreferencesControllerCMNService(AccCompanyPreferencesControllerCMNService accCompanyPreferencesControllerCMNService) {
        this.accCompanyPreferencesControllerCMNService = accCompanyPreferencesControllerCMNService;
    }

    public void setAuditTrailDAO(auditTrailDAO auditTrailDAO) {
        this.auditTrailDAO = auditTrailDAO;
    }

    public void setAccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesDAO) {
        this.accCompanyPreferencesDAO = accCompanyPreferencesDAO;
    }

    public void setAuthHandlerDAO(authHandlerDAO authHandlerDAO) {
        this.authHandlerDAO = authHandlerDAO;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    @Override
    public void run() {
        try {
            while (!requestList.isEmpty() && !isWorking) {
                Map<String, Object> requestMap = requestList.get(0);
                String companyid = (String) requestMap.get(Constants.companyKey);
                isWorking = true;
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setName("YEC_Tx");
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                TransactionStatus status = txnManager.getTransaction(def);
                try {
                    Logger.getLogger(YearEndClosingProcess.class.getName()).info("YearEndClosingProcess Thread for Company ID " + companyid + " has been started.");
                    if (requestMap.containsKey("requestJSON") && requestMap.get("requestJSON") != null && requestMap.containsKey("jsonArray") && requestMap.get("jsonArray") != null && requestMap.containsKey("requestParams") && requestMap.get("requestParams") != null && requestMap.containsKey("companyAccountPreferences") && requestMap.get("companyAccountPreferences") != null && requestMap.containsKey("extraCompanyPreferences") && requestMap.get("extraCompanyPreferences") != null) {
                        String yearList = "";
                        String reversalyearList = "";
                        String mailSubject="";
                        String mailContent1="";
                        String mailContent2="";
                        String years="";
                        YearLock yearlock;
                        JSONObject requestJSON = (JSONObject) requestMap.get("requestJSON");
                        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) requestMap.get("extraCompanyPreferences");
                        CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) requestMap.get("companyAccountPreferences");
                        Company company = companyAccountPreferences.getCompany();
                        Map<String, Object> auditRequestParams = new HashMap<>();
                        auditRequestParams.put(Constants.reqHeader, requestJSON.getString(Constants.reqHeader));
                        auditRequestParams.put(Constants.remoteIPAddress, requestJSON.getString(Constants.remoteIPAddress));
                        auditRequestParams.put(Constants.useridKey, requestJSON.getString(Constants.useridKey));
                        JSONArray jArr = (JSONArray) requestMap.get("jsonArray");
                        Map<String, Object> requestParams = (Map<String, Object>) requestMap.get("requestParams");
                        if (company != null && extraCompanyPreferences != null && companyAccountPreferences != null && jArr != null && requestParams != null) {
                            for (int i = jArr.length() - 1; i >= 0; i--) {
                                boolean yearAlreadyNotLocked = false;
                                JSONObject jobj = jArr.getJSONObject(i);
                                Map<String, Object> yearLockMap = new HashMap<>();
                                yearLockMap.put("yearid", Integer.parseInt(StringUtil.DecodeText(jobj.optString("name"))));
                                yearLockMap.put("islock", "true".equalsIgnoreCase(StringUtil.DecodeText(jobj.optString("islock"))));
                                yearLockMap.put("companyid", companyid);
                                //neeraj
                                Integer gridfinanyear = Integer.parseInt(StringUtil.DecodeText(jobj.optString("name")));
                                requestParams.put("yearid", gridfinanyear);
                                KwlReturnObject result = accCompanyPreferencesDAO.getYearLock(requestParams);
                                String yearLockid = StringUtil.DecodeText(jobj.optString("id"));
                                if (!StringUtil.isNullOrEmpty(yearLockid)) {
                                    KwlReturnObject yearLockResult = accountingHandlerDAO.getObject(YearLock.class.getName(), yearLockid);
                                    if (!yearLockResult.getEntityList().isEmpty()) {
                                        YearLock yearLockObj = (YearLock) yearLockResult.getEntityList().get(0);
                                        if ((!yearLockObj.isIsLock() && yearLockMap.containsKey("islock") && (Boolean) yearLockMap.get("islock")) || (yearLockObj.isIsLock() && yearLockMap.containsKey("islock") && !(Boolean) yearLockMap.get("islock"))) {
                                            yearAlreadyNotLocked = true;
                                        }
                          
                                        if ((yearLockObj.isIsLock() && yearLockMap.containsKey("islock") && !(Boolean) yearLockMap.get("islock"))) {
                                            if (reversalyearList.length() == 0) {
                                                reversalyearList += !StringUtil.isNullOrEmpty(jobj.optString("endYearId")) ? jobj.optString("endYearId") : yearLockObj.getYearid();
                                            } else {
                                                reversalyearList += "," + (!StringUtil.isNullOrEmpty(jobj.optString("endYearId")) ? jobj.optString("endYearId") : yearLockObj.getYearid());
                                            }
                                        }
                                    }
                                }
                                if (result.getEntityList().size() <= 0) {//if year is empty then add the entry because the mainpart is of close the books
                                    if (StringUtil.isNullOrEmpty(yearLockid)) {
                                        yearLockMap.put("islock", (!jobj.optString("islock").equals("false")));
                                        result = accCompanyPreferencesDAO.addYearLock(yearLockMap);
                                    }
                                } else { //if year lock id is present then update it.
                                    yearlock = (YearLock) result.getEntityList().get(0);
                                    yearLockMap.put("id", yearlock.getID());
                                    yearLockMap.put("islock", (!jobj.optString("islock").equals("false")));
                                    result = accCompanyPreferencesDAO.updateYearLock(yearLockMap);
                                }
                                yearlock = (YearLock) result.getEntityList().get(0);

                                if (yearlock.isIsLock()) {
                                    KwlReturnObject yearLockResult = accountingHandlerDAO.getObject(YearEndCheckList.class.getName(), yearLockid);
                                    if (yearLockResult != null && yearLockResult.getEntityList() != null) {
                                        YearEndCheckList checkList = result.getEntityList().isEmpty() ? null : ((YearEndCheckList) yearLockResult.getEntityList().get(0));
                                        if (checkList == null) {
                                            JSONObject checkListJSON = new JSONObject();
                                            checkListJSON.put("yearlock", yearlock.getID());
                                            checkListJSON.put(Constants.CHECKLIST_DOCUMENT_REVALUATION_COMPLETED, jobj.optBoolean(Constants.CHECKLIST_DOCUMENT_REVALUATION_COMPLETED, false));
                                            checkListJSON.put(Constants.CHECKLIST_ADJUSTMENT_FOR_TRANSACTIONS_COMPLETED, jobj.optBoolean(Constants.CHECKLIST_ADJUSTMENT_FOR_TRANSACTIONS_COMPLETED, false));
                                            checkListJSON.put(Constants.CHECKLIST_INVENTORY_ADJUSTMENT_COMPLETED, jobj.optBoolean(Constants.CHECKLIST_INVENTORY_ADJUSTMENT_COMPLETED, false));
                                            checkListJSON.put(Constants.CHECKLIST_ASSET_DEPRECIATION_COMPLETED, jobj.optBoolean(Constants.CHECKLIST_ASSET_DEPRECIATION_COMPLETED, false));
                                            checkListJSON.put(Constants.companyKey, companyid);
                                            accCompanyPreferencesDAO.addYearEndCheckList(checkListJSON);
                                        }
                                    }
                                } else {
                                    accCompanyPreferencesDAO.deleteYearEndCheckList(yearLockid, companyid);
                                }
                                if (yearAlreadyNotLocked) {
                                    accCompanyPreferencesControllerCMNService.calculateAndStoreClosingAccountBalance(yearlock, requestJSON, extraCompanyPreferences, companyAccountPreferences);
                                    if (yearlock.isIsLock()) {
                                        if (yearList.equals("")) {
                                            yearList += !StringUtil.isNullOrEmpty(jobj.optString("endYearId")) ? jobj.optString("endYearId") : yearlock.getYearid();
                                        } else {
                                            yearList += "," + (!StringUtil.isNullOrEmpty(jobj.optString("endYearId")) ? jobj.optString("endYearId") : yearlock.getYearid());
                                        }
                                    }
                                }
                            }
                            if(!StringUtil.isNullOrEmpty(yearList)){
                                years = yearList;
                                mailSubject= "Year End Closing";
                                mailContent1= "closed";
                                mailContent2= "closing";
                            }else if(!StringUtil.isNullOrEmpty(reversalyearList)){
                                years= reversalyearList;
                                mailSubject= "Reversal Of Year End Closing";
                                mailContent1= "done Reversal Of Year End Closing for";
                                mailContent2= "Reversal Of Year End Closing";
                            }
                            if (!StringUtil.isNullOrEmpty(yearList) || !StringUtil.isNullOrEmpty(reversalyearList)) {
                                try {//If allow to send approval mail in company account preferences
                                    String fromName = "User";
                                    //String fromEmailId = Constants.ADMIN_EMAILID;
                                    String fromEmailId = authHandlerDAO.getSysEmailIdByCompanyID(company.getCompanyID());
                                    ArrayList<String> userEmail = new ArrayList<String>();
                                    KwlReturnObject receiptUsersEmail = accCompanyPreferencesDAO.getCompanyAdminUsersEmailIds(requestJSON.optString(Constants.useridKey),companyid); // Get all users of this company
                                    List<String> list=receiptUsersEmail.getEntityList(); // receiptUsersEmail list conatins all Admin user + Login User
                                    for (String emailId : list){
                                        if(!StringUtil.isNullOrEmpty(emailId)){
                                            userEmail.add(emailId);
                                        }
                                    }
                                    fromName = accCompanyPreferencesDAO.getCreatorFullName(companyid);
                                    String emailAndPhoneNumber = company.getPhoneNumber() == null ? "" : company.getPhoneNumber();
                                    if (!StringUtil.isNullOrEmpty(emailAndPhoneNumber)) {
                                        emailAndPhoneNumber = fromEmailId + " / " + emailAndPhoneNumber;
                                    } else {
                                        emailAndPhoneNumber = fromEmailId;
                                    }
                                    String yearlyClosingSubject = "%s - %s";
                                    String yearlyClosingHtmlMsg = "<html><head><title></title></head><style type='text/css'>"
                                            + "a:link, a:visited, a:active {\n"
                                            + " 	color: #03C;"
                                            + "}\n"
                                            + "body {\n"
                                            + "	font-family: Arial, Helvetica, sans-serif;"
                                            + "	color: #000;"
                                            + "	font-size: 13px;"
                                            + "}\n"
                                            + "</style><body>"
                                            + "<p>Hello There,</p>"
                                            + "<p></p>"
                                            + "<p>For your kind information, We have %s %s Year Books.</p>"
                                            + "<p>If you have any questions about the %s process, please phone/mail at %s.</p>"
                                            + "<p>We would be happy to help.</p>"
                                            + "<p>Thank you for your business. We look forward to working with you.</p>"
                                            + "<p></p>"
                                            + "<p>Sincerely,</p>"
                                            + "<p>%s</p>"
                                            + "<p>This is an auto generated email. Do not reply<br>";
                                    String yearlyClosingPlainMsg = "Hello There,\n\n"
                                            + "For your kind information, We have %s %s Year Books.\n"
                                            + "If you have any questions about the %s process, please phone/mail at %s.\n"
                                            + "We would be happy to help.\n"
                                            + "Thank you for your business. We look forward to working with you.\n\n"
                                            + "Sincerely,\n"
                                            + "%s\n"
                                            + "This is an auto generated email. Do not reply";

                                    String subject = String.format(yearlyClosingSubject, mailSubject, years);
                                    String htmlMsg = String.format(yearlyClosingHtmlMsg, mailContent1, years, mailContent2,emailAndPhoneNumber, fromName);
                                    String plainMsg = String.format(yearlyClosingPlainMsg, mailContent1, years, mailContent2,emailAndPhoneNumber, fromName);
                                    Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).info("Subject : " + subject);
                                    Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).info("htmlMsg : " + htmlMsg);
                                    Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).info("plainMsg : " + plainMsg);
                                    String[] emails = userEmail.toArray(new String[userEmail.size()]); // Converted Arraylist to String Array
                                    if (!StringUtil.isNullOrEmpty(companyAccountPreferences.getApprovalEmails())) {
                                        String[] compPrefMailIds = companyAccountPreferences.getApprovalEmails().split(";");
                                        emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                                    }
                                    if (emails.length > 0) {
                                        Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                                        SendMailHandler.postMail(emails, subject, htmlMsg, plainMsg, fromEmailId, smtpConfigMap);
                                    }
                                } catch (ServiceException | MessagingException ex) {
                                    Logger.getLogger(YearEndClosingProcess.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (Exception ex) {
                                    Logger.getLogger(YearEndClosingProcess.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                if (!StringUtil.isNullOrEmpty(yearList) && yearList.length() > 0) {
                                    String auditMessage = "User " + requestJSON.optString("fullName") + " has done " + String.format("Year End Closing for %s", years) + ".";
                                    auditTrailDAO.insertAuditLog(AuditAction.YEAR_END_CLOSING, auditMessage, auditRequestParams, "0");
                                }
                                if (!StringUtil.isNullOrEmpty(reversalyearList) && reversalyearList.length() > 0) {
                                    String auditMessage = "User " + requestJSON.optString("fullName") + " has done " + String.format("Reversal of Year End Closing for  %s", years) + ".";
                                    auditTrailDAO.insertAuditLog(AuditAction.REVERSAL_OF_YEAR_END_CLOSING, auditMessage, auditRequestParams, "0");
                                }
                            }
                        }
                    }
                    txnManager.commit(status);
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    Logger.getLogger(YearEndClosingProcess.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    Logger.getLogger(YearEndClosingProcess.class.getName()).info("YearEndClosingProcess Thread for Company ID " + companyid + " has been completed.");
                    isWorking = false;
                    requestList.remove(requestMap);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(YearEndClosingProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
