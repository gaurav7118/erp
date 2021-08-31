/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.debitnote;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.goodsreceipt.service.accGoodsReceiptModuleService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.CommonFnControllerService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accDebitNoteServiceImpl implements accDebitNoteService{
    private accDebitNoteDAO accDebitNoteobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accTaxDAO accTaxObj;
    private auditTrailDAO auditTrailObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    private exportMPXDAOImpl exportDaoObj;
    private String successView;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    public accCustomerDAO accCustomerDAOObj;
    private accVendorDAO accVendorDAOObj;
    private accAccountDAO accAccountDAOobj;
    private MessageSource messageSource;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    private authHandlerDAO authHandlerDAOObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private accGoodsReceiptModuleService accGoodsReceiptModuleService;
    private CommonFnControllerService commonFnControllerService;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }

    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }
    
    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    public void setJournalEntryModuleServiceobj(AccJournalEntryModuleService journalEntryModuleServiceobj) {
        this.journalEntryModuleServiceobj = journalEntryModuleServiceobj;
    }
    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOObj) {
        this.accCustomerDAOObj = accCustomerDAOObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setAccVendorDAO(accVendorDAO accVendorDAOObj) {
        this.accVendorDAOObj = accVendorDAOObj;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
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

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setAccGoodsReceiptModuleService(accGoodsReceiptModuleService accGoodsReceiptModuleService) {
        this.accGoodsReceiptModuleService = accGoodsReceiptModuleService;
    }
    
    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }
    
    /*
     * Update gr amount due in case of DN deletion.
     */
    @Override
    public List linkDebitNote(HttpServletRequest request, String debitNoteId, Boolean isInsertAudTrail,Map gramountsmap) throws ServiceException, SessionExpiredException, JSONException, AccountingException {
        List result = new ArrayList();
        String companyid = sessionHandlerImpl.getCompanyid(request);
        DateFormat df=authHandler.getDateOnlyFormat();
        String cnid = "";
        int counter = 0;
        if (!StringUtil.isNullOrEmpty(debitNoteId)) {
            cnid = debitNoteId;
        } else {
            cnid = request.getParameter("cnid");
        }
        HashMap<String, Object> debithm = new HashMap<String, Object>();
        Date maxLinkingDate = null;
        boolean isNoteAlso = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteAlso"))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
            isNoteAlso = Boolean.parseBoolean(request.getParameter("isNoteAlso"));
        }
//        String linkingdate = (String) request.getParameter("linkingdate");
        DateFormat dateformat = authHandler.getDateOnlyFormat();
        //Commented because of ERP-36411
//        if (!StringUtil.isNullOrEmpty(linkingdate)) {
//            try {
//                maxLinkingDate = dateformat.parse(linkingdate);
//            } catch (ParseException ex) {
//                Logger.getLogger(accDebitNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

        String entryNumber = request.getParameter("number");
        String amounts[] = request.getParameter("amounts").split(",");
        String invoiceDetails = request.getParameter("invoicedetails");
        boolean isEdit = request.getParameter("isEdit") != null ? Boolean.parseBoolean(request.getParameter("isEdit")) : false;
        JSONArray jArr = new JSONArray(invoiceDetails);
        Map<String,Object> counterMap=new HashMap<>();
        for (int k = 0; k < jArr.length(); k++) {
                JSONObject jobj = jArr.getJSONObject(k);

                if (StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    continue;
                }
                
                double usedcnamount = 0d;
                double typeFigure = 0d;
                double originalinvoiceamount = 0d;
                int typeOfFigure = 1;
                if (!StringUtil.isNullOrEmpty(amounts[k])) {
                    usedcnamount = Double.parseDouble((String) amounts[k]);
                } else {
                    usedcnamount = 0;
                }
                
                KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), cnid);
                DebitNote debitNote = (DebitNote) cnObj.getEntityList().get(0);
                /**
                 * Need to handle edit case for PR with DN document.
                 */
                if (isEdit && isNoteAlso) {
                 if (!StringUtil.isNullOrEmpty(jobj.getString("billid")) && gramountsmap.containsKey(jobj.getString("billid"))) {
                     //below code is for : In edit case, previous linking of goods receipt is deleted so amoundue should also restore.
                       originalinvoiceamount = (double) gramountsmap.get(jobj.optString("billid"));
                       usedcnamount = debitNote!=null?debitNote.getDnamount():0;
                    }
                debithm.put("dnid", cnid);
                //Delete previous linking between DN and PI
                accDebitNoteobj.deleteLinkingInformationOfDN(debithm);
            }

                if (usedcnamount == 0) {
                    continue;
                }
                if(!StringUtil.isNullOrEmpty(jobj.optString("typeFigure"))){
                    typeFigure = jobj.optDouble("typeFigure",0.0);
                }
                else if (isNoteAlso && !StringUtil.isNullOrEmpty(request.getParameter("linkNumber"))) {
                    typeFigure = jobj.optDouble("typeFigure", usedcnamount);
                }
                if(!StringUtil.isNullOrEmpty(jobj.optString("typeOfFigure"))){
                    typeOfFigure = jobj.optInt("typeOfFigure",1);
                }
                KwlReturnObject grresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("billid"));
                GoodsReceipt goodsReceipt = (GoodsReceipt) grresult.getEntityList().get(0);
                
                
                grresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) grresult.getEntityList().get(0);
                
                Set<DebitNoteDetail> newdebitNoteDetails = new HashSet<DebitNoteDetail>();

                double cnamountdue = debitNote.getDnamountdue();

                if (!debitNote.isOpenflag() || cnamountdue <= 0) {
                    throw new AccountingException(messageSource.getMessage("acc.field.DebitNotehasbeenalreadyutilized", null, RequestContextUtils.getLocale(request)));
                }
//                /**
//                 * Restore the amount due of invoice in case edit SR with CN.
//                 */
//                if (debitNote!=null && isEdit && isNoteAlso) {
//                    double amountdiff = usedcnamount-debitNote.getDnamount();
//                    goodsReceipt.setInvoiceamountdue(originalinvoiceamount+amountdiff);
//                    goodsReceipt.setInvoiceAmountDueInBase(originalinvoiceamount+amountdiff);
//                }
                double amountReceived = usedcnamount;           //amount of DN 
                double amountReceivedConverted = usedcnamount;
                double adjustedRate = 1;
                double exchangeRateforTransaction = jobj.optDouble("exchangeratefortransaction", 1.0);
                if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(goodsReceipt.getCurrency().getCurrencyID()) && !goodsReceipt.getCurrency().getCurrencyID().equals(debitNote.getCurrency().getCurrencyID())) {
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
                DebitNoteDetail dndetailObj = new DebitNoteDetail();
                Date maxDate = null;
                String linkingdate =null;
                if (jobj.has("linkingdate") && jobj.get("linkingdate") != null) {
                    linkingdate = (String) jobj.get("linkingdate");
                    if (!StringUtil.isNullOrEmpty(linkingdate)) {
                        try {
                            maxLinkingDate = dateformat.parse(linkingdate);
                        } catch (ParseException ex) {
                            Logger.getLogger(accDebitNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                if (debitNote.getDntype() != 3 && debitNote.isOpenflag() && debitNote.getDnamount() == debitNote.getDnamountdue()) {//If DN is used for first time.
                    Set<DebitNoteDetail> debitNoteDetails = (Set<DebitNoteDetail>) debitNote.getRows();
                    Iterator itr = debitNoteDetails.iterator();
                    while (itr.hasNext()) {
                        dndetailObj = (DebitNoteDetail) itr.next();

                        if (goodsReceipt != null) {
                            dndetailObj.setGoodsReceipt(goodsReceipt);

                            /*
                             * code to save linking date of DN and GR. This linking
                             * date used while calculating due amount of DN in
                             * Aged/SOA report
                             */
                            if (maxLinkingDate != null) {
                                maxDate = maxLinkingDate;
                            } else {
                                Date linkingDate = null;
                                try {
                                    linkingDate = df.parse(df.format(new Date())); //formatting with date only formmater for removing time 
                                } catch (ParseException ex) {
                                    linkingDate = new Date();
                                }

                                Date grDate = goodsReceipt.getCreationDate();
                                Date dnDate = debitNote.getCreationDate();
                                List<Date> datelist = new ArrayList<Date>();
                                datelist.add(linkingDate);
                                datelist.add(grDate);
                                datelist.add(dnDate);
                                Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                                maxDate = datelist.get(datelist.size() - 1);
                            }
                            dndetailObj.setGrLinkDate(maxDate);
                        }
                        double gsOriginalAmt = 0d;
                        if (goodsReceipt.isNormalInvoice()) {
                            gsOriginalAmt = goodsReceipt.getVendorEntry().getAmount();
                        } else {
                            gsOriginalAmt = goodsReceipt.getOriginalOpeningBalanceAmount();
                        }


                        JSONObject discjson = new JSONObject();
                        discjson.put("discount", usedcnamount);       //enter amount for invoice
                        discjson.put("amountinInvCurrency", amountReceivedConverted);        //amount in invoice currency
                        discjson.put("inpercent", false);
                        //        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, cnamount, currencyid, null, externalCurrencyRate);
                        discjson.put("originalamount", gsOriginalAmt);//(Double) bAmt.getEntityList().get(0));
                        discjson.put("companyid", company.getCompanyID());
                        discjson.put("typeOfFigure", typeOfFigure);
                        discjson.put("typeFigure", typeFigure);
                        KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                        Discount discount = (Discount) dscresult.getEntityList().get(0);
                        dndetailObj.setDiscount(discount);
                        dndetailObj.setExchangeRateForTransaction(exchangeRateforTransaction);
                        newdebitNoteDetails.add(dndetailObj);
                    }
                } else {
                    Set<DebitNoteDetail> cndetails = (Set<DebitNoteDetail>) debitNote.getRows();
                    Iterator itr = cndetails.iterator();
                    int i = 0;
                    while (itr.hasNext()) {
                        dndetailObj = (DebitNoteDetail) itr.next();
                        newdebitNoteDetails.add(dndetailObj);
                        i++;
                    }

                    dndetailObj = new DebitNoteDetail();

                    if (goodsReceipt != null) {
                        dndetailObj.setGoodsReceipt(goodsReceipt);

                        /*
                         * code to save linking date of DN and GR. This linking date
                         * used while calculating due amount of DN in Aged/SOA
                         * report
                         */
                        if (maxLinkingDate != null) {
                            maxDate = maxLinkingDate;
                        } else {
                            Date linkingDate = null;
                            try {
                                linkingDate = df.parse(df.format(new Date())); //formatting with date only formmater for removing time 
                            } catch (ParseException ex) {
                                linkingDate = new Date();
                            }
//                            Date grDate = goodsReceipt.isIsOpeningBalenceInvoice() ? goodsReceipt.getCreationDate() : goodsReceipt.getJournalEntry().getEntryDate();
//                            Date dnDate = debitNote.isIsOpeningBalenceDN() ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
                            Date grDate = goodsReceipt.getCreationDate();
                            Date dnDate = debitNote.getCreationDate();
                            List<Date> datelist = new ArrayList<Date>();
                            datelist.add(linkingDate);
                            datelist.add(grDate);
                            datelist.add(dnDate);
                            Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                            maxDate = datelist.get(datelist.size()-1);
//                            maxDate = Math.max(Math.max(linkingDate.getTime(), grDate.getTime()), dnDate.getTime());
                        }
                        dndetailObj.setGrLinkDate(maxDate);
                    }

                    dndetailObj.setSrno(i + 1);
                    dndetailObj.setTotalDiscount(0.00);
                    dndetailObj.setCompany(company);
                    dndetailObj.setMemo("");
                    dndetailObj.setDebitNote(debitNote);
                    dndetailObj.setID(UUID.randomUUID().toString());

                    double gsOriginalAmt = 0d;;
                    if (goodsReceipt.isNormalInvoice()) {
                        gsOriginalAmt = goodsReceipt.getVendorEntry().getAmount();
                    } else {// for only opening balance goods receipts
                        gsOriginalAmt = goodsReceipt.getOriginalOpeningBalanceAmount();
                    }
                    double gsOriginalAmtConverted = gsOriginalAmt;
                    gsOriginalAmtConverted = gsOriginalAmt / adjustedRate;
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", usedcnamount);  //enter amount for invoice
                    discjson.put("amountinInvCurrency", amountReceivedConverted);        //amount in invoice currency
                    discjson.put("inpercent", false);
                    //        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, cnamount, currencyid, null, externalCurrencyRate);
                    discjson.put("originalamount", gsOriginalAmt);//(Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", companyid);
                    discjson.put("typeOfFigure", typeOfFigure);
                    discjson.put("typeFigure", typeFigure);
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    dndetailObj.setDiscount(discount);
                    dndetailObj.setExchangeRateForTransaction(exchangeRateforTransaction);
                    newdebitNoteDetails.add(dndetailObj);
                }

                double amountDue = cnamountdue - usedcnamount;
                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
                requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
                double externalCurrencyRate = 1d;
                boolean isopeningBalanceCN = debitNote.isIsOpeningBalenceDN();
                Date cnCreationDate = null;
                cnCreationDate =debitNote.getCreationDate();
                externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//                if (!debitNote.isNormalDN() && debitNote.isIsOpeningBalenceDN()) {
//                    cnCreationDate = isopeningBalanceCN ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
//                    externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//                } else {
//                    cnCreationDate = isopeningBalanceCN ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
//                    externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//                }
                String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceCN && debitNote.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                }
                double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

                debithm.put("dndetails", newdebitNoteDetails);
                debithm.put("dnid", debitNote.getID());
                debithm.put("dnamountdue", amountDue);
                debithm.put("openingBalanceAmountDue", amountDue);
                debithm.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);
                debithm.put("openflag", (cnamountdue - usedcnamount) <= 0 ? false : true);
                if (request.getAttribute("entrynumber") != null) {
                    debithm.put("entrynumber", request.getAttribute("entrynumber"));
                }
                if (request.getAttribute("autogenerated") != null) {
                    debithm.put("autogenerated", request.getAttribute("autogenerated"));

                }
                if (request.getAttribute("seqformat") != null) {
                    debithm.put(Constants.SEQFORMAT, request.getAttribute("seqformat"));

                }
                if (request.getAttribute("seqnumber") != null) {
                    debithm.put(Constants.SEQNUMBER, request.getAttribute("seqnumber"));

                }
                if (request.getAttribute(Constants.DATEPREFIX) != null) {
                    debithm.put(Constants.DATEPREFIX, request.getAttribute(Constants.DATEPREFIX));
                }
                if (request.getAttribute(Constants.DATESUFFIX) != null) {
                    debithm.put(Constants.DATESUFFIX, request.getAttribute(Constants.DATESUFFIX));
                }
		if (request.getAttribute(Constants.DATEAFTERPREFIX) != null) {  //SDP-14953
                    debithm.put(Constants.DATEAFTERPREFIX, request.getAttribute(Constants.DATEAFTERPREFIX));
                }
                KwlReturnObject result1 = accDebitNoteobj.updateDebitNote(debithm);

                // Update Invoice base amount due. We have to consider Invoice currency rate to calculate.
                externalCurrencyRate = 1d;
                boolean isopeningBalanceINV = goodsReceipt.isIsOpeningBalenceInvoice();
                Date noteCreationDate = null;
                noteCreationDate = debitNote.getCreationDate();
                externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//                if (!goodsReceipt.isNormalInvoice() && isopeningBalanceINV) {
//                    noteCreationDate = isopeningBalanceCN ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
//                    externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//                } else {
//                    noteCreationDate = isopeningBalanceCN ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
//                    externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//                }
                fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                if (isopeningBalanceINV && goodsReceipt.isConversionRateFromCurrencyToBase()) {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
                }
                
                /*
                 * Store the date on which the amount due has been set to zero
                 * If Approvestatuslevel is not equall to 11 it means CN is going for pending approval. In this case we does not need to update invoice due amount. It will get updated at final approval
                 */

            if (debitNote.getApprovestatuslevel() == 11) {
                totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                KwlReturnObject invoiceResult = updateInvoiceAmountDueAndReturnResult(goodsReceipt, company, amountReceivedConverted, totalBaseAmountDue);
                if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                    GoodsReceipt gr = (GoodsReceipt) invoiceResult.getEntityList().get(0);
                    if (gr.isIsOpeningBalenceInvoice() && gr.getOpeningBalanceAmountDue() == 0) {
                        try {
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            if (debitNote.isIsOpeningBalenceDN() && debitNote != null && debitNote.getCreationDate() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
                            } else if (!debitNote.isIsOpeningBalenceDN() && debitNote != null && debitNote.getJournalEntry() != null && debitNote.getJournalEntry().getCreatedOn() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
                            }
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    } else if (gr.getInvoiceamountdue() == 0) {
                        try {
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            if (debitNote.isIsOpeningBalenceDN() && debitNote != null && debitNote.getCreationDate() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
                            } else if (!debitNote.isIsOpeningBalenceDN() && debitNote != null && debitNote.getJournalEntry() != null && debitNote.getJournalEntry().getCreatedOn() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
                            }
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    }
                }
            }
                if (isInsertAudTrail) {
                    auditTrailObj.insertAuditLog(AuditAction.DABIT_NOTE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has linked Debit Note " + debitNote.getDebitNoteNumber() + " with Vendor Invoice " + goodsReceipt.getGoodsReceiptNumber() + ".", request, debitNote.getID());
                }    

                /*
                 * Start gains/loss calculation Calculate Gains/Loss if Invoice
                 * exchange rate changed at the time of linking Note
                 */
                String debitid = request.getParameter("noteid");
                KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.common.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (isopeningBalanceCN && debitNote.isConversionRateFromCurrencyToBase()) {
                    externalCurrencyRate = 1 / externalCurrencyRate;
                    externalCurrencyRate = externalCurrencyRate;
                }
                Map<String,Object> mapForForexGainLoss = new HashMap<>();
                mapForForexGainLoss.put("dn",debitNote);
                mapForForexGainLoss.put("gr",goodsReceipt);
                mapForForexGainLoss.put("basecurreny",sessionHandlerImpl.getCurrencyID(request));
                mapForForexGainLoss.put("companyid",sessionHandlerImpl.getCompanyid(request));
//                mapForForexGainLoss.put("creationdate",debitNote.isIsOpeningBalenceDN() ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate());
                mapForForexGainLoss.put("creationdate", debitNote.getCreationDate());
                mapForForexGainLoss.put("exchangeratefortransaction",exchangeRateforTransaction);
                mapForForexGainLoss.put("recinvamount",usedcnamount);
                mapForForexGainLoss.put("externalcurrencyrate",externalCurrencyRate);
                mapForForexGainLoss.put("dateformat",authHandler.getDateOnlyFormat(request));
                double amountDiff = getForexGainLossForDebitNote(mapForForexGainLoss);
                if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                    boolean rateDecreased = false;
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JournalEntry journalEntry = null;
                    Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);

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
                        JEFormatParams.put("companyid", companyid);
                        JEFormatParams.put("isdefaultFormat", true);

                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
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
                    jeDataMap.put("entrydate", entryDate);
                    jeDataMap.put("companyid", companyid);
                    jeDataMap.put("memo", "Exchange Gains/Loss posted against Debit Note '" + debitNote.getDebitNoteNumber() + "' linked to Invoice '" + goodsReceipt.getGoodsReceiptNumber() + "'");
                    jeDataMap.put("currencyid", debitNote.getCurrency().getCurrencyID());
                    jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                    jeDataMap.put("isexchangegainslossje", true);
                    journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                    accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                    boolean isDebit = rateDecreased ? false : true;
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", 1);
                    jedjson.put("companyid", companyid);
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
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", goodsReceipt.getAccount().getID());
                    jedjson.put("debit", !isDebit);
                    jedjson.put("jeid", journalEntry.getID());
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    detail.add(jed);
                    journalEntry.setDetails(detail);
                    accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                    // Save link JE iformation in DN details
                    dndetailObj.setLinkedGainLossJE(journalEntry.getID());
                    newdebitNoteDetails.add(dndetailObj);
                    debithm.put("dndetails", newdebitNoteDetails);
                    KwlReturnObject result2 = accDebitNoteobj.updateDebitNote(debithm);
                    counter++;

                }
                // End of Gain/loss calculations
                
                //JE For Receipt which is of Opening Type
                if(counterMap.containsKey("counter")){
                    counter=(Integer)counterMap.get("counter");
                }
                counterMap.put("counter", counter);
                if (debitNote != null && (debitNote.isIsOpeningBalenceDN() ||debitNote.isOtherwise())) {
                    String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                    double finalAmountReval = ReevalJournalEntryForDebitNote(request, debitNote, amountReceived, exchangeRateforTransaction);
                    if (finalAmountReval != 0) {
                        /**
                         * added transactionID and transactionModuleID to
                         * Realised JE.
                         */
                        counterMap.put("transactionModuleid", debitNote.isIsOpeningBalenceDN() ? (debitNote.isdNForVendor() ? Constants.Acc_opening_Vendor_DebitNote : Constants.Acc_opening_Customer_DebitNote) : Constants.Acc_Debit_Note_ModuleId);
                        counterMap.put("transactionId", debitNote.getID());
                        String revaljeid = PostJEFORReevaluation(request, finalAmountReval,companyid, preferences, basecurrency, dndetailObj.getRevalJeId(),counterMap);
                        dndetailObj.setRevalJeId(revaljeid);
                    }
                }
                //JE For Debit which is Linked to Receipt
                counter=(Integer)counterMap.get("counter");
                counterMap.put("counter",counter);
                if (goodsReceipt != null) {
                    double finalAmountReval = ReevalJournalEntryForGoodsReceipt(request, goodsReceipt, amountReceived, exchangeRateforTransaction);
                    if (finalAmountReval != 0) {
                        String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                        /**
                         * added transactionID and transactionModuleID to
                         * Realised JE.
                         */
                        counterMap.put("transactionModuleid", goodsReceipt.isIsOpeningBalenceInvoice() ? Constants.Acc_opening_Prchase_Invoice : Constants.Acc_Vendor_Invoice_ModuleId);
                        counterMap.put("transactionId", goodsReceipt.getID());
                        String revaljeid = PostJEFORReevaluation(request, -(finalAmountReval), companyid, preferences, basecurrency, dndetailObj.getRevalJeIdInvoice(),counterMap);
                        dndetailObj.setRevalJeIdInvoice(revaljeid);

                    }
                }

                /*
                 * saving linking informaion of DebitNote while linking with
                 * purchase invoice
                 */

                if (goodsReceipt != null) {
                    HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                    requestParamsLinking.put("linkeddocid", cnid);
                    requestParamsLinking.put("docid", goodsReceipt.getID());
                    requestParamsLinking.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                    requestParamsLinking.put("linkeddocno", entryNumber);
                    requestParamsLinking.put("sourceflag", 0);
                    KwlReturnObject result3 = accGoodsReceiptobj.saveVILinking(requestParamsLinking);


                    /*
                     * saving linking informaion of Purchase Invoice while linking
                     * with DebitNote
                     */

                    requestParamsLinking.put("linkeddocid", goodsReceipt.getID());
                    requestParamsLinking.put("docid", cnid);
                    requestParamsLinking.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                    requestParamsLinking.put("linkeddocno", goodsReceipt.getGoodsReceiptNumber());
                    requestParamsLinking.put("sourceflag", 1);
                    result1 = accDebitNoteobj.saveDebitNoteLinking(requestParamsLinking);
                }
            }
        return result;

    }

    public double checkFxGainLossOnLinkInvoices(GoodsReceipt gr, double newInvoiceExchageRate, double paymentExchangeRate, double recinvamount, String paymentCurrency, String baseCurrency, String companyid) throws ServiceException {
        double amount = 0;
        HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
         GlobalParams.put("companyid", companyid);
        GlobalParams.put("gcurrencyid", baseCurrency);
        double goodsReceiptExchangeRate = 0d;
        Date goodsReceiptCreationDate = null;
        boolean isopeningBalanceInvoice = gr.isIsOpeningBalenceInvoice();
        if (gr.isNormalInvoice()) {
            goodsReceiptExchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
//            goodsReceiptCreationDate = gr.getJournalEntry().getEntryDate();
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
        invoiceId.put("companyid", companyid);
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
                if (goodsReceiptExchangeRate != paymentExternalCurrencyRate && !revalFlag ) {
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
    
    public double getForexGainLossForDebitNote(Map<String,Object> requestParams) throws ServiceException, SessionExpiredException {
        double amount = 0, actualAmount = 0;
        try {
            String basecurrency = requestParams.get("basecurreny").toString();
            String companyid = requestParams.get("companyid").toString();
            DateFormat dateformat = (DateFormat) requestParams.get("dateformat");
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", basecurrency);
            GlobalParams.put("dateformat", dateformat);
            Date creationDate = (Date) requestParams.get("creationdate");
            DebitNote dn = (DebitNote) requestParams.get("dn");
            String currencyid = dn.getCurrency().getCurrencyID();
            GoodsReceipt gr = (GoodsReceipt) requestParams.get("gr");
            double exchangeratefortransaction = (double) requestParams.get("exchangeratefortransaction");
            double recinvamount = (double) requestParams.get("recinvamount");
                double ratio = 0;
                double newrate = 0.0;
                boolean revalFlag = false;
                boolean isopeningBalanceInvoice = gr.isIsOpeningBalenceInvoice();
                
                boolean isopeningBalancePayment = dn.isIsOpeningBalenceDN();
                boolean isConversionRateFromCurrencyToBase = dn.isConversionRateFromCurrencyToBase();
                double externalCurrencyRate = (double) requestParams.get("externalcurrencyrate");
                double exchangeRate = 0d;
                Date goodsReceiptCreationDate = null;
                if (gr.isNormalInvoice()) {
                    exchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
//                    goodsReceiptCreationDate = gr.getJournalEntry().getEntryDate();
                } else {
                    exchangeRate = gr.getExchangeRateForOpeningTransaction();
                    if(gr.isConversionRateFromCurrencyToBase()){
                        exchangeRate=1/exchangeRate;
                    }
                }
                goodsReceiptCreationDate = gr.getCreationDate();


                HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                invoiceId.put("invoiceid", gr.getID());
                invoiceId.put("companyid", companyid);
                KwlReturnObject result = null;
                result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                if (history != null) {
                    exchangeRate = history.getEvalrate();
                    newrate = exchangeratefortransaction;
                    revalFlag = true;
                }
//                }
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
    
    public double ReevalJournalEntryForGoodsReceipt(HttpServletRequest request, GoodsReceipt goodsReceipt, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
        double finalAmountReval = 0;
        try {
            String basecurrency = sessionHandlerImpl.getCurrencyID(request);
            double ratio = 0;
            double amountReval = 0;
            String revalId = null;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkInvoiceAmount;
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            GlobalParams.put("gcurrencyid", basecurrency);
            GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
            Date creationDate = goodsReceipt.getCreationDate();
            boolean isopeningBalanceInvoice = goodsReceipt.isIsOpeningBalenceInvoice();
            tranDate = goodsReceipt.getCreationDate();
            if (!goodsReceipt.isNormalInvoice()) {
                exchangeRate = goodsReceipt.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = goodsReceipt.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
//                tranDate = goodsReceipt.getJournalEntry().getEntryDate();
            }
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", goodsReceipt.getID());
            invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
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
            if (goodsReceipt.getCurrency() != null) {
                currid = goodsReceipt.getCurrency().getCurrencyID();
            }
            //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && goodsReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            if (revalueationHistory == null && isopeningBalanceInvoice && goodsReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
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

    public double ReevalJournalEntryForDebitNote(HttpServletRequest request, DebitNote debitNote, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
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
            GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            GlobalParams.put("gcurrencyid", basecurrency);
            GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
            Date creationDate = debitNote.getCreationDate();
            boolean isopeningBalanceInvoice = debitNote.isIsOpeningBalenceDN();
            tranDate = debitNote.getCreationDate();
            if (!debitNote.isNormalDN()) {
                exchangeRate = debitNote.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = debitNote.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
//                tranDate = debitNote.getJournalEntry().getEntryDate();
            }
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", debitNote.getID());
            invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
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
            if (debitNote.getCurrency() != null) {
                currid = debitNote.getCurrency().getCurrencyID();
            }
            //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            if (revalueationHistory == null && isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
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

    public String PostJEFORReevaluation(HttpServletRequest request, double finalAmountReval, String companyid, CompanyAccountPreferences preferences, String basecurrency, String oldRevaluationJE,Map<String,Object> dataMap) {
        String jeid = "";
        try {
            String jeentryNumber = "";
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            boolean jeautogenflag = false;
            DateFormat df = authHandler.getDateOnlyFormat(request);
            /**
             * added Link Date to Realised JE. while link Otherwise CN/DN to
             * Reevaluated Invoice. Use 'linkingdate'
             */
            String creationDate = !StringUtil.isNullObject(request.getParameter("linkingdate")) ? request.getParameter("linkingdate") : request.getParameter("creationdate");
            Date entryDate = StringUtil.isNullOrEmpty(creationDate) ? new Date() : df.parse(creationDate);
            int counter=(Integer)dataMap.get("counter");
            synchronized (this) {
                Map<String, Object> JEFormatParams = new HashMap<>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                jeautogenflag = true;
                if (StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                    String nextAutoNoTemp=(String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    int sequence = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
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
                    datePrefix = entry.getDatePreffixValue();
                    dateafterPrefix = entry.getDateAfterPreffixValue();
                    dateSuffix = entry.getDateSuffixValue();
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
            jeDataMapReval.put("companyid", companyid);
            //jeDataMapReval.put("memo", "Realised Gain/Loss");
            jeDataMapReval.put("currencyid", basecurrency);
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
            jedjsonreval.put("companyid", companyid);
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
            jedjsonreval.put("companyid", companyid);
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
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jeid;
    }
    
    /**
     * Description :This method is used to save Dimension For Reval JEDetail
     */
    public void setDimensionForRevalJEDetail(String lineleveldimensions, JournalEntryDetail jed) {
        try {
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
            Logger.getLogger(accDebitNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Function to update goodsreceipt amount due and return the result
     */
    public KwlReturnObject updateInvoiceAmountDueAndReturnResult(GoodsReceipt goodsReceipt, Company company, double amountReceivedForGoodsReceipt, double baseAmountReceivedForGoodsReceipt) throws JSONException, ServiceException {
        KwlReturnObject result = null;
        if (goodsReceipt != null) {
            double invoiceAmountDue = goodsReceipt.getOpeningBalanceAmountDue();
            invoiceAmountDue -= amountReceivedForGoodsReceipt;
            Map<String, Object> greceipthm = new HashMap<String, Object>();
            greceipthm.put("grid", goodsReceipt.getID());;
            greceipthm.put("companyid", company.getCompanyID());
            greceipthm.put("openingBalanceAmountDue", invoiceAmountDue);
            greceipthm.put(Constants.openingBalanceBaseAmountDue, goodsReceipt.getOpeningBalanceBaseAmountDue() - baseAmountReceivedForGoodsReceipt);
            greceipthm.put(Constants.invoiceamountdue, goodsReceipt.getInvoiceamountdue() - amountReceivedForGoodsReceipt);
            greceipthm.put(Constants.invoiceamountdueinbase, goodsReceipt.getInvoiceAmountDueInBase() - baseAmountReceivedForGoodsReceipt);
            result = accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
        }
        return result;
    }
    
    @Override
    public void updateOpeningInvoiceAmountDue(String debitNoteId, String companyId) throws JSONException, ServiceException {

        KwlReturnObject dnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), debitNoteId);
        if (!dnObj.getEntityList().isEmpty()) {
            DebitNote debitNote = (DebitNote) dnObj.getEntityList().get(0);
            Set<DebitNoteDetail> debitNoteDetails = debitNote.getRows();
            if (debitNoteDetails != null && !debitNote.isDeleted()) {// if debit note already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete.
                Iterator itr = debitNoteDetails.iterator();
                while (itr.hasNext()) {
                    DebitNoteDetail debitNoteDetail = (DebitNoteDetail) itr.next();
                    if (debitNoteDetail.getGoodsReceipt() != null && !debitNoteDetail.getGoodsReceipt().isNormalInvoice() && debitNoteDetail.getGoodsReceipt().isIsOpeningBalenceInvoice()) {
//                        double amountPaid = debitNoteDetail.getDiscount().getDiscountValue();
                        double amountPaid = debitNoteDetail.getDiscount().getAmountinInvCurrency();
                        GoodsReceipt goodsReceipt = debitNoteDetail.getGoodsReceipt();
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put(Constants.companyid, companyId);
                        requestParams.put("gcurrencyid", goodsReceipt.getCompany().getCurrency().getCurrencyID());
                        double externalCurrencyRate = 0d;
                        externalCurrencyRate = goodsReceipt.getExchangeRateForOpeningTransaction();
                        String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (goodsReceipt.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, goodsReceipt.getCreationDate(), externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, goodsReceipt.getCreationDate(), externalCurrencyRate);
                        }
                        double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

                        double invoiceAmountDue = goodsReceipt.getOpeningBalanceAmountDue();
                        invoiceAmountDue += amountPaid;
                        Map<String, Object> greceipthm = new HashMap<String, Object>();
                        greceipthm.put("grid", goodsReceipt.getID());;
                        greceipthm.put("companyid", companyId);
                        greceipthm.put("openingBalanceAmountDue", invoiceAmountDue);
                        greceipthm.put(Constants.openingBalanceBaseAmountDue, goodsReceipt.getOpeningBalanceBaseAmountDue() + totalBaseAmountDue);
                        if (invoiceAmountDue != 0) {
                            greceipthm.put("amountduedate", "");
                        }
                        accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                    } else if (debitNoteDetail.getGoodsReceipt() != null && debitNoteDetail.getGoodsReceipt().isNormalInvoice() && !debitNoteDetail.getGoodsReceipt().isIsOpeningBalenceInvoice()) {
//                        double amountPaid = debitNoteDetail.getDiscount().getDiscountValue();
                        double amountPaid = debitNoteDetail.getDiscount().getAmountinInvCurrency();
                        GoodsReceipt goodsReceipt = debitNoteDetail.getGoodsReceipt();
                        double invoiceAmountDue = goodsReceipt.getInvoiceamountdue();
                        invoiceAmountDue += amountPaid;
                        Map<String, Object> greceipthm = new HashMap<String, Object>();
                        greceipthm.put("grid", goodsReceipt.getID());;
                        greceipthm.put("companyid", companyId);
                        greceipthm.put(Constants.invoiceamountdue, invoiceAmountDue);
                        JournalEntry je = goodsReceipt.getJournalEntry();
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put(Constants.companyid, companyId);
                        requestParams.put("gcurrencyid", goodsReceipt.getCompany().getCurrency().getCurrencyID());
                        String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
                        if (je != null) {
//                            KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceAmountDue, fromcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                            KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceAmountDue, fromcurrencyid, goodsReceipt.getCreationDate(), je.getExternalCurrencyRate());
                            double invoiceamountdueinbase = (Double) baseAmount.getEntityList().get(0);
                            greceipthm.put(Constants.invoiceamountdueinbase, invoiceamountdueinbase);
                        }
                         if (invoiceAmountDue != 0) {
                            greceipthm.put("amountduedate", "");
                        }
                        accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                    }
                }
            }
    }
    }
    
    @Override
    public KwlReturnObject deleteDebitNotePartialy(HashMap<String, Object> dataMap) throws ServiceException, JSONException , AccountingException {

        String dnid = (String) dataMap.get("dnid");
        String companyid = (String) dataMap.get("companyid");

        KwlReturnObject dnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
        DebitNote debitNote = (DebitNote) dnObj.getEntityList().get(0);

        if (debitNote.getApprovestatuslevel() == 11) {//For pending approval DN we did not need to update invoice amount. It is only needed for DN whose approval level is 11.
            updateOpeningInvoiceAmountDue(dnid, companyid);
        }
        KwlReturnObject result = accDebitNoteobj.deleteDebitNote(dnid, companyid);

        result = accDebitNoteobj.getJEFromDN(dnid);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            String jeid = (String) itr.next();
            result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
            //Delete entry from optimized table
            accJournalEntryobj.deleteAccountJEs_optimized(jeid);
        }

        result = accDebitNoteobj.getDNDFromDN(dnid);
        list = result.getEntityList();
        itr = list.iterator();
        while (itr.hasNext()) {
            String discountid = (String) itr.next();
            result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
        }

        /*
         * query = "update Discount di set di.deleted=true where di.ID in(select
         * dnd.discount.ID from DebitNoteDetail dnd where dnd.debitNote.ID in( "
         * + qMarks + ") and dnd.company.companyID=di.company.companyID) and
         * di.company.companyID=?"; HibernateUtil.executeUpdate(session, query,
         * params.toArray());
         */
        result = accDebitNoteobj.getDNDIFromDN(dnid);
        list = result.getEntityList();
        itr = list.iterator();
        while (itr.hasNext()) {
            String discountid = (String) itr.next();
            result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
        }
        
        return result;

    }
    
    public HashMap getDebitNoteCommonCode(HttpServletRequest request, HttpServletResponse response) {
        HashMap map = new HashMap();
        JSONArray DataJArr = new JSONArray();
        try {
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            HashMap<String, Object> requestParams = accDebitNoteController.gettDebitNoteMap(request);
            String userid = sessionHandlerImpl.getUserid(request);
            String userFullName = sessionHandlerImpl.getUserFullName(request);
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean eliminateflag = consolidateFlag;
            JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
            DateFormat userDateFormat=null;
            if(paramJobj.has(Constants.userdateformat)){
                userDateFormat=new SimpleDateFormat(String.valueOf(paramJobj.get(Constants.userdateformat)));
                requestParams.put(Constants.userdf,userDateFormat);
            }
            if (consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("linknumber"))){
                requestParams.put("linknumber", request.getParameter("linknumber"));
            }
            requestParams.put("userid", userid);
            requestParams.put("userFullName", userFullName);
            
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            if (extraCompanyPreferences != null && extraCompanyPreferences.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.VENDOR_PERMCODE);
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.VENDOR_VIEWALL_PERMCODE) ==
                     * Constants.VENDOR_VIEWALL_PERMCODE is true then user has
                     * permission to view all vendors documents,so at that time
                     * there is need to filter record according to user&agent.
                     */
//                    String userId = sessionHandlerImpl.getUserid(request);
//                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraCompanyPreferences.isEnablesalespersonAgentFlow());
                }
            }
	    //Sorting call from UI side
            String dir = "", sort = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                dir = request.getParameter("dir");
                sort = request.getParameter("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            
            KwlReturnObject result = null;
            String companyid = "";
            int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
            boolean viewMode = request.getParameter("viewMode") != null ? Boolean.parseBoolean(request.getParameter("viewMode")) : false;
            if (viewMode) {
                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) request.getParameter("noteid"));
                DebitNote debitMemo = (DebitNote) resultObject.getEntityList().get(0);
                cntype = debitMemo.getDntype();
                requestParams.put("cntype", cntype);
                request.setAttribute("cntype", cntype);
            }
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            User user = (User) userResult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                boolean isNoteForPayment = false;
                boolean isVendor = false;
                boolean isNewUI = false;
                Boolean isExport = false;
                if (request.getAttribute("isExport") != null) {
                    isExport = (Boolean) request.getAttribute("isExport");
                    requestParams.put("isExport", isExport);
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteForPayment"))) {
                    isNoteForPayment = Boolean.parseBoolean(request.getParameter("isNoteForPayment"));
                    isVendor = Boolean.parseBoolean(request.getParameter("isVendor"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("isNewUI"))) {
                    isNewUI = Boolean.parseBoolean(request.getParameter("isNewUI"));
                }
                requestParams.put("isNewUI", isNewUI);
                requestParams.put("isNoteForPayment", isNoteForPayment);
                result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                if (cntype != 10 && cntype != 11) {
                    DataJArr = getDebitNotesMergedJson(requestParams, result.getEntityList(), DataJArr);
                }
                if (cntype == 10 || (isNoteForPayment && isVendor)) {
                    result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
                    requestParams.put("cntype", 10);
                    getOpeningDebitNotesJson(requestParams, result.getEntityList(), DataJArr);
                } else if (cntype == 11 || (isNoteForPayment && !isVendor)) {
                    result = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
                    requestParams.put("cntype", 11);
                    getOpeningDebitNotesJson(requestParams, result.getEntityList(), DataJArr);
                }
            }
            int cnt = consolidateFlag ? DataJArr.length() : result.getRecordTotalCount();
            map.put("data", DataJArr);
            map.put("count", cnt);
        } catch (Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }

    public JSONArray getOpeningDebitNotesJson(HashMap<String, Object> requestParams, List list, JSONArray JArr) throws ServiceException {
        try {
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat userdf = (DateFormat) requestParams.get("userdf");
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get("companyid");
            int cnType = (Integer) requestParams.get("cntype");
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
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Debit_Note_ModuleId));
            if (accAccountDAOobj != null) {
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            if (list != null && !list.isEmpty()) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    DebitNote dn = (DebitNote) it.next();
                    

//                    if (isNoteForPayment && dn.getOpeningBalanceAmountDue() <= 0) {
//                        continue;
//                    }

                    JSONObject obj = new JSONObject();

                    String personid = "";
                    String personname = "";
                    String paymentterm = "";
                    String personcode = "";
                    boolean isCustomer = false;

                    if (cnType == 10) {
                        personid = dn.getVendor().getID();
                        personname = dn.getVendor().getName();
                        personcode = dn.getVendor().getAcccode() == null ? "" : dn.getVendor().getAcccode();
                        paymentterm = dn.getVendor().getDebitTerm() == null ? "" : dn.getVendor().getDebitTerm().getTermname();
                    } else {
                        personid = dn.getCustomer().getID();
                        personname = dn.getCustomer().getName();
                        personcode = dn.getCustomer().getAcccode() == null ? "" : dn.getCustomer().getAcccode();
                        paymentterm = dn.getCustomer().getCreditTerm() == null ? "" : dn.getCustomer().getCreditTerm().getTermname();
                        isCustomer = true;
                    }
                    Date creditNoteDate = null;
                    double externalCurrencyRate = 0d;
                    boolean isopeningBalanceInvoice = dn.isIsOpeningBalenceDN();
                    if (dn.isIsOpeningBalenceDN()) {
                        creditNoteDate = dn.getCreationDate();
                        externalCurrencyRate = dn.getExchangeRateForOpeningTransaction();
                    } 
                    
                    String transactionCurrencyId = (dn.getCurrency() == null ? currency.getCurrencyID() : dn.getCurrency().getCurrencyID());

                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("currencysymboltransaction", (dn.getCurrency() == null ? currency.getSymbol() : dn.getCurrency().getSymbol()));

                    String currencyFilterForTrans = "";
                    if (requestParams.containsKey("currencyfilterfortrans")) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                    }
                    if (requestParams.containsKey("currencyfilterfortrans")&&isNoteForPayment) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject curresultpayment = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyPayment = (KWLCurrency) curresultpayment.getEntityList().get(0);
                        obj.put("currencyname", (currencyPayment == null ? currency.getName() : currencyPayment.getName()));
                        obj.put("currencycode", (currencyPayment == null ? currency.getCurrencyCode() : currencyPayment.getCurrencyCode()));
                        obj.put("currencysymbol", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                        obj.put("currencyid", (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getCurrencyID()));
                    }else{
                        obj.put("currencyname", (dn.getCurrency() == null ? currency.getName() : dn.getCurrency().getName()));
                        obj.put("currencycode", (dn.getCurrency() == null ? currency.getCurrencyCode() : dn.getCurrency().getCurrencyCode()));
                        obj.put("currencysymbol", (dn.getCurrency() == null ? currency.getSymbol() : dn.getCurrency().getSymbol()));
                        obj.put("currencyid", (dn.getCurrency() == null ? currency.getCurrencyID() : dn.getCurrency().getCurrencyID()));
  
                    }
                    
                    
                    double amountdue=dn.isOtherwise() ? dn.getDnamountdue() : 0;
                    double amountDueOriginal = dn.isOtherwise() ? dn.getDnamountdue() : 0;
                    KwlReturnObject baseAmt = null;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        if (isopeningBalanceInvoice && dn.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            requestParams.put("isRevalue",true);
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                            amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                        } else {
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                            amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                        }
//                        bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
//                            amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                    }
                    if (isopeningBalanceInvoice && dn.isConversionRateFromCurrencyToBase()) {
                        baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                    }else{
                        baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                    }
                    
                    
                    obj.put("withoutinventory", false);
                    obj.put("isOpeningBalanceTransaction", dn.isIsOpeningBalenceDN());
                    obj.put("isNormalTransaction", dn.isNormalDN());
                    obj.put("noteid", dn.getID());
                    obj.put("companyid", dn.getCompany().getCompanyID());
                    obj.put("companyname", dn.getCompany().getCompanyName());
                    obj.put("noteno", dn.getDebitNoteNumber());
                    obj.put("journalentryid", "");
//                    obj.put("currencysymbol", (dn.getCurrency() == null ? currency.getSymbol() : dn.getCurrency().getSymbol()));
//                    obj.put("currencyid", (dn.getCurrency() == null ? currency.getCurrencyID() : dn.getCurrency().getCurrencyID()));
                    obj.put("entryno", "");
                    obj.put("personid", personid);
                    obj.put("personname", personname);
                    /*
                     * This fields are inserted for Credit Note Register Export
                     * Functionality - Mayur B.
                     */
                    obj.put("personcode", personcode);
                    obj.put("paymentterm", paymentterm);
                    obj.put("iscustomer", isCustomer);
                    obj.put("dateinuserformat", userdf.format(dn.getCreationDate()));
                    obj.put("amount", dn.getDnamount());
//                    KwlReturnObject paidAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, dn.getDnamount(), transactionCurrencyId, creditNoteDate, externalCurrencyRate);
                    obj.put("paidamountinbase", authHandler.round((Double) baseAmt.getEntityList().get(0), companyid));
                    obj.put("amountdue", dn.getOpeningBalanceAmountDue());
                    obj.put("amountduenonnegative", dn.getOpeningBalanceAmountDue());
                    obj.put("date", df.format(dn.getCreationDate()));
                    obj.put("memo", dn.getMemo() == null ? "" : dn.getMemo());
                    obj.put("createdby",dn.getCreatedby() == null ? "" : StringUtil.getFullName(dn.getCreatedby()));
                    obj.put("deleted", false);
                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0&&amountdue<=0) ? 0 : (amountdue/amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal,companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal,companyid));
                    obj.put("amountdueinbase", authHandler.round((Double) baseAmt.getEntityList().get(0), companyid));
                    obj.put("amountinbase", authHandler.round(dn.getOriginalOpeningBalanceBaseAmount(), companyid));
                    obj.put("otherwise", true);
                    obj.put("openflag", dn.isOpenflag());
//                     obj.put("cntype", debitMemo.getDntype());
                    obj.put("costcenterid", "");
                    obj.put("costcenterName", "");

                    obj.put("noteSubTotal", dn.getDnamount());
                    obj.put("notetax", 0);
                    obj.put("totalTax", 0);
                    obj.put("totalDiscount", 0);
                    if (!StringUtil.isNullOrEmpty(dn.getMvatTransactionNo())) {
                        obj.put(Constants.MVATTRANSACTIONNO, dn.getMvatTransactionNo());
                    }
                     Map<String, Object> variableMap = new HashMap<String, Object>();
                        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                        ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                        Detailfilter_names.add("companyid");
                        Detailfilter_params.add(dn.getCompany().getCompanyID());
                        Detailfilter_names.add("OpeningBalanceDebitNoteId");
                        Detailfilter_params.add(dn.getID());
                        Detailfilter_names.add("moduleId");
                        Detailfilter_params.add(Constants.Acc_Debit_Note_ModuleId + "");
                        invDetailRequestParams.put("filter_names", Detailfilter_names);
                        invDetailRequestParams.put("filter_params", Detailfilter_params);
                        KwlReturnObject idcustresult = accJournalEntryobj.getOpeningBalanceDebitNoteCustomData(invDetailRequestParams);
                        if (idcustresult.getEntityList().size() > 0) {
                            OpeningBalanceDebitNoteCustomData balanceDebitNoteCustomData = (OpeningBalanceDebitNoteCustomData) idcustresult.getEntityList().get(0);
                            AccountingManager.setCustomColumnValues(balanceDebitNoteCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put("companyid", companyid);
                            params.put("isExport", true);
                            if (requestParams.containsKey("browsertz") && requestParams.get("browsertz") != null) {
                                params.put("browsertz", requestParams.get("browsertz").toString());
                            }
                            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                        }
                    boolean isLinked = false;                     // Flag for identifying whether the DN is linked to some purchase invoice or not.
                    Set<DebitNoteDetail> dndetails = dn.getRows();
                    for (DebitNoteDetail noteDetail : dndetails) {
                        if (noteDetail.getGoodsReceipt() != null) {
                            isLinked = true;
                            break;
                        } else if (!StringUtil.isNullOrEmpty(noteDetail.getCreditNoteId())) {//Enable to Unlink button, if Debit Note is linked with any transaction 
                            isLinked = true;
                            break;
                        } else if (cnType == 11) {//DN against Customer
                               /*  Checking Whether DN against Customer 
                            
                             is linking with CN against Customer or not*/
                            Boolean isRecord = accDebitNoteobj.checkDNLinking(dn.getID());
                            if (isRecord) {
                                isLinked = true;
                                break;
                            }
                        }
                    }
                    obj.put("isLinked", isLinked);
                    obj.put("cntype",cnType);
                    HashMap<String,Object> reqParams1 = new HashMap<>();
                    reqParams1.put("dnid",dn.getID());
                    reqParams1.put("companyid",dn.getCompany().getCompanyID());
                    KwlReturnObject linkResult=accDebitNoteobj.getLinkDetailReceiptToDebitNote(reqParams1);
                    if(!linkResult.getEntityList().isEmpty()){
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

    public JSONArray getDebitNotesMergedJson(HashMap<String, Object> requestParams, List list, JSONArray JArr) throws ServiceException {
//         JSONArray JArr = new JSONArray();
        try {
            int cntype = (requestParams.containsKey("cntype") && requestParams.get("cntype") != null) ? Integer.parseInt(requestParams.get("cntype").toString()) : 1;
            int transactiontype = (requestParams.containsKey("transactiontype") && requestParams.get("transactiontype") != null) ? Integer.parseInt(requestParams.get("transactiontype").toString()) : 1;
            boolean isApprovalPendingReport =(requestParams.containsKey("pendingapproval") && requestParams.get("pendingapproval")!=null)?Boolean.parseBoolean(requestParams.get("pendingapproval").toString()):false;
            String userName =(requestParams.containsKey("userFullName") && requestParams.get("userFullName")!=null)?requestParams.get("userFullName").toString() :"";
            String userid =(requestParams.containsKey("userid") && requestParams.get("userid")!=null)?requestParams.get("userid").toString() :"";
            String currencyid = (String) requestParams.get("gcurrencyid");
            String transactionCurrencyId=(String) requestParams.get("gcurrencyid");
            DateFormat userDateFormat=null;
            if(requestParams.containsKey(Constants.userdf) && requestParams.get(Constants.userdf)!=null){
                userDateFormat=(DateFormat)requestParams.get(Constants.userdf);
            }
            boolean checkTax=false;
             String companyid="";
            if(requestParams.containsKey("companyid")){
                 companyid = (String) requestParams.get("companyid");
            }
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", companyid);
            Object exPrefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"columnPref"}, paramMap);
            JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
            boolean isPostingDateCheck = false;
            if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                isPostingDateCheck = true;
            }
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            /**
             * ERP-32479 - Issue was linking date showing different, for different time zone.
             */
//            if (requestParams.containsKey("browsertz") && requestParams.get("browsertz") != null && !StringUtil.isNullOrEmpty(requestParams.get("browsertz").toString())) {
//                sdf.setTimeZone(TimeZone.getTimeZone("GMT" + requestParams.get("browsertz")));
//            }
            double tax = 0;
            int amountdigitafterdecimal = 2;
            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
            if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                amountdigitafterdecimal = (Integer) decimalcontact[2];
            }
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList( companyid, Constants.Acc_Debit_Note_ModuleId));
            FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
            Integer countryId = company.getCountry() != null ? Integer.parseInt(company.getCountry().getID()) : null;
            HashSet<String> cnSet = new HashSet<String>();
            Iterator itr = list.iterator();
            HashMap<String, Object> badDebtMap = new HashMap<>();            
            badDebtMap.put("badDebtType", 0);
            KwlReturnObject badDebtResult=null;
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                boolean isLinkedInvoiceClaimed=false;
                boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                
//                if(cnSet.contains((String) row[1])){
//                    continue;
//                }
//                
//                cnSet.add((String) row[1]);
                
                String personid = "";
                String personname = "";
                String aliasname = "";
                String paymentterm = "";
                String personcode = "";
                String personaccountId = "";
                boolean isCustomer = false;
                boolean isHasaccess=false;
                DateFormat userdf = (DateFormat) requestParams.get("userdf");
                if (cntype == 4 && !withoutinventory && transactiontype != 8) {//DN against customer
                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                    Customer customer = (Customer) resultObject.getEntityList().get(0);
                    personid = customer.getID();
                    personname = customer.getName();
                    aliasname = customer.getAliasname();
                    personcode = customer.getAcccode() == null ? "" : customer.getAcccode();
                    paymentterm = customer.getCreditTerm() == null ? "" : customer.getCreditTerm().getTermname();
                    personaccountId = customer.getAccount()==null?"":customer.getAccount().getID();
                    isHasaccess=customer.isActivate();
                    isCustomer = true ;
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
                            personaccountId = vendor.getAccount()==null?"":vendor.getAccount().getID();
                            isHasaccess=vendor.isActivate();
                        } else {
                            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                            Customer customer = (Customer) resultObject.getEntityList().get(0);
                            personid = customer.getID();
                            personname = customer.getName();
                            aliasname = customer.getAliasname();
                            personcode = customer.getAcccode() == null ? "" : customer.getAcccode();
                            paymentterm = customer.getCreditTerm() == null ? "" : customer.getCreditTerm().getTermname();
                            personaccountId = customer.getAccount()==null?"":customer.getAccount().getID();
                            isHasaccess=customer.isActivate();
                            isCustomer = true;
                        }
                        withoutinventory = false;
                    } if(cntype==5){
                        KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                        Customer customer = (Customer) resultObject.getEntityList().get(0);
                        personid = customer.getID();
                        personname = customer.getName();
                        aliasname = customer.getAliasname();
                        personcode = customer.getAcccode() == null ? "" : customer.getAcccode();
                        paymentterm = customer.getCreditTerm()== null ? "" : customer.getCreditTerm().getTermname();
                        personaccountId = customer.getAccount()==null?"":customer.getAccount().getID();
                        isHasaccess=customer.isActivate();
                    }else {
                        KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Vendor.class.getName(), (String) row[2]);
                        Vendor vendor = (Vendor) resultObject.getEntityList().get(0);
                        personid = vendor.getID();
                        personname = vendor.getName();
                        aliasname = vendor.getAliasname();
                        personcode = vendor.getAcccode() == null ? "" : vendor.getAcccode();
                        paymentterm = vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getTermname();
                        personaccountId = vendor.getAccount()==null?"":vendor.getAccount().getID();
                        isHasaccess=vendor.isActivate();
                    }
                }

                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                JSONObject obj = new JSONObject();
                if (withoutinventory) {
                    resultObject = accountingHandlerDAOobj.getObject(BillingDebitNote.class.getName(), (String) row[1]);
                    BillingDebitNote debitMemo = (BillingDebitNote) resultObject.getEntityList().get(0);
                    JournalEntry je = debitMemo.getJournalEntry();
                    //*** Attachments Documents SJ[ERP-16331] 
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", debitMemo.getID());
                    hashMap.put("companyid", debitMemo.getCompany().getCompanyID());
                    KwlReturnObject object = accVendorDAOObj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    obj.put("attachment", attachemntcount);
                    obj.put("billid", debitMemo.getID());
                    //*** Attachments Documents SJ[ERP-16331] 
                    obj.put("withoutinventory", withoutinventory);
                    obj.put("noteid", debitMemo.getID());
                    obj.put("companyid", debitMemo.getCompany().getCompanyID());
                    obj.put("companyname", debitMemo.getCompany().getCompanyName());
                    obj.put("noteno", debitMemo.getDebitNoteNumber());
                    obj.put("journalentryid", je.getID());
                    obj.put("currencysymbol", (debitMemo.getCurrency() == null ? currency.getSymbol() : debitMemo.getCurrency().getSymbol()));
                    obj.put("currencyid", (debitMemo.getCurrency() == null ? currency.getCurrencyID() : debitMemo.getCurrency().getCurrencyID()));
                    obj.put("entryno", je.getEntryNumber());
                    obj.put("personid", personid);
                    obj.put("personname", personname);
                    obj.put("aliasname", aliasname);
                    obj.put("personaccountid", personaccountId);
                    obj.put("hasAccess",isHasaccess);
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
                    obj.put("memo", debitMemo.getMemo());
                    obj.put("deleted", debitMemo.isDeleted());
                    obj.put("otherwise", false);
                    obj.put("openflag", false);
                    obj.put("cntype", 1);//creditMemo.getCntype());
                    obj.put("costcenterid", je.getCostcenter() == null ? "" : je.getCostcenter().getID());
                    obj.put("costcenterName", je.getCostcenter() == null ? "" : je.getCostcenter().getName());
                    obj.put("partlyJeEntryWithCnDn", je.getPartlyJeEntryWithCnDn());
                   
                    KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), je.getCompany().getCompanyID());
                    Iterator iterator = result.getEntityList().iterator();
                    while (iterator.hasNext()) {
                        JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                        Account account = null;
                        account = jed.getAccount();
                        //To do - need to test
                        if (account.getMastertypevalue() == Group.ACCOUNTTYPE_GST) {
                            if (!jed.isDebit()) {
                                tax = jed.getAmount();
                            }
                        }
                    }
                    result = accDebitNoteobj.getTotalTax_TotalDiscount_Billing(debitMemo.getID());
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
                    resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);
                    DebitNote debitMemo = (DebitNote) resultObject.getEntityList().get(0);
                    badDebtMap.put("companyid", debitMemo.getCompany().getCompanyID());
                    JournalEntry je = debitMemo.getJournalEntry();
                    Date debitNoteDate = null;
                    double externalCurrencyRate = 0d;
                    boolean isopeningBalanceInvoice = debitMemo.isIsOpeningBalenceDN();
                    String journalentryid="";
                    String jeentryno="";
                    if (debitMemo.isNormalDN()) {
                        je = debitMemo.getJournalEntry();
//                        debitNoteDate = je.getEntryDate();
                        debitNoteDate = debitMemo.getCreationDate();
                        externalCurrencyRate = je.getExternalCurrencyRate();
                        journalentryid=je.getID();
                        jeentryno=je.getEntryNumber();
                        List resultJe = accDebitNoteobj.getForeignGainLossJE(debitMemo.getID(), companyid);
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
                    
                    transactionCurrencyId = (debitMemo.getCurrency() == null ? currency.getCurrencyID() : debitMemo.getCurrency().getCurrencyID());
                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("currencysymboltransaction", (debitMemo.getCurrency() == null ? currency.getSymbol() : debitMemo.getCurrency().getSymbol()));      
//                    else {
//                        invoiceCreationDate = gReceipt.getCreationDate();
//                        externalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
//                    }
                    String currencyFilterForTrans = "";
                    boolean isNoteForPayment = false;
                    if (requestParams.get("isNoteForPayment") != null) {
                        isNoteForPayment = (Boolean) requestParams.get("isNoteForPayment");
                    }
                    if (requestParams.containsKey("currencyfilterfortrans")&&isNoteForPayment) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject curresultpayment = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyPayment = (KWLCurrency) curresultpayment.getEntityList().get(0);
                        obj.put("currencysymbol", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                        obj.put("currencyid", (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getCurrencyID()));
                    }else{
                        obj.put("currencysymbol", (debitMemo.getCurrency() == null ? currency.getSymbol() : debitMemo.getCurrency().getSymbol()));
                        obj.put("currencyid", (debitMemo.getCurrency() == null ? currency.getCurrencyID() : debitMemo.getCurrency().getCurrencyID()));
  
                    }
                    double amountdue=0.0;
                    double amountDueOriginal = 0.0;
                    if(debitMemo.getDntype() == 5 || debitMemo.getDntype() == Constants.DebitNoteForOvercharge){                     //debitMemo.getDntype()==5 In case of debit note against customer for malaysian country is 5
                        amountdue=debitMemo.getDnamountdue();
                        amountDueOriginal = debitMemo.getDnamountdue();
                    }else{
                        amountdue=debitMemo.isOtherwise() ? debitMemo.getDnamountdue() : 0;
                        amountDueOriginal = debitMemo.isOtherwise() ? debitMemo.getDnamountdue() : 0;
                    }
                    
                    
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, debitNoteDate, externalCurrencyRate);
                        amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                    }
                    KwlReturnObject baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, transactionCurrencyId, debitNoteDate, externalCurrencyRate);
                    obj.put("withoutinventory", withoutinventory);
                    /**
                     * Put GST document history.
                     */
                    if (debitMemo.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        obj.put("refdocid", debitMemo.getID());
                        fieldDataManagercntrl.getGSTDocumentHistory(obj);
                    }
                    obj.put("isReturnNote", debitMemo.getPurchaseReturn()==null? false:true);
                    //*** Attachments Documents SJ[ERP-16331] 
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", debitMemo.getID());
                    hashMap.put("companyid", debitMemo.getCompany().getCompanyID());
                    KwlReturnObject object = accVendorDAOObj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    obj.put("attachment", attachemntcount);
                    obj.put("billid", debitMemo.getID());
                    //*** Attachments Documents SJ[ERP-16331] 
                    obj.put("noteid", debitMemo.getID());
                    obj.put("isOldRecord", debitMemo.isOldRecord());
                    obj.put("companyid", debitMemo.getCompany().getCompanyID());
                    obj.put("companyname", debitMemo.getCompany().getCompanyName());
                    obj.put("noteno", debitMemo.getDebitNoteNumber());
                    obj.put("journalentryid", journalentryid);
                    obj.put("entryno", jeentryno);
                    obj.put("personid", personid);
                    obj.put("personname", personname);
                    obj.put("aliasname", aliasname);
                    obj.put("personaccountid", personaccountId);
                    obj.put("hasAccess",isHasaccess);
                    obj.put("gstCurrencyRate", debitMemo.getGstCurrencyRate());
                    if (countryId != null && countryId == Constants.indian_country_id) {
                        Map<String, Object> filterMap = new HashMap();
                        filterMap.put("debitNote.ID", debitMemo.getID());
                        String[] columnNames = debitMemo.getCustomer() != null ? new String[]{"invoice.ID"} : new String[]{"goodsReceipt.ID"};
                        List invoiceIDs = kwlCommonTablesDAOObj.getRequestedObjectFieldsInCollection(DebitNoteInvoiceMappingInfo.class, columnNames, filterMap);
                        if (invoiceIDs != null && invoiceIDs.size() > 0) {
                            StringBuilder invoiceBuilder = new StringBuilder();
                            for (Object invoiceId : invoiceIDs) {
                                if (!StringUtil.isNullObject(invoiceId)) {
                                    if (invoiceBuilder.length() > 0) {
                                        invoiceBuilder.append(",").append(invoiceId);
                                    } else {
                                        invoiceBuilder.append(invoiceId);
                                    }
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
//                    obj.put("dateinuserformat", userdf.format(je.getEntryDate()));
                    obj.put("dateinuserformat", userdf.format(debitMemo.getCreationDate()));
                    double paidAmount = debitMemo.isOtherwise() ? debitMemo.getDnamount() : details.getAmount();
                    obj.put("amount", paidAmount);
                    KwlReturnObject paidAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, paidAmount, transactionCurrencyId, debitNoteDate, externalCurrencyRate);
                    obj.put("paidamountinbase", authHandler.round((Double) paidAmt.getEntityList().get(0), companyid));
                    obj.put("includingGST", debitMemo.isIncludingGST());
                    obj.put("currencyname", (debitMemo.getCurrency() == null ? currency.getName() : debitMemo.getCurrency().getName()));
                    obj.put("currencycode", (debitMemo.getCurrency() == null ? currency.getCurrencyCode() : debitMemo.getCurrency().getCurrencyCode()));
                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0&&amountdue<=0) ? 0 : (amountdue/amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal,companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal,companyid));
                    obj.put("amountdueinbase", authHandler.round((Double) baseAmt.getEntityList().get(0), companyid));
                    obj.put("amountinbase", authHandler.round(debitMemo.getDnamountinbase(),companyid));
                    if(isPostingDateCheck){
                      obj.put("date", df.format(debitMemo.getCreationDate()));
                    }else{
                      obj.put("date", df.format(je.getEntryDate()));
                    }
                    obj.put("memo", debitMemo.getMemo());
                    obj.put("createdby",debitMemo.getCreatedby() == null ? "" : StringUtil.getFullName(debitMemo.getCreatedby()));
                    obj.put("deleted", debitMemo.isDeleted());
                    obj.put("otherwise", debitMemo.isOtherwise());
                    obj.put("isprinted", debitMemo.isPrinted());
                    obj.put("openflag", debitMemo.isOpenflag());
                    obj.put("isCreatedFromReturnForm", (debitMemo.getPurchaseReturn()!=null)?true:false);
                    obj.put("cntype", debitMemo.getDntype());
                    obj.put("costcenterid", debitMemo.getCostcenter() == null ? "" : debitMemo.getCostcenter().getID());
                    obj.put("costcenterName", debitMemo.getCostcenter() == null ? "" : debitMemo.getCostcenter().getName());
                    obj.put("partlyJeEntryWithCnDn", je.getPartlyJeEntryWithCnDn());
                    obj.put("accountid", debitMemo.getAccount()==null?"":debitMemo.getAccount().getID());
                    obj.put("accountnames", debitMemo.getAccount()==null?"":debitMemo.getAccount().getName());
                    obj.put("externalcurrencyrate", je.getExternalCurrencyRate());
                    obj.put(Constants.SUPPLIERINVOICENO, debitMemo.getSupplierInvoiceNo()!=null?debitMemo.getSupplierInvoiceNo():"");
                    obj.put(Constants.MVATTRANSACTIONNO, debitMemo.getMvatTransactionNo()!=null?debitMemo.getMvatTransactionNo():"");
                    if (debitMemo.getSalesPerson() != null) {       //ERP-28249 Debit note against customer for malaysian country.
                        obj.put("salesPerson", debitMemo.getSalesPerson() != null ? debitMemo.getSalesPerson().getValue(): "");
                        obj.put("salesPersonID", debitMemo.getSalesPerson() != null ? debitMemo.getSalesPerson().getID() : "");
                    }
                    if (debitMemo.getAgent() != null) {
                        obj.put("agent", debitMemo.getAgent().getValue());
                        obj.put("agentid", debitMemo.getAgent().getID());
                    }
                    if (isApprovalPendingReport) {//these data only need for pending Approval report of CN
                        obj = getDebitNoteApprovalPendingJsonData(obj, debitMemo.getID(), companyid, userid, userName);
                    }
                    Set<DebitNoteTaxEntry> dnTaxEntryDetails = debitMemo.getDnTaxEntryDetails();
                    String reason = "";
                    double totalCnTax = 0;
                    double totalTermAmount = 0;
                    boolean considerTermAmount = false; // This will be used DN is not linked with any purchase return and country = India.
                    if (dnTaxEntryDetails != null && !dnTaxEntryDetails.isEmpty()) {

                        for (DebitNoteTaxEntry noteTaxEntry : dnTaxEntryDetails) {
                            reason += ((noteTaxEntry.getReason() != null) ? noteTaxEntry.getReason().getValue() : "") + ",";
                            if (noteTaxEntry.isDebitForMultiCNDN()) {
                                totalCnTax -= noteTaxEntry.getTaxamount();
                            } else {
                                totalCnTax += noteTaxEntry.getTaxamount();
                            }
                            if (countryId != null && countryId == Constants.indian_country_id && debitMemo.getPurchaseReturn() == null && !StringUtil.isNullOrEmpty(noteTaxEntry.getProductid())) {
                                /**
                                 * If debit note is not linked to purchase
                                 * return then show tax amount using termamount
                                 * column.
                                 */
                                if (noteTaxEntry.isDebitForMultiCNDN()) {
                                    totalTermAmount -= noteTaxEntry.getTermAmount();
                                } else {
                                    totalTermAmount += noteTaxEntry.getTermAmount();
                                }
                                considerTermAmount = true;
                            }
                        }
                    }
                    double taxAmount = 0.0;
                    if (debitMemo.getDntype() == 5 || debitMemo.getDntype() == Constants.DebitNoteForOvercharge) {
                        obj.put("amountinbase", authHandler.round((Double) paidAmt.getEntityList().get(0), companyid));
                        Set<DebitNoteAgainstCustomerGst> dnTaxGstDetails = debitMemo.getRowsGst();
                        totalCnTax = 0;
                        if (dnTaxGstDetails != null && !dnTaxGstDetails.isEmpty()) {

                            for (DebitNoteAgainstCustomerGst noteTaxGstEntry : dnTaxGstDetails) {
                                reason += ((noteTaxGstEntry.getReason() != null) ? noteTaxGstEntry.getReason().getValue() : "") + ",";
                                totalCnTax += noteTaxGstEntry.getRowTaxAmount();
                            }
                        }
                        double dnAmount = 0.0;
                        double dnAmountExcludingTax = 0.0;
                        Tax taxObj = debitMemo.getTax();
                        KwlReturnObject result1 = accJournalEntryobj.getJournalEntryDetail(debitMemo.getJournalEntry().getID(), debitMemo.getJournalEntry().getCompany().getCompanyID());
                        Iterator iterator = result1.getEntityList().iterator();
                        boolean taxflag = false;
                        while (iterator.hasNext()) {
                            JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                            Account account = null;
                            account = jed.getAccount();
                    
                            if (taxObj!=null && !jed.isDebit() && account.getID().equals(taxObj.getAccount().getID())) {
                                taxAmount += jed.getAmount();
                                taxflag = true;
                            }
                            if (jed.isDebit()) {
                                dnAmount += jed.getAmount();
                            }
                        }
                        dnAmountExcludingTax = dnAmount - taxAmount;
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
                    KwlReturnObject taxAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, authHandler.round(totalCnTax, companyid), transactionCurrencyId, debitNoteDate, externalCurrencyRate);
                    obj.put("taxamountinbase", authHandler.round((Double) taxAmt.getEntityList().get(0), companyid));
                    obj.put("lasteditedby", debitMemo.getModifiedby() == null ? "" : (debitMemo.getModifiedby().getFirstName() + " " + debitMemo.getModifiedby().getLastName()));
                     /*
                     * If line level tax is present then includeprotax field is true 
                     */
                    if (debitMemo.getDntype() == 5 || debitMemo.getDntype() == Constants.DebitNoteForOvercharge) {
                        Set<DebitNoteAgainstCustomerGst> cndetailsGst = (Set<DebitNoteAgainstCustomerGst>) debitMemo.getRowsGst();
                       for (DebitNoteAgainstCustomerGst noteDetail : cndetailsGst) {
                            if (noteDetail.getTax() != null) {
                                checkTax = true;
                                break;
                            }
                        }
                    }
                    obj.put("includeprotax", checkTax);
                    if(debitMemo.getTax()!=null){
                        obj.put("gTaxId", debitMemo.getTax().getID());
                    }
                    double cnTotalAmount=debitMemo.isOtherwise() ? debitMemo.getDnamount() : details.getAmount();
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
                    obj.put("currencyname", (debitMemo.getCurrency() == null ? currency.getName(): debitMemo.getCurrency().getName()));
                    obj.put("currencycode", (debitMemo.getCurrency() == null ? currency.getCurrencyCode(): debitMemo.getCurrency().getCurrencyCode()));
                    BillingShippingAddresses addresses = debitMemo.getBillingShippingAddresses();
                    if (cntype == 4 && transactiontype != 8) {//CN against customer  
                        AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    } else {
                        AccountingAddressManager.getTransactionAddressJSON(obj, addresses, true);
                    }
                    obj.put(Constants.SEQUENCEFORMATID, debitMemo.getSeqformat() != null ? debitMemo.getSeqformat().getID() : "");
                    KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), je.getCompany().getCompanyID());
                    Iterator iterator = result.getEntityList().iterator();
                    while (iterator.hasNext()) {
                        JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                        Account account = null;
                        account = jed.getAccount();
                        obj.put("accid", account.getID());
                        obj.put("accountid", account.getID());
                        //To do - need to test
                        if (account.getMastertypevalue() == Group.ACCOUNTTYPE_GST) {
                            if (!jed.isDebit()) {
                                tax = jed.getAmount();
                            }
                        }
                    }
                    result = accDebitNoteobj.getTotalTax_TotalDiscount(debitMemo.getID());
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
                    boolean isLinkedTransaction = accDebitNoteobj.isDebitNoteLinkedToOtherTransaction("DebitNote", debitMemo.getID());
                    obj.put(Constants.IS_LINKED_TRANSACTION, isLinkedTransaction);
                    obj.put("noteSubTotal", details.getAmount() + totDiscount - totTax);
                    obj.put("notetax", tax);
                    obj.put("totalTax", totTax);
                    obj.put("totalDiscount", totDiscount);
                    int isReval = 0;
                    KwlReturnObject brdAmt = accGoodsReceiptobj.getRevalFlag(debitMemo.getID());
                    List reval = brdAmt.getEntityList();
                    if (!reval.isEmpty() && (Long) reval.get(0) > 0) {
                        isReval = 1;
                    }
                    obj.put("isreval", isReval);
                    boolean isLinked = false;                     // Flag for identifying whether the DN is linked to some purchase invoice or not.
                    String linkedDate = null;
                    Set<DebitNoteDetail> dndetails = debitMemo.getRows();
                    for (DebitNoteDetail noteDetail : dndetails) {
                        if (noteDetail.getGoodsReceipt() != null) {
                            /*
                             * Applicable for Malaysian Country. Checking if any linked invoice is claimed 
                             */
                            if (noteDetail.getGoodsReceipt().getBadDebtType() == 1 || noteDetail.getGoodsReceipt().getBadDebtType() == 2) {
                                badDebtMap.put("invoiceid", noteDetail.getGoodsReceipt().getID());
                                badDebtResult = accGoodsReceiptobj.getBadDebtPurchaseInvoiceMappingForGoodsReceipt(badDebtMap);
                                List<BadDebtPurchaseInvoiceMapping> maplist = badDebtResult.getEntityList();
                                if (maplist != null && !maplist.isEmpty()) {
                                    BadDebtPurchaseInvoiceMapping mapping = maplist.get(0);
//                                    if (!isLinkedInvoiceClaimed && mapping.getBadDebtClaimedDate() > noteDetail.getGrLinkDate()) {
                                    if (!isLinkedInvoiceClaimed && (mapping.getBadDebtClaimedDate().after(noteDetail.getGrLinkDate()))) {
                                        isLinkedInvoiceClaimed = true;
                                        obj.put("isLinkedInvoiceIsClaimed", isLinkedInvoiceClaimed);
                                    }
                                }
                            }
                            isLinked = true;
                            linkedDate = sdf.format(noteDetail.getGrLinkDate());
                            break;
                        } else if (!StringUtil.isNullOrEmpty(noteDetail.getCreditNoteId())) {
                            isLinked = true;
                            linkedDate = sdf.format(noteDetail.getGrLinkDate());
                            break;
                        } else {
                            /*  Checking Whether DN against Customer 
                            
                             is linking with CN against Customer or not*/
                            Boolean isRecord = accDebitNoteobj.checkDNLinking(debitMemo.getID());
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
                     * Debit note will be allowed to copy in following case-
                     * 1. DN against purchase invoice - NOT allowed
                     * 2. DN otherwise - allowed if no invoice is linked to DN at time of creation or later on
                     * 3. DN against Customer - Allowed
                     */
                    if(debitMemo.getDntype() == Constants.DebitNoteAgainstPurchaseInvoice){
                        obj.put("isCopyAllowed", false);
                    } else if(debitMemo.getDntype() == Constants.DebitNoteOtherwise){
                        boolean copyFlag =true;
//                        Set<DebitNoteDetail> dndetails =debitMemo.getRows();
                        for(DebitNoteDetail dndetail:dndetails){
                            if(dndetail.getGoodsReceipt()!=null){
                                copyFlag=false;
                                break;
                            }
                        }
                        obj.put("isCopyAllowed", copyFlag);
                    } else if(debitMemo.getDntype() == Constants.DebitNoteAgainstCustomer){
                        obj.put("isCopyAllowed", true);
                    } else {
                        obj.put("isCopyAllowed", false);
                    }
                    HashMap<String,Object> reqParams1 = new HashMap<>();
                    reqParams1.put("dnid",debitMemo.getID());
                    reqParams1.put("companyid",debitMemo.getCompany().getCompanyID());
                    KwlReturnObject linkResult=accDebitNoteobj.getLinkDetailReceiptToDebitNote(reqParams1);
                    if(!linkResult.getEntityList().isEmpty()){
                        obj.put("isNoteLinkedToAdvancePayment", true);
                    }
                    // for Custom Fields
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> cnDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                    Detailfilter_names.add("companyid");
                    Detailfilter_params.add( debitMemo.getCompany().getCompanyID());
                    Detailfilter_names.add("journalentryId");
                    Detailfilter_params.add( debitMemo.getJournalEntry().getID());
                    Detailfilter_names.add("moduleId");
                    Detailfilter_params.add(Constants.Acc_Debit_Note_ModuleId + "");
                    cnDetailRequestParams.put("filter_names", Detailfilter_names);
                    cnDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomData(cnDetailRequestParams);
                    if (idcustresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                        boolean isExport = (boolean) ((requestParams.get("isExport") == null) ? false : requestParams.get("isExport"));
                        JSONObject params = new JSONObject();
                        params.put("isExport", isExport);
                        params.put(Constants.userdf, userDateFormat);
                        if (requestParams.containsKey("browsertz") && requestParams.get("browsertz") != null) {
                            params.put("browsertz", requestParams.get("browsertz").toString());
                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                        
                        
//                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
//                            String coldata = varEntry.getValue().toString();
//                            
//                            if (customFieldMap.containsKey(varEntry.getKey())) {
//
//                                String value = "";
//                                String Ids[] = coldata.split(",");
//                                for (int i = 0; i < Ids.length; i++) {
//                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
//                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
//                                    if (fieldComboData != null) {
//                                        if ((fieldComboData.getField().getFieldtype() == 12 || fieldComboData.getField().getFieldtype() == 7 ) && !isExport) {
//                                            value += Ids[i] != null ? Ids[i] + "," : ",";
//                                        } else {
//                                            value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
//                                        }
//
//                                    }
//                                }
//                                if (!StringUtil.isNullOrEmpty(value)) {
//                                    value = value.substring(0, value.length() - 1);
//                                }
//                                obj.put(varEntry.getKey(), value);
//                            } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
//                                if (isExport) {
//                                    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                                    obj.put(varEntry.getKey(), sdf.format(Long.parseLong(coldata)));
//                                } else {
//                                    obj.put(varEntry.getKey(), coldata);
//                                }
//                            } else {
//                                if (!StringUtil.isNullOrEmpty(coldata)) {
//                                    String[] coldataArray = coldata.split(",");
//                                    String Coldata = "";
//                                    for (int countArray = 0; countArray < coldataArray.length; countArray++) {
//                                        Coldata += "'" + coldataArray[countArray] + "',";
//                                    }
//                                    Coldata = Coldata.substring(0, Coldata.length() - 1);
//                                    String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
//                                    obj.put(varEntry.getKey(), coldata);
//                                    obj.put(varEntry.getKey() + "_Values", ColValue);
//                                }
//                            }
//                        }
                    }
                }
                if (requestParams.containsKey("isReceipt") && obj.optDouble("amountdue", 0.0) != 0) {
                    JArr.put(obj);
                } else if (!requestParams.containsKey("isReceipt")) {
                    JArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }
    
    @Override
    public JSONObject getDebitNoteApprovalPendingJsonData(JSONObject obj, String noteid, String companyid, String userid, String userName) throws ServiceException {
        try {
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), noteid);
            DebitNote debitNote = (DebitNote) resultObject.getEntityList().get(0);
            if (debitNote != null) {
                int approvallevel = debitNote.getApprovestatuslevel();
                double amountInBase = authHandler.round(debitNote.getDnamountinbase(), companyid);
                String approvalStatus = "";
                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                String multipleRuleids="";
                if (approvallevel < 0) {//will be negartive for rejected
                    approvalStatus = "Rejected";
                } else if (approvallevel < 11) {//will be less than 11 for pending record 
                    String ruleid = "", userRoleName = "";
                    HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                    qdDataMap.put("companyid", companyid);
                    qdDataMap.put("level", approvallevel);
                    qdDataMap.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
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
                            Iterator useritr = userResult.getEntityList().iterator();
                            while (useritr.hasNext()) {
                                Object[] userrow = (Object[]) useritr.next();
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
                    cnApproveMap.put("companyid", companyid);
                    cnApproveMap.put("level", approvallevel);
                    cnApproveMap.put("totalAmount", String.valueOf(amountInBase));
                    cnApproveMap.put("currentUser", userid);
                    cnApproveMap.put("fromCreate", false);
                    cnApproveMap.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                    hasApprovalAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(cnApproveMap);
                }
                obj.put("hasApprovalAuthority", hasApprovalAuthority);

                int nextApprovalLevel = 11;
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("level", approvallevel + 1);
                qdDataMap.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
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
                obj.put("cntype",debitNote.getDntype());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesJson : " + ex.getMessage(), ex);
        }
        return obj;
    }
    
    public JSONArray getDebitNoteRowsJson(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray JArr = new JSONArray();
        Boolean isExport = false;
        Boolean isForReport = false;
        String companyid = "";
        DateFormat userDateFormat=null;
        if(requestParams.containsKey("userdateformatter") && requestParams.get("userdateformatter")!=null){
            userDateFormat=(DateFormat)requestParams.get("userdateformatter");            
        }
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");
            companyid = (String) requestParams.get("companyid");
            String description="";
            if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) {
                isExport = (Boolean) requestParams.get("isExport");
            }
            if (requestParams.containsKey("isForReport") && requestParams.get("isForReport") != null) {
                isForReport = (Boolean) requestParams.get("isForReport");
            }
            boolean isEdit = requestParams.containsKey(Constants.isEdit) && requestParams.get(Constants.isEdit) != null ? (Boolean) requestParams.get(Constants.isEdit) : false;
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) comp.getEntityList().get(0);
            int countryid = Integer.parseInt(company.getCountry().getID());

            String[] creditNote = (String[]) requestParams.get("bills");
            int i = 0;
            boolean dnAgainstCI = false;
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat userdateformatter = null;
            userdateformatter = requestParams.get("userdateformatter") != null ? (DateFormat) requestParams.get("userdateformatter") : null;
            HashMap<String, Object> dnRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("debitNote.ID");
            order_by.add("srno");
            order_type.add("asc");
            dnRequestParams.put("filter_names", filter_names);
            dnRequestParams.put("filter_params", filter_params);
            dnRequestParams.put("order_by", order_by);
            dnRequestParams.put("order_type", order_type);

            while (creditNote != null && i < creditNote.length) {
//                DebitNote dn = (DebitNote) session.get(DebitNote.class, creditNote[i]);
                KwlReturnObject dnresult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), creditNote[i]);
                DebitNote dn = (DebitNote) dnresult.getEntityList().get(0);
//                Iterator itr = dn.getRows().iterator();
                filter_params.clear();
                filter_params.add(dn.getID());
                
                if (dn.getDntype() == Constants.DebitNoteForOvercharge) {
                    getDebitNoteForOverchargeRows(requestParams,dn,JArr);
                    i++;
                } else if(dn.getDntype() != 5){
                    KwlReturnObject grdresult = accDebitNoteobj.getDebitNoteDetails(dnRequestParams);
                    Iterator itr = grdresult.getEntityList().iterator();
                    dnAgainstCI = dn.getDntype() == 4 ? true : false;
                    boolean cnusedflag = true;
                    if (dn.getDntype() != 3 && dn.getDntype() != 4 && dn.isOtherwise() && dn.getDnamount() == dn.getDnamountdue()) {
                        cnusedflag = false;
                    }
                    while (itr.hasNext()) {
                        DebitNoteDetail row = (DebitNoteDetail) itr.next();
                        if (!cnusedflag) {
                            break;
                        }
                        JSONObject obj = new JSONObject();
                        obj.put(Constants.userdf,userDateFormat);
                        GoodsReceipt grObj = row.getGoodsReceipt();
                        Invoice invObj = row.getInvoice();
                        if (grObj != null || invObj != null) {
                            Double invoiceAmount = 0d;
                            Date invoiceCreationDate = null;
                            invoiceCreationDate = dnAgainstCI ? (invObj.getCreationDate()) : (grObj.getCreationDate());
                            if (dnAgainstCI ? (invObj.isIsOpeningBalenceInvoice()) : (grObj.isIsOpeningBalenceInvoice())) {
                                invoiceAmount = dnAgainstCI ? (invObj.getOriginalOpeningBalanceAmount()) : (grObj.getOriginalOpeningBalanceAmount());
                                obj.put("isOpeningInvoice", true);
                            } else {
    //                            invoiceCreationDate = dnAgainstCI ? (invObj.getJournalEntry().getEntryDate()) : (grObj.getJournalEntry().getEntryDate());
                                invoiceAmount = dnAgainstCI ? (invObj.getCustomerEntry().getAmount()) : (grObj.getVendorEntry().getAmount());
                                obj.put("isOpeningInvoice", false);
                            }
                            obj.put("invcreationdate", df.format(invoiceCreationDate));
                            obj.put("invduedate", dnAgainstCI ? (df.format(row.getInvoice().getDueDate())) : (df.format(row.getGoodsReceipt().getDueDate())));
                            if (userdateformatter != null) {
                                obj.put("invduedateinuserdateformat", dnAgainstCI ? userdateformatter.format(row.getInvoice().getDueDate()) : userdateformatter.format(row.getGoodsReceipt().getDueDate()));
                                obj.put("invcreationdateinuserdateformat", userdateformatter.format(invoiceCreationDate));
                            }
                            obj.put("invamountdue", dnAgainstCI ? ((invObj.isIsOpeningBalenceInvoice() ? invObj.getOpeningBalanceAmountDue() : invObj.getInvoiceamountdue())) : (grObj.isIsOpeningBalenceInvoice() ? grObj.getOpeningBalanceAmountDue() : grObj.getInvoiceamountdue()));
                            obj.put("invamount", invoiceAmount);
                            obj.put("withoutinventory", false);
                            obj.put("billid", dn.getID());
                            obj.put("billno", dn.getDebitNoteNumber());
                            obj.put("srno", row.getSrno());
                            obj.put("rowid", row.getID());
                            obj.put("productid", dnAgainstCI ? (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getID()) : (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getInventory().getProduct().getID()));
                            obj.put("productdetail", dnAgainstCI ? (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getName()) : (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getInventory().getProduct().getName()));
                            obj.put("unitname", dnAgainstCI ? (row.getInventory() != null ? row.getInventory().getUom().getNameEmptyforNA() : row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()) : (row.getInventory() != null && row.getInventory().getUom() != null) ? row.getInventory().getUom().getNameEmptyforNA() : (row.getGoodsReceiptRow() != null && row.getGoodsReceiptRow().getInventory() != null && row.getGoodsReceiptRow().getInventory().getProduct() != null && row.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure() != null) ? row.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA() : "");
                            obj.put("desc", dnAgainstCI ? (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getDescription()) : row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getInventory().getProduct().getDescription());

                            obj.put("remark", !StringUtil.isNullOrEmpty(row.getRemark()) ? row.getRemark() : "");
                            obj.put("currencycode", dnAgainstCI ? ((invObj.getCurrency() != null ? (invObj.getCurrency().getCurrencyCode()) : ((dn.getCurrency() == null ? currency.getCurrencyCode() : dn.getCurrency().getCurrencyCode())))) : (grObj.getCurrency() != null ? (grObj.getCurrency().getCurrencyCode()) : ((dn.getCurrency() == null ? currency.getCurrencyCode() : dn.getCurrency().getCurrencyCode()))));
                            obj.put("currencyname", dnAgainstCI ? ((invObj.getCurrency() != null ? (invObj.getCurrency().getName()) : ((dn.getCurrency() == null ? currency.getName() : dn.getCurrency().getName())))) : (grObj.getCurrency() != null ? (grObj.getCurrency().getName()) : ((dn.getCurrency() == null ? currency.getName() : dn.getCurrency().getName()))));
                            obj.put("currencysymbol", dnAgainstCI ? ((invObj.getCurrency() != null ? (invObj.getCurrency().getSymbol()) : ((dn.getCurrency() == null ? currency.getSymbol() : dn.getCurrency().getSymbol())))) : (grObj.getCurrency() != null ? (grObj.getCurrency().getSymbol()) : ((dn.getCurrency() == null ? currency.getSymbol() : dn.getCurrency().getSymbol()))));
                            obj.put("invstore", (StringUtil.isNullOrEmpty(row.getInvstoreid())) ? "" : row.getInvstoreid());
                            obj.put("invlocation", (StringUtil.isNullOrEmpty(row.getInvlocid())) ? "" : row.getInvlocid());
                            obj.put("currencycodeforinvoice", dnAgainstCI ? ((invObj.getCurrency() != null ? (invObj.getCurrency().getCurrencyCode()) : ((dn.getCurrency() == null ? currency.getCurrencyCode() : dn.getCurrency().getCurrencyCode())))) : ((grObj.getCurrency() != null ? (grObj.getCurrency().getCurrencyCode()) : ((dn.getCurrency() == null ? currency.getCurrencyCode() : dn.getCurrency().getCurrencyCode())))));
                            if (dn.isOtherwise() && row.getPaidinvflag() != 1) {
                                obj.put("transectionid", dnAgainstCI ? (row.getInvoice() == null ? "" : row.getInvoice().getID()) : (row.getGoodsReceipt() == null ? "" : row.getGoodsReceipt().getID()));
                                obj.put("transectionno", dnAgainstCI ? (row.getInvoice() == null ? "" : row.getInvoice().getInvoiceNumber()) : row.getGoodsReceipt() == null ? "" : row.getGoodsReceipt().getGoodsReceiptNumber());
                                obj.put("memo", dnAgainstCI ? (row.getInvoice() == null ? "" : row.getInvoice().getMemo()) : row.getGoodsReceipt() == null ? "" : row.getGoodsReceipt().getMemo());
                            } else {
                                obj.put("transectionid", dnAgainstCI ? (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInvoice().getID()) : (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getID()));
                                obj.put("transectionno", dnAgainstCI ? (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInvoice().getInvoiceNumber()) : row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getGoodsReceiptNumber());
                                obj.put("memo", dnAgainstCI ? (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInvoice().getMemo()) : row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getMemo());
                            }
                            obj.put("otherwise", dn.isOtherwise());
                            //                    obj.put("transectionid", row.getGoodsReceiptRow().getGoodsReceipt().getID());
                            //                    obj.put("transectionno", row.getGoodsReceiptRow().getGoodsReceipt().getGoodsReceiptNumber());
                            Discount disc = row.getDiscount();
                            if (disc != null) {
                                obj.put("discount", row.getDiscount().getAmountinInvCurrency());
                                obj.put("paidAmountinTransactionCurrency", disc.getDiscountValue());
                            } else {
                                obj.put("discount", 0);
                                obj.put("paidAmountinTransactionCurrency", 0);
                            }
                            obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                            obj.put("quantity", row.getQuantity());
                            obj.put("taxamount", row.getTaxAmount());
                            obj.put("paidinvflag", row.getPaidinvflag());
                            obj.put("cntype", dn.getDntype());
                            obj.put("grlinkdate", row.getGrLinkDate() != null ? df.format(row.getGrLinkDate()) : "");//Linking invoice date in report CN,DN
                            JArr.put(obj);
                        } else if (!StringUtil.isNullOrEmpty(row.getCreditNoteId())) {// End of Main If
                            List<Object[]> cnList = accDebitNoteobj.getCreditNoteDetailsForDebitNote(row.getCreditNoteId(), dn.getCompany().getCompanyID());
                            if (cnList != null && !cnList.isEmpty()) {
                                for (Object[] cnRow : cnList) {
                                    if (cnRow[4].equals(1)) {
                                        /*
                                         * if credit note is opening
                                         */
                                        obj.put("isOpeningDnCn", true);
                                    }
                                    obj.put("isCreditNote", true);
                                    obj.put("invcreationdate", df.format((Date) cnRow[2])); //Creation date
                                    obj.put("invamountdue", (Double) cnRow[5]);  //Openingcn Amount due/Cn amount
                                    obj.put("invamount", (Double) cnRow[3]);// getOriginalOpeningBalanceBaseAmount
                                    obj.put("withoutinventory", false);
                                    obj.put("billid", dn.getID());
                                    obj.put("billno", dn.getDebitNoteNumber());
                                    obj.put("isCnDndetails", true);
                                    obj.put("creditNoteId", row.getID());
                                    obj.put("transectionid", cnRow[0]);
                                    obj.put("transectionno", cnRow[1]);
                                    JArr.put(obj);
                                }
                            }

                        }
                    }
                    i++;
                    getAccountDetailsForDebitNote(dn, JArr, isExport, isForReport, companyid, userDateFormat);//I have written this function to get account details on expander click of credit note in CN report.
                    JSONArray sortedArray = new JSONArray();
                    sortedArray = authHandler.sortJson(JArr);
                    if (sortedArray.length() == JArr.length()) {
                        JArr = sortedArray;
                    }
                } else {
                    filter_params.clear();
                    filter_params.add(dn.getID());
                    KwlReturnObject invdResult = accDebitNoteobj.getDebitNoteDetailsGst(dnRequestParams);
                    Iterator itr = invdResult.getEntityList().iterator();
                    
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(dn.getCompany().getCompanyID(), Constants.Acc_Debit_Note_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

                    /*
                     * Iterate Debit Note Against Customer details object
                     */
                    String invoiceId = "";
                    while (itr.hasNext()) {
                        DebitNoteAgainstCustomerGst row = (DebitNoteAgainstCustomerGst) itr.next();
                        Invoice invObj = row.getCidetails().getInvoice();
                        
                        if (invObj.getID().equalsIgnoreCase(invoiceId) && !isEdit) {//This is temporary changes. Need to show product in Debit Note for Overcharge/Undercharge.
                            continue;
                        }
                        JSONObject obj = new JSONObject();
                        obj.put("billid", dn.getID());
                        obj.put("billno", dn.getDebitNoteNumber());
                        obj.put("externalcurrencyrate", dn.getExternalCurrencyRate());
                        obj.put("srno", row.getSrno());
                        obj.put("rowid", row.getID());
                        obj.put("currencysymbol", (dn.getCurrency() == null ? currency.getSymbol() : dn.getCurrency().getSymbol()));
                        obj.put("productid", row.getProduct().getID());
                        obj.put("productname", row.getProduct().getName());
                        obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());

                        if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                            description = row.getDescription();
                        } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                            description = row.getProduct().getDescription();
                        } else {
                            description = "";
                        }
                        //try {
                            obj.put("desc", StringUtil.DecodeText(description));
                        /*} catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(accDebitNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }*/
                        obj.put("type", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getName());
                        obj.put("transectionid", invObj.getID());
                        obj.put("transectionno", invObj.getInvoiceNumber());
//                        obj.put("invcreationdate", df.format(invObj.getJournalEntry().getEntryDate()));
                        obj.put("invcreationdate", df.format(invObj.getCreationDate()));
                        obj.put("invamountdue", invObj.getInvoiceamount());
                        obj.put("invamount", invObj.getInvoiceamount());
                        obj.put("grlinkdate", dn.getCreationDate()!=null ? df.format(dn.getCreationDate()) : "");
                        obj.put("invduedate", df.format(invObj.getDueDate()));
                        obj.put("pid", row.getProduct().getProductid());
                        obj.put("memo", row.getRemark());
                        obj.put("reason", (row.getReason() != null && !isExport) ? row.getReason().getID() : "");
                        obj.put("prtaxid", (row.getTax() != null) ? row.getTax().getID() : "");
                        obj.put("taxamount", row.getRowTaxAmount());
                        obj.put("taxamountforlinking", row.getRowTaxAmount());
                        obj.put("discountispercent", row.getDiscountispercent());
                        obj.put("prdiscount", row.getDiscount());
                        obj.put("quantity", row.getActualQuantity());
                        obj.put("dquantity", row.getReturnQuantity());
                        if (dn.getDntype() == 5) {
                            if (row.getCidetails() != null) {
                                obj.put("linkto", row.getCidetails().getInvoice().getInvoiceNumber());
                                obj.put("linkid", row.getCidetails().getInvoice().getID());
                                obj.put("rowid", row.getCidetails().getID());
                                obj.put("savedrowid", row.getCidetails().getInvoice());
                                obj.put("linktype", 1);
                            } else {
                                obj.put("linkto", "");
                                obj.put("linkid", "");
                                obj.put("linktype", -1);
                            }
                            obj.put("includeprotax", (row.getTax() != null));
                            obj.put("cntype", dn.getDntype());
                        }
                        double baseuomrate = row.getBaseuomrate();
                        if (row.getUom() != null) {
                            obj.put("uomid", row.getUom().getID());
                        } else {
                            obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                        }
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(row.getReturnQuantity(), baseuomrate, companyid));
                        obj.put("baseuomrate", baseuomrate);
                        obj.put("copyquantity", row.getReturnQuantity());
                        try {
                            obj.put("description", StringUtil.DecodeText(description));
                        } catch (Exception ex) {
                            Logger.getLogger(accDebitNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        obj.put("remark", row.getRemark());
                        obj.put("rate", row.getRate());
                        
                        //For Detail Export
                        if (userDateFormat != null) {
                            obj.put("invcreationdateinuserdateformat", userDateFormat.format(invObj.getCreationDate()));
                            obj.put("invduedateinuserdateformat", userDateFormat.format(invObj.getDueDate()));
                        }
                        obj.put("currencycodeforinvoice", invObj.getCurrency().getCurrencyCode());
                        /**
                         * Get Debit Note Line level Term Details in Overcharge/Undercharge case for US Country
                         */
                        if (countryid == Constants.USA_country_id) { // Fetch  term details of Product
                            JSONObject json = new JSONObject();
                            json.put("debitnotedetail", row.getID());
                            KwlReturnObject result6 = accDebitNoteobj.getDebitNoteDetailTermMap(json);
                            if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                                ArrayList<DebitNoteDetailTermMap> productTermDetail = (ArrayList<DebitNoteDetailTermMap>) result6.getEntityList();
                                JSONArray DNTermJsonArry = new JSONArray();
                                double termAccount = 0.0;
                                for (DebitNoteDetailTermMap DNTermsMapObj : productTermDetail) {
                                    JSONObject DNTermJsonObj = new JSONObject();
                                    DNTermJsonObj.put("id", DNTermsMapObj.getId());
                                    DNTermJsonObj.put("termid", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getId());
                                    DNTermJsonObj.put("productentitytermid", DNTermsMapObj.getEntitybasedLineLevelTermRate() != null ? DNTermsMapObj.getEntitybasedLineLevelTermRate().getId() : "");
                                    DNTermJsonObj.put("isDefault", DNTermsMapObj.isIsGSTApplied());
                                    DNTermJsonObj.put("term", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTerm());
                                    DNTermJsonObj.put("formula", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormula());
                                    DNTermJsonObj.put("formulaids", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormula());
                                    DNTermJsonObj.put("termpercentage", DNTermsMapObj.getPercentage());
                                    DNTermJsonObj.put("originalTermPercentage", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPercentage()); // For Service Tax Abatemnt calculation
                                    DNTermJsonObj.put("termamount", DNTermsMapObj.getTermamount());
                                    DNTermJsonObj.put("glaccountname", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getAccountName());
                                    DNTermJsonObj.put("glaccount", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                                    DNTermJsonObj.put("IsOtherTermTaxable", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().isOtherTermTaxable());
                                    DNTermJsonObj.put("sign", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getSign());
                                    DNTermJsonObj.put("purchasevalueorsalevalue", DNTermsMapObj.getPurchaseValueOrSaleValue());
                                    DNTermJsonObj.put("deductionorabatementpercent", DNTermsMapObj.getDeductionOrAbatementPercent());
                                    DNTermJsonObj.put("assessablevalue", DNTermsMapObj.getAssessablevalue());
                                    DNTermJsonObj.put("taxtype", DNTermsMapObj.getTaxType());
                                    DNTermJsonObj.put("taxvalue", DNTermsMapObj.getPercentage());
                                    DNTermJsonObj.put("termtype", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTermType());
                                    DNTermJsonObj.put("termsequence", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTermSequence());
                                    DNTermJsonObj.put("formType", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormType());
                                    DNTermJsonObj.put("accountid", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                                    if (DNTermsMapObj.getEntitybasedLineLevelTermRate() != null && DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms() != null && DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount() != null) {     //SDP-12993
                                        DNTermJsonObj.put("payableaccountid", (DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount().getID()));
                                    } else {
                                        DNTermJsonObj.put("payableaccountid", "");
                                    }
                                    DNTermJsonArry.put(DNTermJsonObj);
                                    termAccount += DNTermsMapObj.getTermamount();
                                }
                                obj.put("LineTermdetails", DNTermJsonArry.toString());
                                obj.put("recTermAmount", termAccount);
                                //obj.put("amountwithtax", noteTaxEntry.getAmount() + termAccount);
                            }
                        }
                        // ## Get Custom Field Data 
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                        ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                        if (row.getJedid() != null) {
                            Detailfilter_names.add(Constants.Acc_jedetailId);
                            Detailfilter_params.add(row.getJedid().getID());
                            invDetailRequestParams.put("filter_names", Detailfilter_names);
                            invDetailRequestParams.put("filter_params", Detailfilter_params);
                            KwlReturnObject idcustresult = accDebitNoteobj.geDebitNoteCustomData(invDetailRequestParams);
                            if (idcustresult.getEntityList().size() > 0) {
                                AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                                if (jeDetailCustom != null) {
                                    JSONObject params = new JSONObject();
                                    params.put(Constants.isExport, isExport);
                                    params.put("isForReport", isForReport);
                                    fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                                }
                            }
                        }
                        invoiceId = invObj.getID();
                        
                        JArr.put(obj);
                    }
                        i++;
                    }
                }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesRowsJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }
    
    public void getDebitNoteForOverchargeRows(HashMap<String, Object> requestParams, DebitNote dn, JSONArray JArr) throws ServiceException, JSONException {
        String companyid = (String) requestParams.get("companyid");
        String currencyid = (String) requestParams.get("gcurrencyid");
        DateFormat df = (DateFormat) requestParams.get("df");
        DateFormat userdf = null;
        if (requestParams.containsKey("userdateformatter") && requestParams.get("userdateformatter") != null) {
            userdf = (DateFormat) requestParams.get("userdateformatter");
        }
        if (requestParams.containsKey(Constants.userdf) && requestParams.get(Constants.userdf) != null) {
            userdf = (DateFormat) requestParams.get(Constants.userdf);
        }
        KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) comp.getEntityList().get(0);
        int countryid = Integer.parseInt(company.getCountry().getID());

        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<>();
        HashMap<String, String> customDateFieldMap = new HashMap<>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(dn.getCompany().getCompanyID(), Constants.Acc_Debit_Note_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        boolean isExport = requestParams.containsKey(Constants.isExport) && requestParams.get(Constants.isExport) != null ? (Boolean) requestParams.get(Constants.isExport) : false;
        boolean isForReport = requestParams.containsKey(Constants.isForReport) && requestParams.get(Constants.isForReport) != null ? (Boolean) requestParams.get(Constants.isForReport) : false;
        boolean isEdit = requestParams.containsKey(Constants.isEdit) && requestParams.get(Constants.isEdit) != null ? (Boolean) requestParams.get(Constants.isEdit) : false;
        
        Set<DebitNoteAgainstCustomerGst> dnDetails = dn.getRowsGst();
        String description = "";
        String goodsReceiptId = "";
        for (DebitNoteAgainstCustomerGst row : dnDetails) {
            GoodsReceiptDetail grdetail = row.getGrdetail();
            GoodsReceipt goodsReceipt = grdetail.getGoodsReceipt();
            if (goodsReceiptId.equals(goodsReceipt.getID()) && !isEdit) {//This is temporary changes. Need to show product in Debit Note for Overcharge/Undercharge.
                continue;
            }
            JSONObject obj = new JSONObject();
            obj.put("billid", dn.getID());
            obj.put("billno", dn.getDebitNoteNumber());
            obj.put("externalcurrencyrate", dn.getExternalCurrencyRate());
            obj.put("srno", row.getSrno());
            obj.put("rowid", row.getID());
            obj.put("currencysymbol", (dn.getCurrency() == null ? currency.getSymbol() : dn.getCurrency().getSymbol()));
            obj.put("productid", row.getProduct().getID());
            obj.put("productname", row.getProduct().getName());
            obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());

            if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                description = row.getDescription();
            } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                description = row.getProduct().getDescription();
            } else {
                description = "";
            }
            obj.put("desc", StringUtil.DecodeText(description));
            obj.put("type", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getName());
            obj.put("transectionid", goodsReceipt.getID());
            obj.put("transectionno", goodsReceipt.getGoodsReceiptNumber());
            obj.put("invcreationdate", df.format(goodsReceipt.getCreationDate()));
            obj.put("invamountdue", goodsReceipt.getInvoiceAmount());
            obj.put("invamount", goodsReceipt.getInvoiceAmount());
            obj.put("grlinkdate", dn.getCreationDate() != null ? df.format(dn.getCreationDate()) : "");
            obj.put("invduedate", df.format(goodsReceipt.getDueDate()));
            obj.put("pid", row.getProduct().getProductid());
            obj.put("memo", row.getRemark());
            obj.put("reason", (row.getReason() != null && !isExport) ? row.getReason().getID() : "");
            obj.put("prtaxid", (row.getTax() != null) ? row.getTax().getID() : "");
            obj.put("taxamount", row.getRowTaxAmount());
            obj.put("taxamountforlinking", row.getRowTaxAmount());
            obj.put("discountispercent", row.getDiscountispercent());
            obj.put("prdiscount", row.getDiscount());
            obj.put("quantity", row.getActualQuantity());
            obj.put("dquantity", row.getReturnQuantity());

            obj.put("linkto", goodsReceipt.getGoodsReceiptNumber());

            obj.put("linkid", goodsReceipt.getID());
            obj.put("rowid", grdetail.getID());
            obj.put("linktype", 1);

            obj.put("includeprotax", (row.getTax() != null));
            obj.put("cntype", dn.getDntype());
            double baseuomrate = row.getBaseuomrate();
            if (row.getUom() != null) {
                obj.put("uomid", row.getUom().getID());
            } else {
                obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
            }
            obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(row.getReturnQuantity(), baseuomrate, companyid));
            obj.put("baseuomrate", baseuomrate);
            obj.put("copyquantity", row.getReturnQuantity());
            try {
                obj.put("description", StringUtil.DecodeText(description));
            } catch (Exception ex) {
                Logger.getLogger(accDebitNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            obj.put("remark", row.getRemark());
            obj.put("rate", row.getRate());
            
            //For Detail Export
            if (userdf != null) {
                obj.put("invcreationdateinuserdateformat", userdf.format(goodsReceipt.getCreationDate()));
                obj.put("invduedateinuserdateformat", userdf.format(goodsReceipt.getDueDate()));
            }
            obj.put("currencycodeforinvoice", goodsReceipt.getCurrency().getCurrencyCode());
            /**
             * get Debit Note Line Level Term Details in Overcharge/Undercharge case for US country
             */
            if (countryid == Constants.USA_country_id) { // Fetch  term details of Product
                JSONObject json = new JSONObject();
                json.put("debitnotedetail", row.getID());
                KwlReturnObject result6 = accDebitNoteobj.getDebitNoteDetailTermMap(json);
                if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                    ArrayList<DebitNoteDetailTermMap> productTermDetail = (ArrayList<DebitNoteDetailTermMap>) result6.getEntityList();
                    JSONArray DNTermJsonArry = new JSONArray();
                    double termAccount = 0.0;
                    for (DebitNoteDetailTermMap DNTermsMapObj : productTermDetail) {
                        JSONObject DNTermJsonObj = new JSONObject();
                        DNTermJsonObj.put("id", DNTermsMapObj.getId());
                        DNTermJsonObj.put("termid", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getId());
                        DNTermJsonObj.put("productentitytermid", DNTermsMapObj.getEntitybasedLineLevelTermRate() != null ? DNTermsMapObj.getEntitybasedLineLevelTermRate().getId() : "");
                        DNTermJsonObj.put("isDefault", DNTermsMapObj.isIsGSTApplied());
                        DNTermJsonObj.put("term", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTerm());
                        DNTermJsonObj.put("formula", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormula());
                        DNTermJsonObj.put("formulaids", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormula());
                        DNTermJsonObj.put("termpercentage", DNTermsMapObj.getPercentage());
                        DNTermJsonObj.put("originalTermPercentage", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPercentage()); // For Service Tax Abatemnt calculation
                        DNTermJsonObj.put("termamount", DNTermsMapObj.getTermamount());
                        DNTermJsonObj.put("glaccountname", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getAccountName());
                        DNTermJsonObj.put("glaccount", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                        DNTermJsonObj.put("IsOtherTermTaxable", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().isOtherTermTaxable());
                        DNTermJsonObj.put("sign", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getSign());
                        DNTermJsonObj.put("purchasevalueorsalevalue", DNTermsMapObj.getPurchaseValueOrSaleValue());
                        DNTermJsonObj.put("deductionorabatementpercent", DNTermsMapObj.getDeductionOrAbatementPercent());
                        DNTermJsonObj.put("assessablevalue", DNTermsMapObj.getAssessablevalue());
                        DNTermJsonObj.put("taxtype", DNTermsMapObj.getTaxType());
                        DNTermJsonObj.put("taxvalue", DNTermsMapObj.getPercentage());
                        DNTermJsonObj.put("termtype", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTermType());
                        DNTermJsonObj.put("termsequence", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTermSequence());
                        DNTermJsonObj.put("formType", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormType());
                        DNTermJsonObj.put("accountid", DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                        if (DNTermsMapObj.getEntitybasedLineLevelTermRate() != null && DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms() != null && DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount() != null) {     //SDP-12993
                            DNTermJsonObj.put("payableaccountid", (DNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount().getID()));
                        } else {
                            DNTermJsonObj.put("payableaccountid", "");
                        }
                        DNTermJsonArry.put(DNTermJsonObj);
                        termAccount += DNTermsMapObj.getTermamount();
                    }
                    obj.put("LineTermdetails", DNTermJsonArry.toString());
                    obj.put("recTermAmount", termAccount);
                    //obj.put("amountwithtax", noteTaxEntry.getAmount() + termAccount);
                }
            }
            //Get Custom Field Data 
            Map<String, Object> variableMap = new HashMap<String, Object>();
            HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
            if (row.getJedid() != null) {
                Detailfilter_names.add(Constants.Acc_jedetailId);
                Detailfilter_params.add(row.getJedid().getID());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                KwlReturnObject idcustresult = accDebitNoteobj.geDebitNoteCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.isForReport, isForReport);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                }
            }
            
            goodsReceiptId= goodsReceipt.getID();
            JArr.put(obj);
        }
    }
    
    public void getAccountDetailsForDebitNote(DebitNote dn, JSONArray jArr, Boolean isExport, Boolean isForReport, String companyid, DateFormat userDateFormat) throws ServiceException {
        try {
            Set<JournalEntryDetail> jedDetails = dn.getJournalEntry()!=null ? dn.getJournalEntry().getDetails() : new HashSet(0);
//          DateFormat userDateFormat=(DateFormat)new JSONObject(jArr.get(jArr.length()-1)).get(Constants.userdf);
            
            if (jedDetails.size() > 0) {
                HashMap<String, Object> fieldrequestParams = new HashMap();
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(dn.getCompany().getCompanyID(), Constants.Acc_Debit_Note_ModuleId));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
		Integer countryId = dn.getCompany().getCountry() != null ? Integer.parseInt(dn.getCompany().getCountry().getID()) : null;
                Iterator<JournalEntryDetail> jeDetailIte = jedDetails.iterator();
                while (jeDetailIte.hasNext()) {
                    JournalEntryDetail jedetail = jeDetailIte.next();
                    if (jedetail.getAccount()!=null && jedetail.getAccount().getMastertypevalue() != 4 && !jedetail.isDebit()) {//4 for not including tax as a account.
                        JSONObject obj = new JSONObject();
                        if(jedetail.getID().equalsIgnoreCase(dn.getVendorEntry().getID())){
                            continue;
                        }
                        obj.put("billid", dn.getID());
                        obj.put("accountname", jedetail.getAccount().getName());
                        obj.put("description", jedetail.getDescription()!=null?StringUtil.DecodeText(jedetail.getDescription()):"-");
                        double amount = authHandler.round(jedetail.getAmount(), companyid);
                        amount=authHandler.round(amount, companyid);
                        if (dn.isIncludingGST()) {
                            obj.put("totalamount", dn.getDnamount());
                        } else {
                        obj.put("totalamount", amount);
                        }
                        obj.put("isIncludingGst", dn.isIncludingGST());
                        obj.put("totalamountforaccount", amount);
                        obj.put("currencysymbol",dn.getCurrency()!=null ? dn.getCurrency().getSymbol() : "");
                        obj.put("isaccountdetails", true);
                        obj.put("taxpercent", 0);
                        obj.put("taxamount", 0);
                        obj.put("taxamountforaccount", 0);
                        obj.put("cntype", dn.getDntype());
                        String jeDetailId = jedetail.getID();
                        String jeDetailaccid = jedetail.getAccount().getID();
                        Set<DebitNoteTaxEntry> Taxset = dn.getDnTaxEntryDetails()!=null ? dn.getDnTaxEntryDetails() : new HashSet(0);
                        Iterator<DebitNoteTaxEntry> taxIte = Taxset.iterator();
                        obj.put("debit", "Credit");
//                        boolean iscustomeraccount=true;
                        while (taxIte.hasNext()) {
                            DebitNoteTaxEntry txEntry = taxIte.next();
                            double taxpercent = 0.0d;
                            String taxJeDetailId = txEntry.getTotalJED()!=null ? txEntry.getTotalJED().getID() : "";
                            String taxJeDetailaccid = txEntry.getAccount() != null ? txEntry.getAccount().getID() : "";
                            obj.put("reason", txEntry.getReason() == null ? "" : txEntry.getReason().getValue());
                            if (StringUtil.equal(jeDetailId, taxJeDetailId) && StringUtil.equal(jeDetailaccid, taxJeDetailaccid) && txEntry.isIsForDetailsAccount()) {
                                if (txEntry.getTax() != null) {
//                                    KwlReturnObject perresult = accTaxObj.getTaxPercent(dn.getCompany().getCompanyID(), dn.getJournalEntry().getEntryDate(), txEntry.getTax().getID());
                                    KwlReturnObject perresult = accTaxObj.getTaxPercent(dn.getCompany().getCompanyID(), dn.getCreationDate(), txEntry.getTax().getID());
                                    taxpercent = (Double) perresult.getEntityList().get(0);
                                }
                                obj.put("taxpercent", taxpercent);
                                double txAmount = authHandler.round(txEntry.getTaxamount(), companyid);
                                if(countryId != null && countryId == Constants.indian_country_id){  //SDP-12993 : For India, Term Amount will be Tax amount
                                    obj.put("taxamount", authHandler.round(txEntry.getTermAmount(), companyid));
                                    obj.put("taxamountforaccount", authHandler.round(txEntry.getTermAmount(), companyid));
                                } else {
                                    obj.put("taxamount", txAmount);
                                    obj.put("taxamountforaccount", txAmount);
                                }
                                obj.put("debit", txEntry.isDebitForMultiCNDN()? "Debit":"Credit");
                                obj.put("srNoForRow", txEntry.getSrNoForRow());
                                if (dn.isIncludingGST()) {
                                    obj.put("totalamount", txEntry.getAmount());
                                } else {
                                obj.put("totalamount", amount);
                            }
                        }
                        }
//                        if (iscustomeraccount) {
//                            continue;
//                        }
                        // ## Get Custom Field Data 
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                        ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                        Detailfilter_names.add(Constants.Acc_jedetailId);
                        Detailfilter_params.add(jedetail.getID());
                        invDetailRequestParams.put("filter_names", Detailfilter_names);
                        invDetailRequestParams.put("filter_params", Detailfilter_params);
                        JSONArray dimensionArr = new JSONArray();
                        KwlReturnObject idcustresult = accDebitNoteobj.geDebitNoteCustomData(invDetailRequestParams);
                        if (idcustresult.getEntityList().size() > 0) {
                            AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                            AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                            if (jeDetailCustom != null) {
                                JSONObject params = new JSONObject();
                                params.put(Constants.userdf,userDateFormat);
                                params.put("isExport", isExport);
                                params.put("isForReport", isForReport);
                                fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                            }
                        }
                        jArr.put(obj);
                    }
                }
            }else if(dn.isIsOpeningBalenceDN()){
                JSONObject obj = new JSONObject();
                obj.put("billid", dn.getID());
                obj.put("accountname", dn.getAccount() != null ? dn.getAccount().getName() :"");
                obj.put("description", dn.getMemo()!=null?StringUtil.DecodeText(dn.getMemo()):"-");
                double amount = authHandler.round(dn.getDnamount(), companyid);
                amount=authHandler.round(amount, companyid);
                obj.put("totalamount", amount);
                obj.put("totalamountforaccount", amount);
                obj.put("currencysymbol",dn.getCurrency()!=null ? dn.getCurrency().getSymbol() : "");
                obj.put("isaccountdetails", true);
                obj.put("taxpercent", 0);
                obj.put("taxamount", 0);
                obj.put("taxamountforaccount", 0);
                obj.put("debit", "Credit");
                obj.put("isOpeningDnCn", true);
                 jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAccountDetailsForDebitNote : " + ex.getMessage(), ex);
        }
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
    public void updateLinkingInformationOfDebitNote(String debitNoteDetailID) throws ServiceException, SessionExpiredException, JSONException, AccountingException {

        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DebitNoteDetail.class.getName(), debitNoteDetailID);
        DebitNoteDetail debitNoteDetail = (DebitNoteDetail) rdresult.getEntityList().get(0);

        String debitNoteNo = debitNoteDetail.getDebitNote().getDebitNoteNumber();
        String debitNoteId = debitNoteDetail.getDebitNote().getID();
        String invoiceNo = debitNoteDetail.getGoodsReceipt().getGoodsReceiptNumber();
        String invoiceId = debitNoteDetail.getGoodsReceipt().getID();

        
          /* Checking Entry of Debit Note 
         * 
         * in linking Information table whether it is present or not*/
        
        KwlReturnObject result = accDebitNoteobj.checkEntryForDebitNoteInLinkingTable(debitNoteId, invoiceId);
        List list = result.getEntityList();
        if (list == null || list.isEmpty()) {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("sourceflag", 0);
            requestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);

            requestParams.put("linkeddocno", debitNoteNo);
            requestParams.put("docid", invoiceId);
            requestParams.put("linkeddocid", debitNoteId);
            result = accGoodsReceiptobj.saveVILinking(requestParams);


            requestParams.put("sourceflag", 1);
            requestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
            requestParams.put("linkeddocno", invoiceNo);
            requestParams.put("docid", debitNoteId);
            requestParams.put("linkeddocid", invoiceId);
            result = accDebitNoteobj.saveDebitNoteLinking(requestParams);
        }
    }
        @Override
        public boolean rejectPendingDebitNote(Map<String, Object> requestParams,JSONArray jArr) throws  ServiceException {
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
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String invid = StringUtil.DecodeText(jobj.optString("billid"));
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), invid);
                    DebitNote debitNote = (DebitNote) cap.getEntityList().get(0);
                    HashMap<String, Object> invApproveMap = new HashMap<String, Object>();
                    level = debitNote.getApprovestatuslevel();
                    invApproveMap.put("companyid", companyid);
                    invApproveMap.put("level", level);
                    invApproveMap.put("totalAmount", String.valueOf(amount));
                    invApproveMap.put("currentUser", currentUser);
                    invApproveMap.put("fromCreate", false);
                    invApproveMap.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(invApproveMap);
                    }
                    if (hasAuthorityToReject) {
                        accDebitNoteobj.rejectPendingDebitNote(debitNote.getID(), companyid);
                        isRejected = true;
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("transtype", AuditAction.DEBIT_NOTE_REJECTED);
                        hashMap.put("transid", debitNote.getID());
                        hashMap.put("approvallevel", Math.abs(debitNote.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", "");
                        hashMap.put("userid", currentUser);
                        hashMap.put("companyid", companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        auditTrailObj.insertAuditLog(AuditAction.DEBIT_NOTE_REJECTED, "User " + userFullName + " " + actionMsg + " Debit Note " + debitNote.getDebitNoteNumber(), requestParams, debitNote.getID());
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
     * Please Note accounting exception should only be thrown in case of if JE posting date dose not belongs to lockin period. As further code is written only to handle AccountingException in case of lockin period.
     */
    @Override
    public List approvePendingDebitNote(Map<String, Object> requestParams) throws ServiceException,AccountingException {
        List returnList = new ArrayList();
        try {
           
            String jeID = "";
            String msg="";
            String companyid = (String) requestParams.get(Constants.companyKey);
            String userid = (String) requestParams.get("userid");
            String billid = (String) requestParams.get("billid");
            String remark = (String) requestParams.get("remark");
            String userFullName = (String) requestParams.get("userName");
            String baseUrl = (String) requestParams.get("baseUrl");
            double amount = (!StringUtil.isNullObject(requestParams.get("amount")) && !StringUtil.isNullOrEmpty(requestParams.get("amount").toString())) ? authHandler.round(Double.parseDouble(requestParams.get("amount").toString()), companyid):0 ;
            boolean isEditToApprove = (!StringUtil.isNullObject(requestParams.get("isEditToApprove")) && !StringUtil.isNullOrEmpty(requestParams.get("isEditToApprove").toString())) ? Boolean.parseBoolean(requestParams.get("isEditToApprove").toString()):false;
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), billid);
            DebitNote debitNote = (DebitNote) CQObj.getEntityList().get(0);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) returnObject.getEntityList().get(0);
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
            
            HashMap<String, Object> invApproveMap = new HashMap<String, Object>();
            int level = debitNote.getApprovestatuslevel();

            invApproveMap.put("companyid", companyid);
            invApproveMap.put("level", level);
            invApproveMap.put("totalAmount", String.valueOf(amount));
            invApproveMap.put("currentUser", userid);
            invApproveMap.put("fromCreate", false);
            invApproveMap.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
            invApproveMap.put(Constants.PAGE_URL, baseUrl);
            int approvedLevel = 0;
            String JENumber = "";
            String JEMsg = "";

            List approvedLevelList = approveDebitNote(debitNote, invApproveMap, true);
            approvedLevel = (Integer) approvedLevelList.get(0);
            jeID = debitNote.getJournalEntry().getID();

            if (approvedLevel == 11) {//when final 
                if (StringUtil.isNullOrEmpty(debitNote.getJournalEntry().getEntryNumber())) {
                    int isApproved = 0;
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);
                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
//                    String JENumBer = journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(requestParams, debitNote.getJournalEntry(), companyid, format.getID(), isApproved);
                    String JENumBer="";
                     KwlReturnObject returnObj = journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(requestParams, debitNote.getJournalEntry(), companyid, format.getID(), isApproved);
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
                JENumber = " with JE No. " + debitNote.getJournalEntry().getEntryNumber();
                JEMsg = "<br/>" + "JE No : <b>" + debitNote.getJournalEntry().getEntryNumber() + "</b>";
            }
            
            if (!isEditToApprove && approvedLevel == 11 && debitNote.getDntype() == 1) {// If this condition true then we need to update invoice amount used in CN
                for (DebitNoteDetail detail : debitNote.getRows()) {
                    GoodsReceipt grObj = detail.getGoodsReceipt();
                    double returnAmountInInvoiceCurrecny = 0;
                    double returnAmountInBaseAmountDue = 0;
                    if (detail.getDiscount() != null) {
                        returnAmountInInvoiceCurrecny = detail.getDiscount().getAmountinInvCurrency();
                        double returnAmountInCNCurrency = detail.getDiscount().getDiscount();
//                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, returnAmountInCNCurrency, debitNote.getCurrency().getCurrencyID(), debitNote.getJournalEntry().getEntryDate(), debitNote.getJournalEntry().getExternalCurrencyRate());
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, returnAmountInCNCurrency, debitNote.getCurrency().getCurrencyID(), debitNote.getCreationDate(), debitNote.getJournalEntry().getExternalCurrencyRate());
                        returnAmountInBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                    }
                    KwlReturnObject invoiceResult = updateInvoiceAmountDueAndReturnResult(grObj, company, returnAmountInInvoiceCurrecny, returnAmountInBaseAmountDue);
                    if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                        GoodsReceipt gr = (GoodsReceipt) invoiceResult.getEntityList().get(0);
                        if ((gr.isIsOpeningBalenceInvoice() && gr.getOpeningBalanceAmountDue() == 0) || (gr.getInvoiceamountdue() == 0)) {
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            dataMap.put("amountduedate", detail.getGrLinkDate());
                            accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr,dataMap);
                        }
                    }
                }
            }
            
            if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String creatormail = company.getCreator().getEmailID();
                String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
                String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
                String creatorname = fname + " " + lname;
                String documentcreatoremail = (debitNote != null && debitNote.getCreatedby() != null) ? debitNote.getCreatedby().getEmailID() : "";
                String approvalpendingStatusmsg = "";
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                ArrayList<String> emailArray = new ArrayList<>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", level);
                qdDataMap.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
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
                if (debitNote.getApprovestatuslevel() < 11) {
                    qdDataMap.put("totalAmount", String.valueOf(amount));
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put("Number", debitNote.getDebitNoteNumber());
                mailParameters.put("userName", userFullName);
                mailParameters.put("emails", emails);
                mailParameters.put("sendorInfo", sendorInfo);
                mailParameters.put("moduleName",  Constants.DEBIT_NOTE);
                mailParameters.put("addresseeName", "All");
                mailParameters.put("companyid", company.getCompanyID());
                mailParameters.put("baseUrl", baseUrl);
                mailParameters.put("approvalstatuslevel", debitNote.getApprovestatuslevel());
                mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
                if (emails.length > 0) {
                    accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
                }
            }
            // Save Approval History
            if (approvedLevel != Constants.NoAuthorityToApprove) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("transtype", Constants.DEBIT_NOTE_APPROVAL);
                hashMap.put("transid", debitNote.getID());
                hashMap.put("approvallevel", debitNote.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
                hashMap.put("remark", remark);
                hashMap.put("userid", userid);
                hashMap.put("companyid", companyid);
                accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                // Audit log entry
                String action = "Debit Note ";
                String auditaction = AuditAction.DEBIT_NOTE_APPROVED;
                auditTrailObj.insertAuditLog(auditaction, "User " + userFullName + " has Approved a " + action + debitNote.getDebitNoteNumber() + JENumber + " at Level-" + debitNote.getApprovestatuslevel(), requestParams, debitNote.getID());
                msg = "Debit Note has been approved successfully " + " by " + userFullName + " at Level " + debitNote.getApprovestatuslevel() + "."+JEMsg;
            } ;
            returnList.add(msg);
        }catch (AccountingException ae) {
            throw new AccountingException(ae.getMessage(), ae);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteServiceImpl.approvePendingDebitNote:"+ex.getMessage(), ex);
        }
        return returnList;
    }

    @Override
    public List approveDebitNote(DebitNote debitNote, HashMap<String, Object> cnApproveMap, boolean isMailApplicable) throws ServiceException {
        List returnList = new ArrayList();
        try {
            boolean hasAuthority = false;
            String companyid = "";

            List mailParamList = new ArrayList();
            int returnStatus;

            if (cnApproveMap.containsKey("companyid") && cnApproveMap.get("companyid") != null) {
                companyid = cnApproveMap.get("companyid").toString();
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
                String dnNumber = debitNote.getDebitNoteNumber();
                String dnID = debitNote.getID();
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("level", level + 1);
                qdDataMap.put("moduleid", moduleid);
                KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                Iterator itr = flowresult.getEntityList().iterator();
                String fromName = "User";
                fromName = debitNote.getCreatedby().getFirstName().concat(" ").concat(debitNote.getCreatedby().getLastName());
                /**
                 * parameters required for sending mail
                 */
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put(Constants.companyid, companyid);
                mailParameters.put(Constants.prNumber, dnNumber);
                mailParameters.put(Constants.fromName, fromName);
                mailParameters.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                mailParameters.put(Constants.createdBy, debitNote.getCreatedby().getUserID());
                if (cnApproveMap.containsKey(Constants.PAGE_URL)) {
                    mailParameters.put(Constants.PAGE_URL, (String) cnApproveMap.get(Constants.PAGE_URL));
                }
                while (itr.hasNext()) {
                    Object[] row = (Object[]) itr.next();
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
                        if (isMailApplicable) {
                            mailParameters.put("level", level+1);
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
                accDebitNoteobj.approvePendingDebitNote(dnID, companyid, approvalStatus);
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

    /**
     * @param mailParameters(String companyid, String ruleId, String documentNumber, String fromName, boolean hasApprover, int moduleid,String createdby, String PAGE_URL)
     * @throws ServiceException
     * @throws MessagingException 
     */
    public void sendMailToApprover(Map<String, Object> mailParameters) throws ServiceException, MessagingException {
        KwlReturnObject cap = null;
        int level =0;
        if (mailParameters.containsKey(Constants.companyid)) {
            cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String) mailParameters.get(Constants.companyid));
        }
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
        boolean hasApprover = false;
        int moduleid = 0;
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
         if(mailParameters.containsKey("level")){
            level = (int) mailParameters.get("level");
        }
        String transactionName = "";
        String transactionNo = "";
        switch (moduleid) {
            case Constants.Acc_Debit_Note_ModuleId:
                transactionName = "Debit Note";
                transactionNo = "Debit Note Number";
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
                + "<p></p>"
                + "<p>Company Name:- %s</p>"
                + "<p>Please check on Url:- %s</p>"
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
            Logger.getLogger(accDebitNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public JSONObject checkInvoiceKnockedOffDuringDebitNotePending(Map<String, Object> requestParams) {
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
            String billid = (String)requestParams.get("billid");//This is mandatory parameter
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), billid);
            DebitNote creditNote = (DebitNote) CQObj.getEntityList().get(0);
            for(DebitNoteDetail detail:creditNote.getRows()){
                double invKnockedOffAmount = 0;
                if(detail.getDiscount()!=null){
                    invKnockedOffAmount = detail.getDiscount().getDiscountValue();
                }
                double invoiceAmtDue = detail.getGoodsReceipt()!=null? detail.getGoodsReceipt().getInvoiceamountdue():0;
                if (invoiceAmtDue == 0) {
                    totalNumberOfRecordHavingAmountDuezero++;
                }
                if (invoiceAmtDue >= invKnockedOffAmount) {
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
             } else if(isInvoiceUtilizedDuringCNPending || (totalInvoicesLinked==(totalNumberOfRecordHavingAmountDuezero+totalNumberOfRecordHavingEqualtoAmountDueorGreater) && totalNumberOfRecordHavingAmountDuezero!=0)){//Second Or case: when multiple invoices linked but few of them become zero while pending DN 
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
                 Logger.getLogger(accDebitNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return obj;
    }

    @Override
    public List approvePendingDebitNoteAgainstInvoiceAsDNOtherwise(HashMap<String, Object> requestParams) throws ServiceException{
        /*
         * Here we have to convert CN agaisnt invoice to CNOtherwise and the
         * approving CN 
         * Step 1: Deleting invoie and CN linking infomation 
         * step 2: Deleteing Forex and Revaluation JE 
         * step 3: Deleteing All CN Details 
         * step 4: Update cnamountdue, cntype, openflag 
         * step 5: Approve CN
         */
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String billid = (String) requestParams.get("billid");
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), billid);
            DebitNote debitNote = (DebitNote) CQObj.getEntityList().get(0);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) returnObject.getEntityList().get(0);

            HashMap<String, Object> debithm = new HashMap<String, Object>();
            debithm.put("dnid", billid);
             /* Deleting linking information of Debit while editing*/
            accDebitNoteobj.deleteLinkingInformationOfDN(debithm);

            //Delete unrealised JE for Credit Note
            accJournalEntryobj.permanentDeleteJournalEntryDetailReval(billid, companyid);
            accJournalEntryobj.permanentDeleteJournalEntryReval(billid, companyid);

            // Delete foreign gain loss JE
            List resultJe = accDebitNoteobj.getForeignGainLossJE(billid, companyid);
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
            DebitNoteDetail noteDetail = new DebitNoteDetail();
            for (DebitNoteDetail dndetails : debitNote.getRows()) {
                if (dndetails.getSrno() == 1) {//CreditNoteDetail at serial number one has totaljedid which get used 
                    noteDetail.setSrno(1);
                    noteDetail.setTotalJED(dndetails.getTotalJED());
                    noteDetail.setGstJED(dndetails.getGstJED());
                    noteDetail.setMemo(dndetails.getMemo());
                    break;
                }
            }
            String CreditNoteDetailID = StringUtil.generateUUID();
            noteDetail.setID(CreditNoteDetailID);
            noteDetail.setDebitNote(debitNote);
            noteDetail.setTotalDiscount(0.00);
            noteDetail.setCompany(company);

            //Deleting All CreditNoteDetails
            KwlReturnObject result = accDebitNoteobj.deleteDebitNoteDetails(debitNote.getID(), companyid);
            //Update CN
            Set<DebitNoteDetail> newcndetails = new HashSet<DebitNoteDetail>();
            newcndetails.add(noteDetail);
            debithm.put("dndetails", newcndetails);
            debithm.put("dnamountdue", debitNote.getDnamount());//updating amount due with amount
            debithm.put("openflag", true);
            debithm.put("cntype", 2);
            debithm.put("otherwise", true);
            KwlReturnObject result1 = accDebitNoteobj.updateDebitNote(debithm);
            
            //Finally Approving CN
            list=approvePendingDebitNote(requestParams);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteServiceImpl.approvePendingCreditNote:"+ex.getMessage(), ex);
        }
        return list;
    }
    
    @Override
        public boolean isNoteLinkedWithPayment(String noteId) {
        boolean isNoteLinkedWithPayment = false;
        try {
            KwlReturnObject result = accDebitNoteobj.getReceivePaymentIdLinkedWithDebitNote(noteId);
            List list = result.getEntityList();
            Iterator it = list.iterator();
            if (it.hasNext()) {
                isNoteLinkedWithPayment = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isNoteLinkedWithPayment;
    }
    /**
     *  Method to check is debit note linked in Credit note
     * @param noteId
     * @param companyId
     * @return boolean 
     */
    @Override
    public boolean isDebitNoteLinkedWithCreditNote(String noteId,String companyId) {
        boolean isDebitNoteLinkedWithCreditNote = false;
        try {
            KwlReturnObject result = accDebitNoteobj.getDebitNotelinkedInCreditNote(noteId, companyId);
            List list = result.getEntityList();
             BigInteger count = null;
            if (!list.isEmpty()) {
                count = (BigInteger) list.get(0);
                if (count.intValue() > 0) {
                    isDebitNoteLinkedWithCreditNote = true;
                }
            }
           
        } catch (ServiceException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isDebitNoteLinkedWithCreditNote;
    }
   
    @Override
    public boolean isNoteLinkedWithAdvancePayment(String noteId) {
        boolean isNoteLinkedWithPayment = false;
        try {
            KwlReturnObject result = accDebitNoteobj.getAdvanceReceivePaymentIdLinkedWithDebitNote(noteId);
            List list = result.getEntityList();
            Iterator it = list.iterator();
            if (it.hasNext()) {
                isNoteLinkedWithPayment = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isNoteLinkedWithPayment;
    }

    @Override
    public boolean isNoteLinkedWithInvoice(String noteId, String comapnyId) {
        boolean isNoteLinkedWithInvoice = false;
        try {
            KwlReturnObject result = accDebitNoteobj.getVendorInvoicesLinkedWithDebitNote(noteId, comapnyId);
            List list = result.getEntityList();
            Iterator it = list.iterator();
            if (it.hasNext()) {
                isNoteLinkedWithInvoice = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return isNoteLinkedWithInvoice;
    }

   /* 
    Funtion Used to delete Forex-Gain Loss JE
    */
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

    /**
     * @param : paramJobj 
     * @Desc : Method to post rounding JE when DN Linked with Invoice
     * @throws : ServiceException
     * @Return : void
     */
    @Override
    public void postRoundingJEAfterLinkingInvoiceInDebitNote(JSONObject paramJobj) throws ServiceException {
        try {
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
            String companyid = paramJobj.optString(Constants.companyKey);
            boolean isEdit = paramJobj.optBoolean("isEdit", false);
            String dnid = paramJobj.optString("cnid", "");
            KwlReturnObject dnresult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
            DebitNote dn = (DebitNote) dnresult.getEntityList().get(0);
            String dnNumber = (dn != null) ? dn.getDebitNoteNumber() : "";

            boolean prWithDN = paramJobj.optBoolean("PRWithDN", false);
            if (prWithDN) {
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
                            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceID);
                            GoodsReceipt gr = (GoodsReceipt) grresult.getEntityList().get(0);
                            String grNumber = gr.getGoodsReceiptNumber();
                            if ((gr.isIsOpeningBalenceInvoice() && gr.getOpeningBalanceAmountDue() == 0) || (gr.isNormalInvoice() && gr.getInvoiceamountdue() == 0)) {
                                //below method return Rounding JE if created otherwise it returns null
                                paramJobj.put("goodsReceiptObj", gr);
                                JournalEntry roundingJE = accGoodsReceiptModuleService.createRoundingOffJE(paramJobj);
                                if (roundingJE != null) {
                                    String jeid = roundingJE.getID();
                                    String jenumber = roundingJE.getEntryNumber();
                                    if (isEdit) {
                                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_UPDATED, "User " + paramJobj.optString(Constants.userfullname) + " has updated Purchase Return with DN " + dnNumber + ". Rounding off JE " + jenumber + " updated.", auditRequestParams, jeid);
                                    } else {
                                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_UPDATED, "User " + paramJobj.optString(Constants.userfullname) + " has created Sales Return with DN " + dnNumber + ". Rounding off JE " + jenumber + " posted.", auditRequestParams, jeid);
                                    }
                                }
                            } else if (isEdit) {//If amount due becomes non zero in edit case then we need to check wheather rounding JE was generated for this GR or not if yes then need to delete
                                KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(gr.getID(), companyid);
                                List<JournalEntry> jeList = jeResult.getEntityList();
                                for (JournalEntry roundingJE : jeList) {
                                    String jeid = roundingJE.getID();
                                    String jenumber = roundingJE.getEntryNumber();
                                    deleteJEArray(roundingJE.getID(), companyid);
                                    auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has edited DN " + dnNumber + ". Rounding JE " + jenumber + " deleted.", auditRequestParams, jeid);
                                }
                            }
                        }
                    }
                }
            } else {
                JSONArray linkJSONArray = new JSONArray();
                if (paramJobj.has("linkdetails") && !StringUtil.isNullOrEmpty(paramJobj.optString("linkdetails"))) {
                    linkJSONArray = new JSONArray(paramJobj.optString("linkdetails"));
                }

                for (int i = 0; i < linkJSONArray.length(); i++) {
                    JSONObject obj = linkJSONArray.getJSONObject(i);
                    int documenttype = Integer.parseInt(obj.optString("documentType"));
                    if (documenttype == Constants.CreditNoteOtherwise && obj.optDouble("linkamount", 0.0) != 0) {
                        String grid = obj.optString("documentid");
                        if (!StringUtil.isNullOrEmpty(grid)) {
                            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grid);
                            GoodsReceipt gr = (GoodsReceipt) grresult.getEntityList().get(0);
                            String grNumber = gr.getGoodsReceiptNumber();
                            if ((gr.isIsOpeningBalenceInvoice() && gr.getOpeningBalanceAmountDue() == 0) || (gr.isNormalInvoice() && gr.getInvoiceamountdue() == 0)) {
                                //below method return Rounding JE if created otherwise it returns null
                                paramJobj.put("goodsReceiptObj", gr);
                                JournalEntry roundingJE = accGoodsReceiptModuleService.createRoundingOffJE(paramJobj);
                                if (roundingJE != null) {
                                    String jeid = roundingJE.getID();
                                    String jenumber = roundingJE.getEntryNumber();
                                    auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has knocked off Vendor Invoice " + grNumber + " against DN " + dnNumber + ". Rounding JE " + jenumber + " posted.", auditRequestParams, jeid);
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
    public void postRoundingJEOnDebitNoteSave(JSONObject paramJobj) throws ServiceException {
        try {
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
            
            boolean isEditDN = paramJobj.optBoolean("isEdit", false);
            String amounts[] = paramJobj.optString("amounts").split(",");
            String companyid = paramJobj.optString(Constants.companyKey);
            String debitnotenumber = paramJobj.optString("debitnotenumber");
            String invoiceDetails = paramJobj.optString("invoicedetails");
            int cntype = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.cntype, null)) ? 1 : Integer.parseInt(paramJobj.optString(Constants.cntype));
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
                 * Sales Invoice id in case of DN Undercharge SI. 
                 */
                if (cntype != Constants.DebitNoteForUndercharge) {
                    if (usedcnamount != 0 && !StringUtil.isNullOrEmpty(invoiceId)) {
                        KwlReturnObject grresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceId);
                        GoodsReceipt gr = (GoodsReceipt) grresult.getEntityList().get(0);
                        String grNumber = gr.getGoodsReceiptNumber();
                        if ((gr.isIsOpeningBalenceInvoice() && gr.getOpeningBalanceAmountDue() == 0) || (gr.isNormalInvoice() && gr.getInvoiceamountdue() == 0)) {
                            //below method return Rounding JE if created otherwise it returns null
                            paramJobj.put("goodsReceiptObj", gr);
                            JournalEntry roundingJE = accGoodsReceiptModuleService.createRoundingOffJE(paramJobj);
                            if (roundingJE != null) {
                                String jeid = roundingJE.getID();
                                String jenumber = roundingJE.getEntryNumber();
                                if (isEditDN) {
                                    auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_UPDATED, "User " + paramJobj.optString(Constants.userfullname) + " has knocked off Vendor Invoice " + grNumber + " against  DN " + debitnotenumber + ". Rounding off JE " + jenumber + " updated.", auditRequestParams, jeid);
                                } else {
                                    auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has knocked off Vendor Invoice " + grNumber + " against  DN " + debitnotenumber + ". Rounding off JE " + jenumber + " posted.", auditRequestParams, jeid);
                                }
                            }
                        } else if (isEditDN) {//If amount due becomes non zero in edit case then we need to check wheather rounding JE was generated for this GR or not if yes then need to delete
                            KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(gr.getID(), companyid);
                            List<JournalEntry> jeList = jeResult.getEntityList();
                            for (JournalEntry roundingJE : jeList) {
                                String jeid = roundingJE.getID();
                                String jenumber = roundingJE.getEntryNumber();
                                deleteJEArray(roundingJE.getID(),companyid);
                                auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has edited DN " + debitnotenumber + ". Rounding JE "+jenumber+" deleted.", auditRequestParams, jeid);
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
    public void postRoundingJEAfterApproveDebitNote(JSONObject paramJobj) throws ServiceException {
        try {
            String dnid = paramJobj.optString("billid", "");
            KwlReturnObject dnresult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
            DebitNote debitnote = (DebitNote) dnresult.getEntityList().get(0);

            //Here used method evictObj to remove current payment object from session.
            //It was giving different value from database

            accountingHandlerDAOobj.evictObj(debitnote);
            dnresult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
            debitnote = (DebitNote) dnresult.getEntityList().get(0);

            if (debitnote != null && debitnote.getApprovestatuslevel() == 11) {//Code will execute for approved payment only
                String dnNumber = debitnote != null ? debitnote.getDebitNoteNumber() : "";
                Map<String, Object> auditRequestParams = new HashMap<>();
                auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));

                for (DebitNoteDetail detail : debitnote.getRows()) {
                    GoodsReceipt gr = detail.getGoodsReceipt();
                    if (gr != null && ((gr.isIsOpeningBalenceInvoice() && gr.getOpeningBalanceAmountDue() == 0) || (gr.isNormalInvoice() && gr.getInvoiceamountdue() == 0))) {
                        paramJobj.put("goodsReceiptObj", gr);
                        JournalEntry roundingJE = accGoodsReceiptModuleService.createRoundingOffJE(paramJobj);
                        if (roundingJE != null) {
                            auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has approved Debit Note " + dnNumber + ". Rounding JE " + roundingJE.getEntryNumber() + " posted.", auditRequestParams, roundingJE.getID());
                        }
                    }
                }
            }
        } catch (JSONException | NumberFormatException | ServiceException | SessionExpiredException | AccountingException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
    @Override
    public void getDebitNoteCustomDataForPayment(HashMap<String, Object> request, JSONObject obj, DebitNote debitMemo, JournalEntry je) throws ServiceException {
        try {
            String companyid = (String) request.get(Constants.companyKey);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            KwlReturnObject custumObjresult = null;
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Debit_Note_ModuleId, 0));
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            if (debitMemo.isNormalDN()) {
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
            throw ServiceException.FAILURE("accDebitNoteServiceImpl.getDebitNoteCustomDataForPayment : " + ex.getMessage(), ex);
        }
    }
    
       public JSONArray getDNKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException{
        JSONArray allTransaction = new JSONArray();
        String companyid = (String) invoiceRequestParams.get(Constants.companyKey);
        List list = accDebitNoteobj.getDNKnockOffTransactions(invoiceRequestParams);
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
            obj.put("type", Constants.DEBIT_NOTE);
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
    public JSONArray getOpeningDNKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException{
        JSONArray allTransaction = new JSONArray();
        String companyid = (String) invoiceRequestParams.get(Constants.companyKey);
        List list = accDebitNoteobj.getOpeningDNKnockOffTransactions(invoiceRequestParams);
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
            obj.put("type", Constants.DEBIT_NOTE);
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
    public JSONArray getAllDNKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException{
        JSONArray allTransaction = null;
        allTransaction = getDNKnockOffJSON(invoiceRequestParams);
        JSONArray arr1 = getOpeningDNKnockOffJSON(invoiceRequestParams);
        if (arr1 != null && arr1.length() > 0) {
            for (int i = 0; i < arr1.length(); i++) {
                allTransaction.put(arr1.getJSONObject(i));
            }
        }
        return allTransaction;
    }
}
