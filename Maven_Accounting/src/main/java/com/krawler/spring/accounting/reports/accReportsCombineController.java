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
package com.krawler.spring.accounting.reports;

import com.google.common.collect.HashBiMap;
import com.ibm.icu.util.IndianCalendar;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.packaging.Packaging;
//import com.krawler.spring.accounting.account.accAccountController;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.depreciation.accDepreciationDAO;
import com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.AccExportReportsServiceDAO;
import com.krawler.spring.exportFuctionality.ExportRecord;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
 * @author sagar
 */
public class accReportsCombineController extends MultiActionController implements MessageSourceAware{
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accProductDAO accProductObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accAccountDAO accAccountDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private String successView;
    private accGoodsReceiptDAO accGoodsReceiptDAOObj;
    private accGoodsReceiptCMN accGoodsReceiptCommon;
    private accInvoiceDAO accInvoiceDAOobj;
    private accTaxDAO accTaxObj;
    private accInvoiceCMN accInvoiceCommon;
    private authHandlerDAO authHandlerDAO;
    private ExportRecord ExportrecordObj;
    private AccCostCenterDAO accCostCenterObj;
    private accReceiptDAO accReceiptDao;
    private accVendorDAO accVendorDAOobj;
    private accVendorPaymentDAO accVendorPaymentDao;
    private MessageSource messageSource;
    private accDebitNoteDAO accDebitNoteobj;
    private accCreditNoteDAO accCreditNoteobj;
    private accDepreciationDAO accDepreciationObj;
    private accBankReconciliationDAO accBankReconciliationObj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accPurchaseOrderDAO accPurchaseOrderDAOobj;
    AccReportsService accReportsService;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private APICallHandlerService apiCallHandlerService; 
    private AccExportReportsServiceDAO accExportReportsServiceDAO; 
    private HibernateTransactionManager txnManager;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    
    public void setAccPurchaseOrderDAOobj(accPurchaseOrderDAO accPurchaseOrderDAOobj) {
        this.accPurchaseOrderDAOobj = accPurchaseOrderDAOobj;
    }
    
    public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }
    
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }

    public void setAccExportReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAO) {
        this.accExportReportsServiceDAO = accExportReportsServiceDAO;
    }
    
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    
    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }
    public void setaccDepreciationDAO(accDepreciationDAO accDepreciationObj) {
        this.accDepreciationObj = accDepreciationObj;
    }
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteobj) {
        this.accCreditNoteobj = accCreditNoteobj;
    }
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentDao) {
            this.accVendorPaymentDao = accVendorPaymentDao;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptDao) {
            this.accReceiptDao = accReceiptDao;
    }
    public void setAuthHandlerDAO(authHandlerDAO authHandlerDAO) {
    this.authHandlerDAO = authHandlerDAO;
}
    public void setExportRecord(ExportRecord ExportrecordObj) {
        this.ExportrecordObj = ExportrecordObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setaccCostCenterDAO (AccCostCenterDAO accCostCenterDAOObj) {
        this.accCostCenterObj = accCostCenterDAOObj;
    }

    public void setAccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptDAOObj){
        this.accGoodsReceiptDAOObj = accGoodsReceiptDAOObj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCommon) {
        this.accGoodsReceiptCommon = accGoodsReceiptCommon;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccTaxDAO (accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }
    
    public void setAccVendorDAOobj(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }
    
    public double getAccountBalanceMerged(HttpServletRequest request, String accountid, Date startDate, Date endDate, boolean eliminateflag) throws ServiceException, SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("costcenter", request.getParameter("costcenter"));
        requestParams.put(Constants.Acc_Search_Json ,request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria ,request.getParameter(InvoiceConstants.Filter_Criteria));
       // requestParams.put(Constants.moduleid ,"100");
        return getAccountBalanceMerged(request,requestParams, accountid, startDate, endDate, eliminateflag);
    }
        
    public double getAccountBalanceMerged(HttpServletRequest request, HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, boolean eliminateflag) throws ServiceException {
        double amount = 0;
        try {
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
            Account account = (Account) accresult.getEntityList().get(0);

            String costCenterId = (String)requestParams.get("costcenter");
            if(StringUtil.isNullOrEmpty(costCenterId)){ //Don't consider opening balance for CostCenter
                if(startDate!=null && ((startDate.before(account.getCreationDate()) || startDate.equals(account.getCreationDate())) && endDate.after(account.getCreationDate()) || endDate.equals(account.getCreationDate()))  ){
                    double accountOpeningBalance = accInvoiceCommon.getOpeningBalanceOfAccount(request, account,false,null);
//                    KwlReturnObject result = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,accountOpeningBalance,account.getCurrency().getCurrencyID(),account.getCreationDate(),0);
                    amount = accountOpeningBalance;//(Double) result.getEntityList().get(0);
                } 
                
            }
            String Searchjson = "";
            
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }            
            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = requestParams.get("searchJson").toString();
            }
            KwlReturnObject abresult = accJournalEntryobj.getAccountBalanceMerged(accountid, startDate, endDate, costCenterId, eliminateflag,filterConjuctionCriteria,Searchjson);
            List list = abresult.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JournalEntryDetail jed = (JournalEntryDetail) row[1];
                String fromcurrencyid = (jed.getJournalEntry().getCurrency() == null ? currency.getCurrencyID() : jed.getJournalEntry().getCurrency().getCurrencyID());
//            amount += CompanyHandler.getCurrencyToBaseAmount(session, request, ((Double) row[0]).doubleValue(), fromcurrencyid, jed.getJournalEntry().getEntryDate());
                KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ((Double) row[0]).doubleValue(), fromcurrencyid, jed.getJournalEntry().getEntryDate(), jed.getJournalEntry().getExternalCurrencyRate());
                amount += (Double) crresult.getEntityList().get(0);
            }
            if (itr.hasNext()) {
                amount += ((Double) itr.next()).doubleValue();
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAccountBalance : "+ex.getMessage(), ex);
        }
        return amount;
    }
       
     public double[] getOpeningBalancesWithDateMerged(HttpServletRequest request, String[] companyids, Date startDate, Date endDate, boolean eliminateflag) throws ServiceException, SessionExpiredException {
        double[] balances = {0, 0};
//        String query="from Account ac where ac.company.companyID=?";
//        List list = HibernateUtil.executeQuery(session, query, companyid);
        HashMap<String, Object> filterParams = new HashMap<String, Object>();
        String companyid = "";
        for(int cnt=0; cnt<companyids.length; cnt++) {
            filterParams.clear();
            companyid = companyids[cnt];
            request.setAttribute(Constants.companyKey, companyid);
            request.setAttribute(Constants.globalCurrencyKey, request.getParameter(Constants.globalCurrencyKey));
            filterParams.put("companyid", companyid);
            KwlReturnObject accresult = accAccountDAOobj.getAccountDatewiseMerged(companyid, startDate, endDate, eliminateflag);
            List list = accresult.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Account account = (Account) itr.next();
                Date createdOn = AccountingManager.resetTimeField(account.getCreationDate());
                Date toDate = AccountingManager.resetTimeField(endDate);
                if(toDate.compareTo(createdOn)<=0){
                    continue;
                }

    //            double bal= CompanyHandler.getCurrencyToBaseAmount(session,request,account.getOpeningBalance(),account.getCurrency().getCurrencyID(),account.getCreationDate());
                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                double accountOpeningBalance = accInvoiceCommon.getOpeningBalanceOfAccount(request, account,false,null);
               KwlReturnObject retObj = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,accountOpeningBalance,account.getCurrency().getCurrencyID(),account.getCreationDate(),0);
                double bal = accountOpeningBalance;//(Double) retObj.getEntityList().get(0);//account.getOpeningBalance();//
                if (bal > 0) {
                    balances[0] += bal;
                } else if (bal < 0) {
                    balances[1] += bal;
                }
            }
        }
        return balances;
    }

    public JSONObject getTradingMerged(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj=new JSONObject();
        try {
            String[] companyids = request.getParameter("companyids").split(",");
            double dtotal=0,ctotal=0;
            JSONArray jArrL=new JSONArray();
            JSONArray jArrR=new JSONArray();
            JSONObject objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Debit)</div>");
            objlast.put("fmt", "H");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Credit)</div>");
            objlast.put("fmt", "H");
//            jArrR.put(objlast);

//            dtotal=getTrading(session, request, Group.NATURE_EXPENSES, jArrL);
            dtotal=getTradingMerged(request, companyids, Group.NATURE_EXPENSES, jArrL);
//            ctotal=getTrading(session, request, Group.NATURE_INCOME, jArrR);
            ctotal=getTradingMerged(request, companyids, Group.NATURE_INCOME, jArrR);

            double balance=dtotal+ctotal;
            if(balance>0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", "Gross Loss");
                objlast.put("amount", balance);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal-=balance;
            }
            if(balance<0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("accountname", "Gross Profit");
                objlast.put("amount", -balance);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal-=balance;
            }
            objlast = new JSONObject();
            objlast.put("accountname", "Total Debit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Total Credit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", -ctotal);
            objlast.put("fmt", "T");
//            jArrR.put(objlast);

            JSONObject fobj=new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("["+dtotal+","+-ctotal+"]"));
            jobj.put("data", fobj);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTrading : "+ex.getMessage(), ex);
        }
        return jobj;
    }
    
    public double getTradingMerged(HttpServletRequest request, String[] companyids, int nature, JSONArray jArr) throws ServiceException, SessionExpiredException {
        double total = 0;
        try {
//            String companyid = AccountingManager.getCompanyidFromRequest(request);
            //To do - Need to change company id logic
            String companyid = "";
            boolean defaulttypeflag = true;
            KwlReturnObject plresult = accAccountDAOobj.getGroupForProfitNlossMerged(companyid, nature, true, defaulttypeflag);
            List list = plresult.getEntityList();
            Iterator itr = list.iterator();

            Date startDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
            Date endDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
            while (itr.hasNext()) {
                Group group = (Group) itr.next();
                total += formatGroupDetailsMerged(request, companyids, group, startDate, endDate, 0, false, jArr);
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getTrading : "+ex.getMessage(), ex);
        }
        return total;
    }

    private double formatGroupDetailsMerged(HttpServletRequest request, String[] companyids, Group group, Date startDate, Date endDate, int level, boolean isBalanceSheet, JSONArray jArr) throws ServiceException, SessionExpiredException, ParseException {
        double totalAmount = 0;
        boolean isDebit = false;
        try {
            if (isBalanceSheet) {
                if (group.getNature() == Group.NATURE_LIABILITY) {
                    isDebit = true;
                }
            } else if (group.getNature() == Group.NATURE_EXPENSES) {
                isDebit = true;
            }
            Set children = group.getChildren();
            JSONArray chArr = new JSONArray();
            HashMap<String, Object> filterParams = new HashMap<String, Object>();
            String companyid = "";
            HashMap<String, String> usedAccountsMap = new HashMap<String, String>();
            for(int cnt = 0; cnt < companyids.length; cnt++){
                filterParams.clear();
                companyid = companyids[cnt];
                request.setAttribute(Constants.companyKey, companyid);
                request.setAttribute(Constants.globalCurrencyKey, request.getParameter(Constants.globalCurrencyKey));
                filterParams.put("companyid", companyid);
                filterParams.put("groupid", group.getID());
                filterParams.put("parent", null);
                KwlReturnObject accresult = accAccountDAOobj.getAccountEntry(filterParams);
                List list = accresult.getEntityList();
                DateFormat sdf = authHandler.getDateOnlyFormat(request);
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Account account = (Account) itr.next();
                    if(!usedAccountsMap.containsKey(account.getID())) {
                        usedAccountsMap.put(account.getID(), account.getID());
                        totalAmount += formatAccountDetailsMerged(request, account, startDate, endDate, level + 1, isDebit, isBalanceSheet, chArr, sdf, usedAccountsMap);
                    }
                }
                //To do - Need to uncomment this for custom account types.
//                if (children != null && !children.isEmpty()) {
//                    itr = children.iterator();
//                    while (itr.hasNext()) {
//                        Group child = (Group) itr.next();
//    //                    totalAmount+=formatGroupDetails(session,request, companyid, child, startDate, endDate, level+1, isBalanceSheet, chArr);
//                        totalAmount += formatGroupDetailsMerged(request, companyid, child, startDate, endDate, level + 1, isBalanceSheet, chArr);
//                    }
//                }
            }

            if (chArr.length() > 0) {
                JSONObject obj = new JSONObject();
                obj.put("accountname", group.getName());
                obj.put("accountid", group.getID());
                obj.put("level", level);
                obj.put("leaf", false);
                obj.put("amount", "");
                obj.put("isdebit", isDebit);
                obj.put("acctype", (group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                obj.put("group", (group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));
                jArr.put(obj);
                for (int i = 0; i < chArr.length(); i++) {
                    jArr.put(chArr.getJSONObject(i));
                }

                obj = new JSONObject();
                obj.put("accountname", "Total " + group.getName());
                obj.put("accountid", group.getID());
                obj.put("level", level);
                obj.put("leaf", true);
                obj.put("show", true);
                obj.put("acctype", (group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                obj.put("group", (group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));
                double ta = totalAmount;
                if (!isDebit) {
                    ta = -ta;
                }
                if (isBalanceSheet) {
                    ta = -ta;
                }
                obj.put("amount", ta);
                obj.put("isdebit", isDebit);
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("formatGroupDetails : " + ex.getMessage(), ex);
        }
        return totalAmount;
    }
    
    private double formatAccountDetailsMerged(HttpServletRequest request, Account account, Date startDate, Date endDate, int level, boolean isDebit, boolean isBalanceSheet, JSONArray jArr, DateFormat sdf, HashMap<String, String> usedAccountsMap) throws ServiceException, SessionExpiredException, ParseException {
//        double amount = getAccountBalance(session, request, account.getID(), startDate, endDate);
        boolean isDeleted = false;
        boolean eliminateflag = false;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        if(account.isDeleted()){ //BUG #16733: Deleted account check for sub Assets/Account
            isDeleted = true;
        }
        if(account.isEliminateflag()){ //Dont consider amount of eliminated accounts.
            eliminateflag = true;
        }
        
        //Logic to get new fixed asset group from old fixed asset id
        //To do - Need to test wheteher is working or not
        String fixedAssetgrp = "";
        Group group = accAccountDAOobj.getNewGroupFromOldId(Group.FIXED_ASSETS, account.getCompany().getCompanyID());
        if (group != null) {
            fixedAssetgrp = group.getID();
        }
        if(account.getGroup()!= null && account.getGroup().getID().equalsIgnoreCase(fixedAssetgrp)){ //BUG Fixed #16739 : Creation date check for Fixed Assets
            Date createdOn = AccountingManager.resetTimeField(account.getCreationDate());
            Date toDate = AccountingManager.resetTimeField(endDate);
            if(toDate.compareTo(createdOn)<=0){
                isDeleted = true;
            }
        }        
        double amount = 0;
        if(!isDeleted){
            amount = getAccountBalanceMerged(request, account.getID(), startDate, endDate, true);
            amount = authHandler.round(amount, companyid);
        }
        double totalAmount = amount;
        String accname = StringUtil.isNullOrEmpty(account.getAcccode())?account.getName():"["+account.getAcccode()+"] "+account.getName();
        Iterator<Account> itr = account.getChildren().iterator();
        JSONArray chArr = new JSONArray();
        while (itr.hasNext()) {
            Account child = itr.next();
            if(!usedAccountsMap.containsKey(child.getID())) {
                usedAccountsMap.put(child.getID(), child.getID());
                totalAmount += formatAccountDetailsMerged(request, child, startDate, endDate, level + 1, isDebit, isBalanceSheet, chArr, sdf, usedAccountsMap);
            }
        }
         
        //Fetched Mapped accounts data for multi company feature.
        List mapaccresult = accAccountDAOobj.getMappedAccountsForReports(account.getID());
        Iterator<Object[]> itr1 = mapaccresult.iterator();
        JSONArray chArr1 = new JSONArray();
        String childaccountid = "";
        KwlReturnObject childObj = null;
        Account child = null;
        String parentaccountid = "";
        String groupaccname = "";
        String groupacccode = "";
        double mappedAmount = 0;
//        if(accname.equals("Cash in hand")) {
//            accname = accname;
//        }
        while (itr1.hasNext()) {
           Object[] row = (Object[]) itr1.next();
           childaccountid = row[0].toString();
           if(!usedAccountsMap.containsKey(childaccountid)) {
               usedAccountsMap.put(childaccountid, childaccountid);
               childObj = accountingHandlerDAOobj.getObject(Account.class.getName(), childaccountid);
               child = (Account) childObj.getEntityList().get(0);
               parentaccountid = row[1].toString();
               groupaccname = (row[2]!=null)?row[2].toString():"";
               groupacccode = (row[3]!=null)?row[3].toString():"";
               mappedAmount += formatAccountDetailsMerged(request, child, startDate, endDate, level + 1, isDebit, isBalanceSheet, chArr1, sdf, usedAccountsMap);
           }
        }
        totalAmount += mappedAmount;
        amount += mappedAmount;
        Date creationDate = sdf.parse(sdf.format(account.getCreationDate()));

        try {
            groupaccname = StringUtil.isNullOrEmpty(groupacccode)?groupaccname:"["+groupacccode+"] "+groupaccname;
            accname = (!StringUtil.isNullOrEmpty(groupaccname)?groupaccname:accname);
            String accid = (!StringUtil.isNullOrEmpty(parentaccountid)?parentaccountid:account.getID());
            boolean accmappedflag = (!StringUtil.isNullOrEmpty(groupaccname)?true:false);
            if (chArr.length() > 0) {
                JSONObject obj = new JSONObject();
//                obj.put("accountname", accname);
//                obj.put("accountid", account.getID());
                obj.put("accountname", accname);
                obj.put("accountid", accid);
                obj.put("accmappedflag", accmappedflag);
                obj.put("level", level);
                obj.put("leaf", false);
                obj.put("amount", "");
                obj.put("isdebit", isDebit);
                obj.put("accountflag", true);                
                obj.put("acctype", (account.getGroup().getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                obj.put("group", (account.getGroup().getID().toString().equals("15"))?"income":((account.getGroup().getID().toString().equals("8"))?"expense":""));
                jArr.put(obj);
                for (int i = 0; i < chArr.length(); i++) {
                    jArr.put(chArr.getJSONObject(i));
                }

                if (amount != 0) {
                    obj = new JSONObject();
                    obj.put("accountname", "Other " + accname);
                    obj.put("accountid", accid);
                    obj.put("accmappedflag", accmappedflag);
                    obj.put("level", level + 1);
                    obj.put("leaf", true);
                    if (!isDebit) {
                        amount = -amount;
                    }
                    if (isBalanceSheet) {
                        amount = -amount;
                    }
                    obj.put("amount", amount);
                    obj.put("isdebit", isDebit);
                    obj.put("accountflag", true);
                    obj.put("acctype", (account.getGroup().getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                    obj.put("group", (account.getGroup().getID().toString().equals("15"))?"income":((account.getGroup().getID().toString().equals("8"))?"expense":""));
                    jArr.put(obj);
                }

                obj = new JSONObject();
                obj.put("accountname", "Total " + accname);
                obj.put("accountid", accid);
                obj.put("accmappedflag", accmappedflag);
                obj.put("level", level);
                obj.put("leaf", true);
                obj.put("show", true);
//                obj.put("accountflag", true);
                double ta = totalAmount;
                if (!isDebit) {
                    ta = -ta;
                }
                if (isBalanceSheet) {
                    ta = -ta;
                }
                obj.put("amount", ta);
                obj.put("isdebit", isDebit);
                obj.put("acctype", (account.getGroup().getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                obj.put("group", (account.getGroup().getID().toString().equals("15"))?"income":((account.getGroup().getID().toString().equals("8"))?"expense":""));
                jArr.put(obj);
            } else if (amount != 0 /*&& (endDate.after(creationDate) )*/) {
                JSONObject obj = new JSONObject();
                obj.put("accountname", accname);
                obj.put("accountid", accid);
                obj.put("accmappedflag", accmappedflag);
                obj.put("level", level);
                obj.put("leaf", true);
                if (!isDebit) {
                    amount = -amount;
                }
                if (isBalanceSheet) {
                    amount = -amount;
                }
                obj.put("amount", (amount != 0.0 ? amount : ""));
                obj.put("isdebit", isDebit);
                obj.put("accountflag", true);
                obj.put("acctype", (account.getGroup().getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                obj.put("group", (account.getGroup().getID().toString().equals("15"))?"income":((account.getGroup().getID().toString().equals("8"))?"expense":""));
                jArr.put(obj);
            } else {
                return 0;
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("formatAccountDetails : " + e.getMessage(), e);
        }
        return totalAmount;
    }
    
    public ModelAndView generateIBGFile(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        try {
            accReportsService.generateIBGFile(request, response);
        } catch (SessionExpiredException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }
    
    // Method for generating the GIRO file for CIMB bank
    public ModelAndView generateGIORFileForCIMBBank(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        try {
            accReportsService.generateIBGFileForCIMBbank(request, response);
        } catch (SessionExpiredException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }
    
    public ModelAndView getProfitLossMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
//            createUserSession(request);
            jobj = getProfitLossMerged(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getProfitLossMerged(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            double dtotal = 0, ctotal = 0;
            JSONArray jArrL = new JSONArray();
            JSONArray jArrR = new JSONArray();
            JSONObject objlast = new JSONObject();
            String[] companyids = request.getParameter("companyids").split(",");
            double balance = getTradingMerged(request, companyids, Group.NATURE_EXPENSES, new JSONArray()) +
                    getTradingMerged(request, companyids, Group.NATURE_INCOME, new JSONArray());
            objlast.put("accountname", messageSource.getMessage("", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.3", null, RequestContextUtils.getLocale(request))+"</div>");   //Amount (Debit)
            objlast.put("fmt", "H");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.4", null, RequestContextUtils.getLocale(request))+"</div>");   // Amount (Credit)
            objlast.put("fmt", "H");
//            jArrR.put(objlast);
            if (balance > 0) {
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Gross Loss");
                objlast.put("amount", balance);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal = balance;
            }
            if (balance < 0) {
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", messageSource.getMessage("acc.report.6", null, RequestContextUtils.getLocale(request)));  //"Gross Profit");
                objlast.put("amount", -balance);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal = balance;
            }

//            dtotal += getProfitLoss(session, request, Group.NATURE_EXPENSES, jArrL);
            dtotal += getProfitLossMerged(request, companyids, Group.NATURE_EXPENSES, jArrL);
//            ctotal += getProfitLoss(session, request, Group.NATURE_INCOME, jArrR);
            ctotal += getProfitLossMerged(request, companyids, Group.NATURE_INCOME, jArrR);

            balance = dtotal + ctotal;
            if (balance > 0) {
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", messageSource.getMessage("acc.report.9", null, RequestContextUtils.getLocale(request)));  //"Net Loss");
                objlast.put("amount", balance);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal -= balance;
            }
            if (balance < 0) {
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("accountname", messageSource.getMessage("acc.report.10", null, RequestContextUtils.getLocale(request)));  //"Net Profit");
                objlast.put("amount", -balance);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal -= balance;
            }
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.7", null, RequestContextUtils.getLocale(request)));  //"Total Debit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.8", null, RequestContextUtils.getLocale(request)));  //"Total Credit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", -ctotal);
            objlast.put("fmt", "T");
//            jArrR.put(objlast);

            JSONObject fobj = new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("[" + dtotal + "," + -ctotal + "]"));
            jobj.put("data", fobj);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getProfitLoss : " + ex.getMessage(), ex);
        }
        return jobj;
    }
 
    public double getProfitLossMerged(HttpServletRequest request, String[] companyids, int nature, JSONArray jArr) throws ServiceException, SessionExpiredException {
        double total = 0;
        try {
            //String companyid = AccountingManager.getCompanyidFromRequest(request);
            //To do - Need to change company id logic
            String companyid = "";
            boolean defaulttypeflag = true;
            KwlReturnObject plresult = accAccountDAOobj.getGroupForProfitNlossMerged(companyid, nature, false, defaulttypeflag);
            List list = plresult.getEntityList();
            Iterator itr = list.iterator();

            Date startDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
            Date endDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
            while (itr.hasNext()) {
                Group group = (Group) itr.next();
                total += formatGroupDetailsMerged(request, companyids, group, startDate, endDate, 0, false, jArr);
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getProfitLoss : " + ex.getMessage(), ex);
        }
        return total;
    }

    public ModelAndView getTradingAndProfitLossMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
//            createUserSession(request);
            if(request.getParameter("singleGrid") != null && Boolean.parseBoolean(request.getParameter("singleGrid").toString())) {
                jobj = getTradingAndProfitLossforExportMerged(request);
                jobj = getNewMYOBtradingreport(request, jobj, false);
            } else
                jobj = getTradingAndProfitLossMerged(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView exportTradingAndProfitLossMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
//            createUserSession(request);
            String fileType = request.getParameter("filetype");
            boolean periodView=Boolean.parseBoolean(request.getParameter("periodView"));
            if (StringUtil.equal(fileType, "print")) {
                if(request.getParameter("singleGrid") != null && Boolean.parseBoolean(request.getParameter("singleGrid").toString()))
                    jobj = getNewMYOBtradingreport(request, getTradingAndProfitLossforExportMerged(request), true); //getExportBalanceSheetJSON(getTradingAndProfitLossforExport(request),2, 0);
                else
                    jobj = getExportBalanceSheetJSON(getTradingAndProfitLossMerged(request),2, 0,periodView);
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
                exportDaoObj.processRequest(request, response, jobj);
            } else {
                if(fileType.equals("csv")){
                    if(request.getParameter("singleGrid") != null && Boolean.parseBoolean(request.getParameter("singleGrid").toString()))
                        jobj = getNewMYOBtradingreport(request, getTradingAndProfitLossforExportMerged(request), true); //getExportBalanceSheetJSON(getTradingAndProfitLossforExport(request),2, 0);
                    else
                        jobj = getExportBalanceSheetJSON(getTradingAndProfitLossMerged(request),2, 0,periodView);
                    exportDaoObj.processRequest(request, response, jobj);
                }
                else{
                    jobj = getTradingAndProfitLossforExportMerged(request);
                    String currencyid = sessionHandlerImpl.getCurrencyID(request);
                    java.text.DateFormat formatter = authHandler.getDateOnlyFormatter(request);
                    String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                    java.io.ByteArrayOutputStream baos = null;
                    String filename = request.getParameter("filename");                    
                    String comName = sessionHandlerImpl.getCompanyName(request);
                    Date endDate=authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
                    Date startDate=authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
                    Calendar cal = Calendar.getInstance();
                    String comAdd = authHandlerDAO.getCompanyAddress(sessionHandlerImpl.getCompanyid(request));
                    cal.set(1900+endDate.getYear(), endDate.getMonth(), endDate.getDate());
                    cal.add(Calendar.DAY_OF_MONTH, -1);             
                    String endDateString = authHandler.getDateOnlyFormat().format(cal.getTime());
                    endDate = authHandler.getDateOnlyFormat().parse(endDateString);
                    //endDate = cal.getTime(); //Get actual end date i.e. 1 day before given date
                    baos = ExportrecordObj.exportTradingPdf(request, currencyid, formatter, logoPath, comName, jobj,startDate,endDate,2, 0, comAdd,null);
                    if (baos != null) {
                        ExportrecordObj.writeDataToFile(filename+"."+fileType, baos, response);
                    }

                }
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public JSONObject getTradingAndProfitLossMerged(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        double invOpeBal=0, invCloseBal=0, assemblyValuation=0;
        try {
            String costCenterId = request.getParameter("costcenter"); //Filter for costcenter
            String reportView = request.getParameter("reportView"); //"TradingAndProfitLoss","CostCenter"
            String companyid = "";
            double dtotal = 0, ctotal = 0;
            JSONArray jArrL = new JSONArray();
            JSONArray jArrR = new JSONArray();
            JSONObject objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.3", null, RequestContextUtils.getLocale(request))+"</div>");    // Amount (Debit)
            objlast.put("fmt", "H");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.4", null, RequestContextUtils.getLocale(request))+"</div>");      //Amount (Credit)
            objlast.put("fmt", "H");
//            jArrR.put(objlast);
            Date startDate=authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
            Date endDate=authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
            
            String[] companyids = request.getParameter("companyids").split(",");
            
            if(!"CostCenter".equalsIgnoreCase(reportView) && StringUtil.isNullOrEmpty(costCenterId)){ //Don't show Opening/Closing Stock for any Cost-Center
                
                for(int cnt = 0; cnt < companyids.length; cnt++){
                    companyid = companyids[cnt];
                    request.setAttribute("companyid", companyid);
                    
                    JSONObject jObjX= getInventoryOpeningBalance(request, companyid, startDate);                
                    JSONArray jarr = jObjX.getJSONArray("data");
                    if(jarr.length() > 0) {
                        JSONObject jobj1 = jarr.getJSONObject(0);
                        invOpeBal = jobj1.has("valuation")?jobj1.getDouble("valuation"):0;
                    }
                    jObjX=new JSONObject();
                    request.setAttribute("assemblyValuation", true);
                    jObjX =getInventoryOpeningBalance(request, companyid, endDate);
                    jarr = jObjX.getJSONArray("data");
                    if(jarr.length() > 0) {
                        JSONObject jobj1 = jarr.getJSONObject(0);
                        invCloseBal = jobj1.has("valuation")?jobj1.getDouble("valuation"):0;
                        assemblyValuation = jobj1.has("assemblyValuation")?jobj1.getDouble("assemblyValuation"):0;
                    }
                }
                
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.13", null, RequestContextUtils.getLocale(request)));
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", invOpeBal);
                objlast.put("fmt", "H");
                jArrL.put(objlast);
            }
//            dtotal = getTrading(session, request, Group.NATURE_EXPENSES, jArrL);
            dtotal = getTradingMerged(request, companyids, Group.NATURE_EXPENSES, jArrL);
//            ctotal = getTrading(session, request, Group.NATURE_INCOME, jArrR);
            ctotal = getTradingMerged(request, companyids, Group.NATURE_INCOME, jArrR);

            dtotal+=invOpeBal;
            ctotal-=invCloseBal;

            if(!"CostCenter".equalsIgnoreCase(reportView) && StringUtil.isNullOrEmpty(costCenterId)){ //Don't show Opening/Closing Stock for any Cost-Center
                JSONObject obj = new JSONObject();
                if(invCloseBal+assemblyValuation > 0){ //Show details if Closing_Stock > 0
                    obj.put("accountname", messageSource.getMessage("acc.report.17", null, RequestContextUtils.getLocale(request)));  //"Closing Stock");
                    obj.put("accountid", "");
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", false);
                    obj.put("amount", "");
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname",messageSource.getMessage("acc.report.14", null, RequestContextUtils.getLocale(request)));  // "Finish Products (Total Value of \"Inventory Assembly\" products)");
                    obj.put("accountid", "");
                    obj.put("level", 1);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", assemblyValuation);
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname", messageSource.getMessage("acc.report.15", null, RequestContextUtils.getLocale(request)));  //"Raw Materials (Total Value of \"Inventory Item\" products)");
                    obj.put("accountid", "");
                    obj.put("level", 1);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", invCloseBal-assemblyValuation);
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname", messageSource.getMessage("acc.report.16", null, RequestContextUtils.getLocale(request)));  //"Total Closing Stock");
                    obj.put("accountid", "");
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", invCloseBal);
                    objlast.put("fmt", "H");
                    jArrR.put(obj);
                } else { // Show single line if Closing_Stock = 0
                    obj = new JSONObject();
                    obj.put("accountname",messageSource.getMessage("acc.report.17", null, RequestContextUtils.getLocale(request)));  // "Closing Stock");
                    obj.put("accountid", "");
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", invCloseBal);
                    obj.put("fmt", "H");
                    jArrR.put(obj);
                }
            }

            if(!"CostCenter".equalsIgnoreCase(reportView)) {//Don't Adjust report layout for cost center report
                int len = jArrL.length() - jArrR.length(); //Adjust report layout by equaling no. of rows
                JSONArray jArr = jArrR;
                if (len < 0) {
                    len = -len;
                    jArr = jArrL;
                }
                for (int i = 0; i < len; i++) {
                    jArr.put(new JSONObject());
                }
            }

            double balance = dtotal + ctotal;
            if(!"CostCenter".equalsIgnoreCase(reportView)) {//Don't show GrossLoss,GrossProfit for cost center report
                if (balance > 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", false);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.5", null, RequestContextUtils.getLocale(request)));  //"Gross Loss");
                    objlast.put("amount", balance);
                    objlast.put("fmt", "B");
                    jArrR.put(objlast);
                    jArrL.put(new JSONObject());
                    ctotal -= balance;
                }
                if (balance < 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.6", null, RequestContextUtils.getLocale(request)));  //"Gross Profit");
                    objlast.put("amount", balance==0?balance:-balance);//Remove '-' sign if 0
                    objlast.put("fmt", "B");
                    jArrL.put(objlast);
                    jArrR.put(new JSONObject());
                    dtotal -= balance;
                }
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.7", null, RequestContextUtils.getLocale(request)));  //"Total Debit");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", dtotal);
                objlast.put("fmt", "T");
                jArrL.put(objlast);
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.8", null, RequestContextUtils.getLocale(request)));  //"Total Credit");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("amount", ctotal==0?ctotal:-ctotal);//Remove '-' sign if 0
                objlast.put("fmt", "T");
                jArrR.put(objlast);

                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.3", null, RequestContextUtils.getLocale(request))+"</div>");   //Amount (Debit)
                objlast.put("fmt", "H");
                jArrL.put(objlast);
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.4", null, RequestContextUtils.getLocale(request))+"</div>");       //Amount (Credit)
                objlast.put("fmt", "H");
                jArrR.put(objlast);
                dtotal = 0;
                ctotal = 0;
                if (balance > 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.5", null, RequestContextUtils.getLocale(request)));  //"Gross Loss");
                    objlast.put("amount", balance);
                    objlast.put("fmt", "B");
                    jArrL.put(objlast);
                    dtotal = balance;
                }
                if (balance < 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", false);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.6", null, RequestContextUtils.getLocale(request)));  //"Gross Profit");
                    objlast.put("amount", balance==0?balance:-balance);//Remove '-' sign if 0
                    objlast.put("fmt", "B");
                    jArrR.put(objlast);
                    ctotal = balance;
                }
            }
            
            dtotal += getProfitLossMerged(request, companyids, Group.NATURE_EXPENSES, jArrL);
            ctotal += getProfitLossMerged(request, companyids, Group.NATURE_INCOME, jArrR);

            if(!"CostCenter".equalsIgnoreCase(reportView)) { //Don't show NetLoss,NetProfit for cost center report
                balance = dtotal + ctotal;
                if (balance > 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", false);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.9", null, RequestContextUtils.getLocale(request)));  //"Net Loss");
                    objlast.put("amount", balance);
                    objlast.put("fmt", "B");
                    jArrR.put(objlast);
                    ctotal -= balance;
                }
                if (balance < 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.10", null, RequestContextUtils.getLocale(request)));  //"Net Profit");
                    objlast.put("amount", balance==0?balance:-balance);//Remove '-' sign if 0
                    objlast.put("fmt", "B");
                    jArrL.put(objlast);
                    dtotal -= balance;
                }
            }

            if ("CostCenter".equalsIgnoreCase(reportView)) { //Add LIABILITY for cost center report (Tax Amount)
//                KwlReturnObject ret =  accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Group", Group.OTHER_CURRENT_LIABILITIES);
//                if(!ret.getEntityList().isEmpty()){
                //Logic to get new OTHER_CURRENT_LIABILITIES group from old OTHER_CURRENT_LIABILITIES
                //To do - Need to test wheteher is working or not

                Group liab_group = accAccountDAOobj.getNewGroupFromOldId(Group.OTHER_CURRENT_LIABILITIES, companyid);
                if (liab_group != null) {
                    ctotal += formatGroupDetailsMerged(request, companyids, liab_group, startDate, endDate, 0, true, jArrR); //Bug Fixed #16746
                    liab_group.getName();
                }
            }

            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.7", null, RequestContextUtils.getLocale(request)));  //"Total Debit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.8", null, RequestContextUtils.getLocale(request)));  //"Total Credit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", ctotal==0?ctotal:-ctotal);//Remove '-' sign if 0
            objlast.put("fmt", "T");
//            jArrR.put(objlast);

            JSONObject fobj = new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("[" + dtotal + "," + (ctotal==0?ctotal:-ctotal) + "]"));
            jobj.put("data", fobj);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : "+ex.getMessage(), ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : "+e.getMessage(), e);
        }
        return jobj;
    }

    public ModelAndView getBalanceSheetMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
//            createUserSession(request);
            jobj = getBalanceSheetMerged(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
 
    public JSONObject getExportBalanceSheetJSON(JSONObject jobj, int flag, int toggle, boolean periodView) {
        JSONObject retObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        JSONArray rightObjArr = new JSONArray();
        JSONArray leftObjArr = new JSONArray();
        try {
            jobj = jobj.getJSONObject("data");
            if (toggle == 0) {
                rightObjArr = jobj.getJSONArray("right");
                leftObjArr = jobj.getJSONArray("left");
            } else {
                rightObjArr = jobj.getJSONArray("left");
                leftObjArr = jobj.getJSONArray("right");
            }
            int length = leftObjArr.length() > rightObjArr.length() ? leftObjArr.length() : rightObjArr.length();
            for (int i = 0; i < length; i++) {
                JSONObject tempObj = new JSONObject();
                if (i < leftObjArr.length() && !leftObjArr.getJSONObject(i).toString().equalsIgnoreCase("{}")) {
                    JSONObject leftObj = leftObjArr.getJSONObject(i);
                    if (periodView) {
                        tempObj.put("laccountname", leftObj.get("accountname"));
                        tempObj.put("laccountid", leftObj.get("accountid"));
                        tempObj.put("llevel", leftObj.get("level"));
                        tempObj.put("lisdebit", leftObj.get("isdebit"));
                        tempObj.put("lleaf", leftObj.get("leaf"));
                        tempObj.put("lopeningamount", com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.get("openingamount").toString()));
                        tempObj.put("lperiodamount", com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.get("periodamount").toString()));
                        tempObj.put("lendingamount", com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.get("endingamount").toString()));
                    } else {
                        tempObj.put("laccountname", leftObj.get("accountname"));
                        tempObj.put("laccountid", leftObj.get("accountid"));
                        tempObj.put("llevel", leftObj.get("level"));
                        tempObj.put("lisdebit", leftObj.get("isdebit"));
                        tempObj.put("lleaf", leftObj.get("leaf"));
                        tempObj.put("lamount", com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.get("amount").toString()));/*
                         * tempObj.put("lfmt",leftObj.get("fmt"));
                         */
                    }
                } else {
                    if (periodView) {
                        tempObj.put("laccountname", "");
                        tempObj.put("laccountid", "");
                        tempObj.put("llevel", "");
                        tempObj.put("lisdebit", "");
                        tempObj.put("lleaf", "");
                        tempObj.put("lopeningamount", "");
                        tempObj.put("lperiodamount", "");
                        tempObj.put("lendingamount", "");
                    } else {
                        tempObj.put("laccountname", "");
                        tempObj.put("laccountid", "");
                        tempObj.put("llevel", "");
                        tempObj.put("lisdebit", "");
                        tempObj.put("lleaf", "");
                        tempObj.put("lamount", "");
                    }
                }
                if (i < rightObjArr.length() && !rightObjArr.getJSONObject(i).toString().equalsIgnoreCase("{}")) {
                    JSONObject rightObj = rightObjArr.getJSONObject(i);
                    if (periodView) {
                        tempObj.put("raccountname", rightObj.get("accountname"));
                        tempObj.put("raccountid", rightObj.get("accountid"));
                        tempObj.put("rlevel", rightObj.get("level"));
                        tempObj.put("risdebit", rightObj.get("isdebit"));
                        tempObj.put("rleaf", rightObj.get("leaf"));
                        tempObj.put("ropeningamount", com.krawler.common.util.StringUtil.serverHTMLStripper(rightObj.get("openingamount").toString()));
                        tempObj.put("rperiodamount", com.krawler.common.util.StringUtil.serverHTMLStripper(rightObj.get("openingamount").toString()));
                        tempObj.put("rendingamount", com.krawler.common.util.StringUtil.serverHTMLStripper(rightObj.get("openingamount").toString()));
                    } else {
                        tempObj.put("raccountname", rightObj.get("accountname"));
                        tempObj.put("raccountid", rightObj.get("accountid"));
                        tempObj.put("rlevel", rightObj.get("level"));
                        tempObj.put("risdebit", rightObj.get("isdebit"));
                        tempObj.put("rleaf", rightObj.get("leaf"));
                        tempObj.put("ramount", com.krawler.common.util.StringUtil.serverHTMLStripper(rightObj.get("amount").toString()));/*
                         * tempObj.put("rfmt",rightObj.get("fmt"));
                         */                        
                    }
                } else {
                    if (periodView) {
                        tempObj.put("raccountname", "");
                        tempObj.put("raccountid", "");
                        tempObj.put("rlevel", "");
                        tempObj.put("risdebit", "");
                        tempObj.put("rleaf", "");
                        tempObj.put("ramount", "");  
                        tempObj.put("ropeningamount","");
                        tempObj.put("rperiodamount","");
                        tempObj.put("rendingamount","");
                    } else {
                        tempObj.put("raccountname", "");
                        tempObj.put("raccountid", "");
                        tempObj.put("rlevel", "");
                        tempObj.put("risdebit", "");
                        tempObj.put("rleaf", "");
                        tempObj.put("ramount", "");
                    }
                }
                jArr.put(tempObj);
            }
            if (flag != -1) {
                double totalAsset = 0, totalLibility = 0;
                JSONArray finalValArr = jobj.getJSONArray("total");
                totalAsset = Double.parseDouble(finalValArr.getString(0));
                totalLibility = Double.parseDouble(finalValArr.getString(1));
                String leftSummaryHeader = "", rightSummaryHeader = "";
                if (flag == 1) {
                    if (toggle == 0) {
                        leftSummaryHeader = "Total Liability";
                        rightSummaryHeader = "Total Asset";
                    } else {
                        leftSummaryHeader = "Total Asset";
                        rightSummaryHeader = "Total Liability";
                    }
                } else if (flag == 2) {
                    leftSummaryHeader = "Total Debit";
                    rightSummaryHeader = "Total Credit";
                }
                if (periodView) {
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("laccountname", leftSummaryHeader);
                    tempObj.put("laccountid", "");
                    tempObj.put("llevel", "");
                    tempObj.put("lisdebit", "");
                    tempObj.put("lleaf", "");
                    tempObj.put("lopeningamount", "");
                    tempObj.put("lperiodamount",totalAsset);
                    tempObj.put("lendingamount","");
                    
                    tempObj.put("raccountname", rightSummaryHeader);
                    tempObj.put("raccountid", "");
                    tempObj.put("rlevel", "");
                    tempObj.put("risdebit", "");
                    tempObj.put("rleaf", "");
                    tempObj.put("ramount", "");
                    tempObj.put("rperiodamount",totalLibility);
                    tempObj.put("rendingamount","");
                    jArr.put(tempObj);
                } else {
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("laccountname", leftSummaryHeader);
                    tempObj.put("laccountid", "");
                    tempObj.put("llevel", "");
                    tempObj.put("lisdebit", "");
                    tempObj.put("lleaf", "");
                    tempObj.put("lamount", totalAsset);
                    
                    tempObj.put("raccountname", rightSummaryHeader);
                    tempObj.put("raccountid", "");
                    tempObj.put("rlevel", "");
                    tempObj.put("risdebit", "");
                    tempObj.put("rleaf", "");
                    tempObj.put("ramount", totalLibility);
                    jArr.put(tempObj);
                }
                
            }
            retObj.put("data", jArr);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retObj;
    }

     public ModelAndView exportBalanceSheetMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
//            createUserSession(request);
            String fileType = request.getParameter("filetype");
            boolean periodView=request.getParameter("periodView")!=null?Boolean.parseBoolean(request.getParameter("periodView")):false;
            if (StringUtil.equal(fileType, "print")) {
                jobj = getExportBalanceSheetJSON(getBalanceSheetMerged(request),1, Integer.parseInt(request.getParameter("toggle")),periodView);
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
                exportDaoObj.processRequest(request, response, jobj);
            } else {
                if (StringUtil.equal(fileType, "csv")) {
                    jobj = getExportBalanceSheetJSON(getBalanceSheetMerged(request),1,Integer.parseInt(request.getParameter("toggle")),periodView);
                    exportDaoObj.processRequest(request, response, jobj);
                }else{
                    jobj = getBalanceSheetMerged(request);
                    String currencyid = sessionHandlerImpl.getCurrencyID(request);
                    java.text.DateFormat formatter = authHandler.getDateOnlyFormatter(request);
                    String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                    java.io.ByteArrayOutputStream baos = null;
                    String filename = request.getParameter("filename");                       
                    String comName = sessionHandlerImpl.getCompanyName(request);
                    String comAdd = authHandlerDAO.getCompanyAddress(sessionHandlerImpl.getCompanyid(request));

                    Date endDate=authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
                    Calendar cal = Calendar.getInstance();                    
                    cal.set(1900+endDate.getYear(), endDate.getMonth(), endDate.getDate());
                    cal.add(Calendar.DAY_OF_MONTH, -1);                                 
                    String endDateString = authHandler.getDateOnlyFormat().format(cal.getTime());
                    endDate = authHandler.getDateOnlyFormat().parse(endDateString);

                    baos = ExportrecordObj.exportBalanceSheetPdf(request, currencyid, formatter, logoPath, comName, jobj,null,endDate,1, Integer.parseInt(request.getParameter("toggle")), comAdd,endDate);
                    if (baos != null) {
                        ExportrecordObj.writeDataToFile(filename+"."+fileType, baos, response);
                    }

                }
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
     
    public JSONObject getBalanceSheetMerged(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            double dtotal=0, ctotal=0, invCloseBal=0,invOpeBal=0;            
            String[] companyids = request.getParameter("companyids").split(",");
            String companyid = "";
            Date startDate=authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
            Date endDate=authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);                
                JSONObject jObjX= getInventoryOpeningBalance(request, companyid, startDate);                
                JSONArray jarr = jObjX.getJSONArray("data");
                if(jarr.length() > 0) {
                    JSONObject jobj1 = jarr.getJSONObject(0);
                    invOpeBal = jobj1.has("valuation")?jobj1.getDouble("valuation"):0;
                }
                invOpeBal=authHandler.round(invOpeBal, companyid);
                jObjX=new JSONObject();
                jObjX =getInventoryOpeningBalance(request, companyid, endDate);
                jarr = jObjX.getJSONArray("data");
                if(jarr.length() > 0) {
                    JSONObject jobj1 = jarr.getJSONObject(0);
                    invCloseBal = jobj1.has("valuation")?jobj1.getDouble("valuation"):0;
                }
                invCloseBal=authHandler.round(invCloseBal, companyid);
            }
            companyid = sessionHandlerImpl.getCompanyid(request);
            double balance = getTradingMerged(request, companyids, Group.NATURE_EXPENSES, new JSONArray()) -invCloseBal +
                    getTradingMerged(request, companyids, Group.NATURE_INCOME, new JSONArray()) +
                    getProfitLossMerged(request, companyids, Group.NATURE_EXPENSES, new JSONArray()) +
                    getProfitLossMerged(request, companyids, Group.NATURE_INCOME, new JSONArray()) +invOpeBal;
            balance=authHandler.round(balance, companyid);
            JSONArray jArrL = new JSONArray();
            JSONArray jArrR = new JSONArray();
            JSONObject objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+ messageSource.getMessage("acc.report.18", null, RequestContextUtils.getLocale(request))+"</div>");   //       Amount (Assets)
            objlast.put("fmt", "H");
//            jArrR.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+ messageSource.getMessage("acc.report.19", null, RequestContextUtils.getLocale(request))+"</div>");     //Amount (Liabilities)
            objlast.put("fmt", "H");
//            jArrL.put(objlast);

//            dtotal=-getBalanceSheet(session, request, Group.NATURE_LIABILITY, jArrL);
            dtotal = -getBalanceSheetMerged(request, companyids, Group.NATURE_LIABILITY, jArrL);
//            ctotal=-getBalanceSheet(session, request, Group.NATURE_ASSET, jArrR);
            ctotal = -getBalanceSheetMerged(request, companyids, Group.NATURE_ASSET, jArrR);

            System.out.println(dtotal + "," + ctotal);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.17", null, RequestContextUtils.getLocale(request)));  //"Closing Stock");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", invCloseBal);
            objlast.put("fmt", "H");
            jArrR.put(objlast);
            
            objlast = new JSONObject();
            objlast.put("accountname", "Opening Stock");  //"Closing Stock");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", invOpeBal);
            objlast.put("fmt", "H");
            jArrL.put(objlast);
            dtotal += invOpeBal;
            if (balance > 0) {
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.9", null, RequestContextUtils.getLocale(request)));  //"Net Loss");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("leaf", true);
                objlast.put("amount", balance);
                objlast.put("isdebit", false);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal -= balance;
            }

            if (balance < 0) {
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.10", null, RequestContextUtils.getLocale(request)));  //"Net Profit");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("leaf", true);
                objlast.put("amount", -balance);
                objlast.put("isdebit", true);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal -= balance;
            }
            
            boolean eliminateflag = true;
            double bals[] = getOpeningBalancesWithDateMerged(request, companyids, startDate, endDate, eliminateflag);

            balance = bals[0] + bals[1];////+invCloseBal;
            balance=authHandler.round(balance, companyid);
            ctotal-=invCloseBal;
            if (balance != 0) {
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.1", null, RequestContextUtils.getLocale(request)));  //"Difference in Opening balances");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("fmt", "A");
                objlast.put("amount", Math.abs(balance));
                objlast.put("leaf", true);
                objlast.put("isdebit", balance > 0);
                // balance+=invCloseBal;
                if (balance > 0) {
                    dtotal += balance;
                    jArrL.put(objlast);
                } else {
                    ctotal += balance;
                    jArrR.put(objlast);
                }
            }
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.20", null, RequestContextUtils.getLocale(request)));  //"Total Assets");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount",authHandler.round(dtotal, companyid));
            objlast.put("fmt", "T");
//            jArrR.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.21", null, RequestContextUtils.getLocale(request)));  //"Total Liabilities");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount",authHandler.round(-ctotal, companyid));
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            JSONObject fobj = new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("[" + dtotal + "," + -ctotal + "]"));
            jobj.put("data", fobj);
        } catch (ParseException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBalanceSheet : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public double getBalanceSheetMerged(HttpServletRequest request, String[] companyids, int nature, JSONArray jArr) throws ServiceException, SessionExpiredException {
        double total=0;
        try {
//            String companyid = AccountingManager.getCompanyidFromRequest(request);
            //To do - Need to change company id logic
            String companyid = "";
            boolean eliminateflag = true;
            KwlReturnObject plresult = accAccountDAOobj.getGroupForProfitNlossMerged(companyid, nature, false, eliminateflag);
            List list = plresult.getEntityList();
            Iterator itr = list.iterator();

            Date startDate=authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
            Date endDate=authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
            while(itr.hasNext()) {
                Group group=(Group)itr.next();
                total+=formatGroupDetailsMerged(request, companyids, group, startDate, endDate, 0, true, jArr);
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getBalanceSheet : "+ex.getMessage(), ex);
        }
        return total;
    }

    public ModelAndView getProValuation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String enddate = request.getParameter("enddate");
            Date endDate = null;
            if (enddate != null) {
                endDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
            }
            jobj = getInventoryOpeningBalance(request, companyid, endDate);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getProValuation(HttpServletRequest request, String companyid, Date stDate, Date endDate) throws ServiceException {
        JSONObject jobj=new JSONObject();
        try {
            JSONArray DataJArr =getProValuationArray(request, companyid, stDate, endDate);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            JSONArray jArr1 = new JSONArray();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                int st = Integer.parseInt(start);
                int ed = Math.min(DataJArr.length(), st + Integer.parseInt(limit));
                for (int i = st; i < ed; i++) {
                    jArr1.put(DataJArr.getJSONObject(i));
                }
            }
            else{
                jArr1=DataJArr;
            }
            jobj.put("data", jArr1);
            jobj.put("count", DataJArr.length());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    public JSONArray getProValuationArray(HttpServletRequest request, String companyid, Date stDate, Date endDate) throws ServiceException {
        double valuation = 0, totalAssemblyValuation = 0, totalValuation = 0;
       JSONArray jArr=new JSONArray();
        try {
            Calendar startcal= Calendar.getInstance();
            Calendar endcal= Calendar.getInstance();
            if (stDate != null) {
                startcal.setTime(stDate);
            }
            if (endDate != null) {
                endcal.setTime(endDate);
            }
            boolean isprovalReport=false;
            if(request.getParameter("isprovalreport")!=null)
            {
                isprovalReport= Boolean.parseBoolean(request.getParameter("isprovalreport"));
            }
            boolean assemblyValuation=false;
            if(request.getAttribute("assemblyValuation")!=null)
            {
                assemblyValuation= Boolean.parseBoolean(request.getParameter("assemblyValuation"));
            }
            HashMap<String, Object> requestParams1 = AccountingManager.getGlobalParams(request);
            requestParams1.put("companyid", companyid);
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("stDate", stDate);
            requestParam.put("endDate", endDate);
            requestParam.put("companyid", companyid);
            requestParam.put("isprovalReport", isprovalReport);
            KwlReturnObject rtObject = accProductObj.getProValuation(requestParam);
            List list = rtObject.getEntityList();
            Iterator itr = list.iterator();
            while(itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Product product = (Product) row[0];
                double avgcost =0;// Double.parseDouble(row[1]==null?"0":row[1].toString());
                String productTypeID = product.getProducttype().getID();
                if(!productTypeID.equalsIgnoreCase(Producttype.ASSEMBLY)){
                    double totalProPurchase = 0;
                    double totalQuantityIn = 0;

                    List ll = getClosingStockVal(product.getID(), requestParams1);

//                    List ll=getTotalPurchaseCost(product.getID());
                    totalProPurchase=(Double)ll.get(0);
                    totalQuantityIn=(Double)ll.get(1);
                    if(totalQuantityIn!=0.0)
                    {
                        avgcost =  (totalProPurchase/totalQuantityIn);
                    }
                } else {
                    Date endCalDate = new Date();
                    Date startCalDate = new Date();
                    String endcalString = authHandler.getDateOnlyFormat().format(endcal.getTime());
                    String startcalString = authHandler.getDateOnlyFormat().format(startcal.getTime());
                    endCalDate = authHandler.getDateOnlyFormat().parse(endcalString);
                    startCalDate = authHandler.getDateOnlyFormat().parse(startcalString);                 
                    KwlReturnObject avgcostLi = accProductObj.getAvgcostAssemblyProduct(product.getID(), startCalDate, endCalDate);
                    avgcost = (Double) avgcostLi.getEntityList().get(0);
                }               
                double onhand = Double.parseDouble(row[2]==null?"0":row[2].toString());
                valuation = avgcost*onhand;
                if(isprovalReport&&valuation==0){
                    continue;
                }
                
                
                if(assemblyValuation && productTypeID.equals(Producttype.ASSEMBLY)){
                    totalAssemblyValuation += valuation;
                }
                totalValuation += valuation;
                
                if(isprovalReport) {
                    double purchasecost = Double.parseDouble(row[1]==null?"0":row[1].toString());//Need to fetch only for Pro valuation report
                    double lifo = 0,fifo = 0;
    //                if(!(product.getProducttype().getID().equals(Producttype.ASSEMBLY))) {
                    if(onhand > 0){//Need to calculate only in case of provaluation report
                        lifo = getFIFO(product.getID(),endDate,onhand,true);
                        fifo = getFIFO(product.getID(),endDate,onhand,false);
                    }
    //                }
                    JSONObject obj = new JSONObject();
                    obj.put("productid", product.getID());
                    obj.put("productname", product.getName());
                    obj.put("productdesc", product.getDescription());
                    obj.put("productType", product.getProducttype().getName());
                    obj.put("productTypeID", product.getProducttype().getID());                
                    obj.put("fifo", fifo);
                    obj.put("lifo", lifo);
                    obj.put("avgcost", (avgcost!=0 && onhand!=0)?avgcost:"N.A");
                    obj.put("purchasecost", purchasecost);
                    obj.put("quantity", onhand);
                    obj.put("valuation", valuation);
                    jArr.put(obj);
                }
            }
            if(!isprovalReport) {
                JSONObject obj = new JSONObject();
                obj.put("valuation", totalValuation);
                obj.put("assemblyValuation", totalAssemblyValuation);
                jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jArr;
    }

    public double getFIFO(String productid, Date endDate, double onhand, boolean isLifo) throws ServiceException {
    	try{
    		double lifo = 0,GRrate = 0,purchaseQty = 0;
    		int rateCount = 0,soldQty = 0,totalPurchaseQty = 0;
			List<Double> qty = new ArrayList();
			List<Double> rate = new ArrayList();
			KwlReturnObject initialQty, initialPurchasePrice, qtyfrominv,qtyRatefromInv;
			initialQty = accProductObj.getInitialQuantity(productid);
			if(initialQty.getEntityList().get(0) != null){
				initialPurchasePrice = accProductObj.getInitialCost(productid);
				if(initialPurchasePrice.getEntityList().get(0) != null){
					qty.add(Double.parseDouble(initialQty.getEntityList().get(0).toString()));
					rate.add(Double.parseDouble(initialPurchasePrice.getEntityList().get(0).toString()));
				}
			}
                        //To do - need to modify for multiple UOM.
			qtyRatefromInv = accProductObj.getRateandQtyfromInvoice(productid);
			Iterator<List> iteratorInv = qtyRatefromInv.getEntityList().iterator();
			while(iteratorInv.hasNext()){
				GoodsReceiptDetail goodsReceiptDetail = (GoodsReceiptDetail)iteratorInv.next();
				purchaseQty = goodsReceiptDetail.getInventory().getQuantity();
				GRrate = goodsReceiptDetail.getRate();
				qty.add(purchaseQty);
				rate.add(GRrate);
			}

			double totalQty = onhand;
			if(onhand > 0 && isLifo){
				for(int i = 0; i < qty.size(); i++){
					if(totalQty >= qty.get(i)  && totalQty != 0){
						lifo = lifo + (qty.get(i) * rate.get(i));
						totalQty = totalQty - qty.get(i);
					}else if(totalQty < qty.get(i) && totalQty != 0){
						lifo = lifo + totalQty * rate.get(i);
						totalQty = 0;
					}
				}
			}

			if(onhand > 0 && !isLifo){
				for(int i = qty.size(); i > 0; i--)
					if(totalQty >= qty.get(i - 1)   && totalQty != 0){
						lifo = lifo + (qty.get(i - 1) * rate.get(i - 1));
						totalQty = totalQty - qty.get(i - 1);
					}else if(totalQty < qty.get(i - 1)   && totalQty != 0){
						lifo = lifo + totalQty * rate.get(i - 1);
						totalQty = 0;
					}
			}


    		return lifo;
    	}catch(Exception ex){
    		ex.printStackTrace();
    		throw ServiceException.FAILURE(ex.getMessage(), ex);
		}
    }

    public double getLIFO(String productid, Date endDate, double onhand, boolean isLifo) throws ServiceException {
		try{
			double lifo = 0;
			int rateCount = 0; double soldQty = 0;
			List<Double> qty = new ArrayList();
			List<Double> rate = new ArrayList();
			KwlReturnObject initialQty, initialPurchasePrice, qtyfrominv,qtyPrice;
			initialQty = accProductObj.getInitialQuantity(productid);
			if(initialQty.getEntityList().get(0) != null){
				initialPurchasePrice = accProductObj.getInitialCost(productid);
				if(initialPurchasePrice.getEntityList().get(0) != null){
					qty.add(Double.parseDouble(initialQty.getEntityList().get(0).toString()));
					rate.add(Double.parseDouble(initialPurchasePrice.getEntityList().get(0).toString()));
				}
			}
			qtyfrominv = accProductObj.getQtyandUnitCost(productid, endDate);
			Iterator qtyRateIterator = qtyfrominv.getEntityList().iterator();
			while (qtyRateIterator.hasNext()){
				Object[] row = (Object[]) qtyRateIterator.next();
				qty.add(Double.parseDouble(row[0]==null?"0":row[0].toString()));
				qtyPrice = accProductObj.getProductPrice(productid, true, (Date) row[1], "","");
				if(qtyPrice.getEntityList().get(0) == null){
					qtyPrice = accProductObj.getInitialPrice(productid, true);
				}
				rate.add(Double.parseDouble(qtyPrice.getEntityList().get(0).toString()));
			}

			for(int i = 0; i < qty.size(); i++)
				soldQty = soldQty + qty.get(i);
			soldQty = ((int) (soldQty - onhand));
			if(isLifo){
				while(soldQty > 0){
					soldQty = soldQty - qty.get(rateCount++);
					if(soldQty <= 0){
						qty.set(rateCount - 1, (double) (-soldQty));
					}else{
						qty.set(rateCount - 1, 0.0);
					}
				}
				}else{
					rateCount = qty.size() - 1;
					while(soldQty > 0){
						soldQty = soldQty - qty.get(rateCount--);
						if(soldQty <= 0){
							qty.set(rateCount + 1, (double) (-soldQty));
						}else{
							qty.set(rateCount + 1, 0.0);
						}
					}
				}
			for(int i = 0; i < qty.size(); i++)
				lifo = lifo + (qty.get(i) * rate.get(i));
			return lifo;
		}catch(Exception ex){
    		System.out.print(ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
		}
    }

    public JSONObject getInventoryOpeningBalance(HttpServletRequest request, String companyid, Date stDate){
        JSONObject jobj=new JSONObject();
        try{
//            String companyid = AuthHandler.getCompanyid(request);

            Date date = null;
            KwlReturnObject rtObj = accProductObj.getInventoryOpeningBalanceDate(companyid);
//            String query = "select min(updateDate) from Inventory where product.company.companyID = ?";
//            List lst = HibernateUtil.executeQuery(session,query,companyid);
            List lst = rtObj.getEntityList();
            Iterator ite = lst.iterator();
            while(ite.hasNext()){
                date =  (Date)ite.next();
            }
            jobj = getProValuation(request, companyid, date, stDate);
        }catch(Exception ex){
        }
        return jobj;
    }

    @Deprecated
    public String formatValue(String value,String format,String currencyid){
        String result = "";
        try{

            double no = Double.parseDouble(value);
            String val = "";
            if(format.equals("CD") || format.equals("total") || format.equals("cash") || format.equals("export")){
                if(no>0){
                    val = authHandlerDAO.getFormattedCurrency(no,currencyid);
                    val = val + " Dr";
                }else if(no<0){
                    val = authHandlerDAO.getFormattedCurrency((-no),currencyid);
                    val = val + " Cr";
                }else{
                   val = "0";
                }
            }else if(format.equals("RAT")){
                java.text.DecimalFormat obj = new java.text.DecimalFormat("#,##0.00");
                val = obj.format(no) + " : 1";
            }else if(format.equals("PER")){
                java.text.DecimalFormat obj = new java.text.DecimalFormat("#,##0.00");
                val = obj.format(no) + " %";
            }else if(format.equals("DAY")){
                java.text.DecimalFormat obj = new java.text.DecimalFormat("#,##0.00");
                val = obj.format(no) + " days";
            }else{
                java.text.DecimalFormat obj = new java.text.DecimalFormat("#,##0.00");
                val = obj.format(no) + "";
            }
            result = val;
        }catch(Exception ex){
        }
        return result;
    }
    
    public String formatValue(String value,String format,String currencyid, String companyid){
        String result = "";
        try{

            double no = Double.parseDouble(value);
            String val = "";
            if(format.equals("CD") || format.equals("total") || format.equals("cash") || format.equals("export")){
                if(no>0){
                    val = authHandlerDAO.getFormattedCurrency(no,currencyid, companyid);
                    val = val + " Dr";
                }else if(no<0){
                    val = authHandlerDAO.getFormattedCurrency((-no),currencyid, companyid);
                    val = val + " Cr";
                }else{
                   val = "0";
                }
            }else if(format.equals("RAT")){
                java.text.DecimalFormat obj = new java.text.DecimalFormat("#,##0.00");
                val = obj.format(no) + " : 1";
            }else if(format.equals("PER")){
                java.text.DecimalFormat obj = new java.text.DecimalFormat("#,##0.00");
                val = obj.format(no) + " %";
            }else if(format.equals("DAY")){
                java.text.DecimalFormat obj = new java.text.DecimalFormat("#,##0.00");
                val = obj.format(no) + " days";
            }else{
                java.text.DecimalFormat obj = new java.text.DecimalFormat("#,##0.00");
                val = obj.format(no) + "";
            }
            result = val;
        }catch(Exception ex){
        }
        return result;
    }

    public List getClosingStockVal(String productid, HashMap<String, Object> requestParams) throws ServiceException, ParseException {
        List ll=new ArrayList() ;
          try {
          double totalProPurchase=0;
          double totalQuantityIn = 0;
          String companyid = (String) requestParams.get("companyid");
          KwlReturnObject quantityResult, priceResult, amtResult, tax, grCurrency, priceInHomeCurrency;

          quantityResult = accProductObj.getQuantityPurchaseOrSalesDetails(productid, true,false);
               List<Inventory> quantityList=  quantityResult.getEntityList();
              if (quantityList != null ){
                  for (Inventory inv : quantityList){
                	 if(!inv.isDefective()){
	                     double quantityIn = 0;
                             double baseuomrate = 1;
	                     double proPrice=0, ProPurchase=0, taxPercent=0, discountPercent = 0;
	                     quantityIn = inv.getQuantity();
                              baseuomrate = inv.getBaseuomrate();                           
	                      Date invDate = inv.getUpdateDate();
	                      priceResult = accProductObj.getProductPrice(productid, true, invDate, "","");
	                      List<Object> priceList= priceResult.getEntityList();
	                      if (priceList != null){
	                          for (Object cogsval : priceList){
	                              proPrice = (cogsval==null?0.0:(Double)cogsval);
	                          }
                                 
	                          // new logic for fetching product price
	                          priceResult = accGoodsReceiptDAOObj.getGoodsReceiptFormInventory(inv.getID());
	                          List<GoodsReceiptDetail> grDetail= priceResult.getEntityList();
                                  GoodsReceiptDetail grd = null;
                                  try{
                                      boolean viflag = false;
                                      if (grDetail != null){
    //	                              for (Object[] row : grDetail){
    //                                          grd = (GoodsReceiptDetail) row[0];
                                          for (GoodsReceiptDetail grd1 : grDetail){
                                              viflag = true;
                                              grd = grd1;
                                              proPrice = grd.getRate();
                                              discountPercent = grd.getDiscount()!=null?grd.getDiscount().getDiscountValue():0;
    //                                          if(row[1] != null)
    //                                              taxPercent = (Double)row[1];
                                          }
                                      }
                                      //To do - need to test this properly.
                                      if(viflag) {
                                          ProPurchase += authHandler.round(quantityIn*proPrice, companyid);
                                      } else {
                                          ProPurchase += authHandler.round(quantityIn*baseuomrate*proPrice, companyid);
                                      }

                                         double debitQty = 0, debitAmt =0;
                                      // Debit Note Qty
                                      quantityResult = accDebitNoteobj.getTotalDiscountAndQty(inv.getID());
                                      if(quantityResult.getEntityList() != null && quantityResult.getEntityList().size()>0) {
                                          List<Object[]> dnDetail= quantityResult.getEntityList();
                                          if(dnDetail != null) {
                                              for (Object[] row : dnDetail){
                                                  if(row[1] != null) {
                                                    debitQty = (Long)row[1];
                                                  }
                                                  if(row[0] != null) {
                                                    debitAmt = (Double)row[0];
                                                  }
                                              }
                                          }
                                      }
                                      quantityIn = quantityIn - debitQty;

                                      if(quantityIn != 0){                     
                                              if(debitAmt != 0) {
    //	                        	  amtResult = accDebitNoteobj.getTotalDiscount(inv.getID());
    //	                        	  if(amtResult.getEntityList() != null && amtResult.getEntityList().size()>0 && amtResult.getEntityList().get(0)!=null){
    //	                        		  debitAmt = Double.parseDouble(amtResult.getEntityList().get(0).toString());
                                                      tax = accGoodsReceiptDAOObj.getGR_ProductTaxPercent(inv.getID());
                                                      if(tax.getEntityList() != null && tax.getEntityList().size()>0 && tax.getEntityList().get(0)!=null)
                                                              taxPercent = Double.parseDouble(tax.getEntityList().get(0).toString());
                                                      debitAmt = debitAmt - (taxPercent/(taxPercent+100) * debitAmt);
    //                                                  tax = accGoodsReceiptDAOObj.getGR_ProductDiscountPercent(inv.getID());
    //                                                  if(tax.getEntityList() != null && tax.getEntityList().size()>0 && tax.getEntityList().get(0)!=null)
    //                                                          discountPercent = Double.parseDouble(tax.getEntityList().get(0).toString());
                                                      debitAmt = (debitAmt * 100) / (100 - discountPercent);
                                                      ProPurchase = ProPurchase - debitAmt;
    //	                        		  accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ProPurchase, inv ,invDetail.getInvoice().getJournalEntry().getEntryDate(),invDetail.getInvoice().getExternalCurrencyRate());
                                              }
                                      }else
                                              ProPurchase = 0;

                                      if(grd != null) {
    //	                          grCurrency = accGoodsReceiptDAOObj.getGoodsReceipt_Currency(inv.getID());
    //	                          if(grCurrency.getEntityList() != null && grCurrency.getEntityList().size()>0 && grCurrency.getEntityList().get(0)!=null) {
                                              GoodsReceipt gr = grd.getGoodsReceipt();
//                                              priceInHomeCurrency = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ProPurchase, gr.getCurrency().getCurrencyID(), gr.getJournalEntry().getEntryDate(), gr.getJournalEntry().getExternalCurrencyRate());
                                              priceInHomeCurrency = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ProPurchase, gr.getCurrency().getCurrencyID(), gr.getCreationDate(), gr.getJournalEntry().getExternalCurrencyRate());
                                              ProPurchase = (Double) priceInHomeCurrency.getEntityList().get(0);
    //	                          }
                                      }
                                  } catch (Exception ex) {
                                         ex.printStackTrace();
                                      //throw ServiceException.FAILURE("getClosingStockVal : "+ex.getMessage(), ex);
                                  }
                                  totalQuantityIn+=(quantityIn*baseuomrate);
	                          totalProPurchase += ProPurchase;
	                      }
                	 }
                  }
                  ll.add(0,totalProPurchase);
                  ll.add(1,totalQuantityIn);
              }
           } catch (Exception ex) {
          	 ex.printStackTrace();
              throw ServiceException.FAILURE("getClosingStockVal : "+ex.getMessage(), ex);
           }
          return ll;
      }

    public JSONObject getNewMYOBtradingreport(HttpServletRequest request, JSONObject tradingjobj,boolean isPrint) throws ServiceException, SessionExpiredException {
       try {
            JSONObject jobj = tradingjobj.getJSONObject("data");
            JSONArray rightObjArr = jobj.getJSONArray("right");
            JSONArray leftObjArr = jobj.getJSONArray("left");

            JSONArray tradingArray = new JSONArray();

            JSONObject objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("accountname", "Income");
            objlast.put("amount", "");
            objlast.put("fmt", "B");

            for(int i = 0; i < rightObjArr.length(); i++)
                    leftObjArr.put(rightObjArr.getJSONObject(i));

            int j = 0;
            for (int i = 0; i < leftObjArr.length(); i++) {
               JSONObject leftobj = leftObjArr.getJSONObject(i);
               if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("income")) {
                   if (!leftobj.has("group") || (leftobj.has("group") && !leftobj.get("group").toString().equals("income"))) {
                       if(j==0) {
                           tradingArray.put(objlast);
                           j++;
                       }
                       tradingArray.put(leftobj);
                   }
               }
            }

            j = 0;
            objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "");
            objlast.put("fmt", "B");
            objlast.put("accountname", "Expense");
            for (int i = 0; i < leftObjArr.length(); i++) {
               JSONObject leftobj = leftObjArr.getJSONObject(i);
               if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("expense")) {
                   if (!leftobj.has("group") || (leftobj.has("group") && !leftobj.get("group").toString().equals("expense"))) {
                       if(j==0) {
                           tradingArray.put(objlast);
                           j++;
                       }
                       tradingArray.put(leftobj);
                   }
               }
           }

           for (int i = 0; i < leftObjArr.length(); i++) {

               JSONObject leftobj = leftObjArr.getJSONObject(i);

               if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("grossprofit")) {
                   tradingArray.put(leftobj);
               } else if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("grossloss")) {
                   tradingArray.put(leftobj);
               }
           }

           j = 0;
           objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "");
            objlast.put("fmt", "B");
           objlast.put("accountname", "Other Income");
           for (int i = 0; i < leftObjArr.length(); i++) {
               JSONObject leftobj = leftObjArr.getJSONObject(i);
               if (leftobj.has("group") && leftobj.get("group").toString().equals("income")) {
                   if(j==0) {
                           tradingArray.put(objlast);
                           j++;
                   }
                   tradingArray.put(leftobj);
               }
           }

           j = 0;
           objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "");
            objlast.put("fmt", "B");
           objlast.put("accountname", "Other Expense");
           for (int i = 0; i < leftObjArr.length(); i++) {
               JSONObject leftobj = leftObjArr.getJSONObject(i);
               if (leftobj.has("group") && leftobj.get("group").toString().equals("expense")) {
                   if(j==0) {
                           tradingArray.put(objlast);
                           j++;
                   }
                   tradingArray.put(leftobj);
               }
           }

           for (int i = 0; i < leftObjArr.length(); i++) {
               JSONObject leftobj = leftObjArr.getJSONObject(i);

               if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("netprofit")) {
                   tradingArray.put(leftobj);
               } else if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("netloss")) {
                   tradingArray.put(leftobj);
               }
           }

            jobj.put("left", tradingArray);
            tradingjobj.getJSONObject("data").remove("left");
            tradingjobj.getJSONObject("data").put("left", tradingArray);

            if(isPrint){
                tradingjobj.remove("data");
                tradingjobj.put("data", tradingArray);
            }

            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
       return tradingjobj;
    }
    
    public JSONObject getTradingAndProfitLossforExportMerged(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        double invOpeBal=0, invCloseBal=0, assemblyValuation=0;
        try {            
            String costCenterId = request.getParameter("costcenter"); //Filter for costcenter
            String reportView = request.getParameter("reportView"); //"TradingAndProfitLoss","CostCenter"
            String[] companyids = request.getParameter("companyids").split(",");
            String companyid = "";
            double dtotal = 0, ctotal = 0;
            JSONArray jArrL = new JSONArray();
            JSONArray jArrR = new JSONArray();
            JSONObject objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.3", null, RequestContextUtils.getLocale(request))+"</div>");    // Amount (Debit)
            objlast.put("fmt", "H");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.4", null, RequestContextUtils.getLocale(request))+"</div>");      //Amount (Credit)
            objlast.put("fmt", "H");
//            jArrR.put(objlast);
            Date startDate=authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
            Date endDate=authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
            
            if(!"CostCenter".equalsIgnoreCase(reportView) && StringUtil.isNullOrEmpty(costCenterId)){ //Don't show Opening/Closing Stock for any Cost-Center
                for(int cnt = 0; cnt < companyids.length; cnt++){
                    companyid = companyids[cnt];
                    request.setAttribute("companyid", companyid);                    
                    JSONObject jObjX= getInventoryOpeningBalance(request, companyid, startDate);                
                    JSONArray jarr = jObjX.getJSONArray("data");
                    if(jarr.length() > 0) {
                        JSONObject jobj1 = jarr.getJSONObject(0);
                        invOpeBal = jobj1.has("valuation")?jobj1.getDouble("valuation"):0;
                    }
                    jObjX=new JSONObject();
                    request.setAttribute("assemblyValuation", true);
                    jObjX =getInventoryOpeningBalance(request, companyid, endDate);
                    jarr = jObjX.getJSONArray("data");
                    if(jarr.length() > 0) {
                        JSONObject jobj1 = jarr.getJSONObject(0);
                        invCloseBal = jobj1.has("valuation")?jobj1.getDouble("valuation"):0;
                        assemblyValuation = jobj1.has("assemblyValuation")?jobj1.getDouble("assemblyValuation"):0;
                    }
                }

                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.13", null, RequestContextUtils.getLocale(request)));
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", invOpeBal);
                objlast.put("acctype", "expense");
                objlast.put("fmt", "H");
                jArrL.put(objlast);
            }
//            dtotal = getTrading(session, request, Group.NATURE_EXPENSES, jArrL);
            dtotal = getTradingMerged(request, companyids, Group.NATURE_EXPENSES, jArrL);
//            ctotal = getTrading(session, request, Group.NATURE_INCOME, jArrR);
            ctotal = getTradingMerged(request, companyids, Group.NATURE_INCOME, jArrR);

            dtotal+=invOpeBal;
            ctotal-=invCloseBal;

            if(!"CostCenter".equalsIgnoreCase(reportView) && StringUtil.isNullOrEmpty(costCenterId)){ //Don't show Opening/Closing Stock for any Cost-Center
                JSONObject obj = new JSONObject();
                if(invCloseBal+assemblyValuation > 0){ //Show details if Closing_Stock > 0
                    obj.put("accountname", messageSource.getMessage("acc.report.17", null, RequestContextUtils.getLocale(request)));  //"Closing Stock");
                    obj.put("accountid", "");
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", false);
                    obj.put("acctype", "income");
                    obj.put("amount", "");
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname",messageSource.getMessage("acc.report.14", null, RequestContextUtils.getLocale(request)));  // "Finish Products (Total Value of \"Inventory Assembly\" products)");
                    obj.put("accountid", "");
                    obj.put("level", 1);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", assemblyValuation);
                    obj.put("acctype", "income");
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname", messageSource.getMessage("acc.report.15", null, RequestContextUtils.getLocale(request)));  //"Raw Materials (Total Value of \"Inventory Item\" products)");
                    obj.put("accountid", "");
                    obj.put("level", 1);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", invCloseBal-assemblyValuation);
                    obj.put("acctype", "income");
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname", messageSource.getMessage("acc.report.16", null, RequestContextUtils.getLocale(request)));  //"Total Closing Stock");
                    obj.put("accountid", "");
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", invCloseBal);
                    objlast.put("fmt", "H");
                    obj.put("acctype", "income");
                    jArrR.put(obj);
                } else { // Show single line if Closing_Stock = 0
                    obj = new JSONObject();
                    obj.put("accountname",messageSource.getMessage("acc.report.17", null, RequestContextUtils.getLocale(request)));  // "Closing Stock");
                    obj.put("accountid", "");
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", invCloseBal);
                    obj.put("acctype", "income");
                    obj.put("fmt", "H");
                    jArrR.put(obj);
                }
            }

            if(!"CostCenter".equalsIgnoreCase(reportView)) {//Don't Adjust report layout for cost center report
                int len = jArrL.length() - jArrR.length(); //Adjust report layout by equaling no. of rows
                JSONArray jArr = jArrR;
                if (len < 0) {
                    len = -len;
                    jArr = jArrL;
                }
                for (int i = 0; i < len; i++) {
                    jArr.put(new JSONObject());
                }
            }

            double balance = dtotal + ctotal;
            if(!"CostCenter".equalsIgnoreCase(reportView)) {//Don't show GrossLoss,GrossProfit for cost center report
                if (balance > 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", false);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.5", null, RequestContextUtils.getLocale(request)));  //"Gross Loss");
                    objlast.put("amount", balance);
                    objlast.put("fmt", "B");
                    objlast.put("acctype", "grossloss");
                    jArrR.put(objlast);
                    jArrL.put(new JSONObject());
                    ctotal -= balance;
                }
                if (balance < 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.6", null, RequestContextUtils.getLocale(request)));  //"Gross Profit");
                    objlast.put("amount", balance==0?balance:-balance);//Remove '-' sign if 0
                    objlast.put("fmt", "B");
                    objlast.put("acctype", "grossprofit");
                    jArrL.put(objlast);
                    jArrR.put(new JSONObject());
                    dtotal -= balance;
                }
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.7", null, RequestContextUtils.getLocale(request)));  //"Total Debit");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", dtotal);
                objlast.put("fmt", "T");
                jArrL.put(objlast);
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.8", null, RequestContextUtils.getLocale(request)));  //"Total Credit");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("amount", ctotal==0?ctotal:-ctotal);//Remove '-' sign if 0
                objlast.put("fmt", "T");
                jArrR.put(objlast);

                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.3", null, RequestContextUtils.getLocale(request))+"</div>");   //Amount (Debit)
                objlast.put("fmt", "H");
                jArrL.put(objlast);
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.4", null, RequestContextUtils.getLocale(request))+"</div>");       //Amount (Credit)
                objlast.put("fmt", "H");
                jArrR.put(objlast);
                dtotal = 0;
                ctotal = 0;
                if (balance > 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.5", null, RequestContextUtils.getLocale(request)));  //"Gross Loss");
                    objlast.put("amount", balance);
                    objlast.put("fmt", "B");
                    jArrL.put(objlast);
                    objlast.put("acctype", "grossloss1");
                    dtotal = balance;
                }
                if (balance < 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", false);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.6", null, RequestContextUtils.getLocale(request)));  //"Gross Profit");
                    objlast.put("amount", balance==0?balance:-balance);//Remove '-' sign if 0
                    objlast.put("fmt", "B");
                    objlast.put("acctype", "grossprofit1");
                    jArrR.put(objlast);
                    ctotal = balance;
                }
            }

//            dtotal += getProfitLoss(session, request, Group.NATURE_EXPENSES, jArrL);
            dtotal += getProfitLossMerged(request, companyids, Group.NATURE_EXPENSES, jArrL);
//            ctotal += getProfitLoss(session, request, Group.NATURE_INCOME, jArrR);
            ctotal += getProfitLossMerged(request, companyids, Group.NATURE_INCOME, jArrR);

            if(!"CostCenter".equalsIgnoreCase(reportView)) { //Don't show NetLoss,NetProfit for cost center report
                balance = dtotal + ctotal;
                if (balance > 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", false);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.9", null, RequestContextUtils.getLocale(request)));  //"Net Loss");
                    objlast.put("amount", balance);
                    objlast.put("fmt", "B");
                    objlast.put("acctype", "netloss");
                    jArrR.put(objlast);
                    ctotal -= balance;
                }
                if (balance < 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.10", null, RequestContextUtils.getLocale(request)));  //"Net Profit");
                    objlast.put("amount", balance==0?balance:-balance);//Remove '-' sign if 0
                    objlast.put("fmt", "B");
                    objlast.put("acctype", "netprofit");
                    jArrL.put(objlast);
                    dtotal -= balance;
                }
            }

            if ("CostCenter".equalsIgnoreCase(reportView)) { //Add LIABILITY for cost center report (Tax Amount)
                //  KwlReturnObject ret =  accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Group", Group.OTHER_CURRENT_LIABILITIES);
                //   if(!ret.getEntityList().isEmpty()){

                //Logic to get new OTHER_CURRENT_LIABILITIES group from old OTHER_CURRENT_LIABILITIES
                //To do - Need to test wheteher is working or not

                Group liab_group = accAccountDAOobj.getNewGroupFromOldId(Group.OTHER_CURRENT_LIABILITIES, companyid);
                if (liab_group != null) {
                    ctotal += formatGroupDetailsMerged(request, companyids, liab_group, startDate, endDate, 0, true, jArrR); //Bug Fixed #16746
                    liab_group.getName();
                }
            }

            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.7", null, RequestContextUtils.getLocale(request)));  //"Total Debit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.8", null, RequestContextUtils.getLocale(request)));  //"Total Credit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", ctotal==0?ctotal:-ctotal);//Remove '-' sign if 0
            objlast.put("fmt", "T");
//            jArrR.put(objlast);

            JSONObject fobj = new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("[" + dtotal + "," + (ctotal==0?ctotal:-ctotal) + "]"));
            jobj.put("data", fobj);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : "+ex.getMessage(), ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : "+e.getMessage(), e);
        }
        return jobj;
    }
    public ModelAndView getMissingAutoSequenceNumber(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONArray jarr = getMissingAutoSequenceNumber(request);
            jobj.put("data", jarr);
            jobj.put("count", jarr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
      public JSONArray getMissingAutoSequenceNumber(HttpServletRequest request) throws ServiceException, ParseException, JSONException {
       JSONArray jArr = new JSONArray();
       String companyid="";   
       int moduleid=0;
         try {
             companyid = sessionHandlerImpl.getCompanyid(request);
             HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
             moduleid=request.getParameter("moduleid")!=null? Integer.parseInt(request.getParameter("moduleid")):27;
             requestParams.put("tablename", request.getParameter("tablename")!=null? request.getParameter("tablename"):"DeliveryOrder");
             requestParams.put("moduleid",moduleid);
             requestParams.put("orderby",request.getParameter("orderby")!=null? request.getParameter("orderby"):"deliveryOrderNumber");
             requestParams.put("companyid", companyid);
             requestParams.put("sequenceFormat", request.getParameter("sequenceFormat")); 
             jArr = accInvoiceDAOobj.getMissingAutoSequenceNumber(requestParams);           
        
         } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("getMissingAutoSequenceNumber : " + ex.getMessage(), ex);
        }  
        return jArr;
    }
    public ModelAndView getNewBalanceSheetMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONArray rightObjArr = new JSONArray();
            JSONObject objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("accountname", "Assets");
            objlast.put("amount", "");
            objlast.put("fmt", "B");

            jobj = getBalanceSheetMerged(request);
            JSONObject jobj1 = jobj.getJSONObject("data");
            JSONArray rightObjArr1 = jobj1.getJSONArray("right");
            JSONArray leftObjArr = jobj1.getJSONArray("left");

            rightObjArr.put(0, objlast);
            String earnings = "0";
            for(int i = 0; i < rightObjArr1.length(); i++) {
                if(rightObjArr1.getJSONObject(i).getString("accountname").equals("Net Loss")) {
                    earnings = rightObjArr1.getJSONObject(i).getString("amount");
                } else {
                    rightObjArr.put(rightObjArr1.getJSONObject(i));
                }
            }

            objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("accountname", "Liabilities");
            objlast.put("amount", "");
            objlast.put("fmt", "B");

            rightObjArr.put(objlast);

            objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("accountname", "Equity");
            objlast.put("amount", "");
            objlast.put("fmt", "B");

            for(int i = 0; i < leftObjArr.length(); i++) {
                if(leftObjArr.getJSONObject(i).getString("accountname").equals("Net Profit")) {
                    earnings = leftObjArr.getJSONObject(i).getString("amount");
                }
            }
            
            JSONObject objlast1 = new JSONObject();
            objlast1.put("accountid", "");
            objlast1.put("level", 0);
            objlast1.put("isdebit", false);
            objlast1.put("leaf", true);
            objlast1.put("accountname", messageSource.getMessage("acc.field.CurrentYearEarnings", null, RequestContextUtils.getLocale(request)));
            objlast1.put("amount", earnings);
            objlast1.put("fmt", "H");

            for(int i = 0; i < leftObjArr.length(); i++) {
                if(leftObjArr.getJSONObject(i).getString("accountname").equals("Equity")) {
                    rightObjArr.put(objlast);
                }
                
                if(leftObjArr.getJSONObject(i).getString("accountname").equals("Total Equity")) {
                    leftObjArr.getJSONObject(i).put("amount", Double.parseDouble(leftObjArr.getJSONObject(i).getString("amount")) +  Double.parseDouble(earnings));
                    rightObjArr.put(objlast1);
                }

                if(!leftObjArr.getJSONObject(i).getString("accountname").equals("Net Profit"))
                    rightObjArr.put(leftObjArr.getJSONObject(i));
            }
            jobj.getJSONObject("data").remove("left");
            jobj.getJSONObject("data").put("left", rightObjArr);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    @Override
    public void setMessageSource(MessageSource msg) {
            this.messageSource = msg;
    }
    
    
    public ModelAndView getProjectStatusReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getProjectStatusReport(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
    public ModelAndView getIBGDummyData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {

            JSONObject jobj1 = new JSONObject();
            jobj1.put("ibgId", "111");
            jobj1.put("receivingBankCode", "7131");
            jobj1.put("receivingBankName", "UOB Bank Limited");
            jobj1.put("receivingBranchCode", "71");
            jobj1.put("receivingAccountNumber", "047376131");
            jobj1.put("receivingAccountName", "Telecom Soft");

            JSONArray arr = new JSONArray();
            arr.put(jobj1);

            jobj.put("data", arr);

            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getProjectStatusReport(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject retJobj = new JSONObject();
        //Session session=null;
        try {
            KwlReturnObject result = null;
            JSONArray jArr = new JSONArray();
            String companyid = sessionHandlerImpl.getCompanyid(request);

            //Fetched data from Deskera PM
//            String action = "106";
//            String pmURL = this.getServletContext().getInitParameter("pmURL");
            String pmURL = URLUtil.buildRestURL("pmURL");
            pmURL = pmURL + "project";            
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("companyid", companyid);

//            Date startDate = authHandler.getDateFormatter(request).parse("Jan 01, 2014 12:00:00 AM");
//            Date endDate = authHandler.getDateFormatter(request).parse("Jan 08, 2014 12:00:00 AM");
            Date startDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
            Date endDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
//            Date startDate = new Date();
//            Date endDate = new Date();

            //session = HibernateUtil.getCurrentSession();

            JSONObject jobj = apiCallHandlerService.restGetMethod(pmURL, userData.toString());
//            JSONObject jobj = apiCallHandlerService.callApp(pmURL, userData, companyid, action);

            JSONArray jArray = jobj.getJSONArray("data");

            //Fetched Project field params     
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("filter_names", Arrays.asList("companyid", "isforproject"));
            requestParams.put("filter_values", Arrays.asList(companyid, 1));

            result = accAccountDAOobj.getFieldParams(requestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            
//            new DecimalFormat("#,##0.00").format(authHandler.round(monthlyRevenueAmount, 2))
            //Loop on fields params having isProject true
//            while (itr.hasNext()) {
//                FieldParams fieldParams = (FieldParams) itr.next();
                //Loop to set combo values 
                for (int i = 0; i < jArray.length(); i++) {
//                for (int i = 0; i < 1; i++) {
                    JSONObject jobjData = jArray.getJSONObject(i);
                    JSONObject jobjTemp = new JSONObject();

                    String projectid = (String) jobjData.get("projectId");
//                    String projectid = "a5722fa5-81c5-44ee-b182-d3555819be0b";
                    String projectName = (String) jobjData.get("projectName");
//                    String projectName = "Accounting PM Project";
                    double projectActualCost = jobjData.optDouble("projectActualCost", 0.0);
                    double projectBudgetedCost = jobjData.optDouble("projectBudgetedCost", 0.0);
//                    double projectActualCost = 100;
//                    double projectBudgetedCost = 100;
                    double totalInvoiceAmount = getTotalInvoiceAmountForProject(companyid, projectid, startDate, endDate);
                    double totalSOAmount = getTotalSalesOrderAmountForProject(companyid, projectid, startDate, endDate);
                    double totalReceivedAmount = getTotalReceivedAmountForProject(companyid, projectid, startDate, endDate);
//                    double totalReceivedAmount = 100;
                    double percentComplete = 0.0;
                    if(projectActualCost != 0){
                        percentComplete = (projectActualCost / projectBudgetedCost)*100;
                    }
                    
                    double balanceAmt = (totalSOAmount * percentComplete/100) - totalInvoiceAmount;
                    List<String> soData = getSOContaingProject(companyid, projectid, startDate, endDate);
                    String soIds = soData.get(0);
                    String customerIds = soData.get(1);

                    jobjTemp.put("projectId", projectid);
                    jobjTemp.put("projectName", projectName);
                    jobjTemp.put("projectActualCost", authHandler.formattedAmount(authHandler.round(projectActualCost, companyid), companyid));
                    jobjTemp.put("budgetedAmount", authHandler.formattedAmount(authHandler.round(projectBudgetedCost, companyid), companyid));
                    jobjTemp.put("totalInvoiceAmount", authHandler.formattedAmount(authHandler.round(totalInvoiceAmount, companyid), companyid));
                    jobjTemp.put("projectValue", authHandler.formattedAmount(authHandler.round(totalSOAmount, companyid), companyid));
                    jobjTemp.put("totalAmountReceived", authHandler.formattedAmount(authHandler.round(totalReceivedAmount, companyid), companyid));
                    jobjTemp.put("percentCompletion", authHandler.round(percentComplete, companyid)+"%");
                    jobjTemp.put("balanceAmount", authHandler.formattedAmount(authHandler.round(balanceAmt, companyid), companyid));
                    jobjTemp.put("salesOrderIds", soIds);
                    jobjTemp.put("customerIds", customerIds);
                    jArr.put(jobjTemp);


                }
                retJobj.put("data", jArr);
//            }

        } catch (ParseException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } 
        catch (JSONException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
//        finally{
//            HibernateUtil.closeSession(session);
//        }
        return retJobj;
    }
    
    
    public double getTotalInvoiceAmountForProject(String companyId, String projectId, Date startDate, Date endDate){
        Double totalAmount = 0.0;
        try {
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) objItr.getEntityList().get(0);
            String gcurrencyid = company.getCurrency().getCurrencyID();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyId);
            requestParams.put("gcurrencyid", gcurrencyid);
            String searchString = getSearchColumns(companyId, projectId);
            requestParams.put("searchString", searchString);
            requestParams.put("startDate", startDate);
            requestParams.put("endDate", endDate);
            KwlReturnObject returnObject = accInvoiceDAOobj.getInvoicesContainingProject(requestParams);
            
            List list = returnObject.getEntityList();
            
            Iterator itr = list.iterator();
            
            while (itr.hasNext()) {
                Double amount = 0.0;

                Object[] oj = (Object[]) itr.next();
                String invid = oj[0].toString();
                
                boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
                if (withoutinventory) {
//                    BillingInvoice billingInvoice = (BillingInvoice) session.get(BillingInvoice.class, invid);
                    KwlReturnObject billObjItr = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), invid);
                    BillingInvoice billingInvoice = (BillingInvoice) billObjItr.getEntityList().get(0);
                    JournalEntryDetail d = billingInvoice.getCustomerEntry();
                    amount = d.getAmount();
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amount, billingInvoice.getCurrency().getCurrencyID(), gcurrencyid, billingInvoice.getJournalEntry().getEntryDate(), 0.0);
                    amount = (Double) bAmt.getEntityList().get(0);
                    amount = authHandler.round(amount, companyId);
                    
//                    invoicenum = billingInvoice.getBillingInvoiceNumber();
//                    jenum = billingInvoice.getJournalEntry().getEntryNumber();
                } else {
//                    Invoice invoice = (Invoice) session.get(Invoice.class, invid);
                    KwlReturnObject invObjItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                    Invoice invoice = (Invoice) invObjItr.getEntityList().get(0);
                    
                    JournalEntryDetail d = invoice.getCustomerEntry();
                    amount = d.getAmount();
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amount, invoice.getCurrency().getCurrencyID(), gcurrencyid, invoice.getJournalEntry().getEntryDate(), 0.0);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amount, invoice.getCurrency().getCurrencyID(), gcurrencyid, invoice.getCreationDate(), 0.0);
                    amount = (Double) bAmt.getEntityList().get(0);
                    amount = authHandler.round(amount, companyId);
                    
//                    invoicenum = invoice.getInvoiceNumber();
//                    jenum = invoice.getJournalEntry().getEntryNumber();
                }
                totalAmount+=amount;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return totalAmount;
    }
    
    public double getTotalReceivedAmountForProject(String companyId, String projectId, Date startDate, Date endDate) {
        Double totalAmount = 0.0;
        try {
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) objItr.getEntityList().get(0);
            String gcurrencyid = company.getCurrency().getCurrencyID();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyId);
            requestParams.put("gcurrencyid", gcurrencyid);
            String searchString = getSearchColumns(companyId, projectId);
            requestParams.put("searchString", searchString);
            requestParams.put("startDate", startDate);
            requestParams.put("endDate", endDate);
            KwlReturnObject receiptObject = accReceiptDao.getReceiptsContainingProject(requestParams);
            List list = receiptObject.getEntityList();
            Iterator receiptItr = list.iterator();

            while (receiptItr.hasNext()) {
                Double amount = 0.0;

                String receitId = (String) receiptItr.next();
//                String receitId = oj[0].toString();

                KwlReturnObject receiptObjItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receitId);
                Receipt receipt = (Receipt) receiptObjItr.getEntityList().get(0);

                JournalEntryDetail d = receipt.getDeposittoJEDetail();
                amount = d.getAmount();
//                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amount, receipt.getCurrency().getCurrencyID(), gcurrencyid, receipt.getJournalEntry().getEntryDate(), 0.0);
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amount, receipt.getCurrency().getCurrencyID(), gcurrencyid, receipt.getCreationDate(), 0.0);
                amount = (Double) bAmt.getEntityList().get(0);
                amount = authHandler.round(amount, companyId);
                totalAmount += amount;
            }

            KwlReturnObject breceiptObject = accReceiptDao.getBillingReceiptsContainingProject(requestParams);
            List blist = breceiptObject.getEntityList();
            Iterator breceiptItr = blist.iterator();

            while (breceiptItr.hasNext()) {
                Double amount = 0.0;
                
                String breceitId = (String) breceiptItr.next();

//                Object[] oj = (Object[]) breceiptItr.next();
//                String breceitId = oj[0].toString();

                KwlReturnObject billObjItr = accountingHandlerDAOobj.getObject(BillingReceipt.class.getName(), breceitId);
                BillingReceipt billingReceipt = (BillingReceipt) billObjItr.getEntityList().get(0);
                JournalEntryDetail d = billingReceipt.getDeposittoJEDetail();
                amount = d.getAmount();
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amount, billingReceipt.getCurrency().getCurrencyID(), gcurrencyid, billingReceipt.getJournalEntry().getEntryDate(), 0.0);
                amount = (Double) bAmt.getEntityList().get(0);
                amount = authHandler.round(amount, companyId);
                totalAmount += amount;
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return totalAmount;
    }
    
    
    
    public double getTotalSalesOrderAmountForProject(String companyId, String projectId, Date startDate, Date endDate) {
        Double totalAmount = 0.0;
        try {
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) objItr.getEntityList().get(0);
            String gcurrencyid = company.getCurrency().getCurrencyID();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyId);
            requestParams.put("gcurrencyid", gcurrencyid);
            String searchString = getSearchColumns(companyId, projectId);
            requestParams.put("searchString", searchString);
            requestParams.put("startDate", startDate);
            requestParams.put("endDate", endDate);
            KwlReturnObject returnObject = accSalesOrderDAOobj.getSOContainingProject(requestParams);

            List list = returnObject.getEntityList();

            Iterator itr = list.iterator();

            while (itr.hasNext()) {
                Double amount = 0.0;

                Object[] oj = (Object[]) itr.next();
                String invid = oj[0].toString();

                boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
                if (!withoutinventory) {
//                    BillingInvoice billingInvoice = (BillingInvoice) session.get(BillingInvoice.class, invid);
                    KwlReturnObject billObjItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), invid);
                    SalesOrder salesOrder = (SalesOrder) billObjItr.getEntityList().get(0);

                    //calculating sales order amount

                    Iterator itrRow = salesOrder.getRows().iterator();
                    double totalDiscount = 0, discountPrice = 0;
                    System.out.println(salesOrder.getSalesOrderNumber());
                    while (itrRow.hasNext()) {
                        SalesOrderDetail sod = (SalesOrderDetail) itrRow.next();
//                        amount+=sod.getQuantity()*sod.getRate();
                        double rowTaxPercent = 0;
                        if (sod.getTax() != null) {
                            requestParams.put("transactiondate", salesOrder.getOrderDate());
                            requestParams.put("taxid", sod.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        }
//                        amount+=sod.getQuantity() *sod.getRate()*rowTaxPercent/100;

                        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, sod.getRate(), salesOrder.getCurrency().getCurrencyID(), salesOrder.getOrderDate(), 0);
                        double sorate = (Double) bAmt.getEntityList().get(0);

                        double quotationPrice = authHandler.round(sod.getQuantity() * sorate, companyId);
                        if (sod.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - (quotationPrice * sod.getDiscount() / 100);
                        } else {
                            discountPrice = quotationPrice - sod.getDiscount();
                        }

                        amount += discountPrice + (discountPrice * rowTaxPercent / 100);
                    }

                    if (salesOrder.getDiscount() != 0) {
                        if (salesOrder.isPerDiscount()) {
                            totalDiscount = amount * salesOrder.getDiscount() / 100;
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - salesOrder.getDiscount();
                        }
                    }
                }
                totalAmount += amount;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return totalAmount;
    }
    
    public List<String> getSOContaingProject(String companyId, String projectId, Date startDate, Date endDate) {
        String soIds = "";
        String customerIds = "";
        List<String> returnList = new ArrayList<String>();
        try {
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) objItr.getEntityList().get(0);
            String gcurrencyid = company.getCurrency().getCurrencyID();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyId);
            requestParams.put("gcurrencyid", gcurrencyid);
            String searchString = getSearchColumns(companyId, projectId);
            requestParams.put("searchString", searchString);
            requestParams.put("startDate", startDate);
            requestParams.put("endDate", endDate);
            KwlReturnObject returnObject = accSalesOrderDAOobj.getSOContainingProject(requestParams);

            List list = returnObject.getEntityList();

            Iterator itr = list.iterator();

            while (itr.hasNext()) {
                Double amount = 0.0;

                Object[] oj = (Object[]) itr.next();
                String invid = oj[0].toString();

                boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
                if (!withoutinventory) {
                    KwlReturnObject billObjItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), invid);
                    SalesOrder salesOrder = (SalesOrder) billObjItr.getEntityList().get(0);
                    if (salesOrder != null) {
                        soIds += salesOrder.getID() + ",";
                        customerIds+=salesOrder.getCustomer().getID()+",";
                    }
                }
            }
            if (!StringUtil.isNullOrEmpty(soIds)) {
                soIds = soIds.substring(0, soIds.length() - 1);
                customerIds = customerIds.substring(0, customerIds.length() - 1);
            }
            
            returnList.add(soIds);
            returnList.add(customerIds);
            
        } catch (ServiceException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnList;
    }
         
    public String getSearchColumns(String companyid, String projectid) {

        String searchString = "";
        try {
//            String mysqlQuery = " select colnum, id from fieldParams where isforproject = 1 and companyid = ?";

            //Fetched all isProject fields
            KwlReturnObject returnObject = accountingHandlerDAOobj.getFieldParamsForProject(companyid, projectid);
            List list = returnObject.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String columnno = (oj[0]).toString();
                String id = (oj[1]).toString();

                //Checked if provided projectid is present for combo data
//                String mysqlQueryCmbData = " select id from fieldcombodata where fieldid = ? and projectid = ?";

//                List listCmbData = HibernateUtil.executeSQLQuery(session, mysqlQueryCmbData, new Object[]{id, projectid});
                KwlReturnObject cmbReturnObject = accountingHandlerDAOobj.getFieldComboDataForProject(id, projectid);
                List listCmbData = cmbReturnObject.getEntityList();
                Iterator itrCmbData = listCmbData.iterator();
                //If project id is present that searchfield is added
                if (itrCmbData.hasNext()) {
                    String comboid = (String) itrCmbData.next();

                    searchString += "col" + columnno + " = '" + comboid + "' or ";
                }
            }
            if (searchString.length() > 0) {
                searchString = searchString.substring(0, searchString.lastIndexOf("or"));
                searchString = "and (" + searchString + ")";
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return searchString;
    }
    
    
    
     public ModelAndView exportProjectStatusReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            jobj = getProjectStatusReport(request);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
	public ModelAndView getProductExportDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONArray returnArr = getProductExportDetails(request);
            jobj.put("data", returnArr);
            jobj.put("count", returnArr.length());
            issuccess = true;
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

     public JSONArray getProductExportDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONArray returnArray = new JSONArray();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String companyId = sessionHandlerImpl.getCompanyid(request);

//            DateFormat df = authHandler.getDateOnlyFormat();

            //While sending start date & end date as a date filter for report, we need to convert these dates into GMT. Because we have saved request time date in GMT.
            DateFormat dff = authHandler.getGlobalDateFormat();
            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate"))) {
                Date startdate = dff.parse(request.getParameter("startdate"));
                requestParams.put("startdate", startdate);
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                Date enddate = dff.parse(request.getParameter("enddate"));
                requestParams.put("enddate", enddate);
            }

            requestParams.put("companyId", companyId);

            KwlReturnObject retObj = accProductObj.getProductExportDetails(requestParams);

            List list = retObj.getEntityList();
            DateFormat userdf= authHandler.getUserDateFormatterWithoutTimeZone(request);

            if (!list.isEmpty()) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    ProductExportDetail exportDetail = (ProductExportDetail) it.next();
                    JSONObject jobj = new JSONObject();
                    jobj.put("id", exportDetail.getId());
                    jobj.put("filename", exportDetail.getFileName());
                    jobj.put("requestTime", userdf.format(exportDetail.getRequestTime()));
                    jobj.put("status", exportDetail.getStatus());
                    jobj.put("type", exportDetail.getFileType());

                    returnArray.put(jobj);
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex){
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnArray;
    }
    public ModelAndView getProductPriceReportCustVen(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String productid = request.getParameter("ids");
            boolean carryin = Boolean.parseBoolean(request.getParameter("carryin"));
            requestParams.put("carryin", carryin);
            requestParams.put("productid", productid);
            requestParams.put("vendorid", request.getParameter("vids"));
            requestParams.put("productPriceinMultipleCurrency", Boolean.FALSE.parseBoolean(request.getParameter("productPriceinMultipleCurrency")));
            JSONArray DataJArr = accReportsService.getProductPriceReportCustVenJson(request, requestParams);
            JSONArray pagedJson = DataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(DataJArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            // Create column model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "affecteduser");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "priceid");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "carryin");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "price");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currency");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "applydate");
            jobjTemp.put("type", "date");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "productname");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "productcode");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "productid");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "bandlist");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "uom");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", carryin ? messageSource.getMessage("acc.invoice.vendor", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.invoice.customer", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "affecteduser");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "");
            jobjTemp.put("dataIndex", "priceid");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("hidden", true);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.product.UpdatePriceRule.sequenceCM2", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "carryin");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.het.100", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "currency");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("custom", "true");
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.rem.73", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "applydate");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contract.product.name", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "productname");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);            
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header",messageSource.getMessage("acc.field.ProductCode", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "productcode");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("hidden", true);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header8", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "productid");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("hidden", true);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.field.priceSource", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "bandlist");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.masterconfig.setPriceFieldItemPrice", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "price");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.masterConfig.uom", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "uom");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            commData.put("success", true);
            commData.put("coldata", pagedJson);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", DataJArr.length());
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            jobj.put("valid", true);
            jobj.put("data", commData);
            jobj.put("count", DataJArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accReportsController.getProductPriceCustVen : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public ModelAndView exportProductPriceReportCustVen(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            boolean carryin = Boolean.parseBoolean(request.getParameter("carryin"));
            requestParams.put("carryin", carryin);
            requestParams.put("productid", request.getParameter("pid"));
            requestParams.put("vendorid", request.getParameter("vids"));
            requestParams.put("productPriceinMultipleCurrency", Boolean.FALSE.parseBoolean(request.getParameter("productPriceinMultipleCurrency")));
            JSONArray DataJArr = accReportsService.getProductPriceReportCustVenJson(request, requestParams);
            JSONArray pagedJson = DataJArr; //ERP-13641 [SJ]                
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(DataJArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", DataJArr.length());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
                exportDaoObj.processRequest(request, response, jobj);
            } else {
                if (StringUtil.equal(fileType, "csv")) {
                    exportDaoObj.processRequest(request, response, jobj);
                } else if (StringUtil.equal(fileType, "xls")) {
                    exportDaoObj.processRequest(request, response, jobj);
                } else if (StringUtil.equal(fileType, "pdf")) {
                    exportDaoObj.processRequest(request, response, jobj);
                } else {
                    String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                    java.io.ByteArrayOutputStream baos = null;
                    String filename = request.getParameter("filename") + "_v1";
                    String comName = sessionHandlerImpl.getCompanyName(request);
                    baos = ExportrecordObj.exportRatioAnalysis(request, jobj, logoPath, comName);
                    if (baos != null) {
                        ExportrecordObj.writeDataToFile(filename + "." + fileType, baos, response);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
        public ModelAndView getIndiaComplianceReportData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject finaljobj = new JSONObject();
        JSONObject jObj1 = new JSONObject();
        JSONObject jFinal = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String view = "jsonView_ex";//"jsonView";
        try {
            Map<String, Object> requestParams = new HashMap<>();
            String reportId = request.getParameter("reportid");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            DateFormat dfp = authHandler.getDateOnlyFormat(request);
            DateFormat dfGlobal = authHandler.getGlobalDateFormat();
            String startDate = request.getParameter("startdate");
            String endDate = request.getParameter("enddate");
            KwlReturnObject companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) companyresult.getEntityList().get(0);
            KwlReturnObject extracompanypreferencesresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracompanypreferencesresult.getEntityList().get(0);
            KwlReturnObject indiacompanypreferencesresult = accountingHandlerDAOobj.getObject(IndiaComplianceCompanyPreferences.class.getName(), companyId);
            IndiaComplianceCompanyPreferences IndiaCompanyPreferences = (IndiaComplianceCompanyPreferences) indiacompanypreferencesresult.getEntityList().get(0);
                        
            if(reportId.equalsIgnoreCase("11")){
                view = "jsonView";
            }
            if (Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id) {
                switch (reportId) {

                    case "6":   // TDS not deductecd report

                        HashMap tdsDetailHM = new HashMap();
                        tdsDetailHM.put("companyid", companyId);
                        tdsDetailHM.put("startdate", startDate);
                        tdsDetailHM.put("enddate", endDate);

                        List tdsnotdeducted = new ArrayList();
                        
                        KwlReturnObject result = accVendorPaymentDao.getTDSDeductionDetails(tdsDetailHM);
                        List TDSNotDeductedAdvancePayment = result.getEntityList();
                        tdsnotdeducted.addAll(TDSNotDeductedAdvancePayment);
                        String vendorId="";
                        if (!StringUtil.isNullOrEmpty(request.getParameter("vendorid"))) {
                            vendorId = request.getParameter("vendorid");
                        }
                        tdsDetailHM.put("isTDSApplied", true);
                        
                        accReportsService.getInvoicesForTDSNotDeductedReport(tdsDetailHM, tdsnotdeducted);
                        
                        Iterator itr = tdsnotdeducted.iterator();
                        while (itr.hasNext()) {
                            JSONObject jobj = new JSONObject();
                            HashMap res = (HashMap) itr.next();
                            double taxableamount = 0.0;
                            double tdstobemade = 0.0;
                            double deductedtilldate = 0.0;
                            double balancetobemade = 0.0;
                            if(res.containsKey("taxableamount") && res.get("taxableamount")!=null && !StringUtil.isNullOrEmpty(res.get("taxableamount").toString())){
                                taxableamount=Double.parseDouble(res.get("taxableamount").toString());
                            }
                            if(res.containsKey("tdstobemade") && res.get("tdstobemade")!=null && !StringUtil.isNullOrEmpty(res.get("tdstobemade").toString())){
                                tdstobemade=Double.parseDouble(res.get("tdstobemade").toString());
                            }
                            if(res.containsKey("deductedtilldate") && res.get("deductedtilldate")!=null && !StringUtil.isNullOrEmpty(res.get("deductedtilldate").toString())){
                                deductedtilldate=Double.parseDouble(res.get("deductedtilldate").toString());
                            }
                            if(res.containsKey("balancetobemade") && res.get("balancetobemade")!=null && !StringUtil.isNullOrEmpty(res.get("balancetobemade").toString())){
                                balancetobemade=Double.parseDouble(res.get("balancetobemade").toString());
                            }
                            String panNo = res.get("vendorpan") != null ? res.get("vendorpan").toString() : "";
                            
                            if (StringUtil.isNullOrEmpty(panNo)) {
                                String vendorpanstatus=res.get("vendorpanstatus").toString();
                                if (vendorpanstatus.equalsIgnoreCase("2")) {
                                    panNo = "PAN Not Available";
                                } else if (vendorpanstatus.equalsIgnoreCase("3")) {
                                    panNo = "Applied For PAN";
                                }

                            }
                            jobj.put("vendorname", res.get("vendorname") != null ? res.get("vendorname") : "");

                            jobj.put("natureofpayment", res.get("natureofpayment"));
                            jobj.put("vendorspan", panNo);

                            jobj.put("taxableamount", authHandler.round((taxableamount), companyId));
                            jobj.put("tdstobemade", authHandler.round((tdstobemade), companyId));
                            jobj.put("deductedtilldate", authHandler.round((deductedtilldate), companyId));
                            jobj.put("balancetobemade", authHandler.round((balancetobemade), companyId));
                            DataJArr.put(jobj);
                        }
                        break;

                    case "7":   // Unknown Deductee Type report
                        HashMap UnknownDeducteeMap = new HashMap();
                        UnknownDeducteeMap.put("companyid", companyId);
                        UnknownDeducteeMap.put("vendorsWithoutDeductee", true);
                        UnknownDeducteeMap.put(Constants.df, dfp);
                        UnknownDeducteeMap.put("startdate", startDate);
                        UnknownDeducteeMap.put("enddate", endDate);
                        DataJArr = getUnknownDeducteeTypeReport(UnknownDeducteeMap);
                        break;

                    case "8":   // Pan not available report
                        HashMap panStatusMap = new HashMap();
                        panStatusMap.put("companyid", companyId);
                        panStatusMap.put("vendorsWithoutpan", true);
                        panStatusMap.put(Constants.df, dfp);
                        panStatusMap.put("startdate", startDate);
                        panStatusMap.put("enddate", endDate);
                        DataJArr = getPANNotAvailableReport(panStatusMap);
                        break;

                    case "9":   // TDS Calculation : Nature of payment wise ( Each Transaction wise) Report
                        String paymentid = "";
                        boolean tdsPaymentJsonFlag = false;
                        boolean expanderStoreLoad = false;
                        String nop = "", deducteetype = "", asOfDate = "" , paymentMethodId = "";
                        int TDSPaymentType = IndiaComplianceConstants.NOTDSPAID; 
                        double TDSInterestRate = 0;
                        vendorId = "";
                        if (!StringUtil.isNullOrEmpty(request.getParameter("tdsPaymentJsonFlag"))) {
                            tdsPaymentJsonFlag = Boolean.parseBoolean(request.getParameter("tdsPaymentJsonFlag"));
                        }
                        if (!StringUtil.isNullOrEmpty(request.getParameter("expanderStoreLoad"))) {
                            expanderStoreLoad = Boolean.parseBoolean(request.getParameter("expanderStoreLoad"));
                        }
                        if (!StringUtil.isNullOrEmpty(request.getParameter("paymentid"))) {
                            paymentid = request.getParameter("paymentid");
                        }
                        if (!StringUtil.isNullOrEmpty(request.getParameter("vendorid"))) {
                            vendorId = request.getParameter("vendorid");
                        }
                        if (!StringUtil.isNullOrEmpty(request.getParameter("deducteetype"))) {
                            deducteetype = request.getParameter("deducteetype");
                        }
                        if (!StringUtil.isNullOrEmpty(request.getParameter("nop"))) {
                            nop = request.getParameter("nop");
                        }
                        if (!StringUtil.isNullOrEmpty(request.getParameter("tdsPaymentType"))) {
                            TDSPaymentType = Integer.parseInt(request.getParameter("tdsPaymentType"));
                        }
                        if (!StringUtil.isNullOrEmpty(request.getParameter("asOfDate"))) {
                            asOfDate = request.getParameter("asOfDate");
                        }
                        if (IndiaCompanyPreferences != null) {
                            TDSInterestRate = IndiaCompanyPreferences.getTdsInterestRate();
                        }
                        if (extraCompanyPreferences != null) {
                            paymentMethodId = extraCompanyPreferences.getPaymentMethodId();
                        }
                        tdsDetailHM = new HashMap();
                        tdsDetailHM.put("companyid", companyId);
                        tdsDetailHM.put("startdate", startDate);
                        tdsDetailHM.put("enddate", endDate);
                        tdsDetailHM.put(Constants.df, dfGlobal);
                        tdsDetailHM.put("vendorId", vendorId);
                        tdsDetailHM.put("deducteetype", deducteetype);
                        tdsDetailHM.put("nop", nop);
                        if (!StringUtil.isNullOrEmpty(paymentid)) {
                            tdsDetailHM.put("paymentid", paymentid);
                        }
                        tdsDetailHM.put("isPayment", tdsPaymentJsonFlag);
                        tdsDetailHM.put("tdsPaymentJsonFlag", tdsPaymentJsonFlag);
                        tdsDetailHM.put("tdsPaymentType", TDSPaymentType);
                        tdsDetailHM.put("asOfDate", asOfDate);
                        tdsDetailHM.put("TDSInterestRate", TDSInterestRate);
                        tdsDetailHM.put("paymentMethodId", paymentMethodId);
                        DataJArr = accReportsService.getNatureOfPaymentWiseReport(tdsDetailHM);
                        if (tdsPaymentJsonFlag || expanderStoreLoad) {
                            view = "jsonView";
                        }
                        break;
                    case "11":   //VAT Computation Report

                        HashMap paramsHM = new HashMap();
                        paramsHM.put("companyid", companyId);
                        paramsHM.put("startdate", startDate);
                        paramsHM.put("enddate", endDate);

                        boolean makePaymentJsonFlag = false;
                        if(!StringUtil.isNullOrEmpty(request.getParameter("makePaymentJsonFlag"))){
                            makePaymentJsonFlag = Boolean.parseBoolean(request.getParameter("makePaymentJsonFlag"));
                        }

                        if (!StringUtil.isNullOrEmpty(request.getParameter("vatpaymentjedetails")) && request.getParameter("vatpaymentjedetails").equals("true")) {

                            boolean vatOrCSTpaymentFlag = true;
                            JSONObject jobj = new JSONObject();
                            JSONArray jeDetailsArr = new JSONArray();
                            List ivVatDetailList = accInvoiceDAOobj.getInvoiceVatDetails(paramsHM);
                            jeDetailsArr = vatPaymentJEdetails(ivVatDetailList, jeDetailsArr, true, vatOrCSTpaymentFlag, makePaymentJsonFlag);

                            // check for VAT Input Credit Account mapped at Company Level
                            double inputVatCreditBalance = 0.0;
                            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getVatInCreditAvailAcc())) {

                                // ordersequence = 1  - Input VAT

                                KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), extraCompanyPreferences.getVatInCreditAvailAcc());
                                Account account = (Account) accountresult.getEntityList().get(0);

                                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//                                inputVatCreditBalance = accReportsService.getAccountBalance(request, extraCompanyPreferences.getVatInCreditAvailAcc(), null, new Date(endDate));
                                inputVatCreditBalance = accReportsService.getAccountBalance(paramJobj, extraCompanyPreferences.getVatInCreditAvailAcc(), null, new Date(endDate),null);

                                // Add VAT Input Credit Account into Json if Balance is greater than 0.
                                if (inputVatCreditBalance > 0.0) {
                                    
                                    // For Tax Payment - open Make Payment with below json
                                    if (makePaymentJsonFlag) {
                                        JSONObject jtemp = new JSONObject();

                                        jtemp.put("exchangeratefortransaction", 1);
                                        jtemp.put("type", 4);// Document Type - General Ledger Code
                                        jtemp.put("debit", false);
                                        jtemp.put("amountdue", 0);
                                        jtemp.put("srNoForRow", jeDetailsArr.length() + 1);
                                        jtemp.put("masterTypeValue", account.getMastertypevalue());
                                        jtemp.put("documentno", account.getAccountName());
                                        jtemp.put("enteramount", authHandler.round((inputVatCreditBalance), companyId));
                                        jtemp.put("amountDueOriginal", 0);
                                        jtemp.put("documentid", account.getID());
                                        jtemp.put("amountDueOriginalSaved", 0);

                                        jeDetailsArr.put(jtemp);
                                        
                                    } else {  // For VAT Input Credit Carry Forward - open JE Form with below json
                                        
                                        JSONObject jtemp = new JSONObject();

                                        jtemp.put("accountid", account.getID());
                                        jtemp.put("accountname", account.getAccountName());
                                        jtemp.put("debit", "Credit");
                                        jtemp.put("c_amount", authHandler.round((inputVatCreditBalance), companyId));
                                        jtemp.put("c_amount_transactioncurrency", authHandler.round((inputVatCreditBalance), companyId));
                                        jtemp.put("srno", jeDetailsArr.length() + 1);
                                        jtemp.put("description", "");
                                        jtemp.put("mappedaccountid", account.getID());

                                        jeDetailsArr.put(jtemp);
                                    }
                                }
                            }

                            List grVatDetailList = accGoodsReceiptDAOObj.getGoodsRecieptVatDetails(paramsHM);
                            jeDetailsArr = vatPaymentJEdetails(grVatDetailList, jeDetailsArr, false, vatOrCSTpaymentFlag, makePaymentJsonFlag);

                            // Create Final Json
                            if(makePaymentJsonFlag){
                                
                                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
                                SimpleDateFormat sdf2 = new SimpleDateFormat("YYYY-MM-dd");
                                
                                JSONObject jtemp2 = new JSONObject();
                                jtemp2.put("data", jeDetailsArr);
                                jobj.put("Details", jtemp2);
                                jobj.put("memo", "Being VAT Payable for the Period "+ sdf2.format(sdf.parse(startDate)) +" to "+ sdf2.format(sdf.parse(endDate)));
                                
                                KwlReturnObject paymentMethodresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), extraCompanyPreferences.getPaymentMethodId());
                                PaymentMethod paymentMethod = (PaymentMethod) paymentMethodresult.getEntityList().get(0);

                                jobj.put("paymentmethodname", paymentMethod.getMethodName());
                                jobj.put("pmtmethod", paymentMethod.getID());                                
                                jobj.put("billid", "");
                                jobj.put("sequenceformatid", "");
                                jobj.put("bankChargesCmb", "");
                                jobj.put("bankInterestCmb", "");
                                
                                DataJArr.put(jobj);
                                
                            } else {
                                
                                // Check For VAT Payable OR VAT Input Credit Carry Forward Scenario
                                // And Add respective Json entry into Array.
                                
                                double inputVat = 0.0;
                                double outputVat = 0.0;
                                for (int i = 0; i < jeDetailsArr.length(); i++) {
                                    JSONObject jtemp = jeDetailsArr.getJSONObject(i);
                                    if (jtemp.has("debit") && jtemp.getString("debit").equals("Debit")) {
                                        outputVat += jtemp.getDouble("d_amount");
                                    } else {
                                        inputVat += jtemp.getDouble("c_amount");
                                    }
                                }

                                boolean vatPayableFlag = false;
                                double vatpayable = outputVat - inputVat;
                                if (vatpayable > 0) {
                                    if(StringUtil.isNullOrEmpty(extraCompanyPreferences.getPaymentMethodId())){
                                        jobj.put("paymentMethodNotSetFlag", true);
                                    }
                                    vatPayableFlag = true;
                                    JSONObject jtemp = new JSONObject();
                                    jtemp.put("accountid", extraCompanyPreferences.getVatPayableAcc());
                                    jtemp.put("accountname", extraCompanyPreferences.getVatPayableAcc());
                                    jtemp.put("debit", "Credit");
                                    jtemp.put("c_amount", authHandler.round((vatpayable), companyId));
                                    jtemp.put("c_amount_transactioncurrency", authHandler.round((vatpayable), companyId));
                                    jtemp.put("srno", jeDetailsArr.length() + 1);
                                    jtemp.put("description", "");
                                    jtemp.put("mappedaccountid", extraCompanyPreferences.getVatPayableAcc());
                                    jeDetailsArr.put(jtemp);
                                } else if (vatpayable < 0) {
                                    JSONObject jtemp = new JSONObject();
                                    jtemp.put("accountid", extraCompanyPreferences.getVatInCreditAvailAcc());
                                    jtemp.put("accountname", extraCompanyPreferences.getVatInCreditAvailAcc());
                                    jtemp.put("debit", "Debit");
                                    jtemp.put("d_amount",authHandler.round((vatpayable * -1), companyId));
                                    jtemp.put("d_amount_transactioncurrency", authHandler.round((vatpayable * -1), companyId));
                                    jtemp.put("srno", jeDetailsArr.length() + 1);
                                    jtemp.put("description", "");
                                    jtemp.put("mappedaccountid", extraCompanyPreferences.getVatInCreditAvailAcc());
                                    jeDetailsArr.put(jtemp);
    //                            } else {
                                }

                                jobj.put("jeDetails", jeDetailsArr);
                                jobj.put("memo", "VAT Payment");
                                jobj.put("vatPayableFlag", vatPayableFlag);

                                jobj.put("description", "");
                                jobj.put("currencyid", sessionHandlerImpl.getCurrencyID(request));

                                DataJArr.put(jobj);
                            }

                        } else if (!StringUtil.isNullOrEmpty(request.getParameter("cstpaymentjedetails")) && request.getParameter("cstpaymentjedetails").equals("true")) {

                            boolean vatOrCSTpaymentFlag = false;
                            JSONObject jobj = new JSONObject();
                            JSONArray jeDetailsArr = new JSONArray();
                            List ivVatDetailList = accInvoiceDAOobj.getInvoiceVatDetails(paramsHM);
                            jeDetailsArr = vatPaymentJEdetails(ivVatDetailList, jeDetailsArr, true, vatOrCSTpaymentFlag, makePaymentJsonFlag);

                            // Create Final Json
                            if(makePaymentJsonFlag){
                                
                                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
                                SimpleDateFormat sdf2 = new SimpleDateFormat("YYYY-MM-dd");
                                
                                JSONObject jtemp = new JSONObject();
                                jtemp.put("data", jeDetailsArr);
                                jobj.put("Details", jtemp);
                                jobj.put("memo", "Being CST Payable for the Period "+ sdf2.format(sdf.parse(startDate)) +" to "+ sdf2.format(sdf.parse(endDate)));
                                
                                KwlReturnObject paymentMethodresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), extraCompanyPreferences.getPaymentMethodId());
                                PaymentMethod paymentMethod = (PaymentMethod) paymentMethodresult.getEntityList().get(0);

                                jobj.put("paymentmethodname", paymentMethod.getMethodName());
                                jobj.put("pmtmethod", paymentMethod.getID());                                
                                jobj.put("billid", "");
                                jobj.put("sequenceformatid", "");
                                jobj.put("bankChargesCmb", "");
                                jobj.put("bankInterestCmb", "");
                                
                                DataJArr.put(jobj);
                                
                            } else {
                            
                                // If it CST Payment then there will be No Credit Accounts  -- So no need to get CST entries from Goods Receipts (CST Purchases)

                                double cstSales = 0.0;  // inputCST
                                for (int i = 0; i < jeDetailsArr.length(); i++) {
                                    JSONObject jtemp = jeDetailsArr.getJSONObject(i);
                                    if (jtemp.has("debit") && jtemp.getString("debit").equals("Debit")) {
                                        cstSales += jtemp.getDouble("d_amount");
                                    }
                                }

                                if (cstSales > 0) {
                                    if(StringUtil.isNullOrEmpty(extraCompanyPreferences.getPaymentMethodId())){
                                        jobj.put("paymentMethodNotSetFlag", true);
                                    }
                                    JSONObject jtemp = new JSONObject();
                                    jtemp.put("accountid", extraCompanyPreferences.getCSTPayableAcc());
                                    jtemp.put("accountname", extraCompanyPreferences.getCSTPayableAcc());
                                    jtemp.put("debit", "Credit");
                                    jtemp.put("c_amount", authHandler.round(cstSales, companyId));
                                    jtemp.put("c_amount_transactioncurrency", authHandler.round(cstSales, companyId));
                                    jtemp.put("srno", jeDetailsArr.length() + 1);
                                    jtemp.put("description", "");
                                    jtemp.put("mappedaccountid", extraCompanyPreferences.getCSTPayableAcc());
                                    jeDetailsArr.put(jtemp);
                                }
                                jobj.put("jeDetails", jeDetailsArr);
                                jobj.put("memo", "CST Payment");

                                jobj.put("currencyid", sessionHandlerImpl.getCurrencyID(request));

                                DataJArr.put(jobj);
                            }

                        } else { // Data for VAT Computation Report starts here
                            HashMap vatComputationMap = new HashMap();
                            vatComputationMap.put("companyid", companyId);
                            vatComputationMap.put("startdate", startDate);
                            vatComputationMap.put("enddate", endDate);
                            
                            String vatInCreditAvailAcc = extraCompanyPreferences.getVatInCreditAvailAcc();
                            double inputVatCreditBalance = 0.0;
                            if (!StringUtil.isNullOrEmpty(vatInCreditAvailAcc)) {
                                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//                                inputVatCreditBalance = accReportsService.getAccountBalance(request, extraCompanyPreferences.getVatInCreditAvailAcc(), null, new Date(endDate));
                                inputVatCreditBalance = accReportsService.getAccountBalance(paramJobj, vatInCreditAvailAcc, null, new Date(endDate),null);
                            }
                            vatComputationMap.put("vatInCreditAvailAcc", extraCompanyPreferences.getVatInCreditAvailAcc());
                            vatComputationMap.put("inputVatCreditBalance", inputVatCreditBalance);
                            
                            DataJArr = getVATComputationReport(vatComputationMap);
                        }
                        break;
                    case "12":   // Excise Computation Report: Credit Adjustment
                        JSONObject jobj = new JSONObject();
                        boolean CreditAdjustmentflag = !StringUtil.isNullOrEmpty(request.getParameter("CreditAdjustmentflag"))? Boolean.parseBoolean(request.getParameter("CreditAdjustmentflag")) :false;
                        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                        DateFormat df = authHandler.getDateOnlyFormat(request);
                        JSONArray jeDetailsArr = new JSONArray();
                        HashMap CreditAdjustmentHM = new HashMap();
                        CreditAdjustmentHM.put("companyid", companyId);
                        CreditAdjustmentHM.put(Constants.df, df);
                        CreditAdjustmentHM.put("startdate", df.format(preferences.getFinancialYearFrom()));//Payable From Financial Year From Date to "To Date".
                        CreditAdjustmentHM.put("enddate", endDate);
                        CreditAdjustmentHM.put("isexcisepaid", 0);//0- Non paid, 1- Paid.
                        CreditAdjustmentHM.put("CreditAdjustmentflag", CreditAdjustmentflag);
                        result = accInvoiceDAOobj.getComputationReportDetailsTransactionWise(CreditAdjustmentHM);
                        Iterator itr1 = result.getEntityList().iterator();
                        String termAccount = null;
                        double dutyTotalAmount = 0.0d;
                        while (itr1.hasNext()) {
                            Object[] oj = (Object[]) itr1.next();
                            termAccount = oj[2].toString();
                            dutyTotalAmount += Double.parseDouble(oj[3].toString());
                        }
                        if (!StringUtil.isNullOrEmpty(termAccount)) {
                            KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), termAccount);
                            Account account = (Account) accountresult.getEntityList().get(0);
                            JSONObject jtemp = new JSONObject();
                            jtemp.put("accountid", account.getID());
                            jtemp.put("accountname", account.getAccountName());
                            jtemp.put("debit", "Debit");
                            jtemp.put("d_amount", dutyTotalAmount);
                            jtemp.put("d_amount_transactioncurrency", dutyTotalAmount);
                            jtemp.put("srno", jeDetailsArr.length() + 1);
                            jtemp.put("description", "A+B");
                            jtemp.put("mappedaccountid", account.getID());
                            jeDetailsArr.put(jtemp);
                        
                            jtemp = new JSONObject();
                            jtemp.put("accountid","" );
                            jtemp.put("accountname","" );
                            jtemp.put("debit", "Credit");
                            jtemp.put("c_amount", dutyTotalAmount);
                            jtemp.put("c_amount_transactioncurrency", dutyTotalAmount);
                            jtemp.put("srno", jeDetailsArr.length() + 1);
                            jtemp.put("description", "Credit Adjustment");
                            jtemp.put("mappedaccountid","" );
                            jeDetailsArr.put(jtemp);
                            
                            double inputexciseCreditBalance = 0.0;
                        if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getExcisePayableAcc())) {
                            KwlReturnObject Exciseaccresult = accountingHandlerDAOobj.getObject(Account.class.getName(), extraCompanyPreferences.getExcisePayableAcc());
                            Account Exciseaccount = (Account) Exciseaccresult.getEntityList().get(0);
                            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//                            inputexciseCreditBalance = accReportsService.getAccountBalance(request, extraCompanyPreferences.getExcisePayableAcc(), null, new Date(endDate));
                            inputexciseCreditBalance = accReportsService.getAccountBalance(paramJobj, extraCompanyPreferences.getExcisePayableAcc(), null, new Date(endDate),null);
                            if (inputexciseCreditBalance > 0.0) {
                                JSONObject jextemp = new JSONObject();
                                jextemp.put("accountid", Exciseaccount.getID());
                                jextemp.put("accountname", Exciseaccount.getAccountName());
                                jextemp.put("debit", "Credit");
                                jextemp.put("c_amount", inputexciseCreditBalance);
                                jextemp.put("c_amount_transactioncurrency", inputexciseCreditBalance);
                                jextemp.put("srno", jeDetailsArr.length() + 1);
                                jextemp.put("description", "Excise Duty Payable");
                                jextemp.put("mappedaccountid", Exciseaccount.getID());

                                jeDetailsArr.put(jextemp);
                }
            }
                        }
                        jobj.put("jeDetails", jeDetailsArr);
                        jobj.put("memo", CreditAdjustmentflag? "Credit Adjustment": "Excise Payment");
                        jobj.put("currencyid",sessionHandlerImpl.getCurrencyID(request));

                        DataJArr.put(jobj);
                       
                        break;
                }
            }
            finaljobj.put("data", DataJArr);
            finaljobj.put("count", DataJArr.length());
            finaljobj.put("success", true);
            
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", finaljobj.toString());
    }
        
    public ModelAndView getRule16Register(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            finaljobj = getRule16RegisterJson(request,false);
           
        } catch(Exception ex){
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new ModelAndView("jsonView", "model", finaljobj.toString());

    }
    
    public ModelAndView exportRule16Register(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            jobj = getRule16RegisterJson(request,true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public JSONObject getRule16RegisterJson(HttpServletRequest request, boolean isExport) {
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "", exciseUnit = "";
        boolean issuccess = false;
        String templateid = "";
        try {
            HashMap requestParams = new HashMap();
            DateFormat userdf=authHandler.getUserDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                requestParams.put("startdate", request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                requestParams.put("productid", request.getParameter("productid"));
            }
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            DateFormat df = authHandler.getDateOnlyFormat();
            requestParams.put("df", df);
            requestParams.put("companyid", companyid);
            requestParams.put("isSalesReturnCreditNote", true);
            KwlReturnObject kwl = accInvoiceDAOobj.getSalesReturn(requestParams);
            List srList = kwl.getEntityList();
            Iterator itr = srList.iterator();
            if (!StringUtil.isNullOrEmpty(request.getParameter("exciseunit"))) {
                exciseUnit = request.getParameter("exciseunit");
                List modt = new ArrayList();
                requestParams.put("companyunitid", exciseUnit);
                KwlReturnObject kwltemp = accountingHandlerDAOobj.getModuleTemplates(requestParams);
                modt = kwltemp.getEntityList();
                if (modt.size() > 0) {
                    ModuleTemplate moduleTemp = (ModuleTemplate) modt.get(0);
                    templateid = moduleTemp.getTemplateId();
//                    requestParams.put("moduletemplateid", templateid);

                }
            }
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String id = oj[0].toString();
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), id);
                SalesReturn sr = (SalesReturn) objItr.getEntityList().get(0);
                Set<SalesReturnDetail> salesReturnRows = sr.getRows();
                String srNumber = sr.getSalesReturnNumber();
                for (SalesReturnDetail srdetail : salesReturnRows) {
                    if (srdetail.getCidetails() != null) {
//                        if (srdetail.getCidetails().getInvoice() != null && srdetail.getCidetails().getInvoice().isIsExciseInvoice()) {
//                            if((!StringUtil.isNullOrEmpty(templateid) && srdetail.getCidetails().getInvoice().getModuletemplateid() != null && 
//                                    srdetail.getCidetails().getInvoice().getModuletemplateid().getTemplateId().equals(templateid)) || exciseUnit.equals("")){
//                                String inVDate = srdetail.getCidetails().getInvoice().getJournalEntry() != null ? userdf.format(srdetail.getCidetails().getInvoice().getJournalEntry().getEntryDate()) : "";
//                                String uom = "";
//                                if (srdetail.getUom() != null) {
//                                    uom = !StringUtil.isNullOrEmpty(srdetail.getUom().getName()) ? srdetail.getUom().getName() : "";
//                                }
//                                double totalExcise = 0;
//                                HashMap<String, Object> srdetailParams = new HashMap();
//                                srdetailParams.put("salesReturnDetailid", srdetail.getID());
//                                KwlReturnObject salesMapresult = accInvoiceDAOobj.getSalesReturnDetailTermMap(srdetailParams);
//                                List<SalesReturnDetailsTermMap> SalesReturnDetailsTermMapList = salesMapresult.getEntityList();
//                                for (SalesReturnDetailsTermMap salesReturnDetailTermMap : SalesReturnDetailsTermMapList) {
//                                    if (salesReturnDetailTermMap.getTerm() != null && salesReturnDetailTermMap.getTerm().getTermType() == 2) {
//                                        totalExcise += salesReturnDetailTermMap.getTermamount();
//                                    }
//                                }
//                                double rowTermAmountExcise = totalExcise;
//                                double rate = srdetail.getRate();
//                                double returnQuantity = srdetail.getReturnQuantity();
////                            double amountofcreditavailed = rowTermAmount + (rate * returnQuantity); // ERP-23212 Need to capture term amount only
//                                String productDetails = !StringUtil.isNullOrEmpty(srdetail.getProduct().getDescription()) ? srdetail.getProduct().getDescription() : "";
//                                String invNumber = srdetail.getCidetails().getInvoice() != null ? srdetail.getCidetails().getInvoice().getInvoiceNumber() : "";
//                                JSONObject jobjSR = new JSONObject();
//                                jobjSR.put("particularsofinvoiceboe", invNumber + " / " + inVDate);
//                                jobjSR.put("owninvoiceno", srNumber + " / " + userdf.format(sr.getOrderDate()));
//                                jobjSR.put("partyinvoiceno", invNumber + " / " + inVDate);
//                                jobjSR.put("descriptionofgoods", productDetails);
//                                jobjSR.put("quantityrejected", returnQuantity);
//                                jobjSR.put("reasonforrejection", srdetail.getReason() != null ? srdetail.getReason().getValue() : "");
//                                jobjSR.put("uom", uom);
//                                jobjSR.put("amountofcreditavailed", authHandler.formattingDecimalForAmount(rowTermAmountExcise, companyid));
//                                dataArr.put(jobjSR);
//                            }
//                        }
                    }
                }
            }
            int totalcount=dataArr.length();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            finaljobj.put("data", dataArr);
            finaljobj.put("count", totalcount);
            finaljobj.put("success", true);

        } catch (SessionExpiredException | ServiceException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return finaljobj;

    }

   public HashMap vatComputation(List vatDetailList, HashMap finalHm, boolean taxType) {
       
        // Hardcoded Order required in report as below       
        // ordersequence = 0  - Output VAT
        // ordersequence = 1  - Input VAT        
        // ordersequence = 2  - VAT Paid
        // ordersequence = 3  - VAT Payable
        // ordersequence = 4  - CST Sales
        // ordersequence = 5  - CST Paid
        // ordersequence = 6  - CST Payable
        // ordersequence = 7  - CST Purchases

        int ordersequence = 1;
        String taxTypeName= "Input VAT";
        if(taxType){
            ordersequence = 0;
            taxTypeName= "Output VAT";
        }
       
        Iterator itr = vatDetailList.iterator();
        while (itr.hasNext()) {
            HashMap vatComputationHM = new HashMap();
            Object[] oj = (Object[]) itr.next();

            String accName = oj[0].toString(); //Account Name
            String accId = oj[1].toString(); // Account uuid
            double percent = (Double) oj[2]; // Percentage
            double assessableValues = (Double) oj[3]; // Assessable Value
            double termamount = (Double) oj[4]; // Term Amount (Calculated Tax Amount)
            int termType = (Integer) oj[5]; // Term type (Tax Description)
            int taxPaidFlag = (Integer) oj[6]; // taxPaidFlag - 0 TAX not paid & 1 - TAX Paid
            
            // If CST then change taxTypeName to CST
            if(termType == 3){ // Term Type 3 for CST
                ordersequence = 7;
                taxTypeName= "CST on Inter State Purchases";//CST Purchases";
                if(taxType){
                    ordersequence = 4;
                    taxTypeName= "CST on Inter State Sales /Outward Stock Transfers";// "CST Sales";
               }
            }

            vatComputationHM.put("taxtype", taxTypeName);
            vatComputationHM.put("particulars", accName);
            vatComputationHM.put("assessablevalue", assessableValues);
            vatComputationHM.put("taxamount", termamount);
            vatComputationHM.put("accId", accId);
            vatComputationHM.put("ordersequence", ordersequence);

            if (finalHm.containsKey(accId)) {
                HashMap tempHm = (HashMap) finalHm.get(accId);
                
                if((Integer) tempHm.get("ordersequence") == ordersequence){
                    double existingAssasable = (Double) tempHm.get("assessablevalue");
                    tempHm.put("assessablevalue", existingAssasable + assessableValues);

                    double existingtaxamount = (Double) tempHm.get("taxamount");
                    tempHm.put("taxamount", existingtaxamount + termamount);
    
                    finalHm.put(accId, tempHm);
                } else {
                    finalHm.put(accId, vatComputationHM);
                }
            } else {
                finalHm.put(accId, vatComputationHM);
            }
            
            if(termType == 1){ // Term Type 1 for VAT
                if(taxPaidFlag == 0){
                    if (finalHm.containsKey("totalVatPayble")) {
                        double totalVatPayble = (Double) finalHm.get("totalVatPayble");
                        finalHm.put("totalVatPayble", taxType ? totalVatPayble + termamount : totalVatPayble - termamount);

                    } else {
                        finalHm.put("totalVatPayble",taxType ?termamount:(termamount * -1));
                    }
                } else {
                    if (finalHm.containsKey("totalVatPaid")) {
                        double totalVatPaid = (Double) finalHm.get("totalVatPaid");
                        finalHm.put("totalVatPaid", taxType ? totalVatPaid + termamount : totalVatPaid - termamount);

                    } else {
                        finalHm.put("totalVatPaid",taxType ?termamount:(termamount * -1));
                    }
                }
            }
            
            if(termType == 3 && taxType){ // Term Type 3 for CST
                if(taxPaidFlag == 0){
                    if (finalHm.containsKey("totalCSTPayble")) {
                        double totalCSTPayble = (Double) finalHm.get("totalCSTPayble");
                        finalHm.put("totalCSTPayble", totalCSTPayble + termamount);

                    } else {
                        finalHm.put("totalCSTPayble", termamount);
                    }
                } else {
                    if (finalHm.containsKey("totalCSTPaid")) {
                        double totalCSTPaid = (Double) finalHm.get("totalCSTPaid");
                        finalHm.put("totalCSTPaid", totalCSTPaid + termamount);

                    } else {
                        finalHm.put("totalCSTPaid", termamount);
                    }
                }
            }
        }
        
        return finalHm;
    }
   // Filters Purchase or sales return list if excise is applied in invoice
    public List filterVatList(List vatDetailList, boolean isSales, String companyId) {
        Iterator itr = vatDetailList.iterator();
        while (itr.hasNext()) {
            Object[] oj = (Object[]) itr.next();

            String invoiceid = oj[7] != null ? (String) oj[7] : "";

            try {
                if (!StringUtil.isNullOrEmpty(invoiceid)) {
                    boolean isExciseApplied = false;
                    HashMap<String, Object> data = new HashMap<String, Object>();
                    data.put("invoiceid", invoiceid);
                    data.put("companyid", companyId);
                    if (isSales) {
                        isExciseApplied = accInvoiceDAOobj.isTaxApplied(data, IndiaComplianceConstants.LINELEVELTERMTYPE_Excise_DUTY);
                    } else {
                        isExciseApplied = accGoodsReceiptDAOObj.isTaxApplied(data, IndiaComplianceConstants.LINELEVELTERMTYPE_Excise_DUTY);
                    }
                    if (isExciseApplied) {
                        itr.remove();
                    }
                } else {
                    itr.remove();
                }
            } catch (ServiceException ex) {
                Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return vatDetailList;
    }
   
    // vatOrCSTpaymentFlag  =  true - vatPayment  AND  false - CSTPayment
    public JSONArray vatPaymentJEdetails(List vatDetailList, JSONArray jeDetailsArr, boolean taxType, boolean vatOrCSTpaymentFlag, boolean makePaymentJsonFlag) throws JSONException, ServiceException {
        int srno = 1;
        JSONObject finaljobj = new JSONObject();
        if(jeDetailsArr.length() > 0){
            srno = jeDetailsArr.length() + 1;
        }
        Iterator itr = vatDetailList.iterator();
        while (itr.hasNext()) {
            JSONObject jobj = new JSONObject();
            Object[] oj = (Object[]) itr.next();

            String accName = oj[0].toString(); //Account Name
            String accId = oj[1].toString(); // Account uuid
            double percent = (Double) oj[2]; // Percentage
            double assessableValues = (Double) oj[3]; // Assessable Value
            double termamount = (Double) oj[4]; // Term Amount (Calculated Tax Amount)
            int termType = (Integer) oj[5]; // Term type (Tax Description)
            int taxPaidFlag = (Integer) oj[6]; // taxPaidFlag - 0 TAX not paid & 1 - TAX Paid
            
//            if(termType == 1 && vatPaidFlag == 0){
            if((vatOrCSTpaymentFlag && termType == 1 && taxPaidFlag == 0) // For VAT Payment, get VAT entries From Invoices with VAT not paid
                || (!vatOrCSTpaymentFlag && termType == 3 && taxPaidFlag == 0)){ // For CST Payment, get CST entries From Invoices with CST not paid
                
                // For Tax Payment - open Make Payment with below json
                if(makePaymentJsonFlag){
                    
                    if(finaljobj.has(accId)){
                        
                        JSONObject jtemp = finaljobj.getJSONObject(accId);
                        jtemp.put("enteramount", jtemp.getDouble("enteramount") + termamount);
                        
                        finaljobj.put(accId, jtemp);
                    } else {
                        
                        KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accId);
                        Account account = (Account) accountresult.getEntityList().get(0);
                    
                        jobj.put("exchangeratefortransaction", 1);
                        jobj.put("type", 4);// Document Type - General Ledger Code
                        if(taxType){
                            jobj.put("debit", true);
                        } else {
                            jobj.put("debit", false);
                        }
                        jobj.put("amountdue", 0);
                        jobj.put("srNoForRow", srno);
                        jobj.put("masterTypeValue", account.getMastertypevalue());
                        jobj.put("documentno", accName);
                        jobj.put("enteramount", termamount);
                        jobj.put("amountDueOriginal", 0);
                        jobj.put("documentid", accId);
                        jobj.put("amountDueOriginalSaved", 0);

                        finaljobj.put(accId, jobj);
                        srno ++;
                    }
                    
                } else {// For VAT Input Credit Carry Forward - open JE Form with below json

                    if(finaljobj.has(accId)){
                        JSONObject jtemp = finaljobj.getJSONObject(accId);
                        if(jtemp.has("debit") && jtemp.getString("debit").equals("Debit")){
                            jtemp.put("d_amount", jtemp.getDouble("d_amount") + termamount);
                            jtemp.put("d_amount_transactioncurrency", jtemp.getDouble("d_amount_transactioncurrency") + termamount);
                        }else if(jtemp.has("debit") && jtemp.getString("debit").equals("Credit")){
                            jtemp.put("c_amount", jtemp.getDouble("c_amount") + termamount);
                            jtemp.put("c_amount_transactioncurrency", jtemp.getDouble("c_amount_transactioncurrency") + termamount);
                        }
                        finaljobj.put(accId, jtemp);
                    } else {
                        jobj.put("accountid", accId);
                        jobj.put("accountname", accName);
                        if(taxType){
                            jobj.put("debit", "Debit");
                            jobj.put("d_amount", termamount);
                            jobj.put("d_amount_transactioncurrency", termamount);
                        } else {
    //                    } else if(vatOrCSTpaymentFlag) { // If it CST Payment then there will be No Credit Accounts
                            jobj.put("debit", "Credit");
                            jobj.put("c_amount", termamount);
                            jobj.put("c_amount_transactioncurrency", termamount);
                        }
                        jobj.put("srno", srno);
                        jobj.put("description", "");
                        jobj.put("mappedaccountid", accId);

                        finaljobj.put(accId, jobj);
                        //jeDetailsArr.put(jobj);
                        srno ++;
                    }
                }
            }
        }
        
        Iterator jitr = finaljobj.keys();
        while(jitr.hasNext()){
            String key = (String) jitr.next();
            jeDetailsArr.put(finaljobj.get(key));
        }
        return jeDetailsArr;
    }
    
    public ModelAndView updateVATorCSTPayemntFlagOnJEPosting(HttpServletRequest request, HttpServletResponse response) {
        String view = "jsonView";//"jsonView_ex";
        JSONObject finaljobj = new JSONObject();
        try {
            
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String startDate = request.getParameter("startdate");
            String endDate = request.getParameter("enddate");
            boolean vatPaymentFlag = false;
            boolean cstPaymentFlag = false;
            boolean excisePaymentFlag = false;
            String journalentryid = request.getParameter("journalentryid")!=null ? request.getParameter("journalentryid") : "";
            String paymentid = request.getParameter("paymentid")!=null ? request.getParameter("paymentid") : "";
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("vatPaymentFlag"))){
                vatPaymentFlag = Boolean.parseBoolean(request.getParameter("vatPaymentFlag").toString());
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("cstPaymentFlag"))){
                cstPaymentFlag = Boolean.parseBoolean(request.getParameter("cstPaymentFlag").toString());
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("excisePaymentFlag"))){
                excisePaymentFlag = Boolean.parseBoolean(request.getParameter("excisePaymentFlag").toString());
            }
            
            HashMap paramsHM = new HashMap();
            paramsHM.put("companyid", companyId);
            paramsHM.put("startdate", startDate);
            paramsHM.put("enddate", endDate);
            if(vatPaymentFlag){
                paramsHM.put("vatpaidflag", 1);
            }
            if(cstPaymentFlag){
                paramsHM.put("cstpaidflag", 1);
            }
            if(excisePaymentFlag){
                paramsHM.put("excisepaidflag", 1);
            }
            if(!StringUtil.isNullOrEmpty(journalentryid)){
                paramsHM.put("journalentryid", journalentryid);
            }
            if(!StringUtil.isNullOrEmpty(paymentid)){
                paramsHM.put("paymentid", paymentid);
            }
            
            accInvoiceDAOobj.updateInvoiceTaxPaidFlag(paramsHM);
            accGoodsReceiptDAOObj.updateGoodsRecieptTaxPaidFlag(paramsHM);
            accInvoiceDAOobj.updateSalesReturnTaxPaidFlag(paramsHM);
            accGoodsReceiptDAOObj.updatePurchaseReturnTaxPaidFlag(paramsHM);
            finaljobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", finaljobj.toString());
    }
    public ModelAndView updateTDSPaymentFlag(HttpServletRequest request, HttpServletResponse response) {
        String view = "jsonView";//"jsonView_ex";
        JSONObject finaljobj = new JSONObject();
        try {
            
            HashMap paramsHM = new HashMap();
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String startDate = request.getParameter("startdate");
            String endDate = request.getParameter("enddate");
            String paymentid = request.getParameter("paymentid")!=null ? request.getParameter("paymentid") : "";
            int tdsPaymentType = !StringUtil.isNullOrEmpty((String)request.getParameter("tdsPaymentType")) ? Integer.parseInt(request.getParameter("tdsPaymentType")) : 0;
            String vendorId = "", deducteecode = "",nop = "";
            double tdsInterestRateAtTimeOfPayment=0.0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("vendorid"))) {
                vendorId = request.getParameter("vendorid");
                paramsHM.put("vendorId", vendorId);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("deducteetype"))) {
                deducteecode = request.getParameter("deducteetype");
                paramsHM.put("deducteecode", deducteecode);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("nop"))) {
                nop = request.getParameter("nop");
                paramsHM.put("nop", nop);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("tdsInterestRateAtPaymentTime"))) {
                tdsInterestRateAtTimeOfPayment = Double.parseDouble(request.getParameter("tdsInterestRateAtPaymentTime"));
                paramsHM.put("tdsInterestRateAtPaymentTime", tdsInterestRateAtTimeOfPayment);
            }
            
            paramsHM.put("companyid", companyId);
            paramsHM.put("startdate", startDate);
            paramsHM.put("enddate", endDate);
            paramsHM.put("tdspaidflag", tdsPaymentType);
            if(!StringUtil.isNullOrEmpty(paymentid)){
                paramsHM.put("paymentid", paymentid);
            }
            
            accGoodsReceiptDAOObj.updateGoodsRecieptTDSPaidFlag(paramsHM);
            accVendorPaymentDao.updateAdvancePaymentTDSPaidFlag(paramsHM);
            accDebitNoteobj.updateDebitNoteTDSPaidFlag(paramsHM);
            finaljobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", finaljobj.toString());
    }
    public ModelAndView getDailyStockRegister(HttpServletRequest request, HttpServletResponse response) {
        JSONObject finaljobj = new JSONObject();
        try {
            finaljobj = getJSONDailyStockRegister(request, false);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());

    }

    public ModelAndView exportDailyStockRegister(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            jobj = getJSONDailyStockRegister(request, true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    /**
     * Description : This Method is used to Export India Compliance Reports(Unknown Deductee Type Report, PAN Not Available Report)
     * @param <request> used to get request parameters
     */
    public ModelAndView exportIndiaComplianceReportData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String view = Constants.jsonView_ex;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            DateFormat dfp = authHandler.getDateOnlyFormat(request);
            String startDate = request.getParameter("startdate");
            String endDate = request.getParameter("enddate");
            String asOfDate = request.getParameter("asOfDate");
            String repID = request.getParameter("reportid");
            HashMap ExportDataMap = new HashMap();
            ExportDataMap.put("companyid", companyId);
            ExportDataMap.put(Constants.df, dfp);
            ExportDataMap.put("startdate", startDate);
            ExportDataMap.put("enddate", endDate);
            
            KwlReturnObject extracompanypreferencesresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracompanypreferencesresult.getEntityList().get(0);
            KwlReturnObject indiacompanypreferencesresult = accountingHandlerDAOobj.getObject(IndiaComplianceCompanyPreferences.class.getName(), companyId);
            IndiaComplianceCompanyPreferences IndiaCompanyPreferences = (IndiaComplianceCompanyPreferences) indiacompanypreferencesresult.getEntityList().get(0);
            switch(repID){
                case Constants.UnkownDeducteeTypeReportID: // Export Unknown Deductee Type Report
                    ExportDataMap.put("vendorsWithoutDeductee", true);
                    dataArr = getUnknownDeducteeTypeReport(ExportDataMap);
                    break;
                case Constants.PANNotAvailableReportID://Export PANNot Available Report
                    ExportDataMap.put("vendorsWithoutpan", true);
                    dataArr = getPANNotAvailableReport(ExportDataMap);
                    break;
                case Constants.NatureOfPaymentWiseReportID://Export PANNot Available Report
                    ExportDataMap.put("companyid", companyId);
                    ExportDataMap.put("startdate", startDate);
                    ExportDataMap.put("enddate", endDate);
                    ExportDataMap.put(Constants.df, authHandler.getGlobalDateFormat());
                    ExportDataMap.put("isPayment", false);
                    ExportDataMap.put("tdsPaymentJsonFlag", false);
                    ExportDataMap.put("asOfDate", asOfDate);
                    ExportDataMap.put("TDSInterestRate", IndiaCompanyPreferences.getTdsInterestRate());
                    dataArr = accReportsService.getNatureOfPaymentWiseReport(ExportDataMap);
                    break;
                case "11":
                    String vatInCreditAvailAcc = extraCompanyPreferences.getVatInCreditAvailAcc();
                    double inputVatCreditBalance = 0.0;
                    if (!StringUtil.isNullOrEmpty(vatInCreditAvailAcc)) {
                        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//                                inputVatCreditBalance = accReportsService.getAccountBalance(request, extraCompanyPreferences.getVatInCreditAvailAcc(), null, new Date(endDate));
                        inputVatCreditBalance = accReportsService.getAccountBalance(paramJobj, vatInCreditAvailAcc, null, new Date(endDate),null);
                    }
                    ExportDataMap.put("vatInCreditAvailAcc", extraCompanyPreferences.getVatInCreditAvailAcc());
                    ExportDataMap.put("inputVatCreditBalance", inputVatCreditBalance);
                    ExportDataMap.put("isExport", true);

                    dataArr = getVATComputationReport(ExportDataMap);
                    break;
            }
            finaljobj.put("data", dataArr);
            finaljobj.put("totalcount", dataArr.length());
            finaljobj.put("count", dataArr.length());
            finaljobj.put("success", true);
            if(finaljobj.length() > 0){
                exportDaoObj.processRequest(request, response, finaljobj);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, Constants.model, finaljobj.toString());
    }

    /**
     * Description : This Method is used to generate Unknown Deductee Type Report Data map
     * @param <UnknownDeducteeMap> used to get request parameters
     * @return :< dataArr > JSONArray of Vendor having Unknown Deductee Type
     */
    public JSONArray getUnknownDeducteeTypeReport(HashMap UnknownDeducteeMap) {
        JSONArray dataArr = new JSONArray();
        KwlReturnObject result = null;
        Iterator itr = null;
        String companyId = "";
        try {
            if (UnknownDeducteeMap.containsKey("companyid")) {
                companyId = (String) UnknownDeducteeMap.get("companyid");
            }
            //To Fetch Vendors
            result = accVendorDAOobj.getVendor(UnknownDeducteeMap);
            itr = result.getEntityList().iterator();
            while (itr.hasNext()) {
                JSONObject jobj = new JSONObject();
                String panNo = "None";
                Vendor v = (Vendor) itr.next();
                jobj.put("vendorname", v.getName());
                jobj.put("vendorcode", v.getAcccode());
                jobj.put("deducteetype", "Unknown");
                jobj.put("vendorresidentialstatus", v.getResidentialstatus() == 0 ? "Resident" : "Non-Resident");
                if (v.getPanStatus().equalsIgnoreCase("2")) {
                    panNo = "PAN Not Available";
                } else if (v.getPanStatus().equalsIgnoreCase("3")) {
                    panNo = "Applied For PAN";
                } else {
                    panNo = v.getPANnumber();
                }
                jobj.put("panno", panNo);

                String contactperson = "";
                String contactno = "";
                String email = "";
                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put("vendorid", v.getID());
                addrRequestParams.put("companyid", companyId);
                KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                if (!addressResult.getEntityList().isEmpty()) {
                    VendorAddressDetails vendAddrList = (VendorAddressDetails) addressResult.getEntityList().get(0);
                    contactperson = vendAddrList.getContactPerson();
                    contactno = vendAddrList.getContactPersonNumber();
                    email = vendAddrList.getEmailID();
                }
                jobj.put("contactperson", contactperson);
                jobj.put("contactnumber", contactno);
                jobj.put("email", email);
                dataArr.put(jobj);
            }
        } catch (Exception e) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, e);
        }
        return dataArr;
    }
    
    /**
     * Description : This Method is used to generate PAN Not Available Report Data map
     * @param < panStatusMap > used to get request parameters
     * @return :< dataArr > JSONArray of Vendor having PAN Not Available
     */
    public JSONArray getPANNotAvailableReport(HashMap panStatusMap) {
        JSONArray dataArr = new JSONArray();
        KwlReturnObject result = null;
        Iterator itr = null;
        String companyId = "";
        try {
            if (panStatusMap.containsKey("companyid")) {
                companyId = (String) panStatusMap.get("companyid");
            }
            //To Fetch Vendors
            result = accVendorDAOobj.getVendor(panStatusMap);
            if (result != null) {
                itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    JSONObject jobj = new JSONObject();
                    String panStatus = "None";
                    Vendor v = (Vendor) itr.next();
                    jobj.put("vendorname", v.getName());
                    jobj.put("vendorcode", v.getAcccode());
                    String deducteeType = "";
                    KwlReturnObject deducteetype = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), v.getDeducteeType());
                    if (deducteetype != null && !StringUtil.isNullOrEmpty(v.getDeducteeType())) {
                        MasterItem masteritem = (MasterItem) deducteetype.getEntityList().get(0);
                        deducteeType = masteritem.getValue();
                    } else {
                        deducteeType = "unknown";
                    }
                    jobj.put("deducteetype", deducteeType);
                    jobj.put("vendorresidentialstatus", v.getResidentialstatus() == 0 ? "Resident" : "Non-Resident");
                    if (v.getPanStatus().equalsIgnoreCase("2")) {
                        panStatus = "PAN Not Available";
                    } else if (v.getPanStatus().equalsIgnoreCase("3")) {
                        panStatus = "Applied For PAN";
                    }
                    jobj.put("panno", panStatus);

                    String contactperson = "";
                    String contactno = "";
                    String email = "";
                    HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                    addrRequestParams.put("vendorid", v.getID());
                    addrRequestParams.put("companyid", companyId);
                    KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                    if (!addressResult.getEntityList().isEmpty()) {
                        VendorAddressDetails vendAddrList = (VendorAddressDetails) addressResult.getEntityList().get(0);
                        contactperson = vendAddrList.getContactPerson();
                        contactno = vendAddrList.getContactPersonNumber();
                        email = !StringUtil.isNullObject(vendAddrList.getEmailID()) ? vendAddrList.getEmailID() : "";
                    }
                    jobj.put("contactperson", contactperson);
                    jobj.put("contactnumber", contactno);
                    jobj.put("email", email);
                    dataArr.put(jobj);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, e);
        }
        return dataArr;
    }
    
    public JSONArray getVATComputationReport(HashMap vatComputationMap) throws ServiceException, JSONException {
        JSONArray DataJArr = new JSONArray();

        String companyId = "";
        if(vatComputationMap.containsKey("companyid") && vatComputationMap.get("companyid")!=null){
            companyId = vatComputationMap.get("companyid").toString();
        }
        String startDate = "";
        if(vatComputationMap.containsKey("startdate") && vatComputationMap.get("startdate")!=null){
            startDate = vatComputationMap.get("startdate").toString();
        }
        String endDate = "";
        if(vatComputationMap.containsKey("enddate") && vatComputationMap.get("enddate")!=null){
            endDate = vatComputationMap.get("enddate").toString();
        }
        
        String vatInCreditAvailAcc = "";
        if(vatComputationMap.containsKey("vatInCreditAvailAcc") && vatComputationMap.get("vatInCreditAvailAcc")!=null){
            vatInCreditAvailAcc = vatComputationMap.get("vatInCreditAvailAcc").toString();
        }
        
        boolean isExport = false;
        if(vatComputationMap.containsKey("isExport") && vatComputationMap.get("isExport")!=null && vatComputationMap.get("isExport")!=""){
            isExport = Boolean.parseBoolean(vatComputationMap.get("isExport").toString());
        }
        
        HashMap paramsHM = new HashMap();
        paramsHM.put("companyid", companyId);
        paramsHM.put("startdate", startDate);
        paramsHM.put("enddate", endDate);
        
        double inputVatCreditBalance = 0.0;
        if(vatComputationMap.containsKey("inputVatCreditBalance") && vatComputationMap.get("inputVatCreditBalance")!=null){
            inputVatCreditBalance = Double.parseDouble(vatComputationMap.get("inputVatCreditBalance").toString());
        }
        
        HashMap finalHm = new HashMap();
        
        if (!StringUtil.isNullOrEmpty(vatInCreditAvailAcc)) {

            // ordersequence = 1  - Input VAT

            KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), vatInCreditAvailAcc);
            Account account = (Account) accountresult.getEntityList().get(0);

            JSONObject jobj = new JSONObject();
            jobj.put("taxtype", "Input VAT");
            jobj.put("particulars", account.getAccountName());
            jobj.put("assessablevalue", "");
            jobj.put("taxamount", authHandler.round(inputVatCreditBalance, companyId));
            jobj.put("ordersequence", 1);

            DataJArr.put(jobj);
        }

        List ivVatDetailList = accInvoiceDAOobj.getInvoiceVatDetails(paramsHM);
        finalHm = vatComputation(ivVatDetailList, finalHm, true);  // true for OP vat
        List grVatDetailList = accGoodsReceiptDAOObj.getGoodsRecieptVatDetails(paramsHM);
        finalHm = vatComputation(grVatDetailList, finalHm, false);  // false for IP vat
         /*
        *  If Purchase Return with debit note or Sales Return with credit note 
        *  and if excise is not applied then vat amount should be reduced
        */
        List prVatDetailList = accGoodsReceiptDAOObj.getPurchaseReturnVatDetails(paramsHM);
        prVatDetailList = filterVatList(prVatDetailList,false,companyId);
        finalHm = vatComputation(prVatDetailList, finalHm,false);  
        List srVatDetailList = accInvoiceDAOobj.getSalesReturnVatDetails(paramsHM);
        srVatDetailList = filterVatList(srVatDetailList,true,companyId);
        finalHm = vatComputation(srVatDetailList, finalHm, true);  
        //                        List PRVatDetailList = accPurchaseOrderDAOobj.getPurchaseRerturnVatDetails(companyId);
        //                        finalHm = vatComputation(PRVatDetailList, finalHm, true);
        //                        List SRVatDetailList = accSalesOrderDAOobj.getSalesReturnVatDetails(companyId);
        //                        finalHm = vatComputation(SRVatDetailList, finalHm, false);

        // ordersequence = 2  - VAT Paid
        // ordersequence = 3  - VAT Payable
        // ordersequence = 5  - CST Paid
        // ordersequence = 6  - CST Payable
        Iterator Finalitr = finalHm.keySet().iterator();
        while (Finalitr.hasNext()) {
            JSONObject jobj = new JSONObject();
            String key = Finalitr.next().toString();
            if (key.equalsIgnoreCase("totalVatPaid")) {
                jobj.put("taxtype", "VAT Paid");
                jobj.put("particulars", "VAT Paid");
                jobj.put("assessablevalue", "");
                jobj.put("taxamount", authHandler.round((double) finalHm.get("totalVatPaid"), companyId));
                jobj.put("ordersequence", 2);

                DataJArr.put(jobj);
            } else if (key.equalsIgnoreCase("totalCSTPaid")) {
                jobj.put("taxtype", "CST on Inter State Sales /Outward Stock Transfers Paid");
                jobj.put("particulars", "CST on Inter State Sales /Outward Stock Transfers Paid");
                jobj.put("assessablevalue", "");
                jobj.put("taxamount", authHandler.round((double) finalHm.get("totalCSTPaid"), companyId));
                jobj.put("ordersequence", 5);

                DataJArr.put(jobj);
            } else if (key.equalsIgnoreCase("totalVatPayble")) {
                jobj.put("taxtype", "VAT Payable or (Input Tax Credit Carried Forward)");
                jobj.put("particulars", "VAT Payable or (Input Tax Credit Carried Forward)");
                jobj.put("assessablevalue", "");
                jobj.put("taxamount", authHandler.round(((double) finalHm.get("totalVatPayble") - inputVatCreditBalance), companyId));
                jobj.put("ordersequence", 3);

                DataJArr.put(jobj);
            } else if (key.equalsIgnoreCase("totalCSTPayble")) {
                jobj.put("taxtype", "CST on Inter State Sales /Outward Stock Transfers Payable");
                jobj.put("particulars", "CST on Inter State Sales /Outward Stock Transfers Payable");
                jobj.put("assessablevalue", "");
                jobj.put("taxamount", authHandler.round((double) finalHm.get("totalCSTPayble"), companyId));
                jobj.put("ordersequence", 6);

                DataJArr.put(jobj);
            } else {
                HashMap hm = (HashMap) finalHm.get(key);
                jobj.put("taxtype", hm.get("taxtype"));
                jobj.put("particulars", hm.get("particulars"));
                jobj.put("assessablevalue", authHandler.round((double) hm.get("assessablevalue"), companyId));
                jobj.put("taxamount", authHandler.round((double) hm.get("taxamount"), companyId));
                jobj.put("ordersequence", hm.get("ordersequence"));

                DataJArr.put(jobj);
            }
        }
        
        if(isExport){
            // Sort by ordersequence.
            DataJArr = StringUtil.sortJsonArray(DataJArr, "ordersequence", false, true);
            
            // Add Subtotal for Groups
            JSONArray finalDataArr = new JSONArray();
            double subtotalAssessableValue = 0.0;
            double subtotalTaxAmount = 0.0;
            int previousOrdersequence = -1;
            for(int i=0; i<DataJArr.length(); i++){
                JSONObject jtemp = DataJArr.getJSONObject(i);
                if(previousOrdersequence == -1){
                    previousOrdersequence = jtemp.getInt("ordersequence");
                }
                
                // if previous == current   THEN sum up and push current into array.
                if(previousOrdersequence == jtemp.getInt("ordersequence")){
                    if(jtemp.has("assessablevalue") && !StringUtil.isNullOrEmpty(jtemp.getString("assessablevalue"))){
                        subtotalAssessableValue += jtemp.getDouble("assessablevalue");
                    }
                    if(jtemp.has("taxamount") && !StringUtil.isNullOrEmpty(jtemp.getString("taxamount"))){
                        subtotalTaxAmount += jtemp.getDouble("taxamount");
                    }
                    finalDataArr.put(jtemp);
                } else { // else previous != current   THEN push summaryJsonobjtotal and reset variables with current and push current into array
                    JSONObject summaryJObj = new JSONObject();
                    summaryJObj.put("particulars", "Sub Total");
                    summaryJObj.put("assessablevalue", subtotalAssessableValue);
                    summaryJObj.put("taxamount", subtotalTaxAmount);
                    summaryJObj.put("ordersequence", previousOrdersequence);
                    summaryJObj.put(IndiaComplianceConstants.boldAndItalicFontStyleFlag, true);
                    finalDataArr.put(summaryJObj);
                    
                    previousOrdersequence = jtemp.getInt("ordersequence");
                    subtotalAssessableValue = 0.0;
                    subtotalTaxAmount = 0.0;
                    if(jtemp.has("assessablevalue") && !StringUtil.isNullOrEmpty(jtemp.getString("assessablevalue"))){
                        subtotalAssessableValue = jtemp.getDouble("assessablevalue");
                    }
                    if(jtemp.has("taxamount") && !StringUtil.isNullOrEmpty(jtemp.getString("taxamount"))){
                        subtotalTaxAmount = jtemp.getDouble("taxamount");
                    }
                    finalDataArr.put(jtemp);
                }
                
                // If Last Record, then push the summary
                if(i == DataJArr.length()-1){
                    JSONObject summaryJObj = new JSONObject();
                    summaryJObj.put("particulars", "Sub Total");
                    summaryJObj.put("assessablevalue", subtotalAssessableValue);
                    summaryJObj.put("taxamount", subtotalTaxAmount);
                    summaryJObj.put("ordersequence", previousOrdersequence);
                    summaryJObj.put(IndiaComplianceConstants.boldAndItalicFontStyleFlag, true);
                    finalDataArr.put(summaryJObj);
                }
            }
            DataJArr = finalDataArr;
        }
        return DataJArr;
    }
    
     public JSONObject getJSONDailyStockRegister(HttpServletRequest request, boolean isExport) {
        JSONObject finaljobj = new JSONObject();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        JSONArray dataArr = new JSONArray();
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        try {
            double totalQuantity = 0.0;
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
            requestParams.put(Constants.df, df);
            if (!isExport) {
                requestParams.put("start", start);
                requestParams.put("limit", limit);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                requestParams.put("startdate", request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("productId"))) {
                String productid = request.getParameter("productId");
                requestParams.put("productid", productid);
                KwlReturnObject initialQObj = accProductObj.getInitialQuantity(productid);
                List initial = initialQObj.getEntityList();
                if(initial != null && initial.get(0) != null ){
                    totalQuantity = (double) initial.get(0);
                }
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("exciseunit"))) {
                requestParams.put("exciseunit", request.getParameter("exciseunit"));
            }
            KwlReturnObject kwl = accProductObj.getDailyStockRegister(requestParams);

            List list = kwl.getEntityList();
            Iterator itr = list.iterator();
            
            while (itr.hasNext()) {
                Object[] dataObject = (Object[]) itr.next();

                BigInteger transactionType = (BigInteger) (dataObject[0]!= null ? dataObject[0]: 99);
                String pid = (String) (dataObject[1] != null?dataObject[1]:"");
                String productCode = (String) (dataObject[2] != null?dataObject[2]:"");
                String productDescription = (String) (dataObject[3] != null?dataObject[3]:"");
                Date transactionDate = (Date) (dataObject[4] != null?dataObject[4]:new Date());
                String transactionNumber = (String) (dataObject[5] != null?dataObject[5]:"");
                double quantity = (double) (dataObject[6] != null?dataObject[6]:0.0);
                String uomid = (String) (dataObject[7] != null?dataObject[7]:"");
                String packagingid = (String) (dataObject[8] != null?dataObject[8]:"");
                String hsncode = (String) (dataObject[10] != null?dataObject[10]:"");
                String transactionid = (String) (dataObject[12] != null?dataObject[12]:"");
                String transactiondetailid = (String) (dataObject[13] != null?dataObject[13]:"");

                KwlReturnObject uomObj = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), uomid);
                UnitOfMeasure uom = (UnitOfMeasure) uomObj.getEntityList().get(0);


                JSONObject jsonobj = new JSONObject();
                //Assembly Main Product
                 if(transactionType.intValue() == 0 ){
                    double homeClearanceStock = 0.0;
                    jsonobj.put("openingBalnace",authHandler.formattingDecimalForQuantity(totalQuantity, companyid));

                    KwlReturnObject packagingObj = accountingHandlerDAOobj.getObject(Packaging.class.getName(), packagingid);
                    Packaging pkg = (Packaging) packagingObj.getEntityList().get(0);
                    double uomquantity = pkg.getQuantityInStockUoM(uom, quantity);

                    totalQuantity -=  uomquantity;

                    jsonobj.put("quantityManufactured","" );
                    jsonobj.put("totalQuantity",authHandler.formattingDecimalForQuantity(totalQuantity, companyid));
                    jsonobj.put("issuedForHomeClearance",authHandler.formattingDecimalForQuantity(quantity, companyid));
                    jsonobj.put("uom","");
                    jsonobj.put("uomsales",(uom!= null && !StringUtil.isNullOrEmpty(uom.getNameEmptyforNA()))?uom.getNameEmptyforNA():"");
                    jsonobj.put("transactionNumber", "");
                    jsonobj.put("issuedForProduction",transactionNumber);
                    jsonobj.put("looseNoOfReals", "");
                    jsonobj.put("storeClosingbalance", uomquantity);
                    jsonobj.put("finishedRoomClosingbalance", "");
                    jsonobj.put("educationCess", "");
                    jsonobj.put("totalAmountOFTransaction", "");
                    jsonobj.put("additionalExciseDutyAmount", "");
                    jsonobj.put("additionalExciseDutyRate", "");
                    jsonobj.put("exciseDutyAmount", "");
                    jsonobj.put("exciseDutyRate", "");
                    jsonobj.put("quantity", "");
                    jsonobj.put("purpose", "");
                    jsonobj.put("exportUnderClaimForRebateAMOUNT", "");
                    jsonobj.put("exportUnderClaimForRebateQTY", "");
                    jsonobj.put("valueOfHomeClearanceStock",homeClearanceStock);
                } else if(transactionType.intValue() == 1 ){
                    //Assembly Sub Product
                    double homeClearanceStock = 0.0;
                    jsonobj.put("openingBalnace",authHandler.formattingDecimalForQuantity(totalQuantity, companyid));

                    KwlReturnObject packagingObj = accountingHandlerDAOobj.getObject(Packaging.class.getName(), packagingid);
                    Packaging pkg = (Packaging) packagingObj.getEntityList().get(0);
                    double uomquantity = pkg.getQuantityInStockUoM(uom, quantity);

                    totalQuantity -=  uomquantity;

                    jsonobj.put("quantityManufactured","" );
                    jsonobj.put("totalQuantity",authHandler.formattingDecimalForQuantity(totalQuantity, companyid));
                    jsonobj.put("issuedForHomeClearance",authHandler.formattingDecimalForQuantity(quantity, companyid));
                    jsonobj.put("uom","");
                    jsonobj.put("uomsales",(uom!= null && !StringUtil.isNullOrEmpty(uom.getNameEmptyforNA()))?uom.getNameEmptyforNA():"");
                    jsonobj.put("transactionNumber","" );
                    jsonobj.put("issuedForProduction",transactionNumber);
                    jsonobj.put("looseNoOfReals", "");
                    jsonobj.put("storeClosingbalance", uomquantity);
                    jsonobj.put("finishedRoomClosingbalance", "");
                    jsonobj.put("educationCess", "");
                    jsonobj.put("totalAmountOFTransaction", "");
                    jsonobj.put("additionalExciseDutyAmount", "");
                    jsonobj.put("additionalExciseDutyRate", "");
                    jsonobj.put("exciseDutyAmount", "");
                    jsonobj.put("exciseDutyRate", "");
                    jsonobj.put("quantity", "");
                    jsonobj.put("purpose", "");
                    jsonobj.put("exportUnderClaimForRebateAMOUNT", "");
                    jsonobj.put("exportUnderClaimForRebateQTY", "");
                    jsonobj.put("valueOfHomeClearanceStock", homeClearanceStock);
                } else if(transactionType.intValue() == 2){
                    // Purchase Invoice
                    
                    double homeClearanceStock = 0.0;
                    double exciseRate = 0.0;
                    double totalExciseDuty = 0.0;
                    double totalExcise = 0.0;
                    double additionalExciseRate = 0.0;
                    double totalBaseAmount = 0.0;
                    double assessableValue = 0.0;
                    double totalAdditionalExcise = 0.0;
                    if(!StringUtil.isNullOrEmpty(transactionid)){
                        KwlReturnObject grObj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), transactionid);
                        GoodsReceipt gr = (grObj != null && grObj.getEntityList() != null && grObj.getEntityList().size()>0) ?(GoodsReceipt)grObj.getEntityList().get(0): null;
                       
                        if(!StringUtil.isNullOrEmpty(transactiondetailid)){
                            HashMap<String, Object> GoodsReceiptDetailParams = new HashMap<String, Object>();
                            GoodsReceiptDetailParams.put("GoodsReceiptDetailid", transactiondetailid);
                            GoodsReceiptDetailParams.put("orderbyadditionaltax", true);
                            GoodsReceiptDetailParams.put("termtype", 2);
                            KwlReturnObject grdTermMapresult = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(GoodsReceiptDetailParams);
                            List<ReceiptDetailTermsMap> excise = grdTermMapresult.getEntityList();
                            for (ReceiptDetailTermsMap exciseTermMap : excise) {
                                LineLevelTerms mt = exciseTermMap.getTerm();
                                assessableValue = exciseTermMap.getAssessablevalue();
                                if(mt.isIsAdditionalTax()){
                                    additionalExciseRate = exciseTermMap.getPercentage();
                                    totalAdditionalExcise += exciseTermMap.getTermamount();
                                } else{
                                    exciseRate = exciseTermMap.getPercentage();
                                    totalExcise += exciseTermMap.getTermamount();
                                }
                            }
                        }
                        totalBaseAmount = gr != null ?gr.getInvoiceAmountInBase():0;
                    }
                    totalExciseDuty = totalAdditionalExcise + totalExcise;
                    homeClearanceStock = totalBaseAmount + totalAdditionalExcise + totalExcise;
                    
                    
                    jsonobj.put("openingBalnace",authHandler.formattingdecimal(totalQuantity, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));

                    double baseUOMRate =  Double.parseDouble(dataObject[9].toString()); 
                    double uomquantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate, companyid);

                    totalQuantity +=  uomquantity;

                    jsonobj.put("quantityManufactured",authHandler.formattingDecimalForQuantity(quantity, companyid) );
                    jsonobj.put("totalQuantity",authHandler.formattingDecimalForQuantity(totalQuantity, companyid));
                    jsonobj.put("issuedForHomeClearance","");
                    jsonobj.put("uom",(uom!= null && !StringUtil.isNullOrEmpty(uom.getNameEmptyforNA()))?uom.getNameEmptyforNA():"");
                    jsonobj.put("transactionNumber",transactionNumber );
                    jsonobj.put("issuedForProduction","");
                    jsonobj.put("uomsales","");
                    jsonobj.put("looseNoOfReals", "");
                    jsonobj.put("storeClosingbalance", uomquantity);
                    jsonobj.put("finishedRoomClosingbalance", "");
                    jsonobj.put("totalAmountOFTransaction", CustomDesignHandler.getAmountinCommaDecimal(totalExciseDuty, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    jsonobj.put("additionalExciseDutyAmount", CustomDesignHandler.getAmountinCommaDecimal(totalAdditionalExcise, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    jsonobj.put("additionalExciseDutyRate", CustomDesignHandler.getAmountinCommaDecimal(additionalExciseRate, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    jsonobj.put("exciseDutyAmount", CustomDesignHandler.getAmountinCommaDecimal(totalExcise, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    jsonobj.put("exciseDutyRate", CustomDesignHandler.getAmountinCommaDecimal(exciseRate, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    jsonobj.put("quantity", "");
                    jsonobj.put("purpose", "");
                    jsonobj.put("exportUnderClaimForRebateAMOUNT", "");
                    jsonobj.put("exportUnderClaimForRebateQTY", "");
                    jsonobj.put("valueOfHomeClearanceStock", CustomDesignHandler.getAmountinCommaDecimal(assessableValue, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                } else if(transactionType.intValue() == 3){
                    // Inventory Stock Register
                    double homeClearanceStock = 0.0;
                    jsonobj.put("openingBalnace",authHandler.formattingDecimalForQuantity(totalQuantity, companyid));

                    KwlReturnObject packagingObj = accountingHandlerDAOobj.getObject(Packaging.class.getName(), packagingid);
                    Packaging pkg = (Packaging) packagingObj.getEntityList().get(0);
                    double uomquantity = pkg.getQuantityInStockUoM(uom, quantity);

                    totalQuantity -=  uomquantity;

                    jsonobj.put("quantityManufactured","" );
                    jsonobj.put("totalQuantity",authHandler.formattingDecimalForQuantity(totalQuantity, companyid));
                    jsonobj.put("issuedForHomeClearance",authHandler.formattingDecimalForQuantity(quantity, companyid));
                    jsonobj.put("uom","");
                    jsonobj.put("uomsales",(uom!= null && !StringUtil.isNullOrEmpty(uom.getNameEmptyforNA()))?uom.getNameEmptyforNA():"");
                    jsonobj.put("transactionNumber",transactionNumber );
                    jsonobj.put("issuedForProduction","");
                    jsonobj.put("looseNoOfReals", "");
                    jsonobj.put("storeClosingbalance", uomquantity);
                    jsonobj.put("finishedRoomClosingbalance", "");
                    jsonobj.put("educationCess", "");
                    jsonobj.put("totalAmountOFTransaction", "");
                    jsonobj.put("additionalExciseDutyAmount", "");
                    jsonobj.put("additionalExciseDutyRate", "");
                    jsonobj.put("exciseDutyAmount", "");
                    jsonobj.put("exciseDutyRate", "");
                    jsonobj.put("quantity", "");
                    jsonobj.put("purpose", "");
                    jsonobj.put("exportUnderClaimForRebateAMOUNT", "");
                    jsonobj.put("exportUnderClaimForRebateQTY", "");
                    jsonobj.put("valueOfHomeClearanceStock", homeClearanceStock);
                } else if(transactionType.intValue() == 4){
                    // Sales Invoice
                    double homeClearanceStock = 0.0;
                    double exciseRate = 0.0;
                    double totalExciseDuty = 0.0;
                    double totalExcise = 0.0;
                    double additionalExciseRate = 0.0;
                    double totalBaseAmount = 0.0;
                    double assessableValue = 0.0;
                    double totalAdditionalExcise = 0.0;
                     if (!StringUtil.isNullOrEmpty(transactionid)) {
                         KwlReturnObject invoiceObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), transactionid);
                         Invoice invoice = (invoiceObj != null && invoiceObj.getEntityList() != null && invoiceObj.getEntityList().size()>0)?(Invoice) invoiceObj.getEntityList().get(0):null;
                         if (!StringUtil.isNullOrEmpty(transactiondetailid)) {
                             HashMap<String, Object> InvoiceDetailParams = new HashMap<String, Object>();
                             InvoiceDetailParams.put("InvoiceDetailid", transactiondetailid);
                             InvoiceDetailParams.put("orderbyadditionaltax", true);
                             InvoiceDetailParams.put("termtype", 2);
                             KwlReturnObject indTermMapresult = accInvoiceDAOobj.getInvoicedetailTermMap(InvoiceDetailParams);
                             List<InvoiceDetailTermsMap> excise = indTermMapresult.getEntityList();
                             for (InvoiceDetailTermsMap exciseTermMap : excise) {
                                 LineLevelTerms mt = exciseTermMap.getTerm();
                                 assessableValue = exciseTermMap.getAssessablevalue();
                                 if (mt.isIsAdditionalTax()) {
                                     additionalExciseRate = exciseTermMap.getPercentage();
                                     totalAdditionalExcise += exciseTermMap.getTermamount();
                                 } else {
                                     exciseRate = exciseTermMap.getPercentage();
                                     totalExcise += exciseTermMap.getTermamount();
                                 }
                             }
                         }
                         totalBaseAmount = invoice!= null ? invoice.getInvoiceamountinbase():0;
                     }
                    totalExciseDuty = totalAdditionalExcise + totalExcise;
                    homeClearanceStock = totalBaseAmount + totalAdditionalExcise + totalExcise;
                    
                    jsonobj.put("openingBalnace",authHandler.formattingDecimalForQuantity(totalQuantity, companyid));

                    double baseUOMRate = Double.parseDouble(dataObject[9].toString());
                    double uomquantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate, companyid);

                    totalQuantity -=  uomquantity;

                    jsonobj.put("quantityManufactured","" );
                    jsonobj.put("totalQuantity",authHandler.formattingDecimalForQuantity(totalQuantity, companyid));
                    jsonobj.put("issuedForHomeClearance",authHandler.formattingDecimalForQuantity(quantity, companyid));
                    jsonobj.put("uom","");
                    jsonobj.put("uomsales",(uom!= null && !StringUtil.isNullOrEmpty(uom.getNameEmptyforNA()))?uom.getNameEmptyforNA():"");
                    jsonobj.put("transactionNumber",transactionNumber );
                    jsonobj.put("issuedForProduction","");
                    jsonobj.put("looseNoOfReals", "");
                    jsonobj.put("storeClosingbalance", uomquantity);
                    jsonobj.put("finishedRoomClosingbalance", "");
                    jsonobj.put("totalAmountOFTransaction", CustomDesignHandler.getAmountinCommaDecimal(totalExciseDuty, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    jsonobj.put("additionalExciseDutyAmount", CustomDesignHandler.getAmountinCommaDecimal(totalAdditionalExcise, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    jsonobj.put("additionalExciseDutyRate", CustomDesignHandler.getAmountinCommaDecimal(additionalExciseRate, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    jsonobj.put("exciseDutyAmount", CustomDesignHandler.getAmountinCommaDecimal(totalExcise, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    jsonobj.put("exciseDutyRate", CustomDesignHandler.getAmountinCommaDecimal(exciseRate, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    jsonobj.put("quantity", "");
                    jsonobj.put("purpose", "");
                    jsonobj.put("exportUnderClaimForRebateAMOUNT", "");
                    jsonobj.put("exportUnderClaimForRebateQTY", "");
                    jsonobj.put("valueOfHomeClearanceStock", CustomDesignHandler.getAmountinCommaDecimal(assessableValue, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                }
                String date = (transactionDate != null)?userdf.format(transactionDate):"";
                jsonobj.put("pid",pid);
                jsonobj.put("productid",productCode);
                jsonobj.put("productDesc",productDescription);
                jsonobj.put("transactionDate", date);
                jsonobj.put("issuedForRepacking","");
                jsonobj.put("issuedUnderBond","");
                jsonobj.put("issuedUnderLUT","");
                jsonobj.put("hsncode",hsncode);
                jsonobj.put("educationCess", CustomDesignHandler.getAmountinCommaDecimal(0, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                jsonobj.put("sign","");
                dataArr.put(jsonobj);
            }
            int totalCount = dataArr.length();
            if ((!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) && !isExport) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            finaljobj.put("data",dataArr);
            finaljobj.put("totalCount",totalCount);
            finaljobj.put("count",totalCount);
            finaljobj.put("success",true);

        } catch (ServiceException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finaljobj;

    }
    
    public ModelAndView getVatSalesRegister(HttpServletRequest request, HttpServletResponse response) {
        JSONObject finaljobj = new JSONObject();
        HashMap<String,Object> requestParams = new HashMap<String,Object>();
        JSONArray dataArr = new JSONArray();
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        try {
            DateFormat df=authHandler.getDateOnlyFormat();
            DateFormat userdf=authHandler.getUserDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
            requestParams.put(Constants.df, df);
            requestParams.put("userdf", userdf);
            requestParams.put("dateformat", df);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                requestParams.put("startdate", request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            //Function moved to service layer.
            dataArr = accReportsService.getVatSalesRegister(requestParams);
            int totalCount = dataArr.length();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            finaljobj.put("data",dataArr);
            finaljobj.put("totalcount",totalCount);
            finaljobj.put("count",totalCount);
            finaljobj.put("success",true);
            
        } catch (ServiceException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());

    }
    
    public ModelAndView getVatPurchaseRegister(HttpServletRequest request, HttpServletResponse response) {
        JSONObject finaljobj = new JSONObject();
        HashMap<String,Object> requestParams = new HashMap<String,Object>();
        JSONArray dataArr = new JSONArray();
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        try {
            DateFormat df=authHandler.getDateOnlyFormat();
            DateFormat userdf=authHandler.getUserDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
            requestParams.put(Constants.df, df);
            requestParams.put("dateformat", df);
            requestParams.put("userdf", userdf);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                requestParams.put("startdate", request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            //Function moved to service layer.
            dataArr = accReportsService.getVatPurchaseRegister(requestParams);
            int totalCount = dataArr.length();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            finaljobj.put("data",dataArr);
            finaljobj.put("totalcount",totalCount);
            finaljobj.put("count",totalCount);
            finaljobj.put("success",true);
            
        } catch (ServiceException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());

    }
    
    public ModelAndView getRG23PartII(HttpServletRequest request, HttpServletResponse response) {
        JSONObject finaljobj = new JSONObject();
        try {
            finaljobj = getJSONRG23PartII(request, false);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());
    }
    
    public ModelAndView exportRG23PartIIReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            jobj = getJSONRG23PartII(request, true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public JSONObject getJSONRG23PartII(HttpServletRequest request, boolean isExport) {
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            HashMap requestParams = new HashMap();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                requestParams.put("startdate", request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("natureOfStockItem"))) {
                requestParams.put("natureOfStockItem", request.getParameter("natureOfStockItem"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isExciseInvoice"))) {
                requestParams.put("isExciseInvoice", request.getParameter("isExciseInvoice"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("includeAllRec"))) {
                requestParams.put("includeAllRec", request.getParameter("includeAllRec"));
            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("exciseUnit"))) {
                String exciseUnit = request.getParameter("exciseUnit");
                List modt = new ArrayList();
                requestParams.put("companyunitid", exciseUnit);
                KwlReturnObject kwltemp = accountingHandlerDAOobj.getModuleTemplates(requestParams);
                modt = kwltemp.getEntityList();
                if (modt.size() > 0) {
                    ModuleTemplate moduleTemp = (ModuleTemplate) modt.get(0);
                    String templateid = moduleTemp.getTemplateId();
                    requestParams.put("moduletemplateid", templateid);

                }
            }
            
            requestParams.put("sort", "date");
            requestParams.put("dir", "ASC");
            requestParams.put(Constants.df, df);
            requestParams.put("dateformat", df);
            requestParams.put("nondeleted", true);

            KwlReturnObject kwl = accGoodsReceiptDAOObj.getGoodsReceiptsMerged(requestParams);
            List piList = kwl.getEntityList();
            Iterator piitr = piList.iterator();
            double openingBalance = 0.0;
            while (piitr.hasNext()) {
                String vendorName = "";
                String ECCnumber = "";
                String IECNo = "";
                String rangeCode = "";
                String city = "";
                String ECCIECRangeCity = "";
                Object[] oj = (Object[]) piitr.next();
                String grid = oj[0].toString();
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grid);
                GoodsReceipt goodsReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                Set<GoodsReceiptDetail> rows = goodsReceipt.getRows();

                if (goodsReceipt.getVendor() != null) {
                    vendorName = goodsReceipt.getVendor().getName() != null ? goodsReceipt.getVendor().getName() : "";
                    ECCnumber = goodsReceipt.getVendor().getECCnumber() != null ? goodsReceipt.getVendor().getECCnumber() : "";
                    IECNo = goodsReceipt.getVendor().getIECNo() != null ? goodsReceipt.getVendor().getIECNo() : "";
                    rangeCode = goodsReceipt.getVendor().getRangecode() != null ? goodsReceipt.getVendor().getRangecode() : "";
                }

                //------------Address---------------------------

                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put("vendorid", goodsReceipt.getVendor().getID());
                addrRequestParams.put("companyid", companyid);
                KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                if (!addressResult.getEntityList().isEmpty()) {
                    List<VendorAddressDetails> vendAddrList = addressResult.getEntityList();
                    for (VendorAddressDetails vendAddr : vendAddrList) {
                        city = vendAddr.getCity() != null ? vendAddr.getCity() : "";
                    }
                }

                if (!StringUtil.isNullOrEmpty(ECCnumber)) {
                    ECCIECRangeCity = ECCnumber;
                }
                if (!StringUtil.isNullOrEmpty(IECNo)) {
                    if (!StringUtil.isNullOrEmpty(ECCIECRangeCity)) {
                        ECCIECRangeCity = ECCIECRangeCity + " - " + IECNo;
                    } else {
                        ECCIECRangeCity = ECCIECRangeCity + IECNo;
                    }
                }
                if (!StringUtil.isNullOrEmpty(rangeCode)) {
                    if (!StringUtil.isNullOrEmpty(ECCIECRangeCity)) {
                        ECCIECRangeCity = ECCIECRangeCity + " - " + rangeCode;
                    } else {
                        ECCIECRangeCity = ECCIECRangeCity + rangeCode;
                    }
                }
                if (!StringUtil.isNullOrEmpty(city)) {
                    if (!StringUtil.isNullOrEmpty(ECCIECRangeCity)) {
                        ECCIECRangeCity = ECCIECRangeCity + " - " + city;
                    } else {
                        ECCIECRangeCity = ECCIECRangeCity + city;
                    }
                }
                //----------------------------------------------
                String pi = goodsReceipt.getGoodsReceiptNumber();
                Date creationDate = null;
                JournalEntry je = null;
                if (goodsReceipt.isNormalInvoice()) {
                    je = goodsReceipt.getJournalEntry();
                }
                creationDate = goodsReceipt.getCreationDate();
//                if (goodsReceipt.isIsOpeningBalenceInvoice() && !goodsReceipt.isNormalInvoice()) {
//                    creationDate = goodsReceipt.getCreationDate();
//                } else {
//                    creationDate = je.getEntryDate();
//                }
                double totalExcise = 0;
                double otherDuties = 0;
                boolean isCreditAvailed = false;
                for (GoodsReceiptDetail row : rows) {
                    if (row.getInventory() != null && row.getInventory().getProduct() != null && row.getInventory().getProduct().getNatureofStockItem() != null) {
                        if (StringUtil.isNullOrEmpty(request.getParameter("natureOfStockItem")) || (row.getInventory().getProduct().getNatureofStockItem()).equals(request.getParameter("natureOfStockItem"))) {
                            HashMap<String, Object> GoodsReceiptDetailParams = new HashMap<String, Object>();
                            GoodsReceiptDetailParams.put("GoodsReceiptDetailid", row.getID());
                            KwlReturnObject grdTermMapresult = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(GoodsReceiptDetailParams);
                            List<ReceiptDetailTermsMap> ReceiptDetailTermsMapList = grdTermMapresult.getEntityList();
                            for (ReceiptDetailTermsMap invoicedetailTermMap : ReceiptDetailTermsMapList) {
                                if(invoicedetailTermMap.getCreditAvailedFlag() == IndiaComplianceConstants.CREDIT_AVAILED_EXCISE_INVOICE){
                                    isCreditAvailed = true;
                                }
                                if (invoicedetailTermMap.getTerm() != null && invoicedetailTermMap.getTerm().getTermType() == 2) {
                                    totalExcise += invoicedetailTermMap.getTermamount();
                                }
                            }
                        }
                    }
                }
//                if (!StringUtil.isNullOrEmpty(request.getParameter("natureOfStockItem"))) {
                    double totalCreditAvailable = openingBalance+totalExcise; 
                    double creditadjustmentAmount = isCreditAvailed ?totalExcise:0;
                    double balanceCredit = totalCreditAvailable - creditadjustmentAmount;
                    JSONObject grJsonobj = new JSONObject();
                    grJsonobj.put("cenvat", CustomDesignHandler.getAmountinCommaDecimal(openingBalance, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    grJsonobj.put("totalexcise", CustomDesignHandler.getAmountinCommaDecimal(totalCreditAvailable, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    openingBalance = balanceCredit;
                    grJsonobj.put("nameofsupplier", vendorName);
                    grJsonobj.put("date", creationDate);
                    grJsonobj.put("invoiceno", pi);
                    grJsonobj.put("additionalduty", CustomDesignHandler.getAmountinCommaDecimal(0, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    grJsonobj.put("ECCIECRangeCity", ECCIECRangeCity);
                    grJsonobj.put("folioentry", "");
                    grJsonobj.put("officerinitial", "");
                    grJsonobj.put("remark", "");
                    grJsonobj.put("debitotherduties", CustomDesignHandler.getAmountinCommaDecimal(otherDuties, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    grJsonobj.put("debitexcise", CustomDesignHandler.getAmountinCommaDecimal(creditadjustmentAmount, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    grJsonobj.put("creditotherduties", CustomDesignHandler.getAmountinCommaDecimal(otherDuties, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    grJsonobj.put("creditexcise", CustomDesignHandler.getAmountinCommaDecimal(balanceCredit, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    grJsonobj.put("cetno", "");
                    grJsonobj.put("invoice_date", "");
                    grJsonobj.put("totalotherduties", CustomDesignHandler.getAmountinCommaDecimal(otherDuties, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    grJsonobj.put("otherduties", CustomDesignHandler.getAmountinCommaDecimal(otherDuties, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    grJsonobj.put("excise", CustomDesignHandler.getAmountinCommaDecimal(totalExcise, Constants.AMOUNT_DIGIT_AFTER_DECIMAL,Constants.indian_country_id));
                    dataArr.put(grJsonobj);
//                }
            }

            int totalCount = dataArr.length();
            if ((!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) && !isExport) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            finaljobj.put("data", dataArr);
            finaljobj.put("totalcount", totalCount);
            finaljobj.put("count", totalCount);
            finaljobj.put("success", true);

        } catch (SessionExpiredException | ServiceException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return finaljobj;
    }
    
    public ModelAndView exportDVATAnnexure2AReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            jobj = getJSONDVATAnnexure2A(request, true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
public JSONObject getJSONDVATAnnexure2A(HttpServletRequest request, boolean isExport) {
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat gdf = authHandler.getGlobalDateFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
            SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
            HashMap requestParams = new HashMap();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String startdate = "";
            String enddate = "";
            Date firstDate=null;
            Date lastDate=null;
            int year = 0;
            boolean is2A = true;
            String yearMonthOrQuater = "";
            requestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                requestParams.put("startdate", request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("year"))) {
                year = Integer.parseInt(request.getParameter("year"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("is2A"))) {
                is2A = Boolean.parseBoolean(request.getParameter("is2A"));
            }
//            if (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) {
//                requestParams.put("isFixedAsset", Boolean.parseBoolean(request.getParameter("isFixedAsset")));
//            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("frequency"))) {  // get start date and end date

                if (request.getParameter("frequency").equals("1")) { //Quaterly
                    int firstMonth = 0;
                    int lastMonth = 11;
                    String qq = "";
                    if (request.getParameter("period").equals("quater4")) { //1st January 2017 to 31st Mar 2017 (Quarter IV)
                        firstMonth = 0;
                        lastMonth = 2;
                        qq = "44";
                        year+=1;
                    } else if (request.getParameter("period").equals("quater1")) {  //1st April 2016 to 30th June 2016 (Quarter I)
                        firstMonth = 3;
                        lastMonth = 5;
                        qq = "41";
                    } else if (request.getParameter("period").equals("quater2")) {  //1st July 2016 to 30th September 2016 (Quarter II)
                        firstMonth = 6;
                        lastMonth = 8;
                        qq = "42";
                    } else if (request.getParameter("period").equals("quater3")) { //1st October 2016 to 31st December 2016 (Quarter III)
                        firstMonth = 9;
                        lastMonth = 11;
                        qq = "43";
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, firstMonth);
                    calendar.set(Calendar.DATE, 1);
                    firstDate = calendar.getTime();
                    startdate = gdf.format(firstDate);

                    calendar.set(Calendar.MONTH, lastMonth);
                    int TotalDaysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    calendar.set(Calendar.DATE, TotalDaysOfMonth);
                    lastDate = calendar.getTime();
                    enddate = gdf.format(lastDate);

                    yearMonthOrQuater = sdfYear.format(firstDate) + qq;

                } else { // Monthly

                    Calendar calendar = Calendar.getInstance();
                    int month = Integer.parseInt(request.getParameter("period"));
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DATE, 1);
                    firstDate = calendar.getTime();
                    startdate = gdf.format(firstDate);

                    int TotalDaysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    calendar.set(Calendar.DATE, TotalDaysOfMonth);
                    lastDate = calendar.getTime();
                    enddate = gdf.format(lastDate);

                    yearMonthOrQuater = sdfYear.format(firstDate) + sdfMonth.format(firstDate);
                }
                if (!StringUtil.isNullOrEmpty(startdate) && !StringUtil.isNullOrEmpty(enddate)) {
                    requestParams.put("startdate", startdate);
                    requestParams.put("enddate", enddate);
                }
            }

            requestParams.put("sort", "date");
            requestParams.put("dir", "DESC");
            requestParams.put(Constants.df, df);
            requestParams.put("dateformat", df);
            if (is2A) {
                int srno = 1;
                int jsonIndex = 0;
                KwlReturnObject kwl = accGoodsReceiptDAOObj.getGoodsReceiptsMerged(requestParams);
                List piList = kwl.getEntityList();
                Iterator piitr = piList.iterator();
                double totalAssessableValue = 0.0;
                double totalVATValue = 0.0;
                double totalAmountWithTax = 0.0;
                double totalimpfrmoutind = 0, totalhighseaspur = 0, totalreceivbckf = 0, totalamountunregisterdealer = 0, totalotherdealerreceivedf = 0;
                double totalineterstatepurwithout = 0, totalinwardstockbranch = 0, totalinwardstockconsignment = 0, totalcgpurc = 0, totalcpurc = 0, totalineterstatepurh = 0;
                while (piitr.hasNext()) {
                    String TIN = "";
                    String vendorName = "";
                    Object[] oj = (Object[]) piitr.next();
                    String grid = oj[0].toString();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grid);
                    GoodsReceipt goodsReceipt = (GoodsReceipt) objItr.getEntityList().get(0);

                    double externalcurrencyrate = 1.0;
                    if (goodsReceipt.getJournalEntry() != null) {
                        externalcurrencyrate = goodsReceipt.getJournalEntry().getExternalCurrencyRate();
                    }

                    Set<GoodsReceiptDetail> rows = goodsReceipt.getRows();

                    if (goodsReceipt.getVendor() != null) {
                        vendorName = goodsReceipt.getVendor().getName() != null ? goodsReceipt.getVendor().getName() : "";
                        TIN = goodsReceipt.getVendor().isInterstateparty() ? (goodsReceipt.getVendor().getCSTTINnumber() != null ? goodsReceipt.getVendor().getCSTTINnumber() : "") : (goodsReceipt.getVendor().getVATTINnumber() != null ? goodsReceipt.getVendor().getVATTINnumber() : "");
                    }
                    String prevquater = "No";
                    String pi = goodsReceipt.getGoodsReceiptNumber();
                    String piId = goodsReceipt.getID();
                    KwlReturnObject gobj = accGoodsReceiptDAOObj.getGRfromPI(piId);
                    List<String> grlist = gobj.getEntityList();
                    for (String grId : grlist) {
                        KwlReturnObject grItr = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), grId);
                        GoodsReceiptOrder goodsReceiptOrder = (GoodsReceiptOrder) grItr.getEntityList().get(0);
                        if (goodsReceiptOrder != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(goodsReceiptOrder.getOrderDate());
                            Date grDate = cal.getTime();
                            if (grDate.compareTo(firstDate) < 0) {
                                prevquater = "Yes";
                            }
                        }
                    }
                    JournalEntry je = null;
                    if (goodsReceipt.isNormalInvoice()) {
                        je = goodsReceipt.getJournalEntry();
                    }
                    String typeofpur = "";
                    double VATamount = 0;
                    double amountWithTax = 0;
                    double assessableValue = 0;
                    double vatPer = 0;
                    double vatPer1 = 0;
                    double impfrmoutind = 0, highseaspur = 0, receivbckf = 0, amountunregisterdealer = 0, otherdealerreceivedf = 0;
                    double cgpurc = 0, cpurc = 0, ineterstatepurh = 0, ineterstatepurwithout = 0, inwardstockbranch = 0, inwardstockconsignment = 0;
                    for (GoodsReceiptDetail row : rows) {
                        if (row.getInventory() != null && row.getInventory().getProduct() != null) {
                            jsonIndex++;
                            VATamount = 0;
                            assessableValue = 0;
                            String typeOfPurchase = "";
                            typeofpur = "";
                            impfrmoutind = 0;
                            highseaspur = 0;
                            receivbckf = 0;
                            amountunregisterdealer = 0;
                            otherdealerreceivedf = 0;
                            cgpurc = 0;
                            cpurc = 0;
                            ineterstatepurh = 0;
                            ineterstatepurwithout = 0;
                            inwardstockbranch = 0;
                            inwardstockconsignment = 0;
                            vatPer=0;
                            vatPer1=0;
                            HashMap<String, Object> GoodsReceiptDetailParams = new HashMap<String, Object>();
                            GoodsReceiptDetailParams.put("GoodsReceiptDetailid", row.getID());
                            if (row.getPurchaseJED() != null && !StringUtil.isNullOrEmpty(row.getPurchaseJED().getID())) {
                                if (row.getPurchaseJED().getAccount() != null && !StringUtil.isNullOrEmpty(row.getPurchaseJED().getAccount().getPurchaseType())) {
                                    typeOfPurchase = row.getPurchaseJED().getAccount().getPurchaseType();
                                }
                            }
                            KwlReturnObject grdTermMapresult = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(GoodsReceiptDetailParams);
                            List<ReceiptDetailTermsMap> ReceiptDetailTermsMapList = grdTermMapresult.getEntityList();
                            for (ReceiptDetailTermsMap invoicedetailTermMap : ReceiptDetailTermsMapList) {
                                if (invoicedetailTermMap.getTerm() != null) {
                                    invoicedetailTermMap.getPurchaseValueOrSaleValue();
                                    invoicedetailTermMap.getGoodsreceiptdetail();
                                    if (invoicedetailTermMap.getTerm() != null && (invoicedetailTermMap.getTerm().getTermType() == IndiaComplianceConstants.LINELEVELTERMTYPE_CST || invoicedetailTermMap.getTerm().getTermType() == IndiaComplianceConstants.LINELEVELTERMTYPE_VAT)) {  // 1 VAT   3 CST
                                        switch (typeOfPurchase) {
                                            case "Blank1":
                                                impfrmoutind += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = "";
                                                break;
                                            case "Blank2":
                                                highseaspur += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = "";
                                                break;
                                            case "Blank3":
                                                receivbckf += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = "";
                                                break;
                                            case "PUR":
                                                amountunregisterdealer += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = typeOfPurchase;
                                                break;
                                            case "PCD":
                                                amountunregisterdealer += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = typeOfPurchase;
                                                break;
                                            case "PCG":
                                                amountunregisterdealer += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = typeOfPurchase;
                                                break;
                                            case "PRI":
                                                amountunregisterdealer += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = typeOfPurchase;
                                                break;
                                            case "PTF":
                                                amountunregisterdealer += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = typeOfPurchase;
                                                break;
                                            case "PLSWC":
                                                amountunregisterdealer += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = typeOfPurchase;
                                                break;
                                            case "PATI":
                                                amountunregisterdealer += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = typeOfPurchase;
                                                break;
                                            case "PDDH":
                                                amountunregisterdealer += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = typeOfPurchase;
                                                break;
                                            case "PCGM":
                                                amountunregisterdealer += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = typeOfPurchase;
                                                break;
                                            case "Blank4":
                                                otherdealerreceivedf += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = "";
                                                break;
                                            case "Blank5":
                                                cgpurc += invoicedetailTermMap.getAssessablevalue();
                                                typeofpur = "";
                                                break;
                                            case "Blank6":
                                                cpurc += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = "";
                                                break;
                                            case "Blank7":
                                                ineterstatepurh += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = "";
                                                break;
                                            case "Blank8":
                                                ineterstatepurwithout += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = "";
                                                break;
                                            case "Blank9":
                                                inwardstockbranch += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = "";
                                                break;
                                            case "Blank10":
                                                inwardstockconsignment += invoicedetailTermMap.getAssessablevalue();
//                                                vatPer = invoicedetailTermMap.getPercentage();
                                                typeofpur = "";
                                                break;
                                            case "PPD":
                                                typeofpur = "PPD";
                                                break;
                                            case "GD":
                                                typeofpur = "GD";
                                                assessableValue += invoicedetailTermMap.getAssessablevalue();
                                                VATamount += invoicedetailTermMap.getTermamount();
                                                vatPer = invoicedetailTermMap.getPercentage();
                                                vatPer1=0;
                                                break;
                                            case "WC":
                                                typeofpur = "WC";
                                                assessableValue += invoicedetailTermMap.getAssessablevalue();
                                                VATamount += invoicedetailTermMap.getTermamount();
                                                vatPer = invoicedetailTermMap.getPercentage();
                                                vatPer1=0;
                                                break;
                                            default:
                                                assessableValue += invoicedetailTermMap.getAssessablevalue();
                                                VATamount += invoicedetailTermMap.getTermamount();
                                                vatPer = invoicedetailTermMap.getPercentage();
                                                vatPer1 = vatPer;
                                                break;

                                        }
                                    }
                                }
                            }
                        }
                        assessableValue = assessableValue / externalcurrencyrate;
                        VATamount = VATamount / externalcurrencyrate;
                        amountWithTax = assessableValue + VATamount;

                        totalimpfrmoutind += (impfrmoutind / externalcurrencyrate);
                        totalhighseaspur += (highseaspur / externalcurrencyrate);
                        totalreceivbckf += (receivbckf / externalcurrencyrate);
                        totalamountunregisterdealer += (amountunregisterdealer / externalcurrencyrate);
                        totalotherdealerreceivedf += (otherdealerreceivedf / externalcurrencyrate);
                        totalcgpurc += (cgpurc / externalcurrencyrate);
                        totalcpurc += (cpurc / externalcurrencyrate);
                        totalineterstatepurh += (ineterstatepurh / externalcurrencyrate);
                        totalineterstatepurwithout += (ineterstatepurwithout / externalcurrencyrate);
                        totalinwardstockbranch += (inwardstockbranch / externalcurrencyrate);
                        totalinwardstockconsignment += (inwardstockconsignment / externalcurrencyrate);

                        totalAssessableValue += assessableValue;
                        totalVATValue += VATamount;
                        totalAmountWithTax += amountWithTax;
                        JSONObject grJsonobj = new JSONObject();
                        grJsonobj.put("srno", srno);
                        grJsonobj.put("yearmmqq", yearMonthOrQuater);
                        grJsonobj.put("nameofsupplier", vendorName);
                        grJsonobj.put("tin", TIN);
                        grJsonobj.put("vatper", vatPer == 0.0 ? "" : authHandler.formattingDecimalForAmount(vatPer, companyid));
                        grJsonobj.put("vatdper", vatPer1 == 0.0 ? "" : authHandler.formattingDecimalForAmount(vatPer1, companyid));
                        grJsonobj.put("impfrmoutind", impfrmoutind == 0.0 ? "" : authHandler.formattingDecimalForAmount(impfrmoutind, companyid));
                        grJsonobj.put("highseaspur", highseaspur == 0.0 ? "" : authHandler.formattingDecimalForAmount(highseaspur, companyid));
                        grJsonobj.put("receivbckf", receivbckf == 0.0 ? "" : authHandler.formattingDecimalForAmount(receivbckf, companyid));
                        grJsonobj.put("amountunregisterdealer", amountunregisterdealer == 0.0 ? "" : authHandler.formattingDecimalForAmount(amountunregisterdealer, companyid));
                        grJsonobj.put("otherdealerreceivedf", otherdealerreceivedf == 0.0 ? "" : authHandler.formattingDecimalForAmount(otherdealerreceivedf, companyid));
                        grJsonobj.put("cgpurc", cgpurc == 0.0 ? "" : authHandler.formattingDecimalForAmount(cgpurc, companyid));
                        grJsonobj.put("cpurc", cpurc == 0.0 ? "" : authHandler.formattingDecimalForAmount(cpurc, companyid));
                        grJsonobj.put("ineterstatepurh", ineterstatepurh == 0.0 ? "" : authHandler.formattingDecimalForAmount(ineterstatepurh, companyid));
                        grJsonobj.put("ineterstatepurwithout", ineterstatepurwithout == 0.0 ? "" : authHandler.formattingDecimalForAmount(ineterstatepurwithout, companyid));
                        grJsonobj.put("inwardstockbranch", inwardstockbranch == 0.0 ? "" : authHandler.formattingDecimalForAmount(inwardstockbranch, companyid));
                        grJsonobj.put("inwardstockconsignment", inwardstockconsignment == 0.0 ? "" : authHandler.formattingDecimalForAmount(inwardstockconsignment, companyid));
                        grJsonobj.put("lpurcgramt", "");
                        grJsonobj.put("lpurcgpuramt", "");
                        grJsonobj.put("lpurcgiptaxpaid", "");
                        grJsonobj.put("lpurcgtotalpurintax", "");
                        grJsonobj.put("typeofpur", typeofpur);
                        grJsonobj.put("amount", assessableValue == 0.0 ? "" : authHandler.formattingDecimalForAmount(assessableValue, companyid));
                        grJsonobj.put("vatamount", VATamount == 0.0 ? "" : authHandler.formattingDecimalForAmount(VATamount, companyid));
                        grJsonobj.put("amountwithtax", amountWithTax == 0.0 ? "" : authHandler.formattingDecimalForAmount(amountWithTax, companyid));
                        grJsonobj.put("prevquater", prevquater);
                        dataArr.put(jsonIndex, grJsonobj);
                        srno++;
                    }
                }
                
                JSONObject totalJSON = new JSONObject();
                totalJSON.put("srno", 0);
                totalJSON.put("yearmmqq", 0);
                totalJSON.put("tin", "T");
                totalJSON.put("nameofsupplier", "T");
                totalJSON.put("impfrmoutind",authHandler.formattingDecimalForAmount(totalimpfrmoutind, companyid));
                totalJSON.put("highseaspur",authHandler.formattingDecimalForAmount(totalhighseaspur, companyid));
                totalJSON.put("receivbckf",authHandler.formattingDecimalForAmount(totalreceivbckf, companyid));
                totalJSON.put("amountunregisterdealer",authHandler.formattingDecimalForAmount(totalamountunregisterdealer, companyid));//authHandler.formattingdecimal(amountUnregisterdealer, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
                totalJSON.put("otherdealerreceivedf",authHandler.formattingDecimalForAmount(totalotherdealerreceivedf, companyid));
                totalJSON.put("cgpurc",authHandler.formattingDecimalForAmount(totalcgpurc, companyid));
                totalJSON.put("cpurc",authHandler.formattingDecimalForAmount(totalcpurc, companyid));
                totalJSON.put("ineterstatepurh",authHandler.formattingDecimalForAmount(totalineterstatepurh, companyid));
                totalJSON.put("ineterstatepurwithout",authHandler.formattingDecimalForAmount(totalineterstatepurwithout, companyid));
                totalJSON.put("inwardstockbranch",authHandler.formattingDecimalForAmount(totalinwardstockbranch, companyid));
                totalJSON.put("inwardstockconsignment",authHandler.formattingDecimalForAmount(totalinwardstockconsignment, companyid));
                totalJSON.put("lpurcgramt",authHandler.formattingDecimalForAmount(0, companyid));
                totalJSON.put("lpurcgpuramt",authHandler.formattingDecimalForAmount(0, companyid));
                totalJSON.put("lpurcgiptaxpaid",authHandler.formattingDecimalForAmount(0, companyid));
                totalJSON.put("lpurcgtotalpurintax",authHandler.formattingDecimalForAmount(0, companyid));
                totalJSON.put("typeofpur","T");
                totalJSON.put("vatper",authHandler.formattingDecimalForAmount(0, companyid));
                totalJSON.put("amount", authHandler.formattingDecimalForAmount(totalAssessableValue, companyid));
                totalJSON.put("vatamount", authHandler.formattingDecimalForAmount(totalVATValue, companyid));
                totalJSON.put("amountwithtax", authHandler.formattingDecimalForAmount(totalAmountWithTax, companyid));
                totalJSON.put("vatdper", authHandler.formattingDecimalForAmount(0, companyid));
                totalJSON.put("prevquater","");
                
                dataArr.put(0,totalJSON);
                
            } else {  //Annexture 2B
                int srno = 1;
                int jsonIndex = 0;
                KwlReturnObject kwl = accInvoiceDAOobj.getInvoicesMerged(requestParams);
                List siList = kwl.getEntityList();
                Iterator siitr = siList.iterator();
                double totalAssessableValue=0;
                double totalVATamount=0;
                double totalAmountWithTax=0;
                while (siitr.hasNext()) {
                    String TIN = "";
                    String customerName = "";
                    String dealertype = "";
                    boolean isInterStateParty=false;
                    Object[] oj = (Object[]) siitr.next();
                    String grid = oj[0].toString();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), grid);
                    Invoice invoice = (Invoice) objItr.getEntityList().get(0);

                    double externalcurrencyrate = 1.0;
                    if (invoice.getJournalEntry() != null) {
                        externalcurrencyrate = invoice.getJournalEntry().getExternalCurrencyRate();
                    }

                    Set<InvoiceDetail> rows = invoice.getRows();

                    if (invoice.getCustomer() != null) {
                        customerName = invoice.getCustomer().getName() != null ? invoice.getCustomer().getName() : "";
                        dealertype = invoice.getCustomer().getDealertype() != null ? invoice.getCustomer().getDealertype() : "";
                        TIN = invoice.getCustomer().isInterstateparty() ? (invoice.getCustomer().getCSTTINnumber() != null ? invoice.getCustomer().getCSTTINnumber() : "") : (invoice.getCustomer().getVATTINnumber() != null ? invoice.getCustomer().getVATTINnumber() : "");
                        isInterStateParty=invoice.getCustomer().isInterstateparty();
                    }
                    String pi = invoice.getInvoiceNumber();
                    JournalEntry je = null;
                    if (invoice.isNormalInvoice()) {
                        je = invoice.getJournalEntry();
                    }
                    double VATamount = 0;
                    double rate = 0;
                    double assessableValue = 0;
                    double amountWithTax = 0;
                    double vatPer = 0;
                    for (InvoiceDetail row : rows) {
                        if (row.getInventory() != null && row.getInventory().getProduct() != null) {
                            jsonIndex++;
                            VATamount = 0;
                            assessableValue = 0;
                            rate = row.getRate() / externalcurrencyrate;

                            HashMap<String, Object> invoiceDetailParams = new HashMap();
                            invoiceDetailParams.put("InvoiceDetailid", row.getID());
                            KwlReturnObject invoiceMapresult = accInvoiceDAOobj.getInvoicedetailTermMap(invoiceDetailParams);
                            List<InvoiceDetailTermsMap> PurchaseReturnDetailTermsMapList = invoiceMapresult.getEntityList();
                            for (InvoiceDetailTermsMap invDetailTermMap : PurchaseReturnDetailTermsMapList) {
                                if (invDetailTermMap.getTerm() != null) {
                                    invDetailTermMap.getPurchaseValueOrSaleValue();
                                    invDetailTermMap.getInvoicedetail();
                                    if (invDetailTermMap.getTerm() != null && invDetailTermMap.getTerm().getTermType() == 1) {
                                        assessableValue += invDetailTermMap.getAssessablevalue();
                                        VATamount += invDetailTermMap.getTermamount();
                                        vatPer = invDetailTermMap.getPercentage();
                                    }
                                }
                            }
                        }
                        assessableValue = assessableValue / externalcurrencyrate;
                        totalAssessableValue+=assessableValue;
                        VATamount = VATamount / externalcurrencyrate;
                        totalVATamount+= VATamount;
                        amountWithTax = assessableValue + VATamount;
                        totalAmountWithTax += amountWithTax;
                        
                        JSONObject grJsonobj = new JSONObject();
                        grJsonobj.put("srno", srno);
                        grJsonobj.put("yearmmqq", yearMonthOrQuater);
                        grJsonobj.put("tin", TIN);
                        grJsonobj.put("nameofbuyer", customerName);
                        grJsonobj.put("interstatebranch", "");
                        grJsonobj.put("interstateconsignment", "");
                        grJsonobj.put("expoutind", "");
                        grJsonobj.put("highseasales", "");
                        grJsonobj.put("issgoodtrantype", "");
                        grJsonobj.put("chie1e2jnone", "");
                        grJsonobj.put("ratoftax", "");
                        grJsonobj.put("salespricexcst", "");
                        grJsonobj.put("cst", "");
                        grJsonobj.put("total", "");
                        grJsonobj.put("lsaletypeofsale", "");
                        grJsonobj.put("lratetax",vatPer);
                        grJsonobj.put("salespricexvat", authHandler.formattingDecimalForAmount(assessableValue, companyid));
                        grJsonobj.put("optax", authHandler.formattingDecimalForAmount(VATamount, companyid));
                        grJsonobj.put("totalinvat", authHandler.formattingDecimalForAmount(amountWithTax, companyid));
                        grJsonobj.put("rattaxdvat", vatPer);
                        grJsonobj.put("saleomclvl", "");
                        grJsonobj.put("chargesinwork", "");
                        grJsonobj.put("chargesinservicework", "");
                        grJsonobj.put("saleshdelhi", "");
                        grJsonobj.put("chargeother","");
                        dataArr.put(jsonIndex,grJsonobj);
                        srno++;
                    }
                }
                JSONObject inJsonobj = new JSONObject();
                        inJsonobj.put("srno", 0);
                        inJsonobj.put("yearmmqq", 0);
                        inJsonobj.put("tin", "T");
                        inJsonobj.put("nameofbuyer", "T");
                        inJsonobj.put("interstatebranch", authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("interstateconsignment", authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("expoutind",authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("highseasales",authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("issgoodtrantype","T");
                        inJsonobj.put("chie1e2jnone", "T");
                        inJsonobj.put("ratoftax", authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("salespricexcst", authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("cst", authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("total", authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("lsaletypeofsale", "T");
                        inJsonobj.put("lratetax",authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("salespricexvat", authHandler.formattingDecimalForAmount(totalAssessableValue, companyid));
                        inJsonobj.put("optax", authHandler.formattingDecimalForAmount(totalVATamount, companyid));
                        inJsonobj.put("totalinvat", authHandler.formattingDecimalForAmount(totalAmountWithTax, companyid));
                        inJsonobj.put("rattaxdvat", authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("saleomclvl", authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("chargesinwork", authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("chargesinservicework", authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("saleshdelhi", authHandler.formattingDecimalForAmount(0, companyid));
                        inJsonobj.put("chargeother",authHandler.formattingDecimalForAmount(0, companyid));
                        dataArr.put(0,inJsonobj);
                
            }

            int totalCount = dataArr.length();
            if ((!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) && !isExport) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            finaljobj.put("data", dataArr);
            finaljobj.put("totalcount", totalCount);
            finaljobj.put("success", true);

        } catch (SessionExpiredException | ServiceException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return finaljobj;
    }

    public ModelAndView getAnnexure10Report(HttpServletRequest request, HttpServletResponse response) {
        JSONObject finaljobj = new JSONObject();
        try {
            finaljobj = getJSONAnnexure10Report(request, false);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());
    }

    public ModelAndView exportAnnexure10Report(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            jobj = getJSONAnnexure10Report(request, true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public JSONObject getJSONAnnexure10Report(HttpServletRequest request, boolean isExport) {
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "", exciseUnit = "";
        boolean issuccess = false;
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        String stockItemType = "", templateid = "";

        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            HashMap requestParams = new HashMap();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                requestParams.put("startdate", request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("stockitemtype"))) {
                stockItemType = request.getParameter("stockitemtype");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isExciseInvoice"))) {
                requestParams.put("isExciseInvoice", request.getParameter("isExciseInvoice"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("includeAllRec"))) {
                requestParams.put("includeAllRec", request.getParameter("includeAllRec"));
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("exciseunit"))) {
                exciseUnit = request.getParameter("exciseunit");
                List modt = new ArrayList();
                requestParams.put("companyunitid", exciseUnit);
                KwlReturnObject kwltemp = accountingHandlerDAOobj.getModuleTemplates(requestParams);
                modt = kwltemp.getEntityList();
                if (modt.size() > 0) {
                    ModuleTemplate moduleTemp = (ModuleTemplate) modt.get(0);
                    templateid = moduleTemp.getTemplateId();
                    requestParams.put("moduletemplateid", templateid);

                }
            }
            requestParams.put("isPurchaseReturnCreditNote", true);
            requestParams.put(Constants.df, df);
            requestParams.put("dateformat", df);
            requestParams.put("sort", "date");
            requestParams.put("dir", "ASC");
            
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("companyid", companyid);
            params.put("gcurrencyid", currency.getCurrencyID());

            KwlReturnObject kwl = accGoodsReceiptDAOObj.getGoodsReceiptsMerged(requestParams);
            List grList = kwl.getEntityList();

            Iterator gritr = grList.iterator();

            while (gritr.hasNext()) {
                Object[] oj = (Object[]) gritr.next();
                String grid = oj[0].toString();
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grid);
                GoodsReceipt goodsReceipt = (GoodsReceipt) objItr.getEntityList().get(0);

                String currencyid = (goodsReceipt.getCurrency() == null ? currency.getCurrencyID() : goodsReceipt.getCurrency().getCurrencyID());

                boolean isstockItemPresent = false;
                if (StringUtil.isNullOrEmpty(stockItemType)) {
                    isstockItemPresent = true;
                }
                double invAmount = goodsReceipt.getInvoiceAmountInBase();
                Set<GoodsReceiptDetail> rows = goodsReceipt.getRows();
                double TotalExcise = 0.0;
                double additionalTotalExcise = 0.0;
                double quantity = 0.0;
                double TotalAssasableValue = 0.0;
                for (GoodsReceiptDetail row : rows) {
                    if (!StringUtil.isNullOrEmpty(stockItemType)) {
                        if (stockItemType.equals(row.getInventory().getProduct().getNatureofStockItem())) {
                            isstockItemPresent = true;
                            HashMap<String, Object> grDetailParams = new HashMap();
                            grDetailParams.put("GoodsReceiptDetailid", row.getID());
                            KwlReturnObject grMapresult = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(grDetailParams);
                            List<ReceiptDetailTermsMap> grDetailTermsMapList = grMapresult.getEntityList();
                            for (ReceiptDetailTermsMap grDetailTermMap : grDetailTermsMapList) {
                                if (grDetailTermMap.getTerm() != null && grDetailTermMap.getTerm().getTermType() == 2) {
//                                    Date date= goodsReceipt.getJournalEntry() !=null?goodsReceipt.getJournalEntry().getEntryDate():null;
//                                    if(goodsReceipt.isIsOpeningBalenceInvoice() || date==null){
//                                        date= goodsReceipt.getCreationDate();
//                                    }
                                    Date date= goodsReceipt.getCreationDate();
                                    KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, date, 0);
                                    Double Excise = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                    if (grDetailTermMap.getTerm().isIsAdditionalTax()) {  //Additional Excise Duty
                                        additionalTotalExcise += Excise;
                                    } else {
//                                        Double assesable_value = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                        Double assesable_value = grDetailTermMap.getAssessablevalue();
                                        TotalAssasableValue += assesable_value;
                                        TotalExcise += Excise;
                                    }

                                    kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, date, 0);
                                    }
                                }
                            quantity += row.getInventory().getQuantity();
                        }
                    } else {
                        HashMap<String, Object> grDetailParams = new HashMap();
                        grDetailParams.put("GoodsReceiptDetailid", row.getID());
                        KwlReturnObject grMapresult = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(grDetailParams);
                        List<ReceiptDetailTermsMap> grDetailTermsMapList = grMapresult.getEntityList();
                        for (ReceiptDetailTermsMap grDetailTermMap : grDetailTermsMapList) {
                            if (grDetailTermMap.getTerm() != null && grDetailTermMap.getTerm().getTermType() == 2) {
                                if (grDetailTermMap.getTerm().isIsAdditionalTax()) {   //Additional Excise Duty
                                    additionalTotalExcise += grDetailTermMap.getTermamount();
                                } else {
                                    TotalExcise += grDetailTermMap.getTermamount();
                                    TotalAssasableValue += grDetailTermMap.getAssessablevalue();
                                }
                                }
                            }
                        quantity += row.getInventory().getQuantity();
                    }
                }
                if (isstockItemPresent) {
                    String suppliernameManufacturertype = goodsReceipt.getVendor().getName() != null ? goodsReceipt.getVendor().getName() : "";
                    if (goodsReceipt.getManufacturerType() != null) {
                        if (goodsReceipt.getManufacturerType().equals("1")) {
                            suppliernameManufacturertype += " (Regular)";
                        } else if (goodsReceipt.getManufacturerType().equals("2")) {
                            suppliernameManufacturertype += " (Small Scale Industries(SSI))";
                        }
                    }
                    String documenttype="Excise Purchase";
//                    String date = (goodsReceipt.getJournalEntry() != null) ? userdf.format(goodsReceipt.getJournalEntry().getEntryDate()) : "";
                    String date = (goodsReceipt.getCreationDate() != null) ? userdf.format(goodsReceipt.getCreationDate()) : "";
                    if(goodsReceipt.isIsOpeningBalenceInvoice()){
                        date= (goodsReceipt.getCreationDate() != null) ? userdf.format(goodsReceipt.getCreationDate()) : "";
                        documenttype="Excise Purchase Opening Balance";
                    }
                    
                    JSONObject grJsonobj = new JSONObject();
                    grJsonobj.put("dateofentry", date);
                    grJsonobj.put("documentnumber", goodsReceipt.getGoodsReceiptNumber());
                    grJsonobj.put("documentnumberdate", goodsReceipt.getGoodsReceiptNumber() + " " + date);
                    grJsonobj.put("jenumber", goodsReceipt.getJournalEntry() != null ? goodsReceipt.getJournalEntry().getEntryNumber() : "");
                    grJsonobj.put("documenttype", documenttype);
                    grJsonobj.put("suppliername", suppliernameManufacturertype);
                    grJsonobj.put("manufacturertype", goodsReceipt.getVendor().getManufacturerType() != null ? goodsReceipt.getVendor().getManufacturerType() : "");
                    grJsonobj.put("eccno", goodsReceipt.getVendor().getECCnumber() != null ? goodsReceipt.getVendor().getECCnumber() : "");
                    grJsonobj.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    grJsonobj.put("assessablevalue", authHandler.formattingDecimalForAmount(TotalAssasableValue, companyid));
                    grJsonobj.put("exciseduty", authHandler.formattingDecimalForAmount(TotalExcise, companyid));
                    grJsonobj.put("additionalexciseduty", authHandler.formattingDecimalForAmount(additionalTotalExcise, companyid));
                    grJsonobj.put("quantity", quantity);
                    dataArr.put(grJsonobj);
                }
            }
            kwl = accGoodsReceiptDAOObj.getPurchaseReturn(requestParams);
            List prList = kwl.getEntityList();
            Iterator itr = prList.iterator();
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String id = oj[0].toString();
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseReturn.class.getName(), id);
                PurchaseReturn pr = (PurchaseReturn) objItr.getEntityList().get(0);
                Set<PurchaseReturnDetail> purchaseReturnRows = pr.getRows();
                String currencyid = (pr.getCurrency() == null ? currency.getCurrencyID() : pr.getCurrency().getCurrencyID());
                double TotalExcise = 0.0;
                double TotalExcise1 = 0.0;
                double additionalTotalExcise = 0.0;
                double additionalTotalExcise1 = 0.0;
                boolean addRecord = true;
                boolean stockItemTypeFlag = false;
                double quantity = 0.0;
                double quantity1 = 0.0;
                double TotalAssasableValue = 0.0;
                double TotalAssasableValue1 = 0.0;
                for (PurchaseReturnDetail row : purchaseReturnRows) {
                    if (row.getVidetails() != null) {
                        if (row.getVidetails().getGoodsReceipt() != null && row.getVidetails().getGoodsReceipt().isIsExciseInvoice()) {
                            if (!StringUtil.isNullOrEmpty(stockItemType) && !stockItemType.equals(row.getInventory().getProduct().getNatureofStockItem())) {
                                stockItemTypeFlag = true;
                                HashMap<String, Object> PurchaseReturnDetailParams = new HashMap<String, Object>();
                                PurchaseReturnDetailParams.put("PurchaseReturnDetailid", row.getID());
                                KwlReturnObject prdTermMapresult = accGoodsReceiptDAOObj.getPurchaseReturnDetailTermMap(PurchaseReturnDetailParams);
                                List<PurchaseReturnDetailsTermMap> PurchaseReturnDetailTermsMapList = prdTermMapresult.getEntityList();
                                for (PurchaseReturnDetailsTermMap purchaseReturnDetailTermMap : PurchaseReturnDetailTermsMapList) {
                                    if (purchaseReturnDetailTermMap.getTerm() != null && purchaseReturnDetailTermMap.getTerm().getTermType() == 2) {

                                        KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, purchaseReturnDetailTermMap.getTermamount(), currencyid, pr.getOrderDate(), 0);
                                        Double Excise = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                        if (purchaseReturnDetailTermMap.getTerm().isIsAdditionalTax()) { //Additional Excise Duty
                                            additionalTotalExcise1 += Excise;
                                        } else {
                                            TotalExcise1 += Excise;
//                                            Double assesable_value = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                            Double assesable_value = purchaseReturnDetailTermMap.getAssessablevalue();
                                            TotalAssasableValue1 += assesable_value;
                                        }

                                        kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, purchaseReturnDetailTermMap.getAssessablevalue(), currencyid, pr.getOrderDate(), 0);
                                    }
                                }
                                quantity1 += row.getInventory().getQuantity();
                            }
//                            if (!StringUtil.isNullOrEmpty(templateid) && ((row.getVidetails().getGoodsReceipt().getModuletemplateid() != null && !templateid.equals(row.getVidetails().getGoodsReceipt().getModuletemplateid().getTemplateId())) || row.getVidetails().getGoodsReceipt().getModuletemplateid() == null)) {
//                                addRecord = false;
//                                break;
//                            }
                            if (!stockItemTypeFlag) {
                                HashMap<String, Object> PurchaseReturnDetailParams = new HashMap<String, Object>();
                                PurchaseReturnDetailParams.put("PurchaseReturnDetailid", row.getID());
                                KwlReturnObject prdTermMapresult = accGoodsReceiptDAOObj.getPurchaseReturnDetailTermMap(PurchaseReturnDetailParams);
                                List<PurchaseReturnDetailsTermMap> PurchaseReturnDetailTermsMapList = prdTermMapresult.getEntityList();
                                for (PurchaseReturnDetailsTermMap purchaseReturnDetailTermMap : PurchaseReturnDetailTermsMapList) {
                                    if (purchaseReturnDetailTermMap.getTerm() != null && purchaseReturnDetailTermMap.getTerm().getTermType() == 2) {

                                        KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, purchaseReturnDetailTermMap.getTermamount(), currencyid, pr.getOrderDate(), 0);
                                        Double Excise = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                        if (purchaseReturnDetailTermMap.getTerm().isIsAdditionalTax()) { //Additional Excise Duty
                                            additionalTotalExcise += Excise;
                                        } else {
                                            TotalExcise += Excise;
//                                            Double assesable_value = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                            Double assesable_value = purchaseReturnDetailTermMap.getAssessablevalue();
                                            TotalAssasableValue += assesable_value;
                                        }
                                        kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, purchaseReturnDetailTermMap.getAssessablevalue(), currencyid, pr.getOrderDate(), 0);
                                        }
                                        }
                                quantity += row.getInventory().getQuantity();
                            }
                        } else {
                            addRecord = false;
                            break;
                        }
                    } else {
                        addRecord = false;
                        break;
                    }
                }
                if (stockItemTypeFlag) {
                    TotalExcise = TotalExcise1;
                    quantity = quantity1;
                    TotalAssasableValue = TotalAssasableValue1;
                    additionalTotalExcise = additionalTotalExcise1;
                }
                if (addRecord || stockItemTypeFlag) {
                    String suppliernameManufacturertype = pr.getVendor().getName() != null ? pr.getVendor().getName() : "";
                    if (pr.getVendor().getManufacturerType() != null) {
                        if (pr.getVendor().getManufacturerType().equals("1")) {
                            suppliernameManufacturertype += " (Regular)";
                        } else if (pr.getVendor().getManufacturerType().equals("2")) {
                            suppliernameManufacturertype += " (Small Scale Industries(SSI))";
                        }
                    }

                    HashMap mapDN = new HashMap();
                    mapDN.put("purchaseReturnID", pr.getID());
                    mapDN.put("companyid", companyid);
                    String debitNoteNumber = "";
                    KwlReturnObject kwlDN = accGoodsReceiptDAOObj.getDebitNoteLinkedInPurchaseReturn(mapDN);
                    List list = kwlDN.getEntityList();
                    if (list != null && list.size() > 0) {
                        Iterator itrcq = list.iterator();
                        while (itrcq.hasNext()) {
                            DebitNote debitNote = (DebitNote) itrcq.next();
                            debitNoteNumber = debitNote.getDebitNoteNumber();
                        }

                    }
                    String date = (pr.getOrderDate() != null) ? userdf.format(pr.getOrderDate()) : "";
                    JSONObject grJsonobj = new JSONObject();
                    grJsonobj.put("dateofentry", date);
                    grJsonobj.put("documentnumber", pr.getPurchaseReturnNumber() != null ? pr.getPurchaseReturnNumber() : "");
                    grJsonobj.put("documentnumberdate", debitNoteNumber + " " + date);
                    grJsonobj.put("documenttype", "Debite Note");
                    grJsonobj.put("suppliername", suppliernameManufacturertype);
                    grJsonobj.put("manufacturertype", pr.getVendor().getManufacturerType() != null ? pr.getVendor().getManufacturerType() : "");
                    grJsonobj.put("eccno", pr.getVendor().getECCnumber() != null ? pr.getVendor().getECCnumber() : "");
                    grJsonobj.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    grJsonobj.put("assessablevalue", authHandler.formattingDecimalForAmount(-TotalAssasableValue, companyid));
                    grJsonobj.put("exciseduty", authHandler.formattingDecimalForAmount(-TotalExcise, companyid));
                    grJsonobj.put("additionalexciseduty", authHandler.formattingDecimalForAmount(-additionalTotalExcise, companyid));
                    grJsonobj.put("quantity", quantity);
                    dataArr.put(grJsonobj);
                }
            }

            int totalCount = dataArr.length();
            if ((!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) && !isExport) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            finaljobj.put("data", dataArr);
            finaljobj.put("totalcount", totalCount);
            finaljobj.put("count", totalCount);
            finaljobj.put("success", true);

        } catch (SessionExpiredException | ServiceException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return finaljobj;
    }
     public ModelAndView getPlaReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "",exciseUnit="";
        boolean isExciseUnit = false;
        boolean issuccess = false;
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        String templateid = "";
        double TotalBalance = 0.0;

        try {
            finaljobj = getPlaReportJson(request,false);
           
        } catch(Exception ex){
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());

    }
    public ModelAndView getCreditAvailedReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "",exciseUnit="";
        boolean isExciseUnit = false;
        boolean issuccess = false;
        String start = request.getParameter("start");

        try {
            finaljobj = getCreditAvailedReportJson(request,false);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());

    }
    public ModelAndView getCreditAvailedGoodsReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "",exciseUnit="";
        boolean isExciseUnit = false;
        boolean issuccess = false;
        String start = request.getParameter("start");

        try {
            finaljobj = getCreditAvailedJson(request,false);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());

    }
    public ModelAndView getPlaSummaryReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONObject finaljobj1 = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        double TotalBalance = 0.0;

        try {
            finaljobj = getPlaSummaryReportJson(request,false);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());

    }
    public ModelAndView exportPlaReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            jobj = getPlaReportJson(request,true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public ModelAndView exportCreditAvailedReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            jobj = getCreditAvailedReportJson(request,true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public ModelAndView exportPlaSummaryReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            jobj = getPlaSummaryReportJson(request,true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public JSONObject getPlaSummaryReportJson(HttpServletRequest request, boolean isExport){
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONObject finaljobj1 = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        double TotalBalance = 0.0;

        try {
            DateFormat df=authHandler.getDateOnlyFormat();
            DateFormat userdf=authHandler.getUserDateFormatter(request);
            HashMap requestParams = new HashMap();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                requestParams.put("startdate", request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isExciseInvoice"))){
                requestParams.put("isExciseInvoice", request.getParameter("isExciseInvoice"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("exciseunit"))){
                requestParams.put("exciseunit", request.getParameter("exciseunit"));
            }
            requestParams.put("isSalesReturnCreditNote", true);
            requestParams.put(Constants.df, df);
            requestParams.put("dateformat", df);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            
            /* --- Advance Make Payment of Excise Duty  ---*/  
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getExciseDutyAdvancePaymentaccount())) {
                requestParams.put("exciseDutyAdvancePaymentaccount", extraCompanyPreferences.getExciseDutyAdvancePaymentaccount());
            }else{
                 requestParams.put("exciseDutyAdvancePaymentaccount", "");
            }
            KwlReturnObject kwlAdvExcPay = accInvoiceDAOobj.getAdvanceMakepaymentOfExcisDuty(requestParams);
            List advExcisePayment = kwlAdvExcPay.getEntityList();
            Iterator itrMP = advExcisePayment.iterator();
            while(itrMP.hasNext()){
                PaymentDetailOtherwise pdow= (PaymentDetailOtherwise)itrMP.next();
                if(pdow.getPayment()!=null && pdow.isIsdebit()){
                    Payment paymentObj=pdow.getPayment();
                    TotalBalance+=pdow.getAmount();
//                    String date = (paymentObj.getJournalEntry() != null)?userdf.format(paymentObj.getJournalEntry().getEntryDate()):"";
                    String date = (paymentObj.getCreationDate() != null)?userdf.format(paymentObj.getCreationDate()):"";
                    JSONObject invoiceJsonobj = new JSONObject();
                    invoiceJsonobj.put("documenttype", "Advance Excise Duty (Payment)");
                    invoiceJsonobj.put("documentnumber", paymentObj.getPaymentNumber() + " / " + date);
                    invoiceJsonobj.put("hsncode", "");
                    invoiceJsonobj.put("eccno", "");
                    invoiceJsonobj.put("credit", authHandler.formattingDecimalForAmount(pdow.getAmount(), companyid));
                    invoiceJsonobj.put("debit", "");
                    invoiceJsonobj.put("balance", authHandler.formattingDecimalForAmount(TotalBalance, companyid));
                    dataArr.put(invoiceJsonobj);
                    
                }
            } 
            /* --- Advance Make Payment Tag to JE Adjustment Of Excise Duty  ---*/  
           // requestParams.put("exciseDutyAdvancePaymentaccount", extraCompanyPreferences.getExciseDutyAdvancePaymentaccount());
            kwlAdvExcPay = accInvoiceDAOobj.getAdvanceJEAdjustmentOfExcisDuty(requestParams);
            advExcisePayment = kwlAdvExcPay.getEntityList();
            itrMP = advExcisePayment.iterator();
            while(itrMP.hasNext()){
                JournalEntryDetail pdow= (JournalEntryDetail)itrMP.next();
                if(pdow.getJournalEntry()!=null && !pdow.isDebit()){
                    JournalEntry paymentObj=pdow.getJournalEntry();
                    TotalBalance-=pdow.getAmount();
                    String date = (paymentObj.getEntryDate() != null)?userdf.format(paymentObj.getEntryDate()):"";
                    JSONObject invoiceJsonobj = new JSONObject();
                    invoiceJsonobj.put("documenttype", "Excise Duty (Payment)");
                    invoiceJsonobj.put("documentnumber", paymentObj.getEntryNumber() + " / " + date);
                    invoiceJsonobj.put("hsncode", "");
                    invoiceJsonobj.put("eccno", "");
                    invoiceJsonobj.put("credit", "");
                    invoiceJsonobj.put("debit", authHandler.formattingDecimalForAmount(pdow.getAmount(), companyid));
                    invoiceJsonobj.put("balance", authHandler.formattingDecimalForAmount(TotalBalance, companyid));
                    dataArr.put(invoiceJsonobj);
                    
                }else{
                    JournalEntry paymentObj=pdow.getJournalEntry();
                    if(!StringUtil.isNullOrEmpty(paymentObj.getTransactionId())){
                        continue;
                    }
                    TotalBalance+=pdow.getAmount();
                    String date = (paymentObj.getEntryDate() != null)?userdf.format(paymentObj.getEntryDate()):"";
                    JSONObject invoiceJsonobj = new JSONObject();
                    invoiceJsonobj.put("documenttype", "Excise Duty (Payment)");
                    invoiceJsonobj.put("documentnumber", paymentObj.getEntryNumber() + " / " + date);
                    invoiceJsonobj.put("hsncode", "");
                    invoiceJsonobj.put("eccno", "");
                    invoiceJsonobj.put("credit", authHandler.formattingDecimalForAmount(pdow.getAmount(), companyid));
                    invoiceJsonobj.put("debit", "");
                    invoiceJsonobj.put("balance", authHandler.formattingDecimalForAmount(TotalBalance, companyid));
                    dataArr.put(invoiceJsonobj);
                }
            }
            
            int totalCount = dataArr.length();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit) && !isExport) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            finaljobj.put("data",dataArr);
            finaljobj.put("totalcount",totalCount);
            finaljobj.put("count",totalCount);
            finaljobj.put("success",true);
           
           
        } catch (SessionExpiredException | ServiceException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return finaljobj;
    }
    public JSONObject getCreditAvailedReportJson(HttpServletRequest request, boolean isExport){
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "", exciseUnit = "";
        boolean issuccess = false;
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        String stockItemType = "", templateid = "";

        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            HashMap requestParams = new HashMap();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                requestParams.put("startdate", request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("stockitemtype"))) {
                stockItemType = request.getParameter("stockitemtype");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isExciseInvoice"))) {
                requestParams.put("isExciseInvoice", request.getParameter("isExciseInvoice"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("includeAllRec"))) {
                requestParams.put("includeAllRec", request.getParameter("includeAllRec"));
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("exciseunit"))) {
                exciseUnit = request.getParameter("exciseunit");
                List modt = new ArrayList();
                requestParams.put("companyunitid", exciseUnit);
                KwlReturnObject kwltemp = accountingHandlerDAOobj.getModuleTemplates(requestParams);
                modt = kwltemp.getEntityList();
                if (modt.size() > 0) {
                    ModuleTemplate moduleTemp = (ModuleTemplate) modt.get(0);
                    templateid = moduleTemp.getTemplateId();
                    requestParams.put("moduletemplateid", templateid);

                }
            }
            requestParams.put("isSalesReturnCreditNote", true);
            requestParams.put("isPurchaseReturnCreditNote", true);
            requestParams.put(Constants.df, df);
            requestParams.put("dateformat", df);
            requestParams.put("sort", "date");
            requestParams.put("dir", "ASC");
            Map<String, ArrayList<Double>> creditAdj = new HashMap<String, ArrayList<Double>>();


            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("companyid", companyid);
            params.put("gcurrencyid", currency.getCurrencyID());
            
            KwlReturnObject kwl = accGoodsReceiptDAOObj.getGoodsReceiptsMerged(requestParams);
            List grList = kwl.getEntityList();

            Iterator gritr = grList.iterator();

            while (gritr.hasNext()) {
                Object[] oj = (Object[]) gritr.next();
                String grid = oj[0].toString();
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grid);
                GoodsReceipt goodsReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                boolean isstockItemPresent = false;
                if (StringUtil.isNullOrEmpty(stockItemType)) {
                    isstockItemPresent = true;
                }
                Set<GoodsReceiptDetail> rows = goodsReceipt.getRows();
                String currencyid = (goodsReceipt.getCurrency() == null ? currency.getCurrencyID() : goodsReceipt.getCurrency().getCurrencyID());
                double TotalExcise = 0.0;
                double TotalService = 0.0;
                double TotalAssasableValue = 0.0;
                for (GoodsReceiptDetail row : rows) {
                    if (!StringUtil.isNullOrEmpty(stockItemType)) {
                        if (stockItemType.equals(row.getInventory().getProduct().getNatureofStockItem())) {
                            isstockItemPresent = true;
                            HashMap<String, Object> grDetailParams = new HashMap();
                            grDetailParams.put("GoodsReceiptDetailid", row.getID());
                            KwlReturnObject grMapresult = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(grDetailParams);
                            List<ReceiptDetailTermsMap> grDetailTermsMapList = grMapresult.getEntityList();
                            for (ReceiptDetailTermsMap grDetailTermMap : grDetailTermsMapList) {
                                if (grDetailTermMap.getTerm() != null && grDetailTermMap.getTerm().getTermType() == 2) {
//                                    KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                    KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                    Double Excise = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                    TotalExcise += Excise;
//                                    kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                    kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                    Double assesable_value_to_add = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                    TotalAssasableValue += assesable_value_to_add;
                                    if(grDetailTermMap.getCreditAvailedFlag()==1){ 
                                        /* --- Credit Adjustment JE post Entry  --- */		                                    
                                        TotalExcise += Excise;
                                        if (creditAdj.containsKey(grDetailTermMap.getTaxPaymentJE().getID())) {
//                                            kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                            kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                            ArrayList<Double> tempAmt = (ArrayList<Double>) creditAdj.get(grDetailTermMap.getTaxPaymentJE().getID());
                                            Double assesable_value = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                            tempAmt.add(grDetailTermMap.getTermamount());
                                            TotalAssasableValue += assesable_value;
                                            creditAdj.put(grDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                        } else {
                                            ArrayList<Double> tempAmt = new ArrayList<Double>();
                                            tempAmt.add(grDetailTermMap.getTermamount());
                                            creditAdj.put(grDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                        }
                                    }
                                }
                                if (grDetailTermMap.getTerm() != null && (grDetailTermMap.getTerm().getTermType() == IndiaComplianceConstants.LINELEVELTERMTYPE_SERVICE_TAX 
                                        || grDetailTermMap.getTerm().getTermType() == IndiaComplianceConstants.LINELEVELTERMTYPE_KKC )){
//                                    KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                    KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                    Double Service = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                    TotalService += Service;
//                                    kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                    kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                    Double assesable_value_to_add = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                    TotalAssasableValue = assesable_value_to_add;
                                    if(grDetailTermMap.getCreditAvailedFlagServiceTax()==1){ 
                                        /* --- Credit Adjustment JE post Entry Against Service Tax  --- */		                                    
                                        TotalService += Service;
                                        if (creditAdj.containsKey(grDetailTermMap.getTaxPaymentJE().getID())) {
//                                            kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                            kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                            ArrayList<Double> tempAmt = (ArrayList<Double>) creditAdj.get(grDetailTermMap.getTaxPaymentJE().getID());
                                            Double assesable_value = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                            tempAmt.add(grDetailTermMap.getTermamount());
                                            TotalAssasableValue = assesable_value;
                                            creditAdj.put(grDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                        } else {
                                            ArrayList<Double> tempAmt = new ArrayList<Double>();
                                            tempAmt.add(grDetailTermMap.getTermamount());
                                            creditAdj.put(grDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        HashMap<String, Object> grDetailParams = new HashMap();
                        grDetailParams.put("GoodsReceiptDetailid", row.getID());
                        KwlReturnObject grMapresult = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(grDetailParams);
                        List<ReceiptDetailTermsMap> grDetailTermsMapList = grMapresult.getEntityList();
                        for (ReceiptDetailTermsMap grDetailTermMap : grDetailTermsMapList) {
                            if (grDetailTermMap.getTerm() != null && grDetailTermMap.getTerm().getTermType() == 2) {
//                                Date date = goodsReceipt.isIsOpeningBalenceInvoice()?goodsReceipt.getCreationDate():goodsReceipt.getJournalEntry().getEntryDate();
                                Date date = goodsReceipt.getCreationDate();
                                KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid,date,0);
                                Double Excise = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                TotalExcise += Excise;
                                kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, date,0);
                                Double assesable_value_to_add = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                TotalAssasableValue += assesable_value_to_add;
                                if (grDetailTermMap.getCreditAvailedFlag()==1 && grDetailTermMap.getTaxPaymentJE()!=null) {
                                    /*
                                     * --- Credit Adjustment JE post Entry ---
                                     */
                                    if (creditAdj.containsKey(grDetailTermMap.getTaxPaymentJE().getID())) {
                                        kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, date,0);
                                        ArrayList<Double> tempAmt = (ArrayList<Double>) creditAdj.get(grDetailTermMap.getTaxPaymentJE().getID());
                                        Double assesable_value = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                        tempAmt.add(grDetailTermMap.getTermamount());
                                        TotalAssasableValue += assesable_value;
                                        creditAdj.put(grDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                    } else {
                                        ArrayList<Double> tempAmt = new ArrayList<Double>();
                                        tempAmt.add(grDetailTermMap.getTermamount());
                                        creditAdj.put(grDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                    }
                                }

                            }
                            if (grDetailTermMap.getTerm() != null && (grDetailTermMap.getTerm().getTermType() == IndiaComplianceConstants.LINELEVELTERMTYPE_SERVICE_TAX 
                                        || grDetailTermMap.getTerm().getTermType() == IndiaComplianceConstants.LINELEVELTERMTYPE_KKC )){
//                                KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                Double Service = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                TotalService += Service;
//                                kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                Double assesable_value_to_add = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                TotalAssasableValue = assesable_value_to_add;
                                if (grDetailTermMap.getCreditAvailedFlagServiceTax()==1 && grDetailTermMap.getTaxPaymentJE()!=null) {
                                    /*
                                     * --- Credit Adjustment JE post Entry Against Service Tax ---
                                     */
                                    if (creditAdj.containsKey(grDetailTermMap.getTaxPaymentJE().getID())) {
//                                        kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                        kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                        ArrayList<Double> tempAmt = (ArrayList<Double>) creditAdj.get(grDetailTermMap.getTaxPaymentJE().getID());
                                        Double assesable_value = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                        tempAmt.add(grDetailTermMap.getTermamount());
                                        TotalAssasableValue = assesable_value;
                                        creditAdj.put(grDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                    } else {
                                        ArrayList<Double> tempAmt = new ArrayList<Double>();
                                        tempAmt.add(grDetailTermMap.getTermamount());
                                        creditAdj.put(grDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                    }
                                }

                            }
                        }
                    }
                }
                if (isstockItemPresent) {
                    String documenttype="Excise Purchase";
//                    String date = (goodsReceipt.getJournalEntry() != null) ? userdf.format(goodsReceipt.getJournalEntry().getEntryDate()) : "";
                    String date = (goodsReceipt.getCreationDate() != null) ? userdf.format(goodsReceipt.getCreationDate()) : "";
                    if(goodsReceipt.isIsOpeningBalenceInvoice()){
                        date= (goodsReceipt.getCreationDate() != null) ? userdf.format(goodsReceipt.getCreationDate()) : "";
                        documenttype="Excise Purchase Opening Balance";
                    }                    
                    JSONObject grJsonobj = new JSONObject();
                    grJsonobj.put("dateofentry", date);
                    grJsonobj.put("documentnumber", goodsReceipt.getGoodsReceiptNumber());
                    grJsonobj.put("documentid", goodsReceipt.getID());
                    grJsonobj.put("jenumber", goodsReceipt.getJournalEntry() != null ? goodsReceipt.getJournalEntry().getEntryNumber() : "");
                    grJsonobj.put("documenttype", documenttype);
                    grJsonobj.put("suppliername", goodsReceipt.getVendor().getName());
                    grJsonobj.put("eccno", goodsReceipt.getVendor().getECCnumber());
                    grJsonobj.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    grJsonobj.put("assessablevalue", authHandler.formattingDecimalForAmount(TotalAssasableValue, companyid));
                    grJsonobj.put("exciseduty", authHandler.formattingDecimalForAmount(TotalExcise, companyid));
                    dataArr.put(grJsonobj);
                }
            }
//            kwl = accGoodsReceiptDAOObj.getPurchaseReturn(requestParams);
//            List prList = kwl.getEntityList();
//            Iterator itr = prList.iterator();
//            while (itr.hasNext()) {
//                Object[] oj = (Object[]) itr.next();
//                String id = oj[0].toString();
//                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseReturn.class.getName(), id);
//                PurchaseReturn pr = (PurchaseReturn) objItr.getEntityList().get(0);
//
//                Set<PurchaseReturnDetail> purchaseReturnRows = pr.getRows();
//                String currencyid = (pr.getCurrency() == null ? currency.getCurrencyID() : pr.getCurrency().getCurrencyID());
//
//                double TotalExcise = 0.0;
//                double TotalExcise_Stock = 0.0;
//                double TotalAssasableValue = 0.0;
//                double TotalAssasableValue_Stock = 0.0;
//                boolean addRecord = true;
//                boolean stockItemTypeFlag = false;
//                for (PurchaseReturnDetail row : purchaseReturnRows) {
//                    if (row.getVidetails() != null) {
//                        if (row.getVidetails().getGoodsReceipt() != null && row.getVidetails().getGoodsReceipt().isIsExciseInvoice()) {
//                            if (!StringUtil.isNullOrEmpty(stockItemType) && !stockItemType.equals(row.getInventory().getProduct().getNatureofStockItem())) {
//                                stockItemTypeFlag = true;
//                                HashMap<String, Object> PurchaseReturnDetailParams = new HashMap<String, Object>();
//                                PurchaseReturnDetailParams.put("PurchaseReturnDetailid", row.getID());
//                                KwlReturnObject prdTermMapresult = accGoodsReceiptDAOObj.getPurchaseReturnDetailTermMap(PurchaseReturnDetailParams);
//                                List<PurchaseReturnDetailsTermMap> PurchaseReturnDetailTermsMapList = prdTermMapresult.getEntityList();
//                                for (PurchaseReturnDetailsTermMap purchaseReturnDetailTermMap : PurchaseReturnDetailTermsMapList) {
//                                    if (purchaseReturnDetailTermMap.getTerm() != null && purchaseReturnDetailTermMap.getTerm().getTermType() == 2) {
//                                        KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, purchaseReturnDetailTermMap.getTermamount(), currencyid, pr.getOrderDate(), pr.getExternalCurrencyRate());
//                                        Double Excise = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
//                                        TotalExcise += Excise;
//                                        kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, purchaseReturnDetailTermMap.getAssessablevalue(), currencyid, pr.getOrderDate(), pr.getExternalCurrencyRate());
//                                        Double assesable_value = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
//                                        TotalAssasableValue += assesable_value;
//                                    }
//                                }
//                            }
//                            if (!StringUtil.isNullOrEmpty(templateid) && ((row.getVidetails().getGoodsReceipt().getModuletemplateid() != null && !templateid.equals(row.getVidetails().getGoodsReceipt().getModuletemplateid().getTemplateId())) || row.getVidetails().getGoodsReceipt().getModuletemplateid() == null)) {
//                                addRecord = false;
//                                break;
//                            }
//                            if (!stockItemTypeFlag) {
//                                HashMap<String, Object> PurchaseReturnDetailParams = new HashMap<String, Object>();
//                                PurchaseReturnDetailParams.put("PurchaseReturnDetailid", row.getID());
//                                KwlReturnObject prdTermMapresult = accGoodsReceiptDAOObj.getPurchaseReturnDetailTermMap(PurchaseReturnDetailParams);
//                                List<PurchaseReturnDetailsTermMap> PurchaseReturnDetailTermsMapList = prdTermMapresult.getEntityList();
//                                for (PurchaseReturnDetailsTermMap purchaseReturnDetailTermMap : PurchaseReturnDetailTermsMapList) {
//                                    if (purchaseReturnDetailTermMap.getTerm() != null && purchaseReturnDetailTermMap.getTerm().getTermType() == 2) {
//                                        KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, purchaseReturnDetailTermMap.getTermamount(), currencyid, pr.getOrderDate(), pr.getExternalCurrencyRate());
//                                        Double Excise = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
//                                        TotalExcise += Excise;
//                                        kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, purchaseReturnDetailTermMap.getAssessablevalue(), currencyid, pr.getOrderDate(), pr.getExternalCurrencyRate());
//                                        Double assesable_value = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
//                                        TotalAssasableValue += assesable_value;
//                                    }
//                                }
//                            }
//                        } else {
//                            addRecord = false;
//                            break;
//                        }
//                    } else {
//                        addRecord = false;
//                        break;
//                    }
//                }
//                if (stockItemTypeFlag) {
//                    TotalExcise = TotalExcise_Stock;
//                    TotalAssasableValue = TotalAssasableValue_Stock;
//                }
//                if (addRecord || stockItemTypeFlag) {
//                    String date = (pr.getOrderDate() != null) ? userdf.format(pr.getOrderDate()) : "";
//                    JSONObject grJsonobj = new JSONObject();
//                    grJsonobj.put("dateofentry", date);
//                    grJsonobj.put("documentnumber", pr.getPurchaseReturnNumber());
//                    grJsonobj.put("documenttype", "Excise Purchase Return");
//                    grJsonobj.put("suppliername", pr.getVendor().getName());
//                    grJsonobj.put("eccno", pr.getVendor().getECCnumber());
//                    grJsonobj.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
//                    grJsonobj.put("assessablevalue", authHandler.formattingdecimal(TotalAssasableValue, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
//                    grJsonobj.put("exciseduty", authHandler.formattingdecimal(TotalExcise, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
//                    dataArr.put(grJsonobj);
//                }
//            }

            /*
             * --- Credit Adjustment JE post Entry ---
             */
                for (String jeId : creditAdj.keySet()) {
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeId);
                    JournalEntry journalentry = (JournalEntry) objItr.getEntityList().get(0);
                    double creditAdjAmount = 0.0d;
                    for (double amount : creditAdj.get(jeId)) {
                        creditAdjAmount += amount;
                    }
                    String date = (journalentry.getCreatedOn() != null) ? userdf.format(journalentry.getCreatedOn()) : "";
                    JSONObject grJsonobj = new JSONObject();
                    grJsonobj.put("dateofentry", date);
                    grJsonobj.put("documentnumber", "");
                    grJsonobj.put("jenumber", journalentry.getEntryNumber());
                    grJsonobj.put("documenttype", "Credit Adjustment");
                    grJsonobj.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    grJsonobj.put("exciseduty", authHandler.formattingDecimalForAmount(creditAdjAmount, companyid));
                    dataArr.put(grJsonobj);
                }
            int totalCount = dataArr.length();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit) && !isExport) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            finaljobj.put("data", dataArr);
            finaljobj.put("totalcount", totalCount);
            finaljobj.put("count", totalCount);
            finaljobj.put("success", true);


        } catch (SessionExpiredException | ServiceException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return finaljobj;
    }
    public JSONObject getCreditAvailedJson(HttpServletRequest request, boolean isExport){
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "", exciseUnit = "";
        boolean issuccess = false;
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        String stockItemType = "", templateid = "";
        boolean excludeJE = false, isSTCompReport = false;

        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            HashMap requestParams = new HashMap();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("excludeJE"))) {
                excludeJE = Boolean.parseBoolean(request.getParameter("excludeJE"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                requestParams.put("startdate", request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("stockitemtype"))) {
                stockItemType = request.getParameter("stockitemtype");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isExciseInvoice"))) {
                requestParams.put("isExciseInvoice", request.getParameter("isExciseInvoice"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("includeAllRec"))) {
                requestParams.put("includeAllRec", request.getParameter("includeAllRec"));
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("exciseunit"))) {
                exciseUnit = request.getParameter("exciseunit");
                List modt = new ArrayList();
                requestParams.put("companyunitid", exciseUnit);
                KwlReturnObject kwltemp = accountingHandlerDAOobj.getModuleTemplates(requestParams);
                modt = kwltemp.getEntityList();
                if (modt.size() > 0) {
                    ModuleTemplate moduleTemp = (ModuleTemplate) modt.get(0);
                    templateid = moduleTemp.getTemplateId();
                    requestParams.put("moduletemplateid", templateid);

                }
            }
            requestParams.put("isSalesReturnCreditNote", true);
            requestParams.put("isPurchaseReturnCreditNote", true);
            requestParams.put(Constants.df, df);
            requestParams.put("dateformat", df);
            if (!StringUtil.isNullOrEmpty(request.getParameter("isSTCompReport"))) {
                isSTCompReport = Boolean.parseBoolean(request.getParameter("isSTCompReport"));
            }
            if (isSTCompReport) {// If Service Tax Computation Report.
                requestParams.put("termType", IndiaComplianceConstants.LINELEVELTERMTYPE_SERVICE_TAX);
            } else { // else Excise Compaputation Report.
                requestParams.put("termType", IndiaComplianceConstants.LINELEVELTERMTYPE_Excise_DUTY);
            }
            Map<String, ArrayList<Double>> creditAdj = new HashMap<String, ArrayList<Double>>();


            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("companyid", companyid);
            params.put("gcurrencyid", currency.getCurrencyID());
            JSONObject jObj = new JSONObject();
            List grList = accGoodsReceiptDAOObj.getGoodsRecieptIndiaTaxDetails(requestParams);
            jObj = indiaTaxComputation(grList, jObj);
            JSONObject grHasmap = jObj.getJSONObject("grId");
            Iterator itr = grHasmap.keys();
            while (itr.hasNext()) {
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), (String) itr.next());
                GoodsReceipt goodsReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                if (goodsReceipt.getIsCenvatAdjust() == 1) {
                    continue;
                }
                boolean isstockItemPresent = false;
                boolean isCreditPaid = true;
                if (StringUtil.isNullOrEmpty(stockItemType)) {
                    isstockItemPresent = true;
                }
                Set<GoodsReceiptDetail> rows = goodsReceipt.getRows();
                String currencyid = (goodsReceipt.getCurrency() == null ? currency.getCurrencyID() : goodsReceipt.getCurrency().getCurrencyID());
                double TotalExcise = 0.0;
                boolean isSTCreditPaid = true;
                double TotalServiceTax = 0.0;
                double TotalKKC = 0.0;
                double TotalSTAssasableValue = 0.0;
                double TotalAssasableValue = 0.0;

                for (GoodsReceiptDetail row : rows) {
                    isstockItemPresent = true;
                    HashMap<String, Object> grDetailParams = new HashMap();
                    grDetailParams.put("GoodsReceiptDetailid", row.getID());
                    KwlReturnObject grMapresult = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(grDetailParams);
                    List<ReceiptDetailTermsMap> grDetailTermsMapList = grMapresult.getEntityList();
                    for (ReceiptDetailTermsMap grDetailTermMap : grDetailTermsMapList) {
//                        Date creationDate=goodsReceipt.getJournalEntry()!=null?goodsReceipt.getJournalEntry().getEntryDate():goodsReceipt.getCreationDate();
                        Date creationDate=goodsReceipt.getCreationDate();
                        Double currencyRate=goodsReceipt.getJournalEntry()!=null?goodsReceipt.getJournalEntry().getExternalCurrencyRate():0;
                        if (grDetailTermMap.getTerm() != null) {
                            if (grDetailTermMap.getTerm().getTermType() == IndiaComplianceConstants.LINELEVELTERMTYPE_Excise_DUTY ) {//
                                KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, creationDate,currencyRate);
                                Double Excise = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                TotalExcise += Excise;
                                kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, creationDate, currencyRate);
                                Double assesable_value_to_add = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                TotalAssasableValue += assesable_value_to_add;
                                if (grDetailTermMap.getCreditAvailedFlag() == 1 && !isSTCompReport ) {
                                    isCreditPaid = false;
                                    break;
                                }
                            } else if ((grDetailTermMap.getTerm().getTermType() == IndiaComplianceConstants.LINELEVELTERMTYPE_SERVICE_TAX)) {
                                KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, creationDate,currencyRate);
                                Double ServiceTax = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                TotalServiceTax += ServiceTax;
                                kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, creationDate,currencyRate);
                                Double assesable_value_to_add = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                TotalSTAssasableValue = assesable_value_to_add;
                                if (isSTCompReport && grDetailTermMap.getCreditAvailedFlagServiceTax() == 1) {
                                    isSTCreditPaid = false;
                                    break;
                                }
                            } else if (isSTCompReport && (grDetailTermMap.getTerm().getTermType() == IndiaComplianceConstants.LINELEVELTERMTYPE_KKC)) {
                                KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, creationDate,currencyRate);
                                Double KKC = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                TotalKKC += KKC;
                            }
                        }
                    }
                    double TotalService = 0.0;
                    double capitalgoods = 0.0;
                    HashMap<String, Object> grDetailParamsnew = new HashMap();
                    grDetailParamsnew.put("GoodsReceiptid", goodsReceipt.getID());
                    grDetailParamsnew.put("termtype", 4);
                    KwlReturnObject grMapresultnew = accGoodsReceiptDAOObj.getGenricGoodsReceiptdetailTermMap(grDetailParamsnew);
                    Iterator itrObj = grMapresultnew.getEntityList().iterator();
                    while (itrObj.hasNext()) {
                        Object[] oj = (Object[]) itrObj.next();
                        if (oj[1] != null) {
                            TotalService += Double.valueOf(oj[1].toString());
                        }
                        if (oj[0] != null) {
                            TotalAssasableValue += Double.valueOf(oj[0].toString());
                        }
                    }
                    if ((isCreditPaid && TotalExcise > 0) || (isSTCompReport && isSTCreditPaid && TotalServiceTax > 0 && TotalKKC > 0)) {
//                        String date = (goodsReceipt.getJournalEntry() != null) ? userdf.format(goodsReceipt.getJournalEntry().getEntryDate()) : "";
                        String date = (goodsReceipt.getCreationDate() != null) ? userdf.format(goodsReceipt.getCreationDate()) : "";
                        JSONObject grJsonobj = new JSONObject();
                        grJsonobj.put("dateofentry", date);
                        grJsonobj.put("documentnumber", goodsReceipt.getGoodsReceiptNumber());
                        grJsonobj.put("documentid", goodsReceipt.getID());
                        grJsonobj.put("jenumber", goodsReceipt.getJournalEntry() != null ? goodsReceipt.getJournalEntry().getEntryNumber() : "");
                        grJsonobj.put("documenttype", "Excise Purchase");
                        grJsonobj.put("suppliername", goodsReceipt.getVendor().getName());
                        grJsonobj.put("eccno", goodsReceipt.getVendor().getECCnumber());
                        grJsonobj.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                        if (isSTCompReport) {
                            grJsonobj.put("kkc", authHandler.formattingDecimalForAmount(TotalKKC, companyid));
                            grJsonobj.put("servicetax", authHandler.formattingDecimalForAmount(TotalServiceTax, companyid));
                            grJsonobj.put("assessablevalue", authHandler.formattingDecimalForAmount(TotalSTAssasableValue, companyid));
                            grJsonobj.put("exciseduty", authHandler.formattingDecimalForAmount(TotalExcise, companyid));
                        } else {
                            grJsonobj.put("exciseduty", authHandler.formattingDecimalForAmount(TotalExcise, companyid));
                            grJsonobj.put("assessablevalue", authHandler.formattingDecimalForAmount(TotalAssasableValue, companyid));
                            grJsonobj.put("servicetax", authHandler.formattingDecimalForAmount(TotalServiceTax, companyid));
                        }
                        grJsonobj.put("capitalgoods", authHandler.formattingDecimalForAmount(capitalgoods, companyid));
                        dataArr.put(grJsonobj);
                    }
                }
            }


//            while (gritr.hasNext()) {
//                Object[] oj = (Object[]) gritr.next();
//                String grid = oj[0].toString();
//                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grid);
//                GoodsReceipt goodsReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
//                if(goodsReceipt.getIsCenvatAdjust()==1){
//                    continue;
//                }
//                boolean isstockItemPresent = false;
//                boolean isCreditPaid = true;
//                if (StringUtil.isNullOrEmpty(stockItemType)) {
//                    isstockItemPresent = true;
//                }
//                Set<GoodsReceiptDetail> rows = goodsReceipt.getRows();
//                String currencyid = (goodsReceipt.getCurrency() == null ? currency.getCurrencyID() : goodsReceipt.getCurrency().getCurrencyID());
//                double TotalExcise = 0.0;
//                double TotalAssasableValue = 0.0;
//                for (GoodsReceiptDetail row : rows) {
//                    isstockItemPresent = true;
//                    HashMap<String, Object> grDetailParams = new HashMap();
//                    grDetailParams.put("GoodsReceiptDetailid", row.getID());
//                    KwlReturnObject grMapresult = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(grDetailParams);
//                    List<ReceiptDetailTermsMap> grDetailTermsMapList = grMapresult.getEntityList();
//                    for (ReceiptDetailTermsMap grDetailTermMap : grDetailTermsMapList) {
//                        if (grDetailTermMap.getTerm() != null && grDetailTermMap.getTerm().getTermType() == 2) {
//                            KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
//                            Double Excise = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
//                            TotalExcise += Excise;
//                            kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
//                            Double assesable_value_to_add = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
//                            TotalAssasableValue += assesable_value_to_add;
//                            if (grDetailTermMap.getCreditAvailedFlag() == 1) {
//                                isCreditPaid = false;
//                                break;
//                            }
//                        }
//                    }
//                }
//                if (isCreditPaid && TotalExcise > 0) {
//                    String date = (goodsReceipt.getJournalEntry() != null) ? userdf.format(goodsReceipt.getJournalEntry().getEntryDate()) : "";
//                    JSONObject grJsonobj = new JSONObject();
//                    grJsonobj.put("dateofentry", date);
//                    grJsonobj.put("documentnumber", goodsReceipt.getGoodsReceiptNumber());
//                    grJsonobj.put("documentid", goodsReceipt.getID());
//                    grJsonobj.put("jenumber", goodsReceipt.getJournalEntry() != null ? goodsReceipt.getJournalEntry().getEntryNumber() : "");
//                    grJsonobj.put("documenttype", "Excise Purchase");
//                    grJsonobj.put("suppliername", goodsReceipt.getVendor().getName());
//                    grJsonobj.put("eccno", goodsReceipt.getVendor().getECCnumber());
//                    grJsonobj.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
//                    grJsonobj.put("assessablevalue", authHandler.formattingdecimal(TotalAssasableValue, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
//                    grJsonobj.put("exciseduty", authHandler.formattingdecimal(TotalExcise, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
//                    dataArr.put(grJsonobj);
//                }
//            }
            int totalCount = dataArr.length();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit) && !isExport) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            finaljobj.put("data", dataArr);
            finaljobj.put("totalcount", totalCount);
            finaljobj.put("count", totalCount);
            finaljobj.put("success", true);


        } catch (SessionExpiredException | ServiceException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return finaljobj;
    }
    public JSONObject indiaTaxComputation(List grExciseDetailList, JSONObject jObj) throws JSONException {
        double totalAssessableValue = 0.0;
        double totalTaxAmount = 0.0;
        String termAccount = null;
        Iterator itr = grExciseDetailList.iterator();
        HashMap<String, Double> grIDSet = new HashMap<String, Double>();
        while (itr.hasNext()) {
            Object[] oj = (Object[]) itr.next();

            String accName = oj[0].toString(); //Account Name
            String accId = oj[1].toString(); // Account uuid
            double percent = (Double) oj[2]; // Percentage
            double assessableValues = (Double) oj[3]; // Assessable Value
            double termamount = (Double) oj[4]; // Term Amount (Calculated Tax Amount)
            int termType = (Integer) oj[5]; // Term type (Tax Description)
            termAccount = oj[6].toString(); // Term Account
            totalAssessableValue += assessableValues;
            totalTaxAmount += termamount;
            if (!StringUtil.isNullObject(oj[7]) && !grIDSet.containsKey(oj[7])) {
                grIDSet.put(oj[7].toString(), termamount);
            }
        }
        jObj.put("accessablevalue", totalAssessableValue);
        jObj.put("dutyamount", totalTaxAmount);
        jObj.put("account", termAccount);
        jObj.put("grId", grIDSet);
        return jObj;
    }
    public JSONObject getPlaReportJson(HttpServletRequest request, boolean isExport){
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String msg = "",exciseUnit="";
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        String templateid = "";
        double TotalBalance = 0.0;

        try {
            DateFormat df=authHandler.getDateOnlyFormat();
            DateFormat userdf=authHandler.getUserDateFormatter(request);
            HashMap requestParams = new HashMap();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                requestParams.put("startdate", request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isExciseInvoice"))){
                requestParams.put("isExciseInvoice", request.getParameter("isExciseInvoice"));
            }
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("exciseunit"))){
                exciseUnit = request.getParameter("exciseunit");
                List modt = new ArrayList();
                requestParams.put("companyunitid", exciseUnit);
                KwlReturnObject kwltemp = accountingHandlerDAOobj.getModuleTemplates(requestParams);
                modt = kwltemp.getEntityList();
                if(modt.size() > 0){
                    ModuleTemplate moduleTemp = (ModuleTemplate) modt.get(0);
                    templateid = moduleTemp.getTemplateId();
                    requestParams.put("moduletemplateid", templateid);
                    
                }
            }
            requestParams.put("isSalesReturnCreditNote", true);
            requestParams.put("isPurchaseReturnCreditNote", true);
            requestParams.put(Constants.df, df);
            requestParams.put("dateformat", df);
            Map<String, ArrayList<Double>> creditAdj = new HashMap<String, ArrayList<Double>>();
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("companyid", companyid);
            params.put("gcurrencyid", currency.getCurrencyID());
            
            
            KwlReturnObject kwl = accInvoiceDAOobj.getInvoicesMerged(requestParams);
            List invoiceList = kwl.getEntityList();
            Iterator itr = invoiceList.iterator();
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
           
            while (itr.hasNext()) {
                Object[] oj = (Object[])itr.next();               
                String invid = oj[0].toString();
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                Invoice invoice = (Invoice) objItr.getEntityList().get(0);
                if (StringUtil.isNullOrEmpty(invoice.getExcisetype())) {
                    continue;
                }
                String currencyid = (invoice.getCurrency() == null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());


                Set<InvoiceDetail> rows = invoice.getRows();
                double TotalExcise = 0.0;
                for(InvoiceDetail row: rows){                                              
                    HashMap<String, Object> invoiceDetailParams = new HashMap();
                    invoiceDetailParams.put("InvoiceDetailid", row.getID());
                    KwlReturnObject invoiceMapresult = accInvoiceDAOobj.getInvoicedetailTermMap(invoiceDetailParams);
                    List<InvoiceDetailTermsMap> PurchaseReturnDetailTermsMapList = invoiceMapresult.getEntityList();
                    for (InvoiceDetailTermsMap purchaseReturnDetailTermMap : PurchaseReturnDetailTermsMapList) {
                        if (purchaseReturnDetailTermMap.getTerm() != null && purchaseReturnDetailTermMap.getTerm().getTermType() == 2) {
//                            KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, purchaseReturnDetailTermMap.getTermamount(), currencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                            KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, purchaseReturnDetailTermMap.getTermamount(), currencyid, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                            Double Excise = (Double)kwlBaseCurrencyrate.getEntityList().get(0);
                            TotalExcise += Excise;
                            if(purchaseReturnDetailTermMap.getCreditAvailedFlag()==1){ 
                                        /* --- Credit Adjustment JE post Entry  --- */	
                                        if (creditAdj.containsKey(purchaseReturnDetailTermMap.getTaxPaymentJE().getID())) {
//                                            kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, purchaseReturnDetailTermMap.getAssessablevalue(), currencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                                            kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, purchaseReturnDetailTermMap.getAssessablevalue(), currencyid, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                                            ArrayList<Double> tempAmt = (ArrayList<Double>) creditAdj.get(purchaseReturnDetailTermMap.getTaxPaymentJE().getID());
                                            Double assesable_value = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                            tempAmt.add(purchaseReturnDetailTermMap.getTermamount());
                                            creditAdj.put(purchaseReturnDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                        } else {
                                            ArrayList<Double> tempAmt = new ArrayList<Double>();
                                            tempAmt.add(purchaseReturnDetailTermMap.getTermamount());
                                            creditAdj.put(purchaseReturnDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                        }
                                    }
                        } 
                    }
                }
                TotalBalance += TotalExcise;
//                String date = (invoice.getJournalEntry() != null)?userdf.format(invoice.getJournalEntry().getEntryDate()):"";
                String date = (invoice.getCreationDate() != null)?userdf.format(invoice.getCreationDate()):"";
                JSONObject invoiceJsonobj = new JSONObject();
                invoiceJsonobj.put("documenttype", "Excise Sales");
                invoiceJsonobj.put("documentnumber", invoice.getInvoiceNumber()+" / "+date);
                invoiceJsonobj.put("hsncode", extraCompanyPreferences.getHSNCode());
                invoiceJsonobj.put("eccno", invoice.getCustomer().getECCnumber());
                invoiceJsonobj.put("credit","");
                invoiceJsonobj.put("debit",authHandler.formattingDecimalForAmount(TotalExcise, companyid));
                invoiceJsonobj.put("balance", authHandler.formattingDecimalForAmount(TotalBalance, companyid));
                
                dataArr.put(invoiceJsonobj);
            }
            /*
             * --- Credit Adjustment JE post Entry ---
             */
            String stockItemType = "";
            kwl = accGoodsReceiptDAOObj.getGoodsReceiptsMerged(requestParams);
            List grList = kwl.getEntityList();

            Iterator gritr = grList.iterator();

            while (gritr.hasNext()) {
                Object[] oj = (Object[]) gritr.next();
                String grid = oj[0].toString();
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grid);
                GoodsReceipt goodsReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                boolean isstockItemPresent = false;
                if (StringUtil.isNullOrEmpty(stockItemType)) {
                    isstockItemPresent = true;
                }
                Set<GoodsReceiptDetail> rows = goodsReceipt.getRows();
                String currencyid = (goodsReceipt.getCurrency() == null ? currency.getCurrencyID() : goodsReceipt.getCurrency().getCurrencyID());
                double TotalExcise = 0.0;
                double TotalAssasableValue = 0.0;
                for (GoodsReceiptDetail row : rows) {
                    if (!StringUtil.isNullOrEmpty(stockItemType)) {
                        if (stockItemType.equals(row.getInventory().getProduct().getNatureofStockItem())) {
                            isstockItemPresent = true;
                            HashMap<String, Object> grDetailParams = new HashMap();
                            grDetailParams.put("GoodsReceiptDetailid", row.getID());
                            KwlReturnObject grMapresult = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(grDetailParams);
                            List<ReceiptDetailTermsMap> grDetailTermsMapList = grMapresult.getEntityList();
                            for (ReceiptDetailTermsMap grDetailTermMap : grDetailTermsMapList) {
                                if (grDetailTermMap.getTerm() != null && grDetailTermMap.getTerm().getTermType() == 2) {
//                                    KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                    KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                    Double Excise = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                    TotalExcise += Excise;
//                                    kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                    kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                    Double assesable_value_to_add = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                    TotalAssasableValue += assesable_value_to_add;
                                    if(grDetailTermMap.getCreditAvailedFlag()==1){ 
                                        /* --- Credit Adjustment JE post Entry  --- */		                                    
                                        TotalExcise += Excise;
                                        if (creditAdj.containsKey(grDetailTermMap.getTaxPaymentJE().getID())) {
//                                            kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                            kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                            ArrayList<Double> tempAmt = (ArrayList<Double>) creditAdj.get(grDetailTermMap.getTaxPaymentJE().getID());
                                            Double assesable_value = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                            tempAmt.add(grDetailTermMap.getTermamount());
                                            TotalAssasableValue += assesable_value;
                                            creditAdj.put(grDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                        } else {
                                            ArrayList<Double> tempAmt = new ArrayList<Double>();
                                            tempAmt.add(grDetailTermMap.getTermamount());
                                            creditAdj.put(grDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        HashMap<String, Object> grDetailParams = new HashMap();
                        grDetailParams.put("GoodsReceiptDetailid", row.getID());
                        KwlReturnObject grMapresult = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(grDetailParams);
                        List<ReceiptDetailTermsMap> grDetailTermsMapList = grMapresult.getEntityList();
                        for (ReceiptDetailTermsMap grDetailTermMap : grDetailTermsMapList) {
                            if (grDetailTermMap.getTerm() != null && grDetailTermMap.getTerm().getTermType() == 2) {
//                                KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                KwlReturnObject kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getTermamount(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                Double Excise = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                TotalExcise += Excise;
//                                kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                Double assesable_value_to_add = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                TotalAssasableValue += assesable_value_to_add;
                                if (grDetailTermMap.getCreditAvailedFlag()==1 && grDetailTermMap.getTaxPaymentJE()!=null) {
                                    /*
                                     * --- Credit Adjustment JE post Entry ---
                                     */
                                    if (creditAdj.containsKey(grDetailTermMap.getTaxPaymentJE().getID())) {
//                                        kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                        kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(params, grDetailTermMap.getAssessablevalue(), currencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                        ArrayList<Double> tempAmt = (ArrayList<Double>) creditAdj.get(grDetailTermMap.getTaxPaymentJE().getID());
                                        Double assesable_value = (Double) kwlBaseCurrencyrate.getEntityList().get(0);
                                        tempAmt.add(grDetailTermMap.getTermamount());
                                        TotalAssasableValue += assesable_value;
                                        creditAdj.put(grDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                    } else {
                                        ArrayList<Double> tempAmt = new ArrayList<Double>();
                                        tempAmt.add(grDetailTermMap.getTermamount());
                                        creditAdj.put(grDetailTermMap.getTaxPaymentJE().getID(), tempAmt);
                                    }
                                }

                            }
                        }
                    }
                }               
            }
            /*
             * --- Credit Adjustment JE post Entry ---
             */
//          
            String excisePayableAcc = StringUtil.isNullOrEmpty(extraCompanyPreferences.getExcisePayableAcc())?"":extraCompanyPreferences.getExcisePayableAcc();
            
            String exciseDutyAdvancePaymentaccount =StringUtil.isNullOrEmpty(extraCompanyPreferences.getExciseDutyAdvancePaymentaccount())?"":extraCompanyPreferences.getExciseDutyAdvancePaymentaccount();
            
            for (String jeId : creditAdj.keySet()) {
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeId);
                JournalEntry journalentry = (JournalEntry) objItr.getEntityList().get(0);
                double creditAdjAmount = 0.0d;
                Set<JournalEntryDetail> jedSet=(Set<JournalEntryDetail>)journalentry.getDetails();
                for(JournalEntryDetail jedObj:jedSet){
                    if(!jedObj.isDebit() && !jedObj.getAccount().getID().equals(excisePayableAcc)){
                        creditAdjAmount+=jedObj.getAmount();
                    }
                }
                TotalBalance-=creditAdjAmount;
                String date = (journalentry.getCreatedOn() != null) ? userdf.format(journalentry.getCreatedOn()) : "";
                JSONObject grJsonobj = new JSONObject();
                grJsonobj.put("documentnumber", journalentry.getEntryNumber());
                grJsonobj.put("documenttype", "CENVAT Credit Adjustment");
                grJsonobj.put("credit", authHandler.formattingDecimalForAmount(creditAdjAmount, companyid));
                grJsonobj.put("balance", authHandler.formattingDecimalForAmount(TotalBalance, companyid));
                dataArr.put(grJsonobj);
            }       
            
            /* --- Advance Make Payment Tag to JE Adjustment Of Excise Duty  ---*/  
            requestParams.put("exciseDutyAdvancePaymentaccount", exciseDutyAdvancePaymentaccount);
            KwlReturnObject kwlAdvExcPay = accInvoiceDAOobj.getAdvanceJEAdjustmentOfExcisDuty(requestParams);
            List advExcisePayment = kwlAdvExcPay.getEntityList();
            Iterator itrMP = advExcisePayment.iterator();
            while(itrMP.hasNext()){
                JournalEntryDetail pdow= (JournalEntryDetail)itrMP.next();
                if(pdow.getJournalEntry()!=null && !pdow.isDebit()){
                    JournalEntry paymentObj=pdow.getJournalEntry();
                    TotalBalance-=pdow.getAmount();
                    String date = (paymentObj.getEntryDate() != null)?userdf.format(paymentObj.getEntryDate()):"";
                    JSONObject invoiceJsonobj = new JSONObject();
                    invoiceJsonobj.put("documenttype", "Advance Excise Duty (Payment)");
                    invoiceJsonobj.put("documentnumber", paymentObj.getEntryNumber() + " / " + date);
                    invoiceJsonobj.put("hsncode", extraCompanyPreferences.getHSNCode());
                    invoiceJsonobj.put("eccno", "");
                    invoiceJsonobj.put("credit", authHandler.formattingDecimalForAmount(pdow.getAmount(), companyid));
                    invoiceJsonobj.put("debit", "");
                    invoiceJsonobj.put("balance", authHandler.formattingDecimalForAmount(TotalBalance, companyid));
                    dataArr.put(invoiceJsonobj);                    

                }
            } 
            /* ---  Make Payment of Excise Duty  ---*/
            
            requestParams.put("exciseDutyAdvancePaymentaccount", excisePayableAcc);
            kwlAdvExcPay = accInvoiceDAOobj.getAdvanceMakepaymentOfExcisDuty(requestParams);
            advExcisePayment = kwlAdvExcPay.getEntityList();
            itrMP = advExcisePayment.iterator();
            while(itrMP.hasNext()){
                PaymentDetailOtherwise pdow= (PaymentDetailOtherwise)itrMP.next();
                if(pdow.getPayment()!=null && pdow.isIsdebit()){
                    Payment paymentObj=pdow.getPayment();
                    TotalBalance-=pdow.getAmount();
                    String date = (paymentObj.getJournalEntry() != null)?userdf.format(paymentObj.getJournalEntry().getEntryDate()):"";
                    JSONObject invoiceJsonobj = new JSONObject();
                    invoiceJsonobj.put("documenttype", "Excise Duty Paid (Payment)");
                    invoiceJsonobj.put("documentnumber", paymentObj.getPaymentNumber() + " / " + date);
                    invoiceJsonobj.put("hsncode", extraCompanyPreferences.getHSNCode());
                    invoiceJsonobj.put("eccno", "");
                    invoiceJsonobj.put("credit", authHandler.formattingDecimalForAmount(pdow.getAmount(), companyid));
                    invoiceJsonobj.put("debit", "");
                    invoiceJsonobj.put("balance", authHandler.formattingDecimalForAmount(TotalBalance, companyid));
                    dataArr.put(invoiceJsonobj);
                    
                }
            } 
           
            int totalCount = dataArr.length();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit) && !isExport) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            finaljobj.put("data",dataArr);
            finaljobj.put("totalcount",totalCount);
            finaljobj.put("count",totalCount);
            finaljobj.put("success",true);
           
           
        } catch (SessionExpiredException | ServiceException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finaljobj;
    }
    public ModelAndView getServiceTaxInputCreditSummaryReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject dataMap = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        try {
            DateFormat df=authHandler.getDateOnlyFormat();
            DateFormat userdf=authHandler.getUserDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                dataMap.put(Constants.REQ_startdate, request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                dataMap.put(Constants.REQ_enddate, request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("start"))) {
                dataMap.put("start",request.getParameter("start"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("limit"))) {
                dataMap.put("limit",request.getParameter("limit"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("type"))) {
                dataMap.put("type",request.getParameter("type"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("basisOfCalculation"))) {
                dataMap.put("basisOfCalculation",request.getParameter("basisOfCalculation"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("natureOfTransaction"))) {// Import on service check  (For filter - vendor IEC code not empty)
                dataMap.put("natureOfTransaction",request.getParameter("natureOfTransaction"));
            }
            dataMap.put("dateformat",df);
            dataMap.put("userdateformat",userdf);
            dataMap.put("companyid",companyid);
            dataMap.put("locale", RequestContextUtils.getLocale(request));
            finaljobj = accReportsService.getServiceTaxInputCreditSummaryReportJSON(dataMap,false);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
}
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());

    }
    public ModelAndView exportServiceTaxInputCreditSummaryReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject dataMap = new JSONObject();
        String view = "jsonView_ex";
        try {
            DateFormat df=authHandler.getDateOnlyFormat();
            DateFormat userdf=authHandler.getUserDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                dataMap.put(Constants.REQ_startdate, request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                dataMap.put(Constants.REQ_enddate, request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("type"))) {
                dataMap.put("type",request.getParameter("type"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("basisOfCalculation"))) {
                dataMap.put("basisOfCalculation", request.getParameter("basisOfCalculation"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("natureOfTransaction"))) {// Import on service check  (For filter - vendor IEC code not empty)
                dataMap.put("natureOfTransaction",request.getParameter("natureOfTransaction"));
            }
            dataMap.put("dateformat",df);
            dataMap.put("userdateformat",userdf);
            dataMap.put("companyid",companyid);
            dataMap.put("locale", RequestContextUtils.getLocale(request));
            jobj = accReportsService.getServiceTaxInputCreditSummaryReportJSON(dataMap,true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView getTDSChallanControlReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject dataMap = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            JSONObject jobjdateFormate = new JSONObject();
            jobjdateFormate.put("userdateformat","yyyy-MM-dd");
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(jobjdateFormate);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                dataMap.put(Constants.REQ_startdate, request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                dataMap.put(Constants.REQ_enddate, request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("start"))) {
                dataMap.put("start", request.getParameter("start"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("limit"))) {
                dataMap.put("limit", request.getParameter("limit"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("type"))) {
                dataMap.put("type", request.getParameter("type"));
            }
            dataMap.put("dateformat", df);
            dataMap.put("userdateformat", userdf);
            dataMap.put("companyid", companyid);
            finaljobj = accReportsService.getTDSChallanControlReportJSON(dataMap, false);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());

    }
     
 public ModelAndView exportTDSChallanControlReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject dataMap = new JSONObject();
        String view = "jsonView_ex";
        try {
            DateFormat df=authHandler.getDateOnlyFormat();
            DateFormat userdf=authHandler.getUserDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                dataMap.put(Constants.REQ_startdate, request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                dataMap.put(Constants.REQ_enddate, request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("type"))) {
                dataMap.put("ExportType", request.getParameter("type"));
            }
            dataMap.put("dateformat",df);
            dataMap.put("userdateformat",userdf);
            dataMap.put("companyid",companyid);
            jobj = accReportsService.getTDSChallanControlReportJSON(dataMap,true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }  
    
     public ModelAndView getSalesPurhcaseAnnexureReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject finaljobj = new JSONObject();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String isSalesAnnax = request.getParameter("isSalesAnnax");
            String isExport = request.getParameter("isExport");
            HashMap requestParams = new HashMap();            
            requestParams.put(Constants.df, df);
            requestParams.put("dateformat", df);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid =  sessionHandlerImpl.getCurrencyID(request);
            
            requestParams.put("companyid", companyid);            
            requestParams.put("currencyid", currencyid);            
            requestParams.put("isSalesAnnax", isSalesAnnax);            
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                requestParams.put("startdate", request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(start)){
                requestParams.put("start", start);
            }
            if (!StringUtil.isNullOrEmpty(limit)) {
                requestParams.put("limit", limit);
            }
            if (!StringUtil.isNullOrEmpty(isSalesAnnax)) {
                requestParams.put("isSalesAnnax", isSalesAnnax);
            }
            if (!StringUtil.isNullOrEmpty(isExport)) {
                requestParams.put("isExport", isExport);
            }            
            finaljobj = getSalesPurhcaseAnnexureReportJSON(requestParams);           
        } catch(Exception ex){
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());

    }
    /*DVAT Form 31 : Sales & Outward Branch Transfer Register*/ 
    public ModelAndView getDVATForm31Report(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject dataMap = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                dataMap.put(Constants.REQ_startdate, request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                dataMap.put(Constants.REQ_enddate, request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("start"))) {
                dataMap.put("start", request.getParameter("start"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("limit"))) {
                dataMap.put("limit", request.getParameter("limit"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("type"))) {
                dataMap.put("type", request.getParameter("type"));
            }
            dataMap.put("dateformat", df);
            dataMap.put("userdateformat", userdf);
            dataMap.put("companyid", companyid);
            finaljobj = accReportsService.getDVATForm31ReportJSON(dataMap, false);
        } catch (SessionExpiredException | JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());
    }

    /*Export - DVAT Form 31*/ 
    public ModelAndView exportDVATForm31Report(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject dataMap = new JSONObject();
        String view = "jsonView_ex";
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                dataMap.put(Constants.REQ_startdate, request.getParameter("stdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                dataMap.put(Constants.REQ_enddate, request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("type"))) {
                dataMap.put("type", request.getParameter("type"));
            }
            dataMap.put("dateformat", df);
            dataMap.put("userdateformat", userdf);
            dataMap.put("companyid", companyid);
            jobj = accReportsService.getDVATForm31ReportJSON(dataMap, true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequestMVATIndia(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
     public ModelAndView getSalesPurhcaseAnnexureExport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject dataMap = new JSONObject();
        String view = "jsonView_ex";
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String isSalesAnnax = request.getParameter("isSalesAnnax");
            String code = request.getParameter("code");
            String isExport = request.getParameter("isExport");
            HashMap requestParams = new HashMap();            
            requestParams.put(Constants.df, df);
            requestParams.put("dateformat", userdf);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid =  sessionHandlerImpl.getCurrencyID(request);
            
            requestParams.put("companyid", companyid);            
            requestParams.put("currencyid", currencyid);            
            requestParams.put("isSalesAnnax", isSalesAnnax);            
            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate"))) {
                requestParams.put("startdate", request.getParameter("startdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(start)){
                requestParams.put("start", start);
            }
            if (!StringUtil.isNullOrEmpty(limit)) {
                requestParams.put("limit", limit);
            }
            if (!StringUtil.isNullOrEmpty(isSalesAnnax)) {
                requestParams.put("isSalesAnnax", isSalesAnnax);
            }
            if (!StringUtil.isNullOrEmpty(isExport)) {
                requestParams.put("isExport", isExport);
            }            
            if (!StringUtil.isNullOrEmpty(code)) {
                requestParams.put("code", code);
            }            
            jobj = getSalesPurhcaseAnnexureReportJSON(requestParams);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequestMVATIndia(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
     public JSONObject getSalesPurhcaseAnnexureReportJSON(HashMap requestParams) {
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        JSONArray dataArrGrosstot = new JSONArray();
        String msg = "", exciseUnit = "";
        String templateid = "";
        String companyid = requestParams.get("companyid").toString();
        String currencyid = requestParams.get("currencyid").toString();
        String start = "";
        String limit = "";
        int code =1;        
        
        boolean isSalesAnnax = false, isExport = false;

        if (requestParams.containsKey("isSalesAnnax")) {
            isSalesAnnax = Boolean.valueOf(requestParams.get("isSalesAnnax").toString());
        }
        if (requestParams.containsKey("code")) {
            code = IndiaComplianceConstants.MVAT_TRANSCATION_CODES.get(requestParams.get("code"));
        }
        if (requestParams.containsKey("isExport")) {
            isExport = Boolean.valueOf(requestParams.get("isExport").toString());
        }
        if (requestParams.containsKey("start")) {
            start = requestParams.get("start").toString();
        }
        if (requestParams.containsKey("limit")) {
            limit = requestParams.get("limit").toString();
        }

        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");

        try {
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("companyid", companyid);
            params.put("gcurrencyid", currency.getCurrencyID());
            int count = 1;
            if (isSalesAnnax) {
                KwlReturnObject kwl = accInvoiceDAOobj.getInvoiceDetailsBetweenDates(requestParams);
                List invoiceList = kwl.getEntityList();
                Iterator itr = invoiceList.iterator();

                while (itr.hasNext()) {
                    Invoice invoice = (Invoice) itr.next();

                    double externalcurrencyrate = 1.0;
                    if (invoice.getJournalEntry() != null) {
                        externalcurrencyrate = invoice.getJournalEntry().getExternalCurrencyRate();
                    }

                    String invoiceno = invoice.getInvoiceNumber();
                    String invoicedate = format.format(invoice.getCreatedon());
                    String tinno = code==IndiaComplianceConstants.LINELEVELTERMTYPE_VAT ? invoice.getCustomer().getVATTINnumber():invoice.getCustomer().getCSTTINnumber();
                    String isRegistered = invoice.getCustomer().getDealertype();
                    if(isRegistered.equals(IndiaComplianceConstants.UNREGISTERED_VENROR_OR_CUSTOMER)){
                        continue;
                    }
                    String action = invoice.isDeleted() ? "D" : "";
                    String transactioncode = "", transcationdesc = "";
                    boolean checkInclusivetax = invoice.isGstIncluded();
                    double vatassessable = 0.0d, vattax = 0.0d, inclusivevattax = 0.0d, valuecompo42 = 0.0d, taxfree = 0.0d, exempted41 = 0.0d, othercharges = 0.0d, grosstotal = 0.0d;
                    Set<InvoiceDetail> rows = invoice.getRows();
                    double TotalExcise = 0.0, amount = 0.0, amountRow = 0.0, rate = 0.0, totaltax = 0.0;
                    
                    HashMap<String, Object> invoiceDetailParams = new HashMap();
                    invoiceDetailParams.put("invoiceid", invoice.getID());
                    invoiceDetailParams.put("termtype",code);
                    
                    KwlReturnObject goodsReceiptMapresult = accInvoiceDAOobj.getGenricInvoicedetailTermMap(invoiceDetailParams);
                    Iterator itrObj = goodsReceiptMapresult.getEntityList().iterator();
                    while(itrObj.hasNext()){
                        Object [] ob=(Object [])itrObj.next();
                        if(!checkInclusivetax){            // No inclusive VAT OR CST invoice                    
                            vatassessable+= Double.valueOf(ob[0].toString());    // VAT/CST Assessable Value
                            vattax+= Double.valueOf(ob[1].toString());           // VAT/CST TAX Value   
                        }else{
                            inclusivevattax+=Double.valueOf(ob[0].toString())+Double.valueOf(ob[1].toString());  //inclusive VAT OR CST Value (Assessable + Tax)
                        }
                    }
                    
                    invoiceDetailParams.put("termpercentage",0);
                    goodsReceiptMapresult = accInvoiceDAOobj.getGenricInvoicedetailTermMap(invoiceDetailParams);
                    itrObj = goodsReceiptMapresult.getEntityList().iterator();
                    while(itrObj.hasNext()){                    // No  VAT OR CST nill rated check  
                        Object [] ob=(Object [])itrObj.next();
                        if(!checkInclusivetax){                // No  VAT OR CST nill rated Assessable Value .           
                            exempted41+= ob[0]!=null?Double.valueOf(ob[0].toString()):0.0f;    
                        }
                    }    
                    
                    goodsReceiptMapresult = accInvoiceDAOobj.getInvoiceTermMap(invoiceDetailParams);
                    itrObj = goodsReceiptMapresult.getEntityList().iterator();
                    while(itrObj.hasNext()){                    // OtherCharges Column
                        InvoiceTermsMap  globalTermmap=(InvoiceTermsMap)itrObj.next();
                        othercharges+=globalTermmap.getTermamount();
                    }
                    
                    for (InvoiceDetail row : rows) {
                        rate = row.getRate() / externalcurrencyrate;
                        amountRow = row.getInventory().getQuantity() * rate;
                        HashMap<String, Object> temp = new HashMap();
                        temp.put("InvoiceDetailid", row.getID());
                        temp.put("termtype", code);
                        KwlReturnObject invoiceMapresult = accInvoiceDAOobj.getInvoicedetailTermMap(temp);
                        List list = invoiceMapresult.getEntityList();
                        if (list.isEmpty()) {
                            taxfree += amountRow + row.getRowTermAmount();          
                        }
                        if (row.getSalesJED() != null && !StringUtil.isNullOrEmpty(row.getSalesJED().getID())) {
                            if (row.getSalesJED().getAccount() != null && !StringUtil.isNullOrEmpty(row.getSalesJED().getAccount().getMVATCode())) {
                                transactioncode = row.getSalesJED().getAccount().getMVATCode();
                                transcationdesc = IndiaComplianceConstants.MVAT_COA_CODES.get(Integer.parseInt(transactioncode)).toString();
                            }
                        }
                    }


                    JSONObject invoiceJsonobj = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(invoiceno)) {
                        invoiceJsonobj.put("invoicenos", invoiceno);
                    }
                    if (!StringUtil.isNullOrEmpty(invoicedate)) {
                        invoiceJsonobj.put("dosalesinvoice", invoicedate);
                    }
                    if (!StringUtil.isNullOrEmpty(tinno)) {
                        invoiceJsonobj.put("tinofPurchase", tinno);
                    }

                    invoiceJsonobj.put("srno", count++);
                    invoiceJsonobj.put("assessableamount", authHandler.formattedCommaSeparatedAmount(vatassessable, companyid));
                    invoiceJsonobj.put("taxamount", authHandler.formattedCommaSeparatedAmount(vattax, companyid));
                    invoiceJsonobj.put("valuecompo42", authHandler.formattedCommaSeparatedAmount(valuecompo42, companyid));
                    invoiceJsonobj.put("inclusivetax", authHandler.formattedCommaSeparatedAmount(inclusivevattax, companyid));
                    invoiceJsonobj.put("taxfree", authHandler.formattedCommaSeparatedAmount(taxfree, companyid));
                    invoiceJsonobj.put("exempted41", authHandler.formattedCommaSeparatedAmount(exempted41, companyid));
                    invoiceJsonobj.put("othercharges", authHandler.formattedCommaSeparatedAmount(othercharges, companyid));
                    grosstotal = authHandler.round((vatassessable + vattax + valuecompo42 + inclusivevattax + taxfree  + exempted41 + othercharges), companyid);
                    invoiceJsonobj.put("grosstotal", authHandler.formattedCommaSeparatedAmount(grosstotal, companyid));
                    invoiceJsonobj.put("action", action);
                    invoiceJsonobj.put("returnformno", "");
                    invoiceJsonobj.put("transactioncode", transactioncode);
                    invoiceJsonobj.put("transcationdesc", transcationdesc);
                    if (grosstotal > 0) {
                        dataArr.put(invoiceJsonobj);
                    }
                }
            } else {
                KwlReturnObject kwl = accInvoiceDAOobj.getInvoiceDetailsBetweenDates(requestParams);
                List goodsReceiptList = kwl.getEntityList();
                Iterator itr = goodsReceiptList.iterator();

                while (itr.hasNext()) {
                    GoodsReceipt goodsReceipt = (GoodsReceipt) itr.next();

                    double externalcurrencyrate = 1.0;
                    if (goodsReceipt.getJournalEntry() != null) {
                        externalcurrencyrate = goodsReceipt.getJournalEntry().getExternalCurrencyRate();
                    }

                    String invoiceno = goodsReceipt.getGoodsReceiptNumber();
                    String invoicedate = format.format(goodsReceipt.getCreatedon());
                    String tinno = code==IndiaComplianceConstants.LINELEVELTERMTYPE_VAT ? goodsReceipt.getVendor().getVATTINnumber():goodsReceipt.getVendor().getCSTTINnumber();
                    String isRegistered = goodsReceipt.getVendor().getDealertype();
                    if (isRegistered.equals(IndiaComplianceConstants.UNREGISTERED_VENROR_OR_CUSTOMER)) {
                        continue;
                    }
                    /*
                     * Ticket :ERP-26370
                     *Check Type of dealer Default Composition Dealer u/s 42(1) ,(2).
                     * 1. Composition Dealer u/s 42(3) ,(3A) & (4)
                       2. Composition Dealer u/s 42(1) ,(2).
                     */
//                    String typeofdealer= (!StringUtil.isNullOrEmpty(goodsReceipt.getVendor().getTypeofdealer()))?goodsReceipt.getVendor().getTypeofdealer():IndiaComplianceConstants.CompositionDealeru421;
                    
                    String action = goodsReceipt.isDeleted() ? "D" : "";
                    String transactioncode = "", transcationdesc = "";
                    boolean checkInclusivetax = goodsReceipt.isGstIncluded();
                    double vatassessable = 0.0d, vattax = 0.0d, inclusivevattax = 0.0d, valuecompo42 = 0.0d, taxfree = 0.0d, exempted41 = 0.0d, othercharges = 0.0d, grosstotal = 0.0d;
                    Set<GoodsReceiptDetail> rows = goodsReceipt.getRows();
                    double TotalExcise = 0.0, amount = 0.0, amountRow = 0.0, rate = 0.0, totaltax = 0.0;
                    
                    HashMap<String, Object> grDetailParams = new HashMap();
                    grDetailParams.put("GoodsReceiptid", goodsReceipt.getID());
                    grDetailParams.put("termtypeArry", Arrays.asList(1, 3));
                    KwlReturnObject goodsReceiptMapresult = accGoodsReceiptDAOObj.getGenricGoodsReceiptdetailTermMap(grDetailParams);
                    Iterator itrObj = goodsReceiptMapresult.getEntityList().iterator();
                    int VatCnt_normal = 0;
                    int VatCnt_inclusive = 0;
                    while(itrObj.hasNext()){
                        Object [] ob=(Object [])itrObj.next();
                         if (isRegistered.equals(IndiaComplianceConstants.CompositionDealeru423) || isRegistered.equals(IndiaComplianceConstants.REGISTERED_DEALER) || isRegistered.equals(IndiaComplianceConstants.UNREGISTERED_VENROR_OR_CUSTOMER)) {            //Check dealer type of CompositionDealeru423
                            if (!checkInclusivetax) {            // No inclusive VAT OR CST invoice                    
                                if(VatCnt_normal == 0){
                                // No need to add assessable value more than once
                                    vatassessable += Double.valueOf(ob[0].toString());    // VAT/CST Assessable Value
                                }
                                VatCnt_normal++;
                                vattax += Double.valueOf(ob[1].toString());           // VAT/CST TAX Value   
                            } else {
                                if(VatCnt_inclusive == 0){
                                    // No need to add assessable value more than once
                                    inclusivevattax += Double.valueOf(ob[1].toString());  //inclusive VAT OR CST Value (Assessable + Tax)
                                    VatCnt_inclusive++;
                                } else{
                                    inclusivevattax += Double.valueOf(ob[0].toString()) + Double.valueOf(ob[1].toString());  //inclusive VAT OR CST Value (Assessable + Tax)
                                }
                            }
                        } else {
                             if(VatCnt_normal == 0){
                                 // No need to add assessable value more than once
                                valuecompo42+=Double.valueOf(ob[0].toString())+Double.valueOf(ob[1].toString()); // VAT/CST Assessable Value + VAT/CST TAX Value 
                                VatCnt_normal++;
                             } else{
                                valuecompo42+=Double.valueOf(ob[1].toString()); // VAT/CST Assessable Value + VAT/CST TAX Value 
                             }
                         }
                    }
                    
                    grDetailParams.put("termpercentage",0);
                    goodsReceiptMapresult = accGoodsReceiptDAOObj.getGenricGoodsReceiptdetailTermMap(grDetailParams);
                    itrObj = goodsReceiptMapresult.getEntityList().iterator();
                    while(itrObj.hasNext()){                    // No  VAT OR CST nill rated check  
                        Object [] ob=(Object [])itrObj.next();
                        if(!checkInclusivetax){                // No  VAT OR CST nill rated Assessable Value .           
                            exempted41+= ob[0]!=null?Double.valueOf(ob[0].toString()):0.0f;    
                        }
                    } 
                    
                    grDetailParams.put("invoiceid",goodsReceipt.getID());
                    goodsReceiptMapresult = accGoodsReceiptDAOObj.getInvoiceTermMap(grDetailParams);
                    itrObj = goodsReceiptMapresult.getEntityList().iterator();
                    while(itrObj.hasNext()){                    // OtherCharges Column
                        ReceiptTermsMap globalTermmap=(ReceiptTermsMap)itrObj.next();
                        othercharges+=globalTermmap.getTermamount();
                    } 
                    
                    
                    
                     for (GoodsReceiptDetail row : rows) {
                         rate = row.getRate() / externalcurrencyrate;
                        amount += row.getInventory().getQuantity() * rate;
                        amountRow = row.getInventory().getQuantity() * rate;
                        HashMap<String, Object> term = new HashMap();
                        term.put("GoodsReceiptDetailid", row.getID());
                        term.put("termtype", code);
                        KwlReturnObject result = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(term);
                        List list = result.getEntityList();
                        if (list.isEmpty()) {
                            taxfree += amountRow + row.getRowTermAmount();          
                        }
                          if(row.getPurchaseJED()!=null && !StringUtil.isNullOrEmpty(row.getPurchaseJED().getID())){
                            if (row.getPurchaseJED().getAccount() != null && !StringUtil.isNullOrEmpty(row.getPurchaseJED().getAccount().getMVATCode())) {
                                transactioncode = row.getPurchaseJED().getAccount().getMVATCode();
                                transcationdesc = IndiaComplianceConstants.MVAT_COA_CODES.get(Integer.parseInt(transactioncode)).toString();
                            }
                        }
                     }

                    JSONObject invoiceJsonobj = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(invoiceno)) {
                        invoiceJsonobj.put("invoicenos", invoiceno);
                    }
                    if (!StringUtil.isNullOrEmpty(invoicedate)) {
                        invoiceJsonobj.put("dosalesinvoice", invoicedate);
                    }
                    if (!StringUtil.isNullOrEmpty(tinno)) {
                        invoiceJsonobj.put("tinofPurchase", tinno);
                    }
                    invoiceJsonobj.put("srno", count++);
                    invoiceJsonobj.put("assessableamount", authHandler.formattedCommaSeparatedAmount(vatassessable, companyid));
                    invoiceJsonobj.put("taxamount", authHandler.formattedCommaSeparatedAmount(vattax, companyid));
                    invoiceJsonobj.put("valuecompo42", authHandler.formattedCommaSeparatedAmount(valuecompo42, companyid));
                    invoiceJsonobj.put("inclusivetax", authHandler.formattedCommaSeparatedAmount(inclusivevattax, companyid));
                    invoiceJsonobj.put("taxfree", vattax == 0 ? authHandler.formattedCommaSeparatedAmount(taxfree, companyid) : 0.0);
                    invoiceJsonobj.put("exempted41", authHandler.formattedCommaSeparatedAmount(exempted41, companyid));
                    invoiceJsonobj.put("othercharges", authHandler.formattedCommaSeparatedAmount(othercharges, companyid));
                    grosstotal = authHandler.round((vatassessable + vattax + valuecompo42 + inclusivevattax + (vattax == 0 ? taxfree : 0.0) + exempted41 + othercharges), companyid);
                    invoiceJsonobj.put("grosstotal", authHandler.formattedCommaSeparatedAmount(grosstotal, companyid));
                    invoiceJsonobj.put("action", action);
                    invoiceJsonobj.put("returnformno", "");
                    invoiceJsonobj.put("transactioncode", transactioncode);
                    invoiceJsonobj.put("transcationdesc", transcationdesc);
                    if (grosstotal > 0) {
                        dataArr.put(invoiceJsonobj);
                    }

                }
            }
            
            dataArr=getPurchaseOrSalesReturn(requestParams,dataArr);
            
            int totalCount = dataArr.length();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit) && !isExport) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }

            if (dataArr.length() > 0 && isExport) {
                double grsvatassessable = 0, grsvattax = 0, grsvaluecompo42 = 0, grsinclusivevattax = 0, grstaxfree = 0, grsexempted41 = 0, grsothercharges = 0, grsgrosstotal = 0;
                DecimalFormat df = new DecimalFormat("#,###,###,##0.");
                for (int i = 0; i < dataArr.length(); i++) {
                    JSONObject jobj = dataArr.getJSONObject(i);
                    grsvatassessable += df.parse(jobj.getString("assessableamount")).doubleValue();
                    grsvattax += (Double) df.parse(jobj.getString("taxamount")).doubleValue();
                    grsvaluecompo42 += (Double) df.parse(jobj.getString("valuecompo42")).doubleValue();
                    grsinclusivevattax += (Double) df.parse(jobj.getString("inclusivetax")).doubleValue();
                    grstaxfree += (Double) df.parse(jobj.getString("taxfree")).doubleValue();
                    grsexempted41 += (Double) df.parse(jobj.getString("exempted41")).doubleValue();
                    grsothercharges += (Double) df.parse(jobj.getString("othercharges")).doubleValue();
                    grsgrosstotal += (Double) df.parse(jobj.getString("grosstotal")).doubleValue();
                }
                JSONObject grsInvoiceJsonobj = new JSONObject();
                grsInvoiceJsonobj.put("invoicenos", "");
                grsInvoiceJsonobj.put("dosalesinvoice", "");
                grsInvoiceJsonobj.put("srno", "");
                grsInvoiceJsonobj.put("tinofPurchase", "Gross Total");
                grsInvoiceJsonobj.put("assessableamount", authHandler.formattedCommaSeparatedAmount(grsvatassessable, companyid));
                grsInvoiceJsonobj.put("taxamount", authHandler.formattedCommaSeparatedAmount(grsvattax, companyid));
                grsInvoiceJsonobj.put("valuecompo42", authHandler.formattedCommaSeparatedAmount(grsvaluecompo42, companyid));
                grsInvoiceJsonobj.put("inclusivetax", authHandler.formattedCommaSeparatedAmount(grsinclusivevattax, companyid));
                grsInvoiceJsonobj.put("taxfree", authHandler.formattedCommaSeparatedAmount(grstaxfree, companyid));
                grsInvoiceJsonobj.put("exempted41", authHandler.formattedCommaSeparatedAmount(grsexempted41, companyid));
                grsInvoiceJsonobj.put("othercharges", authHandler.formattedCommaSeparatedAmount(grsothercharges, companyid));
                grsInvoiceJsonobj.put("grosstotal", authHandler.formattedCommaSeparatedAmount(grsgrosstotal, companyid));
                dataArrGrosstot.put(grsInvoiceJsonobj);
            }
            if (isExport) {
                finaljobj.put("summary", dataArrGrosstot);
            }
            finaljobj.put("data", dataArr);
            finaljobj.put("totalcount", totalCount);
            finaljobj.put("count", totalCount);
            finaljobj.put("success", true);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finaljobj;
    }
     
    public JSONArray getPurchaseOrSalesReturn(HashMap requestParams,JSONArray dataArr){
        String companyid = requestParams.get("companyid").toString();
        String currencyid = requestParams.get("currencyid").toString();
        int code =1;        
        
        boolean isSalesAnnax = false, isExport = false;

        if (requestParams.containsKey("isSalesAnnax")) {
            isSalesAnnax = Boolean.valueOf(requestParams.get("isSalesAnnax").toString());
        }
        if (requestParams.containsKey("code")) {
            code = IndiaComplianceConstants.MVAT_TRANSCATION_CODES.get(requestParams.get("code"));
        }

        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");

        try {
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("companyid", companyid);
            params.put("gcurrencyid", currency.getCurrencyID());
            int count = 1;
            if (isSalesAnnax) {
                KwlReturnObject kwl = accInvoiceDAOobj.getSalesReturn(requestParams);
                List salesReturnList = kwl.getEntityList();
                Iterator itr = salesReturnList.iterator();

                while (itr.hasNext()) {
                    Object[] oj = (Object[]) itr.next();
                    String orderid = oj[0].toString();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), orderid);
                    SalesReturn salesreturn = (SalesReturn) objItr.getEntityList().get(0);
                    double externalcurrencyrate = 1.0;
                    if (salesreturn.getInventoryJE() != null) {
                        externalcurrencyrate = salesreturn.getInventoryJE().getExternalCurrencyRate();
                    }

                    String invoiceno = salesreturn.getSalesReturnNumber();
                    String invoicedate = format.format(salesreturn.getCreatedon());
                    String tinno = code==IndiaComplianceConstants.LINELEVELTERMTYPE_VAT ? salesreturn.getCustomer().getVATTINnumber():salesreturn.getCustomer().getCSTTINnumber();
                    String isRegistered = salesreturn.getCustomer().getDealertype();
                    if(isRegistered.equals(IndiaComplianceConstants.UNREGISTERED_VENROR_OR_CUSTOMER)){
                        continue;
                    }
                    String action = salesreturn.isDeleted() ? "D" : "";
                    String transactioncode = "", transcationdesc = "";
                    boolean checkInclusivetax = false;
                    double vatassessable = 0.0d, vattax = 0.0d, inclusivevattax = 0.0d, valuecompo42 = 0.0d, taxfree = 0.0d, exempted41 = 0.0d, othercharges = 0.0d, grosstotal = 0.0d;
                    Set<SalesReturnDetail> rows = salesreturn.getRows();
                    double TotalExcise = 0.0, amount = 0.0, amountRow = 0.0, rate = 0.0, totaltax = 0.0;
                    
                    HashMap<String, Object> invoiceDetailParams = new HashMap();
                    invoiceDetailParams.put("SalesReturnid", salesreturn.getID());
                    invoiceDetailParams.put("termtype",code);
                    
                    KwlReturnObject goodsReceiptMapresult = accInvoiceDAOobj.getGenricSalesReturndetailTermMap(invoiceDetailParams);
                    Iterator itrObj = goodsReceiptMapresult.getEntityList().iterator();
                    while(itrObj.hasNext()){
                        Object [] ob=(Object [])itrObj.next();
                        if(!checkInclusivetax){            // No inclusive VAT OR CST invoice                    
                            vatassessable+= -(Double.valueOf(ob[0].toString()));    // VAT/CST Assessable Value
                            vattax+= -(Double.valueOf(ob[1].toString()));           // VAT/CST TAX Value   
                        }else{
                            inclusivevattax+=Double.valueOf(ob[0].toString())+Double.valueOf(ob[1].toString());  //inclusive VAT OR CST Value (Assessable + Tax)
                        }
                    }
                    
                    invoiceDetailParams.put("termpercentage",0);
                    goodsReceiptMapresult = accInvoiceDAOobj.getGenricSalesReturndetailTermMap(invoiceDetailParams);
                    itrObj = goodsReceiptMapresult.getEntityList().iterator();
                    while(itrObj.hasNext()){                    // No  VAT OR CST nill rated check  
                        Object [] ob=(Object [])itrObj.next();
                        if(!checkInclusivetax){                // No  VAT OR CST nill rated Assessable Value .           
                            exempted41+= ob[0]!=null?-(Double.valueOf(ob[0].toString())):0.0f;    
                        }
                    }    
                    
//                    goodsReceiptMapresult = accInvoiceDAOobj.getInvoiceTermMap(invoiceDetailParams);
//                    itrObj = goodsReceiptMapresult.getEntityList().iterator();
//                    while(itrObj.hasNext()){                    // OtherCharges Column
//                        InvoiceTermsMap  globalTermmap=(InvoiceTermsMap)itrObj.next();
//                        othercharges+=globalTermmap.getTermamount();
//                    }
                    
                    for (SalesReturnDetail row : rows) {
                        rate = row.getRate() / externalcurrencyrate;
                        amountRow = row.getInventory().getQuantity() * rate;
                        HashMap<String, Object> temp = new HashMap();
                        temp.put("salesReturnDetailid", row.getID());
                        temp.put("termtype", code);
                        KwlReturnObject salesReturnMapresult = accInvoiceDAOobj.getSalesReturnDetailTermMap(temp);
                        List list = salesReturnMapresult.getEntityList();
                        if (list.isEmpty()) {
                            taxfree += amountRow ;          
                        }
                        if (!StringUtil.isNullOrEmpty(salesreturn.getMvatTransactionNo())) {
                                transactioncode = salesreturn.getMvatTransactionNo();
                                if(IndiaComplianceConstants.MVAT_COA_CODES_SALES_RETURN.containsKey(Integer.parseInt(transactioncode))){
                                transcationdesc = IndiaComplianceConstants.MVAT_COA_CODES_SALES_RETURN.get(Integer.parseInt(transactioncode)).toString();
                                }else if(IndiaComplianceConstants.MVAT_COA_CODES_CREDIT_NOTE.containsKey(Integer.parseInt(transactioncode))){
                                    transcationdesc = IndiaComplianceConstants.MVAT_COA_CODES_CREDIT_NOTE.get(Integer.parseInt(transactioncode)).toString();
                                }
                        }
                    }


                    JSONObject invoiceJsonobj = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(invoiceno)) {
                        invoiceJsonobj.put("invoicenos", invoiceno);
                    }
                    if (!StringUtil.isNullOrEmpty(invoicedate)) {
                        invoiceJsonobj.put("dosalesinvoice", invoicedate);
                    }
                    if (!StringUtil.isNullOrEmpty(tinno)) {
                        invoiceJsonobj.put("tinofPurchase", tinno);
                    }

                    invoiceJsonobj.put("srno", count++);
                    invoiceJsonobj.put("assessableamount", authHandler.formattedCommaSeparatedAmount(vatassessable, companyid));
                    invoiceJsonobj.put("taxamount", authHandler.formattedCommaSeparatedAmount(vattax, companyid));
                    invoiceJsonobj.put("valuecompo42", authHandler.formattedCommaSeparatedAmount(valuecompo42, companyid));
                    invoiceJsonobj.put("inclusivetax", authHandler.formattedCommaSeparatedAmount(inclusivevattax, companyid));
                    invoiceJsonobj.put("taxfree", authHandler.formattedCommaSeparatedAmount(taxfree, companyid));
                    invoiceJsonobj.put("exempted41", authHandler.formattedCommaSeparatedAmount(exempted41, companyid));
                    invoiceJsonobj.put("othercharges", authHandler.formattedCommaSeparatedAmount(othercharges, companyid));
                    grosstotal = authHandler.round((vatassessable + vattax + valuecompo42 + inclusivevattax + taxfree  + exempted41 + othercharges), companyid);
                    invoiceJsonobj.put("grosstotal", authHandler.formattedCommaSeparatedAmount(grosstotal, companyid));
                    invoiceJsonobj.put("action", action);
                    invoiceJsonobj.put("returnformno", "");
                    invoiceJsonobj.put("transactioncode", transactioncode);
                    invoiceJsonobj.put("transcationdesc", transcationdesc);
                    if (grosstotal > 0) {
                        dataArr.put(invoiceJsonobj);
                    }
                }
            } else {
                KwlReturnObject kwl = accGoodsReceiptDAOObj.getPurchaseReturn(requestParams);
                List purchaseReturnList = kwl.getEntityList();
                Iterator itr = purchaseReturnList.iterator();

                while (itr.hasNext()) {
                    Object[] oj = (Object[]) itr.next();
                    String orderid = oj[0].toString();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseReturn.class.getName(), orderid);
                    PurchaseReturn purchasereturn = (PurchaseReturn) objItr.getEntityList().get(0);

                    double externalcurrencyrate = 1.0;
                    if (purchasereturn.getInventoryJE() != null) {
                        externalcurrencyrate = purchasereturn.getInventoryJE().getExternalCurrencyRate();
                    }

                    String invoiceno = purchasereturn.getPurchaseReturnNumber();
                    String invoicedate = format.format(purchasereturn.getCreatedon());
                    String tinno = code==IndiaComplianceConstants.LINELEVELTERMTYPE_VAT ? purchasereturn.getVendor().getVATTINnumber():purchasereturn.getVendor().getCSTTINnumber();
                    String isRegistered = purchasereturn.getVendor().getDealertype();
                    if (isRegistered.equals(IndiaComplianceConstants.UNREGISTERED_VENROR_OR_CUSTOMER)) {
                        continue;
                    }
                    /*
                     * Ticket :ERP-26370
                     *Check Type of dealer Default Composition Dealer u/s 42(1) ,(2).
                     * 1. Composition Dealer u/s 42(3) ,(3A) & (4)
                       2. Composition Dealer u/s 42(1) ,(2).
                     */
//                    String typeofdealer= (!StringUtil.isNullOrEmpty(goodsReceipt.getVendor().getTypeofdealer()))?goodsReceipt.getVendor().getTypeofdealer():IndiaComplianceConstants.CompositionDealeru421;
                    
                    String action = purchasereturn.isDeleted() ? "D" : "";
                    String transactioncode = "", transcationdesc = "";
                    boolean checkInclusivetax = false;
                    double vatassessable = 0.0d, vattax = 0.0d, inclusivevattax = 0.0d, valuecompo42 = 0.0d, taxfree = 0.0d, exempted41 = 0.0d, othercharges = 0.0d, grosstotal = 0.0d;
                    Set<PurchaseReturnDetail> rows = purchasereturn.getRows();
                    double TotalExcise = 0.0, amount = 0.0, amountRow = 0.0, rate = 0.0, totaltax = 0.0;
                    
                    HashMap<String, Object> prDetailParams = new HashMap();
                    prDetailParams.put("PurchaseReturnid", purchasereturn.getID());
                    prDetailParams.put("termtypeArry", Arrays.asList(1, 3));
                    KwlReturnObject purchaseReturnMapresult = accGoodsReceiptDAOObj.getGenricPurchaseReturndetailTermMap(prDetailParams);
                    Iterator itrObj = purchaseReturnMapresult.getEntityList().iterator();
                    int VatCnt_normal = 0;
                    int VatCnt_inclusive = 0;
                    while(itrObj.hasNext()){
                        Object [] ob=(Object [])itrObj.next();
                         if (isRegistered.equals(IndiaComplianceConstants.CompositionDealeru423) || isRegistered.equals(IndiaComplianceConstants.REGISTERED_DEALER) || isRegistered.equals(IndiaComplianceConstants.UNREGISTERED_VENROR_OR_CUSTOMER)) {            //Check dealer type of CompositionDealeru423
                            if (!checkInclusivetax) {            // No inclusive VAT OR CST invoice                    
                                if(VatCnt_normal == 0){
                                // No need to add assessable value more than once
                                    vatassessable += -(Double.valueOf(ob[0].toString()));    // VAT/CST Assessable Value
                                    
                                }
                                VatCnt_normal++;
                                vattax += -(Double.valueOf(ob[1].toString()));           // VAT/CST TAX Value   
                            } else {
                                if(VatCnt_inclusive == 0){
                                    // No need to add assessable value more than once
                                    inclusivevattax += Double.valueOf(ob[1].toString());  //inclusive VAT OR CST Value (Assessable + Tax)
                                    VatCnt_inclusive++;
                                } else{
                                    inclusivevattax += Double.valueOf(ob[0].toString()) + Double.valueOf(ob[1].toString());  //inclusive VAT OR CST Value (Assessable + Tax)
                                }
                            }
                        } else {
                             if(VatCnt_normal == 0){
                                 // No need to add assessable value more than once
                                valuecompo42+=Double.valueOf(ob[0].toString())+Double.valueOf(ob[1].toString()); // VAT/CST Assessable Value + VAT/CST TAX Value 
                                VatCnt_normal++;
                             } else{
                                valuecompo42+=Double.valueOf(ob[1].toString()); // VAT/CST Assessable Value + VAT/CST TAX Value 
                             }
                         }
                    }
                    
                    prDetailParams.put("termpercentage",0);
                    purchaseReturnMapresult = accGoodsReceiptDAOObj.getGenricPurchaseReturndetailTermMap(prDetailParams);
                    itrObj = purchaseReturnMapresult.getEntityList().iterator();
                    while(itrObj.hasNext()){                    // No  VAT OR CST nill rated check  
                        Object [] ob=(Object [])itrObj.next();
                        if(!checkInclusivetax){                // No  VAT OR CST nill rated Assessable Value .           
                            exempted41+= ob[0]!=null?Double.valueOf(ob[0].toString()):0.0f;    
                        }
                    } 
                    
//                    prDetailParams.put("invoiceid",purchasereturn.getID());
//                    goodsReceiptMapresult = accGoodsReceiptDAOObj.getInvoiceTermMap(prDetailParams);
//                    itrObj = goodsReceiptMapresult.getEntityList().iterator();
//                    while(itrObj.hasNext()){                    // OtherCharges Column
//                        ReceiptTermsMap globalTermmap=(ReceiptTermsMap)itrObj.next();
//                        othercharges+=globalTermmap.getTermamount();
//                    } 
                    
                    
                    
                     for (PurchaseReturnDetail row : rows) {
                         rate = row.getRate() / externalcurrencyrate;
                        amount += row.getInventory().getQuantity() * rate;
                        amountRow = row.getInventory().getQuantity() * rate;
                        HashMap<String, Object> term = new HashMap();
                        term.put("PurchaseReturnDetailid", row.getID());
                        term.put("termtype", code);
                        KwlReturnObject result = accGoodsReceiptDAOObj.getPurchaseReturnDetailTermMap(term);
                        List list = result.getEntityList();
                        if (list.isEmpty()) {
                            taxfree += amountRow;          
                        }
                          if(!StringUtil.isNullOrEmpty(purchasereturn.getMvatTransactionNo())){
                                transactioncode = purchasereturn.getMvatTransactionNo();
                                if(IndiaComplianceConstants.MVAT_COA_CODES_PURCHASE_RETURN.containsKey(Integer.parseInt(transactioncode))){
                                   transcationdesc = IndiaComplianceConstants.MVAT_COA_CODES_PURCHASE_RETURN.get(Integer.parseInt(transactioncode)).toString(); 
                                }else if(IndiaComplianceConstants.MVAT_COA_CODES_DEBIT_NOTE.containsKey(Integer.parseInt(transactioncode))){
                                   transcationdesc = IndiaComplianceConstants.MVAT_COA_CODES_DEBIT_NOTE.get(Integer.parseInt(transactioncode)).toString();  
                                }
                            }
                     }

                    JSONObject invoiceJsonobj = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(invoiceno)) {
                        invoiceJsonobj.put("invoicenos", invoiceno);
                    }
                    if (!StringUtil.isNullOrEmpty(invoicedate)) {
                        invoiceJsonobj.put("dosalesinvoice", invoicedate);
                    }
                    if (!StringUtil.isNullOrEmpty(tinno)) {
                        invoiceJsonobj.put("tinofPurchase", tinno);
                    }
                    invoiceJsonobj.put("srno", count++);
                    invoiceJsonobj.put("assessableamount", authHandler.formattedCommaSeparatedAmount(vatassessable, companyid));
                    invoiceJsonobj.put("taxamount", authHandler.formattedCommaSeparatedAmount(vattax, companyid));
                    invoiceJsonobj.put("valuecompo42", authHandler.formattedCommaSeparatedAmount(valuecompo42, companyid));
                    invoiceJsonobj.put("inclusivetax", authHandler.formattedCommaSeparatedAmount(inclusivevattax, companyid));
                    invoiceJsonobj.put("taxfree", vattax == 0 ? authHandler.formattedCommaSeparatedAmount(taxfree, companyid) : 0.0);
                    invoiceJsonobj.put("exempted41", authHandler.formattedCommaSeparatedAmount(exempted41, companyid));
                    invoiceJsonobj.put("othercharges", authHandler.formattedCommaSeparatedAmount(othercharges, companyid));
                    grosstotal = authHandler.round((vatassessable + vattax + valuecompo42 + inclusivevattax + (vattax == 0 ? taxfree : 0.0) + exempted41 + othercharges), companyid);
                    invoiceJsonobj.put("grosstotal", authHandler.formattedCommaSeparatedAmount(grosstotal, companyid));
                    invoiceJsonobj.put("action", action);
                    invoiceJsonobj.put("returnformno", "");
                    invoiceJsonobj.put("transactioncode", transactioncode);
                    invoiceJsonobj.put("transcationdesc", transcationdesc);
                    if (grosstotal > 0) {
                        dataArr.put(invoiceJsonobj);
                    }

                }
            }
        }catch(Exception ex){
            
        }
        
        return dataArr;
    } 
     public ModelAndView getCSTForm6Export(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String isSalesAnnax = request.getParameter("isSalesAnnax");
            String isExport = request.getParameter("isExport");
            HashMap requestParams = new HashMap();            
            requestParams.put(Constants.df, df);
            requestParams.put("dateformat", userdf);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid =  sessionHandlerImpl.getCurrencyID(request);
            
            requestParams.put("companyid", companyid);            
            requestParams.put("currencyid", currencyid);            
            requestParams.put("isSalesAnnax", isSalesAnnax);            
            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate"))) {
                requestParams.put(Constants.REQ_startdate, request.getParameter("startdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(start)){
                requestParams.put(Constants.start, start);
            }
            if (!StringUtil.isNullOrEmpty(limit)) {
                requestParams.put(Constants.limit, limit);
            }
            if (!StringUtil.isNullOrEmpty(isExport)) {
                requestParams.put("isExport", isExport);
            }            
            jobj = getCSTForm6ReportJSON(requestParams); 
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequestMVATIndia(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
     public JSONObject getCSTForm6ReportJSON(HashMap requestParams) {
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        String companyid = requestParams.get("companyid").toString();
        String currencyid = requestParams.get("currencyid").toString();
        String  startdate= "", enddate = "";

        if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null ) {
            startdate = requestParams.get(Constants.REQ_startdate).toString();
        }
        if (requestParams.containsKey(Constants.REQ_enddate) && requestParams.get(Constants.REQ_enddate) != null ) {
            enddate = requestParams.get(Constants.REQ_enddate).toString();
        }

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("companyid", companyid);
            params.put("gromoduleid", Constants.Acc_Goods_Receipt_ModuleId);
            params.put(Constants.df, df);
            params.put(Constants.REQ_startdate, startdate);
            params.put(Constants.REQ_enddate, enddate);
            params.put("sourceflag", Constants.LINK_SOURCE_FLAG_0);
            params.put("invoicemoduleid", Constants.Acc_Vendor_Invoice_ModuleId);
            params.put("gcurrencyid", currency.getCurrencyID());
            KwlReturnObject kwl = accGoodsReceiptDAOObj.getGR_Crosslinked_PI(params);
            List invoiceList = kwl.getEntityList();
            Iterator itr = invoiceList.iterator();
            int formtype = 0;

            while (itr.hasNext()) {
                Object[] dataObject = (Object[]) itr.next();

                String transactionType = (String) (dataObject[0]!= null ? dataObject[0]: "");
                String invoiceid = (String) (dataObject[1] != null?dataObject[1]:"");
                String groid = (String) (dataObject[2] != null?dataObject[2]:"");
                Date jedate = (Date) (dataObject[3] != null?dataObject[3]:null);
                
                KwlReturnObject purchase_invoice_obj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceid);
                GoodsReceipt purchase_invoice = (GoodsReceipt) purchase_invoice_obj.getEntityList().get(0);
                
                KwlReturnObject grn_obj = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), groid);
                GoodsReceiptOrder goodsReceipt = (GoodsReceiptOrder) grn_obj.getEntityList().get(0);
                
                formtype = (!StringUtil.isNullOrEmpty(purchase_invoice.getFormtype()))?Integer.parseInt(purchase_invoice.getFormtype()):0;
                int vendorbranch = 0;
                if(purchase_invoice.getVendor() != null && !StringUtil.isNullOrEmpty(purchase_invoice.getVendor().getVendorBranch())){
                    vendorbranch = Integer.parseInt(purchase_invoice.getVendor().getVendorBranch());
                }
                String invoiceNumber = "";
                if( formtype == IndiaComplianceConstants.INVOICE_F_FORM ){
                    invoiceNumber = (formtype == IndiaComplianceConstants.INVOICE_F_FORM && !StringUtil.isNullOrEmpty(purchase_invoice.getFormno()))?purchase_invoice.getFormno():"";
                }
                if(purchase_invoice.getVendor().getDealertype().equals(IndiaComplianceConstants.REGISTERED_DEALER)){
                    
                    String state = "";
    //                    VendorAddressDetails VendorAddressDetails = null;
                    String date = jedate != null?format.format(jedate):"";
                    String vendorName =(purchase_invoice.getVendor() != null)? purchase_invoice.getVendor().getName():"";
                    String shipVia = !StringUtil.isNullOrEmpty(goodsReceipt.getShipvia())?goodsReceipt.getShipvia():"";
                    String remarks = !StringUtil.isNullOrEmpty(purchase_invoice.getMemo())?purchase_invoice.getMemo():"";

                    HashMap<String, Object> addressParams = new HashMap<String, Object>();
                    addressParams.put("isBillingAddress", true);
                    addressParams.put("vendorid", purchase_invoice.getVendor().getID());
                    KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addressParams);
                    if (addressResult.getEntityList() != null && !addressResult.getEntityList().isEmpty()) {
                        VendorAddressDetails VendorAddressDetails = (VendorAddressDetails) addressResult.getEntityList().get(0);
                        state = StringUtil.isNullOrEmpty(VendorAddressDetails.getState()) ? "" : VendorAddressDetails.getState();
                    }

                    Set<GoodsReceiptDetail> rows = purchase_invoice.getRows();
                    for( GoodsReceiptDetail row : rows){
                        JSONObject invoiceJsonobj = new JSONObject();
                        invoiceJsonobj.put("date",date);
                        invoiceJsonobj.put("name",vendorName);
                        invoiceJsonobj.put("state",state);
                        invoiceJsonobj.put("isbranch",(vendorbranch != 0)?"Yes":"No");
                        invoiceJsonobj.put("regno1",goodsReceipt.getVendor().getCSTTINnumber());
                        invoiceJsonobj.put("regno2",goodsReceipt.getVendor().getVATTINnumber());
                        invoiceJsonobj.put("desc",row.getInventory().getProduct().getDescription());
                        invoiceJsonobj.put("quantity",row.getInventory().getQuantity());
                        invoiceJsonobj.put("ship",shipVia);
                        invoiceJsonobj.put("challan",goodsReceipt.getChallanNumber());
                        invoiceJsonobj.put("invoice",invoiceNumber);
                        invoiceJsonobj.put("remarks",remarks);
                        dataArr.put(invoiceJsonobj);
                    }
                }
            }
            
            int totalCount = dataArr.length();

            finaljobj.put("summary", new JSONArray());
            finaljobj.put("data", dataArr);
            finaljobj.put("totalcount", totalCount);
            finaljobj.put("count", totalCount);
            finaljobj.put("success", true);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finaljobj;
    }
     
     public ModelAndView getVatReportsFomr201AExcelExport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject finaljobj = new JSONObject();
        JSONArray jarr = new  JSONArray();
        Map dataMap = new HashMap();
        Map map = new HashMap();
        String view = "jsonView_ex";
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            SimpleDateFormat gdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat formatDateXls = new SimpleDateFormat("d-MMM-yyyy");
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String isExport = request.getParameter("isExport");
            HashMap requestParams = new HashMap();            
            requestParams.put(Constants.df, df);
            requestParams.put("dateformat", userdf);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid =  sessionHandlerImpl.getCurrencyID(request);
            
            boolean isForm202 = false;
            boolean isform201B = false;
            boolean isform201C = false;
            int year = 0;
            String startdate="", enddate="", yearStr="";
            
            requestParams.put("companyid", companyid);            
            requestParams.put("currencyid", currencyid);            
            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate"))) {
                requestParams.put("startdate", request.getParameter("startdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            
            if (!StringUtil.isNullOrEmpty(start)){
                requestParams.put("start", start);
            }
            if (!StringUtil.isNullOrEmpty(limit)) {
                requestParams.put("limit", limit);
            }
            if (!StringUtil.isNullOrEmpty(isExport)) {
                requestParams.put("isExport", isExport);
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isform202"))){
                isForm202 = Boolean.parseBoolean(request.getParameter("isform202"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isform201B"))){
                isform201B = Boolean.parseBoolean(request.getParameter("isform201B"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isform201C"))){
                isform201C = Boolean.parseBoolean(request.getParameter("isform201C"));
            }
            
            if (!isForm202) {
                int frequency = (!StringUtil.isNullOrEmpty(request.getParameter("frequency"))) ? Integer.parseInt(request.getParameter("frequency").toString()) : 0;
                String period = (!StringUtil.isNullOrEmpty(request.getParameter("period"))) ? request.getParameter("period").toString() : "";
                year = Integer.parseInt((!StringUtil.isNullOrEmpty(request.getParameter("year"))) ? request.getParameter("year").toString() : "0");
                Date nextYear = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
                c.add(Calendar.YEAR, +1);
                nextYear.setTime(c.getTime().getTime());
                yearStr = year + "-" +(year+1);
                if (frequency == 0) {
                    startdate = year + "-" + "04" + "-" + "01";
                    enddate = (year + 1) + "-" + "03" + "-" + "31";
                } else if (!StringUtil.isNullOrEmpty(request.getParameter("frequency"))) {  // get start date and end date

                    if (request.getParameter("frequency").equals("1")) { //Quaterly
                        int firstMonth = 0;
                        int lastMonth = 11;
                        String qq = "";
                        if (request.getParameter("period").equals("quater4")) { //1st January 2017 to 31st Mar 2017 (Quarter IV)
                            firstMonth = 0;
                            lastMonth = 2;
                            qq = "44";
                            year += 1;
                        } else if (request.getParameter("period").equals("quater1")) {  //1st April 2016 to 30th June 2016 (Quarter I)
                            firstMonth = 3;
                            lastMonth = 5;
                            qq = "41";
                        } else if (request.getParameter("period").equals("quater2")) {  //1st July 2016 to 30th September 2016 (Quarter II)
                            firstMonth = 6;
                            lastMonth = 8;
                            qq = "42";
                        } else if (request.getParameter("period").equals("quater3")) { //1st October 2016 to 31st December 2016 (Quarter III)
                            firstMonth = 9;
                            lastMonth = 11;
                            qq = "43";
                        }
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, firstMonth);
                        calendar.set(Calendar.DATE, 1);
                        Date firstDate = calendar.getTime();
                        startdate = gdf.format(firstDate);

                        calendar.set(Calendar.MONTH, lastMonth);
                        int TotalDaysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                        calendar.set(Calendar.DATE, TotalDaysOfMonth);
                        Date lastDate = calendar.getTime();
                        enddate = gdf.format(lastDate);


                    } else { // Monthly

                        Calendar calendar = Calendar.getInstance();
                        int month = Integer.parseInt(request.getParameter("period"));
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DATE, 1);
                        Date firstDate = calendar.getTime();
                        startdate = gdf.format(firstDate);

                        int TotalDaysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                        calendar.set(Calendar.DATE, TotalDaysOfMonth);
                        Date lastDate = calendar.getTime();
                        enddate = gdf.format(lastDate);

                    }
                }
            }
            if (!StringUtil.isNullOrEmpty(startdate)) {
                requestParams.put("startdate", startdate);
            }
            if (!StringUtil.isNullOrEmpty(enddate)) {
                requestParams.put("enddate", enddate);
            }
            if(isform201B){
                requestParams.put("moduleid",Constants.Acc_Goods_Receipt_ModuleId);
            } else{
                requestParams.put("moduleid",Constants.Acc_Invoice_ModuleId);
            }
            if(!isform201C){
                map = accExportReportsServiceDAO.exportForm201AJson(requestParams);
                if(map.containsKey("excelData") && map.get("excelData")!= null && !StringUtil.isNullOrEmpty(map.get("excelData").toString())){
                    jarr = (JSONArray) map.get("excelData");
                }
              jobj.put("data",jarr);
            } else{
              jobj =  accReportsService.getForm201CExcelJSON(requestParams,false);
            }
            
            jobj.put("period",formatDateXls.format(gdf.parse(startdate))+" to "+formatDateXls.format(gdf.parse(enddate)));
            jobj.put("year",yearStr);
            jobj.put("success", true);
            
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {                                                                                                                                           
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequestMVATIndia(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
     public JSONObject getVATAndCSTCalculationReportJsonMerged(HashMap<String, Object> requestParams) throws ServiceException {
        
        JSONObject finaljobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        JSONArray dataArrGrosstot = new JSONArray();
        String msg = "", exciseUnit = "";
        String templateid = "";
        String companyid = requestParams.get("companyid").toString();
        String currencyid = requestParams.get("currencyid").toString();
        String start = "";
        String limit = "";
        DateFormat df = (DateFormat)requestParams.get(Constants.df);
        DateFormat userdf = (DateFormat)requestParams.get(Constants.userdf);
        boolean isSalesAnnax = false, isExport = false;

        if (requestParams.containsKey("isSalesAnnax")) {
            isSalesAnnax = Boolean.valueOf(requestParams.get("isSalesAnnax").toString());
        }
        if (requestParams.containsKey("isExport")) {
            isExport = Boolean.valueOf(requestParams.get("isExport").toString());
        }
        if (requestParams.containsKey("start")) {
            start = requestParams.get("start").toString();
        }
        if (requestParams.containsKey("limit")) {
            limit = requestParams.get("limit").toString();
        }

        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");

        try {
            KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String) requestParams.get(Constants.companyKey));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            String cashAccount = pref.getCashAccount().getID();
            
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("companyid", companyid);
            params.put("gcurrencyid", currency.getCurrencyID());
            int count = 1;
            KwlReturnObject kwl = accInvoiceDAOobj.getJEntryBetweenDates(requestParams);
            List listJe =kwl.getEntityList();
            Iterator<JournalEntry> itr = listJe.iterator();
            outerloop:
            while (itr.hasNext()) {                
                JournalEntry jeObj = (JournalEntry) itr.next();
                int transctionModule = jeObj.getTransactionModuleid();
                if ((Constants.Acc_Cash_Purchase_ModuleId == transctionModule || Constants.Acc_Vendor_Invoice_ModuleId == transctionModule || Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId == transctionModule) && !isSalesAnnax) {
                    KwlReturnObject KwlgoodsReceipt = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jeObj.getTransactionId());
                    GoodsReceipt goodsReceipt = (GoodsReceipt) KwlgoodsReceipt.getEntityList().get(0);
                    if (goodsReceipt != null) {
                        String vendorname = goodsReceipt.getVendor().getName();
                        String vendorvattin = goodsReceipt.getVendor().getVATTINnumber();
                        String vendorcsttin = goodsReceipt.getVendor().getCSTTINnumber();
                        boolean cashPurchaseCheck = (goodsReceipt.getVendorEntry() != null && goodsReceipt.getVendorEntry().getAccount().getID().equals(cashAccount));
                        HashMap<String, Object> grDetailParams = new HashMap();
                        grDetailParams.put("GoodsReceiptid", goodsReceipt.getID());
                        grDetailParams.put("termtypeArry", Arrays.asList(1, 3));
                        KwlReturnObject goodsReceiptMapresult = accGoodsReceiptDAOObj.getGenricGoodsReceiptdetailTermMap(grDetailParams);
                        Iterator itrObj = goodsReceiptMapresult.getEntityList().iterator();
                        while (itrObj.hasNext()) {
                            JSONObject obj = new JSONObject();
                            obj.put("entrydate", userdf.format(jeObj.getEntryDate()));
                            obj.put("entryno", jeObj.getEntryNumber());
                            obj.put("partyname", vendorname);
                            obj.put("vattin", vendorvattin);
                            obj.put("csttin", vendorcsttin);
                            obj.put("transactionno", goodsReceipt.getGoodsReceiptNumber());
                            obj.put("transactionDetails", Constants.CASH_PURCHASE + ", " + goodsReceipt.getVendor().getName());
                            boolean fixedassetinvoiceflag = goodsReceipt.isFixedAssetInvoice();
                            obj.put("transcationtype", cashPurchaseCheck ? Constants.CASH_PURCHASE : ((fixedassetinvoiceflag == true) ? Constants.ACQUIRED_INVOICE : Constants.VENDOR_INVOICE));
                            Object[] oj = (Object[]) itrObj.next();
                            obj.put("assessablevalue", oj[0]);
                            if ((int) oj[3] == 3) {
                                obj.put("cstamt", oj[1]);
                            } else {
                                obj.put("vatamt", oj[1]);
                            }
                            Account acct = (Account) oj[2];
                            obj.put("taxaccount", acct.getAccountName());
                            dataArr.put(obj);
                        }
                    }
                } else if(Constants.Acc_Debit_Note_ModuleId == transctionModule && !isSalesAnnax){
                    KwlReturnObject Kwldebitnote = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), jeObj.getTransactionId());
                    DebitNote debitnote = (DebitNote) Kwldebitnote.getEntityList().get(0);
                    if (debitnote != null && debitnote.getPurchaseReturn() != null) {
                        KwlReturnObject Kwlpurchasereturndebitnote = accountingHandlerDAOobj.getObject(PurchaseReturn.class.getName(), debitnote.getPurchaseReturn().getID());
                        PurchaseReturn purchasereturn = (PurchaseReturn) Kwlpurchasereturndebitnote.getEntityList().get(0);
                        if(purchasereturn != null){
                            String goodsReceiptId = "";
                            Set<PurchaseReturnDetail> rows = purchasereturn.getRows();
                            for(PurchaseReturnDetail prdetail:rows){
                                if(prdetail.getVidetails()!=null && prdetail.getVidetails().getGoodsReceipt()!=null){
                                    goodsReceiptId = prdetail.getVidetails().getGoodsReceipt().getID();
                                    break;
                                }
                            }
                            if(!StringUtil.isNullOrEmpty(goodsReceiptId)){
                                KwlReturnObject KwlgoodsReceipt = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), goodsReceiptId);
                                GoodsReceipt goodsReceipt = (GoodsReceipt) KwlgoodsReceipt.getEntityList().get(0);
                                if(goodsReceipt != null){
                                    HashMap<String,Object> data = new HashMap<String,Object>();
                                    data.put("invoiceid",goodsReceipt.getID());
                                    data.put("companyid",companyid);
                                    boolean isExciseApplicable = accGoodsReceiptDAOObj.isTaxApplied(data, IndiaComplianceConstants.LINELEVELTERMTYPE_Excise_DUTY);
                                    if(!isExciseApplicable){
                                        String vendorname = goodsReceipt.getVendor().getName();
                                        String vendorvattin = goodsReceipt.getVendor().getVATTINnumber();
                                        String vendorcsttin = goodsReceipt.getVendor().getCSTTINnumber();
                                        HashMap<String, Object> prDetailParams = new HashMap();
                                        prDetailParams.put("PurchaseReturnid", purchasereturn.getID());
                                        prDetailParams.put("termtypeArry", Arrays.asList(1, 3));
                                        KwlReturnObject goodsReceiptMapresult = accGoodsReceiptDAOObj.getGenricPurchaseReturndetailTermMap(prDetailParams);
                                        Iterator itrObj = goodsReceiptMapresult.getEntityList().iterator();
                                        while (itrObj.hasNext()) {
                                            JSONObject obj = new JSONObject();
                                            obj.put("entrydate", userdf.format(jeObj.getEntryDate()));
                                            obj.put("entryno", jeObj.getEntryNumber());
                                            obj.put("partyname", vendorname);
                                            obj.put("vattin", vendorvattin);
                                            obj.put("csttin", vendorcsttin);
                                            obj.put("transactionno", debitnote.getDebitNoteNumber());
                                            obj.put("transactionDetails", Constants.DEBIT_NOTE + ", " + goodsReceipt.getVendor().getName());
                                            obj.put("transcationtype", Constants.DEBIT_NOTE);
                                            Object[] oj = (Object[]) itrObj.next();
                                            obj.put("assessablevalue", oj[0]);
                                            if ((int) oj[3] == 3) {
                                                obj.put("cstamt", oj[1]);
                                            } else {
                                                obj.put("vatamt", oj[1]);
                                            }
                                            Account acct = (Account) oj[2];
                                            obj.put("taxaccount", acct.getAccountName());
                                            dataArr.put(obj);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if ((Constants.Acc_Cash_Sales_ModuleId == transctionModule || Constants.Acc_Invoice_ModuleId == transctionModule || Constants.Acc_FixedAssets_DisposalInvoice_ModuleId == transctionModule || Constants.LEASE_INVOICE_MODULEID == transctionModule) && isSalesAnnax) {
                    KwlReturnObject KwlgoodsReceipt = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jeObj.getTransactionId());
                    Invoice invoice = (Invoice) KwlgoodsReceipt.getEntityList().get(0);
                    if (invoice != null) {
                        String vendorname = invoice.getCustomer().getName();
                        String vendorvattin = invoice.getCustomer().getVATTINnumber();
                        String vendorcsttin = invoice.getCustomer().getCSTTINnumber();
                        boolean cashSales = (invoice.getCustomerEntry() != null && invoice.getCustomerEntry().getAccount().getID().equals(cashAccount));
                        HashMap<String, Object> grDetailParams = new HashMap();
                        grDetailParams.put("invoiceid", invoice.getID());
                        grDetailParams.put("termtypeArry", Arrays.asList(1, 3));
                        KwlReturnObject invoiceTermMapResult = accInvoiceDAOobj.getGenricInvoicedetailTermMap(grDetailParams);
                        Iterator itrObj = invoiceTermMapResult.getEntityList().iterator();
                        while (itrObj.hasNext()) {
                            JSONObject obj = new JSONObject();
                            Object[] oj = (Object[]) itrObj.next();
                            obj.put("entrydate", userdf.format(jeObj.getEntryDate()));
                            obj.put("entryno", jeObj.getEntryNumber());
                            obj.put("partyname", vendorname);
                            obj.put("vattin", vendorvattin);
                            obj.put("csttin", vendorcsttin);
                            obj.put("transactionno", invoice.getInvoiceNumber());
                            obj.put("transactionDetails", Constants.CASH_SALE + ", " + invoice.getCustomer().getName());
                            obj.put("transcationtype", cashSales ? Constants.CASH_SALE : Constants.CUSTOMER_INVOICE);
                            obj.put("assessablevalue", oj[0]);
                            if ((int) oj[3] == 3) {
                                obj.put("cstamt", oj[1]);
                            } else {
                                obj.put("vatamt", oj[1]);
                            }
                            KwlReturnObject KwlObjACC = accountingHandlerDAOobj.getObject(Account.class.getName(), oj[2].toString());
                            Account acct = (Account) KwlObjACC.getEntityList().get(0);
                            obj.put("taxaccount", acct.getAccountName());
                            dataArr.put(obj);
                        }
                    }
                }else if(Constants.Acc_Credit_Note_ModuleId == transctionModule && isSalesAnnax){
                    KwlReturnObject Kwlcreditnote = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), jeObj.getTransactionId());
                    CreditNote creditnote = (CreditNote) Kwlcreditnote.getEntityList().get(0);
                    if (creditnote != null && creditnote.getSalesReturn()!=null) {
                        KwlReturnObject Kwlpurchasereturndebitnote = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), creditnote.getSalesReturn().getID());
                        SalesReturn salesreturn = (SalesReturn) Kwlpurchasereturndebitnote.getEntityList().get(0);
                        if(salesreturn != null){
                            String invoiceId = "";
                            Set<SalesReturnDetail> rows = salesreturn.getRows();
                            for(SalesReturnDetail prdetail:rows){
                                if(prdetail.getCidetails()!=null && prdetail.getCidetails().getInvoice()!=null){
                                    invoiceId = prdetail.getCidetails().getInvoice().getID();
                                    break;
                                }
                            }
                            if(!StringUtil.isNullOrEmpty(invoiceId)){
                                KwlReturnObject KwlgoodsReceipt = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceId);
                                Invoice invoice = (Invoice) KwlgoodsReceipt.getEntityList().get(0);
                                if (invoice != null) {
                                    HashMap<String,Object> data = new HashMap<String,Object>();
                                    data.put("invoiceid",invoice.getID());
                                    data.put("companyid",companyid);
                                    boolean isExciseApplicable = accInvoiceDAOobj.isTaxApplied(data, IndiaComplianceConstants.LINELEVELTERMTYPE_Excise_DUTY);
                                    if(!isExciseApplicable){
                                        String vendorname = invoice.getCustomer().getName();
                                        String vendorvattin = invoice.getCustomer().getVATTINnumber();
                                        String vendorcsttin = invoice.getCustomer().getCSTTINnumber();
                                        HashMap<String, Object> grDetailParams = new HashMap();
                                        grDetailParams.put("SalesReturnid", salesreturn.getID());
                                        grDetailParams.put("termtypeArry", Arrays.asList(1, 3));
                                        KwlReturnObject invoiceTermMapResult = accInvoiceDAOobj.getGenricSalesReturndetailTermMap(grDetailParams);
                                        Iterator itrObj = invoiceTermMapResult.getEntityList().iterator();
                                        while (itrObj.hasNext()) {
                                            JSONObject obj = new JSONObject();
                                            Object[] oj = (Object[]) itrObj.next();
                                            obj.put("entrydate", userdf.format(jeObj.getEntryDate()));
                                            obj.put("entryno", jeObj.getEntryNumber());
                                            obj.put("partyname", vendorname);
                                            obj.put("vattin", vendorvattin);
                                            obj.put("csttin", vendorcsttin);
                                            obj.put("transactionno", creditnote.getCreditNoteNumber());
                                            obj.put("transactionDetails", Constants.CREDIT_NOTE + ", " + invoice.getCustomer().getName());
                                            obj.put("transcationtype", Constants.CREDIT_NOTE);
                                            obj.put("assessablevalue", oj[0]);
                                            if ((int) oj[3] == 3) {
                                                obj.put("cstamt", oj[1]);
                                            } else {
                                                obj.put("vatamt", oj[1]);
                                            }
                                            KwlReturnObject KwlObjACC = accountingHandlerDAOobj.getObject(Account.class.getName(), oj[2].toString());
                                            Account acct = (Account) KwlObjACC.getEntityList().get(0);
                                            obj.put("taxaccount", acct.getAccountName());
                                            dataArr.put(obj);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (isExport) {
                // Sort and Group by Tax Account Name.
                dataArr = StringUtil.sortJsonArray(dataArr, "taxaccount", false, true);
                
                // Add Subtotal for Groups
                JSONArray finalDataArr = new JSONArray();
                double totalAssessableValue = 0.0;
                double totalVATAmount = 0.0;
                double totalCSTAmount = 0.0;
                String previousTaxAccount = "";
                for(int i=0; i<dataArr.length(); i++){
                    JSONObject jtemp = dataArr.getJSONObject(i);
                    if(previousTaxAccount.isEmpty()){
                        previousTaxAccount = jtemp.getString("taxaccount");
                    }
                    
                    // if previous == current   THEN sum up and push current into array.
                    if(previousTaxAccount.equalsIgnoreCase(jtemp.getString("taxaccount"))){
                        if(jtemp.has("assessablevalue") && !StringUtil.isNullOrEmpty(jtemp.getString("assessablevalue"))){
                            totalAssessableValue += jtemp.getDouble("assessablevalue");
                        }
                        if(jtemp.has("vatamt") && !StringUtil.isNullOrEmpty(jtemp.getString("vatamt"))){
                            totalVATAmount += jtemp.getDouble("vatamt");
                        }
                        if(jtemp.has("cstamt") && !StringUtil.isNullOrEmpty(jtemp.getString("cstamt"))){
                            totalCSTAmount += jtemp.getDouble("cstamt");
                        }
                        finalDataArr.put(jtemp);
                    } else { // else previous != current   THEN push summaryJsonobjtotal and reset variables with current and push current into array
                        JSONObject summaryJObj = new JSONObject();
                        summaryJObj.put("transactionno", previousTaxAccount); //ERP-30431
                        summaryJObj.put("entryno", "Total");
                        summaryJObj.put("assessablevalue", totalAssessableValue);
                        summaryJObj.put("vatamt", totalVATAmount);
                        summaryJObj.put("cstamt", totalCSTAmount);
                        summaryJObj.put(IndiaComplianceConstants.boldAndItalicFontStyleFlag, true);
                        finalDataArr.put(summaryJObj);
                        
                        previousTaxAccount = jtemp.getString("taxaccount");
                        if(jtemp.has("assessablevalue") && !StringUtil.isNullOrEmpty(jtemp.getString("assessablevalue"))){
                            totalAssessableValue = jtemp.getDouble("assessablevalue");
                        }
                        if(jtemp.has("vatamt") && !StringUtil.isNullOrEmpty(jtemp.getString("vatamt"))){
                            totalVATAmount = jtemp.getDouble("vatamt");
                        }
                        if(jtemp.has("cstamt") && !StringUtil.isNullOrEmpty(jtemp.getString("cstamt"))){
                            totalCSTAmount = jtemp.getDouble("cstamt");
                        }
                        finalDataArr.put(jtemp);
                    }
                    
                    // If Last Record, then push the summary
                    if(i == dataArr.length()-1){
                        JSONObject summaryJObj = new JSONObject();                        
                        summaryJObj.put("transactionno", previousTaxAccount); //ERP-30431
                        summaryJObj.put("entryno", "Total");
                        summaryJObj.put("assessablevalue", totalAssessableValue);
                        summaryJObj.put("vatamt", totalVATAmount);
                        summaryJObj.put("cstamt", totalCSTAmount);
                        summaryJObj.put(IndiaComplianceConstants.boldAndItalicFontStyleFlag, true);
                        finalDataArr.put(summaryJObj);
                    }
                }
                
                dataArr = finalDataArr;
            }
            int totalCount = dataArr.length();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit) && !isExport) {
                dataArr = StringUtil.getPagedJSON(dataArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            finaljobj.put("data", dataArr);
            finaljobj.put("totalcount",totalCount);
            finaljobj.put("count", totalCount);
            finaljobj.put("success", true);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finaljobj;
    
    }
     public ModelAndView getVATAndCSTCalculationReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject finaljobj = new JSONObject();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String isSalesAnnax = request.getParameter("isSalesAnnax");
            String isExport = request.getParameter("isExport");
            HashMap requestParams = new HashMap();            
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.userdf, userdf);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid =  sessionHandlerImpl.getCurrencyID(request);
            
            requestParams.put("companyid", companyid);            
            requestParams.put("currencyid", currencyid);            
            requestParams.put("isSalesAnnax", isSalesAnnax);            
            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate"))) {
                requestParams.put("startdate", request.getParameter("startdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(start)){
                requestParams.put("start", start);
            }
            if (!StringUtil.isNullOrEmpty(limit)) {
                requestParams.put("limit", limit);
            }
            if (!StringUtil.isNullOrEmpty(isSalesAnnax)) {
                requestParams.put("isSalesAnnax", isSalesAnnax);
            }
            if (!StringUtil.isNullOrEmpty(isExport)) {
                requestParams.put("isExport", isExport);
            }            
            finaljobj = getVATAndCSTCalculationReportJsonMerged(requestParams);           
        } catch(Exception ex){
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", finaljobj.toString());

    }
     
    public ModelAndView exportVATAndCSTCalculationReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject finaljobj = new JSONObject();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String isSalesAnnax = request.getParameter("isSalesAnnax");
            HashMap requestParams = new HashMap();            
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.userdf, userdf);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid =  sessionHandlerImpl.getCurrencyID(request);
            
            requestParams.put("companyid", companyid);            
            requestParams.put("currencyid", currencyid);            
            requestParams.put("isSalesAnnax", isSalesAnnax);            
            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate"))) {
                requestParams.put("startdate", request.getParameter("startdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }
            if (!StringUtil.isNullOrEmpty(start)){
                requestParams.put("start", start);
            }
            if (!StringUtil.isNullOrEmpty(limit)) {
                requestParams.put("limit", limit);
            }
            if (!StringUtil.isNullOrEmpty(isSalesAnnax)) {
                requestParams.put("isSalesAnnax", isSalesAnnax);
            }
            
            requestParams.put("isExport", true);
            
            finaljobj = getVATAndCSTCalculationReportJsonMerged(requestParams);
            if(finaljobj.length() > 0){
                exportDaoObj.processRequest(request, response, finaljobj);
            }
        } catch(Exception ex){
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(Constants.jsonView_ex, Constants.model, finaljobj.toString());

    }
    public ModelAndView exportSalesByItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        boolean isExport = true;
        try {
             HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("fromDate", request.getParameter("fromDate")); //not using right now[PS]
            requestParams.put("toDate", request.getParameter("toDate"));//not using right now[PS]
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("df1", authHandler.getDateOnlyFormat());
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(request);
            requestParams.put("userdf", userdf);
            requestParams.put("browzertimeToze",sessionHandlerImpl.getBrowserTZ(request));
            if (!isExport) {
                requestParams.put(Constants.start, request.getParameter(Constants.start));
                requestParams.put(Constants.limit, request.getParameter(Constants.limit));
}
            int totalProducts = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("totalProducts"))) {
                totalProducts = Integer.parseInt(request.getParameter("totalProducts"));
            }
            String fileType = request.getParameter("filetype");
            String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
            String companyId = sessionHandlerImpl.getCompanyid(request);
            
            if (totalProducts > 1000 && !StringUtil.equal(fileType, "print")) {
                String loginUserId = sessionHandlerImpl.getUserid(request);
                String userDateFormatId = sessionHandlerImpl.getDateFormatID(request);
                String timeZoneDifferenceId = sessionHandlerImpl.getTimeZoneDifference(request);
                String currencyIDForProduct = sessionHandlerImpl.getCurrencyID(request);
                DateFormat dateFormatForProduct = authHandler.getDateOnlyFormat(request);
                Date requestTime = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
                int exportStatus = 1;
                SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_hhmmssaa");
                String fileName = "Sales by Item_" + (sdfTemp.format(requestTime)).toString();
                HashMap<String, Object> exportDetails = new HashMap<>();
                exportDetails.put("fileName", fileName + "." + fileType);
                exportDetails.put("requestTime", requestTime);
                exportDetails.put("status", exportStatus);
                exportDetails.put("companyId", companyId);
                exportDetails.put("fileType", fileType);

                ProductExportDetail obj = null;
                try {
                    status = txnManager.getTransaction(def);
                    KwlReturnObject result1 = accProductObj.saveProductExportDetails(exportDetails);
                    obj = (ProductExportDetail) result1.getEntityList().get(0);
                    txnManager.commit(status);
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
                }

                jobj = getSalesByItem(requestParams, isExport);
                jobj.put("isLargeNumberofProductsExporting", true);
                jobj.put("dateFormatForProduct", dateFormatForProduct);
                jobj.put("userDateFormatId", userDateFormatId);
                jobj.put("timeZoneDifferenceId", timeZoneDifferenceId);
                jobj.put("currencyIDForProduct", currencyIDForProduct);
                jobj.put("productExportFileName", fileName);
                exportDaoObj.processRequest(request, response, jobj);
                HashMap<String, Object> requestParamsForExport = new HashMap<String, Object>();
                requestParamsForExport.put("loginUserId", loginUserId);
                requestParamsForExport.put("productExportFileName", fileName);
                requestParamsForExport.put("filetype", fileType);
                requestParamsForExport.put("isDeatiledReport", false);
                SendMail(requestParamsForExport);
                if (obj != null) {
                    exportStatus = 2;
                    exportDetails.put("id", obj.getId());
                    exportDetails.put("status", exportStatus);
                    try {
                        status = txnManager.getTransaction(def);
                        accProductObj.saveProductExportDetails(exportDetails);
                        txnManager.commit(status);
                    } catch (Exception ex) {
                        txnManager.rollback(status);
                        Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                jobj = getSalesByItem(requestParams, isExport);
                if (StringUtil.equal(fileType, "print")) {
                    jobj.put("GenerateDate", GenerateDate);
                    exportDaoObj.processRequest(request, response, jobj);
                } else {
                    if (StringUtil.equal(fileType, "csv")) {
                        exportDaoObj.processRequest(request, response, jobj);
                    } else {
                        exportDaoObj.processRequest(request, response, jobj);
                    }
                }
            }
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView-empty", "model", jobj.toString());
    }
    
       public void SendMail(HashMap requestParams) throws ServiceException{
        boolean isDeatiledReport=requestParams.containsKey("isDeatiledReport")? (Boolean) requestParams.get("isDeatiledReport"):false;
        String loginUserId = (String) requestParams.get("loginUserId");
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(User.class.getName(), loginUserId);
        User user = (User) returnObject.getEntityList().get(0);
        returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), user.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        String sendorInfo = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAO.getSysEmailIdByCompanyID(company.getCompanyID());
        String fileName = (String) requestParams.get("productExportFileName");
        String exportFileType = (String) requestParams.get("filetype");
        fileName = fileName+"."+exportFileType;

        String cEmail=user.getEmailID()!=null?user.getEmailID():"";
        if(!StringUtil.isNullOrEmpty(cEmail)){
            try {
            String subject="Sales By Item "+(isDeatiledReport ?" Detail ":"")+"Report Export Status";
                //String sendorInfo="admin@deskera.com";
            String htmlTextC="";
            htmlTextC+="<br/>Hello "+user.getFirstName()+"<br/>"; 
            htmlTextC+="<br/>Sales By Item "+(isDeatiledReport ?" Detail ":"")+"Report <b>\""+fileName+"\"</b> has been generated successfully.<br/><br/> You can download it from <b> Export Details Report</b>.<br/>";
            htmlTextC+="<br/>Regards,<br/>";
            htmlTextC+="<br/>ERP System<br/>";
            String plainMsgC="";
            plainMsgC+="\nHello "+user.getFirstName()+"\n"; 
            plainMsgC+="\nSales By Item "+(isDeatiledReport ?" Detail ":"")+"Report<b>\""+fileName+"\"</b> has been generated successfully.<br/><br/> You can download it from <b> Export Details Report</b>.\n";
                        plainMsgC+="\nRegards,\n";
            plainMsgC+="\nERP System\n";

                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
            SendMailHandler.postMail(new String[]{cEmail},subject,htmlTextC,plainMsgC,sendorInfo, smtpConfigMap);
            } catch (Exception ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
    public ModelAndView getSalesByItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isExport = false;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("fromDate", request.getParameter("fromDate")); //not using right now[PS]
            requestParams.put("toDate", request.getParameter("toDate"));//not using right now[PS]
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("df1", authHandler.getDateOnlyFormat());
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(request);
            requestParams.put("userdf", userdf);
            requestParams.put("browzertimeToze",sessionHandlerImpl.getBrowserTZ(request));
            if (!isExport) {
                requestParams.put(Constants.start, request.getParameter(Constants.start));
                requestParams.put(Constants.limit, request.getParameter(Constants.limit));
            }
            
            jobj = getSalesByItem(requestParams, isExport);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

      public List getTotalSalesCost(HashMap<String, Object> requestParams, String productid) throws ServiceException, ParseException {
        List ll = new ArrayList();
        try {
            DateFormat df1 = (DateFormat) requestParams.get("df1");
            Date fromDate = df1.parse((String) requestParams.get("fromDate"));
            Date toDate = df1.parse((String) requestParams.get("toDate"));
            KwlReturnObject invoiceDetailResult = accInvoiceDAOobj.getInvoiceProductDetails(productid, fromDate, toDate, requestParams);
            List<InvoiceDetail> invDetailList = invoiceDetailResult.getEntityList();
            String companyid = "";
            if(requestParams.containsKey("companyid")){
                companyid = (String) requestParams.get("companyid");
            }
            double totalQuantityOut = 0;
            double totalProSales = 0;
            double baseUomQuantity = 0;
            for (InvoiceDetail invDetail : invDetailList) {
                double temqua = 0;
                double invProSales = 0;
                double baseumrate = 1;
                baseumrate = invDetail.getInventory().getBaseuomrate();
                HashMap hm = accInvoiceServiceDAO.applyCreditNotes_Modified(requestParams, invDetail);
                if (hm.containsKey(invDetail)) {
                    Object[] val = (Object[]) hm.get(invDetail);
                    invProSales = (Double) val[3];//formula:(rate*quantity)-rowdiscount        //-invdiscount-cnamount[PS]
                    temqua = (Double) val[1];
//                    KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invProSales, invDetail.getInvoice().getCurrency().getCurrencyID(), invDetail.getInvoice().getJournalEntry().getEntryDate(), invDetail.getInvoice().getExternalCurrencyRate());
                    KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invProSales, invDetail.getInvoice().getCurrency().getCurrencyID(), invDetail.getInvoice().getCreationDate(), invDetail.getInvoice().getExternalCurrencyRate());
                    invProSales = (Double) crresult.getEntityList().get(0);
                    totalQuantityOut += temqua;
                    totalProSales += invProSales;
                    baseUomQuantity += authHandler.calculateBaseUOMQuatity(temqua, baseumrate, companyid);
                }
            }
            invDetailList.clear();
            KwlReturnObject quantityDO = accProductObj.getQuantityPurchaseOrSalesDetailsByItem(productid,fromDate,toDate, false, false);// SDP-3587 Applied Date filters for the DO Items Sold
            List<Inventory> quantityList = quantityDO.getEntityList();
            double proPriceInBase = 0;
                for (Inventory inv : quantityList) {
                double quantityOut = 0;
                double proPrice = 0;
                quantityOut = inv.getQuantity();
                double srQuantity = 0;
                Date invDate = inv.getUpdateDate();
                double baseuomrate = 1;
                baseuomrate = inv.getBaseuomrate();
                KwlReturnObject priceResult = accProductObj.getProductPrice(productid, true, invDate, "", "");
                List<Object> priceList = priceResult.getEntityList();
                if (priceList != null) {
                    for (Object cogsval : priceList) {
                        proPrice = (cogsval == null ? 0.0 : (Double) cogsval);
                    }
                    priceResult = accInvoiceDAOobj.getDOByInventoryID(inv.getID());
                    List<DeliveryOrderDetail> doDetail = priceResult.getEntityList();
                    for (DeliveryOrderDetail deliveryOrderDetail : doDetail) {
                        DeliveryOrder dr = deliveryOrderDetail.getDeliveryOrder();
                        double rate = 0;
                        // SDP-3587 If DO >> Invoice Linking then minus the invoice quantity 
                        double minusquantity = accInvoiceDAOobj.getInvoiceQuantityFromDOD(deliveryOrderDetail.getID());
                        if (deliveryOrderDetail.getCidetails() == null && deliveryOrderDetail.getSodetails() == null) {
                            quantityOut = quantityOut - srQuantity - minusquantity;
                            proPrice = deliveryOrderDetail.getRate();
                            proPrice = proPrice / baseuomrate;
                            totalQuantityOut += quantityOut;
                            KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, proPrice, dr.getCurrency().getCurrencyID(), dr.getOrderDate(), rate);
                            proPrice = (Double) crresult.getEntityList().get(0);
                            proPriceInBase = proPrice;
                            totalProSales += (quantityOut * proPriceInBase);
                            baseUomQuantity += authHandler.calculateBaseUOMQuatity(quantityOut, baseuomrate, companyid);
                        }
                    }
                }
            }
            quantityList.clear();
            ll.add(0, totalProSales);
            ll.add(1, totalQuantityOut);
            ll.add(2, baseUomQuantity);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getSalesByItem : " + ex.getMessage(), ex);
        }
        return ll;
    }
     public List getTotalPurchaseCost(String productid, HashMap<String, Object> params) throws ServiceException, ParseException {
        List ll = new ArrayList();
        try {
            double totalProPurchase = 0;
            double totalQuantityIn = 0;
            String companyid = (String) params.get(Constants.companyid);
            KwlReturnObject quantityResult = accProductObj.getQuantityPurchaseOrSalesDetails(productid, true, true);
            List<Inventory> quantityList = quantityResult.getEntityList();
            for (Inventory inv : quantityList) {
                double quantityIn = 0;
                double proPrice = 0;
                quantityIn = inv.getQuantity();
                double prQuantity = 0;
                boolean isOpening = true;
                Date invDate = inv.getUpdateDate();
                double baseuomrate = 1;
                baseuomrate = inv.getBaseuomrate();
                KwlReturnObject priceResult = accProductObj.getProductPrice(productid, true, invDate, "", "");
                proPrice = (priceResult.getEntityList().get(0) != null) ? (Double) priceResult.getEntityList().get(0) : 0;
                if (priceResult.getEntityList() != null) {
                    // new logic for fetching product price
                    GoodsReceipt goodsReceipt = null;
                    KwlReturnObject result = accGoodsReceiptDAOObj.getGoodsReceipt_Rate(inv.getID());
                    List<Object[]> GRList = result.getEntityList();
                    if (GRList != null) {
                        for (Object[] temp : GRList) {
                            goodsReceipt = (GoodsReceipt) temp[0];
                            proPrice = (temp[1] == null ? 0.0 : (Double) temp[1]);
                        }
                    }

                    double proPriceInBase = 0;
                    if (goodsReceipt != null) {
                        isOpening = false;
                        totalQuantityIn += quantityIn;
                        Date creationDate = null;
                        creationDate = goodsReceipt.getCreationDate();
//                        if (goodsReceipt.isIsOpeningBalenceInvoice()) {
//                            creationDate = goodsReceipt.getCreationDate();
//                        } else {
//                            creationDate = goodsReceipt.getJournalEntry().getEntryDate();
//                        }
                        KwlReturnObject bAmt = null;
                        if(goodsReceipt.isIsOpeningBalenceInvoice()){
                            if(goodsReceipt.isConversionRateFromCurrencyToBase()){
                                bAmt = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(params, proPrice, goodsReceipt.getCurrency().getCurrencyID(), creationDate, goodsReceipt.getExchangeRateForOpeningTransaction());
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(params, proPrice, goodsReceipt.getCurrency().getCurrencyID(), creationDate, goodsReceipt.getExchangeRateForOpeningTransaction());
                            }
                        } else if(goodsReceipt.isNormalInvoice()){
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(params, proPrice, goodsReceipt.getCurrency().getCurrencyID(), creationDate, goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                        }
                        
                        proPriceInBase = (Double) bAmt.getEntityList().get(0);
                        proPriceInBase = authHandler.round(proPriceInBase, companyid);
                    } else {
                        priceResult = accGoodsReceiptDAOObj.getGoodsReceiptOrderFormInventory(inv.getID());
                        List<GoodsReceiptOrderDetails> grODetail = priceResult.getEntityList();
                        for (GoodsReceiptOrderDetails goodsReceiptOrderDetails : grODetail) {
                            isOpening = false;
                            prQuantity = accGoodsReceiptDAOObj.getPurchaseReturnQtyFormGoodsReceipt(goodsReceiptOrderDetails.getID());
                            GoodsReceiptOrder gr = goodsReceiptOrderDetails.getGrOrder();
                            if (goodsReceiptOrderDetails.getVidetails() == null && goodsReceiptOrderDetails.getPodetails() == null) {
                                totalQuantityIn += quantityIn;
                                quantityIn = quantityIn - prQuantity;
                                proPrice = goodsReceiptOrderDetails.getRate();
                                proPrice = proPrice / baseuomrate;
                                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(params, proPrice, gr.getCurrency().getCurrencyID(), gr.getOrderDate(), gr.getExternalCurrencyRate());
                                proPrice = (Double) bAmt.getEntityList().get(0);
                                proPrice = authHandler.round(proPrice, companyid);
                                proPriceInBase = proPrice;
                            }
                        }
                        if (isOpening) {
                            totalQuantityIn += quantityIn;
                            proPriceInBase = proPrice;
                        }

                    }
                    // new logic for fetching product price
                    totalProPurchase += (quantityIn * proPriceInBase);
                }
            }
            ll.add(0, totalProPurchase);
            ll.add(1, totalQuantityIn);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("getSalesByItem : " + ex.getMessage(), ex);
        }
        return ll;
    }

    public JSONObject getSalesByItem(HashMap<String,Object> requestParams, boolean isExport) throws ServiceException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
            
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap=new HashMap<String, Integer>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList((String)requestParams.get(Constants.companyKey), Constants.Acc_Product_Master_ModuleId));
            FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            String companyid = "";
            if(requestParams.containsKey("companyid")){
                companyid = (String) requestParams.get("companyid");
            }
            DateFormat userdf= (DateFormat) requestParams.get("userdf");
            DecimalFormat df = new DecimalFormat("###0.00");
            KwlReturnObject result = accProductObj.getProductList((Map) requestParams);
            int count = result.getRecordTotalCount();
            List<Product> list = result.getEntityList();
            JSONArray jArr = new JSONArray();
            if(requestParams.containsKey(Constants.start)){
               requestParams.remove(Constants.start);
            }
            if(requestParams.containsKey(Constants.limit)){
               requestParams.remove(Constants.limit);
            }
           
            if (list != null) {
                for (Product proObj : list) {
                    double totalProPurchase = 0;
                    double totalQuantityIn = 0;
                    double totalQuantityOut = 0;
                    double baseUomQuantity = 0;
                    double margin = 0;
                    double totalProSales = 0;

                    List ll = getTotalSalesCost(requestParams, proObj.getID());
                    totalProSales = (Double) ll.get(0);
                    totalQuantityOut = (Double) ll.get(1);
                    baseUomQuantity = (Double) ll.get(2);

                    ll = getTotalPurchaseCost(proObj.getID(), requestParams);
                    totalProPurchase = (Double) ll.get(0);
                    totalQuantityIn = (Double) ll.get(1);

                    margin = (totalQuantityOut == 0 || totalQuantityIn == 0 ? 0 : (totalProSales / totalQuantityOut) - (totalProPurchase / totalQuantityIn));
                    JSONObject obj = new JSONObject();
                    obj.put("productname", proObj.getName());
                    obj.put("pid", proObj.getProductid());
                    obj.put("totalQuantityIn", totalQuantityIn);//cal. through inventory
                    obj.put("quantity", authHandler.formattedQuantity(totalQuantityOut, companyid));//cal. through invoice
                    obj.put("amount", totalProSales);
                    obj.put("baseuomquantity", authHandler.formattedQuantity(baseUomQuantity, companyid));//cal. through inventory
                    obj.put("cogs", totalProPurchase);
                    obj.put("avgsale", (totalQuantityOut == 0 || totalProSales == 0 ? 0 : (totalProSales / totalQuantityOut)));//
                    obj.put("avgcogs", totalProPurchase == 0 || totalQuantityIn == 0 ? 0 : (totalProPurchase / totalQuantityIn));
                    obj.put("margin", margin);
                    obj.put("permargin", (totalProPurchase == 0 || margin == 0 ? 0 : df.format((margin * 100 * totalQuantityIn) / totalProPurchase)));//margin*/100/avg purchase cost
                    
                    Map<String, Object> variableMap = new HashMap<>();
                    AccProductCustomData jeDetailCustom = (AccProductCustomData) proObj.getProductCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                            if (customFieldMap.containsKey(varEntry.getKey())) {
                                String value = "";
                                String Ids[] = coldata.split(",");
                                for (int i = 0; i < Ids.length; i++) {
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                    if (fieldComboData != null) {
                                        if (fieldComboData.getField().getFieldtype() == 12) {
                                            value += fieldComboData.getValue() != null ? fieldComboData.getId() + "," : ",";
                                        } else {
                                            value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                        }
                                    }
                                }
                                if (!StringUtil.isNullOrEmpty(value)) {
                                    value = value.substring(0, value.length() - 1);
                                }
                                obj.put(varEntry.getKey(), value);
                            } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                if (isExport) {
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        DateFormat df2 = null;
                                        DateFormat defaultDateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);
                                        Date dateFromDB = null;
                                        if (userdf != null) {
                                            try {
                                                df2 = userdf;
                                                dateFromDB = defaultDateFormat.parse(coldata);
                                                coldata = df2.format(dateFromDB);
                                            } catch (ParseException p) {

                                            }
                                        }                                       
                                    }
                                }
                                obj.put(varEntry.getKey(), (coldata));
                            } else {
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    obj.put(varEntry.getKey(), coldata);
                                }
                            }
                        }
                    }
                    
                    jArr.put(obj);
                }
            }
            jobj.put(Constants.RES_data, jArr);
            jobj.put(Constants.RES_count, count);
            jobj.put("totalProducts", count);
       
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getSalesByItem : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView exportDetailedSalesByItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        boolean isExport = true;
        try {
            
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("fromDate", request.getParameter("fromDate"));
            requestParams.put("toDate", request.getParameter("toDate"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("fromDate", authHandler.getDateOnlyFormat().parse(request.getParameter("fromDate")));
            requestParams.put("toDate", authHandler.getDateOnlyFormat().parse(request.getParameter("toDate")));
            requestParams.put("userDateFormat", authHandler.getUserDateFormatter(request));
            if (!isExport) {
                requestParams.put(Constants.start, request.getParameter(Constants.start));
                requestParams.put(Constants.limit, request.getParameter(Constants.limit));
            }
            int totalProducts = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("totalProducts"))) {
                totalProducts = Integer.parseInt(request.getParameter("totalProducts"));
            }
            String fileType = request.getParameter("filetype");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            if (totalProducts > 1000 && !StringUtil.equal(fileType, "print")) {
                String loginUserId = sessionHandlerImpl.getUserid(request);
                String userDateFormatId = sessionHandlerImpl.getDateFormatID(request);
                String timeZoneDifferenceId = sessionHandlerImpl.getTimeZoneDifference(request);
                String currencyIDForProduct = sessionHandlerImpl.getCurrencyID(request);
                DateFormat dateFormatForProduct = authHandler.getDateOnlyFormat(request);
                Date requestTime = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
                int exportStatus = 1;
                SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_hhmmssaa");
                String fileName = "Sales by Item_" + (sdfTemp.format(requestTime)).toString();
                HashMap<String, Object> exportDetails = new HashMap<>();
                exportDetails.put("fileName", fileName + "." + fileType);
                exportDetails.put("requestTime", requestTime);
                exportDetails.put("status", exportStatus);
                exportDetails.put("companyId", companyId);
                exportDetails.put("fileType", fileType);

                ProductExportDetail obj = null;
                try {
                    status = txnManager.getTransaction(def);
                    KwlReturnObject result1 = accProductObj.saveProductExportDetails(exportDetails);
                    obj = (ProductExportDetail) result1.getEntityList().get(0);
                    txnManager.commit(status);
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
                jobj = getDetailedSalesByItem(requestParams, isExport);
                jobj.put("isLargeNumberofProductsExporting", true);
                jobj.put("dateFormatForProduct", dateFormatForProduct);
                jobj.put("userDateFormatId", userDateFormatId);
                jobj.put("timeZoneDifferenceId", timeZoneDifferenceId);
                jobj.put("currencyIDForProduct", currencyIDForProduct);
                jobj.put("productExportFileName", fileName);
                exportDaoObj.processRequest(request, response, jobj);
                HashMap<String, Object> requestParamsForExport = new HashMap<>();
                requestParamsForExport.put("loginUserId", loginUserId);
                requestParamsForExport.put("productExportFileName", fileName);
                requestParamsForExport.put("filetype", fileType);
                requestParamsForExport.put("isDeatiledReport", true);
                SendMail(requestParamsForExport);
                if (obj != null) {
                    exportStatus = 2;
                    exportDetails.put("id", obj.getId());
                    exportDetails.put("status", exportStatus);
                    try {
                        status = txnManager.getTransaction(def);
                        accProductObj.saveProductExportDetails(exportDetails);
                        txnManager.commit(status);
                    } catch (Exception ex) {
                        txnManager.rollback(status);
                        Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                jobj = getDetailedSalesByItem(requestParams, isExport);
                if (StringUtil.equal(fileType, "print")) {
                    String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                    jobj.put("GenerateDate", GenerateDate);
                    exportDaoObj.processRequest(request, response, jobj);
                } else {
                    if (StringUtil.equal(fileType, "csv")) {
                        exportDaoObj.processRequest(request, response, jobj);
                    } else {
                        exportDaoObj.processRequest(request, response, jobj);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView-empty", "model", jobj.toString());
    }

    public ModelAndView getDetailedSalesByItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isExport = false;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("fromDate", request.getParameter("fromDate"));
            requestParams.put("toDate", request.getParameter("toDate"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("fromDate", authHandler.getDateOnlyFormat().parse(request.getParameter("fromDate")));
            requestParams.put("toDate", authHandler.getDateOnlyFormat().parse(request.getParameter("toDate")));
            requestParams.put("userDateFormat", authHandler.getUserDateFormatterWithoutTimeZone(request));
            if (!isExport) {
                requestParams.put(Constants.start, request.getParameter(Constants.start));
                requestParams.put(Constants.limit, request.getParameter(Constants.limit));
            }
            jobj = getDetailedSalesByItem(requestParams, isExport);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getDetailedSalesByItem(HashMap<String,Object> requestParams, boolean isExport) throws ServiceException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
            
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String) requestParams.get(Constants.companyKey));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String cashAccount = pref.getCashAccount().getID();
            Date fromDate = (Date) requestParams.get("fromDate");
            Date toDate = (Date)requestParams.get("toDate");
            String companyid = "";
            if(requestParams.containsKey("companyid")){
                companyid = (String) requestParams.get("companyid");
            }
            DateFormat userDateFormat=(DateFormat) requestParams.get("userDateFormat");
            JSONArray jArr = new JSONArray();
                double cumquantity = 0;
                double cumsales = 0;
                String previouseProduct=null;
                requestParams.put("isorderByProduct", true);
                KwlReturnObject invoiceDetaailResult = accInvoiceDAOobj.getInvoiceProductDetails("", fromDate, toDate, requestParams);
                List<InvoiceDetail>  invDetails= invoiceDetaailResult.getEntityList();
                int count=invoiceDetaailResult.getRecordTotalCount();
                
                List<String> productIDs = new ArrayList();
                for (InvoiceDetail invDetail:invDetails) {
                    Product proObj =invDetail.getInventory().getProduct();
                    if(proObj==null){
                        continue;
                    }
                    if(previouseProduct == null || !proObj.getID().equals(previouseProduct)){
                        previouseProduct=proObj.getID();
                        cumquantity=0;
                        cumsales=0;
                    }
                    
                    HashMap hm = accInvoiceServiceDAO.applyCreditNotes_Modified(requestParams, invDetail);
                    if (hm.containsKey(invDetail)) {
                        Object[] val = (Object[]) hm.get(invDetail);
                        double temqua = (Double) val[1];
                        double invProSales = 0;
                        double rateinbase = 0;
                        if (temqua > 0) {
                            double rowTaxPercent = 0;
                            double rowTaxAmount = 0;
                            boolean isRowTaxApplicable = false;
                            if (invDetail.getTax() != null) {
//                                KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get(Constants.companyKey), invDetail.getInvoice().getJournalEntry().getEntryDate(), invDetail.getTax().getID());
                                KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get(Constants.companyKey), invDetail.getInvoice().getCreationDate(), invDetail.getTax().getID());
                                rowTaxPercent = (Double) perresult.getEntityList().get(0);
                                isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                            }
                            invProSales = ((invDetail.getRate() * temqua) - (invDetail.getDiscount() == null ? 0 : invDetail.getDiscount().getDiscountValue()));
                            if (invDetail.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = invDetail.getRowTaxAmount() + invDetail.getRowTermTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = invProSales * rowTaxPercent / 100;
                            }
                            invProSales += rowTaxAmount;//invProSales += rowTaxPercent > 0 ?((rowTaxPercent * invProSales)/100) : 0;
                        }

//                        KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invProSales, invDetail.getInvoice().getCurrency().getCurrencyID(), invDetail.getInvoice().getJournalEntry().getEntryDate(), invDetail.getInvoice().getExternalCurrencyRate());
                        KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invProSales, invDetail.getInvoice().getCurrency().getCurrencyID(), invDetail.getInvoice().getCreationDate(), invDetail.getInvoice().getExternalCurrencyRate());
                        invProSales = (Double) crresult.getEntityList().get(0);
                        cumquantity += temqua;
                        cumsales += invProSales;
                        cumquantity += temqua;

                        JournalEntryDetail d = invDetail.getInvoice().getCustomerEntry();
                        Account account = d.getAccount();
                        String promisedDate = "";
                        double amountdue = 0.0;
                        if (account.getID().equals(cashAccount)) {
//                            promisedDate = authHandler.getDateOnlyFormat().format(invDetail.getInvoice().getJournalEntry().getEntryDate());
                            promisedDate = authHandler.getDateOnlyFormat().format(invDetail.getInvoice().getCreationDate());
                        } else {
                            amountdue = accInvoiceCommon.getAmountDue(requestParams, invDetail.getInvoice());
                            promisedDate = authHandler.getDateOnlyFormat().format(invDetail.getInvoice().getDueDate());
                        }
//                        rateinbase = (Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, (invDetail.getRate()), invDetail.getInvoice().getCurrency().getCurrencyID(), invDetail.getInvoice().getJournalEntry().getEntryDate(), invDetail.getInvoice().getExternalCurrencyRate()).getEntityList().get(0);
                        rateinbase = (Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, (invDetail.getRate()), invDetail.getInvoice().getCurrency().getCurrencyID(), invDetail.getInvoice().getCreationDate(), invDetail.getInvoice().getExternalCurrencyRate()).getEntityList().get(0);
                        JSONObject obj = new JSONObject();
                        obj.put("productname", invDetail.getInventory().getProduct().getName());
                        obj.put("pid", proObj.getProductid());
                        obj.put("productdescription", StringUtil.isNullOrEmpty(invDetail.getDescription()) ? "" : invDetail.getDescription());
                        obj.put("productid", proObj.getID());
                        obj.put("billno", invDetail.getInvoice().getInvoiceNumber());
                        obj.put("invoiceid", invDetail.getInvoice().getID());
//                        obj.put("date", authHandler.getDateOnlyFormat().format(invDetail.getInvoice().getJournalEntry().getEntryDate()));
                        obj.put("date", authHandler.getDateOnlyFormat().format(invDetail.getInvoice().getCreationDate()));
                        obj.put("status", amountdue > 0 ? "Open" : "Closed");
                        obj.put("promisedDate", promisedDate);
                        obj.put("memo", invDetail.getInvoice().getMemo());
                        obj.put("personname", invDetail.getInvoice().getCustomer() == null ? account.getName() : invDetail.getInvoice().getCustomer().getName());
                        obj.put("quantity", authHandler.formattedQuantity(temqua, companyid));
                        obj.put("rateinbase", authHandler.formattedAmount(rateinbase, companyid)); // Sales Price 
                        obj.put("amount", authHandler.formattedAmount(invProSales, companyid)); // Amount 
                        obj.put("totalsales", authHandler.formattedAmount(cumsales, companyid)); // Balance 
                        obj.put("totalquantity", cumquantity);
                        obj.put("isinvoice", "true");
                        jArr.put(obj);
                    }
                
                    if (!productIDs.contains(proObj.getID())) {
                        /**
                         * productIDs keep track for DO if Already added for particular product then not to add again.
                         */
                        productIDs.add(proObj.getID());
                        // Get DO which are not linked with Invoices or SO.
                KwlReturnObject quantityDO = accProductObj.getQuantityPurchaseOrSalesDetailsByItem(proObj.getID(),fromDate,toDate, false, false);
                        List<Inventory> quantityList = quantityDO.getEntityList();
                        double proPriceInBase = 0;
                        for (Inventory inv : quantityList) {
                            double quantityOut = 0;
                            double proPrice = 0;
                            quantityOut = inv.getQuantity();
                            double srQuantity = 0;
                            Date invDate = inv.getUpdateDate();
                            double baseuomrate = 1;
                            baseuomrate = inv.getBaseuomrate();
                            KwlReturnObject priceResult = accProductObj.getProductPrice(proObj.getID(), true, invDate, "", "");
                            List<Object> priceList = priceResult.getEntityList();
                            if (priceList != null) {
                                for (Object cogsval : priceList) {
                                    proPrice = (cogsval == null ? 0.0 : (Double) cogsval);
                                }
                                priceResult = accInvoiceDAOobj.getDOByInventoryID(inv.getID());
                                List<DeliveryOrderDetail> doDetail = priceResult.getEntityList();
                                DeliveryOrderDetail d0d = null;
                                if (doDetail != null) {
                                    for (DeliveryOrderDetail do1 : doDetail) {
                                        d0d = do1;
                                        DeliveryOrder dr = d0d.getDeliveryOrder();
                                        double rate = 0;
                                        // SDP-3587 If DO >> Invoice Linking then minus the invoice quantity 
                                        double minusquantity = accInvoiceDAOobj.getInvoiceQuantityFromDOD(d0d.getID());
                                    quantityOut-=minusquantity;
                                    if (d0d.getCidetails() == null && d0d.getSodetails() == null && quantityOut!=0) {
                                            quantityOut = quantityOut - srQuantity;
                                            proPrice = d0d.getRate();
                                            proPrice = proPrice / baseuomrate;
                                            KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, proPrice, dr.getCurrency().getCurrencyID(), dr.getOrderDate(), rate);
                                            proPrice = (Double) crresult.getEntityList().get(0);
                                            proPriceInBase = proPrice;
                                        cumsales+=(quantityOut * proPriceInBase);
                                            JSONObject obj = new JSONObject();
                                            obj.put("productname", inv.getProduct().getName());
                                            obj.put("pid", proObj.getProductid());
                                            obj.put("productdescription", StringUtil.isNullOrEmpty(d0d.getDescription()) ? "" : d0d.getDescription());
                                            obj.put("productid", inv.getProduct().getID());
                                            obj.put("billno", d0d.getDeliveryOrder().getDeliveryOrderNumber());
                                            obj.put("invoiceid", d0d.getDeliveryOrder().getID());
                                            obj.put("date", userDateFormat.format(d0d.getDeliveryOrder().getOrderDate()));
                                            obj.put("status", "-");
                                        obj.put("promisedDate", d0d.getDeliveryOrder().getShipdate()!=null?userDateFormat.format(d0d.getDeliveryOrder().getShipdate()):"");
                                            obj.put("memo", d0d.getDeliveryOrder().getMemo());
                                            obj.put("personname", d0d.getDeliveryOrder().getCustomer() == null ? "" : d0d.getDeliveryOrder().getCustomer().getName());
                                            obj.put("quantity", authHandler.formattedQuantity(quantityOut, companyid));
                                            obj.put("rateinbase", authHandler.formattedAmount(proPriceInBase, companyid)); // Sales Price 
                                            obj.put("amount", authHandler.formattedAmount((quantityOut * proPriceInBase), companyid)); // Amount 
                                            obj.put("totalsales", authHandler.formattedAmount(cumsales, companyid)); // Balance 
                                        obj.put("totalquantity", cumquantity+quantityOut);
                                            obj.put("isinvoice", "false");
                                            jArr.put(obj);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }    
                jobj.put(Constants.RES_data, jArr);
//            }

            jobj.put(Constants.RES_data, jArr);
            jobj.put(Constants.RES_count, count);
            jobj.put("totalProducts", count);

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("getSalesByItem : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getSalesByItem : " + ex.getMessage(), ex);
        }
        return jobj;
    }
    
    public ModelAndView exportServiceTaxCreditRegister(HttpServletRequest request, HttpServletResponse response) {
        JSONObject finaljobj = new JSONObject();
        String view = Constants.jsonView_ex;
        try {
            SimpleDateFormat gdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat formatDateXls = new SimpleDateFormat("d-MMM-yyyy");
            int frequency = (!StringUtil.isNullOrEmpty(request.getParameter("frequency"))) ? Integer.parseInt(request.getParameter("frequency").toString()) : 0;
            String period = (!StringUtil.isNullOrEmpty(request.getParameter("period"))) ? request.getParameter("period").toString() : "";
            int year = Integer.parseInt((!StringUtil.isNullOrEmpty(request.getParameter("year"))) ? request.getParameter("year").toString() : "0");
            Date nextYear = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.YEAR, +1);
            nextYear.setTime(c.getTime().getTime());
            String yearStr = year + "-" +(year+1);
            String startdate="", enddate="";
            if (frequency == 0) {
                startdate = year + "-" + "04" + "-" + "01";
                enddate = (year + 1) + "-" + "03" + "-" + "31";
            } else if (!StringUtil.isNullOrEmpty(request.getParameter("frequency"))) {  // get start date and end date
                if (request.getParameter("frequency").equals("1")) { //Quaterly
                    int firstMonth = 0;
                    int lastMonth = 11;
                    String qq = "";
                    if (period.equals("quater4")) { //1st January 2017 to 31st Mar 2017 (Quarter IV)
                        firstMonth = 0;
                        lastMonth = 2;
                        qq = "44";
                        year += 1;
                    } else if (period.equals("quater1")) {  //1st April 2016 to 30th June 2016 (Quarter I)
                        firstMonth = 3;
                        lastMonth = 5;
                        qq = "41";
                    } else if (period.equals("quater2")) {  //1st July 2016 to 30th September 2016 (Quarter II)
                        firstMonth = 6;
                        lastMonth = 8;
                        qq = "42";
                    } else if (period.equals("quater3")) { //1st October 2016 to 31st December 2016 (Quarter III)
                        firstMonth = 9;
                        lastMonth = 11;
                        qq = "43";
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, firstMonth);
                    calendar.set(Calendar.DATE, 1);
                    Date firstDate = calendar.getTime();
                    startdate = gdf.format(firstDate);

                    calendar.set(Calendar.MONTH, lastMonth);
                    int TotalDaysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    calendar.set(Calendar.DATE, TotalDaysOfMonth);
                    Date lastDate = calendar.getTime();
                    enddate = gdf.format(lastDate);
                } else { // Monthly
                    Calendar calendar = Calendar.getInstance();
                    int month = Integer.parseInt(period);
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DATE, 1);
                    Date firstDate = calendar.getTime();
                    startdate = gdf.format(firstDate);

                    int TotalDaysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    calendar.set(Calendar.DATE, TotalDaysOfMonth);
                    Date lastDate = calendar.getTime();
                    enddate = gdf.format(lastDate);
                }
            }
            
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject extracompanypreferencesresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracompanypreferencesresult.getEntityList().get(0);
            
            String companyNameAndAddress = extraCompanyPreferences.getCompany().getCompanyName();
            if(!StringUtil.isNullOrEmpty(extraCompanyPreferences.getCompany().getAddress())){
                companyNameAndAddress += ", "+ extraCompanyPreferences.getCompany().getAddress();
            }
            
            finaljobj.append("leftheaderarr", "Name and Address of Assessee :");
            finaljobj.append("leftheaderdataarr", companyNameAndAddress);
            finaljobj.append("leftheaderarr", "ECC No. :");
            finaljobj.append("leftheaderdataarr", extraCompanyPreferences.getEccNumber());
            finaljobj.append("leftheaderarr", "Service Tax Reg. No.:");
            finaljobj.append("leftheaderdataarr", extraCompanyPreferences.getServiceTaxRegNo());
            
            finaljobj.append("rightheaderarr", "Tax Period:");
            finaljobj.append("rightheaderdataarr", formatDateXls.format(gdf.parse(startdate))+" to "+formatDateXls.format(gdf.parse(enddate)));
            finaljobj.append("rightheaderarr", "Year:");
            finaljobj.append("rightheaderdataarr", yearStr);
            
            // Get data Array for excel report
            HashMap<String, Object> serviceTaxCreditRegisterMap = new HashMap<String, Object>();
            serviceTaxCreditRegisterMap.put(Constants.companyid, companyId);
            serviceTaxCreditRegisterMap.put(Constants.REQ_startdate, startdate);
            serviceTaxCreditRegisterMap.put(Constants.REQ_enddate, enddate);
            JSONArray dataArr = getDataForServiceTaxCreditRegister(serviceTaxCreditRegisterMap);
            
            finaljobj.put("data", dataArr);
            if(finaljobj.length() > 0){
                exportDaoObj.processRequestMVATIndia(request, response, finaljobj);
            }
        } catch (Exception ex) {
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, Constants.model, finaljobj.toString());
    }
    
    public JSONArray getDataForServiceTaxCreditRegister(HashMap<String, Object> serviceTaxCreditRegisterMap) throws ServiceException, ParseException, JSONException{
        JSONArray dataArr = new JSONArray();
        
        SimpleDateFormat gdf = new SimpleDateFormat("yyyy-MM-dd");
        int entrynoCounter = 1;
        double previousBalanceServiceTaxAmt = 0.0, previousBalanceSBCTaxAmt = 0.0, previousBalanceKKCTaxAmt = 0.0;

        ArrayList termTypeArr = new ArrayList();
        termTypeArr.add(IndiaComplianceConstants.LINELEVELTERMTYPE_SERVICE_TAX);
        termTypeArr.add(IndiaComplianceConstants.LINELEVELTERMTYPE_SBC);
        termTypeArr.add(IndiaComplianceConstants.LINELEVELTERMTYPE_KKC);

        String companyId = "";
        if(serviceTaxCreditRegisterMap.containsKey(Constants.companyid)){
            companyId = serviceTaxCreditRegisterMap.get(Constants.companyid).toString();
        }
        String startdate = "";
        if(serviceTaxCreditRegisterMap.containsKey(Constants.REQ_startdate)){
            startdate = serviceTaxCreditRegisterMap.get(Constants.REQ_startdate).toString();
        }
        String enddate = "";
        if(serviceTaxCreditRegisterMap.containsKey(Constants.REQ_enddate)){
            enddate = serviceTaxCreditRegisterMap.get(Constants.REQ_enddate).toString();
        }
        
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(GoodsReceiptConstants.COMPANYID, companyId);
        requestParams.put(GoodsReceiptConstants.JEstartDate, gdf.parse(startdate));
        requestParams.put(GoodsReceiptConstants.JEendDate, gdf.parse(enddate));
//        requestParams.put(GoodsReceiptConstants.ORDERBY_COLUMN, "grd.goodsReceipt.journalEntry.entryDate");
        requestParams.put(GoodsReceiptConstants.ORDERBY_COLUMN, "grd.goodsReceipt.creationDate");
        KwlReturnObject grdetailsresult = accGoodsReceiptDAOObj.getGoodsReceiptData(requestParams);
        Iterator itr = grdetailsresult.getEntityList().iterator();
        while (itr.hasNext()) {
            GoodsReceiptDetail row = (GoodsReceiptDetail) itr.next();

            HashMap<String, Object> requestParams2 = new HashMap<String, Object>();
            requestParams2.put("GoodsReceiptDetailid", row.getID());
            requestParams2.put("termtypeArry", termTypeArr);
            KwlReturnObject grdtmresult = accGoodsReceiptDAOObj.getGoodsReceiptdetailTermMap(requestParams2);
            if(grdtmresult.getEntityList().size()>0){

                JSONObject jtemp = new JSONObject();
                jtemp.put("entryno", entrynoCounter++);
                jtemp.put("date", enddate);
                jtemp.put("invoiceno", row.getGoodsReceipt().getGoodsReceiptNumber());
//                jtemp.put("invoicedate", row.getGoodsReceipt().getJournalEntry().getEntryDate());
                jtemp.put("invoicedate", row.getGoodsReceipt().getCreationDate());
                jtemp.put("vendorname", row.getGoodsReceipt().getVendor().getName());
                jtemp.put("servicetaxregno", row.getGoodsReceipt().getVendor().getSERVICEnumber());
                jtemp.put("descriptionofservice", row.getInventory().getProduct().getDescription());
                jtemp.put("typeofservice", "Input Service");
                jtemp.put("invoiceamount", row.getGoodsReceipt().getInvoiceAmount());

                double serviceTaxAmt = 0.0, sbcTaxAmt = 0.0, kkcTaxAmt = 0.0;
                double utilizedServiceTaxAmt = 0.0, utilizedSBCTaxAmt = 0.0, utilizedKKCTaxAmt = 0.0;
                double balanceServiceTaxAmt = 0.0, balanceSBCTaxAmt = 0.0, balanceKKCTaxAmt = 0.0;

                Iterator grdtmitr = grdtmresult.getEntityList().iterator();
                while (grdtmitr.hasNext()) {
                    ReceiptDetailTermsMap grdtmrow = (ReceiptDetailTermsMap) grdtmitr.next();

                    if(grdtmrow.getTerm().getTermType() == IndiaComplianceConstants.LINELEVELTERMTYPE_SERVICE_TAX){
                        serviceTaxAmt = grdtmrow.getTermamount();
                        if(grdtmrow.getCreditAvailedFlagServiceTax()== 1){
                            utilizedServiceTaxAmt = grdtmrow.getTermamount();
                        }
                    } else if(grdtmrow.getTerm().getTermType() == IndiaComplianceConstants.LINELEVELTERMTYPE_SBC){
                        sbcTaxAmt = grdtmrow.getTermamount();
                        if(grdtmrow.getCreditAvailedFlagServiceTax() == 1){
                            utilizedSBCTaxAmt = grdtmrow.getTermamount();
                        }
                    } else if(grdtmrow.getTerm().getTermType() == IndiaComplianceConstants.LINELEVELTERMTYPE_KKC){
                        kkcTaxAmt = grdtmrow.getTermamount();
                        if(grdtmrow.getCreditAvailedFlagServiceTax() == 1){
                            utilizedKKCTaxAmt = grdtmrow.getTermamount();
                        }
                    }
                }

                HashMap<String, Object> requestParams3 = new HashMap<String, Object>();
                requestParams3.put("companyid", companyId);
                requestParams3.put("grid", row.getGoodsReceipt().getID());
                KwlReturnObject paymentDetailresult = accVendorPaymentDao.getPaymentsFromGReceipt(requestParams3);
                if(paymentDetailresult.getEntityList().size()>0){
                    Iterator paymentDetailitr = paymentDetailresult.getEntityList().iterator();
                    while (paymentDetailitr.hasNext()) {
                        PaymentDetail paymentDetailRow = (PaymentDetail) paymentDetailitr.next();
                        jtemp.put("paymentno", paymentDetailRow.getPayment().getPaymentNumber());
//                        jtemp.put("paymentdate", paymentDetailRow.getPayment().getJournalEntry().getEntryDate());
                        jtemp.put("paymentdate", paymentDetailRow.getPayment().getCreationDate());
                    }
                } else {
                    KwlReturnObject linkDetailPaymentresult = accVendorPaymentDao.getLinkedDetailsPayment(requestParams3);
                    if(linkDetailPaymentresult.getEntityList().size()>0){
                        Iterator linkDetailPaymentitr = linkDetailPaymentresult.getEntityList().iterator();
                        while (linkDetailPaymentitr.hasNext()) {
                            LinkDetailPayment linkDetailPaymentRow = (LinkDetailPayment) linkDetailPaymentitr.next();
                            jtemp.put("paymentno", linkDetailPaymentRow.getPayment().getPaymentNumber());
//                            jtemp.put("paymentdate", linkDetailPaymentRow.getPayment().getJournalEntry().getEntryDate());
                            jtemp.put("paymentdate", linkDetailPaymentRow.getPayment().getCreationDate());
                        }
                    }
                }
                balanceServiceTaxAmt = previousBalanceServiceTaxAmt + serviceTaxAmt - utilizedServiceTaxAmt;
                balanceSBCTaxAmt = previousBalanceSBCTaxAmt + sbcTaxAmt - utilizedSBCTaxAmt;
                balanceKKCTaxAmt = previousBalanceKKCTaxAmt + kkcTaxAmt - utilizedKKCTaxAmt;

                jtemp.put("servicetax", serviceTaxAmt);
                jtemp.put("sbc", sbcTaxAmt);
                jtemp.put("kkc", kkcTaxAmt);
                jtemp.put("openingbalanceservicetax", previousBalanceServiceTaxAmt);
                jtemp.put("openingbalancesbc", previousBalanceSBCTaxAmt);
                jtemp.put("openingbalancekkc", previousBalanceKKCTaxAmt);
                jtemp.put("credittakenservicetax", serviceTaxAmt);
                jtemp.put("credittakensbc", sbcTaxAmt);
                jtemp.put("credittakenkkc", kkcTaxAmt);
                jtemp.put("creditutilizedservicetax", utilizedServiceTaxAmt);
                jtemp.put("creditutilizedsbc", utilizedSBCTaxAmt);
                jtemp.put("creditutilizedkkc", utilizedKKCTaxAmt);
                jtemp.put("balanceservicetax", balanceServiceTaxAmt);
                jtemp.put("balancesbc", balanceSBCTaxAmt);
                jtemp.put("balancekkc", balanceKKCTaxAmt);
                previousBalanceServiceTaxAmt = balanceServiceTaxAmt;
                previousBalanceSBCTaxAmt = balanceSBCTaxAmt;
                previousBalanceKKCTaxAmt = balanceKKCTaxAmt;
                dataArr.put(jtemp);
            }
        }
        return dataArr;
    }
    
     public ModelAndView generateGIORFileForUOBBank(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("UOB_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            accReportsService.generateIBGFileForUOBbank(request, response);
            issuccess = true;
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }
     
     public ModelAndView getLedgerDetails(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        JSONArray dataArr = new JSONArray();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("isexportledgerflag",true);
            jobj = accReportsService.getLedgerDetails(paramJobj);
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("valid", true);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsCombineController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     
    public ModelAndView generateGIROFileForOCBCBank(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String filename = "GIROFASTWINV";
            String fileExtention = ".TXT";
            JSONObject paramJObj = StringUtil.convertRequestToJsonObject(request);
            String companyId = paramJObj.optString(Constants.companyKey);
            String[] paymentIds = paramJObj.optString("payments").split(",");
            String giroFileContent = accReportsService.generateIBGFileForOCBCBank(paramJObj);

            // Writing a GIRO file
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(giroFileContent.getBytes());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + fileExtention + "\"");
            response.setContentType("application/octet-stream");
            response.setContentLength(baos.size());
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();
            response.getOutputStream().close();
            
            accReportsService.updatePayment(paymentIds, companyId);//To update giro generation status.
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jObj.put(Constants.RES_success, issuccess);
                jObj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jObj.toString());
    }
}
