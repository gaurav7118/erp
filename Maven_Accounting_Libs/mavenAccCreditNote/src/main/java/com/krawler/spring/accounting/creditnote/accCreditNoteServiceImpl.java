/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.creditnote;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.purchaseorder.service.AccPurchaseOrderModuleService;
import com.krawler.spring.accounting.salesorder.accSalesOrderService;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.CommonFnController;
import com.krawler.spring.common.CommonFnControllerService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.context.MessageSourceAware;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accCreditNoteServiceImpl implements accCreditNoteService,MessageSourceAware {

    private accCreditNoteDAO accCreditNoteDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private fieldDataManager fieldDataManagercntrl;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private accAccountDAO accAccountDAOobj;
    private accInvoiceDAO accInvoiceDAOObj;
    public ImportHandler importHandler;
    public accCustomerDAO accCustomerDAOObj;
    private accVendorDAO accVendorDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private MessageSource messageSource;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private HibernateTransactionManager txnManager;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accSalesOrderService accSalesOrderServiceobj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccPurchaseOrderModuleService accPurchaseOrderModuleServiceObj;
    private AccInvoiceModuleService accInvoiceModuleService;
    private CommonFnControllerService commonFnControllerService;
    private accPaymentDAO accPaymentDAOobj;
    private accBankReconciliationDAO accBankReconciliationDAOObj;

    public void setAccBankReconciliationDAOObj(accBankReconciliationDAO accBankReconciliationDAOObj) {
        this.accBankReconciliationDAOObj = accBankReconciliationDAOObj;
    }

    
    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }

    public void setJournalEntryModuleServiceobj(AccJournalEntryModuleService journalEntryModuleServiceobj) {
        this.journalEntryModuleServiceobj = journalEntryModuleServiceobj;
    }

    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccDiscountDAO(accDiscountDAO accDiscountobj) {
        this.accDiscountobj = accDiscountobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
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

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setAccInvoiceDAO(accInvoiceDAO accInvoiceDAOObj) {
        this.accInvoiceDAOObj = accInvoiceDAOObj;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOObj) {
        this.accCustomerDAOObj = accCustomerDAOObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setAccVendorDAO(accVendorDAO accVendorDAOObj) {
        this.accVendorDAOObj = accVendorDAOObj;
    }

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public AccJournalEntryModuleService getJournalEntryModuleServiceobj() {
        return journalEntryModuleServiceobj;
    }

    public void setAccSalesOrderServiceobj(accSalesOrderService accSalesOrderServiceobj) {
        this.accSalesOrderServiceobj = accSalesOrderServiceobj;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    public void setAccPurchaseOrderModuleServiceObj(AccPurchaseOrderModuleService accPurchaseOrderModuleServiceObj) {
        this.accPurchaseOrderModuleServiceObj = accPurchaseOrderModuleServiceObj;
    }

    public void setAccInvoiceModuleService(AccInvoiceModuleService accInvoiceModuleService) {
        this.accInvoiceModuleService = accInvoiceModuleService;
    }

    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    @Override
    public List linkCreditNote(JSONObject paramJobj, String creditNoteId, Boolean isInsertAudTrail) throws ServiceException, SessionExpiredException, JSONException, AccountingException,ParseException {
        List result = new ArrayList();
        List<Date> datelist = null;
        String companyid = paramJobj.optString(Constants.companyKey);
            DateFormat df=authHandler.getDateOnlyFormatter(paramJobj);
        String cnid = "";
        int counter = 0;
        if (!StringUtil.isNullOrEmpty(creditNoteId)) {
            cnid = creditNoteId;
        } else {
            cnid = paramJobj.optString("cnid");
        }
        Date maxLinkingDate = null;
        boolean isNoteAlso = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNoteAlso",null))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
            isNoteAlso = Boolean.parseBoolean(paramJobj.optString("isNoteAlso"));
        }
//            String linkingdate = (String) paramJobj.optString("linkingdate");//Commented because of ERP-36411
        DateFormat dateformat = authHandler.getDateOnlyFormat();
//            if (!StringUtil.isNullOrEmpty(linkingdate)) {
//                try {
//                    maxLinkingDate = dateformat.parse(linkingdate);
//                } catch (ParseException ex) {
//                    Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
        String entryNumber = paramJobj.optString("number");
        String amounts[] = paramJobj.optString("amounts").split(",");
        String invoiceDetails = paramJobj.optString("invoicedetails");
        String baseCurrency = paramJobj.optString(Constants.currencyKey);
        JSONArray jArr = new JSONArray(invoiceDetails);
        Map<String,Object> counterMap=new HashMap<>();
        for (int k = 0; k < jArr.length(); k++) {
            JSONObject jobj = jArr.getJSONObject(k);

            if (StringUtil.isNullOrEmpty(jobj.getString(Constants.billid))) {
                continue;
            }

            double usedcnamount = 0d;
            double typeFigure = 0d;
            int typeOfFigure = 1;
            if (!StringUtil.isNullOrEmpty(amounts[k])) {
                usedcnamount = Double.parseDouble((String) amounts[k]);
            } else {
                usedcnamount = 0;
            }
            if (usedcnamount == 0) {
                continue;
            }
            if(!StringUtil.isNullOrEmpty(jobj.optString("typeFigure"))){
                typeFigure = jobj.optDouble("typeFigure",0.0);
            }
            else if(isNoteAlso&&!StringUtil.isNullOrEmpty(paramJobj.optString("linkNumber",null))){
               typeFigure = jobj.optDouble("typeFigure",usedcnamount);  
            }
            if(!StringUtil.isNullOrEmpty(jobj.optString("typeOfFigure"))){
                typeOfFigure = jobj.optInt("typeOfFigure",1);
            }
            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString(Constants.billid));
            Invoice invObj = (Invoice) grresult.getEntityList().get(0);

            KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
            CreditNote creditNote = (CreditNote) cnObj.getEntityList().get(0);

            grresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) grresult.getEntityList().get(0);

            Set<CreditNoteDetail> newcndetails = new HashSet<CreditNoteDetail>();
            double cnamountdue = creditNote.getCnamountdue();
            if (!creditNote.isOpenflag() || cnamountdue <= 0) {
                throw new AccountingException(messageSource.getMessage("acc.field.CreditNotehasbeenalreadyutilized.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }

            double amountReceived = usedcnamount;           //amount of DN 
            double amountReceivedConverted = usedcnamount;
            double adjustedRate = 1;
            double exchangeRateforTransaction = jobj.optDouble("exchangeratefortransaction", 1.0);

            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(invObj.getCurrency().getCurrencyID()) && !invObj.getCurrency().getCurrencyID().equals(creditNote.getCurrency().getCurrencyID())) {
                // adjusted exchange rate used to handle case like ERP-34884
                adjustedRate = exchangeRateforTransaction;
                if (jobj.optDouble("amountdue", 0) != 0 && jobj.optDouble("amountDueOriginal", 0) != 0) {
                    adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0);
                }
                amountReceivedConverted = amountReceived / adjustedRate;
                amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
            } else {
                amountReceivedConverted = authHandler.round(amountReceived, companyid);
            }
            Date maxDate = null;
                String linkingdate =null;
            if (jobj.has("linkingdate") && jobj.get("linkingdate") != null) {
                linkingdate = (String) jobj.get("linkingdate");
                if (!StringUtil.isNullOrEmpty(linkingdate)) {
                    try {
                        maxLinkingDate = dateformat.parse(linkingdate);
                    } catch (ParseException ex) {
                        Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            CreditNoteDetail cndetailObj = new CreditNoteDetail();
            if (creditNote.getCntype() != 3 && creditNote.isOpenflag() && creditNote.getCnamount() == creditNote.getCnamountdue()) {//If CN is used for first time.
                Set<CreditNoteDetail> cndetails = (Set<CreditNoteDetail>) creditNote.getRows();
                Iterator itr = cndetails.iterator();
                while (itr.hasNext()) {
                    cndetailObj = (CreditNoteDetail) itr.next();

                    if (invObj != null) {
                        cndetailObj.setInvoice(invObj);
                        /*
                         * code to save linking date of CN and Invoice. This
                         * linking date used while calculating due amount of CN
                         * in Aged/SOA report
                         */
                        if (maxLinkingDate != null) {
                            maxDate = maxLinkingDate;
                        } else {
                            Date linkingDate = null;
                            try {
                                linkingDate = df.parse(df.format(new Date())); //formatting and parsing again with dateonlyformmater for removing time 
                            } catch (ParseException ex) {
                                linkingDate = new Date();
                            }
//                                Date invDate = invObj.isIsOpeningBalenceInvoice() ? invObj.getCreationDate() : invObj.getJournalEntry().getEntryDate();
//                                Date cnDate = creditNote.isIsOpeningBalenceCN() ? creditNote.getCreationDate() : creditNote.getJournalEntry().getEntryDate();
                            Date invDate = invObj.getCreationDate();
                            Date cnDate = creditNote.getCreationDate();
//                                maxDate = Math.max(Math.max(linkingDate.getTime(), invDate.getTime()), cnDate.getTime());
                            datelist = new ArrayList<Date>();
                            datelist.add(linkingDate);
                            datelist.add(invDate);
                            datelist.add(cnDate);
                            Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                            maxDate = datelist.get(datelist.size() - 1);
                        }
                        cndetailObj.setInvoiceLinkDate(maxDate);
                    }
                    //change
                    double invoiceOriginalAmt = 0d;
                    if (!invObj.isNormalInvoice() && invObj.isIsOpeningBalenceInvoice()) {
                        invoiceOriginalAmt = invObj.getOriginalOpeningBalanceAmount();
                    } else {
                        invoiceOriginalAmt = invObj.getCustomerEntry().getAmount();
                    }
                    double gsOriginalAmtConverted = invoiceOriginalAmt;
                    gsOriginalAmtConverted = invoiceOriginalAmt / adjustedRate;
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", usedcnamount);         //amount in DN currency
                    discjson.put("amountinInvCurrency", amountReceivedConverted);
                    discjson.put("inpercent", false);
                    discjson.put("originalamount", invoiceOriginalAmt);
                    discjson.put(Constants.companyKey, companyid);
                    discjson.put("typeOfFigure", typeOfFigure);
                    discjson.put("typeFigure", typeFigure);
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    cndetailObj.setDiscount(discount);
                    cndetailObj.setExchangeRateForTransaction(exchangeRateforTransaction);
                    newcndetails.add(cndetailObj);
                }
            } else {
                Set<CreditNoteDetail> cndetails = (Set<CreditNoteDetail>) creditNote.getRows();
                Iterator itr = cndetails.iterator();
                int i = 0;
                while (itr.hasNext()) {
                    cndetailObj = (CreditNoteDetail) itr.next();
                    newcndetails.add(cndetailObj);
                    i++;
                }

                cndetailObj = new CreditNoteDetail();

                if (invObj != null) {
                    cndetailObj.setInvoice(invObj);
                    /*
                     * code to save linking date of CN and Invoice. This linking
                     * date used while calculating due amount of CN in Aged/SOA
                     * report
                     */
                    if (maxLinkingDate != null) {
                        maxDate = maxLinkingDate;
                    } else {
                        Date linkingDate = null;
                        try {
                            linkingDate = df.parse(df.format(new Date())); //formatting and parsing again with dateonlyformmater for removing time 
                        } catch (ParseException ex) {
                            linkingDate = new Date();
                        }
//                            Date invDate = invObj.isIsOpeningBalenceInvoice() ? invObj.getCreationDate() : invObj.getJournalEntry().getEntryDate();
//                            Date cnDate = creditNote.isIsOpeningBalenceCN() ? creditNote.getCreationDate() : creditNote.getJournalEntry().getEntryDate();                                              
                        Date invDate = invObj.getCreationDate();
                        Date cnDate = creditNote.getCreationDate();
//                            maxDate = Math.max(Math.max(linkingDate.getTime(), invDate.getTime()), cnDate.getTime());
                        datelist = new ArrayList<Date>();
                        datelist.add(linkingDate);
                        datelist.add(invDate);
                        datelist.add(cnDate);
                        Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                        maxDate = datelist.get(datelist.size() - 1);
                    }
                    cndetailObj.setInvoiceLinkDate(maxDate);
                }

                cndetailObj.setSrno(i + 1);
                cndetailObj.setTotalDiscount(0.00);
                cndetailObj.setCompany(company);
                cndetailObj.setMemo("");
                cndetailObj.setCreditNote(creditNote);
                cndetailObj.setID(UUID.randomUUID().toString());

                double invoiceOriginalAmt = 0d;
                if (invObj.isNormalInvoice()) {
                    invoiceOriginalAmt = invObj.getCustomerEntry().getAmount();
                } else {// for only opening balance Invoices
                    invoiceOriginalAmt = invObj.getOriginalOpeningBalanceAmount();
                }

                JSONObject discjson = new JSONObject();
                discjson.put("discount", usedcnamount);
                discjson.put("amountinInvCurrency", amountReceivedConverted);        //amount in DN currency
                discjson.put("inpercent", false);
                discjson.put("originalamount", invoiceOriginalAmt);
                discjson.put(Constants.companyKey, companyid);
                discjson.put("typeOfFigure", typeOfFigure);
                discjson.put("typeFigure", typeFigure);
                KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                Discount discount = (Discount) dscresult.getEntityList().get(0);
                cndetailObj.setDiscount(discount);
                cndetailObj.setExchangeRateForTransaction(exchangeRateforTransaction);
                newcndetails.add(cndetailObj);
            }

            double amountDue = cnamountdue - usedcnamount;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
            double externalCurrencyRate = 1d;
            boolean isopeningBalanceCN = creditNote.isIsOpeningBalenceCN();
            Date cnCreationDate = null;
            cnCreationDate = creditNote.getCreationDate();
            if (!creditNote.isNormalCN() && creditNote.isIsOpeningBalenceCN()) {
                externalCurrencyRate = creditNote.getExchangeRateForOpeningTransaction();
            } else {
//                    cnCreationDate = creditNote.getJournalEntry().getEntryDate();
                externalCurrencyRate = creditNote.getJournalEntry().getExternalCurrencyRate();
            }
            String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
            if (isopeningBalanceCN && creditNote.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
            }
            double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
            HashMap<String, Object> credithm = new HashMap<String, Object>();
            credithm.put("cndetails", newcndetails);
            credithm.put("cnid", creditNote.getID());
            credithm.put("cnamountdue", amountDue);
            credithm.put("openingBalanceAmountDue", authHandler.round(amountDue, companyid));
            credithm.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);
            credithm.put("openflag", (cnamountdue - usedcnamount) <= 0 ? false : true);

                if (paramJobj.optString("entrynumber",null) != null) {
                credithm.put("entrynumber", paramJobj.optString("entrynumber"));
            }
                if (paramJobj.optString("autogenerated",null) != null) {
                credithm.put("autogenerated", paramJobj.get("autogenerated"));

            }
                if (paramJobj.optString("seqformat",null) != null) {
                credithm.put(Constants.SEQFORMAT, paramJobj.get("seqformat"));

            }
                if (paramJobj.optString("seqnumber",null) != null) {
                credithm.put(Constants.SEQNUMBER, paramJobj.get("seqnumber"));

            }
            if (paramJobj.optString(Constants.DATEPREFIX,null) != null) {
                credithm.put(Constants.DATEPREFIX, paramJobj.optString(Constants.DATEPREFIX));
            }
            if (paramJobj.optString(Constants.DATESUFFIX,null) != null) {
                credithm.put(Constants.DATESUFFIX, paramJobj.optString(Constants.DATESUFFIX));
            }
            if (paramJobj.optString(Constants.DATEAFTERPREFIX,null) != null) {  //SDP-14953
                credithm.put(Constants.DATEAFTERPREFIX, paramJobj.optString(Constants.DATEAFTERPREFIX));
            }
            KwlReturnObject result1 = accCreditNoteDAOobj.updateCreditNote(credithm);

            // Update Invoice base amount due. We have to consider Invoice currency rate to calculate.
            externalCurrencyRate = 1d;
            boolean isopeningBalanceINV = invObj.isIsOpeningBalenceInvoice();
            Date noteCreationDate = null;
            noteCreationDate = creditNote.getCreationDate();
            externalCurrencyRate = isopeningBalanceCN ? creditNote.getExchangeRateForOpeningTransaction() : creditNote.getJournalEntry().getExternalCurrencyRate();
            fromcurrencyid = creditNote.getCurrency().getCurrencyID();
            if (isopeningBalanceINV && invObj.isConversionRateFromCurrencyToBase()) {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
            }
            totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

            /*
             * Store the date on which the amount due has been set to zero
             * If Approvestatuslevel is not equall to 11 it means CN is going for pending approval. In this case we does not need to update invoice due amount. It will get updated at final approval
             */

            if (creditNote.getApprovestatuslevel() == 11) {
                KwlReturnObject invoiceResult = updateInvoiceAmountDueAndReturnResult(invObj, company, amountReceivedConverted, totalBaseAmountDue);
                if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                    Invoice inv = (Invoice) invoiceResult.getEntityList().get(0);
                    if (inv.isIsOpeningBalenceInvoice() && inv.getOpeningBalanceAmountDue() == 0) {
                        try {
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            if (creditNote.isIsOpeningBalenceCN() && creditNote != null && creditNote.getCreationDate() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                            } else if (!creditNote.isIsOpeningBalenceCN() && creditNote != null && creditNote.getJournalEntry() != null && creditNote.getJournalEntry().getCreatedOn() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                            }
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    } else if (inv.getInvoiceamountdue() == 0) {
                        try {
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            if (creditNote.isIsOpeningBalenceCN() && creditNote != null && creditNote.getCreationDate() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                            } else if (!creditNote.isIsOpeningBalenceCN() && creditNote != null && creditNote.getJournalEntry() != null && creditNote.getJournalEntry().getCreatedOn() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                            }
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    }
                }
            }

            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));

            if (isInsertAudTrail) {
                auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has linked Credit Note " + creditNote.getCreditNoteNumber() + " with Customer Invoice " + invObj.getInvoiceNumber() + ".", auditRequestParams, creditNote.getID());
            }

            /*
             * Start gains/loss calculation Calculate Gains/Loss if Invoice
             * exchange rate changed at the time of linking with CN
             */
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            if (preferences.getForeignexchange() == null) {
                throw new AccountingException(messageSource.getMessage("acc.common.forex", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            }
            if (isopeningBalanceCN && creditNote.isConversionRateFromCurrencyToBase()) {
                externalCurrencyRate = 1 / externalCurrencyRate;
                externalCurrencyRate = externalCurrencyRate;
            }
            Map<String,Object> mapForForexGainLoss = new HashMap<>();
                mapForForexGainLoss.put("cn",creditNote);
                mapForForexGainLoss.put("invoice",invObj);
                mapForForexGainLoss.put("basecurreny",baseCurrency);
                mapForForexGainLoss.put("companyid",companyid);
                mapForForexGainLoss.put(Constants.creationdate,creditNote.getCreationDate());
                mapForForexGainLoss.put("exchangeratefortransaction",exchangeRateforTransaction);
                mapForForexGainLoss.put("recinvamount",usedcnamount);
                mapForForexGainLoss.put("externalcurrencyrate",externalCurrencyRate);
                mapForForexGainLoss.put("dateformat",authHandler.getDateOnlyFormat());
            double amountDiff = getForexGainLossForCreditNote(mapForForexGainLoss);
            if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                boolean rateDecreased = false;
                if (amountDiff < 0) {
                    rateDecreased = true;
                }
                JournalEntry journalEntry = null;
                Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
                String jeentryNumber = null;
                boolean jeautogenflag = false;
                String jeSeqFormatId = "";
                String datePrefix = "";
                String dateafterPrefix = "";
                String dateSuffix = "";
                Date entryDate = null;
                if (maxLinkingDate != null) {
                    entryDate = new Date(maxLinkingDate.getTime());
                } else {
                    entryDate = new Date();
                }
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put(Constants.companyKey, companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                    int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                    String nextAutoNUmber=(String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNUmber.replaceAll(action, number);
                    jeentryNumber = nextAutoNUmber.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, number);
                    jeDataMap.put(Constants.DATEPREFIX, datePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, dateSuffix);
                }
                    jeDataMap.put("entrydate",dateformat.parse(linkingdate));
                jeDataMap.put(Constants.companyKey, companyid);
                jeDataMap.put("memo", "Exchange Gains/Loss posted against Advance Receipt '" + creditNote.getCreditNoteNumber() + "' linked to Invoice '" + invObj.getInvoiceNumber() + "'");
                jeDataMap.put(Constants.currencyKey, creditNote.getCurrency().getCurrencyID());
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeDataMap.put("isexchangegainslossje", true);
                    jeDataMap.put("transactionId",creditNote.getID() );
                jeDataMap.put("transactionModuleid", Constants.Acc_Credit_Note_ModuleId);
                journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                boolean isDebit = rateDecreased ? true : false;
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", 1);
                jedjson.put(Constants.companyKey, companyid);
                jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                jedjson.put("accountid", preferences.getForeignexchange().getID());
                jedjson.put("debit", isDebit);
                jedjson.put("jeid", journalEntry.getID());
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                Set<JournalEntryDetail> detail = new HashSet();
                detail.add(jed);

                jedjson = new JSONObject();
                jedjson.put("srno", 2);
                jedjson.put(Constants.companyKey, companyid);
                jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                jedjson.put("accountid", invObj.getAccount().getID());
                jedjson.put("debit", !isDebit);
                jedjson.put("jeid", journalEntry.getID());
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                detail.add(jed);
                journalEntry.setDetails(detail);
                accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                // Save link JE iformation in DN details
                cndetailObj.setLinkedGainLossJE(journalEntry.getID());
                newcndetails.add(cndetailObj);
                credithm.put("cndetails", newcndetails);
                KwlReturnObject result2 = accCreditNoteDAOobj.updateCreditNote(credithm);
                counter++;
            }
                // End Gains/Loss Calculation
            //JE For Receipt which is of Opening Type
            if(counterMap.containsKey("counter")){
                counter=(Integer)counterMap.get("counter");
            }
            counterMap.put("counter", counter);
            if (creditNote != null && (creditNote.isIsOpeningBalenceCN() || creditNote.isOtherwise())) {
                String basecurrency = paramJobj.getString(Constants.globalCurrencyKey);
                double finalAmountReval = ReevalJournalEntryForCreditNote(paramJobj, creditNote, amountReceived, exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                    /**
                         * added transactionID and transactionModuleID to
                         * Realised JE.
                     */
                    counterMap.put("transactionModuleid", creditNote.isIsOpeningBalenceCN() ? (creditNote.iscNForCustomer() ? Constants.Acc_opening_Customer_CreditNote : Constants.Acc_opening_Vendor_CreditNote) : Constants.Acc_Credit_Note_ModuleId);
                    counterMap.put("transactionId", creditNote.getID());
                        String revaljeid = PostJEFORReevaluation(paramJobj, -(finalAmountReval),companyid, preferences, basecurrency, cndetailObj.getRevalJeId(),counterMap);
                    cndetailObj.setRevalJeId(revaljeid);
                }
            }
            //JE For Debit which is Linked to Receipt
            if (invObj != null) {
                double finalAmountReval = ReevalJournalEntryForInvoice(paramJobj, invObj, amountReceived, exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                        String basecurrency =paramJobj.getString(Constants.globalCurrencyKey);
                    /**
                         * added transactionID and transactionModuleID to
                         * Realised JE.
                     */
                    counterMap.put("transactionModuleid", invObj.isIsOpeningBalenceInvoice() ? Constants.Acc_opening_Sales_Invoice : Constants.Acc_Invoice_ModuleId);
                    counterMap.put("transactionId", invObj.getID());
                        String revaljeid = PostJEFORReevaluation(paramJobj, finalAmountReval, companyid, preferences, basecurrency, cndetailObj.getRevalJeIdInvoice(),counterMap);
                    cndetailObj.setRevalJeIdInvoice(revaljeid);

                }
            }
            if (invObj != null) {

                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                requestParamsLinking.put("linkeddocid", cnid);
                requestParamsLinking.put("docid", invObj.getID());
                requestParamsLinking.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                requestParamsLinking.put("linkeddocno", entryNumber);
                requestParamsLinking.put("sourceflag", 0);
                KwlReturnObject result3 = accInvoiceDAOObj.saveInvoiceLinking(requestParamsLinking);


                /*
                 * saving linking informaion of Purchase Invoice while linking
                 * with DebitNote
                 */

                requestParamsLinking.put("linkeddocid", invObj.getID());
                requestParamsLinking.put("docid", cnid);
                requestParamsLinking.put("moduleid", Constants.Acc_Invoice_ModuleId);
                requestParamsLinking.put("linkeddocno", invObj.getInvoiceNumber());
                requestParamsLinking.put("sourceflag", 1);
                result1 = accCreditNoteDAOobj.saveCreditNoteLinking(requestParamsLinking);
            }
        }
        return result;
    }
    /**
     * Following Method used for
     * 1) To Update Amount Due of Credit Note Overcharge and Sales Invoice
     * 2) To Update Linking Information
     * @param paramJobj
     * @param creditNoteId
     * @param isInsertAudTrail
     * @return result
     * @throws ServiceException 
     * @throws SessionExpiredException
     * @throws JSONException
     * @throws AccountingException
     * @throws ParseException 
     */
    public List updateAmountDueCreditNoteOvercharge(JSONObject paramJobj, String creditNoteId, boolean isInsertAudTrail) throws ServiceException, SessionExpiredException, JSONException, AccountingException, ParseException {

        List result = new ArrayList();
        List<Date> datelist = null;
        boolean isEdit  = false;
        if (paramJobj.has("isEdit")) {
            isEdit = Boolean.parseBoolean((String) paramJobj.get("isEdit"));
        }
        String invoiceDetails = paramJobj.optString("detail", null) ;
        
        String cnid = !StringUtil.isNullOrEmpty(creditNoteId) ? creditNoteId : null;
        HashMap<String, Object> credithm = new HashMap<>();
        try {
            DateFormat df = authHandler.getDateOnlyFormatter(paramJobj);
            KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
            CreditNote creditNote = (CreditNote) cnObj.getEntityList().get(0);
            double externalCurrencyRate = 0.0;
            DateFormat dateformat = authHandler.getDateOnlyFormat();
            Map<String, Double> invdetailsmap = new HashMap<>();
            Map<String, Object> invLinkingDate = new HashMap<>();
            double finalInvAmtDue = 0, finalCNAmtDue = 0;
            JSONArray jArr = new JSONArray(invoiceDetails);
            for (int k = 0; k < jArr.length(); k++) {
                /**
                 * To Update Amount Due of CN and Invoice.
                 */
                JSONObject invdetail = jArr.getJSONObject(k);
                String invId = !isEdit ? invdetail.optString(Constants.billid) : invdetail.optString("linkid");
                double adjAmount = Double.parseDouble(invdetail.optString(Constants.amount));
                if (StringUtil.isNullOrEmpty(invdetail.optString(Constants.billid))) {
                    continue;
                }
                if (invdetailsmap.containsKey(invId)) {
                    adjAmount += invdetailsmap.get(invId);
                }
                invdetailsmap.put(invId, adjAmount);
                String linkingdate = null;
                Date maxLinkingDate = null;
                if (invdetail.has("linkingdate") && invdetail.get("linkingdate") != null) {
                    linkingdate = (String) invdetail.get("linkingdate");
                    if (!StringUtil.isNullOrEmpty(linkingdate)) {
                        try {
                            maxLinkingDate = dateformat.parse(linkingdate);
                        } catch (ParseException ex) {
                            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                invLinkingDate.put(invId, maxLinkingDate);
            }
            /**
             * if Invoice Amount Due is Smaller than CN Adjusted Amount then
             * following logic will be Invoice amount due to 0 and remaining
             * adjusted amount for DN Amount Due.
             */

            for (Map.Entry<String, Double> entrySet : invdetailsmap.entrySet()) {
                String invId = entrySet.getKey();
                double adjAmount = entrySet.getValue();
                double CNKnowkOffAmount = 0;
                KwlReturnObject bAmt = null;
                KwlReturnObject invresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invId);
                Invoice invObj = (Invoice) invresult.getEntityList().get(0);
                String fromcurrencyid = invObj.getCurrency().getCurrencyID();
                double invAmtDue = invObj.getInvoiceamountdue();
                // Global Level Tax Calculation.
                if (creditNote.getTax() != null) {
                    KwlReturnObject perresult = accTaxObj.getTaxPercent((String) paramJobj.get(Constants.companyKey), creditNote.getCreationDate(), creditNote.getTax().getID());
                    double taxPercent = (Double) perresult.getEntityList().get(0);
                    adjAmount  += adjAmount * (taxPercent/100);
                }
                if (invAmtDue > adjAmount) {
                    finalInvAmtDue = (invAmtDue - adjAmount);
                } else {
                    // for minus amount due of invoice
                    if (invAmtDue < 0) {
                        finalInvAmtDue = invAmtDue;
                        finalCNAmtDue += adjAmount;
                        CNKnowkOffAmount = adjAmount;
                    } else {
                        finalInvAmtDue = 0;
                        finalCNAmtDue += (adjAmount - invAmtDue);
                        CNKnowkOffAmount = (adjAmount - invAmtDue);
                    }
                }
                /**
                 * To Update Invoice Amount Due.
                 */
                JSONObject invjson = new JSONObject();
                invjson.put("invoiceid", invObj.getID());
                invjson.put(Constants.companyKey, paramJobj.opt(Constants.companyKey));
                if (finalInvAmtDue != 0) {
                    if (invObj.isIsOpeningBalenceInvoice()) {
                        externalCurrencyRate = invObj.getExchangeRateForOpeningTransaction();
                        if (invObj.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(null, finalInvAmtDue, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(null, finalInvAmtDue, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                        }
                        double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                        invjson.put(Constants.openingBalanceAmountDue, finalInvAmtDue);
                        invjson.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);

                    } else {
                        externalCurrencyRate = invObj.getExternalCurrencyRate();
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(null, finalInvAmtDue, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                        double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                        invjson.put(Constants.invoiceamountdue, finalInvAmtDue);
                        invjson.put(Constants.invoiceamountdueinbase, totalBaseAmountDue);
                    }
                } else if (finalInvAmtDue == 0) {
                    if (invObj.isIsOpeningBalenceInvoice()) {
                        try {
                            df = authHandler.getDateOnlyFormatter(paramJobj);
                            HashMap<String, Object> dataMap = new HashMap<>();
                            dataMap.put(Constants.amountDueDate, df.parse(paramJobj.optString("creationdate")));
                            accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(invObj, dataMap);
                        } catch (SessionExpiredException | JSONException | ParseException ex) {
                            System.out.println("" + ex.getMessage());
                        }
                        invjson.put(Constants.openingBalanceAmountDue, 0);
                        invjson.put(Constants.openingBalanceBaseAmountDue, 0);
                    } else if (invObj.isNormalInvoice()) {
                        try {
                            df = authHandler.getDateOnlyFormatter(paramJobj);
                            HashMap<String, Object> dataMap = new HashMap<>();
                            dataMap.put(Constants.amountDueDate, df.parse(paramJobj.optString("creationdate")));
                            accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(invObj, dataMap);
                        } catch (SessionExpiredException | JSONException | ParseException ex) {
                            System.out.println("" + ex.getMessage());
                        }
                        invjson.put(Constants.invoiceamountdue, 0);
                        invjson.put(Constants.invoiceamountdueinbase, 0);
                    }
                }
                accInvoiceDAOObj.updateInvoice(invjson, null);
                JSONObject jobj = new JSONObject();
                jobj.put("cnid", creditNote.getID());
                jobj.put("linkedDocId", invObj.getID());
                jobj.put("linkedCNKnowkOffAmount", CNKnowkOffAmount);
                jobj.put(Constants.companyid, paramJobj.opt(Constants.companyKey));
                if (isEdit) {
                    accCreditNoteDAOobj.deleteCreditNoteOverchargeAmountLinking(jobj);
                }
                accCreditNoteDAOobj.saveCreditNoteOverchargeAmountLinking(jobj);
                /**
                 * Update Linking Information.
                 */
                if (invObj != null) {
                    HashMap<String, Object> requestParamsLinking = new HashMap<>();
                    requestParamsLinking.put("linkeddocid", cnid);
                    requestParamsLinking.put("docid", invObj.getID());
                    requestParamsLinking.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                    requestParamsLinking.put("linkeddocno", creditNote.getCreditNoteNumber());
                    requestParamsLinking.put("sourceflag", 0);
                    accInvoiceDAOObj.saveInvoiceLinking(requestParamsLinking);

                    /**
                     * saving linking information of Purchase Invoice while
                     * linking with CreditNote.
                     */
                    requestParamsLinking.put("linkeddocid", invObj.getID());
                    requestParamsLinking.put("docid", cnid);
                    requestParamsLinking.put("moduleid", Constants.Acc_Invoice_ModuleId);
                    requestParamsLinking.put("linkeddocno", invObj.getInvoiceNumber());
                    requestParamsLinking.put("sourceflag", 1);
                    accCreditNoteDAOobj.saveCreditNoteLinking(requestParamsLinking);

                   
                    if (isInsertAudTrail) {
                        Map<String, Object> auditRequestParams = new HashMap<>();
                        auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                        auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                        auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));

                        String cnNumber = paramJobj.optString("entrynumber");
                        auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has linked Credit Note " + cnNumber +" with Customer Invoice " + invObj.getInvoiceNumber() + ".", auditRequestParams, creditNote.getID());
                    }

                    // @TO DO 
                    /**
                     * 1 To Post Gain and Loss Entry 2) To
                     * Post Re-Evaluation Entry.
                     */
                }

            }
            /**
             * To Update CN Amount Due.
             */
            credithm.put("cnid", creditNote.getID());
            credithm.put(Constants.companyKey, paramJobj.opt(Constants.companyKey));
            credithm.put(Constants.cnamountdue, finalCNAmtDue);
            credithm.put("openflag", (finalCNAmtDue != 0));
            if (paramJobj.optString("entrynumber", null) != null) {
                credithm.put("entrynumber", paramJobj.optString("entrynumber"));
            }
            if (paramJobj.optString("autogenerated", null) != null) {
                credithm.put("autogenerated", paramJobj.get("autogenerated"));

            }
            if (paramJobj.optString("seqformat", null) != null) {
                credithm.put(Constants.SEQFORMAT, paramJobj.get("seqformat"));

            }
            if (paramJobj.optString("seqnumber", null) != null) {
                credithm.put(Constants.SEQNUMBER, paramJobj.get("seqnumber"));

            }
            if (paramJobj.optString(Constants.DATEPREFIX, null) != null) {
                credithm.put(Constants.DATEPREFIX, paramJobj.optString(Constants.DATEPREFIX));
            }
            if (paramJobj.optString(Constants.DATESUFFIX, null) != null) {
                credithm.put(Constants.DATESUFFIX, paramJobj.optString(Constants.DATESUFFIX));
            }
            if (paramJobj.optString(Constants.DATEAFTERPREFIX, null) != null) {  //SDP-14953
                credithm.put(Constants.DATEAFTERPREFIX, paramJobj.optString(Constants.DATEAFTERPREFIX));
            }
            accCreditNoteDAOobj.updateCreditNote(credithm);

        } catch (ServiceException | JSONException | NumberFormatException ex) {
            throw new AccountingException(ex.getMessage(), ex);
        }
        return result;
    }
    public double checkFxGainLossOnLinkInvoices(Invoice gr, double newInvoiceExchageRate, double paymentExchangeRate, double recinvamount, String paymentCurrency, String baseCurrency, String companyid) throws ServiceException {
        double amount = 0;
        HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
        GlobalParams.put(Constants.companyKey, companyid);
        GlobalParams.put(Constants.globalCurrencyKey, baseCurrency);
        double goodsReceiptExchangeRate = 0d;
        Date goodsReceiptCreationDate = null;
        boolean isopeningBalanceInvoice = gr.isIsOpeningBalenceInvoice();
        if (gr.isNormalInvoice()) {
            goodsReceiptExchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
        } else {
            if (gr.isConversionRateFromCurrencyToBase()) {
                goodsReceiptExchangeRate = 1 / gr.getExchangeRateForOpeningTransaction();
                goodsReceiptExchangeRate = authHandler.round(goodsReceiptExchangeRate, companyid);
            } else {
                goodsReceiptExchangeRate = gr.getExchangeRateForOpeningTransaction();
            }
        }
        goodsReceiptCreationDate = gr.getCreationDate();

        boolean revalFlag = false;

        Map<String, Object> invoiceId = new HashMap<>();
        invoiceId.put("invoiceid", gr.getID());
        invoiceId.put(Constants.companyKey, companyid);
        KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
        if (history != null) {
            goodsReceiptExchangeRate = history.getEvalrate();
            revalFlag = true;
        }
        String currid = gr.getCurrency().getCurrencyID();
        KwlReturnObject bAmt = null;
        if (currid.equalsIgnoreCase(paymentCurrency)) {
            if (history == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
            } else {
                double paymentExternalCurrencyRate = paymentExchangeRate;
                if (goodsReceiptExchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
                }
            }
        } else {
            if (history == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
            } else {
                double paymentExternalCurrencyRate = paymentExchangeRate;
                if (goodsReceiptExchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
                }
            }
        }
        double oldrate = (Double) bAmt.getEntityList().get(0);
        double newrate = 0.0;
        double ratio = 0;
        if (newInvoiceExchageRate != oldrate && newInvoiceExchageRate != 0.0
                && Math.abs(newInvoiceExchageRate - oldrate) >= 0.000001) {
            newrate = newInvoiceExchageRate;
            ratio = oldrate - newrate;
            amount = (recinvamount - (recinvamount / newrate) * oldrate);
        }
        return amount;
    }

    public double getForexGainLossForCreditNote(Map<String,Object> requestParams) throws ServiceException, SessionExpiredException {
        double amount = 0, actualAmount = 0;
        try {
            String basecurrency = requestParams.get("basecurreny").toString();
            String companyid = requestParams.get(Constants.companyKey).toString();
            DateFormat dateformat = (DateFormat) requestParams.get(Constants.RES_DATEFORMAT);
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put(Constants.companyKey, companyid);
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put(Constants.RES_DATEFORMAT, dateformat);
            Date creationDate = (Date) requestParams.get(Constants.creationdate);
            CreditNote cn = (CreditNote) requestParams.get("cn");
            String currencyid = cn.getCurrency().getCurrencyID();
            Invoice gr = (Invoice) requestParams.get("invoice");
            double exchangeratefortransaction = (double) requestParams.get("exchangeratefortransaction");
            double recinvamount = (double) requestParams.get("recinvamount");
            double ratio = 0;
            double newrate = 0.0;
            boolean revalFlag = false;
            boolean isopeningBalanceInvoice = gr.isIsOpeningBalenceInvoice();

            boolean isopeningBalancePayment = cn.isIsOpeningBalenceCN();
            boolean isConversionRateFromCurrencyToBase = cn.isConversionRateFromCurrencyToBase();
            double externalCurrencyRate = (double) requestParams.get("externalcurrencyrate");
            double exchangeRate = 0d;
            Date goodsReceiptCreationDate = null;
            if (gr.isNormalInvoice()) {
                exchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
            } else {
                exchangeRate = gr.getExchangeRateForOpeningTransaction();
                    if(gr.isConversionRateFromCurrencyToBase()){
                        exchangeRate=1/exchangeRate;
                }
            }
            goodsReceiptCreationDate = gr.getCreationDate();

            HashMap<String, Object> invoiceId = new HashMap<String, Object>();
            invoiceId.put("invoiceid", gr.getID());
            invoiceId.put(Constants.companyKey, companyid);
            KwlReturnObject result = null;
            result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (history != null) {
                exchangeRate = history.getEvalrate();
                newrate = exchangeratefortransaction;
                revalFlag = true;
            }
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (gr.getCurrency() != null) {
                currid = gr.getCurrency().getCurrencyID();
            }

            KwlReturnObject bAmt = null;
            if (currid.equalsIgnoreCase(currencyid)) {
                            double paymentExternalCurrencyRate=externalCurrencyRate;
                // If document is revaluated then document from same currency are linked on same rate i.e revaluation rate. 
                            if(exchangeRate!=paymentExternalCurrencyRate && !revalFlag){
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate,paymentExternalCurrencyRate);
                            }else{
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                }
            } else {
                            double paymentExternalCurrencyRate=externalCurrencyRate;
                            if(exchangeRate!=paymentExternalCurrencyRate && !revalFlag){
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate,paymentExternalCurrencyRate);
                            }else{
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                }
            }
            double oldrate = (Double) bAmt.getEntityList().get(0);
            if (exchangeratefortransaction != oldrate && exchangeratefortransaction != 0.0 && Math.abs(exchangeratefortransaction - oldrate) >= 0.000001) {
                newrate = exchangeratefortransaction;
                ratio = oldrate - newrate;
                amount = (recinvamount - (recinvamount / newrate) * oldrate) / newrate;
                KwlReturnObject bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, newrate);
                    actualAmount = authHandler.round((Double) bAmtActual.getEntityList().get(0),companyid);                    
            } else {
                if (currid.equalsIgnoreCase(currencyid)) {
                            double paymentExternalCurrencyRate=externalCurrencyRate;
                            if(exchangeRate!=paymentExternalCurrencyRate && !revalFlag){
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate,paymentExternalCurrencyRate);
                            }else{
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                    }
                } else {
                            double paymentExternalCurrencyRate=externalCurrencyRate;
                            if(exchangeRate!=paymentExternalCurrencyRate && !revalFlag){
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate,paymentExternalCurrencyRate);
                            }else{
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                    }
                }
                if (!revalFlag) {
                    newrate = (Double) bAmt.getEntityList().get(0);
                }
                if (Math.abs(exchangeratefortransaction - oldrate) >= 0.000001) {
                    ratio = oldrate - newrate;
                }
                amount = recinvamount * ratio;
                KwlReturnObject bAmtActual = null;
                if (isopeningBalancePayment && isConversionRateFromCurrencyToBase) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
                } else {
                    bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
                }
                    actualAmount = authHandler.round((Double) bAmtActual.getEntityList().get(0),companyid);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getForexGainLossFordebitNote : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }
    /*
     * Revalaution Entery for Invoices
     */
    public double ReevalJournalEntryForInvoice(HttpServletRequest request, Invoice invoice, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
        double finalAmountReval = 0;
        try {
            String basecurrency = sessionHandlerImpl.getCurrencyID(request);
            double ratio = 0;
            double amountReval = 0;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkInvoiceAmount;
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put(Constants.RES_DATEFORMAT, authHandler.getDateOnlyFormat());
            Date creationDate = invoice.getCreationDate();
            boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
            tranDate = invoice.getCreationDate();
            if (!invoice.isNormalInvoice()) {
                exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
//                tranDate = invoice.getJournalEntry().getEntryDate();
            }
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", invoice.getID());
            invoiceId.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            //Checking the document entery in revalution history if any for current rate
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (revalueationHistory != null) {
                exchangeRateReval = revalueationHistory.getEvalrate();
            }

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (invoice.getCurrency() != null) {
                currid = invoice.getCurrency().getCurrencyID();
            }
            //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter(Constants.creationdate)));
            if (revalueationHistory == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

            double newrate = (Double) bAmt.getEntityList().get(0);
            ratio = oldrate - newrate;
            if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                exchangeratefortransaction = newrate;
            }
            double amountdueNew = amountdue / exchangeratefortransaction;
            amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
            amountReval = ratio * amountdueNew;
            finalAmountReval = finalAmountReval + amountReval;
        } catch (SessionExpiredException | ServiceException e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }

    /*
     * Revalaution Entery for Credit Note
     */
    public double ReevalJournalEntryForCreditNote(HttpServletRequest request, CreditNote creditNote, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {

        double finalAmountReval = 0;
        try {
            String basecurrency = sessionHandlerImpl.getCurrencyID(request);
            double ratio = 0;
            double amountReval = 0;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkInvoiceAmount;
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put(Constants.RES_DATEFORMAT, authHandler.getDateOnlyFormat(request));
            Date creationDate = creditNote.getCreationDate();
            boolean isopeningBalanceInvoice = creditNote.isIsOpeningBalenceCN();
            tranDate = creditNote.getCreationDate();
            if (!creditNote.isNormalCN()) {
                exchangeRate = creditNote.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = creditNote.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
            }
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", creditNote.getID());
            invoiceId.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            //Checking the document entery in revalution history if any for current rate
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (revalueationHistory != null) {
                exchangeRateReval = revalueationHistory.getEvalrate();
            }

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (creditNote.getCurrency() != null) {
                currid = creditNote.getCurrency().getCurrencyID();
            }
            //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter(Constants.creationdate)));
            if (revalueationHistory == null && isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

            double newrate = (Double) bAmt.getEntityList().get(0);
            ratio = oldrate - newrate;
            if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                exchangeratefortransaction = newrate;
            }
            double amountdueNew = amountdue / exchangeratefortransaction;
            amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
            amountReval = ratio * amountdueNew;
            finalAmountReval = finalAmountReval + amountReval;
        } catch (SessionExpiredException | ServiceException e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }

    public String PostJEFORReevaluation(HttpServletRequest request, double finalAmountReval, String companyid, CompanyAccountPreferences preferences, String basecurrency, String oldRevaluationJE, Map<String,Object> dataMap) {
        String jeid = "";
        try {
            String jeentryNumber = "";
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            boolean jeautogenflag = false;
            DateFormat df = authHandler.getDateOnlyFormat();
            /**
             * added Link Date to Realised JE. while link Otherwise CN/DN to
             * Reevaluated Invoice.
             */
            String creationDate = !StringUtil.isNullObject(request.getParameter("linkingdate")) ? request.getParameter("linkingdate") : request.getParameter("creationdate");
            Date entryDate = StringUtil.isNullOrEmpty(creationDate) ? new Date() : df.parse(creationDate);
            int counter=(Integer)dataMap.get("counter");
            synchronized (this) {
                HashMap<String, Object> JEFormatParams = new HashMap<>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                JEFormatParams.put(Constants.companyKey, companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                jeautogenflag = true;
                if (StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                    String nextAutoNoTemp=(String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                    datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeIntegerPart = String.valueOf(sequence);
                    jeSeqFormatId = format.getID();
                    counter++;
                    dataMap.put("counter", counter);
                } else if (!StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), oldRevaluationJE);
                    JournalEntry entry = (JournalEntry) result.getEntityList().get(0);
                    jeid = entry.getID();
                    jeentryNumber = entry.getEntryNumber();
                    jeSeqFormatId = entry.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(entry.getSeqnumber());
                    datePrefix=entry.getDatePreffixValue();
                    dateafterPrefix = entry.getDateAfterPreffixValue();
                    dateSuffix=entry.getDateSuffixValue();
                    result = accJournalEntryobj.deleteJEDtails(oldRevaluationJE, companyid);
                    result = accJournalEntryobj.deleteJE(oldRevaluationJE, companyid);
                }
            }
            boolean creditDebitFlag = true;
            if (finalAmountReval < 0) {
                finalAmountReval = -(finalAmountReval);
                creditDebitFlag = false;
            }

            Map<String, Object> jeDataMapReval = AccountingManager.getGlobalParams(request);
            jeDataMapReval.put("entrynumber", jeentryNumber);
            jeDataMapReval.put("autogenerated", jeautogenflag);
            jeDataMapReval.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMapReval.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMapReval.put(Constants.DATEPREFIX, datePrefix);
            jeDataMapReval.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
            jeDataMapReval.put(Constants.DATESUFFIX, dateSuffix);
            jeDataMapReval.put("entrydate", entryDate);
            jeDataMapReval.put(Constants.companyKey, companyid);
            //jeDataMapReval.put("memo", "Realised Gain/Loss");
            jeDataMapReval.put(Constants.currencyKey, basecurrency);
            jeDataMapReval.put("isReval", 2);
            jeDataMapReval.put("transactionModuleid", dataMap.containsKey("transactionModuleid") ? dataMap.get("transactionModuleid") : 0);
            jeDataMapReval.put("transactionId", dataMap.get("transactionId"));
            Set jedetailsReval = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMapReval);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMapReval.put("jeid", jeid);
            JSONObject jedjsonreval = new JSONObject();
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put(Constants.companyKey, companyid);
            jedjsonreval.put("amount", finalAmountReval);//rateDecreased?(-1*amountDiff):
            jedjsonreval.put("accountid", preferences.getForeignexchange().getID());
            jedjsonreval.put("debit", creditDebitFlag ? true : false);
            jedjsonreval.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);
            /*
             * Featching Custom field/Dimension Data from Company prefrences.
             */
            String customfield = "";
            String lineleveldimensions = "";
            KwlReturnObject result = accJournalEntryobj.getRevaluationJECustomData(companyid);
            RevaluationJECustomData revaluationJECustomData = (result != null && result.getEntityList().size() > 0  && result.getEntityList().get(0) != null) ? (RevaluationJECustomData) result.getEntityList().get(0) : null;
            if (revaluationJECustomData != null) {
                customfield = revaluationJECustomData.getCustomfield();
                lineleveldimensions = revaluationJECustomData.getLineleveldimensions();
            }

            /*
             * Make dimensions entry
             */
            setDimensionForRevalJEDetail(lineleveldimensions, jed);
            String unrealised_accid = "";
            if (preferences.getUnrealisedgainloss() != null) {
                unrealised_accid = preferences.getUnrealisedgainloss().getID();
            } else {
                throw new AccountingException(messageSource.getMessage("acc.field.NoUnrealisedGain/Lossaccountfound", null, RequestContextUtils.getLocale(request)));
            }
            jedjsonreval = new JSONObject();
            jedjsonreval.put(Constants.companyKey, companyid);
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put("amount", finalAmountReval);
            jedjsonreval.put("accountid", unrealised_accid);
            jedjsonreval.put("debit", creditDebitFlag ? false : true);
            jedjsonreval.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);
            /*
             * Make dimensions entry
             */
            setDimensionForRevalJEDetail(lineleveldimensions, jed);

            jeDataMapReval.put("jedetails", jedetailsReval);
            jeDataMapReval.put("externalCurrencyRate", 0.0);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMapReval);
            /*
             * Make custom field entry
             */
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", jeid);
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    Map<String, Object> customjeDataMap = new HashMap<>();
                    customjeDataMap.put("accjecustomdataref", jeid);
                    customjeDataMap.put("jeid", jeid);
                    customjeDataMap.put("istemplate", journalEntry.getIstemplate());
                    customjeDataMap.put("isReval", journalEntry.getIsReval());
                    accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jeid;
    }

    /*
     * Function to update invoice amount due and return KwlReturnObject
     */
    public KwlReturnObject updateInvoiceAmountDueAndReturnResult(Invoice invoice, Company company, double amountReceivedForInvoice, double amountReceivedInBaseCurrencyForInvoice) throws JSONException, ServiceException {
        KwlReturnObject result = null;
        if (invoice != null) {
            double invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
            invoiceAmountDue -= amountReceivedForInvoice;
            JSONObject invjson = new JSONObject();
            invjson.put("invoiceid", invoice.getID());
            invjson.put(Constants.companyKey, company.getCompanyID());
            invjson.put("openingBalanceAmountDue", invoiceAmountDue);
            invjson.put(Constants.openingBalanceBaseAmountDue, invoice.getOriginalOpeningBalanceBaseAmount() - amountReceivedInBaseCurrencyForInvoice);
            invjson.put(Constants.invoiceamountdue, invoice.getInvoiceamountdue() - amountReceivedForInvoice);
            invjson.put(Constants.invoiceamountdueinbase, invoice.getInvoiceAmountDueInBase() - amountReceivedInBaseCurrencyForInvoice);
            result = accInvoiceDAOObj.updateInvoice(invjson, null);
        }
        return result;
    }

    /*
     * Update invoice amount due in case of CN deletion.
     */
    @Override
    public void updateOpeningInvoiceAmountDue(String creditNoteId, String companyId) throws JSONException, ServiceException {

        KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), creditNoteId);
        if (!cnObj.getEntityList().isEmpty()) {
            CreditNote creditNote = (CreditNote) cnObj.getEntityList().get(0);
            if (creditNote.getCntype() != Constants.CreditNoteForOvercharge) {
                Set<CreditNoteDetail> creditNoteDetails = creditNote.getRows();
                if (creditNoteDetails != null && !creditNote.isDeleted()) { // if credit note already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete. 
                    Iterator itr = creditNoteDetails.iterator();
                    while (itr.hasNext()) {
                        CreditNoteDetail creditNoteDetail = (CreditNoteDetail) itr.next();
                        if (creditNoteDetail.getInvoice() != null && !creditNoteDetail.getInvoice().isNormalInvoice() && creditNoteDetail.getInvoice().isIsOpeningBalenceInvoice()) {
                            double amountPaid = creditNoteDetail.getDiscount().getAmountinInvCurrency();
                            Invoice invObj = creditNoteDetail.getInvoice();
                            HashMap<String, Object> requestParams = new HashMap();
                            requestParams.put(Constants.companyid, companyId);
                            requestParams.put(Constants.globalCurrencyKey, invObj.getCompany().getCurrency().getCurrencyID());
                            double externalCurrencyRate = 0d;
                            externalCurrencyRate = invObj.getExchangeRateForOpeningTransaction();
                            String fromcurrencyid = invObj.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (invObj.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                            }
                            double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                            double invoiceAmountDue = invObj.getOpeningBalanceAmountDue();
                            invoiceAmountDue += amountPaid;
                            JSONObject invjson = new JSONObject();
                            invjson.put("invoiceid", invObj.getID());
                            invjson.put(Constants.companyKey, companyId);
                            invjson.put("openingBalanceAmountDue", invoiceAmountDue);
                            if (invoiceAmountDue != 0) {
                                invjson.put("amountduedate", "");
                            }
                            invjson.put(Constants.openingBalanceBaseAmountDue, invObj.getOpeningBalanceBaseAmountDue() + totalBaseAmountDue);
                            accInvoiceDAOObj.updateInvoice(invjson, null);
                        } else if (creditNoteDetail.getInvoice() != null && creditNoteDetail.getInvoice().isNormalInvoice() && !creditNoteDetail.getInvoice().isIsOpeningBalenceInvoice()) { //For SR with CN case no need to update invoice amount due
                            double amountPaid = creditNoteDetail.getDiscount().getAmountinInvCurrency();
                            Invoice invoice = creditNoteDetail.getInvoice();
                            double invoiceAmountDue = invoice.getInvoiceamountdue();
                            invoiceAmountDue += amountPaid;
                            JSONObject invjson = new JSONObject();
                            invjson.put("invoiceid", invoice.getID());
                            invjson.put(Constants.companyKey, companyId);
                            invjson.put(Constants.invoiceamountdue, invoiceAmountDue);
                            HashMap<String, Object> requestParams = new HashMap();
                            requestParams.put(Constants.companyid, companyId);
                            requestParams.put(Constants.globalCurrencyKey, invoice.getCompany().getCurrency().getCurrencyID());
                            String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            JournalEntry je = invoice.getJournalEntry();
                            if (je != null) {
                                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceAmountDue, fromcurrencyid, invoice.getCreationDate(), je.getExternalCurrencyRate());
                                double invoiceamountdueinbase = (Double) baseAmount.getEntityList().get(0);
                                invjson.put(Constants.invoiceamountdueinbase, invoiceamountdueinbase);
                            }
                            if (invoiceAmountDue != 0) {
                                invjson.put("amountduedate", "");
                            }
                            accInvoiceDAOObj.updateInvoice(invjson, null);
                        }
                    }
                }
            } else if (creditNote.getCntype() == Constants.CreditNoteForOvercharge) {
                
                /**
                 * Update Amount Due of Invoice after delete CN.
                 */
                Map<String, Double> invoiceMap = new HashMap();
                Set<CreditNoteAgainstVendorGst> creditNoteDetails = creditNote.getRowsGst();
                if (creditNoteDetails != null && !creditNote.isDeleted()) { // if credit note already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete. 
                    for (CreditNoteAgainstVendorGst creditNoteDetail : creditNoteDetails) {
                        if (creditNoteDetail != null && creditNoteDetail.getInvoiceDetail() != null) {
                            double amountPaid = creditNoteDetail.getRate() * creditNoteDetail.getReturnQuantity();
                            // Discout calculations 1 - Percentage and 0 - flat Discount. 
                            if (creditNoteDetail.getDiscount() != 0 && creditNoteDetail.getDiscountispercent() == 1) {
                                amountPaid -= amountPaid * (creditNoteDetail.getDiscount() / 100);
                            } else if (creditNoteDetail.getDiscount() != 0 && creditNoteDetail.getDiscountispercent() == 0) {
                                amountPaid -= creditNoteDetail.getDiscount();
                            }
                            //Line Level Tax Calaculations
                            if (creditNoteDetail.getTax() != null) {
                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyId, creditNoteDetail.getCreditNote().getCreationDate(), creditNoteDetail.getTax().getID());
                                double taxPercent = (Double) perresult.getEntityList().get(0);
                                amountPaid += amountPaid * (taxPercent / 100);
                            }
                            Invoice invoice = creditNoteDetail.getInvoiceDetail().getInvoice();
                            if (invoiceMap.containsKey(invoice.getID())) {
                                amountPaid += invoiceMap.get(invoice.getID());
                            }
                            invoiceMap.put(invoice.getID(), amountPaid);
                        }
                    }
                    
                    for (Map.Entry<String, Double> entrySet : invoiceMap.entrySet()) {
                        double cnKnowkOffAmount = 0;
                        String invId = entrySet.getKey();
                        Double amountPaid = entrySet.getValue();
                        //Global Level Tax Calculations.
                        if (creditNote.getTax() != null) {
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyId, creditNote.getCreationDate(), creditNote.getTax().getID());
                            double taxPercent = (Double) perresult.getEntityList().get(0);
                            amountPaid += amountPaid * (taxPercent / 100);
                        }
                        JSONObject jObj = new JSONObject();
                        jObj.put("cnid", creditNote.getID());
                        jObj.put("linkedDocId", invId);
                        jObj.put(Constants.companyid, companyId);
                        List list = accCreditNoteDAOobj.getCreditNoteOverchargeAmountLinking(jObj);
                        if (!list.isEmpty()) {
                            cnKnowkOffAmount = Double.parseDouble(list.get(0).toString());
                        }
                        amountPaid -= cnKnowkOffAmount;
                        KwlReturnObject invresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invId);
                        Invoice invoice = (Invoice) invresult.getEntityList().get(0);
                        if (invoice != null && !invoice.isNormalInvoice() && invoice.isIsOpeningBalenceInvoice()) {
                            HashMap<String, Object> requestParams = new HashMap();
                            requestParams.put(Constants.companyid, companyId);
                            requestParams.put(Constants.globalCurrencyKey, invoice.getCompany().getCurrency().getCurrencyID());
                            double externalCurrencyRate = 0d;
                            externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                            String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (invoice.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, invoice.getCreationDate(), externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, invoice.getCreationDate(), externalCurrencyRate);
                            }
                            double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                            double invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
                            invoiceAmountDue += amountPaid;
                            JSONObject invjson = new JSONObject();
                            invjson.put("invoiceid", invoice.getID());
                            invjson.put(Constants.companyKey, companyId);
                            invjson.put("openingBalanceAmountDue", invoiceAmountDue);
                            if (invoiceAmountDue != 0) {
                                invjson.put("amountduedate", "");
                            }
                            invjson.put(Constants.openingBalanceBaseAmountDue, invoice.getOpeningBalanceBaseAmountDue() + totalBaseAmountDue);
                            accInvoiceDAOObj.updateInvoice(invjson, null);
                        } else if (invoice != null && invoice.isNormalInvoice() && !invoice.isIsOpeningBalenceInvoice()) { //For SR with CN case no need to update invoice amount due
                            double invoiceAmountDue = invoice.getInvoiceamountdue();
                            invoiceAmountDue += amountPaid;
                            JSONObject invjson = new JSONObject();
                            invjson.put("invoiceid", invoice.getID());
                            invjson.put(Constants.companyKey, companyId);
                            invjson.put(Constants.invoiceamountdue, invoiceAmountDue);
                            HashMap<String, Object> requestParams = new HashMap();
                            requestParams.put(Constants.companyid, companyId);
                            requestParams.put(Constants.globalCurrencyKey, invoice.getCompany().getCurrency().getCurrencyID());
                            String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            JournalEntry je = invoice.getJournalEntry();
                            if (je != null) {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceAmountDue, fromcurrencyid, invoice.getCreationDate(), je.getExternalCurrencyRate());
                                double invoiceamountdueinbase = (Double) bAmt.getEntityList().get(0);
                                invjson.put(Constants.invoiceamountdueinbase, invoiceamountdueinbase);
                            }
                            if (invoiceAmountDue != 0) {
                                invjson.put("amountduedate", "");
                            }
                            accInvoiceDAOObj.updateInvoice(invjson, null);
                        }
                    } 
                }
            }
        }
    }
       
    @Override
    public KwlReturnObject deleteCreditNotePartialy(HashMap<String, Object> dataMap) throws JSONException, ServiceException ,AccountingException{

        String cnid = (String) dataMap.get("cnid");
        String companyid = (String) dataMap.get(Constants.companyKey);

        CreditNote creditNote = (CreditNote) kwlCommonTablesDAOObj.getClassObject(CreditNote.class.getName(), cnid);

        if (creditNote.getApprovestatuslevel() == 11) {//For pending approval CN we did not need to update invoice amount. It is only needed for Cn whose approval level is 11.
            updateOpeningInvoiceAmountDue(cnid, companyid);
        }
        KwlReturnObject result = accCreditNoteDAOobj.deleteCreditNote(cnid, companyid);

        result = accCreditNoteDAOobj.getJEFromCN(cnid);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            String jeid = (String) itr.next();
            result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
            //Delete entry from optimized table
            accJournalEntryobj.deleteAccountJEs_optimized(jeid);
        }

        if (!creditNote.isOtherwise()) {//no entry in credit note discount table for otherwise CN
            result = accCreditNoteDAOobj.getCNDFromCN(cnid);
            list = result.getEntityList();
            itr = list.iterator();
            while (itr.hasNext()) {
                String discountid = (String) itr.next();
                result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
            }
        }

        /*
         * query = "update Discount di set di.deleted=true where di.ID in(select
         * cnd.discount.ID from CreditNoteDetail cnd where cnd.creditNote.ID in(
         * " + qMarks + ") and cnd.company.companyID=di.company.companyID) and
         * di.company.companyID=?"; HibernateUtil.executeUpdate(session, query,
         * params.toArray());
         */
        result = accCreditNoteDAOobj.getCNDFromCND(cnid);
        list = result.getEntityList();
        itr = list.iterator();
        while (itr.hasNext()) {
            String discountid = (String) itr.next();
            result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
        }

        return result;
    }

    @Override
    public HashMap getCreditNoteCommonCode(HttpServletRequest request, HttpServletResponse response) {
        HashMap map = new HashMap();
        JSONArray DataJArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = accCreditNoteController.getCreditNoteMap(request);
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter(Constants.globalCurrencyKey) != null) ? request.getParameter(Constants.globalCurrencyKey) : sessionHandlerImpl.getCurrencyID(request);
            String userId = sessionHandlerImpl.getUserid(request);
            if (consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                dir = request.getParameter("dir");
                sort = request.getParameter("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            KwlReturnObject result = null;
            String companyid = "";
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
                if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) ==
                     * Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has
                     * permission to view all customers documents,so at that
                     * time there is need to filter record according to
                     * user&salesperson.
                     */
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
                Map<String, Object> salesPersonParams = new HashMap<>();

                salesPersonParams.put("userid", sessionHandlerImpl.getUserid(request));
                salesPersonParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
                salesPersonParams.put("grID", "15");
                KwlReturnObject masterItemByUserList = accountingHandlerDAOobj.getMasterItemByUserID(salesPersonParams);
                List<MasterItem> masterItems = masterItemByUserList.getEntityList();
                String salesPersons = "";
                StringBuffer salesPersonids = new StringBuffer();
                for (Object obj : masterItems) {
                    if (obj != null) {
                        salesPersonids.append(obj.toString() + ",");
                    }
                }
                if (salesPersonids.length() > 0) {
                    salesPersons = salesPersonids.substring(0, (salesPersonids.length() - 1));
                    requestParams.put("salesPersonid", salesPersons);
                }

            }
            int cntype = StringUtil.isNullOrEmpty(request.getParameter(Constants.cntype)) ? 1 : Integer.parseInt(request.getParameter(Constants.cntype));
            boolean viewMode = request.getParameter("viewMode") != null ? Boolean.parseBoolean(request.getParameter("viewMode")) : false;
            if (viewMode) {
                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) request.getParameter("noteid"));
                CreditNote creditMemo = (CreditNote) resultObject.getEntityList().get(0);
                cntype = creditMemo.getCntype();
                requestParams.put(Constants.cntype, cntype);
                request.setAttribute(Constants.cntype, cntype);
            }
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userResult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute(Constants.companyKey, companyid);
                request.setAttribute(Constants.globalCurrencyKey, gcurrencyid);
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
                boolean isNoteForPayment = false;
                boolean isNewUI = false;
                boolean isVendor = false;
                if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteForPayment"))) {
                    isNoteForPayment = Boolean.parseBoolean(request.getParameter("isNoteForPayment"));
                    isVendor = Boolean.parseBoolean(request.getParameter("isVendor"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("isNewUI"))) {
                    isNewUI = Boolean.parseBoolean(request.getParameter("isNewUI"));
                }
                requestParams.put("isNoteForPayment", isNoteForPayment);
                requestParams.put("isNewUI", isNewUI);
                if(!StringUtil.isNullOrEmpty(request.getParameter("linknumber"))){
                    requestParams.put("linknumber", request.getParameter("linknumber"));
                }
                result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                if (cntype != 10 && cntype != 11) {
                    DataJArr = getCreditNoteMergedJson(request, result.getEntityList(), DataJArr);
                }

                if (cntype == 10 || (isNoteForPayment && !isVendor)) {// cntype=10 is just for help. value 10 have no any sense.
                    result = accCreditNoteDAOobj.getOpeningBalanceCNs(requestParams);
                    requestParams.put(Constants.cntype, 10);
                    getOpeningCreditNotesJson(requestParams, result.getEntityList(), DataJArr);
                } else if (cntype == 11 || (isNoteForPayment && isVendor)) {// cntype=11 is just for help. value 11 have no any sense.
                    result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                    requestParams.put(Constants.cntype, 11);
                    getOpeningCreditNotesJson(requestParams, result.getEntityList(), DataJArr);
                }
            }
            int cnt = consolidateFlag ? DataJArr.length() : result.getRecordTotalCount();
            map.put("data", DataJArr);
            map.put("count", cnt);
        }catch (SessionExpiredException ex) {
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }

    @Override
    public HashMap getCreditNoteCommonCode(JSONObject paramJobj) {
        HashMap map = new HashMap();
        JSONArray DataJArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = accCreditNoteController.getCreditNoteMapJson(paramJobj);
            boolean consolidateFlag = paramJobj.optString("consolidateFlag", null) != null ? Boolean.parseBoolean(paramJobj.getString("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && paramJobj.optString(Constants.companyids, null) != null) ? paramJobj.getString(Constants.companyids).split(",") : paramJobj.getString(Constants.companyKey).split(",");
            String gcurrencyid = (consolidateFlag && paramJobj.optString(Constants.globalCurrencyKey, null) != null) ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.optString(Constants.globalCurrencyKey);
            String userId = paramJobj.getString(Constants.useridKey);
            if (consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            KwlReturnObject result = null;
            String companyid = "";
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", paramJobj.getString(Constants.companyKey));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = paramJobj.getInt(Constants.PermCode_Customer);
                if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) ==
                     * Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has
                     * permission to view all customers documents,so at that
                     * time there is need to filter record according to
                     * user&salesperson.
                     */
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
                Map<String, Object> salesPersonParams = new HashMap<>();

                salesPersonParams.put("userid", paramJobj.getString(Constants.useridKey));
                salesPersonParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
                salesPersonParams.put("grID", "15");
                KwlReturnObject masterItemByUserList = accountingHandlerDAOobj.getMasterItemByUserID(salesPersonParams);
                List<MasterItem> masterItems = masterItemByUserList.getEntityList();
                String salesPersons = "";
                StringBuffer salesPersonids = new StringBuffer();
                for (Object obj : masterItems) {
                    if (obj != null) {
                        salesPersonids.append(obj.toString() + ",");
                    }
                }
                if (salesPersonids.length() > 0) {
                    salesPersons = salesPersonids.substring(0, (salesPersonids.length() - 1));
                    requestParams.put("salesPersonid", salesPersons);
                }
            }

            //Sorting call from PDF side
            String dir = "", sort = "";
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("dir", "")) && !StringUtil.isNullOrEmpty(paramJobj.optString("sort", ""))) {
                dir = paramJobj.optString("dir", "");
                sort = paramJobj.optString("sort", "");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }

            int cntype = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.cntype, null)) ? 1 : Integer.parseInt(paramJobj.getString(Constants.cntype));
            boolean viewMode = paramJobj.optString("viewMode", null) != null ? Boolean.parseBoolean(paramJobj.getString("viewMode")) : false;
            if (viewMode) {
                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) paramJobj.getString("noteid"));
                CreditNote creditMemo = (CreditNote) resultObject.getEntityList().get(0);
                cntype = creditMemo.getCntype();
                requestParams.put(Constants.cntype, cntype);
            }
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userResult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                paramJobj.put(Constants.companyKey, companyid);
                paramJobj.put(Constants.globalCurrencyKey, gcurrencyid);
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
                boolean isNoteForPayment = false;
                boolean isNewUI = false;
                boolean isVendor = false;
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNoteForPayment", null))) {
                    isNoteForPayment = Boolean.parseBoolean(paramJobj.getString("isNoteForPayment"));
                    isVendor = Boolean.parseBoolean(paramJobj.getString("isVendor"));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNewUI", null))) {
                    isNewUI = Boolean.parseBoolean(paramJobj.getString("isNewUI"));
                }
                requestParams.put("isNoteForPayment", isNoteForPayment);
                requestParams.put("isNewUI", isNewUI);
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("linknumber", null))) {
                    requestParams.put("linknumber", paramJobj.getString("linknumber"));
                }
                result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                if (cntype != 10 && cntype != 11) {
                    DataJArr = getCreditNoteMergedJson(paramJobj, result.getEntityList(), DataJArr);
                }

                if (cntype == 10 || (isNoteForPayment && !isVendor)) {// cntype=10 is just for help. value 10 have no any sense.
                    result = accCreditNoteDAOobj.getOpeningBalanceCNs(requestParams);
                    requestParams.put(Constants.cntype, 10);
                    requestParams.put(Constants.isdefaultHeaderMap,paramJobj.optBoolean(Constants.isdefaultHeaderMap, false));
                    getOpeningCreditNotesJson(requestParams, result.getEntityList(), DataJArr);
                } else if (cntype == 11 || (isNoteForPayment && isVendor)) {// cntype=11 is just for help. value 11 have no any sense.
                    result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                    requestParams.put(Constants.cntype, 11);
                    requestParams.put(Constants.isdefaultHeaderMap,paramJobj.optBoolean(Constants.isdefaultHeaderMap, false));
                    getOpeningCreditNotesJson(requestParams, result.getEntityList(), DataJArr);
                }
            }
            int cnt = consolidateFlag ? DataJArr.length() : result.getRecordTotalCount();
            map.put("data", DataJArr);
            map.put("count", cnt);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }

    public JSONArray getCreditNoteMergedJson(JSONObject paramJobj, List<Object[]> list, JSONArray jArr) throws ServiceException {
        try {
            int cntype = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.cntype,null)) ? 1 : Integer.parseInt(paramJobj.getString(Constants.cntype));
            int reportID = StringUtil.isNullOrEmpty(paramJobj.optString("reportID",null)) ? 0 : Integer.parseInt(paramJobj.getString("reportID"));
            int transactiontype = StringUtil.isNullOrEmpty(paramJobj.optString("transactiontype",null)) ? 1 : Integer.parseInt(paramJobj.getString("transactiontype"));
            boolean isApprovalPendingReport = StringUtil.isNullOrEmpty(paramJobj.optString("pendingapproval",null)) ? false : Boolean.parseBoolean(paramJobj.getString("pendingapproval"));
            String companyid = paramJobj.getString(Constants.companyKey);
            String userName = paramJobj.optString(Constants.userfullname, "");
            String userid = paramJobj.optString(Constants.useridKey, "");
            HashMap<String, Object> requestParams = accCreditNoteController.getCreditNoteMapJson(paramJobj);
            double tax = 0;

            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            String transactionCurrencyId = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df"); //ERP-10970

            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(paramJobj.getString(Constants.companyKey), Constants.Acc_Credit_Note_ModuleId));
            FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            for (Object[] row :list) {
                boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                String personid = "";
                String personname = "";
                String aliasname = "";
                String paymentterm = "";
                String personcode = "";
                boolean isCustomer = false;
                DateFormat userdf = authHandler.getUserDateFormatterJson(paramJobj);

                if (cntype == Constants.CreditNoteAgainstVendor && !withoutinventory && transactiontype != 8) {//CN against vendor                   
                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Vendor.class.getName(), (String) row[2]);
                    Vendor vendor = (Vendor) resultObject.getEntityList().get(0);
                    personid = vendor.getID();
                    personname = vendor.getName();
                    personcode = vendor.getAcccode() == null ? "" : vendor.getAcccode();
                    paymentterm = vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getTermname();
                } else {
                    if (cntype == 8) {
                        if (withoutinventory) {
                            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Vendor.class.getName(), (String) row[2]);
                            Vendor vendor = (Vendor) resultObject.getEntityList().get(0);
                            personid = vendor.getID();
                            personname = vendor.getName();
                            aliasname = vendor.getAliasname();
                            personcode = vendor.getAcccode() == null ? "" : vendor.getAcccode();
                            paymentterm = vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getTermname();
                        } else {
                            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                            Customer customer = (Customer) resultObject.getEntityList().get(0);
                            personid = customer.getID();
                            personname = customer.getName();
                            aliasname = customer.getAliasname();
                            personcode = customer.getAcccode() == null ? "" : customer.getAcccode();
                            paymentterm = customer.getCreditTerm() == null ? "" : customer.getCreditTerm().getTermname();
                            isCustomer = true;

                        }
                        withoutinventory = false;
                    } 
                    if (cntype == Constants.CreditNoteForUndercharge) {
                        KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Vendor.class.getName(), (String) row[2]);
                        Vendor vendor = (Vendor) resultObject.getEntityList().get(0);
                        personid = vendor.getID();
                        personname = vendor.getName();
                        aliasname = vendor.getAliasname();
                        personcode = vendor.getAcccode() == null ? "" : vendor.getAcccode();
                        paymentterm = vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getTermname();
                    } else {
                        KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                        Customer customer = (Customer) resultObject.getEntityList().get(0);
                        personid = customer.getID();
                        personname = customer.getName();
                        aliasname = customer.getAliasname();
                        personcode = customer.getAcccode() == null ? "" : customer.getAcccode();
                        paymentterm = customer.getCreditTerm() == null ? "" : customer.getCreditTerm().getTermname();
                        isCustomer = true;

                    }
                }
                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                JSONObject obj = new JSONObject();
                if (withoutinventory) {
                    resultObject = accountingHandlerDAOobj.getObject(BillingCreditNote.class.getName(), (String) row[1]);
                    BillingCreditNote creditMemo = (BillingCreditNote) resultObject.getEntityList().get(0);
                    JournalEntry je = creditMemo.getJournalEntry();
                    //*** Attachments Documents SJ[ERP-16331] 
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", creditMemo.getID());
                    hashMap.put(Constants.companyKey, creditMemo.getCompany().getCompanyID());
                    KwlReturnObject object = accVendorDAOObj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    obj.put("attachment", attachemntcount);
                    obj.put(Constants.billid, creditMemo.getID());
                    //*** Attachments Documents SJ[ERP-16331] 
                    obj.put("withoutinventory", withoutinventory);
                    obj.put("noteid", creditMemo.getID());
                    obj.put(Constants.companyKey, creditMemo.getCompany().getCompanyID());
                    obj.put("companyname", creditMemo.getCompany().getCompanyName());

                    obj.put("journalentryid", je.getID());
                    obj.put("currencysymbol", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));
                    obj.put(Constants.currencyKey, (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID()));
                    obj.put("entryno", je.getEntryNumber());

                    if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {

                        if (cntype == Constants.CreditNoteAgainstVendor) { // CN AGAINST VENDOR
                            obj.put("vendor", personid);
                            obj.put("vendorValue", personname);
                        } else {
                            obj.put("customer", personid);
                            obj.put("customerValue", personname);
                        }
                        obj.put(Constants.creationdate, df.format(je.getEntryDate()));
                        obj.put("number", creditMemo.getCreditNoteNumber());
                        obj.put("costcenter", je.getCostcenter() == null ? "" : je.getCostcenter().getID());
                        obj.put("costcenterValue", je.getCostcenter() == null ? "" : je.getCostcenter().getName());
                        obj.put(Constants.currencyKey+"Value", (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getName()));

                    } else {
                        obj.put("noteno", creditMemo.getCreditNoteNumber());
                        obj.put("date", df.format(je.getEntryDate()));
                        obj.put("personid", personid);
                        obj.put("personname", personname);
                        obj.put("costcenterid", je.getCostcenter() == null ? "" : je.getCostcenter().getID());
                        obj.put("costcenterName", je.getCostcenter() == null ? "" : je.getCostcenter().getName());
                    }

                    obj.put("aliasname", aliasname);
                    /*
                     * This fields are inserted for Credit Note Register Export
                     * Functionality - Mayur B.
                     */
                    obj.put("personcode", personcode);
                    obj.put("paymentterm", paymentterm);
                    obj.put("iscustomer", isCustomer);
                    obj.put("dateinuserformat", userdf.format(je.getEntryDate()));
                    obj.put("amount", details.getAmount());
                    obj.put("amountdue", 0);
                    obj.put("amountduenonnegative", 0);

                    obj.put("memo", creditMemo.getMemo());
                    obj.put("otherwise", false);//creditMemo.isOtherwise());
                    obj.put("openflag", false);//creditMemo.isOpenflag());
                    obj.put(Constants.cntype, 1);//creditMemo.getCntype());
                    obj.put("deleted", creditMemo.isDeleted());

                    obj.put("partlyJeEntryWithCnDn", je.getPartlyJeEntryWithCnDn());
                    KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), paramJobj.getString(Constants.companyKey));
                    List<JournalEntryDetail> journallist= result.getEntityList();
                    for (JournalEntryDetail jed:journallist) {
                        Account account = null;
                        account = jed.getAccount();
                        //To do - need to test.
                        if (account.getMastertypevalue() == Group.ACCOUNTTYPE_GST) {//GST transaction.
                            if (jed.isDebit()) {
                                tax = jed.getAmount();
                            }
                        }
                    }
                    result = accCreditNoteDAOobj.getTotalTax_TotalDiscount_Billing(creditMemo.getID());
                    double totTax = 0, totDiscount = 0;
                    if (result != null && result.getEntityList() != null) {
                        Iterator resItr = result.getEntityList().iterator();
                        Object[] sumRow = (Object[]) resItr.next();
                        if (sumRow[0] != null) {
                            totTax = Double.parseDouble(sumRow[0].toString());
                        }
                        if (sumRow[1] != null) {
                            totDiscount = Double.parseDouble(sumRow[1].toString());
                        }
                    }

                    obj.put("noteSubTotal", details.getAmount() + totDiscount - totTax);
                    obj.put("notetax", tax);
                    obj.put("totalTax", totTax);
                    obj.put("totalDiscount", totDiscount);

                } else {
                    resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                    CreditNote creditMemo = (CreditNote) resultObject.getEntityList().get(0);
                    JournalEntry je = creditMemo.getJournalEntry();

                    Date creditNoteDate = null;
                    double externalCurrencyRate = 0d;
                    String journalentryid = "";
                    String jeentryno = "";
                    if (creditMemo.isNormalCN()) {
                        je = creditMemo.getJournalEntry();
                        creditNoteDate = creditMemo.getCreationDate();
                        externalCurrencyRate = je.getExternalCurrencyRate();
                        journalentryid = je.getID();
                        jeentryno = je.getEntryNumber();
                        List resultJe = accCreditNoteDAOobj.getForeignGainLossJE(creditMemo.getID(), paramJobj.getString(Constants.companyKey));
                        if (resultJe.size() > 0 && resultJe.get(0) != null) {
                            Iterator itr1 = resultJe.iterator();
                            while (itr1.hasNext()) {
                                Object object = itr1.next();
                                String jeid = object != null ? object.toString() : "";
                                if (!StringUtil.isNullOrEmpty(jeid)) {
                                    resultObject = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
                                    JournalEntry foreignGainlossJe = (JournalEntry) resultObject.getEntityList().get(0);
                                    journalentryid += "," + foreignGainlossJe.getID();
                                    jeentryno += "," + foreignGainlossJe.getEntryNumber();
                                }

                            }
                        }
                    }

                    transactionCurrencyId = (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID());
                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("currencysymboltransaction", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));
                    String currencyFilterForTrans = "";
                    boolean isNoteForPayment = false;
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNoteForPayment",null))) {
                        isNoteForPayment = Boolean.parseBoolean(paramJobj.getString("isNoteForPayment"));
                    }

                    if (requestParams.containsKey("currencyfilterfortrans") && isNoteForPayment) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject curresultpayment = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyPayment = (KWLCurrency) curresultpayment.getEntityList().get(0);
                        obj.put("currencysymbol", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                        obj.put(Constants.currencyKey, (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getCurrencyID()));
                        obj.put(Constants.currencyKey+"Value",(currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getName()));
                    } else {
                        obj.put("currencysymbol", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));
                        obj.put(Constants.currencyKey, (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID()));
                        obj.put(Constants.currencyKey+"Value",(creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getName()));
                    }
                    /*
                   
                     * credit note against vendor for malaysian country (In Export to xls,pdf functionality (creditMemo.getCntype()==5) is used to get amountdue,amountDueOriginal value). 
                    
                     */
                    double amountdue = (creditMemo.isOtherwise() || creditMemo.getCntype()==5 || creditMemo.getCntype() == Constants.CreditNoteForOvercharge) ? creditMemo.getCnamountdue() : 0;
                    double amountDueOriginal = (creditMemo.isOtherwise() || creditMemo.getCntype()==5 || creditMemo.getCntype() == Constants.CreditNoteForOvercharge) ? creditMemo.getCnamountdue() : 0;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                        amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                    }

                    KwlReturnObject baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                    obj.put("withoutinventory", withoutinventory);
                    obj.put("isReturnNote", creditMemo.getSalesReturn() == null ? false : true);
                    //*** Attachments Documents SJ[ERP-16331] 
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", creditMemo.getID());
                    hashMap.put(Constants.companyKey, creditMemo.getCompany().getCompanyID());
                    KwlReturnObject object = accVendorDAOObj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    obj.put("attachment", attachemntcount);
                    obj.put(Constants.billid, creditMemo.getID());
                    //*** Attachments Documents SJ[ERP-16331] 
                    obj.put("noteid", creditMemo.getID());
                    obj.put("isOldRecord", creditMemo.isOldRecord());
                    obj.put(Constants.companyKey, creditMemo.getCompany().getCompanyID());
                    obj.put("companyname", creditMemo.getCompany().getCompanyName());

                    obj.put("journalentryid", journalentryid);
                    obj.put("entryno", jeentryno);

                    if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                        obj.put("number", creditMemo.getCreditNoteNumber());
                        obj.put(Constants.creationdate, df.format(creditMemo.getCreationDate()));
                        obj.put("customer", personid);
                        obj.put("customerValue", personname);
                        obj.put("costcenter", creditMemo.getCostcenter() == null ? "" : creditMemo.getCostcenter().getID());
                        obj.put("costcenterValue", creditMemo.getCostcenter() == null ? "" : creditMemo.getCostcenter().getName());
                        obj.put("salesperson", creditMemo.getSalesPerson() == null ? "" : creditMemo.getSalesPerson().getID());
                        obj.put("salespersonValue", (creditMemo.getSalesPerson() == null) ? "" : creditMemo.getSalesPerson().getValue());
                    } else {
                        obj.put("noteno", creditMemo.getCreditNoteNumber());
                        obj.put("date", df.format(creditMemo.getCreationDate()));
                        obj.put("personid", personid);
                        obj.put("personname", personname);
                        obj.put("costcenterid", creditMemo.getCostcenter() == null ? "" : creditMemo.getCostcenter().getID());
                        obj.put("costcenterName", creditMemo.getCostcenter() == null ? "" : creditMemo.getCostcenter().getName());
                        obj.put("salesPersonID", creditMemo.getSalesPerson() == null ? "" : creditMemo.getSalesPerson().getID());
                        obj.put("salesPerson", (creditMemo.getSalesPerson() == null) ? "" : creditMemo.getSalesPerson().getValue());
                    }
                    obj.put("aliasname", aliasname);
                    /*
                     * This fields are inserted for Credit Note Register Export
                     * Functionality - Mayur B.
                     */
                    obj.put("personcode", personcode);
                    obj.put("paymentterm", paymentterm);
                    obj.put("iscustomer", isCustomer);
                    obj.put("dateinuserformat", userdf.format(creditMemo.getCreationDate()));
                    double paidAmount = creditMemo.isOtherwise() ? creditMemo.getCnamount() : details.getAmount();
                    obj.put("amount", paidAmount);
                    KwlReturnObject paidAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, paidAmount, transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                    obj.put("paidamountinbase", authHandler.round((Double) paidAmt.getEntityList().get(0), companyid));
                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountdueinbase", authHandler.round((Double) baseAmt.getEntityList().get(0), companyid));
                    obj.put("amountinbase", authHandler.round(creditMemo.getCnamountinbase(), companyid));
                    obj.put("memo", creditMemo.getMemo());
                    obj.put("createdby", creditMemo.getCreatedby() == null ? "" : StringUtil.getFullName(creditMemo.getCreatedby()));
                    obj.put("includingGST", creditMemo.isIncludingGST());
                    obj.put("deleted", creditMemo.isDeleted());
                    obj.put("otherwise", creditMemo.isOtherwise());
                    obj.put("isprinted", creditMemo.isPrinted());
                    obj.put("openflag", creditMemo.isOpenflag());
                    obj.put("isCreatedFromReturnForm", (creditMemo.getSalesReturn() != null) ? true : false);
                    obj.put(Constants.cntype, creditMemo.getCntype());
                    obj.put("partlyJeEntryWithCnDn", je.getPartlyJeEntryWithCnDn());
                    obj.put("accountid", creditMemo.getAccount() == null ? "" : creditMemo.getAccount().getID());
                    obj.put("accountnames", creditMemo.getAccount() == null ? "" : creditMemo.getAccount().getName());
                    obj.put("externalcurrencyrate", je.getExternalCurrencyRate());
                    if (isApprovalPendingReport) {//these data only need for pending Approval report of CN
                        obj = getCreditNoteApprovalPendingJsonData(obj, creditMemo.getID(), companyid, userid, userName);
                    }

                    Set<CreditNoteTaxEntry> cnTaxEntryDetails = creditMemo.getCnTaxEntryDetails();
                    String reason = "";
                    double totalCnTax = 0;
                    if (cnTaxEntryDetails != null && !cnTaxEntryDetails.isEmpty()) {

                        for (CreditNoteTaxEntry noteTaxEntry : cnTaxEntryDetails) {
                            reason += ((noteTaxEntry.getReason() != null) ? noteTaxEntry.getReason().getValue() : "") + ",";
                            if (noteTaxEntry.isDebitForMultiCNDN()) {
                                totalCnTax += noteTaxEntry.getTaxamount();
                            } else {
                                totalCnTax -= noteTaxEntry.getTaxamount();
                            }
                            if (reportID == 822) {
                                if (obj.has(noteTaxEntry.getAccount().getID() + "_Amount")) {
                                    double temAccAmount = obj.optDouble((noteTaxEntry.getAccount().getID() + "_Amount"), 0);
                                    obj.put(noteTaxEntry.getAccount().getID() + "_Amount", temAccAmount + noteTaxEntry.getAmount());
                                } else {
                                    obj.put(noteTaxEntry.getAccount().getID() + "_Amount", noteTaxEntry.getAmount());
                                }
                            }
                        }
                    }
                    /*
                     * when credit note is created against vendor for malaysian country (Tax Amount) is not fetched for csv,xls,pdf file display(ERP-34829) .
                     * For Export to csv,xls,pdf Fuctionality if condition will true for malaysian country. 
                     * if block is written to get tax for credit note against vendor only for malaysian country ERP-34829
                     * tax amount is putted in JSONObject obj with key (taxamountinbase) and calculated total tax( in pdf Tax Amount in MYR) in accOtherReportsController. 
                     
                     */
                    double taxAmount=0;
                    if (creditMemo.getCntype() == 5 || creditMemo.getCntype() == Constants.CreditNoteForOvercharge) {
                        /*
                         *
                         * below code is written to get line lavel tax amount
                         * (totalCnTax)
                         *
                         */
                        Set<CreditNoteAgainstVendorGst> dnTaxGstDetails = creditMemo.getRowsGst();
                        totalCnTax = 0;
                        if (dnTaxGstDetails != null && !dnTaxGstDetails.isEmpty()) {

                            for (CreditNoteAgainstVendorGst noteTaxGstEntry : dnTaxGstDetails) {
                                reason += ((noteTaxGstEntry.getReason() != null) ? noteTaxGstEntry.getReason().getValue() : "") + ",";
                                totalCnTax += noteTaxGstEntry.getRowTaxAmount();
                            }
                        }

                        /*
                         *
                         * below code is written to get global tax amount
                         * (taxAmount)
                         *
                         */

                        Tax taxObj = creditMemo.getTax();
                        KwlReturnObject result1 = accJournalEntryobj.getJournalEntryDetail(creditMemo.getJournalEntry().getID(), creditMemo.getJournalEntry().getCompany().getCompanyID());
                        Iterator iterator = result1.getEntityList().iterator();

                        while (iterator.hasNext()) {
                            JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                            Account account = jed.getAccount();

                            if (taxObj != null && jed.isDebit() && account.getID().equals(taxObj.getAccount().getID())) {
                                taxAmount += jed.getAmount();

                            }

                        }

                    }

                    /*
                     * if credit note is created against vendor for malaysian country then outer if condition will be true
                     * if line level tax is provided(totalCnTax != 0) then inner if condition will be true                                              
                     */
                    if (creditMemo.getCntype() == 5) {
                        if (totalCnTax != 0) {
                            obj.put("taxamount", authHandler.round(totalCnTax, companyid));
                        } else {
                            obj.put("taxamount", authHandler.round(taxAmount, companyid));
                        }
                    } else {
                        obj.put("taxamount", authHandler.round(totalCnTax, companyid));
                    }

                    KwlReturnObject res = accCreditNoteDAOobj.checkEntryForTransactionInLinkingTableForForwardReference("CreditNote", creditMemo.getID());
                    List reslist = res.getEntityList();

                    if ((reslist != null && !reslist.isEmpty())) {
                        obj.put(Constants.IS_LINKED_TRANSACTION, true);
                    } else {
                        obj.put(Constants.IS_LINKED_TRANSACTION, false);
                    }

                    KwlReturnObject taxAmt = null;

                    /*
                     * if credit note is created against vendor for malaysian country then outer if condition will be true
                     * if line level tax is provided(totalCnTax != 0) then inner if condition will be true                                              
                     */
                    if (creditMemo.getCntype() == 5 || creditMemo.getCntype() == Constants.CreditNoteForOvercharge) {
                        if (totalCnTax != 0) {
                            taxAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, authHandler.round(totalCnTax, companyid), transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                        } else {
                            taxAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, authHandler.round(taxAmount, companyid), transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                        }
                    } else {
                        taxAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, authHandler.round(totalCnTax, companyid), transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                    }

                    if (taxAmt != null) {
                        obj.put("taxamountinbase", authHandler.round((Double) taxAmt.getEntityList().get(0), companyid));
                    }
                    double cnTotalAmount = creditMemo.isOtherwise() ? creditMemo.getCnamount() : details.getAmount();

                    /*
                     * if credit note is created against vendor for malaysian country then outer if condition will be true
                     * if line level tax is provided(totalCnTax != 0) then inner if condition will be true                                              
                     */
                    if (creditMemo.getCntype() == 5 || creditMemo.getCntype() == Constants.CreditNoteForOvercharge) {
                        if (totalCnTax != 0) {
                            obj.put("amountbeforegst", authHandler.round(cnTotalAmount - totalCnTax, companyid));
                        } else {
                            obj.put("amountbeforegst", authHandler.round(cnTotalAmount - taxAmount, companyid));
                        }
                    } else {
                        obj.put("amountbeforegst", authHandler.round(cnTotalAmount - totalCnTax, companyid));
                    }
                    if (!StringUtil.isNullOrEmpty(reason)) {
                        obj.put("reason", reason.substring(0, reason.length() - 1));
                    } else {
                        obj.put("reason", reason);
                    }
                    obj.put("currencyname", (creditMemo.getCurrency() == null ? currency.getName() : creditMemo.getCurrency().getName()));
                    obj.put("currencycode", (creditMemo.getCurrency() == null ? currency.getCurrencyCode() : creditMemo.getCurrency().getCurrencyCode()));
                    obj.put(Constants.SEQUENCEFORMATID, creditMemo.getSeqformat() != null ? creditMemo.getSeqformat().getID() : "");
                    BillingShippingAddresses addresses = creditMemo.getBillingShippingAddresses();
                    if (cntype == 4 && transactiontype != 8) {//CN against vendor  
                        AccountingAddressManager.getTransactionAddressJSON(obj, addresses, true);
                    } else {
                        AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    }
                    KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), paramJobj.getString(Constants.companyKey));
                    Iterator iterator = result.getEntityList().iterator();
                    while (iterator.hasNext()) {
                        JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                        Account account = null;
                        account = jed.getAccount();
                        //To do - need to test.
                        if (account.getMastertypevalue() == Group.ACCOUNTTYPE_GST) {//GST transaction.
                            if (jed.isDebit()) {
                                tax = jed.getAmount();
                            }
                        }
                    }
                    result = accCreditNoteDAOobj.getTotalTax_TotalDiscount(creditMemo.getID());
                    double totTax = 0, totDiscount = 0;
                    if (result != null && result.getEntityList() != null) {
                        Iterator resItr = result.getEntityList().iterator();
                        Object[] sumRow = (Object[]) resItr.next();
                        if (sumRow[0] != null) {
                            totTax = Double.parseDouble(sumRow[0].toString());
                        }
                        if (sumRow[1] != null) {
                            totDiscount = Double.parseDouble(sumRow[1].toString());
                        }
                    }

                    obj.put("noteSubTotal", details.getAmount() + totDiscount - totTax);
                    obj.put("notetax", tax);
                    obj.put("totalTax", totTax);
                    obj.put("totalDiscount", totDiscount);

                    int isReval = 0;
                    KwlReturnObject brdAmt = accInvoiceDAOObj.getRevalFlag(creditMemo.getID());
                    List reval = brdAmt.getEntityList();
                    if (!reval.isEmpty() && (Long) reval.get(0) > 0) {
                        isReval = 1;
                    }
                    obj.put("isreval", isReval);
                    Set<CreditNoteDetail> cndetails = creditMemo.getRows();
                    boolean isLinked = false;
                    String linkedDate = null;
                    for (CreditNoteDetail noteDetail : cndetails) {
                        if (noteDetail.getInvoice() != null) {
                            isLinked = true;
                            linkedDate = df.format(noteDetail.getInvoiceLinkDate());
                            break;
                        } else if (!StringUtil.isNullOrEmpty(noteDetail.getDebitNoteId())) {
                            isLinked = true;
                            linkedDate = df.format(noteDetail.getInvoiceLinkDate());
                            break;
                        }
                    }
                    obj.put("isLinked", isLinked);
                    if (linkedDate != null) {
                        obj.put("linkingdate", linkedDate);
                    }
                    /*
                     * Credit note will be allowed to copy in following case- 1.
                     * CN against sales invoice - NOT allowed 2. CN otherwise -
                     * allowed if no invoice is linked to CN at time of creation
                     * or later on 3. CN against Vendor - Allowed
                     */
                    if (creditMemo.getCntype() == Constants.CreditNoteAgainstSalesInvoice) {
                        obj.put("isCopyAllowed", false);
                    } else if (creditMemo.getCntype() == Constants.CreditNoteOtherwise) {
                        boolean copyFlag = true;
                        for (CreditNoteDetail cndetail : cndetails) {
                            if (cndetail.getInvoice() != null) {
                                copyFlag = false;
                                break;
                            }
                        }
                        obj.put("isCopyAllowed", copyFlag);
                    } else if (creditMemo.getCntype() == Constants.CreditNoteAgainstVendor) {
                        obj.put("isCopyAllowed", true);
                    } else {
                        obj.put("isCopyAllowed", false);
                    }
                    HashMap<String, Object> reqParams1 = new HashMap<>();
                    reqParams1.put("cnid", creditMemo.getID());
                    reqParams1.put(Constants.companyKey, creditMemo.getCompany().getCompanyID());
                    KwlReturnObject linkResult = accCreditNoteDAOobj.getLinkDetailPaymentToCreditNote(reqParams1);
                    if (!linkResult.getEntityList().isEmpty()) {
                        obj.put("isNoteLinkedToAdvancePayment", true);
                    }

                    boolean getlineItemDetailsflag = (paramJobj.optString("getlineItemDetailsflag", null) != null) ? Boolean.FALSE.parseBoolean((String) paramJobj.get("getlineItemDetailsflag")) : false;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                    Detailfilter_names.add(Constants.companyKey);
                    Detailfilter_params.add(creditMemo.getCompany().getCompanyID());
                    Detailfilter_names.add("journalentryId");
                    Detailfilter_params.add(creditMemo.getJournalEntry().getID());
                    Detailfilter_names.add("moduleId");
                    Detailfilter_params.add(Constants.Acc_Credit_Note_ModuleId + "");
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomData(invDetailRequestParams);
                    if (idcustresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        boolean isExport = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isExport, null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isExport)) : false;
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.isdefaultHeaderMap, paramJobj.optBoolean(Constants.isdefaultHeaderMap, false));
                        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.browsertz,null))) {
                            params.put(Constants.browsertz, paramJobj.getString(Constants.browsertz));
                        }

                        if (!getlineItemDetailsflag) {
                            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                        } else {
                            fieldDataManagercntrl.getLineLevelCustomDataWithKey(variableMap, customFieldMap, customDateFieldMap, obj, params);
                        }
                    }

                }
                if (requestParams.containsKey("isReceipt") && obj.optDouble("amountdue", 0.0) != 0) {
                    jArr.put(obj);
                } else if (!requestParams.containsKey("isReceipt")) {
                    jArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getCreditNoteJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    @Override
    public JSONArray getCreditNoteMergedJson(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException {
        try {
            int cntype = StringUtil.isNullOrEmpty(request.getParameter(Constants.cntype)) ? 1 : Integer.parseInt(request.getParameter(Constants.cntype));
            int reportID = StringUtil.isNullOrEmpty(request.getParameter("reportID")) ? 0 : Integer.parseInt(request.getParameter("reportID"));
            int transactiontype = StringUtil.isNullOrEmpty(request.getParameter("transactiontype")) ? 1 : Integer.parseInt(request.getParameter("transactiontype"));
            boolean isApprovalPendingReport =StringUtil.isNullOrEmpty(request.getParameter("pendingapproval"))?false:Boolean.parseBoolean(request.getParameter("pendingapproval"));
            String companyid= sessionHandlerImpl.getCompanyid(request);
            String userName = sessionHandlerImpl.getUserFullName(request);
            String userid = sessionHandlerImpl.getUserid(request);
            boolean checkTax=false;
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", companyid);
            Object exPrefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"columnPref"}, paramMap);
            JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
            boolean isPostingDateCheck = false;
            if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                isPostingDateCheck = true;
            }
            if (request.getAttribute(Constants.cntype) != null) {
                cntype = (Integer) request.getAttribute(Constants.cntype);
            }
            HashMap<String, Object> requestParams = accCreditNoteController.getCreditNoteMap(request);
            double tax = 0;

            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            String transactionCurrencyId = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df"); //ERP-10970
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            /**
                 * ERP-32479 - Issue was linking date showing different, for different time zone.
             */
//            if (requestParams.containsKey("browsertz") && requestParams.get("browsertz") != null && !StringUtil.isNullOrEmpty(requestParams.get("browsertz").toString())) {
//                sdf.setTimeZone(TimeZone.getTimeZone("GMT" + requestParams.get("browsertz")));
//            }

            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Credit_Note_ModuleId));
            FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            HashSet<String> cnSet = new HashSet<String>();
            Iterator itr = list.iterator();
            HashMap<String, Object> badDebtMap = new HashMap<>();
            badDebtMap.put("badDebtType", 0);
            KwlReturnObject badDebtResult=null;

            Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
            Integer countryId = company.getCountry() != null ? Integer.parseInt(company.getCountry().getID()) : null;
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                boolean isLinkedInvoiceClaimed=false;
                boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                String personid = "";
                String personname = "";
                String aliasname = "";
                String paymentterm = "";
                String personcode = "";
                String personaccountId = "";
                boolean isCustomer = false;
                DateFormat userdf = authHandler.getUserDateFormatter(request);
                boolean hasAccess = true;
                if (cntype == Constants.CreditNoteAgainstVendor && !withoutinventory && transactiontype != 8) {//CN against vendor                   
                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Vendor.class.getName(), (String) row[2]);
                    Vendor vendor = (Vendor) resultObject.getEntityList().get(0);
                    personid = vendor.getID();
                    personname = vendor.getName();
                    personcode = vendor.getAcccode() == null ? "" : vendor.getAcccode();
                    paymentterm = vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getTermname();
                    personaccountId = vendor.getAccount() == null ? "" : vendor.getAccount().getID();
                    hasAccess = vendor.isActivate();
                } else {
                    if (cntype == 8) {
                        if (withoutinventory) {
                            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Vendor.class.getName(), (String) row[2]);
                            Vendor vendor = (Vendor) resultObject.getEntityList().get(0);
                            personid = vendor.getID();
                            personname = vendor.getName();
                            aliasname = vendor.getAliasname();
                            personcode = vendor.getAcccode() == null ? "" : vendor.getAcccode();
                            paymentterm = vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getTermname();
                            personaccountId = vendor.getAccount() == null ? "" : vendor.getAccount().getID();
                            hasAccess = vendor.isActivate();
                        } else {
                            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                            Customer customer = (Customer) resultObject.getEntityList().get(0);
                            personid = customer.getID();
                            personname = customer.getName();
                            aliasname = customer.getAliasname();
                            personcode = customer.getAcccode() == null ? "" : customer.getAcccode();
                            paymentterm = customer.getCreditTerm() == null ? "" : customer.getCreditTerm().getTermname();
                            personaccountId = customer.getAccount() == null ? "" : customer.getAccount().getID();
                            isCustomer = true;
                            hasAccess = customer.isActivate();
                        }
                        withoutinventory = false;
                    }
                    if (cntype == 5) {
                        KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Vendor.class.getName(), (String) row[2]);
                        Vendor vendor = (Vendor) resultObject.getEntityList().get(0);
                        personid = vendor.getID();
                        personname = vendor.getName();
                        aliasname = vendor.getAliasname();
                        personcode = vendor.getAcccode() == null ? "" : vendor.getAcccode();
                        paymentterm = vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getTermname();
                        personaccountId = vendor.getAccount() == null ? "" : vendor.getAccount().getID();
                        hasAccess = vendor.isActivate();
                    }else {
                        KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                        Customer customer = (Customer) resultObject.getEntityList().get(0);
                        personid = customer.getID();
                        personname = customer.getName();
                        aliasname = customer.getAliasname();
                        personcode = customer.getAcccode() == null ? "" : customer.getAcccode();
                        paymentterm = customer.getCreditTerm() == null ? "" : customer.getCreditTerm().getTermname();
                        personaccountId = customer.getAccount() == null ? "" : customer.getAccount().getID();
                        isCustomer = true;
                        hasAccess = customer.isActivate();
                    }
                }
                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                JSONObject obj = new JSONObject();
                if (withoutinventory) {
                    resultObject = accountingHandlerDAOobj.getObject(BillingCreditNote.class.getName(), (String) row[1]);
                    BillingCreditNote creditMemo = (BillingCreditNote) resultObject.getEntityList().get(0);
                    JournalEntry je = creditMemo.getJournalEntry();
                    //*** Attachments Documents SJ[ERP-16331] 
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", creditMemo.getID());
                    hashMap.put(Constants.companyKey, creditMemo.getCompany().getCompanyID());
                    KwlReturnObject object = accVendorDAOObj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    obj.put("attachment", attachemntcount);
                    obj.put(Constants.billid, creditMemo.getID());
                    //*** Attachments Documents SJ[ERP-16331] 
                    obj.put("withoutinventory", withoutinventory);
                    obj.put("noteid", creditMemo.getID());
                    obj.put(Constants.companyKey, creditMemo.getCompany().getCompanyID());
                    obj.put("companyname", creditMemo.getCompany().getCompanyName());
                    obj.put("noteno", creditMemo.getCreditNoteNumber());
                    obj.put("journalentryid", je.getID());
                    obj.put("currencysymbol", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));
                    obj.put(Constants.currencyKey, (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID()));
                    obj.put("entryno", je.getEntryNumber());
                    obj.put("personid", personid);
                    obj.put("personname", personname);
                    obj.put("aliasname", aliasname);
                    obj.put("personaccountid", personaccountId);
                    /*
                     * This fields are inserted for Credit Note Register Export
                     * Functionality - Mayur B.
                     */
                    obj.put("personcode", personcode);
                    obj.put("paymentterm", paymentterm);
                    obj.put("iscustomer", isCustomer);
                    obj.put("dateinuserformat", userdf.format(je.getEntryDate()));
                    obj.put("amount", details.getAmount());
                    obj.put("amountdue", 0);
                    obj.put("amountduenonnegative", 0);
                    obj.put("date", df.format(je.getEntryDate()));
                    obj.put("memo", creditMemo.getMemo());
                    obj.put("otherwise", false);//creditMemo.isOtherwise());
                    obj.put("openflag", false);//creditMemo.isOpenflag());
                    obj.put(Constants.cntype, 1);//creditMemo.getCntype());
                    obj.put("deleted", creditMemo.isDeleted());
                    obj.put("costcenterid", je.getCostcenter() == null ? "" : je.getCostcenter().getID());
                    obj.put("costcenterName", je.getCostcenter() == null ? "" : je.getCostcenter().getName());
                    obj.put("partlyJeEntryWithCnDn", je.getPartlyJeEntryWithCnDn());
                    obj.put("hasAccess", hasAccess);
                    KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), sessionHandlerImpl.getCompanyid(request));
                    Iterator iterator = result.getEntityList().iterator();
                    while (iterator.hasNext()) {
                        JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                        Account account = null;
                        account = jed.getAccount();
                        //To do - need to test.
                        if (account.getMastertypevalue() == Group.ACCOUNTTYPE_GST) {//GST transaction.
                            if (jed.isDebit()) {
                                tax = jed.getAmount();
                            }
                        }
                    }
                    result = accCreditNoteDAOobj.getTotalTax_TotalDiscount_Billing(creditMemo.getID());
                    double totTax = 0, totDiscount = 0;
                    if (result != null && result.getEntityList() != null) {
                        Iterator resItr = result.getEntityList().iterator();
                        Object[] sumRow = (Object[]) resItr.next();
                        if (sumRow[0] != null) {
                            totTax = Double.parseDouble(sumRow[0].toString());
                        }
                        if (sumRow[1] != null) {
                            totDiscount = Double.parseDouble(sumRow[1].toString());
                        }
                    }

                    obj.put("noteSubTotal", details.getAmount() + totDiscount - totTax);
                    obj.put("notetax", tax);
                    obj.put("totalTax", totTax);
                    obj.put("totalDiscount", totDiscount);

                } else {
                    resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                    CreditNote creditMemo = (CreditNote) resultObject.getEntityList().get(0);
                    JournalEntry je = creditMemo.getJournalEntry();
                    badDebtMap.put("companyid", creditMemo.getCompany().getCompanyID());
                    Date creditNoteDate = null;
                    double externalCurrencyRate = 0d;
                    boolean isopeningBalanceInvoice = creditMemo.isIsOpeningBalenceCN();
                    String journalentryid = "";
                    String jeentryno = "";
                    if (creditMemo.isNormalCN()) {
                        je = creditMemo.getJournalEntry();
                        creditNoteDate = creditMemo.getCreationDate();
                        externalCurrencyRate = je.getExternalCurrencyRate();
                        journalentryid = je.getID();
                        jeentryno = je.getEntryNumber();
                        List<Object> resultJe = accCreditNoteDAOobj.getForeignGainLossJE(creditMemo.getID(), sessionHandlerImpl.getCompanyid(request));
                        if (resultJe.size() > 0 && resultJe.get(0) != null) {
                            for (Object object:resultJe) {
                                String jeid = object != null ? object.toString() : "";
                                if (!StringUtil.isNullOrEmpty(jeid)) {
                                    resultObject = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
                                    JournalEntry foreignGainlossJe = (JournalEntry) resultObject.getEntityList().get(0);
                                    journalentryid += "," + foreignGainlossJe.getID();
                                    jeentryno += "," + foreignGainlossJe.getEntryNumber();
                                }

                            }
                        }

                    }

                    transactionCurrencyId = (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID());

                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("hasAccess", hasAccess);
                    obj.put("currencysymboltransaction", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));
                    String currencyFilterForTrans = "";
                    boolean isNoteForPayment = false;
                    if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteForPayment"))) {
                        isNoteForPayment = Boolean.parseBoolean(request.getParameter("isNoteForPayment"));
                    }
                    if (requestParams.containsKey("currencyfilterfortrans") && isNoteForPayment) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject curresultpayment = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyPayment = (KWLCurrency) curresultpayment.getEntityList().get(0);
                        obj.put("currencysymbol", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                        obj.put(Constants.currencyKey, (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getCurrencyID()));
                    } else {
                        obj.put("currencysymbol", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));
                        obj.put(Constants.currencyKey, (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID()));
                    }

                    double amountdue=0.0;
                    double amountDueOriginal = 0.0;
                        amountdue=(creditMemo.isOtherwise() || creditMemo.getCntype() == 5 || creditMemo.getCntype() == Constants.CreditNoteForOvercharge) ? creditMemo.getCnamountdue() : 0;            //getting amount due for credit note against vendor only for malaysian country ERP-27284 / ERP-28249
                        amountDueOriginal = (creditMemo.isOtherwise() || creditMemo.getCntype() == 5 || creditMemo.getCntype() == Constants.CreditNoteForOvercharge)? creditMemo.getCnamountdue() : 0;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                        amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                    }

                    KwlReturnObject baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                    obj.put("withoutinventory", withoutinventory);
                    /**
                     * Put GST document history.
                     */
                    if (creditMemo.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        obj.put("refdocid", creditMemo.getID());
                        fieldDataManagercntrl.getGSTDocumentHistory(obj);
                    }
                    obj.put("isReturnNote", creditMemo.getSalesReturn() == null ? false : true);
                    //*** Attachments Documents SJ[ERP-16331] 
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", creditMemo.getID());
                    hashMap.put(Constants.companyKey, creditMemo.getCompany().getCompanyID());
                    KwlReturnObject object = accVendorDAOObj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    obj.put("attachment", attachemntcount);
                    obj.put(Constants.billid, creditMemo.getID());
                    //*** Attachments Documents SJ[ERP-16331] 
                    obj.put("noteid", creditMemo.getID());
                    obj.put("isOldRecord", creditMemo.isOldRecord());
                    obj.put(Constants.companyKey, creditMemo.getCompany().getCompanyID());
                    obj.put("companyname", creditMemo.getCompany().getCompanyName());
                    obj.put("noteno", creditMemo.getCreditNoteNumber());
                    obj.put("journalentryid", journalentryid);
                    obj.put("entryno", jeentryno);
                    obj.put("personid", personid);
                    obj.put("personname", personname);
                    obj.put("aliasname", aliasname);
                    obj.put("personaccountid", personaccountId);
                    if (countryId != null && countryId == Constants.indian_country_id) {
                        Map<String, Object> filterMap = new HashMap();
                        filterMap.put("creditNote.ID", creditMemo.getID());
                        String[] columnNames = creditMemo.getCustomer() != null ? new String[]{"invoice.ID"} : new String[]{"goodsReceipt.ID"};
                        List invoiceIDs = kwlCommonTablesDAOObj.getRequestedObjectFieldsInCollection(CreditNoteInvoiceMappingInfo.class, columnNames, filterMap);
                        if (invoiceIDs != null && invoiceIDs.size() > 0) {
                            StringBuilder invoiceBuilder = new StringBuilder();
                            for (Object invoiceId : invoiceIDs) {
                                if (invoiceBuilder.length() > 0) {
                                    invoiceBuilder.append(",").append(invoiceId);
                                } else {
                                    invoiceBuilder.append(invoiceId);
                                }
                            }
                            obj.put("linkInvoices", invoiceBuilder.toString());
                        }
                    }

                    /*
                     * This fields are inserted for Credit Note Register Export
                     * Functionality - Mayur B.
                     */
                    obj.put("personcode", personcode);
                    obj.put("paymentterm", paymentterm);
                    obj.put("iscustomer", isCustomer);
                    obj.put("dateinuserformat", userdf.format(creditMemo.getCreationDate()));
                    double paidAmount = creditMemo.isOtherwise() ? creditMemo.getCnamount() : details.getAmount();
                    obj.put("amount", paidAmount);
                    KwlReturnObject paidAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, paidAmount, transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                    obj.put("paidamountinbase", authHandler.round((Double) paidAmt.getEntityList().get(0), companyid));

                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountdueinbase", ((Double)baseAmt.getEntityList().get(0)<=0)?0:authHandler.round((Double) baseAmt.getEntityList().get(0), companyid));
                    obj.put("amountinbase", authHandler.round(creditMemo.getCnamountinbase(),companyid));
                    if(isPostingDateCheck){
                        obj.put("date", df.format(creditMemo.getCreationDate()));
                    }else{
                        obj.put("date", df.format(je.getEntryDate()));
                    }
                    obj.put("memo", creditMemo.getMemo());
                    if(creditMemo.getTax()!=null){
                        obj.put("gTaxId", creditMemo.getTax().getID());
                    }
                    obj.put("createdby", creditMemo.getCreatedby() == null ? "" : StringUtil.getFullName(creditMemo.getCreatedby()));
                    obj.put("includingGST", creditMemo.isIncludingGST());
                    obj.put("deleted", creditMemo.isDeleted());
                    obj.put("salesPerson", (creditMemo.getSalesPerson()==null)?"":creditMemo.getSalesPerson().getValue()) ;
                    obj.put("otherwise", creditMemo.isOtherwise());
                    obj.put("isprinted", creditMemo.isPrinted());
                    obj.put("openflag", creditMemo.isOpenflag());
                    obj.put("isCreatedFromReturnForm", (creditMemo.getSalesReturn() != null) ? true : false);
                    obj.put(Constants.cntype, creditMemo.getCntype());
                    obj.put("costcenterid", creditMemo.getCostcenter() == null ? "" : creditMemo.getCostcenter().getID());
                    obj.put("costcenterName", creditMemo.getCostcenter() == null ? "" : creditMemo.getCostcenter().getName());
                    obj.put("salesPersonID", creditMemo.getSalesPerson() == null ? "" : creditMemo.getSalesPerson().getID());
                    obj.put("partlyJeEntryWithCnDn", je.getPartlyJeEntryWithCnDn());
                    obj.put("accountid", creditMemo.getAccount() == null ? "" : creditMemo.getAccount().getID());
                    obj.put("accountnames", creditMemo.getAccount() == null ? "" : creditMemo.getAccount().getName());
                    obj.put("externalcurrencyrate", je.getExternalCurrencyRate());
                    obj.put("agent", (creditMemo.getMasterAgent()==null)?"":creditMemo.getMasterAgent().getValue()) ;
                    obj.put("agentid", (creditMemo.getMasterAgent()==null)?"":creditMemo.getMasterAgent().getID()) ;
                    obj.put(Constants.MVATTRANSACTIONNO, creditMemo.getMvatTransactionNo()!=null?creditMemo.getMvatTransactionNo():"") ;
                    obj.put("gstCurrencyRate", creditMemo.getGstCurrencyRate());
                    if (isApprovalPendingReport) {//these data only need for pending Approval report of CN
                        obj = getCreditNoteApprovalPendingJsonData(obj,creditMemo.getID(),companyid,userid,userName);
                    }

                    Set<CreditNoteTaxEntry> cnTaxEntryDetails = creditMemo.getCnTaxEntryDetails();
                    String reason = "";
                    double totalCnTax = 0;
                    double totalTermAmount = 0;
                    boolean considerTermAmount = false; // This will be used CN is not linked with any sales return and country = India.
                    if (cnTaxEntryDetails != null && !cnTaxEntryDetails.isEmpty()) {

                        for (CreditNoteTaxEntry noteTaxEntry : cnTaxEntryDetails) {
                            reason += ((noteTaxEntry.getReason() != null) ? noteTaxEntry.getReason().getValue() : "") + ",";
                            if (noteTaxEntry.isDebitForMultiCNDN()) {
                                totalCnTax += noteTaxEntry.getTaxamount();
                            } else {
                                totalCnTax -= noteTaxEntry.getTaxamount();
                            }
                            if (countryId != null && countryId == Constants.indian_country_id && creditMemo.getSalesReturn() == null && !StringUtil.isNullOrEmpty(noteTaxEntry.getProductid())) {
                                /**
                                 * If credit note is not linked to sales return
                                 * then show tax amount using termamount column.
                                 */
                                if (noteTaxEntry.isDebitForMultiCNDN()) {
                                    totalTermAmount += noteTaxEntry.getTermAmount();
                                } else {
                                    totalTermAmount -= noteTaxEntry.getTermAmount();
                                }
                                considerTermAmount = true;
                            }
                            if (reportID == 822) {
                                if (obj.has(noteTaxEntry.getAccount().getID() + "_Amount")) {
                                    double temAccAmount = obj.optDouble((noteTaxEntry.getAccount().getID() + "_Amount"), 0);
                                    obj.put(noteTaxEntry.getAccount().getID() + "_Amount", temAccAmount + noteTaxEntry.getAmount());
                                } else {
                                    obj.put(noteTaxEntry.getAccount().getID() + "_Amount", noteTaxEntry.getAmount());
                                }
                            }
                        }
                    }
                    double taxAmount = 0.0;
                    if (creditMemo.getCntype() == 5 || creditMemo.getCntype() == Constants.CreditNoteForOvercharge) {
                        obj.put("amountinbase", authHandler.round((Double) paidAmt.getEntityList().get(0), companyid));
                        Set<CreditNoteAgainstVendorGst> dnTaxGstDetails = creditMemo.getRowsGst();
                        totalCnTax = 0;
                        if (dnTaxGstDetails != null && !dnTaxGstDetails.isEmpty()) {

                            for (CreditNoteAgainstVendorGst noteTaxGstEntry : dnTaxGstDetails) {
                                reason += ((noteTaxGstEntry.getReason() != null) ? noteTaxGstEntry.getReason().getValue() : "") + ",";
                                totalCnTax += noteTaxGstEntry.getRowTaxAmount();
                            }
                        }
//                      Below code is written to get tax for credit note against vendor only for malaysian country ERP-27284 / ERP-28249
                        double cnAmount = 0.0;
                        double cnAmountExcludingTax = 0.0;
                        Tax taxObj = creditMemo.getTax();
                        KwlReturnObject result1 = accJournalEntryobj.getJournalEntryDetail(creditMemo.getJournalEntry().getID(), creditMemo.getJournalEntry().getCompany().getCompanyID());
                        Iterator iterator = result1.getEntityList().iterator();
                        boolean taxflag = false;
                        while (iterator.hasNext()) {
                            JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                            Account account = null;
                            account = jed.getAccount();

                            if (taxObj!=null && jed.isDebit() && account.getID().equals(taxObj.getAccount().getID())) {
                                taxAmount += jed.getAmount();
                                taxflag = true;
                            }
                            if (jed.isDebit()) {
                                cnAmount += jed.getAmount();
                            }
                        }
                        cnAmountExcludingTax = cnAmount - taxAmount;
                    }
                    if (considerTermAmount) {
                        obj.put("taxamount", authHandler.round(totalTermAmount, companyid));
                        totalCnTax = totalTermAmount;
                    } else {
                        if (totalCnTax != 0) {
                            obj.put("taxamount", authHandler.round(totalCnTax, companyid));
                        } else {
                            obj.put("taxamount", authHandler.round(taxAmount, companyid));
                        }
                    }
                    boolean isLinkedTransaction = accCreditNoteDAOobj.isCreditNoteLinkedToOtherTransaction("CreditNote", creditMemo.getID());
                    obj.put(Constants.IS_LINKED_TRANSACTION, isLinkedTransaction);
                    KwlReturnObject taxAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, authHandler.round(totalCnTax, companyid), transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                    obj.put("taxamountinbase", authHandler.round((Double) taxAmt.getEntityList().get(0), companyid));
                    double cnTotalAmount = creditMemo.isOtherwise() ? creditMemo.getCnamount() : details.getAmount();
                    if (totalCnTax != 0) {
                        obj.put("amountbeforegst", authHandler.round(cnTotalAmount - totalCnTax, companyid));
                    } else {
                        obj.put("amountbeforegst", authHandler.round(cnTotalAmount - taxAmount, companyid));
                    }
                    if (!StringUtil.isNullOrEmpty(reason)) {
                        obj.put("reason", reason.substring(0, reason.length() - 1));
                    } else {
                        obj.put("reason", reason);
                    }
                    obj.put("currencyname", (creditMemo.getCurrency() == null ? currency.getName() : creditMemo.getCurrency().getName()));
                    obj.put("currencycode", (creditMemo.getCurrency() == null ? currency.getCurrencyCode() : creditMemo.getCurrency().getCurrencyCode()));
                    obj.put(Constants.SEQUENCEFORMATID, creditMemo.getSeqformat() != null ? creditMemo.getSeqformat().getID() : "");
                    BillingShippingAddresses addresses = creditMemo.getBillingShippingAddresses();
                    if (cntype == Constants.CreditNoteAgainstVendor && transactiontype != 8) {//CN against vendor  
                        AccountingAddressManager.getTransactionAddressJSON(obj, addresses, true);
                    } else {
                        AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    }
                    KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), sessionHandlerImpl.getCompanyid(request));
                    Iterator iterator = result.getEntityList().iterator();
                    while (iterator.hasNext()) {
                        JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                        Account account = null;
                        account = jed.getAccount();
                        //To do - need to test.
                        if (account.getMastertypevalue() == Group.ACCOUNTTYPE_GST) {//GST transaction.
                            if (jed.isDebit()) {
                                tax = jed.getAmount();
                            }
                        }
                    }
                    result = accCreditNoteDAOobj.getTotalTax_TotalDiscount(creditMemo.getID());
                    double totTax = 0, totDiscount = 0;
                    if (result != null && result.getEntityList() != null) {
                        Iterator resItr = result.getEntityList().iterator();
                        Object[] sumRow = (Object[]) resItr.next();
                        if (sumRow[0] != null) {
                            totTax = Double.parseDouble(sumRow[0].toString());
                        }
                        if (sumRow[1] != null) {
                            totDiscount = Double.parseDouble(sumRow[1].toString());
                        }
                    }

                    obj.put("noteSubTotal", details.getAmount() + totDiscount - totTax);
                    obj.put("notetax", tax);
                    obj.put("totalTax", totTax);
                    obj.put("totalDiscount", totDiscount);

                    int isReval = 0;
                    KwlReturnObject brdAmt = accInvoiceDAOObj.getRevalFlag(creditMemo.getID());
                    List reval = brdAmt.getEntityList();
                    if (!reval.isEmpty() && (Long) reval.get(0) > 0) {
                        isReval = 1;
                    }
                    obj.put("isreval", isReval);
                    obj.put("lasteditedby", creditMemo.getModifiedby() == null ? "" : (creditMemo.getModifiedby().getFirstName() + " " + creditMemo.getModifiedby().getLastName()));
                    /*
                     * If line level tax is present then includeprotax field is true 
                     */
                    if (creditMemo.getCntype() == 5 || creditMemo.getCntype() == Constants.CreditNoteForOvercharge) {
                        Set<CreditNoteAgainstVendorGst> cndetailsGst = (Set<CreditNoteAgainstVendorGst>) creditMemo.getRowsGst();
                        for (CreditNoteAgainstVendorGst noteDetail : cndetailsGst) {
                            if (noteDetail.getTax() != null) {
                                checkTax = true;
                                break;
                            }

                        }
                    }
                    obj.put("includeprotax", checkTax);
                    Set<CreditNoteDetail> cndetails = creditMemo.getRows();
                    boolean isLinked = false;
                    String linkedDate = null;
                    for (CreditNoteDetail noteDetail : cndetails) {
                        if (noteDetail.getInvoice() != null) {
                            /*
                             * Applicable for Malaysian Country. Checking if any linked invoice is claimed 
                             */
                            if ((noteDetail.getInvoice().getBadDebtType() == 1 || noteDetail.getInvoice().getBadDebtType() == 2)) {
                                badDebtMap.put("invoiceid", noteDetail.getInvoice().getID());
                                badDebtResult = accInvoiceDAOObj.getBadDebtInvoiceMappingForInvoice(badDebtMap);
                                List<BadDebtInvoiceMapping> maplist = badDebtResult.getEntityList();
                                if (maplist != null && !maplist.isEmpty()) {
                                    BadDebtInvoiceMapping mapping = maplist.get(0);
                                    //if (!isLinkedInvoiceClaimed && mapping.getBadDebtClaimedDate().getTime() > noteDetail.getInvoiceLinkDate()) {
                                    if (!isLinkedInvoiceClaimed && (mapping.getBadDebtClaimedDate().after(noteDetail.getInvoiceLinkDate()))) {
                                        isLinkedInvoiceClaimed = true;
                                        obj.put("isLinkedInvoiceIsClaimed", isLinkedInvoiceClaimed);
                                    }
                                }
                            }

                            isLinked = true;
                            linkedDate=sdf.format(noteDetail.getInvoiceLinkDate());
                            break;
                        }else if(!StringUtil.isNullOrEmpty(noteDetail.getDebitNoteId())){
                            isLinked = true;
                            linkedDate=sdf.format(noteDetail.getInvoiceLinkDate());
                            break;
                        }else  {
                            /*  Checking Whether CN against Vendor 
                            
                             is linking with DN against Vendor or not*/
                            Boolean isRecord = accCreditNoteDAOobj.checkCNLinking(creditMemo.getID());
                            if (isRecord) {
                                isLinked = true;
                                break;
                            }
                        }
                    }
                    obj.put("isLinked", isLinked);
                    if (linkedDate != null) {
                        obj.put("linkingdate", linkedDate);
                    }
                    /*
                     * Credit note will be allowed to copy in following case- 1.
                     * CN against sales invoice - NOT allowed 2. CN otherwise -
                     * allowed if no invoice is linked to CN at time of creation
                     * or later on 3. CN against Vendor - Allowed
                     */
                    if (creditMemo.getCntype() == Constants.CreditNoteAgainstSalesInvoice) {
                        obj.put("isCopyAllowed", false);
                    } else if (creditMemo.getCntype() == Constants.CreditNoteOtherwise) {
                        boolean copyFlag = true;
//                        Set<CreditNoteDetail> cndetails = creditMemo.getRows();
                        for (CreditNoteDetail cndetail : cndetails) {
                            if (cndetail.getInvoice() != null) {
                                copyFlag = false;
                                break;
                            }
                        }
                        obj.put("isCopyAllowed", copyFlag);
                    } else if (creditMemo.getCntype() == Constants.CreditNoteAgainstVendor) {
                        obj.put("isCopyAllowed", true);
                    } else {
                        obj.put("isCopyAllowed", false);
                    }
                    HashMap<String, Object> reqParams1 = new HashMap<>();
                    reqParams1.put("cnid", creditMemo.getID());
                    reqParams1.put(Constants.companyKey, creditMemo.getCompany().getCompanyID());
                    KwlReturnObject linkResult = accCreditNoteDAOobj.getLinkDetailPaymentToCreditNote(reqParams1);
                    if (!linkResult.getEntityList().isEmpty()) {
                        obj.put("isNoteLinkedToAdvancePayment", true);
                    }
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                    Detailfilter_names.add(Constants.companyKey);
                    Detailfilter_params.add(creditMemo.getCompany().getCompanyID());
                    Detailfilter_names.add("journalentryId");
                    Detailfilter_params.add(creditMemo.getJournalEntry().getID());
                    Detailfilter_names.add("moduleId");
                    Detailfilter_params.add(Constants.Acc_Credit_Note_ModuleId + "");
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomData(invDetailRequestParams);
                    if (idcustresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        boolean isExport = (request.getAttribute("isExport") == null) ? false : (boolean) request.getAttribute("isExport");
                        params.put("isExport", isExport);
                        if (!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) {
                            params.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }

                }
                if (requestParams.containsKey("isReceipt") && obj.optDouble("amountdue", 0.0) != 0) {
                    jArr.put(obj);
                } else if (!requestParams.containsKey("isReceipt")) {
                    jArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getCreditNoteJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    @Override
    public JSONObject getCreditNoteApprovalPendingJsonData(JSONObject obj, String noteid, String companyid, String userid, String userName) throws ServiceException {
        try {
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), noteid);
            CreditNote creditNote = (CreditNote) resultObject.getEntityList().get(0);
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            String multipleRuleids="";
            if (creditNote != null) {
                int approvallevel = creditNote.getApprovestatuslevel();
                double amountInBase = authHandler.round(creditNote.getCnamountinbase(), companyid);
                String approvalStatus = "";
                if (approvallevel < 0) {//will be negartive for rejected
                    approvalStatus = "Rejected";
                } else if (creditNote.getApprovestatuslevel() < 11) {//will be less than 11 for pending record 
                    String ruleid = "", userRoleName = "";
                    HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                    qdDataMap.put(Constants.companyKey, companyid);
                    qdDataMap.put("level", approvallevel);
                    qdDataMap.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                    KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                    Iterator ruleitr = flowresult.getEntityList().iterator();
                    while (ruleitr.hasNext()) {
                        Object[] rulerow = (Object[]) ruleitr.next();
                        ruleid = rulerow[0].toString();
                        int appliedUpon = Integer.parseInt(rulerow[5].toString());
                        String rule = "";
                        if (rulerow[2] != null) {
                            rule = rulerow[2].toString();
                        }
                        if (appliedUpon == Constants.Total_Amount) {
                            /*
                             Added to get condition of approval rule i.e set when creating approval rule 
                             */
                            rule = rule.replaceAll("[$$]+", String.valueOf(amountInBase));
                        }
                        /*
                         Added to check if record falls in total amount approval rule 
                         */
                        if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon == Constants.Total_Amount && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                            multipleRuleids += ruleid + ",";
                        }

                    }
                    /*
                     Added to get multiple ruleid if record falls in multiple approval rule 
                     */
                    String[] multipleRuleidsArray = multipleRuleids.split(",");
                    for (int multiRule = 0; multiRule < multipleRuleidsArray.length; multiRule++) {
                        ruleid = multipleRuleidsArray[multiRule];
                        if (!StringUtil.isNullOrEmpty(ruleid)) {
                            qdDataMap.put("ruleid", ruleid);
                            KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(qdDataMap);
                            List<Object[]> list = userResult.getEntityList();
                            for (Object[] userrow : list) {
                                String approvalID = userrow[0].toString();
                                String approvalName = userrow[1].toString();
                                /*
                                 Addded so duplicate approve's can be eleminated 
                                 */
                                if (userRoleName.contains(approvalName)) {
                                    continue;
                                }
                                KwlReturnObject kmsg = null;
                                String roleName = "Company User";
                                kmsg = permissionHandlerDAOObj.getRoleofUser(approvalID);
                                Iterator ite2 = kmsg.getEntityList().iterator();
                                while (ite2.hasNext()) {
                                    Object[] row = (Object[]) ite2.next();
                                    roleName = row[1].toString();
                                }
                                userRoleName += roleName + " " + approvalName + ",";
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(userRoleName)) {
                        userRoleName = userRoleName.substring(0, userRoleName.length() - 1);
                    }
                    approvalStatus = "Pending Approval" + (StringUtil.isNullOrEmpty(userRoleName) ? "" : " by " + userRoleName) + " at Level - " + approvallevel;
                } else {
                    approvalStatus = "Approved";
                }
                obj.put("approvalstatusinfo", approvalStatus);
                obj.put("approvalLevel", approvallevel);

                KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
                User user = (User) userclass.getEntityList().get(0);
                boolean hasApprovalAuthority = false;
                if (AccountingManager.isCompanyAdmin(user)) {
                    hasApprovalAuthority = true;
                } else {
                    HashMap<String, Object> cnApproveMap = new HashMap<String, Object>();
                    cnApproveMap.put(Constants.companyKey, companyid);
                    cnApproveMap.put("level", approvallevel);
                    cnApproveMap.put("totalAmount", String.valueOf(amountInBase));
                    cnApproveMap.put("currentUser", userid);
                    cnApproveMap.put("fromCreate", false);
                    cnApproveMap.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                    hasApprovalAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(cnApproveMap);
                }
                obj.put("hasApprovalAuthority", hasApprovalAuthority);

                int nextApprovalLevel = 11;
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", approvallevel + 1);
                qdDataMap.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                Iterator approvalRuleItr = flowresult.getEntityList().iterator();
                while (approvalRuleItr.hasNext()) {
                    Object[] rowObj = (Object[]) approvalRuleItr.next();
                    String rule = "";
                    if (rowObj[2] != null) {
                        rule = rowObj[2].toString();
                    }
                    rule = rule.replaceAll("[$$]+", String.valueOf(amountInBase));
                    if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                        nextApprovalLevel = approvallevel + 1;
                    }
                }
                obj.put("isFinalLevelApproval", nextApprovalLevel == 11 ? true : false);
                obj.put(Constants.cntype,creditNote.getCntype());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getCreditNoteJson : " + ex.getMessage(), ex);
        }
        return obj;
    }

    @Override
    public JSONArray getOpeningCreditNotesJson(HashMap<String, Object> requestParams, List<CreditNote> list, JSONArray JArr) throws ServiceException {
        try {
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat userdf = (DateFormat) requestParams.get("userdf");
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            String companyid = (String) requestParams.get(Constants.companyKey);
            int cnType = (Integer) requestParams.get(Constants.cntype);
            boolean isdefaultHeaderMap = false;
            if (requestParams.containsKey(Constants.isdefaultHeaderMap) && requestParams.get(Constants.isdefaultHeaderMap) != null) {
                isdefaultHeaderMap = (Boolean) requestParams.get(Constants.isdefaultHeaderMap);
            }

            boolean isNoteForPayment = false;
            if (requestParams.containsKey("isNoteForPayment")) {
                isNoteForPayment = (Boolean) requestParams.get("isNoteForPayment");
            }

            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Credit_Note_ModuleId));
            FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            if (list != null && !list.isEmpty()) {
                for (CreditNote cn:list) {
                    JSONObject obj = new JSONObject();
                    String personid = "";
                    String personname = "";
                    String paymentterm = "";
                    String personcode = "";
                    boolean isCustomer = false;
                    boolean hasAccess = true;
                    if (cnType == 11) {
                        if (cn.getVendor() != null) {
                            personid = cn.getVendor().getID();
                            personname = cn.getVendor().getName();
                            personcode = cn.getVendor().getAcccode() == null ? "" : cn.getVendor().getAcccode();
                            paymentterm = cn.getVendor().getDebitTerm() == null ? "" : cn.getVendor().getDebitTerm().getTermname();
                            hasAccess = cn.getVendor().isActivate();
                        }
                    } else {
                        personid = cn.getCustomer().getID();
                        personname = cn.getCustomer().getName();
                        personcode = cn.getCustomer().getAcccode() == null ? "" : cn.getCustomer().getAcccode();
                        paymentterm = cn.getCustomer().getCreditTerm() == null ? "" : cn.getCustomer().getCreditTerm().getTermname();
                        hasAccess = cn.getCustomer().isActivate();
                        isCustomer = true;
                    }

                    Date creditNoteDate = null;
                    double externalCurrencyRate = 0d;
                    boolean isopeningBalanceInvoice = cn.isIsOpeningBalenceCN();
                    if (cn.isIsOpeningBalenceCN()) {
                        creditNoteDate = cn.getCreationDate();
                        externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                    }

                    String transactionCurrencyId = (cn.getCurrency() == null ? currency.getCurrencyID() : cn.getCurrency().getCurrencyID());

                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("currencysymboltransaction", (cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol()));
                    obj.put("hasAccess", hasAccess);
                    String currencyFilterForTrans = "";
                    if (requestParams.containsKey("currencyfilterfortrans")) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                    }
                    if (requestParams.containsKey("currencyfilterfortrans") && isNoteForPayment) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject curresultpayment = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyPayment = (KWLCurrency) curresultpayment.getEntityList().get(0);
                        obj.put("currencycode", (currencyPayment == null ? currency.getCurrencyCode() : currencyPayment.getCurrencyCode()));
                        obj.put("currencysymbol", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                        obj.put(Constants.currencyKey, (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getCurrencyID()));
                        obj.put(Constants.currencyKey+"Value", (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getName()));
                    } else {
                        obj.put("currencycode", (cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode()));
                        obj.put("currencysymbol", (cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol()));
                        obj.put(Constants.currencyKey, (cn.getCurrency() == null ? currency.getCurrencyID() : cn.getCurrency().getCurrencyID()));
                        obj.put(Constants.currencyKey+"Value", (cn.getCurrency() == null ? currency.getCurrencyID() : cn.getCurrency().getName()));
                    }

                    double amountdue = cn.isOtherwise() ? cn.getCnamountdue() : 0;
                    double amountDueOriginal = cn.isOtherwise() ? cn.getCnamountdue() : 0;
                    KwlReturnObject baseAmt = null;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        if (isopeningBalanceInvoice && cn.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            requestParams.put("isRevalue", true);
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                            amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                        } else {
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                            amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                        }
                    }
                    if (isopeningBalanceInvoice && cn.isConversionRateFromCurrencyToBase()) {
                        baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                    } else {
                        baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                    }

                    if (isdefaultHeaderMap) {
                        if (cnType == Constants.OpeingCreditNoteforCustomers) {
                            obj.put("vendor", personid);
                            obj.put("vendorValue", personname);
                        } else {
                            obj.put("customer", personid);
                            obj.put("customerValue", personname);
                        }
                        obj.put(Constants.creationdate, df.format(cn.getCreationDate()));
                        obj.put("number", cn.getCreditNoteNumber());
                        obj.put("costcenter", "");
                        obj.put("costcenterValue", "");
                        obj.put("linkingdate", "");
                    } else {
                        obj.put("noteno", cn.getCreditNoteNumber());
                        obj.put("date", df.format(cn.getCreationDate()));
                        obj.put("personid", personid);
                        obj.put("personname", personname);
                        obj.put("costcenterid", "");
                        obj.put("costcenterName", "");
                    }

                    obj.put("withoutinventory", false);
                    obj.put("isOpeningBalanceTransaction", cn.isIsOpeningBalenceCN());
                    obj.put("isNormalTransaction", cn.isNormalCN());
                    obj.put("noteid", cn.getID());
                    obj.put(Constants.companyKey, cn.getCompany().getCompanyID());
                    obj.put("companyname", cn.getCompany().getCompanyName());
                    obj.put("journalentryid", "");
                    obj.put("entryno", "");
                    obj.put("personcode", personcode);
                    obj.put("paymentterm", paymentterm);
                    obj.put("iscustomer", isCustomer);
                    obj.put("dateinuserformat", userdf.format(cn.getCreationDate()));
                    obj.put("amount", cn.getCnamount());
                    obj.put("paidamountinbase", authHandler.round((Double) baseAmt.getEntityList().get(0), companyid));
                    obj.put("amountdue", cn.getCnamountdue());
                    obj.put("amountduenonnegative", cn.getCnamountdue());
                    obj.put("memo", cn.getMemo() == null ? "" : cn.getMemo());
                    obj.put("createdby", cn.getCreatedby() == null ? "" : StringUtil.getFullName(cn.getCreatedby()));
                    obj.put("deleted", false);
                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountdueinbase", authHandler.round((Double) baseAmt.getEntityList().get(0), companyid));
                    obj.put("amountinbase", authHandler.round(cn.getOriginalOpeningBalanceBaseAmount(),companyid));
                    obj.put("otherwise", true);     //Used to enable Export Record(s) button for all company. Otherwisw ll consider for CN against Customer case.
                    obj.put("openflag", cn.isOpenflag());
                    obj.put("noteSubTotal", cn.getCnamount());
                    obj.put("notetax", 0);
                    obj.put("totalTax", 0);
                    obj.put("totalDiscount", 0);
                    obj.put(Constants.MVATTRANSACTIONNO,cn.getMvatTransactionNo()!=null?cn.getMvatTransactionNo():"");
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                    Detailfilter_names.add(Constants.companyKey);
                    Detailfilter_params.add(cn.getCompany().getCompanyID());
                    Detailfilter_names.add("OpeningBalanceCreditNoteId");
                    Detailfilter_params.add(cn.getID());
                    Detailfilter_names.add("moduleId");
                    Detailfilter_params.add(Constants.Acc_Credit_Note_ModuleId + "");
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accJournalEntryobj.getOpeningBalanceCreditNoteCustomData(invDetailRequestParams);
                    if (idcustresult.getEntityList().size() > 0) {
                        OpeningBalanceCreditNoteCustomData balanceCreditNoteCustomData = (OpeningBalanceCreditNoteCustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(balanceCreditNoteCustomData, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, companyid);
                        params.put("isExport", true);
                        if (requestParams.containsKey("browsertz") && requestParams.get("browsertz") != null) {
                            params.put("browsertz", requestParams.get("browsertz").toString());
                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    Set<CreditNoteDetail> cndetails = cn.getRows();
                    boolean isLinked = false;
                    for (CreditNoteDetail noteDetail : cndetails) {
                        if (noteDetail.getInvoice() != null) {
                            isLinked = true;
                            break;
                        } else if (!StringUtil.isNullOrEmpty(noteDetail.getDebitNoteId())) { //for enable to unlink button after selection of Opening CN against Vendor
                            isLinked = true;
                            break;
                        } else if (cnType == 11) {//If CN against Vendor
                            Boolean isRecord = accCreditNoteDAOobj.checkCNLinking(cn.getID());
                            if (isRecord) {
                                isLinked = true;
                                break;
                            }
                        }
                    }
                    obj.put("isLinked", isLinked);
                    obj.put(Constants.cntype, cnType);
                    HashMap<String, Object> reqParams1 = new HashMap<>();
                    reqParams1.put("cnid", cn.getID());
                    reqParams1.put(Constants.companyKey, cn.getCompany().getCompanyID());
                    KwlReturnObject linkResult = accCreditNoteDAOobj.getLinkDetailPaymentToCreditNote(reqParams1);
                    if (!linkResult.getEntityList().isEmpty()) {
                        obj.put("isNoteLinkedToAdvancePayment", true);
                    }
                    JArr.put(obj);
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }

    /**
     * Description : Method is used to Update Entry in linking information table
     * for Credit Note & Invoice If any Invoice linked with Credit Note
     *
     * @param <creditNoteDetailID> ID of Credit Note Detail which linked in
     * Invoice
     *
     * @return :void
     */
    
    @Override
    public void updateLinkingInformationOfCreditNote(String creditNoteDetailID) throws ServiceException, SessionExpiredException, JSONException, AccountingException {

        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(CreditNoteDetail.class.getName(), creditNoteDetailID);
        CreditNoteDetail creditNoteDetail = (CreditNoteDetail) rdresult.getEntityList().get(0);

        String creditNoteNo = creditNoteDetail.getCreditNote().getCreditNoteNumber();
        String creditNoteId = creditNoteDetail.getCreditNote().getID();
        String invoiceNo = creditNoteDetail.getInvoice().getInvoiceNumber();
        String invoiceId = creditNoteDetail.getInvoice().getID();

        /* Checking Entry of Credit Note 
         * 
         * in linking Information table whether it is present or not*/
        
        KwlReturnObject result = accCreditNoteDAOobj.checkEntryForCreditNoteInLinkingTable(creditNoteId, invoiceId);
        List list = result.getEntityList();

        if (list == null || list.isEmpty()) {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("sourceflag", 0);
            requestParams.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
            requestParams.put("linkeddocno", creditNoteNo);
            requestParams.put("docid", invoiceId);
            requestParams.put("linkeddocid", creditNoteId);
            result = accInvoiceDAOObj.saveInvoiceLinking(requestParams);

            requestParams.put("sourceflag", 1);
            requestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
            requestParams.put("linkeddocno", invoiceNo);
            requestParams.put("docid", creditNoteId);
            requestParams.put("linkeddocid", invoiceId);
            result = accCreditNoteDAOobj.saveCreditNoteLinking(requestParams);
        }
    }

    @Override
    public JSONObject getColumnsForCreditNoteWithAccounts(HttpServletRequest request, boolean isExport) {
        JSONObject commData = new JSONObject();
        String accountIds = StringUtil.isNullOrEmpty(request.getParameter("cnAccountIds")) ? "" : request.getParameter("cnAccountIds");
        try {
            // Column Model
            int reportId = 822;
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
            Company company = (Company) cmpresult.getEntityList().get(0);
            List customFieldList = new ArrayList();
            HashMap hashMap=new HashMap();
            hashMap.put("companyId", company.getCompanyID());
            hashMap.put("reportId", reportId);
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();

            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "entryno");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "journalentryid");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "noteid");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "isReturnNote");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "otherwise");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "openflag");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "isOpeningBalanceTransaction");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "isNormalTransaction");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "isOldRecord");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", Constants.cntype);
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "isCreatedFromReturnForm");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "partlyJeEntryWithCnDn");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "sequenceformatid");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "externalcurrencyrate");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "noteno");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "personid");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "salesPersonID");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "costcenterid");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", Constants.currencyKey);
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.cnList.gridNoteNo", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "noteno");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("renderer", "WtfGlobal.linkDeletedRenderer");
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "date");
            jobjTemp.put("type", "date");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header",messageSource.getMessage("acc.cnList.gridDate", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "date");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("renderer", "WtfGlobal.onlyDateDeletedRenderer");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "personname");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header",messageSource.getMessage("mrp.workorder.report.header3", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "personname");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "aliasname");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mastercontract.aliasname", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "aliasname");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencycode");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencysymbol");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "entryno");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.repeatedJE.Gridcol1", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "entryno");
            jobjTemp.put("renderer", "WtfGlobal.linkDeletedRenderer");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "memo");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.common.memo", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "memo");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencycode");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.agedPay.gridCurrency", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "currencycode");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            /*
             * Insert Account as column Over here
             */
            String[] accID = accountIds.split(",");
            for (String accountID : accID) {
                KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountID);
                if (accountresult.getEntityList() != null && !accountresult.getEntityList().isEmpty()) {
                    Account account = (Account) accountresult.getEntityList().get(0);
                    if (account != null) {
                        jobjTemp = new JSONObject();
                        jobjTemp.put("name", accountID + "_Amount");
                        jobjTemp.put("defaultValue",0);
                        jarrRecords.put(jobjTemp);

                        jobjTemp = new JSONObject();
                        jobjTemp.put("header", account.getAccountName());
                        jobjTemp.put("dataIndex", accountID + "_Amount");
                        jobjTemp.put("align", "right");
                        jobjTemp.put("pdfrenderer", "rowcurrency");
                        jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                        jobjTemp.put("width", 150);
                        jobjTemp.put("pdfwidth", 150);
                        jarrColumns.put(jobjTemp);
                    }
                }

            }
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "taxamount");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.invoice.gridTaxAmount", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "taxamount");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountbeforegst");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header",messageSource.getMessage("acc.invoiceList.AmntBeforeGST", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "amountbeforegst");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amount");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header",messageSource.getMessage("acc.serviceTaxComputationReport.totalAmount", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "amount");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountinbase");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header",messageSource.getMessage("acc.invoiceList.totAmtHome", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "amountinbase");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.1099.gridAmtDue", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "amountdue");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdueinbase");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.agedPay.gridAmtDueHomeCurrency", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "amountdueinbase");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "reason");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.masterConfig.29", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "reason");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            /*
             * Include Custom Fileds
             */
            for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                String column = "Custom_" + customizeReportMapping.getDataIndex();

                if (!customFieldList.contains(customizeReportMapping.getDataIndex())) {
                    jobjTemp = new JSONObject();
                    jobjTemp.put("name", column);
                    jarrRecords.put(jobjTemp);

                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", customizeReportMapping.getDataHeader());
                    jobjTemp.put("dataIndex", column);
                    jobjTemp.put("width", 150);
                    jobjTemp.put("pdfwidth", 150);
                    jobjTemp.put("custom", "true");
                    jarrColumns.put(jobjTemp);

                    customFieldList.add(customizeReportMapping.getDataIndex());
                }
            }

            commData.put("success", true);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return commData;
    }

    @Override
        public boolean rejectPendingCreditNote(Map<String, Object> requestParams,JSONArray jArr) throws  ServiceException {
        boolean isRejected = false;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String currentUser = (String) requestParams.get("userid");
            String userFullName = (String) requestParams.get("userName");
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), currentUser);
            User user = (User) userResult.getEntityList().get(0);

            String actionMsg = "rejected";
            int level = 0;
            double amount = (!StringUtil.isNullObject(requestParams.get("amount")) && StringUtil.isNullOrEmpty(requestParams.get("amount").toString())) ? 0 : authHandler.round(Double.parseDouble(requestParams.get("amount").toString()), companyid);
            for (int i = 0; i < jArr.length(); i++) {
                boolean hasAuthorityToReject = false;
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString(Constants.billid))) {
                    String invid = StringUtil.DecodeText(jobj.optString(Constants.billid));
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), invid);
                    CreditNote creditNote = (CreditNote) cap.getEntityList().get(0);
                    HashMap<String, Object> invApproveMap = new HashMap<String, Object>();
                    level = creditNote.getApprovestatuslevel();
                    invApproveMap.put(Constants.companyKey, companyid);
                    invApproveMap.put("level", level);
                    invApproveMap.put("totalAmount", String.valueOf(amount));
                    invApproveMap.put("currentUser", currentUser);
                    invApproveMap.put("fromCreate", false);
                    invApproveMap.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(invApproveMap);
                    }
                    if (hasAuthorityToReject) {
                        accCreditNoteDAOobj.rejectPendingCreditNote(creditNote.getID(), companyid);
                        isRejected = true;
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("transtype", AuditAction.CREDIT_NOTE_REJECTED);
                        hashMap.put("transid", creditNote.getID());
                        hashMap.put("approvallevel", Math.abs(creditNote.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", "");
                        hashMap.put("userid", currentUser);
                        hashMap.put(Constants.companyKey, companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_REJECTED, "User " + userFullName + " " + actionMsg + " Credit Note " + creditNote.getCreditNoteNumber(), requestParams, creditNote.getID());
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteServiceImpl.rejectPendingCreditNote:"+ex.getMessage(), ex);
        }
        return isRejected;
    }

    /**
     * @param
     * isEditToApprove : this param come from savecreditnote method 
     * Please Note accounting exception should only be thrown in case of if JE posting date dose not belongs to lockin period.As further code is written only to handle AccountingException in case of lockin period.
     */
    @Override
    public List approvePendingCreditNote(Map<String, Object> requestParams) throws ServiceException,AccountingException {
        List returnList = new ArrayList();
        try {

            String jeID = "";
            String msg="";
            String companyid = (String) requestParams.get(Constants.companyKey);
            String userid = (String) requestParams.get("userid");
            String billid = (String) requestParams.get(Constants.billid);
            String remark = (String) requestParams.get("remark");
            String userFullName = (String) requestParams.get("userName");
            String baseUrl = (String) requestParams.get("baseUrl");
            double amount = (!StringUtil.isNullObject(requestParams.get("amount")) && !StringUtil.isNullOrEmpty(requestParams.get("amount").toString())) ? authHandler.round(Double.parseDouble(requestParams.get("amount").toString()), companyid):0 ;
            boolean isEditToApprove = (!StringUtil.isNullObject(requestParams.get("isEditToApprove")) && !StringUtil.isNullOrEmpty(requestParams.get("isEditToApprove").toString())) ? Boolean.parseBoolean(requestParams.get("isEditToApprove").toString()):false;
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), billid);
            CreditNote creditNote = (CreditNote) CQObj.getEntityList().get(0);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) returnObject.getEntityList().get(0);

            HashMap<String, Object> invApproveMap = new HashMap<String, Object>();
            int level = creditNote.getApprovestatuslevel();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", companyid);
            Object exPrefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"columnPref"}, paramMap);
            JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
            boolean isPostingDateCheck = false;
            if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                isPostingDateCheck = true;
            }

            String psotingDateStr = (String) requestParams.get("postingDate");
            DateFormat df = authHandler.getDateOnlyFormat();
            Date postingDate = null;
            if (!StringUtil.isNullOrEmpty(psotingDateStr)) {
                postingDate = df.parse(psotingDateStr);
            }

            invApproveMap.put(Constants.companyKey, companyid);
            invApproveMap.put("level", level);
            invApproveMap.put("totalAmount", String.valueOf(amount));
            invApproveMap.put("currentUser", userid);
            invApproveMap.put("fromCreate", false);
            invApproveMap.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
            invApproveMap.put(Constants.PAGE_URL, baseUrl);
            int approvedLevel = 0;
            String JENumber = "";
            String JEMsg = "";

            List approvedLevelList = approveCreditNote(creditNote, invApproveMap, true);
            approvedLevel = (Integer) approvedLevelList.get(0);
            jeID = creditNote.getJournalEntry().getID();

            if (approvedLevel == 11) {//when final 
                if (StringUtil.isNullOrEmpty(creditNote.getJournalEntry().getEntryNumber())) {
                    int isApproved = 0;
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                    JEFormatParams.put(Constants.companyKey, companyid);
                    JEFormatParams.put("isdefaultFormat", true);
                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
//                    String JENumBer = journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(requestParams, creditNote.getJournalEntry(), companyid, format.getID(), isApproved);
                    String JENumBer = "";
                    KwlReturnObject returnObj = journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(requestParams, creditNote.getJournalEntry(), companyid, format.getID(), isApproved);
                    if (returnObj.isSuccessFlag() && returnObj.getRecordTotalCount() > 0) {
                        JENumBer = (String) returnObj.getEntityList().get(0);
                    } else if (!returnObj.isSuccessFlag()) {
                        throw new AccountingException((String) returnObj.getEntityList().get(0));
                    }
                } else {
                    JSONObject jeJobj = new JSONObject();
                    HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
                    jeJobj.put("jeid", jeID);
                    jeJobj.put("comapnyid", companyid);
                    jeJobj.put("pendingapproval", 0);
                    if (isPostingDateCheck && postingDate!=null) {
                        jeJobj.put("entrydate", postingDate);
                    }
                    accJournalEntryobj.updateJournalEntry(jeJobj, details);
                }
                JENumber = " with JE No. " + creditNote.getJournalEntry().getEntryNumber();
                JEMsg = "<br/>" + "JE No : <b>" + creditNote.getJournalEntry().getEntryNumber() + "</b>";
            }

            if (!isEditToApprove && approvedLevel == 11 && creditNote.getCntype() == 1) {// If this condition true then we need to update invoice amount used in CN
                for (CreditNoteDetail detail : creditNote.getRows()) {
                    Invoice invObj = detail.getInvoice();
                    double returnAmountInInvoiceCurrecny = 0;
                    double returnAmountInBaseAmountDue = 0;
                    if (detail.getDiscount() != null) {
                        returnAmountInInvoiceCurrecny = detail.getDiscount().getAmountinInvCurrency();
                        double returnAmountInCNCurrency = detail.getDiscount().getDiscount();
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, returnAmountInCNCurrency, creditNote.getCurrency().getCurrencyID(), creditNote.getCreationDate(), creditNote.getJournalEntry().getExternalCurrencyRate());
                        returnAmountInBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                    }
                    KwlReturnObject invoiceResult = updateInvoiceAmountDueAndReturnResult(invObj, company, returnAmountInInvoiceCurrecny, returnAmountInBaseAmountDue);
                    if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                        Invoice inv = (Invoice) invoiceResult.getEntityList().get(0);
                        if ((inv.isIsOpeningBalenceInvoice() && inv.getOpeningBalanceAmountDue() == 0) || (inv.getInvoiceamountdue() == 0)) {
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            dataMap.put("amountduedate", detail.getInvoiceLinkDate());
                            accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                        }
                    }
                }
            }

            if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String creatormail = company.getCreator().getEmailID();
                String approvalpendingStatusmsg = "";
                String documentcreatoremail = (creditNote != null && creditNote.getCreatedby() != null) ? creditNote.getCreatedby().getEmailID() : "";
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                ArrayList<String> emailArray = new ArrayList<>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", level);
                qdDataMap.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
//                emailArray = commonFnControllerService.getUserApprovalEmail(qdDataMap);

                if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
                    emailArray.add(documentcreatoremail);
                }
                emailArray.add(creatormail);
                String[] emails = {};
                emails = emailArray.toArray(emails);
                if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                    String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                    emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                }
                if (creditNote.getApprovestatuslevel() < 11) {
                    qdDataMap.put("totalAmount", String.valueOf(amount));
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }

                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put("Number", creditNote.getCreditNoteNumber());
                mailParameters.put("userName", userFullName);
                mailParameters.put("emails", emails);
                mailParameters.put("sendorInfo", sendorInfo);
                mailParameters.put("moduleName",  Constants.CREDIT_NOTE);
                mailParameters.put("addresseeName", "All");
                mailParameters.put("companyid", company.getCompanyID());
                mailParameters.put("baseUrl", baseUrl);
                mailParameters.put("approvalstatuslevel", creditNote.getApprovestatuslevel());
                mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
                if (emails.length > 0) {
                    accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
                }
            }
            // Save Approval History
            if (approvedLevel != Constants.NoAuthorityToApprove) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("transtype", Constants.CREDIT_NOTE_APPROVAL);
                hashMap.put("transid", creditNote.getID());
                hashMap.put("approvallevel", creditNote.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
                hashMap.put("remark", remark);
                hashMap.put("userid", userid);
                hashMap.put(Constants.companyKey, companyid);
                accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                // Audit log entry
                String action = "Credit Note ";
                String auditaction = AuditAction.CREDIT_NOTE_APPROVED;
                auditTrailObj.insertAuditLog(auditaction, "User " + userFullName + " has Approved a " + action + creditNote.getCreditNoteNumber() + JENumber + " at Level-" + creditNote.getApprovestatuslevel(), requestParams, creditNote.getID());
                msg = "Credit Note has been approved successfully " + " by " + userFullName + " at Level " + creditNote.getApprovestatuslevel() + "."+JEMsg;
            } ;
            returnList.add(msg);
        } catch(AccountingException ae){
            throw new AccountingException(ae.getMessage(), ae);
        }catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteServiceImpl.approvePendingCreditNote:"+ex.getMessage(), ex);
        }
        return returnList;
    }

    @Override
    public List approveCreditNote(CreditNote creditnote, HashMap<String, Object> cnApproveMap, boolean isMailApplicable) throws ServiceException {
        List returnList = new ArrayList();
        try {
            boolean hasAuthority = false;
            String companyid = "";

            List mailParamList = new ArrayList();
            int returnStatus;

            if (cnApproveMap.containsKey(Constants.companyKey) && cnApproveMap.get(Constants.companyKey) != null) {
                companyid = cnApproveMap.get(Constants.companyKey).toString();
            }
            String currentUser = "";
            if (cnApproveMap.containsKey("currentUser") && cnApproveMap.get("currentUser") != null) {
                currentUser = cnApproveMap.get("currentUser").toString();
            }
            int level = 0;
            if (cnApproveMap.containsKey("level") && cnApproveMap.get("level") != null) {
                level = Integer.parseInt(cnApproveMap.get("level").toString());
            }
            String amount = "";
            if (cnApproveMap.containsKey("totalAmount") && cnApproveMap.get("totalAmount") != null) {
                amount = cnApproveMap.get("totalAmount").toString();
            }
            boolean fromCreate = false;
            if (cnApproveMap.containsKey("fromCreate") && cnApproveMap.get("fromCreate") != null) {
                fromCreate = Boolean.parseBoolean(cnApproveMap.get("fromCreate").toString());
            }
            int moduleid = 0;
            if (cnApproveMap.containsKey("moduleid") && cnApproveMap.get("moduleid") != null) {
                moduleid = Integer.parseInt(cnApproveMap.get("moduleid").toString());
            }

            if (!fromCreate) {
                String thisUser = currentUser;
                KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
                User user = (User) userclass.getEntityList().get(0);

                if (AccountingManager.isCompanyAdmin(user)) {
                    hasAuthority = true;
                } else {
                    hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(cnApproveMap);
                }
            } else {
                hasAuthority = true;
            }

            if (hasAuthority) {
                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                int approvalStatus = 11;
                String cnNumber = creditnote.getCreditNoteNumber();
                String cnID = creditnote.getID();
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", level + 1);
                qdDataMap.put("moduleid", moduleid);
                KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                List<Object[]> list=flowresult.getEntityList();
                String fromName = "User";
                fromName = creditnote.getCreatedby().getFirstName().concat(" ").concat(creditnote.getCreatedby().getLastName());
                /**
                 * parameters required for sending mail
                 */
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put(Constants.companyid, companyid);
                mailParameters.put(Constants.prNumber, cnNumber);
                mailParameters.put(Constants.fromName, fromName);
                mailParameters.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                mailParameters.put(Constants.createdBy, creditnote.getCreatedby().getUserID());
                if (cnApproveMap.containsKey(Constants.PAGE_URL)) {
                    mailParameters.put(Constants.PAGE_URL, (String) cnApproveMap.get(Constants.PAGE_URL));
                }
                for (Object[] row:list) {
                    mailParameters.put(Constants.ruleid, row[0].toString());
                    HashMap<String, Object> recMap = new HashMap();
                    String rule = "";
                    if (row[2] != null) {
                        rule = row[2].toString();
                    }
                    rule = rule.replaceAll("[$$]+", amount);
                    if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                        // send emails
                        boolean hasApprover = Boolean.parseBoolean(row[3].toString());
                        mailParameters.put(Constants.hasApprover, hasApprover);
                        mailParameters.put("level", level+1);
                        if (isMailApplicable) {
                            sendMailToApprover(mailParameters);
                            approvalStatus = level + 1;
                        } else {
                            approvalStatus = level + 1;
                            recMap.put("ruleid", row[0].toString());
                            recMap.put("fromName", fromName);
                            recMap.put("hasApprover", hasApprover);
                            mailParamList.add(recMap);
                        }
                    }
                }
                accCreditNoteDAOobj.approvePendingCreditNote(cnID, companyid, approvalStatus);
                returnStatus = approvalStatus;
            } else {
                returnStatus = Constants.NoAuthorityToApprove; //if not have approval permission then return one fix value like 999
            }
            returnList.add(returnStatus);
            returnList.add(mailParamList);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return returnList;
    }

    @Override
    /**
     * @param mailParameters (String companyid, String ruleId, String documentNumber, String fromName, boolean hasApprover, int moduleid,String createdby, String PAGE_URL)
     * @throws ServiceException
     * @throws MessagingException
     */
    public void sendMailToApprover(Map<String, Object> mailParameters) throws ServiceException, MessagingException{
        boolean hasApprover = false;
        int moduleid = 0;
        String createdby = "";
        int level=0;
        if(mailParameters.containsKey(Constants.createdBy)){
            createdby = (String) mailParameters.get(Constants.createdBy);
        }
        if(mailParameters.containsKey(Constants.moduleid)){
            moduleid = (int) mailParameters.get(Constants.moduleid);
        }
        if(mailParameters.containsKey(Constants.hasApprover)){
            hasApprover = (boolean) mailParameters.get(Constants.hasApprover);
        }
         if(mailParameters.containsKey("level")){
            level = (int) mailParameters.get("level");
        }
        KwlReturnObject cap = null;
        if (mailParameters.containsKey(Constants.companyid)) {
            cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String) mailParameters.get(Constants.companyid));
        }
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
        String transactionName = "";
        String transactionNo = "";
        switch (moduleid) {
            case Constants.Acc_Credit_Note_ModuleId:
                transactionName = "Credit Note";
                transactionNo = "Credit Note Number";
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
                + "<p>%s has created " + transactionName + " %s and sent it to you for approval. at level "+(level)+"</p>"
                + "<p>Please review and approve it (" + transactionNo + ": %s).</p>"
                + "<p>Company Name:- %s</p>"
                + "<p>Please check on Url:- %s</p>"
                + "<p></p>"
                + "<p>Thanks</p>"
                + "<p>This is an auto generated email. Do not reply<br>";
        String requisitionApprovalPlainMsg = "Hi All,\n\n"
                + "%s has created " + transactionName + " %s and sent it to you for approval. at level "+(level)+"\n"
                + "Please review and approve it (" + transactionNo + ": %s).\n\n"
                + "Company Name:- %s \n"
                + "Please check on Url:- %s \n\n"
                + "Thanks\n\n"
                + "This is an auto generated email. Do not reply\n";
        try {
            if (hasApprover && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
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
                ArrayList<String> emailArray = new ArrayList<String>();
                String[] emails = {};

                String userDepartment=null;
                KwlReturnObject returnObjectRes=null;
                HashMap<String, Object> dataMap=new HashMap<String,Object>();
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
                     if(user!=null && !StringUtil.isNullObject(user.getDepartment())){
                       userDepartment= user.getDepartment();
                        dataMap.put("userdepartment", userDepartment);
                    }
                }

                KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(dataMap);

                if(userResult.getEntityList()!=null && userResult.getEntityList().size()<=0 && !StringUtil.isNullOrEmpty(userDepartment )){
                    dataMap.remove("userdepartment");
                    userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(dataMap);
                }

                List<Object[]> list = userResult.getEntityList();
                for (Object[] userrow:list) {
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
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public JSONObject checkInvoiceKnockedOffDuringCreditNotePending(Map<String, Object> requestParams) {
        JSONObject obj = new JSONObject();
        boolean issuccess = true;
        int approveType = 1;
        /*
         * ApproveType = 1 : Approve As normal way
         * ApproveType = 2 : Approve As otherwise
         * ApproveType = 3 : Approve after editing record
         */
        try{
            int totalInvoicesLinked =0;
            int totalNumberOfRecordHavingAmountDuezero=0;
            int totalNumberOfRecordHavingEqualtoAmountDueorGreater=0;
            boolean allInvoiceHasAmountDueZero = true;
            boolean isInvoiceUtilizedDuringCNPending = false;
            String billid = (String)requestParams.get(Constants.billid);//This is mandatory parameter
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), billid);
            CreditNote creditNote = (CreditNote) CQObj.getEntityList().get(0);
            for(CreditNoteDetail detail:creditNote.getRows()){
                totalInvoicesLinked++;
                double invKnockedOffAmount = 0;
                if(detail.getDiscount()!=null){
                    invKnockedOffAmount = detail.getDiscount().getDiscountValue();
                }
                double invoiceAmtDue = detail.getInvoice()!=null? detail.getInvoice().getInvoiceamountdue():0;
                if(invoiceAmtDue==0){
                    totalNumberOfRecordHavingAmountDuezero ++;
                }
                if(invoiceAmtDue>=invKnockedOffAmount){
                    totalNumberOfRecordHavingEqualtoAmountDueorGreater++;
                }
                if(invoiceAmtDue!=0){
                    allInvoiceHasAmountDueZero = false;
                }
                if(invoiceAmtDue>0 && invoiceAmtDue<invKnockedOffAmount){// It means invoice has been utilized in other transaction but not fully
                    isInvoiceUtilizedDuringCNPending =true;
                }
            }
            if(allInvoiceHasAmountDueZero){//when all invoices due are zero
               approveType= 2; //Approve As otherwise
            } else if(isInvoiceUtilizedDuringCNPending || (totalInvoicesLinked==(totalNumberOfRecordHavingAmountDuezero+totalNumberOfRecordHavingEqualtoAmountDueorGreater) && totalNumberOfRecordHavingAmountDuezero!=0)){//Second Or case: when multiple invoices linked but few of them become zero while pending CN 
               approveType= 3; //Approve after editing record
            } else {
                approveType= 1; //Approve As normal way
            }

        } catch (Exception ex){
            issuccess =false;
        } finally{
            try{
                obj.put("success", issuccess);
                obj.put("approvalType", approveType);
            } catch (JSONException ex){
                Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return obj;
    }

    @Override
    public List approvePendingCreditNoteAgainstInvoiceAsCNOtherwise(HashMap<String, Object> requestParams) throws ServiceException{
        /*
         * Here we have to convert CN agaisnt invoice to CNOtherwise and the
         * approving CN Step 1: Deleting invoie and CN linking infomation step
         * 2: Deleteing Forex and Revaluation JE step 3: Deleteing All CN
         * Details Step 4: Update cnamountdue, cntype, openflag Step 
         * 5: Approve CN
         */
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String billid = (String) requestParams.get(Constants.billid);
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), billid);
            CreditNote creditNote = (CreditNote) CQObj.getEntityList().get(0);
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) returnObject.getEntityList().get(0);

            HashMap<String, Object> credithm = new HashMap<String, Object>();
            credithm.put("cnid", billid);
            accCreditNoteDAOobj.deleteLinkingInformationOfCN(credithm);

            //Delete unrealised JE for Credit Note
            accJournalEntryobj.permanentDeleteJournalEntryDetailReval(billid, companyid);
            accJournalEntryobj.permanentDeleteJournalEntryReval(billid, companyid);

            // Delete foreign gain loss JE
            List resultJe = accCreditNoteDAOobj.getForeignGainLossJE(billid, companyid);
            if (resultJe.size() > 0 && resultJe.get(0) != null) {
                Iterator itr = resultJe.iterator();
                while (itr.hasNext()) {
                    Object object = itr.next();
                    String jeid = object != null ? object.toString() : "";
                    if (StringUtil.isNullOrEmpty(jeid)) {
                        KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(jeid, companyid);
                        List list1 = result.getEntityList();
                        Iterator itr1 = list1.iterator();
                        while (itr1.hasNext()) {
                            JournalEntryDetail jed = (JournalEntryDetail) itr1.next();
                            //Sagar - No need to revert entry from optimized table as entries are already reverted from calling main function in edit case.
                            result = accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
                        }
                        result = accJournalEntryobj.permanentDeleteJournalEntry(jeid, companyid);
                    }
                }
            }

            //Before deleting all CNDetails we need CNdetails at sno -1
            CreditNoteDetail noteDetail = new CreditNoteDetail();
            for (CreditNoteDetail cndetails : creditNote.getRows()) {
                if (cndetails.getSrno() == 1) {//CreditNoteDetail at serial number one has totaljedid which get used 
                    noteDetail.setSrno(1);
                    noteDetail.setTotalJED(cndetails.getTotalJED());
                    noteDetail.setGstJED(cndetails.getGstJED());
                    noteDetail.setMemo(cndetails.getMemo());
                    break;
                }
            }
            String CreditNoteDetailID = StringUtil.generateUUID();
            noteDetail.setID(CreditNoteDetailID);
            noteDetail.setCreditNote(creditNote);
            noteDetail.setTotalDiscount(0.00);
            noteDetail.setCompany(company);

            //Deleting All CreditNoteDetails
            accCreditNoteDAOobj.deleteCreditNoteDetails(billid, companyid);

            //Update CN
            Set<CreditNoteDetail> newcndetails = new HashSet<CreditNoteDetail>();
            newcndetails.add(noteDetail);
            credithm.put("cndetails", newcndetails);
            credithm.put("cnamountdue", creditNote.getCnamount());//updating amount due with amount
            credithm.put("openflag", true);
            credithm.put(Constants.cntype, 2);
            credithm.put("otherwise", true);
            KwlReturnObject result1 = accCreditNoteDAOobj.updateCreditNote(credithm);

            //Finally Approving CN
            list=approvePendingCreditNote(requestParams);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteServiceImpl.approvePendingCreditNote:"+ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public boolean isNoteLinkedWithPayment(String noteId) {
        boolean isNoteLinkedWithPayment = false;
        try {
            KwlReturnObject result = accCreditNoteDAOobj.getMakePaymentIdLinkedWithCreditNote(noteId);
            List list = result.getEntityList();
            Iterator it = list.iterator();
            if (it.hasNext()) {
                isNoteLinkedWithPayment = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isNoteLinkedWithPayment;
    }

    @Override
    public boolean isNoteLinkedWithAdvancePayment(String noteId) {
        boolean isNoteLinkedWithPayment = false;
        try {
            KwlReturnObject result = accCreditNoteDAOobj.getAdvancePaymentIdLinkedWithCreditNote(noteId);
            List list = result.getEntityList();
            Iterator it = list.iterator();
            if (it.hasNext()) {
                isNoteLinkedWithPayment = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isNoteLinkedWithPayment;
    }

    @Override
    public boolean isCreditNotelinkedInDebitNote(String noteId,String companyId) {
        boolean isCreditNotelinkedInDebitNote = false;
        try {
            KwlReturnObject result = accCreditNoteDAOobj.getCreditNotelinkedInDebitNote(noteId,companyId);
            List list = result.getEntityList();
            BigInteger count = null;
            if (!list.isEmpty()) {
                count = (BigInteger) list.get(0);
                if (count.intValue() > 0) {
                    isCreditNotelinkedInDebitNote = true;
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isCreditNotelinkedInDebitNote;
    }

    public boolean isNoteLinkedWithInvoice(String noteId, String companyId) {
        boolean isNoteLinkedWithInvoice = false;
        try {
            KwlReturnObject result = accCreditNoteDAOobj.getInvoicesLinkedWithCreditNote(noteId, companyId);
            List list = result.getEntityList();
            Iterator it = list.iterator();
            if (it.hasNext()) {
                isNoteLinkedWithInvoice = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isNoteLinkedWithInvoice;
    }

    /*Request Dependency Removed*/
    @Override
    public List linkCreditNotewithoutRequest(JSONObject paramJobj, String creditNoteId, Boolean isInsertAudTrail,Map<String, Object> siamountsmap) throws ServiceException, SessionExpiredException, JSONException, AccountingException ,ParseException{
        List result = new ArrayList();
        List<Date> datelist = null;
            String companyid =paramJobj.getString(Constants.companyKey);
            DateFormat df=authHandler.getDateOnlyFormat();
        String cnid = "";
        int counter = 0;
        if (!StringUtil.isNullOrEmpty(creditNoteId)) {
            cnid = creditNoteId;
        } else {
                cnid = paramJobj.optString("cnid",null);
        }
        Date maxLinkingDate = null;
        boolean isNoteAlso = false;
        HashMap<String, Object> credithm = new HashMap<String, Object>();
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNoteAlso",null))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
            isNoteAlso = Boolean.parseBoolean(paramJobj.getString("isNoteAlso"));
        }
            String linkingdate = (String) paramJobj.optString("linkingdate",null);
        DateFormat dateformat = authHandler.getDateOnlyFormat();
        if (!StringUtil.isNullOrEmpty(linkingdate)) {
            try {
                maxLinkingDate = dateformat.parse(linkingdate);
            } catch (ParseException ex) {
                Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
            String entryNumber = paramJobj.optString("number",null);
            String amounts[] = paramJobj.optString("amounts","").split(",");
            String invoiceDetails = paramJobj.optString("invoicedetails",null);
        boolean isEdit = paramJobj.optBoolean("isEdit");
        JSONArray jArr = new JSONArray(invoiceDetails);
        Map<String, Object> counterMap = new HashMap<>();
        for (int k = 0; k < jArr.length(); k++) {
            JSONObject jobj = jArr.getJSONObject(k);

            if (StringUtil.isNullOrEmpty(jobj.getString(Constants.billid))) {
                continue;
            }

            double usedcnamount = 0d;
            double originalinvoiceamount = 0d;
            double typeFigure = 0d;
            int typeOfFigure = 1;
            if (!StringUtil.isNullOrEmpty(amounts[k])) {
                usedcnamount = Double.parseDouble((String) amounts[k]);
            } else {
                usedcnamount = 0;
            }
            KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
            CreditNote creditNote = (CreditNote) cnObj.getEntityList().get(0);
            /**
             * Need to handle edit case for SR with CN document.
             */
            if (isEdit && isNoteAlso) {
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid")) && siamountsmap.containsKey(jobj.getString("billid"))) {
                    //below code is for : In edit case, previous linking of invoice is deleted so amoundue should also restore.
                    originalinvoiceamount = (double) siamountsmap.get(jobj.optString("billid"));
                        usedcnamount = creditNote!=null?creditNote.getCnamount():0;
                }
                credithm.put("dnid", cnid);
                //Delete previous linking between CN and SI
                accCreditNoteDAOobj.deleteLinkingInformationOfCN(credithm);
            }
            if (usedcnamount == 0) {
                continue;
            }
            if(!StringUtil.isNullOrEmpty(jobj.optString("typeFigure",null))){
                typeFigure = jobj.optDouble("typeFigure",0.0);
            }
            else if(isNoteAlso&&!StringUtil.isNullOrEmpty(paramJobj.optString("linkNumber",null))){
               typeFigure = jobj.optDouble("typeFigure",usedcnamount);  
            }
            if(!StringUtil.isNullOrEmpty(jobj.optString("typeOfFigure",null))){
                typeOfFigure = jobj.optInt("typeOfFigure",1);
            }
            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString(Constants.billid));
            Invoice invObj = (Invoice) grresult.getEntityList().get(0);

            grresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) grresult.getEntityList().get(0);

            Set<CreditNoteDetail> newcndetails = new HashSet<CreditNoteDetail>();
            double cnamountdue = creditNote.getCnamountdue();
            if (!creditNote.isOpenflag() || cnamountdue <= 0) {
                throw new AccountingException(messageSource.getMessage("acc.field.CreditNotehasbeenalreadyutilized.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }
//                /**
//                 * Restore the amount due of invoice in case edit SR with CN.
//                 */
//                if (creditNote!=null && isEdit && isNoteAlso) {
//                    double amountdiff = usedcnamount-creditNote.getCnamount();
//                    invObj.setInvoiceamountdue(originalinvoiceamount+amountdiff);
//                    invObj.setInvoiceAmountDueInBase(originalinvoiceamount+amountdiff);
//                }
            double amountReceived = usedcnamount;           //amount of DN 
            double amountReceivedConverted = usedcnamount;
            double adjustedRate = 1;
            double exchangeRateforTransaction = jobj.optDouble("exchangeratefortransaction", 1.0);

            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(invObj.getCurrency().getCurrencyID()) && !invObj.getCurrency().getCurrencyID().equals(creditNote.getCurrency().getCurrencyID())) {
                // adjusted exchange rate used to handle case like ERP-34884
                adjustedRate = exchangeRateforTransaction;
                if (jobj.optDouble("amountdue", 0) != 0 && jobj.optDouble("amountDueOriginal", 0) != 0) {
                    adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0);
                }
                amountReceivedConverted = amountReceived / adjustedRate;
                amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
            } else {
                amountReceivedConverted = authHandler.round(amountReceived, companyid);
            }
            Date maxDate = null;
            CreditNoteDetail cndetailObj = new CreditNoteDetail();
            if (creditNote.getCntype() != 3 && creditNote.isOpenflag() && creditNote.getCnamount() == creditNote.getCnamountdue()) {//If CN is used for first time.
                Set<CreditNoteDetail> cndetails = (Set<CreditNoteDetail>) creditNote.getRows();
                Iterator itr = cndetails.iterator();
                while (itr.hasNext()) {
                    cndetailObj = (CreditNoteDetail) itr.next();

                    if (invObj != null) {
                        cndetailObj.setInvoice(invObj);
                        /*
                         * code to save linking date of CN and Invoice. This
                         * linking date used while calculating due amount of CN
                         * in Aged/SOA report
                         */
                        if (maxLinkingDate != null) {
                            maxDate = maxLinkingDate;
                        } else {
                            Date linkingDate = null;
                            try {
                                linkingDate = df.parse(df.format(new Date())); //formatting and parsing again with dateonlyformmater for removing time 
                            } catch (ParseException ex) {
                                linkingDate = new Date();
                            }
                            Date invDate = invObj.getCreationDate();
                            Date cnDate = creditNote.getCreationDate();
                            datelist = new ArrayList<Date>();
                            datelist.add(linkingDate);
                            datelist.add(invDate);
                            datelist.add(cnDate);
                            Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                            maxDate = datelist.get(datelist.size() - 1);
                        }
                        cndetailObj.setInvoiceLinkDate(maxDate);
                    }
                    //change
                    double invoiceOriginalAmt = 0d;
                    if (!invObj.isNormalInvoice() && invObj.isIsOpeningBalenceInvoice()) {
                        invoiceOriginalAmt = invObj.getOriginalOpeningBalanceAmount();
                    } else {
                        invoiceOriginalAmt = invObj.getCustomerEntry().getAmount();
                    }
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", usedcnamount);         //amount in DN currency
                    discjson.put("amountinInvCurrency", amountReceivedConverted);
                    discjson.put("inpercent", false);
                    //        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, cnamount, currencyid, null, externalCurrencyRate);
                    discjson.put("originalamount", invoiceOriginalAmt);//(Double) bAmt.getEntityList().get(0));
                    discjson.put(Constants.companyKey, companyid);
                    discjson.put("typeOfFigure", typeOfFigure);
                    discjson.put("typeFigure", typeFigure);
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    cndetailObj.setDiscount(discount);
                    cndetailObj.setExchangeRateForTransaction(exchangeRateforTransaction);
                    newcndetails.add(cndetailObj);
                }
            } else {
                Set<CreditNoteDetail> cndetails = (Set<CreditNoteDetail>) creditNote.getRows();
                Iterator itr = cndetails.iterator();
                int i = 0;
                while (itr.hasNext()) {
                    cndetailObj = (CreditNoteDetail) itr.next();
                    newcndetails.add(cndetailObj);
                    i++;
                }

                cndetailObj = new CreditNoteDetail();

                if (invObj != null) {
                    cndetailObj.setInvoice(invObj);
                    /*
                     * code to save linking date of CN and Invoice. This linking
                     * date used while calculating due amount of CN in Aged/SOA
                     * report
                     */
                    if (maxLinkingDate != null) {
                        maxDate = maxLinkingDate;
                    } else {
                        Date linkingDate = null;
                        try {
                            linkingDate = df.parse(df.format(new Date())); //formatting and parsing again with dateonlyformmater for removing time 
                        } catch (ParseException ex) {
                            linkingDate = new Date();
                        }
                        Date invDate = invObj.getCreationDate();
                        Date cnDate = creditNote.getCreationDate();
                        datelist = new ArrayList<Date>();
                        datelist.add(linkingDate);
                        datelist.add(invDate);
                        datelist.add(cnDate);
                        Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                        maxDate = datelist.get(datelist.size() - 1);
                    }
                    cndetailObj.setInvoiceLinkDate(maxDate);
                }

                cndetailObj.setSrno(i + 1);
                cndetailObj.setTotalDiscount(0.00);
                cndetailObj.setCompany(company);
                cndetailObj.setMemo("");
                cndetailObj.setCreditNote(creditNote);
                cndetailObj.setID(UUID.randomUUID().toString());

                double invoiceOriginalAmt = 0d;
                if (invObj.isNormalInvoice()) {
                    invoiceOriginalAmt = invObj.getCustomerEntry().getAmount();
                } else {// for only opening balance Invoices
                    invoiceOriginalAmt = invObj.getOriginalOpeningBalanceAmount();
                }

                JSONObject discjson = new JSONObject();
                discjson.put("discount", usedcnamount);
                discjson.put("amountinInvCurrency", amountReceivedConverted);        //amount in DN currency
                discjson.put("inpercent", false);
                discjson.put("originalamount", invoiceOriginalAmt);//(Double) bAmt.getEntityList().get(0));
                discjson.put(Constants.companyKey, companyid);
                discjson.put("typeOfFigure", typeOfFigure);
                discjson.put("typeFigure", typeFigure);
                KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                Discount discount = (Discount) dscresult.getEntityList().get(0);
                cndetailObj.setDiscount(discount);
                cndetailObj.setExchangeRateForTransaction(exchangeRateforTransaction);
                newcndetails.add(cndetailObj);
            }

            double amountDue = cnamountdue - usedcnamount;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
                requestParams.put(Constants.companyKey,paramJobj.getString(Constants.companyKey));
                requestParams.put(Constants.globalCurrencyKey,paramJobj.getString(Constants.globalCurrencyKey));
            double externalCurrencyRate = 1d;
            boolean isopeningBalanceCN = creditNote.isIsOpeningBalenceCN();
            Date cnCreationDate = null;
            cnCreationDate = creditNote.getCreationDate();
            if (!creditNote.isNormalCN() && creditNote.isIsOpeningBalenceCN()) {
                externalCurrencyRate = creditNote.getExchangeRateForOpeningTransaction();
            } else {
                externalCurrencyRate = creditNote.getJournalEntry().getExternalCurrencyRate();
            }
            String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
            if (isopeningBalanceCN && creditNote.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
            }
            double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

            credithm.put("cndetails", newcndetails);
            credithm.put("cnid", creditNote.getID());
            credithm.put("cnamountdue", amountDue);
            credithm.put("openingBalanceAmountDue", amountDue);
            credithm.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);
            credithm.put("openflag", (cnamountdue - usedcnamount) <= 0 ? false : true);

                if (paramJobj.optString("entrynumber",null) != null) {
                credithm.put("entrynumber", paramJobj.getString("entrynumber"));
            }
                if (paramJobj.optString("autogenerated",null) != null) {
                credithm.put("autogenerated", paramJobj.getString("autogenerated"));

            }
                if (paramJobj.optString("seqformat",null) != null) {
                credithm.put(Constants.SEQFORMAT, paramJobj.getString("seqformat"));

            }
                if (paramJobj.optString("seqnumber",null) != null) {
                credithm.put(Constants.SEQNUMBER, paramJobj.getString("seqnumber"));

            }
            if (paramJobj.optString(Constants.DATEPREFIX,null) != null) {
                credithm.put(Constants.DATEPREFIX, paramJobj.getString(Constants.DATEPREFIX));
            }
            if (paramJobj.optString(Constants.DATESUFFIX,null) != null) {
                credithm.put(Constants.DATESUFFIX, paramJobj.getString(Constants.DATESUFFIX));
            }
	    if (paramJobj.optString(Constants.DATEAFTERPREFIX,null) != null) {  //SDP-14953
                credithm.put(Constants.DATEAFTERPREFIX, paramJobj.optString(Constants.DATEAFTERPREFIX));
            }

            KwlReturnObject result1 = accCreditNoteDAOobj.updateCreditNote(credithm);

            // Update Invoice base amount due. We have to consider Invoice currency rate to calculate.
            externalCurrencyRate = 1d;
            boolean isopeningBalanceINV = invObj.isIsOpeningBalenceInvoice();
            Date noteCreationDate = null;
            noteCreationDate = creditNote.getCreationDate();
            externalCurrencyRate = isopeningBalanceCN ? creditNote.getExchangeRateForOpeningTransaction() : creditNote.getJournalEntry().getExternalCurrencyRate();
            fromcurrencyid = creditNote.getCurrency().getCurrencyID();
            if (isopeningBalanceINV && invObj.isConversionRateFromCurrencyToBase()) {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
            }
            totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

            /*
             * Store the date on which the amount due has been set to zero
             * If Approvestatuslevel is not equall to 11 it means CN is going for pending approval. In this case we does not need to update invoice due amount. It will get updated at final approval
             */

            if (creditNote.getApprovestatuslevel() == 11) {
                KwlReturnObject invoiceResult = updateInvoiceAmountDueAndReturnResult(invObj, company, amountReceivedConverted, totalBaseAmountDue);
                if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                    Invoice inv = (Invoice) invoiceResult.getEntityList().get(0);
                    if (inv.isIsOpeningBalenceInvoice() && inv.getOpeningBalanceAmountDue() == 0) {
                        try {
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            if (creditNote.isIsOpeningBalenceCN() && creditNote != null && creditNote.getCreationDate() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                            } else if (!creditNote.isIsOpeningBalenceCN() && creditNote != null && creditNote.getJournalEntry() != null && creditNote.getJournalEntry().getCreatedOn() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                            }
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    } else if (inv.getInvoiceamountdue() == 0) {
                        try {
//                            DateFormat df = authHandler.getDateFormatter(request);
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            if (creditNote.isIsOpeningBalenceCN() && creditNote != null && creditNote.getCreationDate() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                            } else if (!creditNote.isIsOpeningBalenceCN() && creditNote != null && creditNote.getJournalEntry() != null && creditNote.getJournalEntry().getCreatedOn() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                            }
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    }
                }
            }

            /*
             * Updating entry in Audit Trial while unlinking transaction
             * through Editing
             */
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            if (isInsertAudTrail) {
                auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_CREATED, "User " + paramJobj.optString(Constants.userfullname, null) + " has linked Credit Note " + creditNote.getCreditNoteNumber() + " with Customer Invoice " + invObj.getInvoiceNumber() + ".", auditRequestParams, creditNote.getID());
            }

            /*
             * Start gains/loss calculation Calculate Gains/Loss if Invoice
             * exchange rate changed at the time of linking with CN
             */
            String creditid = paramJobj.optString("noteid",null);
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            if (preferences.getForeignexchange() == null) {
                throw new AccountingException(messageSource.getMessage("acc.common.forex", null,  Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }
            if (isopeningBalanceCN && creditNote.isConversionRateFromCurrencyToBase()) {
                externalCurrencyRate = 1 / externalCurrencyRate;
                externalCurrencyRate = externalCurrencyRate;
            }
            Map<String,Object> mapForForexGainLoss = new HashMap<>();
                mapForForexGainLoss.put("cn",creditNote);
                mapForForexGainLoss.put("invoice",invObj);
                mapForForexGainLoss.put("basecurreny",paramJobj.getString(Constants.globalCurrencyKey));
                mapForForexGainLoss.put(Constants.companyKey,paramJobj.getString(Constants.companyKey));
                mapForForexGainLoss.put(Constants.creationdate,creditNote.getCreationDate());
                mapForForexGainLoss.put("exchangeratefortransaction",exchangeRateforTransaction);
                mapForForexGainLoss.put("recinvamount",usedcnamount);
                mapForForexGainLoss.put("externalcurrencyrate",externalCurrencyRate);
                mapForForexGainLoss.put(Constants.RES_DATEFORMAT,authHandler.getDateOnlyFormat());
            double amountDiff = getForexGainLossForCreditNote(mapForForexGainLoss);
            if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                boolean rateDecreased = false;
                if (amountDiff < 0) {
                    rateDecreased = true;
                }
                JournalEntry journalEntry = null;
                Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
                String jeentryNumber = null;
                boolean jeautogenflag = false;
                String jeSeqFormatId = "";
                String datePrefix = "";
                String dateafterPrefix = "";
                String dateSuffix = "";
                Date entryDate = null;
                if (maxLinkingDate != null) {
                    entryDate = new Date(maxLinkingDate.getTime());
                } else {
                    entryDate = new Date();
                }
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put(Constants.companyKey, companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                    int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                    String nextAutoNUmber=(String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNUmber.replaceAll(action, number);
                    jeentryNumber = nextAutoNUmber.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, number);
                    jeDataMap.put(Constants.DATEPREFIX, datePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, dateSuffix);
                }
                    jeDataMap.put("entrydate",dateformat.parse(linkingdate));
                jeDataMap.put(Constants.companyKey, companyid);
                jeDataMap.put("memo", "Exchange Gains/Loss posted against Advance Receipt '" + creditNote.getCreditNoteNumber() + "' linked to Invoice '" + invObj.getInvoiceNumber() + "'");
                jeDataMap.put(Constants.currencyKey, creditNote.getCurrency().getCurrencyID());
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeDataMap.put("isexchangegainslossje", true);
                    jeDataMap.put("transactionId",creditNote.getID() );
                jeDataMap.put("transactionModuleid", Constants.Acc_Credit_Note_ModuleId);
                journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                boolean isDebit = rateDecreased ? true : false;
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", 1);
                jedjson.put(Constants.companyKey, companyid);
                jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                jedjson.put("accountid", preferences.getForeignexchange().getID());
                jedjson.put("debit", isDebit);
                jedjson.put("jeid", journalEntry.getID());
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                Set<JournalEntryDetail> detail = new HashSet();
                detail.add(jed);

                jedjson = new JSONObject();
                jedjson.put("srno", 2);
                jedjson.put(Constants.companyKey, companyid);
                jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                jedjson.put("accountid", invObj.getAccount().getID());
                jedjson.put("debit", !isDebit);
                jedjson.put("jeid", journalEntry.getID());
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                detail.add(jed);
                journalEntry.setDetails(detail);
                accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                // Save link JE iformation in DN details
                cndetailObj.setLinkedGainLossJE(journalEntry.getID());
                newcndetails.add(cndetailObj);
                credithm.put("cndetails", newcndetails);
                KwlReturnObject result2 = accCreditNoteDAOobj.updateCreditNote(credithm);
                counter++;
            }
                // End Gains/Loss Calculation

            //JE For Receipt which is of Opening Type
                if(counterMap.containsKey("counter")){
                   counter=(Integer)counterMap.get("counter");
            }
            counterMap.put("counter", counter);
            if (creditNote != null && (creditNote.isIsOpeningBalenceCN() || creditNote.isOtherwise())) {
                String basecurrency = paramJobj.getString(Constants.globalCurrencyKey);
                double finalAmountReval = ReevalJournalEntryForCreditNote(paramJobj, creditNote, amountReceived, exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                    /**
                         * added transactionID and transactionModuleID to
                         * Realised JE.
                     */
                    counterMap.put("transactionModuleid", creditNote.isIsOpeningBalenceCN() ? (creditNote.iscNForCustomer() ? Constants.Acc_opening_Customer_CreditNote : Constants.Acc_opening_Vendor_CreditNote) : Constants.Acc_Credit_Note_ModuleId);
                    counterMap.put("transactionId", creditNote.getID());
                        String revaljeid = PostJEFORReevaluation(paramJobj, -(finalAmountReval),companyid, preferences, basecurrency, cndetailObj.getRevalJeId(),counterMap);
                    cndetailObj.setRevalJeId(revaljeid);
                }
            }
            //JE For Debit which is Linked to Receipt
            if (invObj != null) {
                double finalAmountReval = ReevalJournalEntryForInvoice(paramJobj, invObj, amountReceived, exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                        String basecurrency =paramJobj.getString(Constants.globalCurrencyKey);
                    /**
                         * added transactionID and transactionModuleID to
                         * Realised JE.
                     */
                    counterMap.put("transactionModuleid", invObj.isIsOpeningBalenceInvoice() ? Constants.Acc_opening_Sales_Invoice : Constants.Acc_Invoice_ModuleId);
                    counterMap.put("transactionId", invObj.getID());
                        String revaljeid = PostJEFORReevaluation(paramJobj, finalAmountReval, companyid, preferences, basecurrency, cndetailObj.getRevalJeIdInvoice(),counterMap);
                    cndetailObj.setRevalJeIdInvoice(revaljeid);

                }
            }
            if (invObj != null) {

                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                requestParamsLinking.put("linkeddocid", cnid);
                requestParamsLinking.put("docid", invObj.getID());
                requestParamsLinking.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                requestParamsLinking.put("linkeddocno", entryNumber);
                requestParamsLinking.put("sourceflag", 0);
                KwlReturnObject result3 = accInvoiceDAOObj.saveInvoiceLinking(requestParamsLinking);


                /*
                 * saving linking informaion of Purchase Invoice while linking
                 * with DebitNote
                 */

                requestParamsLinking.put("linkeddocid", invObj.getID());
                requestParamsLinking.put("docid", cnid);
                requestParamsLinking.put("moduleid", Constants.Acc_Invoice_ModuleId);
                requestParamsLinking.put("linkeddocno", invObj.getInvoiceNumber());
                requestParamsLinking.put("sourceflag", 1);
                result1 = accCreditNoteDAOobj.saveCreditNoteLinking(requestParamsLinking);
            }
        }
        return result;
    }

    /*
     * Revalaution Entery for Invoices
     */
    public double ReevalJournalEntryForInvoice(JSONObject paramJobj, Invoice invoice, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException, JSONException {
        double finalAmountReval = 0;
        try {
            String basecurrency = paramJobj.getString(Constants.globalCurrencyKey);
            double ratio = 0;
            double amountReval = 0;
            String revalId = null;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkInvoiceAmount;
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put(Constants.RES_DATEFORMAT, authHandler.getDateOnlyFormat());
            Date creationDate = invoice.getCreationDate();
            boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
            tranDate = invoice.getCreationDate();
            if (!invoice.isNormalInvoice()) {
                exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
//                tranDate = invoice.getJournalEntry().getEntryDate();
            }
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", invoice.getID());
            invoiceId.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
            invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            //Checking the document entery in revalution history if any for current rate
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (revalueationHistory != null) {
                exchangeRateReval = revalueationHistory.getEvalrate();
            }

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (invoice.getCurrency() != null) {
                currid = invoice.getCurrency().getCurrencyID();
            }
            //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter(Constants.creationdate)));
            if (revalueationHistory == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

            double newrate = (Double) bAmt.getEntityList().get(0);
            ratio = oldrate - newrate;
            if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                exchangeratefortransaction = newrate;
            }
            double amountdueNew = amountdue / exchangeratefortransaction;
            amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
            amountReval = ratio * amountdueNew;
            finalAmountReval = finalAmountReval + amountReval;
        } catch (SessionExpiredException | ServiceException e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }

    /*
     * Revalaution Entery for Credit Note with Request Dependency Removed
     */
    public double ReevalJournalEntryForCreditNote(JSONObject paramJobj, CreditNote creditNote, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException, JSONException {

        double finalAmountReval = 0;
        try {
            String basecurrency = paramJobj.getString(Constants.globalCurrencyKey);
            double ratio = 0;
            double amountReval = 0;
            String revalId = null;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkInvoiceAmount;
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put(Constants.RES_DATEFORMAT, authHandler.getDateOnlyFormat());
            Date creationDate = creditNote.getCreationDate();
            boolean isopeningBalanceInvoice = creditNote.isIsOpeningBalenceCN();
            tranDate = creditNote.getCreationDate();
            if (!creditNote.isNormalCN()) {
                exchangeRate = creditNote.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = creditNote.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
//                tranDate = creditNote.getJournalEntry().getEntryDate();
            }
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", creditNote.getID());
            invoiceId.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
            invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            //Checking the document entery in revalution history if any for current rate
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (revalueationHistory != null) {
                exchangeRateReval = revalueationHistory.getEvalrate();
            }

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (creditNote.getCurrency() != null) {
                currid = creditNote.getCurrency().getCurrencyID();
            }
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            if (revalueationHistory == null && isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

            double newrate = (Double) bAmt.getEntityList().get(0);
            ratio = oldrate - newrate;
            if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                exchangeratefortransaction = newrate;
            }
            double amountdueNew = amountdue / exchangeratefortransaction;
            amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
            amountReval = ratio * amountdueNew;
            finalAmountReval = finalAmountReval + amountReval;
        } catch (SessionExpiredException | ServiceException e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }

    /*Request Dependency Removed*/
      public String PostJEFORReevaluation(JSONObject paramJobj, double finalAmountReval, String companyid, CompanyAccountPreferences preferences, String basecurrency, String oldRevaluationJE, Map<String,Object> dataMap) {
        String jeid = "";
        try {
            String jeentryNumber = "";
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            boolean jeautogenflag = false;
            DateFormat df = authHandler.getDateOnlyFormat();
            /**
             * added Link Date to Realised JE. while link Otherwise CN/DN to
             * Reevaluated Invoice. Use 'linkingdateString'
             */
            String creationDate = !StringUtil.isNullOrEmpty(paramJobj.optString("linkingdateString","")) ? paramJobj.optString("linkingdateString","") : paramJobj.optString("creationdate");
            Date entryDate = StringUtil.isNullOrEmpty(creationDate) ? new Date() : df.parse(creationDate);
            int counter=(Integer)dataMap.get("counter");
            synchronized (this) {
                HashMap<String, Object> JEFormatParams = new HashMap<>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                JEFormatParams.put(Constants.companyKey, companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                jeautogenflag = true;
                if (StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                    String nextAutoNoTemp=(String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                    datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeIntegerPart = String.valueOf(sequence);
                    jeSeqFormatId = format.getID();
                    counter++;
                    dataMap.put("counter", counter);
                } else if (!StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), oldRevaluationJE);
                    JournalEntry entry = (JournalEntry) result.getEntityList().get(0);
                    jeid = entry.getID();
                    jeentryNumber = entry.getEntryNumber();
                    jeSeqFormatId = entry.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(entry.getSeqnumber());
                    datePrefix=entry.getDatePreffixValue();
                    dateafterPrefix = entry.getDateAfterPreffixValue();
                    dateSuffix=entry.getDateSuffixValue();
                    result = accJournalEntryobj.deleteJEDtails(oldRevaluationJE, companyid);
                    result = accJournalEntryobj.deleteJE(oldRevaluationJE, companyid);
                }
            }
            boolean creditDebitFlag = true;
            if (finalAmountReval < 0) {
                finalAmountReval = -(finalAmountReval);
                creditDebitFlag = false;
            }

            Map<String, Object> jeDataMapReval = AccountingManager.getGlobalParamsJson(paramJobj);
            jeDataMapReval.put("entrynumber", jeentryNumber);
            jeDataMapReval.put("autogenerated", jeautogenflag);
            jeDataMapReval.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMapReval.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMapReval.put(Constants.DATEPREFIX, datePrefix);
            jeDataMapReval.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
            jeDataMapReval.put(Constants.DATESUFFIX, dateSuffix);
            jeDataMapReval.put("entrydate", entryDate);
            jeDataMapReval.put(Constants.companyKey, companyid);
            //jeDataMapReval.put("memo", "Realised Gain/Loss");
            jeDataMapReval.put(Constants.currencyKey, basecurrency);
            jeDataMapReval.put("isReval", 2);
            jeDataMapReval.put("transactionModuleid", dataMap.containsKey("transactionModuleid") ? dataMap.get("transactionModuleid") : 0);
            jeDataMapReval.put("transactionId", dataMap.get("transactionId"));
            Set jedetailsReval = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMapReval);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMapReval.put("jeid", jeid);
            JSONObject jedjsonreval = new JSONObject();
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put(Constants.companyKey, companyid);
            jedjsonreval.put("amount", finalAmountReval);//rateDecreased?(-1*amountDiff):
            jedjsonreval.put("accountid", preferences.getForeignexchange().getID());
            jedjsonreval.put("debit", creditDebitFlag ? true : false);
            jedjsonreval.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);
            /*
             * Featching Custom field/Dimension Data from Company prefrences.
             */
            String customfield = "";
            String lineleveldimensions = "";
            KwlReturnObject result = accJournalEntryobj.getRevaluationJECustomData(companyid);
            RevaluationJECustomData revaluationJECustomData = (result != null && result.getEntityList().size() > 0  && result.getEntityList().get(0) != null) ? (RevaluationJECustomData) result.getEntityList().get(0) : null;
            if (revaluationJECustomData != null) {
                customfield = revaluationJECustomData.getCustomfield();
                lineleveldimensions = revaluationJECustomData.getLineleveldimensions();
            }

            /*
             * Make dimensions entry
             */
            setDimensionForRevalJEDetail(lineleveldimensions, jed);
            String unrealised_accid = "";
            if (preferences.getUnrealisedgainloss() != null) {
                unrealised_accid = preferences.getUnrealisedgainloss().getID();
            } else {
                throw new AccountingException(messageSource.getMessage("acc.field.NoUnrealisedGain/Lossaccountfound", null,  Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }
            jedjsonreval = new JSONObject();
            jedjsonreval.put(Constants.companyKey, companyid);
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put("amount", finalAmountReval);
            jedjsonreval.put("accountid", unrealised_accid);
            jedjsonreval.put("debit", creditDebitFlag ? false : true);
            jedjsonreval.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);

            /*
             * Make dimensions entry
             */
            setDimensionForRevalJEDetail(lineleveldimensions, jed);
            jeDataMapReval.put("jedetails", jedetailsReval);
            jeDataMapReval.put("externalCurrencyRate", 0.0);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMapReval);
            /*
             * Make custom field entry
             */
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", jeid);
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    Map<String, Object> customjeDataMap = new HashMap<>();
                    customjeDataMap.put("accjecustomdataref", jeid);
                    customjeDataMap.put("jeid", jeid);
                    customjeDataMap.put("istemplate", journalEntry.getIstemplate());
                    customjeDataMap.put("isReval", journalEntry.getIsReval());
                    accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jeid;
    }

    /**
     * Description :This method is used to save Dimension For Reval JEDetail
     */
    public void setDimensionForRevalJEDetail(String lineleveldimensions,JournalEntryDetail jed){
        try{
            if (!StringUtil.isNullOrEmpty(lineleveldimensions)) {
                JSONArray jcustomarray = new JSONArray(lineleveldimensions);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", jed.getID());
                customrequestParams.put("recdetailId", jed.getID());
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", jed.getCompany().getCompanyID());
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    //jed.setAccJEDetailCustomData((AccJEDetailCustomData) hibernateTemplate.get(AccJEDetailCustomData.class, jed.getID()));
                    JSONObject jedjsonreval = new JSONObject();
                    jedjsonreval.put("accjedetailcustomdata", jed.getID());
                    jedjsonreval.put("jedid", jed.getID());
                    accJournalEntryobj.updateJournalEntryDetails(jedjsonreval);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Description :This method is used to save Credit Note
     *
     * @param paramJobj
     * @return JSONObject
     */
    @Override
    public JSONObject saveCreditNoteJSON(JSONObject paramJobj) {

        /* Varible declaration*/
        boolean issuccess = false;
        boolean isAccountingExe = false;
        boolean isTaxDeactivated = false;
        String msg = "";
        String creditNoteNumBer = "";
        String JENumBer = "";
        String jeSeqFormatId = "";
        String channelName = "";
        String sequenceformat = "";
        String creditNoteId = "";
        String entryNumber = "";
        String companyid = "";
        String auditMsg = "", auditID = "";
        int approvalStatusLevel = 11;
        int flag = 0;
        JSONObject jobj = new JSONObject();
        Company company=null;
        Map<String, Object> seqNumMap = new HashMap<String, Object>();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;//txnManager.getTransaction(def);
        String invoiceids = paramJobj.optString("invoiceids");
        String invoiceIdsArrary[] = invoiceids.split(",");
        try {
            /* Varible initialization*/
            channelName = "/CreditNoteReport/gridAutoRefresh";
            sequenceformat = paramJobj.optString("sequenceformat");
            entryNumber = paramJobj.optString("number");
            companyid = paramJobj.optString(Constants.companyKey);
            int cntype = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.cntype, null)) ? 1 : Integer.parseInt(paramJobj.optString(Constants.cntype));
            /*
             * For Malaysian Company while save credit note against vendor noteis as srid
             */
            if (cntype == 5 || cntype == Constants.CreditNoteForOvercharge) {
                creditNoteId = paramJobj.optString("srid");
            } else {
                creditNoteId = paramJobj.optString("noteid");
            }
            KwlReturnObject cncount = null;

            if (!StringUtil.isNullOrEmpty(creditNoteId)) {//Edit case checks duplicate
                cncount = accCreditNoteDAOobj.getCNFromNoteNoAndId(entryNumber, companyid, creditNoteId);
                if (cncount.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    isAccountingExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.CN.creditnoteno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
            } else {     // create new case checks duplicate
                cncount = accCreditNoteDAOobj.getCNFromNoteNo(entryNumber, companyid);
                if (cncount.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    isAccountingExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.CN.creditnoteno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
                //Check Deactivate Tax in New Transaction.
                if ((cntype != 5 || cntype != Constants.CreditNoteForOvercharge) && !fieldDataManagercntrl.isTaxActivated(paramJobj)) {
                    isTaxDeactivated = true;
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }
            }
            /*
             * Check squence format length for INDIA country
             */
            KwlReturnObject cmpny = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            company = (Company) cmpny.getEntityList().get(0);
            if (company.getCountry().getID().equals(String.valueOf(Constants.indian_country_id))) {
                if ((!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA"))) {
                    boolean seqformat_oldflg = StringUtil.getBoolean(paramJobj.optString("seqformat_oldflag"));
                    Date billDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("creationdate"));
                    seqNumMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_CREDITNOTE, sequenceformat, seqformat_oldflg, billDate);
                    String autoNum = (String) seqNumMap.get(Constants.AUTO_ENTRYNUMBER);
                    if (autoNum.length() > Constants.SequenceFormatMaxLength || !StringUtil.isSequenceFormatValid(autoNum)) {
                        throw new AccountingException(Constants.SequenceformatErrorMsg1 + autoNum + Constants.SequenceformatErrorMsg2 + Constants.SequenceFormatMaxLength + Constants.SequenceformatErrorMsg3);
                    }
                } else if (!StringUtil.isNullOrEmpty(sequenceformat) && sequenceformat.equals("NA") || !StringUtil.isSequenceFormatValid(entryNumber)) {
                    if ((!StringUtil.isNullOrEmpty(sequenceformat) && entryNumber.length() > Constants.SequenceFormatMaxLength) || !StringUtil.isSequenceFormatValid(entryNumber)) {
                        throw new AccountingException(Constants.SequenceformatErrorMsg1 + entryNumber + Constants.SequenceformatErrorMsg2 + Constants.SequenceFormatMaxLength + Constants.SequenceformatErrorMsg3);
                    }
                }
            }

            synchronized (this) {//Checks duplicate number for simultaneous transactions
                status = txnManager.getTransaction(def);
                if (sequenceformat.equals("NA")) {
                    KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Credit_Note_ModuleId);
                    if (resultInv.getRecordTotalCount() > 0) {
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.CN.selectedcreditnoteno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Credit_Note_ModuleId);
                    }
                }
                //ERP-36411
//                for (String invoiceId : invoiceIdsArrary) {
//                    KwlReturnObject resultInv1 = accPaymentDAOobj.getInvoiceInTemp(invoiceId, companyid, Constants.Acc_Invoice_ModuleId);
//                    if (resultInv1.getRecordTotalCount() > 0) {
//                        throw new AccountingException("Selected invoice is already in process, please try after sometime.");
//                    } else {
//                        accPaymentDAOobj.insertInvoiceOrCheque(invoiceId, companyid, Constants.Acc_Invoice_ModuleId, "");
//                    }
//                }

                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            List returnList = null;
            if (cntype == 5 || cntype == Constants.CreditNoteForOvercharge) {
                returnList = saveCreditNoteGst(paramJobj, jobj);
            } else {
                returnList = saveCreditNote(paramJobj, jobj);
            }

            JSONArray invoicedetails = null;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("invoicedetails", null))) {
                invoicedetails = new JSONArray(paramJobj.optString("invoicedetails"));
            }
            List mailParams = Collections.EMPTY_LIST;

            String oldJeId = (String) returnList.get(0);
            CreditNote creditnote = (CreditNote) returnList.get(1);
            if (returnList.get(2) != null) {//Approval Status Level
                approvalStatusLevel = Integer.parseInt(returnList.get(2).toString());
            }
            if (cntype != 5 && cntype != Constants.CreditNoteForOvercharge) {
                mailParams = (List) returnList.get(3);
            }
            creditNoteNumBer = creditnote.getCreditNoteNumber();
            JENumBer = creditnote.getJournalEntry().getEntryNumber();
            issuccess = true;
            txnManager.commit(status);
            status = null;
            TransactionStatus AutoNoStatus = null;
            String nextCNAutoNo = "";
            int nextAutoNoInt = 0;
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            boolean autogenerated = false;
            try {
                synchronized (this) {
                    /*
                     auto number save functionality
                     */
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("AutoNum_Tx");
                    if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {
                        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    } else {
                        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    }

                    AutoNoStatus = txnManager.getTransaction(def1);
                    boolean isFromOtherSource=paramJobj.optBoolean("isFromOtherSource", false);             //if this service is called from other source like import than the sequence number should not be generated instead it will take Receipt number from file
                    if (!sequenceformat.equals("NA") && StringUtil.isNullOrEmpty(creditNoteId)) {
                        boolean seqformat_oldflag = paramJobj.optBoolean("seqformat_oldflag", false);
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        if (isFromOtherSource) {
                            nextAutoNoInt = paramJobj.optInt(Constants.SEQNUMBER);
                            String autoNumber = paramJobj.optString("number");
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, autoNumber);//complete number
                            seqNumberMap.put(Constants.SEQNUMBER, nextAutoNoInt);//interger part 
                            seqNumberMap.put(Constants.DATEPREFIX, datePrefix);
                            seqNumberMap.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                            seqNumberMap.put(Constants.DATESUFFIX, dateSuffix);
                        } else if (seqformat_oldflag) {
                            nextCNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_CREDITNOTE, sequenceformat);
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextCNAutoNo);
                        } else {
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_CREDITNOTE, sequenceformat, seqformat_oldflag, creditnote.getCreationDate());
                            nextAutoNoInt = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                            datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        }
                        seqNumberMap.put(Constants.DOCUMENTID, creditnote.getID());
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        creditNoteNumBer = accCreditNoteDAOobj.updateCreditNoteEntryNumber(seqNumberMap);
                        autogenerated = true;
                    }

                    /*
                     * new and copy case when document going for pending
                     * approval. In this case does not need to give number to
                     * Journal entry it get assigned when document if finally approved.
                     */
                    if (StringUtil.isNullOrEmpty(oldJeId) && approvalStatusLevel == 11) {
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", "autojournalentry");
                        JEFormatParams.put("companyid", companyid);
                        JEFormatParams.put("isdefaultFormat", true);

                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        jeSeqFormatId = format.getID();
                        Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
//                        JENumBer = getJournalEntryModuleServiceobj().updateJEEntryNumberForNewJE(jeDataMap, creditnote.getJournalEntry(), companyid, jeSeqFormatId, creditnote.getJournalEntry().getPendingapproval());
                        KwlReturnObject returnObj = getJournalEntryModuleServiceobj().updateJEEntryNumberForNewJE(jeDataMap, creditnote.getJournalEntry(), companyid, jeSeqFormatId, creditnote.getJournalEntry().getPendingapproval());
                        if (returnObj.isSuccessFlag() && returnObj.getRecordTotalCount() > 0) {
                            JENumBer = (String) returnObj.getEntityList().get(0);
                        }
                    }
                    txnManager.commit(AutoNoStatus);
                    /*
                     Check if selected invoice already knock-off from another transaction,
                     If yes then remove already saved debit note in above block.
                     */
                    if (cntype != Constants.CreditNoteForOvercharge) {
                        for (int i = 0; i < invoiceIdsArrary.length; i++) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceIdsArrary[i]);
                            Invoice invoice = (Invoice) objItr.getEntityList().get(0);
                            if (invoice != null && invoicedetails != null) {
                                double invoiceAmoutDue = 0; // Opening transaction record
                                if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                                    invoiceAmoutDue = invoice.getOpeningBalanceAmountDue();
                                } else {
                                    invoiceAmoutDue = invoice.getInvoiceamountdue();
                                }
                                JSONObject invoiceJobj = invoicedetails.getJSONObject(i);
                                double amountReceived = invoiceJobj.getDouble("invamount");           //amount of DN 
                                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                                String currencyid = paramJobj.optString(Constants.currencyKey);
                                filterRequestParams.put(Constants.companyKey, companyid);
                                filterRequestParams.put(Constants.globalCurrencyKey, currencyid);
                                KwlReturnObject baseAmt = null;
                                if (!invoice.isIsOpeningBalenceInvoice()) {
                                    baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, invoiceAmoutDue, currencyid, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                                } else {
                                    baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, invoiceAmoutDue, currencyid, invoice.getCreationDate(), invoice.getExchangeRateForOpeningTransaction());
                                }
                                KwlReturnObject baseAmtRec = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, amountReceived, currencyid, creditnote.getCreationDate(), creditnote.getExternalCurrencyRate());
                                double invoiceAmoutDueBase = authHandler.round((Double) baseAmt.getEntityList().get(0), companyid);
                                double amountReceivedBase = authHandler.round((Double) baseAmtRec.getEntityList().get(0), companyid);
                                /**
                                 * I have comment this code due to some scenario
                                 * are interrupt. below condition for checks
                                 * simultaneous transaction,
                                 */
//                                if (invoiceAmoutDueBase < amountReceivedBase) { // Last Amount column should be compared with Amount Due column (SDP-9124)
//                                    flag = 1;
//                                    issuccess = false;
//                                    status = txnManager.getTransaction(def);
//                                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
//                                    requestParams.put("cnid", creditnote.getID());
//                                    requestParams.put("companyid", companyid);
//                                    requestParams.put("cnno", creditNoteNumBer);
//                                    accCreditNoteDAOobj.deleteCreditNotesPermanent(requestParams);
//                                    txnManager.commit(status);
//                                    status = null;
//                                    throw new AccountingException(messageSource.getMessage("acc.field.alreadyknock_off", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
//                                }
                            }
                        }
                    }

                    /*
                     If valid invoice details then update invoice amount due accordingly
                     */
                    status = txnManager.getTransaction(def);
                    paramJobj.put("entrynumber", creditNoteNumBer);
                    if (!sequenceformat.equals("NA") && StringUtil.isNullOrEmpty(creditNoteId)) {

                        paramJobj.put("autogenerated", autogenerated);
                        paramJobj.put("seqformat", sequenceformat);
                        paramJobj.put("seqnumber", nextAutoNoInt);
                        paramJobj.put(Constants.DATEPREFIX, datePrefix);
                        paramJobj.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                        paramJobj.put(Constants.DATESUFFIX, dateSuffix);
                    }
                    if (cntype == 1 || cntype == 3) {
                        linkCreditNote(paramJobj, creditnote.getID(), true);  // "true" flag is passed for inserting Audit Trial entry ( ERP-18558 )
                    } else if (cntype == Constants.CreditNoteForOvercharge) {
                        updateAmountDueCreditNoteOvercharge(paramJobj, creditnote.getID(), true);  
                    }
                    if (!StringUtil.isNullOrEmpty(creditNoteId)) {
                        auditID = AuditAction.CREDIT_NOTE_MODIFIED;
                        auditMsg = "updated";
                    } else {
                        auditID = AuditAction.CREDIT_NOTE_CREATED;
                        auditMsg = "added";
                    }
                    Map<String, Object> auditRequestParams = new HashMap<>();
                    auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                    auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                    auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                    if (approvalStatusLevel != 11) {//pending for approval case
                        String pendingforApproval = " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                        auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has " + auditMsg + " a Credit Note " + creditNoteNumBer + pendingforApproval, auditRequestParams, creditnote.getID());
                    } else {
                        auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has " + auditMsg + " a Credit Note " + creditNoteNumBer, auditRequestParams, creditnote.getID());
                    } 
                    txnManager.commit(status);
                    status = null;
                }
            } catch (ServiceException ex) {
                issuccess=false;
                throw ServiceException.FAILURE(ex.getMessage(), "", false);
            }  catch (Exception ex) {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
                deleteTemporaryInvoicesEntries(invoiceIdsArrary, companyid);
                /*
                 If valid invoice details then flag = 0 otherwise set as 1;
                 */
                if (AutoNoStatus != null && flag != 1) {
                    txnManager.rollback(AutoNoStatus);
                } else {
                    throw new AccountingException(ex.getMessage(), ex);
                    // }
                }
                Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            //=================Create Rounding JE After CN Save Start===============
            try {
                if (invoiceIdsArrary.length > 0) {//CN Against Sales Invoice
                    //After used of invoice in CN it is needed to check for Rounding JE
                    status = txnManager.getTransaction(def);
                    try {
                        if (!StringUtil.isNullOrEmpty(creditNoteId)) {
                            paramJobj.put("isEdit", true);
                        }
                        paramJobj.put("creditnotenumber", creditNoteNumBer);
                        postRoundingJEOnCreditNoteSave(paramJobj);
                        txnManager.commit(status);
                    } catch (JSONException | ServiceException | TransactionException ex) {
                        if (status != null) {
                            txnManager.rollback(status);
                        }
                        Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
            //=================Create Rounding JE After CN Save End===============

            if (approvalStatusLevel != 11) {//pending for approval case
                String creditnoteSaved = messageSource.getMessage("acc.creditN.save", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                String butPendingForApproval = " " + messageSource.getMessage("acc.field.butpendingforApproval", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                msg = creditnoteSaved + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + ": <b>" + creditNoteNumBer + "</b>.";
            } else {
                msg = messageSource.getMessage("acc.creditN.save", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + ": <b>" + creditNoteNumBer + ",</b>" + messageSource.getMessage("acc.field.JENo", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + ": <b>" + JENumBer + "</b>";   //"Credit Note has been saved successfully";
            }
            //txnManager.commit(status);

            status = txnManager.getTransaction(def);
            if (mailParams != null && !mailParams.isEmpty()) {
                /**
                 * parameters required for sending mail
                 */
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put(Constants.companyid, companyid);
                mailParameters.put(Constants.prNumber, creditNoteNumBer);
                mailParameters.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                mailParameters.put(Constants.createdBy, creditnote.getCreatedby().getUserID());
                mailParameters.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));
                mailParameters.put("level",creditnote.getApprovestatuslevel());

                Iterator itr = mailParams.iterator();
                while (itr.hasNext()) {
                    HashMap<String, Object> paramsMap = (HashMap<String, Object>) itr.next();
                    mailParameters.put(Constants.ruleid, (String) paramsMap.get("ruleid"));
                    mailParameters.put(Constants.fromName, (String) paramsMap.get("fromName"));
                    mailParameters.put(Constants.hasApprover, (Boolean) paramsMap.get("hasApprover"));

                    sendMailToApprover(mailParameters);
                }
            }
            deleteJEArray(oldJeId, companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
            deleteTemporaryInvoicesEntries(invoiceIdsArrary, companyid);
            txnManager.commit(status);
        }catch (ServiceException ex) {
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
                deleteTemporaryInvoicesEntries(invoiceIdsArrary, companyid);
            } catch (ServiceException ex1) {
                Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = "" + ex.getCause().getMessage();
            }
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
                deleteTemporaryInvoicesEntries(invoiceIdsArrary, companyid);
            } catch (ServiceException ex1) {
                Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
                deleteTemporaryInvoicesEntries(invoiceIdsArrary, companyid);
            } catch (ServiceException ex1) {
                Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null && flag != 1) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
                deleteTemporaryInvoicesEntries(invoiceIdsArrary, companyid);
            } catch (ServiceException ex1) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = "" + ex.getCause().getMessage();
            }
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("accException", isAccountingExe);
                jobj.put(Constants.channelName, channelName);
                jobj.put(Constants.billno,creditNoteNumBer );
                jobj.put(Constants.isTaxDeactivated, isTaxDeactivated);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return jobj;
    }

    /**
     * Description : Method is used to Save saveCreditNoteGstDetails
     * @param <paramJobj> :-Contains parameters company ID
     * @param <returnJobj> :-Contains return result of saveCreditNoteGst
     * @return :return list
     */
    public List saveCreditNoteGst(JSONObject paramJobj, JSONObject returnJobj) throws SessionExpiredException, ServiceException, AccountingException, JSONException {
        List returnList = new ArrayList();
        String debitNoteNumber = "";
        String debitNoteId = "";
        CreditNote creditNote = null;
        String oldjeid = "";
        String jeid = "";
        CreditNote creditnote = null;
        KwlReturnObject result;
        String linkedDocuments = "";
        String unlinkMessage = "";
        String jeentryNumber = "";
        boolean jeautogenflag = false;
        String jeIntegerPart = "";
        String jeDatePrefix = "";
        String jeDateAfterPrefix = "";
        String jeDateSuffix = "";
        String jeSeqFormatId = "";
        int approvalStatusLevel = 11;
        boolean isEditNote = false;
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            String currencyid = paramJobj.optString(Constants.currencyKey);
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString(Constants.externalcurrencyrate) != "" ? paramJobj.optString(Constants.externalcurrencyrate) : "0.0");
            KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) comp.getEntityList().get(0);
            int countryid = Integer.parseInt(company.getCountry().getID());
            String entryNumber = paramJobj.optString("number");
            String creditNoteId = paramJobj.optString("srid");
            String costCenterId = paramJobj.optString(Constants.costcenter);
            boolean isNoteAlso = false;
            String userFullName = paramJobj.optString(Constants.userfullname);
            boolean isEditToApprove = StringUtil.isNullOrEmpty(paramJobj.optString("isEditToApprove", null)) ? false : Boolean.parseBoolean(paramJobj.optString("isEditToApprove"));
            int cntype = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.cntype, null)) ? 1 : Integer.parseInt(paramJobj.optString(Constants.cntype));
            String srid = ("srid");
            String agentId=paramJobj.optString("agent","");         //ERP-28249 Debit note against customer for malaysian country.
            String isfavourite = paramJobj.optString("isfavourite");
            String sequenceformat = paramJobj.optString(Constants.sequenceformat);
            String customfield = paramJobj.optString("customfield");
            HashMap<String, Object> doDataMap = new HashMap<String, Object>();
            long createdon = System.currentTimeMillis();
            String createdby = paramJobj.optString(Constants.useridKey);
            String modifiedby = createdby;
            long updatedon = createdon;
            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();
            DateFormat df = authHandler.getDateOnlyFormat();
            Date creationDate = formatter.parse(paramJobj.optString(Constants.BillDate));
            if (sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(srid)) {
                doDataMap.put("entrynumber", entryNumber);
            } else {
                doDataMap.put("entrynumber", "");
            }
            if (sequenceformat.equals("NA")) {//SDP-14953 - In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Credit_Note_ModuleId, entryNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                    }
                }
            }
            String taxid = "";
            if (paramJobj.optString("taxid").equalsIgnoreCase("None")) {
                taxid = null;
            } else {
                taxid = paramJobj.optString("taxid",null);
            }
            if (!StringUtil.isNullOrEmpty(taxid)) {
                doDataMap.put("taxid", taxid);
            }
            /*
             *GST Currency Rate in CN Overcharge/Undercharge when Country currency is SGD and base currency is other than SGD 
             */
            doDataMap.put("gstCurrencyRate",paramJobj.optDouble("gstCurrencyRate", 0));                
            String transactionDateStr =  paramJobj.optString("billdate");
            Date transactionDate = df.parse(df.format(new Date()));
            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                transactionDate = df.parse(transactionDateStr);
            }
            doDataMap.put("autogenerated", sequenceformat.equals("NA") ? false : true);
            doDataMap.put(Constants.memo, paramJobj.optString(Constants.memo));
            doDataMap.put(Constants.posttext, paramJobj.optString(Constants.posttext) == null ? "" : paramJobj.optString(Constants.posttext));
            doDataMap.put("vendorid", paramJobj.optString("vendor"));
            doDataMap.put("customerid", paramJobj.optString("customer"));//ERM-778
            doDataMap.put("salesPersonID", paramJobj.optString("salesPerson"));//ERM-778

            if (paramJobj.getString(Constants.shipdate) != null && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.shipdate))) {
                doDataMap.put(Constants.shipdate, df.parse(paramJobj.optString(Constants.shipdate)));
            }
            doDataMap.put(Constants.shipvia, paramJobj.optString(Constants.shipvia));
            doDataMap.put(Constants.fob, paramJobj.optString(Constants.fob));
            doDataMap.put("orderdate", df.parse(paramJobj.optString(Constants.BillDate)));
            doDataMap.put("creationDate", formatter.parse(paramJobj.optString(Constants.BillDate)));

            doDataMap.put("isfavourite", isfavourite);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                doDataMap.put("costcenter", costCenterId);
            }
            doDataMap.put(Constants.companyKey, companyid);
            doDataMap.put(Constants.currencyKey, currencyid);
            doDataMap.put(Constants.cntype, cntype);

            doDataMap.put("createdon", createdon);
            doDataMap.put("createdby", createdby);
            doDataMap.put("modifiedby", modifiedby);
            doDataMap.put("updatedon", updatedon);
            doDataMap.put("creationDate", transactionDate);
            doDataMap.put("masteragent",agentId);     //ERP-28249 Debit note against customer for malaysian country.
            doDataMap.put("isNoteAlso", isNoteAlso);
            if (!StringUtil.isNullOrEmpty(creditNoteId)) {
                /*
                 * In case of edit if format is NA then entry number can be changed so need to check duplicate
                 */
                result = accCreditNoteDAOobj.getCNFromNoteNoAndId(entryNumber, companyid, creditNoteId);
                if (result.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Creditnotenumber", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                isEditNote = true;
                /**
                 * For US country Credit note delete Overcharge/Undercharge details
                 */
                if (countryid == Constants.USA_country_id) {
                    accCreditNoteDAOobj.deleteCreditNoteDetailTermMapAgainstDebitNote(creditNoteId, companyid);
                }
                /*
                 * For malaysian Company delete permanent credit note against vendor
                 */
                KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), creditNoteId);
                creditnote = (CreditNote) cnObj.getEntityList().get(0);
                oldjeid = creditnote.getJournalEntry().getID();
                JournalEntry jetemp = creditnote.getJournalEntry();
                approvalStatusLevel = creditnote.getApprovestatuslevel();
                boolean isEdit = false;
                if (paramJobj.has("isEdit")) {
                    isEdit = Boolean.parseBoolean((String) paramJobj.get("isEdit"));
                }
                if (creditnote != null && isEdit) {
                    updateOpeningInvoiceAmountDue(creditnote.getID(), companyid);
                }
                accJournalEntryobj.permanentDeleteCreditNoteAgainstVendorGst(creditNoteId, companyid);
                accCreditNoteDAOobj.deleteCreditTaxDetails(debitNoteId, companyid);
                if (jetemp != null) {
                    jeentryNumber = jetemp.getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                    jeautogenflag = jetemp.isAutoGenerated();
                    jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                    jeDatePrefix = jetemp.getDatePreffixValue();
                    jeDateAfterPrefix = jetemp.getDateAfterPreffixValue();
                    jeDateSuffix = jetemp.getDateSuffixValue();
                }
                doDataMap.put("cnid", creditNoteId);
            } else {
                doDataMap.put("autogenerated", !sequenceformat.equals("NA") ? true : false);
                doDataMap.put("oldRecord", false);
                Long seqNumber = null;
                result = accCreditNoteDAOobj.getCNSequenceNo(companyid, creationDate);
                List list = result.getEntityList();
                if (!list.isEmpty()) {
                    seqNumber = (Long) list.get(0);
                }
                doDataMap.put("sequence", seqNumber.intValue());
            }
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                jeDataMap.put("entrynumber", "");
            } else {
                jeDataMap.put("entrynumber", jeentryNumber);
                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            }
            jeDataMap.put("autogenerated", true);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("entrydate", formatter.parse(paramJobj.optString(Constants.BillDate)));
            jeDataMap.put(Constants.Checklocktransactiondate, paramJobj.optString(Constants.creationdate));//ERP-16800-Without parsing date
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("memo", paramJobj.optString("memo"));
            jeDataMap.put("createdby", createdby);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                jeDataMap.put("costcenterid", costCenterId);
            }
            jeDataMap.put("currencyid", currencyid);
            HashSet<JournalEntryDetail> jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            doDataMap.put("journalentryid", jeid);

            double totalAmt = 0, totalRowDiscount = 0, totalAmountinbase = 0;
            double subTotal = 0, taxAmt = 0;
            JSONArray jArr = new JSONArray(paramJobj.optString(Constants.detail));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                double qrate = authHandler.roundUnitPrice(jobj.optDouble("rate", 0),companyid);
                double quantity = authHandler.roundQuantity(jobj.getDouble("quantity"), companyid);
                double quotationPrice = authHandler.round(quantity * qrate, companyid);
                double discountQD = authHandler.round(jobj.optDouble("prdiscount", 0), companyid);
                double discountPerRow = 0;
                if (jobj.optInt("discountispercent", 0) == 1) {//percent discount
                    discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                } else {//flat discount
                    discountPerRow = discountQD;
                }
                totalRowDiscount += discountPerRow;
                taxAmt += jobj.optDouble("taxamount", 0.0);
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("subTotal"))) {
                subTotal = Double.parseDouble(paramJobj.optString("subTotal"));
            }
            /*
             * if global level tax applied it will execute only for dashbord
             * transaction
             */
            if (taxAmt == 0) {
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxamount"))) {
                    taxAmt = Double.parseDouble(paramJobj.optString("taxamount"));
                }
            }

            totalAmt = subTotal + taxAmt;
            totalRowDiscount = authHandler.round(totalRowDiscount, companyid);
            doDataMap.put("totallineleveldiscount", totalRowDiscount);
            doDataMap.put("cnamount", totalAmt);
            if (cntype == Constants.CreditNoteForOvercharge) {//Amount due getting in edit case for Overcharge case also need to handle this for Undercharge case.
                doDataMap.put("cnamountdue", totalAmt);
            }
            doDataMap.put("approvestatuslevel", approvalStatusLevel);
            if(paramJobj.has(Constants.MVATTRANSACTIONNO) && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.MVATTRANSACTIONNO))){
                doDataMap.put(Constants.MVATTRANSACTIONNO,paramJobj.optString(Constants.MVATTRANSACTIONNO));
            }else{
                doDataMap.put(Constants.MVATTRANSACTIONNO,"");
            }
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            filterRequestParams.put(Constants.companyKey, companyid);
            filterRequestParams.put(Constants.globalCurrencyKey, currencyid);
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalAmt, currencyid, df.parse(paramJobj.optString(Constants.BillDate)), externalCurrencyRate);
            totalAmountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
            doDataMap.put("cnamountinbase", totalAmountinbase);
            KwlReturnObject descbAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalRowDiscount, currencyid, df.parse(paramJobj.optString(Constants.BillDate)), externalCurrencyRate);
            double descountinBase = authHandler.round((Double) descbAmtTax.getEntityList().get(0), companyid);
            doDataMap.put("discountinbase", descountinBase);
            String amount=paramJobj.optString("amounts","");
            String amountsArr[]=amount.split(",");
            double totalAmount=0.0;
            for (String amountsArr1 : amountsArr) {
                if (!StringUtil.isNullOrEmpty(amountsArr1)) {
                    totalAmount += Double.parseDouble(amountsArr1);
                }
            }
            if (!StringUtil.isNullOrEmpty(amount) && cntype != Constants.CreditNoteForOvercharge) {//Amount due getting in edit case for Overcharge case.
                doDataMap.put("cnamountdue", totalAmount);
            }
            KwlReturnObject doresult = null;
            if (isEditNote) {
                doresult = accCreditNoteDAOobj.updateCreditNote(doDataMap);
            } else {
                //add accountID of respective selected perosn in case of CN/DN Overchaged, Undercharged. 
                doDataMap.put("accountId", paramJobj.optString("personaccid"));
                doresult = accCreditNoteDAOobj.addCreditNote(doDataMap);
            }
            creditNote = (CreditNote) doresult.getEntityList().get(0);
            double totalAmountInBase = 0;
            KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(jeDataMap, totalAmt, currencyid, creationDate, externalCurrencyRate);
            totalAmountInBase = (Double) baseAmount.getEntityList().get(0);
            HashMap<String, Object> CNApproveMap = new HashMap<String, Object>();
            CNApproveMap.put("companyid", companyid);
            List approvedlevel = null;
            if (isEditToApprove) {
                CNApproveMap.put("billid", creditNote.getID());
                CNApproveMap.put("userid", createdby);
                CNApproveMap.put("remark", "");
                CNApproveMap.put("userName", userFullName);
                CNApproveMap.put("level", creditNote.getApprovestatuslevel());
                CNApproveMap.put("amount", String.valueOf(authHandler.round(totalAmountInBase, companyid)));
                CNApproveMap.put("isEditToApprove", isEditToApprove);
                CNApproveMap.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));
                List list = approvePendingCreditNote(CNApproveMap);
                approvalStatusLevel = 11;
                jeDataMap.put(Constants.SEQFORMAT, journalEntry.getSeqformat() != null ? journalEntry.getSeqformat().getID() : null);
                jeDataMap.put(Constants.SEQNUMBER, journalEntry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, journalEntry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, journalEntry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, journalEntry.getDateSuffixValue());
                jeDataMap.put("entrynumber", journalEntry.getEntryNumber());
            } else {
                CNApproveMap.put("level", 0);//Initialy it will be 0
                CNApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmountInBase, companyid)));
                CNApproveMap.put("currentUser", createdby);
                CNApproveMap.put("fromCreate", true);
                CNApproveMap.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                CNApproveMap.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));
                boolean isMailApplicable = false;
                approvedlevel = approveCreditNote(creditNote, CNApproveMap, isMailApplicable);
                approvalStatusLevel = (Integer) approvedlevel.get(0);
            }

            if (approvalStatusLevel == 11) {
                jeDataMap.put("pendingapproval", 0);
            } else {
                jeDataMap.put("pendingapproval", 1);
            }
            doDataMap.put(Constants.Acc_id, creditNote.getID());
            Set<CreditNoteAgainstVendorGst> podetails = null;
            List rowDetails = saveCreditNoteGstRows1(paramJobj, companyid, journalEntry, externalCurrencyRate, creditNote);
            podetails = (HashSet) rowDetails.get(0);
            creditNote.setRowsGst(podetails);
            creditNote.setApprovestatuslevel(approvalStatusLevel);

            String accountId = "";
            Map<String, Object> paramsMap = new HashMap<>();
            if (cntype == Constants.CreditNoteForOvercharge) {
                paramsMap.put("ID", paramJobj.optString("customer"));
                accountId = (String) kwlCommonTablesDAOObj.getRequestedObjectFields(Customer.class, new String[]{"account.ID"}, paramsMap);
            } else {
                paramsMap.put("ID", paramJobj.optString("vendor"));
                accountId = (String) kwlCommonTablesDAOObj.getRequestedObjectFields(Vendor.class, new String[]{"account.ID"}, paramsMap);
            }
            jedetails = (HashSet) rowDetails.get(2);
            double totalDNAmt = (Double) rowDetails.get(4);
            double totalDiscountAmt = (Double) rowDetails.get(6);
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put(Constants.companyKey, companyid);
            jedjson.put("amount", totalDNAmt);
            jedjson.put("accountid", accountId);
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
            creditNote.setCustomerEntry(jed);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String discountAccountId = cntype == Constants.CreditNoteForOvercharge ? preferences.getDiscountGiven().getID() : preferences.getDiscountReceived().getID();
            if (totalDiscountAmt > 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put(Constants.companyKey, companyid);
                jedjson.put("amount", totalDiscountAmt);
                jedjson.put("accountid", discountAccountId);
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("transactionId", creditNote.getID());
            jeDataMap.put("transactionModuleid", Constants.Acc_Credit_Note_ModuleId);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("savePurchaseReturn : " + ex.getMessage(), ex);
        }
        returnList.add(oldjeid);
        returnList.add(creditNote);
        returnList.add(approvalStatusLevel);
        returnList.add(debitNoteNumber);
        returnList.add(debitNoteId);
        returnList.add(linkedDocuments);
        returnList.add(unlinkMessage);
        return returnList;
    }

    public List saveCreditNote(JSONObject paramJobj, JSONObject returnJobj) throws ServiceException, SessionExpiredException, AccountingException, ScriptException, MessagingException {
        CreditNote creditnote = null;
        KwlReturnObject result;
        List ll = new ArrayList();
        String oldjeid = "";
        try {
            boolean reloadInventory = false;//Flag used to reload inventory on Client Side If CN type equals to "Return" or "Defective"
            String companyid = paramJobj.optString(Constants.companyKey);
            String currencyid = paramJobj.optString(Constants.currencyKey);
            DateFormat df = authHandler.getDateOnlyFormat();
            String customfield = paramJobj.optString("customfield");
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate"));
            boolean otherwise = paramJobj.optString("otherwise", null) != null;
            boolean isEditToApprove = StringUtil.isNullOrEmpty(paramJobj.optString("isEditToApprove", null)) ? false : Boolean.parseBoolean(paramJobj.optString("isEditToApprove"));
            String sequenceformat = paramJobj.optString("sequenceformat");
            String createdby = paramJobj.optString(Constants.useridKey);

            String accountid = paramJobj.optString("accountid");
            Boolean isCopy = paramJobj.optBoolean("isCopy",false);
            String oldNoteId = paramJobj.optString("billid","");

            String creditNoteId = paramJobj.optString("noteid");
            String modifiedby = createdby;
            String userFullName = paramJobj.optString(Constants.userfullname);
            long createdon = System.currentTimeMillis();
            long updatedon = createdon;
            String jeentryNumber = "";
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            String addressID = "";
            boolean isEditNote = false;
            List mailParams = Collections.EMPTY_LIST;
            int approvalStatusLevel = 11;
            String entryNumber = paramJobj.optString("number");
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParamsJson(paramJobj);

            Date creationDate = df.parse(paramJobj.optString(Constants.creationdate));
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            HashMap<String, Object> credithm = new HashMap<String, Object>();
            int cntype = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.cntype, null)) ? 1 : Integer.parseInt(paramJobj.optString(Constants.cntype));

            if (!StringUtil.isNullOrEmpty(creditNoteId)) {
                //In case of edit if format is NA then entry number can be changed so need to check duplicate
                result = accCreditNoteDAOobj.getCNFromNoteNoAndId(entryNumber, companyid, creditNoteId);
                if (result.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Creditnotenumber", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                isEditNote = true;
                KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), creditNoteId);
                creditnote = (CreditNote) cnObj.getEntityList().get(0);
                oldjeid = creditnote.getJournalEntry().getID();
                JournalEntry jetemp = creditnote.getJournalEntry();
                approvalStatusLevel = creditnote.getApprovestatuslevel();
                /*
                 * Taking original account in edit and copy Credit note. Refer
                 * SDP-7867
                 */
                if (creditnote.getCustomerEntry() != null) {
                    accountid = creditnote.getCustomerEntry().getAccount().getID();
                }

                if (jetemp != null) {
                    jeentryNumber = jetemp.getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                    jeautogenflag = jetemp.isAutoGenerated();
                    jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                    jeDatePrefix = jetemp.getDatePreffixValue();
                    jeDateAfterPrefix = jetemp.getDateAfterPreffixValue();
                    jeDateSuffix = jetemp.getDateSuffixValue();
                }
                // chk for open
                if (creditnote != null) {
                    boolean isCreditNoteWithSalesReturn = (creditnote.getSalesReturn() != null && creditnote.getSalesReturn().isIsNoteAlso()) ? true : false;
                    if (!isEditToApprove && !isCreditNoteWithSalesReturn) {//for case editToApprove doesnot need to update invoice amount because we did not reduce invoice amount for pending CN
                        updateOpeningInvoiceAmountDue(creditnote.getID(), companyid);
                    }
                    // delete foreign gain loss JE
                    List resultJe = accCreditNoteDAOobj.getForeignGainLossJE(creditnote.getID(), companyid);
                    if (resultJe.size() > 0 && resultJe.get(0) != null) {
                        Iterator itr = resultJe.iterator();
                        while (itr.hasNext()) {
                            Object object = itr.next();
                            String jeid = object != null ? object.toString() : "";
                            deleteJEArray(jeid, companyid);
                        }
                    }
                }

                //taking address id in edit case
                addressID = creditnote.getBillingShippingAddresses() != null ? creditnote.getBillingShippingAddresses().getID() : "";

                result = accCreditNoteDAOobj.deleteCreditNoteDetails(creditnote.getID(), companyid);
                if (company.getCountry() != null && company.getCountry().getID().equals(Constants.INDIA_COUNTRYID) && creditnote.getCnTaxEntryDetails() != null) {
                    /**
                     * delete CreditNoteDetailTermMap mapping while editing
                     * CN.
                     */
                    String ids = "";
                    for (CreditNoteTaxEntry cnTaxEntry : creditnote.getCnTaxEntryDetails()) {
                        ids += "'" + cnTaxEntry.getID() + "',";
                    }
                    if(!StringUtil.isNullOrEmpty(ids)){
                        accCreditNoteDAOobj.deleteCreditNoteDetailTermMap(ids.substring(0, ids.length() - 1));
                    }
                    accCreditNoteDAOobj.deleteGstTaxClassDetails(creditNoteId);
                }
                result = accCreditNoteDAOobj.deleteCreditTaxDetails(creditnote.getID(), companyid);

                //Delete old entries and insert new entries again from optimized table in edit case.
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
                deleteJEDetailsCustomData(oldjeid);
                credithm.put("cnid", creditNoteId);

                /*Deleting Linking information of Credit Note while Editing */
                accCreditNoteDAOobj.deleteLinkingInformationOfCN(credithm);
                if (company.getCountry() != null && company.getCountry().getID().equals(Constants.INDIA_COUNTRYID)) {
                    JSONObject json = new JSONObject();
                    json.put("creditnoteid", creditNoteId);
                    accCreditNoteDAOobj.deleteCreditNoteInvoiceMappingInfo(json);
                }

            } else {
                if (isCopy) {
                    /*
                     * Taking original account in edit and copy Credit note.
                     * Refer SDP-7867
                     */
                    KwlReturnObject obj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), oldNoteId);
                    creditnote = (CreditNote) obj.getEntityList().get(0);

                    //While copying credit note if customer or vendor is changed then take account of new Customer/Vendor.
                    boolean isCustomerChanged = false;
                    if (cntype == 4 && creditnote.getVendor() != null) {//CN against vendor
                        isCustomerChanged = !(creditnote.getVendor().getID().equals(paramJobj.optString("accid")));
                    } else if (creditnote.getCustomer() != null) {
                        isCustomerChanged = !(creditnote.getCustomer().getID().equals(paramJobj.optString("accid")));
                    }

                    if (creditnote != null && creditnote.getCustomerEntry() != null && !isCustomerChanged) {
                        accountid = creditnote.getCustomerEntry().getAccount().getID();
                    }
                }
                credithm.put("autogenerated", !sequenceformat.equals("NA") ? true : false);
                credithm.put("oldRecord", false);
                Long seqNumber = null;
                result = accCreditNoteDAOobj.getCNSequenceNo(companyid, creationDate);
                List list = result.getEntityList();
                if (!list.isEmpty()) {
                    seqNumber = (Long) list.get(0);
                }
                credithm.put("sequence", seqNumber.intValue());
            }
            if (sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(creditNoteId)) {
                credithm.put("entrynumber", entryNumber);
            } else {
                credithm.put("entrynumber", "");
            }

            if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Credit_Note_ModuleId, entryNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                    }
                }
            }
            currencyid = (paramJobj.optString("currencyid", null) == null ? kwlcurrency.getCurrencyID() : paramJobj.optString("currencyid"));
            curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String costCenterId = !StringUtil.isNullOrEmpty(paramJobj.optString("costCenterId", null)) ? paramJobj.optString("costCenterId") : "";
            String salesPersonID = !StringUtil.isNullOrEmpty(paramJobj.optString("salesPersonID", null)) ? paramJobj.optString("salesPersonID") : "";
            String masterAgentID = !StringUtil.isNullOrEmpty(paramJobj.optString("masteragent", null)) ? paramJobj.optString("masteragent") : "";
            credithm.put("memo", (!StringUtil.isNullOrEmpty(paramJobj.optString("memo"))) ? paramJobj.optString("memo") : "");
            credithm.put("includingGST", Boolean.parseBoolean(paramJobj.optString("includingGST")));
            credithm.put("companyid", company.getCompanyID());
            credithm.put("currencyid", currencyid);
            credithm.put("createdby", createdby);
            credithm.put("modifiedby", modifiedby);
            credithm.put("createdon", createdon);
            credithm.put("updatedon", updatedon);
            credithm.put("costcenter", costCenterId);
            credithm.put("salesPersonID", salesPersonID);
            credithm.put("masteragent", masterAgentID);
            credithm.put("externalCurrencyRate", externalCurrencyRate);
            credithm.put("approvestatuslevel", approvalStatusLevel);
            if(paramJobj.has(Constants.MVATTRANSACTIONNO) && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.MVATTRANSACTIONNO))){
                credithm.put(Constants.MVATTRANSACTIONNO,paramJobj.optString(Constants.MVATTRANSACTIONNO));
            }else{
                credithm.put(Constants.MVATTRANSACTIONNO,"");
            }
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                jeDataMap.put("entrynumber", "");
            } else {
                jeDataMap.put("entrynumber", jeentryNumber);
                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            }
            jeDataMap.put("autogenerated", true);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("entrydate", creationDate);
            jeDataMap.put(Constants.Checklocktransactiondate, paramJobj.optString(Constants.creationdate));//ERP-16800-Without parsing date
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", paramJobj.optString("memo"));
            jeDataMap.put("createdby", createdby);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                jeDataMap.put("costcenterid", costCenterId);
            }
            jeDataMap.put("currencyid", currencyid);
            HashSet<JournalEntryDetail> jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            credithm.put("journalentryid", jeid);
            List CNlist = new ArrayList();
            double cnamount = 0.0;
            double cnamountdue = 0.0;
            if (cntype == 4) {//CN against vendor
                credithm.put("vendorid", paramJobj.optString("accid"));
            } else {
                credithm.put("customerid", paramJobj.optString("accid"));
            }
            paramJobj.put("customerAccountId",accountid);

            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                String invoiceids = paramJobj.optString("invoiceids");
                String invoiceIdsArrary[] = invoiceids.split(",");
                JSONArray invoicedetails = null;
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("invoicedetails", null))) {
                    invoicedetails = new JSONArray(paramJobj.optString("invoicedetails"));
                }
                Map<String, Double> invoiceIdAmountDueMap = new HashMap<>();//to calculate invoiceamountdue
                Map<String, String> invoiceIdNoMap = new HashMap<>();//to calculate invoiceamountdue
                if (cntype != Constants.CreditNoteForOvercharge) {
                    for (int i = 0; i < invoiceIdsArrary.length; i++) {
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceIdsArrary[i]);
                        Invoice invoice = (Invoice) objItr.getEntityList().get(0);
                        if (invoice != null && invoicedetails != null) {
                            double invoiceAmoutDue = 0; // Opening transaction record
                            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                                invoiceAmoutDue = invoice.getOpeningBalanceAmountDue();
                            } else {
                                invoiceAmoutDue = invoice.getInvoiceamountdue();
                            }
                            JSONObject invoiceJobj = invoicedetails.getJSONObject(i);
                            double amountReceived = invoiceJobj.getDouble("invamount");           //amount of DN 
                            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                            String gCurrencyid = paramJobj.optString(Constants.currencyKey);
                            filterRequestParams.put(Constants.companyKey, companyid);
                            filterRequestParams.put(Constants.globalCurrencyKey, currencyid);
                            KwlReturnObject baseAmt = null;
                            if (!invoice.isIsOpeningBalenceInvoice()) {
                                baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, invoiceAmoutDue, gCurrencyid, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                            } else {
                                baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, invoiceAmoutDue, gCurrencyid, invoice.getCreationDate(), invoice.getExchangeRateForOpeningTransaction());
                            }
                            KwlReturnObject baseAmtRec = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, amountReceived, currencyid, creationDate, externalCurrencyRate);
                            double invoiceAmoutDueBase = authHandler.round((Double) baseAmt.getEntityList().get(0), companyid);
                            double amountReceivedBase = authHandler.round((Double) baseAmtRec.getEntityList().get(0), companyid);
                            double diffDueReceive = 0.0;
                            if (invoiceIdAmountDueMap.containsKey(invoiceIdsArrary[i]) && invoiceIdAmountDueMap.get(invoiceIdsArrary[i]) != null) {
                                Double tempinvamtdue = (Double) invoiceIdAmountDueMap.get(invoiceIdsArrary[i]);
                                diffDueReceive = tempinvamtdue - amountReceivedBase;
                            } else {
                                diffDueReceive = invoiceAmoutDueBase - amountReceivedBase;
                            }
                            invoiceIdAmountDueMap.put(invoiceIdsArrary[i], diffDueReceive);
                            invoiceIdNoMap.put(invoiceIdsArrary[i], invoice.getInvoiceNumber());
                        }
                    }
                    if (!invoiceIdAmountDueMap.isEmpty() && invoiceIdAmountDueMap != null) {
                        StringBuilder invoiceNoBuildString = new StringBuilder();
                        for (Map.Entry<String, Double> extraColsEntry : invoiceIdAmountDueMap.entrySet()) {
                            if (extraColsEntry.getValue() < 0) {
                                if (extraColsEntry.getValue() < 0.0 || extraColsEntry.getValue() < 0) {
                                    String invoicenumber = invoiceIdNoMap.get(extraColsEntry.getKey());
                                    if (invoiceNoBuildString.length() > 0) {
                                        invoiceNoBuildString.append(",");
                                    }
                                    invoiceNoBuildString.append(invoicenumber);
                                }
                            }
                        }
                        if (invoiceNoBuildString.length() > 0) {
                            String invoicenos = invoiceNoBuildString.toString();
                            if (invoicenos.contains(",")) {
                                invoicenos = invoicenos.substring(invoicenos.lastIndexOf(","));
                            }
                            throw ServiceException.FAILURE("Selected Invoice(s) "+ invoicenos +" "+messageSource.getMessage("acc.field.alreadyknock_off_Invoice", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))), "", false);
                        }
                    }
                }
            }
            if (otherwise && cntype == 2) {//Credit note otherwise//
                credithm.put("otherwise", true);
                credithm.put("openflag", true);
                cnamount = Double.parseDouble(paramJobj.optString("amount"));
                cnamountdue = cnamount;
                credithm.put("cnamount", cnamount);
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, cnamount, currencyid, creationDate, externalCurrencyRate);
                double cnamountinbase = (Double) baseAmount.getEntityList().get(0);
                credithm.put("cnamountinbase", authHandler.round(cnamountinbase, companyid));
                credithm.put("cnamountdue", cnamountdue);
                credithm.put(Constants.cntype, 2);
                CNlist = saveCreditNoteRowsOW2(GlobalParams, paramJobj, company, currency, journalEntry, preferences, externalCurrencyRate);
            } else if (cntype == 3) {//Credit Note against Paid Customer Invoice
                //this option removed from UI. So this code is no longer in use
                credithm.put("otherwise", true);
                credithm.put("openflag", true);
                CNlist = saveCreditNoteRows2(GlobalParams, paramJobj, company, currency, journalEntry, preferences, externalCurrencyRate);
                credithm.put("cnamount", (Double) CNlist.get(5));
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, (Double) CNlist.get(5), currencyid, creationDate, externalCurrencyRate);
                double cnamountinbase = (Double) baseAmount.getEntityList().get(0);
                credithm.put("cnamountinbase", authHandler.round(cnamountinbase, companyid));
                credithm.put("cnamountdue", (Double) CNlist.get(5));
                credithm.put(Constants.cntype, 3);
            } else if (cntype == 4) {//Credit Note against vendor
                credithm.put("otherwise", true);
                credithm.put("openflag", true);
                cnamount = Double.parseDouble(paramJobj.optString("amount"));
                cnamountdue = cnamount;
                credithm.put("cnamount", cnamount);
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, cnamount, currencyid, creationDate, externalCurrencyRate);
                double cnamountinbase = (Double) baseAmount.getEntityList().get(0);
                credithm.put("cnamountinbase", authHandler.round(cnamountinbase, companyid));
                credithm.put("cnamountdue", cnamountdue);
                credithm.put(Constants.cntype, cntype);
                CNlist = saveCreditNoteRowsOW2(GlobalParams, paramJobj, company, currency, journalEntry, preferences, externalCurrencyRate);
            } else {//Credit note against unpaid invoice
                credithm.put("otherwise", true);
                credithm.put("openflag", true);
                cnamount = Double.parseDouble(paramJobj.optString("amount"));
                cnamountdue = cnamount;
                credithm.put("cnamount", cnamount);
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, cnamount, currencyid, creationDate, externalCurrencyRate);
                double cnamountinbase = (Double) baseAmount.getEntityList().get(0);
                credithm.put("cnamountinbase", authHandler.round(cnamountinbase, companyid));
                credithm.put("cnamountdue", cnamountdue);
                credithm.put(Constants.cntype, 1);
                CNlist = saveCreditNoteRowsOW2(GlobalParams, paramJobj, company, currency, journalEntry, preferences, externalCurrencyRate);
            }

            Double totalAmount = (Double) CNlist.get(0);
            HashSet<CreditNoteDetail> cndetails = (HashSet<CreditNoteDetail>) CNlist.get(2);
            jedetails = (HashSet<JournalEntryDetail>) CNlist.get(3);
            HashSet<CreditNoteTaxEntry> creditNoteTaxEntryDetails = (HashSet<CreditNoteTaxEntry>) CNlist.get(5);
            reloadInventory = (Boolean) CNlist.get(4);
            returnJobj.put("reloadInventory", reloadInventory);

            double termTotalAmount = 0;
            HashMap<String, Double> termAcc = new HashMap<String, Double>();
            String cnTerms = paramJobj.optString("invoicetermsmap",null);
            if (!StringUtil.isNullOrEmpty(cnTerms)) {
                JSONArray termsArr = new JSONArray(cnTerms);
                for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                    double termamount = termsArr.getJSONObject(cnt).getDouble("termamount");
                    termTotalAmount += termamount;
                    if (termAcc.containsKey(termsArr.getJSONObject(cnt).getString("glaccount"))) {
                        double tempAmount = termAcc.get(termsArr.getJSONObject(cnt).getString("glaccount"));
                        termAcc.put(termsArr.getJSONObject(cnt).getString("glaccount"), termamount + tempAmount);
                    } else {
                        termAcc.put(termsArr.getJSONObject(cnt).getString("glaccount"), termamount);
                    }
                }
            }
            totalAmount += termTotalAmount;

            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", totalAmount);
            jedjson.put("accountid", accountid);
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);

            credithm.put("customerentry", jed.getID());
            credithm.put("accountId", jed.getAccount().getID());

            if (termAcc.size() > 0) {
                for (Map.Entry<String, Double> entry : termAcc.entrySet()) {
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", entry.getValue() > 0 ? entry.getValue() : (entry.getValue() * (-1)));
                    jedjson.put("accountid", entry.getKey());
                    jedjson.put("debit", entry.getValue() > 0 ? true : false);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }

            //**********Credit Node address related code start**************
            boolean isDefaultAddress = paramJobj.optString("defaultAdress", null) != null ? Boolean.parseBoolean(paramJobj.optString("defaultAdress")) : false;
            Map<String, Object> addressParams = Collections.EMPTY_MAP;
            if (isDefaultAddress) { //defautladdress came true only when user create a new CN without saving any address from address window.customer/vendor addresses taken default 
                if (cntype == 4) {//Credit Note against vendor
                    addressParams = AccountingAddressManager.getDefaultVendorAddressParams(paramJobj.optString("accid"), companyid, accountingHandlerDAOobj);
                } else {
                    addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(paramJobj.optString("accid"), companyid, accountingHandlerDAOobj);
                }
            } else {
                if (cntype == 4) {//Credit Note against vendor
                    addressParams = AccountingAddressManager.getAddressParamsJson(paramJobj, true);
                } else {
                    addressParams = AccountingAddressManager.getAddressParamsJson(paramJobj, false);
                }
            }

            if (!StringUtil.isNullOrEmpty(addressID)) { //If Edit case then updating existing CN address 
                addressParams.put("id", addressID);
            }
            KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
            BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
            credithm.put("billshipAddressid", bsa.getID());
            credithm.put("creationDate", creationDate);
            credithm.put(Constants.Checklocktransactiondate, paramJobj.optString(Constants.creationdate));

            if (isEditNote) {
                result = accCreditNoteDAOobj.updateCreditNote(credithm);
            } else {
                result = accCreditNoteDAOobj.addCreditNote(credithm);
            }

            creditnote = (CreditNote) result.getEntityList().get(0);
            /**
             * Save GST History Customer/Vendor data.
             */
            if (creditnote.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                paramJobj.put("docid", creditnote.getID());
                paramJobj.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(paramJobj);
            }

            /**
             * *****************Credit Note Multilevel Approval Rule Related
             * Code Start************************
             */
            double totalAmountInBase = 0;
            KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, totalAmount, currencyid, creationDate, externalCurrencyRate);
            totalAmountInBase = (Double) baseAmount.getEntityList().get(0);
            HashMap<String, Object> CNApproveMap = new HashMap<String, Object>();
            CNApproveMap.put("companyid", companyid);
            List approvedlevel = null;
            if (isEditToApprove) {
                CNApproveMap.put("billid", creditnote.getID());
                CNApproveMap.put("userid", createdby);
                CNApproveMap.put("remark", "");
                CNApproveMap.put("userName", userFullName);
                CNApproveMap.put("level", creditnote.getApprovestatuslevel());
                CNApproveMap.put("amount", String.valueOf(authHandler.round(totalAmountInBase, companyid)));
                CNApproveMap.put("isEditToApprove", isEditToApprove);
                CNApproveMap.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));
                List list = approvePendingCreditNote(CNApproveMap);
                approvalStatusLevel = 11;
                jeDataMap.put(Constants.SEQFORMAT, journalEntry.getSeqformat() != null ? journalEntry.getSeqformat().getID() : null);
                jeDataMap.put(Constants.SEQNUMBER, journalEntry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, journalEntry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, journalEntry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, journalEntry.getDateSuffixValue());
                jeDataMap.put("entrynumber", journalEntry.getEntryNumber());
            } else {
                CNApproveMap.put("level", 0);//Initialy it will be 0
                CNApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmountInBase, companyid)));
                CNApproveMap.put("currentUser", createdby);
                CNApproveMap.put("fromCreate", true);
                CNApproveMap.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                CNApproveMap.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));
                boolean isMailApplicable = false;
                approvedlevel = approveCreditNote(creditnote, CNApproveMap, isMailApplicable);
                approvalStatusLevel = (Integer) approvedlevel.get(0);
                mailParams = (List) approvedlevel.get(1);
            }

            credithm.put("approvestatuslevel", approvalStatusLevel);
            if (approvalStatusLevel == 11) {
                jeDataMap.put("pendingapproval", 0);
            } else {
                jeDataMap.put("pendingapproval", 1);
            }
            /**
             * **********************Credit Note Multilevel Approval
             * RuleRelated Code End****************************
             */

            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("transactionId", creditnote.getID());
            jeDataMap.put("transactionModuleid", Constants.Acc_Credit_Note_ModuleId);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }
            credithm.put("cnid", creditnote.getID());
            Iterator itr = cndetails.iterator();
            while (itr.hasNext()) {
                CreditNoteDetail cnd = (CreditNoteDetail) itr.next();
                cnd.setCreditNote(creditnote);
            }
            credithm.put("cndetails", cndetails);

            Iterator cntaxitr = creditNoteTaxEntryDetails.iterator();
            while (cntaxitr.hasNext()) {
                CreditNoteTaxEntry noteTaxEntry = (CreditNoteTaxEntry) cntaxitr.next();
                noteTaxEntry.setCreditNote(creditnote);
            }
            credithm.put("creditNoteTaxEntryDetails", creditNoteTaxEntryDetails);

            result = accCreditNoteDAOobj.updateCreditNote(credithm);
            creditnote = (CreditNote) result.getEntityList().get(0);

            //Add entry in optimized table
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            if (preferences.isInventoryAccountingIntegration()) {

                String action = "17";
                boolean isDirectUpdateInvFlag = false;
                if (preferences.isUpdateInvLevel()) {
                    isDirectUpdateInvFlag = true;
                    action = "19";//Direct Inventory Update action
                }

                JSONArray productArray = new JSONArray();
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("productdetails", null))) {
                    JSONArray jArr = new JSONArray(paramJobj.optString("productdetails"));
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jobj = jArr.getJSONObject(i);
                        KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString("productid"));
                        Product product = (Product) proresult.getEntityList().get(0);
                        if (reloadInventory) { //check inventory flag and then update inventory to the inventory systeam

                            KwlReturnObject inResult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                            InvoiceDetail invoiceRow = (InvoiceDetail) inResult.getEntityList().get(0);

                            boolean updateInventoryFlag = invoiceRow.getInventory().isInvrecord();

                            if (preferences.isWithInvUpdate()) {
                                updateInventoryFlag = getInvoiceStatusForDO(invoiceRow);
                            }

                            if (updateInventoryFlag) {
                                JSONObject productObject = new JSONObject();
                                productObject.put("itemUomId", jobj.getString("uomid"));
                                productObject.put("itemBaseUomRate", jobj.getDouble("baseuomrate"));
                                productObject.put("itemQuantity", jobj.getDouble("remquantity") * jobj.getDouble("baseuomrate"));
                                productObject.put("quantity", jobj.getDouble("remquantity") * jobj.getDouble("baseuomrate"));
                                //productObject.put("itemQuantity", jobj.getDouble("remquantity"));
                                productObject.put("itemCode", product.getProductid());
                                if (isDirectUpdateInvFlag) {
                                    productObject.put("storeid", jobj.getString("invstore"));
                                    productObject.put("locationid", jobj.getString("invlocation"));
                                    productObject.put("rate", jobj.getDouble("rate"));
                                }
                                productArray.put(productObject);

                            }
                        }
                    }
                    if (productArray.length() > 0) {

                        String sendDateFormat = "yyyy-MM-dd";
                        DateFormat dateformat = new SimpleDateFormat(sendDateFormat);
                        Date date = df.parse(paramJobj.optString(Constants.creationdate));
                        String stringDate = dateformat.format(date);

                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("deliveryDate", stringDate);
                        jSONObject.put("dateFormat", sendDateFormat);
                        jSONObject.put("details", productArray);
                        jSONObject.put("orderNumber", entryNumber);
                        jSONObject.put("selling", true);

                        String url = paramJobj.optString(Constants.inventoryURL);
                        CommonFnController cfc = new CommonFnController();
                        cfc.updateInventoryLevel(paramJobj, jSONObject, url, action);
                    }
                }
            }

            cnTerms = paramJobj.optString("invoicetermsmap",null);
            List<HashMap<String, Object>> creditTermDataList = new ArrayList();
            if (StringUtil.isAsciiString(cnTerms)) {
                creditTermDataList = mapDebitTerms(cnTerms, creditnote.getID(), paramJobj.optString(Constants.useridKey));

            }
            String moduleName = Constants.CREDIT_NOTE;
            //Send Mail when Purchase Requisition is generated or modified.
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), companyid);
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(creditNoteId)) {
                    if (documentEmailSettings.isCreditNoteGenerationMail()) { ////Create New Case
                        sendmail = true;
                    }
                } else {
                    isEditMail = true;
                    if (documentEmailSettings.isCreditNoteUpdationMail()) { // edit case  
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
                        if (creditnote != null && creditnote.getCreatedby() != null) {
                            createdByEmail = creditnote.getCreatedby().getEmailID();
                            createdById = creditnote.getCreatedby().getUserID();
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
                    String crNumber = entryNumber;
                    accountingHandlerDAOobj.sendSaveTransactionEmails(crNumber, moduleName, tomailids, userName, isEditMail, companyid);
                }
            }
            if (company.getCountry() != null && company.getCountry().getID().equals(Constants.INDIA_COUNTRYID) && !StringUtil.isNullOrEmpty(paramJobj.optString("linkinvoiceids"))) {
                String[] linkinvoiceids = paramJobj.optString("linkinvoiceids").split(",");
                if (linkinvoiceids != null && linkinvoiceids.length > 0) {
                    for (String invoiceid : linkinvoiceids) {
                        JSONObject json = new JSONObject();
                        json.put("creditnoteid", creditnote.getID());
                        if (creditnote.getVendor() != null) {
                            json.put("goodsReceipt", invoiceid);
                        } else {
                            json.put("invoiceid", invoiceid);
                        }
                        accCreditNoteDAOobj.saveCreditNoteInvoiceMappingInfo(json);
                    }
                }
            }
            ll.add(oldjeid);
            ll.add(creditnote);
            ll.add(approvalStatusLevel);
            ll.add(mailParams);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveCreditNote : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveCreditNote : " + ex.getMessage(), ex);
        }
        return ll;
    }

    private List saveCreditNoteRowsOW2(HashMap GlobalParams, JSONObject paramJobj, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException, AccountingException {
        List resultlist = new ArrayList();
        double cnamount = 0.0;
        HashSet cndetails = new HashSet();
        HashSet cnTaxEntryDetails = new HashSet();
        HashSet jedetails = new HashSet();
        JournalEntryDetail jed;
        boolean reloadInventory = false;
        String customerAccountId = paramJobj.optString("customerAccountId");
        String countryId = company.getCountry() != null ? company.getCountry().getID() : null;
        Boolean isCopy = paramJobj.optBoolean("isCopy",false);  
        int i = 0;
        String details = paramJobj.optString("details");
        if (!StringUtil.isNullOrEmpty(details)) {
            JSONArray jArr = new JSONArray(details);
            for (int iter = 0; iter < jArr.length(); iter++) {
                JSONObject jobj = jArr.getJSONObject(iter);
                String CreditNoteDetailID = StringUtil.generateUUID();
                CreditNoteDetail row = new CreditNoteDetail();
                row.setSrno(i + 1);
                row.setID(CreditNoteDetailID);
                row.setTotalDiscount(0.00);
                row.setCompany(company);
                row.setMemo(StringUtil.DecodeText(jobj.optString("description")));
                CreditNoteTaxEntry taxEntry = new CreditNoteTaxEntry();
                String CreditNoteTaxID = StringUtil.generateUUID();
                taxEntry.setID(CreditNoteTaxID);
                taxEntry.setRateIncludingGst(jobj.optDouble("rateIncludingGst"));
                String sales_accid = "";
                int cntype = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.cntype, null)) ? 1 : Integer.parseInt(paramJobj.optString(Constants.cntype));
                int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                boolean isdebit = jobj.has("debit") ? Boolean.parseBoolean(jobj.optString("debit", "true")) : true;
                if (cntype == 1 || cntype == 4 || cntype == 2) {//CN against vendor
                    if (!StringUtil.isNullOrEmpty(jobj.optString("accountid"))) {
                        sales_accid = jobj.optString("accountid");
                    } else {
                        throw new AccountingException(messageSource.getMessage("acc.field.Nodebitaccountselected", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                    }
                } else {
                    KwlReturnObject dscresult = this.accAccountDAOobj.getAccountFromName(company.getCompanyID(), Constants.SALES_ACCOUNT);
                    List ll = dscresult.getEntityList();

                    if (ll.size() == 1) {
                        sales_accid = ((Account) ll.get(0)).getID();
                    } else {
                        throw new AccountingException(messageSource.getMessage("acc.field.Nosalesaccountfound", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                    }
                }
                if (isdebit) {
                    /*
                     * calculate cnamount when including GST check is enable
                     */
                    if (paramJobj.optBoolean("includingGST", false)) {
                        cnamount += Double.parseDouble(jobj.optString("rateIncludingGst"));
                    } else {
                        cnamount += Double.parseDouble(jobj.optString("dramount"));
                    }
                } else {
                    if (paramJobj.optBoolean("includingGST", false)) {
                        cnamount -= Double.parseDouble(jobj.optString("rateIncludingGst"));
                    } else {
                        cnamount -= Double.parseDouble(jobj.optString("dramount"));

                    }
                }

                double amount =0;
                if (paramJobj.optBoolean("includingGST", false)) {
                    amount = (Double.parseDouble(jobj.optString("rateIncludingGst")));
                } else {
                    amount = (Double.parseDouble(jobj.optString("dramount")));
                }
                JSONObject jedjson = new JSONObject();
                JSONObject sepatratedjedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", company.getCompanyID());
                jedjson.put("amount", amount);
                jedjson.put("accountid", sales_accid);
                jedjson.put("debit", isdebit);
                jedjson.put("jeid", je.getID());
                jedjson.put("description", jobj.optString("description"));
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
                // Add Custom fields details 
                if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                    /*
                     * Posting additional jedetail against control account for
                     * matching balance sheet in advance search.
                     */
                    sepatratedjedjson = new JSONObject();
                    sepatratedjedjson.put("srno", jedetails.size() + 1);
                    sepatratedjedjson.put("companyid", company.getCompanyID());
                    sepatratedjedjson.put("amount", amount);
                    sepatratedjedjson.put("accountid", customerAccountId);
                    sepatratedjedjson.put("debit", !isdebit);
                    sepatratedjedjson.put("jeid", je.getID());
                    sepatratedjedjson.put("mainjedid", jed.getID());
                    sepatratedjedjson.put("description", jobj.optString("description"));
                    sepatratedjedjson.put(Constants.ISSEPARATED, true);
                    KwlReturnObject separatedjedresult = accJournalEntryobj.addJournalEntryDetails(sepatratedjedjson);
                    JournalEntryDetail pmAmountJed = (JournalEntryDetail) separatedjedresult.getEntityList().get(0);
                    jedetails.add(pmAmountJed);

                    JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                    customrequestParams.put("modulerecid", pmAmountJed.getID());
                    customrequestParams.put("recdetailId", taxEntry.getID());
                    customrequestParams.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                    customrequestParams.put("companyid", company.getCompanyID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        JSONObject tempjedjson = new JSONObject();
                        tempjedjson.put("accjedetailcustomdata", pmAmountJed.getID());
                        tempjedjson.put("jedid", pmAmountJed.getID());
                        accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                    }

                    customrequestParams.put("modulerecid", jed.getID());
                    customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        JSONObject tempjedjson = new JSONObject();
                        tempjedjson.put("accjedetailcustomdata", jed.getID());
                        tempjedjson.put("jedid", jed.getID());
                        jedresult = accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                    }
                }
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                row.setTotalJED(jed);
                taxEntry.setTotalJED(jed);

                // Entering data for Tax Entry
                String rowtaxid = jobj.optString("prtaxid", "");
                double rowtaxamount = 0d;
                double gstCurrencyRate = 0d;
                String rowTaxJeId = "";
                KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid);
                Tax rowtax = (Tax) txresult.getEntityList().get(0);
                if (rowtax != null) {
                    rowtaxamount = Double.parseDouble(jobj.optString("taxamount", "0.0"));
                    if (!StringUtil.isNullOrEmpty(jobj.optString("gstCurrencyRate", "0.0"))) {
                        gstCurrencyRate = Double.parseDouble(jobj.optString("gstCurrencyRate", "0.0"));
                    }
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", company.getCompanyID());
                    jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, company.getCompanyID()));
                    jedjson.put("accountid", rowtax.getAccount().getID());
                    jedjson.put("debit", isdebit);
                    jedjson.put("jeid", je.getID());
                    jedjson.put("description", jobj.optString("description"));
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
//                      Add Custom fields details 
                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", "")) && !jobj.optString("customfield", "").equals("[{}]")) {
                        /*
                         * Posting additional jedetail against tax account
                         * for matching balance sheet in advance search.
                         */
                        sepatratedjedjson = new JSONObject();
                        sepatratedjedjson.put("srno", jedetails.size() + 1);
                        sepatratedjedjson.put("companyid", company.getCompanyID());
                        sepatratedjedjson.put("amount", authHandler.formattedAmount(rowtaxamount, company.getCompanyID()));
                        sepatratedjedjson.put("accountid", customerAccountId);
                        sepatratedjedjson.put("debit", !isdebit);
                        sepatratedjedjson.put("jeid", je.getID());
                        sepatratedjedjson.put("mainjedid", jed.getID());
                        sepatratedjedjson.put("description", jobj.optString("description"));
                        sepatratedjedjson.put(Constants.ISSEPARATED, true);
                        jedresult = accJournalEntryobj.addJournalEntryDetails(sepatratedjedjson);
                        JournalEntryDetail pmTaxAmountJed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(pmTaxAmountJed);

                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                        customrequestParams.put("modulerecid", pmTaxAmountJed.getID());
                        customrequestParams.put("recdetailId", taxEntry.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                        customrequestParams.put("companyid", company.getCompanyID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            JSONObject tempjedjson = new JSONObject();
                            tempjedjson.put("accjedetailcustomdata", pmTaxAmountJed.getID());
                            tempjedjson.put("jedid", pmTaxAmountJed.getID());
                            accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                        }

                        customrequestParams.put("modulerecid", jed.getID());
                        customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            JSONObject tempjedjson = new JSONObject();
                            tempjedjson.put("accjedetailcustomdata", jed.getID());
                            tempjedjson.put("jedid", jed.getID());
                            jedresult = accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                        }
                    }
                    rowTaxJeId = jed.getID();

                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    row.setGstJED(jed);
                    taxEntry.setGstJED(jed);
                    if (isdebit) {
                        cnamount += rowtaxamount;
                    } else {
                        cnamount -= rowtaxamount;
                    }

                }
                if (iter == 0) {// create  cndetail entry only once in this case i.e if multitple accounts are linked.
                    cndetails.add(row);
                }

                KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), sales_accid);
                Account account = (Account) accountresult.getEntityList().get(0);

                taxEntry.setAccount(account);
                taxEntry.setAmount((Double.parseDouble(jobj.optString("dramount"))));
                taxEntry.setCompany(company);
                taxEntry.setDescription(StringUtil.DecodeText(jobj.optString("description")));
                if (!StringUtil.isNullOrEmpty(jobj.optString("reason", ""))) {
                    MasterItem reason = (MasterItem) kwlCommonTablesDAOObj.getClassObject(MasterItem.class.getName(), jobj.optString("reason", ""));
                    taxEntry.setReason(reason);
                }
                taxEntry.setIsForDetailsAccount(true);
                taxEntry.setDebitForMultiCNDN(isdebit);
                taxEntry.setTax(rowtax);
                taxEntry.setTaxJedId(rowTaxJeId);
                taxEntry.setTaxamount(rowtaxamount);
                taxEntry.setGstCurrencyRate(gstCurrencyRate);
                taxEntry.setSrNoForRow(srNoForRow);
                taxEntry.setProductid(jobj.optString("productid", null));
                if (!StringUtil.isNullOrEmpty(countryId) && countryId.equalsIgnoreCase("" + Constants.indian_country_id) && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                    JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                    double termAmount = 0.0;
                    for (int j = 0; j < termsArray.length(); j++) {
                        HashMap<String, Object> purchaseOrderDetailsTermsMap = new HashMap<>();
                        JSONObject termObject = termsArray.getJSONObject(j);
                        if (termObject.has("termid")) {
                            purchaseOrderDetailsTermsMap.put("term", termObject.get("termid"));
                        }
                        if (termObject.has("termamount")) {
                            purchaseOrderDetailsTermsMap.put("termamount", termObject.optDouble("termamount",0.0));
                            termAmount+= termObject.optDouble("termamount",0.0);
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
                        if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                            purchaseOrderDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                            if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                if (termObject.getInt("taxtype") == 0) { // If Flat
                                    purchaseOrderDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                } else { // Else Percentage
                                    purchaseOrderDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                }
                            }
                        }
                        if (termObject.has("id")) {
                            purchaseOrderDetailsTermsMap.put("id", isCopy ? "" : termObject.get("id"));
                        }
                        purchaseOrderDetailsTermsMap.put("creditNoteTaxEntry", taxEntry.getID());
                        purchaseOrderDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                        purchaseOrderDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                        purchaseOrderDetailsTermsMap.put("userid", paramJobj.optString(Constants.useridKey));
                        purchaseOrderDetailsTermsMap.put("product", termObject.opt("productid"));
                        purchaseOrderDetailsTermsMap.put("createdOn", new Date());
                        KwlReturnObject cnDetailTermMapResult = accCreditNoteDAOobj.saveCreditNoteDetailTermMap(purchaseOrderDetailsTermsMap);
                        CreditNoteDetailTermMap creditNoteDetailTermMap = cnDetailTermMapResult.getEntityList() != null && cnDetailTermMapResult.getEntityList().size() > 0 ? (CreditNoteDetailTermMap) cnDetailTermMapResult.getEntityList().get(0) : null;
                        if (creditNoteDetailTermMap != null && creditNoteDetailTermMap.getEntitybasedLineLevelTermRate() != null && creditNoteDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms() != null && creditNoteDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount() != null) {
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", termObject.optDouble("termamount",0.0));
                            jedjson.put("accountid", creditNoteDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                            jedjson.put("debit", isdebit);
                            jedjson.put("jeid", je.getID());
                            jedjson.put("description", jobj.optString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                            if (isdebit) {
                                cnamount += termObject.optDouble("termamount",0.0);
                            } else {
                                cnamount -= termObject.optDouble("termamount",0.0);
                            }
                        }
                    }
                    taxEntry.setTermAmount(termAmount);
                }
                if (!StringUtil.isNullOrEmpty(countryId) && countryId.equalsIgnoreCase("" + Constants.indian_country_id)) {
                    /**
                     * Save GST History Customer/Vendor data.
                     */
                    jobj.put("detaildocid", taxEntry.getID());
                    jobj.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                    fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jobj);
                }
                cnTaxEntryDetails.add(taxEntry);

            }
        } else {
            CreditNoteDetail row = new CreditNoteDetail();
            String CreditNoteDetailID = StringUtil.generateUUID();
            row.setID(CreditNoteDetailID);
            row.setSrno(i + 1);
            row.setTotalDiscount(0.00);
            row.setCompany(company);
            row.setMemo(paramJobj.optString("memo"));

            String sales_accid = "";
            int cntype = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.cntype, null)) ? 1 : Integer.parseInt(paramJobj.optString(Constants.cntype, null));
            if (cntype == 4 || cntype == 2) {//CN against vendor
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("reverseaccid", null))) {
                    sales_accid = paramJobj.optString("reverseaccid");
                } else {
                    throw new AccountingException(messageSource.getMessage("acc.field.Nodebitaccountselected", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
            } else {
                KwlReturnObject dscresult = this.accAccountDAOobj.getAccountFromName(company.getCompanyID(), Constants.SALES_ACCOUNT);
                List ll = dscresult.getEntityList();

                if (ll.size() == 1) {
                    sales_accid = ((Account) ll.get(0)).getID();
                } else {
                    throw new AccountingException(messageSource.getMessage("acc.field.Nosalesaccountfound", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
            }
            cnamount = Double.parseDouble(paramJobj.optString("amount"));//jobj.getDouble("discamount");
            cndetails.add(row);

            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", (cnamount));
            jedjson.put("accountid", sales_accid);
            jedjson.put("debit", true);
            jedjson.put("jeid", je.getID());
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
        }
        resultlist.add(cnamount);  //resultlist.add(totalAmount + totalTax);
        resultlist.add(cnamount);
        resultlist.add(cndetails);
        resultlist.add(jedetails);
        resultlist.add(reloadInventory);
        resultlist.add(cnTaxEntryDetails);
        return resultlist;
    }

    public List saveCreditNoteGstRows1(JSONObject paramJobj, String companyId, JournalEntry je, double externalCurrencyRate,CreditNote creditNote) throws ServiceException, AccountingException, SessionExpiredException, ParseException {
        List returnList = new ArrayList();
        Set rows = new HashSet();
        HashSet cndetails = new HashSet();
        HashSet jedetails = new HashSet();
        HashSet dnTaxEntryDetails = new HashSet();
        double totalDiscountAmt = 0;
        double totalDNAmt = 0;
        double totalDNAmtExludingTax = 0;
        try {

            GoodsReceiptDetail id = null;
            InvoiceDetail invoiceDetail = null;
            KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) comp.getEntityList().get(0);
            int countryid = Integer.parseInt(company.getCountry().getID());
            int cntype = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.cntype, null)) ? 1 : Integer.parseInt(paramJobj.optString(Constants.cntype));

            String globalTaxID = paramJobj.optString("taxid");
            double globalTaxPercent = 0;
            if (!StringUtil.isNullOrEmpty(globalTaxID)) {
                globalTaxPercent = StringUtil.isNullOrEmpty(paramJobj.getString("globalTaxPercent")) ? 0 : Double.parseDouble(paramJobj.getString("globalTaxPercent"));
            }
            JSONArray jArr = new JSONArray(paramJobj.optString(Constants.detail));
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> dodDataMap = new HashMap<String, Object>();

                if(jobj.has("srno")) {
                    dodDataMap.put("srno", jobj.getInt("srno"));
                }

                dodDataMap.put(Constants.companyKey, companyId);
                dodDataMap.put("cnId", creditNote.getID());
                dodDataMap.put(Constants.productid, jobj.getString(Constants.productid));

                if (jobj.has("priceSource") && jobj.get("priceSource") != null) {
                    dodDataMap.put("priceSource", !StringUtil.isNullOrEmpty(jobj.optString("priceSource")) ? StringUtil.DecodeText(jobj.getString("priceSource")) : "");
                }

                String linkMode = paramJobj.optString("fromLinkCombo");

                dodDataMap.put("description", jobj.getString("description"));

                dodDataMap.put("partno", jobj.getString("partno"));

                double actquantity = jobj.getDouble("quantity");
                double dquantity = jobj.getDouble("dquantity");
                double baseuomrate = 1;
                if (jobj.has("baseuomrate") && jobj.get("baseuomrate") != null) {
                    baseuomrate = jobj.getDouble("baseuomrate");
                }
                dodDataMap.put("quantity", actquantity);
                dodDataMap.put("returnquantity", dquantity);
                dodDataMap.put("baseuomrate", baseuomrate);
                if (jobj.has("uomid")) {
                    dodDataMap.put("uomid", jobj.getString("uomid"));
                }
                dodDataMap.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(actquantity, baseuomrate, companyId));
                dodDataMap.put("baseuomreturnquantity", authHandler.calculateBaseUOMQuatity(dquantity, baseuomrate, companyId));

                double receivedQty = dquantity*baseuomrate;

                dodDataMap.put("remark", jobj.optString("remark"));
                dodDataMap.put("reason", jobj.optString("reason"));

                String rowtaxid = "";
                if (!StringUtil.isNullOrEmpty(jobj.optString("prtaxid", null)) && jobj.optString("prtaxid").equalsIgnoreCase("None")) {
                    rowtaxid = null;
                } else {
                    rowtaxid = jobj.optString("prtaxid", null);
                }
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    double rowtaxamountFromJS = StringUtil.getDouble(jobj.getString("taxamount"));

                    if (rowtax == null) {
                        throw new AccountingException("The Tax code(s) used in this transaction has been deleted.");//messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    } else {
                        dodDataMap.put("prtaxid", rowtaxid);
                        dodDataMap.put("taxamount", rowtaxamountFromJS);
                    }
                }

                int discountispercent = jobj.optInt("discountispercent", 1);

                double prdiscount = jobj.optDouble("prdiscount", 0);

                if (jobj.has("prdiscount") && jobj.get("prdiscount") != null) {
                    dodDataMap.put("discount", prdiscount);
                }
                if (jobj.has("discountispercent") && jobj.get("discountispercent") != null) {
                    dodDataMap.put("discountispercent", discountispercent);
                }

                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore"))) {
                    dodDataMap.put("invstoreid", jobj.optString("invstore"));
                } else {
                    dodDataMap.put("invstoreid", "");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation"))) {
                    dodDataMap.put("invlocationid", jobj.optString("invlocation"));
                } else {
                    dodDataMap.put("invlocationid", "");
                }

                if (!StringUtil.isNullOrEmpty(linkMode)) {
                    if (linkMode.equalsIgnoreCase(Constants.Goods_Receipt) || linkMode.equalsIgnoreCase("Consignment Goods Receipt") || linkMode.equalsIgnoreCase("Asset Goods Receipt")) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrderDetails.class.getName(), jobj.getString("rowid"));
                        GoodsReceiptOrderDetails sod = (GoodsReceiptOrderDetails) rdresult.getEntityList().get(0);
                        dodDataMap.put("GoodReceiptDetail", sod);
                    } else if (linkMode.equalsIgnoreCase("Vendor Invoice") || linkMode.equalsIgnoreCase("Purchase Invoice")) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), jobj.getString("rowid"));
                        id = (GoodsReceiptDetail) rdresult.getEntityList().get(0);
                        dodDataMap.put("InvoiceDetail", id);
                    } else if (linkMode.equalsIgnoreCase("Sales Invoice")) {
                        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), jobj.optString("rowid"));
                        invoiceDetail = (InvoiceDetail) returnObject.getEntityList().get(0);
                        dodDataMap.put("salesInvoiceDetail", invoiceDetail);
                    }
                }

                double unitPrice = 0;
                if (jobj.has("rate")) {
                    dodDataMap.put("rate", jobj.optDouble("rate",0));
                    unitPrice = jobj.optDouble("rate",0);
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("recTermAmount"))) {
                    dodDataMap.put("recTermAmount", jobj.optString("recTermAmount"));
                }

                KwlReturnObject result = accCreditNoteDAOobj.saveCreditNoteGstDetails(dodDataMap);
                CreditNoteAgainstVendorGst row = (CreditNoteAgainstVendorGst) result.getEntityList().get(0);

                
                // Create Debit Nore
                String purchase_accid = "";
                KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString(Constants.productid));
                Product product = (Product) proresult.getEntityList().get(0);
                double discountVal = 0d;
                double totalAmt = unitPrice * receivedQty;
                if (discountispercent == 1) {
                    discountVal = totalAmt * prdiscount / 100;
                } else {
                    discountVal = prdiscount;
                }
                totalDiscountAmt += discountVal;

                double rowtaxamount = 0d;
                double amountExcludingTax = 0;
//                    /*
//                     * Here handling three cases #1 tax given at line level #2
//                     * tax given at global level #3 No tax is given. When tax
//                     * given at line level amount comes with tax When tax given
//                     * at global level amount comes without tax
//                     */
                Tax rowtax = null;
                    double dnRowAmount=jobj.optDouble("amount", 0);
                    if(!StringUtil.isNullOrEmpty(globalTaxID)){//when tax given at global Level
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), globalTaxID);
                    rowtax = (Tax) txresult.getEntityList().get(0);
                    amountExcludingTax = dnRowAmount;// it comes excluding tax
                        rowtaxamount= authHandler.round(dnRowAmount * (globalTaxPercent/100), companyId);
                        totalDNAmt += amountExcludingTax+rowtaxamount;
                    totalDNAmtExludingTax += amountExcludingTax;
                    } else if(!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid);
                    rowtax = (Tax) txresult.getEntityList().get(0);
                    totalDNAmt += dnRowAmount;
                    rowtaxamount = jobj.optDouble("taxamount", 0);
                    amountExcludingTax = dnRowAmount - rowtaxamount;
                    totalDNAmtExludingTax += amountExcludingTax;
                } else {// No tax applied
                    totalDNAmt += dnRowAmount;
                    rowtaxamount = 0;
                        amountExcludingTax = dnRowAmount ;
                    totalDNAmtExludingTax += dnRowAmount;
                }
                    if(id != null && countryid == Constants.indian_country_id && id.getPurchaseJED() != null){//id is Goodsreceiptdetailid
                    purchase_accid = id.getPurchaseJED().getAccount().getID();//account used in goodsreceiptdetail
                } else if (cntype == Constants.CreditNoteForOvercharge) {
                    purchase_accid = product.getSalesAccount().getID();//ERM-778
                    }else{
                    purchase_accid = product.getPurchaseAccount().getID();
                }
                CreditNoteTaxEntry taxEntry = new CreditNoteTaxEntry();
                String DebitNoteTaxID = StringUtil.generateUUID();
                taxEntry.setID(DebitNoteTaxID);
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put(Constants.companyKey, company.getCompanyID());
                    jedjson.put("amount",amountExcludingTax+discountVal);
                jedjson.put("accountid", purchase_accid);
                jedjson.put("debit", true);
                jedjson.put("jeid", je.getID());
                jedjson.put("description", "");
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                JournalEntryDetail jedTotal = (JournalEntryDetail) jedresult.getEntityList().get(0);//ERP-17888                    
                jedetails.add(jed);
                row.setJedid(jed);

                // Add Custom fields details 
                if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                    JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                    customrequestParams.put("modulerecid", jed.getID());
                    customrequestParams.put("recdetailId", taxEntry.getID());
                    customrequestParams.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                    customrequestParams.put("companyid", company.getCompanyID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        JSONObject tempjedjson = new JSONObject();
                        tempjedjson.put("accjedetailcustomdata", jed.getID());
                        tempjedjson.put("jedid", jed.getID());
                        jedresult = accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                    }
                }
                /**
                 * Save Credit Note Overcharge/Undercharge Line level Terms details
                 */
                if (countryid == Constants.USA_country_id && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                    JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                    double termAmount = 0.0;
                    for (int j = 0; j < termsArray.length(); j++) {
                        HashMap<String, Object> CNDetailsTermsMap = new HashMap<>();
                        JSONObject termObject = termsArray.getJSONObject(j);
                        if (termObject.has("termid")) {
                            CNDetailsTermsMap.put("term", termObject.get("termid"));
                        }
                        if (termObject.has("termamount")) {
                            CNDetailsTermsMap.put("termamount", termObject.optDouble("termamount", 0.0));
                            termAmount += termObject.optDouble("termamount", 0.0);
                        }
                        if (termObject.has("termpercentage")) {
                            CNDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                        }
                        if (termObject.has("purchasevalueorsalevalue")) {
                            CNDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                        }
                        if (termObject.has("deductionorabatementpercent")) {
                            CNDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                        }
                        if (termObject.has("assessablevalue")) {
                            CNDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                        }
                        if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                            CNDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                            if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                if (termObject.getInt("taxtype") == 0) { // If Flat
                                    CNDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                } else { // Else Percentage
                                    CNDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                }
                            }
                        }
                        if (termObject.has("id")) {
                            CNDetailsTermsMap.put("id", termObject.get("id"));
                        }
                        CNDetailsTermsMap.put("creditnotedetail", row.getID());
                        CNDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                        CNDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                        CNDetailsTermsMap.put("userid", paramJobj.optString(Constants.useridKey));
                        CNDetailsTermsMap.put("product", termObject.opt("productid"));
                        CNDetailsTermsMap.put("createdOn", new Date());
                        KwlReturnObject cnDetailTermMapResult = accCreditNoteDAOobj.saveCreditNoteDetailTermMap(CNDetailsTermsMap);
                        CreditNoteDetailTermMap creditNoteDetailTermMap = cnDetailTermMapResult.getEntityList() != null && cnDetailTermMapResult.getEntityList().size() > 0 ? (CreditNoteDetailTermMap) cnDetailTermMapResult.getEntityList().get(0) : null;
                        if (creditNoteDetailTermMap != null && creditNoteDetailTermMap.getEntitybasedLineLevelTermRate() != null && creditNoteDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms() != null && creditNoteDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount() != null) {
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", termObject.optDouble("termamount", 0.0));
                            jedjson.put("accountid", creditNoteDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                            jedjson.put("debit", true);
                            jedjson.put("jeid", je.getID());
                            jedjson.put("description", jobj.optString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                            totalDNAmt += termObject.optDouble("termamount", 0.0);
                        }
                    }
                    row.setRowTaxAmount(termAmount);
                }
                String rowTaxJeId = "";
                if (rowtax != null) {
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put(Constants.companyKey, company.getCompanyID());
                    jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyId));
                    jedjson.put("accountid", rowtax.getAccount().getID());
                    jedjson.put("debit", true);
                    jedjson.put("jeid", je.getID());
                    jedjson.put("description", "");
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                    row.setGstJED(jed);
                    rowTaxJeId = jed.getID();
                }
                KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), purchase_accid);
                Account account = (Account) accountresult.getEntityList().get(0);

                taxEntry.setAccount(account);
                taxEntry.setAmount(amountExcludingTax);
                taxEntry.setCompany(company);
                if (!StringUtil.isNullOrEmpty(jobj.optString("reason", ""))) {
                    KwlReturnObject reasonresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), jobj.optString("reason", ""));
                    MasterItem reason = (MasterItem) reasonresult.getEntityList().get(0);
                    taxEntry.setReason(reason);
                }
                taxEntry.setDescription("");
                taxEntry.setIsForDetailsAccount(true);
                taxEntry.setDebitForMultiCNDN(false);
                taxEntry.setTax(rowtax);
                taxEntry.setTaxJedId(rowTaxJeId);
                taxEntry.setTaxamount(rowtaxamount);
                taxEntry.setTotalJED(jedTotal); //ERP-17888
                dnTaxEntryDetails.add(taxEntry);
                rows.add(row);

            }
            returnList.add(rows);
            returnList.add(cndetails);
            returnList.add(jedetails);
            returnList.add(dnTaxEntryDetails);
            returnList.add(totalDNAmt);
            returnList.add(totalDNAmtExludingTax);
            returnList.add(totalDiscountAmt);
        }  catch (JSONException ex) {
            throw ServiceException.FAILURE("savePurchaseReturnRows : " + ex.getMessage(), ex);
        }
        return returnList;
    }
    private List saveCreditNoteRows2(HashMap GlobalParams, JSONObject paramJobj, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException, AccountingException {
        List resultlist = new ArrayList();
        double totalAmount = 0;
        double totalTax = 0, prodTax = 0;
        double discAccAmount = 0.0, discTotal = 0;
        HashSet cndetails = new HashSet();
        HashSet jedetails = new HashSet();

        JournalEntryDetail jed;
        KwlReturnObject result;

        boolean reloadInventory = false;
        String currencyid = (paramJobj.optString("currencyid", null) == null ? currency.getCurrencyID() : paramJobj.optString("currencyid"));
        JSONArray jArr = new JSONArray(paramJobj.optString("productdetails"));
        List list = new ArrayList();
        boolean includeTax = StringUtil.getBoolean(paramJobj.optString("includetax"));
        double totalInvoiceDiscount = StringUtil.getDouble(paramJobj.optString("totalInvoiceDiscount"));
        double debitAmount = 0, creditAmount = 0;
        String cntype = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.cntype, null)) ? "1" : paramJobj.optString(Constants.cntype);

        for (int i = 0; i < jArr.length(); i++) {
            double taxamount = 0;
            double amount = 0, amount1 = 0;
            prodTax = 0;
            discAccAmount = 0.0;
            JSONObject jobj = jArr.getJSONObject(i);
            CreditNoteDetail row = new CreditNoteDetail();
            String CreditNoteDetailID = StringUtil.generateUUID();
            row.setID(CreditNoteDetailID);
            row.setSrno(i + 1);
            double disc = jobj.getDouble("discamount");
            double rowDiscountpercent = jobj.getDouble("prdiscount");
            double rowDiscount = 0;
            discTotal = discTotal + disc;
            row.setTotalDiscount(0.00);
            row.setCompany(company);
            row.setMemo(paramJobj.optString("memo"));
            row.setQuantity(jobj.getDouble("remquantity"));
            if (!StringUtil.isNullOrEmpty(jobj.getString("gridRemark"))) {
                row.setRemark(jobj.getString("gridRemark"));
            }
            String accountId = preferences.getDiscountGiven().getID();
            if (!StringUtil.isNullOrEmpty(jobj.getString("accountId"))) {
                accountId = jobj.getString("accountId");
            }
            if (cntype.equals("3")) {
                row.setPaidinvflag(1);
            }
            result = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
            InvoiceDetail invoiceRow = (InvoiceDetail) result.getEntityList().get(0);

            boolean updateInventoryFlag = invoiceRow.getInventory().isInvrecord();

            if (preferences.isWithInvUpdate()) {
                updateInventoryFlag = getInvoiceStatusForDO(invoiceRow);
            }

            if (preferences.isInventoryAccountingIntegration() && preferences.isUpdateInvLevel()) {
                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore"))) {
                    row.setInvstoreid(jobj.optString("invstore"));
                } else {
                    row.setInvstoreid("");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation"))) {
                    row.setInvlocid(jobj.optString("invlocation"));
                } else {
                    row.setInvlocid("");
                }
            }

            row.setInvoiceRow(invoiceRow);
            Product product = invoiceRow.getInventory().getProduct();
            result = accountingHandlerDAOobj.getObject(Account.class.getName(), product.getSalesReturnAccount().getID());
            Account account = (Account) result.getEntityList().get(0);

            double percent = 0;
            if (invoiceRow.getInvoice().getTax() != null) {
                KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), invoiceRow.getInvoice().getCreationDate(), invoiceRow.getInvoice().getTax().getID());
                percent = (Double) perresult.getEntityList().get(0);
            }

            amount = jobj.getDouble("discamount");
            if (jobj.getInt("typeid") > 0) {

///////////////////////////////////////////////   Reload Inventory
                reloadInventory = true;
                JSONObject inventoryjson = new JSONObject();
                inventoryjson.put("productid", product.getID());
//                inventoryjson.put("quantity", jobj.getInt("remquantity"));    
                double baseuomrate = 1;
                if (jobj.has("baseuomrate")) {
                    baseuomrate = jobj.getDouble("baseuomrate");
                }
                double quantity = jobj.getDouble("remquantity");
                inventoryjson.put("quantity", quantity);
                if (jobj.has("uomid")) {
                    inventoryjson.put("uomid", jobj.getString("uomid"));
                }
                inventoryjson.put("baseuomquantity", updateInventoryFlag ? authHandler.calculateBaseUOMQuatity(quantity, baseuomrate, company.getCompanyID()) : 0);
                inventoryjson.put("actquantity", updateInventoryFlag ? 0 : authHandler.calculateBaseUOMQuatity(quantity, baseuomrate, company.getCompanyID()));
                inventoryjson.put("baseuomrate", baseuomrate);
                inventoryjson.put("invrecord", updateInventoryFlag ? true : false);

                inventoryjson.put("description", jobj.optString("desc"));
                inventoryjson.put("carryin", true);
                inventoryjson.put("defective", true);
                inventoryjson.put("newinventory", false);
                inventoryjson.put("companyid", company.getCompanyID());
                inventoryjson.put("updatedate", authHandler.getDateOnlyFormat().parse(paramJobj.optString(Constants.creationdate)));
                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                Inventory inventory = (Inventory) invresult.getEntityList().get(0);
                row.setInventory(inventory);
///////////////////////////////////////////////   Reload Inventory Over

                result = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                InvoiceDetail compInvoiceRow = (InvoiceDetail) result.getEntityList().get(0);
                Product compProduct = compInvoiceRow.getInventory().getProduct();

                result = accountingHandlerDAOobj.getObject(Account.class.getName(), compProduct.getSalesReturnAccount().getID());
                Account compAccount = (Account) result.getEntityList().get(0);
                if (jobj.getInt("typeid") > 1) {

                    amount1 = jobj.getDouble("discamount");
                    list.add(compAccount);

                    if (disc > 0) {

/////////////////////////////////////////////    Total Tax
                        if (includeTax && i == jArr.length() - 1) {
                            taxamount = (discTotal - (totalInvoiceDiscount)) * percent / 100;
                            row.setTaxAmount(taxamount);
                            totalTax += taxamount;
                            if (includeTax && taxamount > 0) {
                                debitAmount += taxamount;
                                JSONObject jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put("companyid", company.getCompanyID());
                                jedjson.put("amount", taxamount);
                                jedjson.put("accountid", invoiceRow.getInvoice().getTax().getAccount().getID());
                                jedjson.put("debit", true);
                                jedjson.put("jeid", je.getID());
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed);
                            }
                        }
/////////////////////////////////////////////    Total Tax Over

/////////////////////////////////////////////    Total Discount if Exist
                        if (totalInvoiceDiscount > 0 && i == jArr.length() - 1) {
                            row.setTotalDiscount(totalInvoiceDiscount);
                            creditAmount += totalInvoiceDiscount;
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", totalInvoiceDiscount);
                            jedjson.put("accountid", preferences.getDiscountGiven().getID());
                            jedjson.put("debit", false);
                            jedjson.put("jeid", je.getID());
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                        }
/////////////////////////////////////////////    Total Discount if Exist Over

/////////////////////////////////////////////    Discount Row Added
                        JSONObject discjson = new JSONObject();
                        discjson.put("discount", disc);
                        discjson.put("inpercent", false);
                        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                        discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                        discjson.put("companyid", company.getCompanyID());
                        KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                        Discount discount = (Discount) dscresult.getEntityList().get(0);
                        row.setDiscount(discount);
                        cndetails.add(row);

/////////////////////////////////////////////    Discount Row Added Over
                    }
                }

/////////////////////////////////////////////    Product Tax
                if (invoiceRow.getTax() != null) {
                    /*
                     * Product level tax taken care of
                     */

                    KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), invoiceRow.getInvoice().getCreationDate(), invoiceRow.getTax().getID());
                    percent = (Double) perresult.getEntityList().get(0);
                    prodTax = percent * disc / (percent + 100);
                    debitAmount += prodTax;
//        	            amount = amount - prodTax;

                    JSONObject jedtaxjson = new JSONObject();
                    jedtaxjson.put("srno", jedetails.size() + 1);
                    jedtaxjson.put("companyid", company.getCompanyID());
                    jedtaxjson.put("amount", prodTax);
                    jedtaxjson.put("accountid", invoiceRow.getTax().getAccount().getID());
                    jedtaxjson.put("debit", true);
                    jedtaxjson.put("jeid", je.getID());
                    KwlReturnObject jedtaxresult = accJournalEntryobj.addJournalEntryDetails(jedtaxjson);
                    jed = (JournalEntryDetail) jedtaxresult.getEntityList().get(0);
                    jedetails.add(jed);

                }
/////////////////////////////////////////////    Product Tax  Over

/////////////////////////////////////////////    Discount Row Added Over
                if (rowDiscountpercent > 0) {
                    rowDiscount = (rowDiscountpercent * (amount - prodTax)) / (100 - rowDiscountpercent);
                    creditAmount += rowDiscount;
/////////////////////////////////////////////    Product Row Discount Added
                    JSONObject jedjson1 = new JSONObject();
                    jedjson1.put("srno", jedetails.size() + 1);
                    jedjson1.put("companyid", company.getCompanyID());
                    jedjson1.put("amount", rowDiscount);
                    jedjson1.put("accountid", preferences.getDiscountGiven().getID());
                    jedjson1.put("debit", false);
                    jedjson1.put("jeid", je.getID());
                    KwlReturnObject jedresult1 = accJournalEntryobj.addJournalEntryDetails(jedjson1);
                    jed = (JournalEntryDetail) jedresult1.getEntityList().get(0);
                    jedetails.add(jed);

/////////////////////////////////////////////    Product Row Discount Added Over
                }

                totalAmount = totalAmount + amount + taxamount;//  - totalInvoiceDiscount; // + prodTax;

                debitAmount += (amount + rowDiscount - prodTax);
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", company.getCompanyID());
                jedjson.put("amount", (amount + rowDiscount - prodTax));
                jedjson.put("accountid", accountId);
                jedjson.put("debit", true);
                jedjson.put("jeid", je.getID());
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);

            } else {

                discAccAmount = jobj.getDouble("discamount");

                if (includeTax && i == jArr.length() - 1) {
                    taxamount = (discTotal - (totalInvoiceDiscount)) * percent / 100;
                    row.setTaxAmount(taxamount);
                    totalTax += taxamount;
                    if (includeTax && taxamount > 0) {
                        debitAmount += taxamount;
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", company.getCompanyID());
                        jedjson.put("amount", taxamount);
                        jedjson.put("accountid", invoiceRow.getInvoice().getTax().getAccount().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", je.getID());
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                    }
                }

/////////////////////////////////////////////    Total Discount if Exist
                if (totalInvoiceDiscount > 0 && i == jArr.length() - 1) {
                    row.setTotalDiscount(totalInvoiceDiscount);
                    creditAmount += totalInvoiceDiscount;
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", company.getCompanyID());
                    jedjson.put("amount", totalInvoiceDiscount);
                    jedjson.put("accountid", preferences.getDiscountGiven().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", je.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
/////////////////////////////////////////////    Total Discount if Exist Over

                if (disc > 0) {
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", disc);
                    discjson.put("inpercent", false);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                    discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                    cndetails.add(row);
                }

                if (invoiceRow.getTax() != null) {
                    /*
                     * Product level tax taken care of
                     */

                    KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), invoiceRow.getInvoice().getCreationDate(), invoiceRow.getTax().getID());
                    percent = (Double) perresult.getEntityList().get(0);
                    prodTax = percent * discAccAmount / (percent + 100);
                    debitAmount += prodTax;
                    JSONObject jedtaxjson = new JSONObject();
                    jedtaxjson.put("srno", jedetails.size() + 1);
                    jedtaxjson.put("companyid", company.getCompanyID());
                    jedtaxjson.put("amount", prodTax);
                    jedtaxjson.put("accountid", invoiceRow.getTax().getAccount().getID());
                    jedtaxjson.put("debit", true);
                    jedtaxjson.put("jeid", je.getID());
                    KwlReturnObject jedtaxresult = accJournalEntryobj.addJournalEntryDetails(jedtaxjson);
                    jed = (JournalEntryDetail) jedtaxresult.getEntityList().get(0);
                    jedetails.add(jed);

                    /*
                     * Product level tax taken care of
                     */
                }

                if (rowDiscountpercent > 0) {
                    rowDiscount = (rowDiscountpercent * (amount - prodTax)) / (100 - rowDiscountpercent);
                    creditAmount += rowDiscount;
/////////////////////////////////////////////    Product Row Discount Added
                    JSONObject jedjson1 = new JSONObject();
                    jedjson1.put("srno", jedetails.size() + 1);
                    jedjson1.put("companyid", company.getCompanyID());
                    jedjson1.put("amount", rowDiscount);
                    jedjson1.put("accountid", preferences.getDiscountGiven().getID());
                    jedjson1.put("debit", false);
                    jedjson1.put("jeid", je.getID());
                    KwlReturnObject jedresult1 = accJournalEntryobj.addJournalEntryDetails(jedjson1);
                    jed = (JournalEntryDetail) jedresult1.getEntityList().get(0);
                    jedetails.add(jed);

/////////////////////////////////////////////    Product Row Discount Added Over
                }

                if (discAccAmount != 0.0) {
                    debitAmount += (discAccAmount + rowDiscount - prodTax);
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", company.getCompanyID());
                    jedjson.put("amount", (discAccAmount + rowDiscount - prodTax));
//                  jedjson.put("accountid", preferences.getDiscountGiven().getID());
                    jedjson.put("accountid", accountId);
                    jedjson.put("debit", true);
                    jedjson.put("jeid", je.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);

                }
                totalAmount += discAccAmount + taxamount; //  - totalInvoiceDiscount;
            }
        }

        resultlist.add(totalAmount - totalInvoiceDiscount);  //resultlist.add(totalAmount + totalTax);
        resultlist.add(discAccAmount);
        resultlist.add(cndetails);
        resultlist.add(jedetails);
        resultlist.add(reloadInventory);
        resultlist.add(debitAmount);
        return resultlist;
    }

    @Override
    public boolean getInvoiceStatusForDO(InvoiceDetail iDetail) throws ServiceException {
        //accCreditNoteDAOobj
        boolean updateInventoryFlag = false;
        KwlReturnObject idresult = accCreditNoteDAOobj.getDOIDFromInvoiceDetails(iDetail.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        if (ite1.hasNext()) {
            updateInventoryFlag = true;
        }
        return updateInventoryFlag;
    }

    @Override
    public List mapDebitTerms(String cnTerms, String creditNoteId, String userid) throws ServiceException {
        List ll = new ArrayList();
        try {
            JSONArray termsArr = new JSONArray(cnTerms);
            for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                JSONObject temp = termsArr.getJSONObject(cnt);
                HashMap<String, Object> termMap = new HashMap<String, Object>();
                termMap.put("term", temp.getString("id"));
                termMap.put("termamount", Double.parseDouble(temp.getString("termamount")));
                int percentage = 0;
                if (!StringUtil.isNullOrEmpty(temp.getString("termpercentage"))) {
                    percentage = Integer.parseInt(temp.getString("termpercentage"));
                }
                termMap.put("termpercentage", percentage);
                termMap.put("creditNoteId", creditNoteId);
                termMap.put(Constants.creationdate, new Date());
                termMap.put("userid", userid);
                accCreditNoteDAOobj.saveCreditNoteTermMap(termMap);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }

    @Override
    public void deleteJEArray(String oldjeid, String companyid) throws ServiceException, AccountingException, SessionExpiredException {
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
                result = accJournalEntryobj.permanentDeleteJournalEntry(oldjeid, companyid);
            }
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    //If Exception occured or payment completed  then delete entry from temporary table
    public void deleteTemporaryInvoicesEntries(String invoiceIdsArrary [],String companyid) {
        try {
            for (String invoiceId : invoiceIdsArrary) {
                accPaymentDAOobj.deleteUsedInvoiceOrCheque(invoiceId, companyid);
            }
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void deleteJEDetailsCustomData(String jeid) throws ServiceException {
        KwlReturnObject cap = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
        JournalEntry salesOrderDetails = (JournalEntry) cap.getEntityList().get(0);
        Set<JournalEntryDetail> journalEntryDetails = salesOrderDetails.getDetails();
        for (JournalEntryDetail journalEntryDetail : journalEntryDetails) {
            String jeDetailsId = journalEntryDetail.getID();
            KwlReturnObject jedresult1 = accJournalEntryobj.deleteJEDetailsCustomData(jeDetailsId);
        }
    }
    @Override
    public JSONObject importCreditNotesJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        try {
            String doAction = paramJobj.getString("do");

            if (doAction.compareToIgnoreCase("import") == 0) {
                jobj = importCreditNoteRecordsForCSV(paramJobj);
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
                Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return jobj;
    }

    public JSONObject importCreditNoteRecordsForCSV(JSONObject requestJobj) throws AccountingException, IOException, SessionExpiredException, JSONException {

        JSONObject returnObj = new JSONObject();
        String msg = "";
        int total = 0, failed = 0;
        String fileName = requestJobj.getString("filename");
        String companyID = requestJobj.getString(Constants.companyKey);
        String masterPreference = requestJobj.getString("masterPreference");
        boolean issuccess = true;
        boolean isAlreadyExist = false;
        boolean isRecordFailed = false;
        boolean isCustomer = true;
        boolean otherwise = false;
        boolean isIncludingGST = false;

        FileInputStream fileInputStream = null;
        CsvReader csvReader = null;
        JSONObject paramJobj = new JSONObject();
        JSONArray rows = new JSONArray();
        String prevInvNo = "";
        String entryNumber = "";
        String exceptionMSg="";
        int cntype =2;
        int srNoForRow =0;
        double subTotal = 0;

        try {
            String dateFormat = null, dateFormatId;
            dateFormatId = requestJobj.optString("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);

            if (!StringUtil.isNullOrEmpty(requestJobj.optString("againstVendor", null))) {
                isCustomer = false;
                cntype=4;
            }
            if (!StringUtil.isNullOrEmpty(requestJobj.optString("otherwise", null))) {
                otherwise = true;
                cntype=2;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");

            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode = extrareferences.isCurrencyCode();
            Boolean isDebitCreditAllow = extrareferences.isManyCreditDebit();

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
            double totalTransactionAmount=0d;
            StringBuilder failedRecords = new StringBuilder();
            StringBuilder singleCNFailedRecords = new StringBuilder();// CN with one row of failure record then all rows will be included in failure file
            int singleCNFailureRecoredCount = 0;//  count of total CN rows in import file
            Set<String> failureList = new HashSet<>(); // set of CN having failyure record's
            HashMap currencyMap = accSalesOrderServiceobj.getCurrencyMap(isCurrencyCode);

            while (csvReader.readRecord()) {
                String failureMsg = "";
                String[] recarr = csvReader.getValues();
                boolean isfailurerecord = false; // used to keep track CN Row  failure/correct

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
                                failureMsg += "Credit Note Number is not available. ";
                            }
                        } else {
                            failureMsg += "Credit Note Number column is not found. ";
                        }

                        Date creationDate = null;
                        if (columnConfig.containsKey(Constants.creationdate)) {
                            String creditNoteDateStr = recarr[(Integer) columnConfig.get(Constants.creationdate)].replaceAll("\"", "").trim();
                            authHandlerDAOObj.checkLockDatePeroid((Date)df.parse(creditNoteDateStr),companyID);
                            if (StringUtil.isNullOrEmpty(creditNoteDateStr)) {
                                failureMsg += "Credit Note Date is not available. ";
                            } else {
                                try {
                                    creationDate = df.parse(creditNoteDateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Credit Note Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        } else {
                            failureMsg += "Credit Note Date column is not found. ";
                        }

                        String costCenterID = "";
                        if (columnConfig.containsKey("costcenter")) {
                            String costCenterName = recarr[(Integer) columnConfig.get("costcenter")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(costCenterName)) {
                                costCenterID = accSalesOrderServiceobj.getCostCenterIDByName(costCenterName, companyID);
                                if (StringUtil.isNullOrEmpty(costCenterID)) {
                                    failureMsg += "Cost Center is not found for name " + costCenterName + ". ";
                                }
                            }
                        }

                        String customerID = "";
                        String custAccountID = "";

                        if (columnConfig.containsKey("customer")) {
                            String customerName = recarr[(Integer) columnConfig.get("customer")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(customerName)) {
                                Customer customer = null;
                                KwlReturnObject retObj = accCustomerDAOObj.getCustomerByName(customerName, companyID);
                                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                    customer = (Customer) retObj.getEntityList().get(0);
                                }
                                if (customer != null) {
                                    customerID = customer.getID();
                                    custAccountID = customer.getAccount().getID();
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.CustomerNameisnotavailable", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) + ". ";
                                }
                            } else {
                                failureMsg += messageSource.getMessage("acc.field.CustomerNameisnotavailable", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) + ".";
                            }
                        }

                        
                        String vendorName="";
                        String vendorId="";
                        String vendorAccountId="";
                        if (columnConfig.containsKey("vendor") && !isCustomer) {
                            vendorName = recarr[(Integer) columnConfig.get("vendor")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(vendorName)) {
                                Vendor vendor = null;
                                KwlReturnObject retObj = accVendorDAOObj.getVendorByName(vendorName, companyID);
                                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                    vendor = (Vendor) retObj.getEntityList().get(0);
                                }
                                if (vendor != null) {
                                    vendorId = vendor.getID();
                                    vendorAccountId = vendor.getAccount().getID();
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, Locale.forLanguageTag(requestJobj.optString(Constants.language)));
                                }
                            } else {
                                failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, Locale.forLanguageTag(requestJobj.optString(Constants.language)));
                            }
                        }

                        String memo = "";
                        if (columnConfig.containsKey("memo")) {
                            memo = recarr[(Integer) columnConfig.get("memo")].replaceAll("\"", "").trim();
                        }

                        String salesPersonID = "";
                        if (columnConfig.containsKey("salesperson")) {
                            String salesPersonName = recarr[(Integer) columnConfig.get("salesperson")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(salesPersonName)) {
                                salesPersonID = accSalesOrderServiceobj.getSalesPersonIDByName(salesPersonName, companyID);
                                if (StringUtil.isNullOrEmpty(salesPersonID)) {
                                    failureMsg += "Sales Person is not found for name " + salesPersonName + ". ";
                                }
                            }
                        }
                        String agentID = "";
                        if (columnConfig.containsKey("masteragent")) {
                            String agentName = recarr[(Integer) columnConfig.get("masteragent")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(agentName)) {
                                agentID = accPurchaseOrderModuleServiceObj.getAgentIDByName(agentName, companyID);
                                if (StringUtil.isNullOrEmpty(agentName)) {
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
                        /*
                         Added Including GST column 
                         */
                        if (columnConfig.containsKey("gstIncluded")) {
                            String isIncludingGSTStr = recarr[(Integer) columnConfig.get("gstIncluded")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(isIncludingGSTStr)) {
                                if (isIncludingGSTStr.equalsIgnoreCase("TRUE")) {
                                    isIncludingGST = true;
                                } else if (isIncludingGSTStr.equalsIgnoreCase("FALSE")) {
                                    isIncludingGST = false;
                                } else {
                                    failureMsg += "Format you entered for Include GST is not correct. It should be like \"TRUE\" or \"FALSE\". ";
                                }
                            }
                        }

                        /* Getting line level details */
                        
                        String accountID = "";
                        if (columnConfig.containsKey("account")) {
                            String accountName = recarr[(Integer) columnConfig.get("account")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(accountName)) {
                                Account account = getAccount(accountName, companyID, true);
                                if (account != null) {
                                    accountID = account.getID();
                                } else {
                                    failureMsg += "Account is not found for " + accountName + ".";
                                }
                            } else {
                                failureMsg += "Account is not available.";
                            }
                        } else {
                            failureMsg += "Account column is not found.";
                        }

                        /*
                         Added type column when 'Allow Many Credit-Debit in CN/DN' option is true from Account preferences.
                         */
                        boolean isDebit = true;
                        if (isDebitCreditAllow) {
                            if (columnConfig.containsKey("type")) {
                                String type = recarr[(Integer) columnConfig.get("type")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(type)) {
                                    failureMsg += "Account debit or credit type is not available. ";
                                } else {
                                    if (type.equalsIgnoreCase("debit")) {
                                        isDebit = true;
                                    } else if (type.equalsIgnoreCase("credit")) {
                                        isDebit = false;
                                    } else {
                                        failureMsg += "Account type value is not correct.Please give Type value as 'debit' or 'credit' ";
                                    }
                                }
                            } else {
                                failureMsg += "Account type column is not found. ";
                            }
                        }

                        
                        String transactionAmountStr = "";
                        double transactionAmount = 0d;
                        if (columnConfig.containsKey("amount")) {
                            transactionAmountStr = recarr[(Integer) columnConfig.get("amount")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(transactionAmountStr)) {
                                failureMsg += "Empty data found in Amount, cannot set empty data for Amount.";
                            } else {
                                try {
                                    transactionAmount = Double.parseDouble(transactionAmountStr);
                                    transactionAmount = authHandler.round(transactionAmount, companyID);
                                    if (transactionAmount <= 0) {
                                        failureMsg += "Amount can not be zero or negative.";
                                    }
                                } catch (NumberFormatException ex) {
                                    failureMsg += "Incorrect numeric value for Amount, Please ensure that value type of Amount matches with the Amount.";
                                }
                            }
                        }

                        
                        String taxID = "";
                        if (columnConfig.containsKey("taxid")) {
                            String taxCode = recarr[(Integer) columnConfig.get("taxid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(taxCode)) {
//                                Tax tax = getTaxByCode(taxCode, companyID);
                                Map taxMap = new HashMap<>();
                                taxMap.put(Constants.companyKey, companyID);
                                taxMap.put(Constants.TAXCODE, taxCode);
                                ArrayList taxList = importHandler.getTax(taxMap);
                                if (taxList.get(0) != null) {
                                    Tax tax = (Tax) taxList.get(0);
                                    taxID = tax.getID();
                                } else if (!StringUtil.isNullOrEmpty((String) taxList.get(2))) {
                                    failureMsg += (String) taxList.get(2) + taxCode;
                                }
                            }
                        }

                        String transactionTaxAmountStr = "";
                        double transactionTaxAmount = 0d;
                        if (columnConfig.containsKey("taxamount")) {
                            transactionTaxAmountStr = recarr[(Integer) columnConfig.get("taxamount")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(transactionTaxAmountStr)) {
                                try {
                                    transactionTaxAmount = Double.parseDouble(transactionTaxAmountStr);
                                    transactionTaxAmount = authHandler.round(transactionTaxAmount, companyID);
                                    if (transactionTaxAmount <= 0) {
                                        failureMsg += "Tax Amount can not be zero or negative.";
                                    }
                                } catch (NumberFormatException ex) {
                                    failureMsg += "Incorrect numeric value for Tax Amount, Please ensure that value type of Tax Amount matches with the Amount.";
                                }
                            }
                        }

                        String reasonID = "";
                        if (columnConfig.containsKey("reason")) {
                            String reason = recarr[(Integer) columnConfig.get("reason")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(reason)) {
                                reasonID = getReasonIDByName(reason, companyID);
                                if (StringUtil.isNullOrEmpty(reasonID)) {
                                    failureMsg += "Reason is not found for name " + reason + ". ";
                                }
                            }else {
                                failureMsg += "Reason field value is empty.";
                            }
                        }else {
                            failureMsg += "Reason column is not found.";
                        }

                        
                        String description = "";
                        if (columnConfig.containsKey("desc")) {
                            description = recarr[(Integer) columnConfig.get("desc")].replaceAll("\"", "").trim();
                        }

                        
                        // Creating Credit Note JSON
                        if (!prevInvNo.equalsIgnoreCase(entryNumber) || entryNumber.equalsIgnoreCase("")) {

                            //If failed Credit Note then increase failed count and append in failed records string
                            if (failureList.contains(prevInvNo)) {
                                if (singleCNFailureRecoredCount > 0) {
                                    //append record in failed records string
                                    failedRecords.append(singleCNFailedRecords);
                                }
                                //Increase failed records count
                                failed += singleCNFailureRecoredCount;
                                //reinitialize variables for next record
                                singleCNFailedRecords = new StringBuilder();
                                singleCNFailureRecoredCount = 0;
                            }
                            if (rows.length() > 0 && !isRecordFailed) {
                                if (authHandler.round(subTotal, companyID) <= 0) {
                                    String message = "Total amount should be greater than Zero.";
                                    String temString = singleCNFailedRecords.append(message).append("\"").toString();
                                    temString = temString.replaceAll("\"", "");
                                    singleCNFailedRecords = new StringBuilder();
                                    singleCNFailedRecords.append(temString);
                                    isRecordFailed = true;
                                }
                                if (!isRecordFailed) {
                                    paramJobj.put(Constants.detail, rows.toString());
                                    // for Delivery Order
                                    paramJobj.put("amount", authHandler.round(subTotal, companyID));
                                    paramJobj.put("details", rows.toString());
                                    paramJobj.put(Constants.PAGE_URL, requestJobj.optString(Constants.PAGE_URL));
                                    saveCreditNoteJSON(paramJobj);
                                }else {
                                    /**
                                     * If record failed then append it in failed
                                     * records string. Increase failed records
                                     * count.
                                     */
                                    failedRecords.append(singleCNFailedRecords);
                                    failed += singleCNFailureRecoredCount;
                                    failureList.add(prevInvNo);
                                }
                                //below variable are get initialized to give correct failure and sucess
                                singleCNFailedRecords = new StringBuilder();
                                singleCNFailureRecoredCount = 0;
                            }
                            prevInvNo = entryNumber;
                            totalTransactionAmount=0d;
                            srNoForRow=0;
                            // reset variables
                            paramJobj = new JSONObject();
                            rows = new JSONArray();
                            isRecordFailed = false;
                            isAlreadyExist = false;
                            subTotal=0;

                            KwlReturnObject result = accCreditNoteDAOobj.getCNFromNoteNo(entryNumber, companyID);
                            int nocount = result.getRecordTotalCount();
                            if (nocount > 0) {
                                isAlreadyExist = true;
                                throw new AccountingException("Credit Note number'" + entryNumber + "' already exists.");
                            }

                            // For create custom field array
                            JSONArray customJArr = new JSONArray();
                            exceptionMSg="";
                            try {
                                customJArr = accSalesOrderServiceobj.createGlobalCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Credit_Note_ModuleId);
                                if (customJArr.length() > 0) {
                                    exceptionMSg = accSalesOrderServiceobj.isValidCustomFieldData(customJArr);
                                    if (!StringUtil.isNullOrEmpty(exceptionMSg)) {
                                        failureMsg += exceptionMSg;
                                    }
                                }

                            } catch (Exception ex) {
                                failureMsg += "Invalid data entered in custom field.Please check date format,numeric value etc.";
                            }

                            // For getting exchange rate
                            double exchangeRateForTransaction = accSalesOrderServiceobj.getExchangeRateForTransaction(requestJobj, creationDate, currencyID);

                            String sequenceFormatID = "NA";
                            boolean autogenerated = false;
                            boolean isFromOtherSource = false;
                            if (!StringUtil.isNullOrEmpty(entryNumber)) {
                                Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                                sequenceNumberDataMap.put("moduleID", String.valueOf(Constants.Acc_Credit_Note_ModuleId));
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
                                        isFromOtherSource = true;
                                    }
                                }
                            }

                            paramJobj.put(Constants.companyKey, companyID);
                            paramJobj.put(Constants.globalCurrencyKey, requestJobj.optString(Constants.globalCurrencyKey));
                            paramJobj.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
                            paramJobj.put(Constants.userfullname, requestJobj.optString(Constants.userfullname));
                            paramJobj.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
                            paramJobj.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
                            paramJobj.put(Constants.timezonedifference, requestJobj.optString(Constants.timezonedifference));
                            paramJobj.put(Constants.language, Constants.language);
                            paramJobj.put(Constants.currencyKey, currencyID);
                            paramJobj.put("number", entryNumber);
                            paramJobj.put("sequenceformat", sequenceFormatID);
                            paramJobj.put("isFromOtherSource", isFromOtherSource);
                            paramJobj.put("autogenerated", autogenerated);

                            paramJobj.put("defaultAdress", "true");
                            paramJobj.put(Constants.costcenter, costCenterID);
                            paramJobj.put("memo", memo);
                            paramJobj.put(Constants.creationdate, (creationDate!=null)?sdf.format(creationDate):creationDate);
                            paramJobj.put("includingGST", isIncludingGST);
                            paramJobj.put("rateIncludingGst", "0.0");
                            paramJobj.put("isfavourite", "false");

                            if (cntype == 4) {
                                paramJobj.put("masteragent", agentID);
                                paramJobj.put("vendor", vendorId);
                                paramJobj.put("accid", vendorId);
                                paramJobj.put("accountid", vendorAccountId);
                            } else {
                                paramJobj.put("salesPersonID", salesPersonID);
                                paramJobj.put("customer", customerID);
                                paramJobj.put("accid", customerID);
                                paramJobj.put("accountid", custAccountID);
                            }
                            paramJobj.put("costCenterId", costCenterID);
                            paramJobj.put("externalcurrencyrate", String.valueOf(exchangeRateForTransaction));
                            paramJobj.put("customfield", customJArr.toString());
                            paramJobj.put("isEdit", "false");
                            paramJobj.put("seqformat_oldflag", "false");

                            paramJobj.put(Constants.cntype, cntype);
                            paramJobj.put("otherwise", otherwise);
                            paramJobj.put("invoicedetails", "");
                            paramJobj.put("invoiceids", "");
                            paramJobj.put("isEditToApprove", "false");

                            Map<String, Object> requestParams = new HashMap<>();
                            requestParams.put(Constants.companyKey, companyID);
                            if (creationDate != null) {
                                CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, creationDate, false);
                            }
                        } // end global details

                        
                        // For Line level details
                        // Add Custom fields details of line items
                        JSONArray lineCustomJArr = new JSONArray();
                        exceptionMSg="";
                        try {
                            lineCustomJArr = accSalesOrderServiceobj.createLineLevelCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Credit_Note_ModuleId);
                            if (lineCustomJArr.length() > 0) {
                                exceptionMSg = accSalesOrderServiceobj.isValidCustomFieldData(lineCustomJArr);
                                if (!StringUtil.isNullOrEmpty(exceptionMSg)) {
                                    failureMsg += exceptionMSg;
                                }
                            }
                        } catch (Exception ex) {
                            failureMsg += "Invalid data entered in custom field.Please check date format,numeric value etc.";
                        }
                        if (!StringUtil.isNullOrEmpty(failureMsg)) {
                            throw new AccountingException(failureMsg);
                        }

                        JSONObject detailData = new JSONObject();
                        detailData.put("accountid", accountID);
                        detailData.put("reason", reasonID);
                        detailData.put("description", description);
                        detailData.put("customfield", lineCustomJArr.toString());
                        detailData.put("debit", isDebit);
                        detailData.put("srNoForRow",++srNoForRow);
                        detailData.put("includingGST", isIncludingGST);
                        detailData.put("rateIncludingGst", "0.0");
                        detailData.put("gstCurrencyRate", "");

                        
                        /*
                         Tax calculation
                         */
                         double taxamount=0d;
                         double amountWithTax=0d;
                         double includingGSTAmt=0d;
                         double tempRowAmount=0d;

                        detailData.put("dramount", transactionAmount);
                        if (!StringUtil.isNullOrEmpty(taxID) && !StringUtil.isNullOrEmpty(transactionTaxAmountStr)) {
                            detailData.put("taxamount", transactionTaxAmount);
                            if (isIncludingGST) {
                                includingGSTAmt=transactionAmount - transactionTaxAmount;
                                totalTransactionAmount += (includingGSTAmt+transactionTaxAmount);
                                tempRowAmount=includingGSTAmt+transactionTaxAmount;
                            } else {
                                totalTransactionAmount += transactionAmount + transactionTaxAmount;
                                tempRowAmount=transactionAmount + transactionTaxAmount;
                                includingGSTAmt=transactionAmount;
                            }
                            amountWithTax = includingGSTAmt + transactionTaxAmount;
                            detailData.put("prtaxid", taxID);
                            detailData.put("taxamount", transactionTaxAmount);
                        } else if (!StringUtil.isNullOrEmpty(taxID)) {
                            HashMap<String, Object> taxParams = new HashMap<>();
                            taxParams.put("transactiondate", creationDate);
                            taxParams.put("taxid", taxID);
                            taxParams.put("companyid", companyID);
                            KwlReturnObject taxResult = accTaxObj.getTax(taxParams);
                            Object[] taxRow = (Object[]) taxResult.getEntityList().get(0);
                            double taxPercentage = (double) taxRow[1];
                            if (isIncludingGST) {
                                taxamount = authHandler.round((transactionAmount * taxPercentage) / (taxPercentage + 100), companyID);
                                includingGSTAmt=transactionAmount - taxamount;
                                totalTransactionAmount += (includingGSTAmt + taxamount);
                                tempRowAmount=includingGSTAmt + taxamount;
                            } else {
                                taxamount = authHandler.round((transactionAmount * taxPercentage) / 100, companyID);
                                totalTransactionAmount += (transactionAmount + taxamount);
                                tempRowAmount=transactionAmount + taxamount;
                                includingGSTAmt=transactionAmount;
                            }
                            amountWithTax = includingGSTAmt + taxamount;
                            detailData.put("prtaxid", taxID);
                            detailData.put("taxamount", taxamount);
                        } else {
                            detailData.put("prtaxid", "");
                            detailData.put("taxamount", "0");
                            includingGSTAmt=transactionAmount;
                            amountWithTax = transactionAmount;
                            totalTransactionAmount += transactionAmount;
                            tempRowAmount=transactionAmount;
                        }
                        detailData.put("rateIncludingGst", includingGSTAmt);
                        detailData.put("amountwithtax", amountWithTax);

                        /*
                         Calculate the total amount of line level .
                         */
                        if (isDebit) {
                            subTotal += authHandler.round((tempRowAmount), companyID);
                        } else {
                            subTotal -= authHandler.round((tempRowAmount), companyID);
                        }
                        rows.put(detailData);

                    } catch (Exception ex) {
                        isRecordFailed = true;
                        isfailurerecord = true;
                        String errorMsg = ex.getMessage();
                        if (ex.getMessage() != null) {
                            errorMsg = ex.getMessage();
                            errorMsg = StringUtil.replaceFullHTML(errorMsg);
                        } else if (ex.getCause() != null) {
                            errorMsg = ex.getCause().getMessage();
                        }
                        if (prevInvNo.equalsIgnoreCase(entryNumber) || entryNumber.equalsIgnoreCase("")) {
                            if (!failureList.contains(entryNumber)) {
                                if (singleCNFailureRecoredCount > 0) {
                                    failedRecords.append(singleCNFailedRecords);
                                }
                                singleCNFailedRecords = new StringBuilder();
                                singleCNFailureRecoredCount = 0;
                                prevInvNo = entryNumber;
                            }
                            failureList.add(entryNumber);
                        }
                        singleCNFailureRecoredCount++;
                        singleCNFailedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"").append(errorMsg.replaceAll("\"", "")).append("\"");
                    }
                    if (!isfailurerecord) {
                        singleCNFailureRecoredCount++;
                        singleCNFailedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"").append(" ").append("\"");
                    }
                    total++;
                }
                cnt++;
            }

            // save Credit Note for last record
            if (!isAlreadyExist && !isRecordFailed) {
                if (authHandler.round(subTotal, companyID) <= 0) {
                    String message = "Total amount should be greater than Zero.";
                    String temString=singleCNFailedRecords.append(message).append("\"").toString();
                    temString= temString.replaceAll("\"", "");
                    singleCNFailedRecords=new StringBuilder();
                    singleCNFailedRecords.append(temString);
                    isRecordFailed = true;
                }
                if (!isRecordFailed) {
                    paramJobj.put("amount", authHandler.round(subTotal, companyID));
                    paramJobj.put("details", rows.toString());
                    paramJobj.put(Constants.PAGE_URL, requestJobj.optString(Constants.PAGE_URL));
                    saveCreditNoteJSON(paramJobj);
                }
            }
            if (isRecordFailed) {// only if last Credit Note is failed
                failed += singleCNFailureRecoredCount; // last interation failure record
                if (singleCNFailedRecords.toString().length() > 0) {
                    failedRecords.append(singleCNFailedRecords);
                }
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
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();

            // For saving import log
            accSalesOrderServiceobj.saveImportLog(requestJobj, msg, total, failed, Constants.Acc_Credit_Note_ModuleId);
            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", ImportLog.getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Credit_Note_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnObj;
    }

    @Override
    public String getReasonIDByName(String reason, String companyID) throws AccountingException {
        String reasonID = "";
        try {
            if (!StringUtil.isNullOrEmpty(reason) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> filterRequestParams = new HashMap<>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyID);
                filter_names.add("masterGroup.ID");
                filter_params.add("29"); // For Geting Reason Master Item
                filter_names.add("value");
                filter_params.add(reason);
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);

                KwlReturnObject retObj = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    MasterItem reasonObj = (MasterItem) retObj.getEntityList().get(0);
                    reasonID = reasonObj.getID();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Reason.");
        }
        return reasonID;
    }

    private Tax getTaxByCode(String accountCode, String companyID) throws AccountingException {
        Tax tax = null;
        try {
            if (!StringUtil.isNullOrEmpty(accountCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = this.accAccountDAOobj.getTaxFromCode(companyID, accountCode);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    tax = (Tax) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Tax");
        }
        return tax;
    }

    private Account getAccount(String accountCode, String companyID, boolean isByAccountName) throws AccountingException {
        Account account = null;
        try {
            if (!StringUtil.isNullOrEmpty(accountCode) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> requestParams = new HashMap<>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyID);
                filter_names.add("deleted");
                filter_params.add(false);
                if (isByAccountName) {
                    filter_names.add("name");
                    filter_params.add(accountCode);
                } else {
                    filter_names.add("acccode");
                    filter_params.add(accountCode);
                }

                requestParams.put("filter_names", filter_names);
                requestParams.put("filter_params", filter_params);

                KwlReturnObject retObj = accAccountDAOobj.getAccount(requestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    account = (Account) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Account");
        }
        return account;
    }

    /**
     * @param : paramJobj
     * @Desc : Method to post rounding JE when CN Linked with Invoice
     * @throws : ServiceException
     * @Return : void
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {ServiceException.class})
    public void postRoundingJEAfterLinkingInvoiceInCreditNote(JSONObject paramJobj) throws ServiceException {
        try {
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
            String companyid = paramJobj.optString(Constants.companyKey);
            boolean isEdit = paramJobj.optBoolean("isEdit", false);
            String cnid = paramJobj.optString("cnid", "");
            KwlReturnObject dnresult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
            CreditNote cn = (CreditNote) dnresult.getEntityList().get(0);
            String cnNumber = (cn != null) ? cn.getCreditNoteNumber() : "";

            boolean srWithCN = paramJobj.optBoolean("SRWithCN", false);
            if (srWithCN) {//Case when CN Created with SR By Linking Invoices
                String invoiceDetails = paramJobj.optString("invoicedetails", null);
                String amountsArray[] = paramJobj.optString("amounts", "").split(",");
                if (!StringUtil.isNullOrEmpty(invoiceDetails)) {
                    JSONArray jArr = new JSONArray(invoiceDetails);
                    for (int k = 0; k < jArr.length(); k++) {
                        JSONObject invjobj = jArr.getJSONObject(k);
                        double usedcnamount = 0d;
                        if (!StringUtil.isNullOrEmpty(amountsArray[k])) {
                            usedcnamount = Double.parseDouble((String) amountsArray[k]);
                        } else {
                            usedcnamount = 0;
                        }
                        String invoiceID = invjobj.optString("billid", "");
                        if (!StringUtil.isNullOrEmpty(invoiceID) && (usedcnamount != 0 || isEdit)) {
                            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceID);
                            Invoice invoice = (Invoice) grresult.getEntityList().get(0);
                            if (invoice != null && (invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() == 0) || (!invoice.isIsOpeningBalenceInvoice() && invoice.getInvoiceamountdue() == 0)) {
                                String grNumber = invoice.getInvoiceNumber();
                                //below method return Rounding JE if created otherwise it returns null
                                paramJobj.put("salesInvoiceObj", invoice);
                                JournalEntry roundingJE = accInvoiceModuleService.createRoundingOffJE(paramJobj);
                                if (roundingJE != null) {
                                    String jeid = roundingJE.getID();
                                    String jenumber = roundingJE.getEntryNumber();
                                    if (isEdit) {
                                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_UPDATED, "User " + paramJobj.optString(Constants.userfullname) + " has updated Sales Return with Credit Note " + cnNumber +". Rounding off JE " + jenumber + " updated.", auditRequestParams, jeid);
                                    } else {
                                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_UPDATED, "User " + paramJobj.optString(Constants.userfullname) + " has created Sales Return with Credit Note " + cnNumber +". Rounding off JE " + jenumber + " posted.", auditRequestParams, jeid);
                                    }
                                }
                            } else if (isEdit) {//If amount due becomes non zero in edit case then we need to check wheather rounding JE was generated for this GR or not if yes then need to delete
                                KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(invoice.getID(), companyid);
                                List<JournalEntry> jeList = jeResult.getEntityList();
                                for (JournalEntry roundingJE : jeList) {
                                    String jeid = roundingJE.getID();
                                    String jenumber = roundingJE.getEntryNumber();
                                    deleteJEArray(roundingJE.getID(), companyid);
                                    auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has edited CN " + cnNumber + ". Rounding JE " + jenumber + " deleted.", auditRequestParams, jeid);
                                }
                            }
                        }
                    }
                }
            } else {//Case when Invoice linked In CN
                JSONArray linkJSONArray = new JSONArray();
                if (paramJobj.has("linkdetails") && !StringUtil.isNullOrEmpty(paramJobj.optString("linkdetails"))) {
                    linkJSONArray = new JSONArray(paramJobj.optString("linkdetails"));
                }
                for (int i = 0; i < linkJSONArray.length(); i++) {
                    JSONObject obj = linkJSONArray.getJSONObject(i);
                    int documenttype = Integer.parseInt(obj.optString("documentType"));
                    if (documenttype == Constants.CreditNoteOtherwise && obj.optDouble("linkamount", 0.0) != 0) {
                        String invoiceID = obj.optString("documentid");
                        if (!StringUtil.isNullOrEmpty(invoiceID)) {
                            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceID);
                            Invoice invoice = (Invoice) grresult.getEntityList().get(0);
                            if (invoice != null && (invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() == 0) || (!invoice.isIsOpeningBalenceInvoice() && invoice.getInvoiceamountdue() == 0)) {
                                String grNumber = invoice.getInvoiceNumber();
                                //below method return Rounding JE if created otherwise it returns null
                                paramJobj.put("salesInvoiceObj", invoice);
                                JournalEntry roundingJE = accInvoiceModuleService.createRoundingOffJE(paramJobj);
                                if (roundingJE != null) {
                                    String jeid = roundingJE.getID();
                                    String jenumber = roundingJE.getEntryNumber();
                                    auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has knocked off Sales Invoice " + grNumber + " against CN " + cnNumber + ". Rounding JE " + jenumber + " posted.", auditRequestParams, jeid);
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException | NumberFormatException | ServiceException | SessionExpiredException | AccountingException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    /**
     * @param : paramJobj
     * @Desc : Method to post rounding JE when DN created with Invoice
     * @throws : ServiceException
     * @Return : void
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor={ServiceException.class})
    public void postRoundingJEOnCreditNoteSave(JSONObject paramJobj) throws ServiceException {
        try {
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));

            boolean isEditCN = paramJobj.optBoolean("isEdit", false);
            String amounts[] = paramJobj.optString("amounts").split(",");
            String companyid = paramJobj.optString(Constants.companyKey);
            String creditnotenumber = paramJobj.optString("creditnotenumber");
            String invoiceDetails = paramJobj.optString("invoicedetails");
            
            int cntype = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.cntype, null)) ? 1 : Integer.parseInt(paramJobj.optString(Constants.cntype));
            if (!StringUtil.isNullOrEmpty(invoiceDetails)) {
//                JSONArray linkJSONArray = new JSONArray(invoiceDetails);
                JSONArray linkJSONArray = new JSONArray();
                if (!StringUtil.isNullOrEmpty(invoiceDetails)) {
                    linkJSONArray = new JSONArray(invoiceDetails);
                }
                for (int invCount = 0; invCount < linkJSONArray.length(); invCount++) {
                    JSONObject obj = linkJSONArray.getJSONObject(invCount);
                    String invoiceId = obj.optString("billid", "");
                    double usedcnamount = 0d;
                    if (!StringUtil.isNullOrEmpty(amounts[invCount])) {
                        usedcnamount = Double.parseDouble((String) amounts[invCount]);
                    } else {
                        usedcnamount = 0;
                    }
                    /*
                    * Purchase Invoice id in case of CN Undercharge PI. 
                    */
                    if (cntype != Constants.CreditNoteForUndercharge) {
                        if (usedcnamount != 0 && !StringUtil.isNullOrEmpty(invoiceId)) {
                            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceId);
                            Invoice invoice = (Invoice) grresult.getEntityList().get(0);
                            String invNumber = invoice.getInvoiceNumber();
                            if ((invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() == 0) || (!invoice.isIsOpeningBalenceInvoice() && invoice.getInvoiceamountdue() == 0)) {
                                //below method return Rounding JE if created otherwise it returns null
                                paramJobj.put("salesInvoiceObj", invoice);
                                JournalEntry roundingJE = accInvoiceModuleService.createRoundingOffJE(paramJobj);
                                if (roundingJE != null) {
                                    String jeid = roundingJE.getID();
                                    String jenumber = roundingJE.getEntryNumber();
                                    if (isEditCN) {
                                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_UPDATED, "User " + paramJobj.optString(Constants.userfullname) + " has knocked off Sales Invoice " + invNumber + " against  CN " + creditnotenumber + ". Rounding off JE " + jenumber + " updated.", auditRequestParams, jeid);
                                    } else {
                                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has knocked off Sales Invoice " + invNumber + " against  CN " + creditnotenumber + ". Rounding off JE " + jenumber + " posted.", auditRequestParams, jeid);
                                    }
                                }
                            } else if (isEditCN) {//If amount due becomes non zero in edit case then we need to check wheather rounding JE was generated for this GR or not if yes then need to delete
                                KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(invoice.getID(), companyid);
                                List<JournalEntry> jeList = jeResult.getEntityList();
                                for (JournalEntry roundingJE : jeList) {
                                    String jeid = roundingJE.getID();
                                    String jenumber = roundingJE.getEntryNumber();
                                    deleteJEArray(roundingJE.getID(),companyid);
                                    auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has edited CN " + creditnotenumber + ". Rounding JE "+jenumber+" deleted.", auditRequestParams, jeid);
                                }
                            }
                        }
                    }
                }
            }

        } catch (JSONException | NumberFormatException | ServiceException | SessionExpiredException | AccountingException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor={ServiceException.class})
    public void postRoundingJEAfterApproveCreditNote(JSONObject paramJobj) throws ServiceException {
        try {
            String dnid = paramJobj.optString("billid", "");
            KwlReturnObject dnresult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), dnid);
            CreditNote creditnote = (CreditNote) dnresult.getEntityList().get(0);

            //Here used method evictObj to remove current payment object from session.
            //It was giving different value from database

            accountingHandlerDAOobj.evictObj(creditnote);
            dnresult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), dnid);
            creditnote = (CreditNote) dnresult.getEntityList().get(0);

            if (creditnote != null && creditnote.getApprovestatuslevel() == 11) {//Code will execute for approved payment only
                String cnNumber = creditnote != null ? creditnote.getCreditNoteNumber() : "";
                Map<String, Object> auditRequestParams = new HashMap<>();
                auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));

                for (CreditNoteDetail detail : creditnote.getRows()) {
                    Invoice invoice = detail.getInvoice();
                    if (invoice != null && ((invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() == 0) || (!invoice.isIsOpeningBalenceInvoice() && invoice.getInvoiceamountdue() == 0))) {
                        paramJobj.put("salesInvoiceObj", invoice);
                        JournalEntry roundingJE = accInvoiceModuleService.createRoundingOffJE(paramJobj);
                        if (roundingJE != null) {
                            auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has approved Credit Note " + cnNumber + ". Rounding JE " + roundingJE.getEntryNumber() + " posted.", auditRequestParams, roundingJE.getID());
                        }
                    }
                }
            }
        } catch (JSONException | NumberFormatException | ServiceException | SessionExpiredException | AccountingException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    @Override
    public void getCreditNoteCustomDataForPayment(HashMap<String, Object> request, JSONObject obj, CreditNote creditMemo, JournalEntry je) throws ServiceException {
        try {
            String companyid = (String) request.get(Constants.companyKey);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            KwlReturnObject custumObjresult = null;
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Credit_Note_ModuleId, 0));
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            if (creditMemo.isNormalCN()) {
                Map<String, Object> variableMap = new HashMap<>();
                custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), je.getID());
                replaceFieldMap = new HashMap<>();
                if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                    AccJECustomData jeDetailCustom = (AccJECustomData) custumObjresult.getEntityList().get(0);
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, companyid);
                        params.put(Constants.isLink, true);
                        if (request.containsKey(Constants.requestModuleId) && request.get(Constants.requestModuleId) != null) {
                            params.put(Constants.linkModuleId, request.get(Constants.requestModuleId));
                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteServiceImpl.getCreditNoteCustomDataForPayment : " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, SessionExpiredException.class, JSONException.class, AccountingException.class})
    public JSONObject deleteCreditNoteTemporary(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String linkedTransactions = deleteCreditNoteTemporaryJson(paramJobj);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransactions)) {
                msg = messageSource.getMessage("acc.creditN.dels", null, Locale.forLanguageTag(paramJobj.optString("language")));   //"Credit Note(s) has been deleted successfully";
            } else {
                msg = messageSource.getMessage("acc.cnexcept.deleted", null, Locale.forLanguageTag(paramJobj.optString("language"))) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.deletedsuccessfully", null, Locale.forLanguageTag(paramJobj.optString("language"))) + " " + messageSource.getMessage("acc.field.usedintransactionorlockingperiod", null, Locale.forLanguageTag(paramJobj.optString("language")));   //"Sales Order has been deleted successfully;
            }

        } catch (SessionExpiredException |AccountingException |ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public String deleteCreditNoteTemporaryJson(JSONObject paramJobj) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransactions = "";
        try {
            JSONArray jArr = new JSONArray(paramJobj.optString("data", "[{}]"));
            String companyid = paramJobj.optString(Constants.companyKey);
            String cnid = "", cnno = "", entryno = "", jid = "";
            boolean isMassDelete = true; //flag for bulk delete
            if (jArr.length() == 1) {
                isMassDelete = false;
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("noteid"))) {
                    cnid = StringUtil.DecodeText(jobj.optString("noteid"));
                    cnno = jobj.getString("noteno");
                    entryno = jobj.optString("entryno", "");
                    jid = jobj.optString("journalentryid", "");
                    String roundingJENo = "";
                    String roundingJEIDs = "";
                    boolean isNoteLinkedWithPayment = isNoteLinkedWithPayment(cnid);
                    if (isNoteLinkedWithPayment) {
                        linkedTransactions += cnno + ", ";
                        continue;
                    }
                    boolean isNoteLinkedWithAdvancePayment = isNoteLinkedWithAdvancePayment(cnid);
                    if (isNoteLinkedWithAdvancePayment) {
                        linkedTransactions += cnno + ", ";
                        continue;
                    }
                    if (isCreditNotelinkedInDebitNote(cnid, companyid) == true) {
                        linkedTransactions += cnno + ", ";
                        continue;
                    }
                    KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

                    /**
                     * Method to check the payment is Reconciled or not
                     * according to its JE id
                     */
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("jeid", jid);
                    requestParams.put("companyid", companyid);
                    boolean isReconciledFlag = accBankReconciliationDAOObj.isRecordReconciled(requestParams);
                    if (isReconciledFlag) {
                        linkedTransactions += cnno + ", ";
                        if (isMassDelete) { //if bulk delete then only append document no
                            continue;
                        } else { //if single document delete then throw exception with proper message
                            if (!StringUtil.isNullOrEmpty(linkedTransactions)) {
                                linkedTransactions = linkedTransactions.substring(0, linkedTransactions.length() - 2);
                            }
                            throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, Locale.forLanguageTag(paramJobj.optString("language"))) + " " + "<b>" + linkedTransactions + " " + "</b>" + messageSource.getMessage("acc.reconcilation.asitisreconciled", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                        }
                    }
                    boolean withoutinventory = Boolean.parseBoolean(jobj.optString("withoutinventory","false"));
                    if (withoutinventory) {
                        KwlReturnObject result = accCreditNoteDAOobj.deleteBillingCreditNote(cnid, companyid);

                        result = accCreditNoteDAOobj.getJEFromBCN(cnid);
                        List list = result.getEntityList();
                        Iterator itr = list.iterator();
                        while (itr.hasNext()) {
                            String jeid = (String) itr.next();
                            result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                            //Delete entry from optimized table
                            accJournalEntryobj.deleteAccountJEs_optimized(jeid);
                        }

                        result = accCreditNoteDAOobj.getCNDFromBCN(cnid);
                        list = result.getEntityList();
                        itr = list.iterator();
                        while (itr.hasNext()) {
                            String discountid = (String) itr.next();
                            result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                        }

                        result = accCreditNoteDAOobj.getCNDFromBCND(cnid);
                        list = result.getEntityList();
                        itr = list.iterator();
                        while (itr.hasNext()) {
                            String discountid = (String) itr.next();
                            result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                        }
                    } else {

                        // check for is CN created from Sales Return
                        CreditNote creditNote = (CreditNote) kwlCommonTablesDAOObj.getClassObject(CreditNote.class.getName(), cnid);
                        if (creditNote.getSalesReturn() != null) {
                            linkedTransactions += cnno + ",";
                            continue;
                        }

                        Set<String> invoiceIDSet = new HashSet<>();
                        if (creditNote.getApprovestatuslevel() == 11 && !creditNote.isDeleted()) {
                            for (CreditNoteDetail cnd : creditNote.getRows()) {
                                if (cnd.getInvoice() != null) {
                                    invoiceIDSet.add(cnd.getInvoice().getID());
                                }
                            }
                        }
                        /*ERP-40734
                         *To check that the Credit note is belongs to Locked accoundting period
                         *If it is belong to locked accounting period it will throw the exception
                         */
                        HashMap<String, Object> deleteMap = new HashMap<String, Object>();
                        KwlReturnObject result = null;
                        deleteMap.put("cnid", cnid);
                        deleteMap.put("companyid", companyid);
                        deleteMap.put("entryno", entryno);
                        try {
                            deleteCreditNotePartialy(deleteMap);
                        } catch (AccountingException ex) {
                            linkedTransactions += cnno + ",";
                            continue;
                        }

                        if (!creditNote.isOtherwise()) {//no entry in inventory table for otherwise CN
                            /*
                             * query = "update Inventory inv set
                             * inv.deleted=true where inv.ID in(select
                             * cnd.inventory.ID from CreditNoteDetail cnd where
                             * cnd.creditNote.ID in( " + qMarks + ") and
                             * cnd.company.companyID=inv.company.companyID) and
                             * inv.company.companyID=?";
                             * HibernateUtil.executeUpdate(session, query,
                             * params.toArray());
                             */
                            result = accCreditNoteDAOobj.getCNIFromCND(cnid);
                            List list = result.getEntityList();
                            Iterator itr = list.iterator();
                            while (itr.hasNext()) {
                                String inventoryid = (String) itr.next();
                                result = accProductObj.deleteInventoryEntry(inventoryid, companyid);
                            }
                        }

                        //Delete Rouding JEs if created against SI
                        if (!invoiceIDSet.isEmpty()) {
                            String invIDs = "";
                            for (String invID : invoiceIDSet) {
                                invIDs = invID + ",";
                            }
                            if (!StringUtil.isNullOrEmpty(invIDs)) {
                                invIDs = invIDs.substring(0, invIDs.length() - 1);
                            }
                            KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(invIDs, companyid);
                            List<JournalEntry> jeList = jeResult.getEntityList();
                            for (JournalEntry roundingJE : jeList) {
                                roundingJENo = roundingJE.getEntryNumber() + ",";
                                roundingJEIDs = roundingJE.getID() + ",";
                                deleteJEArray(roundingJE.getID(), companyid);
                            }
                            if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                                roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
                            }
                            if (!StringUtil.isNullOrEmpty(roundingJEIDs)) {
                                roundingJEIDs = roundingJEIDs.substring(0, roundingJEIDs.length() - 1);
                            }
                        }

                        if (preferences.isInventoryAccountingIntegration()) {

                            String action = "17";
                            boolean isDirectUpdateInvFlag = false;
                            if (preferences.isUpdateInvLevel()) {
                                isDirectUpdateInvFlag = true;
                                action = "19";//Direct Inventory Update action
                            }

                            JSONArray productArray = new JSONArray();

                            KwlReturnObject res = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
                            creditNote = (CreditNote) res.getEntityList().get(0);

                            Set<CreditNoteDetail> creditNoteDetails = creditNote.getRows();
                            for (CreditNoteDetail creditNoteDetail : creditNoteDetails) {
                                JSONObject productObject = new JSONObject();
                                if (creditNoteDetail.getInventory() != null) {
                                    productObject.put("itemUomId", creditNoteDetail.getInventory().getUom().getID());
                                    productObject.put("itemBaseUomRate", creditNoteDetail.getInventory().getBaseuomrate());
                                    productObject.put("itemQuantity", creditNoteDetail.getInventory().getBaseuomquantity() * (-1));
                                    productObject.put("quantity", creditNoteDetail.getInventory().getQuantity() * (-1));
                                    productObject.put("itemCode", creditNoteDetail.getInventory().getProduct().getProductid());
                                    if (isDirectUpdateInvFlag) {
                                        productObject.put("storeid", creditNoteDetail.getInvstoreid());
                                        productObject.put("locationid", creditNoteDetail.getInvlocid());
                                    }
                                    productArray.put(productObject);
                                }
                            }
                            if (productArray.length() > 0) {

                                String sendDateFormat = "yyyy-MM-dd";
                                DateFormat dateformat = new SimpleDateFormat(sendDateFormat);
                                Date date = creditNote.getCreationDate();
                                String stringDate = dateformat.format(date);

                                JSONObject jSONObject = new JSONObject();
                                jSONObject.put("deliveryDate", stringDate);
                                jSONObject.put("dateFormat", sendDateFormat);
                                jSONObject.put("details", productArray);
                                jSONObject.put("orderNumber", creditNote.getCreditNoteNumber());
                                jSONObject.put("companyId", companyid);
                                jSONObject.put("selling", true);

                                String url = paramJobj.optString(Constants.inventoryURL);
                                CommonFnController cfc = new CommonFnController();
                                cfc.updateInventoryLevel(paramJobj, jSONObject, url, action);
                            }
                        }
                    }
                    // delete foreign gain loss JE
                    List resultJe = accCreditNoteDAOobj.getForeignGainLossJE(cnid, companyid);
                    if (resultJe.size() > 0 && resultJe.get(0) != null) {
                        Iterator itr1 = resultJe.iterator();
                        while (itr1.hasNext()) {
                            Object object = itr1.next();
                            String jeid = object != null ? object.toString() : "";
                            KwlReturnObject result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                        }
                    }
                    StringBuffer journalEntryMsg = new StringBuffer();

                    Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                    auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                    auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                    auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                    if (!StringUtil.isNullOrEmpty(entryno)) {
                        journalEntryMsg.append(" along with the JE No. " + entryno);
                    }
                    auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has deleted Credit Note " + cnno + journalEntryMsg.toString(), auditRequestParams, cnid);

                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has deleted Credit Note " + cnno + ". So Rounding JE No. " + roundingJENo + " deleted.", auditRequestParams, roundingJEIDs);
                    }
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, Locale.forLanguageTag(paramJobj.optString("language"))));
        }
        return linkedTransactions;
    }


    public JSONArray getCNKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException{
        JSONArray allTransaction = new JSONArray();
        String companyid = (String) invoiceRequestParams.get(Constants.companyKey);
//        invoiceRequestParams.put(Constants.Acc_Search_Json, getSearchJsonByModuleIDForGroupBy(moduleWiseSarchMap, fieldCombodataMapmoduleWise, fieldComboValueName, Constants.Acc_Credit_Note_ModuleId));
        List list = accCreditNoteDAOobj.getCNKnockOffTransactions(invoiceRequestParams);
        boolean onlyAmountDue = invoiceRequestParams.containsKey("onlyAmountDue") ? (Boolean) invoiceRequestParams.get("onlyAmountDue") : false;
        for (int i = 0; i < list.size(); i++) {
            Object[] details = (Object[]) list.get(i);
            if(details[3]!=null && (Double)details[3] == 0){
                continue;
            }
            JSONObject obj = new JSONObject();
            double amountdueinbase = (Double) details[3] - (Double) details[5];
            amountdueinbase = authHandler.round(amountdueinbase, companyid);
            double amountdue = (Double) details[2] - (Double) details[4];
            amountdue = authHandler.round(amountdue, companyid);
            obj.put(InvoiceConstants.amountdueinbase, amountdueinbase);
            obj.put("amountdue", authHandler.round(amountdue, companyid));
            obj.put(Constants.billid, details[0]);
            obj.put("isOpeningBalanceTransaction", false);
            obj.put("creationdate", details[8]);
            obj.put(InvoiceConstants.personid, details[16]);
            obj.put("type", Constants.CREDIT_NOTE);
            if(!onlyAmountDue){
                obj.put(Constants.companyKey, companyid);
                obj.put("companyname", details[29]);
                obj.put("customername", details[17]);
                obj.put("customercode", details[19]);
                obj.put(InvoiceConstants.CustomerCreditTerm, details[20]);
                obj.put(InvoiceConstants.aliasname, details[18]);
                obj.put(InvoiceConstants.billno, details[1]);
                obj.put(Constants.currencyKey, details[25]);
                obj.put(InvoiceConstants.currencysymbol, details[27]);
                obj.put(InvoiceConstants.currencyname, details[26]);
                double externalCurrencyRate =  details[24] == null ? 1 : Double.parseDouble( details[24].toString());
                obj.put("externalcurrencyrate",externalCurrencyRate);
                String baseCurrencySymbol = (String)details[31];
                String exchangeRate = "1 "+baseCurrencySymbol+" = "+externalCurrencyRate+" "+obj.getString(InvoiceConstants.currencysymbol);
                obj.put("exchangerate", exchangeRate);
                obj.put("entrydate", details[14]);
                obj.put(Constants.shipdate, details[30]);
                obj.put(Constants.duedate, details[9]);
                obj.put(InvoiceConstants.personname, details[17]);
                obj.put("entryno", details[13]);
                obj.put("salespersonname", details[10]);
                obj.put("memo", details[23]);
                obj.put("salespersoncode", details[11]);
                obj.put("salespersonid", details[12]);
                obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));//XX
                obj.put("amount", authHandler.round((Double) details[2], companyid));   //actual invoice amount
                obj.put("creditlimitinbase", details[22]);
            }
            allTransaction.put(obj);
        }
        return allTransaction;
    }
    public JSONArray getOpeningCNKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException{
        JSONArray allTransaction = new JSONArray();
        String companyid = (String) invoiceRequestParams.get(Constants.companyKey);
        List list = accCreditNoteDAOobj.getOpeningCNKnockOffTransactions(invoiceRequestParams);
        boolean onlyAmountDue = invoiceRequestParams.containsKey("onlyAmountDue") ? (Boolean) invoiceRequestParams.get("onlyAmountDue") : false;
        for (int i = 0; i < list.size(); i++) {
            Object[] details = (Object[]) list.get(i);
            if(details[3]!=null && (Double)details[3] == 0){
                continue;
            }
            JSONObject obj = new JSONObject();
            double amountdueinbase = (Double) details[3] - (Double) details[5];
            amountdueinbase = authHandler.round(amountdueinbase, companyid);
            double amountdue = (Double) details[2] - (Double) details[4];
            amountdue = authHandler.round(amountdue, companyid);
            obj.put(InvoiceConstants.amountdueinbase, amountdueinbase);
            obj.put("amountdue", authHandler.round(amountdue, companyid));
            obj.put(Constants.billid, details[0]);
            obj.put("isOpeningBalanceTransaction", true);
            obj.put("creationdate", details[8]);
            obj.put(InvoiceConstants.personid, details[16]);
            obj.put("type", Constants.CREDIT_NOTE);
            if(!onlyAmountDue){
                obj.put(Constants.companyKey, companyid);
                obj.put("companyname", details[29]);
                obj.put("customername", details[17]);
                obj.put("customercode", details[19]);
                obj.put(InvoiceConstants.CustomerCreditTerm, details[20]);
                obj.put(InvoiceConstants.aliasname, details[18]);
                obj.put(InvoiceConstants.billno, details[1]);
                obj.put(Constants.currencyKey, details[25]);
                obj.put(InvoiceConstants.currencysymbol, details[27]);
                obj.put(InvoiceConstants.currencyname, details[26]);
                double externalCurrencyRate =  details[24] == null ? 1 : Double.parseDouble( details[24].toString());
                obj.put("externalcurrencyrate",externalCurrencyRate);
                String baseCurrencySymbol = (String)details[31];
                String exchangeRate = "1 "+baseCurrencySymbol+" = "+externalCurrencyRate+" "+obj.getString(InvoiceConstants.currencysymbol);
                obj.put("exchangerate", exchangeRate);
                obj.put("entrydate", details[14]);
                obj.put(Constants.shipdate, details[30]);
                obj.put(Constants.duedate, details[9]);
                obj.put(InvoiceConstants.personname, details[17]);
                obj.put("entryno", details[13]);
                obj.put("salespersonname", details[10]);
                obj.put("memo", details[23]);
                obj.put("salespersoncode", details[11]);
                obj.put("salespersonid", details[12]);
                obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));//XX
                obj.put("amount", authHandler.round((Double) details[2], companyid));   //actual invoice amount
//                obj.put(InvoiceConstants.termname, details[31] == null ? "" : details[31]);
                obj.put("creditlimitinbase", details[22]);
            }
            allTransaction.put(obj);
        }
        return allTransaction;
    }
    public JSONArray getAllCNKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException{
        JSONArray allTransaction = null;
        allTransaction = getCNKnockOffJSON(invoiceRequestParams);
        JSONArray arr1 = getOpeningCNKnockOffJSON(invoiceRequestParams);
        if (arr1 != null && arr1.length() > 0) {
            for (int i = 0; i < arr1.length(); i++) {
                allTransaction.put(arr1.getJSONObject(i));
            }
        }
        return allTransaction;
    }
}
