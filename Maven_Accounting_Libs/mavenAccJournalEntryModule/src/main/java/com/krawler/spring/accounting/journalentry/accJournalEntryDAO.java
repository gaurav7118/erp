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

import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.*;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.sql.SQLException;
import java.util.*;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface accJournalEntryDAO {

    public KwlReturnObject deleteJournalEntryDetailRow(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject deleteRevalEntry(String id) throws ServiceException;
    
    public KwlReturnObject getJournalEntryDetail(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getTax1099AccJE(String string, Date date, String iD) throws ServiceException;

    public KwlReturnObject permanentDeleteJournalEntry(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getChequeIdLinkedToJournalEntry(String jeid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteRevalhistoryEntry(Date date,String ids[],String evalId, int accTypeId) throws ServiceException;

    public KwlReturnObject deleteJournalEntryDetails(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getJECount(String jeno, String companyid) throws ServiceException;

    public KwlReturnObject getJEDset(JSONArray JArr, String companyid,JournalEntry je) throws ServiceException;

    public KwlReturnObject getJEDsetCNDN(JSONArray JArr, String companyid) throws ServiceException;

    public KwlReturnObject getJEDsetForRevaluation(String companyid, String accountId, String proflossaccountId, double acmount, int accTypeId) throws ServiceException;

    public KwlReturnObject addJournalEntryDetails(JSONObject json) throws ServiceException;
    
    public KwlReturnObject saveJournalEntryDetailsSet(Set<JournalEntryDetail> entryDetails) throws ServiceException;
    
    public JournalEntryDetail getJournalEntryDetails(JSONObject json) throws ServiceException;

    public int saveCustomDataForRecurringJE(String New_JE_ID, String Old_JE_ID, boolean JE_OR_JED) throws ServiceException;

    public KwlReturnObject getJournalEntry(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getJournalEntryTotalAmountSum(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveRepeateJEInfo(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveRepeateJEMemo(HashMap<String, Object> dataMap) throws ServiceException;

    public int DelRepeateJEMemo(String repeateid, String column) throws ServiceException;

    public KwlReturnObject updateJE(JSONObject json, HashSet details) throws ServiceException;

    public KwlReturnObject getRepeateJEDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getJERepeateMemoDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getRepeateJEMemo(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getRepeateJE(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveJournalEntry(Map<String, Object> dataMap) throws ServiceException, AccountingException;
    
    public KwlReturnObject saveJournalEntryByObject(JournalEntry journalEntry) throws ServiceException, AccountingException;
    
    public JournalEntry getJournalEntry(Map<String, Object> dataMap) throws ServiceException, AccountingException;

    public KwlReturnObject updateCustomFieldJournalEntry(Map<String, Object> dataMap) throws ServiceException, AccountingException;

    public KwlReturnObject updateJournalEntryDetails(JSONObject json) throws ServiceException;

    public KwlReturnObject addJournalEntry(JSONObject json, HashSet<JournalEntryDetail> details) throws ServiceException, AccountingException;

    public KwlReturnObject updateJournalEntry(JSONObject json, HashSet<JournalEntryDetail> details) throws ServiceException, AccountingException;

    public KwlReturnObject deleteJE(String jeid, String companyid) throws ServiceException;

    public boolean updateJEDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public boolean updateJEDetailsSQLQuery(HashMap<String, Object> dataMap) throws ServiceException ;

    public KwlReturnObject deleteJEDtails(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getJEDfromAccount(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject deleteJournalEntry(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject deleteJournalEntryPermanent(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getAccountBalance(String accountid, Date startDate, Date endDate) throws ServiceException;

    public KwlReturnObject getAccountBalance(String accountid, Date startDate, Date endDate, String costCenterID, String filterConjuctionCriteria, String Searchjson) throws ServiceException;

    public KwlReturnObject getAccountBalance(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, String costCenterID, String filterConjuctionCriteria, String Searchjson) throws ServiceException;

    public KwlReturnObject getAccountBalanceAmount(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, String costCenterID, String filterConjuctionCriteria, String Searchjson, Map<String, Object> advSearchAttributes) throws ServiceException;

    public KwlReturnObject getClosingAccountBalance(String accountId, String companyId, int year) throws ServiceException;

    public KwlReturnObject getClosingAccountBalanceYTD(String accountId, String companyId) throws ServiceException;

    public KwlReturnObject getCurrencyJEs(String currencyid, Date startDate, Date endDate) throws ServiceException;

    public KwlReturnObject getOldCurrencyRateAndDate(String companyid, Date transactiondate, String erid) throws ServiceException;

    public KwlReturnObject getAccountJEs(String accountid, Date startDate, Date endDate) throws ServiceException;

    public KwlReturnObject saveAccountJEs_optimized(String accountid, String companyid, String entryDate, String costCenterID, double amount) throws ServiceException;

    public KwlReturnObject getAccountBalance_optimized(String accountid, Date startDate, Date endDate, String costCenterID) throws ServiceException;

    public boolean setCompany_optimizedflag(String companyid) throws ServiceException;

    public boolean setJEs_optimizedflag(String jeid) throws ServiceException;

    public boolean saveAccountJEs_optimized(String jeid) throws ServiceException;

    public boolean deleteOnEditAccountJEs_optimized(String jeid) throws ServiceException;

        public boolean deleteAccountJEs_optimized(String jeid) throws ServiceException;

    public boolean deleteAccountJEs_optimized(String jeid, boolean callfromJEdelete) throws ServiceException;

    public KwlReturnObject eliminateJournalEntry(String jeid) throws ServiceException;

    public KwlReturnObject getAccountBalanceMerged(String accountid, Date startDate, Date endDate, String costCenterID, Boolean eliminateFlag, String filterConjuctionCriteria, String Searchjson) throws ServiceException;

    public KwlReturnObject getAccountBalanceMergedAmount(String accountid, Date startDate, Date endDate, String costCenterID, Boolean eliminateFlag, String filterConjuctionCriteria, String Searchjson, Boolean isDebit, Boolean isKnockOffAdvancedSearch,String companyid) throws ServiceException ;

    public KwlReturnObject getLedgerMerged(String accountid, Date startDate, Date endDate, boolean eliminateflag, boolean generalLedgerFlag, String selectedCurrencyIds, String filterConjuctionCriteria, String Searchjson,int viewFlag, int accountTransactionType,CompanyAccountPreferences pref) throws ServiceException;

    public KwlReturnObject getLedger(String companyid, String accountid, Date startDate, Date endDate) throws ServiceException;
    
    public KwlReturnObject getLedgerForReconciliation(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getReconciliationOfOpeningTransactions(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getMaxDateOfReconciliation(String companyid, String accountid, Date startDate, Date endDate) throws ServiceException;

    public double getReconciliationClearBalance(String companyid, String accountid, Date endDate) throws ServiceException;

    public KwlReturnObject deleteJEEntry(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject updateJournalEntryDetails(Map requestMap) throws ServiceException;

    public KwlReturnObject getJournalEntryDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getJournalEntryDetailsForBank(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getJournalEntryDetailsForReport(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getJournalEntryForFinanceReport(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getJournalEntryDetailsForFinanceReport(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getJournalEntryCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getJournalEntryCustomDataNew(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceInvoiceCustomDataNew(HashMap<String, Object> requestParams) throws ServiceException ;
    
    public KwlReturnObject getOpeningBalanceInvoiceCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceDebitNoteCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceCreditNoteCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getOpeningBalanceReceiptCustomData(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getOpeningBalancePaymentCustomData(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getMonthlyRevenue(String companyid, String accountid, Date startDate, Date endDate) throws ServiceException;

    public KwlReturnObject getJEDFixedAssetSale(String companyid, String accountid, boolean isDebit, String Memo) throws ServiceException;

    public KwlReturnObject getJournalEntryForReports(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject updateReverseJournalEntryValue(JournalEntry journalEntry, String reverseJournalEntry) throws ServiceException;

    public KwlReturnObject approvePendingJE(String jeid) throws ServiceException;

    public KwlReturnObject getAllJournalEntry(String companyid) throws ServiceException;

    public KwlReturnObject addRevalHistory(RevaluationHistory requestParams) throws ServiceException, AccountingException;

    public KwlReturnObject updateRevaluationFlag(String id) throws ServiceException, AccountingException;

    public Double getRevalHistoryRateForInvoice(String invoiceId,Map<String, Object> reqParams) throws ServiceException, AccountingException;

    public KwlReturnObject deleteJEDetailsCustomData(String jedetailsid) throws ServiceException;

    public KwlReturnObject deleteJECustomData(String jedetailsid) throws ServiceException;

    public KwlReturnObject saveRevalTime(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveRevaluationJECustomData(Map<String, Object> requestParams) throws ServiceException;
     
    public KwlReturnObject getRevaluationJECustomData(String companyid) throws ServiceException;

    public KwlReturnObject getRevalMonthYearStatus(Date revalDate, int accountType, String currencyIDs, String companyid) throws ServiceException;

    public KwlReturnObject ReevaluationHistoryReport(Map<String, Object> request) throws ServiceException;
    
    public KwlReturnObject deleteRevaluationHistory(JSONObject paramsJobj) throws ServiceException;

    public KwlReturnObject getRevalInvoiceId(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getFromRevalID(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteJournalEntryReval(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject permanentDeleteJournalEntryReval(String invoiceid, String companyid) throws ServiceException;
    
    public KwlReturnObject permanentDeleteJournalEntryDetailReval(String invoiceid, String companyid) throws ServiceException;
    
    public KwlReturnObject permanentDeleteCreditNoteAgainstVendorGst(String cnId, String companyid) throws ServiceException;
    
    public KwlReturnObject permanentDeleteDebitNoteAgainstCustomerGst(String dnId, String companyid) throws ServiceException;

    public String getJsornStringForSearch(String searchJson, String accId, String companyId) throws ServiceException;
    
    public String advanceserachJsornEdit(String Searchjson,String companyid,int moduleid,boolean retainModuleId) throws ServiceException ;
    
    public String advSerachJsonForMultiModules(String Searchjson,String companyid,int moduleid,String modulids) throws ServiceException ;
    
    public String[] getNextAutoNumber_modified(String companyID, String modulename) throws ServiceException, AccountingException;
    //public void updateJETemplateCode(JournalEntry JEObj, String companyid) throws ServiceException;

    public boolean checkForRule(int level, String companyid, String amount, String thisUser) throws AccountingException, ServiceException, ScriptException;
            
    public KwlReturnObject approvePendingJE(String jeID, String companyid, int approvalStatus) throws ServiceException;
    
    public KwlReturnObject rejectPendingJE(String jeID, String companyid) throws ServiceException;
    
    public KwlReturnObject updateChequePrint(String jeid,String companyid)throws ServiceException;

    public KwlReturnObject getCNFromJE(String oldjeid, String companyid)throws ServiceException;

    public void deletePartyJournalCN(List list, String companyid)throws ServiceException;

    public KwlReturnObject getDNFromJE(String oldjeid, String companyid)throws ServiceException;

    public void deletePartyJournalDN(List list, String companyid)throws ServiceException;
    
    public KwlReturnObject getJEforRefund(HashMap<String, Object> requestParams) throws ServiceException;
    
    public int getModuleIdForJournalEntry(HashMap<String, Object> requestParams , String jeId) throws ServiceException;
    
    public void reverseRecurringJEForOneTime(String reversejeno, String companyid) throws ServiceException;
    
    public KwlReturnObject activateDeactivateJournalEntry(String repeateid, boolean isactivate) throws ServiceException;
    
    public KwlReturnObject approveRecurringJE(String repeateid, boolean ispendingapproval) throws ServiceException;
    
    public KwlReturnObject getRepeateJEEntryNo(Date prevDate) throws ServiceException;
    
    public Object getUserObject(String id) throws ServiceException;   
    
    public KwlReturnObject getEntryDateFromJEId(String id, String CompanyId) throws ServiceException ;
    
    public KwlReturnObject saveRepeatePaymentChequeDetail(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getRepeatePaymentChequeDetail(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getChequeDetailsForRepeatedJE(String repeatPaymentId) throws ServiceException;
    
    public int DelRepeateJEChequeDetails(String repeateid) throws ServiceException;
    
    public KwlReturnObject saveRepeateJEChequeDetail(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getRepeateJEChequeDetail(HashMap<String, Object> requestParams) throws ServiceException;

    public String UpdateJournalEntry(Map<String, Object> seqNumberMap);
    
    public KwlReturnObject getJECountForEdit(String jeno, String companyid,String jeId) throws ServiceException; 
    
    public String updateJEEntryNumberForNA(String jeId, String entrynumber) throws ServiceException;
    
    public KwlReturnObject saveRevenueJEInvoiceMapping(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject DeleteBankUnReconciliationDetail(String jeID, String companyid) throws ServiceException ;
    
    public KwlReturnObject DeleteBankReconciliationDetail(String jeID, String companyid) throws ServiceException ;
    
    public KwlReturnObject getJEDetailsToIncludeInGSTReport(Map<String, Object> dataMap) throws ServiceException;
    
    public int updateToNullRepeatedJEOfJournalEntry(String invoiceid, String repeateid)throws ServiceException;
    
    public int deleteRepeatedJE(String repeateid)throws ServiceException;
    
    public KwlReturnObject getFieldParameters(HashMap<String, Object> requestParams);
    
    public KwlReturnObject getForeignGainLossJouranalEntryDetails(Map<String, Object> requestParams)throws ServiceException;
    
    public KwlReturnObject getReconciliationOfAccountOpeningTransactions(HashMap<String, Object> requestParams) throws ServiceException ;
    
    public boolean saveJournalEntryUpdateHistory(JSONObject params);
    
    public boolean checkIfJEisVatJE(JSONObject params) throws ServiceException;
    
    public KwlReturnObject getJEDetailListToCalculatePrincipleAmount(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getJEDetailsID(Map<String, Object> params) throws ServiceException;
    public List getLedgerInfo(String usersessionid) throws ServiceException;
    public KwlReturnObject getMasterItemByNameorID(String companyid, String value, String masterGroupID) throws ServiceException ;
    public List getSumAmountForAccount(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getRoundingJournalEntryByGRIds(String grIDs,String companyid) throws ServiceException;
    
    public KwlReturnObject getTDSJEEntryMapping(String goodsReceipt, String companyid) throws ServiceException;
    
    public KwlReturnObject getJEEntryFromMapping(String goodsReceipt, String companyid) throws ServiceException ;
    
    public void deleteJournalEntryTDSMapping(String jeid, String companyid) throws ServiceException;
    
    public void permanentDeleteJournalEntryDetailTDSMapping(String jeid, String companyid)throws ServiceException;
    
    public void permanentDeleteJournalEntryTDSMapping(String jeid,String companyid)throws ServiceException;
    
    public Map<String, Object> getSQLDefaultAdvanceSearchAttributes(String mySearchFilterString, String Searchjson, String accountid, String companyid, ArrayList params, String filterConjuctionCriteria,boolean isLineDimPresentInAdvSearch) throws ServiceException;
    
    public int saveCustomDataForReverseJE(String New_JE_ID, String Old_JE_ID, boolean JE_OR_JED) throws ServiceException;
    
    public Map<String, Object> getAdvanceSearchAttributes(String Searchjson, CompanyAccountPreferences preferences, String companyid, String accountid, String filterConjuctionCriteria) throws ServiceException;
    
    public KwlReturnObject resetIsRealisedFlagofRevalHistory(Map<String, Object> params) throws ServiceException;
    
    public KwlReturnObject getJournalEntryDetailsCustomDatabyJEIds(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getJournalEntryCustomDataByJEIds(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getTransactionNumber(HashMap<String,Object> params) throws ServiceException ;
    
}
