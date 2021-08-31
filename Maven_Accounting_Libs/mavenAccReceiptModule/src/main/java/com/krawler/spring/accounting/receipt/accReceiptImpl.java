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
package com.krawler.spring.accounting.receipt;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.hql.accounting.ReceiptDetail;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAOImpl;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceImpl;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class accReceiptImpl extends BaseDAO implements accReceiptDAO {
    
    private accCurrencyDAO accCurrencyDAOobj;
    public void setAccCurrencyDAOobj(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    
    public KwlReturnObject getaccountdetailsReceipt(String accid) throws ServiceException {
        List ll = new ArrayList();
        try {
            // insert new entry
            String accountid = accid;
            String query = "from Customer WHERE ID= ? ";
            ll = executeQuery(query, new Object[]{accountid});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.createTemplate", e);
        }

        return new KwlReturnObject(true, "Account Details received successfully", null, ll, ll.size());

    }

    @Override
    public KwlReturnObject saveReceipt(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            Receipt receipt = getReceiptObj(hm);
            boolean isEdit = false;
            String receiptid = (String) hm.get("receiptid");
            boolean mainPaymentForCNDNFlag = receipt.isMainPaymentForCNDNFlag();
            saveOrUpdate(receipt);
            if (mainPaymentForCNDNFlag && hm.containsKey("mainPaymentForCNDNId")) {
                Receipt mainPayment = hm.get("mainPaymentForCNDNId") == null ? null : (Receipt) get(Receipt.class, (String) hm.get("mainPaymentForCNDNId"));
                mainPayment.setIsCnDnAndInvoicePayment(true);
                mainPayment.setCndnAndInvoiceId(receipt.getID());
                saveOrUpdate(receipt);
            }
            
            if (hm.containsKey("isEdit") && (Boolean) hm.get("isEdit") && hm.containsKey("invoiceadvcndntype") && (Integer) hm.get("invoiceadvcndntype") == 1) {
                List<Receipt> paymentList = new ArrayList<Receipt>();
                if (hm.containsKey("paymentHashMap")) {
                    HashMap<Integer, String> paymentHashMap = (HashMap<Integer, String>) hm.get("paymentHashMap");
                    if (!paymentHashMap.containsKey(1) && paymentHashMap.containsKey(3) && paymentHashMap.containsKey(2)) {
                        Receipt advancePayment = paymentHashMap.containsKey(2) ? (Receipt) get(Receipt.class, (String) paymentHashMap.get(2)) : null;
                        Receipt cnDnPayment = paymentHashMap.containsKey(3) ? (Receipt) get(Receipt.class, (String) paymentHashMap.get(3)) : null;
                        if (advancePayment != null) {
                            receipt.setAdvanceid(advancePayment);
                            receipt.setCndnAndInvoiceId(null);
                            receipt.setAdvanceamount(advancePayment.getDepositAmount()-receipt.getDepositAmount());
                            cnDnPayment.setAdvanceamount(advancePayment.getDepositAmount()-receipt.getDepositAmount());
                            paymentList.add(advancePayment);
                        }
                        if (advancePayment != null && cnDnPayment != null) {
                            cnDnPayment.setAdvanceid(receipt.getAdvanceid());
                        }
                        if (cnDnPayment != null) {
                            receipt.setIsCnDnAndInvoicePayment(true);
                            receipt.setCndnAndInvoiceId(cnDnPayment.getID());
                            cnDnPayment.setIsCnDnAndInvoicePayment(true);
                            cnDnPayment.setCndnAndInvoiceId(receipt.getID());
                            cnDnPayment.setReceipttype(receipt.getReceipttype());
                            paymentList.add(cnDnPayment);
                        }
                        paymentList.add(receipt);
                    }
                }
                       saveAll(paymentList);
            }
            
            list.add(receipt);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.saveReceipt : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Receipt has been updated successfully", null, list, list.size());
    }
    @Override
    public Receipt getReceiptObj(HashMap<String, Object> hm) throws ServiceException,AccountingException {
        List list = new ArrayList();
        Receipt receipt = null;
        String companyid = "";
        try {
            boolean isEdit = false;
            String receiptid = (String) hm.get("receiptid");
            boolean mainPaymentForCNDNFlag = false;
            if (hm.containsKey("companyid")) {
                companyid = (String) hm.get("companyid");
            }
            if (StringUtil.isNullOrEmpty(receiptid)) {
                receipt = new Receipt();
                receipt.setDeleted(false);
                if (hm.containsKey("createdby")) {
                    User createdby = hm.get("createdby") == null ? null : (User) get(User.class, (String) hm.get("createdby"));
                    receipt.setCreatedby(createdby);
                }
                if (hm.containsKey("modifiedby")) {
                    User modifiedby = hm.get("modifiedby") == null ? null : (User) get(User.class, (String) hm.get("modifiedby"));
                    receipt.setModifiedby(modifiedby);
                }
                if (hm.containsKey("createdon")) {
                    receipt.setCreatedon((Long) hm.get("createdon"));
                }
                if (hm.containsKey("updatedon")) {
                    receipt.setUpdatedon((Long) hm.get("updatedon"));
                }
            } else {
                receipt = (Receipt) get(Receipt.class, receiptid);
                isEdit = true;
                if (hm.containsKey("modifiedby")) {
                    User modifiedby = hm.get("modifiedby") == null ? null : (User) get(User.class, (String) hm.get("modifiedby"));
                    receipt.setModifiedby(modifiedby);
                }
                if (hm.containsKey("updatedon")) {
                    receipt.setUpdatedon((Long) hm.get("updatedon"));
                }
                if (hm.containsKey(Constants.MARKED_PRINTED)) {
                    receipt.setPrinted(Boolean.parseBoolean((String) hm.get(Constants.MARKED_PRINTED)));
                }
            }
            receipt.setMainPaymentForCNDNFlag(false);
            if (hm.containsKey(Constants.SEQFORMAT)) {
                receipt.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) hm.get(Constants.SEQFORMAT)));
            }
            if (hm.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullOrEmpty(hm.get(Constants.SEQNUMBER).toString())) {
                receipt.setSeqnumber(Integer.parseInt(hm.get(Constants.SEQNUMBER).toString()));
            }
            if (hm.containsKey(Constants.DATEPREFIX) && hm.get(Constants.DATEPREFIX) != null) {
                receipt.setDatePreffixValue((String) hm.get(Constants.DATEPREFIX));
            }
            if (hm.containsKey(Constants.DATEAFTERPREFIX) && hm.get(Constants.DATEAFTERPREFIX) != null) {
                receipt.setDateAfterPreffixValue((String) hm.get(Constants.DATEAFTERPREFIX));
            }
            if (hm.containsKey(Constants.DATESUFFIX) && hm.get(Constants.DATESUFFIX) != null) {
                receipt.setDateSuffixValue((String) hm.get(Constants.DATESUFFIX));
            }
            if (hm.containsKey("entrynumber")) {
                receipt.setReceiptNumber((String) hm.get("entrynumber"));
            }
            
            if (hm.containsKey("nonRefundable")) {
                receipt.setNonRefundable((Boolean) hm.get("nonRefundable"));
            }

            if (hm.containsKey(Constants.generatedSource) && hm.get(Constants.generatedSource) != null) {
                receipt.setGeneratedSource((Integer) hm.get(Constants.generatedSource));
            }
            
            if (hm.containsKey("autogenerated")) {
                receipt.setAutoGenerated((Boolean) hm.get("autogenerated"));
            }
            if (hm.containsKey("memo")) {
                receipt.setMemo((String) hm.get("memo"));
            }
            if (hm.containsKey("externalCurrencyRate")) {
                receipt.setExternalCurrencyRate((Double) hm.get("externalCurrencyRate"));
            }
            if (hm.containsKey("paydetailsid")) {
                PayDetail pd = hm.get("paydetailsid") == null ? null : (PayDetail) get(PayDetail.class, (String) hm.get("paydetailsid"));
                receipt.setPayDetail(pd);
            }
            if (hm.containsKey("companyid")) {
                Company company = hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid"));
                receipt.setCompany(company);
            }
            if (hm.containsKey("currencyid")) {
                KWLCurrency currency = hm.get("currencyid") == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) hm.get("currencyid"));
                receipt.setCurrency(currency);
            }
            if (hm.containsKey("journalentryid")) {
                JournalEntry je = hm.get("journalentryid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) hm.get("journalentryid"));
                receipt.setJournalEntry(je);
            }
            if (hm.containsKey("deposittojedetailid")) {
                JournalEntryDetail jed = hm.get("deposittojedetailid") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("deposittojedetailid"));
                receipt.setDeposittoJEDetail(jed);
            }
            if (hm.containsKey("openingBalanceReceiptCustomData")) {
                    OpeningBalanceReceiptCustomData openingBalanceReceiptCustomData = hm.get("openingBalanceReceiptCustomData") == null ? null : (OpeningBalanceReceiptCustomData) get(OpeningBalanceReceiptCustomData.class, (String) hm.get("openingBalanceReceiptCustomData"));
                    receipt.setOpeningBalanceReceiptCustomData(openingBalanceReceiptCustomData);
            }
            if (hm.containsKey("depositamount") && !StringUtil.isNullOrEmpty(hm.get("depositamount").toString())) {
                receipt.setDepositAmount(Double.parseDouble(hm.get("depositamount").toString()));
            }
            if (hm.containsKey("receiptdetails")) {
                receipt.setRows((Set<ReceiptDetail>) hm.get("receiptdetails"));
            }
            if (hm.containsKey("contraentry")) {
                receipt.setContraentry((Boolean) hm.get("contraentry"));
            }
            if (hm.containsKey("isLinkedToClaimedInvoice") && hm.get("isLinkedToClaimedInvoice") != null) {
                receipt.setLinkedToClaimedInvoice((Boolean) hm.get("isLinkedToClaimedInvoice"));
            }
            if (hm.containsKey("isadvancepayment")) {
                receipt.setIsadvancepayment((Boolean) hm.get("isadvancepayment"));
            }
            if (hm.containsKey("isadvanceFromVendor")) {
                receipt.setIsadvancefromvendor((Boolean) hm.get("isadvanceFromVendor"));
            }
            if (hm.containsKey("advanceamounttype")) {
                receipt.setAdvanceamounttype((Integer) hm.get("advanceamounttype"));
            }
            if (hm.containsKey("receipttype")) {
                receipt.setReceipttype((Integer) hm.get("receipttype"));
            }
            if (hm.containsKey("vendor")) {
                receipt.setVendor((String) hm.get("vendor"));
            }
            if (hm.containsKey("advanceid")) {
                Receipt advReceipt = hm.get("advanceid") == null ? null : (Receipt) get(Receipt.class, (String) hm.get("advanceid"));
                receipt.setAdvanceid(advReceipt);
            }
            if (hm.containsKey("advanceamount")) {
                receipt.setAdvanceamount((Double) hm.get("advanceamount"));
            }

            if (hm.containsKey("advancePaymentIdForCnDn")) {
                Receipt advReceipt = hm.get("advancePaymentIdForCnDn") == null ? null : (Receipt) get(Receipt.class, (String) hm.get("advancePaymentIdForCnDn"));
                receipt.setAdvanceid(advReceipt);
                receipt.setAdvanceamount(advReceipt.getDepositAmount());
            }
            if (hm.containsKey("mainPaymentForCNDNId") && hm.containsKey("isadvancepayment") && !(Boolean) hm.get("isadvancepayment")) {
                receipt.setIsCnDnAndInvoicePayment(true);
                receipt.setCndnAndInvoiceId((String) hm.get("mainPaymentForCNDNId"));
                mainPaymentForCNDNFlag = true;
                receipt.setMainPaymentForCNDNFlag(true);
            }
            if (hm.containsKey("invoiceadvcndntype")) {
                receipt.setInvoiceAdvCndnType((Integer) hm.get("invoiceadvcndntype"));
            }

            if (hm.containsKey("ismanydbcr")) {
                receipt.setIsmanydbcr((Boolean) hm.get("ismanydbcr"));
            }
            if (hm.containsKey("bankCharges") && !StringUtil.isNullOrEmpty(hm.get("bankCharges").toString())) {
                receipt.setBankChargesAmount(Double.parseDouble(hm.get("bankCharges").toString()));
            }
            if (hm.containsKey("bankChargesCmb")) {
                Account bankChargesAccount = hm.get("bankChargesCmb") == null ? null : (Account) get(Account.class, (String) hm.get("bankChargesCmb"));
                receipt.setBankChargesAccount(bankChargesAccount);
            }
            if (hm.containsKey("bankInterest") && !StringUtil.isNullOrEmpty(hm.get("bankInterest").toString())) {
                receipt.setBankInterestAmount(Double.parseDouble(hm.get("bankInterest").toString()));
            }
            if (hm.containsKey("linkDetails")) {
                receipt.setLinkDetailReceipts((Set<LinkDetailReceipt>) hm.get("linkDetails"));
            }
            if (hm.containsKey("linkWithDebitNoteDetails")) {
                receipt.setLinkDetailReceiptsToDebitNote((Set<LinkDetailReceiptToDebitNote>) hm.get("linkWithDebitNoteDetails"));
            }
            if (hm.containsKey("bankInterestCmb")) {
                Account bankInterestAccount = hm.get("bankInterestCmb") == null ? null : (Account) get(Account.class, (String) hm.get("bankInterestCmb"));
                receipt.setBankInterestAccount(bankInterestAccount);
            }
            if (hm.containsKey("tax") && hm.get("tax") != null) {
                Tax tax = hm.get("tax") == null ? null : (Tax) get(Tax.class, (String) hm.get("tax"));
                receipt.setTax(tax);
            }
            if (hm.containsKey("taxAmount") && hm.get("taxAmount") != null) {
                receipt.setTaxAmount((Double) hm.get("taxAmount"));
            }
            if (hm.containsKey("paidToCmb")) {
                MasterItem paidToCmb = hm.get("paidToCmb") == null ? null : (MasterItem) get(MasterItem.class, (String) hm.get("paidToCmb"));
                receipt.setReceivedFrom(paidToCmb);
            }
            if (hm.containsKey("accountId")) {
                Account account = hm.get("accountId") == null ? null : (Account) get(Account.class, (String) hm.get("accountId"));
                receipt.setAccount(account);
            }
            if (hm.containsKey("chequeNumber")) {//
                receipt.setChequeNumber((String) hm.get("chequeNumber"));
            }
            if (hm.containsKey("drawnOn")) {//
                receipt.setDrawnOn((String) hm.get("drawnOn"));
            }
            if (hm.containsKey("creationDate")) {//
                receipt.setCreationDate((Date) hm.get("creationDate"));
            }
            if (hm.containsKey("chequeDate")) {//
                receipt.setChequeDate((Date) hm.get("chequeDate"));
            }
            if (hm.containsKey("customerId")) {//
                String customerId = (String) hm.get("customerId");
                Customer customer = (Customer) get(Customer.class, customerId);
                receipt.setCustomer(customer);
            }
            if (hm.containsKey("isOpeningBalenceReceipt") && hm.get("isOpeningBalenceReceipt")!=null) {//
                boolean isOpeningReceipt = (Boolean) hm.get("isOpeningBalenceReceipt");
                receipt.setIsOpeningBalenceReceipt(isOpeningReceipt);
                if(isOpeningReceipt){
                    receipt.setApprovestatuslevel(11);//Since Approval flow is not available for opening receipt Henece Approval level will be always 11
                }
            }
            if (hm.containsKey("openingBalanceAmountDue") && !StringUtil.isNullOrEmpty(hm.get("openingBalanceAmountDue").toString())) {
                receipt.setOpeningBalanceAmountDue(Double.parseDouble(hm.get("openingBalanceAmountDue").toString()));
            }
            if (hm.containsKey(Constants.openingBalanceBaseAmountDue) && !StringUtil.isNullOrEmpty(hm.get(Constants.openingBalanceBaseAmountDue).toString())) {
                receipt.setOpeningBalanceBaseAmountDue(authHandler.round(Double.parseDouble(hm.get(Constants.openingBalanceBaseAmountDue).toString()), companyid));
            }
            if (hm.containsKey(Constants.originalOpeningBalanceBaseAmount) && !StringUtil.isNullOrEmpty(hm.get(Constants.originalOpeningBalanceBaseAmount).toString())) {
                receipt.setOriginalOpeningBalanceBaseAmount(authHandler.round(Double.parseDouble(hm.get(Constants.originalOpeningBalanceBaseAmount).toString()), companyid));
            }
            if (hm.containsKey("normalReceipt")) {//
                receipt.setNormalReceipt((Boolean) hm.get("normalReceipt"));
            } else {
                if (!isEdit) {
                    receipt.setNormalReceipt(true);
                }
            }
            if (hm.containsKey("exchangeRateForOpeningTransaction")) {
                double exchangeRateForOpeningTransaction = (Double) hm.get("exchangeRateForOpeningTransaction");
                receipt.setExchangeRateForOpeningTransaction(exchangeRateForOpeningTransaction);
            }
            if (hm.containsKey("conversionRateFromCurrencyToBase")) {
                receipt.setConversionRateFromCurrencyToBase((Boolean) hm.get("conversionRateFromCurrencyToBase"));
            }
            if (hm.containsKey("revalJeId")) {
                receipt.setRevalJeId((String) hm.get("revalJeId"));
            }
            if(hm.containsKey("PaymentCurrencyToPaymentMethodCurrencyRate")){
                receipt.setPaymentcurrencytopaymentmethodcurrencyrate((Double)hm.get("PaymentCurrencyToPaymentMethodCurrencyRate"));
            }
            if (hm.containsKey("paymentWindowType")) {
                receipt.setPaymentWindowType((Integer) hm.get("paymentWindowType"));
            }
            if (hm.containsKey("lmsreceiptid") && !StringUtil.isNullOrEmpty((String)hm.get("lmsreceiptid"))) {
                receipt.setLmsReceiptID((String) hm.get("lmsreceiptid"));
            }
            if (hm.containsKey("linkWithAdvancePaymentDetails")) {
                receipt.setLinkDetailReceiptsToAdvancePayment((Set<LinkDetailReceiptToAdvancePayment>) hm.get("linkWithAdvancePaymentDetails"));
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.saveReceipt : " + ex.getMessage(), ex);
        }
        return receipt;
    }
    
    public KwlReturnObject saveReceiptObject(List<Receipt> receiptList) throws ServiceException {
        List list = new ArrayList();
        try {
            saveAll(receiptList);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.saveReceiptObject : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Payment has been updated successfully", null, receiptList, receiptList.size());
    }
    
    public KwlReturnObject deleteReceiptAdvanceDetails(String receiptid, String companyid) throws ServiceException {
        //Delete Payment Details
        String delQuery = "delete from ReceiptAdvanceDetail ad where ad.receipt.ID=? and ad.company.companyID=?";
        int numRows = executeUpdate(delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Advance Receipt has been deleted successfully.", null, null, numRows);
    }
    
    public KwlReturnObject getReceiptAdvanceAmountDueDetails(HashMap<String, Object> request) throws ServiceException {
        //Delete Payment Details
        List list = null;
        int count = 0;
        try {
            String start = (String) request.get("start");
            String limit = (String) request.get("limit");
            String ss = (String) request.get("ss");
            String customerid = (String) request.get("customerid");
            if (customerid == null) {
                customerid = (String) request.get("accid");
            }
            String companyid = (String) request.get("companyid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            String condition = " where r.company.companyID=? and r.approvestatuslevel=11 ";          //added r.approvestatuslevel=11 so that only approved records are displayed in reports
            
            /*
                Fetch receipts which have advance payment with amountdue > 0 
            */
            params.add(0d);
            condition += " and ad.amountDue > ? ";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"ac.name", "r.receiptNumber", "r.journalEntry.entryNumber", "r.memo", "r.payDetail.paymentMethod.methodName", "r.receivedFrom.value"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 6);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                searchQuery = searchQuery.substring(0, searchQuery.lastIndexOf(")"));   //removing last ')' to add to following customer and vendor search             
                searchQuery += " or (r.vendor in (select ID from Vendor where name like ?)) or (r.customer in (select ID from Customer where name like ?)))";
                params.add("%" + ss + "%");
                params.add("%" + ss + "%");
                condition += searchQuery;

//               params.add(ss+"%");
//               params.add(ss+"%");
//               params.add(ss+"%");
//               params.add(ss+"%");
//               params.add(ss+"%");
//               condition+= " and (ac.name like ? or r.receiptNumber like ? or r.journalEntry.entryNumber like ? or r.memo like ? or r.payDetail.paymentMethod.methodName like ?) ";
            }
            
             if (!StringUtil.isNullOrEmpty(customerid)) {
                    params.add(customerid);
                    condition += " and r.customer.ID=?";
             }
             /*
              * If non refundable check is true then in cross linking non refundable payment not shown in Advance payment window.
              * It's only for malaysian company
              */
                params.add(false);
                condition += " and r.nonRefundable=? ";
                
            /*
             * ERP-40387
             * documents marked as dishonoured also getting used to link transaction case
             * e.g. created RP advanced and marked as dishonoured still user can able to link that document to MP refund
             * therefore only records having 'isDishonouredCheque' as false are fetched     
             */
            params.add(false);    
            condition += " and r.isDishonouredCheque=?";
            
            /*
             * ERP-40513
             * e.g. Create MP advance in Pound
             * While creating PP refund take payment method
             * Change payment currency to USD
             * Now only USD currency documents loading
             * 'currencyfilterfortrans' is currencyid of payment currency and 'applyFilterOnCurrency' is true when 'isReceipt' flag is missing from request
             */                                    
            if (request.containsKey("applyFilterOnCurrency") && !StringUtil.isNullObject(request.get("applyFilterOnCurrency")) && Boolean.parseBoolean(request.get("applyFilterOnCurrency").toString())) {
                if (request.containsKey("currencyfilterfortrans") && !StringUtil.isNullObject(request.get("currencyfilterfortrans"))) {
                    condition += " and ad.receipt.currency.currencyID = ? ";
                    params.add(request.get("currencyfilterfortrans").toString());   
                }
            }
            // required in loading of Advance Receipts in Sales Order-ERP-39926
            if (request.containsKey(Constants.df) && request.get(Constants.df) != null) {
                DateFormat df = (DateFormat) request.get(Constants.df);
                String endDate = (String) request.get(Constants.REQ_enddate);
                endDate = StringUtil.DecodeText(endDate);

                if (request.containsKey(Constants.REQ_enddate) && request.get(Constants.REQ_enddate) != null) {//All ReceiptDetail between start date and end date 
                    condition += " and ad.receipt.creationDate <=? ";
                    params.add(df.parse(endDate));
                }
            }
            String query = "select distinct ad from ReceiptAdvanceDetail ad inner join ad.receipt r inner join r.journalEntry je  inner join je.details jed inner join jed.account ac left join r.receivedFrom rf "+ condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
            
        } catch (Exception ex) {
            Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
        
    }
    
     public KwlReturnObject deleteReceiptsDetailsAndUpdateAmountDue(String receiptid, String companyid,int beforeEditApprovalStatus) throws ServiceException {
        //Delete Payment Details
        String selQuery = "from ReceiptDetail pd where pd.receipt.ID=? and pd.company.companyID=?";
        List<ReceiptDetail> details=find(selQuery,new Object[]{receiptid, companyid});
        List<Invoice> invoiceList=new ArrayList<Invoice>();
        for(ReceiptDetail receiptDetail:details){
            Invoice invoice=receiptDetail.getInvoice();
            boolean isInvoiceIsClaimed = (invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered);
            if (isInvoiceIsClaimed) {
                invoice.setClaimAmountDue(invoice.getClaimAmountDue()+receiptDetail.getAmountInInvoiceCurrency());
            } else {
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyid);
                requestParams.put(Constants.globalCurrencyKey, invoice.getCurrency().getCurrencyID());
//                double discountAmtInInvoiceCurrency = authHandler.round(receiptDetail.getDiscountAmount() / receiptDetail.getExchangeRateForTransaction(), companyid);
//                double discountAmount = receiptDetail.getDiscountAmount();
//                double invoiceBaseAmountDue = totalInvoiceAmtPaidAndDiscountInBase;
                double discountAmtInInvoiceCurrency = receiptDetail.getDiscountAmountInInvoiceCurrency();
                double totalInvoiceAmtPaidAndDiscountInBase = 0d;
                double totalInvoiceAndDiscountAmtInInvoiceCurrency = (invoice.isNormalInvoice() ? invoice.getInvoiceamountdue() : invoice.getOpeningBalanceAmountDue()) + receiptDetail.getAmountInInvoiceCurrency()+ discountAmtInInvoiceCurrency;
                totalInvoiceAndDiscountAmtInInvoiceCurrency = authHandler.round(totalInvoiceAndDiscountAmtInInvoiceCurrency, companyid);
                if (invoice.isNormalInvoice()) {
                    double amountdue = invoice.getInvoiceamountdue();
                    /*
                     set status flag for amount due 
                     */
                    double amountdueforstatus = amountdue + receiptDetail.getAmountInInvoiceCurrency()+discountAmtInInvoiceCurrency;
                    if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                        invoice.setIsOpenReceipt(false);
                    } else {
                        invoice.setIsOpenReceipt(true);
                    }
                    if (beforeEditApprovalStatus==Constants.APPROVED_STATUS_LEVEL && receiptDetail.getReceipt().getApprovestatuslevel() <= Constants.APPROVED_STATUS_LEVEL) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalInvoiceAndDiscountAmtInInvoiceCurrency, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                        totalInvoiceAmtPaidAndDiscountInBase = authHandler.round(((Double) bAmt.getEntityList().get(0)),companyid);
                        invoice.setInvoiceamountdue(totalInvoiceAndDiscountAmtInInvoiceCurrency);
                        invoice.setInvoiceAmountDueInBase(totalInvoiceAmtPaidAndDiscountInBase);
                    }
                }
                if (beforeEditApprovalStatus == Constants.APPROVED_STATUS_LEVEL && receiptDetail.getReceipt().getApprovestatuslevel() <= Constants.APPROVED_STATUS_LEVEL) {
                    if (invoice.isIsOpeningBalenceInvoice()) {
                        double amountdue = invoice.getOpeningBalanceAmountDue();
                        /*
                         * set status flag for opening invoices
                         */
                        if(invoice.isConversionRateFromCurrencyToBase()){
                            totalInvoiceAmtPaidAndDiscountInBase = authHandler.round((totalInvoiceAndDiscountAmtInInvoiceCurrency * invoice.getExchangeRateForOpeningTransaction()), companyid);
                        }else{
                            totalInvoiceAmtPaidAndDiscountInBase = authHandler.round((totalInvoiceAndDiscountAmtInInvoiceCurrency / invoice.getExchangeRateForOpeningTransaction()), companyid);
                        }
                        double amountdueforstatus = amountdue + receiptDetail.getAmountInInvoiceCurrency()+discountAmtInInvoiceCurrency;
                        if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                            invoice.setIsOpenReceipt(false);
                        } else {
                            invoice.setIsOpenReceipt(true);
                        }
                    }
                    invoice.setOpeningBalanceAmountDue(totalInvoiceAndDiscountAmtInInvoiceCurrency);
                    invoice.setOpeningBalanceBaseAmountDue(totalInvoiceAmtPaidAndDiscountInBase);
                }
            }
            invoiceList.add(invoice);
        }
        if(!invoiceList.isEmpty()){
            saveAll(invoiceList);
        }
         return deleteReceiptDetails(receiptid, companyid);
    }
    
    
    public List<LinkDetailReceipt> getDeletedLinkedReceiptInvoices(Receipt receipt,List<String> linkedDetailInvoice, String companyid) throws ServiceException {
        String invoicelinkIDsVal = "";
        String receiptid = receipt.getID();
        for(String linkID : linkedDetailInvoice) {
            invoicelinkIDsVal = invoicelinkIDsVal.concat("'").concat(linkID).concat("',");
        }
        if(!StringUtil.isNullOrEmpty(invoicelinkIDsVal.toString())) {
            invoicelinkIDsVal = invoicelinkIDsVal.substring(0, invoicelinkIDsVal.length()-1);
        }
        String selQuery = "from LinkDetailReceipt pd where pd.receipt.ID=? and pd.company.companyID=? ";
        if(!StringUtil.isNullOrEmpty(invoicelinkIDsVal)) {
           selQuery = selQuery.concat(" and pd.id not in (" + invoicelinkIDsVal + ")");
        }
        List<LinkDetailReceipt> details = find(selQuery, new Object[]{receiptid, companyid});
        return details;
    }
    
     public KwlReturnObject deleteLinkReceiptsDetailsAndUpdateAmountDue(Map<String,Object> requestMap,String receiptid, String companyid,boolean tempCheck) throws ServiceException {
        //Delete Payment Details
        String selQuery = "from LinkDetailReceipt pd where pd.receipt.ID=? and pd.company.companyID=?";
        String rcurrenyid = requestMap.containsKey(Constants.globalCurrencyKey) && requestMap.get(Constants.globalCurrencyKey)!=null?(String)requestMap.get(Constants.globalCurrencyKey):"";
        List<LinkDetailReceipt> details=find(selQuery,new Object[]{receiptid, companyid});
        List<Invoice> invoiceList=new ArrayList<Invoice>();
        for(LinkDetailReceipt linkDetailReceipt:details){
            Invoice invoice=linkDetailReceipt.getInvoice();
            double rpExternalCurrencyRate = 0d;
            Date rpCreationDate = null;
            rpCreationDate = invoice.getCreationDate();
            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                rpExternalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
            } else {
//                rpCreationDate = invoice.getJournalEntry().getEntryDate();
                rpExternalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
            }
            String rpCurrencyID = invoice.getCurrency() != null ? invoice.getCurrency().getCurrencyID() : rcurrenyid;
            double amountPaidInbase = 0;
            double amountPaid=linkDetailReceipt.getAmountInInvoiceCurrency();
            if(invoice.isNormalInvoice()){
                double amountdue=invoice.getInvoiceamountdue();
                double amountdueinbase=invoice.getInvoiceAmountDueInBase();
                KwlReturnObject rpAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestMap, amountPaid, rpCurrencyID, rpCreationDate, rpExternalCurrencyRate);
                if (rpAmtInBaseResult != null) {
                    amountPaidInbase = authHandler.round((Double) rpAmtInBaseResult.getEntityList().get(0), companyid);
                }
                /*
                 set status flag for amount due 
                 */
                double amountdueforstatus = amountdue + linkDetailReceipt.getAmountInInvoiceCurrency();
                if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                    invoice.setIsOpenReceipt(false);
                } else {
                    invoice.setIsOpenReceipt(true);
                }
                invoice.setInvoiceamountdue(amountdue+linkDetailReceipt.getAmountInInvoiceCurrency());
                invoice.setInvoiceAmountDueInBase(authHandler.round(amountdueinbase + amountPaidInbase, companyid));
                if((amountdue+linkDetailReceipt.getAmountInInvoiceCurrency())!=0){
                    invoice.setAmountDueDate(null);
            }
            }
            if (invoice.isIsOpeningBalenceInvoice()) {
                double amountdue = invoice.getOpeningBalanceAmountDue();
                /*
                 * set status flag for opening invoices
                 */
                double amountdueforstatus = amountdue + linkDetailReceipt.getAmountInInvoiceCurrency();
                if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                    invoice.setIsOpenReceipt(false);
                } else {
                    invoice.setIsOpenReceipt(true);
                }
            }
            KwlReturnObject rpAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestMap, amountPaid, rpCurrencyID, rpCreationDate, rpExternalCurrencyRate);
            if (rpAmtInBaseResult != null) {
                amountPaidInbase = authHandler.round((Double) rpAmtInBaseResult.getEntityList().get(0), companyid);
            }
            invoice.setOpeningBalanceAmountDue(invoice.getOpeningBalanceAmountDue()+linkDetailReceipt.getAmountInInvoiceCurrency());
//            invoice.setOpeningBalanceBaseAmountDue(invoice.getOpeningBalanceBaseAmountDue()+linkDetailReceipt.getAmount());
            invoice.setOpeningBalanceBaseAmountDue(authHandler.round(invoice.getOpeningBalanceBaseAmountDue() + amountPaidInbase, companyid));
            invoiceList.add(invoice);
        }
        
        if(!invoiceList.isEmpty()){
            saveAll(invoiceList);
        }
        /*
         * tempCheck is for temporary delete receipt
         * if receipt is temporary delete then tempCheck is set as true
         */
         if (tempCheck) {
             return new KwlReturnObject(true, "Payment Details has been deleted successfully.", null, null, 0);
         } else {
             return deleteLinkReceiptDetails(receiptid, companyid);
         }
    }
     
     public KwlReturnObject deleteLinkReceiptsDetailsToDebitNoteAndUpdateAmountDue(String receiptid, String companyid,boolean tempCheck) throws ServiceException {
        //Delete Payment Details
        String selQuery = "from LinkDetailReceiptToDebitNote pd where pd.receipt.ID=? and pd.company.companyID=?";
        List<LinkDetailReceiptToDebitNote> details=find(selQuery,new Object[]{receiptid, companyid});
        List<DebitNote> dnList=new ArrayList<DebitNote>();
        for(LinkDetailReceiptToDebitNote linkDetailReceipt:details){
            DebitNote DN=linkDetailReceipt.getDebitnote();
            double amountdue=DN.getDnamountdue();
            DN.setDnamountdue(amountdue+linkDetailReceipt.getAmountInDNCurrency());
            DN.setOpeningBalanceAmountDue(DN.getOpeningBalanceAmountDue()+linkDetailReceipt.getAmountInDNCurrency());
            DN.setOpeningBalanceBaseAmountDue(DN.getOpeningBalanceBaseAmountDue()+linkDetailReceipt.getAmount());
            dnList.add(DN);
        }
        
        if(!dnList.isEmpty()){
            saveAll(dnList);
        }
        /*
         * tempCheck is for temporary delete receipt
         * if receipt is temporary delete then tempCheck is set as true
         */
         if (tempCheck) {
             return new KwlReturnObject(true, "Payment Details has been deleted successfully.", null, null, 0);
         } else {
             return deleteLinkReceiptToDebitNoteDetails(receiptid, companyid);
         }
    }
     
 
     
    public KwlReturnObject deleteReceiptsAgainstCNDN(String receiptId, String companyid,int beforeEditApprovalStatus) throws ServiceException {
        //Delete Payment Details
                    int numRows=0;
                    KwlReturnObject cnhistoryresult = getCustomerDnPaymenyHistory("", 0.0, 0.0, receiptId);
                    List<DebitNotePaymentDetails> dnHistoryList = cnhistoryresult.getEntityList();
                    for (DebitNotePaymentDetails dnpd:dnHistoryList) {
                        String dnid = dnpd.getDebitnote().getID();
                        Double dnpaidamount = dnpd.getAmountPaid();
                        Double dnPaidAmountInBaseCurrency =dnpd.getAmountInBaseCurrency();
                        if (beforeEditApprovalStatus == Constants.APPROVED_STATUS_LEVEL) {
                            KwlReturnObject cnjedresult = updateDnAmount(dnid, -dnpaidamount);
                            KwlReturnObject opencnjedresult = updateDnOpeningAmountDue(dnid, -dnpaidamount);
                            KwlReturnObject openingCnBaseAmtDueResult = updateDnOpeningBaseAmountDue(dnid, -dnPaidAmountInBaseCurrency);
                        }
                        String query = " delete from debitnotepayment where receiptid = ? and dnid = ? ";
                        numRows += executeSQLUpdate( query, new Object[]{receiptId,dnid});
                    }
        return new KwlReturnObject(true, "Amount has been updated successfully.", null, null, numRows);
    }

    public KwlReturnObject saveReceiptObject(Receipt receipt) throws ServiceException {
        List list = new ArrayList();
        try {
            saveOrUpdate(receipt);
            list.add(receipt);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.saveReceipt : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Receipt has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject getReceipts(HashMap<String, Object> request) throws ServiceException {
        List<Object> list = new ArrayList<Object>();
        List list1 = new ArrayList();
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String moduleid = "";
            if (request.containsKey(Constants.moduleid) && request.get(Constants.moduleid) != null) {
                moduleid = request.get(Constants.moduleid).toString();
            }
            String start = (String) request.get("start");
            String limit = (String) request.get("limit");
            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            boolean contraentryflag = false;
            /*
             * isExportPayment is true if export record from entry form 
             */ 
            boolean exportRecord = false;
            boolean isAged = (request.containsKey("isAged") && request.get("isAged") != null) ? Boolean.parseBoolean(request.get("isAged").toString()) : false;
            boolean isToFetchRecordLessEndDate = (request.containsKey("isToFetchRecordLessEndDate") && request.get("isToFetchRecordLessEndDate") != null) ? Boolean.parseBoolean(request.get("isToFetchRecordLessEndDate").toString()) : false;
            boolean isAgedPayables=false;//when request will come from aged payable report either summary or details this flag will be true.
            boolean getRecordBasedOnJEDate = false;
          
            /*
             fetch customer id and includeExcludeChildCmb value
             */
            String Customerid=(String) request.get("custVendorID");
            boolean includeExcludeChildCmb=false;
            if (request.containsKey("includeExcludeChildCmb") && request.get("includeExcludeChildCmb") != null) {
                includeExcludeChildCmb = (Boolean) request.get("includeExcludeChildCmb");
            }
             /*
             when request will come from payment report this flag will be true.
             */
            boolean isPaymentReport=false;
            if (request.containsKey("isPaymentReport") && request.get("isPaymentReport") != null) {
                isPaymentReport = (Boolean) request.get("isPaymentReport");
            }
            
         
            if(request.containsKey("isAgedPayables") && request.get("isAgedPayables")!=null && Boolean.parseBoolean(request.get("isAgedPayables").toString())){
                isAgedPayables = true;
            }
            if(request.containsKey("getRecordBasedOnJEDate") && request.get("getRecordBasedOnJEDate")!=null && Boolean.parseBoolean(request.get("getRecordBasedOnJEDate").toString())){
                getRecordBasedOnJEDate = true;
            }
            if (request.get("contraentryflag") != null) {
                contraentryflag = (Boolean) request.get("contraentryflag");
            }
            boolean isAdvancePayment = false;
            if (request.get("isadvancepayment") != null) {
                isAdvancePayment = (Boolean) request.get("isadvancepayment");
            }
            if(request.containsKey("exportRecord") && request.get("exportRecord")!=null && Boolean.parseBoolean(request.get("exportRecord").toString())){
                exportRecord = true;
            }
            boolean allAdvPayment=false;
            boolean onlyAdvAmountDue=false;
            boolean unUtilizedAdvPayment =false;
            boolean partiallyUtilizedAdvPayment = false;
            boolean nonorpartiallyUtilizedAdvPayment = false;           //ERM-85  added another filter Advance payment non or partially utilized.
            boolean fullyUtilizedAdvPayment = false;
            if (request.containsKey("allAdvPayment")) {
                allAdvPayment = (Boolean) request.get("allAdvPayment");
            }
            if (request.containsKey("onlyAdvAmountDue")) {
                onlyAdvAmountDue = (Boolean) request.get("onlyAdvAmountDue");
            }
            if (request.containsKey("unUtilizedAdvPayment")) {
                unUtilizedAdvPayment = (Boolean) request.get("unUtilizedAdvPayment");
            }
            if (request.containsKey("partiallyUtilizedAdvPayment")) {
                partiallyUtilizedAdvPayment = (Boolean) request.get("partiallyUtilizedAdvPayment");
            }
            if (request.containsKey("nonorpartiallyUtilizedAdvPayment")) {
                nonorpartiallyUtilizedAdvPayment = (Boolean) request.get("nonorpartiallyUtilizedAdvPayment");
            }
            if (request.containsKey("fullyUtilizedAdvPayment")) {
                fullyUtilizedAdvPayment = (Boolean) request.get("fullyUtilizedAdvPayment");
            }
            
            boolean isprinted = false;
            if (request.get(Constants.MARKED_PRINTED) != null) {
                isprinted = (Boolean) request.get(Constants.MARKED_PRINTED);
            }
            boolean isAdvanceFromVendor = false;
            if (request.get("isadvancefromvendor") != null) {
                isAdvanceFromVendor = (Boolean) request.get("isadvancefromvendor");
            }
            boolean isPostDatedCheque = false;
            if (request.get("isPostDatedCheque") != null) {
                isPostDatedCheque = (Boolean) request.get("isPostDatedCheque");
            }
            boolean isDishonouredCheque = false;
            if (request.get("isDishonouredCheque") != null) {
                isDishonouredCheque = (Boolean) request.get("isDishonouredCheque");
            }
            boolean isGlcode = false;
            if (request.get("isGlcode") != null) {
                isGlcode = (Boolean) request.get("isGlcode");
            }
            String vendorIdGroup = (String) request.get("custVendorID");
            if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
                vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
            }
            boolean isMonthlyAgeingReport = false;
            if (request.get("isMonthlyAgeingReport") != null) {
                isMonthlyAgeingReport = Boolean.parseBoolean(request.get("isMonthlyAgeingReport").toString());
            }
            String userDepartment = "";
            if (request.containsKey("userDepartment") && request.get("userDepartment") != null) {
                userDepartment = (String) request.get("userDepartment");
            }
            String newcustomerid = "";
            if (request.containsKey(Constants.newcustomerid) && request.get(Constants.newcustomerid) != null) {
                newcustomerid = (String) request.get(Constants.newcustomerid);
            } else if(request.containsKey("customerid") && request.get("customerid") != null){
                 newcustomerid = (String) request.get("customerid");
            }
            String newvendorid = "";
            if (request.containsKey(Constants.newvendorid) && request.get(Constants.newvendorid) != null) {
                newvendorid = (String) request.get(Constants.newvendorid);
            } else if(request.containsKey("vendorid") && request.get("vendorid") != null){
                newvendorid = (String) request.get("vendorid");
            }
            String ss = (String) request.get("ss");
            String companyid = (String) request.get("companyid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            String condition = " where r.company.companyID=?  and jed.debit=false ";
            condition += " and r.isOpeningBalenceReceipt=false ";
            String innerJoin = "";
            String linkReceiptJoin = "";
            
            
            if (!StringUtil.isNullOrEmpty(newcustomerid)) {
                if (newcustomerid.contains(",")) {
                    newcustomerid = AccountingManager.getFilterInString(newcustomerid);
                    condition += " and r.customer.ID IN" + newcustomerid;
                } else {
                    params.add(newcustomerid);
                    condition += " and r.customer.ID = ? ";
                }
            }

            if (!StringUtil.isNullOrEmpty(newvendorid)) {
                if (newvendorid.contains(",")) {
                    newvendorid = AccountingManager.getFilterInString(newvendorid);
                    condition += " and r.vendor IN" + newvendorid;
                } else {
                    params.add(newvendorid);
                    condition += " and r.vendor = ? ";

                }
            }
            /*
              perform operation for customer and include exclude child combobox selection
              if includeExcludechildCmb value is true it gives customer and its child
              otherwise only parent customer 
              when quick search is empty then it will be used
             */
            if (isPaymentReport) {
                if (!StringUtil.isNullOrEmpty(Customerid) && !Customerid.equals("All") && StringUtil.isNullOrEmpty(ss)) {
                    String[] customers = Customerid.split(",");
                    StringBuilder custValues = new StringBuilder();
                    for (String customer : customers) {
                        custValues.append("'").append(customer).append("',");
                    }
                    String custStr = custValues.substring(0, custValues.lastIndexOf(","));
                    if (includeExcludeChildCmb) {
                        condition += " and (r.customer.ID IN (" + custStr + ") or r.customer.parent IN (" + custStr + "))";
                    } else {
                        condition += " and r.customer.ID IN (" + custStr + ")";
                    }
                } else if (!includeExcludeChildCmb && StringUtil.isNullOrEmpty(ss)) {
                    condition += " and r.customer.parent is  null";
                }
            }else if (isAged && !isAgedPayables &&  !includeExcludeChildCmb && StringUtil.isNullOrEmpty(ss)) {
                    condition += " and r.customer.parent is  null";
                }
            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
                condition += " and r.currency.currencyID = ?";
                params.add(currencyfilterfortrans);
            }
            
            if (request.containsKey("groupcombo") && request.get("groupcombo") != null && request.containsKey(Constants.globalCurrencyKey) && request.get(Constants.globalCurrencyKey) != null) {
                int groupcombo = (Integer) request.get("groupcombo");

                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    condition += " and r.currency.currencyID=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                }  else if(groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    condition += " and r.currency.currencyID!=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                }
            }
            
            if(allAdvPayment || unUtilizedAdvPayment || partiallyUtilizedAdvPayment || fullyUtilizedAdvPayment || onlyAdvAmountDue || nonorpartiallyUtilizedAdvPayment){
                innerJoin += "  inner join r.receiptAdvanceDetails rad ";
            }
            if(unUtilizedAdvPayment){
               condition += " and rad.amount=rad.amountDue ";
            }
             if(partiallyUtilizedAdvPayment){
               condition += " and rad.amount!=rad.amountDue and rad.amountDue!=0";
            }
              if(fullyUtilizedAdvPayment){
               condition += " and rad.amountDue=0";
            }
            if (onlyAdvAmountDue) {
                condition += " and rad.amountDue > 0 ";
            }
            if(nonorpartiallyUtilizedAdvPayment){
               condition += " and ((rad.amount!=rad.amountDue and rad.amountDue!=0) or (rad.amount=rad.amountDue)) ";
            }
              
            
            if (!StringUtil.isNullOrEmpty(ss)) {
                linkReceiptJoin = " left join r.linkDetailReceipts lr ";
                String[] searchcol = new String[]{"ac.name", "r.receiptNumber", "r.journalEntry.entryNumber", "r.memo", "r.payDetail.paymentMethod.methodName", "r.receivedFrom.value","chk.chequeNo"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 7);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                searchQuery = searchQuery.substring(0, searchQuery.lastIndexOf(")"));   //removing last ')' to add to following customer and vendor search             
                /*
                when if condition true displaying parent and child customer payment transaction
                otherwise displaying only parent customer payment transaction
                */
                if (isPaymentReport) {
                    if (includeExcludeChildCmb) {
                        
                        searchQuery += " or (r.customer in (select ID from Customer where name like ? or aliasname like ?  or parent in (select r.customer from r where r.receiptNumber like ?) or parent in (select ID from Customer where name like ?) or parent in (select r.customer from r where r.memo like ?)or parent in (select r.customer from r where r.journalEntry.entryNumber like ?)or parent in (select r.customer from r where r.payDetail.paymentMethod.methodName like ?)or parent in (select r.customer from r where r.receivedFrom.value like ?))) or (r.vendor in (select ID from Vendor where name like ?or  aliasname like ?))";
                    } else {
                        
                        searchQuery += " or (r.customer in (select ID from Customer where name like ? or aliasname like ?)) or (r.vendor in (select ID from Vendor where name like ?or  aliasname like ? ))";
                    }
                } else {

                    searchQuery += " or (r.customer in (select ID from Customer where name like ? or aliasname like ?)) or (r.vendor in (select ID from Vendor where name like ?or  aliasname like ? ))";
                }
              
                searchQuery += " or (lr.linkedGainLossJE in (select ID from JournalEntry jn where jn.entryNumber like ? AND jn.company.companyID = ?))";
                searchQuery += " or (r.journalEntryForBankCharges.ID in (select ID from JournalEntry jn where jn.entryNumber like ? AND jn.company.companyID = ?))";
                searchQuery += " or (r.journalEntryForBankInterest.ID in (select ID from JournalEntry jn where jn.entryNumber like ? AND jn.company.companyID = ?)))";
               /*
                if both if are true add specific value into param for query
                otherwise add value to param for regular query
                */
                if(isPaymentReport) {
                    if(includeExcludeChildCmb){
                        params.add("%" + ss + "%");
                        params.add("%" + ss + "%");
                        params.add("%" + ss + "%");
                        params.add("%" + ss + "%");

                        params.add("%" + ss + "%"); 
                        params.add("%" + ss + "%");
                        params.add("%" + ss + "%");
                        params.add("%" + ss + "%"); 
                        params.add("%" + ss + "%"); 
                        params.add("%" + ss + "%"); 
                    }else{
                        params.add("%" + ss + "%");
                        params.add("%" + ss + "%");
                        params.add("%" + ss + "%");
                        params.add("%" + ss + "%");
                    }
               }
               else
               {
                    params.add("%" + ss + "%");
                    params.add("%" + ss + "%");
                    params.add("%" + ss + "%");
                    params.add("%" + ss + "%");
               }
                
                params.add("%" + ss + "%");
                params.add((String) request.get("companyid"));
                params.add("%" + ss + "%");
                params.add((String) request.get("companyid"));
                params.add("%" + ss + "%");
                params.add((String) request.get("companyid"));
                condition += searchQuery;

//               params.add(ss+"%");
//               params.add(ss+"%");
//               params.add(ss+"%");
//               params.add(ss+"%");
//               params.add(ss+"%");
//               condition+= " and (ac.name like ? or r.receiptNumber like ? or r.journalEntry.entryNumber like ? or r.memo like ? or r.payDetail.paymentMethod.methodName like ?) ";
            }
            if (nondeleted) {
                condition += " and r.deleted=false ";
            } else if (deleted) {
                condition += " and r.deleted=true ";
            }
            if (isprinted) {
                condition += " and r.printedflag=true";
            }

            if (contraentryflag) {
                condition += " and r.contraentry=true ";
            } else {
                condition += " and r.contraentry=false ";
            }

            if (isAdvancePayment) {
                condition += " and r.isadvancepayment=true ";
            }

            if(isAged){
                condition += " and r.isDishonouredCheque='F' ";
            }
            
            if (isAdvanceFromVendor) {
                condition += " and r.isadvancefromvendor=true and r.isadvancepayment=false ";//Normal advance payment from customer not need to show
            }
//            if (isGlcode) {
//                condition += " and (r.receipttype=9 or r.receipttype=2)";
//            }
            if(request.get("paymentWindowType")!=null){
                condition +=" and r.paymentWindowType = ? ";
                params.add(request.get("paymentWindowType"));
            }
            if (request.get("receipttype") != null) {
                String receipttypestr = (String) request.get("receipttype");
                String receipttypeArr[] = receipttypestr.split(",");
                condition += " and ( ";
                for (int i = 0; i < receipttypeArr.length; i++) {
                    if (i >= 1) {
                        condition += " or r.receipttype=? ";
                    } else {
                        condition += " r.receipttype=? ";
                    }
                    params.add(Integer.parseInt(receipttypeArr[i]));
                }
                condition += " )  ";
            }
            if (request.containsKey("linknumber") && request.get("linknumber") != null && !request.get("linknumber").toString().equals("")) {
                condition += " and r.receiptNumber = ? ";
                params.add(request.get("linknumber"));
            }

            if (isPostDatedCheque) {
                condition += " and r.payDetail.cheque.dueDate > now() ";
            }
            if (isDishonouredCheque) {
                condition += " and r.isDishonouredCheque='T' ";
            }
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and ((r.journalEntry.ID IN(" + jeIds + ")) or (r.journalEntryForBankCharges.ID IN (" + jeIds + ")) or (r.journalEntryForBankInterest.ID IN (" + jeIds + "))) ";
                innerJoin += " left join r.journalEntryForBankCharges bandchargeje left join r.journalEntryForBankInterest bandinterestje ";
            }
            String disHonouredJeIds = (String) request.get("disHonouredJeIds");
            if (!StringUtil.isNullOrEmpty(disHonouredJeIds)) {
                condition += " and r.disHonouredChequeJe.ID IN(" + disHonouredJeIds + ")";
            }
            
            String billid = (String) request.get("billid");     // handled comma separated billid's 
            if (!StringUtil.isNullOrEmpty(billid)) {
                billid = AccountingManager.getFilterInString(billid);
                condition += " and r.ID in " + billid +" ";
//                params.add(billid);
            }

            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            
            if((isAged || isToFetchRecordLessEndDate)&& !StringUtil.isNullOrEmpty(endDate) && !isMonthlyAgeingReport){ //Fetching all transactions whose creation date is upto end date for aged Report
                if (getRecordBasedOnJEDate) {
                    condition += " and r.journalEntry.entryDate <=? ";
                } else {
                    condition += " and r.creationDate <=? ";
                }
                params.add(df.parse(endDate));
            } else if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                startDate = StringUtil.DecodeText(startDate);
                endDate = StringUtil.DecodeText(endDate);
                if (getRecordBasedOnJEDate) {
                    condition += " and (r.journalEntry.entryDate >=? and r.journalEntry.entryDate <=?) ";
                } else {
                    condition += " and (r.creationDate >=? and r.creationDate <=?) ";
                }
                if (isMonthlyAgeingReport) {
                    Date startDate1 = (Date) request.get("MonthlyAgeingStartDate");
                    Date endDate1 = (Date) request.get("MonthlyAgeingEndDate");
                    params.add(startDate1);
                    params.add(endDate1);
                } else {
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
            }

             if (!StringUtil.isNullOrEmpty(userDepartment)) {
                params.add(userDepartment);
                condition += " and r.createdby.department = ? ";
               
            }
          
            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All") && !isPaymentReport) {
                if (isAdvanceFromVendor || isAgedPayables) {
                    condition += " and r.vendor in " + vendorIdGroup;
                } else {
                    condition += " and r.customer.ID in " + vendorIdGroup;
                }
            }
            String appendCase = "and";
            String mySearchFilterString = "";
//            String joinString = "";
            String joinString1 = "";
            boolean applyInvoiceSearch=false;
            HashMap<String, Object> reqParams1 = new HashMap<String, Object>();
            reqParams1.putAll(request);
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().trim().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            
            boolean ispendingAproval = false;
            if (request.containsKey("ispendingAproval") && request.get("ispendingAproval") != null) {
                ispendingAproval = Boolean.FALSE.parseBoolean(String.valueOf(request.get("ispendingAproval")));
            }
            
            if (!exportRecord) {
                if (ispendingAproval) { // Get only pending approved records
                    condition += " and r.approvestatuslevel != ?  and r.normalReceipt=? ";
                    params.add(11);
                    params.add(true);
                } else {// Get only approved records
                    condition += " and r.approvestatuslevel = ?  ";
                    params.add(11);
                }
            }
            
            String Searchjson = "";
            String searchDefaultFieldSQL="";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = request.get("searchJson").toString();
                Searchjson = StringUtil.DecodeText(Searchjson);
                if (!StringUtil.isNullOrEmpty(Searchjson)) {
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
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, moduleid, tableArray, filterConjuctionCriteria);
                        innerJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchDefaultFieldSQL=searchDefaultFieldSQL.replaceAll("receiptRef", "r");
                        searchDefaultFieldSQL=searchDefaultFieldSQL.replaceAll("receiptlinkingRef", "rl");
                        if (searchDefaultFieldSQL.contains("inv")) {
                            applyInvoiceSearch = true;
                        }
                    }
                    if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field
                        reqParams1.put(Constants.Searchjson, Searchjson);
                        reqParams1.put(Constants.appendCase, appendCase);
                        reqParams1.put("isPaymentFromInvoice", true);
                        reqParams1.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(reqParams1, true).get(Constants.myResult));
//                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(reqParams1, true).get(Constants.myResult));
                        if (mySearchFilterString.contains("accinvoicecustomdata")) {
                            applyInvoiceSearch = true;
                            mySearchFilterString = mySearchFilterString.replaceAll("accinvoicecustomdata", "invje.accBillInvCustomData");
                        }
                        mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "r.journalEntry.accBillInvCustomData");
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//        
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");
                        if (mySearchFilterString.contains("VendorCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("VendorCustomData", "r.vendor.accVendorCustomData");
                        }
                        if (mySearchFilterString.contains("CustomerCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "r.customer.accCustomerCustomData");
                        }
//                    joinString = " inner join je.accBillInvCustomData accjecustomdata ";
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
//                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
            }
            }
            
            String orderByCondition="";
            
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                orderByCondition = sortColumnPayment(Col_Name, Col_Dir, false);
            } else {
                if (getRecordBasedOnJEDate) {
                    orderByCondition = " order by r.journalEntry.entryDate desc";
                } else {
                    orderByCondition = " order by r.creationDate desc";
                }
            }
            if (applyInvoiceSearch) {
                String mainquery = "select r, ac from ReceiptLinking rl right join rl.DocID r inner join r.journalEntry je  inner join je.details jed inner join jed.account ac left join r.receivedFrom rf left join r.payDetail pd left join pd.cheque chk ";
                String paymentDetailJoin = "left join r.rows pde "
                        + " left join pde.invoice inv "
                        + "left join inv.journalEntry invje ";
                String linkDetailsJoin = " left join r.linkDetailReceipts lp "
                        + " left join lp.invoice inv "
                        + "left join inv.journalEntry invje ";
                String payquery = mainquery + innerJoin + paymentDetailJoin + condition + mySearchFilterString + " group by r " + orderByCondition;
                String linkQuery = mainquery + innerJoin + linkDetailsJoin + condition + mySearchFilterString + " group by r " + orderByCondition;
                list = executeQuery(payquery, params.toArray());
                count = list.size();
                list = executeQuery(linkQuery, params.toArray());
                count = count + list.size();
                list = new ArrayList();
                if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                    list1 = executeQueryPaging(payquery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                    list.addAll(list1);
                    list1 = executeQueryPaging(linkQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                    list.addAll(list1);
                } else {
                    list1 = executeQuery(payquery, params.toArray());
                    list.addAll(list1);
                    list1 = executeQuery(linkQuery, params.toArray());
                    list.addAll(list1);
                }
                /*
                 remove duplicate
                 */
                List<Object> finalList = new ArrayList<>();
                Map map = new HashMap();
                for (Object objectArr : list) {
                    Object[] row = (Object[]) objectArr;
                    Receipt receipt = (Receipt) row[0];
                    if (map.containsKey(receipt.getID())) {
                        continue;
                    } else {
                        map.put(receipt.getID(), receipt.getID());
                        finalList.add(objectArr);
                    }

                }
                list.clear();
                list.addAll(finalList);
            } else {
                String query = "select r, ac from ReceiptLinking rl right join rl.DocID r inner join r.journalEntry je " + linkReceiptJoin + " inner join je.details jed inner join jed.account ac left join r.receivedFrom rf left join r.payDetail pd left join pd.cheque chk " + innerJoin + condition + mySearchFilterString + " group by r " + orderByCondition;
                list = executeQuery(query, params.toArray());
                count = list.size();
                if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                    list = executeQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    @Override
    public KwlReturnObject getOpeningBalanceReceipts(HashMap<String, Object> request) throws ServiceException {
        List<Receipt> list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalReceipts = false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String start = (String) request.get("start");
        String limit = (String) request.get("limit");
        String customerid = (String) request.get("customerid");
        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }
        
        String currencyfilterfortrans= (request.containsKey("currencyfilterfortrans") && request.get("currencyfilterfortrans") != null) ? (String) request.get("currencyfilterfortrans"): null;
        boolean isAged = (request.containsKey("isAged") && request.get("isAged") != null) ? Boolean.parseBoolean(request.get("isAged").toString()) : false;
        
        boolean onlyAmountDue = (request.containsKey("onlyAmountDue") && request.get("onlyAmountDue") != null) ? Boolean.parseBoolean(request.get("onlyAmountDue").toString()) : false;
        boolean isAccountReceipts = false;
        if (request.containsKey("isAccountReceipts") && request.get("isAccountReceipts") != null) {
            isAccountReceipts = (Boolean) request.get("isAccountReceipts");
        }

        String condition = "";
        ArrayList params = new ArrayList();

        params.add(companyid);

        if (isAccountReceipts && request.containsKey("accountId") && request.get("accountId") != null) {
            String accountId = request.get("accountId").toString();
            condition += " AND r.account.ID=? ";
            params.add(accountId);
        } else if (!StringUtil.isNullOrEmpty(customerid)) {
            condition += " AND r.customer.ID=? ";
            params.add(customerid);
        }

        if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
            condition += " AND r.customer.ID IN " + vendorIdGroup;
        }

        if (request.get("excludeNormal") != null) {
            excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
        }

        if (excludeNormal) {
            condition += " AND r.normalReceipt=false ";
        }

        if (request.get("onlyOpeningNormalReceipts") != null) {
            onlyOpeningNormalReceipts = Boolean.parseBoolean(request.get("onlyOpeningNormalReceipts").toString());
        }

        if (onlyOpeningNormalReceipts) {
            condition += " AND r.normalReceipt=true ";
        }
        if (onlyAmountDue) {
            condition += " AND r.openingBalanceAmountDue > 0 ";
        }
        String endDateString = "";
        Date endDate = null;
        if (request.containsKey("MonthlyAgeingEndDate") && request.get("MonthlyAgeingEndDate") != null) {
            endDate = (Date) request.get("MonthlyAgeingEndDate");
        } else if (request.containsKey(Constants.REQ_enddate) && request.get(Constants.REQ_enddate) != null) {
            try {
                endDateString = (String) request.get(Constants.REQ_enddate);
                endDate = df.parse(endDateString);
            } catch (ParseException ex) {
                Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (isAged) {
           condition += " AND r.isDishonouredCheque='F' ";
            if (endDate != null) {
                condition += " AND r.creationDate <= ? ";
                params.add(endDate);
        }
        }
        
        if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
            condition += " AND r.currency.currencyID = ?";
            params.add(currencyfilterfortrans);
        }

        if (request.containsKey("groupcombo") && request.get("groupcombo") != null && request.containsKey(Constants.globalCurrencyKey) && request.get(Constants.globalCurrencyKey) != null) {
            int groupcombo = (Integer) request.get("groupcombo");

            if (groupcombo == Constants.AgedPayableBaseCurrency) {
                condition += " and r.currency.currencyID=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
            } else if (groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                condition += " and r.currency.currencyID!=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
            }
        }

        String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"r.customer.name","r.customer.aliasname","r.customer.acccode", "r.receiptNumber", "r.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 5); 
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND r.customer IS NOT NULL ";
            } catch (SQLException ex) {
                Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
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
                    try {
                        request.put(Constants.Searchjson, Searchjson);
                        request.put(Constants.appendCase, appendCase);
                        request.put("isOpeningBalance",true);
                        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "OpeningBalanceReceiptCustomData");
                        mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "r.customer.accCustomerCustomData");
                        mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceReceiptCustomData", "r.openingBalanceReceiptCustomData");
                        mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "r.openingBalanceReceiptCustomData");
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                        request.put("isOpeningBalance",false);
                    } catch (JSONException ex) {
                        Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ParseException ex) {
                        Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        String query = "Select r from Receipt r where r.isOpeningBalenceReceipt=true AND r.deleted=false AND r.company.companyID=?" + condition+mySearchFilterString;
        list = executeQuery( query, params.toArray());
        count = list.size();
        if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
            list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    
    @Override
    public int getOpeningBalanceReceiptCount(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalReceipts = false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        String customerid = (String) request.get("customerid");
        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }

        boolean isAccountReceipts = false;
        if (request.containsKey("isAccountReceipts") && request.get("isAccountReceipts") != null) {
            isAccountReceipts = (Boolean) request.get("isAccountReceipts");
        }

        String condition = "";
        ArrayList params = new ArrayList();

        params.add(companyid);

        if (isAccountReceipts && request.containsKey("accountId") && request.get("accountId") != null) {
            String accountId = request.get("accountId").toString();
            condition += " AND r.account.ID=? ";
            params.add(accountId);
        } else if (!StringUtil.isNullOrEmpty(customerid)) {
            condition += " AND r.customer.ID=? ";
            params.add(customerid);
        }

        if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
            condition += " AND r.customer.ID IN " + vendorIdGroup;
        }

        if (request.get("excludeNormal") != null) {
            excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
        }

        if (excludeNormal) {
            condition += " AND r.normalReceipt=false ";
        }

        if (request.get("onlyOpeningNormalReceipts") != null) {
            onlyOpeningNormalReceipts = Boolean.parseBoolean(request.get("onlyOpeningNormalReceipts").toString());
        }

        if (onlyOpeningNormalReceipts) {
            condition += " AND r.normalReceipt=true ";
        }

        String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"r.customer.name","r.customer.acccode", "r.receiptNumber", "r.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND r.customer IS NOT NULL ";
            } catch (SQLException ex) {
                Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String query = "Select count(r.ID) from Receipt r where r.isOpeningBalenceReceipt=true AND r.deleted=false AND r.company.companyID=?" + condition;
        list = executeQuery( query, params.toArray());
        Long totalCnt = 0l;
        if (list != null && !list.isEmpty()){
            totalCnt = (Long) list.get(0);
        }
        count = totalCnt.intValue();
        return count;
    }
    
    
    @Override
    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForReceipts(HashMap<String, Object> request) throws ServiceException {
        List<Receipt> list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalReceipts = false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String start = (String) request.get("start");
        String limit = (String) request.get("limit");
        String customerid = (String) request.get("customerid");
        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }

        boolean isAccountReceipts = false;
        if (request.containsKey("isAccountReceipts") && request.get("isAccountReceipts") != null) {
            isAccountReceipts = (Boolean) request.get("isAccountReceipts");
        }

        String condition = "";
        ArrayList params = new ArrayList();

        params.add(companyid);

        if (isAccountReceipts && request.containsKey("accountId") && request.get("accountId") != null) {
            String accountId = request.get("accountId").toString();
            condition += " AND r.account.ID=? ";
            params.add(accountId);
        } else if (!StringUtil.isNullOrEmpty(customerid)) {
            condition += " AND r.customer.ID=? ";
            params.add(customerid);
        }

        if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
            condition += " AND r.customer.ID IN " + vendorIdGroup;
        }

        if (request.get("excludeNormal") != null) {
            excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
        }

        if (excludeNormal) {
            condition += " AND r.normalReceipt=false ";
        }

        if (request.get("onlyOpeningNormalReceipts") != null) {
            onlyOpeningNormalReceipts = Boolean.parseBoolean(request.get("onlyOpeningNormalReceipts").toString());
        }

        if (onlyOpeningNormalReceipts) {
            condition += " AND r.normalReceipt=true ";
        }

        String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"r.customer.name","r.customer.acccode", "r.receiptNumber", "r.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND r.customer IS NOT NULL ";
            } catch (SQLException ex) {
                Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String query = "Select COALESCE(SUM(r.openingBalanceBaseAmountDue),0)  from Receipt r where r.isOpeningBalenceReceipt=true AND r.deleted=false AND r.company.companyID=?" + condition;
        list = executeQuery( query, params.toArray());
        count = list.size();
        return new KwlReturnObject(true, null, null, list, count);
    }
    
    
    @Override
    public KwlReturnObject getOpeningBalanceTotalBaseAmountForReceipts(HashMap<String, Object> request) throws ServiceException {
        List<Receipt> list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalReceipts = false;
        int count = 0;
        try {
            String companyid = (String) request.get("companyid");
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get("start");
            String limit = (String) request.get("limit");
            String customerid = (String) request.get("customerid");
            String vendorIdGroup = (String) request.get("custVendorID");
            if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
                vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
            }

            boolean isAccountReceipts = false;
            if (request.containsKey("isAccountReceipts") && request.get("isAccountReceipts") != null) {
                isAccountReceipts = (Boolean) request.get("isAccountReceipts");
            }

            String condition = "";
            ArrayList params = new ArrayList();

            params.add(companyid);

            if (isAccountReceipts && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                condition += " AND r.account.ID=? ";
                params.add(accountId);
            } else if (!StringUtil.isNullOrEmpty(customerid)) {
                condition += " AND r.customer.ID=? ";
                params.add(customerid);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND r.customer.ID IN " + vendorIdGroup;
            }

            if (request.get("excludeNormal") != null) {
                excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
            }

            if (excludeNormal) {
                condition += " AND r.normalReceipt=false ";
            }

            if (request.get("onlyOpeningNormalReceipts") != null) {
                onlyOpeningNormalReceipts = Boolean.parseBoolean(request.get("onlyOpeningNormalReceipts").toString());
            }

            if (onlyOpeningNormalReceipts) {
                condition += " AND r.normalReceipt=true ";
            }

            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                try {
                String[] searchcol = new String[]{"r.customer.name","r.customer.acccode", "r.receiptNumber", "r.account.name"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                    StringUtil.insertParamSearchString(map);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    condition += searchQuery + " AND r.customer IS NOT NULL ";
                } catch (SQLException ex) {
                    Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
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
            if (request.containsKey("Searchjson") && request.get("Searchjson") != null && !StringUtil.isNullOrEmpty((String) request.get("Searchjson"))) {
                Searchjson = request.get("Searchjson").toString();
                Searchjson = getJsornStringForSearch(Searchjson, companyid);
                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, appendCase);
                    request.put("isOpeningBalance", true);
                    request.put(Constants.moduleid, 16);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "r.openingBalanceReceiptCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceReceiptCustomData", "r.openingBalanceReceiptCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "r.openingBalanceReceiptCustomData");
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            String query = "";
            if (request.containsKey("Searchjson") && request.get("Searchjson") != null && !StringUtil.isNullOrEmpty((String) request.get("Searchjson"))) {
                if (!StringUtil.isNullOrEmpty(mySearchFilterString) && !mySearchFilterString.equals(" ")) {
                    query = "Select COALESCE(SUM(r.originalOpeningBalanceBaseAmount),0) from Receipt r where r.isOpeningBalenceReceipt=true AND r.deleted=false AND r.company.companyID=?" + condition + mySearchFilterString;
                    list = executeQuery( query, params.toArray());
                    count = list.size();
                }
            }else{
                query = "Select COALESCE(SUM(r.originalOpeningBalanceBaseAmount),0) from Receipt r where r.isOpeningBalenceReceipt=true AND r.deleted=false AND r.company.companyID=?" + condition + mySearchFilterString;
                list = executeQuery( query, params.toArray());
                count = list.size();
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
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
            int moduleId = 0;
            if (requestParams.containsKey("moduleid")) {
                moduleId = requestParams.get("moduleid") != null ? Integer.parseInt(requestParams.get("moduleid").toString()) : 0;
            }

            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }

            if (requestParams.containsKey("customfield") && (Integer) requestParams.get("customfield") != null) {
                hql += " and customfield = 1";
            }
            if (requestParams.containsKey("isActivated") && (Integer) requestParams.get("isActivated") != null) {
                int activatedFlag = (Integer) requestParams.get("isActivated");
                hql += " and isactivated = " + activatedFlag;
            }
            if (requestParams.containsKey("relatedmoduleid")) {
                hql += " and relatedmoduleid like '%" + requestParams.get("relatedmoduleid") + "%'";
            }
            if (requestParams.containsKey("parentid")) {
                hql += " and parentid = '" + requestParams.get("parentid") + "'";
            }
            if (requestParams.containsKey("checkForParent")) {
                hql += " and parentid is not null ";
            }
            if (moduleId != 0) {
                value.add(moduleId);
                hql += " and moduleid = ? ";
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

    public String getJsornStringForSearch(String Searchjson, String companyId) throws ServiceException {
        String returnStr = "";
        try {
            JSONArray jArray = new JSONArray();
            JSONObject jSONObject = new JSONObject();
            JSONObject jobjSearch = new JSONObject(Searchjson);
            int count = jobjSearch.getJSONArray(Constants.root).length();
            for (int i = 0; i < count; i++) {
                KwlReturnObject result = null;
                KwlReturnObject resultdata = null;
                JSONObject jobj1 = jobjSearch.getJSONArray(Constants.root).getJSONObject(i);
                String[] arr = null;
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("moduleid",16);
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel));
                requestParams.put(Constants.filter_values, Arrays.asList(companyId,StringUtil.DecodeText(jobj1.optString("columnheader"))));
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
                                requestParamsdata.put(Constants.filter_values, Arrays.asList(tmpcontyp.getId(),StringUtil.DecodeText(fieldComboData1.getValue())));
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
                    jobj.put("columnheader",StringUtil.DecodeText(jobj1.optString("columnheader")));
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
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }catch (Exception e) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnStr;
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
    
    @Override
    public KwlReturnObject getAllOpeningBalanceReceipts(HashMap<String, Object> request) throws ServiceException {

        List<Receipt> list = null;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String start = (String) request.get("start");
        String limit = (String) request.get("limit");
        String condition = "";
        String ss = "";
        try {
            
//        String customerid = (String) request.get("customerid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            
            if (request.containsKey("ss"))
                ss = (String) request.get("ss");
            
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                startDate = StringUtil.DecodeText(startDate);
                endDate = StringUtil.DecodeText(endDate);
            }
            
            String newcustomerid = "";
            if (request.containsKey(Constants.newcustomerid) && request.get(Constants.newcustomerid) != null) {
                newcustomerid = (String) request.get(Constants.newcustomerid);
            }
            boolean isPendingApproval = false;
            if (request.containsKey("ispendingAproval") && request.get("ispendingAproval") != null) {
                isPendingApproval = Boolean.parseBoolean(request.get("ispendingAproval").toString());
            }
            String newvendorid = "";
            if (request.containsKey(Constants.newvendorid) && request.get(Constants.newvendorid) != null) {
                newvendorid = (String) request.get(Constants.newvendorid);
            }
            
            
            if (!StringUtil.isNullOrEmpty(newcustomerid)) {
                if (newcustomerid.contains(",")) {
                    newcustomerid = AccountingManager.getFilterInString(newcustomerid);
                    condition += " and r.customer.ID IN" + newcustomerid;
                } else {
                    params.add(newcustomerid);
                    condition += " and r.customer.ID = ? ";
                }
            }
            
            String billid = (String) request.get("billid");
            if (!StringUtil.isNullOrEmpty(billid)) {
                billid = AccountingManager.getFilterInString(billid);
                condition += " and r.ID in " + billid +" ";
            }

            if (!StringUtil.isNullOrEmpty(newvendorid)) {
                condition += " and r.vendor = ? ";
                params.add(newvendorid);
            }
            if(request.containsKey("paymentWindowType") && request.get("paymentWindowType")!=null){
                if(request.get("paymentWindowType").toString().equalsIgnoreCase("1")){
                    condition += " and r.customer is not null ";
                }else if(request.get("paymentWindowType").toString().equalsIgnoreCase("2")){
                    condition += " and r.vendor is not null ";
                }
            }
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (r.creationDate >=? and r.creationDate <=?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"r.account.name", "r.receiptNumber"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                searchQuery = searchQuery.substring(0, searchQuery.lastIndexOf(")"));   //removing last ')' to add to following customer and vendor search             
                searchQuery += " or (r.vendor in (select ID from Vendor where name like ?)) or (r.customer in (select ID from Customer where name like ?)))";
                params.add("%" + ss + "%");
                params.add("%" + ss + "%");
                condition += searchQuery;
            }
            
            String appendCase = "and";
            String mySearchFilterString = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            if (isPendingApproval) { // Get only pending approved records
                condition += " and r.approvestatuslevel != ? ";
                params.add(11);
            }
            String Searchjson = "";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = request.get("searchJson").toString();

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, appendCase);
                    request.put("isOpeningBalance",true);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "OpeningBalanceReceiptCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceReceiptCustomData", "r.openingBalanceReceiptCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "r.openingBalanceReceiptCustomData");
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            String orderByCondition="";
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                orderByCondition = sortColumnPayment(Col_Name, Col_Dir, true);
            }
            String query = "select r from Receipt r where r.isOpeningBalenceReceipt=true AND r.deleted=false AND r.company.companyID=?" + condition + mySearchFilterString+" "+orderByCondition;
            list = executeQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAllOpeningBalanceReceipts : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    @Override
    public KwlReturnObject getReceiptsContainingProject(HashMap<String, Object> dataMap) throws ServiceException {
        String companyId = (String) dataMap.get("companyid");
        String searchString = (String) dataMap.get("searchString");
        Date startDate = (Date) dataMap.get("startDate");
        Date endDate = (Date) dataMap.get("endDate");

        String mysqlQuery = "select r.id from receipt r "
                + " inner join journalentry je  on r.journalentry = je.id "
                + " inner join accjecustomdata on accjecustomdata.journalentryId=r.journalentry "
                + " where r.company=?  and r.deleteflag=false "
                + " and r.contraentry=false and (je.entrydate >=? and je.entrydate <=?) " + searchString;

        List list = executeSQLQuery( mysqlQuery, new Object[]{companyId, startDate, endDate});

        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject saveBillingReceipt(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            BillingReceipt receipt = new BillingReceipt();
            String receiptid = (String) hm.get("billingreceiptid");

            if (StringUtil.isNullOrEmpty(receiptid)) {
                receipt = new BillingReceipt();
                receipt.setDeleted(false);
            } else {
                receipt = (BillingReceipt) get(BillingReceipt.class, receiptid);
            }

            if (hm.containsKey(Constants.SEQFORMAT)) {
                receipt.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) hm.get(Constants.SEQFORMAT)));
            }
            if (hm.containsKey(Constants.SEQNUMBER)) {
                receipt.setSeqnumber(Integer.parseInt(hm.get(Constants.SEQNUMBER).toString()));
            }
            if (hm.containsKey("entrynumber")) {
                receipt.setBillingReceiptNumber((String) hm.get("entrynumber"));
            }
            if (hm.containsKey("autogenerated")) {
                receipt.setAutoGenerated((Boolean) hm.get("autogenerated"));
            }
            if (hm.containsKey("memo")) {
                receipt.setMemo((String) hm.get("memo"));
            }
            if (hm.containsKey("externalCurrencyRate")) {
                receipt.setExternalCurrencyRate((Double) hm.get("externalCurrencyRate"));
            }
            if (hm.containsKey("paydetailsid")) {
                PayDetail pd = hm.get("paydetailsid") == null ? null : (PayDetail) get(PayDetail.class, (String) hm.get("paydetailsid"));
                receipt.setPayDetail(pd);
            }
            if (hm.containsKey("companyid")) {
                Company company = hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid"));
                receipt.setCompany(company);
            }
            if (hm.containsKey("currencyid")) {
                KWLCurrency currency = hm.get("currencyid") == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) hm.get("currencyid"));
                receipt.setCurrency(currency);
            }
            if (hm.containsKey("journalentryid")) {
                JournalEntry je = hm.get("journalentryid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) hm.get("journalentryid"));
                receipt.setJournalEntry(je);
            }
            if (hm.containsKey("deposittojedetailid")) {
                JournalEntryDetail jed = hm.get("deposittojedetailid") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("deposittojedetailid"));
                receipt.setDeposittoJEDetail(jed);
            }
            if (hm.containsKey("depositamount") && !StringUtil.isNullOrEmpty(hm.get("depositamount").toString())) {
                receipt.setDepositAmount(Double.parseDouble(hm.get("depositamount").toString()));
            }
            if (hm.containsKey("receiptdetails")) {
                receipt.setRows((Set<ReceiptDetail>) hm.get("receiptdetails"));
            }
            if (hm.containsKey("contraentry")) {
                receipt.setContraentry((Boolean) hm.get("contraentry"));
            }
            if (hm.containsKey("ismanydbcr")) {
                receipt.setIsmanydbcr((Boolean) hm.get("ismanydbcr"));
            }
            if (hm.containsKey("isadvanceFromVendor")) {
                receipt.setIsadvancefromvendor((Boolean) hm.get("isadvanceFromVendor"));
            }
            if (hm.containsKey("bankCharges") && !StringUtil.isNullOrEmpty(hm.get("bankCharges").toString())) {
                receipt.setBankChargesAmount(Double.parseDouble(hm.get("bankCharges").toString()));
            }
            if (hm.containsKey("bankChargesCmb")) {
                Account bankChargesAccount = hm.get("bankChargesCmb") == null ? null : (Account) get(Account.class, (String) hm.get("bankChargesCmb"));
                receipt.setBankChargesAccount(bankChargesAccount);
            }
            if (hm.containsKey("bankInterest") && !StringUtil.isNullOrEmpty(hm.get("bankInterest").toString())) {
                receipt.setBankInterestAmount(Double.parseDouble(hm.get("bankInterest").toString()));
            }
            if (hm.containsKey("bankInterestCmb")) {
                Account bankInterestAccount = hm.get("bankInterestCmb") == null ? null : (Account) get(Account.class, (String) hm.get("bankInterestCmb"));
                receipt.setBankInterestAccount(bankInterestAccount);
            }
            if (hm.containsKey("paidToCmb")) {
                MasterItem paidToCmb = hm.get("paidToCmb") == null ? null : (MasterItem) get(MasterItem.class, (String) hm.get("paidToCmb"));
                receipt.setReceivedFrom(paidToCmb);
            }
            if (hm.containsKey("receipttype")) {
                receipt.setReceipttype((Integer) hm.get("receipttype"));
            }
            if (hm.containsKey("vendor")) {
                receipt.setVendor((String) hm.get("vendor"));
            }
            if (hm.containsKey("revalJeId")) {
                receipt.setRevalJeId((String) hm.get("revalJeId"));
            }
            saveOrUpdate(receipt);
            list.add(receipt);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.saveBillingReceipt : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Billing Receipt has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject getBillingReceipts(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get("start");
            String limit = (String) request.get("limit");
            String ss = (String) request.get("ss");
            String companyid = (String) request.get("companyid");
            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            boolean contraentryflag = request.containsKey("contraentryflag") ? (Boolean) request.get("contraentryflag") : false;

            boolean isPostDatedCheque = false;
            if (request.get("isPostDatedCheque") != null) {
                isPostDatedCheque = (Boolean) request.get("isPostDatedCheque");
            }
            boolean isDishonouredCheque = false;
            if (request.get("isDishonouredCheque") != null) {
                isDishonouredCheque = (Boolean) request.get("isDishonouredCheque");
            }

            boolean isAdvanceFromVendor = false;
            if (request.get("isadvancefromvendor") != null) {
                isAdvanceFromVendor = (Boolean) request.get("isadvancefromvendor");
            }

            ArrayList params = new ArrayList();
            params.add(companyid);

            String condition = " where r.company.companyID=?  and jed.debit=false ";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"ac.name", "r.billingReceiptNumber", "r.journalEntry.entryNumber", "r.memo"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;

//               params.add(ss+"%");
//               params.add(ss+"%");
//               params.add(ss+"%");
//               condition+= " and (ac.name like ?  or r.billingReceiptNumber  like ? or r.journalEntry.entryNumber like ?) ";
            }
            if (nondeleted) {
                condition += " and r.deleted=false ";
            } else if (deleted) {
                condition += " and r.deleted=true ";
            }

            if (contraentryflag) {
                condition += " and r.contraentry=true ";
            } else {
                condition += " and r.contraentry=false ";
            }

            if (isAdvanceFromVendor) {
                condition += " and r.isadvancefromvendor=true ";//Normal advance payment from customer not need to show
            }
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and r.journalEntry.ID IN(" + jeIds + ")";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
//                condition += " and (r.journalEntry.entryDate >=? and r.journalEntry.entryDate <=?) ";
                condition += " and (r.creationDate >=? and r.creationDate <=?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if (isPostDatedCheque) {
                condition += " and r.payDetail.cheque.dueDate > now() ";
            }
            if (isDishonouredCheque) {
                condition += " and r.payDetail.paymentMethod.detailType=2 ";
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
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = request.get("searchJson").toString();

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, appendCase);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "r.journalEntry.accBillInvCustomData");
                    joinString = " inner join je.accBillInvCustomData accjecustomdata ";
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
//                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }

            String query = "select r, ac from BillingReceipt r inner join r.journalEntry je inner join je.details jed inner join jed.account ac" + condition + mySearchFilterString + " group by r";
            list = executeQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    @Override
    public KwlReturnObject getBillingReceiptsContainingProject(HashMap<String, Object> dataMap) throws ServiceException {
        String companyId = (String) dataMap.get("companyid");
        String searchString = (String) dataMap.get("searchString");
        Date startDate = (Date) dataMap.get("startDate");
        Date endDate = (Date) dataMap.get("endDate");

        String mysqlQuery = "select r.id from billingreceipt r "
                + " inner join journalentry je  on r.journalentry = je.id "
                + " inner join accjecustomdata on accjecustomdata.journalentryId=r.journalentry "
                + " where r.company=?  and r.deleteflag=false "
                + " and r.contraentry=false and (je.entrydate >=? and je.entrydate <=?) " + searchString;

        List list = executeSQLQuery( mysqlQuery, new Object[]{companyId, startDate, endDate});

        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getReciptPaymentCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from AccJEDetailCustomData";
        return buildNExecuteQuery( query, requestParams);
    }
    
    public KwlReturnObject getReciptPaymentGlobalCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from AccJECustomData";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getContraPaymentFromInvoice(String invoiceid, String companyid) throws ServiceException {
        String selQuery = "from PaymentDetail rd  where rd.invoice.ID=? and rd.payment.deleted=false and rd.company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{invoiceid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getReceiptFromInvoice(HashMap<String, Object> receiptMap) throws ServiceException {
        List list=null;
        try {
            String condition="";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) receiptMap.get(Constants.df);
            String invoiceId = (String) receiptMap.get("invoiceid");
            params.add(invoiceId);
            if(receiptMap.containsKey("companyid") && receiptMap.get("companyid")!=null){
                String companyId = (String) receiptMap.get("companyid");
                condition += " and rd.company.companyID=?";
                params.add(companyId);
            }
            if(receiptMap.containsKey("asofdate") && receiptMap.get("asofdate")!=null){
                String asOfDate = (String) receiptMap.get("asofdate");
//                condition += "  and (rd.receipt.creationDate<=? or rd.receipt.journalEntry.entryDate<=?)" ;
                condition += "  and (rd.receipt.creationDate<=?)" ;
//                params.add(df.parse(asOfDate));
                params.add(df.parse(asOfDate));
            }
            if (receiptMap.containsKey("startDate") && receiptMap.get("startDate") != null && receiptMap.containsKey("endDate") && receiptMap.get("endDate") != null) {//All ReceiptDetail between start date and end date 
//                condition += " and (rd.receipt.journalEntry.entryDate >=? and rd.receipt.journalEntry.entryDate <=?) ";
                condition += " and (rd.receipt.creationDate >=? and rd.receipt.creationDate <=?) ";
                params.add((Date)receiptMap.get("startDate"));
                params.add((Date)receiptMap.get("endDate"));
            }
            if(receiptMap.containsKey("isApprovedPayment") && receiptMap.get("isApprovedPayment")!=null && Boolean.parseBoolean(receiptMap.get("isApprovedPayment").toString())){                
                condition+=" and rd.receipt.approvestatuslevel=11";
            }
            if (receiptMap.containsKey("filterby") && receiptMap.get("filterby") != null) {
                if (receiptMap.get("filterby").toString().equalsIgnoreCase(Constants.FULLY_PAID)) {
                    condition += " and rd.invoice.invoiceamountdue <= 0 ";
                }
            }
            String selQuery = "from ReceiptDetail rd  where rd.invoice.ID=? and rd.receipt.deleted=false and rd.receipt.isDishonouredCheque='F' "+condition;
            list = executeQuery( selQuery, params.toArray());
        }catch (ParseException ex) {
                Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    /**
     * Description : get data of Cash Sales
     * @param <requestParams> To send parameters
     * @return :KwlReturnObject
     */
    
    public KwlReturnObject getCashSalesFOrDayEndCollection(HashMap<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList params = new ArrayList();
        int count = 0;
        String condition = "";
        DateFormat df = (DateFormat) requestParams.get(Constants.df);
        try {
            String start = "";
            String limit = "";
            String joinString1 = "";
            String conditionSQL = "";
            String ss = (String) requestParams.get(Constants.ss);
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            String companyId = (String) requestParams.get("companyid");
            params.add(companyId);
            if (requestParams.containsKey(Constants.start) && requestParams.get(Constants.start) != null && requestParams.containsKey(Constants.limit) && requestParams.get(Constants.limit) != null) {
                start = (String) requestParams.get(Constants.start);
                limit = (String) requestParams.get(Constants.limit);
            }
            String newcustomerid = "";
            String paymentMethodId = "";
            if (requestParams.containsKey(InvoiceConstants.newcustomerid) && requestParams.get(InvoiceConstants.newcustomerid) != null) {
                newcustomerid = (String) requestParams.get(InvoiceConstants.newcustomerid);
            }
            
            if (requestParams.containsKey("paymentMethodId") && requestParams.get("paymentMethodId") != null) {
                paymentMethodId = (String) requestParams.get("paymentMethodId");
            }
            if (!StringUtil.isNullOrEmpty(ss)) {
                joinString1 += " inner join customer on customer.id = invoice.customer ";
                String[] searchcol = new String[]{"invoice.invoicenumber", "customer.acccode"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, Constants.and, searchcol);
                condition += searchQuery;
            }

            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                joinString1 += " inner join journalentry on journalentry.ID=invoice.journalentry ";
//                condition += " and (journalentry.entryDate >=? and journalentry.entryDate <=?)";
                condition += " and (invoice.creationdate >=? and invoice.creationdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            if (!StringUtil.isNullOrEmpty(newcustomerid)) {
                params.add(newcustomerid);
                conditionSQL += " and invoice.customer = ? ";
            }
            
            if (!StringUtil.isNullOrEmpty(paymentMethodId)) {
                joinString1 += "inner join paydetail on paydetail.id= invoice.paydetail ";
                conditionSQL += " and paydetail.paymentMethod = ? ";
                params.add(paymentMethodId);
            }
            
            String appendCase = "and";
            boolean Or=false;
            String mySearchFilterString = "";

            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    Or=true;
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String Searchjson = "";
            String searchDefaultFieldSQL = "";
            String jeid = " jedetail.id = invoice.centry ";
            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = requestParams.get("searchJson").toString();

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray customSearchFieldArray = new JSONArray();
                    JSONArray defaultSearchFieldArray = new JSONArray();
                    StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);

                    if (customSearchFieldArray.length() > 0) {
                        /*
                         * Advance Search For Custom fields
                         */
                        requestParams.put(Constants.Searchjson, Searchjson);
                        requestParams.put(Constants.appendCase, appendCase);
                        requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);

                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, true).get(Constants.myResult));
                        if (Or) {
                            requestParams.put("filterConjuctionCriteria", "OR");
                        }
                        if (mySearchFilterString.contains("accjecustomdata")) {
                            joinString1 += " inner join accjecustomdata on accjecustomdata.journalentryId=invoice.journalentry ";
                        }
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                        if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//    
                            joinString1 += " inner join accjedetailcustomdata  on accjedetailcustomdata.jedetailId=jedetail.id ";
                            jeid = " jedetail.journalentry = invoice.journalentry ";
                        }
                        if (mySearchFilterString.contains("CustomerCustomData")) {
                            joinString1 += " inner join customercustomdata  on customercustomdata.customerId=invoice.customer ";
                            mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "customercustomdata");
                        }
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
            String query = " select invoice.id from invoice " + joinString1 + " where invoice.company=? and cashtransaction=1" + condition + conditionSQL + mySearchFilterString;
            returnlist = executeSQLQuery(query, params.toArray());
            count = returnlist.size();

        } catch (Exception ex) {
            Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, returnlist, count);
    }
    
    /**
     * Description : get data of Receive Payment
     * @param <requestParams> To send parameters
     * @return :KwlReturnObject
     */
    public KwlReturnObject getReceiptsFOrDayEndCollection(HashMap<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList params = new ArrayList();
        int count = 0;
        String condition = "";
        try {
            String joinString1 = "";
            String conditionSQL = "";
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String ss = (String) requestParams.get(Constants.ss);
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            String companyId = (String) requestParams.get("companyid");
            params.add(companyId);
//            if (requestParams.containsKey(Constants.start) && requestParams.get(Constants.start) != null && requestParams.containsKey(Constants.limit) && requestParams.get(Constants.limit) != null) {
//                start = (String) requestParams.get(Constants.start);
//                limit = (String) requestParams.get(Constants.limit);
//            }
            String newcustomerid = "";
           String paymentMethodId = "";
            if (requestParams.containsKey(InvoiceConstants.newcustomerid) && requestParams.get(InvoiceConstants.newcustomerid) != null) {
                newcustomerid = (String) requestParams.get(InvoiceConstants.newcustomerid);
            }
            
            if (requestParams.containsKey("paymentMethodId") && requestParams.get("paymentMethodId") != null) {
                paymentMethodId = (String) requestParams.get("paymentMethodId");
            }
            
            
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                joinString1 += " inner join journalentry on journalentry.ID=receipt.journalentry ";
//                condition += " and (journalentry.entryDate >=? and journalentry.entryDate <=?)";
                condition += " and (receipt.creationdate >=? and receipt.creationdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            if (!StringUtil.isNullOrEmpty(ss)) {
                joinString1 += " inner join customer on customer.id = receipt.customer ";
                String[] searchcol = new String[]{"receipt.receiptnumber", "customer.acccode"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, Constants.and, searchcol);
                condition += searchQuery;
            }
            if (!StringUtil.isNullOrEmpty(newcustomerid)) {
                params.add(newcustomerid);
                conditionSQL += " and receipt.customer = ? ";
            }
            if (!StringUtil.isNullOrEmpty(paymentMethodId)) {
                joinString1 += "inner join paydetail on paydetail.id= receipt.paydetail ";
                conditionSQL += " and paydetail.paymentMethod = ? ";
                params.add(paymentMethodId);
            }
            
            String appendCase = "and";
            String mySearchFilterString = "";

            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String Searchjson = "";
            String searchDefaultFieldSQL = "";
            String jeid = " jedetail.id = invoice.centry ";
            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = requestParams.get("searchJson").toString();

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray customSearchFieldArray = new JSONArray();
                    JSONArray defaultSearchFieldArray = new JSONArray();
                    StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);

                    if (customSearchFieldArray.length() > 0) {
                        /*
                         * Advance Search For Custom fields
                         */
                        requestParams.put(Constants.Searchjson, Searchjson);
                        requestParams.put(Constants.appendCase, appendCase);
                        requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);

                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, true).get(Constants.myResult));
                        if (mySearchFilterString.contains("accjecustomdata")) {
                            joinString1 += " inner join accjecustomdata on accjecustomdata.journalentryId=receipt.journalentry ";
                        }
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                        if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//    
                            joinString1 += " inner join accjedetailcustomdata  on accjedetailcustomdata.jedetailId=jedetail.id ";
                            jeid = " jedetail.journalentry = receipt.journalentry ";
                        }
                        if (mySearchFilterString.contains("CustomerCustomData")) {
                            joinString1 += " inner join customercustomdata  on customercustomdata.customerId=receipt.customer ";
                            mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "customercustomdata");
                        }
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
            String query = " select receipt.id from receipt " + joinString1 + " where receipt.company=? and paymentwindowtype=1" + condition + conditionSQL + mySearchFilterString;
            returnlist = executeSQLQuery(query, params.toArray());
            count = returnlist.size();
//            returnlist = executeSQLQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});

        } catch (Exception ex) {
            Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, returnlist, count);
    }
    
    
    
    public KwlReturnObject getLinkDetailReceipt(HashMap<String, Object> receiptMap) throws ServiceException {
        List list=null;
        try {
            String condition="";
            String innerJoinQuery="";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) receiptMap.get(Constants.df);                      
             if(receiptMap.containsKey("invoiceid") && receiptMap.get("invoiceid") != null){
                   String invoiceId = (String) receiptMap.get("invoiceid");
                   condition += " and rd.invoice.ID=? ";
                   params.add(invoiceId);
            }
            if(receiptMap.containsKey("receiptid") && receiptMap.get("receiptid") != null){
                   String paymentId = (String) receiptMap.get("receiptid");
                   condition += " and rd.receipt.ID=? ";
                   params.add(paymentId);
            }
            if(receiptMap.containsKey("companyid") && receiptMap.get("companyid")!=null){
                String companyId = (String) receiptMap.get("companyid");
                condition += " and rd.company.companyID=? ";
                params.add(companyId);
            }
            if(receiptMap.containsKey("asofdate") && receiptMap.get("asofdate")!=null){
                String asOfDate = (String) receiptMap.get("asofdate");
                condition += "  and rd.receiptLinkDate<=? ";
                params.add(df.parse(asOfDate));
            }
            if (receiptMap.containsKey("startDate") && receiptMap.get("startDate") != null && receiptMap.containsKey("endDate") && receiptMap.get("endDate") != null) {//All ReceiptDetail between start date and end date 
                Date startDate = (Date)receiptMap.get("startDate");
                Date endDate = (Date)receiptMap.get("endDate");
                condition += " and (rd.receiptLinkDate >=? and rd.receiptLinkDate <=?) ";
                params.add(startDate);
                params.add(endDate);
            }
            if (receiptMap.containsKey("taxId") && receiptMap.get("taxId") != null) {
                String taxId = (String)receiptMap.get("taxId");
                innerJoinQuery = " join rd.receipt.receiptAdvanceDetails rad ";
                condition += " and (rad.GST.ID = ? ) ";
                params.add(taxId);
            }
            if (receiptMap.containsKey("upperLimitDate") && receiptMap.get("upperLimitDate") != null ) {//All ReceiptDetail till end date date
                Date endDate = (Date)receiptMap.get("upperLimitDate");
                condition += " and (rd.receiptLinkDate <=?) ";
                params.add(endDate);
            }
            if(receiptMap.containsKey("isApprovedPayment") && receiptMap.get("isApprovedPayment")!=null && Boolean.parseBoolean(receiptMap.get("isApprovedPayment").toString())){                
                condition+=" and rd.receipt.approvestatuslevel=11";                 //added rd.receipt.approvestatuslevel=11 condition so that only approved records are displayed in reports
            }
            String selQuery = "select rd from LinkDetailReceipt rd "+innerJoinQuery+" where rd.receipt.deleted=false and rd.receipt.isDishonouredCheque='F' "+condition;    
            list = executeQuery( selQuery, params.toArray());
        }catch (Exception ex) {
                Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getLinkDetailReceiptToDebitNote(HashMap<String, Object> receiptMap) throws ServiceException {
        List list=null;
        try {
            String condition="";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) receiptMap.get(Constants.df);                      
             if(receiptMap.containsKey("debitnoteid") && receiptMap.get("debitnoteid") != null){
                   String dnid = (String) receiptMap.get("debitnoteid");
                   condition += " and rd.debitnote.ID=? ";
                   params.add(dnid);
            }
            if(receiptMap.containsKey("receiptid") && receiptMap.get("receiptid") != null){
                   String paymentId = (String) receiptMap.get("receiptid");
                   condition += " and rd.receipt.ID=? ";
                   params.add(paymentId);
            }
            if(receiptMap.containsKey("companyid") && receiptMap.get("companyid")!=null){
                String companyId = (String) receiptMap.get("companyid");
                condition += " and rd.company.companyID=? ";
                params.add(companyId);
            }
            if(receiptMap.containsKey("asofdate") && receiptMap.get("asofdate")!=null){
                String asOfDate = (String) receiptMap.get("asofdate");
                condition += "  and rd.receiptLinkDate<=? ";
                params.add(df.parse(asOfDate));
            }
            if (receiptMap.containsKey("startDate") && receiptMap.get("startDate") != null && receiptMap.containsKey("endDate") && receiptMap.get("endDate") != null) {//All ReceiptDetail between start date and end date 
                Date startDate = (Date)receiptMap.get("startDate");
                Date endDate = (Date)receiptMap.get("endDate");
                condition += " and (rd.receiptLinkDate >=? and rd.receiptLinkDate <=?) ";
                params.add(startDate);
                params.add(endDate);
            }
            String selQuery = "from LinkDetailReceiptToDebitNote rd  where rd.receipt.deleted=false and rd.receipt.isDishonouredCheque='F' "+condition;
            list = executeQuery( selQuery, params.toArray());
        }catch (Exception ex) {
                Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getLinkDetailAdvanceReceiptToRefundPayment(HashMap<String, Object> reqParams1) throws ServiceException {
        List list=null;
        try {
            String condition="";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) reqParams1.get(Constants.df);      
            if(reqParams1.containsKey("receiptid") && reqParams1.get("receiptid") != null){
                   String receiptid = (String) reqParams1.get("receiptid");
                   condition += " and rd.receipt.ID=? ";
                   params.add(receiptid);
            }
            if(reqParams1.containsKey("companyid") && reqParams1.get("companyid")!=null){
                String companyId = (String) reqParams1.get("companyid");
                condition += " and rd.company.companyID=? ";
                params.add(companyId);
            }
            if(reqParams1.containsKey("asofdate") && reqParams1.get("asofdate")!=null){
                String asOfDate = (String) reqParams1.get("asofdate");
                condition += "  and rd.paymentLinkDate<=? ";
                params.add(df.parse(asOfDate));
            }
            if (reqParams1.containsKey("startDate") && reqParams1.get("startDate") != null && reqParams1.containsKey("endDate") && reqParams1.get("endDate") != null) {//All ReceiptDetail between start date and end date 
                Date startDate = (Date)reqParams1.get("startDate");
                Date endDate = (Date)reqParams1.get("endDate");
                condition += " and (rd.paymentLinkDate >=? and rd.paymentLinkDate <=?) ";
                params.add(startDate);
                params.add(endDate);
            }
            /* ERP-30246
             *  checks for linkdetails of foreign exchange gain/loss je at time of delete operation from journalentry report 
             */
            if(reqParams1.containsKey("exgainlossjeid") && reqParams1.get("exgainlossjeid") != null){
                String exgainlossjeid = (String)reqParams1.get("exgainlossjeid");
                condition += " and rd.linkedGainLossJE =? ";
                params.add(exgainlossjeid);
            }
            String selQuery = "from LinkDetailPaymentToAdvancePayment rd  where rd.payment.deleted=false "+condition;
            list = executeQuery( selQuery, params.toArray());
        }catch (Exception ex) {
                Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getLinkDetailReceiptToAdvancePayment(HashMap<String, Object> reqParams1) throws ServiceException {
        List list=null;
        try {
            String condition="";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) reqParams1.get(Constants.df);      
            if(reqParams1.containsKey("receiptid") && reqParams1.get("receiptid") != null){
                   String receiptid = (String) reqParams1.get("receiptid");
                   condition += " and rd.receipt.ID=? ";
                   params.add(receiptid);
            }
            if(reqParams1.containsKey("companyid") && reqParams1.get("companyid")!=null){
                String companyId = (String) reqParams1.get("companyid");
                condition += " and rd.company.companyID=? ";
                params.add(companyId);
            }
            if(reqParams1.containsKey("asofdate") && reqParams1.get("asofdate")!=null){
                String asOfDate = (String) reqParams1.get("asofdate");
                condition += "  and rd.receiptLinkDate<=? ";
                params.add(df.parse(asOfDate));
            }
            if (reqParams1.containsKey("startDate") && reqParams1.get("startDate") != null && reqParams1.containsKey("endDate") && reqParams1.get("endDate") != null) {//All ReceiptDetail between start date and end date 
                Date startDate = (Date)reqParams1.get("startDate");
                Date endDate = (Date)reqParams1.get("endDate");
                condition += " and (rd.receiptLinkDate >=? and rd.receiptLinkDate <=?) ";
                params.add(startDate);
                params.add(endDate);
            }
            String selQuery = "from LinkDetailReceiptToAdvancePayment rd  where rd.receipt.deleted=false "+condition;
            list = executeQuery( selQuery, params.toArray());
        }catch (Exception ex) {
                Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getReceiptFromInvoice(String invoiceid) throws ServiceException {
        String selQuery = "from ReceiptDetail rd  where rd.invoice.ID=? and rd.receipt.deleted=false and rd.receipt.isDishonouredCheque='F'";
        List list = executeQuery( selQuery, new Object[]{invoiceid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getReceiptFromInvoiceNewUI(String invoiceid) throws ServiceException {
        String selQuery = "from LinkDetailReceipt rd  where rd.invoice.ID=? and rd.receipt.deleted=false";
        List list = executeQuery( selQuery, new Object[]{invoiceid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public Map<String, List<ReceiptDetail>> getContraPayReceiptFromGReceiptList(List<String> invoiceIDLIST) throws ServiceException {
        Map<String, List<ReceiptDetail>> invoiceMap = new HashMap<String, List<ReceiptDetail>>();
        if (invoiceIDLIST != null && !invoiceIDLIST.isEmpty()) {
            List li = null;
            String query = "select  rd.goodsReceipt.ID, rd "
                    + " from ReceiptDetail rd "
                    + " where rd.goodsReceipt.ID in (:invoiceIDList) and rd.receipt.deleted=false";
            List<List> values = new ArrayList<List>();
            values.add(invoiceIDLIST);
            List<Object[]> results = executeCollectionQuery( query, Collections.singletonList("invoiceIDList"), values);

            if (results != null) {
                for (Object[] result : results) {
                    String invID = (String) result[0];
                    if (invoiceMap.containsKey(invID)) {
                        li = invoiceMap.get(invID);
                    } else {
                        li = new ArrayList<ReceiptDetail>();
                    }
                    li.add((ReceiptDetail) result[1]);
                    invoiceMap.put(invID, li);
                }
            }
        }
        return invoiceMap;
    }

    public KwlReturnObject getBReceiptFromBInvoice(String invoiceid, String companyid) throws ServiceException {
        String selQuery = "from BillingReceiptDetail rd  where rd.billingInvoice.ID=? and rd.billingReceipt.deleted=false and rd.company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{invoiceid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject deleteReceiptDetails(String receiptid, String companyid) throws ServiceException {
        ArrayList params5 = new ArrayList();
        params5.add(companyid);
        params5.add(companyid);
        params5.add(receiptid);
        String delQuery5 = "delete from accjedetailcustomdata where jedetailId in (select id from jedetail where company =? and journalEntry in (select journalentry from receipt where company =? and id =?))";
        int numRows5 = executeSQLUpdate( delQuery5, params5.toArray());
        String delQuery = "delete from ReceiptDetail brd where brd.receipt.ID=? and brd.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Receipt details has been deleted successfully", null, null, numRows);
    }
    
    public KwlReturnObject deleteSelectedLinkedReceiptInvoices(String receiptid, String linkedDetailIDs, String companyid,String unlinkedDetailIDs) throws ServiceException {
        String delQuery = "delete from LinkDetailReceipt brd where brd.receipt.ID=? and brd.company.companyID=?";
        if(!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
           delQuery = delQuery.concat(" and brd.id not in (" + linkedDetailIDs + ")");
        }
        if(!StringUtil.isNullOrEmpty(unlinkedDetailIDs)) {
           delQuery = delQuery.concat(" and brd.id in (" + unlinkedDetailIDs + ")");
        }
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Receipt linked invoices have been deleted successfully", null, null, numRows);
    }
    public KwlReturnObject deleteLinkReceiptDetails(String receiptid, String companyid) throws ServiceException {
        String delQuery = "delete from LinkDetailReceipt brd where brd.receipt.ID=? and brd.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Receipt details has been deleted successfully", null, null, numRows);
    }
    public KwlReturnObject deleteLinkReceiptToDebitNoteDetails(String receiptid, String companyid) throws ServiceException {
        String delQuery = "delete from LinkDetailReceiptToDebitNote LD where LD.receipt.ID=? and LD.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Receipt details has been deleted successfully", null, null, numRows);
    }
    public KwlReturnObject deleteReceiptDetailsAndUpdateAmountDue(String receiptid, String companyid) throws ServiceException {

        String selQuery = "from ReceiptDetail pd where pd.receipt.ID=? and pd.company.companyID=?";
        List<ReceiptDetail> details=find(selQuery,new Object[]{receiptid, companyid});
        List<Invoice> invoicesList=new ArrayList<Invoice>();
        for(ReceiptDetail receiptDetail:details){
            Invoice invoice=receiptDetail.getInvoice();
            double discountAmtInInvoiceCurrency = authHandler.round(receiptDetail.getDiscountAmount() / receiptDetail.getExchangeRateForTransaction(), companyid);
            double discountAmount = receiptDetail.getDiscountAmount();
            if(invoice.isNormalInvoice()){
                /*
                 set status flag for amount due 
                 */
                double amountdueforstatus = invoice.getInvoiceamountdue() + receiptDetail.getAmount()+discountAmount;
                if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                    invoice.setIsOpenReceipt(false);
                } else {
                    invoice.setIsOpenReceipt(true);
                }
                invoice.setInvoiceamountdue(invoice.getInvoiceamountdue()+receiptDetail.getAmount()+discountAmount);
            } else if (invoice.isIsOpeningBalenceInvoice()) {
                double amountdue = invoice.getOpeningBalanceAmountDue();
                /*
                 * set status flag for opening invoices
                 */
                double amountdueforstatus = amountdue + receiptDetail.getAmountInInvoiceCurrency()+discountAmtInInvoiceCurrency;
                if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                    invoice.setIsOpenReceipt(false);
                } else {
                    invoice.setIsOpenReceipt(true);
                }
            }
            invoice.setOpeningBalanceAmountDue(invoice.getOpeningBalanceAmountDue()+receiptDetail.getAmount()+discountAmount);
            invoice.setOpeningBalanceBaseAmountDue(invoice.getOpeningBalanceBaseAmountDue()+receiptDetail.getAmountDueInBaseCurrency()+discountAmount);
            invoicesList.add(invoice);
        }
        if(!invoicesList.isEmpty()){
            saveAll(invoicesList);
        }

        String delQuery = "delete from ReceiptDetail brd where brd.receipt.ID=? and brd.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Receipt details has been deleted successfully", null, null, numRows);
    }

    public KwlReturnObject deleteReceipt(String receiptid, String companyid) throws ServiceException {
        String delQuery = "delete from Receipt br where ID=? and br.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Receipt details has been deleted successfully", null, null, numRows);
    }

    public KwlReturnObject deleteReceiptEntry(String receiptid, String companyid) throws ServiceException,AccountingException {
        String query = "update receipt set advanceid = null, advanceamount=0 where company=? and advanceid=?";
        int numRows = executeSQLUpdate( query, new Object[]{companyid, receiptid});
        query = "update Receipt set deleted=true where ID=? and company.companyID=?";
        numRows = executeUpdate( query, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Receipt has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteReceiptPermanent(HashMap<String, Object> requestParams) throws ServiceException,AccountingException {
       String delQuery = "", delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuery4 = "", delQuery5 = "", delQuery6 = "", delQuery7 = "", delQuery8 = "", delQuery9 = "", delQuery12 = "", delQuery13 = "",delQuery15="",delQuery16="",delQuery17="",delQuery18="",delQuery19="",delQuery20="",delquery21="",delquery22="",delQuery23,delQueryDnLink="";
       String delQueryForWriteOff="";
       int numtotal = 0, numRows12 = 0,numRows=0 , numRows2=0 , numRows3=0 , numRows4=0,numRows8=0,numRows13=0,numRows21=0,numRows22=0,numDeletedWriteOffJE=0;
        try {
            if (requestParams.containsKey("receiptid") && requestParams.containsKey("companyid")) {

                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("companyid"));
                params8.add(requestParams.get("receiptid"));
                String myquery = "select journalentry from receipt where company =? and id = ?";
                List list = executeSQLQuery( myquery, params8.toArray());
                Iterator itr = list.iterator();
                String idStrings = "";
                while (itr.hasNext()) {
                    Object invdidobj = itr.next();
                    String invdid = (invdidobj != null) ? invdidobj.toString() : "";
                    idStrings += "'" + invdid + "',";
                }
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    idStrings = idStrings.substring(0, idStrings.length() - 1);
                }

                // delete query for bank charges JE
                String bankchargemyquery = "select journalentryforbankcharges from receipt where company =? and id = ?";
                List bnkchrgelist1 = executeSQLQuery( bankchargemyquery, params8.toArray());
                Iterator bkitr = bnkchrgelist1.iterator();
                String bankchargeidStrings = "";
                while (bkitr.hasNext()) {
                    Object invdidobj = bkitr.next();
                    String invdid = (invdidobj != null) ? invdidobj.toString() : "";
                    bankchargeidStrings += "'" + invdid + "',";
                }
                if (!StringUtil.isNullOrEmpty(bankchargeidStrings)) {
                    bankchargeidStrings = bankchargeidStrings.substring(0, bankchargeidStrings.length() - 1);
                }
                // delete query for bank interest JE
                String bankinterestmyquery = "select journalentryforbankinterest from receipt where company =? and id = ?";
                List bnkintrstlist1 = executeSQLQuery( bankinterestmyquery, params8.toArray());
                Iterator bkintitr = bnkintrstlist1.iterator();
                String bankinterestidStrings = "";
                while (bkintitr.hasNext()) {
                    Object invdidobj = bkintitr.next();
                    String invdid = (invdidobj != null) ? invdidobj.toString() : "";
                    bankinterestidStrings += "'" + invdid + "',";
                }
                if (!StringUtil.isNullOrEmpty(bankinterestidStrings)) {
                    bankinterestidStrings = bankinterestidStrings.substring(0, bankinterestidStrings.length() - 1);
                }
                
                // Delete Write Off JE and Recovred JE
                String writeOffrelatedJE="";
                ArrayList writeOffParams = new ArrayList();
                writeOffParams.add(requestParams.get("companyid"));
                writeOffParams.add(requestParams.get("receiptid"));
                
                String queryToGetWriteOffJE = "select journalentry from receiptwriteoff where company = ? and receipt = ?";
                List writeOffJEList = executeSQLQuery( queryToGetWriteOffJE, writeOffParams.toArray());
                Iterator writeOffJEIterator = writeOffJEList.iterator();

                while (writeOffJEIterator.hasNext()) {
                    String jeidi = writeOffJEIterator.next().toString();
                    writeOffrelatedJE += "'" + jeidi + "',";
                }
                
                String queryToGetReverseWriteOffJE = "select reversejournalentry from receiptwriteoff where company = ? and receipt = ?";
                List writeOffReverseJEList = executeSQLQuery( queryToGetReverseWriteOffJE, writeOffParams.toArray());
                Iterator writeOffReverseJEIterator = writeOffReverseJEList.iterator();
                
                while (writeOffReverseJEIterator.hasNext()) {
                    String jeidi = writeOffReverseJEIterator.next().toString();
                    writeOffrelatedJE += "'" + jeidi + "',";
                }
                
                if (!StringUtil.isNullOrEmpty(writeOffrelatedJE)) {
                    writeOffrelatedJE = writeOffrelatedJE.substring(0, writeOffrelatedJE.length() - 1);
                }
                
                // delete Queries for DisHonoured Cheque JE's
                String disHonouredChequeJe = "select dishonouredchequeje from receipt where company =? and id = ?";
                List disHonouredChequeJelist1 = executeSQLQuery( disHonouredChequeJe, params8.toArray());
                Iterator disHonouredChequeJeitr = disHonouredChequeJelist1.iterator();
                String disHonouredChequeJeStrings = "";
                while (disHonouredChequeJeitr.hasNext()) {
                    Object invdidobj = disHonouredChequeJeitr.next();
                    String invdid = (invdidobj != null) ? invdidobj.toString() : "";
                    disHonouredChequeJeStrings += "'" + invdid + "',";
                }
                if (!StringUtil.isNullOrEmpty(disHonouredChequeJeStrings)) {
                    disHonouredChequeJeStrings = disHonouredChequeJeStrings.substring(0, disHonouredChequeJeStrings.length() - 1);
                }
                
                ArrayList params5 = new ArrayList();
                params5.add(requestParams.get("companyid"));
                params5.add(requestParams.get("companyid"));
                params5.add(requestParams.get("receiptid"));
                delQuery5 = "delete from accjedetailcustomdata where jedetailId in (select id from jedetail where company =? and journalEntry in (select journalentry from receipt where company =? and id =?))";

                int numRows5 = executeSQLUpdate( delQuery5, params5.toArray());


                ArrayList params9 = new ArrayList();
                params9.add(requestParams.get("companyid"));
                params9.add(requestParams.get("receiptid"));
                String myquery1 = "select paydetail from receipt where company = ? and id=?";
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

                //GET THE CHEK DETAILS FOR THE PAYMENT
                List chequeList = new ArrayList();
                String chequeQuery = "select cheque from paydetail where id in(" + journalent + ")";
                if (!StringUtil.isNullOrEmpty(journalent)) {
                    chequeList = executeSQLQuery(chequeQuery);
                }
                Iterator chqItr = chequeList.iterator();
                String chequeIds = "";
                while (chqItr.hasNext()) {
                    Object jeidobj = chqItr.next();
                    String jeidi = (jeidobj != null) ? jeidobj.toString() : "";
                    chequeIds += "'" + jeidi + "',";
                }
                if (!StringUtil.isNullOrEmpty(chequeIds)) {
                    chequeIds = chequeIds.substring(0, chequeIds.length() - 1);
                }
                
                
                ArrayList params1 = new ArrayList();
                params1.add(requestParams.get("companyid"));
                params1.add(requestParams.get("receiptid"));
//                delQuery1 = "delete from receiptdetails where receipt in (select id from receipt where company =? and id =?)";
                delQuery1 = "delete rpd from receiptdetails rpd inner join receipt r on rpd.receipt=r.id where r.company=? and r.id=?";
                int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());

                /*
                    update payment amountdue on refund receipt
                */
                ArrayList paramsRefund = new ArrayList();
                paramsRefund.add(requestParams.get("receiptid"));
                delQuery15 = "update advancedetail payadv inner join receiptadvancedetail recadv on payadv.id = recadv.advancedetailid "
                        + " inner join receipt rec on rec.id = recadv.receipt "
                        + "set payadv.amountdue = (payadv.amountdue + (recadv.amount/recadv.exchangeratefortransaction)) "
                        + "where rec.id = ?";
                int approvalStatusLevel = 0;
                if (requestParams.containsKey("approvalStatusLevel")) {
                    approvalStatusLevel = Integer.parseInt(requestParams.get("approvalStatusLevel").toString());
                }
                if (approvalStatusLevel == Constants.APPROVED_STATUS_LEVEL) {
                    int numparamsRefundRows = executeSQLUpdate(delQuery15, paramsRefund.toArray());     // updates payment amountdue on refund receipt only when receipt is approved
                }
                
                ArrayList params15 = new ArrayList();
                params15.add(requestParams.get("companyid"));
                params15.add(requestParams.get("receiptid"));
//                delQuery15 = "delete from receiptadvancedetail where receipt in (select id from receipt where company =? and id =?)";
                delQuery15 = "delete rad from receiptadvancedetail rad inner join receipt r on rad.receipt=r.id where r.company=? and r.id=?";
                int numRows15 = executeSQLUpdate( delQuery15, params15.toArray());
                
                ArrayList params16 = new ArrayList();
                params16.add(requestParams.get("companyid"));
                params16.add(requestParams.get("receiptid"));
//                delQuery16 = "delete from linkdetailreceipt where receipt in (select id from receipt where company =? and id =?)";
                delQuery16 = " delete ldr from linkdetailreceipt ldr inner join receipt r on ldr.receipt=r.id where r.company=? and r.id=?";
                int numRows16 = executeSQLUpdate( delQuery16, params16.toArray());
                
                ArrayList paramsDNLink = new ArrayList();
                paramsDNLink.add(requestParams.get("companyid"));
                paramsDNLink.add(requestParams.get("receiptid"));
                delQueryDnLink = "delete from linkdetailreceipttodebitnote where receipt in (select id from receipt where company =? and id =?)";
                int numRowsDnLink = executeSQLUpdate( delQueryDnLink, paramsDNLink.toArray());
          
                ArrayList params7 = new ArrayList();
                params7.add(requestParams.get("companyid"));
                params7.add(requestParams.get("receiptid"));
//                delQuery7 = "delete from receiptdetailotherwise where receipt in (select id from receipt where company =? and id =?)";
                delQuery7 = "delete rdo from receiptdetailotherwise rdo inner join receipt r on rdo.receipt=r.id where r.company=? and r.id=?";
                int numRows7 = executeSQLUpdate( delQuery7, params7.toArray());

                ArrayList paramsWriteOffDelete = new ArrayList();
                paramsWriteOffDelete.add(requestParams.get("receiptid"));
                delQueryForWriteOff = "delete from receiptwriteoff where receipt = ? ";
                int writeOffCount = executeSQLUpdate( delQueryForWriteOff, paramsWriteOffDelete.toArray());
                
                ArrayList params24 = new ArrayList();
                String delQuery24 = "delete from openingbalancereceiptcustomdata where openingbalancereceiptid=? ";
                params24.add(requestParams.get("receiptid"));
                int numRows24 = executeSQLUpdate(delQuery24, params24.toArray());
                
                ArrayList params6 = new ArrayList();
                params6.add(requestParams.get("companyid"));
                params6.add(requestParams.get("receiptid"));
                delQuery6 = "update receipt set advanceid=null where company =? and advanceid = ? ";
                int numRows6 = executeSQLUpdate( delQuery6, params6.toArray());
                delQuery6 = "delete from receipt where company =? and id = ?";
                numRows6 = executeSQLUpdate( delQuery6, params6.toArray());

                ArrayList params2 = new ArrayList();
                delQuery2 = "delete from paydetail where id in(" + journalent + ")";
                if (!StringUtil.isNullOrEmpty(journalent)) {
                    numRows2 = executeSQLUpdate( delQuery2, params2.toArray());
                }

                ArrayList params10 = new ArrayList();
                delQuery8 = "delete from paymentmethod where id in(select paymentMethod from paydetail where id in(" + journalent + "))";
                if (!StringUtil.isNullOrEmpty(journalent)) {
                    numRows8 = executeSQLUpdate( delQuery8, params10.toArray());
                }

                int chequeDelRows = 0;
                if (!StringUtil.isNullOrEmpty(chequeIds)) {
                    String chequeDelQuery = "delete from cheque where id in(" + chequeIds + ")";
                    chequeDelRows = executeSQLUpdate(chequeDelQuery);
                }

                ArrayList params3 = new ArrayList();
                params3.add(requestParams.get("companyid"));
                delQuery3 = "delete from jedetail where company = ? and journalEntry in (" + idStrings + ") ";
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    numRows3 = executeSQLUpdate( delQuery3, params3.toArray());
                }
                // delete query for bank charges JE
                delQuery17 = "delete from jedetail where company = ? and journalEntry in (" + bankchargeidStrings + ") ";
                if (!StringUtil.isNullOrEmpty(bankchargeidStrings)) {
                    numRows3 = executeSQLUpdate( delQuery17, params3.toArray());
                }
                // delete query for bank interest JE
                delQuery19 = "delete from jedetail where company = ? and journalEntry in (" + bankinterestidStrings + ") ";
                if (!StringUtil.isNullOrEmpty(bankinterestidStrings)) {
                    numRows3 = executeSQLUpdate( delQuery19, params3.toArray());
                }
                
                delQuery23 = "delete from jedetail where company = ? and journalEntry in (" + disHonouredChequeJeStrings + ") ";
                if (!StringUtil.isNullOrEmpty(disHonouredChequeJeStrings)) {
                    numRows3 = executeSQLUpdate( delQuery23, params3.toArray());
                }  
                
                delQueryForWriteOff = "delete from jedetail where company = ? and journalEntry in (" + writeOffrelatedJE + ") ";
                if(!StringUtil.isNullOrEmpty(writeOffrelatedJE)){
                    numDeletedWriteOffJE = executeSQLUpdate( delQueryForWriteOff, params3.toArray());
                }
                
                ArrayList params12 = new ArrayList();
                List list3 = new ArrayList();
                params12.add(requestParams.get("companyid"));
                String myquery2 = "select bankReconciliation from bankreconciliationdetail where journalEntry in (" + idStrings + ") and company=?";
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    list3 = executeSQLQuery( myquery2, params12.toArray());
                }
                Iterator itr3 = list3.iterator();
                String bankrec = "";
                while (itr3.hasNext()) {
                    Object bankrecobj = itr3.next();
                    String bankrecid = (bankrecobj != null) ? bankrecobj.toString() : "";
                    bankrec += "'" + bankrecid + "',";
                }
                if (!StringUtil.isNullOrEmpty(bankrec)) {
                    bankrec = bankrec.substring(0, bankrec.length() - 1);
                }
                ArrayList params14 = new ArrayList();
                params14.add(requestParams.get("companyid"));
                delQuery12 = "delete from bankreconciliationdetail where journalEntry in (" + idStrings + ") and company =?";
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    numRows13 = executeSQLUpdate( delQuery12, params14.toArray());
                }
                
                ArrayList params18 = new ArrayList();
                params18.add(requestParams.get("companyid"));
                delquery22 = "delete from bankunreconciliationdetail where journalEntry in (" + idStrings + ") and company = ?";
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    numRows22 = executeSQLUpdate( delquery22, params18.toArray());
                }
                
                if (!StringUtil.isNullOrEmpty(bankrec)) {
                    
                    ArrayList params17 = new ArrayList();
                    params17.add(requestParams.get("companyid"));
                    delquery21= "delete from bankreconciliationdetail where bankReconciliation in ("+ bankrec + ") and company = ?";
                    numRows21 = executeSQLUpdate(delquery21, params17.toArray());
                    
                    ArrayList params13 = new ArrayList();
                    params13.add(requestParams.get("companyid"));
                    delQuery13 = "delete from bankreconciliation where id in (" + bankrec + ") and company =?";
                    numRows12 = executeSQLUpdate( delQuery13, params13.toArray());
                }


                  // DELETE THE ENTRIES FOR THE BANK CHARGES AND INTREST FROM RECONCIALTION
               deleteBankRconcialtionEntries(bankchargeidStrings,requestParams);
               deleteBankRconcialtionEntries(bankinterestidStrings,requestParams);  

                ArrayList params4 = new ArrayList();
                delQuery4 = "delete from journalentry where id  in (" + idStrings + ")";
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    numRows4 = executeSQLUpdate( delQuery4, params4.toArray());
                }
                // delete query for bank charges JE
               delQuery18 = "delete from journalentry where id  in (" + bankchargeidStrings + ")";
                if (!StringUtil.isNullOrEmpty(bankchargeidStrings)) {
                    numRows4 = executeSQLUpdate( delQuery18, params4.toArray());
                }
                // delete query for bank interest JE
                delQuery20 = "delete from journalentry where id  in (" + bankinterestidStrings + ")";
                if (!StringUtil.isNullOrEmpty(bankinterestidStrings)) {
                    numRows4 = executeSQLUpdate( delQuery20, params4.toArray());
                }
                //Delete Write Off relted JE's
                delQueryForWriteOff = "delete from journalentry where id  in (" + writeOffrelatedJE + ")";
                if (!StringUtil.isNullOrEmpty(writeOffrelatedJE)) {
                    numDeletedWriteOffJE = executeSQLUpdate( delQueryForWriteOff, params4.toArray());
                }
                
                // delete query for bank DisHonouredChequeJE
                delQuery23 = "delete from journalentry where id  in (" + disHonouredChequeJeStrings + ")";
                if (!StringUtil.isNullOrEmpty(disHonouredChequeJeStrings)) {
                    numRows4 = executeSQLUpdate( delQuery23, params4.toArray());
                }
                
                //updating payment in debit note
                String receiptid = (String) requestParams.get("receiptid");
                KwlReturnObject result2 = getreceipthistory(receiptid);
//                List ls = result2.getEntityList();
//                Iterator<Object[]> itr2 = ls.iterator();
//                while (itr2.hasNext()) {
//                    Object[] row = (Object[]) itr2.next();
//                    String dnid = row[0].toString();
//                    Double amount = Double.parseDouble(row[1].toString());
//                    KwlReturnObject dnidresult = updateDnUpAmount(dnid, amount);
//                    KwlReturnObject dnopeningidresult = updateDnOpeningAmountDueBalance(dnid, amount);
//                }
//                    KwlReturnObject dnhistoryresult = getCustomerDnPaymenyHistory("", 0.0, 0.0, receiptid);
//                    List<DebitNotePaymentDetails> dnHistoryList = dnhistoryresult.getEntityList();
//                    for (DebitNotePaymentDetails dnpd:dnHistoryList) {
//                        String dnid = dnpd.getDebitnote().getID()!=null?dnpd.getDebitnote().getID():"";
//                        Double dnpaidamount = dnpd.getAmountPaid();
//                        Double dnPaidAmountInBaseCurrency =dnpd.getAmountInBaseCurrency();
//                        KwlReturnObject dnjedresult = updateDnAmount(dnid, -dnpaidamount);
//                        KwlReturnObject opendnjedresult = updateDnOpeningAmountDue(dnid, -dnpaidamount);
//                        KwlReturnObject openingdnBaseAmtDueResult = updateDnOpeningBaseAmountDue(dnid,-dnPaidAmountInBaseCurrency);                        
//                        if(dnpd.getDebitnote()!=null && !StringUtil.isNullOrEmpty(dnpd.getDebitnote().getRevalJeId())){
//                            deleteJEDtails(dnpd.getDebitnote().getRevalJeId(),(String)requestParams.get("companyid"));
//                            deleteJEEntry(dnpd.getDebitnote().getRevalJeId(),(String)requestParams.get("companyid"));
//                        }
//                    }
                ArrayList params11 = new ArrayList();
                params11.add(requestParams.get("receiptid"));
                delQuery9 = " delete from debitnotepayment where receiptid=?";
                int numRows9 = executeSQLUpdate( delQuery9, params11.toArray());

                ArrayList params = new ArrayList();
                delQuery = "delete  from accjecustomdata where journalentryId in(" + idStrings + ") ";
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    numRows = executeSQLUpdate( delQuery, params.toArray());
                }

                numtotal = numRows + numRows2 + numRows3 + numRows4 + numRows5 + numRows6 + numRows1 + numRows7 + numRows8 + numRows9 + numRows12 + numRows13+numRows16+numRows15+numRows21+numRowsDnLink;
            }

            return new KwlReturnObject(true, "Receipt has been deleted successfully.", null, null, numtotal);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Receipt as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
        }

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
    public KwlReturnObject getJEFromReceipt(String receiptid) throws ServiceException {
        List list = new ArrayList();
        String query = "select r.journalEntry.ID from Receipt r where r.ID=? and r.company.companyID=r.journalEntry.company.companyID";
        list = executeQuery( query, new Object[]{receiptid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    public KwlReturnObject getDisHonouredJEFromReceipt(String receiptid) throws ServiceException {
        List list = new ArrayList();
        String query = "select r.disHonouredChequeJe.ID from Receipt r where r.ID=? and r.company.companyID=r.journalEntry.company.companyID";
        list = executeQuery( query, new Object[]{receiptid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }    
        
    public KwlReturnObject updateDisHonouredJEFromReceipt(String receiptid,String company) throws ServiceException {
        List list = new ArrayList();
        String query = "update Receipt set disHonouredChequeJe=null where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{receiptid,company});
        return new KwlReturnObject(true, null, null, list, list.size());
    }    
        
        // delete Bank charges JE
        public KwlReturnObject getBankChargeJEFromReceipt(String receiptid) throws ServiceException {
        List list = new ArrayList();
        String query = "select r.journalEntryForBankCharges.ID from Receipt r where r.ID=? and r.company.companyID=r.journalEntryForBankCharges.company.companyID";
        list = executeQuery( query, new Object[]{receiptid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
              // delete Bank Interest JE
    public KwlReturnObject getBankInterestJEFromReceipt(String receiptid) throws ServiceException {
        List list = new ArrayList();
        String query = "select r.journalEntryForBankInterest.ID from Receipt r where r.ID=? and r.company.companyID=r.journalEntryForBankInterest.company.companyID";
        list = executeQuery( query, new Object[]{receiptid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from ReceiptDetail";
        return buildNExecuteQuery( query, requestParams);
    }
    public List<String> getTotalJEDIDReceiptDetails(String receiptid,String companyid) throws ServiceException {
//        String query = "select totalJED.ID from ReceiptDetail where company.companyID=? and receipt.ID=? and totalJED is not null ";
        String query = "select totalJED.ID from ReceiptDetail where receipt.ID=? and totalJED is not null ";
        List<String> detailsList = executeQuery( query,receiptid);
        return detailsList;
    }

    public KwlReturnObject getDebitNotePaymentDetails(HashMap<String, Object> requestParams) throws ServiceException{
        String query = "from DebitNotePaymentDetails";
        return buildNExecuteQuery( query, requestParams);
    }
    public List<String> getTotalJEDIDDebitNotePaymentDetails(String receiptid,String companyid) throws ServiceException {
//        String query = "select totalJED.ID from DebitNotePaymentDetails where receipt.company.companyID=? and receipt.ID=? and totalJED is not null ";
        String query = "select totalJED.ID from DebitNotePaymentDetails where receipt.ID=? and totalJED is not null ";
        List<String> detailsList = executeQuery( query,receiptid);
        return detailsList;
    }
    
    public KwlReturnObject getBillingReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from BillingReceiptDetail";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject deleteBillingReceiptDetails(String receiptid, String companyid) throws ServiceException {
        String delQuery = "delete from BillingReceiptDetail brd where brd.billingReceipt.ID=? and brd.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Billing receipt details has been deleted successfully", null, null, numRows);
    }

    public KwlReturnObject deleteBillingReceipt(String receiptid, String companyid) throws ServiceException {
        String delQuery = "delete from BillingReceipt br where ID=? and br.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Billing receipt details has been deleted successfully", null, null, numRows);
    }

    public KwlReturnObject deleteBillingReceiptEntry(String receiptid, String companyid) throws ServiceException {
        //query = "update BillingReceipt set deleted=true where ID in("+qMarks +") and company.companyID=?";
        String delQuery = "update BillingReceipt set deleted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Billing receipt details has been deleted successfully", null, null, numRows);
    }

    @Override
    public KwlReturnObject getRefundNameCount(String refundNo, String companyid,String vendorId) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "select id,payment.currency.currencyCode,amount,amountDue,payment.creationDate from AdvanceDetail where payment.paymentNumber=? and payment.company.companyID=? and payment.vendor.ID=? and amountDue>0 AND payment.approvestatuslevel=11 and payment.deleted=false";
        list = executeQuery( q, new Object[]{refundNo, companyid,vendorId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    /*
     * Get CURRENCY as per currency Name
     * 
     */
    
    @Override
    public KwlReturnObject getCurrency(String currencyName) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = " from KWLCurrency where name=?";
        list = executeQuery( q, new Object[]{currencyName});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    /*
     * Get PAYMENT METHOD as per method Name
     * 
     */
    
    @Override
    public KwlReturnObject getPaymentMethodCount(String paymentMethodStr, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from PaymentMethod where methodName=? and company.companyID=?";
        list = executeQuery( q, new Object[]{paymentMethodStr,companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
     /*
     * Get ACCOUNT NAME as per accountName
     * Additional check - get Non deleted accounts only.
     */
    
    
    @Override
    public KwlReturnObject getAccountNameCount(String accountName, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from Account where name=? and company.companyID=? and deleted='F'";
        list = executeQuery( q, new Object[]{accountName, companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    
    
    @Override
    public KwlReturnObject getReceiptEditCount(String entryNumber, String companyid, String receiptId) throws ServiceException {
        try {
            List list = new ArrayList();
            int count = 0;
            String query = "from Receipt where receiptNumber=? and company.companyID=? and ID!=?";
            list = executeQuery( query, new Object[]{entryNumber, companyid, receiptId});
            count = list.size();
            return new KwlReturnObject(true, "", null, list, count);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderImpl.updateQAApprovalItems:" + ex.getMessage(), ex);
        }
    }
    
    public KwlReturnObject getReceiptFromBillNo(String billno, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from Receipt where receiptNumber=? and company.companyID=?";
        list = executeQuery( q, new Object[]{billno, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBillingReceiptFromBillNo(String billno, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from BillingReceipt where billingReceiptNumber=? and company.companyID=?";
        list = executeQuery( q, new Object[]{billno, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getReceiptAmountFromInvoice(String invoiceid) throws ServiceException {
        List list = new ArrayList();
        String q = "select sum(amount) from ReceiptDetail rd where rd.invoice.ID=? group by rd.invoice";
        List l = executeQuery( q, new Object[]{invoiceid});
        double amount = (l.isEmpty() ? 0 : (Double) l.get(0));
        list.add(amount);
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getReceiptAmountFromInvoiceNewUI(String invoiceid) throws ServiceException {
        List list = new ArrayList();
        String q = "select sum(amount) from LinkDetailReceipt rd where rd.invoice.ID=? group by rd.invoice";
        List l = executeQuery( q, new Object[]{invoiceid});
        double amount = (l.isEmpty() ? 0 : (Double) l.get(0));
        list.add(amount);
        return new KwlReturnObject(true, "", null, list, list.size());
    }


    @Override
    public KwlReturnObject getReceiptAmountFromBadDebtClaimedInvoice(String invoiceid, boolean isBeforeClaimed) throws ServiceException {
        List list = new ArrayList();
        String q = "select sum(amount) from ReceiptDetail rd where rd.invoice.ID=? and rd.receipt.linkedToClaimedInvoice=? group by rd.invoice";
        List l = executeQuery( q, new Object[]{invoiceid,isBeforeClaimed});
        double amount = (l.isEmpty() ? 0 : (Double) l.get(0));
        list.add(amount);
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getReceiptFromBadDebtClaimedInvoice(String invoiceid, boolean isBeforeClaimed, Date badDebtCalculationToDate) throws ServiceException {
        List list = new ArrayList();
        list.add(invoiceid);
        list.add(isBeforeClaimed);
        String addDate = "";
        if (badDebtCalculationToDate != null) {
            list.add(badDebtCalculationToDate);
//            addDate = " and rd.receipt.journalEntry.entryDate<=?";
            addDate = " and rd.receipt.creationDate<=?";
        }
        String q = "from ReceiptDetail rd where rd.invoice.ID=? and rd.receipt.linkedToClaimedInvoice=? " + addDate;
        List l = executeQuery( q, list.toArray());
//        double amount = (l.isEmpty() ? 0 : (Double) l.get(0));
//        list.add(amount);
        return new KwlReturnObject(true, "", null, l, l.size());
    }

    @Override
    public KwlReturnObject getReceiptFromBadDebtClaimedInvoiceForNewUI(String invoiceid, boolean isBeforeClaimed) throws ServiceException {
        List list = new ArrayList();
        String q = "from LinkDetailReceipt rd where rd.invoice.ID=? and rd.receipt.linkedToClaimedInvoice=?";
        List l = executeQuery( q, new Object[]{invoiceid,isBeforeClaimed});
//        double amount = (l.isEmpty() ? 0 : (Double) l.get(0));
//        list.add(amount);
        return new KwlReturnObject(true, "", null, l, l.size());
    }

    public KwlReturnObject getBillingReceiptAmountFromInvoice(String invoiceid) throws ServiceException {
        List list = new ArrayList();
        String q = "select sum(amount) from BillingReceiptDetail rd where rd.billingReceipt.deleted=false and rd.billingInvoice.ID=? group by rd.billingInvoice";
        List l = executeQuery( q, new Object[]{invoiceid});
        double amount = (l.isEmpty() ? 0 : (Double) l.get(0));
        list.add(amount);
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getReciptFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from Receipt where journalEntry.ID=? and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

     public KwlReturnObject getReciptFromDisHonouredJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from Receipt where disHonouredChequeJe.ID=? and deleted=false and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getBReciptFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from BillingReceipt where journalEntry.ID=? and deleted=false and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getReceiptLinkedInvoiceJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from LinkDetailReceipt where linkedGainLossJE=?";
        List list = executeQuery( selQuery, new Object[]{jeid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getAdvanceReceiptLinkedInvoiceJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from LinkDetailReceipt where linkedGSTJE=?";
        List list = executeQuery( selQuery, new Object[]{jeid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getJEFromBR(String receiptid, String companyid) throws ServiceException {
        String selQuery = "select p.journalEntry.ID from BillingReceipt p where p.ID = ? and p.company.companyID = ?";
        List list = executeQuery( selQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBillingReceiptDetail(String receiptid, String companyid) throws ServiceException {
        //"from BillingReceiptDetail rd  where rd.billingInvoice.ID in ( "+qMarks +")  and rd.billingReceipt.deleted=false and rd.company.companyID=?";
        String selQuery = "from BillingReceiptDetail rd  where rd.billingInvoice.ID = ? and rd.billingReceipt.deleted=false and rd.company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getReceiptCustomerNames(String companyid, String paymentid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(paymentid);
            String condition = " where p.company.companyID=? and jed.debit=false and p.ID=? ";
            String query = "select  ac.name from Receipt p inner join p.journalEntry je inner join je.details jed inner join jed.account ac" + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.getReceiptCustomerNames : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getBillingReceiptCustomerNames(String companyid, String paymentid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(paymentid);
            String condition = " where p.company.companyID=? and jed.debit=false and p.ID=? ";
            String query = "select  ac.name from BillingReceipt p inner join p.journalEntry je inner join je.details jed inner join jed.account ac" + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.getBillingReceiptCustomerNames : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject updateDnAmount(String noteid, double amount) throws ServiceException {
        String delQuery = "update DebitNote set dnamountdue=(dnamountdue-?) where ID=?";
        int numRows = executeUpdate( delQuery, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Payment Method has been deleted successfully.", null, null, numRows);
    }
    /**
     * updateAdvancePaymentAmountDue(JSONObject params) method is used to Update
     * the amount due of advance payment linked to refund receipt.We need to
     * pass Advance Payments detail id amount and a flag isToAddAmount = true if
     * we want to add amount to a current amount due or isToAddAmount = false if
     * we want to subtract the amount due from current amount due. ERP-39559
     * @param params
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject updateAdvancePaymentAmountDue(JSONObject params) throws ServiceException {
        double amount = params.optDouble("amount");
        boolean isToAddAmount = params.optBoolean("isToAddAmount", false);
        String paymentAdvanceDetailId = params.optString("paymentAdvanceDetailId");
        String condition = " (amountdue - ?) ";
        ArrayList paramsForUpdate = new ArrayList();
        paramsForUpdate.add(amount);
        paramsForUpdate.add(paymentAdvanceDetailId);
        if (isToAddAmount) {
            condition = " (amountdue + ?) ";
        }
        String updateAdvancePaymentAmtDueQuery = "UPDATE advancedetail set amountdue = " + condition + " where id=? ";
        int numRows = executeSQLUpdate(updateAdvancePaymentAmtDueQuery, paramsForUpdate.toArray());
        return new KwlReturnObject(true, "Advance Payment amount due updated successfully", null, null, numRows);
    }
    
    /**
     * This method is used to update the amount due of advance payment which user
     * links to refund receipt from receipt report.
     * @param paramJobj
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject updateAdvancePaymentAmountDueLinkedExternally(JSONObject params) throws ServiceException {
        double amount = params.optDouble("amount");
        /**
         * isToAddAmount flag is used to add the passed amount to the current
         * amountdue else it will subtract the passed amount from current
         * amountdue.
         */
        boolean isToAddAmount = params.optBoolean("isToAddAmount", false);
        String paymentAdvanceDetailId = params.optString("paymentid","");
        String companyid = params.optString(Constants.companyid,"");
        String condition = " (amountdue - ?) ";
        ArrayList paramsForUpdate = new ArrayList();
        paramsForUpdate.add(amount);
        paramsForUpdate.add(paymentAdvanceDetailId);
        paramsForUpdate.add(companyid);
        if (isToAddAmount) {
            condition = " (amountdue + ?) ";
        }
        String updateAdvancePaymentAmtDueQuery = "UPDATE advancedetail ad INNER JOIN payment p on ad.payment = p.id set amountdue = " + condition + " where p.id=? and p.company = ? ";
        int numRows = executeSQLUpdate(updateAdvancePaymentAmtDueQuery, paramsForUpdate.toArray());
        return new KwlReturnObject(true, "Advance Payment amount due updated successfully", null, null, numRows);
    }

    @Override
    public KwlReturnObject updateDnOpeningAmountDue(String noteid, double amount) throws ServiceException {
        String query = "update DebitNote set openingBalanceAmountDue=(openingBalanceAmountDue-?) where ID=?";
        int numRows = executeUpdate( query, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Debit Note Amount has been updated successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject updateDnOpeningBaseAmountDue(String noteid, double amount) throws ServiceException {
        String query = "update DebitNote set openingbalancebaseamountdue=(openingbalancebaseamountdue-?) where ID=?";
        int numRows = executeUpdate( query, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Debit Note Amount has been updated successfully.", null, null, numRows);
    }
    
    public KwlReturnObject saveCustomerDnPaymenyHistory(String dnnoteid, double paidncamount, double originalamountdue, String paymentId) throws ServiceException {
        List list = new ArrayList();

        try {
            String uuid = UUID.randomUUID().toString();
            //     String invoiceID = hm.get("invoiceid").toString();
            String query = "insert into debitnotepayment (id,dnid,receiptid,amountdue,amountpaid) values(?,?,?,?,?)";
//            list = executeQuery( query, new Object[]{invoiceID});
            executeSQLUpdate( query, new Object[]{uuid, dnnoteid, paymentId, originalamountdue, paidncamount});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.saveVendorCnPaymenyHistory:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject saveCustomerDnPaymenyHistory(HashMap<String,String> hashMap) throws ServiceException {
        List list = new ArrayList();

        try {
            String uuid = UUID.randomUUID().toString();
            String cnid=hashMap.get("cnnoteid");
            String paymentid=hashMap.get("paymentId");
            double amountdue=Double.parseDouble(hashMap.get("originalamountdue"));
            double amountpaid=Double.parseDouble(hashMap.get("paidncamount"));
            String fromcurrency=hashMap.get("tocurrency");
            String tocurrency=hashMap.get("fromcurrency");
            String exchangeratefortransaction=hashMap.get("exchangeratefortransaction");
            double amountinreceiptcurrency=Double.parseDouble(hashMap.get("amountinpaymentcurrency"));
            double paidamountinreceiptcurrency=Double.parseDouble(hashMap.get("paidamountinpaymentcurrency"));
            String description= StringUtil.DecodeText(hashMap.get("description"));
            String jedetailId=hashMap.get("jedetail")!=null?hashMap.get("jedetail"):"";
            double gstCurrencyRate=Double.parseDouble(hashMap.get("gstCurrencyRate"));
            double amountinbasecurrency = Double.parseDouble(hashMap.get("amountinbasecurrency"));
            int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(hashMap.get("srNoForRow"));
            String query = "insert into debitnotepayment (id,dnid,receiptid,amountdue,amountpaid,tocurrency,fromcurrency,exchangeratefortransaction,amountinreceiptcurrency,paidamountinreceiptcurrency,description,gstcurrencyrate,srnoforrow,totaljedid,amountinbasecurrency) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            executeSQLUpdate( query, new Object[]{uuid,cnid,paymentid,amountdue,amountpaid,tocurrency,fromcurrency,exchangeratefortransaction,amountinreceiptcurrency,paidamountinreceiptcurrency,description,gstCurrencyRate,srNoForRow,jedetailId,amountinbasecurrency});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.saveCustomerDnPaymenyHistory:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getCustomerDnPayment(String receiptId,String dnnoteid) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "From DebitNotePaymentDetails dnpd where dnpd.debitnote.ID=? and dnpd.receipt.ID=? ";
            list =executeQuery( query, new Object[]{dnnoteid,receiptId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorCnPayment:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getCustomerDnPayment(String receiptId) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "From DebitNotePaymentDetails dnpd where dnpd.receipt.ID=? ";
            list =executeQuery( query, new Object[]{receiptId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorCnPayment:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getCustomerDnPaymenyHistory(String dnnoteid, double paidncamount, double originalamountdue, String paymentId) throws ServiceException {
        List list = new ArrayList();

        try {
            if (StringUtil.isNullOrEmpty(dnnoteid)) {
                String query = "From DebitNotePaymentDetails dnpd where dnpd.receipt.ID=?";
                list = executeQuery( query, new Object[]{paymentId});
                query = "delete from debitnotepayment where receiptid=? ";
                executeSQLUpdate( query, new Object[]{paymentId});
            } else {
                String query = "From DebitNotePaymentDetails dnpd where dnpd.debitnote.ID=? and dnpd.receipt.ID=?";
                list = executeQuery( query, new Object[]{dnnoteid, paymentId});
                query = "delete from debitnotepayment where dnid=? and receiptid=? ";
                executeSQLUpdate( query, new Object[]{dnnoteid, paymentId});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.saveVendorCnPaymenyHistory:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getPaymentIdLinkedWithNote(String noteId) throws ServiceException {
        List params = new ArrayList();
        params.add(noteId);
        String query = "select receiptid from debitnotepayment where dnid=?";
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject updateCnAmount(String noteid, double amount) throws ServiceException {
        String delQuery = "update CreditNote set cnamountdue=(cnamountdue-?) where ID=?";
        int numRows = executeUpdate( delQuery, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Payment Method has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject updateCnOpeningAmountDue(String noteid, double amount) throws ServiceException {
        String query = "update CreditNote set openingBalanceAmountDue=(openingBalanceAmountDue-?) where ID=?";
        int numRows = executeUpdate( query, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Credit Note has been updated successfully.", null, null, numRows);
    }

    public KwlReturnObject saveCustomerCnPaymenyHistory(String dnnoteid, double paidncamount, double originalamountdue, String paymentId) throws ServiceException {
        List list = new ArrayList();

        try {
            String uuid = UUID.randomUUID().toString();
            //     String invoiceID = hm.get("invoiceid").toString();
            String query = "insert into receivecreditnotepayment (id,cnid,receiptid,amountdue,amountpaid) values(?,?,?,?,?)";
//            list = executeQuery( query, new Object[]{invoiceID});
            executeSQLUpdate( query, new Object[]{uuid, dnnoteid, paymentId, originalamountdue, paidncamount});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.saveVendorCnPaymenyHistory:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveReceiptDetailOtherwise(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            ReceiptDetailOtherwise receiptDetailOtherwise = null;
            //String paymentid = (String) hm.get("payment");
            String receiptotherwiseid = (String) hm.get("receiptotherwise");

            if (StringUtil.isNullOrEmpty(receiptotherwiseid)) {
                receiptDetailOtherwise = new ReceiptDetailOtherwise();

            } else {
                receiptDetailOtherwise = (ReceiptDetailOtherwise) get(ReceiptDetailOtherwise.class, receiptotherwiseid);
            }

            if (hm.containsKey("amount")) {
                receiptDetailOtherwise.setAmount((Double) hm.get("amount"));
            }
            if (hm.containsKey("taxamount")) {
                receiptDetailOtherwise.setTaxamount((Double) hm.get("taxamount"));
            }
            if (hm.containsKey("description")) {

                receiptDetailOtherwise.setDescription(StringUtil.DecodeText((String) hm.get("description")));
            }
            if (hm.containsKey("taxjedid")) {

                receiptDetailOtherwise.setTaxJedId((String) hm.get("taxjedid"));
            }
            if (hm.containsKey("accountid")) {
                Account account = hm.get("accountid") == null ? null : (Account) get(Account.class, (String) hm.get("accountid"));
                receiptDetailOtherwise.setAccount(account);
            }

            if (hm.containsKey("receipt")) {
                Receipt receipt = hm.get("receipt") == null ? null : (Receipt) get(Receipt.class, (String) hm.get("receipt"));
                receiptDetailOtherwise.setReceipt(receipt);
            }
            if (hm.containsKey("tax")) {
                Tax tax = hm.get("tax") == null ? null : (Tax) get(Tax.class, (String) hm.get("tax"));
                receiptDetailOtherwise.setTax(tax);
            }
            if (hm.containsKey("isdebit")) {
                receiptDetailOtherwise.setIsdebit(Boolean.parseBoolean(hm.get("isdebit").toString()));
            }
            if (hm.containsKey("srNoForRow")) {
                receiptDetailOtherwise.setSrNoForRow(Integer.parseInt(hm.get("srNoForRow").toString()));
            }
            if(hm.containsKey("gstApplied")) {                
                receiptDetailOtherwise.setGstapplied((Tax)hm.get("gstApplied"));
            }
            if (hm.containsKey("jedetail") && hm.get("jedetail")!=null) {
                JournalEntryDetail jed = hm.get("jedetail") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("jedetail"));
                receiptDetailOtherwise.setTotalJED(jed);
            }
            if (hm.containsKey("taxjedetail") && hm.get("taxjedetail")!=null) {
                JournalEntryDetail jed = hm.get("taxjedetail") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("taxjedetail"));
                receiptDetailOtherwise.setGstJED(jed);
            }
            saveOrUpdate(receiptDetailOtherwise);
            list.add(receiptDetailOtherwise);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.savePayment : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Receipt has been updated successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject getReceiptDetailOtherwise(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from ReceiptDetailOtherwise ";
        return buildNExecuteQuery( query, requestParams);
    }

    @Override
    public KwlReturnObject getCalculatedReceivePaymentOtherwiseTax(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList paramslist = new ArrayList();
        String Condition = "";
        String conditionForAccount = "";
        String taxid = (String) requestParams.get("taxid");
        boolean showDishonouredPayment = requestParams.containsKey("showDishonouredPayment") ? (Boolean) requestParams.get("showDishonouredPayment") : false;
        paramslist.add(taxid);

        Tax tax = (Tax) get(Tax.class, taxid);
        /*
         * Below code is commented for ERP-9272
         * Added new column 'appliedgst'  as told by Paritosh sir
         */
//        if (tax != null) {
//            System.out.println(tax.getTaxCode());
//            paramslist.add(tax.getAccount().getID());
//            conditionForAccount += " or rdo.account.ID=? ";
//        }

        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition += " and rdo.receipt.journalEntry.entryDate >= ? and rdo.receipt.journalEntry.entryDate <= ? and rdo.receipt.journalEntry.pendingapproval=0";     //appended rdo.receipt.journalEntry.pendingapproval=0 so that only approved records are displayed in reports
            Condition += " and rdo.receipt.creationDate >= ? and rdo.receipt.creationDate <= ? and rdo.receipt.journalEntry.pendingapproval=0";     //appended rdo.receipt.journalEntry.pendingapproval=0 so that only approved records are displayed in reports
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
		String ss = requestParams.containsKey("ss")?(String) requestParams.get("ss"):"";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"rdo.receipt.journalEntry.entryNumber","rdo.receipt.receiptNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramslist, ss, 2);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                searchQuery=searchQuery.substring(0,searchQuery.lastIndexOf(")"));   //removing last ')' to add to following customer and vendor search             
                searchQuery+=" or (rdo.gstapplied in (select ID from Tax where name like ?)))";     // Changes for ERP_9272
                paramslist.add("%"+ss+"%");
                Condition +=searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        String mySearchFilterString = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", "rdo.receipt.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", " rdo.gstJED.accJEDetailCustomData");
            }
        }
//            if(StringUtil.isNullOrEmpty(ss)==false){
//               for(int i=0;i<=3;i++){
//                 paramslist.add(ss+"%");
//               }
//                 Condition+= " and (rdo.tax.name like ? or rdo.account.name like ?  or rdo.receipt.journalEntry.entryNumber like ? or rdo.receipt.receiptNumber like ? ) ";
//        }

        /*
         * Below code is modified for ERP-9272
         * Added new column 'appliedgst'  as told by Paritosh sir
         */
        String query = "";
        if (showDishonouredPayment) {
            query = "from ReceiptDetailOtherwise rdo where (rdo.gstapplied.ID = ? " + conditionForAccount + ") and rdo.receipt.deleted=false " + Condition + mySearchFilterString;
        } else {
            query = "from ReceiptDetailOtherwise rdo where (rdo.gstapplied.ID = ? " + conditionForAccount + ") and rdo.receipt.deleted=false and rdo.receipt.isDishonouredCheque=false " + Condition + mySearchFilterString;
        }
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }
    
    @Override
    public KwlReturnObject getAdvanceReceiptTax(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList paramslist = new ArrayList();
        String Condition = "";
        boolean showDishonouredPayment = requestParams.containsKey("showDishonouredPayment") ? (Boolean) requestParams.get("showDishonouredPayment") : false;
        String taxid = (String) requestParams.get("taxid");
        paramslist.add(taxid);
        
        Tax tax = (Tax) get(Tax.class, taxid);
        System.out.println(tax.getTaxCode());        

        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition += " and rad.receipt.journalEntry.entryDate >= ? and rad.receipt.journalEntry.entryDate <= ?";
            Condition += " and rad.receipt.creationDate >= ? and rad.receipt.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
        String mySearchFilterString = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", "rad.receipt.journalEntry.accBillInvCustomData");
            }
        }

        String query = "";
        
        if (showDishonouredPayment) {
            query = "from ReceiptAdvanceDetail rad where rad.tax.ID = ? and rad.receipt.deleted=false and rad.receipt.approvestatuslevel=11 " + Condition + mySearchFilterString;
        } else {
            query = "from ReceiptAdvanceDetail rad where rad.tax.ID = ? and rad.receipt.deleted=false and rad.receipt.isDishonouredCheque=false and rad.receipt.approvestatuslevel=11 " + Condition + mySearchFilterString;
        }
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    @Override
    public KwlReturnObject getCalculatedCreditNoteOtherwiseTax(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList paramslist = new ArrayList();
        String Condition = "";
        String conditionForAccount = "";
        String taxid = (String) requestParams.get("taxid");
        paramslist.add(taxid);
        /**
         * Below code commented due to Multiple entries is showing in GST report for
         * CN & DN for GST Type account - ERP-27172.
         */
        
//        Tax tax = (Tax) get(Tax.class, taxid);
//        if (tax != null) {
//            System.out.println(tax.getTaxCode());
//            paramslist.add(tax.getAccount().getID());
//            conditionForAccount += " or cnt.account.ID=? ";
//        }

        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition += " and cnt.creditNote.journalEntry.entryDate >= ? and cnt.creditNote.journalEntry.entryDate <= ?";
            Condition += " and cnt.creditNote.creationDate >= ? and cnt.creditNote.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }

		String ss = requestParams.containsKey("ss")?(String) requestParams.get("ss"):"";

        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"cnt.creditNote.journalEntry.entryNumber", "cnt.creditNote.creditNoteNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramslist, ss, 2);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                searchQuery=searchQuery.substring(0,searchQuery.lastIndexOf(")"));   //removing last ')' to add to following customer and vendor search             
                searchQuery+=" or (cnt.creditNote.vendor in (select ID from Vendor where name like ?)) or (cnt.creditNote.customer in (select ID from Customer where name like ?)) or (cnt.tax in (select ID from Tax where name like ?)) or (cnt.account in (select ID from Account where name like ?)))";
                paramslist.add("%"+ss+"%");
                paramslist.add("%"+ss+"%");
                paramslist.add("%"+ss+"%");
                paramslist.add("%"+ss+"%");
                Condition +=searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        String mySearchFilterString = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", "cnt.creditNote.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", "gstJED.accJEDetailCustomData");
            }
        }
//            if(StringUtil.isNullOrEmpty(ss)==false){
//               for(int i=0;i<=4;i++){
//                 paramslist.add(ss+"%");
//               }
//                 Condition+= " and (cnt.tax.name like ? or cnt.creditNote.journalEntry.entryNumber like ? or cnt.creditNote.creditNoteNumber like ? or (cnt.creditNote.vendor in (select ID from Vendor where name like ?)) or (cnt.creditNote.customer in (select ID from Customer where name like ?))) ";
//        }

        String query = "from CreditNoteTaxEntry cnt where (cnt.tax.ID = ? " + conditionForAccount + ") and cnt.creditNote.deleted=false AND cnt.creditNote.approvestatuslevel=11 AND cnt.creditNote.oldRecord = false " + Condition + mySearchFilterString;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    @Override
    public KwlReturnObject saveBillingReceiptDetailOtherwise(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            BillingReceiptDetailOtherwise receiptDetailOtherwise = null;
            //String paymentid = (String) hm.get("payment");
            String receiptotherwiseid = (String) hm.get("receiptotherwise");

            if (StringUtil.isNullOrEmpty(receiptotherwiseid)) {
                receiptDetailOtherwise = new BillingReceiptDetailOtherwise();

            } else {
                receiptDetailOtherwise = (BillingReceiptDetailOtherwise) get(BillingReceiptDetailOtherwise.class, receiptotherwiseid);
            }

            if (hm.containsKey("amount")) {
                receiptDetailOtherwise.setAmount((Double) hm.get("amount"));
            }
            if (hm.containsKey("taxamount")) {
                receiptDetailOtherwise.setTaxamount((Double) hm.get("taxamount"));
            }
            if (hm.containsKey("description")) {

                receiptDetailOtherwise.setDescription(StringUtil.DecodeText((String) hm.get("description")));
            }
            if (hm.containsKey("taxjedid")) {

                receiptDetailOtherwise.setTaxJedId((String) hm.get("taxjedid"));
            }
            if (hm.containsKey("accountid")) {
                Account account = hm.get("accountid") == null ? null : (Account) get(Account.class, (String) hm.get("accountid"));
                receiptDetailOtherwise.setAccount(account);
            }

            if (hm.containsKey("billingreceipt")) {
                BillingReceipt receipt = hm.get("billingreceipt") == null ? null : (BillingReceipt) get(BillingReceipt.class, (String) hm.get("billingreceipt"));
                receiptDetailOtherwise.setBillingReceipt(receipt);
            }
            if (hm.containsKey("tax")) {
                Tax tax = hm.get("tax") == null ? null : (Tax) get(Tax.class, (String) hm.get("tax"));
                receiptDetailOtherwise.setTax(tax);
            }
            if (hm.containsKey("isdebit")) {
                receiptDetailOtherwise.setIsdebit(Boolean.parseBoolean(hm.get("isdebit").toString()));
            }
            saveOrUpdate(receiptDetailOtherwise);
            list.add(receiptDetailOtherwise);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.savePayment : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Billing Receipt has been updated successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject getBillingReceiptDetailOtherwise(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from BillingReceiptDetailOtherwise ";
        return buildNExecuteQuery( query, requestParams);
    }

    @Override
    public KwlReturnObject deleteReceiptDetailsOtherwise(String receiptid) throws ServiceException {
        String delQuery = "delete from ReceiptDetailOtherwise brd where brd.receipt.ID=? ";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid});
        return new KwlReturnObject(true, "Receipt details has been deleted successfully", null, null, numRows);
    }

    @Override
    public KwlReturnObject deleteBillingReceiptDetailsOtherwise(String receiptid) throws ServiceException {
        String delQuery = "delete from BillingReceiptDetailOtherwise brd where brd.billingReceipt.ID=? ";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid});
        return new KwlReturnObject(true, "Billing receipt details has been deleted successfully", null, null, numRows);
    }

    @Override
    public KwlReturnObject getreceipthistory(String receiptid) throws ServiceException {
        String query = "select dnid,amountpaid,amountdue from debitnotepayment where receiptid=? ";
        ArrayList params = new ArrayList();
        params.add(receiptid);
//        List list = executeQuery( query, params.toArray());
        List listSql = executeSQLQuery( query, params.toArray());
        int count = listSql.size();
        return new KwlReturnObject(true, "", "", listSql, count);
    }

    @Override
    //for retrieving the information of account grid in payment 2nd option
    public KwlReturnObject getaccounthistory(String receiptid) throws ServiceException {
        String query = "select tax,amount,description,taxamount,account from receiptdetailotherwise where receipt=? ";
        ArrayList params = new ArrayList();
        params.add(receiptid);
//        List list = executeQuery( query, params.toArray());
        List listSql = executeSQLQuery( query, params.toArray());
        int count = listSql.size();
        return new KwlReturnObject(true, "", "", listSql, count);
    }

    @Override
    public KwlReturnObject gettotalrecordOfreceiptno(String receiptno) throws ServiceException {
        ArrayList params = new ArrayList();
        List list = new ArrayList();
        params.add(receiptno);
        String q = "from Receipt receipt where receipt.receiptNumber=?";
        list = executeQuery( q, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject updateDnUpAmount(String noteid, double amount) throws ServiceException {
        String delQuery = "update DebitNote set dnamountdue=(dnamountdue+?) where ID=?";
        int numRows = executeUpdate( delQuery, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Payment Method has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject updateDnOpeningAmountDueBalance(String noteid, double amount) throws ServiceException {
        String query = "update DebitNote set openingBalanceAmountDue=(openingBalanceAmountDue+?) where ID=?";
        int numRows = executeUpdate( query, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Debit Note Amount Due has been updated successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject getDuplicateForNormalReceipt(String entryNumber, String companyid, String receiptid, String advanceId, Receipt receipt) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = " ";
        params.add(companyid);
        params.add(receiptid);
        if (!StringUtil.isNullOrEmpty(entryNumber)) {
            condition += " and receiptNumber=?";
            params.add(entryNumber);
        }
        if (receipt != null && !StringUtil.isNullOrEmpty(receipt.getCndnAndInvoiceId())) {
            condition += " and ID!=?";
            params.add(receipt.getCndnAndInvoiceId());
        }
        if (!StringUtil.isNullOrEmpty(advanceId)) {
            condition += " and ID!=?";
            params.add(advanceId);
        } else {
            List<Receipt> receipts = find("from Receipt where company.companyID='" + companyid + "' and advanceid.ID='" + receiptid + "'");
            if (!receipts.isEmpty()) {
                condition += " and ID NOT IN (";
                for (Receipt receiptObj : receipts) {
                    condition += "'" + receiptObj.getID() + "',";
                }
                condition = condition.substring(0, condition.lastIndexOf(","));
                condition += ")";
            }
            String q = "from Receipt where company.companyID=? and ID!=? " + condition;
            list = executeQuery( q, params.toArray());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getCurrentSeqNumberForAdvance(String sequenceformat, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "select max(seqnumber) from receipt where seqformat=? and  company =? ";
        list = executeSQLQuery( q, new Object[]{sequenceformat, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());

    }

    @Override
    public KwlReturnObject getInvoiceAdvPaymentList(HashMap<String, String> payHashMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String paymentId = payHashMap.get("paymentId");
            int invoiceAdvCNDN = !StringUtil.isNullOrEmpty(payHashMap.get("invoiceadvcndntype")) ? Integer.parseInt(payHashMap.get("invoiceadvcndntype")) : 0;
            String condition = "";
            if (!StringUtil.isNullOrEmpty(paymentId)) {
                Receipt receipt = (Receipt) get(Receipt.class, paymentId);
                List params = new ArrayList();
                if (receipt.isIsadvancepayment() && receipt.getInvoiceAdvCndnType() == 2) {
                    condition += " where advanceid.ID=? ";
                    params.add(receipt.getID());
                }
                if (receipt.getInvoiceAdvCndnType() == 1 || receipt.getInvoiceAdvCndnType() == 3) {
                    if (receipt.getAdvanceid() != null) {
                        condition += " where ID = ? ";
                        params.add(receipt.getAdvanceid().getID());
                    }

                    if (!StringUtil.isNullOrEmpty(receipt.getCndnAndInvoiceId())) {
                        if (StringUtil.isNullOrEmpty(condition)) {
                            condition += " where ";
                        } else {
                            condition += " or ";
                        }
                        condition += " ID = ? ";
                        params.add(receipt.getCndnAndInvoiceId());
                    }

                }

                String query = "from Receipt " + condition;
                if(!StringUtil.isNullOrEmpty(condition)){
                    list = executeQuery( query, params.toArray());
                }
                list.add(receipt);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorCnPayment:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public Receipt getReceiptObject(Receipt receipt) throws ServiceException {
        List list = new ArrayList();
        Receipt paymentObject = null;
        try {
            if (receipt != null) {
                if (receipt.getInvoiceAdvCndnType() == 1 && !StringUtil.isNullOrEmpty(receipt.getCndnAndInvoiceId())) {
                    paymentObject = (Receipt) get(Receipt.class, receipt.getCndnAndInvoiceId());
                } else if (receipt.getInvoiceAdvCndnType() == 2) {
                    List<Receipt> paymentList = find("from Receipt where invoiceAdvCndnType=3 and advanceid.ID='" + receipt.getID() + "'");
                    if (!paymentList.isEmpty()) {
                        paymentObject = paymentList.get(0);
                    }
                }

            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorCnPayment:" + ex.getMessage(), ex);
        }
        return paymentObject;
    }

    @Override
    public List getAdvanceReceiptUsedInRefundPayment(String receiptadvancedetailid) throws ServiceException {
        List list = new ArrayList(); 
        try {
            String query = "select p.paymentnumber, p.currency, adv.amountdue, p.id, p.isopeningbalencepayment,p.journalentry from advancedetail adv inner join payment p on p.id = adv.payment where adv.receiptadvancedetail= ?";
            list = executeSQLQuery( query, new Object[]{receiptadvancedetailid});
        } catch(Exception ex) {
            throw ServiceException.FAILURE("getAdvanceReceiptUsedInRefundPayment:" + ex.getMessage(), ex);
        }
        return list;
    }
    @Override
    public List getAdvanceReceiptUsedSalesOrder(JSONObject params) throws ServiceException {
        List list = new ArrayList();
        String receiptid = params.optString("receiptid", "");
        try {
            String query = " select distinct soling.docid, slo.sonumber, slo.id from solinking soling inner join salesorder slo on soling.docid = slo.id where slo.deleteflag='F' and approvestatuslevel=11 and isdraft=0 and linkeddocid in "
                    + "(select rad.id from receiptadvancedetail rad inner join receipt re on re.id = rad.receipt where re.id = ? )";
            list = executeSQLQuery(query, new Object[]{receiptid});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAdvanceReceiptUsedSalesOrder:" + ex.getMessage(), ex);
        }
        return list;
    }
    @Override
    public List getAdvancePaymentDetails(String receiptadvancedetailid) throws ServiceException {
        List list = new ArrayList(); 
        try {
            String query = "select distinct p.id,p.currency,p.journalentry,adv.amountdue,p.isopeningbalencepayment,adv.exchangeratefortransaction from advancedetail adv inner join payment p on p.id = adv.payment where adv.id= ? ";
            list = executeSQLQuery( query, new Object[]{receiptadvancedetailid});
        } catch(Exception ex) {
            throw ServiceException.FAILURE("getAdvancePaymentDetails:" + ex.getMessage(), ex);
        }
        return list;
    }
    public KwlReturnObject getDebitNotePaymentDetail(HashMap<String, Object> reqMap) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        DateFormat df = (DateFormat) reqMap.get(Constants.df);
        String condition = "";
        try {
        if (reqMap.containsKey("debitnoteid")) {
            condition += " WHERE dnp.dnid=? ";
            params.add((String) reqMap.get("debitnoteid"));
        }
        if (reqMap.containsKey("asofdate") && reqMap.get("asofdate") != null) {
//            condition += condition.isEmpty() ? " WHERE journalentry.entrydate<=? " : " AND journalentry.entrydate<=? ";
            condition += condition.isEmpty() ? " WHERE receipt.creationdate<=? " : " AND receipt.creationdate<=? ";
                String asOfDate = (String) reqMap.get("asofdate");
                params.add(df.parse(asOfDate));
        }

        condition += "and receipt.isdishonouredcheque='F'";
        
            String query = "SELECT dnp.exchangeratefortransaction,dnp.paidamountinreceiptcurrency,dnp.amountpaid FROM debitnotepayment AS dnp "
                    + " INNER JOIN receipt ON receipt.id = dnp.receiptid "
                    + " INNER JOIN journalentry ON journalentry.id=receipt.journalentry "
                    + condition;
            list = executeSQLQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.getrDnPaymentDetails:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getReceiptAdvanceDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from ReceiptAdvanceDetail";
        return buildNExecuteQuery( query, requestParams);
    }
    public List<String> getTotalJEDIDReceiptAdvanceDetails(String receiptid,String companyid) throws ServiceException {
        String query = "select totalJED.ID from ReceiptAdvanceDetail where receipt.ID=? and totalJED is not null ";
        List<String> detailsList = executeQuery( query,receiptid);
        return detailsList;
    }
    
    public KwlReturnObject getPayDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from PayDetail";
        return buildNExecuteQuery( query, requestParams);
    }

    @Override
    public List getMulticurrencyReceiptsWithPCToPMCRateOne(String companyId) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select r.id as receiptid,r.receiptnumber as receiptnumber,r.company as companyid,co.subdomain as companysubdomain,"
                    + "c.currencyid as currencyid from receipt r inner join paydetail pd on pd.id=r.paydetail "
                    + "inner join paymentmethod pm on pd.paymentmethod=pm.id "
                    + "inner join account a on a.id=pm.account "
                    + "inner join currency c on c.currencyid=a.currency "
                    + "inner join company co on r.company=co.companyid where r.paymentcurrencytopaymentmethodcurrencyrate=1 "
                    + "and r.currency<>c.currencyid and r.deleteflag='F' and co.companyid=?";
            list = executeSQLQuery( query, new Object[]{companyId});
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("accReceiptImpl.getMulticurrencyReceiptsWithPCToPMCRateOne:"+ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public KwlReturnObject getPaymentInformationFromAdvanceDetailId(String documentId, String companyId) throws ServiceException{
     List list = new ArrayList();
     ArrayList params = new ArrayList();
     params.add(documentId);
     params.add(companyId);
//     String query = "select externalcurrencyrate,journalentry from payment where id in (select payment from advancedetail where id = ? and company = ? )";
     String query = " select p.externalcurrencyrate,p.journalentry,p.id,p.paymentnumber,p.currency,je.entrydate, v.account, advd.amountdue, p.isdishonouredcheque from payment p "
             + " inner join advancedetail advd on advd.payment=p.id "
             + " left join journalentry je on je.id = p.journalentry "
             + " left join vendor v on v.id = p.vendor "
             + " where advd.id = ? and advd.company =? ";
        try {
            list = executeSQLQuery( query, params.toArray());
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("accReceiptImpl.getPaymentInformationFromAdvanceDetailId:" + ex.getMessage(), ex);
        }
     return new KwlReturnObject(true, "", null, list, list.size());
    }

    /**
     * This function is used to get Advance details of payment.
     * @param paramJobj
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getAdvanceDetailInformationFromPaymentId(JSONObject paramJobj) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String paymentId = paramJobj.optString("paymentid", "");
        String companyId = paramJobj.optString(Constants.companyid, "");
        params.add(paymentId);
        params.add(companyId);
        String query = " select advd.amount, advd.amountdue, advd.exchangeratefortransaction from advancedetail advd "
                + " inner join payment p on advd.payment=p.id "
                + " where p.id = ? and advd.company =? ";
        list = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getReceiptsForVendor(String personId, String companyId) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(personId);
        params.add(companyId);
        String query="";
        query= "from Receipt r where  r.vendor = ? and r.company.companyID = ? and r.deleted = false";
        list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getDnReceiptHistory(String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            if (!StringUtil.isNullOrEmpty(companyid)) {
                String query = "From DebitNotePaymentDetails dnp where dnp.receipt.company.companyID=? ";
                list = executeQuery( query, new Object[]{companyid});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.getDnReceiptHistory:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject saveDebitNotePaymentDetails(HashMap<String,Object> datamap) throws ServiceException {
        List list = new ArrayList();
        try{
            DebitNotePaymentDetails dnd = new DebitNotePaymentDetails();
            if (datamap.containsKey("id")) {
                dnd = (DebitNotePaymentDetails) get(DebitNotePaymentDetails.class, datamap.get("id").toString());
            }
            if (datamap.containsKey("srno")) {
                dnd.setSrno((Integer) datamap.get("srno"));
            }
            if (datamap.containsKey("amountDue")) {
                dnd.setAmountDue((Double) datamap.get("amountDue"));
            }
            if (datamap.containsKey("amountPaid")) {
                dnd.setAmountPaid((Double) datamap.get("amountPaid"));
            }
            if (datamap.containsKey("exchangeRateForTransaction")) {
                dnd.setExchangeRateForTransaction((Double) datamap.get("exchangeRateForTransaction"));
            }
            if (datamap.containsKey("amountInPaymentCurrency")) {
                dnd.setAmountInReceiptCurrency((Double) datamap.get("amountInPaymentCurrency"));
            }
            if (datamap.containsKey("amountInBaseCurrency")) {
                dnd.setAmountInBaseCurrency((Double) datamap.get("amountInBaseCurrency"));
            }
            if (datamap.containsKey("paidAmountDueInBaseCurrency")) {
                dnd.setPaidAmountDueInBaseCurrency((Double) datamap.get("paidAmountDueInBaseCurrency"));
            }
            if (datamap.containsKey("exchangeRateCurrencyToBase")) {
                dnd.setExchangeRateCurrencyToBase((Double) datamap.get("exchangeRateCurrencyToBase"));
            }
            if (datamap.containsKey("gstCurrencyRate")) {
                dnd.setGstCurrencyRate((Double) datamap.get("gstCurrencyRate"));
            }
            saveOrUpdate(dnd);
            list.add(dnd);
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveDebitNotePaymentDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size())    ;
    }

    @Override
     public synchronized String UpdateReceiptEntry(Map<String, Object> seqNumberMap) {
         String documnetNumber= "";
        try {
            documnetNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
            int seqNumber = 0;
            if(seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())){
               seqNumber= Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
            }
            String datePrefix = seqNumberMap.containsKey(Constants.DATEPREFIX) ? (String)seqNumberMap.get(Constants.DATEPREFIX) : "";
            String dateafterPrefix = seqNumberMap.containsKey(Constants.DATEAFTERPREFIX) ? (String) seqNumberMap.get(Constants.DATEAFTERPREFIX) : "";
            String dateSuffix = seqNumberMap.containsKey(Constants.DATESUFFIX) ? (String)seqNumberMap.get(Constants.DATESUFFIX) : "";
            String sequenceFormatID = seqNumberMap.containsKey(Constants.SEQUENCEFORMATID) ? (String)seqNumberMap.get(Constants.SEQUENCEFORMATID) : "";
            String documentID = seqNumberMap.containsKey(Constants.DOCUMENTID) ? (String)seqNumberMap.get(Constants.DOCUMENTID) : "";
            String companyID = seqNumberMap.containsKey(Constants.companyKey) ? (String)seqNumberMap.get(Constants.companyKey) : "";
            String query = "update Receipt set receiptNumber = ?,seqnumber=?,datePreffixValue=?,dateafterpreffixvalue=?, dateSuffixValue=?,seqformat.ID=? where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{documnetNumber, seqNumber,datePrefix,dateafterPrefix, dateSuffix,sequenceFormatID, documentID, companyID});
        } catch (Exception e) {
            System.out.println(e);
        }
        return documnetNumber;
    }
    @Override
     public synchronized String UpdateReceiptEntryForNA(String recid, String entrynumber) {
        try {
            String query = "update Receipt set receiptNumber = ? where ID=?";
            int numRows = executeUpdate( query, new Object[]{entrynumber,recid});
        } catch (Exception e) {
            System.out.println(e);
        }
        return entrynumber;
    }
     
    public KwlReturnObject getPaymentReceiptsForJE(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        String hqlQuery = "from Receipt receipt where receipt.company.companyID=? and receipt.journalEntry is not null";
        list = list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getNormalReceipts(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get("companyid");
            String query = "from Receipt where company.companyID = ? and normalReceipt=? ";
            list = executeQuery( query, new Object[]{companyid, true});
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            return new KwlReturnObject(true, "", null, list, list.size());
        }
    }

    @Override
    public KwlReturnObject updateReceipt(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String receiptid = (String) requestParams.get("receiptid");
            Receipt receipt = (Receipt) get(Receipt.class, receiptid);
            if (receipt != null) {
                if (requestParams.containsKey("depositamountinbase") && requestParams.get("depositamountinbase") != null) {
                    receipt.setDepositamountinbase(Double.parseDouble(requestParams.get("depositamountinbase").toString()));
                }
                saveOrUpdate(receipt);
            }
            list.add(receipt);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.updateReceipt : " + ex.getMessage(), ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, list.size());
        }
    }
    
    @Override
    public KwlReturnObject getReceiptLinkedDebitNoteJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from LinkDetailReceiptToDebitNote L where L.linkedGainLossJE.ID=?";
        List list = executeQuery( selQuery, new Object[]{jeid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public List<LinkDetailReceiptToDebitNote> getDeletedLinkedReceiptDebitNotes(Receipt receipt, String linkedDetailInvoice, String companyid) throws ServiceException {
        String selQuery = "from LinkDetailReceiptToDebitNote ld where ld.receipt.ID=? and ld.company.companyID=? ";
        if(!StringUtil.isNullOrEmpty(linkedDetailInvoice)) {
           selQuery = selQuery.concat(" and ld.id not in (" + linkedDetailInvoice + ")");
        }
        List<LinkDetailReceiptToDebitNote> details = find(selQuery, new Object[]{receipt.getID(), companyid});
        return details;
    }

    @Override
    public KwlReturnObject deleteSelectedLinkedReceiptDebitNotes(String receiptid, String linkedDetailIDs, String companyid) throws ServiceException {
        String delQuery = "delete from LinkDetailReceiptToDebitNote brd where brd.receipt.ID=? and brd.company.companyID=?";
        if(!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
           delQuery = delQuery.concat(" and brd.id not in (" + linkedDetailIDs + ")");
        }
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Receipt linked debit notes have been deleted successfully", null, null, numRows);
    }

    @Override
    public KwlReturnObject getAdvanceReceiptIdLinkedWithNote(String noteId) throws ServiceException {
        List params = new ArrayList();
        params.add(noteId);
        //To show distinct id in case of partial receive payment as like in Invoice.
        //String query = "select distinct receipt from linkdetailreceipttodebitnote where debitnote=?";
        String query = "select distinct receipt,receiptlinkdate from linkdetailreceipttodebitnote where debitnote=?";
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
     public KwlReturnObject getReceiptWriteOffJEs(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
            String ss = (String) request.get(Constants.ss);
            
            ArrayList params = new ArrayList();
            String condition = "";
            params.add(companyid);
            if (request.containsKey("receiptid") && request.get("receiptid") != null) {
                String receiptid = (String) request.get("receiptid");
                condition += " and rwo.receipt.ID=? ";
                params.add(receiptid);
            }
            
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and rwo.journalEntry.ID IN(" + jeIds + ")";
            }
            
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"rwo.journalEntry.entryNumber", "rwo.memo"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }
            
            String query = "from ReceiptWriteOff rwo where rwo.company.companyID=? " + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accReceiptImpl.getReceiptWriteOffJEs:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    @Override
    public KwlReturnObject getReverseReceiptWriteOffJEs(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
            String ss = (String) request.get(Constants.ss);

            ArrayList params = new ArrayList();
            String condition = "";
            params.add(companyid);
            if (request.containsKey("receiptid") && request.get("receiptid") != null) {
                String receiptid = (String) request.get("receiptid");
                condition += " and rwo.receipt.ID=? ";
                params.add(receiptid);
            }
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and rwo.reversejournalEntry.ID IN(" + jeIds + ")";
            }

            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"rwo.reversejournalEntry.entryNumber", "iwo.memo"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }
            String query = "from ReceiptWriteOff rwo where rwo.company.companyID=? " + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accReceiptImpl.getReverseReceiptWriteOffJEs:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    public KwlReturnObject getReceiptWriteOffEntries(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        String condition = "";
        try {
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            if (requestParams.containsKey("receiptid") && requestParams.get("receiptid") != null) {
                String receiptid = (String) requestParams.get("receiptid");
                condition += " and RWO.receipt.ID=? ";
                params.add(receiptid);
            }
            if (requestParams.containsKey("onlyOpeningWrittenOffReceipts") && requestParams.get("onlyOpeningWrittenOffReceipts") != null) {
                boolean onlyOpeningReceipts = Boolean.parseBoolean(requestParams.get("onlyOpeningWrittenOffReceipts").toString());
                if(onlyOpeningReceipts){
                    condition += " and RWO.receipt.isOpeningBalenceReceipt = true ";
                }
                
            }
            if (requestParams.containsKey("customerid") && requestParams.get("customerid") != null) {
                String customerid = requestParams.get("customerid").toString();
                condition += " and RWO.receipt.customer.ID = ? ";
                params.add(customerid);
            }
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                String companyId = (String) requestParams.get("companyid");
                condition += " and RWO.company.companyID=? ";
                params.add(companyId);
            }
            if (requestParams.containsKey("asofdate") && requestParams.get("asofdate") != null) {
                String asOfDate = (String) requestParams.get("asofdate");
                condition += "  and RWO.writeOffDate<=? ";
                params.add(df.parse(asOfDate));
            }
            if (requestParams.containsKey("startDate") && requestParams.get("startDate") != null && requestParams.containsKey("endDate") && requestParams.get("endDate") != null) {
                Date startDate = (Date) requestParams.get("startDate");
                Date endDate = (Date) requestParams.get("endDate");
                condition += " and (RWO.writeOffDate >=? and RWO.writeOffDate <=?) ";
                params.add(startDate);
                params.add(endDate);
            }
            String selQuery = "from ReceiptWriteOff RWO  where RWO.isRecovered=false " + condition;
            list = executeQuery( selQuery, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    public KwlReturnObject deleteReceiptsDetailsLoanAndUpdateAmountDue(String receiptid, String companyid) throws ServiceException {
        //Delete Payment Details
        String selQuery = "from ReceiptDetailLoan rd where rd.receipt.ID=? and rd.company.companyID=?";
        List<ReceiptDetailLoan> details = find(selQuery, new Object[]{receiptid, companyid});
        List<RepaymentDetails> list = new ArrayList<>();
        for (ReceiptDetailLoan receiptDetail : details) {
            RepaymentDetails obj = receiptDetail.getRepaymentDetail();
            double amountdue = obj.getAmountdue();
            obj.setAmountdue(amountdue + receiptDetail.getAmountInRepaymentDetailCurrency());
            obj.setPaymentStatus(PaymentStatus.Unpaid);
            list.add(obj);
        }
        if (!list.isEmpty()) {
            saveAll(list);
        }
        return deleteReceiptDetailsLoan(receiptid, companyid);
    }
    
    public KwlReturnObject deleteReceiptDetailsLoan(String receiptid, String companyid) throws ServiceException {
        String delQuery = "delete from ReceiptDetailLoan rd where rd.receipt.ID=? and rd.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Receipt details has been deleted successfully", null, null, numRows);
    }
    
    public String sortColumnPayment(String Col_Name, String Col_Dir, boolean isOpeningPayment) throws ServiceException {
        String String_Sort = "";
        if (Col_Name.equals("billno")) {
            String_Sort = " order by r.receiptNumber " + Col_Dir;
        } else if (Col_Name.equals("personname")) {
            if(isOpeningPayment){
                String_Sort = " order by r.customer.name " + Col_Dir;
            } else {
                String_Sort = " order by ac.name " + Col_Dir;
            }    
        } else if (Col_Name.equals("billdate")) {
              String_Sort = " order by r.creationDate " + Col_Dir;
//            if(isOpeningPayment){
//                String_Sort = " order by r.creationDate " + Col_Dir;
//            } else {
//                String_Sort = " order by r.journalEntry.entryDate " + Col_Dir;
//            }    
        } else if (Col_Name.equals("entryno")) {
            if(isOpeningPayment){
                String_Sort = "";
            } else {
                String_Sort = " order by r.journalEntry.entryNumber " + Col_Dir;
            }
        } else if (Col_Name.equals("paymentmethod")) {
            if(isOpeningPayment){
                String_Sort = "";
            } else {
                String_Sort = " order by pd.paymentMethod.methodName " + Col_Dir;
            }   
        } else if (Col_Name.equals("chequenumber")) {
            if(isOpeningPayment){
                String_Sort = " order by r.chequeNumber+'0' " + Col_Dir;
            } else {
                String_Sort = " order by chk.chequeNo+'0' " + Col_Dir;
            }
        } else if (Col_Name.equals("chequedescription")) {
            if(isOpeningPayment){
                String_Sort = "";
            } else {
                String_Sort = " order by chk.description " + Col_Dir;
            }
        } else if (Col_Name.equals("memo")) {
            if(isOpeningPayment){
                String_Sort = "";
            } else {
                String_Sort = " order by r.memo " + Col_Dir;
            }
        } else {
            String_Sort = " order by r.creationDate " + Col_Dir;
//            if(isOpeningPayment){
//                String_Sort = " order by r.creationDate " + Col_Dir;
//            } else {
//                String_Sort = " order by r.journalEntry.entryDate " + Col_Dir;
//            }
        }
        return String_Sort;
    }
    @Override
    public KwlReturnObject getAdvanceDetailsByReceipt(HashMap<String, Object> requestParams) throws ServiceException{
        List list = new ArrayList();
        String condition = "";
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                ArrayList params = new ArrayList();
                DateFormat df = (DateFormat) requestParams.get(Constants.df);
                String companyId = (String) requestParams.get("companyid");
                params.add(companyId);
                if (requestParams.containsKey("receiptid") && requestParams.get("receiptid") != null) {
                    String receiptid = (String) requestParams.get("receiptid");
                    condition += " and advDet.receiptAdvanceDetails.receipt.ID=? ";
                    params.add(receiptid);
                }
                if(requestParams.containsKey("asofdate") && requestParams.get("asofdate")!=null){
                    String asOfDate = (String) requestParams.get("asofdate");
//                    condition += "  and advDet.payment.journalEntry.entryDate<=? ";
                    condition += "  and advDet.payment.creationDate<=? ";
                    params.add(df.parse(asOfDate));
                }
                String selQuery = "from AdvanceDetail advDet where advDet.company.companyID=? " + condition;
                list = executeQuery( selQuery, params.toArray());
            }
        } catch (ParseException | ServiceException ex) {
            Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    @Override
    public List getAdvancePaymentUsedInRefundReceipt(String advancedetailid) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select p.paymentnumber, p.currency, adv.amountdue, p.id from advancedetail adv inner join payment p on p.id = adv.payment where adv.id= ? ";
            list = executeSQLQuery(query, new Object[]{advancedetailid});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAdvancePaymentUsedInRefundReceipt:" + ex.getMessage(), ex);
        }
        return list;
    }

    /*Purpose to Save linking information of Receive Payment while linking with any transaction */
    @Override
    public KwlReturnObject saveReceiptLinking(HashMap<String, Object> reqParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String receiptid = (String) reqParams.get("docid");
            ReceiptLinking receiptlinking = new ReceiptLinking();
            if (reqParams.containsKey("docid")) {
                Receipt receipt = (Receipt) get(Receipt.class, receiptid);
                receiptlinking.setDocID(receipt);
            }
            if (reqParams.containsKey("moduleid")) {
                receiptlinking.setModuleID((Integer) reqParams.get("moduleid"));
            }
            if (reqParams.containsKey("linkeddocid")) {
                receiptlinking.setLinkedDocID((String) reqParams.get("linkeddocid"));
            }
            if (reqParams.containsKey("linkeddocno")) {
                receiptlinking.setLinkedDocNo((String) reqParams.get("linkeddocno"));
            }
            if (reqParams.containsKey("sourceflag")) {
                receiptlinking.setSourceFlag((Integer) reqParams.get("sourceflag"));
            }
            saveOrUpdate(receiptlinking);
            list.add(receiptlinking);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.saveReceiptLinking : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /*Purpose to Save linking information of Debit Note while linking with any transaction */

    @Override
    public KwlReturnObject updateEntryInDebitNoteLinkingTable(HashMap<String, Object> request) throws ServiceException {
        String newitemID = UUID.randomUUID().toString();
        String linkeddocid = (String) request.get("linkeddocid");
        String docid = (String) request.get("docid");
        int moduleid = (Integer) request.get("moduleid");
        int sourceFlag = (Integer) request.get("sourceflag");
        String linkeddocno = (String) request.get("linkeddocno");

        String query = "insert into  debitnotelinking(id,docid,linkeddocid,linkeddocno,moduleid,sourceflag) values(" + '"' + newitemID + '"' + ',' + '"' + docid + '"' + ',' + '"' + linkeddocid + '"' + ',' + '"' + linkeddocno + '"' + ',' + '"' + moduleid + '"' + ',' + '"' + sourceFlag + '"' + ")";
        int numRows = executeSQLUpdate(query, new String[]{});
        return new KwlReturnObject(true, "Debit Note Linking has been saved successfully.", null, null, numRows);
    }
    /* Method is used to Delete entry from linking table of Receive Payment*/

    @Override
    public KwlReturnObject deleteLinkingInformationOfRP(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        int numRows1 = 0, numRows2 = 0, numRows3 = 0, numRows4 = 0, numRowsTotal = 0;
        try {
            String delQuery = "";
            if (requestParams.containsKey("unlinkflag") && requestParams.get("unlinkflag") != null && Boolean.parseBoolean(requestParams.get("unlinkflag").toString())) {

                params.add(requestParams.get("linkedTransactionID"));
                params.add(requestParams.get("receiptid"));

                delQuery = "delete from DebitNoteLinking dn where dn.DocID.ID=? and dn.LinkedDocID=?";
                numRows1 = executeUpdate(delQuery, params.toArray());
                delQuery = "delete from InvoiceLinking inv where inv.DocID.ID=? and inv.LinkedDocID=?";
                numRows2 = executeUpdate(delQuery, params.toArray());
                // for deleting payment linking
                delQuery = "delete from PaymentLinking pl where pl.DocID.ID=? and pl.LinkedDocID=?";
                numRows4 = executeUpdate(delQuery, params.toArray());
                delQuery = "delete from ReceiptLinking inv where inv.LinkedDocID=? and inv.DocID.ID=?";
                numRows3 = executeUpdate(delQuery, params.toArray());

            } else {
                params.add(requestParams.get("receiptid"));

                delQuery = "delete from DebitNoteLinking dn where dn.LinkedDocID=?";
                numRows1 = executeUpdate(delQuery, params.toArray());

                delQuery = "delete from InvoiceLinking inv where inv.LinkedDocID=?";
                numRows2 = executeUpdate(delQuery, params.toArray());
                // for deleting payment linking
                delQuery = "delete from PaymentLinking pl where pl.LinkedDocID=?";
                numRows4 = executeUpdate(delQuery, params.toArray());

                delQuery = "delete from ReceiptLinking rp where rp.DocID.ID=?";
                numRows3 = executeUpdate(delQuery, params.toArray());
            }

            numRowsTotal = numRows1 + numRows2 + numRows3;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("", ex);
        }
        return new KwlReturnObject(true, "", null, null, numRowsTotal);
    }
    
    /**
     * Description: get LinkDetailReceiptToAdvancePayment objects which to be deleted
     * @param <receipt> used to get id of Receipt
     * @param <linkedDetailIDs> used to get LinkDetailReceiptToAdvancePayment ids which not to be deleted
     * @param <companyid> used to get company id 
     * @return List
     * @throws ServiceException 
     */
    @Override
    public List<LinkDetailReceiptToAdvancePayment> getDeletedLinkedReceiptAdvancePayment(Receipt receipt, String linkedDetailIDs, String companyid) throws ServiceException {
        String selQuery = "from LinkDetailReceiptToAdvancePayment ld where ld.receipt.ID=? and ld.company.companyID=? ";
        if (!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
            selQuery = selQuery.concat(" and ld.id not in (" + linkedDetailIDs + ")");
        }
        List<LinkDetailReceiptToAdvancePayment> details = find(selQuery, new Object[]{receipt.getID(), companyid});
        return details;
    }
    
    /**
     * Description : Method id used to get Payment information like id, paymentnumber, advancedetail id, amountdue from payment id
     * @param <paymentId> used to get Payment id
     * @param <companyId> used to get company id 
     * @return KwlReturnObject
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject getPaymentInformationFromPaymentId(String paymentId, String companyId) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(paymentId);
        params.add(companyId);
        String query = " select p.id as paymentid, p.paymentnumber, advd.id, advd.amountdue as advdetailid, p.isdishonouredcheque as isPaymentDishonoured from payment p "
                + " inner join advancedetail advd on advd.payment=p.id "
                + " where p.id = ? and p.company =? ";
        try {
            list = executeSQLQuery(query, params.toArray());
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("accReceiptImpl.getPaymentInformationFromPaymentId:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    /**
     * Description : Method is used to delete LinkDetailReceiptToAdvancePayment from receipt id, companyid and not listed detail ids in linkedDetailIDs
     * @param <receiptid> used to get receiptid
     * @param <linkedDetailIDs> used to get link detail ids
     * @param <companyid> used to get company id 
     * @return KwlReturnObject
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject deleteSelectedLinkedReceiptAdvanceDetails(String receiptid, String linkedDetailIDs, String companyid) throws ServiceException {
        String delQuery = "delete from LinkDetailReceiptToAdvancePayment ldr where ldr.receipt.ID=? and ldr.company.companyID=?";
        if (!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
            delQuery = delQuery.concat(" and ldr.id not in (" + linkedDetailIDs + ")");
        }
        int numRows = executeUpdate(delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Receipt linked Advance Payment have been deleted successfully.", null, null, numRows);
    }
    
    /**
     * Description : Method is used to delete LinkDetailReceiptToAdvancePayment and update amount due of receipt
     * @param <receiptid> used to get receipt id
     * @param <companyid> used to get company id
     * @return KwlReturnObject
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject deleteLinkReceiptsDetailsToAdvancePaymentAndUpdateAmountDue(String receiptid, String companyid, boolean tempCheck) throws ServiceException {
        // Delete Payment Details
        String selQuery = "from LinkDetailReceiptToAdvancePayment pd where pd.receipt.ID=? and pd.company.companyID=?";
        List<LinkDetailReceiptToAdvancePayment> details = find(selQuery, new Object[]{receiptid, companyid});
        for (LinkDetailReceiptToAdvancePayment linkDetailReceipt : details) {
            KwlReturnObject paymentResult = getPaymentInformationFromPaymentId(linkDetailReceipt.getPaymentId(), companyid);
            Object[] paymentInfoObjArr = (Object[]) paymentResult.getEntityList().get(0);

            // for updating amount due of advance payment
            String query = "Update advancedetail set amountdue = (amountdue + ?) where id = ?";
            executeSQLUpdate(query, new Object[]{linkDetailReceipt.getAmountInPaymentCurrency(), (String) paymentInfoObjArr[2]});
        }
        /*
         * tempCheck is for temporary delete receipt
         * if receipt is temporary delete then tempCheck is set as true
         */
        if (tempCheck) {
            return new KwlReturnObject(true, "Payment Details has been deleted successfully.", null, null, 0);
        } else {
            return deleteLinkReceiptToAdvancePaymentDetails(receiptid, companyid);
        }
    }
    
    /**
     * Description : Method is used for delete LinkDetailReceiptToAdvancePayment for recieptid and companyid
     * @param <receiptid> used to get receiptid
     * @param <companyid> used to get companyid
     * @return KwlReturnObject
     * @throws ServiceException 
     */
    public KwlReturnObject deleteLinkReceiptToAdvancePaymentDetails(String receiptid, String companyid) throws ServiceException {
        String delQuery = "delete from LinkDetailReceiptToAdvancePayment LD where LD.receipt.ID=? and LD.company.companyID=?";
        int numRows = executeUpdate(delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Receipt details has been deleted successfully", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject deleteLinkReceiptToSalesOrder(String receiptid, String companyid) throws ServiceException {
        String delQuery = "delete from solinking where linkeddocid in ("+receiptid+") "   ;
        int numRows = executeSQLUpdate(delQuery);
        return new KwlReturnObject(true, "SO Linking Information has been deleted successfully", null, null, numRows);
    }

    @Override
    public List getAdvanceReceiptLinkedWithRefundPayment(String receiptid,String companyid) throws ServiceException {
        List list = new ArrayList(); 
        try {
            String query = "select p.id, p.paymentnumber from linkdetailpaymenttoadvancepayment ldr inner join payment p on p.id = ldr.payment where ldr.receipt = ? and ldr.company = ? ";
            list = executeSQLQuery( query, new Object[]{receiptid,companyid});
        } catch(Exception ex) {
            throw ServiceException.FAILURE("getAdvanceReceiptUsedInRefundPayment:" + ex.getMessage(), ex);
        }
        return list;
    }
    
      public void deleteBankRconcialtionEntries(String idStrings, HashMap requestParams) throws ServiceException {

        if (!StringUtil.isNullOrEmpty(idStrings)) { // IF ID STRING IS NOT NULL
            List list = new ArrayList();
            ArrayList params1 = new ArrayList();
            params1.add(requestParams.get("companyid"));
            String myquery = "select bankReconciliation from bankreconciliationdetail where journalEntry in (" + idStrings + ") and company=?";   // GET BANK RECONCIALTION ENTRIES
            list = executeSQLQuery(myquery, params1.toArray());
            Iterator itr = list.iterator();
            String bankrec = "";
            while (itr.hasNext()) {
                Object bankrecobj = itr.next();
                String bankrecid = (bankrecobj != null) ? bankrecobj.toString() : "";
                bankrec += "'" + bankrecid + "',";
            }
            if (!StringUtil.isNullOrEmpty(bankrec)) {
                bankrec = bankrec.substring(0, bankrec.length() - 1);
            }

            ArrayList params2 = new ArrayList();
            params2.add(requestParams.get("companyid"));
            String delQuery1 = "delete from bankreconciliationdetail where journalEntry in (" + idStrings + ") and company =?"; // DELETE RECONCILATION DETAILS
            int numRows1 = executeSQLUpdate(delQuery1, params2.toArray());

            ArrayList params3 = new ArrayList();
            params3.add(requestParams.get("companyid"));
            String delquery2 = "delete from bankunreconciliationdetail where journalEntry in (" + idStrings + ") and company = ?";// DELETE UN-RECONCILATION DETAILS
            int numRows2 = executeSQLUpdate(delquery2, params3.toArray());

            if (!StringUtil.isNullOrEmpty(bankrec)) { // DELETE FOR THE ENTRIES RECONCILED
                ArrayList params4 = new ArrayList();
                params4.add(requestParams.get("companyid"));
                String delquery3 = "delete from bankreconciliationdetail where bankReconciliation in (" + bankrec + ") and company = ?";
                int numRows3 = executeSQLUpdate(delquery3, params4.toArray());

                ArrayList params5 = new ArrayList();
                params5.add(requestParams.get("companyid"));
                String delQuery4 = "delete from bankreconciliation where id in (" + bankrec + ") and company =?";
                int numRows4 = executeSQLUpdate(delQuery4, params5.toArray());
            }

        }

    }
      @Override
    public KwlReturnObject getDataSTRealisationDateWiseReport(HashMap<String, Object> requestParams) throws ServiceException {
         List list = new ArrayList();
        int count = 0; 
        try {
            DateFormat df = null;
            String companyid= "";
            String groupby= "";
            
            if(requestParams.containsKey("companyid") && requestParams.get("companyid")!=null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())){
                companyid = requestParams.get("companyid").toString();
            }
            if (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df)!=null && !StringUtil.isNullOrEmpty(requestParams.get(Constants.df).toString())) {
                df = (DateFormat) requestParams.get(Constants.df);
            }
            
            ArrayList params = new ArrayList();
            
            String receivePayment = "select 1 as transType, je.entrydate as date, inv.invoicenumber as refno, cust.name as partyname, 'Taxable Service' as category, inv.invoiceamountinbase as totalamount, sum(invdtm.termamount/je.externalcurrencyrate) as totaltax,0.0 as paidamount, 0.0 as inputcredit, 0.0 as balancecredit, invdtm.assessablevalue, rec.receiptnumber as receiptno, rcpd.amountinbasecurrency as receiptamount"
                    + " from receipt rec "
                    + " inner join receiptdetails rcpd  on rec.id = rcpd.receipt "
                    + " inner join invoice inv on inv.id = rcpd.invoice "
                    + " left join customer cust on cust.id = inv.customer  "
                    + " left join journalentry je on je.id = rec.journalentry  "
                    + " left join invoicedetails invd on invd.invoice = inv.id"
                    + " left join invoicedetailtermsmap invdtm on invdtm.invoicedetail = invd.id"
                    + " left join linelevelterms llt on invdtm.term = llt.id"
                    + " where inv.deleteflag = 'F' and rec.deleteflag = 'F' and ( llt.termtype = 4 or llt.termtype = 5 or llt.termtype = 6 ) " ;
            if(!StringUtil.isNullOrEmpty(companyid)){
                receivePayment += " and inv.company = ? ";
                params.add(companyid);
            }
            groupby += " group by refno, receiptno ";
            receivePayment += groupby;
            
            String advancedreceivePayment = "select 1 as transType, je.entrydate as date, inv.invoicenumber as refno, cust.name as partyname, 'Taxable Service' as category, inv.invoiceamountinbase as totalamount, sum(invdtm.termamount/je.externalcurrencyrate) as totaltax,0.0 as paidamount, 0.0 as inputcredit, 0.0 as balancecredit, invdtm.assessablevalue, rec.receiptnumber as receiptno, ldp.amount/ldp.exchangeratefortransaction as receiptamount"
                    + " from linkdetailreceipt ldp "
                    + " inner join invoice inv on ldp.invoice= inv.id "
                    + " inner join receipt rec on ldp.receipt= rec.id "
                    + " left join customer cust on cust.id = inv.customer  "
                    + " left join journalentry je on je.id = rec.journalentry  "
                    + " left join invoicedetails invd on invd.invoice = inv.id"
                    + " left join invoicedetailtermsmap invdtm on invdtm.invoicedetail = invd.id"
                    + " left join linelevelterms llt on invdtm.term = llt.id"
                    + " where inv.deleteflag = 'F' and rec.deleteflag = 'F' and ( llt.termtype = 4 or llt.termtype = 5 or llt.termtype = 6 ) " ;
            if(!StringUtil.isNullOrEmpty(companyid)){
                advancedreceivePayment += " and inv.company = ? ";
                params.add(companyid);
            }
            advancedreceivePayment += groupby;
            
            String unionQry=" ("
                        + " (" + receivePayment + ") "
                        + " UNION "
                        + " (" + advancedreceivePayment + ") "
                        + " ) ";
            String mysqlquery = " select * from "+unionQry+" as t1";
            
            String startDate = "";
            String endDate = "";
            if(requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate)!=null && !StringUtil.isNullOrEmpty(requestParams.get(Constants.REQ_startdate).toString())){
                 startDate = (String) requestParams.get(Constants.REQ_startdate);
            }
            if(requestParams.containsKey(Constants.REQ_enddate) && requestParams.get(Constants.REQ_enddate)!=null && !StringUtil.isNullOrEmpty(requestParams.get(Constants.REQ_enddate).toString())){
                 endDate = (String) requestParams.get(Constants.REQ_enddate);
            }
            
            String conditionQuery = "";
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate) && df !=null) {
                conditionQuery += " t1.date >=? and t1.date <=? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            String orderQuery = " order by t1.date ASC ";
            mysqlquery +=" where" +conditionQuery+orderQuery;
            
            list = executeSQLQuery(mysqlquery,params.toArray());
            count = list.size();
            
        } catch ( ParseException | ServiceException | NumberFormatException ex) {
            Logger.getLogger(accInvoiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getReceiptDetailsLinkedWithInvoices(HashMap<String, Object> requestMap) throws ServiceException {
        ArrayList params = new ArrayList();
        List<Object> list = new ArrayList<Object>();
        params.add((String) requestMap.get("companyid"));

        String condition = "";
        String Searchjson = "";
        String moduleid = "";
        if (requestMap.containsKey("invoiceid") && requestMap.get("invoiceid") != null) {
            condition += " and rd.invoice.ID=? ";
            params.add((String) requestMap.get("invoiceid"));
        }

        if (requestMap.containsKey(Constants.moduleid) && requestMap.get(Constants.moduleid) != null) {
            moduleid = requestMap.get(Constants.moduleid).toString();
        }

        if (requestMap.containsKey("startDate") && requestMap.get("startDate") != null && requestMap.containsKey("endDate") && requestMap.get("endDate") != null) {//All PaymentDetails between start date and end date 
//            condition += " and (rd.receipt.journalEntry.entryDate>=? and rd.receipt.journalEntry.entryDate<=?) ";
            condition += " and (rd.receipt.creationDate>=? and rd.receipt.creationDate<=?) ";
            params.add((Date) requestMap.get("startDate"));
            params.add((Date) requestMap.get("endDate"));
        } else if (requestMap.containsKey("endDate") && requestMap.get("endDate") != null) {
//            condition += " and (rd.receipt.journalEntry.entryDate <?) ";
            condition += " and (rd.receipt.creationDate <?) ";
            params.add((Date) requestMap.get("endDate"));
        }

        String searchDefaultFieldSQL = "";
        String mySearchFilterString = "";
        String appendCase = "and";
        String paymentDetailJoin = " ";
        boolean applyInvoiceSearch = false;
        HashMap<String, Object> reqParams1 = new HashMap<String, Object>();
        if (requestMap.containsKey("searchJson") && requestMap.get("searchJson") != null) {
            Searchjson = requestMap.get("searchJson").toString();
        }
        String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
        if (requestMap.containsKey("filterConjuctionCriteria") && requestMap.get("filterConjuctionCriteria") != null) {
            filterConjuctionCriteria = requestMap.get("filterConjuctionCriteria").toString();
        }
        if (!StringUtil.isNullOrEmpty(Searchjson)) {
            try {
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
                    Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, moduleid, tableArray, filterConjuctionCriteria);
//                    innerJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                    searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                    searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("receiptRef", "rd.receipt");
                    searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("receiptlinkingRef", "rl");
                    if (searchDefaultFieldSQL.contains("inv")) {
                        applyInvoiceSearch = true;
                    }
                }
                /*
                  Advance search case for Custome field
                */
                if (customSearchFieldArray.length() > 0) {   
                    reqParams1.put(Constants.Searchjson, Searchjson);
                    reqParams1.put(Constants.appendCase, appendCase);
                    reqParams1.put("isPaymentFromInvoice", true);
                    reqParams1.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    reqParams1.put(Constants.moduleid, moduleid);
                    mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(reqParams1, true).get(Constants.myResult));
                    if (mySearchFilterString.contains("accinvoicecustomdata")) {
                        applyInvoiceSearch = true;
                        mySearchFilterString = mySearchFilterString.replaceAll("accinvoicecustomdata", "invje.accBillInvCustomData");
                    }
                    mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "rd.receipt.journalEntry.accBillInvCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "rd.totalJED.accJEDetailCustomData");//        
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "rd.totalJED.accJEDetailsProductCustomData");
                    if (mySearchFilterString.contains("CustomerCustomData")) {
                        mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "rd.receipt.customer.accCustomerCustomData");
                    }
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
                mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
            } catch (Exception ex) {
                Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        /*
          If applied on invoice custom field
        */
        if (applyInvoiceSearch) {
            paymentDetailJoin = " inner join rd.invoice inv "
                    + "inner join inv.journalEntry invje ";
        }
        String q = "select rd from ReceiptDetail rd " + paymentDetailJoin + " where rd.receipt.company.companyID=? and rd.receipt.deleted = 'F' " + condition + mySearchFilterString;
        list = executeQuery(q, params.toArray());

        return new KwlReturnObject(true, "", null, list, list.size());

    }
    
    @Override
    public KwlReturnObject approvePendingReceivePayment(String cnID, String companyid, int status) throws ServiceException  {
        ArrayList params = new ArrayList();
        params.add(status);
        params.add(cnID);
        params.add(companyid);
        String query = "update Receipt set approvestatuslevel = ? where ID=? and company.companyID=?";
        int numRows = executeUpdate(query, params.toArray());
        return new KwlReturnObject(true, "Payment has been updated successfully.", null, null, numRows);
}

    @Override
    public KwlReturnObject rejectPendingReceivePayment(String cnid, String companyid) throws ServiceException{
         try {
            String query = "update Receipt set deleted=true,approvestatuslevel = (-approvestatuslevel) where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{cnid, companyid});
            return new KwlReturnObject(true, "Receive Payment has been rejected successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.rejectPendingReceivePayment : " + ex.getMessage(), ex);
        }
    }
    public KwlReturnObject saveAdvanceDetailsTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ReceiptAdvanceDetailTermMap termmap = new ReceiptAdvanceDetailTermMap();

            if (dataMap.containsKey("id") && !StringUtil.isNullOrEmpty(dataMap.get("id").toString())) {
                termmap = (ReceiptAdvanceDetailTermMap) get(ReceiptAdvanceDetailTermMap.class, (String) dataMap.get("id"));
                if (termmap == null) {
                    termmap = new ReceiptAdvanceDetailTermMap();
                }
            }
            if (dataMap.containsKey("termamount") && !StringUtil.isNullOrEmpty(dataMap.get("termamount").toString())) {
                termmap.setTermamount(Double.parseDouble(dataMap.get("termamount").toString()));
            }
            if (dataMap.containsKey("termpercentage")) {
                termmap.setPercentage(Double.parseDouble(dataMap.get("termpercentage").toString()));
            }
            if (dataMap.containsKey("podetails")) {
                String purchaseorderdetail = (String) dataMap.get("podetails");
                termmap.setReceiptAdvanceDetail(purchaseorderdetail);
            }

            if (dataMap.containsKey("userid")) {
                User userid = (User) get(User.class, (String) dataMap.get("userid"));
                termmap.setCreator(userid);
            }
            if (dataMap.containsKey("createdOn") && !StringUtil.isNullOrEmpty(dataMap.get("createdOn").toString())) {
                termmap.setCreatedOn(((Date) dataMap.get("createdOn")).getTime());
            }
            if (dataMap.containsKey("purchasevalueorsalevalue")) {
                termmap.setPurchaseValueOrSaleValue(Double.parseDouble(dataMap.get("purchasevalueorsalevalue").toString()));
            }
            if (dataMap.containsKey("deductionorabatementpercent")) {
                termmap.setDeductionOrAbatementPercent(Double.parseDouble(dataMap.get("deductionorabatementpercent").toString()));
            }
            if (dataMap.containsKey("assessablevalue")) {
                termmap.setAssessablevalue(Double.parseDouble(dataMap.get("assessablevalue").toString()));
            }
            if (dataMap.containsKey("taxtype")) {
                termmap.setTaxType(Integer.parseInt(dataMap.get("taxtype").toString()));
            }
            if (dataMap.containsKey("isDefault")) {
                termmap.setIsGSTApplied(Boolean.parseBoolean(dataMap.get("isDefault").toString()));
            }
            if (dataMap.containsKey("productentitytermid")) {
                EntitybasedLineLevelTermRate term = (EntitybasedLineLevelTermRate) get(EntitybasedLineLevelTermRate.class, (String) dataMap.get("productentitytermid"));
                termmap.setEntitybasedLineLevelTermRate(term);
            }
            saveOrUpdate(termmap);
            list.add(termmap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderImpl.savePurchaseOrderDetailsTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getAdvanceDetailsTerm(JSONObject paramObj) throws ServiceException, JSONException {
        String invdId = paramObj.optString("adId");
        List params = new ArrayList();
        params.add(invdId);
        String query = " From ReceiptAdvanceDetailTermMap rm where rm.receiptAdvanceDetail=? ";
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getTDSJEmappingTerm(String jeid, String companyid) throws ServiceException, JSONException {
        List params = new ArrayList();
        params.add(jeid);
        params.add(companyid);
        
        String goodReceiptQuery = " SELECT grd.tdsJEMapping.journalEntry.ID From GoodsReceiptDetail grd where  grd.tdsJEMapping IS NOT NULL AND grd.tdsJEMapping.journalEntry.ID=? AND grd.goodsReceipt.company.companyID=? ";
        List list = executeQuery(goodReceiptQuery, params.toArray());
                     
        String expenseQuery =" SELECT erd.tdsJEMapping.journalEntry.ID From ExpenseGRDetail erd where erd.tdsJEMapping IS NOT NULL AND erd.tdsJEMapping.journalEntry.ID=? AND erd.goodsReceipt.company.companyID=? ";
        List listExpense = executeQuery(expenseQuery, params.toArray());
        
        list.addAll(listExpense);
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    /**
     * Save Mapping of invoice-Receipt-JE for TDS and TCS
     * @param params
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject saveReceiptInvoiceJEMapping(JSONObject params) throws ServiceException {
        List list = new ArrayList();
        try {
            ReceiptInvoiceJEMapping receiptInvoiceJEMapping = new ReceiptInvoiceJEMapping();
            String docid = params.optString("id");
            if (StringUtil.isNullOrEmpty(docid)) {
                receiptInvoiceJEMapping = new ReceiptInvoiceJEMapping();
            } else {
                receiptInvoiceJEMapping = (ReceiptInvoiceJEMapping) get(ReceiptInvoiceJEMapping.class, docid);
            }
            if (params.has("receiptid")) {
                receiptInvoiceJEMapping.setReceipt((Receipt) get(Receipt.class, params.optString("receiptid")));
            }
            if (params.has("invoiceid")) {
                receiptInvoiceJEMapping.setInvoice((Invoice) get(Invoice.class, params.optString("invoiceid")));
            }
            if (params.has("journalid")) {
                receiptInvoiceJEMapping.setJournalEntry((JournalEntry) get(JournalEntry.class, params.optString("journalid")));
            }
            if (params.has("gstjeid")) {
                receiptInvoiceJEMapping.setGstAdjustment((JournalEntry) get(JournalEntry.class, params.optString("gstjeid")));
            }
            if (params.has("companyid")) {
                receiptInvoiceJEMapping.setCompany((Company) get(Company.class, params.optString("companyid")));
            }
            if(params.has("invoiceamountdue")){
                receiptInvoiceJEMapping.setInvoiceamountdue(params.optDouble("invoiceamountdue"));
            }
            if(params.has("invoiceamountdueinbase")){
                receiptInvoiceJEMapping.setInvoiceamountdueinbase(params.optDouble("invoiceamountdueinbase"));
            }
            saveOrUpdate(receiptInvoiceJEMapping);
            list.add(receiptInvoiceJEMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.saveReceiptInvoiceJEMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Receipt Invoice JE Mapping has been saved successfully", null, list, list.size());
    }
/**
 * get JE-Receipt-Invoice Mapping for TDS TCS
 * @param paramObj
 * @return
 * @throws ServiceException
 * @throws JSONException 
 */
    public KwlReturnObject getReceiptInvoiceJEMapping(JSONObject paramObj) throws ServiceException, JSONException {
        List params = new ArrayList();
        String condition = "";
        if (paramObj.has("journalentryid")) {
            String jeid = paramObj.optString("journalentryid");
            params.add(jeid);
            condition = "rim.journalEntry.ID=?";
        }
        if (paramObj.has("receiptid")) {
            String receiptid = paramObj.optString("receiptid");
            params.add(receiptid);
            condition = "rim.receipt.ID=?";
        }

        String query = " From ReceiptInvoiceJEMapping  rim where " + condition;
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public int deleteReceiptInvoiceJEMapping(JSONObject paramObj) throws ServiceException, JSONException {
        String jeid = paramObj.optString("journalentryid");
        List params = new ArrayList();
        params.add(jeid);
        String query = " delete from  ReceiptInvoiceJEMapping  rim where rim.journalEntry.ID=?";
        int list = executeUpdate(query, params.toArray());
        return list;
    }
    
    @Override
    public KwlReturnObject checkTransactionsForDiscountOnPaymentTerms(JSONObject paramJobj) throws ServiceException {
        List params = new ArrayList();
        String companyid = paramJobj.optString("companyid","");
        params.add(companyid);
        params.add(companyid);

        String receiptQuery = "SELECT count(*) from  receiptdetails where discountamount>0 and company=? union SELECT count(*) from  paymentdetail where discountamount>0 and company=? ";
        List list = executeSQLQuery(receiptQuery, params.toArray());
        
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    /**
     * This method is used to update amount due of refund payment linked with
     * advance receipt internally i.e while creating refund payment and linking
     * advance receipt at line level.
     * @param paramJobj
     * @return
     * @throws ServiceException
     */
    @Override
    public int updateRefundPaymentLinkedWithAdvance(JSONObject paramJobj) throws ServiceException {
        List params = new ArrayList();
        String companyid = paramJobj.optString(Constants.companyid, "");
        String receiptadvancedetail = paramJobj.optString("receiptadvancedetail", "");
        boolean isToRevertAmtDue = paramJobj.optBoolean("isToRevertAmtDue", false);
        String condition = " amountdue = amount ";
        if (isToRevertAmtDue) {
            condition = " amountdue = 0 ";
        }
        String updateQuery = "update advancedetail set " + condition + " where receiptadvancedetail = ? and company = ? ";
        params.add(receiptadvancedetail);
        params.add(companyid);
        int count = executeSQLUpdate(updateQuery, params.toArray());
        return count;
    }
 
    /**
     * This method is used to update the amount due of refund payment which user
     * links to advance receipt from payment report.
     * @param paramJobj
     * @return
     * @throws ServiceException
     */
    @Override
    public int updateRefundPaymentExternallyLinkedWithAdvance(JSONObject paramJobj) throws ServiceException {
        List params = new ArrayList();
        String companyid = paramJobj.optString(Constants.companyid, "");
        String receiptId = paramJobj.optString("receiptId", "");
        double amountDue = paramJobj.optDouble("amountDue");
        String updateQuery = "UPDATE advancedetail ad INNER JOIN linkdetailpaymenttoadvancepayment ldp on ad.payment = ldp.payment "
                + "set ad.amountdue = ? where ad.receiptadvancedetail is null and ldp.receipt = ?  and ldp.company = ? ";
        params.add(amountDue);
        params.add(receiptId);
        params.add(companyid);
        int count = executeSQLUpdate(updateQuery, params.toArray());
        return count;
    }
    
    /**
     * This method is used to get Linking information of refund payment linked
     * with advance receipt on the basis of receipt.
     * @param paramJobj
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getRefundPaymentLinkDetailsLinkedWithAdvance(JSONObject paramJobj) throws ServiceException {
        List params = new ArrayList();
        List list = new ArrayList();
        String companyid = paramJobj.optString(Constants.companyid, "");
        String receiptId = paramJobj.optString("receiptId", "");
        String selectQuery = "SELECT ad.amountdue, ldp.amountinpaymentcurrency, ldp.exchangeratefortransaction from advancedetail ad "
                + "INNER JOIN linkdetailpaymenttoadvancepayment ldp on ad.payment = ldp.payment "
                + "where ad.receiptadvancedetail is null and ldp.receipt = ? and ldp.company = ?";
        params.add(receiptId);
        params.add(companyid);
        list = executeSQLQuery(selectQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    /**
     * This function is used to get details of refund payment linked to advance
     * receipt internally i.e when the refund payment is created by linking the
     * receipt at line level.
     * @param paramJobj
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getRefundPaymentDetailsLinkedToAdvance(JSONObject paramJobj) throws ServiceException {
        List list = new ArrayList();
        List params = new ArrayList();
        String companyid = paramJobj.optString(Constants.companyid, "");
        String receiptadvancedetail = paramJobj.optString("receiptadvancedetail", "");
        String q = "select amount, amountdue, exchangeratefortransaction from advancedetail where receiptadvancedetail=? and company=?";
        params.add(receiptadvancedetail);
        params.add(companyid);
        list = executeSQLQuery(q, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    
    public List getSalesReceiptKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException{
        List ll = null;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String customerid = (String) requestParams.get("custVendorID");
            boolean isAgedDetailsReport = requestParams.containsKey("isAgedDetailsReport") ? (Boolean)requestParams.get("isAgedDetailsReport"): false;
            boolean isSalesPersonAgedReport = requestParams.containsKey("isSalesPersonAgedReport") ? (Boolean)requestParams.get("isSalesPersonAgedReport"): false;
            
            int datefilter = requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            String custQuery = "";
            boolean includeExcludeChildCmb=false;
            if (requestParams.containsKey("includeExcludeChildCmb") && requestParams.get("includeExcludeChildCmb") != null) {
                includeExcludeChildCmb = (Boolean) requestParams.get("includeExcludeChildCmb");
            }
            if (!StringUtil.isNullOrEmpty(customerid) && !customerid.equals("All")) {
                String[] customers = customerid.split(",");
                StringBuilder custValues = new StringBuilder();
                for (String customer : customers) {
                    custValues.append("'").append(customer).append("',");
                }
                String custStr = custValues.substring(0, custValues.lastIndexOf(","));
                if (includeExcludeChildCmb) {
                    custQuery += " and (cust.id IN (" + custStr + ") or cust.parent IN (" + custStr + "))";
                } else {
                    custQuery += " and cust.id IN (" + custStr + ")";
                }
            }else if(!includeExcludeChildCmb){
                custQuery += " and cust.parent is  null";
            }
            DateFormat origdf = authHandler.getDateOnlyFormat();
            String duedateStr = (String)requestParams.get("enddate");
            String asofdateStr= (requestParams.containsKey("asofdate") && requestParams.get("asofdate") != null) ? (String) requestParams.get("asofdate") : duedateStr;
            Date duedate = origdf.parse(duedateStr);
            Date asofdate = origdf.parse(asofdateStr);
            DateFormat mysqldf = new SimpleDateFormat("yyyy-MM-dd");
            duedateStr = mysqldf.format(duedate);
            ArrayList paramsAdvSearch = new ArrayList();
            ArrayList paramsAdvSearch1= new ArrayList();
            requestParams.remove("isOpeningBalanceReceipt");
            JSONObject advSearchQueryObj = getAdvanceSearchForCustomQuery(requestParams, paramsAdvSearch1, paramsAdvSearch, "");            
            String jeid = " jedetail.id = rad.totaljedid";
            if(advSearchQueryObj.has("jeid") && !StringUtil.isNullOrEmpty(advSearchQueryObj.getString("jeid"))){
                jeid= advSearchQueryObj.getString("jeid");
            }
            String joinString1 = advSearchQueryObj.getString("joinString1");
            String mySearchFilterString = custQuery +advSearchQueryObj.getString("mySearchFilterString");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(duedate);
            params.addAll(paramsAdvSearch);
            
            params.add(asofdate);
            params.add(companyid);
            params.add(duedate);
            
            params.add(asofdate);
            params.add(companyid);
            params.add(duedate);
            
            params.add(asofdate);
            params.add(companyid);
            params.add(duedate);
            
//            params.add(asofdate);
//            params.add(companyid);
//            params.addAll(paramsAdvSearch);

            params.add(asofdate);
            params.add(companyid);
            params.add(duedate);
            
            params.add(asofdate);
            params.add(companyid);
            params.add(duedate);
            
            String conditionSQL="";
            if (requestParams.containsKey("groupcombo") && requestParams.get("groupcombo") != null && requestParams.containsKey(Constants.globalCurrencyKey) && requestParams.get(Constants.globalCurrencyKey) != null) {
                int groupcombo = (Integer) requestParams.get("groupcombo");
                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    conditionSQL += " where rt.doccurrency=" + Integer.parseInt((String) requestParams.get(Constants.globalCurrencyKey));
                } else if (groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    conditionSQL += " where rt.doccurrency!=" + Integer.parseInt((String) requestParams.get(Constants.globalCurrencyKey));
                }
            }
            //global search
            String ss = (requestParams.containsKey("ss") && requestParams.get("ss") != null) ? (String) requestParams.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                try {
                    String[] searchcol = new String[]{"rt.customername","rt.custaliasname","rt.custcode", "rt.docnumber", "rt.accountname"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 5); 
                    StringUtil.insertParamSearchString(map);
                    String queryStart = "and";
                    if(StringUtil.isNullOrEmpty(conditionSQL)){
                        queryStart = "where";
                    }
                    String searchQuery = StringUtil.getSearchString(ss, queryStart, searchcol);
                    conditionSQL += searchQuery + " AND rt.custcode IS NOT NULL ";
                } catch (SQLException ex) {
                    Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            String sql = "select rt.docid, rt.docnumber, SUM(rt.amount), SUM(rt.amountinbase), SUM(rt.koamt), SUM(rt.koamtbase), rt.doctype, rt.docterm, rt.creationdate, rt.duedate, rt.salespersonname, rt.salespersoncode, rt.salespersonid, rt.entryno, rt.entrydate, rt.isOpeningBalanceTransaction, rt.customerid, rt.customername, rt.custaliasname, rt.custcode, rt.customertermname, rt.customertermid, rt.customercreditlimit, rt.memo, rt.exchangerate,rt.doccurrency, rt.doccurrencyname, rt.doccurrencycode, rt.doccurrencysymbol,rt.companyname,rt.shipdate, rt.basecurrencysymbol, rt.accountname from (\n"
                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, jedetail.amount, jedetail.amountinbase,0 as koamtbase,0 as koamt, 'Receipt' as doctype, ' ' as docterm, receipt.creationdate, receipt.creationdate as duedate, ' ' as salespersonname, ' ' as salespersoncode, ' ' as salespersonid, je.entryno, je.entrydate, '0' as isOpeningBalanceTransaction, cust.id as customerid, cust.name as customername, cust.aliasname as custaliasname, cust.acccode as custcode, custcredit.termname as customertermname, custcredit.termid as customertermid, cust.creditlimit as customercreditlimit, receipt.memo,if(je.externalcurrencyrate=0,exchangerate_calc(receipt.company,receipt.creationdate,receipt.currency,company.currency),je.externalcurrencyrate) as exchangerate,receipt.currency as doccurrency, rtcurr.name as doccurrencyname, rtcurr.currencycode as doccurrencycode, rtcurr.symbol as doccurrencysymbol,company.companyname, ' ' as shipdate, compcurr.symbol as basecurrencysymbol, account.name  as accountname\n"
                    + "from receipt \n"
                    + "INNER JOIN journalentry je ON receipt.journalentry=je.id \n"
                    + "INNER JOIN receiptadvancedetail rad on rad.receipt=receipt.id\n" 
                    + "inner join jedetail on "+jeid+"\n"
                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"
                    + "LEFT JOIN account ON account.id=receipt.account \n"
                    + "inner join creditterm custcredit on cust.creditterm = custcredit.termid\n"
                    + "INNER JOIN company on receipt.company = company.companyid \n"
                    + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                    + "INNER JOIN currency rtcurr on receipt.currency = rtcurr.currencyid \n"
                    +joinString1
                    + "where receipt.company = ? and receipt.creationdate <= ? and receipt.isopeningbalencereceipt=0 and receipt.deleteflag='F' and receipt.contraentry='F' and receipt.isdishonouredcheque='F' and receipt.paymentwindowtype = '1' and receipt.approvestatuslevel = '11'  \n"
                    + mySearchFilterString
                    + " \n"
                    + "group by receipt.receiptnumber \n"
                    + " UNION \n"
                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(ifnull(lp.amountinpaymentcurrency,0)/COALESCE(if(je.externalcurrencyrate =0, exchangerate_calc(receipt.company,receipt.creationdate,receipt.currency,company.currency), je.externalcurrencyrate ),1)) as koamtbase,SUM(ifnull(lp.amountinpaymentcurrency,0)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from receipt \n"
                    + "INNER JOIN journalentry je ON receipt.journalentry=je.id \n"
                    + "INNER JOIN receiptadvancedetail rad on rad.receipt=receipt.id\n" 
                    + "inner join jedetail on "+jeid+" \n"
                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"
                    + "INNER JOIN company on receipt.company = company.companyid \n"
                    + "INNER join linkdetailpaymenttoadvancepayment lp on lp.receipt=receipt.id and lp.paymentlinkdate <= ? and lp.company=receipt.company \n"
                    + "where receipt.company = ? and receipt.creationdate <= ? and receipt.isopeningbalencereceipt=0 and receipt.deleteflag='F' and receipt.contraentry='F' and receipt.isdishonouredcheque='F' and receipt.paymentwindowtype = '1' and receipt.approvestatuslevel = '11'  \n"
                    + " \n"
                    + "group by receipt.receiptnumber \n"
                    + " UNION \n"
                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(ifnull(ldr.amount,0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(receipt.company,receipt.creationdate,receipt.currency,company.currency),je.externalcurrencyrate ),1)) as koamtbase,SUM(ifnull(ldr.amount,0)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from receipt \n"
                    + "INNER JOIN journalentry je ON receipt.journalentry=je.id \n"
                    + "INNER JOIN receiptadvancedetail rad on rad.receipt=receipt.id\n" 
                    + "inner join jedetail on "+jeid+" \n"
                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"
                    + "INNER JOIN company on receipt.company = company.companyid \n"
                    + "INNER join linkdetailreceipt ldr on ldr.receipt=receipt.id and ldr.receiptlinkdate<=? and ldr.company=receipt.company \n"
                    + "where receipt.company = ? and receipt.creationdate <= ? and receipt.isopeningbalencereceipt=0 and receipt.deleteflag='F' and receipt.contraentry='F' and receipt.isdishonouredcheque='F' and receipt.paymentwindowtype = '1' and receipt.approvestatuslevel = '11'  \n"
                    + " \n"
                    + "group by receipt.receiptnumber \n"
//                    + " UNION \n"
//                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, 0 as amount, 0 as amountinbase,SUM(ifnull(rd.amount,0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(receipt.company,receipt.creationdate,receipt.currency,company.currency),je.externalcurrencyrate ),1)) as koamtbase,SUM(ifnull(rd.amount,0)) as koamt\n"
//                    + "from receipt \n"
//                    + "INNER JOIN journalentry je ON receipt.journalentry=je.id \n"
//                    + "inner join jedetail on "+jeid+" \n"
//                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"
//                    + "INNER JOIN receiptadvancedetail rad on rad.receipt=receipt.id\n" 
//                    + "inner join creditterm custcredit on cust.creditterm = custcredit.termid\n"
//                    + "INNER JOIN company on receipt.company = company.companyid \n"
//                    + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
//                    + "INNER JOIN currency rtcurr on receipt.currency = rtcurr.currencyid \n"
//                    + "INNER join receiptdetails rd on rd.receipt=receipt.id and rd.invoice is not null and rd.company=receipt.company and je.entrydate <= ? \n"
//                    +joinString1
//                    + "where receipt.company = ? and receipt.isopeningbalencereceipt=0 and receipt.deleteflag='F' and receipt.contraentry='F' and receipt.isdishonouredcheque='F' and receipt.paymentwindowtype = '1' and receipt.approvestatuslevel = '11'  \n"
//                    + mySearchFilterString
//                    + " \n"
//                    + "group by receipt.receiptnumber \n"
                    + " UNION \n"
                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(ifnull(rd.amount,0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(receipt.company,receipt.creationdate,receipt.currency,company.currency),je.externalcurrencyrate ),1)) as koamtbase,SUM(ifnull(rd.amount,0)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from receipt \n"
                    + "INNER JOIN journalentry je ON receipt.journalentry=je.id \n"
                    + "INNER JOIN receiptadvancedetail rad on rad.receipt=receipt.id\n" 
                    + "inner join jedetail on "+jeid+" \n"
                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"
                    + "INNER JOIN company on receipt.company = company.companyid \n"
                    + "INNER join linkdetailreceipttodebitnote rd on rd.receipt=receipt.id and rd.receiptlinkdate<=? and rd.company=receipt.company\n"
                    + "where receipt.company = ? and receipt.creationdate <= ? and receipt.isopeningbalencereceipt=0 and receipt.deleteflag='F' and receipt.contraentry='F' and receipt.isdishonouredcheque='F' and receipt.paymentwindowtype = '1' and receipt.approvestatuslevel = '11'  \n"
                    + " \n"
                    + "group by receipt.receiptnumber \n"
                    + " UNION \n"
                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(adv.amount/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(receipt.company,receipt.creationdate,receipt.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase,SUM(COALESCE(adv.amount,0)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from receipt \n"
                    + "INNER JOIN journalentry je ON receipt.journalentry=je.id \n"
                    + "INNER JOIN receiptadvancedetail rad on rad.receipt=receipt.id\n" 
                    + "inner join jedetail on "+jeid+" \n"
                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"                    
                    + "INNER JOIN company on receipt.company = company.companyid \n"
                    + "INNER JOIN advancedetail adv on receiptadvancedetail=rad.id\n" 
                    + "INNER JOIN payment on payment.id=adv.payment and payment.creationdate <= ?\n"
                    + "where receipt.company = ? and receipt.creationdate <= ? and receipt.isopeningbalencereceipt=0 and receipt.deleteflag='F' and receipt.contraentry='F' and receipt.isdishonouredcheque='F' and receipt.paymentwindowtype = '1' and receipt.approvestatuslevel = '11'  \n"
                    + " \n"
                    + "group by receipt.receiptnumber \n"
                    + " UNION \n"
                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(ifnull(rwo.writtenoffamountinbasecurrency,0)) as koamtbase,SUM(ifnull(rwo.writtenoffamountinreceiptcurrency,0)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from receipt \n"
                    + "INNER JOIN journalentry je ON receipt.journalentry=je.id \n"
                    + "INNER JOIN receiptadvancedetail rad on rad.receipt=receipt.id\n" 
                    + "inner join jedetail on "+jeid+" \n"
                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"
                    + "INNER JOIN company on receipt.company = company.companyid \n"
                    + "INNER join receiptwriteoff rwo on rwo.receipt=receipt.id and rwo.writeoffdate<=? and rwo.company=receipt.company and rwo.isrecovered='F'\n"
                    + "where receipt.company = ? and receipt.creationdate <= ? and receipt.isopeningbalencereceipt=0 and receipt.deleteflag='F' and receipt.contraentry='F' and receipt.isdishonouredcheque='F' and receipt.paymentwindowtype = '1' and receipt.approvestatuslevel = '11'  \n"
                    + " \n"
                    + "group by receipt.receiptnumber \n"
                    + ") rt " + conditionSQL +" group by rt.docnumber order by rt.creationdate desc";
            
            ll = executeSQLQuery(sql, params.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("error", ex);
        }
        return ll;
    }
    
    public List getOpeningSalesReceiptKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException{
        List ll = null;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String customerid = (String) requestParams.get("custVendorID");
            boolean isAgedDetailsReport = requestParams.containsKey("isAgedDetailsReport") ? (Boolean)requestParams.get("isAgedDetailsReport"): false;
            boolean isSalesPersonAgedReport = requestParams.containsKey("isSalesPersonAgedReport") ? (Boolean)requestParams.get("isSalesPersonAgedReport"): false;
            
            int datefilter = requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            String custQuery = "";
            boolean includeExcludeChildCmb=false;
            if (requestParams.containsKey("includeExcludeChildCmb") && requestParams.get("includeExcludeChildCmb") != null) {
                includeExcludeChildCmb = (Boolean) requestParams.get("includeExcludeChildCmb");
            }
            if (!StringUtil.isNullOrEmpty(customerid) && !customerid.equals("All")) {
                String[] customers = customerid.split(",");
                StringBuilder custValues = new StringBuilder();
                for (String customer : customers) {
                    custValues.append("'").append(customer).append("',");
                }
                String custStr = custValues.substring(0, custValues.lastIndexOf(","));
                if (isSalesPersonAgedReport) {
                    custQuery += " and masteritem.id IN (" + custStr + ")";
                } else if (includeExcludeChildCmb) {
                    custQuery += " and (cust.id IN (" + custStr + ") or cust.parent IN (" + custStr + "))";
                } else {
                    custQuery += " and cust.id IN (" + custStr + ")";
                }
            }else if(!includeExcludeChildCmb){
                custQuery += " and cust.parent is  null";
            }
            DateFormat origdf = authHandler.getDateOnlyFormat();
            String duedateStr = (String)requestParams.get("enddate");
            String asofdateStr= (requestParams.containsKey("asofdate") && requestParams.get("asofdate") != null) ? (String) requestParams.get("asofdate") : duedateStr;
            Date duedate = origdf.parse(duedateStr);
            Date asofdate = origdf.parse(asofdateStr);
            DateFormat mysqldf = new SimpleDateFormat("yyyy-MM-dd");
            duedateStr = mysqldf.format(duedate);
            ArrayList paramsAdvSearch = new ArrayList();
            ArrayList paramsAdvSearch1= new ArrayList();
            requestParams.put("isOpeningBalanceReceipt",true);
            JSONObject advSearchQueryObj = getAdvanceSearchForCustomQuery(requestParams, paramsAdvSearch1, paramsAdvSearch, "");            
            String joinString1 = advSearchQueryObj.getString("joinString1");
            String mySearchFilterString = custQuery +advSearchQueryObj.getString("mySearchFilterString");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(duedate);
            params.addAll(paramsAdvSearch);
            
            params.add(asofdate);
            params.add(companyid);
            params.add(duedate);
            
            params.add(asofdate);
            params.add(companyid);
            params.add(duedate);
            
            params.add(asofdate);
            params.add(companyid);
            params.add(duedate);
            
            params.add(asofdate);
            params.add(companyid);
            params.add(duedate);
            
            params.add(asofdate);
            params.add(companyid);
            params.add(duedate);
            
            String conditionSQL="";
            if (requestParams.containsKey("groupcombo") && requestParams.get("groupcombo") != null && requestParams.containsKey(Constants.globalCurrencyKey) && requestParams.get(Constants.globalCurrencyKey) != null) {
                int groupcombo = (Integer) requestParams.get("groupcombo");
                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    conditionSQL += " where rt.doccurrency=" + Integer.parseInt((String) requestParams.get(Constants.globalCurrencyKey));
                } else if (groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    conditionSQL += " where rt.doccurrency!=" + Integer.parseInt((String) requestParams.get(Constants.globalCurrencyKey));
                }
            }
            //global search
            String ss = (requestParams.containsKey("ss") && requestParams.get("ss") != null) ? (String) requestParams.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                try {
                    String[] searchcol = new String[]{"rt.customername","rt.custaliasname","rt.custcode", "rt.docnumber", "rt.accountname"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 5); 
                    StringUtil.insertParamSearchString(map);
                    String queryStart = "and";
                    if(StringUtil.isNullOrEmpty(conditionSQL)){
                        queryStart = "where";
                    }
                    String searchQuery = StringUtil.getSearchString(ss, queryStart, searchcol);
                    conditionSQL += searchQuery + " AND rt.custcode IS NOT NULL ";
                } catch (SQLException ex) {
                    Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            String sql = "select rt.docid, rt.docnumber, SUM(rt.amount), SUM(rt.amountinbase), SUM(rt.koamt), SUM(rt.koamtbase), rt.doctype, rt.docterm, rt.creationdate, rt.duedate, rt.salespersonname, rt.salespersoncode, rt.salespersonid, rt.entryno, rt.entrydate, rt.isOpeningBalanceTransaction, rt.customerid, rt.customername, rt.custaliasname, rt.custcode, rt.customertermname, rt.customertermid, rt.customercreditlimit, rt.memo, rt.exchangerate,rt.doccurrency, rt.doccurrencyname, rt.doccurrencycode, rt.doccurrencysymbol,rt.companyname,rt.shipdate, rt.basecurrencysymbol, rt.accountname from (\n"
                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, receipt.depositamount as amount, receipt.originalopeningbalancebaseamount as amountinbase,0 as koamtbase,0 as koamt, 'Receipt' as doctype, ' ' as docterm, receipt.creationdate, receipt.creationdate as duedate, ' ' as salespersonname, ' ' as salespersoncode, ' ' as salespersonid, ' ' as entryno, receipt.creationdate as entrydate, '1' as isOpeningBalanceTransaction, cust.id as customerid, cust.name as customername, cust.aliasname as custaliasname, cust.acccode as custcode, custcredit.termname as customertermname, custcredit.termid as customertermid, cust.creditlimit as customercreditlimit, receipt.memo,if(receipt.isconversionratefromcurrencytobase=1,1/receipt.exchangerateforopeningtransaction, 1/receipt.exchangerateforopeningtransaction) as exchangerate,receipt.currency as doccurrency, rtcurr.name as doccurrencyname, rtcurr.currencycode as doccurrencycode, rtcurr.symbol as doccurrencysymbol,company.companyname, ' ' as shipdate, compcurr.symbol as basecurrencysymbol, account.name as accountname\n"
                    + "from receipt \n"
                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"
                    + "LEFT JOIN account ON account.id=receipt.account \n"
                    + "inner join creditterm custcredit on cust.creditterm = custcredit.termid\n"
                    + "INNER JOIN company on receipt.company = company.companyid \n"
                    + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                    + "INNER JOIN currency rtcurr on receipt.currency = rtcurr.currencyid \n"
                    +joinString1
                    + "where receipt.company = ? and receipt.creationdate <= ? and receipt.isopeningbalencereceipt=1 and receipt.deleteflag='F' and receipt.isdishonouredcheque='F'\n"
                    + mySearchFilterString
                    + " \n"
                    + "group by receipt.receiptnumber \n"
                    + " UNION \n"
                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(ifnull(lp.amountinpaymentcurrency,0)*if(receipt.isconversionratefromcurrencytobase=1,receipt.exchangerateforopeningtransaction, 1/receipt.exchangerateforopeningtransaction)) as koamtbase,SUM(ifnull(lp.amountinpaymentcurrency,0)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from receipt \n"
                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"
                    + "INNER join linkdetailpaymenttoadvancepayment lp on lp.receipt=receipt.id and lp.paymentlinkdate <= ? and lp.company=receipt.company \n"
                    + "where receipt.company = ? and receipt.creationdate <= ? and receipt.isopeningbalencereceipt=1 and receipt.deleteflag='F' and receipt.contraentry='F' and receipt.isdishonouredcheque='F' and receipt.paymentwindowtype = '1' and receipt.approvestatuslevel = '11'  \n"
                    + " \n"
                    + "group by receipt.receiptnumber \n"
                    + " UNION \n"
                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(ldr.amount*if(receipt.isconversionratefromcurrencytobase=1,receipt.exchangerateforopeningtransaction, 1/receipt.exchangerateforopeningtransaction)),SUM(ifnull(ldr.amount,0)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from receipt \n"
                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"
                    + "INNER join linkdetailreceipt ldr on ldr.receipt=receipt.id and ldr.receiptlinkdate<=? and ldr.company=receipt.company \n"
                    + "where receipt.company = ? and receipt.creationdate <= ? and receipt.isopeningbalencereceipt=1 and receipt.deleteflag='F' and receipt.isdishonouredcheque='F'\n"
                    + " \n"
                    + "group by receipt.receiptnumber \n"
                    + " UNION \n"
                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(ifnull(rd.amount,0)*if(receipt.isconversionratefromcurrencytobase=1,receipt.exchangerateforopeningtransaction, 1/receipt.exchangerateforopeningtransaction)) as koamtbase,SUM(ifnull(rd.amount,0)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from receipt \n"
                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"
                    + "INNER join linkdetailreceipttodebitnote rd on rd.receipt=receipt.id and rd.receiptlinkdate<=? and rd.company=receipt.company\n"
                    + "where receipt.company = ? and receipt.creationdate <= ? and receipt.isopeningbalencereceipt=1 and receipt.deleteflag='F' and receipt.contraentry='F' and receipt.isdishonouredcheque='F' and receipt.paymentwindowtype = '1' and receipt.approvestatuslevel = '11'  \n"
                    + " \n"
                    + "group by receipt.receiptnumber \n"
                    + " UNION \n"
                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(adv.amount*if(receipt.isconversionratefromcurrencytobase=1,receipt.exchangerateforopeningtransaction, 1/receipt.exchangerateforopeningtransaction)) as koamtbase,SUM(COALESCE(adv.amount,0)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from receipt \n"
                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"
                    + "INNER JOIN receiptadvancedetail rad on rad.receipt=receipt.id\n" 
                    + "INNER JOIN advancedetail adv on receiptadvancedetail=rad.id\n" 
                    + "INNER JOIN payment on payment.id=adv.payment and payment.creationdate <= ?\n"
                    + "where receipt.company = ? and receipt.creationdate <= ? and receipt.isopeningbalencereceipt=1 and receipt.deleteflag='F' and receipt.isdishonouredcheque='F'\n"
                    + " \n"
                    + "group by receipt.receiptnumber \n"
                    + " UNION \n"
                    + "SELECT  receipt.id as docid, receipt.receiptnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(ifnull(rwo.writtenoffamountinbasecurrency,0)) as koamtbase,SUM(ifnull(rwo.writtenoffamountinreceiptcurrency,0)/if(receipt.isconversionratefromcurrencytobase=1,receipt.exchangerateforopeningtransaction, 1/receipt.exchangerateforopeningtransaction)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from receipt \n"
                    + "INNER JOIN customer cust ON cust.id=receipt.customer \n"
                    + "INNER join receiptwriteoff rwo on rwo.receipt=receipt.id and rwo.writeoffdate<=? and rwo.company=receipt.company and rwo.isrecovered='F'\n"
                    + "where receipt.company = ? and receipt.creationdate <= ? and receipt.isopeningbalencereceipt=1 and receipt.deleteflag='F' and receipt.isdishonouredcheque='F'\n"
                    + " \n"
                    + "group by receipt.receiptnumber \n"
                    + ") rt " + conditionSQL +" group by rt.docnumber order by rt.creationdate desc";
            
            ll = executeSQLQuery(sql, params.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("error", ex);
        }
        return ll;
    }
    
    private JSONObject getAdvanceSearchForCustomQuery(Map<String, Object> request, ArrayList params, ArrayList paramsSQLWithoutInv, String searchDefaultFieldSQL) throws JSONException, ServiceException {
        JSONObject returnObj = new JSONObject();
        boolean isOpeningBalanceReceipt = false;
        if (request.get("isOpeningBalanceReceipt") != null) {
            isOpeningBalanceReceipt = (Boolean) request.get("isOpeningBalanceReceipt");
        }
        String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
        if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
            if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                filterConjuctionCriteria = com.krawler.common.util.Constants.or;
            }
        }
        String mySearchFilterString = "";
        String joinString1 = "";
        String jeid = "";
        String Searchjson =  "";
        
       try{
        if (request.containsKey("searchJson") && request.get("searchJson") != null) {
            if(request.containsKey("searchJsonReceivePayment") && request.get("searchJsonReceivePayment") != null)
            {
                Searchjson = StringUtil.DecodeText(request.get("searchJsonReceivePayment").toString());
            } 
            else
            {
                Searchjson = StringUtil.DecodeText(request.get("searchJson").toString());
            }

            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                JSONObject serachJobj = new JSONObject(Searchjson);
                JSONArray customSearchFieldArray = new JSONArray();
                JSONArray defaultSearchFieldArray = new JSONArray();
                StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);

                if (customSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Custom fields
                         */
                        request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, "and");
                        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                            request.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                            request.put("isOpeningBalance", isOpeningBalanceReceipt);
                        if (isOpeningBalanceReceipt) {
                            mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "openingbalancereceiptcustomdata");//    
                            mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceReceiptCustomData", "openingbalancereceiptcustomdata");//    
//                        mySearchFilterStringforOpeningTransaction = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                            joinString1 = " inner join openingbalancereceiptcustomdata on openingbalancereceiptcustomdata.openingbalancereceiptid=receipt.id ";                            
                        } else {
                            mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
//                            mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                            if (mySearchFilterString.contains("accjecustomdata") || mySearchFilterString.contains("AccJECustomData")) {
                                joinString1 = " inner join accjecustomdata on accjecustomdata.journalentryId=receipt.journalentry ";
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "accjecustomdata");//    
                            }
                            StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                            if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//    
                                joinString1 += " left join accjedetailcustomdata  on accjedetailcustomdata.jedetailId=jedetail.id ";
                                jeid = " jedetail.journalentry = receipt.journalentry ";
                            }
                            if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "accjedetailproductcustomdata");//    
                                joinString1 += " left join accjedetailproductcustomdata  on accjedetailproductcustomdata.jedetailId=jedetail.id ";
                                jeid = " jedetail.journalentry = receipt.journalentry ";
                            }
                            if (mySearchFilterString.contains("CustomerCustomData")) {
                                joinString1 += " left join customercustomdata  on customercustomdata.customerId=customer.id ";
                                mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "customercustomdata");
                            }
                        }
                        StringUtil.insertParamAdvanceSearchString1(paramsSQLWithoutInv, Searchjson);
                    }
                mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
            }
        }
        returnObj.put("jeid", jeid);
        returnObj.put("joinString1", joinString1);
        returnObj.put("mySearchFilterString", mySearchFilterString);
         } catch (Exception ex) {
            Logger.getLogger(accReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accReceiptImpl.getAdvanceSearchForCustomQuery:" + ex.getMessage(), ex);
        }
        return returnObj;
    }

}
