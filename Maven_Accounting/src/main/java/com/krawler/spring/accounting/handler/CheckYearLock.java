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

package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.AccountingPeriod;
import static com.krawler.common.admin.AccountingPeriod.SAVE_JOURNAL_ENTRY;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.periodSettings.accPeriodSettingsDao;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 *
 * @author krawler
 */
public class CheckYearLock implements MethodInterceptor {
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accPeriodSettingsDao accPeriodSettingsDao;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyDAOobj = accCurrencyobj;
    }
    public void setaccPeriodSettingsDao(accPeriodSettingsDao accPeriodSettingsDao) {
        this.accPeriodSettingsDao = accPeriodSettingsDao;
    }
    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        String methodName = mi.getMethod().getName();
        Date date=new Date();
        Object valueReturn=null;
        boolean saveasDraftflag = false;
        boolean isOpeningInvoice = false;
        if(AccountingPeriod.MethodSetForAllGeneralLedger.contains(methodName)|| AccountingPeriod.MethodSetForAccountPayable.contains(methodName)  || AccountingPeriod.MethodSetForAccountReceivable.contains(methodName)) {
            boolean checkYearLock = true;
            Object arguments[] = mi.getArguments();
            if (methodName.equals("saveJournalEntry")) {//While Saving Journal Entry In some case we does not needed to check Year lock, This if block is for that purpose.
                try {
                    Map<String, Object> dataMap = (Map<String, Object>) arguments[0];
                    if (dataMap.containsKey("DontCheckYearLock") && dataMap.get("DontCheckYearLock") != null) {
                        boolean dontCheckYearLock = Boolean.parseBoolean(dataMap.get("DontCheckYearLock").toString());
                        if (dontCheckYearLock) {
                            checkYearLock = false;
                        }
                    }
                } catch (Exception ex) {
                    checkYearLock = true;
                }
            }
            HashMap<String, Object> requestParams =new HashMap();
            if(checkYearLock){

                SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();
                if (methodName.equals(AccountingPeriod.SAVE_INVOICE)) {
                    JSONObject invocejobj = (JSONObject) arguments[0];
                    saveasDraftflag = invocejobj.optBoolean(Constants.isSaveAsDraft, false);
                    isOpeningInvoice = invocejobj.optBoolean("isOpeningBalenceInvoice", false);
                    requestParams.put("isOpeningBalanceOrder", isOpeningInvoice);
                    if (!saveasDraftflag) {
                        if (invocejobj.has(Constants.Checklocktransactiondate)) {
                            try {

                                date = formatter.parse((String) invocejobj.getString(Constants.Checklocktransactiondate));
                            } catch (Exception ex) {
                                date = (Date) invocejobj.get(Constants.Checklocktransactiondate);
                            }
                        }
                    }
                    requestParams.put("companyid", invocejobj.get("companyid"));
                } else {
                    requestParams = (HashMap<String, Object>) arguments[0];
                    if (requestParams.containsKey(Constants.isSaveAsDraft)) {
                        saveasDraftflag = (Boolean) requestParams.get(Constants.isSaveAsDraft);
                    } else {
                        saveasDraftflag = false;
                    }
                }
                
                if (!saveasDraftflag) {//Excluding saveasDraft condition to check lock period
                    if (methodName.equals("saveJournalEntry") || methodName.equals("saveReverseJournalEntry")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                        } else {
                            date = (Date) requestParams.get("entrydate");
                        }

                    } else if (methodName.equals(AccountingPeriod.ADD_PURCHASE_INVOICE)) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            try {
                                date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                            } catch (Exception e) {//In case of repeated vendor invoice at crone hit.
                                date = (java.util.Date) requestParams.get(Constants.Checklocktransactiondate);
                            }
                        }
                        if (requestParams.containsKey("isOpeningBalenceInvoice") && requestParams.get("isOpeningBalenceInvoice") != null) {
                            requestParams.put("isOpeningBalanceOrder", Boolean.parseBoolean(requestParams.get("isOpeningBalenceInvoice").toString()));
                        }
                       
                    } else if (methodName.equals("addCreditNote")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            try {
                                date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                            } catch (Exception e) {//In case of repeated vendor invoice at crone hit.
                                date = (java.util.Date) requestParams.get(Constants.Checklocktransactiondate);
                            }
                        }
                        if (requestParams.containsKey("isOpeningBalenceCN") && requestParams.get("isOpeningBalenceCN") != null) {
                            requestParams.put("isOpeningBalanceOrder", Boolean.parseBoolean(requestParams.get("isOpeningBalenceCN").toString()));
                        }

                    } else if (methodName.equals("addDebitNote")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            try {
                                date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                            } catch (Exception e) {//In case of repeated vendor invoice at crone hit.
                                date = (java.util.Date) requestParams.get(Constants.Checklocktransactiondate);
                            }
                        }
                        if (requestParams.containsKey("isOpeningBalenceDN") && requestParams.get("isOpeningBalenceDN") != null) {
                            requestParams.put("isOpeningBalanceOrder", Boolean.parseBoolean(requestParams.get("isOpeningBalenceDN").toString()));
                        }


                    } else if (methodName.equals("updateCreditNote")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            try {
                                date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                            } catch (Exception e) {//In case of repeated vendor invoice at crone hit.
                                date = (java.util.Date) requestParams.get(Constants.Checklocktransactiondate);
                            }
                        }
                        if (requestParams.containsKey("isOpeningBalenceCN") && requestParams.get("isOpeningBalenceCN") != null) {
                            requestParams.put("isOpeningBalanceOrder", Boolean.parseBoolean(requestParams.get("isOpeningBalenceCN").toString()));
                        }

                    } else if (methodName.equals("updateDebitNote")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            try {
                                date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                            } catch (Exception e) {//In case of repeated vendor invoice at crone hit.
                                date = (java.util.Date) requestParams.get(Constants.Checklocktransactiondate);
                            }
                        }
                        if (requestParams.containsKey("isOpeningBalenceDN") && requestParams.get("isOpeningBalenceDN") != null) {
                            requestParams.put("isOpeningBalanceOrder", Boolean.parseBoolean(requestParams.get("isOpeningBalenceDN").toString()));
                        }

                    }
                    else if (methodName.equals("saveGoodsReceiptOrder")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                        } else {
                            date = (Date) requestParams.get("orderdate");
                        }
                    } else if (methodName.equals("saveDeliveryOrder")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                        } else {
                            date = (Date) requestParams.get("orderdate");
                        }
                    } else if (methodName.equals("saveQuotation")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                        } else {
                            date = (Date) requestParams.get("orderdate");
                        }
                    } else if (methodName.equals("savePurchaseOrder")||methodName.equals("updateLinkedPurchaseOrder")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                        } else {
                            date = (Date) requestParams.get("orderdate");
                        }
                        requestParams.put(Constants.isFromPO, true);
                    }else if (methodName.equals("saveSalesOrder")||methodName.equals("updateLinkedSalesOrder")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                        } else {
                            date = (Date) requestParams.get("orderdate");
                        }
                        requestParams.put(Constants.isFromSO, true);
                    } else if (methodName.equals("savePurchaseReturn")) {
                        if (requestParams.containsKey("transactiondate")) {
                            date = (Date) requestParams.get("transactiondate");
                        } else {
                            date = (Date) requestParams.get("orderdate");
                        }
                    } else if (methodName.equals("saveSalesReturn")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                        } else {
                            date = (Date) requestParams.get("orderdate");
                        }
                    } else if (methodName.equals("getPaymentObj")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                        } else {
                            date = (Date) requestParams.get("creationdate");
                        }
                    } else if (methodName.equals("getReceiptObj")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                        } else {
                            date = (Date) requestParams.get("creationdate");
                        }
                    } else if (methodName.equals("saveVendorQuotation")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                        } else {
                            date = (Date) requestParams.get("orderdate");
                        }
                    } else if (methodName.equals("savePurchaseRequisition")) {
                        if (requestParams.containsKey(Constants.Checklocktransactiondate)) {
                            date = formatter.parse((String) requestParams.get(Constants.Checklocktransactiondate));
                        } else {
                            date = (Date) requestParams.get("orderdate");
                        }
                    }
                    

                    if (AccountingPeriod.MethodSetForAccountPayable.contains(methodName)) {

                        isTransactionBelongingTOLockedPeriod(requestParams, date, AccountingPeriod.ACCOUNT_PAYABLE_LOCK, methodName);
                    }
                    if (AccountingPeriod.MethodSetForAccountReceivable.contains(methodName)) {

                        isTransactionBelongingTOLockedPeriod(requestParams, date, AccountingPeriod.ACCOUNT_RECEIVABLE_LOCK, methodName);
                    }

                    if (AccountingPeriod.MethodSetForAllGeneralLedger.contains(methodName)) {

                        isTransactionBelongingTOLockedPeriod(requestParams, date, AccountingPeriod.All_GL_TRANSACTION_LOCK, methodName);
                    }

                    CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, date, false);
                }//end of save as draft
            }//end of dont check year lock     
        }
        if (methodName.equals("deleteGoodsReceiptPermanent") || methodName.equals("deletePaymentPermanent") || methodName.equals("deleteInvoicePermanent") || methodName.equals("deleteCreditNotesPermanent") || methodName.equals("deleteDebitNotesPermanent") || methodName.equals("deleteReceiptPermanent")) {
          Object arguments[] = mi.getArguments();  
            HashMap<String, Object> requestParams = (HashMap<String, Object>) arguments[0];
            if(!requestParams.containsKey("DontCheckYearLock")&&requestParams.containsKey("entrydate")){
                Date entryDate = (Date) requestParams.get("entrydate");
                if(entryDate != null){
                    isTransactionBelongingTOLockedPeriod(requestParams, entryDate, AccountingPeriod.All_GL_TRANSACTION_LOCK, methodName);
                }
                CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, entryDate,false);
            }
        }
        
        /**
         * Method to check if transaction date falls under locked accounting period.
         */
        if (methodName.equals("deleteInvoiceEntry") || methodName.equals("deleteGoodsReceiptEntry")||methodName.equals("deleteCreditNote")||methodName.equals("deleteDebitNote")||methodName.equals("deletePaymentEntry")||methodName.equals("deleteReceiptEntry")) {
            HashMap<String, Object> requestParams = new HashMap<>();
            Object arguments[] = mi.getArguments();
            String transactionID = (String) arguments[0];
            String companyId = (String) arguments[1];
            requestParams.put("companyid", companyId);

            Date entryDate = null;
            if (methodName.equals("deleteInvoiceEntry")) {
                KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), transactionID);
                Invoice invoice = (Invoice) jeResult.getEntityList().get(0);
                entryDate = (Date) invoice.getJournalEntry().getEntryDate();
            } else if (methodName.equals("deleteGoodsReceiptEntry")) {
                KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), transactionID);
                GoodsReceipt greceipt = (GoodsReceipt) jeResult.getEntityList().get(0);
                entryDate = (Date) greceipt.getJournalEntry().getEntryDate();
            }else if (methodName.equals("deleteCreditNote")) {
                KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), transactionID);
                CreditNote creditNote = (CreditNote) jeResult.getEntityList().get(0);
                entryDate = (Date) creditNote.getJournalEntry().getEntryDate();
            }else if (methodName.equals("deleteDebitNote")) {
                KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), transactionID);
                DebitNote debitNote = (DebitNote) jeResult.getEntityList().get(0);
                entryDate = (Date) debitNote.getJournalEntry().getEntryDate();
            } else if (methodName.equals("deletePaymentEntry")) {
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), transactionID);
                Payment payment = (Payment) objItr.getEntityList().get(0);
                entryDate = (Date) payment.getJournalEntry().getEntryDate();
            } else if (methodName.equals("deleteReceiptEntry")) {
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), transactionID);
                Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                entryDate = (Date) receipt.getJournalEntry().getEntryDate();
            }

            if (entryDate != null) {
                isTransactionBelongingTOLockedPeriod(requestParams, entryDate, AccountingPeriod.All_GL_TRANSACTION_LOCK, methodName);
            }
            CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, entryDate, false);
        }
        if (methodName.equals("saveJournalEntryDetailsSet") || methodName.equals("addJournalEntryDetails") || methodName.equals("getJEDset")) {
            valueReturn = mi.proceed(); // process called function 
            KwlReturnObject result = (KwlReturnObject) valueReturn; // value returned from the function
            try {
                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    Set<JournalEntryDetail> jedetails = new HashSet<>(result.getEntityList());
                    if (methodName.equals("getJEDset")) {
                        List list = result.getEntityList();
                        if (list != null && !list.isEmpty()) {
                            jedetails = new HashSet<>((Collection<? extends JournalEntryDetail>) list.get(0));
                        }
                    }
                    HashMap<String, Object> requestParams = new HashMap();
                    for (JournalEntryDetail journalEntryDetail : jedetails) {
                        requestParams.put("companyid", journalEntryDetail.getCompany().getCompanyID());
                        requestParams.put("gcurrencyid", journalEntryDetail.getCompany().getCurrency().getCurrencyID());
                        double exchangeRate=journalEntryDetail.getJournalEntry().getExternalCurrencyRate();
                        if (journalEntryDetail.getCompany().getCurrency().getCurrencyID().equals(journalEntryDetail.getJournalEntry().getCurrency().getCurrencyID())) {
                            exchangeRate = 1; // if transaction made in base currency
                        }
                        if(exchangeRate==0){
                            Logger.getLogger(CheckYearLock.class.getName()).log(Level.WARNING, null, "Exchange rate for Journal Entry ID: "+journalEntryDetail.getJournalEntry().getID() +" has not set.");
                        }
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, journalEntryDetail.getAmount(), journalEntryDetail.getJournalEntry().getCurrency().getCurrencyID(), journalEntryDetail.getJournalEntry().getEntryDate(),exchangeRate);
                        journalEntryDetail.setAmountinbase(authHandler.round((Double) bAmt.getEntityList().get(0), journalEntryDetail.getCompany().getCompanyID())); // updated amountinbase for jedetails
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(CheckYearLock.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (methodName.equals("saveJournalEntryDetailsSet") || methodName.equals("addJournalEntryDetails") || methodName.equals("getJEDset")) {
            return valueReturn;
        } else {
            return mi.proceed();
        }
        }
    
    
    public void isTransactionBelongingTOLockedPeriod(Map<String, Object> requestParams, Date date,int lockType,String methodName) throws AccountingException, ServiceException {
        boolean success = false;
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                Map<String, Object> AccountingPeriodRequestParams = new HashMap();
                AccountingPeriodRequestParams.put("periodtype", AccountingPeriod.AccountingPeriod_MONTH);
                AccountingPeriodRequestParams.put(Constants.Checklocktransactiondate, date);
                AccountingPeriodRequestParams.put("company", requestParams.get("companyid"));
                boolean isFromSO= requestParams.containsKey("isFromSO") && requestParams.get("isFromSO")!=null?(boolean)requestParams.get("isFromSO"):false;
                boolean isFromPO=  requestParams.containsKey("isFromPO") && requestParams.get("isFromPO")!=null&& (boolean) requestParams.get("isFromPO")?(boolean)requestParams.get("isFromPO"):false;
                boolean isBlockQuantity = requestParams.containsKey("islockQuantity") && requestParams.get("islockQuantity")!=null?(Boolean) requestParams.get("islockQuantity"):false;
                boolean isLinkedTransaction=requestParams.containsKey("isLinkedTransaction") &&  requestParams.get("isLinkedTransaction")!=null?(boolean)requestParams.get("isLinkedTransaction"):false;
                boolean isEdit=requestParams.containsKey("isEdit") && requestParams.get("isEdit")!=null?(boolean)requestParams.get("isEdit"):false;
                boolean isCopy=requestParams.containsKey("isCopy") &&  requestParams.get("isCopy")!=null?(boolean)requestParams.get("isCopy"):false; 
                DateFormat userdf = null;
                userdf = requestParams.containsKey("userdf") ? (DateFormat) requestParams.get("userdf") : null;
                if (StringUtil.isNullObject((userdf))) {
                    userdf = requestParams.containsKey("df") ? (DateFormat) requestParams.get("df") : null;
                }
                AccountingPeriod accPeriod = null;

                KwlReturnObject kwl = accPeriodSettingsDao.getParentAccountingPeriods(AccountingPeriodRequestParams);
                if (kwl.getEntityList() != null && kwl.getRecordTotalCount() > 0) {

                    accPeriod = (AccountingPeriod) kwl.getEntityList().get(0);
                    if (accPeriod != null) {
                        String startDate = userdf != null ? userdf.format(accPeriod.getStartDate()) : accPeriod.getStartDate().toString();
                        String endDate = userdf != null ? userdf.format(accPeriod.getEndDate()) : accPeriod.getStartDate().toString();

                        if (accPeriod.isPeridClosed()) {
                            /*
                             Allows user to edit SO/PO of closed period if SO Block Quantity option is not checked and SO/PO are not linked to any transaction.
                             */
                            if (isFromSO || isFromPO) {
                                if (!isEdit || (isEdit && isCopy)) { //create and copy case 
                                    throw new AccountingException("Transaction date belongs to locked period period name <b>(" + startDate + "</b> to <b>" + endDate + ")</b> so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting Period settings and then proceed.");
                                } else if (isEdit && isBlockQuantity && !isCopy) { //edit case and having block quantity
                                    throw new AccountingException("Transaction date belongs to locked period period name <b>(" + startDate + "</b> to <b>" + endDate + ")</b> and has blocked quantity so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting Period settings and then proceed.");
                                } else if (isEdit && isLinkedTransaction && !isCopy) { //edit case and is linked to another transaction 
                                    throw new AccountingException("Transaction date belongs to locked period period name <b>(" + startDate + "</b> to <b>" + endDate + ")</b> and it is linked to other transaction so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting Period settings and then proceed.");
                                }

                            } else {
                                throw new AccountingException("Transaction date belongs to locked period period name <b>(" + startDate + "</b> to <b>" + endDate + ")</b> so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting Period settings and then proceed.");
                            }
                        } else if (lockType == AccountingPeriod.ACCOUNT_RECEIVABLE_LOCK && accPeriod.isArTransactionClosed()) {
                            if (isFromSO) {
                                /*
                                 Allows user to edit SO/PO of closed period if SO Block Quantity option is not checked and SO/PO are not linked to any transaction.
                                 */
                                if (!isEdit || (isEdit && isCopy)) { //create and copy case 
                                    throw new AccountingException("Transaction date belongs to locked period period name<b>(A/R Transactions)</b>  <b>(" + startDate + "</b> to <b>" + endDate + ")</b> so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting period settings and then proceed.");
                                } else if (isEdit && isBlockQuantity && !isCopy) { //edit case and having block quantity
                                    throw new AccountingException("Transaction date belongs to locked period period name<b>(A/R Transactions)</b>  <b>(" + startDate + "</b> to <b>" + endDate + ")</b> and has blocked quantity so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting period settings and then proceed.");
                                } else if (isEdit && isLinkedTransaction && !isCopy) { //edit case and is linked to another transaction 
                                    throw new AccountingException("Transaction date belongs to locked period period name<b>(A/R Transactions)</b>  <b>(" + startDate + "</b> to <b>" + endDate + ")</b> and it is linked to other transaction so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting period settings and then proceed.");
                                }

                            } else {

                                throw new AccountingException("Transaction date belongs to locked period period name<b>(A/R Transactions)</b>  <b>(" + startDate + "</b> to <b>" + endDate + ")</b> so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting period settings and then proceed.");
                            }
                        } else if (lockType == AccountingPeriod.ACCOUNT_PAYABLE_LOCK && accPeriod.isApTransactionClosed()) {
                            if (isFromPO) {
                                /*
                                 Allows user to edit SO/PO of closed period if SO Block Quantity option is not checked and SO/PO are not linked to any transaction.
                                 */
                                if (!isEdit || (isEdit && isCopy)) { //create and copy case 
                                    throw new AccountingException("Transaction date belongs to locked period period name<b>(A/P Transactions)</b>  <b>(" + startDate + "</b> to <b>" + endDate + ")</b> so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting Period settings and then proceed.");
                                } else if (isEdit && isBlockQuantity && !isCopy) { //edit case and having block quantity
                                    throw new AccountingException("Transaction date belongs to locked period period name<b>(A/P Transactions)</b>  <b>(" + startDate + "</b> to <b>" + endDate + ")</b> and has blocked quantity so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting Period settings and then proceed.");
                                } else if (isEdit && isLinkedTransaction && !isCopy) { //edit case and is linked to another transaction 
                                    throw new AccountingException("Transaction date belongs to locked period period name<b>(A/P Transactions)</b>  <b>(" + startDate + "</b> to <b>" + endDate + ")</b> and it is linked to other transaction so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting Period settings and then proceed.");
                                }

                            } else {
                                throw new AccountingException("Transaction date belongs to locked period period name<b>(A/P Transactions)</b>  <b>(" + startDate + "</b> to <b>" + endDate + ")</b> so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting Period settings and then proceed.");
                            }
                        } else if (lockType == AccountingPeriod.All_GL_TRANSACTION_LOCK && accPeriod.isAllGLTransactionClosed()) {
                            throw new AccountingException("Transaction date belongs to locked period period name<b>(All GL Transactions)</b>  <b>(" + startDate + "</b> to <b>" + endDate + ")</b> so it could not be saved. You can reopen the locked period <b>(" + startDate + "</b> to <b>" + endDate + ")</b> from Accounting Period settings and then proceed.");
                        }

                    }
                }
            }
        } catch (AccountingException ex) {
          throw new AccountingException(ex.getMessage(),ex);
        }
       
    }
    
}
