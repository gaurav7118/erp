/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.invoice;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public class AccInvoiceServiceHandler {

    public static JSONArray getAgedOpeningBalanceReceiptJson(HashMap<String, Object> requestParams, List list, com.krawler.utils.json.base.JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, accReceiptDAO accReceiptDAOobj, HttpServletRequest request, AccountingHandlerDAO accountingHandlerDAOobj, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj) {
        try {
            boolean ignoreZero = request.getParameter("ignorezero") != null ? Boolean.parseBoolean(request.getParameter("ignorezero")) : false;
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ //Handle Date Parse exception. ERP-33531
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            String companyid = (String) requestParams.get(InvoiceConstants.companyid);
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Receive_Payment_ModuleId));
            if (accAccountDAOobj != null) {
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }

            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.getAttribute(Constants.asOfDate) != null) {
                    curDate = (Date) request.getAttribute(Constants.asOfDate);
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            if (list != null) {
                for (Object object : list) {

                    double amountdue1 = 0;
                    double amountdue2 = 0;
                    double amountdue3 = 0;
                    double amountdue4 = 0;
                    double amountdue5 = 0;
                    double amountdue6 = 0;
                    double amountdue7 = 0;
                    double amountdue8 = 0;
                    double amountdue9 = 0;
                    double amountdue10 = 0;
                    double amountdue11 = 0;
//                    double accruedbalance = 0;

                    Receipt receipt = (Receipt) object;
                    if (receipt != null) {
                        Logger.getLogger(AccInvoiceServiceHandler.class.getName()).log(Level.INFO, "Opening Receipt=" + receipt.getReceiptNumber());
                        HashMap<String, Object> reqParams1 = new HashMap();
                        reqParams1.put("receiptid", receipt.getID());
                        reqParams1.put("companyid", companyid);
                        reqParams1.put(Constants.df, df);
                        if (requestParams.containsKey("asofdate") && requestParams.get("asofdate") != null) {
                            reqParams1.put("asofdate", requestParams.get("asofdate"));
                        }
                        double amountdue = getOpeningReceiptAmountDue(receipt, reqParams1, accReceiptDAOobj);
                        if (amountdue != 0 || !ignoreZero) {
                            JSONObject invoiceJson = new JSONObject();
                            Date creationDate = receipt.getCreationDate();
                            double exchangeRateForOtherCurrency = receipt.getExchangeRateForOpeningTransaction();
                            boolean isopeningBalanceRecceipt = receipt.isIsOpeningBalenceReceipt();
                            boolean isConversionRateFromCurrencyToBase = receipt.isConversionRateFromCurrencyToBase();
                            
                            invoiceJson.put(InvoiceConstants.billid, receipt.getID());
                            invoiceJson.put(InvoiceConstants.billno, receipt.getReceiptNumber());
                            invoiceJson.put(InvoiceConstants.journalentryid, receipt.getJournalEntry() == null ? "" : receipt.getJournalEntry().getID());
                            invoiceJson.put(InvoiceConstants.withoutinventory, false);
                            invoiceJson.put(InvoiceConstants.currencysymbol, receipt.getCurrency() == null ? "" : receipt.getCurrency().getSymbol());
                            invoiceJson.put(InvoiceConstants.currencyid, (receipt.getCurrency() == null ? "" : receipt.getCurrency().getCurrencyID()));
                            invoiceJson.put(InvoiceConstants.currencyname, (receipt.getCurrency() == null ? "" : receipt.getCurrency().getName()));
                            invoiceJson.put(InvoiceConstants.entryno, "");
                            invoiceJson.put(InvoiceConstants.personid, receipt.getCustomer() == null ? "" : receipt.getCustomer().getID());
                            invoiceJson.put(InvoiceConstants.personname, receipt.getCustomer() == null ? "" : receipt.getCustomer().getName());
                            invoiceJson.put(InvoiceConstants.personinfo, receipt.getCustomer() == null ? "" : receipt.getCustomer().getName()+"("+receipt.getCustomer().getAcccode()+")");
                            invoiceJson.put(InvoiceConstants.aliasname, receipt.getCustomer() == null ? "" : receipt.getCustomer().getAliasname());
                            invoiceJson.put(InvoiceConstants.CustomerCreditTerm, receipt.getCustomer() == null ? "" : receipt.getCustomer().getCreditTerm() == null ? "" : receipt.getCustomer().getCreditTerm().getTermname());
                            invoiceJson.put(InvoiceConstants.duedate, df.format(creationDate));
                            invoiceJson.put(InvoiceConstants.date, df.format(creationDate));
                            invoiceJson.put(InvoiceConstants.memo, receipt.getMemo() == null ? "" : receipt.getMemo());
                            invoiceJson.put(InvoiceConstants.deleted, receipt.isDeleted());
                            invoiceJson.put(InvoiceConstants.externalcurrencyrate, exchangeRateForOtherCurrency);
                            invoiceJson.put(InvoiceConstants.isConversionRateFromCurrencyToBase, receipt.isConversionRateFromCurrencyToBase());
                            invoiceJson.put("isOpeningBalanceTransaction", isopeningBalanceRecceipt);
                            invoiceJson.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + ((isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency ) + " " + (receipt.getCurrency() == null ? "" : receipt.getCurrency().getCurrencyCode()));
                            KwlReturnObject bAmt = null;
                            double openingBalanceAmountDueInBase = 0d;
//                            if (Constants.OpeningBalanceBaseAmountFlag) {
//                                openingBalanceAmountDueInBase = -receipt.getOpeningBalanceBaseAmountDue();
//                            } else {
                            if (isopeningBalanceRecceipt && receipt.isConversionRateFromCurrencyToBase()) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, receipt.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, receipt.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            }
                            openingBalanceAmountDueInBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
//                            }
                            invoiceJson.put(InvoiceConstants.amountdue, amountdue);
                            invoiceJson.put(InvoiceConstants.amountdueinbase, openingBalanceAmountDueInBase);
                            invoiceJson.put("amountinbase", receipt.getOriginalOpeningBalanceBaseAmount());
                            invoiceJson.put("code", receipt.getCustomer() != null ? receipt.getCustomer().getAcccode() : "");
                            invoiceJson.put("type", "Payment Received");
                            double customerCreditLimit = 0;
                            double customerCreditLimitInbase = 0;
                            String currencyId = "";
                            if (receipt.getCustomer() != null && receipt.getCustomer().getCurrency() != null) {
                                currencyId = receipt.getCustomer().getCurrency().getCurrencyID();
                                customerCreditLimit = receipt.getCustomer().getCreditlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, customerCreditLimit, currencyId, receipt.getCustomer().getCreatedOn(), 0);
                                customerCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            }
                            invoiceJson.put("creditlimit", customerCreditLimit);
                            invoiceJson.put("creditlimitinbase", customerCreditLimitInbase);
                            Date dueDate = df.parse(df.format(creationDate));

//                            if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                accruedbalance = authHandler.round(amountdue, companyid);
//                            } else
                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }

                            switch(noOfInterval){
                                case 2:
                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 3:
                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 4:
                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 5:
                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 6:
                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 7:
                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 8:
                                    amountdue9 += amountdue10 + amountdue11;
                                    amountdue10 = amountdue11 = 0;
                                    break;
                                case 9:
                                    amountdue10 += amountdue11;
                                    amountdue11 = 0;
                                    break;
                            }
                            
                            invoiceJson.put(InvoiceConstants.amountdue1, amountdue1);
                            invoiceJson.put(InvoiceConstants.amountdue2, amountdue2);
                            invoiceJson.put(InvoiceConstants.amountdue3, amountdue3);
                            invoiceJson.put(InvoiceConstants.amountdue4, amountdue4);
                            invoiceJson.put(InvoiceConstants.amountdue5, amountdue5);
                            invoiceJson.put(InvoiceConstants.amountdue6, amountdue6);
                            invoiceJson.put(InvoiceConstants.amountdue7, amountdue7);
                            invoiceJson.put(InvoiceConstants.amountdue8, amountdue8);
                            invoiceJson.put(InvoiceConstants.amountdue9, amountdue9);
                            invoiceJson.put(InvoiceConstants.amountdue10, amountdue10);
                            invoiceJson.put(InvoiceConstants.amountdue11, amountdue11);
//                            invoiceJson.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
                            if(!requestParams.containsKey("isAgedReceivables") || !(Boolean)requestParams.get("isAgedReceivables")){
                                getOpeningReceiptCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, request, receipt, replaceFieldMap, customFieldMap, customDateFieldMap, FieldMap, invoiceJson);
                            }

                            dataArray.put(invoiceJson);
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }
    
    public static void getOpeningReceiptCustomField(String companyId, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj, HttpServletRequest request, Receipt receipt, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, Integer> FieldMap, com.krawler.utils.json.base.JSONObject obj ) throws ServiceException, JSONException{
        if (accJournalEntryobj != null && fieldDataManagercntrl != null) {
            Map<String, Object> variableMap = new HashMap<String, Object>();
            HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
            Detailfilter_names.add("companyid");
            Detailfilter_params.add(companyId);
            Detailfilter_names.add("OpeningBalanceReceiptId");
            Detailfilter_params.add(receipt.getID());
            Detailfilter_names.add("moduleId");
            Detailfilter_params.add(Constants.Acc_Receive_Payment_ModuleId + "");
            invDetailRequestParams.put("filter_names", Detailfilter_names);
            invDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresult = accJournalEntryobj.getOpeningBalanceReceiptCustomData(invDetailRequestParams);
            if (idcustresult.getEntityList().size() > 0) {
                OpeningBalanceReceiptCustomData balanceReceiptCustomData = (OpeningBalanceReceiptCustomData) idcustresult.getEntityList().get(0);
                AccountingManager.setCustomColumnValues(balanceReceiptCustomData, FieldMap, replaceFieldMap, variableMap);
                JSONObject params = new JSONObject();
                params.put("companyid", "");
                boolean isExport = (boolean) ((request.getAttribute("isExport") == null) ? false : request.getAttribute("isExport"));
                if (!isExport) {
                    isExport = (request.getParameter("isAged") == null) ? false : Boolean.parseBoolean(request.getParameter("isAged"));
                }
                params.put("isExport", isExport);
                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
            }
        }
    }
    
    public static double getOpeningReceiptAmountDue(Receipt receipt, HashMap<String, Object> reqParams1, accReceiptDAO accReceiptDAOobj) throws ServiceException{
        double amountdue = 0;
        double linkAmount = 0;
        double pmtAmount = receipt.getDepositAmount();//original payment amount in amount currency
        KwlReturnObject result = accReceiptDAOobj.getLinkDetailReceipt(reqParams1);
        List<LinkDetailReceipt> linkedDetaisReceipts = result.getEntityList();
        for (LinkDetailReceipt ldr : linkedDetaisReceipts) {
            linkAmount += ldr.getAmount();
        }
        result = accReceiptDAOobj.getLinkDetailReceiptToDebitNote(reqParams1);
        List<LinkDetailReceiptToDebitNote> detail = result.getEntityList();
        for (LinkDetailReceiptToDebitNote ldr : detail) {
            linkAmount += ldr.getAmount();
        }
        KwlReturnObject temp = accReceiptDAOobj.getReceiptWriteOffEntries(reqParams1);
        List<ReceiptWriteOff> list1 = temp.getEntityList();
        for (ReceiptWriteOff R : list1) {
            linkAmount += R.getWrittenOffAmountInReceiptCurrency();
        }
        amountdue = pmtAmount - linkAmount;
        amountdue = -amountdue;//amount due will be negative for receipt payment
        return amountdue;
    }
    public static JSONArray getAgedOpeningBalanceReceiptJson(HashMap<String, Object> requestParams, List list, com.krawler.utils.json.base.JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, accReceiptDAO accReceiptDAOobj, JSONObject request, AccountingHandlerDAO accountingHandlerDAOobj, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj) {
        try {
            boolean ignoreZero = request.optString("ignorezero") != null ? Boolean.parseBoolean(request.optString("ignorezero")) : false;
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ 
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            int noOfInterval = request.has("noOfInterval") ? request.optInt("noOfInterval",7) : 7;
            String companyid = (String) requestParams.get(InvoiceConstants.companyid);
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(request.optString(Constants.companyKey), Constants.Acc_Receive_Payment_ModuleId));
            if (accAccountDAOobj != null) {
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }

            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.optString(Constants.asOfDate) != null) {
                    try {
                        curDate = df.parse(request.optString(Constants.asOfDate));
                    } catch (Exception e) {
                        curDate = new Date(request.optString(Constants.asOfDate));
                    }
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            if (list != null) {
                for (Object object : list) {

                    double amountdue1 = 0;
                    double amountdue2 = 0;
                    double amountdue3 = 0;
                    double amountdue4 = 0;
                    double amountdue5 = 0;
                    double amountdue6 = 0;
                    double amountdue7 = 0;
                    double amountdue8 = 0;
                    double amountdue9 = 0;
                    double amountdue10 = 0;
                    double amountdue11 = 0;
//                    double accruedbalance = 0;

                    Receipt receipt = (Receipt) object;
                    if (receipt != null) {
                        Logger.getLogger(AccInvoiceServiceHandler.class.getName()).log(Level.INFO, "Opening Receipt=" + receipt.getReceiptNumber());
                        double amountdue = 0;
                        double linkAmount = 0;
                        double pmtAmount = receipt.getDepositAmount();//original payment amount in amount currency
                        HashMap<String, Object> reqParams1 = new HashMap();
                        reqParams1.put("receiptid", receipt.getID());
                        reqParams1.put("companyid", companyid);
                        reqParams1.put(Constants.df, df);
                        if (requestParams.containsKey("asofdate") && requestParams.get("asofdate") != null) {
                            reqParams1.put("asofdate", requestParams.get("asofdate"));
                        }
                        KwlReturnObject result = accReceiptDAOobj.getLinkDetailReceipt(reqParams1);
                        List<LinkDetailReceipt> linkedDetaisReceipts = result.getEntityList();
                        for (LinkDetailReceipt ldr : linkedDetaisReceipts) {
                            linkAmount += ldr.getAmount();
                        }
                        result = accReceiptDAOobj.getLinkDetailReceiptToDebitNote(reqParams1);
                        List<LinkDetailReceiptToDebitNote> detail = result.getEntityList();
                        for (LinkDetailReceiptToDebitNote ldr : detail) {
                            linkAmount += ldr.getAmount();
                        }
                        KwlReturnObject temp = accReceiptDAOobj.getReceiptWriteOffEntries(reqParams1);
                        List<ReceiptWriteOff> list1 = temp.getEntityList();
                        for (ReceiptWriteOff R : list1) {
                            linkAmount += R.getWrittenOffAmountInReceiptCurrency();
                        }
                        amountdue = pmtAmount - linkAmount;
                        if (amountdue != 0 || !ignoreZero) {
                            JSONObject invoiceJson = new JSONObject();
                            Date creationDate = receipt.getCreationDate();
                            amountdue = -amountdue;//amount due will be negative for receipt payment
                            double exchangeRateForOtherCurrency = receipt.getExchangeRateForOpeningTransaction();
                            boolean isopeningBalanceRecceipt = receipt.isIsOpeningBalenceReceipt();
                            boolean isConversionRateFromCurrencyToBase = receipt.isConversionRateFromCurrencyToBase();

                            invoiceJson.put(InvoiceConstants.billid, receipt.getID());
                            invoiceJson.put(InvoiceConstants.billno, receipt.getReceiptNumber());
                            invoiceJson.put(InvoiceConstants.journalentryid, receipt.getJournalEntry() == null ? "" : receipt.getJournalEntry().getID());
                            invoiceJson.put(InvoiceConstants.withoutinventory, false);
                            invoiceJson.put(InvoiceConstants.currencysymbol, receipt.getCurrency() == null ? "" : receipt.getCurrency().getSymbol());
                            invoiceJson.put(InvoiceConstants.currencyid, (receipt.getCurrency() == null ? "" : receipt.getCurrency().getCurrencyID()));
                            invoiceJson.put(InvoiceConstants.currencyname, (receipt.getCurrency() == null ? "" : receipt.getCurrency().getName()));
                            invoiceJson.put(InvoiceConstants.entryno, "");
                            invoiceJson.put(InvoiceConstants.personid, receipt.getCustomer() == null ? "" : receipt.getCustomer().getID());
                            invoiceJson.put(InvoiceConstants.personname, receipt.getCustomer() == null ? "" : receipt.getCustomer().getName());
                            invoiceJson.put(InvoiceConstants.personinfo, receipt.getCustomer() == null ? "" : receipt.getCustomer().getName()+"("+receipt.getCustomer().getAcccode()+")");
                            invoiceJson.put(InvoiceConstants.aliasname, receipt.getCustomer() == null ? "" : receipt.getCustomer().getAliasname());
                            invoiceJson.put(InvoiceConstants.duedate, df.format(creationDate));
                            invoiceJson.put(InvoiceConstants.date, df.format(creationDate));
                            invoiceJson.put(InvoiceConstants.memo, receipt.getMemo() == null ? "" : receipt.getMemo());
                            invoiceJson.put(InvoiceConstants.deleted, receipt.isDeleted());
                            invoiceJson.put(InvoiceConstants.externalcurrencyrate, exchangeRateForOtherCurrency);
                            invoiceJson.put(InvoiceConstants.isConversionRateFromCurrencyToBase, receipt.isConversionRateFromCurrencyToBase());
                            invoiceJson.put("isOpeningBalanceTransaction", isopeningBalanceRecceipt);
                            invoiceJson.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + ((isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency ) + " " + (receipt.getCurrency() == null ? "" : receipt.getCurrency().getCurrencyCode()));
                            KwlReturnObject bAmt = null;
                            double openingBalanceAmountDueInBase = 0d;
//                            if (Constants.OpeningBalanceBaseAmountFlag) {
//                                openingBalanceAmountDueInBase = -receipt.getOpeningBalanceBaseAmountDue();
//                            } else {
                            if (isopeningBalanceRecceipt && receipt.isConversionRateFromCurrencyToBase()) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, receipt.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, receipt.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            }
                            openingBalanceAmountDueInBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
//                            }
                            invoiceJson.put(InvoiceConstants.amountdue, amountdue);
                            invoiceJson.put(InvoiceConstants.amountdueinbase, openingBalanceAmountDueInBase);
                            invoiceJson.put("amountinbase", receipt.getOriginalOpeningBalanceBaseAmount());
                            invoiceJson.put("code", receipt.getCustomer() != null ? receipt.getCustomer().getAcccode() : "");
                            invoiceJson.put("type", "Payment Received");

                            Date dueDate = df.parse(df.format(creationDate));

//                            if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                accruedbalance = authHandler.round(amountdue, companyid);
//                            } else 
                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }
                            
                            switch(noOfInterval){
                                case 2:
                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 3:
                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 4:
                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 5:
                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 6:
                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 7:
                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 8:
                                    amountdue9 += amountdue10 + amountdue11;
                                    amountdue10 = amountdue11 = 0;
                                    break;
                                case 9:
                                    amountdue10 += amountdue11;
                                    amountdue11 = 0;
                                    break;
                            }

                            invoiceJson.put(InvoiceConstants.amountdue1, amountdue1);
                            invoiceJson.put(InvoiceConstants.amountdue2, amountdue2);
                            invoiceJson.put(InvoiceConstants.amountdue3, amountdue3);
                            invoiceJson.put(InvoiceConstants.amountdue4, amountdue4);
                            invoiceJson.put(InvoiceConstants.amountdue5, amountdue5);
                            invoiceJson.put(InvoiceConstants.amountdue6, amountdue6);
                            invoiceJson.put(InvoiceConstants.amountdue7, amountdue7);
                            invoiceJson.put(InvoiceConstants.amountdue8, amountdue8);
                            invoiceJson.put(InvoiceConstants.amountdue9, amountdue9);
                            invoiceJson.put(InvoiceConstants.amountdue10, amountdue10);
                            invoiceJson.put(InvoiceConstants.amountdue11, amountdue11);
//                            invoiceJson.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
                            if (accJournalEntryobj != null && fieldDataManagercntrl != null) {
                                Map<String, Object> variableMap = new HashMap<String, Object>();
                                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                                Detailfilter_names.add("companyid");
                                Detailfilter_params.add(receipt.getCompany().getCompanyID());
                                Detailfilter_names.add("OpeningBalanceReceiptId");
                                Detailfilter_params.add(receipt.getID());
                                Detailfilter_names.add("moduleId");
                                Detailfilter_params.add(Constants.Acc_Receive_Payment_ModuleId + "");
                                invDetailRequestParams.put("filter_names", Detailfilter_names);
                                invDetailRequestParams.put("filter_params", Detailfilter_params);
                                KwlReturnObject idcustresult = accJournalEntryobj.getOpeningBalanceReceiptCustomData(invDetailRequestParams);
                                if (idcustresult.getEntityList().size() > 0) {
                                    OpeningBalanceReceiptCustomData balanceReceiptCustomData = (OpeningBalanceReceiptCustomData) idcustresult.getEntityList().get(0);
                                    AccountingManager.setCustomColumnValues(balanceReceiptCustomData, FieldMap, replaceFieldMap, variableMap);
                                    JSONObject params = new JSONObject();
                                    params.put("companyid", "");
                                    boolean isExport = request.optBoolean("isExport",false);
                                    if(!isExport){
                                        isExport =  request.optBoolean("isAged",false);
                                    }
                                    params.put("isExport", isExport);
                                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, invoiceJson, params);
                                }
                            }

                            dataArray.put(invoiceJson);
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }

    public static JSONArray getAgedOpeningBalanceInvoiceJson(HashMap<String, Object> requestParams, List list, com.krawler.utils.json.base.JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, accInvoiceCMN accInvoiceCommon, HttpServletRequest request, AccountingHandlerDAO accountingHandlerDAOobj, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj) {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean ignoreZero = request.getParameter("ignorezero") != null ? Boolean.parseBoolean(request.getParameter("ignorezero")) : false;
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                //Handle Date Parse exception. ERP-33531
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;// 0 = Invoice Due date OR 1 = Invoice date
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Invoice_ModuleId));
            if (accAccountDAOobj != null) {
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }

            if (requestParams.get(Constants.asOfDate) != null) {  // Aged Of Document will be calculated on asOfDate.
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.getAttribute(Constants.asOfDate) != null) {
                    curDate = (Date) request.getAttribute(Constants.asOfDate);
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;
            
            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);
            
            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);
            
            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);
            
            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);
            
            if (list != null) {
                for (Object object : list) {
                    double amountdue1 = 0;
                    double amountdue2 = 0;
                    double amountdue3 = 0;
                    double amountdue4 = 0;
                    double amountdue5 = 0;
                    double amountdue6 = 0;
                    double amountdue7 = 0;
                    double amountdue8 = 0;
                    double amountdue9 = 0;
                    double amountdue10 = 0;
                    double amountdue11 = 0;
//                    double accruedbalance = 0;

                    Invoice invoice = (Invoice) object;
                    if (invoice != null) {
                        double amountdue= getOpeningInvoiceAmountDue(invoice, requestParams, accInvoiceCommon);
                        if (amountdue > 0 || !ignoreZero) {
                            com.krawler.utils.json.base.JSONObject invoiceJson = new com.krawler.utils.json.base.JSONObject();
                            Date invoiceCreationDate = invoice.getCreationDate();
                            Date invoiceDueDate = invoice.getDueDate();
                            double exchangeRateForOtherCurrency = invoice.getExchangeRateForOpeningTransaction();
                            boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                            boolean isConversionRateFromCurrencyToBase = invoice.isConversionRateFromCurrencyToBase();
                            invoiceJson.put(InvoiceConstants.billid, invoice.getID());
                            invoiceJson.put(InvoiceConstants.billno, invoice.getInvoiceNumber());
                            invoiceJson.put(InvoiceConstants.journalentryid, invoice.getJournalEntry() == null ? "" : invoice.getJournalEntry().getID());
                            invoiceJson.put(InvoiceConstants.withoutinventory, false);
                            invoiceJson.put(InvoiceConstants.currencysymbol, invoice.getCurrency() == null ? "" : invoice.getCurrency().getSymbol());
                            invoiceJson.put(InvoiceConstants.currencyid, (invoice.getCurrency() == null ? "" : invoice.getCurrency().getCurrencyID()));
                            invoiceJson.put(InvoiceConstants.currencyname, (invoice.getCurrency() == null ? "" : invoice.getCurrency().getName()));
                            invoiceJson.put(InvoiceConstants.entryno, "");
                            invoiceJson.put(InvoiceConstants.personid, invoice.getCustomer() == null ? "" : invoice.getCustomer().getID());
                            invoiceJson.put(InvoiceConstants.personname, invoice.getCustomer() == null ? "" : invoice.getCustomer().getName());
                            invoiceJson.put(InvoiceConstants.personinfo, invoice.getCustomer() == null ? "" : invoice.getCustomer().getName()+"("+invoice.getCustomer().getAcccode()+")");
                            invoiceJson.put(InvoiceConstants.aliasname, invoice.getCustomer() == null ? "" : invoice.getCustomer().getAliasname());
                            invoiceJson.put(InvoiceConstants.duedate, df.format(invoice.getDueDate()));
                            invoiceJson.put(InvoiceConstants.date, df.format(invoiceCreationDate));
                            invoiceJson.put(InvoiceConstants.memo, invoice.getMemo() == null ? "" : invoice.getMemo());
                            invoiceJson.put(InvoiceConstants.termname, invoice.getTermid() == null ? "" : invoice.getTermid().getTermname());
                            invoiceJson.put(InvoiceConstants.CustomerCreditTerm, invoice.getCustomer() == null ? "" : invoice.getCustomer().getCreditTerm()==null ?"" :invoice.getCustomer().getCreditTerm().getTermname());
                            invoiceJson.put(InvoiceConstants.deleted, invoice.isDeleted());
                            invoiceJson.put(InvoiceConstants.externalcurrencyrate, exchangeRateForOtherCurrency);
                            invoiceJson.put("isOpeningBalanceTransaction", isopeningBalanceInvoice);
                            invoiceJson.put("isConversionRateFromCurrencyToBase", invoice.isConversionRateFromCurrencyToBase());
                            invoiceJson.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + ((isopeningBalanceInvoice && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency ) + " " + (invoice.getCurrency() == null ? "" : invoice.getCurrency().getCurrencyCode()));
                            invoiceJson.put("amountinbase", invoice.getOriginalOpeningBalanceBaseAmount());
                            invoiceJson.put("code", invoice.getCustomer() != null ? invoice.getCustomer().getAcccode() : "");
                            invoiceJson.put("customercurrencyid", invoice.getCustomer() == null ? "" : invoice.getCustomer().getCurrency().getCurrencyID());
                            KwlReturnObject bAmt = null;
                            double amountdueinbase = 0d;
                            if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, invoice.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, invoice.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            }
                            amountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                            double customerCreditLimit = 0;
                            double customerCreditLimitInbase = 0;
                            String currencyId = "";
                            if (invoice.getCustomer()!=null && invoice.getCustomer().getCurrency() != null) {
                                currencyId = invoice.getCustomer().getCurrency().getCurrencyID();
                                customerCreditLimit = invoice.getCustomer().getCreditlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, customerCreditLimit, currencyId, invoice.getCustomer().getCreatedOn(), 0);
                                customerCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            }
                            
                            invoiceJson.put(InvoiceConstants.amountdue, amountdue);
                            invoiceJson.put(InvoiceConstants.amountdueinbase, amountdueinbase);
                            invoiceJson.put("type", "Sales Invoice");
                            MasterItem salesPerson = invoice.getMasterSalesPerson();
                            invoiceJson.put("salespersonname", salesPerson == null ? "" : salesPerson.getValue());
                            invoiceJson.put("salespersoncode", salesPerson == null ? "" : salesPerson.getValue());
                            invoiceJson.put("salespersoninfo", salesPerson == null ? "" : salesPerson.getValue()+"("+salesPerson.getCode()+")");
                            invoiceJson.put("salespersonid", salesPerson == null ? "" : salesPerson.getID());
                            invoiceJson.put("creditlimit", customerCreditLimit);
                            invoiceJson.put("creditlimitinbase", customerCreditLimitInbase);

                            Date dueDate = null;
                            if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                dueDate = df.parse(df.format(invoiceDueDate));
                            } else {
                                dueDate = df.parse(df.format(invoiceCreationDate));
                            }

//                            if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                accruedbalance = authHandler.round(amountdue, companyid);
//                            } else
                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }
                            
                            switch(noOfInterval){
                                case 2:
                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 3:
                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 4:
                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 5:
                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 6:
                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 7:
                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 8:
                                    amountdue9 += amountdue10 + amountdue11;
                                    amountdue10 = amountdue11 = 0;
                                    break;
                                case 9:
                                    amountdue10 += amountdue11;
                                    amountdue11 = 0;
                                    break;
                            }

                            invoiceJson.put(InvoiceConstants.amountdue1, amountdue1);
                            invoiceJson.put(InvoiceConstants.amountdue2, amountdue2);
                            invoiceJson.put(InvoiceConstants.amountdue3, amountdue3);
                            invoiceJson.put(InvoiceConstants.amountdue4, amountdue4);
                            invoiceJson.put(InvoiceConstants.amountdue5, amountdue5);
                            invoiceJson.put(InvoiceConstants.amountdue6, amountdue6);
                            invoiceJson.put(InvoiceConstants.amountdue7, amountdue7);
                            invoiceJson.put(InvoiceConstants.amountdue8, amountdue8);
                            invoiceJson.put(InvoiceConstants.amountdue9, amountdue9);
                            invoiceJson.put(InvoiceConstants.amountdue10, amountdue10);
                            invoiceJson.put(InvoiceConstants.amountdue11, amountdue11);

//                            invoiceJson.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
                            
                            if(!requestParams.containsKey("isAgedReceivables") || !(Boolean)requestParams.get("isAgedReceivables")){
                                getOpeningInvoiceCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, accountingHandlerDAOobj, request, invoice, replaceFieldMap, customFieldMap, customDateFieldMap, FieldMap, invoiceJson);
                            }   
                            dataArray.put(invoiceJson);
                        }
                    }
                }
//                requestParams.put("invoiceid", null);
            }
            replaceFieldMap = null;
            customFieldMap = null;
            customDateFieldMap = null;
            FieldMap = null;
            fieldrequestParams = null;
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }
    
    public static void getOpeningInvoiceCustomField(String companyId, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj, AccountingHandlerDAO accountingHandlerDAOobj, HttpServletRequest request, Invoice invoice, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, Integer> FieldMap, com.krawler.utils.json.base.JSONObject obj ) throws ServiceException, JSONException{
        if (accJournalEntryobj != null && fieldDataManagercntrl != null) {
            Map<String, Object> variableMap = new HashMap<String, Object>();
            HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
            Detailfilter_names.add("companyid");
            Detailfilter_params.add(companyId);
            Detailfilter_names.add("OpeningBalanceInvoiceId");
            Detailfilter_params.add(invoice.getID());
            Detailfilter_names.add("moduleId");
            Detailfilter_params.add(Constants.Acc_Invoice_ModuleId + "");
            invDetailRequestParams.put("filter_names", Detailfilter_names);
            invDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresult = accJournalEntryobj.getOpeningBalanceInvoiceCustomDataNew(invDetailRequestParams);
            if (idcustresult.getEntityList().size() > 0) {
                String openingBalanceInvoiceId = idcustresult.getEntityList().get(0).toString();
                KwlReturnObject jeCustomResult = accountingHandlerDAOobj.getObject(OpeningBalanceInvoiceCustomData.class.getName(), openingBalanceInvoiceId);
                OpeningBalanceInvoiceCustomData openingBalanceInvoiceCustomData = (OpeningBalanceInvoiceCustomData) jeCustomResult.getEntityList().get(0);
                AccountingManager.setCustomColumnValues(openingBalanceInvoiceCustomData, FieldMap, replaceFieldMap, variableMap);
                JSONObject params = new JSONObject();
                params.put("companyid", "");
                boolean isExport = (boolean) ((request.getAttribute("isExport") == null) ? false : request.getAttribute("isExport"));
                if (!isExport) {
                    isExport = (request.getParameter("isAged") == null) ? false : Boolean.parseBoolean(request.getParameter("isAged"));
                }
                params.put("isExport", isExport);
                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
            }
        }
    }
    
    public static double getOpeningInvoiceAmountDue(Invoice invoice, HashMap<String, Object> requestParams, accInvoiceCMN accInvoiceCommon) throws ServiceException{
        double amountdue = 0;
        if (invoice.getOpeningBalanceAmountDue() == invoice.getOriginalOpeningBalanceAmount()) {
//                          Payment hasn't been made for invoice. Skip calculation and take amount due from invoice table.
            amountdue = invoice.getOpeningBalanceAmountDue();
        } else {
            requestParams.put("invoiceAmtDueEqualsInvoiceAmt", false);
            List ll = accInvoiceCommon.getAmountDue_Discount(requestParams, invoice);
            amountdue = (Double) ll.get(0);
        }
        return amountdue;
    }
    public static JSONArray getAgedOpeningBalanceInvoiceJson(HashMap<String, Object> requestParams, List list, com.krawler.utils.json.base.JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, accInvoiceCMN accInvoiceCommon, JSONObject request, AccountingHandlerDAO accountingHandlerDAOobj, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj) {
        try {
            String companyid = request.optString("companyid");
            boolean ignoreZero = request.optString("ignorezero") != null ? Boolean.parseBoolean(request.optString("ignorezero")) : false;
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ 
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? df.parse(Constants.opening_Date) : df.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;// 0 = Invoice Due date OR 1 = Invoice date
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            int noOfInterval = request.has("noOfInterval") ? request.optInt("noOfInterval",7) : 7;
            
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(request.optString(Constants.companyKey), Constants.Acc_Invoice_ModuleId));
            if (accAccountDAOobj != null) {
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }

            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.optString(Constants.asOfDate) != null) {
                    try {
                        curDate = df.parse(request.optString(Constants.asOfDate));
                    } catch (Exception e) {
                        curDate = new Date(request.optString(Constants.asOfDate));
                    }
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);
                        
            if (list != null) {
                for (Object object : list) {
                    double amountdue1 = 0;
                    double amountdue2 = 0;
                    double amountdue3 = 0;
                    double amountdue4 = 0;
                    double amountdue5 = 0;
                    double amountdue6 = 0;
                    double amountdue7 = 0;
                    double amountdue8 = 0;
                    double amountdue9 = 0;
                    double amountdue10 = 0;
                    double amountdue11 = 0;
//                    double accruedbalance = 0;

                    Invoice invoice = (Invoice) object;
                    if (invoice != null) {
                        double amountdue=0;
                        if (invoice.getOpeningBalanceAmountDue() == invoice.getOriginalOpeningBalanceAmount()) {
//                          Payment hasn't been made for invoice. Skip calculation and take amount due from invoice table.
                            amountdue = invoice.getOpeningBalanceAmountDue();
                        } else {
                            requestParams.put("invoiceAmtDueEqualsInvoiceAmt", false);
                            List ll = accInvoiceCommon.getAmountDue_Discount(requestParams, invoice);
                            amountdue = (Double) ll.get(0);
                        }
                        if (amountdue > 0 || !ignoreZero) {
                            com.krawler.utils.json.base.JSONObject invoiceJson = new com.krawler.utils.json.base.JSONObject();
                            Date invoiceCreationDate = invoice.getCreationDate();
                            Date invoiceDueDate = invoice.getDueDate();
                            double exchangeRateForOtherCurrency = invoice.getExchangeRateForOpeningTransaction();
                            boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                            boolean isConversionRateFromCurrencyToBase = invoice.isConversionRateFromCurrencyToBase();
                            
                            invoiceJson.put(InvoiceConstants.billid, invoice.getID());
                            invoiceJson.put(InvoiceConstants.billno, invoice.getInvoiceNumber());
                            invoiceJson.put(InvoiceConstants.journalentryid, invoice.getJournalEntry() == null ? "" : invoice.getJournalEntry().getID());
                            invoiceJson.put(InvoiceConstants.withoutinventory, false);
                            invoiceJson.put(InvoiceConstants.currencysymbol, invoice.getCurrency() == null ? "" : invoice.getCurrency().getSymbol());
                            invoiceJson.put(InvoiceConstants.currencyid, (invoice.getCurrency() == null ? "" : invoice.getCurrency().getCurrencyID()));
                            invoiceJson.put(InvoiceConstants.currencyname, (invoice.getCurrency() == null ? "" : invoice.getCurrency().getName()));
                            invoiceJson.put(InvoiceConstants.entryno, "");
                            invoiceJson.put(InvoiceConstants.personid, invoice.getCustomer() == null ? "" : invoice.getCustomer().getID());
                            invoiceJson.put(InvoiceConstants.personname, invoice.getCustomer() == null ? "" : invoice.getCustomer().getName());
                            invoiceJson.put(InvoiceConstants.personinfo, invoice.getCustomer() == null ? "" : invoice.getCustomer().getName()+"("+invoice.getCustomer().getAcccode()+")");
                            invoiceJson.put(InvoiceConstants.aliasname, invoice.getCustomer() == null ? "" : invoice.getCustomer().getAliasname());
                            invoiceJson.put(InvoiceConstants.duedate, df.format(invoice.getDueDate()));
                            invoiceJson.put(InvoiceConstants.date, df.format(invoiceCreationDate));
                            invoiceJson.put(InvoiceConstants.memo, invoice.getMemo() == null ? "" : invoice.getMemo());
                            invoiceJson.put(InvoiceConstants.termname, invoice.getTermid() == null ? "" : invoice.getTermid().getTermname());
                            invoiceJson.put(InvoiceConstants.deleted, invoice.isDeleted());
                            invoiceJson.put(InvoiceConstants.externalcurrencyrate, exchangeRateForOtherCurrency);
                            invoiceJson.put("isOpeningBalanceTransaction", isopeningBalanceInvoice);
                            invoiceJson.put("isConversionRateFromCurrencyToBase", invoice.isConversionRateFromCurrencyToBase());
                            invoiceJson.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + ((isopeningBalanceInvoice && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency ) + " " + (invoice.getCurrency() == null ? "" : invoice.getCurrency().getCurrencyCode()));
                            invoiceJson.put("amountinbase", invoice.getOriginalOpeningBalanceBaseAmount());
                            invoiceJson.put("code", invoice.getCustomer() != null ? invoice.getCustomer().getAcccode() : "");
                            invoiceJson.put("customercurrencyid", invoice.getCustomer() == null ? "" : invoice.getCustomer().getCurrency().getCurrencyID());
                            KwlReturnObject bAmt = null;
                            double amountdueinbase = 0d;
                            if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, invoice.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, invoice.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            }
                            amountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                            invoiceJson.put(InvoiceConstants.amountdue, amountdue);
                            invoiceJson.put(InvoiceConstants.amountdueinbase, amountdueinbase);
                            invoiceJson.put("type", "Sales Invoice");

                            Date dueDate = null;
                            if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                dueDate = df.parse(df.format(invoiceDueDate));
                            } else {
                                dueDate = df.parse(df.format(invoiceCreationDate));
                            }

//                            if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                accruedbalance = authHandler.round(amountdue, companyid);
//                            } else 
                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }

                            switch(noOfInterval){
                                case 2:
                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 3:
                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 4:
                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 5:
                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 6:
                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 7:
                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 8:
                                    amountdue9 += amountdue10 + amountdue11;
                                    amountdue10 = amountdue11 = 0;
                                    break;
                                case 9:
                                    amountdue10 += amountdue11;
                                    amountdue11 = 0;
                                    break;
                            }

                            invoiceJson.put(InvoiceConstants.amountdue1, amountdue1);
                            invoiceJson.put(InvoiceConstants.amountdue2, amountdue2);
                            invoiceJson.put(InvoiceConstants.amountdue3, amountdue3);
                            invoiceJson.put(InvoiceConstants.amountdue4, amountdue4);
                            invoiceJson.put(InvoiceConstants.amountdue5, amountdue5);
                            invoiceJson.put(InvoiceConstants.amountdue6, amountdue6);
                            invoiceJson.put(InvoiceConstants.amountdue7, amountdue7);
                            invoiceJson.put(InvoiceConstants.amountdue8, amountdue8);
                            invoiceJson.put(InvoiceConstants.amountdue9, amountdue9);
                            invoiceJson.put(InvoiceConstants.amountdue10, amountdue10);
                            invoiceJson.put(InvoiceConstants.amountdue11, amountdue11);
//                            invoiceJson.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
                            if (accJournalEntryobj != null && fieldDataManagercntrl != null) {
                                Map<String, Object> variableMap = new HashMap<String, Object>();
                                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                                Detailfilter_names.add("companyid");
                                Detailfilter_params.add(invoice.getCompany().getCompanyID());
                                Detailfilter_names.add("OpeningBalanceInvoiceId");
                                Detailfilter_params.add(invoice.getID());
                                Detailfilter_names.add("moduleId");
                                Detailfilter_params.add(Constants.Acc_Invoice_ModuleId + "");
                                invDetailRequestParams.put("filter_names", Detailfilter_names);
                                invDetailRequestParams.put("filter_params", Detailfilter_params);
                                KwlReturnObject idcustresult = accJournalEntryobj.getOpeningBalanceInvoiceCustomDataNew(invDetailRequestParams);
                                if (idcustresult.getEntityList().size() > 0) {
                                    String openingBalanceInvoiceId = idcustresult.getEntityList().get(0).toString();
                                    KwlReturnObject jeCustomResult = accountingHandlerDAOobj.getObject(OpeningBalanceInvoiceCustomData.class.getName(), openingBalanceInvoiceId);
                                    OpeningBalanceInvoiceCustomData openingBalanceInvoiceCustomData = (OpeningBalanceInvoiceCustomData) jeCustomResult.getEntityList().get(0);
                                    AccountingManager.setCustomColumnValues(openingBalanceInvoiceCustomData, FieldMap, replaceFieldMap, variableMap);
                                    JSONObject params = new JSONObject();
                                    params.put("companyid", "");
                                    boolean isExport =  request.optBoolean("isExport",false);
                                    if (!isExport) {
                                        isExport = request.optBoolean("isAged",false);
                                    }
                                    params.put("isExport", isExport);
                                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, invoiceJson, params);
                                }
                            }

                            dataArray.put(invoiceJson);
                        }
                    }
                }
//                requestParams.put("invoiceid", null);
            }
            replaceFieldMap = null;
            customFieldMap = null;
            customDateFieldMap = null;
            FieldMap = null;
            fieldrequestParams = null;
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }

    public static JSONArray getAgedOpeningBalanceDebitNoteJson(HashMap<String, Object> requestParams, List list, com.krawler.utils.json.base.JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, accReceiptDAO accReceiptDao, HttpServletRequest request, AccountingHandlerDAO accountingHandlerDAOobj, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj,accCreditNoteDAO accCreditNoteDAOobj) {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean ignoreZero = request.getParameter("ignorezero") != null ? Boolean.parseBoolean(request.getParameter("ignorezero")) : false;
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ //Handle Date Parse exception. ERP-33531
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Debit_Note_ModuleId));
            if (accAccountDAOobj != null) {
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }
            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.getAttribute(Constants.asOfDate) != null) { //this set atrribute current date coming only in monthly ageing report
                    curDate = (Date) request.getAttribute(Constants.asOfDate);
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);
            
            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);
            
            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);
            
            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            if (list != null) {
                for (Object object : list) {

                    double amountdue1 = 0;
                    double amountdue2 = 0;
                    double amountdue3 = 0;
                    double amountdue4 = 0;
                    double amountdue5 = 0;
                    double amountdue6 = 0;
                    double amountdue7 = 0;
                    double amountdue8 = 0;
                    double amountdue9 = 0;
                    double amountdue10 = 0;
                    double amountdue11 = 0;
//                    double accruedbalance = 0;

                    DebitNote dn = (DebitNote) object;

                    if (dn != null) {
                        Logger.getLogger(AccInvoiceServiceHandler.class.getName()).log(Level.INFO, "Opening DN=" + dn.getDebitNoteNumber());
                        requestParams.put("debitnoteid", dn.getID());
                        double amountdue = getOpeningDebitNoteAmountDue(dn, requestParams, accReceiptDao, accCreditNoteDAOobj, companyid);
                        
                        requestParams.remove("debitnoteid");//Removing debitnoteid after use, So that is does not affect other 
                        if (amountdue > 0 || !ignoreZero) {
                            com.krawler.utils.json.base.JSONObject invoiceJson = new com.krawler.utils.json.base.JSONObject();
                            Date creationDate = dn.getCreationDate();
                            double exchangeRateForOtherCurrency = dn.getExchangeRateForOpeningTransaction();
                            boolean isopeningBalanceDN = dn.isIsOpeningBalenceDN();
                            boolean isConversionRateFromCurrencyToBase = dn.isConversionRateFromCurrencyToBase();
                            
                            invoiceJson.put(InvoiceConstants.billid, dn.getID());
                            invoiceJson.put(InvoiceConstants.billno, dn.getDebitNoteNumber());
                            invoiceJson.put(InvoiceConstants.journalentryid, dn.getJournalEntry() == null ? "" : dn.getJournalEntry().getID());
                            invoiceJson.put(InvoiceConstants.withoutinventory, false);
                            invoiceJson.put(InvoiceConstants.currencysymbol, dn.getCurrency() == null ? "" : dn.getCurrency().getSymbol());
                            invoiceJson.put(InvoiceConstants.currencyid, (dn.getCurrency() == null ? "" : dn.getCurrency().getCurrencyID()));
                            invoiceJson.put(InvoiceConstants.currencyname, (dn.getCurrency() == null ? "" : dn.getCurrency().getName()));
                            invoiceJson.put(InvoiceConstants.entryno, "");
                            invoiceJson.put(InvoiceConstants.personid, dn.getCustomer() == null ? "" : dn.getCustomer().getID());
                            invoiceJson.put(InvoiceConstants.personname, dn.getCustomer() == null ? "" : dn.getCustomer().getName());
                            invoiceJson.put(InvoiceConstants.personinfo, dn.getCustomer() == null ? "" : dn.getCustomer().getName()+"("+dn.getCustomer().getAcccode()+")");
                            invoiceJson.put(InvoiceConstants.aliasname, dn.getCustomer() == null ? "" : dn.getCustomer().getAliasname());
                            invoiceJson.put(InvoiceConstants.CustomerCreditTerm, dn.getCustomer() == null ? "" : dn.getCustomer().getCreditTerm()==null?"":dn.getCustomer().getCreditTerm().getTermname());
                            invoiceJson.put(InvoiceConstants.duedate, df.format(creationDate));
                            invoiceJson.put(InvoiceConstants.date, df.format(creationDate));
                            invoiceJson.put(InvoiceConstants.memo, dn.getMemo() == null ? "" : dn.getMemo());
                            invoiceJson.put(InvoiceConstants.deleted, dn.isDeleted());
                            invoiceJson.put(InvoiceConstants.externalcurrencyrate, exchangeRateForOtherCurrency);
                            invoiceJson.put("isOpeningBalanceTransaction", isopeningBalanceDN);
                            invoiceJson.put(InvoiceConstants.isConversionRateFromCurrencyToBase, dn.isConversionRateFromCurrencyToBase());
                            invoiceJson.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + ((isopeningBalanceDN && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency ) + " " + (dn.getCurrency() == null ? "" : dn.getCurrency().getCurrencyCode()));
                            KwlReturnObject bAmt = null;
                            double openingBalanceAmountDueInBase = 0d;

                            if (isopeningBalanceDN && dn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, dn.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dn.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            }
                            openingBalanceAmountDueInBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                            invoiceJson.put(InvoiceConstants.amountdue, amountdue);
                            invoiceJson.put(InvoiceConstants.amountdueinbase, openingBalanceAmountDueInBase);
                            invoiceJson.put("amountinbase", dn.getOriginalOpeningBalanceBaseAmount());
                            invoiceJson.put("code", dn.getCustomer() != null ? dn.getCustomer().getAcccode() : "");
                            invoiceJson.put("type", "Debit Note");
                            double customerCreditLimit = 0;
                            double customerCreditLimitInbase = 0;
                            String currencyId = "";
                            if (dn.getCustomer() != null && dn.getCustomer().getCurrency() != null) {
                                currencyId = dn.getCustomer().getCurrency().getCurrencyID();
                                customerCreditLimit = dn.getCustomer().getCreditlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, customerCreditLimit, currencyId, dn.getCustomer().getCreatedOn(), 0);
                                customerCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            }
                            invoiceJson.put("creditlimit", customerCreditLimit);
                            invoiceJson.put("creditlimitinbase", customerCreditLimitInbase);
                            Date dueDate = df.parse(df.format(creationDate));

//                            if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                accruedbalance = authHandler.round(amountdue, companyid);
//                            } else
                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }
                            
                            switch(noOfInterval){
                                case 2:
                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 3:
                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 4:
                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 5:
                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 6:
                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 7:
                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 8:
                                    amountdue9 += amountdue10 + amountdue11;
                                    amountdue10 = amountdue11 = 0;
                                    break;
                                case 9:
                                    amountdue10 += amountdue11;
                                    amountdue11 = 0;
                                    break;
                            }

                            invoiceJson.put(InvoiceConstants.amountdue1, amountdue1);
                            invoiceJson.put(InvoiceConstants.amountdue2, amountdue2);
                            invoiceJson.put(InvoiceConstants.amountdue3, amountdue3);
                            invoiceJson.put(InvoiceConstants.amountdue4, amountdue4);
                            invoiceJson.put(InvoiceConstants.amountdue5, amountdue5);
                            invoiceJson.put(InvoiceConstants.amountdue6, amountdue6);
                            invoiceJson.put(InvoiceConstants.amountdue7, amountdue7);
                            invoiceJson.put(InvoiceConstants.amountdue8, amountdue8);
                            invoiceJson.put(InvoiceConstants.amountdue9, amountdue9);
                            invoiceJson.put(InvoiceConstants.amountdue10, amountdue10);
                            invoiceJson.put(InvoiceConstants.amountdue11, amountdue11);
//                            invoiceJson.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
                            if(!requestParams.containsKey("isAgedReceivables") || !(Boolean)requestParams.get("isAgedReceivables")){
                                getOpeningDebitNoteCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, request, dn, replaceFieldMap, customFieldMap, customDateFieldMap, FieldMap, invoiceJson);
                            }

                            dataArray.put(invoiceJson);
                        }
                    }
                }
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }
    
    public static void getOpeningDebitNoteCustomField(String companyId, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj, HttpServletRequest request, DebitNote dn, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, Integer> FieldMap, com.krawler.utils.json.base.JSONObject obj ) throws ServiceException, JSONException{
        if (accJournalEntryobj != null && fieldDataManagercntrl != null) {
            Map<String, Object> variableMap = new HashMap<String, Object>();
            HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
            Detailfilter_names.add("companyid");
            Detailfilter_params.add(companyId);
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
                params.put("companyid", "");
                boolean isExport = (boolean) ((request.getAttribute("isExport") == null) ? false : request.getAttribute("isExport"));
                if (!isExport) {
                    isExport = (request.getParameter("isAged") == null) ? false : Boolean.parseBoolean(request.getParameter("isAged"));
                }
                params.put("isExport", isExport);
                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
            }
        }
    }
    
    
    public static double getOpeningDebitNoteAmountDue(DebitNote dn, HashMap<String, Object> requestParams, accReceiptDAO accReceiptDao, accCreditNoteDAO accCreditNoteDAOobj, String companyid) throws ServiceException{
        double dnReceiptAmount = 0;
        KwlReturnObject dnpResult = accReceiptDao.getDebitNotePaymentDetail(requestParams);
        if (!dnpResult.getEntityList().isEmpty()) {
            Iterator dnpItr = dnpResult.getEntityList().iterator();
            while (dnpItr.hasNext()) {
                Object[] objects = (Object[]) dnpItr.next();
//                                double exchangeratefortransaction = objects[7] != null ? (Double) objects[7] : 1.0;
                double dnPaidAmtInDNCurrency = objects[2] != null ? (Double) objects[2] : 0.0;
                //dnReceiptAmount += authHandler.round(dnPaidAmtInReceiptCurrency / exchangeratefortransaction, Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
                dnReceiptAmount += authHandler.round(dnPaidAmtInDNCurrency, companyid);
            }
        }

        double linkAmount = 0;
        KwlReturnObject linkResult = accReceiptDao.getLinkDetailReceiptToDebitNote(requestParams);
        List<LinkDetailReceiptToDebitNote> detail = linkResult.getEntityList();
        for (LinkDetailReceiptToDebitNote ldr : detail) {
            linkAmount += ldr.getAmountInDNCurrency();
        }

        /*
         * Inlcuding Debit Note amount Linked in Credit Note
         */
        double cnLinkAmount = 0;
        KwlReturnObject result = accCreditNoteDAOobj.getCNRowsFromDebitNote(requestParams);
        List<CreditNoteDetail> rows = result.getEntityList();
        for (CreditNoteDetail creditNoteDetail : rows) {
            double exchangeRate = creditNoteDetail.getExchangeRateForTransaction();
            Discount disc = creditNoteDetail.getDiscount();
            if (disc != null) {
                //When currency of CN and DN is different, if currency is same then exchangeRate=1, case exchangeRate!=0 is applied for avaoiding AE
                if (exchangeRate != 1 && exchangeRate != 0) {
                    cnLinkAmount += authHandler.round(disc.getDiscountValue() / exchangeRate, companyid);
                } else {
                    cnLinkAmount += disc.getDiscountValue();
                }
            }
        }
        double amountdue = dn.getDnamount() - (dnReceiptAmount + linkAmount + cnLinkAmount);
        return amountdue;
    }
    public static JSONArray getAgedOpeningBalanceDebitNoteJson(HashMap<String, Object> requestParams, List list, com.krawler.utils.json.base.JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, accReceiptDAO accReceiptDao, JSONObject request, AccountingHandlerDAO accountingHandlerDAOobj, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj,accCreditNoteDAO accCreditNoteDAOobj) {
        try {
            String companyid = request.optString("companyid");
            boolean ignoreZero = request.optString("ignorezero") != null ? Boolean.parseBoolean(request.optString("ignorezero")) : false;
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ 
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            int noOfInterval = request.has("noOfInterval") ? request.optInt("noOfInterval",7) : 7;
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(request.optString(Constants.companyKey), Constants.Acc_Debit_Note_ModuleId));
            if (accAccountDAOobj != null) {
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }
            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.optString(Constants.asOfDate) != null) { //this set atrribute current date coming only in monthly ageing report
                    curDate =df.parse(request.optString(Constants.asOfDate)); 
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            if (list != null) {
                for (Object object : list) {

                    double amountdue1 = 0;
                    double amountdue2 = 0;
                    double amountdue3 = 0;
                    double amountdue4 = 0;
                    double amountdue5 = 0;
                    double amountdue6 = 0;
                    double amountdue7 = 0;
                    double amountdue8 = 0;
                    double amountdue9 = 0;
                    double amountdue10 = 0;
                    double amountdue11 = 0;
//                    double accruedbalance = 0;

                    DebitNote dn = (DebitNote) object;

                    if (dn != null) {
                        Logger.getLogger(AccInvoiceServiceHandler.class.getName()).log(Level.INFO, "Opening DN=" + dn.getDebitNoteNumber());
                        requestParams.put("debitnoteid", dn.getID());
                        double dnReceiptAmount = 0;
                        KwlReturnObject dnpResult = accReceiptDao.getDebitNotePaymentDetail(requestParams);
                        if (!dnpResult.getEntityList().isEmpty()) {
                            Iterator dnpItr = dnpResult.getEntityList().iterator();
                            while (dnpItr.hasNext()) {
                                Object[] objects = (Object[]) dnpItr.next();
//                                double exchangeratefortransaction = objects[7] != null ? (Double) objects[7] : 1.0;
                                double dnPaidAmtInDNCurrency = objects[2] != null ? (Double) objects[2] : 0.0;
                                //dnReceiptAmount += authHandler.round(dnPaidAmtInReceiptCurrency / exchangeratefortransaction, Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
                                dnReceiptAmount += authHandler.round(dnPaidAmtInDNCurrency, companyid);
                            }
                        }

                        double linkAmount = 0;
                        KwlReturnObject linkResult = accReceiptDao.getLinkDetailReceiptToDebitNote(requestParams);
                        List<LinkDetailReceiptToDebitNote> detail = linkResult.getEntityList();
                        for (LinkDetailReceiptToDebitNote ldr : detail) {
                            linkAmount += ldr.getAmountInDNCurrency();
                        }
                        
                          /*
                         * Inlcuding Debit Note amount Linked in Credit Note
                         */
                       double cnLinkAmount = 0; 
                       KwlReturnObject result = accCreditNoteDAOobj.getCNRowsFromDebitNote(requestParams);
                        List<CreditNoteDetail> rows = result.getEntityList();
                        for (CreditNoteDetail creditNoteDetail : rows) {
                            double exchangeRate = creditNoteDetail.getExchangeRateForTransaction();
                            Discount disc = creditNoteDetail.getDiscount();
                            if (disc != null) {
                                //When currency of CN and DN is different, if currency is same then exchangeRate=1, case exchangeRate!=0 is applied for avaoiding AE
                                if(exchangeRate!=1 && exchangeRate!=0){
                                   cnLinkAmount += authHandler.round(disc.getDiscountValue()/exchangeRate, companyid); 
                                } else{
                                   cnLinkAmount += disc.getDiscountValue();  
                                }
                            }
                        }
                        double amountdue = dn.getDnamount() - (dnReceiptAmount + linkAmount+cnLinkAmount);
                        if (amountdue > 0 || !ignoreZero) {
                            com.krawler.utils.json.base.JSONObject invoiceJson = new com.krawler.utils.json.base.JSONObject();
                            Date creationDate = dn.getCreationDate();
                            double exchangeRateForOtherCurrency = dn.getExchangeRateForOpeningTransaction();
                            boolean isopeningBalanceDN = dn.isIsOpeningBalenceDN();
                            boolean isConversionRateFromCurrencyToBase = dn.isConversionRateFromCurrencyToBase();
                            
                            invoiceJson.put(InvoiceConstants.billid, dn.getID());
                            invoiceJson.put(InvoiceConstants.billno, dn.getDebitNoteNumber());
                            invoiceJson.put(InvoiceConstants.journalentryid, dn.getJournalEntry() == null ? "" : dn.getJournalEntry().getID());
                            invoiceJson.put(InvoiceConstants.withoutinventory, false);
                            invoiceJson.put(InvoiceConstants.currencysymbol, dn.getCurrency() == null ? "" : dn.getCurrency().getSymbol());
                            invoiceJson.put(InvoiceConstants.currencyid, (dn.getCurrency() == null ? "" : dn.getCurrency().getCurrencyID()));
                            invoiceJson.put(InvoiceConstants.currencyname, (dn.getCurrency() == null ? "" : dn.getCurrency().getName()));
                            invoiceJson.put(InvoiceConstants.entryno, "");
                            invoiceJson.put(InvoiceConstants.personid, dn.getCustomer() == null ? "" : dn.getCustomer().getID());
                            invoiceJson.put(InvoiceConstants.personname, dn.getCustomer() == null ? "" : dn.getCustomer().getName());
                            invoiceJson.put(InvoiceConstants.personinfo, dn.getCustomer() == null ? "" : dn.getCustomer().getName()+"("+dn.getCustomer().getAcccode()+")");
                            invoiceJson.put(InvoiceConstants.aliasname, dn.getCustomer() == null ? "" : dn.getCustomer().getAliasname());
                            invoiceJson.put(InvoiceConstants.duedate, df.format(creationDate));
                            invoiceJson.put(InvoiceConstants.date, df.format(creationDate));
                            invoiceJson.put(InvoiceConstants.memo, dn.getMemo() == null ? "" : dn.getMemo());
                            invoiceJson.put(InvoiceConstants.deleted, dn.isDeleted());
                            invoiceJson.put(InvoiceConstants.externalcurrencyrate, exchangeRateForOtherCurrency);
                            invoiceJson.put("isOpeningBalanceTransaction", isopeningBalanceDN);
                            invoiceJson.put(InvoiceConstants.isConversionRateFromCurrencyToBase, dn.isConversionRateFromCurrencyToBase());
                            invoiceJson.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + ((isopeningBalanceDN && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency ) + " " + (dn.getCurrency() == null ? "" : dn.getCurrency().getCurrencyCode()));
                            KwlReturnObject bAmt = null;
                            double openingBalanceAmountDueInBase = 0d;

                            if (isopeningBalanceDN && dn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, dn.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dn.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            }
                            openingBalanceAmountDueInBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                            invoiceJson.put(InvoiceConstants.amountdue, amountdue);
                            invoiceJson.put(InvoiceConstants.amountdueinbase, openingBalanceAmountDueInBase);
                            invoiceJson.put("amountinbase", dn.getOriginalOpeningBalanceBaseAmount());
                            invoiceJson.put("code", dn.getCustomer() != null ? dn.getCustomer().getAcccode() : "");
                            invoiceJson.put("type", "Debit Note");

                            Date dueDate = df.parse(df.format(creationDate));

//                            if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                accruedbalance = authHandler.round(amountdue, companyid);
//                            } else 
                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }

                            switch(noOfInterval){
                                case 2:
                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 3:
                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 4:
                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 5:
                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 6:
                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 7:
                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 8:
                                    amountdue9 += amountdue10 + amountdue11;
                                    amountdue10 = amountdue11 = 0;
                                    break;
                                case 9:
                                    amountdue10 += amountdue11;
                                    amountdue11 = 0;
                                    break;
                            }
                            
                            invoiceJson.put(InvoiceConstants.amountdue1, amountdue1);
                            invoiceJson.put(InvoiceConstants.amountdue2, amountdue2);
                            invoiceJson.put(InvoiceConstants.amountdue3, amountdue3);
                            invoiceJson.put(InvoiceConstants.amountdue4, amountdue4);
                            invoiceJson.put(InvoiceConstants.amountdue5, amountdue5);
                            invoiceJson.put(InvoiceConstants.amountdue6, amountdue6);
                            invoiceJson.put(InvoiceConstants.amountdue7, amountdue7);
                            invoiceJson.put(InvoiceConstants.amountdue8, amountdue8);
                            invoiceJson.put(InvoiceConstants.amountdue9, amountdue9);
                            invoiceJson.put(InvoiceConstants.amountdue10, amountdue10);
                            invoiceJson.put(InvoiceConstants.amountdue11, amountdue11);
//                            invoiceJson.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
                            if (accJournalEntryobj != null && fieldDataManagercntrl != null) {
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
                                    params.put("companyid", "");
                                    boolean isExport = request.optBoolean("isExport",false);
                                    if(!isExport){
                                        isExport =  request.optBoolean("isAged",false);
                                    }
                                    params.put("isExport", isExport);
                                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, invoiceJson, params);
                                }
                            }

                            dataArray.put(invoiceJson);
                        }
                    }
                }
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }

    public static JSONArray getAgedOpeningBalanceCreditNoteJson(HashMap<String, Object> requestParams, List list, com.krawler.utils.json.base.JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, accPaymentDAO accPaymentDAOobj, HttpServletRequest request, AccountingHandlerDAO accountingHandlerDAOobj, accCreditNoteDAO accCreditNoteDAOobj, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj) {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean ignoreZero = request.getParameter("ignorezero") != null ? Boolean.parseBoolean(request.getParameter("ignorezero")) : false;
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){//Handle Date Parse exception. ERP-33531
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Credit_Note_ModuleId));
            if (accAccountDAOobj != null) {
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }
            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.getAttribute(Constants.asOfDate) != null) {
                    curDate = (Date) request.getAttribute(Constants.asOfDate);
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            if (list != null) {
                for (Object object : list) {

                    double amountdue1 = 0;
                    double amountdue2 = 0;
                    double amountdue3 = 0;
                    double amountdue4 = 0;
                    double amountdue5 = 0;
                    double amountdue6 = 0;
                    double amountdue7 = 0;
                    double amountdue8 = 0;
                    double amountdue9 = 0;
                    double amountdue10 = 0;
                    double amountdue11 = 0;
//                    double accruedbalance = 0;

                    CreditNote cn = (CreditNote) object;

                    if (cn != null) {
                        Logger.getLogger(AccInvoiceServiceHandler.class.getName()).log(Level.INFO, "Opening CN=" + cn.getCreditNoteNumber());
                        requestParams.put("creditnoteid", cn.getID());
                        double amountdue = getOpeningCreditNoteAmountDue(cn, requestParams, accPaymentDAOobj, accCreditNoteDAOobj, companyid);
                        requestParams.remove("creditnoteid");//Removing debitnoteid after use, So that is does not affect other 
                        if (amountdue < 0 || !ignoreZero) {
                            com.krawler.utils.json.base.JSONObject invoiceJson = new com.krawler.utils.json.base.JSONObject();
                            Date creationDate = cn.getCreationDate();
                            double exchangeRateForOtherCurrency = cn.getExchangeRateForOpeningTransaction();
                            boolean isopeningBalanceTransaction = cn.isIsOpeningBalenceCN();
                            boolean isConversionRateFromCurrencyToBase = cn.isConversionRateFromCurrencyToBase();

                            invoiceJson.put(InvoiceConstants.billid, cn.getID());
                            invoiceJson.put(InvoiceConstants.billno, cn.getCreditNoteNumber());
                            invoiceJson.put(InvoiceConstants.journalentryid, cn.getJournalEntry() == null ? "" : cn.getJournalEntry().getID());
                            invoiceJson.put(InvoiceConstants.withoutinventory, false);
                            invoiceJson.put(InvoiceConstants.currencysymbol, cn.getCurrency() == null ? "" : cn.getCurrency().getSymbol());
                            invoiceJson.put(InvoiceConstants.currencyid, (cn.getCurrency() == null ? "" : cn.getCurrency().getCurrencyID()));
                            invoiceJson.put(InvoiceConstants.currencyname, (cn.getCurrency() == null ? "" : cn.getCurrency().getName()));
                            invoiceJson.put(InvoiceConstants.entryno, "");
                            invoiceJson.put(InvoiceConstants.personid, cn.getCustomer() == null ? "" : cn.getCustomer().getID());
                            invoiceJson.put(InvoiceConstants.personname, cn.getCustomer() == null ? "" : cn.getCustomer().getName());
                            invoiceJson.put(InvoiceConstants.personinfo, cn.getCustomer() == null ? "" : cn.getCustomer().getName()+"("+cn.getCustomer().getAcccode()+")");
                            invoiceJson.put(InvoiceConstants.aliasname, cn.getCustomer() == null ? "" : cn.getCustomer().getAliasname());
                            invoiceJson.put(InvoiceConstants.CustomerCreditTerm, cn.getCustomer() == null ? "" : cn.getCustomer().getCreditTerm() == null ? "" : cn.getCustomer().getCreditTerm().getTermname());
                            invoiceJson.put(InvoiceConstants.duedate, df.format(creationDate));
                            invoiceJson.put(InvoiceConstants.date, df.format(creationDate));
                            invoiceJson.put(InvoiceConstants.memo, cn.getMemo() == null ? "" : cn.getMemo());
                            invoiceJson.put(InvoiceConstants.deleted, cn.isDeleted());
                            invoiceJson.put(InvoiceConstants.externalcurrencyrate, exchangeRateForOtherCurrency);
                            invoiceJson.put("isOpeningBalanceTransaction", isopeningBalanceTransaction);
                            invoiceJson.put(InvoiceConstants.isConversionRateFromCurrencyToBase, isConversionRateFromCurrencyToBase);
                            invoiceJson.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + ((isopeningBalanceTransaction && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency ) + " " + (cn.getCurrency() == null ? "" : cn.getCurrency().getCurrencyCode()));
                            KwlReturnObject bAmt = null;
                            double amountdueinbase = 0d;
                            if (isopeningBalanceTransaction && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, cn.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cn.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            }
                            amountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            invoiceJson.put(InvoiceConstants.amountdue, amountdue);
                            invoiceJson.put(InvoiceConstants.amountdueinbase, amountdueinbase);
                            invoiceJson.put("amountinbase", cn.getOriginalOpeningBalanceBaseAmount());
                            invoiceJson.put("code", cn.getCustomer() != null ? cn.getCustomer().getAcccode() : "");
                            double customerCreditLimit = 0;
                            double customerCreditLimitInbase = 0;
                            String currencyId = "";
                            if (cn.getCustomer() != null && cn.getCustomer().getCurrency() != null) {
                                currencyId = cn.getCustomer().getCurrency().getCurrencyID();
                                customerCreditLimit = cn.getCustomer().getCreditlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, customerCreditLimit, currencyId, cn.getCustomer().getCreatedOn(), 0);
                                customerCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            }
                            invoiceJson.put("creditlimit", customerCreditLimit);
                            invoiceJson.put("creditlimitinbase", customerCreditLimitInbase);
                            invoiceJson.put("type", "Credit Note");

                            Date dueDate = df.parse(df.format(creationDate));

//                            if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                accruedbalance = authHandler.round(amountdue, companyid);
//                            } else 
                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }

                            switch(noOfInterval){
                                case 2:
                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 3:
                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 4:
                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 5:
                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 6:
                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 7:
                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 8:
                                    amountdue9 += amountdue10 + amountdue11;
                                    amountdue10 = amountdue11 = 0;
                                    break;
                                case 9:
                                    amountdue10 += amountdue11;
                                    amountdue11 = 0;
                                    break;
                            }
                            
                            invoiceJson.put(InvoiceConstants.amountdue1, amountdue1);
                            invoiceJson.put(InvoiceConstants.amountdue2, amountdue2);
                            invoiceJson.put(InvoiceConstants.amountdue3, amountdue3);
                            invoiceJson.put(InvoiceConstants.amountdue4, amountdue4);
                            invoiceJson.put(InvoiceConstants.amountdue5, amountdue5);
                            invoiceJson.put(InvoiceConstants.amountdue6, amountdue6);
                            invoiceJson.put(InvoiceConstants.amountdue7, amountdue7);
                            invoiceJson.put(InvoiceConstants.amountdue8, amountdue8);
                            invoiceJson.put(InvoiceConstants.amountdue9, amountdue9);
                            invoiceJson.put(InvoiceConstants.amountdue10, amountdue10);
                            invoiceJson.put(InvoiceConstants.amountdue11, amountdue11);
                            
//                            invoiceJson.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
                            if(!requestParams.containsKey("isAgedReceivables") || !(Boolean)requestParams.get("isAgedReceivables")){
                                getOpeningCreditNoteCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, request, cn, replaceFieldMap, customFieldMap, customDateFieldMap, FieldMap, invoiceJson);
                            }

                            dataArray.put(invoiceJson);
                        }
                    }
                }
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }
    
    public static void getOpeningCreditNoteCustomField(String companyId, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj, HttpServletRequest request, CreditNote cn, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, Integer> FieldMap, com.krawler.utils.json.base.JSONObject obj ) throws ServiceException, JSONException{
        if (accJournalEntryobj != null && fieldDataManagercntrl != null) {
            Map<String, Object> variableMap = new HashMap<String, Object>();
            HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
            Detailfilter_names.add("companyid");
            Detailfilter_params.add(companyId);
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
                params.put("companyid", "");
                boolean isExport = (boolean) ((request.getAttribute("isExport") == null) ? false : request.getAttribute("isExport"));
                if (!isExport) {
                    isExport = (request.getParameter("isAged") == null) ? false : Boolean.parseBoolean(request.getParameter("isAged"));
                }
                params.put("isExport", isExport);
                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
            }
        }
    }
    
    public static double getOpeningCreditNoteAmountDue(CreditNote cn, HashMap<String, Object> requestParams, accPaymentDAO accPaymentDAOobj, accCreditNoteDAO accCreditNoteDAOobj, String companyid) throws ServiceException{
        double cnPaidAmount = 0;
        double dnReturnAmt = 0;

        KwlReturnObject cnpResult = accPaymentDAOobj.getCreditNotePaymentDetails(requestParams);
        if (!cnpResult.getEntityList().isEmpty()) {
            Iterator cnpItr = cnpResult.getEntityList().iterator();
            while (cnpItr.hasNext()) {
                Object[] objects = (Object[]) cnpItr.next();
//                                double exchangeratefortransaction = objects[7] != null ? (Double) objects[7] : 1.0;
                double cnPaidAmtInCNCurrency = objects[2] != null ? (Double) objects[2] : 0.0;
                //cnPaidAmount += authHandler.round(cnPaidAmtInReceiptCurrency / exchangeratefortransaction, Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
                cnPaidAmount += authHandler.round(cnPaidAmtInCNCurrency, companyid);
            }
        }

        double invReturnAmt = 0;
        KwlReturnObject result = accCreditNoteDAOobj.getCNRowsFromInvoice(requestParams);
        List<CreditNoteDetail> rows = result.getEntityList();
        for (CreditNoteDetail detail : rows) {
            Discount disc = detail.getDiscount();
            if (disc != null) {
                invReturnAmt += disc.getDiscountValue();
            }
        }
        result = accCreditNoteDAOobj.getCNRowsFromDebitNote(requestParams);
        rows = result.getEntityList();
        for (CreditNoteDetail detail : rows) {
            Discount disc = detail.getDiscount();
            if (disc != null) {
                dnReturnAmt += disc.getDiscountValue();
            }
        }
        double amountdue = cn.getCnamount() - (cnPaidAmount + invReturnAmt + dnReturnAmt);

        amountdue = -amountdue;//amount due will be negative for credit note
        return amountdue;
    }
    public static JSONArray getAgedOpeningBalanceCreditNoteJson(HashMap<String, Object> requestParams, List list, com.krawler.utils.json.base.JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, accPaymentDAO accPaymentDAOobj, JSONObject request, AccountingHandlerDAO accountingHandlerDAOobj, accCreditNoteDAO accCreditNoteDAOobj, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj) {
        try {
            String companyid = request.optString("companyid");
            boolean ignoreZero = request.optString("ignorezero") != null ? Boolean.parseBoolean(request.optString("ignorezero")) : false;
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ 
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            int noOfInterval = request.has("noOfInterval") ? request.optInt("noOfInterval",7) : 7;
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(request.optString(Constants.companyKey), Constants.Acc_Credit_Note_ModuleId));
            if (accAccountDAOobj != null) {
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }
            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.optString(Constants.asOfDate) != null) {
                    try {
                        curDate = df.parse(request.optString(Constants.asOfDate));
                    } catch (Exception e) {
                        curDate = new Date(request.optString(Constants.asOfDate));
                    }
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            if (list != null) {
                for (Object object : list) {

                    double amountdue1 = 0;
                    double amountdue2 = 0;
                    double amountdue3 = 0;
                    double amountdue4 = 0;
                    double amountdue5 = 0;
                    double amountdue6 = 0;
                    double amountdue7 = 0;
                    double amountdue8 = 0;
                    double amountdue9 = 0;
                    double amountdue10 = 0;
                    double amountdue11 = 0;
//                    double accruedbalance = 0;

                    CreditNote cn = (CreditNote) object;

                    if (cn != null) {
                        Logger.getLogger(AccInvoiceServiceHandler.class.getName()).log(Level.INFO, "Opening CN=" + cn.getCreditNoteNumber());
                        double cnPaidAmount = 0;
                        requestParams.put("creditnoteid", cn.getID());

                        KwlReturnObject cnpResult = accPaymentDAOobj.getCreditNotePaymentDetails(requestParams);
                        if (!cnpResult.getEntityList().isEmpty()) {
                            Iterator cnpItr = cnpResult.getEntityList().iterator();
                            while (cnpItr.hasNext()) {
                                Object[] objects = (Object[]) cnpItr.next();
//                                double exchangeratefortransaction = objects[7] != null ? (Double) objects[7] : 1.0;
                                double cnPaidAmtInCNCurrency = objects[2] != null ? (Double) objects[2] : 0.0;
                                //cnPaidAmount += authHandler.round(cnPaidAmtInReceiptCurrency / exchangeratefortransaction, Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
                                cnPaidAmount += authHandler.round(cnPaidAmtInCNCurrency, companyid);
                            }
                        }

                        double invReturnAmt = 0;
                        KwlReturnObject result = accCreditNoteDAOobj.getCNRowsFromInvoice(requestParams);
                        List<CreditNoteDetail> rows = result.getEntityList();
                        for (CreditNoteDetail detail : rows) {
                            Discount disc = detail.getDiscount();
                            if (disc != null) {
                                invReturnAmt += disc.getDiscountValue();
                            }
                        }
                        double amountdue = cn.getCnamount() - (cnPaidAmount + invReturnAmt);

                        if (amountdue > 0 || !ignoreZero) {
                            com.krawler.utils.json.base.JSONObject invoiceJson = new com.krawler.utils.json.base.JSONObject();
                            Date creationDate = cn.getCreationDate();
                            amountdue = -amountdue;//amount due will be negative for credit note
                            double exchangeRateForOtherCurrency = cn.getExchangeRateForOpeningTransaction();
                            boolean isopeningBalanceTransaction = cn.isIsOpeningBalenceCN();
                            boolean isConversionRateFromCurrencyToBase = cn.isConversionRateFromCurrencyToBase();

                            invoiceJson.put(InvoiceConstants.billid, cn.getID());
                            invoiceJson.put(InvoiceConstants.billno, cn.getCreditNoteNumber());
                            invoiceJson.put(InvoiceConstants.journalentryid, cn.getJournalEntry() == null ? "" : cn.getJournalEntry().getID());
                            invoiceJson.put(InvoiceConstants.withoutinventory, false);
                            invoiceJson.put(InvoiceConstants.currencysymbol, cn.getCurrency() == null ? "" : cn.getCurrency().getSymbol());
                            invoiceJson.put(InvoiceConstants.currencyid, (cn.getCurrency() == null ? "" : cn.getCurrency().getCurrencyID()));
                            invoiceJson.put(InvoiceConstants.currencyname, (cn.getCurrency() == null ? "" : cn.getCurrency().getName()));
                            invoiceJson.put(InvoiceConstants.entryno, "");
                            invoiceJson.put(InvoiceConstants.personid, cn.getCustomer() == null ? "" : cn.getCustomer().getID());
                            invoiceJson.put(InvoiceConstants.personname, cn.getCustomer() == null ? "" : cn.getCustomer().getName());
                            invoiceJson.put(InvoiceConstants.personinfo, cn.getCustomer() == null ? "" : cn.getCustomer().getName()+"("+cn.getCustomer().getAcccode()+")");
                            invoiceJson.put(InvoiceConstants.aliasname, cn.getCustomer() == null ? "" : cn.getCustomer().getAliasname());
                            invoiceJson.put(InvoiceConstants.duedate, df.format(creationDate));
                            invoiceJson.put(InvoiceConstants.date, df.format(creationDate));
                            invoiceJson.put(InvoiceConstants.memo, cn.getMemo() == null ? "" : cn.getMemo());
                            invoiceJson.put(InvoiceConstants.deleted, cn.isDeleted());
                            invoiceJson.put(InvoiceConstants.externalcurrencyrate, exchangeRateForOtherCurrency);
                            invoiceJson.put("isOpeningBalanceTransaction", isopeningBalanceTransaction);
                            invoiceJson.put(InvoiceConstants.isConversionRateFromCurrencyToBase, isConversionRateFromCurrencyToBase);
                            invoiceJson.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + ((isopeningBalanceTransaction && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency ) + " " + (cn.getCurrency() == null ? "" : cn.getCurrency().getCurrencyCode()));
                            KwlReturnObject bAmt = null;
                            double amountdueinbase = 0d;
                            if (isopeningBalanceTransaction && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, cn.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cn.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            }
                            amountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            invoiceJson.put(InvoiceConstants.amountdue, amountdue);
                            invoiceJson.put(InvoiceConstants.amountdueinbase, amountdueinbase);
                            invoiceJson.put("amountinbase", cn.getOriginalOpeningBalanceBaseAmount());
                            invoiceJson.put("code", cn.getCustomer() != null ? cn.getCustomer().getAcccode() : "");

                            invoiceJson.put("type", "Credit Note");

                            Date dueDate = df.parse(df.format(creationDate));

//                            if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                accruedbalance = authHandler.round(amountdue, companyid);
//                            } else 
                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }

                            switch(noOfInterval){
                                case 2:
                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 3:
                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 4:
                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 5:
                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 6:
                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 7:
                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 8:
                                    amountdue9 += amountdue10 + amountdue11;
                                    amountdue10 = amountdue11 = 0;
                                    break;
                                case 9:
                                    amountdue10 += amountdue11;
                                    amountdue11 = 0;
                                    break;
                            }
                            
                            invoiceJson.put(InvoiceConstants.amountdue1, amountdue1);
                            invoiceJson.put(InvoiceConstants.amountdue2, amountdue2);
                            invoiceJson.put(InvoiceConstants.amountdue3, amountdue3);
                            invoiceJson.put(InvoiceConstants.amountdue4, amountdue4);
                            invoiceJson.put(InvoiceConstants.amountdue5, amountdue5);
                            invoiceJson.put(InvoiceConstants.amountdue6, amountdue6);
                            invoiceJson.put(InvoiceConstants.amountdue7, amountdue7);
                            invoiceJson.put(InvoiceConstants.amountdue8, amountdue8);
                            invoiceJson.put(InvoiceConstants.amountdue9, amountdue9);
                            invoiceJson.put(InvoiceConstants.amountdue10, amountdue10);
                            invoiceJson.put(InvoiceConstants.amountdue11, amountdue11);
//                            invoiceJson.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
                            
                            if (accJournalEntryobj != null && fieldDataManagercntrl != null) {
                                Map<String, Object> variableMap = new HashMap<String, Object>();
                                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                                Detailfilter_names.add("companyid");
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
                                    params.put("companyid", "");
                                    boolean isExport = request.optBoolean("isExport",false);
                                    if (!isExport) {
                                        isExport = request.optBoolean("isAged",false);
                                    }
                                    params.put("isExport", isExport);
                                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, invoiceJson, params);
                                }
                            }

                            dataArray.put(invoiceJson);
                        }
                    }
                }
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }
    
    public static JSONArray getCreditNotesMergedJson(HashMap<String, Object> requestParams, List list, JSONArray JArr, AccountingHandlerDAO accountingHandlerDAOobj, authHandlerDAO authHandlerDAOObj, accCurrencyDAO accCurrencyDAOobj, accPaymentDAO accPaymentDAOobj, HttpServletRequest request, accCreditNoteDAO accCreditNoteDAOobj, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj) throws ServiceException {
        try {
            boolean ignoreZero = request.getParameter("ignorezero") != null ? Boolean.parseBoolean(request.getParameter("ignorezero")) : false;
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get(Constants.companyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ //Handle Date Parse exception. ERP-33531
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }
            }
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            int duration = (requestParams.containsKey(InvoiceConstants.duration) && requestParams.containsKey(InvoiceConstants.duration)) ? Integer.parseInt(requestParams.get(InvoiceConstants.duration).toString()) : 30;
            boolean agedReport = (requestParams.containsKey("agedReport") && requestParams.get("agedReport") != null) ? Boolean.parseBoolean(requestParams.get("agedReport").toString()) : false;
            boolean isSOA = (requestParams.containsKey("isSOA") && requestParams.get("isSOA") != null) ? Boolean.parseBoolean(requestParams.get("isSOA").toString()) : false;
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();

            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Credit_Note_ModuleId));
            if (accAccountDAOobj != null) {
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }

            //Custom field details Maps for Line Level data
            HashMap<String, Object> fieldrequestParamsRows = new HashMap();
            HashMap<String, String> replaceFieldMapRows = new HashMap();
            HashMap<String, String> customFieldMapRows = new HashMap();
            HashMap<String, String> customDateFieldMapRows = new HashMap();
            fieldrequestParamsRows.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParamsRows.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Credit_Note_ModuleId));
            HashMap<String, Integer> fieldMapRows = null;
            if(accAccountDAOobj!=null){
                fieldMapRows = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParamsRows, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows);
            }

            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.getAttribute(Constants.asOfDate) != null) {
                    curDate = (Date) request.getAttribute(Constants.asOfDate);
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {

                double amountdue1 = 0;
                double amountdue2 = 0;
                double amountdue3 = 0;
                double amountdue4 = 0;
                double amountdue5 = 0;
                double amountdue6 = 0;
                double amountdue7 = 0;
                double amountdue8 = 0;
                double amountdue9 = 0;
                double amountdue10 = 0;
                double amountdue11 = 0;
//                double accruedbalance = 0;

                Object[] row = (Object[]) itr.next();
                boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                String personid = "";
                String personname = "";
                String aliasname = "";
                String customerCreditTerm ="";
                String billto = "";
                String cncurrencyid = "";
                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                Customer customer = (Customer) resultObject.getEntityList().get(0);
                if (customer != null) {
                    personid = customer.getID();
                    personname = customer.getName();
                    aliasname = customer.getAliasname();
                    customerCreditTerm = customer.getCreditTerm() == null ? "" : customer.getCreditTerm().getTermname();

                    HashMap<String, Object> addressParams = new HashMap<String, Object>();
                    addressParams.put("companyid", companyid);
                    addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                    addressParams.put("isBillingAddress", true);    //true to get billing address
                    addressParams.put("customerid", customer.getID());
                    CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                    billto = customerAddressDetails != null ? customerAddressDetails.getAddress() : "";
                }

                resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                if (!withoutinventory) {
                    resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                    CreditNote creditNote = (CreditNote) resultObject.getEntityList().get(0);
                    if (creditNote != null) {
                        JournalEntry je = creditNote.getJournalEntry();
                        double externalCurrencyRate = je.getExternalCurrencyRate()==0?1:je.getExternalCurrencyRate();
                        cncurrencyid = creditNote.getCurrency() == null ? currency.getCurrencyID() : creditNote.getCurrency().getCurrencyID();
                        
                        Logger.getLogger(AccInvoiceServiceHandler.class.getName()).log(Level.INFO, "Normal CN=" + creditNote.getCreditNoteNumber());
                        requestParams.put("creditnoteid", creditNote.getID());
                        double amountdue = getCreditNoteAmountDue(creditNote, requestParams, accPaymentDAOobj, accCreditNoteDAOobj, companyid);
                        KwlReturnObject cnAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cncurrencyid, creditNote.getCreationDate(), je.getExternalCurrencyRate());
                        double amountdueinbase = authHandler.round((Double) cnAmtInBaseResult.getEntityList().get(0), companyid);
                        requestParams.remove("creditnoteid");//Removing creditnoteid after use, So that is does not affect other 
                        
                        if ((creditNote.isOtherwise() && amountdue > 0) || !ignoreZero || (creditNote.getCntype() == Constants.CreditNoteForOvercharge && amountdue > 0)) {
                            obj.put(InvoiceConstants.billid, creditNote.getID());
                            obj.put(InvoiceConstants.noteid, creditNote.getID());
                            obj.put(InvoiceConstants.noteno, creditNote.getCreditNoteNumber());
                            obj.put(InvoiceConstants.companyid, creditNote.getCompany().getCompanyID());
                            obj.put(InvoiceConstants.companyname, creditNote.getCompany().getCompanyName());
                            obj.put(InvoiceConstants.billno, creditNote.getCreditNoteNumber());
                            obj.put(InvoiceConstants.journalentryid, je.getID());
                            obj.put(InvoiceConstants.withoutinventory, withoutinventory);
                            obj.put(InvoiceConstants.currencysymbol, (creditNote.getCurrency() == null ? currency.getSymbol() : creditNote.getCurrency().getSymbol()));
                            obj.put(InvoiceConstants.currencyid, (creditNote.getCurrency() == null ? currency.getCurrencyID() : creditNote.getCurrency().getCurrencyID()));
                            obj.put(InvoiceConstants.currencyname, (creditNote.getCurrency() == null ? currency.getName() : creditNote.getCurrency().getName()));
                            obj.put(InvoiceConstants.entryno, je.getEntryNumber());
                            obj.put(InvoiceConstants.personid, personid);
                            obj.put(InvoiceConstants.personname, personname);
                            obj.put(InvoiceConstants.aliasname, aliasname);
                            obj.put(InvoiceConstants.CustomerCreditTerm, customerCreditTerm);
                            obj.put(InvoiceConstants.billto, billto);
                            if (agedReport || isSOA) {
                                obj.put(InvoiceConstants.amount, creditNote.isOtherwise() || creditNote.getCntype() == Constants.CreditNoteForOvercharge ? -creditNote.getCnamount() : details.getAmount());
                                obj.put(InvoiceConstants.amountdue, creditNote.isOtherwise() || creditNote.getCntype() == Constants.CreditNoteForOvercharge ? -amountdue : 0);
                                obj.put(InvoiceConstants.amountdueinbase, -amountdueinbase);//-authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), 2));
                            } else {
                                obj.put(InvoiceConstants.amount, creditNote.isOtherwise() || creditNote.getCntype() == Constants.CreditNoteForOvercharge ? creditNote.getCnamount() : details.getAmount());
                                obj.put(InvoiceConstants.amountdue, creditNote.isOtherwise() || creditNote.getCntype() == Constants.CreditNoteForOvercharge ? amountdue : 0);
                                obj.put(InvoiceConstants.amountdueinbase, amountdueinbase);//authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), 2));
                            }
//                            obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNote.getCnamount(), cncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNote.getCnamount(), cncurrencyid, creditNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            obj.put("code", customer != null ? customer.getAcccode() : "");
                            obj.put(InvoiceConstants.personinfo, customer != null ? personname+"("+customer.getAcccode()+")" : "");
                            obj.put("currencyidval", authHandlerDAOObj.getCurrency(currencyid));
//                            obj.put(InvoiceConstants.duedate, df.format(je.getEntryDate()));
//                            obj.put(InvoiceConstants.date, df.format(je.getEntryDate()));
                            obj.put(InvoiceConstants.duedate, df.format(creditNote.getCreationDate()));
                            obj.put(InvoiceConstants.date, df.format(creditNote.getCreationDate()));
                            obj.put(InvoiceConstants.memo, creditNote.getMemo());
                            obj.put(InvoiceConstants.deleted, creditNote.isDeleted());
                            obj.put(InvoiceConstants.externalcurrencyrate, externalCurrencyRate);
                            obj.put(Constants.SEQUENCEFORMATID, creditNote.getSeqformat() != null ? creditNote.getSeqformat().getID() : "");
                            obj.put("includingGST", creditNote.isIncludingGST());
                            obj.put("salesPersonID", creditNote.getSalesPerson() == null ? "" : creditNote.getSalesPerson().getID());
                            obj.put("salespersonname", creditNote.getSalesPerson() == null ? "" : creditNote.getSalesPerson().getValue());
                            obj.put("costcenterid", creditNote.getCostcenter() == null ? "" : creditNote.getCostcenter().getID());
                            obj.put("type", "Credit Note");
                            obj.put("isCN", true);
                            obj.put("cntype", creditNote.getCntype());
                            double customerCreditLimit = 0;
                            double customerCreditLimitInbase = 0;
                            String currencyId = "";
                            if (creditNote.getCustomer() != null && creditNote.getCustomer().getCurrency() != null) {
                                currencyId = creditNote.getCustomer().getCurrency().getCurrencyID();
                                customerCreditLimit = creditNote.getCustomer().getCreditlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, customerCreditLimit, currencyId, creditNote.getCustomer().getCreatedOn(), 0);
                                customerCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            }
                            obj.put("creditlimit", customerCreditLimit);
                            obj.put("creditlimitinbase", customerCreditLimitInbase);
                            obj.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + externalCurrencyRate + " " + (creditNote.getCurrency() == null ? "" : creditNote.getCurrency().getCurrencyCode()));
//                            Date dueDate = df.parse(df.format(je.getEntryDate()));
                            Date dueDate = df.parse(df.format(creditNote.getCreationDate()));
                            if (agedReport) {//aged report view case
                                amountdue = -amountdue;
                            } else if (isSOA) { //export SOA report case
//                                amountdue = -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid);
                                amountdue = -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cncurrencyid, creditNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid);
                            }

//                            if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                accruedbalance = authHandler.round(amountdue, companyid);
//                            } else
                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }

                            switch(noOfInterval){
                                case 2:
                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 3:
                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 4:
                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 5:
                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 6:
                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 7:
                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 8:
                                    amountdue9 += amountdue10 + amountdue11;
                                    amountdue10 = amountdue11 = 0;
                                    break;
                                case 9:
                                    amountdue10 += amountdue11;
                                    amountdue11 = 0;
                                    break;
                            }
                            
                            obj.put("amountdue1", amountdue1);
                            obj.put("amountdue2", amountdue2);
                            obj.put("amountdue3", amountdue3);
                            obj.put("amountdue4", amountdue4);
                            obj.put("amountdue5", amountdue5);
                            obj.put("amountdue6", amountdue6);
                            obj.put("amountdue7", amountdue7);
                            obj.put("amountdue8", amountdue8);
                            obj.put("amountdue9", amountdue9);
                            obj.put("amountdue10", amountdue10);
                            obj.put("amountdue11", amountdue11);
                            
//                            obj.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
                            if(!requestParams.containsKey("isAgedReceivables") || !(Boolean)requestParams.get("isAgedReceivables")){
                                getCreditNoteCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, accountingHandlerDAOobj, accCreditNoteDAOobj, request, creditNote, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows, fieldMapRows, replaceFieldMap, customFieldMap, customDateFieldMap, FieldMap, obj);
                            }
                            JArr.put(obj);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }
    
    public static double getCreditNoteAmountDue(CreditNote creditNote, HashMap<String, Object> requestParams, accPaymentDAO accPaymentDAOobj, accCreditNoteDAO accCreditNoteDAOobj, String companyid) throws ServiceException{
        double cnPaidAmount = 0;
        double invReturnAmt = 0;
        double dnReturnAmt = 0;
        KwlReturnObject cnpResult = accPaymentDAOobj.getCreditNotePaymentDetails(requestParams);
        if (!cnpResult.getEntityList().isEmpty()) {
            Iterator cnpItr = cnpResult.getEntityList().iterator();
            while (cnpItr.hasNext()) {
                Object[] objects = (Object[]) cnpItr.next();
                double exchangeratefortransaction = objects[0] != null ? (Double) objects[0] : 1.0;
                double cnPaidAmtInReceiptCurrency = objects[1] != null ? (Double) objects[1] : 0.0;
                cnPaidAmount += authHandler.round(cnPaidAmtInReceiptCurrency / exchangeratefortransaction, companyid);
            }
        }
        KwlReturnObject result = accCreditNoteDAOobj.getCNRowsFromInvoice(requestParams);
        List<CreditNoteDetail> rows = result.getEntityList();
        for (CreditNoteDetail detail : rows) {
            Discount disc = detail.getDiscount();
            if (disc != null) {
                invReturnAmt += authHandler.round(disc.getDiscountValue(), companyid);
            }
        }
        /*
         * Inlcuding Credit Note amount Linked in Debit Note
         */
        result = accCreditNoteDAOobj.getCNRowsFromDebitNote(requestParams);
        rows = result.getEntityList();
        for (CreditNoteDetail detail : rows) {
            Discount disc = detail.getDiscount();
            if (disc != null) {
                dnReturnAmt += disc.getDiscountValue();
            }
        }
        double amountdue = authHandler.round(creditNote.getCnamount() - (cnPaidAmount + +authHandler.round(invReturnAmt, companyid) + dnReturnAmt), companyid);
        return amountdue;
    }
    
    public static void getCreditNoteCustomField(String companyid, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj, AccountingHandlerDAO accountingHandlerDAOobj, accCreditNoteDAO accCreditNoteDAOobj, HttpServletRequest request, CreditNote creditNote, HashMap<String, String> replaceFieldMapRows, HashMap<String, String> customFieldMapRows, HashMap<String, String> customDateFieldMapRows, HashMap<String, Integer> fieldMapRows, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, Integer> FieldMap, com.krawler.utils.json.base.JSONObject obj ) throws ServiceException, JSONException{
                            if (fieldDataManagercntrl != null && accJournalEntryobj != null) {
                                Map<String, Object> variableMap = new HashMap<String, Object>();
                                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                                Detailfilter_names.add("companyid");
                                Detailfilter_params.add(companyid);
                                Detailfilter_names.add("journalentryId");
                                Detailfilter_params.add(creditNote.getJournalEntry().getID());
                                Detailfilter_names.add("moduleId");
                                Detailfilter_params.add(Constants.Acc_Credit_Note_ModuleId + "");
                                invDetailRequestParams.put("filter_names", Detailfilter_names);
                                invDetailRequestParams.put("filter_params", Detailfilter_params);
                                KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomDataNew(invDetailRequestParams);
                                if (idcustresult.getEntityList().size() > 0) {
                                    String journalentryId = idcustresult.getEntityList().get(0).toString();
                                    KwlReturnObject jeCustomResult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalentryId);
                                    AccJECustomData jeCustom = (AccJECustomData) jeCustomResult.getEntityList().get(0);
                                    AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                                    JSONObject params = new JSONObject();
                                    boolean isExport = (boolean) ((request.getAttribute("isExport") == null) ? false : request.getAttribute("isExport"));
                                    if (!isExport) {
                                        isExport = (request.getParameter("isAged") == null) ? false : Boolean.parseBoolean(request.getParameter("isAged"));
                                    }
                                    params.put("isExport", isExport);
                                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                                }
                            }
                            Set<JournalEntryDetail> jedDetails = creditNote.getJournalEntry() != null ? creditNote.getJournalEntry().getDetails() : new HashSet(0);
                            if (jedDetails != null && !jedDetails.isEmpty()) {// In aged Report we are showing line level item dimension at global level by comma seperated. below code is for that
                                Map<String, List<Object>> linelabelDataMap = new LinkedHashMap();
                                for (JournalEntryDetail jedetailrow : jedDetails) {
                                    JSONObject customObject = new JSONObject();
                                    Map<String, Object> variableMapRows = new HashMap<String, Object>();
                                    HashMap<String, Object> invDetailsRequestParams = new HashMap<String, Object>();
                                    ArrayList Detailfilter_names = new ArrayList();
                                    ArrayList Detailfilter_params = new ArrayList();
                                    Detailfilter_names.add(Constants.Acc_jedetailId);
                                    Detailfilter_params.add(jedetailrow.getID());
                                    invDetailsRequestParams.put(Constants.filterNamesKey, Detailfilter_names);
                                    invDetailsRequestParams.put(Constants.filterParamsKey, Detailfilter_params);
                                    KwlReturnObject idcustdetailresult = accCreditNoteDAOobj.geCreditNoteCustomData(invDetailsRequestParams);
                                    if (idcustdetailresult.getEntityList().size() > 0) {
                                        AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustdetailresult.getEntityList().get(0);
                                        AccountingManager.setCustomColumnValues(jeDetailCustom, fieldMapRows, replaceFieldMapRows, variableMapRows);
                                        if (jeDetailCustom != null) {
                                            JSONObject params = new JSONObject();
                                            params.put(Constants.isForReport, true);
                                            fieldDataManagercntrl.getLineLevelCustomData(variableMapRows, customFieldMapRows, customDateFieldMapRows, customObject, params);
                                            if (customObject.length() > 0) {
                                                Iterator<String> keys = customObject.keys();
                                                while (keys.hasNext()) {
                                                    String key = (String) keys.next();
                                                    if (!key.equals("allCustomFieldKeyValuePairString")) {// no need to go for key allCustomFieldKeyValuePairString
                                                        if (linelabelDataMap.containsKey(key)) {
                                                            if (!linelabelDataMap.get(key).contains(customObject.get(key))) {
                                                                linelabelDataMap.get(key).add(customObject.get(key));
                                                            }
                                                        } else {
                                                            List<Object> dataList = new ArrayList<>();
                                                            dataList.add(customObject.get(key));
                                                            linelabelDataMap.put(key, dataList);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                for (Map.Entry<String, List<Object>> entry : linelabelDataMap.entrySet()) {
                                    String commaSeperatedValue = "";
                                    String key = entry.getKey();
                                    List<Object> data = entry.getValue();
                                    for (Object dataObj : data) {
                                        if (dataObj != null) {
                                            if (StringUtil.isNullOrEmpty(commaSeperatedValue)) {
                                                commaSeperatedValue = dataObj.toString();
                                            } else {
                                                commaSeperatedValue += "," + dataObj.toString();
                                            }
                                        }
                                    }
                                    obj.put(key, commaSeperatedValue);
                                }
                            }
                        }

    public static JSONArray getCreditNotesMergedJson(HashMap<String, Object> requestParams, List list, JSONArray JArr, AccountingHandlerDAO accountingHandlerDAOobj, authHandlerDAO authHandlerDAOObj, accCurrencyDAO accCurrencyDAOobj, accPaymentDAO accPaymentDAOobj, JSONObject request, accCreditNoteDAO accCreditNoteDAOobj, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj) throws ServiceException {
        try {
            boolean ignoreZero = request.optString("ignorezero") != null ? Boolean.parseBoolean(request.optString("ignorezero")) : false;
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get(Constants.companyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
           if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ 
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            int noOfInterval = request.has("noOfInterval") ? request.optInt("noOfInterval",7) : 7;
            int duration = (requestParams.containsKey(InvoiceConstants.duration) && requestParams.containsKey(InvoiceConstants.duration)) ? Integer.parseInt(requestParams.get(InvoiceConstants.duration).toString()) : 30;
            boolean agedReport = (requestParams.containsKey("agedReport") && requestParams.get("agedReport") != null) ? Boolean.parseBoolean(requestParams.get("agedReport").toString()) : false;
            boolean isSOA = (requestParams.containsKey("isSOA") && requestParams.get("isSOA") != null) ? Boolean.parseBoolean(requestParams.get("isSOA").toString()) : false;
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();

            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(request.optString(Constants.companyKey), Constants.Acc_Credit_Note_ModuleId));
            if (accAccountDAOobj != null) {
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }

            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.optString(Constants.asOfDate) != null) {
                    try {
                        curDate = df.parse(request.optString(Constants.asOfDate));
                    } catch (Exception e) {
                        curDate = new Date(request.optString(Constants.asOfDate));
                    }
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {

                double amountdue1 = 0;
                double amountdue2 = 0;
                double amountdue3 = 0;
                double amountdue4 = 0;
                double amountdue5 = 0;
                double amountdue6 = 0;
                double amountdue7 = 0;
                double amountdue8 = 0;
                double amountdue9 = 0;
                double amountdue10 = 0;
                double amountdue11 = 0;
//                double accruedbalance = 0;

                Object[] row = (Object[]) itr.next();
                boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                String personid = "";
                String personname = "";
                String aliasname = "";
                String billto = "";
                String cncurrencyid = "";
                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                Customer customer = (Customer) resultObject.getEntityList().get(0);
                personid = customer != null ? customer.getID() : "";
                personname = customer != null ? customer.getName() : "";
                aliasname = customer != null ? customer.getAliasname() : "";
                if (customer != null) {
                    HashMap<String, Object> addressParams = new HashMap<String, Object>();
                    addressParams.put("companyid", companyid);
                    addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                    addressParams.put("isBillingAddress", true);    //true to get billing address
                    addressParams.put("customerid", customer.getID());
                    CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                    billto = customerAddressDetails != null ? customerAddressDetails.getAddress() : "";
                }

                resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                if (!withoutinventory) {
                    resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                    CreditNote creditNote = (CreditNote) resultObject.getEntityList().get(0);
                    if (creditNote != null) {
                        JournalEntry je = creditNote.getJournalEntry();
                        double externalCurrencyRate = je.getExternalCurrencyRate()==0?1:je.getExternalCurrencyRate();
                        cncurrencyid = creditNote.getCurrency() == null ? currency.getCurrencyID() : creditNote.getCurrency().getCurrencyID();
                        List<Double> knockedOffAmountList = new ArrayList();//variable used to hold knocked off amounts in invoice currency
                        Logger.getLogger(AccInvoiceServiceHandler.class.getName()).log(Level.INFO, "Normal CN=" + creditNote.getCreditNoteNumber());
                        double cnPaidAmount = 0;
                        double invReturnAmt = 0;
                        double dnReturnAmt = 0;
                        requestParams.put("creditnoteid", creditNote.getID());
                        KwlReturnObject cnpResult = accPaymentDAOobj.getCreditNotePaymentDetails(requestParams);
                        if (!cnpResult.getEntityList().isEmpty()) {
                            Iterator cnpItr = cnpResult.getEntityList().iterator();
                            while (cnpItr.hasNext()) {
                                Object[] objects = (Object[]) cnpItr.next();
                                double exchangeratefortransaction = objects[0] != null ? (Double) objects[0] : 1.0;
                                double cnPaidAmtInReceiptCurrency = objects[1] != null ? (Double) objects[1] : 0.0;
                                cnPaidAmount += authHandler.round(cnPaidAmtInReceiptCurrency / exchangeratefortransaction, companyid);
                            }
                        }
                        knockedOffAmountList.add(cnPaidAmount);
                        KwlReturnObject result = accCreditNoteDAOobj.getCNRowsFromInvoice(requestParams);
                        List<CreditNoteDetail> rows = result.getEntityList();
                        for (CreditNoteDetail detail : rows) {
                            Discount disc = detail.getDiscount();
                            if (disc != null) {
                                   invReturnAmt +=authHandler.round(disc.getDiscountValue(),companyid);
                            }
                        }
                        knockedOffAmountList.add(invReturnAmt);
                        /*
                         * Inlcuding Credit Note amount Linked in Debit Note
                         */
                        result = accCreditNoteDAOobj.getCNRowsFromDebitNote(requestParams);
                        rows = result.getEntityList();
                        for (CreditNoteDetail detail : rows) {
                            Discount disc = detail.getDiscount();
                            if (disc != null) {
                                dnReturnAmt += disc.getDiscountValue();
                            }
                        }
                        knockedOffAmountList.add(dnReturnAmt);
                        double knockedOffAmtInBase = 0;
                        for (double knockedOffAmount : knockedOffAmountList) {
                            if (knockedOffAmount != 0) {
                                KwlReturnObject dnAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, knockedOffAmount, cncurrencyid, creditNote.getCreationDate(), externalCurrencyRate);
                                //Doing round off each value before summing for matching aged amount with balance sheet in base currency 
                                knockedOffAmtInBase += authHandler.round((Double) dnAmtInBaseResult.getEntityList().get(0), companyid);
                            }
                        }
                        double amountdueinbase = authHandler.round(creditNote.getCnamountinbase() - knockedOffAmtInBase, companyid);
                        double amountdue = creditNote.getCnamount() - (cnPaidAmount + + authHandler.round(invReturnAmt,companyid)+dnReturnAmt);

                        if ((creditNote.isOtherwise() && amountdue > 0) || !ignoreZero || (creditNote.getCntype() == Constants.CreditNoteForOvercharge && amountdue > 0)) {
                            obj.put(InvoiceConstants.billid, creditNote.getID());
                            obj.put(InvoiceConstants.noteid, creditNote.getID());
                            obj.put(InvoiceConstants.noteno, creditNote.getCreditNoteNumber());
                            obj.put(InvoiceConstants.companyid, creditNote.getCompany().getCompanyID());
                            obj.put(InvoiceConstants.companyname, creditNote.getCompany().getCompanyName());
                            obj.put(InvoiceConstants.billno, creditNote.getCreditNoteNumber());
                            obj.put(InvoiceConstants.journalentryid, je.getID());
                            obj.put(InvoiceConstants.withoutinventory, withoutinventory);
                            obj.put(InvoiceConstants.currencysymbol, (creditNote.getCurrency() == null ? currency.getSymbol() : creditNote.getCurrency().getSymbol()));
                            obj.put(InvoiceConstants.currencyid, (creditNote.getCurrency() == null ? currency.getCurrencyID() : creditNote.getCurrency().getCurrencyID()));
                            obj.put(InvoiceConstants.currencyname, (creditNote.getCurrency() == null ? currency.getName() : creditNote.getCurrency().getName()));
                            obj.put(InvoiceConstants.entryno, je.getEntryNumber());
                            obj.put(InvoiceConstants.personid, personid);
                            obj.put(InvoiceConstants.personname, personname);
                            obj.put(InvoiceConstants.aliasname, aliasname);
                            obj.put(InvoiceConstants.billto, billto);
                            if (agedReport || isSOA) {
                                obj.put(InvoiceConstants.amount, creditNote.isOtherwise() || creditNote.getCntype() == Constants.CreditNoteForOvercharge ? -creditNote.getCnamount() : details.getAmount());
                                obj.put(InvoiceConstants.amountdue, creditNote.isOtherwise() || creditNote.getCntype() == Constants.CreditNoteForOvercharge ? -amountdue : 0);
                                obj.put(InvoiceConstants.amountdueinbase, -amountdueinbase);//-authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), 2));
                            } else {
                                obj.put(InvoiceConstants.amount, creditNote.isOtherwise() || creditNote.getCntype() == Constants.CreditNoteForOvercharge ? creditNote.getCnamount() : details.getAmount());
                                obj.put(InvoiceConstants.amountdue, creditNote.isOtherwise() || creditNote.getCntype() == Constants.CreditNoteForOvercharge ? amountdue : 0);
                                obj.put(InvoiceConstants.amountdueinbase, amountdueinbase);//authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), 2));
                            }
//                            obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNote.getCnamount(), cncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNote.getCnamount(), cncurrencyid, creditNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            obj.put("code", customer != null ? customer.getAcccode() : "");
                            obj.put(InvoiceConstants.personinfo, customer != null ? personname+"("+customer.getAcccode()+")" : "");
                            obj.put("currencyidval", authHandlerDAOObj.getCurrency(currencyid));
//                            obj.put(InvoiceConstants.duedate, df.format(je.getEntryDate()));
//                            obj.put(InvoiceConstants.date, df.format(je.getEntryDate()));
                            obj.put(InvoiceConstants.duedate, df.format(creditNote.getCreationDate()));
                            obj.put(InvoiceConstants.date, df.format(creditNote.getCreationDate()));
                            obj.put(InvoiceConstants.memo, creditNote.getMemo());
                            obj.put(InvoiceConstants.deleted, creditNote.isDeleted());
                            obj.put(InvoiceConstants.externalcurrencyrate, externalCurrencyRate);
                            obj.put(Constants.SEQUENCEFORMATID, creditNote.getSeqformat() != null ? creditNote.getSeqformat().getID() : "");
                            obj.put("includingGST", creditNote.isIncludingGST());
                            obj.put("salesPersonID", creditNote.getSalesPerson() == null ? "" : creditNote.getSalesPerson().getID());
                            obj.put("costcenterid", creditNote.getCostcenter() == null ? "" : creditNote.getCostcenter().getID());
                            obj.put("type", "Credit Note");
                            obj.put("isCN", true);
                            obj.put("cntype", creditNote.getCntype());
                            obj.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + externalCurrencyRate + " " + (creditNote.getCurrency() == null ? "" : creditNote.getCurrency().getCurrencyCode()));
//                            Date dueDate = df.parse(df.format(je.getEntryDate()));
                            Date dueDate = df.parse(df.format(creditNote.getCreationDate()));
                            if (agedReport) {//aged report view case
                                amountdue = -amountdue;
                            } else if (isSOA) { //export SOA report case
                                amountdue = -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cncurrencyid, creditNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid);
                            }

//                            if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                accruedbalance = authHandler.round(amountdue, companyid);
//                            } else 
                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }

                            switch(noOfInterval){
                                case 2:
                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 3:
                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 4:
                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 5:
                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 6:
                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 7:
                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 8:
                                    amountdue9 += amountdue10 + amountdue11;
                                    amountdue10 = amountdue11 = 0;
                                    break;
                                case 9:
                                    amountdue10 += amountdue11;
                                    amountdue11 = 0;
                                    break;
                            }
                            
                            obj.put("amountdue1", amountdue1);
                            obj.put("amountdue2", amountdue2);
                            obj.put("amountdue3", amountdue3);
                            obj.put("amountdue4", amountdue4);
                            obj.put("amountdue5", amountdue5);
                            obj.put("amountdue6", amountdue6);
                            obj.put("amountdue7", amountdue7);
                            obj.put("amountdue8", amountdue8);
                            obj.put("amountdue9", amountdue9);
                            obj.put("amountdue10", amountdue10);
                            obj.put("amountdue11", amountdue11);
//                            obj.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
                            
                            if (fieldDataManagercntrl != null && accJournalEntryobj != null) {
                                Map<String, Object> variableMap = new HashMap<String, Object>();
                                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                                Detailfilter_names.add("companyid");
                                Detailfilter_params.add(creditNote.getCompany().getCompanyID());
                                Detailfilter_names.add("journalentryId");
                                Detailfilter_params.add(creditNote.getJournalEntry().getID());
                                Detailfilter_names.add("moduleId");
                                Detailfilter_params.add(Constants.Acc_Credit_Note_ModuleId + "");
                                invDetailRequestParams.put("filter_names", Detailfilter_names);
                                invDetailRequestParams.put("filter_params", Detailfilter_params);
                                KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomDataNew(invDetailRequestParams);
                                if (idcustresult.getEntityList().size() > 0) {
                                    String journalentryId = idcustresult.getEntityList().get(0).toString();
                                    KwlReturnObject jeCustomResult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalentryId);
                                    AccJECustomData jeCustom = (AccJECustomData) jeCustomResult.getEntityList().get(0);
                                    AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                                    JSONObject params = new JSONObject();
                                    boolean isExport =   request.optBoolean("isExport",false);
                                    if (!isExport) {
                                        isExport = request.optBoolean("isAged",false);
                                    }
                                    params.put("isExport", isExport);
                                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                                }
                            }

                            JArr.put(obj);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }

    public static JSONArray getDebitNotesMergedJson(HashMap<String, Object> requestParams, List list, JSONArray JArr, AccountingHandlerDAO accountingHandlerDAOobj, authHandlerDAO authHandlerDAOObj, accCurrencyDAO accCurrencyDAOobj, accReceiptDAO accReceiptDao, HttpServletRequest request, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj,accCreditNoteDAO accCreditNoteDAOobj) throws ServiceException {
        try {
            boolean ignoreZero = request.getParameter("ignorezero") != null ? Boolean.parseBoolean(request.getParameter("ignorezero")) : false;
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get(Constants.companyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ //Handle Date Parse exception. ERP-33531
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }
            }
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            int duration = (requestParams.containsKey(InvoiceConstants.duration) && requestParams.containsKey(InvoiceConstants.duration)) ? Integer.parseInt(requestParams.get(InvoiceConstants.duration).toString()) : 30;
            boolean isSOA = (requestParams.containsKey("isSOA") && requestParams.get("isSOA") != null) ? Boolean.parseBoolean(requestParams.get("isSOA").toString()) : false;
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();

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
            
            //Custom field details Maps for Line Level data
            HashMap<String, Object> fieldrequestParamsRows = new HashMap();
            HashMap<String, String> replaceFieldMapRows = new HashMap();
            HashMap<String, String> customFieldMapRows = new HashMap();
            HashMap<String, String> customDateFieldMapRows = new HashMap();
            fieldrequestParamsRows.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParamsRows.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Debit_Note_ModuleId));
            HashMap<String, Integer> fieldMapRows = null;
            if (accAccountDAOobj != null) {
                fieldMapRows = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParamsRows, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows);
            }
            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.getAttribute(Constants.asOfDate) != null) {
                    curDate = (Date) request.getAttribute(Constants.asOfDate);
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {

                double amountdue1 = 0;
                double amountdue2 = 0;
                double amountdue3 = 0;
                double amountdue4 = 0;
                double amountdue5 = 0;
                double amountdue6 = 0;
                double amountdue7 = 0;
                double amountdue8 = 0;
                double amountdue9 = 0;
                double amountdue10 = 0;
                double amountdue11 = 0;
//                double accruedbalance = 0;

                Object[] row = (Object[]) itr.next();
                boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                String personid = "";
                String personname = "";
                String aliasname = "";
                String billto = "";
                String dncurrencyid = "";
                String customercreditterm = "";

                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                Customer customer = (Customer) resultObject.getEntityList().get(0);
                if (customer != null) {
                    personid = customer.getID();
                    personname = customer.getName();
                    aliasname = customer.getAliasname();
                    customercreditterm = customer.getCreditTerm()==null?"":customer.getCreditTerm().getTermname();
                    HashMap<String, Object> addressParams = new HashMap<String, Object>();
                    addressParams.put("companyid", companyid);
                    addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                    addressParams.put("isBillingAddress", true);    //true to get billing address
                    addressParams.put("customerid", personid);
                    CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                    billto = customerAddressDetails != null ? customerAddressDetails.getAddress() : "";
                }

                resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                if (!withoutinventory) {
                    resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);
                    DebitNote debitNote = (DebitNote) resultObject.getEntityList().get(0);
                    if (debitNote != null) {
                        Logger.getLogger(AccInvoiceServiceHandler.class.getName()).log(Level.INFO, "Normal DN=" + debitNote.getDebitNoteNumber());
                        requestParams.put("debitnoteid", debitNote.getID());
                        double amountdue = getDebitNoteAmountDue(debitNote, requestParams, accReceiptDao, accCreditNoteDAOobj, companyid);
                        requestParams.remove("debitnoteid");//Removing debitnoteid after use, So that is does not affect other 

                        if (((debitNote.isOtherwise() || debitNote.getDntype()==5) && amountdue > 0) || !ignoreZero) {  //debitMemo.getDntype()==5 In case of debit note against customer for malaysian country
                            JournalEntry je = debitNote.getJournalEntry();
                            dncurrencyid = debitNote.getCurrency() == null ? currency.getCurrencyID() : debitNote.getCurrency().getCurrencyID();
                            double externalCurrencyRate = je.getExternalCurrencyRate()==0?1:je.getExternalCurrencyRate();
                            obj.put(InvoiceConstants.billid, debitNote.getID());
                            obj.put(InvoiceConstants.noteid, debitNote.getID());
                            obj.put(InvoiceConstants.noteno, debitNote.getDebitNoteNumber());
                            obj.put(InvoiceConstants.companyid, debitNote.getCompany().getCompanyID());
                            obj.put(InvoiceConstants.companyname, debitNote.getCompany().getCompanyName());
                            obj.put(InvoiceConstants.billno, debitNote.getDebitNoteNumber());
                            obj.put(InvoiceConstants.journalentryid, je.getID());
                            obj.put(InvoiceConstants.withoutinventory, withoutinventory);
                            obj.put(InvoiceConstants.currencysymbol, (debitNote.getCurrency() == null ? currency.getSymbol() : debitNote.getCurrency().getSymbol()));
                            obj.put(InvoiceConstants.currencyid, (debitNote.getCurrency() == null ? currency.getCurrencyID() : debitNote.getCurrency().getCurrencyID()));
                            obj.put(InvoiceConstants.currencyname, (debitNote.getCurrency() == null ? currency.getName() : debitNote.getCurrency().getName()));
                            obj.put(InvoiceConstants.entryno, je.getEntryNumber());
                            obj.put(InvoiceConstants.personid, personid);
                            obj.put(InvoiceConstants.personname, personname);
                            obj.put(InvoiceConstants.aliasname, aliasname);
                            obj.put(InvoiceConstants.CustomerCreditTerm, customercreditterm);
                            double customerCreditLimit = 0;
                            double customerCreditLimitInbase = 0;
                            String currencyId = "";
                            if (debitNote.getCustomer() != null && debitNote.getCustomer().getCurrency() != null) {
                                currencyId = debitNote.getCustomer().getCurrency().getCurrencyID();
                                customerCreditLimit = debitNote.getCustomer().getCreditlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, customerCreditLimit, currencyId, debitNote.getCustomer().getCreatedOn(), 0);
                                customerCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            }
                            obj.put("creditlimit", customerCreditLimit);
                            obj.put("creditlimitinbase", customerCreditLimitInbase);
                            obj.put(InvoiceConstants.billto, billto);
                            obj.put(InvoiceConstants.amount, debitNote.isOtherwise() ? debitNote.getDnamount() : details.getAmount());
                            if (debitNote.getDntype() == 5) {
                                obj.put(InvoiceConstants.amountdue, details.getAmount());
                                obj.put(InvoiceConstants.amountduenonnegative, details.getAmount());
//                                obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, details.getAmount(), dncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
//                                obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, details.getAmount(), dncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                                obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, details.getAmount(), dncurrencyid, debitNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                                obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, details.getAmount(), dncurrencyid, debitNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                                if (debitNote.getTax() != null) {
                                    obj.put("gTaxId", debitNote.getTax().getID());
                                }
                                boolean checkTax = false;
                                Set<DebitNoteAgainstCustomerGst> cndetailsGst = (Set<DebitNoteAgainstCustomerGst>) debitNote.getRowsGst();
                                for (DebitNoteAgainstCustomerGst noteDetail : cndetailsGst) {
                                    if (noteDetail.getTax() != null) {
                                        checkTax = true;
                                        break;
                                    }
                                }
                                obj.put("includeprotax", checkTax);
                                obj.put("lasteditedby", debitNote.getModifiedby() == null ? "" : (debitNote.getModifiedby().getFirstName() + " " + debitNote.getModifiedby().getLastName()));
                                
                            } else {
                                obj.put(InvoiceConstants.amountdue, debitNote.isOtherwise() ? amountdue : 0);
                                obj.put(InvoiceConstants.amountduenonnegative, debitNote.isOtherwise() ? amountdue : 0);
//                                obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                                obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dncurrencyid, debitNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
//                                obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, debitNote.getDnamount(), dncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                                obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, debitNote.getDnamount(), dncurrencyid, debitNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            }
                           
                            obj.put("code", customer != null ? customer.getAcccode() : "");
                            obj.put(InvoiceConstants.personinfo, customer != null ? personname+"("+customer.getAcccode()+")" : "");
//                            obj.put(InvoiceConstants.duedate, df.format(je.getEntryDate()));
//                            obj.put(InvoiceConstants.date, df.format(je.getEntryDate()));
                            obj.put(InvoiceConstants.duedate, df.format(debitNote.getCreationDate()));
                            obj.put(InvoiceConstants.date, df.format(debitNote.getCreationDate()));
                            obj.put(InvoiceConstants.memo, debitNote.getMemo());
                            obj.put(InvoiceConstants.deleted, debitNote.isDeleted());
                            obj.put(InvoiceConstants.externalcurrencyrate, externalCurrencyRate);
                            obj.put(Constants.SEQUENCEFORMATID, debitNote.getSeqformat() != null ? debitNote.getSeqformat().getID() : "");
                            obj.put("includingGST", debitNote.isIncludingGST());
                            obj.put("costcenterid", debitNote.getCostcenter() == null ? "" : debitNote.getCostcenter().getID());
                            obj.put("type", "Debit Note");
                            obj.put("isDN", true);
                            obj.put("currencyidval", authHandlerDAOObj.getCurrency(currencyid));
                            obj.put("cntype", debitNote.getDntype());
                            obj.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + externalCurrencyRate + " " + (debitNote.getCurrency() == null ? "" : debitNote.getCurrency().getCurrencyCode()));
//                            Date dueDate = df.parse(df.format(je.getEntryDate()));
                            Date dueDate = df.parse(df.format(debitNote.getCreationDate()));
                            if (isSOA) { //export SOA report case
//                                amountdue = authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid);
                                amountdue = authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dncurrencyid, debitNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid);
                            }
//                            if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                accruedbalance = authHandler.round(amountdue, companyid);
//                            } else
                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }

                            switch(noOfInterval){
                                case 2:
                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 3:
                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 4:
                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 5:
                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 6:
                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 7:
                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 8:
                                    amountdue9 += amountdue10 + amountdue11;
                                    amountdue10 = amountdue11 = 0;
                                    break;
                                case 9:
                                    amountdue10 += amountdue11;
                                    amountdue11 = 0;
                                    break;
                            }
                            
                            obj.put("amountdue1", amountdue1);
                            obj.put("amountdue2", amountdue2);
                            obj.put("amountdue3", amountdue3);
                            obj.put("amountdue4", amountdue4);
                            obj.put("amountdue5", amountdue5);
                            obj.put("amountdue6", amountdue6);
                            obj.put("amountdue7", amountdue7);
                            obj.put("amountdue8", amountdue8);
                            obj.put("amountdue9", amountdue9);
                            obj.put("amountdue10", amountdue10);
                            obj.put("amountdue11", amountdue11);
                            
//                            obj.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
                            if(!requestParams.containsKey("isAgedReceivables") || !(Boolean)requestParams.get("isAgedReceivables")){
                                getDebitNoteCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, accountingHandlerDAOobj, accCreditNoteDAOobj, request, debitNote, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows, fieldMapRows, replaceFieldMap, customFieldMap, customDateFieldMap, FieldMap, obj);
                            }
                            JArr.put(obj);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }
    
    public static double getDebitNoteAmountDue(DebitNote debitNote, HashMap<String, Object> requestParams, accReceiptDAO accReceiptDao, accCreditNoteDAO accCreditNoteDAOobj, String companyid) throws ServiceException{
        double dnReceiptAmount = 0;
        KwlReturnObject dnpResult = accReceiptDao.getDebitNotePaymentDetail(requestParams);
        if (!dnpResult.getEntityList().isEmpty()) {
            Iterator dnpItr = dnpResult.getEntityList().iterator();
            while (dnpItr.hasNext()) {
                Object[] objects = (Object[]) dnpItr.next();
                double exchangeratefortransaction = objects[0] != null ? (Double) objects[0] : 1.0;
                double dnPaidAmtInReceiptCurrency = objects[1] != null ? (Double) objects[1] : 0.0;
                dnReceiptAmount += authHandler.round(dnPaidAmtInReceiptCurrency / exchangeratefortransaction, companyid);
            }
        }
        double linkAmount = 0;
        KwlReturnObject linkResult = accReceiptDao.getLinkDetailReceiptToDebitNote(requestParams);
        List<LinkDetailReceiptToDebitNote> detail = linkResult.getEntityList();
        for (LinkDetailReceiptToDebitNote ldr : detail) {
            linkAmount += ldr.getAmountInDNCurrency();
        }

        /*
         * Inlcuding Debit Note amount Linked in Credit Note
         */
        double cnLinkAmount = 0;
        KwlReturnObject result = accCreditNoteDAOobj.getCNRowsFromDebitNote(requestParams);
        List<CreditNoteDetail> rows = result.getEntityList();
        for (CreditNoteDetail creditNoteDetail : rows) {
            double exchangeRate = creditNoteDetail.getExchangeRateForTransaction();
            Discount disc = creditNoteDetail.getDiscount();
            if (disc != null) {
                //When currency of CN and DN is different, if currency is same then exchangeRate=1, case exchangeRate!=0 is applied for avaoiding AE
                if (exchangeRate != 1 && exchangeRate != 0) {
                    cnLinkAmount += authHandler.round(disc.getDiscountValue() / exchangeRate, companyid);
                } else {
                    cnLinkAmount += disc.getDiscountValue();
                }
            }
        }
        double amountdue = debitNote.getDnamount() - (dnReceiptAmount + linkAmount + cnLinkAmount);
        return amountdue;
    }
    
    public static void getDebitNoteCustomField(String companyid, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj, AccountingHandlerDAO accountingHandlerDAOobj, accCreditNoteDAO accCreditNoteDAOobj, HttpServletRequest request, DebitNote debitNote, HashMap<String, String> replaceFieldMapRows, HashMap<String, String> customFieldMapRows, HashMap<String, String> customDateFieldMapRows, HashMap<String, Integer> fieldMapRows, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, Integer> FieldMap, com.krawler.utils.json.base.JSONObject obj ) throws ServiceException, JSONException{
                            if (fieldDataManagercntrl != null && accJournalEntryobj != null) {
                                Map<String, Object> variableMap = new HashMap<String, Object>();
                                HashMap<String, Object> cnDetailRequestParams = new HashMap<String, Object>();
                                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                                Detailfilter_names.add("companyid");
                                Detailfilter_params.add(companyid);
                                Detailfilter_names.add("journalentryId");
                                Detailfilter_params.add(debitNote.getJournalEntry().getID());
                                Detailfilter_names.add("moduleId");
                                Detailfilter_params.add(Constants.Acc_Debit_Note_ModuleId + "");
                                cnDetailRequestParams.put("filter_names", Detailfilter_names);
                                cnDetailRequestParams.put("filter_params", Detailfilter_params);
                                KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomDataNew(cnDetailRequestParams);
                                if (idcustresult.getEntityList().size() > 0) {
                                    String journalentryId = idcustresult.getEntityList().get(0).toString();
                                    KwlReturnObject jeCustomResult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalentryId);
                                    AccJECustomData jeCustom = (AccJECustomData) jeCustomResult.getEntityList().get(0);
                                    AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                                    JSONObject params = new JSONObject();
                                    boolean isExport = (boolean) ((request.getAttribute("isExport") == null) ? false : request.getAttribute("isExport"));
                if (!isExport) {
                                        isExport = (request.getParameter("isAged") == null) ? false : Boolean.parseBoolean(request.getParameter("isAged"));
                                    }
                                    params.put("isExport", isExport);
                                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                                }
                            }
                            
                            Set<JournalEntryDetail> jedDetails = debitNote.getJournalEntry() != null ? debitNote.getJournalEntry().getDetails() : new HashSet(0);
                            if (jedDetails != null && !jedDetails.isEmpty()) {// In aged Report we are showing line level item dimension at global level by comma seperated. below code is for that
                                Map<String, List<Object>> linelabelDataMap = new LinkedHashMap();
                                for (JournalEntryDetail jedetailrow : jedDetails) {
                                    JSONObject customObject = new JSONObject();
                                    Map<String, Object> variableMapRows = new HashMap<String, Object>();
                                    HashMap<String, Object> invDetailsRequestParams = new HashMap<String, Object>();
                                    ArrayList Detailfilter_names = new ArrayList();
                                    ArrayList Detailfilter_params = new ArrayList();
                                    Detailfilter_names.add(Constants.Acc_jedetailId);
                                    Detailfilter_params.add(jedetailrow.getID());
                                    invDetailsRequestParams.put(Constants.filterNamesKey, Detailfilter_names);
                                    invDetailsRequestParams.put(Constants.filterParamsKey, Detailfilter_params);
                                    KwlReturnObject idcustdetailresult = accCreditNoteDAOobj.geCreditNoteCustomData(invDetailsRequestParams);
                                    if (idcustdetailresult.getEntityList().size() > 0) {
                                        AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustdetailresult.getEntityList().get(0);
                                        AccountingManager.setCustomColumnValues(jeDetailCustom, fieldMapRows, replaceFieldMapRows, variableMapRows);
                                        if (jeDetailCustom != null) {
                                            JSONObject params = new JSONObject();
                                            params.put(Constants.isForReport, true);
                                            fieldDataManagercntrl.getLineLevelCustomData(variableMapRows, customFieldMapRows, customDateFieldMapRows, customObject, params);
                                            if (customObject.length() > 0) {
                                                Iterator<String> keys = customObject.keys();
                                                while (keys.hasNext()) {
                                                    String key = (String) keys.next();
                                                    if (!key.equals("allCustomFieldKeyValuePairString")) {// no need to go for key allCustomFieldKeyValuePairString
                                                        if (linelabelDataMap.containsKey(key)) {
                                                            if (!linelabelDataMap.get(key).contains(customObject.get(key))) {
                                                                linelabelDataMap.get(key).add(customObject.get(key));
                                                            }
                                                        } else {
                                                            List<Object> dataList = new ArrayList<>();
                                                            dataList.add(customObject.get(key));
                                                            linelabelDataMap.put(key, dataList);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                for (Map.Entry<String, List<Object>> entry : linelabelDataMap.entrySet()) {
                                    String commaSeperatedValue = "";
                                    String key = entry.getKey();
                                    List<Object> data = entry.getValue();
                                    for (Object dataObj : data) {
                                        if (dataObj != null) {
                                            if (StringUtil.isNullOrEmpty(commaSeperatedValue)) {
                                                commaSeperatedValue = dataObj.toString();
                                            } else {
                                                commaSeperatedValue += "," + dataObj.toString();
                                            }
                                        }
                                    }
                                    obj.put(key, commaSeperatedValue);
                                }
                            }
                        }
    
    public static JSONArray getDebitNotesMergedJson(HashMap<String, Object> requestParams, List list, JSONArray JArr, AccountingHandlerDAO accountingHandlerDAOobj, authHandlerDAO authHandlerDAOObj, accCurrencyDAO accCurrencyDAOobj, accReceiptDAO accReceiptDao, JSONObject request, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj,accCreditNoteDAO accCreditNoteDAOobj) throws ServiceException {
        try {
            boolean ignoreZero = request.optString("ignorezero") != null ? Boolean.parseBoolean(request.optString("ignorezero")) : false;
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get(Constants.companyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ 
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
           int noOfInterval = request.has("noOfInterval") ? request.optInt("noOfInterval",7) : 7;
            int duration = (requestParams.containsKey(InvoiceConstants.duration) && requestParams.containsKey(InvoiceConstants.duration)) ? Integer.parseInt(requestParams.get(InvoiceConstants.duration).toString()) : 30;
            boolean isSOA = (requestParams.containsKey("isSOA") && requestParams.get("isSOA") != null) ? Boolean.parseBoolean(requestParams.get("isSOA").toString()) : false;
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();

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
            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.optString(Constants.asOfDate) != null) {
                    try {
                        curDate = df.parse(request.optString(Constants.asOfDate));
                    } catch (Exception e) {
                        curDate = new Date(request.optString(Constants.asOfDate));
                    }

                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {

                double amountdue1 = 0;
                double amountdue2 = 0;
                double amountdue3 = 0;
                double amountdue4 = 0;
                double amountdue5 = 0;
                double amountdue6 = 0;
                double amountdue7 = 0;
                double amountdue8 = 0;
                double amountdue9 = 0;
                double amountdue10 = 0;
                double amountdue11 = 0;
//                double accruedbalance = 0;

                Object[] row = (Object[]) itr.next();
                boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                String personid = "";
                String personname = "";
                String aliasname = "";
                String billto = "";
                String dncurrencyid = "";

                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                Customer customer = (Customer) resultObject.getEntityList().get(0);
                if (customer != null) {
                    personid = customer.getID();
                    personname = customer.getName();
                    aliasname = customer.getAliasname();
                    HashMap<String, Object> addressParams = new HashMap<String, Object>();
                    addressParams.put("companyid", companyid);
                    addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                    addressParams.put("isBillingAddress", true);    //true to get billing address
                    addressParams.put("customerid", personid);
                    CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                    billto = customerAddressDetails != null ? customerAddressDetails.getAddress() : "";
                }

                resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                if (!withoutinventory) {
                    resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);
                    DebitNote debitNote = (DebitNote) resultObject.getEntityList().get(0);
                    if (debitNote != null) {
                        Logger.getLogger(AccInvoiceServiceHandler.class.getName()).log(Level.INFO, "Normal DN=" + debitNote.getDebitNoteNumber());
                        requestParams.put("debitnoteid", debitNote.getID());
                        double dnReceiptAmount = 0;
                        KwlReturnObject dnpResult = accReceiptDao.getDebitNotePaymentDetail(requestParams);
                        if (!dnpResult.getEntityList().isEmpty()) {
                            Iterator dnpItr = dnpResult.getEntityList().iterator();
                            while (dnpItr.hasNext()) {
                                Object[] objects = (Object[]) dnpItr.next();
                                double exchangeratefortransaction = objects[0] != null ? (Double) objects[0] : 1.0;
                                double dnPaidAmtInReceiptCurrency = objects[1] != null ? (Double) objects[1] : 0.0;
                                dnReceiptAmount += authHandler.round(dnPaidAmtInReceiptCurrency / exchangeratefortransaction, companyid);
                            }
                        }
                        double linkAmount = 0;
                        KwlReturnObject linkResult = accReceiptDao.getLinkDetailReceiptToDebitNote(requestParams);
                        List<LinkDetailReceiptToDebitNote> detail = linkResult.getEntityList();
                        for (LinkDetailReceiptToDebitNote ldr : detail) {
                            linkAmount += ldr.getAmountInDNCurrency();
                        }

                          /*
                         * Inlcuding Debit Note amount Linked in Credit Note
                         */
                       double cnLinkAmount = 0; 
                       KwlReturnObject result = accCreditNoteDAOobj.getCNRowsFromDebitNote(requestParams);
                        List<CreditNoteDetail> rows = result.getEntityList();
                        for (CreditNoteDetail creditNoteDetail : rows) {
                            Discount disc = creditNoteDetail.getDiscount();
                            if (disc != null) {
                                cnLinkAmount += disc.getDiscountValue();
                            }
                        }
                        double amountdue = debitNote.getDnamount() - (dnReceiptAmount + linkAmount+cnLinkAmount);
                        requestParams.remove("debitnoteid");//Removing debitnoteid after use, So that is does not affect other 

                        if ((debitNote.isOtherwise() && amountdue > 0) || !ignoreZero) {
                            JournalEntry je = debitNote.getJournalEntry();
                            double externalCurrencyRate = je.getExternalCurrencyRate()==0?1:je.getExternalCurrencyRate();
                            dncurrencyid = debitNote.getCurrency() == null ? currency.getCurrencyID() : debitNote.getCurrency().getCurrencyID();
                            obj.put(InvoiceConstants.billid, debitNote.getID());
                            obj.put(InvoiceConstants.noteid, debitNote.getID());
                            obj.put(InvoiceConstants.noteno, debitNote.getDebitNoteNumber());
                            obj.put(InvoiceConstants.companyid, debitNote.getCompany().getCompanyID());
                            obj.put(InvoiceConstants.companyname, debitNote.getCompany().getCompanyName());
                            obj.put(InvoiceConstants.billno, debitNote.getDebitNoteNumber());
                            obj.put(InvoiceConstants.journalentryid, je.getID());
                            obj.put(InvoiceConstants.withoutinventory, withoutinventory);
                            obj.put(InvoiceConstants.currencysymbol, (debitNote.getCurrency() == null ? currency.getSymbol() : debitNote.getCurrency().getSymbol()));
                            obj.put(InvoiceConstants.currencyid, (debitNote.getCurrency() == null ? currency.getCurrencyID() : debitNote.getCurrency().getCurrencyID()));
                            obj.put(InvoiceConstants.currencyname, (debitNote.getCurrency() == null ? currency.getName() : debitNote.getCurrency().getName()));
                            obj.put(InvoiceConstants.entryno, je.getEntryNumber());
                            obj.put(InvoiceConstants.personid, personid);
                            obj.put(InvoiceConstants.personname, personname);
                            obj.put(InvoiceConstants.aliasname, aliasname);
                            obj.put(InvoiceConstants.billto, billto);
                            obj.put(InvoiceConstants.amount, debitNote.isOtherwise() ? debitNote.getDnamount() : details.getAmount());
                            obj.put(InvoiceConstants.amountdue, debitNote.isOtherwise() ? amountdue : 0);
                            obj.put(InvoiceConstants.amountduenonnegative, debitNote.isOtherwise() ? amountdue : 0);
//                            obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dncurrencyid, debitNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
//                            obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, debitNote.getDnamount(), dncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, debitNote.getDnamount(), dncurrencyid, debitNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            obj.put("code", customer != null ? customer.getAcccode() : "");
                            obj.put(InvoiceConstants.personinfo, customer != null ? personname+"("+customer.getAcccode()+")" : "");
//                            obj.put(InvoiceConstants.duedate, df.format(je.getEntryDate()));
//                            obj.put(InvoiceConstants.date, df.format(je.getEntryDate()));
                            obj.put(InvoiceConstants.duedate, df.format(debitNote.getCreationDate()));
                            obj.put(InvoiceConstants.date, df.format(debitNote.getCreationDate()));
                            obj.put(InvoiceConstants.memo, debitNote.getMemo());
                            obj.put(InvoiceConstants.deleted, debitNote.isDeleted());
                            obj.put(InvoiceConstants.externalcurrencyrate, externalCurrencyRate);
                            obj.put(Constants.SEQUENCEFORMATID, debitNote.getSeqformat() != null ? debitNote.getSeqformat().getID() : "");
                            obj.put("includingGST", debitNote.isIncludingGST());
                            obj.put("costcenterid", debitNote.getCostcenter() == null ? "" : debitNote.getCostcenter().getID());
                            obj.put("type", "Debit Note");
                            obj.put("isDN", true);
                            obj.put("currencyidval", authHandlerDAOObj.getCurrency(currencyid));
                            obj.put("cntype", debitNote.getDntype());
                            obj.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + externalCurrencyRate + " " + (debitNote.getCurrency() == null ? "" : debitNote.getCurrency().getCurrencyCode()));
//                            Date dueDate = df.parse(df.format(je.getEntryDate()));
                            Date dueDate = df.parse(df.format(debitNote.getCreationDate()));
                            if (isSOA) { //export SOA report case
//                                amountdue = authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid);
                                amountdue = authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dncurrencyid, debitNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid);
                            }
//                            if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                accruedbalance = authHandler.round(amountdue, companyid);
//                            } else 
                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }
                            
                            switch(noOfInterval){
                                case 2:
                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 3:
                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 4:
                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 5:
                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 6:
                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 7:
                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                    break;
                                case 8:
                                    amountdue9 += amountdue10 + amountdue11;
                                    amountdue10 = amountdue11 = 0;
                                    break;
                                case 9:
                                    amountdue10 += amountdue11;
                                    amountdue11 = 0;
                                    break;
                            }

                            obj.put("amountdue1", amountdue1);
                            obj.put("amountdue2", amountdue2);
                            obj.put("amountdue3", amountdue3);
                            obj.put("amountdue4", amountdue4);
                            obj.put("amountdue5", amountdue5);
                            obj.put("amountdue6", amountdue6);
                            obj.put("amountdue7", amountdue7);
                            obj.put("amountdue8", amountdue8);
                            obj.put("amountdue9", amountdue9);
                            obj.put("amountdue10", amountdue10);
                            obj.put("amountdue11", amountdue11);
//                            obj.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
                            if (fieldDataManagercntrl != null && accJournalEntryobj != null) {
                                Map<String, Object> variableMap = new HashMap<String, Object>();
                                HashMap<String, Object> cnDetailRequestParams = new HashMap<String, Object>();
                                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                                Detailfilter_names.add("companyid");
                                Detailfilter_params.add(debitNote.getCompany().getCompanyID());
                                Detailfilter_names.add("journalentryId");
                                Detailfilter_params.add(debitNote.getJournalEntry().getID());
                                Detailfilter_names.add("moduleId");
                                Detailfilter_params.add(Constants.Acc_Debit_Note_ModuleId + "");
                                cnDetailRequestParams.put("filter_names", Detailfilter_names);
                                cnDetailRequestParams.put("filter_params", Detailfilter_params);
                                KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomDataNew(cnDetailRequestParams);
                                if (idcustresult.getEntityList().size() > 0) {
                                    String journalentryId = idcustresult.getEntityList().get(0).toString();
                                    KwlReturnObject jeCustomResult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalentryId);
                                    AccJECustomData jeCustom = (AccJECustomData) jeCustomResult.getEntityList().get(0);
                                    AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                                    JSONObject params = new JSONObject();
                                    boolean isExport =  request.optBoolean("isExport",false);
                                    if(!isExport){
                                        isExport =  request.optBoolean("isAged",false);
                                    }
                                    params.put("isExport", isExport);
                                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                                }
                            }
                            JArr.put(obj);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }

    public static JSONArray getReceiptsJson(HashMap requestParams, List entityList, JSONArray JArr, AccountingHandlerDAO accountingHandlerDAOobj, authHandlerDAO authHandlerDAOObj, accCurrencyDAO accCurrencyDAOobj, accReceiptDAO accReceiptDAOobj, HttpServletRequest request, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj) throws ServiceException {
        try {
            boolean ignoreZero = request.getParameter("ignorezero") != null ? Boolean.parseBoolean(request.getParameter("ignorezero")) : false;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ //Handle Date Parse exception. ERP-33531
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            String companyid = (String) requestParams.get(InvoiceConstants.companyid);
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            boolean agedReport = (requestParams.containsKey("agedReport") && requestParams.get("agedReport") != null) ? Boolean.parseBoolean(requestParams.get("agedReport").toString()) : false;
            boolean isSOA = (requestParams.containsKey("isSOA") && requestParams.get("isSOA") != null) ? Boolean.parseBoolean(requestParams.get("isSOA").toString()) : false;
            boolean isAgedPayables=false;//when request will come from aged payable report either summary or details this flag will be true.
            if(requestParams.containsKey("isAgedPayables") && requestParams.get("isAgedPayables")!=null && Boolean.parseBoolean(requestParams.get("isAgedPayables").toString())){
                isAgedPayables = true;
            }
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            HashMap<String, Object> fieldrequestParamsGlobalLevel = new HashMap();
            HashMap<String, String> customFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, Integer> FieldMapGlobalLevel = null;
            fieldrequestParamsGlobalLevel.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParamsGlobalLevel.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId));
            HashMap<String, String> replaceFieldMapGlobalLevel = new HashMap<String, String>();
            if (accAccountDAOobj != null) {
                FieldMapGlobalLevel = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParamsGlobalLevel, replaceFieldMapGlobalLevel, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel);
            }
            
            //Custom field details Maps for Line Level data
            HashMap<String, Object> fieldrequestParamsRows = new HashMap();
            HashMap<String, String> replaceFieldMapRows = new HashMap();
            HashMap<String, String> customFieldMapRows = new HashMap();
            HashMap<String, String> customDateFieldMapRows = new HashMap();
            fieldrequestParamsRows.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParamsRows.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId));
            HashMap<String, Integer> fieldMapRows = null;
            if (accAccountDAOobj != null) {
                fieldMapRows = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParamsRows, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows);
            }
            
            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.getAttribute(Constants.asOfDate) != null) {
                    curDate = (Date) request.getAttribute(Constants.asOfDate);
                } else if(requestParams.get("MonthlyAgeingCurrDate")!=null){
                    curDate = (Date) requestParams.get("MonthlyAgeingCurrDate");
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;
            
            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            Iterator itr = entityList.iterator();
            while (itr.hasNext()) {

                double amountdue1 = 0;
                double amountdue2 = 0;
                double amountdue3 = 0;
                double amountdue4 = 0;
                double amountdue5 = 0;
                double amountdue6 = 0;
                double amountdue7 = 0;
                double amountdue8 = 0;
                double amountdue9 = 0;
                double amountdue10 = 0;
                double amountdue11 = 0;
//                double accruedbalance = 0;

                String rpcurrencyid = "";
                String personid = "";
                String personname = "";
                String aliasname = "";
                String personcode = "";
                String customercreditterm = "";
                String billto = "";

                Object[] row = (Object[]) itr.next();
                Receipt receipt = (Receipt) row[0];

                if (receipt != null && (receipt.getCustomer() != null || !StringUtil.isNullOrEmpty(receipt.getVendor()))) {
                    Logger.getLogger(AccInvoiceServiceHandler.class.getName()).log(Level.INFO, "Normal Receipt=" + receipt.getReceiptNumber());
                    com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                    double amountdue = getReceiptAmountDue(receipt, requestParams, accReceiptDAOobj);

                    if (amountdue > 0 || !ignoreZero) {
                        if (receipt.getCustomer() != null) {
                            Customer customer = receipt.getCustomer();
                            personid = customer.getID();
                            personname = customer.getName();
                            aliasname = customer.getAliasname();
                            personcode = customer.getAcccode();
                            customercreditterm = customer.getCreditTerm()==null? "":customer.getCreditTerm().getTermname();
                            HashMap<String, Object> addressParams = new HashMap<String, Object>();
                            addressParams.put("companyid", companyid);
                            addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                            addressParams.put("isBillingAddress", true);    //true to get billing address
                            addressParams.put("customerid", receipt.getCustomer().getID());
                            CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                            billto = customerAddressDetails != null ? customerAddressDetails.getAddress() : "";
                        } else if (!StringUtil.isNullOrEmpty(receipt.getVendor())) {
                            KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                            Vendor vendor = (Vendor) customerResult.getEntityList().get(0);
                            if (vendor != null) {
                                personid = vendor.getID();
                                personname = vendor.getName();
                                aliasname = vendor.getAliasname();
                                personcode = vendor.getAcccode();
                                customercreditterm = vendor.getDebitTerm().getTermname();
                            }
                        }
                        JournalEntry je = receipt.getJournalEntry();
                        double externalCurrencyRate = je.getExternalCurrencyRate()==0?1:je.getExternalCurrencyRate();
                        rpcurrencyid = receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID();
                        obj.put(InvoiceConstants.billid, receipt.getID());
                        obj.put(InvoiceConstants.companyid, receipt.getCompany().getCompanyID());
                        obj.put(InvoiceConstants.companyname, receipt.getCompany().getCompanyName());
                        obj.put(InvoiceConstants.billno, receipt.getReceiptNumber());
                        obj.put(InvoiceConstants.journalentryid, je.getID());
                        obj.put(InvoiceConstants.withoutinventory, false);
                        obj.put(InvoiceConstants.currencysymbol, (receipt.getCurrency() == null ? currency.getSymbol() : receipt.getCurrency().getSymbol()));
                        obj.put(InvoiceConstants.currencyid, (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID()));
                        obj.put(InvoiceConstants.currencyname, (receipt.getCurrency() == null ? currency.getName() : receipt.getCurrency().getName()));
                        obj.put(InvoiceConstants.entryno, je.getEntryNumber());
                        obj.put(InvoiceConstants.personid, personid);
                        obj.put(InvoiceConstants.personname, personname);
                        obj.put(InvoiceConstants.personinfo, personname+"("+personcode+")");
                        obj.put(InvoiceConstants.aliasname, aliasname);
                        obj.put(InvoiceConstants.CustomerCreditTerm, customercreditterm);
                        obj.put(InvoiceConstants.billto, billto);
                        if ((agedReport || isSOA) && !isAgedPayables) {
                            obj.put(InvoiceConstants.amountdue, -amountdue);
                            obj.put(InvoiceConstants.amountdueinbase, -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, rpcurrencyid, receipt.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                        } else {
                            obj.put(InvoiceConstants.amountdue, amountdue);
                            obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, rpcurrencyid, receipt.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                        }
                        obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receipt.getDepositAmount(), rpcurrencyid, receipt.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                        obj.put("code", personcode);
//                        obj.put(InvoiceConstants.duedate, df.format(je.getEntryDate()));
//                        obj.put(InvoiceConstants.date, df.format(je.getEntryDate()));
                        obj.put(InvoiceConstants.duedate, df.format(receipt.getCreationDate()));
                        obj.put(InvoiceConstants.date, df.format(receipt.getCreationDate()));
                        obj.put(InvoiceConstants.memo, receipt.getMemo());
                        obj.put(InvoiceConstants.deleted, receipt.isDeleted());
                        obj.put(InvoiceConstants.externalcurrencyrate, externalCurrencyRate);
                        obj.put("paymentwindowtype", receipt.getPaymentWindowType());
                        obj.put("type", "Payment Received");
                        double customerCreditLimit = 0;
                        double customerCreditLimitInbase = 0;
                        String currencyId = "";
                        if (receipt.getCustomer() != null && receipt.getCustomer().getCurrency() != null) {
                            currencyId = receipt.getCustomer().getCurrency().getCurrencyID();
                            customerCreditLimit = receipt.getCustomer().getCreditlimit();
                            KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, customerCreditLimit, currencyId, receipt.getCustomer().getCreatedOn(), 0);
                            customerCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                        } else if (!StringUtil.isNullOrEmpty(receipt.getVendor())) {
                            KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                            Vendor vendor = (Vendor) customerResult.getEntityList().get(0);
                            currencyId = vendor.getCurrency().getCurrencyID();
                            customerCreditLimit = vendor.getDebitlimit();
                            KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, customerCreditLimit, currencyId, vendor.getCreatedOn(), 0);
                            customerCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                        }
                        obj.put("creditlimit", customerCreditLimit);
                        obj.put("creditlimitinbase", customerCreditLimitInbase);
                        obj.put("isRP", true);
                        obj.put("currencyidval", authHandlerDAOObj.getCurrency(currencyid));
                        obj.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + externalCurrencyRate + " " + (receipt.getCurrency() == null ? "" : receipt.getCurrency().getCurrencyCode()));
//                        Date dueDate = df.parse(df.format(je.getEntryDate()));
                        Date dueDate = df.parse(df.format(receipt.getCreationDate()));
                        if (!isAgedPayables) {
                            if (agedReport) { //Aged report viev case
                                amountdue = -amountdue;
                            } else if (isSOA) { //export SOA report case
//                                amountdue = -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, rpcurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid);
                                amountdue = -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, rpcurrencyid, receipt.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid);
                            }
                        }

//                        if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                            accruedbalance = authHandler.round(amountdue, companyid);
//                        } else
                        if (dueDate.after(oneDayBeforeCal1Date)) {
                            if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                            } else {
                                amountdue1 = authHandler.round(amountdue, companyid); // Current
                            }
                        } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                            amountdue2 = authHandler.round(amountdue, companyid);
                        } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                            amountdue3 = authHandler.round(amountdue, companyid);
                        } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                            amountdue4 = authHandler.round(amountdue, companyid);
                        } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                            amountdue5 = authHandler.round(amountdue, companyid);
                        } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                            amountdue6 = authHandler.round(amountdue, companyid);
                        } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                            amountdue7 = authHandler.round(amountdue, companyid);
                        } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                            amountdue8 = authHandler.round(amountdue, companyid);
                        } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                            amountdue9 = authHandler.round(amountdue, companyid);
                        } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                            amountdue10 = authHandler.round(amountdue, companyid);
                        } else {                                                                                          // > 270           
                            amountdue11 = authHandler.round(amountdue, companyid);
                        } 

                        switch (noOfInterval) {
                            case 2:
                                amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 3:
                                amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 4:
                                amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 5:
                                amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 6:
                                amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 7:
                                amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 8:
                                amountdue9 += amountdue10 + amountdue11;
                                amountdue10 = amountdue11 = 0;
                                break;
                            case 9:
                                amountdue10 += amountdue11;
                                amountdue11 = 0;
                                break;
                        }
                        
                        obj.put("amountdue1", amountdue1);
                        obj.put("amountdue2", amountdue2);
                        obj.put("amountdue3", amountdue3);
                        obj.put("amountdue4", amountdue4);
                        obj.put("amountdue5", amountdue5);
                        obj.put("amountdue6", amountdue6);
                        obj.put("amountdue7", amountdue7);
                        obj.put("amountdue8", amountdue8);
                        obj.put("amountdue9", amountdue9);
                        obj.put("amountdue10", amountdue10);
                        obj.put("amountdue11", amountdue11);
//                        obj.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);

                        if(!requestParams.containsKey("isAgedReceivables") || !(Boolean)requestParams.get("isAgedReceivables")){
                            getReceiptCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, accountingHandlerDAOobj, accReceiptDAOobj, request, receipt, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel, FieldMapGlobalLevel, replaceFieldMapGlobalLevel, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows, fieldMapRows, obj);
                        }
                        JArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceControllerCMN.getReceiptsJson: " + ex.getMessage(), ex);
        }
        return JArr;
    }
    
    public static void getReceiptCustomField(String companyid, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj, AccountingHandlerDAO accountingHandlerDAOobj, accReceiptDAO accReceiptDAOobj, HttpServletRequest request, Receipt receipt, HashMap<String, String> customFieldMapGlobalLevel, HashMap<String, String> customDateFieldMapGlobalLevel,HashMap<String, Integer> FieldMapGlobalLevel,HashMap<String, String> replaceFieldMapGlobalLevel, HashMap<String, String> replaceFieldMapRows, HashMap<String, String> customFieldMapRows, HashMap<String, String> customDateFieldMapRows, HashMap<String, Integer> fieldMapRows, com.krawler.utils.json.base.JSONObject obj ) throws ServiceException, JSONException{
                        if (fieldDataManagercntrl != null && accJournalEntryobj != null) {
                            Map<String, Object> variableMap = new HashMap<String, Object>();
                            HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
            
                            Detailfilter_names.add("companyid");
            Detailfilter_params.add(companyid);
                            Detailfilter_names.add("journalentryId");
                            Detailfilter_params.add(receipt.getJournalEntry().getID());
                            Detailfilter_names.add("moduleId");
                            Detailfilter_params.add(Constants.Acc_Receive_Payment_ModuleId + "");
                            invDetailRequestParams.put("filter_names", Detailfilter_names);
                            invDetailRequestParams.put("filter_params", Detailfilter_params);
                            KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomDataNew(invDetailRequestParams);
                            if (idcustresult.getEntityList().size() > 0) {
                                String journalentryId = idcustresult.getEntityList().get(0).toString();
                                KwlReturnObject jeCustomResult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalentryId);
                                AccJECustomData jeCustom = (AccJECustomData) jeCustomResult.getEntityList().get(0);
                                AccountingManager.setCustomColumnValues(jeCustom, FieldMapGlobalLevel, replaceFieldMapGlobalLevel, variableMap);
                                JSONObject params = new JSONObject();
                                params.put("companyid", companyid);
                                boolean isExport = (boolean) ((request.getAttribute("isExport") == null) ? false : request.getAttribute("isExport"));
                                if (!isExport) {
                                    isExport = (request.getParameter("isAged") == null) ? false : Boolean.parseBoolean(request.getParameter("isAged"));
                                }
                                params.put("isExport", isExport);
                                fieldDataManagercntrl.addCustomData(variableMap, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel, obj, params);
                            }
                        }

                        Map<String, List<Object>> linelabelDataMap = new LinkedHashMap();
                        for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {//line level item at advance record
                            getReceiptPaymentLineLevelCustomData(linelabelDataMap, fieldMapRows, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows, obj, advanceDetail.getId(), accReceiptDAOobj, fieldDataManagercntrl);
                        }

                        KwlReturnObject cndnResult = accReceiptDAOobj.getCustomerDnPayment(receipt.getID());
                        List<DebitNotePaymentDetails> cndnList = cndnResult.getEntityList();
                        for (DebitNotePaymentDetails dnpd : cndnList) {//line level item at cndn record
                            getReceiptPaymentLineLevelCustomData(linelabelDataMap, fieldMapRows, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows, obj, dnpd.getID(), accReceiptDAOobj, fieldDataManagercntrl);
                        }

                        HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
                        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                        filter_names.add("receipt.ID");
                        filter_params.add(receipt.getID());
                        rRequestParams.put("filter_names", filter_names);
                        rRequestParams.put("filter_params", filter_params);
                        KwlReturnObject grdresult = accReceiptDAOobj.getReceiptDetails(rRequestParams);
                        List<ReceiptDetail> details = grdresult.getEntityList();
                        for (ReceiptDetail rd : details) {//line level item at invoice record
                            getReceiptPaymentLineLevelCustomData(linelabelDataMap, fieldMapRows, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows, obj, rd.getID(), accReceiptDAOobj, fieldDataManagercntrl);
                        }

                        for (Map.Entry<String, List<Object>> entry : linelabelDataMap.entrySet()) {
                            String commaSeperatedValue = "";
                            String key = entry.getKey();
                            List<Object> data = entry.getValue();
                            for (Object dataObj : data) {
                                if (dataObj != null) {
                                    if (StringUtil.isNullOrEmpty(commaSeperatedValue)) {
                                        commaSeperatedValue = dataObj.toString();
                                    } else {
                                        commaSeperatedValue += "," + dataObj.toString();
                                    }
                                }
                            }
                            obj.put(key, commaSeperatedValue);
                        }
                    }
    
    public static void getReceiptPaymentLineLevelCustomData(Map<String, List<Object>> linelabelDataMap,HashMap<String, Integer> fieldMapRows,HashMap<String, String> replaceFieldMapRows,HashMap<String, String> customFieldMapRows,HashMap<String, String> customDateFieldMapRows,JSONObject obj,String recID,accReceiptDAO accReceiptDAOobj,fieldDataManager fieldDataManagercntrl) throws JSONException, ServiceException {
        JSONObject customObject = new JSONObject();
        Map<String, Object> variableMapRows = new HashMap<String, Object>();
        HashMap<String, Object> invDetailsRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList();
        ArrayList Detailfilter_params = new ArrayList();
        Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
        Detailfilter_params.add(recID);
        invDetailsRequestParams.put(Constants.filterNamesKey, Detailfilter_names);
        invDetailsRequestParams.put(Constants.filterParamsKey, Detailfilter_params);
        KwlReturnObject idcustdetailresult = accReceiptDAOobj.getReciptPaymentCustomData(invDetailsRequestParams);
        if (idcustdetailresult.getEntityList().size() > 0) {
            AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustdetailresult.getEntityList().get(0);
            AccountingManager.setCustomColumnValues(jeDetailCustom, fieldMapRows, replaceFieldMapRows, variableMapRows);
            if (jeDetailCustom != null) {
                JSONObject params = new JSONObject();
                params.put(Constants.isForReport, true);
                fieldDataManagercntrl.getLineLevelCustomData(variableMapRows, customFieldMapRows, customDateFieldMapRows, customObject, params);
                if (customObject.length() > 0) {
                    Iterator<String> keys = customObject.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (!key.equals("allCustomFieldKeyValuePairString")) {// no need to go for key allCustomFieldKeyValuePairString
                            if (linelabelDataMap.containsKey(key)) {
                                if (!linelabelDataMap.get(key).contains(customObject.get(key))) {
                                    linelabelDataMap.get(key).add(customObject.get(key));
                                }
                            } else {
                                List<Object> dataList = new ArrayList<>();
                                dataList.add(customObject.get(key));
                                linelabelDataMap.put(key, dataList);
                            }
                        }
                    }
                }
            }
        }
    }
    public static JSONArray getReceiptsJson(HashMap requestParams, List entityList, JSONArray JArr, AccountingHandlerDAO accountingHandlerDAOobj, authHandlerDAO authHandlerDAOObj, accCurrencyDAO accCurrencyDAOobj, accReceiptDAO accReceiptDAOobj, JSONObject request, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj) throws ServiceException {
        try {
            boolean ignoreZero = request.optString("ignorezero") != null ? Boolean.parseBoolean(request.optString("ignorezero")) : false;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ 
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            String companyid = (String) requestParams.get(InvoiceConstants.companyid);
            int noOfInterval = request.has("noOfInterval") ? request.optInt("noOfInterval",7) : 7;
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            boolean agedReport = (requestParams.containsKey("agedReport") && requestParams.get("agedReport") != null) ? Boolean.parseBoolean(requestParams.get("agedReport").toString()) : false;
            boolean isSOA = (requestParams.containsKey("isSOA") && requestParams.get("isSOA") != null) ? Boolean.parseBoolean(requestParams.get("isSOA").toString()) : false;
            boolean isAgedPayables=false;//when request will come from aged payable report either summary or details this flag will be true.
            if(requestParams.containsKey("isAgedPayables") && requestParams.get("isAgedPayables")!=null && Boolean.parseBoolean(requestParams.get("isAgedPayables").toString())){
                isAgedPayables = true;
            }
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            HashMap<String, Object> fieldrequestParamsGlobalLevel = new HashMap();
            HashMap<String, String> customFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, Integer> FieldMapGlobalLevel = null;
            fieldrequestParamsGlobalLevel.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParamsGlobalLevel.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId));
            HashMap<String, String> replaceFieldMapGlobalLevel = new HashMap<String, String>();
            if (accAccountDAOobj != null) {
                FieldMapGlobalLevel = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParamsGlobalLevel, replaceFieldMapGlobalLevel, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel);
            }
            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = new Date();
                if (request.optString(Constants.asOfDate) != null) {
                    try {
                        curDate = df.parse(request.optString(Constants.asOfDate));
                    } catch (Exception e) {
                        curDate = new Date(request.optString(Constants.asOfDate));
                    }
                } else if(requestParams.get("MonthlyAgeingCurrDate")!=null){
                    curDate = (Date) requestParams.get("MonthlyAgeingCurrDate");
                } else {
                    curDate = df.parse(curDateString);
                }
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            Iterator itr = entityList.iterator();
            while (itr.hasNext()) {

                double amountdue1 = 0;
                double amountdue2 = 0;
                double amountdue3 = 0;
                double amountdue4 = 0;
                double amountdue5 = 0;
                double amountdue6 = 0;
                double amountdue7 = 0;
                double amountdue8 = 0;
                double amountdue9 = 0;
                double amountdue10 = 0;
                double amountdue11 = 0;
//                double accruedbalance = 0;

                String rpcurrencyid = "";
                String personid = "";
                String personname = "";
                String aliasname = "";
                String personcode = "";
                String billto = "";

                Object[] row = (Object[]) itr.next();
                Receipt receipt = (Receipt) row[0];

                if (receipt != null && (receipt.getCustomer() != null || !StringUtil.isNullOrEmpty(receipt.getVendor()))) {
                    Logger.getLogger(AccInvoiceServiceHandler.class.getName()).log(Level.INFO, "Normal Receipt=" + receipt.getReceiptNumber());
                    com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                    double amountdue = getReceiptAmountDue(receipt, requestParams, accReceiptDAOobj);

                    if (amountdue > 0 || !ignoreZero) {
                        if (receipt.getCustomer() != null) {
                            Customer customer = receipt.getCustomer();
                            personid = customer.getID();
                            personname = customer.getName();
                            aliasname = customer.getAliasname();
                            personcode = customer.getAcccode();
                            HashMap<String, Object> addressParams = new HashMap<String, Object>();
                            addressParams.put("companyid", companyid);
                            addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                            addressParams.put("isBillingAddress", true);    //true to get billing address
                            addressParams.put("customerid", receipt.getCustomer().getID());
                            CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                            billto = customerAddressDetails != null ? customerAddressDetails.getAddress() : "";
                        } else if (!StringUtil.isNullOrEmpty(receipt.getVendor())) {
                            KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                            Vendor vendor = (Vendor) customerResult.getEntityList().get(0);
                            if (vendor != null) {
                                personid = vendor.getID();
                                personname = vendor.getName();
                                aliasname = vendor.getAliasname();
                                personcode = vendor.getAcccode();
                            }
                        }
                        JournalEntry je = receipt.getJournalEntry();
                        double externalCurrencyRate = je.getExternalCurrencyRate()==0?1:je.getExternalCurrencyRate();
                        rpcurrencyid = receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID();
                        obj.put(InvoiceConstants.billid, receipt.getID());
                        obj.put(InvoiceConstants.companyid, receipt.getCompany().getCompanyID());
                        obj.put(InvoiceConstants.companyname, receipt.getCompany().getCompanyName());
                        obj.put(InvoiceConstants.billno, receipt.getReceiptNumber());
                        obj.put(InvoiceConstants.journalentryid, je.getID());
                        obj.put(InvoiceConstants.withoutinventory, false);
                        obj.put(InvoiceConstants.currencysymbol, (receipt.getCurrency() == null ? currency.getSymbol() : receipt.getCurrency().getSymbol()));
                        obj.put(InvoiceConstants.currencyid, (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID()));
                        obj.put(InvoiceConstants.currencyname, (receipt.getCurrency() == null ? currency.getName() : receipt.getCurrency().getName()));
                        obj.put(InvoiceConstants.entryno, je.getEntryNumber());
                        obj.put(InvoiceConstants.personid, personid);
                        obj.put(InvoiceConstants.personname, personname);
                        obj.put(InvoiceConstants.personinfo, personname+"("+personcode+")");
                        obj.put(InvoiceConstants.aliasname, aliasname);
                        obj.put(InvoiceConstants.billto, billto);
                        if ((agedReport || isSOA) && !isAgedPayables) {
                            obj.put(InvoiceConstants.amountdue, -amountdue);
//                            obj.put(InvoiceConstants.amountdueinbase, -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, rpcurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            obj.put(InvoiceConstants.amountdueinbase, -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, rpcurrencyid, receipt.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                        } else {
                            obj.put(InvoiceConstants.amountdue, amountdue);
//                            obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, rpcurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, rpcurrencyid, receipt.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                        }
//                        obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receipt.getDepositAmount(), rpcurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                        obj.put("amountinbase", authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receipt.getDepositAmount(), rpcurrencyid, receipt.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                        obj.put("code", personcode);
//                        obj.put(InvoiceConstants.duedate, df.format(je.getEntryDate()));
//                        obj.put(InvoiceConstants.date, df.format(je.getEntryDate()));
                        obj.put(InvoiceConstants.duedate, df.format(receipt.getCreationDate()));
                        obj.put(InvoiceConstants.date, df.format(receipt.getCreationDate()));
                        obj.put(InvoiceConstants.memo, receipt.getMemo());
                        obj.put(InvoiceConstants.deleted, receipt.isDeleted());
                        obj.put(InvoiceConstants.externalcurrencyrate, externalCurrencyRate);
                        obj.put("paymentwindowtype", receipt.getPaymentWindowType());
                        obj.put("type", "Payment Received");
                        obj.put("isRP", true);
                        obj.put("currencyidval", authHandlerDAOObj.getCurrency(currencyid));
                        obj.put(InvoiceConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + externalCurrencyRate + " " + (receipt.getCurrency() == null ? "" : receipt.getCurrency().getCurrencyCode()));
//                        Date dueDate = df.parse(df.format(je.getEntryDate()));
                        Date dueDate = df.parse(df.format(receipt.getCreationDate()));
                        if (!isAgedPayables) {
                            if (agedReport) { //Aged report viev case
                                amountdue = -amountdue;
                            } else if (isSOA) { //export SOA report case
//                                amountdue = -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, rpcurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid);
                                amountdue = -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, rpcurrencyid, receipt.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid);
                            }
                        }

//                        if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                            accruedbalance = authHandler.round(amountdue, companyid);
//                        } else 
                        if (dueDate.after(oneDayBeforeCal1Date)) {
                            if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                            } else {
                                amountdue1 = authHandler.round(amountdue, companyid); // Current
                            }
                        } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                            amountdue2 = authHandler.round(amountdue, companyid);
                        } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                            amountdue3 = authHandler.round(amountdue, companyid);
                        } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                            amountdue4 = authHandler.round(amountdue, companyid);
                        } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                            amountdue5 = authHandler.round(amountdue, companyid);
                        } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                            amountdue6 = authHandler.round(amountdue, companyid);
                        } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                            amountdue7 = authHandler.round(amountdue, companyid);
                        } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                            amountdue8 = authHandler.round(amountdue, companyid);
                        } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                            amountdue9 = authHandler.round(amountdue, companyid);
                        } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                            amountdue10 = authHandler.round(amountdue, companyid);
                        } else {                                                                                          // > 270           
                            amountdue11 = authHandler.round(amountdue, companyid);
                        }

                        switch(noOfInterval){
                            case 2:
                                amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 3:
                                amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 4:
                                amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 5:
                                amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 6:
                                amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 7:
                                amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 8:
                                amountdue9 += amountdue10 + amountdue11;
                                amountdue10 = amountdue11 = 0;
                                break;
                            case 9:
                                amountdue10 += amountdue11;
                                amountdue11 = 0;
                                break;
                        }
                        
                        obj.put("amountdue1", amountdue1);
                        obj.put("amountdue2", amountdue2);
                        obj.put("amountdue3", amountdue3);
                        obj.put("amountdue4", amountdue4);
                        obj.put("amountdue5", amountdue5);
                        obj.put("amountdue6", amountdue6);
                        obj.put("amountdue7", amountdue7);
                        obj.put("amountdue8", amountdue8);
                        obj.put("amountdue9", amountdue9);
                        obj.put("amountdue10", amountdue10);
                        obj.put("amountdue11", amountdue11);
//                        obj.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);

                        if (fieldDataManagercntrl != null && accJournalEntryobj != null) {
                            Map<String, Object> variableMap = new HashMap<String, Object>();
                            HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                            Detailfilter_names.add("companyid");
                            Detailfilter_params.add(receipt.getCompany().getCompanyID());
                            Detailfilter_names.add("journalentryId");
                            Detailfilter_params.add(receipt.getJournalEntry().getID());
                            Detailfilter_names.add("moduleId");
                            Detailfilter_params.add(Constants.Acc_Receive_Payment_ModuleId + "");
                            invDetailRequestParams.put("filter_names", Detailfilter_names);
                            invDetailRequestParams.put("filter_params", Detailfilter_params);
                            KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomDataNew(invDetailRequestParams);
                            if (idcustresult.getEntityList().size() > 0) {
                                String journalentryId = idcustresult.getEntityList().get(0).toString();
                                KwlReturnObject jeCustomResult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalentryId);
                                AccJECustomData jeCustom = (AccJECustomData) jeCustomResult.getEntityList().get(0);
                                AccountingManager.setCustomColumnValues(jeCustom, FieldMapGlobalLevel, replaceFieldMapGlobalLevel, variableMap);
                                JSONObject params = new JSONObject();
                                params.put("companyid", companyid);
                                boolean isExport =   request.optBoolean("isExport",false);
                                if (!isExport) {
                                    isExport =  request.optBoolean("isAged",false);
                                }
                                params.put("isExport", isExport);
                                fieldDataManagercntrl.addCustomData(variableMap, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel, obj, params);
                            }
                        }
                        JArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceControllerCMN.getReceiptsJson: " + ex.getMessage(), ex);
        }
        return JArr;
    }

    public static double getReceiptAmountDue(Receipt receipt, HashMap requestParams, accReceiptDAO accReceiptDAOobj) throws ServiceException, ParseException {
         /*
         * There are only three type of payments can be due 
         * 1 : Opening receipt
         * 2 : Advance receipt Against customer
         * 3 : Refund Payment Agaisnt vendor
         */
        boolean isOpeningReceipt = false;
        boolean isAdvanceReceiptToCustomer = false;
        boolean isRefundReceiptToVendor = false;
        DateFormat df = (DateFormat) requestParams.get("df");
        String companyid = (String) requestParams.get(InvoiceConstants.companyid);
        double receiptDueAmt = 0;
        if ((receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty()) || receipt.isIsOpeningBalenceReceipt()) {//case for advance payment and opening payment
            double advAmount = 0;
            double linkedReceiptAmt = 0;
            if (receipt.isIsOpeningBalenceReceipt()) {//opening receipt
                isOpeningReceipt = true;
                advAmount = receipt.getDepositAmount();
            } else if (receipt.getCustomer() != null) {//customer advance receipt
                isAdvanceReceiptToCustomer = true;
                for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                    advAmount += advanceDetail.getAmount();
                }
            } else if (!StringUtil.isNullOrEmpty(receipt.getVendor())) { // vendor refund
                isRefundReceiptToVendor = true;
                for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                    if (StringUtil.isNullOrEmpty(advanceDetail.getAdvancedetailid())) {//Only such refunds can be due in which at the time of creation no document (no advance receipt) is selected 
                        advAmount += advanceDetail.getAmount();
                    }
                }
                //In this case advAmount can be zero (if all refund have advance payment documnet selected) so we need to retun amount due zero here
                if (advAmount == 0) {
                    return 0;
                }
            }
            HashMap<String, Object> reqParams1 = new HashMap();
            reqParams1.put("receiptid", receipt.getID());
            reqParams1.put("companyid", companyid);
            reqParams1.put(Constants.df, df);
            if (requestParams.containsKey("asofdate") && requestParams.get("asofdate") != null) {
                reqParams1.put("asofdate", requestParams.get("asofdate"));
            }
            if (isOpeningReceipt || isAdvanceReceiptToCustomer) {
                KwlReturnObject result = accReceiptDAOobj.getLinkDetailReceipt(reqParams1);
                List<LinkDetailReceipt> linkedDetaisReceipts = result.getEntityList();
                for (LinkDetailReceipt ldr : linkedDetaisReceipts) {
                    linkedReceiptAmt += ldr.getAmount();
                }
                result = accReceiptDAOobj.getLinkDetailReceiptToDebitNote(reqParams1);
                List<LinkDetailReceiptToDebitNote> lrdn = result.getEntityList();
                for (LinkDetailReceiptToDebitNote l : lrdn) {
                    linkedReceiptAmt += l.getAmount();
                }
                result = accReceiptDAOobj.getReceiptWriteOffEntries(reqParams1);
                List<ReceiptWriteOff> list1 = result.getEntityList();
                for (ReceiptWriteOff R : list1) {
                    linkedReceiptAmt += R.getWrittenOffAmountInReceiptCurrency();
                }
                result = accReceiptDAOobj.getAdvanceDetailsByReceipt(reqParams1);
                List<AdvanceDetail> list2 = result.getEntityList();
                for (AdvanceDetail advDet : list2) {
                    double revExchangeRate = 1.0;
                    if (advDet.getExchangeratefortransaction() != 0.0) {
                        revExchangeRate = 1 / advDet.getExchangeratefortransaction();
                    }
                    linkedReceiptAmt += authHandler.round(revExchangeRate * advDet.getAmount(), companyid);
                }
                result = accReceiptDAOobj.getLinkDetailAdvanceReceiptToRefundPayment(reqParams1);
                List<LinkDetailPaymentToAdvancePayment> linkDetailPaymentToAdvancePayment = result.getEntityList();
                for (LinkDetailPaymentToAdvancePayment ldp : linkDetailPaymentToAdvancePayment) {
                    linkedReceiptAmt += ldp.getAmountInPaymentCurrency();
                }
                receiptDueAmt = advAmount - linkedReceiptAmt;
            } else if(isRefundReceiptToVendor){
                KwlReturnObject result = accReceiptDAOobj.getLinkDetailReceiptToAdvancePayment(reqParams1);
                List<LinkDetailReceiptToAdvancePayment> linkDetailReceiptToAdvancePayment = result.getEntityList();
                for (LinkDetailReceiptToAdvancePayment ldp : linkDetailReceiptToAdvancePayment) {
                    linkedReceiptAmt += ldp.getAmount();
                }
                receiptDueAmt = advAmount - linkedReceiptAmt;
            }

        }
        receiptDueAmt = authHandler.round(receiptDueAmt, companyid);
        return receiptDueAmt;
    }

    public static JSONArray getInvoiceJsonMerged(HttpServletRequest request, List list, JSONArray jArr, AccountingHandlerDAO accountingHandlerDAOobj, authHandlerDAO authHandlerDAOObj, accCurrencyDAO accCurrencyDAOobj, accInvoiceDAO accInvoiceDAOobj, accAccountDAO accAccountDAOobj, AccCostCenterDAO accCostCenterObj, accInvoiceCMN accInvoiceCommon, accReceiptDAO accReceiptDAOobj, accTaxDAO accTaxObj, kwlCommonTablesDAO kwlCommonTablesDAOObj, accJournalEntryDAO accJournalEntryobj, AccInvoiceServiceDAO accInvoiceServiceDAO) throws SessionExpiredException, ServiceException {
//        JSONObject jobj=new JSONObject();        
//        JSONArray jArr=new JSONArray();
        List ExcludedIDlist = Collections.EMPTY_LIST;
        try {
            HashMap requestParams = getInvoiceRequestMap(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
            boolean ignoreZero = request.getParameter("ignorezero") != null ? Boolean.parseBoolean(request.getParameter("ignorezero")) : false;
            boolean onlyAmountDue = requestParams.get("onlyamountdue") != null ? Boolean.parseBoolean(request.getParameter("onlyamountdue")) : false;
            boolean isAged = (requestParams.containsKey("isAged") && requestParams.get("isAged") != null) ? Boolean.parseBoolean(requestParams.get("isAged").toString()) : false;
            boolean onlyOutstanding = request.getAttribute("onlyOutstanding") != null ? Boolean.parseBoolean(request.getParameter("onlyOutstanding")) : false;
            boolean invoiceReport = false; // ((Boolean) request.getAttribute("report")) : false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("report"))) {
                invoiceReport = Boolean.parseBoolean(request.getParameter("report"));
            }
            
           int noOfInterval = StringUtil.isNullOrEmpty(request.getParameter("noOfInterval"))? 7 : Integer.parseInt(request.getParameter("noOfInterval"));
           
            boolean isSOA = request.getAttribute("isSOA") != null ? (Boolean) request.getAttribute("isSOA") : false;
            int datefilter = StringUtil.getInteger(request.getParameter("datefilter"));// 0 = Invoice Due date OR 1 = Invoice date
//            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
//            String cashAccount = pref.getCashAccount().getID();
            boolean isSalesCommissionStmt = (requestParams.containsKey(InvoiceConstants.isSalesCommissionStmt)) ? Boolean.parseBoolean((String) requestParams.get(InvoiceConstants.isSalesCommissionStmt)) : false;
            boolean isProduct = (requestParams.containsKey(InvoiceConstants.productid) && !StringUtil.isNullOrEmpty((String) requestParams.get(InvoiceConstants.productid))) ? true : false;
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            int duration = 30;
            if (!StringUtil.isNullOrEmpty(request.getParameter("InvoicesOnDate"))) {
                Date InvoicesOnDate = (Date) df.parse(request.getParameter("InvoicesOnDate"));
                HashMap<String, Object> ExcParams = new HashMap();
                ExcParams.put("InvoicesOnDate", InvoicesOnDate);
                KwlReturnObject excludedInvoicesObj = accInvoiceDAOobj.getExcludedInvoices(ExcParams);
                ExcludedIDlist = excludedInvoicesObj.getEntityList();
            }
            double commission = 0;
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            if (onlyOutstanding) {
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, "customfield"));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Invoice_ModuleId, 0, 1));
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }
            if (isSalesCommissionStmt) {

                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(requestParams.get(Constants.companyKey));
                requestParams.put(Constants.filterNamesKey, filter_names);
                requestParams.put(Constants.filterParamsKey, filter_params);

                KwlReturnObject result = accCostCenterObj.getSalesCommission(requestParams);
                List<SalesCommission> salesCommissions = result.getEntityList();
                commission = salesCommissions.get(0).getCommission();

            }
            String curDateString = "";
            Date curDate = null;
            boolean booleanAged = false;//Added for aged payable/receivable

            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();

            if (requestParams.get(Constants.asOfDate) != null) {//Added for aged payable/receivable
                curDateString = (String) requestParams.get(Constants.asOfDate);
                curDate = df.parse(curDateString);
                booleanAged = true;
                oneDayBeforeCal1.setTime(curDate);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal8.setTime(curDate);
                cal9.setTime(curDate);
                cal10.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
                cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
                cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            }

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;
            DateFormat dateFormat = authHandler.getDateOnlyFormat();

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            double amountdue1 = 0;
            double amountdue2 = 0;
            double amountdue3 = 0;
            double amountdue4 = 0;
            double amountdue5 = 0;
            double amountdue6 = 0;
            double amountdue7 = 0;
            double amountdue8 = 0;
            double amountdue9 = 0;
            double amountdue10 = 0;
            double amountdue11 = 0;
            double amountWD = 0;
            Iterator itr = list.iterator();
            while (itr.hasNext()) {

                //Invoice invoice = (Invoice) itr.next();
                Object[] oj = (Object[]) itr.next();
                String invid = oj[0].toString();
                //Withoutinventory 0 for normal, 1 for billing
                boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
                {
                    amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = amountWD = 0;
                    double taxPercent = 0;
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                    Invoice invoice = (Invoice) objItr.getEntityList().get(0);

                    Date invoiceCreationDate = invoice.getCreationDate();
                    Double externalCurrencyRate = 0d;
                    Double invoiceOriginalAmount = 0d;
                    if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                        ExchangeRateDetails erd = invoice.getExchangeRateDetail();
                        externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                        invoiceOriginalAmount = invoice.getOriginalOpeningBalanceAmount();
                    }

                    JournalEntry je = null;
                    if (invoice.isNormalInvoice()) {
                        je = invoice.getJournalEntry();
//                        invoiceCreationDate = je.getEntryDate();
                        externalCurrencyRate = je.getExternalCurrencyRate();
                    }

                    JournalEntryDetail d = null;
                    if (invoice.isNormalInvoice()) {
                        d = invoice.getCustomerEntry();
                        invoiceOriginalAmount = d.getAmount();
                    }

                    Account account = null;
                    if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                        account = invoice.getCustomer().getAccount();
//                        KwlReturnObject accObjItr = accountingHandlerDAOobj.getObject(Account.class.getName(), invoice.getCustomer().getID());
//                        account = (Account) accObjItr.getEntityList().get(0);
                    } else {
                        account = d.getAccount();
                    }

                    String currencyid = (invoice.getCurrency() == null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());
                    List ll = null;
                    if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                        ll = new ArrayList();
                        ll.add(invoice.getOpeningBalanceAmountDue());
                        ll.add(0.0);
                        ll.add(0.0);
                    } else {
                        if (Constants.InvoiceAmountDueFlag && !isAged) {
                            ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, invoice);
                        } else {
                            ll = accInvoiceCommon.getAmountDue_Discount(requestParams, invoice);
                        }
                    }

                    double amountdue = (Double) ll.get(0);
                    double discountDeduct = (Double) ll.get(1);
                    amountWD = (Double) ll.get(2);
                    amountWD = amountWD - accInvoiceServiceDAO.getInvDisountOnAmt(invoice.getID().toString(), amountWD, withoutinventory);
                    if (onlyAmountDue && authHandler.round(amountdue, companyid) == 0) {
                        continue;
                    }
                    int isReval = 0;
                    if (invoiceReport && !invoice.isIsOpeningBalenceInvoice()) {
                        KwlReturnObject brdAmt = accInvoiceDAOobj.getRevalFlag(invoice.getID());
                        List reval = brdAmt.getEntityList();
                        if (!reval.isEmpty() && (Long) reval.get(0) > 0) {
                            isReval = 1;
                        }
                    }
                    com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                    obj.put("billid", invoice.getID());
                    obj.put("isOpeningBalanceTransaction", invoice.isIsOpeningBalenceInvoice());
                    obj.put("isNormalTransaction", invoice.isNormalInvoice());
                    obj.put("companyid", invoice.getCompany().getCompanyID());
                    obj.put("companyname", invoice.getCompany().getCompanyName());
                    obj.put("withoutinventory", withoutinventory);
                    obj.put("partialinv", invoice.isPartialinv());
                    obj.put("personid", invoice.getCustomer() == null ? account.getID() : invoice.getCustomer().getID());
                    HashMap<String, Object> addressParams = new HashMap<String, Object>();
                    addressParams.put("companyid", companyid);
                    addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                    addressParams.put("isBillingAddress", true);    //true to get billing address
                    addressParams.put("customerid", invoice.getCustomer().getID());
                    CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                    obj.put("personemail", customerAddressDetails != null ? customerAddressDetails.getEmailID() : "");
                    obj.put("customername", invoice.getCustomer() == null ? "" : invoice.getCustomer().getName());
                    obj.put("accid", account.getID());
                    obj.put("billno", invoice.getInvoiceNumber());
                    obj.put("currencyid", currencyid);
                    obj.put("currencyidval", authHandlerDAOObj.getCurrency(sessionHandlerImpl.getCurrencyID(request)));
                    obj.put("currencysymbol", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
                    obj.put("currencycode", (invoice.getCurrency() == null ? currency.getCurrencyCode() : invoice.getCurrency().getCurrencyCode()));
                    obj.put("currencyname", (invoice.getCurrency() == null ? currency.getName() : invoice.getCurrency().getName()));
                    obj.put("companyaddress", invoice.getCompany().getAddress());
                    obj.put("companyname", invoice.getCompany().getCompanyName());
                    obj.put("isfavourite", invoice.isFavourite());
                    obj.put("isprinted", invoice.isPrinted());
                    //                obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,je.getEntryDate()));
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, invoiceCreationDate, 0);
                    obj.put("oldcurrencyrate", (Double) bAmt.getEntityList().get(0));
                    obj.put("billto", invoice.getBillTo());
                    obj.put("shipto", invoice.getShipTo());
                    obj.put("journalentryid", (je != null ? je.getID() : ""));
                    obj.put("porefno", invoice.getPoRefNumber());
                    obj.put("externalcurrencyrate", (je != null ? je.getExternalCurrencyRate() : externalCurrencyRate));
                    obj.put("entryno", (je != null ? je.getEntryNumber() : ""));
                    obj.put("date", df.format(invoiceCreationDate));
                    obj.put("shipdate", invoice.getShipDate() == null ? "" : df.format(invoice.getShipDate()));
                    obj.put("duedate", df.format(invoice.getDueDate()));
                    obj.put("personname", invoice.getCustomer() == null ? account.getName() : invoice.getCustomer().getName());
                    obj.put(InvoiceConstants.personinfo, invoice.getCustomer() == null ? "" : invoice.getCustomer().getName()+"("+invoice.getCustomer().getAcccode()+")");
                    obj.put("salesPerson", invoice.getMasterSalesPerson() == null ? "" : invoice.getMasterSalesPerson().getID());
                    obj.put("memo", invoice.getMemo());
                    obj.put("termname", invoice.getCustomer() == null ? "" : ((invoice.getCustomer().getCreditTerm() == null) ? "" : invoice.getCustomer().getCreditTerm().getTermname()));
                    obj.put("deleted", invoice.isDeleted());
                    obj.put("taxincluded", invoice.getTax() == null ? false : true);
                    obj.put("taxid", invoice.getTax() == null ? "" : invoice.getTax().getID());
                    obj.put("taxname", invoice.getTax() == null ? "" : invoice.getTax().getName());
                    obj.put("taxamount", invoice.getTaxEntry() == null ? 0 : invoice.getTaxEntry().getAmount());
                    obj.put("discount", invoice.getDiscount() == null ? 0 : invoice.getDiscount().getDiscountValue());
                    obj.put("ispercentdiscount", invoice.getDiscount() == null ? false : invoice.getDiscount().isInPercent());
                    obj.put("discountval", invoice.getDiscount() == null ? 0 : invoice.getDiscount().getDiscount());
                    obj.put("shipvia", invoice.getShipvia() == null ? "" : invoice.getShipvia());
                    obj.put("posttext", invoice.getPostText() == null ? "" : invoice.getPostText());
                    obj.put("fob", invoice.getFob() == null ? "" : invoice.getFob());
                    obj.put("termdetails", accInvoiceServiceDAO.getTermDetails(invoice.getID()));
                    obj.put("termdays", (invoice.getTermid() == null) ? 0 : invoice.getTermid().getTermdays());
                    if (extraPref != null && extraPref.getCompany().getCountry()!= null && extraPref.getCompany().getCountry().getID().equals(String.valueOf(Constants.indian_country_id))) {
                        obj.put("formtypeid", invoice.getFormtype() != null ? invoice.getFormtype() : 0);
                        obj.put("isInterstateParty", invoice.getCustomer().isInterstateparty());
                        obj.put("formseriesno", !StringUtil.isNullOrEmpty(invoice.getFormseriesno())? invoice.getFormseriesno():0);
                        obj.put("formno", !StringUtil.isNullOrEmpty(invoice.getFormno())? invoice.getFormno():0);
                        obj.put("formdate", invoice.getFormdate());
                        obj.put("formamount", invoice.getFormamount());
                        if (!StringUtil.isNullOrEmpty(invoice.getFormstatus())) {
                            if (invoice.getFormstatus().equals("1")) {
                                obj.put("formstatus", "NA");
                            } else if (invoice.getFormstatus().equals("2")) {
                                obj.put("formstatus", "Pending");
                            } else if(invoice.getFormstatus().equals("3")){
                                obj.put("formstatus", "Submitted");
                            }
                        }
                    }
                    boolean excluded = false;
                    String action = "";
                    Iterator Excludeditr = ExcludedIDlist.iterator();
                    while (Excludeditr.hasNext()) {
                        ExcludedOutstandingOrders ExcludedObj = (ExcludedOutstandingOrders) Excludeditr.next();
                        if (ExcludedObj.getInvoice().getID().equals(invoice.getID())) {
                            excluded = true;
                            if (ExcludedObj.getExcludeOrGenerate() == 1) {
                                action = "<b>Manualy Generated</b>";
                            }
                            if (ExcludedObj.getExcludeOrGenerate() == 0) {
                                action = "<b>Excluded</b>";
                            }
                        }
                    }
                    obj.put("excluded", excluded);
                    obj.put("action", action);
                    BillingShippingAddresses addresses = invoice.getBillingShippingAddresses();
                    obj.put(Constants.BILLING_ADDRESS, addresses == null ? (invoice.getBillTo() == null ? "" : invoice.getBillTo()) : addresses.getBillingAddress());
                    obj.put(Constants.BILLING_CITY, addresses == null ? "" : addresses.getBillingCity());
                    obj.put(Constants.BILLING_CONTACT_PERSON, addresses == null ? "" : addresses.getBillingContactPerson());
                    obj.put(Constants.BILLING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getBillingContactPersonNumber());
                    obj.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getBillingContactPersonDesignation());
                    obj.put(Constants.BILLING_COUNTRY, addresses == null ? "" : addresses.getBillingCountry());
                    obj.put(Constants.BILLING_EMAIL, addresses == null ? "" : addresses.getBillingEmail());
                    obj.put(Constants.BILLING_FAX, addresses == null ? "" : addresses.getBillingFax());
                    obj.put(Constants.BILLING_MOBILE, addresses == null ? "" : addresses.getBillingMobile());
                    obj.put(Constants.BILLING_PHONE, addresses == null ? "" : addresses.getBillingPhone());
                    obj.put(Constants.BILLING_POSTAL, addresses == null ? "" : addresses.getBillingPostal());
                    obj.put(Constants.BILLING_STATE, addresses == null ? "" : addresses.getBillingState());
                    obj.put(Constants.BILLING_ADDRESS_TYPE, addresses == null ? "" : addresses.getBillingAddressType());
                    obj.put(Constants.SHIPPING_ADDRESS, addresses == null ? (invoice.getShipTo() == null ? "" : invoice.getShipTo()) : addresses.getShippingAddress());
                    obj.put(Constants.SHIPPING_CITY, addresses == null ? "" : addresses.getShippingCity());
                    obj.put(Constants.SHIPPING_CONTACT_PERSON, addresses == null ? "" : addresses.getShippingContactPerson());
                    obj.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getShippingContactPersonNumber());
                    obj.put(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getShippingContactPersonDesignation());
                    obj.put(Constants.SHIPPING_COUNTRY, addresses == null ? "" : addresses.getShippingCountry());
                    obj.put(Constants.SHIPPING_EMAIL, addresses == null ? "" : addresses.getShippingEmail());
                    obj.put(Constants.SHIPPING_FAX, addresses == null ? "" : addresses.getShippingFax());
                    obj.put(Constants.SHIPPING_MOBILE, addresses == null ? "" : addresses.getShippingMobile());
                    obj.put(Constants.SHIPPING_PHONE, addresses == null ? "" : addresses.getShippingPhone());
                    obj.put(Constants.SHIPPING_POSTAL, addresses == null ? "" : addresses.getShippingPostal());
                    obj.put(Constants.SHIPPING_STATE, addresses == null ? "" : addresses.getShippingState());
                    obj.put(Constants.SHIPPING_ADDRESS_TYPE, addresses == null ? "" : addresses.getShippingAddressType());
                    obj.put("sequenceformatid", invoice.getSeqformat() == null ? "" : invoice.getSeqformat().getID());
                    if (invoiceReport) {
                        obj.put("isreval", isReval);
                    }
                    int pendingApprovalInt = invoice.getPendingapproval();
                    obj.put("approvalstatusint", pendingApprovalInt);
                    if (pendingApprovalInt == Constants.LEVEL_ONE) {
                        obj.put("approvalstatus", "Pending level 1 approval");
                    } else if (pendingApprovalInt == Constants.LEVEL_TWO) {
                        obj.put("approvalstatus", "Pending level 2 approval");
                    } else {
                        obj.put("approvalstatus", "");
                    }

                    if (invoice.getTemplateid() == null) {
                        obj.put("templateid", "");
                        obj.put("templatename", "");
                    } else {
                        obj.put("templateid", invoice.getTemplateid().getTempid());
                        obj.put("templatename", invoice.getTemplateid().getTempname());
                    }
                    obj.put("costcenterid", (je != null ? je.getCostcenter() == null ? "" : je.getCostcenter().getID() : ""));
                    obj.put("costcenterName", (je != null ? je.getCostcenter() == null ? "" : je.getCostcenter().getName() : ""));
                    obj.put("archieve", 0);
                    obj.put("cashtransaction", invoice.isCashtransaction());
                    boolean includeprotax = false;
                    Set<InvoiceDetail> invoiceDetails = invoice.getRows();
                    for (InvoiceDetail invoiceDetail : invoiceDetails) {
                        if (invoiceDetail.getTax() != null) {
                            includeprotax = true;
                            break;
                        }
                    }
                    obj.put("includeprotax", includeprotax);
                    if (invoice.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(invoice.getModifiedby()));
                    }

                    /*
                     * For Product search, add Products details from Invoice
                     * details
                     */
                    if (isProduct && invoice.isNormalInvoice()) {
                        String idvString = isProduct ? oj[4].toString() : ""; //as in list invoiedetail id comes 4th
                        KwlReturnObject objItrID = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), idvString);
                        InvoiceDetail idvObj = (InvoiceDetail) objItrID.getEntityList().get(0);

                        if (idvObj != null) {
                            obj.put("rowproductname", idvObj.getInventory().getProduct().getName());
//                            obj.put("rowquantity", idvObj.getInventory().isInvrecord() ? idvObj.getInventory().getQuantity() : idvObj.getInventory().getActquantity());
                            obj.put("rowquantity", idvObj.getInventory().getQuantity());
                            obj.put("rowrate", idvObj.getRate());

                            Discount disc = idvObj.getDiscount();
                            if (disc != null && disc.isInPercent()) {
                                obj.put("rowprdiscount", disc.getDiscount()); //product discount in percent
                            } else {
                                obj.put("rowprdiscount", 0);
                            }
                            double rowTaxPercent = 0;
                            if (idvObj.getTax() != null) {
//                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), idvObj.getTax().getID());
                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getCreationDate(), idvObj.getTax().getID());
                                rowTaxPercent = (Double) perresult.getEntityList().get(0);
                            }
                            obj.put("rowprtaxpercent", rowTaxPercent);
                        }
                    }

                    if (invoice.isCashtransaction()) {
                        obj.put("amountdue", 0);
                        obj.put("amountdueinbase", 0);
                        obj.put("incash", true);
                    } else {
                        //                    obj.put("amountdueinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,amount - ramount,currencyid,je.getEntryDate()));  //amount left after apllying receipt and CN
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, currencyid, invoiceCreationDate, externalCurrencyRate);
                        obj.put("amountdueinbase", authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                        obj.put("amountdue", authHandler.round(amountdue, companyid));
                        if (booleanAged) {
                            Date dueDate = null;
                            if (!StringUtil.isNullOrEmpty(df.format(invoice.getDueDate()))) {
                                dueDate = df.parse(df.format(invoice.getDueDate()));
                            }
                            if (isSOA) {
                                amountdue = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            }
                            if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                dueDate = df.parse(df.format(invoice.getDueDate()));
                            } else {
                                dueDate = df.parse(df.format(invoiceCreationDate));
                            }

                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                    amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                                } else {
                                    amountdue1 = authHandler.round(amountdue, companyid); // Current
                                }
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                                amountdue8 = authHandler.round(amountdue, companyid);
                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                                amountdue9 = authHandler.round(amountdue, companyid);
                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                                amountdue10 = authHandler.round(amountdue, companyid);
                            } else {                                                                                          // > 270           
                                amountdue11 = authHandler.round(amountdue, companyid);
                            }
                        }

                        switch(noOfInterval){
                            case 2:
                                amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 3:
                                amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 4:
                                amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 5:
                                amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 6:
                                amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 7:
                                amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                amountdue9 = amountdue10 = amountdue11 = 0;
                                break;
                            case 8:
                                amountdue9 += amountdue10 + amountdue11;
                                amountdue10 = amountdue11 = 0;
                                break;
                            case 9:
                                amountdue10 += amountdue11;
                                amountdue11 = 0;
                                break;
                        }

                        
                        // obj.put("amountdue", amountdue);
                    }
                    obj.put("deductDiscount", discountDeduct);
                    obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("amount", invoiceOriginalAmount);   //actual invoice amount
                    if (!invoiceReport) {
                        obj.put("amountdue1", amountdue1);
                        obj.put("amountdue2", amountdue2);
                        obj.put("amountdue3", amountdue3);
                        obj.put("amountdue4", amountdue4);
                        obj.put("amountdue5", amountdue5);
                        obj.put("amountdue6", amountdue6);
                        obj.put("amountdue7", amountdue7);
                        obj.put("amountdue8", amountdue8);
                        obj.put("amountdue9", amountdue9);
                        obj.put("amountdue10", amountdue10);
                        obj.put("amountdue11", amountdue11);
                        obj.put("type", "Sales Invoice");
                    }
                    //                obj.put("amountinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,d.getAmount(),currencyid,je.getEntryDate()));
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceOriginalAmount, currencyid, invoiceCreationDate, externalCurrencyRate);
                    double amountinbase = (Double) bAmt.getEntityList().get(0);
                    obj.put("amountinbase", authHandler.round(amountinbase, companyid));

                    if (invoice.getTax() != null) {
                        //                    taxPercent = CompanyHandler.getTaxPercent(session, request, je.getEntryDate(), invoice.getTax().getID());
//                        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, je.getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoiceCreationDate, invoice.getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);  //tax in percent applyind on invoice
                    try {
                        obj.put("creditDays", invoice.getTermid().getTermdays());
                    } catch (Exception ex) {
                        obj.put("creditDays", 0);
                    }
                    RepeatedInvoices repeatedInvoice = invoice.getRepeateInvoice();
                    obj.put("isRepeated", repeatedInvoice == null ? false : true);
                    if (repeatedInvoice != null) {
                        obj.put("repeateid", repeatedInvoice.getId());
                        obj.put("interval", repeatedInvoice.getIntervalUnit());
                        obj.put("intervalType", repeatedInvoice.getIntervalType());
                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
                        //                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"+sessionHandlerImpl.getTimeZoneDifference(request)));
                        obj.put("NoOfpost", repeatedInvoice.getNoOfInvoicespost());
                        obj.put("NoOfRemainpost", repeatedInvoice.getNoOfRemainInvoicespost());
                        obj.put("startDate", sdf.format(repeatedInvoice.getStartDate()));
                        obj.put("nextDate", sdf.format(repeatedInvoice.getNextDate()));
                        obj.put("expireDate", repeatedInvoice.getExpireDate() == null ? "" : sdf.format(repeatedInvoice.getExpireDate()));
                        requestParams.put("parentInvoiceId", invoice.getID());
                        KwlReturnObject details = accInvoiceDAOobj.getRepeateInvoicesDetails(requestParams);
                        List detailsList = details.getEntityList();
                        obj.put("childCount", detailsList.size());
                    }
                    if (onlyOutstanding && invoice.isNormalInvoice()) {
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                        ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                        Detailfilter_names.add("companyid");
                        Detailfilter_params.add(invoice.getCompany().getCompanyID());
                        Detailfilter_names.add("journalentryId");
                        Detailfilter_params.add(invoice.getJournalEntry().getID());
                        Detailfilter_names.add("moduleId");
                        Detailfilter_params.add(Constants.Acc_Invoice_ModuleId + "");
                        invDetailRequestParams.put("filter_names", Detailfilter_names);
                        invDetailRequestParams.put("filter_params", Detailfilter_params);
                        KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomData(invDetailRequestParams);
                        if (idcustresult.getEntityList().size() > 0) {
                            AccJECustomData jeCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                            AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                            DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                            Date dateFromDB=null;
                            for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                String coldata = varEntry.getValue().toString();
                                if (customFieldMap.containsKey(varEntry.getKey())) {
                                    FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), coldata);
                                    if (fieldComboData != null) {
                                        obj.put(varEntry.getKey(), fieldComboData.getValue() != null ? fieldComboData.getValue() : "");
                                    }
                                } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                    try {
                                        dateFromDB = defaultDateFormat.parse(coldata);
                                        coldata = sdf.format(dateFormat);
                                    } catch (ParseException p) {

                                    }
                                    obj.put(varEntry.getKey(), coldata);
                                } else {
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        obj.put(varEntry.getKey(), coldata);
                                    }
                                }
                            }
                        }
                    }
                    if (isSalesCommissionStmt && !invoice.isIsOpeningBalenceInvoice()) {
                        double remainingAmount = obj.getDouble("amountdue");
                        double invoiceAmount = obj.getDouble("amount");
                        double paidAmount = invoiceAmount - remainingAmount;
                        double difference = amountWD - paidAmount;
                        if (paidAmount == 0) {
                            obj.put("amountDueStatus", "UnPaid");
                            obj.put("amountwithouttax", difference);
                            double commissionamount = difference * commission / 100;
                            obj.put("commission", commissionamount);

//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, difference, currencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, difference, currencyid, invoiceCreationDate, je.getExternalCurrencyRate());
                            double differenceinbase = (Double) bAmt.getEntityList().get(0);
                            obj.put("amountwithouttaxinbase", authHandler.round(differenceinbase, companyid));

//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, commissionamount, currencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, commissionamount, currencyid, invoiceCreationDate, je.getExternalCurrencyRate());
                            commissionamount = (Double) bAmt.getEntityList().get(0);
                            obj.put("commissioninbase", commissionamount);

                        } else if (difference > 0) {
                            com.krawler.utils.json.base.JSONObject ab1 = new com.krawler.utils.json.base.JSONObject(obj.toString());
                            ab1.put("amountDueStatus", "UnPaid");
                            ab1.put("amountwithouttax", difference);
                            double commissionamount = difference * commission / 100;
                            ab1.put("commission", commissionamount);

//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, difference, currencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, difference, currencyid, invoiceCreationDate, je.getExternalCurrencyRate());
                            double differenceinbase = (Double) bAmt.getEntityList().get(0);
                            ab1.put("amountwithouttaxinbase", authHandler.round(differenceinbase, companyid));

//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, commissionamount, currencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, commissionamount, currencyid, invoiceCreationDate, je.getExternalCurrencyRate());
                            commissionamount = (Double) bAmt.getEntityList().get(0);
                            ab1.put("commissioninbase", commissionamount);
                            jArr.put(ab1);
                            obj.put("amountDueStatus", "Paid");
                            obj.put("amountwithouttax", paidAmount);
                            obj.put("commission", (paidAmount * commission / 100));
                        } else {
                            obj.put("amountDueStatus", "Paid");
                            obj.put("amountwithouttax", amountWD);
                            double commissionamount = amountWD * commission / 100;
                            obj.put("commission", commissionamount);

//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountWD, currencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountWD, currencyid, invoiceCreationDate, je.getExternalCurrencyRate());
                            double amountWDinbase = (Double) bAmt.getEntityList().get(0);
                            obj.put("amountwithouttaxinbase", authHandler.round(amountWDinbase, companyid));

//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, commissionamount, currencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, commissionamount, currencyid, invoiceCreationDate, je.getExternalCurrencyRate());
                            commissionamount = (Double) bAmt.getEntityList().get(0);
                            obj.put("commissioninbase", commissionamount);
                        }
                    }
                    if (!(ignoreZero && authHandler.round(amountdue, companyid) <= 0)) {
                        jArr.put(obj);
                    }
                }
            }
            if (request.getParameter("filename") != null) {
                if (request.getParameter("filename").equals("Aged Receivable")) {
                    if (request.getParameter("filetype") != null) {
                        if (request.getParameter("filetype").equals("print")) {
                            if (!request.getParameter("mode").equals("18")) {
                                double total = 0;
                                for (int i = 0; i < jArr.length(); i++) {
                                    total = total + (Double) jArr.getJSONObject(i).get("amountdueinbase");
                                }
                                com.krawler.utils.json.base.JSONObject obj1 = new com.krawler.utils.json.base.JSONObject();
                                obj1.put("amountdueinbase", total);
                                obj1.put("billno", "Total Amount Due");
                                jArr.put(obj1);
                            }
                        }
                    }
                }
            }
//            jobj.put("data", jArr);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getInvoiceJson : " + ex.getMessage(), ex);
        } catch (com.krawler.utils.json.base.JSONException ex) {
            throw ServiceException.FAILURE("getInvoiceJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public static HashMap<String, Object> getInvoiceRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(CCConstants.REQ_costCenterId, request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(InvoiceConstants.accid, request.getParameter(InvoiceConstants.accid));
        requestParams.put(InvoiceConstants.cashonly, request.getParameter(InvoiceConstants.cashonly));
        requestParams.put(InvoiceConstants.creditonly, request.getParameter(InvoiceConstants.creditonly));
        requestParams.put("CashAndInvoice", request.getParameter("CashAndInvoice") != null ? Boolean.parseBoolean(request.getParameter("CashAndInvoice")) : false);
        boolean fullPaidFlag = StringUtil.getBoolean(request.getParameter("fullPaidFlag"));
        requestParams.put(InvoiceConstants.ignorezero, fullPaidFlag ? "false" : request.getParameter(InvoiceConstants.ignorezero));
        requestParams.put(InvoiceConstants.persongroup, request.getParameter(InvoiceConstants.persongroup));
        requestParams.put(InvoiceConstants.isagedgraph, request.getParameter(InvoiceConstants.isagedgraph));
        requestParams.put(InvoiceConstants.curdate, request.getParameter(InvoiceConstants.curdate));
        requestParams.put("asofdate", request.getParameter("asofdate"));
        requestParams.put("isAged", request.getParameter("isAged"));
        requestParams.put(InvoiceConstants.customerid, request.getParameter(InvoiceConstants.customerid));
        requestParams.put(InvoiceConstants.newcustomerid, request.getParameter(InvoiceConstants.newcustomerid));
        requestParams.put(InvoiceConstants.customerCategoryid, request.getParameter(InvoiceConstants.customerCategoryid));
        requestParams.put(InvoiceConstants.deleted, request.getParameter(InvoiceConstants.deleted));
        requestParams.put(InvoiceConstants.nondeleted, request.getParameter(InvoiceConstants.nondeleted));
        requestParams.put(InvoiceConstants.billid, request.getParameter(InvoiceConstants.billid));
        requestParams.put(InvoiceConstants.getRepeateInvoice, request.getParameter(InvoiceConstants.getRepeateInvoice));
        requestParams.put(InvoiceConstants.isSalesCommissionStmt, request.getParameter(InvoiceConstants.isSalesCommissionStmt));
        requestParams.put(InvoiceConstants.userid, request.getParameter(InvoiceConstants.userid));
        requestParams.put(InvoiceConstants.onlyamountdue, request.getParameter(InvoiceConstants.REQ_onlyAmountDue));
        requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
        requestParams.put("noOfInterval", (!StringUtil.isNullOrEmpty(request.getParameter("noOfInterval"))) ? Integer.parseInt(request.getParameter("noOfInterval")) : 7);
        requestParams.put("pendingapproval", (request.getParameter("pendingapproval") != null) ? Boolean.parseBoolean(request.getParameter("pendingapproval")) : false);
        requestParams.put("istemplate", (request.getParameter("istemplate") != null) ? Integer.parseInt(request.getParameter("istemplate")) : 0);
        requestParams.put("isFixedAsset", (request.getParameter("isFixedAsset") != null) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false);
        requestParams.put("isLeaseFixedAsset", (request.getParameter("isLeaseFixedAsset") != null) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false);
        requestParams.put("includeFixedAssetInvoicesFlag", (request.getParameter("includeFixedAssetInvoicesFlag") != null) ? Boolean.parseBoolean(request.getParameter("includeFixedAssetInvoicesFlag")) : false);
        requestParams.put("isMonthlySalesInvoice", (request.getParameter("isMonthlySalesInvoice") != null) ? Boolean.parseBoolean(request.getParameter("isMonthlySalesInvoice")) : false);
        requestParams.put(InvoiceConstants.productid, (request.getParameter(InvoiceConstants.productid) == null) ? "" : request.getParameter(InvoiceConstants.productid));
        requestParams.put(InvoiceConstants.productCategoryid, request.getParameter(InvoiceConstants.productCategoryid));
        requestParams.put(InvoiceConstants.termid, (request.getParameter(InvoiceConstants.termid) == null) ? "" : request.getParameter(InvoiceConstants.termid));
        requestParams.put(InvoiceConstants.prodfiltercustid, (request.getParameter(InvoiceConstants.prodfiltercustid) == null) ? "" : request.getParameter(InvoiceConstants.prodfiltercustid));
        requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null) ? "" : request.getParameter("currencyfilterfortrans"));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        requestParams.put(InvoiceConstants.MARKED_FAVOURITE, request.getParameter(InvoiceConstants.MARKED_FAVOURITE));
        requestParams.put("isOpeningBalanceInvoices", request.getParameter("isOpeningBalanceInvoices"));
        requestParams.put("direction", (request.getParameter("direction") == null) ? "" : request.getParameter("direction"));
        requestParams.put("isLifoFifo", (request.getParameter("isLifoFifo") == null) ? "" : request.getParameter("isLifoFifo"));
        requestParams.put(InvoiceConstants.salesPersonid, (request.getParameter(InvoiceConstants.salesPersonid) == null) ? "" : request.getParameter(InvoiceConstants.salesPersonid));
        if (request.getAttribute("custVendorID") != null) {
            requestParams.put("custVendorID", request.getAttribute("custVendorID").toString());
        } else {
            requestParams.put("custVendorID", request.getParameter("custVendorID"));
        }
        requestParams.put("datefilter", request.getParameter("datefilter"));
        requestParams.put("isBlockQtyReport", request.getParameter("isBlockQtyReport"));
        requestParams.put(InvoiceConstants.duration, (request.getParameter(InvoiceConstants.duration) != null) ? Integer.parseInt(request.getParameter(InvoiceConstants.duration)) : 0);
        requestParams.put("isConsignment", (request.getParameter("isConsignment") != null) ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false);
        requestParams.put("isForOpeningTransactionTab", (request.getParameter("isForOpeningTransactionTab") != null) ? Boolean.parseBoolean(request.getParameter("isForOpeningTransactionTab")) : false);
        requestParams.put("isExciseInvoice", (request.getParameter("isExciseInvoice") != null) ? Boolean.parseBoolean(request.getParameter("isExciseInvoice")) : false);
        requestParams.put("custWarehouse", (request.getParameter("custWarehouse") == null) ? "" : request.getParameter("custWarehouse"));
        requestParams.put("upperLimitDate", request.getParameter("upperLimitDate") == null ? "" : request.getParameter("upperLimitDate"));
        if (request.getParameter("isReceipt") != null) {
            requestParams.put("isReceipt", request.getParameter("isReceipt"));
        }
        requestParams.put("filterForClaimedDateForPayment", request.getParameter("filterForClaimedDateForPayment") == null ? "" : request.getParameter("filterForClaimedDateForPayment"));
        requestParams.put("isDraft", (request.getParameter("isDraft") != null) ? Boolean.parseBoolean(request.getParameter("isDraft")) : false);
        requestParams.put("joborderitem", (request.getParameter("joborderitem") != null) ? Boolean.parseBoolean(request.getParameter("joborderitem")) : false);
        requestParams.put(Constants.CHART_TYPE, (!StringUtil.isNullOrEmpty(request.getParameter(Constants.CHART_TYPE))) ? request.getParameter(Constants.CHART_TYPE) : null); //added chart type in request map object
        if (request.getParameter(Constants.generatedSource) != null) {
            requestParams.put(Constants.generatedSource, (!StringUtil.isNullOrEmpty(request.getParameter(Constants.generatedSource))) ? Integer.parseInt(request.getParameter(Constants.generatedSource)) : null);
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("cntype"))) {
            requestParams.put("cntype", request.getParameter("cntype"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("requestModuleid"))) {
            requestParams.put("requestModuleid", Integer.parseInt(request.getParameter("requestModuleid")));
        }
        if(!StringUtil.isNullOrEmpty(request.getParameter("activeInactive"))){          //Getting the type of filter applied in recurring sales invoice report
            requestParams.put("activeInactive", request.getParameter("activeInactive"));
        }
        return requestParams;
    }

    public static HashMap<String, Object> getInvoiceRequestMapJson(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        requestParams.put(CCConstants.REQ_costCenterId, paramJobj.optString(CCConstants.REQ_costCenterId, null));
        requestParams.put(Constants.ss, paramJobj.optString(Constants.ss, null));
        requestParams.put(InvoiceConstants.accid, paramJobj.optString(InvoiceConstants.accid, null));
        requestParams.put(InvoiceConstants.cashonly, paramJobj.optString(InvoiceConstants.cashonly, null));
        requestParams.put(InvoiceConstants.creditonly, paramJobj.optString(InvoiceConstants.creditonly, null));
        requestParams.put("CashAndInvoice", paramJobj.optString("CashAndInvoice", null) != null ? Boolean.parseBoolean(paramJobj.getString("CashAndInvoice")) : false);
        boolean fullPaidFlag = StringUtil.getBoolean(paramJobj.optString("fullPaidFlag", null));
        requestParams.put(InvoiceConstants.ignorezero, fullPaidFlag ? "false" : paramJobj.optString(InvoiceConstants.ignorezero, null));
        requestParams.put(InvoiceConstants.persongroup, paramJobj.optString(InvoiceConstants.persongroup, null));
        requestParams.put(InvoiceConstants.isagedgraph, paramJobj.optString(InvoiceConstants.isagedgraph, null));
//        requestParams.put(InvoiceConstants.curdate, paramJobj.optString(InvoiceConstants.curdate, null));
        requestParams.put(InvoiceConstants.curdate, paramJobj.optString(Constants.asOfDate, null));
        requestParams.put("asofdate", paramJobj.optString("asofdate", null));
        requestParams.put("isAged", paramJobj.optString("isAged", null));
        requestParams.put(InvoiceConstants.customerid, paramJobj.optString(InvoiceConstants.customerid, null));
        requestParams.put(InvoiceConstants.newcustomerid, paramJobj.optString(InvoiceConstants.newcustomerid, null));
        requestParams.put(InvoiceConstants.customerCategoryid, paramJobj.optString(InvoiceConstants.customerCategoryid, null));
        requestParams.put(InvoiceConstants.deleted, paramJobj.optString(InvoiceConstants.deleted, null));
        requestParams.put(InvoiceConstants.nondeleted, paramJobj.optString(InvoiceConstants.nondeleted, null));
        requestParams.put(InvoiceConstants.billid, paramJobj.optString(InvoiceConstants.billid, null));
        requestParams.put(InvoiceConstants.getRepeateInvoice, paramJobj.optString(InvoiceConstants.getRepeateInvoice, null));
        requestParams.put(InvoiceConstants.isSalesCommissionStmt, paramJobj.optString(InvoiceConstants.isSalesCommissionStmt, null));
        requestParams.put(InvoiceConstants.userid, paramJobj.optString(InvoiceConstants.userid, null));
        requestParams.put(InvoiceConstants.onlyamountdue, paramJobj.optString(InvoiceConstants.REQ_onlyAmountDue, null));
        requestParams.put(Constants.REQ_startdate, paramJobj.optString(Constants.REQ_startdate, null));
        requestParams.put(Constants.REQ_enddate, paramJobj.optString(Constants.REQ_enddate, null));
        requestParams.put("pendingapproval", (paramJobj.optString("pendingapproval", null) != null) ? Boolean.parseBoolean(paramJobj.getString("pendingapproval")) : false);
        requestParams.put("istemplate", (paramJobj.optString("istemplate", null) != null) ? Integer.parseInt(paramJobj.getString("istemplate")) : 0);
        requestParams.put("isFixedAsset", (paramJobj.optString("isFixedAsset", null) != null) ? Boolean.parseBoolean(paramJobj.getString("isFixedAsset")) : false);
        requestParams.put("isLeaseFixedAsset", (paramJobj.optString("isLeaseFixedAsset", null) != null) ? Boolean.parseBoolean(paramJobj.getString("isLeaseFixedAsset")) : false);
        requestParams.put("includeFixedAssetInvoicesFlag", (paramJobj.optString("includeFixedAssetInvoicesFlag", null) != null) ? Boolean.parseBoolean(paramJobj.getString("includeFixedAssetInvoicesFlag")) : false);
        requestParams.put("isMonthlySalesInvoice", (paramJobj.optString("isMonthlySalesInvoice",null) != null) ? Boolean.parseBoolean(paramJobj.optString("isMonthlySalesInvoice","false")) : false);
        requestParams.put(InvoiceConstants.productid, (paramJobj.optString(InvoiceConstants.productid, null) == null) ? "" : paramJobj.getString(InvoiceConstants.productid));
        requestParams.put(InvoiceConstants.productCategoryid, paramJobj.optString(InvoiceConstants.productCategoryid, null));
        requestParams.put(InvoiceConstants.termid, (paramJobj.optString(InvoiceConstants.termid, null) == null) ? "" : paramJobj.optString(InvoiceConstants.termid));
        requestParams.put(InvoiceConstants.prodfiltercustid, (paramJobj.optString(InvoiceConstants.prodfiltercustid, null) == null) ? "" : paramJobj.optString(InvoiceConstants.prodfiltercustid));
        requestParams.put("currencyfilterfortrans", (paramJobj.optString("currencyfilterfortrans", null) == null) ? "" : paramJobj.optString("currencyfilterfortrans"));
        requestParams.put(Constants.Acc_Search_Json, paramJobj.optString(Constants.Acc_Search_Json, null));
        requestParams.put(Constants.Filter_Criteria, paramJobj.optString(Constants.Filter_Criteria, null));
        requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid, null));
        requestParams.put(InvoiceConstants.MARKED_FAVOURITE, paramJobj.optString(InvoiceConstants.MARKED_FAVOURITE, null));
        requestParams.put("isOpeningBalanceInvoices", paramJobj.optString("isOpeningBalanceInvoices", null));
        requestParams.put("direction", (paramJobj.optString("direction", null) == null) ? "" : paramJobj.getString("direction"));
        requestParams.put("isLifoFifo", (paramJobj.optString("isLifoFifo", null) == null) ? "" : paramJobj.getString("isLifoFifo"));
        requestParams.put(InvoiceConstants.salesPersonid, (paramJobj.optString(InvoiceConstants.salesPersonid, null) == null) ? "" : paramJobj.getString(InvoiceConstants.salesPersonid));
        if (paramJobj.optString("custVendorID", null) != null) {
            requestParams.put("custVendorID", paramJobj.get("custVendorID").toString());
        } else {
            requestParams.put("custVendorID", paramJobj.optString("custVendorID", null));
        }
        requestParams.put("datefilter", paramJobj.optString("datefilter", null));
        requestParams.put("isBlockQtyReport", paramJobj.optString("isBlockQtyReport",null));
        requestParams.put(InvoiceConstants.duration, (paramJobj.optString(InvoiceConstants.duration, null) != null) ? Integer.parseInt(paramJobj.get(InvoiceConstants.duration).toString()) : 0);
        requestParams.put("isConsignment", (paramJobj.optString("isConsignment", null) != null) ? Boolean.parseBoolean(paramJobj.getString("isConsignment")) : false);
        requestParams.put("isForOpeningTransactionTab", (paramJobj.optString("isForOpeningTransactionTab",null) != null) ? Boolean.parseBoolean(paramJobj.optString("isForOpeningTransactionTab")) : false);
        requestParams.put("isExciseInvoice", (paramJobj.optString("isExciseInvoice",null) != null) ? Boolean.parseBoolean(paramJobj.optString("isExciseInvoice")) : false);
        requestParams.put("custWarehouse", (paramJobj.optString("custWarehouse", null) == null) ? "" : paramJobj.getString("custWarehouse"));
        requestParams.put("upperLimitDate", paramJobj.optString("upperLimitDate", null) == null ? "" : paramJobj.getString("upperLimitDate"));
        if (paramJobj.optString("isReceipt", null) != null) {
            requestParams.put("isReceipt", paramJobj.optString("isReceipt", null));
        }
        requestParams.put("filterForClaimedDateForPayment", paramJobj.optString("filterForClaimedDateForPayment", null) == null ? "" : paramJobj.getString("filterForClaimedDateForPayment"));
        requestParams.put("isDraft", (paramJobj.optString("isDraft", null) != null) ? Boolean.parseBoolean(paramJobj.getString("isDraft")) : false);
        requestParams.put("joborderitem", (paramJobj.optString("joborderitem",null) != null) ? Boolean.parseBoolean(paramJobj.optString("joborderitem")) : false);
        requestParams.put(Constants.CHART_TYPE, (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.CHART_TYPE))) ? paramJobj.optString(Constants.CHART_TYPE) : null); //added chart type in request map object
        requestParams.put("custInvFlagForSalesPerson", (paramJobj.optString("salesPersonFilterFlag", null) != null) ? Boolean.parseBoolean(paramJobj.getString("salesPersonFilterFlag")) : false);
        requestParams.put("includeAllRec", (paramJobj.optString("includeAllRec", null) != null) ? Boolean.parseBoolean(paramJobj.getString("includeAllRec")) : false);
        requestParams.put("salesPersonFilterFlag", (paramJobj.optString("salesPersonFilterFlag", null) != null) ? Boolean.parseBoolean(paramJobj.getString("salesPersonFilterFlag")) : false);
        requestParams.put(Constants.generatedSource, (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.generatedSource, null))) ? Integer.parseInt(paramJobj.optString(Constants.generatedSource, Constants.RECORD_WEB_Application)) : null);
        requestParams.put("noOfInterval",(paramJobj.has("noOfInterval") ? paramJobj.optInt("noOfInterval",7) : 7));

        if (!StringUtil.isNullOrEmpty(paramJobj.optString("cntype", null))) {
            requestParams.put("cntype", paramJobj.optString("cntype"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("requestModuleid", null))) {
            requestParams.put("requestModuleid", Integer.parseInt(paramJobj.optString("requestModuleid")));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("activeInactive", null))) {          //Getting the type of filter applied in recurring sales invoice report
            requestParams.put("activeInactive", paramJobj.optString("activeInactive"));
        }

        return requestParams;
    }
    
    public static JSONArray sortJsonById(JSONArray array) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject ja, JSONObject jb) {
                    String sr1 = "", sr2 = "";
                    try {
                        sr1 = (ja.optString("personid", "0"));
                        sr2 = (jb.optString("personid", "0"));
                    } catch (Exception ex) {
                        Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return sr1.compareTo(sr2);
                }
            });

        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new JSONArray(jsons);
    }

    public static JSONArray calculateSubtotal(HttpServletRequest request, JSONArray DataJArr) throws JSONException {
        double customertotal = 0;
        double total = 0;
        String temp = "";
        JSONArray array = new JSONArray();
        String headers[] = null;
        String subtotalCol = "";
        if (request.getParameter("header") != null) {
            String head = request.getParameter("header");
            headers = (String[]) head.split(",");
            int index = Arrays.asList(headers).indexOf("amountdueinbase");
            if(index > 0){
                subtotalCol = headers[index - 1];
            }
        } else {
            subtotalCol = "termname";
        }

        for (int i = 0; i < DataJArr.length(); i++) {
            JSONObject jSONObject = DataJArr.getJSONObject(i);
            String id = jSONObject.getString("personid");
            if (!StringUtil.isNullOrEmpty(temp)) {      // not first record
                if (id.equalsIgnoreCase(temp)) {        // same vendor/customer 
                    customertotal = customertotal + jSONObject.getDouble("amountdueinbase");
                    array.put(jSONObject);
                } else {        // customer sequence break
                    JSONObject jSONObject1 = new JSONObject();
                    jSONObject1.put(subtotalCol, "Subtotal");
                    jSONObject1.put("amountdueinbase", customertotal);
                    array.put(jSONObject1);             // put subtotal row 
                    customertotal = jSONObject.getDouble("amountdueinbase");
                    temp = jSONObject.getString("personid");
                    array.put(jSONObject);
                }
            } else {            // first object frm array
                temp = jSONObject.getString("personid");
                customertotal = customertotal + jSONObject.getDouble("amountdueinbase");
                array.put(jSONObject);
            }
            if (i == DataJArr.length() - 1) {       // put last record with its total
                JSONObject jSONObject1 = new JSONObject();
                jSONObject1.put(subtotalCol, "Subtotal");
                jSONObject1.put("amountdueinbase", customertotal);
                array.put(jSONObject1);             // put subtotal row 
            }
            total = total + jSONObject.getDouble("amountdueinbase");
        }
        JSONObject jSONObject2 = new JSONObject();
        jSONObject2.put(subtotalCol, "Total");
        jSONObject2.put("amountdueinbase", total);          // put final total
        array.put(jSONObject2);
        return array;
    }
}
