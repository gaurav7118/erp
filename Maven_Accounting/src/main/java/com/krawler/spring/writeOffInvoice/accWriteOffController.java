/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.writeOffInvoice;

import com.krawler.common.util.Constants;
import com.krawler.hql.accounting.Invoice;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.receipt.AccReceiptServiceDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONArray;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.resource.JarResource;
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
public class accWriteOffController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accInvoiceDAO accInvoiceDAOObj;
    private accCurrencyDAO accCurrencyobj;
    private accWriteOffServiceDao accWriteOffServiceDao;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accJournalEntryDAO accJournalEntryobj;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private AccReceiptServiceDAO accReceiptServiceDAOobj;

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setAccInvoiceDAOObj(accInvoiceDAO accInvoiceDAOObj) {
        this.accInvoiceDAOObj = accInvoiceDAOObj;
    }

    public void setAccCurrencyobj(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
    }

    public void setAccWriteOffServiceDao(com.krawler.spring.writeOffInvoice.accWriteOffServiceDao accWriteOffServiceDao) {
        this.accWriteOffServiceDao = accWriteOffServiceDao;
    }

    public void setAccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setAccJournalEntryobj(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public void setAccReceiptServiceDAOobj(AccReceiptServiceDAO accReceiptServiceDAOobj) {
        this.accReceiptServiceDAOobj = accReceiptServiceDAOobj;
    }

    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public ModelAndView writeOffSalesInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = writeOffSalesInvoices(request);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            issuccess = false;
            Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject writeOffSalesInvoices(HttpServletRequest request) {
        JSONObject returnObject = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {
            String detailsJsonString = request.getParameter("invoiceDetails");
            HashMap<String, Object> requestParams = new HashMap();
            String Memo = request.getParameter("memo") != null ? request.getParameter("memo") : "";
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String createdby = sessionHandlerImpl.getUserid(request);
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) companyResult.getEntityList().get(0);
            requestParams.put(Constants.companyid, company.getCompanyID());
            requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
            DateFormat df = authHandler.getDateOnlyFormat();           
            Date writeOffDate=df.parse(request.getParameter("writeOffDate"));
            JSONArray jSONArray = new JSONArray(detailsJsonString);
            String transactionSaved="";
            try {
                for (int i = 0; i < jSONArray.length(); i++) {
                    status = txnManager.getTransaction(def);
                    JSONObject jObj = jSONArray.getJSONObject(i);
                    String invoiceId = jObj.getString("billid");
                    KwlReturnObject invoiceResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceId);
                    Invoice invoice = (Invoice) invoiceResult.getEntityList().get(0);
                    String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                    double invoiceAmountDue = 0;
                    double ExternalCurrencyRate = 0d;
                    double amountWrittenOffInInvoiceCurrency;
                    double amountWrittenOffInBaseCurrency;
                    Date grCreationDate = null;
                    grCreationDate = invoice.getCreationDate();
                    if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                        invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
                        ExternalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                    } else {
                        invoiceAmountDue = invoice.getInvoiceamountdue();
//                        grCreationDate = invoice.getJournalEntry().getEntryDate();
                        ExternalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
                    }
                    KwlReturnObject bAmt = null;
                    if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                        if (invoice.isConversionRateFromCurrencyToBase()) {
                            ExternalCurrencyRate = 1 / ExternalCurrencyRate;
                        }
                    }
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, invoiceAmountDue, fromcurrencyid, grCreationDate, ExternalCurrencyRate);
                    amountWrittenOffInInvoiceCurrency = invoiceAmountDue;
                    amountWrittenOffInBaseCurrency = (Double) bAmt.getEntityList().get(0);

                    //Update Invoice
                    JSONObject invjson = new JSONObject();
                    invjson.put("invoiceid", invoice.getID());
                    invjson.put("companyid", company.getCompanyID());
                    invjson.put("openingBalanceAmountDue", 0);
                    invjson.put(Constants.openingBalanceBaseAmountDue, 0);
                    invjson.put(Constants.invoiceamountdue, 0);
                    invjson.put(Constants.invoiceamountdueinbase, 0);
                    accInvoiceDAOObj.updateInvoice(invjson, null);
                    // Updating amount due date - i.e. date on which amount due became zero
                    HashMap<String, Object> dataMap = new HashMap<String, Object>();
                    dataMap.put("amountduedate", writeOffDate);
                    accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(invoice, dataMap);
                    //Create InvoiceWriteOffObject
                    HashMap<String, Object> writeOffMap = new HashMap<String, Object>();
                    writeOffMap.put("invoiceId", invoice.getID());
                    writeOffMap.put("writtenOffAmountInInvoiceCurrency", amountWrittenOffInInvoiceCurrency);
                    writeOffMap.put("writtenOffAmountInBaseCurrency", amountWrittenOffInBaseCurrency);
                    writeOffMap.put("companyId", companyId);
                    writeOffMap.put("date", writeOffDate);
                    writeOffMap.put("memo", Memo);

                    KwlReturnObject writeOffResult = accWriteOffServiceDao.saveInvoiceWriteOff(writeOffMap);
                    List list = writeOffResult.getEntityList();
                    InvoiceWriteOff invoiceWriteOff = (InvoiceWriteOff) list.get(0);

                    // Create JE

                    ExtraCompanyPreferences extraCompanyPreferences = null;
                    KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
                    extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);

                    int counter = 0;
                    String jeentryNumber = null;
                    boolean jeautogenflag = false;
                    String jeSeqFormatId = "";
                    String jeIntegerPart = "";
                    String jeDatePrefix = "";
                    String jeDateAfterPrefix = "";
                    String jeDateSuffix = "";
                    JournalEntryDetail jed = null;

                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyId);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, writeOffDate);
                    String nextAutoNoTemp = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, number);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                    jeDataMap.put("entrydate", writeOffDate);
                    jeDataMap.put("companyid", companyId);
                    jeDataMap.put("memo", "");
                    jeDataMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
                    jeDataMap.put("externalCurrencyRate", ExternalCurrencyRate);
                    jeDataMap.put("createdby", createdby);
                    jeDataMap.put("transactionModuleid", Constants.Acc_Sales_Invocie_WriteOff_ModuleId);
                    JournalEntry journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                    accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                    Set jedetails = new HashSet();
                    
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyId);
                    jedjson.put("amount", amountWrittenOffInInvoiceCurrency);
                    jedjson.put("accountid", extraCompanyPreferences.getWriteOffAccount());
                    jedjson.put("debit", true);            
                    jedjson.put("jeid", journalEntry.getID());

                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedjson.put("jedid", JEdeatilId.getID());
                    jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                    jedetails.add(jed);

                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyId);
                    jedjson.put("amount", amountWrittenOffInInvoiceCurrency);
                    jedjson.put("accountid", invoice.getAccount().getID());
                    jedjson.put("debit", false);            
                    jedjson.put("jeid", journalEntry.getID());

                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedjson.put("jedid", JEdeatilId.getID());
                    jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                    jedetails.add(jed);

                    accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
                    journalEntry.setDetails(jedetails);
                    invoiceWriteOff.setJournalEntry(journalEntry);

                    auditTrailObj.insertAuditLog(AuditAction.Written_Off_Sales_Invoice, "User " + sessionHandlerImpl.getUserFullName(request) + " Written Off a Sales Invoice " + invoice.getInvoiceNumber() +","+ " Journal Entry Number - " + journalEntry.getEntryNumber(), request, invoiceWriteOff.getID());

                    txnManager.commit(status);
                    transactionSaved+="<br>";
                    transactionSaved+= messageSource.getMessage("acc.agedPay.gridIno", null, RequestContextUtils.getLocale(request))+": "+"<b>"+invoice.getInvoiceNumber()+"</b>"+", "+messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request))+": "+"<b>"+journalEntry.getEntryNumber()+"</b>";
                    issuccess=true;
                }
                if(!transactionSaved.equals("")){
                    msg = messageSource.getMessage("acc.writeOff.savedSuccessfully", null, RequestContextUtils.getLocale(request))+" "+transactionSaved;
                }
            } catch (Exception e) {
                if (status != null) {
                    txnManager.rollback(status);
                }
                Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            issuccess = false;
            Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                returnObject.put("success", issuccess);
                returnObject.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnObject;
    }

    public ModelAndView getWrittenOffSalesInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String ss = request.getParameter("ss")!=null?request.getParameter("ss"):"";
            HashMap<String,Object> requestMap = new HashMap();
            requestMap.put("companyId", companyId);
            requestMap.put("ss", ss);
            KwlReturnObject result = accWriteOffServiceDao.getWrittenOfInvoices(requestMap);
            List<InvoiceWriteOff> invoiceList = result.getEntityList();
            DateFormat df = authHandler.getDateOnlyFormat(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("df", df);
            requestParams.put("list", invoiceList);
            jArr = getWriteOffJsonArray(requestParams);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            issuccess = false;
            Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArr);
            } catch (JSONException ex) {
                Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getWriteOffJsonArray(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            List<InvoiceWriteOff> list = (List) requestParams.get("list");
            DateFormat df = (DateFormat) requestParams.get("df");

            for (InvoiceWriteOff IWO : list) {
                JSONObject obj = new JSONObject();
                Invoice invoice = IWO.getInvoice();
                boolean isOpeningInvoice = IWO.getInvoice().isIsOpeningBalenceInvoice();
                obj.put("id", IWO.getID());
                obj.put("billid", invoice.getID());
                obj.put("billno", invoice.getInvoiceNumber());
                obj.put("currencysymbol", IWO.getInvoice().getCurrency().getSymbol());
                obj.put("currencyid", invoice.getCurrency().getCurrencyID());
                obj.put("personname", invoice.getCustomer().getName());
                obj.put("billdate", isOpeningInvoice ? df.format(invoice.getCreationDate()) : df.format(invoice.getJournalEntry().getEntryDate()));
                obj.put("writeOffDate", df.format(IWO.getWriteOffDate()));
                obj.put("invoiceAmount", isOpeningInvoice ? invoice.getOriginalOpeningBalanceAmount() : invoice.getCustomerEntry().getAmount());
                obj.put("amountWrittenOff", IWO.getWrittenOffAmountInInvoiceCurrency());
                obj.put("reversejeno", IWO.getReversejournalEntry() != null ? IWO.getReversejournalEntry().getEntryNumber() : "");
                obj.put("jeno", IWO.getJournalEntry() != null ? IWO.getJournalEntry().getEntryNumber() : "");
                obj.put("isrecovered", IWO.isIsRecovered());
                obj.put("memo", IWO.getMemo());
                obj.put("deleted",IWO.getInvoice().isDeleted() );
                jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accWriteOffController.getWriteOffJsonArray : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView recoverSalesInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            jobj = recoverSalesInvoice(request);
            if(jobj.getString("invoiceNumber") != null){
                issuccess =true;
                msg = messageSource.getMessage("acc.writeOff.recoveredSuccessfully", null, RequestContextUtils.getLocale(request))+"</br>";
                msg += messageSource.getMessage("acc.agedPay.gridIno", null, RequestContextUtils.getLocale(request))+": "+"<b>"+jobj.getString("invoiceNumber")+"</b>"+", "+messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request))+": "+"<b>"+jobj.getString("reverseJeNumber")+"</b>";
                auditTrailObj.insertAuditLog(AuditAction.Written_Off_Sales_Invoice_Reverse, "User " + sessionHandlerImpl.getUserFullName(request) + " has recovered a Sales Invoice " + jobj.getString("invoiceNumber") +","+ " Journal Entry Number - " + jobj.getString("reverseJeNumber"), request, jobj.getString("writeOIffId"));
            }
             txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            issuccess = false;
            Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject recoverSalesInvoice(HttpServletRequest request) throws ServiceException {
        JSONObject obj = new JSONObject();
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String writeOffId = request.getParameter("writeOffId") != null ? request.getParameter("writeOffId") : "";
            String createdby = sessionHandlerImpl.getUserid(request);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(InvoiceWriteOff.class.getName(), writeOffId);
            InvoiceWriteOff invoiceWriteOff = (InvoiceWriteOff) result.getEntityList().get(0);

            JournalEntry JE = invoiceWriteOff.getJournalEntry();
            Invoice invoice = invoiceWriteOff.getInvoice();
            double amountWriteOff = invoiceWriteOff.getWrittenOffAmountInInvoiceCurrency();
            double amountWriteOffInBase = invoiceWriteOff.getWrittenOffAmountInBaseCurrency();
            JSONObject invjson = new JSONObject();

            invjson.put("invoiceid", invoice.getID());
            invjson.put("companyid", companyId);
            invjson.put("openingBalanceAmountDue", invoice.getOpeningBalanceAmountDue() + amountWriteOff);
            invjson.put(Constants.openingBalanceBaseAmountDue, invoice.getOpeningBalanceBaseAmountDue() + amountWriteOffInBase);
            invjson.put(Constants.invoiceamountdue, invoice.getInvoiceamountdue() + amountWriteOff);
            invjson.put(Constants.invoiceamountdueinbase, invoice.getInvoiceAmountDueInBase() + amountWriteOffInBase);
            accInvoiceDAOObj.updateInvoice(invjson, null);
             // Updating amount due date - i.e. date on which amount due became zero
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("amountduedate", null);
            accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(invoice, dataMap);
            int counter = 0;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            JournalEntryDetail jed = null;

            HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
            JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
            JEFormatParams.put("modulename", "autojournalentry");
            JEFormatParams.put("companyid", companyId);
            JEFormatParams.put("isdefaultFormat", true);

            KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
            SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, new Date());
            String nextAutoNoTemp = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
            int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
            jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
            jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
            jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
            sequence = sequence + counter;
            String number = "" + sequence;
            String action = "" + (sequence - counter);
            nextAutoNoTemp.replaceAll(action, number);
            jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
            jeSeqFormatId = format.getID();
            jeautogenflag = true;

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMap.put(Constants.SEQNUMBER, number);
            jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMap.put("entrydate", new Date());
            jeDataMap.put("companyid", companyId);
            jeDataMap.put("memo", "");
            jeDataMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
            jeDataMap.put("externalCurrencyRate", JE.getExternalCurrencyRate());
            jeDataMap.put("createdby", createdby);
            jeDataMap.put("transactionModuleid", Constants.Acc_Sales_Invocie_WriteOff_ModuleId_Reverse);
            JournalEntry reverseJournalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
            accJournalEntryobj.saveJournalEntryByObject(reverseJournalEntry);

            Set<JournalEntryDetail> jedetails = JE.getDetails();
            Set jedetailsnew = new HashSet();
            for (JournalEntryDetail jedetail : jedetails) {
                Account account = jedetail.getAccount();
                Double amount = jedetail.getAmount();
                boolean isDebit = jedetail.isDebit();

                JSONObject jedjson = new JSONObject();
                
                jedjson.put("srno", jedetailsnew.size() + 1);
                jedjson.put("companyid", companyId);
                jedjson.put("amount", amount);
                jedjson.put("accountid", account.getID());
                jedjson.put("debit", !isDebit);            // receipt side charges
                jedjson.put("jeid", reverseJournalEntry.getID());

                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedjson.put("jedid", JEdeatilId.getID());
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetailsnew.add(jed);
            }
            invoiceWriteOff.setIsRecovered(true);
            invoiceWriteOff.setReversejournalEntry(reverseJournalEntry);
            obj.put("invoiceNumber", invoice.getInvoiceNumber());
            obj.put("reverseJeNumber", reverseJournalEntry.getEntryNumber());
            obj.put("writeOIffId", invoiceWriteOff.getID());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accWriteOffController.getWriteOffJsonArray : " + ex.getMessage(), ex);
        }
        return obj;
    }
    
    public ModelAndView writeOffReceipts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = writeOffReceipts(request);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            issuccess = false;
            Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject writeOffReceipts(HttpServletRequest request) {
        JSONObject returnObject = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {
            String detailsJsonString = request.getParameter("invoiceDetails");
            HashMap<String, Object> requestParams = new HashMap();
            String Memo = request.getParameter("memo") != null ? request.getParameter("memo") : "";
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String createdby = sessionHandlerImpl.getUserid(request);
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) companyResult.getEntityList().get(0);
            requestParams.put(Constants.companyid, company.getCompanyID());
            requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
            DateFormat df = authHandler.getDateOnlyFormat();           
            Date writeOffDate=df.parse(request.getParameter("writeOffDate"));
            JSONArray jSONArray = new JSONArray(detailsJsonString);
            String transactionSaved="";
            try {
                for (int i = 0; i < jSONArray.length(); i++) {
                    status = txnManager.getTransaction(def);
                    JSONObject jObj = jSONArray.getJSONObject(i);
                    String receiptId = jObj.getString("billid");
                    KwlReturnObject Result = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptId);
                    Receipt receipt = (Receipt) Result.getEntityList().get(0);
                    String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                    double receiptAmountDue = 0;
                    double ExternalCurrencyRate = 0d;
                    double amountWrittenOffInReceiptCurrency;
                    double amountWrittenOffInBaseCurrency;
                    Date CreationDate = null;
                    CreationDate = receipt.getCreationDate();
                    if (receipt.isIsOpeningBalenceReceipt() && !receipt.isNormalReceipt()) {
                        ExternalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                    } else {
//                        CreationDate = receipt.getJournalEntry().getEntryDate();
                        ExternalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
                    }
                    if(receipt.isIsOpeningBalenceReceipt()){
                        receiptAmountDue = accReceiptServiceDAOobj.getReceiptAmountDue(receipt);
                    } else {
                        for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                            receiptAmountDue = advanceDetail.getAmountDue();
                        }
                    }    
                    KwlReturnObject bAmt = null;
                    if (receipt.isIsOpeningBalenceReceipt() && !receipt.isNormalReceipt()) {
                        if (receipt.isConversionRateFromCurrencyToBase()) {
                            ExternalCurrencyRate = 1 / ExternalCurrencyRate;
                        }
                    }
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, receiptAmountDue, fromcurrencyid, CreationDate, ExternalCurrencyRate);
                    amountWrittenOffInReceiptCurrency = receiptAmountDue;
                    amountWrittenOffInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                    

                    //Create ReceiptWriteOff Object
                    HashMap<String, Object> writeOffMap = new HashMap<String, Object>();
                    writeOffMap.put("receiptId", receipt.getID());
                    writeOffMap.put("writtenOffAmountInReceiptCurrency", amountWrittenOffInReceiptCurrency);
                    writeOffMap.put("writtenOffAmountInBaseCurrency", amountWrittenOffInBaseCurrency);
                    writeOffMap.put("companyId", companyId);
                    writeOffMap.put("date", writeOffDate);
                    writeOffMap.put("memo", Memo);

                    KwlReturnObject writeOffResult = accWriteOffServiceDao.saveReceiptWriteOff(writeOffMap);
                    List list = writeOffResult.getEntityList();
                    ReceiptWriteOff receiptWriteOff = (ReceiptWriteOff) list.get(0);

                    // Create JE

                    ExtraCompanyPreferences extraCompanyPreferences = null;
                    KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
                    extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);

                    int counter = 0;
                    String jeentryNumber = null;
                    boolean jeautogenflag = false;
                    String jeSeqFormatId = "";
                    String jeIntegerPart = "";
                    String jeDatePrefix = "";
                    String jeDateAfterPrefix = "";
                    String jeDateSuffix = "";
                    JournalEntryDetail jed = null;

                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyId);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, writeOffDate);
                    String nextAutoNoTemp = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number 
                    int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, number);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                    jeDataMap.put("entrydate", writeOffDate);
                    jeDataMap.put("companyid", companyId);
                    jeDataMap.put("memo", "");
                    jeDataMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
                    jeDataMap.put("externalCurrencyRate", ExternalCurrencyRate);
                    jeDataMap.put("createdby", createdby);
                    jeDataMap.put("transactionModuleid", Constants.Acc_Receipt_WriteOff_ModuleId);
                    jeDataMap.put("transactionModuleid", Constants.Acc_Receipt_WriteOff_ModuleId);
                    jeDataMap.put("transactionId", receiptWriteOff.getID());
                    JournalEntry journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                    accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                    String accountIdForReceipt="";
                    JournalEntryDetail totalJedId=null;
                    Account account=null;
                    if(receipt.isIsOpeningBalenceReceipt()){
                        accountIdForReceipt = receipt.getAccount().getID();
                    } else {
                        for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                            totalJedId  = advanceDetail.getTotalJED();
                            if(totalJedId == null){
                                continue;
                            }
                            account = totalJedId.getAccount();
                            accountIdForReceipt = account.getID();
                        }
                    }
                    Set jedetails = new HashSet();
                    
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyId);
                    jedjson.put("amount", amountWrittenOffInReceiptCurrency);
                    jedjson.put("accountid", extraCompanyPreferences.getWriteOffReceiptAccount());
                    jedjson.put("debit", false);            
                    jedjson.put("jeid", journalEntry.getID());

                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedjson.put("jedid", JEdeatilId.getID());
                    jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                    jedetails.add(jed);

                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyId);
                    jedjson.put("amount", amountWrittenOffInReceiptCurrency);
                    jedjson.put("accountid", accountIdForReceipt);
                    jedjson.put("debit", true);            
                    jedjson.put("jeid", journalEntry.getID());

                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedjson.put("jedid", JEdeatilId.getID());
                    jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                    jedetails.add(jed);

                    accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
                    journalEntry.setDetails(jedetails);
                    receiptWriteOff.setJournalEntry(journalEntry);
                    
                    if(receipt.isNormalReceipt() && !receipt.isIsOpeningBalenceReceipt()){
                        for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                                advanceDetail.setAmountDue(0);
                        }
                    } else {
                        receipt.setOpeningBalanceAmountDue(receipt.getOpeningBalanceAmountDue()-amountWrittenOffInReceiptCurrency);
                    }
                    receipt.setIsWrittenOff(true);
                    auditTrailObj.insertAuditLog(AuditAction.Written_Off_Receipt, "User " + sessionHandlerImpl.getUserFullName(request) + " Written Off a Receipt " + receipt.getReceiptNumber() +","+ " Journal Entry Number - " + journalEntry.getEntryNumber(), request, receiptWriteOff.getID());

                    txnManager.commit(status);
                    transactionSaved+="<br>";
                    transactionSaved+= messageSource.getMessage("acc.prList.gridReceiptNo", null, RequestContextUtils.getLocale(request))+": "+"<b>"+receipt.getReceiptNumber()+"</b>"+", "+messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request))+": "+"<b>"+journalEntry.getEntryNumber()+"</b>";
                    issuccess=true;
                }
                if(!transactionSaved.equals("")){
                    msg = messageSource.getMessage("acc.writeOffReceipts.savedSuccessfully", null, RequestContextUtils.getLocale(request))+" "+transactionSaved;
                }
            } catch (Exception e) {
                if (status != null) {
                    txnManager.rollback(status);
                }
                Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            issuccess = false;
            Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                returnObject.put("success", issuccess);
                returnObject.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnObject;
    }
    
    public ModelAndView getWrittenOffReceipts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        String msg = "";
        int count=0;
        boolean issuccess = false;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String ss = request.getParameter("ss")!=null?request.getParameter("ss"):"";
            String start=request.getParameter("start");
            String limit=request.getParameter("limit");
            HashMap<String,Object> requestMap = new HashMap();
            requestMap.put("companyId", companyId);
            requestMap.put("ss", ss);
            requestMap.put("start",start);
            requestMap.put("limit",limit);
            KwlReturnObject result = accWriteOffServiceDao.getWrittenOfReceipts(requestMap);
            List<ReceiptWriteOff> receiptList = result.getEntityList();
            count=result.getRecordTotalCount();
            DateFormat df = authHandler.getDateOnlyFormat(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("df", df);
            requestParams.put("list", receiptList);
            jArr = getWrittenOffReceiptsJsonArray(requestParams);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            issuccess = false;
            Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("count", count);
                
                jobj.put("data", jArr);
            } catch (JSONException ex) {
                Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getWrittenOffReceiptsJsonArray(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            List<ReceiptWriteOff> list = (List) requestParams.get("list");
            DateFormat df = (DateFormat) requestParams.get("df");

            for (ReceiptWriteOff RWO : list) {
                JSONObject obj = new JSONObject();
                Receipt receipt = RWO.getReceipt();
                boolean isOpeningReceipt = RWO.getReceipt().isIsOpeningBalenceReceipt();
                obj.put("id", RWO.getID());
                obj.put("billid", receipt.getID());
                obj.put("billno", receipt.getReceiptNumber());
                obj.put("currencysymbol", RWO.getReceipt().getCurrency().getSymbol());
                obj.put("currencyid", receipt.getCurrency().getCurrencyID());
                obj.put("personname", receipt.getCustomer().getName());
                obj.put("billdate", isOpeningReceipt ? df.format(receipt.getCreationDate()) : df.format(receipt.getJournalEntry().getEntryDate()));
                obj.put("writeOffDate", df.format(RWO.getWriteOffDate()));
                obj.put("receiptAmount", receipt.getDepositAmount());
                obj.put("receipttype", receipt.getReceipttype());
                obj.put("type", receipt.getReceipttype());
                obj.put("paymentwindowtype", receipt.getPaymentWindowType());
                obj.put("amountWrittenOff", RWO.getWrittenOffAmountInReceiptCurrency());
                obj.put("reversejeno", RWO.getReversejournalEntry() != null ? RWO.getReversejournalEntry().getEntryNumber() : "");
                obj.put("jeno", RWO.getJournalEntry() != null ? RWO.getJournalEntry().getEntryNumber() : "");
                obj.put("journalentryid", RWO.getJournalEntry() != null ? RWO.getJournalEntry().getID() : "");
                obj.put("jedate", RWO.getJournalEntry() != null ? RWO.getJournalEntry().getEntryDate() : "");
                obj.put("isrecovered", RWO.isIsRecovered());
                obj.put("memo", RWO.getMemo());
                obj.put("deleted",RWO.getReceipt().isDeleted());
                jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accWriteOffController.getWriteOffJsonArray : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    public ModelAndView recoverReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            jobj = recoverReceipt(request);
            if(jobj.getString("receiptNumber") != null){
                issuccess =true;
                msg = messageSource.getMessage("acc.writeOff.receiptsRecoveredSuccessfully", null, RequestContextUtils.getLocale(request))+"</br>";
                msg += messageSource.getMessage("acc.prList.gridReceiptNo", null, RequestContextUtils.getLocale(request))+": "+"<b>"+jobj.getString("receiptNumber")+"</b>"+", "+messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request))+": "+"<b>"+jobj.getString("reverseJeNumber")+"</b>";
                auditTrailObj.insertAuditLog(AuditAction.Written_Off_Receipt_Reverse, "User " + sessionHandlerImpl.getUserFullName(request) + " has recovered a Receipt " + jobj.getString("receiptNumber") +","+ " Journal Entry Number - " + jobj.getString("reverseJeNumber"), request, jobj.getString("writeOIffId"));
            }
             txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            issuccess = false;
            Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accWriteOffController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject recoverReceipt(HttpServletRequest request) throws ServiceException {
        JSONObject obj = new JSONObject();
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String writeOffId = request.getParameter("writeOffId") != null ? request.getParameter("writeOffId") : "";
            String createdby = sessionHandlerImpl.getUserid(request);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(ReceiptWriteOff.class.getName(), writeOffId);
            ReceiptWriteOff receiptWriteOff = (ReceiptWriteOff) result.getEntityList().get(0);

            JournalEntry JE = receiptWriteOff.getJournalEntry();
            Receipt receipt = receiptWriteOff.getReceipt();
            double amountWriteOff = receiptWriteOff.getWrittenOffAmountInReceiptCurrency();
            double amountWriteOffInBase = receiptWriteOff.getWrittenOffAmountInBaseCurrency();
           
            if(receipt.isNormalReceipt()){
                for(ReceiptAdvanceDetail detail : receipt.getReceiptAdvanceDetails()){
                    detail.setAmountDue(detail.getAmountDue()+amountWriteOff);
                }
            } else{
                receipt.setOpeningBalanceAmountDue(receipt.getOpeningBalanceAmountDue()+amountWriteOff);
            }   
            
            int counter = 0;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            JournalEntryDetail jed = null;

            HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
            JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
            JEFormatParams.put("modulename", "autojournalentry");
            JEFormatParams.put("companyid", companyId);
            JEFormatParams.put("isdefaultFormat", true);

            KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
            SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, new Date());
            String nextAutoNoTemp = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
            int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
            jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
            jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
            jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
            sequence = sequence + counter;
            String number = "" + sequence;
            String action = "" + (sequence - counter);
            nextAutoNoTemp.replaceAll(action, number);
            jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
            jeSeqFormatId = format.getID();
            jeautogenflag = true;

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMap.put(Constants.SEQNUMBER, number);
            jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMap.put("entrydate", new Date());
            jeDataMap.put("companyid", companyId);
            jeDataMap.put("memo", "");
            jeDataMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
            jeDataMap.put("externalCurrencyRate", JE.getExternalCurrencyRate());
            jeDataMap.put("createdby", createdby);
            jeDataMap.put("transactionModuleid", Constants.Acc_Receipt_WriteOff_ModuleId_Reverse);
            jeDataMap.put("transactionId",receiptWriteOff.getID());
            JournalEntry reverseJournalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
            accJournalEntryobj.saveJournalEntryByObject(reverseJournalEntry);

            Set<JournalEntryDetail> jedetails = JE.getDetails();
            Set jedetailsnew = new HashSet();
            for (JournalEntryDetail jedetail : jedetails) {
                Account account = jedetail.getAccount();
                Double amount = jedetail.getAmount();
                boolean isDebit = jedetail.isDebit();

                JSONObject jedjson = new JSONObject();
                
                jedjson.put("srno", jedetailsnew.size() + 1);
                jedjson.put("companyid", companyId);
                jedjson.put("amount", amount);
                jedjson.put("accountid", account.getID());
                jedjson.put("debit", !isDebit);            
                jedjson.put("jeid", reverseJournalEntry.getID());

                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedjson.put("jedid", JEdeatilId.getID());
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetailsnew.add(jed);
            }
            receipt.setIsWrittenOff(false);
            receiptWriteOff.setIsRecovered(true);
            receiptWriteOff.setReversejournalEntry(reverseJournalEntry);
            obj.put("receiptNumber", receipt.getReceiptNumber());
            obj.put("reverseJeNumber", reverseJournalEntry.getEntryNumber());
            obj.put("writeOIffId", receiptWriteOff.getID());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accWriteOffController.recoverReceipt : " + ex.getMessage(), ex);
        }
        return obj;
    }
}
