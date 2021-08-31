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
package com.krawler.spring.accounting.invoice;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.CommonIndonesianNumberToWords;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
//import com.sun.jmx.remote.internal.ArrayQueue; No need to Import.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import static com.krawler.common.util.StringUtil.replaceSearchquery;
import com.krawler.common.util.URLUtil;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteController;
import com.krawler.spring.accounting.creditnote.dm.CreditNoteInfo;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignController;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.customer.accCustomerHandler;
import com.krawler.spring.accounting.debitnote.accDebitNoteController;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptControllerCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.CommonEnglishNumberToWords;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.product.accProductControllerCMN;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptController;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentController;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.ObjectNotFoundException;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.spring.writeOffInvoice.accWriteOffServiceDao;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.spring.accounting.account.accCusVenMapDAO;
import com.krawler.spring.mrp.jobwork.AccJobWorkDao;
import com.krawler.spring.mrp.jobwork.AccJobWorkDaoImpl;
import com.krawler.spring.mrp.jobwork.AccJobWorkService;
import java.io.File;
import java.io.FileInputStream;
 

/**
 *
 * @author krawler
 */
public class accInvoiceCMN {
    
    private accCreditNoteDAO accCreditNoteobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accAccountDAO accAccountDAOobj;
    private accTaxDAO accTaxObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accReceiptDAO accReceiptobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accGoodsReceiptCMN accGoodsReceiptCommon;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accProductDAO accProductObj;
    private accDebitNoteDAO accDebitNoteobj;
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private accWriteOffServiceDao accWriteOffServiceDao;
    private accMasterItemsDAO accMasterItemsDAOobj;

    private accSalesOrderDAO accSalesOrderDAOobj;
    private CommonEnglishNumberToWords EnglishNumberToWordsOjb = new CommonEnglishNumberToWords();
    private CommonIndonesianNumberToWords IndonesianNumberToWordsOjb = new CommonIndonesianNumberToWords();
    private fieldDataManager fieldDataManagercntrl;
    private fieldManagerDAO fieldManagerDAOobj;
    private MessageSource messageSource;
     private accCusVenMapDAO accCusVenMapDAOObj;
     private AccJobWorkDaoImpl  accJobWorkDaoImplObj;
    private AccJobWorkDao accJobWorkDaoObj;
    private AccJobWorkService accJobWorkServiceObj;


    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCommon) {
        this.accGoodsReceiptCommon = accGoodsReceiptCommon;
    }

    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }

    public void setAccCreditNoteDAO(accCreditNoteDAO accCreditNoteobj) {
        this.accCreditNoteobj = accCreditNoteobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccSalesOrderServiceDAO(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyDAOobj = accCurrencyobj;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setAccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccReceiptDAO(accReceiptDAO accReceiptobj) {
        this.accReceiptobj = accReceiptobj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }

    public void setAccWriteOffServiceDao(accWriteOffServiceDao accWriteOffServiceDao) {
        this.accWriteOffServiceDao = accWriteOffServiceDao;
    }
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    public void setAccCusVenMapDAOObj(accCusVenMapDAO accCusVenMapDAOObj) {
        this.accCusVenMapDAOObj = accCusVenMapDAOObj;
    }
    public void setAccJobWorkDaoObj(AccJobWorkDao accJobWorkDaoObj) {
        this.accJobWorkDaoObj = accJobWorkDaoObj;
    }

    public void setAccJobWorkServiceObj(AccJobWorkService accJobWorkServiceObj) {
        this.accJobWorkServiceObj = accJobWorkServiceObj;
    }
    public String getdefaultBatchJson(Product product, HttpServletRequest request, String documentid, double quantity) throws JSONException {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        if (product.getLocation() != null && !StringUtil.isNullOrEmpty(product.getLocation().getId())) {
            jobj.put("location", product.getLocation().getId());
        }
        if (product.getWarehouse() != null && !StringUtil.isNullOrEmpty(product.getWarehouse().getId())) {
            jobj.put("warehouse", product.getWarehouse().getId());
        }
        jobj.put("documentid", "");
        if (!StringUtil.isNullOrEmpty(product.getID())) {
            jobj.put("productid", product.getID());
        }
        jobj.put("quantity", quantity);
        jobj.put("purchasebatchid", "");
        jarr.put(jobj);
        return jarr.toString();
    }

    public String getBatchJson(ProductBatch productBatch, boolean isFixedAssetInvoice, HttpServletRequest request, boolean isbatch, boolean isBatchForProduct, boolean isserial, boolean isSerialForProduct) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jSONArray = new JSONArray();
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String purchasebatchid = "";
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        filter_names.add("batch.id");
        filter_params.add(productBatch.getId());

        filter_names.add("company.companyID");
        filter_params.add(sessionHandlerImpl.getCompanyid(request));

        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject kmsg = accCommonTablesDAO.getSerialForBatch(filterRequestParams);

        List list = kmsg.getEntityList();
        Iterator iter = list.iterator();
        int i = 1;
        while (iter.hasNext()) {
            BatchSerial batchSerial = (BatchSerial) iter.next();
            JSONObject obj = new JSONObject();
            if (i == 1) {
                obj.put("id", productBatch.getId());
                obj.put("batch", productBatch.getName());
                obj.put("batch", productBatch.getName());
                obj.put("batchname", productBatch.getName());
                obj.put("location", productBatch.getLocation().getId());
                obj.put("warehouse", productBatch.getWarehouse().getId());
                obj.put("mfgdate", productBatch.getMfgdate() != null ? authHandler.getDateOnlyFormat(request).format(productBatch.getMfgdate()) : "");
                obj.put("expdate", productBatch.getExpdate() != null ? authHandler.getDateOnlyFormat(request).format(productBatch.getExpdate()) : "");
                obj.put("quantity", productBatch.getQuantity());
                obj.put("balance", productBatch.getBalance());
                obj.put("balance", productBatch.getBalance());
                obj.put("asset", productBatch.getAsset());
                if (isFixedAssetInvoice) {
                    obj.put("purchasebatchid", productBatch.getId());
                } else {
                    obj.put("purchasebatchid", getPurchaseBatchid(productBatch.getId()));
                }
            } else {
                obj.put("id", "");
                obj.put("batch", "");
                obj.put("batchname", "");
                obj.put("location", "");
                obj.put("warehouse", "");
                obj.put("mfgdate", "");
                obj.put("expdate", "");
                obj.put("quantity", "");
                obj.put("balance", "");
                obj.put("purchasebatchid", "");
            }
            i++;
            obj.put("serialnoid", batchSerial.getId());
            obj.put("serialno", batchSerial.getName());
            obj.put("expstart", batchSerial.getExpfromdate() != null ? authHandler.getDateOnlyFormat(request).format(batchSerial.getExpfromdate()) : "");
            obj.put("expend", batchSerial.getExptodate() != null ? authHandler.getDateOnlyFormat(request).format(batchSerial.getExptodate()) : "");
            if (isFixedAssetInvoice) {
                obj.put("purchaseserialid", batchSerial.getId());
            } else {
                obj.put("purchaseserialid", getPurchaseSerialid(batchSerial.getId()));
            }
            jSONArray.put(obj);
        }
        if (isBatchForProduct && !isSerialForProduct) //only in batch case
        {
            JSONObject Jobj = new JSONObject();
            Jobj = getOnlyBatchDetail(productBatch, request);
            if (isFixedAssetInvoice) {
                purchasebatchid = productBatch.getId();
            } else {
                purchasebatchid = getPurchaseBatchid(productBatch.getId());
            }
            if (!StringUtil.isNullOrEmpty(purchasebatchid)) {
                Jobj.put("purchasebatchid", purchasebatchid);
            }
            jSONArray.put(Jobj);
        }

        return jSONArray.toString();
    }

    public JSONObject getOnlyBatchDetail(ProductBatch productBatch, HttpServletRequest request) throws JSONException, SessionExpiredException {

        JSONObject obj = new JSONObject();
        obj.put("id", productBatch.getId());
        obj.put("batch", productBatch.getName());
        obj.put("batchname", productBatch.getName());
        obj.put("location", productBatch.getLocation().getId());
        obj.put("warehouse", productBatch.getWarehouse().getId());
        obj.put("mfgdate", productBatch.getMfgdate() != null ? authHandler.getDateOnlyFormat(request).format(productBatch.getMfgdate()) : "");
        obj.put("expdate", productBatch.getExpdate() != null ? authHandler.getDateOnlyFormat(request).format(productBatch.getExpdate()) : "");
        obj.put("quantity", productBatch.getQuantity());
        obj.put("balance", productBatch.getBalance());
        obj.put("asset", productBatch.getAsset());
        obj.put("expstart", "");
        obj.put("expend", "");

        return obj;
    }

    public double getAmountDue(HashMap<String, Object> requestParams, Invoice invoice) throws ServiceException {
        double amountdue = 0;
        double amount = 0, ramount = 0, contraamount = 0, termAmount = 0;
        String currencyid = (String) requestParams.get("gcurrencyid");
        String companyid = (String) requestParams.get("companyid");
        String currencyFilterForTrans = "";
        if (requestParams.containsKey("currencyfilterfortrans")) {
            currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
        }
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID = baseCurrency.getCurrencyID();
        HashMap hm = accInvoiceServiceDAO.applyCreditNotes(requestParams, invoice);
        Iterator itrCn = hm.values().iterator();
        
        currencyid = (invoice.getCurrency() == null ? baseCurrencyID : invoice.getCurrency().getCurrencyID());
        while (itrCn.hasNext()) {
            Object[] temp = (Object[]) itrCn.next();
            amount += (Double) temp[0] - (Double) temp[2];
        }
        JournalEntryDetail tempd = invoice.getTaxEntry();
        tempd = invoice.getTaxEntry();
        if (tempd != null) {
            amount += tempd.getAmount();
        }
        HashMap<String, Object> receiptMap = new HashMap<String, Object>();

        receiptMap.put("invoiceid", invoice.getID());
        receiptMap.put("companyid", companyid);
        KwlReturnObject result = accReceiptobj.getReceiptFromInvoice(receiptMap);
        List l = result.getEntityList();
        Iterator recitr = l.iterator();
        while (recitr.hasNext()) {
            ReceiptDetail rd = (ReceiptDetail) recitr.next();
            double rowAmount = (authHandler.round(rd.getAmount(), companyid));
            Date receiptEntryDate = null;
            double externalCurrencyRate = 0d;
            boolean isopeningBalanceReceipt = rd.getReceipt().isIsOpeningBalenceReceipt();
            receiptEntryDate = rd.getReceipt().getCreationDate();
            if (isopeningBalanceReceipt && !rd.getReceipt().isNormalReceipt()) {
                externalCurrencyRate = rd.getReceipt().getExchangeRateForOpeningTransaction();
            } else {
//                receiptEntryDate = rd.getReceipt().getJournalEntry().getEntryDate();
                externalCurrencyRate = rd.getReceipt().getJournalEntry().getExternalCurrencyRate();
            }

//            ramount+=rd.getAmount();
//            String fromcurrencyid=(rd.getReceipt().getCurrency()==null?baseCurrencyID:rd.getReceipt().getCurrency().getCurrencyID());
////                   ramount=CompanyHandler.getOneCurrencyToOther(session,request,ramount,fromcurrencyid,currencyid,invoice.getJournalEntry().getEntryDate());
//            KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, ramount, fromcurrencyid, currencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
//            ramount = (Double) bAmt.getEntityList().get(0);
            if (rd.getFromCurrency() != null && rd.getToCurrency() != null) {
//                    String fromcurrencyid = (rd.getReceipt().getCurrency() == null ? baseCurrencyID : rd.getReceipt().getCurrency().getCurrencyID());
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, rd.getAmount(), fromcurrencyid, currencyid, invoice.getJournalEntry().getEntryDate(),invoice.getJournalEntry().getExternalCurrencyRate());
//                    ramount = (Double) bAmt.getEntityList().get(0);                 
                ramount += (authHandler.round(rowAmount / rd.getExchangeRateForTransaction(), companyid));
            } else {
//                    ramount += rd.getAmount();
                String fromcurrencyid = (rd.getReceipt().getCurrency() == null ? baseCurrencyID : rd.getReceipt().getCurrency().getCurrencyID());
                KwlReturnObject bAmt = null;
                if (isopeningBalanceReceipt && rd.getReceipt().isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(requestParams, rowAmount, fromcurrencyid, currencyid, receiptEntryDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, rowAmount, fromcurrencyid, currencyid, receiptEntryDate, externalCurrencyRate);
                }
                ramount += (authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
            }
        }

        // Get amount from Invoice Terms 
        HashMap<String, Object> requestParam = new HashMap();
        requestParam.put("invoiceid", invoice.getID());
        curresult = accInvoiceDAOobj.getInvoiceTermMap(requestParam);
        List<InvoiceTermsMap> termMap = curresult.getEntityList();
        for (InvoiceTermsMap invoiceTerMap : termMap) {
            InvoiceTermsSales mt = invoiceTerMap.getTerm();
            termAmount += invoiceTerMap.getTermamount();
        }

        result = accReceiptobj.getContraPaymentFromInvoice(invoice.getID(), companyid);
        l = result.getEntityList();
        recitr = l.iterator();
        while (recitr.hasNext()) {
            PaymentDetail rd = (PaymentDetail) recitr.next();
            contraamount += rd.getAmount();
            String fromcurrencyid = (rd.getPayment().getCurrency() == null ? baseCurrencyID : rd.getPayment().getCurrency().getCurrencyID());
//                   ramount=CompanyHandler.getOneCurrencyToOther(session,request,ramount,fromcurrencyid,currencyid,invoice.getJournalEntry().getEntryDate());
//            KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, contraamount, fromcurrencyid, currencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
            KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, contraamount, fromcurrencyid, currencyid, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
            contraamount = (Double) bAmt.getEntityList().get(0);
        }

        double amountDueOriginal = 0;
        amountdue = amount - ramount - contraamount + termAmount;
        if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {//amount due difference for revaluation.
            amountDueOriginal = amountdue;
            if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
//                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                amountdue = (Double) bAmt.getEntityList().get(0);
            }
        } else {
            amount = 0;
            amountdue = 0;
        }
        if (requestParams.containsKey("amountDueOriginalFlag")) {
            return amountDueOriginal;
        } else {
            return amountdue;
        }
    }

    public double getBillingAmountDue(HashMap<String, Object> requestParams, BillingInvoice invoice) throws ServiceException {
        double amountdue = 0;
        double amount = 0, ramount = 0;
        String currencyid = (String) requestParams.get("gcurrencyid");
        String companyid = (String) requestParams.get("companyid");
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID = baseCurrency.getCurrencyID();
        Iterator itrCn = applyBillingCreditNotes(requestParams, invoice).values().iterator();
        currencyid = (invoice.getCurrency() == null ? baseCurrencyID : invoice.getCurrency().getCurrencyID());
        while (itrCn.hasNext()) {
            Object[] temp = (Object[]) itrCn.next();
            amount += (Double) temp[0] - (Double) temp[2];
        }
        JournalEntryDetail tempd = invoice.getTaxEntry();
        tempd = invoice.getTaxEntry();
        if (tempd != null) {
            amount += tempd.getAmount();
        }
        KwlReturnObject result = accReceiptobj.getBReceiptFromBInvoice(invoice.getID(), companyid);
        List l = result.getEntityList();
        Iterator recitr = l.iterator();
        while (recitr.hasNext()) {
            BillingReceiptDetail rd = (BillingReceiptDetail) recitr.next();
            ramount += rd.getAmount();
            String fromcurrencyid = (rd.getBillingReceipt().getCurrency() == null ? baseCurrencyID : rd.getBillingReceipt().getCurrency().getCurrencyID());
//                   ramount=CompanyHandler.getOneCurrencyToOther(session,request,ramount,fromcurrencyid,currencyid,invoice.getJournalEntry().getEntryDate());
            KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, ramount, fromcurrencyid, currencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
            ramount = (Double) bAmt.getEntityList().get(0);
        }
        amountdue = amount - ramount;
        return amountdue;
    }

    public HashMap applyCreditNotes(HashMap requestParams, Invoice invoice, String transactionCurrencyId, KWLCurrency baseCurrency) throws ServiceException {

        HashMap hm = new HashMap();
        String companyid = (String) requestParams.get("companyid");
        Set<InvoiceDetail> invRows = invoice.getRows();
        double amount;
        double quantity;
        double withoutDTAmt = 0;
        for (InvoiceDetail temp : invRows) {//reqiured for invoice discount row wise division[PS]
            double quantityTemp = temp.getInventory().getQuantity();
            double rowAmount = temp.getRate() * quantityTemp;
            if (invoice.getInvoicetype() != null && invoice.getInvoicetype().equals(Constants.Acc_Retail_Invoice_Variable)) {
                rowAmount = temp.getRate() * quantityTemp;
                rowAmount = authHandler.round(rowAmount, companyid);
            }
            if (temp.getPartamount() != 0.0) {
                rowAmount = rowAmount * (temp.getPartamount() / 100);
                rowAmount = authHandler.round(rowAmount, companyid);
            }
            withoutDTAmt += rowAmount;
        }
        for (InvoiceDetail temp : invRows) {
            //For the case of update inventory from DO
            double quantityTemp = temp.getInventory().getQuantity();
            quantity = quantityTemp;
            amount = temp.getRate() * quantity;
            amount = authHandler.round(amount, companyid);
            if (temp.getPartamount() != 0.0) {
                amount = amount * (temp.getPartamount() / 100);
            }
            double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
            double rowTaxPercent = 0;
            double rowTaxAmount = 0;
            boolean isRowTaxApplicable = false;
            double rowWithDTAmt = 0; // amount with discount & tax 
            if (temp.getTax() != null) {
//                KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), invoice.getJournalEntry().getEntryDate(), temp.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), invoice.getCreationDate(), temp.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
                isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                if (isRowTaxApplicable) {
                    rowTaxAmount = temp.getRowTaxAmount()+temp.getRowTermTaxAmount();
                }
            }
            double rowWithDAmt = amount - rdisc;// amount with discount
            if (temp.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. - DATE - 28 -Jan-2014
                rowWithDTAmt = rowWithDAmt + rowTaxAmount;
            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                rowWithDTAmt = rowWithDAmt + (rowWithDAmt * rowTaxPercent) / 100;
            }

            rowWithDTAmt = authHandler.round(rowWithDTAmt, companyid);
            double invoiceDisc = temp.getInvoice().getDiscount() == null ? 0 : accInvoiceServiceDAO.applyInvDisount(temp, withoutDTAmt);
            rowWithDTAmt -= invoiceDisc;
            hm.put(temp, new Object[]{rowWithDTAmt, quantity, 0.0, rowWithDAmt - invoiceDisc, 0.0, rowWithDAmt});
            if (invoice == null) {
                invoice = temp.getInvoice();
            }
        }

        /* TODO - Not In Use accCreditNoteobj.getCNRowsDiscountFromInvoice(). To Check record is exist on any database
        
         select distinct(cmp.subdomain)  from creditnote cn inner join company cmp on cmp.companyid = cn.company 
         left outer join cndetails cnd on cn.id=cnd.creditnote  left outer join cndiscount discounts2_ on cn.id=discounts2_.creditnote, invoicedetails invd  
         where cnd.invoiceRow=invd.id and cn.deleteflag='F' and (invd.invoice in (select id from invoice) or discounts2_.invoice in (select id from invoice))
        
         */
        KwlReturnObject result = accCreditNoteobj.getCNRowsDiscountFromInvoice(invoice.getID());
        List<Object[]> list = result.getEntityList();
//        Iterator cnitr = list.iterator();
        double cnTaxAmount = 0, discountAmount = 0, deductDiscount = 0;
        for (Object[] cnrow : list) {
            cnTaxAmount = 0;
            discountAmount = 0;
            deductDiscount = 0;
//            Object[] cnrow = (Object[]) cnitr.next();
            CreditNoteDetail cnr = (CreditNoteDetail) cnrow[1];
            InvoiceDetail temp = cnr.getInvoiceRow();
            if (!hm.containsKey(temp)) {//Discard CN rows which are for Otherwise CN and paid invoice detail and invoice rows for which no credit note has been applied.
                continue;
            }
            Object[] val = (Object[]) hm.get(temp);
            String fromcurrencyid = (cnr.getCreditNote().getCurrency() == null ? baseCurrency.getCurrencyID() : cnr.getCreditNote().getCurrency().getCurrencyID());
            String tocurrencyid = transactionCurrencyId;
//                double v=(Double)val[0]-(cnr.getDiscount()==null?0:CompanyHandler.getOneCurrencyToOther(session,request,cnr.getDiscount().getDiscountValue(),fromcurrencyid,tocurrencyid,invoice.getJournalEntry().getEntryDate()));
            double baseDisount = 0;//Credit note amount
            if (cnr.getPaidinvflag() != 1) {//Ignore cn detail rows amount which are inserted in table for paid invoice row 
                if (cnr.getDiscount() != null) {
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, cnr.getDiscount().getDiscountValue(), fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, cnr.getDiscount().getDiscountValue(), fromcurrencyid, tocurrencyid, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    baseDisount = (Double) bAmt.getEntityList().get(0);
                }
            }
            double v = (Double) val[0] - (cnr.getDiscount() == null ? 0 : baseDisount);//Amount = Total Invoice Amount(Including tax and dicount) - Credit note amount
            if (cnr.getTaxAmount() != null) {
                cnTaxAmount = cnr.getTaxAmount() + (Double) val[2];//Credit note tax. val[2] = 0;
            }
            double q = (Double) val[1]; //Product quantity from invoice
            if (temp.getInventory() != null) {
                q -= cnr.getQuantity(); //New product quantity after subtracting credit note quantity.
            }
            if (cnr.getTotalDiscount() != null) {
                discountAmount = cnr.getTotalDiscount();//Credit note discount amount
                deductDiscount = (Double) val[4] + cnr.getTotalDiscount();//val[4] = 0;  Same Credit note discount amount
            }
            hm.put(temp, new Object[]{v, q, cnTaxAmount - discountAmount, val[3], deductDiscount, val[5]});//formula for val[3]:(rate*quantity)-rowdiscount-invdiscount-cnamount[PS]
        }
        return hm;
    }
    public JSONArray getInvoiceDetailsItemJSON(JSONObject requestObj, String invid, HashMap<String, Object> paramMap) {
        JSONArray jArr = new JSONArray();
        JSONObject summaryData = new JSONObject();
        PdfTemplateConfig config = null;
        String allTerms = "",IFSCCode = "", accountNumber = "";
        StringBuilder appendtermString = new StringBuilder();
        String currencyid = "", globallevelcustomfields = "", globalleveldimensions = "", customerTitle = "";
        String billAddr = "", shipAddr = "", mainTaxName = "", createdby = "", updatedby = "";
        boolean isLocationForProduct = false, isbatchforproduct = false, isserialforproduct = false, isWarehouseForProduct = false, isPartialInvoice = false;
        String DOref = "", DOdate = "", SOref = "", QouteRef = "",allLineLevelTax="", allLineLevelTaxAmount = "", allLineLevelTaxBasic = "", lineLevelTaxSign = "", uomForTotalQuantity = "";
        Date SalesOrderDate = null;
        Date invoiceCreationDate = null;
        Date invoiceUpdationDate = null, transactionDate = null;
        Tax mainTax = null;
        String jobIN = "",challanBalanceQtyString = "",challanConsumeQtyString= "", shipmentTrackingNo = "";
        String buyerexcRegNo="", buyetinNo="", buyerpanNo="", buyerRange="", buyerDivision="", buyerComrate="", ManuName="", ManuAddress="", ManuexcRegNo="", ManutinNo="", buyerServiceTaxRegNo="";
        String ManuRange="", ManuDivision="" , ManuComrate="" , createdOnWithTime="";
        double totaltax = 0, subtotal = 0, totalDiscount = 0, totalVat = 0.0,Amountwithoutterm = 0, totalQuantity = 0, TotalLineLevelTaxAmount = 0, roundingDiff = 0;;
        Map<String,Double> lineLevelTaxNames = new LinkedHashMap<String,Double>();
        int quantitydigitafterdecimal = 2, amountdigitafterdecimal = 2, unitpricedigitafterdecimal = 2, count = 0, exchangerateafterdecimal = 4;
        double revExchangeRate = 0.0,totalExcise = 0.0,totaleducationCess=0.0,totalHCess=0.0, subTotalBeforePartialPayment = 0.0, totalTaxAmountBeforePartialPayment = 0.0;
        /*
       * Declaration of Variables to fetch Line level taxes and its information like
       * Tax Code, Tax Amount etc.
       */
        double globalLevelExchangedRateTotalAmount = 0, globalLevelExchangedRateSubTotal = 0, globalLevelExchangedRateTotalTax = 0, globalLevelExchangedRateSubTotalwithDiscount = 0, globalLevelExchangedRateTermAmount = 0, subTotalWithDiscount = 0;
        List<String> lineLevelTaxesGST = new ArrayList<>();
        Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
        Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
        Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            HashMap<String, Integer> ProductLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.productLevelCustomFieldMap);
            
            String companyid = requestObj.optString(Constants.companyKey);
            String compids[] = Constants.Companyids_Chkl_And_Marubishi.split(",");
            boolean isFromChklorMarubishi = false;
            int countOfBatches = 0;
            for (int cnt = 0; cnt < compids.length; cnt++) {
                String compid = compids[cnt];
                if (compid.equalsIgnoreCase(companyid)) {
                    isFromChklorMarubishi = true;
                }
            }
            
            java.util.Date entryDate = null;
            double amountDue = 0d, paymentreceived = 0, totalAmount = 0,totalAmountForServiceTaxInvoice=0, swacchaBharatCessForServiceTaxInvoice=0,serviceTaxRateForServiceTaxInvoice=0,taxPercent = 0;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestObj);
            HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
            DateFormat df1 = authHandler.getDateOnlyFormat();
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(requestObj);//User Date Formatter
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("invoice.ID");
            order_by.add("srno");
            order_type.add("asc");
            invRequestParams.put("filter_names", filter_names);
            invRequestParams.put("filter_params", filter_params);
            invRequestParams.put("order_by", order_by);
            invRequestParams.put("order_type", order_type);
            KwlReturnObject result = kwlCommonTablesDAOObj.getObject(Invoice.class.getName(), invid);
            Invoice invoice = (Invoice) result.getEntityList().get(0);
            filter_params.clear();
            filter_params.add(invoice.getID());
            KwlReturnObject idresult = accInvoiceDAOobj.getInvoiceDetails(invRequestParams);
            List<InvoiceDetail> invoiceDetailList = idresult.getEntityList();
            //getting main company posttext
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, requestObj.optInt("moduleid"));
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            /**
             * get customer title (Mr./Mrs.)
             */
            customerTitle = invoice.getCustomer().getTitle();
            if(!StringUtil.isNullOrEmpty(customerTitle)){
                KwlReturnObject masterItemResult = kwlCommonTablesDAOObj.getObject(MasterItem.class.getName(), customerTitle);
                MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                customerTitle = masterItem.getValue();
            }
            isPartialInvoice = invoice.isPartialinv();
            currencyid = invoice.getCurrency().getCurrencyID();
            createdby = invoice.getCreatedby() != null ? invoice.getCreatedby().getFullName() : "";
            updatedby = invoice.getModifiedby() != null ? invoice.getModifiedby().getFullName() : "";
            mainTax = invoice.getTax();
            currencyid = invoice.getCurrency().getCurrencyID();
            
            //Document Currency
            summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, currencyid);
            
            double externalcurrency = invoice.getExternalCurrencyRate();
            double externalCurrencyRate = invoice.getExternalCurrencyRate();
            // Load Invoices in map 
            List<String> idsList = new ArrayList<String>();
            for (InvoiceDetail row : invoiceDetailList) {
                idsList.add(row.getID());
            }

            Map<String, JournalEntry> invoiceJEMap = accInvoiceDAOobj.getInvoiceJEList(idsList);
            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
            }

            JournalEntry je = null;
            if (invoice.isNormalInvoice() && invoiceJEMap.containsKey(invid)) {
                je = invoiceJEMap.get(invid);
                externalCurrencyRate = je.getExternalCurrencyRate();
            }

            if (externalCurrencyRate != 0) {
                revExchangeRate = 1 / externalCurrencyRate;
            }

            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[1] != null) {
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                }
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
                if (decimalcontact[3] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalcontact[3].toString());
                }
            }
            invoiceCreationDate = new Date(invoice.getCreatedon());
            invoiceUpdationDate = new Date(invoice.getUpdatedon());
            String invoiceCreationDateStr = invoiceCreationDate!= null?IndiaComplianceConstants.INDIAN_TEMPLATE_DATE_FORMATTER.format(invoiceCreationDate):"";
            String invoiceUpdationDateStr = invoiceUpdationDate!= null?IndiaComplianceConstants.INDIAN_TEMPLATE_DATE_FORMATTER.format(invoiceUpdationDate):"";
            
            KwlReturnObject pref = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preference = (CompanyAccountPreferences) pref.getEntityList().get(0);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            boolean isMalaysian = extraCompanyPreferences != null ? extraCompanyPreferences.getCompany().getCountry().getID().equalsIgnoreCase("137") : false;
            // get Shipment Tracking No if UPS flow is on
            boolean isUpsIntegration = extraCompanyPreferences.isUpsIntegration();
            Set<String> deliveryOrderDetailIDs = new TreeSet<String>();
            if (isUpsIntegration) {
                KwlReturnObject linkedDOobj = accInvoiceDAOobj.getDOFromOrToInvoices(invoice.getID(), companyid);
                List linkedDOList = linkedDOobj.getEntityList();
                Iterator itr3 = linkedDOList.iterator();
                while (itr3.hasNext()) {
                    Object[] objArr = (Object[]) itr3.next();
                    DeliveryOrder dorder = (DeliveryOrder) objArr[0];
                    Set<DeliveryOrderDetail> doRows = dorder.getRows();
                    if (doRows != null && !doRows.isEmpty()) {
                        for (DeliveryOrderDetail temp : doRows) {
                            String deliveryOrderDetailID = temp.getID();
                            if (!StringUtil.isNullOrEmpty(deliveryOrderDetailID)) {
                                deliveryOrderDetailIDs.add(deliveryOrderDetailID);
                            }
                        }
                    }
                }
                StringBuilder upsTrackingNumbers = new StringBuilder("");
                Set<String> upsTrackingNumbersSet = new TreeSet<String>();
                upsTrackingNumbersSet = accInvoiceServiceDAO.getUPSTrackingNumberFromDoDetails(deliveryOrderDetailIDs);
                if (!upsTrackingNumbersSet.isEmpty()) {
                    String upsTrackingNumbersStr = upsTrackingNumbersSet.toString();
                    upsTrackingNumbers.append(upsTrackingNumbersStr.substring(1, (upsTrackingNumbersStr.length() - 1)));
                }
                shipmentTrackingNo = upsTrackingNumbers.toString().trim();
                shipmentTrackingNo = shipmentTrackingNo.replaceAll(",", "!##");// replace comma with !## for value seperator funtionality
            }
            int countryid = 0;
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            if( extraCompanyPreferences.getCompany()!= null && extraCompanyPreferences.getCompany().getCountry() != null ){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            if(countryid == Constants.indian_country_id && extraCompanyPreferences.getBankId()!= null ){
                KwlReturnObject accobj = accountingHandlerDAOobj.getObject(Account.class.getName(),  extraCompanyPreferences.getBankId());
                Account account= (Account) accobj.getEntityList().get(0);
                if(account != null && account.getIfsccode() != null){
                       IFSCCode = account.getIfsccode();
                }
                if(account != null && account.getAccountCode() != null ){
                    accountNumber = account.getAccountCode();
                }
            }
            
            List<PricingBandMasterDetail> list = CommonFunctions.getRRPFieldsForAllModulesLineItem(requestObj, accountingHandlerDAOobj, accMasterItemsDAOobj);
            int rowcnt = 0;
            Date jobworkorderdate = null;
            String jobworkorderno =  "";
            String jobworkorderdateStr = "";
            Set<String> quotationSet = new HashSet<String>();
            Set<String> salesOrderSet = new HashSet<String>();
            JSONObject detailsTableData = new JSONObject();
            JSONArray gstTaxSummaryDetailsTableDataArr = new JSONArray();
            for (InvoiceDetail row:invoiceDetailList) {
                rowcnt++;
                /*
                * Fetching Job Work Order Qty, Number and Date.
                */
                Inventory inv = row.getInventory();
                Product prod = inv.getProduct();
                String uom = row.getInventory().getUom() == null ? (row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()) : row.getInventory().getUom().getNameEmptyforNA();
                double jobWorkOrderQty = 0.0;
                String challanDate = "";
                JSONObject challanQtyDetails = new JSONObject();
                if (extraCompanyPreferences.isJobworkrecieverflow()) {
                    JSONObject jwJobj = new JSONObject();
                    
                    HashMap<String, Object> jwRequestParams = new HashMap<>();
                    jwRequestParams.put("quantitydigitafterdecimal", quantitydigitafterdecimal);
                    jwRequestParams.put("uom", uom);
                    /*
                     * This Function gives Json For Job Work Module
                     */
                    jwJobj = getJobWorkJson(requestObj, row, jwRequestParams);
                    challanDate = jwJobj.optString("challanDate", "");
                    jobIN = jwJobj.optString("jobIN", "");
                    jobWorkOrderQty = jwJobj.optDouble("jobWorkOrderQty", 0.0);
                    jobworkorderno = jwJobj.optString("jobworkorderno", "");
                    jobworkorderdateStr = jwJobj.optString("jobworkorderdate", "");
                    /*
                    * Challan NO and invoice specific balance quantity and consume quantity 
                    * are fetched into this URL.
                    */ 
                    challanQtyDetails = jwJobj.optJSONObject("challanQtyDetails");
                }
                JSONObject obj = new JSONObject();
                JSONObject gstTaxSummaryDetailsTableData = new JSONObject();
                Discount disc = row.getDiscount();
                String rowTaxName = "", proddesc = "", discountname = "0";
                double rate = 0, rowTaxPercent = 0, rowTaxAmt = 0, rowamountwithtax = 0, rowdiscountvalue = 0, rowamountwithouttax = 0;
                double exchangerateunitprice = 0, exchangeratelineitemsubtotal = 0, exchangeratelineitemdiscount = 0, exchangeratesubtotalwithoutdiscount = 0, exchangeratelineitemamount = 0, exchangeratelineitemtax = 0, exchangeratelineitemsubtotalwithdiscount = 0;

                proddesc = StringUtil.isNullOrEmpty(row.getDescription()) ? (StringUtil.isNullOrEmpty(inv.getProduct().getDescription()) ? "" : inv.getProduct().getDescription()) : row.getDescription();
                proddesc = StringUtil.DecodeText(proddesc);
                obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr No
                obj.put(CustomDesignerConstants.ProductName, prod.getName().replaceAll("\n", "<br>"));// productname
                obj.put(CustomDesignerConstants.IN_ProductCode, prod.getProductid() == null ? "" : prod.getProductid());
                obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>"));//Product Description
                obj.put(CustomDesignerConstants.HSCode, (prod.getHSCode() != null) ? prod.getHSCode().replaceAll("\n", "<br>") : "");//get HSCode of ProductGrid
                obj.put(CustomDesignerConstants.ProductBarcode, prod.getBarcode() == null ? "" : prod.getBarcode());//Product Bar Code
                obj.put(CustomDesignerConstants.AdditionalDescription, prod.getAdditionalDesc() != null ? prod.getAdditionalDesc().replaceAll("\n", "<br>") :"");  //product Addtional description
//                String uom = row.getInventory().getUom() == null ? (row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getName()) : row.getInventory().getUom().getName();
                uomForTotalQuantity = uom;
                //used to get the line level total amount
                for (PricingBandMasterDetail pricingBandMasterDetailObj : list) {
                    if (pricingBandMasterDetailObj.getProduct().equals(prod.getID())) {
                            obj.put(CustomDesignerConstants.RRP, authHandler.formattingDecimalForAmount(pricingBandMasterDetailObj.getSalesPrice(), companyid));//RRP
                    }
                }
                KwlReturnObject productCategories = null;
                productCategories = accProductObj.getProductCategoryForDetailsReport(prod.getID());
                List productCategoryList = productCategories.getEntityList();
                String cateogry = "";
                Iterator catIte = productCategoryList.iterator();
                while (catIte.hasNext()) {
                    ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                    String categoryName  = pcm.getProductCategory()!=null?(!StringUtil.isNullOrEmpty(pcm.getProductCategory().getValue().toString())?pcm.getProductCategory().getValue().toString():""):"";
                    cateogry += categoryName + " ";
                }
                if (StringUtil.isNullOrEmpty(cateogry)) {
                    cateogry = "None";
                }
                obj.put("productCategory", cateogry);
                obj.put("productType", prod.getProducttype().getName());

                HashMap<String, Object> params = new HashMap<String, Object>();
                filter_names = new ArrayList();
                filter_params = new ArrayList();
                filter_names.add("companyid");
                filter_names.add("fieldtype");
                filter_names.add("customcolumn");
                filter_names.add("moduleid");
                filter_params.add(companyid);
                filter_params.add(4);
                filter_params.add(1);
                filter_params.add(requestObj.optInt("moduleid"));
                params.put("filter_names", filter_names);
                params.put("filter_values", filter_params);
                
                KwlReturnObject fieldparams =  accAccountDAOobj.getFieldParams(params);
                List fieldParamsList = fieldparams.getEntityList();
                
                JSONArray jsonarr = new JSONArray();
                for(int cnt=0; cnt < fieldParamsList.size(); cnt++){
                    FieldParams fieldParamsObj = (FieldParams) fieldParamsList.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("comboName", fieldParamsObj.getFieldname());
                    jsonarr.put(tempObj);
                }
                
                obj.put("groupingComboList", jsonarr);
                
                double quantity = row.getInventory().getQuantity();
                rate = row.getRate();
                rate = authHandler.roundUnitPrice(rate, companyid);
                rowamountwithouttax = rate * quantity;
                totalQuantity += quantity;

                if(isPartialInvoice){
                    if(row.getPartamount() > 0){
                        rowamountwithouttax = (rowamountwithouttax * row.getPartamount()) / 100;
                    }
                    obj.put(CustomDesignerConstants.PartialAmount, authHandler.formattingDecimalForAmount(row.getPartamount(), companyid) + "%");//Partial Amount
                }
                
            /*
                 * In include GST case calculations are in reverse order
                 * In other cases calculations are in forward order
                 */
                double rowamountwithgst = 0;
                double discountBeforePartial = 0.0;
                double rowtaxamountincludeGST = 0.0;
                if (row.getInvoice().isGstIncluded()) {//if gstincluded is the case
                    rowamountwithgst = row.getRateincludegst() * quantity;
                    rowdiscountvalue = row.getDiscount() != null ? row.getDiscount().getDiscountValue() : 0;
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.round(rowamountwithgst - row.getRowTaxAmount(), companyid));//Subtotal
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount,authHandler.round((rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount()), companyid));//Subtotal
                    subtotal += authHandler.round(rowamountwithgst - row.getRowTaxAmount(), companyid);
                    subTotalWithDiscount = authHandler.round((rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount()), companyid);
                    /*
                     * In case of including GST Tax value is substacted from original amount that will amount of product
                     */
                    rowamountwithouttax = rowamountwithgst - row.getRowTaxAmount();
                    /**
                     * Calculate amount before deducting partial payment
                     */
                    if(isPartialInvoice){
                        double rowamtwithouttax = 0.0;
                        if(row.getTax() != null){
//                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), row.getTax().getID());
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getCreationDate(), row.getTax().getID());
                            double rowtaxpercent = (Double) perresult.getEntityList().get(0);
                            rowamtwithouttax = (rowamountwithgst * 100) / (100 + rowtaxpercent);
                            rowtaxamountincludeGST = rowamtwithouttax * rowtaxpercent / 100;
                        } else{
                            rowamtwithouttax = rowamountwithgst;
                        }
                        if(row.getDiscount() != null && row.getDiscount().isInPercent()){
                            discountBeforePartial = row.getDiscount() != null ? ((rowamountwithgst - row.getRowTaxAmount()) * row.getDiscount().getDiscount()) / 100 : 0.0;
                        } else{
                            discountBeforePartial = row.getDiscount() != null ? row.getDiscount().getDiscountValue() : 0.0;
                        }
                        obj.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, authHandler.formattingDecimalForAmount((rowamtwithouttax - discountBeforePartial), companyid));//Subtotal
                        subTotalBeforePartialPayment += authHandler.round(rowamtwithouttax - discountBeforePartial, companyid);
                    } else{
                        obj.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, authHandler.formattingDecimalForAmount((rowamountwithgst - (rowamountwithgst * rowdiscountvalue) / 100 - row.getRowTaxAmount()), companyid));//Subtotal
                        subTotalBeforePartialPayment += authHandler.round(rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount(), companyid);
                    }
                } else{
                    rowdiscountvalue = row.getDiscount() != null ? row.getDiscount().getDiscountValue() : 0;
                    if(isPartialInvoice && row.getDiscount() != null){
                        rowdiscountvalue = authHandler.round(row.getPartialDiscount(),companyid);
                    }
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.round(rowamountwithouttax, companyid));//Subtotal
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount,authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid));//Subtotal
                    subtotal += authHandler.round(rowamountwithouttax, companyid);//rounded becuase totaltax & amount are rounded and saved in db
                    subTotalWithDiscount = authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid);
                    /**
                     * Calculate amount before deducting partial payment
                     */
                    if(isPartialInvoice){
                        if(row.getDiscount() != null && row.getDiscount().isInPercent()){
                            discountBeforePartial = row.getDiscount() != null ? ((rate * quantity) * row.getDiscount().getDiscount()) / 100 : 0.0;
                        } else{
                            discountBeforePartial = row.getDiscount() != null ? row.getDiscount().getDiscountValue(): 0.0;
                        }
                        obj.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, authHandler.formattingDecimalForAmount((rate * quantity) - discountBeforePartial, companyid));//Subtotal
                        subTotalBeforePartialPayment += authHandler.round((rate * quantity) - discountBeforePartial, companyid);
                    } else{
                        obj.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, authHandler.formattingDecimalForAmount((rowamountwithouttax-rowdiscountvalue), companyid));//Subtotal
                        subTotalBeforePartialPayment += authHandler.round(rowamountwithouttax - rowdiscountvalue, companyid);
                    }
                }

                /*Discount Section*/
                Discount discount = row.getDiscount();
                if (discount != null) {
                    if (discount.isInPercent()) {
                        double discountpercent = row.getDiscount().getDiscount();
                        discountname = (long) discountpercent == discountpercent ? (long) discountpercent + "%" : discountpercent + "%"; // ERP-27882
                    } else {
                        discountname = invoice.getCurrency().getSymbol() + " " + authHandler.formattingDecimalForAmount(row.getDiscount().getDiscountValue(), companyid);//to show as it is in UI
                    }
                } else {
                    discountname = "0 %";
                }
                
                totalDiscount += rowdiscountvalue;
                obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                obj.put(CustomDesignerConstants.Discountname, discountname);// Discount
                obj.put(CustomDesignerConstants.QuantitywithUOM, authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom); // Quantity
                obj.put(CustomDesignerConstants.JobWorkChallanDate, challanDate);
                obj.put(CustomDesignerConstants.IN_Quantity, authHandler.formattingDecimalForQuantity(quantity, companyid));
                obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate
                obj.put(CustomDesignerConstants.RATEINCLUDINGGST, authHandler.formattingDecimalForUnitPrice(row.getRateincludegst(), companyid));// Rate Including GST
                obj.put(CustomDesignerConstants.IN_Currency, invoice.getCurrency().getCurrencyCode());
                obj.put(CustomDesignerConstants.IN_UOM, uom);
                obj.put(CustomDesignerConstants.PRODUCTNETWEIGHT, authHandler.formattingDecimalForAmount(prod.getProductweight(), companyid));//Product Weight
                obj.put(CustomDesignerConstants.PartNumber, StringUtil.isNullOrEmpty(prod.getCoilcraft()) ? "" : prod.getCoilcraft()); //Part Number
                obj.put(CustomDesignerConstants.SupplierPartNumber, StringUtil.isNullOrEmpty(prod.getSupplier()) ? "" : prod.getSupplier());
                obj.put(CustomDesignerConstants.CustomerPartNumber, StringUtil.isNullOrEmpty(prod.getInterplant()) ? "" : prod.getInterplant());
                obj.put("currencysymbol", invoice.getCurrency().getSymbol());//used for headercurrency & record currency
                obj.put("basecurrencysymbol", invoice.getCompany().getCurrency().getSymbol());//used for headercurrency & record currency
                obj.put("basecurrencycode", invoice.getCompany().getCurrency().getCurrencyCode());//used for headercurrency & record currency
                obj.put("isGstIncluded", invoice.isGstIncluded());//used for headercurrency & record currency
                obj.put("currencycode", invoice.getCurrency().getCurrencyCode());//used for headercurrencyCode & record currency
                String baseUrl = URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                /*
                 * Following code is to check whether the image is predent for product or not. 
                 * If Image is not present sent s.gif instead of product id
                 */
                String fileName = null;
                fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+prod.getID() + ".png";
                File file = new File(fileName);
                FileInputStream in = null; 
                String filePathString = "";
                try {
                    in = new FileInputStream(file);
                } catch(java.io.FileNotFoundException ex) {
                    //catched exception if file not found, and to continue for rest of the products
                   filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                if(in != null){
                    filePathString = baseUrl + "productimage?fname=" + prod.getID() + ".png&isDocumentDesignerPrint=true";
                } else{
                    filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                obj.put(CustomDesignerConstants.imageTag, filePathString);
                
                String lineLevelTax = "";
                String lineLevelTaxPercent = "";
                String lineLevelTaxAmount = "";
                double lineLevelTaxAmountTotal = 0;
                if(extraCompanyPreferences.isIsNewGST()){ // for New gst check 
                    HashMap<String, Object> InvoiceDetailParams = new HashMap<String, Object>();
                    InvoiceDetailParams.put("InvoiceDetailid", row.getID());
                    InvoiceDetailParams.put("orderbyadditionaltax",true);
                    // GST
                    InvoiceDetailParams.put("termtype", 7);
                    KwlReturnObject grdTermMapresult = accInvoiceDAOobj.getInvoicedetailTermMap(InvoiceDetailParams);
                    List<InvoiceDetailTermsMap> gst = grdTermMapresult.getEntityList();
                    obj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.CESSPERCENT, 0);
                    obj.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.TAXABLE_VALUE, subTotalWithDiscount);
                    
                    
                    for (InvoiceDetailTermsMap invoicedetailTermMap : gst) {
                            LineLevelTerms mt = invoicedetailTermMap.getTerm();
                        if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                            obj.put(CustomDesignerConstants.CGSTPERCENT, invoicedetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CGSTAMOUNT, invoicedetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, invoicedetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, invoicedetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                            obj.put(CustomDesignerConstants.IGSTPERCENT, invoicedetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.IGSTAMOUNT, invoicedetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, invoicedetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, invoicedetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                            obj.put(CustomDesignerConstants.SGSTPERCENT, invoicedetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.SGSTAMOUNT, invoicedetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, invoicedetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, invoicedetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                            obj.put(CustomDesignerConstants.UTGSTPERCENT, invoicedetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.UTGSTAMOUNT, invoicedetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, invoicedetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, invoicedetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                            obj.put(CustomDesignerConstants.CESSPERCENT, invoicedetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CESSAMOUNT, invoicedetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, invoicedetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, invoicedetailTermMap.getTermamount());
                        } 
                            lineLevelTax += mt.getTerm();
                            lineLevelTax += "!## ";
 
                            lineLevelTaxPercent += authHandler.formattingDecimalForAmount(invoicedetailTermMap.getPercentage(), companyid);
                            lineLevelTaxPercent += "!## ";
                            lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(invoicedetailTermMap.getTermamount(), amountdigitafterdecimal,countryid);
                            /*
                             * calculating total of line level taxes
                             */
                            lineLevelTaxAmountTotal += invoicedetailTermMap.getTermamount();
                            lineLevelTaxAmount += "!## ";
                            if(lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm())!=null){
                                double value = lineLevelTaxNames.get(mt.getTerm());
                                lineLevelTaxNames.put(mt.getTerm(),invoicedetailTermMap.getTermamount()+value );

                            } else{
                                lineLevelTaxNames.put(mt.getTerm(),invoicedetailTermMap.getTermamount());
                            }
                            if(mt.isIsAdditionalTax()){
                                String term = "";
                                if(mt.getTerm()!= null){
                                    term = mt.getTerm().toLowerCase();
                                }
                                if(term.contains(IndiaComplianceConstants.EDUCATION_CESS) && !term.contains(IndiaComplianceConstants.HCESS) &&  !term.contains(IndiaComplianceConstants.HIGHER_EDUCATION_CESS)){
                                    totaleducationCess += invoicedetailTermMap.getTermamount();
                                }
                                if(term.contains(IndiaComplianceConstants.HCESS) || term.contains(IndiaComplianceConstants.HIGHER_EDUCATION_CESS)){
                                    totalHCess += invoicedetailTermMap.getTermamount();
                                }
                            }
                    }
                    
                    if(!StringUtil.isNullOrEmpty(lineLevelTax)){
                        lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length()-4);
                    }
                    if(!StringUtil.isNullOrEmpty(lineLevelTaxPercent)){
                        lineLevelTaxPercent=lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length()-4);
                    }
                    if(!StringUtil.isNullOrEmpty(lineLevelTaxAmount)){
                        lineLevelTaxAmount= lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length()-4);
                    }
                    obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                    obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                    obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                    /*
                     * putting subtotal+tax
                     */
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid) + authHandler.round(lineLevelTaxAmountTotal, companyid));       
                    //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                    ExportRecordHandler.setHsnSacProductDimensionField(prod, obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.HSN_SAC_CODE, obj.optString(CustomDesignerConstants.HSN_SAC_CODE, ""));
                    gstTaxSummaryDetailsTableDataArr.put(gstTaxSummaryDetailsTableData);
                } else {
                    /*
                    * Fetching distinct taxes used at line level, feetched in the set
                    * Also, fetched the information related to tax in different maps
                    */
                    boolean isRowTaxApplicable = false;
                    double rowTaxPercentGST = 0.0;
                    double linelevelTaxAmount = 0.0; // Line level Tax + Term tax amount
                    if (row.getTax() != null) {
                        String taxCode = row.getTax().getTaxCode();
                        if (!lineLevelTaxesGST.contains(taxCode)) {
                            lineLevelTaxesGST.add(taxCode);
//                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), row.getTax().getID());
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getCreationDate(), row.getTax().getID());
                            rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                            lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                            lineLevelTaxAmountGST.put(taxCode, 0.0);
                            lineLevelTaxBasicGST.put(taxCode, 0.0);
                        }
                        linelevelTaxAmount = row.getRowTaxAmount() + row.getRowTermTaxAmount();
                        lineLevelTaxAmountGST.put(taxCode,(Double) lineLevelTaxAmountGST.get(taxCode) + linelevelTaxAmount);
                        /*
                         * All line level basic value is subtotal - discount.
                         */
                        lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + obj.optDouble(CustomDesignerConstants.SUBTOTAL,0.0) - obj.optDouble(CustomDesignerConstants.IN_Discount,0.0));
                     }
                    /*
                     * putting subtotal+tax
                     */
                     obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid) + authHandler.round(linelevelTaxAmount, companyid)); 
                        
                }

                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(prod.getID())) {
                    KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), prod.getID());
                    Product product = (Product) prodresult.getEntityList().get(0);
                    isLocationForProduct = product.isIslocationforproduct();
                    isbatchforproduct = product.isIsBatchForProduct();
                    isserialforproduct = product.isIsSerialForProduct();
                    isWarehouseForProduct = product.isIswarehouseforproduct();

                }
                if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory()
                        || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory()
                        || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {
                    if (isbatchforproduct || isserialforproduct || isLocationForProduct || isWarehouseForProduct) {  //product level batch and serial no on or not but now only location is checked
                        String rowid = "";
                        KwlReturnObject res = accInvoiceDAOobj.getDOFromInvoiceNew(invoice.getID(), companyid);
                        if (res.getEntityList().size() > 0 && res.getEntityList().size() >= rowcnt) {
                            String DOdetailID  = res.getEntityList().get(rowcnt-1).toString();
                            DeliveryOrderDetail deliveryOrderDetailObj = (DeliveryOrderDetail) kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.DeliveryOrderDetail", DOdetailID);
                            if (deliveryOrderDetailObj != null) {
                                rowid = deliveryOrderDetailObj.getID();
                            }
                        } else {
                            if (row.getDeliveryOrderDetail() != null) {
                                rowid = row.getDeliveryOrderDetail().getID();
                            }
                        }
                        
                        String batchdetails = accInvoiceServiceDAO.getNewBatchJson(prod, requestObj, rowid);
                        JSONArray locjArr = new JSONArray(batchdetails);
                        String location = "";
                        String locationName = "", batchname = "", warehouse = "", serialnumber = "", serialexpdate = "",batchexpdate="",batchmfgdate="";
                        String locationnamenew = "", batchnamenew = "", warehousenew = "", serialnumbernew = "", serialexpdatenew = "",batchexpdatenew="",batchesmfgdatenew = "";
                        LinkedList<String> batchnames = new LinkedList();
                        LinkedList<String> serialnumbers = new LinkedList();
                        LinkedList<String> locationnames = new LinkedList();
                        LinkedList<String> warehouses = new LinkedList();
                        LinkedList<String> serialsexpdate = new LinkedList();
                        LinkedList<String> batchesexpirydate = new LinkedList();
                        LinkedList<String> batchesmfgdate = new LinkedList();
                        
                        for (int i = 0; i < locjArr.length(); i++) {
                            JSONObject jSONObject = new JSONObject(locjArr.get(i).toString());
                            location = jSONObject.optString("location", "");
                            batchname = jSONObject.optString("batchname", "");
                            warehouse = jSONObject.optString("warehouse", "");
                            serialnumber = jSONObject.optString("serialno", "");
                            serialexpdate = jSONObject.optString("expend", "");
                            batchexpdate = jSONObject.optString("expdate", "");
                            batchmfgdate = jSONObject.optString("mfgdate", "");
                            Date date = null;
                            if (!StringUtil.isNullOrEmpty(serialexpdate)) {
                                date = df1.parse(serialexpdate);
                                serialexpdate = df.format(date);
                                serialsexpdate.add(serialexpdate);
                            } else {
                                serialsexpdate.add(" ");
                            }
                            
                            if (!StringUtil.isNullOrEmpty(batchexpdate)) {
                                date = df1.parse(batchexpdate);
                                batchexpdate = df.format(date);
                                if (batchesexpirydate.contains(batchexpdate)) {
                                     batchesexpirydate.add(" ");
                                } else {
                                    batchesexpirydate.add(batchexpdate);
                                }
                            } else {
                                batchesexpirydate.add(" ");
                            }
                            if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
                                date = df1.parse(batchmfgdate);
                                batchmfgdate = df.format(date);
                                if (batchesmfgdate.contains(batchmfgdate)) {
                                    batchesmfgdate.add(" ");
                                } else {
                                    batchesmfgdate.add(batchmfgdate);
                                }
                            } else {
                                batchesmfgdate.add(" ");
                            }
                            serialnumbers.add(serialnumber);
                            
                            if (!StringUtil.isNullOrEmpty(batchname)) {
                                if (batchnames.contains(batchname)) {
                                    batchnames.add(" ");
                                } else {
                                    batchnames.add(batchname);
                                }
                            }
                            
                            if (!StringUtil.isNullOrEmpty(warehouse)) {
                                if (warehouses.contains(warehouse)) {
                                    warehouses.add(" ");
                                } else {
                                    warehouses.add(warehouse);
                                }
                            }
                            
                            if (!StringUtil.isNullOrEmpty(location)) {
                                KwlReturnObject loc = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), location);
                                InventoryLocation localist = (InventoryLocation) loc.getEntityList().get(0);
                                locationName = localist.getName();
                                if (locationnames.contains(locationName)) {
                                    locationnames.add(" ");
                                } else {
                                    locationnames.add(locationName);
                                }
//                                locationName = locationName.concat(",");
                            }

                            if (!StringUtil.isNullOrEmpty(batchname)) {
                                batchname = batchname.concat("!##");
                                countOfBatches++;
                            }
                            if (!StringUtil.isNullOrEmpty(serialnumber)) {
                                serialnumber = serialnumber.concat("!##");
                            }
                            if (!StringUtil.isNullOrEmpty(serialexpdate)) {
                                serialexpdate = serialexpdate.concat("!##");
                            }
                            if (!StringUtil.isNullOrEmpty(batchexpdate)) {
                                batchexpdate = batchexpdate.concat("!##");
                            }
                            if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
                                batchmfgdate = batchmfgdate.concat("!##");
                            }
                        }

                        for (String str : batchnames) {
                            String bno = "";
                            bno = str;
                            challanBalanceQtyString += challanQtyDetails.optString( row.getInvoice().getInvoiceNumber() + str + "balance", "0.0") + CustomDesignerConstants.VALUE_SEPARATOR;// Get balance qty in same squence as batch name comes
                            challanConsumeQtyString += challanQtyDetails.optString( row.getInvoice().getInvoiceNumber() + str + "consume", "0.0") + CustomDesignerConstants.VALUE_SEPARATOR;// Get consume qty in same squence as batch name comes
                            batchnamenew += bno.concat("!##");
                        }

                        for (String str : serialnumbers) {
                            String sno = "";
                            sno = str;
                            if (!StringUtil.isNullOrEmpty(sno) && !sno.equals(" ")) {
                                serialnumbernew += sno.concat("!##");
                            }
                        }
                        for (String str : locationnames) {
                            String lno = "";
                            lno = str;
                            if (!StringUtil.isNullOrEmpty(lno) && !lno.equals(" ")) {
                                locationnamenew += lno.concat("!##");
                            }
                        }
                        for (String str : serialsexpdate) {
                            String sexp = "";
                            sexp = str;
                            if (!StringUtil.isNullOrEmpty(sexp) && !sexp.equals(" ")) {
                                serialexpdatenew += sexp.concat("!##");
                            }
                        }

                        for (String str : batchesexpirydate) {
                            String bexp = "";
                            bexp = str;
                            if (!StringUtil.isNullOrEmpty(bexp) && !bexp.equals(" ")) {
                                batchexpdatenew += bexp.concat("!##");
                            }
                        }
                        for (String str : batchesmfgdate) {
                            String bmfg = "";
                            bmfg = str;
                            if (!StringUtil.isNullOrEmpty(bmfg) && !bmfg.equals(" ")) {
                                batchesmfgdatenew += bmfg.concat("!##");
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(batchesmfgdatenew)) {
                            batchesmfgdatenew = batchesmfgdatenew.substring(0, batchesmfgdatenew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(serialnumbernew)) {
                            serialnumbernew = serialnumbernew.substring(0, serialnumbernew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(serialexpdatenew)) {
                            serialexpdatenew = serialexpdatenew.substring(0, serialexpdatenew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(batchnamenew)) {
                            batchnamenew = batchnamenew.substring(0, batchnamenew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(locationnamenew)) {
                            locationnamenew = locationnamenew.substring(0, locationnamenew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(batchexpdatenew)) {
                            batchexpdatenew = batchexpdatenew.substring(0, batchexpdatenew.length() - 3);
                        }
                        obj.put(CustomDesignerConstants.SerialNumber, serialnumbernew);
                        obj.put(CustomDesignerConstants.SerialNumberExp, serialexpdatenew);
                        obj.put(CustomDesignerConstants.BatchNumber, batchnamenew);
                        obj.put(CustomDesignerConstants.JobWorkBalanceQty, challanBalanceQtyString);
                        obj.put(CustomDesignerConstants.JobWorkConsumeQty, challanConsumeQtyString);
                        obj.put(CustomDesignerConstants.BatchNumberExp, batchexpdatenew);
                        obj.put(CustomDesignerConstants.IN_Loc, locationnamenew);
                        obj.put(CustomDesignerConstants.ManufacturingDate, batchesmfgdatenew);// Batch Manufacturing Date
                    } else { //if not activated location for product level then take default location for product.
                        obj.put(CustomDesignerConstants.IN_Loc, prod.getLocation() == null ? "" : prod.getLocation().getName());
                    }
                } else { //if not activated location for product level then take default location for product.
                    obj.put(CustomDesignerConstants.IN_Loc, prod.getLocation() == null ? "" : prod.getLocation().getName());
                }
                /*Row tax section*/
//                entryDate = invoice.getJournalEntry().getEntryDate();
                entryDate = invoice.getCreationDate();
                if (row != null && row.getTax() != null) {
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", row.getTax().getID());
                    KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                    List taxList = result1.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    rowTaxName = row.getTax().getName();
                    rowTaxAmt = row.getRowTaxAmount();
//                    rowTaxAmt += row.getRowTermTaxAmount();
                }
                /**
                 * Calculate line level total tax amount before deducting partial payment
                 */
                if(isPartialInvoice){
                    if(row.getInvoice().isGstIncluded()){
                        totalTaxAmountBeforePartialPayment += authHandler.round(rowtaxamountincludeGST, companyid);
                    } else{
                        totalTaxAmountBeforePartialPayment += authHandler.round(((rate * quantity) - discountBeforePartial) * rowTaxPercent / 100, companyid);
                    }
                } else{
                    totalTaxAmountBeforePartialPayment += rowTaxAmt;
                }
                totaltax += rowTaxAmt;//Calculate tax amount from line item
                obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmt);//Row TaxAmount
                obj.put(CustomDesignerConstants.IN_ProductTax, rowTaxName);

                if (row.getInvoice().isGstIncluded()) {
                    rowamountwithtax = rowamountwithouttax + rowTaxAmt - rowdiscountvalue; //Amount will be equal to rowamountwithouttax because tax gets added in gst
                } else {
                    if (rowdiscountvalue != 0) {
                        rowamountwithouttax -= rowdiscountvalue;//deducting discount if any
                    }
                    rowamountwithtax = rowamountwithouttax + rowTaxAmt;
                }
                obj.put(CustomDesignerConstants.Amount, authHandler.round(rowamountwithtax, companyid)); // Amount
                //obj.put(CustomDesignerConstants.SpecificCurrencyAmount, authHandler.round(rowamountwithtax, companyid)); // Amount
                obj.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, 1);
                obj.put("gstCurrencyRate", invoice.getGstCurrencyRate() == 0 ? "" : invoice.getGstCurrencyRate());
                summaryData.put("gstCurrencyRate", invoice.getGstCurrencyRate() == 0 ? "" : invoice.getGstCurrencyRate());
                transactionDate = invoice.getJournalEntry().getEntryDate();
                obj.put("transactiondate", transactionDate); 
                summaryData.put("transactiondate", transactionDate);
                 
                /*<--------------------Base Currency Unit Price,Base currency Subtotal ----------->*/
                /*Exchange Rate Section---- We have not given amount with tax because it is matched with UI*/
                if (externalCurrencyRate != 0) {
                    //Unit Price   
                    exchangerateunitprice = authHandler.round((rate * revExchangeRate), companyid);  //exchanged rate unit rate
                    //SUbTotal (rate*quantity)
                    exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                    //Exhanged Rate Discount
                    exchangeratelineitemdiscount = authHandler.round((rowdiscountvalue * revExchangeRate), companyid);//exchange rate total discount
                    if (row.getInvoice().isGstIncluded()) {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal), companyid);   
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                    } else {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                    }
                    
                    //Exhanged Rate Tax Amount
                    exchangeratelineitemtax = extraCompanyPreferences.isIsNewGST() ? authHandler.round((lineLevelTaxAmountTotal * revExchangeRate), companyid) : authHandler.round((rowTaxAmt * revExchangeRate), companyid);
                    
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);
                    
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, authHandler.round(exchangeratelineitemtax, companyid));
                    obj.put(CustomDesignerConstants.SpecificCurrencyAmount, authHandler.round(exchangeratelineitemamount, companyid)); // Amount

                    //Total Exchanged Rate (RATE*Quantity)
                    globalLevelExchangedRateSubTotal = globalLevelExchangedRateSubTotal + authHandler.round(exchangeratelineitemsubtotal, companyid);
                    //Overall Subtotal with Discount
                    globalLevelExchangedRateSubTotalwithDiscount = globalLevelExchangedRateSubTotalwithDiscount + authHandler.round(exchangeratelineitemsubtotalwithdiscount, companyid);
                    //Overall Total Tax
                    globalLevelExchangedRateTotalTax = globalLevelExchangedRateTotalTax +  authHandler.round(exchangeratelineitemtax, companyid);

                } else {
                    //Unit Price   
                    exchangerateunitprice = authHandler.round(rate, companyid) ;  //exchanged rate unit rate
                    //SUbTotal (rate*quantity)
                    exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                    //Exhanged Rate Discount
                    exchangeratelineitemdiscount = authHandler.round(rowdiscountvalue, companyid) ;//exchange rate total discount
                    if (row.getInvoice().isGstIncluded()) {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                    } else {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                    }
                    
                    //Exhanged Rate Tax Amount
                    exchangeratelineitemtax = extraCompanyPreferences.isIsNewGST() ? authHandler.round(lineLevelTaxAmountTotal, companyid) : authHandler.round(rowTaxAmt, companyid);
                    
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount); // Amount

                    //Total Exchanged Rate (RATE*Quantity)
                    globalLevelExchangedRateSubTotal = globalLevelExchangedRateSubTotal + exchangeratelineitemsubtotal;
                    //Overall Subtotal with Discount
                    globalLevelExchangedRateSubTotalwithDiscount = globalLevelExchangedRateSubTotalwithDiscount + exchangeratelineitemsubtotalwithdiscount;
                    //Overall Total Tax
                    globalLevelExchangedRateTotalTax = globalLevelExchangedRateTotalTax +  exchangeratelineitemtax;
                }
                
                /*
                 * to get the linkig information upto 2-3 levels (Mayur B).
                 */
                if (row.getDeliveryOrderDetail() != null) {
                    if (DOref.indexOf(row.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber()) == -1) {
                        DOref += row.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber() + ", ";
                        DOdate += df.format(row.getDeliveryOrderDetail().getDeliveryOrder().getOrderDate()) + ", ";
                    }
                    //get indirect linking info of Sales Order
                    if(row.getDeliveryOrderDetail().getSodetails() != null){
                        SOref += row.getDeliveryOrderDetail().getSodetails().getSalesOrder().getSalesOrderNumber() + ",";
                        salesOrderSet.add(row.getDeliveryOrderDetail().getSodetails().getSalesOrder().getSalesOrderNumber());
                        //get indirect linking info of Customer Quotation
                        if(row.getDeliveryOrderDetail().getSodetails().getQuotationDetail() != null){
                            QouteRef += row.getDeliveryOrderDetail().getSodetails().getQuotationDetail().getQuotation().getquotationNumber() + ",";
                            quotationSet.add(row.getDeliveryOrderDetail().getSodetails().getQuotationDetail().getQuotation().getquotationNumber());
                        }
                    }
                    obj.put(CustomDesignerConstants.Link_No, row.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber());
                } else if (row.getSalesorderdetail() != null) {
                    if (SOref.indexOf(row.getSalesorderdetail().getSalesOrder().getSalesOrderNumber()) == -1) {
                        SOref += row.getSalesorderdetail().getSalesOrder().getSalesOrderNumber() + ",";
                    }
                    //get indirect linking info of Customer Quotation
                    if(row.getSalesorderdetail().getQuotationDetail() != null){
                        QouteRef += row.getSalesorderdetail().getQuotationDetail().getQuotation().getquotationNumber() + ",";
                        quotationSet.add(row.getSalesorderdetail().getQuotationDetail().getQuotation().getquotationNumber());
                    }
                    obj.put(CustomDesignerConstants.Link_No, row.getSalesorderdetail().getSalesOrder().getSalesOrderNumber());
                    SalesOrderDate = row.getSalesorderdetail().getSalesOrder().getOrderDate();
                } else if (row.getQuotationDetail() != null) {
                    if (QouteRef.indexOf(row.getQuotationDetail().getQuotation().getquotationNumber()) == -1) {
                        QouteRef += row.getQuotationDetail().getQuotation().getquotationNumber() + ",";
                    }
                    obj.put(CustomDesignerConstants.Link_No, row.getQuotationDetail().getQuotation().getquotationNumber());
                }

                /*
                 * get custom line data
                 */
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                Detailfilter_params.add(row.getID());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                KwlReturnObject idcustresult = accInvoiceDAOobj.getInvoiceDetailsCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, jeDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                
                /*Product Level Custom Fields Evaluation*/
                invDetailRequestParams.clear();
                Detailfilter_names.add("productId");
                Detailfilter_params.add(row.getInventory().getProduct().getID());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                KwlReturnObject idcustresultForProduct = accInvoiceDAOobj.getInvoiceDetailsCustomDataForProductNew(invDetailRequestParams);
                AccJEDetailsProductCustomData accJEDetailsProductCustomData = null;
                String customDataID = "";
                if (idcustresultForProduct.getEntityList().size() > 0) {
//                    accJEDetailsProductCustomData = (AccJEDetailsProductCustomData) idcustresultForProduct.getEntityList().get(0);
                    customDataID = idcustresultForProduct.getEntityList().get(0).toString();
                    accJEDetailsProductCustomData =  (AccJEDetailsProductCustomData)  kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.AccJEDetailsProductCustomData", customDataID);
                }
                replaceFieldMap = new HashMap<String, String>();
                if (accJEDetailsProductCustomData != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, accJEDetailsProductCustomData, ProductLevelCustomFieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                
                  /*Set All Line level Dimension & All LIne level Custom Field Values*/   
                obj=CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap,variableMap,obj,false);//for dimensions
                obj=CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap,variableMap,obj,true);//for customfields
                                                
                //Exchange Rate
                obj.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);
                JSONObject jObj = new JSONObject();
                if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                    jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                }
                if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                    obj = accProductObj.getProductDisplayUOM(prod, quantity,row.getInventory().getBaseuomrate(), false, obj);
                }
                //GST Exchange Rate
                if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !preference.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                    obj.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, row.getGstCurrencyRate());
                }
                jArr.put(obj);
            }
            
            if (!StringUtil.isNullOrEmpty(invid)) {
                HashMap<String, Object> reqParam = new HashMap();
                reqParam.put("invoiceid", invid);
                KwlReturnObject curresult = null;
                double termTaxAmnt = 0;
                /*
                 * Terms calculation
                 */
                curresult = accInvoiceDAOobj.getInvoiceTermMap(reqParam);
                List<InvoiceTermsMap> termMap = curresult.getEntityList();
                for (InvoiceTermsMap invoiceTerMap : termMap) {
                    termTaxAmnt += invoiceTerMap.getTermtaxamount();
                }
                totaltax += termTaxAmnt;
            }
            
            detailsTableData.put("gsttaxsummary", gstTaxSummaryDetailsTableDataArr);
            detailsTableData.put("isDetailsTableData", true);
            jArr.put(detailsTableData);
            //Auto generated true for Delivery Order && f invoice is linked to any Delivery Order.
            KwlReturnObject InvoiceDo = accInvoiceDAOobj.getDOFromInvoices(invid, companyid, true);
            count = InvoiceDo.getRecordTotalCount();
            List<Object[]> InvoiceDoList = InvoiceDo.getEntityList();
            for (Object[] oj : InvoiceDoList) {
                DeliveryOrder DO = (DeliveryOrder) accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), oj[1].toString()).getEntityList().get(0);
                DOref += (oj[0] != null ? oj[0].toString() : "0") + ", ";
                DOdate += DO != null ? (DO.getOrderDate() != null ? df.format(DO.getOrderDate()) : "") + ", " : "";
            }
            //remove comma 
            if (!StringUtil.isNullOrEmpty(DOref)) {
                DOref = DOref.substring(0, DOref.length() - 2);
                DOdate = DOdate.substring(0, DOdate.length() - 2);
            }
            if (!StringUtil.isNullOrEmpty(SOref)) {
                SOref = SOref.substring(0, SOref.length() - 1);
            }
            if (!StringUtil.isNullOrEmpty(QouteRef)) {
                QouteRef = QouteRef.substring(0, QouteRef.length() - 1);
            }
            if (!StringUtil.isNullOrEmpty(jobIN)) {
                jobIN = jobIN.substring(0, jobIN.length() - 2);
            }
            //If multiple indirect linking present then don't show indirect linking info. Show it as blank. - ERP-33521
            if(quotationSet.size() > 1 || salesOrderSet.size() > 1){
                QouteRef = "";
                SOref = "";
            } else if(quotationSet.size() == 1 || salesOrderSet.size() == 1){
                if(!quotationSet.isEmpty()){
                    QouteRef = quotationSet.toString();
                    QouteRef = QouteRef.substring(1, QouteRef.length()-1);
                }
                if(!salesOrderSet.isEmpty()){
                    SOref = salesOrderSet.toString();
                    SOref = SOref.substring(1, SOref.length()-1);
                }
            }
            mainTax = invoice.getTax();
            if (mainTax != null) { //Get Overall Tax percent && total tax amount
                requestParams.put("transactiondate", entryDate);
                requestParams.put("taxid", mainTax.getID());
                KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                List taxList = result1.getEntityList();
                Object[] taxObj = (Object[]) taxList.get(0);
                taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                mainTaxName = mainTax.getName();
//                if(!isFromChklorMarubishi){
//                totaltax = invoice.getTaxEntry() != null ? invoice.getTaxEntry().getAmount() : 0;
//                }else{
                totaltax += invoice.getTaxEntry() != null ? invoice.getTaxEntry().getAmount() : 0;
//                }
                if (externalCurrencyRate != 0) {
                    globalLevelExchangedRateTotalTax = authHandler.round((totaltax * revExchangeRate), companyid);//exchanged rate total tax amount 
                } else {
                    globalLevelExchangedRateTotalTax = authHandler.round((totaltax), companyid);//exchanged rate total tax amount 
                }
                /**
                 * Calculate global total tax amount before deducting partial payment
                 */
                if(isPartialInvoice){
                    totalTaxAmountBeforePartialPayment = subTotalBeforePartialPayment * taxPercent / 100;
                } else{
                    totalTaxAmountBeforePartialPayment = totaltax;
                }
                // Get global level tax details for GST Summary
                allLineLevelTax = mainTax.getTaxCode();
                allLineLevelTaxAmount = String.valueOf(totaltax);
                allLineLevelTaxBasic = String.valueOf(subtotal);
            }
            
            mainTaxName = invoice.getTax() != null ? invoice.getTax().getName() : "";//overall tax name
            totalAmount = invoice.getCustomerEntry()!=null?invoice.getCustomerEntry().getAmount():0;//Total amount of transaction
            if(invoice.isIsOpeningBalenceInvoice()){
                totalAmount = invoice.getOriginalOpeningBalanceAmount();
            }
            if(extraCompanyPreferences.isIsNewGST()){ // for New gst check 
                if(!lineLevelTaxNames.isEmpty()){
                    Iterator lineTax = lineLevelTaxNames.entrySet().iterator();
                    while(lineTax.hasNext()){
                        Map.Entry tax = (Map.Entry)lineTax.next();
                        allLineLevelTax += tax.getKey();
                        allLineLevelTax += "!## ";
                        double taxamount = (double)tax.getValue();
                        allLineLevelTaxAmount += tax.getValue().toString();
                        TotalLineLevelTaxAmount += taxamount;
                        allLineLevelTaxAmount += "!## ";
                        if (taxamount > 0) {
                            lineLevelTaxSign += "+";
                            lineLevelTaxSign += "!## ";
                        } else {
                            lineLevelTaxSign += "-&nbsp;";
                            lineLevelTaxSign += "!## ";
                        }
                    }
                }
                if(!StringUtil.isNullOrEmpty(allLineLevelTax)){
                    allLineLevelTax = allLineLevelTax.substring(0, allLineLevelTax.length()-4);
                }
                if(!StringUtil.isNullOrEmpty(allLineLevelTaxAmount)){
                    allLineLevelTaxAmount=allLineLevelTaxAmount.substring(0, allLineLevelTaxAmount.length()-4);
                }
                if(!StringUtil.isNullOrEmpty(lineLevelTaxSign)){
                    lineLevelTaxSign= lineLevelTaxSign.substring(0, lineLevelTaxSign.length()-4);
                }
            } else {
                /*
                * Putting all line taxes and its information in summary data separated by !##
                */
                for ( String key : lineLevelTaxesGST ) {
                    allLineLevelTax += key + "!##";
                    allLineLevelTaxAmount +=  lineLevelTaxAmountGST.get(key).toString() + "!##";
                    allLineLevelTaxBasic += lineLevelTaxBasicGST.get(key).toString() + "!##";
                }
            }

            if(extraCompanyPreferences.isIsNewGST()){ // for New gst check 
                totaltax = TotalLineLevelTaxAmount;
            }
            serviceTaxRateForServiceTaxInvoice = (globalLevelExchangedRateSubTotal * 14) / 100;
            swacchaBharatCessForServiceTaxInvoice = (globalLevelExchangedRateSubTotal * 0.5) / 100;
            totalAmountForServiceTaxInvoice = globalLevelExchangedRateSubTotal + serviceTaxRateForServiceTaxInvoice + swacchaBharatCessForServiceTaxInvoice;
            //get Rounding Difference //ERP-25876
            if(invoice.getJournalEntry() != null){
                Iterator ite = invoice.getJournalEntry().getDetails().iterator();
                while(ite.hasNext()){
                    JournalEntryDetail detailsObj = (JournalEntryDetail) ite.next();
                     if(detailsObj.isRoundingDifferenceDetail()){
                        roundingDiff = authHandler.round(detailsObj.getAmount(), companyid);
                    }
                }
            }
            
            String term = "", termsname = "", termsNameWithoutPercent= "",termsamount = "",termssign="";
            double totalTermAmount = 0;
            if (!StringUtil.isNullOrEmpty(invid)) {
                HashMap<String, Object> requestParam = new HashMap();
                requestParam.put("invoiceid", invid);
                KwlReturnObject curresult = null;

                /*Terms calculation*/
                curresult = accInvoiceDAOobj.getInvoiceTermMap(requestParam);
                List<InvoiceTermsMap> termMap = curresult.getEntityList();
                for (InvoiceTermsMap invoiceTerMap : termMap) {
                    InvoiceTermsSales mt = invoiceTerMap.getTerm();
                    double termAmnt = 0;
                    if(invoice.isGstIncluded()){
                        termAmnt = invoiceTerMap.getTermAmountExcludingTax();
                    }else{
                        termAmnt = invoiceTerMap.getTermamount();
                    }
                    String termName = (mt.getTerm() + (termAmnt > 0 ? "(+)" : "(-)"));
                    summaryData.put(mt.getTerm(), termAmnt);
                    summaryData.put(CustomDesignerConstants.BaseCurrency + mt.getTerm(), (termAmnt * revExchangeRate));//Base currency exchange rate term value-ERP-13451
                    totalTermAmount += termAmnt;
                    double exchangeratetermamount = 0;
                    if (externalCurrencyRate != 0) {
                        exchangeratetermamount = (termAmnt * revExchangeRate);
                    } else {
                        exchangeratetermamount = termAmnt;
                    }
                    globalLevelExchangedRateTermAmount += exchangeratetermamount;
                    term += "<div> <table><tr><td>" + termName + " : </td><td>" + Double.toString(termAmnt) + "</td></tr></table></div><br>";
                    if(termAmnt != 0){
                        termsNameWithoutPercent += mt.getTerm() +"!## ";
                        if(termAmnt > 0){
                            termssign +="+!## ";
                        } else{
                            termssign +="-&nbsp;!## ";
                        }
                        termsamount += CustomDesignHandler.getAmountinCommaDecimal(Math.abs(termAmnt), amountdigitafterdecimal,countryid) +"!## ";
                    } 
                    termsname += mt.getTerm() + " " + invoiceTerMap.getPercentage() + "%, ";
                     if (!StringUtil.isNullOrEmpty(String.valueOf(termAmnt))) {
                        String allTermsPlaceholder = CustomDesignerConstants.AllTermsKeyValuePair;
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsLabel, termName);
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsValue, CustomDesignHandler.getAmountinCommaDecimal(termAmnt, amountdigitafterdecimal, countryid));
                        appendtermString.append(allTermsPlaceholder);
                    }
                }
                if (!StringUtil.isNullOrEmpty(termsname)) {
                    termsname = termsname.substring(0, termsname.length() - 2);
                }
                if (!StringUtil.isNullOrEmpty(termsNameWithoutPercent)) {
                    termsNameWithoutPercent = termsNameWithoutPercent.substring(0, termsNameWithoutPercent.length() - 4);
                }
                if (!StringUtil.isNullOrEmpty(termssign)) {
                    termssign = termssign.substring(0, termssign.length() - 4);
                }
                if (!StringUtil.isNullOrEmpty(termsamount)) {
                    termsamount = termsamount.substring(0, termsamount.length() - 4);
                }
                if (!StringUtil.isNullOrEmpty(term) && term.indexOf("<br>") != -1) {
                    term = term.substring(0, term.lastIndexOf("<br>"));
                }
                if(!StringUtil.isNullOrEmpty(appendtermString.toString())){
                    allTerms=appendtermString.toString();
                }
            }
            
            globalLevelExchangedRateTermAmount = authHandler.round(globalLevelExchangedRateTermAmount, companyid);
            /* Base Currency Total Amount*/
            globalLevelExchangedRateTotalAmount = globalLevelExchangedRateSubTotalwithDiscount + globalLevelExchangedRateTotalTax + globalLevelExchangedRateTermAmount;
            
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
            String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency,countryLanguageId);
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if(countryid == Constants.INDONESIAN_COUNTRY_ID){
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(totalAmount)), indoCurrency);
            }
            String gstAmountInWords = EnglishNumberToWordsOjb.convert(totaltax, currency,countryLanguageId);
            if (invoice.isNormalInvoice()) {
                if (Constants.InvoiceAmountDueFlag) {
                    List ll = getInvoiceDiscountAmountInfo(requestParams, invoice);
                    amountDue = (Double) ll.get(0);
                } else {
                    List ll = getAmountDue_Discount(requestParams, invoice);
                    amountDue = (Double) ll.get(0);
                }
            } else if (invoice.isIsOpeningBalenceInvoice()) {
                amountDue = invoice.getOpeningBalanceAmountDue();
            } else {
                amountDue = invoice.getOriginalOpeningBalanceAmount();
            }
            Amountwithoutterm = subtotal + totaltax;//Amount after adition of subtotal and total tax without invoice term
            paymentreceived = totalAmount - amountDue;
            String amountDueinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amountDue)), currency,countryLanguageId);
            String paymentreceivedinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(paymentreceived)), currency,countryLanguageId);

            Date date = new Date();
            String printedOn = df.format(date);

            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("customerid", invoice.getCustomer().getID());
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData, preference, extraCompanyPreferences);
            billAddr = CommonFunctions.getTotalBillingShippingAddress(invoice.getBillingShippingAddresses(), true);
            shipAddr = CommonFunctions.getTotalBillingShippingAddress(invoice.getBillingShippingAddresses(), false);
            summaryData.put("summarydata", true);

            /*All Global Section Custom Field and DImensions*/
            boolean isConsignment = requestObj.optBoolean("isConsignment", false);
            boolean isLeaseFixedAsset = requestObj.optBoolean("isLeaseFixedAsset", false);
            
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            int moduleid = Constants.Acc_Invoice_ModuleId;
            if(isConsignment){
                moduleid = Constants.Acc_ConsignmentInvoice_ModuleId;
            } else if(isLeaseFixedAsset){
                moduleid = Constants.LEASE_INVOICE_MODULEID;
            }
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, moduleid);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", invoice.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            extraparams.put("approvestatuslevel", invoice.getApprovestatuslevel());
            String userId = requestObj.optString(Constants.useridKey);
            KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userresult.getEntityList().get(0);
            //Details like company details
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);
            boolean isGstInclued = invoice.isGstIncluded();
            double subTotalwithDiscount = 0.0;//Subtotal with discount
            double subTotalwithTax = 0.0;//Subtotal with tax //ERP-25162
            if (isGstInclued) {
                if(invoice.isGstIncluded()){
                    subTotalwithDiscount = subtotal - totalDiscount;
                } else{
                    subTotalwithDiscount = totalAmount - totalTermAmount;
                }
                subTotalwithTax = totalAmount - totalTermAmount; //ERP-25162
            } else {
                subTotalwithDiscount = totalAmount - totaltax - totalTermAmount;
                subTotalwithTax = totalAmount - totalTermAmount; //ERP-25162
            }
            if(countryid == Constants.indian_country_id){
                
                // Customer Related indian details
                String panStatus = "", IECNo = "", CSTDateStr = "", VATDateStr = "", dealerTypeStr = "";
                String ImporterECCNo = "",  typeOfSales = "";

                if (invoice.getCustomer().getPanStatus() != null && !StringUtil.isNullOrEmpty(invoice.getCustomer().getPanStatus())) {
                    panStatus = invoice.getCustomer().getPanStatus().equals(IndiaComplianceConstants.PAN_STATUS_PANNOTAVBL) ? IndiaComplianceConstants.PAN_NOT_AVAILABLE : invoice.getCustomer().getPanStatus().equals(IndiaComplianceConstants.PAN_STATUS_APPLIEDFOR) ? IndiaComplianceConstants.PAN_APPLIED_FOR : "";
                }
                if(invoice.getCustomer().getDefaultnatureOfPurchase()!= null && !StringUtil.isNullOrEmpty(invoice.getCustomer().getDefaultnatureOfPurchase())){
                    KwlReturnObject naturetyperesult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), invoice.getCustomer().getDefaultnatureOfPurchase());
                    MasterItem natureTypeObj = (MasterItem) naturetyperesult.getEntityList().get(0);
                    if(natureTypeObj != null){
                        typeOfSales = natureTypeObj.getValue();
                    }
                }

                if (invoice.getCustomer().getDealertype() != null && !StringUtil.isNullOrEmpty(invoice.getCustomer().getDealertype())) {
                    String dealerType = invoice.getCustomer().getDealertype();
                    if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_REGISTERED)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_REGISTERED_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_UNREGISTERED)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_UNREGISTERED_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_1_2)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_1_2_STR;
                    }
                }
                if (invoice.getCustomer().getCSTRegDate() != null) {
                    Date CSTDate = invoice.getCustomer().getCSTRegDate();
                    CSTDateStr = CSTDate != null ? authHandler.getUserDateFormatterJson(requestObj).format(CSTDate) : "";
                }
                if (invoice.getCustomer().getVatregdate() != null) {
                    Date VATDate = invoice.getCustomer().getVatregdate();
                    VATDateStr = VATDate != null ? authHandler.getUserDateFormatterJson(requestObj).format(VATDate) : "";
                }

                SimpleDateFormat timeFormatter = new SimpleDateFormat(Constants.SQL_DATE_FORMAT);
                createdOnWithTime = invoiceCreationDate != null ? timeFormatter.format(invoiceCreationDate) : "";
                buyerComrate = invoice.getCustomer().getCommissionerate() != null ? invoice.getCustomer().getCommissionerate() : "";
                buyerDivision = invoice.getCustomer().getDivision() != null ? invoice.getCustomer().getDivision() : "";
                buyerRange = invoice.getCustomer().getRangecode() != null ? invoice.getCustomer().getRangecode() : "";
                buyetinNo = invoice.getCustomer().getVATTINnumber() != null ? invoice.getCustomer().getVATTINnumber() : "";
                buyerexcRegNo = invoice.getCustomer().getECCnumber() != null ? invoice.getCustomer().getECCnumber() : "";
                buyerpanNo = invoice.getCustomer().getPANnumber() != null ? invoice.getCustomer().getPANnumber() : "";
                buyerServiceTaxRegNo = invoice.getCustomer().getSERVICEnumber() != null ? invoice.getCustomer().getSERVICEnumber() : "";
                ImporterECCNo = (invoice.getCustomer() != null && invoice.getCustomer().getImporterECCNo() != null) ? invoice.getCustomer().getImporterECCNo() : "";
                IECNo = (invoice.getCustomer() != null && invoice.getCustomer().getIECNo() != null) ? invoice.getCustomer().getIECNo() : "";
                
                // ************************   Customer Related Information **********************************************
                summaryData.put(CustomDesignerConstants.CUSTOMER_PAN_NO, buyerpanNo);
                summaryData.put(CustomDesignerConstants.CustomerVendor_PAN_STATAUS, panStatus);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, buyetinNo);
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, invoice.getCustomer().getCSTTINnumber() != null ? invoice.getCustomer().getCSTTINnumber() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_REG_DATE, CSTDateStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_REG_DATE, VATDateStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_DEALER_TYPE, dealerTypeStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_INTERSTATEPARTY, invoice.getCustomer().isInterstateparty() ? "Yes" : "No");
                summaryData.put(CustomDesignerConstants.CustomerVendor_C_FORM_APPLICABLE, invoice.getCustomer().isCformapplicable() ? "Yes" : "No");
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_TYPE_OF_SALES, typeOfSales);
                summaryData.put(CustomDesignerConstants.CustomerVendor_ECC_NO, buyerexcRegNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_IMPORTER_ECC_NUMBER, ImporterECCNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_IEC_NUMBER, IECNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_RANGE_CODE, buyerRange);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_DIVISION_CODE, buyerDivision);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_COMMISSIONERATE_CODE, buyerComrate);
                summaryData.put(CustomDesignerConstants.CUSTOMER_SERVICE_TAX_REG_NO, buyerServiceTaxRegNo);
                // ****************************************************************************************************
                if(invoice.isIsExciseInvoice()){
                    KwlReturnObject  data=accInvoiceDAOobj.getSalesInvoiceExciseDetailsHQL(invoice.getID());
                    List list1 = data.getEntityList();
                    Iterator itr = list1.iterator();
                    if(itr.hasNext()){
                        SalesInvoiceExciseDetailsMap details=null;
                        details=(SalesInvoiceExciseDetailsMap) itr.next();
                        buyerexcRegNo=details.getSupplierExciseRegnNo();
                        buyetinNo=details.getSupplierTINSalesTaxNo();
                        buyerRange=details.getSupplierRange();
                        buyerDivision=details.getSupplierDivision();
                        buyerComrate=details.getSupplierCommissioneRate();
                        ManuName=details.getManufacturerName();
                        ManuexcRegNo=details.getManufacturerExciseregnNo();
                        ManuRange=details.getManufacturerRange();
                        ManuDivision=details.getManufacturerDivision();
                        ManuComrate=details.getManufacturerCommissionerate();
                        ManuAddress=details.getManufacturerAddress();
                    }
                }
                summaryData.put(CustomDesignerConstants.CONSIGNEE_NAME, ManuName);
                summaryData.put(CustomDesignerConstants.CONSIGNEE_ADDRESS, ManuAddress);
                summaryData.put(CustomDesignerConstants.CONSIGNEE_EXCISE_REGN_NO, ManuexcRegNo);
                summaryData.put(CustomDesignerConstants.CONSIGNEE_DIVISION_CODE, ManuDivision);
                summaryData.put(CustomDesignerConstants.CONSIGNEE_RANGE_CODE, ManuRange);
                summaryData.put(CustomDesignerConstants.CONSIGNEE_COMMISSIONERATE_CODE, ManuComrate);
                summaryData.put(CustomDesignerConstants.BUYER_EXCISE_REGN_NO, buyerexcRegNo);
                summaryData.put(CustomDesignerConstants.INVOICE_DATE_WITH_TIME, createdOnWithTime);
                summaryData.put(CustomDesignerConstants.VAT_AMOUNT_IN_WORDS, EnglishNumberToWordsOjb.convert(totalVat, currency,countryLanguageId)+ " Only.");
                
                summaryData.put(CustomDesignerConstants.RANGE_CODE_COMPANY, !StringUtil.isNullOrEmpty(extraCompanyPreferences.getExciseRangeCode()) ? extraCompanyPreferences.getExciseRangeCode() : "");
                summaryData.put(CustomDesignerConstants.DIVISION_CODE_COMPANY, !StringUtil.isNullOrEmpty(extraCompanyPreferences.getExciseDivisionCode())? extraCompanyPreferences.getExciseDivisionCode(): "");
                summaryData.put(CustomDesignerConstants.COMMISSIONERATE_CODE_COMPANY, !StringUtil.isNullOrEmpty(extraCompanyPreferences.getExciseCommissionerateCode()) ? extraCompanyPreferences.getExciseCommissionerateCode() : "");
                summaryData.put(CustomDesignerConstants.EXCISE_IN_WORDS, EnglishNumberToWordsOjb.convert(totalExcise, currency,countryLanguageId)+ " Only.");
                summaryData.put(CustomDesignerConstants.EDUCATION_CESS_IN_WORDS, EnglishNumberToWordsOjb.convert(totaleducationCess, currency,countryLanguageId)+ " Only.");
                summaryData.put(CustomDesignerConstants.H_CESS_IN_WORDS, EnglishNumberToWordsOjb.convert(totalHCess, currency,countryLanguageId)+ " Only.");
                summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber()!= null ? extraCompanyPreferences.getVatNumber() : "");
                summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber()!= null ? extraCompanyPreferences.getCstNumber() : "");
                summaryData.put(CustomDesignerConstants.Company_ECC_NO, extraCompanyPreferences.getEccNumber()!= null ? extraCompanyPreferences.getEccNumber() : "");
                summaryData.put(CustomDesignerConstants.CompanyPANNumber, extraCompanyPreferences.getPanNumber()!= null ? extraCompanyPreferences.getPanNumber() : "");
                summaryData.put(CustomDesignerConstants.serviceTaxNumber, extraCompanyPreferences.getServiceTaxRegNumber()!= null ? extraCompanyPreferences.getServiceTaxRegNumber() : "");
                summaryData.put(CustomDesignerConstants.COMPANY_SERVICE_TAX_REG_NO, extraCompanyPreferences.getServiceTaxRegNo()!= null ? extraCompanyPreferences.getServiceTaxRegNo() : "");
                summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, invoice.getCustomer().getGSTIN() != null ? invoice.getCustomer().getGSTIN() : "");
            }
            String systemcurrencysymbol = invoice.getCustomer().getCurrency() != null ? invoice.getCustomer().getCurrency().getSymbol() : "";
            String customcurrencysymbol = accCommonTablesDAO.getCustomCurrencySymbol(systemcurrencysymbol, companyid);//Take custom currency symbol
            summaryData.put(CustomDesignerConstants.CompanyBankIFSCCode, IFSCCode);
            summaryData.put(CustomDesignerConstants.CompanyBankAccountNumber, accountNumber);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, authHandler.formattedAmount(totalAmount, companyid));
            summaryData.put(CustomDesignerConstants.totalAmountForServiceTaxInvoice, authHandler.formattedAmount(totalAmountForServiceTaxInvoice, companyid));
            summaryData.put(CustomDesignerConstants.swacchaBharatCessForServiceTaxInvoice, authHandler.formattedAmount(swacchaBharatCessForServiceTaxInvoice, companyid));
            summaryData.put(CustomDesignerConstants.serviceTaxRateForServiceTaxInvoice, authHandler.formattedAmount(serviceTaxRateForServiceTaxInvoice, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
            if(invoice.isGstIncluded()){
                summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, authHandler.formattedAmount(subTotalwithDiscount, companyid));
                summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, authHandler.formattedAmount(subTotalwithTax, companyid)); //ERP-25162
            } else{
                summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, authHandler.formattedAmount(subTotalwithDiscount, companyid));
                summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, authHandler.formattedAmount(subTotalwithTax, companyid)); //ERP-25162
            }
            summaryData.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, authHandler.formattedAmount((subTotalwithDiscount + totalTermAmount), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, authHandler.formattedAmount(totalDiscount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, authHandler.formattedAmount(totaltax, companyid));
            summaryData.put(CustomDesignerConstants.SummaryTaxPercent, taxPercent);
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword + " Only.");
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsDescription_fieldTypeId, term);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, termsname);
            summaryData.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, QouteRef);
            summaryData.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, SOref);
            summaryData.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, DOref);
            summaryData.put(CustomDesignerConstants.CustomDesignDORef_Date_fieldTypeId, DOdate);
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
            summaryData.put(CustomDesignerConstants.CustomerVendor_Term, invoice.getCustomer().getCreditTerm() != null ? (invoice.getCustomer().getCreditTerm().getTermname() != null ? invoice.getCustomer().getCreditTerm().getTermname() : "") : "");
            summaryData.put(CustomDesignerConstants.CustomerVendor_AccountCode, invoice.getCustomer().getAccount() != null ? (invoice.getCustomer().getAccount().getAcccode() != null ? invoice.getCustomer().getAccount().getAcccode() : "") : "");
            summaryData.put(CustomDesignerConstants.Customer_AccountNo_fieldTypeId, invoice.getCustomer().getAccount() != null ? (invoice.getCustomer().getAccount().getAccountName() != null ? invoice.getCustomer().getAccount().getAccountName() : "") : "");
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorAlice_Name, invoice.getCustomer() != null ? !StringUtil.isNullOrEmpty(invoice.getCustomer().getAliasname()) ? invoice.getCustomer().getAliasname() : "" : "");
            summaryData.put(CustomDesignerConstants.InvoiceAmountDue, authHandler.formattedAmount(amountDue, companyid));
            summaryData.put(CustomDesignerConstants.AmountDueInWords, amountDueinword+ " Only.");
            summaryData.put(CustomDesignerConstants.PaymentReceivedInWords, paymentreceivedinword+ " Only.");
            summaryData.put(CustomDesignerConstants.InvoicePaymentReceived, authHandler.formattedAmount(paymentreceived, companyid));
            summaryData.put(CustomDesignerConstants.CustomerVendor_Code, invoice.getCustomer().getAcccode() != null ? invoice.getCustomer().getAcccode() : "");
            summaryData.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, customcurrencysymbol);
            summaryData.put(CustomDesignerConstants.BillTo, billAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.ShipTo, shipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            
            /*Exchanged Rate Overall */
            summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, 1);
            
            //GST Exchange Rate
            if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !preference.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                summaryData.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, invoice.getGstCurrencyRate() != 0.0 ? invoice.getGstCurrencyRate() : "");
            } else {
                summaryData.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "");
            }
            
            if(isPartialInvoice){
                summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, authHandler.formattedAmount((totalAmount * revExchangeRate), companyid));
                summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, authHandler.formattedAmount((subtotal * revExchangeRate), companyid));
                summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, authHandler.formattedAmount((totaltax * revExchangeRate), companyid));
                summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, authHandler.formattedAmount((subTotalwithDiscount * revExchangeRate), companyid));
                summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, authHandler.formattedAmount((totalTermAmount * revExchangeRate), companyid));
                
                summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, authHandler.formattedAmount((totalAmount * revExchangeRate), companyid));
                summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, authHandler.formattedAmount((subtotal * revExchangeRate), companyid));
                summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, authHandler.formattedAmount((totaltax * revExchangeRate), companyid));
                summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, authHandler.formattedAmount((subTotalwithDiscount * revExchangeRate), companyid));
                summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, authHandler.formattedAmount((totalTermAmount * revExchangeRate), companyid));
            } else{
                summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTotalAmount, companyid));
                summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateSubTotal, companyid));
                summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTotalTax, companyid));
                summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateSubTotalwithDiscount, companyid));
                summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTermAmount, companyid));   
                
                summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, authHandler.formattedAmount(globalLevelExchangedRateTotalAmount, companyid));
                summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, authHandler.formattedAmount(globalLevelExchangedRateSubTotal, companyid));
                summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, authHandler.formattedAmount(globalLevelExchangedRateTotalTax, companyid));
                summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, authHandler.formattedAmount(globalLevelExchangedRateSubTotalwithDiscount, companyid));             
                summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, authHandler.formattedAmount(globalLevelExchangedRateTermAmount, companyid));
            }
            
            summaryData.put(CustomDesignerConstants.Createdby, createdby);
            summaryData.put(CustomDesignerConstants.Updatedby, updatedby);
            summaryData.put(CustomDesignerConstants.TotalAmountWithoutTerm, Amountwithoutterm);
            summaryData.put(CustomDesignerConstants.TotalQuantity, totalQuantity);
            summaryData.put(CustomDesignerConstants.Total_Quantity_UOM, authHandler.formattingDecimalForQuantity(totalQuantity, companyid) +" "+ uomForTotalQuantity);
            summaryData.put(CustomDesignerConstants.SalesOrderDate, SalesOrderDate != null ? authHandler.getUserDateFormatterJson(requestObj).format(SalesOrderDate) : "");
            summaryData.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, totalTermAmount);
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPreText_fieldTypeId, config == null ? "" : config.getPdfPreText());
            summaryData.put(CustomDesignerConstants.CustomDesignTemplate_Print, printedOn);
            summaryData.put(CustomDesignerConstants.CustomDesignTemplate_ItemsNo, rowcnt);
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.AllTerms, allTerms);
            summaryData.put(CustomDesignerConstants.AllTermNames, termsNameWithoutPercent);
            summaryData.put(CustomDesignerConstants.AllTermSigns, termssign);
            summaryData.put(CustomDesignerConstants.AllTermAmounts, termsamount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTax, allLineLevelTax);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxAmount, allLineLevelTaxAmount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxBasic, allLineLevelTaxBasic);
            summaryData.put(CustomDesignerConstants.AllLineLevelTermSigns, lineLevelTaxSign);
            summaryData.put(CustomDesignerConstants.CurrentUserFirstName, !StringUtil.isNullOrEmpty(user.getFirstName())?user.getFirstName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserLastName, !StringUtil.isNullOrEmpty(user.getLastName())?user.getLastName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName())?user.getFullName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserEmail, !StringUtil.isNullOrEmpty(user.getEmailID())?user.getEmailID():"");
            summaryData.put(CustomDesignerConstants.CurrentUserAddress, !StringUtil.isNullOrEmpty(user.getAddress())?user.getAddress().replaceAll(Constants.REGEX_LINE_BREAK, "<br>"):"");
            summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber())?user.getContactNumber():"");
            summaryData.put(CustomDesignerConstants.roundingDifference, roundingDiff == 0 ? "" : roundingDiff); //ERP-25876
            summaryData.put(CustomDesignerConstants.CUSTOMER_TITLE, customerTitle);
            summaryData.put(CustomDesignerConstants.SUBTOTAL_BEFORE_PARTIAL_PAYMENT, subTotalBeforePartialPayment);
            summaryData.put(CustomDesignerConstants.SUBTOTAL_AND_TAX_BEFORE_PARTIAL_PAYMENT, subTotalBeforePartialPayment + totalTaxAmountBeforePartialPayment);
            summaryData.put(CustomDesignerConstants.TOTAL_TAX_BEFORE_PARTIAL_PAYMENT, totalTaxAmountBeforePartialPayment);
            summaryData.put(CustomDesignerConstants.TOTAL_BEFORE_PARTIAL_PAYMENT, subTotalBeforePartialPayment + totalTaxAmountBeforePartialPayment + totalTermAmount);
            summaryData.put(CustomDesignerConstants.REMAINING_BALANCE_DUE, (subTotalBeforePartialPayment + totalTaxAmountBeforePartialPayment + totalTermAmount) - totalAmount);
            summaryData.put(CustomDesignerConstants.INVOICE_CREATION_DATE, invoiceCreationDateStr);
            summaryData.put(CustomDesignerConstants.INVOICE_UPDATION_DATE, invoiceUpdationDateStr);
            summaryData.put(CustomDesignerConstants.CustomJobWorkOrderDate, jobworkorderdateStr);
            summaryData.put(CustomDesignerConstants.CustomJobWorkOrderNo, jobworkorderno);
            summaryData.put(CustomDesignerConstants.CustomJobWorkInNo, jobIN);
            summaryData.put(CustomDesignerConstants.SHIPMENT_TRACKING_NO, !StringUtil.isNullOrEmpty(shipmentTrackingNo)? shipmentTrackingNo : "");  
            summaryData.put(CustomDesignerConstants.GSTAmountInWords, gstAmountInWords + Constants.ONLY);
            summaryData.put(CustomDesignerConstants.CountOfBatches, countOfBatches);
            
            int deliveryDate = 0;
            String deliveryDateVal = "";
            String driver = "";
            String vehicleNo = "";
            String deliveryTime = "";
            if (extraCompanyPreferences.isDeliveryPlanner()) {
                deliveryDate = invoice.getCustomer() != null ? invoice.getCustomer().getDeliveryDate():-1;
                deliveryDateVal = getDeliverDayVal(deliveryDate);
                deliveryTime = (invoice.getCustomer() != null && invoice.getCustomer().getDeliveryTime() != null) ? invoice.getCustomer().getDeliveryTime():"";
                driver = (invoice.getCustomer() != null && invoice.getCustomer().getDriver() != null)? invoice.getCustomer().getDriver().getValue():"";
                vehicleNo = (invoice.getCustomer() != null && invoice.getCustomer().getVehicleNo() != null)? invoice.getCustomer().getVehicleNo().getValue():""; 
            }
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, deliveryDateVal);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, deliveryTime);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDriver, driver);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo,vehicleNo);
            jArr.put(summaryData);
            // Dummay Data for Details table - Will be removed after testing done
//            JSONObject detailsTableData = new JSONObject();
//            JSONArray depositedDetailsTableDataArr = new JSONArray();
//            JSONArray unpresentedDetailsTableDataArr = new JSONArray();
//            
//            for(int ind = 1; ind <= 2; ind++){
//                JSONObject depositedDetailsTableData = new JSONObject();
//                depositedDetailsTableData.put(CustomDesignerConstants.SrNo, ind);
//                depositedDetailsTableData.put("Cheque Date", "09/08/2017");
//                depositedDetailsTableData.put("Cheque Number", "0123456789_"+ind);
//                depositedDetailsTableData.put("Vendor", "Ashish Mohite_"+ind);
//                depositedDetailsTableData.put("Amount", ind+"0000"+ind);
//                
//                depositedDetailsTableDataArr.put(depositedDetailsTableData);
//            }
//            detailsTableData.put("deposited", depositedDetailsTableDataArr);
//            for(int ind = 1; ind <= 2; ind++){
//                JSONObject unpresentedDetailsTableData = new JSONObject();
//                unpresentedDetailsTableData.put(CustomDesignerConstants.SrNo, ind);
//                unpresentedDetailsTableData.put("Cheque Date", "09/08/2017");
//                unpresentedDetailsTableData.put("Cheque Number", "0123456789_"+ind);
//                unpresentedDetailsTableData.put("Vendor", "Ashish Mohite_"+ind);
//                unpresentedDetailsTableData.put("Amount", ind+"0000"+ind);
//                
//                unpresentedDetailsTableDataArr.put(unpresentedDetailsTableData);
//            }
//            detailsTableData.put("unpresented", unpresentedDetailsTableDataArr);
//            detailsTableData.put("isDetailsTableData", true);
//            jArr.put(detailsTableData);
            // Dummay Data ends        
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
  
    /*
     * Function to get summary data for Credit Note
     */
  
    public JSONObject getCNSummaryData(JSONObject paramsJobj,Map<String,Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONObject summaryData = new JSONObject();
        try {
            String allLineLevelTax = "", allLineLevelTaxAmount = "", allLineLevelTaxBasic = "";
            String globallevelcustomfields = "", globalleveldimensions = "", billAddr = "", shipAddr = "";
            double bascurrencyaccountsubtotal = 0, bascurrencyaccounttotaltax = 0, basecurrencyaccounttotalamount = 0;
            double bascurrencyinvoicesubtotal = 0, bascurrencyinvoicetotaltax = 0, basecurrencyinvoicetotalamount = 0;

            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) requestParams.get("companyAccountPreferences");
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) requestParams.get("extraCompanyPreferences");
            PdfTemplateConfig config = paramsJobj.has("pdfConfig") ? (PdfTemplateConfig) requestParams.get("pdfConfig") : null;
            Set<String> lineLevelTaxesGST = (HashSet<String>) requestParams.get("lineLevelTaxesGST");
            Map<String, Object> lineLevelTaxAmountGST = (Map<String, Object>) requestParams.get("lineLevelTaxAmountGST");
            Map<String, Object> lineLevelTaxBasicGST = (Map<String, Object>) requestParams.get("lineLevelTaxBasicGST");
            Map<String, Object> lineLevelTaxNames = (Map<String, Object>) requestParams.get("lineLevelTaxNames");

            CreditNote creditNote = (CreditNote) requestParams.get("creditNote");
            String companyid = paramsJobj.getString("companyid");
            String currencyid = (creditNote.getCurrency() == null) ? "" : creditNote.getCurrency().getCurrencyID();
            int countryid = 0;
            if (extraCompanyPreferences.getCompany() != null && extraCompanyPreferences.getCompany().getCountry() != null && !StringUtil.isNullOrEmpty(extraCompanyPreferences.getCompany().getCountry().getID())) {
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }

            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            double externalcurrency = creditNote.getExternalCurrencyRate();
            double revExchangeRate = 0.0;
            if (externalcurrency != 0) {
                revExchangeRate = 1 / externalcurrency;
            }
            double accountsubtotal = requestParams.containsKey("accountsubtotal") ? (Double)requestParams.get("accountsubtotal"):0;
            double accounttotaltax = requestParams.containsKey("accounttotaltax") ? (Double)requestParams.get("accounttotaltax"):0;
            double accounttotalamount = requestParams.containsKey("accounttotalamount") ? (Double)requestParams.get("accounttotalamount"):0;
            double invoicesubtotal = requestParams.containsKey("invoicesubtotal") ? (Double)requestParams.get("invoicesubtotal"):0;
            double invoicetotaltax = requestParams.containsKey("invoicetotaltax") ? (Double)requestParams.get("invoicetotaltax"):0;
            double invoicetotalamount = requestParams.containsKey("invoicetotalamount") ? (Double)requestParams.get("invoicetotalamount"):0;
            double totalwithtax = requestParams.containsKey("totalwithtax") ? (Double)requestParams.get("totalwithtax"):0;
            String GSTExchangeRate = requestParams.containsKey("GSTExchangeRate") ? (String)requestParams.get("GSTExchangeRate"):"";

            String templateSubtype = requestParams.containsKey("templateSubtype") ? (String)requestParams.get("templateSubtype"):"";

            /**
             * get customer/vendor title (Mr./Mrs.)
             */
            String customerOrVendorTitle = "", VATTInnumber = "", CSTTInNumber = "", custOrVendorPanNumber = "", gstin = "";
            if (creditNote.getCustomer() != null) {
                customerOrVendorTitle = creditNote.getCustomer().getTitle();
                VATTInnumber = creditNote.getCustomer().getVATTINnumber() != null ? creditNote.getCustomer().getVATTINnumber() : "";
                CSTTInNumber = creditNote.getCustomer().getCSTTINnumber() != null ? creditNote.getCustomer().getCSTTINnumber() : "";
                custOrVendorPanNumber = creditNote.getCustomer().getPANnumber() != null ? creditNote.getCustomer().getPANnumber() : "";
                gstin = creditNote.getCustomer().getGSTIN() != null ? creditNote.getCustomer().getGSTIN() : "";
            } else if (creditNote.getVendor() != null) {
                customerOrVendorTitle = creditNote.getVendor().getTitle();
                VATTInnumber = creditNote.getVendor().getVATTINnumber() != null ? creditNote.getVendor().getVATTINnumber() : "";
                CSTTInNumber = creditNote.getVendor().getCSTTINnumber() != null ? creditNote.getVendor().getCSTTINnumber() : "";
                custOrVendorPanNumber = creditNote.getVendor().getPANnumber() != null ? creditNote.getVendor().getPANnumber() : "";
                gstin = creditNote.getVendor().getGSTIN() != null ? creditNote.getVendor().getGSTIN() : "";
            }

            if (!StringUtil.isNullOrEmpty(customerOrVendorTitle)) {
                KwlReturnObject masterItemResult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), customerOrVendorTitle);
                MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                customerOrVendorTitle = masterItem.getValue();
            }

            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyUEN_fieldTypeId, companyAccountPreferences.getCompanyUEN() != null ? companyAccountPreferences.getCompanyUEN() : "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyGRN_No_fieldTypeId, companyAccountPreferences.getGstNumber() != null ? companyAccountPreferences.getGstNumber() : "");

            if(extraCompanyPreferences.isIsNewGST()){ // for New gst check 
                if(!lineLevelTaxNames.isEmpty()){
                    Iterator lineTax = lineLevelTaxNames.entrySet().iterator();
                    while(lineTax.hasNext()){
                        Map.Entry tax = (Map.Entry)lineTax.next();
                        allLineLevelTax += tax.getKey();
                        allLineLevelTax += "!##";
                        double taxamount = (double)tax.getValue();
                        allLineLevelTaxAmount += tax.getValue().toString();
                        allLineLevelTaxAmount += "!##";
                    }
                }
                if(!StringUtil.isNullOrEmpty(allLineLevelTax)){
                    allLineLevelTax = allLineLevelTax.substring(0, allLineLevelTax.length()-3);
                }
                if(!StringUtil.isNullOrEmpty(allLineLevelTaxAmount)){
                    allLineLevelTaxAmount = allLineLevelTaxAmount.substring(0, allLineLevelTaxAmount.length()-3);
                }
            } else {
                /*
                 * Putting all line taxes and its information in summary data
                 * separated by !##
                 */
                for (String key : lineLevelTaxesGST) {
                    allLineLevelTax += key + "!##";
                    allLineLevelTaxAmount += lineLevelTaxAmountGST.get(key).toString() + "!##";
                    allLineLevelTaxBasic += lineLevelTaxBasicGST.get(key).toString() + "!##";
                }
            }
            summaryData.put(CustomDesignerConstants.AllLineLevelTax, allLineLevelTax);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxAmount, allLineLevelTaxAmount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxBasic, allLineLevelTaxBasic);

            /*
             * All Global Section Custom Field and DImensions
             */
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            DateFormat df = authHandler.getUserDateFormatterJson(paramsJobj);//User Date Formatter
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put(Constants.billid, creditNote.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }

            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            //Details like company details,base currency
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);


            //Base Currency values in Subtotal, Total Tax and Total Amount  for Account
            if (externalcurrency != 0) {
                bascurrencyaccountsubtotal = accountsubtotal * revExchangeRate;  //Base currency account subtotal
                bascurrencyaccounttotaltax = accounttotaltax * revExchangeRate;//Base currency account total tax
                basecurrencyaccounttotalamount = accounttotalamount * revExchangeRate;//Base currency account total amount
            } else {
                bascurrencyaccountsubtotal = accountsubtotal;
                bascurrencyaccounttotaltax = accounttotaltax;
                basecurrencyaccounttotalamount = accounttotalamount;
            }

            //Base Currency values in Subtotal, Total Tax and Total Amount  for Invoice
            if (externalcurrency != 0) {
                bascurrencyinvoicesubtotal = invoicesubtotal * revExchangeRate;  //Base currency account subtotal
                bascurrencyinvoicetotaltax = invoicetotaltax * revExchangeRate;//Base currency account total tax
                basecurrencyinvoicetotalamount = invoicetotalamount * revExchangeRate;//Base currency account total amount
            } else {
                bascurrencyinvoicesubtotal = invoicesubtotal;
                bascurrencyinvoicetotaltax = invoicetotaltax;
                basecurrencyinvoicetotalamount = invoicetotalamount;
            }

            if (creditNote.getCustomer() != null) {
                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put(Constants.customerid, creditNote.getCustomer().getID());
                addrRequestParams.put(Constants.companyKey, companyid);
                KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
                List<AddressDetails> addressResultList = addressResult.getEntityList();
                CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
                addrRequestParams.put("isBillingAddress", true);
                addrRequestParams.put("isDefaultAddress", true);
                addrRequestParams.put("isSeparator", true);
                billAddr = accountingHandlerDAOobj.getCustomerAddress(addrRequestParams);
                addrRequestParams.put("isBillingAddress", false);
                shipAddr = accountingHandlerDAOobj.getCustomerAddress(addrRequestParams);
                summaryData.put(CustomDesignerConstants.CustomerVendor_Term, creditNote.getCustomer().getCreditTerm() != null ? creditNote.getCustomer().getCreditTerm().getTermdays() + " Days" : "0 Days");
            } else {
                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put("vendorid", creditNote.getVendor().getID());
                addrRequestParams.put(Constants.companyKey, companyid);
                KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                List<AddressDetails> addressResultList = addressResult.getEntityList();
                CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
                addrRequestParams.put("isBillingAddress", true);
                addrRequestParams.put("isDefaultAddress", true);
                addrRequestParams.put("isSeparator", true);
                billAddr = accountingHandlerDAOobj.getVendorAddress(addrRequestParams);
                addrRequestParams.put("isBillingAddress", false);
                shipAddr = accountingHandlerDAOobj.getVendorAddress(addrRequestParams);
                summaryData.put(CustomDesignerConstants.CustomerVendor_Term, creditNote.getVendor().getDebitTerm() != null ? creditNote.getVendor().getDebitTerm().getTermdays() + " Days" : "0 Days");
            }

            String creditterm = creditNote.getCustomer() == null ? String.valueOf(creditNote.getVendor().getDebitTerm().getTermdays()) : String.valueOf(creditNote.getCustomer().getCreditTerm().getTermdays());
            String netCreditTerm = creditNote.getCustomer() == null ? String.valueOf(creditNote.getVendor().getDebitTerm().getTermname()) : String.valueOf(creditNote.getCustomer().getCreditTerm().getTermname());
            String custaccountcodeno = creditNote.getCustomer() == null ? (creditNote.getVendor().getAccount().getAcccode() != null ? creditNote.getVendor().getAccount().getAcccode() : "") : (creditNote.getCustomer().getAccount().getAcccode() != null ? creditNote.getCustomer().getAccount().getAcccode() : "");
            String custAccountNo = creditNote.getCustomer() == null ? (creditNote.getVendor().getAccount().getAccountName() != null ? creditNote.getVendor().getAccount().getAccountName() : "") : (creditNote.getCustomer().getAccount().getAccountName() != null ? creditNote.getCustomer().getAccount().getAccountName() : "");
            String customervendorcode = creditNote.getCustomer() == null ? (creditNote.getVendor().getAcccode() != null ? creditNote.getVendor().getAccount().getAcccode() : "") : (creditNote.getCustomer().getAcccode() != null ? creditNote.getCustomer().getAcccode() : "");
            boolean isIncludeGST = creditNote.isIncludingGST();

            summaryData.put(CustomDesignerConstants.Createdby, creditNote.getCreatedby() != null ? creditNote.getCreatedby().getFullName() : "");
            summaryData.put(CustomDesignerConstants.Updatedby, creditNote.getModifiedby() != null ? creditNote.getModifiedby().getFullName() : "");
            summaryData.put(CustomDesignerConstants.CNDN_InvoiceNo_fieldTypeId, requestParams.containsKey("invoiceNos") ? (String)requestParams.get("invoiceNos"):"");
            summaryData.put(CustomDesignerConstants.CNDN_InvoiceAmount_fieldTypeId, requestParams.containsKey("invAmounts") ? (String)requestParams.get("invAmounts"):"");
            summaryData.put(CustomDesignerConstants.CNDN_InvoiceTax_fieldTypeId, requestParams.containsKey("invTaxAmounts") ? (String)requestParams.get("invTaxAmounts"):"");
            summaryData.put(CustomDesignerConstants.InvoiceTax, requestParams.containsKey("invTaxAmounts") ? (String)requestParams.get("invTaxAmounts"):"");
            summaryData.put(CustomDesignerConstants.CNDN_InvoiceAmountDue_fieldTypeId, requestParams.containsKey("invAmountDues") ? (String)requestParams.get("invAmountDues"):"");
            summaryData.put(CustomDesignerConstants.CNDN_InvoiceEnterAmount_fieldTypeId, requestParams.containsKey("invEnterAmounts") ? (String)requestParams.get("invEnterAmounts"):"");
            summaryData.put(CustomDesignerConstants.CNDN_INvoiceDates_fieldTypeId, requestParams.containsKey("invoicedates") ? (String)requestParams.get("invoicedates"):"");

            summaryData.put(CustomDesignerConstants.CNDN_AccountCode_fieldTypeId, requestParams.containsKey("accCodes") ? (String)requestParams.get("accCodes"):"");
            summaryData.put(CustomDesignerConstants.CNDN_Account_fieldTypeId, requestParams.containsKey("accNames") ? (String)requestParams.get("accNames"):"");
            summaryData.put(CustomDesignerConstants.CNDN_AccountAmount_fieldTypeId, requestParams.containsKey("accAmounts") ? (String)requestParams.get("accAmounts"):"");
            summaryData.put(CustomDesignerConstants.CNDN_AccountTax_fieldTypeId, requestParams.containsKey("accTaxNames") ? (String)requestParams.get("accTaxNames"):"");
            summaryData.put(CustomDesignerConstants.CNDN_AccountTaxAmount_fieldTypeId, requestParams.containsKey("accTaxAmounts") ? (String)requestParams.get("accTaxAmounts"):"");
            summaryData.put(CustomDesignerConstants.CNDN_AccountTotalAmount_fieldTypeId, requestParams.containsKey("accAmountWithTaxes") ? (String)requestParams.get("accAmountWithTaxes"):"");
            summaryData.put(CustomDesignerConstants.CNDN_AccountDescription_fieldTypeId, requestParams.containsKey("accDescriptions") ? (String)requestParams.get("accDescriptions"):"");
            summaryData.put(CustomDesignerConstants.SummaryTaxPercent, requestParams.containsKey("accTaxPercent") ? (String)requestParams.get("accTaxPercent"):"");
            summaryData.put(CustomDesignerConstants.CNDN_AccountReason_fieldTypeId, requestParams.containsKey("accReason") ? (String)requestParams.get("accReason"):"");
            summaryData.put(CustomDesignerConstants.CNDN_AccountAmountExcludeGST_fieldTypeId, requestParams.containsKey("accAmountExcludeGST") ? (String)requestParams.get("accAmountExcludeGST"):"");

            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, String.valueOf(authHandler.formattedAmount(requestParams.containsKey("subTotal") ? (Double)requestParams.get("subTotal"):0, companyid)));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, String.valueOf(authHandler.formattedAmount(requestParams.containsKey("totaltax") ? (Double)requestParams.get("totaltax"):0, companyid)));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, String.valueOf(authHandler.formattedAmount(totalwithtax, companyid)));
            
            if (templateSubtype.equals(CustomDesignerConstants.OVERCHARGE_SUBTYPE) || templateSubtype.equals(CustomDesignerConstants.UNDERCHARGE_SUBTYPE)) {
                summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, String.valueOf(authHandler.formattedAmount((requestParams.containsKey("subTotalWithDiscount") ? (Double)requestParams.get("subTotalWithDiscount"):0), companyid)));
                summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, String.valueOf(authHandler.formattedAmount((requestParams.containsKey("subTotalWithTax") ? (Double)requestParams.get("subTotalWithTax"):0), companyid)));
                summaryData.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, String.valueOf(authHandler.formattedAmount((requestParams.containsKey("totalDiscount") ? (Double)requestParams.get("totalDiscount"):0), companyid)));
            }

            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
            String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalwithtax)), currency, countryLanguageId);
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if (countryid == Constants.INDONESIAN_COUNTRY_ID) {
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(totalwithtax)), indoCurrency);
            }


            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword + " Only.");
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, creditterm);
            summaryData.put(CustomDesignerConstants.CustomDesignNetCreditTerm_fieldTypeId, netCreditTerm);
            summaryData.put(CustomDesignerConstants.Customer_AccountNo_fieldTypeId, custAccountNo);
            summaryData.put(CustomDesignerConstants.CustomerVendoraccountcode, custaccountcodeno);
            summaryData.put(CustomDesignerConstants.CustomerVendorCode, customervendorcode);
            summaryData.put(CustomDesignerConstants.SrNo, 1);
            summaryData.put(CustomDesignerConstants.BillTo, billAddr);
            summaryData.put(CustomDesignerConstants.ShipTo, shipAddr);
            summaryData.put(CustomDesignerConstants.AccountSubTotal, authHandler.formattedAmount(accountsubtotal, companyid));//Account Sub Total
            summaryData.put(CustomDesignerConstants.AccountTotalTax, authHandler.formattedAmount(accounttotaltax, companyid)); //Account Total Tax
            summaryData.put(CustomDesignerConstants.AccountTotalAmount, authHandler.formattedAmount(accounttotalamount, companyid)); //Account Total Amount

            summaryData.put(CustomDesignerConstants.BaseCurrencyAccountSubTotal, authHandler.formattedAmount(bascurrencyaccountsubtotal, companyid));//Account Sub Total
            summaryData.put(CustomDesignerConstants.BaseCurrencyAccountTotalTax, authHandler.formattedAmount(bascurrencyaccounttotaltax, companyid)); //Account Total Tax
            summaryData.put(CustomDesignerConstants.BaseCurrencyAccountTotalAmount, authHandler.formattedAmount(basecurrencyaccounttotalamount, companyid)); //Account Total Amount
            summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);

            //GST Exchange Rate
            if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !companyAccountPreferences.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                summaryData.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, !StringUtil.isNullOrEmpty(GSTExchangeRate) ? GSTExchangeRate : "");
            } else {
                summaryData.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "");
            }

            summaryData.put(CustomDesignerConstants.InvoiceSubTotal, authHandler.formattedAmount(invoicesubtotal, companyid));//Invoice SubTotal
            summaryData.put(CustomDesignerConstants.InvoiceTotalTax, authHandler.formattedAmount(invoicetotaltax, companyid)); //Invoice Total Tax
            summaryData.put(CustomDesignerConstants.InvoiceTotalAmount, authHandler.formattedAmount(invoicetotalamount, companyid)); //Invoice Total Amount
            //Sub-Total, Total tax, And Total Amount in Base currency for Invoice
            summaryData.put(CustomDesignerConstants.BaseCurrencyInvoiceSubTotal, authHandler.formattedAmount(bascurrencyinvoicesubtotal, companyid));//Base Currency Invoice SubTotal
            summaryData.put(CustomDesignerConstants.BaseCurrencyInvoiceTotalTax, authHandler.formattedAmount(bascurrencyinvoicetotaltax, companyid)); //Base Currency Invoice Total Tax
            summaryData.put(CustomDesignerConstants.BaseCurrencyInvoiceTotal, authHandler.formattedAmount(basecurrencyinvoicetotalamount, companyid)); //Base Currency Invoice Total Amount
            summaryData.put(CustomDesignerConstants.CNDN_InvoiceSalesPerson_fieldTypeId, requestParams.containsKey("salespersonvalue") ? (String)requestParams.get("salespersonvalue"):"");
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.AllDimensions, "");
            summaryData.put(CustomDesignerConstants.AllLinelevelCustomFields, "");
            summaryData.put(CustomDesignerConstants.CUSTOMER_OR_VENDOR_TITLE, customerOrVendorTitle);
            summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, VATTInnumber);
            summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, CSTTInNumber);
            summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber() != null ? extraCompanyPreferences.getVatNumber() : "");
            summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber() != null ? extraCompanyPreferences.getCstNumber() : "");
            summaryData.put(CustomDesignerConstants.CompanyPANNumber, extraCompanyPreferences.getPanNumber() != null ? extraCompanyPreferences.getPanNumber() : "");
            summaryData.put(CustomDesignerConstants.CUSTOMER_PAN_NO, custOrVendorPanNumber);
            int deliveryDate = 0;
            String deliveryDateVal = "";
            String driver = "";
            String vehicleNo = "";
            String deliveryTime = "";
            if (extraCompanyPreferences.isDeliveryPlanner()) {
                deliveryDate = creditNote.getCustomer() != null ? creditNote.getCustomer().getDeliveryDate() : -1;
                deliveryDateVal = getDeliverDayVal(deliveryDate);
                deliveryTime = (creditNote.getCustomer() != null && creditNote.getCustomer().getDeliveryTime() != null) ? creditNote.getCustomer().getDeliveryTime() : "";
                driver = (creditNote.getCustomer() != null && creditNote.getCustomer().getDriver() != null) ? creditNote.getCustomer().getDriver().getValue() : "";
                vehicleNo = (creditNote.getCustomer() != null && creditNote.getCustomer().getVehicleNo() != null) ? creditNote.getCustomer().getVehicleNo().getValue() : "";
            }
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, deliveryDateVal);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, deliveryTime);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDriver, driver);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo, vehicleNo);
            summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, gstin);
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
//        jArr.put(summaryData);
        return summaryData;
    }
    
    /**
     * Get DetailJSON For Job Order Flow in document designer
     * @return HashMap of string id as key and JSONArray as value
     * @Author Ashish Mohite
     */
    public HashMap<String, JSONArray> getSIDetailsJobOrderFlowItemJSON(JSONObject requestObj, String invoiceID, HashMap<String, Object> paramMap) {
        HashMap<String, JSONArray>  itemDataSO = new HashMap<String, JSONArray>();
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            HashMap<String, Integer> ProductLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.productLevelCustomFieldMap);
            String companyid = requestObj.optString(Constants.companyKey);
            JSONArray recordids = new JSONArray();
            String createdby = "", customerName = "";
            String templateSubType = requestObj.optString("templatesubtype", "0");
            
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceID);
            Invoice invoice = (Invoice) objItr.getEntityList().get(0);
            
            createdby = invoice.getCreatedby().getFullName();
            customerName = invoice.getCustomer().getName();
            
            for(InvoiceDetail invDetailsRow : invoice.getRows()){
                if(invDetailsRow.isJobOrderItem()){
                    DateFormat df = authHandler.getDateOnlyFormat();
                    JSONArray jArr = new JSONArray();
                    JSONObject summaryData = new JSONObject();

                    summaryData.put("summarydata", true);
                    // Product level data
                    summaryData.put(CustomDesignerConstants.IN_ProductCode, invDetailsRow.getInventory().getProduct().getProductid());
                    summaryData.put(CustomDesignerConstants.ProductName, invDetailsRow.getInventory().getProduct().getProductName());
                    summaryData.put(CustomDesignerConstants.ProductDescription, invDetailsRow.getDescription());
                    String baseUrl = URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                    /*
                    * Following code is to check whether the image is predent for product or not. 
                    * If Image is not present sent s.gif instead of product id
                    */
                    String fileName = null;
                    fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+invDetailsRow.getInventory().getProduct().getID() + ".png";
                    File file = new File(fileName);
                    FileInputStream in = null; 
                    String filePathString = "";
                    try {
                        in = new FileInputStream(file);
                    } catch(java.io.FileNotFoundException ex) {
                        //catched exception if file not found, and to continue for rest of the products
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    if(in != null){
                        filePathString = baseUrl + "productimage?fname=" + invDetailsRow.getInventory().getProduct().getID() + ".png&isDocumentDesignerPrint=true";
                    } else{
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    summaryData.put(CustomDesignerConstants.imageTag, "<img src=\""+filePathString+"\" width='100' height='100' />");
                    // Global level data
                    summaryData.put(CustomDesignerConstants.Createdby, createdby);
                    summaryData.put(CustomDesignerConstants.CUSTOMER_NAME, customerName);
                    summaryData.put(CustomDesignerConstants.SALES_INVOICE_NO, invoice.getInvoiceNumber());
//                    summaryData.put(CustomDesignerConstants.InvoiceDate, invoice.getJournalEntry() != null ? df.format(invoice.getJournalEntry().getEntryDate()) : "");
                    summaryData.put(CustomDesignerConstants.InvoiceDate, invoice.getJournalEntry() != null ? df.format(invoice.getCreationDate()) : "");
                    summaryData.put(CustomDesignerConstants.SHIP_DATE, invoice.getShipDate() != null ? df.format(invoice.getShipDate()) : "");

                    /*
                    * All Global Section Custom Field and DImensions
                    */
                    HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                    HashMap<String, Object> extraparams = new HashMap<String, Object>();
                    df = authHandler.getUserDateFormatterJson(requestObj);//User Date Formatter
                    extraparams.put(Constants.companyid, companyid);
                    extraparams.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                    extraparams.put(Constants.customcolumn, 0);
                    extraparams.put(Constants.customfield, 1);
                    extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                    extraparams.put("billid", invoice.getID());
                    returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);

                    if (returnvalues.containsKey("summaryData")) {
                        summaryData = (JSONObject) returnvalues.get("summaryData");
                    }

                    /*
                     * get custom line data
                     */
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                    Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                    Detailfilter_params.add(invDetailsRow.getID());
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accInvoiceDAOobj.getInvoiceDetailsCustomData(invDetailRequestParams);
                    AccJEDetailCustomData jeDetailCustom = null;
                    if (idcustresult.getEntityList().size() > 0) {
                        jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                        ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, jeDetailCustom, FieldMap, summaryData, kwlCommonTablesDAOObj, variableMap);
                    }

                    /*Product Level Custom Fields Evaluation*/
                    invDetailRequestParams.clear();
                    Detailfilter_names.add("productId");
                    Detailfilter_params.add(invDetailsRow.getInventory().getProduct().getID());
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresultForProduct = accInvoiceDAOobj.getInvoiceDetailsCustomDataForProductNew(invDetailRequestParams);
                    AccJEDetailsProductCustomData accJEDetailsProductCustomData = null;
                    String customDataID = "";
                    if (idcustresultForProduct.getEntityList().size() > 0) {
                        customDataID = idcustresultForProduct.getEntityList().get(0).toString();
                        accJEDetailsProductCustomData =  (AccJEDetailsProductCustomData)  kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.AccJEDetailsProductCustomData", customDataID);
                    }
                    replaceFieldMap = new HashMap<String, String>();
                    if (accJEDetailsProductCustomData != null) {
                        ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, accJEDetailsProductCustomData, ProductLevelCustomFieldMap, summaryData, kwlCommonTablesDAOObj, variableMap);
                    }

                    /*Set All Line level Dimension & All LIne level Custom Field Values*/   
                    summaryData=CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap,variableMap,summaryData,false);//for dimensions
                    summaryData=CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap,variableMap,summaryData,true);//for customfields
                
                    if(jeDetailCustom != null && templateSubType.equals(Constants.SUBTYPE_JOB_ORDER_LABEL)){ // for Job Order Label Flow
                        if(FieldMap.containsKey("Custom_Size")){// If Size custom field present
                            String sizeValue = jeDetailCustom.getCol(FieldMap.get("Custom_Size"));
                            String[] sizeArr = sizeValue.split("\\n"); // Split sizes
                            for(int ind = 0; ind < sizeArr.length; ind++){
                                jArr = new JSONArray();
                                String size = sizeArr[ind];
                                JSONObject newSummaryData = new JSONObject(summaryData.toString());

                                newSummaryData.put("col"+FieldMap.get("Custom_Size"), size);
                                newSummaryData.put("Custom_Size", size);

                                jArr.put(newSummaryData);
                                itemDataSO.put(invoiceID+"_"+invDetailsRow.getSrno()+""+ind, jArr);
                                recordids.put(invoiceID+"_"+invDetailsRow.getSrno()+""+ind);
                            }
                        } else{// If no Size custom field present
                            jArr.put(summaryData);
                            itemDataSO.put(invoiceID+"_"+invDetailsRow.getSrno(), jArr);
                            recordids.put(invoiceID+"_"+invDetailsRow.getSrno());
                        }
                    } else{ // for Job Order Flow
                        jArr.put(summaryData);
                        itemDataSO.put(invoiceID+"_"+invDetailsRow.getSrno(), jArr);
                        recordids.put(invoiceID+"_"+invDetailsRow.getSrno());
                    }
                    
                    //document currency
                    if (invoice != null && invoice.getCurrency() != null && !StringUtil.isNullOrEmpty(invoice.getCurrency().getCurrencyID())) {
                        summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, invoice.getCurrency().getCurrencyID());
                    }
                    
                    itemDataSO.put("recordids", recordids);// put all recordids
                }
            }
            
        } catch(Exception ex ) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return itemDataSO;
    }
    
    /*
     * Function to fetch product details for Overcharged CN
     */
    public JSONArray getOverchargeCNDetailsItemJSON(JSONObject requestJobj, Map<String, Object> summaryParams ) throws ServiceException {
        JSONArray jArr = new JSONArray();

        int rowcnt = 0;
        double totaltax = 0, subtotal = 0, subTotalWithDiscount = 0,totalDiscount=0;
        int amountdigitafterdecimal = 2;
        boolean isLocationForProduct = false, isbatchforproduct = false, isserialforproduct = false, isWarehouseForProduct = false, isPartialInvoice = false;

        Map<String, Double> lineLevelTaxNames = new LinkedHashMap<String, Double>();
        Set<String> lineLevelTaxesGST = new HashSet<String>();
        Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
        Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
        Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();


        try {
            CreditNote cn = (CreditNote) summaryParams.get("creditNote");

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestJobj);

            Set<CreditNoteAgainstVendorGst> rowsGst = cn.getRowsGst();
            InvoiceDetail row = null;

            DateFormat df1 = authHandler.getDateOnlyFormat();
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(requestJobj);//User Date Formatter

            CompanyAccountPreferences preference = (CompanyAccountPreferences) summaryParams.get("companyAccountPreferences");
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) summaryParams.get("extraCompanyPreferences");

            String companyid = requestJobj.optString(Constants.companyKey);
            List<PricingBandMasterDetail> list = CommonFunctions.getRRPFieldsForAllModulesLineItem(requestJobj, accountingHandlerDAOobj, accMasterItemsDAOobj);


            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
            }

            int countryid = 0;
            if (extraCompanyPreferences.getCompany() != null && extraCompanyPreferences.getCompany().getCountry() != null) {
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }

            String invoiceNos = "", invAmounts = "", invTaxAmounts = "", invAmountDues = "", invoicedates = "", invoiceSalesPerson = "";
            double invoicesubtotal = 0, invoicetotaltax = 0, invoicetotalamount = 0;

            // List to identify processed invoices while calculating summary data for CN
            List<String> processedInv = new ArrayList<String>();
            for (CreditNoteAgainstVendorGst detail : rowsGst) {
                if (detail.getInvoiceDetail() != null) {

                    row = detail.getInvoiceDetail();

                    rowcnt++;
                    /*
                     * Fetching Job Work Order Qty, Number and Date.
                     */
                    Inventory inv = row.getInventory();
                    Product prod = inv.getProduct();
                    Invoice invoice = row.getInvoice();

                    double externalCurrencyRate = invoice.getExternalCurrencyRate(), revExchangeRate = 0;

                    if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                        externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                    }

                    if (externalCurrencyRate != 0) {
                        revExchangeRate = 1 / externalCurrencyRate;
                    }


                    String uom = row.getInventory().getUom() == null ? (row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()) : row.getInventory().getUom().getNameEmptyforNA();
                    String challanDate = "";
                    JSONObject obj = new JSONObject();
                    Discount disc = row.getDiscount();
                    String rowTaxName = "", proddesc = "", discountname = "0";
                    double rate = 0, rowTaxPercent = 0, rowTaxAmt = 0, rowamountwithtax = 0, rowdiscountvalue = 0, rowamountwithouttax = 0;
                    double exchangerateunitprice = 0, exchangeratelineitemsubtotal = 0, exchangeratelineitemdiscount = 0, exchangeratelineitemamount = 0, exchangeratelineitemtax = 0, exchangeratelineitemsubtotalwithdiscount = 0;
                    isPartialInvoice = invoice.isPartialinv();

                    proddesc = StringUtil.isNullOrEmpty(row.getDescription()) ? (StringUtil.isNullOrEmpty(inv.getProduct().getDescription()) ? "" : inv.getProduct().getDescription()) : row.getDescription();
                    proddesc = StringUtil.DecodeText(proddesc);
                    obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr No
                    obj.put(CustomDesignerConstants.ProductName, prod.getName().replaceAll("\n", "<br>"));// productname
                    obj.put(CustomDesignerConstants.IN_ProductCode, prod.getProductid() == null ? "" : prod.getProductid());
                    obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>"));//Product Description
                    obj.put(CustomDesignerConstants.HSCode, (prod.getHSCode() != null) ? prod.getHSCode().replaceAll("\n", "<br>") : "");//get HSCode of ProductGrid
                    obj.put(CustomDesignerConstants.ProductBarcode, prod.getBarcode() == null ? "" : prod.getBarcode());//Product Bar Code
                    obj.put(CustomDesignerConstants.AdditionalDescription, prod.getAdditionalDesc() != null ? prod.getAdditionalDesc().replaceAll("\n", "<br>") : "");  //product Addtional description
                    //used to get the line level total amount
                    for (PricingBandMasterDetail pricingBandMasterDetailObj : list) {
                        if (pricingBandMasterDetailObj.getProduct().equals(prod.getID())) {
                            obj.put(CustomDesignerConstants.RRP, authHandler.formattingDecimalForAmount(pricingBandMasterDetailObj.getSalesPrice(), companyid));//RRP
                        }
                    }
                    KwlReturnObject productCategories = null;
                    productCategories = accProductObj.getProductCategoryForDetailsReport(prod.getID());
                    List productCategoryList = productCategories.getEntityList();
                    String cateogry = "";
                    Iterator catIte = productCategoryList.iterator();
                    while (catIte.hasNext()) {
                        ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                        String categoryName = pcm.getProductCategory() != null ? (!StringUtil.isNullOrEmpty(pcm.getProductCategory().getValue().toString()) ? pcm.getProductCategory().getValue().toString() : "") : "";
                        cateogry += categoryName + " ";
                    }
                    if (StringUtil.isNullOrEmpty(cateogry)) {
                        cateogry = "None";
                    }
                    obj.put("productCategory", cateogry);


                    double quantity = detail.getReturnQuantity();
                    rate = detail.getRate();
                    rate = authHandler.roundUnitPrice(rate, companyid);
                    rowamountwithouttax = rate * quantity;

                    if (isPartialInvoice) {
                        if (row.getPartamount() > 0) {
                            rowamountwithouttax = (rowamountwithouttax * row.getPartamount()) / 100;
                        }
                        obj.put(CustomDesignerConstants.PartialAmount, authHandler.formattingDecimalForAmount(row.getPartamount(), companyid) + "%");//Partial Amount
                    }

                    /*
                     * In include GST case calculations are in reverse order In
                     * other cases calculations are in forward order
                     */
                    double rowamountwithgst = 0;
                    double discountBeforePartial = 0.0;
                    double rowtaxamountincludeGST = 0.0;
                    if (row.getInvoice().isGstIncluded()) {//if gstincluded is the case
                        rowamountwithgst = row.getRateincludegst() * quantity;

                        rowdiscountvalue = (rowamountwithouttax * detail.getDiscount()) / 100;

                        obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.round(rowamountwithouttax, companyid));//Subtotal
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.round((rowamountwithgst - (rowamountwithgst * rowdiscountvalue) / 100 - row.getRowTaxAmount()), companyid));//Subtotal
                        subtotal += authHandler.round(rowamountwithouttax, companyid);
                        subTotalWithDiscount += authHandler.round(rowamountwithouttax - rowdiscountvalue, companyid);
                        /**
                         * Calculate amount before deducting partial payment
                         */
                        if (isPartialInvoice) {
                            double rowamtwithouttax = 0.0;
                            if (detail.getTax() != null) {
                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getCreationDate(), detail.getTax().getID());
                                double rowtaxpercent = (Double) perresult.getEntityList().get(0);
                                rowamtwithouttax = (rowamountwithgst * 100) / (100 + rowtaxpercent);
                                rowtaxamountincludeGST = rowamtwithouttax * rowtaxpercent / 100;
                            } else {
                                rowamtwithouttax = rowamountwithgst;
                            }
                            if (row.getDiscount() != null && row.getDiscount().isInPercent()) {
                                discountBeforePartial = row.getDiscount() != null ? ((rowamountwithgst - row.getRowTaxAmount()) * row.getDiscount().getDiscount()) / 100 : 0.0;
                            } else {
                                discountBeforePartial = row.getDiscount() != null ? row.getDiscount().getDiscountValue() : 0.0;
                            }
                            obj.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, authHandler.formattingDecimalForAmount((rowamtwithouttax - discountBeforePartial), companyid));//Subtotal
                        } else {
                            obj.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, authHandler.formattingDecimalForAmount((rowamountwithgst - (rowamountwithgst * rowdiscountvalue) / 100 - row.getRowTaxAmount()), companyid));//Subtotal
                        }
                    } else {
                        rowdiscountvalue = (rowamountwithouttax * detail.getDiscount()) / 100;

                        obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.round(rowamountwithouttax, companyid));//Subtotal
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.round((rowamountwithouttax - rowdiscountvalue), companyid));//Subtotal
                        subtotal += authHandler.round(rowamountwithouttax, companyid);//rounded becuase totaltax & amount are rounded and saved in db
                        subTotalWithDiscount += authHandler.round((rowamountwithouttax - rowdiscountvalue), companyid);
                        /**
                         * Calculate amount before deducting partial payment
                         */
                        if (isPartialInvoice) {
                            if (row.getDiscount() != null && row.getDiscount().isInPercent()) {
                                discountBeforePartial = row.getDiscount() != null ? ((rate * quantity) * row.getDiscount().getDiscount()) / 100 : 0.0;
                            } else {
                                discountBeforePartial = row.getDiscount() != null ? row.getDiscount().getDiscountValue() : 0.0;
                            }
                            obj.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, authHandler.formattingDecimalForAmount(((rate * quantity) - discountBeforePartial), companyid));//Subtotal
                        } else {
                            obj.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, authHandler.formattingDecimalForAmount((rowamountwithouttax - rowdiscountvalue), companyid));//Subtotal
                        }
                    }

                    /*
                     * Discount Section
                     */
//                    Discount discount = row.getDiscount();
//                    if (discount != null) {
//                        if (discount.isInPercent()) {
//                            double discountpercent = row.getDiscount().getDiscount();
//                            discountname = (long) discountpercent == discountpercent ? (long) discountpercent + "%" : discountpercent + "%"; // ERP-27882
//                        } else {
//                            discountname = invoice.getCurrency().getSymbol() + " " + authHandler.formattingDecimalForAmount(row.getDiscount().getDiscountValue(), companyid);//to show as it is in UI
//                        }
//                    } else {
//                        discountname = "0 %";
//                    }
                    discountname = detail.getDiscount() + " %";
                    totalDiscount += authHandler.round(rowdiscountvalue, companyid);

                    obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                    obj.put(CustomDesignerConstants.Discountname, discountname);// Discount
                    obj.put(CustomDesignerConstants.QuantitywithUOM, authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom); // Quantity
                    obj.put(CustomDesignerConstants.JobWorkChallanDate, challanDate);
                    obj.put(CustomDesignerConstants.IN_Quantity, authHandler.formattingDecimalForQuantity(quantity, companyid));
                    obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate
                    obj.put(CustomDesignerConstants.RATEINCLUDINGGST, authHandler.formattingDecimalForUnitPrice(row.getRateincludegst(), companyid));// Rate Including GST
                    obj.put(CustomDesignerConstants.IN_Currency, invoice.getCurrency().getCurrencyCode());
                    obj.put(CustomDesignerConstants.IN_UOM, uom);
                    obj.put(CustomDesignerConstants.PRODUCTNETWEIGHT, authHandler.formattingDecimalForAmount(prod.getProductweight(), companyid));//Product Weight
                    obj.put("currencysymbol", invoice.getCurrency().getSymbol());//used for headercurrency & record currency
                    obj.put("basecurrencysymbol", invoice.getCompany().getCurrency().getSymbol());//used for headercurrency & record currency
                    obj.put("basecurrencycode", invoice.getCompany().getCurrency().getCurrencyCode());//used for headercurrency & record currency
                    obj.put("isGstIncluded", invoice.isGstIncluded());//used for headercurrency & record currency
                    obj.put("currencycode", invoice.getCurrency().getCurrencyCode());//used for headercurrencyCode & record currency
                    String baseUrl = com.krawler.common.util.URLUtil.getDomainURL(requestJobj.optString("cdomain", ""), false);
                    /*
                     * Following code is to check whether the image is predent
                     * for product or not. If Image is not present sent s.gif
                     * instead of product id
                     */
                    String fileName = null;
                    fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages + prod.getID() + ".png";
                    File file = new File(fileName);
                    FileInputStream in = null;
                    String filePathString = "";
                    try {
                        in = new FileInputStream(file);
                    } catch (java.io.FileNotFoundException ex) {
                        //catched exception if file not found, and to continue for rest of the products
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    if (in != null) {
                        filePathString = baseUrl + "productimage?fname=" + prod.getID() + ".png&isDocumentDesignerPrint=true";
                    } else {
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    obj.put(CustomDesignerConstants.imageTag, filePathString);

                    String lineLevelTax = "";
                    String lineLevelTaxPercent = "";
                    String lineLevelTaxAmount = "";
                    if (extraCompanyPreferences.isIsNewGST()) { // for New gst check 
                        HashMap<String, Object> InvoiceDetailParams = new HashMap<String, Object>();
                        InvoiceDetailParams.put("InvoiceDetailid", row.getID());
                        InvoiceDetailParams.put("orderbyadditionaltax", true);
                        // GST
                        InvoiceDetailParams.put("termtype", 7);
                        KwlReturnObject grdTermMapresult = accInvoiceDAOobj.getInvoicedetailTermMap(InvoiceDetailParams);
                        List<InvoiceDetailTermsMap> gst = grdTermMapresult.getEntityList();

                        for (InvoiceDetailTermsMap invoicedetailTermMap : gst) {
                            LineLevelTerms mt = invoicedetailTermMap.getTerm();
                            //Put tax rate and amount in respective fields
                            if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                                obj.put(CustomDesignerConstants.CGSTPERCENT, invoicedetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.CGSTAMOUNT, invoicedetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                                obj.put(CustomDesignerConstants.IGSTPERCENT, invoicedetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.IGSTAMOUNT, invoicedetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                                obj.put(CustomDesignerConstants.SGSTPERCENT, invoicedetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.SGSTAMOUNT, invoicedetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                                obj.put(CustomDesignerConstants.UTGSTPERCENT, invoicedetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.UTGSTAMOUNT, invoicedetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                                obj.put(CustomDesignerConstants.CESSPERCENT, invoicedetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.CESSAMOUNT, invoicedetailTermMap.getTermamount());
                            }
                            lineLevelTax += mt.getTerm();
                            lineLevelTax += "!## ";

                            lineLevelTaxPercent += authHandler.formattingDecimalForAmount(invoicedetailTermMap.getPercentage(), companyid);
                            lineLevelTaxPercent += "!## ";
                            lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(invoicedetailTermMap.getTermamount(), amountdigitafterdecimal, countryid);
                            lineLevelTaxAmount += "!## ";
                            if (lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm()) != null) {
                                double value = lineLevelTaxNames.get(mt.getTerm());
                                lineLevelTaxNames.put(mt.getTerm(), invoicedetailTermMap.getTermamount() + value);

                            } else {
                                lineLevelTaxNames.put(mt.getTerm(), invoicedetailTermMap.getTermamount());
                            }
                        }

                        if (!StringUtil.isNullOrEmpty(lineLevelTax)) {
                            lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length() - 4);
                        }
                        if (!StringUtil.isNullOrEmpty(lineLevelTaxPercent)) {
                            lineLevelTaxPercent = lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length() - 4);
                        }
                        if (!StringUtil.isNullOrEmpty(lineLevelTaxAmount)) {
                            lineLevelTaxAmount = lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length() - 4);
                        }
                        obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                        obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                        obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                        //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                        ExportRecordHandler.setHsnSacProductDimensionField(prod, obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    } else {
                        /*
                         * Fetching distinct taxes used at line level, feetched
                         * in the set Also, fetched the information related to
                         * tax in different maps
                         */
                        boolean isRowTaxApplicable = false;
                        double rowTaxPercentGST = 0.0;
                        if (detail.getTax() != null) {
                            String taxCode = detail.getTax().getTaxCode();
                            if (!lineLevelTaxesGST.contains(taxCode)) {
                                lineLevelTaxesGST.add(taxCode);
//                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), detail.getTax().getID());
                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getCreationDate(), detail.getTax().getID());
                                rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                                lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                                lineLevelTaxAmountGST.put(taxCode, 0.0);
                                lineLevelTaxBasicGST.put(taxCode, 0.0);
                            }
                            double taxAmount = (((rowamountwithouttax - rowdiscountvalue) * rowTaxPercentGST) / 100);
                            lineLevelTaxAmountGST.put(taxCode, (Double) lineLevelTaxAmountGST.get(taxCode) + (taxAmount));
                            lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + obj.optDouble(CustomDesignerConstants.SUBTOTAL, 0.0));

                        }
                    }

                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                    if (!StringUtil.isNullOrEmpty(prod.getID())) {
                        KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), prod.getID());
                        Product product = (Product) prodresult.getEntityList().get(0);
                        isLocationForProduct = product.isIslocationforproduct();
                        isbatchforproduct = product.isIsBatchForProduct();
                        isserialforproduct = product.isIsSerialForProduct();
                        isWarehouseForProduct = product.isIswarehouseforproduct();

                    }
                    if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory()
                            || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory()
                            || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {
                        if (isbatchforproduct || isserialforproduct || isLocationForProduct || isWarehouseForProduct) {  //product level batch and serial no on or not but now only location is checked
                            String rowid = "";
                            KwlReturnObject res = accInvoiceDAOobj.getDOFromInvoiceNew(invoice.getID(), companyid);
                            if (res.getEntityList().size() > 0 && res.getEntityList().size() >= rowcnt) {
                                String DOdetailID = res.getEntityList().get(rowcnt - 1).toString();
                                DeliveryOrderDetail deliveryOrderDetailObj = (DeliveryOrderDetail) kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.DeliveryOrderDetail", DOdetailID);
                                if (deliveryOrderDetailObj != null) {
                                    rowid = deliveryOrderDetailObj.getID();
                                }
                            } else {
                                if (row.getDeliveryOrderDetail() != null) {
                                    rowid = row.getDeliveryOrderDetail().getID();
                                }
                            }

                            String batchdetails = accInvoiceServiceDAO.getNewBatchJson(prod, requestJobj, rowid);
                            JSONArray locjArr = new JSONArray(batchdetails);
                            String location = "";
                            String locationName = "", batchname = "", warehouse = "", serialnumber = "", serialexpdate = "", batchexpdate = "", batchmfgdate = "";
                            String locationnamenew = "", batchnamenew = "", warehousenew = "", serialnumbernew = "", serialexpdatenew = "", batchexpdatenew = "", batchesmfgdatenew = "";
                            LinkedList<String> batchnames = new LinkedList();
                            LinkedList<String> serialnumbers = new LinkedList();
                            LinkedList<String> locationnames = new LinkedList();
                            LinkedList<String> warehouses = new LinkedList();
                            LinkedList<String> serialsexpdate = new LinkedList();
                            LinkedList<String> batchesexpirydate = new LinkedList();
                            LinkedList<String> batchesmfgdate = new LinkedList();

                            for (int i = 0; i < locjArr.length(); i++) {
                                JSONObject jSONObject = new JSONObject(locjArr.get(i).toString());
                                location = jSONObject.optString("location", "");
                                batchname = jSONObject.optString("batchname", "");
                                warehouse = jSONObject.optString("warehouse", "");
                                serialnumber = jSONObject.optString("serialno", "");
                                serialexpdate = jSONObject.optString("expend", "");
                                batchexpdate = jSONObject.optString("expdate", "");
                                batchmfgdate = jSONObject.optString("mfgdate", "");
                                Date date = null;
                                if (!StringUtil.isNullOrEmpty(serialexpdate)) {
                                    date = df1.parse(serialexpdate);
                                    serialexpdate = df.format(date);
                                    serialsexpdate.add(serialexpdate);
                                } else {
                                    serialsexpdate.add(" ");
                                }

                                if (!StringUtil.isNullOrEmpty(batchexpdate)) {
                                    date = df1.parse(batchexpdate);
                                    batchexpdate = df.format(date);
                                    if (batchesexpirydate.contains(batchexpdate)) {
                                        batchesexpirydate.add(" ");
                                    } else {
                                        batchesexpirydate.add(batchexpdate);
                                    }
                                } else {
                                    batchesexpirydate.add(" ");
                                }
                                if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
                                    date = df1.parse(batchmfgdate);
                                    batchmfgdate = df.format(date);
                                    if (batchesmfgdate.contains(batchmfgdate)) {
                                        batchesmfgdate.add(" ");
                                    } else {
                                        batchesmfgdate.add(batchmfgdate);
                                    }
                                } else {
                                    batchesmfgdate.add(" ");
                                }
                                serialnumbers.add(serialnumber);

                                if (!StringUtil.isNullOrEmpty(batchname)) {
                                    if (batchnames.contains(batchname)) {
                                        batchnames.add(" ");
                                    } else {
                                        batchnames.add(batchname);
                                    }
                                }

                                if (!StringUtil.isNullOrEmpty(warehouse)) {
                                    if (warehouses.contains(warehouse)) {
                                        warehouses.add(" ");
                                    } else {
                                        warehouses.add(warehouse);
                                    }
                                }

                                if (!StringUtil.isNullOrEmpty(location)) {
                                    KwlReturnObject loc = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), location);
                                    InventoryLocation localist = (InventoryLocation) loc.getEntityList().get(0);
                                    locationName = localist.getName();
                                    if (locationnames.contains(locationName)) {
                                        locationnames.add(" ");
                                    } else {
                                        locationnames.add(locationName);
                                    }
//                                locationName = locationName.concat(",");
                                }

//                                if (!StringUtil.isNullOrEmpty(batchname)) {
//                                    batchname = batchname.concat(",");
//                                }
//                                if (!StringUtil.isNullOrEmpty(serialnumber)) {
//                                    serialnumber = serialnumber.concat(",");
//                                }
//                                if (!StringUtil.isNullOrEmpty(serialexpdate)) {
//                                    serialexpdate = serialexpdate.concat("<br>");
//                                }
//                                if (!StringUtil.isNullOrEmpty(batchexpdate)) {
//                                    batchexpdate = batchexpdate.concat("<br>");
//                                }
//                                if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
//                                    batchmfgdate = batchmfgdate.concat("<br>");
//                                }
                            }

                            for (String str : batchnames) {
                                String bno = "";
                                bno = str;
                                if (!StringUtil.isNullOrEmpty(bno) && !bno.equals(" ")) {
                                    batchnamenew += bno.concat("!##");
                                }
                            }

                            for (String str : serialnumbers) {
                                String sno = "";
                                sno = str;
                                if (!StringUtil.isNullOrEmpty(sno) && !sno.equals(" ")) {
                                    serialnumbernew += sno.concat("!##");
                                }
                            }
                            for (String str : locationnames) {
                                String lno = "";
                                lno = str;
                                if (!StringUtil.isNullOrEmpty(lno) && !lno.equals(" ")) {
                                    locationnamenew += lno.concat("!##");
                                }
                            }
                            for (String str : serialsexpdate) {
                                String sexp = "";
                                sexp = str;
                                if (!StringUtil.isNullOrEmpty(sexp) && !sexp.equals(" ")) {
                                    serialexpdatenew += sexp.concat("!##");
                                }
                            }

                            for (String str : batchesexpirydate) {
                                String bexp = "";
                                bexp = str;
                                if (!StringUtil.isNullOrEmpty(bexp) && !bexp.equals(" ")) {
                                    batchexpdatenew += bexp.concat("!##");
                                }
                            }
                            for (String str : batchesmfgdate) {
                                String bmfg = "";
                                bmfg = str;
                                if (!StringUtil.isNullOrEmpty(bmfg) && !bmfg.equals(" ")) {
                                    batchesmfgdatenew += bmfg.concat("!##");
                                }
                            }
                            if (!StringUtil.isNullOrEmpty(batchesmfgdatenew)) {
                                batchesmfgdatenew = batchesmfgdatenew.substring(0, batchesmfgdatenew.length() - 3);
                            }
                            if (!StringUtil.isNullOrEmpty(serialnumbernew)) {
                                serialnumbernew = serialnumbernew.substring(0, serialnumbernew.length() - 3);
                            }
                            if (!StringUtil.isNullOrEmpty(serialexpdatenew)) {
                                serialexpdatenew = serialexpdatenew.substring(0, serialexpdatenew.length() - 3);
                            }
                            if (!StringUtil.isNullOrEmpty(batchnamenew)) {
                                batchnamenew = batchnamenew.substring(0, batchnamenew.length() - 3);
                            }
                            if (!StringUtil.isNullOrEmpty(locationnamenew)) {
                                locationnamenew = locationnamenew.substring(0, locationnamenew.length() - 3);
                            }
                            if (!StringUtil.isNullOrEmpty(batchexpdatenew)) {
                                batchexpdatenew = batchexpdatenew.substring(0, batchexpdatenew.length() - 3);
                            }
                            obj.put(CustomDesignerConstants.SerialNumber, serialnumbernew);
                            obj.put(CustomDesignerConstants.SerialNumberExp, serialexpdatenew);
                            obj.put(CustomDesignerConstants.BatchNumber, batchnamenew);
                            obj.put(CustomDesignerConstants.JobWorkBalanceQty, "");
                            obj.put(CustomDesignerConstants.JobWorkConsumeQty, "");
                            obj.put(CustomDesignerConstants.BatchNumberExp, batchexpdatenew);
                            obj.put(CustomDesignerConstants.IN_Loc, locationnamenew);
                            obj.put(CustomDesignerConstants.ManufacturingDate, batchesmfgdatenew);// Batch Manufacturing Date
                        } else { //if not activated location for product level then take default location for product.
                            obj.put(CustomDesignerConstants.IN_Loc, prod.getLocation() == null ? "" : prod.getLocation().getName());
                        }
                    } else { //if not activated location for product level then take default location for product.
                        obj.put(CustomDesignerConstants.IN_Loc, prod.getLocation() == null ? "" : prod.getLocation().getName());
                    }
                    /*
                     * Row tax section
                     */
//                entryDate = invoice.getJournalEntry().getEntryDate();
                    Date entryDate = invoice.getCreationDate();
                    if (detail != null && detail.getTax() != null) {

                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", detail.getTax().getID());
                        KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                        List taxList = result1.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        rowTaxName = detail.getTax().getName();
                        rowTaxAmt = detail.getRowTaxAmount() ;
                    }

                    totaltax += rowTaxAmt;//Calculate tax amount from line item
                    obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmt);//Row TaxAmount
                    obj.put(CustomDesignerConstants.IN_ProductTax, rowTaxName);

                    if (row.getInvoice().isGstIncluded()) {
                        rowamountwithtax = rowamountwithouttax - rowdiscountvalue + rowTaxAmt; //Amount will be equal to rowamountwithouttax because tax gets added in gst
                    } else {
                        if (rowdiscountvalue != 0) {
                            rowamountwithouttax -= rowdiscountvalue;//deducting discount if any
                        }
                        rowamountwithtax = rowamountwithouttax + rowTaxAmt;
                    }
                    obj.put(CustomDesignerConstants.Amount, authHandler.round(rowamountwithtax, companyid)); // Amount

                    obj.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, 1);
                    obj.put("gstCurrencyRate", invoice.getGstCurrencyRate() == 0 ? "" : invoice.getGstCurrencyRate());

                    obj.put("transactiondate", invoice.getJournalEntry().getEntryDate());

                    /*
                     * <--------------------Base Currency Unit Price,Base
                     * currency Subtotal ----------->
                     */
                    /*
                     * Exchange Rate Section---- We have not given amount with
                     * tax because it is matched with UI
                     */
                    if (externalCurrencyRate != 0) {
                        //Unit Price   
                        exchangerateunitprice = authHandler.round((rate * revExchangeRate), companyid);  //exchanged rate unit rate
                        //SUbTotal (rate*quantity)
                        exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                        //Exhanged Rate Discount
                        exchangeratelineitemdiscount = authHandler.round((rowdiscountvalue * revExchangeRate), companyid);//exchange rate total discount
                        if (row.getInvoice().isGstIncluded()) {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal), companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                        } else {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                        }

                        //Exhanged Rate Tax Amount
                        exchangeratelineitemtax = authHandler.round((rowTaxAmt * revExchangeRate), companyid);

                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);

                        obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);
                        //Total Exchanged Rate (RATE*Quantity)
                    } else {
                        //Unit Price   
                        exchangerateunitprice = authHandler.round(rate, companyid);  //exchanged rate unit rate
                        //SUbTotal (rate*quantity)
                        exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                        //Exhanged Rate Discount
                        exchangeratelineitemdiscount = authHandler.round(rowdiscountvalue, companyid);//exchange rate total discount
                        if (row.getInvoice().isGstIncluded()) {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                        } else {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                        }

                        //Exhanged Rate Tax Amount
                        exchangeratelineitemtax = authHandler.round(rowTaxAmt, companyid);

                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);

                        obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);

                    }

                    /*
                     * to get the linkig information upto 2-3 levels (Mayur B).
                     */
                    if (row.getDeliveryOrderDetail() != null) {
                        obj.put(CustomDesignerConstants.Link_No, row.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber());
                    } else if (row.getSalesorderdetail() != null) {
                        obj.put(CustomDesignerConstants.Link_No, row.getSalesorderdetail().getSalesOrder().getSalesOrderNumber());
                    } else if (row.getQuotationDetail() != null) {
                        obj.put(CustomDesignerConstants.Link_No, row.getQuotationDetail().getQuotation().getquotationNumber());
                    }

//                    /*
//                     * get custom line data
//                     */
//                    Map<String, Object> variableMap = new HashMap<String, Object>();
//                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
//                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
//                    Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
//                    Detailfilter_params.add(row.getID());
//                    invDetailRequestParams.put("filter_names", Detailfilter_names);
//                    invDetailRequestParams.put("filter_params", Detailfilter_params);
//                    KwlReturnObject idcustresult = accInvoiceDAOobj.getInvoiceDetailsCustomData(invDetailRequestParams);
//                    if (idcustresult.getEntityList().size() > 0) {
//                        AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
//                        ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestJobj, jeDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
//                    }

//                    /*
//                     * Product Level Custom Fields Evaluation
//                     */
//                    invDetailRequestParams.clear();
//                    Detailfilter_names.add("productId");
//                    Detailfilter_params.add(row.getInventory().getProduct().getID());
//                    invDetailRequestParams.put("filter_names", Detailfilter_names);
//                    invDetailRequestParams.put("filter_params", Detailfilter_params);
//                    KwlReturnObject idcustresultForProduct = accInvoiceDAOobj.getInvoiceDetailsCustomDataForProductNew(invDetailRequestParams);
//                    AccJEDetailsProductCustomData accJEDetailsProductCustomData = null;
//                    String customDataID = "";
//                    if (idcustresultForProduct.getEntityList().size() > 0) {
////                    accJEDetailsProductCustomData = (AccJEDetailsProductCustomData) idcustresultForProduct.getEntityList().get(0);
//                        customDataID = idcustresultForProduct.getEntityList().get(0).toString();
//                        accJEDetailsProductCustomData = (AccJEDetailsProductCustomData) kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.AccJEDetailsProductCustomData", customDataID);
//                    }
//                    replaceFieldMap = new HashMap<String, String>();
//                    if (accJEDetailsProductCustomData != null) {
//                        ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestJobj, accJEDetailsProductCustomData, ProductLevelCustomFieldMap, obj, kwlCommonTablesDAOObj, variableMap);
//                    }

                    /*
                     * Set All Line level Dimension & All LIne level Custom
                     * Field Values
                     */
//                    obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
//                    obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields
                    //GST Exchange Rate
                    if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !preference.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                        obj.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, row.getGstCurrencyRate());
                    }
                    jArr.put(obj);
                    
                    //Calculate summary data for CN
                    if (!processedInv.contains(invoice.getID())) {

                        if (!StringUtil.isNullOrEmpty(invoiceNos)) {
                            invoiceNos += " , ";
                            invAmounts += " , ";
                            invTaxAmounts += " , ";
                            invAmountDues += " , ";
//                        invEnterAmounts += " , ";
                            invoicedates += " , ";
                            invoiceSalesPerson += " , ";
                        }
                        double invamount = 0, invtax = 0;

                        if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                            invamount = invoice.getOriginalOpeningBalanceAmount();
                        } else {
                            invamount = invoice.getCustomerEntry().getAmount();//Invoice Sub Total
                        }

                        invtax = invoice.getTaxEntry() == null ? 0 : invoice.getTaxEntry().getAmount();

                        invoiceNos += invoice.getInvoiceNumber();
                        invAmounts += invamount;
                        invTaxAmounts += invtax;
                        invAmountDues += invoice.getInvoiceamountdue();
//                            invEnterAmounts += (StringUtil.isNullOrEmpty(Double.toString(jObj.getDouble(CustomDesignerConstants.CNDN_InvoiceEnterAmount_fieldTypeId))) ? "-" : Double.toString(jObj.getDouble(CustomDesignerConstants.CNDN_InvoiceEnterAmount_fieldTypeId)));
                        invoicedates += invoice.getJournalEntry() != null ? invoice.getJournalEntry().getEntryDate() : invoice.getCreationDate();
                        invoiceSalesPerson += invoice.getMasterSalesPerson() == null ? " - " : invoice.getMasterSalesPerson().getValue();

                        invoicesubtotal += invamount - invtax;//Invoice Sub Total
                        invoicetotaltax += invtax; //Invoice Total Tax
                        invoicetotalamount += invamount; //Invoice Total Amount

                        processedInv.add(invoice.getID());
                    }
                }
            }
          
            //Calculate total tax if global tax is applied 
            if (cn.getTax() != null) {
                requestParams.put("transactiondate", cn.getJournalEntry().getEntryDate());
                requestParams.put("taxid", cn.getTax().getID());
                requestParams.put("companyid", companyid);
                KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                List taxList = result1.getEntityList();
                Object[] taxObj = (Object[]) taxList.get(0);
                double taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];

                totaltax = (subTotalWithDiscount * taxPercent) / 100;

            }


            summaryParams.put("lineLevelTaxesGST", lineLevelTaxesGST);
            summaryParams.put("lineLevelTaxAmountGST", lineLevelTaxAmountGST);
            summaryParams.put("lineLevelTaxBasicGST", lineLevelTaxBasicGST);
            summaryParams.put("invoicesubtotal", invoicesubtotal);
            summaryParams.put("invoicetotaltax", invoicetotaltax);
            summaryParams.put("invoicetotalamount", invoicetotalamount);
            summaryParams.put("invoiceNos", invoiceNos);
            summaryParams.put("invAmounts", invAmounts);
            summaryParams.put("invTaxAmounts", invTaxAmounts);
            summaryParams.put("invAmountDues", invAmountDues);
            summaryParams.put("invoicedates", invoicedates);
            summaryParams.put("totaltax", totaltax);
            summaryParams.put("totalDiscount", totalDiscount);
            summaryParams.put("subTotal", subtotal);
            summaryParams.put("subTotalWithDiscount", subTotalWithDiscount);
            summaryParams.put("subTotalWithTax", subTotalWithDiscount + totaltax);
            summaryParams.put("totalwithtax", subTotalWithDiscount + totaltax);
            summaryParams.put("salespersonvalue", invoiceSalesPerson);
            summaryParams.put("lineLevelTaxNames", lineLevelTaxNames);
            
            //Getting summary data for Overcharged Cn
            JSONObject summaryData = getCNSummaryData(requestJobj, summaryParams);

            jArr.put(summaryData);

        } catch (Exception e) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, e);
        }

        return jArr;
    }
    
    /*
     * Function to fetch product details for Undercharged CN
     */
    public JSONArray getUnderchargeCNDetailsItemJSON(JSONObject requestJobj, Map<String, Object> summaryParams ) throws ServiceException {

        JSONArray jArr = new JSONArray();
        try {
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) summaryParams.get("companyAccountPreferences");
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) summaryParams.get("extraCompanyPreferences");
            String companyid = requestJobj.optString(Constants.companyKey);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestJobj);

            int countryid = 0;
            if (extraCompanyPreferences.getCompany() != null && extraCompanyPreferences.getCompany().getCountry() != null) {
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }

            CreditNote cn = (CreditNote) summaryParams.get("creditNote");
            Set<CreditNoteAgainstVendorGst> rowsGst = cn.getRowsGst();
            GoodsReceiptDetail row = null;
            GoodsReceipt goodsReceipt = null;
            int rowcnt = 0;
            String currencyid = "", uom = "", vendorTitle = "";
            Date entryDate = null;
            double subtotal = 0, subTotalWithDiscount = 0, totalDiscount = 0, totaltax = 0, discountamount = 0, revExchangeRate = 0.0;
            int amountdigitafterdecimal = 2;

            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
            }

            /*
             * Declaration of Variables to fetch Line level taxes and its
             * information like Tax Code, Tax Amount etc.
             */
            Set<String> lineLevelTaxesGST = new HashSet<String>();
            Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();
            Map<String, Double> lineLevelTaxNames = new LinkedHashMap<String, Double>();

            String invoiceNos = "", invAmounts = "", invTaxAmounts = "", invAmountDues = "", invoicedates = "", invoiceSalesPerson = "";
            double invoicesubtotal = 0, invoicetotaltax = 0, invoicetotalamount = 0;


            // List to identify processed invoices while calculating summary data for CN
            List<String> processedInv = new ArrayList<>();


            for (CreditNoteAgainstVendorGst detail : rowsGst) {
                if (detail.getVidetails() != null) {
                    rowcnt++;
                    row = detail.getVidetails();
                    goodsReceipt = row.getGoodsReceipt();

                    double externalCurrencyRate = goodsReceipt.getExchangeRateDetail() != null ? goodsReceipt.getExchangeRateDetail().getExchangeRate() : 1;
                    if (externalCurrencyRate != 0) {
                        revExchangeRate = 1 / externalCurrencyRate;
                    }
                    
                    JSONObject obj = new JSONObject();
                    Inventory inv = row.getInventory();
                    Product prod = inv.getProduct();
                    String proddesc = "", discountname = "", rowTaxName = "", basqtyuom = "", BaseQtyWithUOM = "";
                    double rate = 0, rowTaxPercent = 0, rowTaxAmt = 0, rowamountwithtax = 0, rowdiscountvalue = 0, rowamountwithouttax = 0, quantity = 0, baseqty = 0;
                    double rowamountwithgst = 0;
                    //product type
                    obj.put("type", prod.getProducttype() == null ? "" : prod.getProducttype().getName());
                    obj.put(CustomDesignerConstants.IN_ProductCode, prod.getProductid());

                    double disc = detail.getDiscount();
                    obj.put("prdiscount", disc);
                    obj.put("discountispercent", 1);

//                entryDate = goodsReceipt.getJournalEntry().getEntryDate();
                    entryDate = goodsReceipt.getCreationDate();

                    rate = detail.getRate();
                    quantity = (goodsReceipt.getPendingapproval() == 1 || goodsReceipt.getIstemplate() == 2) ? row.getInventory().getActquantity() : (row.getInventory().isInvrecord() ? detail.getReturnQuantity() : detail.getReturnQuantity());
                    rowamountwithouttax = rate * quantity;

                    /*
                     * In include GST case calculations are in reverse order In
                     * other cases calculations are in forward order
                     */
                    if (row.getGoodsReceipt().isGstIncluded()) {//if gstincluded is the case
                        
                        rowamountwithgst = row.getRateincludegst() * quantity;
                        rowdiscountvalue = (rowamountwithouttax * detail.getDiscount()) / 100;
                        obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.round(rowamountwithouttax, companyid));//Subtotal
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.round((rowamountwithgst - (rowamountwithgst * rowdiscountvalue) / 100 - row.getRowTaxAmount()), companyid));//Subtotal
                        subtotal += authHandler.round(rowamountwithouttax, companyid);
                        subTotalWithDiscount += authHandler.round(rowamountwithouttax - rowdiscountvalue, companyid);
                    } else {
                        rowdiscountvalue = (rowamountwithouttax * detail.getDiscount()) / 100;
                        obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithouttax, companyid));//Subtotal
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount((rowamountwithouttax - rowdiscountvalue), companyid));//Subtotal
                        subtotal += authHandler.round(rowamountwithouttax, companyid);//rounded becuase totaltax & amount are rounded and saved in db
                        subTotalWithDiscount += authHandler.round((rowamountwithouttax - rowdiscountvalue), companyid);
                    }
                    obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate

                    /*
                     * Discount Section
                     */
//                    Discount discount = row.getDiscount();
//                    if (discount != null) {
//                        if (discount.isInPercent()) {
//                            discountname = CustomDesignHandler.getAmountinCommaDecimal(row.getDiscount().getDiscount(), 0, countryid) + "%";//to return 0 no of zeros
//                        } else {
//                            discountname = goodsReceipt.getCurrency().getSymbol() + " " + authHandler.formattingDecimalForAmount(row.getDiscount().getDiscountValue(), companyid);//to show as it is in UI
//                        }
//                    } else {
                        discountname = detail.getDiscount()+ " %";
//                    }
                    totalDiscount += authHandler.round(rowdiscountvalue, companyid);
                    obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                    obj.put(CustomDesignerConstants.Discountname, discountname);// Discount Name

                    KwlReturnObject productCategories = null;
                    productCategories = accProductObj.getProductCategoryForDetailsReport(prod.getID());
                    List productCategoryList = productCategories.getEntityList();
                    String cateogry = "";
                    Iterator catIte = productCategoryList.iterator();
                    while (catIte.hasNext()) {
                        ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                        String categoryName = pcm.getProductCategory() != null ? pcm.getProductCategory().getValue().toString() : "";
                        cateogry += categoryName + " ";
                    }
                    if (StringUtil.isNullOrEmpty(cateogry)) {
                        cateogry = "None";
                    }
                    obj.put("productCategory", cateogry);

                    /*
                     * Row tax section
                     */
                    if (row != null && detail.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", detail.getTax().getID());
                        requestParams.put("companyid", companyid);
                        KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                        List taxList = result1.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        rowTaxName = detail.getTax().getName();
                        discountamount += discountamount * rowTaxPercent / 100;
                        rowTaxAmt = detail.getRowTaxAmount();
                    }
                    totaltax += authHandler.round(rowTaxAmt, companyid);//Calculate tax amount from line item
                    obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmt);//Row TaxAmount
                    obj.put(CustomDesignerConstants.IN_ProductTax, rowTaxName);

                    if (row.getGoodsReceipt().isGstIncluded()) {
                        rowamountwithtax = rowamountwithouttax - rowdiscountvalue + rowTaxAmt; 
                    } else {
                        if (rowdiscountvalue != 0) {
                            rowamountwithouttax -= rowdiscountvalue;//deducting discount if any
                        }
                        rowamountwithtax = rowamountwithouttax + rowTaxAmt;
                    }

                    obj.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, 1);
                    obj.put("gstCurrencyRate", goodsReceipt.getGstCurrencyRate() == 0 ? "" : goodsReceipt.getGstCurrencyRate());
                    obj.put("transactiondate", goodsReceipt.getJournalEntry().getEntryDate());

                    double exchangerateunitprice = 0, exchangeratelineitemsubtotal = 0, exchangeratelineitemdiscount = 0, exchangeratelineitemamount = 0, exchangeratelineitemtax = 0, exchangeratelineitemsubtotalwithdiscount = 0;
                    /*
                     * <--------------------Base Currency Unit Price,Base
                     * currency Subtotal ----------->
                     */
                    /*
                     * Exchange Rate Section---- We have not given amount with
                     * tax because it is matched with UI
                     */
                    if (externalCurrencyRate != 0) {
                        //Unit Price   
                        exchangerateunitprice = authHandler.round((rate * revExchangeRate), companyid);  //exchanged rate unit rate
                        //SUbTotal (rate*quantity)
                        exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                        //Exhanged Rate Discount
                        exchangeratelineitemdiscount = authHandler.round((rowdiscountvalue * revExchangeRate), companyid);//exchange rate total discount
                        if (row.getGoodsReceipt().isGstIncluded()) {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                        } else {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                        }

                        //Exhanged Rate Tax Amount
                        exchangeratelineitemtax = authHandler.round((rowTaxAmt * revExchangeRate), companyid);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);

                        obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);


                    } else {
                        //Unit Price   
                        exchangerateunitprice = authHandler.round(rate, companyid);  //exchanged rate unit rate
                        //SUbTotal (rate*quantity)
                        exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                        //Exhanged Rate Discount
                        exchangeratelineitemdiscount = authHandler.round(rowdiscountvalue, companyid);//exchange rate total discount
                        if (row.getGoodsReceipt().isGstIncluded()) {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                        } else {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                        }

                        //Exhanged Rate Tax Amount
                        exchangeratelineitemtax = authHandler.round(rowTaxAmt, companyid);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);

                        obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);

                    }

                    obj.put(CustomDesignerConstants.Amount, authHandler.formattingDecimalForAmount(rowamountwithtax, companyid)); // Amount
                    String baseUrl = com.krawler.common.util.URLUtil.getDomainURL(requestJobj.optString("cdomain", ""), false);
                    /*
                     * Following code is to check whether the image is predent
                     * for product or not. If Image is not present sent s.gif
                     * instead of product id
                     */
                    String fileName = null;
                    fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages + prod.getID() + ".png";
                    File file = new File(fileName);
                    FileInputStream in = null;
                    String filePathString = "";
                    try {
                        in = new FileInputStream(file);
                    } catch (java.io.FileNotFoundException ex) {
                        //catched exception if file not found, and to continue for rest of the products
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    if (in != null) {
                        filePathString = baseUrl + "productimage?fname=" + prod.getID() + ".png&isDocumentDesignerPrint=true";
                    } else {
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    obj.put(CustomDesignerConstants.imageTag, filePathString);
                    /*
                     * Put frid values in Line Item Grid
                     */
                    if (row.getGoodsReceiptOrderDetails() != null) {
                        obj.put(CustomDesignerConstants.Link_No, row.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber());
                    } else if (row.getPurchaseorderdetail() != null) {
                        obj.put(CustomDesignerConstants.Link_No, row.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber());
                    } else if (row.getVendorQuotationDetail() != null) {
                        obj.put(CustomDesignerConstants.Link_No, row.getVendorQuotationDetail().getVendorquotation().getQuotationNumber());
                    } else {
                        obj.put(CustomDesignerConstants.Link_No, "");
                    }
                    obj.put("currencysymbol", goodsReceipt.getCurrency().getSymbol());
                    obj.put("currencycode", goodsReceipt.getCurrency().getCurrencyCode());
                    obj.put("isGstIncluded", goodsReceipt.isGstIncluded());//used for headercurrency & record currency


                    String lineLevelTax = "";
                    String lineLevelTaxPercent = "";
                    String lineLevelTaxAmount = "";
                    double lineLevelTaxAmountTotal = 0;
                    if (extraCompanyPreferences.isIsNewGST()) { // For India Country 
                        HashMap<String, Object> GoodsReceiptDetailParams = new HashMap<String, Object>();
                        GoodsReceiptDetailParams.put("GoodsReceiptDetailid", row.getID());
                        GoodsReceiptDetailParams.put("orderbyadditionaltax", true);
                        // GST
                        GoodsReceiptDetailParams.put("termtype", 2);
                        KwlReturnObject grdTermMapresult = accGoodsReceiptobj.getGoodsReceiptdetailTermMap(GoodsReceiptDetailParams);
                        List<ReceiptDetailTermsMap> gst = grdTermMapresult.getEntityList();
                        //CGST fields
                        obj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                        obj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                        //IGST fields
                        obj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                        obj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                        //SGST fields
                        obj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                        obj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                        //UTGST fields
                        obj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                        obj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                        //CESS fields
                        obj.put(CustomDesignerConstants.CESSPERCENT, 0);
                        obj.put(CustomDesignerConstants.CESSAMOUNT, 0);

                        for (ReceiptDetailTermsMap receiptdetailTermMap : gst) {
                            LineLevelTerms mt = receiptdetailTermMap.getTerm();
                            /**
                             * Put respective GST field in constants CGST, SGST,
                             * IGST, UTGST, CESS
                             */
                            if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                                obj.put(CustomDesignerConstants.CGSTPERCENT, receiptdetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.CGSTAMOUNT, receiptdetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                                obj.put(CustomDesignerConstants.IGSTPERCENT, receiptdetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.IGSTAMOUNT, receiptdetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                                obj.put(CustomDesignerConstants.SGSTPERCENT, receiptdetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.SGSTAMOUNT, receiptdetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                                obj.put(CustomDesignerConstants.UTGSTPERCENT, receiptdetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.UTGSTAMOUNT, receiptdetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                                obj.put(CustomDesignerConstants.CESSPERCENT, receiptdetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.CESSAMOUNT, receiptdetailTermMap.getTermamount());
                            }
                            lineLevelTax += mt.getTerm();
                            lineLevelTax += "!## ";
                            lineLevelTaxPercent += authHandler.formattingDecimalForAmount(receiptdetailTermMap.getPercentage(), companyid);
                            lineLevelTaxPercent += "!## ";
                            lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(receiptdetailTermMap.getTermamount(), amountdigitafterdecimal, countryid);
                            lineLevelTaxAmount += "!## ";
                            /*
                             * calculating total of line level taxes
                             */
                            lineLevelTaxAmountTotal += receiptdetailTermMap.getTermamount();
                            if (lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm()) != null) {
                                double value = lineLevelTaxNames.get(mt.getTerm());
                                lineLevelTaxNames.put(mt.getTerm(), receiptdetailTermMap.getTermamount() + value);
                            } else {
                                lineLevelTaxNames.put(mt.getTerm(), receiptdetailTermMap.getTermamount());
                            }
                            if (mt.isIsAdditionalTax()) {
                                String term = "";
                                if (mt.getTerm() != null) {
                                    term = mt.getTerm().toLowerCase();
                                }
                            }
                        }

                        if (!StringUtil.isNullOrEmpty(lineLevelTax)) {
                            lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length() - 4);
                        }
                        if (!StringUtil.isNullOrEmpty(lineLevelTaxPercent)) {
                            lineLevelTaxPercent = lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length() - 4);
                        }
                        if (!StringUtil.isNullOrEmpty(lineLevelTaxAmount)) {
                            lineLevelTaxAmount = lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length() - 4);
                        }
                        obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                        obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                        obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                        /*
                         * putting subtotal+tax
                         */
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round(lineLevelTaxAmountTotal, companyid));
                        //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                        ExportRecordHandler.setHsnSacProductDimensionField(prod, obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    } else {
                        /*
                         * Fetching distinct taxes used at line level, feetched
                         * in the set Also, fetched the information related to
                         * tax in different maps
                         */
                        boolean isRowTaxApplicable = false;
                        double rowTaxPercentGST = 0.0;
                        if (detail.getTax() != null) {
                            String taxCode = detail.getTax().getTaxCode();
                            if (!lineLevelTaxesGST.contains(taxCode)) {
                                lineLevelTaxesGST.add(taxCode);
//                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceipt.getJournalEntry().getEntryDate(), row.getTax().getID());
                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceipt.getCreationDate(), detail.getTax().getID());
                                rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                                lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                                lineLevelTaxAmountGST.put(taxCode, 0.0);
                                lineLevelTaxBasicGST.put(taxCode, 0.0);
                            }
                            lineLevelTaxAmountGST.put(taxCode, (Double) lineLevelTaxAmountGST.get(taxCode) + (row.getRowTaxAmount()));
                            lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + obj.optDouble(CustomDesignerConstants.SUBTOTAL, 0.0));
                            /*
                             * putting subtotal+tax
                             */
                            obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round((row.getRowTaxAmount()), companyid));
                        }
                    }


                    uom = row.getInventory().getUom() == null ? (row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()) : row.getInventory().getUom().getNameEmptyforNA();

                    //Calculating base qty with UOM
                    basqtyuom = prod.getUnitOfMeasure() == null ? "" : prod.getUnitOfMeasure().getNameEmptyforNA();
                    double baseuomqty = row.getInventory().getBaseuomrate() * quantity;

                    if (!basqtyuom.equals(uom)) {
                        BaseQtyWithUOM = authHandler.formattingDecimalForQuantity(baseuomqty, companyid) + " " + basqtyuom;
                    } else {
                        BaseQtyWithUOM = authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom;
                        baseuomqty = quantity;
                    }

                    obj.put(CustomDesignerConstants.BaseQty, authHandler.formattingDecimalForQuantity(baseuomqty, companyid));
                    obj.put(CustomDesignerConstants.BaseQtyWithUOM, BaseQtyWithUOM);

                    proddesc = StringUtil.isNullOrEmpty(row.getDescription()) ? (StringUtil.isNullOrEmpty(inv.getProduct().getDescription()) ? "" : inv.getProduct().getDescription()) : row.getDescription();
                    proddesc = StringUtil.DecodeText(proddesc);
                    obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr No
                    obj.put(CustomDesignerConstants.ProductName, prod.getName().replaceAll("\n", "<br>"));// productname
                    obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>"));//Product Description
                    obj.put(CustomDesignerConstants.HSCode, (prod.getHSCode() != null) ? prod.getHSCode().replaceAll("\n", "<br>") : "");//get HSCode of ProductGrid
                    obj.put(CustomDesignerConstants.QuantitywithUOM, authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom); // Quantity
                    obj.put(CustomDesignerConstants.IN_Currency, goodsReceipt.getCurrency().getCurrencyCode());
                    obj.put(CustomDesignerConstants.IN_Quantity, authHandler.formattingDecimalForQuantity(quantity, companyid)); // Quantity
                    obj.put(CustomDesignerConstants.IN_UOM, uom);
                    obj.put(CustomDesignerConstants.PRODUCTNETWEIGHT, authHandler.formattingDecimalForAmount(prod.getProductweight(), companyid));//Product Weight
                    obj.put(CustomDesignerConstants.AdditionalDescription, prod.getAdditionalDesc() != null ? prod.getAdditionalDesc().replaceAll("\n", "<br>") : "");  //product Addtional description
                    //GST Exchange Rate
                    if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !companyAccountPreferences.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                        obj.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, row.getGstCurrencyRate());
                    }
                    jArr.put(obj);

                    if (!processedInv.contains(goodsReceipt.getID())) {

                        if (!StringUtil.isNullOrEmpty(invoiceNos)) {
                            invoiceNos += " , ";
                            invAmounts += " , ";
                            invTaxAmounts += " , ";
                            invAmountDues += " , ";
//                        invEnterAmounts += " , ";
                            invoicedates += " , ";
                            invoiceSalesPerson += " , ";
                        }
                        double invamount = 0, invtax = 0;

                        if (goodsReceipt.isIsOpeningBalenceInvoice() && !goodsReceipt.isNormalInvoice()) {
                            invamount = goodsReceipt.getOriginalOpeningBalanceAmount();
                        } else {
                            invamount = goodsReceipt.getVendorEntry().getAmount();//Invoice Sub Total
                        }

                        invtax = goodsReceipt.getTaxEntry() == null ? 0 : goodsReceipt.getTaxEntry().getAmount();

                        invoiceNos += goodsReceipt.getGoodsReceiptNumber();
                        invAmounts += invamount;
                        invTaxAmounts += invtax;
                        invAmountDues += goodsReceipt.getInvoiceamountdue();
//                            invEnterAmounts += (StringUtil.isNullOrEmpty(Double.toString(jObj.getDouble(CustomDesignerConstants.CNDN_InvoiceEnterAmount_fieldTypeId))) ? "-" : Double.toString(jObj.getDouble(CustomDesignerConstants.CNDN_InvoiceEnterAmount_fieldTypeId)));
                        invoicedates += goodsReceipt.getJournalEntry() != null ? goodsReceipt.getJournalEntry().getEntryDate() : goodsReceipt.getCreationDate();
                        invoiceSalesPerson += goodsReceipt.getMasterSalesPerson() == null ? " - " : goodsReceipt.getMasterSalesPerson().getValue();

                        invoicesubtotal += invamount - invtax;//Invoice Sub Total
                        invoicetotaltax += invtax; //Invoice Total Tax
                        invoicetotalamount += invamount; //Invoice Total Amount

                        processedInv.add(goodsReceipt.getID());
                    }
                }
            }
            
            //Calculate total tax if Global tax is applied
            if (cn.getTax() != null) {
                requestParams.put("transactiondate", cn.getJournalEntry().getEntryDate());
                requestParams.put("taxid", cn.getTax().getID());
                requestParams.put("companyid", companyid);
                KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                List taxList = result1.getEntityList();
                Object[] taxObj = (Object[]) taxList.get(0);
                double taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];

                totaltax = (subTotalWithDiscount * taxPercent) / 100;

            }

            summaryParams.put("lineLevelTaxesGST", lineLevelTaxesGST);
            summaryParams.put("lineLevelTaxAmountGST", lineLevelTaxAmountGST);
            summaryParams.put("lineLevelTaxBasicGST", lineLevelTaxBasicGST);
            summaryParams.put("invoicesubtotal", invoicesubtotal);
            summaryParams.put("invoicetotaltax", invoicetotaltax);
            summaryParams.put("invoicetotalamount", invoicetotalamount);
            summaryParams.put("invoiceNos", invoiceNos);
            summaryParams.put("invAmounts", invAmounts);
            summaryParams.put("invTaxAmounts", invTaxAmounts);
            summaryParams.put("invAmountDues", invAmountDues);
            summaryParams.put("invoicedates", invoicedates);
            summaryParams.put("totaltax", totaltax);
            summaryParams.put("totalDiscount", totalDiscount);
            summaryParams.put("subTotal", subtotal);
            summaryParams.put("subTotalWithDiscount", subTotalWithDiscount);
            summaryParams.put("subTotalWithTax", subTotalWithDiscount + totaltax);
            summaryParams.put("totalwithtax", subTotalWithDiscount + totaltax);
            summaryParams.put("salespersonvalue", invoiceSalesPerson);
            
            //getting summary data for Undercharged CN
            JSONObject summaryData = getCNSummaryData(requestJobj, summaryParams);

            jArr.put(summaryData);

        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    /*
     * Author: Sayed kausar Ali
     * Purpose: It gives Json For JOb Work Order Module related fields
     * Params: HttpServletRequest Request
     *          InvoiceDetail row 
     *          HashMap jwRequestParams
     */
    public JSONObject getJobWorkJson(JSONObject paramJobj, InvoiceDetail row, HashMap<String, Object> jwRequestParams) {
        JSONObject jobj = new JSONObject();
        try {
            double jobWorkOrderQty = 0.0;
            String challanDate = "";
            String jobworkorderno = "";
            Date jobworkorderdate = null;
            int quantitydigitafterdecimal = 2;
            String uom = "";
            String jobIN = "",challanBalanceQtyString = "",challanConsumeQtyString = "";
            String companyid = paramJobj.optString(Constants.companyKey);
            Product prod = row.getInventory().getProduct();
            SalesOrderDetail soDetail = null;
            DeliveryOrderDetail doDetail = null;
            if (jwRequestParams.containsKey("quantitydigitafterdecimal") && jwRequestParams.get("quantitydigitafterdecimal") != null) {
                
                quantitydigitafterdecimal = Integer.parseInt(jwRequestParams.get("quantitydigitafterdecimal").toString());
            }
            if (jwRequestParams.containsKey("uom") && jwRequestParams.get("uom") != null) {
                
                uom = jwRequestParams.get("uom").toString();
            }
            /*
             * Fetching SO Details and DO Details
             */
            if (row.getDeliveryOrderDetail() != null) { // Invoice is Created By linking A DO
                soDetail = row.getDeliveryOrderDetail().getSodetails() != null ? row.getDeliveryOrderDetail().getSodetails() : null;
                doDetail = row.getDeliveryOrderDetail();
            } else {  // Invoice is created with AUTO-DO true. or DO is created after invoice is created.
                soDetail = row.getSalesorderdetail();
                KwlReturnObject doResult = accInvoiceDAOobj.getDODetailfromInvoiceDetailID(row.getID(), companyid);
                if (!doResult.getEntityList().isEmpty()) {
                    doDetail = (DeliveryOrderDetail) doResult.getEntityList().get(0);
                }
            }
            JSONObject jobjchallan = new JSONObject();

            /*
             * if Both SO and DO details are present then only Job work Invoice
             * can be printed
             */
            JSONObject challanQtyDetails = new JSONObject();
            if (soDetail != null && doDetail != null) {

                Set challanInInvSet = new HashSet();
                HashMap<String, Double> challanQtyMap = new HashMap();
                /*
                 * Getting batch details
                 */
              String batchdetails = accInvoiceServiceDAO.getNewBatchJson(prod, paramJobj, doDetail.getID());
                if (!StringUtil.isNullOrEmpty(batchdetails)) {
                   /*
                     * Fetching Batch Name And quantity
                     */
                    JSONArray locjArr = new JSONArray(batchdetails);
                    for (int i = 0; i < locjArr.length(); i++) {
                        JSONObject jSONObject = new JSONObject(locjArr.get(i).toString());
                        if (!StringUtil.isNullOrEmpty(jSONObject.optString("batchname", ""))) {
                            challanInInvSet.add(jSONObject.optString("batchname", ""));
                            challanQtyMap.put(jSONObject.optString("batchname", ""), jSONObject.optDouble("quantity", 0.0));
                        }
                    }

                    jobWorkOrderQty = soDetail.getQuantity();
                    jobworkorderno = soDetail.getSalesOrder() != null ? soDetail.getSalesOrder().getSalesOrderNumber() : "";
                    jobworkorderdate = soDetail.getSalesOrder() != null ? soDetail.getSalesOrder().getOrderDate() : new Date();

                    String jobworkorderid = soDetail.getSalesOrder().getID();
                    KwlReturnObject kwlresult = null;
                    List<String> saDetailIds = null;
                    HashMap<String, Object> SADetailParams = new HashMap<String, Object>();
                    SADetailParams.put("soid", jobworkorderid);
                    kwlresult = accProductObj.getSADetailsForSO(SADetailParams);
                    saDetailIds = kwlresult.getEntityList();
                    /*
                     * Forming challanDate comma separated String
                     */
                    Set<String> saset = new HashSet<>();
                    for (String saDetailId : saDetailIds) {
                        StockAdjustmentDetail saObj = (StockAdjustmentDetail) kwlCommonTablesDAOObj.getClassObject(StockAdjustmentDetail.class.getName(), saDetailId);
                        if (!saset.contains(saObj.getStockAdjustment().getTransactionNo()) && challanInInvSet.contains(saObj.getBatchName())) {

                            /**
                             * get Balance Qty from Challan
                             */
                            Map<String, Object> requestParamsChallan = new HashMap<String, Object>();
                            requestParamsChallan.put("productid", saObj.getStockAdjustment().getProduct().getID());
                            requestParamsChallan.put("batchName", saObj.getBatchName());
                            requestParamsChallan.put("jobworkorder", soDetail.getSalesOrder().getID());
                            requestParamsChallan.put("companyId", row.getInvoice().getCompany().getCompanyID());

                            /*
                             * This Function gives the balance Quantity for
                             * products used in various invoices for a
                             * particular challan
                             */
                            KwlReturnObject result1 = accJobWorkDaoObj.getConsumeChallan(requestParamsChallan);
                            List list1 = result1.getEntityList();
                            Iterator itr1 = list1.iterator();
                            double deliveredqty = 0d;
                            double finalqty = saObj.getFinalQuantity();
                            JSONArray dataJArr = new JSONArray();
                            JSONObject nObject = new JSONObject();
                            while (itr1.hasNext()) {
                                Object[] row1 = (Object[]) itr1.next();
                               double deliveredquantity = (Double) row1[0];
                                double subproductqty = (Double) row1[3];
                                deliveredquantity = deliveredquantity * subproductqty;
                                deliveredqty = deliveredqty + deliveredquantity;
                                String donumber = (String) row1[1];
                                nObject.put("receiveqty", "-");
                                nObject.put("consumeqty", deliveredquantity);
                                nObject.put("invoicenumber", donumber);
                                nObject.put("balanceqty", finalqty - deliveredqty);
                                nObject.put("challanno", saObj.getBatchName());
                                nObject.put(donumber + "balance", finalqty - deliveredqty);
                                nObject.put(donumber + "consume", deliveredquantity);
                                challanQtyDetails.put( donumber + saObj.getBatchName() + "balance" , authHandler.formattingDecimalForQuantity((finalqty - deliveredqty), companyid));// get balance qty with specific invoice,batchname 
                                challanQtyDetails.put( donumber + saObj.getBatchName() + "consume" , authHandler.formattingDecimalForQuantity(deliveredquantity, companyid));// get balance qty with specific invoice,batchname
                                dataJArr.put(nObject);
                            }

                            challanDate += saObj.getStockAdjustment() != null ? saObj.getStockAdjustment().getBusinessDate() + "," : "";
                            saset.add(saObj.getStockAdjustment().getTransactionNo());
                        }
                    }
                    for (String saId : saset) {
                        jobIN += saId + ", ";
                    }
                }
            }
            jobj.put("challanDate",challanDate);
            jobj.put("challanQtyDetails",challanQtyDetails);
            jobj.put("jobIN",jobIN);
            jobj.put("jobWorkOrderQty",jobWorkOrderQty);
            jobj.put("jobworkorderno",jobworkorderno);
            jobj.put("jobworkorderdate", jobworkorderdate != null ? authHandler.getUserDateFormatterJson(paramJobj).format(jobworkorderdate) : "");
            
        } catch (Exception ex) {
        }
        return jobj;
    }
   public HashMap getInvoiceProductAmount(Invoice invoice) throws ServiceException {
        HashMap hm = new HashMap();
        Set invRows = invoice.getRows();
        Iterator itr = invRows.iterator();
        double amount;
        double quantity;
        while (itr.hasNext()) {
            InvoiceDetail temp = (InvoiceDetail) itr.next();
            //For the case of update inventory from DO
//            double quantityTemp = (invoice.getPendingapproval()==1)? temp.getInventory().getActquantity() : (temp.getInventory().isInvrecord() ? temp.getInventory().getQuantity() : temp.getInventory().getActquantity());
            double quantityTemp = temp.getInventory().getQuantity();
            quantity = quantityTemp;
            amount = temp.getRate() * quantity;
            if (invoice.getInvoicetype() != null && invoice.getInvoicetype().equals(Constants.Acc_Retail_Invoice_Variable)) {
                amount = temp.getRate() * (quantity / 100);
            }
            if (temp.getPartamount() != 0) {
                amount = amount * (temp.getPartamount() / 100);
            }
            double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
            double rowTaxPercent = 0;
            double rowTaxAmount = 0;
            boolean isRowTaxApplicable = false;
            if (temp.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
//                KwlReturnObject perresult = accTaxObj.getTaxPercent(invoice.getCompany().getCompanyID(), invoice.getJournalEntry().getEntryDate(), temp.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(invoice.getCompany().getCompanyID(), invoice.getCreationDate(), temp.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
                isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
            }
            if (temp.isWasRowTaxFieldEditable()) {
                if (isRowTaxApplicable) {
                    rowTaxAmount = temp.getRowTaxAmount()+temp.getRowTermTaxAmount();
                }
            } else {
                rowTaxAmount = (amount - rdisc) * rowTaxPercent / 100;
            }
            double ramount = amount - rdisc;
            double amountWithoutTax = amount - rdisc;
            ramount += rowTaxAmount;//ramount+=ramount*rowTaxPercent/100;

            hm.put(temp, new Object[]{ramount, quantity, rowTaxAmount, amountWithoutTax});
            //    hm.put(temp, new Object[]{amount-rdisc+rowTaxPercent,quantity});
            if (invoice == null) {
                invoice = temp.getInvoice();
            }
        }
        return hm;
    }
   
    public Map getInvoiceDetailsTermAmount(InvoiceDetail inv) throws ServiceException {
        Map<String, Double> map = new HashMap<>();
        double termAmount = 0;
        double taxAmount = inv.getRowTermTaxAmount();
        String invoicedetailID = inv.getID();
        KwlReturnObject listTerms = accInvoiceDAOobj.getInvoiceTermMapFromInvoiceDetail(invoicedetailID);
        List<InvoiceTermsMap> invoiceTermMaps = listTerms.getEntityList();
        for (InvoiceTermsMap itm : invoiceTermMaps) {
            termAmount += itm.getTermamount();
        }
        map.put("termamount", termAmount);
        map.put("taxamount", taxAmount);
        return map;
    }

    public HashMap getInvoiceProductAmountByDetails(Invoice invoice, Set<InvoiceDetail> rows, JournalEntry je) throws ServiceException {
        HashMap hm = new HashMap();
        Iterator itr = rows.iterator();
        double amount;
        double quantity;
        while (itr.hasNext()) {
            InvoiceDetail temp = (InvoiceDetail) itr.next();
            //For the case of update inventory from DO
//            double quantityTemp = (invoice.getPendingapproval()==1)? temp.getInventory().getActquantity() : (temp.getInventory().isInvrecord() ? temp.getInventory().getQuantity() : temp.getInventory().getActquantity());
            double quantityTemp = temp.getInventory().getQuantity();
            quantity = quantityTemp;
            amount = temp.getRate() * quantity;
            if (invoice.getInvoicetype() != null && invoice.getInvoicetype().equals(Constants.Acc_Retail_Invoice_Variable)) {
                amount = temp.getRate() * (quantity / 100);
            }
            if (temp.getPartamount() != 0) {
                amount = amount * (temp.getPartamount() / 100);
            }
            double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
            double rowTaxPercent = 0;
            double rowTaxAmount = 0;
            boolean isRowTaxApplicable = false;
            if (temp.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
//                KwlReturnObject perresult = accTaxObj.getTaxPercent(invoice.getCompany().getCompanyID(), je.getEntryDate(), temp.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(invoice.getCompany().getCompanyID(), invoice.getCreationDate(), temp.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
                isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
            }
            if (temp.isWasRowTaxFieldEditable()) {
                if (isRowTaxApplicable) {
                    rowTaxAmount = temp.getRowTaxAmount()+temp.getRowTermTaxAmount();
                }
            } else {
                rowTaxAmount = (amount - rdisc) * rowTaxPercent / 100;
            }
            double ramount = amount - rdisc;
            double amountWithoutTax = amount - rdisc;
            ramount += rowTaxAmount;//ramount+=ramount*rowTaxPercent/100;
            hm.put(temp, new Object[]{ramount, quantity, rowTaxAmount, amountWithoutTax});
            //    hm.put(temp, new Object[]{amount-rdisc+rowTaxPercent,quantity});
            if (invoice == null) {
                invoice = temp.getInvoice();
            }
        }
        return hm;
    }

    public HashMap applyBillingCreditNotes(HashMap requestParams, BillingInvoice invoice) throws ServiceException {
        HashMap hm = new HashMap();
        Set invRows = invoice.getRows();
        Iterator itr = invRows.iterator();
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), (String) requestParams.get("gcurrencyid"));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

        double amount;
        double quantity;
        double withoutDTAmt = 0;
        while (itr.hasNext()) {//reqiured for invoice discount row wise division[PS]
            BillingInvoiceDetail temp = (BillingInvoiceDetail) itr.next();
            withoutDTAmt += temp.getRate() * temp.getQuantity();
        }
        itr = invRows.iterator();
        while (itr.hasNext()) {
            BillingInvoiceDetail temp = (BillingInvoiceDetail) itr.next();
            quantity = temp.getQuantity();
            amount = temp.getRate() * quantity;
            double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
            double rowTaxPercent = 0;
            double rowTaxAmount = 0;
            boolean isRowTaxApplicable = false;
            if (temp.getTax() != null) {
                KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), invoice.getJournalEntry().getEntryDate(), temp.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
                isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
            }
            double ramount = amount - rdisc;
            if (temp.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                if (isRowTaxApplicable) {
                    rowTaxAmount = temp.getRowTaxAmount();
                }
            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                rowTaxAmount = ramount * rowTaxPercent / 100;
            }

            double ramountWD = ramount;
            ramount += rowTaxAmount;//ramount+=ramount*rowTaxPercent/100;
            double invoiceDisc = 0;//temp.getBillingInvoice().getDiscount()==null?0:applyBillingInvDisount(temp,withoutDTAmt);
            ramount -= invoiceDisc;
            hm.put(temp, new Object[]{ramount, quantity, 0.0, 0.0, ramountWD, rowTaxAmount});
            if (invoice == null) {
                invoice = temp.getBillingInvoice();
            }
        }
        KwlReturnObject result = accCreditNoteobj.getCNRowsDiscountFromBillingInvoice(invoice.getID());
        List list = result.getEntityList();
        Iterator cnitr = list.iterator();
        while (cnitr.hasNext()) {
            double taxAmount = 0, totalDiscount = 0;
            Object[] cnrow = (Object[]) cnitr.next();
            BillingCreditNoteDetail cnr = (BillingCreditNoteDetail) cnrow[1];
            BillingInvoiceDetail temp = cnr.getInvoiceRow();
            if (!hm.containsKey(temp)) {
                continue;
            }
            Object[] val = (Object[]) hm.get(temp);
            String fromcurrencyid = (cnr.getCreditNote().getCurrency() == null ? currency.getCurrencyID() : cnr.getCreditNote().getCurrency().getCurrencyID());
            String tocurrencyid = (invoice.getCurrency() == null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());
            double baseDisount = 0;
            if (cnr.getDiscount() != null) {
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, cnr.getDiscount().getDiscountValue(), fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                baseDisount = (Double) bAmt.getEntityList().get(0);
            }
            double v = (Double) val[0] - (cnr.getDiscount() == null ? 0 : baseDisount);
            if (cnr.getTaxAmount() != null) {
                taxAmount = (Double) val[2] + cnr.getTaxAmount();
            } else {
                taxAmount = (Double) val[2];
            }
            if (cnr.getTotalDiscount() != null) {
                totalDiscount = (Double) val[3] + cnr.getTotalDiscount();
            } else {
                totalDiscount = (Double) val[3];
            }
            double q = (Double) val[1];
            double ramountWD = (Double) val[4];
            q -= cnr.getQuantity();
            hm.put(temp, new Object[]{v, q, taxAmount, totalDiscount, ramountWD, 0.0});
        }
        return hm;
    }

    public double applyBillingInvDisount(BillingInvoiceDetail invdetail, double withoutDTAmt) throws ServiceException {
        double disc = (invdetail.getBillingInvoice().getDiscount() == null ? 0 : invdetail.getBillingInvoice().getDiscount().getDiscountValue());
        if (disc == 0) {
            return 0;
        }
        double quantity = invdetail.getQuantity();
        double amount = (quantity == 0 ? 0 : invdetail.getRate() * quantity);
        double rowDiscountRatio = (withoutDTAmt == 0 ? 0 : amount / withoutDTAmt);
        double rowDiscount = disc * rowDiscountRatio;
        return rowDiscount;
    }

    public List getAmountDue_Discount(HashMap<String, Object> requestParams, Invoice invoice) throws ServiceException {
        List ll = new ArrayList();
        double amountdue = 0,amountdueinbase = 0;
        double amount = 0, ramount = 0, contraamount = 0, deductDiscount = 0, amountWD = 0, termAmount = 0;
        List<Double> knockedOffAmountList = new ArrayList();//variable used to hold knocked off amounts in invoice currency
        String currencyid = (String) requestParams.get("gcurrencyid");
        String companyid = (String) requestParams.get("companyid");
        String currencyFilterForTrans = "";
        if (requestParams.containsKey("currencyfilterfortrans")) {
            currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
        }
        double amountDueOriginal = 0;
        KwlReturnObject curresult = null;//accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        String baseCurrencyID = currencyid;
        HashMap hm = accInvoiceServiceDAO.applyCreditNotes(requestParams, invoice);
        Iterator itrCn = hm.values().iterator();
        
        currencyid = (invoice.getCurrency() == null ? baseCurrencyID : invoice.getCurrency().getCurrencyID());
        double externalCurrencyRate = 0d;
        Date invCreationDate = null;
        invCreationDate = invoice.getCreationDate();
        if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
            externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
            amount = invoice.getOriginalOpeningBalanceAmount();
        } else {
            externalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
//            invCreationDate = invoice.getJournalEntry().getEntryDate();
            amount = invoice.getInvoiceamount(); 
        }
        while (itrCn.hasNext()) {
            Object[] temp = (Object[]) itrCn.next();
//            amount += (Double) temp[0] - (Double) temp[2];// Skip as we are getting invoice amount from table directly
            deductDiscount += (Double) temp[4];
            amountWD += (Double) temp[5];
        }
        boolean invoiceAmtDueEqualsInvoiceAmt = false;
        // Skip the amount due calculation if orignal amount due equals to invoice amount due 
        if (requestParams.containsKey("invoiceAmtDueEqualsInvoiceAmt") && requestParams.get("invoiceAmtDueEqualsInvoiceAmt") != null) { // check if orignal invoice amount equals to invoice amount due
            invoiceAmtDueEqualsInvoiceAmt = Boolean.parseBoolean(requestParams.get("invoiceAmtDueEqualsInvoiceAmt").toString()); // if equal, then payment hasn't been made for invoice. Skip calculation and take amount due from invoice table
        }
        if (!invoiceAmtDueEqualsInvoiceAmt) {
//            JournalEntryDetail tempd = invoice.getTaxEntry();// Skip As we are getting invoice amount from table directly
//            tempd = invoice.getTaxEntry();
//            if (tempd != null) {
//                amount += authHandler.round(tempd.getAmount(), Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
//            }

            HashMap<String, Object> reqParams1 = new HashMap();
            reqParams1.put("invoiceid", invoice.getID());
            reqParams1.put("companyid", companyid);
            if (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) {
                reqParams1.put(Constants.df, requestParams.get(Constants.df));
            }
            if (requestParams.containsKey("asofdate") && requestParams.get("asofdate") != null) {
                reqParams1.put("asofdate", requestParams.get("asofdate"));
            }

            //Get amount knock off using otherwise credit notes.
            //        KwlReturnObject result = accCreditNoteobj.getCNRowsFromInvoice(invoice.getID());
            KwlReturnObject result = accCreditNoteobj.getCNRowsFromInvoice(reqParams1);
            List<CreditNoteDetail> rows = result.getEntityList();
            double cnAmountOW = 0;
            for (CreditNoteDetail cnr : rows) {
                if (cnr.getDiscount() != null) {
                    if (cnr.getExchangeRateForTransaction() == 0) {// For Old records if exchage rate is ZERO , It will Go by Old way
                        String fromcurrencyid = (cnr.getCreditNote().getCurrency() == null ? baseCurrencyID : cnr.getCreditNote().getCurrency().getCurrencyID());
                        KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, cnr.getDiscount().getDiscountValue(), fromcurrencyid, currencyid, invCreationDate, externalCurrencyRate);
                        cnAmountOW += (Double) bAmt.getEntityList().get(0);
                    } else {
                        cnAmountOW += cnr.getDiscount().getDiscountValue() / cnr.getExchangeRateForTransaction();
                    }
                }
            }
            knockedOffAmountList.add(cnAmountOW);
            //        result = accReceiptobj.getReceiptFromInvoice(invoice.getID(), companyid);
            reqParams1.put("isApprovedPayment", true);                  //added as we need only approved receive payments
            result = accReceiptobj.getReceiptFromInvoice(reqParams1);
            
            List l = result.getEntityList();
            Iterator recitr = l.iterator();
            while (recitr.hasNext()) {
                ReceiptDetail rd = (ReceiptDetail) recitr.next();
                double receiptExchangeRate = 0d;
                Date receiptCreationDate = null;
                double ExchangeRate = 0d;
                receiptCreationDate = rd.getReceipt().getCreationDate();
                if (rd.getReceipt().isIsOpeningBalenceReceipt() && !rd.getReceipt().isNormalReceipt()) {
                    receiptExchangeRate = rd.getReceipt().getExchangeRateForOpeningTransaction();
                } else {
                    receiptExchangeRate = rd.getReceipt().getJournalEntry().getExternalCurrencyRate();
//                    receiptCreationDate = rd.getReceipt().getJournalEntry().getEntryDate();
                }
                if (rd.getFromCurrency() != null && rd.getToCurrency() != null) {
//                    ramount += rd.getAmount() / rd.getExchangeRateForTransaction();
                    ExchangeRate = rd.getExchangeRateForTransaction();
                    if (rd.getAmountDueInInvoiceCurrency() != 0) {
                        ExchangeRate = rd.getAmountDueInPaymentCurrency() / rd.getAmountDueInInvoiceCurrency();
                    }                  
                    ramount += (rd.getAmount() + rd.getDiscountAmount()) / ExchangeRate;
                } else {
                    String fromcurrencyid = (rd.getReceipt().getCurrency() == null ? baseCurrencyID : rd.getReceipt().getCurrency().getCurrencyID());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, rd.getAmount(), fromcurrencyid, currencyid, receiptCreationDate, receiptExchangeRate);
                    ramount += (Double) bAmt.getEntityList().get(0);
                }

            }
            knockedOffAmountList.add(ramount);          
            
            result = accReceiptobj.getLinkDetailReceipt(reqParams1);
            List<LinkDetailReceipt> linkDetailReceipts = result.getEntityList();
            double amountLinkedInReceipt = 0;
            if (linkDetailReceipts != null && !linkDetailReceipts.isEmpty()) {
                for (LinkDetailReceipt ldr : linkDetailReceipts) {
                    amountLinkedInReceipt += ldr.getAmountInInvoiceCurrency();
                }
            }
            knockedOffAmountList.add(amountLinkedInReceipt);
            //        TO-DO Contra Entry - Not in use now (30th Sept 2014)
            //        Get amount knock off using contra entry. 
            result = accReceiptobj.getContraPaymentFromInvoice(invoice.getID(), companyid);
            l = result.getEntityList();
            recitr = l.iterator();
            while (recitr.hasNext()) {
                PaymentDetail rd = (PaymentDetail) recitr.next();
                contraamount += rd.getAmount();
                String fromcurrencyid = (rd.getPayment().getCurrency() == null ? baseCurrencyID : rd.getPayment().getCurrency().getCurrencyID());
                //                   ramount=CompanyHandler.getOneCurrencyToOther(session,request,ramount,fromcurrencyid,currencyid,invoice.getJournalEntry().getEntryDate());
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, contraamount, fromcurrencyid, currencyid, invCreationDate, externalCurrencyRate);
                contraamount = (Double) bAmt.getEntityList().get(0);
            }
            knockedOffAmountList.add(contraamount);
            // Get amount from Invoice Terms 
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("invoiceid", invoice.getID());
//            curresult = accInvoiceDAOobj.getInvoiceTermMap(requestParam);// Skip As we are getting invoice amount from table directly
//            List<InvoiceTermsMap> termMap = curresult.getEntityList();
//            for (InvoiceTermsMap invoiceTerMap : termMap) {
//                //            InvoiceTermsSales mt = invoiceTerMap.getTerm();
//                termAmount += invoiceTerMap.getTermamount();
//            }

            KwlReturnObject resultCn = accCreditNoteobj.getDistinctCNFromInvoice(invoice.getID());
            List listCn = resultCn.getEntityList();
            Iterator cnitrTerms = listCn.iterator();
            double termAmountCn = 0;
            //        CreditNote creditNote=null;
            while (cnitrTerms.hasNext()) {
                CreditNote creditNote = (CreditNote) cnitrTerms.next();
                //            creditNote=(CreditNote) cnrow[0];         
                if (creditNote != null) {
                    requestParam.put("creditNoteId", creditNote.getID());
                    curresult = accCreditNoteobj.getCreditNoteTermMap(requestParam);
                    List<CreditNoteTermsMap> termMapCn = curresult.getEntityList();
                    for (CreditNoteTermsMap creditNoteTermsMap : termMapCn) {
                        InvoiceTermsSales mt = creditNoteTermsMap.getTerm();
                        termAmountCn += creditNoteTermsMap.getTermamount();
                    }
                }
            }
            knockedOffAmountList.add(termAmountCn);
            
            KwlReturnObject writeOffresult = accWriteOffServiceDao.getInvoiceWriteOffEntries(reqParams1);
            List<InvoiceWriteOff> writeOffList = writeOffresult.getEntityList();
            double totalWrittenOfAmount = 0;
            for (InvoiceWriteOff iwo : writeOffList) {
                totalWrittenOfAmount = iwo.getWrittenOffAmountInInvoiceCurrency();
            }

            knockedOffAmountList.add(totalWrittenOfAmount);
//            double amountDueOriginal = 0;
            // below calculation of amountdueinbase is done for removing rounding off issues in balance sheet and Aged Report
            double knockedOffAmtInBase = 0;
            for (double knockedOffAmount : knockedOffAmountList) {
                if (knockedOffAmount != 0) {
                    KwlReturnObject grAmtInBaseResult = null;
                    if (invoice.isIsOpeningBalenceInvoice() && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, knockedOffAmount, currencyid, invCreationDate, externalCurrencyRate);
                    } else {
                        grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, knockedOffAmount, currencyid, invCreationDate, externalCurrencyRate);
                    }
                    if (grAmtInBaseResult != null) {
                        //Doing round off each value before summing for matching aged amount with balance sheet in base currency 
                        knockedOffAmtInBase += authHandler.round((Double) grAmtInBaseResult.getEntityList().get(0), companyid);
                    }
                }
            }
            amountdueinbase = authHandler.round((invoice.getInvoiceamountinbase() - knockedOffAmtInBase), companyid);
            amountdue = amount - cnAmountOW - ramount - contraamount + termAmount - termAmountCn - amountLinkedInReceipt - totalWrittenOfAmount;
            if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {//amount due difference for revaluation.
                amountDueOriginal = amountdue;
                if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, invCreationDate, externalCurrencyRate);
                    amountdue = (Double) bAmt.getEntityList().get(0);
                }
            } else {
                amountdue = 0;
            }
        }
        if(invoiceAmtDueEqualsInvoiceAmt && amountdueinbase==0){
            amountdueinbase = authHandler.round(invoice.getInvoiceAmountDueInBase(), companyid);
        }
        ll.add(amountdue);
        ll.add(deductDiscount);
        ll.add(amountWD);
        ll.add(amountDueOriginal);
        ll.add(amountdueinbase);
        return ll;
    }

//    public double getAmountDueOfInvoiceBeforeClaimedDate(HashMap<String, Object> requestParams) throws ServiceException {
//        double totalAmtDue = 0d;
//        String companyid = (String) requestParams.get("companyid");
//        String invoiceId = (String) requestParams.get("invoiceId");
//
//        // get Received Amount for this invoice before claimed date
//
//        KwlReturnObject invoicePaidAmtObj = accReceiptobj.getReceiptAmountFromBadDebtClaimedInvoice(invoiceId, false);
//        double paidAmt = (Double) invoicePaidAmtObj.getEntityList().get(0);
//
//        // get Credited Amount for invoice
//
//        CreditNote creditNote = null;
//
//        KwlReturnObject cnResult = accCreditNoteobj.getCNFromInvoiceOtherwise(invoiceId, companyid);
//        List<CreditNoteDetail> cnds = cnResult.getEntityList();
//        HashSet<String> cnSet = new HashSet<String>();
//
//        for (CreditNoteDetail noteDetail : cnds) {
//            creditNote = noteDetail.getCreditNote();
//            cnSet.add(creditNote.getID());
//        }
//
//        double cnAmt = 0;
//
//        for (String cnId : cnSet) {
//            KwlReturnObject cnReturnObj = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), cnId);
//            CreditNote cn = (CreditNote) cnReturnObj.getEntityList().get(0);
//            cnAmt = cn.getCnamount();
//        }
//
//        // get Debited Amount of Customer Invoice
//
//        totalAmtDue = cnAmt + paidAmt;
//        
//        return totalAmtDue;
//
//    }
    public List getAmountDue_Discount(HashMap<String, Object> requestParams, KWLCurrency baseCurrency,
            Invoice invoice, String transactionCurrencyId, JournalEntry je,
            Map<String, List<CreditNoteInfo>> creditInvoiceMap, List<InvoiceTermsMap> invoiceTermMapList) throws ServiceException {
        List ll = new ArrayList();
        double amountdue = 0;
        double amount = 0, ramount = 0, contraamount = 0, deductDiscount = 0, amountWD = 0, termAmount = 0;
        String companyid = (String) requestParams.get("companyid");
        String currencyFilterForTrans = "";
        if (requestParams.containsKey("currencyfilterfortrans")) {
            currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
        }
        String baseCurrencyID = baseCurrency.getCurrencyID();
        Iterator itrCn = applyCreditNotes(requestParams, invoice, transactionCurrencyId, baseCurrency).values().iterator();
        String currencyid = transactionCurrencyId;
        double externalCurrencyRate = 0d;
        Date invCreationDate = null;
        invCreationDate = invoice.getCreationDate();
        if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
            externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
        } else {
            externalCurrencyRate = je.getExternalCurrencyRate();
//            invCreationDate = je.getEntryDate();
        }
        while (itrCn.hasNext()) {
            Object[] temp = (Object[]) itrCn.next();
            amount += (Double) temp[0] - (Double) temp[2];
            deductDiscount += (Double) temp[4];
            amountWD += (Double) temp[5];
        }
        JournalEntryDetail tempd = invoice.getTaxEntry();
        tempd = invoice.getTaxEntry();
        if (tempd != null) {
            amount += tempd.getAmount();
        }

        HashMap<String, Object> reqParams1 = new HashMap();
        reqParams1.put("invoiceid", invoice.getID());
        reqParams1.put("companyid", companyid);
        if (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) {
            reqParams1.put(Constants.df, requestParams.get(Constants.df));
        }
        if (requestParams.containsKey("asofdate") && requestParams.get("asofdate") != null) {
            reqParams1.put("asofdate", requestParams.get("asofdate"));
        }

        //Get amount knock off using otherwise credit notes.
        double cnAmountOW = 0;
        if (creditInvoiceMap.containsKey(invoice.getID())) {
            List<CreditNoteInfo> cninfoList = creditInvoiceMap.get(invoice.getID());
            for (CreditNoteInfo cninfo : cninfoList) {
//            CreditNoteInfo cninfo = creditInvoiceMap.get(invoice.getID());
                CreditNoteDetail cnr = (CreditNoteDetail) cninfo.getCreditNoteDetails();
                String fromcurrencyid = (cnr.getCreditNote().getCurrency() == null ? baseCurrencyID : cnr.getCreditNote().getCurrency().getCurrencyID());
                if (cnr.getDiscount() != null) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, cnr.getDiscount().getDiscountValue(), fromcurrencyid, currencyid, invCreationDate, externalCurrencyRate);
                    cnAmountOW += (Double) bAmt.getEntityList().get(0);
                }
            }
//            }
        }
        KwlReturnObject result = accReceiptobj.getReceiptFromInvoice(invoice.getID());
        List<ReceiptDetail> l = result.getEntityList();
        for (ReceiptDetail rd : l) {
            double receiptExchangeRate = 0d;
            Date receiptCreationDate = null;

            receiptCreationDate = rd.getReceipt().getCreationDate();
            if (rd.getReceipt().isIsOpeningBalenceReceipt() && !rd.getReceipt().isNormalReceipt()) {
                receiptExchangeRate = rd.getReceipt().getExchangeRateForOpeningTransaction();
            } else {
                receiptExchangeRate = rd.getReceipt().getJournalEntry().getExternalCurrencyRate();
//                receiptCreationDate = rd.getReceipt().getJournalEntry().getEntryDate();
            }

            if (rd.getFromCurrency() != null && rd.getToCurrency() != null) {
                ramount += rd.getAmount() / rd.getExchangeRateForTransaction();
            } else {
//                    ramount += rd.getAmount();
                String fromcurrencyid = (rd.getReceipt().getCurrency() == null ? baseCurrencyID : rd.getReceipt().getCurrency().getCurrencyID());
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, rd.getAmount(), fromcurrencyid, currencyid, receiptCreationDate, receiptExchangeRate);
                ramount += (Double) bAmt.getEntityList().get(0);
            }

        }

        result = accReceiptobj.getLinkDetailReceipt(reqParams1);
        List<LinkDetailReceipt> linkDetailReceipts = result.getEntityList();
        double amountLinkedInReceipt = 0;
        if (linkDetailReceipts != null && !linkDetailReceipts.isEmpty()) {
            for (LinkDetailReceipt ldr : linkDetailReceipts) {
                amountLinkedInReceipt += ldr.getAmountInInvoiceCurrency();
            }
        }
        //Get amount knock off using contra entry.
        result = accReceiptobj.getContraPaymentFromInvoice(invoice.getID(), companyid);
        List<PaymentDetail> pdList = result.getEntityList();
        for (PaymentDetail rd : pdList) {
            contraamount += rd.getAmount();
            String fromcurrencyid = (rd.getPayment().getCurrency() == null ? baseCurrencyID : rd.getPayment().getCurrency().getCurrencyID());
            KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, contraamount, fromcurrencyid, currencyid, invCreationDate, externalCurrencyRate);
            contraamount = (Double) bAmt.getEntityList().get(0);
        }
        // Get amount from Invoice Terms 
        HashMap<String, Object> requestParam = new HashMap();
        if (invoiceTermMapList != null && invoiceTermMapList.size() > 0) {
            for (InvoiceTermsMap invoiceTerMap : invoiceTermMapList) {
                termAmount += invoiceTerMap.getTermamount();
            }
        }

        KwlReturnObject resultCn = accCreditNoteobj.getDistinctCNFromInvoice(invoice.getID());
        List<CreditNote> listCn = resultCn.getEntityList();
        double termAmountCn = 0;
//        CreditNote creditNote=null;
        for (CreditNote creditNote : listCn) {
//            creditNote=(CreditNote) cnrow[0];         
            if (creditNote != null) {
                requestParam.put("creditNoteId", creditNote.getID());
                KwlReturnObject curresult = accCreditNoteobj.getCreditNoteTermMap(requestParam);
                List<CreditNoteTermsMap> termMapCn = curresult.getEntityList();
                for (CreditNoteTermsMap creditNoteTermsMap : termMapCn) {
                    InvoiceTermsSales mt = creditNoteTermsMap.getTerm();
                    termAmountCn += creditNoteTermsMap.getTermamount();
                }
            }
        }
        double amountDueOriginal = 0;
        amountdue = amount - cnAmountOW - ramount - contraamount + termAmount - termAmountCn - amountLinkedInReceipt;
        if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {//amount due difference for revaluation.
            amountDueOriginal = amountdue;
            if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, invCreationDate, externalCurrencyRate);
                amountdue = (Double) bAmt.getEntityList().get(0);
            }
        } else {
            amountdue = 0;
        }
        ll.add(amountdue);
        ll.add(deductDiscount);
        ll.add(amountWD);
        ll.add(amountDueOriginal);
        return ll;
    }

    public List getInvoiceDiscountAmountInfo(HashMap<String, Object> requestParams, Invoice invoice) throws ServiceException {
        List ll = new ArrayList();
        HashMap hm = new HashMap();
        Set<InvoiceDetail> invRows = invoice.getRows();
        double amount = 0, deductDiscount = 0, amountWD = 0;
        double amountdue = 0, amountDueOriginal = 0;
        double quantity;
        double withoutDTAmt = 0;
        String currencyFilterForTrans = "";
        if (requestParams.containsKey("currencyfilterfortrans")) {
            currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
        }
        String basecurrencyid = (String) requestParams.get("gcurrencyid");
        String currencyid = (invoice.getCurrency() == null ? basecurrencyid : invoice.getCurrency().getCurrencyID());

        double externalCurrencyRate = 0d;
        Date invCreationDate = null;
        invCreationDate = invoice.getCreationDate();
        if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
            externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
        } else {
            externalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
//            invCreationDate = invoice.getJournalEntry().getEntryDate();
        }
        if (!invoice.isCashtransaction()) {       //check invoices only
            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                amountdue = invoice.getOpeningBalanceAmountDue();
            } else {
                amountdue = invoice.getInvoiceamountdue();
            }
        }
        for (InvoiceDetail temp : invRows) {//reqiured for invoice discount row wise division[PS]
            double quantityTemp = temp.getInventory().getQuantity();
            double rowAmount = temp.getRate() * quantityTemp;
            if (invoice.getInvoicetype() != null && invoice.getInvoicetype().equals(Constants.Acc_Retail_Invoice_Variable)) {
                rowAmount = temp.getRate() * quantityTemp;
            }
            if (temp.getPartamount() != 0.0) {
                rowAmount = rowAmount * (temp.getPartamount() / 100);
            }
            withoutDTAmt += rowAmount;
        }
        for (InvoiceDetail temp : invRows) {
            double quantityTemp = temp.getInventory().getQuantity();
            quantity = quantityTemp;
            amount = temp.getRate() * quantity;
            if (temp.getPartamount() != 0.0) {
                amount = amount * (temp.getPartamount() / 100);
            }
            double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
            double rowWithDAmt = amount - rdisc;
            double invoiceDisc = temp.getInvoice().getDiscount() == null ? 0 : accInvoiceServiceDAO.applyInvDisount(temp, withoutDTAmt);
            hm.put(temp, new Object[]{0, quantity, 0.0, rowWithDAmt - invoiceDisc, 0.0, rowWithDAmt});
            if (invoice == null) {
                invoice = temp.getInvoice();
            }
        }

        Iterator itrCn = hm.values().iterator();
        while (itrCn.hasNext()) {
            Object[] temp = (Object[]) itrCn.next();
            deductDiscount += (Double) temp[4];
            amountWD += (Double) temp[5];
        }
        if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {//amount due difference for revaluation.
            amountDueOriginal = amountdue;
            if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, invCreationDate, externalCurrencyRate);
                amountdue = (Double) bAmt.getEntityList().get(0);
            }
        } else {
            amountdue = 0;
        }
        ll.add(amountdue);
        ll.add(deductDiscount);
        ll.add(amountWD);
        ll.add(amountDueOriginal);
        return ll;
    }
    
    public double getOpeningBalanceOfAccount(HttpServletRequest request, Account account, boolean isVendorOrCustomer, String vendorOrCustomerId) {
        double totalOpeningAmount = 0d;
        
//        JSONObject jobj = new JSONObject();
         try{
            JSONObject paramJObj = new JSONObject();
            try{Enumeration<String> attributes = request.getAttributeNames();
            while(attributes.hasMoreElements()){
                String attribute = attributes.nextElement();            
                paramJObj.put(attribute, request.getAttribute(attribute));
            }
//            System.out.println("attributes ended");
            Enumeration<String> parameters = request.getParameterNames();
            while(parameters.hasMoreElements()){
                String parameter = parameters.nextElement();
                paramJObj.put(parameter, request.getParameter(parameter));
            }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userDateFormat = sessionHandlerImpl.getUserDateFormat(request);
            paramJObj.put(Constants.companyKey, companyid);
            paramJObj.put("userdateformat", userDateFormat);
            paramJObj.put(Constants.companyKey, companyid);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            paramJObj.put("currencyid", currencyid);
            paramJObj.put("gcurrencyid", currencyid);
            totalOpeningAmount = getOpeningBalanceOfAccountJson(paramJObj, account, isVendorOrCustomer, vendorOrCustomerId);

        
        }
         catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
         return totalOpeningAmount;
    }

    public double getOpeningBalanceOfAccountJson(JSONObject paramJobj, Account account, boolean isVendorOrCustomer, String vendorOrCustomerId) {
        double totalOpeningAmount = 0d;
        boolean isCustomer = false;
        boolean isVendor = false;
        boolean isDepreciationAccount = false;               
        boolean isAssetPurchaseAccount = false;               

        try {
            String baseCurrency = paramJobj.optString("gcurrencyid", null);
            String companyid = paramJobj.optString("companyid");
            String filterConjuctionCriteria = paramJobj.optString("filterConjuctionCriteria",null) != null ? paramJobj.getString("filterConjuctionCriteria") : "";
            String Searchjson = paramJobj.optString("Searchjson",null) != null ? paramJobj.getString("Searchjson") : "";
            int accountTransactionType = paramJobj.optInt("accountTransactionType",Constants.All_Transaction_TypeID);// It will be zero for all transactions  otherwise it value will be transaction type value given in constant
            if (paramJobj.optString("DimensionBasedComparisionReport",null) != null && paramJobj.optString("DimensionBasedComparisionReport").equals("DimensionBasedComparisionReport")) {
                Searchjson = paramJobj.optString("DimensionBasedSearchJson",null);
            } else if (StringUtil.isNullOrEmpty(Searchjson)) {
                Searchjson = paramJobj.optString("searchJson",null) != null ? paramJobj.getString("searchJson") : "";
            }
            if (StringUtil.isNullOrEmpty(filterConjuctionCriteria)) {
                filterConjuctionCriteria = paramJobj.optString("filterConjuctionCriteria",null) != null ? paramJobj.getString("filterConjuctionCriteria") : "";
            }
            boolean shouldAccountOpeningBalanceInclude = true;
            if (account != null && account.getUsedIn() != null) {
                if (account.getUsedIn().contains(Constants.Customer_Default_Account)) {
                    isCustomer = true;
                } else if (account.getUsedIn().contains(Constants.Vendor_Default_Account)) {
                    isVendor = true;
                } else if (account.getUsedIn().equals(Constants.Depreciation_Provision_GL_Account)) {
                    isDepreciationAccount = true;
                } else if (account.getUsedIn().contains(Constants.Product_Sales_Return_Account) && account.getUsedIn().contains(Constants.Product_Purchase_Return_Account)) {
                    isAssetPurchaseAccount = true;
                }
            }
       
            if (isCustomer || isVendor || isDepreciationAccount || isVendorOrCustomer ||isAssetPurchaseAccount) {
                shouldAccountOpeningBalanceInclude = !accountHasOpeningTransactionsJson(paramJobj, account, isVendorOrCustomer, vendorOrCustomerId);
            }
            /*if(account !=null && StringUtil.equal(account.getName(), "7100 GST Input")){
             account.getName();
             account.getOpeningBalance();
             }*/

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
            if (!shouldAccountOpeningBalanceInclude) {
                // calculating amount due of opening balance Customer invoices.

                requestParams = AccInvoiceServiceHandler.getInvoiceRequestMapJson(paramJobj);//AccountingManager.getGlobalParams(request);      
                requestParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        requestParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                    }
                } else {
//                if(StringUtil.equal(account.getName(), "Trade Debtors"))
//                System.out.println(account.getName());
                    requestParams.put("accountId", account.getID());
                    requestParams.put("isAccountInvoices", true);
                }

                requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                requestParams.put("Searchjson", Searchjson);
                requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));

//                if(request.getAttribute("customerid")!=null){ //please uncomment the code order to calculate the amoundue for single customer
//                    requestParams.put(InvoiceConstants.customerid,(String)request.getAttribute("customerid"));
//                }
                if (Constants.OpeningBalanceBaseAmountFlag && accountTransactionType==Constants.All_Transaction_TypeID) {
                    KwlReturnObject result = accInvoiceDAOobj.getOpeningBalanceTotalBaseAmountForInvoices(requestParams);
                    if (result.getEntityList() != null && result.getEntityList().get(0) != null) {
                        totalOpeningAmount += (Double) result.getEntityList().get(0);
                    }
                } else if(accountTransactionType==Constants.All_Transaction_TypeID){
                    KwlReturnObject result = accInvoiceDAOobj.getOpeningBalanceInvoices(requestParams);

                    if (result.getEntityList() != null) {
                        Iterator itr = result.getEntityList().iterator();
                        while (itr.hasNext()) {
                            shouldAccountOpeningBalanceInclude = false;
                            Invoice invoice = (Invoice) itr.next();

//                            if(request.getAttribute("customerid")!=null){  //please uncomment the code order to calculate the amoundue for single customer
//                                String customerid=(String)request.getAttribute("customerid");
//                                if(!customerid.equals(invoice.getCustomer().getID())){
//                                    continue;
//                                }
//                            }
                            double externalCurrencyRate = 0d;
                            boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                            Date invoiceCreationDate = null;
                            double amountDue = 0d;
                            if (invoice.isNormalInvoice()) {
                                if (Constants.InvoiceAmountDueFlag) {
                                    List ll = getInvoiceDiscountAmountInfo(requestParams, invoice);
                                    amountDue = (Double) ll.get(0);
                                } else {
                                    List ll = getAmountDue_Discount(requestParams, invoice);
                                    amountDue = (Double) ll.get(0);
                                }
//                                invoiceCreationDate = invoice.getJournalEntry().getEntryDate();
                                externalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
                            } else {
                                amountDue = invoice.getOriginalOpeningBalanceAmount();
                                externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                            }
                            invoiceCreationDate = invoice.getCreationDate();

                            String fromcurrencyid = invoice.getCurrency().getCurrencyID();
//                    String tocurrencyid = invoice.getCustomer().getCompany().getCurrency().getCurrencyID();

//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amountDue, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }

                // Calculating Amount Due of opening balance Payment
                HashMap<String, Object> receiptParams = accReceiptController.getReceiptRequestMapJson(paramJobj);
                receiptParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        receiptParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                    }
                } else {
                    receiptParams.put("accountId", account.getID());
                    receiptParams.put("isAccountReceipts", true);
                }
                receiptParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                receiptParams.put("Searchjson", Searchjson);
                receiptParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));
                // getting opening balance receipt excluding normal Receipts
                KwlReturnObject receiptResult = null;
                receiptParams.put("excludeNormal", true);
                if (Constants.OpeningBalanceBaseAmountFlag && (accountTransactionType==Constants.All_Transaction_TypeID || accountTransactionType==Constants.Acc_Receive_Payment_ModuleId)) {
                    receiptResult = accReceiptobj.getOpeningBalanceTotalBaseAmountForReceipts(receiptParams);
                    if (receiptResult.getEntityList() != null && receiptResult.getEntityList().get(0) != null) {
                        totalOpeningAmount -= (Double) receiptResult.getEntityList().get(0);
                    }
                } else if(accountTransactionType==Constants.All_Transaction_TypeID || accountTransactionType==Constants.Acc_Receive_Payment_ModuleId){
                    receiptResult = accReceiptobj.getOpeningBalanceReceipts(receiptParams);
                    if (receiptResult.getEntityList() != null) {
                        Iterator itr = receiptResult.getEntityList().iterator();
                        while (itr.hasNext()) {
                            shouldAccountOpeningBalanceInclude = false;
                            Receipt receipt = (Receipt) itr.next();

//                            if(request.getAttribute("customerid")!=null){  //please uncomment the code order to calculate the amoundue for single customer
//                                String customerid=(String)request.getAttribute("customerid");
//                                if(!customerid.equals(receipt.getCustomer().getID())){
//                                    continue;
//                                }
//                            }
                            double externalCurrencyRate = 0d;
                            boolean isopeningBalanceRecceipt = receipt.isIsOpeningBalenceReceipt();
                            double amountDue = 0d;
                            Date receiptCreationDate = null;
//                    Receipt receipt = (Receipt) itr.next();
                            if (!receipt.isNormalReceipt() && receipt.isIsOpeningBalenceReceipt()) {
                                amountDue = receipt.getDepositAmount();
                                receiptCreationDate = receipt.getCreationDate();
                                externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                            }
                            String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceRecceipt && receipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }

                // getting opening balance receipts which are normal receipts also
                receiptParams.remove("excludeNormal");
                receiptParams.put("onlyOpeningNormalReceipts", true);

                if (account != null) {
                    receiptParams.put("accountId", account.getID());
                }

                // getting opening balance due of vendor invoices for a vendor
                requestParams = accGoodsReceiptControllerCMN.getGoodsReceiptMapJson(paramJobj);
                requestParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes

                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        requestParams.put(InvoiceConstants.vendorid, vendorOrCustomerId);
                    }
                } else {
                    requestParams.put("accountId", account.getID());
                    requestParams.put("isAccountInvoices", true);
                }

                requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                requestParams.put("Searchjson", Searchjson);
                requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));

                if (Constants.OpeningBalanceBaseAmountFlag && accountTransactionType==Constants.All_Transaction_TypeID) {
                    KwlReturnObject result = accGoodsReceiptobj.getOpeningBalanceTotalBaseAmountForInvoices(requestParams);
                    List list = result.getEntityList();
                    if (list != null && list.get(0) != null) {
                        totalOpeningAmount += (Double) list.get(0);
                    }
                } else if(accountTransactionType==Constants.All_Transaction_TypeID){
                    KwlReturnObject result = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
                    List list = result.getEntityList();
                    if (list != null) {
                        Iterator it = list.iterator();
                        while (it.hasNext()) {

                            shouldAccountOpeningBalanceInclude = false;
                            List ll = null;
                            double amountdue = 0d;
                            String grId = it.next().toString();
                            KwlReturnObject grReceiptReturnObj = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(), grId);
                            GoodsReceipt goodsReceipt = (GoodsReceipt) grReceiptReturnObj.getEntityList().get(0);
                            Date invoiceCreationDate = null;
                            double externalCurrencyRate = 0d;
                            boolean isopeningBalanceInvoice = goodsReceipt.isIsOpeningBalenceInvoice();

                            if (goodsReceipt.isNormalInvoice()) {// gr which are not created from opening balance pop-up
//                                invoiceCreationDate = goodsReceipt.getJournalEntry().getEntryDate();
                                externalCurrencyRate = goodsReceipt.getJournalEntry().getExternalCurrencyRate();

                                if (goodsReceipt.isIsExpenseType()) {
                                    ll = accGoodsReceiptCommon.getExpGRAmountDue(requestParams, goodsReceipt);
                                    amountdue = (Double) ll.get(1);
//                        belongsTo1099 = (Boolean) ll.get(3);
                                } else {
                                    if (Constants.InvoiceAmountDueFlag) {
                                        ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(requestParams, goodsReceipt);
                                        amountdue = (Double) ll.get(1);
                                    } else {
                                        ll = accGoodsReceiptCommon.getGRAmountDue(requestParams, goodsReceipt);
                                        amountdue = (Double) ll.get(1);
                                    }
                                }
                            } else {
                                amountdue = goodsReceipt.getOriginalOpeningBalanceAmount();
                                externalCurrencyRate = goodsReceipt.getExchangeRateForOpeningTransaction();
                            }
                            invoiceCreationDate = goodsReceipt.getCreationDate();

                            String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();

                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceInvoice && goodsReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }

            // getting opening balance payments excluding normal payments
                // Calculating Amount Due of opening balance Payment
                HashMap<String, Object> paymentParams = accVendorPaymentController.getPaymentMapJson(paramJobj);
                paymentParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        paymentParams.put(InvoiceConstants.vendorid, vendorOrCustomerId);
                    }
                } else {
                    paymentParams.put("accountId", account.getID());
                    paymentParams.put("isAccountPayments", true);
                }

                paymentParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                paymentParams.put("Searchjson", Searchjson);
                paymentParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));

                paymentParams.put("excludeNormal", true);

                if (Constants.OpeningBalanceBaseAmountFlag && (accountTransactionType==Constants.All_Transaction_TypeID || accountTransactionType==Constants.Acc_Make_Payment_ModuleId)) {
                    KwlReturnObject paymentresult = accVendorPaymentobj.getOpeningBalanceTotalBaseAmountForPayments(paymentParams);
                    if (paymentresult.getEntityList() != null && paymentresult.getEntityList().get(0) != null) {
                        totalOpeningAmount -= (Double) paymentresult.getEntityList().get(0);
                    }
                } else if(accountTransactionType==Constants.All_Transaction_TypeID || accountTransactionType==Constants.Acc_Make_Payment_ModuleId){
                    KwlReturnObject paymentresult = accVendorPaymentobj.getOpeningBalancePayments(paymentParams);
                    if (paymentresult.getEntityList() != null) {
                        Iterator itr = paymentresult.getEntityList().iterator();
                        while (itr.hasNext()) {

                            shouldAccountOpeningBalanceInclude = false;
                            Payment payment = (Payment) itr.next();
                            double externalCurrencyRate = 0d;
                            boolean isopeningBalancePayment = payment.isIsOpeningBalencePayment();
                            double amountDue = 0d;
                            Date receiptCreationDate = null;
//                    Receipt receipt = (Receipt) itr.next();
                            if (!payment.isNormalPayment() && payment.isIsOpeningBalencePayment()) {
                                amountDue = payment.getDepositAmount();
                                receiptCreationDate = payment.getCreationDate();
                                externalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                            }
                            String fromcurrencyid = payment.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (isopeningBalancePayment && payment.isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }

                boolean isCNForAccountIncluded = false;

                // Calculating Amount Due of opening balance CNs
                HashMap<String, Object> noteParams = accCreditNoteController.getCreditNoteMapJson(paramJobj);
                noteParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        noteParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                    }
                } else {
                    noteParams.put("accountId", account.getID());
                    noteParams.put("isAccountCNs", true);
                    isCNForAccountIncluded = true;
                }

                noteParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                noteParams.put("Searchjson", Searchjson);
                noteParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));

                // getting opening balance cn excluding normal cns
                noteParams.put("excludeNormal", true);
                if (Constants.OpeningBalanceBaseAmountFlag && accountTransactionType==Constants.All_Transaction_TypeID) {
                    KwlReturnObject cnResult = accCreditNoteobj.getOpeningBalanceTotalBaseAmountForCNs(noteParams);
                    if (cnResult.getEntityList() != null) {
                        totalOpeningAmount -= (Double) cnResult.getEntityList().get(0);
                    }
                } else if(accountTransactionType==Constants.All_Transaction_TypeID) {
                    KwlReturnObject cnResult = accCreditNoteobj.getOpeningBalanceCNs(noteParams);
                    if (cnResult.getEntityList() != null) {
                        Iterator itr = cnResult.getEntityList().iterator();
                        while (itr.hasNext()) {

                            shouldAccountOpeningBalanceInclude = false;
                            CreditNote cn = (CreditNote) itr.next();

//                            if(request.getAttribute("customerid")!=null){   //please uncomment the code order to calculate the amoundue for single customer
//                                String customerid=(String)request.getAttribute("customerid");
//                                if(!customerid.equals(cn.getCustomer().getID())){
//                                    continue;
//                                }
//                            }
                            double externalCurrencyRate = 0d;
                            double amountDue = 0d;
                            boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
                            Date cnCreationDate = null;
                            if (!cn.isNormalCN() && cn.isIsOpeningBalenceCN()) {
                                amountDue = cn.getCnamount();
                                cnCreationDate = cn.getCreationDate();
                                externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                            }
                            String fromcurrencyid = cn.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }

                boolean isDNForAccountIncluded = false;
                // Calculating Amount Due of opening balance DNs for Customers

                HashMap<String, Object> cdnParams = accDebitNoteController.gettDebitNoteMapJson(paramJobj);
                cdnParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        cdnParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                    }
                } else {
                    cdnParams.put("accountId", account.getID());
                    cdnParams.put("isAccountDNs", true);
                    isDNForAccountIncluded = true;
                }

                cdnParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                cdnParams.put("Searchjson", Searchjson);
                cdnParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));

                // getting opening balance cn excluding normal cns
                cdnParams.put("excludeNormal", true);
                if (Constants.OpeningBalanceBaseAmountFlag && accountTransactionType==Constants.All_Transaction_TypeID) {
                    KwlReturnObject cdnResult = accDebitNoteobj.getOpeningBalanceTotalBaseAmountForCustomerDNs(cdnParams);
                    if (cdnResult.getEntityList() != null) {
                        totalOpeningAmount += (Double) cdnResult.getEntityList().get(0);
                    }
                } else if(accountTransactionType==Constants.All_Transaction_TypeID){
                    KwlReturnObject cdnResult = accDebitNoteobj.getOpeningBalanceCustomerDNs(cdnParams);
                    if (cdnResult.getEntityList() != null) {
                        Iterator itr = cdnResult.getEntityList().iterator();
                        while (itr.hasNext()) {
                            shouldAccountOpeningBalanceInclude = false;

                            DebitNote cn = (DebitNote) itr.next();

//                            if(request.getAttribute("customerid")!=null){  //please uncomment the code order to calculate the amoundue for single customer
//                                String customerid=(String)request.getAttribute("customerid");
//                                if(!customerid.equals(cn.getCustomer().getID())){
//                                    continue;
//                                }
//                            }
                            double externalCurrencyRate = 0d;
                            boolean isopeningBalanceDN = cn.isIsOpeningBalenceDN();
                            double amountDue = 0d;
                            Date cnCreationDate = null;
                            if (!cn.isNormalDN() && cn.isIsOpeningBalenceDN()) {
                                amountDue = cn.getDnamount();
                                cnCreationDate = cn.getCreationDate();
                                externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                            }
                            String fromcurrencyid = cn.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceDN && cn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }

                // Calculating Amount Due of opening balance DNs
                HashMap<String, Object> dnParams = accDebitNoteController.gettDebitNoteMapJson(paramJobj);
                dnParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        dnParams.put("vendorid", vendorOrCustomerId);
                    }
                } else {
                    dnParams.put("accountId", account.getID());
                    dnParams.put("isAccountDNs", true);
                }

                dnParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                dnParams.put("Searchjson", Searchjson);
                dnParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));
                // getting opening balance cn excluding normal dns
                dnParams.put("excludeNormal", true);
                if (Constants.OpeningBalanceBaseAmountFlag && accountTransactionType==Constants.All_Transaction_TypeID) {
                    KwlReturnObject dnResult = accDebitNoteobj.getOpeningBalanceTotalBaseAmountForDNs(dnParams);
                    if (dnResult.getEntityList() != null) {
                        totalOpeningAmount -= (Double) dnResult.getEntityList().get(0);
                    }
                } else if(accountTransactionType==Constants.All_Transaction_TypeID){
                    KwlReturnObject dnResult = accDebitNoteobj.getOpeningBalanceDNs(dnParams);
                    if (dnResult.getEntityList() != null) {
                        Iterator itr = dnResult.getEntityList().iterator();
                        while (itr.hasNext()) {
                            shouldAccountOpeningBalanceInclude = false;

                            DebitNote cn = (DebitNote) itr.next();

//                            if(request.getAttribute("customerid")!=null){  //please uncomment the code order to calculate the amoundue for single customer
//                                String customerid=(String)request.getAttribute("customerid");
//                                if(!customerid.equals(cn.getCustomer().getID())){
//                                    continue;
//                                }
//                            }
                            double externalCurrencyRate = 0d;
                            boolean isopeningBalanceDN = cn.isIsOpeningBalenceDN();
                            double amountDue = 0d;
                            Date cnCreationDate = null;
                            if (!cn.isNormalDN() && cn.isIsOpeningBalenceDN()) {
                                amountDue = cn.getDnamount();
                                cnCreationDate = cn.getCreationDate();
                                externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                            }
                            String fromcurrencyid = cn.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceDN && cn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }

                // Calculating Amount Due of opening balance CNs for Vendors
                HashMap<String, Object> vcnParams = accCreditNoteController.getCreditNoteMapJson(paramJobj);
                vcnParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        vcnParams.put("vendorid", vendorOrCustomerId);
                    }
                } else {
                    vcnParams.put("accountId", account.getID());
                    vcnParams.put("isAccountCNs", true);
                }

                vcnParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                vcnParams.put("Searchjson", Searchjson);
                vcnParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));

                // getting opening balance cn excluding normal cns
                vcnParams.put("excludeNormal", true);
                if (Constants.OpeningBalanceBaseAmountFlag && accountTransactionType==Constants.All_Transaction_TypeID) {
                    KwlReturnObject vcnResult = accCreditNoteobj.getOpeningBalanceTotalBaseAmountForVendorCNs(vcnParams);
                    if (vcnResult.getEntityList() != null) {
                        totalOpeningAmount += (Double) vcnResult.getEntityList().get(0);
                    }
                } else if(accountTransactionType==Constants.All_Transaction_TypeID){
                    KwlReturnObject vcnResult = accCreditNoteobj.getOpeningBalanceVendorCNs(vcnParams);
                    if (vcnResult.getEntityList() != null) {
                        Iterator itr = vcnResult.getEntityList().iterator();
                        while (itr.hasNext()) {
                            shouldAccountOpeningBalanceInclude = false;

                            CreditNote cn = (CreditNote) itr.next();

//                            if(request.getAttribute("customerid")!=null){  //please uncomment the code order to calculate the amoundue for single customer
//                                String customerid=(String)request.getAttribute("customerid");
//                                if(!customerid.equals(cn.getCustomer().getID())){
//                                    continue;
//                                }
//                            }
                            double externalCurrencyRate = 0d;
                            double amountDue = 0d;
                            boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
                            Date cnCreationDate = null;
                            if (!cn.isNormalCN() && cn.isIsOpeningBalenceCN()) {
                                amountDue = cn.getCnamount();
                                cnCreationDate = cn.getCreationDate();
                                externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                            }
                            String fromcurrencyid = cn.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }
                if (!isVendorOrCustomer && accountTransactionType==Constants.All_Transaction_TypeID) {
                    if (account != null && account.getUsedIn() != null) {
                        // If Account is mapped to Provision for the Fixed Asset (Balance Sheet)
                        if (Constants.Depreciation_Provision_GL_Account.equals(account.getUsedIn())) {
                            HashMap<String, Object> reqParams = new HashMap<String, Object>();
                            companyid = paramJobj.getString(Constants.companyKey);
                            reqParams.put("accountid", account.getID());
                            reqParams.put("companyid", companyid);
                            String SearchJsonForAsset = Searchjson;
                            if (!StringUtil.isNullOrEmpty(SearchJsonForAsset)) {
                                /*
                                 * Create Json for Opening asset Search
                                 */
                                SearchJsonForAsset = getJsonStringForSearch(SearchJsonForAsset, companyid);
                            }
                            reqParams.put("searchJson", SearchJsonForAsset);
                            reqParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                            KwlReturnObject result1 = accProductObj.getOpeningDepreciationForAccount(reqParams);
                            List list1 = result1.getEntityList();
                            totalOpeningAmount -= (Double) list1.get(0);
                        } else if (account.getUsedIn().contains(Constants.Product_Sales_Return_Account) && account.getUsedIn().contains(Constants.Product_Purchase_Return_Account)) {
                            HashMap<String, Object> reqParams1 = new HashMap<String, Object>();
                            companyid = paramJobj.getString(Constants.companyKey);
                            reqParams1.put("accountid", account.getID());
                            reqParams1.put("companyid", companyid);
                            String SearchJsonForAsset = Searchjson;
                            if (!StringUtil.isNullOrEmpty(SearchJsonForAsset)) {
                                /*
                                 * Create Json for Opening asset Search
                                 */
                                SearchJsonForAsset = getJsonStringForSearch(SearchJsonForAsset, companyid);
                            }
                            reqParams1.put("searchJson", SearchJsonForAsset);
                            reqParams1.put("filterConjuctionCriteria", filterConjuctionCriteria);
                            KwlReturnObject result1 = accProductObj.getAssetPurchaseOpeningForAccount(reqParams1);
                            List list1 = result1.getEntityList();
                            totalOpeningAmount += (Double) list1.get(0);
                        }
                    }
                }
            }
           
            boolean isSplitOpeningBalanceAmount = false;
            boolean isSplitOpeningBalanceSearch = false;
            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                isSplitOpeningBalanceAmount = isSplitOpeningBalanceAmount(paramJobj.getString(Constants.companyKey));
                isSplitOpeningBalanceSearch = isSplitOpeningBalanceSearch(Searchjson, paramJobj.getString(Constants.companyKey));
            }
            /*
             isSplitOpeningBalanceSearch = false in case if search field not for Opening balance of account
             */

            // we are not shhowing Account opening balance at Creation time in Advance Search
            if (account != null && shouldAccountOpeningBalanceInclude && account.getOpeningBalance() != 0) {
                if(accountTransactionType!=Constants.All_Transaction_TypeID){ //Non zero value came for GL case: when report filtered based on transaction type. In this case we need opening only of transaction type not given at the time of creation. So putting it as zero
                    totalOpeningAmount = 0;
                } else if (!isSplitOpeningBalanceAmount && StringUtil.isNullOrEmpty(Searchjson)) {   // when splitting of opening false and not advance search
                    totalOpeningAmount = account.getOpeningBalance();
                    if(StringUtil.isNullOrEmpty(baseCurrency) || !account.getCurrency().getCurrencyID().equals(baseCurrency)){
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalOpeningAmount, account.getCurrency().getCurrencyID(), account.getCreationDate(), 0);
                        totalOpeningAmount = (Double) bAmt.getEntityList().get(0);
                    }
                } else if (isSplitOpeningBalanceAmount && isSplitOpeningBalanceSearch) {   // calculate split balance
                    totalOpeningAmount = account.getOpeningBalance();
                    if (!StringUtil.isNullOrEmpty(Searchjson)) {
                        List l = getSplitOpeningBalance(Searchjson, account.getID(), account.getCompany().getCompanyID());
                        if (l.size() > 0) {
                            boolean issearch = Boolean.parseBoolean(l.get(0).toString());
                            if (issearch) {
                                totalOpeningAmount = Double.parseDouble(l.get(1).toString());
                            }
                        }
                    }
                    if(StringUtil.isNullOrEmpty(baseCurrency) || !account.getCurrency().getCurrencyID().equals(baseCurrency)){
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalOpeningAmount, account.getCurrency().getCurrencyID(), account.getCreationDate(), 0);
                        totalOpeningAmount = (Double) bAmt.getEntityList().get(0);
                    }
                }
            } else if (account != null && account.getGroup().getNature() == 0) { // amount will be (-)ve if nature of account is Liability. because of to show (+)ve amount to Creditor account in balance sheet, Ledger Report, Trail Balance etc.
                totalOpeningAmount = -totalOpeningAmount;
            }

            String totalOpeningAmountFormatted = authHandler.formattedAmount(totalOpeningAmount, companyid);
            totalOpeningAmount = Double.parseDouble(totalOpeningAmountFormatted);

        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return totalOpeningAmount;
    }
    
    public String getJsonStringForSearch(String Searchjson, String companyid) throws ServiceException, UnsupportedEncodingException {
        String returnStr = "";
        try {
            JSONArray jArray = new JSONArray();
            JSONObject jSONObject = new JSONObject();
            JSONObject jobjSearch = new JSONObject(Searchjson);
            int count = jobjSearch.getJSONArray(Constants.root).length();
            int noOfSearchField = 0;
            for (int i = 0; i < count; i++) {
                KwlReturnObject result = null;
                KwlReturnObject resultdata = null;
                JSONObject jobj1 = jobjSearch.getJSONArray(Constants.root).getJSONObject(i);
                String[] arr = null;
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, StringUtil.DecodeText(jobj1.optString("columnheader"))));
                result = accAccountDAOobj.getFieldParams(requestParams);
                List<FieldParams> lst = result.getEntityList();
                int noOfModules=0;
                for (FieldParams tmpcontyp : lst) {
                    noOfModules++;
                    if (tmpcontyp.getModuleid() == Constants.Acc_FixedAssets_Details_ModuleId) {
                        JSONObject jobj = new JSONObject();
                        jobj.put("column", tmpcontyp.getId());
                        jobj.put("refdbname", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                        jobj.put("xfield", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                        jobj.put("iscustomcolumn", jobj1.getString("iscustomcolumn"));
                        jobj.put("iscustomcolumndata", tmpcontyp.isIsForKnockOff()? (tmpcontyp.getCustomcolumn() == 1 ? "true" : "false") : jobj1.getString("iscustomcolumndata"));
                        jobj.put("isfrmpmproduct", jobj1.getString("isfrmpmproduct"));
                        jobj.put("fieldtype", tmpcontyp.getFieldtype());
                        if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12) {
                            String[] coldataArray = StringUtil.DecodeText(jobj1.optString("combosearch")).split(",");
                            String Searchstr = "";
                            String Coldata = "";
                            for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                Coldata += "'" + coldataArray[countArray] + "',";
                            }
                            Coldata = Coldata.substring(0, Coldata.length() - 1);
                            Searchstr = fieldManagerDAOobj.getIdsUsingParamsValue(tmpcontyp.getId(), Coldata.replaceAll("'", ""));
                            jobj.put("searchText", Searchstr);
                            jobj.put("search", Searchstr);
                        } else {
                            jobj.put("searchText", jobj1.getString("searchText"));
                            jobj.put("search", jobj1.getString("searchText"));
                        }
                        jobj.put("columnheader", StringUtil.DecodeText(jobj1.optString("columnheader")));
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
                        break;
                    } else {
                        if (noOfModules == lst.size()) {
                            jArray.put(jobj1);
                        }
                    }
                    
                }
            }
            jSONObject.put("root", jArray);
            returnStr = jSONObject.toString();

        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnStr;

    }
    public List getSplitOpeningBalance(String serchJson, String accid, String companyid) throws ServiceException {
        List returnlist = new ArrayList();
        double amount = 0d;
        boolean doAdvanceSearch = false;
        try {
            JSONObject jobj = new JSONObject(serchJson);
            int count = jobj.getJSONArray(Constants.root).length();
            for (int i = 0; i < count; i++) {
                JSONObject jobj1 = jobj.getJSONArray(Constants.root).getJSONObject(i);
                String fieldId = jobj1.optString("column");
                String fieldLabel = jobj1.optString("columnheader");
                fieldLabel = StringUtil.DecodeText(fieldLabel);
                String value = StringUtil.DecodeText(jobj1.optString("combosearch"));
                KwlReturnObject result = accAccountDAOobj.fieldForOpeningBalance(fieldLabel, Constants.Account_Statement_ModuleId, companyid);
                if (!result.getEntityList().isEmpty() && result.getEntityList().size() > 0) {
                    FieldParams fieldParams = (FieldParams) result.getEntityList().get(0);
                    fieldId = fieldParams.getId();
                    String arraySearchstr[] = value.split(",");
                    for (int searchlength = 0; searchlength < arraySearchstr.length; searchlength++) {
                        String comboId = accAccountDAOobj.getComboIdForAccount(arraySearchstr[searchlength], fieldId);  // get fieldComboId for GL module
                        KwlReturnObject result1 = accAccountDAOobj.getSplitAccountAmount(comboId, accid);
                        List list = result1.getEntityList();
                        for (Object liObject : list) {
                            DistributeBalance distributeBalance = (DistributeBalance) liObject;
                            amount += distributeBalance.getOpeningbal();
                        }
                    }
                    doAdvanceSearch = true;
                }
            }
            returnlist.add(doAdvanceSearch);
            returnlist.add(amount);
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnlist;
    }

    public boolean isSplitOpeningBalanceSearch(String serchJson, String companyid) throws ServiceException {
        boolean flag = true;
        int GLmoduleCounter = 0;
        try {
            if (!StringUtil.isNullOrEmpty(serchJson)) {
                JSONObject jobj = new JSONObject(serchJson);
                int count = jobj.getJSONArray(Constants.root).length();
                for (int i = 0; i < count; i++) {
                    JSONObject jobj1 = jobj.getJSONArray(Constants.root).getJSONObject(i);
                    String fieldLabel = jobj1.optString("columnheader");
                    fieldLabel = StringUtil.DecodeText(fieldLabel);
                    String fieldId = jobj1.optString("column");
                    KwlReturnObject result = accAccountDAOobj.fieldForOpeningBalance(fieldLabel, Constants.Account_Statement_ModuleId, companyid);
                    if (result.getEntityList().isEmpty()) {
                        GLmoduleCounter++;
                    }
                }
                if (GLmoduleCounter == count) {     // if all search fields are GL type
                    flag = false;
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return flag;
    }
    public boolean isSearchFieldForAsset(HashMap<String, Object> hashMap) throws ServiceException {
        String serchJson = "";
        String companyid = "";
        if (hashMap.containsKey("searchJson") && hashMap.get("searchJson") != null) {
            serchJson = hashMap.get("searchJson").toString();
        }
        if (hashMap.containsKey("companyid") && hashMap.get("companyid") != null) {
            companyid = hashMap.get("companyid").toString();
        }
        boolean searchJsonContainsAssetField = false;
        int otherThanAsset = 0;
        try {
            if (!StringUtil.isNullOrEmpty(serchJson)) {
                JSONObject jobj = new JSONObject(serchJson);
                int count = jobj.getJSONArray(Constants.root).length();
                for (int i = 0; i < count; i++) {
                    JSONObject jobj1 = jobj.getJSONArray(Constants.root).getJSONObject(i);
                    String fieldLabel = jobj1.optString("columnheader");
                    fieldLabel = StringUtil.DecodeText(fieldLabel);
                    String fieldId = jobj1.optString("column");
                    KwlReturnObject result = accAccountDAOobj.fieldForOpeningBalance(fieldLabel, Constants.Acc_FixedAssets_Details_ModuleId, companyid);
                    if (result.getEntityList().isEmpty()) {
                        otherThanAsset++;
                    } else {
                        searchJsonContainsAssetField = true;
                        break;
                    }
                }
                if (otherThanAsset == count) {     // if all search fields are other GL type
                    searchJsonContainsAssetField = false;
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return searchJsonContainsAssetField;
    }
    public boolean isSplitOpeningBalanceAmount(String companyid) throws ServiceException {
        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
        return extraCompanyPreferences.isSplitOpeningBalanceAmount();
    }

    public double getOpeningBalanceAmountDueOfAccount(HttpServletRequest request, Account account, boolean isVendorOrCustomer, String vendorOrCustomerId) {
        double totalOpeningAmount = 0d;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean shouldAccountOpeningBalanceInclude = true;

            if (account != null && StringUtil.equal(account.getName(), "Trade Debtors")) {
                account.getName();
            }

            // calculating amount due of opening balance Customer invoices.
            HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);//AccountingManager.getGlobalParams(request);
            requestParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    requestParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
//                if(StringUtil.equal(account.getName(), "Trade Debtors"))
//                System.out.println(account.getName());
                requestParams.put("accountId", account.getID());
                requestParams.put("isAccountInvoices", true);
            }
            KwlReturnObject result = null;
            if (Constants.OpeningBalanceBaseAmountFlag) {
                result = accInvoiceDAOobj.getOpeningBalanceTotalBaseAmountDueForInvoices(requestParams);
                if (result.getEntityList() != null && result.getEntityList().get(0) != null) {
                    totalOpeningAmount += (Double) result.getEntityList().get(0);
                }
            } else {
                result = accInvoiceDAOobj.getOpeningBalanceInvoices(requestParams);
                if (result.getEntityList() != null) {
                    Iterator itr = result.getEntityList().iterator();
                    while (itr.hasNext()) {
                        shouldAccountOpeningBalanceInclude = false;
                        Invoice invoice = (Invoice) itr.next();

                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                        Date invoiceCreationDate = null;
                        double amountDue = 0d;
                        if (invoice.isNormalInvoice()) {
                            if (Constants.InvoiceAmountDueFlag) {
                                List ll = getInvoiceDiscountAmountInfo(requestParams, invoice);
                                amountDue = (Double) ll.get(0);
                            } else {
                                List ll = getAmountDue_Discount(requestParams, invoice);
                                amountDue = (Double) ll.get(0);
                            }
//                            invoiceCreationDate = invoice.getJournalEntry().getEntryDate();
                            externalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
                        } else {
                            amountDue = invoice.getOpeningBalanceAmountDue();
                            externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                        }
                        invoiceCreationDate = invoice.getCreationDate();

                        String fromcurrencyid = invoice.getCurrency().getCurrencyID();
//                    String tocurrencyid = invoice.getCustomer().getCompany().getCurrency().getCurrencyID();

//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amountDue, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        }
                        totalOpeningAmount += (Double) bAmt.getEntityList().get(0);

                    }
                }
            }

            // Calculating Amount Due of opening balance Payment
            HashMap<String, Object> receiptParams = accReceiptController.getReceiptRequestMap(request);
            receiptParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    receiptParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
                receiptParams.put("accountId", account.getID());
                receiptParams.put("isAccountReceipts", true);
            }
            KwlReturnObject receiptResult = null;
            // getting opening balance receipt excluding normal Receipts
            receiptParams.put("excludeNormal", true);
            if (Constants.OpeningBalanceBaseAmountFlag) {
                receiptResult = accReceiptobj.getOpeningBalanceTotalBaseAmountDueForReceipts(receiptParams);
                if (receiptResult.getEntityList() != null && receiptResult.getEntityList().get(0) != null) {
                    totalOpeningAmount -= (Double) receiptResult.getEntityList().get(0);
                    HashMap<String, Object> writeOffParams = new HashMap();
                    writeOffParams.put("onlyOpeningWrittenOffReceipts", true);
                    writeOffParams.put("customerid", vendorOrCustomerId);
                    KwlReturnObject result1 = accReceiptobj.getReceiptWriteOffEntries(writeOffParams);
                    List<ReceiptWriteOff> R = result1.getEntityList();
                    for (ReceiptWriteOff RWO : R) {
                        totalOpeningAmount += RWO.getWrittenOffAmountInBaseCurrency();
                    }
                }
            } else {
                receiptResult = accReceiptobj.getOpeningBalanceReceipts(receiptParams);
                if (receiptResult.getEntityList() != null) {
                    Iterator itr = receiptResult.getEntityList().iterator();
                    while (itr.hasNext()) {
                        shouldAccountOpeningBalanceInclude = false;
                        Receipt receipt = (Receipt) itr.next();
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceRecceipt = receipt.isIsOpeningBalenceReceipt();
                        double amountDue = 0d;
                        Date receiptCreationDate = null;
//                    Receipt receipt = (Receipt) itr.next();
                        if (!receipt.isNormalReceipt() && receipt.isIsOpeningBalenceReceipt()) {
                            amountDue = receipt.getOpeningBalanceAmountDue();
                            receiptCreationDate = receipt.getCreationDate();
                            externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                        }
                        String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceRecceipt && receipt.isConversionRateFromCurrencyToBase()) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                        }
                        totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);

                    }
                }
            }

            // getting opening balance receipts which are normal receipts also
            receiptParams.remove("excludeNormal");
            receiptParams.put("onlyOpeningNormalReceipts", true);

            if (account != null) {
                receiptParams.put("accountId", account.getID());
            }

            // getting opening balance due of vendor invoices for a vendor
            requestParams = accGoodsReceiptControllerCMN.getGoodsReceiptMap(request);
            requestParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    requestParams.put(InvoiceConstants.vendorid, vendorOrCustomerId);
                }
            } else {
                requestParams.put("accountId", account.getID());
                requestParams.put("isAccountInvoices", true);
            }

            if (Constants.OpeningBalanceBaseAmountFlag) {
                result = accGoodsReceiptobj.getOpeningBalanceTotalBaseAmountDueForInvoices(requestParams);
                if (result.getEntityList() != null && result.getEntityList().get(0) != null) {
                    totalOpeningAmount += (Double) result.getEntityList().get(0);
                }
            } else {
                result = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
                List list = result.getEntityList();
                if (list != null) {
                    Iterator it = list.iterator();
                    while (it.hasNext()) {

                        shouldAccountOpeningBalanceInclude = false;
                        List ll = null;
                        double amountdue = 0d;
                        String grId = it.next().toString();
                        KwlReturnObject grReceiptReturnObj = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(), grId);
                        GoodsReceipt goodsReceipt = (GoodsReceipt) grReceiptReturnObj.getEntityList().get(0);
                        Date invoiceCreationDate = null;
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceInvoice = goodsReceipt.isIsOpeningBalenceInvoice();

                        if (goodsReceipt.isNormalInvoice()) {// gr which are not created from opening balance pop-up
//                            invoiceCreationDate = goodsReceipt.getJournalEntry().getEntryDate();
                            externalCurrencyRate = goodsReceipt.getJournalEntry().getExternalCurrencyRate();

                            if (goodsReceipt.isIsExpenseType()) {
                                ll = accGoodsReceiptCommon.getExpGRAmountDue(requestParams, goodsReceipt);
                                amountdue = (Double) ll.get(1);
//                        belongsTo1099 = (Boolean) ll.get(3);
                            } else {
                                if (Constants.InvoiceAmountDueFlag) {
                                    ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(requestParams, goodsReceipt);
                                    amountdue = (Double) ll.get(1);
                                } else {
                                    ll = accGoodsReceiptCommon.getGRAmountDue(requestParams, goodsReceipt);
                                    amountdue = (Double) ll.get(1);
                                }
                            }
                        } else {
                            amountdue = goodsReceipt.getOpeningBalanceAmountDue();
                            externalCurrencyRate = goodsReceipt.getExchangeRateForOpeningTransaction();
                        }
                        invoiceCreationDate = goodsReceipt.getCreationDate();

                        String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();

                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceInvoice && goodsReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        }
                        totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                    }
                }
            }

            // getting opening balance payments excluding normal payments
           // Calculating Amount Due of opening balance Payment
            HashMap<String, Object> paymentParams = accVendorPaymentController.getPaymentMap(request);
            paymentParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    paymentParams.put(InvoiceConstants.vendorid, vendorOrCustomerId);
                }
            } else {
                paymentParams.put("accountId", account.getID());
                paymentParams.put("isAccountPayments", true);
            }

            paymentParams.put("excludeNormal", true);

            if (Constants.OpeningBalanceBaseAmountFlag) {
                KwlReturnObject paymentresult = accVendorPaymentobj.getOpeningBalanceTotalBaseAmountDueForPayments(paymentParams);
                if (paymentresult.getEntityList() != null && paymentresult.getEntityList().get(0) != null) {
                    totalOpeningAmount -= (Double) paymentresult.getEntityList().get(0);
                }
            } else {
                KwlReturnObject paymentresult = accVendorPaymentobj.getOpeningBalancePayments(paymentParams);
                if (paymentresult.getEntityList() != null) {
                    Iterator itr = paymentresult.getEntityList().iterator();
                    while (itr.hasNext()) {

                        shouldAccountOpeningBalanceInclude = false;
                        Payment payment = (Payment) itr.next();
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalancePayment = payment.isIsOpeningBalencePayment();
                        double amountDue = 0d;
                        Date receiptCreationDate = null;
//                    Receipt receipt = (Receipt) itr.next();
                        if (!payment.isNormalPayment() && payment.isIsOpeningBalencePayment()) {
                            amountDue = payment.getOpeningBalanceAmountDue();
                            receiptCreationDate = payment.getCreationDate();
                            externalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                        }
                        String fromcurrencyid = payment.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalancePayment && payment.isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                        }
                        totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);

                    }
                }
            }

            // Calculating Amount Due of opening balance CNs
            HashMap<String, Object> noteParams = accCreditNoteController.getCreditNoteMap(request);
            noteParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    noteParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
                noteParams.put("accountId", account.getID());
                noteParams.put("isAccountCNs", true);
            }

            // getting opening balance cn excluding normal cns
            noteParams.put("excludeNormal", true);
            if (Constants.OpeningBalanceBaseAmountFlag) {
                KwlReturnObject cnResult = accCreditNoteobj.getOpeningBalanceTotalBaseAmountDueForCNs(noteParams);
                if (cnResult.getEntityList() != null && cnResult.getEntityList().get(0) != null) {
                    totalOpeningAmount -= (Double) cnResult.getEntityList().get(0);
                }
            } else {
                KwlReturnObject cnResult = accCreditNoteobj.getOpeningBalanceCNs(noteParams);
                if (cnResult.getEntityList() != null) {
                    Iterator itr = cnResult.getEntityList().iterator();
                    while (itr.hasNext()) {

                        shouldAccountOpeningBalanceInclude = false;
                        CreditNote cn = (CreditNote) itr.next();
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
                        double amountDue = 0d;
                        Date cnCreationDate = null;
                        if (!cn.isNormalCN() && cn.isIsOpeningBalenceCN()) {
                            amountDue = cn.getOpeningBalanceAmountDue();
                            cnCreationDate = cn.getCreationDate();
                            externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                        }
                        String fromcurrencyid = cn.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        }

                        totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);
                    }
                }
            }
            // Calculating Amount Due of opening balance DNs for Customers

            HashMap<String, Object> cdnParams = accDebitNoteController.gettDebitNoteMap(request);
            cdnParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    cdnParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
                cdnParams.put("accountId", account.getID());
                cdnParams.put("isAccountDNs", true);
            }

            // getting opening balance cn excluding normal cns
            cdnParams.put("excludeNormal", true);
            if (Constants.OpeningBalanceBaseAmountFlag) {
                KwlReturnObject cdnResult = accDebitNoteobj.getOpeningBalanceTotalBaseAmountDueForCustomerDNs(cdnParams);
                if (cdnResult.getEntityList() != null && cdnResult.getEntityList().get(0) != null) {
                    totalOpeningAmount += (Double) cdnResult.getEntityList().get(0);
                }
            } else {
                KwlReturnObject cdnResult = accDebitNoteobj.getOpeningBalanceCustomerDNs(cdnParams);
                if (cdnResult.getEntityList() != null) {
                    Iterator itr = cdnResult.getEntityList().iterator();
                    while (itr.hasNext()) {
                        shouldAccountOpeningBalanceInclude = false;

                        DebitNote cn = (DebitNote) itr.next();
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceDN = cn.isIsOpeningBalenceDN();
                        double amountDue = 0d;
                        Date cnCreationDate = null;
                        if (!cn.isNormalDN() && cn.isIsOpeningBalenceDN()) {
                            amountDue = cn.getOpeningBalanceAmountDue();
                            cnCreationDate = cn.getCreationDate();
                            externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                        }
                        String fromcurrencyid = cn.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceDN && cn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        }
                        totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                    }
                }
            }

            // Calculating Amount Due of opening balance DNs
            HashMap<String, Object> dnParams = accDebitNoteController.gettDebitNoteMap(request);
            dnParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    dnParams.put("vendorid", vendorOrCustomerId);
                }
            } else {
                dnParams.put("accountId", account.getID());
                dnParams.put("isAccountDNs", true);
            }

            // getting opening balance cn excluding normal dns
            dnParams.put("excludeNormal", true);
            if (Constants.OpeningBalanceBaseAmountFlag) {
                KwlReturnObject dnResult = accDebitNoteobj.getOpeningBalanceTotalBaseAmountDueForDNs(dnParams);
                if (dnResult.getEntityList() != null && dnResult.getEntityList().get(0) != null) {
                    totalOpeningAmount -= (Double) dnResult.getEntityList().get(0);
                }
            } else {
                KwlReturnObject dnResult = accDebitNoteobj.getOpeningBalanceDNs(dnParams);
                if (dnResult.getEntityList() != null) {
                    Iterator itr = dnResult.getEntityList().iterator();
                    while (itr.hasNext()) {
                        shouldAccountOpeningBalanceInclude = false;

                        DebitNote cn = (DebitNote) itr.next();
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceDN = cn.isIsOpeningBalenceDN();
                        double amountDue = 0d;
                        Date cnCreationDate = null;
                        if (!cn.isNormalDN() && cn.isIsOpeningBalenceDN()) {
                            amountDue = cn.getOpeningBalanceAmountDue();
                            cnCreationDate = cn.getCreationDate();
                            externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                        }
                        String fromcurrencyid = cn.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceDN && cn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        }

                        totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);
                    }
                }
            }
            // Calculating Amount Due of opening balance CNs for Vendors

            HashMap<String, Object> vcnParams = accCreditNoteController.getCreditNoteMap(request);
            vcnParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    vcnParams.put("vendorid", vendorOrCustomerId);
                }
            } else {
                vcnParams.put("accountId", account.getID());
                vcnParams.put("isAccountCNs", true);
            }

            // getting opening balance cn excluding normal cns
            vcnParams.put("excludeNormal", true);
            if (Constants.OpeningBalanceBaseAmountFlag) {
                KwlReturnObject vcnResult = accCreditNoteobj.getOpeningBalanceTotalBaseAmountDueForVendorCNs(vcnParams);
                if (vcnResult.getEntityList() != null && vcnResult.getEntityList().get(0) != null) {
                    totalOpeningAmount += (Double) vcnResult.getEntityList().get(0);
                }
            } else {
                KwlReturnObject vcnResult = accCreditNoteobj.getOpeningBalanceVendorCNs(vcnParams);
                if (vcnResult.getEntityList() != null) {
                    Iterator itr = vcnResult.getEntityList().iterator();
                    while (itr.hasNext()) {
                        shouldAccountOpeningBalanceInclude = false;

                        CreditNote cn = (CreditNote) itr.next();
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
                        double amountDue = 0d;
                        Date cnCreationDate = null;
                        if (!cn.isNormalCN() && cn.isIsOpeningBalenceCN()) {
                            amountDue = cn.getOpeningBalanceAmountDue();
                            cnCreationDate = cn.getCreationDate();
                            externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                        }
                        String fromcurrencyid = cn.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        }

                        totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                    }
                }
            }

            if (account != null && shouldAccountOpeningBalanceInclude) {
                totalOpeningAmount = account.getOpeningBalance();
            }

            String totalOpeningAmountFormatted = authHandler.formattedAmount(totalOpeningAmount, companyid);
            totalOpeningAmount = Double.parseDouble(totalOpeningAmountFormatted);

        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return totalOpeningAmount;
    }
    
  /*Request Dependency Removed*/  
    public double getOpeningBalanceAmountDueOfAccount(JSONObject paramJobj, Account account, boolean isVendorOrCustomer, String vendorOrCustomerId) {
        double totalOpeningAmount = 0d;
        try {
            String companyid = paramJobj.optString("companyid");
            boolean shouldAccountOpeningBalanceInclude = true;

            if (account != null && StringUtil.equal(account.getName(), "Trade Debtors")) {
                account.getName();
            }

            // calculating amount due of opening balance Customer invoices.
            HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMapJson(paramJobj);//AccountingManager.getGlobalParams(request);
            requestParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    requestParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
                requestParams.put("accountId", account.getID());
                requestParams.put("isAccountInvoices", true);
            }
            KwlReturnObject result = null;
            if (Constants.OpeningBalanceBaseAmountFlag) {
                result = accInvoiceDAOobj.getOpeningBalanceTotalBaseAmountDueForInvoices(requestParams);
                if (result.getEntityList() != null && result.getEntityList().get(0) != null) {
                    totalOpeningAmount += (Double) result.getEntityList().get(0);
                }
            } else {
                result = accInvoiceDAOobj.getOpeningBalanceInvoices(requestParams);
                if (result.getEntityList() != null) {
                    Iterator itr = result.getEntityList().iterator();
                    while (itr.hasNext()) {
                        shouldAccountOpeningBalanceInclude = false;
                        Invoice invoice = (Invoice) itr.next();

                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                        Date invoiceCreationDate = null;
                        double amountDue = 0d;
                        if (invoice.isNormalInvoice()) {
                            if (Constants.InvoiceAmountDueFlag) {
                                List ll = getInvoiceDiscountAmountInfo(requestParams, invoice);
                                amountDue = (Double) ll.get(0);
                            } else {
                                List ll = getAmountDue_Discount(requestParams, invoice);
                                amountDue = (Double) ll.get(0);
                            }
//                            invoiceCreationDate = invoice.getJournalEntry().getEntryDate();
                            externalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
                        } else {
                            amountDue = invoice.getOpeningBalanceAmountDue();
                            externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                        }
                        invoiceCreationDate = invoice.getCreationDate();

                        String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        }
                        totalOpeningAmount += (Double) bAmt.getEntityList().get(0);

                    }
                }
            }

            // Calculating Amount Due of opening balance Payment
            HashMap<String, Object> receiptParams = accReceiptController.getReceiptRequestMapJson(paramJobj);
            receiptParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    receiptParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
                receiptParams.put("accountId", account.getID());
                receiptParams.put("isAccountReceipts", true);
            }
            KwlReturnObject receiptResult = null;
            // getting opening balance receipt excluding normal Receipts
            receiptParams.put("excludeNormal", true);
            if (Constants.OpeningBalanceBaseAmountFlag) {
                receiptResult = accReceiptobj.getOpeningBalanceTotalBaseAmountDueForReceipts(receiptParams);
                if (receiptResult.getEntityList() != null && receiptResult.getEntityList().get(0) != null) {
                    totalOpeningAmount -= (Double) receiptResult.getEntityList().get(0);
                    HashMap<String, Object> writeOffParams = new HashMap();
                    writeOffParams.put("onlyOpeningWrittenOffReceipts", true);
                    writeOffParams.put("customerid", vendorOrCustomerId);
                    KwlReturnObject result1 = accReceiptobj.getReceiptWriteOffEntries(writeOffParams);
                    List<ReceiptWriteOff> R = result1.getEntityList();
                    for (ReceiptWriteOff RWO : R) {
                        totalOpeningAmount += RWO.getWrittenOffAmountInBaseCurrency();
                    }
                }
            } else {
                receiptResult = accReceiptobj.getOpeningBalanceReceipts(receiptParams);
                if (receiptResult.getEntityList() != null) {
                    Iterator itr = receiptResult.getEntityList().iterator();
                    while (itr.hasNext()) {
                        shouldAccountOpeningBalanceInclude = false;
                        Receipt receipt = (Receipt) itr.next();
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceRecceipt = receipt.isIsOpeningBalenceReceipt();
                        double amountDue = 0d;
                        Date receiptCreationDate = null;
                        if (!receipt.isNormalReceipt() && receipt.isIsOpeningBalenceReceipt()) {
                            amountDue = receipt.getOpeningBalanceAmountDue();
                            receiptCreationDate = receipt.getCreationDate();
                            externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                        }
                        String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceRecceipt && receipt.isConversionRateFromCurrencyToBase()) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                        }
                        totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);
                    }
                }
            }

            // getting opening balance receipts which are normal receipts also
            receiptParams.remove("excludeNormal");
            receiptParams.put("onlyOpeningNormalReceipts", true);

            if (account != null) {
                receiptParams.put("accountId", account.getID());
            }

            // getting opening balance due of vendor invoices for a vendor
            requestParams = accGoodsReceiptControllerCMN.getGoodsReceiptMapJson(paramJobj);
            requestParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    requestParams.put(InvoiceConstants.vendorid, vendorOrCustomerId);
                }
            } else {
                requestParams.put("accountId", account.getID());
                requestParams.put("isAccountInvoices", true);
            }

            if (Constants.OpeningBalanceBaseAmountFlag) {
                result = accGoodsReceiptobj.getOpeningBalanceTotalBaseAmountDueForInvoices(requestParams);
                if (result.getEntityList() != null && result.getEntityList().get(0) != null) {
                    totalOpeningAmount += (Double) result.getEntityList().get(0);
                }
            } else {
                result = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
                List list = result.getEntityList();
                if (list != null) {
                    Iterator it = list.iterator();
                    while (it.hasNext()) {

                        shouldAccountOpeningBalanceInclude = false;
                        List ll = null;
                        double amountdue = 0d;
                        String grId = it.next().toString();
                        KwlReturnObject grReceiptReturnObj = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(), grId);
                        GoodsReceipt goodsReceipt = (GoodsReceipt) grReceiptReturnObj.getEntityList().get(0);
                        Date invoiceCreationDate = null;
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceInvoice = goodsReceipt.isIsOpeningBalenceInvoice();

                        if (goodsReceipt.isNormalInvoice()) {// gr which are not created from opening balance pop-up
//                            invoiceCreationDate = goodsReceipt.getJournalEntry().getEntryDate();
                            externalCurrencyRate = goodsReceipt.getJournalEntry().getExternalCurrencyRate();

                            if (goodsReceipt.isIsExpenseType()) {
                                ll = accGoodsReceiptCommon.getExpGRAmountDue(requestParams, goodsReceipt);
                                amountdue = (Double) ll.get(1);
                            } else {
                                if (Constants.InvoiceAmountDueFlag) {
                                    ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(requestParams, goodsReceipt);
                                    amountdue = (Double) ll.get(1);
                                } else {
                                    ll = accGoodsReceiptCommon.getGRAmountDue(requestParams, goodsReceipt);
                                    amountdue = (Double) ll.get(1);
                                }
                            }
                        } else {
                            amountdue = goodsReceipt.getOpeningBalanceAmountDue();
                            externalCurrencyRate = goodsReceipt.getExchangeRateForOpeningTransaction();
                        }
                        invoiceCreationDate = goodsReceipt.getCreationDate();

                        String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();

                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceInvoice && goodsReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        }
                        totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                    }
                }
            }

            // getting opening balance payments excluding normal payments
           // Calculating Amount Due of opening balance Payment
            HashMap<String, Object> paymentParams = accVendorPaymentController.getPaymentMapJson(paramJobj);
            paymentParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    paymentParams.put(InvoiceConstants.vendorid, vendorOrCustomerId);
                }
            } else {
                paymentParams.put("accountId", account.getID());
                paymentParams.put("isAccountPayments", true);
            }

            paymentParams.put("excludeNormal", true);

            if (Constants.OpeningBalanceBaseAmountFlag) {
                KwlReturnObject paymentresult = accVendorPaymentobj.getOpeningBalanceTotalBaseAmountDueForPayments(paymentParams);
                if (paymentresult.getEntityList() != null && paymentresult.getEntityList().get(0) != null) {
                    totalOpeningAmount -= (Double) paymentresult.getEntityList().get(0);
                }
            } else {
                KwlReturnObject paymentresult = accVendorPaymentobj.getOpeningBalancePayments(paymentParams);
                if (paymentresult.getEntityList() != null) {
                    Iterator itr = paymentresult.getEntityList().iterator();
                    while (itr.hasNext()) {

                        shouldAccountOpeningBalanceInclude = false;
                        Payment payment = (Payment) itr.next();
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalancePayment = payment.isIsOpeningBalencePayment();
                        double amountDue = 0d;
                        Date receiptCreationDate = null;
                        if (!payment.isNormalPayment() && payment.isIsOpeningBalencePayment()) {
                            amountDue = payment.getOpeningBalanceAmountDue();
                            receiptCreationDate = payment.getCreationDate();
                            externalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                        }
                        String fromcurrencyid = payment.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalancePayment && payment.isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                        }
                        totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);

                    }
                }
            }

            // Calculating Amount Due of opening balance CNs
            HashMap<String, Object> noteParams = accCreditNoteController.getCreditNoteMapJson(paramJobj);
            noteParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    noteParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
                noteParams.put("accountId", account.getID());
                noteParams.put("isAccountCNs", true);
            }

            // getting opening balance cn excluding normal cns
            noteParams.put("excludeNormal", true);
            if (Constants.OpeningBalanceBaseAmountFlag) {
                KwlReturnObject cnResult = accCreditNoteobj.getOpeningBalanceTotalBaseAmountDueForCNs(noteParams);
                if (cnResult.getEntityList() != null && cnResult.getEntityList().get(0) != null) {
                    totalOpeningAmount -= (Double) cnResult.getEntityList().get(0);
                }
            } else {
                KwlReturnObject cnResult = accCreditNoteobj.getOpeningBalanceCNs(noteParams);
                if (cnResult.getEntityList() != null) {
                    Iterator itr = cnResult.getEntityList().iterator();
                    while (itr.hasNext()) {

                        shouldAccountOpeningBalanceInclude = false;
                        CreditNote cn = (CreditNote) itr.next();
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
                        double amountDue = 0d;
                        Date cnCreationDate = null;
                        if (!cn.isNormalCN() && cn.isIsOpeningBalenceCN()) {
                            amountDue = cn.getOpeningBalanceAmountDue();
                            cnCreationDate = cn.getCreationDate();
                            externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                        }
                        String fromcurrencyid = cn.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        }
                        totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);
                    }
                }
            }
            // Calculating Amount Due of opening balance DNs for Customers

            HashMap<String, Object> cdnParams = accDebitNoteController.gettDebitNoteMapJson(paramJobj);
            cdnParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    cdnParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
                cdnParams.put("accountId", account.getID());
                cdnParams.put("isAccountDNs", true);
            }

            // getting opening balance cn excluding normal cns
            cdnParams.put("excludeNormal", true);
            if (Constants.OpeningBalanceBaseAmountFlag) {
                KwlReturnObject cdnResult = accDebitNoteobj.getOpeningBalanceTotalBaseAmountDueForCustomerDNs(cdnParams);
                if (cdnResult.getEntityList() != null && cdnResult.getEntityList().get(0) != null) {
                    totalOpeningAmount += (Double) cdnResult.getEntityList().get(0);
                }
            } else {
                KwlReturnObject cdnResult = accDebitNoteobj.getOpeningBalanceCustomerDNs(cdnParams);
                if (cdnResult.getEntityList() != null) {
                    List<DebitNote> list=cdnResult.getEntityList();
                    for (DebitNote cn:list) {
                        shouldAccountOpeningBalanceInclude = false;
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceDN = cn.isIsOpeningBalenceDN();
                        double amountDue = 0d;
                        Date cnCreationDate = null;
                        if (!cn.isNormalDN() && cn.isIsOpeningBalenceDN()) {
                            amountDue = cn.getOpeningBalanceAmountDue();
                            cnCreationDate = cn.getCreationDate();
                            externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                        }
                        String fromcurrencyid = cn.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceDN && cn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        }
                        totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                    }
                }
            }

            // Calculating Amount Due of opening balance DNs
            HashMap<String, Object> dnParams = accDebitNoteController.gettDebitNoteMapJson(paramJobj);;
            dnParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    dnParams.put("vendorid", vendorOrCustomerId);
                }
            } else {
                dnParams.put("accountId", account.getID());
                dnParams.put("isAccountDNs", true);
            }

            // getting opening balance cn excluding normal dns
            dnParams.put("excludeNormal", true);
            if (Constants.OpeningBalanceBaseAmountFlag) {
                KwlReturnObject dnResult = accDebitNoteobj.getOpeningBalanceTotalBaseAmountDueForDNs(dnParams);
                if (dnResult.getEntityList() != null && dnResult.getEntityList().get(0) != null) {
                    totalOpeningAmount -= (Double) dnResult.getEntityList().get(0);
                }
            } else {
                KwlReturnObject dnResult = accDebitNoteobj.getOpeningBalanceDNs(dnParams);
                if (dnResult.getEntityList() != null) {
                     List<DebitNote> list=dnResult.getEntityList();
                    for (DebitNote cn:list) {
                        shouldAccountOpeningBalanceInclude = false;
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceDN = cn.isIsOpeningBalenceDN();
                        double amountDue = 0d;
                        Date cnCreationDate = null;
                        if (!cn.isNormalDN() && cn.isIsOpeningBalenceDN()) {
                            amountDue = cn.getOpeningBalanceAmountDue();
                            cnCreationDate = cn.getCreationDate();
                            externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                        }
                        String fromcurrencyid = cn.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceDN && cn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        }

                        totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);
                    }
                }
            }
            // Calculating Amount Due of opening balance CNs for Vendors

            HashMap<String, Object> vcnParams = accCreditNoteController.getCreditNoteMapJson(paramJobj);
            vcnParams.remove("ss");//search string unnessesary getting added in query and resulting as wrong amount calculation
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    vcnParams.put("vendorid", vendorOrCustomerId);
                }
            } else {
                vcnParams.put("accountId", account.getID());
                vcnParams.put("isAccountCNs", true);
            }

            // getting opening balance cn excluding normal cns
            vcnParams.put("excludeNormal", true);
            if (Constants.OpeningBalanceBaseAmountFlag) {
                KwlReturnObject vcnResult = accCreditNoteobj.getOpeningBalanceTotalBaseAmountDueForVendorCNs(vcnParams);
                if (vcnResult.getEntityList() != null && vcnResult.getEntityList().get(0) != null) {
                    totalOpeningAmount += (Double) vcnResult.getEntityList().get(0);
                }
            } else {
                KwlReturnObject vcnResult = accCreditNoteobj.getOpeningBalanceVendorCNs(vcnParams);
                if (vcnResult.getEntityList() != null) {
                    List<CreditNote> list = vcnResult.getEntityList();
                    for (CreditNote cn : list) {
                        shouldAccountOpeningBalanceInclude = false;
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
                        double amountDue = 0d;
                        Date cnCreationDate = null;
                        if (!cn.isNormalCN() && cn.isIsOpeningBalenceCN()) {
                            amountDue = cn.getOpeningBalanceAmountDue();
                            cnCreationDate = cn.getCreationDate();
                            externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                        }
                        String fromcurrencyid = cn.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                        }

                        totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                    }
                }
            }

            if (account != null && shouldAccountOpeningBalanceInclude) {
                totalOpeningAmount = account.getOpeningBalance();
            }

            String totalOpeningAmountFormatted = authHandler.formattedAmount(totalOpeningAmount, companyid);
            totalOpeningAmount = Double.parseDouble(totalOpeningAmountFormatted);

        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return totalOpeningAmount;
    }

    public boolean accountHasOpeningTransactions(HttpServletRequest request, Account account, boolean isVendorOrCustomer, String vendorOrCustomerId) {
        boolean accountHasOpeningTransactions = false;
         try{
            JSONObject paramJObj = new JSONObject();
            try{Enumeration<String> attributes = request.getAttributeNames();
            while(attributes.hasMoreElements()){
                String attribute = attributes.nextElement();            
                paramJObj.put(attribute, request.getAttribute(attribute));
            }
//            System.out.println("attributes ended");
            Enumeration<String> parameters = request.getParameterNames();
            while(parameters.hasMoreElements()){
                String parameter = parameters.nextElement();
                paramJObj.put(parameter, request.getParameter(parameter));
            }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userDateFormat = sessionHandlerImpl.getUserDateFormat(request);
            paramJObj.put(Constants.companyKey, companyid);
            paramJObj.put("userdateformat", userDateFormat);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            paramJObj.put("currencyid", currencyid);
            paramJObj.put("gcurrencyid", currencyid);
            accountHasOpeningTransactions = accountHasOpeningTransactionsJson(paramJObj, account, isVendorOrCustomer, vendorOrCustomerId);
     
    }   catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    return accountHasOpeningTransactions;         
    }
    
    public boolean accountHasOpeningTransactionsJson(JSONObject paramJobj, Account account, boolean isVendorOrCustomer, String vendorOrCustomerId) {
        boolean accountHasOpeningTransactions = false;
        int cnt = 0;
        try {
            // calculating amount due of opening balance Customer invoices.

            HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMapJson(paramJobj);//AccountingManager.getGlobalParams(request);
            requestParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    requestParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
                requestParams.put("accountId", account.getID());
                requestParams.put("isAccountInvoices", true);
            }

            cnt = accInvoiceDAOobj.getOpeningBalanceInvoiceCount(requestParams);

            if (cnt > 0) {
                return true;
            }
            // Calculating Amount Due of opening balance Payment

            HashMap<String, Object> receiptParams = accReceiptController.getReceiptRequestMapJson(paramJobj);
            receiptParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    receiptParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
                receiptParams.put("accountId", account.getID());
                receiptParams.put("isAccountReceipts", true);
            }

            // getting opening balance receipt excluding normal Receipts
            receiptParams.put("excludeNormal", true);
            cnt = accReceiptobj.getOpeningBalanceReceiptCount(receiptParams);
            if (cnt > 0) {
                return true;
            }

            // getting opening balance receipts which are normal receipts also
            receiptParams.remove("excludeNormal");
            receiptParams.put("onlyOpeningNormalReceipts", true);

            if (account != null) {
                receiptParams.put("accountId", account.getID());
            }

            // getting opening balance due of vendor invoices for a vendor
            requestParams = accGoodsReceiptControllerCMN.getGoodsReceiptMapJson(paramJobj);
            requestParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639

            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    requestParams.put(InvoiceConstants.vendorid, vendorOrCustomerId);
                }
            } else {
                requestParams.put("accountId", account.getID());
                requestParams.put("isAccountInvoices", true);
            }

            cnt = accGoodsReceiptobj.getOpeningBalanceInvoiceCount(requestParams);
            if (cnt > 0) {
                return true;
            }

            // getting opening balance payments excluding normal payments
            // Calculating Amount Due of opening balance Payment
            HashMap<String, Object> paymentParams = accVendorPaymentController.getPaymentMapJson(paramJobj);
            paymentParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    paymentParams.put(InvoiceConstants.vendorid, vendorOrCustomerId);
                }
            } else {
                paymentParams.put("accountId", account.getID());
                paymentParams.put("isAccountPayments", true);
            }

            paymentParams.put("excludeNormal", true);
            cnt = accVendorPaymentobj.getOpeningBalancePaymentCount(paymentParams);
            if (cnt > 0) {
                return true;
            }

            // Calculating Amount Due of opening balance CNs
            HashMap<String, Object> noteParams = accCreditNoteController.getCreditNoteMapJson(paramJobj);
            noteParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    noteParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
                noteParams.put("accountId", account.getID());
                noteParams.put("isAccountCNs", true);
            }

            // getting opening balance cn excluding normal cns
            noteParams.put("excludeNormal", true);
            // start date & end date shouldn't be send for opening transaction(s) on account.
            if (noteParams.containsKey(Constants.REQ_startdate)) {
                noteParams.remove(Constants.REQ_startdate); 
            }
            if (noteParams.containsKey(Constants.REQ_enddate)) {
                noteParams.remove(Constants.REQ_enddate);
            }
            cnt = accCreditNoteobj.getOpeningBalanceCNCount(noteParams);
            if (cnt > 0) {
                return true;
            }

            // Calculating Amount Due of opening balance DNs for Customers
            HashMap<String, Object> cdnParams = accDebitNoteController.gettDebitNoteMapJson(paramJobj);
            cdnParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    cdnParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
                cdnParams.put("accountId", account.getID());
                cdnParams.put("isAccountDNs", true);
            }

            // getting opening balance cn excluding normal cns
            cdnParams.put("excludeNormal", true);
            // start date & end date shouldn't be send for opening transaction(s) on account.
            if (cdnParams.containsKey(Constants.REQ_startdate)) {
                cdnParams.remove(Constants.REQ_startdate);
            }
            if (cdnParams.containsKey(Constants.REQ_enddate)) {
                cdnParams.remove(Constants.REQ_enddate);
            }
            cnt = accDebitNoteobj.getOpeningBalanceCustomerDNCount(cdnParams);
            if (cnt > 0) {
                return true;
            }

            // Calculating Amount Due of opening balance DNs
            HashMap<String, Object> dnParams = accDebitNoteController.gettDebitNoteMapJson(paramJobj);
            dnParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    dnParams.put("vendorid", vendorOrCustomerId);
                }
            } else {
                dnParams.put("accountId", account.getID());
                dnParams.put("isAccountDNs", true);
            }

            // getting opening balance cn excluding normal dns
            dnParams.put("excludeNormal", true);
            // start date & end date shouldn't be send for opening transaction(s) on account.
            if (dnParams.containsKey(Constants.REQ_startdate)) {
                dnParams.remove(Constants.REQ_startdate);
            }
            if (dnParams.containsKey(Constants.REQ_enddate)) {
                dnParams.remove(Constants.REQ_enddate);
            }
            cnt = accDebitNoteobj.getOpeningBalanceDNCount(dnParams);
            if (cnt > 0) {
                return true;
            }

            // Calculating Amount Due of opening balance CNs for Vendors
            HashMap<String, Object> vcnParams = accCreditNoteController.getCreditNoteMapJson(paramJobj);
            vcnParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    vcnParams.put("vendorid", vendorOrCustomerId);
                }
            } else {
                vcnParams.put("accountId", account.getID());
                vcnParams.put("isAccountCNs", true);
            }

            // getting opening balance cn excluding normal cns
            vcnParams.put("excludeNormal", true);
            // start date & end date shouldn't be send for opening transaction(s) on account.
            if (vcnParams.containsKey(Constants.REQ_startdate)) {
                vcnParams.remove(Constants.REQ_startdate);
            }
            if (vcnParams.containsKey(Constants.REQ_enddate)) {
                vcnParams.remove(Constants.REQ_enddate);
            }
            cnt = accCreditNoteobj.getOpeningBalanceVendorCNCount(vcnParams);
            if (cnt > 0) {
                return true;
            }
           
            if (!isVendorOrCustomer) {
                if (account != null && account.getUsedIn() != null) {
                    // If Account is mapped to Provision for the Fixed Asset (Balance Sheet)
                    if (Constants.Depreciation_Provision_GL_Account.equals(account.getUsedIn())) {
                        cnt = accProductObj.getOpeningDepreciationCountForAccount(account.getID(), paramJobj.getString(Constants.companyKey));
                        if (cnt > 0) {
                            return true;
                        }
                        /* If Account is mapped to Purchase account for the Fixed Asset Group (Balance Sheet)*/
                    } else if (account.getUsedIn().contains(Constants.Product_Sales_Return_Account) && account.getUsedIn().contains(Constants.Product_Purchase_Return_Account)) {
                        HashMap<String, Object> requestMap = new HashMap<String, Object>();
//                    Date finanDate = null;
                        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
                        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);

                        KwlReturnObject extraprefresult1 = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
                        CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) extraprefresult1.getEntityList().get(0);

//                    finanDate = accProductObj.getAssetPurchaseAccountBalanceBasedOnDate(extraCompanyPreferences, companyAccountPreferences);
                        requestMap.put("accountid", account.getID());
                        requestMap.put("companyid", paramJobj.getString(Constants.companyKey));
//                    requestMap.put("filterdate", finanDate);
                        cnt = accProductObj.getAssetPurchaseOpeningBalanceCountForAccount(requestMap);
                        if (cnt > 0) {
                            return true;
                        }
                    }
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return accountHasOpeningTransactions;
    }

    //Function to get amount of all otherwise open credit notes of perticular customer.
    public List getOpenCreditNotes_customer(HashMap<String, Object> requestParams, String customerid) throws ServiceException {
        List ll = new ArrayList();
        String currencyid = (String) requestParams.get("gcurrencyid");
        String companyid = (String) requestParams.get("companyid");
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID = baseCurrency.getCurrencyID();

        //Get amount knock off using otherwise open credit notes.
        KwlReturnObject result = accCreditNoteobj.getCNRowsOpen_customer(customerid);
        List list = result.getEntityList();
        Iterator cnitr = list.iterator();
        double cnAmountOW = 0;
        while (cnitr.hasNext()) {
            CreditNote cn = (CreditNote) cnitr.next();
//            CreditNote cn = (CreditNote) cnrow[0];
//            CreditNoteDetail cnr = (CreditNoteDetail) cnrow[1];
//            CreditNote cn = cnr.getCreditNote();
            String fromcurrencyid = (cn.getCurrency() == null ? baseCurrencyID : cn.getCurrency().getCurrencyID());
            double cnamountdue = cn.getCnamountdue();
//            Date cnCreationDate = (cn.isNormalCN()) ? cn.getJournalEntry().getEntryDate() : cn.getCreationDate();
            Date cnCreationDate = cn.getCreationDate();
            double externalCurrencyRate = (cn.isNormalCN()) ? cn.getJournalEntry().getExternalCurrencyRate() : cn.getExchangeRateForOpeningTransaction();
            boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
            if (cnamountdue > 0) {
                KwlReturnObject bAmt = null;
                if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(requestParams, cnamountdue, fromcurrencyid, currencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, cnamountdue, fromcurrencyid, currencyid, cnCreationDate, externalCurrencyRate);
                }
                cnAmountOW += (Double) bAmt.getEntityList().get(0);
            }
        }

        ll.add(cnAmountOW);
        return ll;
    }

    //Function to get amount of all otherwise open debit notes of perticular customer.
    public List getOpenDebitNotes_customer(HashMap<String, Object> requestParams, String customerid) throws ServiceException {
        List ll = new ArrayList();
        String currencyid = (String) requestParams.get("gcurrencyid");
        String companyid = (String) requestParams.get("companyid");
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID = baseCurrency.getCurrencyID();

        //Get amount knock off using otherwise open credit notes.
        KwlReturnObject result = accCreditNoteobj.getDNRowsOpen_customer(customerid);
        List list = result.getEntityList();
        Iterator cnitr = list.iterator();
        double cnAmountOW = 0;
        while (cnitr.hasNext()) {
            DebitNote cn = (DebitNote) cnitr.next();
//            CreditNote cn = (CreditNote) cnrow[0];
//            CreditNoteDetail cnr = (CreditNoteDetail) cnrow[1];
//            CreditNote cn = cnr.getCreditNote();
            String fromcurrencyid = (cn.getCurrency() == null ? baseCurrencyID : cn.getCurrency().getCurrencyID());
            double cnamountdue = cn.getDnamountdue();
            Date dnCreationDate = null;
            double externalCurrencyRate = 0d;
            boolean isopeningBalanceDN = cn.isIsOpeningBalenceDN();
            if (cn.isNormalDN()) {
//                dnCreationDate = cn.getJournalEntry().getEntryDate();
                externalCurrencyRate = cn.getJournalEntry().getExternalCurrencyRate();
            } else {// for opening balance dn
                externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
            }
            dnCreationDate = cn.getCreationDate();
            if (cnamountdue > 0) {
                KwlReturnObject bAmt = null;
                if (isopeningBalanceDN && cn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(requestParams, cnamountdue, fromcurrencyid, currencyid, dnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, cnamountdue, fromcurrencyid, currencyid, dnCreationDate, externalCurrencyRate);
                }
                cnAmountOW += (Double) bAmt.getEntityList().get(0);
            }
        }

        ll.add(cnAmountOW);
        return ll;
    }
      //Customer Quotation
     public JSONArray getCustomerQuotationDetailsItemJSON(JSONObject requestObj, String invid, HashMap<String, Object> paramMap) {
        JSONArray jArr = new JSONArray();
        JSONObject summaryData = new JSONObject();
        Quotation quotation = null;
        KwlReturnObject idresult = null;
        KwlReturnObject bAmt = null;
        java.util.Date entryDate = null;
        String allTerms = "";
        StringBuilder appendtermString = new StringBuilder();
        double subtotal = 0, totaltax = 0, totalAmount = 0;
        double totalQuantity = 0;
        double revExchangeRate = 0.0;
        double globalLevelExchangedRateTotalAmount = 0, globalLevelExchangedRateSubTotal = 0, globalLevelExchangedRateTotalTax = 0, globalLevelExchangedRateSubTotalwithDiscount = 0, globalLevelExchangedRateTermAmount = 0, subTotalWithDiscount = 0;
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            HashMap<String, Integer> ProductLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.productLevelCustomFieldMap);
            String companyid = requestObj.optString(Constants.companyKey);
            BillingPurchaseOrder po = null;
            Tax mainTax = null;
            PdfTemplateConfig config = null;
            String currencyid = "";
            String billAddr = "", shipAddr = "",createdby = "", updatedby = "", customerTitle="";
            Set<String> replacementNumber = new TreeSet<String>();
            String mainTaxName = "", globallevelcustomfields = "", globalleveldimensions = "", priceListBand = "";
            double taxPercent = 0, totalDiscount = 0;
            int quantitydigitafterdecimal = 2, amountdigitafterdecimal = 2, unitpricedigitafterdecimal = 2;
            boolean isgstincluded = false; //flag to check includedgst

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestObj);
            HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            quotation = (Quotation) kwlCommonTablesDAOObj.getClassObject(Quotation.class.getName(), invid);
            currencyid = (quotation.getCurrency() == null) ? currencyid : quotation.getCurrency().getCurrencyID();
            
            //Document Currency
            summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, currencyid);
            
            /**
             * get customer title (Mr./Mrs.)
             */
            customerTitle = quotation.getCustomer().getTitle();
            if(!StringUtil.isNullOrEmpty(customerTitle)){
                KwlReturnObject masterItemResult = kwlCommonTablesDAOObj.getObject(MasterItem.class.getName(), customerTitle);
                MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                customerTitle = masterItem.getValue();
            }
            /*
             * ExchangeRate values
             */
            double externalCurrencyRate = quotation.getExternalCurrencyRate();
            if (externalCurrencyRate != 0) {
                revExchangeRate = 1 / externalCurrencyRate;
            }
            //getting main company posttext
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, requestObj.optInt("moduleid"));
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            HashMap<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("companyid", companyid);
            addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
            addressParams.put("isBillingAddress", true);    //true to get billing address
            addressParams.put("customerid", quotation.getCustomer().getID());
            CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
            createdby = quotation.getCreatedby() != null ? quotation.getCreatedby().getFullName() : "";
            updatedby = quotation.getModifiedby() != null ? quotation.getModifiedby().getFullName() : "";

            entryDate = quotation.getQuotationDate();
            mainTax = quotation.getTax();
            billAddr = quotation.getBillTo() != null ? quotation.getBillTo() : "";

            filter_names.add("quotation.ID");
            filter_params.add(quotation.getID());
            order_by.add("srno");
            order_type.add("asc");
            invRequestParams.put("order_by", order_by);
            invRequestParams.put("order_type", order_type);
            invRequestParams.put("filter_names", filter_names);
            invRequestParams.put("filter_params", filter_params);
            idresult = accInvoiceDAOobj.getQuotationDetails(invRequestParams);
            KwlReturnObject resultcompany = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) resultcompany.getEntityList().get(0);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            /*
            * Declaration of Variables to fetch Line level taxes and its information like
            * Tax Code, Tax Amount etc.
            */ 
            String allLineLevelTax="", allLineLevelTaxAmount = "", allLineLevelTaxBasic = "", lineLevelTaxSign = "";
            Set<String> lineLevelTaxesGST = new HashSet<String>();
            Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();
            double TotalLineLevelTaxAmount = 0;
            Map<String,Double> lineLevelTaxNames = new LinkedHashMap<String,Double>();
            int countryid = 0;
            if( extraCompanyPreferences.getCompany()!= null && extraCompanyPreferences.getCompany().getCountry() != null ){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            int rowcnt = 0;
            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[1] != null) {
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                }
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
                if (decimalcontact[3] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalcontact[3].toString());
                }
            }
            if(quotation.getCustomer() != null && quotation.getCustomer().getPricingBandMaster() != null){
                // If Price List Band is selected at Customer Master
                priceListBand = quotation.getCustomer().getPricingBandMaster().getID();
            }
            Set<String> uniqueProductTaxList = new HashSet<String>();
            List<QuotationDetail> list= idresult.getEntityList();
            JSONObject detailsTableData = new JSONObject();
            JSONArray gstTaxSummaryDetailsTableDataArr = new JSONArray();
             for (QuotationDetail row:list) {//Product Grid
                JSONObject obj = new JSONObject();
                JSONObject gstTaxSummaryDetailsTableData = new JSONObject();
                Map<Integer, String> lot_size_map = new TreeMap<Integer, String>();
                String prodDesc = "", prodName = "", uom = "", rowTaxName = "", discountname = "",lot_size = "";
                double rowamountwithouttax = 0, rowamountwithtax = 0, rowTaxAmt = 0, quantity = 0, rate = 0, rowTaxPercent = 0, rowdiscountvalue = 0;
                double rowamountwithgst = 0;
                rowcnt++;

                prodName = row.getProduct().getName();
                // Get Replacement Number if linked with Lease Quotation
                if(quotation.isLeaseQuotation()){ // If Lease Quotation
                    if(row.getProductReplacementDetail() != null){ // If linked to Replacement Number
                        if(row.getProductReplacementDetail().getProductReplacement() != null){ // If linked to Replacement Number
                            replacementNumber.add(row.getProductReplacementDetail().getProductReplacement().getReplacementRequestNumber());
                        }
                    }
                }
                
                if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                    prodDesc = row.getDescription();
                } else {
                    if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                        prodDesc = row.getProduct().getDescription();
                    }
                }
                prodDesc = StringUtil.DecodeText(prodDesc);
                uom = row.getUom() == null ? (row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA()) : row.getUom().getNameEmptyforNA(); //ERP-25533
                // Lot Size
                if (!StringUtil.isNullOrEmpty(priceListBand)) {
                    KwlReturnObject volmapped = accMasterItemsDAOobj.getPricingVolumeDiscountMapped(priceListBand);
                    if (volmapped != null && volmapped.getEntityList() != null && !volmapped.getEntityList().isEmpty()) {
                        List<PricingBandmappingWithVolumeDisc> volMappedList = volmapped.getEntityList();
                        for(PricingBandmappingWithVolumeDisc volumeDiscountOrFlat : volMappedList){
                            if(volumeDiscountOrFlat.getVolumediscountid() != null && volumeDiscountOrFlat.getVolumediscountid().getPricePolicyValue() == Constants.PRICE_LIST_DISCOUNT_AT_FIXED_RATE){
                                HashMap<String, Object> filterRequestParams = new HashMap<>();
                                ArrayList price_band_filter_names = new ArrayList(), price_band_filter_params = new ArrayList();
                                price_band_filter_names.add("pricingBandMaster.ID");
                                price_band_filter_params.add(volumeDiscountOrFlat.getVolumediscountid().getID());
                                if (row.getProduct() != null) {
                                    price_band_filter_names.add("product");
                                    price_band_filter_params.add(row.getProduct().getID());
                                }
                                filterRequestParams.put("filter_names", price_band_filter_names);
                                filterRequestParams.put("filter_params", price_band_filter_params);
                                KwlReturnObject detailObj = accMasterItemsDAOobj.getPricingBandMasterDetailsList(filterRequestParams);
                                List<PricingBandMasterDetail> detailList = detailObj.getEntityList();

                                for (PricingBandMasterDetail bandMasterDetails : detailList) {
                                    String currencycode = bandMasterDetails.getCurrency()!= null ?bandMasterDetails.getCurrency().getCurrencyCode():"";
                                    String lot_size_str = bandMasterDetails.getMaximumQty() + " " + uom + " - "+ currencycode +" "+ CustomDesignHandler.getAmountinCommaDecimal(bandMasterDetails.getSalesPrice(), Constants.AMOUNT_DIGIT_AFTER_DECIMAL, countryid) + " /" + uom;
                                    lot_size_map.put(bandMasterDetails.getMaximumQty(),lot_size_str);
                                }
                            }
                        }
                    }
                    
                }
                Iterator itr = lot_size_map.keySet().iterator();
                while(itr.hasNext()){
                    lot_size +=  lot_size_map.get((Integer)itr.next())+"</br>";
                }
                if(!StringUtil.isNullOrEmpty(lot_size)){
                    lot_size = lot_size.substring(0, lot_size.length()-5);
                }
                obj.put(CustomDesignerConstants.LOT_SIZE, lot_size);  //Lot Size
                
                quantity = row.getQuantity();
                if (row.getQuotation().isGstIncluded()) {//if gstincluded is the case.
                    isgstincluded = true;
                }
                
                rate = row.getRate();
                
                rowamountwithouttax = rate * quantity;

                /*
                 * In include GST case calculations are in reverse order
                 * In other cases calculations are in forward order
                 */
                if (row.getQuotation().isGstIncluded()) {//if gstincluded is the case
                    rowamountwithgst = row.getRateincludegst() * quantity;
                    rowdiscountvalue = (row.getDiscountispercent() == 1)? rowamountwithgst * row.getDiscount()/100 : row.getDiscount();
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount((rowamountwithgst - row.getRowTaxAmount()), companyid));//Subtotal
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount,authHandler.formattingDecimalForAmount((rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount()), companyid));//Subtotal
                    subtotal += authHandler.round(rowamountwithgst - row.getRowTaxAmount(), companyid);
                    subTotalWithDiscount = authHandler.round((rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount()), companyid);
                } else{
                    rowdiscountvalue = (row.getDiscountispercent() == 1)? rowamountwithouttax*row.getDiscount()/100 : row.getDiscount();
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithouttax, companyid));//Subtotal
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount,authHandler.formattingDecimalForAmount((rowamountwithouttax-rowdiscountvalue), companyid));//Subtotal
                    subtotal += authHandler.round(rowamountwithouttax, companyid);//rounded becuase totaltax & amount are rounded and saved in db
                    subTotalWithDiscount = authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid);
                }
                bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                totalDiscount +=authHandler.round(rowdiscountvalue, companyid);
                 if (row.getDiscountispercent() == 1) {
                     double discountpercent = row.getDiscount();
                     discountname = (long) discountpercent == discountpercent ? (long) discountpercent + "%" : discountpercent + "%"; 
                 } else {
                     discountname = quotation.getCurrency().getSymbol() + " " + authHandler.formattingDecimalForAmount(row.getDiscount(), companyid);//to show as it is in UI
                 }

                obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                obj.put(CustomDesignerConstants.Discountname, discountname);// Discount

                KwlReturnObject productCategories = null;
                productCategories = accProductObj.getProductCategoryForDetailsReport(row.getProduct().getID());
                List productCategoryList = productCategories.getEntityList();
                String cateogry = "";
                Iterator catIte = productCategoryList.iterator();
                while (catIte.hasNext()) {
                    ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                    String categoryName = pcm.getProductCategory() != null ? pcm.getProductCategory().getValue().toString() : "";
                    cateogry += categoryName + " ";
                }
                if (StringUtil.isNullOrEmpty(cateogry)) {
                    cateogry = "None";
                }
                obj.put("productCategory", cateogry);
                obj.put("productType", row.getProduct().getProducttype().getName());
                HashMap<String, Object> params = new HashMap<String, Object>();
                filter_names = new ArrayList();
                filter_params = new ArrayList();
                filter_names.add("companyid");
                filter_names.add("fieldtype");
                filter_names.add("customcolumn");
                filter_names.add("moduleid");
                filter_params.add(companyid);
                filter_params.add(4);
                filter_params.add(1);
                filter_params.add(requestObj.optInt("moduleid"));
                params.put("filter_names", filter_names);
                params.put("filter_values", filter_params);
                
                KwlReturnObject fieldparams =  accAccountDAOobj.getFieldParams(params);
                List fieldParamsList = fieldparams.getEntityList();
                
                JSONArray jsonarr = new JSONArray();
                for(int cnt=0; cnt < fieldParamsList.size(); cnt++){
                    FieldParams fieldParamsObj = (FieldParams) fieldParamsList.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("comboName", fieldParamsObj.getFieldname());
                    jsonarr.put(tempObj);
                }
                
                obj.put("groupingComboList", jsonarr);
                
                //Product level Tax
                if (row != null && row.getTax() != null) {
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", row.getTax().getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    rowTaxName = row.getTax().getName();
                    rowTaxAmt = row.getRowTaxAmount();
                    uniqueProductTaxList.add(row.getTax().getID());
                }
                totaltax +=authHandler.round(rowTaxAmt,companyid);//Calculate tax amount from line item

                if (row.getQuotation().isGstIncluded()) {
                    rowamountwithtax = rowamountwithouttax; //Amount will be equal to rowamountwithouttax because tax gets added in gst
                } else {
                    if (rowdiscountvalue != 0) {
                        rowamountwithouttax -= rowdiscountvalue;//deducting discount if any
                    }
                    rowamountwithtax = rowamountwithouttax + rowTaxAmt;
                }
                
                String lineLevelTax = "";
                String lineLevelTaxPercent = "";
                String lineLevelTaxAmount = "";
                double lineLevelTaxAmountTotal = 0;
                /*
                 * Check for 
                 */
                if(extraCompanyPreferences.isIsNewGST()){ // for New gst check 
                    HashMap<String, Object> CustQuotationDetailParams = new HashMap<String, Object>();
                    CustQuotationDetailParams.put("quotationDetailId", row.getID());
                    CustQuotationDetailParams.put("orderbyadditionaltax", true);
                    // GST
                    CustQuotationDetailParams.put("termtype", 7);
                    KwlReturnObject grdTermMapresult = accSalesOrderDAOobj.getQuotationDetailTermMap(CustQuotationDetailParams);
                    List<QuotationDetailTermMap> gst = grdTermMapresult.getEntityList();
                    obj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.CESSPERCENT, 0);
                    obj.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.TAXABLE_VALUE, subTotalWithDiscount);
                    
                    for (QuotationDetailTermMap custquotationdetailTermMap : gst) {
                        LineLevelTerms mt = custquotationdetailTermMap.getTerm();
                        if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                            obj.put(CustomDesignerConstants.CGSTPERCENT, custquotationdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CGSTAMOUNT, custquotationdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, custquotationdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, custquotationdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                            obj.put(CustomDesignerConstants.IGSTPERCENT, custquotationdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.IGSTAMOUNT, custquotationdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, custquotationdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, custquotationdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                            obj.put(CustomDesignerConstants.SGSTPERCENT, custquotationdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.SGSTAMOUNT, custquotationdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, custquotationdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, custquotationdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                            obj.put(CustomDesignerConstants.UTGSTPERCENT, custquotationdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.UTGSTAMOUNT, custquotationdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, custquotationdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, custquotationdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                            obj.put(CustomDesignerConstants.CESSPERCENT, custquotationdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CESSAMOUNT, custquotationdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, custquotationdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, custquotationdetailTermMap.getTermamount());
                        }
                        lineLevelTax += mt.getTerm();
                        lineLevelTax += "!## ";
                        lineLevelTaxPercent += authHandler.formattingDecimalForAmount(custquotationdetailTermMap.getPercentage(), companyid);
                        lineLevelTaxPercent += "!## ";
                        lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(custquotationdetailTermMap.getTermamount(), amountdigitafterdecimal,countryid);
                        lineLevelTaxAmount += "!## ";
                        /*
                         * calculating total of line level taxes
                         */
                        lineLevelTaxAmountTotal += custquotationdetailTermMap.getTermamount();
                        if (lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm()) != null) {
                            double value = lineLevelTaxNames.get(mt.getTerm());
                            lineLevelTaxNames.put(mt.getTerm(), custquotationdetailTermMap.getTermamount() + value);
                        } else {
                            lineLevelTaxNames.put(mt.getTerm(), custquotationdetailTermMap.getTermamount());
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTax)) {
                        lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxPercent)) {
                        lineLevelTaxPercent = lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxAmount)) {
                        lineLevelTaxAmount = lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length() - 4);
                    }
                    obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                    obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                    obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                    /*
                     * putting subtotal+tax
                     */
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round(lineLevelTaxAmountTotal, companyid));       
                    //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                    ExportRecordHandler.setHsnSacProductDimensionField(row.getProduct(), obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.HSN_SAC_CODE, obj.optString(CustomDesignerConstants.HSN_SAC_CODE, ""));
                    gstTaxSummaryDetailsTableDataArr.put(gstTaxSummaryDetailsTableData);
                } else {
                    /*
                    * Fetching distinct taxes used at line level, feetched in the set
                    * Also, fetched the information related to tax in different maps
                    */
                    boolean isRowTaxApplicable = false;
                    double rowTaxPercentGST = 0.0;
                    if (row.getTax() != null) {
                        String taxCode = row.getTax().getTaxCode();
                        if (!lineLevelTaxesGST.contains(taxCode)) {
                            lineLevelTaxesGST.add(taxCode);
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, quotation.getQuotationDate(), row.getTax().getID());
                            rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                            lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                            lineLevelTaxAmountGST.put(taxCode, 0.0);
                            lineLevelTaxBasicGST.put(taxCode, 0.0);
                        }
                        lineLevelTaxAmountGST.put(taxCode,(Double) lineLevelTaxAmountGST.get(taxCode) + row.getRowTaxAmount());
                        lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + obj.optDouble(CustomDesignerConstants.SUBTOTAL,0.0));
                        /*
                         * putting subtotal+tax
                         */
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round(row.getRowTaxAmount(), companyid)); 
                    }
                }
                
                obj.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, 1);
                obj.put("gstCurrencyRate", "");
                summaryData.put("gstCurrencyRate", "");
                Date transactionDate = quotation.getQuotationDate();
                // transactionDate = quotation.getJournalEntry().getEntryDate();
                //Date transactionDate=new SimpleDateFormat("dd/MM/yyyy").parse(invoice.getJournalEntry().getEntryDate());
                 obj.put("transactiondate", transactionDate); // Amount
                 summaryData.put("transactiondate", transactionDate); // Amount
                   
                double exchangerateunitprice = 0, exchangeratelineitemsubtotal = 0, exchangeratelineitemdiscount = 0, exchangeratelineitemamount = 0, exchangeratelineitemtax = 0, exchangeratelineitemsubtotalwithdiscount = 0;               
                /*<--------------------Base Currency Unit Price,Base currency Subtotal ----------->*/
                /*Exchange Rate Section---- We have not given amount with tax because it is matched with UI*/
                if (externalCurrencyRate != 0) {
                    //Unit Price   
                    exchangerateunitprice = authHandler.round((rate * revExchangeRate), companyid);  //exchanged rate unit rate
                    //SUbTotal (rate*quantity)
                    exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                    //Exhanged Rate Discount
                    exchangeratelineitemdiscount = authHandler.round((rowdiscountvalue * revExchangeRate), companyid);//exchange rate total discount
                    if (row.getQuotation().isGstIncluded()) {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                    } else {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                    }
                    
                    //Exhanged Rate Tax Amount
                    exchangeratelineitemtax = extraCompanyPreferences.isIsNewGST() ? authHandler.round((lineLevelTaxAmountTotal * revExchangeRate), companyid) : authHandler.round((rowTaxAmt * revExchangeRate), companyid);
                    
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);
                    
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);

                    //Total Exchanged Rate (RATE*Quantity)
                    globalLevelExchangedRateSubTotal = globalLevelExchangedRateSubTotal + exchangeratelineitemsubtotal;
                    //Overall Subtotal with Discount
                    globalLevelExchangedRateSubTotalwithDiscount = globalLevelExchangedRateSubTotalwithDiscount + exchangeratelineitemsubtotalwithdiscount;
                    //Overall Total Tax
                    globalLevelExchangedRateTotalTax = globalLevelExchangedRateTotalTax + exchangeratelineitemtax;

                } else {
                    //Unit Price   
                    exchangerateunitprice = authHandler.round(rate, companyid);  //exchanged rate unit rate
                    //SUbTotal (rate*quantity)
                    exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                    //Exhanged Rate Discount
                    exchangeratelineitemdiscount = authHandler.round(rowdiscountvalue, companyid);//exchange rate total discount
                    if (row.getQuotation().isGstIncluded()) {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                    } else {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                    }
                    
                    //Exhanged Rate Tax Amount
                    exchangeratelineitemtax = extraCompanyPreferences.isIsNewGST() ? authHandler.round(lineLevelTaxAmountTotal, companyid) : authHandler.round(rowTaxAmt, companyid);
                    
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);

                    //Total Exchanged Rate (RATE*Quantity)
                    globalLevelExchangedRateSubTotal = globalLevelExchangedRateSubTotal + exchangeratelineitemsubtotal;
                    //Overall Subtotal with Discount
                    globalLevelExchangedRateSubTotalwithDiscount = globalLevelExchangedRateSubTotalwithDiscount + exchangeratelineitemsubtotalwithdiscount;
                    //Overall Total Tax
                    globalLevelExchangedRateTotalTax = globalLevelExchangedRateTotalTax + exchangeratelineitemtax;
                }
                
                totalQuantity += quantity;
                obj.put(CustomDesignerConstants.SrNO, rowcnt);  //Sr No
                obj.put(CustomDesignerConstants.ProductName, prodName.replaceAll("\n", "<br>"));  //product name
                obj.put(CustomDesignerConstants.ProductDescription, prodDesc.replaceAll("\n", "<br>"));  //product description
                obj.put(CustomDesignerConstants.HSCode, (row.getProduct().getHSCode() != null) ? row.getProduct().getHSCode().replaceAll("\n", "<br>") : "");//get HSCode of ProductGrid
                obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate
                obj.put(CustomDesignerConstants.IN_ProductCode, row.getProduct().getProductid() == null ? "" : row.getProduct().getProductid());
                obj.put(CustomDesignerConstants.QuantitywithUOM, authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom); // Quantity
                obj.put(CustomDesignerConstants.IN_Quantity, authHandler.formattingDecimalForQuantity(quantity, companyid));
                obj.put(CustomDesignerConstants.IN_UOM, uom);
                obj.put(CustomDesignerConstants.PRODUCTNETWEIGHT, authHandler.formattingDecimalForAmount(row.getProduct().getProductweight(), companyid));//Product Weight
                obj.put(CustomDesignerConstants.Amount, authHandler.formattingDecimalForAmount(rowamountwithtax, companyid)); // Amount of single product
                //obj.put(CustomDesignerConstants.IN_Discount, row.getDiscount());// Discount
                obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmt);   //Tax Amount
                obj.put(CustomDesignerConstants.IN_ProductTax, rowTaxName);//Tax Name
                obj.put(CustomDesignerConstants.IN_Currency, quotation.getCurrency().getCurrencyCode());//Currency code
                obj.put(CustomDesignerConstants.AdditionalDescription, row.getProduct().getAdditionalDesc() != null ? row.getProduct().getAdditionalDesc().replaceAll("\n", "<br>") :"");  //product Addtional description
                obj.put("currencysymbol", quotation.getCurrency().getSymbol());
                obj.put("isGstIncluded", isgstincluded);
                obj.put("currencycode", quotation.getCurrency().getCurrencyCode());//used for headercurrencyCode & record currency
                String baseUrl = URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                /*
                 * Following code is to check whether the image is predent for product or not. 
                 * If Image is not present sent s.gif instead of product id
                 */
                String fileName = null;
                fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+row.getProduct().getID() + ".png";
                File file = new File(fileName);
                FileInputStream in = null; 
                String filePathString = "";
                try {
                    in = new FileInputStream(file);
                } catch(java.io.FileNotFoundException ex) {
                    //catched exception if file not found, and to continue for rest of the products
                   filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                if(in != null){
                    filePathString = baseUrl + "productimage?fname=" + row.getProduct().getID() + ".png&isDocumentDesignerPrint=true";
                } else{
                    filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                obj.put(CustomDesignerConstants.imageTag, filePathString);

                //Amol D. Change: Get custom line data and add Custom fields in pdf 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                QuotationDetailCustomData qDetailCustom = (QuotationDetailCustomData) row.getQuotationDetailCustomData();
                replaceFieldMap = new HashMap<String, String>();
                if (qDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, qDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                
                
                
                QuotationDetailsProductCustomData qProcuctDetailCustom = (QuotationDetailsProductCustomData) row.getQuotationDetailProductCustomData();
                if(qProcuctDetailCustom != null){
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, qProcuctDetailCustom, ProductLevelCustomFieldMap, obj, kwlCommonTablesDAOObj,variableMap);
                }
                    
                /*
                 * Set All Line level Dimension & All LIne level Custom Field Values
                 */
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields
                JSONObject jObj = new JSONObject();
                if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                    jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                }
                if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                    obj = accProductObj.getProductDisplayUOM(row.getProduct(), quantity,row.getBaseuomrate(), false, obj);
                }
                jArr.put(obj);
            }
           detailsTableData.put("gsttaxsummary", gstTaxSummaryDetailsTableDataArr);
           detailsTableData.put("isDetailsTableData", true);
           jArr.put(detailsTableData);
            //Overall GST Tax
           if (!StringUtil.isNullOrEmpty(invid)) {
                HashMap<String, Object> reqParam = new HashMap();
                reqParam.put("quotation", invid);
                KwlReturnObject curresult = null;
                double termTaxAmnt = 0;
                /*
                 * Terms calculation
                 */
                curresult = accSalesOrderDAOobj.getQuotationTermMap(reqParam);
                List<QuotationTermMap> termMap = curresult.getEntityList();
                for (QuotationTermMap quotationTermMap : termMap) {
                    termTaxAmnt += quotationTermMap.getTermtaxamount();
                }
                totaltax += termTaxAmnt;
            }
            mainTax = quotation.getTax();
            if (mainTax != null) { //Get tax percent
                requestParams.put("transactiondate", entryDate);
                requestParams.put("taxid", mainTax.getID());
                KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                List taxList = result1.getEntityList();
                Object[] taxObj = (Object[]) taxList.get(0);
                taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                mainTaxName = mainTax.getName();
                totalAmount = subtotal - totalDiscount;
                totaltax += (taxPercent == 0 ? 0 : totalAmount * taxPercent / 100);//overall tax calculate
                if (externalCurrencyRate != 0) {
                    globalLevelExchangedRateTotalTax = authHandler.round((totaltax * revExchangeRate), companyid);//exchanged rate total tax amount 
                } else {
                    globalLevelExchangedRateTotalTax = authHandler.round((totaltax), companyid);//exchanged rate total tax amount 
                }
            }
            totalAmount = subtotal + totaltax - totalDiscount;

            /*Invoice Terms Calculation*/
//            JSONObject summaryData = new JSONObject();
            String term = "",termsName = "";
            String termids = "";
            if(mainTax != null){
                List l = accCusVenMapDAOObj.getTerms(mainTax.getID()); // get terms mapped with applied tax
                Iterator itr = l.iterator();
                while (itr.hasNext()) {
                    InvoiceTermsSales invoiceTermsSales = (InvoiceTermsSales) kwlCommonTablesDAOObj.getClassObject(InvoiceTermsSales.class.getName(), itr.next().toString());
                    if (invoiceTermsSales != null) {
                        termids += invoiceTermsSales.getId() + ",";
                    }
                }
            }
            
            
            double lineleveltermTaxAmount = 0;
            Map<String, Object> taxListParams = new HashMap<String, Object>();
            taxListParams.put("companyid", companyid);
            boolean isApplyTaxToTerms=quotation.isApplyTaxToTerms();
            
            HashMap<String, Object> filterrequestParams = new HashMap();
            filterrequestParams.put("taxid", quotation.getTax()==null?"":quotation.getTax().getID());
            
            double totalTermAmount = 0;
            boolean isTaxTermMapped = false;
            double termAmountBeforeTax = 0;
            double termAmountAfterTax = 0;
            String termsNameWithoutPercent= "",termsamount = "",termssign="";
            if (!StringUtil.isNullOrEmpty(quotation.getID())) {
                HashMap<String, Object> requestParam = new HashMap();
                requestParam.put("quotation", quotation.getID());
                KwlReturnObject curresult = null;
                curresult = accSalesOrderDAOobj.getQuotationTermMap(requestParam);
                List<QuotationTermMap> termMap = curresult.getEntityList();
                for (QuotationTermMap quotationTermMap : termMap) {
                    double termAmnt = 0;
                    if(quotation.isGstIncluded()){
                        termAmnt = quotationTermMap.getTermAmountExcludingTax();
                    }else{
                        termAmnt = quotationTermMap.getTermamount();
                    }
                    
                    InvoiceTermsSales mt = quotationTermMap.getTerm();
                    
                    filterrequestParams.put("term", quotationTermMap.getTerm()==null?"":quotationTermMap.getTerm().getId());
                    filterrequestParams.put("companyid", companyid);

//                    if (uniqueProductTaxList.size() > 0 && isApplyTaxToTerms) {
//                        for(String taxId : uniqueProductTaxList){
//                            filterrequestParams.put("taxid",taxId);
//                            taxListParams.put("taxid",taxId);
//                            isTaxTermMapped = accTaxObj.isTermMappedwithTax(filterrequestParams);
//                            if(isTaxTermMapped){
//                                KwlReturnObject taxListResult = accTaxObj.getTaxList(taxListParams);
//                                if (taxListResult != null && taxListResult.getEntityList() != null) {
//                                    List<TaxList> taxListPercent = taxListResult.getEntityList();
//                                    lineleveltermTaxAmount += (termAmnt * (taxListPercent.get(0).getPercent()))/100;
//                                }
//    //                                    break;
//                            }
//                        }
//                    } else {
//                        isTaxTermMapped = accTaxObj.isTermMappedwithTax(filterrequestParams);
//                    }
                    
                    termsName += mt.getTerm() + " " + quotationTermMap.getPercentage() + "%, ";
//                    if (isTaxTermMapped) { // If term is mapped with tax
//                        termAmountBeforeTax += quotationTermMap.getTermamount(); // term amount for adding before tax calculation
//                    } else {
//                        termAmountAfterTax += quotationTermMap.getTermamount(); // term amount for adding after tax calculation
//                    }
                    
                    String termName = (mt.getTerm() + (termAmnt > 0 ? "(+)" : "(-)"));
                    summaryData.put(mt.getTerm(), termAmnt);
                    summaryData.put(CustomDesignerConstants.BaseCurrency + mt.getTerm(), (termAmnt * revExchangeRate));//Base currency exchange rate term value-ERP-13451
                    totalTermAmount += termAmnt;
                    double exchangeratetermamount = 0;
                    if (externalCurrencyRate != 0) {
                        exchangeratetermamount = (termAmnt * revExchangeRate);
                    } else {
                        exchangeratetermamount = termAmnt;
                    }
                    globalLevelExchangedRateTermAmount += exchangeratetermamount;
                    double tempTermValue = (termAmnt > 0 ? termAmnt : (termAmnt * -1));
                    term += "<div> <table><tr><td>" + termName + " : </td><td>" + Double.toString(tempTermValue) + "</td></tr></table></div><br>";
                    termsNameWithoutPercent += mt.getTerm() + "!## ";
                    if (termAmnt > 0) {
                        termssign += "+!## ";
                    } else {
                        termssign += "-&nbsp;!## ";
                    }
                    termsamount += CustomDesignHandler.getAmountinCommaDecimal(Math.abs(termAmnt), amountdigitafterdecimal,countryid) + "!## ";
                    if (!StringUtil.isNullOrEmpty(String.valueOf(termAmnt))) {
                        String allTermsPlaceholder = CustomDesignerConstants.AllTermsKeyValuePair;
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsLabel, termName);
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsValue, CustomDesignHandler.getAmountinCommaDecimal(termAmnt, amountdigitafterdecimal, countryid));
                        appendtermString.append(allTermsPlaceholder);
                    }
                }
                if (!StringUtil.isNullOrEmpty(termsName)) {
                    termsName = termsName.substring(0, termsName.length() - 2);
                }
                if (!StringUtil.isNullOrEmpty(term) && term.indexOf("<br>") != -1) {
                    term = term.substring(0, term.lastIndexOf("<br>"));
                }
                if(!StringUtil.isNullOrEmpty(appendtermString.toString())){
                    allTerms=appendtermString.toString();
                }
                if (!StringUtil.isNullOrEmpty(termsNameWithoutPercent)) {
                    termsNameWithoutPercent = termsNameWithoutPercent.substring(0, termsNameWithoutPercent.length() - 4);
            }
                if (!StringUtil.isNullOrEmpty(termssign)) {
                    termssign = termssign.substring(0, termssign.length() - 4);
                }
                if (!StringUtil.isNullOrEmpty(termsamount)) {
                    termsamount = termsamount.substring(0, termsamount.length() - 4);
                }
            }
            
//            if(isTaxTermMapped){ // If tax mapped with any term
//                if (mainTax != null) {
//                    totaltax = (taxPercent == 0 ? 0 : ((subtotal - totalDiscount) + termAmountBeforeTax) * taxPercent /100); // first add term into subtotal then calculate tax
//                }
//                totaltax += lineleveltermTaxAmount;
//                totalAmount = subtotal - totalDiscount + termAmountBeforeTax + totaltax + termAmountAfterTax; // first add mapped terms amount in subtotal then add total tax then add unmapped terms amount for total amount
//            } else{
            totalAmount = totalAmount + totalTermAmount;
//            }
            globalLevelExchangedRateTermAmount = authHandler.round(globalLevelExchangedRateTermAmount, companyid);
            /* Base Currency Total Amount*/
            globalLevelExchangedRateTotalAmount = globalLevelExchangedRateSubTotalwithDiscount + globalLevelExchangedRateTotalTax + globalLevelExchangedRateTermAmount;
            
            summaryData.put("summarydata", true);
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("customerid", quotation.getCustomer().getID());
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
            billAddr = CommonFunctions.getTotalBillingShippingAddress(quotation.getBillingShippingAddresses(), true);
            shipAddr = CommonFunctions.getTotalBillingShippingAddress(quotation.getBillingShippingAddresses(), false);
            /*
             * 
             */
            boolean isLeaseFixedAsset = requestObj.optBoolean("isLeaseFixedAsset", false);
            int moduleid = Constants.Acc_Customer_Quotation_ModuleId;
            if(isLeaseFixedAsset){
                moduleid = Constants.Acc_Lease_Quotation;
            }
            /*All Global Section Custom Field and DImensions*/
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            DateFormat df = authHandler.getUserDateFormatterJson(requestObj);//User Date Formatter
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, moduleid);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", quotation.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            /*
             * Check for 
             */
            if(extraCompanyPreferences.isIsNewGST()){ // for New gst check 
                    if (!lineLevelTaxNames.isEmpty()) {
                        Iterator lineTax = lineLevelTaxNames.entrySet().iterator();
                        while (lineTax.hasNext()) {
                            Map.Entry tax = (Map.Entry) lineTax.next();
                            allLineLevelTax += tax.getKey();
                            allLineLevelTax += "!## ";
                            double taxamount = (double) tax.getValue();
                            allLineLevelTaxAmount += tax.getValue().toString();
                            TotalLineLevelTaxAmount += taxamount;
                            allLineLevelTaxAmount += "!## ";
                            if (taxamount > 0) {
                                lineLevelTaxSign += "+";
                                lineLevelTaxSign += "!## ";
                            } else {
                                lineLevelTaxSign += "-&nbsp;";
                                lineLevelTaxSign += "!## ";
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(allLineLevelTax)) {
                        allLineLevelTax = allLineLevelTax.substring(0, allLineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(allLineLevelTaxAmount)) {
                        allLineLevelTaxAmount = allLineLevelTaxAmount.substring(0, allLineLevelTaxAmount.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxSign)) {
                        lineLevelTaxSign = lineLevelTaxSign.substring(0, lineLevelTaxSign.length() - 4);
                    }
                
                totalAmount = totalAmount + TotalLineLevelTaxAmount;
                totaltax = TotalLineLevelTaxAmount;
            } else {
                /*
                * Putting all line taxes and its information in summary data separated by !##
                */
                for ( String key : lineLevelTaxesGST ) {
                    allLineLevelTax += key + "!##";
                    allLineLevelTaxAmount +=  lineLevelTaxAmountGST.get(key).toString() + "!##";
                    allLineLevelTaxBasic += lineLevelTaxBasicGST.get(key).toString() + "!##";
                }
            }
            /**
             * SDP-13772
             * Add Rounding Adjustment in Total Amount
             */
            if(quotation.isIsRoundingAdjustmentApplied()){
                totalAmount = totalAmount + (quotation.getRoundingadjustmentamount());
            }
            if(countryid == Constants.indian_country_id) { // For India Country 
                String buyerexcRegNo="", buyetinNo="", buyerpanNo="", buyerRange="", buyerDivision="", buyerComrate="", buyerServiceTaxRegNo="";
                
                // Customer Related indian details
                String panStatus = "", IECNo = "", CSTDateStr = "", VATDateStr = "", dealerTypeStr = "";
                String ImporterECCNo = "",  typeOfSales = "";

                if (quotation.getCustomer().getPanStatus() != null && !StringUtil.isNullOrEmpty(quotation.getCustomer().getPanStatus())) {
                    panStatus = quotation.getCustomer().getPanStatus().equals(IndiaComplianceConstants.PAN_STATUS_PANNOTAVBL) ? IndiaComplianceConstants.PAN_NOT_AVAILABLE : quotation.getCustomer().getPanStatus().equals(IndiaComplianceConstants.PAN_STATUS_APPLIEDFOR) ? IndiaComplianceConstants.PAN_APPLIED_FOR : "";
                }
                if(quotation.getCustomer().getDefaultnatureOfPurchase()!= null && !StringUtil.isNullOrEmpty(quotation.getCustomer().getDefaultnatureOfPurchase())){
                    KwlReturnObject naturetyperesult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), quotation.getCustomer().getDefaultnatureOfPurchase());
                    MasterItem natureTypeObj = (MasterItem) naturetyperesult.getEntityList().get(0);
                    if(natureTypeObj != null){
                        typeOfSales = natureTypeObj.getValue();
                    }
                }

                if (quotation.getCustomer().getDealertype() != null && !StringUtil.isNullOrEmpty(quotation.getCustomer().getDealertype())) {
                    String dealerType = quotation.getCustomer().getDealertype();
                    if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_REGISTERED)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_REGISTERED_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_UNREGISTERED)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_UNREGISTERED_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_1_2)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_1_2_STR;
                    }
                }
                if (quotation.getCustomer().getCSTRegDate() != null) {
                    Date CSTDate = quotation.getCustomer().getCSTRegDate();
                    CSTDateStr = CSTDate != null ? authHandler.getUserDateFormatterJson(requestObj).format(CSTDate) : "";
                }
                if (quotation.getCustomer().getVatregdate() != null) {
                    Date VATDate = quotation.getCustomer().getVatregdate();
                    VATDateStr = VATDate != null ? authHandler.getUserDateFormatterJson(requestObj).format(VATDate) : "";
                }

                buyerComrate = quotation.getCustomer().getCommissionerate() != null ? quotation.getCustomer().getCommissionerate() : "";
                buyerDivision = quotation.getCustomer().getDivision() != null ? quotation.getCustomer().getDivision() : "";
                buyerRange = quotation.getCustomer().getRangecode() != null ? quotation.getCustomer().getRangecode() : "";
                buyetinNo = quotation.getCustomer().getVATTINnumber() != null ? quotation.getCustomer().getVATTINnumber() : "";
                buyerexcRegNo = quotation.getCustomer().getECCnumber() != null ? quotation.getCustomer().getECCnumber() : "";
                buyerpanNo = quotation.getCustomer().getPANnumber() != null ? quotation.getCustomer().getPANnumber() : "";
                buyerServiceTaxRegNo = quotation.getCustomer().getSERVICEnumber() != null ? quotation.getCustomer().getSERVICEnumber() : "";
                ImporterECCNo = (quotation.getCustomer() != null && quotation.getCustomer().getImporterECCNo() != null) ? quotation.getCustomer().getImporterECCNo() : "";
                IECNo = (quotation.getCustomer() != null && quotation.getCustomer().getIECNo() != null) ? quotation.getCustomer().getIECNo() : "";

                // ************************   Customer Related Information **********************************************
                summaryData.put(CustomDesignerConstants.CUSTOMER_PAN_NO, buyerpanNo);
                summaryData.put(CustomDesignerConstants.CustomerVendor_PAN_STATAUS, panStatus);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, buyetinNo);
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, quotation.getCustomer().getCSTTINnumber() != null ? quotation.getCustomer().getCSTTINnumber() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_REG_DATE, CSTDateStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_REG_DATE, VATDateStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_DEALER_TYPE, dealerTypeStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_INTERSTATEPARTY, quotation.getCustomer().isInterstateparty() ? "Yes" : "No");
                summaryData.put(CustomDesignerConstants.CustomerVendor_C_FORM_APPLICABLE, quotation.getCustomer().isCformapplicable() ? "Yes" : "No");
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_TYPE_OF_SALES, typeOfSales);
                summaryData.put(CustomDesignerConstants.CustomerVendor_ECC_NO, buyerexcRegNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_IMPORTER_ECC_NUMBER, ImporterECCNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_IEC_NUMBER, IECNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_RANGE_CODE, buyerRange);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_DIVISION_CODE, buyerDivision);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_COMMISSIONERATE_CODE, buyerComrate);
                summaryData.put(CustomDesignerConstants.CUSTOMER_SERVICE_TAX_REG_NO, buyerServiceTaxRegNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, quotation.getCustomer().getGSTIN() != null ? quotation.getCustomer().getGSTIN() : "");
                // ****************************************************************************************************
            }
            KwlReturnObject kmsg = null;
            String userId = requestObj.optString(Constants.useridKey);
            KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userresult.getEntityList().get(0);
            extraparams.put("approvestatuslevel", quotation.getApprovestatuslevel());
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
            String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(authHandler.formattedAmount(totalAmount, companyid))), currency,countryLanguageId );
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if(countryid == Constants.INDONESIAN_COUNTRY_ID){
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(authHandler.formattedAmount(totalAmount, companyid))), indoCurrency);
            }
            String gstAmountInWords = EnglishNumberToWordsOjb.convert(totaltax, currency,countryLanguageId );
            //Details like company details
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);

            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, authHandler.formattedAmount(totaltax, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, authHandler.formattedAmount(authHandler.round(totalAmount,companyid), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, authHandler.formattedAmount((subtotal - totalDiscount), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, authHandler.formattedAmount(((subtotal - totalDiscount) + totaltax), companyid)); //ERP-25162
            summaryData.put(CustomDesignerConstants.SummaryTaxPercent, taxPercent);
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword + " Only.");
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, authHandler.formattedAmount(totalDiscount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsDescription_fieldTypeId, term);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, termsName);
            summaryData.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, totalTermAmount);
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPreText_fieldTypeId, config == null ? "" : config.getPdfPreText());
            summaryData.put(CustomDesignerConstants.CustomDesignPORefNo, quotation.getCustomerPORefNo() != null ? quotation.getCustomerPORefNo() : "");
            summaryData.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, quotation.getCustomer().getCreditTerm() != null ? quotation.getCustomer().getCreditTerm().getTermdays() + " Days" : "0 Days");
            summaryData.put(CustomDesignerConstants.CustomerVendor_Code, quotation.getCustomer().getAcccode() != null ? quotation.getCustomer().getAcccode() : "");
            summaryData.put(CustomDesignerConstants.BillTo, billAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.ShipTo, shipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.Createdby, createdby);
            summaryData.put(CustomDesignerConstants.Updatedby, updatedby);
            
            /*Exchanged Rate Overall*/
            summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTotalAmount, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateSubTotal, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTotalTax, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateSubTotalwithDiscount, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTermAmount, companyid));
            
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, 1);
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, authHandler.formattedAmount(globalLevelExchangedRateTotalAmount, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, authHandler.formattedAmount(globalLevelExchangedRateSubTotal, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, authHandler.formattedAmount(globalLevelExchangedRateTotalTax, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, authHandler.formattedAmount(globalLevelExchangedRateSubTotalwithDiscount, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, authHandler.formattedAmount(globalLevelExchangedRateTermAmount, companyid));
            
            summaryData.put(CustomDesignerConstants.TotalQuantity, totalQuantity);
            summaryData.put(CustomDesignerConstants.CustomDesignTemplate_ItemsNo, rowcnt);
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.AllTerms, allTerms);
            summaryData.put(CustomDesignerConstants.CUSTOMER_TITLE, customerTitle);
//            if(countryid == Constants.indian_country_id) {
            summaryData.put(CustomDesignerConstants.AllLineLevelTax, allLineLevelTax);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxAmount, allLineLevelTaxAmount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTermSigns, lineLevelTaxSign);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxBasic, allLineLevelTaxBasic);
            summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, quotation.getCustomer().getVATTINnumber()!= null ? quotation.getCustomer().getVATTINnumber() : "");
            summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, quotation.getCustomer().getCSTTINnumber()!= null ? quotation.getCustomer().getCSTTINnumber() : "");
            summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber()!= null ? extraCompanyPreferences.getVatNumber() : "");
            summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber()!= null ? extraCompanyPreferences.getCstNumber() : "");
            summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName()) ? user.getFullName() : "");
            summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber())?user.getContactNumber():"");
            summaryData.put(CustomDesignerConstants.AllTermNames, termsNameWithoutPercent);
            summaryData.put(CustomDesignerConstants.AllTermSigns, termssign);
            summaryData.put(CustomDesignerConstants.AllTermAmounts, termsamount);
            summaryData.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, authHandler.formattedAmount(((subtotal-totalDiscount) + totalTermAmount), companyid));
            int deliveryDate = 0;
            String deliveryDateVal = "";
            String driver = "";
            String vehicleNo = "";
            String deliveryTime = "";
            if (extraCompanyPreferences.isDeliveryPlanner()) {
                deliveryDate = quotation.getCustomer() != null ? quotation.getCustomer().getDeliveryDate():-1;
                deliveryDateVal = getDeliverDayVal(deliveryDate);
                deliveryTime = (quotation.getCustomer() != null && quotation.getCustomer().getDeliveryTime() != null) ? quotation.getCustomer().getDeliveryTime():"";
                driver = (quotation.getCustomer() != null && quotation.getCustomer().getDriver() != null)? quotation.getCustomer().getDriver().getValue():"";
                vehicleNo = (quotation.getCustomer() != null && quotation.getCustomer().getVehicleNo() != null)? quotation.getCustomer().getVehicleNo().getValue():""; 
            }
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, deliveryDateVal);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, deliveryTime);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDriver, driver);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo,vehicleNo);
            
            String replacementNo = replacementNumber.toString();
            replacementNo = replacementNo.substring(1, replacementNo.length()-1);
            replacementNo = replacementNo.replaceAll(",", "!##");
            summaryData.put(CustomDesignerConstants.REPLACEMENT_NUMBER, replacementNo);
            summaryData.put(CustomDesignerConstants.GSTAmountInWords, gstAmountInWords + Constants.ONLY);
            
//            }
            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    public String getPurchaseSerialid(String salesid) throws ServiceException {
        String purchaseMapId = "";
        BatchSerialMapping batchSerialMapping = null;
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("salesSerial.id");
        filter_params.add(salesid);
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject result = accCommonTablesDAO.getSerialMappingDetails(filterRequestParams);

        List list = result.getEntityList();
        Iterator iter = list.iterator();
        int i = 1;
        while (iter.hasNext()) {
            batchSerialMapping = (BatchSerialMapping) iter.next();
        }
        if (batchSerialMapping != null) {
            purchaseMapId = batchSerialMapping.getPurchaseSerial().getId();
        }
        return purchaseMapId;

    }

    public double getOpeningBalanceOfAccountLedger(HashMap<String, Object> requestParams, Account account, boolean isVendorOrCustomer, String vendorOrCustomerId) {
        double totalOpeningAmount = 0d;
        try {
            String companyid = "";
            if(requestParams.containsKey("companyid")){
                companyid = (String) requestParams.get("companyid");
            }
            boolean shouldAccountOpeningBalanceInclude = !accountHasOpeningTransactionsLedger(requestParams, account, isVendorOrCustomer, vendorOrCustomerId);
            /*if(account !=null && StringUtil.equal(account.getName(), "7100 GST Input")){
             account.getName();
             account.getOpeningBalance();
             }*/

            if (!shouldAccountOpeningBalanceInclude) {
                // calculating amount due of opening balance Customer invoices.

                requestParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        requestParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                    }
                } else {
//                if(StringUtil.equal(account.getName(), "Trade Debtors"))
//                System.out.println(account.getName());
                    requestParams.put("accountId", account.getID());
                    requestParams.put("isAccountInvoices", true);
                }

                if (Constants.OpeningBalanceBaseAmountFlag) {
                    KwlReturnObject result = accInvoiceDAOobj.getOpeningBalanceTotalBaseAmountForInvoices(requestParams);
                    if (result.getEntityList() != null && result.getEntityList().get(0) != null) {
                        totalOpeningAmount += (Double) result.getEntityList().get(0);
                    }
                } else {
                    KwlReturnObject result = accInvoiceDAOobj.getOpeningBalanceInvoices(requestParams);

                    if (result.getEntityList() != null) {
                        Iterator itr = result.getEntityList().iterator();
                        while (itr.hasNext()) {
                            shouldAccountOpeningBalanceInclude = false;
                            Invoice invoice = (Invoice) itr.next();

                            double externalCurrencyRate = 0d;
                            boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                            Date invoiceCreationDate = null;
                            double amountDue = 0d;
                            if (invoice.isNormalInvoice()) {
                                if (Constants.InvoiceAmountDueFlag) {
                                    List ll = getInvoiceDiscountAmountInfo(requestParams, invoice);
                                    amountDue = (Double) ll.get(0);
                                } else {
                                    List ll = getAmountDue_Discount(requestParams, invoice);
                                    amountDue = (Double) ll.get(0);
                                }
//                                invoiceCreationDate = invoice.getJournalEntry().getEntryDate();
                                externalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
                            } else {
                                amountDue = invoice.getOriginalOpeningBalanceAmount();
                                externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                            }
                            invoiceCreationDate = invoice.getCreationDate();

                            String fromcurrencyid = invoice.getCurrency().getCurrencyID();
//                    String tocurrencyid = invoice.getCustomer().getCompany().getCurrency().getCurrencyID();

//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amountDue, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                            }

                            totalOpeningAmount += (Double) bAmt.getEntityList().get(0);

                        }
                    }
                }

                // Calculating Amount Due of opening balance Payment
                //HashMap<String, Object> receiptParams = accReceiptController.getReceiptRequestMap(request);
                requestParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        requestParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                    }
                } else {
                    requestParams.put("accountId", account.getID());
                    requestParams.put("isAccountReceipts", true);
                }

                // getting opening balance receipt excluding normal Receipts
                KwlReturnObject receiptResult = null;
                requestParams.put("excludeNormal", true);
                if (Constants.OpeningBalanceBaseAmountFlag) {
                    receiptResult = accReceiptobj.getOpeningBalanceTotalBaseAmountForReceipts(requestParams);
                    if (receiptResult.getEntityList() != null && receiptResult.getEntityList().get(0) != null) {
                        totalOpeningAmount -= (Double) receiptResult.getEntityList().get(0);
                    }
                } else {
                    receiptResult = accReceiptobj.getOpeningBalanceReceipts(requestParams);
                    if (receiptResult.getEntityList() != null) {
                        Iterator itr = receiptResult.getEntityList().iterator();
                        while (itr.hasNext()) {
                            shouldAccountOpeningBalanceInclude = false;
                            Receipt receipt = (Receipt) itr.next();
                            double externalCurrencyRate = 0d;
                            boolean isopeningBalanceRecceipt = receipt.isIsOpeningBalenceReceipt();
                            double amountDue = 0d;
                            Date receiptCreationDate = null;
//                    Receipt receipt = (Receipt) itr.next();
                            if (!receipt.isNormalReceipt() && receipt.isIsOpeningBalenceReceipt()) {
                                amountDue = receipt.getDepositAmount();
                                receiptCreationDate = receipt.getCreationDate();
                                externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                            }
                            String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceRecceipt && receipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                            }

                            totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);

                        }
                    }
                }

                // getting opening balance receipts which are normal receipts also
                requestParams.remove("excludeNormal");
                requestParams.put("onlyOpeningNormalReceipts", true);

                if (account != null) {
                    requestParams.put("accountId", account.getID());
                }

                // getting opening balance due of vendor invoices for a vendor
                //requestParams = accGoodsReceiptControllerCMN.getGoodsReceiptMap(request);
                requestParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes

                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        requestParams.put(InvoiceConstants.vendorid, vendorOrCustomerId);
                    }
                } else {
                    requestParams.put("accountId", account.getID());
                    requestParams.put("isAccountInvoices", true);
                }

                if (Constants.OpeningBalanceBaseAmountFlag) {
                    KwlReturnObject result = accGoodsReceiptobj.getOpeningBalanceTotalBaseAmountForInvoices(requestParams);
                    List list = result.getEntityList();
                    if (list != null && list.get(0) != null) {
                        totalOpeningAmount += (Double) list.get(0);
                    }
                } else {
                    KwlReturnObject result = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
                    List list = result.getEntityList();
                    if (list != null) {
                        Iterator it = list.iterator();
                        while (it.hasNext()) {

                            shouldAccountOpeningBalanceInclude = false;
                            List ll = null;
                            double amountdue = 0d;
                            String grId = it.next().toString();
                            KwlReturnObject grReceiptReturnObj = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(), grId);
                            GoodsReceipt goodsReceipt = (GoodsReceipt) grReceiptReturnObj.getEntityList().get(0);
                            Date invoiceCreationDate = null;
                            double externalCurrencyRate = 0d;
                            boolean isopeningBalanceInvoice = goodsReceipt.isIsOpeningBalenceInvoice();

                            if (goodsReceipt.isNormalInvoice()) {// gr which are not created from opening balance pop-up
//                                invoiceCreationDate = goodsReceipt.getJournalEntry().getEntryDate();
                                externalCurrencyRate = goodsReceipt.getJournalEntry().getExternalCurrencyRate();

                                if (goodsReceipt.isIsExpenseType()) {
                                    ll = accGoodsReceiptCommon.getExpGRAmountDue(requestParams, goodsReceipt);
                                    amountdue = (Double) ll.get(1);
//                        belongsTo1099 = (Boolean) ll.get(3);
                                } else {
                                    if (Constants.InvoiceAmountDueFlag) {
                                        ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(requestParams, goodsReceipt);
                                        amountdue = (Double) ll.get(1);
                                    } else {
                                        ll = accGoodsReceiptCommon.getGRAmountDue(requestParams, goodsReceipt);
                                        amountdue = (Double) ll.get(1);
                                    }
                                }
                            } else {
                                amountdue = goodsReceipt.getOriginalOpeningBalanceAmount();
                                externalCurrencyRate = goodsReceipt.getExchangeRateForOpeningTransaction();
                            }
                            invoiceCreationDate = goodsReceipt.getCreationDate();

                            String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();

                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceInvoice && goodsReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }

            // getting opening balance payments excluding normal payments
                // Calculating Amount Due of opening balance Payment
                //HashMap<String, Object> paymentParams = accVendorPaymentController.getPaymentMap(request);
                requestParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        requestParams.put(InvoiceConstants.vendorid, vendorOrCustomerId);
                    }
                } else {
                    requestParams.put("accountId", account.getID());
                    requestParams.put("isAccountPayments", true);
                }

                requestParams.put("excludeNormal", true);

                if (Constants.OpeningBalanceBaseAmountFlag) {
                    KwlReturnObject paymentresult = accVendorPaymentobj.getOpeningBalanceTotalBaseAmountForPayments(requestParams);
                    if (paymentresult.getEntityList() != null && paymentresult.getEntityList().get(0) != null) {
                        totalOpeningAmount -= (Double) paymentresult.getEntityList().get(0);
                    }
                } else {
                    KwlReturnObject paymentresult = accVendorPaymentobj.getOpeningBalancePayments(requestParams);
                    if (paymentresult.getEntityList() != null) {
                        Iterator itr = paymentresult.getEntityList().iterator();
                        while (itr.hasNext()) {

                            shouldAccountOpeningBalanceInclude = false;
                            Payment payment = (Payment) itr.next();
                            double externalCurrencyRate = 0d;
                            boolean isopeningBalancePayment = payment.isIsOpeningBalencePayment();
                            double amountDue = 0d;
                            Date receiptCreationDate = null;
//                    Receipt receipt = (Receipt) itr.next();
                            if (!payment.isNormalPayment() && payment.isIsOpeningBalencePayment()) {
                                amountDue = payment.getDepositAmount();
                                receiptCreationDate = payment.getCreationDate();
                                externalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                            }
                            String fromcurrencyid = payment.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (isopeningBalancePayment && payment.isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);

                        }
                    }
                }

                boolean isCNForAccountIncluded = false;

                // Calculating Amount Due of opening balance CNs
                //HashMap<String, Object> noteParams = accCreditNoteController.getCreditNoteMap(request);
                requestParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        requestParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                    }
                } else {
                    requestParams.put("accountId", account.getID());
                    requestParams.put("isAccountCNs", true);
                    isCNForAccountIncluded = true;
                }

                // getting opening balance cn excluding normal cns
                requestParams.put("excludeNormal", true);
                if (Constants.OpeningBalanceBaseAmountFlag) {
                    KwlReturnObject cnResult = accCreditNoteobj.getOpeningBalanceTotalBaseAmountForCNs(requestParams);
                    if (cnResult.getEntityList() != null) {
                        totalOpeningAmount -= (Double) cnResult.getEntityList().get(0);
                    }
                } else {
                    KwlReturnObject cnResult = accCreditNoteobj.getOpeningBalanceCNs(requestParams);
                    if (cnResult.getEntityList() != null) {
                        Iterator itr = cnResult.getEntityList().iterator();
                        while (itr.hasNext()) {

                            shouldAccountOpeningBalanceInclude = false;
                            CreditNote cn = (CreditNote) itr.next();
                            double externalCurrencyRate = 0d;
                            double amountDue = 0d;
                            boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
                            Date cnCreationDate = null;
                            if (!cn.isNormalCN() && cn.isIsOpeningBalenceCN()) {
                                amountDue = cn.getCnamount();
                                cnCreationDate = cn.getCreationDate();
                                externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                            }
                            String fromcurrencyid = cn.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }

                boolean isDNForAccountIncluded = false;
                // Calculating Amount Due of opening balance DNs for Customers

                //HashMap<String, Object> cdnParams = accDebitNoteController.gettDebitNoteMap(request);
                requestParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        requestParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                    }
                } else {
                    requestParams.put("accountId", account.getID());
                    requestParams.put("isAccountDNs", true);
                    isDNForAccountIncluded = true;
                }

                // getting opening balance cn excluding normal cns
                requestParams.put("excludeNormal", true);
                if (Constants.OpeningBalanceBaseAmountFlag) {
                    KwlReturnObject cdnResult = accDebitNoteobj.getOpeningBalanceTotalBaseAmountForCustomerDNs(requestParams);
                    if (cdnResult.getEntityList() != null) {
                        totalOpeningAmount += (Double) cdnResult.getEntityList().get(0);
                    }
                } else {
                    KwlReturnObject cdnResult = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
                    if (cdnResult.getEntityList() != null) {
                        Iterator itr = cdnResult.getEntityList().iterator();
                        while (itr.hasNext()) {
                            shouldAccountOpeningBalanceInclude = false;

                            DebitNote cn = (DebitNote) itr.next();
                            double externalCurrencyRate = 0d;
                            boolean isopeningBalanceDN = cn.isIsOpeningBalenceDN();
                            double amountDue = 0d;
                            Date cnCreationDate = null;
                            if (!cn.isNormalDN() && cn.isIsOpeningBalenceDN()) {
                                amountDue = cn.getDnamount();
                                cnCreationDate = cn.getCreationDate();
                                externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                            }
                            String fromcurrencyid = cn.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceDN && cn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }

                // Calculating Amount Due of opening balance DNs
                //HashMap<String, Object> dnParams = accDebitNoteController.gettDebitNoteMap(request);
                requestParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        requestParams.put("vendorid", vendorOrCustomerId);
                    }
                } else {
                    requestParams.put("accountId", account.getID());
                    requestParams.put("isAccountDNs", true);
                }

                // getting opening balance cn excluding normal dns
                requestParams.put("excludeNormal", true);
                if (Constants.OpeningBalanceBaseAmountFlag) {
                    KwlReturnObject dnResult = accDebitNoteobj.getOpeningBalanceTotalBaseAmountForDNs(requestParams);
                    if (dnResult.getEntityList() != null) {
                        totalOpeningAmount -= (Double) dnResult.getEntityList().get(0);
                    }
                } else {
                    KwlReturnObject dnResult = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
                    if (dnResult.getEntityList() != null) {
                        Iterator itr = dnResult.getEntityList().iterator();
                        while (itr.hasNext()) {
                            shouldAccountOpeningBalanceInclude = false;

                            DebitNote cn = (DebitNote) itr.next();
                            double externalCurrencyRate = 0d;
                            boolean isopeningBalanceDN = cn.isIsOpeningBalenceDN();
                            double amountDue = 0d;
                            Date cnCreationDate = null;
                            if (!cn.isNormalDN() && cn.isIsOpeningBalenceDN()) {
                                amountDue = cn.getDnamount();
                                cnCreationDate = cn.getCreationDate();
                                externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                            }
                            String fromcurrencyid = cn.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceDN && cn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount -= (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }

                // Calculating Amount Due of opening balance CNs for Vendors
                //HashMap<String, Object> vcnParams = accCreditNoteController.getCreditNoteMap(request);
                requestParams.remove("ss");//ss used to serch account but it serches opening transactions so wrong calculation goes
                if (isVendorOrCustomer) {
                    if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                        requestParams.put("vendorid", vendorOrCustomerId);
                    }
                } else {
                    requestParams.put("accountId", account.getID());
                    requestParams.put("isAccountCNs", true);
                }

                // getting opening balance cn excluding normal cns
                requestParams.put("excludeNormal", true);
                if (Constants.OpeningBalanceBaseAmountFlag) {
                    KwlReturnObject vcnResult = accCreditNoteobj.getOpeningBalanceTotalBaseAmountForVendorCNs(requestParams);
                    if (vcnResult.getEntityList() != null) {
                        totalOpeningAmount += (Double) vcnResult.getEntityList().get(0);
                    }
                } else {
                    KwlReturnObject vcnResult = accCreditNoteobj.getOpeningBalanceVendorCNs(requestParams);
                    if (vcnResult.getEntityList() != null) {
                        Iterator itr = vcnResult.getEntityList().iterator();
                        while (itr.hasNext()) {
                            shouldAccountOpeningBalanceInclude = false;

                            CreditNote cn = (CreditNote) itr.next();
                            double externalCurrencyRate = 0d;
                            double amountDue = 0d;
                            boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
                            Date cnCreationDate = null;
                            if (!cn.isNormalCN() && cn.isIsOpeningBalenceCN()) {
                                amountDue = cn.getCnamount();
                                cnCreationDate = cn.getCreationDate();
                                externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                            }
                            String fromcurrencyid = cn.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                            }
                            totalOpeningAmount += (Double) bAmt.getEntityList().get(0);
                        }
                    }
                }
            }

            ArrayList<String> filter_names = new ArrayList();
            ArrayList<String> filter_params = new ArrayList();
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();

            filter_names.add("ID");
            filter_params.add(account.getGroup().getID());
            rRequestParams.put("filter_names", filter_names);
            rRequestParams.put("filter_params", filter_params);

            KwlReturnObject accountReturnObj = accAccountDAOobj.getGroup(rRequestParams);
            Group group = (Group) accountReturnObj.getEntityList().get(0);

            if (account != null && shouldAccountOpeningBalanceInclude) {
                totalOpeningAmount = account.getOpeningBalance();
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalOpeningAmount, account.getCurrency().getCurrencyID(), account.getCreationDate(), 0);
                totalOpeningAmount = (Double) bAmt.getEntityList().get(0);
            } else if (account != null && group.getNature() == 0) { // amount will be (-)ve if nature of account is Liability. because of to show (+)ve amount to Creditor account in balance sheet, Ledger Report, Trail Balance etc.
                totalOpeningAmount = -totalOpeningAmount;
            }

            String totalOpeningAmountFormatted = authHandler.formattedAmount(totalOpeningAmount, companyid);
            totalOpeningAmount = Double.parseDouble(totalOpeningAmountFormatted);

        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return totalOpeningAmount;
    }

    public boolean accountHasOpeningTransactionsLedger(HashMap<String, Object> requestParams, Account account, boolean isVendorOrCustomer, String vendorOrCustomerId) {
        boolean accountHasOpeningTransactions = false;
        int cnt = 0;
        try {
            // calculating amount due of opening balance Customer invoices.

            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    requestParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
                }
            } else {
                requestParams.put("accountId", account.getID());
                requestParams.put("isAccountInvoices", true);
            }

            cnt = accInvoiceDAOobj.getOpeningBalanceInvoiceCount(requestParams);

            if (cnt > 0) {
                return true;
            }
            // Calculating Amount Due of opening balance Payment

            //HashMap<String, Object> receiptParams = accReceiptController.getReceiptRequestMap(request);
//            receiptParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
//            if (isVendorOrCustomer) {
//                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
//                    receiptParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
//                }
//            } else {
//                receiptParams.put("accountId", account.getID());
//                receiptParams.put("isAccountReceipts", true);
//            }
//
//            // getting opening balance receipt excluding normal Receipts
//            receiptParams.put("excludeNormal", true);
            cnt = accReceiptobj.getOpeningBalanceReceiptCount(requestParams);
            if (cnt > 0) {
                return true;
            }

            // getting opening balance receipts which are normal receipts also
//            receiptParams.remove("excludeNormal");
//            receiptParams.put("onlyOpeningNormalReceipts", true);
//
//            if (account != null) {
//                receiptParams.put("accountId", account.getID());
//            }
            // getting opening balance due of vendor invoices for a vendor
            //requestParams = accGoodsReceiptControllerCMN.getGoodsReceiptMap(request);
//            requestParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
            if (isVendorOrCustomer) {
                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
                    requestParams.put(InvoiceConstants.vendorid, vendorOrCustomerId);
                }
            } else {
                requestParams.put("accountId", account.getID());
                requestParams.put("isAccountInvoices", true);
            }

            cnt = accGoodsReceiptobj.getOpeningBalanceInvoiceCount(requestParams);
            if (cnt > 0) {
                return true;
            }

            // getting opening balance payments excluding normal payments
            // Calculating Amount Due of opening balance Payment
//            HashMap<String, Object> paymentParams = accVendorPaymentController.getPaymentMap(request);
//            paymentParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
//            if (isVendorOrCustomer) {
//                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
//                    paymentParams.put(InvoiceConstants.vendorid, vendorOrCustomerId);
//                }
//            } else {
//                paymentParams.put("accountId", account.getID());
//                paymentParams.put("isAccountPayments", true);
//            }
//
//            paymentParams.put("excludeNormal", true);
            cnt = accVendorPaymentobj.getOpeningBalancePaymentCount(requestParams);
            if (cnt > 0) {
                return true;
            }

            // Calculating Amount Due of opening balance CNs
//            HashMap<String, Object> noteParams = accCreditNoteController.getCreditNoteMap(request);
//            noteParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
//            if (isVendorOrCustomer) {
//                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
//                    noteParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
//                }
//            } else {
//                noteParams.put("accountId", account.getID());
//                noteParams.put("isAccountCNs", true);
//            }
            // getting opening balance cn excluding normal cns
//            noteParams.put("excludeNormal", true);
            cnt = accCreditNoteobj.getOpeningBalanceCNCount(requestParams);
            if (cnt > 0) {
                return true;
            }

            // Calculating Amount Due of opening balance DNs for Customers
//            HashMap<String, Object> cdnParams = accDebitNoteController.gettDebitNoteMap(request);
//            cdnParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
//            if (isVendorOrCustomer) {
//                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
//                    cdnParams.put(InvoiceConstants.customerid, vendorOrCustomerId);
//                }
//            } else {
//                cdnParams.put("accountId", account.getID());
//                cdnParams.put("isAccountDNs", true);
//            }
//
//            // getting opening balance cn excluding normal cns
//            cdnParams.put("excludeNormal", true);
            cnt = accDebitNoteobj.getOpeningBalanceCustomerDNCount(requestParams);
            if (cnt > 0) {
                return true;
            }

            // Calculating Amount Due of opening balance DNs
//            HashMap<String, Object> dnParams = accDebitNoteController.gettDebitNoteMap(request);
//            dnParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
//            if (isVendorOrCustomer) {
//                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
//                    dnParams.put("vendorid", vendorOrCustomerId);
//                }
//            } else {
//                dnParams.put("accountId", account.getID());
//                dnParams.put("isAccountDNs", true);
//            }
//
//            // getting opening balance cn excluding normal dns
//            dnParams.put("excludeNormal", true);
            cnt = accDebitNoteobj.getOpeningBalanceDNCount(requestParams);
            if (cnt > 0) {
                return true;
            }

            // Calculating Amount Due of opening balance CNs for Vendors
//            HashMap<String, Object> vcnParams = accCreditNoteController.getCreditNoteMap(request);
//            vcnParams.remove("ss");//serch string unnecessary getting added in query and resulting as wrong opening balance so removing it from requestmap ERP-4639
//            if (isVendorOrCustomer) {
//                if (!StringUtil.isNullOrEmpty(vendorOrCustomerId)) {
//                    vcnParams.put("vendorid", vendorOrCustomerId);
//                }
//            } else {
//                vcnParams.put("accountId", account.getID());
//                vcnParams.put("isAccountCNs", true);
//            }
//
//            // getting opening balance cn excluding normal cns
//            vcnParams.put("excludeNormal", true);
            cnt = accCreditNoteobj.getOpeningBalanceVendorCNCount(requestParams);
            if (cnt > 0) {
                return true;
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return accountHasOpeningTransactions;
    }

    public String getPurchaseBatchid(String salesid) throws ServiceException {
        String purchaseMapId = "";
        SalesPurchaseBatchMapping productBatch = null;
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("salesBatch.id");
        filter_params.add(salesid);
        order_by.add("id");
        order_type.add("asc");
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        filterRequestParams.put("order_by", order_by);
        filterRequestParams.put("order_type", order_type);
        KwlReturnObject result = accCommonTablesDAO.getBatchMappingDetails(filterRequestParams);

        List list = result.getEntityList();
        Iterator iter = list.iterator();
        int i = 1;
        while (iter.hasNext()) {
            productBatch = (SalesPurchaseBatchMapping) iter.next();
        }
        if (productBatch != null) {
            purchaseMapId = productBatch.getPurchaseBatch().getId();
        }
        return purchaseMapId;

    }

    public List getCalculatedAmountDueForOpeningInvoices(HashMap<String, Object> requestParams, Invoice invoice) throws ServiceException {
        List ll = new ArrayList();
        double amountdue = 0;
        double totalAmountOfInvoice = 0, ramount = 0;
        String currencyid = (String) requestParams.get("gcurrencyid");
        String companyid = (String) requestParams.get("companyid");

        String currencyFilterForTrans = "";
        if (requestParams.containsKey("currencyfilterfortrans")) {
            currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
        }
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID = baseCurrency.getCurrencyID();

        currencyid = (invoice.getCurrency() == null ? baseCurrencyID : invoice.getCurrency().getCurrencyID());
        double externalCurrencyRate = 0d;
        Date invCreationDate = null;
        externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
        invCreationDate = invoice.getCreationDate();
        totalAmountOfInvoice = invoice.getOriginalOpeningBalanceAmount();

        HashMap<String, Object> reqParams1 = new HashMap();
        reqParams1.put("invoiceid", invoice.getID());
        reqParams1.put("companyid", companyid);

        //Get invoice amount used in credit note against SI or credit note made otherwise and then linked to invoice
        KwlReturnObject result = accCreditNoteobj.getCNRowsFromInvoice(reqParams1);
        List<CreditNoteDetail> rows = result.getEntityList();
        double invoiceAmountUsedINCreditNote = 0;
        for (CreditNoteDetail cnr : rows) {
            if (cnr.getDiscount() != null) {
                invoiceAmountUsedINCreditNote += cnr.getDiscount().getAmountinInvCurrency();
            }
        }

        //Get invoice amount used in receipt
        result = accReceiptobj.getReceiptFromInvoice(reqParams1);
        List<ReceiptDetail> RDlist = result.getEntityList();
        for (ReceiptDetail rd : RDlist) {
            double receiptExchangeRate = 0d;
            Date receiptCreationDate = null;

            receiptExchangeRate = rd.getReceipt().getJournalEntry().getExternalCurrencyRate();
//            receiptCreationDate = rd.getReceipt().getJournalEntry().getEntryDate();
            receiptCreationDate = rd.getReceipt().getCreationDate();

            if (rd.getFromCurrency() != null && rd.getToCurrency() != null) {
                ramount += rd.getAmount() / rd.getExchangeRateForTransaction();
            } else {
                String fromcurrencyid = (rd.getReceipt().getCurrency() == null ? baseCurrencyID : rd.getReceipt().getCurrency().getCurrencyID());
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, rd.getAmount(), fromcurrencyid, currencyid, receiptCreationDate, receiptExchangeRate);
                ramount += (Double) bAmt.getEntityList().get(0);
            }

        }

        //Get invoice amount in advance receipt linked to invoice
        result = accReceiptobj.getLinkDetailReceipt(reqParams1);
        List<LinkDetailReceipt> linkDetailReceipts = result.getEntityList();
        double amountLinkedInReceipt = 0;
        if (linkDetailReceipts != null && !linkDetailReceipts.isEmpty()) {
            for (LinkDetailReceipt ldr : linkDetailReceipts) {
                amountLinkedInReceipt += ldr.getAmountInInvoiceCurrency();
            }
        }

        double amountDueOriginal = 0;
        amountdue = totalAmountOfInvoice - invoiceAmountUsedINCreditNote - ramount - amountLinkedInReceipt;
        if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {//amount due difference for revaluation.
            amountDueOriginal = amountdue;
            if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, invCreationDate, externalCurrencyRate);
                amountdue = (Double) bAmt.getEntityList().get(0);
            }
        } else {
            amountdue = 0;
        }
        ll.add(amountdue);
        ll.add(amountDueOriginal);
        return ll;
    }
    public JSONObject getSalesByServiceProductDetailReport(HttpServletRequest request, boolean isExport) {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            int reportId = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("reportId"))) {
                reportId = Integer.parseInt(request.getParameter("reportId"));
            }
            String start = "0";
            String limit = "30";
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.start)) && !StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) {
                start = request.getParameter(Constants.start);
                limit = request.getParameter(Constants.limit);
            }

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            if (!isExport) {
                requestParams.put(Constants.ss, request.getParameter(Constants.ss));
                requestParams.put(Constants.start, start);
                requestParams.put(Constants.limit, limit);
            }
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.Acc_Search_Json))) {
                requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
                requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
                requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
            }

            KwlReturnObject result = accInvoiceDAOobj.getInvoicesHavingServiceProduct(requestParams);
            List<String> list = result.getEntityList();
            int count = result.getRecordTotalCount();

            String companyId = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateFormatter(request);

            // Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();
            jobjTemp.put("name", "billNo");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.field.billNo", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "billNo");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "billDate");
            jobjTemp.put("type", "date");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.1099.gridbillDate", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "billDate");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "fileRef");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.field.fileRef", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "fileRef");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "name");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.invset.header.1", null, RequestContextUtils.getLocale(request)));//Name
            jobjTemp.put("dataIndex", "name");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencycode");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencysymbol");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencyid");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.cust.currency", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "currencycode");
            jobjTemp.put("hidden", "true");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            HashMap hashMap = new HashMap();
            hashMap.put("companyId", companyId);
            hashMap.put("reportId", reportId);
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            List customFieldList = new ArrayList();
            List serviceProductList = new ArrayList();
            double totalGrossAmount = 0;
            double totalTaxAmount = 0;
            double totalTermAmount = 0;
            double totalAmount = 0;
            double totalBaseAmount = 0;
            for (String invoiceID : list) {
                double grossAmount = 0;
                double taxAmount = 0;
                double termAmount = 0;
                KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceID);
                Invoice invoice = (Invoice) extracapresult.getEntityList().get(0);

                JSONObject obj = new JSONObject();
                JSONObject custJobj = new JSONObject();
                String jeId = invoice.getJournalEntry().getID();

                // Get Custom Column and their values
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();

                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Invoice_ModuleId));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                String customFieldMapValues = "";
                KwlReturnObject custumObjresult = null;
                Map<String, Object> variableMap = new HashMap<String, Object>();
                custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                replaceFieldMap = new HashMap<String, String>();
                if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                    AccJECustomData jeDetailCustom = (AccJECustomData) custumObjresult.getEntityList().get(0);
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyId);
                        params.put("isExport", true);
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, custJobj, params);
                    }
                }

                /*
                 Add Customer Custom data
                 */
                HashMap<String, Object> customerParams = new HashMap();
                customerParams.put("companyId", companyId);
                customerParams.put("customerId", invoice.getCustomer().getID());
                putCustomerCustomData(customerParams, custJobj);

                for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                    String column = "Custom_" + customizeReportMapping.getDataIndex();
                    String dataIndex = customizeReportMapping.getDataIndex();
                    String header = customizeReportMapping.getDataHeader();
                    if (customizeReportMapping.getModuleId() == 25) {
                        dataIndex = dataIndex + "Customer";
                        header = header + " (Customer)";
                        column = "CustomerCustom_" + customizeReportMapping.getDataIndex();
                    }
                    if (custJobj.has(column)) {
                        customFieldMapValues = custJobj.getString(column);
                        if (!customFieldList.contains(dataIndex)) {
                            jobjTemp = new JSONObject();
                            jobjTemp.put("name", dataIndex);
                            jarrRecords.put(jobjTemp);

                            jobjTemp = new JSONObject();
                            jobjTemp.put("header", header);
                            jobjTemp.put("dataIndex", dataIndex);
                            jobjTemp.put("width", 150);
                            jobjTemp.put("pdfwidth", 150);
                            jobjTemp.put("custom", "true");
                            jarrColumns.put(jobjTemp);

                            customFieldList.add(dataIndex);
                        }
                        obj.put(dataIndex, customFieldMapValues);
                    } else {
                        if (!customFieldList.contains(dataIndex)) {
                            jobjTemp = new JSONObject();
                            jobjTemp.put("name", dataIndex);
                            jarrRecords.put(jobjTemp);

                            jobjTemp = new JSONObject();
                            jobjTemp.put("header", header);
                            jobjTemp.put("dataIndex", dataIndex);
                            jobjTemp.put("width", 150);
                            jobjTemp.put("pdfwidth", 150);
                            jobjTemp.put("custom", "true");
                            jarrColumns.put(jobjTemp);
                            customFieldList.add(dataIndex);
                        }
                    }
                }

                HashMap<String, Object> serviceProductParams = AccountingManager.getGlobalParams(request);
                serviceProductParams.put("start", "0");
                serviceProductParams.put("limit", "15");
                serviceProductParams.put("productTypeID", Producttype.SERVICE);

                KwlReturnObject serviceProductResult = accProductObj.getProductList(serviceProductParams);
                List<Product> productList = serviceProductResult.getEntityList();
                // Service Product Column
                for (Product product : productList) {
                    if (!serviceProductList.contains(product.getProductid())) {
                        jobjTemp = new JSONObject();
                        jobjTemp.put("name", product.getProductid());
                        jarrRecords.put(jobjTemp);

                        jobjTemp = new JSONObject();
                        jobjTemp.put("header", product.getName() + " (" + product.getProductid() + ")");
                        jobjTemp.put("dataIndex", product.getProductid());
                        jobjTemp.put("pdfwidth", 150);
                        jobjTemp.put("width", 150);
                        jarrColumns.put(jobjTemp);
                        serviceProductList.add(product.getProductid());
                    }
                }
                obj.put("billNo", invoice.getInvoiceNumber());
//                obj.put("billDate", (invoice.getJournalEntry() != null) ? df.format(invoice.getJournalEntry().getEntryDate()) : "");
                obj.put("billDate", (invoice.getCreationDate() != null) ? df.format(invoice.getCreationDate()) : "");
                obj.put("fileRef", (invoice.getCustomer() != null) ? invoice.getCustomer().getAcccode() : "");
                obj.put("name", (invoice.getCustomer() != null) ? invoice.getCustomer().getName() : "");
                obj.put("currencyid", (invoice.getCurrency() != null) ? invoice.getCurrency().getCurrencyID() : "");
                obj.put("currencysymbol", (invoice.getCurrency() != null) ? invoice.getCurrency().getSymbol() : "");
                obj.put("currencycode", (invoice.getCurrency() != null) ? invoice.getCurrency().getCurrencyCode() : "");

                // For calculating Invoice Term amount
                HashMap<String, Object> requestParam = new HashMap();
                requestParam.put("invoiceid", invoice.getID());
                KwlReturnObject invoiceTermMapResult = accInvoiceDAOobj.getInvoiceTermMap(requestParam);
                List<InvoiceTermsMap> termMap = invoiceTermMapResult.getEntityList();
                for (InvoiceTermsMap invoiceTerMap : termMap) {
                    termAmount += invoiceTerMap.getTermamount();
                }
                obj.put("termAmount", termAmount);

                // For calculating Invoice tax amount
                if (invoice.getTaxEntry() != null) {
                    taxAmount += invoice.getTaxEntry().getAmount();
                }
                String fromCurrency = invoice.getCurrency() != null ? invoice.getCurrency().getCurrencyID() : currency.getCurrencyID();
                double externalCurrencyRate = 0d;
                JournalEntry je = null;
                Date invoiceCreationDate = invoice.getCreationDate();
                if (invoice.isNormalInvoice() && invoice.getJournalEntry() != null) {
                    je = invoice.getJournalEntry();
//                    invoiceCreationDate = je.getEntryDate();
                    externalCurrencyRate = je.getExternalCurrencyRate();
                }
                // For calculating row tax amount, discount and gross amount
                double rowTaxAmount = 0;
                double discount = 0;
                Set<InvoiceDetail> invoiceDetails = invoice.getRows();
                for (InvoiceDetail invoiceDetail : invoiceDetails) {
                    double quantity = invoiceDetail.getInventory().getQuantity();
                    double rate = invoiceDetail.getRate();

                    if (invoiceDetail.getDiscount() != null) {
                        discount = invoiceDetail.getDiscount().getDiscountValue();
                    }
                    double productAmount = authHandler.round(((rate * quantity) - discount), companyId);
                    grossAmount += productAmount;

                    rowTaxAmount += invoiceDetail.getRowTaxAmount() + invoiceDetail.getRowTermTaxAmount();

                    if (invoiceDetail.getInventory() != null && invoiceDetail.getInventory().getProduct() != null && invoiceDetail.getInventory().getProduct().getProducttype().getID().equalsIgnoreCase(Producttype.SERVICE) && !StringUtil.isNullOrEmpty(invoiceDetail.getInventory().getProduct().getProductid())) {
                        if (obj.has(invoiceDetail.getInventory().getProduct().getProductid())) {
                            productAmount += (Double) obj.get(invoiceDetail.getInventory().getProduct().getProductid());
                            obj.put(invoiceDetail.getInventory().getProduct().getProductid(), productAmount);
                        } else {
                            obj.put(invoiceDetail.getInventory().getProduct().getProductid(), productAmount);
                        }
                    }
                }

                obj.put("grossTotal", authHandler.round(grossAmount, companyId));

                double invoiceTaxAmount = taxAmount + rowTaxAmount;
                double amount = grossAmount + invoiceTaxAmount + termAmount;
                obj.put("gstAmount", authHandler.round(invoiceTaxAmount, companyId));
                obj.put("totalAmount", authHandler.round(amount, companyId));
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, (grossAmount + invoiceTaxAmount + termAmount), fromCurrency, invoiceCreationDate, externalCurrencyRate);
                double amountinbase = (Double) bAmt.getEntityList().get(0);
                obj.put("amountinbase", authHandler.round(amountinbase, companyId));

                totalGrossAmount += grossAmount;
                totalTaxAmount += invoiceTaxAmount;
                totalTermAmount += termAmount;
                totalAmount += amount;
                totalBaseAmount += amountinbase;

                dataJArr.put(obj);
            }
            if (isExport) {
                JSONObject obj = new JSONObject();
                obj.put("billNo", "Total");
                obj.put("billDate", "");
                obj.put("fileRef", "");
                obj.put("name", "");
                obj.put("currencyid", "");
                obj.put("currencysymbol", "");
                obj.put("currencycode", "");
                obj.put("termAmount", authHandler.round(totalTermAmount, companyId));
                obj.put("grossTotal", authHandler.round(totalGrossAmount, companyId));
                obj.put("gstAmount", authHandler.round(totalTaxAmount, companyId));
                obj.put("totalAmount", authHandler.round(totalAmount, companyId));
                obj.put("amountinbase", authHandler.round(totalBaseAmount, companyId));
                dataJArr.put(obj);
            }

            // Column Model
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "grossTotal");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.common.grossamount", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "grossTotal");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "gstAmount");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "GST");
            jobjTemp.put("dataIndex", "gstAmount");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "termAmount");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.common.Term", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "termAmount");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "totalAmount");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.invoiceList.totAmt", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "totalAmount");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountinbase");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.invoiceList.totAmtHome", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "amountinbase");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", count);
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);
            if (isExport) {
                jobj.put("data", dataJArr);
            } else {
                jobj.put("data", commData);
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    /**
     * 
     * @param customerParams
     * @param custJobj= Put Custom data for Customer fields
     * @throws ServiceException
     * @throws JSONException 
     */
    public void putCustomerCustomData(HashMap<String, Object> customerParams, JSONObject custJobj) throws ServiceException, JSONException {
        String companyId = "";
        String customerId = "";
        if (customerParams.containsKey("companyId")) {
            companyId = customerParams.get("companyId").toString();
        }
        if (customerParams.containsKey("customerId")) {
            customerId = customerParams.get("customerId").toString();
        }
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Customer_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        String customFieldMapValues = "";
        KwlReturnObject custumObjresult = null;
        Map<String, Object> variableMap = new HashMap<String, Object>();
        custumObjresult = accountingHandlerDAOobj.getObject(CustomerCustomData.class.getName(), customerId);
        replaceFieldMap = new HashMap<String, String>();
        if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
            CustomerCustomData jeDetailCustom = (CustomerCustomData) custumObjresult.getEntityList().get(0);
            if (jeDetailCustom != null) {
                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                JSONObject params = new JSONObject();
                params.put("companyid", companyId);
                params.put("isExport", true);
                JSONObject jSONObject = new JSONObject();
                DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                Date dateFromDB=null;
                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                    String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                    String colValue = "";
                    if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                        try {
                            String[] valueData = coldata.split(",");
                            for (String value : valueData) {
                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                if (fieldComboData != null) {
                                    colValue += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                }
                            }
                            if (colValue.length() > 1) {
                                colValue = colValue.substring(0, colValue.length() - 1);
                            }
                            custJobj.put("Customer" + varEntry.getKey(), colValue);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                        } catch (Exception ex) {
                            custJobj.put("Customer" + varEntry.getKey(), coldata);
                        }
                    } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            dateFromDB = defaultDateFormat.parse(coldata);
                            coldata = df2.format(dateFromDB);
                        } catch (Exception e) {
                        }
                        custJobj.put("Customer" + varEntry.getKey(), coldata);
                    } else {
                        custJobj.put("Customer" + varEntry.getKey(), coldata != null ? coldata : "");
                    }
                }
            }
        }
    }
    
    public JSONObject getPriceVarianceReport(HttpServletRequest request, boolean isExport) {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {
            int moduleid = StringUtil.isNullOrEmpty(request.getParameter(Constants.moduleid)) ? -1 : Integer.parseInt(request.getParameter(Constants.moduleid));
            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            DateFormat df = authHandler.getGlobalDateFormat();
            String companyid = sessionHandlerImpl.getCompanyid(request);

            // Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            Map<String, Object> requestparams = new HashMap<String, Object>();
            requestparams.put("moduleid", moduleid);
            requestparams.put("locale", RequestContextUtils.getLocale(request));
            createColumnModelForPriceVarianceReport(jarrColumns, jarrRecords, requestparams);
            
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            requestParams.put(InvoiceConstants.productid, (StringUtil.isNullOrEmpty(request.getParameter(InvoiceConstants.productid))) ? "" : request.getParameter(InvoiceConstants.productid));
            requestParams.put(InvoiceConstants.productCategoryid, (StringUtil.isNullOrEmpty(request.getParameter(InvoiceConstants.productCategoryid))) ? "" : request.getParameter(InvoiceConstants.productCategoryid));
            requestParams.put("companyid", companyid);
            if (moduleid == Constants.Acc_Invoice_ModuleId) { // report for Sales Invoice
                getPriceVarianceReportForSalesInvoice(requestParams, dataJArr);
            } else { // report for Sales Order
                getPriceVarianceReportForSalesOrder(requestParams, dataJArr);
            }
            
            int totalCount = dataJArr.length();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                dataJArr = StringUtil.getPagedJSON(dataJArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            
            // Column Model
            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", totalCount);
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);
            
            jobj.put("valid", true);
            if (isExport) {
                jobj.put("data", dataJArr);
            } else {
                jobj.put("data", commData);
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    public void createColumnModelForPriceVarianceReport(JSONArray jarrColumns, JSONArray jarrRecords, Map requestparams) throws JSONException {
        JSONObject jobjTemp = new JSONObject();
        int moduleid = (int) requestparams.get("moduleid");
        Locale locale = null;
        if(requestparams.containsKey("locale")){
        locale = (Locale) requestparams.get("locale");
        }
        jobjTemp.put("name", "custName");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.customer.customerName", null,locale));
        jobjTemp.put("dataIndex", "custName");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "salesPersonName");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.field.SalesPerson", null,locale));
        jobjTemp.put("dataIndex", "salesPersonName");
        jobjTemp.put("width", 125);
        jobjTemp.put("pdfwidth", 125);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "pid");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.product.gridProductID", null,locale));
        jobjTemp.put("dataIndex", "pid");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);        

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "productName");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.rem.prodName", null,locale));
        jobjTemp.put("dataIndex", "productName");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "productDesc");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.productList.gridProductDescription", null,locale));
        jobjTemp.put("dataIndex", "productDesc");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "currencycode");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "currencysymbol");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "currencyid");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.cust.currency", null,locale));
        jobjTemp.put("dataIndex", "currencycode");
        jobjTemp.put("hidden", "true");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "billNo");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", moduleid == Constants.Acc_Invoice_ModuleId ? messageSource.getMessage("acc.invoice.gridInvNo", null, locale) : messageSource.getMessage("acc.field.salesOrderNo", null, locale));
        jobjTemp.put("dataIndex", "billNo");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "billDate");
        jobjTemp.put("type", "date");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", moduleid == Constants.Acc_Invoice_ModuleId ? messageSource.getMessage("acc.rem.34", null,locale) : messageSource.getMessage("acc.commom.salesOrder.date", null,locale));
        jobjTemp.put("dataIndex", "billDate");
        jobjTemp.put("width", 100);
        jobjTemp.put("pdfwidth", 100);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "quantity");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.invoice.gridQty", null,locale));
        jobjTemp.put("dataIndex", "quantity");
        jobjTemp.put("width", 100);
        jobjTemp.put("pdfwidth", 100);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "unitPrice");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.commom.unitPrice.document", null,locale));
        jobjTemp.put("dataIndex", "unitPrice");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "configuredVariancePercentage");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.invoice.configured.variance", null,locale));
        jobjTemp.put("dataIndex", "configuredVariancePercentage");
        jobjTemp.put("width", 125);
        jobjTemp.put("pdfwidth", 125);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "configuredUnitPrice");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.invoice.configured.unitprice", null,locale));
        jobjTemp.put("dataIndex", "configuredUnitPrice");
        jobjTemp.put("width", 125);
        jobjTemp.put("pdfwidth", 125);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "actualVariancePercentage");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.invoice.actualvariance", null,locale));
        jobjTemp.put("dataIndex", "actualVariancePercentage");
        jobjTemp.put("width", 125);
        jobjTemp.put("pdfwidth", 125);
        jarrColumns.put(jobjTemp);
    }
    
    public void getPriceVarianceReportForSalesInvoice(HashMap<String, Object> requestParams, JSONArray dataJArr) {
        try {
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            KwlReturnObject result = accInvoiceDAOobj.getSalesByCustomer(requestParams);
            String companyid = (String) requestParams.get("companyid");
            List<Object[]> list = result.getEntityList();

            for (Object[] objArr : list) {
                String invoiceID = (String) objArr[0];
                KwlReturnObject invResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceID);
                Invoice invoice = (Invoice) invResult.getEntityList().get(0);

                String invDetailID = !StringUtil.isNullOrEmpty(objArr[3].toString()) ? objArr[3].toString() : "";
                KwlReturnObject invDetailObjResult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), invDetailID);
                InvoiceDetail invoiceDetail = (InvoiceDetail) invDetailObjResult.getEntityList().get(0);

                JSONObject obj = new JSONObject();
                obj.put("billNo", invoice.getInvoiceNumber());
//                obj.put("billDate", (invoice.getJournalEntry() != null) ? df.format(invoice.getJournalEntry().getEntryDate()) : "");
                obj.put("billDate", (invoice.getCreationDate() != null) ? df.format(invoice.getCreationDate()) : "");
                obj.put("custName", (invoice.getCustomer() != null) ? invoice.getCustomer().getName() : "");
                obj.put("salesPersonName", (invoice.getMasterSalesPerson() != null) ? invoice.getMasterSalesPerson().getValue() : "");
                obj.put("currencyid", (invoice.getCurrency() != null) ? invoice.getCurrency().getCurrencyID() : "");
                obj.put("currencysymbol", (invoice.getCurrency() != null) ? invoice.getCurrency().getSymbol() : "");
                obj.put("currencycode", (invoice.getCurrency() != null) ? invoice.getCurrency().getCurrencyCode() : "");
                obj.put("productName", invoiceDetail.getInventory().getProduct().getName());
                obj.put("pid", invoiceDetail.getInventory().getProduct().getProductid());
                obj.put("productDesc", StringUtil.isNullOrEmpty(invoiceDetail.getDescription()) ? "" : invoiceDetail.getDescription());
                double quantity = invoiceDetail.getInventory().getQuantity();
                obj.put("quantity", authHandler.formattedQuantity(quantity, companyid));
                obj.put("unitPrice", authHandler.getFormattedUnitPrice(invoiceDetail.getRate(), companyid));
                
                HashMap<String, Object> productCategoryParams = new HashMap<>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("productID.ID");
                filter_params.add(invoiceDetail.getInventory().getProduct().getID());
                productCategoryParams.put("filter_names", filter_names);
                productCategoryParams.put("filter_params", filter_params);
                KwlReturnObject productCategoryResultObj = accProductObj.getProductCategoryDetails(productCategoryParams);
                List<ProductCategoryMapping> productCategoryList = productCategoryResultObj.getEntityList();
                
                if (!productCategoryList.isEmpty() && productCategoryList.size() == 1) {
                    ProductCategoryMapping productCategoryMapping = productCategoryList.get(0);
                    double configuredVariance = productCategoryMapping.getProductCategory().getVariancePercentage();
                    configuredVariance = authHandler.round(configuredVariance, companyid);
                    
                    HashMap<String, Object> priceParams = new HashMap<>();
                    priceParams.put(Constants.companyKey, requestParams.get(Constants.companyKey));
                    priceParams.put(Constants.df, requestParams.get(Constants.df));
                    priceParams.put(Constants.productid, invoiceDetail.getInventory().getProduct().getID());
                    priceParams.put(Constants.currencyKey, invoice.getCurrency().getCurrencyID());
                    priceParams.put("externalCurrencyRate", invoice.getExternalCurrencyRate());
//                    priceParams.put("transactionDate", invoice.getJournalEntry().getEntryDate());
                    priceParams.put("transactionDate", invoice.getCreationDate());
                    priceParams.put("quantity", (int) quantity);
                    priceParams.put("affecteduser", invoice.getCustomer().getID());
                    double configuredPrice = getConfiguredPriceOfProduct(priceParams);
                    configuredPrice = authHandler.round(configuredPrice, companyid);
                    
                    if (configuredPrice != 0) {
                        double actualVariance = ((configuredPrice - invoiceDetail.getRate()) / configuredPrice) * 100;
                        actualVariance = authHandler.round(actualVariance, companyid);
                        if (actualVariance > configuredVariance) {
                            obj.put("configuredVariancePercentage", authHandler.formattingDecimalForAmount(configuredVariance, companyid));
                            obj.put("configuredUnitPrice", authHandler.getFormattedUnitPrice(configuredPrice, companyid));
                            obj.put("actualVariancePercentage", authHandler.formattingDecimalForAmount(actualVariance, companyid));
                        } else {
                            // do not show record as variance % of record is less than configured variance %
                            continue;
                        }
                    } else {
                        // do not show record as configured price is zero
                        continue;
                    }
                } else {
                    // do not show record as having multiple categories
                    continue;
                }

                dataJArr.put(obj);
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void getPriceVarianceReportForSalesOrder(HashMap<String, Object> requestParams, JSONArray dataJArr) {
        try {
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            KwlReturnObject result = accSalesOrderDAOobj.getSalesOrderDetailsForPriceVariance(requestParams);
            String companyid = (String) requestParams.get("companyid");
            List<Object[]> list = result.getEntityList();

            for (Object[] objArr : list) {
                String soID = (String) objArr[0];
                KwlReturnObject soResult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soID);
                SalesOrder salesOrder = (SalesOrder) soResult.getEntityList().get(0);

                String soDetailID = !StringUtil.isNullOrEmpty(objArr[1].toString()) ? objArr[1].toString() : "";
                KwlReturnObject soDetailObjResult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), soDetailID);
                SalesOrderDetail salesOrderDetail = (SalesOrderDetail) soDetailObjResult.getEntityList().get(0);
                
                JSONObject obj = new JSONObject();
                obj.put("billNo", salesOrder.getSalesOrderNumber());
                obj.put("billDate", df.format(salesOrder.getOrderDate()));
                obj.put("custName", (salesOrder.getCustomer() != null) ? salesOrder.getCustomer().getName() : "");
                obj.put("salesPersonName", (salesOrder.getSalesperson() != null) ? salesOrder.getSalesperson().getValue() : "");
                obj.put("currencyid", (salesOrder.getCurrency() != null) ? salesOrder.getCurrency().getCurrencyID() : "");
                obj.put("currencysymbol", (salesOrder.getCurrency() != null) ? salesOrder.getCurrency().getSymbol() : "");
                obj.put("currencycode", (salesOrder.getCurrency() != null) ? salesOrder.getCurrency().getCurrencyCode() : "");
                obj.put("productName", (salesOrderDetail.getProduct() != null) ? salesOrderDetail.getProduct().getName() : "");
                obj.put("pid", (salesOrderDetail.getProduct() != null) ? salesOrderDetail.getProduct().getProductid() : "");
                obj.put("productDesc", (salesOrderDetail.getProduct() != null) ? salesOrderDetail.getProduct().getDescription() : "");
                double quantity = salesOrderDetail.getQuantity();
                obj.put("quantity", authHandler.formattedQuantity(quantity, companyid));
                obj.put("unitPrice", authHandler.getFormattedUnitPrice(salesOrderDetail.getRate(), companyid));
                
                HashMap<String, Object> productCategoryParams = new HashMap<>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("productID.ID");
                filter_params.add(salesOrderDetail.getProduct().getID());
                productCategoryParams.put("filter_names", filter_names);
                productCategoryParams.put("filter_params", filter_params);
                KwlReturnObject productCategoryResultObj = accProductObj.getProductCategoryDetails(productCategoryParams);
                List<ProductCategoryMapping> productCategoryList = productCategoryResultObj.getEntityList();
                
                if (!productCategoryList.isEmpty() && productCategoryList.size() == 1) {
                    ProductCategoryMapping productCategoryMapping = productCategoryList.get(0);
                    double configuredVariance = productCategoryMapping.getProductCategory().getVariancePercentage();
                    configuredVariance = authHandler.round(configuredVariance, companyid);
                    
                    HashMap<String, Object> priceParams = new HashMap<>();
                    priceParams.put(Constants.companyKey, requestParams.get(Constants.companyKey));
                    priceParams.put(Constants.df, requestParams.get(Constants.df));
                    priceParams.put(Constants.productid, salesOrderDetail.getProduct().getID());
                    priceParams.put(Constants.currencyKey, salesOrder.getCurrency().getCurrencyID());
                    priceParams.put("externalCurrencyRate", salesOrder.getExternalCurrencyRate());
                    priceParams.put("transactionDate", salesOrder.getOrderDate());
                    priceParams.put("quantity", (int) quantity);
                    priceParams.put("affecteduser", salesOrder.getCustomer().getID());
                    double configuredPrice = getConfiguredPriceOfProduct(priceParams);
                    configuredPrice = authHandler.round(configuredPrice, companyid);
                    if (configuredPrice != 0) {
                        double actualVariance = ((configuredPrice - salesOrderDetail.getRate()) / configuredPrice) * 100;
                        actualVariance = authHandler.round(actualVariance, companyid);
                        if (actualVariance > configuredVariance) {
                            obj.put("configuredVariancePercentage", authHandler.formattingDecimalForAmount(configuredVariance, companyid));
                            obj.put("configuredUnitPrice", authHandler.getFormattedUnitPrice(configuredPrice, companyid));
                            obj.put("actualVariancePercentage", authHandler.formattingDecimalForAmount(actualVariance, companyid));
                        } else {
                            // do not show record as variance % of record is less than configured variance %
                            continue;
                        }
                    } else {
                        // do not show record as configured price is zero
                        continue;
                    }
                } else {
                    // do not show record as having multiple categories
                    continue;
                }

                dataJArr.put(obj);
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public double getConfiguredPriceOfProduct(HashMap<String, Object> requestParams) throws ServiceException, ParseException {
        double configuredPrice = 0;
        Date transactionDate = (Date) requestParams.get("transactionDate");
        String companyID = (String) requestParams.get(Constants.companyKey);
        String currencyID = (String) requestParams.get(Constants.currencyKey);
        boolean carryin = false; // false for sales side transaction
        String productID = (String) requestParams.get(Constants.productid);
        boolean isPriceFromBand = false;
        boolean isPriceFromVolumeDiscount = false;
        boolean isPriceFromUseDiscount = false;
        String discountType = "";
        double discountValue = 0;
        boolean isSpecialRateExist = false;
        KwlReturnObject prefResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) prefResult.getEntityList().get(0);
        KwlReturnObject result = null;

        // for get price from pricing band
        if (extraCompanyPreferences != null && !carryin && extraCompanyPreferences.isProductPricingOnBandsForSales()) {
            // if Price List band with special rate for sale activated then first check for special rate
            if (!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales() && extraCompanyPreferences.isBandsWithSpecialRateForSales()) {
                if (extraCompanyPreferences != null && extraCompanyPreferences.isProductPriceinMultipleCurrency()) {
                    KwlReturnObject specialRateResult = accProductObj.getSpecialRateofProduct(productID, carryin, transactionDate, (String) requestParams.get("affecteduser"), currencyID);
                    if (specialRateResult.getEntityList() != null && !specialRateResult.getEntityList().isEmpty() && specialRateResult.getEntityList().get(0) != null) {
                        isSpecialRateExist = true;
                        double specialRate = (Double) specialRateResult.getEntityList().get(0);
                        configuredPrice = specialRate;
                    }
                } else {
                    KwlReturnObject specialRateResult = accProductObj.getSpecialRateofProduct(productID, carryin, transactionDate, (String) requestParams.get("affecteduser"), "");
                    if (specialRateResult.getEntityList() != null && !specialRateResult.getEntityList().isEmpty() && specialRateResult.getEntityList().get(0) != null) {
                        isSpecialRateExist = true;
                        double specialRate = (Double) specialRateResult.getEntityList().get(0);
                        configuredPrice = specialRate;
                    }
                }
            }
            
            if (!isSpecialRateExist) {
                String pricingBandMasterID = "";
                KwlReturnObject affectedUserResult = null;

                // to get price from price list volume discount - use discount
                HashMap<String, Object> pricingDiscountRequestParams = new HashMap<String, Object>();
                pricingDiscountRequestParams.put("isPricePolicyUseDiscount", true);
                pricingDiscountRequestParams.put("productID", productID);
                pricingDiscountRequestParams.put("isPurchase", carryin);
                pricingDiscountRequestParams.put("applicableDate", transactionDate);
                pricingDiscountRequestParams.put("companyID", companyID);
                pricingDiscountRequestParams.put("currencyID", currencyID);
                pricingDiscountRequestParams.put("quantity", requestParams.get("quantity"));

                result = accProductObj.getProductPriceFromPriceListVolumeDiscount(pricingDiscountRequestParams);
                if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    Object[] rowObj = (Object[]) result.getEntityList().get(0);

                    KwlReturnObject detailResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), (String) rowObj[5]);
                    PricingBandMasterDetail pricingBandMasterDetail = detailResult != null ? (PricingBandMasterDetail) detailResult.getEntityList().get(0) : null;

                    discountType = pricingBandMasterDetail.getDiscountType();
                    discountValue = pricingBandMasterDetail.getDiscountValue();
                    isPriceFromUseDiscount = true;
                    isPriceFromBand = true;
                    isPriceFromVolumeDiscount = false;
                } else {
                    // to get price from price list volume discount
                    pricingDiscountRequestParams.put("currencyID", currencyID);
                    pricingDiscountRequestParams.put("isPricePolicyUseDiscount", false);

                    result = accProductObj.getProductPriceFromPriceListVolumeDiscount(pricingDiscountRequestParams);
                    if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        Object[] row = (Object[]) result.getEntityList().get(0);

                        if (row == null) {
                            configuredPrice = 0;
                        } else {
                            isPriceFromVolumeDiscount = true;
                            configuredPrice = (Double) row[0];
                        }
                    }
                }

                if (!carryin) {
                    affectedUserResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) requestParams.get("affecteduser"));
                    Customer customer = affectedUserResult != null ? (Customer) affectedUserResult.getEntityList().get(0) : null;
                    if (customer != null && customer.getPricingBandMaster() != null) {
                        pricingBandMasterID = customer.getPricingBandMaster().getID();
                        isPriceFromBand = true;
                    } else {
                        isPriceFromBand = false;
                    }
                }

                if (isPriceFromBand && !isPriceFromVolumeDiscount) {
                    HashMap<String, Object> pricingBandRequestParams = new HashMap<String, Object>();
                    pricingBandRequestParams.put("productID", productID);
                    pricingBandRequestParams.put("isPurchase", carryin);
                    pricingBandRequestParams.put("pricingBandMasterID", pricingBandMasterID);
                    pricingBandRequestParams.put("applicableDate", transactionDate);
                    pricingBandRequestParams.put("currencyID", currencyID);
                    pricingBandRequestParams.put("companyID", companyID);

                    result = accProductObj.getProductPriceFromPricingBand(pricingBandRequestParams);
                    if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        isPriceFromBand = true;
                        Object[] row = (Object[]) result.getEntityList().get(0);

                        if (row == null) {
                            configuredPrice = 0;
                        } else {
                            double price = (Double) row[0];
                            if (isPriceFromUseDiscount && requestParams.containsKey("quantity") && requestParams.get("quantity") != null) {
                                if (discountType.equalsIgnoreCase("0")) { // for Flat discount
                                    price = price - discountValue;
                                } else if (discountType.equalsIgnoreCase("1")) { // for Percent discount
                                    price = price - ((price * discountValue) / 100);
                                }
                            }
                            configuredPrice = (price >= 0) ? price : 0;
                        }
                    } else {
                        isPriceFromBand = false;
                    }
                }
            }
        }

        // if pricing band is not activated or band price for product is not available and (if special rate not exist)  then check from price list
        if ((((carryin && extraCompanyPreferences.isProductPricingOnBands()) || (!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales())) && !isPriceFromBand && !isPriceFromVolumeDiscount && (currencyID.toString().equalsIgnoreCase(currencyID) || extraCompanyPreferences.isProductPriceinMultipleCurrency()) || (carryin && !extraCompanyPreferences.isProductPricingOnBands() || !carryin && !extraCompanyPreferences.isProductPricingOnBandsForSales())) && !isSpecialRateExist) {
            
            if (extraCompanyPreferences != null && extraCompanyPreferences.isProductPriceinMultipleCurrency()) {
                KwlReturnObject rateResult = accProductObj.getProductPrice(productID, carryin, transactionDate, (String) requestParams.get("affecteduser"), currencyID);
                configuredPrice = (rateResult.getEntityList().get(0) != null) ? (Double) rateResult.getEntityList().get(0) : 0.0;
            } else {
                KwlReturnObject rateResult = accProductObj.getProductPrice(productID, carryin, transactionDate, (String) requestParams.get("affecteduser"), "");
                configuredPrice = (rateResult.getEntityList().get(0) != null) ? (Double) rateResult.getEntityList().get(0) : 0.0;
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, configuredPrice, currencyID, transactionDate, (Double) requestParams.get("externalCurrencyRate"));
                configuredPrice = (Double) bAmt.getEntityList().get(0);
            }
            
            if (isPriceFromUseDiscount && requestParams.containsKey("quantity") && requestParams.get("quantity") != null) {
                if (discountType.equalsIgnoreCase("0")) { // for Flat discount
                    configuredPrice = configuredPrice - discountValue;
                } else if (discountType.equalsIgnoreCase("1")) { // for Percent discount
                    configuredPrice = configuredPrice - ((configuredPrice * discountValue) / 100);
                }
            }
            configuredPrice = (configuredPrice >= 0) ? configuredPrice : 0;
        } else if ((((carryin && extraCompanyPreferences.isProductPricingOnBands()) || (!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales())) && !isPriceFromBand && !isPriceFromVolumeDiscount && !isPriceFromUseDiscount) && !isSpecialRateExist) {
            configuredPrice = 0;
        }
        
        return configuredPrice;
    }
    
    public JSONArray getDODetailsItemJSON(JSONObject requestObj, String SOID, HashMap<String, Object> paramMap) {
        JSONArray jArr = new JSONArray();
        String Invref = "";
        String SOref = "";
        String allTerms="",shipmentTrackingNo = "";
        StringBuilder appendtermString = new StringBuilder();
        String QouteRef = "", linkedreferenecenumber = "", soreatedby = "", createdby = "", poRefNo = "", updatedby = "",allLineLevelTax="", allLineLevelTaxBasic = "", allLineLevelTaxAmount = "";
        Map<String,Double> lineLevelTaxNames = new HashMap<String,Double>();
        int countOfBatches = 0;
        /*Varialble to display Total no of batches*/
        /*
            * Declaration of Variables to fetch Line level taxes and its information like
            * Tax Code, Tax Amount etc.
            */ 
        Set<String> lineLevelTaxesGST = new HashSet<String>();
        Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
        Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
        Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();
        Date SalesOrderDate=null,InvoiceDate=null;
        int creditterm = 0;
        double totaltax = 0,totalAmount = 0,subtotal=0,totalQuantity = 0,totalDeliveredQuantity=0,Amountwithoutterm=0, subTotalWithDiscount = 0, totalGrossWeight = 0;
        String compAccPrefBillAddress="",compAccPrefShipAddress="",remitPaymentTo="",CR_SalesPerson="",globallevelcustomfields="",globalleveldimensions="", uomForTotalQuantity="";
        double totalDiscount = 0,taxPercent=0;
        Tax mainTax=null,leaseMainTax=null;
        String billAddr="",shipAddr="",crmemo="",requestType="",mainTaxName = "",IFSCCode = "", accountNumber = "", customerTitle = "";
        PdfTemplateConfig config=null; 
        Date entryDate = null,crfromdate=null,crtodate=null;
        boolean isLocationForProduct = false,isbatchforproduct=false,isserialforproduct=false,isWarehouseForProduct=false;
        int quantitydigitafterdecimal=2,amountdigitafterdecimal=2,unitpricedigitafterdecimal=2;
        Set<String> requestset= new HashSet<String>();
        JSONObject summaryData = new JSONObject();
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            HashMap<String, Integer> ProductLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.productLevelCustomFieldMap);
            String companyid = requestObj.optString(Constants.companyKey);
            int moduleid = requestObj.optInt("moduleid");
            boolean isEdit = requestObj.optBoolean("isEdit", false);
            boolean srflag = requestObj.optBoolean("srflag", false);
            boolean linkingFlag = requestObj.optBoolean("linkingFlag", false);
            DateFormat df1 = authHandler.getDateOnlyFormat();
            DateFormat df = authHandler.getUserDateFormatterJson(requestObj);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestObj);
            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), SOID);
            DeliveryOrder deliveryOrder = (DeliveryOrder) objItr.getEntityList().get(0);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestObj.optString(Constants.globalCurrencyKey));
            
            //document currency
            if (deliveryOrder != null && deliveryOrder.getCurrency() != null && !StringUtil.isNullOrEmpty(deliveryOrder.getCurrency().getCurrencyID())) {
                summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, deliveryOrder.getCurrency().getCurrencyID());
            }
            
            double externalcurrency = deliveryOrder.getExternalCurrencyRate();
            double revExchangeRate = 1.0;
            if (externalcurrency != 0) {
                revExchangeRate = 1 / externalcurrency;
            }
            /**
             * get customer title (Mr./Mrs.)
             */           
            customerTitle = deliveryOrder.getCustomer().getTitle();
            if(!StringUtil.isNullOrEmpty(customerTitle)){
                KwlReturnObject masterItemResult = kwlCommonTablesDAOObj.getObject(MasterItem.class.getName(), customerTitle);
                MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                customerTitle = masterItem.getValue();
            }
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            filter_names.add("deliveryOrder.ID");
            filter_params.add(deliveryOrder.getID());
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);
            KwlReturnObject podresult = accInvoiceDAOobj.getDeliveryOrderDetails(soRequestParams);
            Iterator itr = podresult.getEntityList().iterator();
            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[1] != null) {//getting quantity in decimal value from companyaccpreferences
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                }
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
                if (decimalcontact[3] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalcontact[3].toString());
                }
            }
         
            boolean isConsignment = requestObj.optBoolean("isConsignment", false);
            if(deliveryOrder.getCreatedby()!=null){
                createdby=deliveryOrder.getCreatedby().getFullName();         
            }
            updatedby = deliveryOrder.getModifiedby() != null ? deliveryOrder.getModifiedby().getFullName() : "";
            //getting Recommended Retail Price
            List<PricingBandMasterDetail> list = CommonFunctions.getRRPFieldsForAllModulesLineItem(requestObj, accountingHandlerDAOobj, accMasterItemsDAOobj);
            KwlReturnObject pref = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preference = (CompanyAccountPreferences) pref.getEntityList().get(0);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            int countryid = 0;
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            if( extraCompanyPreferences.getCompany()!= null && extraCompanyPreferences.getCompany().getCountry() != null ){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            if( countryid == Constants.indian_country_id && extraCompanyPreferences.getBankId()!= null ){
                KwlReturnObject accobj = accountingHandlerDAOobj.getObject(Account.class.getName(),  extraCompanyPreferences.getBankId());
                Account account= (Account) accobj.getEntityList().get(0);
                if(account != null && account.getIfsccode() != null){
                       IFSCCode = account.getIfsccode();
                }
                if(account != null && account.getAccountCode() != null ){
                    accountNumber = account.getAccountCode();
                }
            }
            // get Shipment Tracking No if UPS flow is on
            boolean isUpsIntegration = extraCompanyPreferences.isUpsIntegration();
            Set<String> deliveryOrdeDetailIDs = new TreeSet<String>();
            if (isUpsIntegration) {
                Set<DeliveryOrderDetail> doRows = deliveryOrder.getRows();
                if (doRows != null && !doRows.isEmpty()) {
                    for (DeliveryOrderDetail temp : doRows) {
                        String deliveryOrdeDetailID = temp.getID();
                        if (!StringUtil.isNullOrEmpty(deliveryOrdeDetailID)) {
                            deliveryOrdeDetailIDs.add(deliveryOrdeDetailID);
                        }
                    }
                }
                StringBuilder upsTrackingNumbers = new StringBuilder("");
                Set<String> upsTrackingNumbersSet = new TreeSet<String>();
                upsTrackingNumbersSet = accInvoiceServiceDAO.getUPSTrackingNumberFromDoDetails(deliveryOrdeDetailIDs);
                if (!upsTrackingNumbersSet.isEmpty()) {
                    String upsTrackingNumbersStr = upsTrackingNumbersSet.toString();
                    upsTrackingNumbers.append(upsTrackingNumbersStr.substring(1, (upsTrackingNumbersStr.length() - 1)));
                }
                shipmentTrackingNo = upsTrackingNumbers.toString().trim();
                shipmentTrackingNo = shipmentTrackingNo.replaceAll(",", "!##");// replace comma with !## for value seperator funtionality
            }
            
            //getting main company posttext
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, moduleid);
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            int rowcnt=0;
            Set<String> uniqueProductTaxList = new HashSet<String>();
            JSONObject detailsTableData = new JSONObject();
            JSONArray gstTaxSummaryDetailsTableDataArr = new JSONArray();
            while (itr.hasNext()) {
                String proddesc="",rowTaxName = "",discountname="";            
                double rowTaxPercent = 0, sodQty = 0, sidQty = 0;
                double rowamountwithouttax=0,rowamountwithtax = 0,rowTaxAmt=0,quantity = 0,deliveredquantity=0,rate = 0,rowdiscountvalue=0;
                rowcnt++;
                DeliveryOrderDetail row = (DeliveryOrderDetail) itr.next();

                JSONObject obj = new JSONObject();
                JSONObject gstTaxSummaryDetailsTableData = new JSONObject();
                Product prod = row.getProduct();
                obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr No
                obj.put(CustomDesignerConstants.ProductName, prod.getName().replaceAll("\n", "<br>"));// productname
                obj.put("type", prod.getProducttype() == null ? "" : prod.getProducttype().getName());
                proddesc=StringUtil.isNullOrEmpty(row.getDescription()) ? (StringUtil.isNullOrEmpty(row.getProduct().getDescription())?"":row.getProduct().getDescription()) : row.getDescription();
                proddesc =StringUtil.DecodeText(proddesc);
                obj.put(CustomDesignerConstants.ProductDescription,proddesc.replaceAll("\n", "<br>"));//Product Description
                obj.put(CustomDesignerConstants.AdditionalDescription, !StringUtil.isNullOrEmpty(prod.getAdditionalDesc()) ? prod.getAdditionalDesc().replaceAll(Constants.REGEX_LINE_BREAK, "<br>") :"");  //product Addtional description ERP-39061
                obj.put(CustomDesignerConstants.ProductBarcode, prod.getBarcode() == null ? "" : prod.getBarcode());//Product Bar Code
                rate=deliveryOrder.isGstIncluded() ? row.getRateincludegst() : row.getRate();
                obj.put(CustomDesignerConstants.Rate,  authHandler.formattingDecimalForUnitPrice(row.getRate(), companyid));// Rate
                obj.put(CustomDesignerConstants.RATEINCLUDINGGST,  authHandler.formattingDecimalForUnitPrice(row.getRateincludegst(), companyid));// Rate incluging GST
                String uom = row.getUom()==null?(row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA()):row.getUom().getNameEmptyforNA();
                uomForTotalQuantity = uom;
                quantity = row.getActualQuantity();//Actual quantity
                deliveredquantity= row.getDeliveredQuantity();//Delivered Quantity
                totalQuantity += quantity;
                totalDeliveredQuantity += deliveredquantity;
                
                obj.put(CustomDesignerConstants.DO_ActualQuantityWithUOM, authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom);//Actual Quantity withUOM
                obj.put(CustomDesignerConstants.DO_DeliveredQuantityWithUOM, authHandler.formattingDecimalForQuantity(deliveredquantity, companyid) + " " + uom); // Delivered Quantity with UOM
                obj.put(CustomDesignerConstants.DO_ActualQuantity, authHandler.formattingDecimalForQuantity(quantity, companyid));//Actual Quantity
                obj.put(CustomDesignerConstants.DO_DeliveredQuantity, authHandler.formattingDecimalForQuantity(deliveredquantity, companyid));// Delivered Quantity
                obj.put(CustomDesignerConstants.DO_UOM, uom);
                obj.put(CustomDesignerConstants.PRODUCTNETWEIGHT, authHandler.formattingDecimalForAmount(row.getProduct().getProductweight(), companyid));//Product Weight
                obj.put(CustomDesignerConstants.PartNumber, StringUtil.isNullOrEmpty(prod.getCoilcraft()) ? "" : prod.getCoilcraft()); //Part Number
                obj.put(CustomDesignerConstants.SupplierPartNumber, StringUtil.isNullOrEmpty(prod.getSupplier()) ? "" : prod.getSupplier());
                obj.put(CustomDesignerConstants.CustomerPartNumber, StringUtil.isNullOrEmpty(prod.getInterplant()) ? "" : prod.getInterplant());
                
                for (PricingBandMasterDetail pricingBandMasterDetailObj : list) {
                    if (pricingBandMasterDetailObj.getProduct().equals(prod.getID())) {
                            obj.put(CustomDesignerConstants.DO_RRP, authHandler.formattingDecimalForAmount(pricingBandMasterDetailObj.getSalesPrice(), companyid));//RRP
                    }
                }
                KwlReturnObject productCategories = null;
                productCategories = accProductObj.getProductCategoryForDetailsReport(prod.getID());
                List productCategoryList = productCategories.getEntityList();
                String cateogry = "";
                Iterator catIte = productCategoryList.iterator();
                while (catIte.hasNext()) {
                    ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                    String categoryName  = pcm.getProductCategory()!=null?(!StringUtil.isNullOrEmpty(pcm.getProductCategory().getValue().toString())?pcm.getProductCategory().getValue().toString():""):"";
                    cateogry += categoryName + " ";
                }
                if (StringUtil.isNullOrEmpty(cateogry)) {
                    cateogry = "None";
                }
                obj.put("productCategory", cateogry);

                obj.put(CustomDesignerConstants.DO_ProductCode, row.getProduct().getProductid()==null?"":row.getProduct().getProductid());
//                obj.put(CustomDesignerConstants.DO_Loc, row.getProduct().getLocation()==null?"":row.getProduct().getLocation().getName());
                obj.put(CustomDesignerConstants.DO_Remarks,row.getRemark()==null?"":row.getRemark().replaceAll("\n", "<br>"));//get Remarks of Product Grid
                obj.put(CustomDesignerConstants.DO_SerialNumber, (row.getPartno()!=null) ? row.getPartno() : "" );//getSerialnumber of ProductGrid
                obj.put(CustomDesignerConstants.HSCode, (row.getProduct().getHSCode()!=null) ? row.getProduct().getHSCode().replaceAll("\n", "<br>") : "" );//get HSCode of ProductGrid
                String baseUrl = URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                /*
                 * Following code is to check whether the image is predent for product or not. 
                 * If Image is not present sent s.gif instead of product id
                 */
                String fileName = null;
                fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+prod.getID() + ".png";
                File file = new File(fileName);
                FileInputStream in = null; 
                String filePathString = "";
                try {
                    in = new FileInputStream(file);
                } catch(java.io.FileNotFoundException ex) {
                    //catched exception if file not found, and to continue for rest of the products
                   filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                if(in != null){
                    filePathString = baseUrl + "productimage?fname=" + prod.getID() + ".png&isDocumentDesignerPrint=true";
                } else{
                    filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                obj.put(CustomDesignerConstants.imageTag,filePathString);
             /*To show linked CI or SO in LIne Items*/
                
                if (row.getCidetails() != null) {
                    sidQty = row.getCidetails().getInventory().getQuantity();
                    obj.put(CustomDesignerConstants.DO_CISONo, row.getCidetails().getInvoice().getInvoiceNumber());
                } else if (row.getSodetails() != null) {
                    obj.put(CustomDesignerConstants.DO_CISONo, row.getSodetails().getSalesOrder().getSalesOrderNumber());
                    KwlReturnObject objItrsalesorder = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), row.getSodetails().getSalesOrder().getID());
                    linkedreferenecenumber=row.getSodetails().getSalesOrder().getID();
                    sodQty = row.getSodetails().getQuantity();
                    SalesOrder salesOrder = (SalesOrder) objItrsalesorder.getEntityList().get(0);
                    crmemo=salesOrder.getMemo().replaceAll("\n", "<br>");
                    crfromdate=salesOrder.getFromdate();
                    crtodate=salesOrder.getTodate();
                    if(salesOrder.getCreatedby()!=null){
                       soreatedby=salesOrder.getCreatedby().getFullName();
                    }
                    
                    if (salesOrder.getSalesperson() != null) {
                        CR_SalesPerson =StringUtil.isNullOrEmpty(salesOrder.getSalesperson().getValue())?"":salesOrder.getSalesperson().getValue();
                    }
                    
                    obj.put("linkedreferenecenumber",linkedreferenecenumber);

                } else {
                    obj.put(CustomDesignerConstants.DO_CISONo, "");
                }
                obj.put(CustomDesignerConstants.OrderQuantity, sodQty);
                obj.put(CustomDesignerConstants.SI_ORDER_QUANTITY, sidQty);
                obj.put("currencysymbol",deliveryOrder.getCurrency().getSymbol());
                obj.put("currencycode",deliveryOrder.getCurrency().getCurrencyCode());
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                    KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), row.getProduct().getID());
                    Product product = (Product) prodresult.getEntityList().get(0);
                    isLocationForProduct = product.isIslocationforproduct();
                    isbatchforproduct = product.isIsBatchForProduct();
                    isserialforproduct = product.isIsSerialForProduct();
                    isWarehouseForProduct = product.isIswarehouseforproduct();

                }
                
                rowamountwithouttax = rate * deliveredquantity;
                rowdiscountvalue = (row.getDiscountispercent() == 1) ? rowamountwithouttax * row.getDiscount() / 100 : row.getDiscount();
                
                String lineLevelTax = "";
                String lineLevelTaxPercent = "";
                String lineLevelTaxAmount = "";
                subTotalWithDiscount = authHandler.round((rowamountwithouttax - rowdiscountvalue), companyid);
                if(deliveryOrder.isGstIncluded()){
                    rowamountwithouttax = rowamountwithouttax-rowdiscountvalue-row.getRowTaxAmount();
                }
                /*
                 * The variable to store total of line level taxes
                 */
                double lineLevelTaxAmountTotal = 0;
                if(extraCompanyPreferences.isIsNewGST()){ // for New gst check 
                    HashMap<String, Object> InvoiceDetailParams = new HashMap<String, Object>();
                    JSONArray doDetailTermsMapList = accInvoiceDAOobj.getDODetailsTermMap(row.getID());
                    obj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.CESSPERCENT, 0);
                    obj.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.TAXABLE_VALUE, subTotalWithDiscount);
                    for (int i= 0; i < doDetailTermsMapList.length(); i++) {
                        JSONObject doJson = doDetailTermsMapList.getJSONObject(i);
                        if (doJson.optString("term","").contains(CustomDesignerConstants.CGST)) {
                            obj.put(CustomDesignerConstants.CGSTPERCENT, doJson.optString("termpercentage","0.0"));
                            obj.put(CustomDesignerConstants.CGSTAMOUNT, Double.parseDouble(doJson.optString("termamount","0.0")));
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, doJson.optString("termpercentage","0.0"));
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, Double.parseDouble(doJson.optString("termamount","0.0")));
                        } else if (doJson.optString("term","").contains(CustomDesignerConstants.IGST)) {
                            obj.put(CustomDesignerConstants.IGSTPERCENT, doJson.optString("termpercentage","0.0"));
                            obj.put(CustomDesignerConstants.IGSTAMOUNT, Double.parseDouble(doJson.optString("termamount","0.0")));
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, doJson.optString("termpercentage","0.0"));
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, Double.parseDouble(doJson.optString("termamount","0.0")));
                        } else if (doJson.optString("term","").contains(CustomDesignerConstants.SGST)) {
                            obj.put(CustomDesignerConstants.SGSTPERCENT, doJson.optString("termpercentage","0.0"));
                            obj.put(CustomDesignerConstants.SGSTAMOUNT, Double.parseDouble(doJson.optString("termamount","0.0")));
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, doJson.optString("termpercentage","0.0"));
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, Double.parseDouble(doJson.optString("termamount","0.0")));
                        } else if (doJson.optString("term","").contains(CustomDesignerConstants.UTGST)) {
                            obj.put(CustomDesignerConstants.UTGSTPERCENT, doJson.optString("termpercentage","0.0"));
                            obj.put(CustomDesignerConstants.UTGSTAMOUNT, Double.parseDouble(doJson.optString("termamount","0.0")));
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, doJson.optString("termpercentage","0.0"));
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, Double.parseDouble(doJson.optString("termamount","0.0")));
                        } else if (doJson.optString("term","").contains(CustomDesignerConstants.CESS)) {
                            obj.put(CustomDesignerConstants.CESSPERCENT, doJson.optString("termpercentage","0.0"));
                            obj.put(CustomDesignerConstants.CESSAMOUNT, Double.parseDouble(doJson.optString("termamount","0.0")));
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, doJson.optString("termpercentage","0.0"));
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, Double.parseDouble(doJson.optString("termamount","0.0")));
                        }
                        lineLevelTax += doJson.optString("term","");
                        lineLevelTax += "!## ";
                        lineLevelTaxPercent += authHandler.formattingDecimalForAmount(Double.parseDouble(doJson.optString("termpercentage","0.0")), companyid);
                        lineLevelTaxPercent += "!## ";
                        lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(Double.parseDouble(doJson.optString("termamount","0.0")), amountdigitafterdecimal,countryid);
                        lineLevelTaxAmount += "!## ";
                        /*
                         * calculating total of line level taxes
                         */
                        lineLevelTaxAmountTotal += Double.parseDouble(doJson.optString("termamount","0.0"));
                        if(lineLevelTaxNames.containsKey(doJson.optString("term","")) && lineLevelTaxNames.get(doJson.optString("term",""))!=null){
                            double value = lineLevelTaxNames.get(doJson.optString("term",""));
                            lineLevelTaxNames.put(doJson.optString("term",""),doJson.optDouble("termamount",0.0)+value );
                            
                        } else{
                            lineLevelTaxNames.put(doJson.optString("term",""),doJson.optDouble("termamount",0.0));
                        }
                    }
                    if(!StringUtil.isNullOrEmpty(lineLevelTax)){
                        lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length()-4);
                    }
                    if(!StringUtil.isNullOrEmpty(lineLevelTaxPercent)){
                        lineLevelTaxPercent=lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length()-4);
                    }
                    if(!StringUtil.isNullOrEmpty(lineLevelTaxAmount)){
                        lineLevelTaxAmount= lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length()-4);
                    }
                    //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                    ExportRecordHandler.setHsnSacProductDimensionField(row.getProduct(), obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.HSN_SAC_CODE, obj.optString(CustomDesignerConstants.HSN_SAC_CODE, ""));
                    /*
                     * putting subtotal+tax
                     */
                    if(deliveryOrder.isGstIncluded()){
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round(rowamountwithouttax, companyid) + authHandler.round(lineLevelTaxAmountTotal, companyid));
                    } else{
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid) + authHandler.round(lineLevelTaxAmountTotal, companyid));
                    }

                    gstTaxSummaryDetailsTableDataArr.put(gstTaxSummaryDetailsTableData);
                } else {
                    /*
                    * Fetching distinct taxes used at line level, feetched in the set
                    * Also, fetched the information related to tax in different maps
                    */
                    boolean isRowTaxApplicable = false;
                    double rowTaxPercentGST = 0.0;
                    double linelevelTaxAmount = 0.0; // Line level Tax + Term tax amount
                    if (row.getTax() != null) {
                        String taxCode = row.getTax().getTaxCode();
                        if (!lineLevelTaxesGST.contains(taxCode)) {
                            lineLevelTaxesGST.add(taxCode);
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, deliveryOrder.getOrderDate(), row.getTax().getID());
                            rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                            lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                            lineLevelTaxAmountGST.put(taxCode, 0.0);
                            lineLevelTaxBasicGST.put(taxCode, 0.0);
                        }
                        linelevelTaxAmount = row.getRowTaxAmount();
                        lineLevelTaxAmountGST.put(taxCode,(Double) lineLevelTaxAmountGST.get(taxCode) + row.getRowTaxAmount());
                        lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + (rate * deliveredquantity));
                        /*
                         * putting subtotal+tax
                         */  
                     }
                    if(deliveryOrder.isGstIncluded()){
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round(rowamountwithouttax, companyid) + authHandler.round(linelevelTaxAmount, companyid));
                    } else{
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid) + authHandler.round(linelevelTaxAmount, companyid));
                    }
                }
                obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory()
                        || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory()
                        || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {
                    if (isbatchforproduct || isserialforproduct || isLocationForProduct || isWarehouseForProduct) {  //product level batch and serial no on or not but now only location is checked
                    String batchdetails = "";
                    /* if pick pack is on for delivery order*/
                    if(extraCompanyPreferences.isPickpackship()){
                        Map<String, Object> batchSerialReqMap = new HashMap<>();
                         batchSerialReqMap.put(Constants.companyKey, companyid);
                          batchSerialReqMap.put(Constants.df, df);
                            batchSerialReqMap.put("linkingFlag", linkingFlag);
                            batchSerialReqMap.put("isEdit", isEdit);
                            batchSerialReqMap.put(Constants.isConsignment, isConsignment);
                            batchSerialReqMap.put("srflag", srflag);
                            batchSerialReqMap.put("moduleID", moduleid);
                            batchSerialReqMap.put("dodid", row.getID());
                        batchdetails = accInvoiceServiceDAO.getDOBatchJsonUsingIST(batchSerialReqMap);
                    }else{ 
                        batchdetails = accInvoiceServiceDAO.getNewBatchJson(row.getProduct(), requestObj, row.getID());
                    }
                        JSONArray locjArr = new JSONArray(batchdetails);
                        String location = "";
                        String locationName = "",warehouseName= "", batchname = "", batchQty = "", warehouse = "", serialnumber = "",batchexpdate = "",batchmfgdate = "";
                        String locationnamenew = "", batchnamenew = "", batchQtyNew = "", warehousenew = "", serialnumbernew = "", batchexpdatenew = "", batchesmfgdatenew = "",warehousenewName="";
                        Set<String> batchnames = new LinkedHashSet();
                        Set<String> serialnumbers = new LinkedHashSet();
                        Set<String> locationnames = new LinkedHashSet();
                        Set<String> warehouses = new LinkedHashSet();
                        LinkedList<String> batchesexpirydate = new LinkedList();
                        LinkedList<String> batchesmfgdate = new LinkedList();
                        Date date = null;
                        for (int i = 0; i < locjArr.length(); i++) {
                            JSONObject jSONObject = new JSONObject(locjArr.get(i).toString());
                            location = jSONObject.optString("location", "");
                            batchname = jSONObject.optString("batchname", "");
                            //get batch quantity
                            batchQty = jSONObject.optString("quantity", "");
                            warehouse = jSONObject.optString("warehouse", "");
                            serialnumber = jSONObject.optString("serialno", "");
                            batchexpdate = jSONObject.optString("expdate", "");
                            batchmfgdate = jSONObject.optString("mfgdate", "");
                            serialnumbers.add(serialnumber);
//                            batchnames.add(batchname);
                            //append batch name and quantity
                            if (!StringUtil.isNullOrEmpty(batchname)) {
                                batchnamenew += batchname + "!##";
                                countOfBatches++;
                            }
                            if (!StringUtil.isNullOrEmpty(batchQty)) {
                                batchQtyNew += batchQty + "!##";
                            }
                             if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
                                date = df1.parse(batchmfgdate);
                                batchmfgdate = df.format(date);
                                if (batchesmfgdate.contains(batchmfgdate)) {
                                    batchesmfgdate.add(" ");
                                } else {
                                    batchesmfgdate.add(batchmfgdate);
                                }
                            } else {
                                batchesmfgdate.add(" ");
                            }
                            if (!StringUtil.isNullOrEmpty(warehouse)) {
                                KwlReturnObject warehouseResult = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), warehouse);
                                InventoryWarehouse warehouseobj = (InventoryWarehouse) warehouseResult.getEntityList().get(0);
                                if (warehouseobj != null) {
                                    warehouseName = warehouseobj.getName();
                                    warehouses.add(warehouseName);
                                }
                            }
                            if (!StringUtil.isNullOrEmpty(batchexpdate)) {
                                date = df1.parse(batchexpdate);
                                batchexpdate = df.format(date);
                                if (batchesexpirydate.contains(batchexpdate)) {
                                     batchesexpirydate.add(" ");
                                } else {
                                    batchesexpirydate.add(batchexpdate);
                                }
                            } else {
                                batchesexpirydate.add(" ");
                            }

                            if (!StringUtil.isNullOrEmpty(location)) {
                                KwlReturnObject loc = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), location);
                                InventoryLocation localist = (InventoryLocation) loc.getEntityList().get(0);
                                locationName = localist.getName();
                                locationnames.add(locationName);
//                                locationName = locationName.concat(",");
                            }

//                            if (!StringUtil.isNullOrEmpty(batchname)) {
//                                batchname = batchname.concat(",");
//                            }
//                            if (!StringUtil.isNullOrEmpty(serialnumber)) {
//                                serialnumber = serialnumber.concat(",");
//                            }
//                             if (!StringUtil.isNullOrEmpty(batchexpdate)) {
//                                batchexpdate = batchexpdate.concat("<br>");
//                            }
//                             
//                             if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
//                                batchmfgdate = batchmfgdate.concat("<br>");
//                            }
                        }

                        for (String str : warehouses) {
                            String wno = "";
                            wno = str;
                            if (!StringUtil.isNullOrEmpty(wno) && !wno.equals(" ")) {
                                warehousenewName += wno.concat("!##");
                            }
                        }
//                        for (String str : batchnames) {
//                            String bno = "";
//                            bno = str;
//                            batchnamenew += bno.concat(",");
//                        }

                        for (String str : serialnumbers) {
                            String sno = "";
                            sno = str;
                            if (!StringUtil.isNullOrEmpty(sno) && !sno.equals(" ")) {
                                serialnumbernew += sno.concat("!##");
                            }
                        }
                        for (String str : locationnames) {
                            String lno = "";
                            lno = str;
                            if (!StringUtil.isNullOrEmpty(lno) && !lno.equals(" ")) {
                                locationnamenew += lno.concat("!##");
                            }
                        }
                        for (String str : batchesexpirydate) {
                            String bexp = "";
                            bexp = str;
                            if (!StringUtil.isNullOrEmpty(bexp) && !bexp.equals(" ")) {
                                batchexpdatenew += bexp.concat("!##");
                            }
                        }
                        for (String str : batchesmfgdate) {
                            String bmfg = "";
                            bmfg = str;
                            if (!StringUtil.isNullOrEmpty(bmfg) && !bmfg.equals(" ")) {
                                batchesmfgdatenew += bmfg.concat("!##");
                            }
                        }

                        if (!StringUtil.isNullOrEmpty(warehousenewName)) {
                            warehousenewName = warehousenewName.substring(0, warehousenewName.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(locationnamenew)) {
                            locationnamenew = locationnamenew.substring(0, locationnamenew.length() - 3);
                        }

                        if (!StringUtil.isNullOrEmpty(serialnumbernew)) {
                            serialnumbernew = serialnumbernew.substring(0, serialnumbernew.length() - 3);
                        }

                        if (!StringUtil.isNullOrEmpty(batchnamenew)) {
                            batchnamenew = batchnamenew.substring(0, batchnamenew.length() - 3);
                        }
                        //remove extra comma from end of batch quantity string
                        if (!StringUtil.isNullOrEmpty(batchQtyNew)) {
                            batchQtyNew = batchQtyNew.substring(0, batchQtyNew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(batchesmfgdatenew)) {
                            batchesmfgdatenew = batchesmfgdatenew.substring(0, batchesmfgdatenew.length() - 3);
                        }
                        obj.put(CustomDesignerConstants.Warehouse, warehousenewName);
                        obj.put(CustomDesignerConstants.DO_SerialNumber, serialnumbernew);
                        obj.put(CustomDesignerConstants.BatchNumber, batchnamenew);
                        //put batch quantity
                        obj.put(CustomDesignerConstants.BatchQuantity, batchQtyNew);
                        obj.put(CustomDesignerConstants.DO_Loc, locationnamenew);
                        obj.put(CustomDesignerConstants.PACK_LOCATION, locationnamenew);
                        obj.put(CustomDesignerConstants.BatchNumberExp, batchexpdatenew);
                        obj.put(CustomDesignerConstants.ManufacturingDate, batchesmfgdatenew);// Batch Manufacturing Date
                    } else { //if not activated location for product level then take default location for product.
                        obj.put(CustomDesignerConstants.DO_Loc, row.getProduct().getLocation() == null ? "" : row.getProduct().getLocation().getName());
                        obj.put(CustomDesignerConstants.PACK_LOCATION, row.getProduct().getLocation() == null ? "" : row.getProduct().getLocation().getName());
                    }
                } else { //if not activated location for product level then take default location for product.
                    obj.put(CustomDesignerConstants.DO_Loc, row.getProduct().getLocation() == null ? "" : row.getProduct().getLocation().getName());
                    obj.put(CustomDesignerConstants.PACK_LOCATION, row.getProduct().getLocation() == null ? "" : row.getProduct().getLocation().getName());
                }
                
              /*
                 * to get the linkig information upto 2-3 levels (Mayur B).
                 */
                if (row.getCidetails() != null) {
                    if (Invref.indexOf(row.getCidetails().getInvoice().getInvoiceNumber()) == -1) {
                        Invref += row.getCidetails().getInvoice().getInvoiceNumber()+", ";
//                        InvoiceDate = row.getCidetails().getInvoice().getJournalEntry().getEntryDate();
                        InvoiceDate = row.getCidetails().getInvoice().getCreationDate();
                        poRefNo = row.getCidetails().getInvoice().getPoRefNumber();
                    }
                    if (row.getCidetails().getSalesorderdetail() != null) {
                        if(SOref.indexOf(row.getCidetails().getSalesorderdetail().getSalesOrder().getSalesOrderNumber())==-1){ 
                            SOref += row.getCidetails().getSalesorderdetail().getSalesOrder().getSalesOrderNumber()+", ";
                            SalesOrderDate=row.getCidetails().getSalesorderdetail().getSalesOrder().getOrderDate();
                        }
                        if (row.getCidetails().getSalesorderdetail().getQuotationDetail() != null) {
                            if(QouteRef.indexOf(row.getCidetails().getSalesorderdetail().getQuotationDetail().getQuotation().getquotationNumber())==-1)
                                QouteRef += row.getCidetails().getSalesorderdetail().getQuotationDetail().getQuotation().getquotationNumber()+", ";
                        }
                    }
                    if(row.getCidetails().getQuotationDetail() != null){
                        if(QouteRef.indexOf(row.getCidetails().getQuotationDetail().getQuotation().getquotationNumber())==-1)    
                            QouteRef = row.getCidetails().getQuotationDetail().getQuotation().getquotationNumber()+", ";
                    }
                } else if (row.getSodetails() != null) {
                    if(SOref.indexOf(row.getSodetails().getSalesOrder().getSalesOrderNumber())==-1){
                        SOref += row.getSodetails().getSalesOrder().getSalesOrderNumber()+", ";
                        SalesOrderDate=row.getSodetails().getSalesOrder().getOrderDate();
                    }
                       if (row.getSodetails().getQuotationDetail() != null) {
                        if(QouteRef.indexOf(row.getSodetails().getQuotationDetail().getQuotation().getquotationNumber())==-1)
                            QouteRef += row.getSodetails().getQuotationDetail().getQuotation().getquotationNumber()+", ";
                    }
                       
                   if(row.getSodetails().getSalesOrder().getMovementType()!=null){
                        requestType=row.getSodetails().getSalesOrder().getMovementType().getValue();
                    }
                    if (moduleid == Constants.Acc_Lease_DO) {//Get main tax form lease DO
                        leaseMainTax = row.getSodetails().getSalesOrder().getTax();
                    }
                }
                
                //Product Tax Calculation
                totalDiscount +=authHandler.round(rowdiscountvalue, companyid);
                if (row.getDiscountispercent() == 1) {
                    discountname = CustomDesignHandler.getAmountinCommaDecimal(row.getDiscount(), 0,countryid) + "%";//to return 0 no of zeros
                } else {
                    discountname = currency.getSymbol() + " " + authHandler.formattingDecimalForAmount(row.getDiscount(), companyid);//to show as it is in UI
                }
                
                obj.put(CustomDesignerConstants.DO_Discount,authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                obj.put(CustomDesignerConstants.Discountname,discountname);// Discount
                obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithouttax, companyid));//Subtotal   
                //Sub Total - Discount
                if(deliveryOrder.isGstIncluded()){
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithouttax + rowdiscountvalue, companyid));//Subtotal   
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount((rowamountwithouttax), companyid));
                    subtotal += authHandler.round(rowamountwithouttax + rowdiscountvalue , companyid);//rounded becuase totaltax & amount are rounded and saved in db
                } else{
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithouttax, companyid));//Subtotal   
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount((rowamountwithouttax - rowdiscountvalue), companyid));
                    subtotal += authHandler.round(rowamountwithouttax, companyid);//rounded becuase totaltax & amount are rounded and saved in db
                }
                
                if (row != null && row.getTax() != null) {
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", row.getTax().getID());
                    KwlReturnObject resulttax = accTaxObj.getTax(requestParams);
                    List taxList = resulttax.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    rowTaxName = row.getTax().getName();
                    rowTaxAmt=row.getRowTaxAmount();
                    uniqueProductTaxList.add(row.getTax().getID());
                } 

                totaltax +=authHandler.round(rowTaxAmt, companyid);
                if (rowdiscountvalue != 0 && !deliveryOrder.isGstIncluded()) {
                    rowamountwithouttax -= rowdiscountvalue;//deducting discount if any
                }
                 rowamountwithtax = rowamountwithouttax + rowTaxAmt; 
                 totalAmount +=authHandler.round(rowamountwithtax, companyid);
                 
                obj.put(CustomDesignerConstants.IN_Tax, authHandler.formattingDecimalForAmount(rowTaxAmt, companyid));// Tax Amount
                obj.put(CustomDesignerConstants.DO_ProductTax, rowTaxName);// Tax Name
                obj.put(CustomDesignerConstants.Amount, authHandler.formattingDecimalForAmount(rowamountwithtax, companyid));// Amount

                /*
                 * get custom line data
                 */
                Map<String, Object> variableMap = new HashMap<String, Object>();
                DeliveryOrderDetailCustomData jeDetailCustom = (DeliveryOrderDetailCustomData) row.getDeliveryOrderDetailCustomData();
                replaceFieldMap = new HashMap<String, String>();
                if (jeDetailCustom != null) {
                   ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, jeDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                 
                /*Product Level Custom Fields Evaluation*/
                KwlReturnObject resultProduct = accountingHandlerDAOobj.getObject(DeliveryOrderDetailProductCustomData.class.getName(), row.getID());
                DeliveryOrderDetailProductCustomData qProductDetailCustom = (DeliveryOrderDetailProductCustomData) resultProduct.getEntityList().get(0);
                replaceFieldMap = new HashMap<String, String>();
                if (qProductDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, qProductDetailCustom, ProductLevelCustomFieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                
                 /*Set All Line level Dimension & All LIne level Custom Field Values*/   
                obj=CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap,variableMap,obj,false);//for dimensions
                obj=CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap,variableMap,obj,true);//for customfields
                
                // Pick and Pack details
                double packQty = 0;
                String quantityPerPackage = "", grossWeight = "", packageName = "";
                String measurement = "";
                HashMap<String, Object> doRequestParams = new HashMap<String, Object>();
                ArrayList filter_nm = new ArrayList(), filter_par = new ArrayList();
                filter_nm.add("deliveryOrder.ID");
                filter_par.add(deliveryOrder.getID());
                doRequestParams.put("filter_names", filter_nm);
                doRequestParams.put("filter_params", filter_par);
                KwlReturnObject pdoresult = accInvoiceDAOobj.getPackingDoDetails(doRequestParams);
                Iterator pdoItr = pdoresult.getEntityList().iterator();
                while (pdoItr.hasNext()) {
                    DoDetails dod = (DoDetails) pdoItr.next();
                    String packingno = dod.getPacking().getPackNumber();
                    if(prod.getID().equals(dod.getProduct().getID())){
                        packQty += dod.getPackQuantity();
                        Set<ItemDetail>  packingdetails = dod.getPackingDetails().getPackingdetails();
                        for (ItemDetail item : packingdetails) {
                            quantityPerPackage += item.getItemPerPackage() +"!## ";
                            grossWeight += item.getGrossWeight() + "!## ";
                            measurement += item.getPackages() != null ? item.getPackages().getMeasurement() + "!## " : "";
                            packageName += item.getPackages() != null ? item.getPackages().getPackagename() + "!## " : "";
                            totalGrossWeight += item.getGrossWeight();
                        }
                    }
                }
                if(!StringUtil.isNullOrEmpty(quantityPerPackage)){
                    quantityPerPackage = quantityPerPackage.substring(0, quantityPerPackage.length()-4);
                }
                if(!StringUtil.isNullOrEmpty(grossWeight)){
                    grossWeight = grossWeight.substring(0, grossWeight.length()-4);
                }
                if(!StringUtil.isNullOrEmpty(measurement)){
                    measurement = measurement.substring(0, measurement.length()-4);
                }
                if(!StringUtil.isNullOrEmpty(packageName)){
                    packageName = packageName.substring(0, packageName.length()-4);
                }
                obj.put(CustomDesignerConstants.QUANTITY_PER_PACKAGE, quantityPerPackage);
                obj.put(CustomDesignerConstants.GROSS_WEIGHT, grossWeight);
                obj.put(CustomDesignerConstants.PACKAGE_MEASUREMENT, measurement);
                obj.put(CustomDesignerConstants.PACKAGE_NAME, packageName);
                
                obj.put(CustomDesignerConstants.PACK_QUANTITY, packQty);// Tax Name
                JSONObject jObj = null;
                    if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                    jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                    if (jObj.has(Constants.isDisplayUOM) && jObj.get(Constants.isDisplayUOM) != null && (Boolean) jObj.get(Constants.isDisplayUOM) != false) {
                        obj = accProductObj.getProductDisplayUOM(row.getProduct(), deliveredquantity, row.getBaseuomrate(), false, obj);
                    }
                }
                jArr.put(obj);
            }
            detailsTableData.put("gsttaxsummary", gstTaxSummaryDetailsTableDataArr);
            detailsTableData.put("isDetailsTableData", true);
            jArr.put(detailsTableData);
            
            if (!StringUtil.isNullOrEmpty(SOID)) {
                List doTermMapList = kwlCommonTablesDAOObj.getSummationOfTermAmtAndTermTaxAmt(Constants.deliveryordertermmap, SOID);
                if(doTermMapList != null && !doTermMapList.isEmpty()){
                    Iterator termItr = doTermMapList.iterator();
                    while (termItr.hasNext()) {
                        Object[] obj = (Object[]) termItr.next();
                        /* 
                         * [0] : Sum of termamount  
                         * [1] : Sum of termamountinbase 
                         * [2] : Sum of termTaxamount 
                         * [3] : Sum of termTaxamountinbase 
                         * [4] : Sum of termAmountExcludingTax 
                         * [5] : Sum of termAmountExcludingTaxInBase
                         */                 
                        if(obj[2] != null){
                            totaltax += (Double) obj[2];
                        }
                    }
                }
            }
            if (moduleid == Constants.Acc_Lease_DO) {//Get main tax for lease module
                mainTax = leaseMainTax;
            } else {
                mainTax = deliveryOrder.getTax();
            }
            entryDate=deliveryOrder.getOrderDate();
            if (mainTax != null) { //Get tax percent
                requestParams.put("transactiondate", entryDate);
                requestParams.put("taxid", mainTax.getID());
                KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                List taxList = result1.getEntityList();
                Object[] taxObj = (Object[]) taxList.get(0);
                taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                mainTaxName = mainTax.getName();
                totalAmount = subtotal - totalDiscount;
                totaltax += (taxPercent == 0 ? 0 : totalAmount * taxPercent / 100);//overall tax calculate
            }
            totalAmount = subtotal + totaltax - totalDiscount;
            
            /*
             * Terms calculation
             */
            String term = "", termsname = "";
            double totalTermAmount = 0;
            KwlReturnObject termMapResult = null;
            HashMap<String, Object> requestParam = new HashMap();
//            boolean isTaxTermMapped = false;
//            double lineleveltermTaxAmount = 0;
//            double termAmountBeforeTax = 0;
//            double termAmountAfterTax = 0;
            if (moduleid == Constants.Acc_Lease_DO && !StringUtil.isNullOrEmpty(linkedreferenecenumber)) {
                requestParam.put("salesOrder", linkedreferenecenumber);
                termMapResult = accSalesOrderDAOobj.getSalesOrderTermMap(requestParam);
                List<SalesOrderTermMap> termMap = termMapResult.getEntityList();
                for (SalesOrderTermMap salesOrderTermMap : termMap) {
                    InvoiceTermsSales mt = salesOrderTermMap.getTerm();
                    double termAmnt = salesOrderTermMap.getTermamount();
                    totalTermAmount += termAmnt;
                    String termName = (mt.getTerm() + (termAmnt > 0 ? "(+)" : "(-)"));
                    summaryData.put(mt.getTerm(), termAmnt);
                    term += "<div> <table><tr><td>" + termName + " : </td><td>" + Double.toString(termAmnt) + "</td></tr></table></div><br>";
                    termsname += mt.getTerm() + " " + salesOrderTermMap.getPercentage() + "%, ";
                    if (!StringUtil.isNullOrEmpty(String.valueOf(termAmnt))) {
                        String allTermsPlaceholder = CustomDesignerConstants.AllTermsKeyValuePair;
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsLabel, termName);
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsValue, CustomDesignHandler.getAmountinCommaDecimal(termAmnt, amountdigitafterdecimal, countryid));
                        appendtermString.append(allTermsPlaceholder);
                    }
                }
                if (!StringUtil.isNullOrEmpty(termsname)) {
                    termsname = termsname.substring(0, termsname.length() - 2);
                }
                if (!StringUtil.isNullOrEmpty(term) && term.indexOf("<br>") != -1) {
                    term = term.substring(0, term.lastIndexOf("<br>"));
                }
                if (!StringUtil.isNullOrEmpty(appendtermString.toString())) {
                    allTerms = appendtermString.toString();
                }
            } else if (!StringUtil.isNullOrEmpty(SOID)) {
                Map<String, Object> taxListParams = new HashMap<String, Object>();
                taxListParams.put("companyid", companyid);
                boolean isApplyTaxToTerms=deliveryOrder.isApplyTaxToTerms();

                HashMap<String, Object> filterrequestParams = new HashMap();
                filterrequestParams.put("taxid", deliveryOrder.getTax()==null?"":deliveryOrder.getTax().getID());
                requestParam.put("deliveryOrderID", SOID);
                termMapResult = accInvoiceDAOobj.getDOTermMap(requestParam);
                List<DeliveryOrderTermMap> termMap = termMapResult.getEntityList();
                for (DeliveryOrderTermMap invoiceTerMap : termMap) {
//                    InvoiceTermsSales mt = invoiceTerMap.getTerm();
                    double termAmnt = 0;
                    if(deliveryOrder.isGstIncluded()){
                        termAmnt = invoiceTerMap.getTermAmountExcludingTax();
                    }else{
                        termAmnt = invoiceTerMap.getTermamount();
                    }
                    filterrequestParams.put("term", invoiceTerMap.getTerm() == null ? "" : invoiceTerMap.getTerm().getId());
                    filterrequestParams.put("companyid", companyid);

//                    if (uniqueProductTaxList.size() > 0 && isApplyTaxToTerms) {
//                        for (String taxId : uniqueProductTaxList) {
//                            filterrequestParams.put("taxid", taxId);
//                            taxListParams.put("taxid", taxId);
//                            isTaxTermMapped = accTaxObj.isTermMappedwithTax(filterrequestParams);
//                            if (isTaxTermMapped) {
//                                KwlReturnObject taxListResult = accTaxObj.getTaxList(taxListParams);
//                                if (taxListResult != null && taxListResult.getEntityList() != null) {
//                                    List<TaxList> taxListPercent = taxListResult.getEntityList();
//                                    lineleveltermTaxAmount += (termAmnt * (taxListPercent.get(0).getPercent())) / 100;
//                                }
////                                    break;
//                            }
//                        }
//                    } else {
//                        isTaxTermMapped = accTaxObj.isTermMappedwithTax(filterrequestParams);
//                    }

                    InvoiceTermsSales mt = invoiceTerMap.getTerm();
//                    if(isTaxTermMapped){ // If term is mapped with tax
//                        termAmountBeforeTax += invoiceTerMap.getTermamount(); // term amount for adding before tax calculation
//                    } else{
//                        termAmountAfterTax += invoiceTerMap.getTermamount(); // term amount for adding after tax calculation
//                    }
                    String termName = (mt.getTerm() + (termAmnt > 0 ? "(+)" : "(-)"));
                    summaryData.put(mt.getTerm(), termAmnt);
                    totalTermAmount += termAmnt;
                    term += "<div> <table><tr><td>" + termName + " : </td><td>" + Double.toString(termAmnt) + "</td></tr></table></div><br>";
                    termsname += mt.getTerm() + " " + invoiceTerMap.getPercentage() + "%, ";
                    if (!StringUtil.isNullOrEmpty(String.valueOf(termAmnt))) {
                        String allTermsPlaceholder = CustomDesignerConstants.AllTermsKeyValuePair;
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsLabel, termName);
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsValue, CustomDesignHandler.getAmountinCommaDecimal(termAmnt, amountdigitafterdecimal, countryid));
                        appendtermString.append(allTermsPlaceholder);
                    }
                }
                if (!StringUtil.isNullOrEmpty(termsname)) {
                    termsname = termsname.substring(0, termsname.length() - 2);
                }
                if (!StringUtil.isNullOrEmpty(term) && term.indexOf("<br>") != -1) {
                    term = term.substring(0, term.lastIndexOf("<br>"));
                }
                if (!StringUtil.isNullOrEmpty(appendtermString.toString())) {
                    allTerms = appendtermString.toString();
                }
            }

            Amountwithoutterm = subtotal + totaltax;//Amount after adition of subtotal and total tax without invoice term
            double TotalLineLevelTaxAmount = 0;
//            if(isTaxTermMapped){ // If tax mapped with any term
//                if (mainTax != null) {
//                    totaltax = (taxPercent == 0 ? 0 : ((subtotal - totalDiscount) + termAmountBeforeTax) * taxPercent /100); // first add term into subtotal then calculate tax
//                }
//                totaltax += lineleveltermTaxAmount;
//                totalAmount = subtotal - totalDiscount + termAmountBeforeTax + totaltax + termAmountAfterTax; // first add mapped terms amount in subtotal then add total tax then add unmapped terms amount for total amount
//            } else {
            totalAmount = totalAmount + totalTermAmount;
//            }
            if(extraCompanyPreferences.isIsNewGST()){ // for New gst check 
                if(!lineLevelTaxNames.isEmpty()){
                    Iterator lineTax = lineLevelTaxNames.entrySet().iterator();
                    while(lineTax.hasNext()){
    //                    allLineLevelTax="", allLineLevelTaxAmount = ""
                        Map.Entry tax = (Map.Entry)lineTax.next();
                        allLineLevelTax += tax.getKey();
                        allLineLevelTax += "!## ";
                        allLineLevelTaxAmount += tax.getValue().toString();
                        totalAmount += (double)tax.getValue();
                        TotalLineLevelTaxAmount += (double)tax.getValue();
                        allLineLevelTaxAmount += "!## ";
                    }
                }
                if(!StringUtil.isNullOrEmpty(allLineLevelTax)){
                    allLineLevelTax = allLineLevelTax.substring(0, allLineLevelTax.length()-4);
                }
                if(!StringUtil.isNullOrEmpty(allLineLevelTaxAmount)){
                    allLineLevelTaxAmount=allLineLevelTaxAmount.substring(0, allLineLevelTaxAmount.length()-4);
                }
            } else {
                /*
                * Putting all line taxes and its information in summary data separated by !##
                */
                for ( String key : lineLevelTaxesGST ) {
                    allLineLevelTax += key + "!##";
                    allLineLevelTaxAmount += lineLevelTaxAmountGST.get(key).toString() + "!##";
                    allLineLevelTaxBasic += lineLevelTaxBasicGST.get(key).toString() + "!##";
                }
            }
            
            if(countryid == Constants.indian_country_id){
                totaltax = TotalLineLevelTaxAmount;
                String buyerexcRegNo="", buyetinNo="", buyerpanNo="", buyerRange="", buyerDivision="", buyerComrate="", buyerServiceTaxRegNo="";
                
                // Customer Related indian details
                String panStatus = "", IECNo = "", CSTDateStr = "", VATDateStr = "", dealerTypeStr = "";
                String ImporterECCNo = "",  typeOfSales = "";

                if (deliveryOrder.getCustomer().getPanStatus() != null && !StringUtil.isNullOrEmpty(deliveryOrder.getCustomer().getPanStatus())) {
                    panStatus = deliveryOrder.getCustomer().getPanStatus().equals(IndiaComplianceConstants.PAN_STATUS_PANNOTAVBL) ? IndiaComplianceConstants.PAN_NOT_AVAILABLE : deliveryOrder.getCustomer().getPanStatus().equals(IndiaComplianceConstants.PAN_STATUS_APPLIEDFOR) ? IndiaComplianceConstants.PAN_APPLIED_FOR : "";
                }
                if(deliveryOrder.getCustomer().getDefaultnatureOfPurchase()!= null && !StringUtil.isNullOrEmpty(deliveryOrder.getCustomer().getDefaultnatureOfPurchase())){
                    KwlReturnObject naturetyperesult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), deliveryOrder.getCustomer().getDefaultnatureOfPurchase());
                    MasterItem natureTypeObj = (MasterItem) naturetyperesult.getEntityList().get(0);
                    if(natureTypeObj != null){
                        typeOfSales = natureTypeObj.getValue();
                    }
                }

                if (deliveryOrder.getCustomer().getDealertype() != null && !StringUtil.isNullOrEmpty(deliveryOrder.getCustomer().getDealertype())) {
                    String dealerType = deliveryOrder.getCustomer().getDealertype();
                    if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_REGISTERED)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_REGISTERED_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_UNREGISTERED)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_UNREGISTERED_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_1_2)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_1_2_STR;
                    }
                }
                if (deliveryOrder.getCustomer().getCSTRegDate() != null) {
                    Date CSTDate = deliveryOrder.getCustomer().getCSTRegDate();
                    CSTDateStr = CSTDate != null ? authHandler.getUserDateFormatterJson(requestObj).format(CSTDate) : "";
                }
                if (deliveryOrder.getCustomer().getVatregdate() != null) {
                    Date VATDate = deliveryOrder.getCustomer().getVatregdate();
                    VATDateStr = VATDate != null ? authHandler.getUserDateFormatterJson(requestObj).format(VATDate) : "";
                }

                buyerComrate = deliveryOrder.getCustomer().getCommissionerate() != null ? deliveryOrder.getCustomer().getCommissionerate() : "";
                buyerDivision = deliveryOrder.getCustomer().getDivision() != null ? deliveryOrder.getCustomer().getDivision() : "";
                buyerRange = deliveryOrder.getCustomer().getRangecode() != null ? deliveryOrder.getCustomer().getRangecode() : "";
                buyetinNo = deliveryOrder.getCustomer().getVATTINnumber() != null ? deliveryOrder.getCustomer().getVATTINnumber() : "";
                buyerexcRegNo = deliveryOrder.getCustomer().getECCnumber() != null ? deliveryOrder.getCustomer().getECCnumber() : "";
                buyerpanNo = deliveryOrder.getCustomer().getPANnumber() != null ? deliveryOrder.getCustomer().getPANnumber() : "";
                buyerServiceTaxRegNo = deliveryOrder.getCustomer().getSERVICEnumber() != null ? deliveryOrder.getCustomer().getSERVICEnumber() : "";
                ImporterECCNo = (deliveryOrder.getCustomer() != null && deliveryOrder.getCustomer().getImporterECCNo() != null) ? deliveryOrder.getCustomer().getImporterECCNo() : "";
                IECNo = (deliveryOrder.getCustomer() != null && deliveryOrder.getCustomer().getIECNo() != null) ? deliveryOrder.getCustomer().getIECNo() : "";

                // ************************   Customer Related Information **********************************************
                summaryData.put(CustomDesignerConstants.CUSTOMER_PAN_NO, buyerpanNo);
                summaryData.put(CustomDesignerConstants.CustomerVendor_PAN_STATAUS, panStatus);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, buyetinNo);
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, deliveryOrder.getCustomer().getCSTTINnumber() != null ? deliveryOrder.getCustomer().getCSTTINnumber() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_REG_DATE, CSTDateStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_REG_DATE, VATDateStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_DEALER_TYPE, dealerTypeStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_INTERSTATEPARTY, deliveryOrder.getCustomer().isInterstateparty() ? "Yes" : "No");
                summaryData.put(CustomDesignerConstants.CustomerVendor_C_FORM_APPLICABLE, deliveryOrder.getCustomer().isCformapplicable() ? "Yes" : "No");
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_TYPE_OF_SALES, typeOfSales);
                summaryData.put(CustomDesignerConstants.CustomerVendor_ECC_NO, buyerexcRegNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_IMPORTER_ECC_NUMBER, ImporterECCNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_IEC_NUMBER, IECNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_RANGE_CODE, buyerRange);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_DIVISION_CODE, buyerDivision);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_COMMISSIONERATE_CODE, buyerComrate);
                summaryData.put(CustomDesignerConstants.CUSTOMER_SERVICE_TAX_REG_NO, buyerServiceTaxRegNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, deliveryOrder.getCustomer().getGSTIN() != null ? deliveryOrder.getCustomer().getGSTIN() : "");
                // ****************************************************************************************************
            }
            
            String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency,countryLanguageId);
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if(countryid == Constants.INDONESIAN_COUNTRY_ID){
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(totalAmount)), indoCurrency);
            }
            String gstAmountInWords = EnglishNumberToWordsOjb.convert(totaltax, currency,countryLanguageId);

            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("customerid", deliveryOrder.getCustomer().getID());
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData,preference, extraCompanyPreferences);
            billAddr = CommonFunctions.getTotalBillingShippingAddress(deliveryOrder.getBillingShippingAddresses(), true);
            shipAddr = CommonFunctions.getTotalBillingShippingAddress(deliveryOrder.getBillingShippingAddresses(), false);
            /*
             * All Global Section Custom Field and DImensions
             */
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, moduleid);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", deliveryOrder.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            extraparams.put("approvestatuslevel", deliveryOrder.getApprovestatuslevel());
            String userId = requestObj.optString(Constants.useridKey);
            KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userresult.getEntityList().get(0);
            //Details like company details,base currency
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);
                  
            creditterm = deliveryOrder.getTerm() != null ? deliveryOrder.getTerm().getTermdays() : 0; //ERP-21242
            String systemcurrencysymbol = deliveryOrder.getCurrency().getSymbol();
            String customcurrencysymbol = accCommonTablesDAO.getCustomCurrencySymbol(systemcurrencysymbol, companyid);//Take custom currency symbol
            
            // Pick and Pack details
            String packNo = "";
            int packCount = 0;
            HashMap<String, Object> doRequestParams = new HashMap<String, Object>();
            ArrayList filter_nm = new ArrayList(), filter_par = new ArrayList();
            Set<String> packSet = new TreeSet<String>();
            filter_nm.add("deliveryOrder.ID");
            filter_par.add(deliveryOrder.getID());
            doRequestParams.put("filter_names", filter_nm);
            doRequestParams.put("filter_params", filter_par);
            KwlReturnObject pdoresult = accInvoiceDAOobj.getPackingDoDetails(doRequestParams);
            Iterator pdoItr = pdoresult.getEntityList().iterator();
            while (pdoItr.hasNext()) {
                DoDetails row = (DoDetails) pdoItr.next();
                packSet.add(row.getPacking().getPackNumber());
            }
            packCount = packSet.size();
            packNo = packSet.toString();
            packNo = packNo.substring(1, packNo.length()-1).replaceAll(", ", "!##");
            
            String currentWarehousePacking = "";
            if(!StringUtil.isNullOrEmpty(extraCompanyPreferences.getPackingstore())){
                KwlReturnObject storeResult = kwlCommonTablesDAOObj.getObject(Store.class.getName(), extraCompanyPreferences.getPackingstore());
                Store store = (Store) storeResult.getEntityList().get(0);
                currentWarehousePacking = store.getFullName();
            }
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.CompanyBankIFSCCode, IFSCCode);
            summaryData.put(CustomDesignerConstants.CompanyBankAccountNumber, accountNumber);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId,authHandler.formattedAmount( totalAmount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, authHandler.formattedAmount(totalDiscount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, authHandler.formattedAmount(totaltax, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, authHandler.formattedAmount(subtotal - totalDiscount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, authHandler.formattedAmount(((subtotal - totalDiscount) + totaltax), companyid)); //ERP-25162
            summaryData.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, authHandler.formattedAmount(((subtotal - totalDiscount) + totalTermAmount), companyid));
            summaryData.put(CustomDesignerConstants.SummaryTaxPercent, taxPercent);
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword+" Only.");
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId,QouteRef.equals("")?"":QouteRef.substring(0, QouteRef.length()-2));
            summaryData.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId,SOref.equals("")?"":SOref.substring(0, SOref.length()-2));
            summaryData.put(CustomDesignerConstants.CustomDesignInvRefNumber_fieldTypeId,Invref.equals("")?"":Invref.substring(0, Invref.length()-2));
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
            summaryData.put(CustomDesignerConstants.CustomerVendor_Term,deliveryOrder.getCustomer().getCreditTerm()!=null?(deliveryOrder.getCustomer().getCreditTerm().getTermname()!=null?deliveryOrder.getCustomer().getCreditTerm().getTermname():""):"");
            summaryData.put(CustomDesignerConstants.CustomerVendor_AccountCode,deliveryOrder.getCustomer().getAccount()!=null?(deliveryOrder.getCustomer().getAccount().getAcccode()!=null?deliveryOrder.getCustomer().getAccount().getAcccode():""):"");
            summaryData.put(CustomDesignerConstants.CustomerVendor_Code,deliveryOrder.getCustomer().getAcccode()!=null?deliveryOrder.getCustomer().getAcccode():"");
            summaryData.put(CustomDesignerConstants.CustomerVendor_MappingSalesPerson,deliveryOrder.getCustomer().getMappingSalesPerson()!=null?(deliveryOrder.getCustomer().getMappingSalesPerson().getValue()!=null?deliveryOrder.getCustomer().getMappingSalesPerson().getValue():""):"");
            summaryData.put(CustomDesignerConstants.BillTo, billAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.ShipTo, shipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.CR_memo, crmemo);
            summaryData.put(CustomDesignerConstants.CR_SalesPerson,CR_SalesPerson);
            summaryData.put(CustomDesignerConstants.CR_fromdate,crfromdate!=null?df.format(crfromdate)  :"" ); 
            summaryData.put(CustomDesignerConstants.CR_todate,crtodate!=null?df.format(crtodate):"");
            summaryData.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId,customcurrencysymbol);
            summaryData.put(CustomDesignerConstants.Consocreatedby,soreatedby);
            summaryData.put(CustomDesignerConstants.Createdby,createdby);
            summaryData.put(CustomDesignerConstants.Updatedby, updatedby);
            summaryData.put(CustomDesignerConstants.SalesOrderDate,SalesOrderDate!=null? authHandler.getUserDateFormatterJson(requestObj).format(SalesOrderDate):"");
            summaryData.put(CustomDesignerConstants.InvoiceDate, InvoiceDate != null ? authHandler.getUserDateFormatterJson(requestObj).format(InvoiceDate):"");
            summaryData.put(CustomDesignerConstants.TotalQuantity, totalQuantity);
            summaryData.put(CustomDesignerConstants.TOTAL_DELIVERED_QUANTITY, totalDeliveredQuantity);
            summaryData.put(CustomDesignerConstants.Total_Quantity_UOM, authHandler.formattingDecimalForQuantity(totalQuantity, companyid) +" "+ uomForTotalQuantity);
            summaryData.put(CustomDesignerConstants.TOTAL_DELIVERED_QUANTITY_UOM, authHandler.formattingDecimalForQuantity(totalDeliveredQuantity, companyid) +" "+ uomForTotalQuantity);
            summaryData.put(CustomDesignerConstants.Poreferencenumber, poRefNo);
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPreText_fieldTypeId, config == null ? "" : config.getPdfPreText());
            summaryData.put(CustomDesignerConstants.CustomDesignTemplate_ItemsNo, rowcnt);
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, termsname);
            summaryData.put(CustomDesignerConstants.TotalAmountWithoutTerm, Amountwithoutterm);
            summaryData.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, totalTermAmount);
            summaryData.put(CustomDesignerConstants.AllTerms, allTerms);
            summaryData.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, creditterm); //ERP-21242
            summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, deliveryOrder.getCustomer().getVATTINnumber()!= null ? deliveryOrder.getCustomer().getVATTINnumber() : "");
            summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, deliveryOrder.getCustomer().getCSTTINnumber()!= null ? deliveryOrder.getCustomer().getCSTTINnumber() : "");
            summaryData.put(CustomDesignerConstants.CustomerVendor_ECC_NO, deliveryOrder.getCustomer().getECCnumber()!= null ? deliveryOrder.getCustomer().getECCnumber() : "");
            summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber()!= null ? extraCompanyPreferences.getVatNumber() : "");
            summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber()!= null ? extraCompanyPreferences.getCstNumber() : "");
            summaryData.put(CustomDesignerConstants.Company_ECC_NO, extraCompanyPreferences.getEccNumber()!= null ? extraCompanyPreferences.getEccNumber() : "");
            summaryData.put(CustomDesignerConstants.AllLineLevelTax, allLineLevelTax);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxAmount, allLineLevelTaxAmount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxBasic, allLineLevelTaxBasic);
            summaryData.put(CustomDesignerConstants.CurrentUserFirstName, !StringUtil.isNullOrEmpty(user.getFirstName())?user.getFirstName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserLastName, !StringUtil.isNullOrEmpty(user.getLastName())?user.getLastName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName())?user.getFullName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserEmail, !StringUtil.isNullOrEmpty(user.getEmailID())?user.getEmailID():"");
            summaryData.put(CustomDesignerConstants.CurrentUserAddress, !StringUtil.isNullOrEmpty(user.getAddress())?user.getAddress().replaceAll(Constants.REGEX_LINE_BREAK, "<br>"):"");
            summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber())?user.getContactNumber():"");
            summaryData.put(CustomDesignerConstants.CUSTOMER_TITLE, customerTitle);
            summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName()) ? user.getFullName() : "");
            summaryData.put(CustomDesignerConstants.NO_OF_PACKAGE, packCount);
            summaryData.put(CustomDesignerConstants.PACKAGE_NO, packNo);
            summaryData.put(CustomDesignerConstants.PACK_CURRENT_WAREHOUSE, currentWarehousePacking);
            summaryData.put(CustomDesignerConstants.SHIPMENT_TRACKING_NO, !StringUtil.isNullOrEmpty(shipmentTrackingNo) ? shipmentTrackingNo : "");
            summaryData.put(CustomDesignerConstants.CountOfBatches, countOfBatches);
            summaryData.put(CustomDesignerConstants.GSTAmountInWords, gstAmountInWords + Constants.ONLY);
            summaryData.put(CustomDesignerConstants.TOTAL_GROSS_WEIGHT, totalGrossWeight);
            int deliveryDate = 0;
            String deliveryDateVal = "";
            String driver = "";
            String vehicleNo = "";
            String deliveryTime = "";
            if (extraCompanyPreferences.isDeliveryPlanner()) {
                deliveryDate = deliveryOrder.getCustomer() != null ? deliveryOrder.getCustomer().getDeliveryDate():-1;
                deliveryDateVal = getDeliverDayVal(deliveryDate);
                deliveryTime = (deliveryOrder.getCustomer() != null && deliveryOrder.getCustomer().getDeliveryTime() != null) ? deliveryOrder.getCustomer().getDeliveryTime():"";
                driver = (deliveryOrder.getCustomer() != null && deliveryOrder.getCustomer().getDriver() != null)? deliveryOrder.getCustomer().getDriver().getValue():"";
                vehicleNo = (deliveryOrder.getCustomer() != null && deliveryOrder.getCustomer().getVehicleNo() != null)? deliveryOrder.getCustomer().getVehicleNo().getValue():""; 
            }
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, deliveryDateVal);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, deliveryTime);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDriver, driver);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo,vehicleNo);
            summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);
            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE,null,ex); 
        }
        return jArr;
    }
     /*Sales Return & Sales Return with Credit Note*/
    //Method shifted from accInvoiceControllerCMN
   public JSONArray getSalesReturnDetailsItemJSON(JSONObject requestObj, String billids,int moduleid) throws SessionExpiredException, ServiceException, JSONException {
        JSONArray jArr = new JSONArray();
        try {
            String creditnotenumber = "";
            PdfTemplateConfig config = null;
            DateFormat df1 = authHandler.getDateOnlyFormat();
            int quantitydigitafterdecimal = 2, amountdigitafterdecimal = 2, unitpricedigitafterdecimal = 2;
            String Accountcode = "", attn = "", Customerfax = "", customerTel = "", transactionTerm = "",salesreturnnumber="", salesperson = "",salespersondesignation="", custmerterms = "", Createdby = "", refno = "", updatedby = "";
            String billAddr = "", shipAddr = "",requestType="",linkedshippingaddresscp="",globallevelcustomfields="",globalleveldimensions="";
            String linkedshipAddr = "", linkedshipaddresscity="", linkedshipaddresscountry="",linkedshipaddressstate="",linkedshipaddresspincode="",linkedshipaddressphone="",linkedshipaddressmobile="",linkedshipaddressemail="", linkedshipaddressfax="";
            String linkedbillAddr = "", linkedbilladdresscity="", linkedbilladdresscountry="",linkedbilladdressstate="",linkedbilladdresspincode="",linkedbilladdressphone="",linkedbilladdressmobile="",linkedbilladdressemail="", linkedbilladdressfax="";
            String reflinknumber = "", mainTaxName = "",linkedreferencedate="", customerTitle = "", uomForTotalQuantity="",doReferenceNo="",doReferenceDate ="";
            Tax mainTax = null;
            Date entryDate = null, transactionDate = null;
            boolean linkedaddress=false,parseflag=false;
            double subtotal = 0, total = 0;
            double totaltaxamount = 0, rowtaxamount = 0, taxPercent = 0;
            double totalQuantity = 0,totalDiscount = 0;;
            boolean isLocationForProduct = false,isbatchforproduct=false,isserialforproduct=false,isWarehouseForProduct=false;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestObj);
            String companyid = requestObj.optString(Constants.companyKey);
            SalesReturn sr = null;
            sr = (SalesReturn) kwlCommonTablesDAOObj.getClassObject(SalesReturn.class.getName(), billids);
            CreditNote creditNote = null;
            JSONObject summaryData = new JSONObject();
            if (sr.isIsNoteAlso()) {
                KwlReturnObject creditnoteresult = accCreditNoteobj.getCreditNoteIdFromSRId(sr.getID(), companyid);
                if (!creditnoteresult.getEntityList().isEmpty()) {
                    creditNote = (CreditNote) creditnoteresult.getEntityList().get(0);
                    creditnotenumber = creditNote != null ? creditNote.getCreditNoteNumber() : "";
                }
            }
            entryDate = sr.getOrderDate();
            double externalcurrency = sr.getExternalCurrencyRate();
            double revExchangeRate = 0.0;
            double exchangedTotalAmount = 0, exchangedSubTotal = 0, exchangedTotalTax = 0, exchangedSubTotalwithDiscount = 0, subTotalWithDiscount = 0;
            if (externalcurrency != 0) {
                revExchangeRate = 1 / externalcurrency;
            }
            //document currency
            if (sr != null && sr.getCurrency() != null && !StringUtil.isNullOrEmpty(sr.getCurrency().getCurrencyID())) {
                summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, sr.getCurrency().getCurrencyID());
            }
            /**
             * get customer title (Mr./Mrs.)
             */
            customerTitle = sr.getCustomer().getTitle();
            if(!StringUtil.isNullOrEmpty(customerTitle)){
                KwlReturnObject masterItemResult = kwlCommonTablesDAOObj.getObject(MasterItem.class.getName(), customerTitle);
                MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                customerTitle = masterItem.getValue();
            }
            boolean isConsignment = requestObj.optBoolean("isConsignment", false);
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[1] != null) {
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                }
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
                if (decimalcontact[3] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalcontact[3].toString());
                }
            }
            //getting main company posttext
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, Constants.Acc_Sales_Return_ModuleId);
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            /*
             * get Line Item Data
             */
            KwlReturnObject result1 = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result1.getEntityList().get(0);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            /*
            * Declaration of Variables to fetch Line level taxes and its information like
            * Tax Code, Tax Amount etc.
            */ 
            String allLineLevelTax="", allLineLevelTaxAmount = "", allLineLevelTaxBasic = "", lineLevelTaxSign = "",poRefNo="";
            Set<String> poRefNumbers = new HashSet<String>();
            Set<String> lineLevelTaxesGST = new HashSet<String>();
            Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();
            double TotalLineLevelTaxAmount = 0;
            Map<String,Double> lineLevelTaxNames = new LinkedHashMap<String,Double>();
            int countryid = 0;
            if( extraCompanyPreferences.getCompany()!= null && extraCompanyPreferences.getCompany().getCountry() != null ){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            
            Accountcode = sr.getCustomer().getAcccode() != null ? sr.getCustomer().getAcccode() : "";

            HashMap<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("companyid", companyid);
            addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
            addressParams.put("isBillingAddress", true);    //true to get billing address
            addressParams.put("customerid", sr.getCustomer().getID());
            CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
            Customerfax = customerAddressDetails!=null?customerAddressDetails.getFax():"";
            customerTel = customerAddressDetails!=null?customerAddressDetails.getPhone():"";
            custmerterms = sr.getCustomer().getCreditTerm() != null ? (Integer.toString(sr.getCustomer().getCreditTerm().getTermdays()) + " Days") : "";
            attn = customerAddressDetails!=null?customerAddressDetails.getContactPerson():"";
            if (sr.getCreatedby() != null) {
                Createdby = StringUtil.getFullName(sr.getCreatedby());
            }
            updatedby = sr.getModifiedby() != null ? sr.getModifiedby().getFullName() : "";
            HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            order_by.add("srno");
            order_type.add("asc");
            invRequestParams.put("order_by", order_by);
            invRequestParams.put("order_type", order_type);
            KwlReturnObject idresult = null;
            Iterator itr = null;
            filter_names.add("salesReturn.ID");
            filter_params.add(sr.getID());
            invRequestParams.put("filter_names", filter_names);
            invRequestParams.put("filter_params", filter_params);
            idresult = accInvoiceDAOobj.getSalesReturnDetails(invRequestParams);
            itr = idresult.getEntityList().iterator();
            int i = 0;
            JSONObject detailsTableData = new JSONObject();
            JSONArray gstTaxSummaryDetailsTableDataArr = new JSONArray();
            double totalLineLevelTaxAmount = 0;
            while (itr.hasNext()) {
                i++;
                JSONObject obj = new JSONObject();
                JSONObject gstTaxSummaryDetailsTableData = new JSONObject();
                String prodId = "",prodName = "";
                String prodDesc = "",rowTaxName = "",discountname="";;
                double Uprice = 0, amount = 0,returnquantity = 0, actualquantity = 0;
                double exchangerateunitprice = 0,exchangeratelineitemsubtotal=0,exchangeratelineitemdiscount=0,exchangeratesubtotalwithoutdiscount=0,exchangeratelineitemamount=0,exchangeratelineitemtax=0; 
                double rowamountwithouttax=0,rowamountwithtax = 0,rowTaxAmt=0,quantity = 0,rate = 0,rowTaxPercent = 0,rowdiscountvalue=0,rowtotalamount=0;
               
                String Uom = "";
                SalesReturnDetail row = (SalesReturnDetail) itr.next();
                prodId = row.getProduct().getProductid() != null ? row.getProduct().getProductid() : "";
                prodName = row.getProduct().getName() != null ? row.getProduct().getName() : "";
                actualquantity = row.getActualQuantity();
                returnquantity = row.getReturnQuantity();
                df = authHandler.getUserDateFormatterWithoutTimeZone(requestObj);//User Date Formatter
                if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                    prodDesc = row.getDescription();
                }else {
                    if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                        prodDesc = row.getProduct().getDescription();
                    }
                }
                prodDesc = StringUtil.DecodeText(prodDesc);
                KwlReturnObject productCategories = null;
                productCategories = accProductObj.getProductCategoryForDetailsReport(row.getProduct().getID());
                List productCategoryList = productCategories.getEntityList();
                String cateogry = "";
                Iterator catIte = productCategoryList.iterator();
                while (catIte.hasNext()) {
                    ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                    String categoryName = pcm.getProductCategory() != null ? pcm.getProductCategory().getValue().toString() : "";
                    cateogry += categoryName + " ";
                } 
                if ( StringUtil.isNullOrEmpty(cateogry)) {
                    cateogry = "None";
                }
                obj.put("productCategory", cateogry);
                HashMap<String, Object> params = new HashMap<String, Object>();
                filter_names = new ArrayList();
                filter_params = new ArrayList();
                filter_names.add("companyid");
                filter_names.add("fieldtype");
                filter_names.add("customcolumn");
                filter_names.add("moduleid");
                filter_params.add(companyid);
                filter_params.add(4);
                filter_params.add(1);
                filter_params.add(requestObj.optInt("moduleid"));
                params.put("filter_names", filter_names);
                params.put("filter_values", filter_params);
                
                KwlReturnObject fieldparams =  accAccountDAOobj.getFieldParams(params);
                List fieldParamsList = fieldparams.getEntityList();
                
                JSONArray jsonarr = new JSONArray();
                for(int cnt=0; cnt < fieldParamsList.size(); cnt++){
                    FieldParams fieldParamsObj = (FieldParams) fieldParamsList.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("comboName", fieldParamsObj.getFieldname());
                    jsonarr.put(tempObj);
                }
                
                obj.put("groupingComboList", jsonarr);
                
                /*To get the linking information upto 2-3 levels */
                if ((row.getDodetails() != null) && (!(row.getDodetails().equals("undefined")))) {
                    if (reflinknumber.indexOf(row.getDodetails().getDeliveryOrder().getDeliveryOrderNumber()) == -1) {
                        reflinknumber += row.getDodetails().getDeliveryOrder().getDeliveryOrderNumber() + ",";
                        doReferenceNo += row.getDodetails().getDeliveryOrder().getDeliveryOrderNumber() + ",";//Delivery Order Reference Number
                        String refdateString = df.format(row.getDodetails().getDeliveryOrder().getOrderDate());
                        linkedreferencedate = refdateString + ",";
                        doReferenceDate = refdateString + ",";//Delivery Order Reference Date
                    }
                    if (row.getDodetails().getCidetails() != null) {
                        /*
                            Commented below line as we are not going to fetch sales Person Name directly from Sales Return (SDP-14957)
                         */
//                      salesperson = row.getDodetails().getCidetails().getInvoice().getMasterSalesPerson() != null ? !StringUtil.isNullOrEmpty(row.getDodetails().getCidetails().getInvoice().getMasterSalesPerson().getValue()) ? row.getDodetails().getCidetails().getInvoice().getMasterSalesPerson().getValue() : "" : "";
                        salespersondesignation = row.getDodetails().getCidetails().getInvoice().getMasterSalesPerson() != null ? !StringUtil.isNullOrEmpty(row.getDodetails().getCidetails().getInvoice().getMasterSalesPerson().getDesignation()) ? row.getDodetails().getCidetails().getInvoice().getMasterSalesPerson().getDesignation() : "" : "";
                    }
                    if (isConsignment && (!parseflag)) {//For consignment only
                        if (row.getDodetails().getDeliveryOrder().getMovementType() != null) {
                            requestType = row.getDodetails().getDeliveryOrder().getMovementType().getValue();
                        }
                        KwlReturnObject doobject = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), row.getDodetails().getDeliveryOrder().getID());
                        DeliveryOrder consignmentdeliveryorder = (DeliveryOrder) doobject.getEntityList().get(0);
                        billAddr = CommonFunctions.getTotalBillingShippingAddress(consignmentdeliveryorder.getBillingShippingAddresses(), true);
                        shipAddr = CommonFunctions.getTotalBillingShippingAddress(consignmentdeliveryorder.getBillingShippingAddresses(), false);

                        if (consignmentdeliveryorder.getBillingShippingAddresses() != null && consignmentdeliveryorder.getBillingShippingAddresses().getShippingContactPerson() != null) {
                            linkedshippingaddresscp = consignmentdeliveryorder.getBillingShippingAddresses().getShippingContactPerson();
                            //Billing Address
                            linkedbillAddr = consignmentdeliveryorder.getBillingShippingAddresses().getBillingAddress();
                            linkedbilladdresscity = consignmentdeliveryorder.getBillingShippingAddresses().getBillingCity();
                            linkedbilladdressstate = consignmentdeliveryorder.getBillingShippingAddresses().getBillingState();
                            linkedbilladdresscountry = consignmentdeliveryorder.getBillingShippingAddresses().getBillingCountry();
                            linkedbilladdresspincode = consignmentdeliveryorder.getBillingShippingAddresses().getBillingPostal();
                            linkedbilladdressphone = consignmentdeliveryorder.getBillingShippingAddresses().getBillingPhone();
                            linkedbilladdressmobile = consignmentdeliveryorder.getBillingShippingAddresses().getBillingMobile();
                            linkedbilladdressemail = consignmentdeliveryorder.getBillingShippingAddresses().getBillingEmail();
                            linkedbilladdressfax = consignmentdeliveryorder.getBillingShippingAddresses().getBillingFax();
                            //Shipping Address
                            linkedshipAddr = consignmentdeliveryorder.getBillingShippingAddresses().getShippingAddress();
                            linkedshipaddresscity = consignmentdeliveryorder.getBillingShippingAddresses().getShippingCity();
                            linkedshipaddressstate = consignmentdeliveryorder.getBillingShippingAddresses().getShippingState();
                            linkedshipaddresscountry = consignmentdeliveryorder.getBillingShippingAddresses().getShippingCountry();
                            linkedshipaddresspincode = consignmentdeliveryorder.getBillingShippingAddresses().getShippingPostal();
                            linkedshipaddressphone = consignmentdeliveryorder.getBillingShippingAddresses().getShippingPhone();
                            linkedshipaddressmobile = consignmentdeliveryorder.getBillingShippingAddresses().getShippingMobile();
                            linkedshipaddressemail = consignmentdeliveryorder.getBillingShippingAddresses().getShippingEmail();
                            linkedshipaddressfax = consignmentdeliveryorder.getBillingShippingAddresses().getBillingFax();
                        }
                        if (!StringUtil.isNullOrEmpty(billAddr) && !StringUtil.isNullOrEmpty(shipAddr) && !billAddr.equals(".") && !shipAddr.equals(".")) {
                            linkedaddress = true;
                        }
                        parseflag = true;//to stop the calculation of fields again and again
                    }

                } else if (row.getCidetails() != null) {
                    if (reflinknumber.indexOf(row.getCidetails().getInvoice().getInvoiceNumber()) == -1) {
                        reflinknumber += row.getCidetails().getInvoice().getInvoiceNumber() + ",";
                        /**
                         * Get Delivery Order Numbers while Generate Auto DO Option 
                         */
                        KwlReturnObject InvoiceDo = accInvoiceDAOobj.getDOFromInvoices(row.getCidetails().getInvoice().getID(), companyid, true);
                        List<Object[]> InvoiceDoList = InvoiceDo.getEntityList();
                        for (Object[] object : InvoiceDoList) {
                            doReferenceNo = object[0] != null ? object[0].toString()+",": "";
                            doReferenceDate = df.format(row.getCidetails().getInvoice().getCreationDate()) + ",";//Delivery Order Reference Date)+ ",";
                        }
                        /**
                         * Get Delivery Order Numbers Linked to Invoice 
                         */
                        Set<InvoiceDetail> invoiceDetails = row.getCidetails().getInvoice().getRows();
                        for (InvoiceDetail invrow : invoiceDetails) {
                            if (invrow != null && invrow.getDeliveryOrderDetail() != null && invrow.getDeliveryOrderDetail().getDeliveryOrder() != null) {
                                String doNumber = invrow.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber();
                                if (!doReferenceNo.contains(doNumber)) {
                                    doReferenceNo += invrow.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber() + ",";
                                    doReferenceDate = df.format(invrow.getDeliveryOrderDetail().getDeliveryOrder().getOrderDate()) + ",";//Delivery Order Reference Date
                                }
                            }
                        }
//                        String refdateString = df.format(row.getCidetails().getInvoice().getJournalEntry().getEntryDate());
                        String refdateString = df.format(row.getCidetails().getInvoice().getCreationDate());
                        linkedreferencedate = refdateString + ",";
                        transactionTerm = row.getCidetails().getInvoice().getTermid().getTermname();
                    }
                    /*
                        Commented below line as we are not going to fetch sales Person Name directly from Sales Return (SDP-14957)
                    */
//                    salesperson = row.getCidetails().getInvoice().getMasterSalesPerson() != null ? !StringUtil.isNullOrEmpty(row.getCidetails().getInvoice().getMasterSalesPerson().getValue()) ? row.getCidetails().getInvoice().getMasterSalesPerson().getValue() : "" : "";
                    salespersondesignation = row.getCidetails().getInvoice().getMasterSalesPerson() != null ? !StringUtil.isNullOrEmpty(row.getCidetails().getInvoice().getMasterSalesPerson().getDesignation()) ? row.getCidetails().getInvoice().getMasterSalesPerson().getDesignation() : "" : "";
                    if(row.getCidetails().getInvoice()!= null && !StringUtil.isNullOrEmpty(row.getCidetails().getInvoice().getPoRefNumber())){
                        poRefNumbers.add(row.getCidetails().getInvoice().getPoRefNumber());
                    }
                }
                
                salesperson = row.getSalesReturn() != null ? row.getSalesReturn().getSalesperson() != null ? row.getSalesReturn().getSalesperson().getValue() : "" : "";
                salesreturnnumber = row.getSalesReturn() != null ? !StringUtil.isNullOrEmpty(row.getSalesReturn().getSalesReturnNumber()) ? row.getSalesReturn().getSalesReturnNumber() : "" : "";
                rate = row.getRate();
                /**
                 * SDP-14569 : remove authHandler.round() function.
                 */
                rate = authHandler.roundUnitPrice(rate, companyid);
                rowamountwithouttax = rate * returnquantity;
                obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.round(rowamountwithouttax, companyid));//subtotal
                subtotal += authHandler.round(rowamountwithouttax, companyid);
                /*
                 * Discount Section
                 */
                rowdiscountvalue = (row.getDiscountispercent() == 1) ? rowamountwithouttax * row.getDiscount() / 100 : row.getDiscount();
                rowdiscountvalue = authHandler.round(rowdiscountvalue, companyid);
                totalDiscount += rowdiscountvalue;
                if (row.getDiscountispercent() == 1) {
                    discountname = CustomDesignHandler.getAmountinCommaDecimal(row.getDiscount(), 0,countryid) + "%";//to return 0 no of zeros
                } else {
                    discountname = sr.getCurrency().getSymbol() + " " + authHandler.formattingDecimalForAmount(row.getDiscount(), companyid);//to show as it is in UI
                }

                obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                obj.put(CustomDesignerConstants.Discountname, discountname);// Discount
                obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount((rowamountwithouttax - rowdiscountvalue), companyid));//Subtotal with Discount
                    
                subTotalWithDiscount = authHandler.round((rowamountwithouttax - rowdiscountvalue), companyid);
                //Product level Tax
                if (row != null && row.getTax() != null) {
                    rowTaxName = row.getTax().getName();
                    rowTaxAmt = row.getRowTaxAmount();
                totaltaxamount += row.getRowTaxAmount();

                }
                obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmt);//Tax amount
                obj.put(CustomDesignerConstants.SR_ProductTax, rowTaxName);//Product Tax Name

                obj.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, 1);
                obj.put("gstCurrencyRate", "");
                summaryData.put("gstCurrencyRate", "");
                transactionDate = sr.getOrderDate();
                obj.put("transactiondate", transactionDate);
                summaryData.put("transactiondate", transactionDate);
                
                if (externalcurrency != 0) {
                    exchangerateunitprice = (rate * revExchangeRate);  //Exchange Rate Unit Price
                    exchangeratelineitemsubtotal = (rowamountwithouttax * revExchangeRate);//Exchange Rate SubTotal(Rate*Quantity)
                    exchangeratelineitemdiscount = (rowdiscountvalue * revExchangeRate);//exchange rate total discount
                    exchangeratesubtotalwithoutdiscount = (rowamountwithouttax - rowdiscountvalue) * revExchangeRate;//Exchange Rate SubTotal with Discount
                    
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, authHandler.formattingDecimalForUnitPrice(exchangerateunitprice, companyid));
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, authHandler.formattingDecimalForAmount(exchangeratelineitemsubtotal, companyid));
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, authHandler.formattingDecimalForAmount(exchangeratelineitemdiscount, companyid));
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount(exchangeratesubtotalwithoutdiscount, companyid));
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, authHandler.formattingDecimalForUnitPrice(exchangerateunitprice, companyid));
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, authHandler.formattingDecimalForAmount(exchangeratelineitemsubtotal, companyid));
                    obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, authHandler.formattingDecimalForAmount(exchangeratelineitemdiscount, companyid));
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, authHandler.formattingDecimalForAmount(exchangeratesubtotalwithoutdiscount, companyid));
                } else {
                    exchangerateunitprice = rate;
                    exchangeratelineitemsubtotal = rowamountwithouttax;
                    exchangeratelineitemdiscount = rowdiscountvalue;
                    exchangeratesubtotalwithoutdiscount = (rowamountwithouttax - rowdiscountvalue) * revExchangeRate;
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, authHandler.formattingDecimalForUnitPrice(exchangerateunitprice, companyid));
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, authHandler.formattingDecimalForAmount(exchangeratelineitemsubtotal, companyid));
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, authHandler.formattingDecimalForAmount(exchangeratelineitemdiscount, companyid));
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount(exchangeratesubtotalwithoutdiscount, companyid));
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, authHandler.formattingDecimalForUnitPrice(exchangerateunitprice, companyid));
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, authHandler.formattingDecimalForAmount(exchangeratelineitemsubtotal, companyid));
                    obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, authHandler.formattingDecimalForAmount(exchangeratelineitemdiscount, companyid));
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, authHandler.formattingDecimalForAmount(exchangeratesubtotalwithoutdiscount, companyid));
                }

                rowamountwithtax = rowamountwithouttax - rowdiscountvalue + rowTaxAmt; //ERP-26502
                obj.put(CustomDesignerConstants.Amount, authHandler.formattingDecimalForAmount(rowamountwithtax, companyid)); // Amount of single product //ERP-26502

                //Line Item Exchange Rate 
                if (externalcurrency != 0) {
                    exchangeratelineitemamount = (rowamountwithtax * revExchangeRate);//Exchange Rate line item amount
                    exchangeratelineitemtax = (rowTaxAmt * revExchangeRate);
                    
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, authHandler.formattingDecimalForAmount(exchangeratelineitemamount, companyid));//Subtotal
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, authHandler.formattingDecimalForAmount(exchangeratelineitemtax, companyid));//Subtotal
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyAmount, authHandler.formattingDecimalForAmount(exchangeratelineitemamount, companyid));//Subtotal
                    obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, authHandler.formattingDecimalForAmount(exchangeratelineitemtax, companyid));//Subtotal
                } else {
                    exchangeratesubtotalwithoutdiscount = (rate * quantity);//Exchange Rate line item without discount
                    exchangeratelineitemamount = rowamountwithtax;//Exchange Rate line item amount
                    exchangeratelineitemtax = rowTaxAmt;
                    
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount(exchangeratesubtotalwithoutdiscount, companyid));//Subtotal
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, authHandler.formattingDecimalForAmount(exchangeratelineitemamount, companyid));//Subtotal
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, authHandler.formattingDecimalForAmount(exchangeratelineitemtax, companyid));//Subtotal
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, authHandler.formattingDecimalForAmount(exchangeratesubtotalwithoutdiscount, companyid));//Subtotal
                    obj.put(CustomDesignerConstants.SpecificCurrencyAmount, authHandler.formattingDecimalForAmount(exchangeratelineitemamount, companyid));//Subtotal
                    obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, authHandler.formattingDecimalForAmount(exchangeratelineitemtax, companyid));//Subtotal
                }
                
                Uom = row.getUom() == null ? "" : row.getUom().getNameEmptyforNA();
                uomForTotalQuantity = Uom;
                totalQuantity+=returnquantity;
                obj.put(CustomDesignerConstants.SrNO, i);// Sr No
                obj.put(CustomDesignerConstants.ProductName, prodName);// productname
                obj.put(CustomDesignerConstants.IN_ProductCode, prodId);
                obj.put(CustomDesignerConstants.ProductDescription, prodDesc.replaceAll("\n", "<br>"));//Product Description
                obj.put(CustomDesignerConstants.AdditionalDescription, !StringUtil.isNullOrEmpty(row.getProduct().getAdditionalDesc()) ? row.getProduct().getAdditionalDesc().replaceAll(Constants.REGEX_LINE_BREAK, "<br>") :"");  //product Addtional description ERP-39061
                obj.put(CustomDesignerConstants.HSCode, (row.getProduct().getHSCode()!=null) ? row.getProduct().getHSCode().replaceAll("\n", "<br>") : "" );//get HSCode of ProductGrid
                obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate
                obj.put(CustomDesignerConstants.SR_ReturnQuantitywithUOM, authHandler.formattingDecimalForQuantity(returnquantity, companyid) + " " + Uom); // Quantity
                obj.put(CustomDesignerConstants.SR_ReturnQuantity, authHandler.formattingDecimalForQuantity(returnquantity, companyid));
                obj.put(CustomDesignerConstants.SR_ActualQuantitywithUOM, authHandler.formattingDecimalForQuantity(actualquantity, companyid) + " " + Uom); // Quantity
                obj.put(CustomDesignerConstants.SR_ActualQuantity, authHandler.formattingDecimalForQuantity(actualquantity, companyid));
                obj.put(CustomDesignerConstants.SR_Remark, row.getRemark() == null ? "" : row.getRemark());//get Remarks of Product Grid
                obj.put(CustomDesignerConstants.SR_SerialNumber, (row.getPartno() != null) ? row.getPartno() : "");//getSerialnumber of ProductGrid
                obj.put(CustomDesignerConstants.SR_Reason, (row.getReason() != null) ? row.getReason().getValue() : "");//getReason of ProductGrid
                obj.put(CustomDesignerConstants.IN_UOM, Uom);
                obj.put(CustomDesignerConstants.PRODUCTNETWEIGHT, authHandler.formattingDecimalForAmount(row.getProduct().getProductweight(), companyid));//Product Weight
                obj.put(CustomDesignerConstants.IN_Currency, sr.getCurrency().getCurrencyCode());
//                obj.put(CustomDesignerConstants.Amount, authHandler.formattingdecimal(amount, amountdigitafterdecimal)); // Value already inserted above in json //ERP-26502
                obj.put("currencysymbol",sr.getCurrency().getSymbol());
                obj.put("currencycode",sr.getCurrency().getCurrencyCode());
                String baseUrl = URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                /*
                 * Following code is to check whether the image is predent for product or not. 
                 * If Image is not present sent s.gif instead of product id
                 */
                String fileName = null;
                fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+row.getProduct().getID() + ".png";
                File file = new File(fileName);
                FileInputStream in = null; 
                String filePathString = "";
                try {
                    in = new FileInputStream(file);
                } catch(java.io.FileNotFoundException ex) {
                    //catched exception if file not found, and to continue for rest of the products
                   filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                if(in != null){
                    filePathString = baseUrl + "productimage?fname=" + row.getProduct().getID() + ".png&isDocumentDesignerPrint=true";
                } else{
                    filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                obj.put(CustomDesignerConstants.imageTag,filePathString);

                //Calculating Batch & Serial Number of Product                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                    KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), row.getProduct().getID());
                    Product product = (Product) prodresult.getEntityList().get(0);
                    isLocationForProduct = product.isIslocationforproduct();
                    isbatchforproduct = product.isIsBatchForProduct();
                    isserialforproduct = product.isIsSerialForProduct();
                    isWarehouseForProduct = product.isIswarehouseforproduct();

                }
                if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory()
                        || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory()
                        || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {
                    if (isbatchforproduct || isserialforproduct || isLocationForProduct || isWarehouseForProduct) {  //product level batch and serial no on or not but now only location is checked
                        String batchdetails = accInvoiceServiceDAO.getNewBatchJson(row.getProduct(), requestObj, row.getID());
                        JSONArray locjArr = new JSONArray(batchdetails);
                        String location = "";
                        String locationName = "", batchname = "", warehouse = "", serialnumber = "",batchexpdate = "",batchmfgdate = "";
                        String locationnamenew = "", batchnamenew = "", warehousenew = "", serialnumbernew = "",batchexpdatenew= "",batchesmfgdatenew= "";
                        Set<String> batchnames = new LinkedHashSet();
                        Set<String> serialnumbers = new LinkedHashSet();
                        Set<String> locationnames = new LinkedHashSet();
                        Set<String> warehouses = new LinkedHashSet();
                        Set<String> batchesexpirydate = new LinkedHashSet();
                        Set<String> batchesmfgdate = new LinkedHashSet();
                        df = authHandler.getUserDateFormatterWithoutTimeZone(requestObj);//User Date Formatter
                        Date date = null;
                        
                        for (int count = 0; count < locjArr.length(); count++) {
                            JSONObject jSONObject = new JSONObject(locjArr.get(count).toString());
                            location = jSONObject.optString("location", "");
                            batchname = jSONObject.optString("batchname", "");
                            warehouse = jSONObject.optString("warehouse", "");
                            serialnumber = jSONObject.optString("serialno", "");
                            batchmfgdate = jSONObject.optString("mfgdate", "");
                            batchexpdate = jSONObject.optString("expdate", "");
                            serialnumbers.add(serialnumber);
                            batchnames.add(batchname);
                            warehouses.add(warehouse);

                            if (!StringUtil.isNullOrEmpty(location)) {
                                KwlReturnObject loc = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), location);
                                InventoryLocation localist = (InventoryLocation) loc.getEntityList().get(0);
                                locationName = localist.getName();
                                locationnames.add(locationName);
                            }

//                            if (!StringUtil.isNullOrEmpty(batchname)) {
//                                batchname = batchname.concat(",");
//                            }
//                            if (!StringUtil.isNullOrEmpty(serialnumber)) {
//                                serialnumber = serialnumber.concat(",");
//                            }
                            if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
                                date = df1.parse(batchmfgdate);
                                batchmfgdate = df.format(date);
                                if (batchesmfgdate.contains(batchmfgdate)) {
                                    batchesmfgdate.add(" ");
                                } else {
                                    batchesmfgdate.add(batchmfgdate);
                                }
                            } else {
                                batchesmfgdate.add(" ");
                            }
                            if (!StringUtil.isNullOrEmpty(batchexpdate)) {
                                date = df1.parse(batchexpdate);
                                batchexpdate = df.format(date);
                                if (batchesexpirydate.contains(batchexpdate)) {
                                     batchesexpirydate.add(" ");
                                } else {
                                    batchesexpirydate.add(batchexpdate);
                                }
                            } else {
                                batchesexpirydate.add(" ");
                            }
                        }

                        for (String str : batchnames) {
                            String bno = "";
                            bno = str;
                            if (!StringUtil.isNullOrEmpty(bno) && !bno.equals(" ")) {
                                batchnamenew += bno.concat("!##");
                            }
                        }

                        for (String str : serialnumbers) {
                            String sno = "";
                            sno = str;
                            if (!StringUtil.isNullOrEmpty(sno) && !sno.equals(" ")) {
                                serialnumbernew += sno.concat("!##");
                            }
                        }
                        for (String str : locationnames) {
                            String lno = "";
                            lno = str;
                            if (!StringUtil.isNullOrEmpty(lno) && !lno.equals(" ")) {
                                locationnamenew += lno.concat("!##");
                            }
                        }
                        for (String str : batchesexpirydate) {
                            String bexp = "";
                            bexp = str;
                            if (!StringUtil.isNullOrEmpty(bexp) && !bexp.equals(" ")) {
                                batchexpdatenew += bexp.concat("!##");
                            }
                        }
                        for (String str : batchesmfgdate) {
                            String bmfg = "";
                            bmfg = str;
                            if (!StringUtil.isNullOrEmpty(bmfg) && !bmfg.equals(" ")) {
                                batchesmfgdatenew += bmfg.concat("!##");
                            }
                        }


                        if (!StringUtil.isNullOrEmpty(locationnamenew)) {
                            locationnamenew = locationnamenew.substring(0, locationnamenew.length() - 3);
                        }

                        if (!StringUtil.isNullOrEmpty(serialnumbernew)) {
                            serialnumbernew = serialnumbernew.substring(0, serialnumbernew.length() - 3);
                        }

                        if (!StringUtil.isNullOrEmpty(batchnamenew)) {
                            batchnamenew = batchnamenew.substring(0, batchnamenew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(batchesmfgdatenew)) {
                            batchesmfgdatenew = batchesmfgdatenew.substring(0, batchesmfgdatenew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(batchexpdatenew)) {
                            batchexpdatenew = batchexpdatenew.substring(0, batchexpdatenew.length() - 3);
                        }
                        obj.put(CustomDesignerConstants.SerialNumber, serialnumbernew);
                        obj.put(CustomDesignerConstants.BatchNumber, batchnamenew);
                        obj.put(CustomDesignerConstants.IN_Loc, locationnamenew);
                        obj.put(CustomDesignerConstants.ManufacturingDate_Batch, batchesmfgdatenew);// Batch Manufacturing Date
                        obj.put(CustomDesignerConstants.BatchNumberExp, batchexpdatenew);
                        obj.put(CustomDesignerConstants.SR_SerialNumber, serialnumbernew);
                    } else { //if not activated location for product level then take default location for product.
                        obj.put(CustomDesignerConstants.IN_Loc, row.getProduct().getLocation() == null ? "" : row.getProduct().getLocation().getName());
                    }
                } else { //if not activated location for product level then take default location for product.
                    obj.put(CustomDesignerConstants.IN_Loc, row.getProduct().getLocation() == null ? "" : row.getProduct().getLocation().getName());
                }
                
                String lineLevelTax = "";
                String lineLevelTaxPercent = "";
                String lineLevelTaxAmount = "";
                double lineLevelTaxAmountTotal = 0;
                /*
                 * Check for 
                 */
                if(extraCompanyPreferences.isIsNewGST()){ // for New gst check 
                    HashMap<String, Object> SalesReturnDetailParams = new HashMap<String, Object>();
                    SalesReturnDetailParams.put("salesReturnDetailid", row.getID());
                    SalesReturnDetailParams.put("orderbyadditionaltax", true);
                    // GST
                    SalesReturnDetailParams.put("termtype", 7);
                    KwlReturnObject grdTermMapresult = accInvoiceDAOobj.getSalesReturnDetailTermMap(SalesReturnDetailParams);
                    List<SalesReturnDetailsTermMap> gst = grdTermMapresult.getEntityList();
                    obj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.CESSPERCENT, 0);
                    obj.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.TAXABLE_VALUE, subTotalWithDiscount);
                    
                    for (SalesReturnDetailsTermMap salesreturndetailTermMap : gst) {
                        LineLevelTerms mt = salesreturndetailTermMap.getTerm();
                        if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                            obj.put(CustomDesignerConstants.CGSTPERCENT, salesreturndetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CGSTAMOUNT, salesreturndetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, salesreturndetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, salesreturndetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                            obj.put(CustomDesignerConstants.IGSTPERCENT, salesreturndetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.IGSTAMOUNT, salesreturndetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, salesreturndetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, salesreturndetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                            obj.put(CustomDesignerConstants.SGSTPERCENT, salesreturndetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.SGSTAMOUNT, salesreturndetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, salesreturndetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, salesreturndetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                            obj.put(CustomDesignerConstants.UTGSTPERCENT, salesreturndetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.UTGSTAMOUNT, salesreturndetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, salesreturndetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, salesreturndetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                            obj.put(CustomDesignerConstants.CESSPERCENT, salesreturndetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CESSAMOUNT, salesreturndetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, salesreturndetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, salesreturndetailTermMap.getTermamount());
                        }
                        rowTaxAmt += salesreturndetailTermMap.getTermamount();
                        lineLevelTax += mt.getTerm();
                        lineLevelTax += "!## ";
                        lineLevelTaxPercent += authHandler.formattingDecimalForAmount(salesreturndetailTermMap.getPercentage(), companyid);
                        lineLevelTaxPercent += "!## ";
                        lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(salesreturndetailTermMap.getTermamount(), amountdigitafterdecimal,countryid);
                        lineLevelTaxAmount += "!## ";
                        /*
                         * calculating total of line level taxes
                         */
                        lineLevelTaxAmountTotal += salesreturndetailTermMap.getTermamount();
                        totalLineLevelTaxAmount += salesreturndetailTermMap.getTermamount();
                        if (lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm()) != null) {
                            double value = lineLevelTaxNames.get(mt.getTerm());
                            lineLevelTaxNames.put(mt.getTerm(), salesreturndetailTermMap.getTermamount() + value);
                        } else {
                            lineLevelTaxNames.put(mt.getTerm(), salesreturndetailTermMap.getTermamount());
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTax)) {
                        lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxPercent)) {
                        lineLevelTaxPercent = lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxAmount)) {
                        lineLevelTaxAmount = lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length() - 4);
                    }
                    obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                    obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                    obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                    /*
                     *Put line level tax amount SDP-13044 
                     */
                    obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmt); //Tax amount 
                    /*
                     * putting subtotal+tax
                     */
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid) + authHandler.round(lineLevelTaxAmountTotal, companyid));       
                    //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                    ExportRecordHandler.setHsnSacProductDimensionField(row.getProduct(), obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.HSN_SAC_CODE, obj.optString(CustomDesignerConstants.HSN_SAC_CODE, ""));
                    gstTaxSummaryDetailsTableDataArr.put(gstTaxSummaryDetailsTableData);
                } else  {
                    /*
                    * Fetching distinct taxes used at line level, feetched in the set
                    * Also, fetched the information related to tax in different maps
                    */
                    boolean isRowTaxApplicable = false;
                    double rowTaxPercentGST = 0.0;
                    if (row.getTax() != null) {
                        String taxCode = row.getTax().getTaxCode();
                        if (!lineLevelTaxesGST.contains(taxCode)) {
                            lineLevelTaxesGST.add(taxCode);
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, sr.getOrderDate(), row.getTax().getID());
                            rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                            lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                            lineLevelTaxAmountGST.put(taxCode, 0.0);
                            lineLevelTaxBasicGST.put(taxCode, 0.0);
                        }
                        lineLevelTaxAmountGST.put(taxCode,(Double) lineLevelTaxAmountGST.get(taxCode) + row.getRowTaxAmount());
                        lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + obj.optDouble(CustomDesignerConstants.SUBTOTAL,0.0));
                        /*
                         * putting subtotal+tax
                         */
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid) + authHandler.round(row.getRowTaxAmount(), companyid)); 
                    }
                }

//                        /*
//                         * get custom line data
//                         */
                /**
                 * change module id for asset module
                 */
                boolean isFixedAsset = requestObj.optBoolean(Constants.isFixedAsset, false);
                if(isFixedAsset){
                    moduleid = Constants.Acc_FixedAssets_Sales_Return_ModuleId;
                }
                HashMap<String, Object> fieldrequestParams = new HashMap();
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> fieldrequestParamsProduct = new HashMap();
                fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyid,moduleid, 1));
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParamsProduct, replaceFieldMap);

                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParams.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

                fieldrequestParams.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, customfieldFieldMap);
                /**
                 * reset module id
                 */
                if(isFixedAsset){
                    moduleid = Constants.Acc_Sales_Return_ModuleId;
                }
                //For product custom field
                fieldrequestParams.clear();
                HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                HashMap<String, Integer> ProductLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);

                SalesReturnDetailCustomData srDetailCustom = (SalesReturnDetailCustomData) row.getSalesReturnDetailCustomData();
                replaceFieldMap = new HashMap<String, String>();
                if (srDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, srDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }

                SalesReturnDetailProductCustomData srProductDetailCustom = (SalesReturnDetailProductCustomData) row.getSalesReturnDetailProductCustomData();
                replaceFieldMap = new HashMap<String, String>();
                if (srProductDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, srProductDetailCustom, ProductLevelCustomFieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                /*
                 * Set Dimension Values
                 */
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields
                JSONObject jObj = null;
                if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                    jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                    if (jObj.has(Constants.isDisplayUOM) && jObj.get(Constants.isDisplayUOM) != null && (Boolean) jObj.get(Constants.isDisplayUOM) != false) {
                        obj = accProductObj.getProductDisplayUOM(row.getProduct(), row.getReturnQuantity(), row.getBaseuomrate(), false, obj);
                    }
                }
                jArr.put(obj);
            }
           detailsTableData.put("gsttaxsummary", gstTaxSummaryDetailsTableDataArr);
           detailsTableData.put("isDetailsTableData", true);
           jArr.put(detailsTableData);
            if (!StringUtil.isNullOrEmpty(reflinknumber)) {
                reflinknumber = reflinknumber.substring(0, reflinknumber.length() - 1);
            }
            if (!StringUtil.isNullOrEmpty(doReferenceNo)) {
                doReferenceNo = doReferenceNo.substring(0, doReferenceNo.length() - 1);
            }
            mainTax = sr.getTax();
            if (mainTax != null) { //Get Overall Tax percent && total tax amount
                requestParams.put("transactiondate", entryDate);
                requestParams.put("taxid", mainTax.getID());
                KwlReturnObject result = accTaxObj.getTax(requestParams);
                List taxList = result.getEntityList();
                Object[] taxObj = (Object[]) taxList.get(0);
                taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                mainTaxName = mainTax.getName();
                totaltaxamount += (taxPercent == 0 ? 0 : (subtotal - totalDiscount) * taxPercent / 100);//overall tax calculate
            }
           // total = subtotal-totalDiscount+ totaltaxamount;
            
            /*
             * Base Currency values
             */
            total = sr.getTotalamount();
            exchangedTotalAmount = (total * revExchangeRate);  //exchanged rate total amount 
            exchangedSubTotal = (subtotal * revExchangeRate);//exchanged rate sub total amount 
            exchangedTotalTax =   extraCompanyPreferences.isIsNewGST() ? (totalLineLevelTaxAmount * revExchangeRate) : (totaltaxamount * revExchangeRate);//exchanged rate total tax amount 
            exchangedSubTotalwithDiscount= (subtotal-totalDiscount) * revExchangeRate;//exchanged rate subtotal with discount
            
            String netinword = EnglishNumberToWordsOjb.convert(total, sr.getCurrency(),countryLanguageId) + " Only.";
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if(countryid == Constants.INDONESIAN_COUNTRY_ID){
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(total)), indoCurrency);
            }
//            JSONObject summaryData = new JSONObject();
            /*
             * All Global Section Custom Field and DImensions
             */
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            df = authHandler.getUserDateFormatterJson(requestObj);//User Date Formatter
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, Constants.Acc_Sales_Return_ModuleId);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", sr.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            /*
             * Check for 
             */
            if(extraCompanyPreferences.isIsNewGST()){ // for New gst check  
                    if (!lineLevelTaxNames.isEmpty()) {
                        Iterator lineTax = lineLevelTaxNames.entrySet().iterator();
                        while (lineTax.hasNext()) {
                            Map.Entry tax = (Map.Entry) lineTax.next();
                            allLineLevelTax += tax.getKey();
                            allLineLevelTax += "!## ";
                            double taxamount = (double) tax.getValue();
                            allLineLevelTaxAmount += tax.getValue().toString();
                            TotalLineLevelTaxAmount += taxamount;
                            allLineLevelTaxAmount += "!## ";
                            if (taxamount > 0) {
                                lineLevelTaxSign += "+";
                                lineLevelTaxSign += "!## ";
                            } else {
                                lineLevelTaxSign += "-&nbsp;";
                                lineLevelTaxSign += "!## ";
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(allLineLevelTax)) {
                        allLineLevelTax = allLineLevelTax.substring(0, allLineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(allLineLevelTaxAmount)) {
                        allLineLevelTaxAmount = allLineLevelTaxAmount.substring(0, allLineLevelTaxAmount.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxSign)) {
                        lineLevelTaxSign = lineLevelTaxSign.substring(0, lineLevelTaxSign.length() - 4);
                    }
                //Add in Total Tax amount.
                totaltaxamount = totaltaxamount + TotalLineLevelTaxAmount;
                
                String buyerexcRegNo="", buyetinNo="", buyerpanNo="", buyerRange="", buyerDivision="", buyerComrate="", buyerServiceTaxRegNo="";
                
                // Customer Related indian details
                String panStatus = "", IECNo = "", CSTDateStr = "", VATDateStr = "", dealerTypeStr = "";
                String ImporterECCNo = "",  typeOfSales = "";

                if (sr.getCustomer().getPanStatus() != null && !StringUtil.isNullOrEmpty(sr.getCustomer().getPanStatus())) {
                    panStatus = sr.getCustomer().getPanStatus().equals(IndiaComplianceConstants.PAN_STATUS_PANNOTAVBL) ? IndiaComplianceConstants.PAN_NOT_AVAILABLE : sr.getCustomer().getPanStatus().equals(IndiaComplianceConstants.PAN_STATUS_APPLIEDFOR) ? IndiaComplianceConstants.PAN_APPLIED_FOR : "";
                }
                if(sr.getCustomer().getDefaultnatureOfPurchase()!= null && !StringUtil.isNullOrEmpty(sr.getCustomer().getDefaultnatureOfPurchase())){
                    KwlReturnObject naturetyperesult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), sr.getCustomer().getDefaultnatureOfPurchase());
                    MasterItem natureTypeObj = (MasterItem) naturetyperesult.getEntityList().get(0);
                    if(natureTypeObj != null){
                        typeOfSales = natureTypeObj.getValue();
                    }
                }

                if (sr.getCustomer().getDealertype() != null && !StringUtil.isNullOrEmpty(sr.getCustomer().getDealertype())) {
                    String dealerType = sr.getCustomer().getDealertype();
                    if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_REGISTERED)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_REGISTERED_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_UNREGISTERED)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_UNREGISTERED_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_1_2)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_1_2_STR;
                    }
                }
                if (sr.getCustomer().getCSTRegDate() != null) {
                    Date CSTDate = sr.getCustomer().getCSTRegDate();
                    CSTDateStr = CSTDate != null ? authHandler.getUserDateFormatterJson(requestObj).format(CSTDate) : "";
                }
                if (sr.getCustomer().getVatregdate() != null) {
                    Date VATDate = sr.getCustomer().getVatregdate();
                    VATDateStr = VATDate != null ? authHandler.getUserDateFormatterJson(requestObj).format(VATDate) : "";
                }

                buyerComrate = sr.getCustomer().getCommissionerate() != null ? sr.getCustomer().getCommissionerate() : "";
                buyerDivision = sr.getCustomer().getDivision() != null ? sr.getCustomer().getDivision() : "";
                buyerRange = sr.getCustomer().getRangecode() != null ? sr.getCustomer().getRangecode() : "";
                buyetinNo = sr.getCustomer().getVATTINnumber() != null ? sr.getCustomer().getVATTINnumber() : "";
                buyerexcRegNo = sr.getCustomer().getECCnumber() != null ? sr.getCustomer().getECCnumber() : "";
                buyerpanNo = sr.getCustomer().getPANnumber() != null ? sr.getCustomer().getPANnumber() : "";
                buyerServiceTaxRegNo = sr.getCustomer().getSERVICEnumber() != null ? sr.getCustomer().getSERVICEnumber() : "";
                ImporterECCNo = (sr.getCustomer() != null && sr.getCustomer().getImporterECCNo() != null) ? sr.getCustomer().getImporterECCNo() : "";
                IECNo = (sr.getCustomer() != null && sr.getCustomer().getIECNo() != null) ? sr.getCustomer().getIECNo() : "";

                // ************************   Customer Related Information **********************************************
                summaryData.put(CustomDesignerConstants.CUSTOMER_PAN_NO, buyerpanNo);
                summaryData.put(CustomDesignerConstants.CustomerVendor_PAN_STATAUS, panStatus);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, buyetinNo);
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, sr.getCustomer().getCSTTINnumber() != null ? sr.getCustomer().getCSTTINnumber() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_REG_DATE, CSTDateStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_REG_DATE, VATDateStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_DEALER_TYPE, dealerTypeStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_INTERSTATEPARTY, sr.getCustomer().isInterstateparty() ? "Yes" : "No");
                summaryData.put(CustomDesignerConstants.CustomerVendor_C_FORM_APPLICABLE, sr.getCustomer().isCformapplicable() ? "Yes" : "No");
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_TYPE_OF_SALES, typeOfSales);
                summaryData.put(CustomDesignerConstants.CustomerVendor_ECC_NO, buyerexcRegNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_IMPORTER_ECC_NUMBER, ImporterECCNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_IEC_NUMBER, IECNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_RANGE_CODE, buyerRange);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_DIVISION_CODE, buyerDivision);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_COMMISSIONERATE_CODE, buyerComrate);
                summaryData.put(CustomDesignerConstants.CUSTOMER_SERVICE_TAX_REG_NO, buyerServiceTaxRegNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, sr.getCustomer().getGSTIN() != null ? sr.getCustomer().getGSTIN() : "");
                // ****************************************************************************************************
            } else {
                /*
                * Putting all line taxes and its information in summary data separated by !##
                */
                for ( String key : lineLevelTaxesGST ) {
                    allLineLevelTax += key + "!##";
                    allLineLevelTaxAmount +=  lineLevelTaxAmountGST.get(key).toString() + "!##";
                    allLineLevelTaxBasic += lineLevelTaxBasicGST.get(key).toString() + "!##";
                }
            }
            if(!poRefNumbers.isEmpty()){
                poRefNo = poRefNumbers.toString();
                poRefNo = poRefNo.substring(1, poRefNo.length());
                poRefNo = poRefNo.replaceAll(", ","!## ");
            }
            String userId = requestObj.optString(Constants.useridKey);
            KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userresult.getEntityList().get(0);
            //Details like company details,base currency
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);
            
            String gstAmountInWords = EnglishNumberToWordsOjb.convert(totaltaxamount, sr.getCurrency(),countryLanguageId) + Constants.ONLY;
            summaryData.put("summarydata", true);
            if (!linkedaddress) {
                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put("customerid", sr.getCustomer().getID());
                addrRequestParams.put("companyid", companyid);
                KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
                List<AddressDetails> addressResultList = addressResult.getEntityList();
                CommonFunctions.getAddressSummaryData(addressResultList, summaryData,companyAccountPreferences, extraCompanyPreferences);
                addrRequestParams.put("isDefaultAddress", true);
                addrRequestParams.put("isBillingAddress", true);
                addrRequestParams.put("isSeparator", true);
                billAddr = accountingHandlerDAOobj.getCustomerAddress(addrRequestParams);
                addrRequestParams.put("isBillingAddress", false);
                shipAddr = accountingHandlerDAOobj.getCustomerAddress(addrRequestParams);
            }else {
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, linkedshippingaddresscp);//ERP-14642
                //Billing Address
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, linkedbillAddr);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, linkedbilladdresscity);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, linkedbilladdressstate);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, linkedbilladdresscountry);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, linkedbilladdresspincode);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, linkedbilladdressphone);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingMobileNo_fieldTypeId, linkedbilladdressmobile);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, linkedbilladdressemail);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, linkedbilladdressfax);
                //Shipping Address
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, linkedshipAddr);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, linkedshipaddresscity);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, linkedshipaddressstate);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, linkedshipaddresscountry);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, linkedshipaddresspincode);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, linkedshipaddressphone);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingMobileNo_fieldTypeId, linkedshipaddressmobile);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, linkedshipaddressemail);
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, linkedshipaddressfax);
            } 
            String systemcurrencysymbol = sr.getCustomer().getCurrency().getSymbol();
            String customcurrencysymbol = accCommonTablesDAO.getCustomCurrencySymbol(systemcurrencysymbol, companyid);//Take custom currency symbol
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, authHandler.formattedAmount(total, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, authHandler.formattedAmount((subtotal-totalDiscount), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, authHandler.formattedAmount(((subtotal - totalDiscount) + totaltaxamount), companyid)); //ERP-25162
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsDescription_fieldTypeId, custmerterms);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, authHandler.formattedAmount(totaltaxamount, companyid));
            summaryData.put(CustomDesignerConstants.SummaryTaxPercent, taxPercent);
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword);
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, sr.getPostText() == null ? "" : sr.getPostText());
            summaryData.put(CustomDesignerConstants.Posttext, sr.getPostText() == null ? "" : sr.getPostText());  //Post Text
            summaryData.put(CustomDesignerConstants.CustomerVendor_Term, custmerterms);
            summaryData.put(CustomDesignerConstants.CustomerVendor_Code, Accountcode);
            summaryData.put(CustomDesignerConstants.CustomDesignCN_InvoiceNo, creditnotenumber);
            summaryData.put(CustomDesignerConstants.SR_LinkTo,reflinknumber);
            summaryData.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, customcurrencysymbol);
            summaryData.put(CustomDesignerConstants.BillTo, billAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.ShipTo, shipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.AppendRequestType,requestType);
            summaryData.put(CustomDesignerConstants.Createdby, sr.getCreatedby()!=null?sr.getCreatedby().getFullName():"");
            summaryData.put(CustomDesignerConstants.Updatedby, updatedby);
            summaryData.put(CustomDesignerConstants.TotalQuantity, totalQuantity);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, totalDiscount);
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.SalesReturnNumber, salesreturnnumber);
            summaryData.put(CustomDesignerConstants.CustomerVendor_AccountCode,sr.getCustomer().getAccount()!=null?(sr.getCustomer().getAccount().getAcccode()!=null?sr.getCustomer().getAccount().getAcccode():""):"");

            /*Exchange Rate Calculations of SubTotal,Subtotal withDiscount Total Tax and Total Amount*/
            summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, authHandler.formattedAmount(exchangedTotalAmount, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, authHandler.formattedAmount(exchangedSubTotal, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, authHandler.formattedAmount(exchangedTotalTax, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, authHandler.formattedAmount(exchangedSubTotalwithDiscount, companyid));
            
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, 1);
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, authHandler.formattedAmount(exchangedTotalAmount, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, authHandler.formattedAmount(exchangedSubTotal, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, authHandler.formattedAmount(exchangedTotalTax, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, authHandler.formattedAmount(exchangedSubTotalwithDiscount, companyid));
            
            summaryData.put(CustomDesignerConstants.LinkedSalesPerson, salesperson);
            summaryData.put(CustomDesignerConstants.LinkedSalesPersonDesignation,salespersondesignation);
            summaryData.put(CustomDesignerConstants.LinkedReferenceDate,!StringUtil.isNullOrEmpty(linkedreferencedate)? linkedreferencedate.substring(0, linkedreferencedate.length() - 1):"");
            summaryData.put(CustomDesignerConstants.CustomDesignDORef_Date_fieldTypeId,!StringUtil.isNullOrEmpty(doReferenceDate)? doReferenceDate.substring(0, doReferenceDate.length() - 1):"");
            summaryData.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId,doReferenceNo);
            
            summaryData.put(CustomDesignerConstants.CurrentUserFirstName, !StringUtil.isNullOrEmpty(user.getFirstName())?user.getFirstName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserLastName, !StringUtil.isNullOrEmpty(user.getLastName())?user.getLastName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName())?user.getFullName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserEmail, !StringUtil.isNullOrEmpty(user.getEmailID())?user.getEmailID():"");
            summaryData.put(CustomDesignerConstants.CurrentUserAddress, !StringUtil.isNullOrEmpty(user.getAddress())?user.getAddress().replaceAll(Constants.REGEX_LINE_BREAK, "<br>"):"");
            summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber())?user.getContactNumber():"");
            summaryData.put(CustomDesignerConstants.CUSTOMER_TITLE, customerTitle);
//            if(countryid == Constants.indian_country_id) {
            summaryData.put(CustomDesignerConstants.AllLineLevelTax, allLineLevelTax);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxAmount, allLineLevelTaxAmount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTermSigns, lineLevelTaxSign);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxBasic, allLineLevelTaxBasic);
            summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, sr.getCustomer().getVATTINnumber()!= null ? sr.getCustomer().getVATTINnumber() : "");
            summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, sr.getCustomer().getCSTTINnumber()!= null ? sr.getCustomer().getCSTTINnumber() : "");
            summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber()!= null ? extraCompanyPreferences.getVatNumber() : "");
            summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber()!= null ? extraCompanyPreferences.getCstNumber() : "");
            summaryData.put(CustomDesignerConstants.CompanyPANNumber, extraCompanyPreferences.getPanNumber()!= null ? extraCompanyPreferences.getPanNumber() : "");
            summaryData.put(CustomDesignerConstants.CUSTOMER_PAN_NO, sr.getCustomer().getPANnumber()!= null ? sr.getCustomer().getPANnumber() : "");
            summaryData.put(CustomDesignerConstants.Poreferencenumber, !StringUtil.isNullOrEmpty(poRefNo)?poRefNo.substring(0, poRefNo.length()-1) :"");
            summaryData.put(CustomDesignerConstants.Total_Quantity_UOM, authHandler.formattingDecimalForQuantity(totalQuantity, companyid) +" "+ uomForTotalQuantity);
            summaryData.put(CustomDesignerConstants.TAXNAME, mainTaxName);
            summaryData.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, !StringUtil.isNullOrEmpty(transactionTerm)?transactionTerm:sr.getCustomer().getCreditTerm().getTermname());
            summaryData.put(CustomDesignerConstants.GSTAmountInWords, gstAmountInWords);
            int deliveryDate = 0;
            String deliveryDateVal = "";
            String driver = "";
            String vehicleNo = "";
            String deliveryTime = "";
            if (extraCompanyPreferences.isDeliveryPlanner()) {
                deliveryDate = sr.getCustomer() != null ? sr.getCustomer().getDeliveryDate():-1;
                deliveryDateVal = getDeliverDayVal(deliveryDate);
                deliveryTime = (sr.getCustomer() != null && sr.getCustomer().getDeliveryTime() != null) ? sr.getCustomer().getDeliveryTime():"";
                driver = (sr.getCustomer() != null && sr.getCustomer().getDriver() != null)? sr.getCustomer().getDriver().getValue():"";
                vehicleNo = (sr.getCustomer() != null && sr.getCustomer().getVehicleNo() != null)? sr.getCustomer().getVehicleNo().getValue():""; 
            }
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, deliveryDateVal);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, deliveryTime);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDriver, driver);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo,vehicleNo);
//            }
            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }   
   public String getDeliverDayVal(int day) {
       String returnVAl = "";
       switch (day) {
                case 0:
                    returnVAl = "Every Sunday";
                    break;
                case 1:
                    returnVAl = "Every Monday";
                    break;
                case 2:
                    returnVAl = "Every Tuesday";
                    break;
                case 3:
                    returnVAl = "Every Wednesday";
                    break;
                case 4:
                    returnVAl = "Every Thursday";
                    break;
                case 5:
                    returnVAl = "Every Friday";
                    break;
                case 6:
                    returnVAl = "Every Saturday";
                    break;
                case 7:
                    returnVAl = "Next Day";
                    break;
       }
       return returnVAl;
   }
}
