/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting.journalentry.service;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import static com.krawler.spring.accounting.journalentry.JournalEntryConstants.GSTRTYPE;
import static com.krawler.spring.accounting.journalentry.JournalEntryConstants.ITC_TRANSACTION_IDS;
import static com.krawler.spring.accounting.journalentry.JournalEntryConstants.JEDID;
import static com.krawler.spring.accounting.journalentry.JournalEntryConstants.JEID;
import com.krawler.spring.accounting.journalentry.accJournalEntryController;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.payment.accPaymentService;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccJournalEntryModuleServiceImpl implements AccJournalEntryModuleService, MessageSourceAware {

    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private accJournalEntryDAO accJournalEntryobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accCurrencyDAO accCurrencyobj;
    private auditTrailDAO auditTrailObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private fieldDataManager fieldDataManagercntrl;
    private accCustomerDAO accCustomerDAOobj;
    private accPaymentDAO accPaymentDAOobj;
    private accPaymentService accPaymentService;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private com.krawler.spring.common.fieldDataManager fieldDataManagercntrl1;
    private authHandlerDAO authHandlerDAOObj;
    private EnglishNumberToWords EnglishNumberToWordsOjb = new EnglishNumberToWords();
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }
    
    public void setFieldDataManagercntrl1(com.krawler.spring.common.fieldDataManager fieldDataManagercntrl1) {
        this.fieldDataManagercntrl1 = fieldDataManagercntrl1;
    }
    
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
    }
    public String getSuccessView() {
        return successView;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setAccPaymentDAOobj(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setAccPaymentService(accPaymentService accPaymentService) {
        this.accPaymentService = accPaymentService;
    }

    
    
    public JSONObject saveJournalEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        String billid = "";
        String amount = "";
        boolean issuccess = true;
        String butPendingForApproval = "";
        try{            
            JSONObject paramJObj = new JSONObject();
            try{Enumeration<String> attributes = request.getAttributeNames();
            while(attributes.hasMoreElements()){
                String attribute = attributes.nextElement();
                paramJObj.put(attribute, request.getAttribute(attribute));
            }
            Enumeration<String> parameters = request.getParameterNames();
            while(parameters.hasMoreElements()){
                String parameter = parameters.nextElement();
                paramJObj.put(parameter, request.getParameter(parameter));
            }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userDateFormat = sessionHandlerImpl.getUserDateFormat(request);
            String userfullname = sessionHandlerImpl.getUserFullName(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            paramJObj.put(Constants.companyKey, companyid);
            paramJObj.put("userdateformat", userDateFormat);
            paramJObj.put(Constants.currencyKey, currencyid);
            paramJObj.put(Constants.globalCurrencyKey, currencyid);
            paramJObj.put(Constants.userfullname, userfullname);
            String timezoneOffset = sessionHandlerImpl.getTimeZoneDifference(request);
            paramJObj.put("timezoneOffset", timezoneOffset);
            paramJObj.put("language",  RequestContextUtils.getLocale(request).getLanguage());
            jobj = saveJournalEntryJson(paramJObj);
            
        }
        catch(SessionExpiredException ex){
            issuccess=false;
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            issuccess=false;
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (Exception ex) {
            issuccess=false;
            ex.printStackTrace();
            
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("billid", billid);
                jobj.put("billno", billno);
                jobj.put("amount", "");
            } catch (JSONException ex) {
                Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public JournalEntry saveJournalEntry(HttpServletRequest request, JSONObject dataMap, int counter) throws SessionExpiredException, ServiceException, AccountingException, JSONException {
        JournalEntry journalEntry = null;
        JSONObject paramJObj = new JSONObject();
        try{Enumeration<String> attributes = request.getAttributeNames();        
        while(attributes.hasMoreElements()){
            String attribute = attributes.nextElement();
            paramJObj.put(attribute, request.getAttribute(attribute));            
        }
        Enumeration<String> parameters = request.getParameterNames(); 
        while(parameters.hasMoreElements()){
            String parameter = parameters.nextElement();
            paramJObj.put(parameter, request.getParameter(parameter));                        
        }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userDateFormat = sessionHandlerImpl.getUserDateFormat(request);
        String userfullname = sessionHandlerImpl.getUserFullName(request);
        String currencyid = sessionHandlerImpl.getCurrencyID(request);
        paramJObj.put(Constants.companyKey, companyid);
        paramJObj.put("userdateformat", userDateFormat);
        paramJObj.put(Constants.currencyKey, currencyid);
        paramJObj.put(Constants.globalCurrencyKey, currencyid);
        paramJObj.put(Constants.userfullname, userfullname);
        String timezoneOffset = sessionHandlerImpl.getTimeZoneDifference(request);
        paramJObj.put("timezoneOffset", timezoneOffset);
        paramJObj.put("language",  RequestContextUtils.getLocale(request).getLanguage());
        journalEntry = saveJournalEntryJson(paramJObj, dataMap, counter);
        return journalEntry;
    }
    
    public JSONObject saveJournalEntryJson(JSONObject paramJobj) throws JSONException, SessionExpiredException, ServiceException{

        JSONObject jobj = new JSONObject();
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;//txnManager.getTransaction(def);
        try{
            int counter = 0;            
            JSONArray jarr = paramJobj.getJSONArray("jedata");
            JSONArray respArray = new JSONArray();
            for (int i = 0; i < jarr.length(); i++) {
                status = txnManager.getTransaction(def);
                JSONObject dataMap = jarr.getJSONObject(i);
                JournalEntry je = saveJournalEntryJson(paramJobj, dataMap, counter);
                JSONObject jeObj = new JSONObject();                
                jeObj.put("entryno", je.getEntryNumber());
                jeObj.put("jeid", je.getID());
                respArray.put(jeObj);
//                counter++;
                txnManager.commit(status);
            }
            
            String msg = messageSource.getMessage("acc.je1.save", null, Locale.forLanguageTag(paramJobj.getString("language")));        
                jobj.put("msg", msg);
                jobj.put("jarr", respArray);
                jobj.put("success", true);
        }
        catch(ServiceException e){
            txnManager.rollback(status);
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        catch(SessionExpiredException e){
            txnManager.rollback(status);
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        catch(JSONException e){
            txnManager.rollback(status);
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, e);
            throw new JSONException(e);
        } catch (AccountingException e) {
            txnManager.rollback(status);
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;

    }

    public JournalEntry saveJournalEntryJson(JSONObject paramJobj, JSONObject dataMap, int counter) throws SessionExpiredException, ServiceException, AccountingException,JSONException {
        JournalEntry journalEntry = null;        
            String companyid = paramJobj.getString(Constants.companyKey);
            String currencyid = paramJobj.getString(Constants.currencyKey);
            DateFormat df = authHandler.getDateOnlyFormat();
            Calendar cal = Calendar.getInstance();
            Date billdateVal = null;
            long billdateValue = 0;
            if(!StringUtil.isNullOrEmpty(paramJobj.optString("creationdate"))){
                billdateValue = (long) Long.parseLong(paramJobj.get("creationdate").toString());
            }
            else{
                billdateValue = (new Date()).getTime();
            }
            cal.setTimeInMillis(billdateValue);
            String timeZoneDifference = "";
            if(paramJobj.has("timezoneOffset")){
                timeZoneDifference = paramJobj.getString("timezoneOffset");
            }
            cal.setTimeZone(TimeZone.getTimeZone("GMT" + timeZoneDifference));
            billdateVal = cal.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
            try {
                billdateVal = df.parse(sdf.format(billdateVal));
            } catch (ParseException ex) {
                Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            double externalCurrencyRate = 1d;
            if (!StringUtil.isNullOrEmpty(currencyid)) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.globalCurrencyKey, paramJobj.getString(Constants.currencyKey));
                requestParams.put(Constants.companyKey, companyid);
                externalCurrencyRate = accCurrencyobj.getCurrencyToBaseRate(requestParams, currencyid, billdateVal);
            }
            double amount = 0;
            if(dataMap.has("amount")){
                amount = Double.parseDouble(dataMap.get("amount").toString());
            }
            String sales_Account = "";
            String revenueaccount = "";

            KwlReturnObject cap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences preferences = (ExtraCompanyPreferences) cap.getEntityList().get(0);
            if (preferences.isRecurringDeferredRevenueRecognition()) {
                KwlReturnObject cap1 = accountingHandlerDAOobj.getObject(Account.class.getName(), preferences.getSalesAccount());
                Account salesaccount = (Account) cap1.getEntityList().get(0);
                sales_Account = salesaccount.getID();
                KwlReturnObject cap2 = accountingHandlerDAOobj.getObject(Account.class.getName(), preferences.getSalesRevenueRecognitionAccount());
                Account revenueacc = (Account) cap2.getEntityList().get(0);
                revenueaccount = revenueacc.getID();
            } else {
                throw new AccountingException("Please set Advance sales and Revenue Recognition Account for LMS");
            }
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);

            String jeentryNumber = null;
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";

            boolean jeautogenflag = false;
            String jeSeqFormatId = "";
            synchronized (this) {
                try {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put(Constants.companyKey, companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, billdateVal);
                    String nextAutoNoTemp = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, number);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                } catch (ServiceException ex) {
                    Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            String invoiceId= dataMap.optString("invoiceId", "");
            String invoiceNo= dataMap.optString("invoiceNo", "");
            String memo="Manual JE For Revenue Recognition for Invoice "+invoiceNo;
            jeDataMap.put("entrydate", billdateVal);
            jeDataMap.put(Constants.companyKey, companyid);
            jeDataMap.put("memo", memo);
            jeDataMap.put(Constants.currencyKey, currencyid);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("typevalue", 1);
            String custCode = dataMap.optString("studentid", "");
            String CustomerId = "";
            if (!StringUtil.isNullOrEmpty(custCode)) {
                KwlReturnObject resultcheck = accCustomerDAOobj.checkCustomerExistbyCode(custCode, companyid);
                if (!resultcheck.getEntityList().isEmpty()) {
                    Customer customer = (Customer) resultcheck.getEntityList().get(0);
                    CustomerId = customer.getID();
                }
            }
            jeDataMap.put("customerid", CustomerId);
            journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
            accJournalEntryobj.saveJournalEntryByObject(journalEntry);
            HashMap hashMap=new HashMap();
            hashMap.put("invoiceId", invoiceId);
            hashMap.put("jeId", journalEntry.getID());
            accJournalEntryobj.saveRevenueJEInvoiceMapping(hashMap);
            
            //custom code
            JSONArray jcustomarray = null;
            boolean customfieldArrayflag = true;
            String customfieldArray = dataMap.optString("customfieldArray", ""); //Custom Data from other Project
            customfieldArray = StringUtil.DecodeText(customfieldArray);
            if (!StringUtil.isNullOrEmpty(customfieldArray)) {
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                String mapWithFieldType = paramJobj.optString("mapWithFieldType",null);
                customrequestParams.put("customarray", customfieldArray);
                customrequestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("mapWithFieldType", mapWithFieldType);
                jcustomarray = fieldDataManagercntrl.createJSONArrForCustomFieldValueFromOtherSource(customrequestParams);
                customfieldArrayflag = false;
            }
            // Add Custom fields details 
            if (!StringUtil.isNullOrEmpty(dataMap.optString("customfield", "")) || !StringUtil.isNullOrEmpty(customfieldArray)) {
                if (customfieldArrayflag) {
                    jcustomarray = new JSONArray(dataMap.optString("customfield", "[]"));
                }
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    Map<String, Object> customjeDataMap = new HashMap<String, Object>();
                    customjeDataMap.put("accjecustomdataref", journalEntry.getID());
                    customjeDataMap.put("jeid", journalEntry.getID());
                    accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                }
            }

            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", 1);
            jedjson.put(Constants.companyKey, companyid);
            jedjson.put("amount", amount);
            jedjson.put("accountid", sales_Account);
            jedjson.put("debit", true);
            jedjson.put("jeid", journalEntry.getID());
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            Set<JournalEntryDetail> detail = new HashSet();
            detail.add(jed);

            jedjson = new JSONObject();
            jedjson.put("srno", 2);
            jedjson.put(Constants.companyKey, companyid);
            jedjson.put("amount", amount);
            jedjson.put("accountid", revenueaccount);
            jedjson.put("debit", false);
            jedjson.put("jeid", journalEntry.getID());
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            detail.add(jed);
            journalEntry.setDetails(detail);
            accJournalEntryobj.saveJournalEntryDetailsSet(detail);
            Map<String, Object> auditTrailMap = new HashMap<String, Object>();
            auditTrailMap.put(Constants.useridKey, paramJobj.getString("lid"));
            auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + paramJobj.optString(Constants.userfullname) + " has posted Journal Entry " + jeentryNumber +" for Revenue Recognition ", auditTrailMap,journalEntry.getID());
        
        return journalEntry;
    }
    
    @Override
     public synchronized KwlReturnObject updateJEEntryNumberForNewJE(Map<String, Object> jeDataMap, JournalEntry JE, String companyid,  String sequenceFormat,int approvedLevel) {
        String entryNumber = "";
        List list = new ArrayList();
        boolean successFlag=true;
        try {
            String nextJEAutoNo = null;
            //String sequenceformat = request.getParameter("sequenceformat");
            String psotingDateStr = (String) jeDataMap.get("postingDate");
            DateFormat df = authHandler.getDateOnlyFormat();
            Date postingDate = null;
            if (!StringUtil.isNullOrEmpty(psotingDateStr)) {
                postingDate = df.parse(psotingDateStr);
            }
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("id",companyid);
            Object exPrefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"columnPref"}, paramMap);
            JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
            boolean isPostingDateCheck = false;
            if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                isPostingDateCheck = true;
            }
            if (!sequenceFormat.equals("NA") && !StringUtil.isNullOrEmpty(sequenceFormat)) {
                boolean seqformat_oldflag = false;//StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                String nextAutoNo = "";
                String nextAutoNoInt = "";
                if (seqformat_oldflag) {
                    nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY, sequenceFormat);
                } else {
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    if (postingDate != null && isPostingDateCheck) {
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, sequenceFormat, seqformat_oldflag, postingDate);
                    } else {
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, sequenceFormat, seqformat_oldflag, JE.getEntryDate());
                    }
                    nextJEAutoNo = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    String jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    String jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    String jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    jeDataMap.put(Constants.SEQFORMAT, sequenceFormat);
                    jeDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);

                    entryNumber = nextJEAutoNo;

                }
            }

            if (isPostingDateCheck && postingDate!=null) {
                jeDataMap.put("entrydate", postingDate);
            } else {
                jeDataMap.put("entrydate", JE.getEntryDate());
            }
            jeDataMap.put(Constants.companyKey, companyid);
            jeDataMap.put("entrynumber", entryNumber);
            jeDataMap.put("jeid", JE.getID());
            jeDataMap.put("istemplate", JE.getIstemplate());
            jeDataMap.put("isReval", JE.getIsReval());
            jeDataMap.put("isDraft", JE.isDraft());
            jeDataMap.put("pendingapproval", approvedLevel);
            KwlReturnObject je1result = accJournalEntryobj.saveJournalEntry(jeDataMap);
            list.add(entryNumber);
        } catch (Exception e) {
            list.add(e.getMessage());
            successFlag=false;
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, e);
        }
        return new KwlReturnObject(successFlag, "JE entry number has been updated successfully", null, list, list.size());
    }

    @Override
    public String getNextChequeNumberForRecurredJE(String companyId, String bankAccountId) {
        String nextChequeNumber = "";
        try {
            nextChequeNumber = accPaymentService.getNextChequeNumberForRecurredPayment(companyId,bankAccountId);
        } catch (Exception ex) {
            Logger.getLogger(accPaymentService.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return nextChequeNumber;
    }

    @Override
    public KwlReturnObject addCheque(HashMap<String, Object> checkHM) {
        KwlReturnObject cqresult = null;
        try {
            cqresult = accPaymentDAOobj.addCheque(checkHM);
        } catch (Exception ex) {
            Logger.getLogger(accPaymentService.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return cqresult;
    }
    
    public JSONObject saveJournalEntryRemoteApplication(HttpServletRequest request, HttpServletResponse response) {
        
        
        String msg = "";
        boolean issuccess = true;
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJObj = new JSONObject();
            Enumeration<String> attributes = request.getAttributeNames();
            while (attributes.hasMoreElements()) {
                String attribute = attributes.nextElement();
                paramJObj.put(attribute, request.getAttribute(attribute));
            }
            Enumeration<String> parameters = request.getParameterNames();
            while (parameters.hasMoreElements()) {
                String parameter = parameters.nextElement();
                paramJObj.put(parameter, request.getParameter(parameter));
            }

            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userDateFormat = sessionHandlerImpl.getUserDateFormat(request);
            paramJObj.put(Constants.companyKey, companyid);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            paramJObj.put(Constants.currencyKey, currencyid);
            paramJObj.put(Constants.globalCurrencyKey, currencyid);
            String userfullname = sessionHandlerImpl.getUserFullName(request);
            paramJObj.put(Constants.userfullname, userfullname);
            paramJObj.put("userdateformat", userDateFormat);
            String userId = sessionHandlerImpl.getUserid(request);
            paramJObj.put(Constants.useridKey, userId);
            paramJObj.put("lid", userId);
            paramJObj.put("language",  RequestContextUtils.getLocale(request).getLanguage());
            jobj = saveJournalEntryRemoteApplicationJson(paramJObj);
        
        } catch (JSONException ex) {
            msg = "" + ex.getMessage();
            issuccess=false;
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            issuccess=false;
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            issuccess=false;
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        
        } 
        catch (Exception ex) {
            issuccess=false;
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
               
            } catch (JSONException ex) {
                Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        return jobj;
    }
        public JournalEntry saveJournalEntryRemoteApplication(HttpServletRequest request, JSONObject dataMap) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry journalEntry = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = (!StringUtil.isNullOrEmpty(request.getParameter(Constants.currencyKey)) ? request.getParameter(Constants.currencyKey) : sessionHandlerImpl.getCurrencyID(request));
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            double externalCurrencyRate = 1d;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            String jeSeqFormatId = "";
            synchronized (this) {
                try {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put(Constants.companyKey, companyid);
                    JEFormatParams.put("isdefaultFormat", true);
                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, null); // Since JE date is always system date with timezone, hence sending null. It has been hadled in called method 
                    String nextAutoNoTemp = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    String jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    String jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    String jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                    sequence = sequence;
                    String number = "" + sequence;
                    String action = "" + (sequence);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;
                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, number);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                } catch (ServiceException ex) {
                    Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            String memo = dataMap.optString("memo", "");
            String auditmessage = dataMap.optString("auditmessage", "");
//            jeDataMap.put("entrydate", new Date());
            jeDataMap.put(Constants.companyKey, companyid);
            jeDataMap.put("memo", memo);
            jeDataMap.put(Constants.currencyKey, currencyid);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("typevalue", 1);
            journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
            
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date newdate=authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            Date newdate=new Date();
            String userdiff=journalEntry.getCompany().getCreator().getTimeZone()!=null?journalEntry.getCompany().getCreator().getTimeZone().getDifference() : journalEntry.getCompany().getTimeZone().getDifference();
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"+userdiff));
            Date newcreatedate=authHandler.getDateWithTimeFormat().parse(sdf.format(newdate));
            journalEntry.setEntryDate(newcreatedate);
            
            accJournalEntryobj.saveJournalEntryByObject(journalEntry);
            JSONArray jArr = new JSONArray(dataMap.optString("details"));
            Set<JournalEntryDetail> detail = new HashSet();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject detailData = jArr.getJSONObject(i);
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", i + 1);
                jedjson.put(Constants.companyKey, companyid);
                jedjson.put("amount", detailData.optString("amount"));
                jedjson.put("accountid", detailData.optString("accountid"));
                jedjson.put("debit", detailData.optBoolean("debit"));
                jedjson.put("jeid", journalEntry.getID());
                jedjson.put("description", detailData.optString("description"));
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                detail.add(jed);
            }
            journalEntry.setDetails(detail);
            accJournalEntryobj.saveJournalEntryDetailsSet(detail);
            auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + sessionHandlerImpl.getUserFullName(request) + " has posted Journal Entry " + jeentryNumber + " "+auditmessage, request, journalEntry.getID());
        } catch (JSONException ex) {
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return journalEntry;
    }
        
    public JSONObject saveJournalEntryRemoteApplicationJson(JSONObject paramJobj)throws SessionExpiredException, ServiceException, JSONException, AccountingException {
        JSONObject jobj = new JSONObject();
        JSONArray array=new JSONArray();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;//txnManager.getTransaction(def);
        try {
            int counter = 0;
            JSONObject inputdata = new JSONObject(paramJobj.get("data").toString());
            JSONArray jarr = inputdata.getJSONArray("jedata");
            
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject dataMap1 = jarr.getJSONObject(i);
                
                JSONArray tempArr = dataMap1.getJSONArray("details");
                for (int j = 0; j < tempArr.length(); j++) {
                status = txnManager.getTransaction(def);
                    JSONObject dataMap = tempArr.getJSONObject(j);
                JournalEntry je = saveJournalEntryRemoteApplicationJson(paramJobj, dataMap);
                JSONObject returnObj=new JSONObject();

                returnObj.put("historyid", dataMap.optString("historyid"));
                returnObj.put("jeid", je.getID());
                returnObj.put("entryno", je.getEntryNumber());
                txnManager.commit(status);
                array.put(returnObj);
            }  
                
                
                
                }
            
            
            issuccess = true;
            msg = messageSource.getMessage("acc.je1.save", null, Locale.forLanguageTag(paramJobj.getString("language")));
            jobj.put("jarr", array);
            jobj.put("success", issuccess);
            jobj.put("msg", msg);
        } catch (JSONException ex) {
            txnManager.rollback(status);            
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new JSONException(ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);            
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new AccountingException(ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
               
            } catch (JSONException ex) {
                Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
        public JournalEntry saveJournalEntryRemoteApplicationJson(JSONObject paramJobj, JSONObject dataMap) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry journalEntry = null;
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            String currencyid = paramJobj.getString(Constants.currencyKey);
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
            double externalCurrencyRate = 1d;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            String jeSeqFormatId = "";
            synchronized (this) {
                try {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put(Constants.companyKey, companyid);
                    JEFormatParams.put("isdefaultFormat", true);
                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, null); // Since JE date is always system date with timezone, hence sending null. It has been hadled in called method 
                    String nextAutoNoTemp = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    String jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    String jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    String jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                    sequence = sequence;
                    String number = "" + sequence;
                    String action = "" + (sequence);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;
                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, number);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                } catch (ServiceException ex) {
                    Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            String memo = dataMap.optString("memo", "");
            String auditmessage = dataMap.optString("auditmessage", "");
//            jeDataMap.put("entrydate", new Date());
            jeDataMap.put(Constants.companyKey, companyid);
            jeDataMap.put("memo", memo);
            jeDataMap.put(Constants.currencyKey, currencyid);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("typevalue", 1);
            journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
            
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date newdate=authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            Date newdate=new Date();
            String userdiff=journalEntry.getCompany().getCreator().getTimeZone()!=null?journalEntry.getCompany().getCreator().getTimeZone().getDifference() : journalEntry.getCompany().getTimeZone().getDifference();
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"+userdiff));
            Date newcreatedate=authHandler.getDateWithTimeFormat().parse(sdf.format(newdate));
            journalEntry.setEntryDate(newcreatedate);
            
            accJournalEntryobj.saveJournalEntryByObject(journalEntry);
            JSONArray jArr = new JSONArray(dataMap.optString("details"));
            Set<JournalEntryDetail> detail = new HashSet();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject detailData = jArr.getJSONObject(i);
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", i + 1);
                    jedjson.put(Constants.companyKey, companyid);
                jedjson.put("amount", detailData.optString("amount"));
                jedjson.put("accountid", detailData.optString("accountid"));
                jedjson.put("debit", detailData.optBoolean("debit"));
                    jedjson.put("jeid", journalEntry.getID());
                jedjson.put("description", detailData.optString("description"));
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    detail.add(jed);
                }
            journalEntry.setDetails(detail);
            accJournalEntryobj.saveJournalEntryDetailsSet(detail);
            Map<String, Object> auditTrailMap = new HashMap<String, Object>();
            auditTrailMap.put(Constants.useridKey, paramJobj.getString("lid"));
            auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + paramJobj.optString(Constants.userfullname) + " has posted Journal Entry " + jeentryNumber + " "+auditmessage, auditTrailMap, journalEntry.getID());
        } catch (JSONException ex) {
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return journalEntry;
    }
        /**
         * 
         * @param requestparams
         * @return 
         * @Desc  :  Update JournalEntry which is created using recurring process
         */
        public JSONObject updateJournalEntry(JSONObject requestparams) {
        JSONObject jSONObject = new JSONObject();
        JournalEntry journalEntry = null;
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        status = txnManager.getTransaction(def);
        try {

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(requestparams);
            String memo = requestparams.optString("memo", "");
            String billid = requestparams.optString("jeid", "");
            String sequenceformat = requestparams.optString("sequenceformat", "");
            DateFormat df = (DateFormat) requestparams.opt("df");
            if (StringUtil.isNullOrEmpty(billid)) {
                throw new AccountingException("Journal Entry Cannot be Saved");
            } else {
                /**
                 * Delete all JE Details 
                 */
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(billid);
            }
            /**
             * Copy all data from Original JE
             */
            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), billid);
            JournalEntry oldjournalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            if (oldjournalEntry != null) {
                if (oldjournalEntry.getSeqformat() != null) {
                    jeDataMap.put(Constants.SEQFORMAT, oldjournalEntry.getSeqformat().getID());
                }
                jeDataMap.put(Constants.SEQNUMBER, oldjournalEntry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, oldjournalEntry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, oldjournalEntry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, oldjournalEntry.getDateSuffixValue());
                if (!sequenceformat.equals("NA")) {
                    jeDataMap.put("entrynumber", oldjournalEntry.getEntryNumber());
                }
                jeDataMap.put("entrynumber", oldjournalEntry.getEntryNumber());
                jeDataMap.put("autogenerated", oldjournalEntry.isAutoGenerated());
                jeDataMap.put("entrydate", df.parse(requestparams.optString("entrydate")));
                jeDataMap.put("jeisedit", true);
                jeDataMap.put(Constants.companyKey, oldjournalEntry.getCompany().getCompanyID());
                jeDataMap.put("memo", memo);
                jeDataMap.put(Constants.currencyKey, oldjournalEntry.getCurrency().getCurrencyID());
                jeDataMap.put("externalCurrencyRate", oldjournalEntry.getExternalCurrencyRate());
                jeDataMap.put("typevalue", oldjournalEntry.getTypeValue());
                jeDataMap.put("DontCheckYearLock", true);
                jeDataMap.put("createdby", oldjournalEntry.getCreatedby().getUserID().toString());
                jeDataMap.put("reversejournalentry", oldjournalEntry.getReverseJournalEntry());
                jeDataMap.put("isreverseje", oldjournalEntry.isIsReverseJE());
                jeDataMap.put("pendingapproval", oldjournalEntry.getPendingapproval());
                jeDataMap.put("isReval", oldjournalEntry.getIsReval());
                jeDataMap.put("revalInvoiceId", oldjournalEntry.getRevalInvoiceId());
                jeDataMap.put("istemplate", 0);
                jeDataMap.put("typevalue", oldjournalEntry.getTypeValue());
                jeDataMap.put("partlyJeEntryWithCnDn", oldjournalEntry.getPartlyJeEntryWithCnDn());
                jeDataMap.put("parentid", oldjournalEntry.getParentJE().getID());
                if (oldjournalEntry.getParentJE().getAccBillInvCustomData() != null) {
                    jeDataMap.put("accjecustomdataref", oldjournalEntry.getParentJE().getID());
                }
                if (oldjournalEntry.getCostcenter() != null) {
                    jeDataMap.put("costcenterid", oldjournalEntry.getCostcenter().getID());
                }
                if (oldjournalEntry.getPaymentMethod() != null) {
                    jeDataMap.put("pmtmethod", oldjournalEntry.getPaymentMethod().getID());
                }
            }
            jeDataMap.put("memo", memo);
            jeDataMap.put("jeisedit", true);
            jeDataMap.put(JEID, billid);
            /**
             * Save Journal Entry
             */
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            JSONArray jArr = new JSONArray(requestparams.optString("detail"));
            /**
             * Save JE Details 
             */
            KwlReturnObject jedresult = accJournalEntryobj.getJEDset(jArr, journalEntry.getCompany().getCompanyID(), journalEntry);
            HashSet jeDetails = (HashSet) jedresult.getEntityList().get(0);
            journalEntry.setDetails(jeDetails);
            accJournalEntryobj.saveJournalEntryDetailsSet(jeDetails);
            /**
             * Save Audit Trial 
             */
            Map<String, Object> auditTrailMap = new HashMap<String, Object>();
            auditTrailMap.put(Constants.useridKey, requestparams.getString("lid"));
            auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + requestparams.optString(Constants.userfullname) + " has updated Journal Entry " + journalEntry.getEntryNumber() + " ", auditTrailMap, journalEntry.getID());
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.je1.save", null, Locale.forLanguageTag(requestparams.getString("language")));        
        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            issuccess = false;
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jSONObject.put("success", issuccess);
                jSONObject.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJournalEntryModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jSONObject;
    }
        
  @Override          
 /*Call from Web Application*/       
    public JSONObject saveJournalEntry(JSONObject paramJObj) {
        JSONObject jobj = new JSONObject();
         JSONObject chequeobj=new JSONObject();
        String msg = "", jeid = "", JENumber = "";
        boolean issuccess = false;
        int level=0;
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        int journalEntryType=0;
        String pendingForApproval="";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        Double jeAmount=0.0;
        String entryNumber = paramJObj.optString("entryno", null);
        KwlReturnObject resultForJe = null;
        String chequeDetail = "";
        String repeatedid="";
        String intervalType = "";
        int intervalUnit = 0;
        int noOfJEPost = 0;
        int noOfJERemainPost = 0;
        Date startdate = null;
        Date nextdate = null;
        Date expdate = null;
        RepeatedJE rje = null;
        int typeValue = 1;// 1 - Normal JE Entry, 2 - Partly JE Entry, 3- Fund  Transfer 
        TransactionStatus status = txnManager.getTransaction(def);
        String controlAccounts = "Control Account(s): ";
        boolean isWarning = paramJObj.optString("isWarning", null) != null ? Boolean.parseBoolean(paramJObj.optString("isWarning")) : false;
        try {
            String companyid = paramJObj.optString(Constants.companyKey);
            boolean jeedit = paramJObj.optString("jeedit", null) != null ? Boolean.parseBoolean(paramJObj.optString("jeedit")) : false;
            chequeDetail = !StringUtil.isNullOrEmpty(paramJObj.optString("chequeDetail", null)) ? paramJObj.optString("chequeDetail") : "";
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("typevalue", null))) {
                typeValue = Integer.parseInt(paramJObj.optString("typevalue"));
            }
           /*
            * ERP-39212 . check cheque number is belongs to any sequence formate or not 
            */
            if (typeValue == Constants.FundTransfer_Journal_Entry && !StringUtil.isNullOrEmpty(chequeDetail)) {
                JSONObject chequeDetailJObj = new JSONObject(chequeDetail);
                String chequeNumber = chequeDetailJObj.getString("chequeno");
                String sequenceFormat = chequeDetailJObj.optString(Constants.sequenceformat);
                JSONObject chequeParamJObj = new JSONObject();
                chequeParamJObj.put(Constants.companyKey, companyid);
                chequeParamJObj.put("chequeNumber", chequeNumber);
                chequeParamJObj.put(Constants.sequenceformat, sequenceFormat);

                List resultList = accCompanyPreferencesObj.checksChequeNumberForSequenceNumber(chequeNumber, companyid);
                if (!resultList.isEmpty()) {
                    JSONObject resultJObj = new JSONObject(((resultList.get(2) != null) && (!StringUtil.isNullOrEmpty(resultList.get(2).toString()))) ? resultList.get(2).toString() : "{}" );
                    if (resultJObj.length() > 0) {
                        String seqformatName = resultJObj.optString("formatName");
                        boolean isSeqnum = resultJObj.optBoolean("isSeqnum");
                        String seqformatId = resultJObj.optString("formatid");
                        if (sequenceFormat.equalsIgnoreCase("NA")) {
                            //selected sequence formate is NA and cheque number is belongs to any sequence format.
                            if (isSeqnum) {
                                throw new AccountingException(messageSource.getMessage("acc.common.enterchequenumber", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))) + " <b>" + chequeNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))) + ". " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))) + " <b>" + seqformatName + "</b> " + messageSource.getMessage("acc.common.insteadofcheque", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))));
                            }
                        } else {
                            if (isSeqnum) {
                                //cheque number is belong to other than selected sequence format
                                if(!(sequenceFormat.equals(seqformatId))){
                                    throw new AccountingException(messageSource.getMessage("acc.common.enterchequenumber", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))) + " <b>" + chequeNumber + "</b> " + messageSource.getMessage("acc.common.notbelongstoselectedsequenceformat", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))) + ". " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))) + " <b>"+seqformatName+"</b> " + messageSource.getMessage("acc.common.insteadofcheque", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))));
                                }
                            } else {
                                //cheque number is not belong to any of the sequence format
                                throw new AccountingException(messageSource.getMessage("acc.common.enterchequenumber", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))) + " <b>" + chequeNumber + "</b> " + messageSource.getMessage("acc.common.notBelongstoAnyOfTheAutoSequenceFormat", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))) + ". " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))) + " <b>NA</b> " + messageSource.getMessage("acc.common.insteadofcheque", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))));
                            }
                        }
                    }
                }
            }
            /**
             * createAsTransactionChkboxwithTemplate- used to create template along with transaction.
             */
            boolean createAsTransactionChkboxwithTemplate = paramJObj.optString("createAsTransactionChkbox").equalsIgnoreCase("on") ? true : false;
            if (!paramJObj.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(paramJObj.optString(Constants.sequenceformat, null))) {
                String sequenceformatid = null;
                SequenceFormat jeSeqFormat = null;
                Map<String, Object> sfrequestParams = new HashMap<String, Object>();
                sfrequestParams.put(Constants.companyKey, paramJObj.get(Constants.companyKey));
                sfrequestParams.put("modulename", "autojournalentry");
                KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
                List<SequenceFormat> ll = seqFormatResult.getEntityList();
                if (ll.isEmpty()) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("modulename", "autojournalentry");
                    requestParams.put("numberofdigit", 6);
                    requestParams.put("showleadingzero", true);
                    requestParams.put("prefix", Constants.JE_DEFAULT_PREFIX);
                    requestParams.put("sufix", "");
                    requestParams.put("startfrom", 0);
                    requestParams.put("name", Constants.JE_DEFAULT_FORMAT);
                    requestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    requestParams.put(Constants.companyKey, paramJObj.get(Constants.companyKey));
                    jeSeqFormat = accCompanyPreferencesObj.saveSequenceFormat(requestParams);
                    paramJObj.put(Constants.sequenceformat, jeSeqFormat.getID());

                } else if (ll.get(0) != null) {
                    SequenceFormat format = (SequenceFormat) ll.get(0);
                    sequenceformatid = format.getID();
                    paramJObj.put(Constants.sequenceformat, sequenceformatid);
                }
            }//end of sequenceformat
            
            if (StringUtil.isNullOrEmpty(paramJObj.optString(Constants.sequenceformat, null))) {
                JSONObject response = StringUtil.getErrorResponse("acc.common.erp33", paramJObj, "Sequence Format Details are missing.", messageSource);
                throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
            }
            
            //            List li = saveJournalEntry(paramJObj);
            /**
             * creating template for Journal Entry.
             * istemplate=2 //creating only template
             * istemplate=0 //creating only transaction
             */
            if (createAsTransactionChkboxwithTemplate) {
                paramJObj.put("istemplate", 2);
                saveJournalEntryList(paramJObj);
                paramJObj.remove("istemplate");
            }
            /**
             *creating Journal Entry.
             */
            List li = saveJournalEntryList(paramJObj);

            if (li.get(2) != null) {
                JENumber = (String) li.get(2);
            }
            issuccess = true;

            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), (String)li.get(0));
            JournalEntry JE = (JournalEntry) jeresult.getEntityList().get(0); 
            String jeId=JE.getID();
            Set<JournalEntryDetail> jeDetails = JE.getDetails();
            for (JournalEntryDetail journalEntryDetail : jeDetails) {
                if(journalEntryDetail.isDebit())                     // As Debit and credit amount for JE are same , any one type can be picked for calculating amount
                jeAmount = jeAmount+journalEntryDetail.getAmount(); 
                if( journalEntryDetail.getAccount() != null && !StringUtil.isNullOrEmpty(journalEntryDetail.getAccount().getUsedIn()) ){
                    controlAccounts += journalEntryDetail.getAccount().getAccountName() + (!StringUtil.isNullOrEmpty(journalEntryDetail.getAccount().getAcccode()) ? " ("+journalEntryDetail.getAccount().getAcccode()+")":"")+",";
                }
            }

            boolean printCheque = StringUtil.isNullOrEmpty(paramJObj.optString("printCheque", null)) ? false : Boolean.parseBoolean(paramJObj.optString("printCheque"));
            if (printCheque) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJObj.optString("pmtmethod"));
                PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                if (!StringUtil.isNullOrEmpty(payMethod.getID())) {
                    requestParams.put("bankid", payMethod.getID());
                }
                
                boolean isnewlayout = false;
                ChequeLayout chequeLayout=null;
                DateFormat DATE_FORMAT = new SimpleDateFormat(Constants.DEFAULT_FORMAT_CHECK);
                String prefixbeforamt = "";
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
                String formatted_date_with_spaces = "";
                chequeDetail = paramJObj.optString("chequeDetail", null);
                if (!StringUtil.isNullOrEmpty(chequeDetail)) {
                    JSONObject obj = new JSONObject(chequeDetail);
                    Date cdate = new Date(obj.getString("payDate"));
                    String chackdate = DATE_FORMAT.format(cdate);
                    if (chequeLayout!=null && chequeLayout.isAddCharacterInCheckDate()) {
                        formatted_date_with_spaces=chackdate;
                    } else {
                        for (int i = 0; i < chackdate.length(); i++) {
                            formatted_date_with_spaces += chackdate.charAt(i);
                            formatted_date_with_spaces += isnewlayout ? "&nbsp;&nbsp;&nbsp;" : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                        }
                    }
                }
                String[] amount = (String[]) li.get(3);
                String[] amount1 = (String[]) li.get(4);
                String[] accName = (String[]) li.get(5);
                jobjDetails.put(amount[0], prefixbeforamt+amount[1]);
                String amount_first_line = "";
                String amount_second_line = "";
                String action=" Only.";
                if (amount1[1].length() > 34 && amount1[1].charAt(34) == ' ') {
                    amount_first_line = amount1[1].substring(0, 34);
                    amount_second_line = amount1[1].substring(34, amount1[1].length());
                    jobjDetails.put(amount1[0], amount_first_line);
                    jobjDetails.put("amountinword1", amount_second_line +action);
                } else if (amount1[1].length() > 34) {
                    amount_first_line = amount1[1].substring(0, 34);
                    amount_first_line = amount1[1].substring(0, amount_first_line.lastIndexOf(" "));
                    amount_second_line = amount1[1].substring(amount_first_line.length(), amount1[1].length());
                    jobjDetails.put(amount1[0], amount_first_line);
                    jobjDetails.put("amountinword1", amount_second_line + action);
                } else {
                    if (amount1[1].length() < 27) {
                        jobjDetails.put(amount1[0], amount1[1] + action);
                        jobjDetails.put("amountinword1", "");
                    } else {
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
                jeid=JE.getID();
                String chequeno="";
               if (li.get(6) != null) {
                chequeno = (String) li.get(6);
                }
                KwlReturnObject result2 = accJournalEntryobj.updateChequePrint(jeid,companyid);
                
                Map<String, Object> insertLogParams = new HashMap<String, Object>();
                insertLogParams.put(Constants.reqHeader, (paramJObj.has(Constants.reqHeader) && paramJObj.get(Constants.reqHeader) != null) ? paramJObj.getString(Constants.reqHeader) : paramJObj.optString(Constants.remoteIPAddress));
                insertLogParams.put(Constants.remoteIPAddress, paramJObj.optString(Constants.remoteIPAddress));
                insertLogParams.put(Constants.useridKey, paramJObj.optString(Constants.useridKey));
               //                auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + paramJObj.optString(Constants.userfullname) + " has printed a cheque "+chequeno+" for "+StringUtil.serverHTMLStripper(accName[1]) +" in Fund Transfer " + JENumber, request, jeid);
                auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + paramJObj.optString(Constants.userfullname) + " has printed a cheque " + chequeno + " for " + StringUtil.serverHTMLStripper(accName[1]) + " in Fund Transfer " + JENumber, insertLogParams, jeid);

            } 
            if (!StringUtil.isNullOrEmpty(chequeDetail) && li.get(7) != null) {
                rje = (RepeatedJE) li.get(7);
                if (rje != null) {
                    repeatedid = rje.getId();
                    intervalUnit = rje.getIntervalUnit();
                    intervalType = rje.getIntervalType();
                    noOfJEPost = rje.getNoOfJEpost();
                    noOfJERemainPost = rje.getNoOfRemainJEpost();
                    startdate = rje.getStartDate();
                    nextdate = rje.getNextDate();
                    expdate = rje.getExpireDate();
                }
            } else if(StringUtil.isNullOrEmpty(chequeDetail) && li.get(3) != null) {
                rje = (RepeatedJE) li.get(3);
                    if (rje != null) {
                        repeatedid = rje.getId();
                        intervalUnit = rje.getIntervalUnit();
                        intervalType = rje.getIntervalType();
                        noOfJEPost = rje.getNoOfJEpost();
                        noOfJERemainPost = rje.getNoOfRemainJEpost();
                        startdate = rje.getStartDate();
                        nextdate = rje.getNextDate();
                        expdate = rje.getExpireDate();
                    }
            }
            txnManager.commit(status);
             String sequenceformat = paramJObj.optString("sequenceformat", "NA");
            TransactionStatus AutoNoStatus = null;
            try {
                synchronized (this) {
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("AutoNum_Tx");
                    if (paramJObj.optBoolean(Constants.isdefaultHeaderMap)) {//same transaction is used to commit
                        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    } else {
                        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    }
                    
                    AutoNoStatus = txnManager.getTransaction(def1);
                    if (sequenceformat.equals("NA") && !jeedit) {
                        resultForJe = accJournalEntryobj.getJECount(entryNumber, companyid);
                        while (resultForJe.getRecordTotalCount() > 0) {
                            entryNumber = entryNumber + "-1";
                            resultForJe = accJournalEntryobj.getJECount(entryNumber, companyid);
                        }
                        JENumber = accJournalEntryobj.updateJEEntryNumberForNA(jeId, entryNumber);
                        JE.setEntryNumber(entryNumber);
                    }
                    if (!jeedit && (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA"))) {  //Post New JE with auto generated Entry No.
//                        status = txnManager.getTransaction(def);
                        Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJObj);
//                        entryNumber = updateJEEntryNumberForNewJE(jeDataMap, JE, companyid, sequenceformat,JE.getPendingapproval());
                        KwlReturnObject returnObj = updateJEEntryNumberForNewJE(jeDataMap, JE, companyid, sequenceformat,JE.getPendingapproval());
                        if(returnObj.isSuccessFlag() && returnObj.getRecordTotalCount()>0){
                            entryNumber = (String) returnObj.getEntityList().get(0);
                        }
                        JENumber = entryNumber;

                    }
                }
            } catch (Exception ex) {
                if (AutoNoStatus != null) {
                    txnManager.rollback(AutoNoStatus);
                }
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (AutoNoStatus != null) {
                    txnManager.commit(AutoNoStatus);
                }
            }
            int approvalStatusLevel =11;    
            journalEntryType = JE.getTypeValue();
            String msgKey = "";
            String currentUserId = paramJObj.optString(Constants.useridKey);
//            if (journalEntryType != 2) {                 // Currently , Party Journal Entry is excluded from the approval rules. 
                approvalStatusLevel = approveJE(JE, paramJObj.optString(Constants.companyKey), level, String.valueOf(jeAmount), paramJObj, true, currentUserId);
                if (approvalStatusLevel != 11) {
                    pendingForApproval = messageSource.getMessage("acc.field.butpendingforApproval", null, Locale.forLanguageTag(paramJObj.optString("language")));
                    msgKey = "acc.je2.save";
                }else{
                    msgKey = "acc.je1.save";
                }
//            }
          
            int istemplate = 0;
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("istemplate", null))) {
                istemplate = Integer.parseInt(paramJObj.optString("istemplate"));
            }

            if (istemplate == 1) {
                msg = messageSource.getMessage("acc.field.JournalEntryandTemplatehasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJObj.optString("language"))) + " " + pendingForApproval + "<br/>JE No: <b>" + JENumber + "</b>";   //"Journal Entry and Template has been saved successfully.";
            } else if (istemplate == 2) {
                msg = messageSource.getMessage("acc.field.JournalEntryTemplatehasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJObj.optString("language")));   //"Journal Entry Template has been saved successfully.";
            } else {
                msg = messageSource.getMessage(msgKey, null, Locale.forLanguageTag(paramJObj.getString("language"))) + " " + pendingForApproval + "<br/>JE No: <b>" + JENumber + "</b>";   //"Journal Entry has been saved successfully";
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("createAsTransactionChkbox", "")) && paramJObj.optString("createAsTransactionChkbox").equalsIgnoreCase("on") && !StringUtil.isNullOrEmpty(paramJObj.optString("templatename", ""))) {
                    msg += " Template Name: <b>" + paramJObj.optString("templatename", "") + "</b>";
                }
            }
            
            String template = " template for record ";
            if (istemplate == 0) {
                template = "";
            }
            String action = "added new";
            if (jeedit == true) {
                action = "updated";
            }
            Map<String, Object> insertLogParams = new HashMap<String, Object>();
            insertLogParams.put(Constants.reqHeader, (paramJObj.has(Constants.reqHeader) && paramJObj.get(Constants.reqHeader) != null) ? paramJObj.getString(Constants.reqHeader) : paramJObj.optString(Constants.remoteIPAddress));
            insertLogParams.put(Constants.remoteIPAddress, paramJObj.optString(Constants.remoteIPAddress));
            insertLogParams.put(Constants.useridKey, paramJObj.optString(Constants.useridKey));
           //            auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + paramJObj.optString(Constants.userfullname) + " has " + action + template + " Journal Entry " + JE.getEntryNumber()+(approvalStatusLevel != 11 ? " "+messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + (!controlAccounts.equals("Control Account(s): ") ? ". "+controlAccounts.substring(0, controlAccounts.length()-1) :""), request, JE.getID());
            auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + paramJObj.optString(Constants.userfullname) + " has " + action + template + " Journal Entry " + JE.getEntryNumber() + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + (!controlAccounts.equals("Control Account(s): ") ? ". " + controlAccounts.substring(0, controlAccounts.length() - 1) : ""), insertLogParams, JE.getID());
            
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("createAsTransactionChkbox", "")) && paramJObj.optString("createAsTransactionChkbox").equalsIgnoreCase("on") && !StringUtil.isNullOrEmpty(paramJObj.optString("templatename", ""))) {
                auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + paramJObj.optString(Constants.userfullname) + " has " + action + " Document Template "+paramJObj.optString("templatename", "")+ " for record Journal Entry" , insertLogParams, JE.getID());
            }
            jeid = li.get(0) != null ? (String) li.get(0) : "";
            if (jeedit) {
                String oldjeid = (String) li.get(1);
                status = txnManager.getTransaction(def);
                deleteJEArray(oldjeid,journalEntryType,companyid);
                txnManager.commit(status);
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
                jobj.put("repeatedid", repeatedid);
                jobj.put("intervalUnit", intervalUnit);
                jobj.put("intervalType", intervalType);
                jobj.put("noOfJEPost", noOfJEPost);
                jobj.put("noOfJERemainPost", noOfJERemainPost);
                jobj.put("typeValue", typeValue);
                jobj.put("billno", JENumber);
                jobj.put("startdate", startdate);
                jobj.put("nextdate", nextdate);
                jobj.put("expdate", expdate);
                jobj.put("msg", msg);
                jobj.put("id", jeid);
                jobj.put("data", jArr);
                jobj.put("isWarning",isWarning);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }       
        
     public List saveJournalEntryList(JSONObject paramJobj) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry je = null;
        List ll = new ArrayList();
        try {
            int istemplate = paramJobj.optString("istemplate", null) != null ? Integer.parseInt(paramJobj.optString("istemplate")) : 0;
            String companyid = paramJobj.optString(Constants.companyKey);
            String createdby = paramJobj.optString(Constants.useridKey);
            String entryNumber = paramJobj.optString("entryno", null);
            String jeid = "";
            RepeatedJE rje = null;
            String oldjeid = "";
            int typeValue = 1;// 1 - Normal JE Entry, 2 - Partly JE Entry, 3- Fund  Transfer 
            boolean jeedit = false;
            boolean isdeletecndnwithJE=false;
            String chequeno="";
            String sequenceformat = paramJobj.optString("sequenceformat", "NA");
            String jeId = paramJobj.optString("jeid", null);
            boolean isWarning = paramJobj.optString("isWarning", null) != null ? Boolean.parseBoolean(paramJobj.optString("isWarning")) : false;
            boolean includeInGSTReport = paramJobj.optString("includeingstreport", null) != null ? Boolean.parseBoolean(paramJobj.optString("includeingstreport")) : false;
            if (istemplate != 2 && sequenceformat.equals("NA")) {
                if(StringUtil.isNullOrEmpty(entryNumber))       // entryNumber is set to be blank in case of istemplate=2 (in template creation case)
                throw new AccountingException(messageSource.getMessage("acc.je1.excp2", null, Locale.forLanguageTag(paramJobj.optString("language"))));
            }

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("jeedit", null))) {
                jeedit = Boolean.parseBoolean(paramJobj.optString("jeedit"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isdeletecndnwithJE", null))) {
                isdeletecndnwithJE = Boolean.parseBoolean(paramJobj.optString("isdeletecndnwithJE"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("typevalue", null))) {
                typeValue = Integer.parseInt(paramJobj.optString("typevalue"));
            }
            
            if (istemplate != 2  && sequenceformat.equals("NA") && jeedit) {  // 
                KwlReturnObject result = accJournalEntryobj.getJECountForEdit(entryNumber, companyid,jeId);
                int nocount = result.getRecordTotalCount();
                if (nocount > 0) {
                    throw new AccountingException("Journal entry number '" + entryNumber + "' already exists.");
                }
            }

            if (istemplate != 2 && sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_GENERAL_LEDGER_ModuleId, entryNumber, companyid);
                if (!list.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) list.get(0);
                    String formatName = (String) list.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.optString("language"))) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.optString("language"))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.optString("language"))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                    }
                }
            }
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate", "1"));
            DateFormat df = authHandler.getDateOnlyFormat();
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);

            // Check Cheque Detail is available or Not
            BigInteger oldChqNoIntValue = new BigInteger("0");
            BigInteger chequesequencenumber = new BigInteger("0");
             String chequeDetail = paramJobj.optString("chequeDetail", null);
       
                if (!StringUtil.isNullOrEmpty(chequeDetail)) {
                    JSONObject obj = new JSONObject(chequeDetail);
                    HashMap chequehm = new HashMap();

                    // cheque whether Cheque Number exist or not if already exist then don't let it save
                    String oldChqNo = "";
                    if (jeedit) {
                        String oldje = paramJobj.optString("jeid", null);
                        KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), oldje);
                        JournalEntry oldjournalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                        if (oldjournalEntry != null && oldjournalEntry.getCheque() != null) {
                            oldChqNo = oldjournalEntry.getCheque().getChequeNo();
                             oldChqNoIntValue  = oldjournalEntry.getCheque().getSequenceNumber();
                        }
                    }
                    
                    
                    KwlReturnObject companypref = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    CompanyAccountPreferences compPref = (CompanyAccountPreferences)companypref.getEntityList().get(0);
                    int chequeNoDuplicationSetting = compPref.getChequeNoDuplicate();
                    BigInteger nextSeqNumber = new BigInteger("0");
                    boolean checkForNextSequenceNumberAlso = true;
                    boolean isChequeNumberInString = false;
                    if (extraCompanyPreferences != null) {
                        try {// USER can enter String values also in such case exception will come
                            nextSeqNumber = new BigInteger(obj.getString("chequeno"));
                        } catch (Exception ex) {
                            checkForNextSequenceNumberAlso = false;
                            isChequeNumberInString = true;
                        }
                    } else {
                        checkForNextSequenceNumberAlso = false;
                    }
                    
                    boolean isChequeNumberAvailable = false;

                    boolean isEditCaseButChqNoChanged = false;
                    if (!StringUtil.isNullOrEmpty(obj.optString("chequeno"))) {
                        try {// OLD CHQ NO. can be String value also in such case exception will come

                            HashMap chequeNohm = new HashMap();
                            chequeNohm.put("companyId", companyid);
                            chequeNohm.put("nextChequeNumber", obj.optString("chequeno"));
                            chequeNohm.put("sequenceNumber", nextSeqNumber);
                            chequeNohm.put("checkForNextSequenceNumberAlso", checkForNextSequenceNumberAlso);
                            chequeNohm.put("bankAccountId", obj.getString("bankAccountId"));

                            JSONObject ChJobj = accCompanyPreferencesObj.isChequeNumberAvailable(chequeNohm);
                            isChequeNumberAvailable = ChJobj.optBoolean("isChequeNumberAvailable");
                            chequesequencenumber =new BigInteger(ChJobj.optString("chequesequencenumber"));

//                            if (!StringUtil.isNullOrEmpty(oldChqNo)) {
//                                oldChqNoIntValue = new BigInteger(oldChqNo);
//                            }


                            if (!oldChqNoIntValue.equals(chequesequencenumber)) {
                                isEditCaseButChqNoChanged = true;
                            }

                            if (isChequeNumberInString) {
                                if (!oldChqNo.equals(obj.optString("chequeno"))) {
                                    isEditCaseButChqNoChanged = true;
                                }
                            }
                        } catch (Exception ex) {
                            if (!oldChqNo.equals(obj.optString("chequeno"))) {
                                isEditCaseButChqNoChanged = true;
                            }
                        }
                    } else {
                        if (!oldChqNo.equals(obj.optString("chequeno"))) {
                            isEditCaseButChqNoChanged = true;
                        }
                    }
                    
                    if (chequeNoDuplicationSetting == Constants.ChequeNoBlock || (chequeNoDuplicationSetting == Constants.ChequeNoWarn && isWarning)) {
                        if (!StringUtil.isNullOrEmpty(obj.optString("chequeno")) && isChequeNumberAvailable && isEditCaseButChqNoChanged) {
                            String msgForException = "Cheque Number : <b>" + obj.getString("chequeno") + "</b> is already exist. ";
                            if (isWarning) {
                                    throw new AccountingException(msgForException+" "+messageSource.getMessage("acc.recurringMP.doYouWantToContinue", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                                } else {
                                    throw new AccountingException(msgForException + "Please enter different one. ");
                                }
                        }
                        String chequeNumber = obj.optString("chequeno");
                        chequeNumber = "'" + chequeNumber + "'";
                        HashMap<String, Object> requestMap = new HashMap();
                        requestMap.put("bankAccountId", obj.getString("bankAccountId"));
                        requestMap.put("chequeNumber", chequeNumber);
                        KwlReturnObject resultRepeatedPaymentChequeDetails = accPaymentDAOobj.getRepeatedPaymentChequeDetailsForPaymentMethod(requestMap);
                        List RPCD = resultRepeatedPaymentChequeDetails.getEntityList();
                        if (RPCD.size() > 0) {
                            Object[] object = (Object[]) RPCD.get(0);
                            String paymentNumber = (String) object[1];
                            String msgForException = messageSource.getMessage("acc.field.ChequeNumber", null, Locale.forLanguageTag(paramJobj.optString("language"))) + " : <b>" + obj.getString("chequeno") + "</b> " + messageSource.getMessage("acc.recurringMP.chequeNoReserverd", null, Locale.forLanguageTag(paramJobj.optString("language"))) + " <b>" + paymentNumber + "</b>. ";
                            if (isWarning) {
                                throw new AccountingException(msgForException + " " + messageSource.getMessage("acc.recurringMP.doYouWantToContinue", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                            } else {
                                throw new AccountingException(msgForException + messageSource.getMessage("acc.recurringMP.enteranotherChequeNo", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                            }
                        }
                    }

                    String chequeseqformatID = obj.optString("sequenceformat","");
                    chequehm.put(Constants.SEQFORMAT, chequeseqformatID);
                    if (!chequeseqformatID.equalsIgnoreCase("NA") && !chequeseqformatID.equalsIgnoreCase("")){
                        chequehm.put("isAutoGeneratedChequeNumber", true);
                    }
                    Map<String, Object> seqchequehm = new HashMap<>();
                    obj.put(Constants.companyKey, companyid);
                  
                    /**
                     * getNextChequeNumber method to generate next sequence number using
                     * sequence format,also saving the dateprefix and datesuffix in cheque table.
                     */
                    obj.put(Constants.companyKey, companyid);
                    obj.put("ischequeduplicatepref", compPref.getChequeNoDuplicate());
                    if (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA")) {
                        //Edit case- check pevious check number and new entered number are same then no need to generate next number else generate next number.
                       if(isEditCaseButChqNoChanged){//create and copy case- generate next number.
                            seqchequehm = accCompanyPreferencesObj.getNextChequeNumber(obj);
                        }
                    }
                    chequehm.put("companyId", companyid);
                    chequehm.put("bankAccount", obj.getString("bankAccountId"));
                    chequehm.put("createdFrom", 3);
                    chequehm.put("sequenceNumber", chequesequencenumber);
                    chequehm.put("chequeno", obj.optString("chequeno"));
                    chequehm.put("description", StringUtil.DecodeText(obj.optString("description")));
                    chequehm.put("bankname", StringUtil.DecodeText(obj.optString("bankname")));
                    chequehm.put("duedate", df.parse(obj.getString("payDate")));
                    if (seqchequehm!=null&&!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA") && seqchequehm.containsKey(Constants.SEQNUMBER)) {
                        chequehm.put("sequenceNumber", (String) seqchequehm.get(Constants.SEQNUMBER));
                    }
                    KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                    Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                    if (cheque != null) {
                        jeDataMap.put(CCConstants.JSON_cheque, cheque.getID());
                    chequeno=cheque.getChequeNo();
                    }
                }
                
                String costCenterId = paramJobj.optString(CCConstants.REQ_costcenter, null);

                jeDataMap.put(Constants.companyKey, companyid);
                jeDataMap.put("createdby", createdby);
                jeDataMap.put("entrydate", df.parse(paramJobj.optString("entrydate")));
                jeDataMap.put("memo", paramJobj.optString("memo"));
                if(jeedit){   
                    jeDataMap.put("entrynumber", entryNumber);
                } else {
                    jeDataMap.put("entrynumber", "");
                }
                jeDataMap.put("autogenerated", sequenceformat.equals("NA") ?  false : true);  //nextJEAutoNo.equals(entryNumber));
                jeDataMap.put(Constants.currencyKey,paramJobj.optString(Constants.currencyKey));
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeDataMap.put("typevalue", typeValue);
                jeDataMap.put("istemplate", istemplate);
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("parentje", null))) {
                    jeDataMap.put("parentid", paramJobj.optString("parentje"));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("repeateid", null))) {
                    jeDataMap.put("repeateid", paramJobj.optString("repeateid"));
                }

                if (jeedit) {
                     oldjeid = paramJobj.optString("jeid", null);
                    KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), oldjeid);
                    JournalEntry oldjournalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                    if (oldjournalEntry != null) {
                        if (oldjournalEntry.getSeqformat() != null) {
                            jeDataMap.put(Constants.SEQFORMAT, oldjournalEntry.getSeqformat().getID());
                        }
                        jeDataMap.put(Constants.SEQNUMBER, oldjournalEntry.getSeqnumber());
                        jeDataMap.put(Constants.DATEPREFIX, oldjournalEntry.getDatePreffixValue());
                        jeDataMap.put(Constants.DATEAFTERPREFIX, oldjournalEntry.getDateAfterPreffixValue());
                        jeDataMap.put(Constants.DATESUFFIX, oldjournalEntry.getDateSuffixValue());
                        if (!sequenceformat.equals("NA")) {
                            jeDataMap.put("entrynumber", oldjournalEntry.getEntryNumber());
                        }
                        jeDataMap.put("autogenerated", oldjournalEntry.isAutoGenerated());
                 
                        /* Saving previous status of flag isfromeclaim 
                        
                         At the time of edit JE 
                        
                         */
                        boolean isFromEclaim = oldjournalEntry.isIsFromEclaim();
                        jeDataMap.put("isFromEclaim", isFromEclaim);
                       
             
                    }
                    try {
                        if (isdeletecndnwithJE) {
                       KwlReturnObject cnresult= accJournalEntryobj.getCNFromJE(oldjeid, companyid);
                       List list=cnresult.getEntityList();
                       if(!list.isEmpty()){
                                accJournalEntryobj.deletePartyJournalCN(list, companyid);
                            }
                            cnresult = accJournalEntryobj.getDNFromJE(oldjeid, companyid);
                       list=cnresult.getEntityList();
                       if(!list.isEmpty()){
                                accJournalEntryobj.deletePartyJournalDN(list, companyid);
                            }
                        }
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
                    } catch (ServiceException ex) {
                        if(!StringUtil.isNullObject(typeValue) && typeValue==Constants.Party_Journal_Entry){
                            throw new AccountingException("CN/DN are already used in other transaction(s).");
                        } else{
                            throw new AccountingException("Journal entry details are already in use.");
                        }                        
                    }
                    jeDataMap.put("jeisedit", true);
                }
                if (!StringUtil.isNullOrEmpty(costCenterId)) {
                    jeDataMap.put(CCConstants.JSON_costcenterid, costCenterId);
                }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("paidToCmb", null))) {
                jeDataMap.put("paidToCmb", paramJobj.optString("paidToCmb"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("pmtmethod", null))) {
                jeDataMap.put("pmtmethod", paramJobj.optString("pmtmethod"));
            }
            jeDataMap.put("includeInGSTReport", includeInGSTReport);
            boolean isFromEclaim = paramJobj.has(Constants.isFromEclaim) ? paramJobj.getBoolean(Constants.isFromEclaim) : false;
            jeDataMap.put("isFromEclaim", isFromEclaim);
            jeDataMap.put(GSTRTYPE, paramJobj.optInt(GSTRTYPE,0));
            jeDataMap.put(ITC_TRANSACTION_IDS, paramJobj.optString(ITC_TRANSACTION_IDS));
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
            je = (JournalEntry) jeresult.getEntityList().get(0);

            JSONArray jArr = new JSONArray(paramJobj.optString("detail", "[]"));
            if (jArr.length()== 0) {
                JSONObject response = StringUtil.getErrorResponse("acc.common.erp32", paramJobj, "Account Details are missing.", messageSource);
                throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
            }
            KwlReturnObject jedresult = accJournalEntryobj.getJEDset(jArr, companyid,je);
            if (je.getTypeValue() == 3) {           // typeValue: 3 Fund Transfer JE
                jArr = StringUtil.sortJsonArray(jArr, "debit", false, false);
            }
            HashSet jeDetails = (HashSet) jedresult.getEntityList().get(0);
            je.setDetails(jeDetails);
            accJournalEntryobj.saveJournalEntryDetailsSet(jeDetails);
                jeid = je.getID();
                ll.add(jeid);
                ll.add(oldjeid);
                ll.add(je.getEntryNumber());
                /*
                 * Make custom field entry at line level
                 */

             double amount=0.0;
                for (Iterator<JournalEntryDetail> jEDIterator = jeDetails.iterator(); jEDIterator.hasNext();) {
                    JournalEntryDetail jed = jEDIterator.next();
                    int srno = 0;
                    if (!jed.isDebit()) {  //check print credit amount
                        amount = jed.getAmount();
                    }
                    if (jed.getSrno() != 0) {
                        srno = jed.getSrno();
                        JSONObject jobj = jArr.getJSONObject(--srno);
                        if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                            JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                            if (jcustomarray.length() > 0 && isFromEclaim) {
                                jcustomarray = fieldDataManagercntrl1.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_GENERAL_LEDGER_ModuleId, companyid, false);
                            }
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                            customrequestParams.put("modulerecid", jed.getID());
                            customrequestParams.put("recdetailId", jed.getID());
                            customrequestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                            customrequestParams.put(Constants.companyKey, companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                //jed.setAccJEDetailCustomData((AccJEDetailCustomData) hibernateTemplate.get(AccJEDetailCustomData.class, jed.getID()));
                                JSONObject jedjson = new JSONObject();
                                jedjson.put("accjedetailcustomdata", jed.getID());
                                jedjson.put("jedid", jed.getID());
                                jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                            }
                        }
                    }
                }

                /*
                 * Make custom field entry
                 */
                String customfield = paramJobj.optString("customfield", null);
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    JSONArray jcustomarray = new JSONArray(customfield);
                    if (jcustomarray.length() > 0 && isFromEclaim) {
                        jcustomarray = fieldDataManagercntrl1.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_GENERAL_LEDGER_ModuleId, companyid, true);
                    }
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                    customrequestParams.put("modulerecid", jeid);
                    customrequestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    customrequestParams.put(Constants.companyKey, companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        Map<String, Object> customjeDataMap = new HashMap<String, Object>();
                        customjeDataMap.put("accjecustomdataref", jeid);
                        customjeDataMap.put("jeid", jeid);
                        customjeDataMap.put("istemplate", istemplate);
                        jeresult = accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                    }
                }
                //Save record as template
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("templatename", null)) && (istemplate == 1 || istemplate == 2)) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                String moduletemplateid = paramJobj.optString("moduletemplateid", null);
                hashMap.put("templatename", paramJobj.optString("templatename", null));
                    if (!StringUtil.isNullOrEmpty(moduletemplateid)) {
                        hashMap.put("moduletemplateid", moduletemplateid);
                    }
                    hashMap.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    hashMap.put("modulerecordid", jeid);
                    hashMap.put(Constants.companyKey, companyid);
                    /**
                     * checks the template name is already exist in create and
                     * edit template case
                     */
                    KwlReturnObject result = accountingHandlerDAOobj.getModuleTemplateForTemplatename(hashMap);
                    int nocount = result.getRecordTotalCount();
                    if (nocount > 0) {
                        throw new AccountingException(messageSource.getMessage("acc.tmp.templateNameAlreadyExists", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                    }

                    accountingHandlerDAOobj.saveModuleTemplate(hashMap);
                }
                //Insert new entries again in optimized table.
                accJournalEntryobj.saveAccountJEs_optimized(jeid);
                
                rje = (RepeatedJE) je.getRepeateJE();
                
                if (!StringUtil.isNullOrEmpty(chequeDetail)) {
                    String name = "";//request.getParameter("paidToCmb");
                    KwlReturnObject custObj = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), paramJobj.optString("paidToCmb"));
                    if (custObj.getEntityList().get(0) != null) {
                        MasterItem obj = (MasterItem) custObj.getEntityList().get(0);
                        if (obj != null) {
                            name = obj.getValue();
                        }
                    }
                    String basecurrency = paramJobj.optString(Constants.currencyKey);
                    String amont1 = "" + amount;
                    KwlReturnObject result2 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
                    KWLCurrency currency = (KWLCurrency) result2.getEntityList().get(0);
                    String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amont1)), currency,countryLanguageId);
                    DecimalFormat def = new DecimalFormat("#,###,###,##0.00");
                    name=StringUtil.DecodeText(name);
                    String amt = def.format(amount);
                    ll.add(new String[]{"amount", amt});
                    ll.add(new String[]{"amountinword", netinword});
                    ll.add(new String[]{"accountName", name});
                    ll.add(chequeno);
                    ll.add(rje);
                } else {
                    ll.add(rje); 
                }
                
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("saveJournalEntry : " + ex.getMessage(), ex);
        }*/ catch (ParseException ex) {
            throw ServiceException.FAILURE("saveJournalEntry : " + ex.getMessage(), ex);
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveJournalEntry : " + ex.getMessage(), ex);
        }
        return ll;
    }      
        
    public void deleteJEArray(String oldjeid, int journalEntryType, String companyid) throws ServiceException, AccountingException, SessionExpiredException {
        try {      //delete old invoice
            JournalEntryDetail jed = null;
            if (!StringUtil.isNullOrEmpty(oldjeid)) {
                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(oldjeid, companyid);
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    jed = (JournalEntryDetail) itr.next();
                    //Sagar - No need to revert entry from optimized table as entries are already reverted from calling main function in edit case.
                    result = accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
                }

                // getting chequeid in case of fund transfer

                result = accJournalEntryobj.getChequeIdLinkedToJournalEntry(oldjeid, companyid);
                String chequeId = "";
                if (result != null) {
                    List chequeList = result.getEntityList();
                    if (!chequeList.isEmpty()) {
                        chequeId = (String) chequeList.get(0);
                    }
                }
                //delete the reconcialation details in case if presentr for the JE
//                if(journalEntryType ==3){//Only in case if Journal Entry is Fund Transfer type JE
                result = accJournalEntryobj.DeleteBankReconciliationDetail(oldjeid, companyid);
                result = accJournalEntryobj.DeleteBankUnReconciliationDetail(oldjeid, companyid);
//                }
                result = accJournalEntryobj.permanentDeleteJournalEntry(oldjeid, companyid);

                result = accJournalEntryobj.deleteJECustomData(oldjeid);

                if (!StringUtil.isNullOrEmpty(chequeId)) {
                    result = accPaymentDAOobj.deleteChequePermanently(chequeId, companyid);
                }
            }
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
     /* Note:
       while making whanges in this function (approveJE) also make chages in 'AccJournalEntryCMN/approveJE' function.
     * 
     */
   @Override   
    public int approveJE(JournalEntry JE, String companyid, int level, String amount, JSONObject paramJobj, boolean fromCreate, String currentUser) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException {
        boolean hasAuthority = false;
        if (!fromCreate) {                                             // check if the currently logged in user has authority or not to approve pending JE...but only in case when this method is called from approveJournalEntry method 
            String thisUser = currentUser;
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);
            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                hasAuthority = accJournalEntryobj.checkForRule(level, companyid, amount, thisUser);
            }
        } else {
            hasAuthority = true;
        }
        if (hasAuthority) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            String requisitionApprovalSubject = "Journal Entry: %s - Approval Notification";
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
                    + "<p>%s has created journal entry %s and sent it to you for approval. at level "+(level+1)+"</p>"
                    + "<p>Please review and approve it (Journal Entry Number: %s).</p>"
                    + "<p></p>"
                    + "<p>Company Name:- %s</p>"
                    + "<p>Please check on Url:- %s</p>"
                    + "<p>Thanks</p>"
                    + "<p>This is an auto generated email. Do not reply<br>";
            String requisitionApprovalPlainMsg = "Hi All,\n\n"
                    + "%s has created journal entry %s and sent it to you for approval. at level "+(level+1)+"\n"
                    + "Please review and approve it (Journal Entry Number: %s).\n\n"
                    + "Company Name:- %s \n"
                    + "Please check on Url:- %s \n"
                    + "Thanks\n\n"
                    + "This is an auto generated email. Do not reply\n";
            int approvalStatus = 11;
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String jeNumber = JE.getEntryNumber();
            String jeID = JE.getID();
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put(Constants.companyKey, companyid);
            qdDataMap.put("level", level + 1);
            qdDataMap.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            Iterator itr = flowresult.getEntityList().iterator();
            
            //String fromEmailId = Constants.ADMIN_EMAILID;
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(),companyid);
            Company company = (Company) returnObject.getEntityList().get(0);
            Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
            
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                String rule = row[2].toString();
                String creator = (!StringUtil.isNullOrEmpty(row[6].toString())) ? row[6].toString() : "";
                boolean sendForApproval = false;
                String[] creators = creator.split(",");
                for (int i = 0; i < creators.length; i++) {
                    if (creators[i].equals(paramJobj.optString(Constants.useridKey))) {
                        sendForApproval = true;
                        break;
                    }
                }
                rule = rule.replaceAll("[$$]+", amount);
                /*
                 * send mail to approvers on any of one below condition is true -
                 * 1 - If level exist and rule is not set
                 * 2 - If level and expression rule exist
                 * 3 - If creator rule exist
                 */
                if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && Boolean.parseBoolean(engine.eval(rule).toString())) || sendForApproval) {
                    // send emails
                    try {
                        if (Boolean.parseBoolean(row[3].toString()) && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                            String fromName = "User";
                            String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                            
                            fromName = paramJobj.optString(Constants.username);
//                            String fromEmailId1 = StringUtil.isNullOrEmpty(JE.getCompany().getEmailID()) ? authHandlerDAOObj.getSysEmailIdByCompanyID(JE.getCompany().getCompanyID()) : JE.getCompany().getEmailID();
//                            if (!StringUtil.isNullOrEmpty(fromEmailId1)) {
//                                fromEmailId = fromEmailId1;
//                            }
                            String subject = String.format(requisitionApprovalSubject, jeNumber);
                            String htmlMsg = String.format(requisitionApprovalHtmlMsg, fromName, jeNumber, jeNumber, company.getCompanyName(), paramJobj.optString(Constants.PAGE_URL));
                            String plainMsg = String.format(requisitionApprovalPlainMsg, fromName, jeNumber, jeNumber , company.getCompanyName(), paramJobj.optString(Constants.PAGE_URL));
                            ArrayList<String> emailArray = new ArrayList<String>();
                            String[] emails = {};
                            qdDataMap.put("ruleid", row[0].toString());
                            KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(qdDataMap);
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
                                SendMailHandler.postMail(emails, subject, htmlMsg, plainMsg, fromEmailId, smtpConfigMap);
                            }
                        }
                    } catch (MessagingException ex) {
                        Logger.getLogger(JournalEntry.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    approvalStatus = level + 1;
                }
            }
            accJournalEntryobj.approvePendingJE(jeID, companyid, approvalStatus);
            return approvalStatus;
        } else {
            return Constants.NoAuthorityToApprove;    // It return fixed value 999 which indecates that current logged in user has no authority to approve the transaction
        }
    }  
   
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

}
