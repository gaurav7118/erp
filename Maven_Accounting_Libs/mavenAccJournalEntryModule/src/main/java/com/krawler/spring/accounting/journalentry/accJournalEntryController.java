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
package com.krawler.spring.accounting.journalentry;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.servlets.ProfileImageServlet;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.CommonFnControllerService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

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
public class accJournalEntryController extends MultiActionController implements MessageSourceAware {
       
    private HibernateTransactionManager txnManager;
    private accJournalEntryDAO accJournalEntryobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDaoObj;
    private MessageSource messageSource;
    private accAccountDAO accAccountDAOobj;
    private accPaymentDAO accPaymentDAOobj;
    private authHandlerDAO authHandlerDAOObj;
    public ImportHandler importHandler;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private EnglishNumberToWords EnglishNumberToWordsOjb = new EnglishNumberToWords();
    private CommonFnControllerService commonFnControllerService;
    
    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj;
    }
    
    @Override    
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setJournalEntryModuleServiceobj(AccJournalEntryModuleService journalEntryModuleServiceobj) {
        this.journalEntryModuleServiceobj = journalEntryModuleServiceobj;
    }
    
    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
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

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public ModelAndView saveJournalEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = journalEntryModuleServiceobj.saveJournalEntry(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView approveJournalEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        double jeAmount = 0.0;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String remark = request.getParameter("remark");
            String jeID = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeID);
            JournalEntry JE = (JournalEntry) jeresult.getEntityList().get(0);
            Set<JournalEntryDetail> jeDetails = JE.getDetails();
            for (JournalEntryDetail journalEntryDetail : jeDetails) {
                jeAmount = journalEntryDetail.getAmount();
            }
            double amount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount"))? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
            String currentUserId = sessionHandlerImpl.getUserid(request);
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            int approvedLevel = journalEntryModuleServiceobj.approveJE(JE, companyid, JE.getApprovestatuslevel(), String.valueOf(amount), paramJobj, false, currentUserId);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                String userName = sessionHandlerImpl.getUserFullName(request);
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                String sendorInfo = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String creatormail = company.getCreator().getEmailID();
                String documentcreatoremail = (JE != null && JE.getCreatedby() != null) ? JE.getCreatedby().getEmailID() : "";
                String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
                String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
                String creatorname = fname + " " + lname;
                String approvalpendingStatusmsg = "";
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                ArrayList<String> emailArray = new ArrayList<>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", JE.getApprovestatuslevel());
                qdDataMap.put(Constants.moduleid,Constants.Acc_GENERAL_LEDGER_ModuleId);
//                emailArray = commonFnControllerService.getUserApprovalEmail(qdDataMap);
                emailArray.add(creatormail);
                if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
                    emailArray.add(documentcreatoremail);
                }
                String[] emails = {};
                emails = emailArray.toArray(emails);
//                String[] emails = {creatormail};
                if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                    String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                    emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                }
                if (JE.getApprovestatuslevel() < 11) {
                    qdDataMap.put("totalAmount",  String.valueOf(amount));
                    qdDataMap.put(Constants.useridKey,  paramJobj.optString(Constants.useridKey));
                    approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put("Number", JE.getEntryNumber());
                mailParameters.put("userName", userName);
                mailParameters.put("emails", emails);
                mailParameters.put("sendorInfo", sendorInfo);
                mailParameters.put("moduleName",  "JOURNAL ENTRY");
                mailParameters.put("addresseeName", "All");
                mailParameters.put("companyid", company.getCompanyID());
                mailParameters.put("baseUrl", baseUrl);
                mailParameters.put("approvalstatuslevel", JE.getApprovestatuslevel());
                mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
                if (emails.length > 0) {
                    accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
                }
            }

            // Save Approval History
            if (approvedLevel != Constants.NoAuthorityToApprove) {  // If currently logged inuser has permission to approve JE , and Je is approved , then only history will be recorded
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("transtype", Constants.JOURNAL_ENTRY_APPROVAL);
                hashMap.put("transid", JE.getID());
                hashMap.put("approvallevel", JE.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
                hashMap.put("remark", remark);
                hashMap.put(Constants.useridKey, sessionHandlerImpl.getUserid(request));
                hashMap.put(Constants.companyKey, companyid);
                accountingHandlerDAOobj.updateApprovalHistory(hashMap);

                // Audit log entry
                auditTrailObj.insertAuditLog("65", "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a Journal Entry " + JE.getEntryNumber()+" at Level-"+JE.getApprovestatuslevel(), request, JE.getID());
                txnManager.commit(status);
                issuccess = true;
                KwlReturnObject kmsg = null;
                String roleName = "Company User";
                kmsg = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
                Iterator ite2 = kmsg.getEntityList().iterator();
                while (ite2.hasNext()) {
                    Object[] row = (Object[]) ite2.next();
                    roleName = row[1].toString();
                }
                msg = messageSource.getMessage("acc.jeApproval.approved", null, RequestContextUtils.getLocale(request))+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+JE.getApprovestatuslevel()+".";;
            } else {                                            // if currently logged in user has no permission to approve JE 
                txnManager.commit(status);
                issuccess = true;
                msg = messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, RequestContextUtils.getLocale(request)) + JE.getApprovestatuslevel()+".";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView rejectPendingJE(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            rejectPendingJE(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.jeApproval.rejected", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void rejectPendingJE(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String actionId = "66", actionMsg = "rejected";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("journalentryid"))) {
                    String jeid = StringUtil.DecodeText(jobj.optString("journalentryid"));
                    KwlReturnObject jeObj = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
                    JournalEntry JE = (JournalEntry) jeObj.getEntityList().get(0);
                    accJournalEntryobj.rejectPendingJE(JE.getID(), companyid);
                    auditTrailObj.insertAuditLog(actionId, "User " + sessionHandlerImpl.getUserFullName(request) + " " + actionMsg + " Journal Entry " + JE.getEntryNumber(), request, JE.getID());
                }
            }
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
    }

 
    /**
     *
     * @param request
     * @param response
     * @return
     * @Desc : Update Recurred JE
     */
    public ModelAndView updateJournalEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jSONObject = new JSONObject();
        try {
            /**
             * Copy all Request Parameter to Json Object
             */
            JSONObject requestparams = StringUtil.convertRequestToJsonObject(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            requestparams.put("df", df);
            jSONObject = journalEntryModuleServiceobj.updateJournalEntry(requestparams);
        } catch (JSONException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, e);
        }
        return new ModelAndView("jsonView", "model", jSONObject.toString());
    }
//    public ModelAndView saveRevaluationJournalEntry(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        String msg = "", jeid = "";
//        boolean issuccess = false;
//
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("JE_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//
//        TransactionStatus status = txnManager.getTransaction(def);
//        try {
//            String companyid = sessionHandlerImpl.getCompanyid(request);
//            List li = saveRevaluationJournalEntry(request);
//            issuccess = true;
//            msg = messageSource.getMessage("acc.je1.save", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been saved successfully";
//            txnManager.commit(status);
//        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
//            msg = ex.getMessage();
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            
//            msg = "" + ex.getMessage();
//            if(msg.equalsIgnoreCase("No Unrealised Gain/Loss account found.")){
//                txnManager.commit(status);   
//            }else{
//                txnManager.rollback(status);
//            }   
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//                jobj.put("id", jeid);
//            } catch (JSONException ex) {
//                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }
    
    
    public class EnglishNumberToWords {

        private final String[] tensNames = {
            "", " Ten", " Twenty", " Thirty", " Forty", " Fifty", " Sixty", " Seventy", " Eighty", " Ninety"
        };
        private final String[] numNames = {
            "", " One", " Two", " Three", " Four", " Five", " Six", " Seven", " Eight", " Nine", " Ten", " Eleven", " Twelve",
            " Thirteen", " Fourteen", " Fifteen", " Sixteen", " Seventeen", " Eighteen", " Nineteen"
        };

        private String convertLessThanOneThousand(int number) {
            String soFar;
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return soFar;
            }
            return numNames[number] + " Hundred" + soFar;
        }

        private String convertLessOne(int number, KWLCurrency currency) {
            String soFar;
            String val = currency.getAfterDecimalName();
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return " And " + soFar + " " + val;
            }
            return " And " + numNames[number] + " " + val + soFar;
        }

        public String convert(Double number, KWLCurrency currency, int countryLanguageId) {
            if (number == 0) {
                return "Zero";
            }
            
            String answer = "";
            
            if (countryLanguageId == Constants.OtherCountryLanguageId) { // For universal conversion of amount in words. i.e. in Billion,trillion etc
                answer = universalConvert(number, currency);
            } else if (countryLanguageId == Constants.CountryIndiaLanguageId) { // For Indian word format.ie. in lakhs, crores
                answer = indianConvert(number, currency);
            }
            return answer;
        }
            
        public String universalConvert(Double number, KWLCurrency currency) {
            boolean isNegative = false;
            if (number < 0) {
                isNegative = true;
                number = -1 * number;
            }
            String snumber = Double.toString(number);
            String mask = "000000000000.00";
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);
            int billions = Integer.parseInt(snumber.substring(0, 3));
            int millions = Integer.parseInt(snumber.substring(3, 6));
            int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
            int thousands = Integer.parseInt(snumber.substring(9, 12));
            int fractions = Integer.parseInt(snumber.substring(13, 15));
            String tradBillions;
            switch (billions) {
                case 0:
                    tradBillions = "";
                    break;
                case 1:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
                    break;
                default:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
            }
            String result = tradBillions;

            String tradMillions;
            switch (millions) {
                case 0:
                    tradMillions = "";
                    break;
                case 1:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
                    break;
                default:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
            }
            result = result + tradMillions;

            String tradHundredThousands;
            switch (hundredThousands) {
                case 0:
                    tradHundredThousands = "";
                    break;
                case 1:
                    tradHundredThousands = "One Thousand ";
                    break;
                default:
                    tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " Thousand ";
            }
            result = result + tradHundredThousands;
            String tradThousand;
            tradThousand = convertLessThanOneThousand(thousands);
            result = result + tradThousand;
            String paises;
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            result = result + paises; //to be done later
            result = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
            if (isNegative) {
                result = "Minus " + result;
            }
//            result = result.substring(0, 1).toUpperCase() + result.substring(1).toLowerCase(); // Make first letter of operand capital.
            return result;
        } 
             
        public String indianConvert(Double number, KWLCurrency currency) {
            boolean isNegative = false;
            if (number < 0) {
                isNegative = true;
                number = -1 * number;
            }
            String snumber = Double.toString(number);
            String mask = "000000000000000.00";  //ERP-17681
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);

            int n = Integer.parseInt(snumber.substring(0, 15));
            int fractions = Integer.parseInt(snumber.split("\\.").length != 0 ? snumber.split("\\.")[1] : "0");
            if (n == 0) {
                return "Zero";
            }
            String arr1[] = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
            String arr2[] = {"Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
            String unit[] = {"Arab", "Crore", "Lakh", "Thousand", "Hundred", ""};
            int factor[] = {1000000000, 10000000, 100000, 1000, 100, 1};
            String answer = "", paises = "";
            if (n < 0) {
                answer = "Minus";
                n = -n;
            }
            int quotient, units, tens;
            for (int i = 0; i < factor.length; i++) {
                quotient = n / factor[i];
                if (quotient > 0) {
                    if (quotient < 20) {
                        answer = answer + " " + arr1[quotient - 1];
                    } else {
                        units = quotient % 10;
                        tens = quotient / 10;
                        if (units > 0) {
                            answer = answer + " " + arr2[tens - 2] + " " + arr1[units - 1];
                        } else {
                            answer = answer + " " + arr2[tens - 2] + " ";
                        }
                    }
                    answer = answer + " " + unit[i];
                }
                n = n % factor[i];
            }
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            answer = answer + paises; //to be done later
            return answer.trim();
        }
    }
    
    public ModelAndView deleteRevalEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "", jeid = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String revalid = request.getParameter("revalId");
            String oldRevalId = request.getParameter("oldRevalId");
            KwlReturnObject result = accJournalEntryobj.deleteRevalEntry(revalid);
            List list = result.getEntityList();
            /**
             * If new added entries are deleted successfully than resetting
             * isrealised flag to false again as it was made true while fetching
             * reval records on Account Re-evaluation screen.If we do not reset
             * isrealised flag to false then no realised JE is passed. ERP-33179
             */
            if(result.getRecordTotalCount()>0){
                HashMap<String, Object> params = new HashMap<>();
                params.put("oldRevalId", oldRevalId);
                result = accJournalEntryobj.resetIsRealisedFlagofRevalHistory(params);
            }
            
            issuccess = true;
            msg = messageSource.getMessage("acc.je1.save", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been saved successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("id", jeid);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getRevalMonthYearStatus(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "", jeid = "";
        boolean issuccess = false;
        int count = 0;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Date revalDate= df.parse(request.getParameter("revaldate"));
            int accountType = Integer.parseInt(request.getParameter("accountType"));
            String currencyIDs = request.getParameter("currencyID");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = accJournalEntryobj.getRevalMonthYearStatus(revalDate, accountType, currencyIDs, companyid);
            List list = result.getEntityList();
            if (!list.isEmpty()) {
                count = list.size();
            }
            issuccess = true;
            // msg = messageSource.getMessage("acc.je1.save", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been saved successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("id", jeid);
                jobj.put("count", count);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

 
    public ModelAndView saveReverseJournalEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "", id = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JournalEntry je = saveReverseJournalEntry(request);
            issuccess = true;
            id = je.getID();
            msg = messageSource.getMessage("acc.je.ReverseJournalEntryhasbeenpostedsuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("id", id);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JournalEntry saveReverseJournalEntry(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry je = null;
        try {
            JSONArray jArr = new JSONArray();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String jeid = StringUtil.isNullOrEmpty(request.getParameter("jeId")) ? "" : request.getParameter("jeId");
            String memo = StringUtil.isNullOrEmpty(request.getParameter("memo")) ? "" : request.getParameter("memo");
	    String reverseEntryDate = !StringUtil.isNullOrEmpty(request.getParameter("entrydate")) ? request.getParameter("entrydate") : "";

            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            if (journalEntry != null) {

//                Set<JournalEntryDetail> journalEntryDetails = journalEntry.getDetails();
//                for (JournalEntryDetail journalEntryDetail : journalEntryDetails) {
//                    JSONObject object = new JSONObject();
//                    object.put("accountid", journalEntryDetail.getAccount().getID());
//                    object.put("amount", journalEntryDetail.getAmount());
//                    object.put("description", journalEntryDetail.getDescription());
//                    object.put("debit", (journalEntryDetail.isDebit()) ? false : true);
//                    object.put("srno", journalEntryDetail.getSrno());
//                    object.put("appliedGst", journalEntryDetail.getGstapplied()!=null?journalEntryDetail.getGstapplied().getID():null);
//                    jArr.put(object);
//                }

            
                double externalCurrencyRate = journalEntry.getExternalCurrencyRate();
                //For Reverse JE we ll save entry date by applying User Date Formatter.
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
                DateFormat df = authHandler.getDateOnlyFormat(request);
                String jeentryNumber = "";
                String jeIntegerPart = "";
                String jeDatePrefix = "";
                String jeDateAfterPrefix = "";
                String jeDateSuffix = "";
                String jeSeqFormatId = "";
                boolean jeautogenflag = false;
                Date entryDate = null;
                if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {
                    entryDate = df.parse(df.format(CompanyPreferencesCMN.removeTimefromDate(new Date()))); // Changes done to remove time from date
                } else if(!reverseEntryDate.equals("")){    //SDP-11553
                    entryDate = df.parse(reverseEntryDate);
                } else {
                    entryDate = df.parse(sdf.format(new Date()));
                }

                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                    JEFormatParams.put(Constants.companyKey, companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                    jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;
                }

                String costCenterId = (journalEntry.getCostcenter() != null) ? journalEntry.getCostcenter().toString() : "";

                Map<String, Object> jeDataMap = new HashMap<String, Object>();
                jeDataMap.put("df", df);
                jeDataMap.put(Constants.companyKey, companyid);
                jeDataMap.put("entrydate", entryDate);
                jeDataMap.put("memo", memo);
                jeDataMap.put("entrynumber", jeentryNumber);
                jeDataMap.put("autogenerated", jeautogenflag);
                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                jeDataMap.put("currencyid", (journalEntry.getCurrency() != null) ? journalEntry.getCurrency().getCurrencyID() : null);
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeDataMap.put("reversejournalentry", journalEntry.getID().toString());
                jeDataMap.put("createdby", journalEntry.getCreatedby()==null?null:journalEntry.getCreatedby().getUserID().toString());
                jeDataMap.put("isreverseje", true);
                jeDataMap.put("includeInGSTReport", journalEntry.isToIncludeInGSTReport());

                if (!StringUtil.isNullOrEmpty(costCenterId)) {
                    jeDataMap.put(CCConstants.JSON_costcenterid, costCenterId);
                }

//                jeDataMap.put("jedetails", jeDetails);
                KwlReturnObject rjeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                je = (JournalEntry) rjeresult.getEntityList().get(0);
                jeDataMap.put("jeid", je.getID());
//                KwlReturnObject jedresult = accJournalEntryobj.getJEDset(jArr, companyid,je);
//                HashSet jeDetails = (HashSet) jedresult.getEntityList().get(0);
//                je.setDetails(jeDetails);

                //Set<JournalEntryDetail> journalEntryDetails = journalEntry.getDetails();
                Set<JournalEntryDetail> rows = journalEntry.getDetails();
                Set<JournalEntryDetail> journalEntryDetails = new TreeSet<>(new SortJournalEntryDetailBySrNo());
                journalEntryDetails.addAll(rows);
                
                HashSet<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();                
                int srno = 1;
                for (JournalEntryDetail journalEntryDetail : journalEntryDetails) {
                    JSONObject object = new JSONObject();
                    object.put("accountid", journalEntryDetail.getAccount().getID());
                    object.put("amount", journalEntryDetail.getAmount());
                    object.put("description", journalEntryDetail.getDescription());
                    object.put("debit", (journalEntryDetail.isDebit()) ? false : true);
                    object.put("srno", srno++);
                    object.put("companyid", journalEntryDetail.getCompany().getCompanyID());
                    object.put("appliedGst", journalEntryDetail.getGstapplied() != null ? journalEntryDetail.getGstapplied().getID() : null);
                    object.put("jeid", je.getID());

                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(object);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);
                    if (journalEntryDetail.getAccJEDetailCustomData() != null) {
                        int NoOFRecords = accJournalEntryobj.saveCustomDataForReverseJE(jed.getID(), journalEntryDetail.getID(), false);
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("accjedetailcustomdata", jed.getID());
                        jedjson.put("jedid", jed.getID());
                        KwlReturnObject jedresult1 = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                    }
                    jArr.put(object);
                }
             
//                jeDataMap.put("accjecustomdataref", je.getID());
                jeDataMap.put("jedetails", jeDetails);
                jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
                if(!StringUtil.isNullOrEmpty(je.getID().toString())){
                    journalEntry.setIsOneTimeReverse(true);
                }
                accJournalEntryobj.updateReverseJournalEntryValue(journalEntry, je.getID().toString());
                if (journalEntry.getAccBillInvCustomData() != null) {
                    int NoOFRecords = accJournalEntryobj.saveCustomDataForReverseJE(je.getID(), journalEntry.getID(), true);
                    Map<String, Object> customjeDataMap = new HashMap<String, Object>();
                    customjeDataMap.put("accjecustomdataref", je.getID());
                    customjeDataMap.put("jeid", je.getID());
                    jeresult = accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                }
                auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + sessionHandlerImpl.getUserFullName(request) + " has reversed journal entry " + je.getEntryNumber(), request, je.getID());

                //Insert new entries again in optimized table.
                accJournalEntryobj.saveAccountJEs_optimized(je.getID());
            }
        } catch (ParseException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveReverseJournalEntry : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveReverseJournalEntry : " + ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, e);
            throw new AccountingException(e.getMessage());
        }
        return je;
    }

    public ModelAndView getJournalEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getJournalEntryMap(request);
            if (request.getParameter("groupid") != null && Boolean.FALSE.parseBoolean(request.getParameter("groupid"))) {
                requestParams.put("groupid", true);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isTemplate"))) {
                requestParams.put("isTemplate", Boolean.FALSE.parseBoolean(request.getParameter("isTemplate")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("typeValue"))) {
                String[] typeValues = request.getParameter("typeValue").split(",");
                String typeValuesString="";
                for (int i = 0; i < typeValues.length; i++) {
                    typeValuesString += "'"+typeValues[i]+"'"+",";
                }
                if(!typeValuesString.equals("")){
                    typeValuesString = typeValuesString.substring(0, typeValuesString.length()-1);
                    requestParams.put(Constants.Journal_Entry_Type, typeValuesString);
                }
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("ispendingAproval"))) {
                requestParams.put("ispendingAproval", Boolean.FALSE.parseBoolean(request.getParameter("ispendingAproval")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isPendingJEFlag))) {
                requestParams.put(Constants.isPendingJEFlag, Boolean.parseBoolean(request.getParameter(Constants.isPendingJEFlag)));
            }
            KwlReturnObject result = null;
            KwlReturnObject result1 = null;
            KwlReturnObject result2 = null;
            boolean withoutinventory = request.getParameter("withoutinventory").equals("true") ? true : false;
            if (request.getParameter("cashtype") != null) {
                String reporttype = request.getParameter("cashtype").toString();
                if (StringUtil.equal(JournalEntryConstants.CashReceiptJournal, reporttype)) {
                    requestParams.put(JournalEntryConstants.ReportClass, withoutinventory ? JournalEntryConstants.BillingReceipt : JournalEntryConstants.Receipt);
                    result1 = accJournalEntryobj.getJournalEntryForReports(requestParams);
                    requestParams.put(JournalEntryConstants.ReportClass, JournalEntryConstants.ASSET);
                    requestParams.put(JournalEntryConstants.ReportType, reporttype);
                    result2 = accJournalEntryobj.getJournalEntryForReports(requestParams);
                    List<JournalEntry> jeList1 = result1.getEntityList();
                    List<JournalEntry> jeList2 = result2.getEntityList();
                    List<JournalEntry> jeList3 = new ArrayList<JournalEntry>();
                    jeList3.addAll(jeList1);
                    jeList3.addAll(jeList2);
                    Collections.sort(jeList3);
                    result = new KwlReturnObject(true, "", null, jeList3, result1.getRecordTotalCount() + result2.getRecordTotalCount());
                } else if (StringUtil.equal(JournalEntryConstants.CashDisbursementJournal, reporttype)) {
                    requestParams.put(JournalEntryConstants.ReportClass, withoutinventory ? JournalEntryConstants.BillingPayment : JournalEntryConstants.Payment);
                    result1 = accJournalEntryobj.getJournalEntryForReports(requestParams);
                    requestParams.put(JournalEntryConstants.ReportClass, JournalEntryConstants.ASSET);
                    requestParams.put(JournalEntryConstants.ReportType, reporttype);
                    result2 = accJournalEntryobj.getJournalEntryForReports(requestParams);
                    List<JournalEntry> jeList1 = result1.getEntityList();
                    List<JournalEntry> jeList2 = result2.getEntityList();
                    List<JournalEntry> jeList3 = new ArrayList<JournalEntry>();
                    jeList3.addAll(jeList1);
                    jeList3.addAll(jeList2);
                    Collections.sort(jeList3);
                    result = new KwlReturnObject(true, "", null, jeList3, result1.getRecordTotalCount() + result2.getRecordTotalCount());
                } else if (StringUtil.equal(JournalEntryConstants.SalesReceivableJournal, reporttype)) {
                    requestParams.put(JournalEntryConstants.ReportClass, withoutinventory ? JournalEntryConstants.BillingInvoice : JournalEntryConstants.Invoice);
                    result = accJournalEntryobj.getJournalEntryForReports(requestParams);
                } else if (StringUtil.equal(JournalEntryConstants.PurchasePayableJournal, reporttype)) {
                    requestParams.put(JournalEntryConstants.ReportClass, withoutinventory ? JournalEntryConstants.BillingGoodsReceipt : JournalEntryConstants.GoodsReceipt);
                    result = accJournalEntryobj.getJournalEntryForReports(requestParams);
                } else {
                    result = accJournalEntryobj.getJournalEntry(requestParams);
                }
            } else {
                result = accJournalEntryobj.getJournalEntry(requestParams);
            }
//            Map<String, JSONArray> jeDetails = getJournalEntryDetailsMap(requestParams);
            jobj = getJournalEntryJson(requestParams, result.getEntityList());//jeDetails
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public HashMap<String, Object> getJournalEntryMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.start, request.getParameter(Constants.start));
        requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(JournalEntryConstants.LINKID, request.getParameter(JournalEntryConstants.LINKID));
        requestParams.put(JournalEntryConstants.DELETED, request.getParameter(JournalEntryConstants.DELETED));
        requestParams.put(JournalEntryConstants.NONDELETED, request.getParameter(JournalEntryConstants.NONDELETED));
        requestParams.put(Constants.isRepeatedFlag, request.getParameter(Constants.isRepeatedFlag));
        requestParams.put(CCConstants.REQ_costCenterId, request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
        return requestParams;
    }

    public JSONObject getJournalEntryJson(HashMap<String, Object> requestParams, List list) throws ServiceException {//, Map<String, JSONArray> jeDetails
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                JournalEntry entry = (JournalEntry) itr.next();
                Set<JournalEntryDetail> entryDetails=entry.getDetails();
                JSONObject obj = new JSONObject();
                obj.put("journalentryid", entry.getID());
                obj.put("ischequeprinted", entry.isChequeprinted());
                if(entry.getPaidTo()!=null){
                    obj.put("paidToCmb", entry.getPaidTo().getID());
                    obj.put("paidTo", entry.getPaidTo().getValue());
                }
                if(entry.getPaymentMethod()!=null){
                    obj.put("pmtmethod", entry.getPaymentMethod().getID());
                }
                
                obj.put("entryno", entry.getEntryNumber());
                obj.put("currencysymbol", entry.getCurrency() == null ? currency.getSymbol() : entry.getCurrency().getSymbol());
                obj.put("memo", entry.getMemo());
                obj.put("deleted", entry.isDeleted());
                obj.put("entrydate", df.format(entry.getEntryDate()));
                if (!entryDetails.isEmpty()) {
//                    obj.put("jeDetails", jeDetails.get(entry.getID()));
                    obj.put("jeDetails", getJEDetailsArray(requestParams, entryDetails));
                } else {
                    obj.put("jeDetails", "[]");
                }

                obj.put("typeValue", entry.getTypeValue());
                RepeatedJE repeatedJE = entry.getRepeateJE();
                obj.put("isRepeated", repeatedJE == null ? false : true);
                if (repeatedJE != null) {
                    obj.put("repeateid", repeatedJE.getId());
                    obj.put("interval", repeatedJE.getIntervalUnit());
                    obj.put("NoOfJEpost", repeatedJE.getNoOfJEpost());
                    obj.put("NoOfRemainJEpost", repeatedJE.getNoOfRemainJEpost());
                    obj.put("intervalType", repeatedJE.getIntervalType());
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
                    obj.put("startDate", sdf.format(repeatedJE.getStartDate()));
                    obj.put("nextDate", sdf.format(repeatedJE.getNextDate()));
                    obj.put("isactivate", repeatedJE.isIsActivate());
                    obj.put("ispendingapproval", repeatedJE.isIspendingapproval());
                    obj.put("approver", repeatedJE.getApprover());
                    obj.put("expireDate", repeatedJE.getExpireDate() == null ? "" : sdf.format(repeatedJE.getExpireDate()));
                    requestParams.put("parentJEId", entry.getID());
                    KwlReturnObject details = accJournalEntryobj.getRepeateJEDetails(requestParams);
                    List detailsList = details.getEntityList();
                    obj.put("childCount", detailsList.size());
                }
                if(entry.getPaymentMethod()!=null){
                    obj.put("pmtmethod", entry.getPaymentMethod().getID());
                    obj.put("pmtmethodtype", entry.getPaymentMethod().getDetailType());
                    obj.put("pmtmethodaccountname", (entry.getPaymentMethod().getAccount()!=null)?entry.getPaymentMethod().getAccount().getName():"");
                    obj.put("pmtmethodaccountid", (entry.getPaymentMethod().getAccount()!=null)?entry.getPaymentMethod().getAccount().getID():"");
                    if(entry.getTypeValue()== Constants.FundTransfer_Journal_Entry && entry.getRepeateJE()!=null){
                        obj.put("chequeOption", entry.getRepeateJE().isAutoGenerateChequeNumber());
                    } else {
                        obj.put("chequeOption", false);
                    }
                }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getJournalEntryJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView saveRepeateJEInfo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String repeateid = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        String detail = "";
        TransactionStatus status = txnManager.getTransaction(def);
        try {            
            GregorianCalendar gc = new GregorianCalendar(); //It returns actual Date object            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();  //Map for notification mail data
            String loginUserId = sessionHandlerImpl.getUserid(request);
            requestParams.put("loginUserId", loginUserId);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            int intervalUnit = Integer.parseInt(request.getParameter("interval"));
            dataMap.put("intervalUnit", intervalUnit);
            boolean isActivate = StringUtil.isNullOrEmpty(request.getParameter("isactivate"))?true:Boolean.parseBoolean(request.getParameter("isactivate"));
            int NoOfJEpost = Integer.parseInt(request.getParameter("NoOfJEpost"));
            dataMap.put("NoOfJEpost", NoOfJEpost);
            dataMap.put("intervalType", request.getParameter("intervalType"));
            Date startDate = df.parse(request.getParameter("startDate"));
            String action = "added";
            int chequeOption = 0;
            chequeOption = StringUtil.isNullOrEmpty(request.getParameter("chequeOption")) ? 1 : Integer.parseInt(request.getParameter("chequeOption"));
            //By default every recurring JE ll be considered as Pending for approval. So if user do recurring by mistake, he need not to worry about it. JE ll recur only on his approval            
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isedit")) ? false : Boolean.parseBoolean(request.getParameter("isedit"));
            if (!isEdit) {    
                //boolean ispendingapproval = true;
                String approver = "";
                int notifyme = StringUtil.isNullOrEmpty(request.getParameter("notifyme")) ? 1 : Integer.parseInt(request.getParameter("notifyme"));
                if (notifyme == 1) {  // 0 : Auto Recurring, 1: Pending Recurring JE
                    dataMap.put("isactivate", false);
                    approver = !StringUtil.isNullOrEmpty(request.getParameter("approver")) ? request.getParameter("approver") : "";
                    dataMap.put("approver", approver);
                    dataMap.put("ispendingapproval", true);    //1: Pending Recurring JE
                    requestParams.put("ispendingapproval", true);
                } else {    //Auto Entry
                    dataMap.put("approver", approver);
                    dataMap.put("isactivate", isActivate);  //isActivate=true means recurring invoice is in active mode.                    
                    dataMap.put("ispendingapproval", false);  
                    requestParams.put("ispendingapproval", false);
                }                
            }
            
            String repeateId = request.getParameter("repeateid");
            if (StringUtil.isNullOrEmpty(repeateId)) {
                dataMap.put("startDate", startDate);
                dataMap.put("nextDate", startDate);
                requestParams.put("nextDate", startDate);
                gc.setTime(startDate);
            } else {
                dataMap.put("id", repeateId);
                //Date nextDate = RepeatedJE.calculateNextDate(startDate, intervalUnit, request.getParameter("intervalType"));
                dataMap.put("nextDate", startDate); //In edit case, next generation date should not be increase.
                action = "updated";
                requestParams.put("nextDate", startDate);
                gc.setTime(startDate);
            }
            if (chequeOption == 1) {
                dataMap.put("autoGenerateChequeNumber", false);
            } else {
                dataMap.put("autoGenerateChequeNumber", true);
            }
            gc.add(Calendar.DAY_OF_YEAR, -1);
            Date prevDate = authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(gc.getTime()));
            dataMap.put("prevDate", prevDate);
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("expireDate"))) {
                dataMap.put("expireDate", df.parse(request.getParameter("expireDate")));
                requestParams.put("prevDate", prevDate);
            }
            KwlReturnObject rObj = accJournalEntryobj.saveRepeateJEInfo(dataMap);
            RepeatedJE rJE = (RepeatedJE) rObj.getEntityList().get(0);

            JSONObject JEjson = new JSONObject();
            JEjson.put("JEid", request.getParameter("JEId"));
            JEjson.put("repeateid", rJE.getId());
            accJournalEntryobj.updateJE(JEjson, null);
            if (!StringUtil.isNullOrEmpty(request.getParameter("detail"))) {
                repeateid = rJE.getId();
                int delcount = accJournalEntryobj.DelRepeateJEMemo(repeateid, "RepeatedJEID.id");
                detail = request.getParameter("detail");
                JSONArray arrMemo = new JSONArray(detail);
                for (int i = 0; i < arrMemo.length(); i++) {
                    JSONObject jsonmemo = arrMemo.getJSONObject(i);
                    HashMap<String, Object> dataMapformemo = new HashMap<String, Object>();
                    dataMapformemo.put("no", Integer.parseInt(jsonmemo.get("no").toString()));
                    /**
                     * Due to encode + is added into the Memo while get.SDP-14105.
                     */
                    //dataMapformemo.put("memo",jsonmemo.get("memo")!=null?URLEncoder.encode(jsonmemo.optString("memo"), Constants.DECODE_ENCODE_FORMAT):"");//Allow chinese characters ERP-24329
                    dataMapformemo.put("memo",jsonmemo.optString("memo",""));
                    dataMapformemo.put("repeatedjeid", rJE.getId());
                    KwlReturnObject savememo = accJournalEntryobj.saveRepeateJEMemo(dataMapformemo);
                }
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("chequedetail"))) {
                repeateid = rJE.getId();
                int delcount = accJournalEntryobj.DelRepeateJEChequeDetails(repeateid);
                detail = request.getParameter("chequedetail");
                JSONArray arrCheque = new JSONArray(detail);
                for (int i = 0; i < arrCheque.length(); i++) {
                    JSONObject jsonCheque = arrCheque.getJSONObject(i);
                    HashMap<String, Object> dataMapforCheque = new HashMap<String, Object>();
                    dataMapforCheque.put("no", Integer.parseInt(jsonCheque.get("no").toString()));
                    dataMapforCheque.put("date", df.parse(jsonCheque.getString("date")));
                    dataMapforCheque.put("RepeatedJEID", rJE.getId());
                    if (chequeOption == 1) {
                        dataMapforCheque.put("chequenumber", jsonCheque.get("chequeno").toString());
                    }
                    KwlReturnObject saveChequeData = accJournalEntryobj.saveRepeateJEChequeDetail(dataMapforCheque);
                }
            }
            String entryno = request.getParameter("entryno");
            requestParams.put("entryno", entryno);
            auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " Recursive journal entry " + entryno, request, rJE.getId());
            msg = messageSource.getMessage("acc.je.saverecurring", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            SendMail(requestParams);    //Notification Mail
            txnManager.commit(status);


        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getJERepeateDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String parentSOId = request.getParameter("parentid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("parentJEId", parentSOId);
            KwlReturnObject details = accJournalEntryobj.getRepeateJEDetails(requestParams);
            List detailsList = details.getEntityList();
            Iterator itr = detailsList.iterator();

            JSONArray JArr = new JSONArray();
            while (itr.hasNext()) {
                JournalEntry repeatedSO = (JournalEntry) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("JEId", repeatedSO.getID());
                obj.put("JENo", repeatedSO.getEntryNumber());
                obj.put("parentJEId", parentSOId);
                JArr.put(obj);
            }

            jobj.put("data", JArr);
            jobj.put("count", details.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getJERepeateMemoDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String repeateid = request.getParameter("repeateid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("repeateid", repeateid);
            requestParams.put("memofor", request.getParameter("memofor"));
            KwlReturnObject details = accJournalEntryobj.getJERepeateMemoDetails(requestParams);
            List detailsList = details.getEntityList();
            Iterator itr = detailsList.iterator();

            JSONArray JArr = new JSONArray();
            while (itr.hasNext()) {
                RepeatedJEMemo RM = (RepeatedJEMemo) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("no", RM.getCount());
                obj.put("memo", !StringUtil.isNullOrEmpty(RM.getMemo())?StringUtil.DecodeText(RM.getMemo()):"");//Allow chinese characters ERP-24329
                obj.put("id", RM.getId());
                obj.put("repeateid", RM.getRepeatedJEID());
                obj.put("RepeatedInvoiceID", RM.getRepeatedInvoiceID());
                obj.put("RepeatedSOID", RM.getRepeatedSOID());
                obj.put("RepeatedPaymentId", RM.getRepeatedPaymentId()!=null?RM.getRepeatedPaymentId():"");
                JArr.put(obj);
            }

            jobj.put("data", JArr);
            jobj.put("count", details.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getJournalEntryJsonForExport(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JournalEntry entry = (JournalEntry) row[0];
                JournalEntryDetail jed = (JournalEntryDetail) row[1];
                String currId = entry.getCurrency() == null ? currency.getCurrencyID() : entry.getCurrency().getCurrencyID();
                JSONObject obj = new JSONObject();
                obj.put("journalentryid", entry.getID());
                obj.put("entryno", entry.getEntryNumber());
                obj.put("currencysymbol", entry.getCurrency() == null ? currency.getSymbol() : entry.getCurrency().getSymbol());
                obj.put("memo", entry.getMemo());
                obj.put("deleted", entry.isDeleted());
                obj.put("entrydate", df.format(entry.getEntryDate()));
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, jed.getAmount(), currId, entry.getEntryDate(), entry.getExternalCurrencyRate());
                if (jed.isDebit()) {
                    obj.put("debitAmount", bAmt.getEntityList().get(0));
                    obj.put("creditAmount", 0.0);
                } else {
                    obj.put("debitAmount", 0.0);
                    obj.put("creditAmount", bAmt.getEntityList().get(0));
                }
                obj.put("accountName", jed.getAccount().getName());

                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getJournalEntryJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView getJournalEntryDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("dateformat", authHandler.getDateOnlyFormat(request));
            requestParams.put("journalentryid", request.getParameter("journalentryid"));

            JSONArray DataJArr = getJournalEntryDetails(requestParams);
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accJournalEntryController.getJournalEntryDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getJournalEntryDetails(HashMap<String, Object> request) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String jeid = (String) request.get("journalentryid");
//            Set details = ((JournalEntry)session.get(JournalEntry.class, request.getParameter("journalentryid"))).getDetails();
            KwlReturnObject result = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
            JournalEntry je = (JournalEntry) result.getEntityList().get(0);
//            Set details = ((JournalEntry) result.getEntityList().get(0)).getDetails();

//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), (String) request.get(Constants.globalCurrencyKey));
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);

//            Iterator itr = details.iterator();
            HashMap<String, Object> jeRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("journalEntry.ID");
            filter_params.add(je.getID());
            order_by.add("debit");
            order_type.add("desc");
            jeRequestParams.put("filter_names", filter_names);
            jeRequestParams.put("filter_params", filter_params);
            jeRequestParams.put("order_by", order_by);
            jeRequestParams.put("order_type", order_type);
            KwlReturnObject jedresult = accJournalEntryobj.getJournalEntryDetails(jeRequestParams);
            Iterator itr = jedresult.getEntityList().iterator();

            while (itr.hasNext()) {
                JournalEntryDetail entry = (JournalEntryDetail) itr.next();
                String currencyid = entry.getJournalEntry().getCurrency() == null ? currency.getCurrencyID() : entry.getJournalEntry().getCurrency().getCurrencyID();
                JSONObject obj = new JSONObject();
                obj.put("srno", entry.getSrno());
                obj.put("accountid", entry.getAccount().getID());
                String accname = StringUtil.isNullOrEmpty(entry.getAccount().getAcccode()) ? entry.getAccount().getName() : "[" + entry.getAccount().getAcccode() + "] " + entry.getAccount().getName();
                obj.put("accountname", accname);
                obj.put("currencysymbol", entry.getJournalEntry().getCurrency() == null ? currency.getSymbol() : entry.getJournalEntry().getCurrency().getSymbol());
                obj.put("description", entry.getDescription());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(request, entry.getAmount(), currencyid, entry.getJournalEntry().getEntryDate(), entry.getJournalEntry().getExternalCurrencyRate());
                if (entry.isDebit() == true) {
                    obj.put("debit", "Debit");
//                    obj.put("d_amount", CompanyHandler.getCurrencyToBaseAmount(session,request,entry.getAmount(),currencyid,entry.getJournalEntry().getEntryDate()));
                    obj.put("d_amount", bAmt.getEntityList().get(0));
                } else {
                    obj.put("debit", "Credit");
//                    obj.put("c_amount",CompanyHandler.getCurrencyToBaseAmount(session,request,entry.getAmount(),currencyid,entry.getJournalEntry().getEntryDate()));
                    obj.put("c_amount", bAmt.getEntityList().get(0));
                }
                jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getJournalEntryDetails : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getJEDetailsArray(HashMap<String, Object> request,Set<JournalEntryDetail> entryDetails) throws ServiceException {
        JSONArray jeDetailsMap = new JSONArray();
        try {
            KwlReturnObject result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), (String) request.get(Constants.globalCurrencyKey));
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);      
            Iterator itr = entryDetails.iterator();
            while (itr.hasNext()) {
                JournalEntryDetail entry = (JournalEntryDetail) itr.next();
                String currencyid = entry.getJournalEntry().getCurrency() == null ? currency.getCurrencyID() : entry.getJournalEntry().getCurrency().getCurrencyID();
                JSONObject obj = new JSONObject();
                obj.put("srno", entry.getSrno());
                obj.put("accountid", entry.getAccount().getID());
                String accname = StringUtil.isNullOrEmpty(entry.getAccount().getAcccode()) ? entry.getAccount().getName() : "[" + entry.getAccount().getAcccode() + "] " + entry.getAccount().getName();
                obj.put("accountname", accname);
                obj.put("currencysymbol", entry.getJournalEntry().getCurrency() == null ? currency.getSymbol() : entry.getJournalEntry().getCurrency().getSymbol());
                obj.put("description", entry.getDescription());
                obj.put("jeId", entry.getJournalEntry().getID());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(request, entry.getAmount(), currencyid, entry.getJournalEntry().getEntryDate(), entry.getJournalEntry().getExternalCurrencyRate());
                if (entry.isDebit() == true) {
                    obj.put("debit", "Debit");
                    obj.put("d_amount", bAmt.getEntityList().get(0));
                } else {
                    obj.put("debit", "Credit");
                    obj.put("c_amount", bAmt.getEntityList().get(0));
                }
                jeDetailsMap.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getJournalEntryDetails : " + ex.getMessage(), ex);
        }
        return jeDetailsMap;
    }

    public Map<String, Object> exportJournalEntry(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        try {

            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject compresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) compresult.getEntityList().get(0);
            List<JournalEntryReportDTO> jeRptList = new ArrayList<JournalEntryReportDTO>();
            JSONArray store = jobj.getJSONArray("data");

            for (int i = 0; i < store.length(); i++) {
                JSONObject temp = store.getJSONObject(i);
                JournalEntryReportDTO jeRpt = new JournalEntryReportDTO();
                jeRpt.setEntryNumber(temp.getString("entryno"));
                jeRpt.setEntryDate(temp.getString("entrydate"));
                jeRpt.setMemo(temp.getString("memo"));
                jeRpt.setAccountName(temp.getString("accountName"));
                jeRpt.setCreditAmount(temp.getDouble("creditAmount"));
                jeRpt.setDebitAmount(temp.getDouble("debitAmount"));
                jeRptList.add(jeRpt);
            }

            JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(jeRptList);
            parameterMap.put("datasource", jrBeanCollectionDataSource);
            String symbol = "";
            try {
                symbol = new Character((char) Integer.parseInt(currency.getHtmlcode(), 16)).toString();
            } catch (Exception e) {
                symbol = currency.getHtmlcode();
            }
            parameterMap.put("CURRENCY_NAME", symbol);
            parameterMap.put("COMPANY_LOGO_IMAGE_PATH", ProfileImageServlet.getProfileImagePath(request, true, null));
            parameterMap.put("COMPANY_ADDRESS", company.getAddress());
            parameterMap.put("COMPANY_NAME", company.getCompanyName());

        } catch (ServiceException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return parameterMap;
    }

    public ModelAndView exportJournalEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        ModelAndView mav = null;
        try {
            HashMap<String, Object> requestParams = getJournalEntryMap(request);
            requestParams.put("exportQuery", true);
            if (request.getParameter("groupid") != null && Boolean.parseBoolean(request.getParameter("groupid"))) {
                requestParams.put("groupid", true);
            }
            KwlReturnObject result = null;
            KwlReturnObject result1 = null;
            KwlReturnObject result2 = null;
            boolean withoutinventory = request.getParameter("withoutinventory").equals("true") ? true : false;
            if (request.getParameter("cashtype") != null) {
                String reporttype = request.getParameter("cashtype").toString();
                if (StringUtil.equal(JournalEntryConstants.CashReceiptJournal, reporttype)) {
                    requestParams.put(JournalEntryConstants.ReportClass, withoutinventory ? JournalEntryConstants.BillingReceipt : JournalEntryConstants.Receipt);
                    result1 = accJournalEntryobj.getJournalEntryForReports(requestParams);
                    requestParams.put(JournalEntryConstants.ReportClass, JournalEntryConstants.ASSET);
                    requestParams.put(JournalEntryConstants.ReportType, reporttype);
                    result2 = accJournalEntryobj.getJournalEntryForReports(requestParams);
                    List jeList1 = result1.getEntityList();
                    List jeList2 = result2.getEntityList();
                    List jeList3 = new ArrayList();
                    jeList3.addAll(jeList1);
                    jeList3.addAll(jeList2);
                    Collections.sort(jeList3, new ExportJEComparator());
                    result = new KwlReturnObject(true, "", null, jeList3, result1.getRecordTotalCount() + result2.getRecordTotalCount());
                } else if (StringUtil.equal(JournalEntryConstants.CashDisbursementJournal, reporttype)) {
                    requestParams.put(JournalEntryConstants.ReportClass, withoutinventory ? JournalEntryConstants.BillingPayment : JournalEntryConstants.Payment);
                    result1 = accJournalEntryobj.getJournalEntryForReports(requestParams);
                    requestParams.put(JournalEntryConstants.ReportClass, JournalEntryConstants.ASSET);
                    requestParams.put(JournalEntryConstants.ReportType, reporttype);
                    result2 = accJournalEntryobj.getJournalEntryForReports(requestParams);
                    List jeList1 = result1.getEntityList();
                    List jeList2 = result2.getEntityList();
                    List jeList3 = new ArrayList();
                    jeList3.addAll(jeList1);
                    jeList3.addAll(jeList2);
                    Collections.sort(jeList3, new ExportJEComparator());
                    result = new KwlReturnObject(true, "", null, jeList3, result1.getRecordTotalCount() + result2.getRecordTotalCount());
                } else if (StringUtil.equal(JournalEntryConstants.SalesReceivableJournal, reporttype)) {
                    requestParams.put(JournalEntryConstants.ReportClass, withoutinventory ? JournalEntryConstants.BillingInvoice : JournalEntryConstants.Invoice);
                    result = accJournalEntryobj.getJournalEntryForReports(requestParams);
                } else if (StringUtil.equal(JournalEntryConstants.PurchasePayableJournal, reporttype)) {
                    requestParams.put(JournalEntryConstants.ReportClass, withoutinventory ? JournalEntryConstants.BillingGoodsReceipt : JournalEntryConstants.GoodsReceipt);
                    result = accJournalEntryobj.getJournalEntryForReports(requestParams);
                } else {
                    result = accJournalEntryobj.getJournalEntry(requestParams);
                }
            } else {
                result = accJournalEntryobj.getJournalEntry(requestParams);
            }
            jobj = getJournalEntryJsonForExport(requestParams, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "csv")) {
                exportDaoObj.processRequest(request, response, jobj);
                mav = new ModelAndView(view, "model", jobj.toString());
            } else {

                Map<String, Object> paramMap = exportJournalEntry(request, response, jobj);
                if (StringUtil.equal(fileType, "print")) {
                    String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                    jobj.put("GenerateDate", GenerateDate);
                    exportDaoObj.processRequest(request, response, jobj);
                    mav = new ModelAndView("jsonView-empty", "model", jobj.toString());
                } else {
                    mav = new ModelAndView("pdfJournalEntry", paramMap);
                }
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return mav;
    }

    public ModelAndView getJournalEntryDetailsForFinanceReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("dateformat", authHandler.getDateOnlyFormat(request));
            requestParams.put("journalentryid", request.getParameter("journalentryid"));

            JSONArray DataJArr = getJournalEntryDetails(requestParams);
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accJournalEntryController.getJournalEntryDetails : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Inner class is used to sort the jedetail by serial number.
     */
    public class SortJournalEntryDetailBySrNo implements Comparator<JournalEntryDetail>{

        @Override
        public int compare(JournalEntryDetail JED1, JournalEntryDetail JED2) {
            if (JED1.getSrno() < JED2.getSrno()) {
                return 1;
            } else {
                return -1;
            }   
        }
    }
    private class ExportJEComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {

            int result = 0;
            Object[] row1 = (Object[]) o1;
            JournalEntry entry1 = (JournalEntry) row1[0];
            Object[] row2 = (Object[]) o2;
            JournalEntry entry2 = (JournalEntry) row2[0];

            if (entry1.getCreatedOn() < entry2.getCreatedOn()) {
                result = 1;
            } else if (entry1.getCreatedOn() > entry2.getCreatedOn()) {
                result = -1;
            } else {
                result = 0;
            }

            return result;
        }
    }
    
    //Send notification mail on set of Recurring JE
    public void SendMail(HashMap requestParams) throws ServiceException {
        String loginUserId = (String) requestParams.get("loginUserId");
        User user = (User) accJournalEntryobj.getUserObject(loginUserId);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), user.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        String sendorInfo = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        boolean ispendingapproval = (Boolean) requestParams.get("ispendingapproval");
        String entryno = (String) requestParams.get("entryno");
        SimpleDateFormat sdf = new SimpleDateFormat();
        //String nextDate = sdf.format((Date)requestParams.get(sdf));
        String cEmail = user.getEmailID() != null ? user.getEmailID() : "";
        //String cEmail = "vaibhav.patil@krawlernetworks.com";
        if (!StringUtil.isNullOrEmpty(cEmail)) {
            try {
                String subject = "Recurring Journal Entry Status Notification";
                //String sendorInfo = "admin@deskera.com";
                String htmlTextC = "";
                htmlTextC += "<br/>Hello " + user.getFirstName() + "<br/>";
                if (ispendingapproval) {
                    htmlTextC += "<br/>Journal Entry <b>\"" + entryno + "\"</b> has been set recurring successfully. <br/><br/>";
                } else {
                    htmlTextC += "<br/>Journal Entry <b>\"" + entryno + "\"</b> has been set recurring successfully.<br/><br/>";
                }
                htmlTextC += "<br/>Regards,<br/>";
                htmlTextC += "<br/>ERP System<br/>";
                htmlTextC += "<br/><br/>";
                htmlTextC += "<br/>This is an auto generated email. Do not reply<br/>";

                String plainMsgC = "";
                plainMsgC += "\nHello " + user.getFirstName() + "\n";
                if (ispendingapproval) {
                    plainMsgC += "\nJournal Entry <b>\"" + entryno + "\"</b> has been generated successfully. \n\n";
                } else {
                    plainMsgC += "\nJournal Entry <b>\"" + entryno + "\"</b> has been generated successfully. \n\n";
                }
                plainMsgC += "\nRegards,\n";
                plainMsgC += "\nERP System\n";
                plainMsgC += "\n\n";
                plainMsgC += "\nThis is an auto generated email. Do not reply.\n";

                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
            } catch (Exception ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

    }//sendMail

//**************Functions to insert / update existing JE's entries in jedetails_optimized tables*********
    public ModelAndView saveAccountJEs_optimized(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "", id = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveAccountJEs_optimized(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.je1.save", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been saved successfully";
            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("id", id);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveAccountJEs_optimized(HttpServletRequest request) throws ServiceException {
        double amount = 0;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            HashMap<String, String> jeMap = new HashMap<String, String>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String companyname = sessionHandlerImpl.getCompanyName(request);
            String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
            Date startDate = null;
            Date endDate = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            HashMap<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(Constants.companyKey, companyid);
//            filterParams.put("groupid", group.getID());
//            filterParams.put("parent", null);
            KwlReturnObject accresult = accAccountDAOobj.getAccountEntry(filterParams);
            List list = accresult.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Account account = (Account) itr.next();
                String accountid = account.getID();
                KwlReturnObject abresult1 = accJournalEntryobj.getAccountJEs(accountid, startDate, endDate);
                List list1 = abresult1.getEntityList();
                Iterator itr1 = list1.iterator();
                while (itr1.hasNext()) {
                    Object[] row = (Object[]) itr1.next();
                    JournalEntryDetail jed = (JournalEntryDetail) row[1];
                    JournalEntry je = jed.getJournalEntry();
                    String jeid = je.getID();
                    String entryDate = sdf.format(je.getEntryDate());
                    String costCenterId = (je.getCostcenter() != null ? je.getCostcenter().getID() : "");
                    String fromcurrencyid = (je.getCurrency() == null ? gcurrencyid : je.getCurrency().getCurrencyID());
                    KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ((Double) row[0]).doubleValue(), fromcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                    amount = (Double) crresult.getEntityList().get(0);

                    if (amount != 0) {
                        //Update amount in new jedetails table
                        accJournalEntryobj.saveAccountJEs_optimized(accountid, companyid, entryDate, costCenterId, amount);
                    }
                    if (!jeMap.containsKey(jeid)) {
                        jeMap.put(jeid, jeid);
                    }
                }
            }
            itr = jeMap.keySet().iterator();
            while (itr.hasNext()) {
                String jeid = (String) itr.next();
                accJournalEntryobj.setJEs_optimizedflag(jeid);
            }

            accJournalEntryobj.setCompany_optimizedflag(companyid);

            KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) result.getEntityList().get(0);
            Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
            String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
            SendMailHandler.postMail(new String[]{"sagar.ahire@deskera.com", "suresh.sonawane@deskera.com"}, "Data optimization script completed for " + companyname, "Data optimization script completed successfully for " + companyname,
                    "Data optimization script completed successfully for " + companyname, fromEmailId, new String[]{}, smtpConfigMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAccountBalance : " + ex.getMessage(), ex);
        }
//        return amount;
    }

    public ModelAndView calculateAccountJEs_optimized(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "", id = "";
        boolean issuccess = false;
        try {
            calculateAccountJEs_optimized(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.je1.save", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been saved successfully";            
//        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
//            msg = ex.getMessage();
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("id", id);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void calculateAccountJEs_optimized(HttpServletRequest request) throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;

        double amount = 0;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            HashMap<String, String> jeMap = new HashMap<String, String>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String companyname = sessionHandlerImpl.getCompanyName(request);
            String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
            Date startDate = null;
            Date endDate = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            HashMap<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(Constants.companyKey, companyid);
            KwlReturnObject accresult = accAccountDAOobj.getAccountEntry(filterParams);
            List list = accresult.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Account account = (Account) itr.next();
                try {
                    status = txnManager.getTransaction(def);

                    String accountid = account.getID();
                    KwlReturnObject abresult1 = accJournalEntryobj.getAccountJEs(accountid, startDate, endDate);
                    List list1 = abresult1.getEntityList();
                    Iterator itr1 = list1.iterator();
                    while (itr1.hasNext()) {
                        Object[] row = (Object[]) itr1.next();
                        JournalEntryDetail jed = (JournalEntryDetail) row[1];
                        JournalEntry je = jed.getJournalEntry();
                        String jeid = je.getID();
                        String entryDate = sdf.format(je.getEntryDate());
                        String costCenterId = (je.getCostcenter() != null ? je.getCostcenter().getID() : "");
                        String fromcurrencyid = (je.getCurrency() == null ? gcurrencyid : je.getCurrency().getCurrencyID());
                        KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ((Double) row[0]).doubleValue(), fromcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                        amount = (Double) crresult.getEntityList().get(0);

                        if (amount != 0) {
                            //Update amount in new jedetails table
                            accJournalEntryobj.saveAccountJEs_optimized(accountid, companyid, entryDate, costCenterId, amount);
                        }
                        if (!jeMap.containsKey(jeid)) {
                            jeMap.put(jeid, jeid);
                        }
                    }
                    txnManager.commit(status);
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            status = txnManager.getTransaction(def);
            itr = jeMap.keySet().iterator();
            while (itr.hasNext()) {
                String jeid = (String) itr.next();
                accJournalEntryobj.setJEs_optimizedflag(jeid);
            }
            accJournalEntryobj.setCompany_optimizedflag(companyid);

            KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) result.getEntityList().get(0);
            Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
            String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
            SendMailHandler.postMail(new String[]{"sagar.ahire@deskera.com", "suresh.sonawane@deskera.com"}, "Data optimization script completed for " + companyname, "Data optimization script completed successfully for " + companyname,
                    "Data optimization script completed successfully for " + companyname, fromEmailId, new String[]{}, smtpConfigMap);
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            throw ServiceException.FAILURE("getAccountBalance : " + ex.getMessage(), ex);
        }
//        return amount;
    }
        public ModelAndView printCheck(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobjDetails = new JSONObject();
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        ChequeLayout chequeLayout=null;
        String msg = "";
        JSONObject chequeobj=new JSONObject();
        boolean issuccess = false;
        boolean isnewlayout = false;
        try {
            
            String  jeid=request.getParameter("jeid");
            String jeno=request.getParameter("jeno");
            String chequeno=request.getParameter("chequeno");
            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("paymentMethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), payMethod.getCompany().getCompanyID());
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            if (!StringUtil.isNullOrEmpty(payMethod.getID())) {
                requestParams.put("bankid", payMethod.getID());
            }
            DateFormat DATE_FORMAT = new SimpleDateFormat(Constants.DEFAULT_FORMAT_CHECK);
            String prefixbeforamt = "";
            String companyid=payMethod.getCompany().getCompanyID();
            KwlReturnObject result1 = accPaymentDAOobj.getChequeLayout(requestParams);
            List list = result1.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                chequeLayout = (ChequeLayout) itr.next();
                chequeobj = new JSONObject(chequeLayout.getCoordinateinfo());
                jobjDetails.put("dateLeft", chequeobj.optString("dateLeft","0"));
                jobjDetails.put("nameLeft", chequeobj.optString("nameLeft","0"));
                jobjDetails.put("amtinwordLeft", chequeobj.optString("amtinwordLeft","0"));
                jobjDetails.put("amtinwordLeftLine2", chequeobj.optString("amtinwordLeftLine2","0"));
                jobjDetails.put("amtLeft", chequeobj.optString("amtLeft","0"));
                jobjDetails.put("dateTop", chequeobj.optString("dateTop","0"));
                jobjDetails.put("nameTop", chequeobj.optString("nameTop","0"));
                jobjDetails.put("amtinwordTop", chequeobj.optString("amtinwordTop","0"));
                jobjDetails.put("amtinwordTopLine2", chequeobj.optString("amtinwordTopLine2","0"));
                jobjDetails.put("amtTop", chequeobj.optString("amtTop","0"));
                String dateformat = chequeLayout.getDateFormat().getJavaForm();
                /*
                If 'AddCharacterInCheckDate' is true then don't remove '/' or '-' from check Date
               */
                if (!chequeLayout.isAddCharacterInCheckDate()) {
                    dateformat = dateformat.replaceAll("/", "");
                    dateformat = dateformat.replaceAll("-", "");
                }
                DATE_FORMAT = new SimpleDateFormat(dateformat);
                prefixbeforamt = chequeLayout.getAppendcharacter();
                isnewlayout = chequeLayout.isIsnewlayout();
            }
            Date creationDate = new Date(request.getParameter("Printdate"));
            String date = DATE_FORMAT.format(creationDate);
            String formatted_date_with_spaces = "";
            if (chequeLayout!=null && chequeLayout.isAddCharacterInCheckDate()) {
                formatted_date_with_spaces = date;
            } else {
                for (int i = 0; i < date.length(); i++) {
                    formatted_date_with_spaces += date.charAt(i);
                    formatted_date_with_spaces += isnewlayout ? "&nbsp;&nbsp;&nbsp;" : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                }
            }
               
            String netinword = "";
            DecimalFormat df=new DecimalFormat("#,###,###,##0.00");
  
            String amount2=request.getParameter("amount");
            String basecurrency=request.getParameter("currencyid");
            KwlReturnObject result2 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result2.getEntityList().get(0);
            netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amount2)), currency,countryLanguageId);
            String[] amount = {"amount", df.format(Double.parseDouble(amount2))};
            String[] amount1 = {"amountinword", netinword};
            String s = StringUtil.DecodeText(request.getParameter("name"));
            String[] accName = {"accountName", s};
            jobjDetails.put(amount[0], prefixbeforamt+amount[1]);
            String amount_first_line = "";
            String amount_second_line = "";
            String action=" Only.";
                if (amount1[1].length() > 34 && amount1[1].charAt(34) == ' ') {
                    amount_first_line = amount1[1].substring(0, 34);
                    amount_second_line = amount1[1].substring(34, amount1[1].length());
                    jobjDetails.put(amount1[0], amount_first_line );
                    jobjDetails.put("amountinword1", amount_second_line+action);
                } else if (amount1[1].length() > 34) {
                    amount_first_line = amount1[1].substring(0, 34);
                    amount_first_line = amount1[1].substring(0, amount_first_line.lastIndexOf(" "));
                    amount_second_line = amount1[1].substring(amount_first_line.length(), amount1[1].length());
                    jobjDetails.put(amount1[0], amount_first_line );
                    jobjDetails.put("amountinword1", amount_second_line+action);
                } else {
                    if(amount1[1].length()<27){
                      jobjDetails.put(amount1[0], amount1[1]+action); 
                      jobjDetails.put("amountinword1", "");
                    }
                    else{
                        jobjDetails.put(amount1[0], amount1[1]); 
                        jobjDetails.put("amountinword1", action);   
                    }
                    
                }
            jobjDetails.put(accName[0], accName[1]);
            jobjDetails.put("date", formatted_date_with_spaces);
            jobjDetails.put("isnewlayout", isnewlayout);
            
            
            boolean isFontStylePresent = chequeobj.has("fontStyle") && !StringUtil.isNullOrEmpty(chequeobj.getString("fontStyle")) ? true : false;
            String fontStyle = chequeobj.has("fontStyle") && !StringUtil.isNullOrEmpty(chequeobj.getString("fontStyle")) ? chequeobj.getString("fontStyle") : "";
            char fontStyleChar;
            if (fontStyle.equals("1")) {
                fontStyleChar = 'b';
            } else if (fontStyle.equals("2")) {
                fontStyleChar = 'i';
                    } else {
                fontStyleChar = 'p';
                    }
                    
                    
            //for name
            if (chequeobj.has("dateFontSize") || isFontStylePresent) {
                if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("dateFontSize"))) {
                    formatted_date_with_spaces = "<font size=" + chequeobj.getString("dateFontSize") + "><" + fontStyleChar + ">" + formatted_date_with_spaces + "</" + fontStyleChar + "></font> ";
                    jobjDetails.put("date", formatted_date_with_spaces);
                } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("dateFontSize"))) {
                   formatted_date_with_spaces = "<font size=" + chequeobj.getString("dateFontSize") + ">" + formatted_date_with_spaces + "</font> ";
                    jobjDetails.put("date", formatted_date_with_spaces);
                } else {
                    formatted_date_with_spaces = "<" + fontStyleChar + ">" + formatted_date_with_spaces + "</" + fontStyleChar + ">";
                    jobjDetails.put("date", formatted_date_with_spaces);

                }
            }
            //for name
            if (chequeobj.has("nameFontSize") || isFontStylePresent) {
                if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("nameFontSize"))) {
                    accName[1] = "<font size=" + chequeobj.getString("nameFontSize") + "><" + fontStyleChar + ">" + accName[1] + "</" + fontStyleChar + "></font> ";
                    jobjDetails.put(accName[0], accName[1]);
                } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("nameFontSize"))) {
                    accName[1] = "<font size=" + chequeobj.getString("nameFontSize") + ">" + accName[1] + "</font> ";
                    jobjDetails.put(accName[0], accName[1]);
                } else {
                    accName[1] = "<" + fontStyleChar + ">" + accName[1] + "</" + fontStyleChar + ">";
                    jobjDetails.put(accName[0], accName[1]);

                }
            }
            
            //for amount in words
            if (chequeobj.has("amountInWordsFontSize") || isFontStylePresent) {
                if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("amountInWordsFontSize"))) {
                    amount_first_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + "><" + fontStyleChar + ">" + amount_first_line + "</" + fontStyleChar + "></font> ";
                    amount_second_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + "><" + fontStyleChar + ">" + amount_second_line +" "+action+ "</" + fontStyleChar + "></font> ";
                     if (amount1[1].length() > 34) {
                        jobjDetails.put(amount1[0], amount_first_line);
                        jobjDetails.put("amountinword1", amount_second_line);
                    } else if (amount1[1].length() < 27) {
                        amount1[1] = "<font size=" + chequeobj.getString("amountInWordsFontSize") + "><" + fontStyleChar + ">" + amount1[1] +" "+action+ "</" + fontStyleChar + "></font> ";
                        jobjDetails.put(amount1[0], amount1[1]);
                        jobjDetails.put("amountinword1", "");
                    }
                } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("amountInWordsFontSize"))) {
                    amount_first_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + ">" + amount_first_line + "</font> ";
                    amount_second_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + ">" + amount_second_line +" "+action+ "</font> ";
                    if (amount1[1].length() > 34) {
                        jobjDetails.put(amount1[0], amount_first_line);
                        jobjDetails.put("amountinword1", amount_second_line);
                    } else if (amount1[1].length() < 27) {
                        amount1[1] = "<font size=" + chequeobj.getString("amountInWordsFontSize") + ">" + amount1[1] +" "+action+ "</font> ";
                        jobjDetails.put(amount1[0], amount1[1]);
                        jobjDetails.put("amountinword1", "");
                    }
                } else {
                    amount_first_line = "<" + fontStyleChar + ">" + amount_first_line + "</" + fontStyleChar + ">";
                    amount_second_line = "<" + fontStyleChar + ">" + amount_second_line +" "+action+ "</" + fontStyleChar + ">";
                     if (amount1[1].length() > 34) {
                        jobjDetails.put(amount1[0], amount_first_line);
                        jobjDetails.put("amountinword1", amount_second_line);
                    } else if (amount1[1].length() < 27) {
                         amount1[1] = "<" + fontStyleChar + ">" + amount1[1] +" "+action+ "</" + fontStyleChar + ">";
                        jobjDetails.put(amount1[0], amount1[1]);
                        jobjDetails.put("amountinword1", "");
                    }
                }
            }
            
            //for amount in number
            if (chequeobj.has("amountFontSize") || isFontStylePresent) {
                if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("amountFontSize"))) {
                    amount[1] = "<font size=" + chequeobj.getString("amountFontSize") + "><" + fontStyleChar + ">" + prefixbeforamt+amount[1] + "</" + fontStyleChar + "></font> ";
                    jobjDetails.put(amount[0], amount[1]);
                } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("amountFontSize"))) {
                    amount[1] = "<font size=" + chequeobj.getString("amountFontSize") + ">" + prefixbeforamt+amount[1] + "</font> ";
                    jobjDetails.put(amount[0], amount[1]);
                } else {
                    amount[1] = "<" + fontStyleChar + ">" + prefixbeforamt+amount[1] + "</" + fontStyleChar + ">";
                    jobjDetails.put(amount[0], amount[1]);

                }
            }
            
            jArr.put(jobjDetails);
           KwlReturnObject result3 = accJournalEntryobj.updateChequePrint(jeid,companyid);
            issuccess = true;
            auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + sessionHandlerImpl.getUserFullName(request) + " has printed a cheque "+chequeno+" for "+StringUtil.serverHTMLStripper(accName[1]) +" in Fund Transfer " + jeno, request, jeid);
            msg = messageSource.getMessage("acc.pay.del", null, RequestContextUtils.getLocale(request));  //"Payment(s) has been deleted successfully";
        } catch (Exception ex) {

            msg = "" + ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);

                jobj.put("data", jArr);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
//**************End of Functions to insert / update existing JE's entries in jedetails_optimized tables*********
        
   public ModelAndView getChequeDetailsForRepeatedJE(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        int count = 0;
        boolean isSuccess = false;
        try {
            String repeateJEId = request.getParameter("repeateid") == null ? "" : request.getParameter("repeateid");
            DateFormat df = authHandler.getDateOnlyFormat(request);
            KwlReturnObject result = accJournalEntryobj.getChequeDetailsForRepeatedJE(repeateJEId);
            List<RepeatedJEChequeDetail> list = result.getEntityList();
            JSONArray jArr = new JSONArray();
            for (RepeatedJEChequeDetail r : list) {
                JSONObject obj = new JSONObject();
                obj.put("no", r.getCount());
                obj.put("chequedate", df.format(r.getChequeDate()));
                obj.put("chequeno", r.getChequeNumber() == null ? "" : r.getChequeNumber());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            isSuccess = true;
            count = jArr.length();
        } catch (Exception ex) {
            msg = ex.getMessage();
            throw ServiceException.FAILURE("accJournalEntryController.getChequeDetailsForRepeatedPayment : " + msg, ex);
        } finally {
            try {
                jobj.put("success", isSuccess);
                jobj.put("msg", msg);
                jobj.put("count", count);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }     
}
