/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.companypref.util.CompanyReportConfigConstants;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.CustomerAddressDetails;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.companypreferenceservice.CompanyReportConfigurationService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accAccountHandler;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.jasperreports.AgeingTableForSOA;
import com.krawler.spring.accounting.jasperreports.SOABalanceOutstandingPojo;
import com.krawler.spring.accounting.jasperreports.StatementOfAccountsSubReport;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.joda.time.DateTime;

/**
 *
 * @author krawler
 */
public class AccTemplateReportServiceImpl implements AccTemplateReportService {

    private static final Logger _logger = Logger.getLogger(AccTemplateReportServiceImpl.class.getName());
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accAccountDAO accAccountDAOobj;
    private accInvoiceCMN accInvoiceCommon;
    private accJournalEntryDAO accJournalEntryobj;
    private AccReportsService accReportsServiceobj;
    private AccReportsDAO accReportsDAO;
    private CompanyReportConfigurationService companyReportConfigurationService;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;

    public void setCompanyReportConfigurationService(CompanyReportConfigurationService companyReportConfigurationService) {
        this.companyReportConfigurationService = companyReportConfigurationService;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setAccReportsService(AccReportsService accReportsServiceobj) {
        this.accReportsServiceobj = accReportsServiceobj;
    }

    public void setaccReportsDAO(AccReportsDAO accReportsDAO) {
        this.accReportsDAO = accReportsDAO;
    }

    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }

    public JSONObject getLedgerInfo(JSONObject requestJobj, JSONObject accountJobj, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException {
        Calendar c1 = Calendar.getInstance();
        JSONObject jobj = new JSONObject();
        String companyid = requestJobj.optString(Constants.companyKey);
        List accountlist = new ArrayList();
        try {
            boolean includeExcludeChildBalances = requestJobj.optBoolean("includeExcludeChildBalances", true);
            boolean isFromExpander = requestJobj.optBoolean("isFromExpander", false);
            double total = 0;
            String accountid = "";
            String selectedCurrencyIds = requestJobj.optString("currencyIds");
            double balanceAmount = 0;
            double OpeningBalanceInAccountCurrency = 0;
            double balanceAmountAccountCurrency = 0;
            double totalAccountCurrency = 0;
            accountid = requestJobj.optString("accountid");
            boolean ledgerReportFlag = requestJobj.optBoolean("ledgerReport");
            boolean generalLedgerFlag = requestJobj.optBoolean("generalLedgerFlag");
            int accountidCount = 0;
            if (!StringUtil.isNullOrEmpty(accountid)) {
                accountidCount = accountid.split(",").length;
            }
            String selectedBalPLId = "";
            boolean isFromTledgerReport = requestJobj.optBoolean("isFromTledgerReport");
            if (isFromTledgerReport && !StringUtil.isNullOrEmpty(requestJobj.optString("balPLId"))) {
                selectedBalPLId = requestJobj.optString("balPLId");
            }
            //SDP-319 : Used appropriate date format for proper filtering
            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();
            DateFormat df = authHandler.getDateOnlyFormat();
            Date endDate = df.parse(requestJobj.optString("enddate"));
            Date startDate = df.parse(requestJobj.optString("startdate"));
            if (accountid.equalsIgnoreCase("All")) {
                HashMap<String, Object> requestParams = accAccountHandler.getJsonMap(requestJobj);
                requestParams.put("nondeleted", "true");
                requestParams.put(Constants.start, "");
                requestParams.put("limit", "");
                requestParams.put("selectedBalPLId", selectedBalPLId);
                KwlReturnObject result = accAccountDAOobj.getAccountsForCombo(requestParams);
                accountlist = result.getEntityList();
            } else {  //addd for miltiple bank accounts selection
                String AccountidGroup[] = accountid.split(",");
                for (int i = 0; i < accountidCount; i++) {
                    KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), AccountidGroup[i]);
                    Account account = (Account) accresult.getEntityList().get(0);
                    if (account != null) {
                        accountlist.add(account);
                        if (generalLedgerFlag) {
                            if (includeExcludeChildBalances) {
                                accountlist = accReportsServiceobj.getChildAccounts(accountlist, account);
                            }
                        } else {
                            accountlist = accReportsServiceobj.getChildAccounts(accountlist, account);
                        }
                    }
                }
            }

            boolean consolidateFlag = requestJobj.optBoolean("consolidateFlag");
            String gcurrencyid = requestJobj.optString(Constants.globalCurrencyKey);
            boolean eliminateflag = consolidateFlag;
            boolean excludePreviousYear = requestJobj.optBoolean("excludePreviousYear");
            Date start = new Date(1970);
            Date openBalEndDate = new DateTime(startDate).minusDays(1).toDate();  //end date used to calculate Opening Balance

            Boolean bankBook = requestJobj.optBoolean("bankBook");
            Boolean ledgerReport = requestJobj.optBoolean("ledgerReport");
            KwlReturnObject prefresult = null;
            CompanyAccountPreferences pref = null;
            String cashAccount = "";
            if (!consolidateFlag) {
                prefresult = accountingHandlerDAOobj.loadObject(CompanyAccountPreferences.class.getName(), companyid);
                pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
                cashAccount = pref.getCashAccount().getID();
            }
            HashMap<String, Object> reqParams = AccountingManager.getGlobalParamsJson(requestJobj);
            reqParams.put(Constants.REQ_startdate, requestJobj.get("startdate"));
            reqParams.put(Constants.REQ_enddate, requestJobj.get("enddate"));
            reqParams.put("dateformat", authHandler.getDateOnlyFormat());

            String Searchjson = requestJobj.optString(Constants.Acc_Search_Json);
            String filterCriteria = requestJobj.optString(Constants.Filter_Criteria);
            if (!StringUtil.isNullOrEmpty(Searchjson) && !StringUtil.isNullOrEmpty(filterCriteria)) {
                reqParams.put("isIAF", true);
                HashMap<String, Object> reqPar1 = new HashMap<String, Object>();
                reqPar1.put(Constants.companyKey, companyid);
                reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                reqPar1.put(Constants.Filter_Criteria, filterCriteria);

                reqPar1.remove(Constants.moduleid);
                reqPar1.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);

                reqPar1.remove(Constants.moduleid);
                reqPar1.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
            }

            Iterator iterator = accountlist.iterator();
            JSONArray jArr = new JSONArray();
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(requestJobj);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), gcurrencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            int count = 0;
            while (iterator.hasNext()) {
                count++;
                balanceAmount = 0;
                balanceAmountAccountCurrency = 0;   //ERP-8853
                total = 0;
                OpeningBalanceInAccountCurrency = 0;
                Object listObj = iterator.next();
                Account account = (Account) listObj;
                accountid = account.getID();
                boolean showOpeningBalanceSeprately = true;
                if (excludePreviousYear && account.getAccounttype() == 0) {//0 :- profit and loss account

                    start = accReportsServiceobj.getDateForExcludePreviousYearBalanceFilter(requestJobj, startDate);
                } else {
                    start = null;
                }

                double accountOpeningBalanceInBase = 0.0d;
                double OpeningBalanceInBaseCurrency = 0.0d;

                //other than T-Ledger reports call come in this else e.g.1)on expander click of General Ledger.2)cashbook/bank book reports.
                accountOpeningBalanceInBase = accInvoiceCommon.getOpeningBalanceOfAccountJson(requestJobj, account, false, null);
                boolean shouldAccountOpeningBalanceInclude = !accInvoiceCommon.accountHasOpeningTransactionsJson(requestJobj, account, false, null);

                if (shouldAccountOpeningBalanceInclude) {
                    OpeningBalanceInAccountCurrency = authHandler.round(account.getOpeningBalance(), companyid);
                } else {
                    String accountcurrencyid = account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID();
                    KwlReturnObject crresult11 = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, accountOpeningBalanceInBase, accountcurrencyid, account.getCreationDate(), 0);
                    OpeningBalanceInAccountCurrency = (Double) crresult11.getEntityList().get(0);
                    OpeningBalanceInAccountCurrency = authHandler.round(OpeningBalanceInAccountCurrency, companyid);
                }

                if (bankBook) {
                    balanceAmountAccountCurrency = 0;
                }
                OpeningBalanceInBaseCurrency = accountOpeningBalanceInBase;

                if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.Acc_Search_Json))) { // ERP-11394 reset opening balance in case of advance serach
                    accountOpeningBalanceInBase = 0;
                    OpeningBalanceInBaseCurrency = 0;
                    OpeningBalanceInAccountCurrency = 0;
                }

                //BUG Fixed #16739 : Creation date check
                Date createdOn = AccountingManager.resetTimeField(account.getCreationDate());
//                Date toDate = AccountingManager.resetTimeField(endDate);
//                if (toDate.compareTo(createdOn) < 0) {
//                    jobj.put(Constants.RES_data, new JSONArray()); //Return Empty Data
//                    return jobj;
//                }

                String entryChar = "c", emptyChar = "d";
                double balance = 0;
                double balanceAccountCurrency = 0;

                requestParams.put("costcenter", requestJobj.optString("costcenter"));
                requestParams.put(Constants.Acc_Search_Json, requestJobj.optString(Constants.Acc_Search_Json));
                requestParams.put(Constants.Filter_Criteria, requestJobj.optString(InvoiceConstants.Filter_Criteria));

                if (!generalLedgerFlag) {

                    balance = accReportsServiceobj.getAccountBalanceMerged(requestJobj, requestParams, accountid, null, openBalEndDate, eliminateflag);
                }
                String mappedAccIDs = accountid + ",";
                if (consolidateFlag && !generalLedgerFlag) {//Fetch mapped account's balances.
                    List mapaccresult = accAccountDAOobj.getMappedAccountsForReports(accountid);
                    Iterator<Object[]> itr1 = mapaccresult.iterator();
                    String mappedaccountid = "";
                    while (itr1.hasNext()) {
                        Object[] row = (Object[]) itr1.next();
                        mappedaccountid = row[0].toString();
                        mappedAccIDs += mappedaccountid + ",";
                        KwlReturnObject childObj = accountingHandlerDAOobj.loadObject(Account.class.getName(), mappedaccountid);
                        Account child = (Account) childObj.getEntityList().get(0);
                        requestJobj.put(Constants.companyKey, child.getCompany().getCompanyID());

                        balance += accReportsServiceobj.getAccountBalanceMerged(requestJobj, requestParams, mappedaccountid, excludePreviousYear ? start : null, openBalEndDate, eliminateflag);
                    }
                }
                if (balance != 0) {

                    requestParams.put("tocurrencyid", requestJobj.optString("tocurrencyid"));
                    requestParams.put("templatecode", requestJobj.optInt("templatecode", -1));
                    if (!StringUtil.isNullOrEmpty(selectedCurrencyIds)) {
                        requestParams.put("currencyFlag", true);
                        requestParams.put("selectedCurrencyIds", selectedCurrencyIds);
                    }

                    if (!generalLedgerFlag) {
                        balanceAccountCurrency = accReportsServiceobj.getAccountBalanceInOriginalCurrency(requestJobj, requestParams, accountid, excludePreviousYear ? start : null, openBalEndDate);
                        mappedAccIDs = accountid + ",";
                        if (consolidateFlag) {//Fetch mapped account's balances.
                            List mapaccresult = accAccountDAOobj.getMappedAccountsForReports(accountid);
                            Iterator<Object[]> itr1 = mapaccresult.iterator();
                            String mappedaccountid = "";
                            while (itr1.hasNext()) {
                                Object[] row = (Object[]) itr1.next();
                                mappedaccountid = row[0].toString();
                                mappedAccIDs += mappedaccountid + ",";
                                KwlReturnObject childObj = accountingHandlerDAOobj.loadObject(Account.class.getName(), mappedaccountid);
                                Account child = (Account) childObj.getEntityList().get(0);
                                requestJobj.put(Constants.companyKey, child.getCompany().getCompanyID());
                                balanceAccountCurrency += accReportsServiceobj.getAccountBalanceInOriginalCurrency(requestJobj, requestParams, mappedaccountid, excludePreviousYear ? start : null, openBalEndDate);
                            }
                        }
                    }
                }
                Date sDate = AccountingManager.resetTimeField(startDate);
                if (sDate.after(createdOn)) {
                    balance += accountOpeningBalanceInBase;
                    balanceAccountCurrency += OpeningBalanceInAccountCurrency;
                    showOpeningBalanceSeprately = false;
                }
                if (balance != 0 || balanceAccountCurrency != 0) {
                    if (balance > 0) {
                        entryChar = "d";
                        emptyChar = "c";
                    } else {
                        entryChar = "c";
                        emptyChar = "d";
                    }
                    JSONObject objlast = new JSONObject();
                    objlast.put(entryChar + "_date", authHandler.getDateOnlyFormat().format(startDate));
                    objlast.put(entryChar + "_accountname", "Balance b/d");
                    objlast.put(entryChar + "_acccode", "");
                    objlast.put(entryChar + "_journalentryid", "");
                    objlast.put(entryChar + "_transactionID", "");
                    objlast.put(entryChar + "_transactionDetails", "");
                    objlast.put(entryChar + "_transactionDetailsForExpander", "");
                    objlast.put(entryChar + "_transactionDetailsBankBook", "");
                    objlast.put(entryChar + "_checkno", "");
                    objlast.put(entryChar + "_description", "");
                    objlast.put(entryChar + "_amount", Math.abs(balance));
                    objlast.put(emptyChar + "_date", "");
                    objlast.put(emptyChar + "_accountname", "");
                    objlast.put(emptyChar + "_journalentryid", "");
                    objlast.put(emptyChar + "_amount", "");
                    objlast.put(emptyChar + "_transactionID", "");
                    objlast.put(emptyChar + "_transactionDetails", "");
                    objlast.put(emptyChar + "_transactionDetailsForExpander", "");
                    objlast.put(emptyChar + "_transactionDetailsBankBook", "");
                    objlast.put(emptyChar + "_checkno", "");
                    objlast.put(emptyChar + "_description", "");
                    objlast.put("accountid", accountid);
                    objlast.put("currencysymbol", account.getCurrency() == null ? currency.getSymbol() : account.getCurrency().getSymbol());
                    objlast.put("currencycode", account.getCurrency() == null ? currency.getCurrencyCode() : account.getCurrency().getCurrencyCode());
                    String accountname = StringUtil.isNullOrEmpty(account.getName()) ? (!StringUtil.isNullOrEmpty(account.getAcccode()) ? account.getAcccode() : "") : account.getName();
                    objlast.put("accountname", accountname);
                    objlast.put("accountcode", StringUtil.isNullOrEmpty(account.getAcccode()) ? "" : account.getAcccode());
                    String acccode = StringUtil.isNullOrEmpty(account.getAcccode()) ? "" : account.getAcccode();
                    objlast.put("accCode", acccode);
                    objlast.put(Constants.currencyKey, account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID());

                    // this is for displaying on the group header
                    if (!acccode.equals("") && account.getName().equals("")) {
                        objlast.put("accCodeName", acccode);
                    } else if (!acccode.equals("") && !account.getName().equals("")) {
                        objlast.put("accCodeName", acccode + " - " + account.getName());
                    } else {
                        objlast.put("accCodeName", accountname);
                    }

                    if (entryChar.equals("d")) {
                        balanceAmount = balanceAmount + Math.abs(balance);
                        objlast.put("balanceAmount", authHandler.round(balanceAmount, companyid));
                    } else if (entryChar.equals("c")) {
                        balanceAmount = balanceAmount - Math.abs(balance);
                        objlast.put("balanceAmount", authHandler.round(balanceAmount, companyid));
                    }

                    if (balanceAccountCurrency > 0) {
                        entryChar = "d";
                        emptyChar = "c";
                    } else {
                        entryChar = "c";
                        emptyChar = "d";
                    }

                    if (entryChar.equals("d")) {
                        balanceAmountAccountCurrency = balanceAmountAccountCurrency + Math.abs(balanceAccountCurrency);
                        objlast.put("balanceAmountAccountCurrency", authHandler.round(balanceAmountAccountCurrency, companyid));
                    } else if (entryChar.equals("c")) {
                        balanceAmountAccountCurrency = balanceAmountAccountCurrency - Math.abs(balanceAccountCurrency);
                        objlast.put("balanceAmountAccountCurrency", authHandler.round(balanceAmountAccountCurrency, companyid));
                    }
                    objlast.put(entryChar + "_amountAccountCurrency", Math.abs(balanceAccountCurrency));
                    jArr.put(objlast);

                    if (requestJobj.optString("filetype") != null) {
                        if (requestJobj.optString("filetype").equals("print") || requestJobj.optString("filetype").equals("csv")) {
                            if (emptyChar == "d") {
                                total = total + Math.abs(balance);
                                totalAccountCurrency = totalAccountCurrency + Math.abs(balanceAccountCurrency);
                            }
                        }
                    }
                }

                if (showOpeningBalanceSeprately) {//shows opening balance if date from in account ceration date
                    if (OpeningBalanceInAccountCurrency != 0) {
                        if (OpeningBalanceInAccountCurrency > 0) {
                            entryChar = "d";
                            emptyChar = "c";
                            balance += OpeningBalanceInBaseCurrency;
                            balanceAccountCurrency += OpeningBalanceInAccountCurrency;
                        } else {
                            balance += OpeningBalanceInBaseCurrency;
                            balanceAccountCurrency += OpeningBalanceInAccountCurrency;
                            entryChar = "c";
                            emptyChar = "d";
                        }

                        JSONObject objlast = new JSONObject();
                        objlast.put(entryChar + "_date", authHandler.getDateOnlyFormat().format(startDate));
                        objlast.put(entryChar + "_accountname", "Opening Balance");
                        objlast.put(entryChar + "_acccode", "");
                        objlast.put(entryChar + "_journalentryid", "");
                        objlast.put(entryChar + "_amount", Math.abs(OpeningBalanceInBaseCurrency));
                        objlast.put(entryChar + "_amountAccountCurrency", Math.abs(OpeningBalanceInAccountCurrency));
                        objlast.put(entryChar + "_transactionID", "");
                        objlast.put(entryChar + "_transactionDetails", "");
                        objlast.put(entryChar + "_transactionDetailsForExpander", "");
                        objlast.put(entryChar + "_transactionDetailsBankBook", "");
                        objlast.put(entryChar + "_checkno", "");
                        objlast.put(entryChar + "_description", "");
                        objlast.put(emptyChar + "_date", "");
                        objlast.put(emptyChar + "_accountname", "");
                        objlast.put(emptyChar + "_journalentryid", "");
                        objlast.put(emptyChar + "_amount", "");
                        objlast.put(emptyChar + "_transactionID", "");
                        objlast.put(emptyChar + "_transactionDetails", "");
                        objlast.put(emptyChar + "_transactionDetailsForExpander", "");
                        objlast.put(emptyChar + "_transactionDetailsBankBook", "");
                        objlast.put(emptyChar + "_checkno", "");
                        objlast.put(emptyChar + "_description", "");
                        objlast.put("accountid", accountid);
                        objlast.put("currencysymbol", (account.getCurrency() == null ? currency.getSymbol() : account.getCurrency().getSymbol()));
                        objlast.put("currencycode", (account.getCurrency() == null ? currency.getCurrencyCode() : account.getCurrency().getCurrencyCode()));
                        String accountname = StringUtil.isNullOrEmpty(account.getName()) ? (!StringUtil.isNullOrEmpty(account.getAcccode()) ? account.getAcccode() : "") : account.getName();
                        objlast.put("accountname", accountname);
                        objlast.put("accountcode", StringUtil.isNullOrEmpty(account.getAcccode()) ? "" : account.getAcccode());
                        objlast.put("accountgroupname", StringUtil.isNullOrEmpty(account.getGroup() != null ? account.getGroup().getName() : "") ? "" : account.getGroup() != null ? account.getGroup().getName() : "");
                        String acccode = StringUtil.isNullOrEmpty(account.getAcccode()) ? "" : account.getAcccode();
                        objlast.put("accCode", acccode);
                        objlast.put(Constants.currencyKey, account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID());

                        // this is for displaying on the group header
                        if (!acccode.equals("") && account.getName().equals("")) {
                            objlast.put("accCodeName", acccode);
                        } else if (!acccode.equals("") && !account.getName().equals("")) {
                            objlast.put("accCodeName", acccode + " - " + account.getName());
                        } else {
                            objlast.put("accCodeName", accountname);
                        }

                        if (entryChar.equals("d")) {
                            balanceAmount = balanceAmount + Math.abs(OpeningBalanceInBaseCurrency);// work on home currency
                            objlast.put("balanceAmount", authHandler.round(balanceAmount, companyid));
                            balanceAmountAccountCurrency = balanceAmountAccountCurrency + Math.abs(OpeningBalanceInAccountCurrency);// work on home currency
                            objlast.put("balanceAmountAccountCurrency", authHandler.round(balanceAmountAccountCurrency, companyid));
                        } else if (entryChar.equals("c")) {
                            balanceAmount = balanceAmount - Math.abs(OpeningBalanceInBaseCurrency);
                            objlast.put("balanceAmount", authHandler.round(balanceAmount, companyid));
                            balanceAmountAccountCurrency = balanceAmountAccountCurrency - Math.abs(OpeningBalanceInAccountCurrency);
                            objlast.put("balanceAmountAccountCurrency", authHandler.round(balanceAmountAccountCurrency, companyid));
                        }
                        jArr.put(objlast);

                    }
                }
                if (!ledgerReportFlag && !generalLedgerFlag) {//we saves balance brod down or Opening Balance to avoid adding to period balance
                    balance = 0.00;
                    balanceAccountCurrency = 0.00;
                }
                String searchJson = requestJobj.optString(Constants.Acc_Search_Json);
                if (!StringUtil.isNullOrEmpty(searchJson)) {//This is used for to Get different dimension entry from dimension name for diffrent modules
                    searchJson = accJournalEntryobj.getJsornStringForSearch(searchJson, mappedAccIDs.split(",")[0], null);
                }

                Map<String, Object> requestParams1 = new HashMap<String, Object>();
                requestParams1.put("accountid", accountid);
//                _logger.info("account-> "+accountid);
                requestParams1.put("companyid", companyid);
                DateFormat sqldf = new SimpleDateFormat("yyyy-MM-dd");
                String sqlStartDate = sqldf.format(startDate);
                String sqlEndDate = sqldf.format(endDate);
                requestParams1.put("startdate", sqlStartDate);
                requestParams1.put("enddate", sqlEndDate);
                String userSessionId = requestJobj.getString(Constants.userSessionId);
                requestParams1.put(Constants.userSessionId, userSessionId);

                requestParams1.put("othersselect", requestJobj.getString("othersselect"));
                requestParams1.put("cnselect", requestJobj.getString("cnselect"));
                requestParams1.put("invoicedetailsselect", requestJobj.getString("invoicedetailsselect"));
                requestParams1.put("invoicetermselect", requestJobj.getString("invoicetermselect"));
                requestParams1.put("invoiceroundingselect", requestJobj.getString("invoiceroundingselect"));
                requestParams1.put("invoicecapselect", requestJobj.getString("invoicecapselect"));
                requestParams1.put("grexpdetailsselect", requestJobj.getString("grexpdetailsselect"));
                requestParams1.put("grdetailsselect", requestJobj.getString("grdetailsselect"));
                requestParams1.put("grtermselect", requestJobj.getString("grtermselect"));
                requestParams1.put("grroundingselect", requestJobj.getString("grroundingselect"));
                requestParams1.put("grcapselect", requestJobj.getString("grcapselect"));
                requestParams1.put("dnselect", requestJobj.getString("dnselect"));
                requestParams1.put("receiptselect", requestJobj.getString("receiptselect"));
                requestParams1.put("receiptwoselect", requestJobj.getString("receiptwoselect"));
                requestParams1.put("paymentselect", requestJobj.getString("paymentselect"));
                requestParams1.put("jeinvrevalselect", requestJobj.getString("jeinvrevalselect"));
                requestParams1.put("jegrrevalselect", requestJobj.getString("jegrrevalselect"));
                requestParams1.put("saselect", requestJobj.getString("saselect"));
                requestParams1.put("prselect", requestJobj.getString("prselect"));
                requestParams1.put("srselect", requestJobj.getString("srselect"));
                requestParams1.put("doselect", requestJobj.getString("doselect"));
                requestParams1.put("groselect", requestJobj.getString("groselect"));
                requestParams1.put("filterConjuctionCriteria", requestJobj.optString(InvoiceConstants.Filter_Criteria));
                requestParams1.put("searchjson", searchJson);
                requestParams1.put("advSearchAttributes", advSearchAttributes);
                
                requestParams1.put("selectedCurrencyIds", selectedCurrencyIds);

                KwlReturnObject glResult = accReportsDAO.getLedgerInfo(requestParams1);
                List glList = glResult.getEntityList();
                int defaultAttrLength = requestJobj.getInt("defaultattrlength");
                int extraAttrFirstIndex = defaultAttrLength;
                int viewableAttrFirstIndex = defaultAttrLength + 4;
                Map params = new HashMap();
                params.put("filetype",requestJobj.optString("filetype"));
                JSONObject attrobject = companyReportConfigurationService.getExportConfigData(companyid, Constants.COMPANY_REPORT_CONFIG_GL, true,isFromExpander,params);
//                JSONArray attrArray = companyReportConfigurationService.getExportConfigData(companyid, Constants.COMPANY_REPORT_CONFIG_GL, Constants.globalFields, true);
                JSONArray attrArray = attrobject.getJSONArray(Constants.RES_data);
//                JSONArray attrArray = new JSONArray("[{\"field\":\"d_date\",\"title\":\"Date\",\"isvisible\":true},{\"field\":\"acc_doublemovement\",\"title\":\"Double Entry Movement\",\"isvisible\":true},{\"field\":\"entryno\",\"title\":\"Journal Folio(J/F)\",\"isvisible\":true},{\"field\":\"description\",\"title\":\"Description\",\"isderived\":true,\"isvisible\":true},{\"field\":\"exchangeratefortransaction\",\"title\":\"Exchange Rate (SGD)\",\"isvisible\":true},{\"field\":\"d_transactionAmount\",\"title\":\"Debit Amount\",\"isvisible\":true},{\"field\":\"d_amount\",\"title\":\"Debit Amount in Base Currency\",\"isvisible\":true},{\"field\":\"c_transactionAmount\",\"title\":\"Credit Amount\",\"isvisible\":true},{\"field\":\"c_amount\",\"title\":\"Credit Amount in Base Currency\",\"isvisible\":true}]");                
                double balAmt = accountJobj.optDouble("openingamount",0.0);  //SDP-12467
                if (isFromExpander) {
                    balAmt = requestJobj.optDouble("accountopeningamount", 0.0);
                }
                double c_sumamount = 0, d_sumamount = 0;
                if (glList != null && !glList.isEmpty()) {
                    for (int i = 0; i < glList.size(); i++) {

                        Object[] details = (Object[]) glList.get(i);

                        JSONObject obj = new JSONObject();

                        if (consolidateFlag) {
                            requestJobj.put(Constants.companyKey, companyid);
                            requestParams.put(Constants.companyKey, companyid);
                            requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
                        }

                        obj.put(entryChar + "_journalentryid", details[3] == null ? "" : details[3].toString());
                        String billid = details[15] == null ? "" : details[15].toString();
                        String txnCurrSymbol = details[17] == null ? "" : details[17].toString();
                        // Showing base currency as transaction currency in case of reevaluation journal entry.
                        String isRevalJE = details[4] == null ? "" : details[4].toString();
                        if (isRevalJE.trim().equals("1")) {
                            txnCurrSymbol = currency.getSymbol();
                        }
                        obj.put("billid", billid);
                        obj.put("noteid", billid);
                        obj.put("txnCurrSymbol", txnCurrSymbol);
                        String accountname = StringUtil.isNullOrEmpty(account.getName()) ? (!StringUtil.isNullOrEmpty(account.getAcccode()) ? account.getAcccode() : "") : account.getName();
                        obj.put("accountname", accountname);
                        obj.put("accountcode", StringUtil.isNullOrEmpty(account.getAcccode()) ? "" : account.getAcccode());
                        obj.put("accountgroupname", StringUtil.isNullOrEmpty(account.getGroup() != null ? account.getGroup().getName() : "") ? "" : account.getGroup() != null ? account.getGroup().getName() : "");
                        String debit = details[2] == null ? "" : details[2].toString();
                        if (!StringUtil.isNullOrEmpty(debit) && debit.equals("T")) {
                            entryChar = "d";
                            emptyChar = "c";
                        } else {
                            entryChar = "c";
                            emptyChar = "d";
                        }

                        JSONObject tempObj = null;
                        int initialIndex = 1;
//                        obj.put("accountcode", account.getAcccode());

                        for (int j = 1; j <= attrArray.length(); j++) {
                            tempObj = attrArray.getJSONObject(j - 1);
                            if (!tempObj.optString("groupinfo", "").equals("GROUP")) {
                                if (tempObj.getString("header").equals("description") || tempObj.getString("header").equals("line_description")) {
                                    String description = details[viewableAttrFirstIndex + initialIndex] == null ? "" : details[viewableAttrFirstIndex + initialIndex].toString();
                                    description = description.replace("&nbsp;", "");
                                    if(!requestJobj.optString("filetype", "").equalsIgnoreCase("detailedPDF")){
                                        description = description.replace("<br>", " ");
                                    }
                                    obj.put(tempObj.getString("header"), description);
                                } else if (tempObj.getString("header").equals("d_date")) {
                                    String entryDateStr = details[viewableAttrFirstIndex + initialIndex] == null ? null : details[viewableAttrFirstIndex + initialIndex].toString();
                                    if (!StringUtil.isNullOrEmpty(entryDateStr)) {
                                        obj.put(entryChar + "_date", authHandler.getDateOnlyFormat().format(sqldf.parse(entryDateStr)));
                                    } else {
                                        obj.put(entryChar + "_date", "");
                                    }
                                    obj.put(emptyChar + "_date", "");
                                } else if (tempObj.optString("groupinfo", "").equals("CALCULATED") && details[viewableAttrFirstIndex + initialIndex] != null) {
                                    String value = (String) details[viewableAttrFirstIndex + initialIndex];
                                    if (!StringUtil.isNullOrEmpty(value) && value.equals("$$CALCULATED_OPENING$$")) {
                                        double camtbase = obj.optDouble("c_amount");
                                        double damtbase = obj.optDouble("d_amount");
                                        balAmt += damtbase - camtbase;
                                        balAmt = authHandler.round(balAmt, companyid);
                                        obj.put(tempObj.getString("header"), balAmt);

                                    }
                                } else {
                                        obj.put(tempObj.getString("header"), details[viewableAttrFirstIndex + initialIndex] == null ? "" : details[viewableAttrFirstIndex + initialIndex]);
                                    }
                                initialIndex++;
                            }

                        }
                        
                        // Showing base currency as transaction currency in case of reevaluation journal entry.
                        if (isRevalJE.trim().equals("1")) {
                            obj.put("txncurrency", currency.getCurrencyCode());
                        }
                        double camtbase = authHandler.round(obj.optDouble("c_amount"), companyid);
                        double damtbase = authHandler.round(obj.optDouble("d_amount"), companyid);
                        double camt = authHandler.round(obj.optDouble("c_transactionAmount"), companyid);
                        double damt = authHandler.round(obj.optDouble("d_transactionAmount"), companyid);
                        obj.put("c_amount", camtbase);
                        obj.put("d_amount", damtbase);
                        obj.put("c_transactionAmount", camt);
                        obj.put("d_transactionAmount", damt);
                        c_sumamount += camtbase;
                        d_sumamount += damtbase;
                        jArr.put(obj);
                    }
                }
//              System.out.println(jArr.toString());
                jobj.put("c_sumamout", c_sumamount);
                jobj.put("d_sumamout", d_sumamount);
            }
            jobj.put(Constants.RES_data, jArr);
            Calendar c2 = Calendar.getInstance();
            _logger.log(Level.FINE, "time taken for " + accountid + " -> " + (c2.getTimeInMillis() - c1.getTimeInMillis()) / 1000);
        } catch (ParseException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getLedger : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getLedger : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getLedger : " + ex.getMessage(), ex);
        }

        return jobj;
    }

    public JSONArray getSOAInfo(JSONObject requestJobj) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        String companyid = requestJobj.getString(Constants.companyKey);
        requestJobj = companyReportConfigurationService.pouplateSelectStatementForSOA(requestJobj, requestJobj.getString(Constants.companyKey));
         List list = accReportsDAO.getSOAInfo(requestJobj);

        boolean isOutstanding = !requestJobj.optBoolean("invoiceAmountDueFilter");
        if (list != null && !list.isEmpty()) {
            DateFormat sqlDF = new SimpleDateFormat("yyyy-MM-dd");
            JSONArray afterDatejArr = new JSONArray();
            JSONArray beforeDatejArr = new JSONArray();
            String accInfo = "";
            Date transactionDate = null;
            Date transactionDateNew = null;
            Date startDate = null;
            double balanceAmtInBase = 0;
            double balanceAmount = 0;
            String prevCustomer = "";
            String invoiceCustomerId = "";
            String type = "";
            String transDate = "";
            try {
                startDate = sqlDF.parse(requestJobj.getString("startdateStr"));
                HashMap<String,Object> paramsMap=new HashMap<>();
                paramsMap.put(Constants.companyid, companyid);
                paramsMap.put(Constants.REPORT_TYPE, Constants.COMPANY_REPORT_CONFIG_SOA);
                paramsMap.put("type", Constants.globalFields);
                paramsMap.put("onlyVisible", false);
                JSONObject propertiesObject = companyReportConfigurationService.getTypeFormatByReportType(paramsMap);
                JSONArray propertiesArray = propertiesObject.getJSONArray("data");
                Map<String, String> allAccountMap = new HashMap<String, String>();

                for (int i = 0; i < list.size(); i++) {
                    Object[] details = (Object[]) list.get(i);
                    JSONObject detailObj = new JSONObject();
                    JSONObject tempObj = null;
                    for (int j = 0; j < propertiesArray.length(); j++) {
                        tempObj = propertiesArray.getJSONObject(j);
                        if (tempObj.optString("format", "").equals("number") && details[j] != null && !details[j].toString().trim().isEmpty()) {
                            detailObj.put(tempObj.getString("header"), authHandler.round(Double.parseDouble(details[j].toString()), companyid));
                        } else {
                            detailObj.put(tempObj.getString("header"), details[j]);
                        }
                    }

                    if(StringUtil.isNullOrEmpty(detailObj.optString("billid"))){
                        continue;
                    }
                    
                    invoiceCustomerId = detailObj.optString("accName", "");
                    type = detailObj.optString("type", "");
                    detailObj.put("categoryName", detailObj.getString("accName"));
                    String sortPersonInfo = detailObj.getString("accName") + "-" + detailObj.getString("accId");
                    detailObj.put("sortpersoninfo", sortPersonInfo);
                    transactionDate = sqlDF.parse(detailObj.getString("jeEntryDate"));
                    if (type.equals("Debit Note") || type.equals("Credit Note")) {
                        detailObj.put("noteid", detailObj.getString(Constants.billid));//Unable to view document from SOA
                    }
                    
                    if (isOutstanding) {
                        if(!StringUtil.isNullOrEmpty(type) && type.equals(CompanyReportConfigConstants.CASH_SALE)){
                            continue;
                        }
                        double knockOffAmtBase = detailObj.isNull("knockOffAmountInBase") ? 0 : detailObj.getDouble("knockOffAmountInBase");
                        double knockOffAmt = detailObj.isNull("knockOffAmount") ? 0 : detailObj.getDouble("knockOffAmount");
                        double amtBase = 0;
                        double amt = 0;
                        if (detailObj.has("creditAmountInBase") && !(detailObj.optDouble("creditAmountInBase",0) == 0)) {
                            amtBase = authHandler.round(detailObj.getDouble("creditAmountInBase"),companyid);
                            amt = authHandler.round(detailObj.getDouble("creditAmount"),companyid);

                            if (amtBase - knockOffAmtBase == 0) {
                                continue;
                            } else {
                                detailObj.put("creditAmountInBase", amtBase - knockOffAmtBase);
                                detailObj.put("creditAmount", amt - knockOffAmt);
                                detailObj.put("transactionAmountInBase", amtBase - knockOffAmtBase);
                                detailObj.put("documentStatus", "Open");
                            }
                        } else {
                            
                            amtBase = authHandler.round(detailObj.getDouble("debitAmountInBase"),companyid);
                            amt = authHandler.round(detailObj.getDouble("debitAmount"),companyid);
                           
                            if (amtBase - knockOffAmtBase == 0) {
                                continue;
                            } else {
                                detailObj.put("debitAmountInBase", amtBase - knockOffAmtBase);
                                detailObj.put("debitAmount", amt - knockOffAmt);
                                detailObj.put("transactionAmountInBase", amtBase - knockOffAmtBase);
                                detailObj.put("documentStatus", "Open");
                            }
                        }

                        
                    }else{

                        /*-----------Code for calculating Document status as per amountdue consumed as "as of date" -------------*/
                        detailObj.put("companyidToGetDocumentStatus", companyid);
                        detailObj.put("documentStatus", getDocumentStatusForCustomerAccountStatement(detailObj));
                        
                    }
                                        
                        if (detailObj.getDouble("transactionAmountInBase") <= 0.0) {
                            continue;
                        }
                    
                    if (transactionDate.before(startDate)) {
                        beforeDatejArr.put(detailObj);
                    } else {
                        afterDatejArr.put(detailObj);
                    }

                    accInfo = detailObj.optString("accName", "") + "~" + detailObj.optString("accId", "") + "~" + detailObj.optString("accCode", "");

                    allAccountMap.put(sortPersonInfo, accInfo);
                }
                Map<String, JSONObject> accruedObjMap = calculateAccruedBalance(allAccountMap, beforeDatejArr, companyid);

                Map<String, JSONArray> transactionJSONMap = getBalnceAmountInBase(afterDatejArr, companyid, accruedObjMap);
                Set<String> customerSet = new TreeSet<String>();
                customerSet.addAll(transactionJSONMap.keySet());
                customerSet.addAll(accruedObjMap.keySet());
                for (String customer : customerSet) {
                    if (accruedObjMap.containsKey(customer)) {
                        jArr.put(accruedObjMap.get(customer));
                    }
                    if (transactionJSONMap.containsKey(customer)) {
                        JSONArray tmpArr = transactionJSONMap.get(customer);
                        for (int i = 0; i < tmpArr.length(); i++) {
                            jArr.put(tmpArr.getJSONObject(i));
                        }
                    }

                }
            } catch (ParseException ex) {
                Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jArr;
    }
    
    /*--------Get Document status as per amountdue remaining on "as of date" ---------- */
    public String getDocumentStatusForCustomerAccountStatement(JSONObject detailObj) throws JSONException {

        String documentStatus = "Open";
        String companyid = detailObj.optString("companyidToGetDocumentStatus");

        double knockOffAmtBase = detailObj.isNull("knockOffAmountInBase") ? 0 : detailObj.getDouble("knockOffAmountInBase");
        double amtBase = 0;

        if (detailObj.has("creditAmountInBase") && !(detailObj.optDouble("creditAmountInBase", 0) == 0)) {
            amtBase = authHandler.round(detailObj.getDouble("creditAmountInBase"), companyid);

            if (amtBase - knockOffAmtBase == 0) {
                documentStatus = "Closed";
            }
        } else {
            amtBase = authHandler.round(detailObj.getDouble("debitAmountInBase"), companyid);

            if (amtBase - knockOffAmtBase == 0) {
                documentStatus = "Closed";
            }
        }

        return documentStatus;
    }

    private Map<String, JSONArray> getBalnceAmountInBase(JSONArray JArr, String companyid, Map<String, JSONObject> accruedObjMap) throws JSONException {

        Map<String, JSONArray> transactionJSONMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(JArr, "sortpersoninfo");

        double balanceAmtInBase = 0;
        String type = "";
        for (String key : transactionJSONMap.keySet()) {
            JSONArray tempArr = transactionJSONMap.get(key);
            JSONObject accuredObj = accruedObjMap.get(key);
            if (accuredObj != null) {
                balanceAmtInBase = accuredObj.optDouble("balanceAmountInBase", 0);
            }

            if (tempArr != null && tempArr.length() > 0) {
                for (int i = 0; i < tempArr.length(); i++) {
                    JSONObject tempObj = tempArr.getJSONObject(i);

                    type = tempObj.optString("type", "");

                    if (type.equalsIgnoreCase("Cash Sale")) {
                        //debit type account
                        tempObj.put("balanceAmountInBase", authHandler.round(balanceAmtInBase, companyid));

                    } else if (type.equalsIgnoreCase("Customer Invoice") || type.equalsIgnoreCase("Sales Invoice") || type.equalsIgnoreCase("Payment Made") || type.equalsIgnoreCase("Debit Note")) {

                        balanceAmtInBase = balanceAmtInBase + tempObj.optDouble("debitAmountInBase", 0);
                        tempObj.put("balanceAmountInBase", authHandler.round(balanceAmtInBase, companyid));

                    } else {//Credit type account

                        balanceAmtInBase = balanceAmtInBase - tempObj.optDouble("creditAmountInBase", 0);
                        balanceAmtInBase = balanceAmtInBase + tempObj.optDouble("debitAmountInBase", 0);
                        tempObj.put("balanceAmountInBase", authHandler.round(balanceAmtInBase, companyid));

                    }
                    tempArr.put(i, tempObj);
                }
            }

            transactionJSONMap.put(key, tempArr);

        }

        return transactionJSONMap;

    }

    private Map<String, JSONObject> calculateAccruedBalance(Map<String, String> allAccountMap, JSONArray beforeDateJArr, String companyid) throws JSONException {
        Map<String, JSONObject> accObjMap = new HashMap<String, JSONObject>();
        Map<String, JSONArray> accJSONMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(beforeDateJArr, "sortpersoninfo");
        for (String key : accJSONMap.keySet()) {
            double totalOpeningInBase = 0;
            JSONArray tempArr = accJSONMap.get(key);
            if (tempArr != null && tempArr.length() > 0) {
                for (int i = 0; i < tempArr.length(); i++) {
                    JSONObject tempObj = tempArr.getJSONObject(i);
                    double amountinbase = tempObj.getDouble("transactionAmountInBase");
                    String type = tempObj.getString("type");
                    if (type.equalsIgnoreCase("Cash Sale") || type.equalsIgnoreCase("Customer Invoice") || type.equalsIgnoreCase("Sales Invoice") || type.equalsIgnoreCase("Payment Made") || type.equalsIgnoreCase("Debit Note")) {
                        //debit type account
                        totalOpeningInBase += amountinbase;
                    } else if (type.equalsIgnoreCase(Constants.DISHONOURED_RECEIVE_PAYMENT)) {
                        totalOpeningInBase += tempObj.getDouble("debitAmountInBase");
                        totalOpeningInBase -= tempObj.getDouble("creditAmountInBase");
                    } else if (type.equalsIgnoreCase(Constants.DISHONOURED_MAKE_PAYMENT)) {
                            totalOpeningInBase -= tempObj.getDouble("debitAmountInBase");
                            totalOpeningInBase += tempObj.getDouble("creditAmountInBase");
                    } else {//Credit type account
                        totalOpeningInBase -= amountinbase;
                    }
                }
                JSONObject openingJSON = new JSONObject();
                openingJSON.put("accName", tempArr.getJSONObject(0).getString("accName"));
                openingJSON.put("accId", tempArr.getJSONObject(0).getString("accId"));
                openingJSON.put("accCode", tempArr.getJSONObject(0).getString("accCode"));
                openingJSON.put("categoryName", openingJSON.get("accName"));
                openingJSON.put("jeEntryExternalCurrencyRate", 1);
                
                openingJSON.put("type", "Accrued Balance");
                openingJSON.put("debitAmount", "");
                openingJSON.put("creditAmount", "");
                openingJSON.put("balanceAmount", "");
                openingJSON.put("balanceAmountInBase", authHandler.round(totalOpeningInBase, companyid));
                if (totalOpeningInBase == 0) {//Nor Debit nor Credit
                    openingJSON.put("debitAmountInBase", 0);
                    openingJSON.put("creditAmountInBase", 0);
                } else if (totalOpeningInBase > 0) {// Debit
                    openingJSON.put("debitAmountInBase", Math.abs(authHandler.round(totalOpeningInBase, companyid)));
                    openingJSON.put("creditAmountInBase", "");
                } else { // Credit
                    openingJSON.put("debitAmountInBase", "");
                    openingJSON.put("creditAmountInBase", Math.abs(authHandler.round(totalOpeningInBase, companyid)));
                }
                accObjMap.put(key, openingJSON);
            }
        }
        Map<String, Object> mapDiff = Maps.difference(allAccountMap, accJSONMap).entriesOnlyOnLeft();
        for (String key : mapDiff.keySet()) {
            JSONObject openingJSON = new JSONObject();
            String accInfo = mapDiff.get(key).toString();

            String[] accInfoArray = accInfo.split("~");
            openingJSON.put("accName", accInfoArray[0]);
            openingJSON.put("accId", accInfoArray[1]);
            openingJSON.put("accCode", accInfoArray[2]);

            openingJSON.put("categoryName", accInfoArray[0]);
            openingJSON.put("jeEntryExternalCurrencyRate", 1);
            
            openingJSON.put("type", "Accrued Balance");
            openingJSON.put("debitAmount", "");
            openingJSON.put("creditAmount", "");
            openingJSON.put("balanceAmount", "");
            openingJSON.put("balanceAmountInBase", authHandler.round(0, companyid));
            openingJSON.put("debitAmountInBase", Math.abs(authHandler.round(0, companyid)));
            openingJSON.put("creditAmountInBase", Math.abs(authHandler.round(0, companyid)));
            accObjMap.put(key, openingJSON);
        }
        return accObjMap;
    }

    public Map<String, Object> getSOAInfoMap(JSONObject requestJobj, int exportType, int templateFlag) throws ServiceException, JSONException {

        Map<String, Object> statementOfAccountsMap = new HashMap<String, Object>();
        HashMap<String, ArrayList> amountDueMap = new HashMap<String, ArrayList>();

        ArrayList<StatementOfAccountsSubReport> statementOfReportsSubReportList = new ArrayList<StatementOfAccountsSubReport>();
        HashMap<String, StatementOfAccountsSubReport> stmSubReportListMap = new HashMap<String, StatementOfAccountsSubReport>();

        try {

            DateFormat df = authHandler.getDateOnlyFormat();
            Date stdate = df.parse(requestJobj.optString("stdate"));
            Date endDate = df.parse(requestJobj.optString("enddate"));
            Date asofDate = df.parse(requestJobj.optString("asofdate"));
            df = new SimpleDateFormat("yyyy-MM-dd");
            requestJobj.put("startdateStr", df.format(stdate));
            requestJobj.put("enddateStr", df.format(endDate));
            requestJobj.put("asofdateStr", df.format(asofDate));

            
            String searchJson = requestJobj.optString(Constants.Acc_Search_Json);
            String filterConjuctionCriteria = requestJobj.optString(Constants.Filter_Criteria);
            if (!StringUtil.isNullOrEmpty(searchJson)) {
                requestJobj.put(Constants.Acc_Search_Json, searchJson);
            }
            if (!StringUtil.isNullOrEmpty(filterConjuctionCriteria)) {
                requestJobj.put(Constants.Filter_Criteria, filterConjuctionCriteria);
            }
            
            
             boolean isAdvanceSearch = false;
            String invoiceSearchJson = "";
            String receiptSearchJson = "";
            String cnSearchJson = "";
            String dnSearchJson = "";
            String makePaymentSearchJson = "";

            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.Filter_Criteria))) {
                if (requestJobj.get(Constants.Filter_Criteria).toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.Acc_Search_Json))) {
                searchJson = requestJobj.get(Constants.Acc_Search_Json).toString();
                if (!StringUtil.isNullOrEmpty(searchJson)) {
                    isAdvanceSearch = true;
                    requestJobj.put(Constants.Filter_Criteria, requestJobj.optString(Constants.Filter_Criteria));
                    HashMap<String, Object> reqPar1 = new HashMap<>();
                    reqPar1.put(Constants.companyKey, requestJobj.get(Constants.companyKey));
                    reqPar1.put(Constants.Acc_Search_Json, searchJson);
                    reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                    invoiceSearchJson = accReportsServiceobj.getSearchJsonByModule(reqPar1);
                    reqPar1.remove(Constants.moduleid);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                    receiptSearchJson = accReportsServiceobj.getSearchJsonByModule(reqPar1);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                    dnSearchJson = accReportsServiceobj.getSearchJsonByModule(reqPar1);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    cnSearchJson = accReportsServiceobj.getSearchJsonByModule(reqPar1);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                    makePaymentSearchJson = accReportsServiceobj.getSearchJsonByModule(reqPar1);
                }
            }
            requestJobj.put("invoiceSearchJson", invoiceSearchJson);
            requestJobj.put("receiptSearchJson", receiptSearchJson);
            requestJobj.put("cnSearchJson", cnSearchJson);
            requestJobj.put("dnSearchJson", dnSearchJson);
            requestJobj.put("makePaymentSearchJson", makePaymentSearchJson);
            requestJobj.put("isAdvanceSearch", isAdvanceSearch);
            
            

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestJobj.optString(Constants.globalCurrencyKey));
            KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);

            String BaseCurr = baseCurrency.getName();
            String BaseCurrCode = baseCurrency.getCurrencyCode();
            int duration = Integer.parseInt(requestJobj.optString("interval"));

            Map<String, Object> params = new HashMap<String, Object>();

            String companyid = requestJobj.getString(Constants.companyKey);
            requestJobj = companyReportConfigurationService.pouplateSelectStatementForSOA(requestJobj, requestJobj.getString(Constants.companyKey));
            List list = accReportsDAO.getSOAInfo(requestJobj);

            params.put("BaseCurr", BaseCurr);
            params.put("BaseCurrCode", BaseCurrCode);
            params.put("duration", duration);
            params.put("companyid", companyid);
            params.put("templateFlag", templateFlag);

            boolean isOutstanding = !requestJobj.optBoolean("invoiceAmountDueFilter");
            if (list != null && !list.isEmpty()) {
                DateFormat sqlDF = new SimpleDateFormat("yyyy-MM-dd");
                JSONArray afterDatejArr = new JSONArray();
                JSONArray beforeDatejArr = new JSONArray();
                String accInfo = "";
                Date transactionDate = null;
                Date transactionDateNew = null;
                Date startDate = null;
                double balanceAmtInBase = 0;
                double balanceAmount = 0;
                String prevCustomer = "";
                String invoiceCustomerId = "";
                String type = "";
                String transDate = "";
                startDate = sqlDF.parse(requestJobj.getString("startdateStr"));
                HashMap<String,Object> paramsMap=new HashMap<>();
                paramsMap.put(Constants.companyid, companyid);
                paramsMap.put(Constants.REPORT_TYPE, Constants.COMPANY_REPORT_CONFIG_SOA);
                paramsMap.put("type", Constants.globalFields);
                paramsMap.put("onlyVisible", false);
                JSONObject propertiesObject = companyReportConfigurationService.getTypeFormatByReportType(paramsMap);
                JSONArray propertiesArray = propertiesObject.getJSONArray("data");
                Map<String, String> allAccountMap = new HashMap<String, String>();
                
                for (int i = 0; i < list.size(); i++) {
                    Object[] details = (Object[]) list.get(i);
                    JSONObject detailObj = new JSONObject();
                    JSONObject tempObj = null;
                    for (int j = 0; j < propertiesArray.length(); j++) {
                        tempObj = propertiesArray.getJSONObject(j);
                        if (tempObj.optString("format", "").equals("number") && details[j] != null && !details[j].toString().trim().isEmpty()) {
                            detailObj.put(tempObj.getString("header"), authHandler.round(Double.parseDouble(details[j].toString()), companyid));
                        } else {
                            detailObj.put(tempObj.getString("header"), details[j]);
                        }
                    }

                    if(StringUtil.isNullOrEmpty(detailObj.optString("billid")))
                    {
                        continue;
                    }
                    invoiceCustomerId = detailObj.optString("accName", "");
                    type = detailObj.optString("type", "");
                    detailObj.put("categoryName", detailObj.getString("accName"));
                    String sortPersonInfo = detailObj.getString("accName") + "-" + detailObj.getString("accId");
                    detailObj.put("sortpersoninfo", sortPersonInfo);
                    transactionDate = sqlDF.parse(detailObj.getString("jeEntryDate"));

                    
                    if (isOutstanding) {
                        double knockOffAmtBase = detailObj.isNull("knockOffAmountInBase") ? 0 : detailObj.getDouble("knockOffAmountInBase");
                        double knockOffAmt = detailObj.isNull("knockOffAmount") ? 0 : detailObj.getDouble("knockOffAmount");
                        double amtBase = 0;
                        double amt = 0;
                        if (detailObj.has("creditAmountInBase") && !(detailObj.optDouble("creditAmountInBase",0) == 0)) {
                            amtBase = detailObj.getDouble("creditAmountInBase");
                            amt = detailObj.getDouble("creditAmount");
                            if (amtBase - knockOffAmtBase == 0) {
                                continue;
                            } else {
                                detailObj.put("creditAmountInBase", amtBase - knockOffAmtBase);
                                detailObj.put("creditAmount", amt - knockOffAmt);
                                detailObj.put("transactionAmountInBase", amtBase - knockOffAmtBase);
                            }
                        } else {
                            amtBase = detailObj.getDouble("debitAmountInBase");
                            amt = detailObj.getDouble("debitAmount");
                            if (amtBase - knockOffAmtBase == 0) {
                                continue;
                            } else {
                                detailObj.put("debitAmountInBase", amtBase - knockOffAmtBase);
                                detailObj.put("debitAmount", amt - knockOffAmt);
                                detailObj.put("transactionAmountInBase", amtBase - knockOffAmtBase);
                            }
                        }

                    }
                    if (transactionDate.before(startDate)) {
                        beforeDatejArr.put(detailObj);
                    } else {
                        afterDatejArr.put(detailObj);
                    }

                    accInfo = detailObj.optString("accName", "") + "~" + detailObj.optString("accId", "") + "~" + detailObj.optString("accCode", "");

                    allAccountMap.put(sortPersonInfo, accInfo);
                }
                Map<String, JSONObject> accruedObjMap = calculateAccruedBalance(allAccountMap, beforeDatejArr, companyid);

                Map<String, JSONArray> transactionJSONMap = getBalnceAmountInBase(afterDatejArr, companyid, accruedObjMap);

                    //Code For Jasper
                switch (exportType) {

                    case 1:
                        statementOfAccountsMap = getExportInvoiceMapForSOA(accruedObjMap, transactionJSONMap, companyid,params,requestJobj, BaseCurr);
                        break;
                    case 2:
                        statementOfAccountsMap = getExportMapForSOA(accruedObjMap, transactionJSONMap, params,requestJobj);
                        break;
                    case 3:
                        statementOfAccountsMap = getExportCustCurrMapForSOA(accruedObjMap, transactionJSONMap, params, requestJobj);
                        break;
                }
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return statementOfAccountsMap;

    }

    private Map<String, Object> getExportMapForSOA(Map<String, JSONObject> accruedObjMap, Map<String, JSONArray> transactionJSONMap, Map<String, Object> params,JSONObject requestJobj) throws ServiceException, JSONException {

        
            Map<String, Object> statementOfAccountsMap = new HashMap<String, Object>();
            StatementOfAccountsSubReport statementOfReportsSubReport = null;
            Set<String> customerSet = new TreeSet<String>();
            customerSet.addAll(transactionJSONMap.keySet());
            customerSet.addAll(accruedObjMap.keySet());
            
            String creditAmount = "";
            String debitAmount = "";
            String BalanceAmount = "";
            
            String companyid="";
            int templateFlag;
            String BaseCurrCode="";
            int duration ;
            String duedate="";
            Date jeEntryDate = new Date();
            
            int templateType = requestJobj.optString("type") != null ? Integer.parseInt(requestJobj.optString("type")) : 0;
            
            companyid = params.get("companyid").toString();
            BaseCurrCode = params.get("BaseCurrCode").toString();
            templateFlag = Integer.parseInt(params.get("templateFlag").toString());
            duration = Integer.parseInt(params.get("duration").toString());
            ArrayList<StatementOfAccountsSubReport> statementOfReportsSubReportList = new ArrayList<StatementOfAccountsSubReport>();
            
            
            //Customer Address Map
            HashMap<String, Object> addressParams = new HashMap<String, Object>();
            String invoiceCustomerAdd="";
            HashMap<String, JSONObject> hashMapJSON = new HashMap<String, JSONObject>();
            CustomerAddressDetails customerAddressDetails=null;
            //requestparameter Map
            HashMap<String, Object> requestParams = getRequestParamMap(requestJobj);
        
            
            Map<String,List<AgeingTableForSOA>> agedDetaislMap = new HashMap<>();
            

            List<AgeingTableForSOA> agedMapobj = new ArrayList<>();
            
            AgeingTableForSOA ageingTableData = new AgeingTableForSOA();
            
            JSONObject jSONObject =new JSONObject();
             JSONObject JObj = new JSONObject();
            
            
             //Currency Calculation
             
             Map<String, HashMap> currencyHashMap = new HashMap<String, HashMap>();
             HashMap<String,Double> tempMap = new HashMap<String,Double>();
                          
            DateFormat sqlDF = new SimpleDateFormat("yyyy-MM-dd");
            
            try {
                JSONObject mainObj = accInvoiceServiceDAO.getCustomerAgedReceivableMerged(requestJobj, false, true);
                JSONArray jSONArray = mainObj.getJSONArray("data");
                for (int i = 0; i < jSONArray.length(); i++) {
                     jSONObject = jSONArray.getJSONObject(i);
                    if (jSONObject.has("personid")) {
                        hashMapJSON.put(jSONObject.getString("personid"), jSONObject);
                    }
                }

                for (String customer : customerSet) {

                    statementOfReportsSubReport = null;

                    if (accruedObjMap.containsKey(customer)) {

                        statementOfReportsSubReport = new StatementOfAccountsSubReport();

                         JObj = accruedObjMap.get(customer);

                        addressParams.put("companyid", companyid);
                        addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                        addressParams.put("isBillingAddress", true);    //true to get billing address
                        addressParams.put("customerid", JObj.optString("accId", ""));
                        addressParams.put("templateFlag", templateFlag);
                        customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                        
                        invoiceCustomerAdd = (templateFlag == Constants.HCIS_templateflag || templateFlag == Constants.Guan_Chong_templateflag || templateFlag == Constants.Guan_ChongBF_templateflag) ? accountingHandlerDAOobj.getCustomerAddressForSenwanTec(addressParams) : accountingHandlerDAOobj.getCustomerAddress(addressParams);
                        statementOfReportsSubReport.setCustomerAddress(invoiceCustomerAdd.replaceAll("\n", "<br>"));

                        creditAmount = String.valueOf(authHandler.round(JObj.optDouble("creditAmount", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("creditAmount", 0), companyid));
                        debitAmount = String.valueOf(authHandler.round(JObj.optDouble("debitAmount", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("debitAmount", 0), companyid));
                        BalanceAmount = String.valueOf(authHandler.round(JObj.optDouble("balanceAmountInBase", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("balanceAmountInBase", 0), companyid));

                        statementOfReportsSubReport.setDate(JObj.optString("jeEntryDate", ""));
                        statementOfReportsSubReport.setTransactionId(JObj.optString("type", ""));
                        statementOfReportsSubReport.setJeId(JObj.optString("jeEntryNumber", ""));
                        statementOfReportsSubReport.setDebit(debitAmount);
                        statementOfReportsSubReport.setCredit(creditAmount);
                        statementOfReportsSubReport.setBalance(BalanceAmount);
                        statementOfReportsSubReport.setCurrency(JObj.optString(BaseCurrCode, ""));
                        statementOfReportsSubReport.setCustomer(JObj.optString("accName", ""));
                        statementOfReportsSubReport.setCustomercode(JObj.optString("accCode", ""));

                        if (hashMapJSON.containsKey(JObj.optString("accId", ""))) {
                            jSONObject = hashMapJSON.get(JObj.optString("accId", ""));
                            KwlReturnObject amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, jSONObject.getDouble("totalinbase"), JObj.optString("currencyid", ""), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                            double totalinCustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                            totalinCustCurr = authHandler.round(totalinCustCurr, companyid);
                            statementOfReportsSubReport.setAmountDue5(totalinCustCurr < 0 ? ("(" + BaseCurrCode + " " + authHandler.formattedCommaSeparatedAmount((totalinCustCurr * -1), companyid) + ")") : BaseCurrCode + " " + authHandler.formattedCommaSeparatedAmount(totalinCustCurr, companyid));
                        }
                        
                        
                        statementOfReportsSubReport.setAmount1Header("1-" + duration + " Days");
                        statementOfReportsSubReport.setAmount2Header(duration + 1 + "-" + (2 * duration) + " Days");
                        statementOfReportsSubReport.setAmount3Header((2 * duration) + 1 + "-" + (3 * duration) + " Days");
                        statementOfReportsSubReport.setAmount4Header("Over " + (3 * duration) + " Days");
                        
                        statementOfReportsSubReportList.add(statementOfReportsSubReport);

                    }
                    if (transactionJSONMap.containsKey(customer)) {
                        JSONArray tmpArr = transactionJSONMap.get(customer);
                        for (int i = 0; i < tmpArr.length(); i++) {

                            statementOfReportsSubReport = new StatementOfAccountsSubReport();

                            
                            JObj = tmpArr.getJSONObject(i);

                            
                            if (JObj.optString("type", "").equals("Sales Invoice")) {

                                KwlReturnObject invoice = accountingHandlerDAOobj.getObject(Invoice.class.getName(), JObj.optString("billid"));
                                Invoice inv = (Invoice) invoice.getEntityList().get(0);

                                duedate = inv.getDueDate() != null ? " Due " + sqlDF.format(inv.getDueDate()) + "," : "";

                            }
                            

                            addressParams.put("companyid", companyid);
                            addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                            addressParams.put("isBillingAddress", true);    //true to get billing address
                            addressParams.put("customerid", JObj.optString("accId", ""));
                            addressParams.put("templateFlag", templateFlag);
                            invoiceCustomerAdd = (templateFlag == Constants.HCIS_templateflag || templateFlag == Constants.Guan_Chong_templateflag || templateFlag == Constants.Guan_ChongBF_templateflag) ? accountingHandlerDAOobj.getCustomerAddressForSenwanTec(addressParams) : accountingHandlerDAOobj.getCustomerAddress(addressParams);
                            customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                            
                            statementOfReportsSubReport.setCustomerAddress(invoiceCustomerAdd.replaceAll("\n", "<br>"));

                            creditAmount = String.valueOf(authHandler.round(JObj.optDouble("creditAmount", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("creditAmount", 0), companyid));
                            debitAmount = String.valueOf(authHandler.round(JObj.optDouble("debitAmount", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("debitAmount", 0), companyid));
//                            BalanceAmount = String.valueOf(authHandler.round(JObj.optDouble("transactionAmountInBase", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("transactionAmountInBase", 0), companyid));
                            BalanceAmount = String.valueOf(authHandler.round(JObj.optDouble("balanceAmountInBase", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("balanceAmountInBase", 0.0), companyid));

                            statementOfReportsSubReport.setDate(JObj.optString("jeEntryDate", ""));
//                        statementOfReportsSubReport.setTransactionId(JObj.optString("type",""));
                            statementOfReportsSubReport.setTransactionId(JObj.optString("invoiceNumber", ""));
                            
                            
                            if (templateFlag == Constants.F1RecreationLeasing_templateflag || templateFlag == Constants.F1Recreation_templateflag) {
                                statementOfReportsSubReport.setJeId(duedate);
                                statementOfReportsSubReport.setCompanyEmail(customerAddressDetails != null ? (customerAddressDetails.getEmailID() != null ? customerAddressDetails.getEmailID() : "") : "");
                                statementOfReportsSubReport.setCompanyFax(customerAddressDetails != null ? (customerAddressDetails.getFax() != null ? customerAddressDetails.getFax() : "") : "");
                                statementOfReportsSubReport.setCompanyPhone(customerAddressDetails != null ? (customerAddressDetails.getPhone() != null ? customerAddressDetails.getPhone() : "") : "");

                            } else {
                                statementOfReportsSubReport.setJeId(templateFlag == Constants.BuildMate_templateflag ? "" : JObj.optString("jeEntryNumber", ""));
                            }

//                            statementOfReportsSubReport.setJeId(JObj.optString("jeEntryNumber", ""));
                            
                            statementOfReportsSubReport.setDebit(debitAmount);
                            statementOfReportsSubReport.setCredit(creditAmount);
                            statementOfReportsSubReport.setBalance(BalanceAmount);
                            
                            
                            statementOfReportsSubReport.setCurrency(JObj.optString("currencycode", ""));
                            statementOfReportsSubReport.setCustomer(JObj.optString("accName", ""));
                            
                            if (templateFlag == Constants.BakerTilly_templateflag) {
                                statementOfReportsSubReport.setCustomercode("");
                            } else {
                                statementOfReportsSubReport.setCustomercode(JObj.optString("accCode", ""));
                            }
                            
                        
                            //Ageing Code
                            
                            jeEntryDate = sqlDF.parse(JObj.optString("jeEntryDate"));
                            
                            if (hashMapJSON.containsKey(JObj.optString("accId", ""))) {
                                jSONObject = hashMapJSON.get(JObj.optString("accId", ""));

                                double amountdue1CustCurr = 0, amountdue2CustCurr = 0, amountdue3CustCurr = 0, amountdue4CustCurr = 0, amountdue5CustCurr = 0;
                                double totalinCustCurr = 0;

                                KwlReturnObject amountdueincustCurrency = null;
                                amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, jSONObject.optDouble("amountdueinbase1"), JObj.optString("currencyid"), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                                amountdue1CustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                                amountdue1CustCurr = authHandler.round(amountdue1CustCurr, companyid);

                                amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, jSONObject.optDouble("amountdueinbase2"), JObj.optString("currencyid"), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                                amountdue2CustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                                amountdue2CustCurr = authHandler.round(amountdue2CustCurr, companyid);

                                amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, jSONObject.optDouble("amountdueinbase3"), JObj.optString("currencyid"), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                                amountdue3CustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                                amountdue3CustCurr = authHandler.round(amountdue3CustCurr, companyid);

                                amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, jSONObject.optDouble("amountdueinbase4"), JObj.optString("currencyid"), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                                amountdue4CustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                                amountdue4CustCurr = authHandler.round(amountdue4CustCurr, companyid);

                                amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, (jSONObject.optDouble("amountdueinbase5") + jSONObject.optDouble("amountdueinbase6") + jSONObject.optDouble("amountdueinbase7") + jSONObject.optDouble("amountdueinbase8") + jSONObject.optDouble("accruedbalanceinbase")), JObj.optString("currencyid", ""), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                                amountdue5CustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                                amountdue5CustCurr = authHandler.round(amountdue5CustCurr, companyid);

                                amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, jSONObject.optDouble("totalinbase"), JObj.optString("currencyid", ""), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                                totalinCustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                                totalinCustCurr = authHandler.round(totalinCustCurr, companyid);
                                
                                
                                if (templateFlag == Constants.HCIS_templateflag || templateFlag == Constants.BuildMate_templateflag) {
                                    statementOfReportsSubReport.setAmountDue1(authHandler.formattedCommaSeparatedAmount(jSONObject.getDouble("amountdueinbase2"), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue2(authHandler.formattedCommaSeparatedAmount(jSONObject.getDouble("amountdueinbase3"), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue3(authHandler.formattedCommaSeparatedAmount(jSONObject.getDouble("amountdueinbase4"), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue4(authHandler.formattedCommaSeparatedAmount((jSONObject.getDouble("amountdueinbase5") + jSONObject.getDouble("amountdueinbase6") + jSONObject.getDouble("amountdueinbase7") + jSONObject.getDouble("amountdueinbase8") + jSONObject.getDouble("accruedbalanceinbase")), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue5(authHandler.formattedCommaSeparatedAmount(jSONObject.getDouble("totalinbase"), companyid) + "");
                                } else if (templateFlag == Constants.Endovation_templateflag || templateFlag == Constants.Endovation_cfdn_templateflag || templateFlag == Constants.Endovation_cftp_templateflag || templateFlag == Constants.Endovation_fved_templateflag) {
                                    statementOfReportsSubReport.setMemo(JObj.optString("memo"));
                                    statementOfReportsSubReport.setDuration(duration);
                                    statementOfReportsSubReport.setAmountDueCurrent(authHandler.formattedCommaSeparatedAmount(jSONObject.getDouble("amountdueinbase2"), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue1(authHandler.formattedCommaSeparatedAmount(jSONObject.getDouble("amountdueinbase3"), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue2(authHandler.formattedCommaSeparatedAmount(jSONObject.getDouble("amountdueinbase4"), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue3(authHandler.formattedCommaSeparatedAmount(jSONObject.getDouble("amountdueinbase5"), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue4(authHandler.formattedCommaSeparatedAmount((jSONObject.getDouble("amountdueinbase6") + jSONObject.getDouble("amountdueinbase7") + jSONObject.getDouble("amountdueinbase8") + jSONObject.getDouble("accruedbalanceinbase")), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue5(authHandler.formattedCommaSeparatedAmount(jSONObject.getDouble("totalinbase"), companyid) + "");
                                } else if (templateFlag == Constants.CleanSolutions_templateflag && templateType == 0) {
                                    statementOfReportsSubReport.setAmountDue1(jSONObject.getDouble("amountdueinbase2") != 0 ? (jSONObject.getDouble("amountdueinbase2") > 0 ? (authHandler.formattedAmount(jSONObject.getDouble("amountdueinbase2"), companyid)) : ("(" + authHandler.formattedAmount((jSONObject.getDouble("amountdueinbase2") * -1), companyid) + ")")) : authHandler.formattedAmount(0, companyid));
                                    statementOfReportsSubReport.setAmountDue2(jSONObject.getDouble("amountdueinbase3") != 0 ? (jSONObject.getDouble("amountdueinbase3") > 0 ? (authHandler.formattedAmount(jSONObject.getDouble("amountdueinbase3"), companyid)) : ("(" + authHandler.formattedAmount((jSONObject.getDouble("amountdueinbase3") * -1), companyid) + ")")) : authHandler.formattedAmount(0, companyid));
                                    statementOfReportsSubReport.setAmountDue3(jSONObject.getDouble("amountdueinbase4") != 0 ? (jSONObject.getDouble("amountdueinbase4") > 0 ? (authHandler.formattedAmount(jSONObject.getDouble("amountdueinbase4"), companyid)) : ("(" + authHandler.formattedAmount((jSONObject.getDouble("amountdueinbase4") * -1), companyid) + ")")) : authHandler.formattedAmount(0, companyid));
                                    double total = jSONObject.getDouble("amountdueinbase5") + jSONObject.getDouble("amountdueinbase6") + jSONObject.getDouble("amountdueinbase7") + jSONObject.getDouble("amountdueinbase8") + jSONObject.getDouble("accruedbalanceinbase");
                                    statementOfReportsSubReport.setAmountDue4(total != 0 ? (total > 0 ? (authHandler.formattedAmount(total, companyid)) : ("(" + authHandler.formattedAmount((total * -1), companyid) + ")")) : authHandler.formattedAmount(0, companyid));
                                    statementOfReportsSubReport.setAmountDue5(jSONObject.getDouble("totalinbase") != 0 ? (jSONObject.getDouble("totalinbase") > 0 ? (authHandler.formattedAmount(jSONObject.getDouble("totalinbase"), companyid)) : ("(" + authHandler.formattedAmount((jSONObject.getDouble("totalinbase") * -1), companyid) + ")")) : authHandler.formattedAmount(0, companyid));
                                } else {
                                    statementOfReportsSubReport.setAmountDue1(authHandler.formattedAmount(jSONObject.getDouble("amountdueinbase2"), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue2(authHandler.formattedAmount(jSONObject.getDouble("amountdueinbase3"), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue3(authHandler.formattedAmount(jSONObject.getDouble("amountdueinbase4"), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue4(authHandler.formattedAmount((jSONObject.getDouble("amountdueinbase5") + jSONObject.getDouble("amountdueinbase6") + jSONObject.getDouble("amountdueinbase7") + jSONObject.getDouble("amountdueinbase8") + jSONObject.getDouble("accruedbalanceinbase")), companyid) + "");
                                    statementOfReportsSubReport.setAmountDue5(authHandler.formattedAmount(jSONObject.getDouble("totalinbase"), companyid) + "");
                                }

                                
                                // Currency Code
                                
                                if(tempMap.containsKey(JObj.optString("currencycode"))){
                                    double temp = tempMap.get(JObj.optString("currencycode")) + JObj.optDouble("knockOffAmount");
                                    tempMap.put(JObj.optString("currencycode"), temp);
                                }else{
                                    tempMap.put(JObj.optString("currencycode"), JObj.optDouble("knockOffAmount"));
                                
                                }
                            }

                            statementOfReportsSubReportList.add(statementOfReportsSubReport);

                        }
                        
                        currencyHashMap.put(JObj.optString("accId", ""), tempMap);
                        
                        List<SOABalanceOutstandingPojo> outstandingPojos = new ArrayList<SOABalanceOutstandingPojo>();
                            statementOfReportsSubReport.setOutstandingFlag(true);
                            for (Map.Entry<String, HashMap> entry : currencyHashMap.entrySet()) {
                                HashMap<String, Double> tempHashMap = entry.getValue();
                                if(tempHashMap != null){
                                    for (Map.Entry<String, Double> entryInner : tempHashMap.entrySet()) {
                                        SOABalanceOutstandingPojo sOABalanceOutstandingPojo = new SOABalanceOutstandingPojo();
                                        sOABalanceOutstandingPojo.setBaseCurrency((entryInner.getKey()));
                                        sOABalanceOutstandingPojo.setBalance((templateFlag == Constants.HCIS_templateflag || templateFlag == Constants.BuildMate_templateflag) ? authHandler.formattedCommaSeparatedAmount(entryInner.getValue(), companyid) : authHandler.formattedAmount(entryInner.getValue(), companyid));
                                        outstandingPojos.add(sOABalanceOutstandingPojo);
                                    }
                                }
                            }
                        statementOfReportsSubReport.setsOABalanceOutstandingPojos(outstandingPojos);
                        tempMap=new HashMap<String,Double>();
                        
                        
                        ageingTableData.setAgeingCurrency(BaseCurrCode);
                        ageingTableData.setAmountDue1(authHandler.formattedAmount(jSONObject.optDouble("amountdue2", 0), companyid));
                        ageingTableData.setAmountDue2(authHandler.formattedAmount(jSONObject.optDouble("amountdue3", 0), companyid));
                        ageingTableData.setAmountDue3(authHandler.formattedAmount(jSONObject.optDouble("amountdue4", 0), companyid));
                        ageingTableData.setAmountDue4(authHandler.formattedAmount((jSONObject.optDouble("amountdue5", 0)+jSONObject.optDouble("amountdue6", 0)+jSONObject.optDouble("amountdue7", 0)+jSONObject.optDouble("amountdue8", 0)), companyid));
                        ageingTableData.setAmountDue5(authHandler.formattedAmount(jSONObject.optDouble("total", 0), companyid));
                                
                        agedMapobj.add(ageingTableData);
                                
                        agedDetaislMap.put(JObj.optString("accId", ""), agedMapobj);
                        
                        statementOfReportsSubReport.setAgeingTableData(agedDetaislMap.get(JObj.optString("accId", ""))==null?(new ArrayList<AgeingTableForSOA>()):agedDetaislMap.get(JObj.optString("accId", "")));
                        
                        statementOfReportsSubReport.setAmount1Header("1-" + duration + " Days");
                        statementOfReportsSubReport.setAmount2Header(duration + 1 + "-" + (2 * duration) + " Days");
                        statementOfReportsSubReport.setAmount3Header((2 * duration) + 1 + "-" + (3 * duration) + " Days");
                        statementOfReportsSubReport.setAmount4Header("Over " + (3 * duration) + " Days");

                        
                        

                    }
//                    statementOfReportsSubReportList.add(statementOfReportsSubReport);
                    agedMapobj = new ArrayList<>();
                }

                statementOfAccountsMap.put("StatementOfAccountsSubReportData", new JRBeanCollectionDataSource(statementOfReportsSubReportList));

            } catch (SessionExpiredException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
            return statementOfAccountsMap;
    }

    private Map<String, Object> getExportInvoiceMapForSOA(Map<String, JSONObject> accruedObjMap, Map<String, JSONArray> transactionJSONMap, String companyid,Map<String, Object> params,JSONObject requestJobj, String BaseCurr) {

        Map<String, Object> statementOfAccountsMap = new HashMap<String, Object>();
        StatementOfAccountsSubReport statementOfReportsSubReport = null;
        Set<String> customerSet = new TreeSet<String>();
        customerSet.addAll(transactionJSONMap.keySet());
        customerSet.addAll(accruedObjMap.keySet());

        String creditAmount = "";
        String debitAmount = "";
        String BalanceAmount = "";

        ArrayList<StatementOfAccountsSubReport> statementOfReportsSubReportList = new ArrayList<StatementOfAccountsSubReport>();

        String jeEntryDateStr = null;

        try {
            DateFormat sqlDF = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(requestJobj);
            Date endDate = authHandler.getDateOnlyFormat().parse(requestJobj.optString("enddate"));

            double AmountDue = 0;
            String BaseCurrCode = "";
            if (params.containsKey("BaseCurrCode")) {
                BaseCurrCode = params.get("BaseCurrCode") != null ? (String) params.get("BaseCurrCode") : "";
            }
            for (String customer : customerSet) {
                statementOfReportsSubReport = null;
                if (transactionJSONMap.containsKey(customer)) {
                    JSONArray tmpArr = transactionJSONMap.get(customer);
                    for (int i = 0; i < tmpArr.length(); i++) {

                        statementOfReportsSubReport = new StatementOfAccountsSubReport();

                        JSONObject JObj = tmpArr.getJSONObject(i);
                        if (JObj.optString("type", "").equals("Sales Invoice")) {

                            creditAmount = String.valueOf(authHandler.round(JObj.optDouble("creditAmount", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("creditAmount", 0), companyid));
                            debitAmount = String.valueOf(authHandler.round(JObj.optDouble("debitAmount", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("debitAmount", 0), companyid));
                            BalanceAmount = String.valueOf(authHandler.round(JObj.optDouble("transactionAmountInBase", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("transactionAmountInBase", 0), companyid));
                            
                            String registrationNo = "";
                            int templateFlag = 0;
                            if(params.containsKey("templateFlag")){
                                templateFlag = params.get("templateFlag") != null ? (Integer) params.get("templateFlag") : 0 ;
                            }
                            if (companyid.equalsIgnoreCase(Constants.BakerTilly_BTC_COMPANYID)) {
                                registrationNo = Constants.BAKERTILLY_BTC_REGISTRATION_NO;
                            } else if (templateFlag == Constants.BakerTilly_templateflag_pcs) {
                                registrationNo = Constants.BAKERTILLY_PCS_REGISTRATION_NO;
                            } else if (companyid.equalsIgnoreCase(Constants.BakerTilly_TFWMS_COMPANYID)) {
                                registrationNo = Constants.BAKERTILLY_TFWMS_REGISTRATION_NO;
                            }
                            if (templateFlag == Constants.BakerTilly_templateflag || templateFlag == Constants.BakerTilly_templateflag_pcs) {
                                creditAmount = !StringUtil.isNullOrEmpty(creditAmount) ? authHandler.formattedCommaSeparatedAmount(Double.parseDouble(creditAmount),companyid) : "";
                                debitAmount = !StringUtil.isNullOrEmpty(debitAmount) ?  authHandler.formattedCommaSeparatedAmount(Double.parseDouble(debitAmount),companyid) : "";
                                BalanceAmount = !StringUtil.isNullOrEmpty(BalanceAmount) ? authHandler.formattedCommaSeparatedAmount(Double.parseDouble(BalanceAmount),companyid) : "";
                            }
                            AmountDue += JObj.optDouble("transactionAmountInBase");

                            jeEntryDateStr = JObj.optString("jeEntryDate", "");
                            Date jeEntryDate = sqlDF.parse(jeEntryDateStr);

                            statementOfReportsSubReport.setDate(JObj.optString("jeEntryDate", ""));
                            statementOfReportsSubReport.setTransactionId(JObj.optString("invoiceNumber", ""));
                            statementOfReportsSubReport.setJeId(JObj.optString("jeEntryNumber", ""));
                            statementOfReportsSubReport.setDebit(debitAmount);
                            statementOfReportsSubReport.setCredit(creditAmount);
                            statementOfReportsSubReport.setBalance(BalanceAmount);
                            statementOfReportsSubReport.setCurrency(JObj.optString("currencycode", ""));
                            statementOfReportsSubReport.setDaysOutstanding((int) authHandler.diffDays(jeEntryDate, new Date()) + "");
                            statementOfReportsSubReport.setHeaderdate(endDate != null ? df.format(endDate) : "");
                            statementOfReportsSubReport.setInvoicAmountDue((AmountDue != 0) ? AmountDue : 0);
                            statementOfReportsSubReport.setCustomer(JObj.optString("accCode", ""));
                            if (templateFlag == Constants.BakerTilly_templateflag || templateFlag == Constants.BakerTilly_templateflag_pcs) {
                                statementOfReportsSubReport.setBaseCurrency(BaseCurrCode);
                            }else {
                                statementOfReportsSubReport.setBaseCurrency(BaseCurr);
                            }
                            statementOfReportsSubReport.setRegistrationNo(registrationNo);
                            statementOfReportsSubReportList.add(statementOfReportsSubReport);
                        }
                    }

                }
            }

            statementOfAccountsMap.put("StatementOfAccountsSubReportData", new JRBeanCollectionDataSource(statementOfReportsSubReportList));
        } catch (ParseException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return statementOfAccountsMap;
    }

    private Map<String, Object> getExportCustCurrMapForSOA(Map<String, JSONObject> accruedObjMap, Map<String, JSONArray> transactionJSONMap, Map<String, Object> params, JSONObject requestJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException, UnsupportedEncodingException {

        Map<String, Object> statementOfAccountsMap = new HashMap<String, Object>();
        StatementOfAccountsSubReport statementOfReportsSubReport = null;
        Set<String> customerSet = new TreeSet<String>();
        customerSet.addAll(transactionJSONMap.keySet());
        customerSet.addAll(accruedObjMap.keySet());

        String creditAmount = "";
        String debitAmount = "";
        String BalanceAmount = "";
        String transactionAmount = "";
        String duedate = "";

        double BalanceAmt = 0.0;
        double creditAmt = 0.0;
        double debitAmt = 0.0;

        String companyid = "";
        int duration;
        int templateFlag;
        Date jeEntryDate = new Date();

        KwlReturnObject amountcustCurrency = null;

        companyid = params.get("companyid").toString();
        duration = Integer.parseInt(params.get("duration").toString());
        templateFlag = Integer.parseInt(params.get("templateFlag").toString());

        KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) comp.getEntityList().get(0);

        HashMap<String, JSONObject> hashMapJSON = new HashMap<String, JSONObject>();
        JSONObject mainObj = accInvoiceServiceDAO.getCustomerAgedReceivableMerged(requestJobj, false, true);
        JSONArray jSONArray = mainObj.getJSONArray("data");
        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jSONObject = jSONArray.getJSONObject(i);
            if (jSONObject.has("personid")) {
                hashMapJSON.put(jSONObject.getString("personid"), jSONObject);
            }
        }

        //requestparameter Map
        HashMap<String, Object> requestParams = getRequestParamMap(requestJobj);

        DateFormat sqlDF = new SimpleDateFormat("yyyy-MM-dd");

        ArrayList<StatementOfAccountsSubReport> statementOfReportsSubReportList = new ArrayList<StatementOfAccountsSubReport>();

        //Customer Address Map
        HashMap<String, Object> addressParams = new HashMap<String, Object>();
        String invoiceCustomerAdd = "";
        Customer customer1 = null;

        for (String customer : customerSet) {

            statementOfReportsSubReport = null;
            JSONObject CustObj = accruedObjMap.get(customer);

            KwlReturnObject cust = accountingHandlerDAOobj.getObject(Customer.class.getName(), CustObj.optString("accId"));
            customer1 = (Customer) cust.getEntityList().get(0);

            double custCurrToBaseExchRate = 1 / accCurrencyDAOobj.getCurrencyToBaseRate(requestParams, customer1.getCurrency().getCurrencyID(), jeEntryDate);
            if (accCurrencyDAOobj.getCurrencyToBaseRate(requestParams, customer1.getCurrency().getCurrencyID(), jeEntryDate) == 0.0) {
                custCurrToBaseExchRate = 1;
            }

            if (accruedObjMap.containsKey(customer)) {

                statementOfReportsSubReport = new StatementOfAccountsSubReport();

                JSONObject JObj = accruedObjMap.get(customer);

                addressParams.put("companyid", companyid);
                addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                addressParams.put("isBillingAddress", true);    //true to get billing address
                addressParams.put("customerid", JObj.optString("accId",""));
                addressParams.put("templateFlag", templateFlag);

                invoiceCustomerAdd = (templateFlag == Constants.HCIS_templateflag || templateFlag == Constants.Guan_Chong_templateflag || templateFlag == Constants.Guan_ChongBF_templateflag) ? accountingHandlerDAOobj.getCustomerAddressForSenwanTec(addressParams) : accountingHandlerDAOobj.getCustomerAddress(addressParams);

                statementOfReportsSubReport.setCustomerAddress(invoiceCustomerAdd.replaceAll("\n", "<br>"));

                //To Calculate Balance in Accrued case
                BalanceAmount = String.valueOf(authHandler.round(JObj.optDouble("balanceAmountInBase", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("balanceAmountInBase", 0), companyid));

                BalanceAmt = JObj.optDouble("balanceAmountInBase", 0);
                amountcustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, BalanceAmt, customer1.getCurrency().getCurrencyID(), jeEntryDate, custCurrToBaseExchRate);
                BalanceAmt = (Double) amountcustCurrency.getEntityList().get(0);
                transactionAmount = String.valueOf(authHandler.formattedCommaSeparatedAmount(JObj.optDouble("transactionAmountInBase", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.formattedCommaSeparatedAmount(JObj.optDouble("transactionAmountInBase", 0), companyid));
                BalanceAmount = String.valueOf(authHandler.round(JObj.optDouble("balanceAmountInBase", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("balanceAmountInBase", 0), companyid));

                if (!StringUtil.isNullOrEmpty(BalanceAmount)) {
                    BalanceAmt = Double.parseDouble(BalanceAmount);
                } else {
                    BalanceAmt = 0.0;
                }
                statementOfReportsSubReport.setDate(JObj.optString("jeEntryDate", ""));
                statementOfReportsSubReport.setTransactionId(JObj.optString("type", ""));
                statementOfReportsSubReport.setJeId(templateFlag == Constants.BuildMate_templateflag ? " " : JObj.optString("jeEntryNumber", ""));
                statementOfReportsSubReport.setBalance(BalanceAmt < 0 ? ("(" + customer1.getCurrency().getSymbol() + " " + authHandler.formattedCommaSeparatedAmount((BalanceAmt * -1), companyid) + ")") : customer1.getCurrency().getSymbol() + " " + authHandler.formattedCommaSeparatedAmount(BalanceAmt, companyid) + "");
                statementOfReportsSubReport.setCustomer(JObj.optString("accName", ""));

                if (hashMapJSON.containsKey(JObj.optString("accId", ""))) {
                    JSONObject jSONObject = hashMapJSON.get(JObj.optString("accId", ""));
                    KwlReturnObject amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, jSONObject.getDouble("totalinbase"), customer1.getCurrency().getCurrencyID(), jeEntryDate, custCurrToBaseExchRate);
                    double totalinCustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                    totalinCustCurr = authHandler.round(totalinCustCurr, companyid);
                    statementOfReportsSubReport.setAmountDue5(totalinCustCurr < 0 ? ("(" + customer1.getCurrency().getSymbol() + " " + authHandler.formattedCommaSeparatedAmount((totalinCustCurr * -1), companyid) + ")") : customer1.getCurrency().getSymbol() + " " + authHandler.formattedCommaSeparatedAmount(totalinCustCurr, companyid));
                }
                statementOfReportsSubReportList.add(statementOfReportsSubReport);

            }
            if (transactionJSONMap.containsKey(customer)) {
                JSONArray tmpArr = transactionJSONMap.get(customer);
                for (int i = 0; i < tmpArr.length(); i++) {
                    statementOfReportsSubReport = new StatementOfAccountsSubReport();
                    JSONObject JObj = tmpArr.getJSONObject(i);

                    if (JObj.optString("type", "").equals("Sales Invoice")) {
                        KwlReturnObject invoice = accountingHandlerDAOobj.getObject(Invoice.class.getName(), JObj.optString("billid"));
                        Invoice inv = (Invoice) invoice.getEntityList().get(0);
                        duedate = inv.getDueDate() != null ? " Due " + sqlDF.format(inv.getDueDate()) + "," : "";
                    }

                    if (accCurrencyDAOobj.getCurrencyToBaseRate(requestParams, customer1.getCurrency().getCurrencyID(), jeEntryDate) == 0.0) {
                        custCurrToBaseExchRate = JObj.optDouble("jeEntryExternalCurrencyRate", 0.0);
                    }

                    // Customer Address Map
                    addressParams.put("companyid", companyid);
                    addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                    addressParams.put("isBillingAddress", true);    //true to get billing address
                    addressParams.put("customerid", JObj.optString("accId", ""));
                    addressParams.put("templateFlag", templateFlag);

                    invoiceCustomerAdd = (templateFlag == Constants.HCIS_templateflag || templateFlag == Constants.Guan_Chong_templateflag || templateFlag == Constants.Guan_ChongBF_templateflag) ? accountingHandlerDAOobj.getCustomerAddressForSenwanTec(addressParams) : accountingHandlerDAOobj.getCustomerAddress(addressParams);

                    statementOfReportsSubReport.setCustomerAddress(invoiceCustomerAdd.replaceAll("\n", "<br>"));

                    //calculate creditAmt,debitAmt,Balnce Amt in Customer Currency
                    creditAmt = JObj.optDouble("creditAmountInBase", 0);
                    amountcustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, creditAmt, customer1.getCurrency().getCurrencyID(), jeEntryDate, custCurrToBaseExchRate);
                    creditAmt = (Double) amountcustCurrency.getEntityList().get(0);
                    creditAmount = String.valueOf(authHandler.formattedCommaSeparatedAmount(creditAmt, companyid)).equals("0.0") ? "" : String.valueOf(authHandler.formattedCommaSeparatedAmount(creditAmt, companyid));

                    debitAmt = JObj.optDouble("debitAmountInBase", 0);
                    amountcustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, debitAmt, customer1.getCurrency().getCurrencyID(), jeEntryDate, custCurrToBaseExchRate);
                    debitAmt = (Double) amountcustCurrency.getEntityList().get(0);
                    debitAmount = String.valueOf(authHandler.formattedCommaSeparatedAmount(debitAmt, companyid)).equals("0.0") ? "" : String.valueOf(authHandler.formattedCommaSeparatedAmount(debitAmt, companyid));

                    BalanceAmt = JObj.optDouble("balanceAmountInBase", 0);
                    amountcustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, BalanceAmt, customer1.getCurrency().getCurrencyID(), jeEntryDate, custCurrToBaseExchRate);
                    BalanceAmt = (Double) amountcustCurrency.getEntityList().get(0);
                    transactionAmount = String.valueOf(authHandler.formattedCommaSeparatedAmount(JObj.optDouble("transactionAmountInBase", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.formattedCommaSeparatedAmount(JObj.optDouble("transactionAmountInBase", 0), companyid));
                    BalanceAmount = String.valueOf(authHandler.round(JObj.optDouble("balanceAmountInBase", 0), companyid)).equals("0.0") ? "" : String.valueOf(authHandler.round(JObj.optDouble("balanceAmountInBase", 0), companyid));

                    statementOfReportsSubReport.setDate(JObj.optString("jeEntryDate", ""));
                    statementOfReportsSubReport.setJeId(templateFlag == Constants.BuildMate_templateflag ? " " : JObj.optString("jeEntryNumber", ""));

                    statementOfReportsSubReport.setCredit("(" + customer1.getCurrency().getSymbol() + " " + creditAmount + ")");
                    if (creditAmount.equals("") || creditAmount.equals("0.00")) {
                        statementOfReportsSubReport.setCredit(customer1.getCurrency().getSymbol() + " " + debitAmount);
                    }

                    statementOfReportsSubReport.setTransactionId(JObj.optString("invoiceNumber", "") + "," + duedate + " Orig Amount  " + JObj.optString("currencysymbol", "")+ " " + transactionAmount);

                    statementOfReportsSubReport.setBalance(BalanceAmt < 0 ? ("(" + customer1.getCurrency().getSymbol() + " " + authHandler.formattedCommaSeparatedAmount((BalanceAmt * -1), companyid) + ")") : customer1.getCurrency().getSymbol() + " " + authHandler.formattedCommaSeparatedAmount(BalanceAmt, companyid) + "");

                    statementOfReportsSubReport.setCurrency(JObj.optString("currencycode", ""));
                    statementOfReportsSubReport.setCustomer(JObj.optString("accName", ""));

                    jeEntryDate = sqlDF.parse(JObj.optString("jeEntryDate"));

                    //Amount Due Calculation
                    if (hashMapJSON.containsKey(JObj.optString("accId", ""))) {
                        JSONObject jSONObject = hashMapJSON.get(JObj.optString("accId", ""));

                        double amountdue1CustCurr = 0, amountdue2CustCurr = 0, amountdue3CustCurr = 0, amountdue4CustCurr = 0, amountdue5CustCurr = 0;
                        double totalinCustCurr = 0;

                        KwlReturnObject amountdueincustCurrency = null;
                        amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, jSONObject.optDouble("amountdueinbase1"), JObj.optString("currencyid"), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                        amountdue1CustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                        amountdue1CustCurr = authHandler.round(amountdue1CustCurr, companyid);

                        amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, jSONObject.optDouble("amountdueinbase2"), JObj.optString("currencyid"), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                        amountdue2CustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                        amountdue2CustCurr = authHandler.round(amountdue2CustCurr, companyid);

                        amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, jSONObject.optDouble("amountdueinbase3"), JObj.optString("currencyid"), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                        amountdue3CustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                        amountdue3CustCurr = authHandler.round(amountdue3CustCurr, companyid);

                        amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, jSONObject.optDouble("amountdueinbase4"), JObj.optString("currencyid"), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                        amountdue4CustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                        amountdue4CustCurr = authHandler.round(amountdue4CustCurr, companyid);

                        amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, (jSONObject.optDouble("amountdueinbase5") + jSONObject.optDouble("amountdueinbase6") + jSONObject.optDouble("amountdueinbase7") + jSONObject.optDouble("amountdueinbase8") + jSONObject.optDouble("accruedbalanceinbase")), JObj.optString("currencyid", ""), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                        amountdue5CustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                        amountdue5CustCurr = authHandler.round(amountdue5CustCurr, companyid);

                        amountdueincustCurrency = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, jSONObject.optDouble("totalinbase"), JObj.optString("currencyid", ""), jeEntryDate, JObj.optDouble("jeEntryExternalCurrencyRate"));
                        totalinCustCurr = (Double) amountdueincustCurrency.getEntityList().get(0);
                        totalinCustCurr = authHandler.round(totalinCustCurr, companyid);

                        statementOfReportsSubReport.setAmountDueCurrent(amountdue1CustCurr < 0 ? ("(" + authHandler.formattedCommaSeparatedAmount((amountdue1CustCurr * -1), companyid) + ")") : authHandler.formattedCommaSeparatedAmount(amountdue1CustCurr, companyid) + "");
                        statementOfReportsSubReport.setAmountDue1(amountdue2CustCurr < 0 ? ("(" + authHandler.formattedCommaSeparatedAmount((amountdue2CustCurr * -1), companyid) + ")") : authHandler.formattedCommaSeparatedAmount(amountdue2CustCurr, companyid) + "");
                        statementOfReportsSubReport.setAmountDue2(amountdue3CustCurr < 0 ? ("(" + authHandler.formattedCommaSeparatedAmount((amountdue3CustCurr * -1), companyid) + ")") : authHandler.formattedCommaSeparatedAmount(amountdue3CustCurr, companyid) + "");
                        statementOfReportsSubReport.setAmountDue3(amountdue4CustCurr < 0 ? ("(" + authHandler.formattedCommaSeparatedAmount((amountdue4CustCurr * -1), companyid) + ")") : authHandler.formattedCommaSeparatedAmount(amountdue4CustCurr, companyid) + "");
                        statementOfReportsSubReport.setAmountDue4(amountdue5CustCurr < 0 ? ("(" + authHandler.formattedCommaSeparatedAmount((amountdue5CustCurr * -1), companyid) + ")") : authHandler.formattedCommaSeparatedAmount(amountdue5CustCurr, companyid) + "");
                        statementOfReportsSubReport.setAmountDue5(totalinCustCurr < 0 ? ("(" + customer1.getCurrency().getSymbol() + " " + authHandler.formattedCommaSeparatedAmount((totalinCustCurr * -1), companyid) + ")") : customer1.getCurrency().getSymbol() + " " + authHandler.formattedCommaSeparatedAmount(totalinCustCurr, companyid));

                    }

                    statementOfReportsSubReport.setCompanyPhone(company.getPhoneNumber());
                    statementOfReportsSubReport.setCompanyFax(company.getFaxNumber());
                    statementOfReportsSubReport.setCompanyEmail(company.getEmailID());

                    statementOfReportsSubReport.setAmount1Header("1-" + duration + " Days");
                    statementOfReportsSubReport.setAmount2Header(duration + 1 + "-" + (2 * duration) + " Days");
                    statementOfReportsSubReport.setAmount3Header((2 * duration) + 1 + "-" + (3 * duration) + " Days");
                    statementOfReportsSubReport.setAmount4Header("Over " + (3 * duration) + " Days");

                    statementOfReportsSubReportList.add(statementOfReportsSubReport);
                    duedate = "";
                }

            }
        }

        statementOfAccountsMap.put("StatementOfAccountsSubReportData", new JRBeanCollectionDataSource(statementOfReportsSubReportList));

        return statementOfAccountsMap;
    }

    private HashMap<String, Object> getRequestParamMap(JSONObject requestJobj) {

        HashMap<String, Object> requestParams = null;
        try {
            requestParams = AccountingManager.getGlobalParamsJson(requestJobj);

            boolean invoiceAmountDueFilter = requestJobj.optBoolean("invoiceAmountDueFilter", false);
            boolean isPostDatedCheque = requestJobj.optBoolean("isPostDatedCheque", false);
            boolean isSortedOnCreationDate = requestJobj.optBoolean("isSortedOnCreationDate", false);

            Date startDate = authHandler.getDateOnlyFormat().parse(requestJobj.optString("stdate"));
            Date endDate = authHandler.getDateOnlyFormat().parse(requestJobj.optString("enddate"));
            Date asofDate = authHandler.getDateOnlyFormat().parse(requestJobj.optString("asofdate"));
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String customerIds = requestJobj.optString("customerIds", "");
            String withoutinventory = requestJobj.optString("withoutinventory");

            requestParams.put("customerIds", customerIds);
            requestParams.put("withoutinventory", withoutinventory);
            requestParams.put("startDate", new Date(0));
            requestParams.put("endDate", endDate);
            requestParams.put("isPostDatedCheque", isPostDatedCheque);
            requestParams.put("invoiceAmountDueFilter", invoiceAmountDueFilter);
            requestParams.put("isSortedOnCreationDate", isSortedOnCreationDate);

            String dir = "";
            String sort = "";

            if (!StringUtil.isNullOrEmpty(requestJobj.optString("dir")) && !StringUtil.isNullOrEmpty(requestJobj.optString("sort"))) {
                dir = requestJobj.optString("dir");
                sort = requestJobj.optString("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }

            String searchJson = requestJobj.optString(Constants.Acc_Search_Json);
            String filterConjuctionCriteria = requestJobj.optString(Constants.Filter_Criteria);
            if (!StringUtil.isNullOrEmpty(searchJson)) {
                requestParams.put(Constants.Acc_Search_Json, searchJson);
            }
            if (!StringUtil.isNullOrEmpty(filterConjuctionCriteria)) {
                requestParams.put(Constants.Filter_Criteria, filterConjuctionCriteria);
            }

            boolean isAdvanceSearch = false;
            String invoiceSearchJson = "";
            String receiptSearchJson = "";
            String cnSearchJson = "";
            String dnSearchJson = "";
            String makePaymentSearchJson = "";

            if (requestJobj.has(Constants.Acc_Search_Json) && requestJobj.opt(Constants.Acc_Search_Json) != null) {
                searchJson = requestJobj.opt(Constants.Acc_Search_Json).toString();
                if (!StringUtil.isNullOrEmpty(searchJson)) {
                    isAdvanceSearch = true;
                    requestParams.put(Constants.Filter_Criteria, requestJobj.optString(Constants.Filter_Criteria));
                    HashMap<String, Object> reqPar1 = new HashMap<>();
                    reqPar1.put(Constants.companyKey, requestJobj.get(Constants.companyKey));
                    reqPar1.put(Constants.Acc_Search_Json, searchJson);
                    reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                    invoiceSearchJson = accReportsServiceobj.getSearchJsonByModule(reqPar1);
                    reqPar1.remove(Constants.moduleid);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                    receiptSearchJson = accReportsServiceobj.getSearchJsonByModule(reqPar1);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                    dnSearchJson = accReportsServiceobj.getSearchJsonByModule(reqPar1);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    cnSearchJson = accReportsServiceobj.getSearchJsonByModule(reqPar1);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                    makePaymentSearchJson = accReportsServiceobj.getSearchJsonByModule(reqPar1);
                }
            }

            requestParams.put("invoiceSearchJson", invoiceSearchJson);
            requestParams.put("receiptSearchJson", receiptSearchJson);
            requestParams.put("cnSearchJson", cnSearchJson);
            requestParams.put("dnSearchJson", dnSearchJson);
            requestParams.put("makePaymentSearchJson", makePaymentSearchJson);
            requestParams.put("isAdvanceSearch", isAdvanceSearch);

        } catch (JSONException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }
    
    
   
    public JSONArray getCustomerAgedReceivable(JSONObject requestJobj,boolean isAgedReceivables,boolean detailReport) throws ServiceException, JSONException, SessionExpiredException, ParseException
    {
            
            JSONArray allTransactionArr = new JSONArray();
            String companyid = requestJobj.optString("companyid","");
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat df1 = authHandler.getDateOnlyFormat();
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            boolean includeExcludeChildCmb;
            
        try {
            
            Date endDate = df1.parse(requestJobj.getString("enddate"));
            Date asofDate = df1.parse(requestJobj.getString("asofdate"));
            
            df1 = new SimpleDateFormat("yyyy-MM-dd");
            

            requestJobj.put("enddate", df1.format(endDate));
            requestJobj.put("asofdate", df1.format(asofDate));
            
            requestJobj.put("isFromAR", true);
            
            requestJobj = companyReportConfigurationService.pouplateSelectStatementForAR(requestJobj, requestJobj.getString(Constants.companyKey));
            
            HashMap invoiceRequestParams = null;
//            HashMap invoiceRequestParams = accInvoiceServiceDAO.getCustomerAgedReceivableMap(requestJobj, isAgedReceivables);
//            
//            requestJobj.put("invoiceSearchJson", invoiceRequestParams.get("invoiceSearchJson"));
//            requestJobj.put("cnSearchJson", invoiceRequestParams.get("cnSearchJson"));
//            requestJobj.put("dnSearchJson", invoiceRequestParams.get("dnSearchJson"));
//            requestJobj.put("makePaymentSearchJson", invoiceRequestParams.get("makePaymentSearchJson"));
//            requestJobj.put("receiptSearchJson", invoiceRequestParams.get("receiptSearchJson"));
//            requestJobj.put("isAdvanceSearch", Boolean.parseBoolean(invoiceRequestParams.get("isAdvanceSearch").toString()));
//            
            
            List list = accReportsDAO.getSOAInfo(requestJobj);
            
            requestJobj.put("agedReport",detailReport);
            
            Date startDate = null;
            
            if (!StringUtil.isNullOrEmpty(requestJobj.optString("includeExcludeChildCmb",null)) && requestJobj.getString("includeExcludeChildCmb").equals("All")) {
                includeExcludeChildCmb = true;
            } else {
                includeExcludeChildCmb = !StringUtil.isNullOrEmpty(requestJobj.optString("includeExcludeChildCmb")) ? Boolean.parseBoolean(requestJobj.getString("includeExcludeChildCmb")) : false;
            }
//            if (invoiceRequestParams.containsKey(Constants.REQ_startdate) && invoiceRequestParams.get(Constants.REQ_startdate) != null) {
//                startDate = df.parse(invoiceRequestParams.get(Constants.REQ_startdate).toString());
//            }
//            invoiceRequestParams.put("includeExcludeChildCmb",includeExcludeChildCmb);
            
            
            
            if (list != null && !list.isEmpty()) {
//                DateFormat sqlDF = new SimpleDateFormat("yyyy-MM-dd");
                JSONObject transactionObj = new JSONObject();
                
                Double amountInBase = 0.0;
               
//                    startDate = sqlDF.parse(requestJobj.getString("startdate"));
                HashMap<String,Object> paramsMap=new HashMap<>();
                paramsMap.put(Constants.companyid, companyid);
                paramsMap.put(Constants.REPORT_TYPE, Constants.COMPANY_REPORT_CONFIG_AR);
                paramsMap.put("type", Constants.globalFields);
                paramsMap.put("onlyVisible", false);
                    JSONObject propertiesObject = companyReportConfigurationService.getTypeFormatByReportType(paramsMap);
                    JSONArray propertiesArray = propertiesObject.getJSONArray("data");
                    
                    for (int i = 0; i < list.size(); i++) {
                        Object[] details = (Object[]) list.get(i);
                        JSONObject detailObj = new JSONObject();
                        JSONObject tempObj = null;
                        for (int j = 0; j < propertiesArray.length(); j++) {
                            tempObj = propertiesArray.getJSONObject(j);
                            if (tempObj.optString("format", "").equals("number") && details[j] != null && !details[j].toString().trim().isEmpty()) {
                                detailObj.put(tempObj.getString("header"), authHandler.round(Double.parseDouble(details[j].toString()), companyid));
                            } else {
                                detailObj.put(tempObj.getString("header"), details[j]);
                            }
                        }
                        
                        amountInBase =authHandler.round(detailObj.optDouble("Amt_Base"),companyid) - authHandler.round(detailObj.optDouble("knockOffAmountInBase"),companyid);
                        
                        if(StringUtil.isNullOrEmpty(detailObj.optString("REF_ID","")) || amountInBase == 0.0)
                        {
                            continue;
                        }
                        
                        transactionObj = getTransactionJSON(dateFormat,detailObj,invoiceRequestParams,requestJobj);
                        
                        allTransactionArr.put(transactionObj);
                        
//                        Map<String, JSONArray> jArrMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(allTransactionArr, InvoiceConstants.personinfo);
                    }
//                    mainObj.put("data",allTransactionArr);
            }     
            }catch (ParseException ex) {
                    Logger.getLogger(AccTemplateReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
            
            return allTransactionArr;
    }
    
    
    public JSONObject getTransactionJSON(DateFormat dateFormat,JSONObject detailObj,Map<String, Object> invoiceRequestParams,JSONObject params) throws ParseException, SessionExpiredException, JSONException{

        JSONObject Jobj = new JSONObject();
        String companyid = (String) invoiceRequestParams.get(Constants.companyKey);
        
        boolean ignoreZero = Boolean.parseBoolean(invoiceRequestParams.get("ignorezero").toString());
//        boolean agedReport = (request.containsKey("agedReport") && request.get("agedReport") != null) ? Boolean.parseBoolean(request.get("agedReport").toString()) : false;
        boolean agedReport = params.optBoolean("agedReport",false);
        int datefilter = params.has("datefilter") ? params.optInt("datefilter", 0) : 0;
//        DateFormat dateFormat = authHandler.getDateOnlyFormat(request);
        Date startDate = null;
        if (invoiceRequestParams.containsKey(Constants.REQ_startdate) && invoiceRequestParams.get(Constants.REQ_startdate) != null) {

            startDate = dateFormat.parse(invoiceRequestParams.get(Constants.REQ_startdate).toString());

        }
 
        DateFormat df = (DateFormat) invoiceRequestParams.get("df");
        DateFormat sqlDF = new SimpleDateFormat("yyyy-MM-dd");
        Boolean isAgedDetailsReport = invoiceRequestParams.containsKey("isAgedDetailsReport") ? (boolean) (invoiceRequestParams.get("isAgedDetailsReport")):false;
        
        Calendar oneDayBeforeCal1 = (Calendar) invoiceRequestParams.get("oneDayBeforeCal1");
        Calendar cal1 = (Calendar) invoiceRequestParams.get("cal1");
        Calendar cal2 = (Calendar) invoiceRequestParams.get("cal2");
        Calendar cal3 = (Calendar) invoiceRequestParams.get("cal3");
        Calendar cal4 = (Calendar) invoiceRequestParams.get("cal4");
        Calendar cal5 = (Calendar) invoiceRequestParams.get("cal5");
        Calendar cal6 = (Calendar) invoiceRequestParams.get("cal6");
        Calendar cal7 = (Calendar) invoiceRequestParams.get("cal7");

        Date oneDayBeforeCal1Date = null;
        Date cal1Date = null;
        Date cal2Date = null;
        Date cal3Date = null;
        Date cal4Date = null;
        Date cal5Date = null;
        Date cal6Date = null;
        Date cal7Date = null;

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
        double accruedbalance = 0;

        
        
        Jobj.put(Constants.billid, detailObj.optString("REF_ID"));
        Jobj.put("isOpeningBalanceTransaction", detailObj.optBoolean("Opening_Trans"));
        Jobj.put(Constants.companyKey, detailObj.optString("Company_Id"));
        Jobj.put("companyname", detailObj.optString("Company_Name"));
        Jobj.put(InvoiceConstants.personid, detailObj.optString("Cust_Id"));
        Jobj.put("customername", detailObj.optString("Cust_Name"));
        Jobj.put("customercode", detailObj.optString("Cust_Code"));
        Jobj.put(InvoiceConstants.CustomerCreditTerm, detailObj.optString("Term_Name"));
        Jobj.put(InvoiceConstants.aliasname, detailObj.optString("Cust_Alise"));
//                Jobj.put("accid", );
//        Jobj.put("type", Constants.CUSTOMER_INVOICE);
        Jobj.put(InvoiceConstants.billno, detailObj.optString("DOC_NUMBER"));
        Jobj.put(Constants.currencyKey, detailObj.optString("Trans_Curr"));
        Jobj.put(InvoiceConstants.currencysymbol, detailObj.optString("Trans_CurrSymbol"));
        Jobj.put(InvoiceConstants.currencyname, detailObj.optString("Trans_CurrName"));
        Jobj.put("externalcurrencyrate", detailObj.optDouble("Ext_Curr_Rate"));
        Jobj.put("exchangerate", detailObj.optString("ExcahgeRate"));
//        String invCreationDate = StringUtil.isNullOrEmpty(detailObj.optString("INV_Createdon")) ?"":sqlDF.formate(detailObj.getString("INV_Createdon"));
        String invCreationDate = (String)(StringUtil.isNullOrEmpty(detailObj.optString("Doc_Createdon")) ?"":df.format(sqlDF.parse(detailObj.getString("Doc_Createdon"))));
        Jobj.put("date", invCreationDate);
        Jobj.put("dateinuserformat", invCreationDate);
        Jobj.put(Constants.shipdate, (StringUtil.isNullOrEmpty(detailObj.optString("Ship_Date")) || detailObj.optString("Ship_Date").equals(" ")) ? "" : df.format(sqlDF.parse(detailObj.getString("Ship_Date"))));
        Jobj.put(Constants.duedate,(StringUtil.isNullOrEmpty(detailObj.optString("Due_Date")) || detailObj.optString("Due_Date").equals(" ")) ? "": df.format(sqlDF.parse(detailObj.getString("Due_Date"))));
        Jobj.put("duedateInUserDateFormat",(StringUtil.isNullOrEmpty(detailObj.optString("Due_Date")) || detailObj.optString("Due_Date").equals(" ")) ? "": df.format(sqlDF.parse(detailObj.getString("Due_Date"))));
        Jobj.put(InvoiceConstants.personname, detailObj.optString("Cust_Name"));
//        Jobj.put(InvoiceConstants.personinfo, details[12] + "(" + details[24] + ")");
        Jobj.put("customercurrencyid", detailObj.optString("Cust_Curr"));
        Jobj.put("entryno", detailObj.optString("JE_ENTRYNO"));
        Jobj.put("salespersonname", detailObj.optString("Sale_Per_Name"));
        Jobj.put("memo", detailObj.optString("MEMO"));
        Jobj.put("salespersoncode", detailObj.optString("Sale_Per_Code"));
        Jobj.put("salespersoninfo", detailObj.optString("Sale_Per_Name") + "(" + detailObj.optString("Sale_Per_Code") + ")");
        Jobj.put("salespersonid", detailObj.optString("Sale_Per_Id"));
        Jobj.put("personinfo", detailObj.optString("Cust_Name")+"("+detailObj.optString("Cust_Code")+")");
        Jobj.put("cntype", detailObj.optString("NOTE_TYPE"));
        Jobj.put("noteno", detailObj.optString("DOC_NUMBER"));
        Jobj.put("noteid", detailObj.optString("REF_ID"));//Unable to view CN/DN document.
        
        double amountdueinbase = detailObj.optDouble("Amt_Base") - detailObj.optDouble("knockOffAmountInBase");
        amountdueinbase = authHandler.round(amountdueinbase, companyid);
        double amountdue = detailObj.optDouble("Amt") - detailObj.optDouble("knockOffAmount");
        amountdue = authHandler.round(amountdue, companyid);

        Jobj.put(InvoiceConstants.amountdueinbase, amountdueinbase);

        Jobj.put("amountdue", authHandler.round(amountdue, companyid));
        
        
        Jobj.put("isCN", false);
        Jobj.put("isRP", false);
        
        //Check For CN And RP (-ve Amount)
        if(detailObj.optString("Type").equals("Credit Note")){

            Jobj.put("isCN", true);

            if (agedReport) {//aged report view case
                amountdue = -amountdue;
                Jobj.put(InvoiceConstants.amountdueinbase, -amountdueinbase);
                Jobj.put("amountdue", -authHandler.round(amountdue, companyid));
            }

        } else if (detailObj.optString("Type").equals("Payment Received")) {

            Jobj.put("isRP", true);

            if (agedReport) {//aged report view case
                amountdue = -amountdue;
                Jobj.put(InvoiceConstants.amountdueinbase, -amountdueinbase);
                Jobj.put("amountdue", -authHandler.round(amountdue, companyid));
            }

        }
        
        Date dueDate = null;

        if (datefilter == 0) {
            dueDate = sqlDF.parse(detailObj.getString("Due_Date"));
        } else {
            dueDate = sqlDF.parse(detailObj.getString("Doc_Createdon"));
        }

        if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
            accruedbalance = authHandler.round(amountdue, companyid);
        } else if (dueDate.after(oneDayBeforeCal1Date) && (dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
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
        Jobj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));//XX
        Jobj.put("amount", authHandler.round(detailObj.optDouble("Amt"), companyid));   //actual invoice amount

        Jobj.put("amountdue1", amountdue1);
        Jobj.put("amountdue2", amountdue2);
        Jobj.put("amountdue3", amountdue3);
        Jobj.put("amountdue4", amountdue4);
        Jobj.put("amountdue5", amountdue5);
        Jobj.put("amountdue6", amountdue6);
        Jobj.put("amountdue7", amountdue7);
        Jobj.put("amountdue8", amountdue8);
        Jobj.put(InvoiceConstants.ACCRUEDBALANCE, accruedbalance);
        Jobj.put("type", detailObj.optString("Type"));


        return Jobj;

    }
    
    
    
}
