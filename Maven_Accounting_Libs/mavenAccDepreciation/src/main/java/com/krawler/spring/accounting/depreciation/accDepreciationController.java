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
package com.krawler.spring.accounting.depreciation;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accAccountHandler;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class accDepreciationController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accDepreciationDAO accDepreciationObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private exportMPXDAOImpl exportDaoObj;
    private String successView;
    private MessageSource messageSource;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccDepreciationDAO(accDepreciationDAO accDepreciationObj) {
        this.accDepreciationObj = accDepreciationObj;
    }
    
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public ModelAndView saveAccountDepreciation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ADD_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveAccountDepreciation(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.dep.done", null, RequestContextUtils.getLocale(request));   //"Depreciation has been done successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveAccountDepreciation(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            Calendar Cal = Calendar.getInstance();
            Cal.setTime(new Date());
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String sequenceformat = request.getParameter("sequenceformat");
//            KWLCurrency currency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", companyid);
            KwlReturnObject cap = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            DateFormat df=authHandler.getDateOnlyFormat();
            if (preferences.getDepereciationAccount() == null) {
                throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
            }
            String accountid = request.getParameter("accountid");
            Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);
            if (jArr.length() > 0) {
                HashMap<String, Object> ddMap;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
//                    DepreciationDetail dd = new DepreciationDetail();
//                    Account account = (Account) session.get(Account.class, request.getParameter("accountid"));
                    double perioddepreciation = Double.parseDouble(StringUtil.DecodeText(jobj.optString("perioddepreciation")));
//                    dd.setAccount(account);
//                    dd.setPeriod(Integer.parseInt(jobj.getString("period")));
//                    dd.setCompany(company);

//                    JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), Cal.getTime(),
//                            request.getParameter("memo"), entryNumber, currency.getCurrencyID(), hs, request);
                    String jeentryNumber = "";
                    boolean jeautogenflag = false;
                    String jeIntegerPart = "";
                    String jeDatePrefix = "";
                    String jeAfterDatePrefix = "";
                    String jeDateSuffix = "";
                    String jeSeqFormatId = "";

                    String date=df.format(Cal.getTime());
                    Date entrtyDate;
                    try{
                        entrtyDate=df.parse(date);
                    }catch(ParseException ex){
                        entrtyDate = Cal.getTime();
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
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entrtyDate);
                        jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                        jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeAfterDatePrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        jeSeqFormatId = format.getID();
                        jeautogenflag = true;
                    }

                    Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeAfterDatePrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                    jeDataMap.put("entrydate", entrtyDate);
                    jeDataMap.put("companyid", companyid);
                    jeDataMap.put("memo", request.getParameter("memo"));
                    jeDataMap.put("currencyid", account.getCurrency().getCurrencyID());
                    jeDataMap.put(CCConstants.JSON_costcenterid, request.getParameter("costcenter"));
                    HashSet jeDetails = new HashSet();
                    KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
                    JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                    String jeid = journalEntry.getID();
                    jeDataMap.put("jeid", jeid);

//                    HashSet hs = new HashSet();
//                    JournalEntryDetail jed = new JournalEntryDetail();
                    if (perioddepreciation >= 0) {
//                        jed = new JournalEntryDetail();
//                        jed.setCompany(company);
//                        jed.setAmount(perioddepreciation);
//                        jed.setAccount(account);
//                        jed.setDebit(false);
//                        hs.add(jed);
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", perioddepreciation);
                        jedjson.put("accountid", accountid);
                        jedjson.put("debit", false);
                        jedjson.put("jeid", jeid);
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jeDetails.add(jed);

//                        jed = new JournalEntryDetail();
//                        jed.setCompany(company);
//                        jed.setAmount(perioddepreciation);
//                        jed.setAccount(preferences.getDepereciationAccount());
//                        jed.setDebit(true);
//                        hs.add(jed);
                        jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", perioddepreciation);
//                        jedjson.put("accountid", account.getDepreciationAccont() == null ? preferences.getDepereciationAccount().getID() : account.getDepreciationAccont().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", jeid);
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jeDetails.add(jed);
                    }

                    jeDataMap.put("jedetails", jeDetails);
                    jeDataMap.put("externalCurrencyRate", 0.0);
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
                    journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
//                    dd.setJournalEntry(journalEntry);
//                    session.saveOrUpdate(dd);
                    ddMap = new HashMap<String, Object>();
                    ddMap.put("accountid", accountid);
                    ddMap.put("period", Integer.parseInt(StringUtil.DecodeText(jobj.optString("period"))));
                    ddMap.put("companyid", companyid);
                    ddMap.put("jeid", jeid);
//                    dd.setPeriodAmount(perioddepreciation);
//                    dd.setAccumulatedAmount(jobj.optDouble("accdepreciation", 0));
//                    dd.setNetBookValue(jobj.optDouble("netbookvalue", 0));
                    ddMap.put("periodamount", perioddepreciation);
                    ddMap.put("accamount", jobj.optDouble("accdepreciation", 0));
                    ddMap.put("netbookvalue", jobj.optDouble("netbookvalue", 0));

                    KwlReturnObject ddresult = accDepreciationObj.addDepreciationDetail(ddMap);
                    DepreciationDetail dd = (DepreciationDetail) ddresult.getEntityList().get(0);
                }
            }
        } /*catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
        } */catch (JSONException ex) {
            throw ServiceException.FAILURE("saveAccountDepreciation : " + ex.getMessage(), ex);
        }
    }

    public ModelAndView getAccountDepreciation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            int depreciationMethod = Integer.parseInt(request.getParameter("depreciationmethod"));
            if (depreciationMethod == 1) {
                jobj = getAccountDepreciation(request);
            } else {
                jobj = getDoubleDeclineDepreciation(request);
            }
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getDoubleDeclineDepreciation(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
            String accountid = request.getParameter("accid");
            Calendar startcal = Calendar.getInstance();
            Calendar endcal = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();
            Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);
            DateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
            Date creationDate = account.getCreationDate();
            double openingbalance = account.getPresentValue();
            double balance = openingbalance;
            double life = account.getLife();
            double salvage = account.getSalvage();
            if (balance == 0) {
                jobj.put("data", "");
                return jobj;
            }
            double depreciationPercent = calDoubleDepreciationPercent(openingbalance, life * 12);
            double depreciationPercentValue = depreciationPercent;
            double accDepreciation = 0;
            JSONArray finalJArr = new JSONArray();
            cal.setTime(creationDate);
            double firstPeriodAmt = 0;
            double postedAccAmt = 0;
            for (int j = 0; j < life; j++) {
                for (int i = 0; i < 12; i++) {
                    int period = (12 * j) + i + 1;
                    double periodDepreciation = getFormatedNumber(balance * depreciationPercent / 100);
                    accDepreciation += periodDepreciation;
                    balance -= periodDepreciation;
                    if (period > life * 12) {
                        break;
                    }

                    if (balance < salvage) {
                        periodDepreciation += balance - salvage;
                        accDepreciation += balance - salvage;
                        balance = salvage;
                        depreciationPercentValue = (periodDepreciation / (balance + periodDepreciation)) * 100;
                    }
                    firstPeriodAmt = periodDepreciation;
                    JSONObject finalObj = new JSONObject();
                    startcal.setTime(creationDate);
                    endcal.setTime(creationDate);
                    startcal.add(Calendar.YEAR, j);
                    endcal.add(Calendar.YEAR, j);
                    startcal.set(Calendar.MONTH, i + cal.get(Calendar.MONTH));
                    endcal.set(Calendar.MONTH, i + 1 + cal.get(Calendar.MONTH));
                    startcal.set(Calendar.HOUR, 0);
                    startcal.set(Calendar.MINUTE, 0);
                    startcal.set(Calendar.SECOND, 0);
                    endcal.set(Calendar.HOUR, 0);
                    endcal.set(Calendar.MINUTE, 0);
                    endcal.set(Calendar.SECOND, 0);
                    HashMap<String, Object> filters = new HashMap<String, Object>();
                    filters.put("period", period);
                    filters.put("accountid", accountid);
                    filters.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    KwlReturnObject dresult = accDepreciationObj.getDepreciation(filters);
                    Iterator itrcust = dresult.getEntityList().iterator();
                    if (itrcust.hasNext()) {
                        DepreciationDetail dd = (DepreciationDetail) itrcust.next();
                        if (period - 1 == cal.get(Calendar.MONTH)) {
                            firstPeriodAmt = dd.getPeriodAmount();
                        }
                        finalObj.put("perioddepreciation", dd.getPeriodAmount());
                        finalObj.put("accdepreciation", dd.getAccumulatedAmount());
                        finalObj.put("netbookvalue", dd.getNetBookValue());
                        finalObj.put("isje", true);
                        finalObj.put("depdetailid", dd.getID());
                        postedAccAmt = accDepreciation - dd.getAccumulatedAmount();
                    } else {
                        if (postedAccAmt > 0) {
                            finalObj.put("perioddepreciation", periodDepreciation + postedAccAmt);
                            postedAccAmt = 0;
                        } else {
                            finalObj.put("perioddepreciation", periodDepreciation);
                        }
                        finalObj.put("accdepreciation", accDepreciation);
                        finalObj.put("netbookvalue", balance);
                        finalObj.put("isje", false);
                        finalObj.put("depdetailid", "");
                    }
                    finalObj.put("period", period);
                    finalObj.put("firstperiodamt", firstPeriodAmt);
                    finalObj.put("frommonth", sdf.format(startcal.getTime()));
                    finalObj.put("tomonth", sdf.format(endcal.getTime()));
                    finalObj.put("currencyid", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID()));
                    finalObj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
                    finalObj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
                    String depreciationPercentString = getFormatedNumber(depreciationPercentValue) + "%";
                    finalObj.put("depreciatedPercent", depreciationPercentString);
                    finalJArr.put(finalObj);
                }
            }
            jobj.put("data", finalJArr);

        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("getAccountDepreciation : " + ne.getMessage(), ne);
        } catch (JSONException jse) {
            throw ServiceException.FAILURE("getAccountDepreciation : " + jse.getMessage(), jse);
        } catch (Exception jse) {
            throw ServiceException.FAILURE("getAccountDepreciation : " + jse.getMessage(), jse);
        }
        return jobj;
    }

    public JSONObject getAccountDepreciation(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
//            JSONObject jobj = CompanyHandler.getAccounts(session, request);
//            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
//            KwlReturnObject result = accAccountDAOobj.getAccounts(requestParams);
//            List list = result.getEntityList();
//            jobj = accAccountHandler.getAccountJson(request, list);
//            jobj.put("count", result.getRecordTotalCount());
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
            String accountid = request.getParameter("accid");
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            Calendar startcal = Calendar.getInstance();
            Calendar endcal = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();
//            Account account=(Account) session.get(Account.class, accountid);
            Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);
            DateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
            Date creationDate = account.getCreationDate();
            double openingbalance = account.getPresentValue();      // account.getOpeningBalance();  Neeraj
            double balance = openingbalance;//account.getPresentValue();
            double life = account.getLife();
            double salvage = account.getSalvage();
            if (balance == 0) {
                jobj.put("data", "");
                return jobj;
            }
            double periodDepreciation = calMonthwiseDepreciation(openingbalance, salvage, life * 12);
            double accDepreciation = 0;
            JSONArray finalJArr = new JSONArray();
            cal.setTime(creationDate);
            double firstPeriodAmt = periodDepreciation;
            double postedAccAmt = 0;
            for (int j = 0; j < life; j++) {
                for (int i = 0; i < 12; i++) {
                    int period = (12 * j) + i + 1;
                    accDepreciation += periodDepreciation;
                    balance -= periodDepreciation;
                    if (balance - salvage < -0.01 || (openingbalance == salvage && period > life * 12)) {
                        break;
                    }

                    if (balance - periodDepreciation - salvage < -0.01) {
                        periodDepreciation += balance - salvage;
                        accDepreciation += balance - salvage;
                        balance = salvage;
                    }
                    JSONObject finalObj = new JSONObject();
                    startcal.setTime(creationDate);
                    endcal.setTime(creationDate);
                    startcal.add(Calendar.YEAR, j);
                    endcal.add(Calendar.YEAR, j);
                    startcal.set(Calendar.MONTH, i + cal.get(Calendar.MONTH));
                    endcal.set(Calendar.MONTH, i + 1 + cal.get(Calendar.MONTH));
                    startcal.set(Calendar.HOUR, 0);
                    startcal.set(Calendar.MINUTE, 0);
                    startcal.set(Calendar.SECOND, 0);
                    endcal.set(Calendar.HOUR, 0);
                    endcal.set(Calendar.MINUTE, 0);
                    endcal.set(Calendar.SECOND, 0);
//                    ArrayList params = new ArrayList();
//                    params.add(period);
//                    params.add(accountid);
//                    params.add(AuthHandler.getCompanyid(request));
//                    String query = "from DepreciationDetail where period=? and account.ID=? and company.companyID=?";
//                    Iterator itrcust = executeQuery(session, query, params.toArray()).iterator();
                    HashMap<String, Object> filters = new HashMap<String, Object>();
                    filters.put("period", period);
                    filters.put("accountid", accountid);
                    filters.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    KwlReturnObject dresult = accDepreciationObj.getDepreciation(filters);
                    Iterator itrcust = dresult.getEntityList().iterator();
                    if (itrcust.hasNext()) {
                        DepreciationDetail dd = (DepreciationDetail) itrcust.next();
                        if (period - 1 == cal.get(Calendar.MONTH)) {
                            firstPeriodAmt = dd.getPeriodAmount();
                        }
                        finalObj.put("perioddepreciation", dd.getPeriodAmount());
                        finalObj.put("accdepreciation", dd.getAccumulatedAmount());
                        finalObj.put("netbookvalue", dd.getNetBookValue());
                        finalObj.put("isje", true);
                        finalObj.put("depdetailid", dd.getID());
                        postedAccAmt = accDepreciation - dd.getAccumulatedAmount();
                    } else {
                        if (postedAccAmt > 0) {
                            finalObj.put("perioddepreciation", periodDepreciation + postedAccAmt);
                            postedAccAmt = 0;
                        } else {
                            finalObj.put("perioddepreciation", periodDepreciation);
                        }
                        finalObj.put("accdepreciation", accDepreciation);
                        finalObj.put("netbookvalue", balance);
                        finalObj.put("isje", false);
                        finalObj.put("depdetailid", "");
                    }
                    finalObj.put("period", period);
                    finalObj.put("firstperiodamt", firstPeriodAmt);
                    finalObj.put("frommonth", sdf.format(startcal.getTime()));
                    finalObj.put("tomonth", sdf.format(endcal.getTime()));
                    finalObj.put("currencyid", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID()));
                    finalObj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
                    finalObj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
                    finalJArr.put(finalObj);
                }
            }
            jobj.put("data", finalJArr);

        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("getAccountDepreciation : " + ne.getMessage(), ne);
        } catch (JSONException jse) {
            throw ServiceException.FAILURE("getAccountDepreciation : " + jse.getMessage(), jse);
        } catch (Exception jse) {
            throw ServiceException.FAILURE("getAccountDepreciation : " + jse.getMessage(), jse);
        }
        return jobj;
    }

    public double calMonthwiseDepreciation(double openingbalance, double salvage, double month) throws ServiceException {
        double amount;
        try {
            amount = (openingbalance - salvage) / month;
        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("calMonthwiseDepreciation : " + ne.getMessage(), ne);
        }
        return amount;
    }

    public double getFormatedNumber(double number) {
        NumberFormat nf = new DecimalFormat("0.00");
        String formatedStringValue = nf.format(number);
        double formatedValue = Double.parseDouble(formatedStringValue);
        return formatedValue;
    }
    /*
     * this method returns double depeciation percent for calculating
     * deprication of fixed asset. as given in WIKIPEDIA named as Declining
     * Balance Method
     */

    public double calDoubleDepreciationPercent(double openingbalance, double month) throws ServiceException {
        double depreciationPercent = 0d;
        double doubleDepreciationPercent = 0d;
        try {
            double oneMonthDepriciationPercent = ((openingbalance / month) / openingbalance) * 100;
            doubleDepreciationPercent = oneMonthDepriciationPercent * 2;
            doubleDepreciationPercent = getFormatedNumber(doubleDepreciationPercent);

        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("calMonthwiseDepreciation : " + ne.getMessage(), ne);
        }
        return doubleDepreciationPercent;
    }

    public ModelAndView exportAccountDepreciation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            int depreciationmethod = Integer.parseInt(request.getParameter("depreciationmethod"));
            if (depreciationmethod == 1) {
                jobj = getAccountDepreciation(request);
            } else {
                jobj = getDoubleDeclineDepreciation(request);
            }
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView saveAssetDetail(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ADD_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveAssetDetail(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.AssetDetailssavedsuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject saveAssetDetail(HttpServletRequest request) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("purchaseJe", request.getParameter("purchaseId"));
            requestParams.put("id", request.getParameter("fixedAssetID"));
            requestParams.put("isSale", false);
            requestParams.put("isWriteOff", false);
            requestParams.put("depreciationMethod", request.getParameter("depreciationmethod"));
            accDepreciationObj.addAssetDetail(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    public ModelAndView saveAssetDepriationMethod(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ADD_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveAssetDepriationMethod(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.AssetDetailssavedsuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject saveAssetDepriationMethod(HttpServletRequest request) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("id", request.getParameter("fixedAssetID"));
            requestParams.put("depreciationMethod", request.getParameter("depreciationmethod"));
            accDepreciationObj.addAssetDetail(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    public ModelAndView getPurchaseAccount(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "", purchaseAccount = "", purchaseAccName = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ADD_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", request.getParameter("fixedAssetID"));
            KwlReturnObject res = accDepreciationObj.getAsset(requestParams);
            if (res != null && res.getEntityList().get(0) != null) {
                Asset asset = (Asset) res.getEntityList().get(0);
                res = accJournalEntryobj.getJournalEntryDetail(asset.getPurchaseJe().getID(), sessionHandlerImpl.getCompanyid(request));
                if (res != null && res.getEntityList().get(0) != null) {
                    Iterator iterator = res.getEntityList().iterator();
                    while (iterator.hasNext()) {
                        JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                        if (!jed.isDebit()) {
                            purchaseAccount = jed.getAccount().getID();
                            purchaseAccName = jed.getAccount().getName();
                            break;
                        }
                    }
                }
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.field.AssetPurchaseAccount", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("purchaseAccName", purchaseAccName);
                jobj.put("purchaseAccount", purchaseAccount);
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDepreciationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
