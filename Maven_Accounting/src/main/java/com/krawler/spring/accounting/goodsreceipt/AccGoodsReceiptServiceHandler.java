 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.goodsreceipt;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.ist.GRODetailISTMapping;
import com.krawler.inventory.model.ist.InterStoreTransferStatus;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants.SHIPDATE;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants.TAXPERCENT;
import com.krawler.spring.accounting.handler.AccLinkDataDao;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import com.krawler.spring.accounting.invoice.accInvoiceControllerCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.jasperreports.OnlyDatePojo;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;

/**
 *
 * @author krawler
 */
public class AccGoodsReceiptServiceHandler implements GoodsReceiptCMNConstants {
    
    private accPaymentDAO accPaymentDAOobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private fieldDataManager fieldDataManagercntrl;
    private AccLinkDataDao accLinkDataDao;
    private accBankReconciliationDAO accBankReconciliationObj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private fieldManagerDAO fieldManagerDAOobj;
    private accProductDAO accProductObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAO;
    
    private accTaxDAO accTaxObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accAccountDAO accAccountDAOobj;
    private StockService stockService;
    private accCurrencyDAO accCurrencyDAOobj;

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setstockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    
    public void setAccLinkDataDao(AccLinkDataDao accLinkDataDao) {
        this.accLinkDataDao = accLinkDataDao;
    }
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setaccGoodsReceiptServiceDAO(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAO) {
        this.accGoodsReceiptServiceDAO = accGoodsReceiptServiceDAO;
    }
    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }
    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public accProductDAO getAccProductObj() {
        return accProductObj;
    }

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    
    public static JSONArray getAgedOpeningBalanceInvoiceJson(HashMap<String, Object> requestParams, List list, JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, AccountingHandlerDAO accountingHandlerDAOobj, accGoodsReceiptCMN accGoodsReceiptCommon, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl) {
        try {
            boolean ignoreZero = requestParams.get(GoodsReceiptCMNConstants.IGNOREZERO) != null;
            DateFormat df = (DateFormat) requestParams.get(GoodsReceiptCMNConstants.DATEFORMAT);
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }
            }
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;// 0 = Invoice Due date OR 1 = Invoice date                                           
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get(GoodsReceiptCMNConstants.COMPANYID);
            boolean isFixedAsset = requestParams.containsKey("isFixedAsset") ? (Boolean) requestParams.get("isFixedAsset") : false;
            boolean isConsignment = requestParams.containsKey("isConsignment") ? (Boolean) requestParams.get("isConsignment") : false;
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, isFixedAsset ? Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId :isConsignment?Constants.Acc_Consignment_GoodsReceipt_ModuleId: Constants.Acc_Vendor_Invoice_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            Calendar oneDayBeforeCal1 = null;
            Calendar cal1 = null;
            Calendar cal2 = null;
            Calendar cal3 = null;
            Calendar cal4 = null;
            Calendar cal5 = null;
            Calendar cal6 = null;
            Calendar cal7 = null;
            Calendar cal8 = null;
            Calendar cal9 = null;
            Calendar cal10 = null;
            if (!(requestParams.containsKey("oneDayBeforeCal1") && requestParams.containsKey("cal1") && requestParams.containsKey("cal2") && requestParams.containsKey("cal3") && requestParams.containsKey("cal4") && requestParams.containsKey("cal5") && requestParams.containsKey("cal6") && requestParams.containsKey("cal7") && requestParams.containsKey("cal8") && requestParams.containsKey("cal9") && requestParams.containsKey("cal10"))) {
                oneDayBeforeCal1 = Calendar.getInstance();
                cal1 = Calendar.getInstance();
                cal2 = Calendar.getInstance();
                cal3 = Calendar.getInstance();
                cal4 = Calendar.getInstance();
                cal5 = Calendar.getInstance();
                cal6 = Calendar.getInstance();
                cal7 = Calendar.getInstance();
                cal8 = Calendar.getInstance();
                cal9 = Calendar.getInstance();
                cal10 = Calendar.getInstance();

                if (requestParams.get(Constants.asOfDate) != null) {
                    String curDateString = (String) requestParams.get(Constants.asOfDate);
                    Date curDate = null;
                    if (requestParams.get("MonthlyAgeingCurrDate") != null) {//This need to be checked.
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
            } else {
                oneDayBeforeCal1 = (Calendar) requestParams.get("oneDayBeforeCal1");
                cal1 = (Calendar) requestParams.get("cal1");
                cal2 = (Calendar) requestParams.get("cal2");
                cal3 = (Calendar) requestParams.get("cal3");
                cal4 = (Calendar) requestParams.get("cal4");
                cal5 = (Calendar) requestParams.get("cal5");
                cal6 = (Calendar) requestParams.get("cal6");
                cal7 = (Calendar) requestParams.get("cal7");
                cal8 = (Calendar) requestParams.get("cal8");
                cal9 = (Calendar) requestParams.get("cal9");
                cal10 = (Calendar) requestParams.get("cal10");
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

            Iterator it = null;
            if (list != null) {
                it = list.iterator();
                while (it.hasNext()) {

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

                    String grId = (String) it.next();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grId);
                    GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                    if (gReceipt != null) {
                        double openingBalanceAmountDue=0;
                        if (gReceipt.isIsOpeningBalenceInvoice() && requestParams.containsKey("isAgedPayables") && requestParams.get("isAgedPayables")!=null && Boolean.parseBoolean(requestParams.get("isAgedPayables").toString()) && (gReceipt.getOpeningBalanceAmountDue()==gReceipt.getOriginalOpeningBalanceAmount())) {
                            openingBalanceAmountDue=gReceipt.getOpeningBalanceAmountDue();
                        } else {
                            List ll = accGoodsReceiptCommon.getGRAmountDue(requestParams, gReceipt);
                            openingBalanceAmountDue = authHandler.round((Double) ll.get(1), companyid);
                        }
                        if (openingBalanceAmountDue > 0 || !ignoreZero) {
                            com.krawler.utils.json.base.JSONObject invoiceJson = new com.krawler.utils.json.base.JSONObject();
                            Date invoiceCreationDate = gReceipt.getCreationDate();
                            Date invoiceDueDate = gReceipt.getDueDate();
                            double exchangeRateForOtherCurrency = gReceipt.getExchangeRateForOpeningTransaction();
                            boolean isopeningBalanceInvoice = gReceipt.isIsOpeningBalenceInvoice();
                            boolean isConversionRateFromCurrencyToBase = gReceipt.isConversionRateFromCurrencyToBase();

                            invoiceJson.put(GoodsReceiptCMNConstants.BILLID, gReceipt.getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.BILLNO, gReceipt.getGoodsReceiptNumber());
                            invoiceJson.put(GoodsReceiptCMNConstants.JOURNALENTRYID, gReceipt.getJournalEntry() == null ? "" : gReceipt.getJournalEntry().getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.WITHOUTINVENTORY, false);
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, gReceipt.getCurrency() == null ? "" : gReceipt.getCurrency().getSymbol());
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYID, (gReceipt.getCurrency() == null ? "" : gReceipt.getCurrency().getCurrencyID()));
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYNAME, (gReceipt.getCurrency() == null ? "" : gReceipt.getCurrency().getName()));
                            invoiceJson.put(GoodsReceiptCMNConstants.ENTRYNO, "");//gReceipt.getJournalEntry()==null?"":gReceipt.getJournalEntry().getEntryNumber());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONID, gReceipt.getVendor() == null ? "" : gReceipt.getVendor().getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONNAME, gReceipt.getVendor() == null ? "" : gReceipt.getVendor().getName());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONINFO, gReceipt.getVendor() == null ? "" : gReceipt.getVendor().getName()+"("+gReceipt.getVendor().getAcccode()+")");
                            invoiceJson.put(GoodsReceiptCMNConstants.ALIASNAME, gReceipt.getVendor() == null ? "" : gReceipt.getVendor().getAliasname());
                            invoiceJson.put(GoodsReceiptCMNConstants.DUEDATE, df.format(invoiceDueDate));
                            invoiceJson.put(GoodsReceiptCMNConstants.DATE, df.format(invoiceCreationDate));
                            invoiceJson.put(GoodsReceiptCMNConstants.TERMNAME, gReceipt.getTermid() == null ? gReceipt.getVendor() == null ? "" : ((gReceipt.getVendor().getDebitTerm() == null) ? "" : gReceipt.getVendor().getDebitTerm().getTermname()) : gReceipt.getTermid().getTermname());
                            invoiceJson.put(GoodsReceiptCMNConstants.CustomerCreditTerm, gReceipt.getVendor() == null ? "" : ((gReceipt.getVendor().getDebitTerm() == null) ? "" : gReceipt.getVendor().getDebitTerm().getTermname()));
                            invoiceJson.put(GoodsReceiptCMNConstants.MEMO, gReceipt.getMemo() == null ? "" : gReceipt.getMemo());
                            invoiceJson.put(GoodsReceiptCMNConstants.DELETED, gReceipt.isDeleted());
                            invoiceJson.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, exchangeRateForOtherCurrency);
                            invoiceJson.put("isOpeningBalanceTransaction", isopeningBalanceInvoice);
                            invoiceJson.put(GoodsReceiptCMNConstants.IsConversionRateFromCurrencyToBase, gReceipt.isConversionRateFromCurrencyToBase());
                            invoiceJson.put(GoodsReceiptCMNConstants.ExchangeRate, "1 "+currency.getCurrencyCode()+" = "+((isopeningBalanceInvoice && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency )+" "+(gReceipt.getCurrency() == null ? "" : gReceipt.getCurrency().getCurrencyCode()));
                            invoiceJson.put("salespersonname", gReceipt.getMasterAgent() == null? "" : gReceipt.getMasterAgent().getValue());
                            KwlReturnObject bAmt = null;
                            double openingBalanceAmountDueInBase = 0d;
                            if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, openingBalanceAmountDue, gReceipt.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, openingBalanceAmountDue, gReceipt.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            }
                            openingBalanceAmountDueInBase = authHandler.round((Double) bAmt.getEntityList().get(0),companyid);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE, openingBalanceAmountDue);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, openingBalanceAmountDueInBase);
                            invoiceJson.put(GoodsReceiptCMNConstants.TYPE, "Purchase Invoice");
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTINBASE, gReceipt.getOriginalOpeningBalanceBaseAmount());
                            invoiceJson.put("code", gReceipt.getVendor() != null ? gReceipt.getVendor().getAcccode() :"");
                            double vendorCreditLimit = 0;
                            double vendorCreditLimitInbase = 0;
                            String currencyId = "";
                            if (gReceipt.getVendor() != null && gReceipt.getVendor().getCurrency() != null) {
                                currencyId = gReceipt.getVendor().getCurrency().getCurrencyID();
                                vendorCreditLimit = gReceipt.getVendor().getDebitlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, vendorCreditLimit, currencyId, gReceipt.getVendor().getCreatedOn(), 0);
                                vendorCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            }
                            invoiceJson.put("creditlimit", vendorCreditLimit);
                            invoiceJson.put("creditlimitinbase", vendorCreditLimitInbase);
                            Date dueDate = null;
                            if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                dueDate = df.parse(df.format(invoiceDueDate));
                            } else {
                                dueDate = df.parse(df.format(invoiceCreationDate));
                            }
                            Double amountdue = openingBalanceAmountDue;

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
                            
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE1, amountdue1);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE2, amountdue2);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE3, amountdue3);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE4, amountdue4);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE5, amountdue5);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE6, amountdue6);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE7, amountdue7);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE8, amountdue8);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE9, amountdue9);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE10, amountdue10);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE11, amountdue11);
                            
                            if (!requestParams.containsKey("isAgedPayables") || !(Boolean) requestParams.get("isAgedPayables")) {
                                getOpeningInvoiceCustomfield(companyid, gReceipt, requestParams, accountingHandlerDAOobj, replaceFieldMap, FieldMap, customFieldMap, customDateFieldMap, invoiceJson, fieldDataManagercntrl);
                            }
                            dataArray.put(invoiceJson);
                        }
                    }
                }
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }

    public static void getOpeningInvoiceCustomfield(String companyid, GoodsReceipt gReceipt, HashMap<String, Object> requestParams, AccountingHandlerDAO accountingHandlerDAOobj, HashMap<String, String> replaceFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, com.krawler.utils.json.base.JSONObject invoiceJson, fieldDataManager fieldDataManagercntrl) throws JSONException, ServiceException {
        KwlReturnObject custumObjresult = null;
        if (gReceipt.isIsOpeningBalenceInvoice()) {
            boolean isExport = (requestParams.get("isExport") == null) ? false : (Boolean) requestParams.get("isExport");
            Map<String, Object> variableMap = new HashMap<String, Object>();
            custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceVendorInvoiceCustomData.class.getName(), gReceipt.getID());
            replaceFieldMap = new HashMap<String, String>();
            if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                OpeningBalanceVendorInvoiceCustomData openingVICustom = (OpeningBalanceVendorInvoiceCustomData) custumObjresult.getEntityList().get(0);
                if (openingVICustom != null) {
                    AccountingManager.setCustomColumnValues(openingVICustom, FieldMap, replaceFieldMap, variableMap);
                    JSONObject params = new JSONObject();
                    params.put("companyid", companyid);
                    if (!isExport) {
                        isExport = (requestParams.get("isAgedPayables") == null) ? false : (Boolean) requestParams.get("isAgedPayables");
                    }
                    params.put("isExport", isExport);
                    if (requestParams.containsKey("browsertz") && requestParams.get("browsertz") != null) {
                        params.put("browsertz", requestParams.get("browsertz").toString());
                    }
                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, invoiceJson, params);
                }
            }
        }
    }

    public static JSONArray getAgedPayablesOpeningBalanceInvoiceJson(HashMap<String, Object> requestParams, List list, JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, AccountingHandlerDAO accountingHandlerDAOobj, accGoodsReceiptCMN accGoodsReceiptCommon) {
        try {
            DateFormat df = (DateFormat) requestParams.get(GoodsReceiptCMNConstants.DATEFORMAT);
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;// 0 = Invoice Due date OR 1 = Invoice date                                           
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())){ 
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
                }                
            }
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? 7 : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            Calendar oneDayBeforeCal1 = (Calendar) requestParams.get("oneDayBeforeCal1");
            Calendar cal1 = (Calendar) requestParams.get("cal1");
            Calendar cal2 = (Calendar) requestParams.get("cal2");
            Calendar cal3 = (Calendar) requestParams.get("cal3");
            Calendar cal4 = (Calendar) requestParams.get("cal4");
            Calendar cal5 = (Calendar) requestParams.get("cal5");
            Calendar cal6 = (Calendar) requestParams.get("cal6");
            Calendar cal7 = (Calendar) requestParams.get("cal7");
            Calendar cal8 = (Calendar) requestParams.get("cal8");
            Calendar cal9 = (Calendar) requestParams.get("cal9");
            Calendar cal10 = (Calendar) requestParams.get("cal10");

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
            
            Iterator it = null;
            if (list != null) {
                it = list.iterator();
                while (it.hasNext()) {
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
                    String grId = (String) it.next();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grId);
                    GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                    if (gReceipt != null) {
                        double openingBalanceAmountDue = 0;
                        if ((gReceipt.getOpeningBalanceAmountDue() == gReceipt.getOriginalOpeningBalanceAmount())) {
                            openingBalanceAmountDue = gReceipt.getOpeningBalanceAmountDue();
                        } else {
                            requestParams.put("invoiceAmtDueEqualsInvoiceAmt", false);
                            List ll = accGoodsReceiptCommon.getGRAmountDue(requestParams, gReceipt);
                            openingBalanceAmountDue = authHandler.round((Double) ll.get(1), companyid);
                        }
                        if (openingBalanceAmountDue > 0) {
                            com.krawler.utils.json.base.JSONObject invoiceJson = new com.krawler.utils.json.base.JSONObject();
                            Date invoiceCreationDate = gReceipt.getCreationDate();
                            Date invoiceDueDate = gReceipt.getDueDate();
                            double exchangeRateForOtherCurrency = gReceipt.getExchangeRateForOpeningTransaction();
                            boolean isopeningBalanceInvoice = gReceipt.isIsOpeningBalenceInvoice();
                            boolean isConversionRateFromCurrencyToBase = gReceipt.isConversionRateFromCurrencyToBase();

                            invoiceJson.put(GoodsReceiptCMNConstants.BILLID, gReceipt.getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.BILLNO, gReceipt.getGoodsReceiptNumber());
                            invoiceJson.put(GoodsReceiptCMNConstants.JOURNALENTRYID, gReceipt.getJournalEntry() == null ? "" : gReceipt.getJournalEntry().getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.WITHOUTINVENTORY, false);
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, gReceipt.getCurrency() == null ? "" : gReceipt.getCurrency().getSymbol());
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYID, (gReceipt.getCurrency() == null ? "" : gReceipt.getCurrency().getCurrencyID()));
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYNAME, (gReceipt.getCurrency() == null ? "" : gReceipt.getCurrency().getName()));
                            invoiceJson.put(GoodsReceiptCMNConstants.ENTRYNO, "");//gReceipt.getJournalEntry()==null?"":gReceipt.getJournalEntry().getEntryNumber());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONID, gReceipt.getVendor() == null ? "" : gReceipt.getVendor().getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONNAME, gReceipt.getVendor() == null ? "" : gReceipt.getVendor().getName());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONINFO, gReceipt.getVendor() == null ? "" : gReceipt.getVendor().getName()+"("+gReceipt.getVendor().getAcccode()+")");
                            invoiceJson.put(GoodsReceiptCMNConstants.ALIASNAME, gReceipt.getVendor() == null ? "" : gReceipt.getVendor().getAliasname());
                            invoiceJson.put(GoodsReceiptCMNConstants.DUEDATE, df.format(invoiceDueDate));
                            invoiceJson.put(GoodsReceiptCMNConstants.DATE, df.format(invoiceCreationDate));
                            invoiceJson.put(GoodsReceiptCMNConstants.TERMNAME, gReceipt.getTermid() == null ? "" : gReceipt.getTermid().getTermname());
                            invoiceJson.put(GoodsReceiptCMNConstants.MEMO, gReceipt.getMemo() == null ? "" : gReceipt.getMemo());
                            invoiceJson.put(GoodsReceiptCMNConstants.DELETED, gReceipt.isDeleted());
                            invoiceJson.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, exchangeRateForOtherCurrency);
                            invoiceJson.put(GoodsReceiptCMNConstants.CODE, gReceipt.getVendor() == null ? "" : (gReceipt.getVendor().getAcccode() == null ? "" : gReceipt.getVendor().getAcccode()));
                            invoiceJson.put(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, isopeningBalanceInvoice);
                            invoiceJson.put(GoodsReceiptCMNConstants.IsConversionRateFromCurrencyToBase, gReceipt.isConversionRateFromCurrencyToBase());
                            invoiceJson.put(GoodsReceiptCMNConstants.ExchangeRate, "1 "+currency.getCurrencyCode()+" = "+((isopeningBalanceInvoice && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency )+" "+(gReceipt.getCurrency() == null ? "" : gReceipt.getCurrency().getCurrencyCode()));
                            invoiceJson.put(GoodsReceiptCMNConstants.CustomerCreditTerm,gReceipt.getVendor() == null ? "" : gReceipt.getVendor().getDebitTerm().getTermname());
                            KwlReturnObject bAmt = null;
                            double openingBalanceAmountDueInBase = 0d;
                            if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, openingBalanceAmountDue, gReceipt.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, openingBalanceAmountDue, gReceipt.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            }
                            openingBalanceAmountDueInBase = authHandler.round((Double) bAmt.getEntityList().get(0),companyid);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE, openingBalanceAmountDue);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, openingBalanceAmountDueInBase);
                            invoiceJson.put(GoodsReceiptCMNConstants.TYPE, "Purchase Invoice");
                            Date dueDate = null;
                            if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                dueDate = df.parse(df.format(invoiceDueDate));
                            } else {
                                dueDate = df.parse(df.format(invoiceCreationDate));
                            }
                            Double amountdue = openingBalanceAmountDue;
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
                            
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE1, amountdue1);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE2, amountdue2);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE3, amountdue3);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE4, amountdue4);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE5, amountdue5);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE6, amountdue6);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE7, amountdue7);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE8, amountdue8);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE9, amountdue9);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE10, amountdue10);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE11, amountdue11);
//                            invoiceJson.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
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

    public static JSONArray getAgedOpeningBalanceDebitNoteJson(HashMap<String, Object> requestParams, List list, JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, AccountingHandlerDAO accountingHandlerDAOobj, accReceiptDAO accReceiptDao, accDebitNoteDAO accDebitNoteobj, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl) {
        try {
            boolean ignoreZero = requestParams.get(GoodsReceiptCMNConstants.IGNOREZERO) != null;
            DateFormat df = (DateFormat) requestParams.get(GoodsReceiptCMNConstants.DATEFORMAT);
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
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String companyid = (String) requestParams.get(GoodsReceiptCMNConstants.COMPANYID);

            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Debit_Note_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            Calendar oneDayBeforeCal1 = null;
            Calendar cal1 = null;
            Calendar cal2 = null;
            Calendar cal3 = null;
            Calendar cal4 = null;
            Calendar cal5 = null;
            Calendar cal6 = null;
            Calendar cal7 = null;
            Calendar cal8 = null;
            Calendar cal9 = null;
            Calendar cal10 = null;
            if (!(requestParams.containsKey("oneDayBeforeCal1") && requestParams.containsKey("cal1") && requestParams.containsKey("cal2") && requestParams.containsKey("cal3") && requestParams.containsKey("cal4") && requestParams.containsKey("cal5") && requestParams.containsKey("cal6") && requestParams.containsKey("cal7") && requestParams.containsKey("cal8") && requestParams.containsKey("cal9") && requestParams.containsKey("cal10"))) {
                oneDayBeforeCal1 = Calendar.getInstance();
                cal1 = Calendar.getInstance();
                cal2 = Calendar.getInstance();
                cal3 = Calendar.getInstance();
                cal4 = Calendar.getInstance();
                cal5 = Calendar.getInstance();
                cal6 = Calendar.getInstance();
                cal7 = Calendar.getInstance();
                cal8 = Calendar.getInstance();
                cal9 = Calendar.getInstance();
                cal10 = Calendar.getInstance();

                if (requestParams.get(Constants.asOfDate) != null) {
                    String curDateString = (String) requestParams.get(Constants.asOfDate);
                    Date curDate = null;
                    if (requestParams.get("MonthlyAgeingCurrDate") != null) {
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
            } else {
                oneDayBeforeCal1 = (Calendar) requestParams.get("oneDayBeforeCal1");
                cal1 = (Calendar) requestParams.get("cal1");
                cal2 = (Calendar) requestParams.get("cal2");
                cal3 = (Calendar) requestParams.get("cal3");
                cal4 = (Calendar) requestParams.get("cal4");
                cal5 = (Calendar) requestParams.get("cal5");
                cal6 = (Calendar) requestParams.get("cal6");
                cal7 = (Calendar) requestParams.get("cal7");
                cal8 = (Calendar) requestParams.get("cal8");
                cal9 = (Calendar) requestParams.get("cal9");
                cal10 = (Calendar) requestParams.get("cal10");
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

            Iterator it = null;
            if (list != null) {
                it = list.iterator();
                while (it.hasNext()) {

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

                    DebitNote dn = (DebitNote) it.next();
                    if (dn != null) {
//                        System.out.println("Opening DN="+dn.getDebitNoteNumber());
                        Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.INFO, "Opening DN=" + dn.getDebitNoteNumber());
                        requestParams.put("debitnoteid", dn.getID());
                        double dnReceiptAmount = 0;
                        KwlReturnObject dnpResult = accReceiptDao.getDebitNotePaymentDetail(requestParams);
                        if (!dnpResult.getEntityList().isEmpty()) {
                            Iterator dnpItr = dnpResult.getEntityList().iterator();
                            while (dnpItr.hasNext()) {
                                Object[] objects = (Object[]) dnpItr.next();
                                double dnPaidAmtInDNCurrency = objects[2] != null ? (Double) objects[2] : 0.0;
                                //dnReceiptAmount += authHandler.round(dnPaidAmtInReceiptCurrency / exchangeratefortransaction, Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
                                dnReceiptAmount += authHandler.round(dnPaidAmtInDNCurrency,companyid);
                            }
                        }

                        double invReturnAmt = 0;
                        KwlReturnObject dnresult = accDebitNoteobj.getDNRowsFromVendorInvoice(requestParams);
                        List<DebitNoteDetail> rows =dnresult.getEntityList(); 
                        for (DebitNoteDetail detail : rows) {
                            Discount disc = detail.getDiscount();
                            if (disc != null) {
                                invReturnAmt += disc.getDiscountValue();
                            }
                        }
                        /*
                         * Including Debit Note Amount which is Linked to Credit Note
                         */
                        double cnReturnAmt = 0;
                        dnresult = accDebitNoteobj.getDNRowsFromCreditNote(requestParams);
                        rows =dnresult.getEntityList(); 
                        for (DebitNoteDetail detail : rows) {
                            Discount disc = detail.getDiscount();
                            if (disc != null) {
                                cnReturnAmt += disc.getDiscountValue();
                            }
                        }
                        double amountdue = dn.getDnamount() - (dnReceiptAmount + invReturnAmt+cnReturnAmt);

                        requestParams.remove("debitnoteid");//Removing debitnoteid after use, So that is does not affect other  
                        if (amountdue > 0 || !ignoreZero) {
                            com.krawler.utils.json.base.JSONObject invoiceJson = new com.krawler.utils.json.base.JSONObject();

                            double exchangeRateForOtherCurrency = 0d;
                            boolean isopeningBalanceDN = dn.isIsOpeningBalenceDN();
                            boolean isConversionRateFromCurrencyToBase = dn.isConversionRateFromCurrencyToBase();

                            Date invoiceCreationDate = null;
                            invoiceCreationDate = dn.getCreationDate();
                            amountdue = -amountdue; //Amount due will be negative in case of debit note
                            exchangeRateForOtherCurrency = dn.getExchangeRateForOpeningTransaction();
                            invoiceJson.put(GoodsReceiptCMNConstants.BILLID, dn.getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.BILLNO, dn.getDebitNoteNumber());
                            invoiceJson.put(GoodsReceiptCMNConstants.JOURNALENTRYID, dn.getJournalEntry() == null ? "" : dn.getJournalEntry().getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.WITHOUTINVENTORY, false);
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, dn.getCurrency() == null ? "" : dn.getCurrency().getSymbol());
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYID, (dn.getCurrency() == null ? "" : dn.getCurrency().getCurrencyID()));
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYNAME, (dn.getCurrency() == null ? "" : dn.getCurrency().getName()));
                            invoiceJson.put(GoodsReceiptCMNConstants.ENTRYNO, "");
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONID, dn.getVendor() == null ? "" : dn.getVendor().getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONNAME, dn.getVendor() == null ? "" : dn.getVendor().getName());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONINFO, dn.getVendor() == null ? "" : dn.getVendor().getName()+"("+dn.getVendor().getAcccode()+")");
                            invoiceJson.put(GoodsReceiptCMNConstants.ALIASNAME, dn.getVendor() == null ? "" : dn.getVendor().getAliasname());
                            invoiceJson.put(GoodsReceiptCMNConstants.CustomerCreditTerm, dn.getVendor() == null ? "" : dn.getVendor().getDebitTerm().getTermname());
                            KwlReturnObject bAmt = null;
                            double amountdueinbase = 0d;
                            if (isopeningBalanceDN && dn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, dn.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dn.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            }
                            amountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0),companyid);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE, amountdue);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, amountdueinbase);
                            invoiceJson.put(GoodsReceiptCMNConstants.DUEDATE, df.format(dn.getCreationDate()));
                            invoiceJson.put(GoodsReceiptCMNConstants.DATE, df.format(dn.getCreationDate()));
                            invoiceJson.put(GoodsReceiptCMNConstants.MEMO, dn.getMemo() == null ? "" : dn.getMemo());
                            invoiceJson.put(GoodsReceiptCMNConstants.DELETED, dn.isDeleted());
                            invoiceJson.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, exchangeRateForOtherCurrency);
                            invoiceJson.put("isOpeningBalanceTransaction", isopeningBalanceDN);
                            invoiceJson.put(GoodsReceiptCMNConstants.IsConversionRateFromCurrencyToBase, dn.isConversionRateFromCurrencyToBase());
                            invoiceJson.put(GoodsReceiptCMNConstants.TYPE, "Debit Note");
                            invoiceJson.put("isDN",true);
                            invoiceJson.put(GoodsReceiptCMNConstants.ExchangeRate, "1 "+currency.getCurrencyCode()+" = "+((isopeningBalanceDN && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency )+" "+(dn.getCurrency() == null ? "" : dn.getCurrency().getCurrencyCode()));
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTINBASE, dn.getOriginalOpeningBalanceBaseAmount());
                            invoiceJson.put("code", dn.getVendor() != null ? dn.getVendor().getAcccode() : "");
                            invoiceJson.put("salespersonname", dn.getSalesPerson() == null? "" : dn.getSalesPerson().getValue());
                            double vendorCreditLimit = 0;
                            double vendorCreditLimitInbase = 0;
                            String currencyId = "";
                            if (dn.getVendor() != null && dn.getVendor().getCurrency() != null) {
                                currencyId = dn.getVendor().getCurrency().getCurrencyID();
                                vendorCreditLimit = dn.getVendor().getDebitlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, vendorCreditLimit, currencyId, dn.getVendor().getCreatedOn(), 0);
                                vendorCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            }
                            invoiceJson.put("creditlimit", vendorCreditLimit);
                            invoiceJson.put("creditlimitinbase", vendorCreditLimitInbase);
                            Date dueDate = df.parse(df.format(dn.getCreationDate()));

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

                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE1, amountdue1);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE2, amountdue2);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE3, amountdue3);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE4, amountdue4);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE5, amountdue5);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE6, amountdue6);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE7, amountdue7);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE8, amountdue8);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE9, amountdue9);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE10, amountdue10);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE11, amountdue11);
//                            invoiceJson.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
                            
                            //If Condition 
                            if (!requestParams.containsKey("isAgedPayables") || !(Boolean) requestParams.get("isAgedPayables")) {
                                getOpeningDebitNoteCustomField(companyid, dn, requestParams, accountingHandlerDAOobj, replaceFieldMap, FieldMap, customFieldMap, customDateFieldMap, invoiceJson, fieldDataManagercntrl);
                            }
                            dataArray.put(invoiceJson);
                        }
                    }
                }
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }

    public static void getOpeningDebitNoteCustomField(String companyid, DebitNote dn, HashMap<String, Object> requestParams, AccountingHandlerDAO accountingHandlerDAOobj, HashMap<String, String> replaceFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, com.krawler.utils.json.base.JSONObject invoiceJson, fieldDataManager fieldDataManagercntrl) throws JSONException, ServiceException {
        KwlReturnObject custumObjresult = null;
        if (dn.isIsOpeningBalenceDN()) {
            boolean isExport = (requestParams.get("isExport") == null) ? false : (Boolean) requestParams.get("isExport");
            Map<String, Object> variableMap = new HashMap<String, Object>();
            custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceDebitNoteCustomData.class.getName(), dn.getID());
            replaceFieldMap = new HashMap<String, String>();
            if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                OpeningBalanceDebitNoteCustomData openingDNCustom = (OpeningBalanceDebitNoteCustomData) custumObjresult.getEntityList().get(0);
                if (openingDNCustom != null) {
                    AccountingManager.setCustomColumnValues(openingDNCustom, FieldMap, replaceFieldMap, variableMap);
                    JSONObject params = new JSONObject();
                    params.put("companyid", companyid);
                    if (!isExport) {
                        isExport = (requestParams.get("isAgedPayables") == null) ? false : (Boolean) requestParams.get("isAgedPayables");
                    }
                    params.put("isExport", isExport);
                    if (requestParams.containsKey("browsertz") && requestParams.get("browsertz") != null) {
                        params.put("browsertz", requestParams.get("browsertz").toString());
                    }
                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, invoiceJson, params);
                }
            }
        }

    }

    public static JSONArray getAgedOpeningBalanceCreditNoteJson(HashMap<String, Object> requestParams, List list, JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, AccountingHandlerDAO accountingHandlerDAOobj, accPaymentDAO accPaymentDao,accVendorPaymentDAO accVendorPaymentobj,accDebitNoteDAO accDebitNoteobj,accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl) {
        try {
            boolean ignoreZero = requestParams.get(GoodsReceiptCMNConstants.IGNOREZERO) != null;
            DateFormat df = (DateFormat) requestParams.get(GoodsReceiptCMNConstants.DATEFORMAT);
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
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String companyid = (String) requestParams.get(GoodsReceiptCMNConstants.COMPANYID);

            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Credit_Note_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            Calendar oneDayBeforeCal1 = null;
            Calendar cal1 = null;
            Calendar cal2 = null;
            Calendar cal3 = null;
            Calendar cal4 = null;
            Calendar cal5 = null;
            Calendar cal6 = null;
            Calendar cal7 = null;
            Calendar cal8 = null;
            Calendar cal9 = null;
            Calendar cal10 = null;
            if (!(requestParams.containsKey("oneDayBeforeCal1") && requestParams.containsKey("cal1") && requestParams.containsKey("cal2") && requestParams.containsKey("cal3") && requestParams.containsKey("cal4") && requestParams.containsKey("cal5") && requestParams.containsKey("cal6") && requestParams.containsKey("cal7") && requestParams.containsKey("cal8") && requestParams.containsKey("cal9") && requestParams.containsKey("cal10"))) {
                oneDayBeforeCal1 = Calendar.getInstance();
                cal1 = Calendar.getInstance();
                cal2 = Calendar.getInstance();
                cal3 = Calendar.getInstance();
                cal4 = Calendar.getInstance();
                cal5 = Calendar.getInstance();
                cal6 = Calendar.getInstance();
                cal7 = Calendar.getInstance();
                cal8 = Calendar.getInstance();
                cal9 = Calendar.getInstance();
                cal10 = Calendar.getInstance();

                if (requestParams.get(Constants.asOfDate) != null) {
                    String curDateString = (String) requestParams.get(Constants.asOfDate);
                    Date curDate = null;
                    if (requestParams.get("MonthlyAgeingCurrDate") != null) {
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
            } else {
                oneDayBeforeCal1 = (Calendar) requestParams.get("oneDayBeforeCal1");
                cal1 = (Calendar) requestParams.get("cal1");
                cal2 = (Calendar) requestParams.get("cal2");
                cal3 = (Calendar) requestParams.get("cal3");
                cal4 = (Calendar) requestParams.get("cal4");
                cal5 = (Calendar) requestParams.get("cal5");
                cal6 = (Calendar) requestParams.get("cal6");
                cal7 = (Calendar) requestParams.get("cal7");
                cal8 = (Calendar) requestParams.get("cal8");
                cal9 = (Calendar) requestParams.get("cal9");
                cal10 = (Calendar) requestParams.get("cal10");
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

          
            Iterator it = null;
            if (list != null && !list.isEmpty()) {
                it = list.iterator();
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
//                        System.out.println("Opening CN="+cn.getCreditNoteNumber());
                        Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.INFO, "Opening CN=" + cn.getCreditNoteNumber());
                        double cnPaidAmount = 0;
                        double amountdue = 0;
                        requestParams.put("creditnoteid", cn.getID());
                        KwlReturnObject cnpResult = accPaymentDao.getCreditNotePaymentDetails(requestParams);
                        if (!cnpResult.getEntityList().isEmpty()) {
                            Iterator cnpItr = cnpResult.getEntityList().iterator();
                            while (cnpItr.hasNext()) {
                                Object[] objects = (Object[]) cnpItr.next();
//                                double exchangeratefortransaction = objects[7] != null ? (Double) objects[7] : 1.0;
                                double cnPaidAmtInCNCurrency = objects[2] != null ? (Double) objects[2] : 0.0;   // ojects[4] gives us amount in CN currency, so no need to divide/multiple by exchange rate
                                // cnPaidAmount += authHandler.round(cnPaidAmtInReceiptCurrency / exchangeratefortransaction, Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
                                cnPaidAmount += authHandler.round(cnPaidAmtInCNCurrency, companyid);
                            }
                        }

                        double linkAmount = 0;
                        KwlReturnObject cnLinkResult = accVendorPaymentobj.getLinkDetailPaymentToCreditNote(requestParams);
                        List<LinkDetailPaymentToCreditNote> linkDetail = cnLinkResult.getEntityList();
                        for (LinkDetailPaymentToCreditNote ldr : linkDetail) {
                            linkAmount += ldr.getAmountInCNCurrency();
                        }

                        /*
                         * Get DN Linked amount in CN
                         */
                        double dnlinkAmt = 0;
                        KwlReturnObject dnresult = accDebitNoteobj.getDNRowsFromCreditNote(requestParams);
                        List<DebitNoteDetail> rows = dnresult.getEntityList();
                        for (DebitNoteDetail detail : rows) {
                            double exchangeRate = detail.getExchangeRateForTransaction();
                            Discount disc = detail.getDiscount();
                            if (disc != null) {
                                //When currency of CN and DN is different, if currency is same then exchangeRate=1, case exchangeRate!=0 is applied for avaoiding AE
                                if(exchangeRate!=1 && exchangeRate!=0){
                                   dnlinkAmt += authHandler.round(disc.getDiscountValue()/exchangeRate, companyid); 
                                } else{
                                    dnlinkAmt += disc.getDiscountValue();
                                }
                            }
                        }

                        requestParams.remove("creditnoteid");//Removing creditnoteid after use, So that is does not affect other  
                        amountdue = cn.getCnamount() - (cnPaidAmount +linkAmount+dnlinkAmt);
                        if (amountdue > 0 || !ignoreZero) {
                            com.krawler.utils.json.base.JSONObject invoiceJson = new com.krawler.utils.json.base.JSONObject();
                            double exchangeRateForOtherCurrency = 0d;
                            Date invoiceCreationDate = null;
                            invoiceCreationDate = cn.getCreationDate();
                            exchangeRateForOtherCurrency = cn.getExchangeRateForOpeningTransaction();
                            boolean isopeningBalanceTransaction = cn.isIsOpeningBalenceCN();
                            boolean isConversionRateFromCurrencyToBase = cn.isConversionRateFromCurrencyToBase();
                            invoiceJson.put(GoodsReceiptCMNConstants.BILLID, cn.getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.BILLNO, cn.getCreditNoteNumber());
                            invoiceJson.put(GoodsReceiptCMNConstants.JOURNALENTRYID, cn.getJournalEntry() == null ? "" : cn.getJournalEntry().getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.WITHOUTINVENTORY, false);
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, cn.getCurrency() == null ? "" : cn.getCurrency().getSymbol());
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYID, (cn.getCurrency() == null ? "" : cn.getCurrency().getCurrencyID()));
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYNAME, (cn.getCurrency() == null ? "" : cn.getCurrency().getName()));
                            invoiceJson.put(GoodsReceiptCMNConstants.ENTRYNO, "");//gReceipt.getJournalEntry()==null?"":gReceipt.getJournalEntry().getEntryNumber());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONID, cn.getVendor() == null ? "" : cn.getVendor().getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONNAME, cn.getVendor() == null ? "" : cn.getVendor().getName());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONINFO, cn.getVendor() == null ? "" : cn.getVendor().getName()+"("+cn.getVendor().getAcccode()+")");
                            invoiceJson.put(GoodsReceiptCMNConstants.ALIASNAME, cn.getVendor() == null ? "" : cn.getVendor().getAliasname());
                            invoiceJson.put(GoodsReceiptCMNConstants.DUEDATE, df.format(cn.getCreationDate()));
                            invoiceJson.put(GoodsReceiptCMNConstants.DATE, df.format(cn.getCreationDate()));
                            invoiceJson.put(GoodsReceiptCMNConstants.MEMO, cn.getMemo() == null ? "" : cn.getMemo());
                            invoiceJson.put(GoodsReceiptCMNConstants.DELETED, cn.isDeleted());
                            invoiceJson.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, exchangeRateForOtherCurrency);
                            invoiceJson.put(GoodsReceiptCMNConstants.CustomerCreditTerm, cn.getVendor() == null ? "" : ((cn.getVendor().getDebitTerm() == null) ? "" : cn.getVendor().getDebitTerm().getTermname()));
                            invoiceJson.put("isOpeningBalanceTransaction", isopeningBalanceTransaction);
                            invoiceJson.put(GoodsReceiptCMNConstants.IsConversionRateFromCurrencyToBase, isConversionRateFromCurrencyToBase);
                            invoiceJson.put(GoodsReceiptCMNConstants.ExchangeRate, "1 "+currency.getCurrencyCode()+" = "+((isopeningBalanceTransaction && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency )+" "+(cn.getCurrency() == null ? "" : cn.getCurrency().getCurrencyCode()));
                            KwlReturnObject bAmt = null;
                            double amountdueinbase = 0d;
                            if (isopeningBalanceTransaction && isConversionRateFromCurrencyToBase) {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, cn.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cn.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            }
                            double vendorCreditLimit = 0;
                            double vendorCreditLimitInbase = 0;
                            String currencyId = "";
                            if (cn.getVendor() != null && cn.getVendor().getCurrency() != null) {
                                currencyId = cn.getVendor().getCurrency().getCurrencyID();
                                vendorCreditLimit = cn.getVendor().getDebitlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, vendorCreditLimit, currencyId, cn      .getVendor().getCreatedOn(), 0);
                                vendorCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            }
                            invoiceJson.put("creditlimit", vendorCreditLimit);
                            invoiceJson.put("creditlimitinbase", vendorCreditLimitInbase);
                            amountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0),companyid);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE, amountdue);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, amountdueinbase);
                            invoiceJson.put(GoodsReceiptCMNConstants.TYPE, "Credit Note");
                            invoiceJson.put("isCN", true);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTINBASE, cn.getOriginalOpeningBalanceBaseAmount());
                            invoiceJson.put("code", cn.getVendor() != null ? cn.getVendor().getAcccode() : "");
                            invoiceJson.put("salespersonname", cn.getSalesPerson() == null?(cn.getMasterAgent() == null? "" : cn.getMasterAgent().getValue()) : cn.getSalesPerson().getValue());
                            Date dueDate = df.parse(df.format(cn.getCreationDate()));

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
                            
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE1, amountdue1);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE2, amountdue2);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE3, amountdue3);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE4, amountdue4);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE5, amountdue5);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE6, amountdue6);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE7, amountdue7);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE8, amountdue8);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE9, amountdue9);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE10, amountdue10);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE11, amountdue11);
//                            invoiceJson.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
                            //If Condition
                            if (!requestParams.containsKey("isAgedPayables") || !(Boolean) requestParams.get("isAgedPayables")) {
                                getOpeningCreditNoteCustomField(companyid, cn, requestParams, accountingHandlerDAOobj, replaceFieldMap, FieldMap, customFieldMap, customDateFieldMap, invoiceJson, fieldDataManagercntrl);
                            }
                            dataArray.put(invoiceJson);
                        }
                    }
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }

    public static void getOpeningCreditNoteCustomField(String companyid, CreditNote cn, HashMap<String, Object> requestParams, AccountingHandlerDAO accountingHandlerDAOobj, HashMap<String, String> replaceFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, com.krawler.utils.json.base.JSONObject invoiceJson, fieldDataManager fieldDataManagercntrl) throws JSONException, ServiceException {
        KwlReturnObject custumObjresult = null;
        if (cn.isIsOpeningBalenceCN()) {
            boolean isExport = (requestParams.get("isExport") == null) ? false : (Boolean) requestParams.get("isExport");
            Map<String, Object> variableMap = new HashMap<String, Object>();
            custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceCreditNoteCustomData.class.getName(), cn.getID());
            replaceFieldMap = new HashMap<String, String>();
            if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                OpeningBalanceCreditNoteCustomData openingCNCustom = (OpeningBalanceCreditNoteCustomData) custumObjresult.getEntityList().get(0);
                if (openingCNCustom != null) {
                    AccountingManager.setCustomColumnValues(openingCNCustom, FieldMap, replaceFieldMap, variableMap);
                    JSONObject params = new JSONObject();
                    params.put("companyid", companyid);
                    if (!isExport) {
                        isExport = (requestParams.get("isAgedPayables") == null) ? false : (Boolean) requestParams.get("isAgedPayables");
                    }
                    params.put("isExport", isExport);
                    if (requestParams.containsKey("browsertz") && requestParams.get("browsertz") != null) {
                        params.put("browsertz", requestParams.get("browsertz").toString());
                    }
                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, invoiceJson, params);
                }
            }
        }
    }

    public static JSONArray getAgedOpeningBalancePaymentJson(HashMap<String, Object> requestParams, List list, JSONArray dataArray, accCurrencyDAO accCurrencyDAOobj, AccountingHandlerDAO accountingHandlerDAOobj, accVendorPaymentDAO accVendorPaymentobj, accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl) {
        try {
            boolean ignoreZero = requestParams.get(GoodsReceiptCMNConstants.IGNOREZERO) != null;
            DateFormat df = (DateFormat) requestParams.get(GoodsReceiptCMNConstants.DATEFORMAT);
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
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            String companyid= (String) requestParams.get(GoodsReceiptCMNConstants.COMPANYID);
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            Calendar oneDayBeforeCal1 = null;
            Calendar cal1 = null;
            Calendar cal2 = null;
            Calendar cal3 = null;
            Calendar cal4 = null;
            Calendar cal5 = null;
            Calendar cal6 = null;
            Calendar cal7 = null;
            Calendar cal8 = null;
            Calendar cal9 = null;
            Calendar cal10 = null;
            if (!(requestParams.containsKey("oneDayBeforeCal1") && requestParams.containsKey("cal1") && requestParams.containsKey("cal2") && requestParams.containsKey("cal3") && requestParams.containsKey("cal4") && requestParams.containsKey("cal5") && requestParams.containsKey("cal6") && requestParams.containsKey("cal7") && requestParams.containsKey("cal8") && requestParams.containsKey("cal9") && requestParams.containsKey("cal10"))) {
                oneDayBeforeCal1 = Calendar.getInstance();
                cal1 = Calendar.getInstance();
                cal2 = Calendar.getInstance();
                cal3 = Calendar.getInstance();
                cal4 = Calendar.getInstance();
                cal5 = Calendar.getInstance();
                cal6 = Calendar.getInstance();
                cal7 = Calendar.getInstance();
                cal8 = Calendar.getInstance();
                cal9 = Calendar.getInstance();
                cal10 = Calendar.getInstance();

                if (requestParams.get(Constants.asOfDate) != null) {
                    String curDateString = (String) requestParams.get(Constants.asOfDate);
                    Date curDate = null;
                    if (requestParams.get("MonthlyAgeingCurrDate") != null) {
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
            } else {
                oneDayBeforeCal1 = (Calendar) requestParams.get("oneDayBeforeCal1");
                cal1 = (Calendar) requestParams.get("cal1");
                cal2 = (Calendar) requestParams.get("cal2");
                cal3 = (Calendar) requestParams.get("cal3");
                cal4 = (Calendar) requestParams.get("cal4");
                cal5 = (Calendar) requestParams.get("cal5");
                cal6 = (Calendar) requestParams.get("cal6");
                cal7 = (Calendar) requestParams.get("cal7");
                cal8 = (Calendar) requestParams.get("cal8");
                cal9 = (Calendar) requestParams.get("cal9");
                cal10 = (Calendar) requestParams.get("cal10");
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

            Iterator it = null;
            if (list != null) {
                it = list.iterator();
                while (it.hasNext()) {

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

                    Payment payment = (Payment) it.next();
                    if (payment != null) {
                        Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.INFO, "Opening Payment=" + payment.getPaymentNumber());
//                        System.out.println("Opening Payment="+payment.getPaymentNumber());
                        HashMap<String, Object> reqParams1 = new HashMap();
                        reqParams1.put("paymentid", payment.getID());
                        reqParams1.put("companyid", companyid);
                        reqParams1.put(Constants.df, df);
                        if (requestParams.containsKey("asofdate") && requestParams.get("asofdate") != null) {
                            reqParams1.put("asofdate", requestParams.get("asofdate"));
                        }
                        double amountdue = getOpeningPaymentAmountDue(payment, reqParams1, accVendorPaymentobj, companyid);
                        if (amountdue != 0 || !ignoreZero) {

                            com.krawler.utils.json.base.JSONObject invoiceJson = new com.krawler.utils.json.base.JSONObject();

                            double exchangeRateForOtherCurrency = 0d;
                            boolean isopeningBalancePayment = payment.isIsOpeningBalencePayment();
                            boolean isConversionRateFromCurrencyToBase = payment.isConversionRateFromCurrencyToBase();

                            Date invoiceCreationDate = null;
                            invoiceCreationDate = payment.getCreationDate();
                            exchangeRateForOtherCurrency = payment.getExchangeRateForOpeningTransaction();
                            invoiceJson.put(GoodsReceiptCMNConstants.BILLID, payment.getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.BILLNO, payment.getPaymentNumber());
                            invoiceJson.put(GoodsReceiptCMNConstants.JOURNALENTRYID, payment.getJournalEntry() == null ? "" : payment.getJournalEntry().getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.WITHOUTINVENTORY, false);
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, payment.getCurrency() == null ? "" : payment.getCurrency().getSymbol());
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYID, (payment.getCurrency() == null ? "" : payment.getCurrency().getCurrencyID()));
                            invoiceJson.put(GoodsReceiptCMNConstants.CURRENCYNAME, (payment.getCurrency() == null ? "" : payment.getCurrency().getName()));
                            invoiceJson.put(GoodsReceiptCMNConstants.ENTRYNO, "");
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONID, payment.getVendor() == null ? "" : payment.getVendor().getID());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONNAME, payment.getVendor() == null ? "" : payment.getVendor().getName());
                            invoiceJson.put(GoodsReceiptCMNConstants.PERSONINFO, payment.getVendor() == null ? "" : payment.getVendor().getName()+"("+payment.getVendor().getAcccode()+")");
                            invoiceJson.put(GoodsReceiptCMNConstants.ALIASNAME, payment.getVendor() == null ? "" : payment.getVendor().getAliasname());
                            invoiceJson.put(GoodsReceiptCMNConstants.CustomerCreditTerm, payment.getVendor() == null ? "" : payment.getVendor().getDebitTerm().getTermname());
                            KwlReturnObject bAmt = null;
                            double amountdueinbase = 0d;
//                        if (Constants.OpeningBalanceBaseAmountFlag) {
//                            amountdueinbase = -payment.getOpeningBalanceBaseAmountDue();
//                        } else {
                            if (isopeningBalancePayment && payment.isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, payment.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, payment.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            }
                            amountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
//                        }
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE, amountdue);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, amountdueinbase);
                            invoiceJson.put(GoodsReceiptCMNConstants.DUEDATE, df.format(payment.getCreationDate()));
                            invoiceJson.put(GoodsReceiptCMNConstants.DATE, df.format(payment.getCreationDate()));
                            invoiceJson.put(GoodsReceiptCMNConstants.MEMO, payment.getMemo() == null ? "" : payment.getMemo());
                            invoiceJson.put(GoodsReceiptCMNConstants.DELETED, payment.isDeleted());
                            invoiceJson.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, exchangeRateForOtherCurrency);
                            invoiceJson.put("isOpeningBalanceTransaction", isopeningBalancePayment);
                            invoiceJson.put(GoodsReceiptCMNConstants.IsConversionRateFromCurrencyToBase, payment.isConversionRateFromCurrencyToBase());
                            invoiceJson.put(GoodsReceiptCMNConstants.TYPE, "Payment Made");
                            invoiceJson.put(GoodsReceiptCMNConstants.ExchangeRate, "1 "+currency.getCurrencyCode()+" = "+((isopeningBalancePayment && isConversionRateFromCurrencyToBase) ? (1 / exchangeRateForOtherCurrency) : exchangeRateForOtherCurrency )+" "+(payment.getCurrency() == null ? "" : payment.getCurrency().getCurrencyCode()));
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTINBASE, payment.getOriginalOpeningBalanceBaseAmount());
                            invoiceJson.put("code", payment.getVendor() != null ? payment.getVendor().getAcccode() : "");
                            invoiceJson.put("isMP", true);
                            double vendorCreditLimit = 0;
                            double vendorCreditLimitInbase = 0;
                            String currencyId = "";
                            if (payment.getVendor() != null && payment.getVendor().getCurrency() != null) {
                                currencyId = payment.getVendor().getCurrency().getCurrencyID();
                                vendorCreditLimit = payment.getVendor().getDebitlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, vendorCreditLimit, currencyId, payment.getVendor().getCreatedOn(), 0);
                                vendorCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            }
                            invoiceJson.put("creditlimit", vendorCreditLimit);
                            invoiceJson.put("creditlimitinbase", vendorCreditLimitInbase);
                            Date dueDate = df.parse(df.format(payment.getCreationDate()));

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

                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE1, amountdue1);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE2, amountdue2);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE3, amountdue3);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE4, amountdue4);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE5, amountdue5);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE6, amountdue6);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE7, amountdue7);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE8, amountdue8);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE9, amountdue9);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE10, amountdue10);
                            invoiceJson.put(GoodsReceiptCMNConstants.AMOUNTDUE11, amountdue11);
//                            invoiceJson.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
                            if (!requestParams.containsKey("isAgedPayables") || !(Boolean) requestParams.get("isAgedPayables")) {
                                getOpeningPaymentCustomField(companyid, payment, requestParams, accountingHandlerDAOobj, replaceFieldMap, FieldMap, customFieldMap, customDateFieldMap, invoiceJson, fieldDataManagercntrl);
                            }

                            dataArray.put(invoiceJson);
                        }
                    }
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }

    public static void getOpeningPaymentCustomField(String companyid, Payment payment, HashMap<String, Object> requestParams, AccountingHandlerDAO accountingHandlerDAOobj, HashMap<String, String> replaceFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, com.krawler.utils.json.base.JSONObject invoiceJson, fieldDataManager fieldDataManagercntrl) throws JSONException, ServiceException {
        KwlReturnObject custumObjresult = null;
        
        if (payment.isIsOpeningBalencePayment()) {
            boolean isExport = (requestParams.get("isExport") == null) ? false : (Boolean) requestParams.get("isExport");
            Map<String, Object> variableMap = new HashMap<String, Object>();
            try {
                custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceMakePaymentCustomData.class.getName(), payment.getID());
            } catch (HibernateObjectRetrievalFailureException ex) {
                Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
            replaceFieldMap = new HashMap<String, String>();
            if (custumObjresult != null && custumObjresult.getEntityList().size() > 0 && custumObjresult.getEntityList().get(0) != null) {
                OpeningBalanceMakePaymentCustomData openingMPCustom = (OpeningBalanceMakePaymentCustomData) custumObjresult.getEntityList().get(0);
                if (openingMPCustom != null) {
                    AccountingManager.setCustomColumnValues(openingMPCustom, FieldMap, replaceFieldMap, variableMap);
                    JSONObject params = new JSONObject();
                    params.put("companyid", companyid);
                    if (!isExport) {
                        isExport = (requestParams.get("isAgedPayables") == null) ? false : (Boolean) requestParams.get("isAgedPayables");
                    }
                    params.put("isExport", isExport);
                    if (requestParams.containsKey("browsertz") && requestParams.get("browsertz") != null) {
                        params.put("browsertz", requestParams.get("browsertz").toString());
                    }
                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, invoiceJson, params);
                }
            }
        }
    }

    public static double getOpeningPaymentAmountDue(Payment payment, HashMap<String, Object> reqParams1, accVendorPaymentDAO accVendorPaymentobj, String companyid) throws ServiceException {
        double amountdue = 0;
        double linkAmount = 0;
        double pmtAmount = payment.getDepositAmount();  //original payment amount in payment currency
        KwlReturnObject result = accVendorPaymentobj.getLinkedDetailsPayment(reqParams1);
        List<LinkDetailPayment> linkedDetaisPayments = result.getEntityList();
        for (LinkDetailPayment ldp : linkedDetaisPayments) {
            linkAmount += ldp.getAmount();
        }
        result = accVendorPaymentobj.getLinkDetailPaymentToCreditNote(reqParams1);
        List<LinkDetailPaymentToCreditNote> linkedDetaisPaymentsToCN = result.getEntityList();
        for (LinkDetailPaymentToCreditNote ldp : linkedDetaisPaymentsToCN) {
            linkAmount += ldp.getAmount();
        }
        amountdue = authHandler.round(pmtAmount - linkAmount, companyid);
        amountdue = -amountdue;// In MP amount due will be negative 
        return amountdue;
    }

    public static JSONArray getDebitNotesMergedJson(HashMap<String, Object> requestParams, List list, JSONArray JArr, accCurrencyDAO accCurrencyDAOobj, AccountingHandlerDAO accountingHandlerDAOobj, accReceiptDAO accReceiptDao, accDebitNoteDAO accDebitNoteobj,accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj) throws ServiceException {
        try {
            boolean ignoreZero = requestParams.get(GoodsReceiptCMNConstants.IGNOREZERO) != null;
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get(Constants.companyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get(GoodsReceiptCMNConstants.DATEFORMAT);
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
            boolean agedReport = (requestParams.containsKey("agedReport") && requestParams.get("agedReport") != null) ? Boolean.parseBoolean(requestParams.get("agedReport").toString()) : false;
            Calendar oneDayBeforeCal1 = null;
            Calendar cal1 = null;
            Calendar cal2 = null;
            Calendar cal3 = null;
            Calendar cal4 = null;
            Calendar cal5 = null;
            Calendar cal6 = null;
            Calendar cal7 = null;
            Calendar cal8 = null;
            Calendar cal9 = null;
            Calendar cal10 = null;

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
            if (!(requestParams.containsKey("oneDayBeforeCal1") && requestParams.containsKey("cal1") && requestParams.containsKey("cal2") && requestParams.containsKey("cal3") && requestParams.containsKey("cal4") && requestParams.containsKey("cal5") && requestParams.containsKey("cal6") && requestParams.containsKey("cal7") && requestParams.containsKey("cal8") && requestParams.containsKey("cal9") && requestParams.containsKey("cal10"))) {
                oneDayBeforeCal1 = Calendar.getInstance();
                cal1 = Calendar.getInstance();
                cal2 = Calendar.getInstance();
                cal3 = Calendar.getInstance();
                cal4 = Calendar.getInstance();
                cal5 = Calendar.getInstance();
                cal6 = Calendar.getInstance();
                cal7 = Calendar.getInstance();
                cal8 = Calendar.getInstance();
                cal9 = Calendar.getInstance();
                cal10 = Calendar.getInstance();

                if (requestParams.get(Constants.asOfDate) != null) {
                    String curDateString = (String) requestParams.get(Constants.asOfDate);
                    Date curDate = null;
                    if (requestParams.get("MonthlyAgeingCurrDate") != null) {
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
            } else {
                oneDayBeforeCal1 = (Calendar) requestParams.get("oneDayBeforeCal1");
                cal1 = (Calendar) requestParams.get("cal1");
                cal2 = (Calendar) requestParams.get("cal2");
                cal3 = (Calendar) requestParams.get("cal3");
                cal4 = (Calendar) requestParams.get("cal4");
                cal5 = (Calendar) requestParams.get("cal5");
                cal6 = (Calendar) requestParams.get("cal6");
                cal7 = (Calendar) requestParams.get("cal7");
                cal8 = (Calendar) requestParams.get("cal8");
                cal9 = (Calendar) requestParams.get("cal9");
                cal10 = (Calendar) requestParams.get("cal10");
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

//            Iterator itr = list.iterator();
            if (list != null && !list.isEmpty()) {
            for (Object object:list) {
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

                    Object[] row = (Object[]) object;
                    boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                    String personid = "";
                    String personname = "";
                    String aliasname = "";
                    String dncurrencyid = "";

                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Vendor.class.getName(), (String) row[2]);
                    Vendor vendor = (Vendor) resultObject.getEntityList().get(0);
                    personid = vendor != null ? vendor.getID() : "";
                    personname = vendor != null ? vendor.getName() : "";
                    aliasname = vendor != null ? vendor.getAliasname() : "";

                    resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                    JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                    com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                    if (!withoutinventory) {
                        resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);
                        DebitNote debitMemo = (DebitNote) resultObject.getEntityList().get(0);
                        if (debitMemo != null) {
//                        System.out.println("Normal DN="+debitMemo.getDebitNoteNumber());
                            JournalEntry je = debitMemo.getJournalEntry();
                        double externalCurrencyRate = je.getExternalCurrencyRate()==0?1:je.getExternalCurrencyRate();
                            dncurrencyid = debitMemo.getCurrency() == null ? currency.getCurrencyID() : debitMemo.getCurrency().getCurrencyID();
                            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.INFO, "Normal DN=" + debitMemo.getDebitNoteNumber());
                            List<Double> knockedOffAmountList = new ArrayList();//variable used to hold knocked off amounts in invoice currency
                            double amountdue = 0;
                            double amountdueinbase = 0;
                            double dnReceiptAmount = 0;
                            double invReturnAmt = 0;
                            double cnReturnAmt = 0;
                            /*DN used or linked in payment*/
                            requestParams.put("debitnoteid", debitMemo.getID());
                            KwlReturnObject dnpResult = accReceiptDao.getDebitNotePaymentDetail(requestParams);//This method fetched row level DN Detail 
                            if (!dnpResult.getEntityList().isEmpty()) {
                                Iterator dnpItr = dnpResult.getEntityList().iterator();
                                while (dnpItr.hasNext()) {
                                    Object[] objects = (Object[]) dnpItr.next();
                                    double exchangeratefortransaction = objects[0] != null ? (Double) objects[0] : 1.0;
                                    double dnPaidAmtInReceiptCurrency = objects[1] != null ? (Double) objects[1] : 0.0;
                                    dnReceiptAmount += authHandler.round(dnPaidAmtInReceiptCurrency / exchangeratefortransaction, companyid);
                                }
                            }
                            knockedOffAmountList.add(dnReceiptAmount);
                            /*Invoice linked or used in DN*/
                            KwlReturnObject dnresult = accDebitNoteobj.getDNRowsFromVendorInvoice(requestParams);
                        List<DebitNoteDetail> rows =dnresult.getEntityList(); 
                            for (DebitNoteDetail detail : rows) {
                                Discount disc = detail.getDiscount();
                                if (disc != null) {
                                    invReturnAmt += disc.getDiscountValue();
                                }
                            }
                            knockedOffAmountList.add(invReturnAmt);
                            /*
                             * Inlcuding Debit Note amount Linked in Credit Note
                             */
                            dnresult = accDebitNoteobj.getDNRowsFromCreditNote(requestParams);
                        rows =dnresult.getEntityList(); 
                            for (DebitNoteDetail detail : rows) {
                                Discount disc = detail.getDiscount();
                                if (disc != null) {
                                    cnReturnAmt += disc.getDiscountValue();
                                }
                            }
                            knockedOffAmountList.add(cnReturnAmt);
                            double knockedOffAmtInBase = 0;
                            for (double knockedOffAmount : knockedOffAmountList) {
                                if (knockedOffAmount != 0) {
//                                KwlReturnObject dnAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, knockedOffAmount, dncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                                    KwlReturnObject dnAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, knockedOffAmount, dncurrencyid, debitMemo.getCreationDate(), je.getExternalCurrencyRate());
                                    //Doing round off each value before summing for matching aged amount with balance sheet in base currency 
                                    knockedOffAmtInBase += authHandler.round((Double) dnAmtInBaseResult.getEntityList().get(0), companyid);
                                }
                            }
                            amountdueinbase = authHandler.round(debitMemo.getDnamountinbase() - knockedOffAmtInBase, companyid);
                        amountdue = debitMemo.getDnamount() - (dnReceiptAmount + invReturnAmt+cnReturnAmt);
                        amountdue = authHandler.round(amountdue,companyid);

                            requestParams.remove("debitnoteid");//Removing debitnoteid after use, So that is does not affect other  
                        if ((debitMemo.isOtherwise() && amountdue > 0) ||!ignoreZero || (debitMemo.getDntype() == Constants.DebitNoteForOvercharge && amountdue > 0)) {
                                obj.put(GoodsReceiptCMNConstants.BILLID, debitMemo.getID());
                                obj.put(GoodsReceiptCMNConstants.NOTEID, debitMemo.getID());
                                obj.put(GoodsReceiptCMNConstants.NOTENO, debitMemo.getDebitNoteNumber());
                                obj.put(GoodsReceiptCMNConstants.COMPANYID, debitMemo.getCompany().getCompanyID());
                                obj.put(GoodsReceiptCMNConstants.COMPANYNAME, debitMemo.getCompany().getCompanyName());
                                obj.put(GoodsReceiptCMNConstants.BILLNO, debitMemo.getDebitNoteNumber());
                                obj.put(GoodsReceiptCMNConstants.JOURNALENTRYID, je.getID());
                                obj.put(GoodsReceiptCMNConstants.WITHOUTINVENTORY, withoutinventory);
                                obj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (debitMemo.getCurrency() == null ? currency.getSymbol() : debitMemo.getCurrency().getSymbol()));
                                obj.put(GoodsReceiptCMNConstants.CURRENCYID, (debitMemo.getCurrency() == null ? currency.getCurrencyID() : debitMemo.getCurrency().getCurrencyID()));
                                obj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (debitMemo.getCurrency() == null ? currency.getName() : debitMemo.getCurrency().getName()));
                                obj.put(GoodsReceiptCMNConstants.ENTRYNO, je.getEntryNumber());
                                obj.put(GoodsReceiptCMNConstants.PERSONID, personid);
                                obj.put(GoodsReceiptCMNConstants.PERSONNAME, personname);
                            obj.put(GoodsReceiptCMNConstants.PERSONINFO, personname+"("+debitMemo.getVendor().getAcccode()+")");
                                obj.put(GoodsReceiptCMNConstants.ALIASNAME, aliasname);
                                obj.put(GoodsReceiptCMNConstants.CustomerCreditTerm, vendor == null ? "" : ((vendor.getDebitTerm() == null) ? "" : vendor.getDebitTerm().getTermname()));

                                if (agedReport) {
                                    obj.put(GoodsReceiptCMNConstants.AMOUNT, debitMemo.isOtherwise() || debitMemo.getDntype() == Constants.DebitNoteForOvercharge ? -debitMemo.getDnamount() : -details.getAmount());
                                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, debitMemo.isOtherwise() || debitMemo.getDntype() == Constants.DebitNoteForOvercharge ? -amountdue : 0);
                                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUENONNEGATIVE, debitMemo.isOtherwise() || debitMemo.getDntype() == Constants.DebitNoteForOvercharge ? -amountdue : 0);
                                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, -amountdueinbase);//authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), 2));
                                obj.put(Constants.SUPPLIERINVOICENO, debitMemo.getSupplierInvoiceNo()!=null? debitMemo.getSupplierInvoiceNo():"");
                                } else {
                                    obj.put(GoodsReceiptCMNConstants.AMOUNT, debitMemo.isOtherwise() || debitMemo.getDntype() == Constants.DebitNoteForOvercharge ? debitMemo.getDnamount() : details.getAmount());
                                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, debitMemo.isOtherwise() || debitMemo.getDntype() == Constants.DebitNoteForOvercharge ? amountdue : 0);
                                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUENONNEGATIVE, debitMemo.isOtherwise() || debitMemo.getDntype() == Constants.DebitNoteForOvercharge ? amountdue : 0);
                                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, amountdueinbase);//authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), 2));
                                }
//                            obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE,authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, debitMemo.getDnamount(), dncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE,authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, debitMemo.getDnamount(), dncurrencyid,debitMemo.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                                obj.put("code", debitMemo.getVendor() != null ? debitMemo.getVendor().getAcccode() : "");
//                            obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(je.getEntryDate()));
//                            obj.put(GoodsReceiptCMNConstants.DATE, df.format(je.getEntryDate()));
                                obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(debitMemo.getCreationDate()));
                                obj.put(GoodsReceiptCMNConstants.DATE, df.format(debitMemo.getCreationDate()));
                                obj.put(GoodsReceiptCMNConstants.MEMO, debitMemo.getMemo());
                                obj.put(GoodsReceiptCMNConstants.DELETED, debitMemo.isDeleted());
                                obj.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, externalCurrencyRate);
                                obj.put(GoodsReceiptCMNConstants.TYPE, "Debit Note");
                                obj.put(Constants.SEQUENCEFORMATID, debitMemo.getSeqformat() != null ? debitMemo.getSeqformat().getID() : "");
                                obj.put("includingGST", debitMemo.isIncludingGST());
                                obj.put("costcenterid", debitMemo.getCostcenter() == null ? "" : debitMemo.getCostcenter().getID());
                                obj.put("isDN", true);
                                obj.put("cntype", debitMemo.getDntype());
                            obj.put(GoodsReceiptCMNConstants.ExchangeRate, "1 "+currency.getCurrencyCode()+" = "+externalCurrencyRate+" "+(debitMemo.getCurrency() == null ? "" : debitMemo.getCurrency().getCurrencyCode()));
                            obj.put("salespersonname", debitMemo.getSalesPerson() == null? "" : debitMemo.getSalesPerson().getValue());
//                            Date dueDate = df.parse(df.format(je.getEntryDate()));
                                Date dueDate = df.parse(df.format(debitMemo.getCreationDate()));
                                if (agedReport) {
                                    amountdue = -amountdue;
                                }
                            double vendorCreditLimit = 0;
                            double vendorCreditLimitInbase = 0;
                            String currencyId = "";
                            if (debitMemo.getVendor() != null && debitMemo.getVendor().getCurrency() != null) {
                                currencyId = debitMemo.getVendor().getCurrency().getCurrencyID();
                                vendorCreditLimit = debitMemo.getVendor().getDebitlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, vendorCreditLimit, currencyId, debitMemo.getVendor().getCreatedOn(), 0);
                                vendorCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            }
                            obj.put("creditlimit", vendorCreditLimit);
                            obj.put("creditlimitinbase", vendorCreditLimitInbase);
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
                            
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE1, amountdue1);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE2, amountdue2);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE3, amountdue3);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE4, amountdue4);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE5, amountdue5);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE6, amountdue6);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE7, amountdue7);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE8, amountdue8);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE9, amountdue9);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE10, amountdue10);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE11, amountdue11);
                                //obj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
                                //If Condition
                                if (!requestParams.containsKey("isAgedPayables") || !(Boolean) requestParams.get("isAgedPayables")) {
                                getDebitNotesMergedCustomField(companyid, accJournalEntryobj, debitMemo, accountingHandlerDAOobj, replaceFieldMap, FieldMap, customFieldMap, customDateFieldMap, accDebitNoteobj, fieldMapRows, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows, obj, fieldDataManagercntrl);
                                }
                                JArr.put(obj);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGoodsReceiptServiceHandler.getDebitNotesMergedJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }

    public static void getDebitNotesMergedCustomField(String companyid, accJournalEntryDAO accJournalEntryobj, DebitNote debitMemo, AccountingHandlerDAO accountingHandlerDAOobj, HashMap<String, String> replaceFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, accDebitNoteDAO accDebitNoteobj, HashMap<String, Integer> fieldMapRows, HashMap<String, String> replaceFieldMapRows, HashMap<String, String> customFieldMapRows, HashMap<String, String> customDateFieldMapRows, com.krawler.utils.json.base.JSONObject obj, fieldDataManager fieldDataManagercntrl) throws JSONException, ServiceException, SessionExpiredException {
        if (fieldDataManagercntrl != null && accJournalEntryobj != null) {
            Map<String, Object> variableMap = new HashMap<String, Object>();
            HashMap<String, Object> cnDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
            Detailfilter_names.add("companyid");
            Detailfilter_params.add(companyid);
            Detailfilter_names.add("journalentryId");
            Detailfilter_params.add(debitMemo.getJournalEntry().getID());
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
                boolean isExport = true;
                params.put("isExport", isExport);
                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
            }
        }
        Set<JournalEntryDetail> jedDetails = debitMemo.getJournalEntry() != null ? debitMemo.getJournalEntry().getDetails() : new HashSet(0);
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
                KwlReturnObject idcustdetailresult = accDebitNoteobj.geDebitNoteCustomData(invDetailsRequestParams);
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

    public static JSONArray getCreditNotesMergedJson(HashMap<String, Object> requestParams, List list, JSONArray JArr, accCurrencyDAO accCurrencyDAOobj, AccountingHandlerDAO accountingHandlerDAOobj, accPaymentDAO accPaymentDao, accVendorPaymentDAO accVendorPaymentobj,accDebitNoteDAO accDebitNoteobj,accAccountDAO accAccountDAOobj, fieldDataManager fieldDataManagercntrl, accJournalEntryDAO accJournalEntryobj) throws ServiceException {
        try {
            boolean ignoreZero = requestParams.get(GoodsReceiptCMNConstants.IGNOREZERO) != null;
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get(Constants.companyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get(GoodsReceiptCMNConstants.DATEFORMAT);
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                if (StringUtil.isValidDateOnlyFormat(requestParams.get(Constants.REQ_startdate).toString())) {
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
            boolean agedReport = (requestParams.containsKey("agedReport") && requestParams.get("agedReport") != null) ? Boolean.parseBoolean(requestParams.get("agedReport").toString()) : false;
            Calendar oneDayBeforeCal1 = null;
            Calendar cal1 = null;
            Calendar cal2 = null;
            Calendar cal3 = null;
            Calendar cal4 = null;
            Calendar cal5 = null;
            Calendar cal6 = null;
            Calendar cal7 = null;
            Calendar cal8 = null;
            Calendar cal9 = null;
            Calendar cal10 = null;

            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Credit_Note_ModuleId));
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
            if (accAccountDAOobj != null) {
                fieldMapRows = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParamsRows, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows);
            }

            if (!(requestParams.containsKey("oneDayBeforeCal1") && requestParams.containsKey("cal1") && requestParams.containsKey("cal2") && requestParams.containsKey("cal3") && requestParams.containsKey("cal4") && requestParams.containsKey("cal5") && requestParams.containsKey("cal6") && requestParams.containsKey("cal7") && requestParams.containsKey("cal8") && requestParams.containsKey("cal9") && requestParams.containsKey("cal10"))) {
                oneDayBeforeCal1 = Calendar.getInstance();
                cal1 = Calendar.getInstance();
                cal2 = Calendar.getInstance();
                cal3 = Calendar.getInstance();
                cal4 = Calendar.getInstance();
                cal5 = Calendar.getInstance();
                cal6 = Calendar.getInstance();
                cal7 = Calendar.getInstance();
                cal9 = Calendar.getInstance();
                cal8 = Calendar.getInstance();
                cal10 = Calendar.getInstance();

                if (requestParams.get(Constants.asOfDate) != null) {
                    String curDateString = (String) requestParams.get(Constants.asOfDate);
                    Date curDate = null;
                    if (requestParams.get("MonthlyAgeingCurrDate") != null) {
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
            } else {
                oneDayBeforeCal1 = (Calendar) requestParams.get("oneDayBeforeCal1");
                cal1 = (Calendar) requestParams.get("cal1");
                cal2 = (Calendar) requestParams.get("cal2");
                cal3 = (Calendar) requestParams.get("cal3");
                cal4 = (Calendar) requestParams.get("cal4");
                cal5 = (Calendar) requestParams.get("cal5");
                cal6 = (Calendar) requestParams.get("cal6");
                cal7 = (Calendar) requestParams.get("cal7");
                cal8 = (Calendar) requestParams.get("cal8");
                cal9 = (Calendar) requestParams.get("cal9");
                cal10 = (Calendar) requestParams.get("cal10");
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
            if (list != null && !list.isEmpty()) {
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
//                double accruedbalance = 0;

                    Object[] row = (Object[]) object;
                    boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                    String personid = "";
                    String personname = "";
                    String aliasname = "";
                    String cncurrencyid = "";

                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Vendor.class.getName(), (String) row[2]);
                    Vendor vendor = (Vendor) resultObject.getEntityList().get(0);
                    personid = vendor != null ? vendor.getID() : "";
                    personname = vendor != null ? vendor.getName() : "";
                    aliasname = vendor != null ? vendor.getAliasname() : "";

                    resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                    JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                    com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                    if (!withoutinventory) {
                        resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                        CreditNote creditNote = (CreditNote) resultObject.getEntityList().get(0);
//                    System.out.println("Normal CN="+creditNote.getCreditNoteNumber());
                    Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.INFO, "Normal CN="+creditNote.getCreditNoteNumber());
                        double cnPaidAmount = 0;
                        double amountdue = 0;
                        requestParams.put("creditnoteid", creditNote.getID());
                        KwlReturnObject cnpResult = accPaymentDao.getCreditNotePaymentDetails(requestParams);
                        if (!cnpResult.getEntityList().isEmpty()) {
                            Iterator cnpItr = cnpResult.getEntityList().iterator();
                            while (cnpItr.hasNext()) {
                                Object[] objects = (Object[]) cnpItr.next();
                                double exchangeratefortransaction = objects[0] != null ? (Double) objects[0] : 1.0;
                                double cnPaidAmtInReceiptCurrency = objects[1] != null ? (Double) objects[1] : 0.0;
                                cnPaidAmount += authHandler.round(cnPaidAmtInReceiptCurrency / exchangeratefortransaction, companyid);
                            }
                        }

                    double linkAmount=0;    
                        KwlReturnObject cnLinkResult = accVendorPaymentobj.getLinkDetailPaymentToCreditNote(requestParams);
                        List<LinkDetailPaymentToCreditNote> linkDetail = cnLinkResult.getEntityList();
                        for (LinkDetailPaymentToCreditNote ldr : linkDetail) {
                            linkAmount += ldr.getAmountInCNCurrency();
                        }
                        /*
                         *  Get DN Linked amount in CN
                         */
                     double dnlinkAmt=0;   
                        KwlReturnObject dnresult = accDebitNoteobj.getDNRowsFromCreditNote(requestParams);
                     List<DebitNoteDetail>    rows =dnresult.getEntityList(); 
                        for (DebitNoteDetail detail : rows) {
                            double exchangeRate = detail.getExchangeRateForTransaction();
                            Discount disc = detail.getDiscount();
                            if (disc != null) {
                                //When currency of CN and DN is different, if currency is same then exchangeRate=1, case exchangeRate!=0 is applied for avaoiding AE
                                if(exchangeRate!=1 && exchangeRate!=0){
                                   dnlinkAmt += authHandler.round(disc.getDiscountValue()/exchangeRate, companyid); 
                                } else{
                                    dnlinkAmt += disc.getDiscountValue();
                                }
                            }
                        }
                    amountdue = creditNote.getCnamount() - (cnPaidAmount+linkAmount+dnlinkAmt);

                        requestParams.remove("creditnoteid");//Removing debitnoteid after use, So that is does not affect other 
                    if ((creditNote.isOtherwise() && amountdue > 0) || !ignoreZero||creditNote.getCntype()==5) {
                            JournalEntry je = creditNote.getJournalEntry();
                        double externalCurrencyRate = je.getExternalCurrencyRate()==0?1:je.getExternalCurrencyRate();
                            obj.put(GoodsReceiptCMNConstants.BILLID, creditNote.getID());
                            obj.put(GoodsReceiptCMNConstants.NOTEID, creditNote.getID());
                            obj.put(GoodsReceiptCMNConstants.NOTENO, creditNote.getCreditNoteNumber());
                            obj.put(GoodsReceiptCMNConstants.COMPANYID, creditNote.getCompany().getCompanyID());
                            obj.put(GoodsReceiptCMNConstants.COMPANYNAME, creditNote.getCompany().getCompanyName());
                            obj.put(GoodsReceiptCMNConstants.BILLNO, creditNote.getCreditNoteNumber());
                            obj.put(GoodsReceiptCMNConstants.JOURNALENTRYID, je.getID());
                            obj.put(GoodsReceiptCMNConstants.WITHOUTINVENTORY, withoutinventory);
                            obj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (creditNote.getCurrency() == null ? currency.getSymbol() : creditNote.getCurrency().getSymbol()));
                            obj.put(GoodsReceiptCMNConstants.CURRENCYID, (creditNote.getCurrency() == null ? currency.getCurrencyID() : creditNote.getCurrency().getCurrencyID()));
                            obj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (creditNote.getCurrency() == null ? currency.getName() : creditNote.getCurrency().getName()));
                            obj.put(GoodsReceiptCMNConstants.ENTRYNO, je.getEntryNumber());
                            obj.put(GoodsReceiptCMNConstants.PERSONID, personid);
                            obj.put(GoodsReceiptCMNConstants.PERSONNAME, personname);
                        obj.put(GoodsReceiptCMNConstants.PERSONINFO, personname+"("+creditNote.getVendor().getAcccode()+")");
                            obj.put(GoodsReceiptCMNConstants.ALIASNAME, aliasname);
                            obj.put(GoodsReceiptCMNConstants.CustomerCreditTerm, vendor == null ? "" : ((vendor.getDebitTerm() == null) ? "" : vendor.getDebitTerm().getTermname()));
                            obj.put(GoodsReceiptCMNConstants.AMOUNT, creditNote.isOtherwise() ? creditNote.getCnamount() : details.getAmount());
                        obj.put("salespersonname", creditNote.getSalesPerson() == null? (creditNote.getMasterAgent() == null? "" : creditNote.getMasterAgent().getValue()) : creditNote.getSalesPerson().getValue());
                            if (creditNote.getCntype() == 5) {
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, amountdue);
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUENONNEGATIVE, amountdue);
//                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, details.getAmount(), cncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, details.getAmount(), cncurrencyid, creditNote.getCreationDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
//                            obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, details.getAmount(), cncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
                                obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, details.getAmount(), cncurrencyid, creditNote.getCreationDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
                                if (creditNote.getTax() != null) {
                                    obj.put("gTaxId", creditNote.getTax().getID());
                                }
                                boolean checkTax = false;
                                Set<CreditNoteAgainstVendorGst> cndetailsGst = (Set<CreditNoteAgainstVendorGst>) creditNote.getRowsGst();
                                for (CreditNoteAgainstVendorGst noteDetail : cndetailsGst) {
                                    if (noteDetail.getTax() != null) {
                                        checkTax = true;
                                        break;
                                    }
                                }
                                obj.put("includeprotax", checkTax);
                                obj.put("lasteditedby", creditNote.getModifiedby() == null ? "" : (creditNote.getModifiedby().getFirstName() + " " + creditNote.getModifiedby().getLastName()));
                            } else {
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, creditNote.isOtherwise() ? amountdue : 0);
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUENONNEGATIVE, creditNote.isOtherwise() ? amountdue : 0);
//                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cncurrencyid, creditNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
//                            obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNote.getCnamount(), cncurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                                obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNote.getCnamount(), cncurrencyid, creditNote.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            }
                        double vendorCreditLimit = 0;
                        double vendorCreditLimitInbase = 0;
                        String currencyId = "";
                        if (creditNote.getVendor() != null && creditNote.getVendor().getCurrency() != null) {
                            currencyId = creditNote.getVendor().getCurrency().getCurrencyID();
                            vendorCreditLimit = creditNote.getVendor().getDebitlimit();
                            KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, vendorCreditLimit, currencyId, creditNote.getVendor().getCreatedOn(), 0);
                            vendorCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                        }
                        obj.put("creditlimit", vendorCreditLimit);
                        obj.put("creditlimitinbase", vendorCreditLimitInbase);
                            cncurrencyid = creditNote.getCurrency() == null ? currency.getCurrencyID() : creditNote.getCurrency().getCurrencyID();
//                        obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(je.getEntryDate()));
//                        obj.put(GoodsReceiptCMNConstants.DATE, df.format(je.getEntryDate()));
                            obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(creditNote.getCreationDate()));
                            obj.put(GoodsReceiptCMNConstants.DATE, df.format(creditNote.getCreationDate()));
                            obj.put(GoodsReceiptCMNConstants.MEMO, creditNote.getMemo());
                            obj.put(GoodsReceiptCMNConstants.DELETED, creditNote.isDeleted());
                            obj.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, externalCurrencyRate);
                            obj.put(GoodsReceiptCMNConstants.TYPE, "Credit Note");
                            obj.put(Constants.SEQUENCEFORMATID, creditNote.getSeqformat() != null ? creditNote.getSeqformat().getID() : "");
                            obj.put("includingGST", creditNote.isIncludingGST());
                            obj.put("salesPersonID", creditNote.getSalesPerson() == null ? "" : creditNote.getSalesPerson().getID());
                            obj.put("costcenterid", creditNote.getCostcenter() == null ? "" : creditNote.getCostcenter().getID());
                            obj.put("code", creditNote.getVendor() != null ? creditNote.getVendor().getAcccode() : "");
                            obj.put("isCN", true);
                            obj.put("cntype", creditNote.getCntype());
                        obj.put(GoodsReceiptCMNConstants.ExchangeRate, "1 "+currency.getCurrencyCode()+" = "+externalCurrencyRate+" "+(creditNote.getCurrency() == null ? "" : creditNote.getCurrency().getCurrencyCode()));
//                        Date dueDate = df.parse(df.format(je.getEntryDate()));
                            Date dueDate = df.parse(df.format(creditNote.getCreationDate()));

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
                        
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE1, amountdue1);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE2, amountdue2);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE3, amountdue3);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE4, amountdue4);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE5, amountdue5);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE6, amountdue6);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE7, amountdue7);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE8, amountdue8);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE9, amountdue9);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE10, amountdue10);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE11, amountdue11);

//                        obj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
                            if (!requestParams.containsKey("isAgedPayables") || !(Boolean) requestParams.get("isAgedPayables")) {
                                getCreditNotesMergedCustomField(companyid, accJournalEntryobj, creditNote, accountingHandlerDAOobj, replaceFieldMap, FieldMap, customFieldMap, customDateFieldMap, accDebitNoteobj, fieldMapRows, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows, obj, fieldDataManagercntrl);
                            }

                            JArr.put(obj);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGoodsReceiptServiceHandler.getCreditNotesMergedJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }

    public static void getCreditNotesMergedCustomField(String companyid, accJournalEntryDAO accJournalEntryobj, CreditNote creditNote, AccountingHandlerDAO accountingHandlerDAOobj, HashMap<String, String> replaceFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, accDebitNoteDAO accDebitNoteobj, HashMap<String, Integer> fieldMapRows, HashMap<String, String> replaceFieldMapRows, HashMap<String, String> customFieldMapRows, HashMap<String, String> customDateFieldMapRows, com.krawler.utils.json.base.JSONObject obj, fieldDataManager fieldDataManagercntrl) throws JSONException, ServiceException, SessionExpiredException {
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
                                boolean isExport =true;
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
                KwlReturnObject idcustdetailresult = accDebitNoteobj.geDebitNoteCustomData(invDetailsRequestParams);
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

    public static JSONArray getPaymentsJson(HashMap requestParams, List entityList, JSONArray JArr, accCurrencyDAO accCurrencyDAOobj, AccountingHandlerDAO accountingHandlerDAOobj, accVendorPaymentDAO accVendorPaymentobj) throws ServiceException {
        try {
            String companyid = (String) requestParams.get("companyid");
            requestParams.put("companyid", companyid);
            boolean ignoreZero = requestParams.get(GoodsReceiptCMNConstants.IGNOREZERO) != null;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get(GoodsReceiptCMNConstants.DATEFORMAT);
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
//            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
//                startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
//            } else {
//                startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(requestParams.get(Constants.REQ_startdate).toString()));
//                }          
            int datefilter = (requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null) ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            boolean agedReport = (requestParams.containsKey("agedReport") && requestParams.get("agedReport") != null) ? Boolean.parseBoolean(requestParams.get("agedReport").toString()) : false;
            boolean isAgedReceivables=false;//when request will come from aged receivable report either summary or details this flag will be true.
            if(requestParams.containsKey("isAgedReceivables") && requestParams.get("isAgedReceivables")!=null && Boolean.parseBoolean(requestParams.get("isAgedReceivables").toString())){
                isAgedReceivables = true;
            }
            if(df==null){//when call come from aged Receivable then df value come in another key named df not in dateformat
                df = (DateFormat) requestParams.get("df");
            }
            Calendar oneDayBeforeCal1 = null;
            Calendar cal1 = null;
            Calendar cal2 = null;
            Calendar cal3 = null;
            Calendar cal4 = null;
            Calendar cal5 = null;
            Calendar cal6 = null;
            Calendar cal7 = null;
            Calendar cal8 = null;
            Calendar cal9 = null;
            Calendar cal10 = null;
            if (!(requestParams.containsKey("oneDayBeforeCal1") && requestParams.containsKey("cal1") && requestParams.containsKey("cal2") && requestParams.containsKey("cal3") && requestParams.containsKey("cal4") && requestParams.containsKey("cal5") && requestParams.containsKey("cal6") && requestParams.containsKey("cal7") && requestParams.containsKey("cal8") && requestParams.containsKey("cal9") && requestParams.containsKey("cal10"))) {
                oneDayBeforeCal1 = Calendar.getInstance();
                cal1 = Calendar.getInstance();
                cal2 = Calendar.getInstance();
                cal3 = Calendar.getInstance();
                cal4 = Calendar.getInstance();
                cal5 = Calendar.getInstance();
                cal6 = Calendar.getInstance();
                cal7 = Calendar.getInstance();
                cal8 = Calendar.getInstance();
                cal9 = Calendar.getInstance();
                cal10 = Calendar.getInstance();

                if (requestParams.get(Constants.asOfDate) != null) {
                    String curDateString = (String) requestParams.get(Constants.asOfDate);
                    Date curDate = null;
                    if (requestParams.get("MonthlyAgeingCurrDate") != null) {
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
            } else {
                oneDayBeforeCal1 = (Calendar) requestParams.get("oneDayBeforeCal1");
                cal1 = (Calendar) requestParams.get("cal1");
                cal2 = (Calendar) requestParams.get("cal2");
                cal3 = (Calendar) requestParams.get("cal3");
                cal4 = (Calendar) requestParams.get("cal4");
                cal5 = (Calendar) requestParams.get("cal5");
                cal6 = (Calendar) requestParams.get("cal6");
                cal7 = (Calendar) requestParams.get("cal7");
                cal8 = (Calendar) requestParams.get("cal8");
                cal9 = (Calendar) requestParams.get("cal9");
                cal10 = (Calendar) requestParams.get("cal10");
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

            if(entityList!=null && !entityList.isEmpty()){
                for (Object object : entityList) {

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

                    String personid = "";
                    String personname = "";
                    String aliasname = "";
                    String personcode = "";
                    String mpcurrencyid = "";
                    String customercreditterm = "";

                    Object[] row = (Object[]) object;
                    Payment payment = (Payment) row[0];
                    if (payment != null && (payment.getVendor() != null || !StringUtil.isNullOrEmpty(payment.getCustomer()))) {
//                    System.out.println("Normal payment="+payment.getPaymentNumber());
                    double amountdue=getPaymentAmountDue(payment,requestParams,accVendorPaymentobj);
                        if (amountdue > 0 || !ignoreZero) {
                            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.INFO, "Normal payment=" + payment.getPaymentNumber());
                            if (payment.getVendor() != null) {
                                Vendor vendor = payment.getVendor();
                                personid = vendor.getID();
                                personname = vendor.getName();
                                aliasname = vendor.getAliasname();
                                personcode = vendor.getAcccode();
                            customercreditterm = vendor.getDebitTerm() ==null ? "": vendor.getDebitTerm().getTermname();
                            } else if (!StringUtil.isNullOrEmpty(payment.getCustomer())) {
                                KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), payment.getCustomer());
                                Customer customer = (Customer) customerResult.getEntityList().get(0);
                                if (customer != null) {
                                    personid = customer.getID();
                                    personname = customer.getName();
                                    aliasname = customer.getAliasname();
                                    personcode = customer.getAcccode();
                                customercreditterm = customer.getCreditTerm() ==null ? "": customer.getCreditTerm().getTermname();
                                }
                            }

                            com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                            JournalEntry je = payment.getJournalEntry();
                        double externalCurrencyRate = je.getExternalCurrencyRate()==0?1:je.getExternalCurrencyRate();
                            obj.put(GoodsReceiptCMNConstants.BILLID, payment.getID());
                            obj.put(GoodsReceiptCMNConstants.COMPANYID, payment.getCompany().getCompanyID());
                            obj.put(GoodsReceiptCMNConstants.COMPANYNAME, payment.getCompany().getCompanyName());
                            obj.put(GoodsReceiptCMNConstants.BILLNO, payment.getPaymentNumber());
                            obj.put(GoodsReceiptCMNConstants.JOURNALENTRYID, je.getID());
                            obj.put(GoodsReceiptCMNConstants.WITHOUTINVENTORY, false);
                            obj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol()));
                            obj.put(GoodsReceiptCMNConstants.CURRENCYID, (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID()));
                            obj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (payment.getCurrency() == null ? currency.getName() : payment.getCurrency().getName()));
                            obj.put(GoodsReceiptCMNConstants.ENTRYNO, je.getEntryNumber());
                            obj.put(GoodsReceiptCMNConstants.PERSONID, personid);
                            obj.put(GoodsReceiptCMNConstants.PERSONNAME, personname);
                        obj.put(GoodsReceiptCMNConstants.PERSONINFO, personname+"("+personcode+")");
                            obj.put(GoodsReceiptCMNConstants.ALIASNAME, aliasname);
                            obj.put(GoodsReceiptCMNConstants.CustomerCreditTerm, customercreditterm);
                            mpcurrencyid = payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID();
                        if(isAgedReceivables){//in Aged Receivables we need we need only posetive value on both summary as well details 
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, amountdue);
//                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, mpcurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, mpcurrencyid, payment.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                        }else if(agedReport) { //in Aged Payable Summary Report we need negative value for calculation
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, -amountdue);
//                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, mpcurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, mpcurrencyid, payment.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            } else { //in Aged Payable Details Report we need posetive value. we handle it with isMP flag
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, amountdue);
//                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, mpcurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, mpcurrencyid, payment.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            }
//                        obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, payment.getDepositAmount(), mpcurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, payment.getDepositAmount(), mpcurrencyid, payment.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                            obj.put("code", personcode);
//                        obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(je.getEntryDate()));
//                        obj.put(GoodsReceiptCMNConstants.DATE, df.format(je.getEntryDate()));
                            obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(payment.getCreationDate()));
                            obj.put(GoodsReceiptCMNConstants.DATE, df.format(payment.getCreationDate()));
                            obj.put(GoodsReceiptCMNConstants.MEMO, payment.getMemo());
                            obj.put(GoodsReceiptCMNConstants.DELETED, payment.isDeleted());
                            obj.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, externalCurrencyRate);
                            obj.put(GoodsReceiptCMNConstants.TYPE, "Payment Made");
                            obj.put("paymentwindowtype", payment.getPaymentWindowType());
                            obj.put("isMP", true);
                            double customerCreditLimit = 0;
                            double customerCreditLimitInbase = 0;
                            String currencyId = "";
                            if (!StringUtil.isNullOrEmpty(payment.getCustomer())) {
                                KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), payment.getCustomer());
                                Customer customer = (Customer) customerResult.getEntityList().get(0);
                                currencyId = customer.getCurrency().getCurrencyID();
                                customerCreditLimit = customer.getCreditlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, customerCreditLimit, currencyId, customer.getCreatedOn(), 0);
                                customerCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            } else if (payment.getVendor() != null && payment.getVendor().getCurrency() != null) {
                                currencyId = payment.getVendor().getCurrency().getCurrencyID();
                                customerCreditLimit = payment.getVendor().getDebitlimit();
                                KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, customerCreditLimit, currencyId, payment.getVendor().getCreatedOn(), 0);
                                customerCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                            }
                            obj.put("creditlimit", customerCreditLimit);
                            obj.put("creditlimitinbase", customerCreditLimitInbase);
                        obj.put(GoodsReceiptCMNConstants.ExchangeRate, "1 "+currency.getCurrencyCode()+" = "+externalCurrencyRate+" "+(payment.getCurrency() == null ? "" : payment.getCurrency().getCurrencyCode()));
//                        Date dueDate = df.parse(df.format(je.getEntryDate()));
                            Date dueDate = df.parse(df.format(payment.getCreationDate()));
                            if (agedReport && !isAgedReceivables) {
                                amountdue = -amountdue;
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
                            
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE1, amountdue1);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE2, amountdue2);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE3, amountdue3);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE4, amountdue4);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE5, amountdue5);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE6, amountdue6);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE7, amountdue7);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE8, amountdue8);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE9, amountdue9);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE10, amountdue10);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE11, amountdue11);
//                        obj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
                            JArr.put(obj);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptControllerCMN.getPaymentsJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }

    public static double getPaymentAmountDue(Payment payment, HashMap requestParams, accVendorPaymentDAO accVendorPaymentobj) throws ServiceException, ParseException {
        /*
         * There are only three type of payments can be due 1 : Opening payment
         * 2 : Advance Payment Against Vendor 3 : Refund Payment Agaisnt
         * Customer
         */
        boolean isOpeningPayment = false;
        boolean isAdvancePaymentToVendor = false;
        boolean isRefundPaymentToCustomer = false;
        DateFormat df = (DateFormat) requestParams.get(GoodsReceiptCMNConstants.DATEFORMAT);
        if (df == null) {
            df = (DateFormat) requestParams.get(Constants.df);
        }
        String companyid = (String) requestParams.get(GoodsReceiptCMNConstants.COMPANYID);
        double paymentDueAmt = 0;
        if ((payment.getAdvanceDetails() != null && !payment.getAdvanceDetails().isEmpty()) || payment.isIsOpeningBalencePayment()) {
            double paymentAmt = 0;
            double linkedPaymentAmt = 0;
            if (payment.isIsOpeningBalencePayment()) {//opening payment
                isOpeningPayment = true;
                paymentAmt += payment.getDepositAmount();
            } else if (payment.getVendor() != null) {//advance payment against vendor
                isAdvancePaymentToVendor = true;
                for (AdvanceDetail advanceDetail : payment.getAdvanceDetails()) {
                    paymentAmt += advanceDetail.getAmount();
                }
            } else if (payment.getCustomer() != null) {//Refund payment against customer
                isRefundPaymentToCustomer = true;
                for (AdvanceDetail advanceDetail : payment.getAdvanceDetails()) {
                    if (advanceDetail.getReceiptAdvanceDetails() == null) {//Only such refunds can be due in which at the time of creation no document (no advance receipt) is selected 
                        paymentAmt += advanceDetail.getAmount();
                    }
                }
                //In this case paymentAmt can be zero (if all refund have advance receipt documnet selected) so we need to retun amount due zero here
                if (paymentAmt == 0) {
                    return 0;
                }
            }
            HashMap<String, Object> reqParams1 = new HashMap();
            reqParams1.put("paymentid", payment.getID());
            reqParams1.put("companyid", companyid);
            reqParams1.put(Constants.df, df);
            if (requestParams.containsKey("asofdate") && requestParams.get("asofdate") != null) {
                reqParams1.put("asofdate", requestParams.get("asofdate"));
            }
            if (isOpeningPayment || isAdvancePaymentToVendor) {
                KwlReturnObject result = accVendorPaymentobj.getLinkedDetailsPayment(reqParams1);
                List<LinkDetailPayment> linkedDetaisPayments = result.getEntityList();
                for (LinkDetailPayment ldp : linkedDetaisPayments) {
                    linkedPaymentAmt += ldp.getAmount();
                }
                result = accVendorPaymentobj.getLinkDetailPaymentToCreditNote(reqParams1);
                List<LinkDetailPaymentToCreditNote> linkedDetaisPaymentsToCN = result.getEntityList();
                for (LinkDetailPaymentToCreditNote ldp : linkedDetaisPaymentsToCN) {
                    linkedPaymentAmt += ldp.getAmount();
                }
                result = accVendorPaymentobj.getAdvanceReceiptDetailsByPayment(reqParams1);
                List<Object[]> list2 = result.getEntityList();
                for (Object obj[] : list2) {
                    double revExchangeRate = 1.0;
                    double amount = obj[1] != null ? Double.parseDouble(obj[1].toString()) : 0.0;
                    double exchangeRate = obj[2] != null ? Double.parseDouble(obj[2].toString()) : 0.0;
                    if (exchangeRate != 0.0) {
                        revExchangeRate = 1 / exchangeRate;
                    }
                    linkedPaymentAmt += authHandler.round(revExchangeRate * amount, companyid);
                }
                result = accVendorPaymentobj.getLinkDetailReceiptToAdvancePayment(reqParams1);
                List<LinkDetailReceiptToAdvancePayment> linkDetailReceiptToAdvancePayment = result.getEntityList();
                for (LinkDetailReceiptToAdvancePayment ldp : linkDetailReceiptToAdvancePayment) {
                    linkedPaymentAmt += ldp.getAmountInPaymentCurrency();
                }
                paymentDueAmt = paymentAmt - linkedPaymentAmt;
            } else if (isRefundPaymentToCustomer) {
                KwlReturnObject result = accVendorPaymentobj.getLinkDetailAdvanceReceiptToRefundPayment(reqParams1);
                List<LinkDetailPaymentToAdvancePayment> linkDetailPaymentToAdvancePayment = result.getEntityList();
                for (LinkDetailPaymentToAdvancePayment ldp : linkDetailPaymentToAdvancePayment) {
                    linkedPaymentAmt += ldp.getAmount();
                }
                paymentDueAmt = paymentAmt - linkedPaymentAmt;
            }
        }
        paymentDueAmt = authHandler.round(paymentDueAmt, companyid);
        return paymentDueAmt;
    }

    public static JSONArray getReceiptsJson(HashMap requestParams, List entityList, JSONArray JArr, accCurrencyDAO accCurrencyDAOobj, AccountingHandlerDAO accountingHandlerDAOobj) throws ServiceException {
        try {
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get(GoodsReceiptCMNConstants.DATEFORMAT);
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration") != null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;

            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
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

            if (requestParams.get(Constants.asOfDate) != null) {
                String curDateString = (String) requestParams.get(Constants.asOfDate);
                Date curDate = df.parse(curDateString);
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

            String oneDayBeforeCal1String = authHandler.getDateOnlyFormat().format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = authHandler.getDateOnlyFormat().parse(oneDayBeforeCal1String);

            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

            String cal2String = authHandler.getDateOnlyFormat().format(cal2.getTime());
            cal2Date = authHandler.getDateOnlyFormat().parse(cal2String);

            String cal3String = authHandler.getDateOnlyFormat().format(cal3.getTime());
            cal3Date = authHandler.getDateOnlyFormat().parse(cal3String);

            String cal4String = authHandler.getDateOnlyFormat().format(cal4.getTime());
            cal4Date = authHandler.getDateOnlyFormat().parse(cal4String);

            String cal5String = authHandler.getDateOnlyFormat().format(cal5.getTime());
            cal5Date = authHandler.getDateOnlyFormat().parse(cal5String);

            String cal6String = authHandler.getDateOnlyFormat().format(cal6.getTime());
            cal6Date = authHandler.getDateOnlyFormat().parse(cal6String);

            String cal7String = authHandler.getDateOnlyFormat().format(cal7.getTime());
            cal7Date = authHandler.getDateOnlyFormat().parse(cal7String);

            String cal8String = authHandler.getDateOnlyFormat().format(cal8.getTime());
            cal8Date = authHandler.getDateOnlyFormat().parse(cal8String);

            String cal9String = authHandler.getDateOnlyFormat().format(cal9.getTime());
            cal9Date = authHandler.getDateOnlyFormat().parse(cal9String);

            String cal10String = authHandler.getDateOnlyFormat().format(cal10.getTime());
            cal10Date = authHandler.getDateOnlyFormat().parse(cal10String);

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

                String personid = "";
                String personname = "";
                String aliasname = "";
                String personCode = "";
                String rpcurrencyid = "";

                Object[] row = (Object[]) itr.next();
                Receipt receipt = (Receipt) row[0];

                KwlReturnObject vendorobj = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                Vendor vendor = (Vendor) vendorobj.getEntityList().get(0);
                personid = vendor != null ? vendor.getID() : "";
                personname = vendor != null ? vendor.getName() : "";
                aliasname = vendor != null ? vendor.getAliasname() : "";
                personCode = vendor != null ? vendor.getAcccode(): "";
                com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();

                if (receipt.isIsadvancefromvendor() && receipt.getDepositAmount() > 0) {
                    JournalEntry je = receipt.getJournalEntry();
                    double externalCurrencyRate = je.getExternalCurrencyRate()==0?1:je.getExternalCurrencyRate();
                    obj.put(GoodsReceiptCMNConstants.BILLID, receipt.getID());
                    obj.put(GoodsReceiptCMNConstants.COMPANYID, receipt.getCompany().getCompanyID());
                    obj.put(GoodsReceiptCMNConstants.COMPANYNAME, receipt.getCompany().getCompanyName());
                    obj.put(GoodsReceiptCMNConstants.BILLNO, receipt.getReceiptNumber());
                    obj.put(GoodsReceiptCMNConstants.JOURNALENTRYID, je.getID());
                    obj.put(GoodsReceiptCMNConstants.WITHOUTINVENTORY, false);
                    obj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (receipt.getCurrency() == null ? currency.getSymbol() : receipt.getCurrency().getSymbol()));
                    obj.put(GoodsReceiptCMNConstants.CURRENCYID, (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID()));
                    obj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (receipt.getCurrency() == null ? currency.getName() : receipt.getCurrency().getName()));
                    obj.put(GoodsReceiptCMNConstants.ENTRYNO, je.getEntryNumber());
                    obj.put(GoodsReceiptCMNConstants.PERSONID, personid);
                    obj.put(GoodsReceiptCMNConstants.PERSONNAME, personname);
                    obj.put(GoodsReceiptCMNConstants.PERSONINFO, personname+"("+personCode+")");
                    obj.put(GoodsReceiptCMNConstants.ALIASNAME, aliasname);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, receipt.getDepositAmount());
                    rpcurrencyid = receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID();
//                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receipt.getDepositAmount(), rpcurrencyid, je.getEntryDate(), externalCurrencyRate).getEntityList().get(0), companyid));
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receipt.getDepositAmount(), rpcurrencyid, receipt.getCreationDate(), externalCurrencyRate).getEntityList().get(0), companyid));
//                    obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(je.getEntryDate()));
//                    obj.put(GoodsReceiptCMNConstants.DATE, df.format(je.getEntryDate()));
                    obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(receipt.getCreationDate()));
                    obj.put(GoodsReceiptCMNConstants.DATE, df.format(receipt.getCreationDate()));
                    obj.put(GoodsReceiptCMNConstants.MEMO, receipt.getMemo());
                    obj.put(GoodsReceiptCMNConstants.DELETED, receipt.isDeleted());
                    obj.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, externalCurrencyRate);
                    obj.put(GoodsReceiptCMNConstants.TYPE, "Payment Received");
                    obj.put("isRP", true);
                    obj.put(GoodsReceiptCMNConstants.ExchangeRate, "1 "+currency.getCurrencyCode()+" = "+externalCurrencyRate+" "+(receipt.getCurrency() == null ? "" : receipt.getCurrency().getCurrencyCode()));
//                    Date dueDate = df.parse(df.format(je.getEntryDate()));
                    Date dueDate = df.parse(df.format(receipt.getCreationDate()));
                    Double amountdue = receipt.getDepositAmount();

                    if (dueDate.after(oneDayBeforeCal1Date)) {
                        amountdue1 = authHandler.round(amountdue, companyid);
                    } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
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

                    
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE1, amountdue1);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE2, amountdue2);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE3, amountdue3);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE4, amountdue4);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE5, amountdue5);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE6, amountdue6);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE7, amountdue7);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE8, amountdue8);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE9, amountdue9);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE10, amountdue10);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE11, amountdue11);
                    JArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptControllerCMN.getReceiptsJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }
    
    public JSONArray getGoodsReceiptOrdersJsonMerged(JSONObject request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object> ();
            
            requestParams.put(Constants.companyKey, request.optString(Constants.companyKey));
            requestParams.put(Constants.globalCurrencyKey, request.optString(Constants.globalCurrencyKey));
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
            requestParams.put(Constants.userdf, request.get(Constants.userdf));
            
            DateFormat userDateFormat=null;
            if(request.has(Constants.userdateformat)){
                userDateFormat=new SimpleDateFormat(String.valueOf(request.opt(Constants.userdateformat)));
            }
            double quantity = 0;
            double discountPrice = 0;
            double amount;
            boolean closeflag = request.has("closeflag")?true:false;
            boolean avoidRecursiveLink = request.has("avoidRecursiveLink")?true:false;// true-skipp GR which already linked with PI
            boolean srflag = request.has("srflag")?true:false;
            boolean isFixedAsset= request.optBoolean("isFixedAsset");
            boolean FixedAsset_DOGRlinkFlag = (request.has("FA_DOGRlinkFlag") )? request.optBoolean("FA_DOGRlinkFlag"): false;
            boolean isConsignment= request.optBoolean("isConsignment");
            boolean pendingapproval = (request.has("pendingapproval"))?request.optBoolean("pendingapproval"): false;
            boolean isLineLevelTermFlag = false;//To Check Whether LinelevelTerms are applicable or not.
            String companyid = request.optString(Constants.companyKey);
            int moduleid=isFixedAsset? Constants.Acc_FixedAssets_GoodsReceipt_ModuleId:isConsignment?Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId:Constants.Acc_Goods_Receipt_ModuleId;
            if (isConsignment) {
                moduleid = Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId;
            }
            KwlReturnObject capresult = accountingHandlerDAOobj.loadObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) capresult.getEntityList().get(0);
            KwlReturnObject prefresult = accountingHandlerDAOobj.loadObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            if(extraCompanyPreferences != null && extraCompanyPreferences.getLineLevelTermFlag()==1){
                isLineLevelTermFlag = true;//If LineLevelTerms are applicable, then update the flag.
            }
            int countryid = 0;
            if(extraCompanyPreferences != null && extraCompanyPreferences.getCompany().getCountry() != null){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid,moduleid));
            HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            String compids[] = Constants.Companyids_Chkl_And_Marubishi.split(",");
            boolean isFromChklorMarubishi = false;
            for (int cnt = 0; cnt < compids.length; cnt++) {
                String compid = compids[cnt];
                if (compid.equalsIgnoreCase(companyid)) {
                    isFromChklorMarubishi = true;
                }
            }
            Iterator itr = list.iterator();
            Set<String> uniqueProductTaxList = new HashSet<String>();
            while (itr.hasNext()) {
                //SalesOrder salesOrder=(SalesOrder)itr.next();
                Object[] oj = (Object[])itr.next();                
                String orderid = oj[0].toString();
                //Withoutinventory 0 for normal, 1 for billing
        
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), orderid);
                GoodsReceiptOrder grOrder = (GoodsReceiptOrder) objItr.getEntityList().get(0);
                boolean addflag = true;
                Set<GoodsReceiptOrderDetails> doRows = grOrder.getRows();
                String status = "Open";
                if (srflag) {   //this flag is true  in purchase return case
                   // status = getGoodsReceiptOrderPRStatus(doRows);
                } else {
                    //status = grOrder.isIsOpenInPI() ? "Open" : "Closed";//getGoodsReceiptOrderStatus(doRows);
                }
                if (closeflag && grOrder.isDeleted()) {
                    addflag = false;
                    continue;
                } else if (closeflag && (status.equalsIgnoreCase("Closed"))) {
                    addflag = false;
                    continue;
                }
                amount = 0;
                discountPrice = 0;
                double totalDiscount=0;
                Set<String> invoiceno = new HashSet<String>();
                GoodsReceiptOrderDetails tempgrodobj = null;
                JSONObject obj = new JSONObject();
                double productTotalAmount = 0d;
                double subtotal = 0d;
                double ordertaxamount=0d;
                if (doRows != null && !doRows.isEmpty()) {
                    for (GoodsReceiptOrderDetails temp : doRows) {
                        quantity = temp.getInventory().getQuantity();
                        /* CHECK FOR RATE INCLUDING GST IN GOODS ORDER REPORT FOR DISCOUNT AND TOTAL AMOUNT CALCULATIONS*/
                        double dorate = 0.0d;
                        if (grOrder.isGstIncluded()) {
                            dorate = authHandler.roundUnitPrice(temp.getRateincludegst(), companyid);
                        } else {
                            dorate = authHandler.roundUnitPrice(temp.getRate(), companyid);
                        }
                        double doPrice = authHandler.round(quantity * dorate, companyid);
                        productTotalAmount += doPrice;
                        double discountDOD = authHandler.round(temp.getDiscount(), companyid);
                        if (temp.getDiscountispercent() == 1) {
                            totalDiscount+=authHandler.round((doPrice * discountDOD / 100), companyid);
                            discountPrice = (doPrice) - authHandler.round((doPrice * discountDOD / 100), companyid);;
                        } else {
                            totalDiscount+=discountDOD;
                            discountPrice = doPrice - discountDOD;
                        }
                        amount += discountPrice;
        
                        //amount += temp.getRate() * quantity;
                        tempgrodobj = temp;
                        if (tempgrodobj != null && tempgrodobj.getVidetails() != null && tempgrodobj.getVidetails().getGoodsReceipt() != null) {
                            invoiceno.add(tempgrodobj.getVidetails().getGoodsReceipt().getGoodsReceiptNumber());
                        }
                        if (avoidRecursiveLink && (temp.getVidetails() != null)) {
                            addflag = false;
                            continue;
                        }
                        if (temp.getTax() != null) {
                            uniqueProductTaxList.add(temp.getTax().getID());
                            }
                        double taxAmt = 0.0d ;
                        if (!grOrder.isGstIncluded()) {// NO NEED TO ADD TAX IF RATE INCLUDING GST
                            taxAmt = temp.getRowTaxAmount();
                            amount += taxAmt;
                        }
                        if (isLineLevelTermFlag) {
                            taxAmt += authHandler.round(temp.getRowTermAmount(), companyid);
                            amount += authHandler.round(temp.getOtherTermNonTaxableAmount(), companyid);
                                ordertaxamount += taxAmt;
                            }
                        }
                    }
                obj.put("productTotalAmount", productTotalAmount);

                    KwlReturnObject linkRresult = accLinkDataDao.checkEntryForTransactionInLinkingTableForForwardReference(Constants.Acc_GoodsReceipt_modulename, grOrder.getID());
                    list = linkRresult.getEntityList();
                    /**
                     * If a GRN has multiple linked PI's then display them with comma separation in the 
                     * Invoice Number column of GRN report : ERP-34490.
                     */
                    GoodsReceiptOrderLinking golinking = null;
                   if (list != null && !list.isEmpty()) { //if list is empty then there are no linked invoices to the GRN
                    String invoicenum = "";
                    List<String> invlist = new ArrayList<>(); //prepare list of linked invoice numbers and then create comma separated string
                    Iterator rs = list.iterator();
                    while (rs.hasNext()) {
                        GoodsReceiptOrderLinking golink = (GoodsReceiptOrderLinking) rs.next();
                        invlist.add(golink != null ? golink.getLinkedDocNo() : "");
                    }
                    invoicenum = org.springframework.util.StringUtils.collectionToCommaDelimitedString(invlist);
                    obj.put("invoiceno", invoicenum);
                    obj.put(Constants.IS_LINKED_TRANSACTION, true);
                } else {
                    obj.put(Constants.IS_LINKED_TRANSACTION, false);
                    }
                    Vendor vendor=grOrder.getVendor();
                    /**
                     * Put GST document history.
                     */
                    if (grOrder.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        obj.put("refdocid", grOrder.getID());
                        fieldDataManagercntrl.getGSTDocumentHistory(obj);

                    }
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", grOrder.getID());
                    hashMap.put("companyid", grOrder.getCompany().getCompanyID());
                    KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    obj.put("attachment", attachemntcount);
                    obj.put("billid", grOrder.getID());
                    obj.put("gtaapplicable", grOrder.isRcmApplicable());
                    obj.put("isjobworkoutrec", grOrder.isIsJobWorkOutOrder());
                    obj.put("gstapplicable", grOrder.isIsIndGSTApplied());
                    obj.put("companyid", grOrder.getCompany().getCompanyID());
                    obj.put("companyname", grOrder.getCompany().getCompanyName());
                    obj.put("externalcurrencyrate", grOrder.getExternalCurrencyRate());
                    obj.put("withoutinventory", false);
                    obj.put("personid", vendor.getID());
                    obj.put("billno", grOrder.getGoodsReceiptOrderNumber());
                    //obj.put("duedate", authHandler.getDateFormatter(request).format(salesOrder.getDueDate()));
                    obj.put("date", authHandler.getDateOnlyFormat().format(grOrder.getOrderDate()));
                    obj.put(Constants.HAS_ACCESS, vendor.isActivate());
//                MasterItem gstRegistrationType = vendor != null ? vendor.getGSTRegistrationType() : null;
//                if (gstRegistrationType != null && gstRegistrationType.getDefaultMasterItem() != null) {
//                    obj.put("GSTINRegTypeDefaultMstrID", gstRegistrationType.getDefaultMasterItem().getID());
//                }
                    if(countryid == Constants.indian_country_id){
                         obj.put("ewayapplicable", grOrder.isEwayapplicable()); // Get EWAY applicable Check - Used for INDIA only ERM-1108
                    }
                    
                    /**
                    * Get Merchant Exporter Check 
                    */
                    obj.put(Constants.isMerchantExporter, grOrder.isIsMerchantExporter());
                    obj.put("personname", vendor.getName());
                    obj.put(Constants.PERSONCODE, vendor.getAcccode());
                    obj.put("aliasname", StringUtil.isNullOrEmpty(vendor.getAliasname())?"":vendor.getAliasname());
                    obj.put("personemail", vendor.getEmail());
                    obj.put("memo", grOrder.getMemo());
                    obj.put("agent", grOrder.getMasterAgent() == null ? "" : grOrder.getMasterAgent().getID());
                    obj.put("agentname", grOrder.getMasterAgent() == null ? "" : grOrder.getMasterAgent().getValue());                    
                    obj.put("posttext", grOrder.getPostText()==null?"":grOrder.getPostText());
                    obj.put("costcenterid", grOrder.getCostcenter()==null?"":grOrder.getCostcenter().getID());
                    obj.put("costcenterName", grOrder.getCostcenter()==null?"":grOrder.getCostcenter().getName());
                    obj.put("statusID", grOrder.getStatus()==null?"":grOrder.getStatus().getID());
                    obj.put("status", grOrder.getStatus()==null?"":grOrder.getStatus().getValue());
                    obj.put(SHIPDATE, grOrder.getShipdate()==null? "" : authHandler.getDateOnlyFormat().format(grOrder.getShipdate()));
                    obj.put("termid", grOrder.getTerm()== null ? "" : grOrder.getTerm().getID());
                    obj.put("termdetails", getGoodsReceiptOrderTermDetails(grOrder.getID(),accGoodsReceiptobj));
                    if(grOrder.getTermsincludegst()!=null) {
                        obj.put(Constants.termsincludegst, grOrder.getTermsincludegst());
                    }
//                    obj.put("termamount", CommonFunctions.getTotalTermsAmount(AccGoodsReceiptServiceHandler.getGRTermDetails(grOrder.getID(),accGoodsReceiptobj)));
                    obj.put("shipvia", grOrder.getShipvia()==null?"":grOrder.getShipvia());
                    obj.put("fob", grOrder.getFob()==null?"":grOrder.getFob());                    
                    obj.put("permitNumber", grOrder.getPermitNumber()==null?"":grOrder.getPermitNumber());
                    obj.put("isfavourite", grOrder.isFavourite());
                    obj.put("isprinted", grOrder.isPrinted());
                    obj.put("isautogenerateddo", grOrder.isIsAutoGeneratedGRO());
                    obj.put("deleted", grOrder.isDeleted());
                    obj.put("currencyid", (grOrder.getCurrency() == null ? "" : grOrder.getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (grOrder.getCurrency() == null ? "" : grOrder.getCurrency().getSymbol()));
                    obj.put("currencyCode", (grOrder.getCurrency() == null ? "" : grOrder.getCurrency().getCurrencyCode()));
                    obj.put("challannumber", (grOrder.getChallanNumber() == null ? "" : grOrder.getChallanNumber()));
                    obj.put(Constants.SEQUENCEFORMATID,grOrder.getSeqformat()!=null?grOrder.getSeqformat().getID():"");
                    obj.put("isConsignment", grOrder.isIsconsignment());
                    obj.put("createdby", grOrder.getCreatedby()==null?"":StringUtil.getFullName(grOrder.getCreatedby()));
                    obj.put(Constants.SUPPLIERINVOICENO, grOrder.getSupplierInvoiceNo() != null ? grOrder.getSupplierInvoiceNo() : "");
                    if(grOrder.getModifiedby()!=null){
                            obj.put("lasteditedby",StringUtil.getFullName(grOrder.getModifiedby()));
                    }
                    obj.put("gstIncluded", grOrder.isGstIncluded());
                    if (!pendingapproval) {
                    JournalEntry inventoryJE = grOrder.getInventoryJE();
                    obj.put("inventoryjeid", (inventoryJE != null ? inventoryJE.getID() : ""));
                    obj.put("inventoryentryno", (inventoryJE != null ? inventoryJE.getEntryNumber() : ""));
                    }
                    boolean isApplyTaxToTerms=grOrder.isApplyTaxToTerms();
                    obj.put("isapplytaxtoterms", isApplyTaxToTerms);
                    
                    double taxPercent = 0;
                    double totalTermAmount = 0;
                    double taxableamount = 0;
                    double totalTermTaxAmount=0;
                    KwlReturnObject grResult = null;
                    HashMap<String, Object> requestParam = new HashMap();
                    requestParam.put("goodsReceiptID", grOrder.getID());
                    grResult = accGoodsReceiptobj.getGRTermMap(requestParam);
                    List<GoodsReceiptOrderTermMap> termMap = grResult.getEntityList();
                    for (GoodsReceiptOrderTermMap goodsReceiptOrderTermMap : termMap) {
                        double termAmnt = 0;
                        if (grOrder.isGstIncluded()) {
                            termAmnt = goodsReceiptOrderTermMap.getTermAmountExcludingTax();
                        } else {
                            termAmnt = goodsReceiptOrderTermMap.getTermamount();
                        }
                        totalTermAmount += authHandler.round(termAmnt, companyid);

                        double termTaxAmnt = goodsReceiptOrderTermMap.getTermtaxamount();
                        totalTermTaxAmount += authHandler.round(termTaxAmnt, companyid);
                    }
                    totalTermAmount = authHandler.round(totalTermAmount, companyid);
                    totalTermTaxAmount = authHandler.round(totalTermTaxAmount, companyid);
                    obj.put("termamount", totalTermAmount);
                    
                    String taxname = "";
                    if (grOrder.getTax() != null) {
                        KwlReturnObject taxresult = accTaxObj.getTaxPercent(companyid,grOrder.getOrderDate() , grOrder.getTax().getID());
                        taxPercent = (Double) taxresult.getEntityList().get(0);
                        taxname = grOrder.getTax().getName();
                        ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round(((amount + taxableamount) * taxPercent / 100), companyid));
                    }
                    obj.put( "taxid",grOrder.getTax() != null ? grOrder.getTax().getID() : "");
                    obj.put( TAXPERCENT,taxPercent);
                    obj.put("taxpercent", taxPercent);
                      if (grOrder.isGstIncluded()) {
                                    amount=amount+totalTermAmount+totalTermTaxAmount;
                      }else{
                          amount=amount+totalTermAmount+ordertaxamount+totalTermTaxAmount;
                      }

                    Set<GoodsReceiptOrderDetails> goodsReceiptOrderDetails = grOrder.getRows();
                    boolean includeprotax = false;
                    double rowTaxAmt = 0 , rowOtherTermNonTaxableAmount = 0d;
                    boolean isTaxRowLvlAndFromTaxGlobalLvl = false;
                    boolean isTransactionSendForQA = false;
                    for (GoodsReceiptOrderDetails goodsReceiptOrderDetail : goodsReceiptOrderDetails) {
                        if (goodsReceiptOrderDetail.getTax() != null) {
                            if (goodsReceiptOrderDetail.getPodetails() != null && goodsReceiptOrderDetail.getPodetails().getPurchaseOrder() != null) {
                            if (goodsReceiptOrderDetail.getPodetails().getPurchaseOrder().getTax() != null) {
                                isTaxRowLvlAndFromTaxGlobalLvl = true;
                            }
                        }
                        if (goodsReceiptOrderDetail.getVidetails()!= null && goodsReceiptOrderDetail.getVidetails().getGoodsReceipt()!= null) {
                            if (goodsReceiptOrderDetail.getVidetails().getGoodsReceipt().getTax() != null) {
                                isTaxRowLvlAndFromTaxGlobalLvl = true;
                            }
                        }
                            includeprotax = true;
                            rowTaxAmt += goodsReceiptOrderDetail.getRowTaxAmount();
                        }
                        if(isLineLevelTermFlag){
                            // Append OtherTermNonTaxableAmount for rach row.
                            rowOtherTermNonTaxableAmount += goodsReceiptOrderDetail.getOtherTermNonTaxableAmount();
                        
                        }
                        if (companyAccountPreferences.isQaApprovalFlow() && !isTransactionSendForQA) {
                            JSONObject json = new JSONObject();
                            json.put("grodid", goodsReceiptOrderDetail.getID());
                            KwlReturnObject kwlReturnObject = stockService.getGRODetailISTMapping(json);
                            List<GRODetailISTMapping> groDetailIstMappings = kwlReturnObject.getEntityList();
                            for (GRODetailISTMapping groDetailIstMapping : groDetailIstMappings) {
                                if (groDetailIstMapping.getApprovedInterStoreTransferRequests() != null && !groDetailIstMapping.getApprovedInterStoreTransferRequests().isEmpty()) {
                                    isTransactionSendForQA = true;
                                }
                                if (groDetailIstMapping.getRejectedInterStoreTransferRequests() != null && !groDetailIstMapping.getRejectedInterStoreTransferRequests().isEmpty()) {
                                    isTransactionSendForQA = true;
                                }
                                if (groDetailIstMapping.getInterStoreTransferRequest() != null) {
                                    if (groDetailIstMapping.getInterStoreTransferRequest().getStatus() != InterStoreTransferStatus.INTRANSIT) {
                                        isTransactionSendForQA = true;
                                    }
                                }
                            }
                        }
                    }
                    obj.put("isTransactionSendForQA", isTransactionSendForQA);
                    obj.put("isTaxRowLvlAndFromTaxGlobalLvl", isTaxRowLvlAndFromTaxGlobalLvl);
                    
                    obj.put("includeprotax", includeprotax);
                    obj.put("rowTaxAmt", authHandler.formattedAmount(rowTaxAmt,companyid));
                    
                    obj.put("taxamount",authHandler.formattedAmount( ordertaxamount + rowTaxAmt + totalTermTaxAmount,companyid));
                    
                    obj.put("taxname", taxname);
                    if (grOrder.isGstIncluded()) {// If you are changing sub total calculation , please notify the report builder team to change the logic at their end too
                        subtotal = productTotalAmount - totalDiscount - rowTaxAmt;
                    } else {
                        subtotal = productTotalAmount - totalDiscount;
                    }

                    if (grOrder.isGstIncluded()&&isLineLevelTermFlag) {// If you are changing sub total calculation , please notify the report builder team to change the logic at their end too
                        subtotal -=  ordertaxamount;
                    } 
//                    double termAmount= CommonFunctions.getTotalTermsAmount(AccGoodsReceiptServiceHandler.getGRTermDetails(grOrder.getID(),accGoodsReceiptobj));
                    obj.put("amountBeforeTax", (subtotal+totalTermAmount));         //For Goods Receipt
                    obj.put("subtotal", subtotal);
                    if(isLineLevelTermFlag){
                        // If LineLevelTerm is applicable then add the value in JSON Object.
                        obj.put("OtherTermNonTaxableAmount", rowOtherTermNonTaxableAmount);
                    }
                    
                    if(countryid == Constants.indian_country_id){
                        obj.put("formtypeid", grOrder.getFormtype());
                        obj.put("isInterstateParty", (grOrder.getVendor() !=null ? grOrder.getVendor().isInterstateparty() : false));
                    }
                    obj=AccountingAddressManager.getTransactionAddressJSON(obj,grOrder.getBillingShippingAddresses(),true);
                    if(invoiceno.size() > 0){
                        obj.put("invoiceno",  org.springframework.util.StringUtils.collectionToCommaDelimitedString(invoiceno));
                    }else{ 
                        if (!obj.has("invoiceno")) { //check if a forward linked invoice no has already been inserted in the object if not then proceed
                            obj.put("invoiceno", "");
                        }
                    }
                    obj.put(Constants.IsRoundingAdjustmentApplied, grOrder.isIsRoundingAdjustmentApplied());
                    if (grOrder.isIsRoundingAdjustmentApplied()) {//for rounding the exact amount is considered
                        amount = grOrder.getTotalamount();
                    }
                    obj.put("amount", authHandler.formattedAmount(amount,companyid));
                    obj.put("discount", authHandler.round(totalDiscount,companyid));
                    if(grOrder.getCurrency()!=null){
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, grOrder.getCurrency().getCurrencyID(), grOrder.getOrderDate(), grOrder.getExternalCurrencyRate());
                        obj.put("amountinbase", authHandler.formattedAmount((Double) bAmt.getEntityList().get(0),companyid));
                    }
 
                    String approvalStatus = "";
                    if (grOrder.getApprovestatuslevel() < 0) {
                        approvalStatus = "Rejected";
                    } else if (grOrder.getApprovestatuslevel() < 11) {
                        String ruleid = "", userRoleName = "";
                        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                        qdDataMap.put("companyid", companyid);
                        qdDataMap.put("level", grOrder.getApprovestatuslevel());
                        qdDataMap.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                        KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                        Iterator ruleitr = flowresult.getEntityList().iterator();
                        while (ruleitr.hasNext()) {
                            Object[] rulerow = (Object[]) ruleitr.next();
                            ruleid = rulerow[0].toString();
                        }
                        if (!StringUtil.isNullOrEmpty(ruleid)) {
                            qdDataMap.put("ruleid", ruleid);
                            KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(qdDataMap);
                            Iterator useritr = userResult.getEntityList().iterator();
                            while (useritr.hasNext()) {
                                Object[] userrow = (Object[]) useritr.next();
                                String userId = userrow[0].toString();
                                String userName = userrow[1].toString();
                                KwlReturnObject kmsg = null;
                                String roleName = "Company User";
                                kmsg = permissionHandlerDAOObj.getRoleofUser(userId);
                                Iterator ite2 = kmsg.getEntityList().iterator();
                                while (ite2.hasNext()) {
                                    Object[] row = (Object[]) ite2.next();
                                    roleName = row[1].toString();
                                }
                                userRoleName += roleName + " " + userName + ",";
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(userRoleName)) {
                            userRoleName = userRoleName.substring(0, userRoleName.length() - 1);
                        }
                        approvalStatus = "Pending Approval" + (StringUtil.isNullOrEmpty(userRoleName) ? "" : " by " + userRoleName) + " at Level - " + grOrder.getApprovestatuslevel();
                    } else {
                        approvalStatus = "Approved";
                    }
                    obj.put("approvalstatusinfo", approvalStatus);
                    obj.put("approvalstatus", grOrder.getApprovestatuslevel());
                      if (pendingapproval) {
                          int nextApprovalLevel = 11;
                          ScriptEngineManager mgr = new ScriptEngineManager();
                          ScriptEngine engine = mgr.getEngineByName("JavaScript");
                          HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                          qdDataMap.put("companyid", companyid);
                          qdDataMap.put("level", grOrder.getApprovestatuslevel() + 1);
                          qdDataMap.put("moduleid", Constants.Acc_Goods_Receipt_ModuleId);
                          KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                          Iterator approvalRuleItr = flowresult.getEntityList().iterator();
                          while (approvalRuleItr.hasNext()) {
                              Object[] rowObj = (Object[]) approvalRuleItr.next();
                              String rule = "";
                              if (rowObj[2] != null) {
                                  rule = rowObj[2].toString();
                              }
                              int appliedUpon = Integer.parseInt(rowObj[5].toString());
                              rule = rule.replaceAll("[$$]+", String.valueOf(grOrder.getTotalamountinbase()));
                              if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && ( appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon != Constants.Specific_Products_Category) && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                                  nextApprovalLevel = grOrder.getApprovestatuslevel() + 1;
                              }
                          }
                          obj.put("isFinalLevelApproval", nextApprovalLevel == 11 ? true : false);
                    }
                    boolean islinkedTransaction = false;
                    KwlReturnObject result = accGoodsReceiptobj.getPurchaseReturnLinkedWithGR(grOrder.getID(), companyid);
                    List list1 = result.getEntityList();
                    if (!list1.isEmpty()) {
                        islinkedTransaction = true;
                    }
                    obj.put("islinkedtransaction", islinkedTransaction);
                     Map<String, Object> variableMap = new HashMap<String, Object>();
                    GoodsReceiptOrderCustomData goodsReceiptOrderCustomData = (GoodsReceiptOrderCustomData)grOrder.getGoodsReceiptOrderCustomData();
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    AccountingManager.setCustomColumnValues(goodsReceiptOrderCustomData, fieldMap, replaceFieldMap,variableMap);
                if (goodsReceiptOrderCustomData != null) {
                    boolean isExport = (request.has("isExport")) ? true : false;
                    boolean linkFlag = request.has("linkFlag")  ? Boolean.parseBoolean(request.get("linkFlag").toString()) :false;
                    JSONObject params = new JSONObject();
                    params.put("isExport", isExport);
                    params.put(Constants.userdf,userDateFormat);
                    if (linkFlag || srflag || FixedAsset_DOGRlinkFlag) {
                        int moduleId = isFixedAsset ? Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId : srflag?Constants.Acc_Purchase_Return_ModuleId:Constants.Acc_Vendor_Invoice_ModuleId;
                        if(FixedAsset_DOGRlinkFlag){
                            moduleId = Constants.Acc_FixedAssets_Purchase_Return_ModuleId;
                        } else if (isConsignment){
                             moduleId = Constants.Acc_ConsignmentPurchaseReturn_ModuleId;
                        }
                        params.put("linkModuleId", moduleId);
                        params.put("isLink", true);
                        params.put("companyid",grOrder.getCompany().getCompanyID());
                        params.put("customcolumn", 0);
                    }
                    if (!StringUtil.isNullOrEmpty(request.optString("getBrowserTZ"))) {
                        params.put("browsertz", request.optString("getBrowserTZ"));
                    }
                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                }
                    if(addflag){
                        jArr.put(obj);
                    }
                }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getDeliveryOrdersJsonMerged : "+ex.getMessage(), ex);
        }
        return jArr;
    }
    

    public JSONArray getGoodsReceiptsJsonMerged(HashMap<String, Object> request, List<GoodsReceipt> list, JSONArray jArr, AccountingHandlerDAO accountingHandlerDAOobj, accCurrencyDAO accCurrencyDAOobj, accGoodsReceiptDAO accGoodsReceiptobj, accAccountDAO accAccountDAOobj, accGoodsReceiptCMN accGoodsReceiptCommon, accTaxDAO accTaxObj) throws ServiceException {
//        JSONArray jArr = new JSONArray();
        try {
            String companyid = (String) request.get(GoodsReceiptCMNConstants.COMPANYID);
            String currencyid = (String) request.get(GoodsReceiptCMNConstants.GCURRENCYID);
//            DateFormat userDateFormat = (DateFormat) request.get(Constants.userdf);
            DateFormat userDateFormat=null;
            if (request.containsKey(Constants.userdf) && request.get(Constants.userdf) != null) {
                userDateFormat = (DateFormat) request.get(Constants.userdf);
            }
            DateFormat df = (DateFormat) request.get(GoodsReceiptCMNConstants.DATEFORMAT);
            String only1099AccStr = (String) request.get(GoodsReceiptCMNConstants.ONLY1099ACC);
            DateFormat dateFormat=(DateFormat) authHandler.getDateOnlyFormat();
            List ll = null;
            /*
             * amoutinbase is used to calculate excnahnge rate for transaction  
             */
            double amountInBase=0.0;
            KwlReturnObject extraprefresult = null;
            ExtraCompanyPreferences extraCompanyPreferences = null;
            if(!StringUtil.isNullOrEmpty(companyid)){
                extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            }
            boolean isMalaysian = extraCompanyPreferences != null ? extraCompanyPreferences.getCompany().getCountry().getID().equalsIgnoreCase("137") : false;
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean isBadDebtInvoices = false;// for Malasian Company
            boolean isproductCategory=false;
            boolean isproductType = false;
            KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmp.getEntityList().get(0);
            int countryid = company.getCountry() != null ? Integer.parseInt(company.getCountry().getID()) : 0;
            request.put("countryid", countryid);
            if (request.containsKey("isBadDebtInvoices") && request.get("isBadDebtInvoices") != null) {
                isBadDebtInvoices = (Boolean) request.get("isBadDebtInvoices");
            }
            if (request.containsKey("productCategoryid") && request.get("productCategoryid")!=null && !StringUtil.isNullOrEmpty((String) request.get("productCategoryid"))) {
                isproductCategory = true;
            }
            if (request.containsKey(InvoiceConstants.productid) && request.get(InvoiceConstants.productid) != null && !StringUtil.isNullOrEmpty((String) request.get(InvoiceConstants.productid))) {
                isproductType = true;
            }
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(request.containsKey("noOfInterval") && request.get("noOfInterval") != null) {
               noOfInterval = request.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(request.get("noOfInterval").toString());
            }

            double taxPercent = 0;
            boolean belongsTo1099 = false;
            boolean isAged = (request.containsKey("isAged") && request.get("isAged") != null) ? Boolean.parseBoolean(request.get("isAged").toString()) : false;
            boolean isProduct = (request.containsKey(GoodsReceiptCMNConstants.PRODUCTID) && !StringUtil.isNullOrEmpty((String) request.get(GoodsReceiptCMNConstants.PRODUCTID))) ? true : false;
            boolean only1099Acc = (only1099AccStr != null ? Boolean.parseBoolean(only1099AccStr) : false);
            boolean ignoreZero = request.get(GoodsReceiptCMNConstants.IGNOREZERO) != null;
            boolean onlyAmountDue = request.get(GoodsReceiptCMNConstants.ONLYAMOUNTDUE) != null;
            boolean report = request.get("report") != null;
            boolean isFixedAsset = request.containsKey("isFixedAsset") ? (Boolean) request.get("isFixedAsset") : false;
            boolean isConsignment = request.containsKey("isConsignment") ? (Boolean) request.get("isConsignment") : false;
            int duration = (request.containsKey(GoodsReceiptCMNConstants.DURATION) && request.get(GoodsReceiptCMNConstants.DURATION) != null) ? Integer.parseInt(request.get(GoodsReceiptCMNConstants.DURATION).toString()) : 30;
            int invoiceLinkedWithGRNStatus = (request.containsKey("invoiceLinkedWithGRNStatus") && request.get("invoiceLinkedWithGRNStatus") != null) ? Integer.parseInt(request.get("invoiceLinkedWithGRNStatus").toString()) : 0;
            boolean isExport = (request.get("isExport") == null) ? false : (Boolean) request.get("isExport");

            Date startDate=null;
            if(request.containsKey(Constants.REQ_startdate) && request.get(Constants.REQ_startdate)!=null){
                try{
                    startDate=dateFormat.parse(request.get(Constants.REQ_startdate).toString());
                }catch(Exception ex){
                    startDate = null;
                }
            }
            String curDateString = "";
            Date curDate = null;
            boolean booleanAged = false;//Added for aged payable/receivable

            //Custom field details Maps for Global data
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();

            HashMap<String, Object> fieldrequestParams = new HashMap();
            int moduleid=isFixedAsset ? Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId :isConsignment?Constants.Acc_Consignment_GoodsReceipt_ModuleId: Constants.Acc_Vendor_Invoice_ModuleId;
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            //Custom field details Maps for Line Level data
            HashMap<String, Object> fieldrequestParamsRows = new HashMap();
            HashMap<String, String> replaceFieldMapRows = new HashMap();
            HashMap<String, String> customFieldMapRows = new HashMap();
            HashMap<String, String> customDateFieldMapRows = new HashMap();
            fieldrequestParamsRows.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParamsRows.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
            HashMap<String, Integer> fieldMapRows = null;
            fieldMapRows = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParamsRows, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows);

            List InvoiceList=new  ArrayList();
            String compids[] = Constants.Companyids_Chkl_And_Marubishi.split(",");
            boolean isFromChklorMarubishi = false;
            for (int cnt = 0; cnt < compids.length; cnt++) {
                String compid = compids[cnt];
                if (compid.equalsIgnoreCase(companyid)) {
                    isFromChklorMarubishi = true;
                }
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

            if (request.get(Constants.asOfDate) != null) {//Added for aged payable/receivable
                curDateString = (String) request.get(Constants.asOfDate);

                if (request.get("MonthlyAgeingCurrDate") != null) {
                    curDate = (Date) request.get("MonthlyAgeingCurrDate");
                } else {
                    curDate = df.parse(curDateString);
                }
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
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);     //Need to verify in multiple cases, then only take action on it
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
//            double accruedbalance = 0;
            if (list != null && !list.isEmpty()) {
                for (Object objectArr : list) {
                    Object[] oj = (Object[]) objectArr;
                    String invid = oj[0].toString();
                    //Withoutinventory 0 for normal, 1 for billing
                    boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
                    {
                        amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
                        GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);

                        
                        
                        
                        /*------- Code for Loading Data in Invoice Grid as per Applied filter of PI linking with GR--------- */
                        if (invoiceLinkedWithGRNStatus != 0) {
                            boolean invoiceLinkedWithGRNStatusFilter = false;

                            if (invoiceLinkedWithGRNStatus == 11) {//When PI is fully received
                                invoiceLinkedWithGRNStatusFilter = isInvoiceFullyLinkedWithGR(gReceipt);
                            } else if (invoiceLinkedWithGRNStatus == 12) {//When PI is not linked with GR
                                invoiceLinkedWithGRNStatusFilter = isInvoiceNotLinkedWithAnyGR(gReceipt);
                            } else if (invoiceLinkedWithGRNStatus == 13) {//When PI is partially received
                                invoiceLinkedWithGRNStatusFilter = isInvoicePartiallyLinkedWithGR(gReceipt);
                            }
                            /*--------Only relevant Data will load as per applied filter--------- */
                            if (!invoiceLinkedWithGRNStatusFilter) {
                                continue;
                            }

                        }

                        
                        //Below If Block code is used to remove duplicate invoice id's when filter on the basis of Product category or Product name
                        if (isproductCategory || isproductType) {
                            if (InvoiceList.contains(gReceipt.getID())) {
                                continue;
                            } else {
                                InvoiceList.add(gReceipt.getID());
                            }
                        }
                        JournalEntry je = null;
                        JournalEntryDetail d = null;
                        if (gReceipt.isNormalInvoice()) {
                            je = gReceipt.getJournalEntry();
                            d = gReceipt.getVendorEntry();
                        }

                        double invoiceOriginalAmt = 0d;
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceInvoice = gReceipt.isIsOpeningBalenceInvoice();
                        Date creationDate = null;

                        currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
                        Account account = null;
                        if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                            KwlReturnObject accObjItr = accountingHandlerDAOobj.getObject(Account.class.getName(), gReceipt.getVendor().getAccount().getID());
                            account = (Account) accObjItr.getEntityList().get(0);
                            externalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
                            creationDate = gReceipt.getCreationDate();
                            invoiceOriginalAmt = gReceipt.getOriginalOpeningBalanceAmount();
                        } else {
                            account = d.getAccount();
                            externalCurrencyRate = je.getExternalCurrencyRate();
                            JSONObject jObj = extraCompanyPreferences.getColumnPref() != null ? new JSONObject(extraCompanyPreferences.getColumnPref())  : new JSONObject();
                            boolean isPostingDateCheck = false;
                            if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                                isPostingDateCheck = true;
                            }
                            if(isPostingDateCheck){
                                creationDate = gReceipt.getCreationDate();
                            }else{
                                creationDate = je.getEntryDate();
                            }
                            invoiceOriginalAmt = d.getAmount();
                        }
                        double amountdue = 0,amountdueinbase = 0, deductDiscount = 0,amountDueOriginal=0.0;
                        if (gReceipt.isIsExpenseType()) {
                            if(Constants.InvoiceAmountDueFlag && !isAged){
                                ll = accGoodsReceiptCommon.getUpdatedExpGRAmountDue(request, gReceipt);
                            } else {
                                ll = accGoodsReceiptCommon.getExpGRAmountDue(request, gReceipt);
                                amountdueinbase = (Double) ll.get(5);
                            }
                            amountdue = (Double) ll.get(1);
                            belongsTo1099 = (Boolean) ll.get(3);
                        } else {
                            if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                                ll = new ArrayList();
                                ll.add(gReceipt.getOriginalOpeningBalanceAmount());
                                ll.add(gReceipt.getOpeningBalanceAmountDue());
                                ll.add("");
                                ll.add(false);
                                ll.add(0.0);
                                ll.add(gReceipt.getOpeningBalanceAmountDue());
                            } else {
                                if (Constants.InvoiceAmountDueFlag && !isAged) {
                                    ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(request, gReceipt);
                                } else {
                                    ll = accGoodsReceiptCommon.getGRAmountDue(request, gReceipt);
                                    amountdueinbase = (Double) ll.get(6);
                                }
                            }

                            amountdue = (Double) ll.get(1);
                            belongsTo1099 = (Boolean) ll.get(3);
                            deductDiscount = (Double) ll.get(4);
                            amountDueOriginal = (Double) ll.get(5);
                        }
                        if (onlyAmountDue && authHandler.round(amountdue, companyid) == 0 || (only1099Acc && !belongsTo1099)) {//remove //belongsTo1099&&gReceipt.isIsExpenseType()\\ in case of viewing all accounts. [PS]
                            continue;
                        }
                        if ((ignoreZero && authHandler.round(amountdue, companyid) <= 0)) {
                            continue;
                        }
                        int isReval = 0;
                        if (report) {
                            KwlReturnObject brdAmt = accGoodsReceiptobj.getRevalFlag(gReceipt.getID());
                            List reval = brdAmt.getEntityList();
                            if (!reval.isEmpty() && (Long) reval.get(0) > 0) {
                                isReval = 1;
                            }
                        }

                        if (request.containsKey("excludeInvoiceId") && request.get("excludeInvoiceId") != null) {
                            String excludeInvoiceId = (String) request.get("excludeInvoiceId");
                            if (gReceipt.getGoodsReceiptNumber().equals(excludeInvoiceId)) {
                                continue;
                            }
                        }
                        Vendor vendor=gReceipt.getVendor();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("invoiceID", gReceipt.getID());
                        hashMap.put("companyid", companyid);
                        KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                        int attachemntcount = object.getRecordTotalCount();
                        com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                        obj.put(GoodsReceiptCMNConstants.BILLID, gReceipt.getID());
                        obj.put("isdropshipchecked", gReceipt.isIsDropshipDocument());
                        obj.put(Constants.isDraft, gReceipt.isIsDraft());
                        boolean isForTemplate = (request.containsKey("isForTemplate") && Boolean.parseBoolean(request.get("isForTemplate").toString()))?true:false;
                        //KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                        // Company company = (Company) cmp.getEntityList().get(0);
                        // int countryid = company.getCountry() != null ? Integer.parseInt(company.getCountry().getID()) : 0;
                        // Comment this code because get Company object availble for method

                        String landedInvoice = accProductObj.consignmentInvoice(gReceipt.getID(), companyid);

                        
                        if (Constants.indian_country_id == countryid) {
                            List<ExciseDetailsInvoice> ed = null;
                            if (!isForTemplate) {
                                if (extraCompanyPreferences.isExciseApplicable()) {
                                    KwlReturnObject exciseDetails = accGoodsReceiptobj.getExciseDetails(gReceipt.getID());
//                                if (!exciseDetails.getEntityList().isEmpty()){
                                    ed = exciseDetails.getEntityList();
//                                }
                                }
                                if (extraCompanyPreferences.isExciseApplicable() && ed.size() > 0) {
                                    if (isFixedAsset) {
                                        obj.put("assetExciseid", ed.get(0).getId());
                                    } else {
                                        obj.put("exciseDetailid", ed.get(0).getId());
                                    }
                                    obj.put("suppliers", ed.get(0).getSupplier());
                                    obj.put("supplierTINSalesTAXNo", ed.get(0).getSupplierTINSalesTaxNo());
                                    obj.put("supplierExciseRegnNo", ed.get(0).getSupplierExciseRegnNo());
                                    obj.put("cstnumber", ed.get(0).getCstnumber());
                                    obj.put("supplierRange", ed.get(0).getSupplierRange());
                                    obj.put("supplierCommissionerate", ed.get(0).getSupplierCommissioneRate());
                                    obj.put("supplierAddress", ed.get(0).getSupplierAddress());
                                    obj.put("supplierImporterExporterCode", ed.get(0).getSupplierImporterExporterCode());
                                    obj.put("supplierDivision", ed.get(0).getSupplierDivision());
                                    obj.put("manufacturername", ed.get(0).getManufacturerName());
                                    obj.put("manufacturerExciseRegnNo", ed.get(0).getManufacturerExciseregnNo());
                                    obj.put("manufacturerRange", ed.get(0).getManufacturerRange());
                                    obj.put("manufacturerCommissionerate", ed.get(0).getManufacturerCommissionerate());
                                    obj.put("manufacturerDivision", ed.get(0).getManufacturerDivision());
                                    obj.put("manufacturerAddress", ed.get(0).getManufacturerAddress());
                                    obj.put("manufacturerImporterExporterCode", ed.get(0).getManufacturerImporterexporterCode());
                                    obj.put("supplierState", ed.get(0).getSupplierstate());
                                    obj.put("registrationType", ed.get(0).getRegistrationType());
                                    obj.put("UnitName", ed.get(0).getUnitname());
                                    obj.put("ECCNo", ed.get(0).getECCNo());
                                    obj.put("isExciseInvoiceWithTemplate", (!ed.get(0).getRegistrationType().equals("") || !ed.get(0).getUnitname().equals("") || !ed.get(0).getECCNo().equals("")) ? true : false);
                                }
                                HashMap tmpHashMap = new HashMap();
                                tmpHashMap.put("moduleRecordId", invid);
                                tmpHashMap.put("companyid", companyid);
                                KwlReturnObject exciseTemp = accountingHandlerDAOobj.getExciseTemplatesMap(tmpHashMap);
                                if (exciseTemp != null && exciseTemp.getEntityList().size() > 0) {
                                    ExciseDetailsTemplateMap moduleTemp = (ExciseDetailsTemplateMap) exciseTemp.getEntityList().get(0);
                                    if (moduleTemp != null) {
//                                    obj.put("manufacturerType", moduleTemp.getManufacturerType());
                                        obj.put("registrationType", moduleTemp.getRegistrationType());
                                        obj.put("UnitName", moduleTemp.getUnitname());
                                        obj.put("ECCNo", moduleTemp.getECCNo());
                                    }
                                }
                                obj.put("vvattin", !StringUtil.isNullOrEmpty(gReceipt.getVendor().getVATTINnumber())?gReceipt.getVendor().getVATTINnumber():"");
                                obj.put("vcsttin", !StringUtil.isNullOrEmpty(gReceipt.getVendor().getCSTTINnumber())?gReceipt.getVendor().getCSTTINnumber():"");
                                obj.put("veccno", !StringUtil.isNullOrEmpty(gReceipt.getVendor().getECCnumber())?gReceipt.getVendor().getECCnumber():"");
                                obj.put("vservicetaxregno", !StringUtil.isNullOrEmpty(gReceipt.getVendor().getSERVICEnumber())?gReceipt.getVendor().getSERVICEnumber():"");
                                obj.put("vattinno", !StringUtil.isNullOrEmpty(vendor.getVATTINnumber())?vendor.getVATTINnumber():"");
                                obj.put("csttinno", !StringUtil.isNullOrEmpty(vendor.getCSTTINnumber())?vendor.getCSTTINnumber():"");
                                obj.put("eccno", !StringUtil.isNullOrEmpty(vendor.getECCnumber())?vendor.getECCnumber():"");
                                obj.put("panno", !StringUtil.isNullOrEmpty(vendor.getPANnumber())?vendor.getPANnumber():"");
                                obj.put("servicetaxno", !StringUtil.isNullOrEmpty(vendor.getSERVICEnumber())?vendor.getSERVICEnumber():"");
                                obj.put("tanno", !StringUtil.isNullOrEmpty(vendor.getTANnumber())?vendor.getTANnumber():"");
                                obj.put("formtypeid", !StringUtil.isNullOrEmpty(gReceipt.getFormtype()) ? gReceipt.getFormtype() : 0);
                                obj.put("gtaapplicable", gReceipt.isGtaapplicable());
                                obj.put("gstapplicable", gReceipt.isIsIndGSTApplied());
                                obj.put("isInterstateParty", gReceipt.getVendor().isInterstateparty());
                                obj.put("formseriesno", !StringUtil.isNullOrEmpty(gReceipt.getFormseriesno()) ? gReceipt.getFormseriesno() : "");
                                obj.put("formno", !StringUtil.isNullOrEmpty(gReceipt.getFormno()) ? gReceipt.getFormno() : "");
                                obj.put("formdate", gReceipt.getFormdate());
                                obj.put("formamount", gReceipt.getFormamount());
                                if (!StringUtil.isNullOrEmpty(gReceipt.getFormstatus())) {
                                    if (gReceipt.getFormstatus().equals("1")) {
                                        obj.put("formstatus", "NA");
                                    } else if (gReceipt.getFormstatus().equals("2")) {
                                        obj.put("formstatus", "Pending");
                                    } else if (gReceipt.getFormstatus().equals("3")) {
                                        obj.put("formstatus", "Submitted");
                                    }
                                } else{
                                    obj.put("formstatus", "NA");
                                }
                            } else {
                                if (company.getCountry() != null && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id && extraCompanyPreferences.isExciseApplicable()) {
                                    obj.put("suppliers", vendor.getName());
                                    obj.put("supplierCommissionerate", vendor.getCommissionerate() != null ? vendor.getCommissionerate() : "");
                                    obj.put("supplierDivision", vendor.getDivision() != null ? vendor.getDivision() : "");
                                    obj.put("supplierRange", vendor.getRangecode() != null ? vendor.getRangecode() : "");
                                    obj.put("supplierImporterExporterCode", vendor.getIECNo() != null ? vendor.getIECNo() : "");
                                    obj.put("cstnumber", vendor.getCSTTINnumber() != null ? vendor.getCSTTINnumber() : "");
                                    obj.put("supplierTINSalesTAXNo", vendor.getVATTINnumber() != null ? vendor.getVATTINnumber() : "");
                                    obj.put("supplierExciseRegnNo", vendor.getECCnumber() != null ? vendor.getECCnumber() : "");

                                    HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                                    addrRequestParams.put("vendorid", vendor.getID());
                                    addrRequestParams.put("companyid", companyid);
                                    addrRequestParams.put("isBillingAddress", true);//only billing address   
                                    KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                                    if (!addressResult.getEntityList().isEmpty()) {
                                        List<VendorAddressDetails> casList = addressResult.getEntityList();
                                        if (casList.size() > 0) {
                                            VendorAddressDetails vas = (VendorAddressDetails) casList.get(0);
                                            String fullAddress = "";
                                            if (!StringUtil.isNullOrEmpty(vas.getAddress())) {
                                                fullAddress += vas.getAddress() + ", ";
                                            }
                                            if (!StringUtil.isNullOrEmpty(vas.getCity())) {
                                                fullAddress += vas.getCity() + ", ";
                                            }
                                            if (!StringUtil.isNullOrEmpty(vas.getState())) {
                                                fullAddress += vas.getState() + ", ";
                                            }
                                            if (!StringUtil.isNullOrEmpty(vas.getCountry())) {
                                                fullAddress += vas.getCountry() + ", ";
                                            }
                                            if (!StringUtil.isNullOrEmpty(fullAddress)) {
                                                fullAddress = fullAddress.substring(0, fullAddress.length() - 2);
                                            }
                                            obj.put("supplierAddress", fullAddress);
                                            obj.put("supplierState", vas.getState() != null ? vas.getState() : "");
                                        }
                                    }
                                }
                            }
                            /**
                             * Get Module template and its mapped Unit details for company if Line level term flag ON 
                             */
                                if(extraCompanyPreferences!=null && extraCompanyPreferences.getLineLevelTermFlag()==1 && isForTemplate){
                                HashMap<String, Object> ModuleTempParams = new HashMap<>();
                                ModuleTempParams.put("modulerecordid", invid);
                                ModuleTempParams.put("companyid", companyid);
                                    /** Get Module template  from invoice id . In module template invoice id add as modulerecordid */
                                KwlReturnObject ModuleTempObj = accountingHandlerDAOobj.getModuleTemplates(ModuleTempParams);
                                    if(ModuleTempObj!=null && ModuleTempObj.getEntityList().size() > 0){
                                    ModuleTemplate moduleTemp = (ModuleTemplate) ModuleTempObj.getEntityList().get(0);
                                    obj.put("companyunitid", moduleTemp.getCompanyUnitid());

                                    HashMap tmpHashMap = new HashMap();
                                    tmpHashMap.put("companyunitid", moduleTemp.getCompanyUnitid());
                                    tmpHashMap.put(Constants.companyKey, companyid);
                                    /* Get Company Unit details from companyunitid mapped with module template */
                                    KwlReturnObject exciseTemp = accountingHandlerDAOobj.getExciseTemplatesMap(tmpHashMap);
                                        if (exciseTemp != null && exciseTemp.getEntityList()!=null && exciseTemp.getEntityList().size() > 0) {
                                        ExciseDetailsTemplateMap ExcisemoduleTemp = (ExciseDetailsTemplateMap) exciseTemp.getEntityList().get(0);
                                        if (ExcisemoduleTemp != null) {
                                            obj.put("registrationType", ExcisemoduleTemp.getRegistrationType());
                                            obj.put("UnitName", ExcisemoduleTemp.getUnitname());
                                            obj.put("ECCNo", ExcisemoduleTemp.getECCNo());
                                        }
                                    }
                                }
                            }
                            if (extraCompanyPreferences.isExciseApplicable()) {
                                KwlReturnObject grDetailsRes = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), gReceipt.getID());
                                GoodsReceipt goodsReceiptDetail = (GoodsReceipt) grDetailsRes.getEntityList().get(0);
                                if (goodsReceiptDetail.isIsExciseInvoice()) {
                                    Set<GoodsReceiptDetail> rows = goodsReceiptDetail.getRows();
                                    for (GoodsReceiptDetail goodsReceiptDetailsRow : rows) {
                                        KwlReturnObject result = accGoodsReceiptobj.getSupplierExciseDetailsMapping(goodsReceiptDetailsRow.getID(), companyid);   //while deleting GR check wether it is used in Consignment Cost
                                        list = result.getEntityList();
                                        if (list != null && !list.isEmpty()) {
                                            obj.put("isSupplierLinekd", true);
                                            break;
                                        }
                                    }
                                }
                            }
                            /**
                             * Put GST document history.
                             */
                            if (gReceipt.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                                obj.put("refdocid", gReceipt.getID());
                                fieldDataManagercntrl.getGSTDocumentHistory(obj);
                            }
                            /**
                             * Put Merchant Exporter Check
                             */
                            obj.put(Constants.isMerchantExporter, gReceipt.isIsMerchantExporter());

                                
                                
                        }
                        if (company.getCountry() != null && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id && extraCompanyPreferences.isTDSapplicable()) {
                            //For Indian Country related fields
                            obj.put("TotalAdvanceTDSAdjustmentAmt", gReceipt.getTotalAdvanceTDSAdjustmentAmt());
                            obj.put("natureOfPayment", gReceipt.getVendor() != null ? gReceipt.getVendor().getNatureOfPayment() : "");
                            obj.put("deducteetype", gReceipt.getVendor() != null ? gReceipt.getVendor().getDeducteeType() : "");
                            obj.put("residentialstatus", gReceipt.getVendor() != null ? gReceipt.getVendor().getResidentialstatus() : "");
                            String tdsPayableAccount = "";
                            MasterItem masterItem2 = null;
                            if (!StringUtil.isNullOrEmpty(vendor.getNatureOfPayment())) {
                                KwlReturnObject catresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), vendor.getNatureOfPayment());
                                masterItem2 = (MasterItem) catresult.getEntityList().get(0);
                                obj.put("natureOfPaymentname", masterItem2.getCode() + " - " + masterItem2.getValue());//INDIAN Company for TDS Calculation
                                tdsPayableAccount = masterItem2.getAccID();
                            } else {
                                obj.put("natureOfPaymentname", "");
                            }
                            if (!StringUtil.isNullOrEmpty(vendor.getDeducteeType())) {
                                KwlReturnObject catresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), vendor.getDeducteeType());
                                masterItem2 = (MasterItem) catresult.getEntityList().get(0);
                                obj.put("deducteetypename", (masterItem2!=null)?masterItem2.getValue():"");//INDIAN Company for TDS Calculation in Make Payment
                            } else {
                                obj.put("deducteetypename", "");
                            }
                            obj.put("tdsPayableAccount", tdsPayableAccount);
                        }

                      
                        HashMap<String, Object> ModuleTempParams = new HashMap<>();
                        ModuleTempParams.put("modulerecordid", invid);
                        ModuleTempParams.put("companyid", companyid);
                        /**
                         * Get Module template from invoice id . In module
                         * template invoice id add as modulerecordid
                         */
                        KwlReturnObject ModuleTempObj = accountingHandlerDAOobj.getModuleTemplates(ModuleTempParams);
                        if (ModuleTempObj != null && ModuleTempObj.getEntityList().size() > 0) {
                            ModuleTemplate moduleTemp = (ModuleTemplate) ModuleTempObj.getEntityList().get(0);
                            obj.put("companyunitid", moduleTemp.getCompanyUnitid());
                            obj.put("populateproducttemplate", moduleTemp.isPopulateproductintemp());
                            obj.put("populatecustomertemplate", moduleTemp.isPopulatecustomerintemp());
                            obj.put("populateautodointemp", moduleTemp.isPopulateautodointemp());
                        }
//                        MasterItem gstRegistrationType = vendor != null ? vendor.getGSTRegistrationType() : null;
//                        if (gstRegistrationType != null && gstRegistrationType.getDefaultMasterItem() != null) {
//                            obj.put("GSTINRegTypeDefaultMstrID", gstRegistrationType.getDefaultMasterItem().getID());
//                        }
                        obj.put("tdsrate", gReceipt.getTdsRate());
                        obj.put("tdsamount", gReceipt.getTdsAmount());
                        obj.put("tdsmasterrateruleid", gReceipt.getTdsMasterRateRuleId());
                        obj.put("isInterstateParty", (gReceipt.getVendor() !=null ? gReceipt.getVendor().isInterstateparty() : false));
                        obj.put("isOpeningBalanceTransaction", gReceipt.isIsOpeningBalenceInvoice());
                        obj.put("isExciseInvoice", gReceipt.isIsExciseInvoice());
                        obj.put("defaultnatureofpurchase", gReceipt.getDefaultnatureOfPurchase());
                        obj.put("manufacturertype", gReceipt.getManufacturerType());
                        obj.put("isNormalTransaction", gReceipt.isNormalInvoice());
                        obj.put("parentinvoiceid", gReceipt.getParentInvoice()!=null?gReceipt.getParentInvoice().getID():"");
                        obj.put("companyid", gReceipt.getCompany().getCompanyID());
                        obj.put("companyname", gReceipt.getCompany().getCompanyName());
                        obj.put("withoutinventory", withoutinventory);
                        obj.put(Constants.HAS_ACCESS, vendor.isActivate());
                        obj.put(GoodsReceiptCMNConstants.PERSONID,vendor == null ? account.getID() : vendor.getID());
                        obj.put(GoodsReceiptCMNConstants.ALIASNAME, vendor == null ? "" : vendor.getAliasname());
                        obj.put(GoodsReceiptCMNConstants.PERSONEMAIL, vendor == null ? "" : vendor.getEmail());
                        obj.put("code", vendor == null ? "" : vendor.getAcccode());
                        obj.put(GoodsReceiptCMNConstants.BILLNO, gReceipt.getGoodsReceiptNumber());
                        obj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                        obj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
                        obj.put("currencyCode", (gReceipt.getCurrency() == null ? currency.getCurrencyCode() : gReceipt.getCurrency().getCurrencyCode()));
                        obj.put("currencycode", (gReceipt.getCurrency() == null ? currency.getCurrencyCode() : gReceipt.getCurrency().getCurrencyCode()));
                        obj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (gReceipt.getCurrency() == null ? currency.getName() : gReceipt.getCurrency().getName()));
                        obj.put(GoodsReceiptCMNConstants.COMPANYADDRESS, gReceipt.getCompany().getAddress());
                        obj.put(GoodsReceiptCMNConstants.COMPANYNAME, gReceipt.getCompany().getCompanyName());
//                        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(request, 1.0, currencyid, creationDate, 0);
                        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(request, 1.0, currencyid, gReceipt.getCreationDate(), 0);
                        obj.put(GoodsReceiptCMNConstants.OLDCURRENCYRATE, bAmt.getEntityList().get(0));
                        obj.put(GoodsReceiptCMNConstants.BILLTO, gReceipt.getBillFrom());
                        obj.put(GoodsReceiptCMNConstants.ISEXPENSEINV, gReceipt.isIsExpenseType());
                        obj.put(GoodsReceiptCMNConstants.SHIPTO, gReceipt.getShipFrom());
                        obj.put(GoodsReceiptCMNConstants.JOURNALENTRYID, je != null ? je.getID() : "");
                        obj.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, externalCurrencyRate);
                        obj.put(GoodsReceiptCMNConstants.ENTRYNO, je != null ? je.getEntryNumber() : "");
                        obj.put(GoodsReceiptCMNConstants.DATE, df.format(creationDate));
                        obj.put(GoodsReceiptCMNConstants.SHIPDATE, gReceipt.getShipDate() == null ? "" : df.format(gReceipt.getShipDate()));
                        obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(gReceipt.getDueDate()));
                        obj.put(GoodsReceiptCMNConstants.PERSONNAME, vendor == null ? account.getName() : vendor.getName());
                        obj.put("personcode", vendor == null ? (account.getAcccode()==null?"":account.getAcccode()) : (vendor.getAcccode()==null?"":vendor.getAcccode()));
                        obj.put(GoodsReceiptCMNConstants.PERSONINFO, vendor == null ? (account.getAcccode()==null?"":account.getAcccode()) : (vendor.getName()+"("+vendor.getAcccode()+")"));
                        obj.put("agent", gReceipt.getMasterAgent() == null ? "" : gReceipt.getMasterAgent().getID());
                        obj.put("agentname", gReceipt.getMasterAgent() == null ? "" : gReceipt.getMasterAgent().getValue());
                        obj.put(GoodsReceiptCMNConstants.MEMO, gReceipt.getMemo());
                        obj.put("posttext", gReceipt.getPostText());
                        obj.put("shiplengthval", gReceipt.getShiplength());
                        obj.put("invoicetype", gReceipt.getInvoicetype());
                        obj.put("purchaseinvoicetype",gReceipt.isIsExpenseType() ? "Expense" : "Product");
                        obj.put(GoodsReceiptCMNConstants.TERMNAME, vendor == null ? "" : ((vendor.getDebitTerm() == null) ? "" : vendor.getDebitTerm().getTermname()));
                        obj.put(GoodsReceiptCMNConstants.DELETED, gReceipt.isDeleted());
                        obj.put(GoodsReceiptCMNConstants.TAXINCLUDED, gReceipt.getTax() == null ? false : true);
                        obj.put(GoodsReceiptCMNConstants.TAXID, gReceipt.getTax() == null ? "" : gReceipt.getTax().getID());
//                        obj.put(GoodsReceiptCMNConstants.TAXNAME, gReceipt.getTax() == null ? "" : gReceipt.getTax().getName());
                        obj.put(GoodsReceiptCMNConstants.ExchangeRate, "1 "+currency.getCurrencyCode()+" = "+externalCurrencyRate+" "+(gReceipt.getCurrency() == null ? "" : gReceipt.getCurrency().getCurrencyCode()));
                        obj.put("status", gReceipt.isIsOpenInGR()?"Open":"Closed");
                        obj.put("amountDueStatus", gReceipt.isIsOpenPayment()? "Open" : "Closed");
                        obj.put("isTDSApplicable", gReceipt.isIsTDSApplicable());// TDS Applicable Flag where at time of creating invoice TDS Applicable or not
                        obj.put(Constants.SUPPLIERINVOICENO, gReceipt.getSupplierInvoiceNo()!=null? gReceipt.getSupplierInvoiceNo():"");
                        obj.put(Constants.importExportDeclarationNo, gReceipt.getImportDeclarationNo()!=null? gReceipt.getImportDeclarationNo():"");
                        obj.put(Constants.IsRoundingAdjustmentApplied, gReceipt.isIsRoundingAdjustmentApplied());
                        obj.put(Constants.isCreditable, isExport ? (gReceipt.isIsCreditable() ? "Yes" : "No") : gReceipt.isIsCreditable());

                        if (!StringUtil.isNullObject(gReceipt.getBillingShippingAddresses())) {
                            obj.put("billingAddContactPerson", gReceipt.getBillingShippingAddresses().getBillingContactPerson() != null ? gReceipt.getBillingShippingAddresses().getBillingContactPerson() : "");
                            obj.put("shippingAddContactPerson", gReceipt.getBillingShippingAddresses().getShippingContactPerson() != null ? gReceipt.getBillingShippingAddresses().getShippingContactPerson() : "");
                            obj.put("billingAddContactNo", gReceipt.getBillingShippingAddresses().getBillingContactPersonNumber() != null ? gReceipt.getBillingShippingAddresses().getBillingContactPersonNumber() : "");
                            obj.put("shippingAddContactNo", gReceipt.getBillingShippingAddresses().getShippingContactPersonNumber() != null ? gReceipt.getBillingShippingAddresses().getShippingContactPersonNumber() : "");
                            obj.put("BillingAddEmail", gReceipt.getBillingShippingAddresses().getBillingEmail() != null ? gReceipt.getBillingShippingAddresses().getBillingEmail() : "");
                            obj.put("shippingAddEmail", gReceipt.getBillingShippingAddresses().getShippingEmail() != null ? gReceipt.getBillingShippingAddresses().getShippingEmail() : "");
                        } else {
                            obj.put("billingAddContactPerson", "");
                            obj.put("shippingAddContactPerson", "");
                            obj.put("billingAddContactNo", "");
                            obj.put("shippingAddContactNo", "");
                            obj.put("BillingAddEmail", "");
                            obj.put("shippingAddEmail", "");
                        }

                        Set<LccManualWiseProductAmount> manualProductDetailsSet = gReceipt.getLccmanualwiseproductamount() != null ? (Set<LccManualWiseProductAmount>) gReceipt.getLccmanualwiseproductamount() : null;
                        if (manualProductDetailsSet != null && !manualProductDetailsSet.isEmpty()) {
                            JSONArray manuProductDetailsJArr = new JSONArray();
                            for (LccManualWiseProductAmount lccManualWiseProductAmountObj : manualProductDetailsSet) {
                                JSONObject manuProductDetailsJOBJ = new JSONObject();
                                manuProductDetailsJOBJ.put("id", lccManualWiseProductAmountObj.getID());
                                manuProductDetailsJOBJ.put("billid", lccManualWiseProductAmountObj.getGrdetailid().getGoodsReceipt().getID());
                                manuProductDetailsJOBJ.put("rowid", lccManualWiseProductAmountObj.getGrdetailid().getID());
                                manuProductDetailsJOBJ.put("originalTransactionRowid", lccManualWiseProductAmountObj.getGrdetailid().getID());
                                manuProductDetailsJOBJ.put("productid", lccManualWiseProductAmountObj.getGrdetailid().getInventory().getProduct().getID());
                                manuProductDetailsJOBJ.put("billno", lccManualWiseProductAmountObj.getGrdetailid().getGoodsReceipt().getGoodsReceiptNumber());
                                manuProductDetailsJOBJ.put("productname", lccManualWiseProductAmountObj.getGrdetailid().getInventory().getProduct().getName());
                                manuProductDetailsJOBJ.put("enterpercentage", lccManualWiseProductAmountObj.getPercentage());
                                manuProductDetailsJOBJ.put("enteramount", lccManualWiseProductAmountObj.getAmount());
                                if (lccManualWiseProductAmountObj.getAssetDetails() != null) {
                                    manuProductDetailsJOBJ.put("productname", lccManualWiseProductAmountObj.getAssetDetails().getAssetId());
                                }
                                manuProductDetailsJOBJ.put("assetId", lccManualWiseProductAmountObj.getAssetDetails() != null ? lccManualWiseProductAmountObj.getAssetDetails().getAssetId() : "");
                                if (lccManualWiseProductAmountObj.isCustomDutyAllocationType()) {
                                    manuProductDetailsJOBJ.put("igstamount", lccManualWiseProductAmountObj.getIgstamount());
                                    manuProductDetailsJOBJ.put("igstrate", lccManualWiseProductAmountObj.getIgstrate());
                                    manuProductDetailsJOBJ.put("taxablevalueforigst", lccManualWiseProductAmountObj.getTaxablevalueforigst());
                                    manuProductDetailsJOBJ.put("customdutyandothercharges", lccManualWiseProductAmountObj.getCustomdutyandothercharges());
                                    manuProductDetailsJOBJ.put("customdutyandothercharges", lccManualWiseProductAmountObj.getCustomdutyandothercharges());
                                    manuProductDetailsJOBJ.put("taxablevalueforcustomduty", lccManualWiseProductAmountObj.getTaxablevalueforcustomduty());
                                    int hsncolnum = 0, producttaxcolnum = 0;
                                    HashMap fieldparams = new HashMap<>();
                                    fieldparams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "fieldname"));
                                    fieldparams.put(Constants.filter_values, Arrays.asList(companyid, lccManualWiseProductAmountObj.getGrdetailid().getGoodsReceipt().isFixedAssetInvoice() ? Constants.Acc_FixedAssets_AssetsGroups_ModuleId : Constants.Acc_Product_Master_ModuleId, "Custom_" + Constants.HSN_SACCODE));

                                    KwlReturnObject kwlReturnObjectGstCust = fieldManagerDAOobj.getFieldParams(fieldparams);
                                    List<FieldParams> fieldParamses = kwlReturnObjectGstCust.getEntityList();
                                    for (FieldParams fieldParams : fieldParamses) {
                                        hsncolnum = fieldParams.getColnum();
                                    }
                                    fieldparams = new HashMap<>();
                                    fieldparams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "fieldname"));
                                    fieldparams.put(Constants.filter_values, Arrays.asList(companyid, lccManualWiseProductAmountObj.getGrdetailid().getGoodsReceipt().isFixedAssetInvoice() ? Constants.Acc_FixedAssets_AssetsGroups_ModuleId : Constants.Acc_Product_Master_ModuleId, "Custom_" + Constants.GSTProdCategory));

                                    kwlReturnObjectGstCust = fieldManagerDAOobj.getFieldParams(fieldparams);
                                    fieldParamses = kwlReturnObjectGstCust.getEntityList();
                                    for (FieldParams fieldParams : fieldParamses) {
                                        producttaxcolnum = fieldParams.getColnum();
                                    }
                                    manuProductDetailsJOBJ.put("producttaxcolnum", producttaxcolnum);
                                    manuProductDetailsJOBJ.put("hsncolnum", hsncolnum);
                                    List temp = fieldManagerDAOobj.getFieldComboValue(hsncolnum,  lccManualWiseProductAmountObj.getGrdetailid().getInventory().getProduct().getID());
                                    if (list != null && !temp.isEmpty()) {
                                        Object[] tempArr = (Object[]) temp.get(0);
                                        String hsncode = (String) tempArr[0];
                                        manuProductDetailsJOBJ.put("hsncode", hsncode);
                                    }
                                    temp = fieldManagerDAOobj.getFieldComboValue(producttaxcolnum, lccManualWiseProductAmountObj.getGrdetailid().getInventory().getProduct().getID());
                                    if (temp != null && !temp.isEmpty()) {
                                        Object[] tempArr = (Object[]) temp.get(0);
                                        String producttaxclassvalue = (String) tempArr[0];
                                        manuProductDetailsJOBJ.put("producttaxclass", producttaxclassvalue);
                                    }
                                }
                                manuProductDetailsJArr.put(manuProductDetailsJOBJ);
                            }
                            //manuProductDetailsJOBJTemp.put("data", manuProductDetailsJArr);
                            obj.put("manualLandedCostCategory", manuProductDetailsJArr.toString());
                        }
                        double taxAmt = 0d;
                        if(isopeningBalanceInvoice){
                            taxAmt = gReceipt.getTaxamount();
                        } else {
                            if (gReceipt.getTaxEntry() != null) {// if Invoice Level Tax is available
                                taxAmt = gReceipt.getTaxEntry() == null ? 0 : gReceipt.getTaxEntry().getAmount();
                                //                            obj.put(GoodsReceiptCMNConstants.TAXAMOUNT, gReceipt.getTaxEntry() == null ? 0 : gReceipt.getTaxEntry().getAmount());
                            }
                        }
                        /**
                         * Finding Term Mapped to Invoice and Discount Mapped to
                         * Term and passing discount value, Type, applicable
                         * days, Purchase invoice date and amount due of invoice
                         * because these are used on JS side for calculation of
                         * discount while making bulk payment of selected invoice.
                         * ERM-981.
                         */
                        JSONObject columnPrefJObj = null;
                        if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                            columnPrefJObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                            if (columnPrefJObj.has(Constants.DISCOUNT_ON_PAYMENT_TERMS) && columnPrefJObj.get(Constants.DISCOUNT_ON_PAYMENT_TERMS) != null && columnPrefJObj.optBoolean(Constants.DISCOUNT_ON_PAYMENT_TERMS, false)) {
                                obj.put("grcreationdate", je != null ? je.getEntryDate() : creationDate);
                            }
                        }
                        obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
//                        obj.put("amountbeforegst", gReceipt.getTaxEntry() == null ? invoiceOriginalAmt : (invoiceOriginalAmt - gReceipt.getTaxEntry().getAmount()));
                        obj.put(GoodsReceiptCMNConstants.DISCOUNT, gReceipt.getDiscountAmount()); //Discount according to created transaction.
                        obj.put("discountinbase", gReceipt.getDiscountAmountInBase()); //Discount according to created transaction.
                        obj.put(GoodsReceiptCMNConstants.ISPERCENTDISCOUNT, gReceipt.getDiscount() == null ? false : gReceipt.getDiscount().isInPercent());
                        obj.put(GoodsReceiptCMNConstants.DISCOUNTVAL, gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscount());
                        obj.put(CCConstants.JSON_costcenterid, je != null ? (je.getCostcenter() == null ? "" : je.getCostcenter().getID()) : "");
                        obj.put(CCConstants.JSON_costcenterName, je != null ? (je.getCostcenter() == null ? "" : je.getCostcenter().getName()) : "");
                        obj.put("isfavourite", gReceipt.isFavourite());
                        obj.put("isprinted", gReceipt.isPrinted());
                        obj.put("isEmailSent", gReceipt.isIsEmailSent());
                        obj.put("cashtransaction", gReceipt.isCashtransaction());
                        obj.put("archieve", 0);
                        obj.put("shipvia", gReceipt.getShipvia() == null ? "" : gReceipt.getShipvia());
                        obj.put("fob", gReceipt.getFob() == null ? "" : gReceipt.getFob());
                        obj.put("termdetails", getPurchaseInvoiceTermDetails(gReceipt.getID(), accGoodsReceiptobj));
                        boolean isApplyTaxToTerms=gReceipt.isApplyTaxToTerms();
                        obj.put("isapplytaxtoterms", isApplyTaxToTerms);
                        if (gReceipt.getTermsincludegst() != null) {
                            obj.put(Constants.termsincludegst, gReceipt.getTermsincludegst());
                        }
                        KwlReturnObject result = accLinkDataDao.checkEntryForTransactionInLinkingTableForForwardReference("GoodsReceipt", gReceipt.getID());
                        list = result.getEntityList();
                        KwlReturnObject linkedDebitNoteResult = accGoodsReceiptobj.getCreditNoteLinkedWithInvoice(gReceipt.getID(), companyid);
                        List listDn = linkedDebitNoteResult.getEntityList();

                        KwlReturnObject linkedDNForOverchargeResult = accGoodsReceiptobj.getDebitNoteForOverchargedLinkedWithInvoice(gReceipt.getID(), companyid);
                        List dnOverchargelist = linkedDNForOverchargeResult.getEntityList();

                        /*
                         * TDS Payment is Done - Set true IsLinkedTransaction 
                         */
                        List listtdspayment = null;
                        if (Constants.indian_country_id == countryid) {
                            KwlReturnObject linkedTDSPaymentObj = accGoodsReceiptobj.getGoodsReceiptTDSPayment(gReceipt.getID(), companyid);
                            listtdspayment = linkedTDSPaymentObj.getEntityList();
                        }
                        if ((list != null && !list.isEmpty())||(!StringUtil.isNullOrEmpty(landedInvoice))||(!gReceipt.isCashtransaction() && (authHandler.round((Double)invoiceOriginalAmt,companyid)!=authHandler.round(amountdue, companyid))) || (listDn!=null && !listDn.isEmpty()) || (listtdspayment!=null && !listtdspayment.isEmpty()) || (dnOverchargelist != null && !dnOverchargelist.isEmpty())){
                            obj.put(Constants.IS_LINKED_TRANSACTION, true);
                        } else {
                            obj.put(Constants.IS_LINKED_TRANSACTION, false);
                        }
                        /*
                         * Check if invoice is claimed as bad debt
                         */
                        if(gReceipt.getClaimAmountDue()!= 0){
                            obj.put("isClaimedTransaction", true);
                        }
//                        double termAmount = CommonFunctions.getTotalTermsAmount(getTermDetails(gReceipt.getID(), accGoodsReceiptobj));
//                        obj.put("termamount", termAmount);
                        obj.put("termdays", gReceipt.getTermid() == null ? 0 : gReceipt.getTermid().getTermdays());
                        obj.put("termid", gReceipt.getTermid() == null ? "" : gReceipt.getTermid().getID());
                        //ERP-20637
                        if (gReceipt.getLandedInvoice() != null) {
                            Set<GoodsReceipt> landInvoiceSet = gReceipt.getLandedInvoice();
                            String landedInvoiceId = "", landedInvoiceNumber = "";
                            for (GoodsReceipt grObj : landInvoiceSet) {
                                if (!(StringUtil.isNullOrEmpty(landedInvoiceId) && StringUtil.isNullOrEmpty(landedInvoiceId))) {
                                    landedInvoiceId += ",";
                                    landedInvoiceNumber += ",";
                                }
                                landedInvoiceId += grObj.getID();
                                landedInvoiceNumber += grObj.getGoodsReceiptNumber();
                            }
                            obj.put("landedInvoiceID", landedInvoiceId);
                            obj.put("landedInvoiceNumber", landedInvoiceNumber);
                        }
//                        obj.put("landedInvoiceID", gReceipt.getLandedInvoice() == null ? "" : gReceipt.getLandedInvoice().getID());
//                        obj.put("landedInvoiceNumber", gReceipt.getLandedInvoice() == null ? "" : gReceipt.getLandedInvoice().getGoodsReceiptNumber());
                        obj.put("billto", gReceipt.getBillTo() == null ? "" : gReceipt.getBillTo());
                        obj.put("shipto", gReceipt.getShipTo() == null ? "" : gReceipt.getShipTo());
                        obj.put("isCapitalGoodsAcquired", gReceipt.isCapitalGoodsAcquired());
                        obj.put("isRetailPurchase", gReceipt.isRetailPurchase());
                        obj.put("importService", gReceipt.isImportService());
                        obj.put("attachment", attachemntcount);
                        obj.put(Constants.isDraft, gReceipt.isIsDraft());
                        obj.put("isConsignment", gReceipt.isIsconsignment());
                        obj.put("landingCostCategoryCombo", gReceipt.getLandingCostCategory()!=null?gReceipt.getLandingCostCategory().getId():"");
                        Set<GoodsReceiptDetail> goodsReceiptDetails = gReceipt.getRows();
                        // Calculating total invoice amount in base currency
                        KwlReturnObject invoiceTotalAmtInBaseResult=null;
                        KwlReturnObject taxTotalAmtInBaseResult=null;
                        if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
//                            invoiceTotalAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, invoiceOriginalAmt, currencyid, creationDate, externalCurrencyRate);
                            invoiceTotalAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, invoiceOriginalAmt, currencyid, gReceipt.getCreationDate(), externalCurrencyRate);
                        } else {
//                            invoiceTotalAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(request, invoiceOriginalAmt, currencyid, creationDate, externalCurrencyRate);
                            invoiceTotalAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(request, invoiceOriginalAmt, currencyid, gReceipt.getCreationDate(), externalCurrencyRate);
                        }
                        double invoiceTotalAmountInBase=authHandler.round((Double) invoiceTotalAmtInBaseResult.getEntityList().get(0),companyid);
                        if (isBadDebtInvoices) {// in case of Malasian Company
                            int baddebttype = (Integer) request.get("baddebttype");
                            double totalTaxAmt = 0d;
                            double totalTaxAmtInBase = 0d;
                            String taxId = "";
                            if (isopeningBalanceInvoice) {
                                totalTaxAmt = gReceipt.getTaxamount();
                            } else {
                                double invoiceLevelTaxAmt = gReceipt.getTaxEntry() == null ? 0 : gReceipt.getTaxEntry().getAmount();
                                taxId = gReceipt.getTaxEntry() == null ? "" : gReceipt.getTax().getID();
                                double rowTaxAmt = 0d;
                                for (GoodsReceiptDetail invoiceDetail : goodsReceiptDetails) {
                                    rowTaxAmt += invoiceDetail.getRowTaxAmount();
                                    rowTaxAmt += invoiceDetail.getRowTermTaxAmount();
                                    taxId = (invoiceDetail.getTax() != null) ? invoiceDetail.getTax().getID() : taxId;
                                }
                                totalTaxAmt = invoiceLevelTaxAmt + rowTaxAmt;
                            }

                            if (totalTaxAmt == 0) {// no need to put invoice in bad debt section if it has tax 0
                                continue;
                            }

                            if (isopeningBalanceInvoice) {
                                totalTaxAmtInBase = gReceipt.getTaxamountinbase();
                            } else {
                                taxTotalAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(request, totalTaxAmt, currencyid, gReceipt.getCreationDate(), externalCurrencyRate);
                                totalTaxAmtInBase = authHandler.round((Double) taxTotalAmtInBaseResult.getEntityList().get(0), companyid);
                            }

//                            if (baddebttype == 1) {
                            // get Paid amount of invoice
                            Date badDebtCalculationToDate = null;
                            if (request.get("badDebtCalculationToDate") != null) {
                                badDebtCalculationToDate = df.parse((String) request.get("badDebtCalculationToDate"));
                            }
                            KwlReturnObject invoicePaidAmtObj = accPaymentDAOobj.getPaymentFromBadDebtClaimedInvoice(gReceipt.getID(), true, badDebtCalculationToDate);//accPaymentDAOobj.getPaymentAmountofBadDebtGoodsReceipt(gReceipt.getID(),true);
//                                double paidAmt = (Double) invoicePaidAmtObj.getEntityList().get(0);

                            double paidAmt = 0;

                            List paidList = invoicePaidAmtObj.getEntityList();
                            if (paidList != null && !paidList.isEmpty()) {
                                Iterator pmtIt = paidList.iterator();
                                while (pmtIt.hasNext()) {
                                    PaymentDetail rd = (PaymentDetail) pmtIt.next();

                                    double paidAmtInPaymentCurrency = rd.getAmount();

//                                    KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, paidAmtInPaymentCurrency, rd.getPayment().getCurrency().getCurrencyID(), rd.getPayment().getJournalEntry().getEntryDate(), rd.getPayment().getJournalEntry().getExternalCurrencyRate());
                                    KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, paidAmtInPaymentCurrency, rd.getPayment().getCurrency().getCurrencyID(), rd.getPayment().getCreationDate(), rd.getPayment().getJournalEntry().getExternalCurrencyRate());
                                    double paidAmtInBase = authHandler.round((Double) bAmt1.getEntityList().get(0), companyid);

                                    paidAmt += paidAmtInBase;

                                }
                            }

                            // paidAmt should be converted into base currency
//                            KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, isopeningBalanceInvoice ? gReceipt.getCreationDate() : gReceipt.getJournalEntry().getEntryDate(), taxId);
                            KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, isopeningBalanceInvoice ? gReceipt.getCreationDate() : gReceipt.getCreationDate(), taxId);

                            double taxPer = (Double) taxObj.getEntityList().get(0);

//                                double gstToRecover = paidAmt*taxPer/(100+taxPer);
                            // Gst claimable amount
                            double grAmountDue = isopeningBalanceInvoice ? gReceipt.getOpeningBalanceAmountDue() : gReceipt.getInvoiceamountdue();

                            // Converting grAmountDue to base currency
                            KwlReturnObject bAmt1 = null;
                            String fromcurrencyid = gReceipt.getCurrency().getCurrencyID();
                            if (isopeningBalanceInvoice) {
                                grAmountDue = gReceipt.getOpeningBalanceBaseAmountDue();
                            } else {
//                                bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, grAmountDue, fromcurrencyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                                bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, grAmountDue, fromcurrencyid, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                                grAmountDue = authHandler.round((Double) bAmt1.getEntityList().get(0), companyid);
                            }

                            double gstclaimableamount = 0.0;
//                            double gstclaimableamount = grAmountDue * taxPer / (100 + taxPer);
                            gstclaimableamount = (totalTaxAmtInBase * grAmountDue) / invoiceTotalAmountInBase;
                            gstclaimableamount = authHandler.round(gstclaimableamount, companyid);
                            //Calculate Bad Debt Recoverable Amount
                            // Get Recovered Amount of invoice

                            HashMap<String, Object> badMaps = new HashMap<String, Object>();
                            badMaps.put("companyid", companyid);
                            badMaps.put("invoiceid", invid);

                            KwlReturnObject badDebtMappingResult = accGoodsReceiptobj.getBadDebtPurchaseInvoiceMappingForGoodsReceipt(badMaps);

                            Date badDebtClaimedDate = null;
                            double totalRecoveredAmt = 0;
                            List badDebtMapping = badDebtMappingResult.getEntityList();
                            if (!badDebtMapping.isEmpty()) {
                                Iterator badIt = badDebtMapping.iterator();
                                while (badIt.hasNext()) {
                                    BadDebtPurchaseInvoiceMapping debtInvoiceMapping = (BadDebtPurchaseInvoiceMapping) badIt.next();
                                    totalRecoveredAmt += debtInvoiceMapping.getBadDebtAmtRecovered();
                                    if (debtInvoiceMapping.getBadDebtClaimedDate() != null) {
                                        badDebtClaimedDate = debtInvoiceMapping.getBadDebtClaimedDate();
                                    }
                                }
                            }

                            // Calculate Recover Amount in base
                            if (isopeningBalanceInvoice) {
                                if (gReceipt.isConversionRateFromCurrencyToBase()) {
                                    totalRecoveredAmt = totalRecoveredAmt / gReceipt.getExchangeRateForOpeningTransaction();
                                } else {
                                    totalRecoveredAmt = totalRecoveredAmt * gReceipt.getExchangeRateForOpeningTransaction();
                                }
                            } else {
//                                bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, totalRecoveredAmt, fromcurrencyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                                bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, totalRecoveredAmt, fromcurrencyid, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                                totalRecoveredAmt = authHandler.round((Double) bAmt1.getEntityList().get(0), companyid);
                            }

                            if (baddebttype == 1) {

//                                System.out.println(gReceipt.getGoodsReceiptNumber());
//                                    HashMap<String, Object> hm = new HashMap<String, Object>();
//
//                                    hm.put("companyid", companyid);
//                                    hm.put("invoiceId", gReceipt.getID());
//    //                                hm.put("invoiceId", invoice.getI);
//
//                                    double consumedAmt = accGoodsReceiptCommon.getAmountDueOfGRBeforeClaimedDate(hm);
//                                    double invoiceOrigAmt = d.getAmount();
//
//                                    double remainedAmtBeforeClaim = invoiceOrigAmt - consumedAmt;
//
//                                    double claimedGST = remainedAmtBeforeClaim * taxPer/(100+taxPer);
                                double claimedGST = 0;

                                if (!badDebtMapping.isEmpty()) {
                                    Iterator badIt = badDebtMapping.iterator();
                                    while (badIt.hasNext()) {
                                        BadDebtPurchaseInvoiceMapping debtInvoiceMapping = (BadDebtPurchaseInvoiceMapping) badIt.next();
                                        if (debtInvoiceMapping.getBadDebtType() == 0) {
                                            claimedGST += debtInvoiceMapping.getBadDebtGSTAmtClaimed();
                                        }
                                    }
                                }

                                // converting claimed GST in Base Currency
//                                bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, claimedGST, fromcurrencyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                                bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, claimedGST, fromcurrencyid, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                                claimedGST = authHandler.round((Double) bAmt1.getEntityList().get(0), companyid);

                                obj.put("gstclaimableamount", claimedGST);
                            } else {
                                obj.put("gstclaimableamount", gstclaimableamount);

                                Date selectedCriteriaDate = df.parse((String) request.get("selectedCriteriaDate"));
                                int badDebtCriteria = (Integer) request.get("badDebtCriteria");

                                long diff = 0;
                                if (badDebtCriteria == 0 && gReceipt.getDueDate() != null) {// on Invoice Due Date
                                    diff = selectedCriteriaDate.getTime() - gReceipt.getDueDate().getTime();
                                } else if (badDebtCriteria == 1) {// on Invoice Creation Date
                                    diff = selectedCriteriaDate.getTime() - creationDate.getTime();
                                }
                                long diffInDays = diff / (24 * 60 * 60 * 1000);

                                obj.put("agingDays", diffInDays);

                            }

                            double gstToRecover = (paidAmt - totalRecoveredAmt) * taxPer / (100 + taxPer);
                            double paidAfterClaimed = paidAmt - totalRecoveredAmt;

//                                obj.put("gstclaimableamount", gstclaimableamount);
                            obj.put("paidAfterClaimed", paidAfterClaimed);
                            obj.put("gstToRecover", gstToRecover);
                            obj.put("claimedPeriod", gReceipt.getClaimedPeriod());
                            obj.put("badDebtClaimedDate", (badDebtClaimedDate != null) ? df.format(badDebtClaimedDate) : null);

                            if (authHandler.round(amountdue, companyid) == 0 && authHandler.round(paidAfterClaimed, companyid) == 0) {// don't put invoices which has amount due zero and whole gst has been recovered
                                continue;
                            }
//                            }
                        }
                        obj=AccountingAddressManager.getTransactionAddressJSON(obj,gReceipt.getBillingShippingAddresses(),true);
                        obj.put("sequenceformatid", gReceipt.getSeqformat() == null ? "" : gReceipt.getSeqformat().getID());
                        obj.put("gstIncluded", gReceipt.isGstIncluded());
                        obj.put("selfBilledInvoice", gReceipt.isSelfBilledInvoice());
                        obj.put("RMCDApprovalNo", gReceipt.getRMCDApprovalNo());
                        obj.put("fixedAssetInvoice", gReceipt.isFixedAssetInvoice());
                        obj.put("isConsignment", gReceipt.isIsconsignment());
                        if (gReceipt.isCashtransaction()) {
                            obj.put(Constants.IS_PYMENT_STATUS_CLEARED, false);
                            PayDetail payDetail = gReceipt.getPayDetail();
                            if (payDetail != null) {
                                PaymentMethod paymentMethod = payDetail.getPaymentMethod();
                                obj.put("paymentname", paymentMethod.getMethodName());
                                obj.put("methodid", paymentMethod.getID());
                                obj.put("detailtype", paymentMethod.getDetailType());
                                if (paymentMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
                                    Card card = payDetail.getCard();
                                    obj.put("cardno", card != null ? card.getCardNo() : "");
                                    obj.put("nameoncard", card != null ? card.getCardHolder() : "");
                                    obj.put("cardexpirydate", card != null ? df.format(card.getExpiryDate()) : "");
                                    obj.put("cardtype", card != null ? card.getCardType() : "");
                                    obj.put("cardrefno", card != null ? card.getRefNo() : "");

                                } else if (paymentMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                                    Cheque cheque = payDetail.getCheque();
                                    obj.put("chequeno", cheque != null ? cheque.getChequeNo() : "");
                                    obj.put("chequedescription", cheque != null ? cheque.getDescription() : "");
                                    obj.put("bankname", cheque != null ? cheque.getBankName() : "");
                                    obj.put("chequedate", cheque != null ? df.format(cheque.getDueDate()) : "");
                                    obj.put("clearanceDate", "");
                                    obj.put("paymentStatus", "Uncleared");
                                    if (gReceipt.getPayDetail() != null) {
                                        KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(gReceipt.getJournalEntry().getID(), gReceipt.getCompany().getCompanyID(), false);
                                        if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                                            BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                                            if (brd.getBankReconciliation().getClearanceDate() != null) {
                                                obj.put("clearanceDate", df.format(brd.getBankReconciliation().getClearanceDate()));
                                                obj.put("paymentStatus", "Cleared");
                                                obj.put(Constants.IS_PYMENT_STATUS_CLEARED, true);// To Disable Feilds in Edit Case for Cleard Cash Payment
                                            }
                                        }
                                    }
                                }

                            } else {
                                obj.put("paymentname", "");
                                obj.put("methodid", "");
                            }
                        } else {
                            obj.put("paymentname", "NA");
                        }
                        if (gReceipt.getModifiedby() != null) {
                            obj.put("lasteditedby", StringUtil.getFullName(gReceipt.getModifiedby()));
                        }
                        obj.put("createdby", gReceipt.getCreatedby() == null ? "" : StringUtil.getFullName(gReceipt.getCreatedby()));
                        if (report) {
                            obj.put("isreval", isReval);
                        }
//                        int pendingApprovalInt = gReceipt.getPendingapproval();
//                        obj.put("approvalstatusint", pendingApprovalInt);
//                        if (pendingApprovalInt == Constants.LEVEL_ONE) {
//                            obj.put("approvalstatus", "Pending level 1 approval");
//                        } else if (pendingApprovalInt == Constants.LEVEL_TWO) {
//                            obj.put("approvalstatus", "Pending level 2 approval");
//                        } else {
//                            obj.put("approvalstatus", "");
//                        }
                        obj.put("approvalstatus", gReceipt.getApprovestatuslevel());
                        obj.put("isjobworkoutrec", gReceipt.isIsJobWorkOutInv());
                    String approvalStatus="";
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    ScriptEngine engine = mgr.getEngineByName("JavaScript");
                    String multipleRuleids="";
                    if(gReceipt.getApprovestatuslevel() < 0){
                        approvalStatus="Rejected";
                    }else if(gReceipt.getApprovestatuslevel() < 11){
                        String ruleid = "",userRoleName="";
                            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                            qdDataMap.put("companyid", companyid);
                        qdDataMap.put("level",gReceipt.getApprovestatuslevel());
                            qdDataMap.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                            Iterator ruleitr = flowresult.getEntityList().iterator();
                            while (ruleitr.hasNext()) {
                            Object[] rulerow = (Object[]) ruleitr.next();
                            ruleid = rulerow[0].toString();
                            boolean sendForApproval = false;
                            int appliedUpon = Integer.parseInt(rulerow[5].toString());
                            String discountRule = "";
                            String rule = "";
                            if (rulerow[2] != null) {
                                rule = rulerow[2].toString();
                            }
                            if (rulerow[7] != null) {
                                discountRule = rulerow[7].toString();
                            }
                            if (appliedUpon == Constants.Total_Amount) {
                                /*
                                 Added to get condition of approval rule i.e set when creating approval rule 
                                 */
                                rule = rule.replaceAll("[$$]+", String.valueOf(gReceipt.getInvoiceAmountInBase()));
                            } else if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount || appliedUpon == Constants.Specific_Products_Category) {
                                /*
                                 Handled for Product,product discount And product category
                                 */
                                HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                                JSONArray productDiscountJArr = new JSONArray();
                                Set<GoodsReceiptDetail> grDetails = gReceipt.getRows();
                                for (GoodsReceiptDetail grDetail : grDetails) {
                                    if (grDetail.getInventory() != null) {
                                        String productId = grDetail.getInventory().getProduct().getID();
                                        Discount invDiscount = grDetail.getDiscount();
                                        double discAmountinBase = 0;
                                        if (invDiscount != null) {
                                            double discountVal = invDiscount.getDiscountValue();
                                            KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(request, discountVal, currencyid, gReceipt.getCreationDate(), gReceipt.getExternalCurrencyRate());
                                            discAmountinBase = (Double) dAmount.getEntityList().get(0);
                                        }
                                        discAmountinBase = authHandler.round(discAmountinBase, companyid);
                                        JSONObject productDiscountObj = new JSONObject();
                                        productDiscountObj.put("productId", productId);
                                        productDiscountObj.put("discountAmount", discAmountinBase);
                                        productDiscountJArr.put(productDiscountObj);
                                    }
                                }
                                if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                                    /*
                                     * Check If Rule is apply on specefic product
                                     *  and Specific product discount from multiapproverule window
                                     */
                                    if (productDiscountJArr != null) {
                                        sendForApproval = AccountingManager.checkForProductAndProductDiscountRule(productDiscountJArr, appliedUpon, rule, discountRule);
                                    }
                                } else if (appliedUpon == Constants.Specific_Products_Category) {
                                    /*
                                     * Check If Rule is apply on product
                                     * category from multiapproverule window
                                     */
                                    sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productDiscountJArr, appliedUpon, rule);
                                }
                            }
                            /*
                             Added to check if record falls in total amount approval rule 
                             */
                            if (StringUtil.isNullOrEmpty(rule) || sendForApproval || (!StringUtil.isNullOrEmpty(rule) && appliedUpon == Constants.Total_Amount && Boolean.parseBoolean(engine.eval(rule).toString()))) {
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
                                    String userId = userrow[0].toString();

                                    String fname = userrow[1].toString();
                                    String lname = userrow[2].toString();
                                    String userName = fname+" "+lname;
                                    /*
                                     Addded so duplicate approve's can be eleminated 
                                     */
                                    if(userRoleName.contains(userName)){
                                        continue;
                                    }
                                    KwlReturnObject kmsg = null;
                                String roleName="Company User";
                                    kmsg = permissionHandlerDAOObj.getRoleofUser(userId);
                                    Iterator ite2 = kmsg.getEntityList().iterator();
                                    while (ite2.hasNext()) {
                                        Object[] row = (Object[]) ite2.next();
                                        roleName = row[1].toString();
                                    }
                                userRoleName += roleName+" "+userName + ",";
                                }
                            }
                        }
                            if (!StringUtil.isNullOrEmpty(userRoleName)) {
                                userRoleName = userRoleName.substring(0, userRoleName.length() - 1);
                            }
                        approvalStatus="Pending Approval" + ( StringUtil.isNullOrEmpty(userRoleName) ? "" : " by "+userRoleName )+" at Level - "+gReceipt.getApprovestatuslevel();
                        } else {
                        approvalStatus="Approved";
                        }
                    obj.put("approvalstatusinfo",approvalStatus);

                        if (request.containsKey("pendingapproval") && request.get("pendingapproval") != null && (Boolean) request.containsKey("pendingapproval")) {
                            int nextApprovalLevel = 11;
                            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                            qdDataMap.put("companyid", companyid);
                            qdDataMap.put("level", gReceipt.getApprovestatuslevel() + 1);
                            qdDataMap.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                            List<Object[]> approvalRuleItr = flowresult.getEntityList();
                            if (approvalRuleItr != null && approvalRuleItr.size() > 0) {
                                for (Object[] rowObj : approvalRuleItr) {
                                    String rule = "";
                                    if (rowObj[2] != null) {
                                        rule = rowObj[2].toString();
                                    }
                                    int appliedUpon = Integer.parseInt(rowObj[5].toString());
                                    rule = rule.replaceAll("[$$]+", String.valueOf(gReceipt.getInvoiceAmountInBase()));
                                    if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && ( appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon != Constants.Specific_Products_Category) && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                                        nextApprovalLevel = gReceipt.getApprovestatuslevel() + 1;
                                    }
                                }
                            }
                            obj.put("isFinalLevelApproval", nextApprovalLevel == Constants.APPROVED_STATUS_LEVEL ? true : false);
                        }
                        /*
                         * For Product search, add Products details from Invoice
                         * details
                         */

                        if (isProduct && gReceipt.isNormalInvoice()) {
                            String idvString = isProduct ? oj[4].toString() : ""; //as in list invoiedetail id comes 4th
                            KwlReturnObject objItrGRD = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), idvString);
                            GoodsReceiptDetail idvObj = (GoodsReceiptDetail) objItrGRD.getEntityList().get(0);
                            if (idvObj != null) {
                                obj.put("rowproductname", idvObj.getInventory().getProduct().getName());
                                obj.put("rowquantity", idvObj.getInventory().isInvrecord() ? idvObj.getInventory().getQuantity() : idvObj.getInventory().getActquantity());
                                obj.put("rowrate", idvObj.getRate());

                                Discount disc = idvObj.getDiscount();
                                if (disc != null && disc.isInPercent()) {
                                    obj.put("rowprdiscount", disc.getDiscount()); //product discount in percent
                                } else {
                                    obj.put("rowprdiscount", 0);
                                }

                                double rowTaxPercent = 0;
                                if (idvObj.getTax() != null) {
//                                    KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), idvObj.getTax().getID());
                                    KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getCreationDate(), idvObj.getTax().getID());
                                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                                }
                                obj.put("rowprtaxpercent", rowTaxPercent);
                            }
                        }

                        //For getting tax in percent applyied on invoice [PS]
                        if (gReceipt.getTax() != null) {
//                            KwlReturnObject taxresult = accTaxObj.getTaxPercent(companyid, je.getEntryDate(), gReceipt.getTax().getID());
                            KwlReturnObject taxresult = accTaxObj.getTaxPercent(companyid, gReceipt.getCreationDate(), gReceipt.getTax().getID());
                            taxPercent = (Double) taxresult.getEntityList().get(0);
                        }
                        obj.put(GoodsReceiptCMNConstants.TAXPERCENT, taxPercent);

                        //For getting amountdue [PS]
                        if (gReceipt.isCashtransaction()) {
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, 0);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, 0);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUENONNEGATIVE, 0);
                            obj.put(GoodsReceiptCMNConstants.INCASH, true);
                        } else {
                            if ((Constants.InvoiceAmountDueFlag && !isAged) || isopeningBalanceInvoice) {
                                if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
//                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, amountdue, currencyid, creationDate, externalCurrencyRate);
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, amountdue, currencyid, gReceipt.getCreationDate(), externalCurrencyRate);
                                } else {
//                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(request, amountdue, currencyid, creationDate, externalCurrencyRate);
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(request, amountdue, currencyid, gReceipt.getCreationDate(), externalCurrencyRate);
                                }
                                amountInBase = (Double) bAmt.getEntityList().get(0);
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                            } else { // For aged we are fetching amount in base as well so no need for calculation
                                obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, amountdueinbase);
                            }
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, authHandler.round(amountdue, companyid));
                            /*
                             * To calulate exchange rate
                             */
                            obj.put("exchangeratefortransaction", (amountInBase <= 0 && amountdue <= 0) ? 0 : (amountInBase / amountdue));

                            if (booleanAged) {//Added for aged payable/receivable
                                int datefilter = (request.containsKey("datefilter") && request.get("datefilter") != null) ? Integer.parseInt(request.get("datefilter").toString()) : 0;// 0 = Invoice Due date OR 1 = Invoice date
                                Date dueDate = null;
                                if (!StringUtil.isNullOrEmpty(df.format(gReceipt.getDueDate()))) {
                                    dueDate = df.parse(df.format(gReceipt.getDueDate()));
                                }
                                if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                    dueDate = df.parse(df.format(gReceipt.getDueDate()));
                                } else {
                                    dueDate = df.parse(df.format(creationDate));
                                }
//                                if(startDate!=null && dueDate.before(startDate)){//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                    accruedbalance = authHandler.round(amountdue, companyid);
//                                } else
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
                            }
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUENONNEGATIVE, (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                        }
                        boolean includeprotax = false;
                        String taxname="";
                        double rowTaxAmt = 0d, rowOtherTermNonTaxableAmount = 0;
                        boolean isLineLevelTermFlag = false;//To Check Whether LinelevelTerms are applicable or not.
                        if(extraCompanyPreferences != null && extraCompanyPreferences.getLineLevelTermFlag()==1){
                            isLineLevelTermFlag = true;//If LineLevelTerms are applicable, then update the flag.
                        }
                       double subtotal=0.0;
                       double productTotalAmount=0.0;
                        double discountAmount = 0.0;
//                       double taxAmountOfTerms=0d;
                        if (!gReceipt.isIsExpenseType() && gReceipt.isNormalInvoice()) {
//                            Set<GoodsReceiptDetail> goodsReceiptDetails = gReceipt.getRows();
                            for (GoodsReceiptDetail goodsReceiptDetail : goodsReceiptDetails) {
                                double rowsubtotal = 0d;
                                double invquantity = goodsReceiptDetail.getInventory().getQuantity();
                                if (goodsReceiptDetail.getGoodsReceipt().isGstIncluded()) {
                                    rowsubtotal = goodsReceiptDetail.getRateincludegst() * invquantity;
                                } else {
                                    rowsubtotal = goodsReceiptDetail.getRate() * invquantity;
                                }
                                /**
                                 * Getting the Discount Value(Amount) and
                                 * subtracting from subtotal.
                                 */
                                Discount disc = goodsReceiptDetail.getDiscount();
                                if (disc != null) {
                                    discountAmount += disc.getDiscountValue();
                                }
                                productTotalAmount += authHandler.round(rowsubtotal, companyid);
                                if(isLineLevelTermFlag){
                                    // Append OtherTermNonTaxableAmount for rach row.
                                    rowOtherTermNonTaxableAmount += goodsReceiptDetail.getOtherTermNonTaxableAmount();
                                    rowTaxAmt += goodsReceiptDetail.getRowTermAmount();
                                    rowTaxAmt += goodsReceiptDetail.getRowTermTaxAmount();
//                                    taxAmountOfTerms += goodsReceiptDetail.getRowTermTaxAmount();
                                }else if (goodsReceiptDetail.getTax() != null) {
                                    includeprotax = true;
                                    taxname += goodsReceiptDetail.getTax().getName() + ", ";
                                    rowTaxAmt += goodsReceiptDetail.getRowTaxAmount();
//                                    rowTaxAmt += goodsReceiptDetail.getRowTermTaxAmount();
//                                    taxAmountOfTerms += goodsReceiptDetail.getRowTermTaxAmount();
                                }
                            }
                        } else if (gReceipt.isIsExpenseType()) {
                            Set<ExpenseGRDetail> expenseGRDetails = gReceipt.getExpenserows();
                            for (ExpenseGRDetail expGReceiptDetail : expenseGRDetails) {
                                if (!expGReceiptDetail.getGoodsReceipt().isGstIncluded()) {
                                    productTotalAmount += expGReceiptDetail.isIsdebit() ? expGReceiptDetail.getRate() : -(expGReceiptDetail.getRate());
                                } else {
                                    productTotalAmount += expGReceiptDetail.isIsdebit() ? expGReceiptDetail.getRate() : -(expGReceiptDetail.getRate());
                                    // productTotalAmount +=expGReceiptDetail.getRate();
                                }
                                /**
                                 * Getting the Discount Value(Amount) and
                                 * subtracting from subtotal.(ERP-38123)
                                 */
                                Discount disc = expGReceiptDetail.getDiscount();
                                if (disc != null) {
                                    if (expGReceiptDetail.isIsdebit()) {
                                        discountAmount += disc.getDiscountValue();
                                    } else {
                                        discountAmount -= disc.getDiscountValue();
                                    }
                                }
//                                System.out.println(expGReceiptDetail.getGoodsReceipt().getGoodsReceiptNumber());
                                if (expGReceiptDetail.getTax() != null) {
                                    includeprotax = true;
                                    taxname += expGReceiptDetail.getTax().getName() + ", ";
                                    rowTaxAmt += expGReceiptDetail.isIsdebit()?expGReceiptDetail.getRowTaxAmount():-(expGReceiptDetail.getRowTaxAmount());// SDP- 4676 PO/PI Expense type records to show  tax amount in report
                                }
                            }
                        }
                        obj.put("productTotalAmount", productTotalAmount);                        
                        double termTaxAmount = 0d;
                        double termAmountInBase = 0d;
                        double termAmount = 0d;
                        List receiptTermMapList = kwlCommonTablesDAOObj.getSummationOfTermAmtAndTermTaxAmt(Constants.receipttermsmap, invid);
                        if(receiptTermMapList != null && !receiptTermMapList.isEmpty()){
                            Iterator termItr = receiptTermMapList.iterator();
                            while (termItr.hasNext()) {
                                Object[] termObj = (Object[]) termItr.next();
                                /* 
                                * [0] : Sum of termamount  
                                * [1] : Sum of termamountinbase 
                                * [2] : Sum of termTaxamount 
                                * [3] : Sum of termTaxamountinbase 
                                * [4] : Sum of termamountexcludingtax 
                                * [5] : Sum of termamountexcludingtaxinbase
                                */ 
                                if (gReceipt.isGstIncluded()) {
                                    if(termObj[4] != null && termObj[5] != null){
                                        termAmount += authHandler.round((Double) termObj[4],companyid);
                                        termAmountInBase += authHandler.round((Double) termObj[5],companyid);
                                    }
                                } else {
                                    if(termObj[0] != null && termObj[1] != null){
                                        termAmount += authHandler.round((Double) termObj[0],companyid);
                                        termAmountInBase += authHandler.round((Double) termObj[1],companyid);
                                    }
                                }
                                if(termObj[2] != null){
                                    termTaxAmount += authHandler.round((Double) termObj[2],companyid);
                                }
                            }
                        }
                        
                        taxAmt += rowTaxAmt + termTaxAmount;
                        if (gReceipt.isGstIncluded()) {
                            subtotal = productTotalAmount - discountAmount - rowTaxAmt;
                        } else {
                            subtotal = productTotalAmount - discountAmount;
                        }
                        obj.put("subtotal", subtotal);
                        obj.put("termamount", termAmount);
                        obj.put("termamountinBase", termAmountInBase);
                        obj.put("amountBeforeTax", authHandler.formattingDecimalForAmount((subtotal+termAmount),companyid));
                        double tdsAmountandOtherCharges = 0.0;
                        if (Constants.indian_country_id == countryid) { // For india Company
                            tdsAmountandOtherCharges = gReceipt.getTdsAmount() - rowOtherTermNonTaxableAmount;
                            obj.put("totalAmountWithTDS", authHandler.round(invoiceOriginalAmt + gReceipt.getTdsAmount(), companyid));   // Amount with TDS
                        }
//                        obj.put("amountbeforegst", authHandler.round(invoiceOriginalAmt-taxAmt-termAmount+tdsAmountandOtherCharges, 2));   // Amount before both kind of tax row level or transaction level
                        obj.put("amountbeforegst", gReceipt.getExcludingGstAmount());
                        obj.put(GoodsReceiptCMNConstants.TAXAMOUNT, taxAmt);

                        //*** For GTA - Start***//
                        if (Constants.indian_country_id == countryid && gReceipt.isGtaapplicable() && !gReceipt.isIsExciseInvoice() && taxAmt > 0) { // exclude service tax from totaltax on grid
                            obj.put("amountbeforegst", authHandler.round(invoiceOriginalAmt - (taxAmt) - termAmount + tdsAmountandOtherCharges, companyid));   // Amount before both kind of tax row level or transaction level
                        }
                        //*** For GTA - END***//

                        if (isLineLevelTermFlag) {
                            // If LineLevelTerm is applicable then add the value in JSON Object.
                            obj.put(Constants.OtherTermNonTaxableAmount, rowOtherTermNonTaxableAmount);
                        }
//                        obj.put("taxamountinbase", accCurrencyDAOobj.getCurrencyToBaseAmount(request, taxAmt, currencyid, creationDate, externalCurrencyRate).getEntityList().get(0));
                        obj.put("taxamountinbase", gReceipt.getTaxamountinbase());
//                        obj.put("taxamountsaved", gReceipt.getTaxamount());
//                        obj.put("taxamountinbasesaved", gReceipt.getTaxamountinbase());
//                        obj.put("excludinggstamountsaved", gReceipt.getExcludingGstAmount());
//                        obj.put("excludinggstamountinbasesaved", gReceipt.getExcludingGstAmountInBase());
                        if (includeprotax) {
                            obj.put(GoodsReceiptCMNConstants.TAXNAME, taxname.substring(0, taxname.length() > 1 ? taxname.length() - 2 : taxname.length()));
                        } else {
                            obj.put(GoodsReceiptCMNConstants.TAXNAME, gReceipt.getTax() == null ? "" : gReceipt.getTax().getName());
                        }

                        obj.put("includeprotax", includeprotax);
                        obj.put(GoodsReceiptCMNConstants.AMOUNT, authHandler.round((Double)invoiceOriginalAmt,companyid)); //actual invoice amount
                        obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, invoiceTotalAmountInBase);
                        obj.put(GoodsReceiptCMNConstants.ACCOUNTNAMES, (String) ll.get(2));
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE1, amountdue1);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE2, amountdue2);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE3, amountdue3);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE4, amountdue4);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE5, amountdue5);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE6, amountdue6);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE7, amountdue7);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE8, amountdue8);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE9, amountdue9);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE10, amountdue10);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE11, amountdue11);
//                        obj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
                        obj.put(GoodsReceiptCMNConstants.TYPE, "Purchase Invoice");
                        obj.put(GoodsReceiptCMNConstants.DEDUCTDISCOUNT, deductDiscount);

                        KwlReturnObject custumObjresult = null;
                        if (gReceipt.isNormalInvoice()) {
//                            boolean isExport = (request.get("isExport") == null) ? false : (Boolean) request.get("isExport");
                            Map<String, Object> variableMap = new HashMap<String, Object>();
                            custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), je.getID());
                            replaceFieldMap = new HashMap<String, String>();
                            if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                                AccJECustomData jeDetailCustom = (AccJECustomData) custumObjresult.getEntityList().get(0);
                                if (jeDetailCustom != null) {
                                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                                    JSONObject params = new JSONObject();
                                    params.put("companyid", companyid);
                                    params.put(Constants.userdf,userDateFormat);
                                    if (!isExport) {
                                        isExport = (request.get("isAgedPayables") == null) ? false : (Boolean) request.get("isAgedPayables");
                                    }
                                    params.put("isExport", isExport);
//                                    if (request.containsKey("browsertz") && request.get("browsertz") != null) {
//                                        params.put("browsertz", request.get("browsertz").toString());
//                                    }
                                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                                }
                            }
                            
                            
                            if (booleanAged ) 
                            {
                                if (!request.containsKey("isAgedPayables") || !(Boolean) request.get("isAgedPayables")) {
                                   accGoodsReceiptServiceDAO.getCustmDataForPurchaseInvoice(request, jArr, companyid, replaceFieldMap, customFieldMap, customDateFieldMap, FieldMap, replaceFieldMapRows, customFieldMapRows, customDateFieldMapRows, fieldMapRows);
                                   //getPurchaseInvoiceCustomField(gReceipt,goodsReceiptDetails,fieldMapRows,replaceFieldMapRows,customFieldMapRows,customDateFieldMapRows,obj,userDateFormat);
                                }
                            }
                        }
                        RepeatedInvoices repeatedInvoice = gReceipt.getRepeateInvoice();
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
                            obj.put("isactivate", repeatedInvoice.isIsActivate());
                            obj.put("ispendingapproval", repeatedInvoice.isIspendingapproval());
                            obj.put("approver", repeatedInvoice.getApprover());
                            obj.put("expireDate", repeatedInvoice.getExpireDate() == null ? "" : sdf.format(repeatedInvoice.getExpireDate()));
                        obj.put("advancedays", repeatedInvoice.getAdvanceNoofdays()== 0 ? 0 : repeatedInvoice.getAdvanceNoofdays());
                        obj.put("advanceDate", repeatedInvoice.getInvoiceAdvanceCreationDate()== null ? "" : sdf.format(repeatedInvoice.getInvoiceAdvanceCreationDate()));
                            request.put("parentInvoiceId", gReceipt.getID());
                            KwlReturnObject details = accGoodsReceiptobj.getRepeateVendorInvoicesDetails(request);
                            List detailsList = details.getEntityList();
                            obj.put("childCount", detailsList.size());
                        }
                           

                        if (gReceipt.isIsOpeningBalenceInvoice()) {
                            Map<String, Object> variableMap = new HashMap<String, Object>();
                            custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceVendorInvoiceCustomData.class.getName(), gReceipt.getID());
                            replaceFieldMap = new HashMap<String, String>();
                            if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                                OpeningBalanceVendorInvoiceCustomData openingBalanceVendorInvoiceCustomData = (OpeningBalanceVendorInvoiceCustomData) custumObjresult.getEntityList().get(0);
                                if (openingBalanceVendorInvoiceCustomData != null) {
                                    AccountingManager.setCustomColumnValues(openingBalanceVendorInvoiceCustomData, FieldMap, replaceFieldMap, variableMap);
                                    DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                                    Date dateFromDB=null;
                                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                        String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                                        if (customFieldMap.containsKey(varEntry.getKey())) {
                                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                            if (fieldComboData != null) {
                                                obj.put(varEntry.getKey(), fieldComboData.getValue() != null ? fieldComboData.getValue() : "");
                                            }
                                        } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                            DateFormat sdf = userDateFormat != null?userDateFormat:new SimpleDateFormat("yyyy-MM-dd");
                                            dateFromDB=defaultDateFormat.parse(coldata);
                                            coldata=sdf.format(dateFromDB);
                                            obj.put(varEntry.getKey(), coldata);
                                        } else {
                                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                                obj.put(varEntry.getKey(), coldata);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        try { // check if credit/cash purchase is allowed to edit
                            // isAllowToEdit= if credit/cash purchase created using auto generate GR option and credit/cash purchase/GR hasn't been forward linked in any document
                            result = accGoodsReceiptobj.getAutoGRFromInvoice(gReceipt.getID(), companyid);
                            list = result.getEntityList();
                            if (list != null && !list.isEmpty()) { // SI/CS created with auto generate DO option
                                boolean isDOLinkedInPR = false;
                                String groID = "";
                                Object groid = list.get(0);
                                groID = (String) groid;
                                KwlReturnObject resultPR = accGoodsReceiptobj.getPurchaseReturnLinkedWithGR(groID, companyid);
                                List listPR = resultPR.getEntityList();
                                if (!listPR.isEmpty()) { // is DO forward linked in any SR
                                    isDOLinkedInPR = true;
                                }
                                if (!isDOLinkedInPR && obj.optDouble(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, 0.0) == obj.optDouble(GoodsReceiptCMNConstants.AMOUNTINBASE, 0.0) && !gReceipt.isCashtransaction()) {
                                    obj.put(Constants.IS_INVOICE_ALLOW_TO_EDIT, true);
                                } else if (gReceipt.isCashtransaction() && !isDOLinkedInPR) {
                                    obj.put(Constants.IS_INVOICE_ALLOW_TO_EDIT, true);
                                } else {
                                    obj.put(Constants.IS_INVOICE_ALLOW_TO_EDIT, false);
                                }
                            } else {
                                obj.put(Constants.IS_INVOICE_ALLOW_TO_EDIT, false);
                            }
                            if (!StringUtil.isNullOrEmpty(landedInvoice)) {
                                obj.put(Constants.IS_INVOICE_ALLOW_TO_EDIT, false);
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
                            obj.put(Constants.IS_INVOICE_ALLOW_TO_EDIT, false);
                        }
                        if (!(ignoreZero && authHandler.round(amountdue, companyid) <= 0)) {
                            jArr.put(obj);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGoodsReceiptServiceHandler.getGoodsReceiptsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
//    public void getPurchaseInvoiceCustomField(GoodsReceipt gReceipt,Set<GoodsReceiptDetail> goodsReceiptDetails,HashMap<String, Integer> fieldMapRows,HashMap<String, String> replaceFieldMapRows,HashMap<String, String> customFieldMapRows, HashMap<String, String> customDateFieldMapRows,com.krawler.utils.json.base.JSONObject obj,DateFormat userDateFormat) throws ServiceException, JSONException
//    {
//        {// In aged Report we are showing line level item dimension at global level by comma seperated. below code is for that
//                                ArrayList<String> rowidList = new ArrayList();
//                                if (gReceipt.isIsExpenseType()) {
//                                    Set<ExpenseGRDetail> expenseGRDetails = gReceipt.getExpenserows();
//                                    for (ExpenseGRDetail expGReceiptDetail : expenseGRDetails) {
//                                       rowidList.add(expGReceiptDetail.getID());
//                                    }
//                                } else if (gReceipt.isNormalInvoice() && goodsReceiptDetails != null && !goodsReceiptDetails.isEmpty()) {
//                                    for (GoodsReceiptDetail row : goodsReceiptDetails) {
//                                        rowidList.add(row.getID());
//                                    }
//                                }
//                                
//                                Map<String, List<Object>> linelabelDataMap = new LinkedHashMap();
//                                for (String rowID : rowidList) {
//                                    JSONObject customObject = new JSONObject();
//                                    Map<String, Object> variableMapRows = new HashMap<String, Object>();
//                                    HashMap<String, Object> invDetailsRequestParams = new HashMap<String, Object>();
//                                    ArrayList Detailfilter_names = new ArrayList();
//                                    ArrayList Detailfilter_params = new ArrayList();
//                                    Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
//                                    Detailfilter_params.add(rowID);
//                                    invDetailsRequestParams.put(Constants.filterNamesKey, Detailfilter_names);
//                                    invDetailsRequestParams.put(Constants.filterParamsKey, Detailfilter_params);
//                                    KwlReturnObject idcustdetailresult = accInvoiceDAOobj.getInvoiceDetailsCustomData(invDetailsRequestParams);
//                                    if (idcustdetailresult.getEntityList().size() > 0) {
//                                        AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustdetailresult.getEntityList().get(0);
//                                        AccountingManager.setCustomColumnValues(jeDetailCustom, fieldMapRows, replaceFieldMapRows, variableMapRows);
//                                        if (jeDetailCustom != null) {
//                                            JSONObject params = new JSONObject();
//                                            params.put(Constants.userdf,userDateFormat);
//                                            params.put(Constants.isExport, false);
//                                            params.put(Constants.isForReport, true);
//                                            fieldDataManagercntrl.getLineLevelCustomData(variableMapRows, customFieldMapRows, customDateFieldMapRows, customObject, params);
//                                            
//                                            if (customObject.length() > 0) {
//                                                Iterator<String> keys = customObject.keys();
//                                                while (keys.hasNext()) {
//                                                    String key = (String) keys.next();
//                                                    if (!key.equals("allCustomFieldKeyValuePairString")) {// no need to go for key allCustomFieldKeyValuePairString
//                                                        if (linelabelDataMap.containsKey(key)) {
//                                                            if (!linelabelDataMap.get(key).contains(customObject.get(key))) {
//                                                                linelabelDataMap.get(key).add(customObject.get(key));
//                            }
//                                                        } else {
//                                                            List<Object> dataList = new ArrayList<>();
//                                                            dataList.add(customObject.get(key));
//                                                            linelabelDataMap.put(key, dataList);
//                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                                for (Map.Entry<String, List<Object>> entry : linelabelDataMap.entrySet()) {
//                                    String commaSeperatedValue = "";
//                                    String key = entry.getKey();
//                                    List<Object> data = entry.getValue();
//                                    for (Object dataObj : data) {
//                                        if (dataObj != null) {
//                                            if (StringUtil.isNullOrEmpty(commaSeperatedValue)) {
//                                                commaSeperatedValue = dataObj.toString();
//                                            } else {
//                                                commaSeperatedValue += "," + dataObj.toString();
//                                            }
//                                        }
//                                    }
//                                    obj.put(key, commaSeperatedValue);
//                                }
//                            }
//    }
            
    
    public JSONArray getGoodsReceiptsJsonMergedForConsignmentNo(HashMap<String, Object> request, List<GoodsReceipt> list, JSONArray jArr, AccountingHandlerDAO accountingHandlerDAOobj, accCurrencyDAO accCurrencyDAOobj, accGoodsReceiptDAO accGoodsReceiptobj, accAccountDAO accAccountDAOobj, accGoodsReceiptCMN accGoodsReceiptCommon, accTaxDAO accTaxObj) throws ServiceException {
        try {
            if (list != null && !list.isEmpty()) {
                //for(GoodsReceipt gReceipt:list){
                Iterator itr = list.iterator();
                while (itr.hasNext()) {

                    Object[] oj = (Object[]) itr.next();
                    String invid = oj[0].toString();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
                    GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);


                    if (request.containsKey("excludeInvoiceId") && request.get("excludeInvoiceId") != null) {
                        String excludeInvoiceId = (String) request.get("excludeInvoiceId");
                        if (gReceipt.getGoodsReceiptNumber().equals(excludeInvoiceId)) {
                            continue;
                        }
                    }
                    com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                    obj.put(GoodsReceiptCMNConstants.BILLID, gReceipt.getID());
                    obj.put(GoodsReceiptCMNConstants.BILLNO, gReceipt.getGoodsReceiptNumber());
                    obj.put(GoodsReceiptCMNConstants.BILLTO, gReceipt.getBillFrom());
                    obj.put("billto", gReceipt.getBillTo() == null ? "" : gReceipt.getBillTo());
                    jArr.put(obj);

                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getGoodsReceiptsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public static JSONArray getTermDetails(String invoiceid, accGoodsReceiptDAO accGoodsReceiptobj) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("invoiceid", invoiceid);
            KwlReturnObject curresult = accGoodsReceiptobj.getInvoiceTermMap(requestParam);
            List<ReceiptTermsMap> termMap = curresult.getEntityList();
            for (ReceiptTermsMap invoiceTerMap : termMap) {
                InvoiceTermsSales mt = invoiceTerMap.getTerm();
                com.krawler.utils.json.base.JSONObject jsonobj = new com.krawler.utils.json.base.JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("glaccountname", mt.getAccount().getAccountName());
                jsonobj.put("accountid", mt.getAccount().getID());
                jsonobj.put("accode", mt.getAccount().getAcccode());
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("termpercentage", invoiceTerMap.getPercentage());
                jsonobj.put("termamount", invoiceTerMap.getTermamount());
                jArr.put(jsonobj);
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    public static JSONArray getGRTermDetails(String id, accGoodsReceiptDAO accGoodsReceiptobj) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("goodsReceiptID", id);
            KwlReturnObject curresult = accGoodsReceiptobj.getGRTermMap(requestParam);
            List<GoodsReceiptOrderTermMap> termMap = curresult.getEntityList();
            for (GoodsReceiptOrderTermMap goodsReceiptOrderTermMap : termMap) {
                InvoiceTermsSales mt = goodsReceiptOrderTermMap.getTerm();
                com.krawler.utils.json.base.JSONObject jsonobj = new com.krawler.utils.json.base.JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("accountid", mt.getAccount().getID());
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("termpercentage", goodsReceiptOrderTermMap.getPercentage());
                jsonobj.put("termamount", goodsReceiptOrderTermMap.getTermamount());
                jArr.put(jsonobj);
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    public JSONArray getGoodsReceiptOrderTermDetails(String id, accGoodsReceiptDAO accGoodsReceiptobj) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("goodsReceiptID", id);
            KwlReturnObject curresult = accGoodsReceiptobj.getGRTermMap(requestParam);
            List<GoodsReceiptOrderTermMap> termMap = curresult.getEntityList();
            for (GoodsReceiptOrderTermMap goodsReceiptOrderTermMap : termMap) {
                InvoiceTermsSales mt = goodsReceiptOrderTermMap.getTerm();
                com.krawler.utils.json.base.JSONObject jsonobj = new com.krawler.utils.json.base.JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("accountid", mt.getAccount().getID());
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("termpercentage", goodsReceiptOrderTermMap.getPercentage());
                jsonobj.put("termamount", goodsReceiptOrderTermMap.getTermamount());
                jsonobj.put("termamountinbase", goodsReceiptOrderTermMap.getTermamountinbase());
                jsonobj.put("termtaxamount", goodsReceiptOrderTermMap.getTermtaxamount());
                jsonobj.put("termtaxamountinbase", goodsReceiptOrderTermMap.getTermtaxamountinbase());
                jsonobj.put("termAmountExcludingTax", goodsReceiptOrderTermMap.getTermAmountExcludingTax());
                jsonobj.put("termAmountExcludingTaxInBase", goodsReceiptOrderTermMap.getTermAmountExcludingTaxInBase());
                jsonobj.put("termtax", goodsReceiptOrderTermMap.getTermtax()!=null ? goodsReceiptOrderTermMap.getTermtax().getID():"");
                jsonobj.put("linkedtaxname", goodsReceiptOrderTermMap.getTermtax()!=null ? goodsReceiptOrderTermMap.getTermtax().getName():"");
                jsonobj.put("isActivated", goodsReceiptOrderTermMap.getTermtax()!=null ? goodsReceiptOrderTermMap.getTermtax().isActivated():false);
                if(goodsReceiptOrderTermMap.getTermtax()!=null){
                    jsonobj.put("linkedtaxpercentage", accInvoiceDAOobj.getPercentageFromTaxid(goodsReceiptOrderTermMap.getTermtax().getID(), mt.getCompany().getCompanyID()));
                }else {
                    jsonobj.put("linkedtaxpercentage", 0);
                }
                jArr.put(jsonobj);
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    public JSONArray getPurchaseInvoiceTermDetails(String invoiceid, accGoodsReceiptDAO accGoodsReceiptobj) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("invoiceid", invoiceid);
            KwlReturnObject curresult = accGoodsReceiptobj.getInvoiceTermMap(requestParam);
            List<ReceiptTermsMap> termMap = curresult.getEntityList();
            for (ReceiptTermsMap invoiceTerMap : termMap) {
                InvoiceTermsSales mt = invoiceTerMap.getTerm();
                com.krawler.utils.json.base.JSONObject jsonobj = new com.krawler.utils.json.base.JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("glaccountname", mt.getAccount().getAccountName());
                jsonobj.put("accountid", mt.getAccount().getID());
                jsonobj.put("accode", mt.getAccount().getAcccode());
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("termpercentage", invoiceTerMap.getPercentage());
                jsonobj.put("termamount", invoiceTerMap.getTermamount());
                jsonobj.put("termamountinbase", invoiceTerMap.getTermamountinbase());
                jsonobj.put("termtaxamount", invoiceTerMap.getTermtaxamount());
                jsonobj.put("termtaxamountinbase", invoiceTerMap.getTermtaxamountinbase());
                jsonobj.put("termAmountExcludingTax", invoiceTerMap.getTermAmountExcludingTax());
                jsonobj.put("termAmountExcludingTaxInBase", invoiceTerMap.getTermAmountExcludingTaxInBase());
                jsonobj.put("termtax", invoiceTerMap.getTermtax()!=null ? invoiceTerMap.getTermtax().getID():"");
                jsonobj.put("linkedtaxname", invoiceTerMap.getTermtax()!=null ? invoiceTerMap.getTermtax().getName():"");
                jsonobj.put("isActivated", invoiceTerMap.getTermtax()!=null ? invoiceTerMap.getTermtax().isActivated():false);
                if(invoiceTerMap.getTermtax()!=null){
                    jsonobj.put("linkedtaxpercentage", accInvoiceDAOobj.getPercentageFromTaxid(invoiceTerMap.getTermtax().getID(), mt.getCompany().getCompanyID()));
                }else {
                    jsonobj.put("linkedtaxpercentage", 0);
                }
                jArr.put(jsonobj);
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(AccGoodsReceiptServiceHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
   
    public JSONArray getGoodsReceiptsJsonForAgedPayables(HashMap<String, Object> request, List<GoodsReceipt> list, JSONArray jArr, AccountingHandlerDAO accountingHandlerDAOobj, accCurrencyDAO accCurrencyDAOobj, accGoodsReceiptDAO accGoodsReceiptobj, accAccountDAO accAccountDAOobj, accGoodsReceiptCMN accGoodsReceiptCommon, accTaxDAO accTaxObj) throws ServiceException {
        try {
            String companyid = (String) request.get(GoodsReceiptCMNConstants.COMPANYID);
            String currencyid = (String) request.get(GoodsReceiptCMNConstants.GCURRENCYID);
            DateFormat df = (DateFormat) request.get(GoodsReceiptCMNConstants.DATEFORMAT);
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (request.containsKey(Constants.REQ_startdate) && request.get(Constants.REQ_startdate) != null) {
                if(StringUtil.isValidDateOnlyFormat(request.get(Constants.REQ_startdate).toString())){ 
                    startDate = (request.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(request.get(Constants.REQ_startdate).toString());
                } else {
                    startDate = (request.get(Constants.REQ_startdate).toString()).equals("") ? new Date(Constants.opening_Date) : new Date(Long.parseLong(request.get(Constants.REQ_startdate).toString()));
                }                
            }
            String only1099AccStr = (String) request.get(GoodsReceiptCMNConstants.ONLY1099ACC);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean isBadDebtInvoices = false;// for Malasian Company
            if (request.containsKey("isBadDebtInvoices") && request.get("isBadDebtInvoices") != null) {
                isBadDebtInvoices = (Boolean) request.get("isBadDebtInvoices");
            }

            KwlReturnObject Cmpobj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) Cmpobj.getEntityList().get(0);
            String subdomain = company.getSubDomain();

            request.put(Constants.COMPANY_SUBDOMAIN,subdomain);

//            double taxPercent = 0;
            boolean belongsTo1099 = false;
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(request.containsKey("noOfInterval") && request.get("noOfInterval") != null) {
               noOfInterval = request.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(request.get("noOfInterval").toString());
            }
            boolean isAged = (request.containsKey("isAged") && request.get("isAged") != null) ? Boolean.parseBoolean(request.get("isAged").toString()) : false;
//            boolean isProduct = (request.containsKey(GoodsReceiptCMNConstants.PRODUCTID) && !StringUtil.isNullOrEmpty((String) request.get(GoodsReceiptCMNConstants.PRODUCTID))) ? true : false;
            boolean only1099Acc = (only1099AccStr != null ? Boolean.parseBoolean(only1099AccStr) : false);
            boolean ignoreZero = request.get(GoodsReceiptCMNConstants.IGNOREZERO) != null;
            boolean onlyAmountDue = request.get(GoodsReceiptCMNConstants.ONLYAMOUNTDUE) != null;
            boolean report = request.get("report") != null;
            boolean booleanAged = true;//Added for aged payable/receivable
            Calendar oneDayBeforeCal1 = (Calendar) request.get("oneDayBeforeCal1");
            Calendar cal1 = (Calendar) request.get("cal1");
            Calendar cal2 = (Calendar) request.get("cal2");
            Calendar cal3 = (Calendar) request.get("cal3");
            Calendar cal4 = (Calendar) request.get("cal4");
            Calendar cal5 = (Calendar) request.get("cal5");
            Calendar cal6 = (Calendar) request.get("cal6");
            Calendar cal7 = (Calendar) request.get("cal7");
            Calendar cal8 = (Calendar) request.get("cal8");
            Calendar cal9 = (Calendar) request.get("cal9");
            Calendar cal10 = (Calendar) request.get("cal10");

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
//            double accruedbalance = 0;
            List ll = null;
            if (list != null && !list.isEmpty()) {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Object[] oj = (Object[]) itr.next();
                    String invid = oj[0].toString();
                    //Withoutinventory 0 for normal, 1 for billing
//                    boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());

                    amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
                    GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                    System.out.println("GoodsReceipt : "+gReceipt.getGoodsReceiptNumber());
                    JournalEntry je = null;
                    JournalEntryDetail d = null;
                    if (gReceipt.isNormalInvoice()) {
                        je = gReceipt.getJournalEntry();
                        d = gReceipt.getVendorEntry();
                    }

                    double invoiceOriginalAmt = 0d;
                    double externalCurrencyRate = 0d;
                    boolean isopeningBalanceInvoice = gReceipt.isIsOpeningBalenceInvoice();
                    Date creationDate = null;

                    currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
                    Account account = null;
                    creationDate = gReceipt.getCreationDate();
                    if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                        KwlReturnObject accObjItr = accountingHandlerDAOobj.getObject(Account.class.getName(), gReceipt.getVendor().getAccount().getID());
                        account = (Account) accObjItr.getEntityList().get(0);
                        externalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
                        invoiceOriginalAmt = gReceipt.getOriginalOpeningBalanceAmount();
                    } else {
                        account = d.getAccount();
                        externalCurrencyRate = je.getExternalCurrencyRate();
//                        creationDate = je.getEntryDate();
                        invoiceOriginalAmt = d.getAmount();
                    }
                    double amountdue = 0, amountdueinbase = 0,deductDiscount = 0;
                    boolean invoiceAmountDueEqualsInvAmount= ((!gReceipt.isIsOpeningBalenceInvoice() && gReceipt.isNormalInvoice()) ? (gReceipt.getInvoiceAmount() == gReceipt.getInvoiceamountdue()) : false);
                    request.put("invoiceAmtDueEqualsInvoiceAmt",invoiceAmountDueEqualsInvAmount);
                    if (invoiceAmountDueEqualsInvAmount) {
                        if (gReceipt.isIsExpenseType()) {
                            if (Constants.InvoiceAmountDueFlag && !isAged) {
                                ll = accGoodsReceiptCommon.getUpdatedExpGRAmountDue(request, gReceipt);
                            } else {
                                ll = accGoodsReceiptCommon.getExpGRAmountDue(request, gReceipt);
                                amountdueinbase = (Double) ll.get(5);
                            }
                            if(gReceipt.isIsOpeningBalenceInvoice()){
                                amountdue=gReceipt.getOpeningBalanceAmountDue();
                            }else{
                                amountdue=gReceipt.getInvoiceamountdue();
                            }
                            belongsTo1099 = (Boolean) ll.get(3);
                        } else {
                            if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                                ll = new ArrayList();
                                ll.add(gReceipt.getOriginalOpeningBalanceAmount());
                                ll.add(gReceipt.getOpeningBalanceAmountDue());
                                ll.add("");
                                ll.add(false);
                                ll.add(0.0);
                            } else {
                                if (Constants.InvoiceAmountDueFlag && !isAged) {
                                    ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(request, gReceipt);
                                } else {
                                    ll = accGoodsReceiptCommon.getGRAmountDue(request, gReceipt);
                                    amountdueinbase = (Double) ll.get(6);
                                }
                            }

                            if (gReceipt.isIsOpeningBalenceInvoice()) {
                                amountdue = gReceipt.getOpeningBalanceAmountDue();
                            } else {
                                amountdue = gReceipt.getInvoiceamountdue();
                            }
                            belongsTo1099 = (Boolean) ll.get(3);
                            deductDiscount = (Double) ll.get(4);
                        }
                    } else {
                        if (gReceipt.isIsExpenseType()) {
                            if (Constants.InvoiceAmountDueFlag && !isAged) {
                                ll = accGoodsReceiptCommon.getUpdatedExpGRAmountDue(request, gReceipt);
                            } else {
                                ll = accGoodsReceiptCommon.getExpGRAmountDue(request, gReceipt);
                                amountdueinbase = (Double) ll.get(5);
                            }
                            amountdue = (Double) ll.get(1);
                            belongsTo1099 = (Boolean) ll.get(3);
                        } else {
                            if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                                ll = new ArrayList();
                                ll.add(gReceipt.getOriginalOpeningBalanceAmount());
                                ll.add(gReceipt.getOpeningBalanceAmountDue());
                                ll.add("");
                                ll.add(false);
                                ll.add(0.0);
                            } else {
                                if (Constants.InvoiceAmountDueFlag && !isAged) {
                                    ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(request, gReceipt);
                                } else {
                                    ll = accGoodsReceiptCommon.getGRAmountDue(request, gReceipt);
                                    amountdueinbase = (Double) ll.get(6);
                                }
                            }
                            amountdue = (Double) ll.get(1);
                            belongsTo1099 = (Boolean) ll.get(3);
                            deductDiscount = (Double) ll.get(4);
                        }
                    }
//                    System.out.println(" Equals= "+(amountdue==(gReceipt.isIsOpeningBalenceInvoice()?gReceipt.getOpeningBalanceAmountDue():gReceipt.getInvoiceamountdue()))+", GoodsReceipt Number - "+gReceipt.getGoodsReceiptNumber()+", amountdue=  "+amountdue +", goodReceipt amountdue= "+(gReceipt.isIsOpeningBalenceInvoice()?gReceipt.getOpeningBalanceAmountDue():gReceipt.getInvoiceamountdue()));
                    if (onlyAmountDue && authHandler.round(amountdue, companyid) == 0 || (only1099Acc && !belongsTo1099)) {//remove //belongsTo1099&&gReceipt.isIsExpenseType()\\ in case of viewing all accounts. [PS]
                        continue;
                    }
                    if ((ignoreZero && authHandler.round(amountdue, companyid) <= 0)) {
                        continue;
                    }
                    int isReval = 0;
                    if (report) {
                        KwlReturnObject brdAmt = accGoodsReceiptobj.getRevalFlag(gReceipt.getID());
                        List reval = brdAmt.getEntityList();
                        if (!reval.isEmpty() && (Long) reval.get(0) > 0) {
                            isReval = 1;
                        }
                    }

                    if (request.containsKey("excludeInvoiceId") && request.get("excludeInvoiceId") != null) {
                        String excludeInvoiceId = (String) request.get("excludeInvoiceId");
                        if (gReceipt.getGoodsReceiptNumber().equals(excludeInvoiceId)) {
                            continue;
                        }
                    }
                    Vendor vendor = gReceipt.getVendor();
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", gReceipt.getID());
                    hashMap.put("companyid", companyid);
//                    KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
//                    int attachemntcount = object.getRecordTotalCount();
                    com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                    obj.put(GoodsReceiptCMNConstants.BILLID, gReceipt.getID());
                    obj.put(Constants.SUPPLIERINVOICENO, gReceipt.getSupplierInvoiceNo()!=null? gReceipt.getSupplierInvoiceNo():"");
//                    obj.put("isOpeningBalanceTransaction", gReceipt.isIsOpeningBalenceInvoice());
//                    obj.put("isNormalTransaction", gReceipt.isNormalInvoice());
//                    obj.put("parentinvoiceid", gReceipt.getParentInvoice() != null ? gReceipt.getParentInvoice().getID() : "");
                    obj.put("companyid", gReceipt.getCompany().getCompanyID());
                    obj.put("companyname", gReceipt.getCompany().getCompanyName());
                    obj.put(GoodsReceiptCMNConstants.PERSONID, vendor == null ? account.getID() : vendor.getID());
                    obj.put(GoodsReceiptCMNConstants.ALIASNAME, vendor == null ? "" : vendor.getAliasname());
                    obj.put(GoodsReceiptCMNConstants.PERSONEMAIL, vendor == null ? "" : vendor.getEmail());
                    obj.put(GoodsReceiptCMNConstants.BILLNO, gReceipt.getGoodsReceiptNumber());
                    obj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                    obj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
                    obj.put("currencyCode", (gReceipt.getCurrency() == null ? currency.getCurrencyCode() : gReceipt.getCurrency().getCurrencyCode()));
                    obj.put("currencycode", (gReceipt.getCurrency() == null ? currency.getCurrencyCode() : gReceipt.getCurrency().getCurrencyCode()));
                    obj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (gReceipt.getCurrency() == null ? currency.getName() : gReceipt.getCurrency().getName()));
                    obj.put(GoodsReceiptCMNConstants.COMPANYADDRESS, gReceipt.getCompany().getAddress());
                    obj.put(GoodsReceiptCMNConstants.COMPANYNAME, gReceipt.getCompany().getCompanyName());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(request, 1.0, currencyid, creationDate, 0);
                    obj.put(GoodsReceiptCMNConstants.OLDCURRENCYRATE, bAmt.getEntityList().get(0));
                    obj.put(GoodsReceiptCMNConstants.BILLTO, gReceipt.getBillFrom());
                    obj.put(GoodsReceiptCMNConstants.ISEXPENSEINV, gReceipt.isIsExpenseType());
                    obj.put(GoodsReceiptCMNConstants.SHIPTO, gReceipt.getShipFrom());
                    obj.put(GoodsReceiptCMNConstants.TERMNAME, gReceipt.getTermid() == null ? gReceipt.getVendor() == null ? "" : ((gReceipt.getVendor().getDebitTerm() == null) ? "" : gReceipt.getVendor().getDebitTerm().getTermname()) : gReceipt.getTermid().getTermname());
                    obj.put(GoodsReceiptCMNConstants.CustomerCreditTerm, vendor == null ? "" : ((vendor.getDebitTerm() == null) ? "" : vendor.getDebitTerm().getTermname()));
                    obj.put(GoodsReceiptCMNConstants.MEMO, gReceipt.getMemo());
                    obj.put(GoodsReceiptCMNConstants.JOURNALENTRYID, je != null ? je.getID() : "");
                    obj.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, externalCurrencyRate);
                    obj.put(GoodsReceiptCMNConstants.ENTRYNO, je != null ? je.getEntryNumber() : "");
                    obj.put(GoodsReceiptCMNConstants.DATE, df.format(creationDate));
                    obj.put(GoodsReceiptCMNConstants.SHIPDATE, gReceipt.getShipDate() == null ? "" : df.format(gReceipt.getShipDate()));
                    obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(gReceipt.getDueDate()));
                    obj.put(GoodsReceiptCMNConstants.PERSONNAME, vendor == null ? account.getName() : vendor.getName());
                    obj.put(GoodsReceiptCMNConstants.CODE,vendor == null?(account.getAcccode() == null ? "" : account.getAcccode()) : (vendor.getAcccode() == null ? "" : vendor.getAcccode()));
                    obj.put(GoodsReceiptCMNConstants.PERSONINFO, vendor == null ? account.getName() : vendor.getName()+"("+vendor.getAcccode()+")");
                    obj.put("personcode", vendor == null ? (account.getAcccode() == null ? "" : account.getAcccode()) : (vendor.getAcccode() == null ? "" : vendor.getAcccode()));
                    obj.put("salespersonname", gReceipt.getMasterAgent() == null? "" : gReceipt.getMasterAgent().getValue());
//                        obj.put("agent", gReceipt.getMasterAgent() == null ? "" : gReceipt.getMasterAgent().getID());
//                        obj.put(GoodsReceiptCMNConstants.MEMO, gReceipt.getMemo());
//                        obj.put("posttext", gReceipt.getPostText());
//                        obj.put("shiplengthval", gReceipt.getShiplength());
//                        obj.put("invoicetype", gReceipt.getInvoicetype());
//                        obj.put(GoodsReceiptCMNConstants.TERMNAME, vendor == null ? "" : ((vendor.getDebitTerm() == null) ? "" : vendor.getDebitTerm().getTermname()));
//                        obj.put(GoodsReceiptCMNConstants.DELETED, gReceipt.isDeleted());
//                        obj.put(GoodsReceiptCMNConstants.TAXINCLUDED, gReceipt.getTax() == null ? false : true);
//                        obj.put(GoodsReceiptCMNConstants.TAXID, gReceipt.getTax() == null ? "" : gReceipt.getTax().getID());
//                        obj.put(GoodsReceiptCMNConstants.TAXNAME, gReceipt.getTax() == null ? "" : gReceipt.getTax().getName());
                    obj.put(GoodsReceiptCMNConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + externalCurrencyRate + " " + (gReceipt.getCurrency() == null ? "" : gReceipt.getCurrency().getCurrencyCode()));
//                    double taxAmt = 0d;
//                    if (gReceipt.getTaxEntry() != null) {// if Invoice Level Tax is available
//                        taxAmt = gReceipt.getTaxEntry() == null ? 0 : gReceipt.getTaxEntry().getAmount();
//                    }
                    obj.put(GoodsReceiptCMNConstants.DISCOUNT, gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscountValue());
                    obj.put(GoodsReceiptCMNConstants.ISPERCENTDISCOUNT, gReceipt.getDiscount() == null ? false : gReceipt.getDiscount().isInPercent());
                    obj.put(GoodsReceiptCMNConstants.DISCOUNTVAL, gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscount());
                    double vendorCreditLimit = 0;
                    double vendorCreditLimitInbase = 0;
                    String currencyId = "";
                    if (gReceipt.getVendor() != null && gReceipt.getVendor().getCurrency() != null) {
                        currencyId = gReceipt.getVendor().getCurrency().getCurrencyID();
                        vendorCreditLimit = gReceipt.getVendor().getDebitlimit();
                        KwlReturnObject bAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(request, vendorCreditLimit, currencyId, gReceipt.getVendor().getCreatedOn(), 0);
                        vendorCreditLimitInbase = authHandler.round((Double) bAmount.getEntityList().get(0), companyid);
                    }
                    obj.put("creditlimit", vendorCreditLimit);
                    obj.put("creditlimitinbase", vendorCreditLimitInbase);
//                        obj.put(CCConstants.JSON_costcenterid, je != null ? (je.getCostcenter() == null ? "" : je.getCostcenter().getID()) : "");
//                        obj.put(CCConstants.JSON_costcenterName, je != null ? (je.getCostcenter() == null ? "" : je.getCostcenter().getName()) : "");
//                        obj.put("isfavourite", gReceipt.isFavourite());
//                        obj.put("isprinted", gReceipt.isPrinted());
//                        obj.put("cashtransaction", gReceipt.isCashtransaction());
//                        obj.put("archieve", 0);
//                        obj.put("shipvia", gReceipt.getShipvia() == null ? "" : gReceipt.getShipvia());
//                        obj.put("fob", gReceipt.getFob() == null ? "" : gReceipt.getFob());
//                        obj.put("termdetails", getTermDetails(gReceipt.getID(), accGoodsReceiptobj));
                    if (gReceipt.getTermsincludegst() != null) {
                        obj.put(Constants.termsincludegst, gReceipt.getTermsincludegst());
                    }
//                    double termAmount = CommonFunctions.getTotalTermsAmount(getTermDetails(gReceipt.getID(), accGoodsReceiptobj));
//                        obj.put("termamount", termAmount);
//                        obj.put("termdays", gReceipt.getTermid() == null ? 0 : gReceipt.getTermid().getTermdays());
//                        obj.put("termid", gReceipt.getTermid() == null ? "" : gReceipt.getTermid().getID());
//                        obj.put("landedInvoiceID", gReceipt.getLandedInvoice() == null ? "" : gReceipt.getLandedInvoice().getID());
//                        obj.put("landedInvoiceNumber", gReceipt.getLandedInvoice() == null ? "" : gReceipt.getLandedInvoice().getGoodsReceiptNumber());
//                        obj.put("billto", gReceipt.getBillTo() == null ? "" : gReceipt.getBillTo());
//                        obj.put("shipto", gReceipt.getShipTo() == null ? "" : gReceipt.getShipTo());
//                        obj.put("isCapitalGoodsAcquired", gReceipt.isCapitalGoodsAcquired());
//                        obj.put("isRetailPurchase", gReceipt.isRetailPurchase());
//                        obj.put("importService", gReceipt.isImportService());
//                        obj.put("attachment", attachemntcount);
                    Set<GoodsReceiptDetail> goodsReceiptDetails = gReceipt.getRows();
                    if (isBadDebtInvoices) {// in case of Malasian Company
//                        int baddebttype = (Integer) request.get("baddebttype");
                        double totalTaxAmt = 0d;
                        double invoiceLevelTaxAmt = gReceipt.getTaxEntry() == null ? 0 : gReceipt.getTaxEntry().getAmount();
                        String taxId = gReceipt.getTaxEntry() == null ? "" : gReceipt.getTax().getID();
//                            Set<GoodsReceiptDetail> invRows = gReceipt.getRows();
                        double rowTaxAmt = 0d;
                        for (GoodsReceiptDetail invoiceDetail : goodsReceiptDetails) {
                            rowTaxAmt += invoiceDetail.getRowTaxAmount() + invoiceDetail.getRowTermTaxAmount();
                            taxId = (invoiceDetail.getTax() != null) ? invoiceDetail.getTax().getID() : taxId;
                        }
                        totalTaxAmt = invoiceLevelTaxAmt + rowTaxAmt;
                        if (totalTaxAmt == 0) {// no need to put invoice in bad debt section if it has tax 0
                            continue;
                        }

                        // get Paid amount of invoice
                        Date badDebtCalculationToDate = null;
                        if (request.get("badDebtCalculationToDate") != null) {
                            badDebtCalculationToDate = df.parse((String) request.get("badDebtCalculationToDate"));
                        }
                        KwlReturnObject invoicePaidAmtObj = accPaymentDAOobj.getPaymentFromBadDebtClaimedInvoice(gReceipt.getID(), true, badDebtCalculationToDate);//accPaymentDAOobj.getPaymentAmountofBadDebtGoodsReceipt(gReceipt.getID(),true);

                        double paidAmt = 0;

                        List paidList = invoicePaidAmtObj.getEntityList();
                        if (paidList != null && !paidList.isEmpty()) {
                            Iterator pmtIt = paidList.iterator();
                            while (pmtIt.hasNext()) {
                                PaymentDetail rd = (PaymentDetail) pmtIt.next();

                                double paidAmtInPaymentCurrency = rd.getAmount();

//                                KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, paidAmtInPaymentCurrency, rd.getPayment().getCurrency().getCurrencyID(), rd.getPayment().getJournalEntry().getEntryDate(), rd.getPayment().getJournalEntry().getExternalCurrencyRate());
                                KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, paidAmtInPaymentCurrency, rd.getPayment().getCurrency().getCurrencyID(), rd.getPayment().getCreationDate(), rd.getPayment().getJournalEntry().getExternalCurrencyRate());
                                double paidAmtInBase = authHandler.round((Double) bAmt1.getEntityList().get(0), companyid);

                                paidAmt += paidAmtInBase;

                            }
                        }

                        // paidAmt should be converted into base currency
//                        KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), taxId);
                        KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, gReceipt.getCreationDate(), taxId);

                        double taxPer = (Double) taxObj.getEntityList().get(0);

                        // Gst claimable amount
                        double grAmountDue = gReceipt.getInvoiceamountdue();

                        // Converting grAmountDue to base currency
                        String fromcurrencyid = gReceipt.getCurrency().getCurrencyID();
//                        KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, grAmountDue, fromcurrencyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                        KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, grAmountDue, fromcurrencyid, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                        grAmountDue = authHandler.round((Double) bAmt1.getEntityList().get(0), companyid);

//                        double gstclaimableamount = grAmountDue * taxPer / (100 + taxPer);

                        //Calculate Bad Debt Recoverable Amount
                        // Get Recovered Amount of invoice
                        HashMap<String, Object> badMaps = new HashMap<String, Object>();
                        badMaps.put("companyid", companyid);
                        badMaps.put("invoiceid", invid);

                        KwlReturnObject badDebtMappingResult = accGoodsReceiptobj.getBadDebtPurchaseInvoiceMappingForGoodsReceipt(badMaps);

                        Date badDebtClaimedDate = null;
                        double totalRecoveredAmt = 0;
                        List badDebtMapping = badDebtMappingResult.getEntityList();
                        if (!badDebtMapping.isEmpty()) {
                            Iterator badIt = badDebtMapping.iterator();
                            while (badIt.hasNext()) {
                                BadDebtPurchaseInvoiceMapping debtInvoiceMapping = (BadDebtPurchaseInvoiceMapping) badIt.next();
                                totalRecoveredAmt += debtInvoiceMapping.getBadDebtAmtRecovered();
                                if (debtInvoiceMapping.getBadDebtClaimedDate() != null) {
                                    badDebtClaimedDate = debtInvoiceMapping.getBadDebtClaimedDate();
                                }
                            }
                        }

                        // Calculate Recover Amount in base
//                        bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, totalRecoveredAmt, fromcurrencyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                        bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, totalRecoveredAmt, fromcurrencyid, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                        totalRecoveredAmt = authHandler.round((Double) bAmt1.getEntityList().get(0), companyid);

//                            if (baddebttype == 1) {
//                                double claimedGST = 0;
//
//                                if (!badDebtMapping.isEmpty()) {
//                                    Iterator badIt = badDebtMapping.iterator();
//                                    while (badIt.hasNext()) {
//                                        BadDebtPurchaseInvoiceMapping debtInvoiceMapping = (BadDebtPurchaseInvoiceMapping) badIt.next();
//                                        if (debtInvoiceMapping.getBadDebtType() == 0) {
//                                            claimedGST += debtInvoiceMapping.getBadDebtGSTAmtClaimed();
//                                        }
//                                    }
//                                }
//
//                                // converting claimed GST in Base Currency
//                                bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, claimedGST, fromcurrencyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
//                                claimedGST = authHandler.round((Double) bAmt1.getEntityList().get(0), 3);
//
//                                obj.put("gstclaimableamount", claimedGST);
//                            } else {
//                                obj.put("gstclaimableamount", gstclaimableamount);
//
//                                Date selectedCriteriaDate = df.parse((String) request.get("selectedCriteriaDate"));
//                                int badDebtCriteria = (Integer) request.get("badDebtCriteria");
//
//                                long diff = 0;
//                                if (badDebtCriteria == 0 && gReceipt.getDueDate() != null) {// on Invoice Due Date
//                                    diff = selectedCriteriaDate.getTime() - gReceipt.getDueDate().getTime();
//                                } else if (badDebtCriteria == 1) {// on Invoice Creation Date
//                                    diff = selectedCriteriaDate.getTime() - creationDate.getTime();
//                                }
//                                long diffInDays = diff / (24 * 60 * 60 * 1000);
//
//                                obj.put("agingDays", diffInDays);
//
//                            }
                        double gstToRecover = (paidAmt - totalRecoveredAmt) * taxPer / (100 + taxPer);
                        double paidAfterClaimed = paidAmt - totalRecoveredAmt;

                        obj.put("paidAfterClaimed", paidAfterClaimed);
                        obj.put("gstToRecover", gstToRecover);
                        obj.put("claimedPeriod", gReceipt.getClaimedPeriod());
                        obj.put("badDebtClaimedDate", (badDebtClaimedDate != null) ? df.format(badDebtClaimedDate) : null);

                        if (authHandler.round(amountdue, companyid) == 0 && authHandler.round(paidAfterClaimed, companyid) == 0) {// don't put invoices which has amount due zero and whole gst has been recovered
                            continue;
                        }
                    }
//                    obj = AccountingAddressManager.getTransactionAddressJSON(obj, gReceipt.getBillingShippingAddresses(), true);
//                    obj.put("sequenceformatid", gReceipt.getSeqformat() == null ? "" : gReceipt.getSeqformat().getID());
//                    obj.put("gstIncluded", gReceipt.isGstIncluded());
//                    obj.put("selfBilledInvoice", gReceipt.isSelfBilledInvoice());
//                    obj.put("RMCDApprovalNo", gReceipt.getRMCDApprovalNo());
//                    obj.put("fixedAssetInvoice", gReceipt.isFixedAssetInvoice());
//                    obj.put("isConsignment", gReceipt.isIsconsignment());
//                    if (gReceipt.isCashtransaction()) {
//                        PayDetail payDetail = gReceipt.getPayDetail();
//                        if (payDetail != null) {
//                            PaymentMethod paymentMethod = payDetail.getPaymentMethod();
//                            obj.put("paymentname", paymentMethod.getMethodName());
//                            obj.put("methodid", paymentMethod.getID());
//                            obj.put("detailtype", paymentMethod.getDetailType());
//                            if (paymentMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
//                                Card card = payDetail.getCard();
//                                obj.put("cardno", card != null ? card.getCardNo() : "");
//                                obj.put("nameoncard", card != null ? card.getCardHolder() : "");
//                                obj.put("cardexpirydate", card != null ? df.format(card.getExpiryDate()) : "");
//                                obj.put("cardtype", card != null ? card.getCardType() : "");
//                                obj.put("cardrefno", card != null ? card.getRefNo() : "");
//
//                            } else if (paymentMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
//                                Cheque cheque = payDetail.getCheque();
//                                obj.put("chequeno", cheque != null ? cheque.getChequeNo() : "");
//                                obj.put("chequedescription", cheque != null ? cheque.getDescription() : "");
//                                obj.put("bankname", cheque != null ? cheque.getBankName() : "");
//                                obj.put("chequedate", cheque != null ? df.format(cheque.getDueDate()) : "");
//                            }
//                        } else {
//                            obj.put("paymentname", "");
//                            obj.put("methodid", "");
//                        }
//                    } else {
//                        obj.put("paymentname", "NA");
//                    }
//                    if (gReceipt.getModifiedby() != null) {
//                        obj.put("lasteditedby", StringUtil.getFullName(gReceipt.getModifiedby()));
//                    }

//                    if (report) {
//                        obj.put("isreval", isReval);
//                    }

//                    obj.put("approvalstatus", gReceipt.getApprovestatuslevel());
//                    String approvalStatus = "";
//                    if (gReceipt.getApprovestatuslevel() < 0) {
//                        approvalStatus = "Rejected";
//                    } else if (gReceipt.getApprovestatuslevel() < 11) {
//                        String ruleid = "", userRoleName = "";
//                        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
//                        qdDataMap.put("companyid", companyid);
//                        qdDataMap.put("level", gReceipt.getApprovestatuslevel());
//                        qdDataMap.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
//                        KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
//                        Iterator ruleitr = flowresult.getEntityList().iterator();
//                        while (ruleitr.hasNext()) {
//                            Object[] rulerow = (Object[]) ruleitr.next();
//                            ruleid = rulerow[0].toString();
//                        }
//                        if (!StringUtil.isNullOrEmpty(ruleid)) {
//                            KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(ruleid);
//                            Iterator useritr = userResult.getEntityList().iterator();
//                            while (useritr.hasNext()) {
//                                Object[] userrow = (Object[]) useritr.next();
//                                String userId = userrow[0].toString();
//                                String userName = userrow[1].toString();
//                                KwlReturnObject kmsg = null;
//                                String roleName = "Company User";
//                                kmsg = permissionHandlerDAOObj.getRoleofUser(userId);
//                                Iterator ite2 = kmsg.getEntityList().iterator();
//                                while (ite2.hasNext()) {
//                                    Object[] row = (Object[]) ite2.next();
//                                    roleName = row[1].toString();
//                                }
//                                userRoleName += roleName + " " + userName + ",";
//                            }
//                        }
//                        if (!StringUtil.isNullOrEmpty(userRoleName)) {
//                            userRoleName = userRoleName.substring(0, userRoleName.length() - 1);
//                        }
//                        approvalStatus = "Pending Approval" + (StringUtil.isNullOrEmpty(userRoleName) ? "" : " by " + userRoleName) + " at Level - " + gReceipt.getApprovestatuslevel();
//                    } else {
//                        approvalStatus = "Approved";
//                    }
//                    obj.put("approvalstatusinfo", approvalStatus);

                    /*
                     * For Product search, add Products details from Invoice
                     * details
                     */
//                    if (isProduct && gReceipt.isNormalInvoice()) {
//                        String idvString = isProduct ? oj[4].toString() : ""; //as in list invoiedetail id comes 4th
//                        KwlReturnObject objItrGRD = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), idvString);
//                        GoodsReceiptDetail idvObj = (GoodsReceiptDetail) objItrGRD.getEntityList().get(0);
//                        if (idvObj != null) {
//                            obj.put("rowproductname", idvObj.getInventory().getProduct().getName());
//                            obj.put("rowquantity", idvObj.getInventory().isInvrecord() ? idvObj.getInventory().getQuantity() : idvObj.getInventory().getActquantity());
//                            obj.put("rowrate", idvObj.getRate());
//
//                            Discount disc = idvObj.getDiscount();
//                            if (disc != null && disc.isInPercent()) {
//                                obj.put("rowprdiscount", disc.getDiscount()); //product discount in percent
//                            } else {
//                                obj.put("rowprdiscount", 0);
//                            }
//
//                            double rowTaxPercent = 0;
//                            if (idvObj.getTax() != null) {
//                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), idvObj.getTax().getID());
//                                rowTaxPercent = (Double) perresult.getEntityList().get(0);
//                            }
//                            obj.put("rowprtaxpercent", rowTaxPercent);
//                        }
//                    }

                    //For getting tax in percent applyied on invoice [PS]
//                    if (gReceipt.getTax() != null) {
//                        KwlReturnObject taxresult = accTaxObj.getTaxPercent(companyid, je.getEntryDate(), gReceipt.getTax().getID());
//                        taxPercent = (Double) taxresult.getEntityList().get(0);
//                    }
//                    obj.put(GoodsReceiptCMNConstants.TAXPERCENT, taxPercent);

                    //For getting amountdue [PS]
                    if (gReceipt.isCashtransaction()) {
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, 0);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, 0);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUENONNEGATIVE, 0);
                        obj.put(GoodsReceiptCMNConstants.INCASH, true);
                    } else {
                        if ((Constants.InvoiceAmountDueFlag && !isAged) || isopeningBalanceInvoice) {
                            if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, amountdue, currencyid, creationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(request, amountdue, currencyid, creationDate, externalCurrencyRate);
                            }
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                        } else {
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, amountdueinbase);
                        }
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, authHandler.round(amountdue, companyid));

                        if (booleanAged) {//Added for aged payable/receivable
                            int datefilter = (request.containsKey("datefilter") && request.get("datefilter") != null) ? Integer.parseInt(request.get("datefilter").toString()) : 0;// 0 = Invoice Due date OR 1 = Invoice date
                            Date dueDate = null;
                            if (!StringUtil.isNullOrEmpty(df.format(gReceipt.getDueDate()))) {
                                dueDate = df.parse(df.format(gReceipt.getDueDate()));
                            }
                            if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                dueDate = df.parse(df.format(gReceipt.getDueDate()));
                            } else {
                                dueDate = df.parse(df.format(creationDate));
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
                        
                        }
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUENONNEGATIVE, (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    }
//                    boolean includeprotax = false;
//                    double rowTaxAmt = 0d;
//                    if (!gReceipt.isIsExpenseType() && gReceipt.isNormalInvoice()) {
//                        for (GoodsReceiptDetail goodsReceiptDetail : goodsReceiptDetails) {
//                            if (goodsReceiptDetail.getTax() != null) {
////                                includeprotax = true;
//                                rowTaxAmt += goodsReceiptDetail.getRowTaxAmount();
//                            }
//                        }
//                    } else if (gReceipt.isIsExpenseType()) {
//                        Set<ExpenseGRDetail> expenseGRDetails = gReceipt.getExpenserows();
//                        for (ExpenseGRDetail expGReceiptDetail : expenseGRDetails) {
//                                System.out.println(expGReceiptDetail.getGoodsReceipt().getGoodsReceiptNumber());
//                            if (expGReceiptDetail.getTax() != null) {
////                                includeprotax = true;
//                                break;
//                            }
//                        }
//                    }

//                    taxAmt += rowTaxAmt;// either row level tax will be avvailable or invoice level

//                    obj.put("amountbeforegst", authHandler.round(invoiceOriginalAmt - taxAmt - termAmount, 2));   // Amount before both kind of tax row level or transaction level
//                    obj.put(GoodsReceiptCMNConstants.TAXAMOUNT, taxAmt);
//                    obj.put("taxamountinbase", accCurrencyDAOobj.getCurrencyToBaseAmount(request, taxAmt, currencyid, creationDate, externalCurrencyRate).getEntityList().get(0));

//                    obj.put("includeprotax", includeprotax);
                    obj.put(GoodsReceiptCMNConstants.AMOUNT, authHandler.round((Double) invoiceOriginalAmt, companyid)); //actual invoice amount
                    if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, invoiceOriginalAmt, currencyid, creationDate, externalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(request, invoiceOriginalAmt, currencyid, creationDate, externalCurrencyRate);
                    }
                    obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                    obj.put(GoodsReceiptCMNConstants.ACCOUNTNAMES, (String) ll.get(2));

                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE1, amountdue1);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE2, amountdue2);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE3, amountdue3);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE4, amountdue4);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE5, amountdue5);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE6, amountdue6);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE7, amountdue7);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE8, amountdue8);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE9, amountdue9);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE10, amountdue10);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE11, amountdue11);
//                    obj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
                    obj.put(GoodsReceiptCMNConstants.TYPE, "Purchase Invoice");
                    obj.put(GoodsReceiptCMNConstants.DEDUCTDISCOUNT, deductDiscount);
                    if (!(ignoreZero && authHandler.round(amountdue, companyid) <= 0)) {
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getGoodsReceiptsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public static HashMap<String, Object> getGoodsReceiptRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
        requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
        requestParams.put(DATEFORMAT, authHandler.getDateOnlyFormat(request));
        requestParams.put(START, request.getParameter(START));
        requestParams.put(LIMIT, request.getParameter(LIMIT));
        requestParams.put(SS, request.getParameter(SS));
        requestParams.put(ACCID, request.getParameter(ACCID));
        requestParams.put(CASHONLY, request.getParameter(CASHONLY));
        requestParams.put(CREDITONLY, request.getParameter(CREDITONLY));
        requestParams.put(IGNOREZERO, request.getParameter(IGNOREZERO));
        requestParams.put(CURDATE, request.getParameter(CURDATE));
        requestParams.put(PERSONGROUP, request.getParameter(PERSONGROUP));
        requestParams.put(ISAGEDGRAPH, request.getParameter(ISAGEDGRAPH));
        requestParams.put(VENDORID, request.getParameter(VENDORID));
        requestParams.put(NONDELETED, request.getParameter(NONDELETED));
        requestParams.put(DURATION, request.getParameter(DURATION));
        requestParams.put(ISDISTRIBUTIVE, request.getParameter(ISDISTRIBUTIVE));
        requestParams.put(WITHINVENTORY, request.getParameter(WITHINVENTORY));
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
        requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(InvoiceConstants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        requestParams.put("pendingapproval", (request.getParameter("pendingapproval") != null) ? Boolean.parseBoolean(request.getParameter("pendingapproval")) : false);
        requestParams.put("istemplate", (request.getParameter("istemplate") != null) ? Integer.parseInt(request.getParameter("istemplate")) : 0);
        requestParams.put("datefilter", request.getParameter("datefilter"));
        requestParams.put("noOfInterval", (request.getParameter("noOfInterval") != null) ? Integer.parseInt(request.getParameter("noOfInterval")) : 7);
        requestParams.put("custVendorID", request.getParameter("custVendorID"));
        requestParams.put("asofdate", request.getParameter("asofdate"));
        requestParams.put("isAged", request.getParameter("isAged"));
        requestParams.put("isAgedPayables", true);
        requestParams.put("exportAgedPayables", true); // add totalinbase to jsonarray, if exportAgedReceivables is true
//        requestParams.put("exportAgedReceivables", true); // add totalinbase to jsonarray, if exportAgedReceivables is true
        return requestParams;
    }
    /**
     * Function to get GoodsReceipt JSON for Monthly Aged Payable Report
     * @return JSONArray
     * @throws ServiceException
     */
    public JSONArray getGoodsReceiptsJsonForMonthlyAgedPayables(HashMap<String, Object> request, List<GoodsReceipt> list, JSONArray jArr, AccountingHandlerDAO accountingHandlerDAOobj, accCurrencyDAO accCurrencyDAOobj, accGoodsReceiptDAO accGoodsReceiptobj, accAccountDAO accAccountDAOobj, accGoodsReceiptCMN accGoodsReceiptCommon, accTaxDAO accTaxObj) throws ServiceException {
        try {
            String companyid = (String) request.get(GoodsReceiptCMNConstants.COMPANYID);
            String currencyid = (String) request.get(GoodsReceiptCMNConstants.GCURRENCYID);
            DateFormat df = (DateFormat) request.get(GoodsReceiptCMNConstants.DATEFORMAT);
            String only1099AccStr = (String) request.get(GoodsReceiptCMNConstants.ONLY1099ACC);
            List ll = null;
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean isBadDebtInvoices = false;// for Malasian Company
            boolean isproductCategory = false;
            boolean isproductType = false;
            if (request.containsKey("isBadDebtInvoices") && request.get("isBadDebtInvoices") != null) {
                isBadDebtInvoices = (Boolean) request.get("isBadDebtInvoices");
            }
            if (request.containsKey("productCategoryid") && request.get("productCategoryid") != null && !StringUtil.isNullOrEmpty((String) request.get("productCategoryid"))) {
                isproductCategory = true;
            }
            if (request.containsKey(InvoiceConstants.productid) && request.get(InvoiceConstants.productid) != null && !StringUtil.isNullOrEmpty((String) request.get(InvoiceConstants.productid))) {
                isproductType = true;
            }
            boolean belongsTo1099 = false;
            boolean isAged = (request.containsKey("isAged") && request.get("isAged") != null) ? Boolean.parseBoolean(request.get("isAged").toString()) : false;
            boolean only1099Acc = (only1099AccStr != null ? Boolean.parseBoolean(only1099AccStr) : false);
            boolean ignoreZero = request.get(GoodsReceiptCMNConstants.IGNOREZERO) != null;
            boolean onlyAmountDue = request.get(GoodsReceiptCMNConstants.ONLYAMOUNTDUE) != null;
            boolean booleanAged = true;//Added for aged payable/receivable
            List InvoiceList = new ArrayList();
            Calendar oneDayBeforeCal1 = (Calendar) request.get("oneDayBeforeCal1");
            Calendar cal1 = (Calendar) request.get("cal1");
            Calendar cal2 = (Calendar) request.get("cal2");
            Calendar cal3 = (Calendar) request.get("cal3");
            Calendar cal4 = (Calendar) request.get("cal4");
            Calendar cal5 = (Calendar) request.get("cal5");
            Calendar cal6 = (Calendar) request.get("cal6");
            Calendar cal7 = (Calendar) request.get("cal7");
            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            DateFormat dateFormat=(DateFormat) authHandler.getDateOnlyFormat();
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
            double amountdue1 = 0;
            double amountdue2 = 0;
            double amountdue3 = 0;
            double amountdue4 = 0;
            double amountdue5 = 0;
            double amountdue6 = 0;
            double amountdue7 = 0;
            double amountdue8 = 0;
            Date asOfDate = null, today = new Date();
            if (request.containsKey("asofdate") && request.get("asofdate") != null) {
                String asOfDateString = (String) request.get("asofdate");
                asOfDate = df.parse(asOfDateString);
            }
            boolean asOfDateEqualsToday = asOfDate != null ? DateUtils.isSameDay(today, asOfDate) : false;
            request.put("asOfDateEqualsToday", asOfDateEqualsToday);
            if (list != null && !list.isEmpty()) {
                for (Object objectArr : list) {
                    Object[] oj = (Object[]) objectArr;
                    String invid = oj[0].toString();
                    amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = 0;
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
                    GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                    //Below If Block code is used to remove duplicate invoice id's when filter on the basis of Product category or Product name
                    if (isproductCategory || isproductType) {
                        if (InvoiceList.contains(gReceipt.getID())) {
                            continue;
                        } else {
                            InvoiceList.add(gReceipt.getID());
                        }
                    }
                    JournalEntry je = null;
                    JournalEntryDetail d = null;
                    if (gReceipt.isNormalInvoice()) {
                        je = gReceipt.getJournalEntry();
                        d = gReceipt.getVendorEntry();
                    }
                    double invoiceOriginalAmt = 0d;
                    double externalCurrencyRate = 0d;
                    boolean isopeningBalanceInvoice = gReceipt.isIsOpeningBalenceInvoice();
                    Date creationDate = null;
                    currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
                    Account account = null;
                    creationDate = gReceipt.getCreationDate();
                    if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                        KwlReturnObject accObjItr = accountingHandlerDAOobj.getObject(Account.class.getName(), gReceipt.getVendor().getAccount().getID());
                        account = (Account) accObjItr.getEntityList().get(0);
                        externalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
                        invoiceOriginalAmt = gReceipt.getOriginalOpeningBalanceAmount();
                    } else {
                        account = d.getAccount();
                        externalCurrencyRate = je.getExternalCurrencyRate();
//                        creationDate = je.getEntryDate();
                        invoiceOriginalAmt = d.getAmount();
                    }
                    double amountdue = 0;
                    boolean invoiceAmountDueEqualsInvAmount= ((!gReceipt.isIsOpeningBalenceInvoice() && gReceipt.isNormalInvoice()) ? (gReceipt.getInvoiceAmount() == gReceipt.getInvoiceamountdue()) : false);
                    request.put("invoiceAmtDueEqualsInvoiceAmt",invoiceAmountDueEqualsInvAmount);
                    if (asOfDateEqualsToday || invoiceAmountDueEqualsInvAmount) {
                        if (gReceipt.isIsExpenseType()) {
                            if (Constants.InvoiceAmountDueFlag && !isAged) {
                                ll = accGoodsReceiptCommon.getUpdatedExpGRAmountDue(request, gReceipt);
                            } else {
                                ll = accGoodsReceiptCommon.getExpGRAmountDue(request, gReceipt);
                            }
                            if(gReceipt.isIsOpeningBalenceInvoice()){
                                amountdue=gReceipt.getOpeningBalanceAmountDue();
                            }else{
                                amountdue=gReceipt.getInvoiceamountdue();
                            }
                            belongsTo1099 = (Boolean) ll.get(3);
                        } else {
                            if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                                ll = new ArrayList();
                                ll.add(gReceipt.getOriginalOpeningBalanceAmount());
                                ll.add(gReceipt.getOpeningBalanceAmountDue());
                                ll.add("");
                                ll.add(false);
                                ll.add(0.0);
                            } else {
                                if (Constants.InvoiceAmountDueFlag && !isAged) {
                                    ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(request, gReceipt);
                                } else {
                                    ll = accGoodsReceiptCommon.getGRAmountDueForMonthlyAgedPayable(request, gReceipt);
                                }
                            }

                            if (gReceipt.isIsOpeningBalenceInvoice()) {
                                amountdue = gReceipt.getOpeningBalanceAmountDue();
                            } else {
                                amountdue = gReceipt.getInvoiceamountdue();
                            }
                            belongsTo1099 = (Boolean) ll.get(3);
                        }
                    } else {
                        if (gReceipt.isIsExpenseType()) {
                            if (Constants.InvoiceAmountDueFlag && !isAged) {
                                ll = accGoodsReceiptCommon.getUpdatedExpGRAmountDue(request, gReceipt);
                            } else {
                                ll = accGoodsReceiptCommon.getExpGRAmountDue(request, gReceipt);
                            }
                            amountdue = (Double) ll.get(1);
                            belongsTo1099 = (Boolean) ll.get(3);
                        } else {
                            if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                                ll = new ArrayList();
                                ll.add(gReceipt.getOriginalOpeningBalanceAmount());
                                ll.add(gReceipt.getOpeningBalanceAmountDue());
                                ll.add("");
                                ll.add(false);
                                ll.add(0.0);
                            } else {
                                if (Constants.InvoiceAmountDueFlag && !isAged) {
                                    ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(request, gReceipt);
                                } else {
                                    ll = accGoodsReceiptCommon.getGRAmountDueForMonthlyAgedPayable(request, gReceipt);
                                }
                            }
                            amountdue = (Double) ll.get(1);
                            belongsTo1099 = (Boolean) ll.get(3);
                        }
                    }
                    if (onlyAmountDue && authHandler.round(amountdue, companyid) == 0 || (only1099Acc && !belongsTo1099)) {//remove //belongsTo1099&&gReceipt.isIsExpenseType()\\ in case of viewing all accounts. [PS]
                        continue;
                    }
                    if ((ignoreZero && authHandler.round(amountdue, companyid) <= 0)) {
                        continue;
                    }
                    if (request.containsKey("excludeInvoiceId") && request.get("excludeInvoiceId") != null) {
                        String excludeInvoiceId = (String) request.get("excludeInvoiceId");
                        if (gReceipt.getGoodsReceiptNumber().equals(excludeInvoiceId)) {
                            continue;
                        }
                    }
                    Vendor vendor = gReceipt.getVendor();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("invoiceID", gReceipt.getID());
                    com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                    obj.put(GoodsReceiptCMNConstants.BILLID, gReceipt.getID());
                    obj.put("isOpeningBalanceTransaction", gReceipt.isIsOpeningBalenceInvoice());
                    obj.put("isNormalTransaction", gReceipt.isNormalInvoice());
                    obj.put(GoodsReceiptCMNConstants.PERSONID, vendor == null ? account.getID() : vendor.getID());
                    obj.put(GoodsReceiptCMNConstants.BILLNO, gReceipt.getGoodsReceiptNumber());
                    obj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                    obj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
                    obj.put("currencyCode", (gReceipt.getCurrency() == null ? currency.getCurrencyCode() : gReceipt.getCurrency().getCurrencyCode()));
                    obj.put("currencycode", (gReceipt.getCurrency() == null ? currency.getCurrencyCode() : gReceipt.getCurrency().getCurrencyCode()));
                    obj.put("termname", (gReceipt.getTermid() == null ? vendor.getDebitTerm().getTermname() : gReceipt.getTermid().getTermname()));
                    obj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (gReceipt.getCurrency() == null ? currency.getName() : gReceipt.getCurrency().getName()));
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(request, 1.0, currencyid, creationDate, 0);
                    obj.put(GoodsReceiptCMNConstants.OLDCURRENCYRATE, bAmt.getEntityList().get(0));
                    obj.put(GoodsReceiptCMNConstants.DATE, df.format(creationDate));
                    obj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(gReceipt.getDueDate()));
                    obj.put(GoodsReceiptCMNConstants.PERSONNAME, vendor == null ? account.getName() : vendor.getName());
                    obj.put(GoodsReceiptCMNConstants.PERSONINFO, vendor == null ? account.getName() : vendor.getName()+"("+vendor.getAcccode()+")");
                    obj.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, externalCurrencyRate);
                    obj.put(GoodsReceiptCMNConstants.JOURNALENTRYID, je != null ? je.getID() : ""); //'journalentryid' is used to fetch data of this invoice to show in journal entry tab
                    obj.put(GoodsReceiptCMNConstants.ENTRYNO, je != null ? je.getEntryNumber() : ""); // 'entryno' is used to show journalentry no. in entry no. column
                    obj.put("cashtransaction", gReceipt.isCashtransaction());
                    obj.put(GoodsReceiptCMNConstants.TYPE, "Purchase Invoice");
                    Set<GoodsReceiptDetail> goodsReceiptDetails = gReceipt.getRows();
                    // Calculating total invoice amount in base currency
                    KwlReturnObject invoiceTotalAmtInBaseResult = null;
                    if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        invoiceTotalAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, invoiceOriginalAmt, currencyid, creationDate, externalCurrencyRate);
                    } else {
                        invoiceTotalAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(request, invoiceOriginalAmt, currencyid, creationDate, externalCurrencyRate);
                    }
                    double invoiceTotalAmountInBase = authHandler.round((Double) invoiceTotalAmtInBaseResult.getEntityList().get(0), companyid);
                    if (isBadDebtInvoices) {// in case of Malasian Company
                        double totalTaxAmt = 0d;
                        double invoiceLevelTaxAmt = gReceipt.getTaxEntry() == null ? 0 : gReceipt.getTaxEntry().getAmount();
                        String taxId = gReceipt.getTaxEntry() == null ? "" : gReceipt.getTax().getID();
                        double rowTaxAmt = 0d;
                        for (GoodsReceiptDetail invoiceDetail : goodsReceiptDetails) {
                            rowTaxAmt += invoiceDetail.getRowTaxAmount() + invoiceDetail.getRowTermTaxAmount();
                            taxId = (invoiceDetail.getTax() != null) ? invoiceDetail.getTax().getID() : taxId;
                        }
                        totalTaxAmt = invoiceLevelTaxAmt + rowTaxAmt;
                        if (totalTaxAmt == 0) {// no need to put invoice in bad debt section if it has tax 0
                            continue;
                        }
                        // get Paid amount of invoice
                        Date badDebtCalculationToDate = null;
                        if (request.get("badDebtCalculationToDate") != null) {
                            badDebtCalculationToDate = df.parse((String) request.get("badDebtCalculationToDate"));
                        }
                        KwlReturnObject invoicePaidAmtObj = accPaymentDAOobj.getPaymentFromBadDebtClaimedInvoice(gReceipt.getID(), true, badDebtCalculationToDate);//accPaymentDAOobj.getPaymentAmountofBadDebtGoodsReceipt(gReceipt.getID(),true);
                        double paidAmt = 0;
                        List paidList = invoicePaidAmtObj.getEntityList();
                        if (paidList != null && !paidList.isEmpty()) {
                            Iterator pmtIt = paidList.iterator();
                            while (pmtIt.hasNext()) {
                                PaymentDetail rd = (PaymentDetail) pmtIt.next();
                                double paidAmtInPaymentCurrency = rd.getAmount();
//                                KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, paidAmtInPaymentCurrency, rd.getPayment().getCurrency().getCurrencyID(), rd.getPayment().getJournalEntry().getEntryDate(), rd.getPayment().getJournalEntry().getExternalCurrencyRate());
                                KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, paidAmtInPaymentCurrency, rd.getPayment().getCurrency().getCurrencyID(), rd.getPayment().getCreationDate(), rd.getPayment().getJournalEntry().getExternalCurrencyRate());
                                double paidAmtInBase = authHandler.round((Double) bAmt1.getEntityList().get(0), companyid);
                                paidAmt += paidAmtInBase;

                            }
                        }
                        String fromcurrencyid = gReceipt.getCurrency().getCurrencyID();
                        HashMap<String, Object> badMaps = new HashMap<String, Object>();
                        badMaps.put("companyid", companyid);
                        badMaps.put("invoiceid", invid);

                        KwlReturnObject badDebtMappingResult = accGoodsReceiptobj.getBadDebtPurchaseInvoiceMappingForGoodsReceipt(badMaps);
                        double totalRecoveredAmt = 0;
                        List badDebtMapping = badDebtMappingResult.getEntityList();
                        if (!badDebtMapping.isEmpty()) {
                            Iterator badIt = badDebtMapping.iterator();
                            while (badIt.hasNext()) {
                                BadDebtPurchaseInvoiceMapping debtInvoiceMapping = (BadDebtPurchaseInvoiceMapping) badIt.next();
                                totalRecoveredAmt += debtInvoiceMapping.getBadDebtAmtRecovered();
                            }
                        }
                        // Calculate Recover Amount in base
//                        KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, totalRecoveredAmt, fromcurrencyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                        KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(request, totalRecoveredAmt, fromcurrencyid, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                        totalRecoveredAmt = authHandler.round((Double) bAmt1.getEntityList().get(0), companyid);
                        double paidAfterClaimed = paidAmt - totalRecoveredAmt;
                        if (authHandler.round(amountdue, companyid) == 0 && authHandler.round(paidAfterClaimed, companyid) == 0) {// don't put invoices which has amount due zero and whole gst has been recovered
                            continue;
                        }
                    }

                    //For getting amountdue [PS]
                    if (gReceipt.isCashtransaction()) {
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, 0);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, 0);
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUENONNEGATIVE, 0);
                        obj.put(GoodsReceiptCMNConstants.INCASH, true);
                    } else {
                        if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, authHandler.round(amountdue,companyid), currencyid, creationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(request, authHandler.round(amountdue,companyid), currencyid, creationDate, externalCurrencyRate);
                        }
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, authHandler.round(amountdue, companyid));

                        if (booleanAged) {//Added for aged payable/receivable
                            int datefilter = (request.containsKey("datefilter") && request.get("datefilter") != null) ? Integer.parseInt(request.get("datefilter").toString()) : 0;// 0 = Invoice Due date OR 1 = Invoice date
                            Date dueDate = null;
                            if (!StringUtil.isNullOrEmpty(df.format(gReceipt.getDueDate()))) {
                                dueDate = df.parse(df.format(gReceipt.getDueDate()));
                            }
                            if (datefilter == 0) {
                                dueDate = df.parse(df.format(gReceipt.getDueDate()));
                            } else {
                                dueDate = df.parse(df.format(creationDate));
                            }

                            if (dueDate.after(oneDayBeforeCal1Date) && (dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                                amountdue1 = authHandler.round(amountdue, companyid);
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else {
                                amountdue8 = authHandler.round(amountdue, companyid);
                            }
                        }
                        obj.put(GoodsReceiptCMNConstants.AMOUNTDUENONNEGATIVE, (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    }
                    obj.put(GoodsReceiptCMNConstants.AMOUNT, authHandler.round((Double) invoiceOriginalAmt, companyid)); //actual invoice amount
                    obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, invoiceTotalAmountInBase);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE1, amountdue1);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE2, amountdue2);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE3, amountdue3);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE4, amountdue4);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE5, amountdue5);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE6, amountdue6);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE7, amountdue7);
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE8, amountdue8);
                    if (!(ignoreZero && authHandler.round(amountdue, companyid) <= 0)) {
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getGoodsReceiptsJsonForMonthlyAgedPayables : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    List exportPurchaseByVendorJasper(HttpServletRequest request, JSONArray jarr, AccountingHandlerDAO accountingHandlerDAOobj) throws ServiceException  {
        List<JasperPrint> l = new ArrayList<>();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Map<String, Object> salesInvoiceMap = new HashMap<String, Object>();
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            String companyname = company.getCompanyName() != null ? company.getCompanyName() : "";
            String companyaddress = AccountingAddressManager.getCompanyDefaultBillingAddress(companyid, accountingHandlerDAOobj);
            String companyemail = company.getEmailID() != null ? company.getEmailID() : "";
            String companyfax = company.getFaxNumber() != null ? company.getFaxNumber() : "";
            String companyphone = company.getPhoneNumber() != null ? company.getPhoneNumber() : "";
            String currencyname = company.getCurrency().getName();
            String currencyCode = company.getCurrency().getCurrencyCode();
            String fileLocalPath = request.getSession().getServletContext().getRealPath("jrxml");
            List<Map<String, Object>> subreportDataList = new ArrayList<>();
            String startDate = (String) request.getParameter(Constants.REQ_startdate);
            String endDate = (String) request.getParameter(Constants.REQ_enddate);

            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.REQ_startdate)) && !StringUtil.isNullOrEmpty(request.getParameter(Constants.REQ_enddate))) {
                Date end_Date = authHandler.getDateOnlyFormat(request).parse(request.getParameter(Constants.REQ_enddate));    //ERP-8442
                Calendar c = Calendar.getInstance();
                c.setTime(end_Date);
                //c.add(Calendar.DATE, -1);   //Subtract 1 day.
                endDate = userdf.format(c.getTime());
                startDate = userdf.format(authHandler.getDateOnlyFormat(request).parse(request.getParameter(Constants.REQ_startdate)));
            }
            // Line detail for Report
            Map<String, Object> row = Collections.EMPTY_MAP;
            for (int i = 0; i < jarr.length(); i++) {
                row = new HashMap<>();
                JSONObject jobj = jarr.getJSONObject(i);
                row.put("PersonName", jobj.optString("customername", jobj.optString("xyz", "")));
                row.put("ProductName", jobj.optString("rowproductname", ""));
                row.put("ProductID", jobj.optString("rowproductid", ""));
                row.put("DocumentNo", jobj.optString("billno", ""));
                row.put("DocumentDate", !StringUtil.isNullOrEmpty(jobj.optString("date", "")) ? userdf.format(new Date(jobj.optString("date", ""))) : "");
                row.put("Quantity", jobj.optString("rowquantity", ""));
                row.put("UnitPrice", authHandler.formattedCommaSeparatedUnitPrice(jobj.optDouble("rowrate", 0), companyid));
                row.put("UOM", jobj.optString("uom", ""));
                row.put("Description", jobj.optString("rowproductdescription", ""));
                row.put("Currency", jobj.optString("currencysymbol", ""));
                subreportDataList.add(row);
            }
            // Global Parameters

            salesInvoiceMap.put("reportName", "Purchase By Vendor");
            salesInvoiceMap.put("companyname", companyname);
            salesInvoiceMap.put("companyaddress", companyaddress);
            salesInvoiceMap.put("companyphone", companyphone);
            salesInvoiceMap.put("companyfax", companyfax);
            salesInvoiceMap.put("companyemail", companyemail);
            salesInvoiceMap.put("currencyname", currencyname);
            salesInvoiceMap.put("currencycode", currencyCode);
            salesInvoiceMap.put("rangeType", "Custom");
            salesInvoiceMap.put("fromDate", startDate);
            salesInvoiceMap.put("toDate", endDate);
            salesInvoiceMap.put("startPeriod", "");
            salesInvoiceMap.put("endPeriod", "");
            salesInvoiceMap.put("SubReportData", new JRBeanCollectionDataSource(subreportDataList));


            JasperPrint jasperPrint = null;
            JasperReport jasperReport = null;
            JasperReport jasperReportSubReport = null;
            FileInputStream inputStream = null;
            FileInputStream inputStreamSubReport = null;

            inputStream = new FileInputStream(fileLocalPath + "/SupplierPriceList.jrxml");
            inputStreamSubReport = new FileInputStream(fileLocalPath + "/SupplierPriceListSubreport.jrxml");

            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            jasperReport = JasperCompileManager.compileReport(jasperDesign);

            List salesInvoiceList = new ArrayList();
            OnlyDatePojo odp = new OnlyDatePojo();
            odp.setDate(new Date().toString());
            salesInvoiceList.add(odp);
            JasperDesign jasperDesignSubReport = JRXmlLoader.load(inputStreamSubReport);
            jasperReportSubReport = JasperCompileManager.compileReport(jasperDesignSubReport);
            salesInvoiceMap.put("SubReport", jasperReportSubReport);
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(salesInvoiceList);
            jasperPrint = JasperFillManager.fillReport(jasperReport, salesInvoiceMap, beanColDataSource);

            l.add(jasperPrint);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getGoodsReceiptsJsonForMonthlyAgedPayables : " + ex.getMessage(), ex);
        }
        return l;
    }
    /*--------- Return true If Invoice will fully linked with GR------------*/

    boolean isInvoiceFullyLinkedWithGR(GoodsReceipt invoice) throws ServiceException {
        boolean isfullyLinkedWithGR = false;
        List invoiceList = accGoodsReceiptobj.isInvoiceNotLinkedWithAnyGR(invoice);
        boolean isAnyGRNIsInPendingState = (boolean) invoiceList.get(2);//Flag to identify whether any of GRN created from PI are in pending state?
        if (!invoice.isIsOpenInGR() && !isAnyGRNIsInPendingState) {
            isfullyLinkedWithGR = true;
        } else {
            /*------------ Check If GR->PI then in this case PI is linked with Full GRN or not?---------*/
            if (accGoodsReceiptobj.isLinkingOfGRInPI(invoice)) {//i.e GR->PI linking is available?
                isfullyLinkedWithGR = checkGRNtoInvoiceLinkingIsFullOrPartial(invoice, true);
            }
            
        }
        return isfullyLinkedWithGR;
    }

    /*----------- Return true If Invoice will partially linked with GR--------------*/
    boolean isInvoicePartiallyLinkedWithGR(GoodsReceipt invoice) throws ServiceException {
        boolean isNotLinkedWithAnyGR = false;
        boolean isPartiallyLinkedWithGR = false;

        List invoiceList = accGoodsReceiptobj.isInvoiceNotLinkedWithAnyGR(invoice);
        isNotLinkedWithAnyGR = (boolean) invoiceList.get(0);
        isPartiallyLinkedWithGR = (boolean) invoiceList.get(1);
        /*--------- Check  PI->GR linked Partially-------*/
        if (!isNotLinkedWithAnyGR && invoice.isIsOpenInGR()) {
            isPartiallyLinkedWithGR = true;

        } 
        /**
         * Commenting below code because of no need check GR-->PI flow in
         * partially case.
         */
//        else {
//            /*------------ Check If GR->PI then in this case PI is linked wih Partial GRN or not?---------*/
//            if (accGoodsReceiptobj.isLinkingOfGRInPI(invoice)) {//i.e GR->PI linking is available?
//                isPartiallyLinkedWithGR = checkGRNtoInvoiceLinkingIsFullOrPartial(invoice, false);//isPartiallyLinkedWithGR->false means linking not like GR->PI but this will like PI->GR  
//            }
//        }
        return isPartiallyLinkedWithGR;

    }

    /*--------- Return true If Invoice will not linked with any GR----------*/
    boolean isInvoiceNotLinkedWithAnyGR(GoodsReceipt invoice) throws ServiceException {
        boolean isNotLinkedWithAnyGR = true;

        List invoiceList = accGoodsReceiptobj.isInvoiceNotLinkedWithAnyGR(invoice);//PI->GR
        isNotLinkedWithAnyGR = (boolean) invoiceList.get(0);
        if (isNotLinkedWithAnyGR) {
            if (accGoodsReceiptobj.isLinkingOfGRInPI(invoice)) {//i.e GR->PI linking is available?
               isNotLinkedWithAnyGR=false; 
            }
        }
        return isNotLinkedWithAnyGR;
    }

     
    /*--------- Return true/false for Partial/complete linking of GR->Invoice----------*/
    boolean checkGRNtoInvoiceLinkingIsFullOrPartial(GoodsReceipt invoice,boolean isCalledForFullGRN) throws ServiceException {
        boolean statusLinking = false;
        Set<GoodsReceiptDetail> invRows = invoice.getRows();
        double invoiceQuantity = 0;
        double deliveryOrderQuantity = 0;
        GoodsReceiptOrder grorder = null;
        /* ----Calculating product quantity used in Invoice------*/
        for (GoodsReceiptDetail invdetails : invRows) {
            if (invdetails.getGoodsReceiptOrderDetails() != null) {
                if (grorder == null) {
                    grorder = invdetails.getGoodsReceiptOrderDetails().getGrOrder();
                }
                invoiceQuantity += invdetails.getInventory().getQuantity();
            }
        }

        if (grorder != null) {
            Set<GoodsReceiptOrderDetails> grorderrows = grorder.getRows();

            /* ----Calculating product quantity used in GR------*/
            for (GoodsReceiptOrderDetails grodetails : grorderrows) {
                deliveryOrderQuantity += grodetails.getDeliveredQuantity();
            }

            if (isCalledForFullGRN) {
                /*--------- If GR is completely used in single invoice-------*/
                if (deliveryOrderQuantity >= invoiceQuantity) {
                    statusLinking = true;
                } else {
                    statusLinking = false;
                }
            } else {
                /*--------- If GR is partially used in single invoice-------*/
                if (deliveryOrderQuantity > invoiceQuantity) {
                    statusLinking = true;
                } else {
                    statusLinking = false;
                }
            }
        }

        return statusLinking;
    }

}
