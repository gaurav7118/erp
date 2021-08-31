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
package com.krawler.spring.accounting.vendorpayment;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAOImpl;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;

/**
 *
 * @author krawler
 */
public class accVendorPaymentImpl extends BaseDAO implements accVendorPaymentDAO ,MessageSourceAware {

    private accBankReconciliationDAO accBankReconciliationObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accPaymentDAO accPaymentDAOobj;
    private accVendorDAO accVendorDAOobj;
    private AccCommonTablesDAO accCommonTablesDAOobj;
    private MessageSource messageSource;

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    
    public void setAccVendorDAOobj(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }
    public void setAccCommonTablesDAOobj(AccCommonTablesDAO accCommonTablesDAOobj) {
        this.accCommonTablesDAOobj = accCommonTablesDAOobj;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    public KwlReturnObject getaccountdetailsPayment(String accid) throws ServiceException {
        List ll = new ArrayList();
        try {
            // insert new entry
            String accountid = accid;
            String query = "from Vendor WHERE ID= ? ";
            ll = executeQuery(query, new Object[]{accountid});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.createTemplate", e);
        }

        return new KwlReturnObject(true, "Account Details received successfully", null, ll, ll.size());

    }

    @Override
    public KwlReturnObject savePayment(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            Payment payment = getPaymentObj(hm);
            boolean mainPaymentForCNDNFlag=payment.isMainPaymentForCNDNFlag();
            saveOrUpdate(payment);
            if(mainPaymentForCNDNFlag&&hm.containsKey("mainPaymentForCNDNId")){
                   Payment mainPayment = hm.get("mainPaymentForCNDNId")==null?null:(Payment) get(Payment.class, (String) hm.get("mainPaymentForCNDNId"));
                   if(mainPayment!=null){
                        mainPayment.setIsCnDnAndInvoicePayment(true);
                        mainPayment.setCndnAndInvoiceId(payment.getID());
                        saveOrUpdate(payment);
                   }
                }
            if (hm.containsKey("isEdit") && (Boolean) hm.get("isEdit") && hm.containsKey("invoiceadvcndntype") && (Integer) hm.get("invoiceadvcndntype") == 1) {
                List<Payment> paymentList = new ArrayList<Payment>();
                if (hm.containsKey("paymentHashMap")) {
                    HashMap<Integer, String> paymentHashMap = (HashMap<Integer, String>) hm.get("paymentHashMap");
                    if (!paymentHashMap.containsKey(1) && paymentHashMap.containsKey(3) && paymentHashMap.containsKey(2)) {
                        Payment advancePayment = paymentHashMap.containsKey(2) ? (Payment) get(Payment.class, (String) paymentHashMap.get(2)) : null;
                        Payment cnDnPayment = paymentHashMap.containsKey(3) ? (Payment) get(Payment.class, (String) paymentHashMap.get(3)) : null;
                        if (advancePayment != null) {
                            payment.setAdvanceid(advancePayment);
                            payment.setCndnAndInvoiceId(null);
                            payment.setAdvanceamounttype((Integer)hm.get("advanceamounttype"));
                            payment.setAdvanceamount(advancePayment.getDepositAmount()-payment.getDepositAmount());
                            cnDnPayment.setAdvanceamount(advancePayment.getDepositAmount()-payment.getDepositAmount());
                            paymentList.add(advancePayment);
                        }
                        if (advancePayment != null && cnDnPayment != null) {
                            cnDnPayment.setAdvanceid(payment.getAdvanceid());
                        }
                        if (cnDnPayment != null) {
                            payment.setIsCnDnAndInvoicePayment(true);
                            payment.setCndnAndInvoiceId(cnDnPayment.getID());
                            cnDnPayment.setIsCnDnAndInvoicePayment(true);
                            cnDnPayment.setCndnAndInvoiceId(payment.getID());
                            cnDnPayment.setReceipttype(payment.getReceipttype());
                            paymentList.add(cnDnPayment);
                        }
                        paymentList.add(payment);
                    }
                }
                       saveAll(paymentList);
            }
            list.add(payment);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.savePayment : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Payment has been updated successfully", null, list, list.size());
    }
    
    
    @Override
    public Payment getPaymentObj(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        Payment payment = null;
        try {
            String paymentid = (String) hm.get("paymentid");
            boolean isEdit = false;
            boolean mainPaymentForCNDNFlag=false;
            String companyid = "";
            if (hm.containsKey("companyid")) {
                companyid = (String) hm.get("companyid");
            }
            if (StringUtil.isNullOrEmpty(paymentid)) {
                payment = new Payment();
                payment.setDeleted(false);
                if (hm.containsKey("createdby")) {
                    User createdby = hm.get("createdby") == null ? null : (User) get(User.class, (String) hm.get("createdby"));
                    payment.setCreatedby(createdby);
                }
                if (hm.containsKey("modifiedby")) {
                    User modifiedby = hm.get("modifiedby") == null ? null : (User) get(User.class, (String) hm.get("modifiedby"));
                    payment.setModifiedby(modifiedby);
                }
                if (hm.containsKey("createdon")) {
                    payment.setCreatedon((Long) hm.get("createdon"));
                }
                if (hm.containsKey("updatedon")) {
                    payment.setUpdatedon((Long) hm.get("updatedon"));
                }
            } else {
                payment = (Payment) get(Payment.class, paymentid);
                isEdit = true;
                if (hm.containsKey("modifiedby")) {
                    User modifiedby = hm.get("modifiedby") == null ? null : (User) get(User.class, (String) hm.get("modifiedby"));
                    payment.setModifiedby(modifiedby);
                }
                if (hm.containsKey("updatedon")) {
                    payment.setUpdatedon((Long) hm.get("updatedon"));
                }
                if (hm.containsKey(Constants.MARKED_PRINTED)) {
                    payment.setPrinted(Boolean.parseBoolean((String) hm.get(Constants.MARKED_PRINTED)));
                }
            }
            payment.setMainPaymentForCNDNFlag(false);
            if (hm.containsKey(Constants.SEQFORMAT)) {
                payment.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) hm.get(Constants.SEQFORMAT)));
            }
            if (hm.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullOrEmpty(hm.get(Constants.SEQNUMBER).toString())) {
                payment.setSeqnumber(Integer.parseInt(hm.get(Constants.SEQNUMBER).toString()));
            }
            if (hm.containsKey(Constants.DATEPREFIX) && hm.get(Constants.DATEPREFIX) != null) {
                payment.setDatePreffixValue((String) hm.get(Constants.DATEPREFIX));
            }
            if (hm.containsKey(Constants.DATEAFTERPREFIX) && hm.get(Constants.DATEAFTERPREFIX) != null) {
                payment.setDateAfterPreffixValue((String) hm.get(Constants.DATEAFTERPREFIX));
            }
            if (hm.containsKey(Constants.DATESUFFIX) && hm.get(Constants.DATESUFFIX) != null) {
                payment.setDateSuffixValue((String) hm.get(Constants.DATESUFFIX));
            }
            if (hm.containsKey("entrynumber")) {
                payment.setPaymentNumber((String) hm.get("entrynumber"));
            }
            if (hm.containsKey("salesReturn")) {
               SalesReturn salesReturn = hm.get("salesReturn") == null ? null : (SalesReturn) get(SalesReturn.class, (String) hm.get("salesReturn"));
                payment.setSalesReturn(salesReturn);
            }
            
            if (hm.containsKey("nonRefundable")) {
                payment.setNonRefundable((Boolean) hm.get("nonRefundable"));
            }
            if (hm.containsKey("autogenerated")) {
                payment.setAutoGenerated((Boolean) hm.get("autogenerated"));
            }
            if (hm.containsKey("memo")) {
                payment.setMemo((String) hm.get("memo"));
            }
            if (hm.containsKey("paydetailsid")) {
                PayDetail pd = hm.get("paydetailsid") == null ? null : (PayDetail) get(PayDetail.class, (String) hm.get("paydetailsid"));
                payment.setPayDetail(pd);
            }
            if (hm.containsKey("companyid")) {
                Company company = hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid"));
                payment.setCompany(company);
            }
            if (hm.containsKey("currencyid")) {
                KWLCurrency currency = hm.get("currencyid") == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) hm.get("currencyid"));
                payment.setCurrency(currency);
            }
            if (hm.containsKey("journalentryid")) {
                JournalEntry je = hm.get("journalentryid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) hm.get("journalentryid"));
                payment.setJournalEntry(je);
            }
            if (hm.containsKey("deposittojedetailid")) {
                JournalEntryDetail jed = hm.get("deposittojedetailid") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("deposittojedetailid"));
                payment.setDeposittoJEDetail(jed);
            }
            if (hm.containsKey("depositamount") && !StringUtil.isNullOrEmpty(hm.get("depositamount").toString())) {
                payment.setDepositAmount(Double.parseDouble(hm.get("depositamount").toString()));
            }
            if (hm.containsKey("pdetails")) {
                payment.setRows((Set<PaymentDetail>) hm.get("pdetails"));
            }
            if (hm.containsKey("linkDetails")) {
                payment.setLinkDetailPayments((Set<LinkDetailPayment>) hm.get("linkDetails"));
            }
            if (hm.containsKey("externalCurrencyRate")) {
                payment.setExternalCurrencyRate((Double) hm.get("externalCurrencyRate"));
            }
            if (hm.containsKey("contraentry")) {
                payment.setContraentry((Boolean) hm.get("contraentry"));
            }
            if (hm.containsKey("isLinkedToClaimedInvoice") && hm.get("isLinkedToClaimedInvoice") != null) {
                payment.setLinkedToClaimedInvoice((Boolean) hm.get("isLinkedToClaimedInvoice"));
            }
            if (hm.containsKey("isadvancepayment")) {
                payment.setIsadvancepayment((Boolean) hm.get("isadvancepayment"));
            }
            if (hm.containsKey("receipttype")) {
                payment.setReceipttype((Integer) hm.get("receipttype"));
            }
            if (hm.containsKey("actualReceiptType") && isEdit) {
                payment.setReceipttype((Integer) hm.get("actualReceiptType"));
            }
            if (hm.containsKey("openingBalanceMakePaymentCustomData")) {
                    OpeningBalanceMakePaymentCustomData openingBalanceMakePaymentCustomData = hm.get("openingBalanceMakePaymentCustomData") == null ? null : (OpeningBalanceMakePaymentCustomData) get(OpeningBalanceMakePaymentCustomData.class, (String) hm.get("openingBalanceMakePaymentCustomData"));
                    payment.setOpeningBalanceMakePaymentCustomData(openingBalanceMakePaymentCustomData);
            }
            if (hm.containsKey("customer")) {
                payment.setCustomer((String) hm.get("customer"));
            }
            if (hm.containsKey("advanceid")) {
                Payment advPayment = hm.get("advanceid") == null ? null : (Payment) get(Payment.class, (String) hm.get("advanceid"));
                payment.setAdvanceid(advPayment);
            }
            if (hm.containsKey("advanceamount")) {
                payment.setAdvanceamount((Double) hm.get("advanceamount"));
            }
            if (hm.containsKey("advancePaymentIdForCnDn")) {
                    Payment advPayment = hm.get("advancePaymentIdForCnDn")==null?null:(Payment) get(Payment.class, (String) hm.get("advancePaymentIdForCnDn"));
                    payment.setAdvanceid(advPayment);
                    payment.setAdvanceamount(advPayment.getDepositAmount());               
            }
            if(hm.containsKey("mainPaymentForCNDNId")&&hm.containsKey("isadvancepayment")&&!(Boolean) hm.get("isadvancepayment")){
                    payment.setIsCnDnAndInvoicePayment(true);
                    payment.setCndnAndInvoiceId((String)hm.get("mainPaymentForCNDNId"));
                    mainPaymentForCNDNFlag=true;
                    payment.setMainPaymentForCNDNFlag(true);
            }
            if(hm.containsKey("invoiceadvcndntype")){
                    payment.setInvoiceAdvCndnType((Integer)hm.get("invoiceadvcndntype"));
            }
            if (hm.containsKey("ismanydbcr")) {
                payment.setIsmanydbcr((Boolean) hm.get("ismanydbcr"));
            }
            if (hm.containsKey("bankCharges") && !StringUtil.isNullOrEmpty(hm.get("bankCharges").toString())) {
                payment.setBankChargesAmount(Double.parseDouble(hm.get("bankCharges").toString()));
            }
            if (hm.containsKey("bankChargesCmb")) {
                Account bankChargesAccount = hm.get("bankChargesCmb") == null ? null : (Account) get(Account.class, (String) hm.get("bankChargesCmb"));
                payment.setBankChargesAccount(bankChargesAccount);
            }
            if (hm.containsKey("bankInterest") && !StringUtil.isNullOrEmpty(hm.get("bankInterest").toString())) {
                payment.setBankInterestAmount(Double.parseDouble(hm.get("bankInterest").toString()));
            }
            if (hm.containsKey("bankInterestCmb")) {
                Account bankInterestAccount = hm.get("bankInterestCmb") == null ? null : (Account) get(Account.class, (String) hm.get("bankInterestCmb"));
                payment.setBankInterestAccount(bankInterestAccount);
            }
            if (hm.containsKey("paidToCmb")) {
                MasterItem paidToCmb = hm.get("paidToCmb") == null ? null : (MasterItem) get(MasterItem.class, (String) hm.get("paidToCmb"));
                payment.setPaidTo(paidToCmb);
            }
            if (hm.containsKey("accountId")) {
                Account account = hm.get("accountId") == null ? null : (Account) get(Account.class, (String) hm.get("accountId"));
                payment.setAccount(account);
            }
            if (hm.containsKey("chequeNumber")) {//
                payment.setChequeNumber((String) hm.get("chequeNumber"));
            }
            if (hm.containsKey("drawnOn")) {//
                payment.setDrawnOn((String) hm.get("drawnOn"));
            }
            if (hm.containsKey("creationDate")) {//
                payment.setCreationDate((Date) hm.get("creationDate"));
            }
            if (hm.containsKey("chequeDate")) {//
                payment.setChequeDate((Date) hm.get("chequeDate"));
            }
            if (hm.containsKey("vendorId")) {//
                String vendorId = (String) hm.get("vendorId");
                Vendor vendor = (Vendor) get(Vendor.class, vendorId);
                payment.setVendor(vendor);
            }
            
            if (hm.containsKey("isOpeningBalencePayment") && hm.get("isOpeningBalencePayment") != null) {//
                boolean isOpeningPayment = (Boolean) hm.get("isOpeningBalencePayment");
                payment.setIsOpeningBalencePayment(isOpeningPayment);
                if(isOpeningPayment) {
                    payment.setApprovestatuslevel(11);
                }
            }
            if (hm.containsKey("openingBalanceAmountDue") && !StringUtil.isNullOrEmpty(hm.get("openingBalanceAmountDue").toString())) {
                payment.setOpeningBalanceAmountDue(Double.parseDouble(hm.get("openingBalanceAmountDue").toString()));
            }
            if (hm.containsKey(Constants.openingBalanceBaseAmountDue) && !StringUtil.isNullOrEmpty(hm.get(Constants.openingBalanceBaseAmountDue).toString())) {
                payment.setOpeningBalanceBaseAmountDue(authHandler.round(Double.parseDouble(hm.get(Constants.openingBalanceBaseAmountDue).toString()), companyid));
            }
            if (hm.containsKey(Constants.originalOpeningBalanceBaseAmount) && !StringUtil.isNullOrEmpty(hm.get(Constants.originalOpeningBalanceBaseAmount).toString())) {
                payment.setOriginalOpeningBalanceBaseAmount(authHandler.round(Double.parseDouble(hm.get(Constants.originalOpeningBalanceBaseAmount).toString()), companyid));
            }
            if (hm.containsKey("normalPayment")) {//
                payment.setNormalPayment((Boolean) hm.get("normalPayment"));
            } else {
                if (!isEdit) {
                    payment.setNormalPayment(true);
                }
            }
            if (hm.containsKey("exchangeRateForOpeningTransaction")) {
                double exchangeRateForOpeningTransaction = (Double) hm.get("exchangeRateForOpeningTransaction");
                payment.setExchangeRateForOpeningTransaction(exchangeRateForOpeningTransaction);
            }
            if (hm.containsKey("conversionRateFromCurrencyToBase")) {
                payment.setConversionRateFromCurrencyToBase((Boolean) hm.get("conversionRateFromCurrencyToBase"));
            }
            if (hm.containsKey("revalJeId")) {
                payment.setRevalJeId((String) hm.get("revalJeId"));
            }
            if (hm.containsKey("isIBGTypeTransaction")) {
                payment.setIBGTypeTransaction((Boolean) hm.get("isIBGTypeTransaction"));
            }
            if (hm.containsKey("ibgDetailsID")) {
                if(hm.containsKey("bankType")){
                    int bankType = (int) hm.get("bankType");
                    if(bankType==Constants.DBS_BANK_Type){
                        payment.setIbgreceivingbankdetails((IBGReceivingBankDetails) get(IBGReceivingBankDetails.class, (String) hm.get("ibgDetailsID")));
                    }else if(bankType == Constants.CIMB_BANK_Type){
                        payment.setCimbreceivingbankdetails((CIMBReceivingDetails) get(CIMBReceivingDetails.class, (String) hm.get("ibgDetailsID")));
                    }else if (bankType == Constants.OCBC_BankType && hm.containsKey("ibgDetailsID")) {
                        payment.setOCBCReceivingDetails((OCBCReceivingDetails) get(OCBCReceivingDetails.class, (String) hm.get("ibgDetailsID")));
                    }
                }
            }
            if (hm.containsKey("ibgCode")) {
                payment.setIbgCode((String) hm.get("ibgCode"));
            }
            if(hm.containsKey("PaymentCurrencyToPaymentMethodCurrencyRate")){
                payment.setPaymentcurrencytopaymentmethodcurrencyrate((Double)hm.get("PaymentCurrencyToPaymentMethodCurrencyRate"));
            }
            if (hm.containsKey("payee")) {
                payment.setPayee((String) hm.get("payee"));
            }
            if (hm.containsKey("exciseunit")) {
                payment.setExciseunit((String) hm.get("exciseunit"));
            }
            if (hm.containsKey("linkWithCreditNoteDetails")) {
                payment.setLinkDetailPaymentToCreditNote((Set<LinkDetailPaymentToCreditNote>) hm.get("linkWithCreditNoteDetails"));
            }
            if (hm.containsKey("linkWithAdvancePaymentDetails")) {
                payment.setLinkDetailPaymentsToAdvancePayment((Set<LinkDetailPaymentToAdvancePayment>) hm.get("linkWithAdvancePaymentDetails"));
            }
            if (hm.containsKey("cinno")) {
                payment.setCinNo((String) hm.get("cinno"));
            }
            if (hm.containsKey("rcmApplicable") && hm.get("rcmApplicable") != null) {
                payment.setRcmApplicable((Boolean) hm.get("rcmApplicable"));
            }
            if (hm.containsKey("advanceToVendor") && hm.get("advanceToVendor") != null) {
                payment.setAdvanceToVendor((Boolean) hm.get("advanceToVendor"));
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.savePayment : " + ex.getMessage(), ex);
        }
        return payment;
    }

    @Override
    public KwlReturnObject updatePaymentsIBGFlag(String companyId, String paymentId) throws ServiceException {
        List list = new ArrayList();
        String query = "UPDATE payment set isgirofilegenerated=true where id=? and company=?";
        int rowsupdated = executeSQLUpdate(query, new Object[]{paymentId, companyId});
        list.add(rowsupdated);
        return new KwlReturnObject(true, "Payment has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject savePaymentObject(List<Payment> paymentList) throws ServiceException {
        List list = new ArrayList();
        try {
            saveAll(paymentList);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.savePayment : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Payment has been updated successfully", null, paymentList, paymentList.size());
    }
    
    @Override
    public PayDetail saveOrUpdatePayDetail(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        PayDetail pdetail;
        try {
            if (hm.containsKey("paydetailid")) {
                String paydetailid = (String) hm.get("paydetailid");
                pdetail = (PayDetail) get(PayDetail.class, paydetailid);
            } else {
                pdetail = new PayDetail();
            }
            if (hm.containsKey("paymethodid")) {
                PaymentMethod paymentMethod = (hm.get("paymethodid") == null ? null : (PaymentMethod) get(PaymentMethod.class, (String) hm.get("paymethodid")));
                pdetail.setPaymentMethod(paymentMethod);
            }
            if (hm.containsKey("companyid")) {
                Company cmp = (hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid")));
                pdetail.setCompany(cmp);
            }
            if (hm.containsKey("chequeid")) {
                Cheque chq = (hm.get("chequeid") == null ? null : (Cheque) get(Cheque.class, (String) hm.get("chequeid")));
                pdetail.setCheque(chq);
            }
            if (hm.containsKey("cardid")) {
                Card card = (hm.get("cardid") == null ? null : (Card) get(Card.class, (String) hm.get("cardid")));
                pdetail.setCard(card);
            }
            saveOrUpdate(pdetail);
            list.add(pdetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.addPayDetail : " + ex.getMessage(), ex);
        }
        return pdetail;
    }
    
    @Override
        public KwlReturnObject getPaymentIdFromSRId(String srid, String companyid) throws ServiceException {
        String selQuery = "from Payment cr where cr.salesReturn.ID = ? and cr.company.companyID=? ";
        List list = executeQuery( selQuery, new Object[]{srid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject saveBillingPayment(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            BillingPayment bPayment = null;
            String billingPaymentid = (String) hm.get("billingPaymentid");
            if (StringUtil.isNullOrEmpty(billingPaymentid)) {
                bPayment = new BillingPayment();
            } else {
                bPayment = (BillingPayment) get(BillingPayment.class, billingPaymentid);
            }
            if (hm.containsKey(Constants.SEQFORMAT)) {
                bPayment.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) hm.get(Constants.SEQFORMAT)));
            }
            if (hm.containsKey(Constants.SEQNUMBER)) {
                bPayment.setSeqnumber(Integer.parseInt(hm.get(Constants.SEQNUMBER).toString()));
            }
            if (hm.containsKey("entrynumber")) {
                bPayment.setBillingPaymentNumber((String) hm.get("entrynumber"));
            }
            if (hm.containsKey("autogenerated")) {
                bPayment.setAutoGenerated((Boolean) hm.get("autogenerated"));
            }
            if (hm.containsKey("memo")) {
                bPayment.setMemo((String) hm.get("memo"));
            }
            if (hm.containsKey("paydetailsid")) {
                PayDetail pd = hm.get("paydetailsid") == null ? null : (PayDetail) get(PayDetail.class, (String) hm.get("paydetailsid"));
                bPayment.setPayDetail(pd);
            }
            if (hm.containsKey("companyid")) {
                Company company = hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid"));
                bPayment.setCompany(company);
            }
            if (hm.containsKey("currencyid")) {
                KWLCurrency currency = hm.get("currencyid") == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) hm.get("currencyid"));
                bPayment.setCurrency(currency);
            }
            if (hm.containsKey("journalentryid")) {
                JournalEntry je = hm.get("journalentryid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) hm.get("journalentryid"));
                bPayment.setJournalEntry(je);
            }
            if (hm.containsKey("deposittojedetailid")) {
                JournalEntryDetail jed = hm.get("deposittojedetailid") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("deposittojedetailid"));
                bPayment.setDeposittoJEDetail(jed);
            }
            if (hm.containsKey("depositamount") && !StringUtil.isNullOrEmpty(hm.get("depositamount").toString())) {
                bPayment.setDepositAmount(Double.parseDouble(hm.get("depositamount").toString()));
            }
            if (hm.containsKey("bpdetails")) {
                bPayment.setRows((Set<BillingPaymentDetail>) hm.get("bpdetails"));
            }
            if (hm.containsKey("deleted")) {
                bPayment.setDeleted((Boolean) hm.get("deleted"));
            }
            if (hm.containsKey("externalCurrencyRate")) {
                bPayment.setExternalCurrencyRate((Double) hm.get("externalCurrencyRate"));
            }
            if (hm.containsKey("contraentry")) {
                bPayment.setContraentry((Boolean) hm.get("contraentry"));
            }
            if (hm.containsKey("ismanydbcr")) {
                bPayment.setIsmanydbcr((Boolean) hm.get("ismanydbcr"));
            }
            if (hm.containsKey("bankCharges") && !StringUtil.isNullOrEmpty(hm.get("bankCharges").toString())) {
                bPayment.setBankChargesAmount(Double.parseDouble(hm.get("bankCharges").toString()));
            }
            if (hm.containsKey("bankChargesCmb")) {
                Account bankChargesAccount = hm.get("bankChargesCmb") == null ? null : (Account) get(Account.class, (String) hm.get("bankChargesCmb"));
                bPayment.setBankChargesAccount(bankChargesAccount);
            }
            if (hm.containsKey("bankInterest") && !StringUtil.isNullOrEmpty(hm.get("bankInterest").toString())) {
                bPayment.setBankInterestAmount(Double.parseDouble(hm.get("bankInterest").toString()));
            }
            if (hm.containsKey("bankInterestCmb")) {
                Account bankInterestAccount = hm.get("bankInterestCmb") == null ? null : (Account) get(Account.class, (String) hm.get("bankInterestCmb"));
                bPayment.setBankInterestAccount(bankInterestAccount);
            }
            if (hm.containsKey("paidToCmb")) {
                MasterItem paidToCmb = hm.get("paidToCmb") == null ? null : (MasterItem) get(MasterItem.class, (String) hm.get("paidToCmb"));
                bPayment.setPaidTo(paidToCmb);
            }
            if (hm.containsKey("receipttype")) {
                bPayment.setReceipttype((Integer) hm.get("receipttype"));
            }
            if (hm.containsKey("customer")) {
                bPayment.setCustomer((String) hm.get("customer"));
            }
            if (hm.containsKey("revalJeId")) {
                bPayment.setRevalJeId((String) hm.get("revalJeId"));
            }
            saveOrUpdate(bPayment);
//            }
            list.add(bPayment);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.saveBillingPayment : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Payment has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject getPaymentVendorNames(String companyid, String paymentid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(paymentid);
            String condition = " where p.company.companyID=? and jed.debit=true and p.ID=? ";
            String query = "select  ac.name from Payment p inner join p.journalEntry je inner join je.details jed inner join jed.account ac" + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getPaymentVendorNames : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    public int isFromTaxPayment(String companyid, String paymentNumber) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        Integer maxcount = 0;
        try {
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(paymentNumber);
            //Check whether it is an Excise Payment,VAT Payment or CST Payment, by checking count in InvoiceDetailTermMap table.
            String query = "select idtm.id from invoicedetailtermsmap idtm INNER JOIN payment p ON idtm.taxmakepayment=p.id "
                    + "where p.company= ? and p.paymentnumber= ? ";
            list = executeSQLQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.isFromTaxPayment : " + ex.getMessage(), ex);
        }
        return count;
    }

  @Override  
    public KwlReturnObject getBillingPaymentVendorNames(String companyid, String paymentid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(paymentid);
            String condition = " where p.company.companyID=? and jed.debit=true and p.ID=? ";
            String query = "select  ac.name from BillingPayment p inner join p.journalEntry je inner join je.details jed inner join jed.account ac" + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getBillingPaymentVendorNames : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getPayments(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        List list1 = new ArrayList();
        Long count = 0l;
        try {
//            KWLCurrency currency = (KWLCurrency) get(KWLCurrency.class, (String) request.get("currencyid"));
            DateFormat df = (DateFormat) request.get(Constants.df);
            String moduleid="";
            if(request.containsKey(Constants.moduleid) && request.get(Constants.moduleid)!=null){
                moduleid =request.get(Constants.moduleid).toString();
            }
            
            String start = (String) request.get("start");
            String limit = (String) request.get("limit");
            String ss = (String) request.get("ss");
           /*
             fetch vendor id and includeExcludeChildCmb value
             */
            String Vendorid=(String) request.get("custVendorID");
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
            boolean ispendingAproval = false;
            /*
             * isExportPayment is true if export record from entry form 
             */ 
            boolean exportRecord = false;
            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            boolean contraentryflag = request.get("contraentryflag") != null ? (Boolean) request.get("contraentryflag") : false;
            String paymentaccid = request.get("paymentaccid") != null ? (String) request.get("paymentaccid") : "";
            String ibgCode = request.get("ibgCode") != null ? (String) request.get("ibgCode") : "";
            int ibgBank = request.get("ibgBank") != null ? Integer.parseInt(request.get("ibgBank").toString()) : 0;
            boolean isAgedReceivables=false;//when request will come from aged receivable report either summary or details this flag will be true.
            boolean isToFetchRecordLessEndDate = (request.containsKey("isToFetchRecordLessEndDate") && request.get("isToFetchRecordLessEndDate") != null) ? Boolean.parseBoolean(request.get("isToFetchRecordLessEndDate").toString()) : false;
            boolean getRecordBasedOnJEDate = false;
            boolean isAgedPayables=false;
            if(request.containsKey("isAgedReceivables") && request.get("isAgedReceivables")!=null && Boolean.parseBoolean(request.get("isAgedReceivables").toString())){
                isAgedReceivables = true;
            }
            /*
             * when request will come from aged AgedPayables report either summary or details this flag will be true.
             */        
            if(request.containsKey("isAgedPayables") && request.get("isAgedPayables")!=null && Boolean.parseBoolean(request.get("isAgedPayables").toString())){
                isAgedPayables = true;
            }
            if(request.containsKey("exportRecord") && request.get("exportRecord")!=null && Boolean.parseBoolean(request.get("exportRecord").toString())){
                exportRecord = true;
            }
            if (request.containsKey("getRecordBasedOnJEDate") && request.get("getRecordBasedOnJEDate") != null && Boolean.parseBoolean(request.get("getRecordBasedOnJEDate").toString())) {
                getRecordBasedOnJEDate = true;
            }
            boolean isAdvancePayment = false;
            if (request.get("isadvancepayment") != null) {
                isAdvancePayment = (Boolean) request.get("isadvancepayment");
            }
            boolean isAdvanceToCustomer = false;
            if (request.get("isadvancetocustomer") != null) {
                isAdvanceToCustomer = (Boolean) request.get("isadvancetocustomer");
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
            if (request.containsKey("ispendingAproval") && request.get("ispendingAproval") != null) {
                ispendingAproval = Boolean.FALSE.parseBoolean(String.valueOf(request.get("ispendingAproval")));
            }
            
            boolean allAdvPayment=false;
            boolean onlyAdvAmountDue=false;
            boolean unUtilizedAdvPayment =false;
            boolean partiallyUtilizedAdvPayment = false;
            boolean fullyUtilizedAdvPayment = false;
            boolean nonorpartiallyUtilizedAdvPayment = false;
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
             boolean isMonthlyAgeingReport = false;
            if (request.get("isMonthlyAgeingReport") != null) {
                isMonthlyAgeingReport = Boolean.parseBoolean(request.get("isMonthlyAgeingReport").toString());
            }
            
            boolean isprinted = false;
            if (request.get(Constants.MARKED_PRINTED) != null) {
                isprinted = (Boolean) request.get(Constants.MARKED_PRINTED);
            }
            
            String userDepartment = "";
            if (request.containsKey("userDepartment") && request.get("userDepartment") != null) {
                userDepartment = (String) request.get("userDepartment");
            }
            
            String vendorIdGroup = (String) request.get("custVendorID");
            if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
                vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
            }
            
            String newcustomerid = "";
            if (request.containsKey(Constants.newcustomerid) && request.get(Constants.newcustomerid) != null) {
                newcustomerid = (String) request.get(Constants.newcustomerid);
            } else if(request.containsKey("customerid") &&  request.get("customerid")!=null){
                newcustomerid = (String) request.get("customerid");
            }
          
            String newvendorid = "";
            if (request.containsKey(Constants.newvendorid) && request.get(Constants.newvendorid) != null) {
                newvendorid = (String) request.get(Constants.newvendorid);
            } else if (request.containsKey("vendorid") && request.get("vendorid") != null) {
                newvendorid = (String) request.get("vendorid");
            }
           String userID = "";
            boolean isenableSalesPersonAgentFlow = false;
            if (request.containsKey("enablesalespersonagentflow") && request.get("enablesalespersonagentflow") != null && !StringUtil.isNullOrEmpty(request.get("enablesalespersonagentflow").toString())) {
                isenableSalesPersonAgentFlow = Boolean.parseBoolean(request.get("enablesalespersonagentflow").toString());
                if (isenableSalesPersonAgentFlow) {
                    if (request.containsKey("userid") && request.get("userid") != null && !StringUtil.isNullOrEmpty(request.get("userid").toString())) {
                        userID = (String) request.get("userid");
                    }
                }
            }
            
            ArrayList params = new ArrayList();
            params.add((String) request.get("companyid"));
            String condition = " where p.company.companyID=? and jed.debit=true ";
            String innerJoin = "";
            String linkPaymentJoin = "";
            
            if (!StringUtil.isNullOrEmpty(newcustomerid)) {
                if (newcustomerid.contains(",")) {
                    newcustomerid = AccountingManager.getFilterInString(newcustomerid);
                    condition += " and p.customer IN" + newcustomerid;
                } else {
                    params.add(newcustomerid);
                    condition += " and p.customer = ? ";
                }
            }
            
            
             if (!StringUtil.isNullOrEmpty(newvendorid)) {
                if (newvendorid.contains(",")) {
                    newvendorid = AccountingManager.getFilterInString(newvendorid);
                    condition += " and p.vendor.ID IN" + newvendorid;
                } else {
                    params.add(newvendorid);
                    condition += " and p.vendor.ID = ? ";

                }
            }
           
             /*
              perform operation for vendor and include exclude child combobox selection
              if includeExcludechildCmb value is true it gives vendors and its child account
              otherwise only parent vendors account
              when quick search is empty then it will be used
             */
             if(isPaymentReport){
               if (!StringUtil.isNullOrEmpty(Vendorid) && !Vendorid.equals("All") && StringUtil.isNullOrEmpty(ss)) {
                String[] customers = Vendorid.split(",");
                StringBuilder custValues = new StringBuilder();
                for (String customer : customers) {
                    custValues.append("'").append(customer).append("',");
                }
                String custStr = custValues.substring(0, custValues.lastIndexOf(","));
                if (includeExcludeChildCmb) {
                   condition += " and (p.vendor.ID IN (" + custStr + ") or p.vendor.parent IN (" + custStr + "))";
                } else {
                    condition += " and p.vendor.ID IN (" + custStr + ")";
                }
              }
              else if(!includeExcludeChildCmb && StringUtil.isNullOrEmpty(ss)){
                condition += " and p.vendor.parent is  null";
              }
           }
   
            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
                condition += " and p.currency.currencyID = ?";
                params.add(currencyfilterfortrans);
            }
            
            if (request.containsKey("groupcombo") && request.get("groupcombo") != null && request.containsKey(Constants.globalCurrencyKey) && request.get(Constants.globalCurrencyKey) != null) {
                int groupcombo = (Integer) request.get("groupcombo");

                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    condition += " and p.currency.currencyID=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                } else if (groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    condition += " and p.currency.currencyID!=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                }
            }
            
            if(allAdvPayment || unUtilizedAdvPayment || partiallyUtilizedAdvPayment || fullyUtilizedAdvPayment || onlyAdvAmountDue || nonorpartiallyUtilizedAdvPayment){
                innerJoin += "  inner join p.advanceDetails ad ";
            }
            if(unUtilizedAdvPayment){
               condition += " and ad.amount=ad.amountDue ";
            }
             if(partiallyUtilizedAdvPayment){
               condition += " and ad.amount!=ad.amountDue and ad.amountDue!=0";
            }
              if(fullyUtilizedAdvPayment){
               condition += " and ad.amountDue=0";
            }
            if (onlyAdvAmountDue) {
                condition += " and ad.amountDue > 0 ";
            }
            if (nonorpartiallyUtilizedAdvPayment) {
                condition += " and ((ad.amount!=ad.amountDue and ad.amountDue!=0) or (ad.amount=ad.amountDue)) ";
            }
              
            if (!StringUtil.isNullOrEmpty(ss)) {
                linkPaymentJoin = " left join p.linkDetailPayments lp ";
                String[] searchcol = new String[]{"ac.name", "p.paymentNumber", "p.journalEntry.entryNumber","p.payDetail.paymentMethod.methodName", "p.memo", "p.paidTo.value", "chk.chequeNo"};  //added quick serch on the basis of paid to
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 7);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                searchQuery = searchQuery.substring(0, searchQuery.lastIndexOf(")"));   //removing last ')' to add to following customer and vendor search             
                 /*
                when if condition true displaying parent and child vendor payment transaction
                otherwise displaying only parent vendor payment transaction
                */
                if (isPaymentReport) {
                    if (includeExcludeChildCmb && request.containsKey("includeExcludeChildCmb")) {
                        
                        searchQuery += " or (p.customer in (select ID from Customer where name like ? or aliasname like ?)) or (p.vendor in (select ID from Vendor where name like ?or  aliasname like ? or parent in (select p.vendor from p where p.paymentNumber like ?) or parent in (select ID from Vendor where name like ?) or parent in (select p.vendor from p where p.memo like ?)or parent in (select p.vendor from p where p.journalEntry.entryNumber like ?)or parent in (select p.vendor from p where p.payDetail.paymentMethod.methodName like ?)or parent in (select p.vendor from p where p.paidTo.value like ?)))";
                    } else {
                        
                        searchQuery += " or (p.customer in (select ID from Customer where name like ? or aliasname like ?)) or (p.vendor in (select ID from Vendor where name like ?or  aliasname like ? ))";
                    }
                } else {

                    searchQuery += " or (p.customer in (select ID from Customer where name like ? or aliasname like ?)) or (p.vendor in (select ID from Vendor where name like ?or  aliasname like ? ))";
                }
            
                searchQuery += " or (lp.linkedGainLossJE in (select ID from JournalEntry jn where jn.entryNumber like ? AND jn.company.companyID = ?))";
                searchQuery += " or (p.journalEntryForBankCharges.ID in (select ID from JournalEntry jn where jn.entryNumber like ? AND jn.company.companyID = ?))";
                searchQuery += " or (p.journalEntryForBankInterest.ID in (select ID from JournalEntry jn where jn.entryNumber like ? AND jn.company.companyID = ?)))";               
                /*
                if both if are true add specific value into param for query
                otherwise add value to param for regular query
                */
                if (isPaymentReport) {
                    if (includeExcludeChildCmb) {
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
                    } else {
                        params.add("%" + ss + "%");
                        params.add("%" + ss + "%");
                        params.add("%" + ss + "%");
                        params.add("%" + ss + "%");
                    }
                } else {
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

//                params.add(ss + "%");
//                params.add(ss + "%");
//                params.add(ss + "%");
//                params.add(ss + "%");
//                condition += " and (ac.name like ? or p.paymentNumber like ? or p.journalEntry.entryNumber like ? or p.memo like ? ) ";
            }
            if (nondeleted) {
                condition += " and p.deleted=false ";
            } else if (deleted) {
                condition += " and p.deleted=true ";
            }

            if (contraentryflag) {
                condition += " and p.contraentry=true ";
            } else {
                condition += " and p.contraentry=false ";
            }

            if (isprinted) {
                condition += " and p.printedflag=true ";
            }

            if (isAdvancePayment) {
                condition += " and p.isadvancepayment=true ";
            }

//            if (isGlcode) {
//                condition += " and p.receipttype=9 ";
//            }

            if (isAdvanceToCustomer) {
                condition += " and p.receipttype='6' ";
            }

            if (isPostDatedCheque) {
                condition += " and p.payDetail.cheque.dueDate > now() ";
            }
             if (isAgedPayables||isAgedReceivables) {
                condition += " and p.isDishonouredCheque='F' ";
            }
            if(isDishonouredCheque&&!isAgedPayables&&!isAgedReceivables){
                 condition += " and p.isDishonouredCheque='T' ";
            }
            String disHonouredJeIds = (String) request.get("disHonouredJeIds");
            if (!StringUtil.isNullOrEmpty(disHonouredJeIds)) {
                condition += " and p.disHonouredChequeJe.ID IN(" + disHonouredJeIds + ")";
            }
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and ((p.journalEntry.ID IN (" + jeIds + ")) or (p.journalEntryForBankCharges.ID IN (" + jeIds + ")) or (p.journalEntryForBankInterest.ID IN (" + jeIds + ")))";
                innerJoin += " left join p.journalEntryForBankCharges bandchargeje left join p.journalEntryForBankInterest bandinterestje ";
            }
            String billid = (String) request.get("billid");
            if (!StringUtil.isNullOrEmpty(billid)) {
                condition += " and p.ID=? ";
                params.add(billid);
            }
             if (request.containsKey("linknumber") && request.get("linknumber") != null && !request.get("linknumber").toString().equals("")) {
                condition += " and p.paymentNumber = ? ";
                params.add(request.get("linknumber"));
            }

            if (request.get("receipttype") != null) {
                String receipttypestr = (String) request.get("receipttype");
                String receipttypeArr[] = receipttypestr.split(",");
                condition += " and ( ";
                for (int i = 0; i < receipttypeArr.length; i++) {
                    if (i >= 1) {
                        condition += " or p.receipttype=? ";
                    } else {
                        condition += " p.receipttype=? ";
                    }
                    params.add(Integer.parseInt(receipttypeArr[i]));
                }
                condition += " )  ";
            }

            String startDate = request.get(Constants.REQ_startdate)!=null? StringUtil.DecodeText((String) request.get(Constants.REQ_startdate)):(String) request.get(Constants.REQ_startdate);
            String endDate =request.get(Constants.REQ_enddate)!=null? StringUtil.DecodeText((String) request.get(Constants.REQ_enddate)):(String) request.get(Constants.REQ_enddate);
            
            if((isAgedPayables||isAgedReceivables||isToFetchRecordLessEndDate) && !StringUtil.isNullOrEmpty(endDate) && !isMonthlyAgeingReport){ //Fetching all transactions whose creation date is upto end date for aged Report
//                condition += " and p.journalEntry.entryDate <=? ";
//                int datefilter = (request.containsKey("datefilter") && request.get("datefilter") != null) ? Integer.parseInt(request.get("datefilter").toString()) : 1;// 0 = Due Date OR 1 = Invoice Date
//                if (datefilter == Constants.dueDateFilter) {
//                    if (getRecordBasedOnJEDate) {
//                        condition += " and (p.journalEntry.entryDate >=? and p.journalEntry.entryDate <=?) ";
//                    } else {
//                        condition += " and (p.creationDate <=? and p.creationDate >=?)";
//                    }
//                    params.add(df.parse(endDate));
//                    params.add(df.parse(startDate));
//
//                } else {
                    if (getRecordBasedOnJEDate) {
                        condition += " and p.journalEntry.entryDate <=? ";
                    } else {
                        condition += " and p.creationDate <=? ";
                    }
                params.add(df.parse(endDate));
//                }
            } else if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                if (getRecordBasedOnJEDate) {
                    condition += " and (p.journalEntry.entryDate >=? and p.journalEntry.entryDate <=?) ";
                } else {
                    condition += " and (p.creationDate >=? and p.creationDate <=?) ";
                }
                 if (isMonthlyAgeingReport) {
                    params.add((Date)request.get("MonthlyAgeingStartDate"));
                    params.add((Date)request.get("MonthlyAgeingEndDate"));
                } else {
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
            }
            if(request.get("paymentWindowType")!=null){
                condition +=" and p.paymentWindowType = ? ";
                params.add(request.get("paymentWindowType"));
            }
           
           /*
            don't execute when request come from payment report
            */
            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All") && !isPaymentReport) {
                if(isAgedReceivables){//In aged receivable report we need payment agaisnt customer 
                    condition += " and p.customer in " + vendorIdGroup;
                } else {
                    condition += " and p.vendor.ID in " + vendorIdGroup;
                }
            }

            if (!StringUtil.isNullOrEmpty(paymentaccid)) {
                params.add(paymentaccid);
                condition += " and p.payDetail.paymentMethod.account.ID = ? ";
            }

            if (!StringUtil.isNullOrEmpty(ibgCode) && ibgBank==1) {   // Fetching the data for DBS Bank
                params.add(ibgCode);
                condition += " and p.ibgCode = ? ";
            }
            
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                condition += " and p.createdby.department = ? ";
                params.add(userDepartment);
            }

            String appendCase = "and";
            String mySearchFilterString = "";
            boolean applyInvoiceSearch=false;
            String joinString = "";
            HashMap<String, Object> reqParams1 = new HashMap<String, Object>();
            reqParams1.putAll(request);
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            
            /*
             * Do decide which payments to be load, with repeated payment generated or with repeated payment = null
             */
            if (request.containsKey(Constants.isRepeatedPaymentFlag) && request.get(Constants.isRepeatedPaymentFlag) != null) {
                if (Boolean.parseBoolean((String) request.get(Constants.isRepeatedPaymentFlag))) {
                    if(ispendingAproval){   //Pending Recurring Approval Records
                        condition += " and ( p.repeatedPayment  is not null and p.repeatedPayment.ispendingapproval=true )";
                    } else {
                        condition += " and ( p.repeatedPayment  is not null and p.repeatedPayment.ispendingapproval=false )";
                    }
                } else {
                    condition += " and p.repeatedPayment is null";
                }
            } else {
                if (!exportRecord) {
                    if (ispendingAproval) { // Get only pending approved records                    
                        condition += " and p.approvestatuslevel != ? and p.normalPayment=?";
                        params.add(11);
                        params.add(true);
                    } else {// Get only approved records                      
                        condition += " and p.approvestatuslevel = ?  ";
                        params.add(11);
                    }
                }
            }
            String Searchjson = "";
            String searchDefaultFieldSQL="";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson =StringUtil.DecodeText(request.get("searchJson").toString());

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
                        searchDefaultFieldSQL=searchDefaultFieldSQL.replaceAll("paymentRef", "p");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("paymentlinkingRef", "pl");
                        if (searchDefaultFieldSQL.contains("gr")) {
                            applyInvoiceSearch = true;
                        }
                    }
                    if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field
                        reqParams1.put(Constants.Searchjson, Searchjson);
                        reqParams1.put(Constants.appendCase, appendCase);
                        reqParams1.put("isPaymentFromInvoice", true);
                        reqParams1.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(reqParams1, true).get(Constants.myResult));
                        mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "p.journalEntry.accBillInvCustomData");
                        if (mySearchFilterString.contains("accinvoicecustomdata")) {
                            applyInvoiceSearch = true;
                            mySearchFilterString = mySearchFilterString.replaceAll("accinvoicecustomdata", "grje.accBillInvCustomData");
                        }
                    joinString = " inner join je.accBillInvCustomData accjecustomdata ";
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//    
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");
                    if (mySearchFilterString.contains("VendorCustomData")) {
                        mySearchFilterString = mySearchFilterString.replaceAll("VendorCustomData", "p.vendor.accVendorCustomData");
                        }
                        if (mySearchFilterString.contains("CustomerCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "p.vendor.accVendorCustomData");
                        }
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
            }
            }
//            String orderBy = " order by je.entryDate desc ";//mahamuni Sir
            String agentQuery = "";
           if (isenableSalesPersonAgentFlow && !StringUtil.isNullOrEmpty(userID)) {
                innerJoin += " left join p.vendor vd left join vd.agent vam left join vam.agent mst ";
                   condition+= " and ((mst.user.userID='" + userID + "' or mst.user.userID is null and vd.isVendorAvailableOnlyToSelectedAgents='T') or (vd.isVendorAvailableOnlyToSelectedAgents='F') or p.vendor is null) ";
            }
            String orderByCondition="";
            
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                orderByCondition = sortColumnPayment(Col_Name, Col_Dir, false);
            } else {
                if (getRecordBasedOnJEDate) {
                    orderByCondition = " order by p.journalEntry.entryDate desc";
                } else {
                    orderByCondition = " order by p.creationDate desc";
                }
            }
            String mainquery = "select distinct p, ac from PaymentLinking pl "
                    + "right join pl.DocID p "
                    + "inner join p.journalEntry je "
                    + linkPaymentJoin
                    + "inner join je.details jed "
                    + "inner join jed.account ac "
                    + "left join p.paidTo pt "
                    + "left join p.payDetail pd "
                    + "left join pd.cheque chk "
                    + "left join p.repeatedPayment rpt ";
            String query = mainquery + innerJoin + condition + mySearchFilterString + " group by p" + orderByCondition;
            String cntsubquery = "select count(distinct p.ID) from PaymentLinking pl right join pl.DocID p inner join p.journalEntry je "+linkPaymentJoin+" inner join je.details jed inner join jed.account ac left join p.paidTo pt left join p.payDetail pd left join pd.cheque chk  ";
            String cntquery = cntsubquery + innerJoin + condition + mySearchFilterString;

            /*
             Code for Invoice advance search
             */
            if (applyInvoiceSearch) {
                String paymentDetailJoin = "left join p.rows pde "
                        + " left join pde.goodsReceipt gr "
                        + "left join gr.journalEntry grje ";
                String linkDetailsJoin = " left join p.linkDetailPayments lp "
                        + " left join lp.goodsReceipt gr "
                        + "left join gr.journalEntry grje ";
                String payquery = mainquery + innerJoin + paymentDetailJoin + condition + mySearchFilterString + " group by p" + orderByCondition;
                String linkQuery = mainquery + innerJoin + linkDetailsJoin + condition + mySearchFilterString + " group by p" + orderByCondition;
                String paycnt = cntsubquery + innerJoin + paymentDetailJoin + condition + mySearchFilterString;
                String linkcnt = cntsubquery + innerJoin + linkDetailsJoin + condition + mySearchFilterString;
                List cntlist = executeQuery(paycnt, params.toArray());
                if (cntlist != null && !cntlist.isEmpty()) {
                    count = (Long) cntlist.get(0);
                }
                cntlist = executeQuery(linkcnt, params.toArray());
                if (cntlist != null && !cntlist.isEmpty()) {
                    count = count + (Long) cntlist.get(0);
                }
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
                    Payment payment = (Payment) row[0];
                    if (map.containsKey(payment.getID())) {
                        continue;
                    } else {
                        map.put(payment.getID(), payment.getID());
                        finalList.add(objectArr);
                    }
                }
                list.clear();
                list.addAll(finalList);
            } else {
                List cntlist = executeQuery(cntquery, params.toArray());
                if (cntlist != null && !cntlist.isEmpty()) {
                    count = (Long) cntlist.get(0);
                }
                if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                    list = executeQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                } else {
                    list = executeQuery(query, params.toArray());
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getPayments : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count.intValue());
    }

     public String sortColumnPayment(String Col_Name, String Col_Dir, boolean isOpeningPayment) throws ServiceException {
        String String_Sort = "";
         if (Col_Name.equals("billno")) {
             if (isOpeningPayment) {
                 String_Sort = " order by r.paymentNumber " + Col_Dir;
             } else {
                 String_Sort = " order by p.paymentNumber " + Col_Dir;
             }
        } else if (Col_Name.equals("personname")) {
            if(isOpeningPayment){
                String_Sort = " order by r.vendor.name " + Col_Dir;
            } else {
                String_Sort = " order by ac.name " + Col_Dir;
            }    
        } else if (Col_Name.equals("billdate")) {
            if(isOpeningPayment){
                String_Sort = " order by r.creationDate " + Col_Dir;
            } else {
//                String_Sort = " order by p.journalEntry.entryDate " + Col_Dir;
                String_Sort = " order by p.creationDate " + Col_Dir;
            }    
        } else if (Col_Name.equals("entryno")) {
            if(isOpeningPayment){
                String_Sort = "";
            } else {
                String_Sort = " order by p.journalEntry.entryNumber " + Col_Dir;
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
                String_Sort = " order by p.memo " + Col_Dir;
            }
        } else if (Col_Name.equals("startDate")) {
                String_Sort = " order by rpt.startDate " + Col_Dir;
        } else if (Col_Name.equals("expireDate")) {
                String_Sort = " order by rpt.expireDate " + Col_Dir;
        } else if (Col_Name.equals("nextDate")) {
                String_Sort = " order by rpt.nextDate " + Col_Dir;
        } else if (Col_Name.equals("NoOfPaymentpost")) {
                String_Sort = " order by rpt.NoOfpaymentspost " + Col_Dir;
        } else {
            if(isOpeningPayment){
                String_Sort = " order by r.creationDate " + Col_Dir;
            } else {
//                String_Sort = " order by p.journalEntry.entryDate " + Col_Dir;
                String_Sort = " order by p.creationDate " + Col_Dir;
            }
        }
        return String_Sort;
    }
     
    public KwlReturnObject getAllCompanyPayments(String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            String query = "from Payment where company.companyID = ?";
            list = executeQuery( query, new Object[]{companyid});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getAllCompanyPayments : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);

    }

    @Override
    public KwlReturnObject saveGIROFileGenerationLog(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();

        GIROFileInfo fileInfo = new GIROFileInfo();

        if (dataMap.containsKey("companyid") && dataMap.get("companyid") != null) {
            String companid = dataMap.get("companyid").toString();
            fileInfo.setCompanyid(companid);;
        }


        if (dataMap.containsKey("accCompanyId") && dataMap.get("accCompanyId") != null) {
            String accCompanyId = dataMap.get("accCompanyId").toString();
            Company company = (Company) get(Company.class, accCompanyId);
            fileInfo.setCompany(company);
        }

        if (dataMap.containsKey("filename") && dataMap.get("filename") != null) {
            String filename = dataMap.get("filename").toString();
            fileInfo.setFileName(filename);
        }


        if (dataMap.containsKey("bank") && dataMap.get("bank") != null) {
            String bank = dataMap.get("bank").toString();
            fileInfo.setBank(bank);
        }

        if (dataMap.containsKey("status") && dataMap.get("status") != null) {
            String status = dataMap.get("status").toString();
            fileInfo.setStatus(status);
        }

        if (dataMap.containsKey("comments") && dataMap.get("comments") != null) {
            String comments = dataMap.get("comments").toString();
            fileInfo.setComments(comments);
        }

        if (dataMap.containsKey("sequencenumber") && dataMap.get("sequencenumber") != null) {
            Integer sequenceNumber = Integer.parseInt(dataMap.get("sequencenumber").toString());
            fileInfo.setSequenceNumber(sequenceNumber);
        }

        fileInfo.setTimeStamp(new Date());


        saveOrUpdate(fileInfo);

        list.add(fileInfo);

        return new KwlReturnObject(true, "GIROFileGenerationLog has been saved successfully", null, list, list.size());
    }

    @Override
    public Integer getSequenceNumberForGiro(String accCompanyId) {
        Integer maxcount = 0;
        DateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        try {
            String hql = "select max(sequenceNumber) from GIROFileInfo where timeStamp=? and company.companyID=?";
            List list = executeQuery( hql, new Object[]{new Date(), accCompanyId});

            if (!list.isEmpty()) {
                maxcount = list.get(0) == null ? 1 : (Integer) list.get(0) + 1;
            }
            list.clear();
            list.add(maxcount);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return maxcount;
    }

    @Override
    public KwlReturnObject getOpeningBalancePayments(HashMap<String, Object> request) throws ServiceException {
        List<Payment> list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalReceipts = false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String start = (String) request.get("start");
        String limit = (String) request.get("limit");
        String vendorId = (String) request.get("vendorid");
        boolean onlyAmountDue = (request.containsKey("onlyAmountDue") && request.get("onlyAmountDue") != null) ? Boolean.parseBoolean(request.get("onlyAmountDue").toString()) : false;
        boolean isAged = false;
        if (request.containsKey("isAged") && request.get("isAged") != null) {
            isAged = Boolean.parseBoolean((String) request.get("isAged"));
        }
        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }

        boolean isAccountPayments = false;
        if (request.containsKey("isAccountPayments") && request.get("isAccountPayments") != null) {
            isAccountPayments = (Boolean) request.get("isAccountPayments");
        }
        String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");

        String condition = "";
        ArrayList params = new ArrayList();

        params.add(companyid);

        if (isAccountPayments && request.containsKey("accountId") && request.get("accountId") != null) {
            String accountId = request.get("accountId").toString();
            condition += " AND r.account.ID=? ";
            params.add(accountId);
        }
        if (!StringUtil.isNullOrEmpty(currencyfilterfortrans) && !request.containsKey("isReceipt")) {
            condition += " AND r.currency.currencyID = ?";
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
        
        if (!StringUtil.isNullOrEmpty(vendorId)) {
            condition += " AND r.vendor.ID=? ";
            params.add(vendorId);
        }

        if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
            condition += " AND r.vendor.ID IN " + vendorIdGroup;
        }

        if (request.get("excludeNormal") != null) {
            excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
        }

        if (excludeNormal) {
            condition += " AND r.normalPayment=false ";
        }

        if (request.get("onlyOpeningNormalReceipts") != null) {
            onlyOpeningNormalReceipts = Boolean.parseBoolean(request.get("onlyOpeningNormalReceipts").toString());
        }

        if (onlyAmountDue) {
            condition += " AND r.openingBalanceAmountDue > 0 ";
        }
        if (onlyOpeningNormalReceipts) {
            condition += " AND r.normalPayment=true ";
        }

        String startDate = (String) request.get(Constants.REQ_startdate);
        String endDate = (String) request.get(Constants.REQ_enddate);
        if (isAged) {
            if (request.containsKey("MonthlyAgeingEndDate") && request.get("MonthlyAgeingEndDate") != null) {
                condition += " and r.creationDate <=? ";
                params.add(request.get("MonthlyAgeingEndDate"));
            } else if (!StringUtil.isNullOrEmpty(endDate)) {
                try {
                    condition += " and r.creationDate <=? ";
                    params.add(df.parse(endDate));
                } catch (ParseException ex) {
                    Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"r.vendor.name","r.vendor.aliasname","r.vendor.acccode", "r.paymentNumber", "r.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 5);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND r.vendor IS NOT NULL ";
            } catch (SQLException ex) {
                Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        /**
         * Advance Search for Opening document
         */
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
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "r.openingBalanceMakePaymentCustomData");
                        mySearchFilterString = mySearchFilterString.replaceAll("VendorCustomData", "r.vendor.accVendorCustomData");
                        mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceMakePaymentCustomData", "r.openingBalanceMakePaymentCustomData");
                        mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "r.openingBalanceMakePaymentCustomData");
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                        request.put("isOpeningBalance",false);
                    } catch (JSONException ex) {
                        Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ParseException ex) {
                        Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        String query = "Select r from Payment r where r.isOpeningBalencePayment=true AND r.deleted=false AND r.company.companyID=?" + condition+mySearchFilterString;
        list = executeQuery( query, params.toArray());
        count = list.size();
        if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
            list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    
    @Override
    public int getOpeningBalancePaymentCount(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalReceipts = false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        String vendorId = (String) request.get("vendorid");

        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }

        boolean isAccountPayments = false;
        if (request.containsKey("isAccountPayments") && request.get("isAccountPayments") != null) {
            isAccountPayments = (Boolean) request.get("isAccountPayments");
        }

        String condition = "";
        ArrayList params = new ArrayList();

        params.add(companyid);

        if (isAccountPayments && request.containsKey("accountId") && request.get("accountId") != null) {
            String accountId = request.get("accountId").toString();
            condition += " AND r.account.ID=? ";
            params.add(accountId);
        }
        if (!StringUtil.isNullOrEmpty(vendorId)) {
            condition += " AND r.vendor.ID=? ";
            params.add(vendorId);
        }

        if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
            condition += " AND r.vendor.ID IN " + vendorIdGroup;
        }

        if (request.get("excludeNormal") != null) {
            excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
        }

        if (excludeNormal) {
            condition += " AND r.normalPayment=false ";
        }

        if (request.get("onlyOpeningNormalReceipts") != null) {
            onlyOpeningNormalReceipts = Boolean.parseBoolean(request.get("onlyOpeningNormalReceipts").toString());
        }

        if (onlyOpeningNormalReceipts) {
            condition += " AND r.normalPayment=true ";
        }

        String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"r.vendor.name","r.vendor.acccode", "r.paymentNumber", "r.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND r.vendor IS NOT NULL ";
            } catch (SQLException ex) {
                Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        String query = "Select count(r.ID) from Payment r where r.isOpeningBalencePayment=true AND r.deleted=false AND r.company.companyID=?" + condition;
        list = executeQuery( query, params.toArray());
        Long totalCnt = 0l;
        if (list != null && !list.isEmpty()){
            totalCnt = (Long) list.get(0);
        }
        count = totalCnt.intValue();
        return count;
    }
    
    
    @Override
    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForPayments(HashMap<String, Object> request) throws ServiceException {
        List<Payment> list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalReceipts = false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String start = (String) request.get("start");
        String limit = (String) request.get("limit");
        String vendorId = (String) request.get("vendorid");

        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }

        boolean isAccountPayments = false;
        if (request.containsKey("isAccountPayments") && request.get("isAccountPayments") != null) {
            isAccountPayments = (Boolean) request.get("isAccountPayments");
        }

        String condition = "";
        ArrayList params = new ArrayList();

        params.add(companyid);

        if (isAccountPayments && request.containsKey("accountId") && request.get("accountId") != null) {
            String accountId = request.get("accountId").toString();
            condition += " AND r.account.ID=? ";
            params.add(accountId);
        }
        if (!StringUtil.isNullOrEmpty(vendorId)) {
            condition += " AND r.vendor.ID=? ";
            params.add(vendorId);
        }

        if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
            condition += " AND r.vendor.ID IN " + vendorIdGroup;
        }

        if (request.get("excludeNormal") != null) {
            excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
        }

        if (excludeNormal) {
            condition += " AND r.normalPayment=false ";
        }

        if (request.get("onlyOpeningNormalReceipts") != null) {
            onlyOpeningNormalReceipts = Boolean.parseBoolean(request.get("onlyOpeningNormalReceipts").toString());
        }

        if (onlyOpeningNormalReceipts) {
            condition += " AND r.normalPayment=true ";
        }

        String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"r.vendor.name","r.vendor.acccode", "r.paymentNumber", "r.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND r.vendor IS NOT NULL ";
            } catch (SQLException ex) {
                Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        String query = "Select COALESCE(SUM(r.openingBalanceBaseAmountDue),0) from Payment r where r.isOpeningBalencePayment=true AND r.deleted=false AND r.company.companyID=?" + condition;
        list = executeQuery( query, params.toArray());
        count = list.size();
        return new KwlReturnObject(true, null, null, list, count);
    }
    
    @Override
    public KwlReturnObject getOpeningBalanceTotalBaseAmountForPayments(HashMap<String, Object> request) throws ServiceException {
        List<Payment> list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalReceipts = false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String start = (String) request.get("start");
        String limit = (String) request.get("limit");
        String vendorId = (String) request.get("vendorid");
        try{
        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }

        boolean isAccountPayments = false;
        if (request.containsKey("isAccountPayments") && request.get("isAccountPayments") != null) {
            isAccountPayments = (Boolean) request.get("isAccountPayments");
        }

        String condition = "";
        ArrayList params = new ArrayList();

        params.add(companyid);

        if (isAccountPayments && request.containsKey("accountId") && request.get("accountId") != null) {
            String accountId = request.get("accountId").toString();
            condition += " AND r.account.ID=? ";
            params.add(accountId);
        }
        if (!StringUtil.isNullOrEmpty(vendorId)) {
            condition += " AND r.vendor.ID=? ";
            params.add(vendorId);
        }

        if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
            condition += " AND r.vendor.ID IN " + vendorIdGroup;
        }

        if (request.get("excludeNormal") != null) {
            excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
        }

        if (excludeNormal) {
            condition += " AND r.normalPayment=false ";
        }

        if (request.get("onlyOpeningNormalReceipts") != null) {
            onlyOpeningNormalReceipts = Boolean.parseBoolean(request.get("onlyOpeningNormalReceipts").toString());
        }

        if (onlyOpeningNormalReceipts) {
            condition += " AND r.normalPayment=true ";
        }

        String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"r.vendor.name","r.vendor.acccode", "r.paymentNumber", "r.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND r.vendor IS NOT NULL ";
            } catch (SQLException ex) {
                Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
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
                request.put(Constants.moduleid, 14);
                request.put("isOpeningBalance", true);
                request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "r.openingBalanceMakePaymentCustomData");
                mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceMakePaymentCustomData", "r.openingBalanceMakePaymentCustomData");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "r.openingBalanceMakePaymentCustomData");
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
            }
        }
            String query = "";
            if (request.containsKey("Searchjson") && request.get("Searchjson") != null && !StringUtil.isNullOrEmpty((String) request.get("Searchjson"))) {
                if (!StringUtil.isNullOrEmpty(mySearchFilterString) && !mySearchFilterString.equals(" ")) {
                    query = "Select COALESCE(SUM(r.originalOpeningBalanceBaseAmount),0) from Payment r where r.isOpeningBalencePayment=true AND r.deleted=false AND r.company.companyID=?" + condition + mySearchFilterString;
                    list = executeQuery( query, params.toArray());
                    count = list.size();
                }
            } else {
                query = "Select COALESCE(SUM(r.originalOpeningBalanceBaseAmount),0) from Payment r where r.isOpeningBalencePayment=true AND r.deleted=false AND r.company.companyID=?" + condition + mySearchFilterString;
                list = executeQuery( query, params.toArray());
                count = list.size();
            }
        }catch(Exception ex){
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
                requestParams.put("moduleid", 14);
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
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
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
    public KwlReturnObject getAllOpeningBalancePayments(HashMap<String, Object> request) throws ServiceException {
        List<Payment> list = null;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String start = (String) request.get("start");
        String limit = (String) request.get("limit");
        String condition = "";
        String ss = "";
//        String customerid = (String) request.get("customerid");
        try {
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
            boolean ispendingAproval = false;
            if (request.containsKey("ispendingAproval") && request.get("ispendingAproval") != null) {
                ispendingAproval = Boolean.parseBoolean(request.get("ispendingAproval").toString());
            }
            String newvendorid = "";
            if (request.containsKey(Constants.newvendorid) && request.get(Constants.newvendorid) != null) {
                newvendorid = (String) request.get(Constants.newvendorid);
            }
            if (!StringUtil.isNullOrEmpty(newcustomerid)) {
                condition += " and r.customer = ? ";
                params.add(newcustomerid);
            }
            
            if (!StringUtil.isNullOrEmpty(newvendorid)) {
                if (newvendorid.contains(",")) {
                    newvendorid = AccountingManager.getFilterInString(newvendorid);
                    condition += " and r.vendor.ID IN" + newvendorid;
                } else {
                    params.add(newvendorid);
                    condition += " and r.vendor.ID = ? ";

                }
            }           
            if(request.containsKey("paymentWindowType") && request.get("paymentWindowType")!=null){
                if(request.get("paymentWindowType").toString().equalsIgnoreCase("1")){
                    condition += " and r.vendor is not null ";
                }else if(request.get("paymentWindowType").toString().equalsIgnoreCase("2")){
                    condition += " and r.customer is not null ";
                }
            }
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (r.creationDate >=? and r.creationDate <=?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"r.paymentNumber", "r.account.name"};  //added quick serch on the basis of paid to
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                searchQuery = searchQuery.substring(0, searchQuery.lastIndexOf(")"));   //removing last ')' to add to following customer and vendor search             
                searchQuery += " or (r.customer in (select ID from Customer where name like ?)) or (r.vendor in (select ID from Vendor where name like ?)))";
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
            if (ispendingAproval) { // Get only pending approved records
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
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "OpeningBalanceMakePaymentCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceMakePaymentCustomData", "r.openingBalanceMakePaymentCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "r.openingBalanceMakePaymentCustomData");
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            
            String orderByCondition="";
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                orderByCondition = sortColumnPayment(Col_Name, Col_Dir, true);
            }
            
            String query = "select r from Payment r where r.isOpeningBalencePayment=true AND r.deleted=false AND r.company.companyID=?" + condition + mySearchFilterString + orderByCondition;
            list = executeQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAllOpeningBalancePayments : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    public KwlReturnObject getBillingPayments(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
//            KWLCurrency currency = (KWLCurrency) get(KWLCurrency.class, (String) request.get("currencyid"));
            DateFormat df = (DateFormat) request.get(Constants.df);
            String moduleid = request.containsKey(Constants.moduleid)?(String) request.get(Constants.moduleid):"";
            String start = (String) request.get("start");
            String limit = (String) request.get("limit");
            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            boolean contraentryflag = request.containsKey("contraentryflag") ? (Boolean) request.get("contraentryflag") : false;

            boolean isAdvanceToCustomer = false;
            if (request.get("isadvancetocustomer") != null) {
                isAdvanceToCustomer = (Boolean) request.get("isadvancetocustomer");
            }
            boolean isPostDatedCheque = false;
            if (request.get("isPostDatedCheque") != null) {
                isPostDatedCheque = (Boolean) request.get("isPostDatedCheque");
            }
            boolean isDishonouredCheque = false;
            if (request.get("isDishonouredCheque") != null) {
                isDishonouredCheque = (Boolean) request.get("isDishonouredCheque");
            }
            String ss = (String) request.get("ss");
            ArrayList params = new ArrayList();
            params.add((String) request.get("companyid"));
            String condition = " where r.company.companyID=?  and jed.debit=true ";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"ac.name", "r.billingPaymentNumber", "r.journalEntry.entryNumber", "r.memo"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;

//                params.add(ss + "%");
//                params.add(ss + "%");
//                params.add(ss + "%");
//                params.add(ss + "%");
//                condition += " and (ac.name like ?  or r.billingPaymentNumber like ? or r.journalEntry.entryNumber like ? or r.memo like ? ) ";
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

            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and r.journalEntry.ID IN(" + jeIds + ")";
            }

            if (isAdvanceToCustomer) {
                condition += " and r.receipttype='6' ";
            }

            if (isPostDatedCheque) {
                condition += " and r.payDetail.cheque.dueDate > now() ";
            }
            if (isDishonouredCheque) {
                condition += " and r.payDetail.paymentMethod.detailType=2 ";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (r.journalEntry.entryDate >=? and r.journalEntry.entryDate <=?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            String appendCase = "and";
            String mySearchFilterString = "";
            String joinString = "";
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
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray customSearchFieldArray = new JSONArray();
                    JSONArray defaultSearchFieldArray = new JSONArray();
                    StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);
                    if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field
                    request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, appendCase);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "r.journalEntry.accBillInvCustomData");
                    joinString = " inner join je.accBillInvCustomData accjecustomdata ";
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//    
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            }

            String query = "select r, ac from BillingPayment r inner join r.journalEntry je inner join je.details jed inner join jed.account ac " + condition + mySearchFilterString + " group by r";
            list = executeQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getBillingPayments : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    public KwlReturnObject getJEBRMap(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        String BRID="";
        int count = 0;
        try {
            String condition="";
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {     
                condition += " where journalEntry.id IN(" + jeIds + ")";
            }

            String query = "from BankReconciliationDetail "+ condition +"";
            list = executeQuery( query);
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getJEBRMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    public KwlReturnObject getJEBURMap(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        String BRID="";
        int count = 0;
        try {
            String condition="";
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {     
                condition += " where journalEntry.id IN(" + jeIds + ")";
            }

            String query = "from BankUnreconciliationDetail "+ condition +"";
            list = executeQuery( query);
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getJEBURMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject deletePayments(String paymentid, String companyid) throws ServiceException {
        //Delete Payment
        String delQuery = "delete from Payment p where ID=? and p.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Payment has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deletePaymentEntry(String paymentid, String companyid) throws ServiceException,AccountingException {
        String query = "update payment set advanceid = null, advanceamount=0 where company=? and advanceid=?";
        int numRows = executeSQLUpdate(query, new Object[]{companyid, paymentid});
        query = "update Payment set deleted=true where ID=? and company.companyID=?";
        numRows = executeUpdate( query, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Payment has been deleted successfully.", null, null, numRows);
    }
//query = "update BillingPayment set deleted=true where ID in("+qMarks +") and company.companyID=?";

    public KwlReturnObject deleteBillingPaymentEntry(String paymentid, String companyid) throws ServiceException {
        String query = "update BillingPayment set deleted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Payment has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deletePaymentsDetails(String paymentid, String companyid) throws ServiceException {
        ArrayList params5 = new ArrayList();
        params5.add(companyid);
        params5.add(companyid);
        params5.add(paymentid);
        String delQuery5 = "delete from accjedetailcustomdata where jedetailId in (select id from jedetail where company =? and journalEntry in (select journalentry from payment where company =? and id =?))";
        int numRows5 = executeSQLUpdate(delQuery5, params5.toArray());
//Delete Payment Details
        String delQuery = "delete from PaymentDetail pd where pd.payment.ID=? and pd.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Payment Details has been deleted successfully.", null, null, numRows);
    }
    public KwlReturnObject deletePaymentsDetailsAndUpdateAmountDue(String paymentid, String companyid,int beforeEditApprovalStatus) throws ServiceException {
        //Delete Payment Details
        String selQuery = "from PaymentDetail pd where pd.payment.ID=? and pd.company.companyID=?";
        List<PaymentDetail> details=find(selQuery,new Object[]{paymentid, companyid});
        List<GoodsReceipt> goodsReceiptsList=new ArrayList<GoodsReceipt>();
        for (PaymentDetail paymentDetail : details) {
            double amountdue = 0;
            GoodsReceipt goodsReceipt = paymentDetail.getGoodsReceipt();
            double discountAmtInInvoiceCurrency = paymentDetail.getDiscountAmountInInvoiceCurrency();
//            double discountAmount = paymentDetail.getDiscountAmount();
            boolean isInvoiceIsClaimed = (goodsReceipt.getBadDebtType() == Constants.Invoice_Claimed || goodsReceipt.getBadDebtType() == Constants.Invoice_Recovered);
            if (isInvoiceIsClaimed) {
                goodsReceipt.setClaimAmountDue(goodsReceipt.getClaimAmountDue() + paymentDetail.getAmountInGrCurrency());
            } else {
                if (goodsReceipt.isNormalInvoice()) {
                    amountdue = goodsReceipt.getInvoiceamountdue();
                    /*
                     set status flag for amount due 
                     */
                    double amountdueforstatus = amountdue + paymentDetail.getAmountInGrCurrency()+discountAmtInInvoiceCurrency;
                    if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                        goodsReceipt.setIsOpenPayment(false);
                    } else {
                        goodsReceipt.setIsOpenPayment(true);
                    }
                } else if(goodsReceipt.isIsOpeningBalenceInvoice()){
                    amountdue = goodsReceipt.getOpeningBalanceAmountDue();
                    /*
                     * set status flag for opening invoices
                     */
                    double amountdueforstatus = amountdue + paymentDetail.getAmountInGrCurrency()+discountAmtInInvoiceCurrency;
                    if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                        goodsReceipt.setIsOpenPayment(false);
                    } else {
                        goodsReceipt.setIsOpenPayment(true);
                    }
                }
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyid);
                requestParams.put(Constants.globalCurrencyKey, goodsReceipt.getCurrency().getCurrencyID());
                if (beforeEditApprovalStatus == Constants.APPROVED_STATUS_LEVEL && paymentDetail.getPayment().getApprovestatuslevel() <= 11) {
                    double totalInvoiceAndDiscountAmtInInvoiceCurrency = (goodsReceipt.isNormalInvoice() ? amountdue : goodsReceipt.getOpeningBalanceAmountDue()) + paymentDetail.getAmountInGrCurrency() + discountAmtInInvoiceCurrency;
                    double totalInvoiceAmtPaidAndDiscountInBase = 0d;
                    if (StringUtil.isNullObject(goodsReceipt.getJournalEntry()) && goodsReceipt.isIsOpeningBalenceInvoice()) {
                        if(goodsReceipt.isConversionRateFromCurrencyToBase()){
                            totalInvoiceAmtPaidAndDiscountInBase = totalInvoiceAndDiscountAmtInInvoiceCurrency * goodsReceipt.getExchangeRateForOpeningTransaction();
                        }else{
                            totalInvoiceAmtPaidAndDiscountInBase = totalInvoiceAndDiscountAmtInInvoiceCurrency / goodsReceipt.getExchangeRateForOpeningTransaction();
                        }
                    } else {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalInvoiceAndDiscountAmtInInvoiceCurrency, goodsReceipt.getCurrency().getCurrencyID(), goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                        totalInvoiceAmtPaidAndDiscountInBase = ((Double) bAmt.getEntityList().get(0));
                    }
//                    double invoiceBaseAmountDue = totalInvoiceAmtPaidAndDiscountInBase;
                    totalInvoiceAndDiscountAmtInInvoiceCurrency = authHandler.round(totalInvoiceAndDiscountAmtInInvoiceCurrency, companyid);
                    totalInvoiceAmtPaidAndDiscountInBase = authHandler.round(totalInvoiceAmtPaidAndDiscountInBase, companyid);
                    if(goodsReceipt.isNormalInvoice()) {
                        goodsReceipt.setInvoiceamountdue(totalInvoiceAndDiscountAmtInInvoiceCurrency);
                        goodsReceipt.setInvoiceAmountDueInBase(totalInvoiceAmtPaidAndDiscountInBase);
                    } else {
                        goodsReceipt.setOpeningBalanceAmountDue(totalInvoiceAndDiscountAmtInInvoiceCurrency);
                        goodsReceipt.setOpeningBalanceBaseAmountDue(totalInvoiceAmtPaidAndDiscountInBase);
                    }
                }
            }
            goodsReceiptsList.add(goodsReceipt);
        }
        if(!goodsReceiptsList.isEmpty()){
            saveAll(goodsReceiptsList);
        }
         return deletePaymentsDetails(paymentid, companyid);
    }
    public KwlReturnObject deletePaymentsAgainstCNDN(String paymentId, String companyid,int beforeEditApprovalStatus) throws ServiceException {
        //Delete Payment Details
                    int numRows=0;
                    KwlReturnObject cnhistoryresult = getVendorCnPaymenyHistory("", 0.0, 0.0, paymentId);
                    List<CreditNotePaymentDetails> cnHistoryList = cnhistoryresult.getEntityList();
                    for (CreditNotePaymentDetails cnpd:cnHistoryList) {
                        String cnnoteid = cnpd.getCreditnote().getID()!=null?cnpd.getCreditnote().getID():"";
                        Double cnpaidamount = cnpd.getAmountPaid();
                        Double cnPaidAmountInBaseCurrency =cnpd.getAmountInBaseCurrency();
                        if (beforeEditApprovalStatus == Constants.APPROVED_STATUS_LEVEL) {
                            KwlReturnObject cnjedresult = updateCnAmount(cnnoteid, -cnpaidamount);
                            KwlReturnObject opencnjedresult = updateCnOpeningAmountDue(cnnoteid, -cnpaidamount);
                            KwlReturnObject openingCnBaseAmtDueResult = accPaymentDAOobj.updateCnOpeningBaseAmountDue(cnnoteid, -cnPaidAmountInBaseCurrency);
                        }
                        String query = " delete from creditnotpayment where paymentid = ? and cnid = ? ";
                        numRows += executeSQLUpdate(query, new Object[]{paymentId,cnnoteid});
                    }
        return new KwlReturnObject(true, "Amount has benn updated successfully.", null, null, numRows);
    }
    
    public KwlReturnObject updateCnOpeningAmountDue(String noteid, double amount) throws ServiceException {
        String query = "update CreditNote set openingBalanceAmountDue=(openingBalanceAmountDue-?) where ID=?";
        int numRows = executeUpdate( query, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Amount has benn updated successfully.", null, null, numRows);
    }
    
    public KwlReturnObject updateCnAmount(String noteid, double amount) throws ServiceException {
        String delQuery = "update CreditNote set cnamountdue=(cnamountdue-?) where ID=?";
        int numRows = executeUpdate( delQuery, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Payment Method has been updated successfully.", null, null, numRows);
    }
    public KwlReturnObject deleteLinkPaymentsDetails(String paymentid, String companyid) throws ServiceException {
        //Delete Payment Details
        String delQuery = "delete from LinkDetailPayment pd where pd.payment.ID=? and pd.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Linked Payment Detail  has been deleted successfully.", null, null, numRows);
    }
    public KwlReturnObject deleteAdvancePaymentsDetails(String paymentid, String companyid) throws ServiceException {
        //Delete Payment Details
        String delQuery = "delete from AdvanceDetail ad where ad.payment.ID=? and ad.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Advance Payment has been deleted successfully.", null, null, numRows);
    }
    public KwlReturnObject deleteLinkDetailsAndUpdateAmountDue(Map<String, Object> requestMap,String paymentid, String companyid,boolean tempCheck) throws ServiceException {
        //Delete Payment Details
        String gcurrenyid = requestMap.containsKey(Constants.globalCurrencyKey) && requestMap.get(Constants.globalCurrencyKey)!=null?(String)requestMap.get(Constants.globalCurrencyKey):"";
        String selQuery = "from LinkDetailPayment pd where pd.payment.ID=? and pd.company.companyID=?";
        List<LinkDetailPayment> details=find(selQuery,new Object[]{paymentid, companyid});
        List<GoodsReceipt> goodsReceiptsList=new ArrayList<GoodsReceipt>();
        for (LinkDetailPayment linkDetailPayment : details) {
            GoodsReceipt goodsReceipt = linkDetailPayment.getGoodsReceipt();
            double grExternalCurrencyRate = 0d;
            Date grCreationDate = null;
            String grCurrencyID = goodsReceipt.getCurrency() != null ? goodsReceipt.getCurrency().getCurrencyID() : gcurrenyid;
            grCreationDate = goodsReceipt.getCreationDate();
            if (goodsReceipt.isIsOpeningBalenceInvoice() && !goodsReceipt.isNormalInvoice()) {
                grExternalCurrencyRate = goodsReceipt.getExchangeRateForOpeningTransaction();
            } else {
//                grCreationDate = goodsReceipt.getJournalEntry().getEntryDate();
                grExternalCurrencyRate = goodsReceipt.getJournalEntry().getExternalCurrencyRate();
            }
            double amountPaid = linkDetailPayment.getAmountInGrCurrency();
            double amountPaidInbase = 0;
            if(goodsReceipt.isNormalInvoice()){
                KwlReturnObject grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestMap, amountPaid, grCurrencyID, grCreationDate, grExternalCurrencyRate);
                if (grAmtInBaseResult != null) {
                    amountPaidInbase = authHandler.round((Double) grAmtInBaseResult.getEntityList().get(0), companyid);
                }
                /*
                 set status flag for amount due 
                 */
                double amountdueforstatus = goodsReceipt.getInvoiceamountdue() + linkDetailPayment.getAmountInGrCurrency();
                if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                    goodsReceipt.setIsOpenPayment(false);
                } else {
                    goodsReceipt.setIsOpenPayment(true);
                }
                goodsReceipt.setInvoiceamountdue(goodsReceipt.getInvoiceamountdue()+linkDetailPayment.getAmountInGrCurrency());
                goodsReceipt.setInvoiceAmountDueInBase(goodsReceipt.getInvoiceAmountDueInBase() + amountPaidInbase);
            } else if (goodsReceipt.isIsOpeningBalenceInvoice()) {
                double amountdue = goodsReceipt.getInvoiceamountdue();
                /*
                 * set status flag for opening invoices
                 */
                double amountdueforstatus = amountdue + linkDetailPayment.getAmountInGrCurrency();
                if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                    goodsReceipt.setIsOpenPayment(false);
                } else {
                    goodsReceipt.setIsOpenPayment(true);

                }
                KwlReturnObject grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestMap, amountPaid, grCurrencyID, grCreationDate, grExternalCurrencyRate);
                if (grAmtInBaseResult != null) {
                    amountPaidInbase = authHandler.round((Double) grAmtInBaseResult.getEntityList().get(0), companyid);
                }
                goodsReceipt.setOpeningBalanceAmountDue(goodsReceipt.getOpeningBalanceAmountDue() + linkDetailPayment.getAmountInGrCurrency());
                goodsReceipt.setOpeningBalanceBaseAmountDue(goodsReceipt.getOpeningBalanceBaseAmountDue() + amountPaidInbase);
            }
            if((goodsReceipt.getInvoiceamountdue()+linkDetailPayment.getAmountInGrCurrency())!=0){
                goodsReceipt.setAmountDueDate(null);
            }
            goodsReceiptsList.add(goodsReceipt);
        }
        if(!goodsReceiptsList.isEmpty()){
            saveAll(goodsReceiptsList);
        }
        /*
         * tempCheck is for temporary delete receipt
         * if payment is temporary delete then tempCheck is set as true
         */
        if (tempCheck) {
            return new KwlReturnObject(true, "Payment Details has been deleted successfully.", null, null, 0);
        } else {
            return deleteLinkPaymentsDetails(paymentid, companyid);
        }
         
    }
    public KwlReturnObject deleteBillingPaymentsDetails(String paymentid, String companyid) throws ServiceException {
        //Delete Billing Payment Details
        String delQuery = "delete from BillingPaymentDetail pd where pd.billingPayment.ID=? and pd.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Payment Details has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getPaymentDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from PaymentDetail";
        return buildNExecuteQuery( query, requestParams);
    }
    public List<String> getTotalJEDIDPaymentDetails(String paymentid,String companyid) throws ServiceException {
        String query = "select totalJED.ID from PaymentDetail where payment.ID=? and totalJED is not null";
         List<String> detailsList = executeQuery( query,new Object[]{paymentid});
        return detailsList;
    }

    public KwlReturnObject getBillingPaymentDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from BillingPaymentDetail";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getJEFromPayment(String paymentid) throws ServiceException {
        List list = new ArrayList();
        String query = "select p.journalEntry.ID from Payment p where p.ID=? and p.company.companyID=p.journalEntry.company.companyID";
        list = executeQuery( query, new Object[]{paymentid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getJEFromBillingPayment(String paymentid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "from BillingPayment p where p.ID = ? and p.company.companyID = ?";
        list = executeQuery( query, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getPaymentsFromGReceipt(String receiptid, String companyid) throws ServiceException {
        List list = new ArrayList();
        //ERP-11287
        String query = "from PaymentDetail pd  where pd.goodsReceipt.ID=? and pd.company.companyID=?";  //and pd.payment.deleted=false 
        list = executeQuery( query, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getPaymentsFromGReceipt(HashMap<String, Object> reqParams) throws ServiceException{
        List list=null;
        try {
            String condition="";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) reqParams.get(Constants.df);
            String invoiceId = (String) reqParams.get("grid");
            params.add(invoiceId);
            if(reqParams.containsKey("companyid") && reqParams.get("companyid")!=null){
                String companyId = (String) reqParams.get("companyid");
                condition += " and pd.company.companyID=?";
                params.add(companyId);
            }            
            if(reqParams.containsKey("asofdate") && reqParams.get("asofdate")!=null){
                String asOfDate = (String) reqParams.get("asofdate");
//                condition += "  and (pd.payment.creationDate<=? or pd.payment.journalEntry.entryDate<=?)" ;
                condition += "  and (pd.payment.creationDate<=? )" ;
                params.add(df.parse(asOfDate));
//                params.add(df.parse(asOfDate));
            }
            boolean isMonthlyAgedPayable = false;
            if (reqParams.containsKey("isMonthlyAgedPayable") && reqParams.get("isMonthlyAgedPayable") != null) {
                isMonthlyAgedPayable = Boolean.parseBoolean(reqParams.get("isMonthlyAgedPayable").toString());
            }
            if (isMonthlyAgedPayable) { // call from monthly aged payable report
                String selQuery = "select pd.ID from PaymentDetail pd where pd.goodsReceipt.ID=? and pd.payment.deleted=false" + condition;
                list = executeQuery(selQuery, params.toArray());
            } else {
                String selQuery = "from PaymentDetail pd where pd.goodsReceipt.ID=? and pd.payment.deleted=false and pd.payment.isDishonouredCheque=false" + condition;
                list = executeQuery(selQuery, params.toArray());
            }
        }catch (ParseException ex) {
                Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getLinkedDetailsPayment(HashMap<String, Object> reqParams) throws ServiceException { 
        List list = null;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) reqParams.get(Constants.df);
            if(reqParams.containsKey("grid") && reqParams.get("grid") != null){
                   String grId = (String) reqParams.get("grid");
                   condition += " and pd.goodsReceipt.ID=?";
                   params.add(grId);
            }
            if(reqParams.containsKey("paymentid") && reqParams.get("paymentid") != null){
                   String paymentId = (String) reqParams.get("paymentid");
                   condition += " and pd.payment.ID=?";
                   params.add(paymentId);
            }
            if (reqParams.containsKey("companyid") && reqParams.get("companyid") != null) {
                String companyId = (String) reqParams.get("companyid");
                condition += " and pd.company.companyID=?";
                params.add(companyId);
            }
            if (reqParams.containsKey("asofdate") && reqParams.get("asofdate") != null) {
                String asOfDate = (String) reqParams.get("asofdate");
                condition += "  and pd.paymentLinkDate<=?";
                params.add(df.parse(asOfDate));
            }
            boolean isMonthlyAgedPayable = false;
            if (reqParams.containsKey("isMonthlyAgedPayable") && reqParams.get("isMonthlyAgedPayable") != null) {
                isMonthlyAgedPayable = Boolean.parseBoolean(reqParams.get("isMonthlyAgedPayable").toString());
            }
            if (isMonthlyAgedPayable) { // call from monthly aged payable report
                String selQuery = "select pd.amountInGrCurrency from LinkDetailPayment pd where  pd.payment.deleted=false " + condition;
                list = executeQuery(selQuery, params.toArray());
            } else {
                String selQuery = "from LinkDetailPayment pd where  pd.payment.deleted=false and pd.payment.isDishonouredCheque=false" + condition;
                list = executeQuery(selQuery, params.toArray());
            }
        } catch (ParseException ex) {
            Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public Map<String, List<PaymentDetail>> getPaymentsInfoFromGReceiptList(List<String> invoiceIDLIST) throws ServiceException {
        Map<String, List<PaymentDetail>> invoiceMap = new HashMap<String, List<PaymentDetail>>();
        if (invoiceIDLIST != null && !invoiceIDLIST.isEmpty()) {
            List li = null;
            String query = "select  pd.goodsReceipt.ID, pd "
                    + " from PaymentDetail pd where pd.goodsReceipt.ID in (:invoiceIDList) and pd.payment.deleted=false";
            List<List> values = new ArrayList<List>();
            values.add(invoiceIDLIST);
            List<Object[]> results = executeCollectionQuery( query, Collections.singletonList("invoiceIDList"), values);

            if (results != null) {
                for (Object[] result : results) {
                    String invID = (String) result[0];
                    if (invoiceMap.containsKey(invID)) {
                        li = invoiceMap.get(invID);
                    } else {
                        li = new ArrayList<PaymentDetail>();
                    }
                    li.add((PaymentDetail) result[1]);
                    invoiceMap.put(invID, li);
                }
            }
        }
        return invoiceMap;
    }

    public KwlReturnObject getContraPayReceiptFromGReceipt(String receiptid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "from ReceiptDetail pd  where pd.goodsReceipt.ID=? and pd.receipt.deleted=false and pd.company.companyID=?";
        list = executeQuery( query, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    public KwlReturnObject getContraPayReceiptIDFromGReceipt(String receiptid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "select pd.ID from ReceiptDetail pd  where pd.goodsReceipt.ID=? and pd.receipt.deleted=false and pd.company.companyID=?";
        list = executeQuery( query, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getPaymentFromNo(String pno, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from Payment where paymentNumber=? and company.companyID=?";
        list = executeQuery( q, new Object[]{pno, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBillingPaymentFromNo(String pno, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from BillingPayment where billingPaymentNumber=? and company.companyID=?";
        list = executeQuery( q, new Object[]{pno, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getPaymentFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from Payment where journalEntry.ID=? and deleted=false and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getPaymentLinkedInvoiceJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from LinkDetailPayment where linkedGainLossJE=?";
        List list = executeQuery( selQuery, new Object[]{jeid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBillingPaymentFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from BillingPayment where journalEntry.ID=? and deleted=false and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBillingPaymentsFromGReceipt(String receiptid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "from BillingPaymentDetail pd  where pd.billingGoodsReceipt.ID = ?  and pd.billingPayment.deleted=false and pd.company.companyID=?";
//        String query = "from PaymentDetail pd  where pd.goodsReceipt.ID=? and pd.payment.deleted=false and pd.company.companyID=?";
        list = executeQuery( query, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject saveVendorCnPaymenyHistory(String cnnoteid, double paidncamount, double originalamountdue, String paymentId) throws ServiceException {
        List list = new ArrayList();

        try {
            String uuid = UUID.randomUUID().toString();
            //     String invoiceID = hm.get("invoiceid").toString();
            String query = "insert into creditnotpayment (id,cnid,paymentid,amountdue,amountpaid) values(?,?,?,?,?)";
//            list = executeQuery( query, new Object[]{invoiceID});
            executeSQLUpdate(query, new Object[]{uuid, cnnoteid, paymentId, originalamountdue, paidncamount});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.saveVendorCnPaymenyHistory:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject saveVendorCnPaymenyHistory(HashMap<String,String> hashMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String uuid = UUID.randomUUID().toString();
            String cnid=hashMap.get("cnnoteid");
            String paymentid=hashMap.get("paymentId");
            double amountdue=Double.parseDouble(hashMap.get("originalamountdue"));
            double tdsamount=Double.parseDouble(hashMap.containsKey("tdsamount") ? hashMap.get("tdsamount"): "0");
            double amountpaid=Double.parseDouble(hashMap.get("paidncamount"));
            String fromcurrency=hashMap.get("tocurrency");
            String tocurrency=hashMap.get("fromcurrency");
            String exchangeratefortransaction=hashMap.get("exchangeratefortransaction");
            double amountinpaymentcurrency=Double.parseDouble(hashMap.get("amountinpaymentcurrency"));
            double paidamountinpaymentcurrency=Double.parseDouble(hashMap.get("paidamountinpaymentcurrency"));
            String description= StringUtil.DecodeText(hashMap.get("description"));
            double gstCurrencyRate=Double.parseDouble(hashMap.get("gstCurrencyRate"));
            int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(hashMap.get("srNoForRow"));            
            String jedetailId=hashMap.get("jedetail")!=null?hashMap.get("jedetail"):"";
            double amountinbasecurrency = Double.parseDouble(hashMap.get("amountinbasecurrency"));
            String query = "insert into creditnotpayment (id,cnid,paymentid,amountdue,amountpaid,tocurrency,fromcurrency,exchangeratefortransaction,amountinpaymentcurrency,paidamountinpaymentcurrency,description,gstcurrencyrate,srNoForRow,totaljedid,amountinbasecurrency,tdsamount) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            executeSQLUpdate(query, new Object[]{uuid,cnid,paymentid,amountdue,amountpaid,tocurrency,fromcurrency,exchangeratefortransaction,amountinpaymentcurrency,paidamountinpaymentcurrency,description,gstCurrencyRate,srNoForRow,jedetailId,amountinbasecurrency,tdsamount});
            list.add(uuid);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.saveVendorCnPaymenyHistory:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject saveTDSdetailsRow(HashMap<String, Object> tdsRequestParams) throws ServiceException{
        List list = new ArrayList();
        
        JSONObject tdsDetailsJsonObj = null;
        CreditNotePaymentDetails creditNotePaymentDetails = null;
        Company company = null;
        if(tdsRequestParams.containsKey("tdsDetailsJsonObj")){
            tdsDetailsJsonObj = (JSONObject) tdsRequestParams.get("tdsDetailsJsonObj");
        }
        if(tdsRequestParams.containsKey("creditNotePaymentDetailsObj")){
            creditNotePaymentDetails = (CreditNotePaymentDetails) tdsRequestParams.get("creditNotePaymentDetailsObj");
        }
        if(tdsRequestParams.containsKey("companyObj")){
            company = (Company) tdsRequestParams.get("companyObj");
        }
        
        try {
            JSONArray jsonArr=tdsDetailsJsonObj.getJSONArray("appliedTDS");    
            for(int i=0;i<jsonArr.length();i++){
                TDSRate tdsRate = null;
                Account account=null,tdsAccount=null;
                jsonArr = tdsDetailsJsonObj.getJSONArray("appliedTDS");
                JSONObject jsonObj=jsonArr.getJSONObject(0);    
                if (!jsonObj.has("ruleid") || StringUtil.isNullOrEmpty(jsonObj.getString("ruleid"))) {
                    break;
                }
                String documenttype= tdsDetailsJsonObj.getString("documenttype");
                String ruleId=jsonObj.getString("ruleid");
                String accountid=jsonObj.getString("accountid");
                String natureofpayment=jsonObj.getString("natureofpayment");
                String amount=jsonObj.getString("amount");
                String rowTaxAmount=jsonObj.getString("rowTaxAmount");
                String includetax=jsonObj.getString("includetax");
                String documentdetail=jsonObj.getString("rowid");
                String tdsaccountid=jsonObj.getString("tdsaccountid");
                double tdspercentage=(StringUtil.isNullOrEmpty(jsonObj.getString("tdspercentage"))?0:jsonObj.getDouble("tdspercentage"));
                double tdsamount=(StringUtil.isNullOrEmpty(jsonObj.getString("tdsamount"))?0:jsonObj.getDouble("tdsamount"));
                double enteramount=(StringUtil.isNullOrEmpty(jsonObj.getString("enteramount"))?0:jsonObj.getDouble("enteramount"));
                String tdsjedid=jsonObj.getString("tdsjedid");
                String uuid=StringUtil.generateUUID();

                if(!StringUtil.isNullOrEmpty(ruleId)){
                    KwlReturnObject tdsRateData = accountingHandlerDAOobj.getObject(TDSRate.class.getName(), Integer.parseInt(ruleId));
                    tdsRate = (TDSRate) tdsRateData.getEntityList().get(0);
                }
                if(!StringUtil.isNullOrEmpty(accountid)){
                    KwlReturnObject accountData = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
                    account = (Account) accountData.getEntityList().get(0);
                }
                if(!StringUtil.isNullOrEmpty(tdsaccountid)){
                    KwlReturnObject accountData = accountingHandlerDAOobj.getObject(Account.class.getName(), tdsaccountid);
                    tdsAccount = (Account) accountData.getEntityList().get(0);
                }

                String query = "insert into tdsdetails(id,documenttype,documentdetails,includetaxamount,account,ruleid,tdspercentage,tdsamount,enteramount,company,creditnotepaymentdetail,journalentrydetail,tdspayableaccount) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
                executeSQLUpdate(query, new Object[]{uuid,documenttype,documentdetail,true,account.getID(),tdsRate,tdspercentage,tdsamount,enteramount,company.getCompanyID(),creditNotePaymentDetails.getID(), tdsjedid,tdsAccount});

            }
        } catch (JSONException ex) {
            Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getVendorCnPaymenyHistory(String cnnoteid, double paidncamount,double originalamountdue,String paymentId) throws ServiceException {
       List list=new ArrayList();
      
        try{
            if(StringUtil.isNullOrEmpty(cnnoteid)){
                String query = "From CreditNotePaymentDetails cnpd where cnpd.payment.ID=? ";
                list =executeQuery(query, new Object[]{paymentId});
                query = "delete from creditnotpayment where paymentid=? ";
                executeSQLUpdate(query, new Object[]{paymentId});
            }else{
                String query = "From CreditNotePaymentDetails cnpd where cnpd.creditnote.ID=? and cnpd.payment.ID=? ";
                list =executeQuery(query, new Object[]{cnnoteid,paymentId});
                query = "delete from creditnotpayment where cnid=? and paymentid=? ";
                executeSQLUpdate(query, new Object[]{cnnoteid,paymentId});
            }
        }catch(Exception ex){
            throw ServiceException.FAILURE("accVendorPaymentImpl.saveVendorCnPaymenyHistory:"+ex.getMessage(), ex);
        } 
        return new KwlReturnObject(true, "", null, list,list.size());
    }
    @Override
    public KwlReturnObject saveVendorDnPaymenyHistory(String cnnoteid, double paidncamount, double originalamountdue, String paymentId) throws ServiceException {
        List list = new ArrayList();
      
        try {
            String uuid = UUID.randomUUID().toString();
       //     String invoiceID = hm.get("invoiceid").toString();
            String query = "insert into makedebitnotepayment (id,dnid,paymentid,amountdue,amountpaid) values(?,?,?,?,?)";
//            list = executeQuery( query, new Object[]{invoiceID});
            executeSQLUpdate(query, new Object[]{uuid, cnnoteid, paymentId, originalamountdue, paidncamount});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.saveVendorCnPaymenyHistory:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getVendorCnPayment(String paymentId) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "From CreditNotePaymentDetails cnpd where cnpd.payment.ID= ?";   
            list = executeQuery(query, new Object[]{paymentId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorCnPayment:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getVendorCnPayment(String paymentId,String cnnoteid) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "From CreditNotePaymentDetails cnpd where cnpd.creditnote.ID=? and cnpd.payment.ID=?";
            list =executeQuery(query, new Object[]{cnnoteid,paymentId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorCnPayment:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getVendorCnPaymentWithAdvance(String paymentId) throws ServiceException {
        List list = new ArrayList();
        try {
            if(!StringUtil.isNullOrEmpty(paymentId)){
                Payment payment=(Payment)get(Payment.class, paymentId);
                String query = "SELECT cnp.* from  creditnotpayment cnp INNER JOIN payment p on cnp.paymentid=p.cndnandinvoiceid where p.id= ?";
                if(payment.isIsadvancepayment()&&payment.getCndnAndInvoiceId()==null){
                    query = "SELECT cnp.* from  creditnotpayment cnp INNER JOIN payment p on cnp.paymentid=p.id where p.advanceid=?";
                }
                list = executeSQLQuery(query, new Object[]{paymentId});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorCnPayment:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public Payment getPaymentObject(Payment payment) throws ServiceException {
        List list = new ArrayList();
        Payment paymentObject=null;
        try {
            if(payment!=null){
                if(payment.getInvoiceAdvCndnType()==1&&!StringUtil.isNullOrEmpty(payment.getCndnAndInvoiceId())){
                    paymentObject=(Payment)get(Payment.class, payment.getCndnAndInvoiceId());
                }else if(payment.getInvoiceAdvCndnType()==2){
                    List<Payment> paymentList=find("from Payment where invoiceAdvCndnType=3 and advanceid.ID='"+payment.getID()+"'");
                    if(!paymentList.isEmpty())
                        paymentObject=paymentList.get(0);
                }
               
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorCnPayment:" + ex.getMessage(), ex);
        }
        return paymentObject;
    }
    @Override
    public KwlReturnObject getInvoiceAdvPaymentList(HashMap <String,String> payHashMap) throws ServiceException {
       List list = new ArrayList();
        try {
            String paymentId=payHashMap.get("paymentId");
            int invoiceAdvCNDN=!StringUtil.isNullOrEmpty(payHashMap.get("invoiceadvcndntype"))?Integer.parseInt(payHashMap.get("invoiceadvcndntype")):0;
            String condition="";
            if(!StringUtil.isNullOrEmpty(paymentId)){
                Payment payment=(Payment)get(Payment.class, paymentId);
                List params = new ArrayList();
                if(payment.isIsadvancepayment()&&payment.getInvoiceAdvCndnType()==2){
                    condition+=" where advanceid.ID=? ";
                    params.add(payment.getID());
                }
                if(payment.getInvoiceAdvCndnType()==1||payment.getInvoiceAdvCndnType()==3){
                    if(payment.getAdvanceid()!=null){
                        condition+=" where ID = ? ";
                        params.add(payment.getAdvanceid().getID());
                    }
                    
                    if(!StringUtil.isNullOrEmpty(payment.getCndnAndInvoiceId())){
                        if(StringUtil.isNullOrEmpty(condition)){
                            condition+=" where ";
                        }else{
                            condition+=" or ";
                        }
                        condition+=" ID = ? ";
                        params.add(payment.getCndnAndInvoiceId());
                    }
                    
                }
                String query = "from Payment "+condition;
                if(!StringUtil.isNullOrEmpty(condition)){
                    list = executeQuery(query, params.toArray());
                }
                list.add(payment);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorCnPayment:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getVendorDnPayment(String paymentId) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "From DebitNotePaymentDetails dnpd where dnpd.receipt.ID = ?";
            list = executeQuery(query, new Object[]{paymentId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorDnPayment:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
   @Override
    public KwlReturnObject getVendorDnPaymentWithAdvance(String receiptId) throws ServiceException {
        List list = new ArrayList();
        try {
            if(!StringUtil.isNullOrEmpty(receiptId)){
                Receipt receipt=(Receipt)get(Receipt.class, receiptId);
                String query = "SELECT dnp.* from  debitnotepayment dnp INNER JOIN receipt r on dnp.receiptid=r.cndnandinvoiceid where r.id= ?";
                if(receipt.isIsadvancepayment()&&receipt.getCndnAndInvoiceId()==null){
                    query = "SELECT dnp.* from  debitnotepayment dnp INNER JOIN receipt r on dnp.receiptid=r.id where r.advanceid=?";
                }
                list = executeSQLQuery(query, new Object[]{receiptId});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorCnPayment:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPaymentIdLinkedWithNote(String noteId) throws ServiceException {
        List params = new ArrayList();
        params.add(noteId);
        String query = "select paymentid from creditnotpayment where cnid=?";
        List list = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject savePaymentDetailOtherwise(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try {
            PaymentDetailOtherwise paymentDetailOtherwise = null;
            //String paymentid = (String) hm.get("payment");
            String paymentotherwiseid = (String) hm.get("paymentotherwise");

            if (StringUtil.isNullOrEmpty(paymentotherwiseid)) {
                paymentDetailOtherwise = new PaymentDetailOtherwise();

            } else {
                paymentDetailOtherwise = (PaymentDetailOtherwise) get(PaymentDetailOtherwise.class, paymentotherwiseid);
            }

            if (hm.containsKey("amount")) {
                paymentDetailOtherwise.setAmount((Double) hm.get("amount"));
            }
            if (hm.containsKey("taxamount")) {
                paymentDetailOtherwise.setTaxamount((Double) hm.get("taxamount"));
            }
            if (hm.containsKey("tdsamount")) {
                paymentDetailOtherwise.setTdsamount((Double) hm.get("tdsamount"));
            }
            if (hm.containsKey("description")) {

                paymentDetailOtherwise.setDescription(StringUtil.DecodeText((String) hm.get("description")));
            }
            if (hm.containsKey("taxjedid")) {

                paymentDetailOtherwise.setTaxJedId((String) hm.get("taxjedid"));
            }
            if (hm.containsKey("accountid")) {
                Account account = hm.get("accountid") == null ? null : (Account) get(Account.class, (String) hm.get("accountid"));
                paymentDetailOtherwise.setAccount(account);
            }

            if (hm.containsKey("payment")) {
                Payment payment = hm.get("payment") == null ? null : (Payment) get(Payment.class, (String) hm.get("payment"));
                paymentDetailOtherwise.setPayment(payment);
            }
            if (hm.containsKey("jedetail") && hm.get("jedetail")!=null && !StringUtil.isNullOrEmpty((String)hm.get("jedetail")) ) {
                JournalEntryDetail jed = hm.get("jedetail") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("jedetail"));
                paymentDetailOtherwise.setTotalJED(jed);
            }
            if (hm.containsKey("taxjedetail") && hm.get("taxjedetail")!=null) {
                JournalEntryDetail jed = hm.get("taxjedetail") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("taxjedetail"));
                paymentDetailOtherwise.setGstJED(jed);
            }
            if (hm.containsKey("tax")) {
                Tax tax = hm.get("tax") == null ? null : (Tax) get(Tax.class, (String) hm.get("tax"));
                paymentDetailOtherwise.setTax(tax);
            }
            if (hm.containsKey("isdebit")) {
                paymentDetailOtherwise.setIsdebit(Boolean.parseBoolean(hm.get("isdebit").toString()));
            }
            if (hm.containsKey("srNoForRow")) {
                paymentDetailOtherwise.setSrNoForRow(Integer.parseInt(hm.get("srNoForRow").toString()));
            }
            if(hm.containsKey("gstApplied")) {                
                paymentDetailOtherwise.setGstapplied((Tax)hm.get("gstApplied"));
            }
            saveOrUpdate(paymentDetailOtherwise);
            list.add(paymentDetailOtherwise);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.savePayment : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Payment has been updated successfully", null, list, list.size());
    }
    @Override
    public PaymentDetailOtherwise getPaymentDetailOtherwiseObject(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        PaymentDetailOtherwise paymentDetailOtherwise = null;
        try {
            //String paymentid = (String) hm.get("payment");
            String paymentotherwiseid = (String) hm.get("paymentotherwise");

            if (StringUtil.isNullOrEmpty(paymentotherwiseid)) {
                paymentDetailOtherwise = new PaymentDetailOtherwise();

            } else {
                paymentDetailOtherwise = (PaymentDetailOtherwise) get(PaymentDetailOtherwise.class, paymentotherwiseid);
            }

            if (hm.containsKey("amount")) {
                paymentDetailOtherwise.setAmount((Double) hm.get("amount"));
            }
            if (hm.containsKey("taxamount")) {
                paymentDetailOtherwise.setTaxamount((Double) hm.get("taxamount"));
            }
            if (hm.containsKey("description")) {

                paymentDetailOtherwise.setDescription(StringUtil.DecodeText((String) hm.get("description")));
            }
            if (hm.containsKey("taxjedid")) {

                paymentDetailOtherwise.setTaxJedId((String) hm.get("taxjedid"));
            }
            if (hm.containsKey("accountid")) {
                Account account = hm.get("accountid") == null ? null : (Account) get(Account.class, (String) hm.get("accountid"));
                paymentDetailOtherwise.setAccount(account);
            }

            if (hm.containsKey("payment")) {
                Payment payment = hm.get("payment") == null ? null : (Payment) get(Payment.class, (String) hm.get("payment"));
                paymentDetailOtherwise.setPayment(payment);
            }
            if (hm.containsKey("tax")) {
                Tax tax = hm.get("tax") == null ? null : (Tax) get(Tax.class, (String) hm.get("tax"));
                paymentDetailOtherwise.setTax(tax);
            }
            if (hm.containsKey("isdebit")) {
                paymentDetailOtherwise.setIsdebit(Boolean.parseBoolean(hm.get("isdebit").toString()));
            }
            list.add(paymentDetailOtherwise);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.savePayment : " + ex.getMessage(), ex);
        }
        return paymentDetailOtherwise;
    }

    @Override
    public KwlReturnObject getPaymentDetailOtherwise(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from PaymentDetailOtherwise ";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getCreditNotePaymentDetails(HashMap<String, Object> requestParams) throws ServiceException{
        String query = "from CreditNotePaymentDetails ";
        return buildNExecuteQuery( query, requestParams);
    }
    public List<String> getTotalJEDIDCreditNotePaymentDetails(String paymentid,String companyid) throws ServiceException {
//        String query = "select totalJED.ID from CreditNotePaymentDetails where payment.company.companyID=? and payment.ID=? and totalJED is not null";
        String query = "select totalJED.ID from CreditNotePaymentDetails where payment.ID=? and totalJED is not null";
        List<String> detailsList = executeQuery( query,paymentid);
        return detailsList;
    }
    
    //Suresh - Need to make this as common function as other modules will also call this.
    @Override
    public KwlReturnObject getinvoiceDocuments(HashMap<String, Object> dataMap) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String start = (String) dataMap.get(Constants.start);
            String limit = (String) dataMap.get(Constants.limit);

            ArrayList params = new ArrayList();

            params.add((String) dataMap.get(Constants.companyKey));

            String conditionSQL = " where invoicedoccompmap.company=?";

            String invoiceId = (String) dataMap.get("invoiceID");
            if (!StringUtil.isNullOrEmpty(invoiceId)) {
                params.add(invoiceId);
                conditionSQL += " and invoicedoccompmap.invoiceid=?";
            }

            String mysqlQuery = "select invoicedocuments.docname  as docname,invoicedocuments.doctypeid as doctypeid,invoicedocuments.docid as docid "
                    + "from invoicedoccompmap inner join invoicedocuments on invoicedoccompmap.documentid=invoicedocuments.id " + conditionSQL;

            list = executeSQLQuery(mysqlQuery, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeSQLQueryPaging(mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.getinvoiceDocuments:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    @Override
    public KwlReturnObject getCalculatedMakePaymentOtherwiseTax(Map<String, Object> requestParams) throws ServiceException {
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
//            paramslist.add(tax.getAccount().getID());
//            conditionForAccount += " or pdo.account.ID=? ";
//        }
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition += " and pdo.payment.journalEntry.entryDate >= ? and pdo.payment.journalEntry.entryDate <= ? and pdo.payment.journalEntry.pendingapproval=0";
            Condition += " and pdo.payment.creationDate >= ? and pdo.payment.creationDate <= ? and pdo.payment.journalEntry.pendingapproval=0";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
		
	String ss = requestParams.containsKey("ss")?(String) requestParams.get("ss"):"";
        
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{ "pdo.payment.journalEntry.entryNumber","pdo.payment.paymentNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramslist, ss, 2);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                searchQuery=searchQuery.substring(0,searchQuery.lastIndexOf(")"));   //removing last ')' to add to following customer and vendor search             
                searchQuery+=" or (pdo.gstapplied.ID in (select ID from Tax where name like ?)))";   // ERP-9272
                paramslist.add("%"+ss+"%");
                Condition +=searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        String mySearchFilterString = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", "pdo.payment.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", " pdo.gstJED.accJEDetailCustomData");
            }
        }
//            if(StringUtil.isNullOrEmpty(ss)==false){
//               for(int i=0;i<=3;i++){
//                 paramslist.add(ss+"%");
//               }
//                 Condition+= " and (pdo.tax.name like ? or pdo.account.name like ?  or pdo.payment.journalEntry.entryNumber like ? or pdo.payment.paymentNumber like ? ) ";
//        }
        /*
         * Below code is modified for ERP-9272
         * Added new column 'appliedgst'  as told by Paritosh sir
         */
        String query="";
        if (showDishonouredPayment) {
            query = "from PaymentDetailOtherwise pdo where (pdo.gstapplied.ID = ? " + conditionForAccount + ") and pdo.payment.deleted=false " + Condition + mySearchFilterString;
        } else {
            query = "from PaymentDetailOtherwise pdo where (pdo.gstapplied.ID = ? " + conditionForAccount + ") and pdo.payment.deleted=false and pdo.payment.isDishonouredCheque=false " + Condition + mySearchFilterString;
        }
        returnlist = executeQuery( query, paramslist.toArray());
//        ((PaymentDetailOtherwise)returnlist.get(0)).getPayment().getJournalEntry().getEntryDate()
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }
    
    @Override
    public KwlReturnObject getCalculatedDebitNoteOtherwiseTax(Map<String, Object> requestParams) throws ServiceException {
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
//            conditionForAccount += " or dnt.account.ID=? ";
//        }

        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition += " and dnt.debitNote.journalEntry.entryDate >= ? and dnt.debitNote.journalEntry.entryDate <= ?";
            Condition += " and dnt.debitNote.creationDate >= ? and dnt.debitNote.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }

       String ss = requestParams.containsKey("ss")?(String) requestParams.get("ss"):"";
       
       if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"dnt.debitNote.journalEntry.entryNumber", "dnt.debitNote.debitNoteNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramslist, ss, 2);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                searchQuery=searchQuery.substring(0,searchQuery.lastIndexOf(")"));   //removing last ')' to add to following customer and vendor search             
                searchQuery+=" or (dnt.debitNote.customer in (select ID from Customer where name like ?)) or (dnt.debitNote.vendor in (select ID from Vendor where name like ?)) or (dnt.tax in (select ID from Tax where name like ?)) or (dnt.account in (select ID from Account where name like ?)))";
                paramslist.add("%"+ss+"%");
                paramslist.add("%"+ss+"%");
                paramslist.add("%"+ss+"%");
                paramslist.add("%"+ss+"%");
                Condition +=searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String mySearchFilterString = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", "dnt.debitNote.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", "gstJED.accJEDetailCustomData");
            }
        }
//            if(StringUtil.isNullOrEmpty(ss)==false){
//               for(int i=0;i<=4;i++){
//                 paramslist.add(ss+"%");
//               }
//                 Condition+= " and ( dnt.tax.name like ?  or dnt.debitNote.journalEntry.entryNumber like ? or dnt.debitNote.debitNoteNumber like ?  or (dnt.debitNote.customer in (select ID from Customer where name like ?)) or (dnt.debitNote.vendor in (select ID from Vendor where name like ?))) ";
//        }

        String query = "from DebitNoteTaxEntry dnt where (dnt.tax.ID = ?" + conditionForAccount + ") and dnt.debitNote.deleted=false AND dnt.debitNote.approvestatuslevel=11 AND dnt.debitNote.oldRecord = false " + Condition + mySearchFilterString;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    public KwlReturnObject getVendorPaymentCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from AccJEDetailCustomData";
        return buildNExecuteQuery( query, requestParams);
    }
    public KwlReturnObject getVendorPaymentGlobalCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from AccJECustomData";
        return buildNExecuteQuery( query, requestParams);
    }


    @Override
    public KwlReturnObject saveBillingPaymentDetailOtherwise(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            BillingPaymentDetailOtherwise billingPaymentDetailOtherwise = null;
            //String paymentid = (String) hm.get("payment");
            String paymentotherwiseid = (String) hm.get("paymentotherwise");

            if (StringUtil.isNullOrEmpty(paymentotherwiseid)) {
                billingPaymentDetailOtherwise = new BillingPaymentDetailOtherwise();

            } else {
                billingPaymentDetailOtherwise = (BillingPaymentDetailOtherwise) get(BillingPaymentDetailOtherwise.class, paymentotherwiseid);
            }

            if (hm.containsKey("amount")) {
                billingPaymentDetailOtherwise.setAmount((Double) hm.get("amount"));
            }
            if (hm.containsKey("taxamount")) {
                billingPaymentDetailOtherwise.setTaxamount((Double) hm.get("taxamount"));
            }
            if (hm.containsKey("description")) {

                billingPaymentDetailOtherwise.setDescription(StringUtil.DecodeText((String) hm.get("description")));
            }
            if (hm.containsKey("taxjedid")) {

                billingPaymentDetailOtherwise.setTaxJedId((String) hm.get("taxjedid"));
            }
            if (hm.containsKey("accountid")) {
                Account account = hm.get("accountid") == null ? null : (Account) get(Account.class, (String) hm.get("accountid"));
                billingPaymentDetailOtherwise.setAccount(account);
            }

            if (hm.containsKey("billingpayment")) {
                BillingPayment payment = hm.get("billingpayment") == null ? null : (BillingPayment) get(BillingPayment.class, (String) hm.get("billingpayment"));
                billingPaymentDetailOtherwise.setBillingPayment(payment);
            }
            if (hm.containsKey("tax")) {
                Tax tax = hm.get("tax") == null ? null : (Tax) get(Tax.class, (String) hm.get("tax"));
                billingPaymentDetailOtherwise.setTax(tax);
            }
            if (hm.containsKey("isdebit")) {
                billingPaymentDetailOtherwise.setIsdebit(Boolean.parseBoolean(hm.get("isdebit").toString()));
            }
            saveOrUpdate(billingPaymentDetailOtherwise);
            list.add(billingPaymentDetailOtherwise);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.savePayment : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "BillingPayment has been updated successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject getBillingPaymentDetailOtherwise(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from BillingPaymentDetailOtherwise ";
        return buildNExecuteQuery( query, requestParams);
    }

    @Override
    public KwlReturnObject deletePaymentsDetailsOtherwise(String paymentid) throws ServiceException {
        String delQuery = "delete from PaymentDetailOtherwise pd where pd.payment.ID=? ";
        int numRows = executeUpdate( delQuery, new Object[]{paymentid});
        return new KwlReturnObject(true, "Payment Details has been deleted successfully.", null, null, numRows);
    }
    
    public KwlReturnObject deletePaymentPermanent(HashMap<String, Object> requestParams) throws AccountingException , ServiceException {
        String delQuery = "", delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuery4 = "", delQuery5 = "", delQuery6, delQuery7 = "", delQuery8 = "", delQuery9 = "", delQuery12 = "", delQuery13 = "",delQuery15 = "",delQuery16 = "",delQuery17="",delQuery18="",delQuery19="",delQuery20="",delquery21="", delquery22="";
        String delQuery23 = "",journalentryids="",paymentno="";
        boolean isPaymentReconciledFlag =false, isDeleted = false;
        Locale locale = null; 
        int numtotal = 0, numRows12 = 0,numRows=0 , numRows2=0 , numRows3=0 , numRows4=0,numRows8=0,numRows13=0,numRows21=0, numRows22=0, numRows23=0;
        try {
            if (requestParams.containsKey("locale")) {
                locale = (Locale) requestParams.get("locale");
            }
            if (requestParams.containsKey("journalentryids")) {
                journalentryids = (String) requestParams.get("journalentryids");
            }
            if (requestParams.containsKey("paymentno")) {
                paymentno = (String) requestParams.get("paymentno");
            }
            if (requestParams.containsKey("isDeleted")) {
                isDeleted = Boolean.parseBoolean(requestParams.get("isDeleted").toString());
            }
            if (requestParams.containsKey("paymentid") && requestParams.containsKey("companyid")) {

                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("companyid"));
                params8.add(requestParams.get("paymentid"));
                String myquery = "select journalentry from payment where company =? and id = ?";
                List list = executeSQLQuery(myquery, params8.toArray());
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
                String bankchargemyquery = "select journalentryforbankcharges from payment where company =? and id = ?";
                List bnkchrgelist1 = executeSQLQuery(bankchargemyquery, params8.toArray());
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
                String bankinterestmyquery = "select journalentryforbankinterest from payment where company =? and id = ?";
                List bnkintrstlist1 = executeSQLQuery(bankinterestmyquery, params8.toArray());
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
                                
                String importServiceJe = "select importserviceje from payment where company =? and id = ?";
                List importServiceJEList = executeSQLQuery(importServiceJe, params8.toArray());
                Iterator importServiceJEItr = importServiceJEList.iterator();
                String importServiceJEListIdStrings = "";
                while (importServiceJEItr.hasNext()) {
                    Object idobj = importServiceJEItr.next();
                    String id = (idobj != null) ? idobj.toString() : "";
                    importServiceJEListIdStrings += "'" + id + "',";
                }
                if (!StringUtil.isNullOrEmpty(importServiceJEListIdStrings)) {
                    importServiceJEListIdStrings = importServiceJEListIdStrings.substring(0, importServiceJEListIdStrings.length() - 1);
                }
                
                ArrayList params5 = new ArrayList();
                params5.add(requestParams.get("companyid"));
                params5.add(requestParams.get("companyid"));
                params5.add(requestParams.get("paymentid"));
                delQuery5 = "delete from accjedetailcustomdata where jedetailId in (select id from jedetail where company =? and journalEntry in (select journalentry from payment where company =? and id =?))";

                int numRows5 = executeSQLUpdate(delQuery5, params5.toArray());


                ArrayList params9 = new ArrayList();
                params9.add(requestParams.get("companyid"));
                params9.add(requestParams.get("paymentid"));
                String myquery1 = "select paydetail from payment where company = ? and id=?";
                List list1 = executeSQLQuery(myquery1, params9.toArray());
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
                List chequeList=new ArrayList();
                String chequeQuery = "select cheque from paydetail where id in(" + journalent + ")";
                if(!StringUtil.isNullOrEmpty(journalent)){
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
                params1.add(requestParams.get("paymentid"));
//                delQuery1 = "delete from paymentdetail where payment in (select id from payment where company =? and id =?)";
                delQuery1 = "delete pd from paymentdetail pd inner join payment p on pd.payment=p.id where p.company =? and p.id =?";
                int numRows1 = executeSQLUpdate(delQuery1, params1.toArray());

                /*
                    update receipt amountdue on refund payment
                */
                ArrayList paramsRefund = new ArrayList();
                paramsRefund.add(requestParams.get("paymentid"));
                delQuery15 = "update receiptadvancedetail recadv inner join advancedetail payadv on payadv.receiptadvancedetail = recadv.id "
                        + " inner join payment paym on paym.id = payadv.payment "
                        + "set recadv.amountdue = (recadv.amountdue + (payadv.amount/payadv.exchangeratefortransaction)) "
                        + "where paym.id = ?";
                int approvalStatusLevel = 0;
                if (requestParams.containsKey("approvalStatusLevel")) {
                    approvalStatusLevel = Integer.parseInt(requestParams.get("approvalStatusLevel").toString());
                }
                if (approvalStatusLevel == Constants.APPROVED_STATUS_LEVEL && !isDeleted) {
                    int numparamsRefundRows = executeSQLUpdate(delQuery15, paramsRefund.toArray());
                }
                
                
                ArrayList params15 = new ArrayList();
                params15.add(requestParams.get("companyid"));
                params15.add(requestParams.get("paymentid"));
//                delQuery15 = "delete from advancedetail where payment in (select id from payment where company =? and id =?)";
                delQuery15 = "delete advd from advancedetail advd inner join payment p on advd.payment=p.id where p.company =? and p.id =?";
                int numRows15 = executeSQLUpdate(delQuery15, params15.toArray());
          
                ArrayList params16 = new ArrayList();
                params16.add(requestParams.get("companyid"));
                params16.add(requestParams.get("paymentid"));
//                delQuery16 = "delete from linkdetailpayment where payment in (select id from payment where company =? and id =?)";
                delQuery16 = "delete ldp from linkdetailpayment ldp inner join payment p on ldp.payment=p.id where p.company =? and p.id =?";
                int numRows16 = executeSQLUpdate(delQuery16, params16.toArray());
          
                ArrayList params7 = new ArrayList();
                params7.add(requestParams.get("companyid"));
                params7.add(requestParams.get("paymentid"));
//                delQuery7 = "delete from paymentdetailotherwise where payment in (select id from payment where company =? and id =?)";
                delQuery7 = "delete pdo from paymentdetailotherwise pdo inner join payment p on pdo.payment = p.id where p.company =? and p.id =?";
                int numRows7 = executeSQLUpdate(delQuery7, params7.toArray());

                ArrayList params24 = new ArrayList();
                String delQuery24 = "delete from openingbalancemakepaymentcustomdata where openingbalancemakepaymentid=? ";
                params24.add(requestParams.get("paymentid"));
                int numRows24 = executeSQLUpdate(delQuery24, params24.toArray());
                
                ArrayList params6 = new ArrayList();
                params6.add(requestParams.get("companyid"));
                params6.add(requestParams.get("paymentid"));
                delQuery6 = "update payment set advanceid=null where company =? and advanceid = ? ";
                int numRows6 = executeSQLUpdate(delQuery6, params6.toArray());
                delQuery6 = "delete from payment where company =? and id = ?";
                numRows6 = executeSQLUpdate(delQuery6, params6.toArray());

                ArrayList params2 = new ArrayList();
                delQuery2 = "delete from paydetail where id in(" + journalent + ")";
                if(!StringUtil.isNullOrEmpty(journalent)){
                    numRows2 = executeSQLUpdate(delQuery2, params2.toArray());
                }

                int chequeDelRows = 0;
                
                if (!StringUtil.isNullOrEmpty(chequeIds)) {
                    String chequeDelQuery = "delete from cheque where id in(" + chequeIds + ")";
                    chequeDelRows = executeSQLUpdate(chequeDelQuery);
                }

                ArrayList params10 = new ArrayList();
                delQuery8 = "delete from paymentmethod where id in(select paymentMethod from paydetail where id in(" + journalent + "))";
                if(!StringUtil.isNullOrEmpty(journalent)){
                    numRows8 = executeSQLUpdate(delQuery8, params10.toArray());
                }


                ArrayList params3 = new ArrayList();
                params3.add(requestParams.get("companyid"));
                delQuery3 = "delete from jedetail where company = ? and journalEntry in (" + idStrings + ") ";
                if(!StringUtil.isNullOrEmpty(idStrings)){
                    numRows3 = executeSQLUpdate(delQuery3, params3.toArray());
                }
                // delete query for bank charges JE
                delQuery17 = "delete from jedetail where company = ? and journalEntry in (" + bankchargeidStrings + ") ";
                if (!StringUtil.isNullOrEmpty(bankchargeidStrings)) {
                    numRows3 = executeSQLUpdate(delQuery17, params3.toArray());
                }
                // delete query for bank interest JE
                delQuery19 = "delete from jedetail where company = ? and journalEntry in (" + bankinterestidStrings + ") ";
                if (!StringUtil.isNullOrEmpty(bankinterestidStrings)) {
                    numRows23 = executeSQLUpdate(delQuery19, params3.toArray());
                }
                
                delQuery23 = "delete from jedetail where company = ? and journalEntry in (" + importServiceJEListIdStrings + ") ";
                if (!StringUtil.isNullOrEmpty(importServiceJEListIdStrings)) {
                    numRows23 = executeSQLUpdate(delQuery23, params3.toArray());
                }
                 List list3=new ArrayList();
                ArrayList params12 = new ArrayList();
                params12.add(requestParams.get("companyid"));
                String myquery2 = "select bankReconciliation from bankreconciliationdetail where journalEntry in (" + idStrings + ") and company=?";
                if(!StringUtil.isNullOrEmpty(idStrings)){
                    list3 = executeSQLQuery(myquery2, params12.toArray());
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
//                ArrayList params14 = new ArrayList();
//                params14.add(requestParams.get("companyid"));
//                delQuery12 = "delete from bankreconciliationdetail where bankreconciliation in (" + bankrec + ") and company =?";
//                if(!StringUtil.isNullOrEmpty(bankrec)){
//                    numRows13 = executeSQLUpdate(delQuery12, params14.toArray());
//                }
                /**
                 * delete unconciled records in case of permanent delete.
                 */
                accBankReconciliationObj.deleteUnReconciliationRecords(requestParams);
                //deleteUnReconciliationPayments(requestParams);
                ArrayList params14 = new ArrayList();
                params14.add(requestParams.get("companyid"));
                delQuery12 = "delete from bankreconciliationdetail where journalEntry in (" + idStrings + ") and company =?";
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    numRows13 = executeSQLUpdate(delQuery12, params14.toArray());
                }
                
                ArrayList params18 = new ArrayList();
                params18.add(requestParams.get("companyid"));
                delquery22 = "delete from bankunreconciliationdetail where journalEntry in (" + idStrings + ") and company = ?";
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    numRows22 = executeSQLUpdate(delquery22, params18.toArray());
                }
                
                if (!StringUtil.isNullOrEmpty(bankrec)) {
                    
                    ArrayList params17 = new ArrayList();
                    params17.add(requestParams.get("companyid"));
                    delquery21= "delete from bankreconciliationdetail where bankReconciliation in ("+ bankrec + ") and company = ?";
                    numRows21 = executeSQLUpdate(delquery21, params17.toArray());
                    
                    ArrayList params13 = new ArrayList();
                    params13.add(requestParams.get("companyid"));
                    delQuery13 = "delete from bankreconciliation where id in (" + bankrec + ") and company =?";
                    numRows12 = executeSQLUpdate(delQuery13, params13.toArray());
                }

                // DELETE THE ENTRIES FOR THE BANK CHARGES AND INTREST FROM RECONCIALTION
               deleteBankRconcialtionEntries(bankchargeidStrings,requestParams);
               deleteBankRconcialtionEntries(bankinterestidStrings,requestParams);

                ArrayList params4 = new ArrayList();
                delQuery4 = "delete from journalentry where id  in (" + idStrings + ")";
                if(!StringUtil.isNullOrEmpty(idStrings)){
                    numRows4 = executeSQLUpdate(delQuery4, params4.toArray());
                }
                // delete query for bank charges JE
                delQuery18 = "delete from journalentry where id  in (" + bankchargeidStrings + ")";
                if (!StringUtil.isNullOrEmpty(bankchargeidStrings)) {
                    numRows4 = executeSQLUpdate(delQuery18, params4.toArray());
                }
                // delete query for bank interest JE
                delQuery20 = "delete from journalentry where id  in (" + bankinterestidStrings + ")";
                if (!StringUtil.isNullOrEmpty(bankinterestidStrings)) {
                    numRows4 = executeSQLUpdate(delQuery20, params4.toArray());
                }
                
                delQuery23 = "delete from journalentry where id  in (" + importServiceJEListIdStrings + ")";
                if (!StringUtil.isNullOrEmpty(importServiceJEListIdStrings)) {
                    numRows23 = executeSQLUpdate(delQuery23, params4.toArray());
                }
                String paymentid = (String) requestParams.get("paymentid");
//                KwlReturnObject result1 = getpaymenthistory(paymentid);
//                List ls = result1.getEntityList();
//                Iterator<Object[]> itr2 = ls.iterator();
//                while (itr2.hasNext()) {
//                    Object[] row = (Object[]) itr2.next();
//                    String cnid = row[0].toString();
//                    Double amount = Double.parseDouble(row[1].toString());
//                    KwlReturnObject cnidresult = updateCnUpAmount(cnid, amount);
//                    KwlReturnObject opencnidresult = updateCnOpeningAmountDue(cnid, amount);
//                }
                
//                KwlReturnObject cnhistoryresult = getVendorCnPaymenyHistory("", 0.0, 0.0, paymentid);
//                    List<CreditNotePaymentDetails> cnHistoryList = cnhistoryresult.getEntityList();
//                    for (CreditNotePaymentDetails cnpd:cnHistoryList) {
//                        String cnnoteid = cnpd.getCreditnote().getID()!=null?cnpd.getCreditnote().getID():"";
//                        Double cnpaidamount = cnpd.getAmountPaid();
//                        Double cnPaidAmountInBaseCurrency =cnpd.getAmountInBaseCurrency();
//                        KwlReturnObject cnjedresult = updateCnAmount(cnnoteid, -cnpaidamount);
//                        KwlReturnObject opencnjedresult = updateCnOpeningAmountDue(cnnoteid, -cnpaidamount);
//                        KwlReturnObject openingCnBaseAmtDueResult = accPaymentDAOobj.updateCnOpeningBaseAmountDue(cnnoteid,-cnPaidAmountInBaseCurrency);
//                        if(cnpd.getCreditnote()!=null && !StringUtil.isNullOrEmpty(cnpd.getCreditnote().getRevalJeId())){
//                            deleteJEDtails(cnpd.getCreditnote().getRevalJeId(),(String)requestParams.get("companyid"));
//                            deleteJEEntry(cnpd.getCreditnote().getRevalJeId(),(String)requestParams.get("companyid"));
//                        }
//                    }
                ArrayList params11 = new ArrayList();
                params11.add(requestParams.get("paymentid"));
                delQuery9 = " delete from creditnotpayment where paymentid=?";
                int numRows9 = executeSQLUpdate(delQuery9, params11.toArray());

                ArrayList params = new ArrayList();
                delQuery = "delete  from accjecustomdata where journalentryId in(" + idStrings + ") ";
                if(!StringUtil.isNullOrEmpty(idStrings)){
                    numRows = executeSQLUpdate(delQuery, params.toArray());
                }

                numtotal = numRows + numRows2 + numRows3 + numRows4 + numRows5 + numRows6 + numRows1 + numRows7 + numRows8 + numRows9 + numRows12 + numRows13 +numRows15 +numRows16+numRows21;
            }

            return new KwlReturnObject(true, "Payment has been deleted successfully.", null, null, numtotal);
//        }catch (AccountingException ex) {
//            throw new AccountingException(ex.getMessage());
        }catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Payment as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
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
    @Override
    public KwlReturnObject deleteBillingPaymentsDetailsOtherwise(String paymentid) throws ServiceException {
        String delQuery = "delete from BillingPaymentDetailOtherwise pd where pd.billingPayment.ID=?";
        int numRows = executeUpdate( delQuery, new Object[]{paymentid});
        return new KwlReturnObject(true, "Payment Details has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject getpaymenthistory(String paymentid) throws ServiceException {
        String query = "select cnid,amountpaid from creditnotpayment where paymentid=? ";
        ArrayList params = new ArrayList();
        params.add(paymentid);
//        List list = executeQuery( query, params.toArray());
        List listSql = executeSQLQuery(query, params.toArray());
        int count = listSql.size();
        return new KwlReturnObject(true, "", "", listSql, count);
    }

    @Override
    public KwlReturnObject updateCnUpAmount(String noteid, Double amount) throws ServiceException {
        String delQuery = "update CreditNote set cnamountdue=(cnamountdue+?) where ID=?";
        int numRows = executeUpdate( delQuery, new Object[]{amount, noteid});
//        List list = executeSQLQuery(delQuery, finalParam.toArray());
        return new KwlReturnObject(true, "Payment Method has been added successfully.", null, null, numRows);
    }

    public KwlReturnObject updateCnOpeningAmountDue(String noteid, Double amount) throws ServiceException {
        String delQuery = "update CreditNote set openingBalanceAmountDue=(openingBalanceAmountDue+?) where ID=?";
        int numRows = executeUpdate( delQuery, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "CN Amount has been updated successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject getDuplicatePNforNormal(String entryNumber, String companyid, String receiptid, String advanceId,Payment payment) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = " ";
        params.add(companyid);
        params.add(receiptid);
        if (!StringUtil.isNullOrEmpty(entryNumber)) {
            condition += " and paymentNumber=?";
            params.add(entryNumber);
        }
        if(payment!=null&&!StringUtil.isNullOrEmpty(payment.getCndnAndInvoiceId())){
             condition+=" and ID!=?";
             params.add(payment.getCndnAndInvoiceId());
        }
        
        if(!StringUtil.isNullOrEmpty(advanceId)){
             condition+=" and ID!=?";
             params.add(advanceId);
        }else{
            List<Payment> payments=find("from Payment where company.companyID='"+companyid+"' and advanceid.ID='"+receiptid+"'");
            if(!payments.isEmpty()){
                condition+=" and ID NOT IN (";
                for(Payment paymentObj:payments){
                    condition+= "'"+paymentObj.getID()+"',";
                }
                condition=condition.substring(0,condition.lastIndexOf(","));
                condition+= ")";
        }
        String q = "from Payment where company.companyID=? and ID!=? " + condition;
        list = executeQuery( q, params.toArray());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

        @Override
    public KwlReturnObject getCurrentSeqNumberForAdvance(String sequenceformat, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "select max(seqnumber) from payment where seqformat=? and  company =? ";
        list = executeSQLQuery(q, new Object[]{sequenceformat, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());

    }
    
    public String getDocumentNumbersUsedInPayment(Payment payment) {
        String usedDocumentNumbers = "";

        //Vendor Invoice number
        if (!payment.getRows().isEmpty()) {
            Set<PaymentDetail> rows = payment.getRows();
            for (PaymentDetail detail : rows) {
                if (detail.getGoodsReceipt() != null) {
                    usedDocumentNumbers += detail.getGoodsReceipt().getGoodsReceiptNumber() + ", ";
                }
            }
        }

        //Credit Note number
        if (!payment.getCreditNotePaymentDetails().isEmpty()) {
            Set<CreditNotePaymentDetails> creditNotePaymentDetails = payment.getCreditNotePaymentDetails();
            for (CreditNotePaymentDetails details : creditNotePaymentDetails) {
                if (details.getCreditnote() != null) {
                    usedDocumentNumbers += details.getCreditnote().getCreditNoteNumber() + ", ";
                }
            }
        }

        //Account code or number
        if (!payment.getPaymentDetailOtherwises().isEmpty()) {
            Set<PaymentDetailOtherwise> paymentDetailOtherwises = payment.getPaymentDetailOtherwises();
            for (PaymentDetailOtherwise detailOthrwise : paymentDetailOtherwises) {
                if (detailOthrwise.getAccount() != null) {
                    String acccode = detailOthrwise.getAccount().getAcccode() == null ? detailOthrwise.getAccount().getAccountName() : detailOthrwise.getAccount().getAcccode();
                        usedDocumentNumbers += acccode + ", ";
                    }
                }
            }
        
        //Refund Receipt Number
        if (!StringUtil.isNullOrEmpty(payment.getCustomer()) && !payment.getAdvanceDetails().isEmpty()) {
            Set<AdvanceDetail> advanceDetails = payment.getAdvanceDetails();
            for (AdvanceDetail advanceDetail : advanceDetails) {
                if (advanceDetail.getReceiptAdvanceDetails() != null && advanceDetail.getReceiptAdvanceDetails().getReceipt()!=null) {
                    usedDocumentNumbers += advanceDetail.getReceiptAdvanceDetails().getReceipt().getReceiptNumber() + ", ";
                }
            }
        }

        //Linked Vendor Invoice Number
        if (!payment.getLinkDetailPayments().isEmpty()) {
            Set<LinkDetailPayment> linkDetailPayments = payment.getLinkDetailPayments();
            for (LinkDetailPayment detailPayment : linkDetailPayments) {
                if (detailPayment.getGoodsReceipt() != null) {
                    usedDocumentNumbers += detailPayment.getGoodsReceipt().getGoodsReceiptNumber() + ", ";
                }
            }
        }

        //Linked Credit Note Number
        if (!payment.getLinkDetailPaymentToCreditNote().isEmpty()) {
            Set<LinkDetailPaymentToCreditNote> linkDetailPaymentToCreditNote = payment.getLinkDetailPaymentToCreditNote();
            for (LinkDetailPaymentToCreditNote detail : linkDetailPaymentToCreditNote) {
                if (detail.getCreditnote() != null) {
                    usedDocumentNumbers += detail.getCreditnote().getCreditNoteNumber() + ", ";
                }
            }
        }
        
        usedDocumentNumbers=usedDocumentNumbers.trim();
        if(usedDocumentNumbers.endsWith(",")){
          usedDocumentNumbers=usedDocumentNumbers.substring(0, usedDocumentNumbers.length()-1);  
        }
        
        return usedDocumentNumbers;
    }
    
    @Override
    public KwlReturnObject getPaymentEditCount(String entryNumber, String companyid, String paymentId) throws ServiceException {
        try {
            List list = new ArrayList();
            int count = 0;
            String query = "from Payment where paymentNumber=? and company.companyID=? and ID!=?";
            list = executeQuery( query, new Object[]{entryNumber, companyid, paymentId});
            count = list.size();
            return new KwlReturnObject(true, "", null, list, count);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderImpl.updateQAApprovalItems:" + ex.getMessage(), ex);
        }
    }

    
    
    public double getPaymentAmountDueNew(Payment payment) {
        Iterator itrRow = payment.getRows().iterator();
        Iterator itrOtherwise = payment.getPaymentDetailOtherwises().iterator();
        double amount = 0, totaltaxamount = 0, linkedAmountDue = payment.getDepositAmount();
        if (!payment.getRows().isEmpty()) {
            while (itrRow.hasNext()) {
                amount += ((PaymentDetail) itrRow.next()).getAmount();
            }
        }
        if (!payment.getPaymentDetailOtherwises().isEmpty()) {
            while (itrOtherwise.hasNext()) {
                amount += ((PaymentDetailOtherwise) itrOtherwise.next()).getAmount();
            }
        }
        KwlReturnObject cndnResult;
        try {
            cndnResult = getVendorCnPayment(payment.getID());
            List<CreditNotePaymentDetails> cnpdList = cndnResult.getEntityList();
            for (CreditNotePaymentDetails cnpd :cnpdList) {
                Double cnPaidAmountPaymentCurrency = cnpd.getPaidAmountInPaymentCurrency();
                amount += cnPaidAmountPaymentCurrency;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        linkedAmountDue = (payment.getDepositAmount()-payment.getBankInterestAmount()-payment.getBankChargesAmount()) - amount;
        return linkedAmountDue;
    }

    @Override
    public List<Payment> getPaymentList(String payments) throws ServiceException {
        String q = "from Payment where ID in ("+payments+")";
        return find(q);
    }
    
    @Override
    public List<Payment> getPaymentListFromCompany(String companyid) throws ServiceException {
        String q = "from Payment where company = '"+companyid+"'";
        return find(q);
    }
    
    @Override
    public List getAdvancePaymentUsedInRefundReceipt(String advancedetailid) throws ServiceException {
        List list = new ArrayList(); 
        try {
            String query = "select r.receiptnumber, r.currency, r.id from receiptadvancedetail recadv inner join receipt r on r.id = recadv.receipt where recadv.advancedetailid= ?";
            list = executeSQLQuery(query, new Object[]{advancedetailid});
        } catch(Exception ex) {
            throw ServiceException.FAILURE("getAdvancePaymentUsedInRefundReceipt:" + ex.getMessage(), ex);
        }
        return list;
    }
    
    @Override
    public List getAdvanceReceiptUsedInRefundPayment(String advancedetailid) throws ServiceException {
        List list = new ArrayList(); 
        try {
            String query = "select r.receiptnumber, r.currency, r.id from receiptadvancedetail recadv inner join receipt r on r.id = recadv.receipt where recadv.id=?";
            list = executeSQLQuery(query, new Object[]{advancedetailid});
        } catch(Exception ex) {
            throw ServiceException.FAILURE("getAdvanceReceiptUsedInRefundPayment:" + ex.getMessage(), ex);
        }
        return list;
    }
    
    @Override
    public KwlReturnObject getPaymentAdvanceAmountDueDetails(HashMap<String, Object> request) throws ServiceException {
        //Delete Payment Details
        List list = null;
        int count = 0;
        try {
            String start = (String) request.get("start");
            String limit = (String) request.get("limit");
            String ss = (String) request.get("ss");
            String customerid = (String) request.get("vendorid");
            if (customerid == null) {
                customerid = (String) request.get("accid");
            }
            String companyid = (String) request.get("companyid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            String condition = " where p.company.companyID=? and p.approvestatuslevel=11";      
            
            /*
                Fetch receipts which have advance payment with amountdue > 0 
            */
            params.add(0d);
            condition += " and ad.amountDue > ? ";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"ac.name", "p.paymentNumber", "p.journalEntry.entryNumber", "p.memo", "p.paidTo.value", "chk.chequeNo"};  //added quick serch on the basis of paid to
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 6);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                searchQuery = searchQuery.substring(0, searchQuery.lastIndexOf(")"));   //removing last ')' to add to following customer and vendor search             
                searchQuery += " or (p.customer in (select ID from Customer where name like ?)) or (p.vendor in (select ID from Vendor where name like ?)))";
                params.add("%" + ss + "%");
                params.add("%" + ss + "%");
                condition += searchQuery;
            }
            
             if (!StringUtil.isNullOrEmpty(customerid)) {
                    params.add(customerid);
                    condition += " and p.vendor.ID=?";
             }
             /*
              * If non refundable check is true then in cross linking non refundable payment not shown in Advance payment window.
              * It's only for malaysian company
              */
                params.add(false);
                condition += " and p.nonRefundable=? ";
            
            /*
             * ERP-40387
             * documents marked as dishonoured also getting used to link transaction case
             * e.g. created MP advanced and marked it dishonoured then also the user can able to link that document to RP refund both internally(i.e. while creating RP refund) and externally(i.e. through 'link transaction' button)  
             * therefore only those documents are fetched having isDishonouredCheque as false
             */    
            params.add(false);
            condition += " and p.isDishonouredCheque=?";   
            
            /*
             * ERP-40513
             * e.g. Create RP advance in Pound
             * While creating MP refund take payment method
             * Change payment currency to USD
             * Now only USD currency documents loading
             */
            if (request.containsKey("currencyfilterfortrans") && !StringUtil.isNullObject(request.get("currencyfilterfortrans")) && !request.containsKey("isReceipt")) {
                condition += " and ad.payment.currency.currencyID = ?";
                params.add(request.get("currencyfilterfortrans"));
            }
                
            String query = "select distinct ad from AdvanceDetail ad inner join ad.payment p inner join p.journalEntry je inner join je.details jed inner join jed.account ac left join p.paidTo pt left join p.payDetail pd left join pd.cheque chk "+ condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
            
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
        
    }
                // delete Bank charges JE
        public KwlReturnObject getBankChargeJEFromPayment(String paymentid) throws ServiceException {
        List list = new ArrayList();
        String query = "select p.journalEntryForBankCharges.ID from Payment p where p.ID=? and p.company.companyID=p.journalEntryForBankCharges.company.companyID";
        list = executeQuery( query, new Object[]{paymentid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
                    // delete Bank charges JE
        public KwlReturnObject getBankInterestJEFromPayment(String paymentid) throws ServiceException {
        List list = new ArrayList();
        String query = "select p.journalEntryForBankInterest.ID from Payment p where p.ID=? and p.company.companyID=p.journalEntryForBankInterest.company.companyID";
        list = executeQuery( query, new Object[]{paymentid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    public List<LinkDetailPayment> getDeletedLinkedPaymentInvoices(Payment payment,List<String> linkedDetailInvoice, String companyid) throws ServiceException {
        String invoicelinkIDsVal = "";
        String receiptid = payment.getID();
        for(String invID : linkedDetailInvoice) {
            invoicelinkIDsVal = invoicelinkIDsVal.concat("'").concat(invID).concat("',");
        }
        if(!StringUtil.isNullOrEmpty(invoicelinkIDsVal.toString())) {
            invoicelinkIDsVal = invoicelinkIDsVal.substring(0, invoicelinkIDsVal.length()-1);
        }
        String selQuery = "from LinkDetailPayment pd where pd.payment.ID=? and pd.company.companyID=? ";
        if(!StringUtil.isNullOrEmpty(invoicelinkIDsVal)) {
           selQuery = selQuery.concat(" and pd.id not in (" + invoicelinkIDsVal + ")");
        }
        List<LinkDetailPayment> details = find(selQuery, new Object[]{receiptid, companyid});
        return details;
    }
            
    public KwlReturnObject deleteSelectedLinkedPaymentInvoices(String paymentid, String linkedDetailIDs, String companyid, String unlinkedDetailIDs) throws ServiceException {
        String delQuery = "delete from LinkDetailPayment brd where brd.payment.ID=? and brd.company.companyID=?";
        if(!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
           delQuery = delQuery.concat(" and brd.id not in (" + linkedDetailIDs + ")");
        }
        if(!StringUtil.isNullOrEmpty(unlinkedDetailIDs)) {
           delQuery = delQuery.concat(" and brd.id in (" + unlinkedDetailIDs + ")");
        }
        int numRows = executeUpdate( delQuery, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Payment linked invoices have been deleted successfully", null, null, numRows);
    }
    
    @Override
    public List<PaymentDetailOtherwise> getPaymentDetailOtherwise(String paymentid) throws ServiceException {
        String query = "from PaymentDetailOtherwise where payment.ID = ? ";
        List<PaymentDetailOtherwise> detailsList = executeQuery( query, paymentid);
        return detailsList;
    }

    @Override
    public List<AdvanceDetail> getPaymentDetailAdvanced(String paymentid) throws ServiceException {
        String query = "from AdvanceDetail where payment.ID = ? ";
        List<AdvanceDetail> detailsList = executeQuery( query, paymentid);
        return detailsList;
    }
    /**
     * Description : Below Method is used to add DefaultTaxDetails default
     * company setup
     * @param <defaultCompSetupMap> used to get common setup parameters 
     * @param <preferences> used to get default Account from
     * CompanyAccountPreferences
     * @param <accounthm> used to get account
     * @return :void
     */
    @Override
    public List<AdvanceDetail> getAdvanceDetailsAgainstVendorForTDS(HashMap<String, Object> requestParams) throws ServiceException {
        //To Fetch Advance Payment Detail made against Given Vendor.
        List<AdvanceDetail> detailsList = new ArrayList();
        try {
            List params = new ArrayList();
            String subquery = "";
            if (requestParams.containsKey("companyid") && !StringUtil.isNullOrEmpty((String) requestParams.get("companyid"))) {
                if (StringUtil.isNullOrEmpty(subquery)) {
                    subquery += " company.companyID = ? ";
                } else {
                    subquery += " AND company.companyID = ? ";
                }
                params.add((String) requestParams.get("companyid"));
            }
            if (requestParams.containsKey("vendorid") && !StringUtil.isNullOrEmpty((String) requestParams.get("vendorid"))) {
                if (StringUtil.isNullOrEmpty(subquery)) {
                    subquery += " payment.vendor.ID = ? ";
                } else {
                    subquery += " AND payment.vendor.ID = ? ";
                }
                params.add((String) requestParams.get("vendorid"));
            }
            if (requestParams.containsKey("billDate") && requestParams.get("billDate") != null) {
                if (StringUtil.isNullOrEmpty(subquery)) {
//                    subquery += " payment.journalEntry.entryDate <= ? ";
                    subquery += " payment.creationDate <= ? ";
                } else {
//                    subquery += " AND payment.journalEntry.entryDate <= ? ";
                    subquery += " AND payment.creationDate <= ? ";
                }
                params.add((Date) requestParams.get("billDate"));
            }
            subquery +=" AND istdsamountusedingoodsreceipt = 'F' AND tdsamount > 0 ";//Those Advance Payments whose TDS Amount is not adjusted & TDS Amount > 0.
            String query = "from AdvanceDetail ";
            if (!StringUtil.isNullOrEmpty(subquery)) {
                query += " WHERE " + subquery;
            }
            detailsList = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getAdvanceDetailsAgainstVendorForTDS:" + ex.getMessage(), ex);
        }
        return detailsList;
    }
    
    @Override
    public List<String> getTotalJEDIDPaymentDetailAdvanced(String paymentid,String companyid) throws ServiceException {
//        String query = "select totalJED.ID from AdvanceDetail where payment.company.companyID=? and payment.ID = ? and totalJED is not null ";
        String query = "select totalJED.ID from AdvanceDetail where payment.ID = ? and totalJED is not null ";
        List<String> detailsList = executeQuery( query,paymentid);
        return detailsList;
    }

    @Override
    public List<PayDetail> getPaymentDetails(String companyid) throws ServiceException {
        String query = "from PayDetail where company.companyID = ? ";
        List<PayDetail> detailsList = executeQuery( query, companyid);
        return detailsList;
    }
    
    @Override
    public List getMulticurrencyPaymentsWithPCToPMCRateOne(String companyId) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select p.id as paymentid,p.paymentnumber as paymentnumber,p.company as companyid,co.subdomain "
                    + "as companysubdomain,c.currencyid as currencyid from payment p "
                    + "inner join paydetail pd on pd.id=p.paydetail "
                    + "inner join paymentmethod pm on pd.paymentmethod=pm.id "
                    + "inner join account a on a.id=pm.account "
                    + "inner join currency c on c.currencyid=a.currency "
                    + "inner join company co on p.company=co.companyid "
                    + "where p.paymentcurrencytopaymentmethodcurrencyrate=1 and p.currency<>c.currencyid and p.deleteflag='F' "
                    + "and co.companyid=?";
            list = executeSQLQuery(query, new Object[]{companyId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getMulticurrencyPaymentsWithPCToPMCRateOne:"+ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public KwlReturnObject getRepeatePaymentDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String parentPaymentId = (String) requestParams.get("parentPaymentId");
        String query = "from Payment where parentPayment.ID = ? ";
        List list = executeQuery( query, new Object[]{parentPaymentId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveRepeatMPInfo(HashMap<String, Object> dataMap) throws ServiceException {        
        List list = new ArrayList();
        try {
            RepeatedPayment rMP = new RepeatedPayment();
            if (dataMap.containsKey("id")) {
                rMP = (RepeatedPayment) get(RepeatedPayment.class, (String) dataMap.get("id"));
            }

            if (dataMap.containsKey("intervalType")) {
                rMP.setIntervalType((String) dataMap.get("intervalType"));
            }
            if (dataMap.containsKey("intervalUnit")) {
                rMP.setIntervalUnit((Integer) dataMap.get("intervalUnit"));
            }
            if (dataMap.containsKey("NoOfPaymentpost")) {
                rMP.setNoOfpaymentspost((Integer) dataMap.get("NoOfPaymentpost"));
            }
            if (dataMap.containsKey("NoOfRemainPaymentpost")) {
                rMP.setNoOfRemainpaymentspost((Integer) dataMap.get("NoOfRemainPaymentpost"));
            }
            if (dataMap.containsKey("startDate")) {
                rMP.setStartDate((Date) dataMap.get("startDate"));
            }
            if (dataMap.containsKey("nextDate")) {
                rMP.setNextDate((Date) dataMap.get("nextDate"));
            }
            if (dataMap.containsKey("expireDate")) {
                rMP.setExpireDate((Date) dataMap.get("expireDate"));
            }
            if (dataMap.containsKey("isactivate")) {
                rMP.setIsActivate((Boolean)dataMap.get("isactivate"));
            }             
            if (dataMap.containsKey("ispendingapproval")) {
                rMP.setIspendingapproval((Boolean)dataMap.get("ispendingapproval"));
            }
            if (dataMap.containsKey("approver")) {
                rMP.setApprover((String) dataMap.get("approver"));
            }
            if (dataMap.containsKey("prevDate")) {
                rMP.setPrevDate((Date) dataMap.get("prevDate"));
            }
            if (dataMap.containsKey("autoGenerateChequeNumber")) {
                rMP.setAutoGenerateChequeNumber((Boolean) dataMap.get("autoGenerateChequeNumber"));
            }
            
            saveOrUpdate(rMP);
            list.add(rMP);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveRepeateInvoiceInfo : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    
    }

    @Override
    public KwlReturnObject activateDeactivatePayment(String repeateid, boolean isactivate) throws ServiceException {
        RepeatedPayment rmp = null;
        try {
            rmp = (RepeatedPayment) get(RepeatedPayment.class, repeateid);
            rmp.setIsActivate(!isactivate);
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return new KwlReturnObject(true, "Recurring Payment has been updated successfully.", null, null, 0);

    }

    @Override
    public int DelRepeateJEMemo(String repeateid, String column) throws ServiceException {
        String query = "delete from RepeatedJEMemo RM where RM." + column + "= ? ";
        int numRows = executeUpdate( query, new Object[]{repeateid});
        return numRows;
    }

    @Override
    public KwlReturnObject getMPCount(String mpno, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from Payment where paymentNumber=? and company.companyID=?";
        list = executeQuery( q, new Object[]{mpno, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getRepeatPayment(HashMap<String, Object> requestParams) throws ServiceException {
        Date currentDate = new Date();
        String query = "from Payment where repeatedPayment is not null and (repeatedPayment.isActivate=true ) and repeatedPayment.startDate<=now() and repeatedPayment.nextDate <= ? and (repeatedPayment.expireDate is null or repeatedPayment.expireDate >= ?)";
        List list = executeQuery( query, new Object[]{currentDate, currentDate});
        return new KwlReturnObject(true, "", null, list, list.size());
    }    
    
    public KwlReturnObject savePaymentDetailOtherwise(List<PaymentDetailOtherwise> pdoList) throws ServiceException {        
        try {
            saveAll(pdoList);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.savePaymentDetailOtherwise : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Payment Detail Otherwise has been updated successfully", null, pdoList, pdoList.size());
    }

    @Override
    public KwlReturnObject getRepeatePaymentNo(Date prevDate) throws ServiceException {    
        String query = "FROM Payment WHERE repeatedPayment is not null and (repeatedPayment.isActivate=true ) and repeatedPayment.prevDate = ?";
        List list = executeQuery( query, new Object[]{prevDate});
        return new KwlReturnObject(true, "", null, list, list.size());
    
    }

    @Override
    public KwlReturnObject getCnPaymenyHistory(String companyid) throws ServiceException{
         List list=new ArrayList();      
        try{
            if(!StringUtil.isNullOrEmpty(companyid)){
                String query = "From CreditNotePaymentDetails cnp where cnp.payment.company.companyID=? ";
                list =executeQuery(query, new Object[]{companyid});                
            }
        }catch(Exception ex){
            throw ServiceException.FAILURE("accVendorPaymentImpl.getCnPaymenyHistory:"+ex.getMessage(), ex);
        } 
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveCreditNotePaymentDetails(HashMap<String,Object> datamap) throws ServiceException {
        List list = new ArrayList();
        try{
            CreditNotePaymentDetails cnd = new CreditNotePaymentDetails();
            if (datamap.containsKey("id")) {
                cnd = (CreditNotePaymentDetails) get(CreditNotePaymentDetails.class, datamap.get("id").toString());
            }
            if (datamap.containsKey("srno")) {
                cnd.setSrno((Integer) datamap.get("srno"));
            }
            if (datamap.containsKey("amountDue")) {
                cnd.setAmountDue((Double) datamap.get("amountDue"));
            }
            if (datamap.containsKey("amountPaid")) {
                cnd.setAmountPaid((Double) datamap.get("amountPaid"));
            }
            if (datamap.containsKey("exchangeRateForTransaction")) {
                cnd.setExchangeRateForTransaction((Double) datamap.get("exchangeRateForTransaction"));
            }
            if (datamap.containsKey("amountInPaymentCurrency")) {
                cnd.setAmountInPaymentCurrency((Double) datamap.get("amountInPaymentCurrency"));
            }
            if (datamap.containsKey("amountInBaseCurrency")) {
                cnd.setAmountInBaseCurrency((Double) datamap.get("amountInBaseCurrency"));
            }
            if (datamap.containsKey("paidAmountDueInBaseCurrency")) {
                cnd.setPaidAmountDueInBaseCurrency((Double) datamap.get("paidAmountDueInBaseCurrency"));
            }
            if (datamap.containsKey("exchangeRateCurrencyToBase")) {
                cnd.setExchangeRateCurrencyToBase((Double) datamap.get("exchangeRateCurrencyToBase"));
            }
            if (datamap.containsKey("gstCurrencyRate")) {
                cnd.setGstCurrencyRate((Double) datamap.get("gstCurrencyRate"));
            }
            saveOrUpdate(cnd);
            list.add(cnd);
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveRepeateInvoiceInfo : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size())    ;
    }

    @Override
    public int DelRepeatePaymentChequeDetails(String repeateid) throws ServiceException {
        String query = "delete from RepeatedPaymentChequeDetail R where R.RepeatedPaymentID=?" ;
        int numRows = executeUpdate( query, new Object[]{repeateid});
        return numRows;
    }

    @Override
    public KwlReturnObject getChequeDetailsForRepeatedPayment(String repeatPaymentId) throws ServiceException {
        List list = new ArrayList();
        String query = "From RepeatedPaymentChequeDetail R where R.RepeatedPaymentID=?" ;
        list = executeQuery( query, new Object[]{repeatPaymentId});
        return new KwlReturnObject(true, "", null, list,list.size());
    }

    @Override
     public synchronized String UpdatePaymentEntry(Map<String, Object> seqNumberMap) {
        String documnetNumber="";
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
            String query = "update Payment set paymentNumber = ?,seqnumber=?,datePreffixValue=?, dateAfterPreffixValue=?, dateSuffixValue=?,seqformat.ID=? where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{documnetNumber, seqNumber, datePrefix,dateafterPrefix,dateSuffix,sequenceFormatID, documentID, companyID});
        } catch (Exception e) {
            System.out.println(e);
        }
        return documnetNumber;
    }

    @Override
     public synchronized String UpdatePaymentEntryForNA(String payid, String entrynumber) {
        try {
            String query = "update Payment set paymentNumber = ? where ID=?";
            int numRows = executeUpdate( query, new Object[]{entrynumber,payid});
        } catch (Exception e) {
            System.out.println(e);
        }
        return entrynumber;
    }
     
    @Override
    public KwlReturnObject getNormalPayments(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get("companyid");
            String query = "from Payment where company.companyID = ? and normalPayment=? ";
            list = executeQuery( query, new Object[]{companyid, true});
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            return new KwlReturnObject(true, "", null, list, list.size());
        }
    }

    @Override
    public KwlReturnObject updatePayment(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String paymentid = (String) requestParams.get("paymentid");
            Payment payment = (Payment) get(Payment.class, paymentid);
            if (payment != null) {
                if (requestParams.containsKey("depositamountinbase") && requestParams.get("depositamountinbase") != null) {
                    payment.setDepositamountinbase(Double.parseDouble(requestParams.get("depositamountinbase").toString()));
                }
                saveOrUpdate(payment);
            }
            list.add(payment);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.updatePayment : " + ex.getMessage(), ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, list.size());
        }
    }

    @Override
    public List<LinkDetailPaymentToCreditNote> getDeletedLinkedPaymentCreditNotes(Payment payment, String linkedDetailIDs, String companyid) throws ServiceException {
        String selQuery = "from LinkDetailPaymentToCreditNote ld where ld.payment.ID=? and ld.company.companyID=? ";
        if(!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
           selQuery = selQuery.concat(" and ld.id not in (" + linkedDetailIDs + ")");
}
        List<LinkDetailPaymentToCreditNote> details = find(selQuery, new Object[]{payment.getID(), companyid});
        return details;
    }

    @Override
    public KwlReturnObject deleteSelectedLinkedPaymentCreditNotes(String paymentid, String linkedDetailIDs, String companyid) throws ServiceException {
         String delQuery = "delete from LinkDetailPaymentToCreditNote ld where ld.payment.ID=? and ld.company.companyID=?";
        if(!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
           delQuery = delQuery.concat(" and ld.id not in (" + linkedDetailIDs + ")");
        }
        int numRows = executeUpdate( delQuery, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Payment linked credit notes have been deleted successfully", null, null, numRows);
    }

    @Override
    public KwlReturnObject getAdvancePaymentIdLinkedWithNote(String noteId) throws ServiceException {
        List params = new ArrayList();
        params.add(noteId);
        String query = "select distinct payment,paymentlinkdate from linkdetailpaymenttocreditnote where creditnote=?";
        List list = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getLinkDetailPaymentToCreditNote(HashMap<String, Object> reqParams1) throws ServiceException {
        List list=null;
        try {
            String condition="";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) reqParams1.get(Constants.df);                      
            if(reqParams1.containsKey("creditnoteid") && reqParams1.get("creditnoteid") != null){
                   String cnid = (String) reqParams1.get("creditnoteid");
                   condition += " and rd.creditnote.ID=? ";
                   params.add(cnid);
            }
            if(reqParams1.containsKey("paymentid") && reqParams1.get("paymentid") != null){
                   String paymentId = (String) reqParams1.get("paymentid");
                   condition += " and rd.payment.ID=? ";
                   params.add(paymentId);
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
            String selQuery = "from LinkDetailPaymentToCreditNote rd  where rd.payment.deleted=false "+condition;
            list = executeQuery( selQuery, params.toArray());
        }catch (Exception ex) {
                Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
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
            if(reqParams1.containsKey("paymentid") && reqParams1.get("paymentid") != null){
                   String paymentId = (String) reqParams1.get("paymentid");
                   condition += " and rd.payment.ID=? ";
                   params.add(paymentId);
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
            String selQuery = "from LinkDetailPaymentToAdvancePayment rd  where rd.payment.deleted=false "+condition;
            list = executeQuery( selQuery, params.toArray());
        }catch (Exception ex) {
                Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
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
            if(reqParams1.containsKey("paymentid") && reqParams1.get("paymentid") != null){
                   String receiptid = (String) reqParams1.get("paymentid");
                   condition += " and rd.paymentId=? ";
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
            String selQuery = "select rd from LinkDetailReceiptToAdvancePayment rd,Payment pmt where pmt.ID=rd.paymentId and pmt.deleted=false"+condition;
            list = executeQuery( selQuery, params.toArray());
        }catch (Exception ex) {
                Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPaymentLinkedCreditNoteJE(String jeid, String companyid) throws ServiceException {
       String selQuery = "from LinkDetailPaymentToCreditNote L where L.linkedGainLossJE.ID=?";
        List list = executeQuery( selQuery, new Object[]{jeid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteLinkPaymentDetailsToCreditNoteAndUpdateAmountDue(String paymentid, String companyid,boolean tempCheck) throws ServiceException {
        String selQuery = "from LinkDetailPaymentToCreditNote pd where pd.payment.ID=? and pd.company.companyID=?";
        List<LinkDetailPaymentToCreditNote> details=find(selQuery,new Object[]{paymentid, companyid});
        List<CreditNote> cnList=new ArrayList<CreditNote>();
        for(LinkDetailPaymentToCreditNote linkDetailReceipt:details){
            CreditNote CN=linkDetailReceipt.getCreditnote();
            double amountdue=CN.getCnamountdue();
            CN.setCnamountdue(amountdue+linkDetailReceipt.getAmountInCNCurrency());
            CN.setOpeningBalanceAmountDue(CN.getOpeningBalanceAmountDue()+linkDetailReceipt.getAmountInCNCurrency());
            CN.setOpeningBalanceBaseAmountDue(CN.getOpeningBalanceBaseAmountDue()+linkDetailReceipt.getAmount());
            cnList.add(CN);
        }
        
        if(!cnList.isEmpty()){
            saveAll(cnList);
        }
        /*
         * tempCheck is for temporary delete receipt
         * if payment is temporary delete then tempCheck is set as true
         */
        if (tempCheck) {
            return new KwlReturnObject(true, "Payment Details has been deleted successfully.", null, null, 0);
        } else {
            return deleteLinkPaymentToCreditNoteDetails(paymentid, companyid);
        }
    }
     public KwlReturnObject deleteLinkPaymentToCreditNoteDetails(String paymentid, String companyid) throws ServiceException {
        String delQuery = "delete from LinkDetailPaymentToCreditNote LD where LD.payment.ID=? and LD.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Payment details has been deleted successfully", null, null, numRows);
    }
     
    public KwlReturnObject approveRecurringMakePayment(String repeateid, boolean ispendingapproval) throws ServiceException {
        RepeatedPayment rmp = null;
        try {
                rmp = (RepeatedPayment) get(RepeatedPayment.class, repeateid);
                rmp.setIspendingapproval(ispendingapproval);
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return new KwlReturnObject(true, "Recurring Make Payment Entry has been approved successfully.", null, null, 0);
    }
    
    public KwlReturnObject activateDeactivateMakePayment(String repeateid, boolean isactivate) throws ServiceException {
        RepeatedPayment rmp = null;
        try {
            rmp = (RepeatedPayment) get(RepeatedPayment.class, repeateid);
            rmp.setIsActivate(!isactivate);
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return new KwlReturnObject(true, "Recurring Make Payment Entry has been updated successfully.", null, null, 0);
    } 
             
    @Override
    public int updateToNullRepeatedMPOfMakePayment(String invoiceid, String repeateid) throws ServiceException {
        String query = "UPDATE payment SET repeatpayment=null WHERE id=? AND repeatpayment=?";
        int numRows = executeSQLUpdate(query, new Object[]{invoiceid, repeateid});
        return numRows;
    }

    @Override
    public int deleteRepeatedMP(String repeateid) throws ServiceException {
        String query = "DELETE FROM repeatedpayment WHERE id=?";
        int numRows = executeSQLUpdate(query, new Object[]{repeateid});
        return numRows;
    }
    @Override
    public KwlReturnObject getAdvanceReceiptDetailsByPayment(HashMap<String, Object> requestParams) throws ServiceException{
        List list = new ArrayList();
        String condition = "";
        String innerJoinStr="";
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                ArrayList params = new ArrayList();
                DateFormat df = (DateFormat) requestParams.get(Constants.df);
                String companyId = (String) requestParams.get("companyid");
                params.add(companyId);
                if (requestParams.containsKey("paymentid") && requestParams.get("paymentid") != null) {
                    String paymentid = (String) requestParams.get("paymentid");
                    condition += " and ad.payment=? ";
                    innerJoinStr+= " INNER JOIN advancedetail ad ON rad.advancedetailid=ad.id ";
                    params.add(paymentid);
                }
                if(requestParams.containsKey("asofdate") && requestParams.get("asofdate")!=null){
                    String asOfDate = (String) requestParams.get("asofdate");
                    condition += "  and je.entrydate<=? ";
                    innerJoinStr+= " INNER JOIN receipt r ON rad.receipt=r.id INNER JOIN journalentry je ON r.journalentry=je.id ";
                    params.add(df.parse(asOfDate));
                }
                String selQuery = "select rad.id, rad.amount, rad.exchangeratefortransaction from receiptadvancedetail rad "+innerJoinStr+" where rad.company=? " + condition;
                list = executeSQLQuery(selQuery, params.toArray());
            }
        } catch (ParseException | ServiceException ex) {
            Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    /* Saving Linking Information In Payment Linking Table If Payment is linked with any Transaction*/
    @Override
    public KwlReturnObject savePaymentLinking(HashMap<String, Object> reqParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String paymentid = (String) reqParams.get("docid");
            PaymentLinking paymentlinking = new PaymentLinking();
            if (reqParams.containsKey("docid")) {
                Payment payment = (Payment) get(Payment.class, paymentid);
                paymentlinking.setDocID(payment);
            }
            if (reqParams.containsKey("moduleid")) {
                paymentlinking.setModuleID((Integer) reqParams.get("moduleid"));
            }
            if (reqParams.containsKey("linkeddocid")) {
                paymentlinking.setLinkedDocID((String) reqParams.get("linkeddocid"));
            }
            if (reqParams.containsKey("linkeddocno")) {
                paymentlinking.setLinkedDocNo((String) reqParams.get("linkeddocno"));
            }
            if (reqParams.containsKey("sourceflag")) {
                paymentlinking.setSourceFlag((Integer) reqParams.get("sourceflag"));
            }
            saveOrUpdate(paymentlinking);
            list.add(paymentlinking);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.savePaymentLinking : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    /* Saving Linking Information In Credit Note Linking Table If Credit Note is linked with any Transaction*/

    @Override
    public KwlReturnObject updateEntryInCreditNoteLinkingTable(HashMap<String, Object> request) throws ServiceException {
        String newitemID = UUID.randomUUID().toString();
        String linkeddocid = (String) request.get("linkeddocid");
        String docid = (String) request.get("docid");
        int moduleid = (Integer) request.get("moduleid");
        int sourceFlag = (Integer) request.get("sourceflag");
        String linkeddocno = (String) request.get("linkeddocno");

        String query = "insert into  creditnotelinking(id,docid,linkeddocid,linkeddocno,moduleid,sourceflag) values(" + '"' + newitemID + '"' + ',' + '"' + docid + '"' + ',' + '"' + linkeddocid + '"' + ',' + '"' + linkeddocno + '"' + ',' + '"' + moduleid + '"' + ',' + '"' + sourceFlag + '"' + ")";
        int numRows = executeSQLUpdate(query, new String[]{});
        return new KwlReturnObject(true, "Credit Note Linking has been saved successfully.", null, null, numRows);
    }

    /* Deleting linking Information from Make Payment table if it is unlinked form any transaction or deleted*/
    @Override
    public KwlReturnObject deleteLinkingInformationOfMP(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        int numRows1 = 0, numRows2 = 0, numRows3 = 0, numRows4 = 0, numRowsTotal = 0;
        try {
            String delQuery = "";

            if (requestParams.containsKey("unlinkflag") && requestParams.get("unlinkflag") != null && Boolean.parseBoolean(requestParams.get("unlinkflag").toString())) {

                params.add(requestParams.get("linkedTransactionID"));
                params.add(requestParams.get("paymentid"));

                delQuery = "delete from CreditNoteLinking dn where dn.DocID.ID=? and dn.LinkedDocID=?";
                numRows1 = executeUpdate(delQuery, params.toArray());
                
                delQuery = "delete from ReceiptLinking rl where rl.DocID.ID=? and rl.LinkedDocID=?";
                numRows4 = executeUpdate(delQuery, params.toArray());

                delQuery = "delete from PaymentLinking inv where inv.LinkedDocID=? and inv.DocID.ID=?";
                numRows2 = executeUpdate(delQuery, params.toArray());

                delQuery = "delete from GoodsReceiptLinking gr where gr.DocID.ID=? and gr.LinkedDocID=?";
                numRows3 = executeUpdate(delQuery, params.toArray());

            } else {
                params.add(requestParams.get("paymentid"));

                delQuery = "delete from CreditNoteLinking cn where cn.LinkedDocID=?";
                numRows1 = executeUpdate(delQuery, params.toArray());

                delQuery = "delete from GoodsReceiptLinking grl where grl.LinkedDocID=?";
                numRows2 = executeUpdate(delQuery, params.toArray());
                
                delQuery = "delete from ReceiptLinking rl where rl.LinkedDocID=?";
                numRows4 = executeUpdate(delQuery, params.toArray());

                delQuery = "delete from PaymentLinking mp where mp.DocID.ID=?";
                numRows3 = executeUpdate(delQuery, params.toArray());
            }

            numRowsTotal = numRows1 + numRows2 + numRows3;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("", ex);
        }
        return new KwlReturnObject(true, "", null, null, numRowsTotal);
    }

    /**
     * Description : Method is used to get List of deleted LinkDetailPaymentToAdvancePayment
     * @param <payment> used to get Payment information
     * @param <linkedDetailIDs> used to get link detial ids
     * @param <companyid> used to get company id
     * @return List
     * @throws ServiceException 
     */
    @Override
    public List<LinkDetailPaymentToAdvancePayment> getDeletedLinkedPaymentAdvancePayment(Payment payment,String linkedDetailIDs, String companyid) throws ServiceException {
        String selQuery = "from LinkDetailPaymentToAdvancePayment ld where ld.payment.ID=? and ld.company.companyID=? ";
        if (!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
            selQuery = selQuery.concat(" and ld.id not in (" + linkedDetailIDs + ")");
        }
        List<LinkDetailPaymentToAdvancePayment> details = find(selQuery, new Object[]{payment.getID(), companyid});
        return details;
    }
    
    /**
     * Description : Method is used to delete LinkDetailPaymentToAdvancePayment.
     * @param <paymentid> used to get Payment id.
     * @param <linkedDetailIDs> used to get link detail ids
     * @param <companyid> used to get company id
     * @return KwlReturnObject
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject deleteSelectedLinkedPaymentAdvanceDetails(String paymentid, String linkedDetailIDs, String companyid) throws ServiceException {
        String delQuery = "delete from LinkDetailPaymentToAdvancePayment ldr where ldr.payment.ID=? and ldr.company.companyID=?";
        if (!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
            delQuery = delQuery.concat(" and ldr.id not in (" + linkedDetailIDs + ")");
        }
        int numRows = executeUpdate(delQuery, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Receipt linked Advance Payment have been deleted successfully.", null, null, numRows);
    }
    
    /**
     * Description : Method is used to delete LinkDetailPaymentToAdvancePayment and update amount due
     * @param <paymentid> used to get Payment id
     * @param <companyid> used to get Company id
     * @return KwlReturnObject
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject deleteLinkPaymentDetailsToAdvancePaymentAndUpdateAmountDue(String paymentid, String companyid,boolean  temmCheck) throws ServiceException {
        String selQuery = "from LinkDetailPaymentToAdvancePayment pd where pd.payment.ID=? and pd.company.companyID=?";
        List<LinkDetailPaymentToAdvancePayment> details = find(selQuery, new Object[]{paymentid, companyid});
        List<Receipt> paymentList = new ArrayList<Receipt>();
        for (LinkDetailPaymentToAdvancePayment linkDetailPayment : details) {
            Receipt receipt = linkDetailPayment.getReceipt();
            for (ReceiptAdvanceDetail advDetails : receipt.getReceiptAdvanceDetails()) {
                advDetails.setAmountDue(advDetails.getAmountDue() + linkDetailPayment.getAmountInPaymentCurrency());
            }
            paymentList.add(receipt);
        }

        if (!paymentList.isEmpty()) {
            saveAll(paymentList);
        }
        if (temmCheck) {
            return new KwlReturnObject(true, "Payment Details has been deleted successfully.", null, null, 0);
        } else {
            return deleteLinkPaymentToAdvancePaymentDetails(paymentid, companyid);
        }
    }
    
    /**
     * Description : Method is used for deleting LinkDetailPaymentToAdvancePayment
     * @param <paymentid> used to get Payment id
     * @param <companyid> used to get Company id
     * @return KwlReturnObject
     * @throws ServiceException 
     */
    public KwlReturnObject deleteLinkPaymentToAdvancePaymentDetails(String paymentid, String companyid) throws ServiceException {
        String delQuery = "delete from LinkDetailPaymentToAdvancePayment LD where LD.payment.ID=? and LD.company.companyID=?";
        int numRows = executeUpdate(delQuery, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Payment details has been deleted successfully.", null, null, numRows);
    }
    @Override
    public KwlReturnObject getTotalAmountofVendorpayment(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        long sdate = ((Date)requestParams.get("activeFromDate")).getTime();
        long edate = ((Date)requestParams.get("activeToDate")).getTime();
        String vendorid = requestParams.get("vendorID") != null ? requestParams.get("vendorID").toString() : "";
        String delQuery = "select SUM(pd.depositAmount) as totalamount from Payment as pd where pd.vendor.ID = ? and pd.deleted='F' "
                        + "and pd.createdon<=? and pd.createdon>=?";
        ll = executeQuery(delQuery, new Object[]{vendorid,edate,sdate});
        return new KwlReturnObject(true, "", null, ll, ll.size());
    }
    public KwlReturnObject getInvoiceDetailsAndTDSDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        String rowdetailid = (String) dataMap.get("rowdetailid");
        String query = "";
        if (!StringUtil.isNullOrEmpty(dataMap.get("documentType").toString())) {
            switch (Integer.parseInt(dataMap.get("documentType").toString())) {
                case Constants.AdvancePayment:
                    query = "from TdsDetails where advanceDetail.id= ? ";
                    break;
                case Constants.PaymentAgainstInvoice:
                    query = "from TdsDetails where paymentdetail.ID= ? ";
                    break;
                case Constants.PaymentAgainstCNDN:
                    query = "from TdsDetails where creditnotepaymentdetail.ID= ? ";
                    break;
                case Constants.GLPayment:
                    query = "from TdsDetails where paymentdetailotherwise.ID= ? ";
                    break;
            }
            list = executeQuery(query, new Object[]{rowdetailid});
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getInvoiceDetails(HashMap<String, Object> dataMap) throws ServiceException{
        String documentid = (String) dataMap.get("documentid");
        String query = "from GoodsReceiptDetail where goodsReceipt.ID= ? ";
        List list = executeQuery( query, new Object[]{documentid});        
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject deleteTDSDetails(HashMap<String, String> dataMap) throws ServiceException{
        String documentid = (String) dataMap.get("documentid");
        String documenttype = (String) dataMap.get("documenttype");
        String company = (String) dataMap.get("companyid");
        String query = "delete from TdsDetails where company.companyID=? and ";
        switch(documenttype){
            case "1":
                query+=" advanceDetail.id in ( "+documentid+" )";                
                break;
            case "2":
                query+=" paymentdetail.ID in ( "+documentid+" )";                
                break;
            case "3":
                query+=" creditnotepaymentdetail.ID in ( "+documentid+" )";                
                break;
            case "4":
                query+=" paymentdetailotherwise.ID in ( "+documentid+" )";                
                break;
        }
        int numRows = executeUpdate( query, new Object[]{company});        
        return new KwlReturnObject(true, "TDS Details has been deleted successfully.", null, null, numRows);
    }
    
    //To Update deleteflag to true of tds_rate table.
    public KwlReturnObject deleteTDSMasterRates(HashMap<String, String> dataMap) throws ServiceException {
        String tdsID = (String) dataMap.get("tdsID");
        if (!StringUtil.isNullOrEmpty(tdsID)) {
            String query = "UPDATE TDSRate SET deleted = true WHERE ";
            query += " id in ( " + tdsID + " )";
            int numRows = executeUpdate(query, new Object[]{});
            return new KwlReturnObject(true, "TDS Master Rates has been deleted successfully.", null, null, numRows);
        }
        return new KwlReturnObject(false, "Failed To delete TDS Master Rates.", null, null, 0);
    }
    
    //Function To check whether selected TDS Master Rate Record/s are used in Transaction(Advance Payment).
    @Override
    public KwlReturnObject ISTDSMasterRatesUsedInAdvancePayment(HashMap<String, String> dataMap) throws ServiceException {
        String tdsID = (String) dataMap.get("tdsID");
        String companyid = (String) dataMap.get("companyid");
        if (!StringUtil.isNullOrEmpty(tdsID) && !StringUtil.isNullOrEmpty(companyid)) {
            String query = "FROM TdsDetails WHERE company.companyID = ?  AND ";
            query += " ruleid in ( " + tdsID + " )";
            List list = executeQuery(query, new Object[]{companyid});
            return new KwlReturnObject(true, "", null, list, list.size());
        }
        return new KwlReturnObject(false, "Failed To check TDS Master Rates.", null, null, 0);
    }
    //Function To check whether selected TDS Master Rate Record/s are used in Transaction(Purchase Invoice).
    @Override
    public KwlReturnObject ISTDSMasterRatesUsedInPI(HashMap<String, String> dataMap) throws ServiceException {
        String tdsID = (String) dataMap.get("tdsID");
        String companyid = (String) dataMap.get("companyid");
        if (!StringUtil.isNullOrEmpty(tdsID) && !StringUtil.isNullOrEmpty(companyid)) {
            String query = "SELECT ID FROM GoodsReceiptDetail grd WHERE grd.goodsReceipt.company.companyID = ?  AND ";
            query += " tdsRuleId in ( " + tdsID + " )";
            List listInvoices = executeQuery(query, new Object[]{companyid});

            query = "SELECT ID FROM  ExpenseGRDetail grd WHERE grd.goodsReceipt.company.companyID = ?  AND ";
            query += " tdsRuleId in ( " + tdsID + " )";
            List listExpense = executeQuery(query, new Object[]{companyid});

            listInvoices.addAll(listExpense);
            return new KwlReturnObject(true, "", null, listInvoices, listInvoices.size());
        }
        return new KwlReturnObject(false, "Failed To check TDS Master Rates.", null, null, 0);
    }
    
    @Override
    public List getAdvancePaymentLinkedWithRefundReceipt(String paymentid,String companyid) throws ServiceException {
        List list = new ArrayList(); 
        try {
            String query = "select r.id, r.receiptnumber from linkdetailreceipttoadvancepayment ldp inner join receipt r on r.id = ldp.receipt where ldp.paymentid = ? and ldp.company = ?";
            list = executeSQLQuery( query, new Object[]{paymentid,companyid});
        } catch(Exception ex) {
            throw ServiceException.FAILURE("getAdvancePaymentLinkedWithRefundReceipt:" + ex.getMessage(), ex);
        }
        return list;
    }
    
    @Override
    public KwlReturnObject getTDSDeductionDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List finalResult = new ArrayList();
        try {
            String companyId = requestParams.get("companyid").toString();
            HashMap getVendorMap = new HashMap();
            getVendorMap.put("companyid", companyId);
            
            // Get Financial Year Start Date and End Date
            KwlReturnObject companyAccprefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) companyAccprefresult.getEntityList().get(0);
            Date financialYearStartDate = companyAccountPreferences.getFinancialYearFrom();
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(financialYearStartDate);
            startCal.add(Calendar.YEAR, 1);
            startCal.add(Calendar.DAY_OF_YEAR, -1);
            Date financialYearEndDate = startCal.getTime();

            KwlReturnObject result = accVendorDAOobj.getVendor(getVendorMap);
            Iterator Vendoritr = result.getEntityList().iterator();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat sdf1 = authHandler.getDateOnlyFormat();

            while (Vendoritr.hasNext()) {
                HashMap finalHM = new HashMap();
                Double totalTaxableAmount = 0.0;
                Double totalTdsTobeMade = 0.0;
                Double tdsDeductedTillDate = 0.0;
                Double balanceTDStoBeMade = 0.0;
                Double BalanceTDStobeMade = 0.0;
                Vendor vendor = (Vendor) Vendoritr.next();
                ArrayList params = new ArrayList();
                params.add(companyId);
                params.add(vendor.getID());
                params.add(financialYearStartDate.getTime());
                params.add(new Date().getTime());

                String query = "from Payment where company.companyID=? and vendor.ID=? and createdon >= ? and createdon <=? ";
                List list = executeQuery(query, params.toArray());
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Payment payment = (Payment) itr.next();
                    String date = sdf.format(new Date(payment.getCreatedon()));
                    // For Advance Payment

                    if (payment.getAdvanceDetails().size() > 0) {
                        Set<AdvanceDetail> rowObj = payment.getAdvanceDetails();
                        for (AdvanceDetail advDetail : rowObj) {
                            if (advDetail.getTdsamount() > 0) {                      // if payment includes tds amount then only that amount is taxable
                                if(advDetail.getTdsPaidFlag() == IndiaComplianceConstants.TDSPAYMENT){
                                    tdsDeductedTillDate += advDetail.getTdsamount();
                                }

                                totalTaxableAmount += advDetail.getAmount();

                                Set<TdsDetails> tdsdetailsSet = advDetail.getTdsdetails();
                                for (TdsDetails tdsdetails : tdsdetailsSet) {
                                    String natureOfPayment ="";
                                    if (!StringUtil.isNullOrEmpty(vendor.getNatureOfPayment())) {
                                         natureOfPayment = vendor.getNatureOfPayment();
                                    }
                                    String deducteeType = vendor.getDeducteeType();

                                    if (!StringUtil.isNullOrEmpty(natureOfPayment) && !StringUtil.isNullOrEmpty(deducteeType) && !StringUtil.isNullOrEmpty(date)
                                            && !StringUtil.isNullOrEmpty(vendor.getID())) {
                                        requestParams.put("activeFromDate", financialYearStartDate);
                                        requestParams.put("activeToDate", financialYearEndDate);
                                        requestParams.put("natureofPayment", natureOfPayment);
                                        requestParams.put("deducteeType", deducteeType);
                                        requestParams.put("residentialstatus", vendor.getResidentialstatus());
                                        requestParams.put("billdate", date);
                                        requestParams.put("amount", advDetail.getAmount());
                                        requestParams.put("vendorID", vendor.getID());

                                        //--------------------------------------------------TDS Calculation Function copied as it is-------------------------------------------------------------------------------------------
                                        totalTdsTobeMade += tdsCalculation(requestParams);
                                        //---------------------------------------------------------------------------------------------------------------------------------------------
                                    }
                                }
                            }
                        }
                    }
                    // For Invoice Payment
//                    if (payment.getRows().size() > 0) {
//                        Set<PaymentDetail> rowObj = payment.getRows();
//                        for (PaymentDetail advDetail : rowObj) {
//                            if (advDetail.getTdsamount() > 0) {                                 // if payment includes tds amount then only that amount is taxable
//                                tdsDeductedTillDate += advDetail.getTdsamount();
//
//                                totalTaxableAmount += advDetail.getAmount();
//
//                                Set<TdsDetails> tdsdetailsSet = advDetail.getTdsdetails();
//                                for (TdsDetails tdsdetails : tdsdetailsSet) {
//                                    String natureOfPayment = tdsdetails.getAccount().getNatureOfPayment();
//                                    String deducteeType = vendor.getDeducteeType();
//
//                                    if (!StringUtil.isNullOrEmpty(natureOfPayment) && !StringUtil.isNullOrEmpty(deducteeType) && !StringUtil.isNullOrEmpty(date)
//                                            && !StringUtil.isNullOrEmpty(vendor.getID()) && activeFromDate != null && activeToDate != null) {
//                                        requestParams.put("activeFromDate", activeFromDate);
//                                        requestParams.put("activeToDate", activeToDate);
//                                        requestParams.put("natureofPayment", natureOfPayment);
//                                        requestParams.put("deducteeType", deducteeType);
//                                        requestParams.put("residentialstatus", vendor.getResidentialstatus());
//                                        requestParams.put("date", date);
//                                        requestParams.put("amount", advDetail.getAmount());
//                                        requestParams.put("vendorID", vendor.getID());
//
//                                        //--------------------------------------------------TDS Calculation Function copied as it is-------------------------------------------------------------------------------------------
//                                        totalTdsTobeMade += tdsCalculation(requestParams);
//                                        //---------------------------------------------------------------------------------------------------------------------------------------------
//                                    }
//                                }
//                            }
//
//                        }
//                    }
//                    // For GL
//                    if (payment.getPaymentDetailOtherwises().size() > 0) {
//                        Set<PaymentDetailOtherwise> rowObj = payment.getPaymentDetailOtherwises();
//                        for (PaymentDetailOtherwise advDetail : rowObj) {
//                            if (advDetail.getTdsamount() > 0) {                           // if payment includes tds amount then only that amount is taxable
//                                tdsDeductedTillDate += advDetail.getTdsamount();
//
//                                totalTaxableAmount += advDetail.getAmount();
//
//                                Set<TdsDetails> tdsdetailsSet = advDetail.getTdsdetails();
//                                for (TdsDetails tdsdetails : tdsdetailsSet) {
//                                    String natureOfPayment = tdsdetails.getAccount().getNatureOfPayment();
//                                    String deducteeType = vendor.getDeducteeType();
//
//                                    if (!StringUtil.isNullOrEmpty(natureOfPayment) && !StringUtil.isNullOrEmpty(deducteeType) && !StringUtil.isNullOrEmpty(date)
//                                            && !StringUtil.isNullOrEmpty(vendor.getID()) && activeFromDate != null && activeToDate != null) {
//                                        requestParams.put("activeFromDate", activeFromDate);
//                                        requestParams.put("activeToDate", activeToDate);
//                                        requestParams.put("natureofPayment", natureOfPayment);
//                                        requestParams.put("deducteeType", deducteeType);
//                                        requestParams.put("residentialstatus", vendor.getResidentialstatus());
//                                        requestParams.put("date", date);
//                                        requestParams.put("amount", advDetail.getAmount());
//                                        requestParams.put("vendorID", vendor.getID());
//
//                                        //--------------------------------------------------TDS Calculation Function copied as it is-------------------------------------------------------------------------------------------
//                                        totalTdsTobeMade += tdsCalculation(requestParams);
//                                        //---------------------------------------------------------------------------------------------------------------------------------------------
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    // For Credit Note(CN)
//                    if (payment.getCreditNotePaymentDetails().size() > 0) {
//                        Set<CreditNotePaymentDetails> rowObj = payment.getCreditNotePaymentDetails();
//                        for (CreditNotePaymentDetails advDetail : rowObj) {
//                            if (advDetail.getTdsamount() > 0) {                           // if payment includes tds amount then only that amount is taxable
//                                tdsDeductedTillDate += advDetail.getTdsamount();
//
//                                totalTaxableAmount += advDetail.getAmountPaid();
//
//                                CreditNote cn = advDetail.getCreditnote();
//                                Set<CreditNoteTaxEntry> CnrowObj = cn.getCnTaxEntryDetails();
//                                for (CreditNoteTaxEntry cnTaxEntry : CnrowObj) {
//                                    String deducteeType = vendor.getDeducteeType();
//                                    String natureOfPayment = "";
//                                    if (cnTaxEntry.getAccount() != null) {
//                                        natureOfPayment = cnTaxEntry.getAccount().getNatureOfPayment();
//                                    }
//
//                                    if (!StringUtil.isNullOrEmpty(natureOfPayment) && !StringUtil.isNullOrEmpty(deducteeType) && !StringUtil.isNullOrEmpty(date)
//                                            && !StringUtil.isNullOrEmpty(vendor.getID()) && activeFromDate != null && activeToDate != null) {
//                                        requestParams.put("activeFromDate", activeFromDate);
//                                        requestParams.put("activeToDate", activeToDate);
//                                        requestParams.put("natureofPayment", natureOfPayment);
//                                        requestParams.put("deducteeType", deducteeType);
//                                        requestParams.put("residentialstatus", vendor.getResidentialstatus());
//                                        requestParams.put("date", date);
//                                        requestParams.put("amount", advDetail.getAmountPaid());
//                                        requestParams.put("vendorID", vendor.getID());
//
//                                        //--------------------------------------------------TDS Calculation Function copied as it is-------------------------------------------------------------------------------------------
//                                        totalTdsTobeMade += tdsCalculation(requestParams);
//                                        //---------------------------------------------------------------------------------------------------------------------------------------------
//                                    }
//                                }
//                            }
//                        }
//                    }
                }
                finalHM.put("vendorname", vendor.getName());
                finalHM.put("vendorpan", vendor.getPANnumber());
                finalHM.put("vendorid", vendor.getID());
                finalHM.put("totaltaxableamount", totalTaxableAmount);
                finalHM.put("vendorpanstatus", vendor.getPanStatus());
                finalHM.put("natureofpayment", "Wat if multiple NOP");
                finalHM.put("taxableamount", totalTaxableAmount);
                finalHM.put("tdstobemade", totalTdsTobeMade);
                finalHM.put("deductedtilldate", tdsDeductedTillDate);

                balanceTDStoBeMade = totalTdsTobeMade - tdsDeductedTillDate;

                finalHM.put("balancetobededucted", balanceTDStoBeMade);
                finalHM.put("balancetobemade", balanceTDStoBeMade);

                finalResult.add(finalHM);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getTDSDeductionDetails : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "", null, finalResult, finalResult.size());
    }
    
    @Override
    public KwlReturnObject getTDSReportTransactionWise(HashMap<String, Object> requestParams) throws ServiceException {
        List finalResult = new ArrayList();
        try {
            String companyId = requestParams.get("companyid").toString();
            String query = "";
            int TDSPaymentType = IndiaComplianceConstants.NOTDSPAID;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MMM-dd");
            String startDate = StringUtil.DecodeText((String) requestParams.get("startdate"));
            String endDate = StringUtil.DecodeText((String) requestParams.get("enddate"));
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                if (requestParams.containsKey("paymentid") && requestParams.get("paymentid") != null && !StringUtil.isNullOrEmpty(requestParams.get("paymentid").toString())) {
                    HashMap TDSPaymentParams = new HashMap();
                    TDSPaymentParams.put("companyID", companyId);
                    TDSPaymentParams.put("paymentID", requestParams.get("paymentid"));
                    TDSPaymentType = getTDSPaymentType(TDSPaymentParams);
                }
            ArrayList params = new ArrayList();
            params.add(companyId);
            params.add(df.parse(startDate));
            params.add(df.parse(endDate));
            
            query = "from Payment p where p.company.companyID=? and p.journalEntry.entryDate >= ? and p.journalEntry.entryDate <=? ";
//            query = "from Payment p where p.company.companyID=? and p.creationDate >= ? and p.creationDate <=? ";
                if (requestParams.containsKey("vendorId") && requestParams.get("vendorId") != null && !StringUtil.isNullOrEmpty(requestParams.get("vendorId").toString())) {
                    if (!StringUtil.isNullOrEmpty(query)) {
                        query += " and ";
                    }
                    query += " p.vendor.ID = ? ";
                    params.add(requestParams.get("vendorId"));
                }
                if (requestParams.containsKey("deducteetype") && requestParams.get("deducteetype") != null && !StringUtil.isNullOrEmpty(requestParams.get("deducteetype").toString())) {
                    if (!StringUtil.isNullOrEmpty(query)) {
                        query += " and ";
                    }
                    query += " p.vendor.deducteeCode = ? ";
                    params.add(requestParams.get("deducteetype"));
                }
                if (requestParams.containsKey("nop") && requestParams.get("nop") != null && !StringUtil.isNullOrEmpty(requestParams.get("nop").toString())) {
                    if (!StringUtil.isNullOrEmpty(query)) {
                        query += " and ";
                    }
                    query += " p.ID in( select payment.ID from AdvanceDetail where id in (SELECT advanceDetail.id from TdsDetails where natureOfPayment.ID =?)) ";
                    params.add(requestParams.get("nop"));
                }
                
            query+=" ORDER BY p.journalEntry.entryDate " ;
//            query+=" ORDER BY p.creationDate " ;
            List list = executeQuery(query, params.toArray());
            Iterator itr = list.iterator();
            while (itr.hasNext()) {

                Payment payment = (Payment) itr.next();
                String date = sdf.format(payment.getJournalEntry().getEntryDate());
                // For Advance Payment
                Vendor vendor = payment.getVendor();
                if (payment.getAdvanceDetails().size() > 0) {
                    Set<AdvanceDetail> rowObj = payment.getAdvanceDetails();
                    for (AdvanceDetail advDetail : rowObj) {
                        if (advDetail.getTdsamount() > 0) {                      // if payment includes tds amount then only that amount is taxable
                            if (requestParams.containsKey("paymentid") && requestParams.get("paymentid") != null && !StringUtil.isNullOrEmpty(requestParams.get("paymentid").toString())) {
                                // To show in the TDS challan report
                                if (TDSPaymentType == IndiaComplianceConstants.TDSPAYMENT) {
                                    if (advDetail.getTdsPayment() == null || (advDetail.getTdsPayment() != null && !advDetail.getTdsPayment().equals(requestParams.get("paymentid").toString()))) {
                                        continue;
                                    }
                                } else if (TDSPaymentType == IndiaComplianceConstants.TDSINTERESTPAYMENT) {
                                    if (advDetail.getTdsInterestPayment() == null || (advDetail.getTdsInterestPayment() != null && !advDetail.getTdsInterestPayment().equals(requestParams.get("paymentid").toString()))) {
                                        continue;
                                    }
                                } else if (TDSPaymentType == IndiaComplianceConstants.TDSANDTDSINTERESTPAYMENT) {
                                    if (advDetail.getTdsPayment() == null && advDetail.getTdsInterestPayment() == null) {
                                        continue;
                                    }
                                    if (!((advDetail.getTdsPayment() != null && advDetail.getTdsPayment().equals(requestParams.get("paymentid").toString()))
                                            || (advDetail.getTdsInterestPayment() != null && advDetail.getTdsInterestPayment().equals(requestParams.get("paymentid").toString())))) {
                                        continue;
                                    }
                                }
                            }
                            if (requestParams.containsKey("isPayment") && requestParams.get("isPayment") != null && !StringUtil.isNullOrEmpty(requestParams.get("isPayment").toString())) {
                                //  records whose payment is done should not be present in new payment
                                boolean isPayment = Boolean.parseBoolean(requestParams.get("isPayment").toString());
                                int tdsPaymentType = IndiaComplianceConstants.NOTDSPAID;
                                if (requestParams.containsKey("tdsPaymentType") && requestParams.get("tdsPaymentType") != "") {
                                    tdsPaymentType = (int) requestParams.get("tdsPaymentType");
                                }
                                boolean conditionToSkipRecords = false;
                                if (tdsPaymentType == IndiaComplianceConstants.TDSPAYMENT) {
                                    conditionToSkipRecords = (isPayment && advDetail.getTdsPaidFlag() == 1);
                                } else if (tdsPaymentType == IndiaComplianceConstants.TDSINTERESTPAYMENT) {
                                    conditionToSkipRecords = (isPayment && advDetail.getTdsInterestPaidFlag() == 1);
                                } else if (tdsPaymentType == IndiaComplianceConstants.TDSANDTDSINTERESTPAYMENT) {
                                    conditionToSkipRecords = (isPayment && (advDetail.getTdsPaidFlag() == 1 || advDetail.getTdsInterestPaidFlag() == 1));
                                }
                                if (conditionToSkipRecords) {
                                    continue;
                                }
                            }

                            Set<TdsDetails> tdsdetailsSet = advDetail.getTdsdetails();
                            for (TdsDetails tdsdetails : tdsdetailsSet) {

                                HashMap finalHM = new HashMap();
                                String transactionDate = sdf1.format(payment.getJournalEntry().getEntryDate());
                                String transactionDocNo = "";
                                String natureOfPayment = "";
                                Double openingAmt = 0.0;
                                Double PendingAmt = 0.0;
                                String dueOn = "";
                                int overdueDays = 0;
                                int overdueMonths = 0;
                                int tdsPaidFlag = advDetail.getTdsPaidFlag();
                                
                                if(!StringUtil.isNullOrEmpty(vendor.getNatureOfPayment())){
                                    natureOfPayment = vendor.getNatureOfPayment();
                                    KwlReturnObject NOPObj = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), natureOfPayment);
                                    if (NOPObj != null) {
                                        MasterItem masteritem = (MasterItem) NOPObj.getEntityList().get(0);
                                        natureOfPayment = masteritem.getValue();
                                    } else {
                                        natureOfPayment = "";
                                    }
                                }

                                openingAmt = advDetail.getTdsamount();
                                
                                transactionDocNo = payment.getPaymentNumber();

                                Calendar cal = Calendar.getInstance();
                                Calendar dueCal = Calendar.getInstance();
                                Date d = payment.getJournalEntry().getEntryDate();
                                cal.setTime(d);
                                int month = cal.get(Calendar.MONTH) + 1;
                                if(cal.get(Calendar.MONTH) == Calendar.MARCH){ // i.e. month of March
                                    dueCal.set(cal.get(Calendar.YEAR), month, 30); // then due date is 30th of April
                                } else {
                                    dueCal.set(cal.get(Calendar.YEAR), month, 7);
                                }

                                overdueDays = (int) ((new Date().getTime() - dueCal.getTimeInMillis()) / (1000 * 60 * 60 * 24));
                                
                                dueOn = sdf1.format(dueCal.getTime());
                                HashMap overDuebyMonthParams = new HashMap();
                                overDuebyMonthParams.put("transactionDate", payment.getJournalEntry().getEntryDate());
                                if (requestParams.containsKey("asOfDate") && !StringUtil.isNullOrEmpty((String)requestParams.get("asOfDate"))) {
                                    overDuebyMonthParams.put("asOfDate",  authHandler.getDateOnlyFormat().parse(requestParams.get("asOfDate").toString()));
                                } else {
                                    overDuebyMonthParams.put("asOfDate",  new Date());
                                }
                                overdueMonths = CalculateOverDueByMonths(overDuebyMonthParams);
                                
                                int tdsInterestPaidFlag = advDetail.getTdsInterestPaidFlag();
                                finalHM.put("vendorname", vendor.getName());
                                finalHM.put("vendorPanNo", vendor.getPANnumber());
                                finalHM.put("vendorPanStatus", vendor.getPanStatus());
                                finalHM.put("transactionDate", transactionDate);
                                finalHM.put("transactionDocNo", transactionDocNo);
                                finalHM.put("natureOfPayment", tdsdetails.getNatureOfPayment()!=null?tdsdetails.getNatureOfPayment().getValue():"");
                                finalHM.put("amountpaid", authHandler.round(advDetail.getAmount(),companyId));
                                finalHM.put("tdsamount", openingAmt);
                                finalHM.put("unpaidtdsamount", (tdsPaidFlag== IndiaComplianceConstants.NOTDSPAID || tdsPaidFlag== IndiaComplianceConstants.TDSINTERESTPAYMENT)?openingAmt:0);
                                finalHM.put("duedate", dueOn);
                                finalHM.put("overdueMonths", overdueMonths);
                                finalHM.put("tdsPayableAccountid", tdsdetails.getTdspayableaccount()!= null?tdsdetails.getTdspayableaccount().getID():"");
                                finalHM.put("tdsPayableAccount", tdsdetails.getTdspayableaccount()!= null?tdsdetails.getTdspayableaccount().getAccountName():"");
                                finalHM.put("tdsInterestPayableAccountid", vendor.getTdsInterestPayableAccount()!= null?vendor.getTdsInterestPayableAccount().getID():"");
                                finalHM.put("tdsInterestPayableAccount", vendor.getTdsInterestPayableAccount()!= null?vendor.getTdsInterestPayableAccount().getAccountName():"");
                                finalHM.put("TDSPaidFlag", tdsPaidFlag);
                                finalHM.put("tdsInterestPaidFlag", tdsInterestPaidFlag);
                                finalHM.put("tdsInterestRateAtPaymentTime", advDetail.getTdsInterestRateAtPaymentTime());
                                finalHM.put("openingAmt", authHandler.round(openingAmt,companyId));
                                finalHM.put("tdsAmt", openingAmt);
                                finalHM.put("PendingAmt", (tdsPaidFlag == IndiaComplianceConstants.NOTDSPAID || tdsPaidFlag == IndiaComplianceConstants.TDSINTERESTPAYMENT) ? openingAmt : 0);
                                finalHM.put("paymentid",advDetail.getTdsPayment()!= null? advDetail.getTdsPayment():"" );
                                finalHM.put("tdsRate",tdsdetails.getTdspercentage());
                                finalHM.put("paymentInterestId",advDetail.getTdsInterestPayment()!= null? advDetail.getTdsInterestPayment():"" );
                                finalHM.put("dueOn", dueOn);
                                finalHM.put("overdueDays", overdueDays <= 0 ? 0 : overdueDays);
                                String tdsPaymentID = advDetail.getTdsPayment();
                                if (tdsPaidFlag == 1 && !StringUtil.isNullOrEmpty(tdsPaymentID)) {
                                    //If TDS is paid then Freeze the TDS Interest Amount.
                                    KwlReturnObject TDSPaymentObjList = accountingHandlerDAOobj.getObject(Payment.class.getName(), tdsPaymentID);
                                    Payment TDSPaymentObj = (Payment) TDSPaymentObjList.getEntityList().get(0);
                                    finalHM.put("asOnDateForDueDateCaluculation", TDSPaymentObj.getJournalEntry().getEntryDate());
                                    finalHM.put("transactionDateForDueDateCaluculation", payment.getJournalEntry().getEntryDate());
                                }
                                finalResult.add(finalHM);
                            }
                        }
                    }
                }
                // For Invoice Payment
//                if (payment.getRows().size() > 0) {
//                    Set<PaymentDetail> rowObj = payment.getRows();
//                    for (PaymentDetail advDetail : rowObj) {
//                        if (advDetail.getTdsamount() > 0) {                                 // if payment includes tds amount then only that amount is taxable
//                            Set<TdsDetails> tdsdetailsSet = advDetail.getTdsdetails();
//                            for (TdsDetails tdsdetails : tdsdetailsSet) {
//
//                                HashMap finalHM = new HashMap();
//                                String transactionDate = sdf1.format(payment.getJournalEntry().getEntryDate());
//                                String transactionDocNo = "";
//                                String natureOfPayment = "";
//                                Double openingAmt = 0.0;
//                                Double PendingAmt = 0.0;
//                                String dueOn = "";
//                                int overdueDays = 0;
//
//                                if(!StringUtil.isNullOrEmpty(vendor.getNatureOfPayment())){
//                                    natureOfPayment = vendor.getNatureOfPayment();
//                                    KwlReturnObject NOPObj = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), natureOfPayment);
//                                    if (NOPObj != null) {
//                                        MasterItem masteritem = (MasterItem) NOPObj.getEntityList().get(0);
//                                        natureOfPayment = masteritem.getValue();
//                                    } else {
//                                        natureOfPayment = "";
//                                    }
//                                }
//
//                                openingAmt = advDetail.getTdsamount();
//                                
//                                transactionDocNo = payment.getPaymentNumber();
//
//                                Calendar cal = Calendar.getInstance();
//                                Calendar dueCal = Calendar.getInstance();
//                                Date d = payment.getJournalEntry().getEntryDate();
//                                cal.setTime(d);
//                                int month = cal.get(Calendar.MONTH) + 1;
//                                if(cal.get(Calendar.MONTH) == Calendar.MARCH){ // i.e. month of March
//                                    dueCal.set(cal.get(Calendar.YEAR), month, 30); // then due date is 30th of April
//                                } else {
//                                    dueCal.set(cal.get(Calendar.YEAR), month, 7);
//                                }
//
//                                overdueDays = (int) ((new Date().getTime() - dueCal.getTimeInMillis()) / (1000 * 60 * 60 * 24));
//
//                                dueOn = sdf1.format(dueCal.getTime());
//
//                                finalHM.put("vendorname", vendor.getName());
//                                finalHM.put("transactionDate", transactionDate);
//                                finalHM.put("transactionDocNo", transactionDocNo);
//                                finalHM.put("natureOfPayment", natureOfPayment);
//                                finalHM.put("amountpaid", advDetail.getAmount());
//                                finalHM.put("openingAmt", openingAmt);
//                                finalHM.put("PendingAmt", openingAmt);
//                                finalHM.put("dueOn", dueOn);
//                                finalHM.put("overdueDays", overdueDays <= 0 ? 0 : overdueDays);
//                                finalResult.add(finalHM);
//
//                            }
//                        }
//
//                    }
//                }
//                // For GL
//                if (payment.getPaymentDetailOtherwises().size() > 0) {
//                    Set<PaymentDetailOtherwise> rowObj = payment.getPaymentDetailOtherwises();
//                    for (PaymentDetailOtherwise advDetail : rowObj) {
//                        if (advDetail.getTdsamount() > 0) {                           // if payment includes tds amount then only that amount is taxable
//                            Set<TdsDetails> tdsdetailsSet = advDetail.getTdsdetails();
//                            for (TdsDetails tdsdetails : tdsdetailsSet) {
//
//                                HashMap finalHM = new HashMap();
//                                String transactionDate = sdf1.format(payment.getJournalEntry().getEntryDate());
//                                String transactionDocNo = "";
//                                String natureOfPayment = "";
//                                Double openingAmt = 0.0;
//                                Double PendingAmt = 0.0;
//                                String dueOn = "";
//                                int overdueDays = 0;
//
//                                if(!StringUtil.isNullOrEmpty(vendor.getNatureOfPayment())){
//                                    natureOfPayment = vendor.getNatureOfPayment();
//                                    KwlReturnObject NOPObj = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), natureOfPayment);
//                                    if (NOPObj != null) {
//                                        MasterItem masteritem = (MasterItem) NOPObj.getEntityList().get(0);
//                                        natureOfPayment = masteritem.getValue();
//                                    } else {
//                                        natureOfPayment = "";
//                                    }
//                                }
//                                
//                                openingAmt = advDetail.getTdsamount();                                    
//                                
//                                transactionDocNo = payment.getPaymentNumber();
//
//                                Calendar cal = Calendar.getInstance();
//                                Calendar dueCal = Calendar.getInstance();
//                                Date d = payment.getJournalEntry().getEntryDate();
//                                cal.setTime(d);
//                                int month = cal.get(Calendar.MONTH) + 1;
//                                if(cal.get(Calendar.MONTH) == Calendar.MARCH){ // i.e. month of March
//                                    dueCal.set(cal.get(Calendar.YEAR), month, 30); // then due date is 30th of April
//                                } else {
//                                    dueCal.set(cal.get(Calendar.YEAR), month, 7);
//                                }
//
//                                overdueDays = (int) ((new Date().getTime() - dueCal.getTimeInMillis()) / (1000 * 60 * 60 * 24));
//
//                                dueOn = sdf1.format(dueCal.getTime());
//
//                                finalHM.put("vendorname", vendor.getName());
//                                finalHM.put("transactionDate", transactionDate);
//                                finalHM.put("transactionDocNo", transactionDocNo);
//                                finalHM.put("natureOfPayment", natureOfPayment);
//                                finalHM.put("amountpaid", advDetail.getAmount());
//                                finalHM.put("openingAmt", openingAmt);
//                                finalHM.put("PendingAmt", openingAmt);
//                                finalHM.put("dueOn", dueOn);
//                                finalHM.put("overdueDays", overdueDays <= 0 ? 0 : overdueDays);
//                                finalResult.add(finalHM);
//                            }
//                        }
//                    }
//                }
                // For Credit Note(CN)
//                if (payment.getCreditNotePaymentDetails().size() > 0) {
//                    Set<CreditNotePaymentDetails> rowObj = payment.getCreditNotePaymentDetails();
//                    for (CreditNotePaymentDetails advDetail : rowObj) {
//                        if (advDetail.getTdsamount() > 0) {                           // if payment includes tds amount then only that amount is taxable
//
//                            CreditNote cn = advDetail.getCreditnote();
//                            Set<CreditNoteTaxEntry> CnrowObj = cn.getCnTaxEntryDetails();
//                            for (CreditNoteTaxEntry cnTaxEntry : CnrowObj) {
//
//                                HashMap finalHM = new HashMap();
//                                String transactionDate = sdf1.format(payment.getJournalEntry().getEntryDate());
//                                String transactionDocNo = "";
//                                String natureOfPayment = "";
//                                Double openingAmt = 0.0;
//                                Double PendingAmt = 0.0;
//                                String dueOn = "";
//                                int overdueDays = 0;
//
//                                if(!StringUtil.isNullOrEmpty(vendor.getNatureOfPayment())){
//                                    natureOfPayment = vendor.getNatureOfPayment();
//                                    KwlReturnObject NOPObj = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), natureOfPayment);
//                                    if (NOPObj != null) {
//                                        MasterItem masteritem = (MasterItem) NOPObj.getEntityList().get(0);
//                                        natureOfPayment = masteritem.getValue();
//                                    } else {
//                                        natureOfPayment = "";
//                                    }
//                                }
//                                
//                                openingAmt = advDetail.getTdsamount();
//
//                                transactionDocNo = payment.getPaymentNumber();
//
//                                Calendar cal = Calendar.getInstance();
//                                Calendar dueCal = Calendar.getInstance();
//                                Date d = payment.getJournalEntry().getEntryDate();
//                                cal.setTime(d);
//                                int month = cal.get(Calendar.MONTH) + 1;
//                                if(cal.get(Calendar.MONTH) == Calendar.MARCH){ // i.e. month of March
//                                    dueCal.set(cal.get(Calendar.YEAR), month, 30); // then due date is 30th of April
//                                } else {
//                                    dueCal.set(cal.get(Calendar.YEAR), month, 7);
//                                }
//
//                                overdueDays = (int) ((new Date().getTime() - dueCal.getTimeInMillis()) / (1000 * 60 * 60 * 24));
//
//                                dueOn = sdf1.format(dueCal.getTime());
//
//                                finalHM.put("vendorname", vendor.getName());
//                                finalHM.put("transactionDate", transactionDate);
//                                finalHM.put("transactionDocNo", transactionDocNo);
//                                finalHM.put("natureOfPayment", natureOfPayment);
//                                finalHM.put("amountpaid", advDetail.getAmountPaid());
//                                finalHM.put("openingAmt", openingAmt);
//                                finalHM.put("PendingAmt", openingAmt);
//                                finalHM.put("dueOn", dueOn);
//                                finalHM.put("overdueDays", overdueDays <= 0 ? 0 : overdueDays);
//                                finalResult.add(finalHM);
//                            }
//                        }
//                    }
//                }
            }

            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getTDSDeductionDetails : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "", null, finalResult, finalResult.size());
    }
    
    @Override
    public double tdsCalculation (HashMap requestParams ) throws ServiceException{
        JSONObject jobj = new JSONObject();
        double rate = 0.0;
        double amount = 0.0;
        double totalamount = 0.0;
        double totalTdsTobeMade = 0.0;
        String id = "";
        try {
            amount = (Double) requestParams.get("amount");
            KwlReturnObject kjobjAmount = getTotalAmountofVendorpayment(requestParams);
            List listobjAmount = kjobjAmount.getEntityList();
            if (listobjAmount.size() > 0) {
                if (listobjAmount.get(0) != null) {
                    totalamount = (Double) listobjAmount.get(0);
                    totalamount = amount + totalamount;
                    requestParams.put("totalamount", totalamount);
                } else {
                    requestParams.put("totalamount", amount);
                }
            } else {
                requestParams.put("totalamount", amount);
            }
            KwlReturnObject kjobj = accCommonTablesDAOobj.getTDSRate(requestParams);
            List listobj = kjobj.getEntityList();
            amount = (Double) requestParams.get("amount");
            if (listobj.size() > 0) {
                Object[] row = (Object[]) listobj.get(0);
                if (row != null) {
                    rate = Double.parseDouble(row[0].toString());
                    id = row[1].toString();
                }
                jobj.put("success", true);
            } else {
                jobj.put("success", false);
            }

            totalTdsTobeMade += (amount * rate) / 100;
            
        } catch (Exception ex) {
             throw ServiceException.FAILURE("accVendorPaymentImpl.getTDSDeductionDetails : " + ex.getMessage(), ex);
        }
        return totalTdsTobeMade;
    }
    
    @Override
    public KwlReturnObject getAccountOpeningBalanceTransactionFromNo(String transactionNumber, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from AccountOpeningTransaction where transactionNumber=? and company.companyID=?";
        list = executeQuery(q, new Object[]{transactionNumber, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveAccountOpeningTransaction(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            AccountOpeningTransaction payment = getAccountOpeningTransactionObj(hm);
            saveOrUpdate(payment);
            list.add(payment);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.saveAccountOpeningTransaction : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Account Opening Transaction has been updated successfully", null, list, list.size());
    }

    public AccountOpeningTransaction getAccountOpeningTransactionObj(HashMap<String, Object> hm) throws ServiceException {
        AccountOpeningTransaction payment = null;
        try {
            String paymentid = (String) hm.get("paymentid");
            if (StringUtil.isNullOrEmpty(paymentid)) {
                payment = new AccountOpeningTransaction();
                if (hm.containsKey("createdby")) {
                    User createdby = hm.get("createdby") == null ? null : (User) get(User.class, (String) hm.get("createdby"));
                    payment.setCreatedby(createdby);
                }
            
                if (hm.containsKey("modifiedby")) {
                    User modifiedby = hm.get("modifiedby") == null ? null : (User) get(User.class, (String) hm.get("modifiedby"));
                    payment.setModifiedby(modifiedby);
                }
                
                if (hm.containsKey("createdon")) {
                    payment.setCreatedon((Long) hm.get("createdon"));
                }
                
                if (hm.containsKey("updatedon")) {
                    payment.setUpdatedon((Long) hm.get("updatedon"));
                }
            } else {
                payment = (AccountOpeningTransaction) get(AccountOpeningTransaction.class, paymentid);
                if (hm.containsKey("modifiedby")) {
                    User modifiedby = hm.get("modifiedby") == null ? null : (User) get(User.class, (String) hm.get("modifiedby"));
                    payment.setModifiedby(modifiedby);
                }
                if (hm.containsKey("updatedon")) {
                    payment.setUpdatedon((Long) hm.get("updatedon"));
                }
            }

            if (hm.containsKey("entrynumber")) {
                payment.setTransactionNumber((String) hm.get("entrynumber"));
            }
            
            if (hm.containsKey("memo")) {
                payment.setMemo((String) hm.get("memo"));
            }
            
            if (hm.containsKey("paydetailsid")) {
                PayDetail pd = hm.get("paydetailsid") == null ? null : (PayDetail) get(PayDetail.class, (String) hm.get("paydetailsid"));
                payment.setPayDetail(pd);
            }
            
            if (hm.containsKey("companyid")) {
                Company company = hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid"));
                payment.setCompany(company);
            }
            
            if (hm.containsKey("currencyid")) {
                KWLCurrency currency = hm.get("currencyid") == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) hm.get("currencyid"));
                payment.setCurrency(currency);
            }
            
            if (hm.containsKey("depositamount") && !StringUtil.isNullOrEmpty(hm.get("depositamount").toString())) {
                payment.setDepositAmount(Double.parseDouble(hm.get("depositamount").toString()));
            }

            if (hm.containsKey("depositamountinbase") && !StringUtil.isNullOrEmpty(hm.get("depositamountinbase").toString())) {
                payment.setDepositamountinbase(Double.parseDouble(hm.get("depositamountinbase").toString()));
            }
            if (hm.containsKey("externalCurrencyRate")) {
                payment.setExternalCurrencyRate((Double) hm.get("externalCurrencyRate"));
            }

            if (hm.containsKey("accountId")) {
                Account account = hm.get("accountId") == null ? null : (Account) get(Account.class, (String) hm.get("accountId"));
                payment.setAccount(account);
            }

            if (hm.containsKey("creationDate")) {
                payment.setCreationDate((Date) hm.get("creationDate"));
            }
            
            if (hm.containsKey("ispayment")) {
                payment.setIsPayment((Boolean) hm.get("ispayment"));
            }
            
            if (hm.containsKey("exchangeRateForOpeningTransaction")) {
                double exchangeRateForOpeningTransaction = (Double) hm.get("exchangeRateForOpeningTransaction");
                payment.setExchangeRateForOpeningTransaction(exchangeRateForOpeningTransaction);
            }
            
            if (hm.containsKey("conversionRateFromCurrencyToBase")) {
                payment.setConversionRateFromCurrencyToBase((Boolean) hm.get("conversionRateFromCurrencyToBase"));
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.savePayment : " + ex.getMessage(), ex);
        }
        return payment;
    }
    
    @Override
    public KwlReturnObject getAccountOpeningBalanceTransaction(HashMap<String, Object> request) throws ServiceException {
        List<AccountOpeningTransaction> list = null;

        int count = 0;
        String companyid = (String) request.get("companyid");
        String start = (String) request.get("start");
        String limit = (String) request.get("limit");
        String condition = "";
        ArrayList params = new ArrayList();

        params.add(companyid);

        if (request.containsKey("accountId") && request.get("accountId") != null) {
            String accountId = request.get("accountId").toString();
            condition += " AND r.account.ID=? ";
            params.add(accountId);
        }

        String query = "Select r from AccountOpeningTransaction r where r.company.companyID=?" + condition;
        list = executeQuery( query, params.toArray());
        count = list.size();
        if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
            list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    
    @Override
    public KwlReturnObject deleteAccountOpeningTransactionPemanently(String transactionID, String companyid) throws ServiceException {
        //Delete Journal Entry
        String delQuery = "delete from AccountOpeningTransaction aot where aot.ID = ? and aot.company.companyID=?";
        int numRows = executeUpdate(delQuery, new Object[]{transactionID, companyid});

        return new KwlReturnObject(true, "Account Opening Transaction has been deleted successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject deleteChequeNumberPemanently(String chequeid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String myquery = "select id from paydetail where cheque = ? and company=?";   // GET paydetail entries from chequeid
        list = executeSQLQuery(myquery,  new Object[]{chequeid, companyid});
        //Delete payDetail entry first
        String delQuery = "delete from PayDetail pd where pd.ID = ? and pd.company.companyID=?";
        int numRows = executeUpdate(delQuery, new Object[]{list.get(0), companyid});
        //Then Delete Cheque
        delQuery = "delete from Cheque cq where cq.ID = ? and cq.company.companyID=?";
        numRows = executeUpdate(delQuery, new Object[]{chequeid, companyid});

        return new KwlReturnObject(true, "Cheque Number has been deleted successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject updateChequeNumber(String oldchequenumber, String newchequenumber, String companyid) throws ServiceException {
        List list = new ArrayList();
        String myquery = "select id from cheque where chequeno = ? and company=?";   // GET chequeid  from cheque
        list = executeSQLQuery(myquery,  new Object[]{oldchequenumber, companyid});
        //Then update Cheque number
        String Query = "update cheque set chequeno = '"+newchequenumber+"' where id = ? and company = ?";
        int numRows = executeSQLUpdate(Query, new Object[]{list.get(0), companyid});

        return new KwlReturnObject(true, "Cheque Number has been updated successfully.", null, null, numRows);
    }  
    
    public KwlReturnObject getChequeIDByNumber(String chequenumber, String companyid) throws ServiceException {
        List list = new ArrayList();
        String myquery = "select id from cheque where chequeno = ? and company=?";   // GET chequeid  from cheque
        list = executeSQLQuery(myquery,  new Object[]{chequenumber, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     
    public KwlReturnObject getPayDetailIDByChequeID(String chequeid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String myquery = "select id from paydetail where cheque = ? and company=?";   // GET chequeid  from cheque
        list = executeSQLQuery(myquery,  new Object[]{chequeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
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
    /**
     * @param jeid ID for JournalEntry
     * @param companyid ID for Company
     * @return KwlReturnObject
     * @throws com.krawler.common.service.ServiceException
     * @description Function to get Payment using Journal Entry ID and company's ID.
     */
    public KwlReturnObject getPaymentMadeFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from Payment where journalEntry.ID=? and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject JEForPaymentOfImportServiceInvoices(String jeid, String companyid) throws ServiceException {        
        String selQuery = "from Payment P where P.importServiceJE.ID=?";
        List list = executeQuery( selQuery, new Object[]{jeid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public void updateAdvancePaymentTDSPaidFlag(HashMap reqparams){
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");    
        try {
            Date stDate = null,endDate = null;
            int tdspaidflag = 0;
            String setQuery = "", conditionQuery = "";
            List params = new ArrayList();
            
            if(reqparams.containsKey("startdate") && reqparams.get("startdate")!=null && !StringUtil.isNullOrEmpty(reqparams.get("startdate").toString())){
                stDate = sdf.parse(reqparams.get("startdate").toString());
            }
            if(reqparams.containsKey("enddate") && reqparams.get("enddate")!=null && !StringUtil.isNullOrEmpty(reqparams.get("enddate").toString())){
                endDate = sdf.parse(reqparams.get("enddate").toString());
            }
            if(reqparams.containsKey("tdspaidflag") && reqparams.get("tdspaidflag")!=null && !StringUtil.isNullOrEmpty(reqparams.get("tdspaidflag").toString())){
                tdspaidflag = Integer.parseInt(reqparams.get("tdspaidflag").toString());
            }
            if (reqparams.containsKey("paymentid") && reqparams.get("paymentid") != null && !StringUtil.isNullOrEmpty(reqparams.get("paymentid").toString())) {
                if (tdspaidflag == IndiaComplianceConstants.TDSPAYMENT || tdspaidflag == IndiaComplianceConstants.TDSANDTDSINTERESTPAYMENT) {
                    setQuery += " advpay.tdspaidflag = 1, advpay.tdspayment='" + reqparams.get("paymentid").toString() + "' ";
                }
                if (tdspaidflag == IndiaComplianceConstants.TDSINTERESTPAYMENT || tdspaidflag == IndiaComplianceConstants.TDSANDTDSINTERESTPAYMENT) {
                    if (!StringUtil.isNullOrEmpty(setQuery)) {
                        setQuery += " , advpay.tdsinterestpaidflag = 1 , advpay.tdsinterestpayment='" + reqparams.get("paymentid").toString() + "' ";
                    } else {
                        setQuery += " advpay.tdsinterestpaidflag = 1 , advpay.tdsinterestpayment='" + reqparams.get("paymentid").toString() + "' ";
                    }
                    
                    // Freeze the TDS interest rate at time of Interest rate payment
                    if (reqparams.containsKey("tdsInterestRateAtPaymentTime") && !StringUtil.isNullOrEmpty(reqparams.get("tdsInterestRateAtPaymentTime").toString())) {
                        if (!StringUtil.isNullOrEmpty(setQuery)) {
                            setQuery += " ,tdsInterestRateAtPaymentTime = '" + reqparams.get("tdsInterestRateAtPaymentTime") + "' ";
                        } else {
                            setQuery += " tdsInterestRateAtPaymentTime = '" + reqparams.get("tdsInterestRateAtPaymentTime") + "' ";
                        }
                    }
                }
            }
            conditionQuery += " advpay.tdsamount > 0 ";
            if (tdspaidflag == IndiaComplianceConstants.TDSPAYMENT || tdspaidflag == IndiaComplianceConstants.TDSANDTDSINTERESTPAYMENT) {
                if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                    conditionQuery += " and ";
                }
                conditionQuery += " advpay.tdspayment IS NULL ";
            }
            if (tdspaidflag == IndiaComplianceConstants.TDSINTERESTPAYMENT || tdspaidflag == IndiaComplianceConstants.TDSANDTDSINTERESTPAYMENT) {
                if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                    conditionQuery += " and ";
                }
                conditionQuery += " advpay.tdsinterestpayment IS NULL ";
            }
            if(stDate!= null && endDate!= null){
                if(!StringUtil.isNullOrEmpty(conditionQuery)){
                    conditionQuery += " and ";
                }
                conditionQuery += " je.entrydate >= ? AND je.entrydate <=? ";
                params.add(stDate);
                params.add(endDate);
            }
            if(reqparams.containsKey("companyid") && reqparams.get("companyid")!=null && !StringUtil.isNullOrEmpty(reqparams.get("companyid").toString())){
                if(!StringUtil.isNullOrEmpty(conditionQuery)){
                    conditionQuery += " and ";
                }
                conditionQuery += " pay.company = ? ";
                params.add(reqparams.get("companyid"));
            }
            if (reqparams.containsKey("vendorId") && reqparams.get("vendorId") != null && !StringUtil.isNullOrEmpty(reqparams.get("vendorId").toString())) {
                if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                    conditionQuery += " and ";
                }
                conditionQuery += " vn.id = ? ";
                params.add(reqparams.get("vendorId"));
            }
            if (reqparams.containsKey("deducteecode") && reqparams.get("deducteecode") != null && !StringUtil.isNullOrEmpty(reqparams.get("deducteecode").toString())) {
                if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                    conditionQuery += " and ";
                }
                conditionQuery += " vn.deducteecode = ? ";
                params.add(reqparams.get("deducteecode"));
            }
            if (reqparams.containsKey("nop") && reqparams.get("nop") != null && !StringUtil.isNullOrEmpty(reqparams.get("nop").toString())) {
                if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                    conditionQuery += " and ";
                } 
                conditionQuery += " advpay.id in(select advancedetail from tdsdetails where natureofpayment = ?) ";
                params.add(reqparams.get("nop"));      
            }
            
            
            String query = "UPDATE advancedetail advpay "
                    + " INNER JOIN payment pay ON pay.id = advpay.payment "
                    + " INNER JOIN vendor vn ON pay.vendor = vn.id "
                    + " INNER JOIN journalentry je ON pay.journalentry = je.id ";
            if(!StringUtil.isNullOrEmpty(setQuery)){
                query += " SET " + setQuery ;
            }
             if(!StringUtil.isNullOrEmpty(conditionQuery)){
                query += " WHERE "+conditionQuery;
            }
            executeSQLUpdate(query,params.toArray());
            
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    @Override
    public void resetAdvancePaymentTDSPaidFlag(HashMap params){
        try {
            //To Update TDS Paid Flag.
            String whereClauseTDSQuery = "", setTDSQuery = "" , TDSquery ="";
            if (params.containsKey("paymentid")) {
                setTDSQuery = " SET tdspaidflag = 0 ,tdspayment=NULL ";
                whereClauseTDSQuery = " tdspayment='" + params.get("paymentid").toString() + "' ";
            }
            if(!StringUtil.isNullOrEmpty(setTDSQuery)){
                TDSquery = "UPDATE advancedetail  " + setTDSQuery + " WHERE " + whereClauseTDSQuery;
                executeSQLUpdate(TDSquery);
            }
            //To Update TDS Interest Paid Flag.
            String whereClauseTDSInterestQuery = "", setTDSInterestQuery = "" , TDSInterestquery ="";
            if (params.containsKey("paymentid")) {
                setTDSInterestQuery = " SET tdsinterestpaidflag = 0 ,tdsinterestpayment=NULL ";
                whereClauseTDSInterestQuery = " tdsinterestpayment='" + params.get("paymentid").toString() + "' ";
            }
            if(!StringUtil.isNullOrEmpty(setTDSInterestQuery)){
                TDSInterestquery = "UPDATE advancedetail  " + setTDSInterestQuery + " WHERE " + whereClauseTDSInterestQuery;
                executeSQLUpdate(TDSInterestquery);
            }
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    //To Verify whether Current Payment Record is of TDS Payment Record.
    public int isFromTDSPayment(HashMap requestParams) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String companyID = "", paymentID = "";
        String advanceDetailSelQuery = "", purchaseInvoiceSelQuery = "", conditionQuery = "";
        try {
            if (requestParams.containsKey("companyID") && !StringUtil.isNullOrEmpty(requestParams.get("companyID").toString())) {
                companyID = (String) requestParams.get("companyID");
            }
            if (requestParams.containsKey("paymentID") && !StringUtil.isNullOrEmpty(requestParams.get("paymentID").toString())) {
                paymentID = (String) requestParams.get("paymentID");
            }
            //IF TDS Payment is done that payment is saved in either GoodsReceipt table(columnName: tdspayment) or AdvanceDetail table(columnName: tdspayment). 
            if (!StringUtil.isNullOrEmpty(companyID) && !StringUtil.isNullOrEmpty(paymentID)) {
                //First Check in advancedetail table, whether this is a TDS Payment or not.
                advanceDetailSelQuery = "from AdvanceDetail ad where ad.company.companyID = ?  AND ad.tdsPayment = ?";
                List advanceDetailList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID});
                count = advanceDetailList.size();

                if (count <= 0) {
                    // In GoodsReceipt table, whether this is a TDS Payment or not.
                    purchaseInvoiceSelQuery = "from GoodsReceiptDetail pi where pi.goodsReceipt.company.companyID = ?  AND pi.tdsPayment = ?";
                    List purchaseInvoiceList = executeQuery(purchaseInvoiceSelQuery, new Object[]{companyID, paymentID});
                    count = purchaseInvoiceList.size();
                    
                    // Also Check whether it is TDS Interest Payment or not.
                    advanceDetailSelQuery = "from AdvanceDetail ad where ad.company.companyID = ?  AND ad.tdsInterestPayment = ?";
                    List advanceDetailInterestList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID});
                    count += advanceDetailInterestList.size();
                    
                    purchaseInvoiceSelQuery = "from ExpenseGRDetail pi where pi.goodsReceipt.company.companyID = ?  AND pi.tdsPayment = ?";
                    List purchaseInvoiceInterestList = executeQuery(purchaseInvoiceSelQuery, new Object[]{companyID, paymentID});
                    count += purchaseInvoiceInterestList.size();
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.isFromTaxPayment : " + ex.getMessage(), ex);
        }
        return count;
    }
    //To Calculate OverDueByMonths w.r.t. transaction date & as of date.
    public int CalculateOverDueByMonths(HashMap overDueByMonthsParams) throws ServiceException {
        int overdueMonths = 0, overdueDays = 0;
        String dueOn = "";
        Date transactionDate = null, asOfDate = null;
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MMM-dd");
            if (overDueByMonthsParams.containsKey("transactionDate")) {
                transactionDate = (Date) overDueByMonthsParams.get("transactionDate");
            }
            if (overDueByMonthsParams.containsKey("asOfDate")) {
                asOfDate = (Date) overDueByMonthsParams.get("asOfDate");
            }
            // Due On Date Calculation
            Calendar cal = Calendar.getInstance();
            Calendar dueCal = Calendar.getInstance();
            Date d = transactionDate;
            cal.setTime(d);
            int month = cal.get(Calendar.MONTH) + 1;
            if (cal.get(Calendar.MONTH) == Calendar.MARCH) { // i.e. month of March
                dueCal.set(cal.get(Calendar.YEAR), month, 30); // then due date is 30th of April
            } else {
                dueCal.set(cal.get(Calendar.YEAR), month, 7);
            }
            dueOn = sdf1.format(dueCal.getTime());
            
//            overdueDays = (int) ((new Date().getTime() - dueCal.getTimeInMillis()) / (1000 * 60 * 60 * 24));
            //Calculation of OverDueMonths.
            Calendar TransactionCal = Calendar.getInstance();//Transaction Date Calendar
            Calendar CurrentCal = Calendar.getInstance(); // Current Date Calendar
            TransactionCal.setTime(transactionDate);// set to Transaction Date
            Date dueOnDate = sdf1.parse(dueOn);
            CurrentCal.setTime(asOfDate); // Set to New Date
            if (asOfDate.after(dueOnDate)) {
                int diffYear = CurrentCal.get(Calendar.YEAR) - TransactionCal.get(Calendar.YEAR); // Current Date Year - Transaction Date Year
                int diffMonth = (CurrentCal.get(Calendar.MONTH)) - (TransactionCal.get(Calendar.MONTH)) + 1; // Current Date Month - Transaction Date Month
                //Calendar.getInstance().get(Calendar.MONTH); is "Zero Based" so adding + 1;
                overdueMonths = diffYear * 12 + diffMonth;
            }
            if (overdueMonths <= 0) {
                overdueMonths = 0;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.isFromTaxPayment : " + ex.getMessage(), ex);
        }
        return overdueMonths;
    }

    @Override
    public int getTDSPaymentType(HashMap requestParams) throws ServiceException {
        List list = new ArrayList();
        int count = 0, PaymentType = IndiaComplianceConstants.NOTDSPAID;
        String companyID = "", paymentID = "";
        String advanceDetailSelQuery = "", purchaseInvoiceSelQuery = "", conditionQuery = "";
        try {
            if (requestParams.containsKey("companyID") && !StringUtil.isNullOrEmpty(requestParams.get("companyID").toString())) {
                companyID = (String) requestParams.get("companyID");
            }
            if (requestParams.containsKey("paymentID") && !StringUtil.isNullOrEmpty(requestParams.get("paymentID").toString())) {
                paymentID = (String) requestParams.get("paymentID");
            }
            //IF TDS Payment is done that payment is saved in either GoodsReceipt table(columnName: tdspayment) or AdvanceDetail table(columnName: tdspayment). 
            if (!StringUtil.isNullOrEmpty(companyID) && !StringUtil.isNullOrEmpty(paymentID)) {
                //First Check in advancedetail table, whether this is a TDS Payment or not.
                advanceDetailSelQuery = "from AdvanceDetail ad where ad.company.companyID = ?  AND ad.tdsPayment = ?";
                List advanceDetailList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID});
                count = advanceDetailList.size();
                if (count > 0) {
                    PaymentType = IndiaComplianceConstants.TDSPAYMENT;
                } 
                // Also Check whether it is TDS Interest Payment or not.
                advanceDetailSelQuery = "from AdvanceDetail ad where ad.company.companyID = ?  AND ad.tdsInterestPayment = ?";
                List advanceDetailInterestList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID});
                count = advanceDetailInterestList.size();
                if (count > 0) {
                    PaymentType += IndiaComplianceConstants.TDSINTERESTPAYMENT;
                } 
                if (count == 0) {
                    //First Check in advancedetail table, whether this is a TDS Payment or not.
                    advanceDetailSelQuery = "from GoodsReceiptDetail ad where ad.goodsReceipt.company.companyID = ?  AND ad.tdsPayment = ?";
                    List GRList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID});
                    count = GRList.size();
                    if (count > 0) {
                        PaymentType = IndiaComplianceConstants.TDSPAYMENT;
                    }

                    // Also Check whether it is TDS Interest Payment or not.
                    advanceDetailSelQuery = "from GoodsReceiptDetail ad where ad.goodsReceipt.company.companyID = ?  AND ad.tdsInterestPayment = ?";
                    List GRInterestList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID});
                    count = GRInterestList.size();
                    if (count > 0) {
                        PaymentType += IndiaComplianceConstants.TDSINTERESTPAYMENT;
                    }
                }    
                if (count == 0) {    
                    //First Check in advancedetail table, whether this is a TDS Payment or not.
                    advanceDetailSelQuery = "from ExpenseGRDetail ad where ad.goodsReceipt.company.companyID = ?  AND ad.tdsPayment = ?";
                    List expenseGRList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID});
                    count = expenseGRList.size();
                    if (count > 0) {
                        PaymentType = IndiaComplianceConstants.TDSPAYMENT;
                    }

                    // Also Check whether it is TDS Interest Payment or not.
                    advanceDetailSelQuery = "from ExpenseGRDetail ad where ad.goodsReceipt.company.companyID = ?  AND ad.tdsInterestPayment = ?";
                    List expenseGRInterestList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID});
                    count = expenseGRInterestList.size();
                    if (count > 0) {
                        PaymentType += IndiaComplianceConstants.TDSINTERESTPAYMENT;
                    }
                }
                if (count == 0) {
                    //First Check in advancedetail table, whether this is a TDS Payment or not.
                    advanceDetailSelQuery = "from DebitNote dn where dn.company.companyID = ?  AND dn.tdsPayment = ?";
                    List DNList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID});
                    count = DNList.size();
                    if (count > 0) {
                        PaymentType = IndiaComplianceConstants.TDSPAYMENT;
                    }

                    // Also Check whether it is TDS Interest Payment or not.
                    advanceDetailSelQuery = "from DebitNote dn where dn.company.companyID = ?  AND dn.tdsInterestPayment = ?";
                    List DNInterestList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID});
                    count = DNInterestList.size();
                    if (count > 0) {
                        PaymentType += IndiaComplianceConstants.TDSINTERESTPAYMENT;
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getTDSPaymentType : " + ex.getMessage(), ex);
        }
        return PaymentType;
    }
    public String getTDSPaymentNOP(HashMap requestParams) throws ServiceException {
        int count = 0;
        String companyID = "", paymentID = "";
        String advanceDetailSelQuery = "";
        String nopValue = "";
        try {
            if (requestParams.containsKey("companyID") && !StringUtil.isNullOrEmpty(requestParams.get("companyID").toString())) {
                companyID = (String) requestParams.get("companyID");
            }
            if (requestParams.containsKey("paymentID") && !StringUtil.isNullOrEmpty(requestParams.get("paymentID").toString())) {
                paymentID = (String) requestParams.get("paymentID");
            }
            if (!StringUtil.isNullOrEmpty(companyID) && !StringUtil.isNullOrEmpty(paymentID)) {
                advanceDetailSelQuery = "from AdvanceDetail ad where ad.company.companyID = ?  AND (ad.tdsInterestPayment = ? OR ad.tdsPayment = ?)";
                List<AdvanceDetail> advanceDetailList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID, paymentID});
                count = advanceDetailList.size();
                if (count > 0) {
                    for (AdvanceDetail adv : advanceDetailList) {
                        Set<TdsDetails> tdslist = adv.getTdsdetails();
                        for (TdsDetails tds : tdslist) {
                            if(tds.getNatureOfPayment()!=null) {
                                nopValue = tds.getNatureOfPayment().getValue();
                            }
                            return nopValue;
                        }
                    }
                }
                
                advanceDetailSelQuery = "from GoodsReceiptDetail ad where ad.goodsReceipt.company.companyID = ?  AND (ad.tdsPayment = ? OR ad.tdsInterestPayment = ?)";
                List<GoodsReceiptDetail> GRList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID, paymentID});
                count = GRList.size();
                if (count > 0) {
                    for (GoodsReceiptDetail grd : GRList) {
                        if(grd.getNatureOfPayment()!=null){
                            nopValue = grd.getNatureOfPayment().getValue();
                        }
                        return nopValue;
                    }
                }

                advanceDetailSelQuery = "from ExpenseGRDetail ad where ad.goodsReceipt.company.companyID = ?  AND (ad.tdsPayment = ? OR ad.tdsInterestPayment = ?)";
                List<ExpenseGRDetail> expenseGRList = executeQuery(advanceDetailSelQuery, new Object[]{companyID, paymentID, paymentID});
                count = expenseGRList.size();
                if (count > 0) {
                    for (ExpenseGRDetail expd : expenseGRList) {
                        if(expd.getNatureOfPayment()!=null){
                            nopValue = expd.getNatureOfPayment().getValue();
                        }
                        return nopValue;
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getTDSPaymentType : " + ex.getMessage(), ex);
        }
        return nopValue;
    }

    /**
     * Method is used to line level terms and advance payment detail mapping. It
     * is used for INDIAN country for GST taxes.
     * @param dataMap
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject saveAdvanceDetailsTermMap(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            AdvanceDetailTermMap termmap = new AdvanceDetailTermMap();
            if (dataMap.containsKey("id") && !StringUtil.isNullOrEmpty(dataMap.get("id").toString())) {
                termmap = (AdvanceDetailTermMap) get(AdvanceDetailTermMap.class, (String) dataMap.get("id"));
                if (termmap == null) {
                    termmap = new AdvanceDetailTermMap();
                }
            }
            if (dataMap.containsKey("termamount") && !StringUtil.isNullOrEmpty(dataMap.get("termamount").toString())) {
                termmap.setTermamount(Double.parseDouble(dataMap.get("termamount").toString()));
            }
            if (dataMap.containsKey("termpercentage")) {
                termmap.setPercentage(Double.parseDouble(dataMap.get("termpercentage").toString()));
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
            if (dataMap.containsKey("advanceDetail") && dataMap.get("advanceDetail") != null) {
                termmap.setAdvanceDetail((String) dataMap.get("advanceDetail"));
            }
            if (dataMap.containsKey("paymentdetailotherwiseid") && dataMap.get("paymentdetailotherwiseid") != null) {
                termmap.setPaymentDetailOtherwise((String) dataMap.get("paymentdetailotherwiseid"));
            }
            saveOrUpdate(termmap);
            list.add(termmap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.saveAdvanceDetailsTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    @Override
    public KwlReturnObject getAdvanceDetailsTerm(JSONObject paramObj) throws ServiceException {
        List params = new ArrayList();
        StringBuilder query = new StringBuilder();
        query.append(" from AdvanceDetailTermMap rm ");
        if (!StringUtil.isNullOrEmpty(paramObj.optString("adId"))) {
            /**
             * term data stored against Advance
             */
            query.append(" where rm.advanceDetail = ?");
            params.add(paramObj.optString("adId"));
        }
        if (!StringUtil.isNullOrEmpty(paramObj.optString("otherwiseid"))) {
            /**
             * Term data stored against GL code
             */
            query.append(" where rm.paymentDetailOtherwise = ?");
            params.add(paramObj.optString("otherwiseid"));
        }
        List list = executeQuery(query.toString(), params.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    @Override
    public void deleteAdvanceDetailsTerm(JSONObject reqParams) throws ServiceException {
        List params = new ArrayList();
        StringBuilder query = new StringBuilder();
        query.append("delete from AdvanceDetailTermMap rm ");
        if (reqParams.has("advanceDetailid")) {
            /**
             * term data stored against Advance
             */
            query.append(" where rm.advanceDetail = ?");
            params.add(reqParams.optString("advanceDetailid"));
        } 
        if (reqParams.has("paymentdetailotherwiseid")) {
            /**
             * Term data stored against GL code
             */
            query.append(" where rm.paymentDetailOtherwise in (?)");
            params.add(reqParams.optString("paymentdetailotherwiseid"));
        }
        executeUpdate(query.toString(), params.toArray());
    }

}
