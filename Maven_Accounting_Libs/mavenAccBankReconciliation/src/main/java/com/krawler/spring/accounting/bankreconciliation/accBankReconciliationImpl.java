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
package com.krawler.spring.accounting.bankreconciliation;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class accBankReconciliationImpl extends BaseDAO implements accBankReconciliationDAO {

    public KwlReturnObject addBankReconciliation(HashMap<String, Object> brMap) throws ServiceException {
        List list = new ArrayList();
        try {
            BankReconciliation br = new BankReconciliation();
            br = buildBankReconciliation(br, brMap);
            save(br);
            list.add(br);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addBankReconciliation : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Bank Reconciliation has been added successfully", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject saveBankReconciliationDraft(JSONObject dataObj) throws ServiceException{
        List list = new ArrayList();
        SimpleDateFormat sdf = null;
        try {
            BankReconcilationDraft draft = new BankReconcilationDraft();
            String draftId = dataObj.has("draftId") ? dataObj.optString("draftId", "") : "";

            if (StringUtil.isNullOrEmpty(draftId)) {
                if (dataObj.has(Constants.useridKey) && dataObj.get(Constants.useridKey) != null) {
                    User user = (User) get(User.class, dataObj.getString(Constants.useridKey));
                    draft.setCreatedby(user);
                }

                if (dataObj.has("createdon")) {
                    draft.setCreatedon(dataObj.getLong("createdon"));
                }
                if (dataObj.has("updatedon")) {
                    draft.setUpdatedon(dataObj.getLong("updatedon"));
                }
            } else {
                draft = dataObj.get("draftId") == null ? null : (BankReconcilationDraft) get(BankReconcilationDraft.class, dataObj.getString("draftId"));
                if (dataObj.has("updatedon")) {
                    draft.setUpdatedon(dataObj.getLong("updatedon"));
                }
            }
            
            if (dataObj.has("description")) {
                draft.setDescription(dataObj.getString("description"));
            }
            if (dataObj.has("bankBookBalanceinAcc")) {
                draft.setBankBookBalanceinAcc(dataObj.optDouble("bankBookBalanceinAcc",0.0));
            }
            if (dataObj.has("bankStmtBalanceinAcc")) {
                draft.setBankStmtBalanceinAcc(dataObj.optDouble("bankStmtBalanceinAcc",0.0));
            }
            if (dataObj.has("clearedChecksAmountinAcc")) {
                draft.setClearedChecksAmountinAcc(dataObj.optDouble("clearedChecksAmountinAcc",0.0));
            }
            if (dataObj.has("clearedDepositsAmountinAcc")) {
                draft.setClearedDepositsAmountinAcc(dataObj.optDouble("clearedDepositsAmountinAcc",0.0));
            }
            if (dataObj.has("unclearedChecksAmountinAcc")) {
                draft.setUnclearedChecksAmountinAcc(dataObj.optDouble("unclearedChecksAmountinAcc",0.0));
            }
            if (dataObj.has("unclearedDepositsAmountinAcc")) {
                draft.setUnclearedDepositsAmountinAcc(dataObj.optDouble("unclearedDepositsAmountinAcc",0.0));
            }
            if (dataObj.has("newstatementbalance")) {
                draft.setNewstatementbalance(dataObj.optDouble("newstatementbalance",0.0));
            }
            if (dataObj.has("depositsreconciled")) {
                draft.setDepositsReconciled(dataObj.optInt("depositsreconciled",0));
            }
            if (dataObj.has("paymentsreconciled")) {
                draft.setPaymentsReconciled(dataObj.optInt("paymentsreconciled",0));
            }
            if (dataObj.has("clearingamount")) {
                draft.setClearingAmount(dataObj.optDouble("clearingamount",0.0));
            }
            if (!StringUtil.isNullOrEmpty(dataObj.optString("fromdate",""))) {
                sdf = new SimpleDateFormat(Constants.yyyyMMdd);
                draft.setFromdate(sdf.parse(dataObj.getString("fromdate")));
            }
            if (dataObj.has("todate")) {
                if (sdf == null) {
                    sdf = new SimpleDateFormat(Constants.yyyyMMdd);
                }
                draft.setTodate(sdf.parse(dataObj.getString("todate")));
            }
            if (dataObj.has(Constants.companyKey) && dataObj.get(Constants.companyKey) != null) {
                Company company = (Company) get(Company.class, dataObj.getString(Constants.companyKey));
                if (company != null) {
                    draft.setCompany(company);
                }
            }
            if (dataObj.has("accountid") && dataObj.get("accountid") != null) {
                Account account = (Account) get(Account.class, dataObj.getString("accountid"));
                if (account != null) {
                    draft.setAccount(account);
                }
            }
            saveOrUpdate(draft);
            list.add(draft);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveBankReconcilationDraft : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Draft Saved successfully", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject deleteBankReconciliationDraft(JSONObject paramJobj) throws ServiceException {
        String companyId = paramJobj.optString(Constants.companyKey,"");
        String accountId = paramJobj.optString("accountid","");
        String draftId = paramJobj.optString("draftId","");
        
        ArrayList params = new ArrayList();
        
        String query = "delete from BankReconcilationDraft where company.companyID=? and account.ID = ? ";
        params.add(companyId);
        params.add(accountId);
        
//        if (!StringUtil.isNullOrEmpty(draftId)) {
//            query += " and ID != ? ";
//            params.add(draftId);
//        }
            
        int numRows = executeUpdate(query, params.toArray());
        return new KwlReturnObject(true, "Bank Reconciliation Draft has been deleted successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject updateDraftedJournalEntries(JSONObject paramJobj) throws ServiceException {
        String companyId = paramJobj.optString(Constants.companyKey, "");
        String jeIds = paramJobj.optString("jeIds", "");
        String draftId = paramJobj.optString("draftId", null);
        String oldDraftId = paramJobj.optString("oldDraftId", "");

        ArrayList params = new ArrayList();

        String query = "update journalentry je set reconciliationdraft = ? ";
        params.add(draftId);

        String conditionQuery = " where je.company = ? ";
        params.add(companyId);

        if (!StringUtil.isNullOrEmpty(jeIds)) {
            conditionQuery += " and id in (" + jeIds + ") ";
        }

        if (!StringUtil.isNullOrEmpty(oldDraftId)) {
            conditionQuery += " and reconciliationdraft = ? ";
            params.add(oldDraftId);
        }


//        if (!StringUtil.isNullOrEmpty(draftId)) {
//            query += " and ID != ? ";
//            params.add(draftId);
//        }

        int numRows = executeSQLUpdate(query + conditionQuery, params.toArray());
        return new KwlReturnObject(true, "Bank Reconciliation Draft has been deleted successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject getDraftedJournalEntries(JSONObject dataObj) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        int totalCount = 0;
        try {
            String selectQuery = " select GROUP_CONCAT(je.id) as jeid from journalentry je  ";

            String conditionQuery = " where je.company = ? and je.reconciliationdraft = ? ";
            params.add(dataObj.optString(Constants.companyKey, ""));
            params.add(dataObj.optString("draftId", ""));


            String sqlQuery = selectQuery + conditionQuery;

            list = executeSQLQuery(sqlQuery, params.toArray());
            totalCount = list.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("getBankReconcilationDrafts : " + e.getMessage(), e);
        }

        return new KwlReturnObject(true, null, null, list, totalCount);
    }
    
    public KwlReturnObject getBankReconcilationDrafts(JSONObject dataObj) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        int totalCount = 0;
        try {
            String selectQuery = " from BankReconcilationDraft bd  ";

            String conditionQuery = " where bd.company.companyID = ?";
            params.add(dataObj.optString(Constants.companyKey, ""));

            if (dataObj.has("accountid")) {
                conditionQuery += " and bd.account.ID = ? ";
                params.add(dataObj.optString("accountid", ""));
            }


            String sqlQuery = selectQuery + conditionQuery;

            list = executeQuery(sqlQuery, params.toArray());
            totalCount = list.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("getBankReconcilationDrafts : " + e.getMessage(), e);
        }

        return new KwlReturnObject(true, null, null, list, totalCount);
    }

    //This method never used in project
//    public KwlReturnObject updateBankReconciliation(HashMap<String, Object> brMap) throws ServiceException {
//        List list = new ArrayList();
//        try {
//            String id = (String) brMap.get("id");
//            BankReconciliation br = (BankReconciliation) get(BankReconciliation.class, id);
//            if (br != null) {
//                br = buildBankReconciliation(br, brMap);
//                saveOrUpdate(br);
//            }
//            list.add(br);
//        } catch (Exception e) {
//            throw ServiceException.FAILURE("updateBankReconciliation : " + e.getMessage(), e);
//        }
//        return new KwlReturnObject(true, "Bank Reconciliation has been updated successfully", null, list, list.size());
//    }
    
    public BankReconciliation buildBankReconciliation(BankReconciliation br, HashMap<String, Object> brMap) {
        if (brMap.containsKey("startdate")&& brMap.get("startdate")!=null) {
            br.setStartDate((Date) brMap.get("startdate"));
        }
        if (brMap.containsKey("clearanceDate")&& brMap.get("clearanceDate")!=null) {
            br.setClearanceDate((Date) brMap.get("clearanceDate"));
        }
        if (brMap.containsKey("enddate")&& brMap.get("enddate")!=null) {
            br.setEndDate((Date) brMap.get("enddate"));
        }
        if (brMap.containsKey("clearingamount")&& brMap.get("clearingamount")!=null) {
            br.setClearingAmount((Double) brMap.get("clearingamount"));
        }
        if (brMap.containsKey("checkCount")&& brMap.get("checkCount")!=null) {
            br.setCheckCount((Integer) brMap.get("checkCount"));
        }
        if (brMap.containsKey("depositeCount")&& brMap.get("depositeCount")!=null) {
            br.setDepositeCount((Integer) brMap.get("depositeCount"));
        }
        if (brMap.containsKey("isUnreconsile")&& brMap.get("isUnreconsile")!=null) {
            br.setDeleted((Boolean) brMap.get("isUnreconsile"));
        }
        if (brMap.containsKey("accountid")) {
            Account account = brMap.get("accountid") == null ? null : (Account) get(Account.class, (String) brMap.get("accountid"));
            br.setAccount(account);
        }
        if (brMap.containsKey("companyid")) {
            Company company = brMap.get("companyid") == null ? null : (Company) get(Company.class, (String) brMap.get("companyid"));
            br.setCompany(company);
        }
        if (brMap.containsKey("brdetails")&& brMap.get("brdetails")!=null) {
            br.setDetails((Set<BankReconciliationDetail>) brMap.get("brdetails"));
        }
        if (brMap.containsKey("createdby")&& brMap.get("createdby")!=null) {
            User createdby = brMap.get("createdby") == null ? null : (User) get(User.class, (String) brMap.get("createdby"));
            br.setCreatedby(createdby);
        }
        if (brMap.containsKey("burdetails")&& brMap.get("burdetails")!=null) {
            br.setUnreconcilationdetails((Set<BankUnreconciliationDetail>) brMap.get("burdetails"));
        }
        if (brMap.containsKey("reconcilenumber")&& brMap.get("reconcilenumber")!=null) {
            br.setReconcilenumber((String)brMap.get("reconcilenumber"));
        }
        if (brMap.containsKey("bankBookBalanceinAcc")&& brMap.get("bankBookBalanceinAcc")!=null) {
            br.setBankBookBalanceinAcc((Double) brMap.get("bankBookBalanceinAcc"));
        }
        if (brMap.containsKey("clearedChecksAmountinAcc")&& brMap.get("clearedChecksAmountinAcc")!=null) {
            br.setClearedChecksAmountinAcc((Double) brMap.get("clearedChecksAmountinAcc"));
        }
        if (brMap.containsKey("clearedDepositsAmountinAcc")&& brMap.get("clearedDepositsAmountinAcc")!=null) {
            br.setClearedDepositsAmountinAcc((Double) brMap.get("clearedDepositsAmountinAcc"));
        }
        if (brMap.containsKey("bankStmtBalanceinAcc")&& brMap.get("bankStmtBalanceinAcc")!=null) {
            br.setBankStmtBalanceinAcc((Double) brMap.get("bankStmtBalanceinAcc"));
        }
        if (brMap.containsKey("datesuffixvalue")) {
            br.setDateSuffixValue((String) brMap.get("datesuffixvalue"));
        }
        if (brMap.containsKey("datepreffixvalue")) {
            br.setDatePreffixValue((String) brMap.get("datepreffixvalue"));
        }
        if (brMap.containsKey("dateafterpreffixvalue")) {
            br.setDateAfterPreffixValue((String) brMap.get("dateafterpreffixvalue"));
        }
        if (brMap.containsKey("autogen")) {
            br.setAutoGenerated((Boolean) brMap.get("autogen"));
        }
        if (brMap.containsKey("seqnumber")) {
            br.setSeqnumber((Integer) brMap.get("seqnumber"));
        }
        if (brMap.containsKey("seqformat")) {
            SequenceFormat format = brMap.get("seqformat") == null ? null : (SequenceFormat) get(SequenceFormat.class, (String) brMap.get("seqformat"));
            br.setSeqformat(format);
        }
        if (brMap.containsKey("createdon")&& brMap.get("createdon")!=null) {
            br.setCreatedon((Long) brMap.get("createdon"));
        }
        return br;
    }

    public KwlReturnObject addBankReconciliationDetail(HashMap<String, Object> brdMap) throws ServiceException {
        List list = new ArrayList();
        try {
            BankReconciliationDetail brd = new BankReconciliationDetail();
            brd = buildBankReconciliationDetail(brd, brdMap);
            save(brd);
            list.add(brd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addBankReconciliationDetail : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Bank Reconciliation Detail has been added successfully", null, list, list.size());
    }
   
    public KwlReturnObject updateBankReconciliationDetail(HashMap<String, Object> brdMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = (String) brdMap.get("id");
            BankReconciliationDetail brd = (BankReconciliationDetail) get(BankReconciliationDetail.class, id);
            if (brd != null) {
                brd = buildBankReconciliationDetail(brd, brdMap);
                saveOrUpdate(brd);
            }
            list.add(brd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateBankReconciliationDetail : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Bank Reconciliation Detail has been updated successfully", null, list, list.size());
    }

    public BankReconciliationDetail buildBankReconciliationDetail(BankReconciliationDetail brd, HashMap<String, Object> brdMap) {
        if (brdMap.containsKey("amount")) {
            brd.setAmount((Double) brdMap.get("amount"));
        }
        if (brdMap.containsKey("accountname")) {
            brd.setAccountnames((String) brdMap.get("accountname"));
        }
        if (brdMap.containsKey("transactionID")) {
            brd.setTransactionID((String) brdMap.get("transactionID"));
        }
        if (brdMap.containsKey("moduleID")) {
            brd.setModuleID((Integer) brdMap.get("moduleID"));
        }
        if (brdMap.containsKey("isOpeningTransaction")) {
            brd.setIsOpeningTransaction((Boolean) brdMap.get("isOpeningTransaction"));
        }
        if (brdMap.containsKey("debit")) {
            brd.setDebit((Boolean) brdMap.get("debit"));
        }
        if (brdMap.containsKey("reconcileDate")) {
            brd.setReconcileDate((Date)brdMap.get("reconcileDate"));
        }
        if (brdMap.containsKey("brid")) {
            BankReconciliation br = brdMap.get("brid") == null ? null : (BankReconciliation) get(BankReconciliation.class, (String) brdMap.get("brid"));
            brd.setBankReconciliation(br);
        }
        if (brdMap.containsKey("jeid")) {
            JournalEntry je = brdMap.get("jeid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) brdMap.get("jeid"));
            brd.setJournalEntry(je);
        }
        if (brdMap.containsKey("companyid")) {
            Company company = brdMap.get("companyid") == null ? null : (Company) get(Company.class, (String) brdMap.get("companyid"));
            brd.setCompany(company);
        }
        return brd;
    }
    
    public KwlReturnObject addBankReconciliationDetailsHistory(HashMap<String, Object> brdMap) throws ServiceException {
        List list = new ArrayList();
        try {
            BankReconciliationDetailHistory brd = new BankReconciliationDetailHistory();
            brd = buildBankReconciliationDetailsHistory(brd, brdMap);
            save(brd);
            list.add(brd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addBankReconciliationDetail : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Bank Reconciliation Detail has been added successfully", null, list, list.size());
    }
    
    public BankReconciliationDetailHistory buildBankReconciliationDetailsHistory(BankReconciliationDetailHistory brd, HashMap<String, Object> brdMap) {
        if (brdMap.containsKey("companyid")) {
            Company company = brdMap.get("companyid") == null ? null : (Company) get(Company.class, (String) brdMap.get("companyid"));
            brd.setCompany(company);
        }
        if (brdMap.containsKey("brid")) {
            BankReconciliation br = brdMap.get("brid") == null ? null : (BankReconciliation) get(BankReconciliation.class, (String) brdMap.get("brid"));
            brd.setBankReconciliation(br);
        }        
        if (brdMap.containsKey("reconcileDate")) {
            brd.setReconciledate(brdMap.get("reconcileDate")!=null ? (Date) brdMap.get("reconcileDate") : null);
        }
        if (brdMap.containsKey("date")) {
            brd.setDate(brdMap.get("date")!=null ? (Date) brdMap.get("date") : null);
        }
        if (brdMap.containsKey("accountname")) {
            brd.setAccountname(brdMap.get("accountname")!=null ? (String) brdMap.get("accountname") : "");
        }
        if (brdMap.containsKey("paidto")) {
            brd.setPaidto(brdMap.get("paidto")!=null ? (String) brdMap.get("paidto") : "");
        }
        if (brdMap.containsKey("chequeno")) {
            brd.setChequeno(brdMap.get("chequeno")!=null ? (String) brdMap.get("chequeno") : "");
        }
        if (brdMap.containsKey("chequedate")) {
            brd.setChequedate(brdMap.get("chequedate")!=null ? (Date) brdMap.get("chequedate") : null);
        }
        if (brdMap.containsKey("description")) {
            brd.setDescription(brdMap.get("description")!=null ? (String) brdMap.get("description") : "");
        }
        if (brdMap.containsKey("entryno")) {
            brd.setEntryno(brdMap.get("entryno")!=null ? (String) brdMap.get("entryno") : "");
        }
        if (brdMap.containsKey("jeid")) {
            brd.setJeid(brdMap.get("jeid")!=null ? (String) brdMap.get("jeid") : "");
        }
        if (brdMap.containsKey("transactionID")) {
            brd.setTransactionid(brdMap.get("transactionID")!=null ? (String) brdMap.get("transactionID") : "");
        }
        if (brdMap.containsKey("transactionNumber")) {
            brd.setTransactionNumber(brdMap.get("transactionNumber")!=null ? (String) brdMap.get("transactionNumber") : "");
        }
        if (brdMap.containsKey("transcurrsymbol")) {
            brd.setTranscurrsymbol(brdMap.get("transcurrsymbol")!=null ? (String) brdMap.get("transcurrsymbol") : "");
        }
        if (brdMap.containsKey("amountintransactioncurrency")) {
            brd.setAmountintransactioncurrency(brdMap.get("amountintransactioncurrency")!=null ? (Double) brdMap.get("amountintransactioncurrency") : 0);
        }
        if (brdMap.containsKey("accountcurrencysymbol")) {
            brd.setAccountcurrencysymbol(brdMap.get("accountcurrencysymbol")!=null ? (String) brdMap.get("accountcurrencysymbol") : "");
        }
        if (brdMap.containsKey("amountinacc")) {
            brd.setAmountinacc(brdMap.get("amountinacc")!=null ? (Double) brdMap.get("amountinacc") : 0);
        }        
        if (brdMap.containsKey("amount")) {
            brd.setAmount(brdMap.get("amount")!=null ? (Double) brdMap.get("amount") : 0);
        }
        if (brdMap.containsKey("clearedstatus")) {
            brd.setClearedstatus((Integer) brdMap.get("clearedstatus"));
        }
        if (brdMap.containsKey("reportname")) {
            brd.setReportname(brdMap.get("reportname")!=null ? (String) brdMap.get("reportname") : "");
        }
        if (brdMap.containsKey("debit")) {
            brd.setDebit((Boolean) brdMap.get("debit"));
        }
        if (brdMap.containsKey("moduleID")) {
            brd.setModuleid((Integer) brdMap.get("moduleID"));
        }
        if (brdMap.containsKey("isOpeningTransaction")) {
            brd.setIsopeningtransaction((Boolean) brdMap.get("isOpeningTransaction"));
        }
        return brd;
    }
    
    public KwlReturnObject getBankReconciliationHistoryRows(HashMap<String, Object> rowmap) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            String companyid = "", brid = "";
            Date startdate = null, enddate = null;
            String query = "FROM BankReconciliationDetailHistory ";
            if (rowmap.containsKey("company") && rowmap.get("company") != null) {
                companyid = (String) rowmap.get("company");                
                query += " WHERE company.companyID=? ";
                params.add(companyid);
            }
            if (rowmap.containsKey("brid") && rowmap.get("brid") != null) {
                brid = (String) rowmap.get("brid");
                query += " AND bankReconciliation.ID=? ";
                params.add(brid);
            }
            if (rowmap.containsKey("startdate") && rowmap.get("startdate") != null) {
                startdate = (Date) rowmap.get("startdate");
                query += " AND bankReconciliation.clearanceDate>=?";
                params.add(startdate);
            }
            if (rowmap.containsKey("enddate") && rowmap.get("enddate") != null) {
                enddate = (Date) rowmap.get("enddate");
                query += " AND bankReconciliation.clearanceDate<=?";
                params.add(enddate);
            }
            query += " ORDER BY clearedstatus";
            list = executeQuery(query, params.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("getBankReconciliationHistoryRows : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject addBankUnreconciliationDetail(HashMap<String, Object> brdMap) throws ServiceException {
        List list = new ArrayList();
        try {
            BankUnreconciliationDetail burd = new BankUnreconciliationDetail();
            burd = buildBankUnreconciliationDetail(burd, brdMap);
            save(burd);
            list.add(burd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addBankUnreconciliationDetail : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Bank Reconciliation Detail has been deleted successfully", null, list, list.size());
    }
    
    public BankUnreconciliationDetail buildBankUnreconciliationDetail(BankUnreconciliationDetail burd, HashMap<String, Object> brdMap) {
        if (brdMap.containsKey("amount")) {
            burd.setAmount((Double) brdMap.get("amount"));
        }
        if (brdMap.containsKey("accountname")) {
            burd.setAccountnames((String) brdMap.get("accountname"));
        }
        if (brdMap.containsKey("debit")) {
            burd.setDebit((Boolean) brdMap.get("debit"));
        }
        if (brdMap.containsKey("unreconcileDate")) {
            burd.setUnreconcileDate((Date)brdMap.get("unreconcileDate"));
        }
        if (brdMap.containsKey("transactionID")) {
            burd.setTransactionID((String) brdMap.get("transactionID"));
        }
        if (brdMap.containsKey("moduleID")) {
            burd.setModuleID((Integer) brdMap.get("moduleID"));
        }
        if (brdMap.containsKey("isOpeningTransaction")) {
            burd.setIsOpeningTransaction((Boolean) brdMap.get("isOpeningTransaction"));
        }
        if (brdMap.containsKey("brid")) {
            BankReconciliation br = brdMap.get("brid") == null ? null : (BankReconciliation) get(BankReconciliation.class, (String) brdMap.get("brid"));
            burd.setBankReconciliation(br);
        }
        if (brdMap.containsKey("jeid")) {
            JournalEntry je = brdMap.get("jeid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) brdMap.get("jeid"));
            burd.setJournalEntry(je);
        }
        if (brdMap.containsKey("companyid")) {
            Company company = brdMap.get("companyid") == null ? null : (Company) get(Company.class, (String) brdMap.get("companyid"));
            burd.setCompany(company);
        }
        return burd;
    }
    
    public KwlReturnObject getBRfromJE(String jeid, String companyid, boolean ignoreDeleted) throws ServiceException {
        List list = new ArrayList();
        String q = "";
        if (ignoreDeleted) {
            q = "from BankReconciliationDetail where journalEntry.ID=? and company.companyID=?";
        } else {
            q = "from BankReconciliationDetail where journalEntry.ID=? and bankReconciliation.deleted=false and company.companyID=?";
        }
        list = executeQuery( q, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getBRWithoutJE(String billid, String companyid, int moduleid) throws ServiceException {
        List list = new ArrayList();
        String q = "from BankReconciliationDetail where transactionID=? and moduleID=? and bankReconciliation.deleted=false and company.companyID=?";
        list = executeQuery(q, new Object[]{billid, moduleid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getBankUnReconsiledfromJE(String jeid, String companyid, boolean ignoreDeleted) throws ServiceException {
        List list = new ArrayList();
        String q = "";
        if (ignoreDeleted) {
            q = "from BankUnreconciliationDetail where journalEntry.ID=? and company.companyID=?";
        } else {
            q = "from BankUnreconciliationDetail where journalEntry.ID=? and bankReconciliation.deleted=false and company.companyID=?";
        }
        list = executeQuery( q, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getBankUnReconsiledWithoutJE(String billid, String companyid, int moduleid) throws ServiceException {
        List list = new ArrayList();
        String q = "from BankUnreconciliationDetail where transactionID=? and moduleID=? and bankReconciliation.deleted=false and company.companyID=?";
        list = executeQuery(q, new Object[]{billid, moduleid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBankReconciliation(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from BankReconciliationDetail brd where brd.bankReconciliation.deleted=false ";

        if (filterParams.containsKey("jeid")) {
            condition += " and brd.journalEntry.ID=?";
            params.add(filterParams.get("jeid"));
        }
        if (filterParams.containsKey("accountid")) {
            condition += " and brd.bankReconciliation.account.ID=?";
            params.add(filterParams.get("accountid"));
        }
        if (filterParams.containsKey("companyid")) {
            condition += " and brd.company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        query += condition;
//        query += " and journalEntry.deleted = false";
//        query="from BankReconciliationDetail where journalEntry.ID=? and bankReconciliation.deleted=false and company.companyID=?";
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    public KwlReturnObject getBankReconciliationHistory(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();        
        String condition = "", companyid = "", accid = "", billid = "", action="";
        Boolean deleted = false;
        try{
        ArrayList params = new ArrayList();
        String query = "from BankReconciliation";
        Date startDate = null, endDate = null;
        if(requestParams.containsKey(Constants.companyKey) && !StringUtil.isNullOrEmpty((String)requestParams.get(Constants.companyKey))){
            companyid = (String) requestParams.get(Constants.companyKey);
            condition += " where company.companyID=? ";            
            params.add(companyid);
        }
        if(requestParams.containsKey("billid") && !StringUtil.isNullOrEmpty((String)requestParams.get("billid"))){
            billid = (String) requestParams.get("billid");
            condition += " AND ID=? ";
            params.add(billid);
        }
        if(requestParams.containsKey(Constants.Acc_Accountid) && !StringUtil.isNullOrEmpty((String)requestParams.get(Constants.Acc_Accountid))){
            accid = (String) requestParams.get(Constants.Acc_Accountid);
            condition += " AND account.ID=? ";
            params.add(accid);
        }
//        if(requestParams.containsKey("action") && ((Boolean)requestParams.get("action"))!=null){      //ERP-34531
//            deleted = (Boolean) requestParams.get("action");
//            condition += " AND deleted=? ";
//            params.add(deleted);   //Currently, we are showing only Reconciled Records in Reconciled Details Report. We are not sending this key from other Reports.
//        }
        if(requestParams.containsKey("startdate") && requestParams.get("startdate")!=null){
            startDate = (Date) requestParams.get("startdate");
            condition += " AND clearanceDate>=? ";
            params.add(startDate);
        }
        if(requestParams.containsKey("enddate") && requestParams.get("enddate")!=null){
            endDate = (Date) requestParams.get("enddate");
            condition += " AND clearanceDate<=? ";
            params.add(endDate);
        }
        query += condition;
        returnList = executeQuery( query, params.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("getBankReconciliationHistory : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    //This method checks whether the record exist with new Reconcile Number or not.
    @Override 
    public KwlReturnObject searchReconcileNo(String reconcilenumber, String companyId, boolean isConcileReport) throws ServiceException {
        try {
            List list = new ArrayList();
            int count = 0;
            String q = "FROM BankReconciliation br WHERE br.reconcilenumber=? AND br.company.companyID=? AND br.deleted=?";
            list = executeQuery(q, new Object[]{reconcilenumber, companyId, isConcileReport});
            count = list.size();
            return new KwlReturnObject(true, "", null, list, count);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("searchReconcileNo : " + ex.getMessage(), ex);
        }
    }
    
    @Override
    public KwlReturnObject updateBankReconciliation(BankReconciliation br) throws ServiceException {
        List list = new ArrayList();
        try {
            if (br != null) {
                saveOrUpdate(br);
                list.add(br);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateBankReconciliation : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Bank Reconciliation has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject deleteBankReconciliation(String brid, String companyid) throws ServiceException {
        BankReconciliation br = (BankReconciliation) get(BankReconciliation.class, brid);
        br.setDeleted(true);
        saveOrUpdate(br);
        return new KwlReturnObject(true, "Bank Reconciliation has been deleted successfully.", null, null, 1);
    }

    @Override
    public KwlReturnObject permenantDeleteBankReconciliation(String brid, String companyid) throws ServiceException {
        String query = "delete from BankReconciliation where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{brid, companyid});
        return new KwlReturnObject(true, "Bank Reconciliation has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject permenantDeleteBankReconciliationDetail(String brdid, String companyid) throws ServiceException {
        String query = "delete from BankReconciliationDetail where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{brdid, companyid});
        return new KwlReturnObject(true, "Bank Reconciliation Detail has been deleted successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject permenantDeleteBankUnReconciliationDetail(String brdid, String companyid) throws ServiceException {
        String query = "delete from BankUnreconciliationDetail where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{brdid, companyid});
        return new KwlReturnObject(true, "Bank UnReconciliation Detail has been deleted successfully.", null, null, numRows);
    }
    
     @Override
    public KwlReturnObject permenantDeleteBankReconciliationDetailUsingJE(String jeID, String companyid) throws ServiceException {
        String query = "delete from BankReconciliationDetail where journalEntry.id=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{jeID, companyid});
        return new KwlReturnObject(true, "Bank Reconciliation Detail has been deleted successfully.", null, null, numRows);
    }
    
     @Override
    public KwlReturnObject permenantDeleteBankUnReconciliationDetailUsingJE(String jeID, String companyid) throws ServiceException {
        String query = "delete from BankUnreconciliationDetail where journalEntry.id=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{jeID, companyid});
        return new KwlReturnObject(true, "Bank UnReconciliation Detail has been deleted successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject saveBankReconcilationDocuments(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            if (dataMap.containsKey("BankReconcilationDocuments")) {
                BankReconcilationDocuments document = (BankReconcilationDocuments) dataMap.get("BankReconcilationDocuments");
                saveOrUpdate(document);
                list.add(document);
            }
            if (dataMap.containsKey("BankReconcilationDocumentMapping")) {
                BankReconcilationDocumentCompMap documentCompMap = (BankReconcilationDocumentCompMap) dataMap.get("BankReconcilationDocumentMapping");
                saveOrUpdate(documentCompMap);
                list.add(documentCompMap);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveBankReconcilationDocuments : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getBankReconcilationDocuments(HashMap<String, Object> dataMap) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String start = (String) dataMap.get(Constants.start);
            String limit = (String) dataMap.get(Constants.limit);

            ArrayList params = new ArrayList();

            params.add((String) dataMap.get(Constants.companyKey));

            String conditionSQL = " where bankreconciliationdoccompmap.company=?";

            String invoiceId = (String) dataMap.get("reconcileID");
            if (!StringUtil.isNullOrEmpty(invoiceId)) {
                params.add(invoiceId);
                conditionSQL += " and bankreconciliationdoccompmap.reconcileid=?";
            }

            String mysqlQuery = "select bankreconciliationdocuments.docname as docname,bankreconciliationdocuments.doctypeid as doctypeid,bankreconciliationdocuments.docid as docid "
                    + "from bankreconciliationdoccompmap inner join bankreconciliationdocuments on bankreconciliationdoccompmap.documentid=bankreconciliationdocuments.id " + conditionSQL;

            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accBankReconciliationImpl.getinvoiceDocuments:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject deleteBankReconcilationDocument(String documentID) throws ServiceException {
        List list = null;
        int numRows = 0;

        String query = "from BankReconcilationDocuments brdoc where brdoc.docID=?";
        list = executeQuery( query, new Object[]{documentID});

        if (!list.isEmpty()) {
            query = "delete from BankReconcilationDocumentCompMap brdocmap where brdocmap.document=?";
            numRows = executeUpdate( query, new Object[]{list.get(0)});
            delete(list.get(0));
            return new KwlReturnObject(true, "Document has been deleted successfully.", null, null, numRows);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    
    public KwlReturnObject updateBankReconciliationDocuments(String docID,String brID) throws ServiceException {
        String query = "update bankreconciliationdoccompmap brdm inner join bankreconciliationdocuments brd on brdm.documentid=brd.id set brdm.reconcileid=? where brd.docid=?";
        int numRows = executeSQLUpdate( query, new Object[]{brID,docID});
        return new KwlReturnObject(true, "Bank Reconciliation Detail has been deleted successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject getMachingRecordsForReconciliation(String companyid, String accountid, Date startDate, Date endDate, double minamount, double maxamount) throws ServiceException {
        List list = new ArrayList();
        String query = "";
        ArrayList params = new ArrayList();
        try {
            params.add(accountid);
            params.add(startDate);
            params.add(endDate);
            params.add(companyid);
            params.add(minamount);
            params.add(maxamount);

            query = "select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and je.entryDate >= ? and je.entryDate <= ? and ac.company.companyID=? and je.deleted=false and jed.amount >= ? and jed.amount <= ?"
                    +" and jed.journalEntry.ID not in (select journalEntry.ID from BankReconciliationDetail where bankReconciliation.deleted=false and company.companyID=? and isOpeningTransaction=false) order by je.entryDate, je.entryNumber";

            params.add(companyid);

            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accBankReconciliationImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accBankReconciliationImpl.getMachingRecordsForReconciliation:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getMachingOpeningBalancesForReconciliation(String companyid, String accountid, Date startDate, Date endDate, double minamount, double maxamount, boolean isReceipt) throws ServiceException {
        List list = new ArrayList();
        String query = "";
        ArrayList params = new ArrayList();
        String tableName = isReceipt ? "Receipt" : "Payment";
        String seqNumber = (isReceipt ? "receipt" : "payment") + "Number";
        String openingCheck = (isReceipt ? "isOpeningBalenceReceipt" : "isOpeningBalencePayment");      //Added OpeningBalence check
        try {
            params.add(accountid);
            params.add(startDate);
            params.add(endDate);
            params.add(companyid);
            params.add(minamount);
            params.add(maxamount);

            query = "select voucher from " + tableName + " as voucher where voucher.payDetail.paymentMethod.account.ID=? and voucher.creationDate >= ? and voucher.creationDate <= ? and voucher.company.companyID=? and voucher.depositAmount >= ? and voucher.depositAmount <= ?"
                    + " and voucher." + openingCheck + "=true and voucher.ID not in (select transactionID from BankReconciliationDetail where company.companyID = ? and isOpeningTransaction=true) order by voucher.creationDate, voucher." + seqNumber + " ";

            params.add(companyid);
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accBankReconciliationImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accBankReconciliationImpl.getMachingOpeningBalancesForReconciliation:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
        
    @Override
    public boolean isAlreadyReconcile(String accountid, String companyid, String jeid, boolean isConcileReport) throws ServiceException {
        boolean isReconcile = false;
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            String condition = isConcileReport? "not":"";
            params.add(accountid);
            params.add(jeid);
            params.add(companyid);
            String query = "select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and je.ID =? and ac.company.companyID=? and je.deleted=false "
                    +  " and jed.journalEntry.ID "+ condition +" in (select journalEntry.ID from BankReconciliationDetail where bankReconciliation.deleted=false AND bankReconciliation.account.ID=? and company.companyID=?) ";
	    params.add(accountid);
            params.add(companyid);
            list = executeQuery(query, params.toArray());
            if(!list.isEmpty()){
                isReconcile = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(accBankReconciliationImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accBankReconciliationImpl.isAlreadyReconcile:" + ex.getMessage(), ex);
        } 
        return isReconcile;
    }
    /**
     * Method to check the Record is Reconciled or not according to its JE id
     * @param jeid
     * @param companyid
     * @return
     * @throws ServiceException 
     */
    @Override
    public boolean isRecordReconciled(HashMap<String, Object> requestParams) throws ServiceException {
        String jeid = "",companyid="",journalentryids="";
        boolean isReconciledFlag = false;
        boolean isOpeningDocument = false;
        List bankConcilationList = null;
        try {

            if(requestParams.containsKey("jeid")){
                jeid= (String) requestParams.get("jeid");
            }
            if(requestParams.containsKey("companyid")){
                companyid= (String) requestParams.get("companyid");
            }
            if (requestParams.containsKey("isOpeningDocument") && requestParams.get("isOpeningDocument") != null) {
                isOpeningDocument = (Boolean) requestParams.get("isOpeningDocument");
            }
            if (!StringUtil.isNullOrEmpty(jeid) && !isOpeningDocument) {
                String[] ids = jeid.split(",");
                if (ids.length > 0) {
                    for (int i = 0; i < ids.length; i++) {
                        journalentryids += "'" + ids[i] + "',";
                    }
                }
                if (!StringUtil.isNullOrEmpty(journalentryids)) {
                    journalentryids = journalentryids.substring(0, journalentryids.length() - 1);
                }
            }
            ArrayList params = new ArrayList();
            params.add(companyid);
            String Query = "from BankReconciliationDetail where company.companyID =? and journalEntry.ID in (" + journalentryids + ")";
            if (isOpeningDocument) {
                Query = "from BankReconciliationDetail where company.companyID =? and transactionID = ? and isOpeningTransaction=true ";
                String transactionId = "";
                if (requestParams.containsKey("transactionId") && requestParams.get("transactionId") != null) {
                    transactionId = (String) requestParams.get("transactionId");
                }
                params.add(transactionId);
            }
            if (isOpeningDocument) {
                bankConcilationList = executeQuery(Query, params.toArray());
            } else {
                if (!StringUtil.isNullOrEmpty(journalentryids)) {
                    bankConcilationList = executeQuery(Query, params.toArray());
                }
            }
            if (bankConcilationList != null && !bankConcilationList.isEmpty()) {
                isReconciledFlag = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accBankReconciliationImpl.isRecordReconciled : " + ex.getMessage(), ex);
        }
        return isReconciledFlag;
    }
    /**
     * Method to delete the un-Reconciled Records according to its JE id
     * @param jeid
     * @param companyid
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject deleteUnReconciliationRecords(HashMap<String, Object> requestParams) throws ServiceException {
        int numtotal = 0;
         String jeid = "",companyid="",journalentryids="";
        try {
             if(requestParams.containsKey("jeid")){
                jeid= (String) requestParams.get("jeid");
            }
            if(requestParams.containsKey("companyid")){
                companyid= (String) requestParams.get("companyid");
            }
                if (!StringUtil.isNullOrEmpty(jeid)) {
                    String[] ids = jeid.split(",");
                    for (int i = 0; i < ids.length; i++) {
                        journalentryids += "'" + ids[i] + "',";
                    }
                    if (!StringUtil.isNullOrEmpty(journalentryids)) {
                        journalentryids = journalentryids.substring(0, journalentryids.length() - 1);
                    }
            }
            ArrayList params1 = new ArrayList();
            params1.add(companyid);
            String delQuery1 = "delete from BankReconciliationDetailHistory where company.companyID =? and jeid in (" + journalentryids + ")";
            if (!StringUtil.isNullOrEmpty(jeid)) {
                numtotal = executeUpdate(delQuery1, params1.toArray());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accBankReconciliationImpl.deleteUnReconciliationRecords : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Bank Reconciliation Detail History has been deleted successfully.", null, null, numtotal);
    }
    
    /**
     * Method to delete the Payment Details Like Cheque Details, Payment Method Details which are linked with Payment
     * @param HashMap ()
     * @return void
     * @throws ServiceException 
     */   
    @Override
    public void deleteCashPayDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String chequeid = "", cardid="", paydetailid="", companyid = "", journalentryids = "";
        try {
            if (requestParams.containsKey("companyid")) {
                companyid = (String) requestParams.get("companyid");
            }            
            if (requestParams.containsKey("paydetailid") && !StringUtil.isNullOrEmpty((String)requestParams.get("paydetailid"))) {
                paydetailid = (String) requestParams.get("paydetailid");
                //Delete Pay Detail Entry
                String paydetailquery = "DELETE FROM paydetail WHERE company=? AND id=?";
                executeSQLUpdate(paydetailquery, new Object[]{companyid, paydetailid});
            }
            if (requestParams.containsKey("chequeid") && !StringUtil.isNullOrEmpty((String)requestParams.get("chequeid"))) {
                chequeid = (String) requestParams.get("chequeid");
                //Delete Cheque Details
                String chkquery = "DELETE FROM cheque WHERE company=? AND id=?";
                executeSQLUpdate(chkquery, new Object[]{companyid,chequeid});
            }
            if (requestParams.containsKey("cardid") && !StringUtil.isNullOrEmpty((String)requestParams.get("cardid"))) {
                cardid = (String) requestParams.get("cardid");
                //Delete Card Details
                String cardquery = "DELETE FROM card WHERE id=?";
                executeSQLUpdate(cardquery, new Object[]{cardid});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accBankReconciliationImpl.deleteCashPurchasePayDetails : " + ex.getMessage(), ex);
        }
    }
    
    @Override
    public KwlReturnObject getLastReconcileAmountAndDate(String accid,String companyid) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            params.add(accid);  
            params.add(companyid);  
            params.add(accid);  
            params.add(companyid);  
//            String mysqlQuery = "select MAX(clearancedate),endingbalancereconcileamount from bankreconciliation where clearancedate=(select MAX(clearancedate) from bankreconciliation where account=? and company=?)";
//            String mysqlQuery = "select clearancedate,bankstmtbalanceinacc from bankreconciliation "
//                    + "where clearancedate=(select MAX(clearancedate) from bankreconciliation where account=? "
//                    + "and company=? and deleteflag='F') and account=? and company=? and deleteflag='F'";

            String mysqlQuery = "select clearancedate,bankstmtbalanceinacc from bankreconciliation "
                    + "where createdon=(select MAX(createdon) from bankreconciliation where account=? "
                    + "and company=? and deleteflag='F') and account=? and company=? and deleteflag='F'";

            list = executeSQLQuery(mysqlQuery, params.toArray());
            count = list.size();

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accBankReconciliationImpl.getLastReconcileAmountAndDate:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }  
    
}
