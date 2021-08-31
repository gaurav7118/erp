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
package com.krawler.spring.accounting.companypreferances;

import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class accCompanyPreferencesImpl extends BaseDAO implements accCompanyPreferencesDAO {
    
    public String getNextAutoNumber(String companyid, int from) throws ServiceException, AccountingException {
        String autoNumber = "";
        String table = "", field = "", pattern = "";
        CompanyAccountPreferences pref = (CompanyAccountPreferences) get(CompanyAccountPreferences.class, companyid);
        if (pref == null) {
            return autoNumber;
        }

        switch (from) {
            case StaticValues.AUTONUM_JOURNALENTRY:
                table = "JournalEntry";
                field = "entryNumber";
                //pattern=pref.getJournalEntryNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getJournalEntryNumberFormat())) {
                    pattern = pref.getJournalEntryNumberFormat().split(",")[0];
                }

                if (StringUtil.isNullOrEmpty(pattern)) {
                    pattern = "JE000000";      //It is used for default value
                }
                break;
            case StaticValues.AUTONUM_SALESORDER:
                table = "SalesOrder";
                field = "salesOrderNumber";
                // pattern=pref.getSalesOrderNumberFormat();

                if (!StringUtil.isNullOrEmpty(pref.getSalesOrderNumberFormat())) {
                    pattern = pref.getSalesOrderNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_INVOICE:
                table = "Invoice";
                field = "invoiceNumber";
                //pattern=pref.getInvoiceNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getInvoiceNumberFormat())) {
                    pattern = pref.getInvoiceNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_CASHSALE:
                table = "Invoice";
                field = "invoiceNumber";
                //pattern=pref.getCashSaleNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getCashSaleNumberFormat())) {
                    pattern = pref.getCashSaleNumberFormat().split(",")[0];
                }

                break;
            case StaticValues.AUTONUM_CREDITNOTE:
                table = "CreditNote";
                field = "creditNoteNumber";
                //pattern=pref.getCreditNoteNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getCreditNoteNumberFormat())) {
                    pattern = pref.getCreditNoteNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_RECEIPT:
                table = "Receipt";
                field = "receiptNumber";
                //pattern=pref.getReceiptNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getReceiptNumberFormat())) {
                    pattern = pref.getReceiptNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_PURCHASEORDER:
                table = "PurchaseOrder";
                field = "purchaseOrderNumber";
                //pattern=pref.getPurchaseOrderNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getPurchaseOrderNumberFormat())) {
                    pattern = pref.getPurchaseOrderNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_GOODSRECEIPT:
                table = "GoodsReceipt";
                field = "goodsReceiptNumber";
                //pattern=pref.getGoodsReceiptNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getGoodsReceiptNumberFormat())) {
                    pattern = pref.getGoodsReceiptNumberFormat().split(",")[0];
                }

                break;
            case StaticValues.AUTONUM_CASHPURCHASE:
                table = "GoodsReceipt";
                field = "goodsReceiptNumber";
                // pattern=pref.getCashPurchaseNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getCashPurchaseNumberFormat())) {
                    pattern = pref.getCashPurchaseNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_DEBITNOTE:
                table = "DebitNote";
                field = "debitNoteNumber";
                // pattern=pref.getDebitNoteNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getDebitNoteNumberFormat())) {
                    pattern = pref.getDebitNoteNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_PAYMENT:
                table = "Payment";
                field = "paymentNumber";
                // pattern=pref.getPaymentNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getPaymentNumberFormat())) {
                    pattern = pref.getPaymentNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_BILLINGINVOICE:
                table = "BillingInvoice";
                field = "billingInvoiceNumber";
                //pattern=pref.getBillingInvoiceNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getBillingInvoiceNumberFormat())) {
                    pattern = pref.getBillingInvoiceNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_BILLINGRECEIPT:
                table = "BillingReceipt";
                field = "billingReceiptNumber";
                //pattern=pref.getBillingReceiptNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getBillingReceiptNumberFormat())) {
                    pattern = pref.getBillingReceiptNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_BILLINGCASHSALE:
                table = "BillingInvoice";
                field = "billingInvoiceNumber";
                // pattern=pref.getBillingCashSaleNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getBillingCashSaleNumberFormat())) {
                    pattern = pref.getBillingCashSaleNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_BILLINGGOODSRECEIPT:
                table = "BillingGoodsReceipt";
                field = "billingGoodsReceiptNumber";
                //pattern=pref.getBillingGoodsReceiptNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getBillingGoodsReceiptNumberFormat())) {
                    pattern = pref.getBillingGoodsReceiptNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_BILLINGPAYMENT:
                table = "BillingPayment";
                field = "billingPaymentNumber";
                //pattern=pref.getBillingPaymentNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getBillingPaymentNumberFormat())) {
                    pattern = pref.getBillingPaymentNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_BILLINGCASHPURCHASE:
                table = "BillingGoodsReceipt";
                field = "billingGoodsReceiptNumber";
                //pattern=pref.getBillingCashPurchaseNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getBillingCashPurchaseNumberFormat())) {
                    pattern = pref.getBillingCashPurchaseNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_BILLINGPURCHASEORDER:
                table = "BillingPurchaseOrder";
                field = "purchaseOrderNumber";
                // pattern=pref.getBillingPurchaseOrderNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getBillingPurchaseOrderNumberFormat())) {
                    pattern = pref.getBillingPurchaseOrderNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_BILLINGSALESORDER:
                table = "BillingSalesOrder";
                field = "salesOrderNumber";
                //pattern=pref.getBillingSalesOrderNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getBillingSalesOrderNumberFormat())) {
                    pattern = pref.getBillingSalesOrderNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_BILLINGDEBITNOTE:
                table = "BillingDebitNote";
                field = "debitNoteNumber";
                //pattern=pref.getBillingDebitNoteNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getBillingDebitNoteNumberFormat())) {
                    pattern = pref.getBillingDebitNoteNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_BILLINGCREDITNOTE:
                table = "BillingCreditNote";
                field = "creditNoteNumber";
                //pattern=pref.getBillingCreditNoteNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getBillingCreditNoteNumberFormat())) {
                    pattern = pref.getBillingCreditNoteNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_QUOTATION:
                table = "Quotation";
                field = "quotationNumber";
                //pattern=pref.getQuotationNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getQuotationNumberFormat())) {
                    pattern = pref.getQuotationNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_VENQUOTATION:
                table = "VendorQuotation";
                field = "quotationNumber";
                // pattern=pref.getVenQuotationNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getVenQuotationNumberFormat())) {
                    pattern = pref.getVenQuotationNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_REQUISITION:
                table = "PurchaseRequisition";
                field = "prNumber";
                //pattern=pref.getRequisitionNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getRequisitionNumberFormat())) {
                    pattern = pref.getRequisitionNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_RFQ:
                table = "RequestForQuotation";
                field = "rfqNumber";
                //pattern=pref.getRfqNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getRfqNumberFormat())) {
                    pattern = pref.getRfqNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_PRODUCTID:
                table = "Product";
                field = "productid";
                // pattern=pref.getProductidNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getProductidNumberFormat())) {
                    pattern = pref.getProductidNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_DELIVERYORDER:
                table = "DeliveryOrder";
                field = "deliveryOrderNumber";
                //pattern=pref.getDeliveryOrderNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getDeliveryOrderNumberFormat())) {
                    pattern = pref.getDeliveryOrderNumberFormat().split(",")[0];
                }
                if (StringUtil.isNullOrEmpty(pattern)) {
                    pattern = "DO000000";      //It is used for default value
                }
                break;
            case StaticValues.AUTONUM_GOODSRECEIPTORDER:
                table = "GoodsReceiptOrder";
                field = "goodsReceiptOrderNumber";
                //pattern=pref.getGoodsReceiptOrderNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getGoodsReceiptOrderNumberFormat())) {
                    pattern = pref.getGoodsReceiptOrderNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_SALESRETURN:
                table = "SalesReturn";
                field = "salesReturnNumber";
                // pattern=pref.getSalesReturnNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getSalesReturnNumberFormat())) {
                    pattern = pref.getSalesReturnNumberFormat().split(",")[0];
                }
                break;
            case StaticValues.AUTONUM_PURCHASERETURN:
                table = "PurchaseReturn";
                field = "purchaseReturnNumber";
                // pattern=pref.getPurchaseReturnNumberFormat();
                if (!StringUtil.isNullOrEmpty(pref.getPurchaseReturnNumberFormat())) {
                    pattern = pref.getPurchaseReturnNumberFormat().split(",")[0];
                }
                break;
        }
        if (StringUtil.isNullOrEmpty(pattern)) {
            return autoNumber;
        }

        String query = "";
        if (StringUtil.equal(table, "Product")) {
            query = "select max(" + field + ") from " + table + " where " + field + "like ? and  company.companyID =  ?";
        } else {
            query = "select max(" + field + ") from " + table + " where autoGenerated = true and " + field + " like ? and  company.companyID =  ?";
        }

        List list = executeQuery( query, new Object[]{pattern.replace('0', '_'), pref.getID()});
        if (list.isEmpty() == false) {
            autoNumber = (String) list.get(0);
        }

        while (!pattern.equals(autoNumber)) {
            autoNumber = AccountingManager.generateNextAutoNumber(pattern, autoNumber);
            query = "select " + field + " from " + table + " where " + field + " = ? and company.companyID=?";
            list = executeQuery( query, new Object[]{autoNumber, pref.getID()});
            if (list.isEmpty()) {
                return autoNumber;
            }
        }
        throw new AccountingException("Auto number for the pattern '" + pattern + "' doesn't exist.<br>Please change the pattern or disable Auto generation.");

    }

    public String getNextAutoNumber(String companyid, int from, String format) throws ServiceException, AccountingException {
        String autoNumber = "";
        String table = "", field = "", pattern = "";
        String sqltable = "", sqlfield = "";
        int startfrom = 1;
        String istemplatecondition="";
        boolean seqformatidChk = false;
        CompanyAccountPreferences pref = (CompanyAccountPreferences) get(CompanyAccountPreferences.class, companyid);
        if (pref == null) {
            return autoNumber;
        }

        switch (from) {
            case StaticValues.AUTONUM_JOURNALENTRY:
                table = "JournalEntry";
                field = "entryNumber";
                sqltable = "journalentry";
                sqlfield = "entryno";
                startfrom = pref.getJournalEntryNumberFormatStartFrom();
                pattern = format;
                istemplatecondition=" and istemplate!= 2";
                break;
            case StaticValues.AUTONUM_SALESORDER:
                table = "SalesOrder";
                field = "salesOrderNumber";
                sqltable = "salesorder";
                sqlfield = "sonumber";
                startfrom = pref.getSalesOrderNumberFormatStartFrom();
                istemplatecondition=" and istemplate!= 2";
                pattern = format;
                break;
            case StaticValues.AUTONUM_CONTRACT:
                table = "Contract";
                field = "contractNumber";
                sqltable = "contract";
                sqlfield = "contractnumber";
                startfrom = 1;
                pattern = format;
                break;
            case StaticValues.AUTONUM_INVOICE:
                table = "Invoice";
                field = "invoiceNumber";
                sqltable = "invoice";
                sqlfield = "invoicenumber";
                startfrom = pref.getInvoiceNumberFormatStartFrom();
                istemplatecondition=" and istemplate!=2 ";
                pattern = format;
                seqformatidChk = true;
                break;
            case StaticValues.AUTONUM_CASHSALE:
                table = "Invoice";
                field = "invoiceNumber";
                sqltable = "invoice";
                sqlfield = "invoicenumber";
                startfrom = pref.getCashSaleNumberFormatStartFrom();
                istemplatecondition=" and istemplate!=2 ";
                pattern = format;
                seqformatidChk = true;
                break;
            case StaticValues.AUTONUM_CREDITNOTE:
                table = "CreditNote";
                field = "creditNoteNumber";
                sqltable = "creditnote";
                sqlfield = "cnnumber";
                startfrom = pref.getCreditNoteNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_RECEIPT:
                table = "Receipt";
                field = "receiptNumber";
                sqltable = "receipt";
                sqlfield = "receiptnumber";
                startfrom = pref.getReceiptNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_PURCHASEORDER:
                table = "PurchaseOrder";
                field = "purchaseOrderNumber";
                sqltable = "purchaseorder";
                sqlfield = "ponumber";
                startfrom = pref.getPurchaseOrderNumberFormatStartFrom();
                istemplatecondition=" and istemplate!=2 ";
                pattern = format;
                break;
            case StaticValues.AUTONUM_GOODSRECEIPT:
                seqformatidChk = true;
                table = "GoodsReceipt";
                field = "goodsReceiptNumber";
                sqltable = "goodsreceipt";
                sqlfield = "grnumber";
                startfrom = pref.getGoodsReceiptNumberFormatStartFrom();
                istemplatecondition=" and istemplate!=2 ";
                pattern = format;
                break;
            case StaticValues.AUTONUM_CASHPURCHASE:
                table = "GoodsReceipt";
                field = "goodsReceiptNumber";
                sqltable = "goodsreceipt";
                sqlfield = "grnumber";
                startfrom = pref.getCashPurchaseNumberFormatStartFrom();
                istemplatecondition=" and istemplate!=2 ";
                pattern = format;
                break;
            case StaticValues.AUTONUM_DEBITNOTE:
                table = "DebitNote";
                field = "debitNoteNumber";
                sqltable = "debitnote";
                sqlfield = "dnnumber";
                startfrom = pref.getDebitNoteNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_PAYMENT:
                table = "Payment";
                field = "paymentNumber";
                sqltable = "payment";
                sqlfield = "paymentnumber";
                startfrom = pref.getPaymentNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGINVOICE:
                table = "BillingInvoice";
                field = "billingInvoiceNumber";
                sqltable = "billinginvoice";
                sqlfield = "billinginvoicenumber";
                startfrom = pref.getBillingInvoiceNumberFormatStartFrom();
                pattern = format;
                seqformatidChk = true;
                break;
            case StaticValues.AUTONUM_BILLINGRECEIPT:
                table = "BillingReceipt";
                field = "billingReceiptNumber";
                sqltable = "billingreceipt";
                sqlfield = "billingreceiptnumber";
                startfrom = pref.getBillingReceiptNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGCASHSALE:
                table = "BillingInvoice";
                field = "billingInvoiceNumber";
                sqltable = "billinginvoice";
                sqlfield = "billinginvoicenumber";
                startfrom = pref.getBillingCashSaleNumberFormatStartFrom();
                pattern = format;
                seqformatidChk = true;
                break;
            case StaticValues.AUTONUM_BILLINGGOODSRECEIPT:
                table = "BillingGoodsReceipt";
                field = "billingGoodsReceiptNumber";
                sqltable = "billinggr";
                sqlfield = "billinggrnumber";
                startfrom = pref.getBillingGoodsReceiptNumberFormatStartFrom();
                pattern = format;
                seqformatidChk = true;
                break;
            case StaticValues.AUTONUM_BILLINGPAYMENT:
                table = "BillingPayment";
                field = "billingPaymentNumber";
                sqltable = "billingpayment";
                sqlfield = "billingpaymentnumber";
                startfrom = pref.getBillingPaymentNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGCASHPURCHASE:
                table = "BillingGoodsReceipt";
                field = "billingGoodsReceiptNumber";
                sqltable = "billinggr";
                sqlfield = "billinggrnumber";
                startfrom = pref.getBillingCashPurchaseNumberFormatStartFrom();
                pattern = format;
                seqformatidChk = true;
                break;
            case StaticValues.AUTONUM_BILLINGPURCHASEORDER:
                table = "BillingPurchaseOrder";
                field = "purchaseOrderNumber";
                sqltable = "billingpurchaseorder";
                sqlfield = "ponumber";
                startfrom = pref.getBillingPurchaseOrderNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGSALESORDER:
                table = "BillingSalesOrder";
                field = "salesOrderNumber";
                sqltable = "billingsalesorder";
                sqlfield = "sonumber";
                startfrom = pref.getBillingSalesOrderNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGDEBITNOTE:
                table = "BillingDebitNote";
                field = "debitNoteNumber";
                sqltable = "billingdebitnote";
                sqlfield = "dnnumber";
                startfrom = pref.getBillingDebitNoteNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGCREDITNOTE:
                table = "BillingCreditNote";
                field = "creditNoteNumber";
                sqltable = "billingcreditnote";
                sqlfield = "cnnumber";
                startfrom = pref.getBillingCreditNoteNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_QUOTATION:
                table = "Quotation";
                field = "quotationNumber";
                sqltable = "quotation";
                sqlfield = "quotationnumber";
                startfrom = pref.getQuotationNumberFormatStartFrom();
                istemplatecondition=" and istemplate!=2 ";
                pattern = format;
                break;
            case StaticValues.AUTONUM_VENQUOTATION:
                table = "VendorQuotation";
                field = "quotationNumber";
                sqltable = "vendorquotation";
                sqlfield = "quotationnumber";
                startfrom = pref.getVenQuotationNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_REQUISITION:
                table = "PurchaseRequisition";
                field = "prNumber";
                sqltable = "purchaserequisition";
                sqlfield = "prnumber";
                startfrom = pref.getRequisitionNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_RFQ:
                table = "RequestForQuotation";
                field = "rfqNumber";
                sqltable = "requestforquotation";
                sqlfield = "rfqnumber";
                startfrom = pref.getRfqNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_PRODUCTID:
                table = "Product";
                field = "productid";
                sqltable = "product";
                sqlfield = "productid";
                startfrom = pref.getProductidNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_DELIVERYORDER:
                table = "DeliveryOrder";
                field = "deliveryOrderNumber";
                sqltable = "deliveryorder";
                sqlfield = "donumber";
                startfrom = pref.getDeliveryOrderNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_GOODSRECEIPTORDER:
                table = "GoodsReceiptOrder";
                field = "goodsReceiptOrderNumber";
                sqltable = "grorder";
                sqlfield = "gronumber";
                startfrom = pref.getGoodsReceiptOrderNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_SALESRETURN:
                table = "SalesReturn";
                field = "salesReturnNumber";
                sqltable = "salesreturn";
                sqlfield = "srnumber";
                startfrom = pref.getSalesReturnNumberFormatStartFrom();
                pattern = format;
                break;
            case StaticValues.AUTONUM_PURCHASERETURN:
                table = "PurchaseReturn";
                field = "purchaseReturnNumber";
                sqltable = "purchasereturn";
                sqlfield = "prnumber";
                startfrom = pref.getPurchaseReturnNumberFormatStartFrom();
                pattern = format;
                break;
        }
        if (StringUtil.isNullOrEmpty(pattern)) {
            return autoNumber;
        }
        String query = "";
        String condition = "";
          if(!StringUtil.isNullOrEmpty(istemplatecondition)){
            condition+=istemplatecondition;
        }
        boolean ignoreLeadingZero = !pref.isShowLeadingZero();
        List list = new ArrayList();
        if (ignoreLeadingZero && pattern.matches("[0-9]\\d*")) {// allows only to digits.            
//             ignoreLeadingZero = true;
            List paramslist = new ArrayList();
            paramslist.add(pref.getID());
            condition = " and " + sqlfield + " REGEXP '^[0-9]+$' ";
            condition += " and " + sqlfield + " >= " + startfrom + " ";
            if (!StringUtil.equal(table, "Product")) {
                condition += " and autogen = 'T' ";//Need to change this to 'T'. Currently one issue on saving invoice. This flag is saved as 'F' which should be 'T'
            }
            if (seqformatidChk) {
                condition += " and seqformat is null ";
            }
            query = "select max(" + sqlfield + ") from " + sqltable + " where company =  ? " + condition;

            list = executeSQLQuery( query, paramslist.toArray());
        } else {
            if (seqformatidChk) {
                condition += " and seqformat is null ";
            }
            if (StringUtil.equal(table, "Product")) {
                query = "select max(" + field + ") from " + table + " where " + field + " like ? and  company.companyID =  ? " + condition;
            } else {
                query = "select max(" + field + ") from " + table + " where autoGenerated = true and " + field + " like ? and  company.companyID =  ? " + condition;
            }
            list = executeQuery( query, new Object[]{pattern.replace('0', '_'), pref.getID()});
        }
        if (list.isEmpty() == false) {
            if (list.get(0) != null) {
                autoNumber = (String) list.get(0);
            }
        }

        while (!pattern.equals(autoNumber)) {
            autoNumber = AccountingManager.generateNextAutoNumber(pattern, autoNumber, ignoreLeadingZero, startfrom);
            query = "select " + field + " from " + table + " where " + field + " = ? and company.companyID=?";
            list = executeQuery( query, new Object[]{autoNumber, pref.getID()});
            if (list.isEmpty()) {
                return autoNumber;
            }
        }
        throw new AccountingException("Auto number for the pattern '" + pattern + "' doesn't exist.<br>Please change the pattern or disable Auto generation.");

    }
    
    @Override
    public Map<String, Object> getNextChequeNumber(JSONObject jsonParams){
        String nextChequeNumber = "";
        String nextAutoNo = "";
         String nextAutoNoInt = "";
         String datePrefix = "";
         String dateafterPrefix = "";
         String dateSuffix = "";
         Date billdateVal = null;
         Map<String, Object> seqNumberMap = new HashMap<String, Object>();
        try {
            HashMap<String, Object> dataMap = new HashMap<>();
            String companyId = jsonParams.has(Constants.companyKey)?jsonParams.getString(Constants.companyKey):jsonParams.optString("companyId");
            String bankAccountId = jsonParams.optString("bankAccountId", "");
            String sequenceFormatID = jsonParams.optString("sequenceformat", "");
            String chequenumber = jsonParams.optString("chequenumber");
            int chequecduplicatepref = jsonParams.optInt("ischequeduplicatepref");
            DateFormat df = authHandler.getDateOnlyFormat();
            if (jsonParams.optString("postdate", null) != null) {
                billdateVal = df.parse(jsonParams.optString("postdate", null));
            }
//            dataMap.put("companyId", companyId);
////            dataMap.put("bankAccountId", bankAccountId);
//            dataMap.put("sequenceformatid", sequenceFormatID);
//            dataMap.put(Constants.language, jsonParams.opt(Constants.language));
//            KwlReturnObject cqresult = companyPreferencesDAO.getMaxChequeSequenceNumber(dataMap);
//            List returnList = cqresult.getEntityList();
//            BigInteger maxSequenceNumber = new BigInteger("0");
//
//            if (!returnList.isEmpty()) {
//                if (returnList.get(0) != null) {
//                    maxSequenceNumber = (BigInteger) returnList.get(0);
//                }
//            }
//            nextChequeNumber = getNextFormatedChequeNumber(maxSequenceNumber, companyId, bankAccountId,dataMap);
            
            seqNumberMap = getNextAutoChequeNumber_Modified(companyId, Constants.Cheque_ModuleId, sequenceFormatID, false, billdateVal,chequenumber,chequecduplicatepref);
            nextChequeNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
            nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
            datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
            dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
            dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
//        return nextChequeNumber;
        return seqNumberMap;
    }
    /**
     * 
     * method checks the cheque number is already in database for given sequence format or not.
     * @param hm
     * @return
     * @throws ServiceException 
     */
    public JSONObject isChequeNumberAvailable(HashMap hm) throws ServiceException, JSONException{
        boolean isChequeNumberAvailable = false;
        JSONObject jobj = new JSONObject();
        String condition = "";
        ArrayList params = new ArrayList();
        if (hm.containsKey("companyId")) {
            String companyId = (String) hm.get("companyId");
            condition += " and ch.company=? ";
            params.add(companyId);
        }
        if (hm.containsKey("bankAccountId")) {
            String bankAccountId = (String) hm.get("bankAccountId");
            condition += " and ch.bankaccount=? ";
            params.add(bankAccountId);
        }
        if (hm.containsKey("nextChequeNumber")) {
            String nextChequeNumber = (String) hm.get("nextChequeNumber");
            params.add(nextChequeNumber);
            condition += " and ch.chequeno=? ";
        }
        
        if (hm.containsKey("chequesequenceformatid") && hm.get("chequesequenceformatid") != null && !StringUtil.isNullOrEmpty(hm.get("chequesequenceformatid").toString())) {
            String sequenceformatid = (String) hm.get("chequesequenceformatid");
            params.add(sequenceformatid);
            condition += " and ( ch.seqformat=? ) ";
        }
        
        String query = "select chequeno,sequencenumber from cheque ch "
                + "WHERE (ch.createdfrom=1 or ch.createdfrom=3) and ch.deleteflag=false" + condition;

        List list = executeSQLQuery( query, params.toArray());
        String chequeno = "",chequesequencenumber="";
        if (list != null && !list.isEmpty() && list.get(0) != null) {
            isChequeNumberAvailable = true;
            Iterator itr = list.iterator(); 
            while (itr.hasNext()) {
                Object[] chobj = (Object[]) itr.next();
                chequeno = chobj[0].toString();
                chequesequencenumber = chobj[1].toString();
                break;
            }
        }
             jobj.put("isChequeNumberAvailable", isChequeNumberAvailable);
             jobj.put("chequeno", chequeno);
             jobj.put("chequesequencenumber", chequesequencenumber);
        return jobj;
    }
   /**
     * if Any changes done in method getNextAutoNumber_Modified() should also be done in getNextAutoNumber_manually method (If needed).
     * @param seqNumberMap
     * @param nextAutoNoInt
     * @return 
     */
    @Override
    public Map<String,Object> getNextAutoNumber_Modified(String companyid, int from, String format, boolean oldflag, Date creationDate) throws ServiceException, AccountingException {
        String autoNumber = "";
        String table = "", field = "", pattern = "";
        String sqltable = "", sqlfield = "";
        int startfrom = 1;
        String istemplatecondition="";
        String condition = "";
        boolean claim=false;
        boolean recover=false;
        Map<String, Object> seqNumberMap = new HashMap<String, Object>();

        switch (from) {
            case StaticValues.AUTONUM_JOURNALENTRY:
                table = "JournalEntry";
                field = "entryNumber";
                sqltable = "journalentry";
                sqlfield = "entryno";
                pattern = format;
               istemplatecondition=" and istemplate!=2 ";
                break;
            case StaticValues.AUTONUM_SALESORDER:
                table = "SalesOrder";
                field = "salesOrderNumber";
                sqltable = "salesorder";
                sqlfield = "sonumber";
                istemplatecondition=" and istemplate!=2 ";
                pattern = format;
                break;
            case StaticValues.AUTONUM_INVOICE:
                table = "Invoice";
                field = "invoiceNumber";
                sqltable = "invoice";
                sqlfield = "invoicenumber";
                pattern = format;
                istemplatecondition=" and istemplate!=2 ";
                break;
            case StaticValues.AUTONUM_CASHSALE:
                table = "Invoice";
                field = "invoiceNumber";
                sqltable = "invoice";
                sqlfield = "invoicenumber";
                pattern = format;
               istemplatecondition=" and istemplate!=2 ";
                break;
            case StaticValues.AUTONUM_CREDITNOTE:
                table = "CreditNote";
                field = "creditNoteNumber";
                sqltable = "creditnote";
                sqlfield = "cnnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_RECEIPT:
                table = "Receipt";
                field = "receiptNumber";
                sqltable = "receipt";
                sqlfield = "receiptnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_PURCHASEORDER:
                table = "PurchaseOrder";
                field = "purchaseOrderNumber";
                sqltable = "purchaseorder";
                sqlfield = "ponumber";
               istemplatecondition=" and istemplate!=2 ";
                pattern = format;
                break;
            case StaticValues.AUTONUM_GOODSRECEIPT:
                table = "GoodsReceipt";
                field = "goodsReceiptNumber";
                sqltable = "goodsreceipt";
                sqlfield = "grnumber";
                 istemplatecondition=" and istemplate!=2 ";
                pattern = format;
                break;
            case StaticValues.AUTONUM_CASHPURCHASE:
                table = "GoodsReceipt";
                field = "goodsReceiptNumber";
                sqltable = "goodsreceipt";
                sqlfield = "grnumber";
                 istemplatecondition=" and istemplate!=2 ";
                pattern = format;
                break;
            case StaticValues.AUTONUM_DEBITNOTE:
                table = "DebitNote";
                field = "debitNoteNumber";
                sqltable = "debitnote";
                sqlfield = "dnnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_PAYMENT:
                table = "Payment";
                field = "paymentNumber";
                sqltable = "payment";
                sqlfield = "paymentnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGINVOICE:
                table = "BillingInvoice";
                field = "billingInvoiceNumber";
                sqltable = "billinginvoice";
                sqlfield = "billinginvoicenumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGRECEIPT:
                table = "BillingReceipt";
                field = "billingReceiptNumber";
                sqltable = "billingreceipt";
                sqlfield = "billingreceiptnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGCASHSALE:
                table = "BillingInvoice";
                field = "billingInvoiceNumber";
                sqltable = "billinginvoice";
                sqlfield = "billinginvoicenumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGGOODSRECEIPT:
                table = "BillingGoodsReceipt";
                field = "billingGoodsReceiptNumber";
                sqltable = "billinggr";
                sqlfield = "billinggrnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGPAYMENT:
                table = "BillingPayment";
                field = "billingPaymentNumber";
                sqltable = "billingpayment";
                sqlfield = "billingpaymentnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGCASHPURCHASE:
                table = "BillingGoodsReceipt";
                field = "billingGoodsReceiptNumber";
                sqltable = "billinggr";
                sqlfield = "billinggrnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGPURCHASEORDER:
                table = "BillingPurchaseOrder";
                field = "purchaseOrderNumber";
                sqltable = "billingpurchaseorder";
                sqlfield = "ponumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGSALESORDER:
                table = "BillingSalesOrder";
                field = "salesOrderNumber";
                sqltable = "billingsalesorder";
                sqlfield = "sonumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGDEBITNOTE:
                table = "BillingDebitNote";
                field = "debitNoteNumber";
                sqltable = "billingdebitnote";
                sqlfield = "dnnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGCREDITNOTE:
                table = "BillingCreditNote";
                field = "creditNoteNumber";
                sqltable = "billingcreditnote";
                sqlfield = "cnnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_CONTRACT:
                table = "Contract";
                field = "contractNumber";
                sqltable = "contract";
                sqlfield = "contractnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_QUOTATION:
                table = "Quotation";
                field = "quotationNumber";
                sqltable = "quotation";
                sqlfield = "quotationnumber";
                istemplatecondition=" and istemplate!=2 ";
                pattern = format;
                break;
            case StaticValues.AUTONUM_VENQUOTATION:
                table = "VendorQuotation";
                field = "quotationNumber";
                sqltable = "vendorquotation";
                sqlfield = "quotationnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_REQUISITION:
                table = "PurchaseRequisition";
                field = "prNumber";
                sqltable = "purchaserequisition";
                sqlfield = "prnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_RFQ:
                table = "RequestForQuotation";
                field = "rfqNumber";
                sqltable = "requestforquotation";
                sqlfield = "rfqnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_PRODUCTID:
                table = "Product";
                field = "productid";
                sqltable = "product";
                sqlfield = "productid";
                pattern = format;
                break;
            case StaticValues.AUTONUM_DELIVERYORDER:
                table = "DeliveryOrder";
                field = "deliveryOrderNumber";
                sqltable = "deliveryorder";
                sqlfield = "donumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_GOODSRECEIPTORDER:
                table = "GoodsReceiptOrder";
                field = "goodsReceiptOrderNumber";
                sqltable = "grorder";
                sqlfield = "gronumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_SALESRETURN:
                table = "SalesReturn";
                field = "salesReturnNumber";
                sqltable = "salesreturn";
                sqlfield = "srnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_PURCHASERETURN:
                table = "PurchaseReturn";
                field = "purchaseReturnNumber";
                sqltable = "purchasereturn";
                sqlfield = "prnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_CUSTOMER:
                table = "Customer";
                field = "acccode";
                sqltable = "customer";
                sqlfield = "acccode";
                pattern = format;
                break;
            case StaticValues.AUTONUM_VENDOR:
                table = "Vendor";
                field = "acccode";
                sqltable = "vendor";
                sqlfield = "acccode";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BADDEBTINVOICECLAIM:
                table = "baddebtinvoicemapping";
                field = "invoice";
                sqltable = "baddebtinvoicemapping";
                sqlfield = "invoice";
                pattern = format;
                claim=true;
                break;
            case StaticValues.AUTONUM_BADDEBTINVOICERECOVER:
                table = "baddebtinvoicemapping";
                field = "invoice";
                sqltable = "baddebtinvoicemapping";
                sqlfield = "invoice";
                pattern = format;
                recover=true;
                break;
            case StaticValues.AUTONUM_BADDEBTPURCHASECLAIM:
                table = "BadDebtPurchaseInvoiceMapping";
                field = "goodsReceipt";
                sqltable = "baddebtpurchaseinvoicemapping";
                sqlfield = "goodsreceipt";
                pattern = format;
                claim=true;
                break;
            case StaticValues.AUTONUM_BADDEBTPURCHASERECOVER:
                table = "BadDebtPurchaseInvoiceMapping";
                field = "goodsReceipt";
                sqltable = "baddebtpurchaseinvoicemapping";
                sqlfield = "goodsreceipt";
                pattern = format;
                recover=true;
                break;
            case StaticValues.AUTONUM_BUILDASSEMBLY:
                table = "ProductBuild";
                sqltable = "productbuild";
                pattern = format;
                break;
            case StaticValues.AUTONUM_ASSETGROUP:
                table = "Product";
                sqltable = "product";
                pattern = format;
                break;
            case StaticValues.AUTONUM_Loan_Management:
                table = "Disbursement";
                sqltable = "disbursement";
                pattern = format;
                break;
            case StaticValues.AUTONUM_MACHINE_Management:
                table = "Machine";
                sqltable = "machine";
                pattern = format;
                break;
            case StaticValues.AUTONUM_LABOUR:
                table = "Labour";
                sqltable = "labour";
                pattern = format;
                break;
                case StaticValues.AUTONUM_MRPCONTRACT:
                table = "MRPContract";
                sqltable = "mrpcontract";
                pattern = format;
                break;
            case StaticValues.AUTONUM_MRP_JOBWORK:
                table = "JobWork";
                field = "jobordernumber";
                sqltable = "mrp_job_order";
                sqlfield = "jobordernumber";
                pattern = format;
                break;    
            case StaticValues.AUTONUM_MRP_WORKCENTRE:
                table = "WorkCentre";
                field = "workcenterid";
                sqltable = "workcenter";
                sqlfield = "workcenterid";
                pattern = format;
                break;    
            case StaticValues.AUTONUM_MRP_WORKORDER:
                table = "WorkOrder";
                field = "workOrderID";
                sqltable = "workorder";
                sqlfield = "workorderid";
                pattern = format;
                break;    
            case StaticValues.AUTONUM_MRP_ROUTECODE:
                table = "RoutingTemplate";
                sqltable = "routing_template";
                pattern = format;
                break;
            case StaticValues.AUTONUM_DEALER_EXCISE_RG23D_NUMBER:
                table = "DealerExciseDetails";
                sqltable = "dealerexcisedetails";
                pattern = format;
                break;
            case StaticValues.AUTONUM_DIMENSION:
                table = "FieldComboData";
                sqltable = "fieldcombodata";
                pattern = format;
                break;
            case StaticValues.AUTONUM_RECONCILENO:
                table = "BankReconciliation";
                sqltable = "bankreconciliation";
                pattern = format;
                break;
            case StaticValues.AUTONUM_UNRECONCILENO:
                table = "BankReconciliation";
                sqltable = "bankreconciliation";
                pattern = format;
                break;
            /**
             * Get table information for getting next auto number for Packing Delivery Order
             */    
            case StaticValues.AUTONUM_PACKINGDO:
                table = "Packing";
                sqltable = "packing";
                pattern = format;
                break;
            /**
             * Get table information for getting next auto number for Shipping Delivery Order
             */    
            case StaticValues.AUTONUM_SHIPPINGDO:
                table = "ShippingDeliveryOrder";
                sqltable = "shippingdelivery";
                pattern = format;
                break;
            case StaticValues.AUTONUM_SECURITYNO:
                table = "SecurityGateEntry";
                sqltable = "securitygateentry";
                pattern = format;
                break;
            case Constants.Cheque_ModuleId:
                table = "Cheque";
                sqltable = "cheque";
                pattern = format;
                break;
        }

        if (StringUtil.isNullOrEmpty(pattern)) {
            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, "");//complete number
            seqNumberMap.put(Constants.SEQNUMBER, "0");//interger part
            seqNumberMap.put(Constants.DATEPREFIX, "");//Date Before Prefix
            seqNumberMap.put(Constants.DATEAFTERPREFIX, "");//Date After Prefix
            seqNumberMap.put(Constants.DATESUFFIX, "");//Date After Suffix
            return seqNumberMap;
        }
        
        String seqformatid = format;
        if(!StringUtil.isNullOrEmpty(istemplatecondition)){
            condition+=istemplatecondition;
        }
        int numberofdigit = 0;
        String selectedDateFormatAfterPrefix="",selectedSuffixDate="",prefix="",selecteddateformat="",suffix="",bankAccountId="";
        boolean dateAfterSuffix = false,datebeforePrefix = false,isdateafterPrefix = false,showleadingzero=false,resetcounter=false;
        if (from == Constants.Cheque_ModuleId) {
            ChequeSequenceFormat seqFormat = (ChequeSequenceFormat) get(ChequeSequenceFormat.class, seqformatid);
            startfrom = seqFormat.getStartFrom().intValue();
            datebeforePrefix = seqFormat.isDateBeforePrefix();
            isdateafterPrefix = seqFormat.isDateAfterPrefix();
            dateAfterSuffix = seqFormat.isShowDateFormatAfterSuffix();
            bankAccountId = seqFormat.getBankAccount()!=null?seqFormat.getBankAccount().getID():"";
            selectedDateFormatAfterPrefix = StringUtil.isNullOrEmpty(seqFormat.getDateformatafterprefix()) ? "" : seqFormat.getDateformatafterprefix();
            selectedSuffixDate = StringUtil.isNullOrEmpty(seqFormat.getDateFormatAfterSuffix()) ? "" : seqFormat.getDateFormatAfterSuffix();
            prefix = seqFormat.getPrefix();
            selecteddateformat = StringUtil.isNullOrEmpty(seqFormat.getDateformatinprefix()) ? "" : seqFormat.getDateformatinprefix();
            suffix = seqFormat.getSuffix();
            numberofdigit = seqFormat.getNumberOfDigits();
            showleadingzero = seqFormat.isShowLeadingZero();
            resetcounter = seqFormat.isResetCounter();
            condition += " and sequencenumber >= " + startfrom + " ";
        } else {
            SequenceFormat seqFormat = (SequenceFormat) get(SequenceFormat.class, seqformatid);
            startfrom = seqFormat.getStartfrom();
            datebeforePrefix = seqFormat.isDateBeforePrefix();
            isdateafterPrefix = seqFormat.isDateAfterPrefix();
            dateAfterSuffix = seqFormat.isShowDateFormatAfterSuffix();
            selectedDateFormatAfterPrefix = StringUtil.isNullOrEmpty(seqFormat.getDateformatafterprefix()) ? "" : seqFormat.getDateformatafterprefix();
            selectedSuffixDate = StringUtil.isNullOrEmpty(seqFormat.getDateFormatAfterSuffix()) ? "" : seqFormat.getDateFormatAfterSuffix();
            prefix = seqFormat.getPrefix();
            selecteddateformat = StringUtil.isNullOrEmpty(seqFormat.getDateformatinprefix()) ? "" : seqFormat.getDateformatinprefix();
            suffix = seqFormat.getSuffix();
            numberofdigit = seqFormat.getNumberofdigit();
            showleadingzero = seqFormat.isShowleadingzero();
            resetcounter = seqFormat.isResetCounter();
            condition += " and seqnumber >= " + startfrom + " ";
            if (!StringUtil.equal(table, "Product")) {
                condition += " and autogen = 'T' ";
            }
        }
        String query = "";
        List list = new ArrayList();

//                String sqlseqnumberfield = "seqnumber";
//                String sqlseqformatfield = "seqformat";
        
        
        String datePrefix = "";
        String dateSuffix = "";
        String dateAfterPrefix = "";
        if (datebeforePrefix || dateAfterSuffix || isdateafterPrefix) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");  //ERP-8689     
            if(creationDate == null){
                creationDate=getCurrentDateWithCompanyCreatorTimeZone(companyid);
        }
            Calendar cal = Calendar.getInstance();
            cal.setTime(creationDate);
            int year = cal.get(Calendar.YEAR);
            int yy = Math.abs(year) % 100; // Get YY value from year    
            DecimalFormat mFormat = new DecimalFormat("00");
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            if (datebeforePrefix) {
                if (selecteddateformat.equalsIgnoreCase("YYYY")) {
                    datePrefix = "" + year;
                } else if (selecteddateformat.equalsIgnoreCase("YYYYMM")) {
                    datePrefix = "" + year + mFormat.format(month);
                } else if (selecteddateformat.equalsIgnoreCase("YY")) {
                    datePrefix = "" + mFormat.format(yy) ;
                } else if (selecteddateformat.equalsIgnoreCase("YYMM")) {
                    datePrefix = "" + mFormat.format(yy) + mFormat.format(month);
                } else if (selecteddateformat.equalsIgnoreCase("YYMMDD")) {
                    datePrefix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day);
                } else if (selecteddateformat.equalsIgnoreCase("YYYY-")) {
                    datePrefix = "" + year+"-";
                } else if (selecteddateformat.equalsIgnoreCase("YYYYMM-")) {
                    datePrefix = "" + year + mFormat.format(month)+"-";
                } else if (selecteddateformat.equalsIgnoreCase("YY-")) {
                    datePrefix = "" + mFormat.format(yy)+"-" ;
                } else if (selecteddateformat.equalsIgnoreCase("YYMM-")) {
                    datePrefix = "" + mFormat.format(yy) + mFormat.format(month)+"-";
                } else if (selecteddateformat.equalsIgnoreCase("YYMMDD-")) {
                    datePrefix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day)+"-";
                } else if (selecteddateformat.equalsIgnoreCase("YYYYMMDD-")) {
                    datePrefix = "" + sdf.format(creationDate) + "-";
                } else { //for YYYYMMDD this will default case
                    datePrefix = sdf.format(creationDate);
                }
            }
            if (isdateafterPrefix) {
                if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYYY")) {
                    dateAfterPrefix = "" + year;
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYYYMM")) {
                    dateAfterPrefix = "" + year + mFormat.format(month);
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YY")) {
                    dateAfterPrefix = "" + mFormat.format(yy) ;
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYMM")) {
                    dateAfterPrefix = "" + mFormat.format(yy) + mFormat.format(month);
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYMMDD")) {
                    dateAfterPrefix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day);
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYYY-")) {
                    dateAfterPrefix = "" + year+"-";
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYYYMM-")) {
                    dateAfterPrefix = "" + year + mFormat.format(month)+"-";
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YY-")) {
                    dateAfterPrefix = "" + mFormat.format(yy)+"-" ;
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYMM-")) {
                    dateAfterPrefix = "" + mFormat.format(yy) + mFormat.format(month)+"-";
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYMMDD-")) {
                    dateAfterPrefix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day)+"-";
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYYYMMDD-")) {
                    dateAfterPrefix = "" + sdf.format(creationDate) + "-";
                } else { //for YYYYMMDD this will default case
                    dateAfterPrefix = sdf.format(creationDate);
                }
            }
            if (dateAfterSuffix) {
                if (selectedSuffixDate.equalsIgnoreCase("YYYY")) {
                    dateSuffix = "" + year;
                } else if (selectedSuffixDate.equalsIgnoreCase("YYYYMM")) {
                    dateSuffix = "" + year + mFormat.format(month);
                } else if (selectedSuffixDate.equalsIgnoreCase("YY")) {
                    dateSuffix = "" + mFormat.format(yy);
                } else if (selectedSuffixDate.equalsIgnoreCase("YYMM")) {
                    dateSuffix = "" + mFormat.format(yy) + mFormat.format(month);
                } else if (selectedSuffixDate.equalsIgnoreCase("YYMMDD")) {
                    dateSuffix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day);
                } else if (selectedSuffixDate.equalsIgnoreCase("YYYY-")) {
                    dateSuffix = "" + year+"-";
                } else if (selectedSuffixDate.equalsIgnoreCase("YYYYMM-")) {
                    dateSuffix = "" + year + mFormat.format(month)+"-";
                } else if (selectedSuffixDate.equalsIgnoreCase("YY-")) {
                    dateSuffix = "" + mFormat.format(yy)+"-";
                } else if (selectedSuffixDate.equalsIgnoreCase("YYMM-")) {
                    dateSuffix = "" + mFormat.format(yy) + mFormat.format(month)+"-";
                } else if (selectedSuffixDate.equalsIgnoreCase("YYMMDD-")) {
                    dateSuffix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day)+"-";
                } else if (selectedSuffixDate.equalsIgnoreCase("YYYYMMDD-")) {
                    dateSuffix = "" + sdf.format(creationDate) + "-";
                }else { //for YYYYMMDD this will default case
                    dateSuffix = sdf.format(creationDate);
                }
            }
        }

      //logic to find maximum counter for the sequence format
        List paramslist = new ArrayList();
        paramslist.add(companyid);
        
        
//        if (resetcounter) { //when reset option is selected/true 
//            if (!StringUtil.isNullOrEmpty(datePrefix) && !StringUtil.isNullOrEmpty(dateSuffix) && !StringUtil.isNullOrEmpty(dateAfterPrefix)) { //when suffix and prefix both exist
//                paramslist.add(dateSuffix);
//                paramslist.add(datePrefix);
//                paramslist.add(dateAfterPrefix);
//                condition += " and datesuffixvalue = ? and datepreffixvalue = ? and dateafterpreffixvalue = ? ";
//            } else if (!StringUtil.isNullOrEmpty(datePrefix)) { // when only prefix exist
//                paramslist.add(datePrefix);
//                condition += " and datepreffixvalue = ? ";
//            } else if (!StringUtil.isNullOrEmpty(dateAfterPrefix)) { // when only date after prefix exist
//                paramslist.add(dateAfterPrefix);
//                condition += " and dateafterpreffixvalue = ? ";
//            } else if (!StringUtil.isNullOrEmpty(dateSuffix)) { // when only suffix exist
//                paramslist.add(dateSuffix);
//                condition += " and datesuffixvalue = ? ";
//            }
//        }
        
        if (resetcounter) {
            String resetCounterCondition = "";
            //Date Before Prefix
            if (datebeforePrefix && !StringUtil.isNullOrEmpty(datePrefix)) {
                resetCounterCondition += datePrefix;//Date Before Prefix
            }
            //Prefix
            if (!StringUtil.isNullOrEmpty(prefix)) {
                resetCounterCondition += prefix;
            }
            //Date After Prefix
            if (isdateafterPrefix && !StringUtil.isNullOrEmpty(dateAfterPrefix)) {
                resetCounterCondition += dateAfterPrefix;
            }
            
            //Number
            resetCounterCondition += "[0-9]+";
            
            //Suffix
            if (!StringUtil.isNullOrEmpty(suffix)) {
                resetCounterCondition += suffix;
            }
            //Date After Suffix
            if (dateAfterSuffix && !StringUtil.isNullOrEmpty(dateSuffix)) {
                resetCounterCondition += dateSuffix;
            }
            
            condition += " AND " + sqlfield + " REGEXP '^" + resetCounterCondition + "$' ";
        }
        
        if (!StringUtil.isNullOrEmpty(seqformatid)) {
            condition += " and seqformat = ? ";
            paramslist.add(seqformatid);
        }
        if (claim) {
            condition += " and baddebttype=0";
        }
        if (recover) {
            condition += " and baddebttype=1";
        }
        if (from==StaticValues.AUTONUM_DIMENSION) {
            query = "select max(seqnumber) from " + sqltable + " where fieldid =  ? " + condition;
        } else if(from==Constants.Cheque_ModuleId){
            query = "select max(sequencenumber) from " + sqltable + " where company =  ? " + condition;
        }else{
            query = "select max(seqnumber) from " + sqltable + " where company =  ? " + condition;
        }
        list = executeSQLQuery(query, paramslist.toArray());
        int nextNumber = startfrom;
        if (!list.isEmpty()) {
            if (list.get(0) != null) {
                nextNumber = Integer.parseInt(list.get(0).toString()) + 1;
            }
        }
        String nextNumTemp = nextNumber + "";
        if (showleadingzero) {
            while (nextNumTemp.length() < numberofdigit) {
                nextNumTemp = "0" + nextNumTemp;
            }
        }

        //Building the complete number
        if (datebeforePrefix || dateAfterSuffix || isdateafterPrefix) {
            autoNumber = datePrefix + prefix + dateAfterPrefix + nextNumTemp + suffix + dateSuffix;
        } else {
            autoNumber = prefix + nextNumTemp + suffix;
        }
        seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, autoNumber);//complete number
        seqNumberMap.put(Constants.SEQNUMBER, nextNumTemp);//interger part 
        seqNumberMap.put(Constants.DATEPREFIX, datePrefix);
        seqNumberMap.put(Constants.DATEAFTERPREFIX, dateAfterPrefix);
        seqNumberMap.put(Constants.DATESUFFIX, dateSuffix);
        seqNumberMap.put("prefix", prefix);
        seqNumberMap.put("suffix", suffix);
        seqNumberMap.put("numberofdigit", numberofdigit);
        seqNumberMap.put("showleadingzero", showleadingzero);
        return seqNumberMap;
    }

   /**
     * if Any changes done in method getNextAutoNumber_Modified() should also be done in below method (If needed)..
     * @param seqNumberMap
     * @param nextAutoNoInt
     * @return 
     */
    public String getNextAutoNumber_manually(Map<String, Object> seqNumberMap,int nextAutoNoInt) {
        String next_transaction_number = "";
//        int nextAutoNoInt = 0;
        int nextAutoNoIntpart = 0;
        int numberofdigit = 0;
        boolean showleadingzero = false;
//        String nextAutoNo = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
//        nextAutoNoInt = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
        String datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
        String dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
        String dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
        String prefix = (String) seqNumberMap.get("prefix");//Date Suffix Part
        String suffix = (String) seqNumberMap.get("suffix");//Date Suffix Part
        numberofdigit = (int) seqNumberMap.get("numberofdigit");//Date Suffix Part
        showleadingzero = (boolean) seqNumberMap.get("showleadingzero");//Date Suffix Part
        nextAutoNoIntpart = nextAutoNoInt;

        String nextNumTemp = nextAutoNoIntpart + "";
        if (showleadingzero) {
            while (nextNumTemp.length() < numberofdigit) {
                nextNumTemp = "0" + nextNumTemp;
            }
        }
        return next_transaction_number = datePrefix + prefix + dateafterPrefix + nextNumTemp + suffix + dateSuffix;
    }
    public Date getCurrentDateWithCompanyCreatorTimeZone(String companyid) {
        Date currentdate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Company capany = (Company) get(Company.class, companyid);
        if (capany != null && capany.getCreator() != null && capany.getCreator().getTimeZone() != null) {
            sdf.setTimeZone(TimeZone.getTimeZone("GMT" + capany.getCreator().getTimeZone().getDifference()));
        }
        Date newdate = new Date();
        try {
            currentdate = authHandler.getDateWithTimeFormat().parse(sdf.format(newdate));
        } catch (Exception ex) {
            currentdate = new Date();
        }
        return currentdate;
    }
    
      @Override
    public List checksEntryNumberForSequenceNumber(int moduleid, String entryNumber, String companyid) throws ServiceException {
        List ll = new ArrayList();
        JSONObject paramObj = new JSONObject();
        JSONObject jObj = new JSONObject();
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("moduleid", moduleid);
            map.put("companyid", companyid);
            //map.put("isChecked", true);
            String formatid = "";
            String formatName = "";
            int intPartValue = 0;
            int intStartFromValue = 0;
            boolean isSeqnum = false;
            boolean isvalidEntryNumber = true;
            //If any one add more date formats in UI of sequence format needs to add here as well
            Map<String,String> dataFormatMap = new HashMap<>();
            dataFormatMap.put("YYYY", "yyyy");
            dataFormatMap.put("YYYYMM", "yyyyMM");
            dataFormatMap.put("YYYYMMDD", "yyyyMMdd");
            dataFormatMap.put("YY", "yy");
            dataFormatMap.put("YYMM", "yyMM");
            dataFormatMap.put("YYMMDD", "yyMMdd");
            dataFormatMap.put("YYYY-", "yyyy-");
            dataFormatMap.put("YYYYMM-", "yyyyMM-");
            dataFormatMap.put("YYYYMMDD-", "yyyyMMdd-");
            dataFormatMap.put("YY-", "yy-");
            dataFormatMap.put("YYMM-", "yyMM-");
            dataFormatMap.put("YYMMDD-", "yyMMdd-");
            
            KwlReturnObject result = getSequenceFormat(map);
            List<SequenceFormat> formats = result.getEntityList();
            for (SequenceFormat format : formats) {
                String selecteddateformat = "";
                String dateFormatAfterPrefix = "";
                String selectedSuffixdateformat = "";
                boolean isDateBeforePrefix = false;
                boolean isDateAfterPrefix = false;
                boolean isDateAfterSuffix = false;
                formatName = format.getName() != null ? format.getName() : "";
                String preffix = format.getPrefix() != null ? format.getPrefix() : "";                
                String suffix = format.getSuffix() != null ? format.getSuffix() : "";
                preffix=preffix.toLowerCase();
                suffix=suffix.toLowerCase();
                isDateBeforePrefix=format.isDateBeforePrefix();
                if (isDateBeforePrefix) {
                    selecteddateformat = format.getDateformatinprefix() != null ? format.getDateformatinprefix() : "";
                }
                isDateAfterPrefix=format.isDateAfterPrefix();
                if (isDateAfterPrefix) {
                    dateFormatAfterPrefix = format.getDateformatafterprefix() != null ? format.getDateformatafterprefix() : "";
                }
                isDateAfterSuffix = format.isShowDateFormatAfterSuffix();
                if (isDateAfterSuffix) {
                    selectedSuffixdateformat = format.getDateFormatAfterSuffix() != null ? format.getDateFormatAfterSuffix() : "";
                }
                
                String lowerEntryNumber=entryNumber.toLowerCase(); 
                
                paramObj.put("isDateBeforePrefix",isDateBeforePrefix );
                paramObj.put("isDateAfterPrefix",isDateAfterPrefix );
                paramObj.put("isDateAfterSuffix",isDateAfterSuffix );
                paramObj.put("lowerEntryNumber",lowerEntryNumber );
                paramObj.put("selecteddateformat",selecteddateformat );
                paramObj.put("formatName",formatName );
                paramObj.put("selectedSuffixdateformat",selectedSuffixdateformat);
                paramObj.put("dateFormatAfterPrefix",dateFormatAfterPrefix);
                paramObj.put("preffix",preffix);
                paramObj.put("formatgetstart",format.getStartfrom());
                paramObj.put("formatid",format.getID());
                paramObj.put("suffix",suffix);
                paramObj.put("entryNumber",entryNumber);
                try {
                    /**
                     * common method to check the transaction is belong to sequence format or not.
                     */
                    jObj = methodTocheckValidTransaction(paramObj);
                } catch (Exception ex) {
                    continue;
                }
                formatName = jObj.optString("formatName");
                intStartFromValue = jObj.optInt("intStartFromValue");
                intPartValue = jObj.optInt("intPartValue");
                isSeqnum = jObj.optBoolean("isSeqnum");
                formatid = jObj.optString("formatid");
                if (isSeqnum) {
                    break;
                }
            }
            if (isSeqnum) {
//                String sqltable = "";
                int autoNum = 0;
                jObj = getmoduleidtableandColumnName(moduleid);
                autoNum = jObj.optInt("autoNum");
//                
                int maxseqnum = intStartFromValue-1;// Initialize with start from number to check sequence no. can be generated from available sequence format or not.
//                ArrayList params = new ArrayList();
//                params.add(formatid);
//                params.add(companyid);
//                String query = "select max(seqnumber) from " + sqltable + " where seqformat = ? and company =  ?";
                Map<String,Object> seqForm = getNextAutoNumber_Modified(companyid, autoNum, formatid, false, null);
                if (seqForm.containsKey("seqnumber") && !StringUtil.isNullOrEmpty((String) seqForm.get("seqnumber"))) {
                    maxseqnum = Integer.parseInt(seqForm.get("seqnumber").toString());
                }
//                List list = executeSQLQuery( query, params.toArray());
//                if (!list.isEmpty() && list.get(0) != null) {
//                    maxseqnum = Integer.parseInt(list.get(0).toString());
//                }
                if (intPartValue >= maxseqnum) {// user entered number can also be generated by sequence number
                    isvalidEntryNumber = false;
                }
            }
            ll.add(isvalidEntryNumber);
            ll.add(formatName);
            ll.add(intPartValue);
            ll.add(formatid);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.checksEntryNumberForSequenceNumber : " + ex.getMessage(), ex);
        }
        return ll;  
    }
    // method to check cheque number belongs to sequence format or not
    @Override
    public List checksChequeNumberForSequenceNumber(String chequeNumber, String companyid) throws ServiceException {
        List ll = new ArrayList();
        JSONObject paramObj = new JSONObject();
        JSONObject jObj = new JSONObject();
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("companyid", companyid);
            String formatName = "";
            boolean isSeqnum = false;
            boolean isvalidEntryNumber = true;

            KwlReturnObject result = getChequeSequenceFormatList(map);
            List<ChequeSequenceFormat> formats = result.getEntityList();
            if (formats != null && !formats.isEmpty()) {
                    for (ChequeSequenceFormat format : formats) {
                        String selecteddateformat = "";
                        String dateFormatAfterPrefix = "";
                        String selectedSuffixdateformat = "";
                        boolean isDateBeforePrefix = false;
                        boolean isDateAfterPrefix = false;
                        boolean isDateAfterSuffix = false;
                        formatName = format.getName() != null ? format.getName() : "";
                        String preffix = format.getPrefix() != null ? format.getPrefix() : "";
                        String suffix = format.getSuffix() != null ? format.getSuffix() : "";
                        preffix = preffix.toLowerCase();
                        suffix = suffix.toLowerCase();
                        isDateBeforePrefix = format.isDateBeforePrefix();
                        if (isDateBeforePrefix) {
                            selecteddateformat = format.getDateformatinprefix() != null ? format.getDateformatinprefix() : "";
                        }
                        isDateAfterPrefix = format.isDateAfterPrefix();
                        if (isDateAfterPrefix) {
                            dateFormatAfterPrefix = format.getDateformatafterprefix() != null ? format.getDateformatafterprefix() : "";
                        }
                        isDateAfterSuffix = format.isShowDateFormatAfterSuffix();
                        if (isDateAfterSuffix) {
                            selectedSuffixdateformat = format.getDateFormatAfterSuffix() != null ? format.getDateFormatAfterSuffix() : "";
                        }
                        formatName = formatName.substring(selecteddateformat.length(), (formatName.length() - selectedSuffixdateformat.length()));
                        String lowerEntryNumber = chequeNumber.toLowerCase();

                        paramObj.put("isDateBeforePrefix", isDateBeforePrefix);
                        paramObj.put("isDateAfterPrefix", isDateAfterPrefix);
                        paramObj.put("isDateAfterSuffix", isDateAfterSuffix);
                        paramObj.put("lowerEntryNumber", lowerEntryNumber);
                        paramObj.put("selecteddateformat", selecteddateformat);
                        paramObj.put("formatName", formatName);
                        paramObj.put("selectedSuffixdateformat", selectedSuffixdateformat);
                        paramObj.put("dateFormatAfterPrefix", dateFormatAfterPrefix);
                        paramObj.put("preffix", preffix);
                        paramObj.put("formatgetstart", format.getStartFrom());
                        paramObj.put("formatendnumber", format.getChequeEndNumber());
                        paramObj.put("formatid", format.getId());
                        paramObj.put("suffix", suffix);
                        paramObj.put("entryNumber", chequeNumber);

                        try {
                            /**
                        * common method to check the transaction is belong to sequence format or not.
                             */
                            jObj = methodTocheckValidTransaction(paramObj);
                        } catch (Exception ex) {
                            continue;
                        }
                        isSeqnum = jObj.optBoolean("isSeqnum");
                        formatName = jObj.optString("formatName");
                        if (isSeqnum) {
                            isvalidEntryNumber = false;
                            break;
                        }
                    }
            }
            ll.add(isvalidEntryNumber);
            ll.add(formatName);
            ll.add(jObj);  //to check cheque number belongs to sequence format or not in JE ERP-39212
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.checksEntryNumberForSequenceNumber : " + ex.getMessage(), ex);
        }
        return ll;
    }
    
    public KwlReturnObject getPreferencesFromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        /*  (ERP-32531)
        *   also check account is use in 'Customer default account' , 'Vendor default account' and 'Unrealized gain and loss'.
        *   Hence added corresponding column(customerdefaultaccount, vendordefaultaccount and unrealisedgainloss) in query. 
        */
        String q = "from CompanyAccountPreferences acp where (discountGiven.ID=? or discountReceived.ID=? or shippingCharges.ID=? or cashAccount.ID=? or foreignexchange.ID =? or depereciationAccount.ID = ? or expenseAccount.ID = ? or liabilityAccount.ID = ? or paymentMethod.id=? or customerdefaultaccount.ID = ? or vendordefaultaccount.ID = ? or unrealisedgainloss.ID = ? ) and acp.company.companyID=?";
        list = executeQuery( q, new Object[]{accountid, accountid, accountid, accountid, accountid, accountid, accountid, accountid,accountid, accountid, accountid, accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCompanyPreferences(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from CompanyAccountPreferences ";

        if (filterParams.containsKey("id")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ID=?";
            params.add(filterParams.get("id"));
        }
        query += condition;
            returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getCompanyPreferencesFieldForExport() throws ServiceException {
        List returnList = new ArrayList();
        String query = "";
        query = "from ExportCompanypref order by displayname";
        returnList = executeQuery( query);
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getExtraCompanyPreferences(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from ExtraCompanyPreferences ";

        if (filterParams.containsKey("id")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ID=?";
            params.add(filterParams.get("id"));
        }
        query += condition;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    public KwlReturnObject getIndiaComplianceExtraCompanyPreferences(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from IndiaComplianceCompanyPreferences ";

        if (filterParams.containsKey("id")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ID=?";
            params.add(filterParams.get("id"));
        }
        query += condition;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    public KwlReturnObject getMRPCompanyPreferences(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from MRPCompanyPreferences ";

        if (filterParams.containsKey("id")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ID=?";
            params.add(filterParams.get("id"));
        }
        query += condition;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    public KwlReturnObject getDocumentEmailSettings(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from DocumentEmailSettings ";

        if (filterParams.containsKey("id")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ID=?";
            params.add(filterParams.get("id"));
        }
        query += condition;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject addPreferences(HashMap<String, Object> prefMap) throws ServiceException {
        List list = new ArrayList();
        try {
            CompanyAccountPreferences preference = new CompanyAccountPreferences();
            preference = buildPreferences(preference, prefMap);
            String companyId=preference.getCompany().getCompanyID();
            ExtraCompanyPreferences ecp=(ExtraCompanyPreferences) get(ExtraCompanyPreferences.class, companyId);
            if (preference != null) {
                preference = buildPreferences(preference, prefMap);
                
                if (ecp != null && prefMap.containsKey("isQaApprovalFlow")  && prefMap.get("isQaApprovalFlow") != null  && prefMap.get("loggedInUserId") != null) {
                    EnableDisableQAFeatureOnInventorySide(companyId,!ecp.isActivateQAApprovalFlow(),(String)prefMap.get("loggedInUserId"));
                }
                saveOrUpdate(preference);
            }
            list.add(preference);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addPreferences : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Company Account Preferences has been added successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject addOrUpdateExtraPreferences(HashMap<String, Object> prefMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = "";
            ExtraCompanyPreferences preference = null;
            if (prefMap.containsKey("id")) {
                id = (String) prefMap.get("id");
                if (!StringUtil.isNullOrEmpty(id)) {
                    preference = (ExtraCompanyPreferences) get(ExtraCompanyPreferences.class, id);
                    if (preference == null) {
                        preference = new ExtraCompanyPreferences();
                    }
                    preference = buildExtraPreferences(preference, prefMap);
                    saveOrUpdate(preference);
                    list.add(preference);
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("addPreferences : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Company Account Preferences has been added successfully", null, list, list.size());
    }
    @Override
    public KwlReturnObject addOrUpdateIndiaComplianceExtraPreferences(HashMap<String, Object> prefMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = "";
            IndiaComplianceCompanyPreferences preference = null;
            if (prefMap.containsKey("id")) {
                id = (String) prefMap.get("id");
                if (!StringUtil.isNullOrEmpty(id)) {
                    preference = (IndiaComplianceCompanyPreferences) get(IndiaComplianceCompanyPreferences.class, id);
                    if (preference == null) {
                        preference = new IndiaComplianceCompanyPreferences();
                    }
                    preference = buildIndiaComplianceExtraPreferences(preference, prefMap);
                    saveOrUpdate(preference);
                    list.add(preference);
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("addCompliancePreferences : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Company Compliance Preferences has been added successfully", null, list, list.size());
    }
    @Override
    public KwlReturnObject addOrUpdateMRPPreferences(HashMap<String, Object> prefMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = "";
            MRPCompanyPreferences preference = null;
            if (prefMap.containsKey("id")) {
                id = (String) prefMap.get("id");
                if (!StringUtil.isNullOrEmpty(id)) {
                    preference = (MRPCompanyPreferences) get(MRPCompanyPreferences.class, id);
                    if (preference == null) {
                        preference = new MRPCompanyPreferences();
                    }
                    preference = buildExtraPreferences(preference, prefMap);
                    saveOrUpdate(preference);
                    list.add(preference);
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("addPreferences : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Company Account Preferences has been added successfully", null, list, list.size());
    }
    public KwlReturnObject addOrUpdateDocumentEmailSettings(HashMap<String, Object> prefMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = "";
            DocumentEmailSettings document = null;
            if (prefMap.containsKey("id")) {
                id = (String) prefMap.get("id");
                if (!StringUtil.isNullOrEmpty(id)) {
                    document = (DocumentEmailSettings) get(DocumentEmailSettings.class, id);
                    if (document == null) {
                        document = new DocumentEmailSettings();
                    }
                    document = buildExtraPreferences(document, prefMap);
                    saveOrUpdate(document);
                    list.add(document);
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("addPreferences : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Company Account Preferences has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updatePreferences(HashMap<String, Object> prefMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = (String) prefMap.get("id");
            CompanyAccountPreferences preference = (CompanyAccountPreferences) get(CompanyAccountPreferences.class, id);
            
            ExtraCompanyPreferences ecp=(ExtraCompanyPreferences) get(ExtraCompanyPreferences.class, id);
            if (preference != null) {
                preference = buildPreferences(preference, prefMap);
                if (ecp != null && prefMap.containsKey("activateQAApprovalFlow") && prefMap.get("activateQAApprovalFlow") != null && prefMap.get("loggedInUserId") != null ) {
                    if (ecp.isActivateQAApprovalFlow()!=(Boolean)prefMap.get("activateQAApprovalFlow")) {
                        EnableDisableQAFeatureOnInventorySide(id, !ecp.isActivateQAApprovalFlow() ,(String)prefMap.get("loggedInUserId"));
                    }
                }
                saveOrUpdate(preference);
            }
            list.add(preference);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updatePreferences : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Company Account Preferences has been updated successfully", null, list, list.size());
    }
    public DocumentEmailSettings buildExtraPreferences(DocumentEmailSettings documentEmailSettings, HashMap<String, Object> prefMap) {
        String str;
        if (prefMap.containsKey("companyid")) {
            str = (String) prefMap.get("companyid");
            if (!StringUtil.isNullOrEmpty(str)) {
                documentEmailSettings.setCompany((Company) get(Company.class, str));
            } else {
                documentEmailSettings.setCompany(null);
            }
        }
        
        if (prefMap.containsKey("purchaseReqGenerationMail")) {
            documentEmailSettings.setPurchaseReqGenerationMail(prefMap.get("purchaseReqGenerationMail") != null ? (Boolean) prefMap.get("purchaseReqGenerationMail") : false);
        }
        if (prefMap.containsKey("purchaseReqUpdationMail")) {
            documentEmailSettings.setPurchaseReqUpdationMail(prefMap.get("purchaseReqUpdationMail") != null ? (Boolean) prefMap.get("purchaseReqUpdationMail") : false);
        }
        if (prefMap.containsKey("vendorQuotationGenerationMail")) {
            documentEmailSettings.setVendorQuotationGenerationMail(prefMap.get("vendorQuotationGenerationMail") != null ? (Boolean) prefMap.get("vendorQuotationGenerationMail") : false);
        }
        if (prefMap.containsKey("vendorQuotationUpdationMail")) {
            documentEmailSettings.setVendorQuotationUpdationMail(prefMap.get("vendorQuotationUpdationMail") != null ? (Boolean) prefMap.get("vendorQuotationUpdationMail") : false);
        }
        if (prefMap.containsKey("purchaseOrderGenerationMail")) {
            documentEmailSettings.setPurchaseOrderGenerationMail(prefMap.get("purchaseOrderGenerationMail") != null ? (Boolean) prefMap.get("purchaseOrderGenerationMail") : false);
        }
        if (prefMap.containsKey("purchaseOrderUpdationMail")) {
            documentEmailSettings.setPurchaseOrderUpdationMail(prefMap.get("purchaseOrderUpdationMail") != null ? (Boolean) prefMap.get("purchaseOrderUpdationMail") : false);
        }
        if (prefMap.containsKey("goodsReceiptGenerationMail")) {
            documentEmailSettings.setGoodsReceiptGenerationMail(prefMap.get("goodsReceiptGenerationMail") != null ? (Boolean) prefMap.get("goodsReceiptGenerationMail") : false);
        }
        if (prefMap.containsKey("goodsReceiptUpdationMail")) {
            documentEmailSettings.setGoodsReceiptUpdationMail(prefMap.get("goodsReceiptUpdationMail") != null ? (Boolean) prefMap.get("goodsReceiptUpdationMail") : false);
        }
        if (prefMap.containsKey("purchaseReturnGenerationMail")) {
            documentEmailSettings.setPurchaseReturnGenerationMail(prefMap.get("purchaseReturnGenerationMail") != null ? (Boolean) prefMap.get("purchaseReturnGenerationMail") : false);
        }
        if (prefMap.containsKey("purchaseReturnUpdationMail")) {
            documentEmailSettings.setPurchaseReturnUpdationMail(prefMap.get("purchaseReturnUpdationMail") != null ? (Boolean) prefMap.get("purchaseReturnUpdationMail") : false);
        }
        if (prefMap.containsKey("vendorPaymentGenerationMail")) {
            documentEmailSettings.setVendorPaymentGenerationMail(prefMap.get("vendorPaymentGenerationMail") != null ? (Boolean) prefMap.get("vendorPaymentGenerationMail") : false);
        }
        if (prefMap.containsKey("vendorPaymentUpdationMail")) {
            documentEmailSettings.setVendorPaymentUpdationMail(prefMap.get("vendorPaymentUpdationMail") != null ? (Boolean) prefMap.get("vendorPaymentUpdationMail") : false);
        }
        if (prefMap.containsKey("debitNoteGenerationMail")) {
            documentEmailSettings.setDebitNoteGenerationMail(prefMap.get("debitNoteGenerationMail") != null ? (Boolean) prefMap.get("debitNoteGenerationMail") : false);
        }
        if (prefMap.containsKey("debitNoteUpdationMail")) {
            documentEmailSettings.setDebitNoteUpdationMail(prefMap.get("debitNoteUpdationMail") != null ? (Boolean) prefMap.get("debitNoteUpdationMail") : false);
        }
        if (prefMap.containsKey("customerQuotationGenerationMail")) {
            documentEmailSettings.setCustomerQuotationGenerationMail(prefMap.get("customerQuotationGenerationMail") != null ? (Boolean) prefMap.get("customerQuotationGenerationMail") : false);
        }
        if (prefMap.containsKey("customerQuotationUpdationMail")) {
            documentEmailSettings.setCustomerQuotationUpdationMail(prefMap.get("customerQuotationUpdationMail") != null ? (Boolean) prefMap.get("customerQuotationUpdationMail") : false);
        }
        if (prefMap.containsKey("salesOrderGenerationMail")) {
            documentEmailSettings.setSalesOrderGenerationMail(prefMap.get("salesOrderGenerationMail") != null ? (Boolean) prefMap.get("salesOrderGenerationMail") : false);
        }
        if (prefMap.containsKey("salesOrderUpdationMail")) {
            documentEmailSettings.setSalesOrderUpdationMail(prefMap.get("salesOrderUpdationMail") != null ? (Boolean) prefMap.get("salesOrderUpdationMail") : false);
        }
        if (prefMap.containsKey("deleveryOrderGenerationMail")) {
            documentEmailSettings.setDeleveryOrderGenerationMail(prefMap.get("deleveryOrderGenerationMail") != null ? (Boolean) prefMap.get("deleveryOrderGenerationMail") : false);
        }
        if (prefMap.containsKey("deleveryOrderUpdationMail")) {
            documentEmailSettings.setDeleveryOrderUpdationMail(prefMap.get("deleveryOrderUpdationMail") != null ? (Boolean) prefMap.get("deleveryOrderUpdationMail") : false);
        }
        if (prefMap.containsKey("salesReturnGenerationMail")) {
            documentEmailSettings.setSalesReturnGenerationMail(prefMap.get("salesReturnGenerationMail") != null ? (Boolean) prefMap.get("salesReturnGenerationMail") : false);
        }
        if (prefMap.containsKey("salesReturnUpdationMail")) {
            documentEmailSettings.setSalesReturnUpdationMail(prefMap.get("salesReturnUpdationMail") != null ? (Boolean) prefMap.get("salesReturnUpdationMail") : false);
        }
        if (prefMap.containsKey("receiptGenerationMail")) {
            documentEmailSettings.setReceiptGenerationMail(prefMap.get("receiptGenerationMail") != null ? (Boolean) prefMap.get("receiptGenerationMail") : false);
        }
        if (prefMap.containsKey("receiptUpdationMail")) {
            documentEmailSettings.setReceiptUpdationMail(prefMap.get("receiptUpdationMail") != null ? (Boolean) prefMap.get("receiptUpdationMail") : false);
        }
        if (prefMap.containsKey("creditNoteGenerationMail")) {
            documentEmailSettings.setCreditNoteGenerationMail(prefMap.get("creditNoteGenerationMail") != null ? (Boolean) prefMap.get("creditNoteGenerationMail") : false);
        }
        if (prefMap.containsKey("creditNoteUpdationMail")) {
            documentEmailSettings.setCreditNoteUpdationMail(prefMap.get("creditNoteUpdationMail") != null ? (Boolean) prefMap.get("creditNoteUpdationMail") : false);
        }
        // Lease Fixed Asset
        
        if (prefMap.containsKey("leaseQuotationGenerationMail")) {
            documentEmailSettings.setLeaseQuotationGenerationMail(prefMap.get("leaseQuotationGenerationMail") != null ? (Boolean) prefMap.get("leaseQuotationGenerationMail") : false);
        }
        if (prefMap.containsKey("leaseQuotationUpdationMail")) {
            documentEmailSettings.setLeaseQuotationUpdationMail(prefMap.get("leaseQuotationUpdationMail") != null ? (Boolean) prefMap.get("leaseQuotationUpdationMail") : false);
        }
        if (prefMap.containsKey("leaseOrderGenerationMail")) {
            documentEmailSettings.setLeaseOrderGenerationMail(prefMap.get("leaseOrderGenerationMail") != null ? (Boolean) prefMap.get("leaseOrderGenerationMail") : false);
        }
        if (prefMap.containsKey("leaseOrderUpdationMail")) {
            documentEmailSettings.setLeaseOrderUpdationMail(prefMap.get("leaseOrderUpdationMail") != null ? (Boolean) prefMap.get("leaseOrderUpdationMail") : false);
        }
        if (prefMap.containsKey("leaseDeliveryOrderGenerationMail")) {
            documentEmailSettings.setLeaseDeliveryOrderGenerationMail(prefMap.get("leaseDeliveryOrderGenerationMail") != null ? (Boolean) prefMap.get("leaseDeliveryOrderGenerationMail") : false);
        }
        if (prefMap.containsKey("leaseDeliveryOrderUpdationMail")) {
            documentEmailSettings.setLeaseDeliveryOrderUpdationMail(prefMap.get("leaseDeliveryOrderUpdationMail") != null ? (Boolean) prefMap.get("leaseDeliveryOrderUpdationMail") : false);
        }
        if (prefMap.containsKey("leaseReturnGenerationMail")) {
            documentEmailSettings.setLeaseReturnGenerationMail(prefMap.get("leaseReturnGenerationMail") != null ? (Boolean) prefMap.get("leaseReturnGenerationMail") : false);
        }
        if (prefMap.containsKey("leaseReturnUpdationMail")) {
            documentEmailSettings.setLeaseReturnUpdationMail(prefMap.get("leaseReturnUpdationMail") != null ? (Boolean) prefMap.get("leaseReturnUpdationMail") : false);
        }
        if (prefMap.containsKey("leaseInvoiceGenerationMail")) {
            documentEmailSettings.setLeaseInvoiceGenerationMail(prefMap.get("leaseInvoiceGenerationMail") != null ? (Boolean) prefMap.get("leaseInvoiceGenerationMail") : false);
        }
        if (prefMap.containsKey("leaseInvoiceUpdationMail")) {
            documentEmailSettings.setLeaseInvoiceUpdationMail(prefMap.get("leaseInvoiceUpdationMail") != null ? (Boolean) prefMap.get("leaseInvoiceUpdationMail") : false);
        }
        if (prefMap.containsKey("leaseContractGenerationMail")) {
            documentEmailSettings.setLeaseContractGenerationMail(prefMap.get("leaseContractGenerationMail") != null ? (Boolean) prefMap.get("leaseContractGenerationMail") : false);
        }
        if (prefMap.containsKey("leaseContractUpdationMail")) {
            documentEmailSettings.setLeaseContractUpdationMail(prefMap.get("leaseContractUpdationMail") != null ? (Boolean) prefMap.get("leaseContractUpdationMail") : false);
        }
        
        // Consignment Stock Sales Module
        if (prefMap.containsKey("consignmentReqGenerationMail")) {
            documentEmailSettings.setConsignmentReqGenerationMail(prefMap.get("consignmentReqGenerationMail") != null ? (Boolean) prefMap.get("consignmentReqGenerationMail") : false);
        }
        if (prefMap.containsKey("consignmentReqUpdationMail")) {
            documentEmailSettings.setConsignmentReqUpdationMail(prefMap.get("consignmentReqUpdationMail") != null ? (Boolean) prefMap.get("consignmentReqUpdationMail") : false);
        }
        if (prefMap.containsKey("consignmentDOGenerationMail")) {
            documentEmailSettings.setConsignmentDOGenerationMail(prefMap.get("consignmentDOGenerationMail") != null ? (Boolean) prefMap.get("consignmentDOGenerationMail") : false);
        }
        if (prefMap.containsKey("consignmentDOUpdationMail")) {
            documentEmailSettings.setConsignmentDOUpdationMail(prefMap.get("consignmentDOUpdationMail") != null ? (Boolean) prefMap.get("consignmentDOUpdationMail") : false);
        }
        if (prefMap.containsKey("consignmentInvoiceGenerationMail")) {
            documentEmailSettings.setConsignmentInvoiceGenerationMail(prefMap.get("consignmentInvoiceGenerationMail") != null ? (Boolean) prefMap.get("consignmentInvoiceGenerationMail") : false);
        }
        if (prefMap.containsKey("consignmentInvoiceUpdationMail")) {
            documentEmailSettings.setConsignmentInvoiceUpdationMail(prefMap.get("consignmentInvoiceUpdationMail") != null ? (Boolean) prefMap.get("consignmentInvoiceUpdationMail") : false);
        }
        if (prefMap.containsKey("consignmentReturnGenerationMail")) {
            documentEmailSettings.setConsignmentReturnGenerationMail(prefMap.get("consignmentReturnGenerationMail") != null ? (Boolean) prefMap.get("consignmentReturnGenerationMail") : false);
        }
        if (prefMap.containsKey("consignmentReturnUpdationMail")) {
            documentEmailSettings.setConsignmentReturnUpdationMail(prefMap.get("consignmentReturnUpdationMail") != null ? (Boolean) prefMap.get("consignmentReturnUpdationMail") : false);
        }
        
        // Consignment Stock Purchase  Module
        if (prefMap.containsKey("consignmentPReqGenerationMail")) {
            documentEmailSettings.setConsignmentPReqGenerationMail(prefMap.get("consignmentPReqGenerationMail") != null ? (Boolean) prefMap.get("consignmentPReqGenerationMail") : false);
        }
        if (prefMap.containsKey("consignmentPReqUpdationMail")) {
            documentEmailSettings.setConsignmentPReqUpdationMail(prefMap.get("consignmentPReqUpdationMail") != null ? (Boolean) prefMap.get("consignmentPReqUpdationMail") : false);
        }
        if (prefMap.containsKey("consignmentPDOGenerationMail")) {
            documentEmailSettings.setConsignmentPDOGenerationMail(prefMap.get("consignmentPDOGenerationMail") != null ? (Boolean) prefMap.get("consignmentPDOGenerationMail") : false);
        }
        if (prefMap.containsKey("consignmentPDOUpdationMail")) {
            documentEmailSettings.setConsignmentPDOUpdationMail(prefMap.get("consignmentPDOUpdationMail") != null ? (Boolean) prefMap.get("consignmentPDOUpdationMail") : false);
        }
        if (prefMap.containsKey("consignmentPInvoiceGenerationMail")) {
            documentEmailSettings.setConsignmentPInvoiceGenerationMail(prefMap.get("consignmentPInvoiceGenerationMail") != null ? (Boolean) prefMap.get("consignmentPInvoiceGenerationMail") : false);
        }
        if (prefMap.containsKey("consignmentPInvoiceUpdationMail")) {
            documentEmailSettings.setConsignmentPInvoiceUpdationMail(prefMap.get("consignmentPInvoiceUpdationMail") != null ? (Boolean) prefMap.get("consignmentPInvoiceUpdationMail") : false);
        }
        if (prefMap.containsKey("consignmentPReturnGenerationMail")) {
            documentEmailSettings.setConsignmentPReturnGenerationMail(prefMap.get("consignmentPReturnGenerationMail") != null ? (Boolean) prefMap.get("consignmentPReturnGenerationMail") : false);
        }
        if (prefMap.containsKey("consignmentPReturnUpdationMail")) {
            documentEmailSettings.setConsignmentPReturnUpdationMail(prefMap.get("consignmentPReturnUpdationMail") != null ? (Boolean) prefMap.get("consignmentPReturnUpdationMail") : false);
        }
        //Asset Module
        if (prefMap.containsKey("assetPurchaseReqGenerationMail")) {
            documentEmailSettings.setAssetPurchaseReqGenerationMail(prefMap.get("assetPurchaseReqGenerationMail") != null ? (Boolean) prefMap.get("assetPurchaseReqGenerationMail") : false);
        }
        if (prefMap.containsKey("assetPurchaseReqUpdationMail")) {
            documentEmailSettings.setAssetPurchaseReqUpdationMail(prefMap.get("assetPurchaseReqUpdationMail") != null ? (Boolean) prefMap.get("assetPurchaseReqUpdationMail") : false);
        }
        if (prefMap.containsKey("assetVendorQuotationGenerationMail")) {
            documentEmailSettings.setAssetVendorQuotationGenerationMail(prefMap.get("assetVendorQuotationGenerationMail") != null ? (Boolean) prefMap.get("assetVendorQuotationGenerationMail") : false);
        }
        if (prefMap.containsKey("assetVendorQuotationUpdationMail")) {
            documentEmailSettings.setAssetVendorQuotationUpdationMail(prefMap.get("assetVendorQuotationUpdationMail") != null ? (Boolean) prefMap.get("assetVendorQuotationUpdationMail") : false);
        }
        if (prefMap.containsKey("assetPurchaseOrderGenerationMail")) {
            documentEmailSettings.setAssetPurchaseOrderGenerationMail(prefMap.get("assetPurchaseOrderGenerationMail") != null ? (Boolean) prefMap.get("assetPurchaseOrderGenerationMail") : false);
        }
        if (prefMap.containsKey("assetPurchaseOrderUpdationMail")) {
            documentEmailSettings.setAssetPurchaseOrderUpdationMail(prefMap.get("assetPurchaseOrderUpdationMail") != null ? (Boolean) prefMap.get("assetPurchaseOrderUpdationMail") : false);
        }
        if (prefMap.containsKey("assetPurchaseInvoiceGenerationMail")) {
            documentEmailSettings.setAssetPurchaseInvoiceGenerationMail(prefMap.get("assetPurchaseInvoiceGenerationMail") != null ? (Boolean) prefMap.get("assetPurchaseInvoiceGenerationMail") : false);
        }
        if (prefMap.containsKey("assetPurchaseInvoiceUpdationMail")) {
            documentEmailSettings.setAssetPurchaseInvoiceUpdationMail(prefMap.get("assetPurchaseInvoiceUpdationMail") != null ? (Boolean) prefMap.get("assetPurchaseInvoiceUpdationMail") : false);
        }
        if (prefMap.containsKey("assetGoodsReceiptGenerationMail")) {
            documentEmailSettings.setAssetGoodsReceiptGenerationMail(prefMap.get("assetGoodsReceiptGenerationMail") != null ? (Boolean) prefMap.get("assetGoodsReceiptGenerationMail") : false);
        }
        if (prefMap.containsKey("assetGoodsReceiptUpdationMail")) {
            documentEmailSettings.setAssetGoodsReceiptUpdationMail(prefMap.get("assetGoodsReceiptUpdationMail") != null ? (Boolean) prefMap.get("assetGoodsReceiptUpdationMail") : false);
        }
        if (prefMap.containsKey("assetPurchaseReturnGenerationMail")) {
            documentEmailSettings.setAssetPurchaseReturnGenerationMail(prefMap.get("assetPurchaseReturnGenerationMail") != null ? (Boolean) prefMap.get("assetPurchaseReturnGenerationMail") : false);
        }
        if (prefMap.containsKey("assetPurchaseReturnUpdationMail")) {
            documentEmailSettings.setAssetPurchaseReturnUpdationMail(prefMap.get("assetPurchaseReturnUpdationMail") != null ? (Boolean) prefMap.get("assetPurchaseReturnUpdationMail") : false);
        }
        if (prefMap.containsKey("assetDisposalInvoiceGenerationMail")) {
            documentEmailSettings.setAssetDisposalInvoiceGenerationMail(prefMap.get("assetDisposalInvoiceGenerationMail") != null ? (Boolean) prefMap.get("assetDisposalInvoiceGenerationMail") : false);
        }
        if (prefMap.containsKey("assetDisposalInvoiceUpdationMail")) {
            documentEmailSettings.setAssetDisposalInvoiceUpdationMail(prefMap.get("assetDisposalInvoiceUpdationMail") != null ? (Boolean) prefMap.get("assetDisposalInvoiceUpdationMail") : false);
        }
        if (prefMap.containsKey("assetDeliveryOrderGenerationMail")) {
            documentEmailSettings.setAssetDeliveryOrderGenerationMail(prefMap.get("assetDeliveryOrderGenerationMail") != null ? (Boolean) prefMap.get("assetDeliveryOrderGenerationMail") : false);
        }
        if (prefMap.containsKey("assetDeliveryOrderUpdationMail")) {
            documentEmailSettings.setAssetDeliveryOrderUpdationMail(prefMap.get("assetDeliveryOrderUpdationMail") != null ? (Boolean) prefMap.get("assetDeliveryOrderUpdationMail") : false);
        }
        if (prefMap.containsKey("assetSalesReturnGenerationMail")) {
            documentEmailSettings.setAssetSalesReturnGenerationMail(prefMap.get("assetSalesReturnGenerationMail") != null ? (Boolean) prefMap.get("assetSalesReturnGenerationMail") : false);
        }
        if (prefMap.containsKey("assetSalesReturnUpdationMail")) {
            documentEmailSettings.setAssetSalesReturnUpdationMail(prefMap.get("assetSalesReturnUpdationMail") != null ? (Boolean) prefMap.get("assetSalesReturnUpdationMail") : false);
        }
        
        
        if (prefMap.containsKey("salesInvoiceGenerationMail")) {
            documentEmailSettings.setSalesInvoiceGenerationMail(prefMap.get("salesInvoiceGenerationMail") != null ? (Boolean) prefMap.get("salesInvoiceGenerationMail") : false);
        }
        if (prefMap.containsKey("salesInvoiceUpdationMail")) {
            documentEmailSettings.setSalesInvoiceUpdationMail(prefMap.get("salesInvoiceUpdationMail") != null ? (Boolean) prefMap.get("salesInvoiceUpdationMail") : false);
        }
        if (prefMap.containsKey("purchaseInvoiceGenerationMail")) {
            documentEmailSettings.setPurchaseInvoiceGenerationMail(prefMap.get("purchaseInvoiceGenerationMail") != null ? (Boolean) prefMap.get("purchaseInvoiceGenerationMail") : false);
        }
        if (prefMap.containsKey("purchaseInvoiceUpdationMail")) {
            documentEmailSettings.setPurchaseInvoiceUpdationMail(prefMap.get("purchaseInvoiceUpdationMail") != null ? (Boolean) prefMap.get("purchaseInvoiceUpdationMail") : false);
        }
        if (prefMap.containsKey("recurringInvoiceMail")) {
            documentEmailSettings.setRecurringInvoiceMail(prefMap.get("recurringInvoiceMail") != null ? (Boolean) prefMap.get("recurringInvoiceMail") : false);
        }
        if (prefMap.containsKey("consignmentRequestApproval")) {
            documentEmailSettings.setConsignmentRequestApproval(prefMap.get("consignmentRequestApproval") != null ? (Boolean) prefMap.get("consignmentRequestApproval") : false);
        }
        if (prefMap.containsKey("qtyBelowReorderLevelMail")) {
            documentEmailSettings.setQtyBelowReorderLevelMail(prefMap.get("qtyBelowReorderLevelMail") != null ? (Boolean) prefMap.get("qtyBelowReorderLevelMail") : false);
        }
        if (prefMap.containsKey("RFQGenerationMail")) {
            documentEmailSettings.setRFQGenerationMail(prefMap.get("RFQGenerationMail") != null ? (Boolean) prefMap.get("RFQGenerationMail") : false);
        }
        if (prefMap.containsKey("RFQUpdationMail")) {
            documentEmailSettings.setRFQUpdationMail(prefMap.get("RFQUpdationMail") != null ? (Boolean) prefMap.get("RFQUpdationMail") : false);
        }
        if (prefMap.containsKey("isCustShipAddressInPurchase")) {
            documentEmailSettings.setCustShippingAddressInPurDoc((Boolean) prefMap.get("isCustShipAddressInPurchase"));
        }
        return documentEmailSettings;
    }
    public ExtraCompanyPreferences buildExtraPreferences(ExtraCompanyPreferences preferences, HashMap<String, Object> prefMap) {
        if (prefMap.containsKey("isDeferredRevenueRecognition")) {
            preferences.setDeferredRevenueRecognition(prefMap.get("isDeferredRevenueRecognition") != null ? (Boolean) prefMap.get("isDeferredRevenueRecognition") : false);
        }
        if (prefMap.containsKey("showAllAccount")) {
            preferences.setShowAllAccount(prefMap.get("showAllAccount") != null ? (Boolean) prefMap.get("showAllAccount") : false);
        }
        if (prefMap.containsKey("showallaccountsinbs")) {
            preferences.setShowallaccountsinbs(prefMap.get("showallaccountsinbs") != null ? (Boolean) prefMap.get("showallaccountsinbs") : false);
        }
        if (prefMap.containsKey("showChildAccountsInTb")) {
            preferences.setShowChildAccountsInTb(prefMap.get("showChildAccountsInTb") != null ? (Boolean) prefMap.get("showChildAccountsInTb") : false);
        }
        if (prefMap.containsKey("showChildAccountsInPnl")) {
            preferences.setShowChildAccountsInPnl(prefMap.get("showChildAccountsInPnl") != null ? (Boolean) prefMap.get("showChildAccountsInPnl") : false);
        }
        if (prefMap.containsKey("showChildAccountsInBS")) {
            preferences.setShowChildAccountsInBS(prefMap.get("showChildAccountsInBS") != null ? (Boolean) prefMap.get("showChildAccountsInBS") : false);
        }
       
        if (prefMap.containsKey("gstIncomeGroup")) {
            preferences.setGstIncomeGroup(prefMap.get("gstIncomeGroup") != null ? (Boolean) prefMap.get("gstIncomeGroup") : false);
        }
        if (prefMap.containsKey("paymentMethodAsCard")) {
            preferences.setPaymentMethodAsCard(prefMap.get("paymentMethodAsCard") != null ? (Boolean) prefMap.get("paymentMethodAsCard") : false);
        }
        if (prefMap.containsKey("jobOrderItemFlow")) {
            preferences.setJobOrderItemFlow(prefMap.get("jobOrderItemFlow") != null ? (Boolean) prefMap.get("jobOrderItemFlow") : false);
        }
        if (prefMap.containsKey("usersVisibilityFlow")) {
            preferences.setUsersVisibilityFlow(prefMap.get("usersVisibilityFlow") != null ? (Boolean) prefMap.get("usersVisibilityFlow") : false);
        }
        if (prefMap.containsKey("usersspecificinfoFlow")) {
            preferences.setusersspecificinfoFlow(prefMap.get("usersspecificinfoFlow") != null ? (Boolean) prefMap.get("usersspecificinfoFlow") : false);
        }
        if (prefMap.containsKey("jobWorkOutFlow")) {
            preferences.setJobWorkOutFlow(prefMap.get("jobWorkOutFlow") != null ? (Boolean) prefMap.get("jobWorkOutFlow") : false);
        }
        if (prefMap.containsKey("showChildAccountsInGl")) {
            preferences.setShowChildAccountsInGl(prefMap.get("showChildAccountsInGl") != null ? (Boolean) prefMap.get("showChildAccountsInGl") : false);
        }        
        if (prefMap.containsKey("showAllAccountInGl")) {
            preferences.setShowAllAccountInGl(prefMap.get("showAllAccountInGl") != null ? (Boolean) prefMap.get("showAllAccountInGl") : false);
        }
        if (prefMap.containsKey("showAllAccountsInPnl")) {
            preferences.setShowAllAccountsInPnl(prefMap.get("showAllAccountsInPnl") != null ? (Boolean) prefMap.get("showAllAccountsInPnl") : false);
        }    
        if (prefMap.containsKey("isnegativestockforlocwar")) {
            preferences.setIsnegativestockforlocwar(prefMap.get("isnegativestockforlocwar") != null ? (Boolean) prefMap.get("isnegativestockforlocwar") : false);
        }    
        if (prefMap.containsKey("isAllowQtyMoreThanLinkedDoc")) {
            preferences.setIsAllowQtyMoreThanLinkedDoc(prefMap.get("isAllowQtyMoreThanLinkedDoc") != null ? (Boolean) prefMap.get("isAllowQtyMoreThanLinkedDoc") : false);
        }    
        if (prefMap.containsKey("isAllowQtyMoreThanLinkedDocCross")) {
            preferences.setIsAllowQtyMoreThanLinkedDocCross(prefMap.get("isAllowQtyMoreThanLinkedDocCross") != null ? (Boolean) prefMap.get("isAllowQtyMoreThanLinkedDocCross") : false);
        }    
        if (prefMap.containsKey("salesAccount")) {
            preferences.setSalesAccount(prefMap.get("salesAccount") != null ? (String) prefMap.get("salesAccount") : "");
        }
        if (prefMap.containsKey("loandisbursementaccount")) {
            preferences.setLoanAccount(prefMap.get("loandisbursementaccount") != null ? (String) prefMap.get("loandisbursementaccount") : "");
        }
        if (prefMap.containsKey("loaninterestaccount")) {
            preferences.setLoanInterestAccount(prefMap.get("loaninterestaccount") != null ? (String) prefMap.get("loaninterestaccount") : "");
        }
        if (prefMap.containsKey("gstaccountforbaddebt")) {
            preferences.setGstAccountForBadDebt(prefMap.get("gstaccountforbaddebt") != null ? (String) prefMap.get("gstaccountforbaddebt") : "");
        }
        if (prefMap.containsKey("gstbaddebtreleifaccount")) {
            preferences.setGstBadDebtsReleifAccount(prefMap.get("gstbaddebtreleifaccount") != null ? (String) prefMap.get("gstbaddebtreleifaccount") : "");
        }
        if (prefMap.containsKey("gstbaddebtrecoveraccount")) {
            preferences.setGstBadDebtsRecoverAccount(prefMap.get("gstbaddebtrecoveraccount") != null ? (String) prefMap.get("gstbaddebtrecoveraccount") : "");
        }
        if (prefMap.containsKey("gstbaddebtreleifpurchaseaccount")) { //ERP-10400, For Purchase
            preferences.setGstBadDebtsReleifPurchaseAccount(prefMap.get("gstbaddebtreleifpurchaseaccount") != null ? (String) prefMap.get("gstbaddebtreleifpurchaseaccount") : "");
        }
        if (prefMap.containsKey("gstbaddebtrecoverpurchaseaccount")) {
            preferences.setGstBadDebtsRecoverPurchaseAccount(prefMap.get("gstbaddebtrecoverpurchaseaccount") != null ? (String) prefMap.get("gstbaddebtrecoverpurchaseaccount") : "");
        }
        if (prefMap.containsKey("gstbaddebtsuspenseaccount")) {
            preferences.setGstBadDebtsSuspenseAccount(prefMap.get("gstbaddebtsuspenseaccount") != null ? (String) prefMap.get("gstbaddebtsuspenseaccount") : "");
        }
        if (prefMap.containsKey("inputtaxadjustmentaccount")) {
            preferences.setInputTaxAdjustmentAccount(prefMap.get("inputtaxadjustmentaccount") != null ? (String) prefMap.get("inputtaxadjustmentaccount") : "");
        }
        if (prefMap.containsKey("taxCgaMalaysian")) {
            preferences.setTaxAllowForCgaMalaysian(prefMap.get("taxCgaMalaysian") != null ? (String) prefMap.get("taxCgaMalaysian") : "");
        }
        if (prefMap.containsKey("freeGiftJEAccount")) {
            preferences.setFreeGiftJEAccount(prefMap.get("freeGiftJEAccount") != null ? (String) prefMap.get("freeGiftJEAccount") : "");
        }
        if (prefMap.containsKey("outputtaxadjustmentaccount")) {
            preferences.setOutputTaxAdjustmentAccount(prefMap.get("outputtaxadjustmentaccount") != null ? (String) prefMap.get("outputtaxadjustmentaccount") : "");
        }
        if (prefMap.containsKey("salesRevenueRecognitionAccount")) {
            preferences.setSalesRevenueRecognitionAccount(prefMap.get("salesRevenueRecognitionAccount") != null ? (String) prefMap.get("salesRevenueRecognitionAccount") : "");
        }
        if (prefMap.containsKey("stockValuationFlag")) {
            preferences.setStockValuationFlag(prefMap.get("stockValuationFlag") != null ? (Boolean) prefMap.get("stockValuationFlag") : true);
        }
        if (prefMap.containsKey("leaseManagementFlag")) {
            preferences.setLeaseManagementFlag(prefMap.get("leaseManagementFlag") != null ? (Boolean) prefMap.get("leaseManagementFlag") : true);
        }
        if (prefMap.containsKey("consignmentSalesManagementFlag")) {
            preferences.setConsignmentSalesManagementFlag(prefMap.get("consignmentSalesManagementFlag") != null ? (Boolean) prefMap.get("consignmentSalesManagementFlag") : true);
        }
        if (prefMap.containsKey("consignmentPurchaseManagementFlag")) {
            preferences.setConsignmentPurchaseManagementFlag(prefMap.get("consignmentPurchaseManagementFlag") != null ? (Boolean) prefMap.get("consignmentPurchaseManagementFlag") : true);
        }
       
        if (prefMap.containsKey("systemManagementFlag")) {
            preferences.setSystemManagementFlag(prefMap.get("systemManagementFlag") != null ? (Boolean) prefMap.get("systemManagementFlag") : true);
        }
        if (prefMap.containsKey("masterManagementFlag")) {
            preferences.setMasterManagementFlag(prefMap.get("masterManagementFlag") != null ? (Boolean) prefMap.get("masterManagementFlag") : true);
        }
        if (prefMap.containsKey("generalledgerManagementFlag")) {
            preferences.setGeneralledgerManagementFlag(prefMap.get("generalledgerManagementFlag") != null ? (Boolean) prefMap.get("generalledgerManagementFlag") : true);
        }
        if (prefMap.containsKey("accountsreceivablesalesFlag")) {
            preferences.setAccountsreceivablesalesFlag(prefMap.get("accountsreceivablesalesFlag") != null ? (Boolean) prefMap.get("accountsreceivablesalesFlag") : true);
        }
        if (prefMap.containsKey("accountpayableManagementFlag")) {
            preferences.setAccountpayableManagementFlag(prefMap.get("accountpayableManagementFlag") != null ? (Boolean) prefMap.get("accountpayableManagementFlag") : true);
        }
        if (prefMap.containsKey("securityGateEntryFlag")) {
            preferences.setSecurityGateEntryFlag(prefMap.get("securityGateEntryFlag") != null ? (Boolean) prefMap.get("securityGateEntryFlag") : true);
        }
        if (prefMap.containsKey("assetManagementFlag")) {
            preferences.setAssetManagementFlag(prefMap.get("assetManagementFlag") != null ? (Boolean) prefMap.get("assetManagementFlag") : true);
        }
        if (prefMap.containsKey("statutoryManagementFlag")) {
            preferences.setStatutoryManagementFlag(prefMap.get("statutoryManagementFlag") != null ? (Boolean) prefMap.get("statutoryManagementFlag") : true);
        }
        if (prefMap.containsKey("miscellaneousManagementFlag")) {
            preferences.setMiscellaneousManagementFlag(prefMap.get("miscellaneousManagementFlag") != null ? (Boolean) prefMap.get("miscellaneousManagementFlag"): true);
        }
        if (prefMap.containsKey("onlyBaseCurrency")) {
            preferences.setOnlyBaseCurrency(prefMap.get("onlyBaseCurrency") != null ? (Boolean) prefMap.get("onlyBaseCurrency") : false);
        }
        if (prefMap.containsKey("packingdolist")) {
            preferences.setPackingdolist(prefMap.get("packingdolist") != null ? (Boolean) prefMap.get("packingdolist") : false);
        }
        if (prefMap.containsKey("versionslist")) {
            preferences.setVersionslist(prefMap.get("versionslist") != null ? (Boolean) prefMap.get("versionslist") : false);
        }
        if (prefMap.containsKey("activateProductComposition")) {
            preferences.setActivateProductComposition(prefMap.get("activateProductComposition") != null ? (Boolean) prefMap.get("activateProductComposition") : false);
        }
        if (prefMap.containsKey("noOfDaysforValidTillField")) {
            preferences.setNoOfDaysforValidTillField(prefMap.get("noOfDaysforValidTillField") != null ? Integer.parseInt(prefMap.get("noOfDaysforValidTillField").toString()) : -1);
        }
        if (prefMap.containsKey("recurringDeferredRevenueRecognition")) {
            preferences.setRecurringDeferredRevenueRecognition(prefMap.get("recurringDeferredRevenueRecognition") != null ? (Boolean) prefMap.get("recurringDeferredRevenueRecognition") : false);
        }
        if (prefMap.containsKey("autoPopulateMappedProduct")) {
            preferences.setAutoPopulateMappedProduct((Boolean) prefMap.get("autoPopulateMappedProduct"));
        }
        if (prefMap.containsKey("columnPref")) {
            updateColumnPrefJson(preferences,prefMap);    // this method updates the columnPref value iinstead of replacing old value with new one..  
        }
        if (prefMap.containsKey("lastsyncwithpm")) {
            preferences.setLastsyncwithpm(prefMap.get("lastsyncwithpm") != null ? (Date) prefMap.get("lastsyncwithpm") : null);
        }
        if (prefMap.containsKey("mslastsyncwithpm")) {
            preferences.setMslastsyncwithpm(prefMap.get("mslastsyncwithpm") != null ? (Date) prefMap.get("mslastsyncwithpm") : null);
        }
        if (prefMap.containsKey("productPriceinMultipleCurrency")) {
            preferences.setProductPriceinMultipleCurrency(prefMap.get("productPriceinMultipleCurrency") != null ? (Boolean) prefMap.get("productPriceinMultipleCurrency") : false);
        }
        if (prefMap.containsKey("showAutoGeneratedChequeNumber")) {
            preferences.setShowAutoGeneratedChequeNumber(prefMap.get("showAutoGeneratedChequeNumber") != null ? (Boolean) prefMap.get("showAutoGeneratedChequeNumber") : false);
        }
        if (prefMap.containsKey("isSalesOrderCreatedForCustomer")) {
            preferences.setSalesOrderCreatedForCustomer(prefMap.get("isSalesOrderCreatedForCustomer") != null ? (Boolean) prefMap.get("isSalesOrderCreatedForCustomer") : false);
        }
        if (prefMap.containsKey("isOutstandingInvoiceForCustomer")) {
            preferences.setOutstandingInvoiceForCustomer(prefMap.get("isOutstandingInvoiceForCustomer") != null ? (Boolean) prefMap.get("isOutstandingInvoiceForCustomer") : false);
        }
        if (prefMap.containsKey("isMinMaxOrdering")) {
            preferences.setMinMaxOrdering(prefMap.get("isMinMaxOrdering") != null ? (Boolean) prefMap.get("isMinMaxOrdering") : false);
        }
        if (prefMap.containsKey("blockPOcreationwithMinValue")) {
            preferences.setBlockPOcreationWithMinPricevalue(prefMap.get("blockPOcreationwithMinValue") != null ? (Boolean) prefMap.get("blockPOcreationwithMinValue") : false);
        }
        if (prefMap.containsKey("DashBoardImageFlag")) {
            preferences.setDashBordImageFlag(prefMap.get("DashBoardImageFlag")!=null?(Boolean)prefMap.get("DashBoardImageFlag"):true);
        }

        if (prefMap.containsKey("activateCRMIntegration")) {
            preferences.setActivateCRMIntegration(prefMap.get("activateCRMIntegration") != null ? (Boolean) prefMap.get("activateCRMIntegration") : true);
        }      
        if (prefMap.containsKey("activateLMSIntegration")) {
            preferences.setLMSIntegration(prefMap.get("activateLMSIntegration") != null ? (Boolean) prefMap.get("activateLMSIntegration") : true);
        }      
        if (prefMap.containsKey("activateGroupCompanyIntegration")) {
            preferences.setActivateGroupCompaniesFlag(prefMap.get("activateGroupCompanyIntegration") != null ? (Boolean) prefMap.get("activateGroupCompanyIntegration") : true);
        }

        if (prefMap.containsKey("isPOSIntegration")) {
            preferences.setIsPOSIntegration(prefMap.get("isPOSIntegration") != null ? (Boolean) prefMap.get("isPOSIntegration") : true);
        }      
        if (prefMap.containsKey("isCloseRegisterMultipleTimes")) {
            preferences.setIsCloseRegisterMultipleTimes(prefMap.get("isCloseRegisterMultipleTimes") != null ? (Boolean) prefMap.get("isCloseRegisterMultipleTimes") : true);
        }      
        if (prefMap.containsKey("allowCustVenCodeEditing")) {
            preferences.setIsallowCustVenCodeEditing(prefMap.get("allowCustVenCodeEditing") != null ? (Boolean) prefMap.get("allowCustVenCodeEditing") : true);
        }      
        if (prefMap.containsKey("manyCreditDebit")) {
            preferences.setManyCreditDebit(prefMap.get("manyCreditDebit") != null ? (Boolean) prefMap.get("manyCreditDebit") : true);
        }
        if (prefMap.containsKey("customerForPOS")) {
            preferences.setCustomerForPOS(prefMap.get("customerForPOS") == null ? null : prefMap.get("customerForPOS").toString());
        }
        if (prefMap.containsKey("vendorForPOS")) {
            preferences.setVendorForPOS(prefMap.get("vendorForPOS") == null ? null : prefMap.get("vendorForPOS").toString());
        }
        if (prefMap.containsKey("gstEffectiveDate") && prefMap.get("gstEffectiveDate") != null) {
            preferences.setGstEffectiveDate((Date) prefMap.get("gstEffectiveDate"));
        }
        if (prefMap.containsKey("enableGST") && prefMap.get("enableGST") != null) {
            preferences.setEnableGST((Boolean) prefMap.get("enableGST"));
        }
        if (prefMap.containsKey(Constants.isMultiEntity) && prefMap.get(Constants.isMultiEntity) != null) {
            preferences.setIsMultiEntity((Boolean) prefMap.get(Constants.isMultiEntity));
        }
        if (prefMap.containsKey(Constants.isDimensionCreated) && prefMap.get(Constants.isDimensionCreated) != null) {
            preferences.setIsDimensionCreated((Boolean) prefMap.get(Constants.isDimensionCreated));
        }
        if (prefMap.containsKey("requestApprovalFlow")) {
            preferences.setRequestApprovalFlow(prefMap.get("requestApprovalFlow") != null ? (Boolean) prefMap.get("requestApprovalFlow") : false);
        }
        if (prefMap.containsKey("isIndianCompany")) {
            preferences.setAmountInIndianWord(prefMap.get("isIndianCompany") != null ? (Boolean) prefMap.get("isIndianCompany") : false);
        }
        if (prefMap.containsKey(Constants.PRODUCT_SEARCH_FLAG)) {
            preferences.setProductSearchingFlag(prefMap.get(Constants.PRODUCT_SEARCH_FLAG) != null ? Integer.parseInt((String)prefMap.get(Constants.PRODUCT_SEARCH_FLAG)) : 1);
        }
        
        String str;
        if (prefMap.containsKey("companyid")) {
            str = (String) prefMap.get("companyid");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setCompany((Company) get(Company.class, str));
            } else {
                preferences.setCompany(null);
            }
        }
        
        if (prefMap.containsKey("UomSchemaType")) {
            if(prefMap.get("UomSchemaType").equals(0) || prefMap.get("UomSchemaType").equals(1)){
                preferences.setUomSchemaType(prefMap.get("UomSchemaType").equals(1)? 1 :0); 
            }else{
            str = (String) prefMap.get("UomSchemaType");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setUomSchemaType(Integer.parseInt(str));
            } else {
                preferences.setUomSchemaType(0);
            }  
        }
        }
                
        if (prefMap.containsKey("activateIBG")) {
            preferences.setActivateIBG((Boolean) prefMap.get("activateIBG"));
        }
        if (prefMap.containsKey("activateIBGCollection")) {
            preferences.setActivateIBGCollection((Boolean) prefMap.get("activateIBGCollection"));
        }
        if (prefMap.containsKey("uobendtoendid") && !StringUtil.isNullOrEmpty((String) prefMap.get("uobendtoendid"))) {
            preferences.setEndToEndId((String) prefMap.get("uobendtoendid"));
        }
        if (prefMap.containsKey("uobpurposecode") && !StringUtil.isNullOrEmpty((String) prefMap.get("uobpurposecode"))) {
            preferences.setPurposeCode((String) prefMap.get("uobpurposecode"));
        }
        if (prefMap.containsKey("activateSalesContrcatManagement")) {
            preferences.setActivateSalesContrcatManagement((Boolean) prefMap.get("activateSalesContrcatManagement"));
        }
        if (prefMap.containsKey("activateLoanManagementFlag")) {
            preferences.setActivateLoanManagement((Boolean) prefMap.get("activateLoanManagementFlag"));
        }
        if (prefMap.containsKey("ProductSelectionType")) {
            preferences.setProductOptimizedFlag(prefMap.get("ProductSelectionType") != null ? Integer.parseInt((String)prefMap.get("ProductSelectionType")) : 1);
        }
        if (prefMap.containsKey("defaultmailsenderFlag")) {
            preferences.setDefaultmailsenderFlag(prefMap.get("defaultmailsenderFlag") != null ? Integer.parseInt((String)prefMap.get("defaultmailsenderFlag")) : 0);
        }
        if (prefMap.containsKey("proddiscripritchtextboxflag")) {
            if (prefMap.get("proddiscripritchtextboxflag")!=null && (prefMap.get("proddiscripritchtextboxflag").equals(1) || prefMap.get("proddiscripritchtextboxflag").equals(0))) {
                preferences.setProddiscripritchtextboxflag(prefMap.get("proddiscripritchtextboxflag") != null ? (Integer) prefMap.get("proddiscripritchtextboxflag") : 0);
            } else {
                preferences.setProddiscripritchtextboxflag(prefMap.get("proddiscripritchtextboxflag") != null ? Integer.parseInt((String) prefMap.get("proddiscripritchtextboxflag")) : 0);
            }
        }
        if (prefMap.containsKey("custvenloadtype")) {
            if (prefMap.get("custvenloadtype")!=null && (prefMap.get("custvenloadtype").equals(1) || prefMap.get("custvenloadtype").equals(0))) {
                preferences.setCustvenloadtype(prefMap.get("custvenloadtype") != null ? (Integer) prefMap.get("custvenloadtype") : 0);
            } else {
                preferences.setCustvenloadtype(prefMap.get("custvenloadtype") != null ? Integer.parseInt((String) prefMap.get("custvenloadtype")) : 0);
            }
        }
        if (prefMap.containsKey("downloadglprocessflag")) {
            preferences.setDownloadglprocessflag(prefMap.get("downloadglprocessflag") != null ? Integer.parseInt((String)prefMap.get("downloadglprocessflag")) : 0);
        }        
        if (prefMap.containsKey("downloadDimPLprocessflag")) {
            preferences.setDownloadDimPLprocessflag(prefMap.get("downloadDimPLprocessflag") != null ? Integer.parseInt((String) prefMap.get("downloadDimPLprocessflag")) : 0);
        }
        if (prefMap.containsKey("downloadSOAprocessflag")) {
            preferences.setDownloadSOAprocessflag(prefMap.get("downloadSOAprocessflag") != null ? Integer.parseInt((String) prefMap.get("downloadSOAprocessflag")) : 0);
        }
        if (prefMap.containsKey("isMovementWarehouseMapping")) {
            preferences.setMovementWarehouseMapping(prefMap.get("isMovementWarehouseMapping") != null ? (Boolean) prefMap.get("isMovementWarehouseMapping") : false);
        }
        if (prefMap.containsKey("InvoiceTermsSetting")) {
            preferences.setTermsincludegst(prefMap.get("InvoiceTermsSetting") != null ? Boolean.parseBoolean((String)prefMap.get("InvoiceTermsSetting")) : false);
        }
        
        if (prefMap.containsKey("activateToDateforExchangeRates") && prefMap.get("activateToDateforExchangeRates") != null) {
            preferences.setActivateToDateforExchangeRates(prefMap.get("activateToDateforExchangeRates") != null ? (Boolean) prefMap.get("activateToDateforExchangeRates") : false);
        }
        
        if (prefMap.containsKey("activateToBlockSpotRate") && prefMap.get("activateToBlockSpotRate") != null) {
            preferences.setActivateToBlockSpotRate(prefMap.get("activateToBlockSpotRate") != null ? (Boolean) prefMap.get("activateToBlockSpotRate") : false);
        }
        
        if (prefMap.containsKey("deliveryPlanner") && prefMap.get("deliveryPlanner") != null) {
            preferences.setDeliveryPlanner((Boolean) prefMap.get("deliveryPlanner"));
        }
        if (prefMap.containsKey("autoPopulateFieldsForDeliveryPlanner") && prefMap.get("autoPopulateFieldsForDeliveryPlanner") != null) {
            preferences.setAutoPopulateFieldsForDeliveryPlanner((Boolean) prefMap.get("autoPopulateFieldsForDeliveryPlanner"));
        }
        if (prefMap.containsKey("priceConfigurationAlert")) {
            preferences.setPriceConfigurationAlert((Boolean) prefMap.get("priceConfigurationAlert"));
        }
        if (prefMap.containsKey("retainExchangeRate")) {
            preferences.setRetainExchangeRate(prefMap.get("retainExchangeRate") != null ? (Boolean) prefMap.get("retainExchangeRate") : false);
        }
        if (prefMap.containsKey("isAutoRefershReportonSave")) {
            preferences.setIsAutoRefershReportOnSave(prefMap.get("isAutoRefershReportonSave") != null ? (Boolean) prefMap.get("isAutoRefershReportonSave") : false);
        }
        if (prefMap.containsKey("generateBarcodeParm")) {
            preferences.setGenerateBarcodeParm((Boolean) prefMap.get("generateBarcodeParm"));
        }
        if (prefMap.containsKey("barcodetype")) {
              preferences.setBarcodetype((String) prefMap.get("barcodetype"));
        }
        if (prefMap.containsKey("SKUFieldParm")) {
            preferences.setSKUFieldParm((Boolean) prefMap.get("SKUFieldParm"));
        }
        if (prefMap.containsKey("SKUFieldRename")) {
            preferences.setSKUFieldRename((String)prefMap.get("SKUFieldRename"));
        }
        if (prefMap.containsKey("productPricingOnBands")) {
            preferences.setProductPricingOnBands((Boolean) prefMap.get("productPricingOnBands"));
        }
        if (prefMap.containsKey("productPricingOnBandsForSales")) {
            preferences.setProductPricingOnBandsForSales((Boolean) prefMap.get("productPricingOnBandsForSales"));
        }
        if (prefMap.containsKey("barcodeDPI")) {
            preferences.setBarcodeDPI((String)prefMap.get("barcodeDPI"));
        }
        if (prefMap.containsKey("barcodeHeight")) {
            preferences.setBarcodeHeight((String) prefMap.get("barcodeHeight"));
        }
        if (prefMap.containsKey("pricePrintType")) {
            preferences.setPricePrinttype((String) prefMap.get("pricePrintType"));
        }
        if (prefMap.containsKey("barcdTopMargin")) {
            preferences.setBarcdTopMargin((String) prefMap.get("barcdTopMargin"));
        }
        if (prefMap.containsKey("barcdLeftMargin")) {
            preferences.setBarcdLeftMargin((String) prefMap.get("barcdLeftMargin"));
        }
        if (prefMap.containsKey("barcdLabelHeight")) {
            preferences.setBarcdLabelHeight((String) prefMap.get("barcdLabelHeight"));
        }
        if (prefMap.containsKey("priceTranslateX")) {
            preferences.setPriceTranslateX((String) prefMap.get("priceTranslateX"));
        }
        if (prefMap.containsKey("priceTranslateY")) {
            preferences.setPriceTranslateY((String) prefMap.get("priceTranslateY"));
        }
        if (prefMap.containsKey("generateBarcodeWithPriceParm")) {
            preferences.setGenerateBarcodeWithPriceParm((Boolean) prefMap.get("generateBarcodeWithPriceParm"));
        }
        if (prefMap.containsKey("priceFontSize")) {
            preferences.setPriceFontSize((String) prefMap.get("priceFontSize"));
        }
        if (prefMap.containsKey("pricePrefix")) {
            preferences.setPricePrefix((String) prefMap.get("pricePrefix"));
        }        
        if (prefMap.containsKey("pnamePrintType")) {
            preferences.setPnamePrintType((String) prefMap.get("pnamePrintType"));
        }
        if (prefMap.containsKey("pnameTranslateX")) {
            preferences.setPnameTranslateX((String) prefMap.get("pnameTranslateX"));
        }
        if (prefMap.containsKey("pnameTranslateY")) {
            preferences.setPnameTranslateY((String) prefMap.get("pnameTranslateY"));
        }
        if (prefMap.containsKey("generateBarcodeWithPnameParm")) {
            preferences.setGenerateBarcodeWithPnameParm((Boolean) prefMap.get("generateBarcodeWithPnameParm"));
        }
        if (prefMap.containsKey("pnameFontSize")) {
            preferences.setPnameFontSize((String) prefMap.get("pnameFontSize"));
        }
        if (prefMap.containsKey("pnamePrefix")) {
            preferences.setPnamePrefix((String) prefMap.get("pnamePrefix"));
        }  
        
        if (prefMap.containsKey("mrpPrintType")) {
            preferences.setMrpPrintType((String) prefMap.get("mrpPrintType"));
        }
        if (prefMap.containsKey("mrpTranslateX")) {
            preferences.setMrpTranslateX((String) prefMap.get("mrpTranslateX"));
        }
        if (prefMap.containsKey("mrpTranslateY")) {
            preferences.setMrpTranslateY((String) prefMap.get("mrpTranslateY"));
        }
        if (prefMap.containsKey("generateBarcodeWithMrpParm")) {
            preferences.setGenerateBarcodeWithMrpParm((Boolean) prefMap.get("generateBarcodeWithMrpParm"));
        }
        if (prefMap.containsKey("mrpFontSize")) {
            preferences.setMrpFontSize((String) prefMap.get("mrpFontSize"));
        }
        if (prefMap.containsKey("mrpPrefix")) {
            preferences.setMrpPrefix((String) prefMap.get("mrpPrefix"));
        }
        
        if (prefMap.containsKey("pidPrintType")) {
            preferences.setPidPrintType((String) prefMap.get("pidPrintType"));
        }
        if (prefMap.containsKey("pidTranslateX")) {
            preferences.setPidTranslateX((String) prefMap.get("pidTranslateX"));
        }
        if (prefMap.containsKey("pidTranslateY")) {
            preferences.setPidTranslateY((String) prefMap.get("pidTranslateY"));
        }
        if (prefMap.containsKey("generateBarcodeWithPidParm")) {
            preferences.setGenerateBarcodeWithPidParm((Boolean) prefMap.get("generateBarcodeWithPidParm"));
        }
        if (prefMap.containsKey("pidFontSize")) {
            preferences.setPidFontSize((String) prefMap.get("pidFontSize"));
        }
        if (prefMap.containsKey("pidPrefix")) {
            preferences.setPidPrefix((String) prefMap.get("pidPrefix"));
        }
        if (prefMap.containsKey("activateInventoryTab")) {
            preferences.setActivateInventoryTab(prefMap.get("activateInventoryTab") != null ? (Boolean) prefMap.get("activateInventoryTab") : true);
        }
        if (prefMap.containsKey("activateCycleCount")) {
            preferences.setActivateCycleCount(prefMap.get("activateCycleCount") != null ? (Boolean) prefMap.get("activateCycleCount") : true);
        }
        if (prefMap.containsKey("activateQAApprovalFlow")) {
            preferences.setActivateQAApprovalFlow(prefMap.get("activateQAApprovalFlow") != null ? (Boolean) prefMap.get("activateQAApprovalFlow") : true);
        }
        if(prefMap.containsKey("SalesSelectionType")){
             if (prefMap.get("SalesSelectionType").equals(true) || prefMap.get("SalesSelectionType").equals(false)) {
                preferences.setSalesTypeFlag(prefMap.get("SalesSelectionType") != null ? (Boolean) prefMap.get("SalesSelectionType") : false);
            }else{
               preferences.setSalesTypeFlag(prefMap.get("SalesSelectionType") != null ? Boolean.parseBoolean((String)prefMap.get("SalesSelectionType")) : false);
             }
        }
        if(prefMap.containsKey("PurchaseSelectionType")){
             if (prefMap.get("PurchaseSelectionType").equals(true) || prefMap.get("PurchaseSelectionType").equals(false)) {
                preferences.setPurchaseTypeFlag(prefMap.get("PurchaseSelectionType") != null ? (Boolean) prefMap.get("PurchaseSelectionType") : false);
            }else{
               preferences.setPurchaseTypeFlag(prefMap.get("PurchaseSelectionType") != null ? Boolean.parseBoolean((String)prefMap.get("PurchaseSelectionType")) : false);  
             }
        }
        if (prefMap.containsKey("activateProfitMargin") && prefMap.get("activateProfitMargin") != null) {
            if (prefMap.get("activateProfitMargin").equals(true) || prefMap.get("activateProfitMargin").equals(false)) {
                preferences.setActivateProfitMargin(prefMap.get("activateProfitMargin") != null ? (Boolean) prefMap.get("activateProfitMargin") : false);
            } else {
                preferences.setActivateProfitMargin(prefMap.get("activateProfitMargin") != null ? Boolean.parseBoolean((String) prefMap.get("activateProfitMargin")) : false);
            }
        }
        if (prefMap.containsKey("activateimportForJE") && prefMap.get("activateimportForJE") != null) {
            if (prefMap.get("activateimportForJE").equals(true) || prefMap.get("activateimportForJE").equals(false)) {
                preferences.setActivateimportForJE(prefMap.get("activateimportForJE") != null ? (Boolean) prefMap.get("activateimportForJE") : false);
            } else {
                preferences.setActivateimportForJE(prefMap.get("activateimportForJE") != null ? Boolean.parseBoolean((String) prefMap.get("activateimportForJE")) : false);
            }
        }
        if (prefMap.containsKey("activateCRblockingWithoutStock") && prefMap.get("activateCRblockingWithoutStock") != null) {
            if (prefMap.get("activateCRblockingWithoutStock").equals(true) || prefMap.get("activateCRblockingWithoutStock").equals(false)) {
                preferences.setActivateCRblockingWithoutStock(prefMap.get("activateCRblockingWithoutStock") != null ? (Boolean) prefMap.get("activateCRblockingWithoutStock") : false);
            } else {
                preferences.setActivateCRblockingWithoutStock(prefMap.get("activateCRblockingWithoutStock") != null ? Boolean.parseBoolean((String) prefMap.get("activateCRblockingWithoutStock")) : false);
            }
        }
        if (prefMap.containsKey("activatefromdateToDate") && prefMap.get("activatefromdateToDate") != null) {
            if (prefMap.get("activatefromdateToDate").equals(true) || prefMap.get("activatefromdateToDate").equals(false)) {
                preferences.setActivateCRblockingWithoutStock(prefMap.get("activatefromdateToDate") != null ? (Boolean) prefMap.get("activatefromdateToDate") : false);
            } else {
                preferences.setActivatefromdateToDate(prefMap.get("activatefromdateToDate") != null ? Boolean.parseBoolean((String) prefMap.get("activatefromdateToDate")) : false);
            }
        }

        if (prefMap.containsKey("isDuplicateItems")) {
            preferences.setDuplicateItems(prefMap.get("isDuplicateItems") != null ? (Boolean) prefMap.get("isDuplicateItems") : false);
        }
        if (prefMap.containsKey("hierarchicalDimensions") && prefMap.get("hierarchicalDimensions") != null) {
            preferences.setHierarchicalDimensions(prefMap.get("hierarchicalDimensions") != null ? (Boolean) prefMap.get("hierarchicalDimensions") : false);
        }
        if (prefMap.containsKey("inspectionStore") && prefMap.get("inspectionStore") != null) {
            preferences.setInspectionStore((String) prefMap.get("inspectionStore"));
        }
        if (prefMap.containsKey("repairStore") && prefMap.get("repairStore") != null) {
            preferences.setRepairStore((String) prefMap.get("repairStore"));
        }
        if (prefMap.containsKey("pickpackship")) {
            preferences.setPickpackship(prefMap.get("pickpackship") != null ? (Boolean) prefMap.get("pickpackship") : true);
        }
        if (prefMap.containsKey("showPivotInCustomReports")) {
            preferences.setShowPivotInCustomReports(prefMap.get("showPivotInCustomReports") != null ? (Boolean) prefMap.get("showPivotInCustomReports") : false);
        }
        if (prefMap.containsKey("interloconpick")) {
            preferences.setInterloconpick(prefMap.get("interloconpick") != null ? (Boolean) prefMap.get("interloconpick") : true);
        }
        if (prefMap.containsKey("packingstore") && prefMap.get("packingstore") != null) {
            preferences.setPackingstore((String) prefMap.get("packingstore"));
        }
        if (prefMap.containsKey("vendorjoborderstore") && prefMap.get("vendorjoborderstore") != null) {
            preferences.setVendorjoborderstore((String) prefMap.get("vendorjoborderstore"));
        }
        if (prefMap.containsKey("packinglocation") && prefMap.get("packinglocation") != null) {
            preferences.setPackinglocation((String) prefMap.get("packinglocation"));
        }
        if (prefMap.containsKey("sendimportmailto") && prefMap.get("sendimportmailto") != null) {
            preferences.setSendImportMailTo((String) prefMap.get("sendimportmailto"));
        }
        if (prefMap.containsKey("useremails") && prefMap.get("useremails") != null) {
            preferences.setUserEmails((String) prefMap.get("useremails"));
        }
        if (prefMap.containsKey("defaultWarehouse") && prefMap.get("defaultWarehouse") != null) {
            preferences.setDefaultWarehouse(prefMap.get("defaultWarehouse") != null ? (String) prefMap.get("defaultWarehouse") : "");
        }
        if (prefMap.containsKey("liabilityAccountForLMS") && prefMap.get("liabilityAccountForLMS") != null) {
            preferences.setLiabilityAccountForLMS(prefMap.get("liabilityAccountForLMS") != null ? (String) prefMap.get("liabilityAccountForLMS") : "");
        }
        if (prefMap.containsKey("enablevatcst")) {
            preferences.setEnableVatCst(prefMap.get("enablevatcst") != null ? (Boolean) prefMap.get("enablevatcst") : true);
        }
        if (prefMap.containsKey("isNewGSTOnly")) {
            preferences.setIsNewGST(prefMap.get("isNewGSTOnly") != null ? (Boolean) prefMap.get("isNewGSTOnly") : false);
        }
        if (prefMap.containsKey("assessmentcircle")) {
            preferences.setAssessmentCircle(prefMap.get("assessmentcircle") != null ? (String) prefMap.get("assessmentcircle") : "");
        }
        if (prefMap.containsKey("division")) {
            preferences.setDivision(prefMap.get("division") != null ? (String) prefMap.get("division") : "");
        }
        if (prefMap.containsKey("areacode")) {
            preferences.setAreaCode(prefMap.get("areacode") != null ? (String) prefMap.get("areacode") : "");
        }
        if (prefMap.containsKey("importexportcode")) {
            preferences.setImportExportCode(prefMap.get("importexportcode") != null ? (String) prefMap.get("importexportcode") : "");
        }
        if (prefMap.containsKey("authorizedby")) {
            preferences.setAuthorizedBy(prefMap.get("authorizedby") != null ? (String) prefMap.get("authorizedby") : "");
        }
        if (prefMap.containsKey("authorizedperson")) {
            preferences.setAuthorizedPerson(prefMap.get("authorizedperson") != null ? (String) prefMap.get("authorizedperson") : "");
        }
        if (prefMap.containsKey("statusordesignation")) {
            preferences.setStatusorDesignation(prefMap.get("statusordesignation") != null ? (String) prefMap.get("statusordesignation") : "");
        }
        if (prefMap.containsKey("place")) {
            preferences.setPlace(prefMap.get("place") != null ? (String) prefMap.get("place") : "");
        }
        if (prefMap.containsKey("vattincomposition")) {
            preferences.setVatTinComposition(prefMap.get("vattincomposition") != null ? (String) prefMap.get("vattincomposition") : "");
        }
        if (prefMap.containsKey("vattinregular")) {
            preferences.setVatTinRegular(prefMap.get("vattinregular") != null ? (String) prefMap.get("vattinregular") : "");
        }
        if (prefMap.containsKey("localsalestaxnumber")) {
            preferences.setLocalSalesTaxNumber(prefMap.get("localsalestaxnumber") != null ? (String) prefMap.get("localsalestaxnumber") : "");
        }
        if (prefMap.containsKey("interstatesalestaxnumber")) {
            preferences.setInterStateSalesTaxNumber(prefMap.get("interstatesalestaxnumber") != null ? (String) prefMap.get("interstatesalestaxnumber") : "");
        }
        if (prefMap.containsKey("typeofdealer")) {
            preferences.setTypeOfDealer(prefMap.get("typeofdealer") != null ? (String) prefMap.get("typeofdealer") : "");
        }
        if (prefMap.containsKey("bankid")) {
            preferences.setBankId(prefMap.get("bankid") != null ? (String) prefMap.get("bankid") : "");
        }
        if (prefMap.containsKey("applicabilityofvat") && prefMap.get("applicabilityofvat") != null) {
            preferences.setApplicabilityOfVat((Date) prefMap.get("applicabilityofvat"));
        }
        if (prefMap.containsKey("showVendorUpdate")) {
            preferences.setShowVendorUpdateFlag(prefMap.get("showVendorUpdate") != null ? (Boolean) prefMap.get("showVendorUpdate") : true);
        }
        if (prefMap.containsKey("showCustomerUpdate")) {
            preferences.setShowCustomerUpdateFlag(prefMap.get("showCustomerUpdate") != null ? (Boolean) prefMap.get("showCustomerUpdate") : true);
        }
        if (prefMap.containsKey("showProductUpdate")) {
            preferences.setShowProductUpdateFlag(prefMap.get("showProductUpdate") != null ? (Boolean) prefMap.get("showProductUpdate") : false);
        }
        if (prefMap.containsKey("isBaseUOMRateEdit")) {
            preferences.setBaseUOMRateEdit(prefMap.get("isBaseUOMRateEdit") != null ? (Boolean) prefMap.get("isBaseUOMRateEdit") : false);
        }
        if (prefMap.containsKey("allowZeroUntiPriceForProduct")) {
            preferences.setAllowZeroUntiPriceForProduct(prefMap.get("allowZeroUntiPriceForProduct") != null ? (Boolean) prefMap.get("allowZeroUntiPriceForProduct") : false);
        }
        if (prefMap.containsKey("allowZeroQuantityForProduct")) {
            preferences.setAllowZeroQuantityForProduct(prefMap.get("allowZeroQuantityForProduct") != null ? (Boolean) prefMap.get("allowZeroQuantityForProduct") : false);
        }
        if (prefMap.containsKey("allowZeroQuantityInQuotation")) {
            preferences.setAllowZeroQuantityInQuotation(prefMap.get("allowZeroQuantityInQuotation") != null ? (Boolean) prefMap.get("allowZeroQuantityInQuotation") : false);
        }
        if (prefMap.containsKey("negativestockformulaso")) {
            str = (String) prefMap.get("negativestockformulaso");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setNegativestockformulaso(Integer.parseInt(str));
            } else {
                preferences.setNegativestockformulaso(0);
            }
        }
        if (prefMap.containsKey("negativestockformulasi")) {
            str = (String) prefMap.get("negativestockformulasi");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setNegativestockformulasi(Integer.parseInt(str));
            } else {
                preferences.setNegativestockformulasi(0);
            }
        }
        if (prefMap.containsKey("enablesalespersonAgentFlow")) {
            preferences.setEnablesalespersonAgentFlow(prefMap.get("enablesalespersonAgentFlow") != null ? (Boolean) prefMap.get("enablesalespersonAgentFlow") : false);
        }
        if (prefMap.containsKey("viewallexcludecustomerwithoutsalesperson")) {
            preferences.setViewAllExcludeCustomer(prefMap.get("viewallexcludecustomerwithoutsalesperson") != null ? (Boolean) prefMap.get("viewallexcludecustomerwithoutsalesperson") : false);
        }
        if (prefMap.containsKey("BuildAssemblyApprovalFlow")) {
            preferences.setBuildAssemblyApprovalFlow(prefMap.get("BuildAssemblyApprovalFlow") != null ? (Boolean) prefMap.get("BuildAssemblyApprovalFlow") : false);
        }
        if (prefMap.containsKey("isPRmandatory")) {
            preferences.setIsPRmandatory(prefMap.get("isPRmandatory") != null ? (Boolean) prefMap.get("isPRmandatory") : false);
        }
        if (prefMap.containsKey("defaultsequenceformatforrecinv")) {
            preferences.setDefaultsequenceformatforrecinv(prefMap.get("defaultsequenceformatforrecinv") != null ? (Boolean) prefMap.get("defaultsequenceformatforrecinv") : false);
        }
        if (prefMap.containsKey("pickaddressfrommaster")) {
            preferences.setPickAddressFromMaster(prefMap.get("pickaddressfrommaster") != null ? (Boolean) prefMap.get("pickaddressfrommaster") : false);
        }
         if (prefMap.containsKey("remitpaymentto")) {
            if (prefMap.get("remitpaymentto") != null) {
                preferences.setRemitpaymentto((String) prefMap.get("remitpaymentto"));
            }
        }
         if (prefMap.containsKey("isaddressfromvendormaster")) {
            preferences.setIsAddressFromVendorMaster(prefMap.get("isaddressfromvendormaster") != null ? (Boolean) prefMap.get("isaddressfromvendormaster") : false);
        }
         
         
        if (prefMap.containsKey("autoPopulateDeliveredQuantity")) {
             preferences.setAutoPopulateDeliveredQuantity(prefMap.get("autoPopulateDeliveredQuantity") != null ? (Boolean) prefMap.get("autoPopulateDeliveredQuantity") : false);
        }
        if (prefMap.containsKey("defaultTemplateLogoFlag")) {
             preferences.setDefaultTemplateLogoFlag(prefMap.get("defaultTemplateLogoFlag") != null ? (Boolean) prefMap.get("defaultTemplateLogoFlag") : false);
        }
        if (prefMap.containsKey("enableLinkToSelWin")) {
            preferences.setEnableLinkToSelWin((Boolean) prefMap.get("enableLinkToSelWin"));
        }
        if (prefMap.containsKey("showBulkInvoices")) {
            preferences.setShowBulkInvoices((Boolean) prefMap.get("showBulkInvoices"));
        }
        if (prefMap.containsKey("showBulkInvoicesFromSO")) {
            preferences.setShowBulkInvoicesFromSO((Boolean) prefMap.get("showBulkInvoicesFromSO"));
        }
        if (prefMap.containsKey("showBulkDOFromSO")) {
            preferences.setShowBulkDOFromSO((Boolean) prefMap.get("showBulkDOFromSO"));
        }
        if (prefMap.containsKey("invoicesWriteOffAccount")) {
            preferences.setWriteOffAccount((String) prefMap.get("invoicesWriteOffAccount"));
            Account account = (Account) get(Account.class, (String) prefMap.get("invoicesWriteOffAccount"));
            if (account != null) {
                String usedin = account.getUsedIn();
                account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.SALES_INVOICE_WRITEOFF_ACCOUNT));
            }
        }
        if (prefMap.containsKey("splitOpeningBalanceAmount")) {
            preferences.setSplitOpeningBalanceAmount((Boolean)prefMap.get("splitOpeningBalanceAmount"));
        }
        if (prefMap.containsKey("salesCommissionReportMode") && prefMap.get("salesCommissionReportMode") != null) {
            preferences.setSalesCommissionReportMode(Integer.parseInt((String) prefMap.get("salesCommissionReportMode")));
        }
        if (prefMap.containsKey("salesorderreopen")&& prefMap.get("salesorderreopen") != null) {
            preferences.setSalesorderreopen((Boolean) prefMap.get("salesorderreopen"));
        }
        if (prefMap.containsKey("isActiveLandingCostOfItem")&& prefMap.get("isActiveLandingCostOfItem") != null) {
            preferences.setActivelandingcostofitem((Boolean) prefMap.get("isActiveLandingCostOfItem"));
        }
        if (prefMap.containsKey("includeAmountInLimitSI") && prefMap.get("includeAmountInLimitSI") != null) {
            preferences.setIncludeAmountInLimitSI((Boolean) prefMap.get("includeAmountInLimitSI"));
        }
        if (prefMap.containsKey("includeAmountInLimitPI") && prefMap.get("includeAmountInLimitPI") != null) {
            preferences.setIncludeAmountInLimitPI((Boolean) prefMap.get("includeAmountInLimitPI"));
        }
        if (prefMap.containsKey("includeAmountInLimitSO") && prefMap.get("includeAmountInLimitSO") != null) {
            preferences.setIncludeAmountInLimitSO((Boolean) prefMap.get("includeAmountInLimitSO"));
        }
        if (prefMap.containsKey("includeAmountInLimitPO") && prefMap.get("includeAmountInLimitPO") != null) {
            preferences.setIncludeAmountInLimitPO((Boolean) prefMap.get("includeAmountInLimitPO"));
        }
        if (prefMap.containsKey("receiptWriteOffAccount")) {
            String account = (String) prefMap.get("receiptWriteOffAccount");
            if(!StringUtil.isNullOrEmpty(account)){
                Account acc = (Account)get(Account.class, account);
                preferences.setWriteOffReceiptAccount(acc.getID());
                String usedin = acc.getUsedIn();
                acc.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Receipts_Write_Off_Account));
            }
            
        }
        if (prefMap.containsKey("vendorCreditControl") && !StringUtil.isNullOrEmpty(prefMap.get("vendorCreditControl").toString())) {
            int vendorDebitControl = Integer.parseInt(prefMap.get("vendorCreditControl").toString());
            preferences.setVendorCreditControlType(vendorDebitControl);
        }
        if (prefMap.containsKey("propagatetochildcompanies")) {
            preferences.setPropagateToChildCompanies(prefMap.get("propagatetochildcompanies") != null ? (Boolean) prefMap.get("propagatetochildcompanies") : false);
        }
        if (prefMap.containsKey("activateDDTemplateFlow")) {
            preferences.setActivateDDTemplateFlow(prefMap.get("activateDDTemplateFlow") != null ? (Boolean) prefMap.get("activateDDTemplateFlow") : false);
        }
        if (prefMap.containsKey("activateDDInsertTemplateLink")) {
            preferences.setActivateDDInsertTemplateLink(prefMap.get("activateDDInsertTemplateLink") != null ? (Boolean) prefMap.get("activateDDInsertTemplateLink") : false);
        }
        if (prefMap.containsKey("wastageDefaultAccount")) {
            preferences.setWastageDefaultAccount((String) prefMap.get("wastageDefaultAccount"));
            Account account = (Account) get(Account.class, (String) prefMap.get("wastageDefaultAccount"));
            if (account != null) {
                String usedin = account.getUsedIn();
                account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Wastage_Default_Account));
            }
        }
        
         if (prefMap.containsKey("adjustmentAccountPayment")) {
           preferences.setAdjustmentAccountPayment((String) prefMap.get("adjustmentAccountPayment"));
            Account account = (Account) get(Account.class, (String) prefMap.get("adjustmentAccountPayment"));
            if (account != null) {
                String usedin = account.getUsedIn();
                account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Adjustment_Account_Payment));
            }
        }
        if (prefMap.containsKey("adjustmentAccountReceipt")) {
             preferences.setAdjustmentAccountReceipt((String) prefMap.get("adjustmentAccountReceipt"));
            Account account = (Account) get(Account.class, (String) prefMap.get("adjustmentAccountReceipt"));
            if (account != null) {
                String usedin = account.getUsedIn();
                account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Adjustment_Account_Receipt));
            }
        }
        if (prefMap.containsKey("activateWastageCalculation")) {
            preferences.setActivateWastageCalculation(prefMap.get("activateWastageCalculation") != null ? (Boolean) prefMap.get("activateWastageCalculation") : false);
        }
        if (prefMap.containsKey("calculateproductweightmeasurment")) {
            preferences.setCalculateProductWeightMeasurment(prefMap.get("calculateproductweightmeasurment") != null ? (Boolean) prefMap.get("calculateproductweightmeasurment") : false);
        }
        if (prefMap.containsKey("carryForwardPriceForCrossLinking")) {
            preferences.setCarryForwardPriceForCrossLinking(prefMap.get("carryForwardPriceForCrossLinking") != null ? (Boolean) prefMap.get("carryForwardPriceForCrossLinking") : false);
        }
        if (prefMap.containsKey("showzeroamountasblank")) {
            preferences.setShowZeroAmountAsBlank((Boolean) prefMap.get("showzeroamountasblank"));
        }
        if (prefMap.containsKey("vatNumber")) {
            preferences.setVatNumber(prefMap.get("vatNumber") != null ? (String) prefMap.get("vatNumber") : "");
        }
        if (prefMap.containsKey("registrationType")) {
            preferences.setRegistrationType(prefMap.get("registrationType") != null ? (String) prefMap.get("registrationType") : "");
        }
        if (prefMap.containsKey("cstNumber")) {
            preferences.setCstNumber(prefMap.get("cstNumber") != null ? (String) prefMap.get("cstNumber") : "");
        }
        if (prefMap.containsKey("panNumber")) {
            preferences.setPanNumber(prefMap.get("panNumber") != null ? (String) prefMap.get("panNumber") : "");
        }
        if (prefMap.containsKey("serviceTaxRegNumber")) {
            preferences.setServiceTaxRegNo(prefMap.get("serviceTaxRegNumber") != null ? (String) prefMap.get("serviceTaxRegNumber") : "");
        }
        if (prefMap.containsKey("tanNumber")) {
            preferences.setTanNumber(prefMap.get("tanNumber") != null ? (String) prefMap.get("tanNumber") : "");
        }
        if (prefMap.containsKey("eccNumber")) {
            preferences.setEccNumber(prefMap.get("eccNumber") != null ? (String) prefMap.get("eccNumber") : "");
        }
        if (prefMap.containsKey("AllowToMapAccounts") && prefMap.get("AllowToMapAccounts") != "") {
            preferences.setAllowToMapAccounts((Boolean) prefMap.get("AllowToMapAccounts"));
        }
        if (prefMap.containsKey("gstdeactivationdate") && prefMap.get("gstdeactivationdate") != null) {
            preferences.setGstDeactivationDate((Date) prefMap.get("gstdeactivationdate"));
        }
        if (prefMap.containsKey("isCurrencyCode")) {
            preferences.setCurrencyCode(Boolean.parseBoolean(prefMap.get("isCurrencyCode").toString()));
        }
        if (prefMap.containsKey("bandsWithSpecialRateForSales")) {
            preferences.setBandsWithSpecialRateForSales(prefMap.get("bandsWithSpecialRateForSales") != null ? (Boolean) prefMap.get("bandsWithSpecialRateForSales") : false);
        }
        if(prefMap.containsKey("badDebtProcessingPeriod")&& !StringUtil.isNullOrEmpty(prefMap.get("badDebtProcessingPeriod").toString())){
            preferences.setBadDebtProcessingPeriod(Integer.parseInt(prefMap.get("badDebtProcessingPeriod").toString()));
        }
        if(prefMap.containsKey("badDebtProcessingPeriodType")&& !StringUtil.isNullOrEmpty(prefMap.get("badDebtProcessingPeriodType").toString())){
            preferences.setBadDebtProcessingPeriodType(Integer.parseInt(prefMap.get("badDebtProcessingPeriodType").toString()));
        }
        if(prefMap.containsKey("gstSubmissionPeriod")&& !StringUtil.isNullOrEmpty(prefMap.get("gstSubmissionPeriod").toString())){
            preferences.setGstSubmissionPeriod(Integer.parseInt(prefMap.get("gstSubmissionPeriod").toString()));
        }        
        if(prefMap.containsKey("isTDSapplicable")){
            preferences.setTDSapplicable((prefMap.get("isTDSapplicable") != null) ?Boolean.valueOf(prefMap.get("isTDSapplicable").toString()):false);
        }
        if(prefMap.containsKey("isSTapplicable")){
            preferences.setSTapplicable((prefMap.get("isSTapplicable") != null) ?Boolean.valueOf(prefMap.get("isSTapplicable").toString()):false);
        }
        if(prefMap.containsKey("commissioneratecode")){
            preferences.setCommissionerateCode((prefMap.get("commissioneratecode") != null) ?prefMap.get("commissioneratecode").toString():"");
        }
        if(prefMap.containsKey("commissioneratename")){
            preferences.setCommissionerateName((prefMap.get("commissioneratename") != null) ?prefMap.get("commissioneratename").toString():"");
        }
//        if(prefMap.containsKey("servicetaxregno")){
//            preferences.setServiceTaxRegNo((prefMap.get("servicetaxregno") != null) ?prefMap.get("servicetaxregno").toString():"");
//        }
        if(prefMap.containsKey("divisioncode")){
            preferences.setDivisionCode((prefMap.get("divisioncode") != null) ?prefMap.get("divisioncode").toString():"");
        }
        if(prefMap.containsKey("rangecode")){
            preferences.setRangeCode((prefMap.get("rangecode") != null) ?prefMap.get("rangecode").toString():"");
        }
        if(prefMap.containsKey("isExciseApplicable")){
            preferences.setExciseApplicable((prefMap.get("isExciseApplicable") != null) ?Boolean.valueOf(prefMap.get("isExciseApplicable").toString()):false);
        }
        if(prefMap.containsKey("registrationType")){
            preferences.setRegistrationType((prefMap.get("registrationType") != null) ?prefMap.get("registrationType").toString():"");
        }
        if(prefMap.containsKey("manufacturerType")){
            preferences.setManufacturerType((prefMap.get("manufacturerType") != null) ?prefMap.get("manufacturerType").toString():"");
        }
        if(prefMap.containsKey("unitname")){
            preferences.setUnitname((prefMap.get("unitname") != null) ?prefMap.get("unitname").toString():"");
        }
        if (prefMap.containsKey("exciseTariffdetails")) {
            preferences.setExciseTariffdetails((prefMap.get("exciseTariffdetails") != null) ? Boolean.valueOf(prefMap.get("exciseTariffdetails").toString()) : false);
        }
        if (prefMap.containsKey("excisejurisdictiondetails")) {
            preferences.setExciseJurisdictiondetails((prefMap.get("excisejurisdictiondetails") != null) ? Boolean.valueOf(prefMap.get("excisejurisdictiondetails").toString()) : false);
        }
        if (prefMap.containsKey("exciseMultipleUnit")) {
            preferences.setExciseMultipleUnit((prefMap.get("exciseMultipleUnit") != null) ? Boolean.valueOf(prefMap.get("exciseMultipleUnit").toString()) : false);
        }
        if(prefMap.containsKey("excisecommissioneratecode")){
            preferences.setExciseCommissionerateCode((prefMap.get("excisecommissioneratecode") != null) ?prefMap.get("excisecommissioneratecode").toString():"");
        }
        if(prefMap.containsKey("excisecommissioneratename")){
            preferences.setExciseCommissionerateName((prefMap.get("excisecommissioneratename") != null) ?prefMap.get("excisecommissioneratename").toString():"");
        }
        if(prefMap.containsKey("excisedivisioncode")){
            preferences.setExciseDivisionCode((prefMap.get("excisedivisioncode") != null) ?prefMap.get("excisedivisioncode").toString():"");
        }
        if(prefMap.containsKey("exciserangecode")){
            preferences.setExciseRangeCode((prefMap.get("exciserangecode") != null) ?prefMap.get("exciserangecode").toString():"");
        }
        if(prefMap.containsKey("deductortype")){
            preferences.setDeductorType((prefMap.get("deductortype") != null) ?Integer.parseInt(prefMap.get("deductortype").toString()) :0);
        }
        if(prefMap.containsKey("headofficetanno")){
            preferences.setHeadOfficeTANno((prefMap.get("headofficetanno") != null) ?prefMap.get("headofficetanno").toString():"");
        }
        if(prefMap.containsKey("tdsincometaxcircle")){
            preferences.setTdsIncomeTaxCircle((prefMap.get("tdsincometaxcircle") != null) ? prefMap.get("tdsincometaxcircle").toString():"");
        }
        if(prefMap.containsKey("tdsrespperson")){
            preferences.setTdsRespPerson((prefMap.get("tdsrespperson") != null) ?prefMap.get("tdsrespperson").toString():"");
        }
        if(prefMap.containsKey("tdsresppersonfathersname")){
            preferences.setTdsRespPersonFatherName((prefMap.get("tdsresppersonfathersname") != null) ?prefMap.get("tdsresppersonfathersname").toString():"");
        }
        if(prefMap.containsKey("tdsresppersondesignation")){
            preferences.setTdsRespPersonDesignation((prefMap.get("tdsresppersondesignation") != null) ?prefMap.get("tdsresppersondesignation").toString():"");
        }
        if (prefMap.containsKey("amountInIndianWord")) {
            preferences.setAmountInIndianWord((Boolean) prefMap.get("amountInIndianWord"));
        }        
        if (prefMap.containsKey("activatemrpmodule") && prefMap.get("activatemrpmodule") != null) {
            preferences.setActivateMRPModule((Boolean) prefMap.get("activatemrpmodule"));
        }
        if (prefMap.containsKey("dateofregistration") && prefMap.get("dateofregistration") != null) {
            preferences.setDateofregistration((Date) prefMap.get("dateofregistration"));
        }
        if (prefMap.containsKey("returncode") && prefMap.get("returncode") != null) {
            preferences.setReturncode((prefMap.get("returncode") != null) ?prefMap.get("returncode").toString():"");
        }
        if (prefMap.containsKey("cstregistrationdate") && prefMap.get("cstregistrationdate") != null) {
            preferences.setCstregistrationdate((Date) prefMap.get("cstregistrationdate"));
        }
        if (prefMap.containsKey("tariffName")) {
            preferences.setTariffName(prefMap.get("tariffName")!=null?prefMap.get("tariffName").toString():"");
        }
        if (prefMap.containsKey("HSNCode")) {
            preferences.setHSNCode(prefMap.get("HSNCode")!=null?prefMap.get("HSNCode").toString():"");
        }
        if (prefMap.containsKey("reportingUOM")) {
            preferences.setReportingUOM((prefMap.get("reportingUOM") != null) ?prefMap.get("reportingUOM").toString():"");
        }
        if (prefMap.containsKey("exciseMethod")) {
            preferences.setExciseMethod((prefMap.get("exciseMethod") != null) ?prefMap.get("exciseMethod").toString():"");
        }
        if (prefMap.containsKey("exciseRate")&& prefMap.get("exciseRate") != null) {
            preferences.setExciseRate((Double) prefMap.get("exciseRate"));
        }
        if (prefMap.containsKey("interStatePurAccCformID")&& prefMap.get("interStatePurAccCformID") != null) {
            preferences.setInterstatepuracccformid(prefMap.get("interStatePurAccCformID").toString());
        }
        if (prefMap.containsKey("interStatePurAccID")&& prefMap.get("interStatePurAccID") != null) {
            preferences.setInterstatepuraccid(prefMap.get("interStatePurAccID").toString());
        }
        if (prefMap.containsKey("interStatePurAccReturnCformID")&& prefMap.get("interStatePurAccReturnCformID") != null) {
            preferences.setInterstatepuraccreturncformid(prefMap.get("interStatePurAccReturnCformID").toString());
        }
        if (prefMap.containsKey("interStatePurReturnAccID")&& prefMap.get("interStatePurReturnAccID") != null) {
            preferences.setInterstatepurreturnaccid(prefMap.get("interStatePurReturnAccID").toString());
        }
        if (prefMap.containsKey("interStateSalesAccCformID")&& prefMap.get("interStateSalesAccCformID") != null) {
            preferences.setInterstatesalesacccformid(prefMap.get("interStateSalesAccCformID").toString());
        }
        if (prefMap.containsKey("interStateSalesAccID")&& prefMap.get("interStateSalesAccID") != null) {
            preferences.setInterstatesalesaccid(prefMap.get("interStateSalesAccID").toString());
        }
        if (prefMap.containsKey("interStateSalesAccReturnCformID")&& prefMap.get("interStateSalesAccReturnCformID") != null) {
            preferences.setInterstatesalesaccreturncformid(prefMap.get("interStateSalesAccReturnCformID").toString());
        }
        if (prefMap.containsKey("interStateSalesReturnAccID")&& prefMap.get("interStateSalesReturnAccID") != null) {
            preferences.setInterstatesalesreturnaccid(prefMap.get("interStateSalesReturnAccID").toString());
        }
        if (prefMap.containsKey("salesaccountidcompany")&& prefMap.get("salesaccountidcompany") != null) {
            preferences.setSalesaccountidcompany(prefMap.get("salesaccountidcompany").toString());
        }
        if (prefMap.containsKey("salesretaccountidcompany")&& prefMap.get("salesretaccountidcompany") != null) {
            preferences.setSalesretaccountidcompany(prefMap.get("salesretaccountidcompany").toString());
        }
        if (prefMap.containsKey("purchaseretaccountidcompany")&& prefMap.get("purchaseretaccountidcompany") != null) {
            preferences.setPurchaseretaccountidcompany(prefMap.get("purchaseretaccountidcompany").toString());
        }
        if (prefMap.containsKey("purchaseaccountidcompany")&& prefMap.get("purchaseaccountidcompany") != null) {
            preferences.setPurchaseaccountidcompany(prefMap.get("purchaseaccountidcompany").toString());
        }
        if (prefMap.containsKey("vatPayableAcc")&& prefMap.get("vatPayableAcc") != null) {
            preferences.setVatPayableAcc(prefMap.get("vatPayableAcc").toString());
        }
        if (prefMap.containsKey("vatInCreditAvailAcc")&& prefMap.get("vatInCreditAvailAcc") != null) {
            preferences.setVatInCreditAvailAcc(prefMap.get("vatInCreditAvailAcc").toString());
        }
        if (prefMap.containsKey("CSTPayableAcc")&& prefMap.get("CSTPayableAcc") != null) {
            preferences.setCSTPayableAcc(prefMap.get("CSTPayableAcc").toString());
        }
        if (prefMap.containsKey("excisePayableAcc")&& prefMap.get("excisePayableAcc") != null) {
            preferences.setExcisePayableAcc(prefMap.get("excisePayableAcc").toString());
        }
        if (prefMap.containsKey("exciseDutyAdvancePaymentaccount")&& prefMap.get("exciseDutyAdvancePaymentaccount") != null) {
            preferences.setExciseDutyAdvancePaymentaccount(prefMap.get("exciseDutyAdvancePaymentaccount").toString());
        }
        if (prefMap.containsKey("STPayableAcc")&& prefMap.get("STPayableAcc") != null) {
            preferences.setSTPayableAcc(prefMap.get("STPayableAcc").toString());
        }
        if (prefMap.containsKey("STAdvancePaymentaccount")&& prefMap.get("STAdvancePaymentaccount") != null) {
            preferences.setSTAdvancePaymentaccount(prefMap.get("STAdvancePaymentaccount").toString());
        }
        if (prefMap.containsKey("paymentMethod")&& prefMap.get("paymentMethod") != null) {
            preferences.setPaymentMethodId(prefMap.get("paymentMethod").toString());
        }
        if (prefMap.containsKey("unitPriceInDO") && prefMap.get("unitPriceInDO") != null) {
            preferences.setUnitPriceInDO((Boolean) prefMap.get("unitPriceInDO"));
        }
        if (prefMap.containsKey("unitPriceInGR") && prefMap.get("unitPriceInGR") != null) {
            preferences.setUnitPriceInGR((Boolean) prefMap.get("unitPriceInGR"));
        }
        if (prefMap.containsKey("unitPriceInSR") && prefMap.get("unitPriceInSR") != null) {
            preferences.setUnitPriceInSR((Boolean) prefMap.get("unitPriceInSR"));
        }
        if (prefMap.containsKey("unitPriceInPR") && prefMap.get("unitPriceInPR") != null) {
            preferences.setUnitPriceInPR((Boolean) prefMap.get("unitPriceInPR"));
        }
        if (prefMap.containsKey("openPOandSO") && prefMap.get("openPOandSO") != null) {
            preferences.setOpenPOandSO((Boolean) prefMap.get("openPOandSO"));
        }
        if (prefMap.containsKey("showAddressonPOSOSave") && prefMap.get("showAddressonPOSOSave") != null) {
            preferences.setShowAddressonPOSOSave((Boolean) prefMap.get("showAddressonPOSOSave"));
        }
        if (prefMap.containsKey("isAutoSaveAndPrintChkBox") && prefMap.get("isAutoSaveAndPrintChkBox") != null) {
            preferences.setAutoSaveAndPrint((Boolean) prefMap.get("isAutoSaveAndPrintChkBox"));
        }
        if (prefMap.containsKey("customervendorsortingflag") && prefMap.get("customervendorsortingflag") != null) {
            preferences.setCustomerVendorSortingFlag(Integer.parseInt(prefMap.get("customervendorsortingflag").toString()));
        }
        if (prefMap.containsKey("accountsortingflag") && prefMap.get("accountsortingflag") != null) {
            preferences.setAccountSortingFlag(Integer.parseInt(prefMap.get("accountsortingflag").toString()));
        }        
        if (prefMap.containsKey("lineLevelTermFlag") && prefMap.get("lineLevelTermFlag") != null) {
            preferences.setLineLevelTermFlag((Integer) prefMap.get("lineLevelTermFlag"));
        }
        if (prefMap.containsKey("custcreditcontrolorder") && prefMap.get("custcreditcontrolorder") != null) {
            preferences.setCustcreditcontrolorder(Integer.parseInt(prefMap.get("custcreditcontrolorder").toString()));
        }
        if (prefMap.containsKey("vendorcreditcontrolorder") && prefMap.get("vendorcreditcontrolorder") != null) {
            preferences.setVendorcreditcontrolorder(Integer.parseInt(prefMap.get("vendorcreditcontrolorder").toString()));
        }
        if (prefMap.containsKey("negativeValueIn") && prefMap.get("negativeValueIn") != null) {
            preferences.setNegativeValueIn((Integer) prefMap.get("negativeValueIn"));
        }
        if (prefMap.containsKey("allowCustomerCheckInCheckOut") && prefMap.get("allowCustomerCheckInCheckOut") != null) {
            preferences.setAllowCustomerCheckInCheckOut((Boolean)prefMap.get("allowCustomerCheckInCheckOut"));
        }
        if (prefMap.containsKey("showaccountcodeinfinancialreport")) {
            preferences.setShowAccountCodeInFinancialReport((Boolean) prefMap.get("showaccountcodeinfinancialreport"));
        }
        if (prefMap.containsKey("isAutoFillBatchDetails") && prefMap.get("isAutoFillBatchDetails") != null) {
            preferences.setAutoFillBatchDetails((Boolean) prefMap.get("isAutoFillBatchDetails"));
        }
        //set flag of Enable Cash Receive Return field
        if (prefMap.containsKey("enableCashReceiveReturn") && prefMap.get("enableCashReceiveReturn") != null) {
            preferences.setEnableCashReceiveReturn((Boolean) prefMap.get("enableCashReceiveReturn"));
        }
        return preferences;
    }    
    public void updateColumnPrefJson(ExtraCompanyPreferences preferences, HashMap<String, Object> prefMap) {
        JSONObject columnPref = new JSONObject();
        if (preferences != null && preferences.getColumnPref() != null && !preferences.getColumnPref().equals("")) {
            try {
                if (!StringUtil.isNullOrEmpty(preferences.getColumnPref())) {
                    columnPref = new JSONObject(preferences.getColumnPref());
                    JSONObject columnPrefNew = new JSONObject(prefMap.get("columnPref").toString());
                    Iterator jsonKeys = columnPrefNew.keys();
                    while (jsonKeys.hasNext()) {
                        String key = jsonKeys.next().toString();
                        columnPref.put(key, columnPrefNew.get(key));
                    }
                    preferences.setColumnPref(columnPref.toString());
                }
            } catch (JSONException j) {    // Update Json Values Instead Reaplacing old with new one
                preferences.setColumnPref((String) prefMap.get("columnPref"));
            }
        } else {
            preferences.setColumnPref((String) prefMap.get("columnPref"));
        }
    }
    public IndiaComplianceCompanyPreferences buildIndiaComplianceExtraPreferences(IndiaComplianceCompanyPreferences preferences, HashMap<String, Object> prefMap) {
        
        if (prefMap.containsKey("isAddressChanged") && prefMap.get("isAddressChanged") != null) {
            preferences.setIsaddresschanged((Boolean) prefMap.get("isAddressChanged"));
        }
        if (prefMap.containsKey("istaxonadvancereceipt") && prefMap.get("istaxonadvancereceipt") != null) {
            preferences.setIstaxonadvancereceipt((Boolean) prefMap.get("istaxonadvancereceipt"));
        }
        if (prefMap.containsKey("istcsapplicable") && prefMap.get("istcsapplicable") != null) {
            preferences.setIstcsapplicable((Boolean) prefMap.get("istcsapplicable"));
        }
        if (prefMap.containsKey("isitcapplicable") && prefMap.get("isitcapplicable") != null) {
            preferences.setIsitcapplicable((Boolean) prefMap.get("isitcapplicable"));
        }        
        if (prefMap.containsKey("istdsapplicable") && prefMap.get("istdsapplicable") != null) {
            preferences.setIstdsapplicable((Boolean) prefMap.get("istdsapplicable"));
        }
        if (prefMap.containsKey("resposiblePersonHasAddressChanged") && prefMap.get("resposiblePersonHasAddressChanged") != null) {
            preferences.setResposiblePersonAddChanged((Boolean) prefMap.get("resposiblePersonHasAddressChanged"));
        }
        if (prefMap.containsKey("resposiblePersonstate")) {
            preferences.setResposiblePersonstate((prefMap.get("resposiblePersonstate") != null)?prefMap.get("resposiblePersonstate").toString():"");
        }
        if (prefMap.containsKey("resposiblePersonAddress")) {
            preferences.setResposiblePersonAddress((prefMap.get("resposiblePersonAddress") != null)?prefMap.get("resposiblePersonAddress").toString():"");
        }
        if (prefMap.containsKey("resposiblePersonTeleNumber")) {
            preferences.setResposiblePersonTeleNumber((prefMap.get("resposiblePersonTeleNumber") != null)?prefMap.get("resposiblePersonTeleNumber").toString():"");
        }
        if (prefMap.containsKey("resposiblePersonMobNumber")) {
            preferences.setResposiblePersonMobNumber((prefMap.get("resposiblePersonMobNumber") != null)?prefMap.get("resposiblePersonMobNumber").toString():"");
        }
        if (prefMap.containsKey("resposiblePersonEmail")) {
            preferences.setResposiblePersonEmail((prefMap.get("resposiblePersonEmail") != null)?prefMap.get("resposiblePersonEmail").toString():"");
        }
        if (prefMap.containsKey("resposiblePersonPostal")) {
            preferences.setResposiblePersonPostal((prefMap.get("resposiblePersonPostal") != null)?prefMap.get("resposiblePersonPostal").toString():"");
        }
        if (prefMap.containsKey("resposiblePersonPAN")) {
            preferences.setResposiblePersonPAN((prefMap.get("resposiblePersonPAN") != null)?prefMap.get("resposiblePersonPAN").toString():"");
        }
        if (prefMap.containsKey("assessmentYear")) {
            preferences.setAssessmentYear((prefMap.get("assessmentYear") != null)?prefMap.get("assessmentYear").toString():"");
        }
        if (prefMap.containsKey("gtakkcpaybleaccount")) {// Tax KKC Payble account
            preferences.setGTAKKCPaybleAccount((prefMap.get("gtakkcpaybleaccount") != null)?prefMap.get("gtakkcpaybleaccount").toString():"");
        }
        if (prefMap.containsKey("gtasbcpaybleaccount")) {// Tax SBC Payble account
            preferences.setGTASBCPaybleAccount((prefMap.get("gtasbcpaybleaccount") != null)?prefMap.get("gtasbcpaybleaccount").toString():"");
        }
        if (prefMap.containsKey("CINnumber")) {
            preferences.setCINnumber((prefMap.get("CINnumber") != null)?prefMap.get("CINnumber").toString():"");
        }
        if (prefMap.containsKey("isGSTApplicable")) {
            preferences.setIsGSTApplicable((prefMap.get("isGSTApplicable") != null)?(Boolean)prefMap.get("isGSTApplicable"):false);
        }
        if (prefMap.containsKey("GSTIN")) {
            preferences.setGstin((prefMap.get("GSTIN") != null)?prefMap.get("GSTIN").toString():"");
        }
        if (prefMap.containsKey("companytdsinterestrate")) {
            preferences.setTdsInterestRate((double) prefMap.get("companytdsinterestrate"));
        }
        if (prefMap.containsKey("igstaccount")) {
            preferences.setIGSTAccount((prefMap.get("igstaccount") != null) ? prefMap.get("igstaccount").toString() : "");
        }
        if (prefMap.containsKey("customdutyaccount")) {
            preferences.setCustomDutyAccount((prefMap.get("customdutyaccount") != null) ? prefMap.get("customdutyaccount").toString() : "");
        }
        String str;
        if (prefMap.containsKey("companyid")) {
            str = (String) prefMap.get("companyid");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setCompany((Company) get(Company.class, str));
            } else {
                preferences.setCompany(null);
            }
        }
        return preferences;
    }
    public MRPCompanyPreferences buildExtraPreferences(MRPCompanyPreferences preferences, HashMap<String, Object> prefMap) {            
        if (prefMap.containsKey("autoGenPurchaseType")) {
            preferences.setAutoGenPurchaseType(prefMap.get("autoGenPurchaseType") != null ? Integer.parseInt((String)prefMap.get("autoGenPurchaseType")) : 0);
        }
        if (prefMap.containsKey("woInventoryUpdateType")) {
            preferences.setWoInventoryUpdateType(prefMap.get("woInventoryUpdateType") != null ? Integer.parseInt((String)prefMap.get("woInventoryUpdateType")) : 0);
        }
        if(prefMap.containsKey("mrpProductComponentType"))
        {
            preferences.setmrpProductComponentType(prefMap.get("mrpProductComponentType") != null ? Integer.parseInt((String)prefMap.get("mrpProductComponentType")) : 0);
        }
        String str;
        if (prefMap.containsKey("companyid")) {
            str = (String) prefMap.get("companyid");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setCompany((Company) get(Company.class, str));
            } else {
                preferences.setCompany(null);
            }
        }
        return preferences;
    }

    public CompanyAccountPreferences buildPreferences(CompanyAccountPreferences preferences, HashMap<String, Object> prefMap) {
        if (prefMap.containsKey("fyfrom")) {
            preferences.setFinancialYearFrom((Date) prefMap.get("fyfrom"));
        }
        if(prefMap.containsKey("gstapplicabledate")){
            preferences.setGSTApplicableDate((Date) prefMap.get("gstapplicabledate"));
        }       
        if (prefMap.containsKey("firstfyfrom")) {
            preferences.setFirstFinancialYearFrom((Date) prefMap.get("firstfyfrom"));
        }

        if (prefMap.containsKey("bbfrom")) {
            preferences.setBookBeginningFrom((Date) prefMap.get("bbfrom"));
        }

        String str;
        if (prefMap.containsKey("discountgiven")) {
            str = (String) prefMap.get("discountgiven");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setDiscountGiven(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Discount_Given));
                }
            } else {
                preferences.setDiscountGiven(null);
            }
        }

        if (prefMap.containsKey("discountreceived")) {
            str = (String) prefMap.get("discountreceived");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setDiscountReceived(account);
                if (account != null) {
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Discount_Received));
                }
            } else {
                preferences.setDiscountReceived(null);
            }
        }

//        if (prefMap.containsKey("othercharges")) {
//            str = (String) prefMap.get("othercharges");
//            if (!StringUtil.isNullOrEmpty(str)) {
//                Account account = (Account) get(Account.class, str);
//                preferences.setOtherCharges(account);
//                if(account != null){
//                    String usedin = account.getUsedIn();
//                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Other_Charges));
//                }
//            } 
//            else {
//                preferences.setOtherCharges(null);
//            }
//        }

        if (prefMap.containsKey("cashaccount")) {
            str = (String) prefMap.get("cashaccount");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setCashAccount(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Cash_Account)); 
                }
            } else {
                preferences.setCashAccount(null);
            }
        }
        if (prefMap.containsKey("foreignexchange")) {
            str = (String) prefMap.get("foreignexchange");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setForeignexchange(account);
                if (account != null) {
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Foreign_Exchange));
                }
            } else {
                preferences.setForeignexchange(null);
            }
        }
        if (prefMap.containsKey("unrealisedgainloss")) {
            str = (String) prefMap.get("unrealisedgainloss");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setUnrealisedgainloss(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Unrealised_Gain_Loss));
                }
            } else {
                preferences.setUnrealisedgainloss(null);
            }
        }
        if (prefMap.containsKey("cashoutaccforPOS")) {
            str = (String) prefMap.get("cashoutaccforPOS");
            if (!StringUtil.isNullOrEmpty(str)) {
//                Account account = (Account) get(Account.class, str);
                preferences.setPaymentMethod(str);
            }

        }
        if (prefMap.containsKey("depreciationaccount")) {
            str = (String) prefMap.get("depreciationaccount");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setDepereciationAccount(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Depreciation_Account));
                }
            } else {
                preferences.setDepereciationAccount(null);
            }
        }
        if (prefMap.containsKey("withoutinventory")) {
            preferences.setWithoutInventory((Boolean) prefMap.get("withoutinventory"));
        }

        if (prefMap.containsKey("withinvupdate")) {
            preferences.setWithInvUpdate((Boolean) prefMap.get("withinvupdate"));
        }

        if (prefMap.containsKey("editTransaction")) {
            preferences.setEditTransaction((Boolean) prefMap.get("editTransaction"));
        }

        if (prefMap.containsKey("updateInvLevelCheck")) {
            preferences.setUpdateInvLevel((Boolean) prefMap.get("updateInvLevelCheck"));
        }
        
        if (prefMap.containsKey("editLinkedTransactionQuantity")) {
            preferences.setEditLinkedTransactionQuantity((Boolean) prefMap.get("editLinkedTransactionQuantity"));
        }
        
        if (prefMap.containsKey("editLinkedTransactionPrice")) {
            preferences.setEditLinkedTransactionPrice((Boolean) prefMap.get("editLinkedTransactionPrice"));
        }

        if (prefMap.containsKey("isQaApprovalFlow")) {
            preferences.setQaApprovalFlow((Boolean) prefMap.get("isQaApprovalFlow"));
        }
        if (prefMap.containsKey("isQaApprovalFlowInDO")) {
            preferences.setQaApprovalFlowInDO((Boolean) prefMap.get("isQaApprovalFlowInDO"));
        }
        if (prefMap.containsKey("closedStatusforDo")) {
            preferences.setDoClosedStatus((Boolean) prefMap.get("closedStatusforDo"));
        }

        if (prefMap.containsKey("editso")) {
            preferences.setEditso((Boolean) prefMap.get("editso"));
        }
        if (prefMap.containsKey("showprodserial")) {
            preferences.setShowprodserial((Boolean) prefMap.get("showprodserial"));
        }
        if (prefMap.containsKey("isInventoryIntegration")) {
            preferences.setInventoryAccountingIntegration(prefMap.get("isInventoryIntegration") != null ? (Boolean) prefMap.get("isInventoryIntegration") : true);
        }
        if (prefMap.containsKey("isLocationCompulsory")) {
            preferences.setIslocationcompulsory(prefMap.get("isLocationCompulsory") != null ? (Boolean) prefMap.get("isLocationCompulsory") : true);
        }
        if (prefMap.containsKey("isWarehouseCompulsory")) {
            preferences.setIswarehousecompulsory(prefMap.get("isWarehouseCompulsory") != null ? (Boolean) prefMap.get("isWarehouseCompulsory") : true);
        }
        if (prefMap.containsKey("isRowCompulsory")) {
            preferences.setIsrowcompulsory(prefMap.get("isRowCompulsory") != null ? (Boolean) prefMap.get("isRowCompulsory") : true);
        }
        if (prefMap.containsKey("isRackCompulsory")) {
            preferences.setIsrackcompulsory(prefMap.get("isRackCompulsory") != null ? (Boolean) prefMap.get("isRackCompulsory") : true);
        }
        if (prefMap.containsKey("isBinCompulsory")) {
            preferences.setIsbincompulsory(prefMap.get("isBinCompulsory") != null ? (Boolean) prefMap.get("isBinCompulsory") : true);
        }
        if (prefMap.containsKey("isBatchCompulsory")) {
            preferences.setIsBatchCompulsory(prefMap.get("isBatchCompulsory") != null ? (Boolean) prefMap.get("isBatchCompulsory") : true);
        }
        if (prefMap.containsKey("isSerialCompulsory")) {
            preferences.setIsSerialCompulsory(prefMap.get("isSerialCompulsory") != null ? (Boolean) prefMap.get("isSerialCompulsory") : true);
        }

        if (prefMap.containsKey("memo")) {
            preferences.setMemo((Boolean) prefMap.get("memo"));
        }

        if (prefMap.containsKey("deleteTransaction")) {
            preferences.setDeleteTransaction((Boolean) prefMap.get("deleteTransaction"));
        }

        if (prefMap.containsKey("withouttax1099")) {
            preferences.setWithoutTax1099((Boolean) prefMap.get("withouttax1099"));
        }

        if (prefMap.containsKey("emailinvoice")) {
            preferences.setEmailInvoice((Boolean) prefMap.get("emailinvoice"));
        }

        if (prefMap.containsKey("setupdone")) {
            preferences.setSetupDone((Boolean) prefMap.get("setupdone"));
        }

        if (prefMap.containsKey("companytype")) {
            str = (String) prefMap.get("companytype");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setCompanyType((CompanyType) get(CompanyType.class, str));
            } else {
                preferences.setCompanyType(null);
            }
        }

        if (prefMap.containsKey("companyid")) {
            str = (String) prefMap.get("companyid");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setCompany((Company) get(Company.class, str));
            } else {
                preferences.setCompany(null);
            }
        }
        if (prefMap.containsKey("currencyChange")) {
            preferences.setCurrencyChange((Boolean) prefMap.get("currencyChange"));
        }
        if (prefMap.containsKey("DOSettings")) {
            preferences.setDOSettings((Boolean) prefMap.get("DOSettings"));
        }
        if (prefMap.containsKey("GRSettings")) {
            preferences.setGRSettings((Boolean) prefMap.get("GRSettings"));
        }
        if (prefMap.containsKey("updateStockAdjustmentPrice")) {
            preferences.setUpdateStockAdjustmentEntries((Boolean) prefMap.get("updateStockAdjustmentPrice"));
        }
        if (prefMap.containsKey("countryChange")) {
            preferences.setCountryChange((Boolean) prefMap.get("countryChange"));
        }
        if (prefMap.containsKey("gstnumber")) {
            preferences.setGstNumber((String) prefMap.get("gstnumber"));
        }
        if (prefMap.containsKey("companyuen")) {
            preferences.setCompanyUEN((String) prefMap.get("companyuen"));
        }
        if (prefMap.containsKey("industryCode")) {
            str = (String) prefMap.get("industryCode");
            if (!StringUtil.isNullOrEmpty(str)&& !str.equals("-1")) {
                preferences.setIndustryCode((MasterItem) get(MasterItem.class, str));
            } else {
                preferences.setIndustryCode(null);
            }
        }
        if (prefMap.containsKey("iafversion")) {
            preferences.setIafVersion((String) prefMap.get("iafversion"));
        }
        if (prefMap.containsKey("taxNumber")) {
            preferences.setTaxNumber((String) prefMap.get("taxNumber"));
        }
        if (prefMap.containsKey("expenseaccount")) {
            str = (String) prefMap.get("expenseaccount");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setExpenseAccount(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Salary_Expense_Account));
                }
            } else {
                preferences.setExpenseAccount(null);
            }
        }
        if (prefMap.containsKey("customerdefaultacc")) {
            str = (String) prefMap.get("customerdefaultacc");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setCustomerdefaultaccount(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Customer_Default_Account));
                }
            } else {
                preferences.setExpenseAccount(null);
            }
        }
        if (prefMap.containsKey("vendordefaultacc")) {
            str = (String) prefMap.get("vendordefaultacc");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account =  (Account) get(Account.class, str);
                preferences.setVendordefaultaccount(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Vendor_Default_Account));
                }
            } else {
                preferences.setExpenseAccount(null);
            }
        }
        if (prefMap.containsKey("liabilityaccount")) {
            str = (String) prefMap.get("liabilityaccount");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setLiabilityAccount(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Salary_Payable_Account));
                }
            } else {
                preferences.setLiabilityAccount(null);
            }
        }
        if (prefMap.containsKey("roundingDifferenceAccount")) {
            str = (String) prefMap.get("roundingDifferenceAccount");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setRoundingDifferenceAccount(account);
                if (account != null) {
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Rounding_Off_Difference));
                }
            } else {
                preferences.setRoundingDifferenceAccount(null);
            }
        }
        if (prefMap.containsKey("negativestock")) {
                str = (String) prefMap.get("negativestock");
                if (!StringUtil.isNullOrEmpty(str)) {
                    preferences.setNegativestock(Integer.parseInt(str));
                } else {
                    preferences.setNegativestock(1);
                }
            }
         if (prefMap.containsKey("custcreditcontrol")) {
                str = (String) prefMap.get("custcreditcontrol");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setCustcreditcontrol(Integer.parseInt(str));
            } else {
                preferences.setCustcreditcontrol(2);
            }
        }
        if (prefMap.containsKey("chequeNoDuplicate")) {
                str = (String) prefMap.get("chequeNoDuplicate");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setChequeNoDuplicate(Integer.parseInt(str));
            } else {
                preferences.setChequeNoDuplicate(0);
            }
        }
        if (prefMap.containsKey("viewDashboard")) {
            str = (String) prefMap.get("viewDashboard");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setviewDashboard(Integer.parseInt(str));
            } else {
                preferences.setviewDashboard(0);
            }
        }
        if (prefMap.containsKey("theme")) {
            str = (String) prefMap.get("theme");
            preferences.setTheme(str);
        }
        if (prefMap.containsKey("partNumber")) {
            preferences.setPartNumber((Boolean) prefMap.get("partNumber"));
        }
        if (prefMap.containsKey("showLeadingZero") && prefMap.get("showLeadingZero") != null) {
            preferences.setShowLeadingZero(Boolean.parseBoolean(prefMap.get("showLeadingZero").toString()));
        }
        if (prefMap.containsKey("accountWithOrWithoutCode") && prefMap.get("accountWithOrWithoutCode") != null) {
            boolean accountWithOrWithoutCode = Boolean.parseBoolean(prefMap.get("accountWithOrWithoutCode").toString());
            preferences.setAccountsWithCode(accountWithOrWithoutCode);
        }
        if (prefMap.containsKey("dependentField")) {
            preferences.setDependentField((Boolean) prefMap.get("dependentField"));
        }
        if (prefMap.containsKey("custbudgetcontrol")) {
            if (prefMap.get("custbudgetcontrol") != null && (prefMap.get("custbudgetcontrol").equals(0) || prefMap.get("custbudgetcontrol").equals(1) || prefMap.get("custbudgetcontrol").equals(2))) {
                preferences.setCustbudgetcontrol((Integer) prefMap.get("custbudgetcontrol"));
            } else {
                str = (String) prefMap.get("custbudgetcontrol");
                if (!StringUtil.isNullOrEmpty(str)) {
                    preferences.setCustbudgetcontrol(Integer.parseInt(str));
                } else {
                    preferences.setCustbudgetcontrol(2);
                }
            }
        }
//        if (prefMap.containsKey("billaddress")) {
//            if (prefMap.get("billaddress") != null) {
//                preferences.setBillAddress((String) prefMap.get("billaddress"));
//            }
//        }
//        if (prefMap.containsKey("shipaddress")) {
//            if (prefMap.get("shipaddress") != null) {
//                preferences.setShipAddress((String) prefMap.get("shipaddress"));
//            }
//        }
        if (prefMap.containsKey("approvalMail")) {
            preferences.setSendapprovalmail((Boolean) prefMap.get("approvalMail"));
        }
        if (prefMap.containsKey("sendmailto")) {
            if (prefMap.get("sendmailto") != null) {
                preferences.setApprovalEmails((String) prefMap.get("sendmailto"));
            }
        }
        if (prefMap.containsKey("shipDateConfiguration") && prefMap.get("shipDateConfiguration") != null) {
            preferences.setShipDateConfiguration((Boolean) prefMap.get("shipDateConfiguration"));
        }
        if (prefMap.containsKey("unitPriceConfiguration") && prefMap.get("unitPriceConfiguration") != null) {
            preferences.setUnitPriceConfiguration((Boolean) prefMap.get("unitPriceConfiguration"));
        }
          if (prefMap.containsKey("viewDetailsPerm")) {
            preferences.setViewDetailsPerm((Boolean) prefMap.get("viewDetailsPerm"));
        }
         if (prefMap.containsKey("isFilterProductByCustomerCategory")) {
            preferences.setFilterProductByCustomerCategory(prefMap.get("isFilterProductByCustomerCategory") != null ? (Boolean) prefMap.get("isFilterProductByCustomerCategory") : false);
        }
        if (prefMap.containsKey("productsortingflag")) {
            preferences.setProductSortingFlag(prefMap.get("productsortingflag") != null ? Integer.parseInt((String)prefMap.get("productsortingflag")) : 0);
        }
        if (prefMap.containsKey("negativeStockSO")) {
            str = (String) prefMap.get("negativeStockSO");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setNegativeStockSO(Integer.parseInt(str));
            } else {
                preferences.setNegativeStockSO(0);
            }
        }
        if (prefMap.containsKey("negativeStockSICS")) {
            str = (String) prefMap.get("negativeStockSICS");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setNegativeStockSICS(Integer.parseInt(str));
            } else {
                preferences.setNegativeStockSICS(0);
            }
        }     
        if (prefMap.containsKey("negativeStockPR")) {
            str = (String) prefMap.get("negativeStockPR");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setNegativeStockPR(Integer.parseInt(str));
            } else {
                preferences.setNegativeStockPR(0);
            }
        }    
        if (prefMap.containsKey("inventoryvaluationtype")) {
            str = (String) prefMap.get("inventoryvaluationtype");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setInventoryValuationType(Integer.parseInt(str));
            } else {
                preferences.setInventoryValuationType(0);
            }
        }
        
        if (prefMap.containsKey("stockadjustmentaccountid")) {
            str = (String) prefMap.get("stockadjustmentaccountid");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setStockadjustmentaccount(account);
            }else{
                preferences.setStockadjustmentaccount(null);
            }
        }
        if (prefMap.containsKey("inventoryaccountid")) {
            str = (String) prefMap.get("inventoryaccountid");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setInventoryaccount(account);
            }else{
                preferences.setInventoryaccount(null);
            }
        }
        if (prefMap.containsKey("cogsaccountid")) {
            str = (String) prefMap.get("cogsaccountid");
            if (!StringUtil.isNullOrEmpty(str)) {
                Account account = (Account) get(Account.class, str);
                preferences.setCogsaccount(account);
            }else{
                preferences.setCogsaccount(null);
            }
        }        
        if (prefMap.containsKey("isshowmarginbutton") && prefMap.get("isshowmarginbutton") != null) {
            preferences.setShowMarginButton((Boolean) prefMap.get("isshowmarginbutton"));
        }
            
       return preferences;
    }
    @Override
    public KwlReturnObject addYearLock(Map<String, Object> yearLockMap) throws ServiceException {
        List list = new ArrayList();
        try {
            YearLock yearlock = new YearLock();
            yearlock.setDeleted(false);
            yearlock = buildYearLock(yearlock, yearLockMap);
            save(yearlock);
            list.add(yearlock);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addYearLock : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Year Lock has been added successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject updateYearLock(Map<String, Object> yearLockMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = (String) yearLockMap.get("id");
            YearLock yearlock = (YearLock) get(YearLock.class, id);
            if (yearlock != null) {
                yearlock = buildYearLock(yearlock, yearLockMap);
                saveOrUpdate(yearlock);
            }
            list.add(yearlock);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateYearLock : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Year Lock has been updated successfully", null, list, list.size());
    }

    private YearLock buildYearLock(YearLock yearlock, Map<String, Object> yearLockMap) {
        if (yearLockMap.containsKey("islock")) {
            yearlock.setIsLock((Boolean) yearLockMap.get("islock"));
        }

        if (yearLockMap.containsKey("yearid")) {
            yearlock.setYearid((Integer) yearLockMap.get("yearid"));
        }

        if (yearLockMap.containsKey("companyid")) {
            String str = (String) yearLockMap.get("companyid");
            if (!StringUtil.isNullOrEmpty(str)) {
                yearlock.setCompany((Company) get(Company.class, str));
            } else {
                yearlock.setCompany(null);
            }
        }
        return yearlock;
    }

    public KwlReturnObject getSequenceFormat(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from SequenceFormat y where y.deleted=false ";

        if (filterParams.containsKey("companyid")) {
            condition += " and y.company.companyID=? ";
            params.add(filterParams.get("companyid"));
        }
        if (filterParams.containsKey("id")) {
            condition += " and y.ID=? ";
            params.add(filterParams.get("id"));
        }
        if (filterParams.containsKey("modulename")) {
            condition += " and y.modulename=? ";
            params.add(filterParams.get("modulename"));
        }
        if (filterParams.containsKey("moduleid")) {
            condition += " and y.moduleid=? ";
            params.add(filterParams.get("moduleid"));
        }
        if (filterParams.containsKey("name")) {
            condition += " and y.name=? ";
            params.add(filterParams.get("name"));
        }
        if (filterParams.containsKey("dateFormatinPrefix")) {
            condition += " and y.dateformatinprefix=? ";
            params.add(filterParams.get("dateFormatinPrefix"));
        }
        if (filterParams.containsKey("dateFormatAfterPrefix")) {
            condition += " and y.dateformatafterprefix=? ";
            params.add(filterParams.get("dateFormatAfterPrefix"));
        }
        if (filterParams.containsKey("selectedsuffixdateformat")) {
            condition += " and y.dateFormatAfterSuffix=? ";
            params.add(filterParams.get("selectedsuffixdateformat"));
        }
        if (filterParams.containsKey("isChecked")) {
            condition += " and y.isactivate=? ";
            params.add(filterParams.get("isChecked"));
        }
        if (filterParams.containsKey("isdefaultFormat")) {
            boolean defFormat = filterParams.get("isdefaultFormat") == null ? false : (Boolean) filterParams.get("isdefaultFormat");
            if (defFormat) {
                condition += "and y.isdefaultformat=? ";
                params.add(defFormat);
            }
        }
        if (filterParams.containsKey("masterid")) {
            condition += " and y.custom=? ";
            params.add(filterParams.get("masterid"));
        }
        query += condition;
//        query += "order by y.createdon desc";
//        String query="from YearLock where deleted=false and company.companyID=?";
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    /**
     * Method to save the Allow Zero quantity settings from system preferences 
     */
     @Override
    public JSONObject saveAllowZeroQtyForProduct (JSONObject paramJobj) throws ServiceException{
        JSONObject jobj = new JSONObject();
        boolean AllowZeroQuantityForDO = false,AllowZeroQuantityInQuotation = false,AllowZeroQuantityInSI = false,AllowZeroQuantityInGRO=false,AllowZeroQuantityInVQ=false,
                AllowZeroQuantityInPI = false,AllowZeroQuantityInSO = false,AllowZeroQuantityInPO = false,AllowZeroQuantityInSR=false,AllowZeroQuantityInPR=false;
        String companyId="";
        try{
            if (!StringUtil.isNullObject(paramJobj.get(Constants.companyKey))) {
                companyId = paramJobj.getString(Constants.companyKey);
            }
            
            if (!StringUtil.isNullObject(paramJobj.get("AllowZeroQuantityForDO"))) {
                AllowZeroQuantityForDO = paramJobj.getBoolean("AllowZeroQuantityForDO");
            }
            if (!StringUtil.isNullObject(paramJobj.get("AllowZeroQuantityInQuotation"))) {
                AllowZeroQuantityInQuotation = paramJobj.getBoolean("AllowZeroQuantityInQuotation");
            }
            if (!StringUtil.isNullObject(paramJobj.get("AllowZeroQuantityInSI"))) {
                AllowZeroQuantityInSI = paramJobj.getBoolean("AllowZeroQuantityInSI");
            }
            if (!StringUtil.isNullObject(paramJobj.get("AllowZeroQuantityInPI"))) {
                AllowZeroQuantityInPI = paramJobj.getBoolean("AllowZeroQuantityInPI");
            }
            if (!StringUtil.isNullObject(paramJobj.get("AllowZeroQuantityInSO"))) {
                AllowZeroQuantityInSO = paramJobj.getBoolean("AllowZeroQuantityInSO");
            }
            if (!StringUtil.isNullObject(paramJobj.get("AllowZeroQuantityInPO"))) {
                AllowZeroQuantityInPO = paramJobj.getBoolean("AllowZeroQuantityInPO");
            }
            if (!StringUtil.isNullObject(paramJobj.get("AllowZeroQuantityInSR"))) {
                AllowZeroQuantityInSR = paramJobj.getBoolean("AllowZeroQuantityInSR");
            }
            if (!StringUtil.isNullObject(paramJobj.get("AllowZeroQuantityInPR"))) {
                AllowZeroQuantityInPR = paramJobj.getBoolean("AllowZeroQuantityInPR");
            }
            if (!StringUtil.isNullObject(paramJobj.get("AllowZeroQuantityInGRO"))) {
                AllowZeroQuantityInGRO = paramJobj.getBoolean("AllowZeroQuantityInGRO");
            }
            if (!StringUtil.isNullObject(paramJobj.get("AllowZeroQuantityInVQ"))) {
                AllowZeroQuantityInVQ = paramJobj.getBoolean("AllowZeroQuantityInVQ");
            }
            HashMap<String,Object> requestParams = new HashMap<>();
            requestParams.put("company", companyId);
            requestParams.put("AllowZeroQuantityForDO", AllowZeroQuantityForDO);
            requestParams.put("AllowZeroQuantityInQuotation", AllowZeroQuantityInQuotation);
            requestParams.put("AllowZeroQuantityInSI", AllowZeroQuantityInSI);
            requestParams.put("AllowZeroQuantityInPI", AllowZeroQuantityInPI);
            requestParams.put("AllowZeroQuantityInSO", AllowZeroQuantityInSO);
            requestParams.put("AllowZeroQuantityInPO", AllowZeroQuantityInPO);
            requestParams.put("AllowZeroQuantityInSR", AllowZeroQuantityInSR);
            requestParams.put("AllowZeroQuantityInPR", AllowZeroQuantityInPR);
            requestParams.put("AllowZeroQuantityInGRO", AllowZeroQuantityInGRO);
            requestParams.put("AllowZeroQuantityInVQ", AllowZeroQuantityInVQ);

            ExtraCompanyPreferences prefRes = new ExtraCompanyPreferences();
            if (!StringUtil.isNullOrEmpty(companyId)) {
                prefRes = (ExtraCompanyPreferences) get(ExtraCompanyPreferences.class, companyId);
            } 

            ExtraCompanyPreferences companyPreferences = null;

            if (prefRes != null) {
                requestParams.put("id", companyId);
                companyPreferences =updateExtraCompanyPreferences(requestParams);
            } else {
                companyPreferences = saveExtraCompanyPreferences(requestParams);
            }
            
            jobj.put("AllowZeroQuantityForDO", companyPreferences.isAllowZeroQuantityForProduct());
            jobj.put("AllowZeroQuantityInQuotation", companyPreferences.isAllowZeroQuantityInQuotation());
            jobj.put("AllowZeroQuantityInSI", companyPreferences.isAllowZeroQuantityInSI());
            jobj.put("AllowZeroQuantityInPI", companyPreferences.isAllowZeroQuantityInPI());
            jobj.put("AllowZeroQuantityInSO", companyPreferences.isAllowZeroQuantityInSO());
            jobj.put("AllowZeroQuantityInPO", companyPreferences.isAllowZeroQuantityInPO());
            jobj.put("AllowZeroQuantityInSR", companyPreferences.isAllowZeroQuantityInSR());
            jobj.put("AllowZeroQuantityInPR", companyPreferences.isAllowZeroQuantityInPR());
            jobj.put("AllowZeroQuantityInGRO", companyPreferences.isAllowZeroQuantityInGRO());
            jobj.put("AllowZeroQuantityInVQ", companyPreferences.isAllowZeroQuantityInVQ());
            
        }catch ( JSONException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveFixedAssetSetting : " + ex.getMessage(), ex);
        }
        return jobj;
    }
     @Override
    public KwlReturnObject getActivatedSequenceFormat(String companyid) throws ServiceException {
        try {
            JSONArray jArr = new JSONArray(); 
            JSONObject naSeqObj = new JSONObject();
                naSeqObj.put("id", "NA");
                naSeqObj.put("value", "NA");
                naSeqObj.put("oldflag", false);
                jArr.put(naSeqObj);
                String hql = "from SequenceFormat y where y.deleted=false  and y.company.companyID=?  and y.modulename=? and y.isactivate=?";    //sent only activated sequence formats
                List list = executeQuery(hql, new Object[]{companyid, "autocustomerid", true});
                return new KwlReturnObject(true, "", null, list, list.size());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getActivatedSequenceFormat : " + ex.getMessage(), ex);
        }
    }
     
     @Override
    public KwlReturnObject getSequenceFormatforModuleid(Map<String,Object> requestParams) throws ServiceException {
        try {
            String modulename = Constants.Mode_Customer;
            String companyid = "",condition="";
            ArrayList params = new ArrayList();
            params.add(true);
            if (requestParams.containsKey(Constants.companyKey)) {
                companyid = (String) requestParams.get(Constants.companyKey);
                condition += "and y.company.companyID=?";
                params.add(companyid);
            }

            if (requestParams.containsKey(Constants.moduleid)) {
                int moduleid = (Integer) requestParams.get(Constants.moduleid);
                if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                    modulename = Constants.Mode_SalesOrder;
                } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    modulename = Constants.Mode_SalesInvoice;
                } else if (moduleid == Constants.Acc_Customer_ModuleId) {
                    modulename = Constants.Mode_Customer;
                } else if (moduleid == Constants.Acc_Credit_Note_ModuleId) {
                    modulename = Constants.Mode_CreditNote;
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    modulename = Constants.Mode_SalesReturn;
                } else if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                    modulename = Constants.Mode_ReceivePayment;
                } else if (moduleid == Constants.Acc_Make_Payment_ModuleId) {
                    modulename = Constants.Mode_MakePayment;
                } else if (moduleid == Constants.Acc_Cash_Sales_ModuleId) {
                    modulename =Constants.Mode_CashSales;
                } else if (moduleid == Constants.Acc_Vendor_Quotation_ModuleId) {
                    modulename = Constants.Mode_VendorQuotation;
                } else if (moduleid == Constants.Acc_Customer_Quotation_ModuleId) {
                    modulename = Constants.Mode_CustomerQuotation;
                } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    modulename = Constants.Mode_DeliveryOrder;
                } else if (moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                    modulename = Constants.Mode_GoodsReceiptOrder;
                } else if (moduleid == Constants.Acc_Product_Master_ModuleId) {
                    modulename =Constants.Mode_Product;
                }
                
                condition += "and y.modulename=?";
                params.add(modulename);
            }
            if (requestParams.containsKey(Constants.isdefault)) {
                condition += " and y.isdefaultformat=?";
                params.add(requestParams.get(Constants.isdefault));
            }
                
                String hql = "from SequenceFormat y where y.deleted=false and y.isactivate=? "+condition;    //sent only activated sequence formats
                List list = executeQuery(hql,params.toArray());
                return new KwlReturnObject(true, "", null, list, list.size());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.getActivatedSequenceFormat : " + ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject getChequeSequenceFormatList(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from ChequeSequenceFormat ch where ch.company.companyID=?";

        params.add(filterParams.get("companyid"));
        if (filterParams.containsKey("showleadingzero")) {
            condition += " and ch.showLeadingZero=? ";
            params.add(filterParams.get("showleadingzero"));
        }
        if (filterParams.containsKey("numberofdigit")) {
            condition += " and ch.numberOfDigits=? ";
            params.add(filterParams.get("numberofdigit"));
        }
        if (filterParams.containsKey("startfrom")) {
            condition += " and ch.startFrom=? ";
//            int startfrom = (Integer) filterParams.get("startfrom");
            BigInteger startFrom = new BigInteger(String.valueOf(filterParams.get("startfrom")));
            params.add(startFrom);
        }

        if (filterParams.containsKey("bankAccountId")) {
            condition += " and ch.bankAccount.ID=? ";
            params.add(filterParams.get("bankAccountId"));
        }

        if (filterParams.containsKey("isChecked")) {
            condition += " and ch.isactivate=? ";
            params.add(filterParams.get("isChecked"));
        }
        if (filterParams.containsKey("chequeEndNumber")) {
            condition += " and ch.chequeEndNumber=? ";
            BigInteger chequeEndNumber =new BigInteger(String.valueOf(filterParams.get("chequeEndNumber")));
            params.add(chequeEndNumber);
        }
        if (filterParams.containsKey("name")) {
            condition += " and ch.name=? ";
            params.add(filterParams.get("name"));
        }
        
        if (filterParams.containsKey("id")) {
            condition += " and ch.id=? ";
            params.add(filterParams.get("id"));
        }
        query += condition;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

      @Override
    public KwlReturnObject getAccountList(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from Account ch where ch.company.companyID=?";

        params.add(filterParams.get("companyid"));
      

        if (filterParams.containsKey("accountcode") && !filterParams.get("accountcode").equals("")) {
            condition += " and ch.acccode=? ";
            params.add(filterParams.get("accountcode"));
        }
       
        if (filterParams.containsKey("bankname") && !filterParams.get("bankname").equals("")) {
            condition += " or ch.name=? ";
            params.add(filterParams.get("bankname"));
        }

        query += condition;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
      
    @Override
    public List getCustomerNameByID(HashMap<String, Object> dataMap) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String query = "select cstr.name,cstr.acccode from customer cstr where cstr.id=? and cstr.company=?";
        String customerID = (String) dataMap.get("customerID");
        String companyid = (String) dataMap.get("companyid");
        params.add(customerID);
        params.add(companyid);
        returnList = executeSQLQuery( query, params.toArray());;
        return returnList;
    }


    @Override
    public ChequeSequenceFormat saveChequeSequenceFormat(HashMap<String, Object> dataMap) {
        ChequeSequenceFormat seqFormat = new ChequeSequenceFormat();
        if (dataMap.containsKey("id")) {
            String id = (String) dataMap.get("id");
            if (!StringUtil.isNullOrEmpty(id)) {
                seqFormat = (ChequeSequenceFormat) get(ChequeSequenceFormat.class, id);
            }
        }
        if (dataMap.containsKey("numberofdigit")) {
            seqFormat.setNumberOfDigits(Integer.parseInt(dataMap.get("numberofdigit").toString()));
        }
        if (dataMap.containsKey("prefix")) {
            seqFormat.setPrefix(dataMap.get("prefix").toString());
        }
        if (dataMap.containsKey("suffix")) {
            seqFormat.setSuffix(dataMap.get("suffix").toString());
        }
        if (dataMap.containsKey("isshowdateinprefix")) {
            seqFormat.setDateBeforePrefix(Boolean.parseBoolean(dataMap.get("isshowdateinprefix").toString()));
        }
        if (dataMap.containsKey("dateFormatinPrefix")) {
            seqFormat.setDateformatinprefix(dataMap.get("dateFormatinPrefix").toString());
        }
        if (dataMap.containsKey("isshowdateafterprefix")) {
            seqFormat.setDateAfterPrefix(Boolean.parseBoolean(dataMap.get("isshowdateafterprefix").toString()));
        }
        if (dataMap.containsKey("dateFormatAfterPrefix")) {
            seqFormat.setDateformatafterprefix(dataMap.get("dateFormatAfterPrefix").toString());
        }
        if (dataMap.containsKey("showdateaftersuffix")) {
            seqFormat.setShowDateFormatAfterSuffix(Boolean.parseBoolean(dataMap.get("showdateaftersuffix").toString()));
        }
        if (dataMap.containsKey("selectedsuffixdateformat")) {
            seqFormat.setDateFormatAfterSuffix(dataMap.get("selectedsuffixdateformat").toString());
        }
        if (dataMap.containsKey("resetcounter")) {
            seqFormat.setResetCounter(Boolean.parseBoolean(dataMap.get("resetcounter").toString()));
        }
        if (dataMap.containsKey("startfrom")) {
            seqFormat.setStartFrom(new BigInteger(dataMap.get("startfrom").toString()));
        }
        if (dataMap.containsKey("showleadingzero")) {
            seqFormat.setShowLeadingZero(Boolean.parseBoolean(dataMap.get("showleadingzero").toString()));
        }
        if (dataMap.containsKey("isChecked")) {
            seqFormat.setIsactivate(Boolean.parseBoolean(dataMap.get("isChecked").toString()));
        }
        if (dataMap.containsKey("isdefault")) {
            seqFormat.setIsdefault(Boolean.parseBoolean(dataMap.get("isdefault").toString()));
        }
        if (dataMap.containsKey("chequeEndNumber")) {
            seqFormat.setChequeEndNumber(new BigInteger(dataMap.get("chequeEndNumber").toString()));
        }
        if (dataMap.containsKey("name")) {
            seqFormat.setName((String) dataMap.get("name"));
        }
        if (dataMap.containsKey("companyid")) {
            String companyid = (String) dataMap.get("companyid");
            if (!StringUtil.isNullOrEmpty(companyid)) {
                seqFormat.setCompany((Company) get(Company.class, companyid));
            } else {
                seqFormat.setCompany(null);
            }
        }
        if (dataMap.containsKey("bankAccountId")) {
            String bankAccountId = (String) dataMap.get("bankAccountId");
            if (!StringUtil.isNullOrEmpty(bankAccountId)) {
                seqFormat.setBankAccount((Account) get(Account.class, bankAccountId));
            } else {
                seqFormat.setBankAccount(null);
            }
        }
        try {
            updateDefaultChequeSequenceFormat(dataMap);
            saveOrUpdate(seqFormat);
        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return seqFormat;
    }
    
    public void updateDefaultChequeSequenceFormat(HashMap<String, Object> dataMap) throws ServiceException {
        if (dataMap.containsKey("isdefault")) {
            boolean isdefault = Boolean.parseBoolean(dataMap.get("isdefault").toString());
            if (isdefault == true) {
                ArrayList params = new ArrayList();
                params.add((String) dataMap.get("companyid"));

                String query = " update chequesequenceformat csf set csf.isdefault='F' where csf.company = ?";
                int num = executeSQLUpdate( query, params.toArray());
            }
        }
    }

      public ChequeSequenceFormat updateChequeSequenceFormat(HashMap<String, Object> dataMap) throws ServiceException, AccountingException {
        ChequeSequenceFormat chequeSequenceFormat = new ChequeSequenceFormat();
        ArrayList params = new ArrayList();
        ArrayList params1 = new ArrayList();
        ArrayList params2 = new ArrayList();
        boolean isdefaultformat = false;
        boolean isChecked = false;
        if (dataMap.containsKey("isdefault")) {
            isdefaultformat = Boolean.parseBoolean(dataMap.get("isdefault").toString());
            if (isdefaultformat == true) {
                params1.add("T");
            } else {
                params1.add("F");
            }
        }
        if (dataMap.containsKey("isChecked")) {
            isChecked = Boolean.parseBoolean(dataMap.get("isChecked").toString());
            if (isChecked == true) {
                params1.add("T");
            } else {
                params1.add("F");
            }
        }
        
        if (dataMap.containsKey("companyid")) {
            String companyid = (String) dataMap.get("companyid");
            params.add(companyid);
            params2.add(companyid);
            params1.add(companyid);
        }
        if (dataMap.containsKey("id")) {
            String id = dataMap.get("id").toString();
            params.add(id);
            params1.add(id);
        }
        List returnList = new ArrayList();
        if (isdefaultformat == true) {                  // to set format as default
            //this 1st query is for setting all previous sequence format as default false because there can be only one SF must be default.
            String query = " update chequesequenceformat csf set csf.isdefault='F' where csf.company = ?";
            int num = executeSQLUpdate( query, params2.toArray());
            
            //this 2nd query is for u[pdating default SF.
            String query1 = " update chequesequenceformat csf set csf.isdefault=?, csf.isactivate=? where csf.company = ? and csf.id=? ";
            int num1 = executeSQLUpdate( query1, params1.toArray());
        } else {
            String query = " select csf.isdefault from chequesequenceformat csf where csf.company = ? and csf.id=? and csf.isdefault='T' ";

            returnList = executeSQLQuery( query, params.toArray());
            if (returnList.size() > 0) {                                   // prevent update for default format
                throw new AccountingException("acc.companypreferences.cannotEditSequenceFormat");
            } else {                                                            // update for non default
                String query1 = " update chequesequenceformat csf set csf.isdefault=?, csf.isactivate=? where csf.company = ? and csf.id=? ";
                int num1 = executeSQLUpdate( query1, params1.toArray());
            }
        }
        return chequeSequenceFormat;
    }
    @Override
    public int deleteChequeSequenceFormat(String id, String companyId) throws ServiceException {
        String query = "Delete From ChequeSequenceFormat ch where ch.id=? and ch.company.companyID=?";
        int delRows = executeUpdate( query, new Object[]{id, companyId});
        return delRows;
    }

    public SequenceFormat saveSequenceFormat(HashMap<String, Object> dataMap) throws ServiceException {
        SequenceFormat seqFormat = new SequenceFormat();
        if (dataMap.containsKey("id")) {
            String id = (String) dataMap.get("id");
            if (!StringUtil.isNullOrEmpty(id)) {
                seqFormat = (SequenceFormat) get(SequenceFormat.class, id);
            }
        }
        if (dataMap.containsKey("name")) {
            seqFormat.setName(dataMap.get("name").toString());
        }
        if (dataMap.containsKey("prefix")) {
            seqFormat.setPrefix(dataMap.get("prefix").toString());
        }
        if (dataMap.containsKey("suffix")) {
            seqFormat.setSuffix(dataMap.get("suffix").toString());
        }
        if (dataMap.containsKey("numberofdigit")) {
            seqFormat.setNumberofdigit(Integer.parseInt(dataMap.get("numberofdigit").toString()));
        }
        if (dataMap.containsKey("startfrom")) {
            seqFormat.setStartfrom(Integer.parseInt(dataMap.get("startfrom").toString()));
        }
        if (dataMap.containsKey("showleadingzero")) {
            seqFormat.setShowleadingzero(Boolean.parseBoolean(dataMap.get("showleadingzero").toString()));
        }
        if (dataMap.containsKey("isdefaultformat")) {
            seqFormat.setIsdefaultformat(Boolean.parseBoolean(dataMap.get("isdefaultformat").toString()));
        }
        if (dataMap.containsKey("deleted")) {
            seqFormat.setDeleted(Boolean.parseBoolean(dataMap.get("deleted").toString()));
        }
        if (dataMap.containsKey("isshowdateinprefix")) {
            seqFormat.setDateBeforePrefix(Boolean.parseBoolean(dataMap.get("isshowdateinprefix").toString()));
        }
        if (dataMap.containsKey("dateFormatinPrefix")) {
            seqFormat.setDateformatinprefix(dataMap.get("dateFormatinPrefix").toString());
        }
        if (dataMap.containsKey("isshowdateafterprefix")) {
            seqFormat.setDateAfterPrefix(Boolean.parseBoolean(dataMap.get("isshowdateafterprefix").toString()));
        }
        if (dataMap.containsKey("dateFormatAfterPrefix")) {
            seqFormat.setDateformatafterprefix(dataMap.get("dateFormatAfterPrefix").toString());
        }
        if (dataMap.containsKey("showdateaftersuffix")) {
            seqFormat.setShowDateFormatAfterSuffix(Boolean.parseBoolean(dataMap.get("showdateaftersuffix").toString()));
        }
        if (dataMap.containsKey("selectedsuffixdateformat")) {
            seqFormat.setDateFormatAfterSuffix(dataMap.get("selectedsuffixdateformat").toString());
        }
        if (dataMap.containsKey("moduleid")) {
            seqFormat.setModuleid(Integer.parseInt(dataMap.get("moduleid").toString()));
        }
        if (dataMap.containsKey("isChecked")) {
            seqFormat.setIsactivate(Boolean.parseBoolean(dataMap.get("isChecked").toString()));
        }
        if (dataMap.containsKey("resetcounter")) {
            seqFormat.setResetCounter(Boolean.parseBoolean(dataMap.get("resetcounter").toString()));
        }
        if (dataMap.containsKey("modulename")) {
            seqFormat.setModulename(dataMap.get("modulename").toString());
        }
        if (dataMap.containsKey("custom")) {
            seqFormat.setCustom(dataMap.get("custom").toString());
        }
        if (dataMap.containsKey("companyid")) {
            String companyid = (String) dataMap.get("companyid");
            if (!StringUtil.isNullOrEmpty(companyid)) {
                seqFormat.setCompany((Company) get(Company.class, companyid));
            } else {
                seqFormat.setCompany(null);
            }
        }
        updateDefaultFormat(dataMap);
        saveOrUpdate(seqFormat);
        return seqFormat;
    }

    public void updateDefaultFormat(HashMap<String, Object> dataMap) throws ServiceException {
        if (dataMap.containsKey("isdefaultformat")) {
            boolean isdefaultformat = Boolean.parseBoolean(dataMap.get("isdefaultformat").toString());
            if (isdefaultformat == true) {
                ArrayList params = new ArrayList();
                params.add((String) dataMap.get("companyid"));
                params.add(dataMap.get("modulename").toString());

                String query = " update sequenceformat sf set sf.isdefaultformat='F' where sf.company = ? and sf.modulename=? ";
                int num = executeSQLUpdate( query, params.toArray());
            }
        }
    }

    @Override
    public ExtraCompanyPreferences saveExtraCompanyPreferences(HashMap<String, Object> dataMap) {
        ExtraCompanyPreferences accountsData = new ExtraCompanyPreferences();
        if (dataMap.containsKey("id") && dataMap.get("id") != null) {
            accountsData.setId(dataMap.get("id").toString());
        }
        if (dataMap.containsKey("wipAccountPrefix") && dataMap.get("wipAccountPrefix") != null) {
            accountsData.setWipAccountPrefix(dataMap.get("wipAccountPrefix").toString());
        }
        if (dataMap.containsKey("cpAccountPrefix") && dataMap.get("cpAccountPrefix") != null) {
            accountsData.setCpAccountPrefix(dataMap.get("cpAccountPrefix").toString());
        }
        if (dataMap.containsKey("wipAccountType") && dataMap.get("wipAccountType") != null) {
            String wipAccountType = dataMap.get("wipAccountType").toString();
//            Group group = (Group) get(Group.class, wipAccountType);
            accountsData.setWipAccountTypeId(wipAccountType);
        }
        if (dataMap.containsKey("cpAccountType") && dataMap.get("cpAccountType") != null) {
            String cpAccountType = dataMap.get("cpAccountType").toString();
//            Group group = (Group) get(Group.class, cpAccountType);
            accountsData.setCpAccountTypeId(cpAccountType);
        }
        if (dataMap.containsKey("assetSetingActivation") && dataMap.get("assetSetingActivation") != null) {
            accountsData.setAssetSetingActivation((Boolean) dataMap.get("assetSetingActivation"));
        }
        
        if (dataMap.containsKey("allowToPostOpeningDepreciation") && dataMap.get("allowToPostOpeningDepreciation") != null) {
            accountsData.setAllowToPostOpeningDepreciation((Boolean) dataMap.get("allowToPostOpeningDepreciation"));
        }
                
        if (dataMap.containsKey("depreciationCalculationType") && dataMap.get("depreciationCalculationType") != null) {
            accountsData.setAssetDepreciationCalculationType((Integer) dataMap.get("depreciationCalculationType"));
        }
        if (dataMap.containsKey("depreciationCalculationBasedOn") && dataMap.get("depreciationCalculationBasedOn") != null) {
            accountsData.setAssetDepreciationCalculationBasedOn((Integer) dataMap.get("depreciationCalculationBasedOn"));
        }
         if (dataMap.containsKey("activatebudgetingforPR") && dataMap.get("activatebudgetingforPR") != null) {
            accountsData.setActivatebudgetingforPR((Boolean) dataMap.get("activatebudgetingforPR"));
        }
        if (dataMap.containsKey("budgetType") && dataMap.get("budgetType") != null) {
            accountsData.setBudgetType((Integer) dataMap.get("budgetType"));
        }
        if (dataMap.containsKey("budgetFreqType") && dataMap.get("budgetFreqType") != null) {
            accountsData.setBudgetFreqType((Integer) dataMap.get("budgetFreqType"));
        }
        if (dataMap.containsKey("budgetwarnblock") && dataMap.get("budgetwarnblock") != null) {
            accountsData.setBudgetwarnblock((Integer) dataMap.get("budgetwarnblock"));
        }
        if (dataMap.containsKey("UomSchemaType") && dataMap.get("UomSchemaType") != null) {
            int UomSchemaType =Integer.parseInt(dataMap.get("UomSchemaType").toString());
            accountsData.setUomSchemaType(UomSchemaType);
        }
        if (dataMap.containsKey("profitLossAccountId") && dataMap.get("profitLossAccountId") != null) {
            accountsData.setProfitLossAccountId((String) dataMap.get("profitLossAccountId"));
        }
        if (dataMap.containsKey("openingStockAccountId") && dataMap.get("openingStockAccountId") != null) {
            accountsData.setOpeningStockAccountId((String) dataMap.get("openingStockAccountId"));
        }
        if (dataMap.containsKey("closingStockAccountId") && dataMap.get("closingStockAccountId") != null) {
            accountsData.setClosingStockAccountId((String) dataMap.get("closingStockAccountId"));
        }
        if (dataMap.containsKey("stockInHandAccountId") && dataMap.get("stockInHandAccountId") != null) {
            accountsData.setStockInHandAccountId((String) dataMap.get("stockInHandAccountId"));
        }
        if (dataMap.containsKey("AllowZeroQuantityForDO") && dataMap.get("AllowZeroQuantityForDO") != null) {
            accountsData.setAllowZeroQuantityForProduct((boolean)(dataMap.get("AllowZeroQuantityForDO")));
        }
        if (dataMap.containsKey("AllowZeroQuantityInQuotation") && dataMap.get("AllowZeroQuantityInQuotation") != null) {
            accountsData.setAllowZeroQuantityInQuotation((boolean) dataMap.get("AllowZeroQuantityInQuotation"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInSI") && dataMap.get("AllowZeroQuantityInSI") != null) {
            accountsData.setAllowZeroQuantityInSI((boolean) dataMap.get("AllowZeroQuantityInSI"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInPI") && dataMap.get("AllowZeroQuantityInPI") != null) {
            accountsData.setAllowZeroQuantityInPI((boolean) dataMap.get("AllowZeroQuantityInPI"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInSO") && dataMap.get("AllowZeroQuantityInSO") != null) {
            accountsData.setAllowZeroQuantityInSO((boolean) dataMap.get("AllowZeroQuantityInSO"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInPO") && dataMap.get("AllowZeroQuantityInPO") != null) {
            accountsData.setAllowZeroQuantityInPO((boolean) dataMap.get("AllowZeroQuantityInPO"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInSR") && dataMap.get("AllowZeroQuantityInSR") != null) {
            accountsData.setAllowZeroQuantityInSR((boolean) dataMap.get("AllowZeroQuantityInSR"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInPR") && dataMap.get("AllowZeroQuantityInPR") != null) {
            accountsData.setAllowZeroQuantityInPR((boolean) dataMap.get("AllowZeroQuantityInPR"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInGRO") && dataMap.get("AllowZeroQuantityInGRO") != null) {
            accountsData.setAllowZeroQuantityInGRO((boolean) dataMap.get("AllowZeroQuantityInGRO"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInVQ") && dataMap.get("AllowZeroQuantityInVQ") != null) {
            accountsData.setAllowZeroQuantityInVQ((boolean) dataMap.get("AllowZeroQuantityInVQ"));
        }
        if (dataMap.containsKey("company")) {
            String companyid = (String) dataMap.get("company");
            if (!StringUtil.isNullOrEmpty(companyid)) {
                accountsData.setCompany((Company) get(Company.class, companyid));
            } else {
                accountsData.setCompany(null);
            }
        }

        try {
            save(accountsData);
        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return accountsData;
    }

    @Override
    public ExtraCompanyPreferences updateExtraCompanyPreferences(HashMap<String, Object> dataMap) {
        String id = (String) dataMap.get("id");
        ExtraCompanyPreferences accountsData = (ExtraCompanyPreferences) get(ExtraCompanyPreferences.class, id);

        if (dataMap.containsKey("wipAccountPrefix") && dataMap.get("wipAccountPrefix") != null) {
            accountsData.setWipAccountPrefix(dataMap.get("wipAccountPrefix").toString());
        }
        if (dataMap.containsKey("cpAccountPrefix") && dataMap.get("cpAccountPrefix") != null) {
            accountsData.setCpAccountPrefix(dataMap.get("cpAccountPrefix").toString());
            }
        if (dataMap.containsKey("wipAccountType") && dataMap.get("wipAccountType") != null) {
            String wipAccountType = dataMap.get("wipAccountType").toString();
//            Group group = (Group) get(Group.class, wipAccountType);
            accountsData.setWipAccountTypeId(wipAccountType);
        }
        if (dataMap.containsKey("cpAccountType") && dataMap.get("cpAccountType") != null) {
            String cpAccountType = dataMap.get("cpAccountType").toString();
//            Group group = (Group) get(Group.class, cpAccountType);
            accountsData.setCpAccountTypeId(cpAccountType);
        }
        if (dataMap.containsKey("assetSetingActivation") && dataMap.get("assetSetingActivation") != null) {
            accountsData.setAssetSetingActivation((Boolean) dataMap.get("assetSetingActivation"));
        }
        
        if (dataMap.containsKey("allowToPostOpeningDepreciation") && dataMap.get("allowToPostOpeningDepreciation") != null) {
            accountsData.setAllowToPostOpeningDepreciation((Boolean) dataMap.get("allowToPostOpeningDepreciation"));
        }
        
        if (dataMap.containsKey("profitLossAccountId") && dataMap.get("profitLossAccountId") != null) {
            accountsData.setProfitLossAccountId((String) dataMap.get("profitLossAccountId"));
        }
        if (dataMap.containsKey("openingStockAccountId") && dataMap.get("openingStockAccountId") != null) {
            accountsData.setOpeningStockAccountId((String) dataMap.get("openingStockAccountId"));
        }
        if (dataMap.containsKey("closingStockAccountId") && dataMap.get("closingStockAccountId") != null) {
            accountsData.setClosingStockAccountId((String) dataMap.get("closingStockAccountId"));
        }        
        if (dataMap.containsKey("stockInHandAccountId") && dataMap.get("stockInHandAccountId") != null) {
            accountsData.setStockInHandAccountId((String) dataMap.get("stockInHandAccountId"));
        }
        if (dataMap.containsKey("depreciationCalculationType") && dataMap.get("depreciationCalculationType") != null) {
            accountsData.setAssetDepreciationCalculationType((Integer) dataMap.get("depreciationCalculationType"));
        }
        if (dataMap.containsKey("depreciationCalculationBasedOn") && dataMap.get("depreciationCalculationBasedOn") != null) {
            accountsData.setAssetDepreciationCalculationBasedOn((Integer) dataMap.get("depreciationCalculationBasedOn"));
        }
         if (dataMap.containsKey("activatebudgetingforPR") && dataMap.get("activatebudgetingforPR") != null) {
            accountsData.setActivatebudgetingforPR((Boolean) dataMap.get("activatebudgetingforPR"));
        }
        if (dataMap.containsKey("budgetType") && dataMap.get("budgetType") != null) {
            accountsData.setBudgetType((Integer) dataMap.get("budgetType"));
        }
        if (dataMap.containsKey("budgetFreqType") && dataMap.get("budgetFreqType") != null) {
            accountsData.setBudgetFreqType((Integer) dataMap.get("budgetFreqType"));
        }
        if (dataMap.containsKey("budgetwarnblock") && dataMap.get("budgetwarnblock") != null) {
            accountsData.setBudgetwarnblock((Integer) dataMap.get("budgetwarnblock"));
        }
        if (dataMap.containsKey("UomSchemaType") && dataMap.get("UomSchemaType") != null) {
            int UomSchemaType =Integer.parseInt(dataMap.get("UomSchemaType").toString());
            accountsData.setUomSchemaType(UomSchemaType);
        }
        if (dataMap.containsKey("activateCRMIntegration")) {
            accountsData.setActivateCRMIntegration(dataMap.get("activateCRMIntegration") != null ? (Boolean) dataMap.get("activateCRMIntegration") : true);
        }
        if (dataMap.containsKey("AllowZeroQuantityForDO") && dataMap.get("AllowZeroQuantityForDO") != null) {
            accountsData.setAllowZeroQuantityForProduct((boolean)(dataMap.get("AllowZeroQuantityForDO")));
        }
        if (dataMap.containsKey("AllowZeroQuantityInQuotation") && dataMap.get("AllowZeroQuantityInQuotation") != null) {
            accountsData.setAllowZeroQuantityInQuotation((boolean) dataMap.get("AllowZeroQuantityInQuotation"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInSI") && dataMap.get("AllowZeroQuantityInSI") != null) {
            accountsData.setAllowZeroQuantityInSI((boolean) dataMap.get("AllowZeroQuantityInSI"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInPI") && dataMap.get("AllowZeroQuantityInPI") != null) {
            accountsData.setAllowZeroQuantityInPI((boolean) dataMap.get("AllowZeroQuantityInPI"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInSO") && dataMap.get("AllowZeroQuantityInSO") != null) {
            accountsData.setAllowZeroQuantityInSO((boolean) dataMap.get("AllowZeroQuantityInSO"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInPO") && dataMap.get("AllowZeroQuantityInPO") != null) {
            accountsData.setAllowZeroQuantityInPO((boolean) dataMap.get("AllowZeroQuantityInPO"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInSR") && dataMap.get("AllowZeroQuantityInSR") != null) {
            accountsData.setAllowZeroQuantityInSR((boolean) dataMap.get("AllowZeroQuantityInSR"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInPR") && dataMap.get("AllowZeroQuantityInPR") != null) {
            accountsData.setAllowZeroQuantityInPR((boolean) dataMap.get("AllowZeroQuantityInPR"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInGRO") && dataMap.get("AllowZeroQuantityInGRO") != null) {
            accountsData.setAllowZeroQuantityInGRO((boolean) dataMap.get("AllowZeroQuantityInGRO"));
        }
        if (dataMap.containsKey("AllowZeroQuantityInVQ") && dataMap.get("AllowZeroQuantityInVQ") != null) {
            accountsData.setAllowZeroQuantityInVQ((boolean) dataMap.get("AllowZeroQuantityInVQ"));
        }
        if (dataMap.containsKey("company")) {
            String companyid = (String) dataMap.get("company");
            if (!StringUtil.isNullOrEmpty(companyid)) {
                accountsData.setCompany((Company) get(Company.class, companyid));
            } else {
                accountsData.setCompany(null);
            }
        }

        try {
            saveOrUpdate(accountsData);
        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return accountsData;
    }

    public KwlReturnObject getYearLock(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "", conditionyearid = "";
        String query = "from YearLock y where y.deleted=false ";

        if (filterParams.containsKey("yearid")) {
            conditionyearid += "and y.yearid=? ";
            params.add(filterParams.get("yearid"));
            query += conditionyearid;
        }
        if (filterParams.containsKey("onlylockedyear")) {
            conditionyearid += "and y.isLock= true ";
            query += conditionyearid;
        }
        if (filterParams.containsKey("companyid")) {
            condition += " and y.company.companyID=? order by y.yearid desc";
            params.add(filterParams.get("companyid"));
        }
        query += condition;
//        String query="from YearLock where deleted=false and company.companyID=?";
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    @Override
    public void saveOrUpdateClosingBalanceObj(List<ClosingAccountBalance> closingAccountBalanceList) throws ServiceException {
        saveAll(closingAccountBalanceList);
    }

    @Override
    public void deleteClosingBalance(YearLock yearLock) throws ServiceException {
        List<ClosingAccountBalance> closingAccountBalancesList = find("from ClosingAccountBalance where company.companyID='" + yearLock.getCompany().getCompanyID() + "' and yearLock.ID='" + yearLock.getID() + "'");
        if (!closingAccountBalancesList.isEmpty()) {
            deleteAll(closingAccountBalancesList);
        }
    }
    
   
    @Override
    public KwlReturnObject getClosingBalanceList(YearLock yearlock, int yearId, String companyId) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from ClosingAccountBalance c where c.company.companyID=? ";
        params.add(companyId);
        if (yearlock != null && !StringUtil.isNullOrEmpty(companyId)) {
            condition += " and c.yearLock.ID=? ";
            params.add(yearlock.getID());
        } else if (yearId != 0 && !StringUtil.isNullOrEmpty(companyId)) {
            condition += " and c.yearLock.yearid=? and c.yearLock.isLock=true";
            params.add(yearId);
        }
        if (!StringUtil.isNullOrEmpty(condition)) {
            query += condition;
            returnList = executeQuery( query, params.toArray());
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    @Override
    public KwlReturnObject getClosingBalanceListMinYear(String companyId) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from ClosingAccountBalance c where c.company.companyID=? and c.yearLock.isLock=true ORDER BY c.yearLock.yearid desc";
        params.add(companyId);
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    @Override
    public KwlReturnObject getAccount(String name, String companyId) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        if (!StringUtil.isNullOrEmpty(name) && !StringUtil.isNullOrEmpty(companyId)) {
            String query = "from Account acc where acc.company.companyID=? and acc.name=?";
            params.add(companyId);
            params.add(name);
            returnList = executeQuery( query, params.toArray());
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    @Override
    public KwlReturnObject getAccountObjectById(String id, String companyId) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        if (!StringUtil.isNullOrEmpty(id) && !StringUtil.isNullOrEmpty(companyId)) {
            String query = "from Account acc where acc.company.companyID=? and acc.ID=?";
            params.add(companyId);
            params.add(id);
            returnList = executeQuery( query, params.toArray());
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    public KwlReturnObject getYearLockforPreferences(Map<String, Object> filterParams) throws ServiceException {
        try {
            List returnList = new ArrayList();
            ArrayList params = new ArrayList();
            String query = "from YearLock y where y.yearid between ? and ?  and y.company.companyID=? group by y.yearid order by y.yearid desc";

            if (StringUtil.isNullOrEmpty((String) filterParams.get("previousfiveyears"))) {
                if (filterParams.get("Backfiveyears") == null) {
                    params.add(filterParams.get("CurrentFinancialYear"));
                } else {
                    params.add(filterParams.get("Backfiveyears"));
                }
                params.add(filterParams.get("CurrentServerYear"));

            } else {
                params.add(filterParams.get("Backfiveyears"));
                params.add(filterParams.get("CurrentServerYear"));
            }
            params.add(filterParams.get("companyid"));

            returnList = executeQuery( query, params.toArray());
            return new KwlReturnObject(true, "", null, returnList, returnList.size());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getYearLockforPreferences : " + ex.getMessage(), ex);
        }
    }


//    @Transactional(propagation = Propagation.REQUIRED)
    public void setAccountPreferences(String companyid, HashMap hm, Date curDate) throws ServiceException {
        try {
            Account account = null;
            String query = "from DefaultCompanyAccountPreferences order by ID";
            List list = executeQuery( query);
            Iterator iter = list.iterator();
            Company company = (Company) get(Company.class, companyid);
            while (iter.hasNext()) {
                DefaultCompanyAccountPreferences dcap = (DefaultCompanyAccountPreferences) iter.next();
                CompanyAccountPreferences cap = new CompanyAccountPreferences();
                cap.setBookBeginningFrom(curDate);
                cap.setFinancialYearFrom(curDate);
                cap.setCompany(company);
                String accountid = null;
                if(hm.containsKey(dcap.getCashAccount() == null ? null : dcap.getCashAccount().getID())){
                accountid = (String)hm.get(dcap.getCashAccount().getID());
                account = (Account)get(Account.class, accountid);
//                account = (Account) hm.get(dcap.getCashAccount());
                cap.setCashAccount(account);
                if (account != null) {
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Cash_Account));
                }
                }
                if(hm.containsKey(dcap.getDiscountGiven() == null ? null : dcap.getDiscountGiven().getID())){
                accountid = (String)hm.get(dcap.getDiscountGiven().getID());
                account = (Account)get(Account.class, accountid);
//                account = (Account) hm.get(dcap.getDiscountGiven());
                cap.setDiscountGiven(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Discount_Given));
                }
                }
                if(hm.containsKey(dcap.getDiscountReceived() == null ? null : dcap.getDiscountReceived().getID())){
                accountid = (String)hm.get(dcap.getDiscountReceived().getID());
                account = (Account)get(Account.class, accountid);
//                account = (Account) hm.get(dcap.getDiscountReceived());
                cap.setDiscountReceived(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Discount_Received));
            }
                }
//                cap.setShippingCharges((Account) hm.get(dcap.getShippingCharges()));
                if(hm.containsKey(dcap.getOtherCharges() == null ? null : dcap.getOtherCharges().getID())){
                accountid = (String)hm.get(dcap.getOtherCharges().getID());
                account = (Account)get(Account.class, accountid);
//                account = (Account) hm.get(dcap.getOtherCharges());
//                cap.setOtherCharges(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Other_Charges));
                }
                }
                if(hm.containsKey(dcap.getForeignExchange() == null ? null : dcap.getForeignExchange().getID())){
                accountid = (String)hm.get(dcap.getForeignExchange().getID());
                account = (Account)get(Account.class, accountid);
//                account = (Account) hm.get(dcap.getForeignExchange());
                cap.setForeignexchange(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Foreign_Exchange));
                }}
                if(hm.containsKey(dcap.getDepereciationAccount() == null ? null : dcap.getDepereciationAccount().getID())){
                accountid = (String)hm.get(dcap.getDepereciationAccount().getID());
                account = (Account)get(Account.class, accountid);
//                account = (Account) hm.get(dcap.getDepereciationAccount());
                cap.setDepereciationAccount(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Depreciation_Account));
                }
                }
                if(hm.containsKey(dcap.getRoundingDifference()== null ? null : dcap.getRoundingDifference().getID())){
                accountid = (String)hm.get(dcap.getRoundingDifference().getID());
                account = (Account)get(Account.class, accountid);
//                account = (Account) hm.get(dcap.getRoundingDifference());
                cap.setRoundingDifferenceAccount(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Rounding_Off_Difference));
                }
                }
                if(hm.containsKey(dcap.getSalaryExpense() == null ? null : dcap.getSalaryExpense().getID())){
                accountid = (String)hm.get(dcap.getSalaryExpense().getID());
                account = (Account)get(Account.class, accountid);
//                account = (Account) hm.get(dcap.getSalaryExpense());
                cap.setExpenseAccount(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Salary_Expense_Account));
                }
                }
                if(hm.containsKey(dcap.getSalaryPayable() == null ? null : dcap.getSalaryPayable().getID())){
                accountid = (String)hm.get(dcap.getSalaryPayable().getID());
                account = (Account)get(Account.class, accountid);
//                account = (Account) hm.get(dcap.getSalaryPayable());
                cap.setLiabilityAccount(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Salary_Payable_Account));
                }
                }
                if(hm.containsKey(dcap.getCustomeraccount() == null ? null : dcap.getCustomeraccount().getID())){
                accountid = (String)hm.get(dcap.getCustomeraccount().getID());
                account = (Account)get(Account.class, accountid);
//                account = (Account) hm.get(dcap.getCustomeraccount());        
                cap.setCustomerdefaultaccount(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Customer_Default_Account));
                }
                }
                if(hm.containsKey(dcap.getVendoraccount() == null ? null : dcap.getVendoraccount().getID())){
                accountid = (String)hm.get(dcap.getVendoraccount().getID());
                account = (Account)get(Account.class, accountid);
//                account = (Account) hm.get(dcap.getVendoraccount());        
                cap.setVendordefaultaccount(account);
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Vendor_Default_Account));
                }}
                if(hm.containsKey(dcap.getUnrealisedglaccount() == null ? null : dcap.getUnrealisedglaccount().getID())){
                accountid = (String)hm.get(dcap.getUnrealisedglaccount().getID());
                account = (Account)get(Account.class, accountid);
//                account = (Account) hm.get(dcap.getUnrealisedglaccount());        
                cap.setUnrealisedgainloss(account);   //unrealised gain loss seted default account
                if(account != null){
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Unrealised_Gain_Loss));
                }}
                cap.setDescriptionType("Memo");
                save(cap);
                break;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("setAccountPreferences : " + ex.getMessage(), ex);
        }
    }
    
    @Override
//    @Transactional(propagation = Propagation.REQUIRED)
    public void saveDefaultSequenceFormat(Company company) throws ServiceException {
        SequenceFormat seqFormat = new SequenceFormat();

        // create JE sequence format

        seqFormat = getSequenceFormatObject(company, Constants.Acc_GENERAL_LEDGER_ModuleId, CompanyPreferencesConstants.AUTOJOURNALENTRY, CompanyPreferencesConstants.DEFAULT_JOURNAL_ENTRY_PREFIX, CompanyPreferencesConstants.DEFAULT_JOURNAL_ENTRY_SEQUENCE_FORMAT_NAME);
        save(seqFormat);

        // create CN sequence format

        seqFormat = getSequenceFormatObject(company, Constants.Acc_Credit_Note_ModuleId, CompanyPreferencesConstants.AUTOCREDITMEMO, CompanyPreferencesConstants.DEFAULT_CREDIT_NOTE_PREFIX, CompanyPreferencesConstants.DEFAULT_CREDIT_NOTE_SEQUENCE_FORMAT_NAME);
        save(seqFormat);

        // create DN sequence format

        seqFormat = getSequenceFormatObject(company, Constants.Acc_Debit_Note_ModuleId, CompanyPreferencesConstants.AUTODEBITNOTE, CompanyPreferencesConstants.DEFAULT_DEBIT_NOTE_PREFIX, CompanyPreferencesConstants.DEFAULT_DEBIT_NOTE_SEQUENCE_FORMAT_NAME);
        save(seqFormat);

        seqFormat = getSequenceFormatObject(company, Constants.SALES_BAD_DEBT_CLAIM_ModuleId, CompanyPreferencesConstants.AUTOSALESDEBTCLAIMID, CompanyPreferencesConstants.DEFAULT_SALESBADDEBT_CLAIM_PREFIX, CompanyPreferencesConstants.DEFAULT_SALESBADDEBT_CLAIM_SEQUENCE_FORMAT_NAME);
        save(seqFormat);

        seqFormat = getSequenceFormatObject(company, Constants.SALES_BAD_DEBT_RECOVER_ModuleId, CompanyPreferencesConstants.AUTOSALESDEBTRECOVERID, CompanyPreferencesConstants.DEFAULT_SALESBADDEBT_RECOVER_PREFIX, CompanyPreferencesConstants.DEFAULT_SALESBADDEBT_RECOVER_SEQUENCE_FORMAT_NAME);
        save(seqFormat);

        seqFormat = getSequenceFormatObject(company, Constants.PURCHASE_BAD_DEBT_CLAIM_ModuleId, CompanyPreferencesConstants.AUTOPURCHASEDEBTCLAIMID, CompanyPreferencesConstants.DEFAULT_PURCHASEBADDEBT_CLAIM_PREFIX, CompanyPreferencesConstants.DEFAULT_PURCHASEBADDEBT_CLAIM_SEQUENCE_FORMAT_NAME);
        save(seqFormat);

        seqFormat = getSequenceFormatObject(company, Constants.PURCHASE_BAD_DEBT_RECOVER_ModuleId, CompanyPreferencesConstants.AUTOPURCHASEDEBTRECOVERID, CompanyPreferencesConstants.DEFAULT_PURCHASEBADDEBT_RECOVER_PREFIX, CompanyPreferencesConstants.DEFAULT_PURCHASEBADDEBT_RECOVER_SEQUENCE_FORMAT_NAME);
        save(seqFormat);

        seqFormat = getSequenceFormatObject(company, Constants.Acc_Build_Assembly_Product_ModuleId, CompanyPreferencesConstants.AUTOBUILDASSEMBLY, CompanyPreferencesConstants.DEFAULT_BUILDASSEMBLY_PREFIX, CompanyPreferencesConstants.DEFAULT_BUILDASSEMBLY_SEQUENCE_FORMAT_NAME);
        save(seqFormat);
    }

    public SequenceFormat getSequenceFormatObject(Company company, int moduleId, String moduleName, String prefix, String name) {
        SequenceFormat seqFormat = new SequenceFormat();

        seqFormat.setID(UUID.randomUUID().toString());
        seqFormat.setCompany(company);
        seqFormat.setDeleted(false);
        seqFormat.setModuleid(moduleId);
        if (!StringUtil.isNullOrEmpty(moduleName)) {
            seqFormat.setModulename(moduleName);
        }
        seqFormat.setName(name);
        seqFormat.setPrefix(prefix);
        seqFormat.setNumberofdigit(6);
        seqFormat.setStartfrom(1);
        seqFormat.setSuffix("");
        seqFormat.setShowleadingzero(true);
        seqFormat.setIsdefaultformat(true);
        seqFormat.setIsactivate(true);

        return seqFormat;

    }

    @Override
    public List getYearID(String companyid) throws ServiceException {
        String query = "select yearid from yearlock where deleteflag = 'F' and islock = 'T' and company = ?";
        List list = executeSQLQuery(query, new Object[]{companyid});
        return list;
    }

    @Override
    public List getMappedCompanies(String parentcompanyid) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String query = " select company.companyid, company.companyname, company.subdomain from companymapping "
                + " inner join company on company.companyid = companymapping.childcompanyid where companymapping.companyid = ? and company.deleteflag=false and company.activated='T' ";
        params.add(parentcompanyid);
        returnList = executeSQLQuery( query, params.toArray());
        return returnList;
    }
    
    @Override
    public int getMappedCompaniesCount(String parentcompanyid) throws ServiceException {
        int count=0;
        String query = " select count(company.companyid) from companymapping inner join company on company.companyid = companymapping.childcompanyid"
                + "  where companymapping.companyid = '"+parentcompanyid+"' and company.deleteflag=false and company.activated='T' ";       
        List<BigInteger> list = executeSQLQuery( query);
        if (!list.isEmpty() && list.size() > 0) {
            count = (Integer) list.get(0).intValue();
        }
        return count;
    }
    
    @Override
    public void setNewYear(Date time, Date financialdate, String companyid) throws ServiceException {
        // TODO Auto-generated method stub
        try {
            String query = "Update CompanyAccountPreferences set financialYearFrom=?,firstFinancialYearFrom=? where company.companyID=?";
            executeUpdate( query, new Object[]{time, financialdate, companyid});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("setNewYear : " + ex.getMessage(), ex);
        }
    }

    @Override
    public void setCurrentYear(int presentYear, int previousYear, String companyid) throws ServiceException {
        // TODO Auto-generated method stub
        try {
            String query = "from YearLock y where y.company.companyID=? and y.yearid=?";
            List resultList = executeQuery( query, new Object[]{companyid, presentYear});
            Company company = (Company) get(Company.class, companyid);
            YearLock yearLock = new YearLock();
            if (resultList.size() == 0) {

                yearLock.setCompany(company);
                yearLock.setDeleted(true);
                yearLock.setIsLock(false);
                yearLock.setYearid(presentYear);

                saveOrUpdate(yearLock);

                String query1 = "Update YearLock y set y.deleted=false, y.isLock=false where y.company.companyID=? and y.yearid=?";
                executeUpdate( query1, new Object[]{companyid, previousYear});
            } else {
                String query1 = "Update YearLock y set y.deleted=false,y.isLock=false where y.company.companyID=? and y.yearid=?";
                executeUpdate( query1, new Object[]{companyid, (presentYear + 1)});

                String query2 = "Update YearLock y set y.deleted=true,y.isLock=false where y.company.companyID=? and y.yearid=?";
                executeUpdate( query1, new Object[]{companyid, presentYear});

                String query3 = "Update YearLock y set y.deleted=false,y.isLock=false where y.company.companyID=? and y.yearid=?";
                executeUpdate( query2, new Object[]{companyid, previousYear});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("setCurrentYear : " + ex.getMessage(), ex);
        }
    }

    public boolean saveCompanyPreferencesObj(CompanyAccountPreferences companyAccountPreferencesObj) throws ServiceException {
        boolean success = false;
        try {
            save(companyAccountPreferencesObj);
            success = true;
        } catch (Exception e) {
            throw ServiceException.FAILURE("addPreferences : " + e.getMessage(), e);
        }
        return success;
    }

    @Override
    public KwlReturnObject getTransactionFormsFieldHideShowProperty(int moduleId, String companyId) throws ServiceException {
        List ll = null;
        try {
            List params = new ArrayList();
            params.add(moduleId);
            params.add(companyId);

            String query = "from CustomizeReportMapping crm Where crm.moduleId=? And crm.company.companyID=? And crm.formField=true";
            ll = executeQuery( query, params.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("addPreferences : getTransactionFormsFieldHideShowProperty " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, ll, ll.size());
    }
    //Neeraj D--checking for transaction if present after the first financial year date 

    public KwlReturnObject checktransactionforbookbeginningdate(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            Date transactiondate = (Date) requestParams.get("transactiondate");
            Date originalbookbdate = (Date) requestParams.get("originalbookbdate");
            String companyid = (String) requestParams.get("companyid");

            ArrayList params = new ArrayList();
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);
            params.add(originalbookbdate);
            params.add(transactiondate);

            String mysqlQuery = "select inv.id from invoice inv inner join journalentry je on inv.journalentry=je.id where je.entrydate>=? and je.entrydate<? and inv.company='" + companyid + "'"
                    + " UNION select so.id from salesorder so  where so.orderdate>=? and so.orderdate<? and so.company='" + companyid + "'"
                    + " UNION select dor.id from deliveryorder dor  where dor.orderdate>=? and dor.orderdate<? and dor.company='" + companyid + "'"
                    + " UNION select cn.id from creditnote cn inner join journalentry je on cn.journalentry=je.id where je.entrydate>=? and je.entrydate<? and cn.company='" + companyid + "'"
                    + " UNION select sr.id from salesreturn sr  where sr.orderdate>=? and sr.orderdate<? and sr.company='" + companyid + "'"
                    + " UNION select rp.id from receipt rp inner join journalentry je on rp.journalentry=je.id where je.entrydate>=? and je.entrydate<? and rp.company='" + companyid + "'"
                    + " UNION select qo.id from quotation qo  where qo.quotationdate>=? and qo.quotationdate<? and qo.company='" + companyid + "'"
                    + " UNION select gr.id from goodsreceipt gr inner join journalentry je on gr.journalentry=je.id where je.entrydate>=? and je.entrydate<? and gr.company='" + companyid + "'"
                    + " UNION select po.id from purchaseorder po  where po.orderdate>=? and po.orderdate<? and po.company='" + companyid + "'"
                    + " UNION select gro.id from grorder gro  where gro.grorderdate>=? and gro.grorderdate<? and gro.company='" + companyid + "'"
                    + " UNION select dn.id from debitnote dn inner join journalentry je on dn.journalentry=je.id where je.entrydate>=? and je.entrydate<? and dn.company='" + companyid + "'"
                    + " UNION select pr.id from purchasereturn pr  where pr.orderdate>=? and pr.orderdate<? and pr.company='" + companyid + "'"
                    + " UNION select mp.id from payment mp inner join journalentry je on mp.journalentry=je.id where je.entrydate>=? and je.entrydate<? and mp.company='" + companyid + "'"
                    + " UNION select vq.id from vendorquotation vq  where vq.quotationdate>=? and vq.quotationdate<? and vq.company='" + companyid + "'";

            List list = executeSQLQuery( mysqlQuery, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccounts:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
    
    /**
     * Method to check- Sales trasnactions are present or not.
     * 
     */
    public KwlReturnObject checkSalesSideTransactionPresent(JSONObject requestParamsJson) throws ServiceException {
        KwlReturnObject result;
        try {
            String companyid = requestParamsJson.optString("companyid");
            ArrayList params = new ArrayList();

            String mysqlQuery = "select inv.id,inv.invoicenumber from invoice inv where inv.company= '" + companyid + "'"
                    + " UNION select so.id,so.sonumber from salesorder so  where so.company= '" + companyid + "'"
                    + " UNION select dor.id,dor.donumber from deliveryorder dor  where dor.company= '" + companyid + "'"
                    + " UNION select cn.id,cn.cnnumber from creditnote cn where cn.company= '" + companyid + "'"
                    + " UNION select sr.id,sr.srnumber from salesreturn sr  where sr.company= '" + companyid + "'"
                    + " UNION select qo.id,qo.quotationnumber from quotation qo  where  qo.company= '" + companyid + "'"
                    + " UNION select mp.id,mp.paymentnumber from payment mp where mp.company='" + companyid + "'";

            List list = executeSQLQuery(mysqlQuery, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accCompanyPreferencesImpl.checkSalesSideTransactionPresent:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    /**
     * Method to check- Purchase transaction are present or not.
     * 
     */
    public KwlReturnObject checkPurchaseSideTransactionPresent(JSONObject requestParamsJson) throws ServiceException {
        KwlReturnObject result;
        try {
            String companyid = requestParamsJson.optString("companyid");
            ArrayList params = new ArrayList();

            String mysqlQuery = "select rp.id,rp.receiptnumber from receipt rp where  rp.company= '" + companyid + "'"
                    + " UNION select gr.id,gr.grnumber from goodsreceipt gr where gr.company= '" + companyid + "'"
                    + " UNION select po.id,po.ponumber from purchaseorder po  where  po.company= '" + companyid + "'"
                    + " UNION select gro.id,gro.gronumber from grorder gro  where  gro.company= '" + companyid + "'"
                    + " UNION select dn.id,dn.dnnumber from debitnote dn where dn.company= '" + companyid + "'"
                    + " UNION select pr.id,pr.prnumber from purchasereturn pr  where pr.company= '" + companyid + "'"
                    + " UNION select vq.id,vq.quotationnumber from vendorquotation vq  where vq.company= '" + companyid + "'";

            List list = executeSQLQuery(mysqlQuery, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accCompanyPreferencesImpl.checkPurchaseSideTransactionPresent:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
    //function to determine whether year entry is present in db or not.If not then entry into db else return true 
    public KwlReturnObject checkYearLockpresentindb(Map<String, Object> filterParams) throws ServiceException {
        try {
            List returnList = new ArrayList();
            ArrayList params = new ArrayList();
            String condition = "", conditionyearid = "";
            String query = "select * from yearlock y";

            if (filterParams.containsKey("yearid")) {
                conditionyearid += " where y.yearid=? ";
                params.add(filterParams.get("yearid"));
                query += conditionyearid;
            }

            if (filterParams.containsKey("companyid")) {
                condition += " and y.company=?";
                params.add(filterParams.get("companyid"));
            }
            query += condition;
            returnList = executeSQLQuery( query, params.toArray());
            if (returnList.isEmpty()) {
                int yearid = (Integer) filterParams.get("yearid");
                String companyid = (String) filterParams.get("companyid");
                query = "insert into yearlock (id,yearid,islock,deleteflag,company) values (?,?,?,?,?);";
                executeSQLUpdate( query, new Object[]{UUID.randomUUID().toString(), yearid, 'F', 'F', companyid});
            }
            return new KwlReturnObject(true, "", null, returnList, returnList.size());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveMultiApprovalRule : " + ex.getMessage(), ex);
        }
    }

    @Override
    public SequenceFormat updateSequenceFormat(HashMap<String, Object> dataMap) throws ServiceException, AccountingException {
        SequenceFormat seqFormat = new SequenceFormat();
        ArrayList params = new ArrayList();
        ArrayList params1 = new ArrayList();
        ArrayList params2 = new ArrayList();
        
        // Below module's sequence format are mandatory. For thease module one seuquence format is created with company creation.
        // So for these module always one sequence format required with default value true. 
        Set<Integer> mandatoryDefaultSequenceFormat = new HashSet<>();
        mandatoryDefaultSequenceFormat.add(Constants.Acc_GENERAL_LEDGER_ModuleId);
        mandatoryDefaultSequenceFormat.add(Constants.Acc_Credit_Note_ModuleId);
        mandatoryDefaultSequenceFormat.add(Constants.Acc_Debit_Note_ModuleId);
        mandatoryDefaultSequenceFormat.add(Constants.SALES_BAD_DEBT_CLAIM_ModuleId);
        mandatoryDefaultSequenceFormat.add(Constants.SALES_BAD_DEBT_RECOVER_ModuleId);
        mandatoryDefaultSequenceFormat.add(Constants.PURCHASE_BAD_DEBT_CLAIM_ModuleId);
        mandatoryDefaultSequenceFormat.add(Constants.PURCHASE_BAD_DEBT_RECOVER_ModuleId);
        mandatoryDefaultSequenceFormat.add(Constants.Acc_Build_Assembly_Product_ModuleId);
        
        boolean isdefaultformat = false;
        boolean isChecked = false;
        if (dataMap.containsKey("isdefaultformat")) {
            isdefaultformat = Boolean.parseBoolean(dataMap.get("isdefaultformat").toString());
            if (isdefaultformat == true) {
                params1.add("T");
            } else {
                params1.add("F");
            }
        }
        if (dataMap.containsKey("isChecked")) {
            isChecked = Boolean.parseBoolean(dataMap.get("isChecked").toString());
            if (isChecked == true) {
                params1.add("T");
            } else {
                params1.add("F");
            }
        }
        
        char resetCounter='F';
        if (dataMap.containsKey("resetcounter") && dataMap.get("resetcounter")!=null) {
            if (Boolean.parseBoolean(dataMap.get("resetcounter").toString())) {
                resetCounter = 'T';
            } 
        }
        params1.add(resetCounter);
        if (dataMap.containsKey("companyid")) {
            String companyid = (String) dataMap.get("companyid");
            params.add(companyid);
            params2.add(companyid);
            params1.add(companyid);
        }
        if (dataMap.containsKey("modulename")) {
            String modulename = dataMap.get("modulename").toString();
            params2.add(modulename);
        }
       
        int moduleID = 0;
        if (dataMap.containsKey("moduleid") && dataMap.get("moduleid")!=null) {
            moduleID = (int) dataMap.get("moduleid");
        }
         
        if (dataMap.containsKey("id")) {
            String id = dataMap.get("id").toString();
            params.add(id);
            params1.add(id);
        }
        List returnList = new ArrayList();
        if (isdefaultformat == true) {                  // to set format as default
            //this 1st query is for setting all previous sequence format as default false because there can be only one SF must be default.
            String query = " update sequenceformat sf set sf.isdefaultformat='F' where sf.company = ? and sf.modulename=? ";
            int num = executeSQLUpdate( query, params2.toArray());
            
            //this 2nd query is for u[pdating default SF.
            String query1 = " update sequenceformat sf set sf.isdefaultformat=?, sf.isactivate=?,sf.resetcounter=? where sf.company = ? and sf.id=? ";
            int num1 = executeSQLUpdate( query1, params1.toArray());
        } else {
            if (mandatoryDefaultSequenceFormat.contains(moduleID)) {//For mandatory sequence format at least one default sequence format is required
                String query = " select sf.isdefaultformat from sequenceformat sf where sf.company = ? and sf.id=? and sf.isdefaultformat='T' ";
                returnList = executeSQLQuery(query, params.toArray());
                if (returnList.size() > 0) {                                   // prevent update for default format
                    throw new AccountingException("acc.companypreferences.cannotEditSequenceFormat");
                } else {                                                            // update for non default
                    String query1 = " update sequenceformat sf set sf.isdefaultformat=?, sf.isactivate=?,sf.resetcounter=? where sf.company = ? and sf.id=? ";
                    int num1 = executeSQLUpdate(query1, params1.toArray());
                }
            } else {//For non mandatory sequence format we can set all sequence format as non default
                String query1 = " update sequenceformat sf set sf.isdefaultformat=?, sf.isactivate=?,sf.resetcounter=? where sf.company = ? and sf.id=? ";
                int num1 = executeSQLUpdate(query1, params1.toArray());
            }
        }
        return seqFormat;
    }

    public List checkSequenceFormat(String id, String companyId, String module) throws ServiceException, AccountingException {
        ArrayList params = new ArrayList();
        params.add(id);
        params.add(companyId);
        List l = new ArrayList();
        String query = "select seqformat from " + module + " where seqformat=? and company=?";
        l = executeSQLQuery( query, params.toArray());
        if (l.size() > 0) {
            throw new AccountingException("acc.companypreferences.cannotDeleteSequenceFormat");
        }
        return l;
    }
    public List checkDimensionSequenceFormat(String id, String companyId, String module) throws ServiceException, AccountingException {
        ArrayList params = new ArrayList();
        params.add(id);
        params.add(companyId);
        List l = new ArrayList();
        String query = "select fieldcombodata.seqformat from " + module + " Inner Join fieldparams on fieldparams.id=fieldcombodata.fieldid "
                     + "where fieldcombodata.seqformat=? and fieldparams.companyid=?";
        l = executeSQLQuery( query, params.toArray());
        if (l.size() > 0) {
            throw new AccountingException("acc.companypreferences.cannotDeleteSequenceFormat");
        }
        return l;
    }

    @Override
    public KwlReturnObject getAllCompanyUsersEmailIds(String companyid) throws ServiceException{
       ArrayList params = new ArrayList();
       params.add(companyid );
       List l = new ArrayList();
       String query= "select emailid from users where company= ?";
       l = executeSQLQuery( query, params.toArray());
       return new KwlReturnObject(true, "", null, l, l.size());
    }
    @Override
    public KwlReturnObject getCompanyAdminUsersEmailIds(String loginUserId,String companyid) throws ServiceException{
       ArrayList params = new ArrayList();
       params.add(companyid );
       params.add(loginUserId );
       String query= "select u.emailid from users u left join role_user_mapping rum on u.userid=rum.userId where u.company= ? and ( rum.roleId = 1 or u.userid = ? ) ";
       List list = executeSQLQuery( query, params.toArray());
       return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject setUserActiveDays(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            UserActiveDaysDetails uadetails = new UserActiveDaysDetails();

            if (dataMap.containsKey("userID") && dataMap.get("userID") != null) {
                User user = dataMap.get("userID") == null ? null : (User) get(User.class, (String) dataMap.get("userID"));
                uadetails.setUser(user);
            }
            if (dataMap.containsKey("activeDays") && dataMap.get("activeDays") != null) {
                uadetails.setActiveDays((Integer) dataMap.get("activeDays"));
            }
            if (dataMap.containsKey("moduleID") && dataMap.get("moduleID") != null) {
                uadetails.setModuleID((Integer) dataMap.get("moduleID"));
            }
            if (dataMap.containsKey("companyID")) {
                Company company = dataMap.get("companyID") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyID"));
                uadetails.setCompany(company);
            }
            if (dataMap.containsKey("isAllUser")) {
                uadetails.setAllUser((Boolean) dataMap.get("isAllUser"));
            }
            saveOrUpdate(uadetails);
            list.add(uadetails);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.setUserActiveDays : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getUserActiveDaysDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List returnList = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            String condition = "";
            params.add((String) dataMap.get("companyID"));

            if (dataMap.containsKey("isAllUser") && dataMap.get("isAllUser") != null) {
                params.add((Boolean) dataMap.get("isAllUser"));
                condition += " and allUser = ? ";
            } else if (dataMap.containsKey("userID") && dataMap.get("userID") != null) {
                params.add((String) dataMap.get("userID"));
                condition += " and user.userID = ? ";
            }

            if (dataMap.containsKey("moduleID") && dataMap.get("moduleID") != null) {
                params.add((Integer) dataMap.get("moduleID"));
                condition += " and moduleID = ? ";
            }

            String query = "from UserActiveDaysDetails where company.companyID = ?" + condition;
            returnList = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.getUserActiveDaysDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    @Override
    public KwlReturnObject deleteUserActiveDaysDetails(String userID, boolean isAllUser, String companyID) throws ServiceException {
        int numRows = 0;
        try {
            ArrayList params = new ArrayList();
            params.add(companyID);
            String condition = "";

            if (isAllUser) {
                condition += " and allUser = ?";
                params.add(isAllUser);
            } else {
                condition += " and user.userID = ?";
                params.add(userID);
            }
            String delQuery = "delete from UserActiveDaysDetails where company.companyID = ? " + condition;
            numRows = executeUpdate( delQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.deleteUserActiveDaysDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "User Active Days Details has been deleted successfully.", null, null, numRows);
    }
        public KwlReturnObject getCustomerFromPreferences(String accountid,String companyid) throws ServiceException{
       ArrayList params = new ArrayList();
       params.add(accountid );
       params.add(companyid );
       List l = new ArrayList();
       String query= "select customerForPOS from extracompanypreferences where customerForPOS= ? and id=?";
       l = executeSQLQuery( query, params.toArray());
       return new KwlReturnObject(true, "", null, l, l.size());
    }
        
    @Override
    public boolean getDepreciationCount(String companyID) {
        boolean freez = false;
        int count = 0;
        ArrayList params = new ArrayList();
        params.add(companyID);
        try {
            String selQuery = "Select count(ID) from assetdepreciationdetail where company = ? ";
            List<BigInteger> list = executeSQLQuery( selQuery, params.toArray());
            if (!list.isEmpty() && list.size() > 0) {
                count = (Integer) list.get(0).intValue();
            }
            if (count > 0) {
                freez = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return freez;
    }
    
    @Override
    public boolean getopeningDepreciationPostedCount(String companyID) {
        boolean freez = false;
        int count = 0;
        ArrayList params = new ArrayList();
        params.add(companyID);
        try {
            String selQuery = "Select count(ID) from assetdetail where openingdepreciation > 0 and company = ? ";
            List<BigInteger> list = executeSQLQuery( selQuery, params.toArray());
            if (!list.isEmpty() && list.size() > 0) {
                count = (Integer) list.get(0).intValue();
            }
            if (count > 0) {
                freez = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return freez;
    }
    
    private KwlReturnObject EnableDisableQAFeatureOnInventorySide(String companyID,boolean isEnable,String userId) throws ServiceException {
        int numRows = 0;
        try {
            ArrayList params = new ArrayList();
            String getInvConfigId = " SELECT id from in_inventoryconfig WHERE company= ? ";
            params.add(companyID);
            List res1 = executeSQLQuery( getInvConfigId, params.toArray());
            
            if (res1 != null && !res1.isEmpty()) {
                String invConfigId = (String) res1.get(0);
                ArrayList params1 = new ArrayList();
                String UpdateQry = "UPDATE in_inventoryconfig SET enable_stockadj_approvalflow= ?, enable_stockreq_approvalflow=?, enable_ist_return_approvalflow=?,enable_sr_return_approvalflow =?,enable_stockout_approvalflow=? WHERE id =? ";
                if (isEnable) {
                    params1.add(1);
                    params1.add(1);
                    params1.add(1);
                    params1.add(1);
                    params1.add(1);
                } else {
                    params1.add(0);
                    params1.add(0);
                    params1.add(0);
                    params1.add(0);
                    params1.add(0);
                }
                params1.add(invConfigId);
                //due to dependancy problem we have used sql queries over here
                numRows = executeSQLUpdate( UpdateQry, params1.toArray());

            }else{
                String insertInvConfigQry=" INSERT INTO in_inventoryconfig (id,company,createdby,modifiedby,createdon,modifiedon,negative_inventory_check,stock_update_batchtype,"
                        + " enable_stockadj_approvalflow,enable_stockreq_approvalflow,enable_stockout_approvalflow,enable_ist_return_approvalflow,enable_sr_return_approvalflow)"
                        + " VALUES (UUID(),?,?,?,NOW(),NOW(),0,0,?,?,?,?,?) ";
                ArrayList params1 = new ArrayList();
                params1.add(companyID);
                params1.add(userId);
                params1.add(userId);
                if (isEnable) {
                    params1.add(1);
                    params1.add(1);
                    params1.add(1);
                    params1.add(1);
                    params1.add(1);
                } else {
                    params1.add(0);
                    params1.add(0);
                    params1.add(0);
                    params1.add(0);
                    params1.add(0);
                }
                numRows = executeSQLUpdate( insertInvConfigQry, params1.toArray());
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.EnableDisableQAFeatureOnInventorySide : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Inventory config updated successfully.", null, null, numRows);
    }
    public KwlReturnObject getCompanyList(String[] subdomainArray) throws ServiceException {
        List list = new ArrayList();
        String sqlQuery = "select companyid from company where deleteflag=0";
//        if(!StringUtil.isNullOrEmpty(subdomain)){
//            sqlQuery+=" and subdomain like '"+subdomain+"'";
//        }
        String subdomain="";
        if (subdomainArray != null) {
            for (int i = 0; i < subdomainArray.length; i++) {
                subdomain += " " + "'" + subdomainArray[i] + "'" + " ,";
            }
            subdomain = subdomain.substring(0, subdomain.length() - 2);
            sqlQuery += " and subdomain in (" + subdomain + ")";
        }
        sqlQuery+=" order by createdon desc";
        list = executeSQLQuery( sqlQuery);
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveCompanyAddressDetails(HashMap<String, Object> addressParams) throws ServiceException {
        List list = new ArrayList();
        try {
            CompanyAddressDetails addresses = new CompanyAddressDetails();

            if (addressParams.containsKey("addressid")) {
                String addressid = (String) addressParams.get("addressid");
                if (!StringUtil.isNullOrEmpty(addressid)) {
                    addresses = (CompanyAddressDetails) get(CompanyAddressDetails.class, addressid);
                }
            }
            if (addressParams.containsKey("aliasName")) {
                addresses.setAliasName((String) addressParams.get("aliasName"));
            }
            if (addressParams.containsKey("address")) {
                addresses.setAddress((String) addressParams.get("address"));
            }
            if (addressParams.containsKey("county")) {
                addresses.setCounty((String) addressParams.get("county"));
            }
            if (addressParams.containsKey("city")) {
                addresses.setCity((String) addressParams.get("city"));
            }
            if (addressParams.containsKey("state")) {
                addresses.setState((String) addressParams.get("state"));
            }
            if (addressParams.containsKey("country")) {
                addresses.setCountry((String) addressParams.get("country"));
            }
            if (addressParams.containsKey("postalCode")) {
                addresses.setPostalCode((String) addressParams.get("postalCode"));
            }
            if (addressParams.containsKey("phone")) {
                addresses.setPhone((String) addressParams.get("phone"));
            }
            if (addressParams.containsKey("mobileNumber")) {
                addresses.setMobileNumber((String) addressParams.get("mobileNumber"));
            }
            if (addressParams.containsKey("fax")) {
                addresses.setFax((String) addressParams.get("fax"));
            }
            if (addressParams.containsKey("emailID")) {
                addresses.setEmailID((String) addressParams.get("emailID"));
            }
            if (addressParams.containsKey("contactPerson")) {
                addresses.setContactPerson((String) addressParams.get("contactPerson"));
            }
            if (addressParams.containsKey("recipientName")) {
                addresses.setRecipientName((String) addressParams.get("recipientName"));
            }
            if (addressParams.containsKey("contactPersonNumber")) {
                addresses.setContactPersonNumber((String) addressParams.get("contactPersonNumber"));
            }
            if (addressParams.containsKey("contactPersonDesignation")) {
                addresses.setContactPersonDesignation((String) addressParams.get("contactPersonDesignation"));
            }
            if (addressParams.containsKey("website")) {
                addresses.setWebsite((String) addressParams.get("website"));
            }
            if (addressParams.containsKey("isBillingAddress")) {
                addresses.setIsBillingAddress(Boolean.parseBoolean((String) addressParams.get("isBillingAddress").toString()));
            }
            if (addressParams.containsKey("isDefaultAddress")) {
                addresses.setIsDefaultAddress(Boolean.parseBoolean((String) addressParams.get("isDefaultAddress").toString()));
            }
            if (addressParams.containsKey("companyid")) {
                addresses.setCompany((Company) get(Company.class, (String) addressParams.get("companyid")));
            }
            saveOrUpdate(addresses);
            list.add(addresses);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "Address field added successfully.", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteCompanyAddressDetails(String companyid) throws ServiceException {
        int numRows = 0;
        try {
            String delQuery = "delete from CompanyAddressDetails cad where cad.company.companyID=?";
            numRows += executeUpdate( delQuery, new Object[]{companyid});
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, null, numRows);
    }
//
//    @Override
//    public KwlReturnObject getCompanyAddressDetails(HashMap<String, Object> requestParams) throws ServiceException {
//               int listSize = 0;
//        List list = new ArrayList();
//        try {
//            String condition="";
//            boolean isdefault=false;
//            List params = new ArrayList();           
//            String companyid= (String) requestParams.get(Constants.companyKey);
//            params.add(companyid);
//            String query = "from CompanyAddressDetails cad where cad.company.companyID=? ";
//            if(requestParams.containsKey("isDefaultAddress")){
//                isdefault=Boolean.parseBoolean(requestParams.get("isDefaultAddress").toString());
//                if(isdefault){
//                    condition+="and cad.isDefaultAddress= 'T' ";
//                }
//            }
//            if(requestParams.containsKey("isBillingAddress")){
//               boolean isBillingAddress=Boolean.parseBoolean(requestParams.get("isBillingAddress").toString());
//                if(isBillingAddress){
//                    condition+="and cad.isBillingAddress= 'T' ";
//                } else {
//                    condition+="and cad.isBillingAddress= 'F' ";
//                }
//            }
//            query+=condition;
//            list = executeQuery( query, params.toArray());
//            if (list != null) {
//                listSize = list.size();
//            }
//
//        } catch (Exception ex) {
//            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            return new KwlReturnObject(true, "", null, list, listSize);
//        }
//    }

    @Override
    public KwlReturnObject getExtraPreferencesFromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from ExtraCompanyPreferences acp where (writeOffReceiptAccount=? or writeOffAccount=? or profitLossAccountId = ? or openingStockAccountId = ? or closingStockAccountId = ? or stockInHandAccountId = ?) and acp.company.companyID=?";
        list = executeQuery( q, new Object[]{accountid, accountid, accountid, accountid, accountid, accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject checkOpeningTransactionsForFirstFinancialYearDate(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            Date transactiondate = (Date) requestParams.get("transactiondate");
            String companyid = (String) requestParams.get("companyid");

            ArrayList params = new ArrayList();
            params.add(transactiondate);
            params.add(transactiondate);
            params.add(transactiondate);
            params.add(transactiondate);
            params.add(transactiondate);
            params.add(transactiondate);
            params.add(transactiondate);
            params.add(transactiondate);

            String mysqlQuery = "select inv.id from invoice inv where inv.creationdate>? and inv.isopeningbalenceinvoice=1 and inv.company='" + companyid + "'"
                    + " UNION select so.id from salesorder so  where so.orderdate>? and so.isopeningbalenceso=1 and so.company='" + companyid + "'"
                    + " UNION select cn.id from creditnote cn where cn.creationdate>? and cn.isopeningbalencecn=1 and cn.company='" + companyid + "'"
                    + " UNION select rp.id from receipt rp where rp.creationdate>? and rp.isopeningbalencereceipt=1 and rp.company='" + companyid + "'"
                    + " UNION select gr.id from goodsreceipt gr where gr.creationdate>? and gr.isopeningbalenceinvoice=1 and gr.company='" + companyid + "'"
                    + " UNION select po.id from purchaseorder po  where po.orderdate>? and po.isopeningbalencepo=1 and po.company='" + companyid + "'"
                    + " UNION select dn.id from debitnote dn where dn.creationdate>? and dn.isopeningbalencedn=1 and dn.company='" + companyid + "'"
                    + " UNION select mp.id from payment mp where mp.creationdate>? and mp.isopeningbalencepayment=1 and mp.company='" + companyid + "'";
            List list = executeSQLQuery( mysqlQuery, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accCompanyPreferencesImpl.checkOpeningTransactionsForFirstFinancialYearDate:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
    
    @Override
    public JSONObject updateOpeningTransactionDates(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject retJObj = new JSONObject();
        try {
            Date bbdate = (Date) requestParams.get("bbdate");
            Date transactiondate = (Date) requestParams.get("transactiondate");
            String companyid = (String) requestParams.get("companyid");
            DateFormat df = requestParams.get("dateformat")!=null?(DateFormat)requestParams.get("dateformat"): authHandler.getDateOnlyFormat();
            ArrayList selParams = new ArrayList();
            selParams.add(bbdate);
            
            ArrayList params = new ArrayList();
//            params.add(transactiondate);
            
            String selQuery="select inv.id, inv.invoicenumber,termid,customer from invoice inv where inv.creationdate>? and inv.isopeningbalenceinvoice=1 and inv.company='" + companyid + "'";
            List<Object[]> list = executeSQLQuery( selQuery, selParams.toArray());
            if(list.size()>0){
                JSONArray jarr = new  JSONArray();
                for (Object[] obj : list) {
                    JSONObject jobj = new JSONObject();
                    String documentId = obj[0].toString();
                    String documentNo = obj[1].toString();
                    jobj.put("documentId", documentId);
                    jobj.put("documentNo", documentNo);
                    String termid = obj[2] != null ? obj[2].toString() : "";
                    String custid = obj[3] != null ? obj[3].toString() : "";
                    StringBuilder updateDueDateQuery = new StringBuilder();
                    params = new ArrayList();
                    params.add(transactiondate);
                    if (!StringUtil.isNullOrEmpty(termid)) {
                        String query = "select termdays from creditterm where company =? and termid=?";
                        List termDaysList = executeSQLQuery(query, new Object[]{companyid,termid});
                        if (termDaysList != null && !termDaysList.isEmpty()) {
                            int termDays = termDaysList.get(0) != null ? (int) termDaysList.get(0) : 0;
                            Calendar c = Calendar.getInstance();
                            c.setTime(transactiondate);
                            c.add(Calendar.DATE, termDays);  // number of days to add
                            Date newDueDate = df.parse(df.format(c.getTime()));
                            updateDueDateQuery.append(", duedate = ?");
                            params.add(newDueDate);
                        }
                    } else if (!StringUtil.isNullOrEmpty(custid)) {
                        String query = "select termdays from creditterm ct inner join customer c on c.creditTerm=ct.termid where ct.company = ? and c.id=? ";
                        List termDaysList = executeSQLQuery(query, new Object[]{companyid, custid});
                        if (termDaysList != null && !termDaysList.isEmpty()) {
                            int termDays = termDaysList.get(0) != null ? (int) termDaysList.get(0) : 0;
                            Calendar c = Calendar.getInstance();
                            c.setTime(transactiondate);
                            c.add(Calendar.DATE, termDays);  // number of days to add
                            Date newDueDate = df.parse(df.format(c.getTime()));
                            updateDueDateQuery.append(", duedate = ?");
                            params.add(newDueDate);
                        }
                    }
                    updateDueDateQuery.append(", porefdate = ?");
                    params.add(transactiondate);
                    jarr.put(jobj);
                    String mysqlQuery = "UPDATE invoice SET creationdate=? "+updateDueDateQuery.toString()+" WHERE id='" + documentId + "'";
                    executeSQLUpdate( mysqlQuery,params.toArray());
                }
                retJObj.put("openingSI",jarr);         
            }
            
            selQuery="select so.id, so.sonumber,term,customer from salesorder so  where so.orderdate>? and so.isopeningbalenceso=1 and so.company='" + companyid + "'";
            list = executeSQLQuery( selQuery, selParams.toArray());
            if(list.size()>0){
                JSONArray jarr = new  JSONArray();
                for (Object[] obj : list) {
                    JSONObject jobj = new JSONObject();
                    String documentId = obj[0].toString();
                    String documentNo = obj[1].toString();
                    jobj.put("documentId", documentId);
                    jobj.put("documentNo", documentNo);
                    String termid = obj[2] != null ? obj[2].toString() : "";
                    String custid = obj[3] != null ? obj[3].toString() : "";
                    StringBuilder updateDueDateQuery = new StringBuilder();
                    params = new ArrayList();
                    params.add(transactiondate);
                    if (!StringUtil.isNullOrEmpty(termid)) {
                        String query = "select termdays from creditterm where company =? and termid=?";
                        List termDaysList = executeSQLQuery(query, new Object[]{companyid,termid});
                        if (termDaysList != null && !termDaysList.isEmpty()) {
                            int termDays = termDaysList.get(0) != null ? (int) termDaysList.get(0) : 0;
                            Calendar c = Calendar.getInstance();
                            c.setTime(transactiondate);
                            c.add(Calendar.DATE, termDays);  // number of days to add
                            Date newDueDate = df.parse(df.format(c.getTime()));
                            updateDueDateQuery.append(", duedate = ?");
                            params.add(newDueDate);
                        }
                    } else if (!StringUtil.isNullOrEmpty(custid)) {
                        String query = "select termdays from creditterm ct inner join customer c on c.creditTerm=ct.termid where ct.company = ? and c.id=? ";
                        List termDaysList = executeSQLQuery(query, new Object[]{companyid, custid});
                        if (termDaysList != null && !termDaysList.isEmpty()) {
                            int termDays = termDaysList.get(0) != null ? (int) termDaysList.get(0) : 0;
                            Calendar c = Calendar.getInstance();
                            c.setTime(transactiondate);
                            c.add(Calendar.DATE, termDays);  // number of days to add
                            Date newDueDate = df.parse(df.format(c.getTime()));
                            updateDueDateQuery.append(", duedate = ?");
                            params.add(newDueDate);
                        }
                    }
                    jarr.put(jobj);
                    String mysqlQuery = "UPDATE salesorder SET orderdate=? "+updateDueDateQuery.toString()+" WHERE id='" + documentId + "'";
                    executeSQLUpdate( mysqlQuery,params.toArray());
                }
                retJObj.put("openingSO",jarr); 
            }
            
            selQuery="select cn.id, cn.cnnumber from creditnote cn where cn.creationdate>? and cn.isopeningbalencecn=1 and cn.company='" + companyid + "'";
            list = executeSQLQuery( selQuery, selParams.toArray());
            if(list.size()>0){
                params = new ArrayList();
                params.add(transactiondate);
                JSONArray jarr = new  JSONArray();
                for (Object[] obj : list) {
                    JSONObject jobj = new JSONObject();
                    String documentId = obj[0].toString();
                    String documentNo = obj[1].toString();
                    jobj.put("documentId", documentId);
                    jobj.put("documentNo", documentNo);
                    jarr.put(jobj);
                    String mysqlQuery = "UPDATE creditnote SET creationdate=? WHERE id='" + documentId + "'";
                    executeSQLUpdate( mysqlQuery,params.toArray());
                }
                retJObj.put("openingCN",jarr); 
            }
            
            selQuery="select rp.id, rp.receiptnumber from receipt rp where rp.creationdate>? and rp.isopeningbalencereceipt=1 and rp.company='" + companyid + "'";
            list = executeSQLQuery( selQuery, selParams.toArray());
            if(list.size()>0){
                params = new ArrayList();
                params.add(transactiondate);
                JSONArray jarr = new  JSONArray();
                for (Object[] obj : list) {
                    JSONObject jobj = new JSONObject();
                    String documentId = obj[0].toString();
                    String documentNo = obj[1].toString();
                    jobj.put("documentId", documentId);
                    jobj.put("documentNo", documentNo);
                    jarr.put(jobj);
                    String mysqlQuery = "UPDATE receipt SET creationdate=? WHERE id='" + documentId + "'";
                    executeSQLUpdate( mysqlQuery,params.toArray());
                }
                retJObj.put("openingReceipt",jarr);
            }
            
            selQuery="select gr.id, gr.grnumber,termid,vendor from goodsreceipt gr where gr.creationdate>? and gr.isopeningbalenceinvoice=1 and gr.company='" + companyid + "'";
            list = executeSQLQuery( selQuery, selParams.toArray());
            if(list.size()>0){
                JSONArray jarr = new  JSONArray();
                for (Object[] obj : list) {
                    JSONObject jobj = new JSONObject();
                    String documentId = obj[0].toString();
                    String documentNo = obj[1].toString();
                    jobj.put("documentId", documentId);
                    jobj.put("documentNo", documentNo);
                    jarr.put(jobj);
                    String termid = obj[2] != null ? obj[2].toString() : "";
                    String venid = obj[3] != null ? obj[3].toString() : "";
                    StringBuilder updateDueDateQuery = new StringBuilder();
                    params = new ArrayList();
                    params.add(transactiondate);
                    if (!StringUtil.isNullOrEmpty(termid)) {
                        String query = "select termdays from creditterm where company =? and termid=?";
                        List termDaysList = executeSQLQuery(query, new Object[]{companyid, termid});
                        if (termDaysList != null && !termDaysList.isEmpty()) {
                            int termDays = termDaysList.get(0) != null ? (int) termDaysList.get(0) : 0;
                            Calendar c = Calendar.getInstance();
                            c.setTime(transactiondate);
                            c.add(Calendar.DATE, termDays);  // number of days to add
                            Date newDueDate = df.parse(df.format(c.getTime()));
                            updateDueDateQuery.append(", duedate = ?");
                            params.add(newDueDate);
                        }
                    } else if (!StringUtil.isNullOrEmpty(venid)) {
                        String query = "select termdays from creditterm ct inner join vendor v on v.debitTerm=ct.termid where ct.company = ? and v.id=? ";
                        List termDaysList = executeSQLQuery(query, new Object[]{companyid, venid});
                        if (termDaysList != null && !termDaysList.isEmpty()) {
                            int termDays = termDaysList.get(0) != null ? (int) termDaysList.get(0) : 0;
                            Calendar c = Calendar.getInstance();
                            c.setTime(transactiondate);
                            c.add(Calendar.DATE, termDays);  // number of days to add
                            Date newDueDate = df.parse(df.format(c.getTime()));
                            updateDueDateQuery.append(", duedate = ?");
                            params.add(newDueDate);
                        }
                    }
                    updateDueDateQuery.append(", partyinvoicedate=?");
                    params.add(transactiondate);
                    String mysqlQuery = "UPDATE goodsreceipt SET creationdate=? "+updateDueDateQuery.toString()+" WHERE id='" + documentId + "'";
                    executeSQLUpdate( mysqlQuery,params.toArray());
                }
                retJObj.put("openingPI",jarr);
            }
            
            selQuery="select po.id, po.ponumber,term,vendor from purchaseorder po  where po.orderdate>? and po.isopeningbalencepo=1 and po.company='" + companyid + "'";
            list = executeSQLQuery( selQuery, selParams.toArray());
            if(list.size()>0){
                JSONArray jarr = new  JSONArray();
                for (Object[] obj : list) {
                    JSONObject jobj = new JSONObject();
                    String documentId = obj[0].toString();
                    String documentNo = obj[1].toString();
                    jobj.put("documentId", documentId);
                    jobj.put("documentNo", documentNo);
                    String termid = obj[2] != null ? obj[2].toString() : "";
                    String venid = obj[3] != null ? obj[3].toString() : "";
                    StringBuilder updateDueDateQuery = new StringBuilder();
                    params = new ArrayList();
                    params.add(transactiondate);
                    if (!StringUtil.isNullOrEmpty(termid)) {
                        String query = "select termdays from creditterm where company =? and termid=?";
                        List termDaysList = executeSQLQuery(query, new Object[]{companyid, termid});
                        if (termDaysList != null && !termDaysList.isEmpty()) {
                            int termDays = termDaysList.get(0) != null ? (int) termDaysList.get(0) : 0;
                            Calendar c = Calendar.getInstance();
                            c.setTime(transactiondate);
                            c.add(Calendar.DATE, termDays);  // number of days to add
                            Date newDueDate = df.parse(df.format(c.getTime()));
                            updateDueDateQuery.append(", duedate = ?");
                            params.add(newDueDate);
                        }
                    } else if (!StringUtil.isNullOrEmpty(venid)) {
                        String query = "select termdays from creditterm ct inner join vendor v on v.debitTerm=ct.termid where ct.company = ? and v.id=? ";
                        List termDaysList = executeSQLQuery(query, new Object[]{companyid, venid});
                        if (termDaysList != null && !termDaysList.isEmpty()) {
                            int termDays = termDaysList.get(0) != null ? (int) termDaysList.get(0) : 0;
                            Calendar c = Calendar.getInstance();
                            c.setTime(transactiondate);
                            c.add(Calendar.DATE, termDays);  // number of days to add
                            Date newDueDate = df.parse(df.format(c.getTime()));
                            updateDueDateQuery.append(", duedate = ?");
                            params.add(newDueDate);
                        }
                    }
                    jarr.put(jobj);
                    String mysqlQuery = "UPDATE purchaseorder SET orderdate=? "+updateDueDateQuery.toString()+" WHERE id='" + documentId + "'";
                    executeSQLUpdate( mysqlQuery,params.toArray());
                }
                retJObj.put("openingPO",jarr);
            }
            
            selQuery="select dn.id, dn.dnnumber from debitnote dn where dn.creationdate>? and dn.isopeningbalencedn=1 and dn.company='" + companyid + "'";
            list = executeSQLQuery( selQuery, selParams.toArray());
            if(list.size()>0){
                JSONArray jarr = new  JSONArray();
                params = new ArrayList();
                params.add(transactiondate);
                for (Object[] obj : list) {
                    JSONObject jobj = new JSONObject();
                    String documentId = obj[0].toString();
                    String documentNo = obj[1].toString();
                    jobj.put("documentId", documentId);
                    jobj.put("documentNo", documentNo);
                    jarr.put(jobj);
                    String mysqlQuery = "UPDATE debitnote SET creationdate=? WHERE id='" + documentId + "'";
                    executeSQLUpdate( mysqlQuery,params.toArray());
                }
                retJObj.put("openingDN",jarr);
            }
            
            selQuery="select mp.id, mp.paymentnumber from payment mp where mp.creationdate>? and mp.isopeningbalencepayment=1 and mp.company='" + companyid + "'";
            list = executeSQLQuery( selQuery, selParams.toArray());
            if(list.size()>0){
                JSONArray jarr = new  JSONArray();
                params = new ArrayList();
                params.add(transactiondate);
                for (Object[] obj : list) {
                    JSONObject jobj = new JSONObject();
                    String documentId = obj[0].toString();
                    String documentNo = obj[1].toString();
                    jobj.put("documentId", documentId);
                    jobj.put("documentNo", documentNo);
                    jarr.put(jobj);
                    String mysqlQuery = "UPDATE payment SET creationdate=? WHERE id='" + documentId + "'";
                    executeSQLUpdate( mysqlQuery,params.toArray());
                }
                retJObj.put("openingPayment",jarr);
            }
            return retJObj;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl:updateOpeningTransactionDates()" + ex.getMessage(), ex);
        }
    }
    
    @Override
    public boolean isCompanyActivated(String companyID) throws ServiceException {
        boolean result = false;
        try {
            String sql = "SELECT activated FROM Company WHERE companyID = ?";
            List list = executeQuery(sql, new Object[]{companyID});
            if (list.size() > 0 && list.get(0).equals(true)) {
                result = true;
            }
        } catch (ServiceException e) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("Service exception in isCompanyActivated()", e);
        }
        return result;
    }
    
    @Override
    public List isCompanyExistWithCompanyID(String companyID) throws ServiceException {
        List list = new ArrayList();
        try{
                String sql = "SELECT COUNT(companyID) AS count FROM Company WHERE companyID = ?";
            	list = executeQuery(sql, new Object[]{companyID});
        
        } catch (ServiceException e) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("Service exception in isCompanyExistWithCompanyID()", e);
        }
        return list;
    }
   
    @Override
    public List isCompanyExistWithSubDomain(String subdomain) throws ServiceException {
        List list = new ArrayList();
        try{
                String sql = "SELECT COUNT(companyID) AS count FROM Company WHERE subDomain = ?";
            	list = executeQuery(sql, new Object[]{subdomain});
        
        } catch (ServiceException e) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("Service exception in isCompanyExistWithSubDomain()", e);
        }
        return list;
    }
    
    @Override
    public List isAnotherCompanyExistWithSameSubDomain(String subdomain, String companyID) throws ServiceException {
        List list = new ArrayList();
        try{
            String query = "from Company where subDomain = ? and companyID <> ?";
            list = executeQuery(query, new Object[]{subdomain, companyID});
        
        } catch (ServiceException e) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("Service exception in isCompanyExistWithSubDomain()", e);
        }
        return list;
    }
    
    @Override
    public void deleteCompanyData(String subdomain) throws ServiceException {
        try{
            Map map = new HashMap();
            map.put("dbname", "");
            map.put("subdomain", subdomain);
            String query = "call deletecompanydata(:dbname,:subdomain)";
//             executeNativeUpdate(query,new Object[]{"",subdomain});
             executeSQLQuery(query, null, map);   
        } catch (Exception e) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("Service exception in isCompanyExistWithSubDomain()", e);
        }
    }
    
    public List getExtraCompanyPreferencesForMalaysia() throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "from ExtraCompanyPreferences where company.country.ID=?";
            list = executeQuery(query, new Object[]{"137"});
        } catch (Exception e) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return list;
    }
    @Override
    public boolean isMRPModuleActivated(String companyID) throws ServiceException {
        boolean result = false;
        try {
            String sql = "SELECT activateMRPModule FROM ExtraCompanyPreferences WHERE company.companyID = ?";
            List list = executeQuery(sql, new Object[]{companyID});
            if (list.size() > 0 && list.get(0).equals(true)) {
                result = true;
            }
        } catch (ServiceException e) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return result;
    }
    
    @Override
    public KwlReturnObject addReportToWidgetView(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        try {
            WidgetReportMaster wrm = new WidgetReportMaster();

            if (requestParams.containsKey("company")) {
                wrm.setCompany((Company) get(Company.class, requestParams.get("company").toString()));
            }

            if (requestParams.containsKey("reportid")) {
                wrm.setReport((ReportMaster) get(ReportMaster.class, requestParams.get("reportid").toString()));
            }
            saveOrUpdate(wrm);
            ll.add(wrm);
        } catch (Exception e) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.addReportToWidgetView " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, ll, ll.size());
    }
    
    @Override
    public KwlReturnObject removeReportFromWidgetView(HashMap<String, Object> requestParams) throws ServiceException {
        List<WidgetReportMaster> ll = new ArrayList<>();
        try {
            String companyid ="";
            if (requestParams.containsKey("company")) {
                companyid = requestParams.get("company").toString();
            }
            String reportid ="";
            if (requestParams.containsKey("reportid")) {
                reportid = requestParams.get("reportid").toString();
            }
            
            String query = "from WidgetReportMaster where report.ID = ? and company.companyID = ?";
            ll = executeQuery(query, new Object[]{reportid, companyid});
            
            if(ll.size()>0){
                for (WidgetReportMaster wrm : ll) {
                    delete(wrm);
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.removeReportFromWidgetView " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, ll, ll.size());
    }
    
    /**
     * @param requestParams  Map should include parameters required by this
     * function
     * @description Function to check whether any transaction is made which has 
     * affected inventory/stock
     * @return KwlReturnObject This will return the total count and list of transaction IDS which will affect inventory 
     */
    @Override
    public KwlReturnObject checkTransactionsForManufacturingModule(Map<String, Object> requestParams){
        KwlReturnObject result;
        try {
            String companyid = (String) requestParams.get("companyid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(companyid);
            params.add(companyid);
            params.add(companyid);
            params.add(companyid);
            params.add(companyid);
            params.add(companyid);
            String mysqlQuery = "select inv.id from invoice inv inner join journalentry je on inv.journalentry=je.id where inv.company=?"
                    + " UNION select dor.id from deliveryorder dor  where dor.company=?"
                    + " UNION select sr.id from salesreturn sr  where sr.company=?"
                    + " UNION select gr.id from goodsreceipt gr inner join journalentry je on gr.journalentry=je.id where gr.company=?"
                    + " UNION select gro.id from grorder gro  where gro.company=?"
                    + " UNION select pr.id from purchasereturn pr where pr.company=?"
                    + " UNION select p.id from product p  where p.company=?";

            List list = executeSQLQuery(mysqlQuery, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accCompanyPreferencesImpl.checkTransactionsForManufacturingModule:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
    
    @Override
       public Map<String,Object> getTermSummary(Map<String, Object> requestMap) throws ServiceException {
        JSONArray jSONArray = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        Map<String, Object> seqNumberMap = new HashMap<>();
        try {
            String companyId = "";
            int termtype = 0;
            
            if (requestMap.containsKey("companyId")) {
                companyId = requestMap.get("companyId").toString();
            }
            if (requestMap.containsKey("termtype")) {
                termtype = Integer.parseInt(requestMap.get("termtype").toString());
            }
            //Check whether it is Used In Product.
            String productTermsMapQuery = " FROM ProductTermsMap ptm WHERE ptm.isDefault='T' AND ptm.product.company.companyID = ? AND ptm.term.termType= ? GROUP BY ptm.term";
            List productTermsMaplist = executeQuery(productTermsMapQuery, new Object[]{companyId,termtype});
            seqNumberMap.put("productTermsMaplist", productTermsMaplist);
            
            //Check whether it is Used In Goods Receipt.
            String goodsReceiptDetailTermsMapQuery = " FROM ReceiptDetailTermsMap rdtm WHERE rdtm.goodsreceiptdetail.company.companyID = ? AND rdtm.term.termType= ? GROUP BY rdtm.term";
            List goodsReceiptDetailTermsMaplist = executeQuery(goodsReceiptDetailTermsMapQuery, new Object[]{companyId,termtype});
            seqNumberMap.put("goodsReceiptDetailTermsMaplist", goodsReceiptDetailTermsMaplist);
            
            //Check whether it is Used In Invoice.
            String invoiceDetailTermsMapQuery = " FROM InvoiceDetailTermsMap idtm WHERE idtm.invoicedetail.company.companyID = ? AND idtm.term.termType= ? GROUP BY idtm.term";
            List InvoiceDetailTermsMaplist = executeQuery(invoiceDetailTermsMapQuery, new Object[]{companyId,termtype});
            seqNumberMap.put("InvoiceDetailTermsMaplist", InvoiceDetailTermsMaplist);
            
            //Check whether it is Used In GoodsReceiptOrder.
            String goodsReceiptOrderDetailTermMapQuery = " FROM ReceiptOrderDetailTermMap rodtm WHERE rodtm.grodetail.company.companyID = ? AND rodtm.term.termType= ? GROUP BY rodtm.term";
            List goodsReceiptOrderDetailTermMaplist = executeQuery(goodsReceiptOrderDetailTermMapQuery, new Object[]{companyId,termtype});
            seqNumberMap.put("goodsReceiptOrderDetailTermMaplist", goodsReceiptOrderDetailTermMaplist);
            
            //Check whether it is Used In DeliveryOrder.
            String deliveryOrderDetailTermMapQuery = " FROM DeliveryOrderDetailTermMap dodtm WHERE dodtm.dodetail.company.companyID = ? AND dodtm.term.termType= ? GROUP BY dodtm.term";
            List deliveryOrderDetailTermMaplist = executeQuery(deliveryOrderDetailTermMapQuery, new Object[]{companyId,termtype});
            seqNumberMap.put("deliveryOrderDetailTermMaplist", deliveryOrderDetailTermMaplist);
            
            //Check whether it is Used In Purchase Order.
            String purchaseOrderDetailTermMapQuery = " FROM PurchaseOrderDetailsTermMap podtm WHERE podtm.podetails.company.companyID = ? AND podtm.term.termType= ? GROUP BY podtm.term";
            List purchaseOrderDetailTermMaplist = executeQuery(purchaseOrderDetailTermMapQuery, new Object[]{companyId,termtype});
            seqNumberMap.put("purchaseOrderDetailTermMaplist", purchaseOrderDetailTermMaplist);
            
            //Check whether it is Used In Sales Order.
            String salesOrderDetailTermMapQuery = " FROM SalesOrderDetailTermMap sodtm WHERE sodtm.salesOrderDetail.company.companyID = ? AND sodtm.term.termType= ? GROUP BY sodtm.term";
            List salesOrderDetailTermMaplist = executeQuery(salesOrderDetailTermMapQuery, new Object[]{companyId,termtype});
            seqNumberMap.put("salesOrderDetailTermMaplist", salesOrderDetailTermMaplist);
            
            //Check whether it is Used In Purchase Return.
            String purchaseReturnDetailTermMapQuery = " FROM PurchaseReturnDetailsTermMap prdtm WHERE prdtm.purchasereturndetail.company.companyID = ? AND prdtm.term.termType= ? GROUP BY prdtm.term";
            List purchaseReturnDetailTermMaplist = executeQuery(purchaseReturnDetailTermMapQuery, new Object[]{companyId,termtype});
            seqNumberMap.put("purchaseReturnDetailTermMaplist", purchaseReturnDetailTermMaplist);
            
            //Check whether it is Used In Sales Return.
            String salesReturnDetailTermMapQuery = " FROM SalesReturnDetailsTermMap srdtm WHERE srdtm.salesreturndetail.company.companyID = ? AND srdtm.term.termType= ? GROUP BY srdtm.term";
            List salesReturnDetailTermMaplist = executeQuery(salesReturnDetailTermMapQuery, new Object[]{companyId,termtype});
            seqNumberMap.put("salesReturnDetailTermMaplist", salesReturnDetailTermMaplist);
            
            //Check whether it is Used In Vendor Quotation.
            String vendorQuotationDetailsTermMapQuery = " FROM VendorQuotationDetailsTermMap vqdtm WHERE vqdtm.vendorquotationdetails.company.companyID = ? AND vqdtm.term.termType= ? GROUP BY vqdtm.term";
            List vendorQuotationDetailsTermMaplist = executeQuery(vendorQuotationDetailsTermMapQuery, new Object[]{companyId,termtype});
            seqNumberMap.put("vendorQuotationDetailsTermMaplist", vendorQuotationDetailsTermMaplist);
            
            //Check whether it is Used In Customer Quotation.
            String quotationDetailTermMapQuery = " FROM QuotationDetailTermMap cqdtm WHERE cqdtm.quotationDetail.company.companyID = ? AND cqdtm.term.termType= ? GROUP BY cqdtm.term";
            List quotationDetailTermMaplist = executeQuery(quotationDetailTermMapQuery, new Object[]{companyId,termtype});
            seqNumberMap.put("quotationDetailTermMaplist", quotationDetailTermMaplist);
            
        } catch (NumberFormatException | ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return seqNumberMap;
    }
    /**
     * Following Function used for-;;;
     * While Editing Account In Company Preferences for Line level terms/ tax check if this term/ tax used in Transaction/ Document if used user can not change term/tax account,
     * if not used user can change account
     * @param requestMap
     */
    @Override
       public Map<String,Object> getTermUsedIn(Map<String, Object> requestMap) throws ServiceException {
        Map<String, Object> seqNumberMap = new HashMap<>();
        try {
            String companyId = "";
            String termid = "";
            
            if (requestMap.containsKey("companyId")) {
                companyId = requestMap.get("companyId").toString();
            }
            if (requestMap.containsKey("termid")) {
                termid = requestMap.get("termid").toString();
            }
            //Check whether term is Used In Product.
            /*String productTermsMapQuery = " FROM ProductTermsMap ptm WHERE ptm.isDefault='T' AND ptm.product.company.companyID = ? AND term= ? ";
            List productTermsMaplist = executeQuery(productTermsMapQuery, new Object[]{companyId,termid});
            seqNumberMap.put("productTermsMaplist", productTermsMaplist);*/
            
            //Check whether term is Used In Goods Receipt.
            String goodsReceiptDetailTermsMapQuery = " FROM ReceiptDetailTermsMap rdtm WHERE rdtm.goodsreceiptdetail.company.companyID = ? AND rdtm.term.id= ? ";
            List goodsReceiptDetailTermsMaplist = executeQuery(goodsReceiptDetailTermsMapQuery, new Object[]{companyId,termid});
            seqNumberMap.put("goodsReceiptDetailTermsMaplist", goodsReceiptDetailTermsMaplist);
            
            //Check whether term is Used In Invoice.
            String invoiceDetailTermsMapQuery = " FROM InvoiceDetailTermsMap idtm WHERE idtm.invoicedetail.company.companyID = ? AND idtm.term.id= ? ";
            List InvoiceDetailTermsMaplist = executeQuery(invoiceDetailTermsMapQuery, new Object[]{companyId,termid});
            seqNumberMap.put("InvoiceDetailTermsMaplist", InvoiceDetailTermsMaplist);
            
            //Check whether term is Used In GoodsReceiptOrder.
            String goodsReceiptOrderDetailTermMapQuery = " FROM ReceiptOrderDetailTermMap rodtm WHERE rodtm.grodetail.company.companyID = ? AND rodtm.term.id= ? ";
            List goodsReceiptOrderDetailTermMaplist = executeQuery(goodsReceiptOrderDetailTermMapQuery, new Object[]{companyId,termid});
            seqNumberMap.put("goodsReceiptOrderDetailTermMaplist", goodsReceiptOrderDetailTermMaplist);
            
            //Check whether term is Used In DeliveryOrder.
            String deliveryOrderDetailTermMapQuery = " FROM DeliveryOrderDetailTermMap dodtm WHERE dodtm.dodetail.company.companyID = ? AND dodtm.term.id= ? ";
            List deliveryOrderDetailTermMaplist = executeQuery(deliveryOrderDetailTermMapQuery, new Object[]{companyId,termid});
            seqNumberMap.put("deliveryOrderDetailTermMaplist", deliveryOrderDetailTermMaplist);
            
            //Check whether term is Used In Purchase Order.
            String purchaseOrderDetailTermMapQuery = " FROM PurchaseOrderDetailsTermMap podtm WHERE podtm.podetails.company.companyID = ? AND podtm.term.id= ? ";
            List purchaseOrderDetailTermMaplist = executeQuery(purchaseOrderDetailTermMapQuery, new Object[]{companyId,termid});
            seqNumberMap.put("purchaseOrderDetailTermMaplist", purchaseOrderDetailTermMaplist);
            
            //Check whether term is Used In Sales Order.
            String salesOrderDetailTermMapQuery = " FROM SalesOrderDetailTermMap sodtm WHERE sodtm.salesOrderDetail.company.companyID = ? AND sodtm.term.id= ? ";
            List salesOrderDetailTermMaplist = executeQuery(salesOrderDetailTermMapQuery, new Object[]{companyId,termid});
            seqNumberMap.put("salesOrderDetailTermMaplist", salesOrderDetailTermMaplist);
            
            //Check whether term is Used In Purchase Return.
            String purchaseReturnDetailTermMapQuery = " FROM PurchaseReturnDetailsTermMap prdtm WHERE prdtm.purchasereturndetail.company.companyID = ? AND prdtm.term.id= ? ";
            List purchaseReturnDetailTermMaplist = executeQuery(purchaseReturnDetailTermMapQuery, new Object[]{companyId,termid});
            seqNumberMap.put("purchaseReturnDetailTermMaplist", purchaseReturnDetailTermMaplist);
            
            //Check whether term is Used In Sales Return.
            String salesReturnDetailTermMapQuery = " FROM SalesReturnDetailsTermMap srdtm WHERE srdtm.salesreturndetail.company.companyID = ? AND srdtm.term.id= ? ";
            List salesReturnDetailTermMaplist = executeQuery(salesReturnDetailTermMapQuery, new Object[]{companyId,termid});
            seqNumberMap.put("salesReturnDetailTermMaplist", salesReturnDetailTermMaplist);
            
            //Check whether term is Used In Vendor Quotation.
            String vendorQuotationDetailsTermMapQuery = " FROM VendorQuotationDetailsTermMap vqdtm WHERE vqdtm.vendorquotationdetails.company.companyID = ? AND vqdtm.term.id= ? ";
            List vendorQuotationDetailsTermMaplist = executeQuery(vendorQuotationDetailsTermMapQuery, new Object[]{companyId,termid});
            seqNumberMap.put("vendorQuotationDetailsTermMaplist", vendorQuotationDetailsTermMaplist);
            
            //Check whether term is Used In Customer Quotation.
            String quotationDetailTermMapQuery = " FROM QuotationDetailTermMap cqdtm WHERE cqdtm.quotationDetail.company.companyID = ? AND cqdtm.term.id= ? ";
            List quotationDetailTermMaplist = executeQuery(quotationDetailTermMapQuery, new Object[]{companyId,termid});
            seqNumberMap.put("quotationDetailTermMaplist", quotationDetailTermMaplist);
            
        } catch (NumberFormatException | ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return seqNumberMap;
    }
    @Override
       public Map<String,Object> getTDSSummary(Map<String, Object> requestMap) throws ServiceException {
        JSONArray jSONArray = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        Map<String, Object> seqNumberMap = new HashMap<>();
        try {
            String companyId = "";
            int termtype = 0;
            
            if (requestMap.containsKey("companyId")) {
                companyId = requestMap.get("companyId").toString();
            }
            if (requestMap.containsKey("termtype") && !StringUtil.isNullOrEmpty((String)requestMap.get("termtype"))) {
                termtype = Integer.parseInt(requestMap.get("termtype").toString());
            }
            //Check whether it is Used In Product.
            String TDSDetailsQuery = " FROM TdsDetails tdsd WHERE tdsd.company.companyID = ? ";
            List TDSMaplist = executeQuery(TDSDetailsQuery, new Object[]{companyId});
            seqNumberMap.put("TDSMaplist", TDSMaplist);
            
            
        } catch (NumberFormatException | ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return seqNumberMap;
    }
    @Override
    public KwlReturnObject isTDSUsedInTransactions(Map<String, Object> requestParams) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyId = "";
            if (requestParams.containsKey("companyId")) {
                companyId = (String)requestParams.get("companyId");
            }
            //Once Goods Receipts are created, don't allow user to uncheck TDS Flow.
            String query = "from GoodsReceipt gr where gr.deleted=false AND gr.company.companyID=? ";
            list = executeQuery( query, companyId);
            count = list.size();
        } catch (Exception ex) {
             Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }   
    @Override
    public boolean isExciseApplicable(String companyID) throws ServiceException {
        boolean result = false;
        try {
            String sql = "SELECT exciseApplicable FROM ExtraCompanyPreferences WHERE id = ?";
            List list = executeQuery(sql, new Object[]{companyID});
            if (list.size() > 0 && list.get(0).equals(true)) {
                result = true;
            }
        } catch (ServiceException e) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("Service exception in isExciseApplicable()", e);
        }
        return result;
    }

    @Override
    public int getCountryID(String companyID) throws ServiceException {
        int result = 0;
        try {
            String sql = "SELECT country FROM company WHERE companyid = ?";
            List list = executeSQLQuery(sql, new Object[]{companyID});
            if (list != null && list.size() > 0) {
                String countryID = StringUtil.isNullOrEmpty((String) list.get(0)) ? "0" : (String) list.get(0);
                result = Integer.parseInt(countryID);
            }
        } catch (ServiceException e) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("Service exception in getCountryID()", e);
        }
        return result;
    }
    
    /**
     * @param requestParams  HashMap should include parameters required by this
     * method
     * @description DAO implementation method addRemoveFavouriteReport which 
     * add or remove report in or from  favourite list respectively
     * @return KwlReturnObject This will return the total count and list of reports 
     */
    @Override
     public KwlReturnObject addRemoveFavouriteReport(HashMap<String, Object> requestParams) throws ServiceException{
         List<FavouriteReportMaster> ll = new ArrayList<>();
         try{
             FavouriteReportMaster favouriteReportMaster = null;
             List list;
             KwlReturnObject result;
             ArrayList params = new ArrayList();
             boolean favourite=false;
            favourite = Boolean.parseBoolean(requestParams.get("isfavourite").toString());
            if(favourite)
            {
            result = getFavouriteReportMasterObject(requestParams);
             list = result.getEntityList();
              if (list != null && list.size() > 0) {
                favouriteReportMaster = (FavouriteReportMaster) list.get(0);
              }
              else {
                favouriteReportMaster = new FavouriteReportMaster();
              }
              if (requestParams.containsKey("userid")) {
                favouriteReportMaster.setUser((User) get(User.class, requestParams.get("userid").toString()));
                
            }
            if (requestParams.containsKey("company")) {
                favouriteReportMaster.setCompany((Company) get(Company.class, requestParams.get("company").toString()));
            }
            if (requestParams.containsKey("reportid")) {
                favouriteReportMaster.setReport((ReportMaster) get(ReportMaster.class, requestParams.get("reportid").toString()));
            }

            if (requestParams.containsKey("isfavourite")) {
                favouriteReportMaster.setFavourite(Boolean.parseBoolean(requestParams.get("isfavourite").toString()));
            }
            saveOrUpdate(favouriteReportMaster);
            ll.add(favouriteReportMaster);
            }
            else
            {
                 if (requestParams.containsKey("reportid")) {
                params.add(requestParams.get("reportid").toString());
            }
            if (requestParams.containsKey("company")) {;
                params.add(requestParams.get("company").toString());
            }
            if (requestParams.containsKey("userid")) {
                params.add(requestParams.get("userid").toString());
            }
            String query = "from FavouriteReportMaster where report.ID = ? and company.companyID = ? and user.userID = ?";
            ll = executeQuery(query, params.toArray());
            if(ll.size()>0){
                for (FavouriteReportMaster frm : ll) {
                    delete(frm);
                }
            }
            }
              
         }catch (Exception e) {System.out.println(e);
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.addRemoveFavouriteReport " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, ll, ll.size()); 
         
    }
    
     /**
     * @param requestParams  Map should include parameters required by this
     * method
     * @description DAO implementation method get FavouriteReportMasterObject which 
     * retrieve an existing instance of FavouriteReportMaster
     * @return KwlReturnObject This will return the total count and list of reports based on requestParams 
     */
    @Override
    public KwlReturnObject getFavouriteReportMasterObject(Map<String, Object> requestParams) throws ServiceException {
        String sql = "from FavouriteReportMaster where report.ID = ? and company.companyID = ? and user.userID = ?";
        String userid = requestParams.get("userid").toString();
        String company = requestParams.get("company").toString();
        String report = requestParams.get("reportid").toString();
        List list = executeQuery(sql, new Object[]{report, company, userid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getCompanyList(Map<String, Object> filterParams) throws ServiceException {
        List list = new ArrayList();
        String sqlQuery = "select companyid from company where deleteflag=0";
        String[] subdomain=filterParams.get("subdomains")!=null?(String[]) filterParams.get("subdomains"):null;
        String country=filterParams.get("country")!=null?(String)filterParams.get("country"):"";
        String subdomains="";
        if (subdomain != null) {
            for (int i = 0; i < subdomain.length; i++) {
                subdomains += " " + "'" + subdomain[i] + "'" + " ,";
            }
            subdomains = subdomains.substring(0, subdomains.length() - 2);
            sqlQuery += " and subdomain in (" + subdomains + ")";
        }
        if(!StringUtil.isNullOrEmpty(country)){
            sqlQuery += " and country = "+country+" ";
        }
        sqlQuery+=" order by createdon desc";
        list = executeSQLQuery( sqlQuery);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
   

    /**
     * This method is used to delete the YearEndCheckList using the YearLock ID
     * and Company ID.
     * @param id
     * @param companyid
     * @throws ServiceException
     */
    @Override
    public void deleteYearEndCheckList(String id, String companyid) throws ServiceException {
        List<YearEndCheckList> yearEndCheckList = find("from YearEndCheckList where company.companyID='" + companyid + "' and id ='" + id + "'");
        if (yearEndCheckList != null && !yearEndCheckList.isEmpty()) {
            deleteAll(yearEndCheckList);
        }
    }

    /**
     * This method is used to add the YearEndCheckList for the YearLock.
     * @param checkListJSON
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject addYearEndCheckList(JSONObject checkListJSON) throws ServiceException {
        List list = new ArrayList();
        try {
            YearEndCheckList yearEndCheckList = new YearEndCheckList();
            if (!StringUtil.isNullOrEmpty(checkListJSON.optString("yearlock"))) {
                yearEndCheckList.setYearlock((YearLock) get(YearLock.class, checkListJSON.optString("yearlock")));
            }
            if (!StringUtil.isNullOrEmpty(checkListJSON.optString("companyid"))) {
                yearEndCheckList.setCompany((Company) get(Company.class, checkListJSON.optString("companyid")));
            }
            yearEndCheckList.setDocumentRevaluationCompleted(checkListJSON.optBoolean(Constants.CHECKLIST_DOCUMENT_REVALUATION_COMPLETED, false));
            yearEndCheckList.setAdjustmentForTransactionCompleted(checkListJSON.optBoolean(Constants.CHECKLIST_ADJUSTMENT_FOR_TRANSACTIONS_COMPLETED, false));
            yearEndCheckList.setInventoryAdjustmentCompleted(checkListJSON.optBoolean(Constants.CHECKLIST_INVENTORY_ADJUSTMENT_COMPLETED, false));
            yearEndCheckList.setAssetDepreciationPosted(checkListJSON.optBoolean(Constants.CHECKLIST_ASSET_DEPRECIATION_COMPLETED, false));
            saveOrUpdate(yearEndCheckList);
            list.add(yearEndCheckList);
        } catch (ServiceException e) {
            throw ServiceException.FAILURE("addYearEndCheckList : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Year End Checklist has been added successfully", null, list, list.size());
    }

    /**
     * This method is used to get the YearEndCheckList using the YearLock Id. 
     * @param id
     * @return
     */
    @Override
    public YearEndCheckList getYearEndCheckList(String id) {
        YearEndCheckList yearEndCheckList = (YearEndCheckList) get(YearEndCheckList.class, id);
        return yearEndCheckList;
    }
    
    @Override
    public KwlReturnObject getAccountUsedForFreeGift(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from ExtraCompanyPreferences acp where (freegiftjeaccount = ?) and acp.company.companyID=?";
        list = executeQuery( q, new Object[]{accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * This method is used to get the max year lock id. 
     * @param companyid
     * @param year
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject getMaxYearLockDetails(String companyid, int year) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            list = executeSQLQuery("select yl2.id from yearlock yl2 where yl2.yearid = (select max(yl.yearid) from yearlock yl inner join company c on c.companyid = yl.company where c.companyid = ? and yl2.company = yl.company and yl.yearid <= ?  and yl.islock = 'T') and yl2.company = ? ", new Object[]{companyid, year, companyid});
        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesDAO.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw ServiceException.FAILURE("getMaxYearLockDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    /**
     * This method is used to get the list of ClosingAccountBalance.
     * @param requestJSON
     * @return
     */
    public List<ClosingAccountBalance> getClosingAccountBalance(JSONObject requestJSON) {
        List<ClosingAccountBalance> closingAccountBalances = new ArrayList<>();
        List params = new ArrayList();
        StringBuilder hql = new StringBuilder();
        try {
            hql.append("from ClosingAccountBalance ");
            if (!StringUtil.isNullOrEmpty(requestJSON.optString(Constants.companyKey))) {
                if (hql.indexOf("where") >= 0) {
                    hql.append(" and company.companyID = ? ");
                } else {
                    hql.append(" where company.companyID = ? ");
                }
                params.add(requestJSON.optString(Constants.companyKey));
            }
            if (!StringUtil.isNullOrEmpty(requestJSON.optString("yearlockid"))) {
                if (hql.indexOf("where") >= 0) {
                    hql.append(" and yearLock.ID = ? ");
                } else {
                    hql.append(" where yearLock.ID = ? ");
                }
                params.add(requestJSON.optString("yearlockid"));
            }
            if (!StringUtil.isNullOrEmpty(requestJSON.optString("yearid"))) {
                if (hql.indexOf("where") >= 0) {
                    hql.append(" and yearId = ? ");
                } else {
                    hql.append(" where yearId = ? ");
                }
                params.add(requestJSON.getInt("yearid"));
            }
            if (!StringUtil.isNullOrEmpty(requestJSON.optString("stockInHand"))) {
                if (hql.indexOf("where") >= 0) {
                    hql.append(" and stockInHand = ? ");
                } else {
                    hql.append(" where stockInHand = ? ");
                }
                params.add(requestJSON.optBoolean("stockInHand"));
            }
            if (!StringUtil.isNullOrEmpty(requestJSON.optString("netProfitAndLossWithStock"))) {
                if (hql.indexOf("where") >= 0) {
                    hql.append(" and netProfitAndLossWithStock = ? ");
                } else {
                    hql.append(" where netProfitAndLossWithStock = ? ");
                }
                params.add(requestJSON.optBoolean("netProfitAndLossWithStock"));
            }
            if (!StringUtil.isNullOrEmpty(requestJSON.optString("netProfitAndLossWithOutStock"))) {
                if (hql.indexOf("where") >= 0) {
                    hql.append(" and netProfitAndLossWithOutStock = ? ");
                } else {
                    hql.append(" where netProfitAndLossWithOutStock = ? ");
                }
                params.add(requestJSON.optBoolean("netProfitAndLossWithOutStock"));
            }
            closingAccountBalances = executeQuery(hql.toString(), params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        return closingAccountBalances;
    }
    
    /**
     * Method is used to check is there any record present in closingaccountbalance for the company.
     * @param jsonObject
     * @return
     */
    @Override
    public boolean isBookClose(JSONObject jsonObject) {
        boolean isBookClose = false;
        try {
            StringBuilder queryBuilder = new StringBuilder();
            List params = new ArrayList();
            queryBuilder.append("select count(*) from yearlock where company = ? and islock = 'T'");
            params.add(jsonObject.optString(Constants.companyKey));
            if (!StringUtil.isNullOrEmpty(jsonObject.optString("yearid"))) {
                int yearid = jsonObject.optInt("yearid");
                if (!StringUtil.isNullOrEmpty(jsonObject.optString("gt")) && jsonObject.optBoolean("gt", false)) { /*Check for greater year*/
                    queryBuilder.append(" and yearid > ? ");
                    params.add(yearid);
                } else {
                    /* Check if the year id closed or not */
                    queryBuilder.append(" and yearid = ? ");
                    params.add(yearid);
                }
            }
            List resultList = executeSQLQuery(queryBuilder.toString(), params.toArray());
            if (resultList != null && !resultList.isEmpty()) {
                Iterator closingBalanceCountIterator = resultList.iterator();
                if (closingBalanceCountIterator.hasNext()) {
                    int closingEntriesCnt = Integer.parseInt(closingBalanceCountIterator.next().toString());
                    if (closingEntriesCnt > 0) {
                        isBookClose = true;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        return isBookClose;
    }
    
    /* Function is used to get Max sequence no generated based on following parameters
     Bank account 
     company 
     sequence Format
     */
    @Override
    public KwlReturnObject getMaxChequeSequenceNumber(HashMap hm) throws ServiceException {
        String query = "select Max(sequencenumber) from cheque ch "
                + "WHERE ch.company=? and (ch.createdfrom=1 or ch.createdfrom=3) and ch.deleteflag=false and ch.bankaccount=?";
        ArrayList params = new ArrayList();
        String companyId = (String) hm.get("companyId");
        String bankAccountId = (String) hm.get("bankAccountId");
        params.add(companyId);
        params.add(bankAccountId);
        if(hm.containsKey("sequenceformatid") && hm.get("sequenceformatid")!=null && !StringUtil.isNullOrEmpty(hm.get("sequenceformatid").toString())){
            String sequenceformatid = hm.get("sequenceformatid").toString();
            query+=" and ch.seqformat=?";
            params.add(sequenceformatid);
        }
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    /**
     * Method used to get the Full Name for company creator using companyId.
     * @param companyID
     * @return Creator Full Name
     */
    @Override
    public String getCreatorFullName(String companyID) {
        String creatorFullName = "" ;
        try {
            String sql = "select u.fname,u.lname from users u, company c where c.companyid = u.company and u.userid = c.creator and c.companyid = ? ";
            List result = executeSQLQuery(sql, new Object[]{companyID});
            if (result != null && !result.isEmpty()) {
                Object[] userArr = (Object[]) result.get(0);
                String fname = userArr[0] != null ? (String) userArr[0] : "";
                String lname = userArr[1] != null ? (String) userArr[1] : "";
                creatorFullName = (StringUtil.isNullOrEmpty(fname) ? "" : fname).concat(" ").concat((StringUtil.isNullOrEmpty(lname) ? "" : lname));
            }
        } catch (Exception ex) {

        }
        return creatorFullName;
    }
    
    /**
     * 
     * @param requestParams
     * @return
     * @throws ServiceException
     * This method gives the all transaction which matches with pattern of sequence format.
     */
    public JSONArray checkTransactionmatchedwithSequenceFormat(HashMap requestParams) throws ServiceException {
        List ll = new ArrayList();
        JSONObject paramObj = new JSONObject();
        JSONObject jObj = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        try {
            String prefix = "", suffix = "", selecteddateformatBeforePrefix = "";
            String formatName="",dateFormatAfterPrefix = "", selecteddateformatSuffix = "", Dateformatinprefix = "", companyid = "", EntryNumber = "";
            int intPartValue = 0, intStartFromValue = 0, moduleid = 0, startfrom = 1;
            int numberofdigit = 0;
            boolean isDateAfterSuffix = false, isDateAfterPrefix = false, isDateBeforePrefix = false, showleadingzero = false;

            if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null) {
                moduleid = (int) requestParams.get("moduleid");
            }
            if (requestParams.containsKey("name") && requestParams.get("name") != null) {
                EntryNumber = (String) requestParams.get("name");
            }
            if (requestParams.containsKey("startfrom") && requestParams.get("startfrom") != null) {
                startfrom = (int) requestParams.get("startfrom");
            }
            if (requestParams.containsKey("numberofdigit") && requestParams.get("numberofdigit") != null) {
                numberofdigit = (int) requestParams.get("numberofdigit");
            }
            if (requestParams.containsKey("prefix") && requestParams.get("prefix") != null) {
                prefix = (String) requestParams.get("prefix");
            }
            if (requestParams.containsKey("suffix") && requestParams.get("suffix") != null) {
                suffix = (String) requestParams.get("suffix");
            }
            if (requestParams.containsKey("showleadingzero") && requestParams.get("showleadingzero") != null) {
                showleadingzero = (boolean) requestParams.get("showleadingzero");
            }
            if (requestParams.containsKey("isshowdateinprefix") && requestParams.get("isshowdateinprefix") != null) {
                isDateBeforePrefix = (boolean) requestParams.get("isshowdateinprefix");//Show Date Before Prefix
            }
            if (requestParams.containsKey("dateFormatinPrefix") && requestParams.get("dateFormatinPrefix") != null) {
                Dateformatinprefix = (String) requestParams.get("dateFormatinPrefix");//Show Date Before Prefix
            }
            if (requestParams.containsKey("isshowdateafterprefix") && requestParams.get("isshowdateafterprefix") != null) {
                isDateAfterPrefix = (boolean) requestParams.get("isshowdateafterprefix");//Show Date After Prefix:
            }
            if (requestParams.containsKey("dateFormatAfterPrefix") && requestParams.get("dateFormatAfterPrefix") != null) {
                dateFormatAfterPrefix = (String) requestParams.get("dateFormatAfterPrefix");//Show Date After Prefix:
            }
            if (requestParams.containsKey("showdateaftersuffix") && requestParams.get("showdateaftersuffix") != null) {
                isDateAfterSuffix = (boolean) requestParams.get("showdateaftersuffix");//Show Date After Suffix:
            }
            if (requestParams.containsKey("selectedsuffixdateformat") && requestParams.get("selectedsuffixdateformat") != null) {
                selecteddateformatSuffix = (String) requestParams.get("selectedsuffixdateformat");//Show Date After Suffix:
            }
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = (String) requestParams.get("companyid");
            }
            if (isDateBeforePrefix) {
                selecteddateformatBeforePrefix = Dateformatinprefix != null ? Dateformatinprefix : "";
            }
            if (isDateAfterPrefix) {
                dateFormatAfterPrefix = dateFormatAfterPrefix != null ? dateFormatAfterPrefix : "";
            }
            if (isDateAfterSuffix) {
                selecteddateformatSuffix = selecteddateformatSuffix != null ? selecteddateformatSuffix : "";
            }
            String datePrefix = "";
            String dateSuffix = "";
            String dateAfterPrefix = "";
            Date creationDate = Calendar.getInstance().getTime();
            if (isDateAfterSuffix || isDateAfterPrefix || isDateBeforePrefix) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");  //ERP-8689     
                if (creationDate == null) {
                    creationDate = getCurrentDateWithCompanyCreatorTimeZone(companyid);
                }
            Calendar cal = Calendar.getInstance();
            cal.setTime(creationDate);
            int year = cal.get(Calendar.YEAR);
            int yy = Math.abs(year) % 100; // Get YY value from year    
            DecimalFormat mFormat = new DecimalFormat("00");
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            if (isDateBeforePrefix) {
                if (selecteddateformatBeforePrefix.equalsIgnoreCase("YYYY")) {
                    datePrefix = "" + year;
                } else if (selecteddateformatBeforePrefix.equalsIgnoreCase("YYYYMM")) {
                    datePrefix = "" + year + mFormat.format(month);
                } else if (selecteddateformatBeforePrefix.equalsIgnoreCase("YY")) {
                    datePrefix = "" + mFormat.format(yy);
                } else if (selecteddateformatBeforePrefix.equalsIgnoreCase("YYMM")) {
                    datePrefix = "" + mFormat.format(yy) + mFormat.format(month);
                } else if (selecteddateformatBeforePrefix.equalsIgnoreCase("YYMMDD")) {
                    datePrefix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day);
                }else if (selecteddateformatBeforePrefix.equalsIgnoreCase("YYYY-")) {
                    datePrefix = "" + year+"-";
                } else if (selecteddateformatBeforePrefix.equalsIgnoreCase("YYYYMM-")) {
                    datePrefix = "" + year + mFormat.format(month)+"-";
                } else if (selecteddateformatBeforePrefix.equalsIgnoreCase("YY-")) {
                    datePrefix = "" + mFormat.format(yy)+"-";
                } else if (selecteddateformatBeforePrefix.equalsIgnoreCase("YYMM-")) {
                    datePrefix = "" + mFormat.format(yy) + mFormat.format(month)+"-";
                } else if (selecteddateformatBeforePrefix.equalsIgnoreCase("YYMMDD-")) {
                    datePrefix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day)+"-";
                }else { //for YYYYMMDD this will default case
                    datePrefix = sdf.format(creationDate);
                }
            }
            if (isDateAfterPrefix) {
                if (dateFormatAfterPrefix.equalsIgnoreCase("YYYY")) {
                    dateAfterPrefix = "" + year;
                } else if (dateFormatAfterPrefix.equalsIgnoreCase("YYYYMM")) {
                    dateAfterPrefix = "" + year + mFormat.format(month);
                } else if (dateFormatAfterPrefix.equalsIgnoreCase("YY")) {
                    dateAfterPrefix = "" + mFormat.format(yy);
                } else if (dateFormatAfterPrefix.equalsIgnoreCase("YYMM")) {
                    dateAfterPrefix = "" + mFormat.format(yy) + mFormat.format(month);
                } else if (dateFormatAfterPrefix.equalsIgnoreCase("YYMMDD")) {
                    dateAfterPrefix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day);
                } if (dateFormatAfterPrefix.equalsIgnoreCase("YYYY-")) {
                    dateAfterPrefix = "" + year+"-";
                } else if (dateFormatAfterPrefix.equalsIgnoreCase("YYYYMM-")) {
                    dateAfterPrefix = "" + year + mFormat.format(month)+"-";
                } else if (dateFormatAfterPrefix.equalsIgnoreCase("YY-")) {
                    dateAfterPrefix = "" + mFormat.format(yy)+"-";
                } else if (dateFormatAfterPrefix.equalsIgnoreCase("YYMM-")) {
                    dateAfterPrefix = "" + mFormat.format(yy) + mFormat.format(month)+"-";
                } else if (dateFormatAfterPrefix.equalsIgnoreCase("YYMMDD-")) {
                    dateAfterPrefix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day)+"-";
                }  else { //for YYYYMMDD this will default case
                    dateAfterPrefix = sdf.format(creationDate);
                }
            }
            if (isDateAfterSuffix) {
                if (selecteddateformatSuffix.equalsIgnoreCase("YYYY")) {
                    dateSuffix = "" + year;
                } else if (selecteddateformatSuffix.equalsIgnoreCase("YYYYMM")) {
                    dateSuffix = "" + year + mFormat.format(month);
                } else if (selecteddateformatSuffix.equalsIgnoreCase("YY")) {
                    dateSuffix = "" + mFormat.format(yy);
                } else if (selecteddateformatSuffix.equalsIgnoreCase("YYMM")) {
                    dateSuffix = "" + mFormat.format(yy) + mFormat.format(month);
                } else if (selecteddateformatSuffix.equalsIgnoreCase("YYMMDD")) {
                    dateSuffix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day);
                } else if (selecteddateformatSuffix.equalsIgnoreCase("YYYY-")) {
                    dateSuffix = "" + year+"-";
                } else if (selecteddateformatSuffix.equalsIgnoreCase("YYYYMM-")) {
                    dateSuffix = "" + year + mFormat.format(month)+"-";
                } else if (selecteddateformatSuffix.equalsIgnoreCase("YY-")) {
                    dateSuffix = "" + mFormat.format(yy)+"-";
                } else if (selecteddateformatSuffix.equalsIgnoreCase("YYMM-")) {
                    dateSuffix = "" + mFormat.format(yy) + mFormat.format(month)+"-";
                } else if (selecteddateformatSuffix.equalsIgnoreCase("YYMMDD-")) {
                    dateSuffix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day)+"-";
                }else { //for YYYYMMDD this will default case
                    dateSuffix = sdf.format(creationDate);
                }
            }
        }
            String extremestartNumber = "";
            if (showleadingzero) {
                for (int i = 0; i < numberofdigit; i++) {
                    extremestartNumber += "0";
                }
            }
            String extremeLastNumber = "";
            if (showleadingzero) {
                for (int i = 0; i < numberofdigit; i++) {
                    extremeLastNumber += "9";
                }
            }
            formatName=prefix+dateFormatAfterPrefix+extremestartNumber+suffix;
            String transactionid = "", transactionNumber = "";
            String sqltable = "", column = "";
            /**
             * get table name and column name accourding to moduleid.
             */
            jObj = getmoduleidtableandColumnName(moduleid);
            sqltable = jObj.optString("sqltable");
            column = jObj.optString("column");
            ArrayList params = new ArrayList();
            String likecondition = "%" + prefix + "%" + suffix+"%";
            String FormatedNumber = selecteddateformatBeforePrefix + prefix + dateFormatAfterPrefix + extremestartNumber + suffix + selecteddateformatSuffix;
            params.add(companyid);
            List list = null;    
            if (!StringUtil.isNullOrEmpty(column) && !StringUtil.isNullOrEmpty(sqltable)) {
                String query = "select id, " + column + " from " + sqltable + " where LENGTH(" + column + ")  = " + FormatedNumber.length() + " and " + column + " like '" + likecondition + "' and seqformat is null and company =  ?";
                list = executeSQLQuery(query, params.toArray());
            }
            if (list!=null && !list.isEmpty() && list.get(0) != null) {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Object next[] = (Object[]) itr.next();
                    transactionid = (String) next[0];
                    transactionNumber = (String) next[1];
                    if (!StringUtil.isNullOrEmpty(transactionNumber)) {
                        paramObj.put("isDateBeforePrefix", isDateBeforePrefix);
                        paramObj.put("isDateAfterPrefix", isDateAfterPrefix);
                        paramObj.put("isDateAfterSuffix", isDateAfterSuffix);
                        paramObj.put("lowerEntryNumber", transactionNumber.toLowerCase());
                        paramObj.put("selecteddateformat", selecteddateformatBeforePrefix);
                        paramObj.put("formatName", formatName);
                        paramObj.put("selectedSuffixdateformat", selecteddateformatSuffix);
                        paramObj.put("dateFormatAfterPrefix", dateFormatAfterPrefix);
                        paramObj.put("preffix", prefix.toLowerCase());
                        paramObj.put("formatgetstart", startfrom);
                        paramObj.put("suffix", suffix.toLowerCase());
                        paramObj.put("entryNumber", transactionNumber);
                        try {
                            /**
                              * common method to check transaction is belong to sequence format or not.
                              */
                            jObj = methodTocheckValidTransaction(paramObj);
                        } catch (Exception ex) {
                            continue;
                        }
                        intPartValue = jObj.optInt("intPartValue");
                        JSONObject jobj = new JSONObject();
                        if (intPartValue >= Integer.parseInt(extremestartNumber) && intPartValue <= Integer.parseInt(extremeLastNumber)) {
                            jobj.put("transactionid", transactionid);
                            jobj.put("transactionnumber", transactionNumber);
                            jobj.put("selecteddateformatBeforePrefix", datePrefix);
                            jobj.put("selecteddateformatSuffix", dateSuffix);
                            jobj.put("dateFormatAfterPrefix", dateAfterPrefix);
                            jobj.put("intPartValue", intPartValue);
                            
                        }
                        jSONArray.put(jobj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.checkTransactionmatchedwithSequenceFormat : " + ex.getMessage(), ex);
        }
        return jSONArray;
    } 
    /**
     * 
     * @param paramObj
     * @return
     * @throws JSONException
     * @throws Exception 
     * 
     * Common method to check the document number is valid or not.
     * i.e number is belongs to sequence format or not.
     */
    public JSONObject methodTocheckValidTransaction(JSONObject paramObj) throws JSONException,Exception {
        JSONObject jobj = new JSONObject();
        boolean isDateBeforePrefix=paramObj.optBoolean("isDateBeforePrefix");
        boolean isDateAfterPrefix=paramObj.optBoolean("isDateAfterPrefix");
        boolean isDateAfterSuffix=paramObj.optBoolean("isDateAfterSuffix");
        String selecteddateformat=paramObj.optString("selecteddateformat");
        String formatName=paramObj.optString("formatName");
        String selectedSuffixdateformat=paramObj.optString("selectedSuffixdateformat");
        String dateFormatAfterPrefix=paramObj.optString("dateFormatAfterPrefix");
        String lowerEntryNumber=paramObj.optString("lowerEntryNumber");
        String formatid=paramObj.optString("formatid");
        String preffix=paramObj.optString("preffix");
        String entryNumber=paramObj.optString("entryNumber");
        String suffix=paramObj.optString("suffix");
        int formatstartNumber=paramObj.optInt("formatgetstart");
        // cheque format end number for sequece format range
        int formatendNumber = -1;   
        if (paramObj.has("formatendnumber")) {
            formatendNumber = paramObj.optInt("formatendnumber", -1);
        }       
        int intPartValue = 0;
        int intStartFromValue = 0;
        boolean isSeqnum = false;
        //If any one add more date formats in UI of sequence format needs to add here as well
            Map<String,String> dataFormatMap = new HashMap<>();
            dataFormatMap.put("YYYY", "yyyy");
            dataFormatMap.put("YYYYMM", "yyyyMM");
            dataFormatMap.put("YYYYMMDD", "yyyyMMdd");
            dataFormatMap.put("YY", "yy");
            dataFormatMap.put("YYMM", "yyMM");
            dataFormatMap.put("YYMMDD", "yyMMdd");
            dataFormatMap.put("YYYY-", "yyyy-");
            dataFormatMap.put("YYYYMM-", "yyyyMM-");
            dataFormatMap.put("YYYYMMDD-", "yyyyMMdd-");
            dataFormatMap.put("YY-", "yy-");
            dataFormatMap.put("YYMM-", "yyMM-");
            dataFormatMap.put("YYMMDD-", "yyMMdd-");
        if ((isDateBeforePrefix || isDateAfterPrefix || isDateAfterSuffix)) {//if sequnece format have date
            if (lowerEntryNumber.length() == (selecteddateformat.length() + formatName.length() + selectedSuffixdateformat.length())) { //when lenght of number as well as lenght of format with date matches
                if (isDateBeforePrefix) {
                    String datePrefix = lowerEntryNumber.substring(0, selecteddateformat.length());
                    DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    if (dataFormatMap.containsKey(selecteddateformat.toUpperCase())) {
                        sdf = new SimpleDateFormat(dataFormatMap.get(selecteddateformat.toUpperCase()));
                    }
                    try {
                        sdf.setLenient(false);//make date validation more strictly.
                        sdf.parse(datePrefix);//If datePrefix is sucessfully parsed it means it is datevalue otherwise this number will not generate from this sequence format so continue
                    } catch (Exception ex) {
                        isSeqnum=false;
                        throw ex;
                    }
                }
                if (isDateAfterPrefix) {
                    String dateAfterPrefix = lowerEntryNumber.substring((selecteddateformat.length() + preffix.length()), (selecteddateformat.length() + preffix.length() + dateFormatAfterPrefix.length()));
                    DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    if (dataFormatMap.containsKey(dateFormatAfterPrefix.toUpperCase())) {
                        sdf = new SimpleDateFormat(dataFormatMap.get(dateFormatAfterPrefix.toUpperCase()));
                    }
                    try {
                        sdf.setLenient(false);//make date validation more strictly.
                        sdf.parse(dateAfterPrefix);//If datePrefix is sucessfully parsed it means it is datevalue otherwise this number will not generate from this sequence format so continue
                    } catch (Exception ex) {
                        isSeqnum=false;
                    }
                }
                if (isDateAfterSuffix) {
                    String dateSuffix = lowerEntryNumber.substring((selecteddateformat.length() + formatName.length()), lowerEntryNumber.length());
                    DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    if (dataFormatMap.containsKey(selectedSuffixdateformat.toUpperCase())) {
                        sdf = new SimpleDateFormat(dataFormatMap.get(selectedSuffixdateformat.toUpperCase()));
                    }
                    try {
                        sdf.setLenient(false);//make date validation more strictly.
                        sdf.parse(dateSuffix);//If dateSuffix is sucessfully parsed it means it is datevalue otherwise entrynumber will not generate from this sequence format so continue
                    } catch (Exception ex) {
                        isSeqnum=false;
                    }
                }
                String lowerEntryNumberWithoutDate = lowerEntryNumber.substring(selecteddateformat.length(), (lowerEntryNumber.length() - selectedSuffixdateformat.length()));//removed prefix and suffix date
                if (lowerEntryNumberWithoutDate.length() == formatName.length() && lowerEntryNumberWithoutDate.startsWith(preffix) && lowerEntryNumberWithoutDate.endsWith(suffix)) {
                    String intPart = lowerEntryNumberWithoutDate.substring((preffix.length() + dateFormatAfterPrefix.length()), (lowerEntryNumberWithoutDate.length() - suffix.length()));
                    try {
                        intPartValue = Integer.parseInt(intPart);
                    } catch (Exception ex) {
                        isSeqnum=false;
                        throw ex;
                    }
                    intStartFromValue = formatstartNumber; 
                    // checks whether integer part belongs to sequence format range
                    if (formatendNumber != -1) {
                        if (intPartValue >= intStartFromValue && intPartValue <= formatendNumber) {
                            isSeqnum = true;
                        }
                    } else {
                        isSeqnum = true;
                    }
                
                    formatName = selecteddateformat + formatName + selectedSuffixdateformat;
//                    break;//once the sequnce format found no need to chek for other sequnece format
                }
            }
        } else {
            if (lowerEntryNumber.length() == formatName.length() && lowerEntryNumber.startsWith(preffix) && lowerEntryNumber.endsWith(suffix)) {
                String intPart = entryNumber.substring(preffix.length(), (entryNumber.length() - suffix.length()));
                try {
                    intPartValue = Integer.parseInt(intPart);
                } catch (Exception ex) {
                    isSeqnum=false;
                }
                intStartFromValue = formatstartNumber;
                //checks wheather cheque number belongs to range
                if (formatendNumber != -1) {
                    if (intPartValue >= intStartFromValue && intPartValue <= formatendNumber) {
                        isSeqnum = true;
                    }
                } else {
                    isSeqnum = true;
                }
            }
        }
        jobj.put("formatName",formatName );
        jobj.put("intStartFromValue",intStartFromValue );
        jobj.put("intPartValue",intPartValue );
        jobj.put("isSeqnum",isSeqnum);
        jobj.put("formatid", formatid);
        jobj.put("chequenumber", entryNumber);
        return jobj;
    }
    /**
     * get table name and column name accourding to moduleid.
     */
    public JSONObject getmoduleidtableandColumnName(int moduleid) throws JSONException {
        String sqltable="",column="";
        int autoNum = 0;
        JSONObject jobj = new JSONObject();
        switch (moduleid) {
            case Constants.Acc_GENERAL_LEDGER_ModuleId:
                sqltable = "journalentry";
                column = "entryno";
                autoNum = StaticValues.AUTONUM_JOURNALENTRY;
                break;
            case Constants.Acc_Sales_Order_ModuleId:
                sqltable = "salesorder";
                column = "sonumber";
                autoNum = StaticValues.AUTONUM_SALESORDER;
                break;
            case Constants.Acc_Invoice_ModuleId:
                sqltable = "invoice";
                column = "invoicenumber";
                autoNum = StaticValues.AUTONUM_INVOICE;
                break;
            case Constants.Acc_Cash_Sales_ModuleId:
                sqltable = "invoice";
                column = "invoicenumber";
                autoNum = StaticValues.AUTONUM_CASHSALE;
                break;
            case Constants.Acc_Credit_Note_ModuleId:
                sqltable = "creditnote";
                column = "cnnumber";
                autoNum = StaticValues.AUTONUM_CREDITNOTE;
                break;
            case Constants.Acc_Receive_Payment_ModuleId:
                sqltable = "receipt";
                column = "receiptnumber";
                autoNum = StaticValues.AUTONUM_RECEIPT;
                break;
            case Constants.Acc_Purchase_Order_ModuleId:
                sqltable = "purchaseorder";
                column = "ponumber";
                autoNum = StaticValues.AUTONUM_PURCHASEORDER;
                break;
            case Constants.Acc_Vendor_Invoice_ModuleId:
                sqltable = "goodsreceipt";
                column = "grnumber";
                autoNum = StaticValues.AUTONUM_GOODSRECEIPT;
                break;
            case Constants.Acc_Cash_Purchase_ModuleId:
                sqltable = "goodsreceipt";
                column = "grnumber";
                autoNum = StaticValues.AUTONUM_CASHPURCHASE;
                break;
            case Constants.Acc_Debit_Note_ModuleId:
                sqltable = "debitnote";
                column = "dnnumber";
                autoNum = StaticValues.AUTONUM_DEBITNOTE;
                break;
            case Constants.Acc_Make_Payment_ModuleId:
                sqltable = "payment";
                column = "paymentnumber";
                autoNum = StaticValues.AUTONUM_PAYMENT;
                break;
            case Constants.Acc_Contract_Order_ModuleId:
                sqltable = "contract";
                column = "contractnumber";
                autoNum = StaticValues.AUTONUM_CONTRACT;
                break;
            case Constants.Acc_Customer_Quotation_ModuleId:
                sqltable = "quotation";
                column = "quotationnumber";
                autoNum = StaticValues.AUTONUM_QUOTATION;
                break;
            case Constants.Acc_Vendor_Quotation_ModuleId:
                sqltable = "vendorquotation";
                column = "quotationnumber";
                autoNum = StaticValues.AUTONUM_VENQUOTATION;
                break;
            case Constants.Acc_Purchase_Requisition_ModuleId:
                sqltable = "purchaserequisition";
                column = "prnumber";
                autoNum = StaticValues.AUTONUM_PURCHASEREQUISITION;
                break;
            case Constants.Acc_RFQ_ModuleId:
                sqltable = "requestforquotation";
                column = "rfqnumber";
                autoNum = StaticValues.AUTONUM_RFQ;
                break;
            case Constants.Acc_Product_Master_ModuleId:
                sqltable = "product";
                column = "productid";
                autoNum = StaticValues.AUTONUM_PRODUCTID;
                break;
            case Constants.Acc_Delivery_Order_ModuleId:
                sqltable = "deliveryorder";
                column = "donumber";
                autoNum = StaticValues.AUTONUM_DELIVERYORDER;
                break;
            case Constants.Acc_Goods_Receipt_ModuleId:
                sqltable = "grorder";
                column = "gronumber";
                autoNum = StaticValues.AUTONUM_GOODSRECEIPTORDER;
                break;
            case Constants.Acc_Sales_Return_ModuleId:
                sqltable = "salesreturn";
                column = "srnumber";
                autoNum = StaticValues.AUTONUM_SALESRETURN;
                break;
            case Constants.Acc_Purchase_Return_ModuleId:
                sqltable = "purchasereturn";
                column = "prnumber";
                autoNum = StaticValues.AUTONUM_PURCHASERETURN;
                break;
            case Constants.Acc_Customer_ModuleId:
                sqltable = "customer";
                column = "acccode";
                autoNum = StaticValues.AUTONUM_CUSTOMER;
                break;
            case Constants.Acc_Vendor_ModuleId:
                sqltable = "vendor";
                column = "acccode";
                autoNum = StaticValues.AUTONUM_VENDOR;
                break;
            case Constants.Acc_Build_Assembly_Product_ModuleId:
                sqltable = "productbuild";
                column = "refno";
                autoNum = StaticValues.AUTONUM_BUILDASSEMBLY;
                break;
            case Constants.Acc_FixedAssets_AssetsGroups_ModuleId:
                sqltable = "product";
                column = "";
                autoNum = StaticValues.AUTONUM_ASSETGROUP;
                break;
            case Constants.Labour_Master:
                sqltable = "labour";
                column = "";
                autoNum = StaticValues.AUTONUM_LABOUR;
                break;
            case Constants.MRP_Contract:
                sqltable = "mrpcontract";
                column = "contractid";
                autoNum = StaticValues.AUTONUM_MRPCONTRACT;
                break;
            case Constants.MRP_JOB_WORK_MODULEID:
                sqltable = "mrp_job_order";
                column = "jobordernumber";
                autoNum = StaticValues.AUTONUM_MRP_JOBWORK;
                break;
            case Constants.MRP_RouteCode:
                sqltable = "routing_template";
                column = "";
                autoNum = StaticValues.AUTONUM_MRP_ROUTECODE;
                break;
            case Constants.MRP_WORK_CENTRE_MODULEID:
                sqltable = "workcenter";
                column = "workcenterid";
                autoNum = StaticValues.AUTONUM_MRP_WORKCENTRE;
                break;
            case Constants.MRP_WORK_ORDER_MODULEID:
                sqltable = "workorder";
                column = "workorderid";
                autoNum = StaticValues.AUTONUM_MRP_WORKORDER;
                break;
            case Constants.Acc_PackingDO_ModuleId:
                sqltable = "packing";
                column = "";
                autoNum = StaticValues.AUTONUM_PACKINGDO;
                break;
            case Constants.Acc_ShippingDO_ModuleId:
                sqltable = "shippingdelivery";
                column = "";
                autoNum = StaticValues.AUTONUM_SHIPPINGDO;
                break;
            case Constants.Acc_SecurityGateEntry_ModuleId:
                sqltable = "securitygateentry";
                column = "";
                autoNum = StaticValues.AUTONUM_SECURITYNO;
                break;
                //ERP-39302
            case Constants.Cheque_ModuleId:
                sqltable = "cheque";
                column = "chequeno";
                autoNum = StaticValues.AUTONUM_CHEQUENO;
                break;
        }
        jobj.put("sqltable", sqltable);
        jobj.put("column", column);
        jobj.put("autoNum", autoNum);
        return jobj;
    }
    /**
     * Method is used to get list of companyIDs for which perpetual inventory
     * valuation is activated.
     */
    @Override
    public KwlReturnObject getPerpetualInventoryActivatedCompanyList(Map<String, Object> filterParams) throws ServiceException {
        List list = new ArrayList();
        String sqlQuery = "select distinct cp.id,c.subdomain,c.currency from compaccpreferences cp inner join company c on c.companyid = cp.id where cp.inventoryvaluationtype = 1 and c.deleteflag=0 ";
        String[] subdomain = filterParams.get("subdomains") != null ? (String[]) filterParams.get("subdomains") : null;
        String subdomains = "";
        if (subdomain != null) {
            for (int i = 0; i < subdomain.length; i++) {
                subdomains += " " + "'" + subdomain[i] + "'" + " ,";
            }
            subdomains = subdomains.substring(0, subdomains.length() - 2);
            sqlQuery += " and c.subdomain in (" + subdomains + ")";
        }
        sqlQuery += " order by createdon desc";
        list = executeSQLQuery(sqlQuery);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public Map<String, Object> getNextAutoChequeNumber_Modified(String companyid, int from, String format, boolean oldflag, Date creationDate, String chequenumber, int chequecduplicatepref) throws ServiceException, AccountingException, JSONException, Exception {
        int startfrom = 1;
        String datePrefix = "", dateSuffix = "", dateAfterPrefix = "", condition = "", pattern = "", sqltable = "", autoNumber = "", table = "";
        int numberofdigit = 0;
        String selectedDateFormatAfterPrefix = "", selectedSuffixDate = "", prefix = "", selecteddateformat = "", suffix = "", bankAccountId = "";
        boolean dateAfterSuffix = false, datebeforePrefix = false, isdateafterPrefix = false, showleadingzero = false, resetcounter = false;
        JSONObject jSONObject = new JSONObject();
        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
        table = "Cheque";
        sqltable = "cheque";
        pattern = format;
        if (StringUtil.isNullOrEmpty(pattern)) {
            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, "");//complete number
            seqNumberMap.put(Constants.SEQNUMBER, "0");//interger part
            seqNumberMap.put(Constants.DATEPREFIX, "");
            seqNumberMap.put(Constants.DATESUFFIX, "");
            return seqNumberMap;
        }
        String seqformatid = format;
        ChequeSequenceFormat seqFormat = (ChequeSequenceFormat) get(ChequeSequenceFormat.class, seqformatid);
        startfrom = seqFormat.getStartFrom().intValue();
        datebeforePrefix = seqFormat.isDateBeforePrefix();
        isdateafterPrefix = seqFormat.isDateAfterPrefix();
        dateAfterSuffix = seqFormat.isShowDateFormatAfterSuffix();
        bankAccountId = seqFormat.getBankAccount() != null ? seqFormat.getBankAccount().getID() : "";
        selectedDateFormatAfterPrefix = StringUtil.isNullOrEmpty(seqFormat.getDateformatafterprefix()) ? "" : seqFormat.getDateformatafterprefix();
        selectedSuffixDate = StringUtil.isNullOrEmpty(seqFormat.getDateFormatAfterSuffix()) ? "" : seqFormat.getDateFormatAfterSuffix();
        prefix = seqFormat.getPrefix();
        selecteddateformat = StringUtil.isNullOrEmpty(seqFormat.getDateformatinprefix()) ? "" : seqFormat.getDateformatinprefix();
        suffix = seqFormat.getSuffix();
        numberofdigit = seqFormat.getNumberOfDigits();
        showleadingzero = seqFormat.isShowLeadingZero();
        resetcounter = seqFormat.isResetCounter();
        condition += " and sequencenumber >= " + startfrom + " ";
        String query = "";
        List list = new ArrayList();
        jSONObject.put("datebeforePrefix", datebeforePrefix);
        jSONObject.put("dateAfterSuffix", dateAfterSuffix);
        jSONObject.put("isdateafterPrefix", isdateafterPrefix);
        jSONObject.put("selecteddateformat", selecteddateformat);
        jSONObject.put("companyid", companyid);
        jSONObject.put("creationDate", creationDate);
        jSONObject.put("datePrefix", datePrefix);
        jSONObject.put("selectedDateFormatAfterPrefix", selectedDateFormatAfterPrefix);
        jSONObject.put("dateAfterPrefix", dateAfterPrefix);
        jSONObject.put("selectedSuffixDate", selectedSuffixDate);
        jSONObject.put("dateSuffix", dateSuffix);
        jSONObject = getDateSuffixandPrefixForsequenceformat(jSONObject);
        datePrefix = jSONObject.optString("datePrefix");
        dateAfterPrefix = jSONObject.optString("dateAfterPrefix");
        dateSuffix = jSONObject.optString("dateSuffix");
        if (datebeforePrefix) {
            selecteddateformat = selecteddateformat != null ? selecteddateformat : "";
        }
        if (isdateafterPrefix) {
            dateAfterPrefix = dateAfterPrefix != null ? dateAfterPrefix : "";
        }
        if (dateAfterSuffix) {
            selectedSuffixDate = selectedSuffixDate != null ? selectedSuffixDate : "";
        }

        //logic to find maximum counter for the sequence format
        List paramslist = new ArrayList();
        paramslist.add(companyid);

        if (resetcounter) { //when reset option is selected/true 
            if (!StringUtil.isNullOrEmpty(datePrefix) && !StringUtil.isNullOrEmpty(dateSuffix) && !StringUtil.isNullOrEmpty(dateAfterPrefix)) { //when suffix and prefix both exist
                paramslist.add(dateSuffix);
                paramslist.add(datePrefix);
                paramslist.add(dateAfterPrefix);
                condition += " and datesuffixvalue = ? and datepreffixvalue = ? and dateafterpreffixvalue = ? ";
            } else if (!StringUtil.isNullOrEmpty(datePrefix)) { // when only prefix exist
                paramslist.add(datePrefix);
                condition += " and datepreffixvalue = ? ";
            } else if (!StringUtil.isNullOrEmpty(dateAfterPrefix)) { // when only date after prefix exist
                paramslist.add(dateAfterPrefix);
                condition += " and dateafterpreffixvalue = ? ";
            } else if (!StringUtil.isNullOrEmpty(dateSuffix)) { // when only suffix exist
                paramslist.add(dateSuffix);
                condition += " and datesuffixvalue = ? ";
            }
        }

        if (!StringUtil.isNullOrEmpty(seqformatid)) {
            condition += " and seqformat = ? ";
            paramslist.add(seqformatid);
        }
        if (!StringUtil.isNullOrEmpty(bankAccountId)) {
            condition += " and bankaccount = ? ";
            paramslist.add(bankAccountId);
        }

        query = "select max(sequencenumber) from " + sqltable + " where company =  ? " + condition;

        list = executeSQLQuery(query, paramslist.toArray());
        int nextNumber = startfrom;
        if (!list.isEmpty()) {
            if (list.get(0) != null) {
                nextNumber = Integer.parseInt(list.get(0).toString()) + 1;
            }
        }
        String nextNumTemp = nextNumber + "";
        if (showleadingzero) {
            while (nextNumTemp.length() < numberofdigit) {
                nextNumTemp = "0" + nextNumTemp;
            }
        }

        //Building the complete number
        if (datebeforePrefix || dateAfterSuffix || isdateafterPrefix) {
            autoNumber = datePrefix + prefix + dateAfterPrefix + nextNumTemp + suffix + dateSuffix;
        } else {
            autoNumber = prefix + nextNumTemp + suffix;
        }
        
        if (!chequenumber.equalsIgnoreCase(autoNumber) && from == Constants.Cheque_ModuleId && !StringUtil.isNullOrEmpty(chequenumber) && !StringUtil.isNullOrEmpty(companyid) && !StringUtil.isNullOrEmpty(nextNumTemp)) {
            String lowerEntryNumber = chequenumber.toLowerCase();
            JSONObject jobject = new JSONObject();
            jobject.put("isDateBeforePrefix", datebeforePrefix);
            jobject.put("isDateAfterPrefix", isdateafterPrefix);
            jobject.put("isDateAfterSuffix", dateAfterSuffix);
            jobject.put("lowerEntryNumber", lowerEntryNumber);
            jobject.put("selecteddateformat", selecteddateformat);
            jobject.put("formatName", autoNumber);
            jobject.put("selectedSuffixdateformat", selectedSuffixDate);
            jobject.put("dateFormatAfterPrefix", dateAfterPrefix);
            jobject.put("preffix", prefix.toLowerCase());
            jobject.put("formatgetstart", startfrom);
            jobject.put("formatid", seqFormat.getId());
            jobject.put("suffix", suffix.toLowerCase());
            jobject.put("entryNumber", chequenumber);
            /**
             * common method to check the transaction is belong to sequence
             * format or not.
             */
            jobject = methodTocheckValidTransaction(jobject);
            autoNumber = jobject.optString("chequenumber");
            int intPartValue = jobject.optInt("intPartValue");
            nextNumTemp = String.valueOf(intPartValue);
        }

        /**
         * Block checks whether the cheque number is already present in database
         * or not, if present then add it add 1 to current number and again
         * check the current cheque number in database until it get unavailable
         * cheque number in DB.
         */
        boolean isChequeNumberAvailable = true;
        while (isChequeNumberAvailable && chequecduplicatepref!=Constants.ChequeNoIgnore) {
            HashMap datamap = new HashMap();
            datamap.put("nextChequeNumber", autoNumber);
            datamap.put("chequesequenceformatid", seqformatid);
            datamap.put("bankAccountId", bankAccountId);
            JSONObject chJsonObj = isChequeNumberAvailable(datamap);
            isChequeNumberAvailable = chJsonObj.optBoolean("isChequeNumberAvailable");
            if (isChequeNumberAvailable && !StringUtil.isNullOrEmpty(autoNumber)) {
                  int nextchqNumber = (int) Integer.parseInt(nextNumTemp);
                  nextchqNumber=nextchqNumber+1;
                  nextNumTemp = String.valueOf(nextchqNumber);
                if (showleadingzero) {
                    while (nextNumTemp.length() < numberofdigit) {
                        nextNumTemp = "0" + nextNumTemp;
                    }
                }
                if (datebeforePrefix || dateAfterSuffix || isdateafterPrefix) {
                    autoNumber = datePrefix + prefix + dateAfterPrefix + nextNumTemp + suffix + dateSuffix;
                } else {
                    autoNumber = prefix + nextNumTemp + suffix;
                }
            }
        }
        seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, autoNumber);//complete number
        seqNumberMap.put(Constants.SEQNUMBER, nextNumTemp);//interger part 
        seqNumberMap.put(Constants.DATEPREFIX, datePrefix);
        seqNumberMap.put(Constants.DATEAFTERPREFIX, dateAfterPrefix);
        seqNumberMap.put(Constants.DATESUFFIX, dateSuffix);
        seqNumberMap.put("prefix", prefix);
        seqNumberMap.put("suffix", suffix);
        seqNumberMap.put("showleadingzero", showleadingzero);
        return seqNumberMap;
    }
    /**
     * get suffix prefix etc for given sequence format.
     * @param jobj
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    private JSONObject getDateSuffixandPrefixForsequenceformat(JSONObject jobj) throws JSONException, ServiceException {
        boolean datebeforePrefix = false, dateAfterSuffix = false, isdateafterPrefix = false;
        datebeforePrefix = jobj.optBoolean("datebeforePrefix");
        dateAfterSuffix = jobj.optBoolean("dateAfterSuffix");
        isdateafterPrefix = jobj.optBoolean("isdateafterPrefix");
        String companyid = jobj.optString("companyid");
        String selecteddateformat = jobj.optString("selecteddateformat");
        String datePrefix = jobj.optString("datePrefix");
        String selectedDateFormatAfterPrefix = jobj.optString("selectedDateFormatAfterPrefix");
        String dateAfterPrefix = jobj.optString("dateAfterPrefix");
        String selectedSuffixDate = jobj.optString("selectedSuffixDate");
        String dateSuffix = jobj.optString("dateSuffix");
//       Date creationDate = jobj.optString("creationDate");
        Date creationDate = null;
        if (datebeforePrefix || dateAfterSuffix || isdateafterPrefix) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");  //ERP-8689     
            if (creationDate == null) {
                creationDate = getCurrentDateWithCompanyCreatorTimeZone(companyid);
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(creationDate);
            int year = cal.get(Calendar.YEAR);
            int yy = Math.abs(year) % 100; // Get YY value from year    
            DecimalFormat mFormat = new DecimalFormat("00");
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            if (datebeforePrefix) {
                if (selecteddateformat.equalsIgnoreCase("YYYY")) {
                    datePrefix = "" + year;
                } else if (selecteddateformat.equalsIgnoreCase("YYYYMM")) {
                    datePrefix = "" + year + mFormat.format(month);
                } else if (selecteddateformat.equalsIgnoreCase("YY")) {
                    datePrefix = "" + mFormat.format(yy);
                } else if (selecteddateformat.equalsIgnoreCase("YYMM")) {
                    datePrefix = "" + mFormat.format(yy) + mFormat.format(month);
                } else if (selecteddateformat.equalsIgnoreCase("YYMMDD")) {
                    datePrefix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day);
                } else { //for YYYYMMDD this will default case
                    datePrefix = sdf.format(creationDate);
                }
            }
            if (isdateafterPrefix) {
                if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYYY")) {
                    dateAfterPrefix = "" + year;
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYYYMM")) {
                    dateAfterPrefix = "" + year + mFormat.format(month);
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YY")) {
                    dateAfterPrefix = "" + mFormat.format(yy);
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYMM")) {
                    dateAfterPrefix = "" + mFormat.format(yy) + mFormat.format(month);
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYMMDD")) {
                    dateAfterPrefix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day);
                } else { //for YYYYMMDD this will default case
                    dateAfterPrefix = sdf.format(creationDate);
                }
            }
            if (dateAfterSuffix) {
                if (selectedSuffixDate.equalsIgnoreCase("YYYY")) {
                    dateSuffix = "" + year;
                } else if (selectedSuffixDate.equalsIgnoreCase("YYYYMM")) {
                    dateSuffix = "" + year + mFormat.format(month);
                } else if (selectedSuffixDate.equalsIgnoreCase("YY")) {
                    dateSuffix = "" + mFormat.format(yy);
                } else if (selectedSuffixDate.equalsIgnoreCase("YYMM")) {
                    dateSuffix = "" + mFormat.format(yy) + mFormat.format(month);
                } else if (selectedSuffixDate.equalsIgnoreCase("YYMMDD")) {
                    dateSuffix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day);
                } else { //for YYYYMMDD this will default case
                    dateSuffix = sdf.format(creationDate);
                }
            }
        }
        jobj.put("datePrefix", datePrefix);
        jobj.put("dateAfterPrefix", dateAfterPrefix);
        jobj.put("dateSuffix", dateSuffix);
        return jobj;
    }
    
     /**
     * This method is used to get the Company's inventory preferences.
     * @param requestJSON
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getCompanyInventoryAccountPreferences(JSONObject requestJSON) throws ServiceException {
        List list = new ArrayList();
        List params = new ArrayList();
        boolean successFlag = true;
        String companyid = requestJSON.optString("companyid");
        params.add(companyid);
        String inventoryPrefQuery = "select negative_inventory_check,stock_update_batchtype,enable_stockadj_approvalflow,enable_stockreq_approvalflow,enable_stockout_approvalflow,enable_ist_return_approvalflow,enable_sr_return_approvalflow from in_inventoryconfig where company = ?";
        try {
            list = executeSQLQuery(inventoryPrefQuery, params.toArray());
        } catch (ServiceException e) {
            successFlag = false;
            throw ServiceException.FAILURE("getCompanyInventoryAccountPreferences : " + e.getMessage(), e);
        }
        return new KwlReturnObject(successFlag, "Company Inventory preferences fetched successfully", null, list, list.size());
    }
    @Override
    public KwlReturnObject getMultiGroupCompanyList(String companyid) throws ServiceException {
        List list = new ArrayList();
        String sqlQuery = "select isparent from groupcompany_companylist gpl where gpl.isparent='1' and gpl.company='" + companyid+"'";
        list = executeSQLQuery(sqlQuery);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getAgedDateFilter(String userId) throws ServiceException {
        List list = new ArrayList();
        String sqlQuery = "select preferencesjson from userpreferences where userid = '" + userId + "'";
        list = executeSQLQuery(sqlQuery);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getIntegrationParties(JSONObject paramsJobj) throws ServiceException {
        List list;
        String hqlQuery = " from IntegrationParty ";
        String conditionQuery = "";
        List paramsList = new ArrayList();
        if (!StringUtil.isNullOrEmpty(paramsJobj.optString(Constants.ID))) {
            conditionQuery += " ID = ? ";
            paramsList.add(paramsJobj.optString(Constants.ID));
        }
        if (!StringUtil.isNullOrEmpty(paramsJobj.optString(IntegrationConstants.integrationPartyNameKey))) {
            conditionQuery += " integrationPartyName = ? ";
            paramsList.add(paramsJobj.optString(IntegrationConstants.integrationPartyNameKey));
        }
        if (!StringUtil.isNullOrEmpty(conditionQuery)) {
            hqlQuery += " where " + conditionQuery;
            list = executeQuery(hqlQuery, paramsList.toArray());
        } else {
            list = executeQuery(hqlQuery);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getIntegrationPartyCountryMapping(JSONObject paramsJobj) throws ServiceException {
        List list;
        String hqlQuery = " from IntegrationPartyCountryMapping ";
        String conditionQuery = "";
        List paramsList = new ArrayList();
        if (!StringUtil.isNullOrEmpty(paramsJobj.optString(Constants.ID))) {
            conditionQuery += " ID = ? ";
            paramsList.add(paramsJobj.optString(Constants.ID));
        }
        if (!StringUtil.isNullOrEmpty(paramsJobj.optString(Constants.COUNTRY_ID))) {
            conditionQuery += " country.ID = ? ";
            paramsList.add(paramsJobj.optString(Constants.COUNTRY_ID));
        }
        if (!StringUtil.isNullOrEmpty(paramsJobj.optString(IntegrationConstants.integrationPartyIdKey))) {
            conditionQuery += " integrationParty.ID = ? ";
            paramsList.add(paramsJobj.optString(IntegrationConstants.integrationPartyIdKey));
        }
        if (!StringUtil.isNullOrEmpty(conditionQuery)) {
            hqlQuery += " where " + conditionQuery;
            list = executeQuery(hqlQuery, paramsList.toArray());
        } else {
            list = executeQuery(hqlQuery);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public boolean isBookClosed(Date finanDate, String companyid) throws JSONException {
        Calendar fyDate = Calendar.getInstance();
        fyDate.setTime(finanDate);
        int firstFinancialYearId = fyDate.get(Calendar.YEAR);
        JSONObject requestJSON = new JSONObject();
        requestJSON.put("yearid", firstFinancialYearId);
        requestJSON.put(Constants.companyKey, companyid);
        return isBookClose(requestJSON);
    }
    
}
