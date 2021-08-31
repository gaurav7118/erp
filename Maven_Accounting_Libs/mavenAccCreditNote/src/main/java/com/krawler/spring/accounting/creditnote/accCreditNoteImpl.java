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
package com.krawler.spring.accounting.creditnote;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAOImpl;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.creditnote.dm.CreditNoteInfo;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.accInvoiceImpl;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
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
import org.hibernate.Session;

/**
 *
 * @author krawler
 */
public class accCreditNoteImpl extends BaseDAO implements accCreditNoteDAO {

    private accBankReconciliationDAO accBankReconciliationDAOObj;
    
    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationDAOObj) {
        this.accBankReconciliationDAOObj = accBankReconciliationDAOObj;
    }
    
     @Override
    public KwlReturnObject addCreditNote(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            CreditNote cn = new CreditNote();
            String companyid = "";
            if (hm.containsKey("companyid")) {
                companyid = (String) hm.get("companyid");
            }
            cn.setDeleted(false);
            if (hm.containsKey("createdby")) {
                User createdby = hm.get("createdby") == null ? null : (User) get(User.class, (String) hm.get("createdby"));
                cn.setCreatedby(createdby);
            }
            if (hm.containsKey("modifiedby")) {
                User modifiedby = hm.get("modifiedby") == null ? null : (User) get(User.class, (String) hm.get("modifiedby"));
                cn.setModifiedby(modifiedby);
            }
            if (hm.containsKey("taxid")) {
                cn.setTax((Tax) get(Tax.class, (String)hm.get("taxid")));
            }
            if (hm.containsKey("createdon")) {
                cn.setCreatedon((Long) hm.get("createdon"));
            }
            if (hm.containsKey("updatedon")) {
                cn.setUpdatedon((Long) hm.get("updatedon"));
            }
            if (hm.containsKey(Constants.SEQFORMAT)) {
                cn.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) hm.get(Constants.SEQFORMAT)));
            }
            if (hm.containsKey(Constants.SEQNUMBER)  && hm.get(Constants.SEQNUMBER)!=null && !StringUtil.isNullOrEmpty(hm.get(Constants.SEQNUMBER).toString())) {
                cn.setSeqnumber(Integer.parseInt(hm.get(Constants.SEQNUMBER).toString()));
            }
            if (hm.containsKey(Constants.DATEPREFIX) && hm.get(Constants.DATEPREFIX) !=null) {
                cn.setDatePreffixValue((String)hm.get(Constants.DATEPREFIX));
            }
            if (hm.containsKey(Constants.DATEAFTERPREFIX) && hm.get(Constants.DATEAFTERPREFIX) !=null) {
                cn.setDateAfterPreffixValue((String)hm.get(Constants.DATEAFTERPREFIX));
            }
            if (hm.containsKey(Constants.DATESUFFIX) && hm.get(Constants.DATESUFFIX) !=null) {
                cn.setDateSuffixValue((String)hm.get(Constants.DATESUFFIX));
            }
            if (hm.containsKey("entrynumber")) {
                cn.setCreditNoteNumber((String) hm.get("entrynumber"));
            }
            if (hm.containsKey("currencyid")) {
                KWLCurrency currency = hm.get("currencyid") == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) hm.get("currencyid"));
                cn.setCurrency(currency);
            }
            if (hm.containsKey("autogenerated")) {
                cn.setAutoGenerated((Boolean) hm.get("autogenerated"));
            }
            if (hm.containsKey("memo")) {
                cn.setMemo((String) hm.get("memo"));
            }
            
            if (hm.containsKey("includingGST")) {
                cn.setIncludingGST((Boolean) hm.get("includingGST"));
            }
            
            if (hm.containsKey("sequence") && hm.get("sequence")!=null && !StringUtil.isNullOrEmpty(hm.get("sequence").toString())) {
                cn.setSequence((Integer) hm.get("sequence"));
            }
            if (hm.containsKey("companyid")) {
                Company company = hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid"));
                cn.setCompany(company);
            }
            if (hm.containsKey("journalentryid")) {
                JournalEntry je = hm.get("journalentryid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) hm.get("journalentryid"));
                cn.setJournalEntry(je);
            }
            if (hm.containsKey("customerentry")) {
                JournalEntryDetail je = hm.get("customerentry") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("customerentry"));
                cn.setCustomerEntry(je);
            }
            if (hm.containsKey("cndetails")) {
                cn.setRows((Set<CreditNoteDetail>) hm.get("cndetails"));
            }
            if (hm.containsKey("customerid")) {
                Customer customer = hm.get("customerid") == null ? null : (Customer) get(Customer.class, (String) hm.get("customerid"));
                cn.setCustomer(customer);
            }
            if (hm.containsKey("vendorid")) {
                Vendor vendor = hm.get("vendorid") == null ? null : (Vendor) get(Vendor.class, (String) hm.get("vendorid"));
                cn.setVendor(vendor);
            }
            if (hm.containsKey("otherwise")) {
                cn.setOtherwise((Boolean) hm.get("otherwise"));
            }
            if (hm.containsKey("oldRecord")) {
                cn.setOldRecord((Boolean) hm.get("oldRecord"));
            }
            if (hm.containsKey("openflag")) {
                cn.setOpenflag((Boolean) hm.get("openflag"));
            }
            if (hm.containsKey("cnamount") && hm.get("cnamount") != null) {
                cn.setCnamount(Double.parseDouble(hm.get("cnamount").toString()));
            }
            if (hm.containsKey("cnamountdue") && hm.get("cnamountdue") != null) {
                cn.setCnamountdue(Double.parseDouble(hm.get("cnamountdue").toString()));
            }
            if (hm.containsKey("narrationValue") && hm.get("narrationValue") != null) {//
                cn.setNarration(hm.get("narrationValue").toString());
            }
            if (hm.containsKey("creationDate") && hm.get("creationDate") != null) {//
                cn.setCreationDate((Date) hm.get("creationDate"));
            }
            if (hm.containsKey("isOpeningBalenceCN")) {//
                boolean isOpeningBalenceCN = hm.get("isOpeningBalenceCN")!= null ? Boolean.parseBoolean(hm.get("isOpeningBalenceCN").toString()) : false;
                cn.setIsOpeningBalenceCN(isOpeningBalenceCN);
                if (isOpeningBalenceCN) {
                    cn.setApprovestatuslevel(11);
                }
            } else {
                cn.setIsOpeningBalenceCN(false);
            }
            if (hm.containsKey("isCNForCustomer")) {//
                cn.setcNForCustomer((Boolean) hm.get("isCNForCustomer"));
            }
            if (hm.containsKey("normalCN")) {//
                cn.setNormalCN((Boolean) hm.get("normalCN"));
            } else {
                cn.setNormalCN(true);
            }
            if (hm.containsKey("openingBalanceAmountDue") && hm.get("openingBalanceAmountDue") != null) {//
                cn.setOpeningBalanceAmountDue(Double.parseDouble(hm.get("openingBalanceAmountDue").toString()));
            }
            if (hm.containsKey(Constants.openingBalanceBaseAmountDue) && hm.get(Constants.openingBalanceBaseAmountDue) != null) {//
                cn.setOpeningBalanceBaseAmountDue(authHandler.round(Double.parseDouble(hm.get(Constants.openingBalanceBaseAmountDue).toString()),companyid));
            }
            if (hm.containsKey(Constants.originalOpeningBalanceBaseAmount) && !StringUtil.isNullOrEmpty(hm.get(Constants.originalOpeningBalanceBaseAmount).toString())) {
                cn.setOriginalOpeningBalanceBaseAmount(authHandler.round(Double.parseDouble(hm.get(Constants.originalOpeningBalanceBaseAmount).toString()), companyid));
            }
            if (hm.containsKey("exchangeRateForOpeningTransaction")) {//
                double exchangeRateForOpeningTransaction = (Double) hm.get("exchangeRateForOpeningTransaction");
                cn.setExchangeRateForOpeningTransaction(exchangeRateForOpeningTransaction);
            }
            if (hm.containsKey("conversionRateFromCurrencyToBase")) {//
                cn.setConversionRateFromCurrencyToBase((Boolean) hm.get("conversionRateFromCurrencyToBase"));
            }
            if (hm.containsKey("accountId") && hm.get("accountId") != null) {
                Account account = (Account) get(Account.class, (String) hm.get("accountId"));
                cn.setAccount(account);
            }
            if (hm.containsKey("salesreturnId")) {
                SalesReturn salesReturn = hm.get("salesreturnId") == null ? null : (SalesReturn) get(SalesReturn.class, (String) hm.get("salesreturnId"));
                cn.setSalesReturn(salesReturn);
            }

            if (hm.containsKey("cntype") && hm.get("cntype") != null) {
                cn.setCntype(Integer.parseInt(hm.get("cntype").toString()));
            }
            if (hm.containsKey("costcenter")) {
                CostCenter costcenter =StringUtil.isNullOrEmpty(hm.get("costcenter").toString())?null:(CostCenter) get(CostCenter.class, (String) hm.get("costcenter"));
                cn.setCostcenter(costcenter);
            }
            if (hm.containsKey("externalCurrencyRate")) {//
                double externalCurrencyRate = (Double) hm.get("externalCurrencyRate");
                cn.setExternalCurrencyRate(externalCurrencyRate);
            }
            if (hm.containsKey("cnamountinbase") && hm.get("cnamountinbase") != null) {
                cn.setCnamountinbase(Double.parseDouble(hm.get("cnamountinbase").toString()));
            }
            if (hm.containsKey("salesPersonID") && hm.get("salesPersonID")!=null) {
                MasterItem salesPerson = StringUtil.isNullOrEmpty(hm.get("salesPersonID").toString()) ? null : (MasterItem) get(MasterItem.class, (String) hm.get("salesPersonID"));
                cn.setSalesPerson(salesPerson);
            }
            if (hm.containsKey("masteragent") && hm.get("masteragent")!=null) {
                MasterItem masterAgent = StringUtil.isNullOrEmpty(hm.get("masteragent").toString()) ? null : (MasterItem) get(MasterItem.class, (String) hm.get("masteragent"));
                cn.setMasterAgent(masterAgent);
            }
            if (hm.containsKey("billshipAddressid") && hm.get("billshipAddressid") != null) {
                BillingShippingAddresses bsa = StringUtil.isNullOrEmpty(hm.get("billshipAddressid").toString()) ? null : (BillingShippingAddresses) get(BillingShippingAddresses.class, (String) hm.get("billshipAddressid"));
                cn.setBillingShippingAddresses(bsa);
            }
            if (hm.containsKey("approvestatuslevel") && hm.get("approvestatuslevel") != null) {
                 cn.setApprovestatuslevel(Integer.parseInt(hm.get("approvestatuslevel").toString()));
            } 
            if (hm.containsKey(Constants.MVATTRANSACTIONNO) && hm.get(Constants.MVATTRANSACTIONNO) != null) {
                 cn.setMvatTransactionNo(hm.get(Constants.MVATTRANSACTIONNO).toString());
            }
            if (hm.containsKey("gstCurrencyRate") && hm.get("gstCurrencyRate") != null) {
                 cn.setGstCurrencyRate(Double.parseDouble(hm.get("gstCurrencyRate").toString()));
            }
                        
            saveOrUpdate(cn);
            list.add(cn);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.addCreditNote : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Credit Note has been added successfully", null, list, list.size());
    }

     /**
     * Description : Method is used to Save saveCreditNoteGstDetails
     * Requisition
     *
     * @param <dataMap> :-Contains parameters company ID
     *
     * @return :return list
     */
     @Override
    public KwlReturnObject saveCreditNoteGstDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String srdid = (String) dataMap.get("id");

            CreditNoteAgainstVendorGst creditNoteAgainstVendorGst = new CreditNoteAgainstVendorGst();
            if (!StringUtil.isNullOrEmpty(srdid)) {
                creditNoteAgainstVendorGst = (CreditNoteAgainstVendorGst) get(CreditNoteAgainstVendorGst.class, srdid);
            }

            if (dataMap.containsKey("cnId")) {
                CreditNote creditNote = dataMap.get("cnId") == null ? null : (CreditNote) get(CreditNote.class, (String) dataMap.get("cnId"));
                creditNoteAgainstVendorGst.setCreditNote(creditNote);
            }
            if (dataMap.containsKey("srno")) {
                creditNoteAgainstVendorGst.setSrno((Integer) dataMap.get("srno"));
            }
            if (dataMap.containsKey("quantity")) {
                creditNoteAgainstVendorGst.setActualQuantity((Double) dataMap.get("quantity"));
            }
            if (dataMap.containsKey("returnquantity")) {
                creditNoteAgainstVendorGst.setReturnQuantity((Double) dataMap.get("returnquantity"));
            }
            if (dataMap.containsKey("uomid")) {
                creditNoteAgainstVendorGst.setUom((UnitOfMeasure) get(UnitOfMeasure.class, dataMap.get("uomid").toString()));
            }
            
            if (dataMap.containsKey("InvoiceDetail")) {
                creditNoteAgainstVendorGst.setVidetails((GoodsReceiptDetail) dataMap.get("InvoiceDetail"));
            }
            
            if (dataMap.containsKey("baseuomrate") && dataMap.get("baseuomrate") != null && dataMap.get("baseuomrate") != "") {
                creditNoteAgainstVendorGst.setBaseuomrate((Double) dataMap.get("baseuomrate"));
            }
            if (dataMap.containsKey("baseuomquantity") && dataMap.get("baseuomquantity") != null && dataMap.get("baseuomquantity") != "") {
                creditNoteAgainstVendorGst.setBaseuomquantity((Double) dataMap.get("baseuomquantity"));
            }
            if (dataMap.containsKey("baseuomreturnquantity") && dataMap.get("baseuomreturnquantity") != null && dataMap.get("baseuomreturnquantity") != "") {
                creditNoteAgainstVendorGst.setBaseuomquantity((Double) dataMap.get("baseuomreturnquantity"));
            }
            if (dataMap.containsKey("remark")) {
                creditNoteAgainstVendorGst.setRemark(StringUtil.DecodeText(StringUtil.isNullOrEmpty((String) dataMap.get("remark")) ? "" : (String) dataMap.get("remark")));
            }
            if (dataMap.containsKey("description")) {
                creditNoteAgainstVendorGst.setDescription(StringUtil.DecodeText((String) dataMap.get("description")));
            }
            if (dataMap.containsKey("productid")) {
                Product product = dataMap.get("productid") == null ? null : (Product) get(Product.class, (String) dataMap.get("productid"));
                creditNoteAgainstVendorGst.setProduct(product);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                creditNoteAgainstVendorGst.setCompany(company);
            }

            if (dataMap.containsKey("reason") && dataMap.get("reason") != null) {
                MasterItem masterItem = dataMap.get("reason") == null ? null : (MasterItem) get(MasterItem.class, (String) dataMap.get("reason"));
                creditNoteAgainstVendorGst.setReason(masterItem);
            }
            
            if (dataMap.containsKey("prtaxid") && dataMap.get("prtaxid") != null) {
                Tax tax = dataMap.get("prtaxid") == null ? null : (Tax) get(Tax.class, (String) dataMap.get("prtaxid"));
                creditNoteAgainstVendorGst.setTax(tax);
            }
            
            if (dataMap.containsKey("taxamount") && dataMap.get("taxamount") != null) {
                creditNoteAgainstVendorGst.setRowTaxAmount((Double) dataMap.get("taxamount"));
            }
            
            if (dataMap.containsKey("discount")) {
                creditNoteAgainstVendorGst.setDiscount((Double) dataMap.get("discount"));
            }
            
            if (dataMap.containsKey("discountispercent")) {
                creditNoteAgainstVendorGst.setDiscountispercent((Integer) dataMap.get("discountispercent"));
            }
            if (dataMap.containsKey("rate") && dataMap.get("rate") != null) {
                creditNoteAgainstVendorGst.setRate((double) dataMap.get("rate"));
            }
            if (dataMap.containsKey("salesInvoiceDetail") && dataMap.get("salesInvoiceDetail") != null) {
                creditNoteAgainstVendorGst.setInvoiceDetail((InvoiceDetail) dataMap.get("salesInvoiceDetail"));
            }
            saveOrUpdate(creditNoteAgainstVendorGst);
            list.add(creditNoteAgainstVendorGst);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("SaveCreditNoteAgainstVendorGst : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
     
    @Override
    public KwlReturnObject updateCreditNote(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String cnid = (String) hm.get("cnid");
            String companyid = "";
            CreditNote cn = (CreditNote) get(CreditNote.class, cnid);
            if (hm.containsKey("companyid")) {
                companyid = (String) hm.get("companyid");
            }
            if (cn != null) {
                if (hm.containsKey("modifiedby")) {
                    User modifiedby = hm.get("modifiedby") == null ? null : (User) get(User.class, (String) hm.get("modifiedby"));
                    cn.setModifiedby(modifiedby);
                }
                if (hm.containsKey("updatedon")) {
                    cn.setUpdatedon((Long) hm.get("updatedon"));
                }
                if (hm.containsKey(Constants.SEQFORMAT)) {
                    cn.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) hm.get(Constants.SEQFORMAT)));
                }
                if (hm.containsKey("taxid")) {
                    cn.setTax((Tax) get(Tax.class, (String) hm.get("taxid")));
                }
                if (hm.containsKey(Constants.SEQNUMBER) && hm.get(Constants.SEQNUMBER)!=null && !StringUtil.isNullOrEmpty(hm.get(Constants.SEQNUMBER).toString())) {
                    cn.setSeqnumber(Integer.parseInt(hm.get(Constants.SEQNUMBER).toString()));
                }
                if (hm.containsKey(Constants.DATEPREFIX) && hm.get(Constants.DATEPREFIX) != null) {
                    cn.setDatePreffixValue((String) hm.get(Constants.DATEPREFIX));
                }
                if (hm.containsKey(Constants.DATEAFTERPREFIX) && hm.get(Constants.DATEAFTERPREFIX) != null) {
                    cn.setDateAfterPreffixValue((String) hm.get(Constants.DATEAFTERPREFIX));
                }
                if (hm.containsKey(Constants.DATESUFFIX) && hm.get(Constants.DATESUFFIX) != null) {
                    cn.setDateSuffixValue((String) hm.get(Constants.DATESUFFIX));
                }
                if (hm.containsKey("entrynumber")) {
                    cn.setCreditNoteNumber((String) hm.get("entrynumber"));
                }
                if (hm.containsKey("currencyid")) {
                    KWLCurrency currency = hm.get("currencyid") == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) hm.get("currencyid"));
                    cn.setCurrency(currency);
                }
                if (hm.containsKey("autogenerated")) {
                    cn.setAutoGenerated((Boolean) hm.get("autogenerated"));
                }
                if (hm.containsKey("memo")) {
                    cn.setMemo((String) hm.get("memo"));
                }
                if (hm.containsKey("includingGST")) {
                    cn.setIncludingGST((Boolean) hm.get("includingGST"));
                }
                
                if (hm.containsKey("sequence") && hm.get("sequence")!=null && !StringUtil.isNullOrEmpty(hm.get("sequence").toString())) {
                    cn.setSequence((Integer) hm.get("sequence"));
                }
                if (hm.containsKey("companyid")) {
                    Company company = hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid"));
                    cn.setCompany(company);
                }
                if (hm.containsKey("journalentryid")) {
                    JournalEntry je = hm.get("journalentryid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) hm.get("journalentryid"));
                    cn.setJournalEntry(je);
                }
                if (hm.containsKey("customerentry")) {
                    JournalEntryDetail je = hm.get("customerentry") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("customerentry"));
                    cn.setCustomerEntry(je);
                }
                if (hm.containsKey("cndetails")) {
                    cn.setRows((Set<CreditNoteDetail>) hm.get("cndetails"));
                }
                if (hm.containsKey("creditNoteTaxEntryDetails")) {
                    cn.setCnTaxEntryDetails((Set<CreditNoteTaxEntry>) hm.get("creditNoteTaxEntryDetails"));
                }
                if (hm.containsKey("customerid")) {
                    Customer customer = hm.get("customerid") == null ? null : (Customer) get(Customer.class, (String) hm.get("customerid"));
                    cn.setCustomer(customer);
                }
                if (hm.containsKey("vendorid")) {
                    Vendor vendor = hm.get("vendorid") == null ? null : (Vendor) get(Vendor.class, (String) hm.get("vendorid"));
                    cn.setVendor(vendor);
                }
                if (hm.containsKey("otherwise")) {
                    cn.setOtherwise((Boolean) hm.get("otherwise"));
                }
                if (hm.containsKey("openflag")) {
                    cn.setOpenflag((Boolean) hm.get("openflag"));
                }
                if (hm.containsKey("cnamount") && hm.get("cnamount") != null) {
                    cn.setCnamount(Double.parseDouble(hm.get("cnamount").toString()));
                }
                if (hm.containsKey("cnamountdue") && hm.get("cnamountdue") != null) {
                    cn.setCnamountdue(Double.parseDouble(hm.get("cnamountdue").toString()));
                }
                if (hm.containsKey("narrationValue") && hm.get("narrationValue") != null) {//
                    cn.setNarration(hm.get("narrationValue").toString());
                }
                if (hm.containsKey("creationDate") && hm.get("creationDate") != null) {//
                    cn.setCreationDate((Date) hm.get("creationDate"));
                }
                if (hm.containsKey("isOpeningBalenceCN")) {//
                    boolean isOpeningBalenceCN = hm.get("isOpeningBalenceCN")!= null ? Boolean.parseBoolean(hm.get("isOpeningBalenceCN").toString()) : false;
                    cn.setIsOpeningBalenceCN(isOpeningBalenceCN);
                    if (isOpeningBalenceCN) {
                        cn.setApprovestatuslevel(11);
                    }
                }
                if (hm.containsKey("isCNForCustomer")) {//
                    cn.setcNForCustomer((Boolean) hm.get("isCNForCustomer"));
                }
                if (hm.containsKey("normalCN")) {//
                    cn.setNormalCN((Boolean) hm.get("normalCN"));
                }
                if (hm.containsKey("openingBalanceAmountDue") && hm.get("openingBalanceAmountDue") != null) {//
                    cn.setOpeningBalanceAmountDue(Double.parseDouble(hm.get("openingBalanceAmountDue").toString()));
                }
                if (hm.containsKey("openingBalanceCreditNoteCustomData")) {
                    OpeningBalanceCreditNoteCustomData openingBalanceCreditNoteCustomData = hm.get("openingBalanceCreditNoteCustomData") == null ? null : (OpeningBalanceCreditNoteCustomData) get(OpeningBalanceCreditNoteCustomData.class, (String) hm.get("openingBalanceCreditNoteCustomData"));
                    cn.setOpeningBalanceCreditNoteCustomData(openingBalanceCreditNoteCustomData);
                }
                if (hm.containsKey(Constants.openingBalanceBaseAmountDue) && hm.get(Constants.openingBalanceBaseAmountDue) != null) {//
                    cn.setOpeningBalanceBaseAmountDue(authHandler.round(Double.parseDouble(hm.get(Constants.openingBalanceBaseAmountDue).toString()),companyid));
                }
                if (hm.containsKey(Constants.originalOpeningBalanceBaseAmount) && !StringUtil.isNullOrEmpty(hm.get(Constants.originalOpeningBalanceBaseAmount).toString())) {
                    cn.setOriginalOpeningBalanceBaseAmount(authHandler.round(Double.parseDouble(hm.get(Constants.originalOpeningBalanceBaseAmount).toString()), companyid));
                }
            
                if (hm.containsKey("exchangeRateForOpeningTransaction")) {//
                    double exchangeRateForOpeningTransaction = (Double) hm.get("exchangeRateForOpeningTransaction");
                    cn.setExchangeRateForOpeningTransaction(exchangeRateForOpeningTransaction);
                }
                if (hm.containsKey("conversionRateFromCurrencyToBase")) {//
                    cn.setConversionRateFromCurrencyToBase((Boolean) hm.get("conversionRateFromCurrencyToBase"));
                }
                if (hm.containsKey("accountId") && hm.get("accountId") != null) {
                    Account account = (Account) get(Account.class, (String) hm.get("accountId"));
                    cn.setAccount(account);
                }
                if (hm.containsKey("cntype") && hm.get("cntype") != null) {
                    cn.setCntype(Integer.parseInt(hm.get("cntype").toString()));
                }
                if (hm.containsKey(Constants.MARKED_PRINTED)) {
                    cn.setPrinted(Boolean.parseBoolean((String) hm.get(Constants.MARKED_PRINTED)));
                }
                if (hm.containsKey("salesreturnId")) {
                    SalesReturn salesReturn = hm.get("salesreturnId") == null ? null : (SalesReturn) get(SalesReturn.class, (String) hm.get("salesreturnId"));
                    cn.setSalesReturn(salesReturn);
                }
                if (hm.containsKey("costcenter")) {
                    CostCenter costcenter =StringUtil.isNullOrEmpty(hm.get("costcenter").toString())?null:(CostCenter) get(CostCenter.class, (String) hm.get("costcenter"));
                    cn.setCostcenter(costcenter);
                }
                if (hm.containsKey("externalCurrencyRate")) {//
                    double externalCurrencyRate = (Double) hm.get("externalCurrencyRate");
                    cn.setExternalCurrencyRate(externalCurrencyRate);
                }
                if (hm.containsKey("cnamountinbase") && hm.get("cnamountinbase") != null) {
                    cn.setCnamountinbase(Double.parseDouble(hm.get("cnamountinbase").toString()));
                }
                if (hm.containsKey("salesPersonID") && hm.get("salesPersonID")!=null) {
                    MasterItem salesPerson = StringUtil.isNullOrEmpty(hm.get("salesPersonID").toString()) ? null : (MasterItem) get(MasterItem.class, (String) hm.get("salesPersonID"));
                    cn.setSalesPerson(salesPerson);
                }
                if (hm.containsKey("masteragent") && hm.get("masteragent") != null) {
                    MasterItem masterAgent = StringUtil.isNullOrEmpty(hm.get("masteragent").toString()) ? null : (MasterItem) get(MasterItem.class, (String) hm.get("masteragent"));
                    cn.setMasterAgent(masterAgent);
                }
                if (hm.containsKey("billshipAddressid") && hm.get("billshipAddressid")!=null) {
                    BillingShippingAddresses bsa = StringUtil.isNullOrEmpty(hm.get("billshipAddressid").toString()) ? null : (BillingShippingAddresses) get(BillingShippingAddresses.class, (String) hm.get("billshipAddressid"));
                    cn.setBillingShippingAddresses(bsa);
                }
                if (hm.containsKey("approvestatuslevel") && hm.get("approvestatuslevel") != null) {
                    cn.setApprovestatuslevel(Integer.parseInt(hm.get("approvestatuslevel").toString()));
                } 
                if (hm.containsKey(Constants.MVATTRANSACTIONNO) && hm.get(Constants.MVATTRANSACTIONNO) != null) {
                    cn.setMvatTransactionNo(hm.get(Constants.MVATTRANSACTIONNO).toString());
                }
                if (hm.containsKey("gstCurrencyRate") && hm.get("gstCurrencyRate") != null) {
                    cn.setGstCurrencyRate(Double.parseDouble(hm.get("gstCurrencyRate").toString()));
                }
                saveOrUpdate(cn);
            }
            list.add(cn);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.updateCreditNote : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Credit Note has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject getCreaditNote(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String startstr = (String) request.get(Constants.start);
            String limitstr = (String) request.get(Constants.limit);
            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            boolean isIAF = request.get("isIAF") != null ? (Boolean) request.get("isIAF") : false;
            String ss = (String) request.get(Constants.ss);
            ArrayList params = new ArrayList();
            params.add((String) request.get(Constants.companyKey));
            String condition="";
            if (isIAF) {
                condition = " where cn.company.companyID=?";
            } else {
                condition = " where cn.company.companyID=?";        //ERP-12796
            }
            if (!StringUtil.isNullOrEmpty(ss)) {
                for (int i = 0; i <= 3; i++) {
                    params.add(ss + "%");
                }
                condition += " and ( cn.creditNoteNumber like ? or c.name like ? or cn.journalEntry.entryNumber like ? or cn.memo like ? ) ";
            }

            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and cn.journalEntry.ID IN(" + jeIds + ")";
            }

            String noteId = (String) request.get("noteid");
            if (!StringUtil.isNullOrEmpty(noteId)) {
                params.add(noteId);
                condition += " and cn.ID = ? ";
            }

            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and cn.journalEntry.costcenter.ID=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
//                condition += " and (cn.journalEntry.entryDate >=? and cn.journalEntry.entryDate <=?)";
                condition += " and (cn.creationDate >=? and cn.creationDate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if (nondeleted) {
                condition += " and cn.deleted=false ";
            } else if (deleted) {
                condition += " and cn.deleted=true ";
            }
            boolean isPartyEntry = false;
            if (request.containsKey("isPartyEntry")) {// party journal entry
                isPartyEntry = (Boolean) request.get("isPartyEntry");
                if (isPartyEntry) {
                    condition += " and je.typeValue=?";
                    params.add(2);
                }
            }
            boolean isPendingApproval = false;
            if (request.containsKey("pendingapproval") && request.get("pendingapproval") != null && !StringUtil.isNullOrEmpty(request.get("pendingapproval").toString())) {
                isPendingApproval = Boolean.parseBoolean(request.get("pendingapproval").toString());
            }
            if(isPendingApproval){
                condition += " and cn.approvestatuslevel != ? ";
                params.add(11);
            } else {
                condition += " and cn.approvestatuslevel = ? ";
                params.add(11);
            }
            
            String appendCase = "and";
            String mySearchFilterString = "";
            String joinString = "";
            HashMap<String, Object> reqParams1 = new HashMap<String, Object>();
            reqParams1.putAll(request);
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
                    reqParams1.put(Constants.Searchjson, Searchjson);
                    reqParams1.put(Constants.appendCase, appendCase);
                    reqParams1.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(reqParams1, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "cn.journalEntry.accBillInvCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//    
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            
            String query = "select cn, cn.customer, jed from CreditNote cn inner join cn.journalEntry je inner join je.details jed "+ condition + mySearchFilterString;  //inner join jed.account ac
            list = executeQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(startstr) == false && StringUtil.isNullOrEmpty(limitstr) == false) {
                int start = Integer.parseInt(startstr);
                int limit = Integer.parseInt(limitstr);
                list = executeQueryPaging( query, params.toArray(), new Integer[]{start, limit});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.getCreaditNote:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getCreaditNoteVendor(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String startstr = (String) request.get(Constants.start);
            String limitstr = (String) request.get(Constants.limit);
            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            boolean isIAF = request.get("isIAF") != null ? (Boolean) request.get("isIAF") : false;
            String ss = (String) request.get(Constants.ss);
            ArrayList params = new ArrayList();
            params.add((String) request.get(Constants.companyKey));
            String condition = "";
            if (isIAF) {
                condition = " where cn.company.companyID=?";
            } else {
                condition = " where cn.company.companyID=?";    //ERP-12796
            }
            if (!StringUtil.isNullOrEmpty(ss)) {
                for (int i = 0; i <= 3; i++) {
                    params.add(ss + "%");
                }
                condition += " and ( cn.creditNoteNumber like ? or c.name like ? or cn.journalEntry.entryNumber like ? or cn.memo like ? ) ";
            }

            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and cn.journalEntry.ID IN(" + jeIds + ")";
            }

            String noteId = (String) request.get("noteid");
            if (!StringUtil.isNullOrEmpty(noteId)) {
                params.add(noteId);
                condition += " and cn.ID = ? ";
            }

            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and cn.journalEntry.costcenter.ID=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
//                condition += " and (cn.journalEntry.entryDate >=? and cn.journalEntry.entryDate <=?)";
                condition += " and (cn.creationDate >=? and cn.creationDate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if (nondeleted) {
                condition += " and cn.deleted=false ";
            } else if (deleted) {
                condition += " and cn.deleted=true ";
            }
            
            boolean isPendingApproval = false;
            if (request.containsKey("pendingapproval") && request.get("pendingapproval") != null && !StringUtil.isNullOrEmpty(request.get("pendingapproval").toString())) {
                isPendingApproval = Boolean.parseBoolean(request.get("pendingapproval").toString());
            }
            if(isPendingApproval){
                condition += " and cn.approvestatuslevel != ? ";
                params.add(11);
            } else {
                condition += " and cn.approvestatuslevel = ? ";
                params.add(11);
            }
            
            String appendCase = "and";
            String mySearchFilterString = "";
            String joinString = "";
            HashMap<String, Object> reqParams1 = new HashMap<String, Object>();
            reqParams1.putAll(request);
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            boolean isPartyEntry = false;
            if (request.containsKey("isPartyEntry")) {// party journal entry
                isPartyEntry = (Boolean)request.get("isPartyEntry");
                if (isPartyEntry) {
                    condition += " and je.typeValue=?";
                    params.add(2);
                }
            }

            String Searchjson = "";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = request.get("searchJson").toString();

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    reqParams1.put(Constants.Searchjson, Searchjson);
                    reqParams1.put(Constants.appendCase, appendCase);
                    reqParams1.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(reqParams1, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "cn.journalEntry.accBillInvCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//      
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            
            String query = "select cn, cn.vendor, jed from CreditNote cn inner join cn.journalEntry je inner join je.details jed " + condition + mySearchFilterString; //inner join jed.account ac
            list = executeQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(startstr) == false && StringUtil.isNullOrEmpty(limitstr) == false) {
                int start = Integer.parseInt(startstr);
                int limit = Integer.parseInt(limitstr);
                list = executeQueryPaging( query, params.toArray(), new Integer[]{start, limit});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.getCreaditNote:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getCNFromInvoice(String invoiceid, String companyid) throws ServiceException {
        String selQuery = "from CreditNoteDetail cn  where cn.invoiceRow.invoice.ID=? and cn.creditNote.deleted=false and cn.company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{invoiceid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCNFromInvoiceOtherwise(String invoiceid, String companyid,boolean includeTempDeleted) throws ServiceException {
        String selQuery = "from CreditNoteDetail cn  where cn.invoice.ID=? and cn.company.companyID=?";
        if(!includeTempDeleted){        
            selQuery += "and cn.creditNote.deleted=false";
        }
        List list = executeQuery(selQuery, new Object[]{invoiceid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getCNDetailsFromOpeningBalanceInvoice(String invoiceid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "from CreditNoteDetail cn where cn.invoice.ID=? and cn.creditNote.deleted=false and cn.company.companyID=?";
        list = executeQuery( query, new Object[]{invoiceid, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getCNFromNoteNo(String noteno, String companyid) throws ServiceException {
        String selQuery = "from CreditNote where creditNoteNumber=? and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{noteno, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCNSequenceNo(String companyid, Date applydate) throws ServiceException {
        String selQuery = "select count(cn.ID) from CreditNote cn inner join cn.journalEntry je where cn.company.companyID=? and je.entryDate<=?";
        List list = executeQuery( selQuery, new Object[]{companyid, applydate});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getCNSequenceNofromsequenceformat(String companyid,String sequenceformatid) throws ServiceException {
        String selQuery = "select max(seqnumber) from CreditNote cn  where cn.company.companyID=?  and cn.seqformat.ID=?";
        List list = executeQuery( selQuery, new Object[]{companyid,sequenceformatid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCNRowsDiscountFromInvoice(String invoiceid) throws ServiceException {
        List list = new ArrayList();
        String query = "select cn, cnr, cnd from CreditNote cn left join cn.rows cnr left join cn.discounts cnd where cn.deleted=false and (cnr.invoiceRow.invoice.ID=? or cnd.invoice.ID=?) order by cn.sequence";
        list = executeQuery( query, new Object[]{invoiceid, invoiceid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getDistinctCNFromInvoice(String invoiceid) throws ServiceException {
        List list = new ArrayList();
        String query = "select distinct cn from CreditNote cn left join cn.rows cnr left join cn.discounts cnd where cn.deleted=false and (cnr.invoiceRow.invoice.ID=? or cnd.invoice.ID=?) order by cn.sequence";
        list = executeQuery( query, new Object[]{invoiceid, invoiceid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    //Get amount knock off using otherwise closed credit notes.
    public KwlReturnObject getCNRowsFromInvoice(String invoiceid) throws ServiceException {//Used to get otherwise credit notes applied to this invoice.
        List list = new ArrayList();
        String query = "select cn, cnr from CreditNote cn left join cn.rows cnr where cn.deleted=false and cnr.invoice.ID=? order by cn.sequence";
        list = executeQuery( query, new Object[]{invoiceid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getCNRowsFromInvoice(HashMap<String, Object> reqParams) throws ServiceException {
        List cnList = Collections.EMPTY_LIST;
        try {
            String condition = " where cnd.creditNote.deleted=false and cnd.invoice is not null";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) reqParams.get(Constants.df);
            if (reqParams.containsKey("invoiceid") && reqParams.get("invoiceid") != null) {
                String invoiceid = (String) reqParams.get("invoiceid");
                condition += " and cnd.invoice.ID=?";
                params.add(invoiceid);
            }
            if (reqParams.containsKey("companyid") && reqParams.get("companyid") != null) {
                String companyId = (String) reqParams.get("companyid");
                condition += " and cnd.company.companyID=?";
                params.add(companyId);
            }
            if (reqParams.containsKey("creditnoteid") && reqParams.get("creditnoteid") != null) {
                String cnid = (String) reqParams.get("creditnoteid");
                condition += " and cnd.creditNote.ID=? ";
                params.add(cnid);
            }

            if (reqParams.containsKey("asofdate") && reqParams.get("asofdate") != null) {
                String asOfDate = (String) reqParams.get("asofdate");
                condition += " and (cnd.invoiceLinkDate<=?) "; // For Normal CN creation date stored in journalentry's entry date
                params.add(df.parse(asOfDate));
            }
            String selQuery = "from CreditNoteDetail cnd " + condition;
            cnList = executeQuery( selQuery, params.toArray());
        } catch (ParseException ex) {
            Logger.getLogger(accCreditNoteImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, cnList, cnList.size());
    }
    @Override
    public KwlReturnObject getCNRowsFromDebitNote(HashMap<String, Object> reqParams) throws ServiceException {
        List cnList = Collections.EMPTY_LIST;
        try {
            String condition = " where cnd.creditNote.deleted=false and cnd.debitNoteId is not null";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) reqParams.get(Constants.df);
            String debitnoteid=null;
            if (reqParams.containsKey("debitnoteid") && reqParams.get("debitnoteid") != null) {
                debitnoteid = (String) reqParams.get("debitnoteid");
                condition += " and cnd.debitNoteId= ? ";
                params.add(debitnoteid);
            }
            if (reqParams.containsKey("companyid") && reqParams.get("companyid") != null) {
                String companyId = (String) reqParams.get("companyid");
                condition += " and cnd.company.companyID=?";
                params.add(companyId);
            }
            if (reqParams.containsKey("creditnoteid") && reqParams.get("creditnoteid") != null && debitnoteid ==null) {
                String cnid = (String) reqParams.get("creditnoteid");
                condition += " and cnd.creditNote.ID=? ";
                params.add(cnid);
            }

            if (reqParams.containsKey("asofdate") && reqParams.get("asofdate") != null) {
                String asOfDate = (String) reqParams.get("asofdate");
                condition += " and (cnd.invoiceLinkDate<=?) "; // For Normal CN creation date stored in journalentry's entry date
                params.add(df.parse(asOfDate));
            }
            String selQuery = "from CreditNoteDetail cnd " + condition;
            cnList = executeQuery( selQuery, params.toArray());
        } catch (ParseException ex) {
            Logger.getLogger(accCreditNoteImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, cnList, cnList.size());
    }
   
    @Override
    public Map<String, List<CreditNoteInfo>> getCNRowsInfoFromInvoice(List<String> invoiceIDLIST) throws ServiceException {//Used to get otherwise credit notes applied to this invoice.
        Map<String, List<CreditNoteInfo>> creditMap = new HashMap<String, List<CreditNoteInfo>>();
        if (invoiceIDLIST != null && !invoiceIDLIST.isEmpty()) {
            String query = "select cn, cnr, cnr.invoice.ID from CreditNote cn left join cn.rows cnr where cn.deleted=false "
                    + "and cnr.invoice.ID in (:invoiceIDList) order by cn.sequence";
            List<List> values = new ArrayList<List>();
            values.add(invoiceIDLIST);
            List<Object[]> results = executeCollectionQuery( query, Collections.singletonList("invoiceIDList"), values);

            if (results != null) {
                for (Object[] result : results) {
                    List<CreditNoteInfo> creditNoteInfoList = new ArrayList<CreditNoteInfo>();
                    String invID = (String) result[2];
                    CreditNoteInfo info = new CreditNoteInfo();
                    info.setCreditnote((CreditNote) result[0]);
                    info.setCreditNoteDetails((CreditNoteDetail) result[1]);
                    creditNoteInfoList.add(info);

                    if (creditMap.containsKey(invID)) {
                        List<CreditNoteInfo> creditNoteInfos = creditMap.get(invID);
                        creditNoteInfos.addAll(creditNoteInfoList);
                        creditMap.put(invID, creditNoteInfos);// if same invoice is being linked with credit note multiple times
                    } else {
                        creditMap.put(invID, creditNoteInfoList);// if same invoice is being linked with credit note multiple times
                    }
                }
            }
        }
        return creditMap;
    }
    
    //Get amount knock off using otherwise open credit notes.
    public KwlReturnObject getCNRowsOpen_customer(String customerid) throws ServiceException {//Used to get otherwise credit notes unused amount due.
        List list = new ArrayList();
        String query = "select cn from CreditNote cn where cn.deleted=false and cn.customer.ID=? and cn.openflag = true order by cn.sequence";
        list = executeQuery( query, new Object[]{customerid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    //Get amountof otherwise open debit notes.
    public KwlReturnObject getDNRowsOpen_customer(String customerid) throws ServiceException {//Used to get otherwise credit notes unused amount due.
        List list = new ArrayList();
        String query = "select cn from DebitNote cn where cn.deleted=false and cn.customer.ID=? and cn.openflag = true order by cn.sequence";
        list = executeQuery( query, new Object[]{customerid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCNRowsDiscountFromBillingInvoice(String invoiceid) throws ServiceException {
        List list = new ArrayList();
        String query = "select cn, cnr, cnd from BillingCreditNote cn left join cn.rows cnr left join cn.discounts cnd where cn.deleted=false and (cnr.invoiceRow.billingInvoice.ID=? or cnd.invoice.ID=?) order by cn.sequence";
        //String query="select cn, cnr, cnd from CreditNote cn left join cn.rows cnr left join cn.discounts cnd where cn.deleted=false and (cnr.invoiceRow.invoice.ID=? or cnd.invoice.ID=?) order by cn.sequence";
        list = executeQuery( query, new Object[]{invoiceid, invoiceid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCreditNoteDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from CreditNoteDetail";
        return buildNExecuteQuery(query, requestParams);
    }
    
    public KwlReturnObject getCreditNoteDetailsGst(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from CreditNoteAgainstVendorGst";
        return buildNExecuteQuery(query, requestParams);
    }

    public KwlReturnObject getCNDetailsCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from AccJEDetailCustomData";
        return buildNExecuteQuery( query, requestParams);
    }
    
    public KwlReturnObject getBillingCreditNoteDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from BillingCreditNoteDetail";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getCNFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from CreditNote where journalEntry.ID=? and deleted=false and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBCNFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from BillingCreditNote where journalEntry.ID=? and deleted=false and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getJEFromCN(String cnid) throws ServiceException {
        String selQuery = "select cn.journalEntry.ID from CreditNote cn where cn.ID=? and cn.company.companyID=cn.journalEntry.company.companyID";
        List list = executeQuery( selQuery, new Object[]{cnid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCNDFromCN(String cnid) throws ServiceException {
        String selQuery = "select cnd.discount.ID from CreditNoteDiscount cnd where cnd.creditNote.ID=? and cnd.company.companyID=cnd.discount.company.companyID";
        List list = executeQuery( selQuery, new Object[]{cnid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCNDFromCND(String cnid) throws ServiceException {
        String selQuery = "select cnd.discount.ID from CreditNoteDetail cnd where cnd.creditNote.ID=? and cnd.company.companyID=cnd.discount.company.companyID";
        List list = executeQuery( selQuery, new Object[]{cnid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCNIFromCND(String cnid) throws ServiceException {
        String selQuery = "select cnd.inventory.ID from CreditNoteDetail cnd where cnd.creditNote.ID=? and cnd.company.companyID=cnd.discount.company.companyID";
        List list = executeQuery( selQuery, new Object[]{cnid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject deleteCreditNote(String cnid, String companyid) throws ServiceException,AccountingException {
        String query = "update CreditNote set deleted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{cnid, companyid});
        return new KwlReturnObject(true, "Credit Note has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject deleteCreditNoteDetails(String cnid, String companyid) throws ServiceException {
        ArrayList params5 = new ArrayList();
        params5.add(companyid);
        params5.add(companyid);
        params5.add(cnid);
        String delQuery5 = "delete from accjedetailcustomdata where jedetailId in (select id from jedetail where company =? and journalEntry in (select journalentry from creditnote where company =? and id =?))";
        int numRows5 = executeSQLUpdate( delQuery5, params5.toArray());
        String query = "delete from CreditNoteDetail cnd where cnd.creditNote.ID=? and cnd.company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{cnid, companyid});
        return new KwlReturnObject(true, "Credit Note Detail has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject deleteCreditTaxDetails(String cnid, String companyid) throws ServiceException {
        String query = "delete from CreditNoteTaxEntry cnd where cnd.creditNote.ID=? and cnd.company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{cnid, companyid});
        return new KwlReturnObject(true, "Credit Note Tax Entry has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject deleteOpeningCreditNote(String cnid, String companyid) throws ServiceException {
        String query = "delete from CreditNote where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{cnid, companyid});
        return new KwlReturnObject(true, "Credit Note has been deleted successfully.", null, null, numRows);

    }

    @Override
    public KwlReturnObject getMakePaymentIdLinkedWithCreditNote(String noteId) throws ServiceException {
        List params = new ArrayList();
        params.add(noteId);
        String query = "select paymentid from creditnotpayment where cnid=?";
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCreditNoteIdFromPaymentId(String paymentid) throws ServiceException {
        List params = new ArrayList();
        params.add(paymentid);
        String query = "select id, cnid, description, totaljedid from creditnotpayment where paymentid=?";
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getInvoicesLinkedWithCreditNote(String noteId, String companyid) throws ServiceException {
        List params = new ArrayList();
        params.add(noteId);
        params.add(companyid);
        String query = "select invoice from cndetails where invoice IS NOT NULL and creditNote=? and company=?";
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
   @Override
    public KwlReturnObject getCreditNotelinkedInDebitNote(String creditNoteId, String companyId) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(creditNoteId);
        params.add(companyId);
        String query = "select count(dnd.id) from dndetails dnd where dnd.creditnoteid=? and dnd.company = ? ";

        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject deleteCreditNotesPermanent(HashMap<String, Object> requestParams) throws ServiceException,AccountingException {

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


//                ArrayList params = new ArrayList();
//                params.add(requestParams.get("companyid"));
                //   params.add(requestParams.get("invoiceid"));
//                delQuery = "delete  from inventory where company =?  and id in(" + idStrings + ") ";
//                int numRows = executeSQLUpdate( delQuery, params.toArray());
                int numRows=0;
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    String companyid = (String) requestParams.get("companyid");
                    String selQuery = "from Inventory where company.companyID = ? and  ID in (" + idStrings + ") ";
                    List resultList = executeQuery(selQuery, new Object[]{companyid});
                    Iterator itrInv = resultList.iterator();
                    while (itrInv.hasNext()) {
                        Inventory inventory = (Inventory) itrInv.next();
                        if (inventory != null && inventory.isDeleted() == false) {
                            if (inventory.isCarryIn()) {
                                inventory.getProduct().setAvailableQuantity(inventory.getProduct().getAvailableQuantity() - inventory.getBaseuomquantity());// minus Purchase and Plus Sales (for Reverse effect for quantity)
                            } else {
                                inventory.getProduct().setAvailableQuantity(inventory.getProduct().getAvailableQuantity() + inventory.getBaseuomquantity());
                            }
                        }
                    }
                    deleteAll(resultList);
                    numRows = resultList.size();
                }
                

                ArrayList params9 = new ArrayList();
                params9.add(requestParams.get("companyid"));
                params9.add(requestParams.get("cnid"));
                String myquery1 = " select journalentry from creditnote where company = ? and id=?";
                List list1 = executeSQLQuery( myquery1, params9.toArray());
                Iterator itr1 = list1.iterator();
                String journalent = "";
                String jeid = "";
                while (itr1.hasNext()) {
                    Object jeidobj = itr1.next();
                    String jeidi = (jeidobj != null) ? jeidobj.toString() : "";
                    journalent += "'" + jeidi + "',";
                    jeid += jeidi + ",";
                }
                if (!StringUtil.isNullOrEmpty(journalent)) {
                    journalent = journalent.substring(0, journalent.length() - 1);
                    jeid = jeid.substring(0, jeid.length() - 1);
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
                delQuery8 = "delete cndtm from creditnotedetailtermmap cndtm inner join cntaxentry cnt on cnt.id = cndtm.creditnotetaxentry where cnt.company =? and cnt.creditnote = ?";
                executeSQLUpdate(delQuery8, params11.toArray());
                
                deleteGstTaxClassDetails((String)requestParams.get("cnid"));
                deleteGstDocHistoryDetails((String)requestParams.get("cnid"));
                params11 = new ArrayList();
                params11.add(requestParams.get("companyid"));
                params11.add(requestParams.get("cnid"));
                delQuery8 = "delete  from cntaxentry  where company =? and creditnote= ?";
                int numRows8 = executeSQLUpdate( delQuery8, params11.toArray());
                
                ArrayList params12 = new ArrayList();
                String delQuery12 = "delete from openingbalancecreditnotecustomdata where openingbalancecreditnoteid=? ";
                params12.add(requestParams.get("cnid"));
                int numRows12 = executeSQLUpdate(delQuery12, params12.toArray());
                /**
                 * Need to delete mapping from debitnoteinvociemappinginfo. It
                 * is used to store debit note & goodsreceipt mapping for India
                 * country.
                 */
                ArrayList params6 = new ArrayList();
                params6.add(requestParams.get("cnid"));
                delQuery6 = "delete from creditnoteinvoicemappinginfo where creditnote = ?";
                executeSQLUpdate(delQuery6, params6.toArray());
                
                params6 = new ArrayList();
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
                    List list13 = new ArrayList();
                    ArrayList params13 = new ArrayList();
                    params13.add(requestParams.get("companyid"));
                    String myquery13 = "select bankReconciliation from bankreconciliationdetail where journalEntry in (" + journalent + ") and company=?";
                    if(!StringUtil.isNullOrEmpty(idStrings)){
                        list13 = executeSQLQuery(myquery13, params13.toArray());
                    }
                    Iterator itr13 = list13.iterator();
                    String bankrec = "";
                    while (itr13.hasNext()) {
                        Object bankrecobj = itr13.next();
                        String bankrecid = (bankrecobj != null) ? bankrecobj.toString() : "";
                        bankrec += "'" + bankrecid + "',";
                    }
                    if (!StringUtil.isNullOrEmpty(bankrec)) {
                        bankrec = bankrec.substring(0, bankrec.length() - 1);
                    }
                    /**
                     * delete unreconciled records in case of permanent delete.
                     */
                    requestParams.put("jeid", jeid);
                    accBankReconciliationDAOObj.deleteUnReconciliationRecords(requestParams);
                    
                    ArrayList params14 = new ArrayList();
                    params14.add(requestParams.get("companyid"));
                    String delquery14 = "delete from bankunreconciliationdetail where journalEntry in (" + journalent + ") and company = ?";
                    if (!StringUtil.isNullOrEmpty(idStrings)) {
                        int numRows14 = executeSQLUpdate(delquery14, params14.toArray());
                    }
                    if (!StringUtil.isNullOrEmpty(bankrec)) {
                        ArrayList params15 = new ArrayList();
                        params15.add(requestParams.get("companyid"));
                        String delQuery15 = "delete from bankreconciliation where id in (" + bankrec + ") and company =?";
                        int numRows15 = executeSQLUpdate(delQuery15, params15.toArray());
                    }
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

    public KwlReturnObject getBillingCreditNoteDet(String bInvid, String companyid) throws ServiceException {
        //"from BillingCreditNoteDetail dn  where dn.invoiceRow.billingInvoice.ID in ( "+qMarks +")  and dn.creditNote.deleted=false and dn.company.companyID=?";
        String query = "from BillingCreditNoteDetail dn  where dn.invoiceRow.billingInvoice.ID = ?  and dn.creditNote.deleted=false and dn.company.companyID=?";
        List list = executeQuery( query, new Object[]{bInvid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBCNFromNoteNo(String noteno, String companyid) throws ServiceException {
        String selQuery = "from BillingCreditNote where creditNoteNumber=? and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{noteno, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject geCreditNoteCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from AccJEDetailCustomData";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getBCNSequenceNo(String companyid, Date applydate) throws ServiceException {
        String selQuery = "select count(cn.ID) from BillingCreditNote cn inner join cn.journalEntry je  where cn.company.companyID=? and je.entryDate<=?";
        List list = executeQuery( selQuery, new Object[]{companyid, applydate});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject saveBillingCreditNote(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String cnid = (String) hm.get("id");
            BillingCreditNote cn = new BillingCreditNote();
            if (StringUtil.isNullOrEmpty(cnid)) {
                cn.setDeleted(false);
            } else {
                cn = (BillingCreditNote) get(BillingCreditNote.class, cnid);
            }

            if (hm.containsKey(Constants.SEQFORMAT)) {
                cn.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) hm.get(Constants.SEQFORMAT)));
            }
            if (hm.containsKey(Constants.SEQNUMBER)) {
                cn.setSeqnumber(Integer.parseInt(hm.get(Constants.SEQNUMBER).toString()));
            }
            if (hm.containsKey("entrynumber")) {
                cn.setCreditNoteNumber((String) hm.get("entrynumber"));
            }
            if (hm.containsKey("currencyid")) {
                KWLCurrency currency = hm.get("currencyid") == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) hm.get("currencyid"));
                cn.setCurrency(currency);
            }
            if (hm.containsKey("autogenerated")) {
                cn.setAutoGenerated((Boolean) hm.get("autogenerated"));
            }
            if (hm.containsKey("memo")) {
                cn.setMemo((String) hm.get("memo"));
            }
            if (hm.containsKey("sequence")) {
                cn.setSequence((Integer) hm.get("sequence"));
            }
            if (hm.containsKey("externalCurrencyRate")) {
                cn.setExternalCurrencyRate((Integer) hm.get("externalCurrencyRate"));
            }
            if (hm.containsKey("companyid")) {
                Company company = hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid"));
                cn.setCompany(company);
            }
            if (hm.containsKey("journalentryid")) {
                JournalEntry je = hm.get("journalentryid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) hm.get("journalentryid"));
                cn.setJournalEntry(je);
            }
            if (hm.containsKey("cndetails")) {
                cn.setRows((Set<BillingCreditNoteDetail>) hm.get("cndetails"));
            }
            saveOrUpdate(cn);
            list.add(cn);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.saveCreditNote : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Credit Note has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject getBillingCreaditNote(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String startstr = (String) request.get(Constants.start);
            String limitstr = (String) request.get(Constants.limit);
            String ss = (String) request.get(Constants.ss);
            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            ArrayList params = new ArrayList();
            params.add((String) request.get(Constants.companyKey));
            String condition = " where ac.ID=c.account.ID and cn.company.companyID=?";
            if (!StringUtil.isNullOrEmpty(ss)) {
                for (int i = 0; i <= 3; i++) {
                    params.add(ss + "%");
                }
                condition += " and ( cn.creditNoteNumber like ? or c.name like ? or cn.journalEntry.entryNumber like ? or cn.memo like ? ) ";
            }
            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and cn.journalEntry.costcenter.ID=?";
            }

            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and cn.journalEntry.ID IN(" + jeIds + ")";
            }

            String noteId = (String) request.get("noteid");
            if (!StringUtil.isNullOrEmpty(noteId)) {
                params.add(noteId);
                condition += " and cn.ID = ? ";
            }

            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
//                condition += " and (cn.journalEntry.entryDate >=? and cn.journalEntry.entryDate <=?)";
                condition += " and (cn.creationDate >=? and cn.creationDate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if (nondeleted) {
                condition += " and cn.deleted=false ";
            } else if (deleted) {
                condition += " and cn.deleted=true ";
            }
            String query = "select cn, c, jed from BillingCreditNote cn inner join cn.journalEntry je inner join je.details jed inner join jed.account ac, Customer c" + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(startstr) == false && StringUtil.isNullOrEmpty(limitstr) == false) {
                int start = Integer.parseInt(startstr);
                int limit = Integer.parseInt(limitstr);
                list = executeQueryPaging( query, params.toArray(), new Integer[]{start, limit});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.getBillingCreaditNote:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getJEFromBCN(String cnid) throws ServiceException {
        String selQuery = "select cn.journalEntry.ID from BillingCreditNote cn where cn.ID=? and cn.company.companyID=cn.journalEntry.company.companyID";
        List list = executeQuery( selQuery, new Object[]{cnid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCNDFromBCN(String cnid) throws ServiceException {
        String selQuery = "select cnd.discount.ID from BillingCreditNoteDiscount cnd where cnd.creditNote.ID=? and cnd.company.companyID=cnd.discount.company.companyID";
        List list = executeQuery( selQuery, new Object[]{cnid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCNDFromBCND(String cnid) throws ServiceException {
        String selQuery = "select cnd.discount.ID from BillingCreditNoteDetail cnd where cnd.creditNote.ID=? and cnd.company.companyID=cnd.discount.company.companyID";
        List list = executeQuery( selQuery, new Object[]{cnid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject deleteBillingCreditNote(String cnid, String companyid) throws ServiceException {
        String query = "update BillingCreditNote set deleted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{cnid, companyid});
        return new KwlReturnObject(true, "Billing Credit Note has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getTotalTax_TotalDiscount(String cnid) throws ServiceException {
        String selQuery = "select sum(taxAmount), sum(totalDiscount) from CreditNoteDetail where creditNote.ID = ?";
        List list = executeQuery( selQuery, new Object[]{cnid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getTotalTax_TotalDiscount_Billing(String cnid) throws ServiceException {
        String selQuery = "select sum(taxAmount), sum(totalDiscount) from BillingCreditNoteDetail where creditNote.ID = ?";
        List list = executeQuery( selQuery, new Object[]{cnid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     @Override
     public synchronized String updateCNEntryNumberForNA(String prid, String entrynumber) {
        try {
            String query = "update CreditNote set creditNoteNumber = ? where ID=?";
            int numRows = executeUpdate( query, new Object[]{entrynumber, prid});
        } catch (Exception e) {
            System.out.println(e);
        }
        return entrynumber;
    }
    public KwlReturnObject getCreditNotesForJE(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        String hqlQuery = "from CreditNote cn where cn.company.companyID=? and cn.journalEntry is not null";
        list = list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getOpeningBalanceCNs(HashMap<String, Object> request) throws ServiceException {
        List<CreditNote> list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalCNs = false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String start = (String) request.get("start");
        String limit = (String) request.get("limit");
        String customerid = (String) request.get("customerid");
        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.equalsIgnoreCase("All")) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }
        boolean isPendingApproval = false;
        if (request.containsKey("pendingapproval") && request.get("pendingapproval") != null && !StringUtil.isNullOrEmpty(request.get("pendingapproval").toString())) {
            isPendingApproval = Boolean.parseBoolean(request.get("pendingapproval").toString());
        }
        boolean isAgedReport = (request.containsKey("agedReport") && request.get("agedReport") != null) ? Boolean.parseBoolean(request.get("agedReport").toString()) : false;
        boolean isAgedSummary = (request.containsKey("isAgedSummary") && request.get("isAgedSummary") != null) ? Boolean.parseBoolean(request.get("isAgedSummary").toString()) : false;
        boolean isSOA = (request.containsKey("isSOA") && request.get("isSOA") != null) ? Boolean.parseBoolean(request.get("isSOA").toString()) : false;
        
        boolean onlyAmountDue = (request.containsKey("onlyAmountDue") && request.get("onlyAmountDue") != null) ? Boolean.parseBoolean(request.get("onlyAmountDue").toString()) : false;

        boolean isAccountCNs = false;
        if (request.containsKey("isAccountCNs") && request.get("isAccountCNs") != null) {
            isAccountCNs = (Boolean) request.get("isAccountCNs");
        }
        String newcustomerid = "";
        if (request.containsKey(Constants.newcustomerid) && request.get(Constants.newcustomerid) != null) {
            newcustomerid = (String) request.get(Constants.newcustomerid);
        }
        String newvendorid = "";
        if (request.containsKey(Constants.newvendorid) && request.get(Constants.newvendorid) != null) {
            newvendorid = (String) request.get(Constants.newvendorid);
        }
        try {
            String condition = "";
            ArrayList params = new ArrayList();

            params.add(companyid);
            
            if (!StringUtil.isNullOrEmpty(newcustomerid)) {
                if (newcustomerid.contains(",")) {
                    newcustomerid = AccountingManager.getFilterInString(newcustomerid);
                    condition += " and cn.customer.ID IN" + newcustomerid;
                } else {
                    params.add(newcustomerid);
                    condition += " and cn.customer.ID = ? ";
                }
            }
            if (!StringUtil.isNullOrEmpty(newvendorid)) {
                condition += " and cn.vendor.ID = ? ";
                params.add(newvendorid);
            }
            if (isAccountCNs && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                condition += " AND cn.account.ID=? ";
                params.add(accountId);
            } else if (!StringUtil.isNullOrEmpty(customerid)) {
                condition += " AND cn.customer.ID=? ";
                params.add(customerid);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND cn.customer.ID IN " + vendorIdGroup;
            }

            if (request.get("excludeNormal") != null) {
                excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
            }

            if (excludeNormal) {
                condition += " AND cn.normalCN=false ";
            }

            if (request.get("onlyOpeningNormalCNs") != null) {
                onlyOpeningNormalCNs = Boolean.parseBoolean(request.get("onlyOpeningNormalCNs").toString());
            }

            if (onlyOpeningNormalCNs) {
                condition += " AND cn.normalCN=true ";
            }
            
            if (onlyAmountDue) {
                condition += " AND cn.openingBalanceAmountDue > 0 ";
            }

            String currencyfilterfortrans = "";
            if (request.containsKey("currencyfilterfortrans")) {
                currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            }

            boolean isNoteForPayment = false;
            if (request.get("isNoteForPayment") != null) {
                isNoteForPayment = (Boolean) request.get("isNoteForPayment");
            }
            
            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)&&!isNoteForPayment) {
                params.add(currencyfilterfortrans);
                condition += " and cn.currency.currencyID = ? ";
            }
            
            if (request.containsKey("groupcombo") && request.get("groupcombo") != null && request.containsKey(Constants.globalCurrencyKey) && request.get(Constants.globalCurrencyKey) != null) {
                int groupcombo = (Integer) request.get("groupcombo");

                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    condition += " and cn.currency.currencyID=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                } else if (groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    condition += " and cn.currency.currencyID!=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                }
            }
            
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!(isAgedReport || isAgedSummary || isSOA)) { //in aged report all opening transactions are required so no need to give start and end date
                String startDate = (String) request.get(Constants.REQ_startdate);
                if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                    condition += " and (cn.creationDate >=? and cn.creationDate <=?) ";
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
            }else if (!isSOA) {
                if (request.containsKey("MonthlyAgeingEndDate") && request.get("MonthlyAgeingEndDate") != null) {
                    condition += " and cn.creationDate <=? ";
                    params.add(request.get("MonthlyAgeingEndDate"));
                } else if (!StringUtil.isNullOrEmpty(endDate)) {
                    condition += " and cn.creationDate <=? ";
                    params.add(df.parse(endDate));
                }
            }

            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"cn.customer.name","cn.customer.aliasname","cn.customer.acccode", "cn.creditNoteNumber", "cn.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 5); 
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND cn.customer IS NOT NULL ";
            }
            if (isPendingApproval) { // Get only pending approved records
                condition += " and cn.approvestatuslevel != ? ";
                params.add(11);
            }
            String appendCase = "and";
            String mySearchFilterString = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().trim().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String Searchjson = "";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = StringUtil.DecodeText(request.get("searchJson").toString());

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, appendCase);
                    request.put("isOpeningBalance",true);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "OpeningBalanceCreditNoteCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "cn.customer.accCustomerCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceCreditNoteCustomData", "cn.openingBalanceCreditNoteCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "cn.openingBalanceCreditNoteCustomData");
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    request.put("isOpeningBalance",false);
                }
            }
            if (!mySearchFilterString.contains("VendorCustomData")) {
                String query = "Select cn from CreditNote cn where cn.isOpeningBalenceCN=true AND cn.cNForCustomer=true AND cn.deleted=false AND cn.company.companyID=?" + condition + mySearchFilterString;
                list = executeQuery( query, params.toArray());
                count = list.size();
                if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                    list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getOpeningBalanceCNs : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    
    @Override
    public int getOpeningBalanceCNCount(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalCNs = false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String customerid = (String) request.get("customerid");
        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }
        boolean isAgedReport = (request.containsKey("agedReport") && request.get("agedReport") != null) ? Boolean.parseBoolean(request.get("agedReport").toString()) : false;
        boolean isAgedSummary = (request.containsKey("isAgedSummary") && request.get("isAgedSummary") != null) ? Boolean.parseBoolean(request.get("isAgedSummary").toString()) : false;

        boolean isAccountCNs = false;
        if (request.containsKey("isAccountCNs") && request.get("isAccountCNs") != null) {
            isAccountCNs = (Boolean) request.get("isAccountCNs");
        }
        try {
            String condition = "";
            ArrayList params = new ArrayList();

            params.add(companyid);

            if (isAccountCNs && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                condition += " AND cn.account.ID=? ";
                params.add(accountId);
            } else if (!StringUtil.isNullOrEmpty(customerid)) {
                condition += " AND cn.customer.ID=? ";
                params.add(customerid);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND cn.customer.ID IN " + vendorIdGroup;
            }

            if (request.get("excludeNormal") != null) {
                excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
            }

            if (excludeNormal) {
                condition += " AND cn.normalCN=false ";
            }

            if (request.get("onlyOpeningNormalCNs") != null) {
                onlyOpeningNormalCNs = Boolean.parseBoolean(request.get("onlyOpeningNormalCNs").toString());
            }

            if (onlyOpeningNormalCNs) {
                condition += " AND cn.normalCN=true ";
            }


            String currencyfilterfortrans = "";
            if (request.containsKey("currencyfilterfortrans")) {
                currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            }

            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
                params.add(currencyfilterfortrans);
                condition += " and cn.currency.currencyID = ? ";
            }
            if (!isAgedReport && !isAgedSummary) { //in aged report all opening transactions are required so no need to give start and end date
                String startDate = (String) request.get(Constants.REQ_startdate);
                String endDate = (String) request.get(Constants.REQ_enddate);
                if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                    condition += " and (cn.creationDate >=? and cn.creationDate <=?) ";
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
            }

            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"cn.customer.name","cn.customer.acccode", "cn.creditNoteNumber", "cn.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND cn.customer IS NOT NULL ";
            }
            String appendCase = "and";
            String mySearchFilterString = "";
//            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
//            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
//                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
//                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
//                }
//            }
//            String Searchjson = "";
//            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
//                Searchjson = request.get("searchJson").toString();
//
//                if (!StringUtil.isNullOrEmpty(Searchjson)) {
//                    request.put(Constants.Searchjson, Searchjson);
//                    request.put(Constants.appendCase, appendCase);
//                    request.put("isOpeningBalance",true);
//                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
//                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
//                    mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceCreditNoteCustomData", "cn.openingBalanceCreditNoteCustomData");
//                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
//                }
//            }
            String query = "Select count(cn.ID) from CreditNote cn where cn.isOpeningBalenceCN=true AND cn.cNForCustomer=true AND cn.deleted=false AND cn.company.companyID=?" + condition + mySearchFilterString;
            list = executeQuery( query, params.toArray());
            Long totalCnt = 0l;
            if (list != null && !list.isEmpty()){
                totalCnt = (Long) list.get(0);
            }
            count = totalCnt.intValue();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getOpeningBalanceCNCount : " + ex.getMessage(), ex);
        }
        return count;
    }
    
    @Override
    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForCNs(HashMap<String, Object> request) throws ServiceException {
        List<CreditNote> list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalCNs = false;
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
        boolean isAgedReport = (request.containsKey("agedReport") && request.get("agedReport") != null) ? Boolean.parseBoolean(request.get("agedReport").toString()) : false;
        boolean isAgedSummary = (request.containsKey("isAgedSummary") && request.get("isAgedSummary") != null) ? Boolean.parseBoolean(request.get("isAgedSummary").toString()) : false;

        boolean isAccountCNs = false;
        if (request.containsKey("isAccountCNs") && request.get("isAccountCNs") != null) {
            isAccountCNs = (Boolean) request.get("isAccountCNs");
        }
        try {
            String condition = "";
            ArrayList params = new ArrayList();

            params.add(companyid);

            if (isAccountCNs && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                condition += " AND cn.account.ID=? ";
                params.add(accountId);
            } else if (!StringUtil.isNullOrEmpty(customerid)) {
                condition += " AND cn.customer.ID=? ";
                params.add(customerid);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND cn.customer.ID IN " + vendorIdGroup;
            }

            if (request.get("excludeNormal") != null) {
                excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
            }

            if (excludeNormal) {
                condition += " AND cn.normalCN=false ";
            }

            if (request.get("onlyOpeningNormalCNs") != null) {
                onlyOpeningNormalCNs = Boolean.parseBoolean(request.get("onlyOpeningNormalCNs").toString());
            }

            if (onlyOpeningNormalCNs) {
                condition += " AND cn.normalCN=true ";
            }


            String currencyfilterfortrans = "";
            if (request.containsKey("currencyfilterfortrans")) {
                currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            }

            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
                params.add(currencyfilterfortrans);
                condition += " and cn.currency.currencyID = ? ";
            }
            if (!isAgedReport && !isAgedSummary) { //in aged report all opening transactions are required so no need to give start and end date
                String startDate = (String) request.get(Constants.REQ_startdate);
                String endDate = (String) request.get(Constants.REQ_enddate);
                if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                    condition += " and (cn.creationDate >=? and cn.creationDate <=?) ";
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
            }

            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"cn.customer.name","cn.customer.acccode", "cn.creditNoteNumber", "cn.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND cn.customer IS NOT NULL ";
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
                    request.put("isOpeningBalance",true);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceCreditNoteCustomData", "cn.openingBalanceCreditNoteCustomData");
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            String query = "Select COALESCE(SUM(cn.openingBalanceBaseAmountDue),0) from CreditNote cn where cn.isOpeningBalenceCN=true AND cn.cNForCustomer=true AND cn.deleted=false AND cn.company.companyID=?" + condition + mySearchFilterString;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getOpeningBalanceTotalBaseAmountDueForCNs : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    
    @Override
    public KwlReturnObject getOpeningBalanceTotalBaseAmountForCNs(HashMap<String, Object> request) throws ServiceException {
        List<CreditNote> list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalCNs = false;
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
        boolean isAgedReport = (request.containsKey("agedReport") && request.get("agedReport") != null) ? Boolean.parseBoolean(request.get("agedReport").toString()) : false;
        boolean isAgedSummary = (request.containsKey("isAgedSummary") && request.get("isAgedSummary") != null) ? Boolean.parseBoolean(request.get("isAgedSummary").toString()) : false;

        boolean isAccountCNs = false;
        if (request.containsKey("isAccountCNs") && request.get("isAccountCNs") != null) {
            isAccountCNs = (Boolean) request.get("isAccountCNs");
        }
        try {
            String condition = "";
            ArrayList params = new ArrayList();

            params.add(companyid);

            if (isAccountCNs && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                condition += " AND cn.account.ID=? ";
                params.add(accountId);
            } else if (!StringUtil.isNullOrEmpty(customerid)) {
                condition += " AND cn.customer.ID=? ";
                params.add(customerid);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND cn.customer.ID IN " + vendorIdGroup;
            }

            if (request.get("excludeNormal") != null) {
                excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
            }

            if (excludeNormal) {
                condition += " AND cn.normalCN=false ";
            }

            if (request.get("onlyOpeningNormalCNs") != null) {
                onlyOpeningNormalCNs = Boolean.parseBoolean(request.get("onlyOpeningNormalCNs").toString());
            }

            if (onlyOpeningNormalCNs) {
                condition += " AND cn.normalCN=true ";
            }


            String currencyfilterfortrans = "";
            if (request.containsKey("currencyfilterfortrans")) {
                currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            }

            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
                params.add(currencyfilterfortrans);
                condition += " and cn.currency.currencyID = ? ";
            }
            
            if (request.containsKey("groupcombo") && request.get("groupcombo") != null && request.containsKey(Constants.globalCurrencyKey) && request.get(Constants.globalCurrencyKey) != null) {
                int groupcombo = (Integer) request.get("groupcombo");

                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    condition += " and cn.currency.currencyID=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                }  else if(groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    condition += " and cn.currency.currencyID!=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                }
            }
            
            //For now this method get called from 2 places and from both places we does need to apply start date and end date. It is casusing issue like SDP-5696 So commenting below code
//            if (!isAgedReport && !isAgedSummary) { //in aged report all opening transactions are required so no need to give start and end date
//                String startDate = (String) request.get(Constants.REQ_startdate);
//                String endDate = (String) request.get(Constants.REQ_enddate);
//                if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
//                    condition += " and (cn.creationDate >=? and cn.creationDate <=?) ";
//                    params.add(df.parse(startDate));
//                    params.add(df.parse(endDate));
//                }
//            }

            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"cn.customer.name", "cn.customer.acccode", "cn.creditNoteNumber", "cn.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND cn.customer IS NOT NULL ";
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
                    request.put(Constants.moduleid, 12);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "cn.openingBalanceCreditNoteCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceCreditNoteCustomData", "cn.openingBalanceCreditNoteCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "cn.openingBalanceCreditNoteCustomData");
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            String query = "";
            if (request.containsKey("Searchjson") && request.get("Searchjson") != null && !StringUtil.isNullOrEmpty((String) request.get("Searchjson"))) {
                if (!StringUtil.isNullOrEmpty(mySearchFilterString) && !mySearchFilterString.equals(" ")) {
                    query = "select COALESCE(SUM(cn.originalOpeningBalanceBaseAmount),0) from CreditNote cn where cn.isOpeningBalenceCN=true AND cn.cNForCustomer=true AND cn.deleted=false AND cn.company.companyID=?" + condition + mySearchFilterString;
                    list = executeQuery( query, params.toArray());
                    count = list.size();
                }
            } else {
                query = "select COALESCE(SUM(cn.originalOpeningBalanceBaseAmount),0) from CreditNote cn where cn.isOpeningBalenceCN=true AND cn.cNForCustomer=true AND cn.deleted=false AND cn.company.companyID=?" + condition + mySearchFilterString;
                list = executeQuery( query, params.toArray());
                count = list.size();
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getOpeningBalanceCNs : " + ex.getMessage(), ex);
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
                requestParams.put("moduleid", 12);
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel));
                requestParams.put(Constants.filter_values, Arrays.asList(companyId, StringUtil.DecodeText(jobj1.getString("columnheader"))));
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
                    jobj.put("columnheader", StringUtil.DecodeText(jobj1.getString("columnheader")));
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
        }
        catch (Exception e) {
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
    public KwlReturnObject getOpeningBalanceVendorCNs(HashMap<String, Object> request) throws ServiceException {
        List<CreditNote> list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalCNs = false;
        boolean isAgedReport = (request.containsKey("agedReport") && request.get("agedReport") != null) ? Boolean.parseBoolean(request.get("agedReport").toString()) : false;
        boolean isAgedSummary = (request.containsKey("isAgedSummary") && request.get("isAgedSummary") != null) ? Boolean.parseBoolean(request.get("isAgedSummary").toString()) : false;
        
         boolean onlyAmountDue = (request.containsKey("onlyAmountDue") && request.get("onlyAmountDue") != null) ? Boolean.parseBoolean(request.get("onlyAmountDue").toString()) : false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String start = (String) request.get("start");
        String limit = (String) request.get("limit");
        String vendorid = (String) request.get("vendorid");

        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }

        boolean isAccountCNs = false;
        if (request.containsKey("isAccountCNs") && request.get("isAccountCNs") != null) {
            isAccountCNs = (Boolean) request.get("isAccountCNs");
        }
        String newcustomerid = "";
        if (request.containsKey(Constants.newcustomerid) && request.get(Constants.newcustomerid) != null) {
            newcustomerid = (String) request.get(Constants.newcustomerid);
        }
        String newvendorid = "";
        if (request.containsKey(Constants.newvendorid) && request.get(Constants.newvendorid) != null) {
            newvendorid = (String) request.get(Constants.newvendorid);
        }
        boolean isPendingApproval = false;
        if (request.containsKey("pendingapproval") && request.get("pendingapproval") != null && !StringUtil.isNullOrEmpty(request.get("pendingapproval").toString())) {
            isPendingApproval = Boolean.parseBoolean(request.get("pendingapproval").toString());
        }
        try {
            String condition = "";
            ArrayList params = new ArrayList();

            params.add(companyid);
            
            if (!StringUtil.isNullOrEmpty(newcustomerid)) {
                condition += " and cn.customer = ? ";
                params.add(newcustomerid);
            }
            
            if (!StringUtil.isNullOrEmpty(newvendorid)) {
                if (newvendorid.contains(",")) {
                    newvendorid = AccountingManager.getFilterInString(newvendorid);
                    condition += " and cn.vendor.ID IN" + newvendorid;
                } else {
                    params.add(newvendorid);
                    condition += " and cn.vendor.ID = ? ";

                }
            }
            
            if (isAccountCNs && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                condition += " AND cn.account.ID=? ";
                params.add(accountId);
            } else if (!StringUtil.isNullOrEmpty(vendorid)) {
                condition += " AND cn.vendor.ID=? ";
                params.add(vendorid);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND cn.vendor.ID IN " + vendorIdGroup;
            }

            if (request.get("excludeNormal") != null) {
                excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
            }

            if (excludeNormal) {
                condition += " AND cn.normalCN=false ";
            }

            if (request.get("onlyOpeningNormalCNs") != null) {
                onlyOpeningNormalCNs = Boolean.parseBoolean(request.get("onlyOpeningNormalCNs").toString());
            }

            if (onlyOpeningNormalCNs) {
                condition += " AND cn.normalCN=true ";
            }
            
              if (onlyAmountDue) {
                condition += " AND cn.openingBalanceAmountDue > 0 ";
            }

            String currencyfilterfortrans = "";
            if (request.containsKey("currencyfilterfortrans")) {
                currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            }

            boolean isNoteForPayment = false;
            if (request.get("isNoteForPayment") != null) {
                isNoteForPayment = (Boolean) request.get("isNoteForPayment");
            }
            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)&&!isNoteForPayment) {
                params.add(currencyfilterfortrans);
                condition += " and cn.currency.currencyID = ? ";
            }
            
            if (request.containsKey("groupcombo") && request.get("groupcombo") != null && request.containsKey(Constants.globalCurrencyKey) && request.get(Constants.globalCurrencyKey) != null) {
                int groupcombo = (Integer) request.get("groupcombo");
                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    condition += " and cn.currency.currencyID=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                } else if (groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    condition += " and cn.currency.currencyID!=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                }
            }
            
                String startDate = (String) request.get(Constants.REQ_startdate);
                String endDate = (String) request.get(Constants.REQ_enddate);
            
            if (!isAgedReport && !isAgedSummary) { //in aged report all opening transactions are required so no need to give start and end date
                if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                    condition += " and (cn.creationDate >=? and cn.creationDate <=?) ";
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
            } else {
                if (request.containsKey("MonthlyAgeingEndDate") && request.get("MonthlyAgeingEndDate") != null) {
                    condition += " and cn.creationDate <=? ";
                    params.add(request.get("MonthlyAgeingEndDate"));
                } else if (!StringUtil.isNullOrEmpty(endDate)) {
                    condition += " and cn.creationDate <=? ";
                    params.add(df.parse(endDate));
                }
            }

            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {

                String[] searchcol = new String[]{"cn.vendor.name","cn.vendor.aliasname","cn.vendor.acccode", "cn.creditNoteNumber", "cn.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 5);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND cn.vendor IS NOT NULL ";
            }
            String appendCase = "and";
            String mySearchFilterString = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().trim().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            if (isPendingApproval) { // Get only pending approved records
                condition += " and cn.approvestatuslevel != ? ";
                params.add(11);
            }
            String Searchjson = "";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = StringUtil.DecodeText(request.get("searchJson").toString());

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, appendCase);
                    request.put("isOpeningBalance",true);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "OpeningBalanceCreditNoteCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceCreditNoteCustomData", "cn.openingBalanceCreditNoteCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "cn.openingBalanceCreditNoteCustomData");
                    if (mySearchFilterString.contains("VendorCustomData")) {
                        mySearchFilterString = mySearchFilterString.replaceAll("VendorCustomData", "cn.vendor.accVendorCustomData");
                    }
                    if (mySearchFilterString.contains("CustomerCustomData")) {
                        mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "cn.customer.CustomerCustomData");
                    }
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    request.put("isOpeningBalance",false);
                }
            }
            if (!mySearchFilterString.contains("CustomerCustomData")) {
                String query = "Select cn from CreditNote cn where cn.isOpeningBalenceCN=true AND cn.cNForCustomer=false AND cn.deleted=false AND cn.company.companyID=?" + condition + mySearchFilterString;
                list = executeQuery( query, params.toArray());
                count = list.size();
                if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                    list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getOpeningBalanceVendorCNs : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    
    @Override
    public int getOpeningBalanceVendorCNCount(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalCNs = false;
        boolean isAgedReport = (request.containsKey("agedReport") && request.get("agedReport") != null) ? Boolean.parseBoolean(request.get("agedReport").toString()) : false;
        boolean isAgedSummary = (request.containsKey("isAgedSummary") && request.get("isAgedSummary") != null) ? Boolean.parseBoolean(request.get("isAgedSummary").toString()) : false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String vendorid = (String) request.get("vendorid");

        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }

        boolean isAccountCNs = false;
        if (request.containsKey("isAccountCNs") && request.get("isAccountCNs") != null) {
            isAccountCNs = (Boolean) request.get("isAccountCNs");
        }
        try {
            String condition = "";
            ArrayList params = new ArrayList();

            params.add(companyid);

            if (isAccountCNs && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                condition += " AND cn.account.ID=? ";
                params.add(accountId);
            } else if (!StringUtil.isNullOrEmpty(vendorid)) {
                condition += " AND cn.vendor.ID=? ";
                params.add(vendorid);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND cn.vendor.ID IN " + vendorIdGroup;
            }

            if (request.get("excludeNormal") != null) {
                excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
            }

            if (excludeNormal) {
                condition += " AND cn.normalCN=false ";
            }

            if (request.get("onlyOpeningNormalCNs") != null) {
                onlyOpeningNormalCNs = Boolean.parseBoolean(request.get("onlyOpeningNormalCNs").toString());
            }

            if (onlyOpeningNormalCNs) {
                condition += " AND cn.normalCN=true ";
            }


            String currencyfilterfortrans = "";
            if (request.containsKey("currencyfilterfortrans")) {
                currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            }

            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
                params.add(currencyfilterfortrans);
                condition += " and cn.currency.currencyID = ? ";
            }
            if (!isAgedReport && !isAgedSummary) { //in aged report all opening transactions are required so no need to give start and end date
                String startDate = (String) request.get(Constants.REQ_startdate);
                String endDate = (String) request.get(Constants.REQ_enddate);
                if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                    condition += " and (cn.creationDate >=? and cn.creationDate <=?) ";
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
            }

            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {

                String[] searchcol = new String[]{"cn.vendor.name","cn.vendor.acccode", "cn.creditNoteNumber", "cn.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4); 
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND cn.vendor IS NOT NULL ";
            }
            String appendCase = "and";
            String mySearchFilterString = "";
//            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
//            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
//                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
//                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
//                }
//            }
//            String Searchjson = "";
//            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
//                Searchjson = request.get("searchJson").toString();
//
//                if (!StringUtil.isNullOrEmpty(Searchjson)) {
//                    request.put(Constants.Searchjson, Searchjson);
//                    request.put(Constants.appendCase, appendCase);
//                    request.put("isOpeningBalance",true);
//                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
//                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
//                    mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceCreditNoteCustomData", "cn.openingBalanceCreditNoteCustomData");
//                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
//                }
//            }
            String query = "Select count(cn.ID) from CreditNote cn where cn.isOpeningBalenceCN=true AND cn.cNForCustomer=false AND cn.deleted=false AND cn.company.companyID=?" + condition + mySearchFilterString;
            list = executeQuery( query, params.toArray());
            Long totalCnt = 0l;
            if (list != null && !list.isEmpty()){
                totalCnt = (Long) list.get(0);
            }
            count = totalCnt.intValue();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getOpeningBalanceVendorCNCount : " + ex.getMessage(), ex);
        }
        return count;
    }
    
    
    @Override
    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForVendorCNs(HashMap<String, Object> request) throws ServiceException {
        List<CreditNote> list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalCNs = false;
        boolean isAgedReport = (request.containsKey("agedReport") && request.get("agedReport") != null) ? Boolean.parseBoolean(request.get("agedReport").toString()) : false;
        boolean isAgedSummary = (request.containsKey("isAgedSummary") && request.get("isAgedSummary") != null) ? Boolean.parseBoolean(request.get("isAgedSummary").toString()) : false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String start = (String) request.get("start");
        String limit = (String) request.get("limit");
        String vendorid = (String) request.get("vendorid");

        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }

        boolean isAccountCNs = false;
        if (request.containsKey("isAccountCNs") && request.get("isAccountCNs") != null) {
            isAccountCNs = (Boolean) request.get("isAccountCNs");
        }
        try {
            String condition = "";
            ArrayList params = new ArrayList();

            params.add(companyid);

            if (isAccountCNs && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                condition += " AND cn.account.ID=? ";
                params.add(accountId);
            } else if (!StringUtil.isNullOrEmpty(vendorid)) {
                condition += " AND cn.vendor.ID=? ";
                params.add(vendorid);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND cn.vendor.ID IN " + vendorIdGroup;
            }

            if (request.get("excludeNormal") != null) {
                excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
            }

            if (excludeNormal) {
                condition += " AND cn.normalCN=false ";
            }

            if (request.get("onlyOpeningNormalCNs") != null) {
                onlyOpeningNormalCNs = Boolean.parseBoolean(request.get("onlyOpeningNormalCNs").toString());
            }

            if (onlyOpeningNormalCNs) {
                condition += " AND cn.normalCN=true ";
            }


            String currencyfilterfortrans = "";
            if (request.containsKey("currencyfilterfortrans")) {
                currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            }

            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
                params.add(currencyfilterfortrans);
                condition += " and cn.currency.currencyID = ? ";
            }
            if (!isAgedReport && !isAgedSummary) { //in aged report all opening transactions are required so no need to give start and end date
                String startDate = (String) request.get(Constants.REQ_startdate);
                String endDate = (String) request.get(Constants.REQ_enddate);
                if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                    condition += " and (cn.creationDate >=? and cn.creationDate <=?) ";
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
            }

            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {

                String[] searchcol = new String[]{"cn.vendor.name","cn.vendor.acccode", "cn.creditNoteNumber", "cn.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND cn.vendor IS NOT NULL ";
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
                    request.put("isOpeningBalance",true);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceCreditNoteCustomData", "cn.openingBalanceCreditNoteCustomData");
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            String query = "Select COALESCE(SUM(cn.openingBalanceBaseAmountDue),0) from CreditNote cn where cn.isOpeningBalenceCN=true AND cn.cNForCustomer=false AND cn.deleted=false AND cn.company.companyID=?" + condition + mySearchFilterString;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getOpeningBalanceTotalBaseAmountDueForVendorCNs : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    @Override
    public KwlReturnObject getOpeningBalanceTotalBaseAmountForVendorCNs(HashMap<String, Object> request) throws ServiceException {
        List<CreditNote> list = null;
        boolean excludeNormal = false;
        boolean onlyOpeningNormalCNs = false;
        boolean isAgedReport = (request.containsKey("agedReport") && request.get("agedReport") != null) ? Boolean.parseBoolean(request.get("agedReport").toString()) : false;
        boolean isAgedSummary = (request.containsKey("isAgedSummary") && request.get("isAgedSummary") != null) ? Boolean.parseBoolean(request.get("isAgedSummary").toString()) : false;
        int count = 0;
        String companyid = (String) request.get("companyid");
        DateFormat df = (DateFormat) request.get(Constants.df);
        String start = (String) request.get("start");
        String limit = (String) request.get("limit");
        String vendorid = (String) request.get("vendorid");

        String vendorIdGroup = (String) request.get("custVendorID");
        if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
            vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
        }

        boolean isAccountCNs = false;
        if (request.containsKey("isAccountCNs") && request.get("isAccountCNs") != null) {
            isAccountCNs = (Boolean) request.get("isAccountCNs");
        }
        try {
            String condition = "";
            ArrayList params = new ArrayList();

            params.add(companyid);

            if (isAccountCNs && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                condition += " AND cn.account.ID=? ";
                params.add(accountId);
            } else if (!StringUtil.isNullOrEmpty(vendorid)) {
                condition += " AND cn.vendor.ID=? ";
                params.add(vendorid);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND cn.vendor.ID IN " + vendorIdGroup;
            }

            if (request.get("excludeNormal") != null) {
                excludeNormal = Boolean.parseBoolean(request.get("excludeNormal").toString());
            }

            if (excludeNormal) {
                condition += " AND cn.normalCN=false ";
            }

            if (request.get("onlyOpeningNormalCNs") != null) {
                onlyOpeningNormalCNs = Boolean.parseBoolean(request.get("onlyOpeningNormalCNs").toString());
            }

            if (onlyOpeningNormalCNs) {
                condition += " AND cn.normalCN=true ";
            }


            String currencyfilterfortrans = "";
            if (request.containsKey("currencyfilterfortrans")) {
                currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            }

            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
                params.add(currencyfilterfortrans);
                condition += " and cn.currency.currencyID = ? ";
            }
            //For now this method get called from 2 places and from both places we does need to apply start date and end date. It is casusing issue like SDP-5696 So commenting below code
//            if (!isAgedReport && !isAgedSummary) { //in aged report all opening transactions are required so no need to give start and end date
//                String startDate = (String) request.get(Constants.REQ_startdate);
//                String endDate = (String) request.get(Constants.REQ_enddate);
//                if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
//                    condition += " and (cn.creationDate >=? and cn.creationDate <=?) ";
//                    params.add(df.parse(startDate));
//                    params.add(df.parse(endDate));
//                }
//            }

            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {

                String[] searchcol = new String[]{"cn.vendor.name", "cn.vendor.acccode", "cn.creditNoteNumber", "cn.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND cn.vendor IS NOT NULL ";
            }

            String query = "Select COALESCE(sum(cn.originalOpeningBalanceBaseAmount),0) from CreditNote cn where cn.isOpeningBalenceCN=true AND cn.cNForCustomer=false AND cn.deleted=false AND cn.company.companyID=?" + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getOpeningBalanceTotalBaseAmountForVendorCNs : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    
    @Override
    public KwlReturnObject getCreditNoteMerged(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String startstr = (String) request.get(Constants.start);
            String limitstr = (String) request.get(Constants.limit);
            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            int cntype = 1;
            int transactiontype = 1;
            String moduleid = "";
            if (request.containsKey(Constants.moduleid) && request.get(Constants.moduleid) != null) {
                moduleid = request.get(Constants.moduleid).toString();
            }
            boolean isprinted = false;
            boolean isMonthlyAgeingReport = false;
            if (request.get("isMonthlyAgeingReport") != null) {
                isMonthlyAgeingReport = Boolean.parseBoolean(request.get("isMonthlyAgeingReport").toString());
            }
            boolean isAged = (request.containsKey("isAged") && request.get("isAged") != null) ? Boolean.parseBoolean(request.get("isAged").toString()) : false;
            String userID = "";
            String companyid = (String) request.get("companyid");
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", companyid);
            Object exPrefObject = executeQueryWithProjection(ExtraCompanyPreferences.class, new String[]{"columnPref"}, paramMap);
            JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
            boolean isPostingDateCheck = false;
            boolean getRecordBasedOnJEDate = false;
            boolean isToFetchRecordLessEndDate = (request.containsKey("isToFetchRecordLessEndDate") && request.get("isToFetchRecordLessEndDate") != null) ? Boolean.parseBoolean(request.get("isToFetchRecordLessEndDate").toString()) : false;
            if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                isPostingDateCheck = true;
            }
            if (request.containsKey("getRecordBasedOnJEDate") && request.get("getRecordBasedOnJEDate") != null && Boolean.parseBoolean(request.get("getRecordBasedOnJEDate").toString())) {
                getRecordBasedOnJEDate = true;
            }
            boolean isenableSalesPersonAgentFlow=false;
            if (request.containsKey("enablesalespersonagentflow") && request.get("enablesalespersonagentflow") != null && !StringUtil.isNullOrEmpty(request.get("enablesalespersonagentflow").toString())) {
                isenableSalesPersonAgentFlow = Boolean.parseBoolean(request.get("enablesalespersonagentflow").toString());
            }
            boolean isCustomDetailLineReport=false;
            if (request.containsKey("isCustomDetailLineReport") && request.get("isCustomDetailLineReport") != null && !StringUtil.isNullOrEmpty(request.get("isCustomDetailLineReport").toString())) {
                isCustomDetailLineReport = Boolean.parseBoolean(request.get("isCustomDetailLineReport").toString());
            }
            if (isenableSalesPersonAgentFlow) {
                if (request.containsKey("userid") && request.get("userid") != null && !StringUtil.isNullOrEmpty(request.get("userid").toString())) {
                    userID = (String) request.get("userid");
                }
            }
            String upperLimitDate=(String) request.get("upperLimitDate");
            if (request.get(Constants.MARKED_PRINTED) != null) {
                isprinted = Boolean.parseBoolean((String) request.get(Constants.MARKED_PRINTED));
            }
            if (request.containsKey("cntype") && request.get("cntype") != null) {
                cntype = Integer.parseInt(request.get("cntype").toString());
            }
            if (request.containsKey("transactiontype") && request.get("transactiontype") != null) {
                transactiontype = Integer.parseInt(request.get("transactiontype").toString());
            }
            String vendorIdGroup = (String) request.get("custVendorID");
            if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
                vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
            }
            String newcustomerid = "";
            if (request.containsKey(Constants.newcustomerid) && request.get(Constants.newcustomerid) != null) {
                newcustomerid = (String) request.get(Constants.newcustomerid);
            }
            String newvendorid = "";
            if (request.containsKey(Constants.newvendorid) && request.get(Constants.newvendorid) != null) {
                newvendorid = (String) request.get(Constants.newvendorid);
            }
            boolean isPendingApproval = false;
            if (request.containsKey("pendingapproval") && request.get("pendingapproval") != null && !StringUtil.isNullOrEmpty(request.get("pendingapproval").toString())) {
                isPendingApproval = Boolean.parseBoolean(request.get("pendingapproval").toString());
            }
            String userDepartment = "";
            if (request.containsKey("userDepartment") && request.get("userDepartment") != null) {
                userDepartment = (String) request.get("userDepartment");
            }
            String ss = (String) request.get(Constants.ss);
            ArrayList params = new ArrayList();
            ArrayList param = new ArrayList();
            params.add((String) request.get(Constants.companyKey));
            param.add((String) request.get(Constants.companyKey));
            String condition = " where cn.company=?";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"cn.cnnumber", "c.name","c.aliasname", "cn.memo", "je.entryno"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 5); 
                StringUtil.insertParamSearchString(map);
                map = StringUtil.insertParamSearchStringMap(param, ss, 5);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
          
            }
            
            if (!StringUtil.isNullOrEmpty(newcustomerid)) {
                if (newcustomerid.contains(",")) {
                    newcustomerid = AccountingManager.getFilterInString(newcustomerid);
                    condition += " and cn.customer IN" + newcustomerid;
                } else {
                    params.add(newcustomerid);
                    condition += " and cn.customer = ? ";

                }
            }
            
            if (!StringUtil.isNullOrEmpty(newvendorid)) {
                if (newvendorid.contains(",")) {
                    newvendorid = AccountingManager.getFilterInString(newvendorid);
                    condition += " and cn.vendor IN" + newvendorid;
                } else {
                    params.add(newvendorid);
                    condition += " and cn.vendor = ? ";

                }
            }
            if (request.containsKey("groupcombo") && request.get("groupcombo") != null && request.containsKey(Constants.globalCurrencyKey) && request.get(Constants.globalCurrencyKey) != null) {
                int groupcombo = (Integer) request.get("groupcombo");

                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    condition += " and cn.currency=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                }  else if(groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    condition += " and cn.currency!=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                }
            }
            
            if (request.containsKey("upperLimitDate") && request.get("upperLimitDate") != null && request.get("upperLimitDate") != "") {
                condition += " and je.entrydate <= ?";
                params.add(df.parse(upperLimitDate));
                param.add(df.parse(upperLimitDate));
            }
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and je.id IN(" + jeIds + ")";
            }

            String noteId = (String) request.get("noteid");
            if (!StringUtil.isNullOrEmpty(noteId)) {
                params.add(noteId);
                param.add(noteId);
                condition += " and cn.id = ? ";
            }
             
            if (isPendingApproval) { // Get only pending approved records
                condition += " and cn.approvestatuslevel != ? ";
                params.add(11);
                param.add(11);
            } else {// Get only approved records
                condition += " and cn.approvestatuslevel = ?";
                params.add(11);
                param.add(11);
            }
            String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            boolean isNoteForPayment = false;
            boolean isNewUI = false;
            if (request.get("isNoteForPayment") != null) {
                isNoteForPayment = (Boolean) request.get("isNoteForPayment");
            }
            if (request.get("isNewUI") != null) {
                isNewUI = (Boolean) request.get("isNewUI");
            }
            
            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)&&!(isNoteForPayment&&isNewUI)) {
                params.add(currencyfilterfortrans);
                param.add(currencyfilterfortrans);
                condition += " and cn.currency = ? ";
            }
            String withInvCondition = "";
            String accID = (String) request.get("accid");
            if (!StringUtil.isNullOrEmpty(accID)) {
                params.add(accID);
                param.add(accID);
                condition += " and c.id = ? ";
//                withInvCondition += " and cn.cnamountdue > 0";
            }

            String customerVendorID = (String) request.get("customerVendorID");
            if (!StringUtil.isNullOrEmpty(customerVendorID)) {
                params.add(customerVendorID);
                condition += " and c.id = ? ";
            }
            
            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " and c.id in " + vendorIdGroup;
//                withInvCondition += " and cn.cnamountdue > 0";
            }

            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                param.add(costCenterId);
                condition += " and je.costcenter=?";
            }
            String startDate = request.get(Constants.REQ_startdate)!=null? StringUtil.DecodeText((String) request.get(Constants.REQ_startdate)):(String) request.get(Constants.REQ_startdate);
            String endDate = request.get(Constants.REQ_enddate)!=null? StringUtil.DecodeText((String) request.get(Constants.REQ_enddate)):(String) request.get(Constants.REQ_enddate);
            if((isAged || isToFetchRecordLessEndDate) && !StringUtil.isNullOrEmpty(endDate) && !isMonthlyAgeingReport){ //Fetching all transactions whose creation date is upto end date
                condition += " and je.entrydate <=? ";
                params.add(df.parse(endDate));
                param.add(df.parse(endDate));
            } else if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                if (isPostingDateCheck && !getRecordBasedOnJEDate) {
                    condition += " and (cn.creationDate >=? and cn.creationDate <=?)";
                } else {
                    condition += " and (je.entrydate >=? and je.entrydate <=?)";
                }
                if (isMonthlyAgeingReport) {
                    params.add(new Date(Long.parseLong(startDate)));
                    params.add(new Date(Long.parseLong(endDate)));
                    param.add(new Date(Long.parseLong(startDate)));
                    param.add(new Date(Long.parseLong(endDate)));
                } else {
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                    param.add(df.parse(startDate));
                    param.add(df.parse(endDate));
                }
            }

            String jeid = " jed.id = cn.centry ";
            String appendCase = "and";
            String mySearchFilterString = "";
            String joinString = "";
            String joinString1 = "";
            String groupBy = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().trim().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String Searchjson = "";
            String searchDefaultFieldSQL = "";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = StringUtil.DecodeText(request.get("searchJson").toString());

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
                        joinString += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("creditnote", "cn");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("journalentry", "je");
                        joinString += " left join creditnotelinking cnlinking on cnlinking.docid=cn.id and cnlinking.sourceflag = 1 ";
                        groupBy = " GROUP BY cn.id ";
                    }
                    if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field
                        request.put(Constants.Searchjson, Searchjson);
                        request.put(Constants.appendCase, appendCase);
                        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                        if (mySearchFilterString.contains("CustomerCustomData")) {
                            joinString += " inner join customercustomdata on customercustomdata.customerId=cn.customer ";
                            mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "customercustomdata");
                        }
                        if (mySearchFilterString.contains("VendorCustomData")) {
                            joinString += " left join vendorcustomdata  on vendorcustomdata.vendorId=cn.vendor ";
                            mySearchFilterString = mySearchFilterString.replaceAll("VendorCustomData", "vendorcustomdata");
                        }
                        if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");
                            joinString += " inner join accjedetailcustomdata  on accjedetailcustomdata.jedetailId = jed.id ";
                            jeid = " je.id=jed.journalentry ";
                        }
                        if (mySearchFilterString.contains("accjecustomdata")) {
                            joinString += " inner join accjecustomdata on accjecustomdata.journalentryId=cn.journalentry ";
                        }                    
//                    joinString1 += " inner join accjecustomdata on accjecustomdata.journalentryId=cn.journalentry ";
                        groupBy = " GROUP BY cn.id ";
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                        StringUtil.insertParamAdvanceSearchString1(param, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
	    String orderBy = "";
            String[] stringSort = null;
            String sort_Col = "";
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                stringSort = columSort(Col_Name, Col_Dir);
                orderBy += stringSort[0];
                sort_Col += stringSort[1];
            } else {
                orderBy += " ORDER BY date DESC ";
                sort_Col += ", je.entrydate AS date ";
            }
            
            if (cntype == 12 ) {  // With Sales Return
                condition += " and cn.salesReturn is not NULL ";
            } 
            if (cntype == 13 ) {  // Without Sales Return
                condition += " and cn.salesReturn is NULL ";
            }
            if (nondeleted) {
                condition += " and cn.deleteflag='F' ";
            } else if (deleted) {
                condition += " and cn.deleteflag='T' ";
            }
            if (isprinted) {
                condition += " and cn.printedflag=true";
            }
            if (request.containsKey("linknumber") && request.get("linknumber") != null && !request.get("linknumber").toString().equals("")) {
                condition += " and cn.cnnumber = '"+request.get("linknumber")+"' ";
            }
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                joinString += " inner join users on users.userid = cn.createdby ";
                condition += " and users.department = ? ";
                params.add(userDepartment);
            }
            //String withInvCustomerCondition = " customer c ";
            String isCustomer = " cn.customer ";
            String iscustomerJoin = " INNER JOIN customer c ON c.id=cn.customer";
            
            String salesPersonMappingQueryCustomer = "";
            String filterQueryForSalesPersonmappingCustomer="";
//           if (isenableSalesPersonAgentFlow  && !StringUtil.isNullOrEmpty(userID)) {
//                salesPersonMappingQueryCustomer = " left join salespersonmapping spm on spm.customerid=cn.customer  left join masteritem  mst on mst.id=spm.salesperson ";
//                filterQueryForSalesPersonmappingCustomer += " and ((mst.user= '" + userID + "' or mst.user is null  and c.isavailableonlytosalespersons='T' ) or  (c.isavailableonlytosalespersons='F')) ";
//            }
            if (isenableSalesPersonAgentFlow && !StringUtil.isNullOrEmpty(userID)) {//this block is executed only when owners restriction feature is on 
                String salesPersonID = (String) request.get("salesPersonid");
                String salespersonQuery = "";
                if (!StringUtil.isNullOrEmpty(salesPersonID)) {
                    salesPersonID = AccountingManager.getFilterInString(salesPersonID);
                    salespersonQuery = "  cn.salesperson in " + salesPersonID + " or ";
                }

                condition += " and ( " + salespersonQuery + "  cn.createdby='" + userID + "' or cn.salesperson is null  ) ";
            }

            String query = "";
//            String jeDetailsCreditCondition = " and jed.DEBIT='F' ";
            if (transactiontype != 8) {
                if (cntype == 4 || cntype == 5) {//CN against vendor
                    withInvCondition += " and (cn.cntype=4 or cn.cntype=5)";
//                String jeDetailsCreditCondition = "";
                    //  withInvCustomerCondition = " vendor c ";
                    isCustomer = " cn.vendor ";
                    iscustomerJoin = " INNER JOIN vendor c on c.id = cn.vendor ";
                    query = "SELECT  'False' as withoutinventory,cn.id as creditnote," + isCustomer + ",jed.id as jedetail "+sort_Col
                            + "from creditnote cn INNER JOIN journalentry je ON cn.journalentry=je.id INNER JOIN jedetail jed  on " + jeid + joinString
                            + "INNER JOIN account ac ON jed.account=ac.id " + iscustomerJoin + condition + withInvCondition + mySearchFilterString
                            + //                         " UNION ALL "+
                            //                         "SELECT 'True' as withoutinventory,cn.id as creditnote,c.id as customer,jed.id as jedetail,je.entrydate as date "+
                            //                         "from billingcreditnote cn INNER JOIN journalentry je ON cn.journalentry=je.id INNER JOIN jedetail jed ON je.id=jed.journalentry "+joinString1+
                            //                         "INNER JOIN account ac ON jed.account=ac.id,customer c "+condition+mySearchFilterString+
                           groupBy + orderBy;         //" ORDER BY date DESC";
                } else if (cntype == 8) {
                    String Customer = " cn.vendor ";
                    String customerJoin = " INNER JOIN vendor c on c.id = cn.vendor ";

//                    withInvCondition += " and cn.cntype!=1 ";  //ERP-11761
                    query = "SELECT  'True' as withoutinventory,cn.id as creditnote," + Customer + ",jed.id as jedetail "+sort_Col
                            + "from creditnote cn INNER JOIN journalentry je ON cn.journalentry=je.id INNER JOIN jedetail jed  on " + jeid + joinString
                            + "INNER JOIN account ac ON jed.account=ac.id" + customerJoin + condition + withInvCondition + mySearchFilterString
                            + " UNION ALL "
                            + " SELECT  'False' as withoutinventory,cn.id as creditnote," + isCustomer + ",jed.id as jedetail "+sort_Col
                            + "from creditnote cn INNER JOIN journalentry je ON cn.journalentry=je.id INNER JOIN jedetail jed  on " + jeid + joinString
                            + "INNER JOIN account ac ON jed.account=ac.id " + iscustomerJoin + condition + withInvCondition + mySearchFilterString
                            + groupBy + orderBy;         //" ORDER BY date DESC";
                    for (Object object : param) {
                        params.add(object);
                    }
                } else if (cntype == Constants.CreditNoteForOvercharge) {
                    withInvCondition += " and cn.cntype = 6 ";
                    query = "SELECT  'False' as withoutinventory,cn.id as creditnote," + isCustomer + ",jed.id as jedetail " + sort_Col
                            + "from creditnote cn INNER JOIN journalentry je ON cn.journalentry=je.id INNER JOIN jedetail jed  on " + jeid + joinString + salesPersonMappingQueryCustomer
                            + "INNER JOIN account ac ON jed.account=ac.id " + iscustomerJoin + condition + withInvCondition + mySearchFilterString + filterQueryForSalesPersonmappingCustomer
                            + groupBy + orderBy;
                } else {
                      withInvCondition += " and cn.cntype!=4 ";
                    if (isCustomDetailLineReport) { // Fetch all CN irrespective of cntype.
                        withInvCondition = "";
                        isCustomer = " -99 ";
                        iscustomerJoin = "";
                    }
                    query = "SELECT  'False' as withoutinventory,cn.id as creditnote," + isCustomer + ",jed.id as jedetail "+sort_Col
                            + "from creditnote cn INNER JOIN journalentry je ON cn.journalentry=je.id INNER JOIN jedetail jed  on "+ jeid + joinString+salesPersonMappingQueryCustomer
                            + "INNER JOIN account ac ON jed.account=ac.id " + iscustomerJoin + condition + withInvCondition + mySearchFilterString+filterQueryForSalesPersonmappingCustomer
//                            + " UNION ALL "
//                            + "SELECT 'True' as withoutinventory,cn.id as creditnote,c.id as customer,jed.id as jedetail,je.entrydate as date "
//                            + "from billingcreditnote cn INNER JOIN journalentry je ON cn.journalentry=je.id INNER JOIN jedetail jed ON je.id=jed.journalentry " + joinString1
//                            + "INNER JOIN account ac ON jed.account=ac.id,customer c " + condition + " and ac.id=c.id " + mySearchFilterString
                            +groupBy + orderBy;         // " ORDER BY date DESC";
                    //String query = "select cn, c, jed from CreditNote cn inner join cn.journalEntry je inner join je.details jed inner join jed.account ac, Customer c" + condition;
//                    for (Object object : param) {
//                        params.add(object);
//                    }
                }
            } else {
                withInvCondition += " and (cn.cntype=2 or cn.cntype=3) ";
                //      withInvCustomerCondition = "inner join  customer c ";
                query = "SELECT  'False' as withoutinventory,cn.id as creditnote," + isCustomer + ",jed.id as jedetail "+sort_Col
                        + "from creditnote cn INNER JOIN journalentry je ON cn.journalentry=je.id INNER JOIN jedetail jed  on " + jeid + joinString
                        + "INNER JOIN account ac ON jed.account=ac.id " + iscustomerJoin + condition + withInvCondition + mySearchFilterString
                        + groupBy + orderBy;         //" ORDER BY date DESC";
            }
            if (request.containsKey("linknumber") && request.get("linknumber") != null && !request.get("linknumber").toString().equals("")) {
                condition += " and cn.creditNoteNumber = ? ";
                param.add(request.get("linknumber"));
            }
            list = executeSQLQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(startstr) == false && StringUtil.isNullOrEmpty(limitstr) == false) {
                int start = Integer.parseInt(startstr);
                int limit = Integer.parseInt(limitstr);
                list = executeSQLQueryPaging( query, params.toArray(), new Integer[]{start, limit});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.getCreditNoteMerged:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getDOIDFromInvoiceDetails(String soid) throws ServiceException {
        String selQuery = "from DeliveryOrderDetail ge where ge.cidetails.ID = ? and ge.deliveryOrder.deleted = false";
        List list = executeQuery( selQuery, new Object[]{soid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getNoteType(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from NoteType ";

        if (filterParams.containsKey("companyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=? or company.companyID=''";
            params.add(filterParams.get("companyid"));
        }
        query += condition;
//        query="from UnitOfMeasure where company.companyID=?";
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject deleteNoteType(int typeid) throws ServiceException {
        String delQuery = "delete from NoteType u where u.id=?";
        int numRows = executeUpdate( delQuery, new Object[]{typeid});
        return new KwlReturnObject(true, "Note Type has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject saveNoteTypes(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            NoteType noteType = new NoteType();
            if (dataMap.containsKey("id")) {
                noteType = dataMap.get("id") == null ? null : (NoteType) get(NoteType.class, (Integer) dataMap.get("id"));
            }
            if (dataMap.containsKey("name")) {
                noteType.setName((String) dataMap.get("name"));
            }

            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                noteType.setCompany(company);
            }
            save(noteType);
            list.add(noteType);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveNoteTypes : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Note Type has been added successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveCreditNoteTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            CreditNoteTermsMap termmap = new CreditNoteTermsMap();

            if (dataMap.containsKey("termamount")) {
                termmap.setTermamount((Double) dataMap.get("termamount"));
            }
            if (dataMap.containsKey("termpercentage")) {
                termmap.setPercentage((Integer) dataMap.get("termpercentage"));
            }
            if (dataMap.containsKey("creditNoteId")) {
                CreditNote creditNote = (CreditNote) get(CreditNote.class, (String) dataMap.get("creditNoteId"));
                termmap.setCreditNote(creditNote);
            }
            if (dataMap.containsKey("term")) {
                InvoiceTermsSales term = (InvoiceTermsSales) get(InvoiceTermsSales.class, (String) dataMap.get("term"));
                termmap.setTerm(term);
            }
            if (dataMap.containsKey("userid")) {
                User userid = (User) get(User.class, (String) dataMap.get("userid"));
                termmap.setCreator(userid);
            }
            if (dataMap.containsKey("createdon")) {
                termmap.setCreatedOn(((Date) dataMap.get("creationdate")).getTime());
            }
            saveOrUpdate(termmap);
            list.add(termmap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.saveCreditNoteTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getCreditNoteTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String debitNoteId = dataMap.get("creditNoteId").toString();
            String query = "from CreditNoteTermsMap where creditNote.ID = ?";
            list = executeQuery( query, new Object[]{debitNoteId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteImpl.getDebitNoteTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public void deletePartyJournalCN(String cnid, String companyid) throws ServiceException,AccountingException {
//        String selQuery = "from CreditNote where journalEntry.ID=? and deleted=false and company.companyID=?";
//        List list = executeQuery( selQuery, new Object[]{jeId, companyid});        
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("cnid", cnid);
        requestParams.put("companyid", companyid);
        requestParams.put("creditNote", true);
        deleteCreditNotesPermanent(requestParams);
    }
    
    @Override
    public void deletePartyJournalCNDNTemporary(HashMap<String, Object> reqParams) throws ServiceException {
        String delQuery = "";
        int numtotal = 0;
        try {
            ArrayList params = new ArrayList();
            if (reqParams.containsKey("CreditNote") && reqParams.containsKey("cnid")) {
                params.add(reqParams.get("cnid"));
                params.add(reqParams.get("companyid"));
                boolean isCNUsed = Boolean.parseBoolean(reqParams.get("isused").toString());
                if (!isCNUsed) {
                    delQuery = "update CreditNote set deleted=true where ID=? and company.companyID=?";
                }
            }
            if (reqParams.containsKey("DebitNote") && reqParams.containsKey("dnid")) {
                params.add(reqParams.get("dnid"));
                params.add(reqParams.get("companyid"));
                boolean isDNUsed = Boolean.parseBoolean(reqParams.get("isused").toString());
                if (!isDNUsed) {
                    delQuery = "update DebitNote set deleted=true where ID=? and company.companyID=?";
                }
            }

            numtotal = executeUpdate(delQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Credit Note as its referance child field is not deleted.", ex);
        }
    }

    @Override
    public KwlReturnObject getCNFromNoteNoAndId(String entryNumber, String companyid, String creditNoteId) throws ServiceException {
        String selQuery = "from CreditNote where creditNoteNumber=? and company.companyID=? and ID!=?";
        List list = executeQuery( selQuery, new Object[]{entryNumber, companyid, creditNoteId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getCNLinkedWithCustomerInvoice(String creditNoteId, String companyid) throws ServiceException {
        String selQuery = "from CreditNoteDetail cn where cn.invoice is not null and cn.creditNote.ID =? and cn.creditNote.deleted=false and cn.company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{creditNoteId, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /*
     Checked whether Credit Note linked in Payment 
     */

    public KwlReturnObject getCNLinkedWithPayment(String creditNoteId, String companyid) throws ServiceException {
        String selQuery = "from CreditNotePaymentDetails cn where cn.payment is not null and cn.creditnote.ID =? and cn.creditnote.deleted=false and cn.creditnote.company.companyID=?";
        List list = executeQuery(selQuery, new Object[]{creditNoteId, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
        public KwlReturnObject getCreditNoteIdFromSRId(String srid, String companyid) throws ServiceException {
        String selQuery = "from CreditNote cr where cr.salesReturn.ID = ? and cr.company.companyID=? ";
        List list = executeQuery( selQuery, new Object[]{srid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
      /**
     * Get Invoice linked with Sales return.
     * @param salesreturnid
     * @return
     * @throws ServiceException 
     */
    @Override
     public KwlReturnObject getinvoicesLinkedWithSR(String salesreturnid) throws ServiceException {
        List list = null;
        if (!StringUtil.isNullOrEmpty(salesreturnid)) {
            String selQuery = "from SalesReturnLinking PRL where  PRL.DocID.ID =?  and PRL.ModuleID=2";
            list = executeQuery(selQuery, new Object[]{salesreturnid});
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getVendorCnPayment(String paymentId) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select * from creditnotpayment where paymentid = ?";
            list = executeSQLQuery( query, new Object[]{paymentId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorCnPayment:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getJEFromCNDetail(String jeid, String companyid) throws ServiceException {
        String selQuery = "select * from cndetails where linkedgainlossje=? and company=?";
        List list = executeSQLQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
  }
    
    @Override
    public KwlReturnObject getCreditTaxDetails(HashMap<String, Object> paramsTaxDetails) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            String Condition = " where cnd.company.companyID=?";
            params.add(paramsTaxDetails.get("companyid"));

            if (paramsTaxDetails.containsKey("cnid") && paramsTaxDetails.get("cnid") != null) {
                Condition += " and cnd.creditNote.ID=?";
                params.add((String) paramsTaxDetails.get("cnid").toString());
            }
            if (paramsTaxDetails.containsKey("accid") && paramsTaxDetails.get("accid") != null) {
                Condition += " and cnd.account.ID=? ";
                params.add((String) paramsTaxDetails.get("accid"));
            }

            String query = " from CreditNoteTaxEntry cnd" + Condition;
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
     @Override
    public KwlReturnObject getCreditNoteAgainstVendorGst(String cnid, String companyid) throws ServiceException {
        String query = " from CreditNoteAgainstVendorGst cn where cn.creditNote.ID = ? and cn.company.companyID = ? ";
        List list = executeQuery(query, new Object[]{cnid, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    public KwlReturnObject getCreditNoteIdFromPaymentIdLedger(String paymentid) throws ServiceException {
        Session session = null;
        List params = new ArrayList();
        List list = new ArrayList();
        try {
            params.add(paymentid);
            String query = "select id, cnid, description from creditnotpayment where paymentid=?";
            list = executeSQLQuery(query, new Object[]{paymentid});
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return new KwlReturnObject(true, "", null, list, list.size());
        }
    }

    public List getForeignGainLossJE(String cnid, String companyid) throws ServiceException {
        String selQuery = "select linkedgainlossje from cndetails where creditNote=? and company=?";
        List list = executeSQLQuery( selQuery, new Object[]{cnid, companyid});
        return list;
    }
    
    public String getInvoiceId(String companyid, String cnnumber) throws ServiceException {
        ArrayList params = new ArrayList();
        String id = "";
        params.add(companyid);
        params.add(cnnumber);
        String query = "select cn.id from creditnote cn where cn.cnnumber=?";
        List<Object[]> list = executeSQLQuery(query, params.toArray());

        for (Object[] linkedCNObj : list) {
            id = (String) linkedCNObj[0];
        }
        return id;
    }
    
    @Override
    public synchronized String updateCreditNoteEntryNumber(Map<String, Object> seqNumberMap) {
        String documnetNumber = "";
        try {
            documnetNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
            int seqNumber = 0;
            if (seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())) {
                seqNumber = Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
            }
            String datePrefix = seqNumberMap.containsKey(Constants.DATEPREFIX) ? (String) seqNumberMap.get(Constants.DATEPREFIX) : "";
            String dateSuffix = seqNumberMap.containsKey(Constants.DATESUFFIX) ? (String) seqNumberMap.get(Constants.DATESUFFIX) : "";
            String dateafterPrefix = seqNumberMap.containsKey(Constants.DATEAFTERPREFIX) ? (String) seqNumberMap.get(Constants.DATEAFTERPREFIX) : "";
            String sequenceFormatID = seqNumberMap.containsKey(Constants.SEQUENCEFORMATID) ? (String) seqNumberMap.get(Constants.SEQUENCEFORMATID) : "";
            String documentID = seqNumberMap.containsKey(Constants.DOCUMENTID) ? (String) seqNumberMap.get(Constants.DOCUMENTID) : "";
            String companyID = seqNumberMap.containsKey(Constants.companyKey) ? (String) seqNumberMap.get(Constants.companyKey) : "";
            String query = "update CreditNote set cnnumber=?,seqnumber=?, datePreffixValue=?,dateafterpreffixvalue=?, dateSuffixValue=? , seqformat.ID=? where ID=? and company.companyID=?";
            int numRows = executeUpdate(query, new Object[]{documnetNumber, seqNumber, datePrefix,dateafterPrefix ,dateSuffix, sequenceFormatID, documentID, companyID});
        } catch (Exception e) {
            System.out.println(e);
        }
        return documnetNumber;
    }

    @Override
    public KwlReturnObject getDeletedLinkedInvoices(CreditNote cn, String linkedDetailIDs, String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "From CreditNoteDetail cnd where cnd.company.companyID = ? and cnd.creditNote.ID = ? and cnd.invoice is not NULL ";
        if(!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
           query = query.concat(" and cnd.ID not in ( "+linkedDetailIDs+" )");
        }
        list = executeQuery(query, new Object[]{companyId,cn.getID()});
        return  new KwlReturnObject(true, "", null, list, list.size());
    }
    
      @Override
    public KwlReturnObject getDeletedLinkedDebitNotes(CreditNote cn, String linkedDetailIDs, String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "From CreditNoteDetail cnd where cnd.company.companyID = ? and cnd.creditNote.ID = ? and cnd.debitNoteId IS NOT NULL ";
        if(!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
           query = query.concat(" and cnd.ID not in ( "+linkedDetailIDs+" )");
        }
        list = executeQuery(query, new Object[]{companyId,cn.getID()});
        return  new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteSelectedLinkedInvoices(String cnid, String linkedDetailIDs, String companyid,String unlinkedDetailIDs) throws ServiceException {
        String delQuery = "delete from CreditNoteDetail cnd where cnd.creditNote.ID= ? and cnd.company.companyID=? and cnd.debitNoteId IS NULL";
        if(!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
           delQuery = delQuery.concat(" and cnd.id not in (" + linkedDetailIDs + ")");
        }
        if(!StringUtil.isNullOrEmpty(unlinkedDetailIDs)){
            delQuery = delQuery.concat(" and cnd.id in (" + unlinkedDetailIDs + ")");
        }
        int numRows = executeUpdate( delQuery, new Object[]{cnid, companyid});
        return new KwlReturnObject(true, "Credit  linked invoices have been deleted successfully", null, null, numRows);
    }
    @Override
    public KwlReturnObject deleteSelectedLinkedDebitNotes(String cnid, String linkedDetailIDs, String companyid,String unlinkedDetailIDs) throws ServiceException {
        String delQuery = "delete from CreditNoteDetail cnd where cnd.creditNote.ID= ? and cnd.company.companyID=? and cnd.invoice is NULL ";
        if(!StringUtil.isNullOrEmpty(linkedDetailIDs)) {
           delQuery = delQuery.concat(" and cnd.id not in (" + linkedDetailIDs + ")");
        }
        if(!StringUtil.isNullOrEmpty(unlinkedDetailIDs)){
            delQuery = delQuery.concat(" and cnd.id in (" + unlinkedDetailIDs + ")");
        }
        int numRows = executeUpdate( delQuery, new Object[]{cnid, companyid});
        return new KwlReturnObject(true, "Credit  linked Debit Note have been deleted successfully", null, null, numRows);
    }

    @Override
    public KwlReturnObject getNormalCreditNotes(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get("companyid");
            String query = "from CreditNote cn where cn.company.companyID = ? and cn.normalCN=? ";
            list = executeQuery( query, new Object[]{companyid, true});
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            return new KwlReturnObject(true, "", null, list, list.size());
        }
    }

    @Override
    public KwlReturnObject getAdvancePaymentIdLinkedWithCreditNote(String noteId) throws ServiceException {
        List params = new ArrayList();
        params.add(noteId);
        String query = "select payment from linkdetailpaymenttocreditnote where creditnote=?";
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getLinkDetailPaymentToCreditNote(HashMap<String, Object> reqParams1) throws ServiceException {
         List list=null;
        try {
            String condition="";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) reqParams1.get(Constants.df);                      
             if(reqParams1.containsKey("cnid") && reqParams1.get("cnid") != null){
                   String dnid = (String) reqParams1.get("cnid");
                   condition += " and rd.creditnote.ID=? ";
                   params.add(dnid);
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
            String selQuery = "from LinkDetailPaymentToCreditNote rd  where rd.payment.deleted=false "+condition;
            list = executeQuery( selQuery, params.toArray());
        }catch (Exception ex) {
                Logger.getLogger(accCreditNoteImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveCreditNoteLinking(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String creditnoteid = (String) hm.get("docid");
            CreditNoteLinking cnlinking = new CreditNoteLinking();
            if (hm.containsKey("docid")) {
                CreditNote creditnote = (CreditNote) get(CreditNote.class, creditnoteid);
                cnlinking.setDocID(creditnote);
            }
            if (hm.containsKey("moduleid")) {
                cnlinking.setModuleID((Integer) hm.get("moduleid"));
            }
            if (hm.containsKey("linkeddocid")) {
                cnlinking.setLinkedDocID((String) hm.get("linkeddocid"));
            }
            if (hm.containsKey("linkeddocno")) {
                cnlinking.setLinkedDocNo((String) hm.get("linkeddocno"));
            }
            if (hm.containsKey("sourceflag")) {
                cnlinking.setSourceFlag((Integer) hm.get("sourceflag"));
            }
            saveOrUpdate(cnlinking);
            list.add(cnlinking);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.saveCreditNoteLinking : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject deleteLinkingInformationOfCN(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        int numRows1 = 0, nuRows2 = 0,nuRows3 = 0, numRowsTotal = 0;
        try {
            params.add(requestParams.get("cnid"));

            String delQuery = "delete from InvoiceLinking inv where inv.LinkedDocID=?";
            numRows1 = executeUpdate( delQuery, params.toArray());

            delQuery = "delete from CreditNoteLinking cn where cn.DocID.ID=?";
            nuRows2 = executeUpdate( delQuery, params.toArray());
            
            
            delQuery = "delete from DebitNoteLinking dn where dn.LinkedDocID=?";
            nuRows3 = executeUpdate( delQuery, params.toArray());

            numRowsTotal = numRows1 + nuRows2 + nuRows3;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("", ex);//+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, numRowsTotal);
    }
    @Override
    public KwlReturnObject deleteLinkingInformationOfCNAgainstDN(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        int numRows1 = 0, nuRows2 = 0, numRowsTotal = 0;
        try {
            params.add(requestParams.get("cnid"));

            String delQuery = "delete from DebitNoteLinking dnl where dnl.LinkedDocID=?";
            numRows1 = executeUpdate( delQuery, params.toArray());

            delQuery = "delete from CreditNoteLinking cn where cn.DocID.ID=?";
            nuRows2 = executeUpdate( delQuery, params.toArray());

            numRowsTotal = numRows1 + nuRows2;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("", ex);//+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, numRowsTotal);
    }
    
    @Override
    public KwlReturnObject checkEntryForTransactionInLinkingTableForForwardReference(String moduleName, String docid) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(docid);
        /**
         * Removed " and invl.SourceFlag=0 " from query to * To solve CN otherwise case linking
         * previous query was checking only case mentioned in ERP-19024.
         
         */
        String hqlQuery = "from " + moduleName + "Linking invl where invl.DocID.ID=? ";
        list = executeQuery(hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public boolean isCreditNoteLinkedToOtherTransaction(String moduleName, String docid) throws ServiceException {

         boolean isCreditNoteLinkedToOtherTransaction = false;
         List list = new ArrayList();
         ArrayList params = new ArrayList();
         params.add(docid);

         CreditNote cnobj = (CreditNote) get(CreditNote.class, docid);
         if (cnobj.getCntype() == Constants.CNAgainstSalesInvoice) {
             isCreditNoteLinkedToOtherTransaction = false;
         } else {
             KwlReturnObject res = checkEntryForTransactionInLinkingTableForForwardReference(moduleName, docid);
             List reslist = res.getEntityList();
             if ((reslist != null && !reslist.isEmpty())) {
                 isCreditNoteLinkedToOtherTransaction = true;
             }
         }
         return isCreditNoteLinkedToOtherTransaction;
     }
    @Override
    public KwlReturnObject getCreditNoteTaxEntryForSalesPersonCommissionDimensionReport(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            boolean deleted = false;
            if (request.containsKey("deleted") && request.get("deleted") != null) {
                deleted = Boolean.parseBoolean((String) request.get("deleted"));
            }
            boolean nondeleted = false;
            if (request.containsKey("nondeleted") && request.get("nondeleted") != null) {
                nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            }
            ArrayList params = new ArrayList();
            params.add((String) request.get(Constants.companyKey));
            String condition = " where creditnote.company = ? ";

            if (request.containsKey(Constants.salesPersonid) && request.get(Constants.salesPersonid) != null) {
                String salesPersonID = (String) request.get(Constants.salesPersonid);
                if (!StringUtil.isNullOrEmpty(salesPersonID) && !salesPersonID.equalsIgnoreCase("All")) {
                    salesPersonID = AccountingManager.getFilterInString(salesPersonID);
                    condition += " and creditnote.salesperson in " + salesPersonID + " ";
                } else {
                    condition += " and creditnote.salesperson is not null ";
                }
            }

            if (nondeleted) {
                condition += " and creditnote.deleteflag = 'F' ";
            } else if (deleted) {
                condition += " and creditnote.deleteflag = 'T' ";
            }

            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
//                condition += " and (journalentry.entrydate >=? and journalentry.entrydate <=?)";
                condition += " and (creditnote.creationDate >=? and creditnote.creationDate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            
            boolean isPendingApproval = false;
            if (request.containsKey("pendingapproval") && request.get("pendingapproval") != null && !StringUtil.isNullOrEmpty(request.get("pendingapproval").toString())) {
                isPendingApproval = Boolean.parseBoolean(request.get("pendingapproval").toString());
            }
            if(isPendingApproval){
                condition += " and creditnote.approvestatuslevel != ? ";
                params.add(11);
            } else {
                condition += " and creditnote.approvestatuslevel = ? ";
                params.add(11);
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
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                String searchjson = request.get("searchJson").toString();

                if (!StringUtil.isNullOrEmpty(searchjson)) {
                    request.put(Constants.Searchjson, searchjson);
                    request.put(Constants.appendCase, appendCase);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                    if (mySearchFilterString.contains("accjecustomdata") || mySearchFilterString.contains("AccJECustomData")) {
                        joinString += " inner join accjecustomdata on accjecustomdata.journalentryId = creditnote.journalentry ";
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "accjecustomdata");
                    }
                    if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");
                        joinString += " inner join accjedetailcustomdata  on accjedetailcustomdata.jedetailId = jedetail.id ";
                    }
                    StringUtil.insertParamAdvanceSearchString1(params, searchjson);
                }
            }

            String query = " SELECT cntaxentry.id from cntaxentry "
                    + " inner join creditnote on creditnote.id = cntaxentry.creditnote "
                    + " INNER JOIN journalentry ON creditnote.journalentry = journalentry.id "
                    + " INNER JOIN jedetail on jedetail.id = cntaxentry.totaljedid " + joinString + condition + mySearchFilterString;
            list = executeSQLQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.getCreditNoteDetailForSalesPersonCommissionDimensionReport:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    /**
     * Description : Method is used to check entry of Credit note in
     * CreditNoteLinking table
     *
     * @param <docid> ID of Credit Note
     * @param <docid> ID of Invoice
     *
     * @return :List
     */
    
    @Override
    public KwlReturnObject checkEntryForCreditNoteInLinkingTable(String docid, String linkeddocid) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(docid);
        params.add(linkeddocid);
        String hqlQuery = "from CreditNoteLinking cnl where cnl.DocID.ID=? and cnl.LinkedDocID=?";
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * Description : Method is used to update credit note approval status
     * @param <cnID> ID of Credit Note
     * @param <companyid> ID of Company
     * @param <status> approval level
     *
     * @return :List
     */
    
    @Override
    public KwlReturnObject approvePendingCreditNote(String cnID, String companyid, int status) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(status);
        params.add(cnID);
        params.add(companyid);
        String query = "update CreditNote set approvestatuslevel = ? where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, params.toArray());
        return new KwlReturnObject(true, "Credit Note has been updated successfully.", null, null, numRows);
    }

        /**
     * Description : Method is used to update credit note approval status
     * @param <cnid> ID of Credit Note
     * @param <companyid> ID of Company
     * 
     * @return :List
     */
    @Override
    public KwlReturnObject rejectPendingCreditNote(String cnid, String companyid) throws ServiceException{
         try {
            String query = "update CreditNote set deleted=true,approvestatuslevel = (-approvestatuslevel) where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{cnid, companyid});
            return new KwlReturnObject(true, "Credit Note has been rejected successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.rejectPendingCreditNote : " + ex.getMessage(), ex);
        }
    }
    /**
     * @param jeid ID for JournalEntry
     * @param companyid ID for Company
     * @return KwlReturnObject
     * @throws com.krawler.common.service.ServiceException
     * @description Function to get CreditNote using Journal Entry ID and company's ID.
     */
    public KwlReturnObject getCreditNoteFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from CreditNote where journalEntry.ID=? and company.companyID=?";
        List list = executeQuery(selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
        /**
     *
     * @param request
     * @return
     * @throws ServiceException
     * @Description : Advance Search on Detail Table
     */
    public KwlReturnObject getCNDetailsUsingAdvanceSearch(HashMap<String, Object> request) throws ServiceException {
        ArrayList params = new ArrayList();
        if (request.containsKey("Id")) {
            String id = request.get("Id").toString();
            params.add(id);
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
        if (request.containsKey("searchJson") && request.get("searchJson") != null) {
            Searchjson = request.get("searchJson").toString();

            if (!StringUtil.isNullOrEmpty(Searchjson) && lineLevelAmount) {
                try {
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray customSearchFieldArray = new JSONArray();
                    JSONArray defaultSearchFieldArray = new JSONArray();
                    StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);

                    if (customSearchFieldArray.length() > 0) {
                        try {
                            //Advance search case for Custome field
                            request.put(Constants.Searchjson, Searchjson);
                            request.put(Constants.appendCase, appendCase);
                            request.put("filterConjuctionCriteria", filterConjuctionCriteria);

                            mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                            if (mySearchFilterString.contains("accjecustomdata")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "cnd.creditNote.journalEntry.accBillInvCustomData");//
//                                joinString1 = " inner join invoice on invoice.id = invoicedetails.invoice  inner join accjecustomdata on accjecustomdata.journalentryId=invoice.journalentry ";
}
                            if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//
//                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//
//                                joinString1 += " left join accjedetailcustomdata  on accjedetailcustomdata.jedetailId=jedetail.id ";
                            }
                            if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");//    
//                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "accjedetailproductcustomdata");//    
//                                joinString1 += " left join accjedetailproductcustomdata  on accjedetailproductcustomdata.jedetailId=jedetail.id ";
                            }
                            StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                        } catch (ParseException ex) {
                            Logger.getLogger(accCreditNoteImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(accCreditNoteImpl.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

//        String query = " select DISTINCT invoicedetails from invoicedetails  inner join jedetail jed on jedetail.id=invoicedetails.salesjedid  where invoicedetails.invoice =? " + mySearchFilterString;
        String query = " select DISTINCT  cnd from CreditNoteTaxEntry cnd  inner join cnd.totalJED  jed where cnd.creditNote.ID =? " + mySearchFilterString;
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    /* 
     Checking Whether Credit Note is Linked
    
     with Debit note or not
       
     */
    @Override
    public boolean checkCNLinking(String creditnoteID) throws ServiceException {
        try {
            String query = "from CreditNoteLinking cnl where cnl.DocID.ID=?";
            ArrayList params = new ArrayList();
            params.add(creditnoteID);
            List list = executeQuery(query, params.toArray());
            if (list.size() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.checkDNLinking : " + ex.getMessage(), ex);
        }

    }
    
   /*
     * Method for fetching Credit notes with tax
     */ 
    @Override
    public KwlReturnObject getCNWithTax(HashMap<String, Object> map) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String companyId = "";
        String currencyToExclude = "";
        if (map.containsKey("companyId") && map.get("companyId")!=null) {
            companyId = map.get("companyId").toString();
            params.add(companyId);
        }
        if (map.containsKey("currencyNotIn") && map.get("currencyNotIn")!=null) {
            currencyToExclude = map.get("currencyNotIn").toString();
            condition += " and cnt.creditNote.currency.currencyID not in (?) ";
            params.add(currencyToExclude);
        }
        String query = "From CreditNoteTaxEntry cnt where cnt.tax is not null and cnt.gstCurrencyRate = 0 and cnt.company.companyID = ? "+condition;
        list = executeQuery(query, params.toArray());
        
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    public KwlReturnObject getCustomerCreditNoCount(String creditNoteNo, String companyid, String customerId) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from CreditNote where creditNoteNumber=? and company.companyID=? and customer.ID=? and approvestatuslevel=11 and deleted=false";
        list = executeQuery(q, new Object[]{creditNoteNo, companyid, customerId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getVendorCreditNoCount(String creditNoteNo, String companyid, String vendorId) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from CreditNote where creditNoteNumber=? and company.companyID=? and vendor.ID=? and approvestatuslevel=11 and deleted=false";
        list = executeQuery(q, new Object[]{creditNoteNo, companyid, vendorId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getCreditNoteLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(invoiceId);
        String query = "select distinct cnd.creditnote from cndetailsgst cnd, grdetails invd , goodsreceipt inv where inv.id = invd.goodsreceipt and invd.id=cnd.videtails and cnd.company =? and inv.id=?";
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

            
    public String[] columSort(String Col_Name, String Col_Dir) throws ServiceException {
        String[] String_Sort = new String[5];
        if (Col_Name.equals("personname")) {
            String_Sort[0] = " ORDER BY c.name " + Col_Dir;
            String_Sort[1] = ", je.entrydate as date, c.name ";
        } else if (Col_Name.equals("noteno")) {
            String_Sort[0] = " ORDER BY cn.cnnumber " + Col_Dir;
            String_Sort[1] = ", je.entrydate as date, cn.cnnumber ";
        } else if (Col_Name.equals("entryno")) {
            String_Sort[0] = " ORDER BY je.entryno " + Col_Dir;
            String_Sort[1] = ", je.entrydate as date, je.entryno ";
        } else if (Col_Name.equals("date")) {
            String_Sort[0] = " ORDER BY date " + Col_Dir;
            String_Sort[1] = ", je.entrydate as date ";
        } else {
            String_Sort[0] = " ORDER BY date " + Col_Dir;
            String_Sort[1] = ", je.entrydate as date ";
        }
        return String_Sort;
    }
    
    @Override
    public KwlReturnObject saveCreditNoteInvoiceMappingInfo(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        try {
            CreditNoteInvoiceMappingInfo creditNoteInvoiceMappingInfo = new CreditNoteInvoiceMappingInfo();
            if (!StringUtil.isNullOrEmpty(json.optString("creditnoteid"))) {
                creditNoteInvoiceMappingInfo.setCreditNote((CreditNote) get(CreditNote.class, json.optString("creditnoteid")));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("invoiceid"))) {
                creditNoteInvoiceMappingInfo.setInvoice((Invoice) get(Invoice.class, json.optString("invoiceid")));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("goodsReceipt"))) {
                creditNoteInvoiceMappingInfo.setGoodsReceipt((GoodsReceipt) get(GoodsReceipt.class, json.optString("goodsReceipt")));
            }
            save(creditNoteInvoiceMappingInfo);
            list.add(creditNoteInvoiceMappingInfo);
        } catch (Exception e) {
            throw ServiceException.FAILURE("accCreditNoteImpl.saveCreditNoteInvoiceMappingInfo", e);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    @Override
    public KwlReturnObject getCreditNoteInvoiceMappingInfo(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        try {
            List params = new ArrayList();
            StringBuilder hqlQuery = new StringBuilder();
            hqlQuery.append("from CreditNoteInvoiceMappingInfo ");
            if (!StringUtil.isNullOrEmpty(json.optString("creditnoteid",null))) {
                hqlQuery.append(" where creditNote.ID = ? ");
                params.add(json.optString("creditnoteid",null));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("invoiceid",null))) {
                hqlQuery.append(hqlQuery.indexOf("where") == -1 ? " where invoice.ID = ? " : " and invoice.ID = ? ");
                params.add(json.optString("invoiceid", null));
            }
            list = executeQuery(hqlQuery.toString(), params.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("accCreditNoteImpl.getCreditNoteInvoiceMappingInfo", e);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    @Override
    public KwlReturnObject deleteCreditNoteInvoiceMappingInfo(JSONObject json) throws ServiceException {
        ArrayList params = new ArrayList();
        int numRowsTotal = 0;
        try {
            StringBuilder delQuery = new StringBuilder();
            delQuery.append("delete from CreditNoteInvoiceMappingInfo ");
            if (!StringUtil.isNullOrEmpty(json.optString("creditnoteid"))) {
                delQuery.append("where creditNote.ID = ?");
                params.add(json.optString("creditnoteid"));
            }
            if (!StringUtil.isNullOrEmpty(json.optString("invoiceid"))) {
                delQuery.append(delQuery.indexOf("where") == -1 ? " where goodsReceipt.ID = ? " : " and goodsReceipt.ID = ? ");
                params.add(json.optString("invoiceid"));
            }
            numRowsTotal = executeUpdate(delQuery.toString(), params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("", ex);//+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, numRowsTotal);
    }
    
    /**
     * Method is used to line level terms and credit note account detail mapping. It
     * is used for INDIAN country for GST taxes.
     *
     * @param dataMap
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject saveCreditNoteDetailTermMap(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            CreditNoteDetailTermMap termmap = new CreditNoteDetailTermMap();
            if (dataMap.containsKey("id") && !StringUtil.isNullOrEmpty(dataMap.get("id").toString())) {
                termmap = (CreditNoteDetailTermMap) get(CreditNoteDetailTermMap.class, (String) dataMap.get("id"));
                if (termmap == null) {
                    termmap = new CreditNoteDetailTermMap();
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
            if (dataMap.containsKey("creditNoteTaxEntry") && dataMap.get("creditNoteTaxEntry") != null) {
                termmap.setCreditNoteTaxEntry((String) dataMap.get("creditNoteTaxEntry"));
            }
            /**
             * For US Country Credit note Overcharge/Undercharge option save line level detail id
             */
            if (dataMap.containsKey("creditnotedetail") && dataMap.get("creditnotedetail") != null) {
                CreditNoteAgainstVendorGst againstVendorGst = (CreditNoteAgainstVendorGst)get(CreditNoteAgainstVendorGst.class, (String)dataMap.get("creditnotedetail"));
                termmap.setCreditnotedetail(againstVendorGst);
            }
            saveOrUpdate(termmap);
            list.add(termmap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.saveCreditNoteDetailTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getCreditNoteDetailTermMap(JSONObject paramObj) throws ServiceException {
        List params = new ArrayList();
        StringBuilder query = new StringBuilder();
        query.append(" from CreditNoteDetailTermMap rm ");
        if (!StringUtil.isNullOrEmpty(paramObj.optString("creditNoteTaxEntry"))) {
            query.append(" where rm.creditNoteTaxEntry = ?");
            params.add(paramObj.optString("creditNoteTaxEntry"));
        }
        /**
         * Get CreditNoteDetailTermMap against credit note detail in Overcharge and Undercharge
         */
        if (!StringUtil.isNullOrEmpty(paramObj.optString("creditnotedetail"))) {
            query.append(" where rm.creditnotedetail.id = ?");
            params.add(paramObj.optString("creditnotedetail"));
        }
        List list = executeQuery(query.toString(), params.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    /**
     * Delete Credit note detail terms map against Credit note for Overcharge/ Undercharge US Country
     * @param creditNoteID
     * @param companyid
     * @throws ServiceException 
     */
    @Override
    public void deleteCreditNoteDetailTermMapAgainstDebitNote(String creditNoteID, String companyid) throws ServiceException {
        List params = new ArrayList();
        StringBuilder query = new StringBuilder();
        query.append("delete from CreditNoteDetailTermMap cnndtm where cnndtm.creditnotedetail "
                + " in (select cnngst.ID from CreditNoteAgainstVendorGst cnngst where cnngst.creditNote.ID=? and cnngst.company.companyID=?)");
        params.add(creditNoteID);
        params.add(companyid);
        executeUpdate(query.toString(), params.toArray());
    }

    @Override
    public void deleteCreditNoteDetailTermMap(String creditNoteTaxEntry) throws ServiceException {
        StringBuilder query = new StringBuilder();
        query.append("delete from creditnotedetailtermmap ");
        query.append(" where creditnotetaxentry in ("+creditNoteTaxEntry+")");
        executeSQLUpdate(query.toString());
    }

    public void deleteGstTaxClassDetails(String docrefid) throws ServiceException {
        if (!StringUtil.isNullOrEmpty(docrefid)) {
            String delQuery = " delete from gsttaxclasshistory where refdocid IN (select id from cntaxentry where creditnote=?)";
            executeSQLUpdate(delQuery, new Object[]{docrefid});
        }
    }

    public void deleteGstDocHistoryDetails(String docrefid) throws ServiceException {
        if (!StringUtil.isNullOrEmpty(docrefid)) {
            String delQuery = " delete from gstdocumenthistory where refdocid=?";
            executeSQLUpdate(delQuery, new Object[]{docrefid});
        }
    }

    @Override
    public KwlReturnObject deleteCreditNoteForOverchargeDetails(String cnId, String companyId) throws ServiceException {
        String query = " delete from CreditNoteAgainstVendorGst cngst where cngst.creditNote.ID = ? and cngst.company.companyID = ? ";
        int numRows = executeUpdate(query, new Object[]{cnId, companyId});
        return new KwlReturnObject(true, null, null, null, numRows);
    }
    
    @Override
    public void saveCreditNoteOverchargeAmountLinking(JSONObject paramJObj) throws ServiceException {
         try {
            String UUIDstr = UUID.randomUUID().toString();
            String companyid = paramJObj.optString(Constants.companyid, "");
            String cnid = paramJObj.optString("cnid","");
            String linkedDocId = paramJObj.optString("linkedDocId","");
            String linkedCNKnowkOffAmount = paramJObj.optString("linkedCNKnowkOffAmount","");
            String sqlQuery = "INSERT INTO cnoverchargelinkamountdetails VALUES (?,?,?,?,?) ";
            executeSQLUpdate(sqlQuery, new Object[]{UUIDstr, cnid, linkedDocId, linkedCNKnowkOffAmount, companyid});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteImpl.saveCreditNoteOverchargeAmountLinking : " + ex.getMessage(), ex);
        }
        
    }
    @Override
    public List getCreditNoteOverchargeAmountLinking(JSONObject paramJObj) throws ServiceException {
        ArrayList params = new ArrayList();
        String cnid = paramJObj.optString("cnid","");
        String linkedDocId = paramJObj.optString("linkedDocId",""); 
        String companyid = paramJObj.optString(Constants.companyid,"");
        params.add(cnid);
        params.add(linkedDocId);
        params.add(companyid);
        String query = "select cnd.linkedcnamount from cnoverchargelinkamountdetails cnd where cnd.cnid=? and cnd.linkeddocid = ? and cnd.company = ?";
        List list = executeSQLQuery( query, params.toArray());
        return list;
    }
    
    @Override
    public void deleteCreditNoteOverchargeAmountLinking(JSONObject paramJObj) throws ServiceException {
        ArrayList params = new ArrayList();
        String cnid = paramJObj.optString("cnid", "");
        String companyid = paramJObj.optString(Constants.companyid, "");
        String condition = " cnid = ? and company = ?";
        params.add(cnid);
        params.add(companyid);
        if (paramJObj.has("linkedDocId")) {
            String linkedDocId = paramJObj.optString("linkedDocId", "");
            condition += " and linkeddocid = ?";
            params.add(linkedDocId);
        }

        String query = "delete from cnoverchargelinkamountdetails where " + condition;
        executeSQLUpdate(query, params.toArray());

    }
    
    public List getCNKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException{
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
            requestParams.remove("isOpeningBalanceCN");
            JSONObject advSearchQueryObj = getAdvanceSearchForCustomQuery(requestParams, paramsAdvSearch1, paramsAdvSearch, "");            
            String jeid = " jedetail.id = creditnote.centry";
            if(advSearchQueryObj.has("jeid") && !StringUtil.isNullOrEmpty(advSearchQueryObj.getString("jeid"))){
                jeid= advSearchQueryObj.getString("jeid");
            }
            String joinString1 = advSearchQueryObj.getString("joinString1");
            String mySearchFilterString = custQuery+ advSearchQueryObj.getString("mySearchFilterString");
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
            
            String conditionSQL="";
            if (requestParams.containsKey("groupcombo") && requestParams.get("groupcombo") != null && requestParams.containsKey(Constants.globalCurrencyKey) && requestParams.get(Constants.globalCurrencyKey) != null) {
                int groupcombo = (Integer) requestParams.get("groupcombo");
                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    conditionSQL += " where cn.doccurrency=" + Integer.parseInt((String) requestParams.get(Constants.globalCurrencyKey));
                } else if (groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    conditionSQL += " where cn.doccurrency!=" + Integer.parseInt((String) requestParams.get(Constants.globalCurrencyKey));
                }
            }

            //global search
            String ss = (requestParams.containsKey("ss") && requestParams.get("ss") != null) ? (String) requestParams.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                try {
                    String[] searchcol = new String[]{"cn.customername","cn.custaliasname","cn.custcode", "cn.docnumber", "cn.accountname"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 5); 
                    StringUtil.insertParamSearchString(map);
                    String queryStart = "and";
                    if(StringUtil.isNullOrEmpty(conditionSQL)){
                        queryStart = "where";
                    }
                    String searchQuery = StringUtil.getSearchString(ss, queryStart, searchcol);
                    conditionSQL += searchQuery + " AND cn.custcode IS NOT NULL ";
                } catch (SQLException ex) {
                    Logger.getLogger(accCreditNoteImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            String salesPerosnJoinType = "left join";
            if (isSalesPersonAgedReport) {
                salesPerosnJoinType = "inner join";
            } 
            String sql = "select cn.docid, cn.docnumber, SUM(cn.amount), SUM(cn.amountinbase), SUM(cn.koamt), SUM(cn.koamtbase), cn.doctype, cn.docterm, cn.creationdate, cn.duedate, cn.salespersonname, cn.salespersoncode, cn.salespersonid, cn.entryno, cn.entrydate, cn.isOpeningBalanceTransaction, cn.customerid, cn.customername, cn.custaliasname, cn.custcode, cn.customertermname, cn.customertermid, cn.customercreditlimit, cn.memo, cn.exchangerate,cn.doccurrency, cn.doccurrencyname, cn.doccurrencycode, cn.doccurrencysymbol,cn.companyname,cn.shipdate, cn.basecurrencysymbol, cn.accountname from (\n"
                    + "SELECT  creditnote.id as docid, creditnote.cnnumber as docnumber , jedetail.amount as amount, jedetail.amountinbase,0 as koamtbase,0 as koamt, 'Credit Note' as doctype, ' ' as docterm, creditnote.creationdate, creditnote.creationdate as duedate, COALESCE(masteritem.value,' ') as salespersonname, COALESCE(masteritem.code,' ') as salespersoncode, COALESCE(masteritem.id,' ') as salespersonid, je.entryno, je.entrydate, '0' as isOpeningBalanceTransaction, cust.id as customerid, cust.name as customername, cust.aliasname as custaliasname, cust.acccode as custcode, custcredit.termname as customertermname, custcredit.termid as customertermid, cust.creditlimit as customercreditlimit, creditnote.memo,if(je.externalcurrencyrate=0,1/exchangerate_calc(creditnote.company,creditnote.creationdate,creditnote.currency,company.currency),je.externalcurrencyrate) as exchangerate,creditnote.currency as doccurrency, cncurr.name as doccurrencyname, cncurr.currencycode as doccurrencycode, cncurr.symbol as doccurrencysymbol,company.companyname, ' ' as shipdate, compcurr.symbol as basecurrencysymbol, account.name as accountname \n"
                    + "from creditnote \n"
                    + "INNER JOIN journalentry je ON creditnote.journalentry=je.id \n"
                    + "inner join jedetail on "+jeid+" \n"
                    + "INNER JOIN customer cust ON cust.id=creditnote.customer \n"
                    + "LEFT JOIN account ON account.id=creditnote.account \n"
                    +  salesPerosnJoinType +" masteritem on masteritem.id = creditnote.salesperson\n"
                    + "inner join creditterm custcredit on cust.creditterm = custcredit.termid\n"
                    + "INNER JOIN company on creditnote.company = company.companyid \n"
                    + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                    + "INNER JOIN currency cncurr on creditnote.currency = cncurr.currencyid \n"
                    +joinString1
                    + "where creditnote.company = ? and creditnote.creationdate <= ? and creditnote.approvestatuslevel = '11' and creditnote.deleteflag='F'  and creditnote.cntype!=4 \n"
                    + mySearchFilterString
                    + " \n"
                    + "group by creditnote.cnnumber \n"
                    + " UNION \n"
                    + "SELECT  creditnote.id as docid, creditnote.cnnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(ldr.amountincncurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(creditnote.company,creditnote.creationdate,creditnote.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase,SUM(ldr.amountincncurrency) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from creditnote \n"
                    + "INNER JOIN journalentry je ON creditnote.journalentry=je.id \n"
                    + "inner join jedetail on "+jeid+" \n"
                    + "INNER JOIN customer cust ON cust.id=creditnote.customer \n"
                    + "INNER JOIN company on creditnote.company = company.companyid \n"
                    + "INNER JOIN linkdetailpaymenttocreditnote ldr on ldr.creditnote=creditnote.id and ldr.paymentlinkdate<=? and ldr.company=creditnote.company \n"
                    + "where creditnote.company = ? and creditnote.creationdate <= ? and creditnote.approvestatuslevel = '11' and creditnote.deleteflag='F'  and creditnote.cntype!=4 \n"
                    + " \n"
                    + "group by creditnote.cnnumber \n"
                    + " UNION \n"
                    + "SELECT  creditnote.id as docid, creditnote.cnnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(cnp.amountinbasecurrency) as koamtbase, SUM(cnp.amountinbasecurrency/if(creditnote.exchangerateforopeningtransaction = 0, exchangerate_calc(creditnote.company,creditnote.creationdate,creditnote.currency, company.currency),if(creditnote.isconversionratefromcurrencytobase=1,creditnote.exchangerateforopeningtransaction, 1/creditnote.exchangerateforopeningtransaction))) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from creditnote \n"
                    + "INNER JOIN journalentry je ON creditnote.journalentry=je.id \n"
                    + "inner join jedetail on "+jeid+" \n"
                    + "INNER JOIN customer cust ON cust.id=creditnote.customer \n"
                    + "INNER JOIN company on creditnote.company = company.companyid \n"
                    + "INNER JOIN creditnotpayment cnp on cnp.cnid = creditnote.id \n"
                    + "INNER JOIN payment pt on cnp.paymentid = pt.id and pt.creationdate <= ?\n"
                    + "where creditnote.company = ? and creditnote.creationdate <= ? and creditnote.approvestatuslevel = '11' and creditnote.deleteflag='F'  and creditnote.cntype!=4 \n"
                    + " \n"
                    + "group by creditnote.cnnumber \n"
                    + " UNION \n"
                    + "SELECT  creditnote.id as docid, creditnote.cnnumber as docnumber,  0 as amount, 0 as amountinbase,SUM((if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount))/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(creditnote.company,creditnote.creationdate,creditnote.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase,SUM(COALESCE(if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount),0)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from creditnote \n"
                    + "INNER JOIN journalentry je ON creditnote.journalentry=je.id \n"
                    + "inner join jedetail on "+jeid+" \n"
                    + "INNER JOIN customer cust ON cust.id=creditnote.customer \n"
                    + "INNER JOIN company on creditnote.company = company.companyid \n"
                    + "INNER JOIN cndetails cnd on cnd.creditnote=creditnote.id and cnd.company=creditnote.company and (cnd.invoice is not null or cnd.debitnoteid is not null) and cnd.invoicelinkdate<=?\n"
                    + "INNER JOIN discount on cnd.discount=discount.id \n"
                    + "where creditnote.company = ? and creditnote.creationdate <= ? and creditnote.approvestatuslevel = '11' and creditnote.deleteflag='F'  and creditnote.cntype!=4 \n"
                    + " \n"
                    + "group by creditnote.cnnumber \n"
                    + ") cn " + conditionSQL +" group by cn.docnumber order by cn.creationdate desc";;
            
            ll = executeSQLQuery(sql, params.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("error", ex);
        }
        return ll;
    }
    
    public List getOpeningCNKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException{
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
            requestParams.put("isOpeningBalanceCN",true);
            JSONObject advSearchQueryObj = getAdvanceSearchForCustomQuery(requestParams, paramsAdvSearch1, paramsAdvSearch, "");            
            String jeid = " jed.id = creditnote.centry";
            if(advSearchQueryObj.has("jeid") && !StringUtil.isNullOrEmpty(advSearchQueryObj.getString("jeid"))){
                jeid= advSearchQueryObj.getString("jeid");
            }
            String joinString1 = advSearchQueryObj.getString("joinString1");
            String mySearchFilterString = custQuery+advSearchQueryObj.getString("mySearchFilterString");
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
            
            String conditionSQL="";
            if (requestParams.containsKey("groupcombo") && requestParams.get("groupcombo") != null && requestParams.containsKey(Constants.globalCurrencyKey) && requestParams.get(Constants.globalCurrencyKey) != null) {
                int groupcombo = (Integer) requestParams.get("groupcombo");
                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    conditionSQL += " where cn.doccurrency=" + Integer.parseInt((String) requestParams.get(Constants.globalCurrencyKey));
                } else if (groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    conditionSQL += " where cn.doccurrency!=" + Integer.parseInt((String) requestParams.get(Constants.globalCurrencyKey));
                }
            }
            //global search
            String ss = (requestParams.containsKey("ss") && requestParams.get("ss") != null) ? (String) requestParams.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                try {
                    String[] searchcol = new String[]{"cn.customername","cn.custaliasname","cn.custcode", "cn.docnumber", "cn.accountname"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 5); 
                    StringUtil.insertParamSearchString(map);
                    String queryStart = "and";
                    if(StringUtil.isNullOrEmpty(conditionSQL)){
                        queryStart = "where";
                    }
                    String searchQuery = StringUtil.getSearchString(ss, queryStart, searchcol);
                    conditionSQL += searchQuery + " AND cn.custcode IS NOT NULL ";
                } catch (SQLException ex) {
                    Logger.getLogger(accCreditNoteImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            String salesPerosnJoinType = "left join";
            if (isSalesPersonAgedReport) {
                salesPerosnJoinType = "inner join";
            } 
            
            String sql = "select cn.docid, cn.docnumber, SUM(cn.amount), SUM(cn.amountinbase), SUM(cn.koamt), SUM(cn.koamtbase), cn.doctype, cn.docterm, cn.creationdate, cn.duedate, cn.salespersonname, cn.salespersoncode, cn.salespersonid, cn.entryno, cn.entrydate, cn.isOpeningBalanceTransaction, cn.customerid, cn.customername, cn.custaliasname, cn.custcode, cn.customertermname, cn.customertermid, cn.customercreditlimit, cn.memo, cn.exchangerate,cn.doccurrency, cn.doccurrencyname, cn.doccurrencycode, cn.doccurrencysymbol,cn.companyname,cn.shipdate, cn.basecurrencysymbol, cn.accountname from (\n"
                    + "SELECT creditnote.id as docid, creditnote.cnnumber as docnumber, creditnote.cnamount as amount, creditnote.originalopeningbalancebaseamount as amountinbase,0 as koamtbase,0 as koamt, 'Credit Note' as doctype, ' ' as docterm, creditnote.creationdate, creditnote.creationdate as duedate, COALESCE(masteritem.value,' ') as salespersonname, COALESCE(masteritem.code,' ') as salespersoncode, COALESCE(masteritem.id,' ') as salespersonid, ' ' as entryno, ' ' as entrydate, '0' as isOpeningBalanceTransaction, cust.id as customerid, cust.name as customername, cust.aliasname as custaliasname, cust.acccode as custcode, custcredit.termname as customertermname, custcredit.termid as customertermid, cust.creditlimit as customercreditlimit, creditnote.memo,if(creditnote.isconversionratefromcurrencytobase=1,1/creditnote.exchangerateforopeningtransaction, creditnote.exchangerateforopeningtransaction) as exchangerate,creditnote.currency as doccurrency, cncurr.name as doccurrencyname, cncurr.currencycode as doccurrencycode, cncurr.symbol as doccurrencysymbol,company.companyname, ' ' as shipdate, compcurr.symbol as basecurrencysymbol, account.name as accountname\n"
                    + "from creditnote\n"
                    + "INNER JOIN company on creditnote.company = company.companyid \n"
                    + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                    + "INNER JOIN customer cust on cust.id = creditnote.customer \n"
                    + "LEFT JOIN account ON account.id=creditnote.account \n"
                    +  salesPerosnJoinType +" masteritem on masteritem.id = creditnote.salesperson\n"
                    + "inner join creditterm custcredit on cust.creditterm = custcredit.termid\n"
                    + "INNER JOIN currency cncurr on creditnote.currency = cncurr.currencyid \n"
                    +joinString1
                    + "where creditnote.isopeningbalencecn=1 and creditnote.iscnforcustomer=1 and creditnote.deleteflag='F' and creditnote.company = ? and creditnote.creationdate <= ? \n"
                    + mySearchFilterString+ " \n"
                    + "group by creditnote.cnnumber \n"
                    + "\n"
                    + " UNION \n"
                    + "SELECT  creditnote.id as docid, creditnote.cnnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(cnp.amountinbasecurrency) as koamtbase, SUM(cnp.amountinbasecurrency/if(creditnote.isconversionratefromcurrencytobase=1,creditnote.exchangerateforopeningtransaction, 1/creditnote.exchangerateforopeningtransaction)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from creditnote\n"
                    + "INNER JOIN company on creditnote.company = company.companyid \n"
                    + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                    + "INNER JOIN currency cncurr on creditnote.currency = cncurr.currencyid \n"
                    + "INNER JOIN customer cust on cust.id = creditnote.customer \n"
                    + "INNER JOIN creditnotpayment cnp on cnp.cnid = creditnote.id \n"
                    + "INNER JOIN payment pt on pt.id = cnp.paymentid and pt.creationdate <= ?\n"
                    + "where creditnote.isopeningbalencecn=1 and creditnote.iscnforcustomer=1 and creditnote.deleteflag='F' and creditnote.company = ? and creditnote.creationdate <= ? \n"
                    + "group by creditnote.cnnumber \n"
                    + "\n"
                    + " UNION \n"
                    + "SELECT  creditnote.id as docid, creditnote.cnnumber as docnumber, 0 as amount, 0 as amountinbase,SUM((if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount))*if(creditnote.isconversionratefromcurrencytobase=1,creditnote.exchangerateforopeningtransaction, 1/creditnote.exchangerateforopeningtransaction)) as koamtbase,SUM(COALESCE(if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount),0)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from creditnote\n"
                    + "INNER JOIN company on creditnote.company = company.companyid \n"
                    + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                    + "INNER JOIN customer cust on cust.id = creditnote.customer \n"
                    + "INNER JOIN cndetails cnd on cnd.creditnote=creditnote.id and cnd.company=creditnote.company and (cnd.invoice is not null or cnd.debitnoteid is not null) and cnd.invoicelinkdate<=?\n"
                    + "INNER JOIN currency cncurr on creditnote.currency = cncurr.currencyid \n"
                    + "INNER JOIN discount on cnd.discount=discount.id \n"
                    + "where creditnote.isopeningbalencecn=1 and creditnote.iscnforcustomer=1 and creditnote.deleteflag='F' and creditnote.company = ? and creditnote.creationdate <= ? \n"
                    + "group by creditnote.cnnumber \n"
                    + "\n"
                    + " UNION \n"
                    + "SELECT  creditnote.id as docid, creditnote.cnnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(ldr.amountincncurrency*if(creditnote.isconversionratefromcurrencytobase=1,creditnote.exchangerateforopeningtransaction, 1/creditnote.exchangerateforopeningtransaction)) as koamtbase,SUM(ldr.amountincncurrency) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from creditnote\n"
                    + "INNER JOIN company on creditnote.company = company.companyid \n"
                    + "INNER JOIN currency cncurr on creditnote.currency = cncurr.currencyid \n"
                    + "INNER JOIN customer cust on cust.id = creditnote.customer \n"
                    + "INNER JOIN linkdetailpaymenttocreditnote ldr on ldr.creditnote=creditnote.id and ldr.paymentlinkdate<=? and ldr.company=creditnote.company\n"
                    + "where creditnote.isopeningbalencecn=1 and creditnote.iscnforcustomer=1 and creditnote.deleteflag='F' and creditnote.company = ? and creditnote.creationdate <= ?\n"
                    + "group by creditnote.cnnumber \n"
                    + ") cn " + conditionSQL +" group by cn.docnumber order by cn.creationdate desc";
            
            ll = executeSQLQuery(sql, params.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("error", ex);
        }
        return ll;
    }
    
    private JSONObject getAdvanceSearchForCustomQuery(Map<String, Object> request, ArrayList params, ArrayList paramsSQLWithoutInv, String searchDefaultFieldSQL) throws JSONException, ServiceException {
        JSONObject returnObj = new JSONObject();
        boolean isOpeningBalanceCN = false;
        if (request.get("isOpeningBalanceCN") != null) {
            isOpeningBalanceCN = (Boolean) request.get("isOpeningBalanceCN");
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
        String Searchjson = "";
       try{
        if (request.containsKey("searchJson") && request.get("searchJson") != null) {
            if(request.containsKey("searchJsonCreditNote") && request.get("searchJsonCreditNote") != null)
            {
                Searchjson = StringUtil.DecodeText(request.get("searchJsonCreditNote").toString());
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
                        request.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                            request.put("isOpeningBalance", isOpeningBalanceCN);
                        if (isOpeningBalanceCN) {
                            mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "openingbalancecreditnotecustomdata");//    
                            mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceCreditNoteCustomData", "openingbalancecreditnotecustomdata");//    
//                        mySearchFilterStringforOpeningTransaction = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                            joinString1 = " inner join openingbalancecreditnotecustomdata on openingbalancecreditnotecustomdata.openingbalancecreditnoteid=creditnote.id ";                            
                        } else {
                            mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
//                            mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                            if (mySearchFilterString.contains("accjecustomdata") || mySearchFilterString.contains("AccJECustomData")) {
                                joinString1 = " inner join accjecustomdata on accjecustomdata.journalentryId=creditnote.journalentry ";
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "accjecustomdata");//    
                            }
                            StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                            if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//    
                                joinString1 += " left join accjedetailcustomdata  on accjedetailcustomdata.jedetailId=jedetail.id ";
                                jeid = " jedetail.journalentry = creditnote.journalentry ";
                            }
                            if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "accjedetailproductcustomdata");//    
                                joinString1 += " left join accjedetailproductcustomdata  on accjedetailproductcustomdata.jedetailId=jedetail.id ";
                                jeid = " jedetail.journalentry = creditnote.journalentry ";
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
            Logger.getLogger(accCreditNoteImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accCreditNoteImpl.getAdvanceSearchForCustomQuery:" + ex.getMessage(), ex);
        }
        return returnObj;
    }
}
