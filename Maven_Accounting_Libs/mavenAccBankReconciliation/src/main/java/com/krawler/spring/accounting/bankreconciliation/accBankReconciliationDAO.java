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

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BankReconciliation;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface accBankReconciliationDAO {

    public KwlReturnObject addBankReconciliation(HashMap<String, Object> brMap) throws ServiceException;
    
    public KwlReturnObject saveBankReconciliationDraft(JSONObject dataObj) throws ServiceException;
    
    public KwlReturnObject deleteBankReconciliationDraft(JSONObject paramJobj) throws ServiceException;
    
    public KwlReturnObject getBankReconcilationDrafts(JSONObject dataObj) throws ServiceException;
    
    public KwlReturnObject updateDraftedJournalEntries(JSONObject paramJobj) throws ServiceException;
    
    public KwlReturnObject getDraftedJournalEntries(JSONObject dataObj) throws ServiceException;

    //public KwlReturnObject updateBankReconciliation(HashMap<String, Object> brMap) throws ServiceException;   //This method is not used anywhere in project

    public KwlReturnObject addBankReconciliationDetail(HashMap<String, Object> brdMap) throws ServiceException;
    
    public KwlReturnObject addBankUnreconciliationDetail(HashMap<String, Object> brdMap) throws ServiceException;

    public KwlReturnObject updateBankReconciliationDetail(HashMap<String, Object> brdMap) throws ServiceException;

    public KwlReturnObject deleteBankReconciliation(String brid, String companyid) throws ServiceException;

    public KwlReturnObject permenantDeleteBankReconciliation(String brid, String companyid) throws ServiceException;

    public KwlReturnObject permenantDeleteBankReconciliationDetail(String brdid, String companyid) throws ServiceException;
    
    public KwlReturnObject permenantDeleteBankUnReconciliationDetail(String brdid, String companyid) throws ServiceException;
    
    public KwlReturnObject permenantDeleteBankReconciliationDetailUsingJE(String jeID, String companyid) throws ServiceException;
    
    public KwlReturnObject permenantDeleteBankUnReconciliationDetailUsingJE(String jeID, String companyid) throws ServiceException;

    public KwlReturnObject getBankReconciliation(HashMap<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject getBankReconciliationHistory(HashMap<String, Object> params) throws ServiceException;

    public KwlReturnObject getBRfromJE(String jeid, String companyid, boolean ignoreDeleted) throws ServiceException;
    
    public KwlReturnObject getBRWithoutJE(String billid, String companyid, int moduleid) throws ServiceException;
    
    public KwlReturnObject getBankUnReconsiledWithoutJE(String billid, String companyid, int moduleid) throws ServiceException;
    
    public KwlReturnObject getBankUnReconsiledfromJE(String jeid, String companyid, boolean ignoreDeleted) throws ServiceException;
     
    public KwlReturnObject saveBankReconcilationDocuments(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getBankReconcilationDocuments(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject deleteBankReconcilationDocument(String documentID) throws ServiceException;
    
    public KwlReturnObject updateBankReconciliationDocuments(String docID,String brID) throws ServiceException;
    
    public KwlReturnObject getMachingRecordsForReconciliation(String companyid, String accountid, Date startDate, Date endDate, double minamount, double maxamount) throws ServiceException;
    
    public KwlReturnObject getMachingOpeningBalancesForReconciliation(String companyid, String accountid, Date startDate, Date endDate, double minamount, double maxamount, boolean isReceipt) throws ServiceException;
    
    public boolean isAlreadyReconcile(String accountid, String companyid, String jeid, boolean isConcileReport) throws ServiceException;
    
    public KwlReturnObject addBankReconciliationDetailsHistory(HashMap<String, Object> brdMap) throws ServiceException;
    
    public KwlReturnObject searchReconcileNo(String reconcilenumber, String companyId, boolean isConcileReport) throws ServiceException;
    
    public KwlReturnObject updateBankReconciliation(BankReconciliation br) throws ServiceException;
    
    public KwlReturnObject getBankReconciliationHistoryRows(HashMap<String, Object> rowmap) throws ServiceException;
    
    public KwlReturnObject getLastReconcileAmountAndDate(String accid,String companyid) throws ServiceException;
    /**
     * Method to check the Record is Reconciled or not according to its JE id
     * @param jeid
     * @param companyid
     * @return
     * @throws ServiceException 
     */
    public boolean isRecordReconciled(HashMap<String, Object> requestParams) throws ServiceException;
    /**
     * Method to delete the un-Reconciled Records according to its JE id
     * @param jeid
     * @param companyid
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject deleteUnReconciliationRecords(HashMap<String, Object> requestParams) throws ServiceException;
    
    /**
     * Method to delete the Payment Details Like Cheque Details, Payment Method Details which are linked with Payment
     * @param HashMap ()
     * @return void
     * @throws ServiceException 
     */
    public void deleteCashPayDetails(HashMap<String, Object> requestParams) throws ServiceException;
}
