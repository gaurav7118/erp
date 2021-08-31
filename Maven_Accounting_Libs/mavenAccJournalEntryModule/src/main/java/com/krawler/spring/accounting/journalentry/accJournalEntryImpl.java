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

import com.krawler.common.admin.*;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.AccJECustomData;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.ExchangeRateDetails;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import static com.krawler.spring.accounting.journalentry.JournalEntryConstants.ACCOUNTID;
import static com.krawler.spring.accounting.journalentry.JournalEntryConstants.AMOUNT;
import static com.krawler.spring.accounting.journalentry.JournalEntryConstants.DEBIT;
import static com.krawler.spring.accounting.journalentry.JournalEntryConstants.DESCRIPTION;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author krawler
 */
public class accJournalEntryImpl extends BaseDAO implements accJournalEntryDAO, JournalEntryConstants {

    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accAccountDAO accAccountDAOobj;
     private fieldManagerDAO fieldManagerDAOobj;

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
     public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
     
    public KwlReturnObject getJEDfromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from JournalEntryDetail jed where account.ID=? and jed.journalEntry.deleted=false and jed.company.companyID=?";
        list = executeQuery( q, new Object[]{accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getJECountForEdit(String jeno, String companyid,String jeId) throws ServiceException {
        List list = new ArrayList();
        String q = "from JournalEntry where entryNumber=? and company.companyID=? and ID!=? ";
        list = executeQuery( q, new Object[]{jeno, companyid,jeId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public String updateJEEntryNumberForNA(String jeId, String entrynumber) throws ServiceException {
        try {
            String q = "update JournalEntry set entryNumber=? where ID=?";
            int numRows = executeUpdate( q, new Object[]{entrynumber, jeId});
        } catch (Exception e) {
            System.out.println(e);
        }
        return entrynumber;
    }
    
    public KwlReturnObject getJECount(String jeno, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from JournalEntry where entryNumber=? and company.companyID=?";
        list = executeQuery( q, new Object[]{jeno, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getJEDset(JSONArray JArr, String companyid,JournalEntry je) throws ServiceException {
        List list = new ArrayList();
        try {
            HashSet hs = new HashSet();
            if (je.getTypeValue() == 3) {           // typeValue: 3 Fund Transfer JE
                JArr = StringUtil.sortJsonArray(JArr, "debit", false, false);
            }
            for (int i = 0; i < JArr.length(); i++) {
                JSONObject jobj = JArr.getJSONObject(i);
                JournalEntryDetail jed = new JournalEntryDetail();
                jed.setSrno(i + 1);
                jed.setCompany((Company) get(Company.class, companyid));
                if (jobj.has(AMOUNT)) {
                    jed.setAmount(jobj.getDouble(AMOUNT));
                }
                if (jobj.has(ACCOUNTID)) {
                    jed.setAccount((Account) get(Account.class, jobj.getString(ACCOUNTID)));
                }
                if (jobj.has(DEBIT)) {
                    jed.setDebit(jobj.getBoolean(DEBIT));
                }
                if (jobj.has(DESCRIPTION)) {
                    String desc = "";
                    if (!jobj.getString(DESCRIPTION).equals("")) {
                        try {
                            desc = StringUtil.DecodeText(jobj.optString(DESCRIPTION));
                        } catch (IllegalArgumentException ie) {
                            desc = jobj.getString(DESCRIPTION);
                        }
                    }
                    jed.setDescription(desc);
                }
                if (jobj.has("srno")) {
                    jed.setSrno(jobj.getInt("srno"));
                }
                if (jobj.has("accountpersontype") && !StringUtil.isNullOrEmpty(jobj.getString("accountpersontype"))) {//1-Customer , 2-Vendor
                    if (!StringUtil.isNullOrEmpty(jobj.getString("accountpersontype"))) {
                        jed.setAccountpersontype(jobj.getInt("accountpersontype"));
                    }
                }
                if (jobj.has("accountpersontype")) {
                    jed.setCustomerVendorId(jobj.optString("customerVendorId", ""));
                }
                               
                if (jobj.has("isbankcharge")) {    //If Bank Charge include, set TRUE
                    jed.setBankcharge(jobj.getBoolean("isbankcharge"));
                }
                if (jobj.has("isbankcharge")) {    //If Bank Charge include, set TRUE
                    jed.setBankcharge(jobj.getBoolean("isbankcharge"));
                }
                if(jobj.has("exchangeratefortransaction")){
                    jed.setExchangeRateForTransaction(jobj.optDouble("exchangeratefortransaction", 1));
                }
                if(jobj.has("appliedGst")) {                
                    KwlReturnObject taxResult = accountingHandlerDAOobj.getObject(Tax.class.getName(), jobj.optString("appliedGst"));
                    Tax tax = (Tax)taxResult.getEntityList().get(0);
                    jed.setGstapplied(tax);
                }
                if(je!=null){
                    jed.setJournalEntry(je);
                }
                hs.add(jed);
            }
            list.add(hs);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getJEDset : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getJEDsetCNDN(JSONArray JArr, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            HashSet hs = new HashSet();
            for (int i = 0; i < JArr.length(); i++) {
                JSONObject jobj = JArr.getJSONObject(i);
                JournalEntryDetail jed = new JournalEntryDetail();
                jed.setSrno(i + 1);
                jed.setCompany((Company) get(Company.class, companyid));
                if (jobj.has(DEBIT) && jobj.get(DEBIT).equals("Debit") && jobj.has("d_amount")) {
                    jed.setAmount(jobj.getDouble("d_amount"));
                }
                if (jobj.has(DEBIT) && jobj.get(DEBIT).equals("Credit") && jobj.has("c_amount")) {
                    jed.setAmount(jobj.getDouble("c_amount"));
                }
                if (jobj.has(ACCOUNTID)) {
                    jed.setAccount((Account) get(Account.class, jobj.getString(ACCOUNTID)));
                }
                if (jobj.has(DEBIT) && jobj.get(DEBIT).equals("Debit")) {
                    jed.setDebit(true);
                } else {
                    jed.setDebit(false);
                }
                if (jobj.has(DESCRIPTION)) {
                    String desc = "";
                    if (!jobj.getString(DESCRIPTION).equals("")) {
                        desc = StringUtil.DecodeText(jobj.optString(DESCRIPTION));
                    }
                    jed.setDescription(desc);
                }
                if (jobj.has("srno")) {
                    jed.setSrno(jobj.getInt("srno"));
                }
                hs.add(jed);
            }
            list.add(hs);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getJEDset : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getJEDsetForRevaluation(String companyid, String accountId, String proflossaccountId, double amount, int accTypeId) throws ServiceException {
        List list = new ArrayList();
        try {
            HashSet hs = new HashSet();
            double newamount = amount;
            if (amount < 0) {
                newamount = -(amount);
            }
            JournalEntryDetail jed = new JournalEntryDetail();
            jed.setSrno(1);
            jed.setCompany((Company) get(Company.class, companyid));
            jed.setAmount(newamount);
            jed.setAccount((Account) get(Account.class, accountId));
            if (accTypeId == Constants.VENDOR) {
                if (amount > 0) {
                    jed.setDebit(false);
                } else {
                    jed.setDebit(true);
                }
            } else if (accTypeId == Constants.CUSTOMER) {
                if (amount > 0) {
                    jed.setDebit(true);
                } else {
                    jed.setDebit(false);
                }
            } else if (accTypeId == Group.ACCOUNTTYPE_BANK || accTypeId==Constants.CashInHandAccountTye) {
                if (amount > 0) {
                    jed.setDebit(true);
                } else {
                    jed.setDebit(false);
                }
            }
            jed.setDescription("");
            hs.add(jed);

            jed = new JournalEntryDetail();
            jed.setSrno(2);
            jed.setCompany((Company) get(Company.class, companyid));
            jed.setAmount(newamount);
            jed.setAccount((Account) get(Account.class, proflossaccountId));
            if (accTypeId == Constants.VENDOR) {
                if (amount > 0) {
                    jed.setDebit(true);
                } else {
                    jed.setDebit(false);
                }
            } else if (accTypeId == Constants.CUSTOMER) {
                if (amount > 0) {
                    jed.setDebit(false);
                } else {
                    jed.setDebit(true);
                }
            } else if (accTypeId == Group.ACCOUNTTYPE_BANK || accTypeId==Constants.CashInHandAccountTye) {
                if (amount > 0) {
                    jed.setDebit(false);
                } else {
                    jed.setDebit(true);
                }
            }
            jed.setDescription("");
            hs.add(jed);

            list.add(hs);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getJEDset : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject addRevalHistory(RevaluationHistory revalueationHistory) throws ServiceException {
        List list = new ArrayList();
        saveOrUpdate(revalueationHistory);
        list.add(revalueationHistory);
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject addJournalEntryDetails(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        JournalEntryDetail jed = new JournalEntryDetail();
        JournalEntry je = null;
        Account accObj = null;
        try {
            if (json.has(JEDID)) {
                if (!StringUtil.isNullOrEmpty(json.getString(JEDID))) {
                    jed = (JournalEntryDetail) get(JournalEntryDetail.class, json.getString(JEDID));
                }
            }
            if (json.has(SRNO)) {
                jed.setSrno((Integer) json.get(SRNO));
            }
            if (json.has(AMOUNT)) {
                jed.setAmount(json.getDouble(AMOUNT));
            }
            if (json.has(DEBIT)) {
                jed.setDebit(json.getBoolean(DEBIT));
            }
            if (json.has(JEID)) {
                je = (JournalEntry) get(JournalEntry.class, json.getString(JEID));
                jed.setJournalEntry(je);
            }
            if (json.has(ACCOUNTID)) {
                accObj = (Account) get(Account.class, json.getString(ACCOUNTID));
                jed.setAccount(accObj);
            }
            if (json.has(COMPANYID)) {
                jed.setCompany((Company) get(Company.class, json.getString(COMPANYID)));
            }
            if (json.has(DESCRIPTION)) {
                jed.setDescription(StringUtil.DecodeText(json.optString(DESCRIPTION)));
            }
            if (json.has(Constants.ISSEPARATED)) {
                jed.setIsSeparated(json.optBoolean(Constants.ISSEPARATED));
            }
            if (json.has("mainjedid")) {
                jed.setMainjedid(json.optString("mainjedid"));
            }
            if (json.has("accjedetailcustomdata")) {
                jed.setAccJEDetailCustomData((AccJEDetailCustomData) get(AccJEDetailCustomData.class, (String) json.get("accjedetailcustomdata")));
            }
            if (json.has("accjedetailproductcustomdataref")) {
                jed.setAccJEDetailsProductCustomData((AccJEDetailsProductCustomData) get(AccJEDetailsProductCustomData.class, (String) json.get("accjedetailproductcustomdataref")));
            }
            if (json.has("gstCurrencyRate")) {
                jed.setGstCurrencyRate(json.getDouble("gstCurrencyRate"));
            }
            if (json.has("forexGainLoss")) {
                jed.setForexGainLoss(json.getDouble("forexGainLoss"));
            }
            if (json.has("paymentType")) {
                 jed.setPaymentType(json.optInt("paymentType",0));
            }
            if (json.has("exchangeratefortransaction")) {
                 jed.setExchangeRateForTransaction(json.optDouble("exchangeratefortransaction"));
            }
            if (json.has("setroundingdifferencedetail")) {
                 jed.setRoundingDifferenceDetail(json.optBoolean("setroundingdifferencedetail"));
            }
            
            if (je == null) {
                je = jed.getJournalEntry();
            }
            if (accObj == null) {
                accObj = jed.getAccount();
            }

            if (je != null && accObj != null) {//Set flag for eliminate JE from multi company
                boolean flag = false;
                if (accObj.isEliminateflag()) {
                    je.setEliminateflag(true);
                    flag = true;
                }
                if (accObj.isIntercompanyflag()) {
                    je.setIntercompanyflag(true);
                    flag = true;
                }
                if (flag) {
                    save(je);
                }
            }
            save(jed);
            list.add(jed);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject saveJournalEntryDetailsSet(Set<JournalEntryDetail> entryDetails) throws ServiceException {
        List list = new ArrayList();
        try {
            if (!entryDetails.isEmpty()) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                String gCurrencyId = null;
                JournalEntry je = null;
                double amount = 0.0;
                double creditAmountInBase = 0.0;
                double debitAmountInBase = 0.0;
                boolean eliminateflag = false;
                boolean intercompanyflag = false;
                KwlReturnObject bAmt = null;
                CompanyAccountPreferences pref = null;
                Company company = null;
                for (JournalEntryDetail jed : entryDetails) {
                    je = jed.getJournalEntry();
                    company = je.getCompany();
                    requestParams.put(Constants.companyKey, company.getCompanyID());
                    requestParams.put(Constants.globalCurrencyKey, company.getCurrency().getCurrencyID());
                    gCurrencyId = company.getCurrency().getCurrencyID();
                    KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), company.getCompanyID());
                    pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
                    if (pref.getRoundingDifferenceAccount() == null) {
                        throw ServiceException.FAILURE("Rounding Difference Account is not Mapped in Company Preferences", "erp11", false);
//                        throw new AccountingException("Rounding Difference Account is not Mapped in Company Preferences");
                    }
                    if (!eliminateflag && jed.getAccount() != null && jed.getAccount().isEliminateflag()) {
                        eliminateflag = true;
                    }
                    if (!intercompanyflag && jed.getAccount() != null && jed.getAccount().isIntercompanyflag()) {
                        intercompanyflag = true;
                    }

                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, jed.getAmount(), jed.getJournalEntry().getCurrency() == null ? gCurrencyId : jed.getJournalEntry().getCurrency().getCurrencyID(), jed.getJournalEntry().getEntryDate(), jed.getJournalEntry().getExternalCurrencyRate());

                    if (!jed.isIsSeparated()) {
                        if (jed.isDebit()) {
                            amount += jed.getAmount();
                            debitAmountInBase += authHandler.round((Double) bAmt.getEntityList().get(0),company.getCompanyID());
                        } else {
                            amount -= jed.getAmount();
                            creditAmountInBase += authHandler.round((Double) bAmt.getEntityList().get(0),company.getCompanyID());
                        }
                    }
                }
                double baseamountdiff= authHandler.round(Math.abs(debitAmountInBase - creditAmountInBase), company.getCompanyID());
                if (Math.abs(amount) >= 0.000001 && baseamountdiff > 0.01) {
                    throw ServiceException.FAILURE("Debit and credit amounts are not same", "erp22", false);
//                    throw new AccountingException("Debit and credit amounts are not same");
                } else if (pref.getRoundingDifferenceAccount() != null) {
                    debitAmountInBase = authHandler.round(debitAmountInBase, company.getCompanyID());
                    creditAmountInBase = authHandler.round(creditAmountInBase, company.getCompanyID());

                    if (creditAmountInBase != debitAmountInBase) {
                        double diff = creditAmountInBase - debitAmountInBase;
                        if (creditAmountInBase < debitAmountInBase) {
                            diff = diff * -1;
                        }
                        bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, diff, je.getCurrency() == null ? gCurrencyId : je.getCurrency().getCurrencyID(), je.getEntryDate(), je.getExternalCurrencyRate());
                        double currencyAmount = (Double) bAmt.getEntityList().get(0);
                        JournalEntryDetail roundJeD = new JournalEntryDetail();
                        roundJeD.setAmount(currencyAmount);
                        roundJeD.setAmountinbase(authHandler.round(diff,company.getCompanyID()));
                        if (creditAmountInBase < debitAmountInBase) {
                            roundJeD.setDebit(false);
                        } else {
                            roundJeD.setDebit(true);
                        }
                        roundJeD.setAccount(pref.getRoundingDifferenceAccount());
                        roundJeD.setCompany(je.getCompany());
                        roundJeD.setJournalEntry(je);
                        roundJeD.setRoundingDifferenceDetail(true);
                        entryDetails.add(roundJeD);
                    }
                }
            }
            saveAll(entryDetails);
            boolean success = list.addAll(entryDetails);
        } catch (Exception ex) {
            if(ex instanceof ServiceException){
                throw ex;
            }
            else{
                throw ServiceException.FAILURE(ex.getMessage(), ex);
            }
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public JournalEntryDetail getJournalEntryDetails(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        JournalEntryDetail jed = new JournalEntryDetail();
        JournalEntry je = null;
        Account accObj = null;
        try {
            if (json.has(JEDID)) {
                if (!StringUtil.isNullOrEmpty(json.getString(JEDID))) {
                    jed = (JournalEntryDetail) get(JournalEntryDetail.class, json.getString(JEDID));
                }
            }
            if (json.has(SRNO)) {
                jed.setSrno((Integer) json.get(SRNO));
            }
            if (json.has(AMOUNT)) {
                jed.setAmount(json.getDouble(AMOUNT));
            }
            if (json.has(DEBIT)) {
                jed.setDebit(json.getBoolean(DEBIT));
            }
            if (json.has(JEID)) {
                je = (JournalEntry) get(JournalEntry.class, json.getString(JEID));
                jed.setJournalEntry(je);
            }
            if (json.has(ACCOUNTID)) {
                accObj = (Account) get(Account.class, json.getString(ACCOUNTID));
                jed.setAccount(accObj);
            }
            if (json.has(COMPANYID)) {
                jed.setCompany((Company) get(Company.class, json.getString(COMPANYID)));
            }
            if (json.has(DESCRIPTION)) {
                jed.setDescription(StringUtil.DecodeText(json.optString(DESCRIPTION)));
            }
            if (json.has(Constants.ISSEPARATED)) {
                jed.setIsSeparated(json.optBoolean(Constants.ISSEPARATED));
            }
            if (json.has("accjedetailcustomdata")) {
                jed.setAccJEDetailCustomData((AccJEDetailCustomData) get(AccJEDetailCustomData.class, (String) json.get("accjedetailcustomdata")));
            }
            if (json.has("accjedetailproductcustomdataref")) {
                jed.setAccJEDetailsProductCustomData((AccJEDetailsProductCustomData) get(AccJEDetailsProductCustomData.class, (String) json.get("accjedetailproductcustomdataref")));
            }
            if (json.has("gstCurrencyRate")) {
                jed.setGstCurrencyRate(json.getDouble("gstCurrencyRate"));
            }
            if (json.has("forexGainLoss")) {
                jed.setForexGainLoss(json.getDouble("forexGainLoss"));
            }
            if (json.has("paymentType")) {
                jed.setPaymentType(json.getInt("paymentType"));
            }
            if (je == null) {
                je = jed.getJournalEntry();
            }
            if (accObj == null) {
                accObj = jed.getAccount();
            }

            if (je != null && accObj != null) {//Set flag for eliminate JE from multi company
                boolean flag = false;
                if (accObj.isEliminateflag()) {
                    je.setEliminateflag(true);
                    flag = true;
                }
                if (accObj.isIntercompanyflag()) {
                    je.setIntercompanyflag(true);
                    flag = true;
                }
                if (flag) {
                    save(je);
                }
            }
            list.add(jed);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jed;
    }

    public int saveCustomDataForRecurringJE(String New_JE_ID, String Old_JE_ID, boolean JE_OR_JED) throws ServiceException {
        int NoOFRecords = 0;
        String query = "";
        String conditionSQL = "";
        String selectColumns = "";
        String tableName = "";
        String column = "";
        try {
            for (int i = Constants.Custom_Column_Combo_start+1; i <= ( Constants.Custom_Column_Combo_start + Constants.Custom_Column_Combo_limit ); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            for (int i = Constants.Custom_Column_Master_start+1; i <=(Constants.Custom_Column_Master_start+Constants.Custom_Column_Master_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            
            for (int i = Constants.Custom_Column_User_start+1; i <= ( Constants.Custom_Column_User_start + Constants.Custom_Column_User_limit ); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            
            for (int i = Constants.Custom_Column_Normal_start +1 ; i <= ( Constants.Custom_Column_Normal_start +  Constants.Custom_Column_Normal_limit ) ; i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            for (int i = Constants.Custom_Column_Check_start + 1; i <= (Constants.Custom_Column_Check_start + Constants.Custom_Column_Check_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            conditionSQL += "acc.company,acc.deleted,acc.moduleId";
            selectColumns += "company, deleted, moduleId";
            if (JE_OR_JED) {
                tableName = "accjecustomdata";
                column = "acc.journalentryId";
                selectColumns = "journalentryId," + selectColumns;
            }
            if (!JE_OR_JED) {
                tableName = "accjedetailcustomdata";
                column = "acc.jedetailId";
                conditionSQL += ",acc.recdetailId";
                selectColumns += ",recdetailId";
                
                selectColumns = "jedetailId," + selectColumns;
            }
            query += "insert into " + tableName + " ("+selectColumns+") (select '" + New_JE_ID + "'," + conditionSQL + " from " + tableName + " as acc where " + column + "='" + Old_JE_ID + "')";
            NoOFRecords = executeSQLUpdate( query, new String[]{});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveAccJECustomData : " + ex.getMessage(), ex);
        }
        return NoOFRecords;
    }

    public KwlReturnObject updateJournalEntryDetails(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        try {
            String jedid = json.getString(JEDID);
            JournalEntryDetail jed = (JournalEntryDetail) get(JournalEntryDetail.class, jedid);
            JournalEntry je = null;
            Account accObj = null;
            if (jed != null) {
                if (json.has(SRNO)) {
                    jed.setSrno((Integer) json.get(SRNO));
                }
                if (json.has(AMOUNT)) {
                    jed.setAmount(json.getDouble(AMOUNT));
                }
                if (json.has(DEBIT)) {
                    jed.setDebit(json.getBoolean(DEBIT));
                }
                if (json.has(JEID)) {
                    je = (JournalEntry) get(JournalEntry.class, json.getString(JEID));
                    jed.setJournalEntry(je);
                }
                if (json.has(ACCOUNTID)) {
                    accObj = (Account) get(Account.class, json.getString(ACCOUNTID));
                    jed.setAccount(accObj);
                }
                if (json.has(COMPANYID)) {
                    jed.setCompany((Company) get(Company.class, json.getString(COMPANYID)));
                }
                if (json.has("accjedetailcustomdata")) {
                    jed.setAccJEDetailCustomData((AccJEDetailCustomData) get(AccJEDetailCustomData.class, (String) json.get("accjedetailcustomdata")));
                }
                if (json.has("accjedetailproductcustomdataref")) {
                    jed.setAccJEDetailsProductCustomData((AccJEDetailsProductCustomData) get(AccJEDetailsProductCustomData.class, (String) json.get("accjedetailproductcustomdataref")));
                }
                if (je == null) {
                    je = jed.getJournalEntry();
                }
                if (accObj == null) {
                    accObj = jed.getAccount();
                }

                if (je != null && accObj != null) {//Set flag for eliminate JE from multi company
                    boolean flag = false;
                    if (accObj.isEliminateflag()) {
                        je.setEliminateflag(true);
                        flag = true;
                    }
                    if (accObj.isIntercompanyflag()) {
                        je.setIntercompanyflag(true);
                        flag = true;
                    }
                    if (flag) {
                        save(je);
                    }
                }
                save(jed);
            }
            list.add(jed);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.updateJournalEntryDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updateJournalEntryDetails(Map requestMap) throws ServiceException {
        List list = new ArrayList();
        JournalEntryDetail jed = null;
        try {
            String jedid = (String) requestMap.get(JEDID);
            if (StringUtil.isNullOrEmpty(jedid)) {
                jed = new JournalEntryDetail();
            } else {
                jed = (JournalEntryDetail) get(JournalEntryDetail.class, jedid);
            }
            JournalEntry je = null;
            Account accObj = null;
            if (jed != null) {
                if (requestMap.containsKey(SRNO)) {
                    jed.setSrno((Integer) requestMap.get(SRNO));
                }
                if (requestMap.containsKey(AMOUNT)) {
                    jed.setAmount((Double) requestMap.get(AMOUNT));
                }
                if (requestMap.containsKey(DEBIT)) {
                    jed.setDebit((Boolean) requestMap.get(DEBIT));
                }
                if (requestMap.containsKey(DESCRIPTION)) {
                    jed.setDescription(StringUtil.DecodeText((String) requestMap.get(DESCRIPTION)));
                }

                if (requestMap.containsKey(JEID)) {
                    je = requestMap.get(JEID) == null ? null : (JournalEntry) get(JournalEntry.class, (String) requestMap.get(JEID));
                    jed.setJournalEntry(je);
                }
                if (requestMap.containsKey(ACCOUNTID)) {
                    accObj = requestMap.get(ACCOUNTID) == null ? null : (Account) get(Account.class, (String) requestMap.get(ACCOUNTID));
                    jed.setAccount(accObj);
                }
                if (requestMap.containsKey(COMPANYID)) {
                    Company company = requestMap.get(COMPANYID) == null ? null : (Company) get(Company.class, (String) requestMap.get(COMPANYID));
                    jed.setCompany(company);
                }
                 if (requestMap.containsKey("baddebtentryNumber")) {
                    jed.getJournalEntry().setBadDebtSeqNumber(requestMap.get("baddebtentryNumber").toString());
                }

                if (je == null) {
                    je = jed.getJournalEntry();
                }
                if (accObj == null) {
                    accObj = jed.getAccount();
                }

                if (je != null && accObj != null) {//Set flag for eliminate JE from multi company
                    boolean flag = false;
                    if (accObj.isEliminateflag()) {
                        je.setEliminateflag(true);
                        flag = true;
                    }
                    if (accObj.isIntercompanyflag()) {
                        je.setIntercompanyflag(true);
                        flag = true;
                    }
                    if (flag) {
                        save(je);
                    }
                }

                saveOrUpdate(jed);
            }
            list.add(jed);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.updateJournalEntryDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject addJournalEntry(JSONObject json, HashSet<JournalEntryDetail> details) throws ServiceException, AccountingException {
        List list = new ArrayList();
        try {
            JournalEntry je = new JournalEntry();
            je = buildJE(je, json, details);
            save(je);
            list.add(je);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("addJournalEntry : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updateJournalEntry(JSONObject json, HashSet<JournalEntryDetail> details) throws ServiceException, AccountingException {
        List list = new ArrayList();
        JournalEntry je;
        try {
            String jeid = json.getString(JEID);
            je = (JournalEntry) get(JournalEntry.class, jeid);
            je = buildJE(je, json, details);
            save(je);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateJournalEntry : " + ex.getMessage(), ex);
        }
        list.add(je);
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    private JournalEntry buildJE(JournalEntry je, JSONObject json, HashSet<JournalEntryDetail> details) throws ServiceException, AccountingException, JSONException {
        if (json.has(ENTRYNUMBER)) {
            je.setEntryNumber(json.getString(ENTRYNUMBER));
        }
        if (json.has(AUTOGENERATED)) {
            je.setAutoGenerated(json.getBoolean(AUTOGENERATED));
        }
        if (json.has(ENTRYDATE)) {
            je.setEntryDate((Date) json.get(ENTRYDATE));
        }
        if (json.has(MEMO)) {
            je.setMemo(json.getString(MEMO));
        }
        if (json.has(COMPANYID)) {
            je.setCompany((Company) get(Company.class, json.getString(COMPANYID)));
            CompanyAccountPreferences accPref = json.getString(COMPANYID) == null ? null : (CompanyAccountPreferences) get(CompanyAccountPreferences.class, json.getString(COMPANYID));
            je.setIsInventory(accPref != null ? (accPref.isWithoutInventory() ? "0" : "1") : null);
        }
        if (json.has(CURRENCYID)) {
            je.setCurrency((KWLCurrency) get(KWLCurrency.class, json.getString(CURRENCYID)));
        }
	if (json.has("pendingapproval")) {
            je.setPendingapproval(json.getInt("pendingapproval"));
        }else{
             je.setPendingapproval(0); 
        }
        if (json.has("isDraft") && !json.isNull("isDraft")) {
            je.setDraft(json.getBoolean("isDraft"));
        } else {
            je.setDraft(false);
        }
        if(json.has("transactionModuleid")){
            int transactionModuleid=json.optInt("transactionModuleid",0);
            je.setTransactionModuleid(transactionModuleid);
        }
        if(json.has("transactionId")){
            je.setTransactionId((String)json.get("transactionId"));
        }
        je.setCreatedOn(System.currentTimeMillis());
        if (!details.isEmpty()) {
            Iterator<JournalEntryDetail> itr = details.iterator();
            double amount = 0.0;
            boolean eliminateflag = false;
            boolean intercompanyflag = false;
            while (itr.hasNext()) {
                JournalEntryDetail jed = itr.next();
                jed.setJournalEntry(je);
                if (!eliminateflag && jed.getAccount() != null && jed.getAccount().isEliminateflag()) {
                    eliminateflag = true;
                }
                if (!intercompanyflag && jed.getAccount() != null && jed.getAccount().isIntercompanyflag()) {
                    intercompanyflag = true;
                }
                if (jed.isDebit()) {
                    amount += jed.getAmount();
                } else {
                    amount -= jed.getAmount();
                }
            }
            if (Math.abs(amount) >= 0.000001) {
                throw new AccountingException("Debit and credit amounts are not same");
            }
            je.setEliminateflag(eliminateflag);
            je.setIntercompanyflag(intercompanyflag);
            je.setDetails(details);
        }
        if (je.getCompany() != null && je.getDetails() != null) {
            Set jeDetails = je.getDetails();

            updateJETemplateCode(je, jeDetails.iterator(), je.getCompany().getCompanyID());
        }

        return je;
    }

    public KwlReturnObject deleteJE(String jeid, String companyid) throws ServiceException {
        //Delete Journal Entry
        String delQuery = "delete from JournalEntry je where ID=? and je.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{jeid, companyid});

        return new KwlReturnObject(true, "Journal Entry has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteJEEntry(String jeid, String companyid) throws ServiceException {
        //Delete Journal Entry
        String delQuery = "update JournalEntry je set je.deleted=true  where je.ID = ? and je.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{jeid, companyid});

        return new KwlReturnObject(true, "Journal Entry has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteJEDtails(String jeid, String companyid) throws ServiceException {
        //Delete Journal Entry details
        String delQuery = "delete from JournalEntryDetail jed where jed.journalEntry.ID=? and jed.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{jeid, companyid});

        return new KwlReturnObject(true, "JournalEntry details has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getJournalEntry(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
//            KWLCurrency currency = (KWLCurrency)get(KWLCurrency.class, (String) request.get("gcurrencyid"));
            boolean isRepeatedFlag = false;
            boolean isTemplate = false;
            boolean ispendingAproval = false;
            boolean roundingAdjustment = false; // used to adjust rounding script
            boolean updateExternalCurrencyRate = request.get("updateexternalcurrencyrate")!=null ? (Boolean)request.get("updateexternalcurrencyrate") : false; // used to update External Currency Rate from CommonFunctions.java            
            String billID = "";
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(START);
            String limit = (String) request.get(LIMIT);
            String ss = (String) request.get(SS);
            String linkentry = (String) request.get(LINKID);
            boolean deleted = request.get(DELETED)!=null ? Boolean.parseBoolean(request.get(DELETED).toString()) : false;
            boolean isUserSummaryReportFlag = (request.containsKey("isUserSummaryReportFlag") && request.get("isUserSummaryReportFlag")!=null) ? Boolean.parseBoolean(request.get("isUserSummaryReportFlag").toString()) : false;
            boolean nondeleted = request.get(NONDELETED)!=null ? Boolean.parseBoolean(request.get(NONDELETED).toString()) : false;
            String companyid = (String) request.get(COMPANYID);
//            CompanyAccountPreferences accPref = companyid == null ? null : (CompanyAccountPreferences) get(CompanyAccountPreferences.class, companyid);

            ArrayList params = new ArrayList();
            params.add(companyid);
            String condition = " where je.company.companyID=? ";
            
            String Searchjson = "";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = request.get("searchJson").toString();
            }
            boolean isKnockOffAdvancedSearch = false;
            boolean isLineDimPresentAdvSearch = false;
                    
            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                isLineDimPresentAdvSearch = !isAdvanceSearchOnGlobalDimension(Searchjson);
                isKnockOffAdvancedSearch = fieldManagerDAOobj.isKnockOffAdvancedSearch(Searchjson, companyid);
            }
            
            if (request.containsKey(Constants.isRepeatedFlag) && request.get(Constants.isRepeatedFlag) != null) {
                isRepeatedFlag = Boolean.parseBoolean((String) request.get(Constants.isRepeatedFlag));
            }
            if (request.containsKey("isTemplate") && request.get("isTemplate") != null) {
                isTemplate = Boolean.FALSE.parseBoolean(String.valueOf(request.get("isTemplate")));
            }
            if (request.containsKey("ispendingAproval") && request.get("ispendingAproval") != null) {
                ispendingAproval = Boolean.FALSE.parseBoolean(String.valueOf(request.get("ispendingAproval")));
            }
            if (request.containsKey("roundingAdjustment") && request.get("roundingAdjustment") != null) {
                roundingAdjustment = Boolean.FALSE.parseBoolean(String.valueOf(request.get("roundingAdjustment")));
            }
            if (request.containsKey("billid") && request.get("billid") != null) {
                billID = (String) request.get("billid");
            }
	    if(updateExternalCurrencyRate){
                String gcurrencyid = (String)request.get("fromcurrencyid");
                params.add(gcurrencyid);
                condition += " and je.externalCurrencyRate = 0 AND je.currency.currencyID <> ?";
            }
            if (!StringUtil.isNullOrEmpty(billID)) {
                params.add(billID);
                condition += " and je.ID = ? ";
            } else if (isTemplate) {
                params.add(companyid);
                condition += " and  je.ID in (select MT.moduleRecordId from ModuleTemplate MT where MT.moduleId=24 and MT.company.companyID=?) ";
            } else if (isRepeatedFlag) {
                condition += " and je.pendingapproval = 0 ";
            } else if(!roundingAdjustment){
                condition += " and je.pendingapproval = 0 and je.istemplate != 2 and je.draft = false ";
            }
//            if(accPref != null) {
//                params.add(accPref.isWithoutInventory()?"0":"1");
//                condition += " and (je.isInventory = ? or je.isInventory is null) ";                
//            }

            if (request.containsKey("pendingFlag") && request.get("pendingFlag") != null) {
                if (Boolean.parseBoolean(request.get("pendingFlag").toString())) {
                    condition += " and (je.approvestatuslevel BETWEEN 1 AND 10 or je.approvestatuslevel<0 ) "; // approvestatuslevel between 1 and 10 ==> Waiting for approval   and  approvestatuslevel < 0  ==> Rejected
                } else {
                    condition += " and (je.approvestatuslevel = 11) ";
                }
            }
            if (request.containsKey("groupid") && Boolean.parseBoolean(request.get("groupid").toString())) {
                params.add("12");
                params.add(companyid);
                condition += " and je.ID in (select distinct(jed.journalEntry.ID) from JournalEntryDetail jed where jed.account.group.ID=? and jed.company.companyID=?) ";
            }
            if (request.containsKey(Constants.Journal_Entry_Type) && !StringUtil.isNullOrEmpty(request.get(Constants.Journal_Entry_Type).toString())) {
                String typeValue = request.get(Constants.Journal_Entry_Type ).toString();
                condition += " and je.typeValue in ( "+typeValue+" )" ;
            }
            if (request.containsKey("GSTRType") && !StringUtil.isNullOrEmpty(request.get("GSTRType").toString())) {
                String GSTRType = request.get("GSTRType").toString();
                condition += " and je.gstrType in ( " + GSTRType + " )";
            }
            String partyJournal = (String) request.get(CCConstants.isPartyJE);
            if (!StringUtil.isNullOrEmpty(partyJournal)) {
                params.add(2);
                condition += " and je.typeValue=?";
            }
            if (request.containsKey("linknumber") && request.get("linknumber") != null && !request.get("linknumber").toString().equals("")) {
                condition += " and je.entryNumber = ? ";
                params.add(request.get("linknumber"));
            }
            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and je.costcenter.ID=?";
            }
            
            if(isUserSummaryReportFlag){
                if (!StringUtil.isNullOrEmpty((String) request.get("userid"))) {
                    params.add((String) request.get("userid"));
                    condition += " and je.createdby.userID=?";
                }
            }
            
            String userDepartment = "";
            if (request.containsKey("userDepartment") && request.get("userDepartment") != null) {
                userDepartment = (String) request.get("userDepartment");
            }
            String selectedIds = (String) request.get("selectedIds");//Geting only Selected records for Export.
            if (!StringUtil.isNullOrEmpty(selectedIds)) {
                selectedIds = AccountingManager.getFilterInString(selectedIds);
                condition += " and je.ID in " + selectedIds + " ";
            }
            String startDate = request.get(Constants.REQ_startdate)!=null? StringUtil.DecodeText((String) request.get(Constants.REQ_startdate)):(String) request.get(Constants.REQ_startdate);
            String endDate = request.get(Constants.REQ_enddate)!=null? StringUtil.DecodeText((String) request.get(Constants.REQ_enddate)):(String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                if (isUserSummaryReportFlag) {
                    condition += " and (je.createdOn >=? and je.createdOn <=?) ";
                  DateFormat df1 =  new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
                    params.add(df1.parse(startDate).getTime());
                    params.add(df1.parse(endDate).getTime());
                    
                } else {
                    condition += " and (je.entryDate >=? and je.entryDate <=?)";
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
            }

            if (nondeleted) {
                condition += " and je.deleted=false ";
            } else if (deleted) {
                condition += " and je.deleted=true ";
            }
            if (!StringUtil.isNullOrEmpty(linkentry)) {
                params.add(linkentry);
                condition += " and je.ID=? ";
            } else {
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"je.entryNumber", "je.memo"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                    StringUtil.insertParamSearchString(map);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    condition += searchQuery;
                }
//                if (!StringUtil.isNullOrEmpty(ss)) {
//                    params.add(ss + "%");
//                    params.add(ss + "%");
//                    condition += " and (je.entryNumber like ? or je.memo like ?) ";
//                }
            }
            if (request.containsKey(Constants.isRepeatedFlag) && request.get(Constants.isRepeatedFlag) != null) {
                if (Boolean.parseBoolean((String) request.get(Constants.isRepeatedFlag))) {
                    if(ispendingAproval){   //Pending Approval Records
                        condition += " and ( je.repeateJE  is not null and je.repeateJE.ispendingapproval=true )";
                    } else {
                        condition += " and ( je.repeateJE  is not null and je.repeateJE.ispendingapproval=false )";
                    }
                } else if (request.containsKey(Constants.isPendingJEFlag) && request.get(Constants.isPendingJEFlag) != null) {
                    boolean isPendingFlag = Boolean.parseBoolean((String) request.get(Constants.isRepeatedFlag));
                    if (!isPendingFlag) {
                        condition += " and je.repeateJE  is null and (je.approvestatuslevel = 11) ";
                    }
                } else {
                    condition += " and je.repeateJE  is null";
                }
            }
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                condition += " and je.createdby.department = ? ";
                params.add(userDepartment);
            }
            
            String appendCase = "and";
            String mySearchFilterString = "";
            String joinString1 = "";
            String exportJoinString = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String searchJoin = "";
            String searchDefaultFieldSQL = "";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    if (!isLineDimPresentAdvSearch && !isKnockOffAdvancedSearch) {
                        condition += " and jed.isSeparated = false ";
                        joinString1 += " inner join je.details jed "; //Adding join on jed if searching on global dimensions/custom fields. 
                    }
                    if (isKnockOffAdvancedSearch) {
                        condition += Constants.KNOCK_OF_HQL_CONDITION;
                    }
                    
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray customSearchFieldArray = new JSONArray();
                    JSONArray defaultSearchFieldArray = new JSONArray();
                    StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);
                    if (defaultSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Default Form fields
                         */
                        ArrayList tableArray = new ArrayList();
                        tableArray.add("customer"); //this table array used to identified wheather join exists on table or not                         
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, ""+Constants.Acc_GENERAL_LEDGER_ModuleId, tableArray, filterConjuctionCriteria);
                        searchJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
//                        searchJoin += " left join solinking on solinking.docid=salesorder.id and solinking.sourceflag = 1 ";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("journalentryRef", "je");
                    }
                    if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field
                        /*
                        If request from JE report then need to filter all JE records i.e. including Normal as well as JE posted from backend
                        */
                        if (request.containsKey("isJEReport") && request.get("isJEReport") != null) {
                            boolean isJEReport = Boolean.parseBoolean(request.get("isJEReport").toString());
                            if (isJEReport) {
                                Searchjson = getJsornStringForSearch(Searchjson, companyid,null);
                                request.put(Constants.moduleid, "100");  // 100 means include all moduels while build query
                            }
                        }
                        request.put(Constants.Searchjson, Searchjson);
                        request.put(Constants.appendCase, appendCase);
                        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
//                        mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "je.accBillInvCustomData");
                        
                        String join = " left join ";
                        if (isLineDimPresentAdvSearch && filterConjuctionCriteria.trim().equalsIgnoreCase(Constants.or)) {
                            join = " inner join ";
                        }
                        if (mySearchFilterString.contains("AccJECustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "jecd");
                            joinString1 += join + " je.accBillInvCustomData jecd ";
                            exportJoinString += join +" je.accBillInvCustomData jecd ";
                        }
                        if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                            if (!request.containsKey("exportQuery") && !joinString1.contains("inner join je.details jed")) { // restricting join on jed if already added.
                                joinString1 += " inner join je.details jed ";
                            }
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jedcd");
                            joinString1 += join +" jed.accJEDetailCustomData jedcd ";
                            exportJoinString += join +" jed.accJEDetailCustomData jedcd ";
                        }
                        if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) { // restricting join on jed if already added.
                            if (!request.containsKey("exportQuery") && !joinString1.contains("inner join je.details jed")) {
                                joinString1 += " inner join je.details jed ";
                            }
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jedprdc");//    
                            joinString1 = " left join jed.accJEDetailsProductCustomData jedprdc ";
                        }
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
            if (request.containsKey("isCustomerReport")) {
                condition += " and je.customer is not null ";
            }
            String orderBy = "";
            String innerQuery = "";
            String[] stringSort = null;
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                stringSort = columSort(Col_Name, Col_Dir);
                orderBy += stringSort[0];
                innerQuery += stringSort[1];
            } else {
                orderBy = " order by je.createdOn desc";
            }

            String query = "";
            if (request.containsKey("exportQuery") && request.get("exportQuery") != null && Boolean.valueOf(request.get("exportQuery").toString())) {
                if (!request.containsKey("searchJson") || (request.containsKey("searchJson") && request.get("searchJson") == null)) {
                    condition += " and jed.isSeparated = false ";
                }
                query = "select je, jed from JournalEntry je inner join je.details jed "+ exportJoinString + condition + mySearchFilterString + " order by je.createdOn desc,jed.debit desc ";
            } else if (request.containsKey("cndnPendingFlag") && request.get("cndnPendingFlag") != null && Boolean.valueOf(request.get("cndnPendingFlag").toString())) {
                params.clear();
                if (request.containsKey("typeValue")) {
                    query = "from JournalEntry where company.companyID=? and typeValue=? and ID not in (select journalEntry.ID from CreditNote where company.companyID=?)";
                    params.add(companyid);
                    params.add((Integer) request.get("typeValue"));
                    params.add(companyid);
                }

            } else {
                query = "select distinct je from JournalEntry je " + innerQuery + joinString1 + condition + mySearchFilterString + orderBy;
            }

            list = executeQuery( query, params.toArray());//params.toArray() sessionHandlerImpl.getCompanyid(request));  new Object[]{ sessionHandlerImpl.getCompanyid(request)}
            count = list.size();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                try {
                    list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                } catch (NumberFormatException ex) {
                    list = executeQueryPaging( query, params.toArray(), new Integer[]{0, 15});//ERP-32676 - Number Format Exception
                    Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getJournalEntry : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

     public KwlReturnObject getJournalEntryTotalAmountSum(HashMap<String, Object> request) throws ServiceException {
         List list = new ArrayList();
         int count = 0;
         String companyid = (String) request.get(COMPANYID);
         DateFormat df = (DateFormat) request.get(Constants.df);
         ArrayList params = new ArrayList();
         String query = " SELECT sum(jed.amountinbase) from JournalEntryDetail jed INNER JOIN  jed.journalEntry je ";
         try {
             params.add(companyid);
             String condition = " where je.company.companyID=? and jed.debit='F' and jed.isSeparated='F'";
             
             if (!StringUtil.isNullOrEmpty((String) request.get("userid"))) {
                 params.add((String) request.get("userid"));
                 condition += " AND je.createdby.userID=? ";
             }
             String startDate = request.get(Constants.REQ_startdate) != null ? StringUtil.DecodeText((String) request.get(Constants.REQ_startdate)) : (String) request.get(Constants.REQ_startdate);
             String endDate = request.get(Constants.REQ_enddate) != null ? StringUtil.DecodeText((String) request.get(Constants.REQ_enddate)) : (String) request.get(Constants.REQ_enddate);
             if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                 condition += " and (je.entryDate >=? and je.entryDate <=?)";
                 params.add(df.parse(startDate));
                 params.add(df.parse(endDate));
             }
             
             query = query + condition;
             
             list = executeQuery(query, params.toArray());
         } catch (NumberFormatException ex) {
             list = executeQueryPaging(query, params.toArray(), new Integer[]{0, 15});//ERP-32676 - Number Format Exception
             Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ParseException ex) {
             Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         return new KwlReturnObject(true, "", null, list, count);
     }

    public String[] columSort(String Col_Name, String Col_Dir) throws ServiceException {
        String[] String_Sort = new String[2];

        if (Col_Name.equals("chequeNumber")) {
            String_Sort[0] = " order by je.cheque.chequeNo " + Col_Dir;
            String_Sort[1] = " left join je.cheque ";
        } else if (Col_Name.equals("memo")) {
            String_Sort[0] = " order by je.memo " + Col_Dir;
            String_Sort[1] = "";
        } else if (Col_Name.equals("entryno")) {
            String_Sort[0] = " order by je.entryNumber " + Col_Dir;
            String_Sort[1] = "";
        } else {
            String_Sort[0] = " order by je.entryDate " + Col_Dir;
            String_Sort[1] = "";
        }

        return String_Sort;
    }
    
    public String jeColumnSort(String Col_Name, String Col_Dir) throws ServiceException {
        String String_Sort = "";

        if (Col_Name.equals("chequeNumber")) {
            String_Sort = " order by chequeNo " + Col_Dir;
        } else if (Col_Name.equals("memo")) {
            String_Sort = " order by memo " + Col_Dir;
        } else if (Col_Name.equals("entryno")) {
            String_Sort = " order by entryNumber " + Col_Dir;
        } else if(Col_Name.equals("approvalstatus")){
            String_Sort = " order by approvalstatus " + Col_Dir;
        } else {
            String_Sort = " order by createdon " + Col_Dir;
        }
        return String_Sort;
    }

    public KwlReturnObject getRepeateJEEntryNo(Date prevDate) throws ServiceException {
//        String query = "SELECT entryNumber from JournalEntry where repeateJE is not null and (repeateJE.isActivate=true and repeateJE.ispendingapproval=false) and repeateJE.prevDate = ?";
        String query = "FROM JournalEntry WHERE repeateJE is not null and (repeateJE.isActivate=true and repeateJE.ispendingapproval=false) and ((repeateJE.prevDate = ? and repeateJE.nextDate <= repeateJE.expireDate) ";
        //getting repeate JE for which prev date will be updated to today's date after repeated JE creation 
        query += " or (repeateJE.nextDate=? and repeateJE.intervalUnit=1 and repeateJE.intervalType='day')) ";
        List list = executeQuery( query, new Object[]{prevDate, prevDate});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     
    public KwlReturnObject getRepeateJE(HashMap<String, Object> requestParams) throws ServiceException {
        Date currentDate = new Date();
        String query = "from JournalEntry where repeateJE is not null and (repeateJE.isActivate=true and repeateJE.ispendingapproval=false) and repeateJE.startDate<=? and repeateJE.nextDate <= ? and (repeateJE.expireDate is null or repeateJE.expireDate >= ?)";
        List list = executeQuery( query, new Object[]{currentDate, currentDate,currentDate});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getRepeateJEDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String parentJEId = (String) requestParams.get("parentJEId");
        String query = "from JournalEntry where parentJE.ID = ? ";
        List list = executeQuery( query, new Object[]{parentJEId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getJERepeateMemoDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String repeateid = (String) requestParams.get("repeateid");
        String memofor = (String) requestParams.get("memofor");
        String query = "from RepeatedJEMemo RM where RM." + memofor + " = ? ";
        List list = executeQuery( query, new Object[]{repeateid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getRepeateJEMemo(HashMap<String, Object> requestParams) throws ServiceException {
        String repeatedJEMemoID = (String) requestParams.get("repeatedJEMemoID");
        int noOfJEpost = Integer.parseInt(requestParams.get("noOfJERemainpost").toString());
        String columnName = (String) requestParams.get("columnName");
        String query = "from RepeatedJEMemo RM where RM." + columnName + " = ? and RM.count = ? ";
        List list = executeQuery( query, new Object[]{repeatedJEMemoID, noOfJEpost});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveRepeateJEInfo(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            RepeatedJE rJE = new RepeatedJE();
            if (dataMap.containsKey("id")) {
                rJE = (RepeatedJE) get(RepeatedJE.class, (String) dataMap.get("id"));
            }

            if (dataMap.containsKey("intervalType")) {
                rJE.setIntervalType((String) dataMap.get("intervalType"));
            }
            if (dataMap.containsKey("intervalUnit")) {
                rJE.setIntervalUnit((Integer) dataMap.get("intervalUnit"));
            }
            if (dataMap.containsKey("NoOfJEpost")) {
                rJE.setNoOfJEpost((Integer) dataMap.get("NoOfJEpost"));
            }
            if (dataMap.containsKey("NoOfRemainJEpost")) {
                rJE.setNoOfRemainJEpost((Integer) dataMap.get("NoOfRemainJEpost"));
            }
            if (dataMap.containsKey("startDate")) {
                rJE.setStartDate((Date) dataMap.get("startDate"));
            }
            if (dataMap.containsKey("nextDate")) {
                rJE.setNextDate((Date) dataMap.get("nextDate"));
            }
            if (dataMap.containsKey("expireDate")) {
                rJE.setExpireDate((Date) dataMap.get("expireDate"));
            }
            if (dataMap.containsKey("isactivate")) {
                rJE.setIsActivate((Boolean)dataMap.get("isactivate"));
            } 
            if (dataMap.containsKey("ispendingapproval")) {
                rJE.setIspendingapproval((Boolean)dataMap.get("ispendingapproval"));
            }
            if (dataMap.containsKey("approver")) {
                rJE.setApprover((String) dataMap.get("approver"));
            }
            if (dataMap.containsKey("prevDate")) {
                rJE.setPrevDate((Date) dataMap.get("prevDate"));
            }
            saveOrUpdate(rJE);
            list.add(rJE);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveRepeateInvoiceInfo : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public int DelRepeateJEMemo(String repeateid, String column) throws ServiceException {
        String query = "DELETE FROM RepeatedJEMemo RM WHERE RM." + column + "= ? ";
        int numRows = executeUpdate( query, new Object[]{repeateid});
        return numRows;
    }

    @Override
    public KwlReturnObject saveRepeateJEMemo(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            RepeatedJEMemo rJEMemo = new RepeatedJEMemo();
            if (dataMap.containsKey("id")) {
                rJEMemo = (RepeatedJEMemo) get(RepeatedJEMemo.class, (String) dataMap.get("id"));
            }

            if (dataMap.containsKey("repeatedjeid")) {
                RepeatedJE rJE = (RepeatedJE) get(RepeatedJE.class, (String) dataMap.get("repeatedjeid"));
                rJEMemo.setRepeatedJEID(rJE);
            }
            if (dataMap.containsKey("RepeatedInvoiceID")) {
                rJEMemo.setRepeatedInvoiceID((String) dataMap.get("RepeatedInvoiceID"));
            }
            if (dataMap.containsKey("RepeatedPaymentID")) {
                rJEMemo.setRepeatedPaymentId((String) dataMap.get("RepeatedPaymentID"));
            }
            if (dataMap.containsKey("no")) {
                rJEMemo.setCount((Integer) dataMap.get("no"));
            }
            if (dataMap.containsKey("memo")) {
                rJEMemo.setMemo((String) dataMap.get("memo"));
            }
            saveOrUpdate(rJEMemo);
            list.add(rJEMemo);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveRepeateJEMemo : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updateJE(JSONObject json, HashSet details) throws ServiceException {
        List list = new ArrayList();
        try {
            String JEid = json.getString("JEid");
            JournalEntry JE = (JournalEntry) get(JournalEntry.class, JEid);
            if (JE != null) {
                if (json.has("repeateid")) {
                    JE.setRepeateJE((RepeatedJE) get(RepeatedJE.class, json.getString("repeateid")));
                }
                if (json.has("parentid")) {
                    JournalEntry je = (JournalEntry) get(JournalEntry.class, json.getString("parentid"));
                    JE.setParentJE(je);
                }
                if (details != null) {
                    if (!details.isEmpty()) {
                        JE.setDetails(details);
                    }
                }
                saveOrUpdate(JE);
            }
            list.add(JEid);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.updateJE:" + ex, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getJournalEntryForReports(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        List finallist = new ArrayList();
        int count = 0;
        String table1 = "", table2 = "";
        try {
//            KWLCurrency currency = (KWLCurrency)get(KWLCurrency.class, (String) request.get("gcurrencyid"));
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(JournalEntryConstants.START);
            String limit = (String) request.get(JournalEntryConstants.LIMIT);
            String ss = (String) request.get(JournalEntryConstants.SS);
            String linkentry = (String) request.get(JournalEntryConstants.LINKID);
            if (request.containsKey(JournalEntryConstants.ReportClass)) {
                String ReportClass = (String) request.get(JournalEntryConstants.ReportClass);
                if (ReportClass.equalsIgnoreCase(JournalEntryConstants.Receipt)) {
                    table1 = "receipt";
                    table2 = "billingreceipt";
                } else if (ReportClass.equalsIgnoreCase(JournalEntryConstants.Payment)) {
                    table1 = "payment";
                    table2 = "billingpayment";
                } else if (ReportClass.equalsIgnoreCase(JournalEntryConstants.Invoice)) {
                    table1 = "invoice";
                    table2 = "billinginvoice";
                } else if (ReportClass.equalsIgnoreCase(JournalEntryConstants.GoodsReceipt)) {
                    table1 = "goodsreceipt";
                    table2 = "billinggr";
                } else if (ReportClass.equalsIgnoreCase(JournalEntryConstants.ASSET)) {
                    table1 = "asset";
                }
            }
            boolean deleted = Boolean.parseBoolean((String) request.get(JournalEntryConstants.DELETED));
            boolean nondeleted = Boolean.parseBoolean((String) request.get(JournalEntryConstants.NONDELETED));
            String companyid = (String) request.get(JournalEntryConstants.COMPANYID);
//            CompanyAccountPreferences accPref = companyid == null ? null : (CompanyAccountPreferences) get(CompanyAccountPreferences.class, companyid);

            ArrayList params = new ArrayList();
            ArrayList param = new ArrayList();
            params.add(companyid);
            param.add(companyid);
            String condition = "";
            String selectedIds = (String) request.get("selectedIds");//Geting only Selected records for Export.
            if (!StringUtil.isNullOrEmpty(selectedIds)) {
                selectedIds = AccountingManager.getFilterInString(selectedIds);
                condition += " and journalentry.ID in " + selectedIds + " ";
            }

//            if(accPref != null) {
//                params.add(accPref.isWithoutInventory()?"0":"1");
//                condition += " and (rc.journalEntry.isInventory = ? or rc.journalEntry.isInventory is null) ";                
//            }
            condition += " and journalentry.pendingapproval = 0 and journalentry.istemplate != 2 and journalentry.isdraft = false ";
            if (request.containsKey("groupid") && Boolean.parseBoolean(request.get("groupid").toString())) {
                params.add("12");
                params.add(companyid);
                param.add("12");
                param.add(companyid);
                condition += " and journalentry.id in (select distinct(jedetail.journalEntry) from jedetail inner join account on account.id=jedetail.account inner join accgroup on account.groupname=accgroup.id where accgroup.id=? and jedetail.company=?)";
            }

            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                param.add(costCenterId);
                condition += " and journalentry.costcenter=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (journalentry.entrydate >=? and journalentry.entrydate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
                param.add(df.parse(startDate));
                param.add(df.parse(endDate));
            }

            if (nondeleted) {
                condition += " and journalentry.deleteflag='F' ";
            } else if (deleted) {
                condition += " and journalentry.deleteflag='T' ";
            }
            String orderBy = "";
            String innerQuery = "";
            String colselect = "";
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                if (Col_Name.equals("chequeNumber")){
                    colselect = ",cheque.chequeno as chequeNo ";
                    innerQuery = " LEFT JOIN cheque ON cheque.id=journalentry.cheque ";
                }
                orderBy = jeColumnSort(Col_Name, Col_Dir);
            } else {
                orderBy = " order by createdon desc";
            }
            if (!StringUtil.isNullOrEmpty(linkentry)) {
                params.add(linkentry);
                param.add(linkentry);
                condition += " and journalentry.id=? ";
            } else {
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"journalentry.entryno", "journalentry.memo"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                    StringUtil.insertParamSearchString(map);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    condition += searchQuery;
                }
//                if (!StringUtil.isNullOrEmpty(ss)) {
//                    params.add(ss + "%");
//                    params.add(ss + "%");
//                    param.add(ss + "%");
//                    param.add(ss + "%");
//                    condition += " and (journalentry.entryno like ? or journalentry.memo like ?) ";
//                }
            }

            String query = "";
            if (request.get("exportQuery") != null && Boolean.valueOf(request.get("exportQuery").toString())) {
                if (!request.containsKey("searchJson") || (request.containsKey("searchJson") && request.get("searchJson") == null)) {   //ERP-40793 added as wrong records was getting exported in files 
                    condition += " and jedetail.isseparated = 'F' ";
                }
                query = "Select " + table1 + ".journalentry,jedetail.id,journalentry.createdon as 'createdon' from " + table1 + " INNER JOIN  journalentry   ON  " + table1 + ".journalentry=journalentry.id  INNER JOIN jedetail  ON journalentry.id=jedetail.journalentry WHERE  " + table1 + ".company=? " + condition + " UNION Select " + table2 + ".journalentry,jedetail.id,journalentry.createdon as 'createdon' from " + table2 + " INNER JOIN journalentry  ON  " + table2 + ".journalentry=journalentry.id  INNER JOIN  jedetail ON journalentry.id=jedetail.journalEntry WHERE " + table2 + ".company=? " + condition + " ORDER BY createdon DESC";
            } //                query = "select rc.journalEntry, jed from "+ReportClass+" rc " + " inner join rc.journalEntry.details jed " + condition + " order by rc.journalEntry.createdOn desc ";
            else {
                query = "Select journalentry as journalentryno,journalentry.createdon as 'createdon',journalentry.entrydate as entrydate,journalentry.entryno as entryNumber ,journalentry.memo as memo,journalentry.pendingapproval as approvalstatus"+colselect+" from " + table1 + "  INNER JOIN  journalentry  ON  " + table1 + ".journalentry=journalentry.id "+innerQuery+" WHERE " + table1 + ".company=? "+ condition + " UNION Select journalentry,journalentry.createdon as 'createdon',journalentry.entrydate as entrydate,journalentry.entryno as entryNumber ,journalentry.memo as memo,journalentry.pendingapproval as approvalstatus"+colselect+" from  " + table2 + "  INNER JOIN  journalentry  ON  " + table2 + ".journalentry=journalentry.id "+innerQuery+" WHERE " + table2 + ".company=? " + condition + orderBy;
            }
//                query = "Select rc.journalEntry  from "+ReportClass+" rc " + condition + " order by rc.journalEntry.createdOn desc ";

            String reportType = (String) request.get(JournalEntryConstants.ReportType);
            if (reportType != null && table1.equals("asset")) {
                if (StringUtil.equal(JournalEntryConstants.CashReceiptJournal, reportType)) {
                    if (request.get("exportQuery") != null && Boolean.valueOf(request.get("exportQuery").toString())) {
                        query = "Select deleteJe as journalentryno,jedetail.id,journalentry.createdon as 'createdon' from " + table1 + " INNER JOIN  journalentry ON " + table1 + ".deleteJe=journalentry.id INNER JOIN jedetail ON journalentry.id=jedetail.journalentry WHERE " + table1 + ".company=? " + condition;
                    } else {
                        query = "Select deleteJe as journalentryno,journalentry.createdon as 'createdon' from " + table1 + "  INNER JOIN  journalentry  ON  " + table1 + ".deleteJe=journalentry.id WHERE " + table1 + ".company=? " + condition;
                    }
                } else if (StringUtil.equal(JournalEntryConstants.CashDisbursementJournal, reportType)) {
                    if (request.get("exportQuery") != null && Boolean.valueOf(request.get("exportQuery").toString())) {
                        query = "Select purchaseJe as journalentryno,jedetail.id,journalentry.createdon as 'createdon' from " + table1 + " INNER JOIN  journalentry ON " + table1 + ".purchaseJe=journalentry.id INNER JOIN jedetail ON journalentry.id=jedetail.journalentry WHERE " + table1 + ".company=? " + condition;
                    } else {
                        query = "Select purchaseJe as journalentryno,journalentry.createdon as 'createdon' from " + table1 + "  INNER JOIN  journalentry  ON  " + table1 + ".purchaseJe=journalentry.id WHERE " + table1 + ".company=? " + condition;
                    }
                }
            }
            ArrayList finalParam = new ArrayList();
            for (Object object : params) {
                finalParam.add(object);
            }
            if (!table1.equals("asset")) {
                for (Object object : param) {
                    finalParam.add(object);
                }
            }
            list = executeSQLQuery( query, finalParam.toArray());//params.toArray() sessionHandlerImpl.getCompanyid(request));  new Object[]{ sessionHandlerImpl.getCompanyid(request)}
            count = list.size();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                //   list =  executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                list = executeSQLQueryPaging( query, finalParam.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                // query+=" LIMIT "+limit+" OFFSET "+start;                
                //list = executeSQLQuery( query, finalParam.toArray());
            }
            for (int i = 0; i < list.size(); i++) {
                Object[] row = (Object[]) list.get(i);
                String id = (String) row[0];
                JournalEntry journalEntry = (JournalEntry) get(JournalEntry.class, id);
                if (request.get("exportQuery") != null && Boolean.valueOf(request.get("exportQuery").toString())) {
                    String jedId = (String) row[1];
                    JournalEntryDetail journalEntryDetail = (JournalEntryDetail) get(JournalEntryDetail.class, jedId);
                    Object[] objects = new Object[]{journalEntry, journalEntryDetail};
                    finallist.add(objects);
                } else {
                    finallist.add(journalEntry);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getJournalEntry : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, finallist, count);
    }
    
    public boolean updateJEDetails(HashMap<String, Object> dataMap) throws ServiceException {
        boolean success = false;
        try {
            if (dataMap.containsKey("journalEntry")) {
                JournalEntry je = (JournalEntry) dataMap.get("journalEntry");
                if (je != null) {
                    if (dataMap.containsKey("transactionID") && dataMap.containsKey("moduleID")) {
                        je.setTransactionId(dataMap.get("transactionID").toString());
                        int moduleID = Integer.parseInt(dataMap.get("moduleID").toString());
                        je.setTransactionModuleid(moduleID);
                        saveOrUpdate(je);
                        success = true;
                    }
                }
            }
        } catch (Exception ex) {
            success = false;
            throw ServiceException.FAILURE("savePurchaseReturnDetails : " + ex.getMessage(), ex);
        } finally {
            return success;
        }
    }
    
    public boolean updateJEDetailsSQLQuery(HashMap<String, Object> dataMap) throws ServiceException {
        boolean success = false;
        ArrayList params = new ArrayList();
        try {
            if (dataMap.containsKey("jeID")) {
                if (dataMap.containsKey("transactionID") && dataMap.containsKey("moduleID")) {
                    int moduleID = Integer.parseInt(dataMap.get("moduleID").toString());
                    params.add(moduleID);
                    params.add(dataMap.get("transactionID").toString());
                    params.add(dataMap.get("jeID").toString());
                    String sqlQuery = "update journalentry set transactionModuleid = ?, transactionId=? where id like ?";
                    int numRows = executeSQLUpdate( sqlQuery, params.toArray());
                    if (numRows > 0) {
                        success = true;
                    }
                }
            }

        } catch (Exception ex) {
            success = false;
            throw ServiceException.FAILURE("savePurchaseReturnDetails : " + ex.getMessage(), ex);
        } finally {
            return success;
        }
    }
    
    public KwlReturnObject deleteJournalEntry(String jeid, String companyid) throws ServiceException {
        JournalEntry journalEntry = (JournalEntry) get(JournalEntry.class, jeid);
        if (journalEntry.isIsReverseJE() && journalEntry.isIsOneTimeReverse()) { //If JE is isIsOneTimeReverse=true then we ll not update reverse JE details in case reverse JE had reverse JE.
            JournalEntry je = (JournalEntry) get(JournalEntry.class, (String) journalEntry.getReverseJournalEntry());
            updateReverseJournalEntryValue(je, "");
            updateReverseJournalEntryValue(journalEntry, "");
        }
        String query = "update JournalEntry set deleted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "Journal Entry has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteJournalEntryPermanent(String jeid, String companyid) throws ServiceException {
        JournalEntry journalEntry = (JournalEntry) get(JournalEntry.class, jeid);
        if (journalEntry !=null && journalEntry.isIsReverseJE() && journalEntry.isIsOneTimeReverse()) { //If JE is isIsOneTimeReverse=true then we ll not update reverse JE details in case reverse JE had reverse JE.
            JournalEntry je = (JournalEntry) get(JournalEntry.class, (String) journalEntry.getReverseJournalEntry());
            updateReverseJournalEntryValue(je, "");
            updateReverseJournalEntryValue(journalEntry, "");
        }
        String delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuery4 = "";
        int numtotal = 0;
        try {
            ArrayList params1 = new ArrayList();
            params1.add(companyid);
            delQuery1 = "delete  from accjedetailcustomdata where jedetailId in (select id from jedetail where company = ? and journalEntry in ('" + jeid + "'))";
            int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());

            ArrayList params3 = new ArrayList();
            params3.add(companyid);
            delQuery3 = "delete from jedetail where company = ? and journalEntry in ('" + jeid + "') ";
            int numRows3 = executeSQLUpdate( delQuery3, params3.toArray());

            ArrayList params4 = new ArrayList();
            delQuery4 = "delete from journalentry where id  in ('" + jeid + "')";
            int numRows4 = executeSQLUpdate( delQuery4, params4.toArray());

            ArrayList params2 = new ArrayList();
            delQuery2 = "delete  from accjecustomdata where journalentryId in ('" + jeid + "')";
            int numRows2 = executeSQLUpdate( delQuery2, params2.toArray());

            numtotal = numRows1 + numRows2 + numRows3 + numRows4;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete JE as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Journal Entry has been deleted successfully.", null, null, numtotal);
    }

    public KwlReturnObject eliminateJournalEntry(String jeid) throws ServiceException {
        String query = "update JournalEntry set eliminateflag=true where ID=?";
        int numRows = executeUpdate( query, new Object[]{jeid});
        return new KwlReturnObject(true, "Journal Entry has been eliminated successfully.", null, null, numRows);
    }

    public KwlReturnObject permanentDeleteJournalEntry(String jeid, String companyid) throws ServiceException {
        String query = "delete from JournalEntry where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "Journal Entry has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject getChequeIdLinkedToJournalEntry(String jeid, String companyid) throws ServiceException {
        String query = "select cheque from journalentry je where je.id=? and je.company=?";
        List params = new ArrayList();
        params.add(jeid);
        params.add(companyid);
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getAccountBalance(String accountid, Date startDate, Date endDate) throws ServiceException {
        return getAccountBalance(accountid, startDate, endDate, "", "", "");
    }

    public KwlReturnObject getAccountBalance(String accountid, Date startDate, Date endDate, String costCenterID, String filterConjuctionCriteria, String Searchjson) throws ServiceException {
        List list = new ArrayList();
        String mySearchFilterString = "";
        String joinString = "";
        String query = "select (case when debit=true then amount else -amount end) ,jed , jed.journalEntry.currency.currencyID from JournalEntryDetail jed " + joinString + " where account.ID=? and jed.journalEntry.deleted=false and jed.journalEntry.pendingapproval = 0 and jed.journalEntry.draft=false and jed.journalEntry.istemplate != 2 and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<=? ";
        ArrayList params = new ArrayList();
        params.add(accountid);
        if (startDate == null) {
            startDate = new Date(0);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        params.add(startDate);
        params.add(endDate);

        if (!StringUtil.isNullOrEmpty(costCenterID)) {
            query += " and jed.journalEntry.costcenter.ID=?";
            params.add(costCenterID);
        }

        if (!StringUtil.isNullOrEmpty(Searchjson)) {
            HashMap<String, Object> request = new HashMap<String, Object>();
            Searchjson = getJsornStringForSearch(Searchjson, accountid,null);
            request.put(Constants.Searchjson, Searchjson);
            request.put(Constants.appendCase, "and");
            request.put(Constants.moduleid, "100");
            request.put("filterConjuctionCriteria", filterConjuctionCriteria);
            try {
                mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "jed.journalEntry.accBillInvCustomData");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//          
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");//             
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        list = executeQuery( query + mySearchFilterString, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getAccountBalance(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, String costCenterID, String filterConjuctionCriteria, String Searchjson) throws ServiceException {
        List list = new ArrayList();
        String mySearchFilterString = "";
        String joinString = "";
        String selectedCurrencyIds = (String) requestParams.get("selectedCurrencyIds");
        int accountTransactionType = requestParams.containsKey("accountTransactionType") && requestParams.get("accountTransactionType")!=null?Integer.parseInt(requestParams.get("accountTransactionType").toString()):Constants.All_Transaction_TypeID;
        String query = "select (case when debit=true then amount else -amount end) ,jed , jed.journalEntry.currency.currencyID, jed.journalEntry from JournalEntryDetail jed " + joinString + " where account.ID=? and jed.journalEntry.deleted=false and jed.journalEntry.pendingapproval = 0 and jed.journalEntry.draft=false and jed.journalEntry.istemplate != 2 and jed.journalEntry.approvestatuslevel=11 and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<=? ";
        ArrayList params = new ArrayList();
        params.add(accountid);
        if (startDate == null) {
            startDate = new Date(0);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        params.add(startDate);
        params.add(endDate);

        if (!StringUtil.isNullOrEmpty(costCenterID)) {
            query += " and jed.journalEntry.costcenter.ID=?";
            params.add(costCenterID);
        }
        if (!StringUtil.isNullOrEmpty(selectedCurrencyIds)) {
            selectedCurrencyIds = AccountingManager.getFilterInString(selectedCurrencyIds);
            query += " and jed.journalEntry.currency.currencyID in " + selectedCurrencyIds + " ";
        }
        
        //below filter is given in GL Report on transaction
        if (accountTransactionType == Constants.Acc_Make_Payment_ModuleId) {
            query += " and jed.journalEntry.transactionModuleid =" + Constants.Acc_Make_Payment_ModuleId + " ";
        } else if (accountTransactionType == Constants.Acc_Receive_Payment_ModuleId) {
            query += " and jed.journalEntry.transactionModuleid =" + Constants.Acc_Receive_Payment_ModuleId + " ";
        }
//        if(!StringUtil.isNullOrEmpty(selectedCurrencyIds)){ 
//            selectedCurrencyIds = AccountingManager.getFilterInString(selectedCurrencyIds);
//            query += " and jed.account.currency.currencyID in "+selectedCurrencyIds+" ";               
//        }

        if (!StringUtil.isNullOrEmpty(Searchjson)) {
            if (isAdvanceSearchOnGlobalDimension(Searchjson)) {
                query += " and jed.isSeparated = false ";
            }
            HashMap<String, Object> request = new HashMap<String, Object>();
            Searchjson = getJsornStringForSearch(Searchjson, accountid,null);
            request.put(Constants.Searchjson, Searchjson);
            request.put(Constants.appendCase, "and");
            request.put(Constants.moduleid, "100");
            request.put("filterConjuctionCriteria", filterConjuctionCriteria);
            try {
                mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "jed.journalEntry.accBillInvCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//           
                     mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");//             
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            query += " and jed.isSeparated = false ";
        }
        list = executeQuery( query + mySearchFilterString, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public List getSumAmountForAccount(Map<String, Object> requestParams) throws ServiceException {
        List params = new ArrayList();
        String companyid = (String) requestParams.get("companyid");
        boolean groupByDebit = requestParams.containsKey("groupByDebit") ? (Boolean) requestParams.get("groupByDebit") : false;
        String jeSearchQuery = "";
        Boolean isConsolidatedPNL = (requestParams.containsKey("isConsolidatedPNL") && requestParams.get("isConsolidatedPNL") != null)? (Boolean) requestParams.get("isConsolidatedPNL"):false;
        boolean isOpening = requestParams.containsKey("isOpening") ? (Boolean)requestParams.get("isOpening") : false;
        if(!isOpening){
        params.add(requestParams.get("startdate"));
        params.add(requestParams.get("enddate"));
        }
        else{
            params.add(requestParams.get("startdate"));
        }
       
        List<String> customdatavalues = null;
        List<String> columns = null;
        Map<Integer, String> colnumMap = null;
        String fieldType = "";
        String costcenter = "";
        String columnheader = "";
        int iscustomcolumndata=0;

        if (!isConsolidatedPNL) {
            customdatavalues = (List<String>) requestParams.get("customdatavalues");
            columns = (List<String>) requestParams.get("columns");
            colnumMap = (Map) requestParams.get("colnumMap");
            fieldType = (String) requestParams.get("fieldtype");
            iscustomcolumndata = (Integer) requestParams.get("iscustomcolumndata");
            costcenter = (String) requestParams.get("costcenter");
            columnheader = (String) requestParams.get("columnheader");
        }
        boolean isProductCustomData = requestParams.containsKey("isProductCustomData") ? (Boolean) requestParams.get("isProductCustomData") : false;
        boolean isForKnockOff = requestParams.containsKey("isForKnockOff") ? (Boolean) requestParams.get("isForKnockOff") : false;
        boolean isMonthly = requestParams.containsKey("isMonthly") ? (Boolean) requestParams.get("isMonthly") : false;
        if (!StringUtil.isNullOrEmpty(costcenter)) {
            jeSearchQuery = " and costcenter = ?";
            params.add(costcenter);
        }
        
        CompanyAccountPreferences preferences = null;
        boolean isPerpetualInventory = false;
        if (!StringUtil.isNullOrEmpty(companyid) && !isConsolidatedPNL) {
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            isPerpetualInventory = preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD;
        }

        String selectAttr = "";
        String selectSubAttr = "";
        String customQuery = "";
        String groupQuery = " group by a.account ";
        if (isConsolidatedPNL) {
            groupQuery += ",a.company";
        }
        if(groupByDebit){
            groupQuery += ",a.debit";
        }
        String querycondition="";
        boolean isDimensionBased = false;

        //In case of custom fields/dimension
        if (!StringUtil.isNullOrEmpty(fieldType) && customdatavalues != null && !customdatavalues.isEmpty() && columns != null && !columns.isEmpty()) {
            //In case of dimension based export from monthly reports.
            isDimensionBased = true;

            //In case of drop down
            if (fieldType.equals("4")) {
                selectAttr += ",trim(fcd.value) as value";
                selectSubAttr += ",trim(a.value)";
                groupQuery += ",a.value";
                if (isProductCustomData) {
                    customQuery = "inner join accjedetailproductcustomdata jedcd on jedcd.jedetailid=jed.id \n";
                } else if (isForKnockOff) {
                    customQuery = "left join accjecustomdata jedcd on jedcd.journalentryid=je.id \n"
                            + " left join accjedetailcustomdata jedc on jedc.jedetailid=jed.id \n";
                } else {
                    String join = isPerpetualInventory ? " left ":" inner ";
                    if (iscustomcolumndata == 0) {
                        customQuery = join + " join accjecustomdata jedcd on jedcd.journalentryid=je.id \n";
                    } else {
                        customQuery = join +" join accjedetailcustomdata jedcd on jedcd.jedetailid=jed.id \n";
                    }
                }
                customQuery += "inner join fieldcombodata fcd on (";
                StringBuilder dimQueryBuilder = new StringBuilder();
                for (String dimension : customdatavalues) {
                    dimQueryBuilder.append("fcd.value = ?").append(" or ");
                    params.add(dimension);
                }
                customQuery += dimQueryBuilder.toString();
                customQuery = customQuery.substring(0, customQuery.lastIndexOf(" or "));
                customQuery += ")\n";
                StringBuilder colQueryBuilder = new StringBuilder(" and (");
               
                // Create condition query to search on dimension / custom field.
                if (colnumMap != null && !colnumMap.isEmpty()) {
                    Set<Integer> colnumSet = colnumMap.keySet();

                    for (Integer moduleId : colnumSet) {
                        String column = colnumMap.get(moduleId);
                        String appendCol = "jedcd." + column;
                        if (isPerpetualInventory) {
                            // If perpetual inventory activated then take joins on custom tables of GR, DO, SR, PR.
                            if (moduleId == Constants.Acc_Delivery_Order_ModuleId) {
                                if (iscustomcolumndata == 0) {
                                    customQuery += " LEFT JOIN deliveryorder  ON je.company = deliveryorder.company and je.id  = deliveryorder.inventoryje \n";
                                    customQuery += " LEFT JOIN deliveryordercustomdata  ON deliveryordercustomdata.deliveryOrderId=deliveryorder.id  \n";
                                    appendCol = "deliveryordercustomdata." + column;
                                } else {
                                    customQuery += " LEFT JOIN dodetails on je.company = dodetails.company and (jed.id = dodetails.costofgoodssoldjedetail or jed.id = dodetails.inventoryjedetail) \n";
                                    customQuery += " LEFT JOIN dodetailscustomdata  ON dodetailscustomdata.dodetailsid=dodetails.id  \n";
                                    appendCol = "dodetailscustomdata." + column;
                                }
                            } else if (moduleId == Constants.Acc_Goods_Receipt_ModuleId) {
                                if (iscustomcolumndata == 0) {
                                    customQuery += " LEFT JOIN grorder  ON  je.company = grorder.company and je.id  = grorder.inventoryje  \n";
                                    customQuery += " LEFT JOIN grordercustomdata  ON grordercustomdata.goodsreceiptorderid=grorder.id \n";
                                    appendCol = "grordercustomdata." + column;
                                } else {
                                    customQuery += " LEFT JOIN grodetails on je.company = grodetails.company and  (jed.id = grodetails.purchasesjedetail or jed.id = grodetails.inventoryjedetail)  \n";
                                    customQuery += " LEFT JOIN grodetailscustomdata  ON grodetailscustomdata.grodetailsid=grodetails.id \n";
                                    appendCol = "grodetailscustomdata." + column;
                                }
                            } else if (moduleId == Constants.Acc_Sales_Return_ModuleId) {
                                if (iscustomcolumndata == 0) {
                                    customQuery += " LEFT JOIN salesreturn  ON je.company = salesreturn.company and je.id  = salesreturn.inventoryje \n";
                                    customQuery += " LEFT JOIN salesreturncustomdata  ON salesreturncustomdata.salesreturnid=salesreturn.id \n";
                                    appendCol = "salesreturncustomdata." + column;
                                } else {
                                    customQuery += " LEFT JOIN srdetails on  je.company = srdetails.company and  (jed.id = srdetails.costofgoodssoldjedetail or jed.id = srdetails.inventoryjedetail) \n";
                                    customQuery += " LEFT JOIN srdetailscustomdata  ON srdetailscustomdata.srdetailsid=srdetails.id \n";
                                    appendCol = "srdetailscustomdata." + column;
                                }
                            } else if (moduleId == Constants.Acc_Purchase_Return_ModuleId) {
                                if (iscustomcolumndata == 0) {
                                    customQuery += " LEFT JOIN purchasereturn  ON je.company = purchasereturn.company and je.id  = purchasereturn.inventoryje \n";
                                    customQuery += " LEFT JOIN purchasereturncustomdata  ON purchasereturncustomdata.purchasereturnid=purchasereturn.id \n";
                                    appendCol = "purchasereturncustomdata." + column;
                                } else {
                                    customQuery += " LEFT JOIN prdetails on  je.company = prdetails.company and  (jed.id = prdetails.purchasesjedetail or  jed.id = prdetails.inventoryjedetail) \n";
                                    customQuery += " LEFT JOIN prdetailscustomdata  ON prdetailscustomdata.prdetailsid=prdetails.id \n";
                                    appendCol = "prdetailscustomdata." + column;
                                }
                            }
                        }
                        if (colQueryBuilder.indexOf(appendCol) == -1) {
                            colQueryBuilder.append("fcd.id = ").append(appendCol).append(" or ");
                            if (isForKnockOff) {
                                colQueryBuilder.append("fcd.id = jedc.").append(column).append(" or ");
                            }
                        }
                    }
                } else {
                    for (String column : columns) {
                        colQueryBuilder.append("fcd.id = jedcd.").append(column).append(" or ");
                        if (isForKnockOff) {
                            colQueryBuilder.append("fcd.id = jedc.").append(column).append(" or ");
                        }
                    }
                }
                customQuery += "inner join fieldparams fp on fcd.fieldid =fp.id and fp.fieldlabel=? and fp.companyid=je.company";
                params.add(columnheader);
                customQuery += colQueryBuilder.toString();
                customQuery = customQuery.substring(0, customQuery.lastIndexOf(" or "));
                customQuery += ")";

            } // In case of custom field
            else {
                if (isProductCustomData) {
                    customQuery = "inner join accjedetailproductcustomdata jedcd on jedcd.jedetailid=jed.id \n";
                } else {
                    if (iscustomcolumndata == 0) {
                        customQuery = "inner join accjecustomdata jedcd on jedcd.journalentryid=je.id \n";
                    } else {
                        customQuery = "inner join accjedetailcustomdata jedcd on jedcd.jedetailid=jed.id \n";
                    }
                }
                for (String custValue : customdatavalues) {
                    selectAttr += ",'" + custValue + "'";
                    selectSubAttr += ",'" + custValue + "'";
                    StringBuilder colQueryBuilder = new StringBuilder(" and (");
                    for (String column : columns) {
                        colQueryBuilder.append("jedcd.").append(column).append("=?").append(" or ");
                        params.add(custValue);
                    }
                    customQuery += colQueryBuilder.toString();
                    customQuery = customQuery.substring(0, customQuery.lastIndexOf(" or "));
                    customQuery += ")";
                }

            }
        } else {
            if (customdatavalues != null) {
                for (String custValue : customdatavalues) {
                    selectAttr += ",'" + custValue + "'";
                }
            }
        }

        //When monthly reports are run
        if (isMonthly) {
            selectAttr += ",je.entrydate";
            selectSubAttr += ",YEAR(a.entrydate),MONTH(a.entrydate)";
            groupQuery += ",YEAR(a.entrydate),MONTH(a.entrydate)";
        }
        
        String query = "";
        if (isConsolidatedPNL) {
            query = "select a.debit,a.company, a.name , sum(case when a.debit='T' then a.amountinbase else -a.amountinbase end), a.account ";
        } else {
            query = "select a.debit,sum(case when a.debit='T' then a.amountinbase else -a.amountinbase end),a.account ";
        }
        if (isConsolidatedPNL) {
            querycondition = " and je.company in (" + requestParams.get("multiCompanyid") + ") ";
        } else {
            querycondition = " and je.company = ? ";
            params.add(companyid);
        }

        String Searchjson="",filterConjuctionCriteria="";
        Searchjson = (String) requestParams.get("Searchjson");
        boolean isKnockOffAdvancedSearch = false;
        boolean isLineDimPresentInAdvSearch = false;
        if (!StringUtil.isNullOrEmpty(Searchjson)) {
            isKnockOffAdvancedSearch = fieldManagerDAOobj.isKnockOffAdvancedSearch(Searchjson, companyid);
        } 
        
        //In case of dimension based export from monthly reports search json is null because we modify search json and put with new key.
        if (!isDimensionBased) {
        if (StringUtil.isNullOrEmpty(Searchjson)) {
            querycondition += " and jed.isseparated = 'F' ";
        }else{
            if (isAdvanceSearchOnGlobalDimension(Searchjson) && !isKnockOffAdvancedSearch) {
                /*
                 * If advance search is performed on only Global custom
                 * field/dimension then inner join is not get applied on
                 * accjedetailcustom data. So additional(separated jed) as well as default entry
                 * for payment method get fetched so payment method's amount get
                 * doubled.So added check to restrict additional jedetails.
                     */
                    querycondition += " and jed.isseparated = 'F' ";
                } else {
                    isLineDimPresentInAdvSearch = true;
                }
            }
        }
        //Advance search code
        String advSearchSqljoin = "", advSearchCustomtablejoin = "", advSearchMySearchFilterString = "";
        if (requestParams.containsKey("Searchjson") && !StringUtil.isNullOrEmpty((String) requestParams.get("Searchjson"))) {
            Map<String, Object> advSearchAttributes = null;            
            filterConjuctionCriteria = (String) requestParams.get("filterConjuctionCriteria");
            if (preferences != null && preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                advSearchAttributes = getPerpetualInvAdvanceSearchAttributes(advSearchMySearchFilterString, Searchjson, null, companyid, new ArrayList(), filterConjuctionCriteria, advSearchCustomtablejoin, "", advSearchSqljoin);
            } else {

                if (isKnockOffAdvancedSearch) {
                    String knockOffCondition="";
                    advSearchAttributes = getKnockOffAdvanceSearchAttributes(advSearchMySearchFilterString, Searchjson, null, companyid, new ArrayList(), filterConjuctionCriteria, advSearchCustomtablejoin, "",isLineDimPresentInAdvSearch);
                    if(advSearchAttributes.containsKey("knockOffCondition") && advSearchAttributes.get("knockOffCondition")!=null){
                        knockOffCondition=(String)advSearchAttributes.get("knockOffCondition");
                    }
                    querycondition += knockOffCondition;
                } else {
                    advSearchAttributes = getSQLDefaultAdvanceSearchAttributes(advSearchMySearchFilterString, Searchjson, null, companyid, new ArrayList(), filterConjuctionCriteria,isLineDimPresentInAdvSearch);
                }
            }
            advSearchMySearchFilterString = advSearchAttributes.containsKey("mySearchFilterString") ? (String) advSearchAttributes.get("mySearchFilterString") : "";
            advSearchCustomtablejoin = advSearchAttributes.containsKey("customtablejoin") ? (String) advSearchAttributes.get("customtablejoin") : "";
            advSearchSqljoin = advSearchAttributes.containsKey("sqljoin") ? (String) advSearchAttributes.get("sqljoin") : "";
            if (advSearchAttributes.containsKey("params") && advSearchAttributes.get("params") != null && !((ArrayList) advSearchAttributes.get("params")).isEmpty()) {
                params.addAll((ArrayList) advSearchAttributes.get("params"));
            }
        }

        String accountCondition = "";
        if(requestParams.containsKey("accounttype")){
            accountCondition += " and acc.accounttype = "+requestParams.get("accounttype");
        }
        query = query + selectSubAttr + " from \n"
                + "( select distinct jed.id,jed.debit,jed.amountinbase,jed.account, acc.company, acc.name " + selectAttr + " from jedetail jed\n"
                + "inner join journalentry je  on jed.journalentry=je.id and " + (isOpening ? "je.entryDate<?" : " je.entryDate>=? and je.entryDate<=? ") + jeSearchQuery + " \n"
                + "inner join account acc on acc.id = jed.account "+accountCondition+"\n"
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + customQuery + " where je.deleteflag='F' and je.pendingapproval = 0 and je.isdraft=0 and je.istemplate != 2 and je.approvestatuslevel=11 " +querycondition+ advSearchMySearchFilterString + " ) a \n" + groupQuery;
        List list = executeSQLQuery(query, params.toArray());
        return list;
    }
    
    public KwlReturnObject getAccountBalanceAmount(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, String costCenterID, String filterConjuctionCriteria, String Searchjson, Map<String, Object> advSearchAttributes) throws ServiceException {
        List list = new ArrayList();
        String mySearchFilterString = "";
        String  sqljoin = "", companyid = "", sqlcondition = " ",customtablejoin=" ";
        boolean isKnockOffAdvancedSearch = false;
        CompanyAccountPreferences preferences = null;
        if (requestParams.containsKey(Constants.companyKey) && requestParams.get(Constants.companyKey) != null) {
            companyid = requestParams.get(Constants.companyKey).toString();
        }
        if (!StringUtil.isNullOrEmpty(companyid)) {
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        }
        int accountTransactionType = requestParams.containsKey("accountTransactionType") && requestParams.get("accountTransactionType")!=null?Integer.parseInt(requestParams.get("accountTransactionType").toString()):Constants.All_Transaction_TypeID;
        String selectedCurrencyIds = (String) requestParams.get("selectedCurrencyIds");
//        String query = "select sum(case when debit=true then amountinbase else -amountinbase end) from JournalEntryDetail jed " + joinString + " where account.ID=? and jed.journalEntry.deleted=false and jed.journalEntry.pendingapproval = 0 and jed.journalEntry.draft=false and jed.journalEntry.istemplate != 2 and jed.journalEntry.approvestatuslevel=11 and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<=? ";
        String sqlquery = "select sum(case when debit='T' then amountinbase else -amountinbase end) from jedetail jed  INNER JOIN journalentry je ON jed.journalentry = je.id ";

        ArrayList params = new ArrayList();
        params.add(accountid);
        if (startDate == null) {
            startDate = new Date(0);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        params.add(startDate);
        params.add(endDate);

        
        
        
        
        if (!StringUtil.isNullOrEmpty(costCenterID)) {
//            query += " and jed.journalEntry.costcenter.ID=?";
            sqljoin += " LEFT JOIN costcenter ON costcenter.id=je.costcenter ";
            sqlcondition += " and costcenter.id=? ";
            params.add(costCenterID);
        }
        if (!StringUtil.isNullOrEmpty(selectedCurrencyIds)) {
            selectedCurrencyIds = AccountingManager.getFilterInString(selectedCurrencyIds);
//            query += " and jed.journalEntry.currency.currencyID in " + selectedCurrencyIds + " ";
            sqljoin += " LEFT JOIN currency ON currency.currencyid=je.currency ";
            sqlcondition += " and currency.currencyid in " + selectedCurrencyIds + " ";
        }

        //below filter is given in GL Report on transaction
        if (accountTransactionType == Constants.Acc_Make_Payment_ModuleId) {
//            query += " and (jed.journalEntry.transactionModuleid =" + Constants.Acc_Make_Payment_ModuleId + " or jed.journalEntry.transactionModuleid =" + Constants.Acc_Dishonoured_Make_Payment_ModuleId + ")" + "";
            sqlcondition += " and (je.transactionModuleid =" + Constants.Acc_Make_Payment_ModuleId + " or je.transactionModuleid =" + Constants.Acc_Dishonoured_Make_Payment_ModuleId + ")" + "";
        } else if (accountTransactionType == Constants.Acc_Receive_Payment_ModuleId) {
//            query += " and (jed.journalEntry.transactionModuleid =" + Constants.Acc_Receive_Payment_ModuleId + " or jed.journalEntry.transactionModuleid =" + Constants.Acc_Dishonoured_Receive_Payment_ModuleId + ")" + "";
            sqlcondition += " and (je.transactionModuleid =" + Constants.Acc_Receive_Payment_ModuleId + " or je.transactionModuleid =" + Constants.Acc_Dishonoured_Receive_Payment_ModuleId + ")" + "";
        }
//        if(!StringUtil.isNullOrEmpty(selectedCurrencyIds)){ 
//            selectedCurrencyIds = AccountingManager.getFilterInString(selectedCurrencyIds);
//            query += " and jed.account.currency.currencyID in "+selectedCurrencyIds+" ";               
//        }



        /*
         Execute IF block only in case of perpetual inventory activated.
         */
        if(!StringUtil.isNullOrEmpty(Searchjson)){
            if(advSearchAttributes == null || advSearchAttributes.isEmpty()){
                advSearchAttributes = getAdvanceSearchAttributes(Searchjson, preferences, companyid, accountid, filterConjuctionCriteria);
            }
            params.addAll((ArrayList)advSearchAttributes.get("params"));            
            mySearchFilterString += advSearchAttributes.containsKey("mySearchFilterString") && !StringUtil.isNullOrEmpty((String)advSearchAttributes.get("mySearchFilterString")) ? (String)advSearchAttributes.get("mySearchFilterString"): "";
            customtablejoin += advSearchAttributes.containsKey("customtablejoin") && !StringUtil.isNullOrEmpty((String)advSearchAttributes.get("customtablejoin")) ? (String)advSearchAttributes.get("customtablejoin"): "";
            sqljoin += advSearchAttributes.containsKey("sqljoin") && !StringUtil.isNullOrEmpty((String)advSearchAttributes.get("sqljoin")) ? (String)advSearchAttributes.get("sqljoin"): "";
//            advancesearch = (Boolean)advSearchAttributes.get("advancesearch");
        }
        
        if(advSearchAttributes!=null && !advSearchAttributes.isEmpty()){
            if(advSearchAttributes.containsKey("isKnockOffAdvancedSearch")){
                isKnockOffAdvancedSearch=(Boolean)advSearchAttributes.get("isKnockOffAdvancedSearch");
            }
        }
        
        if (StringUtil.isNullOrEmpty(Searchjson)) {
//            query += " and jed.isSeparated = false ";
            sqlcondition += " and jed.isseparated = 'F' ";
        }else{
            /*
             * Executing sql query in case of advance search
             */
            if (isAdvanceSearchOnGlobalDimension(Searchjson) && !isKnockOffAdvancedSearch) {
                /*
                 * If advance search is performed on only Global custom
                 * field/dimension then inner join is not get applied on
                 * accjedetailcustom data. So additional(separated jed) as well as default entry
                 * for payment method get fetched so payment method's amount get
                 * doubled.So added check to restrict additional jedetails.
                 */
//                query += " and jed.isSeparated = false ";
                sqlcondition += " and jed.isseparated = 'F' ";
            }
        }
        
        if (isKnockOffAdvancedSearch) {
            String knockOffCondition = "";
            if (advSearchAttributes.containsKey("knockOffCondition") && advSearchAttributes.get("knockOffCondition") != null) {
                knockOffCondition = (String) advSearchAttributes.get("knockOffCondition");
            }
            sqlcondition += knockOffCondition;
        }
            
//        if (advancesearch) {
            sqlquery = sqlquery + sqljoin + customtablejoin + " where jed.account=? and je.deleteflag='F' and je.pendingapproval = 0 and je.isdraft=0 and je.istemplate != 2 and je.approvestatuslevel=11 and je.entrydate>=? and je.entrydate<=? " + sqlcondition + mySearchFilterString;
            list = executeSQLQuery(sqlquery, params.toArray());
//        } else {
//            list = executeQuery(query + mySearchFilterString, params.toArray());
//        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public Map<String, Object> getAdvanceSearchAttributes(String Searchjson, CompanyAccountPreferences preferences, String companyid, String accountid, String filterConjuctionCriteria) throws ServiceException{
        boolean advancesearch = false;
        boolean isLineDimPresentInAdvSearch = false;
        boolean isKnockOffAdvancedSearch = false;
        boolean isAdvanceSearchOnGlobalDimension = false;
        String mySearchFilterString = "", customtablejoin = "", appendSearchString = "", sqljoin = "";
        ArrayList params = params = new ArrayList();
        if (!StringUtil.isNullObject(Searchjson)) {
            isKnockOffAdvancedSearch = fieldManagerDAOobj.isKnockOffAdvancedSearch(Searchjson, companyid);
        }
        if (!StringUtil.isNullOrEmpty(Searchjson)) {

            /*
             * Executing sql query in case of advance search
             */
            if (!isAdvanceSearchOnGlobalDimension(Searchjson)) {
                isLineDimPresentInAdvSearch = true;
            } else {
                isAdvanceSearchOnGlobalDimension = true;
            }
        }
        Map<String, Object> advSearchAttributes = new HashMap<String, Object>();
        if ((!StringUtil.isNullOrEmpty(Searchjson)) && preferences != null && preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
            advancesearch = true;
            advSearchAttributes = getPerpetualInvAdvanceSearchAttributes(mySearchFilterString, Searchjson, accountid, companyid, params, filterConjuctionCriteria, customtablejoin, appendSearchString, sqljoin);

        } else if (!StringUtil.isNullOrEmpty(Searchjson) && isKnockOffAdvancedSearch) {
            advancesearch = true;
            advSearchAttributes = getKnockOffAdvanceSearchAttributes(mySearchFilterString, Searchjson, accountid, companyid, params, filterConjuctionCriteria, customtablejoin, appendSearchString, isLineDimPresentInAdvSearch);
        } else if (!StringUtil.isNullOrEmpty(Searchjson)) {
            advancesearch = true;
            advSearchAttributes = getSQLDefaultAdvanceSearchAttributes(mySearchFilterString, Searchjson, accountid, companyid, params, filterConjuctionCriteria, isLineDimPresentInAdvSearch);
        }
        advSearchAttributes.put("advancesearch", advancesearch);
        advSearchAttributes.put("isKnockOffAdvancedSearch", isKnockOffAdvancedSearch);
        advSearchAttributes.put("isAdvanceSearchOnGlobalDimension", isAdvanceSearchOnGlobalDimension);
        return advSearchAttributes;
    }
    
    public Map<String, Object> getDefaultAdvanceSearchAttributes(String mySearchFilterString, String Searchjson, String accountid, String companyid, ArrayList params, String filterConjuctionCriteria) throws ServiceException {
        Map<String, Object> advSearchAttributes = new HashMap<String, Object>();
        Searchjson = getJsornStringForSearch(Searchjson, accountid, companyid);
        HashMap<String, Object> request = new HashMap<String, Object>();
        request.put(Constants.Searchjson, Searchjson);
        request.put(Constants.appendCase, "and");
        request.put(Constants.moduleid, "100");
        request.put("filterConjuctionCriteria", filterConjuctionCriteria);

        try {
            mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
            mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "jed.journalEntry.accBillInvCustomData");
            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//             
            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");//             
            StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
        } catch (JSONException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        advSearchAttributes.put("mySearchFilterString", mySearchFilterString);
        advSearchAttributes.put("params", params);
        return advSearchAttributes;
    }
    public Map<String, Object> getSQLDefaultAdvanceSearchAttributes(String mySearchFilterString, String Searchjson, String accountid, String companyid, ArrayList params, String filterConjuctionCriteria,boolean isLineDimPresentInAdvSearch) throws ServiceException {
        Map<String, Object> advSearchAttributes = new HashMap<String, Object>();
        Searchjson = getJsornStringForSearch(Searchjson, accountid, companyid);
        String customtablejoin = "";
        HashMap<String, Object> request = new HashMap<String, Object>();
        request.put(Constants.Searchjson, Searchjson);
        request.put(Constants.appendCase, "and");
        request.put(Constants.moduleid, "100");
        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
        String join = " left join ";
        if (isLineDimPresentInAdvSearch && filterConjuctionCriteria.trim().equalsIgnoreCase(Constants.or.trim())) {
            /*
             * Taking inner join if searching on line and global custom field
             * with OR conjunction criteria to excluded separated records
             */
            join = " inner join ";
        }

        try {
            mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
            if (mySearchFilterString.contains("AccJECustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "accjecustomdata");
                customtablejoin += join + " accjecustomdata  ON je.id=accjecustomdata.journalentryId ";
            }
            if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//      
                customtablejoin += join + " accjedetailcustomdata  ON jed.id=accjedetailcustomdata.jedetailId ";
            }
            if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "accjedetailproductcustomdata");//     
                customtablejoin += " LEFT JOIN accjedetailproductcustomdata ON jed.id=accjedetailproductcustomdata.jedetailId ";
            }
            StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
        } catch (JSONException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        advSearchAttributes.put("mySearchFilterString", mySearchFilterString);
        advSearchAttributes.put("params", params);
        advSearchAttributes.put("customtablejoin", customtablejoin);
        return advSearchAttributes;
    }
    
    public Map<String, Object> getKnockOffAdvanceSearchAttributes(String mySearchFilterString, String Searchjson, String accountid, String companyid, ArrayList params, String filterConjuctionCriteria, String customtablejoin, String appendSearchString,boolean isLineDimPresentInAdvSearch) throws ServiceException{
        Map<String, Object> advSearchAttributes = new HashMap<String, Object>();
        try {
                HashMap<String, Object> request = new HashMap<String, Object>();
                String join = " left join ";
                if (isLineDimPresentInAdvSearch && filterConjuctionCriteria.trim().equalsIgnoreCase(Constants.or.trim())) {
                /*
                 * Taking inner join if searching on line and global custom
                 * field with OR conjunction criteria to excluded separated
                 * records
                 */
                   join = " inner join ";
                }
                Searchjson = getJsornStringForSearch(Searchjson, accountid,companyid);
                request.put(Constants.Searchjson, Searchjson);
                request.put(Constants.appendCase, "and");
                request.put(Constants.moduleid, "100");
                request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                if (mySearchFilterString.contains("AccJECustomData")) {
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "accjecustomdata");
                    customtablejoin += join + " accjecustomdata  ON je.id=accjecustomdata.journalentryId ";
                }
                if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//  
                    customtablejoin += join + " accjedetailcustomdata  ON jed.id=accjedetailcustomdata.jedetailId ";
                }
                if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "accjedetailproductcustomdata");//  
                    customtablejoin += " LEFT JOIN accjedetailproductcustomdata ON jed.id=accjedetailproductcustomdata.jedetailId ";
                }
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(appendSearchString, mySearchFilterString, " or ");
            } catch (Exception ex) {
                throw ServiceException.FAILURE("accJournalEntryImpl.getKnockOffAdvanceSearchAttributes" + ex.getMessage(), ex);
            }
        advSearchAttributes.put("mySearchFilterString", mySearchFilterString);
        advSearchAttributes.put("params", params);
        advSearchAttributes.put("customtablejoin", customtablejoin);
        String knockOffCondition="  and  (((isnull(je.transactionModuleid) or je.transactionModuleid in ('"+Constants.Acc_Party_Journal_Entry+"','"+Constants.Acc_Make_Payment_ModuleId+"','"+Constants.Acc_Receive_Payment_ModuleId+"','"+Constants.Acc_GENERAL_LEDGER_ModuleId+"')) and (jed.isseparated = 'F' or jed.isseparated = 'T')) "
                + " or (je.transactionModuleid in ('"+Constants.Acc_Invoice_ModuleId+"','"+Constants.Acc_Vendor_Invoice_ModuleId+"','"+Constants.Acc_Debit_Note_ModuleId+"','"+Constants.Acc_Credit_Note_ModuleId+"') and jed.isseparated = 'F')) ";
        advSearchAttributes.put("knockOffCondition", knockOffCondition);
        return advSearchAttributes;
    }
    
    public Map<String, Object> getPerpetualInvAdvanceSearchAttributes(String mySearchFilterString, String Searchjson, String accountid, String companyid, ArrayList params, String filterConjuctionCriteria, String customtablejoin, String appendSearchString, String sqljoin) throws ServiceException{
        Map<String, Object> advSearchAttributes = new HashMap<String, Object>();
        HashMap<String, Object> request = new HashMap<String, Object>();
        JSONArray doJson = new JSONArray();
        JSONArray grJson = new JSONArray();
        JSONArray prJson = new JSONArray();
        JSONArray srJson = new JSONArray();
        JSONArray stockJson = new JSONArray();
        String myPRFilterString = "", mySRFilterString = "", myStockFilterString = "";
        String myGRFilterString = "", myDOFilterString = "";

        String iscustomcolumn = "", iscustomcolumndata = "", isfrmpmproduct = "", fieldtype = "", searchText = "", columnheader = "",
                xtype = "", combosearch = "", isinterval = "", interval = "", isbefore = "", isdefaultfield = "", isForProductMasterOnly = "";

        try {

            JSONObject SearchJsonObj = new JSONObject(Searchjson);
            JSONArray SearchJsonArray = SearchJsonObj.getJSONArray("root");

            for (int searckArr = 0; searckArr < SearchJsonArray.length(); searckArr++) {
                JSONObject compareObj = SearchJsonArray.optJSONObject(searckArr);
                iscustomcolumn = compareObj.optString("iscustomcolumn");
                iscustomcolumndata = compareObj.optString("iscustomcolumndata");
                isfrmpmproduct = compareObj.optString("isfrmpmproduct");
                fieldtype = compareObj.optString("fieldtype");
                searchText = compareObj.optString("searchText");
                columnheader = compareObj.optString("columnheader");
                columnheader = StringUtil.DecodeText(columnheader);
                xtype = compareObj.optString("xtype");
                combosearch = StringUtil.DecodeText(compareObj.optString("combosearch"));
                isinterval = compareObj.optString("isinterval");
                interval = compareObj.optString("interval");
                isbefore = compareObj.optString("isbefore");
                isdefaultfield = compareObj.optString("isdefaultfield");
                isForProductMasterOnly = compareObj.optString("isForProductMasterOnly");

                String[] coldataArray = combosearch.split(",");
                String Coldata = "";
                for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                    Coldata += "'" + coldataArray[countArray] + "',";
                }
                Coldata = Coldata.substring(0, Coldata.length() - 1);
                HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
//                        columnheader = compareObj.optString("columnheader");
                requestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, "fieldlabel"));
                requestParams1.put(Constants.filter_values, Arrays.asList(companyid, columnheader));
                KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParams1);
                List<FieldParams> lst = result.getEntityList();
                for (FieldParams fieldParams : lst) {
                    int module = fieldParams.getModuleid();
                    iscustomcolumndata = fieldParams.isIsForKnockOff() ? (fieldParams.getCustomcolumn() == 1 ? "true" : "false") : compareObj.getString("iscustomcolumndata");
                    if (module == Constants.Acc_Delivery_Order_ModuleId) {
                        if (fieldtype.equalsIgnoreCase("" + 4) || fieldtype.equalsIgnoreCase("" + 7) || fieldtype.equalsIgnoreCase("" + 12)) {
                            searchText = fieldManagerDAOobj.getIdsUsingParamsValue(fieldParams.getId(), Coldata.replaceAll("'", ""));
                        }
                        JSONObject cntObj = new JSONObject();
                        cntObj.put("iscustomcolumn", iscustomcolumn);
                        cntObj.put("iscustomcolumndata", iscustomcolumndata);
                        cntObj.put("isfrmpmproduct", isfrmpmproduct);
                        cntObj.put("fieldtype", fieldtype);
                        cntObj.put("columnheader", columnheader);
                        cntObj.put("xtype", xtype);
                        cntObj.put("isinterval", isinterval);
                        cntObj.put("interval", interval);
                        cntObj.put("isbefore", isbefore);
                        cntObj.put("isdefaultfield", isdefaultfield);
                        cntObj.put("isForProductMasterOnly", isForProductMasterOnly);
                        cntObj.put("searchText", searchText);
                        cntObj.put("search", searchText);
                        cntObj.put("combosearch", combosearch);
                        cntObj.put("column", fieldParams.getId());
                        cntObj.put("refdbname", "Col" + fieldParams.getColnum());
                        cntObj.put("xfield", "Col" + fieldParams.getColnum());
                        cntObj.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                        doJson.put(cntObj);

                    } else if (module == Constants.Acc_Goods_Receipt_ModuleId) {
                        if (fieldtype.equalsIgnoreCase("" + 4) || fieldtype.equalsIgnoreCase("" + 7) || fieldtype.equalsIgnoreCase("" + 12)) {
                            searchText = fieldManagerDAOobj.getIdsUsingParamsValue(fieldParams.getId(), Coldata.replaceAll("'", ""));
                        }
                        JSONObject cntObj = new JSONObject();
                        cntObj.put("iscustomcolumn", iscustomcolumn);
                        cntObj.put("iscustomcolumndata", iscustomcolumndata);
                        cntObj.put("isfrmpmproduct", isfrmpmproduct);
                        cntObj.put("fieldtype", fieldtype);
                        cntObj.put("columnheader", columnheader);
                        cntObj.put("xtype", xtype);
                        cntObj.put("isinterval", isinterval);
                        cntObj.put("interval", interval);
                        cntObj.put("isbefore", isbefore);
                        cntObj.put("isdefaultfield", isdefaultfield);
                        cntObj.put("isForProductMasterOnly", isForProductMasterOnly);
                        cntObj.put("searchText", searchText);
                        cntObj.put("search", searchText);
                        cntObj.put("combosearch", combosearch);
                        cntObj.put("column", fieldParams.getId());
                        cntObj.put("refdbname", "Col" + fieldParams.getColnum());
                        cntObj.put("xfield", "Col" + fieldParams.getColnum());
                        cntObj.put("moduleid", Constants.Acc_Goods_Receipt_ModuleId);
                        grJson.put(cntObj);

                    } else if (module == Constants.Acc_Sales_Return_ModuleId) {
                        if (fieldtype.equalsIgnoreCase("" + 4) || fieldtype.equalsIgnoreCase("" + 7) || fieldtype.equalsIgnoreCase("" + 12)) {
                            searchText = fieldManagerDAOobj.getIdsUsingParamsValue(fieldParams.getId(), Coldata.replaceAll("'", ""));
                        }
                        JSONObject cntObj = new JSONObject();
                        cntObj.put("iscustomcolumn", iscustomcolumn);
                        cntObj.put("iscustomcolumndata", iscustomcolumndata);
                        cntObj.put("isfrmpmproduct", isfrmpmproduct);
                        cntObj.put("fieldtype", fieldtype);
                        cntObj.put("columnheader", columnheader);
                        cntObj.put("xtype", xtype);
                        cntObj.put("isinterval", isinterval);
                        cntObj.put("interval", interval);
                        cntObj.put("isbefore", isbefore);
                        cntObj.put("isdefaultfield", isdefaultfield);
                        cntObj.put("isForProductMasterOnly", isForProductMasterOnly);
                        cntObj.put("searchText", searchText);
                        cntObj.put("search", searchText);
                        cntObj.put("combosearch", combosearch);
                        cntObj.put("column", fieldParams.getId());
                        cntObj.put("refdbname", "Col" + fieldParams.getColnum());
                        cntObj.put("xfield", "Col" + fieldParams.getColnum());
                        cntObj.put("moduleid", Constants.Acc_Sales_Return_ModuleId);
                        srJson.put(cntObj);

                    } else if (module == Constants.Acc_Purchase_Return_ModuleId) {
                        if (fieldtype.equalsIgnoreCase("" + 4) || fieldtype.equalsIgnoreCase("" + 7) || fieldtype.equalsIgnoreCase("" + 12)) {
                            searchText = fieldManagerDAOobj.getIdsUsingParamsValue(fieldParams.getId(), Coldata.replaceAll("'", ""));
                        }
                        JSONObject cntObj = new JSONObject();
                        cntObj.put("iscustomcolumn", iscustomcolumn);
                        cntObj.put("iscustomcolumndata", iscustomcolumndata);
                        cntObj.put("isfrmpmproduct", isfrmpmproduct);
                        cntObj.put("fieldtype", fieldtype);
                        cntObj.put("columnheader", columnheader);
                        cntObj.put("xtype", xtype);
                        cntObj.put("isinterval", isinterval);
                        cntObj.put("interval", interval);
                        cntObj.put("isbefore", isbefore);
                        cntObj.put("isdefaultfield", isdefaultfield);
                        cntObj.put("isForProductMasterOnly", isForProductMasterOnly);
                        cntObj.put("searchText", searchText);
                        cntObj.put("search", searchText);
                        cntObj.put("combosearch", combosearch);
                        cntObj.put("column", fieldParams.getId());
                        cntObj.put("refdbname", "Col" + fieldParams.getColnum());
                        cntObj.put("xfield", "Col" + fieldParams.getColnum());
                        cntObj.put("moduleid", Constants.Acc_Purchase_Return_ModuleId);
                        prJson.put(cntObj);
                    } else if (module == Constants.Inventory_Stock_Adjustment_ModuleId) {
                        if (fieldtype.equalsIgnoreCase("" + 4) || fieldtype.equalsIgnoreCase("" + 7) || fieldtype.equalsIgnoreCase("" + 12)) {
                            searchText = fieldManagerDAOobj.getIdsUsingParamsValue(fieldParams.getId(), Coldata.replaceAll("'", ""));
                        }
                        JSONObject cntObj = new JSONObject();
                        cntObj.put("iscustomcolumn", iscustomcolumn);
                        cntObj.put("iscustomcolumndata", iscustomcolumndata);
                        cntObj.put("isfrmpmproduct", isfrmpmproduct);
                        cntObj.put("fieldtype", fieldtype);
                        cntObj.put("columnheader", columnheader);
                        cntObj.put("xtype", xtype);
                        cntObj.put("isinterval", isinterval);
                        cntObj.put("interval", interval);
                        cntObj.put("isbefore", isbefore);
                        cntObj.put("isdefaultfield", isdefaultfield);
                        cntObj.put("isForProductMasterOnly", isForProductMasterOnly);
                        cntObj.put("searchText", searchText);
                        cntObj.put("search", searchText);
                        cntObj.put("combosearch", combosearch);
                        cntObj.put("column", fieldParams.getId());
                        cntObj.put("refdbname", "Col" + fieldParams.getColnum());
                        cntObj.put("xfield", "Col" + fieldParams.getColnum());
                        cntObj.put("moduleid", Constants.Inventory_Stock_Adjustment_ModuleId);
                        stockJson.put(cntObj);

                    }
                }

            }
            HashMap<String, Object> advRequestParams = new HashMap();

            /*
             Get filter string for deliveryorder module
             */
            boolean isInnerJoinAppend = false;
            String innerJoinOnDetailTable = "";
            if ((doJson.length() > 0)) {
                JSONObject putSearchJson = new JSONObject();
                putSearchJson.put("root", doJson);
                advRequestParams.clear();
                advRequestParams.put(Constants.Searchjson, putSearchJson);
                advRequestParams.put(Constants.appendCase, "AND");
                advRequestParams.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                advRequestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                myDOFilterString += String.valueOf(StringUtil.getMyAdvanceSearchString(advRequestParams, true).get(Constants.myResult));

                if (myDOFilterString.contains("deliveryordercustomdata")) {
                    sqljoin += " LEFT JOIN deliveryorder  ON deliveryorder.company = je.company  and je.id  = deliveryorder.inventoryje ";
                    customtablejoin += " LEFT JOIN deliveryordercustomdata  ON deliveryordercustomdata.deliveryOrderId=deliveryorder.id ";

                }
                innerJoinOnDetailTable = "  LEFT JOIN dodetails on dodetails.company = je.company  and (jed.id = dodetails.costofgoodssoldjedetail or jed.id = dodetails.inventoryjedetail)  ";
                if (myDOFilterString.contains("AccJEDetailCustomData")) {
                    myDOFilterString = myDOFilterString.replaceAll("AccJEDetailCustomData", "dodetailscustomdata");//  
                    sqljoin += innerJoinOnDetailTable;
                    customtablejoin += " LEFT JOIN dodetailscustomdata  ON dodetailscustomdata.dodetailsid=dodetails.id ";
                    isInnerJoinAppend = true;

                }
                if (myDOFilterString.contains("AccJEDetailsProductCustomData")) {
                    myDOFilterString = myDOFilterString.replaceAll("AccJEDetailsProductCustomData", "dodetailproductcustomdata");
                    customtablejoin += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join dodetailproductcustomdata on dodetails.id=dodetailproductcustomdata.doDetailID ";
                }
                StringUtil.insertParamAdvanceSearchString1(params, putSearchJson.toString());
                appendSearchString = StringUtil.combineCustomSearchStrings(appendSearchString, myDOFilterString, StringUtil.isNullOrEmpty(appendSearchString) ? "":" or ");
            }

            /*
             Get filter string for Goods Receipt module
             */
            if ((grJson.length() > 0)) {
                JSONObject putSearchJson = new JSONObject();
                putSearchJson.put("root", grJson);
                advRequestParams.clear();
                advRequestParams.put(Constants.Searchjson, putSearchJson);
                advRequestParams.put(Constants.appendCase, "AND");
                advRequestParams.put(Constants.moduleid, Constants.Acc_Goods_Receipt_ModuleId);
                advRequestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                myGRFilterString += String.valueOf(StringUtil.getMyAdvanceSearchString(advRequestParams, true).get(Constants.myResult));

                if (myGRFilterString.contains("grordercustomdata")) {
                    sqljoin += " LEFT JOIN grorder  ON grorder.company = je.company  and je.id  = grorder.inventoryje ";
                    customtablejoin += " LEFT JOIN grordercustomdata  ON grordercustomdata.goodsreceiptorderid=grorder.id ";

                }
                isInnerJoinAppend = false;
                innerJoinOnDetailTable = " LEFT JOIN grodetails on grodetails.company = je.company  and (jed.id = grodetails.purchasesjedetail or jed.id = grodetails.inventoryjedetail) ";
                if (myGRFilterString.contains("AccJEDetailCustomData")) {
                    myGRFilterString = myGRFilterString.replaceAll("AccJEDetailCustomData", "grodetailscustomdata");//  
                    sqljoin += innerJoinOnDetailTable;
                    customtablejoin += " LEFT JOIN grodetailscustomdata  ON grodetailscustomdata.grodetailsid=grodetails.id ";
                    isInnerJoinAppend = true;

                }
                if (myGRFilterString.contains("AccJEDetailsProductCustomData")) {
                    myGRFilterString = myDOFilterString.replaceAll("AccJEDetailsProductCustomData", "grodetailproductcustomdata");
                    customtablejoin += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join grodetailproductcustomdata on grodetails.id=grodetailproductcustomdata.grDetailID ";
                }
                StringUtil.insertParamAdvanceSearchString1(params, putSearchJson.toString());
                appendSearchString = StringUtil.combineCustomSearchStrings(appendSearchString, myGRFilterString, StringUtil.isNullOrEmpty(appendSearchString) ? "":" or ");
            }
            /*
             Get filter string for Purchase Return module
             */
            if ((prJson.length() > 0)) {
                JSONObject putSearchJson = new JSONObject();
                putSearchJson.put("root", prJson);
                advRequestParams.clear();
                advRequestParams.put(Constants.Searchjson, putSearchJson);
                advRequestParams.put(Constants.appendCase, "AND");
                advRequestParams.put(Constants.moduleid, Constants.Acc_Purchase_Return_ModuleId);
                advRequestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                myPRFilterString += String.valueOf(StringUtil.getMyAdvanceSearchString(advRequestParams, true).get(Constants.myResult));

                if (myPRFilterString.contains("purchasereturncustomdata")) {
                    sqljoin += " LEFT JOIN purchasereturn  ON purchasereturn.company = je.company  and je.id  = purchasereturn.inventoryje ";
                    customtablejoin += " LEFT JOIN purchasereturncustomdata  ON purchasereturncustomdata.purchasereturnid=purchasereturn.id ";

                }
                isInnerJoinAppend = false;
                innerJoinOnDetailTable = " LEFT JOIN prdetails on prdetails.company = je.company  and (jed.id = prdetails.purchasesjedetail or  jed.id = prdetails.inventoryjedetail) ";
                if (myPRFilterString.contains("AccJEDetailCustomData")) {
                    myPRFilterString = myPRFilterString.replaceAll("AccJEDetailCustomData", "prdetailscustomdata");//  
                    sqljoin += innerJoinOnDetailTable;
                    customtablejoin += " LEFT JOIN prdetailscustomdata  ON prdetailscustomdata.prdetailsid=prdetails.id ";
                    isInnerJoinAppend = true;

                }
                if (myPRFilterString.contains("AccJEDetailsProductCustomData")) {
                    myPRFilterString = myPRFilterString.replaceAll("AccJEDetailsProductCustomData", "prdetailproductcustomdata");
                    customtablejoin += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join prdetailproductcustomdata on prdetails.id=prdetailproductcustomdata.prDetailID ";
                }
                StringUtil.insertParamAdvanceSearchString1(params, putSearchJson.toString());
                appendSearchString = StringUtil.combineCustomSearchStrings(appendSearchString, myPRFilterString, StringUtil.isNullOrEmpty(appendSearchString) ? "":" or ");
            }
            /*
             Get filter string for Sales Return module
             */
            if ((srJson.length() > 0)) {
                JSONObject putSearchJson = new JSONObject();
                putSearchJson.put("root", srJson);
                advRequestParams.clear();
                advRequestParams.put(Constants.Searchjson, putSearchJson);
                advRequestParams.put(Constants.appendCase, "AND");
                advRequestParams.put(Constants.moduleid, Constants.Acc_Sales_Return_ModuleId);
                advRequestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                mySRFilterString += String.valueOf(StringUtil.getMyAdvanceSearchString(advRequestParams, true).get(Constants.myResult));

                if (mySRFilterString.contains("salesreturncustomdata")) {
                    sqljoin += " LEFT JOIN salesreturn  ON salesreturn.company = je.company  and je.id  = salesreturn.inventoryje ";
                    customtablejoin += " LEFT JOIN salesreturncustomdata  ON salesreturncustomdata.salesreturnid=salesreturn.id ";

                }
                isInnerJoinAppend = false;
                innerJoinOnDetailTable = " LEFT JOIN srdetails on srdetails.company = je.company  and (jed.id = srdetails.costofgoodssoldjedetail or jed.id = srdetails.inventoryjedetail) ";
                if (mySRFilterString.contains("AccJEDetailCustomData")) {
                    mySRFilterString = mySRFilterString.replaceAll("AccJEDetailCustomData", "srdetailscustomdata");//  
                    sqljoin += innerJoinOnDetailTable;
                    customtablejoin += " LEFT JOIN srdetailscustomdata  ON srdetailscustomdata.srdetailsid=srdetails.id ";
                    isInnerJoinAppend = true;

                }
                if (mySRFilterString.contains("AccJEDetailsProductCustomData")) {
                    mySRFilterString = mySRFilterString.replaceAll("AccJEDetailsProductCustomData", "srdetailproductcustomdata");
                    customtablejoin += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join srdetailproductcustomdata on srdetails.id=srdetailproductcustomdata.srDetailID ";
                }
                StringUtil.insertParamAdvanceSearchString1(params, putSearchJson.toString());
                appendSearchString = StringUtil.combineCustomSearchStrings(appendSearchString, mySRFilterString, StringUtil.isNullOrEmpty(appendSearchString) ? "":" or ");
            }
            /*
             Get filter string for Stock Adjustment
             */
            if ((stockJson.length() > 0)) {
                JSONObject putSearchJson = new JSONObject();
                putSearchJson.put("root", stockJson);
                advRequestParams.clear();
                advRequestParams.put(Constants.Searchjson, putSearchJson);
                advRequestParams.put(Constants.appendCase, "AND");
                advRequestParams.put(Constants.moduleid, Constants.Inventory_Stock_Adjustment_ModuleId);
                advRequestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                myStockFilterString += String.valueOf(StringUtil.getMyAdvanceSearchString(advRequestParams, true).get(Constants.myResult));

                if (myStockFilterString.contains("in_stockadjustment_customdata") || myStockFilterString.contains("AccJEDetailCustomData") || myStockFilterString.contains("AccJEDetailsProductCustomData")) {
                    myStockFilterString = myStockFilterString.replaceAll("AccJEDetailCustomData", "in_stockadjustment_customdata");//  
                    myStockFilterString = myStockFilterString.replaceAll("AccJEDetailsProductCustomData", "in_stockadjustment_customdata");//  
                    sqljoin += " LEFT JOIN in_stockadjustment  ON je.id  = in_stockadjustment.inventoryje ";
                    customtablejoin += " LEFT JOIN in_stockadjustment_customdata  ON in_stockadjustment_customdata.stockadjustmentid=in_stockadjustment.id ";
                }
                StringUtil.insertParamAdvanceSearchString1(params, putSearchJson.toString());
                appendSearchString = StringUtil.combineCustomSearchStrings(appendSearchString, myStockFilterString, StringUtil.isNullOrEmpty(appendSearchString) ? "":" or ");
            }

            Searchjson = getJsornStringForSearch(Searchjson, accountid, companyid);
            request.put(Constants.Searchjson, Searchjson);
            request.put(Constants.appendCase, "and");
            request.put(Constants.moduleid, "100");
            request.put("filterConjuctionCriteria", filterConjuctionCriteria);

            mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
            if (mySearchFilterString.contains("AccJECustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "accjecustomdata");
                customtablejoin += " LEFT JOIN accjecustomdata  ON je.id=accjecustomdata.journalentryId ";
            }
            if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//  
                customtablejoin += " LEFT JOIN accjedetailcustomdata  ON jed.id=accjedetailcustomdata.jedetailId ";
            }
            if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "accjedetailproductcustomdata");//  
                customtablejoin += " LEFT JOIN accjedetailproductcustomdata ON jed.id=accjedetailproductcustomdata.jedetailId ";

            }
            StringUtil.insertParamAdvanceSearchString1(params, Searchjson);

            mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(appendSearchString, mySearchFilterString, " or ");

        } catch (JSONException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        } /*catch (UnsupportedEncodingException ex) {
         Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
         }*/
        advSearchAttributes.put("mySearchFilterString", mySearchFilterString);
        advSearchAttributes.put("params", params);
        advSearchAttributes.put("customtablejoin", customtablejoin);
        advSearchAttributes.put("sqljoin", sqljoin);
        return advSearchAttributes;
    }
    @Override
    public KwlReturnObject getClosingAccountBalance(String accountId, String companyId, int year) throws ServiceException {
        List list = new ArrayList();
        String query = "from ClosingAccountBalance where company.companyID=? and account.ID=? and yearLock.yearid=? and yearLock.isLock=?";
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(accountId);
        params.add(year);
        params.add(true);
        list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getClosingAccountBalanceYTD(String accountId, String companyId) throws ServiceException {
        List list = new ArrayList();
        String query = "from ClosingAccountBalance where company.companyID=? and account.ID=? and yearLock.isLock=? order by yearLock.yearid desc";
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(accountId);
        params.add(true);
        list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public String getJsornStringForSearch(String Searchjson, String accountid, String companyId) throws ServiceException {
        String returnStr = "";
        try {
            JSONArray jArray = new JSONArray();
            JSONObject jSONObject = new JSONObject();
            
            if(StringUtil.isNullOrEmpty(companyId)){ // companyId is passed only in case of default advance search balance sheet as of now, needs to be passed whenever companyid is provided
                Account account = (Account) get(Account.class, accountid);            
                if (account != null) {
                    companyId = account.getCompany().getCompanyID();
                } else {
                    companyId = accountid;
                }
            }
            JSONObject jobjSearch = new JSONObject(Searchjson);
            int count = jobjSearch.getJSONArray(Constants.root).length();
            for (int i = 0; i < count; i++) {
                KwlReturnObject result = null;
                KwlReturnObject resultdata = null;
                JSONObject jobj1 = jobjSearch.getJSONArray(Constants.root).getJSONObject(i);
                String[] arr = null;
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel));
                requestParams.put(Constants.filter_values, Arrays.asList(companyId, StringUtil.DecodeText(jobj1.optString("columnheader"))));
                result = getFieldParams(requestParams);
                List lst = result.getEntityList();
                Iterator ite = lst.iterator();
                boolean isfrmpmproduct ;
                while (ite.hasNext()) {
                    JSONObject jobj = new JSONObject();
                    FieldParams tmpcontyp = null;
                    tmpcontyp = (FieldParams) ite.next();
                    
                    isfrmpmproduct = false;
                    if (tmpcontyp.getModuleid() == Constants.Acc_Product_Master_ModuleId) {
                        /*
                         * Add this flag only for product and master custom
                         * field.As based on this flag we are taking join on
                         * accjedetailproductcustomdata table
                         */
                        isfrmpmproduct = jobj1.optBoolean("isfrmpmproduct",false);
                    }
                    jobj.put("column", tmpcontyp.getId());
                    jobj.put("refdbname", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                    jobj.put("xfield", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                    jobj.put("iscustomcolumn", jobj1.getString("iscustomcolumn"));
                    jobj.put("iscustomcolumndata", tmpcontyp.isIsForKnockOff()? (tmpcontyp.getCustomcolumn() == 1 ? "true" : "false") : jobj1.getString("iscustomcolumndata"));
                    jobj.put("isfrmpmproduct", isfrmpmproduct);
                    jobj.put("fieldtype", tmpcontyp.getFieldtype());
                    if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12) {
                        arr = jobj1.getString("searchText").split(",");
                        String Searchstr = "";
                        HashMap<String, Object> requestParamsdata = null;
                        for (String key : arr) {
                            FieldComboData fieldComboData1 = (FieldComboData) get(FieldComboData.class, key);
                            requestParamsdata = new HashMap<String, Object>();
                            requestParamsdata.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value"));
                            try {
                                requestParamsdata.put(Constants.filter_values, Arrays.asList(tmpcontyp.getId(), StringUtil.DecodeText(fieldComboData1.getValue())));
                            } catch (Exception e) {
                                requestParamsdata.put(Constants.filter_values, Arrays.asList(tmpcontyp.getId(), fieldComboData1.getValue()));
                            }

                            resultdata = getFieldParamsComboData(requestParamsdata);
                            List lstdata = resultdata.getEntityList();
                            Iterator itedata = lstdata.iterator();
                            if (itedata.hasNext()) {
                                FieldComboData fieldComboData = null;
                                fieldComboData = (FieldComboData) itedata.next();
                                Searchstr += fieldComboData.getId().toString() + ",";
                            }
                        }
                        jobj.put("searchText", Searchstr);
                        jobj.put("search", Searchstr);
                    } else {
                        jobj.put("searchText", jobj1.getString("searchText"));
                        jobj.put("search", jobj1.getString("searchText"));
                    }
                    jobj.put("columnheader", StringUtil.DecodeText(jobj1.optString("columnheader")));
                    try{
                        jobj.put("combosearch", StringUtil.DecodeText(jobj1.optString("combosearch")));
                    } catch(Exception e){
                        jobj.put("combosearch", jobj1.getString("combosearch"));
                    }
                    jobj.put("isinterval", jobj1.getString("isinterval"));
                    jobj.put("interval", jobj1.getString("interval"));
                    jobj.put("isbefore", jobj1.getString("isbefore"));
                    jobj.put("xtype", StringUtil.getXtypeVal(tmpcontyp.getFieldtype()));
                    jArray.put(jobj);
                    if (tmpcontyp.getCustomcolumn() == 1 && tmpcontyp.getCustomfield() == 0) {
                        JSONObject jobjOnlyForDimention = new JSONObject(jobj.toString());
                        jobjOnlyForDimention.remove("iscustomcolumndata");
                        jobjOnlyForDimention.put("iscustomcolumndata", "true");
                        jArray.put(jobjOnlyForDimention);
                    }
                }
            }
            jSONObject.put("root", jArray);
            returnStr = jSONObject.toString();

        } catch (JSONException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnStr;
    }
    
    public String advanceserachJsornEdit(String Searchjson,String companyid,int moduleid,boolean retainModuleId) throws ServiceException {
        String returnStr = "";
        try {
            JSONArray jArray = new JSONArray();
            JSONObject jSONObject = new JSONObject();
            String companyId = "";
            Account account=null;
            if(!StringUtil.isNullOrEmpty(companyid)){
                companyId = companyid;
            }
            if(!StringUtil.isNullOrEmpty(Searchjson)){
            Searchjson = StringUtil.DecodeText(Searchjson);
            JSONObject jobjSearch = new JSONObject(Searchjson);
            int count = jobjSearch.getJSONArray(Constants.root).length();
            for (int i = 0; i < count; i++) {
                KwlReturnObject result = null;
                KwlReturnObject resultdata = null;
                JSONObject jobj1 = jobjSearch.getJSONArray(Constants.root).getJSONObject(i);
                String[] arr = null;
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel,Constants.moduleid));
                requestParams.put(Constants.filter_values, Arrays.asList(companyId,StringUtil.DecodeText(jobj1.optString("columnheader")),moduleid));
                result = getFieldParams(requestParams);
                List lst = result.getEntityList();
                Iterator ite = lst.iterator();
                while (ite.hasNext()) {
                    JSONObject jobj = new JSONObject();
                    FieldParams tmpcontyp = null;
                    tmpcontyp = (FieldParams) ite.next();
                    jobj.put("column", tmpcontyp.getId());
                    jobj.put("refdbname", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                    jobj.put("xfield", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                    jobj.put("iscustomcolumn", jobj1.getString("iscustomcolumn"));
                    jobj.put("iscustomcolumndata", tmpcontyp.isIsForKnockOff() ? (tmpcontyp.getCustomcolumn() == 1 ? "true" : "false") : jobj1.getString("iscustomcolumndata"));
                    jobj.put("isfrmpmproduct", jobj1.getString("isfrmpmproduct"));
                    jobj.put("fieldtype", tmpcontyp.getFieldtype());
                    if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12) {
                        arr = jobj1.getString("searchText").split(",");
                        String Searchstr = "";
                        HashMap<String, Object> requestParamsdata = null;
                        for (String key : arr) {
                            FieldComboData fieldComboData1 = (FieldComboData) get(FieldComboData.class, key);
                            requestParamsdata = new HashMap<String, Object>();
                            requestParamsdata.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value"));
                            try {
                                requestParamsdata.put(Constants.filter_values, Arrays.asList(tmpcontyp.getId(), StringUtil.DecodeText(fieldComboData1.getValue())));
                            } catch (Exception e) {
                                requestParamsdata.put(Constants.filter_values, Arrays.asList(tmpcontyp.getId(), fieldComboData1.getValue()));
                            }

                            resultdata = getFieldParamsComboData(requestParamsdata);
                            List lstdata = resultdata.getEntityList();
                            Iterator itedata = lstdata.iterator();
                            if (itedata.hasNext()) {
                                FieldComboData fieldComboData = null;
                                fieldComboData = (FieldComboData) itedata.next();
                                Searchstr += fieldComboData.getId().toString() + ",";
                            }
                        }
                        jobj.put("searchText", Searchstr);
                        jobj.put("search", Searchstr);
                    } else {
                        jobj.put("searchText", jobj1.getString("searchText"));
                        jobj.put("search", jobj1.getString("searchText"));
                    }
                    jobj.put("columnheader", StringUtil.DecodeText(jobj1.optString("columnheader")));
                    jobj.put("combosearch", jobj1.getString("combosearch"));
                    jobj.put("isinterval", jobj1.optString("isinterval","false"));
                    jobj.put("interval", jobj1.optString("interval","false"));
                    jobj.put("isbefore", jobj1.optString("isbefore","false"));
                    jobj.put("xtype", StringUtil.getXtypeVal(tmpcontyp.getFieldtype()));
                        jobj.put("isdefaultfield", jobj1.getString("isdefaultfield"));
                    if (retainModuleId) {
                        jobj.put("moduleid", jobj1.optString("moduleid"));
                    } else {
                        jobj.put("moduleid", tmpcontyp.getModuleid());
                    }
                    
                    jArray.put(jobj);
                    if (tmpcontyp.getCustomcolumn() == 1 && tmpcontyp.getCustomfield() == 0) {
                        JSONObject jobjOnlyForDimention = new JSONObject(jobj.toString());
                        jobjOnlyForDimention.remove("iscustomcolumndata");
                        jobjOnlyForDimention.put("iscustomcolumndata", "true");
                        jArray.put(jobjOnlyForDimention);
                    }
                }
            }
            jSONObject.put("root", jArray);
            returnStr = jSONObject.toString();
            }
        } catch (JSONException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }/* catch (UnsupportedEncodingException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return returnStr;
    }
    
    public String advSerachJsonForMultiModules(String Searchjson,String companyid,int moduleid,String modulids) throws ServiceException {
        String returnStr = "";
        try {
            JSONArray jArray = new JSONArray();
            JSONObject jSONObject = new JSONObject();
            String companyId = "";
            if(!StringUtil.isNullOrEmpty(companyid)){
                companyId = companyid;
            }
            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                JSONObject jobjSearch = new JSONObject(Searchjson);
                int count = jobjSearch.getJSONArray(Constants.root).length();
                for (int i = 0; i < count; i++) {
                    KwlReturnObject result = null;
                    KwlReturnObject resultdata = null;
                    JSONObject jobj1 = jobjSearch.getJSONArray(Constants.root).getJSONObject(i);
                    String[] arr = null;
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel, Constants.moduleid));
                    requestParams.put(Constants.filter_values, Arrays.asList(companyId, StringUtil.DecodeText(jobj1.optString("columnheader")), modulids));
                    result = getFieldParameters(requestParams);
                    List<FieldParams> lst = result.getEntityList();
                    boolean ismoduleidexists = false;
                    for (FieldParams fp : lst) {
                        if (fp.getModuleid() == moduleid) {
                            ismoduleidexists = true;
                        }
                    }
                    for (FieldParams tmpcontyp : lst) {
                        JSONObject jobj = new JSONObject();
                        if (lst.size() > 1 && tmpcontyp.getModuleid() != moduleid && ismoduleidexists) {
                            continue;
                        }
                        jobj.put("column", tmpcontyp.getId());
                        jobj.put("refdbname", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                        jobj.put("xfield", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                        jobj.put("iscustomcolumn", jobj1.getString("iscustomcolumn"));
                        jobj.put("iscustomcolumndata", tmpcontyp.isIsForKnockOff()? (tmpcontyp.getCustomcolumn() == 1 ? "true" : "false") : jobj1.getString("iscustomcolumndata"));
                        jobj.put("isfrmpmproduct", jobj1.getString("isfrmpmproduct"));
                        jobj.put("fieldtype", tmpcontyp.getFieldtype());
                        if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12) {
                            arr = jobj1.getString("searchText").split(",");
                            String Searchstr = "";
                            HashMap<String, Object> requestParamsdata = null;
                            for (String key : arr) {
                                FieldComboData fieldComboData1 = (FieldComboData) get(FieldComboData.class, key);
                                requestParamsdata = new HashMap<String, Object>();
                                requestParamsdata.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value"));
                                requestParamsdata.put(Constants.filter_values, Arrays.asList(tmpcontyp.getId(), StringUtil.DecodeText(fieldComboData1.getValue())));

                                resultdata = getFieldParamsComboData(requestParamsdata);
                                List lstdata = resultdata.getEntityList();
                                Iterator itedata = lstdata.iterator();
                                if (itedata.hasNext()) {
                                    FieldComboData fieldComboData = null;
                                    fieldComboData = (FieldComboData) itedata.next();
                                    Searchstr += fieldComboData.getId().toString() + ",";
                                }
                            }
                            jobj.put("searchText", Searchstr);
                            jobj.put("search", Searchstr);
                        } else {
                            jobj.put("searchText", jobj1.getString("searchText"));
                            jobj.put("search", jobj1.getString("searchText"));
                        }
                        jobj.put("columnheader",StringUtil.DecodeText(jobj1.optString("columnheader")));
                        jobj.put("combosearch", jobj1.getString("combosearch"));
                        jobj.put("isinterval", jobj1.optString("isinterval", "false"));
                        jobj.put("interval", jobj1.optString("interval", "false"));
                        jobj.put("isbefore", jobj1.optString("isbefore", "false"));
                        jobj.put("xtype", StringUtil.getXtypeVal(tmpcontyp.getFieldtype()));
                        jobj.put("isdefaultfield", jobj1.getString("isdefaultfield"));
                        jobj.put("moduleid", moduleid);
                        jArray.put(jobj);
                        if (tmpcontyp.getCustomcolumn() == 1 && tmpcontyp.getCustomfield() == 0) {
                            JSONObject jobjOnlyForDimention = new JSONObject(jobj.toString());
                            jobjOnlyForDimention.remove("iscustomcolumndata");
                            jobjOnlyForDimention.put("iscustomcolumndata", "true");
                            jArray.put(jobjOnlyForDimention);
                        }
                    }
                }
                jSONObject.put("root", jArray);
                returnStr = jSONObject.toString();
            }
        } catch (JSONException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        } /*catch (UnsupportedEncodingException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return returnStr;
    }

    public KwlReturnObject getFieldParams(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "from FieldParams ";
            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
                int ind = hql.indexOf("(");

                if (ind > -1) {
                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
                    value.remove(index);
                }
            }

            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }

            if (requestParams.containsKey("customfield") && (Integer) requestParams.get("customfield") != null) {
                hql += " and customfield = 1";
            }
            if (requestParams.containsKey("relatedmoduleid")) {
                hql += " and relatedmoduleid like '%" + requestParams.get("relatedmoduleid") + "%'";
            }
             if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
            list = executeQuery( hql, value.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getFieldParameters(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "from FieldParams ";
            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
                hql=hql.substring(0, hql.lastIndexOf("="));
                hql+="IN("+value.get(2).toString()+")";
                value.remove(2);
            }

            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }

            if (requestParams.containsKey("customfield") && (Integer) requestParams.get("customfield") != null) {
                hql += " and customfield = 1";
            }
            if (requestParams.containsKey("relatedmoduleid")) {
                hql += " and relatedmoduleid like '%" + requestParams.get("relatedmoduleid") + "%'";
            }
             if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
            list = executeQuery( hql, value.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getFieldParamsComboData(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "from FieldComboData ";
            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
                int ind = hql.indexOf("(");

                if (ind > -1) {
                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
                    value.remove(index);
                }
            }

            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }

            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
            if (requestParams.containsKey("customfield") && (Integer) requestParams.get("customfield") != null) {
                hql += " and customfield = 1";
            }
            if (requestParams.containsKey("relatedmoduleid")) {
                hql += " and relatedmoduleid like '%" + requestParams.get("relatedmoduleid") + "%'";
            }
            list = executeQuery( hql, value.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getAccountBalance_optimized(String accountid, Date startDate, Date endDate, String costCenterID) throws ServiceException {
        List list = new ArrayList();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String query = "select sum(amount) from jedetail_optimized "
                + "where account=? and date_format(entrydate, '%Y-%m-%d')>=? and date_format(entrydate, '%Y-%m-%d')<? ";
        ArrayList params = new ArrayList();
        params.add(accountid);
        if (startDate == null) {
            startDate = new Date(0);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        params.add(sdf.format(startDate));
        params.add(sdf.format(endDate));

        if (!StringUtil.isNullOrEmpty(costCenterID)) {
            query += " and costcenter=?";
            params.add(costCenterID);
        }
        list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getAccountJEs(String accountid, Date startDate, Date endDate) throws ServiceException {
        List list = new ArrayList();
        String condition = "";
        ArrayList params = new ArrayList();
        params.add(accountid);
        if (startDate != null && endDate != null) {
            condition = " and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<? ";
            if (startDate == null) {
                startDate = new Date(0);
            }
            if (endDate == null) {
                endDate = new Date();
            }
            params.add(startDate);
            params.add(endDate);
        }
        String query = "select (case when debit=true then amount else -amount end) ,jed , jed.journalEntry.currency.currencyID from JournalEntryDetail jed "
                + " where account.ID=? and jed.journalEntry.deleted=false and jed.journalEntry.optimizedflag=false and jed.journalEntry.pendingapproval = 0 and jed.journalEntry.draft=false and jed.journalEntry.istemplate != 2 ";
//        if(!StringUtil.isNullOrEmpty(costCenterID)){
//            query += " and jed.journalEntry.costcenter.ID=?";
//            params.add(costCenterID);
//        }
        list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    //Used to get all JEs of selected currency with no external rate set and whose values are already added in jedetail_optimzed table.
    public KwlReturnObject getCurrencyJEs(String currencyid, Date startDate, Date endDate) throws ServiceException {
        List list = new ArrayList();
        String condition = "";
        ArrayList params = new ArrayList();
        params.add(currencyid);
        if (startDate != null && endDate != null) {
            condition = " and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<? ";
            if (startDate == null) {
                startDate = new Date(0);
            }
            if (endDate == null) {
                endDate = new Date();
            }
            params.add(startDate);
            params.add(endDate);
        }
        String query = "select (case when debit=true then amount else -amount end) ,jed , jed.journalEntry.currency.currencyID from JournalEntryDetail jed "
                + " where jed.journalEntry.currency.currencyID=? and jed.journalEntry.deleted=false and jed.journalEntry.optimizedflag=true and jed.journalEntry.pendingapproval = 0 and jed.journalEntry.draft=false and jed.journalEntry.istemplate != 2 and jed.journalEntry.externalCurrencyRate != 0.0 ";
//        if(!StringUtil.isNullOrEmpty(costCenterID)){
//            query += " and jed.journalEntry.costcenter.ID=?";
//            params.add(costCenterID);
//        }
        list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getOldCurrencyRateAndDate(String companyid, Date transactiondate, String erid) throws ServiceException {
        List list = new ArrayList();
        ExchangeRateDetails erd = null;
        Date minDate = null;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
////            params.add(AuthHandler.getCurrencyID(request));
//            params.add(request.get("gcurrencyid"));
//            params.add(currencyid);
//            if (erid == null) {
//                String erIDQuery = "select ID from ExchangeRate where fromCurrency.currencyID=? and toCurrency.currencyID=? ";
//                List erIDList = executeQuery( erIDQuery, params.toArray());
//                Iterator erIDitr = erIDList.iterator();
//                erid = (String) erIDitr.next();
//            }
            params = new ArrayList();
            params.add(companyid);
            params.add(erid);
            if (transactiondate != null) {
                params.add(transactiondate);
                condition += " and applyDate >= ?  ";
            }
            String applyDateQuery = "select min(erd.applyDate) from ExchangeRateDetails erd where erd.company.companyID=? and  erd.exchangeratelink.ID = ? " + condition;
            List applyDateList = executeQuery( applyDateQuery, params.toArray());
            Iterator itr = applyDateList.iterator();

            minDate = (Date) itr.next();
            params = new ArrayList();
            params.add(minDate);
            params.add(erid);
//            params.add(AuthHandler.getCompanyid(request));
            params.add(companyid);

            String erdIDQuery = "from ExchangeRateDetails erd where erd.applyDate=? and erd.exchangeratelink.ID=? and erd.company.companyID=?";
            List erdIDList = executeQuery( erdIDQuery, params.toArray());
            Iterator erdIDItr = erdIDList.iterator();
            erd = (ExchangeRateDetails) erdIDItr.next();
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getOldCurrencyRateAndDate : " + ex.getMessage(), ex);
        } finally {
            list.add(erd);
            list.add(minDate);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    public boolean saveAccountJEs_optimized(String jeid) throws ServiceException {
        boolean successflag = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        JournalEntry je = (JournalEntry) get(JournalEntry.class, jeid);
        String gcurrencyid = je.getCompany().getCurrency().getCurrencyID();
        String companyid = je.getCompany().getCompanyID();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", companyid);
        requestParams.put("gcurrencyid", gcurrencyid);
        try {
            if (!je.isOptimizedflag() && !je.isDeleted() && je.getPendingapproval() == 0 && !je.isDraft()) {
                Set<JournalEntryDetail> jedetail = (Set<JournalEntryDetail>) je.getDetails();
                Iterator itr = jedetail.iterator();
                while (itr.hasNext()) {
                    JournalEntryDetail jed = (JournalEntryDetail) itr.next();

                    double amount = jed.isDebit() ? jed.getAmount() : -jed.getAmount();
                    String accountid = jed.getAccount().getID();
                    String entrydate = sdf.format(je.getEntryDate());
                    String costCenterID = je.getCostcenter() != null ? je.getCostcenter().getID() : "";

                    String fromcurrencyid = (je.getCurrency() == null ? gcurrencyid : je.getCurrency().getCurrencyID());
                    KwlReturnObject crresult = getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                    amount = (Double) crresult.getEntityList().get(0);
                    if (amount != 0) {
                        saveAccountJEs_optimized(accountid, companyid, entrydate, costCenterID, amount);
                    }
                }
                successflag = true;
                je.setOptimizedflag(true);
                saveOrUpdate(je);
            }
        } catch (Exception e) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("accJournalEntryImpl.saveAccountJEs_optimized : " + e.getMessage(), e);
        }
        return successflag;
    }

    public boolean deleteOnEditAccountJEs_optimized(String jeid) throws ServiceException {
        boolean successflag = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        JournalEntry je = (JournalEntry) get(JournalEntry.class, jeid);
        String gcurrencyid = je.getCompany().getCurrency().getCurrencyID();
        String companyid = je.getCompany().getCompanyID();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", companyid);
        requestParams.put("gcurrencyid", gcurrencyid);
        try {
            if (je.isOptimizedflag() && je.getPendingapproval() == 0 && !je.isDraft()) {
                Set<JournalEntryDetail> jedetail = (Set<JournalEntryDetail>) je.getDetails();
                Iterator itr = jedetail.iterator();
                while (itr.hasNext()) {
                    JournalEntryDetail jed = (JournalEntryDetail) itr.next();

                    double amount = jed.isDebit() ? -jed.getAmount() : jed.getAmount();
                    String accountid = jed.getAccount().getID();
                    String entrydate = sdf.format(je.getEntryDate());
                    String costCenterID = je.getCostcenter() != null ? je.getCostcenter().getID() : "";

                    String fromcurrencyid = (je.getCurrency() == null ? gcurrencyid : je.getCurrency().getCurrencyID());
                    KwlReturnObject crresult = getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                    amount = (Double) crresult.getEntityList().get(0);
                    if (amount != 0) {
                        saveAccountJEs_optimized(accountid, companyid, entrydate, costCenterID, amount);
                    }
                }
                successflag = true;
                je.setOptimizedflag(false);
                saveOrUpdate(je);
            }
        } catch (Exception e) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("accJournalEntryImpl.deleteOnEditAccountJEs_optimized : " + e.getMessage(), e);
        }
        return successflag;
    }

    public boolean deleteAccountJEs_optimized(String jeid) throws ServiceException {
        boolean successflag = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        JournalEntry je = (JournalEntry) get(JournalEntry.class, jeid);
        String gcurrencyid = je.getCompany().getCurrency().getCurrencyID();
        String companyid = je.getCompany().getCompanyID();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", companyid);
        requestParams.put("gcurrencyid", gcurrencyid);
        try {
//            if(je.isOptimizedflag() && je.isDeleted() && je.getPendingapproval() == 0) {
            if (je.isOptimizedflag() && je.getPendingapproval() == 0 && !je.isDraft()) {
                Set<JournalEntryDetail> jedetail = (Set<JournalEntryDetail>) je.getDetails();
                Iterator itr = jedetail.iterator();
                while (itr.hasNext()) {
                    JournalEntryDetail jed = (JournalEntryDetail) itr.next();

                    double amount = jed.isDebit() ? -jed.getAmount() : jed.getAmount();
                    String accountid = jed.getAccount().getID();
                    //                String companyid = companyid;
                    String entrydate = sdf.format(je.getEntryDate());
                    String costCenterID = je.getCostcenter() != null ? je.getCostcenter().getID() : "";

                    String fromcurrencyid = (je.getCurrency() == null ? gcurrencyid : je.getCurrency().getCurrencyID());
                    KwlReturnObject crresult = getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                    amount = (Double) crresult.getEntityList().get(0);
                    if (amount != 0) {
                        saveAccountJEs_optimized(accountid, companyid, entrydate, costCenterID, amount);
                    }
                }
                successflag = true;
                je.setOptimizedflag(false);
                je.setDeleted(true);
                saveOrUpdate(je);
            }
        } catch (Exception e) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("accJournalEntryImpl.deleteAccountJEs_optimized : " + e.getMessage(), e);
        }
        return successflag;
    }

    public boolean deleteAccountJEs_optimized(String jeid, boolean callfromJEdelete) throws ServiceException {
        boolean successflag = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        JournalEntry je = (JournalEntry) get(JournalEntry.class, jeid);
        String gcurrencyid = je.getCompany().getCurrency().getCurrencyID();
        String companyid = je.getCompany().getCompanyID();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", companyid);
        requestParams.put("gcurrencyid", gcurrencyid);
        try {
//            if(je.isOptimizedflag() && je.isDeleted() && je.getPendingapproval() == 0) {
            if (je.isOptimizedflag() && je.getPendingapproval() == 0 && !je.isDraft()) {
                Set<JournalEntryDetail> jedetail = (Set<JournalEntryDetail>) je.getDetails();
                Iterator itr = jedetail.iterator();
                while (itr.hasNext()) {
                    JournalEntryDetail jed = (JournalEntryDetail) itr.next();

                    double amount = jed.isDebit() ? -jed.getAmount() : jed.getAmount();
                    String accountid = jed.getAccount().getID();
                    //                String companyid = companyid;
                    String entrydate = sdf.format(je.getEntryDate());
                    String costCenterID = je.getCostcenter() != null ? je.getCostcenter().getID() : "";

                    String fromcurrencyid = (je.getCurrency() == null ? gcurrencyid : je.getCurrency().getCurrencyID());
                    KwlReturnObject crresult = getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                    amount = (Double) crresult.getEntityList().get(0);
                    if (amount != 0) {
                        saveAccountJEs_optimized(accountid, companyid, entrydate, costCenterID, amount);
                    }
                }
                successflag = true;
//                je.setOptimizedflag(false);
//                je.setDeleted(true);
//                saveOrUpdate(je);
            }
        } catch (Exception e) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("accJournalEntryImpl.deleteAccountJEs_optimized : " + e.getMessage(), e);
        }
        return successflag;
    }

    public KwlReturnObject saveAccountJEs_optimized(String accountid, String companyid, String entryDate, String costCenterID, double amount) throws ServiceException {
        List list = new ArrayList();
        synchronized (this) {
            String condition = " where account = ? and company = ? and date_format(entrydate, '%Y-%m-%d') = ? ";
            ArrayList params = new ArrayList();
            params.add(accountid);
            params.add(companyid);
            params.add(entryDate);
            boolean costcenterflag = false;
            if (!StringUtil.isNullOrEmpty(costCenterID)) {
                costcenterflag = true;
                condition += " and costcenter = ? ";
                params.add(costCenterID);
            }
            String query = " select id from jedetail_optimized " + condition;
            list = executeSQLQuery( query, params.toArray());
            if (list.size() > 0) {
                String id = (String) list.get(0);
                params = new ArrayList();
                params.add(amount);
                params.add(id);
                query = " Update jedetail_optimized set amount = amount + ? where id = ? ";
                int cnt = executeSQLUpdate( query, params.toArray());
                list.add(id);
            } else {
                String id = UUID.randomUUID().toString();
                params = new ArrayList();
                params.add(id);
                params.add(amount);
                params.add(accountid);
                params.add(companyid);
                params.add(entryDate);
                if (costcenterflag) {
                    query = " insert into jedetail_optimized (id, amount, account, company, entrydate, costcenter) values (?, ?, ?, ?, ?, ?) ";
                    params.add(costCenterID);
                } else {
                    query = " insert into jedetail_optimized (id, amount, account, company, entrydate) values (?, ?, ?, ?, ?) ";
                }
                int cnt = executeSQLUpdate( query, params.toArray());
                list.add(id);
            }
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public boolean setCompany_optimizedflag(String companyid) throws ServiceException {
        boolean successflag = false;
        Company company = (Company) get(Company.class, companyid);
        try {
            company.setOptimizedflag(true);
            successflag = true;
            saveOrUpdate(company);
        } catch (Exception e) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("accJournalEntryImpl.setCompany_optimizedflag : " + e.getMessage(), e);
        }
        return successflag;
    }

    public boolean setJEs_optimizedflag(String jeid) throws ServiceException {
        boolean successflag = false;
        JournalEntry je = (JournalEntry) get(JournalEntry.class, jeid);
        try {
            je.setOptimizedflag(true);
            successflag = true;
            saveOrUpdate(je);
        } catch (Exception e) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("accJournalEntryImpl.setCompany_optimizedflag : " + e.getMessage(), e);
        }
        return successflag;
    }

    public KwlReturnObject getExcDetailID(Map request, String currencyid, Date transactiondate, String erid) throws ServiceException {
        List list = new ArrayList();
        ExchangeRateDetails erd = null;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
//            params.add(AuthHandler.getCurrencyID(request));
            params.add(request.get("gcurrencyid"));
            params.add(currencyid);
            if (erid == null) {
                String erIDQuery = "select ID from ExchangeRate where fromCurrency.currencyID=? and toCurrency.currencyID=? ";
                List erIDList = executeQuery( erIDQuery, params.toArray());
                Iterator erIDitr = erIDList.iterator();
                erid = (String) erIDitr.next();
            }
            params = new ArrayList();
//            params.add(AuthHandler.getCompanyid(request));
            params.add(request.get("companyid"));
            params.add(erid);
            if (transactiondate != null) {
                params.add(transactiondate);
                condition += " and applyDate <= ?  ";
            }
            String applyDateQuery = "select max(erd.applyDate) from ExchangeRateDetails erd where erd.company.companyID=? and  erd.exchangeratelink.ID = ? " + condition;
            List applyDateList = executeQuery( applyDateQuery, params.toArray());
            Iterator itr = applyDateList.iterator();

            Date maxDate = (Date) itr.next();
            params = new ArrayList();
            params.add(maxDate);
            params.add(erid);
//            params.add(AuthHandler.getCompanyid(request));
            params.add(request.get("companyid"));

            String erdIDQuery = "from ExchangeRateDetails erd where erd.applyDate=? and erd.exchangeratelink.ID=? and erd.company.companyID=?";
            List erdIDList = executeQuery( erdIDQuery, params.toArray());
            Iterator erdIDItr = erdIDList.iterator();
            erd = (ExchangeRateDetails) erdIDItr.next();
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getExcDetailID : " + ex.getMessage(), ex);
        } finally {
            list.add(erd);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    public KwlReturnObject getCurrencyToBaseAmount(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        try {
            if (Amount != 0) {
                if (rate == 0) {
                    KwlReturnObject result = getExcDetailID(request, currencyid, transactiondate, null);
                    List li = result.getEntityList();
                    if (!li.isEmpty()) {
                        Iterator itr = li.iterator();
                        ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                        rate = erd.getExchangeRate();
                    }
                }
                Amount = Amount / rate;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getCurrencyToBaseAmount : " + ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    public KwlReturnObject getAccountBalanceMerged(String accountid, Date startDate, Date endDate, String costCenterID, Boolean eliminateFlag, String filterConjuctionCriteria, String Searchjson) throws ServiceException {
        List list = new ArrayList();
        String mySearchFilterString = "";
        
//        String query = "select (case when debit=true then amount else -amount end) ,jed , jed.journalEntry.currency.currencyID, jed.journalEntry from JournalEntryDetail jed where account.ID=? and jed.journalEntry.deleted=false and jed.journalEntry.pendingapproval = 0 and jed.journalEntry.draft=false and jed.journalEntry.approvestatuslevel=11 and jed.journalEntry.istemplate != 2 and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<=? ";
        String query = "select (case when debit=true then amount else -amount end) ,jed from JournalEntryDetail jed where account.ID=? and jed.journalEntry.deleted=false and jed.journalEntry.pendingapproval = 0 and jed.journalEntry.draft=false and jed.journalEntry.approvestatuslevel=11 and jed.journalEntry.istemplate != 2 and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<=? ";
        if (eliminateFlag) {//eliminate JE in which eliminate flag is set
            query += " and jed.journalEntry.eliminateflag = false ";
        }

        ArrayList params = new ArrayList();
        params.add(accountid);
        if (startDate == null) {
            startDate = new Date(0);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        params.add(startDate);
        params.add(endDate);

        if (!StringUtil.isNullOrEmpty(costCenterID)) {
            query += " and jed.journalEntry.costcenter.ID=?";
            params.add(costCenterID);
        }

        if (!StringUtil.isNullOrEmpty(Searchjson)) {
//            boolean isAdvonGlobalCust = isAdvanceSearchOnGlobalDimension(Searchjson);
            if (isAdvanceSearchOnGlobalDimension(Searchjson)) {
                query += " and jed.isSeparated = false ";
            }
            HashMap<String, Object> request = new HashMap<String, Object>();
            Searchjson = getJsornStringForSearch(Searchjson, accountid,null);
            request.put(Constants.Searchjson, Searchjson);
            request.put(Constants.appendCase, "and");
            request.put(Constants.moduleid, "100");
            request.put("filterConjuctionCriteria", filterConjuctionCriteria);
            try {
                mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "jed.journalEntry.accBillInvCustomData");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//          
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");//             
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        } 
        else {
            query += " and jed.isSeparated = false ";   //SDP-10502
        }
        list = executeQuery( query + mySearchFilterString, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getAccountBalanceMergedAmount(String accountid, Date startDate, Date endDate, String costCenterID, Boolean eliminateFlag, String filterConjuctionCriteria, String Searchjson, Boolean isDebit, Boolean isKnockOffAdvancedSearch,String companyid) throws ServiceException {
        List list = new ArrayList();
        String mySearchFilterString = "", customtablejoin = "", sqljoin = "", sqlcondition = "";
        CompanyAccountPreferences preferences = null;
        Map<String, Object> advSearchAttributes = null;
        if (!StringUtil.isNullOrEmpty(companyid)) {
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        }
        
//        String query = "select sum(case when debit=true then COALESCE(amountinbase,0) else COALESCE(-amountinbase,0) end) from JournalEntryDetail jed where account.ID=? and jed.journalEntry.deleted=false and jed.journalEntry.pendingapproval = 0 and jed.journalEntry.draft=false and jed.journalEntry.approvestatuslevel=11 and jed.journalEntry.istemplate != 2 and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<=? ";
        String sqlquery = "select sum(case when debit='T' then amountinbase else -amountinbase end) from jedetail jed INNER JOIN journalentry je ON jed.journalentry = je.id ";
        if (eliminateFlag) {//eliminate JE in which eliminate flag is set
//            query += " and jed.journalEntry.eliminateflag = false ";
            sqlcondition += " and je.eliminateflag = 'F'";
        }

        ArrayList params = new ArrayList();
        params.add(accountid);
        if (startDate == null) {
            startDate = new Date(0);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        params.add(startDate);
        params.add(endDate);

        if (!StringUtil.isNullOrEmpty(costCenterID)) {
//            query += " and jed.journalEntry.costcenter.ID=?";
            sqljoin += " LEFT JOIN costcenter ON costcenter.id=je.costcenter ";
            sqlcondition += " and costcenter.id=? ";
            params.add(costCenterID);
        }
        if (isDebit != null) {
//            query += " and jed.debit = ?";
            sqlcondition +=" and jed.debit = ? ";
            params.add(isDebit);
        }
        boolean advnacesearch = false;
        if (!StringUtil.isNullOrEmpty(Searchjson) && !Searchjson.equals("[]") && !Searchjson.equals("[{}]")) {
            if (isAdvanceSearchOnGlobalDimension(Searchjson)) {
//                query += " and jed.isSeparated = false ";
                sqlcondition += " and jed.isseparated = 'F' ";
            }else if(filterConjuctionCriteria.trim().equalsIgnoreCase(Constants.and.trim())){
                isKnockOffAdvancedSearch = true;
            }
        }
        if ((!StringUtil.isNullOrEmpty(Searchjson)) && preferences != null && preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
            advnacesearch = true;
            if (advSearchAttributes == null || advSearchAttributes.isEmpty()) {
                advSearchAttributes = getPerpetualInvAdvanceSearchAttributes(mySearchFilterString, Searchjson, accountid, companyid, params, filterConjuctionCriteria, "", "", sqljoin);
                params = (ArrayList) advSearchAttributes.get("params");
            } else {
                params.addAll((ArrayList) advSearchAttributes.get("params"));
            }
            mySearchFilterString = (String) advSearchAttributes.get("mySearchFilterString");
            customtablejoin = (String) advSearchAttributes.get("customtablejoin");
            sqljoin += (String) advSearchAttributes.get("sqljoin");

        } else if (!StringUtil.isNullOrEmpty(Searchjson) && isKnockOffAdvancedSearch) {
            try {
                advnacesearch = true;
                Searchjson = getJsornStringForSearch(Searchjson, accountid,null);
                HashMap<String, Object> request = new HashMap<>();
                request.put(Constants.Searchjson, Searchjson);
                request.put(Constants.appendCase, "and");
                request.put(Constants.moduleid, "100");
                request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                if (mySearchFilterString.contains("AccJECustomData")) {
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "accjecustomdata");
                    customtablejoin += " LEFT JOIN accjecustomdata  ON je.id=accjecustomdata.journalentryId ";
                }
                if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//  
                    customtablejoin += " LEFT JOIN accjedetailcustomdata  ON jed.id=accjedetailcustomdata.jedetailId ";
                }
                if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "accjedetailproductcustomdata");//  
                    customtablejoin += " LEFT JOIN accjedetailproductcustomdata ON jed.id=accjedetailproductcustomdata.jedetailId ";
                }
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
            } catch (Exception ex) {
                throw ServiceException.FAILURE("accJournalEntryImpl.getAccountBalanceMergedAmount" + ex.getMessage(), ex);
            }
        } else if (!StringUtil.isNullOrEmpty(Searchjson)) {
            HashMap<String, Object> request = new HashMap<String, Object>();
            Searchjson = getJsornStringForSearch(Searchjson, accountid,null);
            request.put(Constants.Searchjson, Searchjson);
            request.put(Constants.appendCase, "and");
            request.put(Constants.moduleid, "100");
            request.put("filterConjuctionCriteria", filterConjuctionCriteria);
            try {
                mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "jed.journalEntry.accBillInvCustomData");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//          
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");//             
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
//            query += " and jed.isSeparated = false ";
            sqlcondition += " and jed.isseparated = 'F' ";
        }
//        if(advnacesearch){
            sqlquery = sqlquery + sqljoin + customtablejoin + " where jed.account=? and je.deleteflag='F' and je.pendingapproval = 0 and je.isdraft=0 and je.istemplate != 2 and je.approvestatuslevel=11 and je.entrydate>=? and je.entrydate<=? " + sqlcondition + mySearchFilterString;
            list = executeSQLQuery(sqlquery, params.toArray());
//        }else{
//            list = executeQuery( query + mySearchFilterString, params.toArray());
//        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getLedger(String companyid, String accountid, Date startDate, Date endDate) throws ServiceException {
        List list = new ArrayList();
        String query = "select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and je.pendingapproval = 0 and je.draft=false and je.istemplate != 2 and je.entryDate >= ? and je.entryDate <= ? and ac.company.companyID=? and je.deleted=false order by je.entryDate, je.entryNumber";
        Object[] params = {
            accountid,
            AccountingManager.setFilterTime(startDate, true),
            AccountingManager.setFilterTime(endDate, false),
            companyid
        };
        list = executeQuery( query, params);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getLedgerMerged(String accountid, Date startDate, Date endDate, boolean eliminateflag, boolean generalLedgerFlag, String selectedCurrencyIds, String filterConjuctionCriteria, String Searchjson,int viewFlag, int accountTransactionType,CompanyAccountPreferences pref) throws ServiceException {
        String mySearchFilterString = "",customDataJoin = "";
        ArrayList params = new ArrayList();
        boolean advnacesearch = false;
        Map<String, Object> advSearchAttributes = null;
        String sqljoin = "";
        String query = "select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac ";
        String sqlquery = "select je.id, jed.id as 'jedid' from journalentry je inner join jedetail jed on jed.journalEntry = je.id ";
        String condition = " and je.approvestatuslevel = 11 and je.istemplate != 2 and je.entryDate >= ? and je.entryDate <= ? and je.deleted=false and je.draft=false ";
        String sqlcondition = " and je.approvestatuslevel = 11 and je.istemplate != 2 and je.entrydate >= ? and je.entrydate <= ? and je.deleteflag='F' and je.isdraft='F' ";
        if (eliminateflag) {
            condition += " and je.eliminateflag = false ";
            sqlcondition += " and je.eliminateflag = 'F' ";
        }
        /*
         * If viewFlag=0 then Display All Records in Bank Book records
         * If viewFlag=1 then Display only Make Payment in Bank Book records
         * If viewFlag=2 then Display only Receive Payment in Bank Book records
         */
        if(viewFlag==Constants.BANKBOOK_VIEW_MAKE_PAYMENT || accountTransactionType==Constants.Acc_Make_Payment_ModuleId){
//                condition += " and je.isDishonouredCheque='F' and je.transactionModuleid =" + Constants.Acc_Make_Payment_ModuleId;
//                sqlcondition += " and je.isdishonouredcheque='F' and je.transactionModuleid =" + Constants.Acc_Make_Payment_ModuleId;
                condition += " and je.transactionModuleid in (" + Constants.Acc_Make_Payment_ModuleId + "," + Constants.Acc_Dishonoured_Make_Payment_ModuleId + ")";
                sqlcondition += " and je.transactionModuleid in (" + Constants.Acc_Make_Payment_ModuleId + "," + Constants.Acc_Dishonoured_Make_Payment_ModuleId + ")";
        }else if(viewFlag==Constants.BANKBOOK_VIEW_RECEIVE_PAYMENT || accountTransactionType==Constants.Acc_Receive_Payment_ModuleId){
//                condition += " and je.isDishonouredCheque='F' and je.transactionModuleid =" + Constants.Acc_Receive_Payment_ModuleId;
//                sqlcondition += " and je.isdishonouredcheque='F' and je.transactionModuleid =" + Constants.Acc_Receive_Payment_ModuleId;
                condition += " and je.transactionModuleid in (" + Constants.Acc_Receive_Payment_ModuleId + "," + Constants.Acc_Dishonoured_Receive_Payment_ModuleId + ")";
                sqlcondition += " and je.transactionModuleid in (" + Constants.Acc_Receive_Payment_ModuleId + "," + Constants.Acc_Dishonoured_Receive_Payment_ModuleId + ")";
        }
//        else if(viewFlag==0){
//                condition += " and je.isDishonouredCheque='F' ";
//                sqlcondition += " and je.isdishonouredcheque='F' ";
//        }
        if (generalLedgerFlag && (!StringUtil.isNullOrEmpty(selectedCurrencyIds))) {
            selectedCurrencyIds = AccountingManager.getFilterInString(selectedCurrencyIds);
            condition += " and je.currency.currencyID in " + selectedCurrencyIds + " ";
            sqlcondition += " and je.currency in " + selectedCurrencyIds + " ";
        }
        condition+=" and je.pendingapproval=0 ";
        sqlcondition+=" and je.pendingapproval=0 ";
//        if(generalLedgerFlag && currencyType < 2){
//                if(currencyType==0)  //For base Currency
//                    condition += " and je.currency.currencyID in ( je.company.currency.currencyID )";
//                else    // for Forign Currency
//                    condition += " and je.currency.currencyID not in ( je.company.currency.currencyID )";
//        }
        List list = new ArrayList();
        accountid = AccountingManager.getFilterInString(accountid);
//        String query = "select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and je.entryDate >= ? and je.entryDate <= ? and ac.company.companyID=? and je.deleted=false order by je.entryDate, je.entryNumber";

        params.add(startDate);
        params.add(endDate);    //ERP-8482 

        if (!StringUtil.isNullOrEmpty(Searchjson)) {
            boolean isLineDimPresent = false;
            boolean isGlobalDimPresent = false;
            try {
                JSONObject jobj = new JSONObject(Searchjson);
                JSONArray rootArr = jobj.getJSONArray("root");
                JSONObject searchjobj = null;
                for (int i = 0; i < rootArr.length(); i++) {
                    searchjobj = rootArr.getJSONObject(i);
                    if (searchjobj.optBoolean("iscustomcolumndata")) {
                        isLineDimPresent = true;
                    } else if (!searchjobj.optBoolean("iscustomcolumndata")) {
                        isGlobalDimPresent = true;
                    }
                    if (isLineDimPresent && isGlobalDimPresent) {
                        break;
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            if ((isGlobalDimPresent && !isLineDimPresent) || (isLineDimPresent && isGlobalDimPresent && filterConjuctionCriteria.trim().equalsIgnoreCase(Constants.or.trim()))) {
                condition += " and jed.isSeparated = false ";
                sqlcondition += " and jed.isseparated = 'F' ";
            }
         
            //Advance search code
            String advSearchSqljoin = "", advSearchCustomtablejoin = "", advSearchMySearchFilterString = "";
            boolean isKnockOffAdvancedSearch = false;
            String companyid = "";
            if (pref != null) {
                companyid = pref.getID();
                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    isKnockOffAdvancedSearch = fieldManagerDAOobj.isKnockOffAdvancedSearch(Searchjson, companyid);
                }
            }
        if (!StringUtil.isNullOrEmpty(Searchjson) && ( isKnockOffAdvancedSearch || (pref != null && pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD))) {
//            CompanyAccountPreferences preferences = null;
//            Map<String, Object> advSearchAttributes = null;            
//            filterConjuctionCriteria = filterConjuctionCriteria;
//            if (!StringUtil.isNullOrEmpty(companyid)) {
//                KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//                preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
//            }
            advnacesearch = true;
            if (advSearchAttributes == null || advSearchAttributes.isEmpty()) {
                if (pref != null && pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                    advSearchAttributes = getPerpetualInvAdvanceSearchAttributes(advSearchMySearchFilterString, Searchjson, null, companyid, params, filterConjuctionCriteria, advSearchCustomtablejoin, "", advSearchSqljoin);
                } else {
                    if (isKnockOffAdvancedSearch) {
                        advSearchAttributes = getKnockOffAdvanceSearchAttributes(advSearchMySearchFilterString, Searchjson, null, companyid, params, filterConjuctionCriteria, advSearchCustomtablejoin, "", isLineDimPresent);
                    }
                }
            } else {
                params.addAll((ArrayList) advSearchAttributes.get("params"));
            }
            mySearchFilterString = advSearchAttributes.containsKey("mySearchFilterString") ? (String) advSearchAttributes.get("mySearchFilterString") : "";
            customDataJoin = advSearchAttributes.containsKey("customtablejoin") ? (String) advSearchAttributes.get("customtablejoin") : "";
            sqljoin += advSearchAttributes.containsKey("sqljoin") ? (String) advSearchAttributes.get("sqljoin") : "";
            sqlcondition += advSearchAttributes.containsKey("knockOffCondition") ? (String) advSearchAttributes.get("knockOffCondition") : "";
        }  else {

                HashMap<String, Object> request = new HashMap<String, Object>();
                request.put(Constants.Searchjson, Searchjson);
                request.put(Constants.appendCase, "and");
                request.put(Constants.moduleid, "100");
                request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                try {
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    if (mySearchFilterString.contains(Constants.AccJECustomData)) {
                        customDataJoin += " left join je.accBillInvCustomData jecd ";
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "jecd");
                    }
                    if (mySearchFilterString.contains(Constants.AccJEDetailCustomData)) {
                        customDataJoin += " left join jed.accJEDetailCustomData jedcd ";
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jedcd");
                    }
                    if (mySearchFilterString.contains(Constants.AccJEDetailsProductCustomData)) {
                        customDataJoin += " left join jed.accJEDetailsProductCustomData jepcd ";
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jepcd");
                    }
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                } catch (JSONException ex) {
                    Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }else{
            condition+=" and jed.isSeparated = false ";
        }
        
        if (advnacesearch) {
            sqlquery = sqlquery + sqljoin + customDataJoin + " where jed.account in "+ accountid + sqlcondition + mySearchFilterString +" order by je.entrydate, je.entryno";
            list = executeSQLQuery(sqlquery, params.toArray());
        } else {
            query = query + customDataJoin + "where ac.ID in " + accountid + condition + mySearchFilterString + " order by je.entryDate, je.entryNumber";
            list = executeQuery(query, params.toArray());
        }
        
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public List getLedgerInfo(String usersessionid) throws ServiceException{
        String query = "Select * from temp_gl_details where usersessionid = '"+usersessionid+"' order by jedate, entryno";
        List list = executeSQLQuery(query);
        return list;
    }

    @Override
    public KwlReturnObject getLedgerForReconciliation(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        String query = "";
        String conditionss = "";
        String dateFilterON = "";
        String orderBy = "", accountid="", companyid="", ss="", billid="", innercondition = "";
        int dateFilter = 0;
        Date startDate=null, endDate=null;
        StringBuilder conditionBuildString=new StringBuilder();
        boolean isConcileReport=false, isExportReportRequest=false, isMaintainHistory=false, isReconciledHistoryDetails=false, isMemoPDF=false; 
        ArrayList params = new ArrayList();
        try {
            //Read the arguments from HashMap
            if(requestParams.containsKey(Constants.companyid) && !StringUtil.isNullOrEmpty((String)requestParams.get(Constants.companyid))){
                companyid = (String)requestParams.get(Constants.companyid);
            }
            if(requestParams.containsKey("ss") && !StringUtil.isNullOrEmpty((String)requestParams.get("ss"))){
                ss = (String)requestParams.get("ss");
            }
            if (requestParams.containsKey("dateFilter") && requestParams.get("dateFilter") != null) {
                dateFilter = (Integer) requestParams.get("dateFilter");
            }
            
            if(requestParams.containsKey("accountid") && !StringUtil.isNullOrEmpty((String)requestParams.get("accountid"))){
                accountid = (String)requestParams.get("accountid");
                params.add(accountid);
            }
            if (requestParams.containsKey(Constants.REQ_startdate) && (Date) requestParams.get(Constants.REQ_startdate) != null) {
                startDate = (Date) requestParams.get(Constants.REQ_startdate);
                if (conditionBuildString.length() > 0) {
                    conditionBuildString.append(" and ");
                }
                if (dateFilter == 0) {
                    conditionBuildString.append(" je.entryDate >= ? ");
                } else {
                    conditionBuildString.append(" brd.reconcileDate >= ? ");
                }
                params.add(startDate);
            }
            if (requestParams.containsKey(Constants.REQ_enddate) && (Date) requestParams.get(Constants.REQ_enddate) != null) {
                endDate = (Date) requestParams.get(Constants.REQ_enddate);
                if (conditionBuildString.length() > 0) {
                    conditionBuildString.append(" and ");
                }
                if (dateFilter == 0) {
                 conditionBuildString.append(" je.entryDate <= ? ");
                }else{
                 conditionBuildString.append(" brd.reconcileDate <= ? ");
                }
                
                params.add(endDate);
            }
            if(requestParams.containsKey("isConcileReport") && requestParams.get("isConcileReport")!=null){
                isConcileReport = (Boolean)requestParams.get("isConcileReport");
            }
            if(requestParams.containsKey("isExportReportRequest") && requestParams.get("isExportReportRequest")!=null){
                isExportReportRequest = (Boolean)requestParams.get("isExportReportRequest");
            }
            if(requestParams.containsKey("isMaintainHistory") && requestParams.get("isMaintainHistory")!=null){
                isMaintainHistory = (Boolean)requestParams.get("isMaintainHistory");
            }
	    if(requestParams.containsKey("isMemoPDF") && requestParams.get("isMemoPDF")!=null){
                isMemoPDF = (Boolean)requestParams.get("isMemoPDF");
            }
            if(requestParams.containsKey("isReconciledHistoryDetails") && requestParams.get("isReconciledHistoryDetails")!=null){
                isReconciledHistoryDetails = (Boolean)requestParams.get("isReconciledHistoryDetails");
            }

            if((requestParams.containsKey("billid") && requestParams.get("billid")!=null) && isReconciledHistoryDetails){  //To Fetch the data based on Record ID
                billid = (String)requestParams.get("billid");                
                conditionss += " AND br.ID IN ('"+billid+"') ";                
                if(requestParams.containsKey("isdeleted") && requestParams.get("isdeleted")!=null && (String)requestParams.get("isdeleted")!=""){   //To Identify the type of Record while expanding it
                    innercondition += " AND bankReconciliation.deleted="+(String)requestParams.get("isdeleted");
                }
            }
                        
            params.add(companyid);
            dateFilterON=conditionBuildString.toString();
            if (dateFilter == 0) {  //Bank Reconciliation Report
                orderBy = " order by je.entryDate, je.entryNumber ";
            } else {                //View Reconciliation Report
                orderBy = " order by brd.reconcileDate, je.entryNumber ";
            }

            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = null;
                if (isConcileReport) {
                    searchcol = new String[]{"brd.accountnames", "je.entryNumber"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                    StringUtil.insertParamSearchString(map);
                } else {
                    searchcol = new String[]{"je.entryNumber"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                }
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                conditionss += searchQuery;
            }
            
            conditionss +="  and je.isDishonouredCheque='F' ";
            
            conditionss +="  and je.pendingapproval = 0 "; // Only Approved JE's SDP-4682
            conditionss +="  and je.approvestatuslevel = 11 "; // Only Approved Manual JE's ERP-33658
                
            if (isExportReportRequest && isConcileReport) { //View Reconciliation PDF
                query = "select je, jed, br, brd from BankReconciliationDetail brd inner join brd.bankReconciliation br inner join brd.journalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and "+dateFilterON+" and ac.company.companyID=? and je.deleted=false AND jed.isSeparated=FALSE "
                        + conditionss + " and jed.journalEntry.ID in (select journalEntry.ID from BankReconciliationDetail where bankReconciliation.deleted=false AND bankReconciliation.account.ID=? and company.companyID=? and isOpeningTransaction=false) GROUP BY jed.ID "+orderBy;	//SDP-9064
            } else if (isExportReportRequest && !isConcileReport && isMaintainHistory && isMemoPDF) { //As of Date Bank Reconciliation PDF with Memo
                query = "select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and "+dateFilterON+" and ac.company.companyID=? and je.deleted=false AND jed.isSeparated=FALSE "
                        + conditionss + " and jed.journalEntry.ID not in (select journalEntry.ID from BankReconciliationDetail where bankReconciliation.deleted=false AND bankReconciliation.account.ID=? and company.companyID=? and isOpeningTransaction=false and journalEntry is not NULL and (reconcileDate<=? OR bankReconciliation.clearanceDate<=?)) order by je.entryDate, je.entryNumber";
                
            } else if (isExportReportRequest && !isConcileReport && isMaintainHistory) { //As of Date Bank Reconciliation PDF
                query = "select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and "+dateFilterON+" and ac.company.companyID=? and je.deleted=false AND jed.isSeparated=FALSE "
                        + conditionss + " and jed.journalEntry.ID not in (select journalEntry.ID from BankReconciliationDetail where bankReconciliation.deleted=false AND bankReconciliation.account.ID=? and company.companyID=? and isOpeningTransaction=false and journalEntry is not NULL and (reconcileDate<=? OR bankReconciliation.clearanceDate<=?)) order by je.entryDate, je.entryNumber";
                
            } else if (isReconciledHistoryDetails) {   //Here we are fetching the data from 'BankReconciliationDetailHistory' Table. Other queries are fetching the data from 'BankReconciliationDetail'. So, we can't change the query
                query = "SELECT je, jed, br, brd FROM BankReconciliationDetailHistory brd INNER JOIN brd.bankReconciliation br INNER JOIN brd.journalEntry je INNER JOIN je.details jed INNER JOIN jed.account ac WHERE ac.ID=? AND "+dateFilterON+" AND ac.company.companyID=? AND je.deleted=FALSE AND jed.isSeparated=FALSE "
                        + conditionss + " AND jed.journalEntry.ID IN (SELECT journalEntry.ID FROM BankReconciliationDetailHistory WHERE bankReconciliation.account.ID=? AND company.companyID=? AND isOpeningTransaction=FALSE"+innercondition+") GROUP BY jed.ID "+orderBy;
            } 
            else if (isConcileReport) {
                query = "select je, jed, br, brd from BankReconciliationDetail brd inner join brd.bankReconciliation br inner join brd.journalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and "+dateFilterON+" and ac.company.companyID=? and je.deleted=false AND jed.isSeparated=FALSE "
                        + conditionss + " and jed.journalEntry.ID in (select journalEntry.ID from BankReconciliationDetail where bankReconciliation.deleted=false AND bankReconciliation.account.ID=? and company.companyID=? and isOpeningTransaction=false) GROUP BY jed.ID "+orderBy;
            } else {
                query = "select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and "+dateFilterON+" and ac.company.companyID=? and je.deleted=false AND jed.isSeparated=FALSE "
                        + conditionss + " and jed.journalEntry.ID not in (select journalEntry.ID from BankReconciliationDetail where bankReconciliation.deleted=false AND bankReconciliation.account.ID=? and company.companyID=? and isOpeningTransaction=false and journalEntry is not NULL) order by je.entryDate, je.entryNumber";
            }
            params.add(accountid);
            params.add(companyid);
            if (isExportReportRequest && !isConcileReport && isMaintainHistory) {
                params.add(endDate);
                params.add(endDate);
            }
            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accJournalEntryImpl.getLedgerForReconciliation:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getReconciliationOfOpeningTransactions(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        String query = "";
        String conditionss = "";
        String dateFilterON = "";
        ArrayList params = new ArrayList();
        boolean isConcileReport = false, isExportReportRequest=false, isMaintainHistory=false, isReconciledHistoryDetails=false, isReceipt = false;
        String tableName = "", openingPaymentQuery="";
        String seqNumber = "";
        String orderBy = "", accountid="", companyid="", ss="", billid="", innercondition = "";
        int dateFilter = 0;
        Date startDate=null, endDate=null;        
        try {
            //Read the arguments from HashMap
            if(requestParams.containsKey("isreceipt")){
                isReceipt = (Boolean)requestParams.get("isreceipt");
            }
            tableName = isReceipt? "Receipt":"Payment";
            openingPaymentQuery = isReceipt? " AND voucher.isOpeningBalenceReceipt=TRUE ":" AND voucher.isOpeningBalencePayment=TRUE ";
            seqNumber = (isReceipt? "receipt":"payment")+"Number";
            
            if(requestParams.containsKey(Constants.companyid) && !StringUtil.isNullOrEmpty((String)requestParams.get(Constants.companyid))){
                companyid = (String)requestParams.get(Constants.companyid);
            }
            if(requestParams.containsKey("ss") && !StringUtil.isNullOrEmpty((String)requestParams.get("ss"))){
                ss = (String)requestParams.get("ss");
            }
            if(requestParams.containsKey("accountid") && !StringUtil.isNullOrEmpty((String)requestParams.get("accountid"))){
                accountid = (String)requestParams.get("accountid");
            }
            if(requestParams.containsKey(Constants.REQ_startdate) && (Date)requestParams.get(Constants.REQ_startdate)!=null){
                startDate = (Date)requestParams.get(Constants.REQ_startdate);
            }
            if(requestParams.containsKey(Constants.REQ_enddate) && (Date)requestParams.get(Constants.REQ_enddate)!=null){
                endDate = (Date)requestParams.get(Constants.REQ_enddate);
            }
            if(requestParams.containsKey("isConcileReport") && requestParams.get("isConcileReport")!=null){
                isConcileReport = (Boolean)requestParams.get("isConcileReport");
            }
            if(requestParams.containsKey("isExportReportRequest") && requestParams.get("isExportReportRequest")!=null){
                isExportReportRequest = (Boolean)requestParams.get("isExportReportRequest");
            }
            if(requestParams.containsKey("isMaintainHistory") && requestParams.get("isMaintainHistory")!=null){
                isMaintainHistory = (Boolean)requestParams.get("isMaintainHistory");
            }
            if(requestParams.containsKey("isReconciledHistoryDetails") && requestParams.get("isReconciledHistoryDetails")!=null){
                isReconciledHistoryDetails = (Boolean)requestParams.get("isReconciledHistoryDetails");
            }
            if(requestParams.containsKey("dateFilter") && requestParams.get("dateFilter")!=null){
                dateFilter = (Integer)requestParams.get("dateFilter");
            }
            if((requestParams.containsKey("billid") && requestParams.get("billid")!=null) && isReconciledHistoryDetails){  //To Fetch the data based on Record ID
                billid = (String)requestParams.get("billid");                
                conditionss += " AND br.ID='"+billid+"'";                
                if(requestParams.containsKey("isdeleted") && requestParams.get("isdeleted")!=null && (String)requestParams.get("isdeleted")!=""){   //To Identify the type of Record while expanding it
                    innercondition += " AND bankReconciliation.deleted="+(String)requestParams.get("isdeleted");
                }
            }
            params.add(accountid);
            if (dateFilter == 0) {  //Bank Reconciliation Report
                 if (startDate != null) {
                    dateFilterON = " voucher.creationDate >= ? and ";
                    params.add(startDate);
                }
                dateFilterON += " voucher.creationDate <= ? ";
                orderBy = " order by voucher.creationDate, voucher."+seqNumber+" ";
            } else {                //View Reconciliation Report
                if (startDate != null) {
                    dateFilterON = " brd.reconcileDate >= ? and ";
                    params.add(startDate);
                }//View Reconciliation Report
                dateFilterON += " brd.reconcileDate <= ? ";
                orderBy = " order by brd.reconcileDate, voucher."+seqNumber+" ";
            }
            
            params.add(endDate);
            params.add(companyid);

            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = null;
                if (isConcileReport) {
                    searchcol = new String[]{"brd.accountnames", "voucher."+seqNumber};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                    StringUtil.insertParamSearchString(map);
                } else {
                    searchcol = new String[]{"voucher."+seqNumber};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                }
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                conditionss += searchQuery;
            }
            
            if (isExportReportRequest && isConcileReport) { //View Reconciliation PDF
                query = "select voucher, br, brd from "+ tableName +" as voucher, BankReconciliationDetail brd inner join brd.bankReconciliation br where voucher.ID=brd.transactionID and voucher.payDetail.paymentMethod.account.ID=? and "+ dateFilterON +" and voucher.company.companyID=? "
                        + conditionss + " and voucher.ID in (select b.transactionID from BankReconciliationDetail b where b.company.companyID = ? and b.isOpeningTransaction=true) "+orderBy;
            } else if (isExportReportRequest && !isConcileReport && isMaintainHistory) { //Bank Reconciliation PDF
                query = "select voucher from "+ tableName +" as voucher where voucher.payDetail.paymentMethod.account.ID=? "+openingPaymentQuery;
                if (startDate != null) {
                    query += " and voucher.creationDate >= ? ";
                }
                query +=" and voucher.creationDate <= ? and voucher.company.companyID=? " + conditionss + " and voucher.ID not in (select transactionID from BankReconciliationDetail where company.companyID = ? and isOpeningTransaction=true and (reconcileDate<=? OR bankReconciliation.clearanceDate<=?)) order by voucher.creationDate, voucher."+seqNumber+" ";
            } else if (isReconciledHistoryDetails) {  //Here we are fetching the data from 'BankReconciliationDetailHistory' Table. Other queries are fetching the data from 'BankReconciliationDetail'. So, we can't change the query
                query = "select voucher, br, brd from "+ tableName +" as voucher, BankReconciliationDetailHistory brd inner join brd.bankReconciliation br where voucher.ID=brd.transactionID and voucher.payDetail.paymentMethod.account.ID=? and "+ dateFilterON +" and voucher.company.companyID=? "
                        + conditionss + " and voucher.ID in (select b.transactionID from BankReconciliationDetailHistory b where b.company.companyID = ? and b.isOpeningTransaction=true) "+orderBy;
            } else if (isConcileReport) {   //Here we are fetching the data from 'BankReconciliationDetailHistory' Table. Other queries are fetching the data from 'BankReconciliationDetail'. So, we can't change the query
                query = "select voucher, br, brd from "+ tableName +" as voucher, BankReconciliationDetail brd inner join brd.bankReconciliation br where voucher.ID=brd.transactionID and voucher.payDetail.paymentMethod.account.ID=? and "+ dateFilterON +" and voucher.company.companyID=? "
                        + conditionss + " and voucher.ID in (select b.transactionID from BankReconciliationDetail b where b.company.companyID = ? and b.isOpeningTransaction=true) "+orderBy;
            } else {        //Bank Reconcile Report
                query = "select voucher from "+ tableName +" as voucher where voucher.payDetail.paymentMethod.account.ID=? "+openingPaymentQuery;
                if (startDate != null) {
                    query += " and voucher.creationDate >= ? ";
                }
                query +=" and voucher.creationDate <= ? and voucher.company.companyID=? "+ conditionss + " and voucher.ID not in (select transactionID from BankReconciliationDetail where company.companyID = ? and isOpeningTransaction=true) order by voucher.creationDate, voucher."+seqNumber+" ";
            }
            params.add(companyid);
            if (isExportReportRequest && !isConcileReport && isMaintainHistory) {
                params.add(endDate);
                params.add(endDate);
            }
            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accJournalEntryImpl.getReconciliationOfOpeningTransactions:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getMaxDateOfReconciliation(String companyid, String accountid, Date startDate, Date endDate) throws ServiceException {
        List list = new ArrayList();
        String query=""; 
            query = "select max(br.clearanceDate) from BankReconciliationDetail brd inner join brd.bankReconciliation br inner join brd.journalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and je.entryDate >= ? and je.entryDate <= ? and ac.company.companyID=? and je.deleted=false and jed.journalEntry.ID in (select journalEntry.ID from BankReconciliationDetail where bankReconciliation.deleted=false and company.companyID=?) order by je.entryDate, je.entryNumber";
        Object[] params = {
            accountid,
            AccountingManager.setFilterTime(startDate, true),
            AccountingManager.setFilterTime(endDate, false),
            companyid,
            companyid
        };
        list = executeQuery( query, params);
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public double getReconciliationClearBalance(String companyid, String accountid, Date endDate) throws ServiceException {
        List list = new ArrayList();
        List list1 = new ArrayList();
        double rate = 0;
        double rate1 = 0;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        ArrayList params = new ArrayList();
        String query = "select sum(clearingamount) from bankreconciliation where account=? and company=? and enddate<=?  and deleteflag='F'";
        String query1 = "select sum(clearingamount) from bankreconciliation where account=? and company=? and enddate<=?  and deleteflag='T'";
        params.add(accountid);
        params.add(companyid);
        params.add(dateFormat.format(endDate));
        list = executeSQLQuery( query, params.toArray());
        list1 = executeSQLQuery( query1, params.toArray());
        if (list != null && list.size() > 0) {
            if (list.get(0) != null) {
                rate = (Double) list.get(0);
            }
        }
        if (list1 != null && list1.size() > 0) {
            if (list1.get(0) != null) {
                rate1 = (Double) list1.get(0);
    }
        }
        return rate-rate1;
    }

    public KwlReturnObject getTax1099AccJE(String companyid, Date endDate, String vendorid) throws ServiceException {
        List list = new ArrayList();
        String query = "select jed,sum(jed.amount) from JournalEntryDetail jed where jed.account.ID in (select t.account.ID from Tax1099Accounts  t where t.company.companyID=?) and  "
                + "jed.debit=true and jed.journalEntry.entryDate <= ?  and jed.journalEntry.deleted=false and  jed.journalEntry.ID in"
                + "(select sjed.journalEntry.ID from JournalEntryDetail sjed where  sjed.account.ID=? and sjed.debit=false and sjed.company.companyID=?)  group by jed.account.ID";
        Object[] params = {
            companyid, endDate, vendorid, companyid
        };
        list = executeQuery( query, params);
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getJournalEntryDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from JournalEntryDetail";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getJournalEntryDetailsForBank(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
//            String currencyId=(String) request.get("gcurrencyid");
            String endDate = (String) request.get("enddate");
            String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            DateFormat df = (DateFormat) request.get(Constants.df);
            ArrayList params = new ArrayList();
            String condition = "";
            String accountid = null;
            if (request.containsKey("accountid")) {
                accountid = (String) request.get("accountid");
            }
            //String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");

            if (!StringUtil.isNullOrEmpty(companyid)) {
                params.add(companyid);
            }
            if (!StringUtil.isNullOrEmpty(accountid)) {
                params.add(accountid);
                condition += " and account.ID=?";
            }
//            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
//                condition += " and journalEntry.currency.currencyID = ?";
//                params.add(currencyfilterfortrans);
//            }
            if (!StringUtil.isNullOrEmpty(endDate)) {
                condition += " and  journalEntry.entryDate <=? ";
                params.add(df.parse(endDate));
            }
            condition += " and journalEntry.deleted = false and jed.isSeparated = false ";  //SDP-10604
            String query = "from JournalEntryDetail jed where company.companyID=? " + condition; //" order by inv.customerEntry.account.id, inv.invoiceNumber";   orderSubQuery         
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accJournalEntryImpl.getJournalEntryDetailsForBank:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    @Override
    public KwlReturnObject getJournalEntryDetailsForReport(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String jeIds="";
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String ss = (String) request.get(JournalEntryConstants.SS);
            String linkentry = (String) request.get(JournalEntryConstants.LINKID);
            boolean deleted = Boolean.parseBoolean((String) request.get(JournalEntryConstants.DELETED));
            boolean nondeleted = Boolean.parseBoolean((String) request.get(JournalEntryConstants.NONDELETED));
            boolean isDraft = false;
            boolean isGSTAuditFile = false;//Addded isGSTAuditFile flag to handle advance search in GAF export case for Multi Entity flow.
            boolean isKnockOffAdvancedSearch = false;
            boolean isUserSummaryReportFlag = (request.containsKey("isUserSummaryReportFlag") && request.get("isUserSummaryReportFlag")!=null) ? Boolean.parseBoolean(request.get("isUserSummaryReportFlag").toString()) : false;;
            String companyid = (String) request.get(JournalEntryConstants.COMPANYID);
            if (request.containsKey(Constants.isGSTAuditFile) && request.get(Constants.isGSTAuditFile) != null) {
                isGSTAuditFile = (boolean) request.get(Constants.isGSTAuditFile);
            }
            if (request.containsKey("isDraft") && request.get("isDraft") != null) {
                isDraft = (Boolean) request.get("isDraft");
            }
           
            ArrayList params = new ArrayList();
            params.add((String) request.get(JournalEntryConstants.COMPANYID));
            String query = " from JournalEntryDetail jed ";
            String condition = " where jed.company.companyID=? ";
            String orderBy = "";

            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and jed.journalEntry.costcenter.ID=?";
            }

            if (request.containsKey("jeIds")) {
                jeIds = (String) request.get("jeIds");
                if (!StringUtil.isNullOrEmpty(jeIds)) {
                    condition += " and jed.journalEntry.ID IN(" + jeIds + ")";
                }
            }

            if (request.containsKey("onlyPendingApprovalJesFlag") && request.get("onlyPendingApprovalJesFlag") != null && (Boolean) request.get("onlyPendingApprovalJesFlag")) {
                condition += " and jed.journalEntry.pendingapproval=0 and jed.journalEntry.istemplate != 2 and (jed.journalEntry.approvestatuslevel = 11) ";
            }

            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                if (isUserSummaryReportFlag) {
                    condition += " and (jed.journalEntry.createdOn >=? and jed.journalEntry.createdOn <=?) ";
                    DateFormat df1 = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
                    params.add(df1.parse(startDate).getTime());
                    params.add(df1.parse(endDate).getTime());

                } else {
                    condition += " and (jed.journalEntry.entryDate >=? and jed.journalEntry.entryDate <=?)";
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
            }

            if (nondeleted) {
                condition += " and jed.journalEntry.deleted=false ";
            } else if (deleted) {
                condition += " and jed.journalEntry.deleted=true ";
            }
            
            if (isDraft) {
                condition += " and jed.journalEntry.draft = true ";
            } else {
                condition += " and jed.journalEntry.draft = false ";
            }
            
            if (!StringUtil.isNullOrEmpty(linkentry)) {
                params.add(linkentry);
                condition += " and jed.journalEntry.ID=? ";
                orderBy = " order by jed.journalEntry.ID,jed.debit desc";
            } else {
                if (!StringUtil.isNullOrEmpty(ss)) {
                    params.add("%"+ss + "%");
                    params.add("%"+ss + "%");
                    condition += " and (jed.journalEntry.entryNumber like ? or jed.journalEntry.memo like ?) ";
                    orderBy = " order by jed.journalEntry.ID,jed.debit desc";
                } else {
                    orderBy = " order by jed.journalEntry.ID,jed.debit desc";
                }
            }
            
            String mySearchFilterString = "";
            String searchDefaultFieldSQL = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            String searchJson = "";
            String customdatajoin = "";
            if(!StringUtil.isNullOrEmpty((String) request.get(Constants.Acc_Search_Json))){
                searchJson = (String) request.get(Constants.Acc_Search_Json);
            }
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().trim().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            
            if (!StringUtil.isNullOrEmpty(searchJson)) {
                isKnockOffAdvancedSearch = fieldManagerDAOobj.isKnockOffAdvancedSearch(searchJson, companyid);
            }
            /*
            No need to search again in detail level if request from JE report becuase records are already filtered
            */
            boolean isJEReport = false;
            if (request.containsKey("isJEReport") && request.get("isJEReport") != null) {
                isJEReport = Boolean.parseBoolean(request.get("isJEReport").toString());
            }
            if (!StringUtil.isNullOrEmpty(searchJson)) {
                if (isGSTAuditFile || isJEReport) {
                    searchJson = accAccountDAOobj.getAdvanceSearchStringForMultiEntity(searchJson,companyid);
                    request.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                    request.put(Constants.appendCase, Constants.and);
                    request.put(Constants.Searchjson, searchJson);
                    request.put(Constants.moduleid, "100");

                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    if (mySearchFilterString.contains(Constants.AccJECustomData)) {
                        customdatajoin += " inner join jed.journalEntry je left join je.accBillInvCustomData jecd ";
                        mySearchFilterString = mySearchFilterString.replaceAll(Constants.AccJECustomData, "jecd");
                    }
                    if (mySearchFilterString.contains(Constants.AccJEDetailCustomData)) {
                        customdatajoin += " left join jed.accJEDetailCustomData jedcd ";
                        mySearchFilterString = mySearchFilterString.replaceAll(Constants.AccJEDetailCustomData, "jedcd");
                    }
                    StringUtil.insertParamAdvanceSearchString1(params, searchJson);
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                    
                    query = "select distinct(jed) from JournalEntryDetail jed ";
                    if (!isJEReport) {
                        condition += " and jed.isSeparated = false ";
                    }
                    
                } else {
                    request.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                    request.put(Constants.appendCase, Constants.and);
                    request.put(Constants.Searchjson, searchJson);
                    mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                    if (mySearchFilterString.contains("AccJECustomData")) {
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "jed.journalEntry.accBillInvCustomData");
                    }
                    if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");
                    }
                    StringUtil.insertParamAdvanceSearchString1(params, searchJson);
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
                if (isKnockOffAdvancedSearch) {
                    condition += Constants.KNOCK_OF_HQL_CONDITION;
                }
            }else{
                condition += " and jed.isSeparated = false ";
            }
            query += customdatajoin + condition + mySearchFilterString + orderBy;
            list = executeQuery( query, params.toArray());//params.toArray() sessionHandlerImpl.getCompanyid(request));  new Object[]{ sessionHandlerImpl.getCompanyid(request)}
            count = list.size();

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getJournalEntry : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "", "", list, count);
    }

    @Override
    public KwlReturnObject getJournalEntryForFinanceReport(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String ss = (String) request.get(JournalEntryConstants.SS);
            String linkentry = (String) request.get(JournalEntryConstants.LINKID);
            String currencyid = (String) request.get("currencyid");
            String accountid = (String) request.get("accountid");
            boolean deleted = Boolean.parseBoolean(request.get(JournalEntryConstants.DELETED) + "");

            ArrayList params = new ArrayList();
            params.add((String) request.get(JournalEntryConstants.COMPANYID));
            String orderBy = "";
            String condition = " where jed.journalEntry.typeValue = 2 and jed.journalEntry.draft=false and jed.journalEntry.company.companyID=? "; //for Journal entry type fund transfer( 2 party journal entry for finacial report)

//            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
//            if (!StringUtil.isNullOrEmpty(costCenterId)) {
//                params.add(costCenterId);
//                condition += " and journalEntry.costcenter.ID=?";
//            }

            if (request.containsKey("jeIds")) {
                String jeIds = (String) request.get("jeIds");
                if (!StringUtil.isNullOrEmpty(jeIds)) {
                    condition += " and ID IN(" + jeIds + ")";
                }
            }
            if (!StringUtil.isNullOrEmpty(currencyid)) {
                condition += " and jed.journalEntry.currency.currencyID IN(" + currencyid + ")";
            }
            if (!StringUtil.isNullOrEmpty(accountid) && !accountid.contains("All")) {
                //condition +="and je inner join je.details jed inner join jed.account ac where ac.ID IN(" + accountid + ")";   
                accountid = AccountingManager.getFilterInString(accountid);
                condition += " and  jed.account.ID IN " + accountid;
            }
            //String query = "select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and je.entryDate >= ? and je.entryDate <= ? and ac.company.companyID=? and je.deleted=false and jed.journalEntry.ID not in (select journalEntry.ID from BankReconciliationDetail where bankReconciliation.deleted=false and company.companyID=?) order by je.entryDate, je.entryNumber";
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and ( jed.journalEntry.entryDate >= ? and jed.journalEntry.entryDate <= ? )";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            if (deleted) {
                condition += " and jed.journalEntry.deleted=false ";
            }

            if (!StringUtil.isNullOrEmpty(linkentry)) {
                params.add(linkentry);
                condition += " and ID=? order by ID,srno asc";
            } else {
                if (!StringUtil.isNullOrEmpty(ss)) {
                    params.add(ss + "%");
                    params.add(ss + "%");
                    condition += " and (jed.journalEntry.entryNumber like ? or jed.journalEntry.memo like ?)"; //,debit
                    orderBy = " order by jed.account.ID desc";
                } else {
                    orderBy += " order by jed.account.ID desc";  //,debit
                }
            }
            String appendCase = "and";
            String mySearchFilterString = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }

            String Searchjson = "";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = request.get("searchJson").toString();

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, appendCase);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "jed.journalEntry.accBillInvCustomData");
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }

            String query = "Select DISTINCT jed.journalEntry from JournalEntryDetail jed" + condition + mySearchFilterString + orderBy;
            list = executeQuery( query, params.toArray());//params.toArray() sessionHandlerImpl.getCompanyid(request));  new Object[]{ sessionHandlerImpl.getCompanyid(request)}
            count = list.size();

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getJournalEntry : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "", "", list, count);
    }

    @Override
    public KwlReturnObject getJournalEntryDetailsForFinanceReport(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            params.add((String) request.get(JournalEntryConstants.COMPANYID));
            String condition = " where company.companyID=? ";
            if (request.containsKey("jeId") && !StringUtil.isNullOrEmpty("jeID")) {
                condition += " and journalEntry.ID=? ";
                params.add((String) request.get("jeId"));
            }
            String debit = ((String) request.get("debit"));
            if (debit.equalsIgnoreCase("debit")) {
                condition += " and debit='T' ";
            } else {
                condition += " and debit='F' ";
            }
            boolean lineLevelAmount = true;
            if (request.containsKey("lineLevelAmount")) {
                lineLevelAmount = Boolean.parseBoolean(request.get("lineLevelAmount").toString());
            }
            String appendCase = "and";
            String mySearchFilterString = "";
            String joinString = "";
            String joinString1 = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String Searchjson = "";
            if (request.containsKey("searchJson") && request.get("searchJson") != null && lineLevelAmount) {
                Searchjson = request.get("searchJson").toString();

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, appendCase);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "jed.journalEntry.accBillInvCustomData");
//                    joinString1=" inner join jed.journalEntry je ";
                    if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//
                    }
                    if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");//    
//                        joinString1 += " inner join jed.accJEDetailsProductCustomData jedprdc ";
                    }
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            String query = "from JournalEntryDetail jed" + condition +mySearchFilterString+ "order by jed.account.ID desc";
            list = executeQuery( query, params.toArray());//params.toArray() sessionHandlerImpl.getCompanyid(request));  new Object[]{ sessionHandlerImpl.getCompanyid(request)}
            count = list.size();

        } catch (Exception ex) {
            throw ServiceException.FAILURE("JournalEntryDetail : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "", "", list, count);
    }

    public KwlReturnObject getJournalEntryCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from AccJECustomData";
        return buildNExecuteQuery( query, requestParams);
    }
    
    public KwlReturnObject getJournalEntryCustomDataNew(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select journalentryId from AccJECustomData ";
        return buildNExecuteQuery(query, requestParams);
    }
    
    public KwlReturnObject getOpeningBalanceInvoiceCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from OpeningBalanceInvoiceCustomData";
        return buildNExecuteQuery( query, requestParams);
    }
    public KwlReturnObject getOpeningBalanceInvoiceCustomDataNew(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select OpeningBalanceInvoiceId from OpeningBalanceInvoiceCustomData";
        return buildNExecuteQuery( query, requestParams);
    }
    
    public KwlReturnObject getOpeningBalanceDebitNoteCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from OpeningBalanceDebitNoteCustomData";
        return buildNExecuteQuery(query, requestParams);
    }

    public KwlReturnObject getOpeningBalanceCreditNoteCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from OpeningBalanceCreditNoteCustomData";
        return buildNExecuteQuery(query, requestParams);
    }

    public KwlReturnObject getOpeningBalanceReceiptCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from OpeningBalanceReceiptCustomData";
        return buildNExecuteQuery(query, requestParams);
    }
    public KwlReturnObject getOpeningBalancePaymentCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from OpeningBalanceMakePaymentCustomData";
        return buildNExecuteQuery(query, requestParams);
    }
    
    @Override
    public KwlReturnObject saveJournalEntry(Map<String, Object> dataMap) throws ServiceException, AccountingException {
        List list = new ArrayList();
        String existingInventoryFlag = "";
        try {
            JournalEntry je =getJournalEntry(dataMap);
            if (je.getCompany() != null && je.getDetails() != null) {
                Set jeDetails = je.getDetails();
                updateJETemplateCode(je, jeDetails.iterator(), je.getCompany().getCompanyID());
            }
            save(je);
            list.add(je);
        } catch (Exception ex) {
            throw new AccountingException(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
        
        @Override
    public KwlReturnObject saveJournalEntryByObject(JournalEntry journalEntry) throws ServiceException, AccountingException {
             List list = new ArrayList();
        try {
            if (journalEntry.getCompany() != null && journalEntry.getDetails() != null) {
                Set jeDetails = journalEntry.getDetails();
                updateJETemplateCode(journalEntry, jeDetails.iterator(), journalEntry.getCompany().getCompanyID());
            }
            saveOrUpdate(journalEntry);
            list.add(journalEntry);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveJournalEntry : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
        
    @Override
    public JournalEntry getJournalEntry(Map<String, Object> dataMap) throws ServiceException, AccountingException {
        List list = new ArrayList();
        String existingInventoryFlag = "";
        JournalEntry je = new JournalEntry();
        String companyId = "";
        String gCurrencyId = "";
        boolean isRoundingDFCNTaxEntry =false;
        boolean isprecisiondiff =false; //landed cost precision difference case ERP-31096
        Integer typeValue = 0;
        try {
            if (dataMap.containsKey(JEID)) {
                je = (JournalEntry) get(JournalEntry.class, (String) dataMap.get(JEID));
                if (dataMap.containsKey("jeisedit")) {
                    existingInventoryFlag = je.getIsInventory();
                    delete(je);
                    je = new JournalEntry();
                    je.setID((String) dataMap.get(JEID));
                }
            }

            if (dataMap.containsKey(Constants.SEQFORMAT)) {
                je.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) dataMap.get(Constants.SEQFORMAT)));
            }
                        //ERP-38100
            if (dataMap.containsKey(Constants.SEQNUMBER)) {
                je.setSeqnumber(Integer.parseInt(dataMap.get(Constants.SEQNUMBER).toString()));
            }
            if (dataMap.containsKey(Constants.DATEPREFIX) && dataMap.get(Constants.DATEPREFIX) != null) {
                je.setDatePreffixValue((String) dataMap.get(Constants.DATEPREFIX));
            }
            if (dataMap.containsKey(Constants.DATEAFTERPREFIX) && dataMap.get(Constants.DATEAFTERPREFIX) != null) {
                je.setDateAfterPreffixValue((String) dataMap.get(Constants.DATEAFTERPREFIX));
            }
            if (dataMap.containsKey(Constants.DATESUFFIX) && dataMap.get(Constants.DATESUFFIX) != null) {
                je.setDateSuffixValue((String) dataMap.get(Constants.DATESUFFIX));
            }
            if (dataMap.containsKey(ENTRYNUMBER)) {
                je.setEntryNumber((String) dataMap.get(ENTRYNUMBER));
            }
            if (dataMap.containsKey(GSTRTYPE)) {
                je.setGstrType((Integer) dataMap.get(GSTRTYPE));
            }
            if (dataMap.containsKey(ITC_TRANSACTION_IDS)) {
                je.setItcTransactionIds((String) dataMap.get(ITC_TRANSACTION_IDS));
            }
            if (dataMap.containsKey(AUTOGENERATED)) {
                je.setAutoGenerated((Boolean) dataMap.get(AUTOGENERATED));
            }
            if (dataMap.containsKey(ENTRYDATE)) {
                je.setEntryDate((Date) dataMap.get(ENTRYDATE));
            }
            if (dataMap.containsKey(MEMO)) {
                je.setMemo((String) dataMap.get(MEMO));
            }
            if (dataMap.containsKey(EXTERNALCURRENCYRATE)) {
                je.setExternalCurrencyRate((Double) dataMap.get(EXTERNALCURRENCYRATE));
            }
            if (dataMap.containsKey(COMPANYID)) {
                Company company = dataMap.get(COMPANYID) == null ? null : (Company) get(Company.class, (String) dataMap.get(COMPANYID));
                je.setCompany(company);
                if (dataMap.containsKey("jeisedit")) {
                    je.setIsInventory(existingInventoryFlag);
                } else {
                    CompanyAccountPreferences accPref = dataMap.get(COMPANYID) == null ? null : (CompanyAccountPreferences) get(CompanyAccountPreferences.class, (String) dataMap.get(COMPANYID));
                    je.setIsInventory(accPref != null ? (accPref.isWithoutInventory() ? "0" : "1") : null);
                }
            }
            if (dataMap.containsKey("createdby")) {
                User createdby = dataMap.get("createdby") == null ? null : (User) get(User.class, (String) dataMap.get("createdby"));
                je.setCreatedby(createdby);
            }
            if (dataMap.containsKey("isroundingdfinCNtax")) {
                isRoundingDFCNTaxEntry=(boolean)dataMap.get("isroundingdfinCNtax");
            }
            if (dataMap.containsKey(ReverseJournalEntry)) {
                je.setReverseJournalEntry((String) dataMap.get(ReverseJournalEntry));
            }
            if (dataMap.containsKey("baddebtentryNumber")) {
                je.setBadDebtSeqNumber((String) dataMap.get("baddebtentryNumber"));
            }
            if (dataMap.containsKey(IsReverseJE)) {
                je.setIsReverseJE((Boolean) dataMap.get(IsReverseJE));
            }
            if (dataMap.containsKey("accjecustomdataref")) {
                je.setAccBillInvCustomData((AccJECustomData) get(AccJECustomData.class, (String) dataMap.get("accjecustomdataref")));
            }
            if (dataMap.containsKey(CURRENCYID)) {
                KWLCurrency currency = dataMap.get(CURRENCYID) == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) dataMap.get(CURRENCYID));
                je.setCurrency(currency);
            }
             if (dataMap.containsKey("isFromPOS")) {
                je.setIsFromPOS((Boolean) dataMap.get("isFromPOS"));
            }
            if (dataMap.containsKey("pendingapproval")) {
                je.setPendingapproval((Integer) dataMap.get("pendingapproval"));
            } else {
                je.setPendingapproval(0);
            }
            if (dataMap.containsKey("isDraft") && dataMap.get("isDraft") != null) {
                je.setDraft((Boolean) dataMap.get("isDraft"));
            } else {
                je.setDraft(false);
            }
            if (dataMap.containsKey("isReval")) {
                je.setIsReval((Integer) dataMap.get("isReval"));
            } else {
                je.setIsReval(0);
            }
            if (dataMap.containsKey("revalInvoiceId")) {
                je.setRevalInvoiceId((String) dataMap.get("revalInvoiceId"));
            }
            if (dataMap.containsKey("gstCurrencyRate")) {
                je.setGstCurrencyRate((Double) dataMap.get("gstCurrencyRate"));
            }
            if (dataMap.containsKey("parentid")) {
                JournalEntry JE = dataMap.get("parentid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) dataMap.get("parentid"));
                je.setParentJE(JE);
            }
            if (dataMap.containsKey("repeateid")) {
                RepeatedJE RJE = dataMap.get("repeateid") == null ? null : (RepeatedJE) get(RepeatedJE.class, (String) dataMap.get("repeateid"));
                je.setRepeateJE(RJE);
            }

            if (dataMap.containsKey("istemplate")) {
                je.setIstemplate((Integer) dataMap.get("istemplate"));
            } else {
                je.setIstemplate(0);
            }
            if (dataMap.containsKey("typevalue")) {
                typeValue= (Integer) dataMap.get("typevalue");
                je.setTypeValue(typeValue);
            }
            if (dataMap.containsKey("partlyJeEntryWithCnDn")) {
                je.setPartlyJeEntryWithCnDn((Integer) dataMap.get("partlyJeEntryWithCnDn"));
            }
            if (dataMap.containsKey("isBadDebtJE") && dataMap.get("isBadDebtJE") != null) {
                je.setBadDebtJE((Boolean) dataMap.get("isBadDebtJE"));
            }
            if (dataMap.containsKey("isTaxAdjustmentJE") && dataMap.get("isTaxAdjustmentJE") != null) {
                je.setTaxAdjustmentJE((Boolean) dataMap.get("isTaxAdjustmentJE"));
            }
            if (dataMap.containsKey(CCConstants.JSON_costcenterid)) {
                CostCenter costCenter = dataMap.get(CCConstants.JSON_costcenterid) == null ? null : (CostCenter) get(CostCenter.class, (String) dataMap.get(CCConstants.JSON_costcenterid));
                je.setCostcenter(costCenter);
            }
            if (dataMap.containsKey(CCConstants.JSON_cheque)) {
                Cheque cheque = dataMap.get(CCConstants.JSON_cheque) == null ? null : (Cheque) get(Cheque.class, (String) dataMap.get(CCConstants.JSON_cheque));
                je.setCheque(cheque);
            }
            if (dataMap.containsKey("paidToCmb")) {
                //  je.setPaidTo((String) dataMap.get("paidToCmb"));
                MasterItem paidToCmb = dataMap.get("paidToCmb") == null ? null : (MasterItem) get(MasterItem.class, (String) dataMap.get("paidToCmb"));
                je.setPaidTo(paidToCmb);
            }
            if (dataMap.containsKey("pmtmethod")) {
                //   je.setPaymentMethod((String) dataMap.get("pmtmethod"));
                PaymentMethod pmtmethod = dataMap.get("pmtmethod") == null ? null : (PaymentMethod) get(PaymentMethod.class, (String) dataMap.get("pmtmethod"));
                je.setPaymentMethod(pmtmethod);
            }
            if (dataMap.containsKey("isexchangegainslossje") && dataMap.get("isexchangegainslossje") != null) {
                je.setIsexchangegainslossje((Boolean) dataMap.get("isexchangegainslossje"));
            }
            //ERP-31096 isprecisiondiff flag for landed cost transactions that cause precision loss
            if (dataMap.containsKey(Constants.isPrecisiongDiffInLandedCost) && dataMap.get(Constants.isPrecisiongDiffInLandedCost) != null) {
               isprecisiondiff =  ((Boolean) dataMap.get(Constants.isPrecisiongDiffInLandedCost));
            }
            if(dataMap.containsKey("PaymentCurrencyToPaymentMethodCurrencyRate") && dataMap.get("PaymentCurrencyToPaymentMethodCurrencyRate")!=null){
                je.setPaymentcurrencytopaymentmethodcurrencyrate((Double)dataMap.get("PaymentCurrencyToPaymentMethodCurrencyRate"));
            } else {
                je.setPaymentcurrencytopaymentmethodcurrencyrate(1);    //ERP-9166 : Set Default value for Bank Charges JE
            }
            if(dataMap.containsKey("ismulticurrencypaymentje") && dataMap.get("ismulticurrencypaymentje")!=null){
                je.setIsmulticurrencypaymentje((Boolean)(dataMap.get("ismulticurrencypaymentje")));
            } else {
                je.setIsmulticurrencypaymentje(false);  // In case the datamap does not contain 'ismulticurrencypaymentje', default value 'False' will be set
            }
            je.setCreatedOn(System.currentTimeMillis());
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            if (dataMap.containsKey(COMPANYID)) {
                companyId = (String) dataMap.get(COMPANYID);
                Company company = dataMap.get(COMPANYID) == null ? null : (Company) get(Company.class,companyId );
                requestParams.put(Constants.companyKey, company.getCompanyID());
                requestParams.put(Constants.globalCurrencyKey, company.getCurrency().getCurrencyID());
                gCurrencyId = company.getCurrency().getCurrencyID(); 
            }

            /*Called two times 
             1.At the time of saving JE
             2.At the time of approving JE
             and 2nd time Jedetails is same as first
             So Skipping to set Jedetails at the time of approving JE as it is not a consistent manner in hibernate
             */
            if (dataMap.containsKey(JEDETAILS) && !(dataMap.containsKey("isEditedPendingDocument") && dataMap.get("isEditedPendingDocument")!=null &&  (Boolean)dataMap.get("isEditedPendingDocument"))) {
                HashSet<JournalEntryDetail> details = (HashSet<JournalEntryDetail>) dataMap.get(JEDETAILS);
                if (!details.isEmpty()) {
                    double amount = 0.0;
                    double creditAmountInBase = 0.0;
                    double debitAmountInBase = 0.0;
                    boolean eliminateflag = false;
                    boolean intercompanyflag = false;
                    KwlReturnObject bAmt = null;
                    CompanyAccountPreferences pref = null;
                    KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                    pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
//                    boolean alreadyAdded = false;
                    
                    for (JournalEntryDetail jed : details) {
//                        if (typeValue != 1 && typeValue != 2 && typeValue != 3) {//ERP-1404 allow to post JE for Rounding Diff account
//                            if (pref.getRoundingDifferenceAccount() != null && jed.getAccount().getID().equals(pref.getRoundingDifferenceAccount().getID())) {
//                                alreadyAdded = true;
//                                continue;
//                            }
//                        }
                        jed.setJournalEntry(je);    //Add JournalEntry Obj in JournalentryDetail
                        if (!jed.isIsSeparated()) { //Excluding separated jed while saving documents 
                            if (!eliminateflag && jed.getAccount() != null && jed.getAccount().isEliminateflag()) {
                                eliminateflag = true;
                            }
                            if (!intercompanyflag && jed.getAccount() != null && jed.getAccount().isIntercompanyflag()) {
                                intercompanyflag = true;
                            }

                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, jed.getAmount(), jed.getJournalEntry().getCurrency() == null ? gCurrencyId : jed.getJournalEntry().getCurrency().getCurrencyID(), jed.getJournalEntry().getEntryDate(),jed.getJournalEntry().getExternalCurrencyRate());
                            jed.setAmountinbase(authHandler.round((Double) bAmt.getEntityList().get(0), companyId));
                            if (jed.isRoundingDifferenceDetail() && isRoundingDFCNTaxEntry) {              //if rounding diffrence jedetail then no need to add in credit/debit amount
                                if (jed.isDebit()) {
                                    debitAmountInBase += authHandler.round((Double) bAmt.getEntityList().get(0), companyId);
                                } else {
                                    debitAmountInBase -= authHandler.round((Double) bAmt.getEntityList().get(0), companyId);
                                }
                                continue;
                            }
                            if (jed.isDebit()) {
                                amount += jed.getAmount();
                                debitAmountInBase += authHandler.round((Double) bAmt.getEntityList().get(0), companyId);
                            } else {
                                amount -= jed.getAmount();
                                creditAmountInBase += authHandler.round((Double) bAmt.getEntityList().get(0), companyId);
                            }
                        }
                    }
                    //ERP-31096 isprecisiondiff flag for landed cost transactions that cause a precision loss 
                    if (Math.abs(amount) >= 0.000001 && Math.abs(debitAmountInBase-creditAmountInBase) > 0.01 && !isprecisiondiff) {
                        throw new AccountingException("Debit and credit amounts are not same");
                    } else if(pref.getRoundingDifferenceAccount() != null ) {
                        debitAmountInBase = authHandler.round(debitAmountInBase, companyId);
                        creditAmountInBase = authHandler.round(creditAmountInBase, companyId);

                        if (creditAmountInBase != debitAmountInBase) {
                            double diff = creditAmountInBase - debitAmountInBase;
                                if (creditAmountInBase < debitAmountInBase) {
                                    diff = diff * -1;
                                }
                                bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, diff, je.getCurrency() == null ? gCurrencyId : je.getCurrency().getCurrencyID(), je.getEntryDate(), je.getExternalCurrencyRate());                                
                                double currencyAmount = (Double) bAmt.getEntityList().get(0);
                                JournalEntryDetail roundJeD = new JournalEntryDetail();
                                roundJeD.setAmount(currencyAmount);
                                roundJeD.setAmountinbase(authHandler.round(diff, companyId));
                                if (creditAmountInBase < debitAmountInBase) {
                                    roundJeD.setDebit(false);
                                } else {
                                    roundJeD.setDebit(true);
                                }
                                roundJeD.setAccount(pref.getRoundingDifferenceAccount());
                                roundJeD.setCompany(je.getCompany());
                                roundJeD.setJournalEntry(je);
                                roundJeD.setRoundingDifferenceDetail(true);
                                details.add(roundJeD);
                            }
                    }
                    je.setEliminateflag(eliminateflag);
                    je.setIntercompanyflag(intercompanyflag);
                        je.setDetails(details);
                    }
                }
            if (dataMap.containsKey("customerid") && dataMap.get("customerid") != null) {
                je.setCustomer(dataMap.get("customerid").toString());
            }
            if (dataMap.containsKey("transactionModuleid")) {
                int transactionModuleId = Integer.parseInt(dataMap.get("transactionModuleid").toString());
                je.setTransactionModuleid(transactionModuleId);
            }
            if (dataMap.containsKey("transactionId")) {
                je.setTransactionId((String) dataMap.get("transactionId"));
            }
            if (dataMap.containsKey("includeInGSTReport") && dataMap.get("includeInGSTReport") != null) {
                je.setToIncludeInGSTReport((Boolean) dataMap.get("includeInGSTReport"));
            }
            if (dataMap.containsKey("isFromEclaim") && dataMap.get("isFromEclaim") != null) {
                je.setIsFromEclaim((Boolean) dataMap.get("isFromEclaim"));
            }
            list.add(je);
        } catch (Exception ex) {
            throw new AccountingException(ex.getMessage(), ex);
        }
        return je;
    }

    public KwlReturnObject saveRevalTime(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            RevaluationTime revaluationTime = new RevaluationTime();
            if (requestParams.containsKey("userid") && requestParams.get("userid") != null) {
                String userid = requestParams.get("userid").toString();
                revaluationTime.setUserid((User) load(User.class, userid));
            }
            if (requestParams.containsKey("company") && requestParams.get("company") != null) {
                String company = requestParams.get("company").toString();
                revaluationTime.setCompany((Company) load(Company.class, company));
            }
            if (requestParams.containsKey("accountType") && requestParams.get("accountType") != null) {
                int accountType = (Integer) requestParams.get("accountType");
                revaluationTime.setAccountType(accountType);
            }
            if (requestParams.containsKey("currencyId") && requestParams.get("currencyId") != null) {
                int currencyId = Integer.parseInt((String) requestParams.get("currencyId"));
                revaluationTime.setCurrencyId(currencyId);
            }
            
            if (requestParams.containsKey("revalId") && requestParams.get("revalId") != null) {
                String revalId = (String)requestParams.get("revalId");
                revaluationTime.setRevalId(revalId);
            }
            if (requestParams.containsKey("revalDate") && requestParams.get("revalDate") != null) {
                revaluationTime.setRevalDate((Date) requestParams.get("revalDate"));
            }
            if (requestParams.containsKey("month") && requestParams.get("month") != null) {
                revaluationTime.setMonth((Integer) requestParams.get("month"));
            }
            if (requestParams.containsKey("year") && requestParams.get("year") != null) {
                revaluationTime.setYear((Integer) requestParams.get("year"));
            }
            saveOrUpdate(revaluationTime);
            list.add(revaluationTime);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveJournalEntry.saveRevalTime", e);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    public KwlReturnObject saveRevaluationJECustomData(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            RevaluationJECustomData revaluationJECustomData = null;
            if (requestParams.containsKey("id") && requestParams.get("id") != null) {
                revaluationJECustomData = (RevaluationJECustomData) get(RevaluationJECustomData.class, (String) requestParams.get("id"));
            }
            if(revaluationJECustomData == null){
                 revaluationJECustomData = new RevaluationJECustomData();
            }
            if (requestParams.containsKey("customfield") && requestParams.get("customfield") != null) {
                revaluationJECustomData.setCustomfield((String) requestParams.get("customfield"));
            }
            if (requestParams.containsKey("lineleveldimensions") && requestParams.get("lineleveldimensions") != null) {
                revaluationJECustomData.setLineleveldimensions((String) requestParams.get("lineleveldimensions"));
            }
            if (requestParams.containsKey("userid") && requestParams.get("userid") != null) {
                String userid = requestParams.get("userid").toString();
                revaluationJECustomData.setUserid((User) load(User.class, userid));
            }
            if (requestParams.containsKey("company") && requestParams.get("company") != null) {
                String company = requestParams.get("company").toString();
                revaluationJECustomData.setCompany((Company) load(Company.class, company));
            }
            saveOrUpdate(revaluationJECustomData);
            list.add(revaluationJECustomData);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveJournalEntry.saveRevaluationJECustomData", e);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }

    public KwlReturnObject getRevaluationJECustomData(String companyid) throws ServiceException {
        List list = new ArrayList();
        StringBuilder query =new StringBuilder("from RevaluationJECustomData where company.companyID= ? "); 
        ArrayList params = new ArrayList();
        params.add(companyid);
        list = executeQuery( query.toString(), params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
   public KwlReturnObject getRevalMonthYearStatus(Date revalDate, int accountType, String currencyIDs, String companyid) throws ServiceException {
        List list = new ArrayList();
        List listCount = new ArrayList();
        List<Object[]> rules = new ArrayList();
        ArrayList params = new ArrayList();
        List countList = new ArrayList();
        StringBuilder queryCountTest =new StringBuilder(" select id from RevaluationTime rt where  rt.revalDate > ? and rt.accountType= ? and company.companyID= ? "); //((rt.year> ?) or (rt.year= ? and ( rt.month > ? or rt.month = ? )))
        params.add(revalDate);
        params.add(accountType);
        params.add(companyid);
        if (!StringUtil.isNullOrEmpty(currencyIDs)) {
           String currency = AccountingManager.getFilterInNumber(currencyIDs);
           queryCountTest.append("and rt.currencyId in ").append(currency);
       }
        listCount = executeQuery(queryCountTest.toString(), params.toArray());
        /**
        * No need to check linking cases for while doing 'Account Revaluation '
        * on same date.
        */
//        if (listCount.size() < 1) {
//           StringBuilder query = new StringBuilder(" select rh.invoiceid,rh.moduleid from revaltime rt,revaluationhistory rh where  rh.revalid=rt.revalid and rt.revaldate >= ? and rt.accounttype= ? and rh.company= ? "); //((rt.year> ?) or (rt.year= ? and ( rt.month > ? or rt.month = ? )))
//           params.clear();
//           params.add(revalDate);
//           params.add(accountType);
//
//           params.add(companyid);
//           if (!StringUtil.isNullOrEmpty(currencyIDs)) {
//               String currency = AccountingManager.getFilterInNumber(currencyIDs);
//               query.append("and rt.currencyId in ").append(currency);
//           }
//
//           list = executeSQLQuery(query.toString(), params.toArray());
//           for (Object object : list) {
//               Object[] groupArr = (Object[]) object;
//               params.clear();
//               params.add(groupArr[0]);
//               params.add(Integer.parseInt(groupArr[1].toString()));
//               String queryCount = "select id from receiptlinking where linkeddocid=? and moduleid=?";
//               listCount = executeSQLQuery(queryCount, params.toArray());
//               if (listCount.size() > 0) {
//                   break;
//               }
//               queryCount = "select id from creditnotelinking where linkeddocid=? and moduleid=?";
//               listCount = executeSQLQuery(queryCount, params.toArray());
//               if (listCount.size() > 0) {
//                   break;
//               }
//               queryCount = "select id from debitnotelinking where linkeddocid=? and moduleid=?";
//               listCount = executeSQLQuery(queryCount, params.toArray());
//               if (listCount.size() > 0) {
//                   break;
//               }
//               /*
//                * To Check Vendor invoice link for payment or CN/DN
//                */
//               queryCount = "select id from goodsreceiptlinking where linkeddocid=? and moduleid=?";
//               listCount = executeSQLQuery(queryCount, params.toArray());
//               if (listCount.size() > 0) {
//                   break;
//               }
//               /*
//                * To Check Customer invoice link for payment or CN/DN
//                */
//               queryCount = "select id from invoicelinking where linkeddocid=? and moduleid=?";
//               listCount = executeSQLQuery(queryCount, params.toArray());
//               if (listCount.size() > 0) {
//                   break;
//               }
//           queryCount = "select id from PaymentLinking where LinkedDocID=?and ModuleID=?";
//           listCount = executeQuery(queryCount, params.toArray());
//               if (listCount.size() > 0) {
//                   break;
//               }
//
//
//               /*
//                * Append list if record is multiple
//                */
//
//           }
//       }
        if (listCount.size()> 0) {
                countList.add(listCount);
            }
        
        return new KwlReturnObject(true, "", null, countList, countList.size());
    }
   
   public KwlReturnObject deleteRevalhistoryEntry(Date revalDate, String ids[], String evalId, int accTypeId) throws ServiceException {

        ArrayList params = new ArrayList();


        //SQL       delete from jedetail where journalEntry ='ff80808128d936af0128d9395fb00002' and  (select count(*) from invoice where journalEntry ='ff80808128d936af0128d9395fb00002' and (centry=jedetail.id or sentry=jedetail.id or oentry=jedetail.id or taxentry=jedetail.id))=0;
        String query = "";
        List <Object[]>list = new ArrayList();
        ArrayList returnList = new ArrayList();
        List listIds = new ArrayList();
        for (int i = 0; i < ids.length; i++) {
            params.clear();
            params.add(revalDate);
            params.add(ids[i]);
            params.add(evalId);
            params.add(accTypeId);
            /*
             * Delete record from Reval history for date and reval id.
             */
            query = "delete rh from revaluationhistory rh inner join revaltime rt on rh.revalid = rt.revalid where rh.evaldate = ? and rh.currency =? and rh.revalid!=? and rt.accounttype = ?";
            executeSQLUpdate(query, params.toArray());

            /*
             * Delete record from revaltime for particular date ,currency and
             * revalid
             */

            query = "delete from revaltime where revaldate = ? and  currencyid=? and revalid!=? and accounttype = ?";
            executeSQLUpdate(query, params.toArray());

            params.clear();
            params.add(evalId);
            params.add(ids[i]);
            params.add(revalDate);
           query = "select rh.invoiceid from revaluationhistory rh where rh.revalid=? and rh.currency =? and evaldate = ?";
           listIds = executeSQLQuery(query, params.toArray());
           int idsCount = 0;
           while (idsCount < listIds.size()) {
                params.clear();
               params.add(listIds.get(idsCount));
                params.add(revalDate);
               if (listIds.get(idsCount) != "" && !StringUtil.isNullOrEmpty(listIds.get(idsCount).toString())) {
                    query = "select id,entryno from journalentry where revalinvoiceid =? and entrydate=?";
                    list = executeSQLQuery(query, params.toArray());
                    returnList.add(list);
                   int inv = 0;
                    if (list != null && !list.isEmpty()) {
                        for (Object[] row : list) {
                            params.clear();
                            params.add(row[0]);
                            /*
                             * Delete record form journal details
                             */
                            if (row[0] != "" && !StringUtil.isNullOrEmpty(row[0].toString())) {
                                query = "delete from jedetail where journalentry=? ";
                                executeSQLUpdate(query, params.toArray());

                                /*
                                 * delete record form journal Entry table
                                 */

                                query = "delete from journalentry where id=?";
                                executeSQLUpdate(query, params.toArray());
                            }
                           inv++;
                        }
                    }
                }
                idsCount++;
            }
           

       }
        return new KwlReturnObject(true, "Invoice has been updated successfully.", null, returnList, returnList.size());
    }

    public KwlReturnObject ReevaluationHistoryReport(Map<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            String companyid = (String) request.get(COMPANYID);
            String start = (request.containsKey("start") && request.get("start") != null) ? (String) request.get("start") : "";
            String limit = (request.containsKey("limit") && request.get("limit") != null) ? (String) request.get("limit") : "";
            DateFormat df = (DateFormat) request.get("df");
            ArrayList params = new ArrayList();
            String condition = "";
            params.add(companyid);
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (rh.evaldate >= ? and rh.evaldate <= ? )";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }  //   company.companyID is null  
            String query = "from RevaluationHistory rh where company.companyID= ? " + condition+" order by rh.evaldate desc ";   //rt.accountType= ? and rt.currencyId= ? 
            String countQuery = "select count(rh) from RevaluationHistory rh where company.companyID= ? " + condition+" order by rh.evaldate desc ";   //rt.accountType= ? and rt.currencyId= ? 
            List countList = executeQuery(countQuery, params.toArray());
            if (!countList.isEmpty()) {
                count = countList.get(0) != null ? Integer.parseInt(countList.get(0).toString()) : 0;
            }
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                list = executeQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            } else {
                list = executeQuery(query, params.toArray());
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getFromRevalID(Map<String, Object> requestParams) throws ServiceException {//String invoiceid ,String companyid,boolean isRealised
        List list = new ArrayList();
        String query = null;
        ArrayList params = new ArrayList();
        if (requestParams.containsKey("revalid") && requestParams.get("revalid") != null) {
            String revalid = (String) requestParams.get("revalid");
            params.add(revalid);
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);
        }
        query = " from RevaluationHistory rt where  rt.revalid= ? and company.companyID=? and rt.deleted=false ";//deleted=false   sort by evaldate

        list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getRevalInvoiceId(Map<String, Object> requestParams) throws ServiceException {//String invoiceid ,String companyid,boolean isRealised
        List list = new ArrayList();
        String query = null;
        ArrayList params = new ArrayList();
        if (requestParams.containsKey("invoiceid") && requestParams.get("invoiceid") != null) {
            String invoiceid = requestParams.get("invoiceid").toString();
            params.add(invoiceid);
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
            String companyid = requestParams.get("companyid").toString();
            params.add(companyid);
        }
        if (requestParams.containsKey("isRealised")) {
            Boolean isRealised = Boolean.parseBoolean(requestParams.get("isRealised") + "");
            params.add(isRealised);
            query = " from RevaluationHistory rt where  rt.invoiceid= ? and company.companyID=? and rt.isRealised= ?  and rt.deleted=false ";//deleted=false sort by evaldate
        } else {
            query = " from RevaluationHistory rt where  rt.invoiceid= ? and company.companyID=? and rt.deleted=false and rt.issaveeval = 1 order by evaldate Desc";//deleted=false   sort by evaldate
            //featching history of re-evalution for invoice on decending order.
        }
        list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject updateCustomFieldJournalEntry(Map<String, Object> dataMap) throws ServiceException, AccountingException {
        List list = new ArrayList();
        String existingInventoryFlag = "";
        try {
            JournalEntry je = new JournalEntry();

            if (dataMap.containsKey(JEID)) {
                je = (JournalEntry) get(JournalEntry.class, (String) dataMap.get(JEID));
                if (dataMap.containsKey("jeisedit")) {
                    existingInventoryFlag = je.getIsInventory();
                    delete(je);
                    je = new JournalEntry();
                    je.setID((String) dataMap.get(JEID));
                }
            }
            if (dataMap.containsKey(ENTRYNUMBER)) {
                je.setEntryNumber((String) dataMap.get(ENTRYNUMBER));
            }
            if (dataMap.containsKey(GSTRTYPE)) {
                je.setGstrType((Integer) dataMap.get(GSTRTYPE));
            }
            if (dataMap.containsKey(ITC_TRANSACTION_IDS)) {
                je.setItcTransactionIds((String) dataMap.get(ITC_TRANSACTION_IDS));
            }
            if (dataMap.containsKey(AUTOGENERATED)) {
                je.setAutoGenerated((Boolean) dataMap.get(AUTOGENERATED));
            }
            if (dataMap.containsKey(ENTRYDATE)) {
                je.setEntryDate((Date) dataMap.get(ENTRYDATE));
            }
            if (dataMap.containsKey(MEMO)) {
                je.setMemo((String) dataMap.get(MEMO));
            }
            if (dataMap.containsKey(EXTERNALCURRENCYRATE)) {
                je.setExternalCurrencyRate((Double) dataMap.get(EXTERNALCURRENCYRATE));
            }
            if (dataMap.containsKey(COMPANYID)) {
                Company company = dataMap.get(COMPANYID) == null ? null : (Company) get(Company.class, (String) dataMap.get(COMPANYID));
                je.setCompany(company);
                if (dataMap.containsKey("jeisedit")) {
                    je.setIsInventory(existingInventoryFlag);
                } else {
                    CompanyAccountPreferences accPref = dataMap.get(COMPANYID) == null ? null : (CompanyAccountPreferences) get(CompanyAccountPreferences.class, (String) dataMap.get(COMPANYID));
                    je.setIsInventory(accPref != null ? (accPref.isWithoutInventory() ? "0" : "1") : null);
                }
            }
            if (dataMap.containsKey(ReverseJournalEntry)) {
                je.setReverseJournalEntry((String) dataMap.get(ReverseJournalEntry));
            }
            if (dataMap.containsKey(IsReverseJE)) {
                je.setIsReverseJE((Boolean) dataMap.get(IsReverseJE));
            }
            if (dataMap.containsKey("accjecustomdataref")) {
                je.setAccBillInvCustomData((AccJECustomData) get(AccJECustomData.class, (String) dataMap.get("accjecustomdataref")));
            }
            if (dataMap.containsKey(CURRENCYID)) {
                KWLCurrency currency = dataMap.get(CURRENCYID) == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) dataMap.get(CURRENCYID));
                je.setCurrency(currency);
            }
            if (dataMap.containsKey("pendingapproval")) {
                je.setPendingapproval((Integer) dataMap.get("pendingapproval"));
            } else {
                je.setPendingapproval(0);
            }
            if (dataMap.containsKey("isReval")) {
                je.setIsReval((Integer) dataMap.get("isReval"));
            } else {
                je.setIsReval(0);
            }
            if (dataMap.containsKey("istemplate")) {
                je.setIstemplate((Integer) dataMap.get("istemplate"));
            } else {
                je.setIstemplate(0);
            }
            if (dataMap.containsKey(CCConstants.JSON_costcenterid)) {
                CostCenter costCenter = dataMap.get(CCConstants.JSON_costcenterid) == null ? null : (CostCenter) get(CostCenter.class, (String) dataMap.get(CCConstants.JSON_costcenterid));
                je.setCostcenter(costCenter);
            }
            je.setCreatedOn(System.currentTimeMillis());
            if (dataMap.containsKey(JEDETAILS)) {
                HashSet<JournalEntryDetail> details = (HashSet<JournalEntryDetail>) dataMap.get(JEDETAILS);
                if (!details.isEmpty()) {
                    Iterator<JournalEntryDetail> itr = details.iterator();
                    double amount = 0.0;
                    boolean eliminateflag = false;
                    boolean intercompanyflag = false;
                    while (itr.hasNext()) {
                        JournalEntryDetail jed = itr.next();
                        jed.setJournalEntry(je);
                        if (!eliminateflag && jed.getAccount() != null && jed.getAccount().isEliminateflag()) {
                            eliminateflag = true;
                        }
                        if (!intercompanyflag && jed.getAccount() != null && jed.getAccount().isIntercompanyflag()) {
                            intercompanyflag = true;
                        }
                        if (jed.isDebit()) {
                            amount += jed.getAmount();
                        } else {
                            amount -= jed.getAmount();
                        }
                    }
                    if (Math.abs(amount) >= 0.000001) {
                        throw new AccountingException("Debit and credit amounts are not same");
                    }
                    je.setEliminateflag(eliminateflag);
                    je.setIntercompanyflag(intercompanyflag);
                    je.setDetails(details);
                }
            }

            save(je);
            list.add(je);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveJournalEntry : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getJournalEntryDetail(String jeid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "from JournalEntryDetail jed where jed.journalEntry.ID=? and company.companyID=?";
        ArrayList params = new ArrayList();
        params.add(jeid);
        params.add(companyid);
        list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getAllJournalEntry(String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "from JournalEntry where company.companyID=?";
        ArrayList params = new ArrayList();

        params.add(companyid);
        list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getPnLTemplates(HashMap<String, Object> filterParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String companyid = (String) filterParams.get("companyid");
        params.add(companyid);

        String query = "from Templatepnl where  deleted = false and company.companyID = ? order by name ";

        List list = executeQuery( query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getAccountsFormappedPnL(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            String templateid = (String) requestParams.get("templateid");

            Templatepnl templatepnl = (Templatepnl) get(Templatepnl.class, templateid);

            int templateMapid = templatepnl.getTemplateid();

            if (requestParams.containsKey("accountid") && !StringUtil.isNullOrEmpty((String) requestParams.get("accountid"))) {
                condition = " account.ID = ? and ";
                params.add((String) requestParams.get("accountid"));
            }

            params.add(templateMapid);
            params.add(companyid);

            String query = "from PnLAccountMap where " + condition + " templateid = ? and company.companyID=?  order by account.name";
            List list = executeQuery( query, params.toArray());

            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccounts:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    public void updateJETemplateCode(JournalEntry JEObj, Iterator itrDetails, String companyid) throws ServiceException {
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
//            KwlReturnObject resultJED = getJournalEntryDetail(JEObj.getID(), companyid);
//            List listJED = resultJED.getEntityList();

            KwlReturnObject result = getPnLTemplates(requestParams);
            List list = result.getEntityList();

            Iterator itr = list.iterator();
            Long finalCode = 0l;
            //Fetched templates
            while (itr.hasNext()) {
                Templatepnl listObj = (Templatepnl) itr.next();
                int templatecode = listObj.getTemplateid();

                int templateCode = 0;
                templateCode += (Math.pow(2, templatecode));

                Iterator itrJED = itrDetails;
                boolean isMapJE = true;

                while (itrJED.hasNext()) {
                    JournalEntryDetail jedObj = (JournalEntryDetail) itrJED.next();
                    String accountid = jedObj.getAccount() == null ? "" : jedObj.getAccount().getID();
                    HashMap<String, Object> requestParamsMap = new HashMap<String, Object>();

                    requestParamsMap.put("templateid", listObj.getID());
                    requestParamsMap.put("companyid", companyid);
                    requestParamsMap.put("accountid", accountid);
                    KwlReturnObject resultExists = getAccountsFormappedPnL(requestParamsMap);
                    if (resultExists.getRecordTotalCount() == 0) {
                        isMapJE = false;
                        break;
                    }
                }

                if (isMapJE) {
                    finalCode += templateCode;
                    JEObj.setTemplatepermcode(finalCode);
                }
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("updateJETemplateCode : " + ex.getMessage(), ex);
        }
    }

    public KwlReturnObject deleteJournalEntryDetails(String jeid, String companyid) throws ServiceException {
        //SQL       delete from jedetail where journalEntry ='ff80808128d936af0128d9395fb00002' and  (select count(*) from invoice where journalEntry ='ff80808128d936af0128d9395fb00002' and (centry=jedetail.id or sentry=jedetail.id or oentry=jedetail.id or taxentry=jedetail.id))=0;
        String delQuery = "delete from JournalEntryDetail jed where jed.journalEntry.ID=?  and jed.company.companyID=?";// and (select count(*) from Invoice inv where inv.journalEntry.ID =? and inv.customerEntry=jed.id) =0";
        int numRows = executeUpdate( delQuery, new Object[]{jeid, companyid});//,jeid});

        return new KwlReturnObject(true, "Invoice has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteJournalEntryDetailRow(String jedid, String companyid) throws ServiceException {
        //SQL       delete from jedetail where journalEntry ='ff80808128d936af0128d9395fb00002' and  (select count(*) from invoice where journalEntry ='ff80808128d936af0128d9395fb00002' and (centry=jedetail.id or sentry=jedetail.id or oentry=jedetail.id or taxentry=jedetail.id))=0;
        String delQuery = "delete from JournalEntryDetail jed where jed.ID=?  and jed.company.companyID=?";// and (select count(*) from Invoice inv where inv.journalEntry.ID =? and inv.customerEntry=jed.id) =0";
        int numRows = executeUpdate( delQuery, new Object[]{jedid, companyid});//,jeid});

        return new KwlReturnObject(true, "Invoice has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject getJEDFixedAssetSale(String companyid, String accountid, boolean isDebit, String Description) throws ServiceException {
        // TODO Auto-generated method stub
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(companyid);
        params.add(accountid);
        params.add(isDebit);
        params.add(Description);
        String Query = "from JournalEntryDetail jed where jed.company.companyID=? and jed.account.ID=? and jed.debit=? and jed.journalEntry.ID=? and jed.journalEntry.deleted = false";
        list = executeQuery( Query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject updateReverseJournalEntryValue(JournalEntry journalEntry, String reverseJournalEntry) throws ServiceException {
        List list = new ArrayList();
        ArrayList params1 = new ArrayList();
        params1.add(reverseJournalEntry);
        params1.add(journalEntry.getCompany().getCompanyID());
        String delQuery1 = "update journalentry set reversejournalentry=? where company = ? and id in ('" + journalEntry.getID() + "')";
        int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject approvePendingJE(String jeid) throws ServiceException {
        String query = "update JournalEntry set pendingapproval = 0  where ID=? ";

        int numRows = executeUpdate( query, new Object[]{jeid});
        return new KwlReturnObject(true, "Journal entry has been updated successfully.", null, null, numRows);
    }

    public Double getRevalHistoryRateForInvoice(String invoiceid,Map<String, Object> reqParams) throws ServiceException {
        List list = new ArrayList();
        double rate = 0;
        String companyid = (String) reqParams.get(Constants.companyKey);
        Date enddate = new Date();
        ArrayList params = new ArrayList();
        if (reqParams.containsKey(Constants.REQ_enddate) && !StringUtil.isNullObject(reqParams.get(Constants.REQ_enddate))){
            enddate = (Date) reqParams.get(Constants.REQ_enddate);
        }
        params.add(enddate);
        params.add(invoiceid);
        params.add(companyid);
        
        String query = " select if(evaldate = ? , currentrate, evalrate) from revaluationhistory where invoiceid= ? and issaveeval= 1 and company = ? order by evaldate desc limit 1";
        list = executeSQLQuery( query, params.toArray());
        if (list.size() > 0) {
            rate = (Double) list.get(0);
        }
        return rate;
    }

    public KwlReturnObject deleteJournalEntryReval(String invoiceid, String companyid) throws ServiceException {
        String query = "update JournalEntry set deleted=true where revalInvoiceId=? and company.companyID=? and isReval=1";
        int numRows = executeUpdate( query, new Object[]{invoiceid, companyid});
        return new KwlReturnObject(true, "Journal Entry has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject permanentDeleteJournalEntryDetailReval(String invoiceid, String companyid) throws ServiceException {
        String query = "delete from JournalEntryDetail jed where jed.journalEntry.ID in (Select je.ID from JournalEntry je where je.revalInvoiceId=? and je.isReval=1 and je.company.companyID=?)";
        int numRows = executeUpdate( query, new Object[]{invoiceid, companyid});
        return new KwlReturnObject(true, "Journal Entry Details has been deleted successfully.", null, null, numRows);
    }
    
    public KwlReturnObject permanentDeleteCreditNoteAgainstVendorGst(String cnId, String companyid) throws ServiceException {
        String query="";
        query = "delete from CreditNoteAgainstVendorGst where creditNote.ID=? and company.companyID=? ";
        int numRows = executeUpdate( query, new Object[]{cnId, companyid});
        return new KwlReturnObject(true, "CreditNoteAgainstVendorGst Details has been deleted successfully.", null, null, numRows);
    }
    
    public KwlReturnObject permanentDeleteDebitNoteAgainstCustomerGst(String dnId, String companyid) throws ServiceException {
        String query="";
        query = "delete from DebitNoteAgainstCustomerGst where debitNote.ID=? and company.companyID=? ";
        int numRows = executeUpdate( query, new Object[]{dnId, companyid});
        return new KwlReturnObject(true, "DebitNoteAgainstCustomerGst Details has been deleted successfully.", null, null, numRows);
    }
    
    public KwlReturnObject permanentDeleteJournalEntryReval(String invoiceid, String companyid) throws ServiceException {
        String query = "delete from JournalEntry where revalinvoiceid=? and company.companyID=? and isReval=1";
        int numRows = executeUpdate( query, new Object[]{invoiceid, companyid});
        return new KwlReturnObject(true, "Journal Entry has been deleted successfully.", null, null, numRows);
    }
    /**
     * deleteRevaluationHistory method deletes all the entries from revaluationhistory table on the basis of document id.
     * @param paramsJobj(companyID and documentId)
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject deleteRevaluationHistory(JSONObject paramsJobj) throws ServiceException {
        ArrayList queryParams = new ArrayList();
        String documentId = paramsJobj.optString("documentId", "");
        String companyID = paramsJobj.optString("companyID", "");
        queryParams.add(documentId);
        queryParams.add(companyID);
        String query = "delete from RevaluationHistory where invoiceid=? and company.companyID=?";
        int numRows = executeUpdate(query, new Object[]{documentId, companyID});
        return new KwlReturnObject(true, "Revaluation History has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject updateRevaluationFlag(String id) throws ServiceException {

        ArrayList params = new ArrayList();
        params.add(id);
        //SQL       delete from jedetail where journalEntry ='ff80808128d936af0128d9395fb00002' and  (select count(*) from invoice where journalEntry ='ff80808128d936af0128d9395fb00002' and (centry=jedetail.id or sentry=jedetail.id or oentry=jedetail.id or taxentry=jedetail.id))=0;
        String query = " Update revaluationhistory set issaveeval = 1 where revalid = ? ";
        int cnt = executeSQLUpdate( query, params.toArray());

        return new KwlReturnObject(true, "Invoice has been updated successfully.", null, null, cnt);
    }

    public KwlReturnObject deleteRevalEntry(String id) throws ServiceException {

        ArrayList params = new ArrayList();
        params.add(id);
        //SQL       delete from jedetail where journalEntry ='ff80808128d936af0128d9395fb00002' and  (select count(*) from invoice where journalEntry ='ff80808128d936af0128d9395fb00002' and (centry=jedetail.id or sentry=jedetail.id or oentry=jedetail.id or taxentry=jedetail.id))=0;
        String query = "delete from revaluationhistory where revalid = ?";
        int cnt = executeSQLUpdate( query, params.toArray());

        return new KwlReturnObject(true, "Invoice has been deleted successfully.", null, null, cnt);
    }
    
    /**
     * resetIsRealisedFlagofRevalHistory method is used to reset the isrealised column to false of revaluationhistory table.
     * @param params
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject resetIsRealisedFlagofRevalHistory(Map<String, Object> params) throws ServiceException {
        ArrayList queryParams = new ArrayList();
        String oldRevalId = params.containsKey("oldRevalId") ? params.get("oldRevalId").toString() : "";
        queryParams.add(oldRevalId);

        String query = "update revaluationhistory set isrealised='F' where revalid = ?";

        int cnt = executeSQLUpdate(query, queryParams.toArray());
        return new KwlReturnObject(true, "Revaluation History has been deleted successfully.", null, null, cnt);
    }

    public KwlReturnObject deleteJEDetailsCustomData(String jedetailid) throws ServiceException {
        String delQuery = "delete from AccJEDetailCustomData  where jedetailId=?";
        int numRows = executeUpdate( delQuery, new Object[]{jedetailid});

        return new KwlReturnObject(true, "JE Details Custom Data has been deleted successfully.", null, null, numRows);
    }
   public KwlReturnObject deleteJECustomData(String jeid) throws ServiceException {
        String delQuery = "delete from AccJECustomData  where journalentryId=?";
        int numRows = executeUpdate( delQuery, new Object[]{jeid});

        return new KwlReturnObject(true, "JE Custom Data has been deleted successfully.", null, null, numRows);
    }
    @Override
    public KwlReturnObject getMonthlyRevenue(String companyid, String accountid, Date startDate, Date endDate) throws ServiceException {
        List list = new ArrayList();

        try {

            List params = new ArrayList();

            String query = "select ac.name, "
                    + "   jed.amount as MonthlyRevenue,  "
                    + "   year(je.entrydate) as year,     "
                    + "   DATE_FORMAT(je.entrydate, '%b') as month  ,"
                    + "   je.currency as currency  ,"
                    + "   je.entrydate as entrydate ,"
                    + "   je.externalcurrencyrate as externalcurrencyrate  "
                    + " from receipt as r  "
                    + "   inner join journalentry as je on r.journalentry = je.id  "
                    + "   inner join jedetail as jed on jed.journalentry = je.id  "
                    + "   inner join account as ac on jed.account = ac.id  "
                    + " where r.deleteflag='F'  and ac.company=?"
                    + " and ac.id=?  "
                    + " and je.entryDate >= ? and je.entryDate <= ? "//GROUP BY DATE_FORMAT(je.entrydate, '%b'), year(je.entrydate) "
                    + " UNION  "
                    + "select ac.name, "
                    + "   jed.amount as MonthlyRevenue,  "
                    + "   year(je.entrydate) as year,     "
                    + "   DATE_FORMAT(je.entrydate, '%b') as month,  "
                    + "   je.currency as currency  ,"
                    + "   je.entrydate as entrydate ,"
                    + "   je.externalcurrencyrate as externalcurrencyrate "
                    + " from invoice as cashinv  "
                    + "   inner join journalentry as je on cashinv.journalentry = je.id  "
                    + "   inner join jedetail as jed on jed.journalentry = je.id  "
                    + "   inner join account as ac on jed.account = ac.id  "
                    + " where cashinv.deleteflag='F' and cashinv.cashtransaction=1 and ac.company=?"
                    + " and ac.id=?  "
                    + " and je.entryDate >= ? and je.entryDate <= ? ";
                    //"GROUP BY DATE_FORMAT(je.entrydate, '%b'), year(je.entrydate) ";

            params.add(companyid);
            params.add(accountid);
            params.add(startDate);
            params.add(endDate);
            params.add(companyid);
            params.add(accountid);
            params.add(startDate);
            params.add(endDate);

            list = executeSQLQuery( query, params.toArray());

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(this.getClass().getName() + ".getMonthlyRevenue:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public String[] getNextAutoNumber_modified(String companyID, String modulename) throws ServiceException, AccountingException {
        String autoNumber = "", pattern = "";
        String sqltable = "";
        String nextNumTemp = "";
        String suffix = "", prefix = "", sequenceformatid = null;
        int startfrom = 1, numberofdigit = 0;
        boolean showleadingzero = false;
        try {

            if (modulename.equals("autojournalentry")) {
                sqltable = "journalentry";
            } else if (modulename.equals("autoinvoice")) {
                sqltable = "invoice";
            } else if (modulename.equals("autocreditmemo")) {
                sqltable = "creditnote";
            } else if (modulename.equals("autodebitnote")) {
                sqltable = "debitnote";
            }

            String hql = "from SequenceFormat y where y.deleted=false and y.company.companyID=? and y.modulename=? ";
            List<SequenceFormat> list = executeQuery( hql, new Object[]{companyID, modulename});
            for (SequenceFormat sequenceFormat : list) {
                sequenceformatid = sequenceFormat.getID();
                pattern = sequenceFormat.getName();
                startfrom = sequenceFormat.getStartfrom();
                prefix = sequenceFormat.getPrefix();
                suffix = sequenceFormat.getSuffix();
                numberofdigit = sequenceFormat.getNumberofdigit();
                showleadingzero = sequenceFormat.isShowleadingzero();
                if (!StringUtil.isNullOrEmpty(pattern)) {
                    break;       //since we require only one sequence format   
                }
            }
            if (StringUtil.isNullOrEmpty(sequenceformatid)) {
                String msg = "";
                if (modulename.equals("autojournalentry")) {
                    msg = "No Sequence Format Created for Journal Entry";
                } else if (modulename.equals("autoinvoice")) {
                    msg = "No Sequence Format Created for Customer invoice";
                }
                throw new AccountingException(msg);
            }

            String condition = "";
            List paramslist = new ArrayList();
            paramslist.add(companyID);
            if (!StringUtil.isNullOrEmpty(sequenceformatid)) {
                paramslist.add(sequenceformatid);
                condition += "and seqformat = ? ";
            }
            hql = "select max(seqnumber) from " + sqltable + " where company =  ? " + condition;
            List ll = executeSQLQuery( hql, paramslist.toArray());
            int nextNumber = startfrom;
            if (!ll.isEmpty()) {
                if (ll.get(0) != null) {
                    nextNumber = Integer.parseInt(ll.get(0).toString()) + 1;
                }
            }
            nextNumTemp = nextNumber + "";
            if (showleadingzero) {
                while (nextNumTemp.length() < numberofdigit) {
                    nextNumTemp = "0" + nextNumTemp;
                }
            }
            autoNumber = prefix + nextNumTemp + suffix;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("hrmsSalaryJE.getNextAutoNumber_modified : " + ex.getMessage(), ex);
        }
        return new String[]{autoNumber, nextNumTemp, sequenceformatid};
    }

    @Override
    public KwlReturnObject approvePendingJE(String jeID, String companyid, int approvalStatus) throws ServiceException {
        String query = "update JournalEntry set approvestatuslevel = ? where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{approvalStatus, jeID, companyid});
        return new KwlReturnObject(true, "Journal Entry has been updated successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject rejectPendingJE(String jeid, String companyid) throws ServiceException {
        try {
            String query = "update JournalEntry set deleted=true,approvestatuslevel = (-approvestatuslevel) where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{jeid, companyid});
            return new KwlReturnObject(true, "Journal Entry has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.rejectPendingJE : " + ex.getMessage(), ex);
        }
    }
    
    @Override
    public void reverseRecurringJEForOneTime(String reversejeno, String companyid) throws ServiceException {
        List params = new ArrayList();
        List rules = new ArrayList();
        String jeid = "";
        params.add(reversejeno);
        params.add(companyid);
        try {
            String jequery = "Select id from journalentry where entryno=? and company=?";
            rules = executeSQLQuery( jequery, params.toArray());
            jeid = (String)rules.get(0);
            String query = "update JournalEntry set isOneTimeReverse=false where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{jeid, companyid});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.reverseRecurringJEForOneTime : " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean checkForRule(int level, String companyid, String amount, String userid) throws AccountingException, ServiceException, ScriptException {
        boolean validate = false;
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        int levelToCheck = level;
        List params = new ArrayList();
        params.add(levelToCheck);
        params.add(companyid);
        params.add(Constants.Acc_GENERAL_LEDGER_ModuleId); // Journal Entry Module ID = 24
        List<Object[]> rules = new ArrayList();
        String query = "select id,level,rule from multilevelapprovalrule where level= ? and companyid = ? and moduleid = ?";
        try {
            rules = executeSQLQuery( query, params.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Object[] row : rules) {
            String rule = row[2].toString();
            String ruleExpression = rule;
            rule = rule.replaceAll("[$$]+", amount);          
            if ((!StringUtil.isNullOrEmpty(rule) && Boolean.parseBoolean(engine.eval(rule).toString())) || StringUtil.isNullOrEmpty(rule)) { // rule valid so check for current user as approver 
                List user = new ArrayList();
                List ParamsNew = new ArrayList();
                ParamsNew.add(levelToCheck);
                ParamsNew.add(companyid);
                ParamsNew.add(ruleExpression);
                ParamsNew.add(Constants.Acc_GENERAL_LEDGER_ModuleId);
                ParamsNew.add(userid);
//                String query1 = "select * from multilevelapprovalruletargetusers where ruleid in(select id from multilevelapprovalrule where level=? and companyid=? and rule=? and moduleid = ? ) and userid=?";
                String query1 = " select multilevelapprovalruletargetusers.* from multilevelapprovalruletargetusers inner join multilevelapprovalrule on multilevelapprovalruletargetusers.ruleid=multilevelapprovalrule.id where multilevelapprovalrule.level=? and multilevelapprovalrule.companyid=? and multilevelapprovalrule.rule=? and multilevelapprovalrule.moduleid=? and multilevelapprovalruletargetusers.userid=?";
                try {
                    user = executeSQLQuery( query1, ParamsNew.toArray());
                } catch (ServiceException ex) {
                    Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (user.size() != 0) {
                    validate = true;
                    break;
                }
            }
        }
        return validate;
    }
    public KwlReturnObject updateChequePrint(String jeid, String companyid) throws ServiceException {
        String query = "update JournalEntry set chequeprinted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "JournalEntry has been updated successfully.", null, null, numRows);
    }

    public KwlReturnObject getCNFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from CreditNote where journalEntry.ID=? and deleted=false and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public void deletePartyJournalCN(List cnList, String companyid) throws ServiceException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        if (!cnList.isEmpty()) {
            requestParams.put("cnid", cnList.get(0));
            requestParams.put("companyid", companyid);
            requestParams.put("creditNote", true);
            deleteCreditNotesPermanent(requestParams);
        }
    }

    
    //Writen for update product quanity on delete
    public void updateProductQuantityOnDeleteInventory(String inventoryIdString,String companyid) throws ServiceException{
       try {
        String [] inventoryIds=inventoryIdString.split(",");
        for(int i=0;i<inventoryIds.length;i++){
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(inventoryIds[i]);
            String myquery = "select product,baseuomquantity,carryin from inventory where company =?  and id = ? ";
            List<Object[]> list = executeSQLQuery( myquery, params.toArray());
               
                if (list.size() > 0 && !list.contains(null)) {
                    Object[] obj = list.get(0);
                    String productid=(String) obj[0];
                    double baseuomqty = (Double) obj[1];
                    boolean carryin=(Boolean) obj[2];;
                    String updatequery="";
                    ArrayList params1 = new ArrayList();
                    params1.add(baseuomqty);
                    params1.add(companyid);
                    params1.add(productid);
                     if(carryin){
                          updatequery= "update product set availablequantity=( availablequantity- ? )  where company =?  and id = ? ";// minus Purchase and Plus Sales (for Reverse effect for quantity)
                     }else{
                         updatequery= "update product set availablequantity=( availablequantity + ? )  where company =?  and id = ? ";
                     }
                     int numRows = executeUpdate( updatequery, params1.toArray());
                }
                
         }
        
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Credit Note as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
        }
    }
    public KwlReturnObject deleteCreditNotesPermanent(HashMap<String, Object> requestParams) throws ServiceException {

        String delQuery = "", delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuery4 = "", delQuery5 = "", delQuery6 = "", delQuery7 = "", delQuery8 = "";
        int numtotal = 0;
        try {
            if (requestParams.containsKey("cnid") && requestParams.containsKey("companyid")) {

                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("companyid"));
                params8.add(requestParams.get("cnid"));
//                String myquery = "select id from cndetails where creditNote in (select id from creditnote where company =? and id = ?)";
                String myquery = "select cnd.id from cndetails cnd inner join creditnote cn on cnd.creditNote=cn.id where cn.company =? and cn.id = ?";
                List list = executeSQLQuery( myquery, params8.toArray());
                Iterator itr = list.iterator();
                String idStrings = "";
                while (itr.hasNext()) {

                    String invdid = itr.next().toString();
                    idStrings += "'" + invdid + "',";
                }
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    idStrings = idStrings.substring(0, idStrings.length() - 1);
                }

                ArrayList params5 = new ArrayList();
                params5.add(requestParams.get("companyid"));
                params5.add(requestParams.get("cnid"));
//                delQuery5 = "delete from cndetails where creditNote in (select id from creditnote where company =? and id = ?)";
                delQuery5 = "delete cnd from cndetails cnd inner join creditnote cn on cnd.creditNote=cn.id where cn.company =? and cn.id = ?";
                int numRows5 = executeSQLUpdate( delQuery5, params5.toArray());


                ArrayList params = new ArrayList();
                params.add(requestParams.get("companyid"));
                //   params.add(requestParams.get("invoiceid"));
                updateProductQuantityOnDeleteInventory(idStrings,(String) requestParams.get("companyid"));
                delQuery = "delete  from inventory where company =?  and id in(" + idStrings + ") ";
                int numRows = executeSQLUpdate( delQuery, params.toArray());


                ArrayList params9 = new ArrayList();
                params9.add(requestParams.get("companyid"));
                params9.add(requestParams.get("cnid"));
                String myquery1 = " select journalentry from creditnote where company = ? and id=?";
                List list1 = executeSQLQuery( myquery1, params9.toArray());
                Iterator itr1 = list1.iterator();
                String journalent = "";
                while (itr1.hasNext()) {
                    Object jeidobj = itr1.next();
                    String jeidi = (jeidobj != null) ? jeidobj.toString() : "";
                    journalent += "'" + jeidi + "',";
                }
                if (!StringUtil.isNullOrEmpty(journalent)) {
                    journalent = journalent.substring(0, journalent.length() - 1);
                }


                ArrayList params1 = new ArrayList();
                params1.add(requestParams.get("companyid"));
                params1.add(requestParams.get("companyid"));
                params1.add(requestParams.get("cnid"));
                delQuery1 = "delete  from accjedetailcustomdata where jedetailId in (select id from jedetail where company = ? and journalEntry in (select journalentry from creditnote where company =? and id = ?))";
                int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());

                ArrayList params11 = new ArrayList();
                params11.add(requestParams.get("companyid"));
                params11.add(requestParams.get("cnid"));
                delQuery8 = "delete  from cntaxentry  where company =? and creditnote= ?";
                int numRows8 = executeSQLUpdate( delQuery8, params11.toArray());

                ArrayList params6 = new ArrayList();
                params6.add(requestParams.get("companyid"));
                params6.add(requestParams.get("cnid"));
                delQuery6 = "delete  from creditnote  where company =? and id = ?";
                int numRows6 = executeSQLUpdate( delQuery6, params6.toArray());

                ArrayList params10 = new ArrayList();
                params10.add(requestParams.get("companyid"));
                params10.add(requestParams.get("cnid"));
                delQuery7 = "delete from cndiscount where company =? and creditnote =?";
                int numRows7 = executeSQLUpdate( delQuery7, params10.toArray());

                int numRows3 = 0;
                int numRows4 = 0;
                int numRows2 = 0;
                if (!requestParams.containsKey("creditNote")) {
                    ArrayList params3 = new ArrayList();
                    params3.add(requestParams.get("companyid"));
                    delQuery3 = "delete from jedetail where company = ? and journalEntry in (" + journalent + ") ";
                    numRows3 = executeSQLUpdate( delQuery3, params3.toArray());

                    ArrayList params4 = new ArrayList();
                    delQuery4 = "delete from journalentry where id  in (" + journalent + ")";
                    numRows4 = executeSQLUpdate( delQuery4, params4.toArray());

                    ArrayList params2 = new ArrayList();
                    delQuery2 = "delete  from accjecustomdata where journalentryId in (" + journalent + ")";
                    numRows2 = executeSQLUpdate( delQuery2, params2.toArray());
                }

                numtotal = numRows + numRows1 + numRows2 + numRows3 + numRows4 + numRows5 + numRows6 + numRows7 + numRows8;
            }

            return new KwlReturnObject(true, "Credit Note has been deleted successfully.", null, null, numtotal);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Credit Note as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
        }

    }

    public KwlReturnObject getDNFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from DebitNote where journalEntry.ID=? and deleted=false and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public void deletePartyJournalDN(List dnList, String companyid) throws ServiceException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("dnid", dnList.get(0));
        requestParams.put("companyid", companyid);
        requestParams.put("debitNote", true);
        deleteDebitNotesPermanent(requestParams);
    }

    public KwlReturnObject deleteDebitNotesPermanent(HashMap<String, Object> requestParams) throws ServiceException {

        String delQuery = "", delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuery4 = "", delQuery5 = "", delQuery6 = "", delQuery7 = "", delQuery8 = "";
        ;
        int numtotal = 0;
        try {
            if (requestParams.containsKey("dnid") && requestParams.containsKey("companyid")) {

                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("companyid"));
                params8.add(requestParams.get("dnid"));
//                String myquery = "select id from dndetails where debitNote in (select id from debitnote where company =? and id = ?)";
                String myquery = "select dnd.id from dndetails dnd inner join debitnote dn on dnd.debitNote=dn.id  where dn.company =? and dn.id = ?";
                List list = executeSQLQuery( myquery, params8.toArray());
                Iterator itr = list.iterator();
                String idStrings = "";
                while (itr.hasNext()) {

                    String invdid = itr.next().toString();
                    idStrings += "'" + invdid + "',";
                }
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    idStrings = idStrings.substring(0, idStrings.length() - 1);
                }

                ArrayList params5 = new ArrayList();
                params5.add(requestParams.get("companyid"));
                params5.add(requestParams.get("dnid"));
//                delQuery5 = "delete from dndetails where debitNote in (select id from debitnote where company =? and id = ?)";
                delQuery5 = "delete dnd from dndetails dnd inner join debitnote dn on dnd.debitNote=dn.id where dn.company =? and dn.id = ?";
                int numRows5 = executeSQLUpdate( delQuery5, params5.toArray());


                ArrayList params = new ArrayList();
                params.add(requestParams.get("companyid"));
                //   params.add(requestParams.get("invoiceid"));
                updateProductQuantityOnDeleteInventory(idStrings,(String) requestParams.get("companyid"));
                delQuery = "delete  from inventory where company =?  and id in(" + idStrings + ") ";
                int numRows = executeSQLUpdate( delQuery, params.toArray());


                ArrayList params9 = new ArrayList();
                params9.add(requestParams.get("companyid"));
                params9.add(requestParams.get("dnid"));
                String myquery1 = " select journalentry from debitnote where company = ? and id=?";
                List list1 = executeSQLQuery( myquery1, params9.toArray());
                Iterator itr1 = list1.iterator();
                String journalent = "";
                while (itr1.hasNext()) {
                    Object jeidobj = itr1.next();
                    String jeidi = (jeidobj != null) ? jeidobj.toString() : "";
                    journalent += "'" + jeidi + "',";
                }
                if (!StringUtil.isNullOrEmpty(journalent)) {
                    journalent = journalent.substring(0, journalent.length() - 1);
                }


                ArrayList params1 = new ArrayList();
                params1.add(requestParams.get("companyid"));
                params1.add(requestParams.get("companyid"));
                params1.add(requestParams.get("dnid"));
                delQuery1 = "delete  from accjedetailcustomdata where jedetailId in (select id from jedetail where company = ? and journalEntry in (select journalentry from debitnote where company =? and id = ?))";
                int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());

                ArrayList params11 = new ArrayList();
                params11.add(requestParams.get("companyid"));
                params11.add(requestParams.get("dnid"));
                delQuery8 = "delete  from dntaxentry  where company =? and debitnote= ?";
                int numRows8 = executeSQLUpdate( delQuery8, params11.toArray());

                ArrayList params6 = new ArrayList();
                params6.add(requestParams.get("companyid"));
                params6.add(requestParams.get("dnid"));
                delQuery6 = "delete  from debitnote  where company =? and id = ?";
                int numRows6 = executeSQLUpdate( delQuery6, params6.toArray());

                ArrayList params10 = new ArrayList();
                params10.add(requestParams.get("companyid"));
                params10.add(requestParams.get("dnid"));
                delQuery7 = "delete from dndiscount where company =? and debitnote =?";
                int numRows7 = executeSQLUpdate( delQuery7, params10.toArray());

                int numRows3 = 0;
                int numRows4 = 0;
                int numRows2 = 0;
                if (!requestParams.containsKey("debitNote")) {
                    ArrayList params3 = new ArrayList();
                    params3.add(requestParams.get("companyid"));
                    delQuery3 = "delete from jedetail where company = ? and journalEntry in (" + journalent + ") ";
                    numRows3 = executeSQLUpdate( delQuery3, params3.toArray());

                    ArrayList params4 = new ArrayList();
                    delQuery4 = "delete from journalentry where id  in (" + journalent + ")";
                    numRows4 = executeSQLUpdate( delQuery4, params4.toArray());

                    ArrayList params2 = new ArrayList();
                    delQuery2 = "delete  from accjecustomdata where journalentryId in (" + journalent + ")";
                    numRows2 = executeSQLUpdate( delQuery2, params2.toArray());
                }
                numtotal = numRows + numRows1 + numRows2 + numRows3 + numRows4 + numRows5 + numRows6 + numRows7 + numRows8;
            }

            return new KwlReturnObject(true, "Debit Note has been deleted successfully.", null, null, numtotal);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Debit Note as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
        }

    }
    
    @Override
    public KwlReturnObject getJEforRefund(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            
            String companyID = "";
            if (requestParams.containsKey("companyID") && requestParams.get("companyID") != null) {
                companyID = (String) requestParams.get("companyID");
            }
            
            String entryNumber = "";
            if (requestParams.containsKey("entryNumber") && requestParams.get("entryNumber") != null) {
                entryNumber = (String) requestParams.get("entryNumber");
            }
            
            params.add(companyID);
            
            
            String condition = "";
            if (!StringUtil.isNullOrEmpty(entryNumber)) {
                condition += " and ( je.entryNumber like ? ) ";
                
                params.add("%" + entryNumber.trim());
            }

            String query = " from JournalEntry je where je.approvestatuslevel = 11 and je.deleted = false and je.company.companyID = ? " + condition;

            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getJEforRefund : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public int getModuleIdForJournalEntry(HashMap<String, Object> requestParams , String jeId) throws ServiceException{
        int moduleId=0;
        try{
            String companyId="";
            String queryMakePayment="";
            String queryAdvanceMPLinkedToInvoice="";
            String queryMakePaymentWithRealisedGainLoss="";
            String queryReceivePayment = "";
            String queryAdvanceRPLinkedInvoice="";
            String queryReceivePaymentWithRealisedGainLoss="";
            String queryCreditNote="";
            String queryDebitNote="";    
            
            ArrayList params= new ArrayList();            
            List list = new ArrayList();
            int count=0;
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyId = (String) requestParams.get("companyid");
            }
            params.add(jeId);
            params.add(companyId);
            
            // If journal entry is posted for Make payment with foreign gain loss
            queryMakePayment = "select id from payment where journalentry =? and company = ?";
            list = executeSQLQuery( queryMakePayment, params.toArray());
            if(list.size()>0){
                moduleId = Constants.Acc_Make_Payment_ModuleId;
                return moduleId;
            }
            
            //If journal entry is posted from linking of advance payment and invoice
            queryAdvanceMPLinkedToInvoice = "select id from linkdetailpayment where linkedgainlossje =? and company = ?";
            list = executeSQLQuery( queryAdvanceMPLinkedToInvoice, params.toArray());
            if(list.size()>0){
                moduleId = Constants.Acc_Make_Payment_ModuleId;
                return moduleId;
            }
            
            //If journal entry is posted from Make payment that have realised gain loss
            queryMakePaymentWithRealisedGainLoss = "select id from payment where revaljeid =? and company = ?";
            list = executeSQLQuery( queryMakePaymentWithRealisedGainLoss, params.toArray());
            if(list.size()>0){
                moduleId = Constants.Acc_Make_Payment_ModuleId;
                return moduleId;
            }
            
            // If journal entry is posted for Receive payment with foreign gain loss
            queryReceivePayment = "select id from receipt where journalentry =? and "
                    + "company = ?";
            list = executeSQLQuery( queryReceivePayment, params.toArray());
            if(list.size()>0){
                moduleId = Constants.Acc_Receive_Payment_ModuleId;
                return moduleId;
            }
            
            //If journal entry is posted from linking of advance payment and invoice
            queryAdvanceRPLinkedInvoice = "select id from linkdetailreceipt where linkedgainlossje =? and company = ?";
            list = executeSQLQuery( queryAdvanceRPLinkedInvoice, params.toArray());
            if(list.size()>0){
                moduleId = Constants.Acc_Receive_Payment_ModuleId;
                return moduleId;
            }
            
            //If journal entry is posted from Receive payment that have realised gain loss
            queryReceivePaymentWithRealisedGainLoss = "select id from receipt where revaljeid =? and company = ?";
            list = executeSQLQuery( queryReceivePaymentWithRealisedGainLoss, params.toArray());
            if(list.size()>0){
                moduleId = Constants.Acc_Receive_Payment_ModuleId;
                return moduleId;
            }
            
            //If journal entry is of forex gain loss from credit note
            queryCreditNote = "select id from cndetails where linkedgainlossje =? and company = ?";
            list = executeSQLQuery( queryCreditNote, params.toArray());
            if(list.size()>0){
                moduleId = Constants.Acc_Credit_Note_ModuleId;
                return moduleId;
            }
            
            //If journal entry is of forex gain loss from debit note
            queryDebitNote = "select id from dndetails where linkedgainlossje =? and company = ?";
            list = executeSQLQuery( queryDebitNote, params.toArray());
            if(list.size()>0){
                moduleId = Constants.Acc_Debit_Note_ModuleId;
                return moduleId;
            }
            
        } catch (Exception e){
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("accJournalEntryImpl.getModuleIdForJournalEntry "+e.getMessage(), e);
        }
        return moduleId;
    }
    
    public KwlReturnObject activateDeactivateJournalEntry(String repeateid, boolean isactivate) throws ServiceException {
        RepeatedJE rje = null;
        try {
            rje = (RepeatedJE) get(RepeatedJE.class, repeateid);
            rje.setIsActivate(!isactivate);
        } catch (Exception e) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return new KwlReturnObject(true, "Recurring Journal Entry has been updated successfully.", null, null, 0);
    } 
    
    public KwlReturnObject approveRecurringJE(String repeateid, boolean ispendingapproval) throws ServiceException {
        RepeatedJE rje = null;
        try {
                rje = (RepeatedJE) get(RepeatedJE.class, repeateid);
                rje.setIspendingapproval(ispendingapproval);
        } catch (Exception e) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return new KwlReturnObject(true, "Recurring Journal Entry has been approved successfully.", null, null, 0);
    }
    
    public Object getUserObject(String id) throws ServiceException {
        Object obj = null;
        try {
            obj = get(User.class, id);
        } catch (Exception e) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return obj;
    }
    
    @Override
    public KwlReturnObject getEntryDateFromJEId(String documentId,String companyId) throws ServiceException{
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(documentId);
        params.add(companyId);
        String query = "select entrydate from journalentry where id = ? and company = ?";
        try {
            list = executeSQLQuery( query, params.toArray());
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getAllDetailsFromJEId:" + ex.getMessage(), ex);
        }               
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveRepeatePaymentChequeDetail(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            RepeatedPaymentChequeDetail RPchequeDetail = new RepeatedPaymentChequeDetail();
            if (dataMap.containsKey("id")) {
                RPchequeDetail = (RepeatedPaymentChequeDetail) get(RepeatedPaymentChequeDetail.class, (String) dataMap.get("id"));
            }
            if (dataMap.containsKey("RepeatedPaymentID")) {
                RPchequeDetail.setRepeatedPaymentID((String) dataMap.get("RepeatedPaymentID"));
            }
            if (dataMap.containsKey("no")) {
                RPchequeDetail.setCount((Integer) dataMap.get("no"));
            }
            if (dataMap.containsKey("date")) {
                RPchequeDetail.setChequeDate((Date) dataMap.get("date"));
            }
            if (dataMap.containsKey("chequenumber")) {
                RPchequeDetail.setChequeNumber((String) dataMap.get("chequenumber"));
            }
            saveOrUpdate(RPchequeDetail);
            list.add(RPchequeDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveRepeatePaymentChequeDetail : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getRepeatePaymentChequeDetail(HashMap<String, Object> requestParams) throws ServiceException {
        String id = (String) requestParams.get("repeatedPaymentID");
        int noOfMPpost = Integer.parseInt(requestParams.get("noOfMPRemainpost").toString());
        String query = "from RepeatedPaymentChequeDetail R where R.RepeatedPaymentID = ? and R.count = ? ";
        List list = executeQuery( query, new Object[]{id, noOfMPpost});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getChequeDetailsForRepeatedJE(String repeatJEId) throws ServiceException {
        List list = new ArrayList();
        String query = "From RepeatedJEChequeDetail R where R.RepeatedJEID=?" ;
        list = executeQuery( query, new Object[]{repeatJEId});
        return new KwlReturnObject(true, "", null, list,list.size());
    }
    
    @Override
    public int DelRepeateJEChequeDetails(String repeateid) throws ServiceException {
        String query = "delete from RepeatedJEChequeDetail R where R.RepeatedJEID=?" ;
        int numRows = executeUpdate( query, new Object[]{repeateid});
        return numRows;
    }
    
    @Override
    public KwlReturnObject saveRepeateJEChequeDetail(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            RepeatedJEChequeDetail RJEchequeDetail = new RepeatedJEChequeDetail();
            if (dataMap.containsKey("id")) {
                RJEchequeDetail = (RepeatedJEChequeDetail) get(RepeatedPaymentChequeDetail.class, (String) dataMap.get("id"));
            }
            if (dataMap.containsKey("RepeatedJEID")) {
                RJEchequeDetail.setRepeatedJEID((String) dataMap.get("RepeatedJEID"));
            }
            if (dataMap.containsKey("no")) {
                RJEchequeDetail.setCount((Integer) dataMap.get("no"));
            }
            if (dataMap.containsKey("date")) {
                RJEchequeDetail.setChequeDate((Date) dataMap.get("date"));
            }
            if (dataMap.containsKey("chequenumber")) {
                RJEchequeDetail.setChequeNumber((String) dataMap.get("chequenumber"));
            }
            saveOrUpdate(RJEchequeDetail);
            list.add(RJEchequeDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveRepeateJEChequeDetail : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getRepeateJEChequeDetail(HashMap<String, Object> requestParams) throws ServiceException {
        String id = (String) requestParams.get("repeatedJEID");
        int noOfJEpost = Integer.parseInt(requestParams.get("noOfJERemainpost").toString());
        String query = "from RepeatedJEChequeDetail R where R.RepeatedJEID = ? and R.count = ? ";
        List list = executeQuery( query, new Object[]{id, noOfJEpost});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
     public synchronized String UpdateJournalEntry(Map<String, Object> seqNumberMap) {
        String documnetNumber = "";
        try {
            documnetNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
            int seqNumber = 0;
            if(seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())){
               seqNumber= Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
            }
            String datePrefix = seqNumberMap.containsKey(Constants.DATEPREFIX) ? (String)seqNumberMap.get(Constants.DATEPREFIX) : "";
            String dateafterPrefix = seqNumberMap.containsKey(Constants.DATEAFTERPREFIX) ? (String)seqNumberMap.get(Constants.DATEAFTERPREFIX) : "";
            String dateSuffix = seqNumberMap.containsKey(Constants.DATESUFFIX) ? (String)seqNumberMap.get(Constants.DATESUFFIX) : "";
            String sequenceFormatID = seqNumberMap.containsKey(Constants.SEQUENCEFORMATID) ? (String)seqNumberMap.get(Constants.SEQUENCEFORMATID) : "";
            String documentID = seqNumberMap.containsKey(Constants.DOCUMENTID) ? (String)seqNumberMap.get(Constants.DOCUMENTID) : "";
            String companyID = seqNumberMap.containsKey(Constants.companyKey) ? (String)seqNumberMap.get(Constants.companyKey) : "";
            String query = "update JournalEntry set entryNumber = ?,seqnumber=?,datePreffixValue=?, dateAfterPreffixValue=?, dateSuffixValue=?,seqformat.ID=? where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{documnetNumber, seqNumber,datePrefix,dateafterPrefix,dateSuffix, sequenceFormatID, documentID, companyID});
        } catch (Exception e) {
            System.out.println(e);
        }
        return documnetNumber;
    }
    public KwlReturnObject saveRevenueJEInvoiceMapping(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            RevenueJEInvoiceMapping revenueJEInvoiceMapping = new RevenueJEInvoiceMapping();
            if (dataMap.containsKey("id")) {
                revenueJEInvoiceMapping = (RevenueJEInvoiceMapping) get(RevenueJEInvoiceMapping.class, (String) dataMap.get("id"));
            }
            if (dataMap.containsKey("invoiceId")) {
                revenueJEInvoiceMapping.setInvoiceId((String) dataMap.get("invoiceId"));
            }
            if (dataMap.containsKey("jeId")) {
                revenueJEInvoiceMapping.setJournalEntryId((String) dataMap.get("jeId"));
            }
            saveOrUpdate(revenueJEInvoiceMapping);
            list.add(revenueJEInvoiceMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveRevenueJEInvoiceMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
       @Override
    public KwlReturnObject DeleteBankUnReconciliationDetail(String jeID, String companyid) throws ServiceException {
        String query = "delete from BankUnreconciliationDetail where journalEntry.id=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{jeID, companyid});
        return new KwlReturnObject(true, "Bank UnReconciliation Detail has been deleted successfully.", null, null, numRows);
}
    
     @Override
    public KwlReturnObject DeleteBankReconciliationDetail(String jeID, String companyid) throws ServiceException {
        String query = "delete from BankReconciliationDetail where journalEntry.id=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{jeID, companyid});
        return new KwlReturnObject(true, "Bank Reconciliation Detail has been deleted successfully.", null, null, numRows);
    }
     
     @Override
     public KwlReturnObject getJEDetailsToIncludeInGSTReport(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList paramslist = new ArrayList();
        String Condition = "";
        String taxid = (String) requestParams.get("taxid");
        paramslist.add(taxid);
        paramslist.add(requestParams.get("companyid"));
        paramslist.add(true);
        paramslist.add(11);                  // approvestatuslevel = 11 i.e. only approved JE's
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
            Condition = " and journalEntry.entryDate >= ? and journalEntry.entryDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
        String ss = requestParams.containsKey("ss")?(String) requestParams.get("ss"):"";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"JED.gstapplied.name", "JED.journalEntry.entryNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramslist, ss, 2);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                Condition +=searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
                
          }
        String mySearchFilterString = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
             if (mySearchFilterString.contains("c.AccJECustomData")) {
                 mySearchFilterString = mySearchFilterString.replaceAll("c.AccJECustomData", "JED.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", " JED.accJEDetailCustomData");
            }
        }
        
        String query = "from JournalEntryDetail JED where JED.journalEntry.deleted = false and JED.gstapplied.ID =? and JED.journalEntry.company.companyID=? and JED.journalEntry.toIncludeInGSTReport = ? and JED.journalEntry.approvestatuslevel = ? " + Condition + mySearchFilterString;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
}
     
    @Override
    public int updateToNullRepeatedJEOfJournalEntry(String invoiceid, String repeateid) throws ServiceException {
        String query = "UPDATE journalentry SET repeateje=null WHERE id=? AND repeateje=?";
        int numRows = executeSQLUpdate( query, new Object[]{invoiceid, repeateid});
        return numRows;
    }
    
    @Override
    public int deleteRepeatedJE(String repeateid) throws ServiceException {
        String query ="DELETE FROM repeatedje WHERE id=?";
        int numRows = executeSQLUpdate( query, new Object[]{repeateid});
        return numRows;
    }
    
    @Override
    public KwlReturnObject getForeignGainLossJouranalEntryDetails(Map<String, Object> requestParam) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) requestParam.get(Constants.df);
            String startDate = (String)requestParam.get(Constants.REQ_startdate);
            String endDate = (String) requestParam.get(Constants.REQ_enddate);
            String accountid = (String) requestParam.get("accountid");
            
            String condition = " where company.companyID=? and journalEntry.deleted=false ";
            params.add((String) requestParam.get(Constants.companyKey));
            
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (journalEntry.entryDate >=? and journalEntry.entryDate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            
            if(!StringUtil.isNullOrEmpty(accountid)){
                String accountGroup = AccountingManager.getFilterInString(accountid);
                condition += " and account.ID in "+accountGroup;
            }
            String mySearchFilterString = "";
            if (requestParam.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParam.get(Constants.Acc_Search_Json))) {
                mySearchFilterString = StringUtil.getMySearchFilterString(requestParam, params);
                if (mySearchFilterString.contains("c.AccJECustomData")) {
                    mySearchFilterString = mySearchFilterString.replaceAll("c.AccJECustomData", "journalEntry.accBillInvCustomData");
                }
                if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                    mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", "accJEDetailCustomData");
                }
            }
            
            String orderby = " order by journalEntry.entryDate desc,journalEntry.entryNumber asc";

            String query = "from JournalEntryDetail " + condition + mySearchFilterString + orderby;
            list = executeQuery(query, params.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE( ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", "", list, count);
    }
    
    @Override
    public KwlReturnObject getReconciliationOfAccountOpeningTransactions(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        String query = "";
        String conditionss = "";
        String dateFilterON = "";
        ArrayList params = new ArrayList();
        String orderBy = "", accountid="", companyid="", ss="", billid="", innercondition = "";
        int dateFilter = 0;
        Date startDate=null, endDate=null;
        StringBuilder conditionBuildString=new StringBuilder();
        boolean isConcileReport=false, isExportReportRequest=false, isMaintainHistory=false, isReconciledHistoryDetails=false, isPayment=false; 
        try {
            //Read the arguments from HashMap
            if(requestParams.containsKey("ispayment")){
                isPayment = (Boolean)requestParams.get("ispayment");
            }
            if(requestParams.containsKey(Constants.companyid) && !StringUtil.isNullOrEmpty((String)requestParams.get(Constants.companyid))){
                companyid = (String)requestParams.get(Constants.companyid);
            }
            if(requestParams.containsKey("ss") && !StringUtil.isNullOrEmpty((String)requestParams.get("ss"))){
                ss = (String)requestParams.get("ss");
            }
            if(requestParams.containsKey("dateFilter") && requestParams.get("dateFilter")!=null){
                dateFilter = ((Integer)requestParams.get("dateFilter")).intValue();
            }
            if(requestParams.containsKey("accountid") && !StringUtil.isNullOrEmpty((String)requestParams.get("accountid"))){
                accountid = (String)requestParams.get("accountid");
                params.add(accountid);
            }
            if(requestParams.containsKey(Constants.REQ_startdate) && (Date)requestParams.get(Constants.REQ_startdate)!=null){
                startDate = (Date)requestParams.get(Constants.REQ_startdate);
                 if (conditionBuildString.length() > 0) {
                    conditionBuildString.append(" and ");
                }
                if (dateFilter == 0) {
                    conditionBuildString.append(" voucher.creationDate >= ? ");
                } else {
                    conditionBuildString.append(" brd.reconcileDate >= ? ");
                }
                params.add(startDate);
                
            }
            if (requestParams.containsKey(Constants.REQ_enddate) && (Date) requestParams.get(Constants.REQ_enddate) != null) {
                endDate = (Date) requestParams.get(Constants.REQ_enddate);

                if (conditionBuildString.length() > 0) {
                    conditionBuildString.append(" and ");
                }
                if (dateFilter == 0) {
                    conditionBuildString.append(" voucher.creationDate <= ? ");
                } else {
                    conditionBuildString.append(" brd.reconcileDate <= ? ");
                }
                params.add(endDate);
            }
            if(requestParams.containsKey("isConcileReport") && requestParams.get("isConcileReport")!=null){
                isConcileReport = (Boolean)requestParams.get("isConcileReport");
            }
            if(requestParams.containsKey("isExportReportRequest") && requestParams.get("isExportReportRequest")!=null){
                isExportReportRequest = (Boolean)requestParams.get("isExportReportRequest");
            }
            if(requestParams.containsKey("isMaintainHistory") && requestParams.get("isMaintainHistory")!=null){
                isMaintainHistory = (Boolean)requestParams.get("isMaintainHistory");
            }
            if(requestParams.containsKey("isReconciledHistoryDetails") && requestParams.get("isReconciledHistoryDetails")!=null){
                isReconciledHistoryDetails = (Boolean)requestParams.get("isReconciledHistoryDetails");
            }

            if((requestParams.containsKey("billid") && requestParams.get("billid")!=null) && isReconciledHistoryDetails){  //To Fetch the data based on Record ID
                billid = (String)requestParams.get("billid");                
                conditionss += " AND br.ID='"+billid+"'";                
                if(requestParams.containsKey("isdeleted") && requestParams.get("isdeleted")!=null && (String)requestParams.get("isdeleted")!=""){   //To Identify the type of Record while expanding it
                    innercondition += " AND bankReconciliation.deleted="+(String)requestParams.get("isdeleted");
                }
            }
            
            params.add(companyid);
            params.add(isPayment);
            dateFilterON=conditionBuildString.toString();
            if (dateFilter == 0) {  //Bank Reconciliation Report
                orderBy = " order by voucher.creationDate, voucher.transactionNumber ";
            } else {                //View Reconciliation Report
                orderBy = " order by brd.reconcileDate, voucher.transactionNumber ";
            }

            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = null;
                if (isConcileReport) {
                    searchcol = new String[]{"brd.accountnames", "voucher.transactionNumber"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                    StringUtil.insertParamSearchString(map);
                } else {
                    searchcol = new String[]{"voucher.transactionNumber"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                }
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                conditionss += searchQuery;
            }
            
            if (isExportReportRequest && isConcileReport) { //View Reconciliation PDF
                query = "select voucher, br, brd from AccountOpeningTransaction as voucher, BankReconciliationDetail brd inner join brd.bankReconciliation br where voucher.ID=brd.transactionID and voucher.payDetail.paymentMethod.account.ID=? and "+ dateFilterON +" and voucher.company.companyID=? and voucher.isPayment=? "
                        + conditionss + " and voucher.ID in (select b.transactionID from BankReconciliationDetail b where b.company.companyID = ? and b.isOpeningTransaction=true and moduleID=?) "+orderBy;
            } else if (isExportReportRequest && !isConcileReport && isMaintainHistory) { //Bank Reconciliation PDF
                query = "select voucher from AccountOpeningTransaction as voucher where voucher.payDetail.paymentMethod.account.ID=? and "+dateFilterON+ " and voucher.company.companyID=? and voucher.isPayment=? "
                        + conditionss + " and voucher.ID not in (select transactionID from BankReconciliationDetail where company.companyID = ? and isOpeningTransaction=true and moduleID=? AND (reconcileDate<=? OR bankReconciliation.clearanceDate<=?)) order by voucher.creationDate, voucher.transactionNumber ";
            } else if (isReconciledHistoryDetails) {   //Here we are fetching the data from 'BankReconciliationDetailHistory' Table. Other queries are fetching the data from 'BankReconciliationDetail'. So, we can't change the query
                query = "select voucher, br, brd from AccountOpeningTransaction as voucher, BankReconciliationDetailHistory brd inner join brd.bankReconciliation br where voucher.ID=brd.transactionID and voucher.payDetail.paymentMethod.account.ID=? and "+ dateFilterON +" and voucher.company.companyID=? and voucher.isPayment=? "
                        + conditionss + " and voucher.ID in (select b.transactionID from BankReconciliationDetailHistory b where b.company.companyID = ? and b.isOpeningTransaction=true and moduleID=?) "+orderBy;
            } else if (isConcileReport) {
                query = "select voucher, br, brd from AccountOpeningTransaction as voucher, BankReconciliationDetail brd inner join brd.bankReconciliation br where voucher.ID=brd.transactionID and voucher.payDetail.paymentMethod.account.ID=? and "+ dateFilterON +" and voucher.company.companyID=? and voucher.isPayment=? "
                        + conditionss + " and voucher.ID in (select b.transactionID from BankReconciliationDetail b where b.company.companyID = ? and b.isOpeningTransaction=true and moduleID=?) "+orderBy;
            } else {
                query = "select voucher from AccountOpeningTransaction as voucher where voucher.payDetail.paymentMethod.account.ID=? and "+dateFilterON+ " and voucher.company.companyID=? and voucher.isPayment=? "
                        + conditionss + " and voucher.ID not in (select transactionID from BankReconciliationDetail where company.companyID = ? and isOpeningTransaction=true and moduleID=?) order by voucher.creationDate, voucher.transactionNumber ";
            }
            params.add(companyid);
            params.add(Constants.Account_Opening_Transaction_ModuleId);
	    if (isExportReportRequest && !isConcileReport && isMaintainHistory) {
                params.add(endDate);
                params.add(endDate);
            }		
            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accJournalEntryImpl.getReconciliationOfAccountOpeningTransactions:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * Use to save the JournalEntryUpdateHistory during the execution of the thread when perpetual inventory is activated
     * @param params
     * @author Swapnil K.
     * @return true if JournalEntryUpdateHistory saved successfully
     */
    public boolean saveJournalEntryUpdateHistory(JSONObject params) {
        boolean success = true;
        try {
            JournalEntryUpdateHistory entryUpdateHistory = new JournalEntryUpdateHistory();
            if (params.has(COMPANYID) && !StringUtil.isNullOrEmpty(params.optString(COMPANYID, null))) {
                entryUpdateHistory.setCompany((Company) get(Company.class, params.optString(COMPANYID)));
            }
            if (params.has(JEID) && !StringUtil.isNullOrEmpty(params.optString(JEID, null))) {
                entryUpdateHistory.setJournalEntryID(params.optString(JEID, null));
            }
            if (params.has(JEDID) && !StringUtil.isNullOrEmpty(params.optString(JEDID, null))) {
                entryUpdateHistory.setJournalEntryDetailID(params.optString(JEDID, null));
            }
            if (params.has(TransactionID) && !StringUtil.isNullOrEmpty(params.optString(TransactionID, null))) {
                entryUpdateHistory.setTransactionID(params.optString(TransactionID));
            }
            if (params.has(TransactionModuleID) && !StringUtil.isNullOrEmpty(params.optString(TransactionModuleID, null))) {
                entryUpdateHistory.setTransactionModuleID(params.optInt(TransactionModuleID, 0));
            }
            if (params.has("oldAmountInBase") && !StringUtil.isNullOrEmpty(params.optString("oldAmountInBase", null))) {
                entryUpdateHistory.setOldAmountInBase(params.optDouble("oldAmountInBase", 0));
            }
            if (params.has("newAmountInBase") && !StringUtil.isNullOrEmpty(params.optString("newAmountInBase", null))) {
                entryUpdateHistory.setNewAmountInBase(params.optDouble("newAmountInBase", 0));
            }
            if (params.has("oldamount") && !StringUtil.isNullOrEmpty(params.optString("oldamount", null))) {
                entryUpdateHistory.setOldAmount(params.optDouble("oldamount", 0));
            }
            if (params.has("newamount") && !StringUtil.isNullOrEmpty(params.optString("newamount", null))) {
                entryUpdateHistory.setNewAmount(params.optDouble("newamount", 0));
            }
            if (params.has("exchangerate") && !StringUtil.isNullOrEmpty(params.optString("exchangerate", null))) {
                entryUpdateHistory.setExchangeRate(params.optDouble("exchangerate", 0));
            }
            if (params.has("updateDate") && !StringUtil.isNullOrEmpty(params.optString("updateDate", null))) {
                entryUpdateHistory.setUpdateDate((Date) params.get("updateDate"));
            }
            saveOrUpdate(entryUpdateHistory);
        } catch (JSONException | ServiceException ex) {
            success = false;
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex){
            success = false;
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }
    /**
     * Use to check whether the JE is VAT payment JE
     * @param params
     * @author Akshay Gujar
     * @return true if JE is VAT Payment JE
     */
     @Override
    public boolean checkIfJEisVatJE(JSONObject dataMap) throws ServiceException{ 
        boolean isVatJe = false;
        List list = new ArrayList();
        String query = "";
        List params = new ArrayList();
        try {
            String tablename = dataMap.getString("table");
            query = "from "+tablename+" ";
            String condition="";
            if(dataMap.has("jeid") && !StringUtil.isNullOrEmpty(dataMap.getString("jeid"))){
                String jeid = dataMap.getString("jeid");
                if(!StringUtil.isNullOrEmpty(condition)){
                    condition += " and ";
                }
                condition += " taxPaymentJE.ID = ? ";
                params.add(jeid);
            }
            if(dataMap.has("paymentid") && !StringUtil.isNullOrEmpty(dataMap.getString("paymentid"))){
                String paymentid = dataMap.getString("paymentid");
                if(!StringUtil.isNullOrEmpty(condition)){
                    condition += " and ";
                }
                condition += " taxMakePayment = ? ";
                params.add(paymentid);
            }
            if(dataMap.has("termtype") && !StringUtil.isNullOrEmpty(dataMap.getString("termtype"))){
                int termtype = Integer.parseInt(dataMap.getString("termtype"));
                if(!StringUtil.isNullOrEmpty(condition)){
                    condition += " and ";
                }
                condition += " term.termType = ? ";
                params.add(termtype);
            }
            if(dataMap.has("companyid") && !StringUtil.isNullOrEmpty(dataMap.getString("companyid"))){
                String companyid = dataMap.getString("companyid");
                if(!StringUtil.isNullOrEmpty(condition)){
                    condition += " and ";
                }
                condition += " term.company.companyID = ? ";
                params.add(companyid);
            }
            if(dataMap.has("istaxpaid") && !StringUtil.isNullOrEmpty(dataMap.getString("istaxpaid"))){
                int istaxpaid = Integer.parseInt(dataMap.getString("istaxpaid"));
                if(!StringUtil.isNullOrEmpty(condition)){
                    condition += " and ";
                }
                condition += " taxPaidFlag = ? ";
                params.add(istaxpaid);
            }
            if(!StringUtil.isNullOrEmpty(condition)){
                query += " where "+condition ;
            }
            list = executeQuery( query,params.toArray());
            if(list.size() > 0){
                isVatJe = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.getInvoicedetailTermMap:" + ex.getMessage(), ex);
        }
        return isVatJe;
    }
    
    /**
     *
     * @param requestParams
     * @return List of JournalEntryDetail for calculate TotalPricipleAmountForEClaimJE
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getJEDetailListToCalculatePrincipleAmount(Map<String, Object> requestParams) throws ServiceException {
        List jeDetail = null;
        try {
            ArrayList paramsList = new ArrayList();
            
            if (requestParams.containsKey(Constants.companyKey) && requestParams.get(Constants.companyKey) != null) {
                paramsList.add(requestParams.get(Constants.companyKey));
            }
            if (requestParams.containsKey(Constants.Acc_JEid) && requestParams.get(Constants.Acc_JEid) != null) {
                paramsList.add(requestParams.get(Constants.Acc_JEid));
            }
            
            String hqlQuery = "from JournalEntryDetail JED where JED.journalEntry.company.companyID = ? and JED.journalEntry.ID = ?  and JED.journalEntry.toIncludeInGSTReport = 'T' and JED.debit = 'T' and gstapplied IS NULL";
            jeDetail = executeQuery(hqlQuery, paramsList.toArray());
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getTotalPricipleAmountForEClaimJE:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, SS, Invoice, jeDetail, jeDetail.size());
    }
    
    /**
     * Use to get JE details id
     * @param params
     * @author Ashish Mohite
     * @return JE Details id list
     */
    public KwlReturnObject getJEDetailsID(Map<String, Object> params) throws ServiceException {
        List jeDetailsIdList = null;
        try {
            String jeno = "";
            String companyid = "";
            jeno = params.get("jeno") != null ? (String) params.get("jeno") : "";
            companyid = params.get("companyid") != null ? (String) params.get("companyid") : "";
            
            String query = "select jed.id from JournalEntryDetail jed  WHERE jed.journalEntry.entryNumber = ? and jed.company.companyID = ? ";
            jeDetailsIdList = executeQuery(query, new Object[]{jeno, companyid});
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getJEDetails:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, jeDetailsIdList, jeDetailsIdList.size());
    }
    
    public boolean isAdvanceSearchOnGlobalDimension(String searchJson) {
        boolean isAdvanceSearchOnGlobalDimension = false;
        try {
            JSONObject jobj = new JSONObject(searchJson);
            JSONArray rootArr = jobj.getJSONArray("root");
            JSONObject searchjobj = null;
            boolean isLineDimPresent = false;
            boolean isGlobalDimPresent = false;
            for (int i = 0; i < rootArr.length(); i++) {
                searchjobj = rootArr.getJSONObject(i);
                if(searchjobj.optBoolean("iscustomcolumndata")){
                   isLineDimPresent = true; 
                }else if(!searchjobj.optBoolean("iscustomcolumndata")){
                   isGlobalDimPresent = true;
                }
                if (isLineDimPresent && isGlobalDimPresent) {
                    break;
                }
            }
            if (!isLineDimPresent && isGlobalDimPresent) {
                isAdvanceSearchOnGlobalDimension = true;
            }

        } catch (JSONException ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isAdvanceSearchOnGlobalDimension;
    }

    
    @Override
    public KwlReturnObject getMasterItemByNameorID(String companyid, String value, String masterGroupID) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(masterGroupID);
            params.add(companyid);
            params.add(value);
            String query = "from MasterItem mst where  mst.masterGroup.ID=? and mst.company.companyID=? and value=?  ";
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.getCategorytByCategoryname", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getRoundingJournalEntryByGRIds(String grIDs, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            String grGroupIds = AccountingManager.getFilterInString(grIDs);
            ArrayList params = new ArrayList();
            params.add(4);//JE type 4 foyr for rounding JE
            params.add(companyid);
            String query = "from JournalEntry je where  je.typeValue=? and je.company.companyID=? and je.transactionId in " + grGroupIds;
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getTDSJEEntryMapping(String goodsReceipt, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(goodsReceipt);//JE type 4 foyr for rounding JE
            String query = "SELECT tdsjemapping from grdetails where goodsreceipt = ? AND tdsjemapping IS NOT NULL";
            list = executeSQLQuery(query, params.toArray());
            query = "SELECT tdsjemapping from expenseggrdetails where goodsreceipt = ? AND tdsjemapping IS NOT NULL";
            List listExpense = executeSQLQuery(query, params.toArray());
            list.addAll(listExpense);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getJEEntryFromMapping(String jeid, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(jeid);//JE type 4 foyr for rounding JE
            String query = "SELECT journalentry from tdsjemapping where id = ?";
            list = executeSQLQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public void deleteJournalEntryTDSMapping(String jeid, String companyid) throws ServiceException {
        try {
            ArrayList params = new ArrayList();
            params.add(jeid);
            
            String query = "UPDATE grdetails SET tdsjemapping = NULL where tdsjemapping=?";
            executeSQLUpdate(query, params.toArray());
            query = "UPDATE expenseggrdetails SET tdsjemapping = NULL where tdsjemapping=?";
            executeSQLUpdate(query, params.toArray());
            
            params.add(companyid);
            query = "DELETE FROM tdsjemapping where id =? AND company=?";
            executeSQLUpdate(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
    @Override
    public void permanentDeleteJournalEntryDetailTDSMapping(String jeid, String companyid) throws ServiceException {
        String query = "delete from jedetail where journalEntry =?";
        executeSQLUpdate(query, new Object[]{jeid});
    }
    
    @Override
    public void permanentDeleteJournalEntryTDSMapping(String jeid, String companyid) throws ServiceException {
        String delQuery = "delete from JournalEntry je where ID=? and je.company.companyID=?";
        executeUpdate( delQuery, new Object[]{jeid, companyid});
    }
    
    @Override
    public int saveCustomDataForReverseJE(String New_JE_ID, String Old_JE_ID, boolean JE_OR_JED) throws ServiceException {
        int NoOFRecords = 0;
        String query = "";
        String conditionSQL = "";
        String selectColumns = "";
        String tableName = "";
        String column = "";
        try {
            for (int i = Constants.Custom_Column_Combo_start + 1; i <= (Constants.Custom_Column_Combo_start + Constants.Custom_Column_Combo_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
}
            for (int i = Constants.Custom_Column_Master_start + 1; i <= (Constants.Custom_Column_Master_start + Constants.Custom_Column_Master_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }

            for (int i = Constants.Custom_Column_User_start + 1; i <= (Constants.Custom_Column_User_start + Constants.Custom_Column_User_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }

            for (int i = Constants.Custom_Column_Normal_start + 1; i <= (Constants.Custom_Column_Normal_start + Constants.Custom_Column_Normal_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            for (int i = Constants.Custom_Column_Check_start + 1; i <= (Constants.Custom_Column_Check_start + Constants.Custom_Column_Check_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            for (int i = Constants.Custom_Column_Date_start + 1; i <= (Constants.Custom_Column_Date_start + Constants.Custom_Column_Date_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            conditionSQL += "acc.company,acc.deleted,acc.moduleId";
            selectColumns += "company, deleted, moduleId";
            if (JE_OR_JED) {
                tableName = "accjecustomdata";
                column = "acc.journalentryId";
                selectColumns = "journalentryId," + selectColumns;
            }
            if (!JE_OR_JED) {
                tableName = "accjedetailcustomdata";
                column = "acc.jedetailId";
                conditionSQL += ",acc.recdetailId";
                selectColumns += ",recdetailId";

                selectColumns = "jedetailId," + selectColumns;
            }
            query += "insert into " + tableName + " (" + selectColumns + ") (select '" + New_JE_ID + "'," + conditionSQL + " from " + tableName + " as acc where " + column + "='" + Old_JE_ID + "')";
            NoOFRecords = executeSQLUpdate(query, new String[]{});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveCustomDataForReverseJE : " + ex.getMessage(), ex);
        }
        return NoOFRecords;
    }
    public KwlReturnObject getJournalEntryDetailsCustomDatabyJEIds(Map<String, Object> requestParams) throws ServiceException {
        
        String query = "";
        List params = new ArrayList();
        List list = new ArrayList();
        String condition =""; 
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);

            query = "select jedcd.id, jedcd from AccJEDetailCustomData jedcd ";
                   
            condition="where jedcd.jedetail.journalEntry.company.companyID=?";
            params.add(companyid);
            String jeIds = (String) requestParams.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and jedcd.jedetail.journalEntry.ID IN(" + jeIds + ")";
            }

            query=query+condition+" order by jedcd.jedetail.journalEntry.createdOn desc,jedcd.jedetail.debit desc";
            list = executeQuery(query, params.toArray());
            
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accJournalEntryImpl.getJournalEntryDetailsCustomDatabyJEIds:" + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getJournalEntryCustomDataByJEIds(Map<String, Object> requestParams) throws ServiceException {
        String query = "";
        List params = new ArrayList();
        List list = new ArrayList();
        String condition = "";
        list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            query = "select  jecd.journalentryId, jecd from AccJECustomData jecd ";
            condition = " where jecd.journalentry.company.companyID=?";
            params.add(companyid);
            String jeIds = (String) requestParams.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and jecd.journalentry.ID IN(" + jeIds + ")";
            }

            query = query + condition + " order by jecd.journalentry.createdOn desc";
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accJournalEntryImpl.getJournalEntryCustomDataByJEIds:" + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());
    }

    /**
     * This method is used to get the transaction i.e document number form
     * transaction id.It is called in AccReportsServiceImpl class in
     * getJournalEntryJsonMerged method.ERP-41455.
     *
     * @param params
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getTransactionNumber(HashMap<String, Object> params) throws ServiceException {
        List list = new ArrayList();
        String companyid = (params.containsKey(Constants.companyid) && params.get(Constants.companyid) != null) ? params.get(Constants.companyid).toString() : "";
        String transactionId = (params.containsKey("transactionId") && params.get("transactionId") != null) ? params.get("transactionId").toString() : "";
        try {
            List queryParams = new ArrayList();
            String query = " select paymentnumber as documentNumber, isopeningbalencepayment as isOpeningDocument from payment where id = ? and company=?  "
                    + " UNION  "
                    + " select receiptnumber as documentNumber, isopeningbalencereceipt as isOpeningDocument from receipt where id = ? and company=? "
                    + " UNION  "
                    + " select cnnumber as documentNumber, isopeningbalencecn as isOpeningDocument from creditnote where id = ? and company=? "
                    + " UNION  "
                    + " select dnnumber as documentNumber, isopeningbalencedn as isOpeningDocument from debitnote where id = ? and company=? "
                    + " UNION  "
                    + " select invoicenumber as documentNumber, isopeningbalenceinvoice as isOpeningDocument from invoice where id = ? and company=? "
                    + " UNION  "
                    + " select grnumber as documentNumber, isopeningbalenceinvoice as isOpeningDocument from goodsreceipt where id = ? and company=? ";

            //Payment
            queryParams.add(transactionId);
            queryParams.add(companyid);
            //Receipt
            queryParams.add(transactionId);
            queryParams.add(companyid);
            //CreditNote
            queryParams.add(transactionId);
            queryParams.add(companyid);
            //DebitNote
            queryParams.add(transactionId);
            queryParams.add(companyid);
            //sales invoice(Table invoice)
            queryParams.add(transactionId);
            queryParams.add(companyid);
            //Purchase Invoice(Table goodsreceipt)
            queryParams.add(transactionId);
            queryParams.add(companyid);

            list = executeSQLQuery(query, queryParams.toArray());
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(this.getClass().getName() + ".getTransactionNumber:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
}
