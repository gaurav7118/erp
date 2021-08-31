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
package com.krawler.spring.accounting.goodsreceipt;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.spring.accounting.account.accAccountDAOImpl;
import com.krawler.spring.accounting.costCenter.CCConstants;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants.NEWVENDORID;
import com.krawler.spring.accounting.goodsreceipt.dm.GoodsReceiptInfo;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigInteger;
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
public class accGoodsReceiptImpl extends BaseDAO implements accGoodsReceiptDAO, GoodsReceiptConstants {
    
    @Override
    public void saveOrUpdateObj(Object obj) throws ServiceException{
        if(obj != null){
            saveOrUpdate(obj);
        }
    }
    
    @Override
    public Map<String, GoodsReceiptInfo> getGoodsReceiptInfoList(List<String> invoiceIDLIST) throws ServiceException {
        Map<String, GoodsReceiptInfo> invoiceMap = new HashMap<String, GoodsReceiptInfo>();
        if (invoiceIDLIST != null && !invoiceIDLIST.isEmpty()) {
            String query = "select  gr.ID, gr, gr.vendor, "
                    + " gr.company "
                    + " from GoodsReceipt gr"
                    + " where gr.ID in (:invoiceIDList)";
            List<List> values = new ArrayList<List>();
            values.add(invoiceIDLIST);
            List<Object[]> results = executeCollectionQuery(query, Collections.singletonList("invoiceIDList"), values);

            if (results != null) {
                for (Object[] result : results) {
                    String invID = (String) result[0];
                    GoodsReceiptInfo info = new GoodsReceiptInfo();
                    info.setGrID(invID);
                    info.setVendor((Vendor) result[2]);
                    info.setCompany((Company) result[3]);
                    invoiceMap.put(invID, info);
                }
            }
        }
        return invoiceMap;
    }

    @Override
    public KwlReturnObject addGoodsReceipt(Map<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        String companyid = "";
        GoodsReceipt receipt = new GoodsReceipt();
        try {
             if (hm.containsKey("companyid")) {
                 companyid = (String) hm.get("companyid");
             }
            if (hm.containsKey(GRID)) {
                if (!StringUtil.isNullOrEmpty((String) hm.get(GRID))) {
                    receipt = (GoodsReceipt) get(GoodsReceipt.class, (String) hm.get(GRID));
                } else {
                    if (hm.containsKey("createdby")) {
                        User createdby = hm.get("createdby") == null ? null : (User) get(User.class, (String) hm.get("createdby"));
                        receipt.setCreatedby(createdby);
                    }
                    if (hm.containsKey("createdon")) {
                        receipt.setCreatedon((Long) hm.get("createdon"));
                    }
                }
                if (hm.containsKey("modifiedby")) {
                    User modifiedby = hm.get("modifiedby") == null ? null : (User) get(User.class, (String) hm.get("modifiedby"));
                    receipt.setModifiedby(modifiedby);
                }
                if (hm.containsKey("updatedon")) {
                    receipt.setUpdatedon((Long) hm.get("updatedon"));
                }
            } else {
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
            }

            if (hm.containsKey(ENTRYNUMBER)) {
                receipt.setGoodsReceiptNumber((String) hm.get(ENTRYNUMBER));
            }
            if (hm.containsKey(Constants.SEQFORMAT)) {
                receipt.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) hm.get(Constants.SEQFORMAT)));
            }
            if (hm.containsKey(Constants.SEQNUMBER) && hm.get(Constants.SEQNUMBER)!=null && !StringUtil.isNullOrEmpty(hm.get(Constants.SEQNUMBER).toString())) {
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
            if (hm.containsKey(AUTOGENERATED)) {
                receipt.setAutoGenerated((Boolean) hm.get(AUTOGENERATED));
            }
            if (hm.containsKey(ISEXPENSETYPE)) {
                receipt.setIsExpenseType((Boolean) hm.get(ISEXPENSETYPE));
            }
            if (hm.containsKey("isFixedAsset")) {
                receipt.setFixedAssetInvoice((Boolean) hm.get("isFixedAsset"));
            }
            if (hm.containsKey("isConsignment")) {
                receipt.setIsconsignment((Boolean) hm.get("isConsignment"));
            }
            if (hm.containsKey("isMRPJOBWORKIN")) {
                receipt.setIsMRPJobWorkIN((Boolean) hm.get("isMRPJOBWORKIN"));
            }
            // Set isDraft flag in purchase invoice
            if (hm.containsKey("isDraft") && hm.get("isDraft") != null) {  // If Save As Draft
                receipt.setIsDraft((Boolean) hm.get("isDraft"));
            }
            if (hm.containsKey("isCapitalGoodsAcquired")) {
                receipt.setCapitalGoodsAcquired((Boolean) hm.get("isCapitalGoodsAcquired"));
            }
            if (hm.containsKey("isRetailPurchase") && hm.get("isRetailPurchase") != null) {
                receipt.setRetailPurchase((Boolean) hm.get("isRetailPurchase"));
            }
            if (hm.containsKey("importService")) {
                receipt.setImportService((Boolean) hm.get("importService"));
            }
            if (hm.containsKey("isExciseInvoice")) {
                receipt.setIsExciseInvoice((Boolean) hm.get("isExciseInvoice"));
            }
            if (hm.containsKey("defaultnatureofpurchase")) {
                receipt.setDefaultnatureOfPurchase((String) hm.get("defaultnatureofpurchase"));
            }
            if (hm.containsKey("isFromPOS")) {
                receipt.setIsFromPOS((Boolean) hm.get("isFromPOS"));
            }
            if (hm.containsKey("manufacturertype")) {
                receipt.setManufacturerType((String) hm.get("manufacturertype"));
            }
            if (hm.containsKey(MEMO)) {
                receipt.setMemo((String) hm.get(MEMO));
            }
//            if (hm.containsKey(BILLTO)) {
//                receipt.setBillFrom((String) hm.get(BILLTO));
//            }
//            if (hm.containsKey(SHIPADDRESS)) {
//                receipt.setShipFrom((String) hm.get(SHIPADDRESS));
//            }
            if (hm.containsKey(SHIPDATE)) {
                receipt.setShipDate((Date) hm.get(SHIPDATE));
            }
            if (hm.containsKey(DUEDATE)) {
                receipt.setDueDate((Date) hm.get(DUEDATE));
            }
            if (hm.containsKey("shipvia")) {
                receipt.setShipvia((String) hm.get("shipvia"));
            }
            if (hm.containsKey("fob")) {
                receipt.setFob((String) hm.get("fob"));
            }
            if (hm.containsKey(DISCOUNTID)) {
                Discount dsc = hm.get(DISCOUNTID) == null ? null : (Discount) get(Discount.class, (String) hm.get(DISCOUNTID));
                receipt.setDiscount(dsc);
            }
            if (hm.containsKey(termid)) {
                //if cash purchase then don't put term
                if(hm.containsKey(INCASH) && hm.get(INCASH) != null && !Boolean.parseBoolean(hm.get(INCASH).toString())){
                    Term term = hm.get("termid") == null ? null : (Term) get(Term.class, (String) hm.get("termid"));
                    receipt.setTermid(term);
                }
            }
            if (hm.containsKey(TAXID)) {
                Tax tax = hm.get(TAXID) == null ? null : (Tax) get(Tax.class, (String) hm.get(TAXID));
                receipt.setTax(tax);
            }
            if (hm.containsKey("taxAmount")) {
               receipt.setTaxamount((Double) hm.get("taxAmount"));
            }
            if (hm.containsKey("taxAmountInBase")) {
               receipt.setTaxamountinbase((Double) hm.get("taxAmountInBase"));
            }
            if (hm.containsKey("excludingGstAmount")) {
               receipt.setExcludingGstAmount((Double) hm.get("excludingGstAmount"));
            }
            if (hm.containsKey("excludingGstAmountInBase")) {
               receipt.setExcludingGstAmountInBase((Double) hm.get("excludingGstAmountInBase"));
            }
            if (hm.containsKey(VENDORENTRYID)) {
                JournalEntryDetail vendorje = hm.get(VENDORENTRYID) == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get(VENDORENTRYID));
                receipt.setVendorEntry(vendorje);
            }
            if (hm.containsKey(Constants.RoundingAdjustmentEntryID)) {
                JournalEntryDetail roundingje = hm.get(Constants.RoundingAdjustmentEntryID) == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get(Constants.RoundingAdjustmentEntryID));
                receipt.setRoundingAdjustmentEntry(roundingje);
            }
            if (hm.containsKey(Constants.IsRoundingAdjustmentApplied) && hm.get(Constants.IsRoundingAdjustmentApplied)!=null) {
                boolean isRoundingAdjustmentApplied = Boolean.parseBoolean(hm.get(Constants.IsRoundingAdjustmentApplied).toString());
                receipt.setIsRoundingAdjustmentApplied(isRoundingAdjustmentApplied);
            }
            if (hm.containsKey(SHIPENTRYID)) {
                JournalEntryDetail shipje = hm.get(SHIPENTRYID) == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get(SHIPENTRYID));
                receipt.setShipEntry(shipje);
            }
            if (hm.containsKey("otherentryid")) {
                JournalEntryDetail otherje = hm.get("otherentryid") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("otherentryid"));
                receipt.setOtherEntry(otherje);
            }
            if (hm.containsKey(TAXENTRYID)) {
                JournalEntryDetail taxje = hm.get(TAXENTRYID) == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get(TAXENTRYID));
                receipt.setTaxEntry(taxje);
            }
            if (hm.containsKey(CURRENCYID)) {
                KWLCurrency currency = hm.get(CURRENCYID) == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) hm.get(CURRENCYID));
                receipt.setCurrency(currency);
            }
            if (hm.containsKey(COMPANYID)) {
                Company company = hm.get(COMPANYID) == null ? null : (Company) get(Company.class, (String) hm.get(COMPANYID));
                receipt.setCompany(company);
            }
            if (hm.containsKey("pendingapproval")) {
                receipt.setPendingapproval((Integer) hm.get("pendingapproval"));
            } else {
                receipt.setPendingapproval(0);
            }
            if (hm.containsKey("approvalstatuslevel")) {
                receipt.setApprovestatuslevel((Integer) hm.get("approvalstatuslevel"));
            } else if (hm.containsKey("isOpeningBalenceInvoice") && hm.get("isOpeningBalenceInvoice") != null && (Boolean) hm.get("isOpeningBalenceInvoice")) {
                receipt.setApprovestatuslevel(11);
            } else{
                if (!hm.containsKey("isEditedPendingDocument")) {//If edited pending Document then Approvalstatus level is being set to 0
                    receipt.setApprovestatuslevel(0);
                }             
            }
            if (hm.containsKey("istemplate")) {
                receipt.setIstemplate((Integer) hm.get("istemplate"));
            } else {
                receipt.setIstemplate(0);
            }
            if (hm.containsKey("journalentryid")) {
                JournalEntry je = hm.get("journalentryid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) hm.get("journalentryid"));
                receipt.setJournalEntry(je);
            }
            if (hm.containsKey(ERDID)) {
                ExchangeRateDetails erd = hm.get(ERDID) == null ? null : (ExchangeRateDetails) get(ExchangeRateDetails.class, (String) hm.get(ERDID));
                receipt.setExchangeRateDetail(erd);
            }
            if (hm.containsKey(VENDORID)) {
                Vendor vendor = hm.get(VENDORID) == null ? null : (Vendor) get(Vendor.class, (String) hm.get(VENDORID));
                receipt.setVendor(vendor);
            }
            if (hm.containsKey(GRDETAILS)) {
                receipt.setRows((Set<GoodsReceiptDetail>) hm.get(GRDETAILS));
            }
             //Setting landed invoice JE if landed cost feature is on.
             if (hm.containsKey("landedInvoiceJE")) {
                JournalEntry je = hm.get("landedInvoiceJE") == null ? null : (JournalEntry) get(JournalEntry.class, (String) hm.get("landedInvoiceJE"));
                receipt.setLandedInvoiceJE(je);
            }
            if (hm.containsKey("posttext")) {

                receipt.setPostText((String) hm.get("posttext"));
            }
            if (hm.containsKey(EXPENSEGRDETAILS)) {
                receipt.setExpenserows((Set<ExpenseGRDetail>) hm.get(EXPENSEGRDETAILS));
            }
            if (hm.containsKey("selfBilledInvoice")) {
                if (hm.get("selfBilledInvoice") != null) {
                    receipt.setSelfBilledInvoice(Boolean.parseBoolean(hm.get("selfBilledInvoice").toString()));
                }
            }
            if (hm.containsKey("RMCDApprovalNo")) {
                if (hm.get("RMCDApprovalNo") != null) {
                    receipt.setRMCDApprovalNo((String) hm.get("RMCDApprovalNo"));
                }
            }
            if (hm.containsKey("shipLength") && !StringUtil.isNullOrEmpty((String)hm.get("shipLength"))) {
                receipt.setShiplength(Double.parseDouble((String) hm.get("shipLength")));
            }
            if (hm.containsKey("invoicetype") && hm.get("invoicetype") != null) {
                receipt.setInvoicetype((String) hm.get("invoicetype"));
            }
            if (hm.containsKey("formtype") && hm.get("formtype") != null  && !StringUtil.isNullOrEmpty((String)hm.get("formtype"))) {  //"1","Without Form"
                receipt.setFormtype((String) hm.get("formtype"));
                if(hm.get("formtype").equals("1") || hm.get("formtype").equals("0")){
                    receipt.setFormstatus("1");//NA
                } else if(receipt.getFormstatus() != null){
                    receipt.setFormstatus(receipt.getFormstatus());
                } else{
                    receipt.setFormstatus("2");//Pending
                }
            }
            if (hm.containsKey("gtaapplicable") && hm.get("gtaapplicable")!=null){  //Special Case - Service Tax ( India Compliance)
                   receipt.setGtaapplicable((Boolean) hm.get("gtaapplicable"));
            }
            if (hm.containsKey("gstapplicable") && hm.get("gstapplicable") != null) {  // If New GST Appliled
                receipt.setIsIndGSTApplied((Boolean) hm.get("gstapplicable"));
            }
            /*---Dropship PI is not eligible for further linking in PR & GR---- */
            if (hm.containsKey("isdropshipchecked") && hm.get("isdropshipchecked") != null) {  // If New GST Appliled
                receipt.setIsDropshipDocument((Boolean) hm.get("isdropshipchecked"));
                receipt.setIsOpenInGR(false);
                receipt.setIsOpenInPR(false);
            }
            if (hm.containsKey(MARKED_FAVOURITE)) {
                if (hm.get(MARKED_FAVOURITE) != null) {
                    receipt.setFavourite(Boolean.parseBoolean(hm.get(MARKED_FAVOURITE).toString()));
                }
            }
//           if(hm.containsKey("venbilladdress")){
//               if(hm.get("venbilladdress")!=null){
//                 receipt.setBillTo((String)hm.get("venbilladdress"));  
//               }               
//           }
//           if(hm.containsKey("venshipaddress")){
//               if(hm.get("venshipaddress")!=null){
//                 receipt.setShipTo((String)hm.get("venshipaddress"));  
//               }               
//           }
            if (hm.containsKey("partyInvoiceNumber")) {
                if (hm.get("partyInvoiceNumber") != null) {
                    receipt.setPartyInvoiceNumber((String) hm.get("partyInvoiceNumber"));
                }
            }
            if (hm.containsKey("salesPerson")) {
                if (hm.get("salesPerson") != null) {
                    receipt.setMasterSalesPerson((MasterItem) get(MasterItem.class, (String) hm.get("salesPerson")));
                }
            }
            if (hm.containsKey("repeateid")) {
                if (hm.get("repeateid") != null) {
                    receipt.setRepeateInvoice((RepeatedInvoices) get(RepeatedInvoices.class, (String) hm.get("repeateid")));
                }
            }
                if (hm.containsKey("parentid")) {
                if (hm.get("parentid") != null) {
                    receipt.setParentInvoice((GoodsReceipt) get(GoodsReceipt.class, (String) hm.get("parentid")));
                }
            }      
            if (hm.containsKey("agent")) {
                if (hm.get("agent") != null && hm.get("agent") != "") {
                    receipt.setMasterAgent((MasterItem) get(MasterItem.class, (String) hm.get("agent")));
                } else {
                    receipt.setMasterAgent(null);
                }
            }
            if (hm.containsKey("exchangeRateForOpeningTransaction")) {
                if (hm.get("exchangeRateForOpeningTransaction") != null) {
                    receipt.setExchangeRateForOpeningTransaction((Double) hm.get("exchangeRateForOpeningTransaction"));
                }
            }
            if (hm.containsKey("conversionRateFromCurrencyToBase")) {
                receipt.setConversionRateFromCurrencyToBase((Boolean) hm.get("conversionRateFromCurrencyToBase"));
            }
            if (hm.containsKey("creationDate") && hm.get("creationDate") != null) {
                receipt.setCreationDate((Date) hm.get("creationDate"));
            }
            if (hm.containsKey("originalOpeningBalanceAmount") && hm.get("originalOpeningBalanceAmount") != null) {
                receipt.setOriginalOpeningBalanceAmount((Double) hm.get("originalOpeningBalanceAmount"));
            }
            if (hm.containsKey("openingBalanceAmountDue") && hm.get("openingBalanceAmountDue") != null) {
                /*
                 set status flag for opening invoices
                 */
                if (authHandler.round((Double) hm.get("openingBalanceAmountDue"), companyid) <= 0) {
                    receipt.setIsOpenPayment(false);
                } else {
                    receipt.setIsOpenPayment(true);
                }
                receipt.setOpeningBalanceAmountDue((Double) hm.get("openingBalanceAmountDue"));
            }
            if (hm.containsKey(Constants.originalOpeningBalanceBaseAmount) && hm.get(Constants.originalOpeningBalanceBaseAmount) != null) {
                receipt.setOriginalOpeningBalanceBaseAmount(authHandler.round((Double) hm.get(Constants.originalOpeningBalanceBaseAmount), companyid));
            }
            if (hm.containsKey(Constants.openingBalanceBaseAmountDue) && hm.get(Constants.openingBalanceBaseAmountDue) != null) {
                receipt.setOpeningBalanceBaseAmountDue(authHandler.round((Double) hm.get(Constants.openingBalanceBaseAmountDue), companyid));
            }
            if (hm.containsKey("partyInvoiceDate") && hm.get("partyInvoiceDate") != null) {
                receipt.setPartyInvoiceDate((Date) hm.get("partyInvoiceDate"));
            }
            if (hm.containsKey("isOpeningBalenceInvoice") && hm.get("isOpeningBalenceInvoice") != null) {
                receipt.setIsOpeningBalenceInvoice((Boolean) hm.get("isOpeningBalenceInvoice"));
            } else {
                receipt.setIsOpeningBalenceInvoice(false);
            }
            if (hm.containsKey("isNormalInvoice") && hm.get("isNormalInvoice") != null) {
                receipt.setNormalInvoice((Boolean) hm.get("isNormalInvoice"));
            } else {
                receipt.setNormalInvoice(true);
            }

            if (hm.containsKey("accountid")) {
                Account account = hm.get("accountid") == null ? null : (Account) get(Account.class, (String) hm.get("accountid"));
                receipt.setAccount(account);
            }

            if (hm.containsKey(INCASH) && hm.get(INCASH) != null) {
                receipt.setCashtransaction(Boolean.parseBoolean(hm.get(INCASH).toString()));
            }
            if (hm.containsKey("isJobWorkOutInv") && hm.get("isJobWorkOutInv") != null) {
                receipt.setIsJobWorkOutInv(Boolean.parseBoolean(hm.get("isJobWorkOutInv").toString()));
            }
            if (hm.containsKey("badDebtType") && hm.get("badDebtType") != null) {
                receipt.setBadDebtType((Integer) hm.get("badDebtType"));
            }
            if (hm.containsKey("claimedPeriod") && hm.get("claimedPeriod") != null) {
                receipt.setClaimedPeriod((Integer) hm.get("claimedPeriod"));
            }
            if (hm.containsKey("claimedDate") && hm.get("claimedDate") != null) {
                receipt.setDebtClaimedDate((Date) hm.get("claimedDate"));
            }
//            if (hm.containsKey("recoveredDate") && hm.get("recoveredDate") != null) {
//                receipt.setDebtRecoveredDate((Date) hm.get("recoveredDate"));
//            }

            if (hm.containsKey("billshipAddressid")) {
                BillingShippingAddresses bsa = hm.get("billshipAddressid") == null ? null : (BillingShippingAddresses) get(BillingShippingAddresses.class, (String) hm.get("billshipAddressid"));
                receipt.setBillingShippingAddresses(bsa);
            }

            if (hm.containsKey("landedInvoiceNumber") && hm.get("landedInvoiceNumber") != null) {
                String data=hm.get("landedInvoiceNumber").toString();
                if (!StringUtil.isNullOrEmpty(data)) {
                    List<String> grIdList = Arrays.asList(data.split(","));
                    Set<GoodsReceipt> landedInvIdSet = new HashSet<GoodsReceipt>();
                    if (!grIdList.isEmpty()) {
                        Iterator landedInvItr = grIdList.iterator();
                        while (landedInvItr.hasNext()) {
                            GoodsReceipt grObj = (GoodsReceipt) get(GoodsReceipt.class, (String) landedInvItr.next());
                            landedInvIdSet.add(grObj);
                        }
                        if (!landedInvIdSet.isEmpty()) {
                            receipt.setLandedInvoice(landedInvIdSet);
                        }
                    }
                }
            }
            
            if (hm.containsKey("gstIncluded") && hm.get("gstIncluded") != null) {
                receipt.setGstIncluded((Boolean) hm.get("gstIncluded"));
            }
//            if (hm.containsKey("moduletemplateid") && !StringUtil.isNullOrEmpty(hm.get("moduletemplateid").toString())) {
//                receipt.setModuletemplateid((ModuleTemplate) get(ModuleTemplate.class, hm.get("moduletemplateid").toString()));
//            }
            
            if (hm.containsKey("gstCurrencyRate") && hm.get("gstCurrencyRate") != null) {
                receipt.setGstCurrencyRate((Double)hm.get("gstCurrencyRate"));
            }
            if (hm.containsKey(Constants.termsincludegst) && hm.get(Constants.termsincludegst) != null) {
                receipt.setTermsincludegst((Boolean) hm.get(Constants.termsincludegst));
            }
            if(hm.containsKey(Constants.invoiceamountdue) && hm.get(Constants.invoiceamountdue)!=null) {
                /*
                 set status flag for amount due 
                 */
                if (authHandler.round((Double) hm.get(Constants.invoiceamountdue), companyid) <= 0) {
                    receipt.setIsOpenPayment(false);
                } else {
                    receipt.setIsOpenPayment(true);
                }
                receipt.setInvoiceamountdue(authHandler.round((Double) hm.get(Constants.invoiceamountdue), companyid));
            }
            receipt.setTemplateid((Projreport_Template) get(Projreport_Template.class, Constants.HEADER_IMAGE_TEMPLATE_ID));
           
            if (hm.containsKey("paydetailsid")) {
                PayDetail payDetail = StringUtil.isNullOrEmpty((String) hm.get("paydetailsid")) ? null : (PayDetail) get(PayDetail.class, (String) hm.get("paydetailsid"));
                receipt.setPayDetail(payDetail);
            }
            if (hm.containsKey(Constants.invoiceamount) && hm.get(Constants.invoiceamount) != null) { // invoice amount
                receipt.setInvoiceAmount(authHandler.round(Double.valueOf(hm.get(Constants.invoiceamount).toString()), companyid));
            }
            if (hm.containsKey(Constants.claimAmountDue) && hm.get(Constants.claimAmountDue) != null) {
                receipt.setClaimAmountDue(authHandler.round(Double.valueOf(hm.get(Constants.claimAmountDue).toString()), companyid));
            }
            if (hm.containsKey(Constants.invoiceamountinbase) && hm.get(Constants.invoiceamountinbase) != null) { // invoice amount in base
                receipt.setInvoiceAmountInBase(authHandler.round(Double.valueOf(hm.get(Constants.invoiceamountinbase).toString()), companyid));
            }
            if (hm.containsKey(Constants.invoiceamountdueinbase) && hm.get(Constants.invoiceamountdueinbase) != null) { // invoice amount due in base
                receipt.setInvoiceAmountDueInBase(authHandler.round(Double.valueOf(hm.get(Constants.invoiceamountdueinbase).toString()), companyid));
               
            }
            if (hm.containsKey(Constants.discountAmount)&& hm.get(Constants.discountAmount) != null) {  // discount amount in document currency
                receipt.setDiscountAmount(authHandler.round(Double.valueOf(hm.get(Constants.discountAmount).toString()), companyid));
            }
            if (hm.containsKey(Constants.discountAmountInBase)) { // discount amount in base
                receipt.setDiscountAmountInBase(authHandler.round(Double.valueOf(hm.get(Constants.discountAmountInBase).toString()), companyid));
            }
            if (hm.containsKey("tdsrate")) {
                receipt.setTdsRate(authHandler.round(Double.valueOf(hm.get("tdsrate").toString()), companyid));
            }
            if (hm.containsKey("tdsamount")) {
                receipt.setTdsAmount(authHandler.round(Double.valueOf(hm.get("tdsamount").toString()), companyid));
            }
            if (hm.containsKey("tdsmasterrateruleid")) {
                receipt.setTdsMasterRateRuleId((int) Integer.parseInt(hm.get("tdsmasterrateruleid").toString()));
            }
            if (hm.containsKey("isTDSApplicable") && hm.get("isTDSApplicable")!=null) {
                receipt.setIsTDSApplicable((boolean) hm.get("isTDSApplicable"));
            }
            if (hm.containsKey(Constants.SUPPLIERINVOICENO) && hm.get(Constants.SUPPLIERINVOICENO) != null) {
                receipt.setSupplierInvoiceNo((String) hm.get(Constants.SUPPLIERINVOICENO));
            }
            if (hm.containsKey("TotalAdvanceTDSAdjustmentAmt")) {
                receipt.setTotalAdvanceTDSAdjustmentAmt(authHandler.round(Double.valueOf(hm.get("TotalAdvanceTDSAdjustmentAmt").toString()), companyid));
            }
            if (hm.containsKey("landingCostCategory") && !StringUtil.isNullObject(hm.get("landingCostCategory")) && !StringUtil.isNullOrEmpty(hm.get("landingCostCategory").toString())) {
                receipt.setLandingCostCategory((LandingCostCategory) get(LandingCostCategory.class,hm.get("landingCostCategory").toString()));
            }
             if (hm.containsKey(Constants.isApplyTaxToTerms) && hm.get(Constants.isApplyTaxToTerms) != null) {  // If Save As Draft
                receipt.setApplyTaxToTerms((Boolean) hm.get(Constants.isApplyTaxToTerms));
            }
             if (hm.containsKey(Constants.isMerchantExporter) && hm.get(Constants.isMerchantExporter) != null) {  // If Save As Draft
                receipt.setIsMerchantExporter((Boolean) hm.get(Constants.isMerchantExporter));
            }
            if (hm.containsKey(Constants.importExportDeclarationNo) && hm.get(Constants.importExportDeclarationNo) != null) {
                receipt.setImportDeclarationNo((String) hm.get(Constants.importExportDeclarationNo));
            }
            if (hm.containsKey(Constants.isCreditable) && hm.get(Constants.isCreditable) != null) {
                receipt.setIsCreditable((Boolean) hm.get(Constants.isCreditable));
            }
            saveOrUpdate(receipt);
            list.add(receipt);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.addGoodsReceipt : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Goods Receipt has been added successfully", null, list, list.size());
    }
    /**
     * Update Un-Registered Purchase Invoice Debit and Credit JE details ID's in Mapping table
     * @param paramsObj
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject updateURDPurchaseInvoiceMapping(JSONObject paramsObj) throws ServiceException {
        List list = new ArrayList();
        int numRows = 0;
        if (!StringUtil.isNullOrEmpty(paramsObj.optString(ID))) {
            String delQuery = "";
            delQuery = " update purchaseinvoiceurd_jedetail set entryDetaildebit= null , entryDetailcredit= null WHERE id = ? and company=?";
            numRows = executeSQLUpdate(delQuery, new Object[]{paramsObj.optString(ID),paramsObj.optString(COMPANYID) });
        }
        return new KwlReturnObject(true, "Purchase Invoice URD JEDetail has been updated successfully", null, list, list.size());
    }
    /**
     * Save or Update Un-registered Vendor PI JE Details mapping table
     * @param paramsObj
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject saveURDPurchaseInvoiceMapping(JSONObject paramsObj) throws ServiceException {
        List list = new ArrayList();
        try {
            PurchaseInvoiceURDJEDetail URDDetails = null;
            if (StringUtil.isNullOrEmpty(paramsObj.optString(ID))) {
                URDDetails = new PurchaseInvoiceURDJEDetail();
            } else {
                URDDetails = (PurchaseInvoiceURDJEDetail) get(PurchaseInvoiceURDJEDetail.class, paramsObj.optString(ID));
            }
            if (URDDetails != null) {
                String companyid = "";
                companyid = paramsObj.optString(COMPANYID);
                Company company = (Company) get(Company.class, companyid);
                // Set Data
                URDDetails.setCompany(company);
                if (paramsObj.has(AMOUNT)) {
                    URDDetails.setInvoiceAmountInBase(paramsObj.optDouble(AMOUNT, 0));
                }
                if (paramsObj.has(BILLDATE)) {
                    URDDetails.setBilldate(paramsObj.optLong(BILLDATE));
                }
                if(paramsObj.has("goodsReceiptDetail")){
                    GoodsReceiptDetail gr = (GoodsReceiptDetail) get(GoodsReceiptDetail.class, paramsObj.optString("goodsReceiptDetail"));
                    URDDetails.setGoodsReceiptDetail(gr);
                }
                if(paramsObj.has("entryDetaildebit")){
                    JournalEntryDetail JED = (JournalEntryDetail) get(JournalEntryDetail.class, paramsObj.optString("entryDetaildebit"));
                    URDDetails.setEntryDetaildebit(JED);
                }else{
                    URDDetails.setEntryDetaildebit(null);
                }
                if(paramsObj.has("entryDetailcredit")){
                    JournalEntryDetail JED = (JournalEntryDetail) get(JournalEntryDetail.class, paramsObj.optString("entryDetailcredit"));
                    URDDetails.setEntryDetailcredit(JED);
                }else{
                    URDDetails.setEntryDetailcredit(null);
                }
                if(paramsObj.has("term")){
                    LineLevelTerms termObj = (LineLevelTerms) get(LineLevelTerms.class, paramsObj.optString("term"));
                    URDDetails.setTerm(termObj);
                }
                if(paramsObj.has("termamount")){
                    URDDetails.setTermamountInBase(paramsObj.optDouble("termamount", 0));
                }
                saveOrUpdate(URDDetails);
                list.add(URDDetails);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(" Failed to save/ Update Un-regisetred Vendor Purchase Invoice Journal Entry Detail", ex);
        }
        return new KwlReturnObject(true, "Purchase Invoice URD JEDetail has been added successfully", null, list, list.size());
    }
    /**
     * Delete data from Mapping table data for Un-Registered Vendor PI 
     * @param paramsObj
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject deleteURDVendorRCMPurchaseInvoice(JSONObject paramsObj) throws ServiceException {
        String receiptID = paramsObj.optString("receiptID");
        String companyid = paramsObj.optString(COMPANYID);
        int numRows = 0;
        String delQuery = "";
        if(!StringUtil.isNullOrEmpty(receiptID) && !StringUtil.isNullOrEmpty(companyid)){
            delQuery = "delete pijed.* from purchaseinvoiceurd_jedetail pijed inner join grdetails grd on pijed.goodsReceiptDetail = grd.id inner join goodsreceipt gr on grd.goodsreceipt= gr.id where gr.id= ? and  gr.company=?; ";
            numRows = executeSQLUpdate(delQuery, new Object[]{receiptID,companyid});
        }
        return new KwlReturnObject(true, "Goods Receipt URD JE Details has been deleted successfully.", null, null, numRows);
    }
    @Override
    public KwlReturnObject getURDVendorRCMPurchaseInvoice(JSONObject paramsObj) throws ServiceException {
        List list = new ArrayList();
        try {
            DateFormat df = (DateFormat) paramsObj.get(Constants.df);

            String sqlQuery =   " select pijed.id, pijed.entryDetaildebit,pijed.entryDetailcredit, gr.journalentry, pijed.goodsReceiptDetail, pijed.termamountInBase, lt.payableaccount, lt.account ,je.entryno, gr.grnumber " +
                                " from purchaseinvoiceurd_jedetail pijed  " +
                                " inner join grdetails grd on pijed.goodsReceiptDetail= grd.id  " +
                                " inner join goodsreceipt gr on grd.goodsreceipt= gr.id   " +
                                " inner join linelevelterms lt on pijed.term=lt.id " + 
                                " inner join journalentry je on gr.journalentry = je.id   ";
            String condtion = "" ;
            ArrayList params = new ArrayList();
            if(!StringUtil.isNullOrEmpty(paramsObj.getString("companyid"))){
                if (condtion.trim().length() > 1) {
                    condtion += " and pijed.company= ?  ";
                } else {
                    condtion += " where pijed.company= ?  ";
                }
                params.add(paramsObj.getString("companyid"));
            }
            if(!StringUtil.isNullOrEmpty(paramsObj.getString("billdate"))){
                if (condtion.trim().length() > 1) {
                    condtion += " and pijed.billdate = ?";
                } else {
                    condtion += " where pijed.billdate= ? ";
                }
            params.add(df.parse(paramsObj.getString("billdate")).getTime());
            }
            if(paramsObj.optBoolean("isInvoiceAmountOnly", false)){
                condtion += " group by gr.goodsreceipt ";
                sqlQuery = " select  sum(temp.invoiceAmountInBase) , temp.id from (select pijed.invoiceAmountInBase ,pijed.id from purchaseinvoiceurd_jedetail pijed inner join grdetails gr on pijed.goodsReceiptDetail= gr.id   " + condtion  + " ) as temp ";
            }else{
                sqlQuery += condtion ;
            }
            list = executeSQLQuery(sqlQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Failed to get Un-regisetred Vendor Purchase Invoice Journal Entry Detail ", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * Function to get product wise ITC details.
     *
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getITCGLForProducts(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        params.add(reqParams.optString("companyid"));
        StringBuilder condition = new StringBuilder();
        if (reqParams.has("productids")) {
            String productids = reqParams.optString("productids");
            if (productids.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and id in (");
                String typeArr[] = productids.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(type);
                    } else {
                        conditionBuilder.append(",?");
                        params.add(type);
                    }
                }
                conditionBuilder.append(") ");
                condition.append(conditionBuilder.toString());
            } else {
                condition.append(" and id=?");
                params.add(productids);
            }
        }
        String query = " select name from product where company=? and itctype=2 and itcaccount is NULL " + condition.toString();
        List list = executeSQLQuery(query, params.toArray());
        return list;
    }
    
    @Override
    public KwlReturnObject saveBadDebtInvoiceMapping(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        BadDebtPurchaseInvoiceMapping badDebtInvoiceMapping = new BadDebtPurchaseInvoiceMapping();
        badDebtInvoiceMapping = createBadDebtInvoiceMapping(badDebtInvoiceMapping, dataMap);
        saveOrUpdate(badDebtInvoiceMapping);
        list.add(badDebtInvoiceMapping);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public BadDebtPurchaseInvoiceMapping createBadDebtInvoiceMapping(BadDebtPurchaseInvoiceMapping badDebtInvoiceMapping, HashMap<String, Object> dataMap) {
        if (dataMap.containsKey("companyId") && dataMap.get("companyId") != null) {
            Company company = (Company) get(Company.class, (String) dataMap.get("companyId"));
            badDebtInvoiceMapping.setCompany(company);
        }
        if (dataMap.containsKey("invoiceId") && dataMap.get("invoiceId") != null) {
            GoodsReceipt goodsReceipt = (GoodsReceipt) get(GoodsReceipt.class, (String) dataMap.get("invoiceId"));
            badDebtInvoiceMapping.setGoodsReceipt(goodsReceipt);
        }
        if (dataMap.containsKey("journalEntryId") && dataMap.get("journalEntryId") != null) {
            JournalEntry journalEntry = (JournalEntry) get(JournalEntry.class, (String) dataMap.get("journalEntryId"));
            badDebtInvoiceMapping.setJournalEntry(journalEntry);
        }
        if (dataMap.containsKey("badDebtType") && dataMap.get("badDebtType") != null) {
            badDebtInvoiceMapping.setBadDebtType((Integer) dataMap.get("badDebtType"));
        }
        if (dataMap.containsKey("invoiceReceivedAmt") && dataMap.get("invoiceReceivedAmt") != null) {
            badDebtInvoiceMapping.setBadDebtAmtRecovered((Double) dataMap.get("invoiceReceivedAmt"));
        }
        if (dataMap.containsKey("gstToRecover") && dataMap.get("gstToRecover") != null) {
            badDebtInvoiceMapping.setBadAmtDebtGSTAmtRecovered((Double) dataMap.get("gstToRecover"));
        }
        if (dataMap.containsKey("badDebtAmtClaimed") && dataMap.get("badDebtAmtClaimed") != null) {
            badDebtInvoiceMapping.setBadDebtAmtClaimed((Double) dataMap.get("badDebtAmtClaimed"));
        }
        if (dataMap.containsKey("badDebtGSTAmtClaimed") && dataMap.get("badDebtGSTAmtClaimed") != null) {
            badDebtInvoiceMapping.setBadDebtGSTAmtClaimed((Double) dataMap.get("badDebtGSTAmtClaimed"));
        }
        if (dataMap.containsKey("recoveredDate") && dataMap.get("recoveredDate") != null) {
            badDebtInvoiceMapping.setBadDebtRecoveredDate((Date) dataMap.get("recoveredDate"));
        }
        if (dataMap.containsKey("claimedDate") && dataMap.get("claimedDate") != null) {
            badDebtInvoiceMapping.setBadDebtClaimedDate((Date) dataMap.get("claimedDate"));
        }
        if (dataMap.containsKey("autoGenerated") && dataMap.get("autoGenerated") != null) {
            badDebtInvoiceMapping.setAutoGenerated(true);
        }
        if (dataMap.containsKey("seqnumber") && dataMap.get("seqnumber") != null) {
            badDebtInvoiceMapping.setSeqnumber(Integer.parseInt(dataMap.get("seqnumber").toString()));
        }
        if (dataMap.containsKey(Constants.DATEPREFIX) && dataMap.get(Constants.DATEPREFIX) !=null) {
            badDebtInvoiceMapping.setDatePreffixValue((String) dataMap.get(Constants.DATEPREFIX));
        }
        if (dataMap.containsKey(Constants.DATEAFTERPREFIX) && dataMap.get(Constants.DATEAFTERPREFIX) !=null) {
            badDebtInvoiceMapping.setDateAfterPreffixValue((String) dataMap.get(Constants.DATEAFTERPREFIX));
        }
        if (dataMap.containsKey(Constants.DATESUFFIX) && dataMap.get(Constants.DATESUFFIX) !=null) {
            badDebtInvoiceMapping.setDateSuffixValue((String) dataMap.get(Constants.DATESUFFIX));
        }
        if (dataMap.containsKey("baddebtentryNumber") && dataMap.get("baddebtentryNumber") != null) {
            badDebtInvoiceMapping.setBadDebtSeqNumber((String) dataMap.get("baddebtentryNumber").toString());
        }
        if (dataMap.containsKey("paymentid") && dataMap.get("paymentid") != null) {
            badDebtInvoiceMapping.setPaymentId((String) dataMap.get("paymentid").toString());
        }
        if (dataMap.containsKey("seqformat") && dataMap.get("seqformat") != null) {
            String id = dataMap.get("seqformat").toString();
            SequenceFormat seqFormat = (SequenceFormat) get(SequenceFormat.class, id);
            badDebtInvoiceMapping.setSeqformat(seqFormat);
        }
        return badDebtInvoiceMapping;
    }

    @Override
    public KwlReturnObject updateGoodsReceipt(Map<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        String companyid = "";
        try {
            String grid = (String) hm.get(GRID);
            if (hm.containsKey("companyid")) {
                companyid = (String) hm.get("companyid");
            }
            GoodsReceipt receipt = (GoodsReceipt) get(GoodsReceipt.class, grid);
            if (receipt != null) {
                if (hm.containsKey(ENTRYNUMBER)) {
                    receipt.setGoodsReceiptNumber((String) hm.get(ENTRYNUMBER));
                }
                if (hm.containsKey(Constants.SEQFORMAT)) {
                    receipt.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) hm.get(Constants.SEQFORMAT)));
                }
                if (hm.containsKey(Constants.SEQNUMBER) && hm.get(Constants.SEQNUMBER)!=null && !StringUtil.isNullOrEmpty(hm.get(Constants.SEQNUMBER).toString())) {
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
                if (hm.containsKey(AUTOGENERATED)) {
                    receipt.setAutoGenerated((Boolean) hm.get(AUTOGENERATED));
                }
                if (hm.containsKey("isFixedAsset")) {
                    receipt.setFixedAssetInvoice((Boolean) hm.get("isFixedAsset"));
                }
                if (hm.containsKey("isCapitalGoodsAcquired")) {
                    receipt.setCapitalGoodsAcquired((Boolean) hm.get("isCapitalGoodsAcquired"));
                }
                if (hm.containsKey("isRetailPurchase") && hm.get("isRetailPurchase") != null) {
                    receipt.setRetailPurchase((Boolean) hm.get("isRetailPurchase"));
                }
                if (hm.containsKey("importService") && hm.get("importService")!= null) {
                    receipt.setImportService((Boolean) hm.get("importService"));
                }
                if (hm.containsKey("isExciseInvoice")) {
                    receipt.setIsExciseInvoice((Boolean) hm.get("isExciseInvoice"));
                }
                if (hm.containsKey("defaultnatureofpurchase")) {
                    receipt.setDefaultnatureOfPurchase((String) hm.get("defaultnatureofpurchase"));
                }
                if (hm.containsKey(MEMO)) {
                    receipt.setMemo((String) hm.get(MEMO));
                }
                if (hm.containsKey(BILLTO)) {
                    receipt.setBillFrom((String) hm.get(BILLTO));
                }
                if (hm.containsKey(SHIPADDRESS)) {
                    receipt.setShipFrom((String) hm.get(SHIPADDRESS));
                }
                if (hm.containsKey(SHIPDATE)) {
                    receipt.setShipDate((Date) hm.get(SHIPDATE));
                }
                if (hm.containsKey(DUEDATE)) {
                    receipt.setDueDate((Date) hm.get(DUEDATE));
                }
                if (hm.containsKey(termid)) {
                    //if cash purchase then don't put term
                    if(hm.containsKey(INCASH) && hm.get(INCASH) != null && !Boolean.parseBoolean(hm.get(INCASH).toString())){
                        Term term = hm.get("termid") == null ? null : (Term) get(Term.class, (String) hm.get("termid"));
                        receipt.setTermid(term);
                    }
                }
                if (hm.containsKey("shipvia")) {
                    receipt.setShipvia((String) hm.get("shipvia"));
                }
                if (hm.containsKey("fob")) {
                    receipt.setFob((String) hm.get("fob"));
                }
                if (hm.containsKey(DISCOUNTID)) {
                    Discount dsc = hm.get(DISCOUNTID) == null ? null : (Discount) get(Discount.class, (String) hm.get(DISCOUNTID));
                    receipt.setDiscount(dsc);
                }
                if (hm.containsKey(TAXID)) {
                    Tax tax = hm.get(TAXID) == null ? null : (Tax) get(Tax.class, (String) hm.get(TAXID));
                    receipt.setTax(tax);
                }
                if (hm.containsKey(VENDORENTRYID)) {
                    JournalEntryDetail vendorje = hm.get(VENDORENTRYID) == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get(VENDORENTRYID));
                    receipt.setVendorEntry(vendorje);
                }
                if (hm.containsKey(Constants.RoundingAdjustmentEntryID)) {
                    JournalEntryDetail roundingje = hm.get(Constants.RoundingAdjustmentEntryID) == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get(Constants.RoundingAdjustmentEntryID));
                    receipt.setRoundingAdjustmentEntry(roundingje);
                }
                if (hm.containsKey(Constants.IsRoundingAdjustmentApplied) && hm.get(Constants.IsRoundingAdjustmentApplied) != null) {
                    boolean isRoundingAdjustmentApplied = Boolean.parseBoolean(hm.get(Constants.IsRoundingAdjustmentApplied).toString());
                    receipt.setIsRoundingAdjustmentApplied(isRoundingAdjustmentApplied);
                }
                if (hm.containsKey(SHIPENTRYID)) {
                    JournalEntryDetail shipje = hm.get(SHIPENTRYID) == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get(SHIPENTRYID));
                    receipt.setShipEntry(shipje);
                }
                if (hm.containsKey("otherentryid")) {
                    JournalEntryDetail otherje = hm.get("otherentryid") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("otherentryid"));
                    receipt.setOtherEntry(otherje);
                }
                if (hm.containsKey(TAXENTRYID)) {
                    JournalEntryDetail taxje = hm.get(TAXENTRYID) == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get(TAXENTRYID));
                    receipt.setTaxEntry(taxje);
                }
                if (hm.containsKey(CURRENCYID)) {
                    KWLCurrency currency = hm.get(CURRENCYID) == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) hm.get(CURRENCYID));
                    receipt.setCurrency(currency);
                }
                if (hm.containsKey(COMPANYID)) {
                    Company company = hm.get(COMPANYID) == null ? null : (Company) get(Company.class, (String) hm.get(COMPANYID));
                    receipt.setCompany(company);
                }
                if (hm.containsKey("journalentryid")) {
                    JournalEntry je = hm.get("journalentryid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) hm.get("journalentryid"));
                    receipt.setJournalEntry(je);
                }
                if (hm.containsKey(ERDID)) {
                    ExchangeRateDetails erd = hm.get(ERDID) == null ? null : (ExchangeRateDetails) get(ExchangeRateDetails.class, (String) hm.get(ERDID));
                    receipt.setExchangeRateDetail(erd);
                }
                if (hm.containsKey("selfBilledInvoice")) {
                    if (hm.get("selfBilledInvoice") != null) {
                        receipt.setSelfBilledInvoice(Boolean.parseBoolean(hm.get("selfBilledInvoice").toString()));
                    }
                }
                if (hm.containsKey("RMCDApprovalNo")) {
                    if (hm.get("RMCDApprovalNo") != null) {
                        receipt.setRMCDApprovalNo((String) hm.get("RMCDApprovalNo"));
                    }
                }
                 if (hm.containsKey("openingBalanceVendorInvoiceCustomData")) {
                    OpeningBalanceVendorInvoiceCustomData openingBalanceVendorInvoiceCustomData = hm.get("openingBalanceVendorInvoiceCustomData") == null ? null : (OpeningBalanceVendorInvoiceCustomData) get(OpeningBalanceVendorInvoiceCustomData.class, (String) hm.get("openingBalanceVendorInvoiceCustomData"));
                    receipt.setOpeningBalanceVendorInvoiceCustomData(openingBalanceVendorInvoiceCustomData);
                }
                if (hm.containsKey(GRDETAILS)) {
                    receipt.setRows((Set<GoodsReceiptDetail>) hm.get(GRDETAILS));
                }
//                if (hm.containsKey("moduletemplateid") && hm.get("moduletemplateid") != null) {
//                    receipt.setModuletemplateid((ModuleTemplate) get(ModuleTemplate.class, hm.get("moduletemplateid").toString()));
//                }
                if (hm.containsKey(VENDORID)) {
                    Vendor vendor = hm.get(VENDORID) == null ? null : (Vendor) get(Vendor.class, (String) hm.get(VENDORID));
                    receipt.setVendor(vendor);
                }
                if (hm.containsKey("pendingapproval")) {
                    receipt.setPendingapproval((Integer) hm.get("pendingapproval"));
                }
                if (hm.containsKey("approvalstatuslevel")) {
                    receipt.setApprovestatuslevel((Integer)hm.get("approvalstatuslevel"));
                } else {
                    receipt.setApprovestatuslevel(11);
                }
                if (hm.containsKey("istemplate")) {
                    receipt.setIstemplate((Integer) hm.get("istemplate"));
                } else {
                    receipt.setIstemplate(0);
                }
                if (hm.containsKey("approvallevel")) {
                    receipt.setApprovallevel((Integer) hm.get("approvallevel"));
                }
                if (hm.containsKey("posttext")) {
                    receipt.setPostText((String) hm.get("posttext"));
                }
                if (hm.containsKey(ISEXPENSETYPE)) {
                    receipt.setIsExpenseType((Boolean) hm.get(ISEXPENSETYPE));
                }
                if (hm.containsKey(EXPENSEGRDETAILS)) {
                    receipt.setExpenserows((Set<ExpenseGRDetail>) hm.get(EXPENSEGRDETAILS));
                }
                if (hm.containsKey(MARKED_FAVOURITE)) {
                    if (hm.get(MARKED_FAVOURITE) != null) {
                        receipt.setFavourite(Boolean.parseBoolean(hm.get(MARKED_FAVOURITE).toString()));
                    }
                }
                 /*---Dropship PI is not eligible for further linking in PR & GR---- */
                if (hm.containsKey("isdropshipchecked") && hm.get("isdropshipchecked") != null) {  // If New GST Appliled
                    receipt.setIsDropshipDocument((Boolean) hm.get("isdropshipchecked"));
                    receipt.setIsOpenInGR(false);
                    receipt.setIsOpenInPR(false);
                }
                
                if (hm.containsKey("venbilladdress")) {
                    if (hm.get("venbilladdress") != null) {
                        receipt.setBillTo((String) hm.get("venbilladdress"));
                    }
                }
                if (hm.containsKey("venshipaddress")) {
                    if (hm.get("venshipaddress") != null) {
                        receipt.setShipTo((String) hm.get("venshipaddress"));
                    }
                }
                if (hm.containsKey(MARKED_PRINTED)) {
                    if (hm.get(MARKED_PRINTED) != null) {
                        receipt.setPrinted(Boolean.parseBoolean(hm.get(MARKED_PRINTED).toString()));
                    }
                }
                if (hm.containsKey("partyInvoiceNumber")) {
                    if (hm.get("partyInvoiceNumber") != null) {
                        receipt.setPartyInvoiceNumber((String) hm.get("partyInvoiceNumber"));
                    }
                }
                if (hm.containsKey("salesPerson")) {
                    if (hm.get("salesPerson") != null) {
                        receipt.setMasterSalesPerson((MasterItem) get(MasterItem.class, (String) hm.get("salesPerson")));
                    }
                }
                if (hm.containsKey("repeateid")) {
                    if (hm.get("repeateid") != null) {
                        receipt.setRepeateInvoice((RepeatedInvoices) get(RepeatedInvoices.class, (String) hm.get("repeateid")));
                    }
                }
                 if (hm.containsKey("parentid")) {
                    if (hm.get("parentid") != null) {
                        receipt.setParentInvoice((GoodsReceipt) get(GoodsReceipt.class, (String) hm.get("parentid")));
                    }
                }                
                if (hm.containsKey("agent") && hm.get("agent") != "") {
                    if (hm.get("agent") != null) {
                        receipt.setMasterAgent((MasterItem) get(MasterItem.class, (String) hm.get("agent")));
                    } else {
                        receipt.setMasterAgent(null);
                    }
                }
                if (hm.containsKey("exchangeRateForOpeningTransaction")) {
                    if (hm.get("exchangeRateForOpeningTransaction") != null) {
                        receipt.setExchangeRateForOpeningTransaction((Double) hm.get("exchangeRateForOpeningTransaction"));
                    }
                }
                if (hm.containsKey("conversionRateFromCurrencyToBase")) {
                    receipt.setConversionRateFromCurrencyToBase((Boolean) hm.get("conversionRateFromCurrencyToBase"));
                }
                if (hm.containsKey("creationDate") && hm.get("creationDate") != null) {
                    receipt.setCreationDate((Date) hm.get("creationDate"));
                }
                if (hm.containsKey("originalOpeningBalanceAmount") && hm.get("originalOpeningBalanceAmount") != null) {
                    receipt.setOriginalOpeningBalanceAmount((Double) hm.get("originalOpeningBalanceAmount"));
                }
                if (hm.containsKey("openingBalanceAmountDue") && hm.get("openingBalanceAmountDue") != null) {
                    /*
                     set status flag for opening invoices
                     */
                    if (authHandler.round((Double) hm.get("openingBalanceAmountDue"), companyid) <= 0) {
                        receipt.setIsOpenPayment(false);
                    } else {
                        receipt.setIsOpenPayment(true);
                    }
                    receipt.setOpeningBalanceAmountDue(authHandler.round((Double) hm.get("openingBalanceAmountDue"), companyid));
                }
                if (hm.containsKey(Constants.originalOpeningBalanceBaseAmount) && hm.get(Constants.originalOpeningBalanceBaseAmount) != null) {
                    receipt.setOriginalOpeningBalanceBaseAmount(authHandler.round((Double) hm.get(Constants.originalOpeningBalanceBaseAmount),companyid));
                }
                if (hm.containsKey(Constants.openingBalanceBaseAmountDue) && hm.get(Constants.openingBalanceBaseAmountDue) != null) {
                    receipt.setOpeningBalanceBaseAmountDue(authHandler.round((Double) hm.get(Constants.openingBalanceBaseAmountDue), companyid));
                }
                if (hm.containsKey("partyInvoiceDate") && hm.get("partyInvoiceDate") != null) {
                    receipt.setPartyInvoiceDate((Date) hm.get("partyInvoiceDate"));
                }
                if (hm.containsKey("isNormalInvoice") && hm.get("isNormalInvoice") != null) {
                    receipt.setNormalInvoice((Boolean) hm.get("isNormalInvoice"));
                }
                if(hm.containsKey(Constants.invoiceamountdue) && hm.get(Constants.invoiceamountdue)!=null) {
                    if (receipt.isNormalInvoice()) {
                        /*
                         set status flag for amount due 
                         */
                        if (authHandler.round((Double) hm.get(Constants.invoiceamountdue), companyid) <= 0) {
                            receipt.setIsOpenPayment(false);
                        } else {
                            receipt.setIsOpenPayment(true);
                        }
                    }
                    receipt.setInvoiceamountdue(authHandler.round((Double) hm.get(Constants.invoiceamountdue), companyid));
                }
                if(hm.containsKey(Constants.claimAmountDue) && hm.get(Constants.claimAmountDue)!=null) {
                    receipt.setClaimAmountDue(authHandler.round((Double) hm.get(Constants.claimAmountDue),companyid));
                }
                if (hm.containsKey("accountid")) {
                    Account account = hm.get("accountid") == null ? null : (Account) get(Account.class, (String) hm.get("accountid"));
                    receipt.setAccount(account);
                }

                if (hm.containsKey("billshipAddressid")) {
                    BillingShippingAddresses bsa = hm.get("billshipAddressid") == null ? null : (BillingShippingAddresses) get(BillingShippingAddresses.class, (String) hm.get("billshipAddressid"));
                    receipt.setBillingShippingAddresses(bsa);
                }

                if (hm.containsKey("gstCurrencyRate") && hm.get("gstCurrencyRate") != null) {
                    receipt.setGstCurrencyRate((Double) hm.get("gstCurrencyRate"));
                }
                    
                if (hm.containsKey("landedInvoiceNumber") && hm.get("landedInvoiceNumber") != null) {
                    String data = hm.get("landedInvoiceNumber").toString();
                    if (!StringUtil.isNullOrEmpty(data)) {
                        List<String> grIdList = Arrays.asList(data.split(","));
                        Set<GoodsReceipt> landedInvIdSet = new HashSet<GoodsReceipt>();
                        if (!grIdList.isEmpty()) {
                            Iterator landedInvItr = grIdList.iterator();
                            while (landedInvItr.hasNext()) {
                                GoodsReceipt grObj = (GoodsReceipt) get(GoodsReceipt.class, (String) landedInvItr.next());
                                landedInvIdSet.add(grObj);
                            }
                            if (!landedInvIdSet.isEmpty()) {
                                receipt.setLandedInvoice(landedInvIdSet);
                            }
                        }
                    }
                }

                if (hm.containsKey("gstIncluded") && hm.get("gstIncluded") != null) {
                    receipt.setGstIncluded((Boolean) hm.get("gstIncluded"));
                }
                if (hm.containsKey(Constants.termsincludegst) && hm.get(Constants.termsincludegst) != null) {
                    receipt.setTermsincludegst((Boolean) hm.get(Constants.termsincludegst));
                }
                if (hm.containsKey("paydetailsid")) {
                    PayDetail payDetail = StringUtil.isNullOrEmpty((String) hm.get("paydetailsid")) ? null : (PayDetail) get(PayDetail.class, (String) hm.get("paydetailsid"));
                    receipt.setPayDetail(payDetail);
                }
                if (hm.containsKey(Constants.invoiceamount) && hm.get(Constants.invoiceamount) != null) { // invoice amount
                    receipt.setInvoiceAmount(authHandler.round(Double.valueOf(hm.get(Constants.invoiceamount).toString()), companyid));
                }
                if (hm.containsKey(Constants.invoiceamountinbase) && hm.get(Constants.invoiceamountinbase) != null) { // invoice amount in base
                    receipt.setInvoiceAmountInBase(authHandler.round(Double.valueOf(hm.get(Constants.invoiceamountinbase).toString()), companyid));
                }
                if (hm.containsKey(Constants.invoiceamountdueinbase) && hm.get(Constants.invoiceamountdueinbase) != null) { // invoice amount due in base
                    receipt.setInvoiceAmountDueInBase(authHandler.round(Double.valueOf(hm.get(Constants.invoiceamountdueinbase).toString()), companyid));
                
                }
                if (hm.containsKey(Constants.discountAmount) && hm.get(Constants.discountAmount) != null) {  // discount amount in document currency
                    receipt.setDiscountAmount(authHandler.round(Double.valueOf(hm.get(Constants.discountAmount).toString()), companyid));
                }
                if (hm.containsKey(Constants.discountAmountInBase)) { // discount amount in base
                    receipt.setDiscountAmountInBase(authHandler.round(Double.valueOf(hm.get(Constants.discountAmountInBase).toString()), companyid));
                }
                if (hm.containsKey("amountduedate") && hm.get("amountduedate") != null) {
                    if (StringUtil.isNullOrEmpty((String) hm.get("amountduedate"))) {
                        receipt.setAmountDueDate(null);
                    } else {
                        receipt.setAmountDueDate((Date) hm.get("amountduedate"));
                    }
                }
                if (hm.containsKey("FormSeriesNo") && hm.get("FormSeriesNo") != null) {
                    receipt.setFormseriesno((String) hm.get("FormSeriesNo"));
                }
                if (hm.containsKey("FormNo") && hm.get("FormNo") != null) {
                    receipt.setFormno((String) hm.get("FormNo"));
                }
                if (hm.containsKey("FormDate") && hm.get("FormDate") != null) {
                    receipt.setFormdate((Date) hm.get("FormDate"));
                }
                if (hm.containsKey("FormAmount") && hm.get("FormAmount") != null) {
                    receipt.setFormamount((Double) hm.get("FormAmount"));
                }
                if (hm.containsKey("FormStatus") && hm.get("FormStatus") != null) {
                    receipt.setFormstatus((String)hm.get("FormStatus"));
                }
                if (hm.containsKey("tdsrate")) {
                    receipt.setTdsRate(authHandler.round(Double.valueOf(hm.get("tdsrate").toString()), companyid));
                }
                if (hm.containsKey("tdsamount")) {
                    receipt.setTdsAmount(authHandler.round(Double.valueOf(hm.get("tdsamount").toString()), companyid));
                }
                if (hm.containsKey("taxAmount")) {
                    receipt.setTaxamount((Double) hm.get("taxAmount"));
                }
                if (hm.containsKey("taxAmountInBase")) {
                receipt.setTaxamountinbase((Double) hm.get("taxAmountInBase"));
                }
                if (hm.containsKey("excludingGstAmount")) {
                receipt.setExcludingGstAmount((Double) hm.get("excludingGstAmount"));
                }
                if (hm.containsKey("excludingGstAmountInBase")) {
                receipt.setExcludingGstAmountInBase((Double) hm.get("excludingGstAmountInBase"));
                }
                if (hm.containsKey("landingCostCategory") && !StringUtil.isNullObject(hm.get("landingCostCategory"))) {
                    receipt.setLandingCostCategory((LandingCostCategory) get(LandingCostCategory.class, hm.get("landingCostCategory").toString()));
                }
                saveOrUpdate(receipt);
            }
            list.add(receipt);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.updateGoodsReceipt : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Goods Receipt has been updated successfully", null, list, list.size());
    }
    /* Function to save the date on which amount due is set to 0*/

    @Override
    public boolean saveGoodsReceiptAmountDueZeroDate(GoodsReceipt goodsReceipt, HashMap<String, Object> dataMap) {
        boolean success = true;
        try {
            DateFormat dateOnlyFormatter = authHandler.getDateOnlyFormat();
            Date maxLinkDate = null;
            Date amountduedate = null;
            if (dataMap.containsKey("amountduedate")) {
                amountduedate = (Date) dataMap.get("amountduedate");
            }
            ArrayList params = new ArrayList();
            params.add(goodsReceipt.getID());
            params.add(goodsReceipt.getID());
            params.add(goodsReceipt.getID());
            String selectQuery = "select max(resulttable.linkeddate) "
                    + "from "
                    + "( "
                    + "select grlinkdate as linkeddate from dndetails  "
                    + "where goodsreceipt=? and (grlinkdate IS NOT NULL AND grlinkdate!='1970-01-01') "
                    + "union "
                    + "select paymentlinkdate as linkeddate from linkdetailpayment  "
                    + "where goodsReceipt=? and (paymentlinkdate IS NOT NULL AND paymentlinkdate!='1970-01-01') "
                    + "union "
                    + "select je.entrydate as linkeddate from paymentdetail  "
                    + "inner join payment mp on mp.id=paymentdetail.payment inner join journalentry as je on je.id=mp.journalentry "
                    + "where  paymentdetail.goodsReceipt=? "
                    + ") as resulttable ";
            List list = executeSQLQuery(selectQuery, params.toArray());
            if (!list.isEmpty() &&  list.get(0) != null) {
                Iterator iterator = list.iterator();
                if (iterator.hasNext()) {
                    try {
                        maxLinkDate = (Date) iterator.next();
                        maxLinkDate = dateOnlyFormatter.parse(dateOnlyFormatter.format(maxLinkDate));//Removing time part
                    } catch (ClassCastException | ParseException ex) {
                        maxLinkDate = null;
                    }
                }
            }
            if (maxLinkDate != null && amountduedate != null) {// when both are not null
                if (maxLinkDate.after(amountduedate)) {
                    amountduedate = maxLinkDate;
                }
            } else if (maxLinkDate != null && amountduedate == null) {// when max date is not null and amountdue date is null
                amountduedate = maxLinkDate;
            }

            if (amountduedate != null) {
                goodsReceipt.setAmountDueDate(amountduedate);
            }
            saveOrUpdate(goodsReceipt);
        } catch (Exception ex) {
            System.out.println("saveInvoiceAmountDueZeroDate: " + ex.getMessage());
            success = false;
        }
        return success;
    }
    /*Function to get goods receipt having invoice amount */
    public KwlReturnObject getGoodsReceiptsHavingInvoiceAmount(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        String hqlQuery = "from GoodsReceipt gr where gr.company.companyID=? and gr.invoiceAmount!=? and gr.isOpeningBalenceInvoice=? and gr.normalInvoice=?";
        params.add(0.0);
        params.add(false);
        params.add(true);
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public boolean updateInvoiceAmountInBase(GoodsReceipt goodsreceipt, JSONObject json) throws ServiceException {
        boolean success = true;
        try {
            String companyid = json.optString("companyid");
            if (goodsreceipt != null) {
                if (json.has(Constants.invoiceamountinbase)) { // invoice amount in base
                    goodsreceipt.setInvoiceAmountInBase(authHandler.round(json.optDouble(Constants.invoiceamountinbase, 0.0), companyid));
                }
                if (json.has(Constants.invoiceamountdueinbase)) { // invoice amount due in base
                    goodsreceipt.setInvoiceAmountDueInBase(authHandler.round(json.optDouble(Constants.invoiceamountdueinbase,0.0), companyid));
                    
                }
                if (json.has(Constants.discountAmount)) {  // discount amount in document currency
                    goodsreceipt.setDiscountAmount(authHandler.round(json.optDouble(Constants.discountAmount,0.0), companyid));
                }
                if (json.has(Constants.discountAmountInBase)) { // discount amount in base
                    goodsreceipt.setDiscountAmountInBase(authHandler.round(json.optDouble(Constants.discountAmountInBase,0.0), companyid));
                }
                saveOrUpdate(goodsreceipt);
            }

        } catch (Exception ex) {
            success = false;
            System.out.println("accGoodsReceiptImpl:updateInvoiceAmountInBase "+ex.getMessage());
        }
        return success;
    }
    @Override
    public KwlReturnObject getGoodsReceipts(Map<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        try {
             boolean calendar=false;
            if(request.get("calendar")!=null){
                calendar=(Boolean)request.get("calendar");
            }
            String companyid = (String) request.get(COMPANYID);
            DateFormat df = (DateFormat) request.get(Constants.df);
            boolean getRecordBasedOnJEDate = (request.containsKey("getRecordBasedOnJEDate") && request.get("getRecordBasedOnJEDate") != null) ? Boolean.parseBoolean(request.get("getRecordBasedOnJEDate").toString()) : false;
            CompanyAccountPreferences pref = (CompanyAccountPreferences) get(CompanyAccountPreferences.class, companyid);
//            KWLCurrency currency = (KWLCurrency) get(KWLCurrency.class, currencyid);
            Calendar startFinYearCal = Calendar.getInstance();
            Calendar endFinYearCal = Calendar.getInstance();
            startFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.setTime(pref.getFinancialYearFrom());

            if (request.get("year") != null) {		// Check for the selected year in the year combo for charts			Neeraj
                int year = Integer.parseInt(request.get("year").toString());
                startFinYearCal.set(Calendar.YEAR, year);
                endFinYearCal.set(Calendar.YEAR, year);
            }

            endFinYearCal.add(Calendar.YEAR, 1);
            String vendorid = (String) request.get(VENDORID);
            String customerid = (vendorid == null ? (String) request.get("accid") : vendorid);
            String ss = (String) request.get("ss");
            String cashAccount = pref.getCashAccount().getID();
            boolean cashonly = false;
            boolean creditonly = false;
            boolean personGroup = false;
            boolean isagedgraph = false;
            boolean isexpenseinv = false;
            boolean only1099Vend = false;
            boolean for1099Report = false;
            boolean isFixedAsset = false;
            boolean isConsignment = false;
            boolean onlyMRPJOBWORKIN = false;
            boolean includeFixedAssetInvoicesFlag = false;
            String group = "";
            boolean isLifoFifo = false;
            String upperLimitDate=(String) request.get("upperLimitDate");
            boolean filterForClaimedDateForPayment =false;
            String invoiceIdToSkip = "";
            if(request.containsKey("invoiceIdToSkip") && request.get("invoiceIdToSkip")!=null){
                invoiceIdToSkip=(String)request.get("invoiceIdToSkip");
            }   
            if (request.containsKey("isLifoFifo") && request.get("isLifoFifo") != null && request.get("isLifoFifo") != "") {
                isLifoFifo = Boolean.parseBoolean((String) request.get("isLifoFifo"));
            }
            boolean isCapitalGoodsAcquired = false;
            if (request.containsKey("isCapitalGoodsAcquired") && request.get("isCapitalGoodsAcquired") != null) {
                isCapitalGoodsAcquired = (Boolean) request.get("isCapitalGoodsAcquired");
            }
            boolean isRetailPurchase = false;
            if (request.containsKey("isRetailPurchase") && request.get("isRetailPurchase") != null) {
                isRetailPurchase = (Boolean) request.get("isRetailPurchase");
            }
            if (request.get("isFixedAsset") != null) {
                isFixedAsset = (Boolean) request.get("isFixedAsset");
            }
            if (request.containsKey("isConsignment") && request.get("isConsignment") != null) {
                isConsignment = (Boolean) request.get("isConsignment");
            }
            if (request.containsKey("onlyMRPJOBWORKIN") && request.get("onlyMRPJOBWORKIN") != null) {
                onlyMRPJOBWORKIN = (Boolean) request.get("onlyMRPJOBWORKIN");
            }
            if (request.get("includeFixedAssetInvoicesFlag") != null) {
                includeFixedAssetInvoicesFlag = (Boolean) request.get("includeFixedAssetInvoicesFlag");
            }
            if(request.get("filterForClaimedDateForPayment")!=null){
                filterForClaimedDateForPayment = Boolean.parseBoolean(request.get("filterForClaimedDateForPayment").toString());
            }
            
            boolean CashAndInvoice = Boolean.FALSE.parseBoolean(String.valueOf(request.get("CashAndInvoice")));
            cashonly = Boolean.parseBoolean((String) request.get("cashonly"));
            creditonly = Boolean.parseBoolean((String) request.get("creditonly"));
            only1099Vend = Boolean.parseBoolean((String) request.get("only1099Vend"));
            String billID = (String) request.get("billid");
            String billIDs = (String) request.get("billids");
            String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
            String expenseinv = (String) request.get("onlyexpenseinv");
            for1099Report = Boolean.parseBoolean((String) request.get("for1099Report"));
            boolean deleted = Boolean.parseBoolean((String) request.get(DELETED));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));

//            if (cashonly) {
//                customerid = cashAccount;
//            }
//            boolean ignoreZero = request.get("ignorezero") != null;
            String dueDate = (String) request.get("curdate");
            personGroup = Boolean.parseBoolean((String) request.get("persongroup"));
            isagedgraph = Boolean.parseBoolean((String) request.get("isagedgraph"));
            ArrayList params = new ArrayList();
            String condition = "";
            String venCondition = "";
            params.add(companyid);

            if (request.containsKey("upperLimitDate") && request.get("upperLimitDate") != null && request.get("upperLimitDate") != "") {
//                condition += " and gr.journalEntry.entryDate <= ?";
                if (getRecordBasedOnJEDate) {
                    condition += " and gr.journalEntry.entryDate <= ?";
                } else {
                    condition += " and gr.creationDate <= ?";
                }
                params.add(df.parse(upperLimitDate));
            }
            
            if(filterForClaimedDateForPayment && request.containsKey("upperLimitDate") && request.get("upperLimitDate") != null && request.get("upperLimitDate") != ""){
                condition += " and (gr.debtClaimedDate is NULL or gr.debtClaimedDate <= ?) ";
                params.add(df.parse(upperLimitDate));
            }
            if (request.containsKey("gtaapplicable") && request.get("gtaapplicable") != null) {
                condition += " and gr.gtaapplicable = ? ";
                params.add(Boolean.parseBoolean(request.get("gtaapplicable").toString()));
            }
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and gr.journalEntry.ID IN(" + jeIds + ")";
            }

            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans) && !request.containsKey("isReceipt")) {
                condition += " and gr.currency.currencyID = ?";
                params.add(currencyfilterfortrans);
            }

            if (!StringUtil.isNullOrEmpty(expenseinv)) {
                isexpenseinv = Boolean.parseBoolean(expenseinv);
                params.add(isexpenseinv);
                condition += " and gr.isExpenseType=?";

            }
            if (!includeFixedAssetInvoicesFlag) {
                if (isFixedAsset) {
                    condition += " and gr.fixedAssetInvoice=true ";
                } else {
                    condition += " and gr.fixedAssetInvoice=false ";
                }
            }
            /*
             *  'isReceipt' is a flag sent when invoices are fetched for 'Make Payment'.
             *   So invoices will not be checked whether they are consignment invoices or normal one. All invoices will be fetched
             */
            if (!request.containsKey("isReceipt")) {
                if (isConsignment) {
                    condition += " and gr.isconsignment='T'";
                } else {
                    condition += " and gr.isconsignment='F'";
                }
            }
            if (!request.containsKey("isReceipt")) {
                if (onlyMRPJOBWORKIN) {
                    condition += " and gr.isMRPJobWorkIN='T'";
                } else {
                    condition += " and gr.isMRPJobWorkIN='F'";
                }
            }
            if (!StringUtil.isNullOrEmpty(dueDate)) {
                if (for1099Report) {
                    params.add(df.parse(dueDate));
//                    condition += " and gr.journalEntry.entryDate<=?";
                    if (getRecordBasedOnJEDate) {
                        condition += " and gr.journalEntry.entryDate<=?";
                    } else {
                        condition += " and gr.creationDate<=?";
                    }
                } else {
                    params.add(df.parse(dueDate));
                    condition += " and gr.dueDate<=?";
                }
            }
            if(!StringUtil.isNullOrEmpty(billIDs)){
                String[] bills = billIDs.split(",");
                StringBuilder billQuery = new StringBuilder();
                for (String bill : bills) {
                    billQuery.append("'").append(bill).append("',");
                }
                condition += " and gr.ID in ("+ billQuery.substring(0, billQuery.lastIndexOf(","))+")";
            }
            else{
            if (!StringUtil.isNullOrEmpty(billID)) {
                params.add(billID);
                condition += " and gr.ID=?";
            } else {
                if (!StringUtil.isNullOrEmpty(customerid)) {
                    params.add(customerid);
                    condition += " and gr.vendor.ID=?";
//                     if(!CashAndInvoice) {
//                            String qMarks = "";
//                            if (!cashonly) {
//                                qMarks = "?,";
//                                params.add(cashAccount);
//                            }
//                            qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
//                            if (!StringUtil.isNullOrEmpty(qMarks)) {
//                            if(!cashonly){
//                                condition += " and gr.vendorEntry.account.ID not in (" + qMarks + ")"; 
//                            } 
//                            }
//                     }

                } else {
//                    if(!CashAndInvoice) {
//                        String qMarks = "";
//                        if (!creditonly) {
//                            qMarks += "?,";
//                            params.add(cashAccount);
//                        }//else{
//                     if (only1099Vend){     //remove this condition in case of viewing all vendors in 1099 [PS]
//                            venCondition += " and taxEligible=true"; //gr.vendorEntry.account.ID in(select v.ID from Vendor v v.taxEligible=true and v.ID=gr.vendorEntry.account.ID";
//                        }
//                      qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
//                            if (!StringUtil.isNullOrEmpty(qMarks)) {
//                                condition += " and gr.vendorEntry.account.ID not in (" + qMarks + ")"; 
//                            }
//                            
//                      params.add(true);
//                      condition += " or gr.cashtransaction=?";
                }
//                }
            }
            }
            if (!CashAndInvoice) {
                if (cashonly) {
                    params.add(true);
                    condition += " and gr.cashtransaction=?";
                } else {
                    params.add(false);
                    condition += " and gr.cashtransaction=?";
                }
            }
            String costCenterId = (String) request.get("costCenterId");
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and gr.journalEntry.costcenter.ID=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
//                condition += " and (gr.journalEntry.entryDate >=? and gr.journalEntry.entryDate <=?)";
                if (getRecordBasedOnJEDate) {
                    condition += " and (gr.journalEntry.entryDate >=? and gr.journalEntry.entryDate <=?)";
                } else {
                    condition += " and (gr.creationDate >=? and gr.creationDate <=?)";
                }
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            } else if (!StringUtil.isNullOrEmpty(endDate)) {  //condition for account reevaluation up to selected date
//                condition += " and gr.journalEntry.entryDate <=? ";
                if (getRecordBasedOnJEDate) {
                    condition += " and gr.journalEntry.entryDate <=? ";
                } else {
                    condition += " and gr.creationDate <=? ";
                }
                params.add(df.parse(endDate));
            }
            if (for1099Report) {
                if (StringUtil.isNullOrEmpty(ss) == false) {
                    params.add(ss + "%");
                    condition += " and gr.vendorEntry.account.name like ? ";
                }
            } else {
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"gr.goodsReceiptNumber","gr.billFrom","gr.journalEntry.entryNumber", "gr.memo", "gr.vendorEntry.account.name","gr.supplierInvoiceNo"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 6);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    condition += searchQuery;
                }
            }
            
            Date startFinYearCalDate = null;
            Date endFinYearCalDate = null;

            String startFinYearCalString = authHandler.getDateOnlyFormat().format(startFinYearCal.getTime());
            startFinYearCalDate = authHandler.getDateOnlyFormat().parse(startFinYearCalString);

            String endFinYearCalString = authHandler.getDateOnlyFormat().format(endFinYearCal.getTime());
            endFinYearCalDate = authHandler.getDateOnlyFormat().parse(endFinYearCalString);

           
            if (personGroup) {
                params.add(startFinYearCalDate);
                params.add(endFinYearCalDate);
                condition += " and gr.dueDate>=? and gr.dueDate<=?";
            }
            
             if (calendar) {
                String calstartdt = "", calenddt = "";
                if (request.get("calstartdt") != null) {
                    calstartdt = (String) request.get("calstartdt");
                }
                if (request.get("calenddt") != null) {
                    calenddt = (String) request.get("calenddt");
                }

                SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                Date dt11 = (Date) df1.parse(calstartdt);
                Date dt12 = (Date) df1.parse(calenddt);
                params.add(df.parse(df.format(dt11)));
                params.add(df.parse(df.format(dt12)));

                condition += " and gr.dueDate>=? and gr.dueDate<=?";
            }
            if (isagedgraph) {
                params.add(startFinYearCalDate);
                params.add(endFinYearCalDate);
                condition += " and gr.dueDate>=? and gr.dueDate<=?";
            }
            if (isCapitalGoodsAcquired) {
                condition += " and gr.capitalGoodsAcquired=? ";
                params.add(isCapitalGoodsAcquired);
            }
            if (isRetailPurchase) {
                condition += " and gr.retailPurchase=? ";
                params.add(isRetailPurchase);
            }
            if (nondeleted) {
                condition += " and gr.deleted=false ";
            } else if (deleted) {
                condition += " and gr.deleted=true ";
            }
             if(!StringUtil.isNullOrEmpty(invoiceIdToSkip)){
                condition+= " and gr.ID not in ( "+invoiceIdToSkip+ " ) ";
            }
            condition += " and gr.pendingapproval = 0 and gr.istemplate != 2 ";
            if(Constants.InvoiceAmountDueFlag && request.containsKey("onlyamountdue") && request.get("onlyamountdue")!=null && Boolean.parseBoolean(request.get("onlyamountdue").toString())) {
               /*
                * onlyclaimedamountdue will be true for fetching the invoices for payment.
                * It will return those invoices which are claimed but not recovered completely.
                */
                if(request.containsKey("onlyclaimedamountdue") && request.get("onlyclaimedamountdue")!=null && Boolean.parseBoolean(request.get("onlyclaimedamountdue").toString())){
                    condition += " and ( gr.invoiceamountdue != 0 or (gr.claimAmountDue != 0 and (gr.badDebtType = 1 or gr.badDebtType = 2))) ";
                } else {
                    condition += " and gr.invoiceamountdue != 0 ";
                }    
            }
            if(Constants.InvoiceAmountDueFlag && request.containsKey("ignorezero") && request.get("ignorezero")!=null && request.get("ignorezero").toString().equals("false")) {
                condition += " and gr.invoiceamountdue == 0 ";
            }
            if(Constants.InvoiceAmountDueFlag && request.containsKey("minimumAmountDue") && request.get("minimumAmountDue")!=null ) {
                double minimummountDue = Double.parseDouble(request.get("minimumAmountDue").toString());
                condition += " and gr.invoiceamountdue >= "+minimummountDue+" ";
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
            String joinquery="from GoodsReceipt gr";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = request.get("searchJson").toString();

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    reqParams1.put(Constants.Searchjson, Searchjson);
                    reqParams1.put(Constants.appendCase, appendCase);
                    reqParams1.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(reqParams1, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "gr.journalEntry.accBillInvCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jedc");//  
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jedprdc"); 
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    if (mySearchFilterString.contains("jedc") || mySearchFilterString.contains("jedprdc")) {
                        joinString = " inner join gr.journalEntry je inner join je.details jed ";
                        if (mySearchFilterString.contains("jedc")) {
                            joinString += " inner join jed.accJEDetailCustomData jedc ";
                        }
                        if (mySearchFilterString.contains("jedprdc")) {
                            joinString += " inner join jed.accJEDetailsProductCustomData jedprdc ";
                        }
                    }
                }
                /*
                 * fixedAssetsPurchaseInvoiceSearchJson passed to fetch FixedAssets_PurchaseInvoice
                 * called from accReportsController.java-->>>getCapitalGoodsAcquired
                 */
                if (request.containsKey(Constants.fixedAssetsPurchaseInvoiceSearchJson) && !StringUtil.isNullOrEmpty((String) request.get(Constants.fixedAssetsPurchaseInvoiceSearchJson))) {
                    String mySearchFilterString1 = "";
                    request.put(Constants.Acc_Search_Json, request.get(Constants.fixedAssetsPurchaseInvoiceSearchJson));
                    request.put(Constants.moduleid, Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId);
                    mySearchFilterString1 = StringUtil.getMySearchFilterString(request, params);
                    if (mySearchFilterString1.contains("c.accjecustomdata")) {
                        mySearchFilterString1 = mySearchFilterString1.replaceAll("c.accjecustomdata", "gr.journalEntry.accBillInvCustomData");
                    }
                    mySearchFilterString = StringUtil.combineTwoCustomSearchStrings(mySearchFilterString, mySearchFilterString1);
                }
            }
            
            if (request.containsKey("salesPurchaseReturnflag") && request.get("salesPurchaseReturnflag").equals(true)) {
                condition += " and gr.isOpenInPR=true ";
            }
            if (request.containsKey("doflag") && request.get("doflag").equals(true)) {
                condition += " and gr.isOpenInGR=true ";
            }
            String orderSubQuery = " order by gr.vendorEntry.account.ID, gr.goodsReceiptNumber asc";
            if (request.containsKey("direction") && request.get("direction") != null && isLifoFifo) {
//                orderSubQuery = " order by gr.journalEntry.entryDate " + request.get("direction").toString() + ", gr.goodsReceiptNumber " + request.get("direction").toString();
                if (getRecordBasedOnJEDate) {
                    orderSubQuery = " order by gr.journalEntry.entryDate " + request.get("direction").toString() + ", gr.goodsReceiptNumber " + request.get("direction").toString();
                } else {
                    orderSubQuery = " order by gr.creationDate " + request.get("direction").toString() + ", gr.goodsReceiptNumber " + request.get("direction").toString();
                }
            }

            String query = joinquery+joinString+" where gr.company.companyID=? " + condition + mySearchFilterString + group + orderSubQuery;//" order by gr.vendorEntry.account.ID, gr.goodsReceiptNumber asc";
            if (request.containsKey("getJEIDAndGR") && request.get("getJEIDAndGR") != null) {
                String executeQuery = "select gr.journalEntry.ID , gr " + query;
                list = executeQuery( executeQuery, params.toArray());
                request.remove("getJEIDAndGR");
            } else {
                list = executeQuery( query, params.toArray());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getGoodsReceipts : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getGoodsReceiptIDForDiamondAviation(String poid,String company) throws ServiceException{
        
       List list = new ArrayList();
        int count = 0;
        try {

            ArrayList params = new ArrayList();
            if (!StringUtil.isNullOrEmpty(poid)){
                params.add(poid);
                params.add(company);
                String query = "select grorder from grodetails where podetails= ? and company = ?";
                list = executeSQLQuery( query, params.toArray());
                count = list.size();
            }

        } catch (Exception ex) {
           throw ServiceException.FAILURE("getGoodsReceiptIDForDiamondAviation : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
        
        
    }
    
    public KwlReturnObject getGoodsReceiptOrderIDFromVI(String videtails,String company) throws ServiceException{
        List list = new ArrayList();
        int count = 0;
        try {

            ArrayList params = new ArrayList();
            if (!StringUtil.isNullOrEmpty(videtails)){
                params.add(videtails);
                params.add(company);
//                String query = "select distinct(grorder) from  grodetails where videtails in( select id from grdetails where goodsreceipt = ? and company = ?)";
                String query = "select distinct(grodetails.grorder) from grodetails inner join grdetails on grodetails.videtails=grdetails.id where grdetails.goodsreceipt = ? and grdetails.company = ?";
                list = executeSQLQuery( query, params.toArray());
                count = list.size();
            }

        } catch (Exception ex) {
           throw ServiceException.FAILURE("getGoodsReceiptIDFromVI : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public Map<String, JournalEntry> getGRInvoiceJEList(List<String> invoiceIDLIST) throws ServiceException {
        Map<String, JournalEntry> invoiceMap = new HashMap<String, JournalEntry>();
        if (invoiceIDLIST != null && !invoiceIDLIST.isEmpty()) {
                String query = "select  gr.ID, gr.journalEntry "
                        + " from GoodsReceipt gr "
                        + " where gr.ID in (:invoiceIDList)";
                List<List> values = new ArrayList<List>();
                values.add(invoiceIDLIST);
                List<Object[]> results = executeCollectionQuery( query, Collections.singletonList("invoiceIDList"), values);

                if (results != null) {
                    for (Object[] result : results) {
                        String invID = (String) result[0];
                        invoiceMap.put(invID, (JournalEntry) result[1]);
                    }
                }
            }
        return invoiceMap;
    }

    public String[] columSort(String Col_Name, String Col_Dir) throws ServiceException {
        String[] String_Sort = new String[5];
        if (Col_Name.equals("personname")) {
            String_Sort[0] = " order by vendor.name " + Col_Dir;
            String_Sort[3] = "";
            String_Sort[4] = "";
            String_Sort[1] = ", vendor.name ";
            String_Sort[2] = ", vendor.name ";
        } else if (Col_Name.equals("billno")) {
            String_Sort[0] = " order by grnumber " + Col_Dir;
            String_Sort[1] = ",goodsreceipt.grnumber ";
            String_Sort[2] = ", billinggr.billinggrnumber ";
            String_Sort[3] = "";
            String_Sort[4] = "";
        } else if (Col_Name.equals("entryno")) {
            String_Sort[0] = " order by entryno " + Col_Dir;
            String_Sort[1] = ", journalentry.entryno ";
            String_Sort[2] = ", journalentry.entryno ";
            String_Sort[3] = "";
            String_Sort[4] = "";
        } else if (Col_Name.equals("date")) {
            String_Sort[0] = " order by entrydate " + Col_Dir;
            String_Sort[1] = ", journalentry.entrydate ";
            String_Sort[2] = ", journalentry.entrydate ";
            String_Sort[3] = "";
            String_Sort[4] = "";
        } else if (Col_Name.equals("duedate")) {
            String_Sort[0] = " order by duedate " + Col_Dir;
            String_Sort[1] = ", goodsreceipt.duedate ";
            String_Sort[2] = ", billinggr.duedate ";
            String_Sort[3] = "";
            String_Sort[4] = "";
        } else if (Col_Name.equals("agentname")){
            String_Sort[0] = " order by value " + Col_Dir;
            String_Sort[1] = ", masteritem.value ";
            String_Sort[2] = ", billinggr.duedate ";
            String_Sort[3] = "";
            String_Sort[4] = "";
        } else if (Col_Name.equals("aliasname")){
            String_Sort[0] = " order by vendor.aliasname " + Col_Dir;
            String_Sort[3] = "";
            String_Sort[4] = "";
            String_Sort[1] = ", vendor.aliasname ";
            String_Sort[2] = ", vendor.aliasname ";
        } else if (Col_Name.equals("startDate")){
            String_Sort[0] = " order by RI.startdate " + Col_Dir;
            String_Sort[3] = " inner join repeatedinvoices RI on RI.id = goodsreceipt.repeateinvoice ";
            String_Sort[4] = "";
            String_Sort[1] = ", RI.startdate ";
            String_Sort[2] = ", RI.startdate ";
        } else if (Col_Name.equals("expireDate")){
            String_Sort[0] = " order by RI.expiredate " + Col_Dir;
            String_Sort[3] = " inner join repeatedinvoices RI on RI.id = goodsreceipt.repeateinvoice ";
            String_Sort[4] = "";
            String_Sort[1] = ", RI.expiredate ";
            String_Sort[2] = ", RI.expiredate ";
        } else if (Col_Name.equals("nextDate")){
            String_Sort[0] = " order by RI.nextdate " + Col_Dir;
            String_Sort[3] = " inner join repeatedinvoices RI on RI.id = goodsreceipt.repeateinvoice ";
            String_Sort[4] = "";
            String_Sort[1] = ", RI.nextdate ";
            String_Sort[2] = ", RI.nextdate ";
        } else if (Col_Name.equals("NoOfpost")){
            String_Sort[0] = " order by RI.noofinvoicespost " + Col_Dir;
            String_Sort[3] = " inner join repeatedinvoices RI on RI.id = goodsreceipt.repeateinvoice ";
            String_Sort[4] = "";
            String_Sort[1] = ", RI.noofinvoicespost ";
            String_Sort[2] = ", RI.noofinvoicespost ";
        } else {
            String_Sort[0] = " order by entrydate " + Col_Dir;
            String_Sort[1] = ", journalentry.entrydate ";
            String_Sort[2] = ", journalentry.entrydate ";
            String_Sort[3] = "";
            String_Sort[4] = "";

        }

        return String_Sort;

    }

    @Override
    public KwlReturnObject getOpeningBalanceInvoices(Map<String, Object> request) throws ServiceException {
        List<GoodsReceipt> list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            boolean forMonthWiseInvoices = request.containsKey("forMonthWiseInvoices");
            String companyid = (String) request.get(Constants.companyKey);
            String vendorId = "";
            boolean isAged = false;
            if (request.containsKey("isAged") && request.get("isAged") != null) {
                isAged = Boolean.parseBoolean((String) request.get("isAged"));
            }
            int datefilter = (request.containsKey("datefilter") && request.get("datefilter") != null) ? Integer.parseInt(request.get("datefilter").toString()) : 0;// 0 = Invoice Due date OR 1 = Invoice date
            if (request.containsKey("vendorid")) {
                vendorId = (String) request.get("vendorid");
            }
            if (vendorId == null) {
                vendorId = (String) request.get("accid");
            }
            String vendorIdGroup = (String) request.get("custVendorID");
            if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
                vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
            }

            String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");

            boolean isAccountInvoices = false;
            if (request.containsKey("isAccountInvoices") && request.get("isAccountInvoices") != null) {
                isAccountInvoices = (Boolean) request.get("isAccountInvoices");
            }
            String invoiceIdToSkip = "";
            if (request.containsKey("invoiceIdToSkip") && request.get("invoiceIdToSkip") != null) {
                invoiceIdToSkip = (String) request.get("invoiceIdToSkip");
            }

            String condition = "";
            ArrayList params = new ArrayList();
            params.add(companyid);

            if (request.containsKey("groupcombo") && request.get("groupcombo")!=null&& request.containsKey(Constants.globalCurrencyKey)&& request.get(Constants.globalCurrencyKey)!=null) {
                int groupcombo = (Integer) request.get("groupcombo");

                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    condition += " and gr.currency=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                } else if (groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    condition += " and gr.currency!=" + Integer.parseInt((String) request.get(Constants.globalCurrencyKey));
                }
            }
            
            if (isAccountInvoices && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                condition += " AND gr.account.ID=? ";
                params.add(accountId);
            }
            // Excise Opening Balance check from Vendor Master ERP-27108 
            if (request.containsKey("isExciseInvoice") && request.get("isExciseInvoice") != null) {
                boolean isExciseInvoice = Boolean.valueOf(request.get("isExciseInvoice").toString());
                condition += " AND gr.isExciseInvoice=? ";
                params.add(isExciseInvoice);
            }

            if (!StringUtil.isNullOrEmpty(vendorId)) {
                condition += " AND gr.vendor.ID=? ";
                params.add(vendorId);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND gr.vendor.ID IN " + vendorIdGroup;

            }

            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans) && !request.containsKey("isReceipt")) {
                condition += " and gr.currency.currencyID = ? ";
                params.add(currencyfilterfortrans);
            }
            if (!StringUtil.isNullOrEmpty(invoiceIdToSkip)) {
                condition += " and gr.id not in ( " + invoiceIdToSkip + " ) ";
            }
            if (request.containsKey("excludeNormalInv") && request.get("excludeNormalInv") != null) {
                boolean excludeNormalInv = (Boolean) request.get("excludeNormalInv");
                if (excludeNormalInv) {
                    condition += " and gr.normalInvoice=false ";
                }
            }

            String startDate = request.get(Constants.REQ_startdate)!=null?request.get(Constants.REQ_startdate).toString():"";
            String endDate =  request.get(Constants.REQ_enddate)!=null?request.get(Constants.REQ_enddate).toString():"";
            if (forMonthWiseInvoices) {
                if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                    condition += " and (gr.creationDate >=? and gr.creationDate <=?)";
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
            } else if (isAged) {
                if (!StringUtil.isNullOrEmpty(endDate)) {
//                    if (datefilter == 0) { // Due Date
//                        condition += " and gr.dueDate <=? ";
//                    } else { // Invoice Date
                        condition += " and gr.creationDate <=? ";  // in Aging Data will be fetch according to Ivoice creation date.
//                    }
                    if(request.containsKey("isMonthlyAgeingReport") && request.get("isMonthlyAgeingReport") != null) {                        
                        params.add(new Date(Long.parseLong(endDate)));
                    } else { 
                        params.add(df.parse(endDate));
                    }
                }
            }
            
            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"gr.vendor.name","gr.vendor.aliasname","gr.vendor.acccode", "gr.goodsReceiptNumber", "gr.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 5);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND gr.vendor IS NOT NULL ";
            }

            if(Constants.InvoiceAmountDueFlag && request.containsKey("onlyamountdue") && request.get("onlyamountdue")!=null && Boolean.parseBoolean(request.get("onlyamountdue").toString())) {
                if(request.containsKey("onlyclaimedamountdue") && request.get("onlyclaimedamountdue")!=null && Boolean.parseBoolean(request.get("onlyclaimedamountdue").toString())){
                    condition += " and ( gr.openingBalanceAmountDue != 0 or (gr.claimAmountDue != 0 and (gr.badDebtType = 1 or gr.badDebtType = 2))) ";
                } else{
                    condition += " and gr.openingBalanceAmountDue != 0 ";
                } 
            }
            if(Constants.InvoiceAmountDueFlag && request.containsKey("ignorezero") && request.get("ignorezero")!=null && request.get("ignorezero").toString().equals("false")) {
                condition += " and gr.openingBalanceAmountDue = 0 ";
            }
            if(Constants.InvoiceAmountDueFlag && request.containsKey("minimumAmountDue") && request.get("minimumAmountDue")!=null) {
                double minimummountDue = Double.parseDouble(request.get("minimumAmountDue").toString());
                condition += " and gr.openingBalanceAmountDue >= "+minimummountDue+" ";
            }
            Date asOfDate = null;
            if (request.containsKey("asofdate") && request.get("asofdate") != null) {
                String asOfDateString = (String) request.get("asofdate");
                asOfDate = df.parse(asOfDateString);
            }
            if (request.containsKey("isAgedPayables") && request.get("isAgedPayables") != null && Boolean.parseBoolean(request.get("isAgedPayables").toString()) && asOfDate != null) { // used for aged payables
                condition += " and (gr.openingBalanceAmountDue>0 or (gr.openingBalanceAmountDue=0 and gr.amountDueDate>?))";
                params.add(asOfDate);
            }
            String appendCase = "and";
            String mySearchFilterStringforOpeningTransaction = "";
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
                    if (customSearchFieldArray.length() > 0) {
                        /*
                         * Advance Search For Custom fields
                         */
                        request.put(Constants.Searchjson, Searchjson);
                        request.put(Constants.appendCase, appendCase);
                        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterStringforOpeningTransaction = String.valueOf(StringUtil.getAdvanceSearchString(request, false).get(Constants.myResult));
                        mySearchFilterStringforOpeningTransaction=mySearchFilterStringforOpeningTransaction.replaceAll("c.openingbalancevendorinvoicecustomdata", "gr.openingBalanceVendorInvoiceCustomData");
                        mySearchFilterStringforOpeningTransaction=mySearchFilterStringforOpeningTransaction.replaceAll("c.AccJEDetailCustomData", "gr.openingBalanceVendorInvoiceCustomData");
                        mySearchFilterStringforOpeningTransaction=mySearchFilterStringforOpeningTransaction.replaceAll("c.VendorCustomData", "gr.vendor.accVendorCustomData");
                        mySearchFilterStringforOpeningTransaction=mySearchFilterStringforOpeningTransaction.replaceAll("c.accjecustomdata", "gr.openingBalanceVendorInvoiceCustomData");
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    }
                }
            }
            
            String query = "select gr.id from GoodsReceipt gr where gr.isOpeningBalenceInvoice=true AND gr.deleted=false AND gr.company.companyID=?" + condition+mySearchFilterStringforOpeningTransaction;

            list = executeQuery( query, params.toArray());
            count = list.size();

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getGoodsReceipts : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, count);
    }

    @Override
    public int getOpeningBalanceInvoiceCount(Map<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
            String vendorId = "";
            if (request.containsKey("vendorid")) {
                vendorId = (String) request.get("vendorid");
            }

            String vendorIdGroup = (String) request.get("custVendorID");
            if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
                vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
            }

            String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");

            boolean isAccountInvoices = false;
            if (request.containsKey("isAccountInvoices") && request.get("isAccountInvoices") != null) {
                isAccountInvoices = (Boolean) request.get("isAccountInvoices");
            }

            String condition = "";
            ArrayList params = new ArrayList();
            params.add(companyid);

            if (isAccountInvoices && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                condition += " AND gr.account.ID=? ";
                params.add(accountId);
            }

            if (!StringUtil.isNullOrEmpty(vendorId)) {
                condition += " AND gr.vendor.ID=? ";
                params.add(vendorId);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND gr.vendor.ID IN " + vendorIdGroup;

            }

            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans) && !request.containsKey("isReceipt")) {
                condition += " and gr.currency.currencyID = ? ";
                params.add(currencyfilterfortrans);
            }
            if (request.containsKey("excludeNormalInv") && request.get("excludeNormalInv") != null) {
                boolean excludeNormalInv = (Boolean) request.get("excludeNormalInv");
                if (excludeNormalInv) {
                    condition += " and gr.normalInvoice=false ";
                }
            }


            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"gr.vendor.name","gr.vendor.acccode", "gr.goodsReceiptNumber", "gr.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND gr.vendor IS NOT NULL ";
            }

            if(Constants.InvoiceAmountDueFlag && request.containsKey("onlyamountdue") && request.get("onlyamountdue")!=null && Boolean.parseBoolean(request.get("onlyamountdue").toString())) {
                condition += " and gr.openingBalanceAmountDue != 0 ";
            }
            if(Constants.InvoiceAmountDueFlag && request.containsKey("ignorezero") && request.get("ignorezero")!=null && request.get("ignorezero").toString().equals("false")) {
                condition += " and gr.openingBalanceAmountDue == 0 ";
            }
            if(Constants.InvoiceAmountDueFlag && request.containsKey("minimumAmountDue") && request.get("minimumAmountDue")!=null) {
                double minimummountDue = Double.parseDouble(request.get("minimumAmountDue").toString());
                condition += " and gr.openingBalanceAmountDue >= "+minimummountDue+" ";
            }
            

            String query = "select count(gr.ID) from GoodsReceipt gr where gr.isOpeningBalenceInvoice=true AND gr.deleted=false AND gr.company.companyID=?" + condition;

            list = executeQuery( query, params.toArray());
            Long totalCnt = 0l;
            if (list != null && !list.isEmpty()){
                totalCnt = (Long) list.get(0);
            }
            count = totalCnt.intValue();

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getGoodsReceipts : " + ex.getMessage(), ex);
        }

        return count;
    }

    @Override
    public KwlReturnObject getOpeningBalanceTotalBaseAmountDueForInvoices(Map<String, Object> request) throws ServiceException {
        List<GoodsReceipt> list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
            String vendorId = "";
            if (request.containsKey("vendorid")) {
                vendorId = (String) request.get("vendorid");
            }

            String vendorIdGroup = (String) request.get("custVendorID");
            if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
                vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
            }

            String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");

            boolean isAccountInvoices = false;
            if (request.containsKey("isAccountInvoices") && request.get("isAccountInvoices") != null) {
                isAccountInvoices = (Boolean) request.get("isAccountInvoices");
            }

            String condition = "";
            ArrayList params = new ArrayList();
            params.add(companyid);

            if (isAccountInvoices && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                condition += " AND gr.account.ID=? ";
                params.add(accountId);
            }

            if (!StringUtil.isNullOrEmpty(vendorId)) {
                condition += " AND gr.vendor.ID=? ";
                params.add(vendorId);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND gr.vendor.ID IN " + vendorIdGroup;

            }

            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans) && !request.containsKey("isReceipt")) {
                condition += " and gr.currency.currencyID = ? ";
                params.add(currencyfilterfortrans);
            }
            if (request.containsKey("excludeNormalInv") && request.get("excludeNormalInv") != null) {
                boolean excludeNormalInv = (Boolean) request.get("excludeNormalInv");
                if (excludeNormalInv) {
                    condition += " and gr.normalInvoice=false ";
                }
            }


            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"gr.vendor.name","gr.vendor.acccode", "gr.goodsReceiptNumber", "gr.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND gr.vendor IS NOT NULL ";
            }

            if(Constants.InvoiceAmountDueFlag && request.containsKey("onlyamountdue") && request.get("onlyamountdue")!=null && Boolean.parseBoolean(request.get("onlyamountdue").toString())) {
                condition += " and gr.openingBalanceAmountDue != 0 ";
            }
            if(Constants.InvoiceAmountDueFlag && request.containsKey("ignorezero") && request.get("ignorezero")!=null && request.get("ignorezero").toString().equals("false")) {
                condition += " and gr.openingBalanceAmountDue == 0 ";
            }
            if(Constants.InvoiceAmountDueFlag && request.containsKey("minimumAmountDue") && request.get("minimumAmountDue")!=null) {
                double minimummountDue = Double.parseDouble(request.get("minimumAmountDue").toString());
                condition += " and gr.openingBalanceAmountDue >= "+minimummountDue+" ";
            }
            

            String query = "select COALESCE(SUM(gr.openingBalanceBaseAmountDue),0) from GoodsReceipt gr where gr.isOpeningBalenceInvoice=true AND gr.deleted=false AND gr.company.companyID=?" + condition;

            list = executeQuery( query, params.toArray());
            count = list.size();

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getOpeningBalanceBaseAmountDueInvoices : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, count);
    }

    
    @Override
    public KwlReturnObject getOpeningBalanceTotalBaseAmountForInvoices(Map<String, Object> request) throws ServiceException {
        List<GoodsReceipt> list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
            String vendorId = "";
            if (request.containsKey("vendorid")) {
                vendorId = (String) request.get("vendorid");
            }

            String vendorIdGroup = (String) request.get("custVendorID");
            if (!StringUtil.isNullOrEmpty(vendorIdGroup)) {
                vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
            }

            String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");

            boolean isAccountInvoices = false;
            if (request.containsKey("isAccountInvoices") && request.get("isAccountInvoices") != null) {
                isAccountInvoices = (Boolean) request.get("isAccountInvoices");
            }

            String condition = "";
            ArrayList params = new ArrayList();
            params.add(companyid);

            if (isAccountInvoices && request.containsKey("accountId") && request.get("accountId") != null) {
                String accountId = request.get("accountId").toString();
                if (request.containsKey("Searchjson") && request.get("Searchjson") != null && !StringUtil.isNullOrEmpty((String) request.get("Searchjson"))) {
                    condition += " AND goodsreceipt.account=? ";
                } else {
                    condition += " AND gr.account.ID=? ";
                }
                params.add(accountId);
            }

            if (!StringUtil.isNullOrEmpty(vendorId)) {
                condition += " AND gr.vendor.ID=? ";
                params.add(vendorId);
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                condition += " AND gr.vendor.ID IN " + vendorIdGroup;

            }

            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans) && !request.containsKey("isReceipt")) {
                condition += " and gr.currency.currencyID = ? ";
                params.add(currencyfilterfortrans);
            }
            if (request.containsKey("excludeNormalInv") && request.get("excludeNormalInv") != null) {
                boolean excludeNormalInv = (Boolean) request.get("excludeNormalInv");
                if (excludeNormalInv) {
                    condition += " and gr.normalInvoice=false ";
                }
            }


            String ss = (request.containsKey("ss") && request.get("ss") != null) ? (String) request.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"gr.vendor.name","gr.vendor.acccode", "gr.goodsReceiptNumber", "gr.account.name"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery + " AND gr.vendor IS NOT NULL ";
            }

            if(Constants.InvoiceAmountDueFlag && request.containsKey("onlyamountdue") && request.get("onlyamountdue")!=null && Boolean.parseBoolean(request.get("onlyamountdue").toString())) {
                condition += " and gr.openingBalanceAmountDue != 0 ";
            }
            if(Constants.InvoiceAmountDueFlag && request.containsKey("ignorezero") && request.get("ignorezero")!=null && request.get("ignorezero").toString().equals("false")) {
                condition += " and gr.openingBalanceAmountDue == 0 ";
            }
            if(Constants.InvoiceAmountDueFlag && request.containsKey("minimumAmountDue") && request.get("minimumAmountDue")!=null) {
                double minimummountDue = Double.parseDouble(request.get("minimumAmountDue").toString());
                condition += " and gr.openingBalanceAmountDue >= "+minimummountDue+" ";
            }

            String appendCase = "and";
            String mySearchFilterStringforOpeningTransaction = "";
            String joinString1 = "";
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
                    request.put(Constants.moduleid, 6);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    request.put("isOpeningBalance", true);
                    mySearchFilterStringforOpeningTransaction = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterStringforOpeningTransaction = mySearchFilterStringforOpeningTransaction.replaceAll("AccJEDetailCustomData", "openingbalancevendorinvoicecustomdata");
                    mySearchFilterStringforOpeningTransaction = mySearchFilterStringforOpeningTransaction.replaceAll("AccJEDetailsProductCustomData", "openingbalancevendorinvoicecustomdata");
                    joinString1 = " inner join openingbalancevendorinvoicecustomdata on openingbalancevendorinvoicecustomdata.openingbalancevendorinvoiceid=goodsreceipt.id ";
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }     
            
            String query = "";
            if (request.containsKey("Searchjson") && request.get("Searchjson") != null && !StringUtil.isNullOrEmpty((String)request.get("Searchjson"))){
                query = "Select COALESCE(SUM(goodsreceipt.originalOpeningBalanceBaseAmount),0) from goodsreceipt  "+joinString1+ " where goodsreceipt.isopeningbalenceinvoice=true AND goodsreceipt.deleteflag=false AND goodsreceipt.company=?" + condition + mySearchFilterStringforOpeningTransaction;
                list = executeSQLQuery( query, params.toArray());
            }else{
                query = "Select COALESCE(SUM(gr.originalOpeningBalanceBaseAmount),0) from GoodsReceipt gr where gr.isOpeningBalenceInvoice=true AND gr.deleted=false AND gr.company.companyID=?" + condition;
                list = executeQuery( query, params.toArray());
            }
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getGoodsReceipts : " + ex.getMessage(), ex);
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
                requestParams.put("moduleid",6);
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel));
                requestParams.put(Constants.filter_values, Arrays.asList(companyId,StringUtil.DecodeText(jobj1.getString("columnheader"))));
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
                     jobj.put("columnheader",StringUtil.DecodeText(jobj1.getString("columnheader")));
                    try{
                        jobj.put("combosearch", StringUtil.DecodeText(jobj1.optString("combosearch")));
                    } catch(Exception e){
                        jobj.put("combosearch", jobj1.getString("combosearch"));
                    }
                    jobj.put("combosearch", StringUtil.DecodeText(jobj1.optString("combosearch")));
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
    public KwlReturnObject getGoodsReceiptsMerged(Map<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        try {
            boolean ispendingAproval = false;
            int start = 0;
            int limit = 15;
            boolean pagingFlag = (request.containsKey("pagingFlag") && request.get("pagingFlag") != null) ? Boolean.parseBoolean(request.get("pagingFlag").toString())  : false;
            boolean ccrAllRecords = (request.containsKey("ccrAllRecords") && request.get("ccrAllRecords") != null) ? (Boolean) request.get("ccrAllRecords") : false;    //All CC records flag            
            boolean isAged = (request.containsKey("isAged") && request.get("isAged") != null) ? Boolean.parseBoolean(request.get("isAged").toString()) : false;
            if (pagingFlag && request.containsKey("start") && request.containsKey("limit") && request.get("start") != null && request.get("limit") != null && !StringUtil.isNullOrEmpty(request.get("start").toString()) && !StringUtil.isNullOrEmpty(request.get("limit").toString())) {
                start = Integer.parseInt(request.get("start").toString());
                limit = Integer.parseInt(request.get("limit").toString());
                pagingFlag = true;
            }
            String companyid = (String) request.get(COMPANYID);
            DateFormat df = (DateFormat) request.get("dateformat");
            String productid = (String) request.get(PRODUCTID);
            int invoiceLinkedWithGRNStatus = (request.containsKey("invoiceLinkedWithGRNStatus") && request.get("invoiceLinkedWithGRNStatus") != null) ? Integer.parseInt(request.get("invoiceLinkedWithGRNStatus").toString()) : 0;
            String userID = "";
            String moduleid = "";
            if (request.containsKey(Constants.moduleid) && request.get(Constants.moduleid) != null) {
                moduleid = request.get(Constants.moduleid).toString();
            }
            boolean requestfromdimensionbasedreport = (request.containsKey("requestfromdimensionbasedreport") && request.get("requestfromdimensionbasedreport") != null) ? Boolean.parseBoolean(request.get("requestfromdimensionbasedreport").toString()) : false;
            boolean isenableSalesPersonAgentFlow = false;
            if (request.containsKey("enablesalespersonagentflow") && request.get("enablesalespersonagentflow") != null && !StringUtil.isNullOrEmpty(request.get("enablesalespersonagentflow").toString())) {
                isenableSalesPersonAgentFlow = Boolean.parseBoolean(request.get("enablesalespersonagentflow").toString());
                if (isenableSalesPersonAgentFlow) {
                    if (request.containsKey("userid") && request.get("userid") != null && !StringUtil.isNullOrEmpty(request.get("userid").toString())) {
                        userID = (String) request.get("userid");
                    }
                }
            }
            
            String productCategoryid = "";
            if (request.containsKey(Constants.productCategoryid) && request.get(Constants.productCategoryid) != null) {
                productCategoryid = (String) request.get(Constants.productCategoryid);
            }

            String prodfilterVenid = (String) request.get(PRODFILTERVENID);
            String newvendorid = "";
            int datefilter = (request.containsKey("datefilter") && request.get("datefilter") != null) ? Integer.parseInt(request.get("datefilter").toString()) : 0;// 0 = Invoice Due date OR 1 = Invoice date
            String vendorIdGroup = "";
            if (request.containsKey("custVendorID") && request.get("custVendorID") != null) {
                vendorIdGroup = (String) request.get("custVendorID");
            }
            if (request.containsKey(NEWVENDORID) && request.get(NEWVENDORID) != null) {
                newvendorid = (String) request.get(NEWVENDORID);
            }
            boolean isExcise = false;
            if (request.containsKey("isExciseInvoice") && request.get("isExciseInvoice") != null) {
                isExcise =  Boolean.parseBoolean(request.get("isExciseInvoice").toString());
            }

            CompanyAccountPreferences pref = (CompanyAccountPreferences) get(CompanyAccountPreferences.class, companyid);
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("id",companyid);
            Object exPrefObject = executeQueryWithProjection(ExtraCompanyPreferences.class, new String[]{"columnPref"}, paramMap);
           JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
            boolean isPostingDateCheck = false;
            if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                isPostingDateCheck = true;
            }
//            KWLCurrency currency = (KWLCurrency) get(KWLCurrency.class, currencyid);
            Calendar startFinYearCal = Calendar.getInstance();
            Calendar endFinYearCal = Calendar.getInstance();
            startFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.setTime(pref.getFinancialYearFrom());
            boolean pendingapproval = request.get("pendingapproval") != null ? (Boolean) request.get("pendingapproval") : false;
            boolean isForTemplate = false;
            boolean isConsignment = false;
            boolean isMRPJOBWORKIN = false;
            if (request.containsKey("isForTemplate") && request.get("isForTemplate") != null) {
                isForTemplate = Boolean.parseBoolean(request.get("isForTemplate").toString());
            }

            boolean isOpeningBalanceInvoices = false;

            if (request.get("isOpeningBalanceInvoices") != null) {
                isOpeningBalanceInvoices = Boolean.parseBoolean((String) request.get("isOpeningBalanceInvoices"));
            }

            if (request.get("year") != null) {		// Check for the selected year in the year combo for charts			Neeraj
                int year = Integer.parseInt(request.get("year").toString());
                startFinYearCal.set(Calendar.YEAR, year);
                endFinYearCal.set(Calendar.YEAR, year);
            }

            endFinYearCal.add(Calendar.YEAR, 1);
            String vendorid = (String) request.get(VENDORID);
            String formtypeid = "0";//By Default, All
            String vatcommodityid = "all";//By Default, All
            String checkformstatus = "0";//By Default, All
            String customerid = (vendorid == null ? (String) request.get("accid") : vendorid);
            String ss = (String) request.get("ss");
            boolean CashAndInvoice = (request.containsKey("CashAndInvoice") && request.get("CashAndInvoice") != null) ? Boolean.parseBoolean(request.get("CashAndInvoice").toString()) : false;
            boolean cashonly = false;
            boolean personGroup = false;
            boolean isagedgraph = false;
            boolean isexpenseinv = false;
            boolean for1099Report = false;
            boolean isfavourite = false;
            boolean isFixedAsset = false;
            boolean isprinted = false;
            /**
             * Include Fixed Asset as well as normal vendor invoices.
             */
            boolean isCallForLandedCostInvoices = false;
            cashonly = Boolean.parseBoolean((String) request.get("cashonly"));
            String billID = (String) request.get("billid");
            String expenseinv = (String) request.get("onlyexpenseinv");
            for1099Report = Boolean.parseBoolean((String) request.get("for1099Report"));
            boolean deleted = false;
            if (request.get(DELETED) != null) {
                deleted = Boolean.parseBoolean((request.get(DELETED).toString()));
            }
            boolean nondeleted = false;
            if (request.get("nondeleted") != null) {
                nondeleted = Boolean.parseBoolean(request.get("nondeleted").toString());
            }
            if (request.get("isCallForLandedCostInvoices") != null) {
                isCallForLandedCostInvoices = Boolean.parseBoolean(request.get("isCallForLandedCostInvoices").toString());
            }
            if (request.get("isfavourite") != null) {
                isfavourite = Boolean.parseBoolean((String) request.get("isfavourite"));
            }
            String vendorCategoryid = "";
            if (request.containsKey(Constants.customerCategoryid) && request.get(Constants.customerCategoryid) != null) {
                vendorCategoryid = (String) request.get(Constants.customerCategoryid);
            }
            if (request.get("isFixedAsset") != null) {
                isFixedAsset = (Boolean) request.get("isFixedAsset");
            }
             if (request.containsKey("isConsignment") && request.get("isConsignment") != null) {
                isConsignment = (Boolean) request.get("isConsignment");
            }
             if (request.containsKey("isMRPJOBWORKIN") && request.get("isMRPJOBWORKIN") != null) {
                isMRPJOBWORKIN = (Boolean) request.get("isMRPJOBWORKIN");
            }
            if (request.containsKey("formtypeid") && request.get("formtypeid") != null) {
                formtypeid = (String) request.get("formtypeid");
            }
            if (request.containsKey("vatcommodityid") && request.get("vatcommodityid") != null) {
                vatcommodityid = (String) request.get("vatcommodityid");
            }
            if (request.containsKey("checkformstatus") && request.get("checkformstatus") != null) {
                checkformstatus = (String) request.get("checkformstatus");
            }
            if (request.get(Constants.MARKED_PRINTED) != null) {
                isprinted = Boolean.parseBoolean((String) request.get(Constants.MARKED_PRINTED));
            }
            if (request.containsKey("ispendingAproval") && request.get("ispendingAproval") != null) {
                ispendingAproval = Boolean.FALSE.parseBoolean(String.valueOf(request.get("ispendingAproval")));
            }
            boolean isMonthlyAgeingReport = false;
            if (request.get("isMonthlyAgeingReport") != null) {
                isMonthlyAgeingReport = Boolean.parseBoolean(request.get("isMonthlyAgeingReport").toString());
            }
            boolean includeAllRec = false;
            if (request.get("includeAllRec") != null) {
                includeAllRec = Boolean.parseBoolean(request.get("includeAllRec").toString());
            }
//            if (cashonly) {
//                customerid = cashAccount;
//            }
//            boolean ignoreZero = request.get("ignorezero") != null;
            
            boolean ispendingpayment = request.containsKey("ispendingpayment")?(Boolean)request.get("ispendingpayment"):false;
            
            String dueDate = (String) request.get("curdate");
            personGroup = Boolean.parseBoolean((String) request.get("persongroup"));
            isagedgraph = Boolean.parseBoolean((String) request.get("isagedgraph"));
            
            String userDepartment = "";
            if (request.containsKey("userDepartment") && request.get("userDepartment") != null) {
                userDepartment = (String) request.get("userDepartment");
            }
            
            ArrayList params = new ArrayList();
            ArrayList paramsSQLOpeningBalanceInv = new ArrayList();
            ArrayList paramsSQLWithoutInv = new ArrayList();
//            String condition = "";
            String conditionSQL = "";
            String conditionSQLForOpeningBalanceInvoice = "";
            params.add(companyid);
            paramsSQLWithoutInv.add(companyid);
            paramsSQLOpeningBalanceInv.add(companyid);
            String innerQuery3 = "";
            
            //below block of code executed when method called from getPoLinkedInTransaction() in case of getting linking
            if (request.containsKey("poid") && request.get("poid") != null) {
                String poid = (String) request.get("poid");
                //if(!StringUtil.isNullOrEmpty(poid)){
                params.add(poid);
                paramsSQLWithoutInv.add(poid);
                
                if (Boolean.parseBoolean(expenseinv)) {
                    innerQuery3 = " inner join expenseggrdetails on expenseggrdetails.goodsreceipt = goodsreceipt.id "
                            + " inner join expensepodetails on expenseggrdetails.expensepodetails = expensepodetails.id "
                            + " inner join purchaseorder on expensepodetails.purchaseorder = purchaseorder.id ";
                } else {
                    innerQuery3 = " inner join grdetails on grdetails.goodsreceipt = goodsreceipt.id "
                            + " inner join podetails on grdetails.purchaseorderdetail = podetails.id "
                            + " inner join purchaseorder on podetails.purchaseorder = purchaseorder.id ";
                }

                conditionSQL += " and  purchaseorder.id= ? ";
            }
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
//                condition += " and gr.journalEntry.ID IN(" + jeIds + ")";
                conditionSQL += " and journalentry.id IN(" + jeIds + ")";
            }

            if (!StringUtil.isNullOrEmpty(expenseinv)) {
                if(Boolean.parseBoolean(expenseinv)){
                    conditionSQL += " and goodsreceipt.isexpensetype='T'";
                } else{
                    conditionSQL += " and goodsreceipt.isexpensetype='F'";
                }
            }
            
            if (request.containsKey("onlyExpensePI") && !StringUtil.isNullOrEmpty((String) request.get("onlyExpensePI")) && Boolean.parseBoolean((String) request.get("onlyExpensePI"))) {
                conditionSQL += " and goodsreceipt.isexpensetype='T'";
            }
            if (request.containsKey("onlyInventoryPI") && !StringUtil.isNullOrEmpty((String) request.get("onlyInventoryPI")) && Boolean.parseBoolean((String) request.get("onlyInventoryPI"))) {
                conditionSQL += " and goodsreceipt.isexpensetype='F'";
            }
            /**
             * for 'PI With Full GRN', 'PI With No GRN' and PI With Partial GRN filter
             * not loaded Expense Invoice. 
             */
            if (invoiceLinkedWithGRNStatus != 0 && (invoiceLinkedWithGRNStatus == Constants.Filter_Invoice_WithFullGRN || 
                    invoiceLinkedWithGRNStatus == Constants.Filter_Invoice_WithNoGRN || invoiceLinkedWithGRNStatus == Constants.Filter_Invoice_WithPartialGRN)) {
                conditionSQL += " and goodsreceipt.isexpensetype='F'";
            }
            if (cashonly) {
                conditionSQL += " and goodsreceipt.cashtransaction=1 ";
            }

            if (request.containsKey("groupcombo") && request.get("groupcombo")!=null&& request.containsKey(Constants.globalCurrencyKey)&& request.get(Constants.globalCurrencyKey)!=null) {
                int groupcombo = (Integer) request.get("groupcombo");
                
                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    conditionSQL += " and goodsreceipt.currency=" + Integer.parseInt((String)request.get(Constants.globalCurrencyKey));
                }  else if(groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    conditionSQL += " and goodsreceipt.currency!=" + Integer.parseInt((String)request.get(Constants.globalCurrencyKey));
                }
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
                conditionSQL += " AND goodsreceipt.vendor in " + vendorIdGroup;
            }
            String repeatPIQry = "";
            if (request.containsKey("getRepeateInvoice") && request.get("getRepeateInvoice") != null) {
                if (Boolean.parseBoolean((String) request.get("getRepeateInvoice"))) {
                    repeatPIQry = " inner join repeatedinvoices on repeatedinvoices.id = goodsreceipt.repeateinvoice  ";
                    if (ispendingAproval) {   //Pending Approval Records
                        conditionSQL += " and (goodsreceipt.repeateinvoice is not null and repeatedinvoices.ispendingapproval='T') ";
                        //conditionSQLWithoutInv += " and (billinggr.repeateinvoice is not null and repeatedinvoices.ispendingapproval='T') ";
                    } else {
                        conditionSQL += " and (goodsreceipt.repeateinvoice is not null and repeatedinvoices.ispendingapproval='F') ";
                        //conditionSQLWithoutInv += " and (billinggr.repeateinvoice is not null and repeatedinvoices.ispendingapproval='F') ";
                    }
                }
            }
            if (!StringUtil.isNullOrEmpty(dueDate)) {
                if (for1099Report) {
                    params.add(df.parse(dueDate));
                    paramsSQLWithoutInv.add(df.parse(dueDate));
//                    condition += " and gr.journalEntry.entryDate<=?";
//                    conditionSQL += " and journalentry.entrydate<=?";
                    conditionSQL += " and goodsreceipt.creationdate<=?";
                } else {
                    if (datefilter == 0 && !isAged) {
                        if (isMonthlyAgeingReport) {
                            params.add(df.format(request.get("MonthlyAgeingStartDate")));
                            paramsSQLWithoutInv.add(df.format(request.get("MonthlyAgeingEndDate")));
                        } else {
                            params.add(df.parse(dueDate));
                            paramsSQLWithoutInv.add(df.parse(dueDate));
                        }   //                    condition += " and gr.dueDate<=?";
                        conditionSQL += " and goodsreceipt.duedate<=?";
                    } 
                }
            }
            if (!StringUtil.isNullOrEmpty(billID)) {
                params.add(billID);
                paramsSQLWithoutInv.add(billID);
                conditionSQL += " and goodsreceipt.id=?";
            }
            if (!StringUtil.isNullOrEmpty(formtypeid) && !formtypeid.equals("0")) {
                params.add(formtypeid);
                paramsSQLWithoutInv.add(formtypeid);
                conditionSQL += " and goodsreceipt.formtype=?";
            }
            if (!StringUtil.isNullOrEmpty(checkformstatus) && !checkformstatus.equals("0")) {
                params.add(checkformstatus);
                conditionSQL += " and goodsreceipt.formstatus=?";
            }
            if (!StringUtil.isNullOrEmpty(customerid)) {
                params.add(customerid);
                paramsSQLWithoutInv.add(customerid);
                conditionSQL += " and goodsreceipt.vendor=?";

            }
            
            if (request.containsKey("importServiceFlag") && request.get("importServiceFlag") != null) {
                conditionSQL += " and goodsreceipt.isimportservice=? ";
                params.add((Boolean)request.get("importServiceFlag"));
                paramsSQLWithoutInv.add((Boolean)request.get("importServiceFlag"));
            }
            if (request.containsKey("linknumber") && request.get("linknumber") != null && !request.get("linknumber").toString().equals("")) {
                conditionSQL += " and goodsreceipt.grnumber = ? ";
                params.add(request.get("linknumber"));
            }
            
            if (request.containsKey("isBadDebtInvoices") && request.get("isBadDebtInvoices") != null && (Boolean) request.get("isBadDebtInvoices")) {
                    int baddebttype = (Integer) request.get("baddebttype");
                    if (baddebttype == 0) {
                        params.add(df.parse((String) request.get("badDebtCalculationDate")));
                        paramsSQLWithoutInv.add(df.parse((String) request.get("badDebtCalculationDate")));
                        paramsSQLOpeningBalanceInv.add(df.parse((String) request.get("badDebtCalculationDate")));
                        params.add(baddebttype);
                        paramsSQLWithoutInv.add(baddebttype);
                        paramsSQLOpeningBalanceInv.add(baddebttype);
                        if ((Integer) request.get("badDebtCriteria") == 0) {// Filter on behalf of goodsreceipt due date
                            if(isOpeningBalanceInvoices){
                            conditionSQLForOpeningBalanceInvoice += " and goodsreceipt.duedate<=? and goodsreceipt.openingbalanceamountdue>0 and goodsreceipt.baddebttype=? ";
                            } else {
                                conditionSQL += " and goodsreceipt.duedate<=? and goodsreceipt.invoiceamountdue>0 and goodsreceipt.baddebttype=? ";
                            }
                        } else if ((Integer) request.get("badDebtCriteria") == 1) {// Filter on behalf of goodsreceipt creation date
                            if(isOpeningBalanceInvoices){
                                conditionSQLForOpeningBalanceInvoice += " and goodsreceipt.creationdate<=? and goodsreceipt.openingbalanceamountdue>0 and goodsreceipt.baddebttype=? ";
                            } else {
//                                conditionSQL += " and journalentry.entryDate<=? and .invoiceamountdue>0 and goodsreceipt.baddebttype=? ";
                                conditionSQL += " and goodsreceipt.creationdate<=? and goodsreceipt.invoiceamountdue>0 and goodsreceipt.baddebttype=? ";
                            }
                        }
                    } else if(baddebttype == 1){// for recover tab
                        params.add(df.parse((String) request.get("badDebtCalculationFromDate")));
                        paramsSQLWithoutInv.add(df.parse((String) request.get("badDebtCalculationFromDate")));
                        params.add(df.parse((String) request.get("badDebtCalculationToDate")));
                        paramsSQLWithoutInv.add(df.parse((String) request.get("badDebtCalculationToDate")));
                        params.add(baddebttype);
                        paramsSQLWithoutInv.add(baddebttype);
                        
                            params.add(2);
                            paramsSQLWithoutInv.add(2);
                            conditionSQL += " and (goodsreceipt.debtclaimeddate>=? and goodsreceipt.debtclaimeddate<=? ) and (goodsreceipt.baddebttype=? or goodsreceipt.baddebttype=?) ";
                    } 
            }
            
            if (!StringUtil.isNullOrEmpty(vendorCategoryid) && !StringUtil.equal(vendorCategoryid, "-1") && !StringUtil.equal(vendorCategoryid, "All")) {
                params.add(vendorCategoryid);
                paramsSQLWithoutInv.add(vendorCategoryid);
                conditionSQL += " and goodsreceipt.vendor in (select vendorid from vendorcategorymapping where vendorcategory = ?)  ";
            }
            if (!CashAndInvoice) {
                if (cashonly) {
                    params.add(true);
                    conditionSQL += " and goodsreceipt.cashtransaction=?";
                } else {
                    params.add(false);
                    conditionSQL += " and goodsreceipt.cashtransaction=?";
                }
            }
            if(isExcise){
                conditionSQL += " and goodsreceipt.isexciseinvoice = ? ";
                params.add(1);
            }
            if(request.containsKey("moduletemplateid") && !StringUtil.isNullOrEmpty(request.get("moduletemplateid").toString())){
                conditionSQL += " and goodsreceipt.moduletemplateid = ? ";
                params.add(request.get("moduletemplateid").toString());
            }

            String innerQuery = "";
            String tableInv = "";
            if (!StringUtil.isNullOrEmpty(productid)) {
                tableInv = ", grdetails.id as invid ";

                params.add(productid);
                innerQuery = " inner join grdetails on grdetails.goodsreceipt = goodsreceipt.id "
                        + " inner join inventory on grdetails.id = inventory.id ";
                if (StringUtil.isNullOrEmpty(prodfilterVenid)) {
                    conditionSQL += " and inventory.product = ? ";
                } else {
                    params.add(prodfilterVenid);
                    conditionSQL += " and inventory.product = ? and goodsreceipt.vendor = ? ";
                }

            }
            
            if (request.get("natureOfStockItem")!=null && !StringUtil.isNullOrEmpty(request.get("natureOfStockItem").toString())) {
                String natureofstockitem = request.get("natureOfStockItem").toString();
                params.add(natureofstockitem);
                innerQuery = " inner join grdetails on grdetails.goodsreceipt = goodsreceipt.id "
                        + " inner join inventory on grdetails.id = inventory.id "
                        + " inner join product on inventory.product = product.id";
                conditionSQL += " and product.natureofstockitem = ? ";
            }

            if (!StringUtil.isNullOrEmpty(productCategoryid)) {
                tableInv = ", grdetails.id as invid ";

                params.add(productCategoryid);
                innerQuery = " inner join grdetails on grdetails.goodsreceipt = goodsreceipt.id "
                        + " inner join inventory on grdetails.id = inventory.id ";
                if (StringUtil.isNullOrEmpty(prodfilterVenid)) {
                    conditionSQL += " and inventory.product in (select productid from productcategorymapping where productcategory = ?) ";
                } else {
                    params.add(prodfilterVenid);
                    conditionSQL += " and inventory.product in (select productid from productcategorymapping where productcategory = ?) and goodsreceipt.vendor = ? ";
                }
            }

            if (!StringUtil.isNullOrEmpty(newvendorid)) {
                if (newvendorid.contains(",")) {
                    newvendorid = AccountingManager.getFilterInString(newvendorid);
                    conditionSQL += " and goodsreceipt.vendor IN" + newvendorid;
                    conditionSQLForOpeningBalanceInvoice += " and goodsreceipt.vendor IN" + newvendorid;
                } else {
                    params.add(newvendorid);
                    paramsSQLWithoutInv.add(newvendorid);
                    conditionSQL += " and goodsreceipt.vendor = ? ";
                    paramsSQLOpeningBalanceInv.add(newvendorid);
                    conditionSQLForOpeningBalanceInvoice += " and goodsreceipt.vendor = ? ";
                }
            }
            String costCenterId = (String) request.get("costCenterId");
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                paramsSQLWithoutInv.add(costCenterId);
                conditionSQL += " and costcenter.id=?";
            } else {
                if (ccrAllRecords) {   //All CC records 
                    conditionSQL += " and costcenter.id IN (Select costcenter.id from costcenter)";
                }
            }
            String termid = (String) request.get("termid");
            if (!StringUtil.isNullOrEmpty(termid)) {
                params.add(termid);
                // paramsSQLWithoutInv.add(termid);
//                condition += " and gr.journalEntry.costcenter.ID=?";
                conditionSQL += " and goodsreceipt.termid=?";
                paramsSQLOpeningBalanceInv.add(termid);
                conditionSQLForOpeningBalanceInvoice += " and goodsreceipt.termid=?";
            }
            String startDate = request.get(Constants.REQ_startdate)!=null? StringUtil.DecodeText((String) request.get(Constants.REQ_startdate)):(String) request.get(Constants.REQ_startdate);
            String endDate = request.get(Constants.REQ_enddate)!=null? StringUtil.DecodeText((String) request.get(Constants.REQ_enddate)):(String) request.get(Constants.REQ_enddate);
            if ((!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) || (isAged && !StringUtil.isNullOrEmpty(endDate))) {
                if (includeAllRec) {
                    
                    if(isPostingDateCheck){
                        conditionSQL += " and ((goodsreceipt.creationdate >=? and goodsreceipt.creationdate <=?) or (goodsreceipt.creationdate BETWEEN ? and ?))";
                    }else{
                        conditionSQL += " and ((journalentry.entrydate >=? and journalentry.entrydate <=?) or (goodsreceipt.creationdate BETWEEN ? and ?))";
                    }
                } else if(isAged && !isMonthlyAgeingReport){// for aged needs all invoices whose creation date is previous to end date
//                    conditionSQL += " and journalentry.entrydate <=? ";
                    conditionSQL += " and goodsreceipt.creationdate <=? ";
                    conditionSQLForOpeningBalanceInvoice += " and goodsreceipt.creationdate <=? ";
                } else {
//                    conditionSQL += " and (journalentry.entrydate >=? and journalentry.entrydate <=?)";
                    conditionSQL += " and (goodsreceipt.creationdate >=? and goodsreceipt.creationdate <=?)";
                    conditionSQLForOpeningBalanceInvoice += " and (goodsreceipt.creationdate >=? and goodsreceipt.creationdate <=?)";
                }
                if (isMonthlyAgeingReport) {
                    params.add(new Date(Long.parseLong(startDate)));
                    params.add(new Date(Long.parseLong(endDate)));
                    paramsSQLOpeningBalanceInv.add(new Date(Long.parseLong(startDate)));
                    paramsSQLOpeningBalanceInv.add(new Date(Long.parseLong(endDate)));
                    paramsSQLWithoutInv.add(new Date(Long.parseLong(startDate)));
                    paramsSQLWithoutInv.add(new Date(Long.parseLong(endDate)));
                } else if(isAged){
                    params.add(df.parse(endDate));
                    paramsSQLOpeningBalanceInv.add(df.parse(endDate));
                } else {
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                    if (includeAllRec) {
                        params.add(df.parse(startDate));
                        params.add(df.parse(endDate));
                    }
                    paramsSQLOpeningBalanceInv.add(df.parse(startDate));
                    paramsSQLOpeningBalanceInv.add(df.parse(endDate));
                    paramsSQLWithoutInv.add(df.parse(startDate));
                    paramsSQLWithoutInv.add(df.parse(endDate));
                }
            }
            String searchJoin = "";
            String openingSerachJoin="";
            if (for1099Report) {
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"account.name"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQL += searchQuery;

                    searchcol = new String[]{"account.name"};
                    SearchStringMap = StringUtil.insertParamSearchStringMap(paramsSQLWithoutInv, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                }
            } else {
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"goodsreceipt.grnumber", "goodsreceipt.billfrom", "journalentry.entryno", "goodsreceipt.memo", "goodsreceipt.supplierinvoiceno", "account.name", "vendor.name","vendor.aliasname", "product.name", "product.productid",
                        "bsaddr.billingaddress", "bsaddr.billingcountry", "bsaddr.billingstate", "bsaddr.billingcounty", "bsaddr.billingcity", "bsaddr.billingemail", "bsaddr.billingpostal",
                        "bsaddr.shippingaddress", "bsaddr.shippingCountry", "bsaddr.shippingstate","bsaddr.shippingcounty",  "bsaddr.shippingcity", "bsaddr.shippingemail", "bsaddr.shippingpostal"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 24);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQL += searchQuery;
                    searchJoin = " left join grdetails on grdetails.goodsreceipt = goodsreceipt.id "
                            + " left join inventory on grdetails.id = inventory.id "
                            + " left join product on inventory.product = product.id "
                            + " left join billingshippingaddresses bsaddr on bsaddr.id=goodsreceipt.billingshippingaddresses ";
                    searchcol = new String[]{"goodsreceipt.grnumber","vendor.name","vendor.aliasname"};
                    SearchStringMap = StringUtil.insertParamSearchStringMap(paramsSQLOpeningBalanceInv, ss, 3);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQLForOpeningBalanceInvoice += searchQuery;
                    openingSerachJoin=" inner join vendor on vendor.id = goodsreceipt.vendor ";
                    searchcol = new String[]{"billinggr.billinggrnumber", "billinggr.billfrom", "journalentry.entryno", "billinggr.memo", "account.name"};
                    SearchStringMap = StringUtil.insertParamSearchStringMap(paramsSQLWithoutInv, ss, 5);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                }
            }


            Date startFinYearCalDate = null;
            Date endFinYearCalDate = null;

            String startFinYearCalString = authHandler.getDateOnlyFormat().format(startFinYearCal.getTime());
            startFinYearCalDate = authHandler.getDateOnlyFormat().parse(startFinYearCalString);

            String endFinYearCalString = authHandler.getDateOnlyFormat().format(endFinYearCal.getTime());
            endFinYearCalDate = authHandler.getDateOnlyFormat().parse(endFinYearCalString);

           
            
            if (personGroup) {
                params.add(startFinYearCalDate);
                params.add(endFinYearCalDate);
                paramsSQLWithoutInv.add(startFinYearCalDate);
                paramsSQLWithoutInv.add(endFinYearCalDate);
                conditionSQL += " and goodsreceipt.duedate>=? and goodsreceipt.duedate<=?";
            }
            if (isagedgraph) {
                params.add(startFinYearCalDate);
                params.add(endFinYearCalDate);
                paramsSQLWithoutInv.add(startFinYearCalDate);
                paramsSQLWithoutInv.add(endFinYearCalDate);
                conditionSQL += " and goodsreceipt.duedate>=? and goodsreceipt.duedate<=?";
            }
            if (nondeleted) {
                conditionSQL += " and goodsreceipt.deleteflag='F' ";
            } else if (deleted) {
                conditionSQL += " and goodsreceipt.deleteflag='T' ";
            }

            if (isfavourite) {
                conditionSQL += " and goodsreceipt.favouriteflag=true ";
            }
            if (!isAged) { //In case of Aged Payable we need all types of Invoices so no need to applying below check in case of aged 
                if (!isCallForLandedCostInvoices) {
                    if (isFixedAsset) {
                        conditionSQL += " and goodsreceipt.isfixedassetinvoice=true ";
                    } else {
                        conditionSQL += " and goodsreceipt.isfixedassetinvoice=false ";
                    }
                }
                if (isConsignment) {
                    conditionSQL += " and goodsreceipt.isconsignment='T'";
                } else {
                    conditionSQL += " and goodsreceipt.isconsignment='F'";
                }
                if (isMRPJOBWORKIN) {
                    conditionSQL += " and goodsreceipt.ismrpjobworkin='T'";
                } else {
                    conditionSQL += " and goodsreceipt.ismrpjobworkin='F'";
                }
            }          
            if (isprinted) {
                conditionSQL += " and goodsreceipt.printedflag=true ";
            }
            String excludeLinkedConsignments = (request.get("excludeLinkedConsignments") != null) ? request.get("excludeLinkedConsignments").toString() : "";
            if (!StringUtil.isNullOrEmpty(excludeLinkedConsignments)) {
                conditionSQL += " and goodsreceipt.id not in ( select goodsreceipt.id from goodsreceipt where goodsreceipt.isexpensetype <> 'F' ) ";
            }
            
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                innerQuery += " inner join users on users.userid = goodsreceipt.createdby ";
                conditionSQL += " and users.department = ? ";

                params.add(userDepartment);
            }
            //fetch isDraft flag from request
            boolean isDraft = false;
            if (request.containsKey(Constants.isDraft) && request.get(Constants.isDraft) != null) {
                isDraft = (Boolean) request.get(Constants.isDraft);
            }
            //Append isDraft condition in query for fetching particular draft transactions
            if (isDraft) {
                params.add(true);
                conditionSQL += " and goodsreceipt.isdraft = ? ";
            } else {
                params.add(false);
                conditionSQL += " and goodsreceipt.isdraft = ? ";
            }

            if (!isForTemplate) {
                //Ignore POs created as only templates.
                conditionSQL += " and goodsreceipt.istemplate != 2 ";

                if (pendingapproval) {
                    params.add(11);
                    conditionSQL += " and goodsreceipt.approvestatuslevel != ? ";
                } else {
                    params.add(11);
                    conditionSQL += " and goodsreceipt.approvestatuslevel = ? ";
                }
            }
            
            if (Constants.InvoiceAmountDueFlag && request.containsKey("minimumAmountDue") && request.get("minimumAmountDue") != null) {
                double minimummountDue = Double.parseDouble(request.get("minimumAmountDue").toString());
                conditionSQL += " and goodsreceipt.invoiceamountdue >= " + minimummountDue + " ";
            }
            /* Used in Aged Payables- to filter invoice on asOfdate */
            Date asOfDate = null;
            if (request.containsKey("asofdate") && request.get("asofdate") != null) {
                String asOfDateString = (String) request.get("asofdate");
                asOfDate = df.parse(asOfDateString);
            }
            if (!requestfromdimensionbasedreport) {
                if (request.containsKey("isAgedPayables") && request.get("isAgedPayables") != null && Boolean.parseBoolean(request.get("isAgedPayables").toString()) && asOfDate != null) {
                    conditionSQL += " and (((goodsreceipt.isopeningbalenceinvoice=false and goodsreceipt.invoiceamountdue>0) or (goodsreceipt.isopeningbalenceinvoice=true and goodsreceipt.openingbalanceamountdue>0)) or "
                            + "(((goodsreceipt.isopeningbalenceinvoice=false and goodsreceipt.invoiceamountdue=0) or (goodsreceipt.isopeningbalenceinvoice=true and goodsreceipt.openingbalanceamountdue=0)) and (goodsreceipt.amountduedate>? or goodsreceipt.amountduedate is null))) ";
                    params.add(asOfDate);
                }
            }
            if (request.containsKey("isreversalitc") && request.get("isreversalitc") != null) {
                boolean isreversalitc = (Boolean) request.get("isreversalitc");
                if (isreversalitc && !innerQuery.contains("grdetails")) {
                    innerQuery = " inner join grdetails on grdetails.goodsreceipt = goodsreceipt.id ";
                    conditionSQL += " and  grdetails.itctype=3 ";
                }
            }
            
            /* 
             * Please Add any condition in query above this comment. If you add any condition
             * after below code then advance search will not work. Its reason is that order of params is changes.
             */
            
            String appendCase = "and";
            String mySearchFilterString = "";
            String joinString1 = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String Searchjson = "";
            String searchDefaultFieldSQL="";
            String jeid=" jedetail.id = goodsreceipt.centry ";
//            String je="jedetail.id";
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
                        searchJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchJoin += " left join goodsreceiptlinking on goodsreceiptlinking.docid=goodsreceipt.id and goodsreceiptlinking.sourceflag = 1 ";
                    }
                    if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field
                        request.put(Constants.Searchjson, Searchjson);
                        request.put(Constants.appendCase, appendCase);
                        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        if (isOpeningBalanceInvoices) {
                            request.put("isOpeningBalance", isOpeningBalanceInvoices);
                            mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "openingbalancevendorinvoicecustomdata");  
                            joinString1 = " inner join openingbalancevendorinvoicecustomdata on openingbalancevendorinvoicecustomdata.openingbalancevendorinvoiceid=goodsreceipt.id ";
                            StringUtil.insertParamAdvanceSearchString1(paramsSQLOpeningBalanceInv, Searchjson);
                        } else {
                            mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                            if (mySearchFilterString.contains("accjecustomdata")) {
                                joinString1 = " inner join accjecustomdata on accjecustomdata.journalentryId=goodsreceipt.journalentry ";
                            }
                            if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//    
                                joinString1 += " left join accjedetailcustomdata  on accjedetailcustomdata.jedetailId=jedetail.id ";
                                jeid = " jedetail.journalentry = goodsreceipt.journalentry ";
                            }
                            if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "accjedetailproductcustomdata");//    
                                joinString1 += " left join accjedetailproductcustomdata  on accjedetailproductcustomdata.jedetailId=jedetail.id ";
                                jeid = " jedetail.journalentry = goodsreceipt.journalentry ";
                            }
                            if (mySearchFilterString.contains("VendorCustomData")) {
                                joinString1 += " left join vendorcustomdata  on vendorcustomdata.vendorId=vendor.id ";
                                mySearchFilterString = mySearchFilterString.replaceAll("VendorCustomData", "vendorcustomdata");
                            }
                            //product custom data
                            if (mySearchFilterString.contains("accproductcustomdata")) {
                                joinString1 += " inner join grdetails on grdetails.goodsreceipt=goodsreceipt.id left join inventory on inventory.id=grdetails.id "
                                        + "left join product on product.id=inventory.product left join accproductcustomdata on accproductcustomdata.productId=product.id ";
                            }
                            StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                        }
                        StringUtil.insertParamAdvanceSearchString1(paramsSQLWithoutInv, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);

                }
            }
            
            String orderBy = "";
            String sort_Col = "";
            String joinString2 = " ";
            String[] stringSort = null;
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                stringSort = columSort(Col_Name, Col_Dir);
                orderBy += stringSort[0];
                sort_Col += stringSort[1];
                innerQuery += stringSort[3];
                 if(request.get("sort").toString().equals("agentname")){
                     joinString2 += "  left join masteritem on masteritem.id = goodsreceipt.masteragent  ";
                 }
            } else {
                orderBy = " order by entrydate desc";
//                sort_Col += ", journalentry.entrydate ";
                sort_Col += ", goodsreceipt.creationdate ";
            }
//            params.addAll(paramsSQLWithoutInv);
            String salesPersonMappingQuery = "";
            if (isenableSalesPersonAgentFlow && !StringUtil.isNullOrEmpty(userID)) {
                salesPersonMappingQuery = " left join vendoragentmapping spm on spm.vendorid=goodsreceipt.vendor  left join masteritem  mst on mst.id=spm.agent ";
                joinString1+=salesPersonMappingQuery;
                conditionSQL += " and ((mst.user= '" + userID + "' or mst.user is null  and vendor.vendavailtoagent='T' ) or  (vendor.vendavailtoagent='F')) ";
            }

            
            if (ispendingpayment) {
                conditionSQL+= " and (goodsreceipt.invoiceamountdueinbase >?) ";
                params.add(0.0);
            }

            String productSearch = "";
            String productjoin = "";
            if (!StringUtil.isNullOrEmpty(vatcommodityid) && !vatcommodityid.equals("all")) {
                if(!joinString1.contains("left join product") &&  !searchJoin.contains("left join product") &&  !innerQuery.contains("left join product") &&  !innerQuery3.contains("left join product") &&  !repeatPIQry.contains("left join product") &&  !joinString2.contains("left join product")){
                    if(!joinString1.contains("left join grdetails") &&  !searchJoin.contains("left join grdetails") &&  !innerQuery.contains("left join grdetails") &&  !innerQuery3.contains("left join grdetails") &&  !repeatPIQry.contains("left join grdetails") &&  !joinString2.contains("left join grdetails")){
                        productjoin = " left join grdetails on grdetails.goodsreceipt = goodsreceipt.id "
                            + " left join inventory on grdetails.id = inventory.id "
                            + " left join product on product.id = inventory.product ";
                    } else if(!joinString1.contains("left join inventory") &&  !searchJoin.contains("left join inventory") &&  !innerQuery.contains("left join inventory") &&  !innerQuery3.contains("left join inventory") &&  !repeatPIQry.contains("left join inventory") &&  !joinString2.contains("left join inventory")){
                        productjoin =  " left join inventory on grdetails.id = inventory.id "
                            + " left join product on product.id = inventory.product ";
                    } else{
                        productjoin = " left join product on product.id = inventory.product ";
                    }
                    
                }
                productSearch += " and product.vatcommoditycode = '"+vatcommodityid+"' ";
            }
            String mysqlQuery = "select DISTINCT goodsreceipt.id,  'false' as withoutinventory, journalentry.createdon " + sort_Col + tableInv + " from goodsreceipt  "
                    + "inner join journalentry on goodsreceipt.journalentry = journalentry.id  "
                    + "inner join jedetail on "+jeid+" "
                    + " inner join account on account.id = jedetail.account "
                    + " inner join vendor on vendor.id = goodsreceipt.vendor "
                    + searchJoin + productjoin + innerQuery + joinString1 + innerQuery3 + repeatPIQry + joinString2
                    + "left join costcenter on costcenter.id = journalentry.costcenter  "
                    + " where goodsreceipt.company = ?" + conditionSQL + productSearch + mySearchFilterString + orderBy;
//                    + " union "
//                    + " select billinggr.id,  'true' as withoutinventory, journalentry.createdon "+sort_Col1+tableBillingInv+" from billinggr"
//                    + " inner join journalentry on billinggr.journalentry = journalentry.id  "
//                    + "inner join jedetail on jedetail.id = billinggr.centry "
//                //    + " inner join goodsreceipt on goodsreceipt.centry=jedetail.id  "
//        
//                    + " inner join account on account.id = jedetail.account "+joinString+innerQuery4
//                    + "left join costcenter on costcenter.id = journalentry.costcenter  "
//                    + " where billinggr.company = ?" + conditionSQLWithoutInv +mySearchFilterString+ orderBy;
//            list = executeQuery( query, params.toArray());
            if (isOpeningBalanceInvoices) {
                params = paramsSQLOpeningBalanceInv;
                mysqlQuery = " select goodsreceipt.id,  'false' as withoutinventory, goodsreceipt.creationdate, goodsreceipt.creationdate from goodsreceipt " + joinString1+openingSerachJoin
                        + " where goodsreceipt.isopeningbalenceinvoice=True and goodsreceipt.company = ?" + conditionSQLForOpeningBalanceInvoice + mySearchFilterString;
            } else if (includeAllRec) {
                mysqlQuery = "select DISTINCT goodsreceipt.id,  'false' as withoutinventory, journalentry.createdon " + sort_Col + tableInv + " from goodsreceipt  "
                        + "left join journalentry on goodsreceipt.journalentry = journalentry.id  "
                        + "left join jedetail on " + jeid + " "
                        + " left join account on account.id = jedetail.account "
                        + " left join vendor on vendor.id = goodsreceipt.vendor "
                        +productjoin+ searchJoin + innerQuery + joinString1 + innerQuery3 + repeatPIQry + joinString2
                        + "left join costcenter on costcenter.id = journalentry.costcenter  "
                        + " where goodsreceipt.company = ?" + conditionSQL + mySearchFilterString + productSearch + orderBy;
            }
            int count = 0;
            if (pagingFlag) {
                /**
                 * for 'PI With Full GRN', 'PI With No GRN' and PI With Partial GRN filter
                 * handling paging issue.
                 */
                if (!(invoiceLinkedWithGRNStatus != 0 && (invoiceLinkedWithGRNStatus == Constants.Filter_Invoice_WithFullGRN
                        || invoiceLinkedWithGRNStatus == Constants.Filter_Invoice_WithNoGRN || invoiceLinkedWithGRNStatus == Constants.Filter_Invoice_WithPartialGRN))) {
                mysqlQuery += " LIMIT " + limit + " OFFSET " + start + "";
                }
                String sqlQueryForCount = "select count(DISTINCT goodsreceipt.id) from goodsreceipt  "
                        + "inner join journalentry on goodsreceipt.journalentry = journalentry.id  "
                        + "inner join jedetail on jedetail.id = goodsreceipt.centry "
                        + " inner join account on account.id = jedetail.account "
                        + " inner join vendor on vendor.id = goodsreceipt.vendor "
                        +productjoin+ searchJoin + innerQuery + joinString1 + innerQuery3 + joinString2
                        + "left join costcenter on costcenter.id = journalentry.costcenter  "
                        + " where goodsreceipt.company = ?" + conditionSQL + mySearchFilterString + orderBy;

                if (isOpeningBalanceInvoices) {
                    params = paramsSQLOpeningBalanceInv;
                    sqlQueryForCount = " select count(*) from goodsreceipt " + joinString1 + openingSerachJoin
                            + " where goodsreceipt.isopeningbalenceinvoice=True and goodsreceipt.company = ?" + conditionSQLForOpeningBalanceInvoice + mySearchFilterString;
                } else if (includeAllRec) {
                    sqlQueryForCount = "select count(DISTINCT goodsreceipt.id) from goodsreceipt  "
                            + "left join journalentry on goodsreceipt.journalentry = journalentry.id  "
                            + "left join jedetail on jedetail.journalentry = goodsreceipt.journalentry "
                            + " left join account on account.id = jedetail.account "
                            + " left join vendor on vendor.id = goodsreceipt.vendor "
                            +productjoin+ searchJoin + innerQuery + joinString1 + innerQuery3 + joinString2
                            + "left join costcenter on costcenter.id = journalentry.costcenter  "
                            + " where goodsreceipt.company = ?" + conditionSQL + mySearchFilterString + productSearch + orderBy;
                }
                List countList = executeSQLQuery( sqlQueryForCount, params.toArray());
                if (!countList.isEmpty()) {
                    BigInteger bigInteger = (BigInteger) countList.get(0);
                    count = bigInteger.intValue();
                }
                request.put("totalCount", count);
            }
            list = executeSQLQuery( mysqlQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getGoodsReceipts : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getSelectedGoodsReceiptsMerged(Map<String, Object> request, String invoiceIds) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) request.get(COMPANYID);
            DateFormat df = (DateFormat) request.get("dateformat");
            String productid = (String) request.get(PRODUCTID);
            String prodfilterVenid = (String) request.get(PRODFILTERVENID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) get(CompanyAccountPreferences.class, companyid);
//            KWLCurrency currency = (KWLCurrency) get(KWLCurrency.class, currencyid);
            Calendar startFinYearCal = Calendar.getInstance();
            Calendar endFinYearCal = Calendar.getInstance();
            startFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.setTime(pref.getFinancialYearFrom());
            boolean pendingapproval = (Boolean) request.get("pendingapproval");

            if (request.get("year") != null) {		// Check for the selected year in the year combo for charts			Neeraj
                int year = Integer.parseInt(request.get("year").toString());
                startFinYearCal.set(Calendar.YEAR, year);
                endFinYearCal.set(Calendar.YEAR, year);
            }

            endFinYearCal.add(Calendar.YEAR, 1);
            String vendorid = (String) request.get(VENDORID);
            String customerid = (vendorid == null ? (String) request.get("accid") : vendorid);
            String ss = (String) request.get("ss");
            String cashAccount = pref.getCashAccount().getID();
            boolean cashonly = false;
            boolean creditonly = false;
            boolean personGroup = false;
            boolean isagedgraph = false;
            boolean isexpenseinv = false;
            boolean only1099Vend = false;
            boolean for1099Report = false;
            boolean isfavourite = false;
            String group = "";
            cashonly = Boolean.parseBoolean((String) request.get("cashonly"));
            creditonly = Boolean.parseBoolean((String) request.get("creditonly"));
            only1099Vend = Boolean.parseBoolean((String) request.get("only1099Vend"));
            String billID = (String) request.get("billid");
            String expenseinv = (String) request.get("onlyexpenseinv");
            for1099Report = Boolean.parseBoolean((String) request.get("for1099Report"));
            boolean deleted = Boolean.parseBoolean((String) request.get(DELETED));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            if (request.get("isfavourite") != null) {
                isfavourite = Boolean.parseBoolean((String) request.get("isfavourite"));
            }

            if (cashonly) {
                customerid = cashAccount;
            }
//            boolean ignoreZero = request.get("ignorezero") != null;
            String dueDate = (String) request.get("curdate");
            personGroup = Boolean.parseBoolean((String) request.get("persongroup"));
            isagedgraph = Boolean.parseBoolean((String) request.get("isagedgraph"));
            ArrayList params = new ArrayList();
            ArrayList paramsSQLWithoutInv = new ArrayList();
//            String condition = "";
            String conditionSQL = "";
            String conditionSQLWithoutInv = "";
            String venCondition = "";
            params.add(companyid);
            paramsSQLWithoutInv.add(companyid);

            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
//                condition += " and gr.journalEntry.ID IN(" + jeIds + ")";
                conditionSQL += " and journalentry.id IN(" + jeIds + ")";
                conditionSQLWithoutInv += " and journalentry.id IN(" + jeIds + ")";
            }

            if (!StringUtil.isNullOrEmpty(expenseinv)) {
                isexpenseinv = Boolean.parseBoolean(expenseinv);
                params.add(isexpenseinv);
//                condition += " and gr.isExpenseType=?";
                conditionSQL += " and goodsreceipt.isexpensetype=?";


            }
            if (!StringUtil.isNullOrEmpty(dueDate)) {
                if (for1099Report) {
                    params.add(df.parse(dueDate));
                    paramsSQLWithoutInv.add(df.parse(dueDate));
//                    condition += " and gr.journalEntry.entryDate<=?";
//                    conditionSQL += " and journalentry.entrydate<=?";
                    conditionSQL += " and goodsreceipt.creationdate<=?";
                    conditionSQLWithoutInv += " and journalentry.entrydate<=?";
                } else {
                    params.add(df.parse(dueDate));
                    paramsSQLWithoutInv.add(df.parse(dueDate));
//                    condition += " and gr.dueDate<=?";
                    conditionSQL += " and goodsreceipt.duedate<=?";
                    conditionSQLWithoutInv += " and billinggr.duedate<=?";
                }
            }
            if (!StringUtil.isNullOrEmpty(invoiceIds)) {
//                params.add(billID);
//                paramsSQLWithoutInv.add(billID);
//                condition += " and gr.ID=?";
                conditionSQL += " and goodsreceipt.id IN(" + invoiceIds + ")";
                conditionSQLWithoutInv += " and billinggr.id IN(" + invoiceIds + ")";
            } else {
                if (!StringUtil.isNullOrEmpty(customerid)) {
                    params.add(customerid);
                    paramsSQLWithoutInv.add(customerid);
//                    condition += " and gr.vendorEntry.account.ID=?";
                    conditionSQL += " and jedetail.account=?";
                    conditionSQLWithoutInv += " and jedetail.account=?";
                } else {
                    String qMarks = "null,";
                    if (!creditonly) {
                        qMarks += "?,";
                        params.add(cashAccount);
                        paramsSQLWithoutInv.add(cashAccount);
                    }//else{
                    if (only1099Vend) {     //remove this condition in case of viewing all vendors in 1099 [PS]
                        venCondition += " and taxEligible=true"; //gr.vendorEntry.account.ID in(select v.ID from Vendor v v.taxEligible=true and v.ID=gr.vendorEntry.account.ID";
                    }
                    String q = "select ID from Vendor where company.companyID=?" + venCondition;
                    Iterator itrcust = executeQuery( q, new Object[]{companyid}).iterator();
                    while (itrcust.hasNext()) {
                        qMarks += "?,";
                        String vendorAcc = (itrcust.next()).toString();
                        params.add(vendorAcc);
                        paramsSQLWithoutInv.add(vendorAcc);
                    }
                    //}
                    qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
//                    condition += " and gr.vendorEntry.account.ID in (" + qMarks + ")";
                    conditionSQL += " and jedetail.account in (" + qMarks + ")";
                    conditionSQLWithoutInv += " and jedetail.account in (" + qMarks + ")";
                }
            }

            String innerQuery = "";
            String tableInv = "";
            String tableBillingInv = "";
            if (!StringUtil.isNullOrEmpty(productid)) {
                tableInv = ", grdetails.id as invid ";
                tableBillingInv = ", '' as invid ";

                params.add(productid);
                params.add(prodfilterVenid);
                innerQuery = " inner join grdetails on grdetails.goodsreceipt = goodsreceipt.id "
                        + " inner join inventory on grdetails.id = inventory.id ";
                conditionSQL += " and inventory.product = ? and goodsreceipt.vendor = ? ";
                conditionSQLWithoutInv += " and jedetail.account = '' ";

            }
            String costCenterId = (String) request.get("costCenterId");
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                paramsSQLWithoutInv.add(costCenterId);
//                condition += " and gr.journalEntry.costcenter.ID=?";
                conditionSQL += " and costcenter.id=?";
                conditionSQLWithoutInv += " and costcenter.id=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
//                condition += " and (gr.journalEntry.entryDate >=? and gr.journalEntry.entryDate <=?)";
//                conditionSQL += " and (journalentry.entrydate >=? and journalentry.entrydate <=?)";
                conditionSQL += " and (goodsreceipt.creationdate >=? and goodsreceipt.creationdate <=?)";
                conditionSQLWithoutInv += " and (journalentry.entrydate >=? and journalentry.entrydate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
                paramsSQLWithoutInv.add(df.parse(startDate));
                paramsSQLWithoutInv.add(df.parse(endDate));
            }
            if (for1099Report) {
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"account.name"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQL += searchQuery;

                    searchcol = new String[]{"account.name"};
                    SearchStringMap = StringUtil.insertParamSearchStringMap(paramsSQLWithoutInv, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQLWithoutInv += searchQuery;

//                        params.add(ss + "%");
//                        paramsSQLWithoutInv.add(ss + "%");
////                    condition += " and gr.vendorEntry.account.name like ? ";
//                    conditionSQL += " and account.name like ? ";
//                    conditionSQLWithoutInv += " and account.name like ? ";
                }
            } else {
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"goodsreceipt.grnumber", "goodsreceipt.billfrom", "journalentry.entryno", "goodsreceipt.memo", "account.name"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 5); 
                    StringUtil.insertParamSearchString(map);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQL += searchQuery;

                    searchcol = new String[]{"billinggr.billinggrnumber", "billinggr.billfrom", "journalentry.entryno", "billinggr.memo", "account.name"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramsSQLWithoutInv, ss, 5);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQLWithoutInv += searchQuery;

//                    for (int i = 0; i <= 4; i++) {
//                        params.add(ss + "%");
//                        paramsSQLWithoutInv.add(ss + "%");
//                    }
////                    condition += " and (gr.goodsReceiptNumber like ? or gr.billFrom like ?  or gr.journalEntry.entryNumber like ? or gr.memo like ? or gr.vendorEntry.account.name like ? ) ";
//                    conditionSQL += " and (goodsreceipt.grnumber like ? or goodsreceipt.billfrom like ?  or journalentry.entryno like ? or goodsreceipt.memo like ? or account.name like ? ) ";
//                    conditionSQLWithoutInv += " and (billinggr.billinggrnumber like ? or billinggr.billfrom like ?  or journalentry.entryno like ? or billinggr.memo like ? or account.name like ? ) ";
                }
            }
            Date startFinYearCalDate = null;
            Date endFinYearCalDate = null;
        
            String startFinYearCalString = authHandler.getDateOnlyFormat().format(startFinYearCal.getTime());
            startFinYearCalDate = authHandler.getDateOnlyFormat().parse(startFinYearCalString);

            String endFinYearCalString = authHandler.getDateOnlyFormat().format(endFinYearCal.getTime());
            endFinYearCalDate = authHandler.getDateOnlyFormat().parse(endFinYearCalString);

        
            
            if (personGroup) {
                params.add(startFinYearCalDate);
                params.add(endFinYearCalDate);
                paramsSQLWithoutInv.add(startFinYearCalDate);
                paramsSQLWithoutInv.add(endFinYearCalDate);
//                condition += " and gr.dueDate>=? and gr.dueDate<=?";
                conditionSQL += " and goodsreceipt.duedate>=? and goodsreceipt.duedate<=?";
                conditionSQLWithoutInv += " and billinggr.duedate>=? and billinggr.duedate<=?";
            }
            if (isagedgraph) {
                params.add(startFinYearCalDate);
                params.add(endFinYearCalDate);
                paramsSQLWithoutInv.add(startFinYearCalDate);
                paramsSQLWithoutInv.add(endFinYearCalDate);
//                condition += " and gr.dueDate>=? and gr.dueDate<=?";
                conditionSQL += " and goodsreceipt.duedate>=? and goodsreceipt.duedate<=?";
                conditionSQLWithoutInv += " and billinggr.duedate>=? and billinggr.duedate<=?";
            }
            if (nondeleted) {
//                 condition += " and gr.deleted=false ";
                conditionSQL += " and goodsreceipt.deleteflag='F' ";
                conditionSQLWithoutInv += " and billinggr.deleteflag='F' ";
            } else if (deleted) {
//                 condition += " and gr.deleted=true ";
                conditionSQL += " and goodsreceipt.deleteflag='T' ";
                conditionSQLWithoutInv += " and billinggr.deleteflag='T' ";
            }

            if (isfavourite) {
                conditionSQL += " and goodsreceipt.favouriteflag=true ";
                conditionSQLWithoutInv += " and billinggr.favouriteflag=true ";
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
                    joinString = " inner join accjecustomdata on accjecustomdata.journalentryId=billinggr.journalentry ";
                    joinString1 = " inner join accjecustomdata on accjecustomdata.journalentryId=goodsreceipt.journalentry ";
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    StringUtil.insertParamAdvanceSearchString1(paramsSQLWithoutInv, Searchjson);
                }
            }


            if (pendingapproval) {
                conditionSQL += " and goodsreceipt.pendingapproval != 0 ";
                conditionSQLWithoutInv += " and billinggr.pendingapproval != 0 ";
            } else {
                conditionSQL += " and goodsreceipt.pendingapproval= 0 ";
                conditionSQLWithoutInv += " and billinggr.pendingapproval= 0 ";
            }

            //Block records created as only template
            conditionSQL += " and goodsreceipt.istemplate != 2 ";
            conditionSQLWithoutInv += " and billinggr.istemplate != 2 ";

//            String query = "from GoodsReceipt gr where gr.company.companyID=? " + condition + group + " order by gr.vendorEntry.account.ID, gr.goodsReceiptNumber asc";

            params.addAll(paramsSQLWithoutInv);

            String mysqlQuery = "select goodsreceipt.id,  'false' as withoutinventory, journalentry.createdon " + tableInv + " from goodsreceipt  "
                    + "inner join journalentry on goodsreceipt.journalentry = journalentry.id  "
                    + "inner join jedetail on jedetail.id = goodsreceipt.centry "
                    + " inner join account on account.id = jedetail.account "
                    + innerQuery + joinString1
                    + "left join costcenter on costcenter.id = journalentry.costcenter  "
                    + " where goodsreceipt.company = ?" + conditionSQL + mySearchFilterString + " "
                    + " union "
                    + " select billinggr.id,  'true' as withoutinventory, journalentry.createdon " + tableBillingInv + " from billinggr"
                    + " inner join journalentry on billinggr.journalentry = journalentry.id  "
                    + "inner join jedetail on jedetail.id = billinggr.centry "
                    + " inner join account on account.id = jedetail.account " + joinString
                    + "left join costcenter on costcenter.id = journalentry.costcenter  "
                    + " where billinggr.company = ?" + conditionSQLWithoutInv + mySearchFilterString + " order by createdon desc";
//            list = executeQuery( query, params.toArray());
            list = executeSQLQuery( mysqlQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getGoodsReceipts : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteGoodsReceipts(String receiptid, String companyid) throws ServiceException {
        //Delete Goods Receipts
        String delQuery = "delete from GoodsReceipt gr where ID=? and gr.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Goods Receipt has been deleted successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject deleteOpeningGoodsReceiptsCustomData(String receiptid) throws ServiceException {
        //Delete Opening Goods Receipts
        String delQuery = "delete from OpeningBalanceVendorInvoiceCustomData gr where OpeningBalanceVendorInvoiceId=? ";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid});
        return new KwlReturnObject(true, "Opening Goods Receipt has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject deleteAssetDetailsLinkedWithGR(HashMap<String, Object> requestParams) throws ServiceException {
        int numtotal = 0;
        try {
            if (requestParams.containsKey("greceiptid") && requestParams.containsKey("companyid")) {

                int numRows = 0;
                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("companyid"));
                params8.add(requestParams.get("greceiptid"));

                String assetDetailIdString = "";

                // Deleting data from asset details table

                String assetQuery = "SELECT ad.id FROM goodsreceipt gr "
                        + "INNER JOIN  grdetails grd ON gr.id=grd.goodsReceipt "
                        + "INNER JOIN assetdetailsinvdetailmapping amp ON grd.id=amp.invoicedetailid "
                        + "INNER JOIN assetdetail ad on ad.id=amp.assetdetails "
                        + "WHERE amp.moduleid=6 AND ad.assetsoldflag=0 AND gr.company=? and gr.id=?";

                List assetList = executeSQLQuery( assetQuery, params8.toArray());
                Iterator assetItr = assetList.iterator();

                while (assetItr.hasNext()) {
                    String assetDetailId = assetItr.next().toString();
                    assetDetailIdString += "'" + assetDetailId + "',";
                }

                if (!StringUtil.isNullOrEmpty(assetDetailIdString)) {
                    assetDetailIdString = assetDetailIdString.substring(0, assetDetailIdString.length() - 1);
                }

//                String myquery = "select id from grdetails where goodsReceipt in (select id from goodsreceipt where company =? and id = ?)";
                String myquery = "select grd.id from grdetails grd inner join goodsreceipt gr on gr.id=grd.goodsReceipt where gr.company =? and gr.id = ?";
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

                // Deleting data from assetdetailsinvdetailmapping

                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    ArrayList assetParams = new ArrayList();
                    assetParams.add(requestParams.get("companyid"));

                    String assetMapDelQuery = "DELETE FROM assetdetailsinvdetailmapping WHERE invoicedetailid IN (" + idStrings + ") and moduleid=6 and company=?";
                    numRows = executeSQLUpdate( assetMapDelQuery, assetParams.toArray());
                }

                if (!StringUtil.isNullOrEmpty(assetDetailIdString)) {
                    ArrayList assetParams = new ArrayList();
                    assetParams.add(requestParams.get("companyid"));

                    String assupdateQuery = "DELETE FROM assetdetailcustomdata  WHERE assetDetailsId IN(" + assetDetailIdString + ") AND company=?";
                    numRows += executeSQLUpdate( assupdateQuery, assetParams.toArray());
                    String deletemachineasset = "DELETE FROM machine_asset_mapping  WHERE assetDetails IN (" + assetDetailIdString + ") AND company=?";
                    numtotal += executeSQLUpdate(deletemachineasset, assetParams.toArray());
                    assupdateQuery = "DELETE FROM assetdetail  WHERE id IN (" + assetDetailIdString + ") AND company=?";
                    numRows += executeSQLUpdate( assupdateQuery, assetParams.toArray());
                }

                numtotal = numRows;
            }

        } catch (Exception ex) {
            try {
                throw new AccountingException("Cannot delete Goods Receipt as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
            } catch (AccountingException ex1) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return new KwlReturnObject(true, "Goods Receipt has been deleted successfully.", null, null, numtotal);
    }

    @Override
    public KwlReturnObject deleteGoodsReceiptPermanent(HashMap<String, Object> requestParams) throws AccountingException {
        String delQuery = "", delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuery4 = "", delQuery5 = "", delQuery6 = "", delQuery7 = "", delQuery8 = "", delQuery11 = "",delQuery12 = "";
        int numtotal = 0;
        boolean isFixedAsset = false;
        try {
            if (requestParams.containsKey("greceiptid") && requestParams.containsKey("companyid")) {

                if (requestParams.containsKey("isFixedAsset") && requestParams.get("isFixedAsset") != null) {
                    isFixedAsset = (Boolean) requestParams.get("isFixedAsset");
                }

                String assetDetailIdString = "";

                boolean isexpenseinv = Boolean.parseBoolean((String) requestParams.get("isexpenseinv"));
                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("companyid"));
                params8.add(requestParams.get("greceiptid"));


                //This code used for update the link flag of vendor quotation
                ArrayList updateLinkFlagList = new ArrayList();
                updateLinkFlagList.add(requestParams.get("greceiptid"));
                String vendorIdQuery ="update vendorquotation as v inner join  vendorquotationdetails as vqd inner join grdetails grd on  v.id=vqd.vendorquotation and grd.vendorquotationdetail=vqd.id set linkflag=0 , isopen='T' where grd.goodsreceipt=?";
                int result = executeSQLUpdate( vendorIdQuery, updateLinkFlagList.toArray());
                // Deleting data from asset details table
                
                if (isFixedAsset) {
                    String assetQuery = "SELECT ad.id FROM goodsreceipt gr "
                            + "INNER JOIN  grdetails grd ON gr.id=grd.goodsReceipt "
                            + "INNER JOIN assetdetailsinvdetailmapping amp ON grd.id=amp.invoicedetailid "
                            + "INNER JOIN assetdetail ad on ad.id=amp.assetdetails "
                            + "WHERE amp.moduleid=6 AND ad.assetsoldflag=0 AND gr.company=? and gr.id=?";

                    List assetList = executeSQLQuery( assetQuery, params8.toArray());
                    Iterator assetItr = assetList.iterator();

                    while (assetItr.hasNext()) {
                        String assetDetailId = assetItr.next().toString();
                        assetDetailIdString += "'" + assetDetailId + "',";
                    }

                    if (!StringUtil.isNullOrEmpty(assetDetailIdString)) {
                        assetDetailIdString = assetDetailIdString.substring(0, assetDetailIdString.length() - 1);
                    }
                }



                String myquery = "";
                if (isexpenseinv) {
//                    myquery = "select id from expenseggrdetails where goodsReceipt in (select id from goodsreceipt where company =? and id = ?)";
                    myquery = "select egrd.id from expenseggrdetails egrd inner join goodsreceipt gr on gr.id=egrd.goodsReceipt where gr.company =? and gr.id = ?";
                } else {
//                    myquery = "select id from grdetails where goodsReceipt in (select id from goodsreceipt where company =? and id = ?)";
                    myquery = "select grd.id from grdetails grd inner join goodsreceipt gr on gr.id=grd.goodsReceipt where gr.company =? and gr.id =?";
                }
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

                // Deleting data from assetdetailsinvdetailmapping
                /**
                 * Delete GST Fields for India.
                 */
                deleteGstTaxClassDetails(idStrings);
                if (isFixedAsset) {
                    if (!StringUtil.isNullOrEmpty(idStrings)) {
                        ArrayList assetParams = new ArrayList();
                        assetParams.add(requestParams.get("companyid"));

                        String assetMapDelQuery = "DELETE FROM assetdetailsinvdetailmapping WHERE invoicedetailid IN (" + idStrings + ") and moduleid=6 and company=?";
                        numtotal += executeSQLUpdate( assetMapDelQuery, assetParams.toArray());
                    }

                    if (!StringUtil.isNullOrEmpty(assetDetailIdString)) {
                        ArrayList assetParams = new ArrayList();
                        assetParams.add(requestParams.get("companyid"));

                        String assupdateQuery = "DELETE FROM assetdetailcustomdata  WHERE assetDetailsId IN(" + assetDetailIdString + ") AND company=?";
                        numtotal += executeSQLUpdate( assupdateQuery, assetParams.toArray());
                        String deletemachineasset = "DELETE FROM machine_asset_mapping  WHERE assetDetails IN (" + assetDetailIdString + ") AND company=?";
                        numtotal += executeSQLUpdate(deletemachineasset, assetParams.toArray());
                         assupdateQuery = "DELETE FROM assetdetail  WHERE id IN (" + assetDetailIdString + ") AND company=?";
                        numtotal += executeSQLUpdate( assupdateQuery, assetParams.toArray());
                    }
                }
                    
                ArrayList params5 = new ArrayList();
                params5.add(requestParams.get("companyid"));
                params5.add(requestParams.get("greceiptid"));
//                delQuery5 = "delete  from grdetails where goodsReceipt in (select id from goodsreceipt where company =? and id = ?)";
                delQuery5 = "delete grd from grdetails grd inner join goodsreceipt gr on grd.goodsReceipt=gr.id where gr.company =? and gr.id = ?";
                int numRows5 = executeSQLUpdate( delQuery5, params5.toArray());
                
                int numRows12=0;
                if (requestParams.containsKey("greceiptid") && requestParams.get("greceiptid") != null) {
                    ArrayList params12 = new ArrayList();
                    params12.add(requestParams.get("greceiptid"));
                    /*
                     * Landed cost category manual amount entry deleted
                     */
                    delQuery12 = "delete from lccmanualwiseproductamount where expenseInvoiceid = ?";
                    numRows12 = executeSQLUpdate(delQuery12, params12.toArray());
                }
                
                ArrayList params11 = new ArrayList();
                params11.add(requestParams.get("companyid"));
                params11.add(requestParams.get("greceiptid"));
//                delQuery11 = "delete from expenseggrdetails where goodsreceipt in (select id from goodsreceipt where company =? and id = ?)";
                delQuery11 = "delete egrd from expenseggrdetails egrd inner join goodsreceipt gr on gr.id=egrd.goodsReceipt where gr.company =? and gr.id = ?";
                int numRows11 = executeSQLUpdate( delQuery11, params11.toArray());


//                ArrayList params = new ArrayList();
//                params.add(requestParams.get("companyid"));
//                delQuery = "delete  from inventory where company =?  and id in(" + idStrings + ") ";
//                int numRows = executeSQLUpdate( delQuery, params.toArray());
                String companyid = (String) requestParams.get("companyid");
                String selQuery = "";
                List resultList = null;
                int numRows = 0;
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    selQuery = "from Inventory where company.companyID = ? and  ID in (" + idStrings + ") ";
                    resultList = executeQuery(selQuery, new Object[]{companyid});
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

                ArrayList params7 = new ArrayList();
                params7.add(requestParams.get("greceiptid"));
                params7.add(requestParams.get("companyid"));
//                delQuery7 = "delete from receiptdetails where goodsReceipt in(select id from goodsreceipt  where id=? and company=?)";
                delQuery7 = "delete rd from receiptdetails rd inner join goodsreceipt gr on rd.goodsReceipt=gr.id where gr.id = ? and gr.company =?";
                int numRows7 = executeSQLUpdate( delQuery7, params7.toArray());


                ArrayList params9 = new ArrayList();
                params9.add(requestParams.get("companyid"));
                params9.add(requestParams.get("greceiptid"));
                String myquery1 = " select journalentry from goodsreceipt where company = ? and id=?";
                List list1 = executeSQLQuery( myquery1, params9.toArray());
                Iterator itr1 = list1.iterator();
                String journalent = "";
                while (itr1.hasNext()) {

                    String jeidi = itr1.next().toString();
                    journalent += "'" + jeidi + "',";
                }
                if (!StringUtil.isNullOrEmpty(journalent)) {
                    journalent = journalent.substring(0, journalent.length() - 1);
                }
                
                String ljeid = "";
                if (isexpenseinv ) {
                    ArrayList lparams10 = new ArrayList();
                    lparams10.add(requestParams.get("companyid"));
                    lparams10.add(requestParams.get("greceiptid"));
                    String myquery10 = " select landedinvoiceje from goodsreceipt where company = ? and id=?";
                    List list10 = executeSQLQuery(myquery10, lparams10.toArray());
                    Iterator litr1 = list10.iterator();
                    while (litr1.hasNext()) {
                        Object obj = litr1.next();
                        if(obj!=null){
                            String jeidi = (String) obj;
                            ljeid += "'" + jeidi + "',";
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(ljeid)) {
                        ljeid = ljeid.substring(0, ljeid.length() - 1);
                    }
                    
                }
                
                ArrayList params1 = new ArrayList();
                params1.add(requestParams.get("companyid"));
                params1.add(requestParams.get("companyid"));
                params1.add(requestParams.get("greceiptid"));
                delQuery1 = "delete  from accjedetailcustomdata where jedetailId in (select id from jedetail where company = ? and journalEntry in (select journalentry from goodsreceipt where company =? and id = ?))";
                int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());

                ArrayList params10 = new ArrayList();
                params10.add(requestParams.get("companyid"));
                params10.add(requestParams.get("companyid"));
                params10.add(requestParams.get("greceiptid"));
                delQuery8 = "delete  from accjedetailproductcustomdata where jedetailId in (select id from jedetail where company = ? and journalEntry in (select journalentry from goodsreceipt where company =? and id = ?))";
                int numRows8 = executeSQLUpdate( delQuery8, params1.toArray());

                /**
                 * ERM-447
                 * Delete landingcostdetailmapping entries for expense type invoice with landed cost.
                 */
                if (isexpenseinv && requestParams.containsKey("greceiptid")) {
                    params10 = new ArrayList();
                    String myquery10 = "delete from landingcostdetailmapping where expenseinvoiceid=?";
                    params10.add(requestParams.get("greceiptid")!=null?requestParams.get("greceiptid"):"");
                    executeSQLUpdate(myquery10, params10.toArray());
                }
                /**
                 *  Delete GST Fields for India.
                 */
                deleteGstDocHistoryDetails(requestParams.get("greceiptid").toString());
                ArrayList params6 = new ArrayList();
                params6.add(requestParams.get("companyid"));
                params6.add(requestParams.get("greceiptid"));
                delQuery6 = "delete  from goodsreceipt  where company =? and id = ?";
                int numRows6 = executeSQLUpdate( delQuery6, params6.toArray());

                ArrayList params3 = new ArrayList();
                ArrayList params4 = new ArrayList();
                ArrayList params2 = new ArrayList();
                int numRows3=0,numRows4=0,numRows2=0;
                params3.add(requestParams.get("companyid"));
                delQuery3 = "delete from jedetail where company = ? and journalEntry in (" + journalent + ") ";
                delQuery4 = "delete from journalentry where id  in (" + journalent + ")";
                delQuery2 = "delete  from accjecustomdata where journalentryId in (" + journalent + ")";
                if (!StringUtil.isNullOrEmpty(journalent)) {
                    numRows3 = executeSQLUpdate(delQuery3, params3.toArray());
                    numRows4 = executeSQLUpdate(delQuery4, params4.toArray());
                    numRows2 = executeSQLUpdate( delQuery2, params2.toArray());
                }
                if (isexpenseinv && !StringUtil.isNullOrEmpty(ljeid)) {
                    params6 = new ArrayList();
                    params6.add(requestParams.get("companyid"));
                    params6.add(requestParams.get("greceiptid"));
                    delQuery6 = "update goodsreceipt set landedinvoiceje = NULL where company =? and id = ?";
                    executeSQLUpdate(delQuery6, params6.toArray());
                    
                    params1 = new ArrayList();
                    params1.add(requestParams.get("companyid"));
                    delQuery1 = "delete  from accjedetailcustomdata where jedetailId in (select id from jedetail where company = ? and journalEntry in (" + ljeid + ") )";
                    executeSQLUpdate(delQuery1, params1.toArray());

                    params3 = new ArrayList();
                    params3.add(requestParams.get("companyid"));
                    delQuery3 = "delete from jedetail where company = ? and journalEntry in (" + ljeid + ") ";
                    executeSQLUpdate(delQuery3, params3.toArray());

                    delQuery4 = "delete from journalentry where id  in (" + ljeid + ")";
                    executeSQLUpdate(delQuery4);
                }
                
                numtotal = numRows + numRows1 + numRows2 + numRows3 + numRows4 + numRows5 + numRows6 + numRows7 + numRows8 + numRows11 + numRows12;
            }

        } catch (Exception ex) {
                throw new AccountingException(("Cannot delete"+(isFixedAsset?" Acquired Invoice":" Purchase Invoice")+" as its referance child field is not deleted."), ex);
        }
        return new KwlReturnObject(true, "GoodsReceipts has been deleted successfully.", null, null, numtotal);

    }
//

    public KwlReturnObject deleteGoodsReceiptEntry(String grid, String companyid) throws ServiceException,AccountingException {
        //This code used for update the link flag of vendor quotation
        ArrayList updateLinkFlagList = new ArrayList();
        updateLinkFlagList.add(grid);
        String vendorIdQuery = "update vendorquotation as v inner join  vendorquotationdetails as vqd inner join grdetails grd on  v.id=vqd.vendorquotation and grd.vendorquotationdetail=vqd.id set linkflag=0  where grd.goodsreceipt=?";
        int result = executeSQLUpdate( vendorIdQuery, updateLinkFlagList.toArray());
        
        String query = "update GoodsReceipt gr set gr.deleted=true where gr.ID=? and gr.company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{grid, companyid});
        return new KwlReturnObject(true, "Goods Receipt has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteBillingGoodsReceiptEntry(String grid, String companyid) throws ServiceException {
        String query = "update BillingGoodsReceipt gr set gr.deleted=true where gr.ID = ? and gr.company.companyID=?";
//        String query = "update GoodsReceipt gr set gr.deleted=true where gr.ID=? and gr.company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{grid, companyid});
        return new KwlReturnObject(true, "Goods Receipt has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getGRJournalEntry(String grid) throws ServiceException {
        String selQuery = "select gr.journalEntry.ID from GoodsReceipt gr where gr.ID=? and gr.company.companyID=gr.journalEntry.company.companyID";
        List list = executeQuery( selQuery, new Object[]{grid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getGoodsReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from GoodsReceiptDetail";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getGoodsReceiptCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from AccJEDetailCustomData";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getExpensePOCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from ExpensePODetailCustomData";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getBillingGoodsReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from BillingGoodsReceiptDetail";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getExpenseGRDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from ExpenseGRDetail";
        return buildNExecuteQuery( query, requestParams);
    }
    @Override
    public KwlReturnObject getProductsFromGoodReceiptOrder(String billid, String companyid) throws ServiceException {
        String query = "select product from grodetails where grorder = ? and company = ?";
        List list = executeSQLQuery(query, new Object[]{billid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getProductsFromPurchaseReturn(String billid, String companyid) throws ServiceException {
        String query = "select product from prdetails where purchasereturn = ? and company = ?";
        List list = executeSQLQuery(query, new Object[]{billid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getCalculatedGRTax(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList paramslist = new ArrayList();
        String Condition = "";
        String taxid = (String) requestParams.get("taxid");
        paramslist.add(taxid);
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition = " and gr.journalEntry.entryDate >= ? and gr.journalEntry.entryDate <= ?";
            Condition = " and gr.creationDate >= ? and gr.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
        boolean isImportServices = false;// For Malasian Company
        if (requestParams.containsKey("isImportServices") && requestParams.get("isImportServices")!=null) {// For Malasian Company
            isImportServices = (Boolean)requestParams.get("isImportServices");
        }
        
        if (requestParams.containsKey("excludeRetailPurchaseInvoice") && requestParams.get("excludeRetailPurchaseInvoice")!=null) {// For Malasian Company
            boolean excludeRetailPurchaseInvoice = (Boolean) requestParams.get("excludeRetailPurchaseInvoice");
            if(excludeRetailPurchaseInvoice){
                Condition += " and gr.retailPurchase= ? ";
                paramslist.add(false);
            }
        }
        
        Condition += " and gr.importService= ? ";// For Malasian Company
        paramslist.add(isImportServices);// For Malasian Company

		String ss = requestParams.containsKey("ss")?(String) requestParams.get("ss"):"";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"gr.tax.name", "gr.vendor.name", "gr.vendor.acccode","gr.journalEntry.entryNumber", "gr.goodsReceiptNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramslist, ss, 5);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                Condition += searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String mySearchFilterString = "";
        String joinString = "";
        String mySearchFilterString1 = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", "gr.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                joinString = " inner join gr.rows rows";
                mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", "rows.gstJED.accJEDetailCustomData");
            }
            }
        if (requestParams.containsKey(Constants.fixedAssetsPurchaseInvoiceSearchJson) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.fixedAssetsPurchaseInvoiceSearchJson))) {
            requestParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.fixedAssetsPurchaseInvoiceSearchJson));
            requestParams.put(Constants.moduleid, Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId);
            mySearchFilterString1 = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString1.contains("c.accjecustomdata")) {
                mySearchFilterString1 = mySearchFilterString1.replaceAll("c.accjecustomdata", "gr.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString1.contains("c.AccJEDetailCustomData")) {
                joinString = " inner join gr.rows rows";
                mySearchFilterString1 = mySearchFilterString1.replaceAll("c.AccJEDetailCustomData", "rows.gstJED.accJEDetailCustomData");
            }
            mySearchFilterString = StringUtil.combineTwoCustomSearchStrings(mySearchFilterString,mySearchFilterString1);
        }
//            if(StringUtil.isNullOrEmpty(ss)==false){
//               for(int i=0;i<=3;i++){
//                 paramslist.add(ss+"%");
//               }
//                 Condition+= " and (gr.tax.name like ? or gr.vendor.name like ?  or gr.journalEntry.entryNumber like ? or gr.goodsReceiptNumber like ? ) ";
//        }


        String query = "select gr from GoodsReceipt gr " + joinString + " where gr.tax.ID = ? and gr.deleted=false and gr.pendingapproval=0 and gr.istemplate!=2 " + Condition + mySearchFilterString;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }
    public KwlReturnObject getCalculatedVHT(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList paramslist = new ArrayList();
        String Condition = "";
        paramslist.add(requestParams.get("companyid"));
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition = " and gr.journalEntry.entryDate >= ? and gr.journalEntry.entryDate <= ?";
            Condition = " and gr.creationDate >= ? and gr.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
        boolean isImportServices = false;// For Malasian Company
        if (requestParams.containsKey("isImportServices") && requestParams.get("isImportServices") != null) {// For Malasian Company
            isImportServices = (Boolean) requestParams.get("isImportServices");
        }

        if (requestParams.containsKey("excludeRetailPurchaseInvoice") && requestParams.get("excludeRetailPurchaseInvoice") != null) {// For Malasian Company
            boolean excludeRetailPurchaseInvoice = (Boolean) requestParams.get("excludeRetailPurchaseInvoice");
            if (excludeRetailPurchaseInvoice) {
                Condition += " and gr.retailPurchase= ? ";
                paramslist.add(false);
            }
        }

        Condition += " and gr.importService= ? ";// For Malasian Company
        paramslist.add(isImportServices);// For Malasian Company

        String ss = requestParams.containsKey("ss") ? (String) requestParams.get("ss") : "";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"gr.tax.name", "gr.vendor.name", "gr.vendor.acccode", "gr.journalEntry.entryNumber", "gr.goodsReceiptNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramslist, ss, 5);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                Condition += searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String query = "from GoodsReceipt gr where gr.deleted=false and gr.pendingapproval=0 and gr.istemplate!=2 and gr.company.companyID=?" + Condition;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }    
    /*
    * Deprecating this method as it is only used to fetch the old records
    * i.e debitnote of type 1 and 3 created before 22 april 2014 are considered as oldrecord.
    * because creation UI of type 1 and 3 has been change.
    */
    @Deprecated    
    @Override
    public KwlReturnObject getCalculatedDNTax(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList paramslist = new ArrayList();
        String Condition = "";
//        String taxid = (String) requestParams.get("taxid");
//        paramslist.add(taxid);
        if (requestParams.containsKey("companyid") && requestParams.containsKey("companyid")) {
            String companyid = (String) requestParams.get("companyid");
            paramslist.add(companyid);
            Condition = " and dn.company.companyID = ? ";
        }
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition += " and dn.journalEntry.entryDate >= ? and dn.journalEntry.entryDate <= ?";
            Condition += " and dn.creationDate >= ? and dn.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }

		String ss = requestParams.containsKey("ss")?(String) requestParams.get("ss"):"";
         if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"dn.vendor.name","dn.vendor.acccode", "dn.journalEntry.entryNumber", "dn.debitNoteNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramslist, ss, 4);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                Condition += searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String mySearchFilterString = "";
        String joinString = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", "dn.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                joinString = " inner join dn.rows rows ";
                mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", " rows.gstJED.accJEDetailCustomData");
            }
        }
//            if(StringUtil.isNullOrEmpty(ss)==false){
//               for(int i=0;i<=2;i++){
//                 paramslist.add(ss+"%");//grd.tax.name like ? or
//               }
//                 Condition+= " and ( dn.vendor.name like ?  or dn.journalEntry.entryNumber like ? or dn.debitNoteNumber like ? ) ";
//        }

//        gr.tax.ID = ? and 
//        String query = "from DebitNote dn where dn.deleted=false AND dn.normalDN = true AND dn.otherwise = false AND dn.oldRecord = true "+Condition;
        String query = "select dn from DebitNote dn " + joinString + " where dn.deleted=false AND dn.normalDN = true AND dn.approvestatuslevel = 11 AND dn.oldRecord = true " + Condition + mySearchFilterString;        
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }
    
    @Override
    public KwlReturnObject getCalculatedDebitNoteTax(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList paramslist = new ArrayList();
        String Condition = "";
        if (requestParams.containsKey("companyid") && requestParams.containsKey("companyid")) {
            String companyid = (String) requestParams.get("companyid");
            paramslist.add(companyid);
            Condition = " and dn.company.companyID = ? ";
        }
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition += " and dn.journalEntry.entryDate >= ? and dn.journalEntry.entryDate <= ?";
            Condition += " and dn.creationDate >= ? and dn.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }

        String ss = requestParams.containsKey("ss") ? (String) requestParams.get("ss") : "";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"dn.vendor.name", "dn.vendor.acccode", "dn.journalEntry.entryNumber", "dn.debitNoteNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramslist, ss, 4);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                Condition += searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (requestParams.containsKey("taxid")) {
            String taxid = (String) requestParams.get("taxid");
            Condition += " and dn.tax.ID = ? ";
            paramslist.add(taxid);
        }
        String mySearchFilterString = "";
        String joinString = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", "dn.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                joinString = " inner join dn.rowsGst rowgst ";
                mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", " rowgst.gstJED.accJEDetailCustomData");
            }
        }
        String query = "select dn from DebitNote dn "+joinString+" where dn.deleted=false AND dn.normalDN = true AND dn.approvestatuslevel = 11 AND dn.oldRecord = false " + Condition + mySearchFilterString;
        returnlist = executeQuery(query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    @Override
    public KwlReturnObject getCalculatedGRTaxBilling(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        List paramslist = new ArrayList();
        String Condition = "";
        String taxid = (String) requestParams.get("taxid");
        paramslist.add(taxid);
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition = " and gr.journalEntry.entryDate >= ? and gr.journalEntry.entryDate <= ?";
            Condition = " and gr.creationDate >= ? and gr.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
        String query = "from BillingGoodsReceipt gr where gr.tax.ID = ? and gr.deleted=false and gr.pendingapproval=0 and gr.istemplate!=2 " + Condition;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }
    
    /**
     * Description : Method is used to Fetch line tax getCalculatedCNTaxGst
     * @param <requestParams> :-Contains start date,End date ,tax id
     */
    @Override
    public KwlReturnObject getCalculatedCNTaxGst(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList paramslist = new ArrayList();
        String Condition = "";
        String taxid = (String) requestParams.get("taxid");
        paramslist.add(taxid);
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition = " and creditNote.journalEntry.entryDate >= ? and creditNote.journalEntry.entryDate <= ?";
            Condition = " and creditNote.creationDate >= ? and creditNote.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
        String mySearchFilterString = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", " creditNote.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", " jedid.accJEDetailCustomData");
            }
        }
        String query = "from CreditNoteAgainstVendorGst where tax.ID = ?" + Condition + mySearchFilterString;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }
    
    
     /**
     * Description : Method is used to Fetch line tax getCalculatedDNTaxGst
     * @param <requestParams> :-Contains start date,End date ,tax id
     */
    @Override
    public KwlReturnObject getCalculatedDNTaxGst(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList paramslist = new ArrayList();
        String Condition = "";
        String taxid = (String) requestParams.get("taxid");
        paramslist.add(taxid);
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition = " and debitNote.journalEntry.entryDate >= ? and debitNote.journalEntry.entryDate <= ?";
            Condition = " and debitNote.creationDate >= ? and debitNote.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
        String mySearchFilterString = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", " debitNote.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", " jedid.accJEDetailCustomData");
            }
        }
        String query = "from DebitNoteAgainstCustomerGst where tax.ID = ?" + Condition + mySearchFilterString;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    @Override
    public KwlReturnObject getCalculatedGRDtlTax(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList paramslist = new ArrayList();
        String Condition = "";
        String taxid = (String) requestParams.get("taxid");
        paramslist.add(taxid);
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition = " and grd.goodsReceipt.journalEntry.entryDate >= ? and grd.goodsReceipt.journalEntry.entryDate <= ?";
            Condition = " and grd.goodsReceipt.creationDate >= ? and grd.goodsReceipt.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
        
        boolean isImportServices = false;// For Malasian Company
        if (requestParams.containsKey("isImportServices") && requestParams.get("isImportServices") != null) {// For Malasian Company
            isImportServices = (Boolean)requestParams.get("isImportServices");
        }
        
        Condition += " and grd.goodsReceipt.importService= ? ";// For Malasian Company
        paramslist.add(isImportServices);// For Malasian Company
        
        
        if (requestParams.containsKey("excludeRetailPurchaseInvoice") && requestParams.get("excludeRetailPurchaseInvoice")!=null) {// For Malasian Company
            boolean excludeRetailPurchaseInvoice = (Boolean) requestParams.get("excludeRetailPurchaseInvoice");
            if(excludeRetailPurchaseInvoice){
                Condition += " and grd.goodsReceipt.retailPurchase= ? ";
                paramslist.add(false);
            }
        }

	String ss = requestParams.containsKey("ss")?(String) requestParams.get("ss"):"";
        
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"grd.tax.name", "grd.goodsReceipt.vendor.name", "grd.goodsReceipt.vendor.acccode", "grd.goodsReceipt.journalEntry.entryNumber","grd.goodsReceipt.goodsReceiptNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramslist, ss, 5);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                Condition += searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String mySearchFilterString = "";
        String mySearchFilterString1 = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", "grd.goodsReceipt.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", "grd.gstJED.accJEDetailCustomData");
            }
        }
        if (requestParams.containsKey(Constants.fixedAssetsPurchaseInvoiceSearchJson) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.fixedAssetsPurchaseInvoiceSearchJson))) {
            requestParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.fixedAssetsPurchaseInvoiceSearchJson));
            requestParams.put(Constants.moduleid, Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId);
            mySearchFilterString1 = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString1.contains("c.accjecustomdata")) {
                mySearchFilterString1 = mySearchFilterString1.replaceAll("c.accjecustomdata", "grd.goodsReceipt.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString1.contains("c.AccJEDetailCustomData")) {
                mySearchFilterString1 = mySearchFilterString1.replaceAll("c.AccJEDetailCustomData", "grd.gstJED.accJEDetailCustomData");
            }
            mySearchFilterString = StringUtil.combineCustomSearchStrings(mySearchFilterString.replace("and", "AND"),mySearchFilterString1.replace("and", "AND")," or ");
            }
        String query = "from GoodsReceiptDetail grd where grd.tax.ID = ? and grd.goodsReceipt.deleted=false and grd.goodsReceipt.pendingapproval =0 and grd.goodsReceipt.istemplate != 2 " + Condition + mySearchFilterString;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    @Override
    public KwlReturnObject getCalculatedGRDtlTaxBilling(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        List paramslist = new ArrayList();
        String Condition = "";
        String taxid = (String) requestParams.get("taxid");
        paramslist.add(taxid);
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
            Condition = " and grd.billingGoodsReceipt.journalEntry.entryDate >= ? and grd.billingGoodsReceipt.journalEntry.entryDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
        String query = "from BillingGoodsReceiptDetail grd where grd.tax.ID = ? and grd.billingGoodsReceipt.deleted=false and grd.billingGoodsReceipt.pendingapproval=0 and grd.billingGoodsReceipt.istemplate != 2 " + Condition;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    public KwlReturnObject getCalculatedExpenseGRDtlTax(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        String taxid = (String) requestParams.get("taxid");
        String condition = "";
        ArrayList paramslist = new ArrayList();
        paramslist.add(taxid);
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            condition = " and grd.goodsReceipt.journalEntry.entryDate >= ? and grd.goodsReceipt.journalEntry.entryDate <= ?";
            condition = " and grd.goodsReceipt.creationDate >= ? and grd.goodsReceipt.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }

	String ss = requestParams.containsKey("ss")?(String) requestParams.get("ss"):"";
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"grd.tax.name", "grd.goodsReceipt.vendor.name", "grd.goodsReceipt.journalEntry.entryNumber","grd.goodsReceipt.goodsReceiptNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramslist, ss, 4);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        String mySearchFilterString = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", "grd.goodsReceipt.journalEntry.accBillInvCustomData");
            }
            if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", "grd.gstJED.accJEDetailCustomData");
            }
        }
//            if(StringUtil.isNullOrEmpty(ss)==false){
//               for(int i=0;i<=3;i++){
//                 paramslist.add(ss+"%");
//               }
//                 condition+= " and (grd.tax.name like ? or grd.goodsReceipt.vendor.name like ?  or grd.goodsReceipt.journalEntry.entryNumber like ? or grd.goodsReceipt.goodsReceiptNumber like ? ) ";
//        }


        String query = "from ExpenseGRDetail grd where grd.tax.ID = ? and grd.goodsReceipt.deleted=false and grd.goodsReceipt.pendingapproval=0 and grd.goodsReceipt.istemplate != 2 " + condition + mySearchFilterString;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    public KwlReturnObject getGRDiscount(String grid) throws ServiceException {
        String selQuery = "select gr.discount.ID from GoodsReceipt gr where gr.ID=? and gr.company.companyID=gr.discount.company.companyID";
        List list = executeQuery( selQuery, new Object[]{grid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBGRDiscount(String bgrid, String companyid) throws ServiceException {
        String selQuery = "from BillingGoodsReceipt gr where gr.ID = ? and gr.company.companyID = ?";
        //String selQuery = "select gr.discount.ID from GoodsReceipt gr where gr.ID=? and gr.company.companyID=gr.discount.company.companyID";
        List list = executeQuery( selQuery, new Object[]{bgrid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getGRDetailsDiscount(String grid) throws ServiceException {
        String selQuery = "select grd.discount.ID from GoodsReceiptDetail grd where grd.goodsReceipt.ID=? and grd.company.companyID=grd.discount.company.companyID";
        List list = executeQuery( selQuery, new Object[]{grid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBGRDetailsDiscount(String bgrid, String companyid) throws ServiceException {
        String selQuery = "from BillingGoodsReceiptDetail grd where grd.billingGoodsReceipt.ID = ? and grd.company.companyID = ?";
//        String selQuery = "select grd.discount.ID from GoodsReceiptDetail grd where grd.goodsReceipt.ID=? and grd.company.companyID=grd.discount.company.companyID";
        List list = executeQuery( selQuery, new Object[]{bgrid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getGRInventory(String grid) throws ServiceException {
        String selQuery = "select grd.inventory.ID from GoodsReceiptDetail grd where grd.goodsReceipt.ID=? and grd.company.companyID=grd.inventory.company.companyID";
        List list = executeQuery( selQuery, new Object[]{grid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject deleteGoodsReceiptDetails(String receiptid, String companyid) throws ServiceException, AccountingException {
        //Delete Goods Receipt Details
        try {
            ArrayList params1 = new ArrayList();
            params1.add(companyid);
            params1.add(companyid);
            params1.add(receiptid);
            String delQuery1 = "delete  from accjedetailcustomdata where jedetailId in (select id from jedetail where company = ? and journalEntry in (select journalentry from goodsreceipt where company =? and id = ?))";
            int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());
            String delQuery = "delete from GoodsReceiptDetail where goodsReceipt.ID=? and company.companyID=?";
            int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
            return new KwlReturnObject(true, "Goods Receipt Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw new AccountingException("Cannot Edit Vendor Invoice as it is already used in Other Transactions.", ex);//+ex.getMessage(), ex);
        }
    }

    public KwlReturnObject deleteExpenseGridDetails(String receiptid, String companyid) throws ServiceException, AccountingException {
        //Delete Goods Receipt Details
        try {
            String delQuery = "delete from ExpenseGRDetail where goodsReceipt.ID=? and company.companyID=?";
            int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
            return new KwlReturnObject(true, "Goods Receipt Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw new AccountingException("Cannot Edit Vendor Invoice as it is already used in Other Transactions.", ex);//+ex.getMessage(), ex);
        }
    }
    public KwlReturnObject deleteExpenseGridDetailsLanded(String receiptid, String companyid) throws ServiceException, AccountingException {
        //Delete Goods Receipt Details
        try {
            int numRows = 0;
            if (!StringUtil.isNullOrEmpty(receiptid)) {
                ArrayList params12 = new ArrayList();
                params12.add(receiptid);
                /*
                 * Landed cost category manual amount entry deleted
                 */
                String delQuery1 = "delete from LccManualWiseProductAmount where expenseInvoiceid.ID = ?";
                numRows = executeUpdate(delQuery1, params12.toArray());
            }
            return new KwlReturnObject(true, "Goods Receipt Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw new AccountingException("Cannot Edit Vendor Invoice as it is already used in Other Transactions.", ex);//+ex.getMessage(), ex);
        }
    }

    public KwlReturnObject deleteBillingGoodsReceiptDetails(String receiptid, String companyid) throws ServiceException {
        //Delete Goods Receipt Details
        String delQuery = "delete from BillingGoodsReceiptDetail where billingGoodsReceipt.ID=? and company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{receiptid, companyid});
        return new KwlReturnObject(true, "Goods Receipt Details has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getReceiptFromNo(String receiptno, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from GoodsReceipt where goodsReceiptNumber=? and company.companyID=? AND isDraft='F'"; //ERP-41992 (Reference - SDP-13487) - Do not check duplicate in Draft Report. Because Multiple draft records having empty entry no.
        list = executeQuery( q, new Object[]{receiptno, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getReceiptFromSIN(JSONObject reqParams) throws ServiceException {
        List list = new ArrayList();
        String receiptno=reqParams.optString("supplierInvoiceNumber");
        String vendor=reqParams.optString("vendor");
        String companyid=reqParams.optString("companyid");
        String grid=reqParams.optString("grid");
        String q = "select goodsReceiptNumber from GoodsReceipt where supplierinvoiceno=? and vendor.ID=? and company.companyID=? ";
        if(!StringUtil.isNullOrEmpty(grid)){
            q +=" and ID<>" +"'"+ grid +"'";
        }
        list = executeQuery( q, new Object[]{receiptno, vendor, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getReceiptDFromPOD(String podid) throws ServiceException {
        List list = new ArrayList();
        String query = "from GoodsReceiptDetail ge where ge.purchaseorderdetail.ID = ? and ge.goodsReceipt.deleted = false ";
        list = executeQuery( query, podid);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * To Get Expense GR from Purchase Order Details.
     *
     * @param params (Required : Company ID, Expense PO Details ID)
     * @return list of Expense PI Details which is link with Expense PO.
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getExpenseGRDetailFromPOD(Map<Object, Object> params) throws ServiceException {
        List list = new ArrayList();
        String expensePODetail = "", companyid = "";
        if (params.containsKey("companyid")) {
            companyid = params.get("companyid").toString();
        }
        if (params.containsKey("expensePODetail")) {
            expensePODetail = params.get("expensePODetail").toString();
        }
        String query = "from ExpenseGRDetail ge where ge.expensePODetail.ID = ? and ge.goodsReceipt.deleted = false and ge.company.companyID = ?  ";
        list = executeQuery(query, new Object[]{expensePODetail, companyid });
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
     public KwlReturnObject getSGEtDFromPOD(String podid) throws ServiceException {
        List list = new ArrayList();
        String query = "from SecurityGateDetails ge where ge.podetail.ID = ? and ge.securityGateEntry.deleted = false ";
        list = executeQuery( query, podid);
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getGRFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from GoodsReceipt where journalEntry.ID=? and deleted=false and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getGROFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from GoodsReceiptOrder where inventoryJE.ID=? and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getPRFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from PurchaseReturn where inventoryJE.ID=? and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBGRFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from BillingGoodsReceipt where journalEntry.ID=? and deleted=false and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getJEFromGR(String greceiptid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "select gr.journalEntry.ID from GoodsReceipt gr where gr.ID=? and gr.company.companyID=gr.journalEntry.company.companyID";
        list = executeQuery( query, new Object[]{greceiptid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getFromBGR(String jeid, String companyid) throws ServiceException {
        String selQuery = "from BillingGoodsReceipt gr where gr.ID = ? and gr.company.companyID = ?";
//        String selQuery = "from GoodsReceipt where journalEntry.ID=? and deleted=false and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getGoodsReceiptData(Map requestParam) throws ServiceException {
        List al = new ArrayList();
        List list = new ArrayList();
        String query = "from GoodsReceiptDetail grd ";
        String queryParam = "where grd.goodsReceipt.deleted=false";

        Calendar startcal = Calendar.getInstance();
        Calendar endcal = Calendar.getInstance();

        if (requestParam.containsKey(PRODUCTID)) {
            al.add(requestParam.get(PRODUCTID));
            queryParam += (queryParam.length() > 0 ? " and " : " where ") + " inventory.product.ID=? ";
        }
        
        // To add companyid in where clause
        if (requestParam.containsKey(COMPANYID)) {
            al.add(requestParam.get(COMPANYID));
            queryParam += (queryParam.length() > 0 ? " and " : " where ") + " grd.company.companyID=? ";
        }

        if (requestParam.containsKey("stDate")) {
            Date stDate = (Date) requestParam.get("stDate");
            if (stDate != null) {
                startcal.setTime(stDate);
                Date startcalDate = null;
                try {
                    String startcalString = authHandler.getDateOnlyFormat().format(startcal.getTime());
                    startcalDate = authHandler.getDateOnlyFormat().parse(startcalString);

                } catch (ParseException ex) {
                    startcalDate = startcal.getTime();
                    Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SessionExpiredException ex) {
                    Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                al.add(startcalDate);
                queryParam += (queryParam.length() > 0 ? " and " : " where ") + " inventory.updateDate>= ?";
            }
        }

        if (requestParam.containsKey("endDate")) {
            Date endDate = (Date) requestParam.get("endDate");
            if (endDate != null) {
                endcal.setTime(endDate);
                endcal.add(Calendar.DAY_OF_MONTH, -1);
                Date endcalDate = null;
                try {
                    String startcalString = authHandler.getDateOnlyFormat().format(endcal.getTime());
                    endcalDate = authHandler.getDateOnlyFormat().parse(startcalString);

                } catch (ParseException ex) {
                    endcalDate = startcal.getTime();
                    Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SessionExpiredException ex) {
                    endcalDate = startcal.getTime();
                    Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                al.add(endcalDate);
                queryParam += (queryParam.length() > 0 ? " and " : " where ") + " inventory.updateDate<= ? ";
            }
        }
        
        // To add date range  i.e. startdate and endate on journalEntry.entryDate in where clause
        if (requestParam.containsKey(JEstartDate)) {
            Date stDate = (Date) requestParam.get(JEstartDate);
            if (stDate != null) {
                startcal.setTime(stDate);
                Date startcalDate = null;
                try {
                    String startcalString = authHandler.getDateOnlyFormat().format(startcal.getTime());
                    startcalDate = authHandler.getDateOnlyFormat().parse(startcalString);

                } catch (ParseException ex) {
                    startcalDate = startcal.getTime();
                    Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SessionExpiredException ex) {
                    Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                al.add(startcalDate);
//                queryParam += (queryParam.length() > 0 ? " and " : " where ") + " grd.goodsReceipt.journalEntry.entryDate>= ?";
                queryParam += (queryParam.length() > 0 ? " and " : " where ") + " grd.goodsReceipt.creationDate>= ?";
            }
        }
        // To add date range  i.e. startdate and endate on journalEntry.entryDate in where clause
        if (requestParam.containsKey(JEendDate)) {
            Date endDate = (Date) requestParam.get(JEendDate);
            if (endDate != null) {
                endcal.setTime(endDate);
                endcal.add(Calendar.DAY_OF_MONTH, -1);
                Date endcalDate = null;
                try {
                    String startcalString = authHandler.getDateOnlyFormat().format(endcal.getTime());
                    endcalDate = authHandler.getDateOnlyFormat().parse(startcalString);

                } catch (ParseException ex) {
                    endcalDate = startcal.getTime();
                    Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SessionExpiredException ex) {
                    endcalDate = startcal.getTime();
                    Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                al.add(endcalDate);
//                queryParam += (queryParam.length() > 0 ? " and " : " where ") + " grd.goodsReceipt.journalEntry.entryDate<= ? ";
                queryParam += (queryParam.length() > 0 ? " and " : " where ") + " grd.goodsReceipt.creationDate<= ? ";
            }
        }

        if (requestParam.containsKey("costcenterid")) {
            al.add(requestParam.get("costcenterid"));
            queryParam += (queryParam.length() > 0 ? " and " : " where ") + " grd.goodsReceipt.journalEntry.costcenter.ID=? ";
        }
        
        // To add order by clause on column passed under key - ORDERBY_COLUMN
        String orderBy_subQuery = "";
        if (requestParam.containsKey(ORDERBY_COLUMN)) {
            queryParam += " order by "+ requestParam.get(ORDERBY_COLUMN);
        }

        query = query + queryParam + orderBy_subQuery;
        list = executeQuery( query, al.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBRDFromBPOD(String podid) throws ServiceException {
        List list = new ArrayList();
        String query = "from BillingGoodsReceiptDetail ge where ge.purchaseOrderDetail.ID = ? and ge.billingGoodsReceipt.deleted=false";
        list = executeQuery( query, podid);
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject saveBillingGoodsReceipt(Map<String, Object> hm) throws ServiceException {
        List list = new ArrayList();

        BillingGoodsReceipt bgr = null;
        try {
            String receiptid = "";
            if (hm.containsKey(GRID)) {
                receiptid = (String) hm.get(GRID);
            } else {
                receiptid = (String) hm.get(ID);
            }

            if (StringUtil.isNullOrEmpty(receiptid)) {
                bgr = new BillingGoodsReceipt();
            } else {
                bgr = (BillingGoodsReceipt) get(BillingGoodsReceipt.class, receiptid);
            }

            if (hm.containsKey("billingGoodsReceiptNumber")) {
                bgr.setBillingGoodsReceiptNumber((String) hm.get("billingGoodsReceiptNumber"));
            }
            if (hm.containsKey(Constants.SEQFORMAT)) {
                bgr.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) hm.get(Constants.SEQFORMAT)));
            }
            if (hm.containsKey(Constants.SEQNUMBER)) {
                bgr.setSeqnumber(Integer.parseInt(hm.get(Constants.SEQNUMBER).toString()));
            }
            if (hm.containsKey("autoGenerated")) {
                bgr.setAutoGenerated((Boolean) hm.get("autoGenerated"));
            }
            if (hm.containsKey("billFrom")) {
                bgr.setBillFrom((String) hm.get("billFrom"));
            }
            if (hm.containsKey("shipFrom")) {
                bgr.setShipFrom((String) hm.get("shipFrom"));
            }
            if (hm.containsKey("dueDate")) {
                bgr.setDueDate((Date) hm.get("dueDate"));
            }
            if (hm.containsKey("shipDate")) {
                bgr.setShipDate((Date) hm.get("shipDate"));
            }
            if (hm.containsKey(MEMO)) {
                bgr.setMemo((String) hm.get(MEMO));
            }
            if (hm.containsKey("dueDate")) {
                bgr.setDueDate((Date) hm.get("dueDate"));
            }
            if (hm.containsKey("shipvia")) {
                bgr.setShipvia((String) hm.get("shipvia"));
            }
            if (hm.containsKey("fob")) {
                bgr.setFob((String) hm.get("fob"));
            }
            if (hm.containsKey(DISCOUNTID)) {
                Discount discount = hm.get(DISCOUNTID) == null ? null : (Discount) get(Discount.class, (String) hm.get(DISCOUNTID));
                bgr.setDiscount(discount);
            }
            if (hm.containsKey("journalEntryid")) {
                JournalEntry journalEntry = hm.get("journalEntryid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) hm.get("journalEntryid"));
                bgr.setJournalEntry(journalEntry);
            }
            if (hm.containsKey(ROWS)) {
                Set<BillingGoodsReceiptDetail> bgrSet = (Set<BillingGoodsReceiptDetail>) hm.get(ROWS);
                bgr.setRows(bgrSet);
            }
            if (hm.containsKey("vendorEntryid")) {
                JournalEntryDetail vendorEntry = hm.get("vendorEntryid") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("vendorEntryid"));
                bgr.setVendorEntry(vendorEntry);
            }
            if (hm.containsKey("shipEntryid")) {
                JournalEntryDetail shipEntry = hm.get("shipEntryid") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("shipEntryid"));
                bgr.setShipEntry(shipEntry);
            }
            if (hm.containsKey("otherEntryid")) {
                JournalEntryDetail otherEntry = hm.get("otherEntryid") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("otherEntryid"));
                bgr.setOtherEntry(otherEntry);
            }
//            if (hm.containsKey("debtorEntryid")) {
//                JournalEntryDetail debtorEntry = hm.get("debtorEntryid")==null?null:(JournalEntryDetail) get(JournalEntryDetail.class, (String)hm.get("debtorEntryid"));
//                bgr.setDebtorEntry(debtorEntry);
//            }
            if (hm.containsKey("taxEntryid")) {
                JournalEntryDetail taxEntry = hm.get("taxEntryid") == null ? null : (JournalEntryDetail) get(JournalEntryDetail.class, (String) hm.get("taxEntryid"));
                bgr.setTaxEntry(taxEntry);
            }
            if (hm.containsKey("exchangeRateDetailsid")) {
                ExchangeRateDetails exchangeRateDetails = hm.get("exchangeRateDetailsid") == null ? null : (ExchangeRateDetails) get(ExchangeRateDetails.class, (String) hm.get("exchangeRateDetailsid"));
                bgr.setExchangeRateDetail(exchangeRateDetails);
            }
            if (hm.containsKey("externalCurrencyRate")) {
                Double externalCurrencyRate = (Double) hm.get("externalCurrencyRate");
                bgr.setExternalCurrencyRate(externalCurrencyRate);
            }
            if (hm.containsKey(COMPANYID)) {
                Company company = hm.get(COMPANYID) == null ? null : (Company) get(Company.class, (String) hm.get(COMPANYID));
                bgr.setCompany(company);
            }
            if (hm.containsKey(CURRENCYID)) {
                KWLCurrency currency = hm.get(CURRENCYID) == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) hm.get(CURRENCYID));
                bgr.setCurrency(currency);
            }
            if (hm.containsKey(DELETED)) {
                boolean deleted = (Boolean) hm.get(DELETED);
                bgr.setDeleted(deleted);
            }
            if (hm.containsKey(TAXID)) {
                Tax tax = hm.get(TAXID) == null ? null : (Tax) get(Tax.class, (String) hm.get(TAXID));
                bgr.setTax(tax);
            }
            if (hm.containsKey("pendingapproval")) {
                bgr.setPendingapproval((Integer) hm.get("pendingapproval"));
            } else {
                bgr.setPendingapproval(0);
            }
            if (hm.containsKey("istemplate")) {
                bgr.setIstemplate((Integer) hm.get("istemplate"));
            } else {
                bgr.setIstemplate(0);
            }
            if (hm.containsKey("posttext")) {
                bgr.setPostText((String) hm.get("posttext"));
            }
            if (hm.containsKey("approvallevel")) {
                bgr.setApprovallevel((Integer) hm.get("approvallevel"));
            }
            if (hm.containsKey(VENDORID)) {
                Vendor vendor = hm.get(VENDORID) == null ? null : (Vendor) get(Vendor.class, (String) hm.get(VENDORID));
                bgr.setVendor(vendor);
            }

            if (hm.containsKey(MARKED_FAVOURITE)) {
                if (hm.get(MARKED_FAVOURITE) != null) {
                    bgr.setFavourite(Boolean.parseBoolean(hm.get(MARKED_FAVOURITE).toString()));
                }
            }

            if (hm.containsKey("venbilladdress")) {
                if (hm.get("venbilladdress") != null) {
                    bgr.setBillTo((String) hm.get("venbilladdress"));
                }
            }
            if (hm.containsKey("venshipaddress")) {
                if (hm.get("venshipaddress") != null) {
                    bgr.setShipTo((String) hm.get("venshipaddress"));
                }
            }

            bgr.setTemplateid((Projreport_Template) get(Projreport_Template.class, Constants.HEADER_IMAGE_TEMPLATE_ID));



            saveOrUpdate(bgr);
            list.add(bgr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.updateBillingGoodsReceipt : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Billing Goods Receipt has been updated successfully", null, list, list.size());
    }

    //from BillingGoodsReceipt where billingGoodsReceiptNumber=? and company.companyID=?
    public KwlReturnObject getBillingGoodsReceipt(Map requestParam) throws ServiceException {
        boolean deleted = false;
        boolean nondeleted = false;
        boolean isVendorAmountDue = false;
        if (requestParam.containsKey("deleted")) {
            deleted = Boolean.parseBoolean((String) requestParam.get(DELETED));
        }
        if (requestParam.containsKey("nondeleted")) {
            nondeleted = Boolean.parseBoolean((String) requestParam.get("nondeleted"));
        }
        if (requestParam.containsKey("isVendorAmountDue")) {
            isVendorAmountDue = Boolean.parseBoolean((String) requestParam.get("isVendorAmountDue"));
        }
        DateFormat df = (DateFormat) requestParam.get("df");
        String enddate = (String) requestParam.get("enddate");
        List al = new ArrayList();
        List list = new ArrayList();
        String query = " from BillingGoodsReceipt bgr ";
        String queryParam = "";

        if (requestParam.containsKey("billingGoodsReceiptNumber")) {
            al.add(requestParam.get("billingGoodsReceiptNumber"));
            if (queryParam.length() > 0) {
                queryParam += " and ";
            } else {
                queryParam += " where ";
            }
            queryParam += " bgr.billingGoodsReceiptNumber = ? ";
        }
        if (requestParam.containsKey(COMPANYID)) {
            al.add(requestParam.get(COMPANYID));
            if (queryParam.length() > 0) {
                queryParam += " and ";
            } else {
                queryParam += " where ";
            }
            queryParam += " bgr.company.companyID = ? ";
        }
        String jeIds = (String) requestParam.get("jeIds");
        if (!StringUtil.isNullOrEmpty(jeIds)) {
            if (queryParam.length() > 0) {
                queryParam += " and ";
            } else {
                queryParam += " where ";
            }
            queryParam += " journalEntry.ID IN(" + jeIds + ")";
        }
        if (requestParam.containsKey("accid")) {
            al.add(requestParam.get("accid"));
            if (queryParam.length() > 0) {
                queryParam += " and ";
            } else {
                queryParam += " where ";
            }
            if (isVendorAmountDue) {
                al.add(requestParam.get("accid"));
                queryParam += " bgr.vendorEntry.account.ID=?";
                queryParam += " and bgr.vendor.ID = ? ";
            } else {
                queryParam += " bgr.vendor.ID = ? ";
            }
        }
        if (nondeleted) {
            if (queryParam.length() > 0) {
                queryParam += " and ";
            } else {
                queryParam += " where ";
            }
            queryParam += " bgr.deleted=false ";
        } else if (deleted) {
            if (queryParam.length() > 0) {
                queryParam += " and ";
            } else {
                queryParam += " where ";
            }
            queryParam += " bgr.deleted=true ";
        }
        if (!StringUtil.isNullOrEmpty(enddate)) { //condition for account reevaluation up to selected date
            try {
                queryParam += " and bgr.journalEntry.entryDate <=? ";
                al.add(df.parse(enddate));
            } catch (ParseException ex) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        query = query + queryParam;
        list = executeQuery( query, al.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBillingGoodsReceiptsData(Map<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        try {

            String companyid = (String) request.get(COMPANYID);
            //String currencyid = (String) request.get("gcurrencyid");
            DateFormat df = (DateFormat) request.get("dateformat");

//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) get(CompanyAccountPreferences.class, companyid);
            String vendorid = (String) request.get(VENDORID);
            vendorid = vendorid == null ? (String) request.get("accid") : vendorid;
//            String vendorid =vendid==null?request.getParameter("accid"):vendid;
            String ss = (String) request.get("ss");
            String cashAccount = pref.getCashAccount().getID();
            boolean cashonly = false;
            boolean creditonly = false;
            boolean isLifoFifo = false;
            if (request.containsKey("isLifoFifo") && request.get("isLifoFifo") != null && request.get("isLifoFifo") != "") {
                isLifoFifo = Boolean.parseBoolean((String) request.get("isLifoFifo"));
            }
            cashonly = Boolean.parseBoolean((String) request.get("cashonly"));
            creditonly = Boolean.parseBoolean((String) request.get("creditonly"));
            boolean deleted = Boolean.parseBoolean((String) request.get(DELETED));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
//            cashonly =Boolean.parseBoolean(request.getParameter("cashonly"));
//            creditonly =Boolean.parseBoolean(request.getParameter("creditonly"));
            if (cashonly) {
                vendorid = cashAccount;
            }

//            boolean ignoreZero = request.getParameter("ignorezero") != null;
//            String dueDate = request.getParameter("curdate");
            String dueDate = (String) request.get("curdate");
            ArrayList params = new ArrayList();
            String condition = "";
            params.add(companyid);

            String billID = (String) request.get("billid");
            if (!StringUtil.isNullOrEmpty(billID)) {
                params.add(billID);
                condition += " and inv.ID=?";
            } else {
                if (!StringUtil.isNullOrEmpty(vendorid)) {
                    params.add(vendorid);
                    condition += " and inv.vendorEntry.account.ID=?";
                } else {
                    String q = "select ID from Vendor where company.companyID=?";
                    Iterator itrcust = executeQuery( q, new Object[]{companyid}).iterator();
                    String qMarks = "";
                    if (!creditonly) {
                        qMarks = "?,";
                        params.add(cashAccount);
                    }
                    while (itrcust.hasNext()) {
                        qMarks += "?,";
                        params.add(itrcust.next());
                    }
                    qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
                    if (!StringUtil.isNullOrEmpty(qMarks)) {
                        condition += " and inv.vendorEntry.account.ID in (" + qMarks + ")";
                    }
                }
            }

            if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
                condition += " and inv.currency.currencyID = ?";
                params.add(currencyfilterfortrans);
            }

            if (!StringUtil.isNullOrEmpty(dueDate)) {
                params.add(df.parse(dueDate));
                condition += " and inv.dueDate<=?";
            }
            String costCenterId = (String) request.get("costCenterId");
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and inv.journalEntry.costcenter.ID=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (inv.journalEntry.entryDate >=? and inv.journalEntry.entryDate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if (StringUtil.isNullOrEmpty(ss) == false) {
                for (int i = 0; i <= 4; i++) {
                    params.add(ss + "%");
                }
                condition += " and (inv.billingGoodsReceiptNumber like ? or inv.billFrom like ?  or inv.journalEntry.entryNumber like ? or inv.memo like ? or inv.vendorEntry.account.name like ? ) ";
            }
            if (nondeleted) {
                condition += " and inv.deleted=false ";
            } else if (deleted) {
                condition += " and inv.deleted=true ";
            }

            condition += " and inv.pendingapproval = 0 and inv.istemplate != 2 ";

            String orderSubQuery = " order by inv.ID";
            if (request.containsKey("direction") && request.get("direction") != null && isLifoFifo) {
                orderSubQuery = " order by inv.journalEntry.entryDate " + request.get("direction").toString() + ", inv.ID " + request.get("direction").toString();
            }

            String query = "from BillingGoodsReceipt inv where inv.company.companyID=? " + condition + orderSubQuery;//" order by inv.ID";
            list = executeQuery( query, params.toArray());

        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getAmtromBPD(String receiptId) throws ServiceException {
        String selQuery = "select sum(amount) from BillingPaymentDetail pd where pd.billingPayment.deleted=false and pd.billingGoodsReceipt.ID=? group by pd.billingGoodsReceipt";
        List list = executeQuery( selQuery, new Object[]{receiptId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getRevalFlag(String receiptId) throws ServiceException { //checked wherther invoice is present or not
        String selQuery = "select count(rh.invoiceid) from RevaluationHistory rh where rh.invoiceid= ? and rh.deleted=false and rh.issaveeval = 1";
        List list = executeQuery( selQuery, new Object[]{receiptId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
//    public KwlReturnObject getQtyandUnitCost(String productid, Date endDate) throws ServiceException {
//    	try{
//    	    String selQuery = "select grd.inventory.quantity, rate, grd.inventory.updateDate from GoodsReceiptDetail grd where grd.inventory.product.ID=? and grd.inventory.updateDate<=? and grd.inventory.carryIn=true and grd.inventory.newInv=false";
//    	    List list = executeQuery( selQuery, new Object[]{productid, endDate});
//    	    return new KwlReturnObject(true, "Rate and Quantity for the product", null, list, list.size());
//    	}catch(Exception ex){
//    		System.out.print(ex);
//    		throw ServiceException.FAILURE(ex.getMessage(), ex);
//    	}
//    }

    public KwlReturnObject getGoodsReceipt_Product(Map<String, Object> requestMap) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String productId = "";
        String companyId = "";
        DateFormat df = null;
        boolean isApproved=false;
        String startdate = "";
        String enddate = "";
        if (requestMap.containsKey("productId")) {
            productId = requestMap.get("productId").toString();
            params.add(productId);
        }
        if (requestMap.containsKey("companyId")) {
            companyId = requestMap.get("companyId").toString();
            params.add(companyId);
        }
        if (requestMap.containsKey("df")) {
            try {
                df = (DateFormat) requestMap.get("df");
                if (requestMap.containsKey("startdate")) {
                    startdate = requestMap.get("startdate").toString();
                }
                if (requestMap.containsKey("enddate")) {
                    enddate = requestMap.get("enddate").toString();
                }
//                condition += " and (grd.goodsReceipt.journalEntry.entryDate >=? and grd.goodsReceipt.journalEntry.entryDate <=?)";
                condition += " and (grd.goodsReceipt.creationDate >=? and grd.goodsReceipt.creationDate <=?)";
                params.add(df.parse(startdate));
                params.add(df.parse(enddate));
            } catch (ParseException ex) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (requestMap.containsKey("isApproved")) {
            isApproved = Boolean.parseBoolean(requestMap.get("isApproved").toString());
            if (isApproved) {
                condition += " and grd.goodsReceipt.approvestatuslevel=?";
                params.add(11);
            }
        }
        String q = "from GoodsReceiptDetail grd where inventory.product.ID=? and grd.company.companyID=?"+condition;// and grd.goodsReceipt.deleted=false";
        list = executeQuery( q,params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
// public KwlReturnObject getGoodsReceiptDetails_Product(String productid, String companyid) throws ServiceException {     ////issue 31469 create Vendor Invoice]: While creating a VI when i linked that to a Goods Receipt then some product name are not displayed in the list 
//        List list = new ArrayList();
//        String q = "from GoodsReceiptOrderDetails grd where inventory.product.ID=? and grd.company.companyID=? and grd.grOrder.deleted=false";
//        list = executeQuery( q, new Object[]{productid, companyid});
//        return new KwlReturnObject(true, "", null, list, list.size());
//    }

    public KwlReturnObject getGoodsReceiptByInventoryID(String inventoryid) throws ServiceException {

        List list = new ArrayList();
//        String q = "from GoodsReceipt gr where gr.ID in (select goodsReceipt.ID from GoodsReceiptDetail where inventory.ID=?)";
        String q="select distinct goodsReceipt from GoodsReceiptDetail grd where grd.inventory.ID=? ";
        list = executeQuery( q, new Object[]{inventoryid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getGoodsReceiptFormInventory(String inventoryid) throws ServiceException {

        List list = new ArrayList();
        String q = "select grd from GoodsReceiptDetail grd where grd.inventory.ID=?";
//        String q = "select grd, TaxList.percent from GoodsReceiptDetail grd left join Tax tx on grd.tax = tx.id inner join TaxList on tx.ID = TaxList.tax where grd.inventory.ID=?";
        list = executeQuery( q, new Object[]{inventoryid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getGoodsReceiptOrderFormInventory(String inventoryid) throws ServiceException {

        List list = new ArrayList();
        String q = "select grod from GoodsReceiptOrderDetails grod where grod.inventory.ID=?";
//        String q = "select grd, TaxList.percent from GoodsReceiptDetail grd left join Tax tx on grd.tax = tx.id inner join TaxList on tx.ID = TaxList.tax where grd.inventory.ID=?";
        list = executeQuery( q, new Object[]{inventoryid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getStockAdjustmentFormInventory(String inventoryid) throws ServiceException {

        List list = new ArrayList();
        String q = "select sa from StockAdjustment sa where sa.inventoryRef.ID=?";
        list = executeQuery( q, new Object[]{inventoryid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getPurchaseReturnFormGoodsReceipt(String grid) throws ServiceException {

        List list = new ArrayList();
        String q = "select prd from PurchaseReturnDetail prd where prd.grdetails.ID=?";
//        String q = "select grd, TaxList.percent from GoodsReceiptDetail grd left join Tax tx on grd.tax = tx.id inner join TaxList on tx.ID = TaxList.tax where grd.inventory.ID=?";
        list = executeQuery( q, new Object[]{grid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public double getPurchaseReturnQtyFormGoodsReceipt(String grid) throws ServiceException {
        double returnQuantity=0;
        String q = "Select sum(quantity) from Inventory where ID in (select prd.ID from PurchaseReturnDetail prd where prd.grdetails.ID= ? )";
        List list = executeQuery( q, new Object[]{grid});
         if (list != null && !list.isEmpty() && list.get(0) !=null){
                returnQuantity = (Double) list.get(0);
            }
        return returnQuantity;
    }

    @Override
    public KwlReturnObject getGoodsReceipt_Rate(String inventoryid) throws ServiceException {
        List list = new ArrayList();
        String q = "select distinct grd.goodsReceipt,grd.rate from GoodsReceiptDetail grd where grd.inventory.ID=?";
        list = executeQuery( q, new Object[]{inventoryid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

//    public KwlReturnObject getGoodsReceipt_Currency(String inventoryid) throws ServiceException {
//        
//        List list = new ArrayList();
//        String q = "from GoodsReceipt gr where gr.ID in (select goodsReceipt.ID from GoodsReceiptDetail where inventory.ID=?)";
//        list = executeQuery( q, new Object[]{inventoryid});
//        return new KwlReturnObject(true, "", null, list, list.size());
//    }
    public KwlReturnObject getGR_ProductTaxPercent(String inventoryid) throws ServiceException {

        List list = new ArrayList();
        String q = "select percent from TaxList where tax.ID in (select tax from GoodsReceiptDetail where inventory.ID=?)";
        list = executeQuery( q, new Object[]{inventoryid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

//    public KwlReturnObject getGR_ProductDiscountPercent(String inventoryid) throws ServiceException {
//        
//        List list = new ArrayList();
//        String q = "select discount.discount from GoodsReceiptDetail where inventory.ID=?";
//        list = executeQuery( q, new Object[]{inventoryid});
//        return new KwlReturnObject(true, "", null, list, list.size());
//    }
    public KwlReturnObject getCalculatedGRDtlTaxDistinct(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        List paramslist = new ArrayList();
        String Condition = "";
        String taxid = (String) requestParams.get("taxid");
        paramslist.add(taxid);
        paramslist.add(taxid);
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
//            Condition = " and grd.goodsReceipt.journalEntry.entryDate >= ? and grd.goodsReceipt.journalEntry.entryDate <= ?";
            Condition = " and grd.goodsReceipt.creationDate >= ? and grd.goodsReceipt.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
        String query = "select distinct(grd.goodsReceipt.journalEntry.ID), grd.goodsReceipt.goodsReceiptNumber  from GoodsReceiptDetail grd where (grd.goodsReceipt.tax.ID = ? OR grd.tax.ID = ?) and grd.goodsReceipt.deleted=false" + Condition;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    public KwlReturnObject getCalculatedGRDtlTaxDistinctBilling(Map<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        List paramslist = new ArrayList();
        String Condition = "";
        String taxid = (String) requestParams.get("taxid");
        paramslist.add(taxid);
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
            Condition = " and grd.billingGoodsReceipt.journalEntry.entryDate >= ? and grd.billingGoodsReceipt.journalEntry.entryDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
        String query = "select distinct(grd.billingGoodsReceipt.journalEntry.ID), grd.billingGoodsReceipt.billingGoodsReceiptNumber  from BillingGoodsReceiptDetail grd where grd.tax.ID = ? and grd.billingGoodsReceipt.deleted=false" + Condition;
        returnlist = executeQuery( query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    @Override
    public KwlReturnObject getPendingGRO(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String userid = request.get("userid").toString();
//            String query = "select distinct(level) from multilevelapprovalrule where id in (select ruleid from multilevelapprovalruletargetusers where userid = ?)";
            ArrayList params = new ArrayList();
//            params.add(userid);
//            List flowList = executeSQLQuery( query, params.toArray());
//            
//            String inQuery = "";
//            ArrayList InParams = new ArrayList();
//            Iterator flowITR = flowList.iterator();
//            while (flowITR.hasNext()) {
//                inQuery += ",?";
//                InParams.add(flowITR.next());
//            }
//            params.clear();
//            list = new ArrayList();
//            boolean hasRecord = flowList.size() > 0 ? true : false;
//            if (hasRecord) {
//                inQuery = inQuery.substring(1);
                DateFormat df = (DateFormat) request.get(Constants.df);
                String start = (String) request.get(Constants.start);
                String limit = (String) request.get(Constants.limit);
                String ss = (String) request.get(Constants.ss);
                boolean isfavourite = false;
                if (request.get("isfavourite") != null) {
                    isfavourite = Boolean.parseBoolean((String) request.get("isfavourite"));
                }
                boolean isprinted = false;
                if (request.get(Constants.MARKED_PRINTED) != null) {
                    isprinted = Boolean.parseBoolean((String) request.get(Constants.MARKED_PRINTED));
                }
                //            params.add((String) request.get(Constants.companyKey));

                String companyid = AccountingManager.getFilterInString((String) request.get(Constants.companyKey));
                //String conditionSQL = " where deliveryorder.deleteflag='F' and deliveryorder.company in "+companyid+" ";
                boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
                boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
                
                boolean isgrodetailsJoin = false;
                String productid = "";
                if (request.containsKey(Constants.productid) && request.get(Constants.productid) != null) {
                    productid = (String) request.get(Constants.productid);
                }
                
              String newvendorid = "";
              if (request.containsKey("newvendorid") && request.get("newvendorid") != null) {
                newvendorid = (String) request.get("newvendorid");
               }
                String productCategoryid = "";
                if (request.containsKey(Constants.productCategoryid) && request.get(Constants.productCategoryid) != null) {
                    productCategoryid = (String) request.get(Constants.productCategoryid);
                }
                
                String vendorCategoryid = "";
                if (request.containsKey(Constants.customerCategoryid) && request.get(Constants.customerCategoryid) != null) {
                    vendorCategoryid = (String) request.get(Constants.customerCategoryid);
                }
                
                String userDepartment = "";
                if (request.containsKey("userDepartment") && request.get("userDepartment") != null) {
                    userDepartment = (String) request.get("userDepartment");
                }
                
                String conditionSQL = "";

                if (nondeleted) {
                    conditionSQL = "  where grorder.deleteflag = 'F' and grorder.company in " + companyid + " ";
                } else if (deleted) {
                    conditionSQL += " where grorder.deleteflag = 'T' and grorder.company in " + companyid + " ";
                } else {
                    conditionSQL += " where grorder.company in " + companyid + " ";
                }
                String searchJoin = "";
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"grorder.gronumber", "grorder.memo", "vendor.name", "grodetails.partno", "grodetails.description", "product.name"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 6);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQL += searchQuery;

                    searchJoin = " inner join grodetails on grodetails.grorder = grorder.id "
                            + " inner join product on grodetails.product = product.id ";
                    
                    isgrodetailsJoin = false;
                    //                for (int i = 0; i < 3; i++) {
                    //                    params.add(ss + "%");
                    //                }
                    //                params.add("%" + ss + "%");
                    //                conditionSQL += " and ( deliveryorder.donumber like ? or deliveryorder.memo like ? or customer.name like ? or dodetails.partno like ? )";

                }
                String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
                if (!StringUtil.isNullOrEmpty(costCenterId)) {
                    params.add(costCenterId);
                    conditionSQL += " and costcenter.id = ? ";
                }
                String customerId = (String) request.get(CCConstants.REQ_customerId);
                if (!StringUtil.isNullOrEmpty(customerId)) {
                    params.add(customerId);
                    conditionSQL += " and vendor.id = ? ";
                }
               if (!StringUtil.isNullOrEmpty(newvendorid)) {
                params.add(newvendorid);
                conditionSQL += " and grorder.vendor = ? ";
               }
                if (!StringUtil.isNullOrEmpty(vendorCategoryid) && !StringUtil.equal(vendorCategoryid, "-1") && !StringUtil.equal(vendorCategoryid, "All")) {
                    params.add(vendorCategoryid);
                    conditionSQL += " and vendor.id in (select vendorid from vendorcategorymapping where vendorcategory = ?)  ";
                }

                if (isfavourite) {
                    conditionSQL += " and grorder.favouriteflag = true ";
                }
                if (isprinted) {
                    conditionSQL += " and grorder.printedflag = true ";
                }

                String startDate = (String) request.get(Constants.REQ_startdate);
                String endDate = (String) request.get(Constants.REQ_enddate);
                String joinString = "";
                if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                    conditionSQL += " and (grorder.grorderdate >= ? and grorder.grorderdate <= ?) ";
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
                String joinString1 = " ";
                if (request.containsKey("poid") && request.get("poid") != null) {
                    String poid = (String) request.get("poid");
                    //if(!StringUtil.isNullOrEmpty(soid)){
                    params.add(poid);
                    if (searchJoin.equals("")) {
                        joinString1 = " inner join grodetails on grodetails.grorder = grorder.id ";
                        isgrodetailsJoin = false;
                    }

                    joinString1 += " inner join podetails on podetails.id = grodetails.podetails  "
                            + " inner join purchaseorder on purchaseorder.id = podetails.purchaseorder ";
                    conditionSQL += " and  purchaseorder.id = ? ";

                }
                String appendCase = " and ";
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
                        joinString = " inner join grocutstomdata on grocutstomdata.goodsreceiptorderid = grorder.accgrordercustomdataref ";
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    }
                }
                
                String joinString2 = "";
                if (!StringUtil.isNullOrEmpty(productid)) {
                    if (!isgrodetailsJoin) {
                        joinString2 = " inner join grodetails on grodetails.grorder = grorder.id ";
                        isgrodetailsJoin = true;
                    }
                    params.add(productid);
                    conditionSQL += " and grodetails.product = ? ";
                }

                if (!StringUtil.isNullOrEmpty(productCategoryid)) {
                    if (!isgrodetailsJoin) {
                        joinString2 = " inner join grodetails on grodetails.grorder = grorder.id ";
                        isgrodetailsJoin = true;
                    }
                    params.add(productCategoryid);
                    conditionSQL += " and grodetails.product in (select productid from productcategorymapping where productcategory = ?) ";
                }
                
                if (!StringUtil.isNullOrEmpty(userDepartment)) {
                    joinString += " inner join users on users.userid = grorder.createdby ";
                    conditionSQL += " and users.department = ? ";
                    params.add(userDepartment);
                }
                
                String orderBy = "";
                String[] stringSort = null;
                String sort_Col = "";
                if (request.containsKey("dir") && request.containsKey("sort")) {
                    String Col_Name = request.get("sort").toString();
                    String Col_Dir = request.get("dir").toString();
                    stringSort = columSortGoodsReceipt(Col_Name, Col_Dir);
                    orderBy += stringSort[0];
                    sort_Col += stringSort[1];

                } else {
                    orderBy += " order by grorderdate desc";
                    sort_Col += " ,grorder.grorderdate ";

                }
                mySearchFilterString += " and approvestatuslevel != ? ";
                params.add(11);

//                mySearchFilterString += " and approvestatuslevel in (" + inQuery + ") ";
//                params.addAll(InParams);

                String mysqlQuery = " select distinct(grorder.id), 'false' as withoutinventory" + sort_Col + "  from grorder "
                        + "inner join vendor on vendor.id = grorder.vendor " + joinString1 + searchJoin + joinString + joinString2
                        + "left join costcenter on costcenter.id = grorder.costcenter " + conditionSQL + mySearchFilterString
                        + orderBy;

                list = executeSQLQuery( mysqlQuery, params.toArray());
                count = list.size();
                if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                    list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                }
//            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptDAOImpl.getGoodsReceiptorders:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject saveExciseTemplateMapping(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try{
            ExciseDetailsTemplateMap exciseTempMap=new ExciseDetailsTemplateMap();
            
//            if (requestParams.containsKey("manufacturertype")) {
//                exciseTempMap.setManufacturerType((String) requestParams.get("manufacturertype"));
//            }
            if (requestParams.containsKey("registrationtype")) {
                exciseTempMap.setRegistrationType((String) requestParams.get("registrationtype"));
            }
            if (requestParams.containsKey("unitname")) {
                exciseTempMap.setUnitname((String) requestParams.get("unitname"));
            }
            if (requestParams.containsKey("eccnumber")) {
                exciseTempMap.setECCNo((String) requestParams.get("eccnumber"));
            }
//            if (requestParams.containsKey("warehouseid")) {
//                exciseTempMap.setWarehouseid((String) requestParams.get("warehouseid"));
//            }
            if (requestParams.containsKey("companyid")) {
                exciseTempMap.setCompanyid((Company) requestParams.get("companyid"));
            }
            
            save(exciseTempMap);
            list.add(exciseTempMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveExciseTemplateMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Excise Details with Invoice Template mapped successfully.", null, null, list.size());
    }
    
    @Override
    public KwlReturnObject approvePendingGRO(String groID, String companyid, int status) throws ServiceException {
        String query = " update GoodsReceiptOrder set approvestatuslevel = ? where ID = ? and company.companyID = ? ";
        int numRows = executeUpdate( query, new Object[]{status, groID, companyid});
        return new KwlReturnObject(true, "Goods Receipt has been updated successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject rejectPendingGRO(String poid, String companyid) throws ServiceException {
        try {
            String query = "update GoodsReceiptOrder set deleted = true, approvestatuslevel = (-approvestatuslevel) where ID = ? and company.companyID = ? ";
            int numRows = executeUpdate( query, new Object[]{poid, companyid});
            return new KwlReturnObject(true, "Goods Receipt Order has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.rejectPendingGRO : " + ex.getMessage(), ex);
        }
    }
    public KwlReturnObject rejectPendingGR(String grID, String companyid) throws ServiceException{
        try {
            String query = "update GoodsReceipt set deleted = true, approvestatuslevel = (-approvestatuslevel) where ID = ? and company.companyID = ? ";
            int numRows = executeUpdate( query, new Object[]{grID, companyid});
            return new KwlReturnObject(true, "Purchase Invoice has been rejected successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.rejectPendingGR : " + ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject ApproveGROForProductIDandSRno(String pendingDOid, String companyid) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(pendingDOid);
        params.add(companyid);
        String query = " update grorder gro "
                + " inner join grodetails grod on grod.grorder = gro.id "
                + " set gro.approvestatuslevel = 11 "
                + " where gro.id = ? and gro.company = ? and gro.deleteflag = 'F' ";
        int numRows = executeSQLUpdate( query, params.toArray());

        return new KwlReturnObject(true, "Goods Receipt has been updated successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject getpendingGROProductIDandSRResult(String pendingDOid, String companyid) throws ServiceException {
        List returnList;
        int totalCount;
        ArrayList params = new ArrayList();
        params.add(pendingDOid);
        params.add(companyid);

        String mysqlQuery = " select bsr.name, p.productid from batchserial bsr "
                + " inner join grodetails grod on grod.batch = bsr.batch "
                + " inner join product p on p.id = grod.product "
                + " inner join grorder gro on gro.id = grod.grorder "
                + " where gro.id = ? and gro.company = ? and gro.deleteflag = 'F' ";
        returnList = executeSQLQuery( mysqlQuery, params.toArray());
        totalCount = returnList.size();

        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getGoodsReceiptOrderCount(String orderno, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from GoodsReceiptOrder where goodsReceiptOrderNumber=? and company.companyID=?";
        list = executeQuery( q, new Object[]{orderno, companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getGoodsReceiptOrderInventory(String doid) throws ServiceException {
        List list = new ArrayList();
        String query = "select invd.inventory.ID from GoodsReceiptOrderDetails invd where invd.grOrder.ID=? and invd.company.companyID=invd.inventory.company.companyID";
        list = executeQuery( query, new Object[]{doid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getGoodsReceiptOrderBatches(String doid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "select invd.batch.id from GoodsReceiptOrderDetails invd where invd.grOrder.ID=? and invd.company.companyID=?";
        list = executeQuery( query, new Object[]{doid, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
   
    @Override
    public KwlReturnObject getGRNDetailsFromPR(String doid, String companyid) throws ServiceException {  //function to check goods receipts used in purchase return
        List list = new ArrayList();
        String query = "from PurchaseReturnDetail sr  where sr.videtails.goodsReceipt.ID=? and sr.purchaseReturn.deleted=false and sr.company.companyID=?";
        list = executeQuery( query, new Object[]{doid, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject deleteGoodsReceiptOrderDetails(String doid, String companyid) throws ServiceException, AccountingException {
        try {
            ArrayList params8 = new ArrayList();
            params8.add(doid);
            params8.add(companyid);
//            String myquery = " select id from grodetails where grorder in (select id from grorder where id=? and company = ?) ";
            String myquery = "select grod.id from grodetails grod inner join grorder gro on grod.grorder=gro.id where gro.id=? and gro.company = ? ";
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
            /**
             *  Delete GST Fields for India.
             */
            deleteGstTaxClassDetails(idStrings);
            ArrayList params1 = new ArrayList();
            String deletecustomdetails = "delete  from grodetailscustomdata where grodetailsid in (" + idStrings + ")";
            int numRows1 = executeSQLUpdate( deletecustomdetails, params1.toArray());
            String delQuery = "delete from GoodsReceiptOrderDetails dod where dod.grOrder.ID=? and dod.company.companyID=?";
            int numRows = executeUpdate( delQuery, new Object[]{doid, companyid});
            return new KwlReturnObject(true, "Goods Receipt Order Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            // throw ServiceException.FAILURE("Cannot Edit Goods Receipt as it is already used in other Transaction.", ex);//+ex.getMessage(), ex);
            throw new AccountingException("Cannot Edit Goods Receipt as it is already used in other Transaction.", ex);
        }
    }
    
    public KwlReturnObject getGROFromVInvoices(String invoiceId, String CompanyId) throws ServiceException {
        String selQuery = "";
        selQuery = "select GR.grOrder.goodsReceiptOrderNumber,GR.grOrder.ID,GR.grOrder.seqformat.ID , GR.grOrder.autoGenerated from GoodsReceiptOrderDetails GR where GR.videtails.goodsReceipt.ID=? and GR.grOrder.deleted=false and GR.company.companyID = ?";
        List list = executeQuery( selQuery, new Object[]{invoiceId, CompanyId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getAutoGeneratedGROFromVInvoices(String invoiceId, String CompanyId) throws ServiceException {
        String selQuery = "";
        selQuery = "select GR.grOrder.goodsReceiptOrderNumber,GR.grOrder.ID,GR.grOrder.seqformat.ID , GR.grOrder.autoGenerated from GoodsReceiptOrderDetails GR where GR.videtails.goodsReceipt.ID=? and GR.grOrder.deleted=false and GR.grOrder.isAutoGeneratedGRO=true and GR.grOrder.approvestatuslevel !=11 and GR.company.companyID = ?";
        List list = executeQuery( selQuery, new Object[]{invoiceId, CompanyId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject saveGoodsReceiptOrder(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String doid = (String) dataMap.get("id");

            GoodsReceiptOrder grOrder = new GoodsReceiptOrder();
            if (StringUtil.isNullOrEmpty(doid)) {
                grOrder.setDeleted(false);
                if (dataMap.containsKey("createdby")) {
                    User createdby = dataMap.get("createdby") == null ? null : (User) get(User.class, (String) dataMap.get("createdby"));
                    grOrder.setCreatedby(createdby);
                }
                if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    grOrder.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("createdon")) {
                    grOrder.setCreatedon((Long) dataMap.get("createdon"));
                }
                if (dataMap.containsKey("updatedon")) {
                    grOrder.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            } else {
                grOrder = (GoodsReceiptOrder) get(GoodsReceiptOrder.class, doid);
                if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    grOrder.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("updatedon")) {

                    grOrder.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            }
            if (dataMap.containsKey(Constants.SEQFORMAT)) {
                grOrder.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) dataMap.get(Constants.SEQFORMAT)));
            }
            if (dataMap.containsKey(Constants.SEQNUMBER)) {
                grOrder.setSeqnumber(Integer.parseInt(dataMap.get(Constants.SEQNUMBER).toString()));
            }
            if (dataMap.containsKey(Constants.DATEPREFIX) && dataMap.get(Constants.DATEPREFIX) != null) {
                grOrder.setDatePreffixValue((String) dataMap.get(Constants.DATEPREFIX));
            }
            if (dataMap.containsKey(Constants.DATEAFTERPREFIX) && dataMap.get(Constants.DATEAFTERPREFIX) != null) {
                grOrder.setDateAfterPreffixValue((String) dataMap.get(Constants.DATEAFTERPREFIX));
            }
            if (dataMap.containsKey(Constants.DATESUFFIX) && dataMap.get(Constants.DATESUFFIX) != null) {
                grOrder.setDateSuffixValue((String) dataMap.get(Constants.DATESUFFIX));
            }
            if (dataMap.containsKey("entrynumber")) {
                grOrder.setGoodsReceiptOrderNumber((String) dataMap.get("entrynumber"));
            }
            if (dataMap.containsKey("autogenerated")) {
                grOrder.setAutoGenerated((Boolean) dataMap.get("autogenerated"));
            }
            if (dataMap.containsKey("externalCurrencyRate")) {
                grOrder.setExternalCurrencyRate((Double) dataMap.get("externalCurrencyRate"));
            }
            if (dataMap.containsKey("isautogeneratedgro")) {
                grOrder.setIsAutoGeneratedGRO((Boolean) dataMap.get("isautogeneratedgro"));
            }
            if (dataMap.containsKey("memo")) {
                grOrder.setMemo((String) dataMap.get("memo"));
            }
            if (dataMap.containsKey("agent")) {
                if (dataMap.get("agent") != null && dataMap.get("agent") != "") {
                    grOrder.setMasterAgent((MasterItem) get(MasterItem.class, (String) dataMap.get("agent")));
                } else {
                    grOrder.setMasterAgent(null);
                }
            }
            if (dataMap.containsKey("posttext")) {
                grOrder.setPostText((String) dataMap.get("posttext"));
            }
            if (dataMap.containsKey("vendorid")) {
                Vendor vendor = dataMap.get("vendorid") == null ? null : (Vendor) get(Vendor.class, (String) dataMap.get("vendorid"));
                grOrder.setVendor(vendor);
            }
            if (dataMap.containsKey("orderdate")) {
                grOrder.setOrderDate((Date) dataMap.get("orderdate"));
            }
            if (dataMap.containsKey("shipdate")) {
                grOrder.setShipdate((Date) dataMap.get("shipdate"));
            }
            if (dataMap.containsKey("shipvia")) {
                grOrder.setShipvia((String) dataMap.get("shipvia"));
            }
            if (dataMap.containsKey("fob")) {
                grOrder.setFob((String) dataMap.get("fob"));
            }
            if (dataMap.containsKey(TAXID)) {
                Tax tax = dataMap.get(TAXID) == null ? null : (Tax) get(Tax.class, (String) dataMap.get(TAXID));
                grOrder.setTax(tax);
            }
            if (dataMap.containsKey("permitNumber") && dataMap.get("permitNumber") != null) {
                grOrder.setPermitNumber((String) dataMap.get("permitNumber"));
            }
            if (dataMap.containsKey("status")) {
                MasterItem masterItem = dataMap.get("status") == null ? null : (MasterItem) get(MasterItem.class, (String) dataMap.get("status"));
                grOrder.setStatus(masterItem);
            }
            if (dataMap.containsKey("costCenterId")) {
                CostCenter costCenter = dataMap.get("costCenterId") == null ? null : (CostCenter) get(CostCenter.class, (String) dataMap.get("costCenterId"));
                grOrder.setCostcenter(costCenter);
            } else {
                grOrder.setCostcenter(null);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                grOrder.setCompany(company);
            }

            if (dataMap.containsKey("isfavourite")) {
                if (dataMap.get("isfavourite") != null) {
                    grOrder.setFavourite(Boolean.parseBoolean(dataMap.get("isfavourite").toString()));
                }
            }
            if (dataMap.containsKey("isJobWorkOutOrder") && dataMap.get("isJobWorkOutOrder") != null) {
                grOrder.setIsJobWorkOutOrder(Boolean.parseBoolean(dataMap.get("isJobWorkOutOrder").toString()));
            }
            if (dataMap.containsKey("isFixedAsset")) {
                grOrder.setFixedAssetGRO((Boolean) dataMap.get("isFixedAsset"));
            }
             if (dataMap.containsKey("isConsignment")) {
                grOrder.setIsconsignment((Boolean) dataMap.get("isConsignment"));
            }

            if (dataMap.containsKey("approvestatuslevel") && dataMap.get("approvestatuslevel") !=null) {
                grOrder.setApprovestatuslevel((Integer) dataMap.get("approvestatuslevel"));
            }
            
            if (dataMap.containsKey(Constants.IsRoundingAdjustmentApplied) && dataMap.get(Constants.IsRoundingAdjustmentApplied) != null) {
                boolean isRoundingAdjustmentApplied = Boolean.parseBoolean(dataMap.get(Constants.IsRoundingAdjustmentApplied).toString());
                grOrder.setIsRoundingAdjustmentApplied(isRoundingAdjustmentApplied);
            }
            
            if (dataMap.containsKey(Constants.roundingadjustmentamountinbase) && dataMap.get(Constants.roundingadjustmentamountinbase) != null) {
                grOrder.setRoundingadjustmentamountinbase(Double.valueOf(dataMap.get(Constants.roundingadjustmentamountinbase).toString()));
            }

            if (dataMap.containsKey(Constants.roundingadjustmentamount) && dataMap.get(Constants.roundingadjustmentamount) != null) {
                grOrder.setRoundingadjustmentamount(Double.valueOf(dataMap.get(Constants.roundingadjustmentamount).toString()));
            }

            if (dataMap.containsKey("challannumber") && dataMap.get("challannumber") !=null) {
                grOrder.setChallanNumber((String) dataMap.get("challannumber"));
            }
            if (dataMap.containsKey("formtype") && dataMap.get("formtype") !=null) {
                grOrder.setFormtype((String) dataMap.get("formtype"));
            }

            if (dataMap.containsKey("dodetails")) {
                if (dataMap.get("dodetails") != null) {
                    grOrder.setRows((Set<GoodsReceiptOrderDetails>) dataMap.get("dodetails"));
                }
            }

            if (dataMap.containsKey(MARKED_PRINTED)) {
                if (dataMap.get(MARKED_PRINTED) != null) {
                    grOrder.setPrinted(Boolean.parseBoolean(dataMap.get(MARKED_PRINTED).toString()));
                }
            }

            if (dataMap.containsKey("billshipAddressid") && !StringUtil.isNullOrEmpty((String)dataMap.get("billshipAddressid"))) {
                grOrder.setBillingShippingAddresses((BillingShippingAddresses) get(BillingShippingAddresses.class, (String) dataMap.get("billshipAddressid")));               
            }
            if (dataMap.containsKey("currencyid") && dataMap.get("currencyid") != null) {
                grOrder.setCurrency((KWLCurrency) get(KWLCurrency.class, (String) dataMap.get("currencyid")));
            }
            if (dataMap.containsKey("termid") && dataMap.get("termid")!=null && dataMap.get("termid").toString()!="") {
                grOrder.setTerm((Term) get(Term.class, (String) dataMap.get("termid")));
            }
            if (dataMap.containsKey("inventoryjeid") && dataMap.get("inventoryjeid") != null && dataMap.get("inventoryjeid").toString() != "") {
                grOrder.setInventoryJE((JournalEntry) get(JournalEntry.class, (String) dataMap.get("inventoryjeid")));
            }
            if (dataMap.containsKey("gstIncluded") && dataMap.get("gstIncluded") !=null) {
                grOrder.setGstIncluded((Boolean) dataMap.get("gstIncluded"));
            }
            if (dataMap.containsKey(Constants.SUPPLIERINVOICENO) && !StringUtil.isNullOrEmpty((String) dataMap.get(Constants.SUPPLIERINVOICENO))) {
                grOrder.setSupplierInvoiceNo((String) dataMap.get(Constants.SUPPLIERINVOICENO));
            }
            if (dataMap.containsKey(Constants.isApplyTaxToTerms) && dataMap.get(Constants.isApplyTaxToTerms) != null) {  // If Save As Draft
                grOrder.setApplyTaxToTerms((Boolean) dataMap.get(Constants.isApplyTaxToTerms));
            }
            if (dataMap.containsKey("gstapplicable") && dataMap.get("gstapplicable") != null) {  // If New GST Appliled
                grOrder.setIsIndGSTApplied((Boolean) dataMap.get("gstapplicable"));
            }
            if (dataMap.containsKey("rcmApplicable") && dataMap.get("rcmApplicable") != null) {
                grOrder.setRcmApplicable((Boolean) dataMap.get("rcmApplicable"));
            }
            if (dataMap.containsKey(Constants.EWAYApplicable) && dataMap.get(Constants.EWAYApplicable) != null) {  // If New GST Appliled
                grOrder.setEwayapplicable((Boolean) dataMap.get(Constants.EWAYApplicable));
            }
            if (dataMap.containsKey(Constants.isMerchantExporter) && dataMap.get(Constants.isMerchantExporter) != null) {
                grOrder.setIsMerchantExporter((Boolean) dataMap.get(Constants.isMerchantExporter));
            }
            grOrder.setTemplateid((Projreport_Template) get(Projreport_Template.class, Constants.HEADER_IMAGE_TEMPLATE_ID));
            saveOrUpdate(grOrder);
            list.add(grOrder);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveDeliveryOrder : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject saveGoodsReceiptOrderDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String dodid = (String) dataMap.get("id");

            GoodsReceiptOrderDetails groDetail = new GoodsReceiptOrderDetails();
            if (!StringUtil.isNullOrEmpty(dodid)) {
                groDetail = (GoodsReceiptOrderDetails) get(GoodsReceiptOrderDetails.class, dodid);
            }
            
            if (dataMap.containsKey("doid")) {
                GoodsReceiptOrder grOrder = dataMap.get("doid") == null ? null : (GoodsReceiptOrder) get(GoodsReceiptOrder.class, (String) dataMap.get("doid"));
                groDetail.setGrOrder(grOrder);
            }
            if (dataMap.containsKey("srno")) {
                groDetail.setSrno((Integer) dataMap.get("srno"));
            }
            if (dataMap.containsKey("quantity")) {
                groDetail.setActualQuantity((Double) dataMap.get("quantity"));
            }
            if (dataMap.containsKey("deliveredquantity")) {
                groDetail.setDeliveredQuantity((Double) dataMap.get("deliveredquantity"));
            }
            if (dataMap.containsKey("uomid")) {
                groDetail.setUom((UnitOfMeasure) get(UnitOfMeasure.class, dataMap.get("uomid").toString()));
            }
            if (dataMap.containsKey("baseuomrate") && dataMap.get("baseuomrate") != null && dataMap.get("baseuomrate") != "") {
                groDetail.setBaseuomrate((Double) dataMap.get("baseuomrate"));
//            } else {
//                groDetail.setBaseuomrate(1);
            }
            if (dataMap.containsKey("baseuomquantity") && dataMap.get("baseuomquantity") != null && dataMap.get("baseuomquantity") != "") {
                groDetail.setBaseuomquantity((Double) dataMap.get("baseuomquantity"));
//            } else {
//                if (dataMap.containsKey("quantity")) {
//                    groDetail.setBaseuomquantity((Double) dataMap.get("quantity"));
//                }
            }
            if (dataMap.containsKey("baseuomdeliveredquantity") && dataMap.get("baseuomdeliveredquantity") != null && dataMap.get("baseuomdeliveredquantity") != "") {
                groDetail.setBaseuomdeliveredquantity((Double) dataMap.get("baseuomdeliveredquantity"));
//            } else {
//                if (dataMap.containsKey("deliveredquantity")) {
//                    groDetail.setBaseuomquantity((Double) dataMap.get("deliveredquantity"));
//                }
            }
            if (dataMap.containsKey("remark")) {
                groDetail.setRemark(StringUtil.DecodeText(StringUtil.isNullOrEmpty((String) dataMap.get("remark")) ? "" : (String) dataMap.get("remark")));
            }
            if (dataMap.containsKey("shelfLocation")) {
                groDetail.setShelfLocation((String) dataMap.get("shelfLocation"));
            }
            if (dataMap.containsKey("description")) {
                groDetail.setDescription((String) dataMap.get("description"));
            }
            if (dataMap.containsKey("productid")) {
                Product product = dataMap.get("productid") == null ? null : (Product) get(Product.class, (String) dataMap.get("productid"));
                groDetail.setProduct(product);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                groDetail.setCompany(company);
            }

            if (dataMap.containsKey("supplierpartnumber")) {
                groDetail.setSupplierpartnumber((String) dataMap.get("supplierpartnumber"));
            }

            if (dataMap.containsKey("GoodsReceiptDetail")) {
                groDetail.setVidetails((GoodsReceiptDetail) dataMap.get("GoodsReceiptDetail"));
            }
            if (dataMap.containsKey("sourcedeliveryorderdetailsid") && dataMap.get("sourcedeliveryorderdetailsid") != null) {
                groDetail.setSourceDeliveryOrderDetailid((String) dataMap.get("sourcedeliveryorderdetailsid"));
            }

            if (dataMap.containsKey("PurchaseOrderDetail")) {
                groDetail.setPodetails((PurchaseOrderDetail) dataMap.get("PurchaseOrderDetail"));
            }
            
            if (dataMap.containsKey("securityGateDetail")) {
                groDetail.setSecuritydetails((SecurityGateDetails) dataMap.get("securityGateDetail"));
            }

            if (dataMap.containsKey("prtaxid") && dataMap.get("prtaxid") != null) {
                Tax tax = dataMap.get("prtaxid") == null ? null : (Tax) get(Tax.class, (String) dataMap.get("prtaxid"));
                groDetail.setTax(tax);
            }

            if (dataMap.containsKey("taxamount") && dataMap.get("taxamount") != null) {
                groDetail.setRowTaxAmount((Double) dataMap.get("taxamount"));
            }
            if (dataMap.containsKey("recTermAmount") && !StringUtil.isNullOrEmpty(dataMap.get("recTermAmount").toString()) ) {
                groDetail.setRowTermAmount(Double.parseDouble(dataMap.get("recTermAmount").toString()));
            }
            if (dataMap.containsKey("OtherTermNonTaxableAmount") && !StringUtil.isNullOrEmpty(dataMap.get("OtherTermNonTaxableAmount").toString()) ) {
                groDetail.setOtherTermNonTaxableAmount(Double.parseDouble(dataMap.get("OtherTermNonTaxableAmount").toString()));
            }
            
            if (dataMap.containsKey("discount")) {
                groDetail.setDiscount((Double) dataMap.get("discount"));
            }
            
            if (dataMap.containsKey("discountispercent")) {
                groDetail.setDiscountispercent((Integer) dataMap.get("discountispercent"));
            }

            if (dataMap.containsKey("Inventory")) {
                groDetail.setInventory((Inventory) dataMap.get("Inventory"));
            }
            if (dataMap.containsKey("batch")) {
                ProductBatch productBatch = dataMap.get("batch") == null ? null : (ProductBatch) get(ProductBatch.class, (String) dataMap.get("batch"));
                if (productBatch != null) {
                    groDetail.setBatch(productBatch);
                }
            }

            if (dataMap.containsKey("partno")) {
                groDetail.setPartno(StringUtil.DecodeText((String) dataMap.get("partno")));
            } else {
                groDetail.setPartno("");
            }
            if (dataMap.containsKey("invstoreid")) {
                groDetail.setInvstoreid((String) dataMap.get("invstoreid"));
            } else {
                groDetail.setInvstoreid("");
            }
            if (dataMap.containsKey("invlocationid")) {
                groDetail.setInvlocid((String) dataMap.get("invlocationid"));
            } else {
                groDetail.setInvlocid("");
            }
            if (dataMap.containsKey("rate") && dataMap.get("rate") != null) {
                if (!StringUtil.isNullOrEmpty((String) dataMap.get("rate"))) {
                    groDetail.setRate(Double.parseDouble((String) dataMap.get("rate")));
                }
            }
            if (dataMap.containsKey("rateIncludingGst") && !StringUtil.isNullOrEmpty((String) dataMap.get("rateIncludingGst"))) {
                groDetail.setRateincludegst(Double.parseDouble((String) dataMap.get("rateIncludingGst")));
            }
            if (dataMap.containsKey("priceSource") && dataMap.get("priceSource") != null) {
                groDetail.setPriceSource((String) dataMap.get("priceSource"));
            }
            if (dataMap.containsKey("pricingbandmasterid") && dataMap.get("pricingbandmasterid") != null) {
                    groDetail.setPricingBandMasterid((String) dataMap.get("pricingbandmasterid"));
                }
            if (dataMap.containsKey("purchasesjedetailid") && dataMap.get("purchasesjedetailid") != null) {
                groDetail.setPurchasesJEDetail((JournalEntryDetail) get(JournalEntryDetail.class, dataMap.get("purchasesjedetailid").toString()));
            }
            if (dataMap.containsKey("inventoryjedetailid") && dataMap.get("inventoryjedetailid") != null) {
                groDetail.setInventoryJEdetail((JournalEntryDetail) get(JournalEntryDetail.class, dataMap.get("inventoryjedetailid").toString()));
            }
            if (dataMap.containsKey(Constants.isUserModifiedTaxAmount) && dataMap.get(Constants.isUserModifiedTaxAmount) != null) {
                groDetail.setIsUserModifiedTaxAmount((boolean)dataMap.get(Constants.isUserModifiedTaxAmount));
            }
            saveOrUpdate(groDetail);
            list.add(groDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveGoodsReceiptOrderDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getGoodsReceiptOrdersMerged(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            String ss = (String) request.get(Constants.ss);
            String moduleid = "";
            if (request.containsKey(Constants.moduleid) && request.get(Constants.moduleid) != null) {
                moduleid = request.get(Constants.moduleid).toString();
            }
            String newvendorid = "";
            if (request.containsKey("newvendorid") && request.get("newvendorid") != null) {
                newvendorid = (String) request.get("newvendorid");
            }
            ArrayList params = new ArrayList();
            boolean isfavourite = false;
            boolean isFixedAsset = false;
            boolean isprinted = false;
            boolean isConsignment = false;
            boolean isgrodetailsJoin = false;
            if (request.get("isfavourite") != null) {
                isfavourite = Boolean.parseBoolean((String) request.get("isfavourite"));
            }
            if (request.get("isFixedAsset") != null && !StringUtil.isNullOrEmpty(request.get("isFixedAsset").toString())) {
                isFixedAsset = Boolean.parseBoolean(request.get("isFixedAsset").toString());
            }
            if (request.containsKey("isConsignment") && request.get("isConsignment") != null) {
                isConsignment = (Boolean) request.get("isConsignment");
            }

            boolean isPendingApproval = false;
            if (request.get("pendingapproval") != null) {
                isPendingApproval = Boolean.parseBoolean(request.get("pendingapproval").toString());
            }
            if (request.get(Constants.MARKED_PRINTED) != null) {
                isprinted = Boolean.parseBoolean((String) request.get(Constants.MARKED_PRINTED));
            }
            
            String productid = "";
            if (request.containsKey(Constants.productid) && request.get(Constants.productid) != null) {
                productid = (String) request.get(Constants.productid);
            }

            String productCategoryid = "";
            if (request.containsKey(Constants.productCategoryid) && request.get(Constants.productCategoryid) != null) {
                productCategoryid = (String) request.get(Constants.productCategoryid);
            }
            
            String vendorCategoryid = "";
            if (request.containsKey(Constants.customerCategoryid) && request.get(Constants.customerCategoryid) != null) {
                vendorCategoryid = (String) request.get(Constants.customerCategoryid);
            }
//            params.add((String) request.get(Constants.companyKey));
            String companyid = AccountingManager.getFilterInString((String) request.get(Constants.companyKey));
            //String conditionSQL = " where deliveryorder.deleteflag='F' and deliveryorder.company in "+companyid+" ";
            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean isJobWorkOutGRO = false;
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            if(request.containsKey("isJobWorkOutGRO") && request.get("isJobWorkOutGRO") != null){
                isJobWorkOutGRO =  Boolean.parseBoolean(request.get("isJobWorkOutGRO").toString());            
            }
            //Flag to show GRN date and qty in product view.
            boolean isProductView = false;
            if(request.containsKey("isProductView") && request.get("isProductView") != null){
                isProductView =  Boolean.parseBoolean(request.get("isProductView").toString());            
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
            
            String userDepartment = "";
            if (request.containsKey("userDepartment") && request.get("userDepartment") != null) {
                userDepartment = (String) request.get("userDepartment");
            }

            String conditionSQL = "";

            if (nondeleted) {

                conditionSQL = "  where grorder.deleteflag='F' and grorder.company in " + companyid + " ";

            } else if (deleted) {

                conditionSQL += " where grorder.deleteflag='T' and grorder.company in " + companyid + " ";

            } else {

                conditionSQL += " where grorder.company in " + companyid + " ";

            }
            if (request.containsKey("linknumber") && request.get("linknumber") != null && !request.get("linknumber").toString().equals("")) {
                conditionSQL += " and grorder.gronumber = ? ";
                params.add(request.get("linknumber"));
            }
            
            //   String conditionSQL = " where grorder.deleteflag='F' and grorder.company in "+companyid+" ";
            String searchJoin = "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                ss=ss.replace("\\", "\\\\");
                String[] searchcol = new String[]{"grorder.gronumber", "grorder.memo", "grorder.supplierinvoiceno", "vendor.name","vendor.aliasname", "grodetails.partno",
                    "vaddr.billingAddress1", "vaddr.billingCity1", "vaddr.billingCountry1", "vaddr.billingEmail1", "vaddr.billingState1", "vaddr.billingPostal1",
                    "vaddr.shippingAddress1", "vaddr.shippingCity1", "vaddr.shippingCountry1", "vaddr.shippingEmail1", "vaddr.shippingState1", "vaddr.shippingPostal1" ,"product.name","product.productid"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 20);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                searchJoin = " inner join grodetails on grodetails.grorder = grorder.id "
                        + " inner join product on grodetails.product = product.id "
                        + " left join vendoraddresses vaddr on vaddr.id= vendor.vendoraddresses ";
                conditionSQL += searchQuery;
                isgrodetailsJoin = true;
            }
//            if (!StringUtil.isNullOrEmpty(ss)) {
//                for (int i = 0; i < 3; i++) {
//                    params.add(ss + "%");
//                }
//                params.add("%" + ss + "%");
//                searchJoin = " inner join grodetails on grodetails.grorder = grorder.id ";
//                conditionSQL += " and ( grorder.gronumber like ? or grorder.memo like ? or vendor.name like ? or grodetails.partno like ? )";
//                
//            }
            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                conditionSQL += " and costcenter.id=?";
            }
            String vendorId = (String) request.get(CCConstants.REQ_vendorId);
            if (!StringUtil.isNullOrEmpty(vendorId)) {
                params.add(vendorId);
                conditionSQL += " and vendor.id=?";
            }
            
            if (!StringUtil.isNullOrEmpty(newvendorid)) {
                if (newvendorid.contains(",")) {
                    newvendorid = AccountingManager.getFilterInString(newvendorid);
                    conditionSQL += " and grorder.vendor IN" + newvendorid;
                } else {
                    params.add(newvendorid);
                    conditionSQL += " and grorder.vendor = ? ";

                }
            }
            if (!StringUtil.isNullOrEmpty(vendorCategoryid) && !StringUtil.equal(vendorCategoryid, "-1") && !StringUtil.equal(vendorCategoryid, "All")) {
                params.add(vendorCategoryid);
                conditionSQL += " and vendor.id in (select vendorid from vendorcategorymapping where vendorcategory = ?)  ";
            }
            
            if (isfavourite) {
                conditionSQL += " and grorder.favouriteflag=true ";
            }
            if (isJobWorkOutGRO) {
                conditionSQL += " and grorder.isjobworkoutorder= 'T'";
            }else{
                conditionSQL += " and grorder.isjobworkoutorder= 'F'";
            }
            if (isFixedAsset) {
                conditionSQL += " and grorder.isfixedAssetgro=true ";
            } else {
                conditionSQL += " and grorder.isfixedAssetgro=false ";
            }
            
            if (isConsignment) {
                conditionSQL += " and grorder.isconsignment='T'";
            } else {
                conditionSQL += " and grorder.isconsignment='F'";
            }
            
            if (isprinted) {
                conditionSQL += " and grorder.printedflag=true ";
            }
            String billID = "";
            if (request.containsKey("billid") && request.get("billid") != null) { // view GRO from journal entry
                billID = (String) request.get("billid");
            }
            if (!StringUtil.isNullOrEmpty(billID)) {
                params.add(billID);
                conditionSQL += " and grorder.id=?";
            }
            // Get only approved records
            if (isPendingApproval) {
                conditionSQL += " and grorder.approvestatuslevel != ? and grorder.approvestatuslevel != ? ";
                params.add(11); // final Aproved Status
                params.add(-1); // Rejected
            } else {
                conditionSQL += " and grorder.approvestatuslevel = ? ";
                params.add(11);
            }

            if (request.containsKey("currencyfilterfortrans") && request.get("currencyfilterfortrans") != null) {
                String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
                if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
                    conditionSQL += " and grorder.currency = ? ";
                    params.add(currencyfilterfortrans);
                }
            }

            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            String joinString = "";
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSQL += " and (grorder.grorderdate >=? and grorder.grorderdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            String joinString1 = " ";
            if (request.containsKey("poid") && request.get("poid") != null) {
                String poid = (String) request.get("poid");
                // if(!StringUtil.isNullOrEmpty(poid)){
                params.add(poid);
                if (searchJoin.equals("")) {
                    joinString1 = " inner join grodetails on grodetails.grorder = grorder.id ";
                    isgrodetailsJoin = true;
                }

                joinString1 += " inner join podetails on podetails.id=grodetails.podetails  "
                        + " inner join purchaseorder on purchaseorder.id=podetails.purchaseorder ";
                conditionSQL += " and  purchaseorder.id= ? ";

            }

            String joinString2 = " ";
            if (request.containsKey("goodreceiptid") && request.get("goodreceiptid") != null) {
                String goodreceiptid = (String) request.get("goodreceiptid");
                params.add(goodreceiptid);
                if (searchJoin.equals("")) {
                    joinString2 = " inner join grodetails on grodetails.grorder = grorder.id ";
                    isgrodetailsJoin = true;
                }
                joinString2 += " inner join grdetails on grdetails.id=grodetails.videtails or grodetails.id=grdetails.grorderdetails ";
                conditionSQL += " and  grdetails.goodsreceipt= ? ";
            }
            
            String appendCase = "and";
            String mySearchFilterString = "";
            String searchDefaultFieldSQL = "";
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
                    if (defaultSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Default Form fields
                         */
                        ArrayList tableArray = new ArrayList();
                        tableArray.add("customer"); //this table array used to identified wheather join exists on table or not                         
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, moduleid, tableArray, filterConjuctionCriteria);
                        searchJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchJoin += " left join goodsreceiptorderlinking on goodsreceiptorderlinking.docid=grorder.id and goodsreceiptorderlinking.sourceflag = 1 ";
                    }
                    if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field
                        request.put(Constants.Searchjson, Searchjson);
                        request.put(Constants.appendCase, appendCase);
                        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                        String innerJoinOnDetailTable = "";
                        if (!searchJoin.contains("grodetails.grorder")) {
                            innerJoinOnDetailTable = " inner join grodetails on grodetails.grorder=grorder.id ";
                        }
                        boolean isInnerJoinAppend = false;
                        if (mySearchFilterString.contains("grordercustomdata")) {
                            joinString = " inner join grordercustomdata on grordercustomdata.goodsreceiptorderid=grorder.accgrordercustomdataref ";
                        }
                        if (mySearchFilterString.contains("VendorCustomData")) {
                            joinString += " left join vendorcustomdata  on vendorcustomdata.vendorId=vendor.id ";
                            mySearchFilterString = mySearchFilterString.replaceAll("VendorCustomData", "vendorcustomdata");
                        }
                        if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "grodetailscustomdata");
                            joinString += innerJoinOnDetailTable + " left join grodetailscustomdata on grodetails.id=grodetailscustomdata.grodetailsid ";
                            isInnerJoinAppend = true;
                            isgrodetailsJoin = true;
                        }
                        if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "grodetailproductcustomdata");
                            joinString += innerJoinOnDetailTable + " left join grodetailproductcustomdata on grodetails.id=grodetailproductcustomdata.grDetailID ";
                            isInnerJoinAppend = true;
                            isgrodetailsJoin = true;
                        }
                        //product custom data
                        if (mySearchFilterString.contains("accproductcustomdata")) {
                            if (isInnerJoinAppend) {
                                joinString += " left join product on product.id=grodetails.product left join accproductcustomdata on accproductcustomdata.productId=product.id ";
                            } else {
                                joinString += innerJoinOnDetailTable + " left join product on product.id=grodetails.product left join accproductcustomdata on accproductcustomdata.productId=product.id ";
                                isgrodetailsJoin = true;
                            }

                        }
//                    mySearchFilterString = mySearchFilterString.replaceAll("DeliveryOrderCustomData", "deliveryorder.accdeliveryordercustomdataref");
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
            
            String joinString3 = "";
            String conditionSQLAfterSearch = "";
            if (!StringUtil.isNullOrEmpty(productid)) {
                if (!isgrodetailsJoin) {
                    joinString3 = " inner join grodetails on grodetails.grorder = grorder.id ";
                    isgrodetailsJoin = true;
                }
                params.add(productid);
                conditionSQLAfterSearch += " and grodetails.product = ? ";
            }

            if (!StringUtil.isNullOrEmpty(productCategoryid)) {
                if (!isgrodetailsJoin) {
                    joinString3 = " inner join grodetails on grodetails.grorder = grorder.id ";
                    isgrodetailsJoin = true;
                }
                params.add(productCategoryid);
                conditionSQLAfterSearch += " and grodetails.product in (select productid from productcategorymapping where productcategory = ?) ";
            }
            
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                joinString += " inner join users on users.userid = grorder.createdby ";
                conditionSQLAfterSearch += " and users.department = ? ";
                params.add(userDepartment);
            }
            
            if ((request.containsKey("linkFlag") && request.get("linkFlag").equals("true"))) {
                if ((request.containsKey("srflag") && request.get("srflag").equals("true"))) {
                    conditionSQLAfterSearch += " and grorder.isopeninpr = 'T' ";
                }else{
                   conditionSQLAfterSearch +=   " and grorder.isopeninpi='T'";
                } 
            } else if (request.containsKey("srflag") && request.get("srflag").equals("true") && isConsignment) {

                conditionSQLAfterSearch += " and grorder.isopeninpr = 'T' ";

            }
            
            String orderBy = "";
            String sort_Col = "";
            String joinString4 = "";
            String[] stringSort = null;
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                stringSort = columSortGoodsReceipt(Col_Name, Col_Dir);
                orderBy += stringSort[0];
                sort_Col += stringSort[1];
                if(request.get("sort").toString().equals("agentname")){
                    joinString4 += "  left join masteritem on masteritem.id = grorder.masteragent ";
                }


            } else {
                orderBy = " order by grorderdate desc";
                sort_Col += " ,grorder.grorderdate ";
            }
            
             String salesPersonMappingQuery = "";
//           if (isenableSalesPersonAgentFlow && !StringUtil.isNullOrEmpty(userID)) {
//                salesPersonMappingQuery = " left join vendoragentmapping spm on spm.vendorid=grorder.vendor  left join masteritem  mst on mst.id=spm.agent ";
//                joinString1+=salesPersonMappingQuery;
//                conditionSQL += " and ((mst.user= '" + userID + "' or mst.user is null  and vendor.vendavailtoagent='T' ) or  (vendor.vendavailtoagent='F')) ";
//            }
            if (isenableSalesPersonAgentFlow && !StringUtil.isNullOrEmpty(userID)) {//this block is executed only when owners restriction feature is on 
                String salesPersonID = (String) request.get("salesPersonid");
                String salespersonQuery = "";
                 if (!StringUtil.isNullOrEmpty(salesPersonID)) {
                   salesPersonID= AccountingManager.getFilterInString(salesPersonID);
                    salespersonQuery = "  grorder.masteragent in " + salesPersonID + " or ";
                }
                
                conditionSQLAfterSearch += " and ( " + salespersonQuery + "  grorder.createdby='" + userID + "' or grorder.masteragent is null  ) ";
            }
            String selectQuery = " select distinct(grorder.id), 'false' as withoutinventory " + sort_Col ; 
            if (isProductView) {
                //Get required fields 
                selectQuery = " select distinct(grorder.id), grodetails.id, grorder.grorderdate,grodetails.deliveredquantity,'false' as withoutinventory " + sort_Col;
            }

            String mysqlQuery = selectQuery +" from grorder "
                    + "inner join vendor on vendor.id = grorder.vendor " + joinString1 + joinString2 + joinString3 + searchJoin + joinString + joinString4
                    + "left join costcenter on costcenter.id = grorder.costcenter " + conditionSQL + mySearchFilterString + conditionSQLAfterSearch + orderBy;




            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
                if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                    list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptDAOImpl.getGoodsReceiptorders:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getGoodsReceiptOrderDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from GoodsReceiptOrderDetails";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getGRODetails(String poid, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select grodetails.id from grodetails inner join grorder on grodetails.grorder=grorder.id "
                    + "where  grorder.company=? and grorder.deleteflag='F' and grodetails.podetails=? "
                    + "union "
                    + "select grodetails.id from grodetails inner join grorder on grodetails.grorder=grorder.id "
                    + "where  grorder.company=? and grorder.deleteflag='F' and grodetails.videtails in "
                    + " ( select grdetails.id from grdetails inner join goodsreceipt on grdetails.goodsreceipt=goodsreceipt.id "
                    + "where goodsreceipt.company=? and goodsreceipt.deleteflag='F' and grdetails.purchaseorderdetail=? )";
            list = executeSQLQuery( query, new Object[]{companyid, poid, companyid, companyid, poid});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getGRODetails:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getBadDebtPurchaseInvoiceMappingForGoodsReceipt(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);

            String start = "";
            String limit = "";

            if (requestParams.containsKey("invoiceid") && requestParams.get("invoiceid") != null) {
                condition += " and bdm.goodsReceipt.ID=? ";
                params.add((String) requestParams.get("invoiceid"));
            }

            if (requestParams.containsKey("badDebtType") && requestParams.get("badDebtType") != null) {
                condition += " and bdm.badDebtType=? ";
                params.add((Integer) requestParams.get("badDebtType"));
            }

            if (requestParams.containsKey("recoveredFromDate") && requestParams.get("recoveredFromDate") != null && requestParams.containsKey("recoveredToDate") && requestParams.get("recoveredToDate") != null) {
                condition += " and (bdm.badDebtRecoveredDate>=? and bdm.badDebtRecoveredDate<=?) ";
                params.add((Date) requestParams.get("recoveredFromDate"));
                params.add((Date) requestParams.get("recoveredToDate"));
            }

            if (requestParams.containsKey("claimedFromDate") && requestParams.get("claimedFromDate") != null && requestParams.containsKey("claimedToDate") && requestParams.get("claimedToDate") != null) {
                condition += " and (bdm.badDebtClaimedDate>=? and bdm.badDebtClaimedDate<=?)";
                params.add((Date) requestParams.get("claimedFromDate"));
                params.add((Date) requestParams.get("claimedToDate"));
            }

            if (requestParams.containsKey(Constants.start) && requestParams.get(Constants.start) != null && requestParams.containsKey(Constants.limit) && requestParams.get(Constants.limit) != null) {
                start = (String) requestParams.get(Constants.start);
                limit = (String) requestParams.get(Constants.limit);
            }

            String ss = (requestParams.containsKey("ss") && requestParams.get("ss") != null) ? (String) requestParams.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"bdm.goodsReceipt.goodsReceiptNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }
            
            String mySearchFilterString = "";
            if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
                mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, params);
                if (mySearchFilterString.contains("c.accjecustomdata")) {
                    mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", "bdm.goodsReceipt.journalEntry.accBillInvCustomData");
                }
            }
            
            String selQuery = "from BadDebtPurchaseInvoiceMapping bdm where bdm.company.companyID=? " + condition + mySearchFilterString;
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging( selQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            } else {
                list = executeQuery( selQuery, params.toArray());
            }
        } catch (SQLException | NumberFormatException | ServiceException ex) {
            throw ServiceException.FAILURE("getBadDebtPurchaseInvoiceMappingForGoodsReceipt : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getGRDetails(String poid) throws ServiceException {
        String selQuery = "from GoodsReceiptDetail grd where grd.purchaseorderdetail.ID = ? and grd.goodsReceipt.deleted = false";
        List list = executeQuery( selQuery, new Object[]{poid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject deleteGoodsReceiptOrder(String doid, String companyid) throws ServiceException {
        String query = "update GoodsReceiptOrder set deleted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{doid, companyid});
        return new KwlReturnObject(true, "Goods receipt has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject updateGoodsReceiptOrder(GoodsReceiptOrder goodsReceiptOrder) throws ServiceException {
        List list = new ArrayList();
        try {
            saveOrUpdate(goodsReceiptOrder);
            list.add(goodsReceiptOrder);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateGoodsReceiptOrder : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteAssetDetailsLinkedWithGROrder(HashMap<String, Object> requestParams) throws ServiceException {
        int numtotal = 0;
        try {
            if (requestParams.containsKey("doid") && requestParams.containsKey("companyid")) {

                int numRows = 0;
                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("companyid"));
                params8.add(requestParams.get("doid"));

                String assetDetailIdString = "",delQuerypb="",delQueryBs="";

                // Deleting data from asset details table

                String assetQuery = "SELECT ad.id FROM grorder gr "
                        + "INNER JOIN  grodetails grd ON gr.id=grd.grorder "
                        + "INNER JOIN assetdetailsinvdetailmapping amp ON grd.id=amp.invoicedetailid "
                        + "INNER JOIN assetdetail ad on ad.id=amp.assetdetails "
                        + "WHERE amp.moduleid=28 AND ad.assetsoldflag=0 AND gr.company=? and gr.id=?";

                List assetList = executeSQLQuery( assetQuery, params8.toArray());
                Iterator assetItr = assetList.iterator();

                while (assetItr.hasNext()) {
                    String assetDetailId = assetItr.next().toString();
                    assetDetailIdString += "'" + assetDetailId + "',";
                }

                if (!StringUtil.isNullOrEmpty(assetDetailIdString)) {
                    assetDetailIdString = assetDetailIdString.substring(0, assetDetailIdString.length() - 1);
                }

//                String myquery = "select id from grodetails where grorder in (select id from grorder where company = ? and id=?)";
                String myquery = "select grod.id from grodetails grod inner join grorder gro on grod.grorder=gro.id where gro.company = ? and gro.id=?";
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
               
                    String batchmapids = "", serialmapids = "";
                    if (!StringUtil.isNullOrEmpty(idStrings)) {
                        ArrayList assetParams = new ArrayList();
                        assetParams.add(requestParams.get("companyid"));
                        String assetMapDelQuery = "DELETE FROM assetdetailsinvdetailmapping WHERE invoicedetailid IN (" + idStrings + ") and moduleid=28 and company=?";
                        int numRowsAsset = executeSQLUpdate( assetMapDelQuery, assetParams.toArray());
                    }

                    if (!StringUtil.isNullOrEmpty(assetDetailIdString)) {
                        ArrayList params14 = new ArrayList();
                        String myquery4 = " select batchmapid from locationbatchdocumentmapping where documentid in (" + assetDetailIdString + ") ";
                        String myquery5 = " select serialid from serialdocumentmapping where documentid in (" + assetDetailIdString + ") ";
                        List list4 = executeSQLQuery( myquery4, params14.toArray());
                        Iterator itr4 = list4.iterator();
                        while (itr4.hasNext()) {

                            String batchstringids = itr4.next().toString();
                            batchmapids += "'" + batchstringids + "',";
                        }
                        if (!StringUtil.isNullOrEmpty(batchmapids)) {
                            batchmapids = batchmapids.substring(0, batchmapids.length() - 1);
                        }
                        list4 = executeSQLQuery( myquery5, params14.toArray());
                        itr4 = list4.iterator();
                        while (itr4.hasNext()) {

                            String serialstringids = itr4.next().toString();
                            serialmapids += "'" + serialstringids + "',";
                        }
                        if (!StringUtil.isNullOrEmpty(serialmapids)) {
                            serialmapids = serialmapids.substring(0, serialmapids.length() - 1);
                        }
                       ArrayList params15 = new ArrayList();
                        delQuerypb = "delete  from locationbatchdocumentmapping where documentid in (" + assetDetailIdString + ") ";
                        int numRowbs = executeSQLUpdate( delQuerypb, params15.toArray());

                        delQuerypb = "delete  from serialdocumentmapping where documentid in (" + assetDetailIdString + ") ";
                        numRowbs = executeSQLUpdate( delQuerypb, params15.toArray());
                    }
                 
                   
                 
                    if (!StringUtil.isNullOrEmpty(batchmapids)) {
                       ArrayList paramsBatch = new ArrayList();
                        delQuerypb = "delete  from newproductbatch where id in (" + batchmapids + ") ";
                        int numRows8 = executeSQLUpdate( delQuerypb, paramsBatch.toArray());
                    }

                    if (!StringUtil.isNullOrEmpty(serialmapids)) {
                        ArrayList paramsSerial = new ArrayList();
                        delQueryBs = " delete from newbatchserial where id in(" + serialmapids + ") ";
                        int numRowsSerial = executeSQLUpdate( delQueryBs, paramsSerial.toArray());
                    }

                    if (!StringUtil.isNullOrEmpty(assetDetailIdString)) {
                        ArrayList assetParams = new ArrayList();
                        assetParams.add(requestParams.get("companyid"));
                        String assupdateQuery = "DELETE FROM assetdetailcustomdata  WHERE assetDetailsId IN(" + assetDetailIdString + ") AND company=?";
                         numRows += executeSQLUpdate( assupdateQuery, assetParams.toArray());
                        String deletemachineasset = "DELETE FROM machine_asset_mapping  WHERE assetDetails IN (" + assetDetailIdString + ") AND company=?";
                        numRows += executeSQLUpdate(deletemachineasset, assetParams.toArray());
                         assupdateQuery = "DELETE FROM assetdetail  WHERE id IN(" + assetDetailIdString + ") AND company=?";
                         numRows += executeSQLUpdate( assupdateQuery, assetParams.toArray());
                    }
//                }

                // Deleting data from assetdetailsinvdetailmapping

//                if (!StringUtil.isNullOrEmpty(idStrings)) {
//                    ArrayList assetParams = new ArrayList();
//                    assetParams.add(requestParams.get("companyid"));
//
//                    String assetMapDelQuery = "DELETE FROM assetdetailsinvdetailmapping WHERE invoicedetailid IN (" + idStrings + ") and moduleid=28 and company=?";
//                    numRows = executeSQLUpdate( assetMapDelQuery, assetParams.toArray());
//                }
//
//                // Deletion of batch in case of partial deletion of Goods Receipt Order
//
//                String myquery3 = " select batch from assetdetail where id in (" + assetDetailIdString + ") ";  //batch of all the asset of group
//                List list3 = executeSQLQuery( myquery, params8.toArray());
//                Iterator itr3 = list3.iterator();
//                String batchIdstring = "";
//                while (itr.hasNext()) {
//
//                    String batchIds = itr3.next().toString();
//                    batchIdstring += "'" + batchIds + "',";
//                }
//                if (!StringUtil.isNullOrEmpty(batchIdstring)) {
//                    batchIdstring = batchIdstring.substring(0, batchIdstring.length() - 1);
//                }
//                if (!StringUtil.isNullOrEmpty(batchIdstring)) {
//                    ArrayList params11 = new ArrayList();
//                    params11.add(requestParams.get("companyid"));
//                    String delQuery11 = " delete  from productbatch where id (" + batchIdstring + ") ";
//                    int numRows11 = executeSQLUpdate( delQuery11, params11.toArray());
//
//                    ArrayList params12 = new ArrayList();
//                    params12.add(requestParams.get("companyid"));
//                    String delQuery12 = " delete from batchserial where batch in (" + batchIdstring + ") ";
//                    int numRows12 = executeSQLUpdate( delQuery12, params12.toArray());
//
//                }


                // Deleting data from asset details table
//                if (!StringUtil.isNullOrEmpty(assetDetailIdString)) {
//                    ArrayList assetParams = new ArrayList();
//                    assetParams.add(requestParams.get("companyid"));
//
//                    String assupdateQuery = "DELETE FROM assetdetail  WHERE id IN(" + assetDetailIdString + ") AND company=?";
//                    numRows += executeSQLUpdate( assupdateQuery, assetParams.toArray());
//                }
                numtotal = numRows;
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Goods Receipt as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Goods Receipt has been deleted successfully.", null, null, numtotal);
    }

    public KwlReturnObject deleteGoodsReceiptOrdersBatchSerialDetails(HashMap<String, Object> requestParams) throws ServiceException,AccountingException {
        String delQuery = "", delQuery1 = "", delQuery2 = "", delQuerypb = "", delQueryBs = "";
        int numtotal = 0, numRows5 = 0;
        String serialmapids = "", docids = "",serialstringids="";
        String batchmapids = "";
        boolean isnegativestockforlocwar = false;
        if (requestParams.containsKey("isnegativestockforlocwar") && requestParams.get("isnegativestockforlocwar") != null) {
            isnegativestockforlocwar = Boolean.parseBoolean(requestParams.get("isnegativestockforlocwar").toString());
        }
        ArrayList params13 = new ArrayList();
        params13.add(requestParams.get("companyid"));
        params13.add(requestParams.get("doid"));
//        String myquery3 = "select id from grodetails where grorder in (select id from grorder where company = ? and id=?)";
        String myquery3 =  "select grod.id from grodetails grod inner join grorder gro on grod.grorder=gro.id where gro.company = ? and gro.id=?";
        List listBatch = executeSQLQuery( myquery3, params13.toArray());
        Iterator itrBatch = listBatch.iterator();
        while (itrBatch.hasNext()) {
            String batchstring = itrBatch.next().toString();
            docids += "'" + batchstring + "',";
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
            docids = docids.substring(0, docids.length() - 1);
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
            ArrayList params14 = new ArrayList();
            String myquery4 = " select batchmapid,id from locationbatchdocumentmapping where documentid in (" + docids + ") ";
            String myquery5 = " select id,serialid from serialdocumentmapping where documentid in (" + docids + ") ";
            List list4 = executeSQLQuery( myquery4, params14.toArray());
            Iterator itr4 = list4.iterator();
            while (itr4.hasNext()) {
                
                Object[] objArr = (Object[]) itr4.next();
                batchmapids += "'" + objArr[0] + "',";
//                if (isnegativestockforlocwar) {
                    LocationBatchDocumentMapping locationBatchDocumentMapping = (LocationBatchDocumentMapping) get(LocationBatchDocumentMapping.class, (String) objArr[1]);
                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    if (locationBatchDocumentMapping != null) {
                        batchUpdateQtyMap.put("qty", -locationBatchDocumentMapping.getQuantity());
                        batchUpdateQtyMap.put("quantity", -locationBatchDocumentMapping.getQuantity());
                    }
                    batchUpdateQtyMap.put("id", locationBatchDocumentMapping.getBatchmapid().getId());
                    saveBatchAmountDue(batchUpdateQtyMap);
//                }
            }
            if (!StringUtil.isNullOrEmpty(batchmapids)) {
                batchmapids = batchmapids.substring(0, batchmapids.length() - 1);
            }
            list4 = executeSQLQuery( myquery5, params14.toArray());
            itr4 = list4.iterator();
            while (itr4.hasNext()) {
                Object[] objArr = (Object[]) itr4.next();
                serialstringids += "'" + objArr[1] + "',";
           }
            if (!StringUtil.isNullOrEmpty(serialstringids)) {
                serialmapids = serialstringids.substring(0, serialstringids.length() - 1);
            }
            String serialDocumentMappingId = getSerialDocumentIds(list4);
            if (!StringUtil.isNullOrEmpty(serialDocumentMappingId)) {
                serialDocumentMappingId = serialDocumentMappingId.substring(0, serialDocumentMappingId.length() - 1);
                ArrayList params1 = new ArrayList();
                delQuery1 = "delete  from serialcustomdata where serialdocumentmappingid in (" + serialDocumentMappingId + ")";
                int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());
            }
            
        }
        ArrayList params15 = new ArrayList();
        delQuerypb = "delete  from locationbatchdocumentmapping where documentid in (" + docids + ") ";
        int numRows = executeSQLUpdate( delQuerypb, params15.toArray());

        delQuerypb = "delete  from serialdocumentmapping where documentid in (" + docids + ") ";
        numRows = executeSQLUpdate( delQuerypb, params15.toArray());
       
        /**
         *  commented below code because we now don't delete whole batch,instead we change only qty and qtydue for that batch because if we don't do so if suppose 
         *  opening is given for that product,then all opening gets deleted if we delete that batch.refer ERP-19794,ERP-19116
         * 
        if (!StringUtil.isNullOrEmpty(batchmapids) && !isnegativestockforlocwar) {
            params15 = new ArrayList();
            delQuerypb = "delete  from newproductbatch where id in (" + batchmapids + ") ";
                int numRows8 = executeSQLUpdate( delQuerypb, params15.toArray());
          
            }
        }           **/
        
         if (!StringUtil.isNullOrEmpty(serialmapids)) {
             int count = 0;
             String  myquery6 = "SELECT count(id) as count from serialdocumentmapping where serialid in(" + serialmapids + ") and transactiontype='27'";
             List list = executeSQLQuery(myquery6);
             if(!list.isEmpty()){
                 if (list.get(0) != null && !StringUtil.isNullOrEmpty(list.get(0).toString())) {
                     count = Integer.parseInt(list.get(0).toString());
                 }
             }
             if(count > 0){
                 throw new AccountingException("Cannot Edit Purchase Invoice as serials are already used in Sales transactions.");
             }
             ArrayList paramsSerial = new ArrayList();
            delQueryBs = " delete from newbatchserial where id in(" + serialmapids + ") ";
            int numRowsSerial = executeSQLUpdate( delQueryBs, paramsSerial.toArray());
        }
        
        return new KwlReturnObject(true, "Delivery Order has been deleted successfully.", null, null, numtotal);
    }
    public String getSerialDocumentIds(List list) {
        String serialDocument = "";
        String serialDocumentMappingId = "";
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objArr = (Object[]) itr.next();
            for (int i = 0; i < objArr.length; i++) {
                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) get(SerialDocumentMapping.class, (String) objArr[i]);
                if (serialDocumentMapping != null) {
                    serialDocument = serialDocumentMapping.getId().toString();
                    serialDocumentMappingId += "'" + serialDocument + "',";

                }
            }

        }
        return serialDocumentMappingId;
    }
    @Override
    public KwlReturnObject getGoodsReceiptOrdersBatchDetails(HashMap<String, Object> requestParams) throws ServiceException {       
        List list=new ArrayList();
        String serialmapids = "", docids = "";
        String batchmapids = "";
        ArrayList params13 = new ArrayList();
        params13.add(requestParams.get("companyid"));
        params13.add(requestParams.get("doid"));
//        String myquery3 = "select id from grodetails where grorder in (select id from grorder where company = ? and id=?)";
        String myquery3 =  "select grod.id from grodetails grod inner join grorder gro on grod.grorder=gro.id where gro.company = ? and gro.id=?";
        List listBatch = executeSQLQuery( myquery3, params13.toArray());
        Iterator itrBatch = listBatch.iterator();
        while (itrBatch.hasNext()) {
            String batchstring = itrBatch.next().toString();
            docids += "'" + batchstring + "',";
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
            docids = docids.substring(0, docids.length() - 1);
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
//            ArrayList params14 = new ArrayList();
            String myquery4 = "from LocationBatchDocumentMapping where documentid in (" + docids + ") ";
//            String myquery5 = " select serialid from serialdocumentmapping where documentid in (" + docids + ") ";
            list = executeQuery( myquery4);            
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteGoodsReceiptOrdersPermanent(HashMap<String, Object> requestParams) throws ServiceException {

        String delQuery = "", delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuery4 = "", delQuery5 = "", delQuery6, delQuerypb = "", delQueryBs = "", delQueryBatch = "";
        int numtotal = 0, numRowsPb = 0, numRowsBs = 0;
        String batchserialids = "", batchids = "";
        try {
            if (requestParams.containsKey("doid") && requestParams.containsKey("companyid")) {
                boolean isFixedAsset = false;
                if (requestParams.containsKey("isFixedAsset") && requestParams.get("isFixedAsset") != null) {
                    isFixedAsset = (Boolean) requestParams.get("isFixedAsset");
                }

                String assetDetailIdString = "";

                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("companyid"));
                params8.add(requestParams.get("doid"));

                // Deleting data from asset details table
                if (isFixedAsset) {

                    String assetQuery = "SELECT ad.id FROM grorder gr "
                            + "INNER JOIN  grodetails grd ON gr.id=grd.grorder "
                            + "INNER JOIN assetdetailsinvdetailmapping amp ON grd.id=amp.invoicedetailid "
                            + "INNER JOIN assetdetail ad on ad.id=amp.assetdetails "
                            + "WHERE amp.moduleid=28 AND ad.assetsoldflag=0 AND gr.company=? and gr.id=?";

                    List assetList = executeSQLQuery( assetQuery, params8.toArray());
                    Iterator assetItr = assetList.iterator();

                    while (assetItr.hasNext()) {
                        String assetDetailId = assetItr.next().toString();
                        assetDetailIdString += "'" + assetDetailId + "',";
                    }
                }

                if (!StringUtil.isNullOrEmpty(assetDetailIdString)) {
                    assetDetailIdString = assetDetailIdString.substring(0, assetDetailIdString.length() - 1);
                }

//                String myquery = "select id from grodetails where grorder in (select id from grorder where company = ? and id=?)";
                String myquery =  "select grod.id from grodetails grod inner join grorder gro on grod.grorder=gro.id where gro.company = ? and gro.id=?";
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
                /**
                 *  Delete GST Fields for India.
                 */
                deleteGstTaxClassDetails(idStrings);

                // Deleting data from assetdetailsinvdetailmapping
                if (isFixedAsset) {
                    String batchmapids = "", serialmapids = "";
                    if (!StringUtil.isNullOrEmpty(idStrings)) {
                        ArrayList assetParams = new ArrayList();
                        assetParams.add(requestParams.get("companyid"));
                        String assetMapDelQuery = "DELETE FROM assetdetailsinvdetailmapping WHERE invoicedetailid IN (" + idStrings + ") and moduleid=28 and company=?";
                        int numRows = executeSQLUpdate( assetMapDelQuery, assetParams.toArray());
                    }

                    if (!StringUtil.isNullOrEmpty(assetDetailIdString)) {
                        ArrayList params14 = new ArrayList();
                        String myquery4 = " select batchmapid from locationbatchdocumentmapping where documentid in (" + assetDetailIdString + ") ";
                        String myquery5 = " select serialid from serialdocumentmapping where documentid in (" + assetDetailIdString + ") ";
                        List list4 = executeSQLQuery( myquery4, params14.toArray());
                        Iterator itr4 = list4.iterator();
                        while (itr4.hasNext()) {

                            String batchstringids = itr4.next().toString();
                            batchmapids += "'" + batchstringids + "',";
                        }
                        if (!StringUtil.isNullOrEmpty(batchmapids)) {
                            batchmapids = batchmapids.substring(0, batchmapids.length() - 1);
                        }
                        list4 = executeSQLQuery( myquery5, params14.toArray());
                        itr4 = list4.iterator();
                        while (itr4.hasNext()) {

                            String serialstringids = itr4.next().toString();
                            serialmapids += "'" + serialstringids + "',";
                        }
                        if (!StringUtil.isNullOrEmpty(serialmapids)) {
                            serialmapids = serialmapids.substring(0, serialmapids.length() - 1);
                        }
                       ArrayList params15 = new ArrayList();
                        delQuerypb = "delete  from locationbatchdocumentmapping where documentid in (" + assetDetailIdString + ") ";
                        int numRowbs = executeSQLUpdate( delQuerypb, params15.toArray());

                        delQuerypb = "delete  from serialdocumentmapping where documentid in (" + assetDetailIdString + ") ";
                        numRowbs = executeSQLUpdate( delQuerypb, params15.toArray());
                    }
                 
                   
                 
                    if (!StringUtil.isNullOrEmpty(batchmapids)) {
                       ArrayList paramsBatch = new ArrayList();
                        delQuerypb = "delete  from newproductbatch where id in (" + batchmapids + ") ";
                        int numRows8 = executeSQLUpdate( delQuerypb, paramsBatch.toArray());
                    }

                    if (!StringUtil.isNullOrEmpty(serialmapids)) {
                        ArrayList paramsSerial = new ArrayList();
                        delQueryBs = " delete from newbatchserial where id in(" + serialmapids + ") ";
                        int numRowsSerial = executeSQLUpdate( delQueryBs, paramsSerial.toArray());
                    }

                    if (!StringUtil.isNullOrEmpty(assetDetailIdString)) {
                        ArrayList assetParams = new ArrayList();
                        assetParams.add(requestParams.get("companyid"));
                        String assupdateQuery = "DELETE FROM assetdetailcustomdata  WHERE assetDetailsId IN(" + assetDetailIdString + ") AND company=?";
                        int numRows = executeSQLUpdate( assupdateQuery, assetParams.toArray());
                        String deletemachineasset = "DELETE FROM machine_asset_mapping  WHERE assetDetails IN (" + assetDetailIdString + ") AND company=?";
                        numRows += executeSQLUpdate(deletemachineasset, assetParams.toArray());
                        assupdateQuery = "DELETE FROM assetdetail  WHERE id IN(" + assetDetailIdString + ") AND company=?";
                        numRows = executeSQLUpdate( assupdateQuery, assetParams.toArray());
                    }
                }

                if (!isFixedAsset) {
                    ArrayList params13 = new ArrayList();
                    params13.add(requestParams.get("companyid"));
                    params13.add(requestParams.get("doid"));
//                    String myquery3 = "select batch from grodetails where grorder in (select id from grorder where company = ? and id=?) and batch is not null";
                    String myquery3 = "select grod.batch from grodetails grod inner join grorder gro on grod.grorder=gro.id where gro.company = ? and gro.id=? and grod.batch is not null";

                    List listBatch = executeSQLQuery( myquery3, params13.toArray());
                    Iterator itrBatch = listBatch.iterator();
                    while (itrBatch.hasNext()) {
                        String batchstring = itrBatch.next().toString();
                        batchids += "'" + batchstring + "',";
                    }
                    if (!StringUtil.isNullOrEmpty(batchids)) {
                        batchids = batchids.substring(0, batchids.length() - 1);
                    }
                    if (!StringUtil.isNullOrEmpty(batchids)) {
                        ArrayList params14 = new ArrayList();
                        String myquery4 = " select id from batchserial where batch in (" + batchids + ") ";
                        List list4 = executeSQLQuery( myquery4, params14.toArray());
                        Iterator itr4 = list4.iterator();
                        while (itr4.hasNext()) {

                            String batchstringids = itr4.next().toString();
                            batchserialids += "'" + batchstringids + "',";
                        }
                        if (!StringUtil.isNullOrEmpty(batchserialids)) {
                            batchserialids = batchserialids.substring(0, batchserialids.length() - 1);
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(batchserialids)) {
                        ArrayList params15 = new ArrayList();
                        delQuerypb = "delete  from batchserialmapping where purchaseSerial in (" + batchserialids + ") ";
                        int numRows8 = executeSQLUpdate( delQuerypb, params15.toArray());

                        ArrayList paramsSerial = new ArrayList();
                        delQueryBs = " delete from batchserial where id in(" + batchserialids + ") ";
                        int numRowsSerial = executeSQLUpdate( delQueryBs, paramsSerial.toArray());
                    }
                }

                ArrayList params5 = new ArrayList();
                params5.add(requestParams.get("companyid"));
                params5.add(requestParams.get("doid"));
                delQuery5 = " delete from grodetails where grorder in (select id from grorder where company =? and id=?)";
                int numRows5 = executeSQLUpdate( delQuery5, params5.toArray());

                if (!isFixedAsset) {
                    if (!StringUtil.isNullOrEmpty(batchids)) {
                        ArrayList paramsBatch = new ArrayList();
                        delQueryBatch = "delete  from productbatch where id in (" + batchids + ") ";
                        int numRowsBatch = executeSQLUpdate( delQueryBatch, paramsBatch.toArray());
                    }
                }

                boolean isMRPModuleActivated = false; 
                String inventoryjeid = ""; // delete Inventory JE from GRO
                boolean isPerpetualValuationActivated = false;
                if (requestParams.containsKey("isPerpetualValuationActivated") && requestParams.get("isPerpetualValuationActivated") != null) {
                    isPerpetualValuationActivated = Boolean.parseBoolean(requestParams.get("isPerpetualValuationActivated").toString());
                }
                if (requestParams.containsKey("isMRPModuleActivated") && requestParams.get("isMRPModuleActivated") != null) {
                    isMRPModuleActivated = Boolean.parseBoolean(requestParams.get("isMRPModuleActivated").toString());
                }
               
                if (isMRPModuleActivated || isPerpetualValuationActivated) {
                    if (requestParams.containsKey("inventoryjeid") && requestParams.get("inventoryjeid") != null && !StringUtil.isNullOrEmpty(requestParams.get("inventoryjeid").toString())) {
                        inventoryjeid = requestParams.get("inventoryjeid").toString();
                        String query = "update grorder set inventoryje=NULL where id = ? and company = ?";
                        executeSQLUpdate(query, new Object[]{requestParams.get("doid"), requestParams.get("companyid")});
                        ArrayList params1 = new ArrayList();
                        params1.add(requestParams.get("companyid"));
                        delQuery1 = "delete  from accjedetailcustomdata where jedetailId in (select id from jedetail where company = ? and journalEntry in ('" + inventoryjeid + "'))";
                        executeSQLUpdate(delQuery1, params1.toArray());
                        ArrayList params3 = new ArrayList();
                        params3.add(requestParams.get("companyid"));
                        delQuery3 = "delete from jedetail where company = ? and journalEntry in ('" + inventoryjeid + "') ";
                        executeSQLUpdate(delQuery3, params3.toArray());
                        ArrayList params4 = new ArrayList();
                        delQuery4 = "delete from journalentry where id  in ('" + inventoryjeid + "')";
                        executeSQLUpdate(delQuery4, params4.toArray());
                        ArrayList params2 = new ArrayList();
                        delQuery2 = "delete  from accjecustomdata where journalentryId in ('" + inventoryjeid + "')";
                        executeSQLUpdate(delQuery2, params2.toArray());
                    }
                }

                //ArrayList params = new ArrayList();
                //params.add(requestParams.get("companyid"));
                String companyid=(String) requestParams.get("companyid");
//                delQuery = "delete  from inventory where company = ? and id in (" + idStrings + ") ";
//                int numRows = executeSQLUpdate( delQuery, params.toArray());
                String selQuery = "from Inventory where company.companyID = ? and  ID in (" + idStrings + ") ";
                List resultList = executeQuery( selQuery, new Object[]{companyid});
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
                int numRows =resultList.size();

                 
                ArrayList params9 = new ArrayList();
                params9.add(requestParams.get("companyid"));
                params9.add(requestParams.get("doid"));
                String myquery1 = "select id from grorder where company = ? and id=?";
                List list1 = executeSQLQuery( myquery1, params9.toArray());
                Iterator itr1 = list1.iterator();
                String journalent = "";
                while (itr1.hasNext()) {

                    String jeidi = itr1.next().toString();
                    journalent += "'" + jeidi + "',";
                }
                if (!StringUtil.isNullOrEmpty(journalent)) {
                    journalent = journalent.substring(0, journalent.length() - 1);
                }
                /**
                 * Delete GST Fields for India.
                 */
                deleteGstDocHistoryDetails(requestParams.get("doid").toString());
                ArrayList params1 = new ArrayList();
                delQuery1 = "delete  from grodetailscustomdata where grodetailsid in (" + idStrings + ")";
                int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());

                ArrayList params6 = new ArrayList();
                params6.add(requestParams.get("companyid"));
                params6.add(requestParams.get("doid"));
                delQuery6 = "delete from grorder where company = ? and id=?";
                int numRows6 = executeSQLUpdate( delQuery6, params6.toArray());


                ArrayList params2 = new ArrayList();
                delQuery2 = "delete  from grordercustomdata where goodsReceiptOrderId in (" + journalent + ")";
                int numRows2 = executeSQLUpdate( delQuery2, params2.toArray());


                numtotal = numRows + numRows1 + numRows2 + numRows5 + numRows6;
            }

            return new KwlReturnObject(true, "GoodsReceipt Order has been deleted successfully.", null, null, numtotal);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete GoodsReceipt Order as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject getGRFromGRInvoice(String greceiptid, String companyid) throws ServiceException {
        String selQuery = "from GoodsReceiptOrderDetails gr  where gr.videtails.goodsReceipt.ID=? and gr.grOrder.deleted=false and gr.company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{greceiptid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPRFromGReceipt(String invoiceid, String companyid) throws ServiceException {
        String selQuery = "from PurchaseReturnDetail pr  where pr.videtails.goodsReceipt.ID=? and pr.purchaseReturn.deleted=false and pr.company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{invoiceid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getConsignmentNumberFromGReceipt(String invoiceid, String companyid) throws ServiceException {
        String query = "select gr.id from goodsreceipt gr INNER JOIN goodsreceiptid_landedInvoice tgl on gr.id = tgl.goodsreceiptid WHERE tgl.landedinvoice = ?";
        List list= executeSQLQuery(query, new Object[]{invoiceid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    //function for checking good receipt order used in purchase return or not   
    public KwlReturnObject getGROFromPR(String invoiceid, String companyid) throws ServiceException {
        String selQuery = "from PurchaseReturnDetail srd  where srd.grdetails.grOrder.ID=? and srd.purchaseReturn.deleted=false and srd.company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{invoiceid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    //function for checking good receipt order used in invoices or not

    public KwlReturnObject getGROFromInv(String invoiceid, String companyid) throws ServiceException {
        String selQuery = "from GoodsReceiptDetail srd  where srd.goodsReceiptOrderDetails.grOrder.ID=? and srd.goodsReceipt.deleted=false and srd.company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{invoiceid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    //function for checking Serial No used in delivery order or not 

    public KwlReturnObject getSerialNoUsedinDOFromGRO(String invoiceid, String companyid) throws ServiceException {
        String selQuery = " select serialid  from serialdocumentmapping  inner join  grodetails on grodetails.id=documentid where grodetails.grorder=? and grodetails.company=?"; // issue unable to delete GRN as company column become ambigious
        List list = executeSQLQuery( selQuery, new Object[]{invoiceid, companyid});
        String docids = "";
        List lst = new ArrayList();
        Iterator itrSerial = list.iterator();
        while (itrSerial.hasNext()) {
            String serialstring = itrSerial.next().toString();
            docids += "'" + serialstring + "',";
        }

        if (!StringUtil.isNullOrEmpty(docids)) {
            docids = docids.substring(0, docids.length() - 1);
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
            selQuery = " select serialid  from serialdocumentmapping where serialid in (" + docids + ") and transactiontype in(27,31)";
            lst = executeSQLQuery( selQuery);



        }
        return new KwlReturnObject(true, "", null, lst, lst.size());
    }
    public KwlReturnObject getbatchUsedinDOFromGRO(String invoiceid, String companyid) throws ServiceException {
        String selQuery = " select batchmapid  from locationbatchdocumentmapping  inner join  grodetails on grodetails.id=documentid where grodetails.grorder=? and grodetails.company=? and documentid not in(select documentid from serialdocumentmapping  inner join  grodetails on grodetails.id=documentid  where grodetails.grorder=? and grodetails.company=?)"; // issue unable to delete GRN as company column become ambigious
        List list = executeSQLQuery( selQuery, new Object[]{invoiceid, companyid,invoiceid, companyid});
        String docids = "";
        List lst = new ArrayList();
        Iterator itrSerial = list.iterator();
        while (itrSerial.hasNext()) {
            String serialstring = itrSerial.next().toString();
            docids += "'" + serialstring + "',";
        }

        if (!StringUtil.isNullOrEmpty(docids)) {
            docids = docids.substring(0, docids.length() - 1);
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
            selQuery = " select batchmapid  from locationbatchdocumentmapping where batchmapid in (" + docids + ") and transactiontype in(27,31)";
            lst = executeSQLQuery( selQuery);



        }
        return new KwlReturnObject(true, "", null, lst, lst.size());
    }

    
    public KwlReturnObject getAvailableQtyOfBatchUsedinDOFromGRO(String invoiceid, String companyid) throws ServiceException {
        String selQuery = " select documentid  from locationbatchdocumentmapping  inner join  grodetails on grodetails.id=documentid where grodetails.grorder=? and grodetails.company=? "; // issue unable to delete GRN as company column become ambigious
        List list = executeSQLQuery( selQuery, new Object[]{invoiceid, companyid});
        String docids = "";
        List lst = new ArrayList();
        Iterator itrSerial = list.iterator();
        while (itrSerial.hasNext()) {
            String serialstring = itrSerial.next().toString();
            docids += "'" + serialstring + "',";
        }

        if (!StringUtil.isNullOrEmpty(docids)) {
            docids = docids.substring(0, docids.length() - 1);
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
            selQuery = " select lbdm.batchmapid from locationbatchdocumentmapping lbdm inner join newproductbatch npb on(lbdm.batchmapid=npb.id  and (npb.quantitydue-lbdm.quantity<0))  where documentid in (" + docids + ")";
            lst = executeSQLQuery( selQuery);

        }
        return new KwlReturnObject(true, "", null, lst, lst.size());
    }
//    public KwlReturnObject getbatchUsedinDOFromGRO(String invoiceid, String companyid) throws ServiceException {
//        String selQuery = "  select distinct(grodetails.id),grodetails.batch  from grodetails "
//                + "  inner join  salespurchasebatchmapping on salespurchasebatchmapping.purchaseBatch=grodetails.batch where grodetails.grorder=? and grodetails.company=?";
//        List list = executeSQLQuery( selQuery, new Object[]{invoiceid, companyid});
//        return new KwlReturnObject(true, "", null, list, list.size());
//    }

    public KwlReturnObject saveGoodsReceiptOrderStatus(String doId, String status) throws ServiceException {
        String query = "update GoodsReceiptOrder set status.ID=? where ID=? ";
        int numRows = executeUpdate( query, new Object[]{status, doId});
        return new KwlReturnObject(true, "Status has been updated successfully.", null, null, numRows);
    }

    public KwlReturnObject getGDOIDFromVendorInvoiceDetails(String soid) throws ServiceException {
        String selQuery = "from GoodsReceiptOrderDetails ge where ge.videtails.ID = ? and ge.grOrder.deleted = false";
        List list = executeQuery( selQuery, new Object[]{soid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPurchaseReturnIDFromVendorInvoiceDetails(String grId) throws ServiceException {
        String selQuery = "from PurchaseReturnDetail prd where prd.videtails.ID = ? and prd.purchaseReturn.deleted = false";
        List list = executeQuery( selQuery, new Object[]{grId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getCreditNoteAgainstVendorGstDetails(String grId) throws ServiceException {
        String selQuery = "from CreditNoteAgainstVendorGst where videtails.ID = ?";
        List list = executeQuery( selQuery, new Object[]{grId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getGROIDFromPOD(String soid) throws ServiceException {
        String selQuery = "from GoodsReceiptOrderDetails ge where ge.podetails.ID = ? and ge.grOrder.deleted = false";
        List list = executeQuery( selQuery, new Object[]{soid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getPRFromPOD(String soid) throws ServiceException {
        String selQuery = "select prd.purchasereturn from prdetails prd inner join grodetails gro on gro.id=prd.grdetails inner join podetails pod on pod.id=gro.podetails where pod.id=?";
        List list = executeSQLQuery( selQuery, new Object[]{soid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getGRODIDFromPOD(String soid,String grorderId) throws ServiceException {
       String selQuery="";
       ArrayList params=new ArrayList();
       params.add(soid);
        if(!StringUtil.isNullOrEmpty(grorderId)){
            params.add(grorderId);
          selQuery = "from GoodsReceiptOrderDetails ge where ge.podetails.ID = ? and ge.grOrder.ID= ? and ge.grOrder.deleted = false";
        }else{
            selQuery = "from GoodsReceiptOrderDetails ge where ge.podetails.ID = ? and ge.grOrder.deleted = false";
        }
        List list = executeQuery( selQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getGRODIDFromSGE(String sgeId,String grorderId) throws ServiceException {
       String selQuery="";
       ArrayList params=new ArrayList();
       params.add(sgeId);
        if(!StringUtil.isNullOrEmpty(grorderId)){
            params.add(grorderId);
          selQuery = "from GoodsReceiptOrderDetails ge where ge.securitydetails.ID = ? and ge.grOrder.ID= ? and ge.grOrder.deleted = false";
        }else{
            selQuery = "from GoodsReceiptOrderDetails ge where ge.securitydetails.ID = ? and ge.grOrder.deleted = false";
        }
        List list = executeQuery( selQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getIDFromGROD(String soid) throws ServiceException {
        String selQuery = "from GoodsReceiptDetail ge where ge.goodsReceiptOrderDetails.ID = ? and ge.goodsReceipt.deleted=false";
        List list = executeQuery( selQuery, new Object[]{soid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getIDFromPRD(String soid) throws ServiceException {
        String selQuery = "from PurchaseReturnDetail prd where prd.grdetails.ID = ? and prd.purchaseReturn.deleted = false";
        List list = executeQuery( selQuery, new Object[]{soid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getGRDIDFromPRD(String soid,String purchasereturnId) throws ServiceException {
        String selQuery ="";
        ArrayList params=new ArrayList();
        params.add(soid);
        if(!StringUtil.isNullOrEmpty(purchasereturnId)){
            params.add(purchasereturnId);
         selQuery = "from PurchaseReturnDetail prd where prd.grdetails.ID = ? and prd.purchaseReturn.ID = ? and prd.purchaseReturn.deleted = false";
        }else{
            selQuery = "from PurchaseReturnDetail prd where prd.grdetails.ID = ? and prd.purchaseReturn.deleted = false";
        }
        List list = executeQuery( selQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
  
    public int approvePendingInvoice(String qid, boolean isbilling, String companyid, String userid) throws ServiceException {
        int approvalLevel = 0;
        User user = (User) get(User.class, userid);

        if (isbilling) {
            BillingGoodsReceipt gr = (BillingGoodsReceipt) get(BillingGoodsReceipt.class, qid);
            approvalLevel = gr.getPendingapproval();
            if (gr.getPendingapproval() < gr.getApprovallevel()) {
                gr.setPendingapproval((gr.getPendingapproval() + 1));
            } else {
                gr.setPendingapproval(Constants.APPROVED);
            }
            gr.setApprover(user);
        } else {
            GoodsReceipt gr = (GoodsReceipt) get(GoodsReceipt.class, qid);
            approvalLevel = gr.getPendingapproval();
            if (gr.getPendingapproval() < gr.getApprovallevel()) {
                gr.setPendingapproval((gr.getPendingapproval() + 1));
            } else {
                gr.setPendingapproval(Constants.APPROVED);
            }

            gr.setApprover(user);
        }

//        String query = "update GoodsReceipt set pendingapproval = 0 where ID=? and company.companyID=?";
//        if(isbilling) {
//            query = "update BillingGoodsReceipt set pendingapproval = 0 where ID=? and company.companyID=?";
//        }
//        int numRows = executeUpdate( query, new Object[]{qid, companyid});
        return approvalLevel;//new KwlReturnObject(true, "Invoice has been updated successfully.", null, null, 1);
    }

    public int pendingApprovalInvoicesCount(String companyid) throws ServiceException {
        String query = "select * from ("
                + " select id from goodsreceipt where deleteflag = 'F' and pendingapproval != 0 and company = ? "
                + " union "
                + " select id from billinggr where deleteflag = 'F' and pendingapproval != 0 and company = ? "
                + ") as test";

        List list = executeSQLQuery( query, new Object[]{companyid, companyid});

        int count = list.size();

        return count;
    }

    public String[] columSortGoodsReceipt(String Col_Name, String Col_Dir) throws ServiceException {
        String[] String_Sort = new String[3];
        if (Col_Name.equals("personname")) {
            String_Sort[0] = " order by name " + Col_Dir;
            String_Sort[1] = ",vendor.name";

        } else if (Col_Name.equals("billno")) {
            String_Sort[0] = " order by gronumber " + Col_Dir;
            String_Sort[1] = ",grorder.gronumber ";


        } else if (Col_Name.equals("date")) {
            String_Sort[0] = " order by grorderdate " + Col_Dir;
            String_Sort[1] = ", grorder.grorderdate";

        }else if (Col_Name.equals("createdon")) {
            //Sorting on order date as well as on creation time to pick latest created GRN.
            String_Sort[0] = " order by grorderdate " + Col_Dir +", grorder.createdon "+Col_Dir;
            String_Sort[1] = ", grorder.grorderdate";

        } else if (Col_Name.equals("agentname")) {
            String_Sort[0] = " order by value " + Col_Dir;
            String_Sort[1] = ", masteritem.value ";
        } else {
            String_Sort[0] = " order by grorderdate " + Col_Dir;
            String_Sort[1] = ", grorder.grorderdate";

        }
        return String_Sort;

    }

    public KwlReturnObject getUnInvoicedGoodsReceiptOrders(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            String ss = (String) request.get(Constants.ss);
            ArrayList params = new ArrayList();
            boolean isUnInvoiced = ((request.get("isUnInvoiced") != null) ? (Boolean) request.get("isUnInvoiced") : false);
            boolean isfavourite = false;
            boolean isprinted = false;
            if (request.get("isfavourite") != null) {
                isfavourite = Boolean.parseBoolean((String) request.get("isfavourite"));
            }
            if (request.get(Constants.MARKED_PRINTED) != null) {
                isprinted = Boolean.parseBoolean((String) request.get(Constants.MARKED_PRINTED));
            }
//            params.add((String) request.get(Constants.companyKey));

            String companyid = AccountingManager.getFilterInString((String) request.get(Constants.companyKey));

            String conditionSQL = " where gro.deleteflag='F' and gro.company in " + companyid + " ";
            String searchJoin = "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                for (int i = 0; i < 3; i++) {
                    params.add(ss + "%");
                }
                params.add("%" + ss + "%");
//                searchJoin = " inner join grodetails on grodetails.grorder = grorder.id ";
                conditionSQL += " and ( gro.gronumber like ? or gro.memo like ? or vendor.name like ? or grod.partno like ? )";

            }
            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                conditionSQL += " and costcenter.id=?";
            }

            if (isfavourite) {
                conditionSQL += " and gro.favouriteflag=true ";
            }
           
            conditionSQL += " and gro.approvestatuslevel = 11";
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);

            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSQL += " and (gro.grorderdate >=? and gro.grorderdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            String orderBy = "";
            String sort_Col = "";
            String sort_Col1 = "";
            String[] stringSort = null;
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                stringSort = columSortGoodsReceipt(Col_Name, Col_Dir);
                orderBy += stringSort[0];
                sort_Col += stringSort[1].replace("grorder.", "gro.");

            } else {
                orderBy = " order by grorderdate desc ";
                sort_Col += ", gro.grorderdate as grorderdate  ";
            }
            String mysqlQuery =" select * from ( select distinct(gro.id), 'false' as withoutinventory " + sort_Col
                    + " from grorder               as gro "
                    + " inner join grodetails      as grod   on grod.grorder = gro.id "
                    + " left join grdetails        as grd    on grod.videtails = grd.id "
                    + " left join goodsreceipt     as gr     on grd.goodsreceipt = gr.id "
                    + " inner join vendor on vendor.id = gro.vendor "
                    + " left join costcenter on costcenter.id = gro.costcenter " + conditionSQL + // " and  grod.videtails is null " +                   
                    //                     " and ( gr.grnumber IS NULL OR gr.grnumber not in "+ 
                    //                     " 		( "+
                    //                     " 				select gr2.grnumber "+
                    //                     " 						from grdetails          as grd2 "+
                    //                     " 						inner join goodsreceipt as gr2     on  grd2.goodsreceipt = gr2.id "+
                    //                     " 						left join grodetails    as grod2   on  grd2.grorderdetails = grod2.id "+
                    //                     " 						left join grorder       as gro2    on  grod2.grorder = gro2.id "+
                    //                     " 				where gr2.company in " + companyid + " and gr2.deleteflag='F' "+
                    //                     " 		) "+
                    //                     " ) "+                     
                    " and  gr.grnumber is null ";
            if(!isUnInvoiced){        
            mysqlQuery += " and not exists  		"
                    + " (  				"
                    + " select null 		"
                    + " from grdetails          as grd2 "
                    + " inner join goodsreceipt as gr2     on  grd2.goodsreceipt = gr2.id "
                    + " left join grodetails    as grod2   on  grd2.grorderdetails = grod2.id "
                    + " left join grorder       as gro2    on  grod2.grorder = gro2.id  	"
                    + " where gr2.company in  " + companyid + "  and gr2.deleteflag='F' and gro2.id = gro.id) "
                    + ") a  ";
            }else{
                
                   mysqlQuery += "and not exists "
                        + "        (     "
                        + "             select null  from grodetails as grodetails3             "
                        + "             left join grdetails as grvd3 on  grodetails3.videtails = grvd3.id "
                        + "             inner join goodsreceipt as gr3 on grvd3.goodsreceipt = gr3.id            "
                        + "             left join grorder as grorder3 on  grodetails3.grorder = grorder3.id                 "
                        + "             where gr3.company in   " + companyid + "   and gr3.deleteflag='F'  and grorder3.id = gro.id )"
                        + " ) a "
                        + " where a.id not in "
                        + "        (    "
                        + "             select gro.id from grorder gro "
                        + "             inner join goodsreceiptorderlinking grl on gro.id = grl.docid  "
                        + "             where gro.company in " + companyid
                        + "             and grl.sourceflag = 0 "
                        + "             and grl.moduleid = 6 "
                        + "             and gro.isopeninpi = 'F' "
                        + "        )    ";
                            
                
            }
            mysqlQuery += orderBy;
//            System.out.println("getUnInvoicedGoodsReceiptOrders - query:"+mysqlQuery);

            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
                if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                    list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getUnInvoicedGoodsReceiptOrders:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject updateGoodsReceiptOrderDetails(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            GoodsReceiptDetail goodsReceiptDetail = (GoodsReceiptDetail) requestParams.get("goodsReceiptDetail");
            GoodsReceiptOrderDetails goodsReceiptOrderDetails = goodsReceiptDetail.getGoodsReceiptOrderDetails();
            goodsReceiptOrderDetails.setVidetails(goodsReceiptDetail);
            saveOrUpdate(goodsReceiptOrderDetails);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updateGoodsReceiptOrderDetails:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    @Override
    public KwlReturnObject updateExchnageforInvoices(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            JournalEntry journalEntry = (JournalEntry) get(JournalEntry.class, (String) requestParams.get("jeid"));
            double exchangeRate = (Double) requestParams.get("exchangerate");
            journalEntry.setExternalCurrencyRate(exchangeRate);
            saveOrUpdate(journalEntry);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updateExchnageforGoodsReceipt:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    @Override
    public KwlReturnObject updateVQLinkflag(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            VendorQuotation quotation = (VendorQuotation) requestParams.get("quotation");
            int num = Integer.parseInt((String) requestParams.get("value"));
            Boolean isopen =requestParams.get("isOpen")!=null?(Boolean)requestParams.get("isOpen"):false;
            quotation.setLinkflag(num);
            quotation.setIsOpen(isopen);           
            saveOrUpdate(quotation);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderImpl.updateVQLinkflag:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    @Override
    public KwlReturnObject updatePOLinkflag(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            PurchaseOrder purchaseOrder = (PurchaseOrder) requestParams.get("purchaseOrder");
            if(requestParams.containsKey("value")){
                int num = Integer.parseInt((String) requestParams.get("value"));
                purchaseOrder.setLinkflag(num);
            }
            Boolean isOpen= (Boolean)requestParams.get("isOpen")!=null?(Boolean)requestParams.get("isOpen"):true;
            purchaseOrder.setIsOpen(isOpen);
            saveOrUpdate(purchaseOrder);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updateSOLinkflag:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    /**
     * to update securuty gate entry isopen flag true while in linking with GR
     * @param requestParams
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject updateSGELinkflag(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            SecurityGateEntry securityGateEntry = (SecurityGateEntry) requestParams.get("securityGateEntry");
            
            if(requestParams.containsKey("value")){
                int num = Integer.parseInt((String) requestParams.get("value"));
                securityGateEntry.setLinkflag(num);
            }
            
            Boolean isOpen= (Boolean)requestParams.get("isOpen")!=null?(Boolean)requestParams.get("isOpen"):true;
            securityGateEntry.setIsOpen(isOpen);
            saveOrUpdate(securityGateEntry);
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updateSGELinkflag:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    public KwlReturnObject updatePurchaseReturnStatus(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            if(requestParams.containsKey("purchasereturn") && requestParams.get("purchasereturn")!=null && requestParams.get("purchasereturn")!="undefined"){
            PurchaseReturn purchasereturn=(PurchaseReturn)  requestParams.get("purchasereturn");
            Boolean isdeletable= (Boolean)requestParams.get("isdeletable")!=null?(Boolean)requestParams.get("isdeletable"):true;
            purchasereturn.setIsdeletable(isdeletable);
            saveOrUpdate(purchasereturn);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updatePurchaseReturnStatus:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    @Override
    public KwlReturnObject updateGRLinkflag(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            GoodsReceiptOrder goodsReceiptOrder = (GoodsReceiptOrder) requestParams.get("goodsReceiptOrder");
            Boolean isOpen= (Boolean)requestParams.get("isOpenInPI")!=null?(Boolean)requestParams.get("isOpenInPI"):true;
            goodsReceiptOrder.setIsOpenInPI(isOpen);
            saveOrUpdate(goodsReceiptOrder);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updateGRLinkflag:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    @Override
    public KwlReturnObject updatePILinkflag(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            GoodsReceipt goodsReceipt = (GoodsReceipt) requestParams.get("goodsReceipt");
            Boolean isOpen= (Boolean)requestParams.get("isOpenInGR")!=null?(Boolean)requestParams.get("isOpenInGR"):true;
            goodsReceipt.setIsOpenInGR(isOpen);
            saveOrUpdate(goodsReceipt);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updatePILinkflag:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    public KwlReturnObject getPurchaseorder(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        params.add(requestParams.get("linkflag"));
        String sqlQuery = "select id from purchaseorder where company=? and linkflag= ? and deleteflag='F'";
        list = executeSQLQuery( sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getGoodsrecipt(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));      
        String sqlQuery = "select goodsreceipt.id from goodsreceipt where goodsreceipt.company=? and goodsreceipt.deleteflag='F'";
        list = executeSQLQuery( sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getGoodsReceiptsAndJE(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        String hqlQuery = "from GoodsReceipt gr where gr.company.companyID=? and gr.journalEntry is not null";
        list = list = executeQuery( hqlQuery,params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getCompanyList() throws ServiceException {
        List list = new ArrayList();
        String sqlQuery = "select companyid from company where deleteflag=0 order by createdon desc";
        list = executeSQLQuery( sqlQuery);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getReceiptOrderDFromPOD(String podid, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select grodetails.id from grodetails inner join grorder on grodetails.grorder=grorder.id "
                    + "where  grorder.company=? and grorder.deleteflag='F' and grodetails.podetails=? "
                    + "union "
                    + "select grodetails.id from grodetails inner join grorder on grodetails.grorder=grorder.id "
                    + "where  grorder.company=? and grorder.deleteflag='F' and grodetails.videtails in "
                    + " ( select grdetails.id from grdetails inner join goodsreceipt on grdetails.goodsreceipt=goodsreceipt.id "
                    + "where goodsreceipt.company=? and goodsreceipt.deleteflag='F' and grdetails.purchaseorderdetail=? )";
            list = executeSQLQuery( query, new Object[]{companyid, podid, companyid, companyid, podid});
//            System.out.println(++Constants.count+" = "+list.size());
//            System.out.println(" "+list.size());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.getReceiptOrderDFromPOD:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public double getReceiptOrderQuantityFromPOD(String podid, String companyid,boolean inSelectedUOM) throws ServiceException {
        List list = new ArrayList();
        double count=0;
        try {
            String selectQuantity=inSelectedUOM ? "grodetails.deliveredQuantity":"grodetails.baseuomdeliveredquantity";
//            String query = "select sum(t1.quantity)  from (select sum("+selectQuantity +") as quantity  from grodetails inner join grorder on grodetails.grorder=grorder.id "
//                    + "where  grorder.company=? and grorder.deleteflag='F' and grodetails.podetails=? "
//                    + "union "
//                    + "select sum("+selectQuantity +") as quantity from grodetails inner join grorder on grodetails.grorder=grorder.id "
//                    + "where  grorder.company=? and grorder.deleteflag='F' and grodetails.videtails in "
//                    + " ( select grdetails.id from grdetails inner join goodsreceipt on grdetails.goodsreceipt=goodsreceipt.id "
//                    + "where goodsreceipt.company=? and goodsreceipt.deleteflag='F' and grdetails.purchaseorderdetail=? ) ) as t1";
//            list = executeSQLQuery( query, new Object[]{companyid, podid, companyid, companyid, podid});
             
            String hql = "select sum("+selectQuantity+") from GoodsReceiptOrderDetails grodetails inner join grodetails.grOrder grorder where grorder.company.companyID=? and grorder.deleted=? and grodetails.podetails.ID=? ";
            list = executeQuery( hql, new Object[]{companyid, false, podid});
            Double totalCnt = 0d;
            if (list != null && !list.isEmpty() && list.get(0) !=null){
                totalCnt = (Double) list.get(0);
            }
            hql = "select sum("+selectQuantity+") from GoodsReceiptOrderDetails grodetails"
                    + " inner join grodetails.grOrder grorder inner join grodetails.videtails grdetails inner join grdetails.goodsReceipt goodsreceipt"
                    + " where goodsreceipt.company.companyID=? and goodsreceipt.deleted=? and grdetails.purchaseorderdetail.ID=? and grorder.company.companyID=? and grorder.deleted=? ";
            list = executeQuery( hql, new Object[]{companyid, false, podid, companyid, false});
            if (list != null && !list.isEmpty() && list.get(0) !=null){
                totalCnt += (Double) list.get(0);
            }
            count = totalCnt.doubleValue();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.getReceiptOrderDFromPOD:" + ex.getMessage(), ex);
        }
        return count;
    }
     
    @Override
    public double getGRODetailQuantityFromProduct(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        double count=0;
        try {
            ArrayList params = new ArrayList();
            String productId = (String) requestParams.get("productId");
            String companyid = (String) requestParams.get("companyid");
            Date startDate = (Date)requestParams.get(Constants.REQ_startdate);
            Date endDate = (Date) requestParams.get(Constants.REQ_enddate);
            String compareUOMId = (String) requestParams.get("compareUOMId");
            boolean inSelectedUOM = (Boolean) requestParams.get("inSelectedUOM");
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            
            String field1 = "", field2 = "", joinquery = "", uomquery = "", datequery = "";
            if(inSelectedUOM){
                field1 = "grodetails.deliveredquantity";
                field2 = "pod.quantity";
            }else{
                field1 = "grodetails.baseuomdeliveredquantity";
                field2 = "pod.baseuomquantity";
            }
            
            params.add(companyid);
            params.add(productId);
            params.add(companyid);
            if(inSelectedUOM){
                joinquery = "inner join uom on uom.id=podetails.uom ";
                uomquery = " and podetails.uom=? ";
                params.add(compareUOMId);
            }
            if(startDate != null  && endDate != null){
                datequery=" and (Date(po.orderdate) >=? and Date(po.orderdate) <=?) ";
                params.add(startDate);
                params.add(endDate);
            }
            String query1 = "(select if(sum("+ field1 +") is not null, sum("+ field1 +"), 0) as deliveredquantity, podetails.id as podetails  "
                + "from grodetails "
                + "inner join grorder on grorder.id=grodetails.grorder "
                    + "inner join podetails on podetails.id=grodetails.podetails "
                    + "inner join purchaseorder as po on po.id=podetails.purchaseorder "
                    + joinquery
                + "where grorder.company=? and grorder.deleteflag=false "
                    + "and podetails.product=? and podetails.company=? and "
                    + "po.deleteflag=false and po.isopeningbalencepo=false  and po.isconsignment=false "
                    + "and po.isfixedassetpo=false  and po.istemplate!=2  and "
                    + "po.approvestatuslevel=11 "
                    + uomquery + datequery
                + " group by grodetails.podetails "
                + ") ";
            
            params.add(companyid);
            params.add(productId);
            params.add(companyid);
            params.add(companyid);
            if(inSelectedUOM){
                params.add(compareUOMId);
            }
            if(startDate != null  && endDate != null){
                datequery=" and (Date(po.orderdate) >=? and Date(po.orderdate) <=?) ";
                params.add(startDate);
                params.add(endDate);
            }
            String query2 = "(select if(sum("+ field1 +") is not null, sum("+ field1 +"), 0) as deliveredquantity, podetails.id as podetails "
                + "from grodetails "
                + "inner join grorder on grorder.id=grodetails.grorder "
                + "inner join grdetails on grdetails.id=grodetails.videtails "
                + "inner join goodsreceipt on goodsreceipt.id=grdetails.goodsreceipt "
                    + "inner join podetails on podetails.id=grdetails.purchaseorderdetail "
                    + "inner join purchaseorder as po on po.id=podetails.purchaseorder "
                    + joinquery
                + "where goodsreceipt.company=? and goodsreceipt.deleteflag=false "
                    + "and podetails.product=? and grorder.company=? and grorder.deleteflag=false "
                    + "and podetails.company=? and po.deleteflag=false and po.isopeningbalencepo=false  and po.isconsignment=false "
                    + "and po.isfixedassetpo=false  and po.istemplate!=2  and "
                    + "po.approvestatuslevel=11 "
                    + uomquery + datequery
                + " group by grodetails.podetails "
                + ") ";
            
            Double totalCnt = 0d;
            String query = "select sum(if(if(table2.deliveredquantity is not null, table2.deliveredquantity, 0) < "+ field2 +", "
                    + field2 +"-if(table2.deliveredquantity is not null, table2.deliveredquantity, 0), 0)) as qty from podetails as pod left join ( "
                        + "select if(sum(deliveredquantity) is not null, sum(deliveredquantity), 0) as deliveredquantity, podetails "
                        + "from ( " + query1 + "union all " + query2 + ")table1 group by podetails "
                    + ")table2 on table2.podetails=pod.id INNER JOIN purchaseorder po ON po.id=pod.purchaseorder where pod.product=? ";
            params.add(productId);
            if(startDate != null  && endDate != null){
                query+=" and (Date(po.orderdate) >=? and Date(po.orderdate) <=?) ";
                params.add(startDate);
                params.add(endDate);
            }
            list = executeSQLQuery(query, params.toArray());
            if (list != null && !list.isEmpty() && list.get(0) !=null){
                totalCnt += (Double) list.get(0);
            }
            count = totalCnt.doubleValue();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getOutStandingPOCountFromPODetail:" + ex.getMessage(), ex);
        }
        return count;
    }
    
    public double getGRODetailQuantityForProduct(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        double count=0;
        try {
            ArrayList params = new ArrayList();
            String productId = (String) requestParams.get("productId");
            String companyid = (String) requestParams.get("companyid");
            Date startDate = (Date)requestParams.get(Constants.REQ_startdate);
            Date endDate = (Date) requestParams.get(Constants.REQ_enddate);
            String compareUOMId = (String) requestParams.get("compareUOMId");
            boolean inSelectedUOM = (Boolean) requestParams.get("inSelectedUOM");
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            
            String field1 = "", field2 = "", joinquery = "", uomquery = "", datequery = "";
            if(inSelectedUOM){
                field1 = "grodetails.deliveredquantity";
                field2 = "pod.quantity";
            }else{
                field1 = "grodetails.baseuomdeliveredquantity";
                field2 = "pod.baseuomquantity";
            }
           
            String conditionalQuery="";
            String conditionalValue="";
            
            if(inSelectedUOM){
                conditionalQuery = "inner join uom on uom.id=pd.uom ";
                conditionalValue = " and pd.uom=? ";
            }
            if (startDate != null && endDate != null) {
                conditionalValue += " and (Date(p.orderdate) >=? and Date(p.orderdate) <=?) ";
            }
            
            Double totalCnt = 0d;

            String query = " SELECT IF(SUM(baseuomquantity) is not null and SUM(baseuomquantity)>=0 , SUM(baseuomquantity), 0) AS qty FROM ( "
                    + " SELECT SUM(pd.baseuomquantity) AS baseuomquantity  FROM purchaseorder p "
                    + " INNER JOIN podetails pd ON pd.purchaseorder=p.id " + conditionalQuery
                    + " WHERE p.company=?  AND pd.product=? " + conditionalValue
                    + " AND p.ispoclosed='F' and p.approvestatuslevel=11 AND p.isfixedassetpo=false AND  p.istemplate!=2 and p.isconsignment=false "
                    + " AND p.deleteflag=false "
                    + " AND pd.islineitemclosed='F'"
                    + " UNION ALL"
                    + " SELECT -sum(grodetails.baseuomdeliveredquantity) AS baseuomquantity  FROM purchaseorder p "
                    + " INNER JOIN podetails pd ON pd.purchaseorder=p.id "
                    + " INNER join grodetails on pd.id=grodetails.podetails "
                    + " INNER JOIN grorder on grorder.id=grodetails.grorder " + conditionalQuery
                    + " WHERE p.company=?  AND pd.product=? " + conditionalValue
                    + " AND p.ispoclosed='F' and p.approvestatuslevel=11 AND p.isfixedassetpo=false AND  p.istemplate!=2 and p.isconsignment=false "
                    + " AND p.deleteflag=false "
                    + " UNION ALL"
                    + " SELECT -SUM(grodetails.baseuomdeliveredquantity) AS baseuomquantity FROM goodsreceipt g "
                    + " INNER JOIN grdetails gr ON g.id=gr.goodsreceipt "
                    + " inner join grodetails ON grodetails.videtails =gr.id"
                    + " inner join podetails pd on pd.id=gr.purchaseorderdetail "
                    + " inner join purchaseorder as p on p.id=pd.purchaseorder " + conditionalQuery
                    + " where p.company=? AND pd.product=? " + conditionalValue
                    + " and p.deleteflag=false and g.deleteflag=false and p.ispoclosed='F' "
                    + " and p.isopeningbalencepo=false  and p.isconsignment=false and p.isfixedassetpo=false  and p.istemplate!=2 "
                    + " and p.approvestatuslevel=11 "
                    + " UNION ALL "
                    + " SELECT SUM(prd.returnquantity) AS baseuomquantity FROM prdetails prd "
                    + " inner JOIN purchasereturn as pr on pr.id = prd.purchasereturn "
                    + " inner JOIN grodetails as grod on grod.id = prd.grdetails "
                    + " inner JOIN podetails as pd on grod.podetails = pd.id "
                    + " inner join purchaseorder as p on p.id=pd.purchaseorder " + conditionalQuery
                    + " where p.company=? AND pd.product=? " + conditionalValue
                    + " and p.deleteflag=false and pr.deleteflag=false and p.ispoclosed='F' "
                    + " and p.isopeningbalencepo=false  and p.isconsignment=false and p.isfixedassetpo=false  and p.istemplate!=2 "
                    + " and p.approvestatuslevel=11 "
                    + " UNION ALL "
                    + " SELECT SUM(prd.returnquantity) AS baseuomquantity FROM prdetails prd "
                    + " inner JOIN purchasereturn as pr on pr.id = prd.purchasereturn "
                    + " inner JOIN grodetails as grod on grod.id = prd.grdetails "
                    + " inner JOIN grdetails as grd on grod.videtails = grd.id "
                    + " inner JOIN podetails as pd on grd.purchaseorderdetail = pd.id "
                    + " inner join purchaseorder as p on p.id=pd.purchaseorder " + conditionalQuery
                    + " where p.company=? AND pd.product=? " + conditionalValue
                    + " and p.deleteflag=false and pr.deleteflag=false and p.ispoclosed='F' "
                    + " and p.isopeningbalencepo=false  and p.isconsignment=false and p.isfixedassetpo=false  and p.istemplate!=2 "
                    + " and p.approvestatuslevel=11) AS tb";
            
            params.add(companyid);
            params.add(productId);
            if (inSelectedUOM) {
                params.add(compareUOMId);
            }
            if(startDate != null  && endDate != null){
                params.add(startDate);
                params.add(endDate);
            }
            params.add(companyid);
            params.add(productId);
            if (inSelectedUOM) {
                params.add(compareUOMId);
            }
            if(startDate != null  && endDate != null){
                params.add(startDate);
                params.add(endDate);
            }
            params.add(companyid);
            params.add(productId);
            if (inSelectedUOM) {
                params.add(compareUOMId);
            }
            if(startDate != null  && endDate != null){
                params.add(startDate);
                params.add(endDate);
            }
            params.add(companyid);
            params.add(productId);
            if (inSelectedUOM) {
                params.add(compareUOMId);
            }
            if(startDate != null  && endDate != null){
                params.add(startDate);
                params.add(endDate);
            }
            
            params.add(companyid);
            params.add(productId);
            if (inSelectedUOM) {
                params.add(compareUOMId);
            }
            if(startDate != null  && endDate != null){
                params.add(startDate);
                params.add(endDate);
            }
    
            list = executeSQLQuery(query, params.toArray());
            if (list != null && !list.isEmpty() && list.get(0) !=null){
                totalCnt += (Double) list.get(0);
            }
            count = totalCnt.doubleValue();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getOutStandingPOCountFromPODetail:" + ex.getMessage(), ex);
        }
        return count;
    }
    
    @Override
    public KwlReturnObject getPurchaseReturnCount(String orderno, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from PurchaseReturn where purchaseReturnNumber=? and company.companyID=?";
        list = executeQuery( q, new Object[]{orderno, companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    public KwlReturnObject getPurchaseReturnDuplicateSIN(JSONObject reqParams) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String receiptno=reqParams.optString("supplierInvoiceNumber");
        String vendor=reqParams.optString("vendor");
        String companyid=reqParams.optString("companyid");
        String srid=reqParams.optString("srid");
        String q = "select purchaseReturnNumber from PurchaseReturn where supplierinvoiceno=? and vendor.ID=? and company.companyID=?";
        if(!StringUtil.isNullOrEmpty(srid)){
            q +=" and ID<>" +"'"+ srid +"'";
        }
        list = executeQuery( q, new Object[]{receiptno, vendor, companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    @Override
    public KwlReturnObject getReceiptOrderDFromPODOptimized(String podid, String companyid) throws ServiceException {
        List list = new ArrayList();
        Set set = new HashSet();
        List list1 = new ArrayList();
        try {
            String hql = "select grodetails.ID from GoodsReceiptOrderDetails grodetails inner join grodetails.grOrder grorder where grorder.company.companyID=? and grorder.deleted=? and grodetails.podetails.ID=? ";
            list1 = executeQuery( hql, new Object[]{companyid, false, podid});
            if (list1 != null && !list1.isEmpty()) {
                set.addAll(list1);
            }
            hql = "select grodetails.ID from GoodsReceiptOrderDetails grodetails"
                    + " inner join grodetails.grOrder grorder inner join grodetails.videtails grdetails inner join grdetails.goodsReceipt goodsreceipt"
                    + " where goodsreceipt.company.companyID=? and goodsreceipt.deleted=? and grdetails.purchaseorderdetail.ID=? and grorder.company.companyID=? and grorder.deleted=? ";
            list1 = executeQuery( hql, new Object[]{companyid, false, podid, companyid, false});
            if (list1 != null && !list1.isEmpty()) {
                set.addAll(list1);
            }
            if (set != null && !set.isEmpty()) {
                list.addAll(set);
            }
//            System.out.println(++Constants.count+" = "+list.size());
//            System.out.println(" "+list.size());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.getReceiptOrderDFromPOD:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getPurchaseReturnInventory(String doid) throws ServiceException {
        List list = new ArrayList();
        String query = "select invd.inventory.ID from PurchaseReturnDetail invd where invd.purchaseReturn.ID=? and invd.company.companyID=invd.inventory.company.companyID";
        list = executeQuery( query, new Object[]{doid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getPurchaseReturnBatches(String doid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "select invd.batch.id from PurchaseReturnDetail invd where invd.purchaseReturn.ID=? and invd.company.companyID=?";
        list = executeQuery( query, new Object[]{doid, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject deletePurchaseReturnDetails(String doid, String companyid) throws AccountingException {
        try {
            ArrayList params8 = new ArrayList();
            params8.add(doid);
            params8.add(companyid);
            String myquery = " select id from prdetails where purchasereturn in (select id from purchasereturn where id=? and company = ?) ";
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
            /**
             *  Delete GST Fields for India.
             */
            deleteGstTaxClassDetails(idStrings);
            ArrayList params1 = new ArrayList();
            String deletecustomdetails = "delete  from prdetailscustomdata where prdetailsid in (" + idStrings + ")";
            int numRows1 = executeSQLUpdate( deletecustomdetails, params1.toArray());
            String delQuery = "delete from PurchaseReturnDetail dod where dod.purchaseReturn.ID=? and dod.company.companyID=?";
            int numRows = executeUpdate( delQuery, new Object[]{doid, companyid});
            return new KwlReturnObject(true, "Purchase Return Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            // throw ServiceException.FAILURE("Cannot Edit Purchase Return as it is already used in other Transactions.", ex);//+ex.getMessage(), ex);
            throw new AccountingException("Cannot Edit Purchase Return as it is already used in other Transactions.", ex);
        }
    }

    public KwlReturnObject savePurchaseReturn(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        String companyid = "";
        try {
            String srid = (String) dataMap.get("id");
            if (dataMap.containsKey("companyid")) {
                companyid = (String) dataMap.get("companyid");
            }
            PurchaseReturn purchaseReturn = new PurchaseReturn();

            if (StringUtil.isNullOrEmpty(srid)) {
                purchaseReturn.setDeleted(false);
                 if (dataMap.containsKey("createdby")) {
                    User createdby = dataMap.get("createdby") == null ? null : (User) get(User.class, (String) dataMap.get("createdby"));
                    purchaseReturn.setCreatedby(createdby);
                }
                if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    purchaseReturn.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("createdon")) {
                    purchaseReturn.setCreatedon((Long) dataMap.get("createdon"));
                }
                if (dataMap.containsKey("updatedon")) {
                    purchaseReturn.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            } else {
                purchaseReturn = (PurchaseReturn) get(PurchaseReturn.class, srid);
                 if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    purchaseReturn.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("updatedon")) {
                    purchaseReturn.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            }
            if (dataMap.containsKey(Constants.SEQFORMAT)) {
                purchaseReturn.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) dataMap.get(Constants.SEQFORMAT)));
            }
            if (dataMap.containsKey(Constants.SEQNUMBER)) {
                purchaseReturn.setSeqnumber(Integer.parseInt(dataMap.get(Constants.SEQNUMBER).toString()));
            }
            if (dataMap.containsKey(Constants.DATEPREFIX) && dataMap.get(Constants.DATEPREFIX) != null) {
                purchaseReturn.setDatePreffixValue((String) dataMap.get(Constants.DATEPREFIX));
            }
            if (dataMap.containsKey(Constants.DATEAFTERPREFIX) && dataMap.get(Constants.DATEAFTERPREFIX) != null) {
                purchaseReturn.setDateAfterPreffixValue((String) dataMap.get(Constants.DATEAFTERPREFIX));
            }
            if (dataMap.containsKey(Constants.DATESUFFIX) && dataMap.get(Constants.DATESUFFIX) != null) {
                purchaseReturn.setDateSuffixValue((String) dataMap.get(Constants.DATESUFFIX));
            }
            if (dataMap.containsKey("entrynumber")) {
                purchaseReturn.setPurchaseReturnNumber((String) dataMap.get("entrynumber"));
            }
            if (dataMap.containsKey("autogenerated")) {
                purchaseReturn.setAutoGenerated((Boolean) dataMap.get("autogenerated"));
            }
            if (dataMap.containsKey("memo")) {
                purchaseReturn.setMemo((String) dataMap.get("memo"));
            }
            if (dataMap.containsKey("externalCurrencyRate")) {
                purchaseReturn.setExternalCurrencyRate((Double)dataMap.get("externalCurrencyRate"));
            }
            if (dataMap.containsKey("posttext")) {
                purchaseReturn.setPostText((String) dataMap.get("posttext"));
            }
            if (dataMap.containsKey("vendor")) {
                Vendor vendor = dataMap.get("vendor") == null ? null : (Vendor) get(Vendor.class, (String) dataMap.get("vendor"));
                purchaseReturn.setVendor(vendor);
            }
            if (dataMap.containsKey("orderdate")) {
                purchaseReturn.setOrderDate((Date) dataMap.get("orderdate"));
            }
            if (dataMap.containsKey("shipdate")) {
                purchaseReturn.setShipdate((Date) dataMap.get("shipdate"));
            }
            if (dataMap.containsKey("shipvia")) {
                purchaseReturn.setShipvia((String) dataMap.get("shipvia"));
            }
            if (dataMap.containsKey("fob")) {
                purchaseReturn.setFob((String) dataMap.get("fob"));
            }

            if (dataMap.containsKey("costCenterId")) {
                CostCenter costCenter = dataMap.get("costCenterId") == null ? null : (CostCenter) get(CostCenter.class, (String) dataMap.get("costCenterId"));
                purchaseReturn.setCostcenter(costCenter);
            } else {
                purchaseReturn.setCostcenter(null);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                purchaseReturn.setCompany(company);
            }
            if (dataMap.containsKey("prdetails")) {
                if (dataMap.get("prdetails") != null) {
                    purchaseReturn.setRows((Set<PurchaseReturnDetail>) dataMap.get("prdetails"));
                }
            }
            if (dataMap.containsKey("currencyid") && dataMap.get("currencyid") != null) {
                purchaseReturn.setCurrency((KWLCurrency) get(KWLCurrency.class, (String) dataMap.get("currencyid")));
            }
            if (dataMap.containsKey("isNoteAlso") && dataMap.get("isNoteAlso") != null) {
                purchaseReturn.setIsNoteAlso((Boolean) dataMap.get("isNoteAlso"));
            }
            if (dataMap.containsKey("gstIncluded") && dataMap.get("gstIncluded") !=null) {
                purchaseReturn.setGstIncluded((Boolean) dataMap.get("gstIncluded"));
            }
            if (dataMap.containsKey(Constants.isApplyTaxToTerms) && dataMap.get(Constants.isApplyTaxToTerms) != null) {
                purchaseReturn.setApplyTaxToTerms((Boolean) dataMap.get(Constants.isApplyTaxToTerms));
            }
            if (dataMap.containsKey(TAXID)) {
                Tax tax = dataMap.get(TAXID) == null ? null : (Tax) get(Tax.class, (String) dataMap.get(TAXID));
                purchaseReturn.setTax(tax);
            }
            if (dataMap.containsKey("isfavourite")) {
                if (dataMap.get("isfavourite") != null) {
                    purchaseReturn.setFavourite(Boolean.parseBoolean(dataMap.get("isfavourite").toString()));
                }
            }
            if (dataMap.containsKey("isConsignment") && dataMap.get("isConsignment") != null) {
                purchaseReturn.setIsconsignment((Boolean) dataMap.get("isConsignment"));
            }
            if (dataMap.containsKey("isFixedAsset") && dataMap.get("isFixedAsset") != null) {
                purchaseReturn.setFixedAsset((Boolean) dataMap.get("isFixedAsset"));
            }
            if (dataMap.containsKey("totalamountinbase") && dataMap.get("totalamountinbase") != null) {
                purchaseReturn.setTotalamountinbase(authHandler.round(Double.valueOf(dataMap.get("totalamountinbase").toString()), companyid));
            }

            if (dataMap.containsKey("totalamount") && dataMap.get("totalamount") != null) { 
                purchaseReturn.setTotalamount(authHandler.round(Double.valueOf(dataMap.get("totalamount").toString()), companyid));
            }

            if (dataMap.containsKey("discountinbase") && dataMap.get("discountinbase") != null) { // Discount in Base
                purchaseReturn.setDiscountinbase(authHandler.round(Double.valueOf(dataMap.get("discountinbase").toString()), companyid));
            }

            if (dataMap.containsKey("totallineleveldiscount") && dataMap.get("totallineleveldiscount") != null) { // Discount
                purchaseReturn.setTotallineleveldiscount(authHandler.round(Double.valueOf(dataMap.get("totallineleveldiscount").toString()), companyid));
            }
            if (dataMap.containsKey("formtype") && dataMap.get("formtype") !=null) {
                purchaseReturn.setFormtype((String) dataMap.get("formtype"));
            }
            if (dataMap.containsKey(Constants.SUPPLIERINVOICENO) && dataMap.get(Constants.SUPPLIERINVOICENO) != null) {
                purchaseReturn.setSupplierInvoiceNo((String) dataMap.get(Constants.SUPPLIERINVOICENO));
            }
            if (dataMap.containsKey(Constants.MVATTRANSACTIONNO) && dataMap.get(Constants.MVATTRANSACTIONNO) != null) {
                purchaseReturn.setMvatTransactionNo((String) dataMap.get(Constants.MVATTRANSACTIONNO));
            }

            purchaseReturn.setTemplateid((Projreport_Template) get(Projreport_Template.class, Constants.HEADER_IMAGE_TEMPLATE_ID));
            if (dataMap.containsKey("gstapplicable") && dataMap.get("gstapplicable") != null) {  // If New GST Appliled
                purchaseReturn.setIsIndGSTApplied((Boolean) dataMap.get("gstapplicable"));
            }
            if (dataMap.containsKey(Constants.EWAYApplicable) && dataMap.get(Constants.EWAYApplicable) != null) { 
                purchaseReturn.setEwayapplicable((Boolean) dataMap.get(Constants.EWAYApplicable));
            }
            saveOrUpdate(purchaseReturn);
            list.add(purchaseReturn);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("savePurchaseReturn : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject savePurchaseReturnDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String srdid = (String) dataMap.get("id");

            PurchaseReturnDetail purchaseReturnDetail = new PurchaseReturnDetail();
            if (!StringUtil.isNullOrEmpty(srdid)) {
                purchaseReturnDetail = (PurchaseReturnDetail) get(PurchaseReturnDetail.class, srdid);
            }

            if (dataMap.containsKey("srid")) {
                PurchaseReturn purchaseReturn = dataMap.get("srid") == null ? null : (PurchaseReturn) get(PurchaseReturn.class, (String) dataMap.get("srid"));
                purchaseReturnDetail.setPurchaseReturn(purchaseReturn);
            }
            if (dataMap.containsKey("srno")) {
                purchaseReturnDetail.setSrno((Integer) dataMap.get("srno"));
            }
            if (dataMap.containsKey("quantity")) {
                purchaseReturnDetail.setActualQuantity(authHandler.roundQuantity((Double) dataMap.get("quantity"),(String) dataMap.get("companyid")));
            }
            if (dataMap.containsKey("returnquantity")) {
                purchaseReturnDetail.setReturnQuantity(authHandler.roundQuantity((Double) dataMap.get("returnquantity"), (String) dataMap.get("companyid")));
            }
            if (dataMap.containsKey("uomid")) {
                purchaseReturnDetail.setUom((UnitOfMeasure) get(UnitOfMeasure.class, dataMap.get("uomid").toString()));
            }
            if (dataMap.containsKey("baseuomrate") && dataMap.get("baseuomrate") != null && dataMap.get("baseuomrate") != "") {
                purchaseReturnDetail.setBaseuomrate((Double) dataMap.get("baseuomrate"));
//            } else {
//                groDetail.setBaseuomrate(1);
            }
            if (dataMap.containsKey("baseuomquantity") && dataMap.get("baseuomquantity") != null && dataMap.get("baseuomquantity") != "") {
                purchaseReturnDetail.setBaseuomquantity(authHandler.roundQuantity((Double) dataMap.get("baseuomquantity"), (String) dataMap.get("companyid")));
//            } else {
//                if (dataMap.containsKey("quantity")) {
//                    groDetail.setBaseuomquantity((Double) dataMap.get("quantity"));
//                }
            }
            if (dataMap.containsKey("baseuomreturnquantity") && dataMap.get("baseuomreturnquantity") != null && dataMap.get("baseuomreturnquantity") != "") {
                purchaseReturnDetail.setBaseuomquantity( authHandler.roundQuantity((Double) dataMap.get("baseuomreturnquantity"), (String) dataMap.get("companyid")));
//            } else {
//                if (dataMap.containsKey("deliveredquantity")) {
//                    groDetail.setBaseuomquantity((Double) dataMap.get("deliveredquantity"));
//                }
            }
            if (dataMap.containsKey("remark")) {
                purchaseReturnDetail.setRemark(StringUtil.DecodeText(StringUtil.isNullOrEmpty((String) dataMap.get("remark")) ? "" : (String) dataMap.get("remark")));
            }
            if (dataMap.containsKey("description")) {
                purchaseReturnDetail.setDescription(StringUtil.DecodeText((String) dataMap.get("description")));
            }
            if (dataMap.containsKey("productid")) {
                Product product = dataMap.get("productid") == null ? null : (Product) get(Product.class, (String) dataMap.get("productid"));
                purchaseReturnDetail.setProduct(product);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                purchaseReturnDetail.setCompany(company);
            }
            if (dataMap.containsKey("GoodReceiptDetail")) {
                purchaseReturnDetail.setGrdetails((GoodsReceiptOrderDetails) dataMap.get("GoodReceiptDetail"));
            }
            if (dataMap.containsKey("InvoiceDetail")) {
                purchaseReturnDetail.setVidetails((GoodsReceiptDetail) dataMap.get("InvoiceDetail"));
            }

            if (dataMap.containsKey("reason") && dataMap.get("reason") != null) {
                MasterItem masterItem = dataMap.get("reason") == null ? null : (MasterItem) get(MasterItem.class, (String) dataMap.get("reason"));
                purchaseReturnDetail.setReason(masterItem);
            }
            
            if (dataMap.containsKey("prtaxid")) {
                Tax tax = dataMap.get("prtaxid") == null ? null : (Tax) get(Tax.class, (String) dataMap.get("prtaxid"));
                purchaseReturnDetail.setTax(tax);
            }
            
            if (dataMap.containsKey("taxamount") && dataMap.get("taxamount") != null) {
                purchaseReturnDetail.setRowTaxAmount((Double) dataMap.get("taxamount"));
            }
            
            if (dataMap.containsKey("discount")) {
                purchaseReturnDetail.setDiscount((Double) dataMap.get("discount"));
            }
            
            if (dataMap.containsKey("discountispercent")) {
                purchaseReturnDetail.setDiscountispercent((Integer) dataMap.get("discountispercent"));
            }
            
            if (dataMap.containsKey("Inventory")) {
                purchaseReturnDetail.setInventory((Inventory) dataMap.get("Inventory"));
            }
            if (dataMap.containsKey("partno")) {
                purchaseReturnDetail.setPartno(StringUtil.DecodeText((String) dataMap.get("partno")));
            } else {
                purchaseReturnDetail.setPartno("");
            }
            if (dataMap.containsKey("invstoreid")) {
                purchaseReturnDetail.setInvstoreid((String) dataMap.get("invstoreid"));
            } else {
                purchaseReturnDetail.setInvstoreid("");
            }
            if (dataMap.containsKey("invlocationid")) {
                purchaseReturnDetail.setInvlocid((String) dataMap.get("invlocationid"));
            } else {
                purchaseReturnDetail.setInvlocid("");
            }
            if (dataMap.containsKey("batch")) {
                ProductBatch productBatch = dataMap.get("batch") == null ? null : (ProductBatch) get(ProductBatch.class, (String) dataMap.get("batch"));
                if (productBatch != null) {
                    purchaseReturnDetail.setBatch(productBatch);
                }
            }
            if (dataMap.containsKey("rate") && dataMap.get("rate") != null) {
                purchaseReturnDetail.setRate((double) dataMap.get("rate"));
            }
            if(dataMap.containsKey("rateIncludingGst") && dataMap.get("rateIncludingGst") != null){
                purchaseReturnDetail.setRateincludegst((Double) dataMap.get("rateIncludingGst"));
            }
            if (dataMap.containsKey("priceSource") && dataMap.get("priceSource") != null) {
                purchaseReturnDetail.setPriceSource((String) dataMap.get("priceSource"));
            }
            if (dataMap.containsKey("pricingbandmasterid") && dataMap.get("pricingbandmasterid") != null) {
                purchaseReturnDetail.setPricingBandMasterid((String) dataMap.get("pricingbandmasterid"));
            }
            if (dataMap.containsKey("recTermAmount") && !StringUtil.isNullOrEmpty(dataMap.get("recTermAmount").toString())) {
                double recTermAmount = Double.parseDouble(dataMap.get("recTermAmount").toString());
                purchaseReturnDetail.setRowtermamount(recTermAmount);
            }
            if (dataMap.containsKey("OtherTermNonTaxableAmount") && !StringUtil.isNullOrEmpty(dataMap.get("OtherTermNonTaxableAmount").toString())) {
                double OtherTermNonTaxableAmount = Double.parseDouble(dataMap.get("OtherTermNonTaxableAmount").toString());
                purchaseReturnDetail.setOtherTermNonTaxableAmount(OtherTermNonTaxableAmount);
            }
            if (dataMap.containsKey("purchasesjedetailid") && dataMap.get("purchasesjedetailid") != null) {
                purchaseReturnDetail.setPurchasesJEDetail((JournalEntryDetail) get(JournalEntryDetail.class, dataMap.get("purchasesjedetailid").toString()));
            }
            if (dataMap.containsKey("inventoryjedetailid") && dataMap.get("inventoryjedetailid") != null) {
                purchaseReturnDetail.setInventoryJEdetail((JournalEntryDetail) get(JournalEntryDetail.class, dataMap.get("inventoryjedetailid").toString()));
            }
            if (dataMap.containsKey("tdsAssessableAmount") && dataMap.get("tdsAssessableAmount") != null) {
                purchaseReturnDetail.setTdsAssessableAmount((double) dataMap.get("tdsAssessableAmount"));
            }
            if (dataMap.containsKey("natureofpayment") && dataMap.get("natureofpayment") != null) {
                purchaseReturnDetail.setNatureOfPayment((MasterItem) dataMap.get("natureofpayment"));
            }
            if (dataMap.containsKey("tdsruleid") && dataMap.get("tdsruleid") != null) {
                purchaseReturnDetail.setTdsRuleId((int) dataMap.get("tdsruleid"));
            }
            if (dataMap.containsKey("tdspercentage") && dataMap.get("tdspercentage") != null) {
                purchaseReturnDetail.setTdsRate((double) dataMap.get("tdspercentage"));
            }
            if (dataMap.containsKey("tdsamount") && dataMap.get("tdsamount") != null) {
                purchaseReturnDetail.setTdsLineAmount((double) dataMap.get("tdsamount"));
            }
            if (dataMap.containsKey("tdspayableaccount") && dataMap.get("tdspayableaccount") != null) {
                purchaseReturnDetail.setTdsPayableAccount((Account) dataMap.get("tdspayableaccount"));
            }
            if (dataMap.containsKey(Constants.isUserModifiedTaxAmount) && dataMap.get(Constants.isUserModifiedTaxAmount) != null) {
                purchaseReturnDetail.setIsUserModifiedTaxAmount((boolean) dataMap.get(Constants.isUserModifiedTaxAmount));
            }
            saveOrUpdate(purchaseReturnDetail);
            list.add(purchaseReturnDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("savePurchaseReturnDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject deletePurchaseReturn(String srid, String companyid) throws ServiceException {
        String query = "update PurchaseReturn set deleted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{srid, companyid});
        return new KwlReturnObject(true, "Purchase Return has been deleted successfully.", null, null, numRows);
    }

    
     
     public void saveBatchAmountDue(HashMap<String, Object> productbatchMap) throws ServiceException {
        try {
            NewProductBatch productBatch = new NewProductBatch();
            String itemID = (String) productbatchMap.get("id");
            if (productbatchMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                productBatch = (NewProductBatch) get(NewProductBatch.class, itemID);
                Double itemQty = (Double) productbatchMap.get("qty");
                if (productbatchMap.containsKey("quantity") && productbatchMap.get("quantity") != null) {
                    Double quantity = (Double) productbatchMap.get("quantity");
                    productBatch.setQuantity(productBatch.getQuantity() + quantity);
                }

                productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() + itemQty),productBatch.getCompany().getCompanyID()));

            }
             //If we are updating for same batch no need to delete
            if(productBatch.getQuantity()==0 && productBatch.getQuantitydue()==0 && !productBatch.getId().equalsIgnoreCase(itemID)){
                delete(productBatch);
            }else{
                saveOrUpdate(productBatch);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveBatchAmountDue : " + ex.getMessage(), ex);
        }
    }
    public void saveSerialAmountDue(HashMap<String, Object> productSerialMap) throws ServiceException {
        try {
            NewBatchSerial newBatchSerial = new NewBatchSerial();
            String itemID = (String) productSerialMap.get("id");
            if (productSerialMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                newBatchSerial = (NewBatchSerial) get(NewBatchSerial.class, itemID);
                Double itemQty = Double.parseDouble((String)productSerialMap.get("qty"));
                Double lockQuantity = Double.parseDouble((String)productSerialMap.get("lockquantity"));
                newBatchSerial.setQuantitydue(newBatchSerial.getQuantitydue() + itemQty);
                newBatchSerial.setLockquantity(lockQuantity);
            }
            if (productSerialMap.containsKey("purchasereturn") && productSerialMap.get("purchasereturn") != null) {
                newBatchSerial.setIspurchasereturn((Boolean) productSerialMap.get("purchasereturn"));
            }
            saveOrUpdate(newBatchSerial);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveSerialAmountDue : " + ex.getMessage(), ex);
        }
    }
   public KwlReturnObject deletePurchasesBatchSerialDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String delQuery = "", delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuerypb = "", delQuery5 = "", delQuery6, delQuery15 = "", delQueryBatch = "", delQuerySerail = "", delQueryBmap = "";
        int numtotal = 0, numRows5 = 0;
        String batchserialids = "", batchids = "";
        String serialmapids = "", docids = "";
        String batchmapids = "";
       boolean isnegativestockforlocwar = false;
       if (requestParams.containsKey("isnegativestockforlocwar") && requestParams.get("isnegativestockforlocwar") != null) {
           isnegativestockforlocwar = Boolean.parseBoolean(requestParams.get("isnegativestockforlocwar").toString());
       }
        ArrayList params13 = new ArrayList();
        params13.add(requestParams.get("companyid"));
        params13.add(requestParams.get("prid"));
        String myquery3 = " select id from prdetails where purchasereturn in (select id from purchasereturn where company = ? and id=? ) ";
        List list3 = executeSQLQuery( myquery3, params13.toArray());
        Iterator itr3 = list3.iterator();
        while (itr3.hasNext()) {
            String batchstring = itr3.next().toString();
            docids += "'" + batchstring + "',";
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
            docids = docids.substring(0, docids.length() - 1);
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
            ArrayList params14 = new ArrayList();
            String myquery4 = " select batchmapid,id from locationbatchdocumentmapping where documentid in (" + docids + ") ";
            String myquery5 = " select serialid,id from serialdocumentmapping where documentid in (" + docids + ") ";


            List list4 = executeSQLQuery( myquery4, params14.toArray());
            Iterator itr4 = list4.iterator();
            while (itr4.hasNext()) {
                Object[] objArr = (Object[]) itr4.next();
                LocationBatchDocumentMapping locationBatchDocumentMapping = (LocationBatchDocumentMapping) get(LocationBatchDocumentMapping.class, (String) objArr[1]);
                if (locationBatchDocumentMapping != null) {
                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    batchUpdateQtyMap.put("qty", locationBatchDocumentMapping.getQuantity());
                    String productid=locationBatchDocumentMapping.getBatchmapid() != null ?locationBatchDocumentMapping.getBatchmapid().getProduct():null;
                    boolean isBatchSerialforProduct=false;
                    if(!StringUtil.isNullOrEmpty(productid)){
                        Product product= (Product) get(Product.class, productid);
                        isBatchSerialforProduct= product != null ? (product.isIsBatchForProduct() || product.isIsSerialForProduct()) :false;
                    } // Now we checking negative stock at item level. if batch or serial is activated we are not allowing negative stock
                    if (isnegativestockforlocwar && !isBatchSerialforProduct) {
                        batchUpdateQtyMap.put("quantity", locationBatchDocumentMapping.getQuantity());
                    }
                    if (locationBatchDocumentMapping.getBatchmapid() != null) {
                        batchUpdateQtyMap.put("id", locationBatchDocumentMapping.getBatchmapid().getId());
                        saveBatchAmountDue(batchUpdateQtyMap);
                    }
                }
                batchmapids += "'" + objArr[0] + "',";

            }
            if (!StringUtil.isNullOrEmpty(batchmapids)) {
                batchmapids = batchmapids.substring(0, batchmapids.length() - 1);
            }
            list4 = executeSQLQuery( myquery5, params14.toArray());
            itr4 = list4.iterator();
            while (itr4.hasNext()) {
                Object[] objArr = (Object[]) itr4.next();
                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) get(SerialDocumentMapping.class, (String) objArr[1]);
                if (serialDocumentMapping != null) {
                    HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                    serialUpdateQtyMap.put("qty", "1");
                    serialUpdateQtyMap.put("lockquantity", "0");
                    serialUpdateQtyMap.put("id", serialDocumentMapping.getSerialid().getId());
                    serialUpdateQtyMap.put("purchasereturn", false);
                    saveSerialAmountDue(serialUpdateQtyMap);
                }
                serialmapids += "'" + objArr[0] + "',";
            }
            if (!StringUtil.isNullOrEmpty(serialmapids)) {
                serialmapids = serialmapids.substring(0, serialmapids.length() - 1);
           }
           String serialDocumentMappingId = getSerialDocumentIds(list4);
           if (!StringUtil.isNullOrEmpty(serialDocumentMappingId)) {
               serialDocumentMappingId = serialDocumentMappingId.substring(0, serialDocumentMappingId.length() - 1);
               ArrayList params1 = new ArrayList();
               delQuery1 = "delete  from serialcustomdata where serialdocumentmappingid in (" + serialDocumentMappingId + ")";
               int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());
           }
           
             ArrayList params15 = new ArrayList();
            delQuerypb = "delete  from locationbatchdocumentmapping where documentid in (" + docids + ") ";
            int numRows = executeSQLUpdate( delQuerypb, params15.toArray());

            delQuerypb = "delete  from serialdocumentmapping where documentid in (" + docids + ") ";
            numRows = executeSQLUpdate( delQuerypb, params15.toArray());

       }
     
        return new KwlReturnObject(true, "Delivery Order has been deleted successfully.", null, null, numtotal);
    }
        
   public KwlReturnObject deletePurchaseReturnPermanent(HashMap<String, Object> requestParams) throws ServiceException {

        String delQuery = "", delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuery4 = "", delQuery5 = "", delQuery6, delQuery7 = "", delQuery8 = "", delQuery9 = "", batchserialids = "", batchids = "";
        int numtotal = 0, numRows8 = 0, numRows9 = 0, numRows10 = 0, numRows11 = 0;
        try {
            if (requestParams.containsKey("prid") && requestParams.containsKey("companyid")) {

                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("companyid"));
                params8.add(requestParams.get("prid"));
                String myquery = "select id from prdetails where purchasereturn in (select id from purchasereturn where company = ? and id=?)";
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
                
                String linkids = (String) requestParams.get("linkIDs");
                if (!StringUtil.isNullOrEmpty(linkids)) {
                    String linksIdsArr[] = linkids.split(",");
                    for (String linkid : linksIdsArr) {
                        if (!StringUtil.isNullOrEmpty(linkid)) {
                            if (requestParams.get("isLinkedWithGR").equals(true)) {
                                GoodsReceiptOrder goodsreceipt = (GoodsReceiptOrder) get(GoodsReceiptOrder.class, linkid);
                                goodsreceipt.setIsOpenInPR(true);
                                //String doUpdateQuery = "Update grorder set isopeninpr='T' where id=? and company=?";
                                //executeSQLUpdate(doUpdateQuery, new Object[]{linkid, requestParams.get("companyid")});
                            } else if (requestParams.get("isLinkedWithVI").equals(true)) {
                                GoodsReceipt goodsreceipt = (GoodsReceipt) get(GoodsReceipt.class, linkid);
                                goodsreceipt.setIsOpenInPR(true);
                                //String doUpdateQuery = "Update goodsreceipt set isopeninpr='T' where id=? and company=?";
                                // executeSQLUpdate(doUpdateQuery, new Object[]{linkid, requestParams.get("companyid")});
                            }
                        }
                    }
                }
                
                ArrayList params13 = new ArrayList();
                params13.add(requestParams.get("companyid"));
                params13.add(requestParams.get("prid"));
                String myquery3 = " select batch from prdetails where purchasereturn in (select id from purchasereturn where company = ? and id=? )  and batch is not null";
                List list3 = executeSQLQuery( myquery3, params13.toArray());
                Iterator itr3 = list3.iterator();
                while (itr3.hasNext()) {

                    String batchstring = itr3.next().toString();
                    batchids += "'" + batchstring + "',";
                }
                if (!StringUtil.isNullOrEmpty(batchids)) {
                    batchids = batchids.substring(0, batchids.length() - 1);
                }

                if (!StringUtil.isNullOrEmpty(batchids)) {
                    ArrayList params14 = new ArrayList();
                    delQuery9 = " select id from batchserial where batch in (" + batchids + ") ";
                    List list4 = executeSQLQuery( delQuery9, params14.toArray());
                    Iterator itr4 = list4.iterator();

                    while (itr4.hasNext()) {

                        String batchstringids = itr4.next().toString();
                        batchserialids += "'" + batchstringids + "',";
                    }
                    if (!StringUtil.isNullOrEmpty(batchserialids)) {
                        batchserialids = batchserialids.substring(0, batchserialids.length() - 1);
                    }
                }
                if (!StringUtil.isNullOrEmpty(batchserialids)) {
                    ArrayList params16 = new ArrayList();
                    delQuery4 = "delete  from batchserialmapping where salesSerial in (" + batchserialids + ") ";
                    numRows9 = executeSQLUpdate( delQuery4, params16.toArray());
                }
                if (!StringUtil.isNullOrEmpty(batchserialids)) {
                    ArrayList params17 = new ArrayList();
                    delQuery7 = "delete  from batchserial where id in (" + batchserialids + ") ";
                    numRows10 = executeSQLUpdate( delQuery7, params17.toArray());
                }

                ArrayList params5 = new ArrayList();
                params5.add(requestParams.get("companyid"));
                params5.add(requestParams.get("prid"));
                delQuery5 = "delete from prdetails where purchasereturn in (select id from purchasereturn where company =?  and id=?)";
                int numRows5 = executeSQLUpdate( delQuery5, params5.toArray());
                boolean isMRPModuleActivated = false;
                boolean isPerpetualValuationActivated = false;
               
                String inventoryjeid = "";
                
                if (requestParams.containsKey("isPerpetualValuationActivated") && requestParams.get("isPerpetualValuationActivated") != null) {
                    isPerpetualValuationActivated = Boolean.parseBoolean(requestParams.get("isPerpetualValuationActivated").toString());
                }
                if (requestParams.containsKey("isMRPModuleActivated") && requestParams.get("isMRPModuleActivated") != null) {
                    isMRPModuleActivated = Boolean.parseBoolean(requestParams.get("isMRPModuleActivated").toString());
                }
                // delete PR permanently
                if (isMRPModuleActivated || isPerpetualValuationActivated) {
                    if (requestParams.containsKey("inventoryjeid") && requestParams.get("inventoryjeid") != null && !StringUtil.isNullOrEmpty(requestParams.get("inventoryjeid").toString())) {
                        inventoryjeid = requestParams.get("inventoryjeid").toString();
                        String query = "update purchasereturn set inventoryje=NULL where id = ? and company = ?";
                        executeSQLUpdate(query, new Object[]{requestParams.get("prid"), requestParams.get("companyid")});
                        ArrayList params1 = new ArrayList();
                        params1.add(requestParams.get("companyid"));
                        delQuery1 = "delete  from accjedetailcustomdata where jedetailId in (select id from jedetail where company = ? and journalEntry in ('" + inventoryjeid + "'))";
                        executeSQLUpdate(delQuery1, params1.toArray());
                        ArrayList params3 = new ArrayList();
                        params3.add(requestParams.get("companyid"));
                        delQuery3 = "delete from jedetail where company = ? and journalEntry in ('" + inventoryjeid + "') ";
                        executeSQLUpdate(delQuery3, params3.toArray());
                        ArrayList params4 = new ArrayList();
                        delQuery4 = "delete from journalentry where id  in ('" + inventoryjeid + "')";
                        executeSQLUpdate(delQuery4, params4.toArray());
                        ArrayList params2 = new ArrayList();
                        delQuery2 = "delete  from accjecustomdata where journalentryId in ('" + inventoryjeid + "')";
                        executeSQLUpdate(delQuery2, params2.toArray());
                    }
                }

                if (!StringUtil.isNullOrEmpty(batchids)) {
                    ArrayList params18 = new ArrayList();
                    delQuery8 = "delete  from returnbatchmapping where batchmap in (" + batchids + ") ";
                    numRows11 = executeSQLUpdate( delQuery8, params18.toArray());
                }

                if (!StringUtil.isNullOrEmpty(batchids)) {  //delete batch detail for both serial and batch option
                    ArrayList params15 = new ArrayList();
                    delQuery3 = "delete  from productbatch where id in (" + batchids + ") ";
                    numRows8 = executeSQLUpdate( delQuery3, params15.toArray());
                }


//                ArrayList params = new ArrayList();
//                params.add(requestParams.get("companyid"));
//                delQuery = "delete  from inventory where company = ? and id in (" + idStrings + ") ";
//                int numRows = executeSQLUpdate( delQuery, params.toArray());
                String companyid = (String) requestParams.get("companyid");
                String selQuery = "from Inventory where company.companyID = ? and  ID in (" + idStrings + ") ";
                List resultList = executeQuery( selQuery, new Object[]{companyid});
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
                int numRows = resultList.size();

                ArrayList params9 = new ArrayList();
                params9.add(requestParams.get("companyid"));
                params9.add(requestParams.get("prid"));
                String myquery1 = "select id from purchasereturn where company = ? and id=?";
                List list1 = executeSQLQuery( myquery1, params9.toArray());
                Iterator itr1 = list1.iterator();
                String journalent = "";
                while (itr1.hasNext()) {

                    String jeidi = itr1.next().toString();
                    journalent += "'" + jeidi + "',";
                }
                if (!StringUtil.isNullOrEmpty(journalent)) {
                    journalent = journalent.substring(0, journalent.length() - 1);
                }
                /**
                 *  Delete GST Fields for India.
                 */
                deleteGstTaxClassDetails(idStrings);
                ArrayList params1 = new ArrayList();
                delQuery1 = "delete  from prdetailscustomdata where prdetailsid in (" + idStrings + ")";
                int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());

                deleteGstDocHistoryDetails(requestParams.get("prid").toString());
                ArrayList params6 = new ArrayList();
                params6.add(requestParams.get("companyid"));
                params6.add(requestParams.get("prid"));
                delQuery6 = "delete from purchasereturn where company = ? and id=?";
                int numRows6 = executeSQLUpdate( delQuery6, params6.toArray());


                ArrayList params2 = new ArrayList();
                delQuery2 = "delete  from purchasereturncustomdata where purchaseReturnId in (" + journalent + ")";
                int numRows2 = executeSQLUpdate( delQuery2, params2.toArray());


                numtotal = numRows + numRows1 + numRows2 + numRows5 + numRows6 + numRows8 + numRows9 + numRows10;
            }

            return new KwlReturnObject(true, "Purchase Return  has been deleted successfully.", null, null, numtotal);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Purchase Return as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
        }

    }

    public String[] columSortPurchaseReturn(String Col_Name, String Col_Dir) throws ServiceException {
        String[] String_Sort = new String[3];
        if (Col_Name.equals("personname")) {
            String_Sort[0] = " order by name " + Col_Dir;
            String_Sort[1] = ",vendor.name";
        } else if (Col_Name.equals("billno")) {
            String_Sort[0] = " order by prnumber " + Col_Dir;
            String_Sort[1] = ",purchasereturn.prnumber ";
        } else if (Col_Name.equals("date")) {
            String_Sort[0] = " order by orderdate " + Col_Dir;
            String_Sort[1] = ", purchasereturn.orderdate";
        } else {
            String_Sort[0] = " order by orderdate " + Col_Dir;
            String_Sort[1] = " , purchasereturn.orderdate";
        }
        return String_Sort;

    }

    public KwlReturnObject getPurchaseReturn(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            String ss = (String) request.get(Constants.ss);
            boolean isfavourite = false;
            boolean isConsignment = false;
            String moduleid = "";
            if (request.containsKey(Constants.moduleid) && request.get(Constants.moduleid) != null) {
                moduleid = request.get(Constants.moduleid).toString();
            }
            if (request.get("isfavourite") != null) {
                isfavourite = Boolean.parseBoolean((String) request.get("isfavourite"));
            }
            if (request.containsKey("isConsignment") && request.get("isConsignment") != null) {
                isConsignment = (Boolean) request.get("isConsignment");
            }
            
            boolean isFixedAsset = false;
            if (request.containsKey("isFixedAsset") && request.get("isFixedAsset") != null) {
                isFixedAsset = (Boolean) request.get("isFixedAsset");
            }
            
            boolean isprdetailsJoin = false;
            String productid = "";
            if (request.containsKey(Constants.productid) && request.get(Constants.productid) != null) {
                productid = (String) request.get(Constants.productid);
            }
            
            String vendorCategoryid = "";
            if (request.containsKey(Constants.customerCategoryid) && request.get(Constants.customerCategoryid) != null) {
                vendorCategoryid = (String) request.get(Constants.customerCategoryid);
            }
            
            String productCategoryid = "";
            if (request.containsKey(Constants.productCategoryid) && request.get(Constants.productCategoryid) != null) {
                productCategoryid = (String) request.get(Constants.productCategoryid);
            }
            
            String newvendorid = "";
            if (request.containsKey(Constants.newvendorid) && request.get(Constants.newvendorid) != null) {
                newvendorid = (String) request.get(Constants.newvendorid);
            }
            ArrayList params = new ArrayList();


            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            
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
            
            String conditionSQL = "";
            String companyid = AccountingManager.getFilterInString((String) request.get(Constants.companyKey));
            if (nondeleted) {

                conditionSQL = "  where purchasereturn.deleteflag='F' and purchasereturn.company in " + companyid + " ";

            } else if (deleted) {

                conditionSQL += " where purchasereturn.deleteflag='T' and purchasereturn.company in " + companyid + " ";

            } else {
                conditionSQL += " where purchasereturn.company in " + companyid + " ";

            }
            
            if (isConsignment) {
                conditionSQL += " and purchasereturn.isconsignment='T' ";
            } else {
                conditionSQL += " and purchasereturn.isconsignment='F' ";
            }
            
            if (isFixedAsset) {
                conditionSQL += " and purchasereturn.isfixedasset=true ";
            } else {
                conditionSQL += " and purchasereturn.isfixedasset=false ";
            }
            if (request.containsKey("linknumber") && request.get("linknumber") != null && !request.get("linknumber").toString().equals("")) {
                conditionSQL += " and purchasereturn.prnumber = '"+request.get("linknumber")+"' ";
            }
            String billID = "";
            if (request.containsKey("billid") && request.get("billid") != null) { // view GRO from journal entry
                billID = (String) request.get("billid");
            }
            if (!StringUtil.isNullOrEmpty(billID)) {
                params.add(billID);
                conditionSQL += " and purchasereturn.id=?";
            }

            //String conditionSQL = " where purchasereturn.deleteflag='F' and purchasereturn.company in "+companyid+" ";
            String searchJoin = "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                searchJoin = " inner join prdetails on prdetails.purchasereturn = purchasereturn.id ";
                if(request.get("moduleid")!=null && request.get("moduleid")!="" && Integer.parseInt(request.get("moduleid").toString())==Constants.Acc_Purchase_Return_ModuleId){
                    for (int i = 0; i < 7; i++) {
                        params.add("%" + ss + "%");//ERP-9307:Option to search by any letter. 
                    }

                    conditionSQL += " and (( purchasereturn.prnumber like ? or purchasereturn.memo like ? or purchasereturn.supplierinvoiceno like ? or vendor.name like ? or vendor.aliasname like ? ) or prdetails.product in(select id from product where product.company in " + companyid + " and (name like ? or productid like ?)))";
                }else{
                    for (int i = 0; i < 7; i++) {
                        params.add("%" + ss + "%");//ERP-9307:Option to search by any letter. 
                    }
    //                params.add("%" + ss + "%");

                    conditionSQL += " and (( purchasereturn.prnumber like ? or purchasereturn.memo like ? or vendor.name like ? or vendor.aliasname like ? or prdetails.partno like ? ) or prdetails.product in(select id from product where name like ? or productid like ?))";
                }
                isprdetailsJoin = true;
            }
            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                conditionSQL += " and costcenter.id=?";
            }
            
            if (!StringUtil.isNullOrEmpty(vendorCategoryid) && !StringUtil.equal(vendorCategoryid, "-1") && !StringUtil.equal(vendorCategoryid, "All")) {
                params.add(vendorCategoryid);
                conditionSQL += " and vendor.id in (select vendorid from vendorcategorymapping where vendorcategory = ?)  ";
            }
            
            if (isfavourite) {
                conditionSQL += " and purchasereturn.favouriteflag=true ";
            }
            
            if (request.containsKey("linknumber") && request.get("linknumber") != null && !request.get("linknumber").toString().equals("")) {
                conditionSQL += " and purchasereturn.prnumber = '"+request.get("linknumber")+"' ";
            }
            
            if (!StringUtil.isNullOrEmpty(newvendorid)) {
                if (newvendorid.contains(",")) {
                    newvendorid = AccountingManager.getFilterInString(newvendorid);
                    conditionSQL += " and purchasereturn.vendor IN" + newvendorid;
                } else {
                    params.add(newvendorid);
                    conditionSQL += " and purchasereturn.vendor = ? ";
                }
            }
            
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            String joinString = "";
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSQL += " and (purchasereturn.orderdate >=? and purchasereturn.orderdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            String appendCase = "and";
            String mySearchFilterString = "";
            String searchDefaultFieldSQL = "";
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
                    if (defaultSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Default Form fields
                         */
                        ArrayList tableArray = new ArrayList();
                        tableArray.add("vendor"); //this table array used to identified wheather join exists on table or not                         
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, moduleid, tableArray, filterConjuctionCriteria);
                        searchJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchJoin += " left join purchasereturnlinking on purchasereturnlinking.docid=purchasereturn.id and purchasereturnlinking.sourceflag = 1 ";
                    }
                    if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field
                        request.put(Constants.Searchjson, Searchjson);
                        request.put(Constants.appendCase, appendCase);
                        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                        String innerJoinOnDetailTable = " inner join prdetails on prdetails.purchasereturn=purchasereturn.id ";
                        boolean isInnerJoinAppend = false;
                        if (mySearchFilterString.contains("purchasereturncustomdata")) {
                            joinString = " inner join purchasereturncustomdata on purchasereturncustomdata.purchasereturnid=purchasereturn.accpurchasereturncustomdataref ";
                        }
                        if (mySearchFilterString.contains("VendorCustomData")) {
                            joinString += " left join vendorcustomdata  on vendorcustomdata.vendorId=vendor.id ";
                            mySearchFilterString = mySearchFilterString.replaceAll("VendorCustomData", "vendorcustomdata");
                        }
//                    mySearchFilterString = mySearchFilterString.replaceAll("DeliveryOrderCustomData", "deliveryorder.accdeliveryordercustomdataref");
                        if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "prdetailscustomdata");
                            joinString += innerJoinOnDetailTable+" left join prdetailscustomdata on prdetails.id=prdetailscustomdata.prdetailsid ";
                            isInnerJoinAppend=true;
                        }
                        if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "prdetailproductcustomdata");
                            joinString += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join prdetailproductcustomdata on prdetails.id=prdetailproductcustomdata.prDetailID ";
                            isInnerJoinAppend=true;
                        }
                        //product custom data
                        if (mySearchFilterString.contains("accproductcustomdata")) {
                            joinString += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join product on product.id=prdetails.product left join accproductcustomdata on accproductcustomdata.productId=product.id ";
                        }
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
            
            String joinString2 = "";
            if (!StringUtil.isNullOrEmpty(productid)) {
                if (!isprdetailsJoin) {
                    joinString2 = " inner join prdetails on prdetails.purchasereturn = purchasereturn.id ";
                    isprdetailsJoin = true;
                }
                params.add(productid);
                conditionSQL += " and prdetails.product = ? ";
            }

            if (!StringUtil.isNullOrEmpty(productCategoryid)) {
                if (!isprdetailsJoin) {
                    joinString2 = " inner join prdetails on prdetails.purchasereturn = purchasereturn.id ";
                    isprdetailsJoin = true;
                }
                params.add(productCategoryid);
                conditionSQL += " and prdetails.product in (select productid from productcategorymapping where productcategory = ?) ";
            }
            
            String joinString3 = "";
            
            boolean isPurchaseReturnCreditNote = false;
            
            if (request.containsKey("isPurchaseReturnCreditNote") && request.get("isPurchaseReturnCreditNote") != null) {
                isPurchaseReturnCreditNote = (Boolean) request.get("isPurchaseReturnCreditNote");
            }
            
            if(isPurchaseReturnCreditNote){// those sales returns which are created with credit notes
                joinString3 = " inner join debitnote on debitnote.purchasereturn = purchasereturn.id ";
            }
            
            String orderBy = "";
            String sort_Col = "";
            String[] stringSort = null;
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                stringSort = columSortPurchaseReturn(Col_Name, Col_Dir);
                orderBy += stringSort[0];
                sort_Col += stringSort[1];

            } else {
                orderBy = " order by orderdate desc";
                sort_Col += " ,purchasereturn.orderdate  ";
            }
            
            String salesPersonMappingQuery = "";
           if (isenableSalesPersonAgentFlow && !StringUtil.isNullOrEmpty(userID)) {
                salesPersonMappingQuery = " left join vendoragentmapping spm on spm.vendorid=purchasereturn.vendor  left join masteritem  mst on mst.id=spm.agent ";
                joinString+=salesPersonMappingQuery;
                conditionSQL += " and ((mst.user= '" + userID + "' or mst.user is null  and vendor.vendavailtoagent='T' ) or  (vendor.vendavailtoagent='F')) ";
            }
            
            String mysqlQuery = " select distinct(purchasereturn.id), 'false' as withoutinventory " + sort_Col + " from purchasereturn "
                    + "inner join vendor on vendor.id = purchasereturn.vendor " + searchJoin + joinString + joinString2+joinString3
                    + "left join costcenter on costcenter.id = purchasereturn.costcenter " + conditionSQL + mySearchFilterString
                    + orderBy;



            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.getPurchaseReturn:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getPurchaseReturnDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from PurchaseReturnDetail";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject saveInvoiceTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ReceiptTermsMap termmap = new ReceiptTermsMap();

            if (dataMap.containsKey("termamount")) {
                termmap.setTermamount((Double) dataMap.get("termamount"));
            }
            if (dataMap.containsKey("termtaxamount")) {
                termmap.setTermtaxamount((Double) dataMap.get("termtaxamount"));
            }
            if (dataMap.containsKey("termtaxamountinbase")) {
                termmap.setTermtaxamountinbase((Double) dataMap.get("termtaxamountinbase"));
            }
            if (dataMap.containsKey("termAmountExcludingTax")) {
                termmap.setTermAmountExcludingTax((Double) dataMap.get("termAmountExcludingTax"));
            }
            if (dataMap.containsKey("termAmountExcludingTaxInBase")) {
                termmap.setTermAmountExcludingTaxInBase((Double) dataMap.get("termAmountExcludingTaxInBase"));
            }
            if (dataMap.containsKey("termamountinbase")) {
                termmap.setTermamountinbase((Double) dataMap.get("termamountinbase"));
            }
            if (dataMap.containsKey("termtax") && dataMap.get("termtax") != null) {
                Tax termtax = (Tax) get(Tax.class, (String) dataMap.get("termtax"));
                termmap.setTermtax(termtax);
            }
            if (dataMap.containsKey("termpercentage")) {
                termmap.setPercentage((Double) dataMap.get("termpercentage"));
            }
            if (dataMap.containsKey("invoice")) {
                GoodsReceipt invoice = (GoodsReceipt) get(GoodsReceipt.class, (String) dataMap.get("invoice"));
                termmap.setGoodsreceipt(invoice);
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
            if (dataMap.containsKey("grdetail") && !StringUtil.isNullOrEmpty((String) dataMap.get("grdetail"))) {
                GoodsReceiptDetail grdetail = (GoodsReceiptDetail) get(GoodsReceiptDetail.class, (String) dataMap.get("grdetail"));
                termmap.setGrdetails(grdetail);
            }
            saveOrUpdate(termmap);
            list.add(termmap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.saveInvoiceTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject updateGoodsReceiptTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ReceiptTermsMap termmap = new ReceiptTermsMap();

            if (dataMap.containsKey("receipttermid")) {
                termmap = (ReceiptTermsMap) get(ReceiptTermsMap.class, (String) dataMap.get("receipttermid"));
            }
            if(termmap!=null){
                if (dataMap.containsKey("termamount")) {
                    termmap.setTermamount((Double) dataMap.get("termamount"));
                }
                if (dataMap.containsKey("termamountinbase")) {
                    termmap.setTermamountinbase((Double) dataMap.get("termamountinbase"));
                }
                if (dataMap.containsKey("termtaxamount")) {
                    termmap.setTermtaxamount((Double) dataMap.get("termtaxamount"));
                }
                if (dataMap.containsKey("termtaxamountinbase")) {
                    termmap.setTermtaxamountinbase((Double) dataMap.get("termtaxamountinbase"));
                }
                if (dataMap.containsKey("termAmountExcludingTax")) {
                    termmap.setTermAmountExcludingTax((Double) dataMap.get("termAmountExcludingTax"));
                }
                if (dataMap.containsKey("termAmountExcludingTaxInBase")) {
                    termmap.setTermAmountExcludingTaxInBase((Double) dataMap.get("termAmountExcludingTaxInBase"));
                }
                if (dataMap.containsKey("termtax") && dataMap.get("termtax") != null) {
                    Tax termtax = (Tax) get(Tax.class, (String) dataMap.get("termtax"));
                    termmap.setTermtax(termtax);
                }
//                if (dataMap.containsKey("termpercentage")) {
//                    termmap.setPercentage((Double) dataMap.get("termpercentage"));
//                }
//                if (dataMap.containsKey("goodsReceiptOrderID") && goodsReceiptOrder != null) {
//                    termmap.setGoodsReceiptOrder(goodsReceiptOrder);
//                }
//                if (dataMap.containsKey("userid")) {
//                    User userid = (User) get(User.class, (String) dataMap.get("userid"));
//                    termmap.setCreator(userid);
//                }
//                if (dataMap.containsKey("createdon")) {
//                    termmap.setCreatedOn(((Date) dataMap.get("creationdate")).getTime());
//                }
                saveOrUpdate(termmap);
                list.add(termmap);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.updateGoodsReceiptTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getAllGlobalGoodsReceiptOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        String sqlQuery = "select gr.id as grid,rtm.id as rtmid,gr.tax,rtm.termamount,tl.percent,rtm.term  from receipttermsmap rtm  inner join goodsreceipt gr on gr.id=rtm.goodsreceipt  inner join taxlist tl on tl.tax=gr.tax where rtm.termamount != 0 and gr.tax is not null and gr.company=?";
        list = executeSQLQuery( sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject savePurchaseReturnTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            PurchaseReturnTermsMap termmap = new PurchaseReturnTermsMap();

            if (dataMap.containsKey("purchaseReturnID")) {
                PurchaseReturn purchasereturn = (PurchaseReturn) get(PurchaseReturn.class, (String) dataMap.get("purchaseReturnID"));
                termmap.setPurchasereturn(purchasereturn);
            }
            if (dataMap.containsKey("term")) {
                InvoiceTermsSales term = (InvoiceTermsSales) get(InvoiceTermsSales.class, (String) dataMap.get("term"));
                termmap.setTerm(term);
            }
            if (dataMap.containsKey("termamount")) {
                termmap.setTermamount((Double) dataMap.get("termamount"));
            }
            if (dataMap.containsKey("termtaxamount")) {
                termmap.setTermtaxamount((Double) dataMap.get("termtaxamount"));
            }
            if (dataMap.containsKey("termtaxamountinbase")) {
                termmap.setTermtaxamountinbase((Double) dataMap.get("termtaxamountinbase"));
            }
            if (dataMap.containsKey("termAmountExcludingTax")) {
                termmap.setTermAmountExcludingTax((Double) dataMap.get("termAmountExcludingTax"));
            }
            if (dataMap.containsKey("termAmountExcludingTaxInBase")) {
                termmap.setTermAmountExcludingTaxInBase((Double) dataMap.get("termAmountExcludingTaxInBase"));
            }
            if (dataMap.containsKey("termamountinbase")) {
                termmap.setTermamountinbase((Double) dataMap.get("termamountinbase"));
            }
            if (dataMap.containsKey("termtax") && dataMap.get("termtax")!=null) {
                Tax termtax = (Tax) get(Tax.class, (String) dataMap.get("termtax"));
                termmap.setTermtax(termtax);
            }
            if (dataMap.containsKey("termpercentage")) {
                termmap.setPercentage((Double) dataMap.get("termpercentage"));
            }
            if (dataMap.containsKey("userid")) {
                User userid = (User) get(User.class, (String) dataMap.get("userid"));
                termmap.setCreator(userid);
            }
            if (dataMap.containsKey("creationdate")) {
                termmap.setCreatedOn(((Date) dataMap.get("creationdate")).getTime());
            }
            saveOrUpdate(termmap);
            list.add(termmap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("savePurchaseReturnTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    public KwlReturnObject saveInvoiceDetailTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ReceiptDetailTermsMap termmap = new ReceiptDetailTermsMap();

            if (dataMap.containsKey("termamount")) {
                termmap.setTermamount((Double) dataMap.get("termamount"));
            }
            if (dataMap.containsKey("termpercentage")) {
                termmap.setPercentage((Double) dataMap.get("termpercentage"));
            }
            if (dataMap.containsKey("assessablevalue")) {
                termmap.setAssessablevalue((Double) dataMap.get("assessablevalue"));
            }
            if (dataMap.containsKey("goodsReceiptDetail")) {
                termmap.setGoodsreceiptdetail((GoodsReceiptDetail)dataMap.get("goodsReceiptDetail"));
            }
            if (dataMap.containsKey("term")) {
                LineLevelTerms term = (LineLevelTerms) get(LineLevelTerms.class, (String) dataMap.get("term"));
                termmap.setTerm(term);
            }
            if (dataMap.containsKey("userid")) {
                User userid = (User) get(User.class, (String) dataMap.get("userid"));
                termmap.setCreator(userid);
            }
            if (dataMap.containsKey("creationdate")) {
                termmap.setCreatedOn(((Date) dataMap.get("creationdate")).getTime());
            }
            if (dataMap.containsKey("purchasevalueorsalevalue") && !StringUtil.isNullObject(dataMap.get("purchasevalueorsalevalue")) && !StringUtil.isNullOrEmpty(dataMap.get("purchasevalueorsalevalue").toString())) {
                termmap.setPurchaseValueOrSaleValue((Double) dataMap.get("purchasevalueorsalevalue"));
            }
            if (dataMap.containsKey("deductionorabatementpercent") && !StringUtil.isNullObject(dataMap.get("deductionorabatementpercent")) && !StringUtil.isNullOrEmpty(dataMap.get("deductionorabatementpercent").toString())) {
                termmap.setDeductionOrAbatementPercent((Double) dataMap.get("deductionorabatementpercent"));
            }
            if (dataMap.containsKey("taxtype") && !StringUtil.isNullObject(dataMap.get("taxtype")) && !StringUtil.isNullOrEmpty(dataMap.get("taxtype").toString())) {
                termmap.setTaxType((Integer) dataMap.get("taxtype"));
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
            throw ServiceException.FAILURE("accGoodsReceiptImpl.saveInvoiceTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getInvoiceTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String invoiceID = hm.get("invoiceid").toString();
            String query = "from ReceiptTermsMap where goodsreceipt.ID = ?";
            list = executeQuery( query, new Object[]{invoiceID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getInvoiceTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getGRTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String goodsReceiptID = hm.get("goodsReceiptID").toString();
            String query = "from GoodsReceiptOrderTermMap where goodsReceiptOrder.ID = ?";
            list = executeQuery( query, new Object[]{goodsReceiptID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getGRTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getGoodsReceiptdetailTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        List param = new ArrayList();
        try {
            String query = "from ReceiptDetailTermsMap ";
            String condition = "";
            String orderby = " order by term.termSequence ";
            if (hm.containsKey("GoodsReceiptDetailid") && hm.get("GoodsReceiptDetailid") != null) {
                String GoodsReceiptDetailid = hm.get("GoodsReceiptDetailid").toString();
                condition += " goodsreceiptdetail.ID = ? ";
                param.add(GoodsReceiptDetailid);
            }
            if (hm.containsKey("GoodsReceiptid") && hm.get("GoodsReceiptid") != null) {
                String GoodsReceiptid = hm.get("GoodsReceiptid").toString();
                condition += " goodsreceiptdetail.goodsReceipt.ID = ? ";
                param.add(GoodsReceiptid);
            }
            if (hm.containsKey("productid") && hm.get("productid") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                condition += " product.ID = ? ";
                param.add(hm.get("productid"));
            }
            if (hm.containsKey("termtype") && hm.get("termtype") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                condition += " term.termType = ? ";
                param.add(hm.get("termtype"));
            }
            if (hm.containsKey("termtypeArry") && hm.get("termtypeArry") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                
                List listTerm= (List) hm.get("termtypeArry");
                StringBuilder commaSepValueBuilder = new StringBuilder();

                //Looping through the list
                for (int i = 0; i < listTerm.size(); i++) {
                    //append the value into the builder
                    commaSepValueBuilder.append(listTerm.get(i));

                    //if the value is not the last element of the list
                    //then append the comma(,) as well
                    if (i != listTerm.size() - 1) {
                        commaSepValueBuilder.append(", ");
                    }
                }
                
                condition += " term.termType in ( "+commaSepValueBuilder +" ) ";
            }
            if (!StringUtil.isNullOrEmpty(condition)) {
                query += " where " + condition;
            }
            if (hm.containsKey("orderbyadditionaltax") && hm.get("orderbyadditionaltax") != null) {
                orderby += " , term.isAdditionalTax ";
            }
            orderby += " ASC ";
            query += orderby;
            list = executeQuery(query, param.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getGoodsReceiptdetailTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getGenricGoodsReceiptdetailTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        List param = new ArrayList();
        try {
            /*
             SELECT sum(idt.assessablevalue),sum(idt.termamount) FROM invoicedetailtermsmap idt inner join linelevelterms llt on idt.term=llt.id inner join invoicedetails ids ON  idt.invoicedetail=ids.id INNER JOIN invoice i ON ids.invoice=i.id where i.company="62eb851a-e852-42f2-8d86-516eae944f5e" and llt.termtype = 1  and i.id='0000000055a628ed0155b4c489740bd2'
             
             */
            
            String query = "SELECT sum(rdt.assessablevalue),sum(rdt.termamount) , t.account,t.termType FROM ReceiptDetailTermsMap rdt JOIN rdt.term t JOIN rdt.goodsreceiptdetail grds JOIN grds.goodsReceipt gr";
            String condition = "";
            String orderby = " GROUP BY t.account ";
            if (hm.containsKey("GoodsReceiptDetailid") && hm.get("GoodsReceiptDetailid") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                String GoodsReceiptDetailid = hm.get("GoodsReceiptDetailid").toString();
                condition += " grds.ID = ? ";
                param.add(GoodsReceiptDetailid);
            }
            if (hm.containsKey("GoodsReceiptid") && hm.get("GoodsReceiptid") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                String GoodsReceiptDetailid = hm.get("GoodsReceiptid").toString();
                condition += " gr.ID = ? ";
                param.add(GoodsReceiptDetailid);
            }
            
            if (hm.containsKey("termtypeArry") && hm.get("termtypeArry") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }

                List listTerm = (List) hm.get("termtypeArry");
                StringBuilder commaSepValueBuilder = new StringBuilder();

                //Looping through the list
                for (int i = 0; i < listTerm.size(); i++) {
                    //append the value into the builder
                    commaSepValueBuilder.append(listTerm.get(i));

                    //if the value is not the last element of the list
                    //then append the comma(,) as well
                    if (i != listTerm.size() - 1) {
                        commaSepValueBuilder.append(", ");
                    }
                }

                condition += " t.termType in ( " + commaSepValueBuilder + " ) ";
            }
            if (hm.containsKey("termtype") && hm.get("termtype") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                param.add(Integer.parseInt(hm.get("termtype").toString()));
                condition += " t.termType = ? ";
            }
            if (hm.containsKey("termpercentage") && hm.get("termpercentage")!=null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                condition+="t.percentage = ? ";
                param.add(Double.parseDouble(hm.get("termpercentage").toString()));
            }
            if (!StringUtil.isNullOrEmpty(condition)) {
                query += " where " + condition;
            }

           
            query += orderby;
            list = executeQuery(query, param.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getGoodsReceiptdetailTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getGoodsReceiptdetailTermMapForRG(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String natureofStockItem = hm.get("natureofStockItem").toString();
            String comapnyID = hm.get("companyID").toString();
            String query = "from GoodsReceiptDetail grd where grd.Inventory.Product.natureofStockItem = ? and grd.Company.ID=?";
            list = executeQuery( query, new Object[]{natureofStockItem,comapnyID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getGoodsReceiptdetailTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public Map<String, List<ReceiptTermsMap>> getInvoiceTermMapGRList(List<String> invoiceIDLIST) throws ServiceException {
        Map<String, List<ReceiptTermsMap>> invoiceMap = new HashMap<String, List<ReceiptTermsMap>>();
        if (invoiceIDLIST != null && !invoiceIDLIST.isEmpty()) {
            List li = null;
            String query = "select  rtm.goodsreceipt.ID, rtm "
                    + " from ReceiptTermsMap rtm "
                    + " where rtm.goodsreceipt.ID in (:invoiceIDList)";
            List<List> values = new ArrayList<List>();
            values.add(invoiceIDLIST);
            List<Object[]> results = executeCollectionQuery( query, Collections.singletonList("invoiceIDList"), values);

            if (results != null) {
                for (Object[] result : results) {
                    String invID = (String) result[0];
                    if (invoiceMap.containsKey(invID)) {
                        li = invoiceMap.get(invID);
                    } else {
                        li = new ArrayList<ReceiptTermsMap>();
                    }
                    li.add((ReceiptTermsMap) result[1]);
                    invoiceMap.put(invID, li);
                }
            }
        }
        return invoiceMap;
    }

    public KwlReturnObject getMasterItemPriceFormulaPrice(String productId, double itemNo) throws ServiceException {
        List list = new ArrayList();
        try {
            Product product = (Product) get(Product.class, productId);
            String query = "from MasterItemPriceFormula mp where mp.type = ? and  mp.lowerlimitvalue<=? and (mp.upperlimitvalue>=? or mp.upperlimitvalue=?)";
            list = executeQuery( query, new Object[]{product.getDependenttype(), itemNo, itemNo, 0.0});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getMasterItemPriceFormulaPrice:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteInvoiceTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String invoiceID = hm.get("invoiceid").toString();
            String query = "delete from receipttermsmap where goodsreceipt = ?";
//            list = executeQuery( query, new Object[]{invoiceID});
            executeSQLUpdate( query, new Object[]{invoiceID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.deleteInvoiceTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject updateGoodsReceiptCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String goodsReceiptRefId = (String) requestParams.get("accgoodsreceiptcustomdataref");
            GoodsReceiptOrder goodsReceiptOrder = (GoodsReceiptOrder) get(GoodsReceiptOrder.class, goodsReceiptRefId);
            if (requestParams.containsKey("accgoodsreceiptcustomdataref")) {
                GoodsReceiptOrderCustomData goodsReceiptCustomData = null;
                goodsReceiptCustomData = (GoodsReceiptOrderCustomData) get(GoodsReceiptOrderCustomData.class, (String) requestParams.get("accgoodsreceiptcustomdataref"));
                goodsReceiptOrder.setGoodsReceiptOrderCustomData(goodsReceiptCustomData);
            }
            saveOrUpdate(goodsReceiptOrder);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.updateGoodsReceiptCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    @Override
    public KwlReturnObject updateRecDetailId(GoodsReceipt gr) throws ServiceException {
        int NoOFRecords = 0;
        String query = "";
        try {
            Set<GoodsReceiptDetail> goodsReceiptDetails = gr.getRows();
            for (GoodsReceiptDetail ivd : goodsReceiptDetails) {
                if (ivd.getPurchaseJED() != null) {
                    query = "update AccJEDetailCustomData   set recdetailId =? where jedetailId =? ";
                    int numRows = executeUpdate(query, new Object[]{ivd.getInventory().getID(), ivd.getPurchaseJED().getID()});
                    NoOFRecords += numRows;
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.updateGoodsReceiptCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, NoOFRecords);
    }
    @Override
    public KwlReturnObject updatePurchaseReturnCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String purchaseReturnRefId = (String) requestParams.get("accpurchasereturncustomdataref");
            PurchaseReturn purchaseReturn = (PurchaseReturn) get(PurchaseReturn.class, purchaseReturnRefId);
            if (requestParams.containsKey("accpurchasereturncustomdataref")) {
                PurchaseReturnCustomData purchaseReturnCustomData = null;
                purchaseReturnCustomData = (PurchaseReturnCustomData) get(PurchaseReturnCustomData.class, (String) requestParams.get("accpurchasereturncustomdataref"));
                purchaseReturn.setPurchaseReturnCustomData(purchaseReturnCustomData);
            }
            saveOrUpdate(purchaseReturn);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.updatePurchaseReturnCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    @Override
    public KwlReturnObject updateGRDetailsCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String goodsReceiptRefId = (String) requestParams.get("grDetailsordercustomdataref");
            GoodsReceiptOrderDetails goodsReceiptOrderDetails = (GoodsReceiptOrderDetails) get(GoodsReceiptOrderDetails.class, goodsReceiptRefId);
            if (requestParams.containsKey("grDetailsordercustomdataref")) {
                GoodsReceiptOrderDetailsCustomDate goodsReceiptOrderDetailsCustomDate = null;
                goodsReceiptOrderDetailsCustomDate = (GoodsReceiptOrderDetailsCustomDate) get(GoodsReceiptOrderDetailsCustomDate.class, (String) requestParams.get("grDetailsordercustomdataref"));
                goodsReceiptOrderDetails.setGoodsReceiptOrderDetailsCustomDate(goodsReceiptOrderDetailsCustomDate);
            }
            saveOrUpdate(goodsReceiptOrderDetails);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.updateGoodsReceiptCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    @Override
    public KwlReturnObject updateGRDetailsProductCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String goodsReceiptRefId = (String) requestParams.get("grodetailproductcustomdataref");
            GoodsReceiptOrderDetails goodsReceiptOrderDetails = (GoodsReceiptOrderDetails) get(GoodsReceiptOrderDetails.class, goodsReceiptRefId);
            if (requestParams.containsKey("grodetailproductcustomdataref")) {
                GoodsReceiptOrderProductCustomData goodsReceiptOrderProductCustomData = null;
                goodsReceiptOrderProductCustomData = (GoodsReceiptOrderProductCustomData) get(GoodsReceiptOrderProductCustomData.class, goodsReceiptRefId);
                goodsReceiptOrderDetails.setGroProductcustomdata(goodsReceiptOrderProductCustomData);
            }
            saveOrUpdate(goodsReceiptOrderDetails);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.updateGRDetailsProductCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    
    @Override
    public KwlReturnObject getDebitNoteLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);
        String query = "select distinct debitNote,grlinkdate from dndetails dn where dn.goodsreceipt=? and dn.company=?";

        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getCreditNoteLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(invoiceId);
        String query = "select distinct cnd.creditnote from cndetailsgst cnd, grdetails grd , goodsreceipt gr where gr.id = grd.goodsreceipt and grd.id = cnd.videtails and cnd.company =? and gr.id =?";
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getDebitNoteForOverchargedLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(invoiceId);
        String query = "select distinct dndgst.debitNote from dndetailsgst dndgst, grdetails grd , goodsreceipt gr where gr.id = grd.goodsreceipt and grd.id = dndgst.grdetail and dndgst.company =? and gr.id =?";
        List list = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getPurchaseInvoiceLinkedWithGR(String goodsreceiptOrderId, String companyId) throws ServiceException {
        List list = new ArrayList();
        List list1 = new ArrayList();
        List list2 = new ArrayList();
        try {
            // Type=0 GR -> PI
            String selQuery = "select distinct grd.goodsReceipt,0 from GoodsReceiptDetail grd inner join grd.goodsReceiptOrderDetails grod where grod.grOrder.ID=? and grd.company.companyID=?";
            list1 = executeQuery( selQuery, new Object[]{goodsreceiptOrderId, companyId});
            // Type=1 PI -> GR
            String hqlQuery = "select distinct invd.goodsReceipt,1 from GoodsReceiptOrderDetails grod inner join grod.videtails invd where grod.grOrder.ID=? and grod.company.companyID=?";
            list2 = executeQuery( hqlQuery, new Object[]{goodsreceiptOrderId, companyId});
            list.addAll(list1);
            list.addAll(list2);
        } catch (Exception ex) {
            System.out.println("Exception: getInvoiceFromDO " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPurchaseReturnLinkedWithGR(String invoiceId, String companyId) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);
        String query = "select distinct purchasereturn from prdetails prd inner join grodetails grod on prd.grdetails = grod.id "
                + "where grod.grorder= ? and grod.company=?";

        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPaymentVouchersLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);
//        String query = "select distinct payment from paymentdetail pd where pd.goodsReceipt=? and pd.company=?";//ERP-12699
        String query = "select distinct payment from paymentdetail pd inner join payment p on pd.payment = p.id where pd.goodsReceipt=? and pd.company=?";

        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPurchaseReturnLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);
        String query = "select distinct purchasereturn from prdetails prd INNER JOIN grdetails gr on prd.videtails=gr.id "
                + "and gr.goodsreceipt=? and prd.company=?";
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public boolean isGRUsedInDebitNote(String grId, String companyId) throws ServiceException {
        boolean isGRUsedInDebitNote = false;
        ArrayList params = new ArrayList();
        params.add(grId);
        params.add(companyId);
        String query = "select * from dndetails dn where dn.goodsreceipt=? and dn.company=?";

        List list = executeSQLQuery( query, params.toArray());
        int count = 0;
        if (list != null && !list.isEmpty()) {
            count = list.size();
        }
        if (count > 0) {
            isGRUsedInDebitNote = true;
        }
        return isGRUsedInDebitNote;
    }
    public boolean isGROhasReturned(String grId, String companyId) throws ServiceException {
        boolean isGROhasReturned = false;
        ArrayList params = new ArrayList();
        params.add(grId);
        params.add(companyId);
        String query = "select id from prdetails prd where prd.grdetails=? and prd.company=?";

        List list = executeSQLQuery( query, params.toArray());
        int count = 0;
        if (list != null && !list.isEmpty()) {
            count = list.size();
        }
        if (count > 0) {
            isGROhasReturned = true;
        }
        return isGROhasReturned;
    }

    @Override
    public boolean isInvoicehasDepreciatedAsset(String invoiceId, String companyId) throws ServiceException {
        boolean isInvoicehasDepreciatedAsset = false;
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);
        String query = "SELECT Distinct(adp.id) FROM goodsreceipt gr INNER JOIN grdetails grd ON "
                + "grd.goodsreceipt=gr.id INNER JOIN assetdetailsinvdetailmapping amp ON "
                + "amp.invoicedetailid=grd.id INNER JOIN assetdepreciationdetail adp ON "
                + "adp.assetdetail=amp.assetdetails WHERE gr.id=? AND amp.moduleid=6 AND gr.company=?";

        List list = executeSQLQuery( query, params.toArray());
        Iterator itr = list.iterator();
        if (itr.hasNext()) {
            isInvoicehasDepreciatedAsset = true;
        }

        return isInvoicehasDepreciatedAsset;
    }

    @Override
    public boolean isInvoicehasSoldAsset(String invoiceId, String companyId) throws ServiceException {
        boolean isInvoicehasSoldAsset = false;
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);
        String query = "SELECT Distinct(ad.id) FROM goodsreceipt gr INNER JOIN grdetails grd ON grd.goodsreceipt = gr.id "
                + "INNER JOIN assetdetailsinvdetailmapping amp ON amp.invoicedetailid=grd.id "
                + "INNER JOIN assetdetail ad ON ad.id=amp.assetdetails WHERE ad.assetsoldflag != 0 "
                + "AND gr.id=? AND gr.company=?";

        List list = executeSQLQuery( query, params.toArray());
        Iterator itr = list.iterator();
        if (itr.hasNext()) {
            isInvoicehasSoldAsset = true;
        }

        return isInvoicehasSoldAsset;
    }

    @Override
    public boolean isGROhasDepreciatedAsset(String invoiceId, String companyId) throws ServiceException {
        boolean isInvoicehasDepreciatedAsset = false;
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);

        String query = "SELECT Distinct(adp.id) FROM grorder gr INNER JOIN grodetails grd ON "
                + "grd.grorder=gr.id INNER JOIN assetdetailsinvdetailmapping amp ON "
                + "amp.invoicedetailid=grd.id INNER JOIN assetdepreciationdetail adp ON "
                + "adp.assetdetail=amp.assetdetails WHERE gr.id=? AND amp.moduleid=28 AND gr.company=?";

        List list = executeSQLQuery( query, params.toArray());
        Iterator itr = list.iterator();
        if (itr.hasNext()) {
            isInvoicehasDepreciatedAsset = true;
        }

        return isInvoicehasDepreciatedAsset;
    }

    @Override
    public boolean isGROhasSoldAsset(String invoiceId, String companyId) throws ServiceException {
        boolean isGROhasSoldAsset = false;
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);
        String query = "SELECT Distinct(ad.id) FROM grorder gr INNER JOIN grodetails grd ON grd.grorder = gr.id "
                + "INNER JOIN assetdetailsinvdetailmapping amp ON amp.invoicedetailid=grd.id "
                + "INNER JOIN assetdetail ad ON ad.id=amp.assetdetails WHERE ad.assetsoldflag != 0 "
                + "AND gr.id=? AND gr.company=?";

        List list = executeSQLQuery( query, params.toArray());
        Iterator itr = list.iterator();
        if (itr.hasNext()) {
            isGROhasSoldAsset = true;
        }

        return isGROhasSoldAsset;
    }

    @Override
    public boolean isGROhasLeasedAsset(String invoiceId, String companyId) throws ServiceException {
        boolean isGROhasLeasedAsset = false;
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);
        String query = "SELECT Distinct(ad.id) FROM grorder gr INNER JOIN grodetails grd ON grd.grorder = gr.id "
                + "INNER JOIN assetdetailsinvdetailmapping amp ON amp.invoicedetailid=grd.id "
                + "INNER JOIN assetdetail ad ON ad.id=amp.assetdetails WHERE ad.islinkedtoleaseso = 1 "
                + "AND gr.id=? AND gr.company=?";

        List list = executeSQLQuery( query, params.toArray());
        Iterator itr = list.iterator();
        if (itr.hasNext()) {
            isGROhasLeasedAsset = true;
        }

        return isGROhasLeasedAsset;
    }

    @Override
    public KwlReturnObject updatePRDetailsCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String purchaseReturnRefId = (String) requestParams.get("gRDetailscustomdataref");
            PurchaseReturnDetail purchaseReturnDetail = (PurchaseReturnDetail) get(PurchaseReturnDetail.class, purchaseReturnRefId);
            if (requestParams.containsKey("gRDetailscustomdataref")) {
                PurchaseReturnDetailCustomDate purchaseReturnDetailCustomDate = null;
                purchaseReturnDetailCustomDate = (PurchaseReturnDetailCustomDate) get(PurchaseReturnDetailCustomDate.class, (String) requestParams.get("gRDetailscustomdataref"));
                purchaseReturnDetail.setPurchaseReturnDetailCustomDate(purchaseReturnDetailCustomDate);
            }
            saveOrUpdate(purchaseReturnDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.updatePurchaseReturnCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    @Override
    public KwlReturnObject updatePRDetailsProductCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String purchaseReturnRefId = (String) requestParams.get("prdetailscustomdataref");
            PurchaseReturnDetail purchaseReturnDetail = (PurchaseReturnDetail) get(PurchaseReturnDetail.class, purchaseReturnRefId);
            if (requestParams.containsKey("prdetailscustomdataref")) {
                PurchaseReturnDetailProductCustomData purchaseReturnDetailProductCustomData = null;
                purchaseReturnDetailProductCustomData = (PurchaseReturnDetailProductCustomData) get(PurchaseReturnDetailProductCustomData.class, purchaseReturnRefId);
                purchaseReturnDetail.setPurchaseReturnDetailProductCustomData(purchaseReturnDetailProductCustomData);
            }
            saveOrUpdate(purchaseReturnDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.updatePRDetailsProductCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    public KwlReturnObject WeeklyCashFlowUnPaidInvoices(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) request.get(COMPANYID);
            DateFormat df = (DateFormat) request.get("dateformat");
            String productid = (String) request.get(PRODUCTID);

            String productCategoryid = "";
            if (request.containsKey(Constants.productCategoryid) && request.get(Constants.productCategoryid) != null) {
                productCategoryid = (String) request.get(Constants.productCategoryid);
            }

            String prodfilterVenid = (String) request.get(PRODFILTERVENID);
            String newvendorid = "";
            boolean isweeklycashflow = false;
            int duration = 0;
            int datefilter = (request.containsKey("datefilter") && request.get("datefilter") != null) ? Integer.parseInt(request.get("datefilter").toString()) : 0;// 0 = Invoice Due date OR 1 = Invoice date
            String vendorIdGroup = "";
            if (request.containsKey("custVendorID") && request.get("custVendorID") != null) {
                vendorIdGroup = (String) request.get("custVendorID");
            }
            if (request.containsKey(NEWVENDORID) && request.get(NEWVENDORID) != null) {
                newvendorid = (String) request.get(NEWVENDORID);
            }
            if (request.get("isweeklycashflow") != null) {
                isweeklycashflow = Boolean.parseBoolean(request.get("isweeklycashflow").toString());
            }
            if (request.get("duration") != null) {
                duration = Integer.parseInt(request.get("duration").toString());
            }

            CompanyAccountPreferences pref = (CompanyAccountPreferences) get(CompanyAccountPreferences.class, companyid);
//            KWLCurrency currency = (KWLCurrency) get(KWLCurrency.class, currencyid);
            Calendar startFinYearCal = Calendar.getInstance();
            Calendar endFinYearCal = Calendar.getInstance();
            startFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.setTime(pref.getFinancialYearFrom());
            boolean pendingapproval = (Boolean) request.get("pendingapproval");
            boolean isForTemplate = false;
            if (request.containsKey("isForTemplate") && request.get("isForTemplate") != null) {
                isForTemplate = Boolean.parseBoolean(request.get("isForTemplate").toString());
            }

            boolean isOpeningBalanceInvoices = false;

            if (request.get("isOpeningBalanceInvoices") != null) {
                isOpeningBalanceInvoices = Boolean.parseBoolean((String) request.get("isOpeningBalanceInvoices"));
            }

            if (request.get("year") != null) {		// Check for the selected year in the year combo for charts			Neeraj
                int year = Integer.parseInt(request.get("year").toString());
                startFinYearCal.set(Calendar.YEAR, year);
                endFinYearCal.set(Calendar.YEAR, year);
            }

            endFinYearCal.add(Calendar.YEAR, 1);
            String vendorid = (String) request.get(VENDORID);
            String customerid = (vendorid == null ? (String) request.get("accid") : vendorid);
            String ss = (String) request.get("ss");
            String cashAccount = pref.getCashAccount().getID();
            boolean CashAndInvoice = (request.containsKey("CashAndInvoice") && request.get("CashAndInvoice") != null) ? Boolean.parseBoolean(request.get("CashAndInvoice").toString()) : false;
            boolean cashonly = false;
            boolean creditonly = false;
            boolean personGroup = false;
            boolean isagedgraph = false;
            boolean isexpenseinv = false;
            boolean only1099Vend = false;
            boolean for1099Report = false;
            boolean isfavourite = false;
            boolean isprinted = false;
            String group = "";
            cashonly = Boolean.parseBoolean((String) request.get("cashonly"));
            creditonly = Boolean.parseBoolean((String) request.get("creditonly"));
            only1099Vend = Boolean.parseBoolean((String) request.get("only1099Vend"));
            String billID = (String) request.get("billid");
            String expenseinv = (String) request.get("onlyexpenseinv");
            for1099Report = Boolean.parseBoolean((String) request.get("for1099Report"));
            boolean deleted = Boolean.parseBoolean((String) request.get(DELETED));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            if (request.get("isfavourite") != null) {
                isfavourite = Boolean.parseBoolean((String) request.get("isfavourite"));
            }
            if (request.get(Constants.MARKED_PRINTED) != null) {
                isprinted = Boolean.parseBoolean((String) request.get(Constants.MARKED_PRINTED));
            }

//            if (cashonly) {
//                customerid = cashAccount;
//            }
//            boolean ignoreZero = request.get("ignorezero") != null;
            String dueDate = (String) request.get("curdate");
            String startdate = (String) request.get(Constants.REQ_startdate);
            personGroup = Boolean.parseBoolean((String) request.get("persongroup"));
            isagedgraph = Boolean.parseBoolean((String) request.get("isagedgraph"));
            ArrayList params = new ArrayList();
            ArrayList paramsSQLOpeningBalanceInv = new ArrayList();
            ArrayList paramsSQLWithoutInv = new ArrayList();
//            String condition = "";
            String conditionSQL = "";
            String conditionSQLWithoutInv = "";
            String conditionSQLForOpeningBalanceInvoice = "";
            String venCondition = "";
            params.add(companyid);
            paramsSQLWithoutInv.add(companyid);
            paramsSQLOpeningBalanceInv.add(companyid);
            String innerQuery3 = "";
            String innerQuery4 = "";
            if (request.containsKey("poid") && request.get("poid") != null) {
                String poid = (String) request.get("poid");
                //if(!StringUtil.isNullOrEmpty(poid)){
                params.add(poid);
                paramsSQLWithoutInv.add(poid);
                innerQuery3 = " inner join grdetails on grdetails.goodsreceipt = goodsreceipt.id "
                        + " inner join podetails on grdetails.purchaseorderdetail = podetails.id "
                        + " inner join purchaseorder on podetails.purchaseorder = purchaseorder.id ";
                innerQuery4 = " inner join billinggrdetails on billinggrdetails.billinggreceipt = billinggr.id "
                        + " inner join billingpodetails on billinggrdetails.purchaseorderdetail = billingpodetails.id "
                        + " inner join billingpurchaseorder on billingpodetails.purchaseorder = billingpurchaseorder.id ";
                conditionSQL += " and  purchaseorder.id= ? ";
                conditionSQLWithoutInv += " and billingpurchaseorder.id =? ";
            }
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
//                condition += " and gr.journalEntry.ID IN(" + jeIds + ")";
                conditionSQL += " and journalentry.id IN(" + jeIds + ")";
                conditionSQLWithoutInv += " and journalentry.id IN(" + jeIds + ")";
            }

            if (!StringUtil.isNullOrEmpty(expenseinv)) {
                isexpenseinv = Boolean.parseBoolean(expenseinv);
                params.add(isexpenseinv);
//                condition += " and gr.isExpenseType=?";
                conditionSQL += " and goodsreceipt.isexpensetype=?";


            }

            if (cashonly) {
                conditionSQL += " and goodsreceipt.cashtransaction=1 ";
            }

            if (!StringUtil.isNullOrEmpty(vendorIdGroup) && !vendorIdGroup.contains("All")) {
                vendorIdGroup = AccountingManager.getFilterInString(vendorIdGroup);
                conditionSQL += " AND goodsreceipt.vendor in " + vendorIdGroup;
                conditionSQLWithoutInv += " AND billinggr.vendor in " + vendorIdGroup;
            }
            if (!StringUtil.isNullOrEmpty(dueDate)) {
                if (for1099Report) {
                    params.add(df.parse(dueDate));
                    paramsSQLWithoutInv.add(df.parse(dueDate));
//                    condition += " and gr.journalEntry.entryDate<=?";
//                    conditionSQL += " and journalentry.entrydate<=?";
                    conditionSQL += " and goodsreceipt.creationdate<=?";
                    conditionSQLWithoutInv += " and journalentry.entrydate<=?";
                } else {
                    if (isweeklycashflow) {
                        params.add(df.parse(startdate));
                        paramsSQLWithoutInv.add(df.parse(startdate));
                        Date curDate = df.parse(startdate);
                        Calendar cal1 = Calendar.getInstance();
                        cal1.setTime(curDate);
                        cal1.add(Calendar.DAY_OF_YEAR, duration * 7);
                        Date cal1Date = null;
                        try {
                            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
                            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

                        } catch (ParseException ex) {
                            cal1Date = cal1.getTime();
                            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        params.add(cal1Date);
                        paramsSQLWithoutInv.add(cal1Date);
                        conditionSQL += " and goodsreceipt.duedate>=? and goodsreceipt.duedate<=? ";
                        conditionSQLWithoutInv += " and billinggr.duedate>=?  and billinggr.duedate<=? ";
                    } else {
                        if (isweeklycashflow) {
                            params.add(df.parse(startdate));
                            paramsSQLWithoutInv.add(df.parse(startdate));
                            Date curDate = df.parse(startdate);
                            Calendar cal1 = Calendar.getInstance();
                            cal1.setTime(curDate);
                            cal1.add(Calendar.DAY_OF_YEAR, duration * 7);
                            Date cal1Date = null;
        
                            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
                            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);
                            params.add(cal1Date);
                            paramsSQLWithoutInv.add(cal1Date);
//                            conditionSQL += " and journalentry.entryDate>=?  and journalentry.entryDate<=?  ";
                            conditionSQL += " and goodsreceipt.creationdate>=?  and goodsreceipt.creationdate<=?  ";
                            conditionSQLWithoutInv += " and journalentry.entryDate>=?  and journalentry.entryDate<=?  ";
                        } else if (!StringUtil.isNullOrEmpty(dueDate)) {
                            params.add(df.parse(dueDate));
                            paramsSQLWithoutInv.add(df.parse(dueDate));
//                condition += " and inv.dueDate<=?";
//                            conditionSQL += " and journalentry.entryDate<=?";
                            conditionSQL += " and goodsreceipt.creationdate<=?";
                            conditionSQLWithoutInv += " and journalentry.entryDate<=?";
                        }
                    }
                }
            }
            if (!StringUtil.isNullOrEmpty(billID)) {
                params.add(billID);
                paramsSQLWithoutInv.add(billID);
//                condition += " and gr.ID=?";
                conditionSQL += " and goodsreceipt.id=?";
                conditionSQLWithoutInv += " and billinggr.id=?";
            }
            if (!StringUtil.isNullOrEmpty(customerid)) {
                params.add(customerid);
                paramsSQLWithoutInv.add(customerid);
                conditionSQL += " and goodsreceipt.vendor=?";
                conditionSQLWithoutInv += " and billinggr.vendor=?";

            }
            if (!CashAndInvoice) {
                if (cashonly) {
                    params.add(true);
                    conditionSQL += " and goodsreceipt.cashtransaction=?";
                } else {
                    params.add(false);
                    conditionSQL += " and goodsreceipt.cashtransaction=?";
                }
            }

            String innerQuery = "";
            String tableInv = "";
            String tableBillingInv = "";
            if (!StringUtil.isNullOrEmpty(productid)) {
                tableInv = ", grdetails.id as invid ";
                tableBillingInv = ", '' as invid ";

                params.add(productid);
                innerQuery = " inner join grdetails on grdetails.goodsreceipt = goodsreceipt.id "
                        + " inner join inventory on grdetails.id = inventory.id ";
                if (StringUtil.isNullOrEmpty(prodfilterVenid)) {
                    conditionSQL += " and inventory.product = ? ";
                } else {
                    params.add(prodfilterVenid);
                    conditionSQL += " and inventory.product = ? and goodsreceipt.vendor = ? ";
                }
                conditionSQLWithoutInv += " and jedetail.account = '' ";

            }

            if (!StringUtil.isNullOrEmpty(productCategoryid)) {
                tableInv = ", grdetails.id as invid ";
                tableBillingInv = ", '' as invid ";

                params.add(productCategoryid);
                innerQuery = " inner join grdetails on grdetails.goodsreceipt = goodsreceipt.id "
                        + " inner join inventory on grdetails.id = inventory.id ";
                if (StringUtil.isNullOrEmpty(prodfilterVenid)) {
                    conditionSQL += " and inventory.product in (select productid from productcategorymapping where productcategory = ?) ";
                } else {
                    params.add(prodfilterVenid);
                    conditionSQL += " and inventory.product in (select productid from productcategorymapping where productcategory = ?) and goodsreceipt.vendor = ? ";
                }
                conditionSQLWithoutInv += " and jedetail.account = '' ";
            }

            if (!StringUtil.isNullOrEmpty(newvendorid)) {
                params.add(newvendorid);
                paramsSQLWithoutInv.add(newvendorid);
                conditionSQL += " and goodsreceipt.vendor = ? ";
                conditionSQLWithoutInv += " and billinggr.vendor = ? ";

            }
            String costCenterId = (String) request.get("costCenterId");
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                paramsSQLWithoutInv.add(costCenterId);
//                condition += " and gr.journalEntry.costcenter.ID=?";
                conditionSQL += " and costcenter.id=?";
                conditionSQLWithoutInv += " and costcenter.id=?";
            }
            String termid = (String) request.get("termid");
            if (!StringUtil.isNullOrEmpty(termid)) {
                params.add(termid);
                // paramsSQLWithoutInv.add(termid);
//                condition += " and gr.journalEntry.costcenter.ID=?";
                conditionSQL += " and goodsreceipt.termid=?";
                //  conditionSQLWithoutInv += " and goodsreceipt.termid=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate) && datefilter != 1) {
//                condition += " and (gr.journalEntry.entryDate >=? and gr.journalEntry.entryDate <=?)";
//                conditionSQL += " and (journalentry.entrydate >=? and journalentry.entrydate <=?)";
                conditionSQL += " and (goodsreceipt.creationdate >=? and goodsreceipt.creationdate <=?)";
                conditionSQLForOpeningBalanceInvoice += " and (goodsreceipt.creationdate >=? and goodsreceipt.creationdate <=?)";
                conditionSQLWithoutInv += " and (journalentry.entrydate >=? and journalentry.entrydate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
                paramsSQLOpeningBalanceInv.add(df.parse(startDate));
                paramsSQLOpeningBalanceInv.add(df.parse(endDate));
                paramsSQLWithoutInv.add(df.parse(startDate));
                paramsSQLWithoutInv.add(df.parse(endDate));
            }
            if (for1099Report) {
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"account.name"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQL += searchQuery;

                    searchcol = new String[]{"account.name"};
                    SearchStringMap = StringUtil.insertParamSearchStringMap(paramsSQLWithoutInv, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQLWithoutInv += searchQuery;

//                        params.add(ss + "%");
//                        paramsSQLWithoutInv.add(ss + "%");
////                    condition += " and gr.vendorEntry.account.name like ? ";
//                    conditionSQL += " and account.name like ? ";
//                    conditionSQLWithoutInv += " and account.name like ? ";
                }
            } else {
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"goodsreceipt.grnumber", "goodsreceipt.billfrom", "journalentry.entryno", "goodsreceipt.memo", "vendor.name"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 5);
                    StringUtil.insertParamSearchString(map);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQL += searchQuery;

                    searchcol = new String[]{"goodsreceipt.grnumber"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramsSQLOpeningBalanceInv, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQLForOpeningBalanceInvoice += searchQuery;

                    searchcol = new String[]{"billinggr.billinggrnumber", "billinggr.billfrom", "journalentry.entryno", "billinggr.memo", "account.name"};
                    SearchStringMap = StringUtil.insertParamSearchStringMap(paramsSQLWithoutInv, ss, 5);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSQLWithoutInv += searchQuery;

//                    for (int i = 0; i <= 4; i++) {
//                        params.add(ss + "%");
//                        paramsSQLWithoutInv.add(ss + "%");
//                    }
////                    condition += " and (gr.goodsReceiptNumber like ? or gr.billFrom like ?  or gr.journalEntry.entryNumber like ? or gr.memo like ? or gr.vendorEntry.account.name like ? ) ";
//                    conditionSQL += " and (goodsreceipt.grnumber like ? or goodsreceipt.billfrom like ?  or journalentry.entryno like ? or goodsreceipt.memo like ? or account.name like ? ) ";
//                    conditionSQLWithoutInv += " and (billinggr.billinggrnumber like ? or billinggr.billfrom like ?  or journalentry.entryno like ? or billinggr.memo like ? or account.name like ? ) ";
                }
            }


            Date startFinYearCalDate = null;
            Date endFinYearCalDate = null;

            String startFinYearCalString = authHandler.getDateOnlyFormat().format(startFinYearCal.getTime());
            startFinYearCalDate = authHandler.getDateOnlyFormat().parse(startFinYearCalString);

            String endFinYearCalString = authHandler.getDateOnlyFormat().format(endFinYearCal.getTime());
            endFinYearCalDate = authHandler.getDateOnlyFormat().parse(endFinYearCalString);
            
            if (personGroup) {
                params.add(startFinYearCalDate);
                params.add(endFinYearCalDate);
                paramsSQLWithoutInv.add(startFinYearCalDate);
                paramsSQLWithoutInv.add(endFinYearCalDate);
//                condition += " and gr.dueDate>=? and gr.dueDate<=?";
                conditionSQL += " and goodsreceipt.duedate>=? and goodsreceipt.duedate<=?";
                conditionSQLWithoutInv += " and billinggr.duedate>=? and billinggr.duedate<=?";
            }
            if (isagedgraph) {
                params.add(startFinYearCalDate);
                params.add(endFinYearCalDate);
                paramsSQLWithoutInv.add(startFinYearCalDate);
                paramsSQLWithoutInv.add(endFinYearCalDate);
//                condition += " and gr.dueDate>=? and gr.dueDate<=?";
                conditionSQL += " and goodsreceipt.duedate>=? and goodsreceipt.duedate<=?";
                conditionSQLWithoutInv += " and billinggr.duedate>=? and billinggr.duedate<=?";
            }
            if (nondeleted) {
//                 condition += " and gr.deleted=false ";
                conditionSQL += " and goodsreceipt.deleteflag='F' ";
                conditionSQLWithoutInv += " and billinggr.deleteflag='F' ";
            } else if (deleted) {
//                 condition += " and gr.deleted=true ";
                conditionSQL += " and goodsreceipt.deleteflag='T' ";
                conditionSQLWithoutInv += " and billinggr.deleteflag='T' ";
            }

            if (isfavourite) {
                conditionSQL += " and goodsreceipt.favouriteflag=true ";
                conditionSQLWithoutInv += " and billinggr.favouriteflag=true ";
            }
            if (isprinted) {
                conditionSQL += " and goodsreceipt.printedflag=true ";
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
                    joinString = " inner join accjecustomdata on accjecustomdata.journalentryId=billinggr.journalentry ";
                    joinString1 = " inner join accjecustomdata on accjecustomdata.journalentryId=goodsreceipt.journalentry ";
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    StringUtil.insertParamAdvanceSearchString1(paramsSQLWithoutInv, Searchjson);
                }
            }

            if (!isForTemplate) {
                if (pendingapproval) {
                    conditionSQL += " and goodsreceipt.pendingapproval != 0 ";
                    conditionSQLWithoutInv += " and billinggr.pendingapproval != 0 ";
                } else {
                    conditionSQL += " and goodsreceipt.pendingapproval= 0 ";
                    conditionSQLWithoutInv += " and billinggr.pendingapproval= 0 ";
                }

                //Block records created as only template
                conditionSQL += " and goodsreceipt.istemplate != 2 ";
                conditionSQLWithoutInv += " and billinggr.istemplate != 2 ";
            }
//            String query = "from GoodsReceipt gr where gr.company.companyID=? " + condition + group + " order by gr.vendorEntry.account.ID, gr.goodsReceiptNumber asc";
            String orderBy = "";
            String sort_Col = "";
            String sort_Col1 = "";
            String[] stringSort = null;
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                stringSort = columSort(Col_Name, Col_Dir);
                orderBy += stringSort[0];
                sort_Col += stringSort[1];
                sort_Col1 += stringSort[2];
                innerQuery += stringSort[3];
                innerQuery4 += stringSort[4];
            } else {
                orderBy = " order by entrydate desc";
//                sort_Col += ", journalentry.entrydate ";
                sort_Col += ", goodsreceipt.creationdate ";
                sort_Col1 += ", journalentry.entrydate ";
            }
            params.addAll(paramsSQLWithoutInv);

            String mysqlQuery = "select goodsreceipt.id,  'false' as withoutinventory, journalentry.createdon " + sort_Col + tableInv + " from goodsreceipt  "
                    + "inner join journalentry on goodsreceipt.journalentry = journalentry.id  "
                    + "inner join jedetail on jedetail.id = goodsreceipt.centry "
                    + " inner join account on account.id = jedetail.account "
                    + " inner join vendor on vendor.id = goodsreceipt.vendor "
                    + innerQuery + joinString1 + innerQuery3
                    + "left join costcenter on costcenter.id = journalentry.costcenter  "
                    + " where goodsreceipt.company = ?" + conditionSQL + mySearchFilterString + " "
                    + " union "
                    + " select billinggr.id,  'true' as withoutinventory, journalentry.createdon " + sort_Col1 + tableBillingInv + " from billinggr"
                    + " inner join journalentry on billinggr.journalentry = journalentry.id  "
                    + "inner join jedetail on jedetail.id = billinggr.centry "
                    //    + " inner join goodsreceipt on goodsreceipt.centry=jedetail.id  "

                    + " inner join account on account.id = jedetail.account " + joinString + innerQuery4
                    + "left join costcenter on costcenter.id = journalentry.costcenter  "
                    + " where billinggr.company = ?" + conditionSQLWithoutInv + mySearchFilterString + orderBy;
//            list = executeQuery( query, params.toArray());
            if (isOpeningBalanceInvoices) {
                params = paramsSQLOpeningBalanceInv;
                mysqlQuery = " select goodsreceipt.id,  'false' as withoutinventory, goodsreceipt.creationdate, goodsreceipt.creationdate from goodsreceipt "
                        + " where goodsreceipt.isopeningbalenceinvoice=True and goodsreceipt.company = ?" + conditionSQLForOpeningBalanceInvoice;
            }
            list = executeSQLQuery( mysqlQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getGoodsReceipts : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getDuplicateGRNumberForEdit(String entryNumber, String companyid, String grid) throws ServiceException {
        try {
            List list = new ArrayList();
            int count = 0;
            String q = "from GoodsReceipt where goodsReceiptNumber=? and company.companyID=? and ID!=? AND isDraft='F'"; //ERP-41992 (Reference - SDP-13487) - Do not check duplicate in Draft Report. Because Multiple draft records having empty entry no.
            list = executeQuery( q, new Object[]{entryNumber, companyid, grid});
            count = list.size();
            return new KwlReturnObject(true, "", null, list, count);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getGoodsReceipts : " + ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject getDuplicaeGoodsReceiptOrderNumber(String entryNumber, String companyid, String doid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from GoodsReceiptOrder where goodsReceiptOrderNumber=? and company.companyID=? and ID!=?";  
        list = executeQuery( q, new Object[]{entryNumber, companyid, doid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    
    public KwlReturnObject getDuplicateSupplierInvoiceNumberForGRN(JSONObject reqParams) throws ServiceException {
        List list = new ArrayList();
        String receiptno=reqParams.optString("supplierInvoiceNumber");
        String vendor=reqParams.optString("vendor");
        String companyid=reqParams.optString("companyid");
        String doid=reqParams.optString("doid");
        String q = "select goodsReceiptOrderNumber from GoodsReceiptOrder where supplierinvoiceno=? and vendor.ID=? and company.companyID=?";
        if (!StringUtil.isNullOrEmpty(doid)) {
            q += " and ID<>" + "'" + doid + "'";
        }
        list = executeQuery( q, new Object[]{receiptno, vendor, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getPurchaseReturnCountEdit(String entryNumber, String companyid, String srid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from PurchaseReturn where purchaseReturnNumber=? and company.companyID=? and ID!=?";
        list = executeQuery( q, new Object[]{entryNumber, companyid, srid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getGR_Product(Map<String, Object> requestMap) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String productId = "";
        String companyId = "";
        DateFormat df = null;
        boolean isApproved=false;
        String startdate = "";
        String enddate = "";
        if (requestMap.containsKey("productId")) {
            productId = requestMap.get("productId").toString();
            params.add(productId);
        }
        if (requestMap.containsKey("companyId")) {
            companyId = requestMap.get("companyId").toString();
            params.add(companyId);
        }
        if (requestMap.containsKey("df")) {
            try {
                df = (DateFormat) requestMap.get("df");
                if (requestMap.containsKey("startdate")) {
                    startdate = requestMap.get("startdate").toString();
                }
                if (requestMap.containsKey("enddate")) {
                    enddate = requestMap.get("enddate").toString();
                }
                condition += " and (grod.grOrder.orderDate >=? and grod.grOrder.orderDate <=?)";
                params.add(df.parse(startdate));
                params.add(df.parse(enddate));
            } catch (ParseException ex) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (requestMap.containsKey("isApproved")) {
            isApproved = Boolean.parseBoolean(requestMap.get("isApproved").toString());
            if (isApproved) {
                condition += " and grod.grOrder.approvestatuslevel=?";
                params.add(11);
            }
        }
        String q = "from GoodsReceiptOrderDetails grod where grod.product.ID=? and grod.company.companyID=?"+condition;// and grod.grOrder.deleted=false ";
        list = executeQuery( q,params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPurchaseReturn_Product(String productid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from PurchaseReturnDetail prd where prd.product.ID=? and prd.company.companyID=?";// and prd.purchaseReturn.deleted=false ";
        list = executeQuery( q, new Object[]{productid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getCompanyGoodsReceipts(String companyid) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String query = "from GoodsReceipt gr where gr.deleted=false AND gr.company.companyID=? "; //" order by inv.customerEntry.account.id, inv.invoiceNumber";            
            list = executeQuery( query, companyid);
            count = list.size();
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getCompanyGoodsReceipts:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
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
                String myquery = "select id from dndetails where debitNote in (select id from debitnote where company =? and id = ?)";
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
                delQuery5 = "delete from dndetails where debitNote in (select id from debitnote where company =? and id = ?)";
                int numRows5 = executeSQLUpdate( delQuery5, params5.toArray());


//                ArrayList params = new ArrayList();
//                params.add(requestParams.get("companyid"));
//                //   params.add(requestParams.get("invoiceid"));
//                delQuery = "delete  from inventory where company =?  and id in(" + idStrings + ") ";
//                int numRows = executeSQLUpdate( delQuery, params.toArray());
                String companyid = (String) requestParams.get("companyid");
                String selQuery = "from Inventory where company.companyID = ? and  ID in (" + idStrings + ") ";
                List resultList = executeQuery( selQuery, new Object[]{companyid});
                Iterator itrInv = resultList.iterator();
                while (itrInv.hasNext()) {
                    Inventory inventory = (Inventory) itrInv.next();
                    if (inventory != null  && inventory.isDeleted() == false) {
                        if (inventory.isCarryIn()) {
                            inventory.getProduct().setAvailableQuantity(inventory.getProduct().getAvailableQuantity() - inventory.getBaseuomquantity());// minus Purchase and Plus Sales (for Reverse effect for quantity)
                        } else {
                            inventory.getProduct().setAvailableQuantity(inventory.getProduct().getAvailableQuantity() + inventory.getBaseuomquantity());
                        }
                    }
                }
                deleteAll(resultList);
                int numRows = resultList.size();


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
     public String getDebitNoteIdFromPRId(String srid, String companyid) throws ServiceException {
        ArrayList params = new ArrayList();
        String debitNoteId = "";
        params.add(srid);
        params.add(companyid);
        String query = "select ID from DebitNote pr where pr.purchaseReturn.ID = ? and pr.company.companyID=? ";
        List list = executeQuery( query, params.toArray());
        if (!list.isEmpty() && !list.equals("null") && list.size() > 0) {
            debitNoteId = (String) list.get(0);
        }
        return debitNoteId;
    }
     public KwlReturnObject getAllUninvoicedConsignmentDetails(HashMap<String, Object> request) {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
             String companyid = (String) request.get(Constants.companyKey);
            String vendorid="";
            if (!StringUtil.isNullOrEmpty((String) request.get("vendorid"))) {
                vendorid = (String) request.get("vendorid");
            }
             ArrayList params= new  ArrayList();
             params.add(vendorid);
             params.add(companyid);
            String mysqlQuery = "select grd.product,sum(grd.actualquantity),sum(grd.deliveredquantity),sum(grd.baseuomquantity),sum(grd.baseuomdeliveredquantity),grd.description from grodetails as grd inner join grorder gr on gr.id=grd.grorder where gr.vendor=?  and gr.isconsignment='T' and gr.company=? and gr.deleteflag='F' group by grd.product "; 
            returnList = executeSQLQuery( mysqlQuery, params.toArray());
            totalCount = returnList.size();
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }
 public double getReturnQuantity(HashMap<String, Object> requestParams) throws ServiceException{
        double returnQuantity=0;
        String companyid = (String) requestParams.get(Constants.companyKey);
        String vendorid = "";
        String productid = "";
        if (!StringUtil.isNullOrEmpty((String) requestParams.get("vendorid"))) {
            vendorid = (String) requestParams.get("vendorid");
        }
        if (!StringUtil.isNullOrEmpty((String) requestParams.get("productid"))) {
            productid = (String) requestParams.get("productid");
        }
        
        ArrayList params = new ArrayList();
        params.add(vendorid);
        params.add(productid);
        params.add(companyid);
        String query = " select -sum(inventory.venconsignuomquantity) from purchasereturn  "
                + " inner join prdetails on prdetails.purchasereturn = purchasereturn.id  "
                + " inner join inventory on prdetails.id = inventory.id where purchasereturn.vendor=? and  inventory.product=? and  inventory.company=? and inventory.isconsignment='T' ";
        List list = executeSQLQuery( query, params.toArray());
        if (list.size() > 0 && !list.contains(null)) {
            returnQuantity = (Double) list.get(0); 
        }
        return returnQuantity;
    }

    public double getGRQuantityWhoseInvoiceCreated(HashMap<String, Object> requestParams) throws ServiceException {
        double quantity = 0;

        String companyid = "";
        String vendorId = "";
        String custWarehouse = "";
        String productid = "";
        String wareconditin = "";
        String custcondition = "";
        ArrayList params = new ArrayList();

        if (!StringUtil.isNullOrEmpty((String) requestParams.get("productid"))) {
            productid = (String) requestParams.get("productid");
            params.add(productid);
        }
//        params.add(companyid);
        if (!StringUtil.isNullOrEmpty((String) requestParams.get("vendorid"))) {
            vendorId = (String) requestParams.get("vendorid");
//            custcondition += " and  goodsreceipt.vendor=? ";
            params.add(vendorId);
        }

        if (!StringUtil.isNullOrEmpty((String) requestParams.get("companyid"))) {
            companyid = (String) requestParams.get("companyid");
//            custcondition += " and  goodsreceipt.vendor=? ";
            params.add(companyid);
        }

        String query = " select sum(inventory.quantity) from goodsreceipt inner join grdetails on grdetails.goodsreceipt =goodsreceipt.id  "
                + " inner join inventory on grdetails.id = inventory.id where inventory.product= ? and goodsreceipt.vendor=? and inventory.company=? and goodsreceipt.isconsignment='T'"
                + " and inventory.invrecord='F'";
        List list = executeSQLQuery(query, params.toArray());
        if (list.size() > 0 && !list.contains(null)) {
            quantity = (Double) list.get(0);
        }
        return quantity;
    }

    @Override
    public KwlReturnObject saveRepeateInvoiceInfo(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            RepeatedInvoices rinvoice = new RepeatedInvoices();
            if (dataMap.containsKey("id")) {
                rinvoice = (RepeatedInvoices) get(RepeatedInvoices.class, (String) dataMap.get("id"));
            }
            if (dataMap.containsKey("intervalType")) {
                rinvoice.setIntervalType((String) dataMap.get("intervalType"));
            }
            if (dataMap.containsKey("intervalUnit")) {
                rinvoice.setIntervalUnit((Integer) dataMap.get("intervalUnit"));
            }
            if (dataMap.containsKey("NoOfpost")) {
                rinvoice.setNoOfInvoicespost(((Integer) dataMap.get("NoOfpost")));
            }
            if (dataMap.containsKey("NoOfRemainpost")) {
                rinvoice.setNoOfRemainInvoicespost((Integer) dataMap.get("NoOfRemainpost"));
            }
            if (dataMap.containsKey("startDate")) {
                rinvoice.setStartDate((Date) dataMap.get("startDate"));
            }
            if (dataMap.containsKey("nextDate")) {
                rinvoice.setNextDate((Date) dataMap.get("nextDate"));
            }
            if (dataMap.containsKey("expireDate")) {
                rinvoice.setExpireDate((Date) dataMap.get("expireDate"));
            }
            if (dataMap.containsKey("isactivate")) {
                rinvoice.setIsActivate((Boolean)dataMap.get("isactivate"));
            } 
            if (dataMap.containsKey("ispendingapproval")) {
                rinvoice.setIspendingapproval((Boolean)dataMap.get("ispendingapproval"));
            }
            if (dataMap.containsKey("approver")) {
                rinvoice.setApprover((String) dataMap.get("approver"));
            }
            if (dataMap.containsKey("prevDate")) {
                rinvoice.setPrevDate((Date) dataMap.get("prevDate"));
            }
            if (dataMap.containsKey("advancedays")) {
                rinvoice.setAdvanceNoofdays((Integer) dataMap.get("advancedays"));
            }
            if (dataMap.containsKey("advanceDate")) {
                rinvoice.setInvoiceAdvanceCreationDate((Date) dataMap.get("advanceDate"));
            }
//            if (dataMap.containsKey("isCustomer")) {
//                rinvoice.setIscustomer(Boolean.parseBoolean((String)dataMap.get("isCustomer")));
//            }

            saveOrUpdate(rinvoice);
            list.add(rinvoice);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveRepeateInvoiceInfo : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
//       @Override
//    public CroneSchedule saveCroneDetails(String croneID, String croneName, Date executionDate) throws ServiceException {
//        CroneSchedule croneSchedule = (CroneSchedule) get(CroneSchedule.class, croneID);
//        if (croneSchedule == null) {
//            croneSchedule = new CroneSchedule();
//            croneSchedule.setId(croneID);
//            croneSchedule.setCroneName(croneName);
//        }
//
//        croneSchedule.setLastHit(executionDate);
//
//        saveOrUpdate(croneSchedule);
//
//        return croneSchedule;
//    }
//     @Override
//    public boolean isCroneExecutedForCurrentDay(String croneID, Date executionDate) throws ServiceException {
//        boolean isCroneExecutedForCurrentDay = false;
//        
//        List list = getCroneDetails(croneID, executionDate);
//        if (!list.isEmpty()) {
//            isCroneExecutedForCurrentDay = true;
//        }
//        return isCroneExecutedForCurrentDay;
//    }
//      public List getCroneDetails(String croneID, Date executionDate) throws ServiceException {
//        String hql = "FROM CroneSchedule where id=? and lastHit=?";
//        List list = executeQuery( hql, new Object[]{croneID, executionDate});
//        return list;
//    }
     public KwlReturnObject getExcludedInvoices(HashMap<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        Date currentDate = new Date();
        if (requestParams.containsKey("InvoicesOnDate") && requestParams.get("InvoicesOnDate") != null) {
            currentDate = (Date) requestParams.get("InvoicesOnDate");
        }
        String query = "from ExcludedOutstandingOrders where invoice is not null and generatedDate=? ";
        list = executeQuery( query, new Object[]{currentDate});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
  @Override
    public KwlReturnObject getRepeateVendorInvoices(HashMap<String, Object> requestParams) throws ServiceException {
        Date currentDate = new Date();
//        String companyid = (String)requestParams.get("companyid");
//        String query = "from Invoice where company.companyID = ? and repeateInvoice is not null and repeateInvoice.nextDate <= ? and (repeateInvoice.expireDate is null or repeateInvoice.expireDate >= ?)";
//        List list = executeQuery( query, new Object[]{companyid, currentDate, currentDate});
//        String query = "from GoodsReceipt where repeateInvoice is not null and (repeateInvoice.isActivate=true and repeateInvoice.ispendingapproval=false) and repeateInvoice.startDate<=now() and repeateInvoice.nextDate <= ? and (repeateInvoice.expireDate is null or repeateInvoice.expireDate >= ?)";
        String query = "from GoodsReceipt where repeateInvoice is not null and (repeateInvoice.isActivate=true and repeateInvoice.ispendingapproval=false) and repeateInvoice.invoiceAdvanceCreationDate <= ? and (repeateInvoice.expireDate is null or repeateInvoice.expireDate >= ?)";
        List list = executeQuery( query, new Object[]{currentDate, currentDate});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
   @Override
    public KwlReturnObject getRepeateBillingGoodsReceipt(HashMap<String, Object> requestParams) throws ServiceException {
        Date currentDate = new Date();
//        String companyid = (String)requestParams.get("companyid");
//        String query = "from BillingInvoice where company.companyID = ? and repeateInvoice is not null and repeateInvoice.nextDate <= ? and (repeateInvoice.expireDate is null or repeateInvoice.expireDate >= ?)";
//        List list = executeQuery( query, new Object[]{companyid, currentDate, currentDate});
        String query = "from BillingGoodsReceipt where repeateInvoice is not null and (repeateInvoice.isActivate=true and repeateInvoice.ispendingapproval=false) and startDate<=now() and repeateInvoice.nextDate <= ? and (repeateInvoice.expireDate is null or repeateInvoice.expireDate >= ?)";
        List list = executeQuery( query, new Object[]{currentDate, currentDate});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
   @Override
    public KwlReturnObject getRepeateVendorInvoicesDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String parentInvoiceId = (String) requestParams.get("parentInvoiceId");
        String query = "from GoodsReceipt where parentInvoice.ID = ? ";
        List list = executeQuery( query, new Object[]{parentInvoiceId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getRepeateVendorInvoicesDetailsForExpander(HashMap<String, Object> requestParams) throws ServiceException {
        String parentInvoiceId = (String) requestParams.get("parentInvoiceId");
        String companyid = (String) requestParams.get("companyid");
        String selQuery = "SELECT id,grnumber from goodsreceipt where company =? and parentinvoice = ?";
        List list = executeSQLQuery(selQuery, new Object[]{companyid,parentInvoiceId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getRepeateBillingGoodsReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String parentInvoiceId = (String) requestParams.get("parentInvoiceId");
        String query = "from BillingGoodsReceipt where parentInvoice.ID = ? ";
        List list = executeQuery( query, new Object[]{parentInvoiceId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     public KwlReturnObject getGoodsReceiptCount(String invoiceno, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from GoodsReceipt where goodsReceiptNumber=? and company.companyID=?";
        if (!StringUtil.isNullOrEmpty(invoiceno)) {
            list = executeQuery( q, new Object[]{invoiceno, companyid});
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    

 
    @Override
    public KwlReturnObject getAdvancePaymentsLinkedWithInvoice(String invoiceId, String companyId) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);
        String query = "select distinct payment,paymentlinkdate from linkdetailpayment lpd where lpd.goodsReceipt=? and lpd.company=?";

        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getSODeatils(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        String mysqlQuery = "";
        int count = 0;
        ArrayList params = new ArrayList();
        params.add((String) requestParams.get("productid"));
        params.add((String) requestParams.get("companyid"));
        mysqlQuery = "select sodetails.id,sodetails.lockquantitydue from sodetails "
                + " inner join salesorder on sodetails.salesorder=salesorder.id "
                + " where sodetails.product=? and salesorder.lockquantityflag=1 and  salesorder.isconsignment='T' and sodetails.lockquantitydue>0  and salesorder.company=? order by salesorder.createdon asc";
        list = executeSQLQuery( mysqlQuery, params.toArray());
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    public KwlReturnObject getPendingConsignmentRequests(String companyid,String productid) throws ServiceException {

        KwlReturnObject retObj = new KwlReturnObject(false, null, null, null, 0);
        List<String> dataList = new ArrayList();
        List params = new ArrayList();
        List<SalesOrder> soList = new ArrayList();


        String qry = " SELECT DISTINCT so.id FROM salesorder so INNER JOIN  sodetails sod ON sod.salesorder=so.id WHERE so.company= ? "
                + " AND sod.product=? AND so.isconsignment='T' AND so.lockquantityflag=1  AND sod.lockquantitydue > 0 "
                + " AND so.fromdate is NOT NULL AND so.fromdate >= ? "
                + " ORDER BY sod.product,so.fromdate ";

        params.add(companyid);
        params.add(productid);
        params.add(new Date());

        try {
            dataList = executeSQLQuery( qry, params.toArray());

            if (!dataList.isEmpty() && dataList != null) {
                for (int i = 0; i < dataList.size(); i++) {
                    String salesorderid = dataList.get(i);
                    if (!StringUtil.isNullOrEmpty(salesorderid)) {
                        SalesOrder so = (SalesOrder) get(SalesOrder.class, salesorderid);
                        if (so != null) {
                            soList.add(so);
                        }
                    }
                }
                retObj = new KwlReturnObject(true, null, null, soList, soList.size());
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.getStackTrace();
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retObj;
        }

    }
    
    public KwlReturnObject getConsignmentRequestApproverList(String ruleid) throws ServiceException {
        String query = "select crm.approver from ConsignmentRequestApproverMapping crm where crm.consignmentRequestRule.ID= ?  " ;//and crm.approver.userID != '0796ad1c-b33c-11e3-986d-7777670e1453'
        List list = executeQuery( query, new Object[]{ruleid});
        int count=list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
     @Override
    public KwlReturnObject approvePendinggr(String grID, String companyid, int status) throws ServiceException {
        String condition = "";
        if (status == 11) {
            condition = " ,pendingapproval=0 ";
        }
        String query = "update GoodsReceipt set approvestatuslevel = ? " + condition + " where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{status, grID, companyid});
        return new KwlReturnObject(true, "Invoice has been updated successfully.", null, null, numRows);
    }
     
     //Linking Information 
   public KwlReturnObject getSalesOrderMerged(HashMap<String, Object> request) throws ServiceException {
    List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            boolean isLeaseSO = false;
            boolean isConsignment = false;
            String conditionSQL="";
            if (request.containsKey("isLeaseFixedAsset") && request.get("isLeaseFixedAsset") != null) {
                isLeaseSO = (Boolean) request.get("isLeaseFixedAsset");
            }
            if (request.containsKey("isConsignment") && request.get("isConsignment") != null) {
                isConsignment = (Boolean) request.get("isConsignment");
            }

            ArrayList params = new ArrayList();
            String billId = "";
            if (request.containsKey("billId")) {
                billId = (String) request.get("billId");
            }
            String orderBy = "";
            String sort_Col = "";
            String sort_Col1 = "";
            String[] stringSort = null;
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                stringSort = columSort(Col_Name, Col_Dir);
                orderBy += stringSort[0];
                sort_Col += stringSort[1];
                sort_Col1 += stringSort[2];

            } else {
                if (isConsignment) {
                    //stringSort = columSort("billno", "desc");
                    orderBy = " order by orderdate desc,sonumber desc " ;
                    sort_Col +=", salesorder.orderdate ";
                    sort_Col1 +=  ", billingsalesorder.orderdate ";
                } else {
                    orderBy = " order by orderdate desc ";
                    sort_Col += ", salesorder.orderdate ";
                    sort_Col1 += ", billingsalesorder.orderdate ";
                }
            }
            
            String companyId = (String) request.get(Constants.companyKey);
            params.add((String) request.get(Constants.companyKey));
            
            if (request.containsKey("poid") && request.get("poid") != null) {
                String poid = (String) request.get("poid");
                params.add(poid);
                conditionSQL +="and purchaseorder.id= ?";
            }
            
            String mysqlQuery = " select DISTINCT salesorder.id,3, 'false' as withoutinventory " + sort_Col + " from salesorder "
                    + "inner join sodetails on sodetails.salesorder = salesorder.id "
                    + "inner join podetails on sodetails.purchaseorderdetailid = podetails.id"
                    + " inner join purchaseorder on podetails.purchaseorder = purchaseorder.id where salesorder.company=?"+conditionSQL;
            
            mysqlQuery += orderBy;
            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.getSalesOrders:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getPurchaseByVendor(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            DateFormat df = (DateFormat) request.get(Constants.df);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) get(CompanyAccountPreferences.class, companyid);
            Calendar startFinYearCal = Calendar.getInstance();
            Calendar endFinYearCal = Calendar.getInstance();
            startFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.add(Calendar.YEAR, 1);
            String vendorid = (String) request.get(GoodsReceiptConstants.VENDORID);
            String productid = (String) request.get(GoodsReceiptConstants.PRODUCTID);
            String productCategoryid = "";
            if (request.containsKey(Constants.productCategoryid) && request.get(Constants.productCategoryid) != null) {
                productCategoryid = (String) request.get(Constants.productCategoryid);
            }
            String prodFilterVenid =  (String) request.get("prodfiltercustid");
            if (vendorid == null) {
                vendorid = (String) request.get("accid");
            }
            String ss = (String) request.get(Constants.ss);
            ArrayList params = new ArrayList();
            String conditionSQL = "";
            params.add(companyid);
            if (!StringUtil.isNullOrEmpty(productid) && !StringUtil.equal(productid, "-1") && !StringUtil.equal(productid, "All")) {
                productid = AccountingManager.getFilterInString(productid);
                conditionSQL += " and inventory.product in " + productid + "  ";
            }
            if (!StringUtil.isNullOrEmpty(productCategoryid) && !StringUtil.equal(productCategoryid, "-1")) {
                params.add(productCategoryid);
                conditionSQL += " and inventory.product in (select productid from productcategorymapping where productcategory = ?)  ";
            }
            if (!StringUtil.isNullOrEmpty(prodFilterVenid) && !StringUtil.equal(prodFilterVenid, "-1") && !StringUtil.equal(prodFilterVenid, "All")) {
                prodFilterVenid = AccountingManager.getFilterInString(prodFilterVenid);
                conditionSQL += " and goodsreceipt.vendor in  " + prodFilterVenid + "  ";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
//                conditionSQL += " and (journalentry.entrydate >=? and journalentry.entrydate <=?) ";
                conditionSQL += " and (goodsreceipt.creationdate >=? and goodsreceipt.creationdate <=?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if (StringUtil.isNullOrEmpty(ss) == false) {
                for (int i = 0; i < 3; i++) {
                    params.add(ss + "%");
                }
                conditionSQL += " and (goodsreceipt.grnumber like ? or account.name like ? or product.name like ? ) ";
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
            String defaultCondition=" jedetail.id = goodsreceipt.centry ";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = request.get("searchJson").toString();
                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, appendCase);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
        
                    if (mySearchFilterString.contains("accjecustomdata")) {
                        joinString = " inner join accjecustomdata on accjecustomdata.journalentryId=goodsreceipt.journalentry ";
                    }
                    if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//    
                        joinString += " left join accjedetailcustomdata  on accjedetailcustomdata.jedetailId=jedetail.id ";
                        defaultCondition = " jedetail.journalentry = goodsreceipt.journalentry ";
                    }
                    if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "accjedetailproductcustomdata");//    
                        joinString += " left join accjedetailproductcustomdata  on accjedetailproductcustomdata.jedetailId=jedetail.id ";
                        defaultCondition = " jedetail.journalentry = goodsreceipt.journalentry ";
                    }
                    if (mySearchFilterString.contains("VendorCustomData")) {
                        joinString += " left join vendorcustomdata  on vendorcustomdata.vendorId=vendor.id ";
                        mySearchFilterString = mySearchFilterString.replaceAll("VendorCustomData", "vendorcustomdata");
                    }
                    //product custom data
                    if (mySearchFilterString.contains("accproductcustomdata")) {
                        joinString += "  left join accproductcustomdata on accproductcustomdata.productId=product.id ";
                    }
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            String mysqlQuery = "select DISTINCT goodsreceipt.id,  'false' as withoutinventory, journalentry.createdon, grdetails.id as invid   from goodsreceipt  "
                    + "inner join journalentry on goodsreceipt.journalentry = journalentry.id   "
                    + "inner join jedetail on "+defaultCondition
                    + " inner join account on account.id = jedetail.account "
                    + " inner join grdetails on grdetails.goodsreceipt = goodsreceipt.id "
                    + " inner join inventory on grdetails.id = inventory.id "
                    + " inner join vendor on vendor.id = goodsreceipt.vendor "
                    + " inner join product on product.id = inventory.product " + joinString
                    + " where goodsreceipt.company = ? and goodsreceipt.deleteflag='F' and goodsreceipt.pendingapproval=0 " + conditionSQL + mySearchFilterString + " "
                    + "order by createdon desc";
            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accInvoiceImpl.getInvoices:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    public KwlReturnObject getRepeatePurchaseInvoiceNo(Date prevDate) throws ServiceException {
        String query = "FROM GoodsReceipt WHERE repeateInvoice is not null and (repeateInvoice.isActivate=true and repeateInvoice.ispendingapproval=false) and ((repeateInvoice.prevDate = ? and repeateInvoice.nextDate <= repeateInvoice.expireDate) ";
        //getting repeate invoices for which prev date will be updated to today's date after repeated invoice creation 
        query += " or (repeateInvoice.invoiceAdvanceCreationDate=? and repeateInvoice.intervalUnit=1 and repeateInvoice.intervalType='day')) ";
        
        List list = executeQuery( query, new Object[]{prevDate, prevDate});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public synchronized String updateGREntryNumberForNewGR(Map<String, Object> seqNumberMap)throws AccountingException  {
        String documnetNumber = "";
        try {
            documnetNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
            int seqNumber = 0;
            if (seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())) {
                seqNumber = Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
                boolean isDraft = false;
                if(seqNumberMap.containsKey(Constants.isDraft) && seqNumberMap.get(Constants.isDraft)!=null){
                    isDraft = (Boolean)seqNumberMap.get(Constants.isDraft);  
                    if(isDraft){
                        documnetNumber = "";    //ERP-41992(Reference - SDP-13487) : Set PO no.empty
                        seqNumber = 0;
                    }
                }
                String datePrefix = seqNumberMap.containsKey(Constants.DATEPREFIX) ? (String) seqNumberMap.get(Constants.DATEPREFIX) : "";
                String dateafterPrefix = seqNumberMap.containsKey(Constants.DATEAFTERPREFIX) ? (String) seqNumberMap.get(Constants.DATEAFTERPREFIX) : "";  //Date After Prefix Part
                String dateSuffix = seqNumberMap.containsKey(Constants.DATESUFFIX) ? (String) seqNumberMap.get(Constants.DATESUFFIX) : "";
                String sequenceFormatID = seqNumberMap.containsKey(Constants.SEQUENCEFORMATID) ? (String) seqNumberMap.get(Constants.SEQUENCEFORMATID) : "";
                String documentID = seqNumberMap.containsKey(Constants.DOCUMENTID) ? (String) seqNumberMap.get(Constants.DOCUMENTID) : "";
                String companyID = seqNumberMap.containsKey(Constants.companyKey) ? (String) seqNumberMap.get(Constants.companyKey) : "";
                String query = "update GoodsReceiptOrder set goodsReceiptOrderNumber = ?,seqnumber=?,datePreffixValue=?, dateAfterPreffixValue=?, dateSuffixValue=? ,seqformat.ID=? where ID=? and company.companyID=?";
                int numRows = executeUpdate(query, new Object[]{documnetNumber, seqNumber, datePrefix, dateafterPrefix, dateSuffix, sequenceFormatID, documentID, companyID});
                String query1 = "update StockMovement set transactionNo = ? where moduleRefId=? and company.companyID=?"; // for update entry no in stock movement
                int numRows1 = executeUpdate(query1, new Object[]{documnetNumber, documentID, companyID});
            }else {
                throw new AccountingException(Constants.invalidSeqNumberMsg);
            }
        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage());
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return documnetNumber;
    }

    @Override
    public KwlReturnObject deleteAssetDetailsLinkedWithPurchaseReturn(HashMap<String, Object> requestParams) throws ServiceException {
        int numtotal = 0;
        String errormsg = "Cannot delete Purchase Return as its referance child field is not deleted.";
        try {
            if (requestParams.containsKey("prid") && requestParams.containsKey("companyid")) {

                int numRows = 0;
                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("companyid"));
                params8.add(requestParams.get("prid"));
                
                String assetQuery = "SELECT ad.id FROM purchasereturn pr "
                        + "INNER JOIN prdetails prd ON pr.id=prd.purchasereturn "
                        + "INNER JOIN assetdetailsinvdetailmapping amp ON prd.id=amp.invoicedetailid "
                        + "INNER JOIN assetdetail ad on ad.id=amp.assetdetails "
                        + "WHERE amp.moduleid=96 AND pr.company=? and pr.id=?";
                
                List<String> assetList = executeSQLQuery( assetQuery, params8.toArray());
                for (String assetID : assetList) {
                    ArrayList assetParams = new ArrayList();
                    assetParams.add(assetID);
                    assetParams.add(requestParams.get("companyid"));
                    
                    String assupdateQuery = "UPDATE assetdetail SET ispurchasereturn=false WHERE id=? AND company=?";
                    executeSQLUpdate( assupdateQuery, assetParams.toArray());
                }

                String myquery = "select id from prdetails where purchasereturn in (select id from purchasereturn where company = ? and id=?)";
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

                // Deleting data from assetdetailsinvdetailmapping
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    ArrayList assetParams = new ArrayList();
                    assetParams.add(requestParams.get("companyid"));

                    String assetMapDelQuery = "DELETE FROM assetdetailsinvdetailmapping WHERE invoicedetailid IN (" + idStrings + ") and moduleid=96 and company=?";
                    numRows += executeSQLUpdate( assetMapDelQuery, assetParams.toArray());
                }
                numtotal = numRows;
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE(errormsg, ex);
        }
        return new KwlReturnObject(true, "Purchase Return Asste Details has been deleted successfully.", null, null, numtotal);
    }

    @Override
    public synchronized String updatePIEntryNumberForNewPI(Map<String, Object> seqNumberMap) {
        String documnetNumber = "";
        try {
            documnetNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
            int seqNumber = 0;
            if (seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())) {
                seqNumber = Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
            }
            boolean isDraft = false;
            if(seqNumberMap.containsKey(Constants.isDraft) && seqNumberMap.get(Constants.isDraft)!=null){
                isDraft = (Boolean)seqNumberMap.get(Constants.isDraft);  
                if(isDraft){
                    documnetNumber = "";    //ERM-1238 (Reference - SDP-13487) : Set PI no.empty
                    seqNumber = 0;
                }
            }
            String datePrefix = seqNumberMap.containsKey(Constants.DATEPREFIX) ? (String) seqNumberMap.get(Constants.DATEPREFIX) : "";
            String dateafterPrefix = seqNumberMap.containsKey(Constants.DATEAFTERPREFIX) ? (String)seqNumberMap.get(Constants.DATEAFTERPREFIX) : "";  //Date After Prefix Part
            String dateSuffix = seqNumberMap.containsKey(Constants.DATESUFFIX) ? (String) seqNumberMap.get(Constants.DATESUFFIX) : "";
            String sequenceFormatID = seqNumberMap.containsKey(Constants.SEQUENCEFORMATID) ? (String) seqNumberMap.get(Constants.SEQUENCEFORMATID) : "";
            String documentID = seqNumberMap.containsKey(Constants.DOCUMENTID) ? (String) seqNumberMap.get(Constants.DOCUMENTID) : "";
            String companyID = seqNumberMap.containsKey(Constants.companyKey) ? (String) seqNumberMap.get(Constants.companyKey) : "";
            String query = "update GoodsReceipt set goodsReceiptNumber = ?,seqnumber=?, datePreffixValue=?, dateAfterPreffixValue=?, dateSuffixValue=? , seqformat.ID=? where ID=? and company.companyID=?";
            int numRows = executeUpdate(query, new Object[]{documnetNumber, seqNumber, datePrefix, dateafterPrefix, dateSuffix, sequenceFormatID, documentID, companyID});
        } catch (Exception e) {
            System.out.println(e);
        }
        return documnetNumber;
    }

    @Override
     public synchronized String updatePIEntryNumberForNA(String grid, String entrynumber) {
        try {
            String query = "update GoodsReceipt set goodsReceiptNumber = ? where ID=?";
            int numRows = executeUpdate( query, new Object[]{entrynumber,grid});
        } catch (Exception e) {
            System.out.println(e);
        }
        return entrynumber;
    }

    @Override
     public synchronized String updateGREntryNumberForNA(String grid, String GRNumber) {
        try {
            String query = "update GoodsReceiptOrder set goodsReceiptOrderNumber = ? where ID=?";
            int numRows = executeUpdate( query, new Object[]{GRNumber,grid});
            String query1 = "update StockMovement set transactionNo = ? where moduleRefId=?"; // for update entry no in stock movement
            int numRows1 = executeUpdate( query1, new Object[]{GRNumber,grid});
        } catch (Exception e) {
            System.out.println(e);
        }
        return GRNumber;
    }
    public KwlReturnObject getGoodsReceiptsOrderByCompany(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        String hqlQuery = "from GoodsReceiptOrder gr where gr.company.companyID=?";
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getAllGlobalGoodsReceiptsOrderOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        String sqlQuery = "select gro.id as groid,grotm.id as grotmid,gro.tax,grotm.termamount,tl.percent,grotm.term  from goodsreceiptordertermmap grotm  inner join grorder gro on gro.id=grotm.goodsreceiptorder  inner join taxlist tl on tl.tax=gro.tax  where grotm.termamount != 0 and gro.tax is not null and gro.company= ? ";
        list = executeSQLQuery( sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public boolean updateGoodsReceiptOrderAmount(GoodsReceiptOrder order, JSONObject json) throws ServiceException {
        boolean success = true;
        try {
            String companyid = json.optString("companyid");
            if (order != null) {
                if (json.has("discountAmountInBase")) { // quotation amount
                    order.setDiscountinbase(authHandler.round(json.optDouble("discountAmountInBase", 0.0), companyid));
                }
                if (json.has("totalAmountInDocumentCurrecy")) { // quotation amount in base
                    order.setTotalamount(authHandler.round(json.optDouble("totalAmountInDocumentCurrecy", 0.0), companyid));
                }
                if (json.has("totalAmountInBaseCurrecy")) { // Discount in base
                    order.setTotalamountinbase(authHandler.round(json.optDouble("totalAmountInBaseCurrecy", 0.0), companyid));
                }
                saveOrUpdate(order);
            }
        } catch (Exception ex) {
            success = false;
            System.out.println("accSalesOrderImpl.updateQuotation:" + ex.getMessage());
        }
        return success;
    }
    public KwlReturnObject getNormalGoodsReceipts(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(requestParams.get("companyid"));
            String hqlQuery = "from GoodsReceipt gr where gr.company.companyID=? and gr.normalInvoice=?";
            params.add(true);
            list = executeQuery( hqlQuery, params.toArray());
        } catch (Exception ex) {
            System.out.println("Exception:getNormalGoodsReceipts- " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     
    @Override
    public KwlReturnObject saveGoodsReceiptTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            GoodsReceiptOrderTermMap termmap = new GoodsReceiptOrderTermMap();

            GoodsReceiptOrder goodsReceiptOrder = null;
            if (dataMap.containsKey("term") && dataMap.containsKey("goodsReceiptOrderID")) {
                goodsReceiptOrder = (GoodsReceiptOrder) get(GoodsReceiptOrder.class, (String) dataMap.get("goodsReceiptOrderID"));
                InvoiceTermsSales term = (InvoiceTermsSales) get(InvoiceTermsSales.class, (String) dataMap.get("term"));
                List<GoodsReceiptOrderTermMap> listTermMap = find("from GoodsReceiptOrderTermMap where goodsReceiptOrder.ID = '" + goodsReceiptOrder.getID() + "' and term.id = '" + term.getId() + "'");
                if (listTermMap.size() > 0) {
                    termmap = listTermMap.get(0);
                }
                termmap.setTerm(term);
            }
            if (dataMap.containsKey("termamount")) {
                termmap.setTermamount((Double) dataMap.get("termamount"));
            }
            if (dataMap.containsKey("termtaxamount")) {
                termmap.setTermtaxamount((Double) dataMap.get("termtaxamount"));
            }
            if (dataMap.containsKey("termtaxamountinbase")) {
                termmap.setTermtaxamountinbase((Double) dataMap.get("termtaxamountinbase"));
            }
            if (dataMap.containsKey("termAmountExcludingTax")) {
                termmap.setTermAmountExcludingTax((Double) dataMap.get("termAmountExcludingTax"));
            }
            if (dataMap.containsKey("termAmountExcludingTaxInBase")) {
                termmap.setTermAmountExcludingTaxInBase((Double) dataMap.get("termAmountExcludingTaxInBase"));
            }
            if (dataMap.containsKey("termamountinbase")) {
                termmap.setTermamountinbase((Double) dataMap.get("termamountinbase"));
            }
            if (dataMap.containsKey("termtax") && dataMap.get("termtax") != null) {
                Tax termtax = (Tax) get(Tax.class, (String) dataMap.get("termtax"));
                termmap.setTermtax(termtax);
            }
            if (dataMap.containsKey("termpercentage")) {
                termmap.setPercentage((Double) dataMap.get("termpercentage"));
            }
            if (dataMap.containsKey("goodsReceiptOrderID") && goodsReceiptOrder != null) {
                termmap.setGoodsReceiptOrder(goodsReceiptOrder);
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
            throw ServiceException.FAILURE("accGoodsReceiptImpl.saveGoodsReceiptTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject updateGoodsReceiptOrderTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            GoodsReceiptOrderTermMap termmap = new GoodsReceiptOrderTermMap();

            if (dataMap.containsKey("ordertermid")) {
                termmap = (GoodsReceiptOrderTermMap) get(GoodsReceiptOrderTermMap.class, (String) dataMap.get("ordertermid"));
            }
            if(termmap!=null){
                if (dataMap.containsKey("termamount")) {
                    termmap.setTermamount((Double) dataMap.get("termamount"));
                }
                if (dataMap.containsKey("termtaxamount")) {
                    termmap.setTermtaxamount((Double) dataMap.get("termtaxamount"));
                }
                if (dataMap.containsKey("termtaxamountinbase")) {
                    termmap.setTermtaxamountinbase((Double) dataMap.get("termtaxamountinbase"));
                }
                if (dataMap.containsKey("termAmountExcludingTax")) {
                    termmap.setTermAmountExcludingTax((Double) dataMap.get("termAmountExcludingTax"));
                }
                if (dataMap.containsKey("termAmountExcludingTaxInBase")) {
                    termmap.setTermAmountExcludingTaxInBase((Double) dataMap.get("termAmountExcludingTaxInBase"));
                }
                if (dataMap.containsKey("termamountinbase")) {
                    termmap.setTermamountinbase((Double) dataMap.get("termamountinbase"));
                }
                if (dataMap.containsKey("termtax") && dataMap.get("termtax") != null) {
                    Tax termtax = (Tax) get(Tax.class, (String) dataMap.get("termtax"));
                    termmap.setTermtax(termtax);
                }
//                if (dataMap.containsKey("termpercentage")) {
//                    termmap.setPercentage((Double) dataMap.get("termpercentage"));
//                }
//                if (dataMap.containsKey("goodsReceiptOrderID") && goodsReceiptOrder != null) {
//                    termmap.setGoodsReceiptOrder(goodsReceiptOrder);
//                }
//                if (dataMap.containsKey("userid")) {
//                    User userid = (User) get(User.class, (String) dataMap.get("userid"));
//                    termmap.setCreator(userid);
//                }
//                if (dataMap.containsKey("createdon")) {
//                    termmap.setCreatedOn(((Date) dataMap.get("creationdate")).getTime());
//                }
                saveOrUpdate(termmap);
                list.add(termmap);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.saveGoodsReceiptTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject saveVILinking(Map<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String invoiceid = (String) hm.get("docid");
            GoodsReceiptLinking vilinking = new GoodsReceiptLinking();
            if (hm.containsKey("docid")) {
                GoodsReceipt vq = (GoodsReceipt) get(GoodsReceipt.class, invoiceid);
                vilinking.setDocID(vq);
            }
            if (hm.containsKey("moduleid")) {
                vilinking.setModuleID((Integer) hm.get("moduleid"));
            }
            if (hm.containsKey("linkeddocid")) {
                vilinking.setLinkedDocID((String) hm.get("linkeddocid"));
            }
            if (hm.containsKey("linkeddocno")) {
                vilinking.setLinkedDocNo((String) hm.get("linkeddocno"));
            }
            if (hm.containsKey("sourceflag")) {
                vilinking.setSourceFlag((Integer) hm.get("sourceflag"));
            }
            saveOrUpdate(vilinking);
            list.add(vilinking);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.saveVILinking : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveGRLinking(Map<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String groderid = (String) hm.get("docid");
            GoodsReceiptOrderLinking grlinking = new GoodsReceiptOrderLinking();
            if (hm.containsKey("docid")) {
                GoodsReceiptOrder groder = (GoodsReceiptOrder) get(GoodsReceiptOrder.class, groderid);
                grlinking.setDocID(groder);
 }
            if (hm.containsKey("moduleid")) {
                grlinking.setModuleID((Integer) hm.get("moduleid"));
            }
            if (hm.containsKey("linkeddocid")) {
                grlinking.setLinkedDocID((String) hm.get("linkeddocid"));
            }
            if (hm.containsKey("linkeddocno")) {
                grlinking.setLinkedDocNo((String) hm.get("linkeddocno"));
            }
            if (hm.containsKey("sourceflag")) {
                grlinking.setSourceFlag((Integer) hm.get("sourceflag"));
            }
            saveOrUpdate(grlinking);
            list.add(grlinking);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.saveGRLinking : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject savePRLinking(Map<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String groderid = (String) hm.get("docid");
            PurchaseReturnLinking prlinking = new PurchaseReturnLinking();
            if (hm.containsKey("docid")) {
                PurchaseReturn purchasereturn = (PurchaseReturn) get(PurchaseReturn.class, groderid);
                prlinking.setDocID(purchasereturn);
            }
            if (hm.containsKey("moduleid")) {
                prlinking.setModuleID((Integer) hm.get("moduleid"));
            }
            if (hm.containsKey("linkeddocid")) {
                prlinking.setLinkedDocID((String) hm.get("linkeddocid"));
            }
            if (hm.containsKey("linkeddocno")) {
                prlinking.setLinkedDocNo((String) hm.get("linkeddocno"));
            }
            if (hm.containsKey("sourceflag")) {
                prlinking.setSourceFlag((Integer) hm.get("sourceflag"));
            }
            saveOrUpdate(prlinking);
            list.add(prlinking);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.savePRLinking : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteLinkingInformationOfPI(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        int numRows1 = 0, numRows2 = 0, numRows3 = 0, numRows4 = 0, numRowsTotal = 0;
        try {
         
            String delQuery = "";

            if (requestParams.containsKey("unlinkflag") && requestParams.get("unlinkflag") != null && Boolean.parseBoolean(requestParams.get("unlinkflag").toString())) {
                int type = -1;
                if (requestParams.containsKey("type") && requestParams.get("type") != null) {
                    type = Integer.parseInt(requestParams.get("type").toString());
                }
                params.add(requestParams.get("linkedTransactionID"));
                params.add(requestParams.get("billid"));
                switch (type) {
                    case 0: // PI->GR
                        delQuery = "delete from GoodsReceiptOrderLinking grol where grol.DocID.ID=? and grol.LinkedDocID=?";
                        numRows3 = executeUpdate( delQuery, params.toArray());
                        delQuery = "delete from GoodsReceiptLinking grl where  grl.LinkedDocID=? and grl.DocID.ID=?";
                        numRows4 = executeUpdate( delQuery, params.toArray());
                        break;
                    case 1: // GR->PI
                        delQuery = "delete from GoodsReceiptOrderLinking grol where grol.DocID.ID=? and grol.LinkedDocID=?";
                        numRows3 = executeUpdate( delQuery, params.toArray());
                        delQuery = "delete from GoodsReceiptLinking grl where  grl.LinkedDocID=? and grl.DocID.ID=?";
                        numRows4 = executeUpdate( delQuery, params.toArray());
                        break;
                    case 2: // PO->PI
                        delQuery = "delete from PurchaseOrderLinking pol where pol.DocID.ID=? and pol.LinkedDocID=?";
                        numRows3 = executeUpdate( delQuery, params.toArray());
                        delQuery = "delete from GoodsReceiptLinking grl where  grl.LinkedDocID=? and grl.DocID.ID=?";
                        numRows4 = executeUpdate( delQuery, params.toArray());
                        break;
                    case 5: // VQ->PI
                        delQuery = "delete from VendorQuotationLinking vql where vql.DocID.ID=? and vql.LinkedDocID=?";
                        numRows2 = executeUpdate( delQuery, params.toArray());
                        delQuery = "delete from GoodsReceiptLinking grl where  grl.LinkedDocID=? and grl.DocID.ID=?";
                        numRows4 = executeUpdate( delQuery, params.toArray());
                        break;
                    case 4: // PI->DN 
                        delQuery = "delete from DebitNoteLinking dnl where dnl.DocID.ID=? and dnl.LinkedDocID=?";
                        numRows1 = executeUpdate( delQuery, params.toArray());
                        delQuery = "delete from GoodsReceiptLinking grl where  grl.LinkedDocID=? and grl.DocID.ID=?";
                        numRows4 = executeUpdate( delQuery, params.toArray());
                        break;

                    case 3: // PI->PR
                        delQuery = "delete from PurchaseReturnLinking prl where prl.DocID.ID=? and prl.LinkedDocID=?";
                        numRows1 = executeUpdate( delQuery, params.toArray());
                        delQuery = "delete from GoodsReceiptLinking grl where  grl.LinkedDocID=? and grl.DocID.ID=?";
                        numRows4 = executeUpdate( delQuery, params.toArray());
                        break;

                    case 8: // VQ->PI
                        delQuery = "delete from VendorQuotationLinking vql where vql.DocID.ID=? and vql.LinkedDocID=?";
                        numRows2 = executeUpdate( delQuery, params.toArray());
                        delQuery = "delete from GoodsReceiptLinking grl where  grl.LinkedDocID=? and grl.DocID.ID=?";
                        numRows4 = executeUpdate( delQuery, params.toArray());
                        break;
                    case 6: //PI->Advance Payment
                        delQuery = "delete from PaymentLinking pl where pl.DocID.ID  = ? and pl.LinkedDocID = ?";
                        numRows2 = executeUpdate( delQuery, params.toArray());
                        delQuery = "delete from GoodsReceiptLinking grl where grl.LinkedDocID = ? and grl.DocID.ID = ?";
                        numRows4 = executeUpdate( delQuery, params.toArray());
                }
            }else{
                params.add(requestParams.get("greceiptid"));
                delQuery = "delete from VendorQuotationLinking vq where vq.LinkedDocID=?";
                numRows1 = executeUpdate( delQuery, params.toArray());

                delQuery = "delete from PurchaseOrderLinking po where po.LinkedDocID=?";
                numRows2 = executeUpdate( delQuery, params.toArray());

                delQuery = "delete from GoodsReceiptOrderLinking grl where grl.LinkedDocID=?";
                numRows3 = executeUpdate( delQuery, params.toArray());

                delQuery = "delete from GoodsReceiptLinking gr where gr.DocID.ID=?";
                numRows4 = executeUpdate( delQuery, params.toArray());
            }


            numRowsTotal = numRows1 + numRows2 + numRows3 + numRows4;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("", ex);//+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, numRowsTotal);
    }

    @Override
    public KwlReturnObject deleteLinkingInformationOfGR(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        int numRows1 = 0, numRows2 = 0, numRows3 = 0, numRowsTotal = 0;
        String delQuery = "";
        try {
         
            if (requestParams.containsKey("unlinkflag") && requestParams.get("unlinkflag") != null && Boolean.parseBoolean(requestParams.get("unlinkflag").toString())) {
                int type = -1;
                if (requestParams.containsKey("type") && requestParams.get("type") != null) {
                    type = Integer.parseInt(requestParams.get("type").toString());
                }
                params.add(requestParams.get("doid"));
                params.add(requestParams.get("billid"));
                if (type == 0 || type == 1) {    // Type=0 GR->PI,  Type=1 PI->GR
                    delQuery = "delete from GoodsReceiptLinking gr where gr.LinkedDocID=? and gr.DocID.ID=?";
                    numRows1 = executeUpdate( delQuery, params.toArray());
                    delQuery = "delete from GoodsReceiptOrderLinking grl where grl.DocID.ID=? and grl.LinkedDocID=?";
                    numRows2 = executeUpdate( delQuery, params.toArray());
                } else if (type == 2) { // Type=2 PO->GR
                    delQuery = "delete from PurchaseOrderLinking po where po.LinkedDocID=? and po.DocID.ID=?";
                    numRows1 = executeUpdate( delQuery, params.toArray());
                    delQuery = "delete from GoodsReceiptOrderLinking grl where grl.DocID.ID=? and grl.LinkedDocID=?";
                    numRows2 = executeUpdate( delQuery, params.toArray());
                } else if (type == 3) { // Type=3 GR->PR
                    delQuery = "delete from PurchaseReturnLinking prl where prl.LinkedDocID=? and prl.DocID.ID=?";
                    numRows1 = executeUpdate( delQuery, params.toArray());
                    delQuery = "delete from GoodsReceiptOrderLinking grl where grl.DocID.ID=? and grl.LinkedDocID=?";
                    numRows2 = executeUpdate( delQuery, params.toArray());
                }
            } else {
                params.add(requestParams.get("doid"));
                delQuery = "delete from PurchaseOrderLinking po where po.LinkedDocID=?";
                numRows1 = executeUpdate( delQuery, params.toArray());

                delQuery = "delete from GoodsReceiptLinking gr where gr.LinkedDocID=?";
                numRows2 = executeUpdate( delQuery, params.toArray());

                delQuery = "delete from GoodsReceiptOrderLinking grl where grl.DocID.ID=?";
                numRows3 = executeUpdate( delQuery, params.toArray());
            }

            numRowsTotal = numRows1 + numRows2 + numRows3;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("", ex);//+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, numRowsTotal);
    }

    @Override
    public KwlReturnObject deleteLinkingInformationOfPR(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        int numRows1 = 0, numRows2 = 0, numRows3 = 0,  numRows4 = 0, numRowsTotal = 0;
        try {
             String delQuery ="";
            if (requestParams.containsKey("unlinkflag") && requestParams.get("unlinkflag") != null && Boolean.parseBoolean(requestParams.get("unlinkflag").toString())) {
                int type = -1;
                if (requestParams.containsKey("type") && requestParams.get("type") != null) {
                    type = Integer.parseInt(requestParams.get("type").toString());
                }
                params.add(requestParams.get("linkedTransactionID"));
                params.add(requestParams.get("prid"));
                switch (type) {
                    case 1: // PI->PR
                        delQuery = "delete from GoodsReceiptLinking grl where grl.DocID.ID=? and grl.LinkedDocID=?";
                        numRows1 = executeUpdate(delQuery, params.toArray());
                        delQuery = "delete from PurchaseReturnLinking prl where  prl.LinkedDocID=? and prl.DocID.ID=?";
                        numRows2 = executeUpdate(delQuery, params.toArray());
                        break;
                    case 2: // GR->PR
                        delQuery = "delete from GoodsReceiptOrderLinking grol where grol.DocID.ID=? and grol.LinkedDocID=?";
                        numRows1 = executeUpdate(delQuery, params.toArray());
                         delQuery = "delete from PurchaseReturnLinking prl where  prl.LinkedDocID=? and prl.DocID.ID=?";
                        numRows2 = executeUpdate(delQuery, params.toArray());
                        break;

                }
            } else {
                params.add(requestParams.get("prid"));
                delQuery = "delete from GoodsReceiptOrderLinking grl where grl.LinkedDocID=?";
                numRows1 = executeUpdate(delQuery, params.toArray());

                delQuery = "delete from GoodsReceiptLinking gr where gr.LinkedDocID=?";
                numRows2 = executeUpdate(delQuery, params.toArray());

                delQuery = "delete from DebitNoteLinking dn where dn.LinkedDocID=?";
                numRows3 = executeUpdate(delQuery, params.toArray());

                delQuery = "delete from PurchaseReturnLinking prl where prl.DocID.ID=?";
                numRows4 = executeUpdate(delQuery, params.toArray());
            }
 

            numRowsTotal = numRows1 + numRows2 + numRows3;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("", ex);//+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, numRowsTotal);
    }

    @Override
    public KwlReturnObject checkEntryForGoodsReceiptInLinkingTable(String docid, String linkeddocid) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(docid);
        params.add(linkeddocid);
        String hqlQuery = "from GoodsReceiptLinking grl where grl.DocID.ID=? and grl.LinkedDocID=?";
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject checkEntryForGoodsReceiptOrderInLinkingTable(String docid, String linkeddocid) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(docid);
        params.add(linkeddocid);
        String hqlQuery = "from GoodsReceiptOrderLinking grol where grol.DocID.ID=? and grol.LinkedDocID=?";
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject checkEntryForPurchaseReturnInLinkingTable(String docid, String linkeddocid) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(docid);
        params.add(linkeddocid);
        String hqlQuery = "from PurchaseReturnLinking prl where prl.DocID.ID=? and prl.LinkedDocID=?";
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject checkEntryForPurchaseOrderInLinkingTable(String docid, String linkeddocid) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(docid);
        params.add(linkeddocid);
        String hqlQuery = "from PurchaseOrderLinking porl where porl.DocID.ID=? and porl.LinkedDocID=?";
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }


    @Override
    public KwlReturnObject getPurchaseOrderLinkedWithGR(String invoiceId, String companyId) throws ServiceException {
         List list = new ArrayList();
        try {
            String hqlQuery = "select distinct pod.purchaseOrder from GoodsReceiptOrderDetails grod inner join grod.podetails pod where grod.grOrder.ID=? and grod.company.companyID=? and pod.purchaseOrder.deleted=false";
            list = executeQuery( hqlQuery, new Object[]{invoiceId, companyId});
        } catch (Exception ex) {
            System.out.println("Exception: getPurchaseOrderLinkedWithGR " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getInvoiceDetailsFromGR(String goodsReceiptId, String invoiceId, String companyid) throws ServiceException {
        List<GoodsReceiptDetail> list = null;
        int count = 0;
        try {
            String hqlQuery = "select grd from GoodsReceiptDetail grd inner join grd.goodsReceiptOrderDetails grod where grd.goodsReceipt.ID=? and grod.grOrder.ID=? and grd.company.companyID=?";
            list = executeQuery( hqlQuery, new Object[]{invoiceId, goodsReceiptId, companyid});
            if (list != null) {
                count = list.size();
            }
        } catch (Exception ex) {
            System.out.println("Exception: getInvoiceDetailsFromGR() " + ex.getMessage());
        } finally {
            return new KwlReturnObject(true, "", null, list, count);
        }
    }

    @Override
    public KwlReturnObject getGRDetailsFromPI(String goodsReceiptId, String invoiceId, String companyid) throws ServiceException {
        List<GoodsReceiptDetail> list = null;
        int count = 0;
        try {
              String hqlQuery = "select grod from GoodsReceiptOrderDetails grod inner join grod.videtails invd where grod.grOrder.ID=? and invd.goodsReceipt.ID=? and grod.company.companyID=?";
            list = executeQuery( hqlQuery, new Object[]{goodsReceiptId, invoiceId, companyid});
            if (list != null) {
                count = list.size();
            }
        } catch (Exception ex) {
            System.out.println("Exception: getGRDetailsFromPI() " + ex.getMessage());
        } finally {
            return new KwlReturnObject(true, "", null, list, count);
        }
    }
         
    @Override
    public KwlReturnObject getGRDetailsFromPO(String goodsReceiptId, String purchaseOrderId, String companyid) throws ServiceException {
       List<GoodsReceiptDetail> list = null;
        int count = 0;
        try {
              String hqlQuery = "select grod from GoodsReceiptOrderDetails grod inner join grod.podetails pod where grod.grOrder.ID=? and pod.purchaseOrder.ID=? and grod.company.companyID=?";
            list = executeQuery( hqlQuery, new Object[]{goodsReceiptId, purchaseOrderId, companyid});
            if (list != null) {
                count = list.size();
            }
        } catch (Exception ex) {
            System.out.println("Exception: getGRDetailsFromPO() " + ex.getMessage());
        } finally {
            return new KwlReturnObject(true, "", null, list, count);
        }
    }

    @Override
    public KwlReturnObject getPurchaseReturnDetailsFromGR(String goodsReceiptId, String purchaseReturnId, String companyid) throws ServiceException {
         List<PurchaseReturn> list = null;
        int count=0;
        try {
            String selQuery = "select prd from PurchaseReturnDetail prd inner join prd.grdetails grod where prd.purchaseReturn.ID=? and grod.grOrder.ID=? and prd.purchaseReturn.deleted=false and prd.company.companyID=? and grod.grOrder.deleted=false";
            list = executeQuery( selQuery, new Object[]{purchaseReturnId,goodsReceiptId, companyid});
            if(list!=null){
                count=list.size();
            }
        } catch (Exception ex) {
            System.out.println("Exception: getPurchaseReturnDetailsFromGR " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list,count);
    }

    @Override
    public KwlReturnObject checkPOLinkedWithAnotherGR(String poid) throws ServiceException {
        List list = new ArrayList();
      
        String selQuery = "select count(*) from GoodsReceiptOrderLinking grol  where grol.LinkedDocID = ?";
        list = executeQuery( selQuery, new Object[]{poid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
 
       @Override
    public KwlReturnObject updatePOBalanceQtyAfterGR(String doid, String linkedDocumentID, String companyid) throws ServiceException {
        try {
            KwlReturnObject resultPo = getPurchaseOrdersDetailsforBalanceqty(doid, companyid);
            List listPo = resultPo.getEntityList();
            Iterator itrPodetails = listPo.iterator();
            while (itrPodetails.hasNext()) {
                Object[] objArr = (Object[]) itrPodetails.next();
                if (objArr != null && (objArr[0]!=null||objArr[2]!=null||objArr[3]!=null)) {
                    PurchaseOrderDetail podetails=null;
                    SecurityGateDetails secustiryGateDetails=null;
                    String poDetailsId =(objArr[0]!=null)?(String) objArr[0]:(objArr[2]!=null)?(String)objArr[2]:(String)objArr[3];
                    if ((String) objArr[2] != null) {
                        GoodsReceiptDetail grdeatils = (GoodsReceiptDetail) get(GoodsReceiptDetail.class, (String) objArr[2]);
                        if (grdeatils.getPurchaseorderdetail() != null) {
                            poDetailsId = grdeatils.getPurchaseorderdetail().getID();
                        }
                    }
                    /*
                     * while linking security gate entry to GR 
                     */
                    boolean isPODFromSecurityGateEntry = false;
                    if ((String) objArr[3] != null) {
                        secustiryGateDetails = (SecurityGateDetails) get(SecurityGateDetails.class, (String) objArr[3]);
                        if (secustiryGateDetails != null) {
                            poDetailsId = secustiryGateDetails.getID();
                            isPODFromSecurityGateEntry = true;
                        }
                    }
                    double grQty = (double) objArr[1];
                    if ((String) objArr[3] != null) {
                        secustiryGateDetails = (SecurityGateDetails) get(SecurityGateDetails.class, poDetailsId);
                        // while deleting GR need to check Secutiry Gate entry
                        if(secustiryGateDetails!=null && secustiryGateDetails.getPodetail()!=null){
                            podetails=(PurchaseOrderDetail) get(PurchaseOrderDetail.class, secustiryGateDetails.getPodetail().getID());
                        }
                    } else {
                        podetails = (PurchaseOrderDetail) get(PurchaseOrderDetail.class, poDetailsId);
                    }
                    boolean unlinkFlag = false;
                    if (!StringUtil.isNullOrEmpty(linkedDocumentID) && !podetails.getPurchaseOrder().getID().equals(linkedDocumentID)) {
                        unlinkFlag = true;
                    }
                    /*
                     * unlinkSGEFlag is used for to check 
                     * security gate entry which is link to gr or not
                     */
                    boolean unlinkSGEFlag = false;
                    if (!StringUtil.isNullOrEmpty(linkedDocumentID) && !StringUtil.isNullObject(secustiryGateDetails) && !secustiryGateDetails.getSecurityGateEntry().getID().equals(linkedDocumentID)) {
                        unlinkSGEFlag = true;
                    }
                    
                    if (!unlinkFlag) {
                        HashMap hMap = new HashMap();
                        hMap.put("podetails", podetails);
                        hMap.put("companyid", companyid);
                        hMap.put("balanceqty", grQty);
                        hMap.put("add", true);
                        updatePurchaseOrderStatus(hMap);
                        
                        if (grQty > 0 && !isPODFromSecurityGateEntry) {
                            HashMap poMap = new HashMap();
                            if (objArr != null && !StringUtil.isNullOrEmpty(poDetailsId)) {
                                if (podetails != null) {
                                    poMap.put("purchaseOrder", podetails.getPurchaseOrder());

                                    /* If balance quantity of linked PO with GR is equal to base quantity then 
                                     * 
                                     We do free PO and setting linkflag =0 */
                                    if (podetails.getBalanceqty() == podetails.getQuantity()) {
                                        poMap.put("value", "0");
                                    } else {
                                        poMap.put("value", "2");
                                    }
                                    if (podetails.getBalanceqty() > 0) {
                                        poMap.put("isOpen", true);
                                    } else {
                                        poMap.put("isOpen", false);
                                    }
                                    updatePOLinkflag(poMap);
                                }
                            }
                        }
                    }
                    if (!unlinkSGEFlag) {
                        HashMap hMap = new HashMap();
                        HashMap pMap = new HashMap();
                        hMap.put("securityGateDetails", secustiryGateDetails);
                        hMap.put("companyid", companyid);
                        hMap.put("balanceqty", grQty);
                        hMap.put("add", true);
                        /*
                         * If Security gate Entry is link to gr
                         * While deletig gr then updating quantity of security gate entry
                         */
                        updateSecurityGateStatus(hMap);
                        
                            if (grQty > 0 && isPODFromSecurityGateEntry) {
                                if (podetails.getPurchaseOrder() != null) {
                                    if (podetails.getPurchaseOrder() != null && !StringUtil.isNullOrEmpty(podetails.getPurchaseOrder().getID())) {
                                        if (podetails.getBalanceqty() == podetails.getQuantity()) {
                                            pMap.put("value", "0");
                                        } else {
                                            pMap.put("value", "3");
                                        }
                                        if (podetails.getBalanceqty() > 0) {
                                            pMap.put("isOpen", true);
                                        } else {
                                            pMap.put("isOpen", false);
                                        }
                                        pMap.put("purchaseOrder", podetails.getPurchaseOrder());
                                        updatePOLinkflag(pMap);
                                    }
                                }
                            }
                        
                    }
            }
            }
       } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.addGoodsReceipt : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "updateSOBalanceQty.", null, null, 0);
    }
//this function is useful when we return any transction with link to GR and if that GR is linked with PO then we will update the PO status
public KwlReturnObject updatePOBalanceQtyAfterPR(String prid, String companyid) throws ServiceException {
        try {
           KwlReturnObject resultPo = getPoDetailsFromGRODetailsforBalanceqty(prid, companyid);
                    List listPo = resultPo.getEntityList();
                    Iterator itrPodetails = listPo.iterator();
                    while (itrPodetails.hasNext()) {
                        Object[] objArr = (Object[]) itrPodetails.next();
                        PurchaseOrderDetail podetails=null;
                        if (objArr != null && (objArr[0]!=null||objArr[2]!=null)) {
                            String poDetailsId = (objArr[0]!=null)?(String) objArr[0]:(String)objArr[2];
                            double prQty = (double) objArr[1];
                            if ((String) objArr[2] != null) {
                                GoodsReceiptDetail grdeatils = (GoodsReceiptDetail) get(GoodsReceiptDetail.class, (String) objArr[2]);
                                if (grdeatils.getPurchaseorderdetail() != null) {
                                    poDetailsId = grdeatils.getPurchaseorderdetail().getID();
                                }
                            }
                            HashMap hMap = new HashMap();
                            if (!StringUtil.isNullOrEmpty(poDetailsId)) {
                                podetails = (PurchaseOrderDetail) get(PurchaseOrderDetail.class, poDetailsId);
                                if (podetails != null) {
                                    hMap.put("podetails", podetails);
                                    hMap.put("companyid", companyid);
                                    hMap.put("balanceqty", prQty);
                                    hMap.put("add", false);
                                    updatePurchaseOrderStatus(hMap);
                                }
                            }
                            
                            
                               if (prQty > 0) {
                                    HashMap poMap = new HashMap();
//                                    if (objArr != null && !StringUtil.isNullOrEmpty(poDetailsId)) {
//                                        PurchaseOrderDetail podetails = (PurchaseOrderDetail) get(PurchaseOrderDetail.class, poDetailsId);
                                        if (podetails != null) {
                                            poMap.put("purchaseOrder", podetails.getPurchaseOrder());
                                            poMap.put("value", "2");
                                            if (podetails.getBalanceqty() > 0) {
                                                poMap.put("isOpen", true);
                                            } else {
                                                poMap.put("isOpen", false);
                                            }
                                            updatePOLinkflag(poMap);
                                        }
                                    }
//                                }
                        }
                    }
       } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.addGoodsReceipt : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "updateSOBalanceQty.", null, null, 0);
    }
    public KwlReturnObject updatePurchaseOrderStatus(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            double balanceqty = 0;
            String query = "";
            boolean add = false;
            boolean update = false;
            if (requestParams.containsKey("add") && requestParams.get("add") != null && requestParams.get("add") != "") {
                add = (Boolean) requestParams.get("add");
            }
            if (requestParams.containsKey("update") && requestParams.get("update") != null && requestParams.get("update") != "") {
                update = (Boolean) requestParams.get("update");
            }
     
            if (requestParams.containsKey("balanceqty") && requestParams.get("balanceqty") != null && requestParams.get("balanceqty") != "") {
                balanceqty = (Double) requestParams.get("balanceqty");
            }
            PurchaseOrderDetail podetails = (PurchaseOrderDetail) requestParams.get("podetails");
            if (podetails != null) {
                if (add) {
                    podetails.setBalanceqty(podetails.getBalanceqty() + balanceqty);
                    
                } else if (update) {
                    podetails.setBalanceqty(balanceqty);
                    
                    } else {
                    podetails.setBalanceqty(podetails.getBalanceqty() - balanceqty);
                    
                    }
                saveOrUpdate(podetails);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updateDeliveryOrderStatus:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    /**
     * To update while deleting Gr in security get entry inking case 
     * @param requestParams
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject updateSecurityGateStatus(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            double balanceqty = 0;
            String query = "";
            boolean add = false;
            boolean update = false;
            if (requestParams.containsKey("add") && requestParams.get("add") != null && requestParams.get("add") != "") {
                add = (Boolean) requestParams.get("add");
            }
            
            if (requestParams.containsKey("balanceqty") && requestParams.get("balanceqty") != null && requestParams.get("balanceqty") != "") {
                balanceqty = (Double) requestParams.get("balanceqty");
            }
            SecurityGateDetails sgeDetails = (SecurityGateDetails) requestParams.get("securityGateDetails");
            if (sgeDetails != null) {
                if (add) {
                    sgeDetails.setBalanceqty(sgeDetails.getBalanceqty() + balanceqty);
                }
                saveOrUpdate(sgeDetails);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updateSecurityGateStatus:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    
    public KwlReturnObject getPurchaseOrdersDetailsforBalanceqty(String groid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "  select podetails,deliveredquantity,videtails,securitydetails from grodetails  where grorder=? and company=?";
        list = executeSQLQuery( query, new Object[]{groid, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getPoDetailsFromGRODetailsforBalanceqty(String prid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "  select gro.podetails,pr.returnquantity,gro.videtails from prdetails pr inner join grodetails gro on gro.id=pr.grdetails where pr.purchasereturn=? and pr.company=?";
        list = executeSQLQuery( query, new Object[]{prid, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getVQlinkedInPO(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            String companyid = "", poid = "";
            if (request.containsKey("poid") && request.containsKey("companyid")) {
                companyid = (String) request.get("companyid");
                poid = (String) request.get("poid");
            }
            if (!StringUtil.isNullOrEmpty(poid) && !StringUtil.isNullOrEmpty(companyid)) {
                String selQuery = "select distinct quotation.ID from PurchaseOrderDetail pod inner join pod.vqdetail vqd inner join vqd.vendorquotation quotation where pod.purchaseOrder.ID=? and quotation.deleted=false and vqd.company.companyID=?";
                list = executeQuery( selQuery, new Object[]{poid, companyid});
            }
        } catch (Exception ex) {
            System.out.println("Exception: getVQlinkedInPO " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getSOLinkedInPO(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            String companyid = "", poid = "";
            if (request.containsKey("poid") && request.containsKey("companyid")) {
                companyid = (String) request.get("companyid");
                poid = (String) request.get("poid");
            }
            if (!StringUtil.isNullOrEmpty(poid) && !StringUtil.isNullOrEmpty(companyid)) {
                String sqlQuery = "select DISTINCT salesorder.id, 2, 'false' as withoutinventory, salesorder.orderdate  from salesorder inner join sodetails on sodetails.salesorder = salesorder.id inner join podetails on podetails.salesorderdetailid = sodetails.id inner join purchaseorder on podetails.purchaseorder = purchaseorder.id\n"
                        + "where salesorder.company=? and purchaseorder.id= ? order by orderdate desc ";

                list = executeSQLQuery( sqlQuery, new Object[]{companyid, poid});
                if (list != null && list.size() > 0) {
                    count = list.size();
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception: getSOLinkedInPO " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getPIDetailsFromPO(String invoiceId, String purchaseOrderId, String companyid) throws ServiceException {
        List<GoodsReceiptDetail> list = null;
        int count = 0;
        try {
            String hqlQuery = "select grd from GoodsReceiptDetail grd inner join grd.purchaseorderdetail pod where grd.goodsReceipt.ID=? and pod.purchaseOrder.ID=? and grd.company.companyID=?";
            list = executeQuery( hqlQuery, new Object[]{invoiceId, purchaseOrderId, companyid});
            if (list != null) {
                count = list.size();
            }
        } catch (Exception ex) {
            System.out.println("Exception: getGRDetailsFromPO() " + ex.getMessage());
        } finally {
            return new KwlReturnObject(true, "", null, list, count);
        }
    }
    
    @Override
    public KwlReturnObject getExpensePIDetailsFromPO(String invoiceId, String purchaseOrderId, String companyid) throws ServiceException {
        List<ExpenseGRDetail> list = null;
        int count = 0;
        try {
            String hqlQuery = "select grd from ExpenseGRDetail grd inner join grd.expensePODetail pod where grd.goodsReceipt.ID=? and pod.purchaseOrder.ID=? and grd.goodsReceipt.deleted=false and grd.company.companyID=? and pod.purchaseOrder.deleted=false";
            list = executeQuery( hqlQuery, new Object[]{invoiceId, purchaseOrderId, companyid});
            if (list != null) {
                count = list.size();
            }
        } catch (Exception ex) {
            System.out.println("Exception: getGRDetailsFromPO() " + ex.getMessage());
        } finally {
            return new KwlReturnObject(true, "", null, list, count);
        }
    }

    @Override
    public KwlReturnObject checkPOLinkedWithAnotherPI(String poid) throws ServiceException {
       List list = new ArrayList();
      
        String selQuery = "select count(*) from GoodsReceiptLinking grl  where grl.LinkedDocID = ?";
        list = executeQuery( selQuery, new Object[]{poid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject checkVQLinkedWithAnotherPO(String billid) throws ServiceException {
          List list = new ArrayList();
      
        String selQuery = "select count(*) from PurchaseOrderLinking pol  where pol.LinkedDocID = ?";
        list = executeQuery( selQuery, new Object[]{billid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getVQLinkedWithPI(String invoiceid, String companyid) throws ServiceException {
        List<Quotation> list = null;
        int count = 0;
        try {
            String selQuery = "select distinct vqd.vendorquotation from GoodsReceiptDetail grd inner join grd.vendorQuotationDetail vqd where grd.goodsReceipt.ID=? and grd.company.companyID=? and vqd.vendorquotation.deleted=false";
            list = executeQuery( selQuery, new Object[]{invoiceid, companyid});
            if (list != null) {
                count = list.size();
            }
        } catch (Exception ex) {
            System.out.println("Exception: getVQLinkedWithPI " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getPurchaseOrderLinkedWithPI(String invoiceId, String companyId) throws ServiceException {
         List list = new ArrayList();
        try {
            String hqlQuery = "select distinct pod.purchaseOrder from GoodsReceiptDetail grd inner join grd.purchaseorderdetail pod where grd.goodsReceipt.ID=? and grd.company.companyID=? and pod.purchaseOrder.deleted=false";
            list = executeQuery( hqlQuery, new Object[]{invoiceId, companyId});
        } catch (Exception ex) {
            System.out.println("Exception: getPurchaseOrderLinkedWithPI " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getPurchaseOrderLinkedWithExpensePI(String invoiceId, String companyId) throws ServiceException {
         List list = new ArrayList();
        try {
            String hqlQuery = "select distinct pod.purchaseOrder from ExpenseGRDetail grd inner join grd.expensePODetail pod where grd.goodsReceipt.ID=? and grd.goodsReceipt.deleted=false and grd.company.companyID=? and pod.purchaseOrder.deleted=false";
            list = executeQuery( hqlQuery, new Object[]{invoiceId, companyId});
        } catch (Exception ex) {
            System.out.println("Exception: getPurchaseOrderLinkedWithExpensePI " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getGoodsReceiptLinkedWithPurchaseInvoice(String invoiceid, String companyId) throws ServiceException {
        List list = new ArrayList();
        List list1 = new ArrayList();
        List list2 = new ArrayList();
        try {
            // Type=0 PI->GR
            String selQuery = "select distinct grod.grOrder,0 from GoodsReceiptOrderDetails grod inner join grod.videtails vid where vid.goodsReceipt.ID=? and vid.goodsReceipt.deleted=false and vid.company.companyID=?";
            list1 = executeQuery( selQuery, new Object[]{invoiceid, companyId});
            // Type=1 GR->PI
            String hqlQuery = "select distinct grod.grOrder,1 from GoodsReceiptDetail grd inner join grd.goodsReceiptOrderDetails grod where grd.goodsReceipt.ID=? and grd.company.companyID=? and grod.grOrder.deleted=false";
            list2 = executeQuery( hqlQuery, new Object[]{invoiceid, companyId});
            list.addAll(list1);
            list.addAll(list2);
        } catch (Exception ex) {
            System.out.println("Exception: getGoodsReceiptLinkedWithPurchaseInvoice " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getGR_Crosslinked_PI(HashMap<String,Object> request) throws ServiceException {
        List list = new ArrayList();
        List params = new ArrayList();
        List invparams = new ArrayList();
        List groparams = new ArrayList();
        String groQuery = "",invoiceQuery = "";
        String groCondQuery = "",invoiceCondQuery = "";
        
        try {
            
            DateFormat df = null;
            if(request.containsKey(Constants.df) && request.get(Constants.df) != null){
                df = (DateFormat) request.get(Constants.df);
            }
            
            groQuery = " select distinct '1' as transactionType, invl.linkeddocid, invl.docid, je.entrydate as date from goodsreceiptorderlinking invl "
                    + " inner join goodsreceipt inv on inv.id=invl.linkeddocid  "
                    + " inner join journalentry je on je.id=inv.journalentry  "
                    + " inner join grorder grdo on grdo.id=invl.docid ";
            
            if(request.containsKey("companyid") && request.get("companyid") != null){
                groCondQuery += " and inv.company=? ";
                groparams.add(request.get("companyid"));
            }
            if(request.containsKey("sourceflag") && request.get("invoicemoduleid") != null){
                groCondQuery += " and invl.moduleid=? ";
                groparams.add(request.get("invoicemoduleid"));
            }
            if(request.containsKey("sourceflag") && request.get("sourceflag") != null){
                groCondQuery += " and invl.sourceflag=? ";
                groparams.add(request.get("sourceflag"));
            }
            groQuery += " where grdo.deleteflag = 'F' and inv.deleteflag = 'F' "+groCondQuery;
            
             invoiceQuery = " select distinct '2' as transactionType, invl.docid, invl.linkeddocid, je.entrydate as date from goodsreceiptlinking invl "
                    + " inner join goodsreceipt inv on inv.id=invl.docid  "
                    + " inner join journalentry je on je.id=inv.journalentry  "
                    + " inner join grorder gro on gro.id=invl.linkeddocid ";
            
            if(request.containsKey("companyid") && request.get("companyid") != null){
                invoiceCondQuery += " and inv.company=? ";
                invparams.add(request.get("companyid"));
            }
            if(request.containsKey("gromoduleid") && request.get("gromoduleid") != null){
                invoiceCondQuery += " and invl.moduleid=? ";
                invparams.add(request.get("gromoduleid"));
            }
            if(request.containsKey("sourceflag") && request.get("sourceflag") != null){
                invoiceCondQuery += " and invl.sourceflag=? ";
                invparams.add(request.get("sourceflag"));
            }
            invoiceQuery += " where gro.deleteflag = 'F' and inv.deleteflag = 'F' "+invoiceCondQuery;
            
            String startDate = "";
            if(request.containsKey(Constants.REQ_startdate) && request.get(Constants.REQ_startdate) != null){
                startDate = (String) request.get(Constants.REQ_startdate);
            }
            String endDate = "";
            if(request.containsKey(Constants.REQ_enddate) && request.get(Constants.REQ_enddate) != null){
                endDate = (String) request.get(Constants.REQ_enddate);
            }
            
            params.addAll(groparams);
            params.addAll(invparams);
            
            String conditionQuery = "";
            if ( df != null && !StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate) ) {
                conditionQuery += " (t1.date >=? and t1.date <=?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            
            String unionQry=" ("
                        + " (" + groQuery + ") "
                        + " UNION "
                        + " (" + invoiceQuery + ") "
                        + " ) ";
            String mysqlquery = " select * from "+unionQry+" as t1" + conditionQuery + " order by t1.date ";
            
            list = executeSQLQuery( mysqlquery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getGR_Interlinked_PI : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getVendorInvoiceInvoiceLinkedWithPI(String invoiceId, String companyId) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);
        String selQuery = "select distinct quotation.ID from GoodsReceiptDetail grd inner join grd.vendorQuotationDetail vqd inner join vqd.vendorquotation quotation where grd.goodsReceipt.ID=? and quotation.deleted=false and vqd.company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{invoiceId, companyId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPIDetailsFromVQ(String invoiceId, String vendorQuotationId, String companyid) throws ServiceException {
        List<GoodsReceiptDetail> list = null;
        int count = 0;
        try {
            String hqlQuery = "select grd from GoodsReceiptDetail grd inner join grd.vendorQuotationDetail vqd where grd.goodsReceipt.ID=? and vqd.vendorquotation.ID=? and grd.company.companyID=?";
            list = executeQuery( hqlQuery, new Object[]{invoiceId, vendorQuotationId, companyid});
            if (list != null) {
                count = list.size();
            }
        } catch (Exception ex) {
            System.out.println("Exception: getPIDetailsFromVQ() " + ex.getMessage());
        } finally {
            return new KwlReturnObject(true, "", null, list, count);
        }
    }

    @Override
    public KwlReturnObject checkVQLinkedWithAnotherPI(String vendorQuotationId) throws ServiceException {
        List list = new ArrayList();
      
        String selQuery = "select count(*) from GoodsReceiptLinking grl  where grl.LinkedDocID = ?";
        list = executeQuery( selQuery, new Object[]{vendorQuotationId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getLinkedPaymentDetail(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from LinkDetailPayment";
        return buildNExecuteQuery( query, requestParams);

    }

      /**
     * Description :Method is used to get Purchase Invoice linked in Purchase Return  
     * @param <request> contains Purchase Return ID & company ID
     * @return List
     */
    
    @Override
    public KwlReturnObject getInvoicesLinkedInPurchaseReturn(Map request) throws ServiceException {
       
        String purchseReturnID = (String) request.get("purchaseReturnID");
        String companyid = (String) request.get("companyid");
        String selQuery = "select distinct gr from PurchaseReturnDetail prd inner join prd.videtails invd inner join invd.goodsReceipt gr where prd.purchaseReturn.ID=? and gr.deleted=false and invd.company.companyID=?";

        List list = executeQuery( selQuery, new Object[]{purchseReturnID, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    
        /**
     * Description : Method is used to get Goods Receipt linked in Purchase Return  
     * @param <request> contains Purchase Return ID & company ID
     * @return :List
     */
    
    @Override
    public KwlReturnObject getGoodsReceiptsLinkedInPurchaseReturn(Map request) throws ServiceException {
        
        String purchseReturnID = (String) request.get("purchaseReturnID");
        String companyid = (String) request.get("companyid");
        String selQuery = "select distinct gr from PurchaseReturnDetail prd inner join prd.grdetails grd inner join grd.grOrder gr where prd.purchaseReturn.ID=? and gr.deleted=false and grd.company.companyID=?";

        List list = executeQuery( selQuery, new Object[]{purchseReturnID, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    
         /**
     * Description : Method is used to get Debit Note linked in Purchase Return  
     * @param <request> contains Purchase Return ID & company ID
     * @return :List
     */
    
    @Override
    public KwlReturnObject getDebitNoteLinkedInPurchaseReturn(Map request) throws ServiceException {
        String purchseReturnID = (String) request.get("purchaseReturnID");
        String companyid = (String) request.get("companyid");
        String selQuery = "select distinct dn from DebitNote dn inner join dn.purchaseReturn pr where pr.ID=? and dn.company.companyID=?";

        List list = executeQuery( selQuery, new Object[]{purchseReturnID, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getAutoGRFromInvoice(String invoiceId, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            String selQuery = "";
            selQuery = "select GR.grOrder.ID from GoodsReceiptOrderDetails GR where GR.videtails.goodsReceipt.ID=? and GR.grOrder.deleted=false and GR.company.companyID = ? and GR.grOrder.isAutoGeneratedGRO=true";
            list = executeQuery(selQuery, new Object[]{invoiceId, companyid});
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        } finally {
            return new KwlReturnObject(true, "", null, list, list.size());
        }
    }
    
    /* Method is used to Updating Entry in Credit Note Linking Table If any Credit Note linked with Sales Return*/

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

    /* Method is used to Updating Entry in Make Payment Linking Table If any Credit Note/Purchase Invoice linked with Make Payment*/
    @Override
    public KwlReturnObject savePaymentLinking(HashMap<String, Object> request) throws ServiceException {
        String newitemID = UUID.randomUUID().toString();
        String linkeddocid = (String) request.get("linkeddocid");
        String docid = (String) request.get("docid");
        int moduleid = (Integer) request.get("moduleid");
        int sourceFlag = (Integer) request.get("sourceflag");
        String linkeddocno = (String) request.get("linkeddocno");

        String query = "insert into  paymentlinking(id,docid,linkeddocid,linkeddocno,moduleid,sourceflag) values(" + '"' + newitemID + '"' + ',' + '"' + docid + '"' + ',' + '"' + linkeddocid + '"' + ',' + '"' + linkeddocno + '"' + ',' + '"' + moduleid + '"' + ',' + '"' + sourceFlag + '"' + ")";
        int numRows = executeSQLUpdate(query, new String[]{});
        return new KwlReturnObject(true, "Debit Note Linking has been saved successfully.", null, null, numRows);
    }

    /* Method is used to Updating Entry in Debit Note Linking Table If any Debit Note linked with Purchase Return*/
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

    /* Method is used to Updating Entry in Received Payment Linking Table If any Debit Note linked with Received Payment*/
    @Override
    public KwlReturnObject saveReceiptLinking(HashMap<String, Object> request) throws ServiceException {
        String newitemID = UUID.randomUUID().toString();
        String linkeddocid = (String) request.get("linkeddocid");
        String docid = (String) request.get("docid");
        int moduleid = (Integer) request.get("moduleid");
        int sourceFlag = (Integer) request.get("sourceflag");
        String linkeddocno = (String) request.get("linkeddocno");

        String query = "insert into  receiptlinking(id,docid,linkeddocid,linkeddocno,moduleid,sourceflag) values(" + '"' + newitemID + '"' + ',' + '"' + docid + '"' + ',' + '"' + linkeddocid + '"' + ',' + '"' + linkeddocno + '"' + ',' + '"' + moduleid + '"' + ',' + '"' + sourceFlag + '"' + ")";
        int numRows = executeSQLUpdate(query, new String[]{});
        return new KwlReturnObject(true, "Debit Note Linking has been saved successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject getGoodsReceiptsWithSearchColumn(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        String condition = "";
        ArrayList paramslist = new ArrayList();
        try {
            String companyid = request.get("companyid").toString();
            paramslist.add(companyid);
            if (request.containsKey("startdate") && request.containsKey("enddate")) {
                Date startDate = (Date) request.get("startdate");
                Date endDate = (Date) request.get("enddate");
//                condition = " and (journalentry.entrydate >=? and journalentry.entrydate <=?)";
                condition = " and (goodsreceipt.creationdate >=? and goodsreceipt.creationdate <=?)";
                paramslist.add(startDate);
                paramslist.add(endDate);

            }
            String searchString = request.get("searchstring").toString();
            
            String mysqlQuery = "select goodsreceipt.id from goodsreceipt  "
                    + "inner join journalentry on goodsreceipt.journalentry = journalentry.id  "
                    + " inner join accjecustomdata on accjecustomdata.journalentryId=goodsreceipt.journalentry "
                    + " where goodsreceipt.company = ? and  goodsreceipt.deleteflag='F' " + condition + searchString;
                 
            list = executeSQLQuery(mysqlQuery, paramslist.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getGoodsReceiptsWithSearchColumn:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
        /**
     * Description : Method is used to get Make Payment made against Purchase
     * Invoice
     *
     * @param <request> contains company ID
     * @return :List contains id of payment detail
     */
    @Override
    public KwlReturnObject getLinkedMPWithPI(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = request.get("companyid").toString();

            String query = "select invpayment.ID from PaymentDetail invpayment  inner join invpayment.goodsReceipt gr  where gr.company.companyID=?";

            list = executeQuery(query, new Object[]{companyid});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.getLinkedMPWithPI:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    /**
     * Description : Method is used to get Advance Make Payment made against
     * Purchase Invoice
     *
     * @param <request> contains company ID
     * @return :List contains id of payment detail
     */
    @Override
    public KwlReturnObject getLinkedAdvanceMPWithPI(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = request.get("companyid").toString();

            String query = "select invpayment.ID from LinkDetailPayment invpayment  inner join invpayment.goodsReceipt gr  where gr.company.companyID=?";

            list = executeQuery(query, new Object[]{companyid});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.getLinkedAdvanceMPWithPI:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject deleteBadDebtPurchaseInvoiceMapping(HashMap<String, Object> requestParams) throws ServiceException {
        int count=0;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);

            if (requestParams.containsKey("id") && requestParams.get("id") != null) {
                condition += " and bdm.id=? ";
                params.add((String) requestParams.get("id"));
            }
            
            if (requestParams.containsKey("invoiceid") && requestParams.get("invoiceid") != null) {
                condition += " and bdm.goodsReceipt.ID=? ";
                params.add((String) requestParams.get("invoiceid"));
            }
            
            if (requestParams.containsKey("paymentid") && requestParams.get("paymentid") != null) {
                condition += " and bdm.paymentId=? ";
                params.add((String) requestParams.get("paymentid"));
            }

            if (requestParams.containsKey("badDebtType") && requestParams.get("badDebtType") != null) {
                condition += " and bdm.badDebtType=? ";
                params.add((Integer) requestParams.get("badDebtType"));
            }
            String query = "Delete from BadDebtPurchaseInvoiceMapping bdm where bdm.company.companyID=? " + condition;
            count = executeUpdate(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteBadDebtPurchaseInvoiceMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null,count);
    }
    /**
     *
     * @param request
     * @return
     * @throws ServiceException
     * @Description : Advance Search on Detail Table
     */
    public KwlReturnObject getGoodsReceiptDetailsUsingAdvanceSearch(HashMap<String, Object> request) throws ServiceException {
        ArrayList params = new ArrayList();
        if (request.containsKey("Id")) {
            String id = request.get("Id").toString();
            params.add(id);
        }
        boolean lineLevelAmount = true;
        if (request.containsKey("lineLevelAmount")) {
            lineLevelAmount = Boolean.parseBoolean(request.get("lineLevelAmount").toString());
        }
        boolean isExpense = false;
        String table = "GoodsReceiptDetail";
        if (request.containsKey("isExpense")) {
            isExpense = Boolean.parseBoolean(request.get("isExpense").toString());
            if (isExpense) {
                table = "ExpenseGRDetail";
            }
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
                                mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "grd.goodsReceipt.journalEntry.accBillInvCustomData");//
//                                joinString1 = " inner join GoodsReceipt on GoodsReceipt.ID = GoodsReceiptDetail.goodsReceipt.ID  inner join AccJECustomData on AccJECustomData.journalentryId=goodsreceipt.journalentry ";
//                                joinString1 = " inner join grd.goodsReceipt gr";
                            }
                            if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//
//                                joinString1 += " left join accjedetailcustomdata  on accjedetailcustomdata.jedetailId=jedetail.id ";
                            }
                            if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");//    
//                                joinString1 += " left join accjedetailproductcustomdata  on accjedetailproductcustomdata.jedetailId=jedetail.id ";
                            }
                            StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                        } catch (ParseException ex) {
                            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

        String query = " select DISTINCT  grd from " + table + " grd  inner join grd.purchaseJED  jed where grd.goodsReceipt.ID =? " + mySearchFilterString;
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getPurchaseReturnDetailTermMap(HashMap<String, Object> hm) throws ServiceException{
        List list = new ArrayList();
        List param = new ArrayList();
        try {
            String query = "from PurchaseReturnDetailsTermMap ";
            String condition = "";
            String orderby = " order by term.termSequence ";
            if (hm.containsKey("PurchaseReturnDetailid") && hm.get("PurchaseReturnDetailid") != null) {
                String SODetailid = hm.get("PurchaseReturnDetailid").toString();
                condition += " purchasereturndetail.ID = ? ";
                param.add(SODetailid);
            }
            if (hm.containsKey("productid") && hm.get("productid") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                condition += " product.ID = ? ";
                param.add(hm.get("productid"));
            }
            if (hm.containsKey("termtype") && hm.get("termtype") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                condition += " term.termType = ? ";
                param.add(hm.get("termtype"));
            }
            if (!StringUtil.isNullOrEmpty(condition)) {
                query += " where " + condition;
            }
            if (hm.containsKey("orderbyadditionaltax") && hm.get("orderbyadditionaltax") != null) {
                orderby += " , term.isAdditionalTax ";
            }
            orderby += " ASC ";
            query += orderby;
            list = executeQuery(query, param.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptobj.getPurchaseReturnDetailTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    
    @Override
    public KwlReturnObject savePurchaseReturnDetailsTermMap(HashMap<String, Object> PRDetailsTermsMap) throws ServiceException{
        List list = new ArrayList();
        try {
            PurchaseReturnDetailsTermMap prtermmap = new PurchaseReturnDetailsTermMap();

            if (PRDetailsTermsMap.containsKey("id")) {
                prtermmap = (PurchaseReturnDetailsTermMap) get(PurchaseReturnDetailsTermMap.class, (String) PRDetailsTermsMap.get("id"));
            }
            if (PRDetailsTermsMap.containsKey("termamount") && PRDetailsTermsMap.get("termamount")!=null && !StringUtil.isNullOrEmpty(PRDetailsTermsMap.get("termamount").toString())) {
                prtermmap.setTermamount(Double.parseDouble(PRDetailsTermsMap.get("termamount").toString()));
            }
            if (PRDetailsTermsMap.containsKey("termpercentage") && PRDetailsTermsMap.get("termpercentage")!=null && !StringUtil.isNullOrEmpty(PRDetailsTermsMap.get("termpercentage").toString())) {
                prtermmap.setPercentage(Double.parseDouble(PRDetailsTermsMap.get("termpercentage").toString()));
            }
            if (PRDetailsTermsMap.containsKey("assessablevalue") && PRDetailsTermsMap.get("assessablevalue")!=null && !StringUtil.isNullOrEmpty(PRDetailsTermsMap.get("assessablevalue").toString())) {
                prtermmap.setAssessablevalue(Double.parseDouble(PRDetailsTermsMap.get("assessablevalue").toString()));
            }
            if (PRDetailsTermsMap.containsKey("PurchaseReturnDetailID") && PRDetailsTermsMap.get("PurchaseReturnDetailID")!=null && !StringUtil.isNullOrEmpty(PRDetailsTermsMap.get("PurchaseReturnDetailID").toString())) {
                PurchaseReturnDetail purchasereturndetail = (PurchaseReturnDetail) get(PurchaseReturnDetail.class, (String) PRDetailsTermsMap.get("PurchaseReturnDetailID"));
                prtermmap.setPurchasereturndetail(purchasereturndetail);
            }
            if (PRDetailsTermsMap.containsKey("term") && PRDetailsTermsMap.get("term")!=null && !StringUtil.isNullOrEmpty(PRDetailsTermsMap.get("term").toString())) {
                LineLevelTerms term = (LineLevelTerms) get(LineLevelTerms.class, (String) PRDetailsTermsMap.get("term"));
                prtermmap.setTerm(term);
            }
            if (PRDetailsTermsMap.containsKey("userid") && PRDetailsTermsMap.get("userid")!=null && !StringUtil.isNullOrEmpty(PRDetailsTermsMap.get("userid").toString())) {
                User userid = (User) get(User.class, (String) PRDetailsTermsMap.get("userid"));
                prtermmap.setCreator(userid);
            }
            if (PRDetailsTermsMap.containsKey("createdon") && PRDetailsTermsMap.get("createdon")!=null && !StringUtil.isNullOrEmpty(PRDetailsTermsMap.get("createdon").toString())) {
                prtermmap.setCreatedOn(((Date) PRDetailsTermsMap.get("createdon")));
            }
            if (PRDetailsTermsMap.containsKey("product") && PRDetailsTermsMap.get("product")!=null && !StringUtil.isNullOrEmpty(PRDetailsTermsMap.get("product").toString())) {
                Product product = (Product) get(Product.class, (String) PRDetailsTermsMap.get("product"));
                prtermmap.setProduct(product);
            }
            if (PRDetailsTermsMap.containsKey("purchasevalueorsalevalue") && PRDetailsTermsMap.get("purchasevalueorsalevalue")!=null && !StringUtil.isNullOrEmpty(PRDetailsTermsMap.get("purchasevalueorsalevalue").toString())) {
                prtermmap.setPurchaseValueOrSaleValue(Double.parseDouble(PRDetailsTermsMap.get("purchasevalueorsalevalue").toString()));
            }
            if (PRDetailsTermsMap.containsKey("deductionorabatementpercent") && PRDetailsTermsMap.get("deductionorabatementpercent")!=null && !StringUtil.isNullOrEmpty(PRDetailsTermsMap.get("deductionorabatementpercent").toString())) {
                prtermmap.setDeductionOrAbatementPercent(Double.parseDouble(PRDetailsTermsMap.get("deductionorabatementpercent").toString()));
            }
            if (PRDetailsTermsMap.containsKey("taxtype") && PRDetailsTermsMap.get("taxtype")!=null && !StringUtil.isNullOrEmpty(PRDetailsTermsMap.get("taxtype").toString())) {
                prtermmap.setTaxType(Integer.parseInt(PRDetailsTermsMap.get("taxtype").toString()));
            }
            if (PRDetailsTermsMap.containsKey("isDefault")) {
                prtermmap.setIsGSTApplied(Boolean.parseBoolean(PRDetailsTermsMap.get("isDefault").toString()));
            }
            if (PRDetailsTermsMap.containsKey("productentitytermid")) {
                EntitybasedLineLevelTermRate term = (EntitybasedLineLevelTermRate) get(EntitybasedLineLevelTermRate.class, (String) PRDetailsTermsMap.get("productentitytermid"));
                prtermmap.setEntitybasedLineLevelTermRate(term);
            }
            saveOrUpdate(prtermmap);
            list.add(prtermmap);
            
            } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptobj.savePurchaseReturnDetailsTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Purchase Return Details Term mapped successfully!!", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject saveOrUpdateGRODetailsTermsMap(HashMap<String, Object> requestParams) throws ServiceException {
        int count = 0;
        try {
            ReceiptOrderDetailTermMap grodetailstermmap = new ReceiptOrderDetailTermMap();
            
//             if (requestParams.containsKey("id")) {
//                grodetailstermmap = (ReceiptOrderDetailTermMap) get(ReceiptOrderDetailTermMap.class, (String) requestParams.get("id"));
//            }
            if (requestParams.containsKey("term")) {
                LineLevelTerms term = (LineLevelTerms) get(LineLevelTerms.class, (String) requestParams.get("term"));
                grodetailstermmap.setTerm(term);
            }
            if (requestParams.containsKey("termamount")) {
                grodetailstermmap.setTermamount(Double.parseDouble(requestParams.get("termamount") + ""));
            }
            if (requestParams.containsKey("termpercentage")) {
                grodetailstermmap.setPercentage(Double.parseDouble(requestParams.get("termpercentage") + ""));
            }
            if (requestParams.containsKey("assessablevalue")) {
                grodetailstermmap.setAssessablevalue((Double) requestParams.get("assessablevalue"));
            }
            if (requestParams.containsKey("creationdate")) {
                grodetailstermmap.setCreatedOn((Date) requestParams.get("creationdate"));
            }
            if (requestParams.containsKey("gordetails")) {
                GoodsReceiptOrderDetails grodetails = (GoodsReceiptOrderDetails) get(GoodsReceiptOrderDetails.class, (String) requestParams.get("gordetails"));
                grodetailstermmap.setGrodetail(grodetails);
            }
            if (requestParams.containsKey("userid")) {
                User userid = (User) get(User.class, (String) requestParams.get("userid"));
                grodetailstermmap.setCreator(userid);
            }
            if (requestParams.containsKey("purchasevalueorsalevalue")) {
                grodetailstermmap.setPurchaseValueOrSaleValue(Double.parseDouble(requestParams.get("purchasevalueorsalevalue") + ""));
            }
            if (requestParams.containsKey("deductionorabatementpercent")) {
                grodetailstermmap.setDeductionOrAbatementPercent(Double.parseDouble(requestParams.get("deductionorabatementpercent") + ""));
            }
            if (requestParams.containsKey("taxtype")) {
                grodetailstermmap.setTaxType(Integer.parseInt(requestParams.get("taxtype") + ""));
            }
            if (requestParams.containsKey("isDefault")) {
                grodetailstermmap.setIsGSTApplied(Boolean.parseBoolean(requestParams.get("isDefault").toString()));
            }
             if (requestParams.containsKey("productentitytermid")) {
                EntitybasedLineLevelTermRate term = (EntitybasedLineLevelTermRate) get(EntitybasedLineLevelTermRate.class, (String) requestParams.get("productentitytermid"));
                grodetailstermmap.setEntitybasedLineLevelTermRate(term);
            }
            saveOrUpdate(grodetailstermmap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveOrUpdateGRODetailsTermsMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, count);
    }
    @Override
    public JSONArray getGRODetailsTermMap(String invoicedetailId) throws ServiceException {
        JSONArray jsonArray = new JSONArray();
        try {
            String query = "from ReceiptOrderDetailTermMap where grodetail.ID = ?";
            List<ReceiptOrderDetailTermMap> list = executeQuery(query, new Object[]{invoicedetailId});
            for (ReceiptOrderDetailTermMap obj : list) {
                JSONObject jsonObj=new JSONObject();
                jsonObj.put("id",obj.getId());
                jsonObj.put("termid",obj.getTerm().getId());
                jsonObj.put("term",obj.getTerm().getTerm());
                jsonObj.put("formulaids",obj.getTerm().getFormula());
                jsonObj.put("termamount",obj.getTermamount());
                jsonObj.put("termpercentage",obj.getPercentage());
                jsonObj.put("originalTermPercentage",obj.getTerm().getPercentage());
                String accName=obj.getTerm().getAccount().getName();
                jsonObj.put("glaccountname",accName!=null?(accName.replace("\"", "'")):accName);
                jsonObj.put("IsOtherTermTaxable",obj.getTerm().isOtherTermTaxable());
                jsonObj.put("accountid",obj.getTerm().getAccount().getID());
                jsonObj.put("glaccount",obj.getTerm().getAccount().getID());
                jsonObj.put("purchasevalueorsalevalue",obj.getPurchaseValueOrSaleValue());
                jsonObj.put("deductionorabatementpercent",obj.getDeductionOrAbatementPercent());
                jsonObj.put("assessablevalue",obj.getAssessablevalue());
                jsonObj.put("taxtype",obj.getTaxType());
                jsonObj.put("taxvalue",obj.getPercentage());
                /**
                 * ERP-32829 
                 */
                jsonObj.put("productentitytermid", obj.getEntitybasedLineLevelTermRate().getId());
                jsonObj.put("isDefault", obj.isIsGSTApplied());
                jsonObj.put("sign",obj.getTerm().getSign());
//                jsonObj.put("taxvalue",obj.getTaxType()==0 ? obj.getTermamount() : obj.getPercentage());
                jsonObj.put("termtype",obj.getTerm().getTermType());
                jsonObj.put("formType",obj.getTerm().getFormType());
                jsonObj.put("termsequence",obj.getTerm().getTermSequence());
                jsonObj.put("creditnotavailedaccount", obj.getTerm().getCreditNotAvailedAccount() != null ? obj.getTerm().getCreditNotAvailedAccount().getID() : "");
                jsonObj.put("payableaccountid", obj.getTerm().getPayableAccount() != null ? obj.getTerm().getPayableAccount().getID() : "");
                jsonObj.put(IndiaComplianceConstants.GST_CESS_TYPE, obj.getEntitybasedLineLevelTermRate()!=null && obj.getEntitybasedLineLevelTermRate().getCessType()!=null ? obj.getEntitybasedLineLevelTermRate().getCessType().getId() : "");
                jsonObj.put(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT, obj.getEntitybasedLineLevelTermRate()!=null ? obj.getEntitybasedLineLevelTermRate().getValuationAmount() :0.0);
                jsonObj.put(IndiaComplianceConstants.DEFAULT_TERMID, obj.getTerm()!=null && obj.getTerm().getDefaultTerms()!=null ? obj.getTerm().getDefaultTerms().getId() : "");
                jsonArray.put(jsonObj);
            }


        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jsonArray;
    }
    @Override
    public boolean deleteGRODetailsTermMap(String invoicedetailId) throws ServiceException {
        boolean result = false;
        try {
            String query = "delete from ReceiptOrderDetailTermMap where grodetail.ID = ? ";
            int numrow= executeUpdate(query, new Object[]{invoicedetailId});
//            if(numrow>0){
                result=true;
//            }
        } catch (Exception ex) {
           Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return result;
    }
    @Override
    public boolean deleteGRDetailsTermMap(String invoicedetailId) throws ServiceException {
        boolean result = false;
        try {
            String query = "delete from ReceiptDetailTermsMap where goodsreceiptdetail.ID = ? ";
            int numrow= executeUpdate(query, new Object[]{invoicedetailId});
            /**
             *  Delete GST Fields for India.
             */
            deleteGstTaxClassDetails("'"+invoicedetailId+"'");
            if(numrow>0){
                result=true;
            }
        } catch (Exception ex) {
           Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return result;
    }
    /**
     * Function to delete GST Tax class history for document
     *
     * @param docrefid
     * @throws ServiceException
     */
    public void deleteGstTaxClassDetails(String docrefid) throws ServiceException {
        if (!StringUtil.isNullOrEmpty(docrefid)) {
            String delQuery = " delete from gsttaxclasshistory where refdocid IN (" + docrefid + ")";
            executeSQLUpdate(delQuery);
        }
    }

    /**
     * Function to delete GST Fields history for document
     *
     * @param docrefid
     * @throws ServiceException
     */
    public void deleteGstDocHistoryDetails(String docrefid) throws ServiceException {
        if (!StringUtil.isNullOrEmpty(docrefid)) {
            String delQuery = " delete from gstdocumenthistory where refdocid=?";
            executeSQLUpdate(delQuery, new Object[]{docrefid});
        }
    }
    @Override
    public KwlReturnObject saveExciseDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ExciseDetailsInvoice exciseDetails = new ExciseDetailsInvoice();

            GoodsReceipt gr = null;
            State state = null;
//                exciseDetails.setId(UUID.randomUUID().toString());
            if (dataMap.containsKey("id") && !StringUtil.isNullOrEmpty(dataMap.get("id").toString())){
                exciseDetails = (ExciseDetailsInvoice) get(ExciseDetailsInvoice.class, dataMap.get("id").toString());
            }
            if (dataMap.containsKey("goodreceiptid") && dataMap.get("goodreceiptid")!=null) {
                gr = (GoodsReceipt) get(GoodsReceipt.class, (String) dataMap.get("goodreceiptid"));
                exciseDetails.setGoodsreceipt(gr);
            }
            if (dataMap.containsKey("suppliers")) {
                exciseDetails.setSupplier(dataMap.get("suppliers").toString());
            }
            if (dataMap.containsKey("supplierTINSalesTAXNo")) {
                exciseDetails.setSupplierTINSalesTaxNo(dataMap.get("supplierTINSalesTAXNo").toString());
            }
            if (dataMap.containsKey("supplierExciseRegnNo")) {
                exciseDetails.setSupplierExciseRegnNo(dataMap.get("supplierExciseRegnNo").toString());
            }
            if (dataMap.containsKey("cstnumber")) {
                exciseDetails.setCstnumber(dataMap.get("cstnumber").toString());
            }
            if (dataMap.containsKey("supplierCommissionerate")) {
                exciseDetails.setSupplierCommissioneRate(dataMap.get("supplierCommissionerate").toString());
            }
            if (dataMap.containsKey("supplierAddress")) {
                exciseDetails.setSupplierAddress(dataMap.get("supplierAddress").toString());
            }
            if (dataMap.containsKey("supplierRange")) {
                exciseDetails.setSupplierRange(dataMap.get("supplierRange").toString());
            }
            if (dataMap.containsKey("supplierState")) {
                exciseDetails.setSupplierstate(dataMap.get("supplierState").toString());
            }
            if (dataMap.containsKey("supplierImporterExporterCode")) {
                exciseDetails.setSupplierImporterExporterCode(dataMap.get("supplierImporterExporterCode").toString());
            }
            if (dataMap.containsKey("supplierDivision")) {
                exciseDetails.setSupplierDivision(dataMap.get("supplierDivision").toString());
            }
            if (dataMap.containsKey("manufacturername")) {
                exciseDetails.setManufacturerName(dataMap.get("manufacturername").toString());
            }
            if (dataMap.containsKey("manufacturerExciseRegnNo")) {
                exciseDetails.setManufacturerExciseregnNo(dataMap.get("manufacturerExciseRegnNo").toString());
            }
            if (dataMap.containsKey("manufacturerRange")) {
                exciseDetails.setManufacturerRange(dataMap.get("manufacturerRange").toString());
            }
            if (dataMap.containsKey("manufacturerCommissionerate")) {
                exciseDetails.setManufacturerCommissionerate(dataMap.get("manufacturerCommissionerate").toString());
            }
            if (dataMap.containsKey("manufacturerDivision")) {
                exciseDetails.setManufacturerDivision(dataMap.get("manufacturerDivision").toString());
            }
            if (dataMap.containsKey("manufacturerAddress")) {
                exciseDetails.setManufacturerAddress(dataMap.get("manufacturerAddress").toString());
            }
            if (dataMap.containsKey("manufacturerImporterExporterCode")) {
                exciseDetails.setManufacturerImporterexporterCode(dataMap.get("manufacturerImporterExporterCode").toString());
            }
            if (dataMap.containsKey("registrationType")) {
                exciseDetails.setRegistrationType(dataMap.get("registrationType").toString());
            }
            if (dataMap.containsKey("UnitName")) {
                exciseDetails.setUnitname(dataMap.get("UnitName").toString());
            }
            if (dataMap.containsKey("ECCNo")) {
                exciseDetails.setECCNo(dataMap.get("ECCNo").toString());
            }
            
            saveOrUpdate(exciseDetails);
            list.add(exciseDetails);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.saveGoodsReceiptTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    } 
    @Override
    public KwlReturnObject getExciseDetails(String receiptId) throws ServiceException {
        List list = new ArrayList();
        try {
//            String selQuery = "select supplier,suppliertinsalestaxno,supplierexciseregnno,supplierrange,suppliercommissionerate,"
//                    + "supplieraddress,supplierimporterexportercode,supplierdivision,manufacturername,manufacturerexciseregnno,"
//                    + "manufacturerrange,manufacturercommissionerate,manufacturerdivision,manufactureraddress,manufacturerimporterexportercode,"
//                    + "manufactureinvoiceno,manufactureinvoicedate,supplierstate from exciseInvoicedetails where goodsreceipt=?";
            String selQuery = "from ExciseDetailsInvoice where goodsreceipt.ID=?";
//             String selQuery = "select ed.supplier,ed.supplierTINSalesTaxNo,ed.supplierExciseRegnNo,ed.supplierRange,ed.supplierCommissioneRate,"
//                    + "ed.supplierAddress,ed.supplierImporterExporterCode,ed.supplierDivision,ed.manufacturerName,ed.manufacturerExciseregnNo,"
//                    + "ed.manufacturerRange,ed.manufacturerCommissionerate,ed.manufacturerDivision,ed.manufacturerAddress,ed.manufacturerImporterexporterCode,"
//                    + "ed.invoicenoManufacture,ed.invoiceDateManufacture,ed.supplierstate from ExciseDetailsInvoice as ed where ed.GoodsReceipt.=?";
            list = executeQuery(selQuery, new Object[]{receiptId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getExciseDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getExciseDetailsAssetQuotation(String receiptId) throws ServiceException {
        List list = new ArrayList();
        try {
            String selQuery = "from ExciseDetailsAssets where quotation.ID=?";
            list = executeQuery(selQuery, new Object[]{receiptId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getExciseDetailsAssetQuotation : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getExciseDetailsAssetPurchaseOrder(String receiptId) throws ServiceException {
        List list = new ArrayList();
        try {
            String selQuery = "from ExciseDetailsAssets where purchaseOrder.ID=?";
            list = executeQuery(selQuery, new Object[]{receiptId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getExciseDetailsAssetQuotation : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public List getStoreManagerListByGROrderId(String companyId, String GROrderId) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            if (!StringUtil.isNullOrEmpty(companyId) && !StringUtil.isNullOrEmpty(GROrderId)) {
                String query = "SELECT s.id from grodetails grod INNER JOIN  locationbatchdocumentmapping lbm  ON lbm.documentid=grod.id INNER JOIN newproductbatch npb  ON npb.id=lbm.batchmapid INNER JOIN in_storemaster s  ON s.id=npb.warehouse " +
                               " WHERE  s.company=?   AND grod.company=?  AND npb.company=?  AND grod.grorder=? ";
                params.add(companyId);
                params.add(companyId);
                params.add(companyId);
                params.add(GROrderId);
                list = executeSQLQuery(query, params.toArray());
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getStoreManagerListByGROrderId : " + ex.getMessage(), ex);
        }
        return list;
    }
    
    public List getGoodsRecieptVatDetails(HashMap params) {
        List list = null;
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");    
//        String query = "SELECT acc.`name`,its.account,its.percentage,rdtm.assessablevalue from goodsreceipt gr INNER JOIN grdetails grd ON gr.id = grd.goodsreceipt"
//                + " INNER JOIN receiptdetailtermsmap rdtm"
//                + " ON grd.id = rdtm.goodsreceiptdetail  INNER JOIN invoicetermssales its ON rdtm.term=its.id INNER JOIN account acc ON its.account = acc.id "
//                + "WHERE  gr.company = ? AND its.termtype = 1 ORDER BY its.account ";
        try {
            Date stDate = sdf.parse(params.get("startdate").toString());
            Date endDate = sdf.parse(params.get("enddate").toString());
            
            String query = "SELECT acc.`name`,its.account,rdtm.percentage,rdtm.assessablevalue, rdtm.termamount, its.termtype, rdtm.taxpaidflag from goodsreceipt gr INNER JOIN grdetails grd ON gr.id = grd.goodsreceipt "
                    + "INNER JOIN receiptdetailtermsmap rdtm ON grd.id = rdtm.goodsreceiptdetail "
                    + "INNER JOIN linelevelterms its ON rdtm.term=its.id "
                    + "INNER JOIN account acc ON its.account = acc.id "
                    + "INNER JOIN journalentry je ON gr.journalentry = je.id "
                    + " WHERE  gr.company =? AND je.entrydate >= ? AND je.entrydate <=? AND (its.termtype = 1 OR its.termtype = 3)  "
                    + "ORDER BY its.termtype, its.account";

            list = executeSQLQuery(query, new Object[]{params.get("companyid").toString(), stDate,endDate});

        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
    
    @Override
    public List getPurchaseReturnVatDetails(HashMap params) {
        List list = null;
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");    
        try {
            Date stDate = sdf.parse(params.get("startdate").toString());
            Date endDate = sdf.parse(params.get("enddate").toString());
            
            String query = "SELECT acc.`name`,its.account,prdtm.percentage,-prdtm.assessablevalue, -prdtm.termamount, its.termtype, prdtm.taxpaidflag , gr.id "
                    + "from purchasereturn pr "
                    + "INNER JOIN prdetails prd ON pr.id = prd.purchasereturn "
                    + "INNER JOIN grdetails grd ON grd.id = prd.videtails "
                    + "INNER JOIN goodsreceipt gr ON gr.id = grd.goodsreceipt "
                    + "INNER JOIN purchasereturndetailtermmap prdtm ON prd.id = prdtm.purchasereturndetail "
                    + "INNER JOIN linelevelterms its ON prdtm.term=its.id "
                    + "INNER JOIN account acc ON its.account = acc.id "
                    + " WHERE  pr.company =? AND pr.orderDate >= ? AND pr.orderDate <=? AND (its.termtype = 1 OR its.termtype = 3) AND pr.isNoteAlso = 'T'  "
                    + "ORDER BY its.termtype, its.account";

            list = executeSQLQuery(query, new Object[]{params.get("companyid").toString(), stDate,endDate});

        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
    
    public List getGoodsRecieptIndiaTaxDetails(HashMap params) {
        List list = null;
        DateFormat df = (DateFormat) params.get(Constants.df);        
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
        try {
            Date stDate = df.parse(params.get("startdate").toString());
            Date endDate = df.parse(params.get("enddate").toString());
            
            String innerSubQuery = "";
            int termType = (Integer) params.get("termType");
            
            if(termType == IndiaComplianceConstants.LINELEVELTERMTYPE_Excise_DUTY){ // termtype = 2  is for Excise Duty
                innerSubQuery = " INNER JOIN inventory inv ON grd.id = inv.id "
                        + " INNER JOIN product pd ON inv.product = pd.id "
                        + " INNER JOIN masteritem nosi ON pd.natureofstockitem = nosi.id "
                        + " AND nosi.defaultmasteritem <>'20aacf92-1044-11e6-b10b-14dda9792823' ";
                if(params.containsKey("capitalGoodsProductsFlag")){
                    if(Boolean.parseBoolean(params.get("capitalGoodsProductsFlag").toString())){
                        innerSubQuery = " INNER JOIN inventory inv ON grd.id = inv.id "
                                + " INNER JOIN product pd ON inv.product = pd.id "
                                + " INNER JOIN masteritem nosi ON pd.natureofstockitem = nosi.id "
                                +" AND nosi.defaultmasteritem ='20aacf92-1044-11e6-b10b-14dda9792823' ";
                    }
                }
            }else if(termType == IndiaComplianceConstants.LINELEVELTERMTYPE_SERVICE_TAX){
                innerSubQuery ="";
            }
            
            String subQuery = "";
            int iscenvatadjust=0;
            if(params.containsKey("iscenvatadjust")){
                iscenvatadjust=Integer.parseInt(params.get("iscenvatadjust").toString());
            }
            
            if(termType == IndiaComplianceConstants.LINELEVELTERMTYPE_Excise_DUTY){
                subQuery = " and rdtm.creditavailedflag = ? AND its.termtype = '" + termType +"' ";
            }else if(termType == IndiaComplianceConstants.LINELEVELTERMTYPE_SERVICE_TAX){
                subQuery = " and rdtm.creditavailedflagservicetax = ? AND its.termtype in ('"
                        +IndiaComplianceConstants.LINELEVELTERMTYPE_SERVICE_TAX +"," + IndiaComplianceConstants.LINELEVELTERMTYPE_KKC +"')  ";
            }
            
            if(termType == IndiaComplianceConstants.LINELEVELTERMTYPE_Excise_DUTY && params.containsKey("excisepaidflag")){
                 subQuery = " AND rdtm.taxpaidflag = '"+ params.get("excisepaidflag") +"' ";
            }
            
            String query = "SELECT acc.`name`,its.account,rdtm.percentage,rdtm.assessablevalue, rdtm.termamount, its.termtype,its.account,grd.goodsreceipt from goodsreceipt gr "
                    + " INNER JOIN grdetails grd ON gr.id = grd.goodsreceipt "
                    + " INNER JOIN receiptdetailtermsmap rdtm ON grd.id = rdtm.goodsreceiptdetail "
                    + " INNER JOIN linelevelterms its ON rdtm.term=its.id "
                    + " INNER JOIN account acc ON its.account = acc.id "
                    + " LEFT JOIN journalentry je ON gr.journalentry = je.id "
                    + innerSubQuery
                    + " WHERE gr.company =? AND ((je.entrydate >=? AND je.entrydate <=?) OR (gr.creationdate BETWEEN ? AND ?))"+ subQuery
                    + " ORDER BY its.termtype, its.account";

            list = executeSQLQuery(query, new Object[]{params.get("companyid").toString(), stDate,endDate,stDate,endDate,iscenvatadjust});

        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return list;
    }
    
    public void updateGoodsRecieptTaxPaidFlag(HashMap params){
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");    
        try {
            Date stDate = sdf.parse(params.get("startdate").toString());
            Date endDate = sdf.parse(params.get("enddate").toString());
            int taxType = 1;
            int taxpaidflag = 0;
            String subQuery = "";
            if(params.containsKey("vatpaidflag")){
                taxpaidflag = Integer.parseInt(params.get("vatpaidflag").toString());
                taxType = 1;
            }
            if(params.containsKey("cstpaidflag")){
                taxpaidflag = Integer.parseInt(params.get("cstpaidflag").toString());
                taxType = 3;
            }
            if(params.containsKey("excisepaidflag")){
                taxpaidflag = Integer.parseInt(params.get("excisepaidflag").toString());
                taxType = 2;
                subQuery += " AND rdtm.creditavailedflag=1 ";
            }
            String setQuery = "";
            if(params.containsKey("journalentryid")){
                setQuery += ",rdtm.taxpaymentje='"+ params.get("journalentryid").toString() +"' ";
            }
            if(params.containsKey("paymentid")){
                setQuery += ",rdtm.taxmakepayment='"+ params.get("paymentid").toString() +"' ";
            }
            
            String query = "UPDATE receiptdetailtermsmap rdtm "
                    + " INNER JOIN grdetails grd ON grd.id = rdtm.goodsreceiptdetail "
                    + " INNER JOIN goodsreceipt gr ON gr.id = grd.goodsreceipt "
                    + " INNER JOIN linelevelterms its ON rdtm.term=its.id "
                    + " INNER JOIN journalentry je ON gr.journalentry = je.id "
                    + " SET rdtm.taxpaidflag=? "+ setQuery
                    + " WHERE gr.company = ? AND je.entrydate >= ? AND je.entrydate <=? AND its.termtype = ? " + subQuery;
            executeSQLUpdate(query, new Object[]{taxpaidflag, params.get("companyid"),stDate,endDate, taxType});
            
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    public void resetGoodsRecieptTaxPaidFlag(HashMap params){
        try {
            String whereClauseQuery = "",setQuery = "", termTypeQuery="",conditionQuery="";
            List list = null;
            List queryParams = new ArrayList();
            int termtype = 0;
            //To Check whether this Credit Adjustment is From Excise Computation Report OR ST Computation Report
            termTypeQuery = "select llt.termtype from linelevelterms llt "
                    + "INNER JOIN receiptdetailtermsmap rdtm ON llt.id = rdtm.term "
                    + "INNER JOIN journalentry je ON rdtm.taxpaymentje = je.id ";
            if(params.containsKey("journalentryid")){
                if(!StringUtil.isNullOrEmpty(conditionQuery)){
                    conditionQuery += " and ";
                }
                conditionQuery += " je.id = ?  ";
                queryParams.add(params.get("journalentryid").toString());
            }
            if(!StringUtil.isNullOrEmpty(conditionQuery)){
               termTypeQuery += " where "+ conditionQuery;
            }
            list = executeSQLQuery(termTypeQuery, queryParams.toArray());
            if (!list.isEmpty() && !list.equals("null") && list.size() > 0) {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    termtype = (int)itr.next();
                    break;
                }
            }
            if(termtype == IndiaComplianceConstants.LINELEVELTERMTYPE_Excise_DUTY){
                setQuery += "  , creditavailedflag=0 ";
            }else if(termtype == IndiaComplianceConstants.LINELEVELTERMTYPE_SERVICE_TAX || termtype == IndiaComplianceConstants.LINELEVELTERMTYPE_KKC ){
                setQuery += "  , creditavailedflagservicetax=0 ";
            }else if(termtype == 0){//Work as previous
                setQuery += "  , creditavailedflag=0 ";
            }
            if(params.containsKey("journalentryid")){
                setQuery += " ,taxpaymentje=NULL ";
                whereClauseQuery = " taxpaymentje='"+ params.get("journalentryid").toString() +"' ";
            }
            if(params.containsKey("paymentid")){
                setQuery = ",taxmakepayment=NULL ";
                whereClauseQuery = " taxmakepayment='"+ params.get("paymentid").toString() +"' ";
            }
            
            String query = "UPDATE receiptdetailtermsmap SET taxpaidflag = 0 "+ setQuery
                    + " WHERE "+ whereClauseQuery;
            executeSQLUpdate(query);
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    @Override
    public void updateGoodsRecieptTDSPaidFlag(HashMap reqparams){
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
            if(reqparams.containsKey("paymentid") && reqparams.get("paymentid")!=null && !StringUtil.isNullOrEmpty(reqparams.get("paymentid").toString())){
                if (tdspaidflag == IndiaComplianceConstants.TDSPAYMENT || tdspaidflag == IndiaComplianceConstants.TDSANDTDSINTERESTPAYMENT) {
                    setQuery += " grd.tdspaidflag=1 , grd.tdspayment='" + reqparams.get("paymentid").toString() + "' ";
                }
                if (tdspaidflag == IndiaComplianceConstants.TDSINTERESTPAYMENT || tdspaidflag == IndiaComplianceConstants.TDSANDTDSINTERESTPAYMENT) {
                    if (!StringUtil.isNullOrEmpty(setQuery)) {
                        setQuery += " , grd.tdsinterestpaidflag = 1 , grd.tdsinterestpayment='" + reqparams.get("paymentid").toString() + "' ";
                    } else {
                        setQuery += " grd.tdsinterestpaidflag = 1 , grd.tdsinterestpayment='" + reqparams.get("paymentid").toString() + "' ";
                    }
                    if (reqparams.containsKey("tdsInterestRateAtPaymentTime") && !StringUtil.isNullOrEmpty(reqparams.get("tdsInterestRateAtPaymentTime").toString())) {
                        if (!StringUtil.isNullOrEmpty(setQuery)) {
                            setQuery += " ,tdsInterestRateAtPaymentTime = '" + reqparams.get("tdsInterestRateAtPaymentTime") + "' ";
                        } else {
                            setQuery += " tdsInterestRateAtPaymentTime = '" + reqparams.get("tdsInterestRateAtPaymentTime") + "' ";
                        }
                    }
                }
            }
            conditionQuery += " (grd.tdslineamount > 0 OR (grd.tdslineamount = 0 AND grd.tdsJEMapping IS NOT NULL))";
            if (tdspaidflag == IndiaComplianceConstants.TDSPAYMENT || tdspaidflag == IndiaComplianceConstants.TDSANDTDSINTERESTPAYMENT) {
                if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                    conditionQuery += " and ";
                }
                conditionQuery += " grd.tdspayment IS NULL ";
            }
            if (tdspaidflag == IndiaComplianceConstants.TDSINTERESTPAYMENT || tdspaidflag == IndiaComplianceConstants.TDSANDTDSINTERESTPAYMENT) {
                if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                    conditionQuery += " and ";
                }
                conditionQuery += " grd.tdsinterestpayment IS NULL ";
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
                conditionQuery += " gr.company = ? ";
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
                conditionQuery += " grd.natureofpayment = ? ";
                params.add(reqparams.get("nop"));
            }
            
            String query = "UPDATE grdetails grd "
                    + " INNER JOIN goodsreceipt gr ON grd.goodsreceipt= gr.id   "
                    + " INNER JOIN vendor vn ON gr.vendor = vn.id "
                    + " INNER JOIN journalentry je ON gr.journalentry = je.id ";
            if(!StringUtil.isNullOrEmpty(setQuery)){
                query += " SET " + setQuery ;
            }
            if(!StringUtil.isNullOrEmpty(conditionQuery)){
                query += " WHERE "+conditionQuery;
            }
            executeSQLUpdate(query,params.toArray());
            
            query = "UPDATE expenseggrdetails grd "
                    + " INNER JOIN goodsreceipt gr ON grd.goodsreceipt= gr.id   "
                    + " INNER JOIN vendor vn ON gr.vendor = vn.id "
                    + " INNER JOIN journalentry je ON gr.journalentry = je.id ";
            if(!StringUtil.isNullOrEmpty(setQuery)){
                query += " SET " + setQuery ;
            }
            if(!StringUtil.isNullOrEmpty(conditionQuery)){
                query += " WHERE "+conditionQuery;
            }
            executeSQLUpdate(query,params.toArray());
            
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    @Override
    public void resetGoodsRecieptTDSPaidFlag(HashMap params) {
        try {
            //To Update TDS Paid Flag.
            String whereClauseTDSQuery = "", setTDSQuery = "", TDSquery = "";
            if (params.containsKey("paymentid")) {
                setTDSQuery = " SET tdspaidflag = 0 ,tdspayment=NULL ";
                whereClauseTDSQuery = " tdspayment='" + params.get("paymentid").toString() + "' ";
            }
            if (!StringUtil.isNullOrEmpty(setTDSQuery)) {
                TDSquery = "UPDATE grdetails  " + setTDSQuery + " WHERE " + whereClauseTDSQuery;
                executeSQLUpdate(TDSquery);
            }
            //To Update TDS Interest Paid Flag.
            String whereClauseTDSInterestQuery = "", setTDSInterestQuery = "", TDSInterestquery = "";
            if (params.containsKey("paymentid")) {
                setTDSInterestQuery = " SET tdsinterestpaidflag = 0 ,tdsinterestpayment=NULL ";
                whereClauseTDSInterestQuery = " tdsinterestpayment='" + params.get("paymentid").toString() + "' ";
            }
            if (!StringUtil.isNullOrEmpty(setTDSInterestQuery)) {
                TDSInterestquery = "UPDATE grdetails  " + setTDSInterestQuery + " WHERE " + whereClauseTDSInterestQuery;
                executeSQLUpdate(TDSInterestquery);
            }
            //To Update TDS Paid Flag.
             whereClauseTDSQuery = ""; setTDSQuery = ""; TDSquery = "";
            if (params.containsKey("paymentid")) {
                setTDSQuery = " SET tdspaidflag = 0 ,tdspayment=NULL ";
                whereClauseTDSQuery = " tdspayment='" + params.get("paymentid").toString() + "' ";
            }
            if (!StringUtil.isNullOrEmpty(setTDSQuery)) {
                TDSquery = "UPDATE expenseggrdetails  " + setTDSQuery + " WHERE " + whereClauseTDSQuery;
                executeSQLUpdate(TDSquery);
            }
            //To Update TDS Interest Paid Flag.
             whereClauseTDSInterestQuery = ""; setTDSInterestQuery = ""; TDSInterestquery = "";
            if (params.containsKey("paymentid")) {
                setTDSInterestQuery = " SET tdsinterestpaidflag = 0 ,tdsinterestpayment=NULL ";
                whereClauseTDSInterestQuery = " tdsinterestpayment='" + params.get("paymentid").toString() + "' ";
            }
            if (!StringUtil.isNullOrEmpty(setTDSInterestQuery)) {
                TDSInterestquery = "UPDATE expenseggrdetails  " + setTDSInterestQuery + " WHERE " + whereClauseTDSInterestQuery;
                executeSQLUpdate(TDSInterestquery);
            }
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    @Override
    public void updatePurchaseReturnTaxPaidFlag(HashMap params){
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");    
        try {
            Date stDate = sdf.parse(params.get("startdate").toString());
            Date endDate = sdf.parse(params.get("enddate").toString());
            int taxType = 1;
            int taxpaidflag = 0;
            String subQuery = "";
            if(params.containsKey("vatpaidflag")){
                taxpaidflag = Integer.parseInt(params.get("vatpaidflag").toString());
                taxType = 1;
            }
            if(params.containsKey("cstpaidflag")){
                taxpaidflag = Integer.parseInt(params.get("cstpaidflag").toString());
                taxType = 3;
            }
            String setQuery = "";
            if(params.containsKey("journalentryid")){
                setQuery += ",prdtm.taxpaymentje='"+ params.get("journalentryid").toString() +"' ";
            }
            if(params.containsKey("paymentid")){
                setQuery += ",prdtm.taxmakepayment='"+ params.get("paymentid").toString() +"' ";
            }
            
            String query = "UPDATE purchasereturndetailtermmap prdtm "
                    + " INNER JOIN prdetails prd ON prd.id = prdtm.purchasereturndetail "
                    + " INNER JOIN purchasereturn pr ON pr.id = prd.purchasereturn "
                    + " INNER JOIN linelevelterms its ON prdtm.term=its.id "
                    + " SET prdtm.taxpaidflag=? "+ setQuery
                    + " WHERE pr.company = ? AND pr.orderdate >= ? AND pr.orderdate <=? AND its.termtype = ? " + subQuery;
            executeSQLUpdate(query, new Object[]{taxpaidflag, params.get("companyid"),stDate,endDate, taxType});
            
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
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
    public void updateAdvancePaymentTDSUsedFlag(HashMap params) {
        try {
            List queryParams = new ArrayList();
            String setQuery = "",subQuery ="",AdvancePaymentIDs = "",GoodsReceiptID="",companyid="";
            Boolean isUsed = false;
            if (params.containsKey("isUsed")) {
                isUsed = (Boolean) params.get("isUsed");
            }
            if (isUsed) {
                setQuery += " istdsamountusedingoodsreceipt = 'T' ";
            } else {
                setQuery += " istdsamountusedingoodsreceipt = 'F' ";
            }
            if (params.containsKey("companyid")) {
                companyid = (String) params.get("companyid");
            }
            if (params.containsKey("AdvancePaymentIDs")) {
                AdvancePaymentIDs =  (String) params.get("AdvancePaymentIDs");
                subQuery += " and payment in ( "+ AdvancePaymentIDs +" ) " ;
            }else if(params.containsKey("goodsreceiptid")){
                GoodsReceiptID = (String) params.get("goodsreceiptid");
                subQuery += " and payment in ( select paymentid from goodsreceiptpaymentmapping where goodsreceiptid = '"+GoodsReceiptID+"')";
            }
            String query = " UPDATE advancedetail SET " + setQuery + " where company = '"+ companyid +"' " + subQuery;
            executeSQLUpdate(query);
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    /**
     * Description : Below Method is used to Delete GoodsReceiptPaymentMapping rows against selected Goods Receipt
     * @param GoodsreceiptID
     * @return :boolean(is Successfully Deleted)
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public boolean deleteGoodsReceiptPaymentMapping(String GoodsreceiptID) throws ServiceException {
        boolean result = false;
        try {
            String query = "delete from GoodsReceiptPaymentMapping where goodsreceiptid.ID = ? ";
            int numrow= executeUpdate(query, new Object[]{GoodsreceiptID});
            if(numrow>0){
                result=true;
            }
        } catch (Exception ex) {
           Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return result;
    }
    /**
     * Description : Below Method is used to get GoodsReceiptPaymentMapping rows against selected Goods Receipt
     * @param requestParams
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public KwlReturnObject getAdvancePaymentDetailsUsedInGoodsReceipt(Map<String, Object> requestParams) throws ServiceException {
        String selQuery = "from AdvanceDetail advd ";
        List params = new ArrayList();
        if(requestParams.containsKey("goodsreceiptid")){
            selQuery += " WHERE advd.payment.ID in (select grpm.paymentid from GoodsReceiptPaymentMapping grpm where grpm.goodsreceiptid.ID = ? ) ";
            params.add((String)requestParams.get("goodsreceiptid"));
        }
        List list = executeQuery(selQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public void resetPurchaseReturnTaxPaidFlag(HashMap params){
        try {
            String whereClauseQuery = "",setQuery = "", termTypeQuery="",conditionQuery="";
            List list = null;
            List queryParams = new ArrayList();
            int termtype = 0;
            //To Check whether this Credit Adjustment is From Excise Computation Report OR ST Computation Report
            termTypeQuery = "select llt.termtype from linelevelterms llt "
                    + "INNER JOIN purchasereturndetailtermmap rdtm ON llt.id = rdtm.term "
                    + "INNER JOIN journalentry je ON rdtm.taxpaymentje = je.id ";
            if(params.containsKey("journalentryid")){
                if(!StringUtil.isNullOrEmpty(conditionQuery)){
                    conditionQuery += " and ";
                }
                conditionQuery += " je.id = ?  ";
                queryParams.add(params.get("journalentryid").toString());
            }
            if(!StringUtil.isNullOrEmpty(conditionQuery)){
               termTypeQuery += " where "+ conditionQuery;
            }
            list = executeSQLQuery(termTypeQuery, queryParams.toArray());
            if (!list.isEmpty() && !list.equals("null") && list.size() > 0) {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    termtype = (int)itr.next();
                    break;
                }
            }
            
            if(params.containsKey("journalentryid")){
                setQuery += " ,taxpaymentje=NULL ";
                whereClauseQuery = " taxpaymentje='"+ params.get("journalentryid").toString() +"' ";
            }
            if(params.containsKey("paymentid")){
                setQuery = ",taxmakepayment=NULL ";
                whereClauseQuery = " taxmakepayment='"+ params.get("paymentid").toString() +"' ";
            }
            
            String query = "UPDATE purchasereturndetailtermmap SET taxpaidflag = 0 "+ setQuery
                    + " WHERE "+ whereClauseQuery;
            executeSQLUpdate(query);
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    @Override
    public KwlReturnObject getTaxPaymentFromGoodsReciept(String greceiptid, String companyid) throws ServiceException {
        String selQuery = "from ReceiptDetailTermsMap rdtm  where rdtm.taxPaidFlag=1 and rdtm.goodsreceiptdetail.goodsReceipt.ID=? and rdtm.goodsreceiptdetail.goodsReceipt.company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{greceiptid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    /**
     *
     * @param grOrder
     * @return
     */
    @Override
    public KwlReturnObject updateGoodsReceiptOrderSetNull(GoodsReceiptOrder grOrder) {
        List list = new ArrayList();
        try {
            String doId = grOrder.getID();
            String companyId = grOrder.getCompany().getCompanyID();
            String query = "update grorder set inventoryje=NULL where id = ? and company = ?";
            executeSQLUpdate(query, new Object[]{doId, companyId});
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
}
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    /**
     *
     * @param purchaseReturn
     * @return
     */
    @Override
    public KwlReturnObject updatePurchaseReturnSetNull(PurchaseReturn purchaseReturn) {
        List list = new ArrayList();
        try {
            String purchaseReturnID = purchaseReturn.getID();
            String companyId = purchaseReturn.getCompany().getCompanyID();
            String query = "update purchasereturn set inventoryje=NULL where id = ? and company = ?";
            executeSQLUpdate(query, new Object[]{purchaseReturnID, companyId});
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public boolean updateGoodsReceiptExciseDuty(GoodsReceipt grId)throws ServiceException{
        boolean issuccess = true;
        try {
            saveOrUpdate(grId);
        } catch (ServiceException ex) {
            issuccess = false;
            throw ServiceException.FAILURE("accInvoiceImpl.updateInvoiceExciseDuty:" + ex.getMessage(), ex);
        } catch (Exception ex) {
            issuccess = false;
            throw ServiceException.FAILURE("accInvoiceImpl.updateInvoiceExciseDuty:" + ex.getMessage(), ex);
        }
        return issuccess;
    }
    
    public KwlReturnObject getGoodsReceiptFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from GoodsReceipt where journalEntry.ID=? and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    /**
     * This method is used to get purchase order information using goods receipt order details and companyid.
     *
     * @param grodetailId GoodsReceiptOrderDetail ID
     * @param companyID CompanyID
     * @return List of purchase order information.
     */
    @Override
    public KwlReturnObject getPurchaseOrderInfoUsingGROD(String grodetailId, String companyID) {
        List list = new ArrayList();
        try {
            String sqlQuery = "select po.ponumber,po.orderdate,po.vendor from purchaseorder po "
                    + "inner join podetails pod on pod.purchaseorder = po.id "
                    + "inner join grodetails grod on grod.podetails = pod.id "
                    + "inner join grorder gro on gro.id = grod.grorder "
                    + "where grod.company=? and grod.id=? "
                    + "union "
                    + "select po.ponumber,po.orderdate,po.vendor from purchaseorder po "
                    + "inner join podetails pod on pod.purchaseorder = po.id "
                    + "inner join grdetails grd on grd.purchaseorderdetail = pod.id "
                    + "inner join goodsreceipt gr on gr.id = grd.goodsreceipt "
                    + "inner join grodetails grod on grod.videtails = grd.id "
                    + "inner join grorder gro on gro.id = grod.grorder "
                    + "where grod.company=? and grod.id=? ";
            List params = new ArrayList();
            params.add(companyID);
            params.add(grodetailId);
            params.add(companyID);
            params.add(grodetailId);
            list = executeSQLQuery(sqlQuery, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public String getDeducteeTypeForTDSChallanControlReport(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String deducteeType = "";
        try {
            String companyid = "";
            String account = "";
            String paymentdetail = "";
            String documenttype = "";
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                companyid = requestParams.get("companyid").toString();
            }
            if (requestParams.containsKey("account") && requestParams.get("account") != null && !StringUtil.isNullOrEmpty(requestParams.get("account").toString())) {
                account = requestParams.get("account").toString();
            }
            ArrayList params = new ArrayList();
            params.add(account);

            String query = "SELECT td.paymentdetail,td.documenttype from tdsdetails td "
                    + "INNER JOIN paymentdetailotherwise pdo on pdo.account=td.tdspayableaccount WHERE pdo.account=?";
            List list1 = executeNativeQuery(query, params.toArray());
            if (!list1.isEmpty() && !list1.equals("null") && list1.size() > 0) {
                Iterator itr = list1.iterator();
                while (itr.hasNext()) {
                    Object[] dataObject = (Object[]) itr.next();
                    paymentdetail = (String) (dataObject[0] != null ? dataObject[0] : "");
                    documenttype = (String) (dataObject[1] != null ? dataObject[1] : "");
                    break;
                }
            }
            List list2 = new ArrayList();
            if (documenttype.equals("2") && !StringUtil.isNullOrEmpty(paymentdetail) && !paymentdetail.equals("undefine")) { // Invoice
                ArrayList ps = new ArrayList();
                ps.add(paymentdetail);
                String queryDtype = "SELECT code from masteritem  mi "
                        + "INNER JOIN vendor vd ON vd.deducteetype=mi.id "
                        + "INNER JOIN goodsreceipt gr on gr.vendor=vd.id "
                        + "INNER JOIN paymentdetail pd ON pd.goodsReceipt=gr.id "
                        + "WHERE mi.mastergroup=34 AND pd.id=?";
                list2 = executeSQLQuery(queryDtype, ps.toArray());

            } else if (documenttype.equals("1") && !StringUtil.isNullOrEmpty(account)) { //Advance Payment
                String queryDtype = "SELECT code from masteritem  mi "
                        + "INNER JOIN vendor vd ON vd.deducteetype=mi.id "
                        + "INNER JOIN payment ON payment.vendor=vd.id "
                        + "INNER JOIN advancedetail ON advancedetail.payment=payment.id "
                        + "INNER JOIN tdsdetails on advancedetail.id=tdsdetails.advancedetail "
                        + "WHERE tdsdetails.tdspayableaccount=?";
                list2 = executeSQLQuery(queryDtype, params.toArray());
            } else if (documenttype.equals("3") && !StringUtil.isNullOrEmpty(account)) { //Credite note Payment
                String queryDtype = "SELECT code from masteritem  mi "
                        + "INNER JOIN vendor vd ON vd.deducteetype=mi.id "
                        + "INNER JOIN payment ON payment.vendor=vd.id "
                        + "INNER JOIN creditnotpayment ON creditnotpayment.paymentid=payment.id "
                        + "INNER JOIN tdsdetails on creditnotpayment.id=tdsdetails.creditnotepaymentdetail "
                        + "WHERE tdsdetails.tdspayableaccount=?";
                list2 = executeSQLQuery(queryDtype, params.toArray());
            }
            if (!list2.isEmpty() && list2.size() > 0) {
                String type = "";
                deducteeType = "";
                type = (String) list2.get(0);
                if (!StringUtil.isNullOrEmpty(type) && type.equals("0")) {
                    deducteeType = "Corporate";
                } else if (!StringUtil.isNullOrEmpty(type) && type.equals("1")) {
                    deducteeType = "Non Corporate";
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return deducteeType;
    }

    @Override
    public KwlReturnObject getDataTDSChallanControlReport(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            DateFormat df = null;
            String companyid = "";
            String groupby = "";

            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                companyid = requestParams.get("companyid").toString();
            }
            if (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null && !StringUtil.isNullOrEmpty(requestParams.get(Constants.df).toString())) {
                df = (DateFormat) requestParams.get(Constants.df);
            }

            ArrayList params = new ArrayList();
            String purchaseInvoice = "SELECT je.entrydate,sum(payother.amount) ,ac.name, ac.bsrcode,pay.cinno,payother.account,pay.id "
                    + "FROM paymentdetailotherwise payother "
                    + "INNER JOIN payment pay  ON payother.payment=pay.id "
                    + "INNER JOIN journalentry je ON pay.journalentry=je.id "
                    + "LEFT JOIN paydetail payd ON pay.paydetail = payd.id "
                    + "LEFT JOIN paymentmethod paym ON payd.paymentMethod = paym.id "
                    + "LEFT JOIN account ac ON ac.id=paym.account "
                    + "WHERE (payother.account IN (SELECT DISTINCT (accid) FROM masteritem WHERE accid <> \"\" AND masterGroup='33' AND company=pay.company) OR  payother.account IN (SELECT DISTINCT (tdsinterestpayableaccount) FROM vendor WHERE tdsinterestpayableaccount <> \"\"))";
            if (!StringUtil.isNullOrEmpty(companyid)) {
                purchaseInvoice += " and pay.company= ? ";
                params.add(companyid);
            }

            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null && !StringUtil.isNullOrEmpty(requestParams.get(Constants.REQ_startdate).toString())) {
                startDate = (String) requestParams.get(Constants.REQ_startdate);
            }
            if (requestParams.containsKey(Constants.REQ_enddate) && requestParams.get(Constants.REQ_enddate) != null && !StringUtil.isNullOrEmpty(requestParams.get(Constants.REQ_enddate).toString())) {
                endDate = (String) requestParams.get(Constants.REQ_enddate);
            }

            String conditionQuery = "";
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate) && df != null) {
                conditionQuery += " and (je.entrydate >=? and je.entrydate <=?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            purchaseInvoice += conditionQuery;
            purchaseInvoice += " group by pay.id";

            list = executeSQLQuery(purchaseInvoice, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    @Override 
    public KwlReturnObject getDataSTInputCreditSummaryForReport(HashMap<String, Object> requestParams) throws ServiceException {
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
            String isCashCheck = "";
            if (requestParams.containsKey("basisOfCalculation") && requestParams.get("basisOfCalculation") != null && !StringUtil.isNullOrEmpty(requestParams.get("basisOfCalculation").toString())) {
                if (StringUtil.equalIgnoreCase(requestParams.get("basisOfCalculation").toString(), "2")) {
                    isCashCheck = " AND gr.id IN(SELECT id FROM goodsreceipt  WHERE id NOT IN (SELECT goodsReceipt as id FROM paymentdetail))";
                } else if (StringUtil.equalIgnoreCase(requestParams.get("basisOfCalculation").toString(), "1")) {
                    isCashCheck = " AND gr.id IN(SELECT goodsReceipt as id FROM paymentdetail WHERE id NOT IN(SELECT id FROM goodsreceipt))";
                }
            }
            if (requestParams.containsKey("natureOfTransaction") && requestParams.get("natureOfTransaction") != null && !StringUtil.isNullOrEmpty(requestParams.get("natureOfTransaction").toString())) {
                if (StringUtil.equalIgnoreCase(requestParams.get("natureOfTransaction").toString(), "1")) { // ERP-26059
                    isCashCheck+= " AND vend.iecno IS NOT NULL AND vend.iecno<>''"; 
                }
                }
            ArrayList params = new ArrayList();
            
            
            String purchaseInvoice = "select 1 as transType, je.entrydate as date, gr.grnumber as refno, vend.name as partyname, 'Taxable Service' as category, gr.invoiceamountinbase as totalamount, sum(rdtm.termamount/je.externalcurrencyrate) as totaltax,0.0 as paidamount, 0.0 as inputcredit, 0.0 as balancecredit "
                    + " from goodsreceipt gr "
                    + " left join vendor vend on vend.id = gr.vendor  "
                    + " left join journalentry je on je.id = gr.journalentry  "
                    + " left join grdetails grd on grd.goodsreceipt = gr.id"
                    + " left join receiptdetailtermsmap rdtm on rdtm.goodsreceiptdetail = grd.id"
                    + " left join linelevelterms llt on rdtm.term = llt.id"
                    + " where gr.deleteflag = 'F' and ( llt.termtype = 4 or llt.termtype = 5 or llt.termtype = 6 ) "+isCashCheck ;
            if(!StringUtil.isNullOrEmpty(companyid)){
                purchaseInvoice += " and gr.company = ? ";
                params.add(companyid);
            }
            
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if(requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate)!=null && !StringUtil.isNullOrEmpty(requestParams.get(Constants.REQ_startdate).toString())){
                 startDate = (String) requestParams.get(Constants.REQ_startdate);
            }
            if(requestParams.containsKey(Constants.REQ_enddate) && requestParams.get(Constants.REQ_enddate)!=null && !StringUtil.isNullOrEmpty(requestParams.get(Constants.REQ_enddate).toString())){
                 endDate = (String) requestParams.get(Constants.REQ_enddate);
            }
            
            String conditionQuery = "";
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate) && df !=null) {
                conditionQuery += " and (je.entrydate >=? and je.entrydate <=?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            purchaseInvoice +=conditionQuery;
            
            groupby += " group by gr.grnumber ";
            String orderQuery = " order by date ASC ";
            purchaseInvoice += groupby+orderQuery;
            list = executeSQLQuery(purchaseInvoice,params.toArray());
            count = list.size();
            
        } catch ( ParseException | ServiceException | NumberFormatException ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject saveUpdateDealerExciseDetails(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        try {
            DealerExciseDetails ded = new DealerExciseDetails();
//            if (json.has("id")) {
//                if (!StringUtil.isNullOrEmpty(json.getString("id"))) {
//                    ded = (DealerExciseDetails)get(DealerExciseDetails.class,json.getString("id"));
//                }
//            }
            if (json.has("SupplierRG23DEntry")) {
                ded.setSupplierRG23DEntry(json.getString("SupplierRG23DEntry"));
            }
            if (json.has("RG23DEntryNumber")) {
//                ded.setRG23DEntryNumber(json.getString("autoSeqNumber"));// From nextAutonumberGenerator            
                ded.setRG23DEntryNumber(json.getString("RG23DEntryNumber")); // From UI           
            }
            if (json.has("AssessableValue")) {
                ded.setAssessableValue(json.getString("AssessableValue"));
            }
            if (json.has("PLARG23DEntry")) {
                ded.setPLARG23DEntry(json.getString("PLARG23DEntry"));
            }
            if (json.has("ManuAssessableValue")) {
                ded.setManuAssessableValue(json.getString("ManuAssessableValue"));
            }
            if (json.has("ManuAssessableValue")) {
                ded.setManuAssessableValue(json.getString("ManuAssessableValue"));
            }
            if (json.has("datePreffixValue")) {
                ded.setDatePreffixValue(json.getString("datePreffixValue"));
            }
            if (json.has("dateAfterPreffixValue")) {
                ded.setDateAfterPreffixValue(json.getString("dateAfterPreffixValue"));
            }
            if (json.has("dateSuffixValue")) {
                ded.setDateSuffixValue(json.getString("dateSuffixValue"));
            }
            if (json.has("seqnumber")) {
                ded.setSeqnumber(Integer.parseInt(json.getString("seqnumber")));
            }
            if (json.has("ManuInvoiceNumber")) {
                ded.setInvoicenoManufacture(json.get("ManuInvoiceNumber").toString());
            }
            if (json.has("ManuInvoiceDate") && !StringUtil.isNullOrEmpty(json.getString("ManuInvoiceDate"))) {
                ded.setInvoiceDateManufacture((Date)json.get("ManuInvoiceDate"));
            }
            if (true) {
                ded.setAutoGenerated(true);
            }
            if (json.has("company")) {
                Company companny = new Company();
                companny=(Company) get(Company.class, json.getString("company"));
                ded.setCompany(companny);
            }
            if (json.has("goodsreceiptdetails")) {
                 GoodsReceiptDetail goodsReceiptDetail = (GoodsReceiptDetail)get(GoodsReceiptDetail.class, json.getString("goodsreceiptdetails"));
                ded.setGoodsreceiptdetails(goodsReceiptDetail);
            }
            if (json.has("sequenceformat")) {
                SequenceFormat sequenceformat = (SequenceFormat)get(SequenceFormat.class, json.getString("sequenceformat"));
                ded.setSeqformat(sequenceformat);
            }

            saveOrUpdate(ded);
            list.add(ded);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getDealerExciseDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String selQuery = "from DealerExciseDetails ded  where ded.goodsreceiptdetails.ID=? and ded.company.companyID=?";
        List list = executeQuery(selQuery, new Object[]{requestParams.get("GoodsReceiptDetailid"),requestParams.get("companyid")});
        return new KwlReturnObject(true, "", null, list, list.size());
    }   
    
    @Override
    public KwlReturnObject getGoodsReceiptsForDealerExciseDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String whereClouse="";
        if(requestParams.containsKey("natureofpurchase") && requestParams.get("natureofpurchase")!=null){
        if(!StringUtil.isNullOrEmpty(requestParams.get("natureofpurchase").toString())){
          whereClouse=" AND grd.goodsReceipt.defaultnatureOfPurchase='"+requestParams.get("natureofpurchase")+"'"; 
        }
        }
        String selQuery = "select distinct grd.ID,grd.goodsReceipt.goodsReceiptNumber,grd.inventory.baseuomquantity,grd.goodsReceipt.creationDate,grd.goodsReceipt.vendor.id,grd.goodsReceipt.vendor.name,grd.goodsReceipt.defaultnatureOfPurchase,grd.inventory.quantity,grd.inventory.baseuomrate "
                + "from GoodsReceiptDetail grd "
//                + "inner join DealerExciseDetails grdded "
                + " where grd.inventory.product.ID=? and grd.goodsReceipt.deleted=false and grd.company.companyID=? and grd.goodsReceipt.isExciseInvoice in('1','T') "+whereClouse;
        List list = executeQuery(selQuery, new Object[]{requestParams.get("productid"),requestParams.get("companyid")});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getutilizedGoodsReceiptsForDealerExciseDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String whereClouse = "";
        if (requestParams.containsKey("filterdata") && requestParams.get("filterdata") != null && !StringUtil.isNullOrEmpty(requestParams.get("filterdata").toString())) {
            whereClouse = " AND sed.invoicedetails.id!='" + requestParams.get("invoice") + "'";
        }
        String selQuery = "select sed.utilizedQuantity,sed.goodsReceiptDetailsId from SupplierExciseDetails sed where sed.goodsReceiptDetailsId=? and sed.company.companyID=? "+whereClouse;
        List list = executeQuery(selQuery, new Object[]{requestParams.get("goodsreceiptdetails"),requestParams.get("companyid")});
        return new KwlReturnObject(true, "", null, list, list.size());
    }  

    public KwlReturnObject getGRfromPI(String pi) throws ServiceException {
        ArrayList params = new ArrayList();
        String grId = "";
        params.add(pi);
        params.add(pi);
        String query = "select distinct docid from ( select distinct linkeddocid as docid from goodsreceiptlinking "
                + "where moduleid = 28 and docid = ? union "
                + "select distinct docid as docid from goodsreceiptorderlinking where moduleid = 6 and linkeddocid = ? ) as rgtable";
        List list = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
        @Override
    public KwlReturnObject saveUpdateDealerExciseTermDetails(JSONObject json, DealerExciseDetails ded) throws ServiceException {
        List list = new ArrayList();
        try {
            DealerExciseTerms det = new DealerExciseTerms();
            if (json.has("termpercentage") && json.get("termpercentage") != null && !StringUtil.isNullOrEmpty(json.getString("termpercentage"))) {
                det.setPercentage(Double.parseDouble(json.getString("termpercentage")));
            }
            if (json.has("termamount") && json.get("termamount") != null && !StringUtil.isNullOrEmpty(json.getString("termamount"))) {
                det.setDutyAmount(Double.parseDouble(json.getString("termamount")));
            }
            if (json.has("manufactureTermAmount") && json.get("manufactureTermAmount") != null && !StringUtil.isNullOrEmpty(json.getString("manufactureTermAmount"))) {
                det.setManuImpDutyAmount(Double.parseDouble(json.getString("manufactureTermAmount")));
            }
            if (json.has("company")) {
                Company companny = new Company();
                companny = (Company) get(Company.class, json.getString("company"));
                det.setCompany(companny);
            }
            if (json.has("termid")) {
                LineLevelTerms lineLevelTerm = (LineLevelTerms) get(LineLevelTerms.class, json.getString("termid"));
                det.setLineLevelTerm(lineLevelTerm);
            }
            if (ded != null) {
                det.setDealerExciseDetails(ded);
            }
            saveOrUpdate(det);
            list.add(det);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    @Override
    public KwlReturnObject getDealerExciseTermDetails(HashMap<String, Object> requestParams) throws ServiceException {
//        String selQuery = "Select ded.lineLevelTerm.percentage,ded.lineLevelTerm.term from DealerExciseTerms ded  where ded.dealerExciseDetails.id=? and ded.company.companyID=?";
        String subQuery=(requestParams.containsKey("rule11Dealer") && Boolean.parseBoolean(requestParams.get("rule11Dealer").toString())==true)?" and ded.lineLevelTerm.isAdditionalTax=false":"";
        String selQuery = "from DealerExciseTerms ded  where ded.dealerExciseDetails.id=? and ded.company.companyID=?"+subQuery;
        List list = executeQuery(selQuery, new Object[]{requestParams.get("dealerExciseDetailid"),requestParams.get("companyid")});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getSupplierExciseDetailsMapping(String invoiceid, String companyid) throws ServiceException {
        String selQuery = "from SupplierExciseDetails sei  where sei.goodsReceiptDetailsId=? and sei.company.companyID=?";
        List list = executeQuery(selQuery, new Object[]{invoiceid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
     @Override
     public boolean isTaxApplied(HashMap<String,Object> data, int taxtype) throws ServiceException{
        ArrayList paramslist = new ArrayList();
        List list = new ArrayList();   
        String conditionQuery = "";
        boolean isTaxApplied = false;
        try {            
            String selQuery = "from ReceiptDetailTermsMap rdtm WHERE rdtm.term.termType = ? ";  
            paramslist.add(taxtype);
            if(data.containsKey("invoiceid") && data.get("invoiceid") != null && !StringUtil.isNullOrEmpty(data.get("invoiceid").toString())){
                conditionQuery += " and rdtm.goodsreceiptdetail.goodsReceipt.ID = ? " ;
                paramslist.add(data.get("invoiceid"));
            }
            if(data.containsKey("invoicedetailid") && data.get("invoicedetailid") != null && !StringUtil.isNullOrEmpty(data.get("invoicedetailid").toString())){
                conditionQuery += " and rdtm.goodsreceiptdetail.ID = ? " ;
                paramslist.add(data.get("invoicedetailid"));
            }
            if(data.containsKey("companyid") && data.get("companyid") != null && !StringUtil.isNullOrEmpty(data.get("companyid").toString())){
                conditionQuery += " and rdtm.term.company.companyID =? " ;
                paramslist.add(data.get("companyid"));
            }
            selQuery += conditionQuery;
            list = executeQuery(selQuery, paramslist.toArray());
            if(list.size() > 0){
                isTaxApplied = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return isTaxApplied;
    }
     
     @Override
    public KwlReturnObject getGenricPurchaseReturndetailTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        List param = new ArrayList();
        try {
            String query = "SELECT -sum(prdt.assessablevalue),-sum(prdt.termamount) , t.account,t.termType FROM PurchaseReturnDetailsTermMap prdt JOIN prdt.term t JOIN prdt.purchasereturndetail prds JOIN prds.purchaseReturn pr";
            String condition = "";
            String orderby = " GROUP BY t.account ";
            if (hm.containsKey("PurchasreReturnDetailid") && hm.get("PurchasreReturnDetailid") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                String PurchasreReturnDetailid = hm.get("PurchasreReturnDetailid").toString();
                condition += " prds.ID = ? ";
                param.add(PurchasreReturnDetailid);
            }
            if (hm.containsKey("PurchaseReturnid") && hm.get("PurchaseReturnid") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                String PurchaseReturnid = hm.get("PurchaseReturnid").toString();
                condition += " pr.ID = ? ";
                param.add(PurchaseReturnid);
            }
            
            if (hm.containsKey("termtypeArry") && hm.get("termtypeArry") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }

                List listTerm = (List) hm.get("termtypeArry");
                StringBuilder commaSepValueBuilder = new StringBuilder();

                //Looping through the list
                for (int i = 0; i < listTerm.size(); i++) {
                    //append the value into the builder
                    commaSepValueBuilder.append(listTerm.get(i));

                    //if the value is not the last element of the list
                    //then append the comma(,) as well
                    if (i != listTerm.size() - 1) {
                        commaSepValueBuilder.append(", ");
                    }
                }

                condition += " t.termType in ( " + commaSepValueBuilder + " ) ";
            }
            if (hm.containsKey("termtype") && hm.get("termtype") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                param.add(Integer.parseInt(hm.get("termtype").toString()));
                condition += " t.termType = ? ";
            }
            if (hm.containsKey("termpercentage") && hm.get("termpercentage")!=null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                condition+="t.percentage = ? ";
                param.add(Double.parseDouble(hm.get("termpercentage").toString()));
            }
            if (!StringUtil.isNullOrEmpty(condition)) {
                query += " where " + condition;
            }

            query += orderby;
            list = executeQuery(query, param.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.getGenricPurchaseReturndetailTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     

    @Override
    public KwlReturnObject getLandedInviceList(String GoodsReceiptId, String landingCostCategory) throws ServiceException {
        boolean result = false;
        List list = new ArrayList();
        try {
            String query = "select gr.id from goodsreceipt gr INNER JOIN goodsreceiptid_landedInvoice tgl on gr.id = tgl.goodsreceiptid WHERE tgl.landedinvoice = ? and gr.landingCostCategory= ? ";
            list = executeSQLQuery(query, new Object[]{GoodsReceiptId, landingCostCategory});
            if (list.size() > 0) {
                result = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new KwlReturnObject(result, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getNumberEligiableItem(String GoodsReceiptId ,String landingCostCategory) throws ServiceException {
        boolean result = false;
         List list = new ArrayList();
        try {
            String query = "SELECT SUM(i.quantity),grd.rate,pd.productweight,grd.goodsreceipt FROM product pd INNER JOIN  productid_landingcostcategoryid p on pd.id=p.productid INNER JOIN inventory i on p.productid in (i.product) INNER JOIN grdetails grd on i.id=grd.id INNER JOIN goodsreceiptid_landedInvoice gl on grd.goodsreceipt in (gl.landedinvoice)  WHERE gl.goodsreceiptid=? AND p.lccategoryid= ? group by pd.id,grd.goodsreceipt,grd.rate";
            list= executeSQLQuery(query, new Object[]{GoodsReceiptId,landingCostCategory});
            if (list.size() > 0) {
                result = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new KwlReturnObject(result, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getManualProductCostLCC(String GoodsReceiptId ,String landingCostCategory) throws ServiceException {
        return getManualProductCostLCC(GoodsReceiptId, landingCostCategory, null);
    }
    public KwlReturnObject getManualProductCostLCC(String GoodsReceiptId, String landingCostCategory, String assetDetailId) throws ServiceException {
        boolean result = false;
        List list = new ArrayList();
        List params = new ArrayList();
        try {
            String query = "";
            query = " from LccManualWiseProductAmount where expenseInvoiceid.id = ? and grdetailid.id = ?";
            params.add(GoodsReceiptId);
            params.add(landingCostCategory);
            if (!StringUtil.isNullOrEmpty(assetDetailId)) {
                query += " and assetDetails.id = ? ";
                params.add(assetDetailId);
            }
            list = executeQuery(query, params.toArray());
            if (list.size() > 0) {
                result = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new KwlReturnObject(result, "", null, list, list.size());
    }
    public KwlReturnObject getGoodsReceipt_LandedInvoice(Map<String, Object> requestMap) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String productId = "";
        String companyId = "";
        DateFormat df = null;
        boolean isApproved=false;
        String startdate = "";
        String enddate = "";
        
        if (requestMap.containsKey("companyId")) {
            companyId = requestMap.get("companyId").toString();
            params.add(companyId);
        }
        if (requestMap.containsKey("df")) {
            try {
                df = (DateFormat) requestMap.get("df");
                if (requestMap.containsKey("startdate")) {
                    startdate = requestMap.get("startdate").toString();
                }
                if (requestMap.containsKey("enddate")) {
                    enddate = requestMap.get("enddate").toString();
                }
//                condition += " and (gr.journalEntry.entryDate >=? and gr.journalEntry.entryDate <=?)";
                condition += " and (gr.creationDate >=? and gr.creationDate <=?)";
                params.add(df.parse(startdate));
                params.add(df.parse(enddate));
            } catch (ParseException ex) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (requestMap.containsKey("isApproved")) {
            isApproved = Boolean.parseBoolean(requestMap.get("isApproved").toString());
            if (isApproved) {
                condition += " and grd.goodsReceipt.approvestatuslevel=?";
                params.add(11);
            }
        }
        
        condition+= " and gr.isExpenseType <>'T' and gr.deleted = 'F' ";
        
        String q = "from GoodsReceipt gr where  gr.company.companyID=?"+condition;// and grd.goodsReceipt.deleted=false";
        list = executeQuery( q,params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
   @Override
    public KwlReturnObject deleteGoodsReceiptsLandedInvoice(String receiptid, String companyid) throws ServiceException {
        //Delete Goods Receipts
        String delQuery = "delete from goodsreceiptid_landedInvoice WHERE goodsreceiptid = ?";
        int numRows = executeSQLUpdate( delQuery, new Object[]{receiptid});
        return new KwlReturnObject(true, "Goods Receipt Landed Invoice has been deleted successfully.", null, null, numRows);
    }
  @Override
    public KwlReturnObject getTDSAppliedVendorInvoices(Map<String, Object> requestParams) throws ServiceException {
        //Get TDS Applied Goods Receipts
        String conditionQuery = "";
        ArrayList params = new ArrayList();
        String hqlQuery = " from "+requestParams.get("class")+" grd ";

        if (requestParams.containsKey(Constants.companyKey) && requestParams.get(Constants.companyKey) != null && !StringUtil.isNullOrEmpty(requestParams.get(Constants.companyKey).toString())) {
            if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                conditionQuery += " and ";
            }
            conditionQuery += " grd.goodsReceipt.company.companyID = ? ";
            params.add(requestParams.get(Constants.companyKey));
        }
        if (requestParams.containsKey("isTDSApplied") && requestParams.get("isTDSApplied") != null && !StringUtil.isNullOrEmpty(requestParams.get("isTDSApplied").toString())) {
            if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                conditionQuery += " and ";
            }
            conditionQuery += " (grd.tdsLineAmount > 0 OR (grd.tdsLineAmount = 0 AND grd.tdsJEMapping IS NOT NULL)) ";
        }
        int tdsPaymentType = IndiaComplianceConstants.NOTDSPAID;
          if (requestParams.containsKey("tdsPaymentType") && requestParams.get("tdsPaymentType") != "") {
              tdsPaymentType = (int) requestParams.get("tdsPaymentType");
          }
        if (requestParams.containsKey("paymentid") && requestParams.get("paymentid") != null && !StringUtil.isNullOrEmpty(requestParams.get("paymentid").toString())) {
          String TDSPaymentCondition = " and ( grd.tdsPayment = ? or  grd.tdsInterestPayment = ? )  ";
          params.add(requestParams.get("paymentid"));
          params.add(requestParams.get("paymentid"));
          conditionQuery += TDSPaymentCondition;
        }
        if (requestParams.containsKey("isPayment") && requestParams.get("isPayment") != null && !StringUtil.isNullOrEmpty(requestParams.get("isPayment").toString())) {
          boolean isPayment = Boolean.parseBoolean(requestParams.get("isPayment").toString());
          if (isPayment) {
              if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                  conditionQuery += " and ";
              }
              String TDSPaymentCondition = "";
              if (tdsPaymentType == IndiaComplianceConstants.TDSPAYMENT || tdsPaymentType == IndiaComplianceConstants.TDSANDTDSINTERESTPAYMENT) {
                  TDSPaymentCondition += " grd.tdsPaidFlag = 0 ";
              }
              if (tdsPaymentType == IndiaComplianceConstants.TDSINTERESTPAYMENT || tdsPaymentType == IndiaComplianceConstants.TDSANDTDSINTERESTPAYMENT) {
                  if (!StringUtil.isNullOrEmpty(TDSPaymentCondition)) {
                      TDSPaymentCondition += " and ";
                  }
                  TDSPaymentCondition += " grd.tdsInterestPaidFlag = 0 ";
              }
              conditionQuery += TDSPaymentCondition;
          }
      }
        // Filters
        if (requestParams.containsKey("vendorId") && requestParams.get("vendorId") != null && !StringUtil.isNullOrEmpty(requestParams.get("vendorId").toString())) {
            if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                conditionQuery += " and ";
            }
            conditionQuery += " grd.goodsReceipt.vendor.ID = ? ";
            params.add(requestParams.get("vendorId"));
        }
        if (requestParams.containsKey("deducteetype") && requestParams.get("deducteetype") != null && !StringUtil.isNullOrEmpty(requestParams.get("deducteetype").toString())) {
            if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                conditionQuery += " and ";
            }
            conditionQuery += " grd.goodsReceipt.vendor.deducteeCode = ? ";
            params.add(requestParams.get("deducteetype"));
        }
        if (requestParams.containsKey("nop") && requestParams.get("nop") != null && !StringUtil.isNullOrEmpty(requestParams.get("nop").toString())) {
            if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                conditionQuery += " and ";
            }
            conditionQuery += " grd.natureOfPayment.ID = ? ";
            params.add(requestParams.get("nop"));
        }




        if (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null
                && requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null
                && requestParams.containsKey(Constants.REQ_enddate) && requestParams.get(Constants.REQ_enddate) != null) {
            try {
                String startdate = "", enddate = "";
                DateFormat df = (DateFormat) requestParams.get(Constants.df);

                startdate = requestParams.get("startdate").toString();
                enddate = requestParams.get("enddate").toString();

                Date startDate = df.parse(startdate);
                Date endDate = df.parse(enddate);
                if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                    conditionQuery += " and ";
                }
                conditionQuery += " ( grd.goodsReceipt.journalEntry.entryDate >= ? and grd.goodsReceipt.journalEntry.entryDate <= ? )";
                params.add(startDate);
                params.add(endDate);
                
                conditionQuery+= " and grd.goodsReceipt.deleted=? ";
                params.add(false);
                
                conditionQuery += " ORDER BY grd.goodsReceipt.journalEntry.entryDate ";
            } catch (ParseException ex) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!StringUtil.isNullOrEmpty(conditionQuery)) {
            hqlQuery += " where " + conditionQuery;
        }
        List list = executeQuery(hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
  
        public KwlReturnObject getGoodsReceiptForCommissionSchema(HashMap<String, Object> request) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(request.get("companyid").toString());
        boolean isExpense = false;
        DateFormat df = (DateFormat) request.get("df");
        String table = "GoodsReceiptDetail";
        if (request.containsKey("isExpense")) {
            isExpense = Boolean.parseBoolean(request.get("isExpense").toString());
            if (isExpense) {
                table = "ExpenseGRDetail";
            }
        }
        String condition = "";
        try {
            if (request.containsKey("startdate")) {
                condition += " and jed.journalEntry.entryDate>=? ";
                String startdate = (String) request.get("startdate");
                params.add(df.parse(startdate));

            }
            if (request.containsKey("enddate")) {
                condition += " and  jed.journalEntry.entryDate<=?";
                String enddate = (String) request.get("enddate");
                params.add(df.parse(enddate));
            }
        } catch (ParseException ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
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
                            request.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);

                            mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                            if (mySearchFilterString.contains("accjecustomdata")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "grd.goodsReceipt.journalEntry.accBillInvCustomData");//
//                                joinString1 = " inner join GoodsReceipt on GoodsReceipt.ID = GoodsReceiptDetail.goodsReceipt.ID  inner join AccJECustomData on AccJECustomData.journalentryId=goodsreceipt.journalentry ";
//                                joinString1 = " inner join grd.goodsReceipt gr";
                            }
                            if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jed.accJEDetailCustomData");//
//                                joinString1 += " left join accjedetailcustomdata  on accjedetailcustomdata.jedetailId=jedetail.id ";
                            }
                            if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jed.accJEDetailsProductCustomData");//    
//                                joinString1 += " left join accjedetailproductcustomdata  on accjedetailproductcustomdata.jedetailId=jedetail.id ";
                            }
                            StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                        } catch (ParseException ex) {
                            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

        String query = " select DISTINCT  grd from " + table + " grd  left join grd.purchaseJED  jed where  grd.goodsReceipt.approvestatuslevel=11 and grd.goodsReceipt.istemplate!=2 and grd.company.companyID=? " + condition + mySearchFilterString;
//        String query = " select DISTINCT  grd from " + table + " grd  left join grd.purchaseJED";
//        params=new ArrayList();
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /*
     * Method for fetching purchase invoices with global level tax
     */ 
    @Override
    public KwlReturnObject getGoodsReceiptsWithGlobalTax(HashMap<String, Object> map) throws ServiceException {
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
            condition += " and gr.currency.currencyID not in (?) ";
            params.add(currencyToExclude);
        }
        String query = "From GoodsReceipt gr where gr.tax is not null and gr.gstCurrencyRate = 0 and gr.isOpeningBalenceInvoice = false and gr.company.companyID = ? "+condition;
        list = executeQuery(query, params.toArray());
        
        return new KwlReturnObject(true, "", "", list, list.size());
    }

    /*
     * Method for fetching purchase invoices with line level tax
     */ 
    @Override
    public KwlReturnObject getGoodsReceiptsWithLineLevelTax(HashMap<String, Object> map) throws ServiceException {
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
            condition += " and grd.goodsReceipt.currency.currencyID not in (?) ";
            params.add(currencyToExclude);
        }
        String query = "From GoodsReceiptDetail grd where grd.tax is not null and grd.gstCurrencyRate = 0 and grd.goodsReceipt.isOpeningBalenceInvoice = false and grd.company.companyID = ? "+condition;
        list = executeQuery(query, params.toArray());
        
        return new KwlReturnObject(true, "", "", list, list.size());
    }   
    
    @Override
    public KwlReturnObject saveManualLandedCostCategoryDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            LccManualWiseProductAmount lccDetails=new LccManualWiseProductAmount();
            if (!dataMap.containsKey("isEdit") || (dataMap.containsKey("isEdit") && (Boolean) dataMap.get("isEdit")==false)) {
                    if (dataMap.containsKey("id") && dataMap.get("id") != null && !StringUtil.isNullOrEmpty(dataMap.get("id").toString())) {
                        lccDetails.setID((String) dataMap.get("id"));
                    }
            }
            if (dataMap.containsKey("originalTransactionRowid") && dataMap.get("originalTransactionRowid")!=null) {
                GoodsReceiptDetail grd = (GoodsReceiptDetail) get(GoodsReceiptDetail.class, (String) dataMap.get("originalTransactionRowid"));
                lccDetails.setGrdetailid(grd);
            }
            if (dataMap.containsKey("expenseid") && dataMap.get("expenseid")!=null) {
                GoodsReceipt gr = (GoodsReceipt) get(GoodsReceipt.class, (String) dataMap.get("expenseid"));
                lccDetails.setExpenseInvoiceid(gr);
            }
            if (dataMap.containsKey("enterpercentage") && dataMap.get("enterpercentage")!=null) {
               double percentage=Double.valueOf(dataMap.get("enterpercentage").toString());
               lccDetails.setPercentage(percentage);
            }
            if (dataMap.containsKey("enteramount") && dataMap.get("enteramount") != null) {
                double amount = Double.valueOf(dataMap.get("enteramount").toString());
                lccDetails.setAmount(amount);
            }       
            if (dataMap.containsKey("igstrate") && dataMap.get("igstrate") != null) {
                double igstrate = Double.valueOf(dataMap.get("igstrate").toString());
                lccDetails.setIgstrate(igstrate);
            }       
            if (dataMap.containsKey("igstamount") && dataMap.get("igstamount") != null) {
                double igstamount = Double.valueOf(dataMap.get("igstamount").toString());
                lccDetails.setIgstamount(igstamount);
            }       
            if (dataMap.containsKey("taxablevalueforigst") && dataMap.get("taxablevalueforigst") != null) {
                double taxablevalueforigst = Double.valueOf(dataMap.get("taxablevalueforigst").toString());
                lccDetails.setTaxablevalueforigst(taxablevalueforigst);
            }       
            if (dataMap.containsKey("customdutyandothercharges") && dataMap.get("customdutyandothercharges") != null) {
                double customdutyandothercharges = Double.valueOf(dataMap.get("customdutyandothercharges").toString());
                lccDetails.setCustomdutyandothercharges(customdutyandothercharges);
            }       
            if (dataMap.containsKey("taxablevalueforcustomduty") && dataMap.get("taxablevalueforcustomduty") != null) {
                double taxablevalueforcustomduty = Double.valueOf(dataMap.get("taxablevalueforcustomduty").toString());
                lccDetails.setTaxablevalueforcustomduty(taxablevalueforcustomduty);
            }       
            if (dataMap.containsKey("customDutyAllocationType") && dataMap.get("customDutyAllocationType") != null) {
                boolean customDutyAllocationType = (Boolean) dataMap.get("customDutyAllocationType");
                lccDetails.setCustomDutyAllocationType(customDutyAllocationType);
            }     
            if (dataMap.containsKey("assetId") && dataMap.get("assetId") != null) {
                AssetDetails assetDetails = (AssetDetails) get(AssetDetails.class, (String) dataMap.get("assetId"));
                lccDetails.setAssetDetails(assetDetails);
            }
            saveOrUpdate(lccDetails);
            list.add(lccDetails);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.saveManualLandedCostCategoryDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    } 
    /*
     *Function to get terms from mapping table
     */
    public List getTerms(String tax) throws ServiceException {
        String query = "select invoicetermssales from taxtermsmapping where tax = ?";
        List list = executeSQLQuery(query, new Object[]{tax});
        return list;
    }
  
    /*----Return true if Invoice is not linked with any GR----*/
    public List isInvoiceNotLinkedWithAnyGR(GoodsReceipt invoice) throws ServiceException {
        ArrayList paramslist = new ArrayList();
        List list = new ArrayList();
        List returnList = new ArrayList();
        int pendingGRCount = 0;
        boolean isNotLinkedWithAnyGR = true;
        boolean isLinkedWithGRPartially = false;
        boolean isAnyGRNIsInPendingState = false;
        try {
            String selQuery = "from GoodsReceiptLinking grl  WHERE grl.DocID.ID = ? ";
            paramslist.add(invoice.getID());
            list = executeQuery(selQuery, paramslist.toArray());
            if (list.size() > 0) {              
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    GoodsReceiptLinking grlinking = (GoodsReceiptLinking) itr.next();
                    if (grlinking.getSourceFlag() == 0 && grlinking.getModuleID()==Constants.Acc_Goods_Receipt_ModuleId) {
                        isNotLinkedWithAnyGR = false;
                        GoodsReceiptOrder gr = (GoodsReceiptOrder) get(GoodsReceiptOrder.class, grlinking.getLinkedDocID());
                        if (gr.getApprovestatuslevel() != 11 && list.size() > 1) {
                           isLinkedWithGRPartially=true;
                           isAnyGRNIsInPendingState=true;
                            pendingGRCount++;

                        } else if (list.size() == 1 && gr.getApprovestatuslevel() != 11) {
                            isNotLinkedWithAnyGR = true;
                            isAnyGRNIsInPendingState=true;
                            break;
                        }
                    }
                }
                /* -----If all GR linked with PI (i.e PI->GR ) are in Pending state-------*/
                if (list.size() > 1 && list.size() == pendingGRCount) {
                    isLinkedWithGRPartially = false;
                    isNotLinkedWithAnyGR = true;
                }

            }
            returnList.add(isNotLinkedWithAnyGR);
            returnList.add(isLinkedWithGRPartially);
            returnList.add(isAnyGRNIsInPendingState);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return returnList;
    }
    
        /*----Return true if linking like GR->PI----*/
    
    public boolean isLinkingOfGRInPI(GoodsReceipt invoice) throws ServiceException {
        ArrayList paramslist = new ArrayList();
        List list = new ArrayList();

        boolean isLinkingOfGRInPI = false;
        try {
            String selQuery = "from GoodsReceiptLinking grl  WHERE grl.DocID.ID = ?  and grl.SourceFlag=1 and grl.ModuleID=?";
            paramslist.add(invoice.getID());
            paramslist.add(Constants.Acc_Goods_Receipt_ModuleId);
            list = executeQuery(selQuery, paramslist.toArray());
            if (list.size() > 0) {
                isLinkingOfGRInPI = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return isLinkingOfGRInPI;
    }
    /**
     * 
     * @param map
     * @return
     * @Desc : Fetch Job Order subgredient
     * @throws ServiceException 
     */
        public KwlReturnObject getJobOrderSubgredients(Map<String, Object> map) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        StringBuilder condition=new StringBuilder();
        String jobworkorderdetail = "";
        String companyId = "";
        boolean isjoborder = true;
        if (map.containsKey("companyid") && map.get("companyid") != null) {
            companyId = map.get("companyid").toString();
            params.add(companyId);
            condition.append(" ist.company.companyID=?");
        }
        if (map.containsKey("jobworkorderdetail") && map.get("jobworkorderdetail") != null) {
            jobworkorderdetail = map.get("jobworkorderdetail").toString();
            params.add(jobworkorderdetail);
            condition.append(" and ist.purchaseOrderDetail=?");
        }
        if (map.containsKey("isjoborder") && map.get("isjoborder") != null) {
            isjoborder = Boolean.parseBoolean(map.get("isjoborder").toString());
            params.add(isjoborder);
            condition.append(" and ist.isJobWorkStockTransfer=?");
        }
        /*
         * isjobworkclose is true means sales invoice is created by using job work stock out in Aged order work report.
         */
        condition.append("and isjobworkclose='F'");
        String query = "From InterStoreTransferRequest ist where " + condition;
        list = executeQuery(query, params.toArray());

        return new KwlReturnObject(true, "", "", list, list.size());
    }
    public KwlReturnObject saveGRODetailsStockOutISTMapping(Map<String, Object> mappingParams) throws ServiceException {
        List list = new ArrayList();
        InterStoreTransferRequest interStoreTransferRequest = null;
        StockAdjustment stockAdjustment = null;
        GoodsReceiptOrderDetails orderDetails = null;
        GRODetailsStockOutISTMapping detailsStockOutISTMapping = null;

        detailsStockOutISTMapping = new GRODetailsStockOutISTMapping();

        if (mappingParams.containsKey("grd")) {
            orderDetails = (GoodsReceiptOrderDetails) mappingParams.get("grd");
            detailsStockOutISTMapping.setGoodsReceiptOrderDetails(orderDetails);
        }
        if (mappingParams.containsKey("ist")) {
            interStoreTransferRequest = (InterStoreTransferRequest) mappingParams.get("ist");
            detailsStockOutISTMapping.setInterStoreTransferRequest(interStoreTransferRequest);
        }
        if (mappingParams.containsKey("stockout")) {
            stockAdjustment = (StockAdjustment) mappingParams.get("stockout");
            detailsStockOutISTMapping.setStockAdjustment(stockAdjustment);
        }
        if (mappingParams.containsKey("quantity")) {
            double quantity = (double) mappingParams.get("quantity");
            detailsStockOutISTMapping.setOutQty(quantity);
        }
        saveOrUpdate(detailsStockOutISTMapping);
        list.add(detailsStockOutISTMapping);
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public double getSumofChallanUsedQuantity(String interstoretransfer) throws ServiceException {
        double qty = 0;
        String query = "select sum(outqty) from grodstockoutistmapping where interstoretransfer=?";
        List list = executeSQLQuery(query, new Object[]{interstoretransfer});
        if (list.size() > 0 && list.get(0) != null) {
            qty = (double) list.get(0);
        }
        return qty;

    }
    /**
     * @Desc : Delete IST and GRO Mapping
     * @param json
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject deleteGRODetailISTMapping(JSONObject json) throws ServiceException {
        List params = new ArrayList();
        if (!StringUtil.isNullOrEmpty(json.optString("istrequest"))) {
            params.add(json.optString("istrequest"));
        }
        String query = "delete from grodetailistmapping where istrequest=?";
        executeSQLUpdate(query, params.toArray());
        return new KwlReturnObject(true, "get Batch Remaining Qty From IST.", "", null, 0);
    }
    
    /**
     * @Desc : Update memo for IST request at a time of GRN creation
     * @param memo
     * @param grOrderId 
     * @param companyid 
     */
    @Override
    public void updateMemoForIST(String memo, String grOrderId, String companyid) {
        try {
            String sqlQuery = "update grodetailistmapping grodm inner join grodetails grod on grod.id = grodm.grodetail inner join grorder gro on gro.id = grod.grorder inner join in_interstoretransfer ist on ist.id = grodm.istrequest set ist.memo = ? where gro.company = ? and gro.id = ?";
            executeSQLUpdate(sqlQuery, new Object[]{memo, companyid, grOrderId});
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
    }
    /**
     * @Desc : Update memo for IST request and Stock Adjustment  at a time of GRN creation
     * @param iSTmemo
     * @param stockAdjustmentMemo
     * @param grOrderId 
     * @param companyid 
     */
    @Override
    public void updateMemoForJWOSA(String stockAdjustmentMemo, String grorderId, String companyid){
        try {
            String sqlQuery = "UPDATE grorder INNER JOIN grodetails ON grodetails.grorder = grorder.id INNER JOIN grodstockoutistmapping grodistmap ON grodistmap.grodetails = grodetails.id INNER JOIN in_stockadjustment ON in_stockadjustment.id = grodistmap.stockadjustment  INNER JOIN in_stockmovement sm ON sm.modulerefid = in_stockadjustment.id SET in_stockadjustment.memo = ?,  in_stockadjustment.remark = ?, sm.memo = ?,  sm.remark = ? WHERE grorder.id =? AND grorder.company =?";
            executeSQLUpdate(sqlQuery, new Object[]{stockAdjustmentMemo,stockAdjustmentMemo,stockAdjustmentMemo,stockAdjustmentMemo,grorderId,companyid});
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
    }
    /**
     * Get Purchase invoice linked with Purchase return.
     * @param purchasereturnid
     * @return
     * @throws ServiceException 
     */
    @Override
     public KwlReturnObject getGoodsReceiptsLinkedWithPR(String purchasereturnid) throws ServiceException {
        List list = null;
        if (!StringUtil.isNullOrEmpty(purchasereturnid)) {
            String selQuery = "from PurchaseReturnLinking PRL where  PRL.DocID.ID =?  and PRL.ModuleID=6";
            list = executeQuery(selQuery, new Object[]{purchasereturnid});
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * 
     * @param reqMap
     * @return
     * @Desc : Get Stock Out GRN Mapping
     * @throws ServiceException 
     */
    public KwlReturnObject getStockOutGRNMapping(Map<String, Object> reqMap) throws ServiceException {
        String grodetail = (String) reqMap.get("grodid");
        ArrayList params = new ArrayList();
        params.add(grodetail);
        String query = "From GRODetailsStockOutISTMapping where goodsReceiptOrderDetails.ID=?";
        List list1 = executeQuery(query, params.toArray());

        return new KwlReturnObject(true, "", "", list1, list1.size());
    }
    /**
     * 
     * @param reqMap
     * @Desc Update Inventory qty in case of Job Work Out GRN
     * @throws ServiceException 
     */

    public void updateQuantityinStock(Map<String, Object> reqMap) throws ServiceException {
        String productid = (String) reqMap.get("productid");
        String storeid = (String) reqMap.get("storeid");
        String locationid = (String) reqMap.get("locationid");
        double qty = (Double) reqMap.get("qty");
        String batchname = "";
        String SerialName = "";
        int count = 0;
        if (reqMap.containsKey("batchname") && reqMap.get("batchname") != null) {
            batchname = (String) reqMap.get("batchname");
        }
        if (reqMap.containsKey("SerialName") && reqMap.get("SerialName") != null) {
            SerialName = (String) reqMap.get("SerialName");
        }

        if (StringUtil.isNullOrEmpty(batchname)) {
            /**
             * If Batch Name is empty
             */
            String updatequry = "update in_stock set quantity = quantity + ? where store=?  AND location=? AND product=?";
            count = executeSQLUpdate(updatequry, new Object[]{qty, storeid, locationid, productid});
            updatequry = "update newproductbatch set quantitydue = quantitydue + ? where warehouse=? AND batchname=? AND location=? AND product=?";
            count = executeSQLUpdate(updatequry, new Object[]{qty, storeid, "", locationid, productid});
        } else if (!StringUtil.isNullOrEmpty(batchname) && StringUtil.isNullOrEmpty(SerialName)) {
            /**
             * If Batch not empty but serial empty
             */
            String updatequry = "update in_stock set quantity = quantity + ? where store=? AND batchname=? AND location=? AND product=?";
            count = executeSQLUpdate(updatequry, new Object[]{qty, storeid, batchname, locationid, productid});
            updatequry = "update newproductbatch set quantitydue = quantitydue + ? where warehouse=? AND batchname=? AND location=? AND product=?";
            count = executeSQLUpdate(updatequry, new Object[]{qty, storeid, batchname, locationid, productid});
        } else if (!StringUtil.isNullOrEmpty(SerialName)) {
            /**
             * If serial present
             */
            String updatequry = "update in_stock set quantity = quantity + ? where store=? AND batchname=? AND location=? AND product=?";
            count = executeSQLUpdate(updatequry, new Object[]{qty, storeid, batchname, locationid, productid});

            String selectquery = "select id from in_stock where  NULLIF(serialnames, ' ') IS NULL and store=?  AND batchname=? AND location=? AND product=?";
            List l = executeSQLQuery(selectquery, new Object[]{storeid, batchname, locationid, productid});
            if (l.size() > 0 && l.get(0) != null) {
                /**
                 * If serial name column is NULL
                 */
                updatequry = "update in_stock set serialnames =  ?  where product=? and store=? and location=? and batchname=?";
                count = executeSQLUpdate(updatequry, new Object[]{SerialName, productid, storeid, locationid, batchname});
            } else {
                /**
                 * If serial column has some data
                 */
                updatequry = "update in_stock set serialnames = CONCAT_WS(',',serialnames,?) where product=? and store=? and location=? and batchname=?";
                count = executeSQLUpdate(updatequry, new Object[]{SerialName, productid, storeid, locationid, batchname});
            }

            /**
             * Update serial and batch details
             *
             */
            String batchid = "";
            String selectbatch = "select id from newproductbatch where warehouse=? AND batchname=? AND location=? AND product=?";
            l = executeSQLQuery(selectbatch, new Object[]{storeid, batchname, locationid, productid});
            if (l.size() > 0 && l.get(0) != null) {
                batchid = (String) l.get(0);
            }
            updatequry = "update newproductbatch set quantitydue = quantitydue + ? where warehouse=? AND batchname=? AND location=? AND product=?";
            count = executeSQLUpdate(updatequry, new Object[]{qty, storeid, batchname, locationid, productid});

            String[] serialarray = SerialName.split(",");
            for (int serialindex = 0; serialindex < serialarray.length; serialindex++) {

                updatequry = "update newbatchserial set quantitydue = 1 where serialname=? and product=? and batch=?";
                count = executeSQLUpdate(updatequry, new Object[]{serialarray[serialindex], productid, batchid});
            }
        }

        /**
         * Update product available quantity
         */
        String quantityquery = "update product set availablequantity = availablequantity + ? where  id=?";
        count = executeSQLUpdate(quantityquery, new Object[]{qty, productid});
    }
/**
 * 
 * @param reqMap
 * @Desc : Delete Stock Out
 * @throws ServiceException 
 */
    public void deleteStockAdjustment(Map<String, Object> reqMap) throws ServiceException {
        String stockadjustmentid = (String) reqMap.get("stockadjustmentid");
        String inventoryid = (String) reqMap.get("inventoryid");
        String journalentryid = (String) reqMap.get("journalentryid");
        int count = 0;
        /**
         * delete Stock Movement
         */
        String deletemvd = "delete from in_sm_detail where stockmovement in (select id from in_stockmovement "
                + "where modulerefid =?)";
        count = executeSQLUpdate(deletemvd, new Object[]{stockadjustmentid});

        String deletemv = "delete from in_stockmovement where modulerefid=?";
        count = executeSQLUpdate(deletemv, new Object[]{stockadjustmentid});

        /**
         * Delete GRN-IST-Stock Out Mapping
         */
        String deletemapping = "delete from grodstockoutistmapping where stockadjustment=?";
        count = executeSQLUpdate(deletemapping, new Object[]{stockadjustmentid});

        /**
         * Delete Stock Adjustment
         */
        String deletestd = " delete from in_sa_detail where stockadjustment=?";
        count = executeSQLUpdate(deletestd, new Object[]{stockadjustmentid});

        String deletest = " delete from in_stockadjustment where id=?";
        count = executeSQLUpdate(deletest, new Object[]{stockadjustmentid});

        /**
         * delete inventory record
         */
        String deleteinv = "delete from inventory where id=?";
        count = executeSQLUpdate(deleteinv, new Object[]{inventoryid});

        /**
         * delete Journal Entry
         */
        if (!StringUtil.isNullOrEmpty(journalentryid)) {
            String deletejed = "delete from jedetail where journalentry =?";
            count = executeSQLUpdate(deletejed, new Object[]{journalentryid});

            String deleteje = "delete from journalentry where id=?";
            count = executeSQLUpdate(deleteje, new Object[]{journalentryid});
        }

    }
        
    @Override
    public List<GoodsReceiptLinking> getGoodsReceiptLinkingDataToValidateLinkingInfo(HashMap<String, Object> requestParams) throws ServiceException {
        List<GoodsReceiptLinking> list = new ArrayList();
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            if(requestParams.containsKey(Constants.moduleid)&& requestParams.get(Constants.moduleid)!=null){
                condition+=" AND grl.ModuleID = ? ";
                params.add(requestParams.get(Constants.moduleid));
            }
            if (requestParams.containsKey(Constants.companyKey) && requestParams.get(Constants.companyKey) != null) {
                condition += " AND grl.DocID.company.companyID = ? ";
                params.add(requestParams.get(Constants.companyKey));
            }
            String query = "SELECT grl FROM GoodsReceiptLinking grl WHERE grl.SourceFlag = 0 " + condition;
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.getGoodsReceiptLinkingDataToValidateLinkingInfo : " + ex.getMessage(), ex);
        }
        return list;
    }
    
    /**
     * @param grID :  ID of Purchase Invoice
     * @Desc : Return Gr amount in base used in Payment while creating payment
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getPaymentDetailsOfGR(String grID) throws ServiceException {
        String query = "SELECT COALESCE(sum(jed.amountinbase),0)  "
                + "FROM paymentdetail "
                + "INNER JOIN jedetail jed on jed.id=paymentdetail.totaljedid "
                + "INNER JOIN payment pmt on pmt.id=paymentdetail.payment "
                + "WHERE paymentdetail.goodsReceipt=? and pmt.deleteflag='F'";
        List list = executeSQLQuery(query, new Object[]{grID});
        return new KwlReturnObject(true, "", "", list, list.size());
    }

     /**
     * @param grID :  ID of Purchase Invoice
     * @Desc : Return Gr amount in GR Currency linked in Debit Note 
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getLinkedPaymentDetailsOfGR(String grID) throws ServiceException {
//        String query = "SELECT COALESCE(amount/exchangeratefortransaction,0) as gramount "
//                + "FROM linkdetailpayment "
//                + "INNER JOIN payment pmt on pmt.id=linkdetailpayment.payment "
//                + "WHERE linkdetailpayment.goodsReceipt=? and pmt.deleteflag='F'";
        String query = "SELECT COALESCE(ldp.amount,0) AS enteredamount,COALESCE(forexjed.amountinbase,0) AS forexjeamountinbase,if(mp.isopeningbalencepayment=1,mp.exchangerateforopeningtransaction,paymentje.externalcurrencyrate) AS paymentexchangerate, mp.isopeningbalencepayment, mp.isconversionratefromcurrencytobase,if(forexjed.debit= 'T' , 'true' , 'false') as isdebit "
                + "FROM linkdetailpayment ldp "
                + "INNER JOIN payment mp ON mp.id=ldp.payment "
                + "LEFT JOIN journalentry paymentje ON paymentje.id=mp.journalentry "
                + "LEFT JOIN journalentry forexje ON forexje.id=ldp.linkedgainlossje "
                + "LEFT JOIN jedetail forexjed ON forexjed.journalEntry=forexje.id "
                + "WHERE ldp.goodsReceipt=? AND mp.deleteflag='F' "
                + "GROUP BY ldp.id";
        List list = executeSQLQuery(query, new Object[]{grID});
        return new KwlReturnObject(true, "", "", list, list.size());
    }
     /**
     * @param grID :  ID of Purchase Invoice
     * @Desc : Return Gr amount in GR Currency linked in Payment
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getLinkedDebitNoteDetailsOfGR(String grID) throws ServiceException {
//        String query = "SELECT COALESCE(amountinInvCurrency,0) as gramount "
//                + "FROM dndetails dnd "
//                + "INNER JOIN debitnote dn on dn.id=dnd.debitNote "
//                + "INNER JOIN discount dis on dis.id=dnd.discount "
//                + "WHERE dnd.goodsreceipt=? and dn.deleteflag='F' ";
        String query = "SELECT COALESCE(dis.discount,0) AS enteredamount, COALESCE(forexjed.amountinbase,0) AS forexjeamountinbase,if(dn.isopeningbalencedn=1,dn.exchangerateforopeningtransaction,dnje.externalcurrencyrate) AS cnexchangerate, dn.isopeningbalencedn,dn.isconversionratefromcurrencytobase,if(forexjed.debit= 'T' , 'true' , 'false') as isdebit "
                + "FROM dndetails dnd INNER JOIN debitnote dn ON dn.id=dnd.debitNote "
                + "INNER JOIN discount dis ON dis.id=dnd.discount "
                + "LEFT JOIN journalentry dnje ON dnje.id=dn.journalentry "
                + "LEFT JOIN journalentry forexje ON forexje.id=dnd.linkedgainlossje "
                + "LEFT JOIN jedetail forexjed ON forexjed.journalEntry=forexje.id "
                + "where dnd.goodsreceipt=? AND dn.deleteflag='F' "
                + "GROUP BY dnd.id";
        List list = executeSQLQuery(query, new Object[]{grID});
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    /**
     * 
     * @param grDetails
     * @Desc : returns the GoodsReceipt Object
     * @return
     * @throws ServiceException 
     */
     
    public KwlReturnObject getGoodsReceiptCountForImport(JSONObject grDetails)  throws ServiceException{
        List list = new ArrayList();
        int count = 0;
        String grNo=grDetails.optString("grNo", "");
        String companyId=grDetails.optString(Constants.companyKey, "");
        String vendorId=grDetails.optString(Constants.vendorid, "");
        String q = " from GoodsReceipt where goodsReceiptNumber = ? and company.companyID = ? and vendor.ID = ? and approvestatuslevel=11 and deleted=false";
        list = executeQuery( q, new Object[]{grNo, companyId,vendorId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
}

    /**
     * Get Purchase Return Quantity when PR is Save in lInking with :
     * PO-PI-GR-PR or PO-GR-PR.
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    @Override
    public Map<String,Object> getPurchaseReturnQuantityLinkWithPO(JSONObject requestParams) throws ServiceException {
        List list = null;
        Map<String,Object> result = new HashMap<>();
        try {
            String query = "";
            String companyID = "";
            String purchaseOrderDetailsID = "";
            if (requestParams.has(Constants.companyid)) {
                companyID = requestParams.optString(Constants.companyid, "");
            }
            if (requestParams.has("purchaseOrderDetailsID")) {
                purchaseOrderDetailsID = requestParams.optString("purchaseOrderDetailsID", "");
            }
    
            query = "select pod.id, sum(prd.returnquantity) from purchaseorder po, podetails pod , grdetails pid , grodetails grd , prdetails prd "
                    + "where po.id = pod.purchaseorder and "
                    + "pod.id = pid.purchaseorderdetail and "
                    + "pid.id = grd.videtails and "
                    + "grd.id = prd.grdetails and "
                    + "prd.company = ? and po.id = ? group by pod.id "
                    + " UNION "
                    + "select pod.id, sum(prd.returnquantity) from purchaseorder po, podetails pod ,grodetails grd , prdetails prd "
                    + "where po.id = pod.purchaseorder and "
                    + "pod.id = grd.podetails and " 
                    + "grd.id = prd.grdetails and " 
                    + "prd.company = ? and po.id = ? group by pod.id";
            list = executeSQLQuery(query, new Object[]{companyID,purchaseOrderDetailsID,companyID,purchaseOrderDetailsID});
            
            if(!list.isEmpty()){
                for (Object object : list) {
                    Object[] objArr = (Object[]) object;
                    result.put((String)objArr[0],(Double)objArr[1]);
                }
               
            }
            
        } catch (ServiceException ex) {
            Logger.getLogger("checkPurchaseReturnAndgetReturnQuantity").log(Level.SEVERE, null, ex);
        }
        return result;
        
    }
    
    /**
     *
     * @Purpose : Get transactions whose amountdue is zero and Rounding JE is
     * not posted, for posting rounding JE if applicable
     * @param : moduleid, companyid
     * @return : KwlReturnObject
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getTransactionsForRoundingJE(int moduleid, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "";
            if (moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                query = " FROM GoodsReceipt gr "
                        + "WHERE "
                        + "gr.company.companyID=? and gr.deleted=false "
                        + "and ((gr.invoiceamountdue='0' AND gr.isOpeningBalenceInvoice=false) OR (gr.openingBalanceAmountDue='0' AND gr.isOpeningBalenceInvoice=true)) "
                        + "and gr.ID not in (select transactionId from JournalEntry je where je.typeValue='4' and je.company.companyID=?) "
                        + "Order by gr.amountDueDate";
            } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                query = " FROM Invoice inv "
                        + "WHERE "
                        + "inv.company.companyID=? and inv.deleted=false "
                        + "and ((inv.invoiceamountdue='0' AND inv.isOpeningBalenceInvoice=false) OR (inv.openingBalanceAmountDue='0' AND inv.isOpeningBalenceInvoice=true)) "
                        + "and inv.ID not in (select transactionId from JournalEntry je where je.typeValue='4' and je.company.companyID=?) "
                        + "Order by inv.amountDueDate";
            }
            if (!StringUtil.isNullOrEmpty(query)) {
                list = executeQuery(query, new Object[]{companyid, companyid});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("error", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /*
     *Getting For Goods Receipts Order Details Term Map
    */
    @Override
    public KwlReturnObject getGRODetailsTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        List param = new ArrayList();
        try {
            String query = "from ReceiptOrderDetailTermMap ";
            String condition="";
            String orderby=" order by term.termSequence ";
            if(hm.containsKey("goodsReceiptOrderID") && hm.get("goodsReceiptOrderID") != null){
                String goodsReceiptOrderID = hm.get("goodsReceiptOrderID").toString();
                condition += " grodetail.ID = ? ";
                param.add(goodsReceiptOrderID);
            }
            if(hm.containsKey("termtype") && hm.get("termtype") != null){
                if(!StringUtil.isNullOrEmpty(condition)){
                    condition +=" and ";
                }
                condition += " term.termType = ? ";
                param.add(hm.get("termtype"));
            }
            if(!StringUtil.isNullOrEmpty(condition)){
                query += " where "+condition ;
            }
            if(hm.containsKey("orderbyadditionaltax") && hm.get("orderbyadditionaltax") != null){
                orderby += " , term.isAdditionalTax ";
            }
            orderby += " ASC ";
            query += orderby;
            list = executeQuery( query,param.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsreceiptsImpl.getGRODetailsTermMap" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
                

    public List getGoodsReceiptListForLinking(Map<String, Object> requestParams) throws ServiceException {
        String companyid = (String) requestParams.get(COMPANYID);
        ArrayList params = new ArrayList();
         List list = null;
        params.add(companyid);
        String vendorid = (String) requestParams.get(VENDORID);
        params.add(vendorid);
        String conditionQuery = "";
        DateFormat df = (DateFormat) requestParams.get(Constants.df);
        String expenseinv = (String) requestParams.get("onlyexpenseinv");
        try{
        if (requestParams.containsKey("isFixedAsset") && requestParams.get("isFixedAsset") != null) {
            conditionQuery += " and gr.isfixedassetinvoice= " + (((Boolean) requestParams.get("isFixedAsset")) ? "'1'" : "'0'");
}
        if (requestParams.containsKey("onlyMRPJOBWORKIN") && requestParams.get("onlyMRPJOBWORKIN") != null) {
            conditionQuery += " and gr.isMRPJobWorkIN= " + (((Boolean) requestParams.get("onlyMRPJOBWORKIN")) ? "'T'" : "'F'");
        }
        if (requestParams.containsKey(Constants.isDraft) && requestParams.get(Constants.isDraft) != null) {
            conditionQuery += " and gr.isDraft= " + (((Boolean) requestParams.get(Constants.isDraft)) ? "'T'" : "'F'");
        }
        if (!StringUtil.isNullOrEmpty(expenseinv)) {
            boolean isexpenseinv = Boolean.parseBoolean(expenseinv);
            conditionQuery += " and gr.isexpensetype=" + (isexpenseinv ? "'T'" : "'F'");
        }
        if (requestParams.containsKey("doflag") && (Boolean) requestParams.get("doflag")) {
            conditionQuery += " and gr.isopeningr='T' ";
        }
        if (requestParams.containsKey("ignoreCashPurchase") && (Boolean) requestParams.get("ignoreCashPurchase")) {
            conditionQuery += " and gr.cashtransaction != ? ";
            params.add(1);
        }
        String currencyfilterfortrans = (String) requestParams.get("currencyfilterfortrans");
        if (!StringUtil.isNullOrEmpty(currencyfilterfortrans) && !requestParams.containsKey("isReceipt")) {
            conditionQuery += " and gr.currency= ?";
            params.add(currencyfilterfortrans);
        }
        if (requestParams.containsKey("startdate") && requestParams.containsKey("enddate") && requestParams.get("startdate")!=null && requestParams.get("enddate")!=null) {
            conditionQuery += " and gr.creationdate >=? and  gr.creationdate<=?";
            params.add(df.parse((String)requestParams.get("startdate")));
            params.add(df.parse((String)requestParams.get("enddate")));
        }
        String query = "select gr.id,gr.grnumber,gr.creationdate from goodsreceipt gr where gr.company=? and gr.isOpeningBalenceInvoice=false and gr.isconsignment='F' and gr.vendor=? and gr.deleteflag='F' and gr.pendingapproval = 0 and gr.istemplate != 2 "+conditionQuery+"  order by grnumber";
        list = executeSQLQuery(query, params.toArray());
        
        } catch (ParseException ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
    /**
     * Function to get ITC reversal Invoices.
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public List isAllITCReversal(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        params.add(reqParams.optString("companyid"));
        StringBuilder condtion = new StringBuilder();
        if (reqParams.has("documentids")) {
            String documentids = reqParams.optString("documentids");
            if (documentids.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and gr.id in (");
                String typeArr[] = documentids.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(type);
                    } else {
                        conditionBuilder.append(",?");
                        params.add(type);
                    }
                }
                conditionBuilder.append(") ");
                condtion.append(conditionBuilder.toString());
            } else {
                condtion.append(" and gr.id=?");
                params.add(documentids);
            }

        }
        if (reqParams.has("itctype")) {
            condtion.append(" and grd.itctype =? ");
            params.add(reqParams.optInt("itctype"));
        }
        String query = " select gr.grnumber from goodsreceipt gr inner join grdetails grd on grd.goodsreceipt=gr.id where gr.company=? " + condtion + "  group by gr.id ";
        List list = executeSQLQuery(query, params.toArray());
        return list;
    }
    public List getGoodsReceiptDOLinkingList(Map<String, Object> requestParams) throws ServiceException {
        String companyid = (String) requestParams.get(COMPANYID);
        ArrayList params = new ArrayList();
        params.add(companyid);
        String vendorid = (String) requestParams.get(VENDORID);
        params.add(vendorid);
        String query = "select gr.id from goodsreceipt gr inner join grdetails grd on grd.goodsreceipt=gr.id inner join grodetails gro on gro.id=grd.grorderdetails where gr.company=? and gr.vendor=? ";
        List list = executeSQLQuery(query, params.toArray());
        return list;
    }
    @Override
    public KwlReturnObject getGoodsReceiptTDSPayment(String greceiptid, String companyid) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(companyid);
        params.add(greceiptid);
        String query = "SELECT ID FROM GoodsReceiptDetail grd WHERE grd.goodsReceipt.company.companyID=? AND grd.goodsReceipt.ID=? and ((grd.tdsPaidFlag<>0 AND grd.tdsPayment IS NOT NULL) OR (grd.tdsInterestPaidFlag<>0 AND grd.tdsInterestPayment IS NOT NULL)) ";
        List list = executeQuery( query,params.toArray());
        query = "SELECT ID FROM ExpenseGRDetail exgrd WHERE exgrd.goodsReceipt.company.companyID=? AND exgrd.goodsReceipt.ID=? and ((exgrd.tdsPaidFlag<>0 AND exgrd.tdsPayment IS NOT NULL) OR (exgrd.tdsInterestPaidFlag<>0 AND exgrd.tdsInterestPayment IS NOT NULL)) ";
        List listExpense = executeQuery( query,params.toArray());
        list.addAll(listExpense);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject listLinkedTDSPaymentDetail(String greceiptid, String companyid) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(greceiptid);
        params.add(greceiptid);
        params.add(greceiptid);
        params.add(greceiptid);
        String query = "SELECT pmt.*  from payment pmt  where id IN " +
        "(SELECT grd.tdspayment as id from goodsreceipt gr INNER JOIN grdetails grd on gr.id = grd.goodsreceipt WHERE gr.id=? and  grd.tdspaidflag<>0  AND grd.tdspayment IS NOT NULL " +
        "UNION " +
        "SELECT grd.tdsinterestpayment  as id from goodsreceipt gr INNER JOIN grdetails grd on gr.id = grd.goodsreceipt WHERE gr.id=? and  grd.tdsinterestpaidflag<>0  and grd.tdsinterestpayment IS NOT NULL " +
        "UNION " +
        "SELECT grd.tdspayment as id from goodsreceipt gr INNER JOIN expenseggrdetails grd on gr.id = grd.goodsreceipt WHERE gr.id=? and  grd.tdspaidflag<>0  AND grd.tdspayment IS NOT NULL " +
        "UNION " +
        "SELECT grd.tdsinterestpayment as id from goodsreceipt gr INNER JOIN expenseggrdetails grd on gr.id = grd.goodsreceipt WHERE gr.id=? and  grd.tdsinterestpayment<>0  AND grd.tdsinterestpayment IS NOT NULL " +
        ")";
        List list = executeSQLQuery( query,params.toArray());   
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getGoodsReceiptDetailsTDSForJE(HashMap<String,Object> hm) throws ServiceException {
        List list = null;
        try {
            ArrayList params = new ArrayList();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            params.add(hm.get("natureofPayment"));
            params.add(sdf.parse(hm.get("activeFromDate").toString()));
            params.add(hm.get("billdateObj"));
            params.add(hm.get("companyid").toString());
            params.add(hm.get("vendorID").toString());
            params.add(false);
            String query = "from GoodsReceiptDetail grd WHERE grd.tdsAssessableAmount<>0 AND grd.natureOfPayment.ID=? AND grd.tdsLineAmount=0 "
                    + " AND grd.goodsReceipt.journalEntry.entryDate >=? AND  grd.goodsReceipt.journalEntry.entryDate<=? AND grd.goodsReceipt.company.companyID=? AND grd.goodsReceipt.vendor.ID=? AND grd.tdsAssessableAmount>0 AND grd.tdsJEMapping IS NULL AND grd.goodsReceipt.deleted=?";
            list = executeQuery( query,params.toArray());
        } catch (ParseException ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
            return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getExpenseReceiptDetailsTDSForJE(HashMap<String,Object> hm) throws ServiceException {
        List list = null;
        try {
            ArrayList params = new ArrayList();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            params.add(hm.get("natureofPayment"));
            params.add(sdf.parse(hm.get("activeFromDate").toString()));
            params.add(hm.get("billdateObj"));
            params.add(hm.get("companyid").toString());
            params.add(hm.get("vendorID").toString());
            params.add(false);
            String query = "from ExpenseGRDetail erd WHERE erd.tdsAssessableAmount<>0 AND erd.natureOfPayment.ID=? AND erd.tdsLineAmount=0 "
                    + " AND erd.goodsReceipt.journalEntry.entryDate >=? AND  erd.goodsReceipt.journalEntry.entryDate<=? AND erd.goodsReceipt.company.companyID=? AND erd.goodsReceipt.vendor.ID=? AND erd.tdsAssessableAmount>0 AND erd.tdsJEMapping IS NULL AND erd.goodsReceipt.deleted=?";
            list = executeQuery( query,params.toArray());
        } catch (ParseException ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public TdsJEMapping saveTDSJEMapping(JournalEntry je, HashMap<String, Object> paramJobj, double rate, double tdsLineAmount,GoodsReceiptDetail grd, ExpenseGRDetail erd) {
        TdsJEMapping tdsJE = new TdsJEMapping();
        try {
            Company company = (Company) paramJobj.get("company");
            String uuid = UUID.randomUUID().toString();
            tdsJE.setID(uuid);
            tdsJE.setCompany(company);
            tdsJE.setJournalEntry(je);
            tdsJE.setTdsRate(rate);
            tdsJE.setTdsLineAmount(tdsLineAmount);
            save(tdsJE);
            if (erd != null) {
                erd.setTdsJEMapping(tdsJE);
            }
            if (grd != null) {
                grd.setTdsJEMapping(tdsJE);
            }
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        } finally {
            return tdsJE;
        }
    }
    public HashSet saveUpdateAdvancePaymentMapping(JSONObject jobjAppliedTDS, GoodsReceiptDetail grd,ExpenseGRDetail erd, Company company) {
        HashSet advancepaymentmapping = new HashSet();
        try {
            JSONArray advancePaymentDetailsJArr = new JSONArray(jobjAppliedTDS.optString("advancePaymentDetails"));
            for (int advPay = 0; advPay < advancePaymentDetailsJArr.length(); advPay++) {
                JSONObject jobjAdvPaymentTDS = advancePaymentDetailsJArr.getJSONObject(advPay);
                GoodsReceiptDetailPaymentMapping grpm = new GoodsReceiptDetailPaymentMapping();
                grpm.setID(StringUtil.generateUUID());
                grpm.setPayment(jobjAdvPaymentTDS.getString("goodsReceiptDetailsAdvancePaymentId"));
                if(grd!=null){
                    grpm.setGrdetails(grd);
                }else{
                    grpm.setErdetails(erd);
                }
                grpm.setPaymentAmount(jobjAdvPaymentTDS.getDouble("paymentamount"));
                grpm.setAdvanceAdjustedAmount(jobjAdvPaymentTDS.getDouble("adjustedAdvanceTDSamount"));
                grpm.setCompany(company);
                advancepaymentmapping.add(grpm);
            }
        } catch (Exception ex) {
             Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            return advancepaymentmapping;
        }
    }
    public KwlReturnObject deleteTDSAdvancePaymentMapping(String receiptid, String companyid, boolean isExpenseInv) throws ServiceException, AccountingException {
        //Delete Goods Receipt Details
        try {
            int numRows =0;
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(receiptid);
            List list = null;
            if(isExpenseInv){
                String query = "SELECT ID FROM ExpenseGRDetail erd WHERE erd.goodsReceipt.company.companyID=? AND erd.goodsReceipt.ID=? ";
                list = executeQuery( query,params.toArray());
            }else{
                String query = "SELECT ID FROM GoodsReceiptDetail grd WHERE grd.goodsReceipt.company.companyID=? AND grd.goodsReceipt.ID=? ";
                list = executeQuery( query,params.toArray());
            }
            String receiptDetailsId="";
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    receiptDetailsId = list.get(i).toString();
                    if (isExpenseInv) {
                        String delerdQuery = "delete from GoodsReceiptDetailPaymentMapping where erdetails.ID=? and company.companyID=?";
                        numRows += executeUpdate(delerdQuery, new Object[]{receiptDetailsId, companyid});
                    } else {
                        String delgrdQuery = "delete from GoodsReceiptDetailPaymentMapping where grdetails.ID=? and company.companyID=?";
                        numRows += executeUpdate(delgrdQuery, new Object[]{receiptDetailsId, companyid});
                    }
                }
            }
            return new KwlReturnObject(true, "Goods Receipt Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw new AccountingException("Cannot Edit Vendor Invoice as it is already used in TDS Advance Payment mapping.", ex);//+ex.getMessage(), ex);
        }
    }
    
    /**
     * Method is used to get the list of GoodsReceiptOrder created using
     * inventory non-sale item.
     * @param companyID
     * @return 
     * @throws com.krawler.common.service.ServiceException 
     */
    @Override
    public List<String> getGoodsReceiptOrderWithNonSaleItem(String companyID) throws ServiceException {
        List<String> grOrders = new ArrayList<>();
//        String hqlQuery = "from GoodsReceiptOrder where company.companyID = ? ";
        String sqlQuery = "select distinct gro.id  from grorder gro inner join company c on c.companyid = gro.company "
                + "inner join grodetails grod on grod.grorder = gro.id "
                + "inner join product p on p.id = grod.product and p.company = c.companyid "
                + "where c.companyid = ? order by gro.gronumber ";
        grOrders = executeSQLQuery(sqlQuery, new Object[]{companyID});
        return grOrders;
    }
    
     /**
     * Setting the Landed Invoice mapping with JE Detail to Null
     * @param gr
     * @return
     */
    @Override
    public KwlReturnObject setLandedInvoiceJEDMappingToNULL(String grid , String companyid) {
        List list = new ArrayList();
        try {
            String query1 ="update expenseggrdetails egrd inner join goodsreceipt gr on gr.id = egrd.goodsreceipt set egrd.landedinvoicejedid = NULL where gr.id = ? and gr.company = ?";
            executeSQLUpdate(query1, new Object[]{grid, companyid});
            String query2 = "update goodsreceipt set landedinvoiceje = NULL where id = ? and company = ?";
            executeSQLUpdate(query2, new Object[]{grid, companyid});
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    /**
     * Save mapping between GoodsReceiptDetail and GoodsReceipt
     * It is to be used when Landing Cost Category is activated at company level.
     * @param json
     * @return
     */
    @Override
    public KwlReturnObject saveLandingCostDetailMapping(JSONObject json) {
        List list = new ArrayList();
        try {
            LandingCostDetailMapping landingCostDetailMapping;
            if (json.has(LandingCostDetailMapping.LANDING_COST_DETAIL_MAPPING_ID)) {
                landingCostDetailMapping = (LandingCostDetailMapping) get(LandingCostDetailMapping.class, json.optString(LandingCostDetailMapping.LANDING_COST_DETAIL_MAPPING_ID));
            } else {
                landingCostDetailMapping = new LandingCostDetailMapping();
            }
            if (json.has(LandingCostDetailMapping.LANDING_COST)) {
                landingCostDetailMapping.setAmount(json.optDouble(LandingCostDetailMapping.LANDING_COST));
            }
            if (json.has(LandingCostDetailMapping.GOODSRECEIPT_DETAIL_ID)) {
                landingCostDetailMapping.setGoodsReceiptDetail((GoodsReceiptDetail) get(GoodsReceiptDetail.class, json.optString(LandingCostDetailMapping.GOODSRECEIPT_DETAIL_ID)));
            }
            if (json.has(LandingCostDetailMapping.EXPENSE_INVOICE_ID)) {
                landingCostDetailMapping.setExpenseInvoice((GoodsReceipt) get(GoodsReceipt.class, json.optString(LandingCostDetailMapping.EXPENSE_INVOICE_ID)));
            }
            if (json.has(LandingCostDetailMapping.INVENTORY_JED)) {
                landingCostDetailMapping.setInventoryJED((JournalEntryDetail) get(JournalEntryDetail.class, json.optString(LandingCostDetailMapping.INVENTORY_JED)));
            }
            if (json.has(LandingCostDetailMapping.LANDING_CATEGORY_ID)) {
                landingCostDetailMapping.setLandingCostCategory((LandingCostCategory) get(LandingCostCategory.class, json.optString(LandingCostDetailMapping.LANDING_CATEGORY_ID)));
            }
            save(landingCostDetailMapping);
            list.add(landingCostDetailMapping);
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * Delete LandingCostDetailMapping entry by passing InvoiceID ,company id and Expense type flag.
     *
     * @param InvoiceID
     * @param companyid
     * @param isExpenseType
     * @return
     */
    @Override
    public KwlReturnObject deleteLandingCostDetailMapping(String InvoiceID, String companyid,boolean isExpenseType) {
        int numRows = 0;String delQuery="";
        List params = new ArrayList<>();
        params.add(InvoiceID);
        params.add(companyid);
        try {
            if (isExpenseType) { //for expensetype invoice there is no entry in grdetails hence different query 
                delQuery=" delete lcdm from landingcostdetailmapping lcdm "
                        + " INNER JOIN goodsreceipt gr ON lcdm.expenseinvoiceid=gr.id"
                        + " where gr.id=? and gr.company=? and gr.isexpensetype='T' ";
            } else {
                delQuery = "delete lcdm from landingcostdetailmapping lcdm "
                        + " INNER JOIN grdetails grd ON grd.id= lcdm.grdetailid "
                        + " INNER JOIN goodsreceipt gr ON gr.id=grd.goodsreceipt "
                        + " where gr.id=? and gr.company=?";
            }
            numRows = executeSQLUpdate(delQuery, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return new KwlReturnObject(true, "Deleted landing cost detail mapping successfully.", null, null, numRows);
    }
    
    /**
     * Get Goods Receipt Detail for a particular landed invoice category
     * @param goodsReceiptId
     * @param landingCategoryId
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getGoodsReceiptDetailForLandingCategory(String goodsReceiptId, String landingCategoryId) throws ServiceException {
        List list = new ArrayList();
        String selQuery = " select grd.id,grd.rate,inv.quantity,p.productweight,p.id as productid,gro.id as groid,grod.id as grodid from grdetails grd"
                + " inner join inventory inv on inv.id = grd.id"
                + " inner join product p on p.id = inv.product"
                + " inner join productid_landingcostcategoryid plc on plc.productid = p.id"
                + " inner join grodetails grod ON (grod.id = grd.grorderdetails or grod.videtails = grd.id) " 
                +"  inner join grorder gro ON gro.id = grod.grorder "
                + " where grd.goodsreceipt = ? and plc.lccategoryid = ? ";
        list = executeSQLQuery(selQuery, new Object[]{goodsReceiptId, landingCategoryId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * Get Receipt Term Map for a GR Details
     *
     * @param goodsReceiptDetailId
     * @return ReceiptTermsMap
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getReceiptTermMapFromGRDetail(String goodsReceiptDetailId) throws ServiceException {
        String query = "from ReceiptTermsMap where grdetails.ID = ?";
        List list = executeQuery(query, goodsReceiptDetailId);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    /**
     * Get Receipt Term Map from Tax id.
     * ERM-782
     * @param receiptTermsParams
     * @return ReceiptTermsMap
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getReceiptTermMapList(Map<String, Object> requestParams) throws ServiceException {
        ArrayList paramslist = new ArrayList();
        String Condition = "";
        String taxid = (String) requestParams.get("taxid");
        String companyid = (String) requestParams.get("companyid");
        paramslist.add(taxid);
        paramslist.add(companyid);
        if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
            Condition = " and rtm.goodsreceipt.creationDate >= ? and rtm.goodsreceipt.creationDate <= ?";
            paramslist.add(requestParams.get("startDate"));
            paramslist.add(requestParams.get("endDate"));
        }
        
        String ss = requestParams.containsKey("ss")?(String) requestParams.get("ss"):"";
        
        if (!StringUtil.isNullOrEmpty(ss)) {
            try {
                String[] searchcol = new String[]{"rtm.termtax.name", "rtm.goodsreceipt.vendor.name", "rtm.goodsreceipt.vendor.acccode", "rtm.goodsreceipt.journalEntry.entryNumber","rtm.goodsreceipt.goodsReceiptNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramslist, ss, 5);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                Condition += searchQuery;
            } catch (SQLException ex) {
                Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /*
         *For Advance search
         */
       String mySearchFilterString = "";
       String mySearchFilterString1 = "";
        if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
            mySearchFilterString = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString.contains("c.accjecustomdata")) {
                mySearchFilterString = mySearchFilterString.replaceAll("c.accjecustomdata", "rtm.goodsreceipt.journalEntry.accBillInvCustomData");
            }
        }
        if (requestParams.containsKey(Constants.fixedAssetsPurchaseInvoiceSearchJson) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.fixedAssetsPurchaseInvoiceSearchJson))) {
            requestParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.fixedAssetsPurchaseInvoiceSearchJson));
            requestParams.put(Constants.moduleid, Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId);
            mySearchFilterString1 = StringUtil.getMySearchFilterString(requestParams, paramslist);
            if (mySearchFilterString1.contains("c.accjecustomdata")) {
                mySearchFilterString1 = mySearchFilterString1.replaceAll("c.accjecustomdata", "rtm.goodsreceipt.journalEntry.accBillInvCustomData");
            }
            mySearchFilterString = StringUtil.combineTwoCustomSearchStrings(mySearchFilterString, mySearchFilterString1);
        }

        String query = "from ReceiptTermsMap rtm where rtm.termtax.ID =? and rtm.goodsreceipt.company.companyID =? " + Condition + mySearchFilterString;
        List list = executeQuery(query, paramslist.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    
    /**
     * Get the total of both Global and line level taxes in transaction currency for a goods receipt (Purchase Invoice).
     * @param grid
     * @param companyid
     * @return Map<String,Object>
     * @throws ServiceException
     */
    @Override
    public Map<String, Object> getGlobalandLineLevelTaxForGoodsReceipt(String grid, String companyid) throws ServiceException {
        Map<String, Object> taxmap = new HashMap<>();
        List<String> params = new ArrayList<>();
        List<String> resultlist = new ArrayList<>();
        params.add(grid);
        params.add(companyid);
        //first query to return global level tax
        StringBuilder taxquery = new StringBuilder("select gr.taxamount from goodsreceipt gr "
                + " INNER JOIN tax tx ON gr.tax = tx.id where gr.id=? and gr.company=? and tx.isinputcredit='F' ");   //get Taxes for which input credit is not available hence check for false 
        resultlist = executeSQLQuery(taxquery.toString(), params.toArray());
        taxmap.put("globalleveltax", resultlist.isEmpty()?0.0:resultlist.get(0));            
        
        //second query to return linelevel tax
        taxquery = new StringBuilder("select expgrd.id from goodsreceipt gr "
                + " INNER JOIN expenseggrdetails expgrd ON gr.id = expgrd.goodsreceipt "
                + " INNER JOIN tax tx ON expgrd.tax = tx.id where gr.id=? and gr.company=? and tx.isinputcredit='F'"); //get Taxes for which input credit is not available hence check for false
        resultlist = executeSQLQuery(taxquery.toString(), params.toArray());
        taxmap.put("lineleveltax",resultlist);           
        return taxmap;
    }
/**
 * Get the specific product details (inventory quantity/rate/currency rate(PI currency rate)) from a PI by passing invoiceid , productid , grodetailid and companyid
 * used mainly for landed cost feature 
 * @param invoiceid
 * @param productid
 * @param companyid
 * @return KwlReturnObject
 * @throws ServiceException 
 */    
    @Override
    public KwlReturnObject getProductDetailsFromGoodsReceipt(JSONObject reqparams) throws ServiceException {
        List<String> params = new ArrayList<>();
        List<Object> resultset;
        params.add(reqparams.optString("invoiceid", ""));
        params.add(reqparams.optString("productid", ""));
        params.add(reqparams.optString("grodetailid", ""));
        params.add(reqparams.optString("companyid", ""));
        String GRNPILinkingJoin = "";
        if (reqparams.optBoolean("isPITOGRNLinking", false)) { //check if flow is from PI ---> Auto GRN
            GRNPILinkingJoin = "INNER JOIN grodetails grod ON grod.videtails = grd.id";
        } else {
            GRNPILinkingJoin = "INNER JOIN grodetails grod ON grod.id = grd.grorderdetails";
        }
        String sqlquery = " select iv.quantity,grd.rate,je.externalcurrencyrate,grd.id from inventory iv "
                + " INNER JOIN grdetails grd ON grd.id = iv.id "
                + GRNPILinkingJoin
                + "   INNER JOIN goodsreceipt gr ON gr.id = grd.goodsreceipt "
                + " INNER JOIN journalentry je ON je.id = gr.journalentry "
                + " where grd.goodsreceipt=? and iv.product=? and grod.id= ? and grd.company=?";
        resultset = executeSQLQuery(sqlquery, params.toArray());
        return new KwlReturnObject(true, "", null, resultset, resultset.size());
    }
    
    /**
     * Get the Purchase Invoice IDs linked to the current GRN by passing the GRN id from the flow GRN->PI .
     * @param grorderid
     * @param companyid
     * @return
     * @throws ServiceException
     */
    @Override
    public List<Object> getGRIDfromGROID(String grorderid, String companyid) throws ServiceException {
        List<String> params = new ArrayList<>();
        List<Object> resultset;
        params.add(grorderid);
        params.add(companyid);
        String query = "SELECT gr.id from grdetails grd "
                + " INNER JOIN  grodetails grod on (grd.grorderdetails = grod.id)"
                + " INNER JOIN goodsreceipt gr ON grd.goodsreceipt = gr.id "
                + " WHERE grod.grorder = ? and grod.company=? GROUP BY gr.id";
        resultset = executeSQLQuery(query, params.toArray());
        return resultset;
    }
    
/**
 * Get all expense invoices which are used in landed cost transactions by passing companyid
 * @param companyid
 * @return
 * @throws ServiceException 
 */
    @Override
    public KwlReturnObject getAllLandedInvoices(String companyid) throws ServiceException {
        boolean result = false;
        List list = new ArrayList();
        try {
            String query = "From GoodsReceipt where landedInvoiceJE IS NOT NULL and company.companyID = ? and isExpenseType=true and deleted = false";
            list = executeQuery(query, new Object[]{companyid});
            if (list.size() > 0) {
                result = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new KwlReturnObject(result, "", null, list, list.size());
    }
    
    public KwlReturnObject getExternalCurrencyRateForGoodsReceipt (String grId, String companyId) throws ServiceException {
        List list = new ArrayList();

        List<String> params = new ArrayList<>();

        String Query = "select je.externalcurrencyrate from goodsreceipt gr\n"
                + "inner join journalentry je on je.id = gr.journalentry\n"
                + "where gr.id =? and gr.company = ?";

        params.add(grId);
        params.add(companyId);

        list = executeSQLQuery(Query, params.toArray());

        return new KwlReturnObject(true, " success ", null, list, list.size());
    }
    /**
     * This function provides comma separated expense invoice numbers for a purchase invoice with landed cost.
     * @param invoiceid
     * @param companyid
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject getExpenseInvoiceNumbersFromGoodsReceipt(String invoiceid, String companyid) throws ServiceException {
        String query = "select GROUP_CONCAT(DISTINCT CONCAT('', grnumber, '')) AS combined_grnumber "
                + "from goodsreceipt where id IN (select goodsreceiptid from goodsreceiptid_landedInvoice "
                + "where landedinvoice=? and company=?)";
        List list= executeSQLQuery(query, new Object[]{invoiceid,companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    /**
     * ERP-39781
     * @desc checks if landed cost transactions are present in the system
     * @param params (companyid)
     * @return true (for Present) and false (for Not Present)
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject isLandedCostWithTermTransactionsPresent(JSONObject params) throws ServiceException {
        List<Object> queryParams = new ArrayList();
        Boolean isLandedCostTermsinJE = false;
        try {
            String defaultQuery = " Select rcm.id from receipttermsmap rcm INNER JOIN goodsreceipt gr "
                    + " ON gr.id = rcm.goodsreceipt WHERE gr.landedinvoiceje IS NOT NULL "
                    + " AND gr.isexpensetype='T'";
            String conditions = "";
            /**
             * Check if landed cost transactions are present for particular  company.
             */
            if (params.has(Constants.companyid)) {
                conditions += " and gr.company = ?";
                queryParams.add(params.optString(Constants.companyid));
            }
            String q = defaultQuery + conditions;
            List res = executeSQLQuery(q, queryParams.toArray());
            if (res.size() > 0) {
                isLandedCostTermsinJE = true;
            }
        } catch (Exception e) {
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return new KwlReturnObject(isLandedCostTermsinJE, "", "", null, 0);
    }

    @Override
    public JSONObject isGRNEditable(Company company, String store, String location, JSONObject detail) throws ServiceException, JSONException {
        String product=detail.optString("productid", "");
        String billId=detail.optString("billid", "");
        String batchname=detail.has("batchname")?detail.optString("batchname"):detail.optString("batch");
        double grnQuantity=0.0;
        Object fromDate=null;
        boolean isEditable = true;
        JSONObject obj = new JSONObject();
        String batch = null;
  
        String query = "select quantitydue from newproductbatch where company = ? AND product = ? AND location = ? AND warehouse = ?";
        
        ArrayList params = new ArrayList();
        params.add(company.getCompanyID());
        params.add(product);
        params.add(location);
        params.add(store);
        if(!StringUtil.isNullOrEmpty(batchname))
        {
            query = query+" AND batchname = ?";
            params.add(batchname);
        }
        
        List quantityList = executeSQLQuery(query, params.toArray());
        double quantityDue =(double) quantityList.get(0);
       
        params.clear();
        String query1 = "SELECT sm.createdon,sum(smd.quantity) FROM in_sm_detail smd INNER JOIN  in_stockmovement sm ON smd.stockmovement=sm.id "
                 +"WHERE sm.company=? AND sm.modulerefid=? AND sm.product=? AND sm.store=? AND smd.location=? ";
        params.add(company.getCompanyID());
        params.add(billId);
        params.add(product);
        params.add(store);
        params.add(location);
        if(!StringUtil.isNullOrEmpty(batchname))
        {
            query1 = query1+" AND smd.batchname = ?";
            params.add(batchname);
        }
        quantityList = executeSQLQuery(query1, params.toArray());
        if(quantityList!=null && quantityList.size()!=0){
            Object[] queryData=(Object [])quantityList.get(0);
            if(queryData[0]!=null && queryData[0]!=null){
                fromDate= queryData[0];
                grnQuantity=(double) queryData[1];
            }
            if(quantityDue < grnQuantity){
                isEditable=false;
                obj.put("isEditable", isEditable);
                obj.put("msg", "Quantity is not available for mentioned Batches.So it cannot be edited");
                return obj;
            }
        }
        
        String query2 = "select sum(case when transaction_type=2 then -smd.quantity else smd.quantity end) as sumquantity "
                + "from in_stockmovement sm inner join in_sm_detail smd on sm.id = smd.stockmovement where "
                + "sm.product=? AND sm.company=? and sm.createdon < ? and sm.store =? and smd.location =?"; 
        ArrayList params1 = new ArrayList();
        params1.add(product);
        params1.add(company.getCompanyID());
        params1.add(fromDate);
        params1.add(store);
        params1.add(location);
        if(!StringUtil.isNullOrEmpty(batchname))
        {
            query2 = query2+" and smd.batchname = ?";
            params1.add(batchname);
        }

        List listQuantity = executeSQLQuery(query2, params1.toArray());
        Double quantity = 0.0;
        Iterator itrQuantity = listQuantity.iterator();
        while (itrQuantity.hasNext()) {
            quantity =(Double) itrQuantity.next();
            quantity=quantity!=null?quantity:0;
        }
        double balance = quantity;

        String query3 = "select sm.transaction_type, sm.quantity,sm.product "
                + "from in_stockmovement sm inner join in_sm_detail smd on sm.id = smd.stockmovement where "
                + "sm.product=? AND sm.company=? and sm.createdon > ? and sm.store =? and smd.location =?";
        if(!StringUtil.isNullOrEmpty(batchname))
        {
            query3 = query3+" and smd.batchname = ?";
        }
        List listAllTransaction = executeSQLQuery(query3, params1.toArray());
        Iterator itrTransactionQuantity = listAllTransaction.iterator();
        while (itrTransactionQuantity.hasNext()) {
            Object[] objArr = (Object[]) itrTransactionQuantity.next();
            if ((int) objArr[0] == 2) {
                balance = balance - (Double) objArr[1];
            } else {
                balance = balance + (Double) objArr[1];
            }
            if (balance < 0) {
                isEditable = false;
                break;
            }
        }
           
        obj.put("isEditable", isEditable);
        obj.put("msg", !isEditable?"Transactions  Done for mentioned Batches.So it cannot be edited":"");
        return obj;
    }
        
    @Override
    public KwlReturnObject getLandingCostDetailMapping(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        if (requestParams.containsKey("grdetailid") && !StringUtil.isNullOrEmpty((String) requestParams.get("grdetailid"))) {
            params.add(requestParams.get("grdetailid"));
            String sqlQuery = "Select id,expenseinvoiceid,amount,inventoryjedid from landingcostdetailmapping where grdetailid=? ";
            list = executeSQLQuery(sqlQuery, params.toArray());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
}

