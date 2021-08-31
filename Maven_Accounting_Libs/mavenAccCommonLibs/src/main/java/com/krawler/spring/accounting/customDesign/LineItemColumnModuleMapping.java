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
package com.krawler.spring.accounting.customDesign;

import java.util.HashMap;
import java.util.TreeMap;

public class LineItemColumnModuleMapping {
    /*
     * xtype 1 - String 2 - Double 3 - Date 4 - Combo 5 - Time 7- Multi combo 8
     * - custom combo
     */

    public static TreeMap<String, String> InvoiceProductLineMap = new TreeMap<String, String>();

    static {
        InvoiceProductLineMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        InvoiceProductLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:1,defaulthiddenfield:false}");
        InvoiceProductLineMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 35,seq:2,defaulthiddenfield:false}");
        InvoiceProductLineMap.put(CustomDesignerConstants.Rate, "{label:'Rate',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:3,baserate:true,defaulthiddenfield:false}");
        InvoiceProductLineMap.put(CustomDesignerConstants.QuantitywithUOM, "{label:'Quantity With UOM',xtype:'1',isFromUnitPriceAndAmount:false,defwidth: 10,seq:4,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.Amount, "{label:'Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:5 , baseamount:true,defaulthiddenfield:false}");
        InvoiceProductLineMap.put(CustomDesignerConstants.IN_Discount, "{label:'Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:6,baseamount:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.IN_Tax, "{label:'Tax',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:7,baseamount:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.IN_Currency, "{label:'Currency',xtype:'1',defwidth: 10,seq:8}");
        InvoiceProductLineMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:9}");
        InvoiceProductLineMap.put(CustomDesignerConstants.IN_Quantity, "{label:'Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:10 , basequantity:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.IN_UOM, "{label:'UOM',xtype:'1',defwidth: 10,seq:11}");
        InvoiceProductLineMap.put(CustomDesignerConstants.IN_Loc, "{label:'Loc',xtype:'1',defwidth: 15,seq:12}");
        InvoiceProductLineMap.put(CustomDesignerConstants.IN_ProductTax, "{label:'Tax Name',xtype:'1',defwidth: 15,seq:13}");
        InvoiceProductLineMap.put(CustomDesignerConstants.SUBTOTAL, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:14,baseamount:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.Discountname, "{label:'Discount Name',xtype:'1',defwidth: 10,seq:15}");
        InvoiceProductLineMap.put(CustomDesignerConstants.SerialNumber, "{label:'Serial Number',xtype:'1',defwidth: 10,seq:16}");
        InvoiceProductLineMap.put(CustomDesignerConstants.BatchNumber, "{label:'Batch No.',xtype:'1',defwidth: 10,seq:17}");
        InvoiceProductLineMap.put(CustomDesignerConstants.HSCode, "{label:'HS Code',xtype:'1',defwidth: 10,seq:18}");
        InvoiceProductLineMap.put(CustomDesignerConstants.Link_No, "{label:'Linked SO/DO/CQ/PO/GRO/VQ No.',xtype:'1',defwidth: 10,seq:19}");
        InvoiceProductLineMap.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:20}");
        InvoiceProductLineMap.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1',defwidth: 10,seq:21}");
        InvoiceProductLineMap.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag',defwidth: 10,seq:22}");
        InvoiceProductLineMap.put(CustomDesignerConstants.AllLinelevelCustomFields, "{label:'All Line Level Custom Fields',xtype:'1',defwidth: 10,seq:23}");
        
        InvoiceProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, "{label:'Exchange Rate Unit Price',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:24,baseamount:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, "{label:'Exchange Rate SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:25,baseamount:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, "{label:'Exchange Rate Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:26,baseamount:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, "{label:'Exchange Rate SubTotal-Disc',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:27,baseamount:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemTax, "{label:'Exchange Rate Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:28,baseamount:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, "{label:'Exchange Rate Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:29,baseamount:true}");
        
        InvoiceProductLineMap.put(CustomDesignerConstants.SerialNumberExp, "{label:'Serial Number Expiry',xtype:'1',defwidth: 10,seq:30}");
        InvoiceProductLineMap.put(CustomDesignerConstants.BatchNumberExp, "{label:'Batch Number Expiry',xtype:'1',defwidth: 8,seq:31}");
        InvoiceProductLineMap.put(CustomDesignerConstants.BaseQty, "{label:'Base Qty',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 6,seq:32,basequantity:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.BaseQtyWithUOM, "{label:'Base Qty With UOM',xtype:'1',isFromUnitPriceAndAmount:false,defwidth: 6,seq:33,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.RRP, "{label:'RRP',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:34,isNumeric:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.AdditionalDescription, "{label:'Additional Description',xtype:'1',defwidth: 10,seq:35}");
        InvoiceProductLineMap.put(CustomDesignerConstants.ProductBarcode, "{label:'Product Bar Code',xtype:'1',defwidth: 10,seq:39}");
        InvoiceProductLineMap.put(CustomDesignerConstants.JobWorkChallanDate, "{label:'Job Work Challan Date',xtype:'1',defwidth: 10,seq:40}");
        InvoiceProductLineMap.put(CustomDesignerConstants.JobWorkBalanceQty, "{label:'Job Work Balance Qty',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:41}");
        InvoiceProductLineMap.put(CustomDesignerConstants.JobWorkConsumeQty, "{label:'Job Work Consume Qty',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:42}");
        InvoiceProductLineMap.put(CustomDesignerConstants.PartialAmount, "{label:'Partial Amount',xtype:'1',defwidth: 10,seq:43}");
        InvoiceProductLineMap.put(CustomDesignerConstants.ManufacturingDate, "{label:'Manufacturing Date',xtype:'1',defwidth: 8,seq:44}");
        InvoiceProductLineMap.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, "{label:'Amount Before Partial Payment',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:45,baseamount:true}");
        InvoiceProductLineMap.put(CustomDesignerConstants.RATEINCLUDINGGST, "{label:'Rate Including GST',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:46,baserate:true,defaulthiddenfield:false}");
        InvoiceProductLineMap.put(CustomDesignerConstants.PRODUCTNETWEIGHT, "{label:'Product Net Weight',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:47}");
        InvoiceProductLineMap.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, "{label:'Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:48}");
        InvoiceProductLineMap.put(CustomDesignerConstants.DISPLAY_UOM, "{label:'Display UOM',xtype:'1',defwidth: 10,seq:49 }");
        InvoiceProductLineMap.put(CustomDesignerConstants.PartNumber, "{label:'Part Number',xtype:'1',defwidth: 10,seq:50 }");
        InvoiceProductLineMap.put(CustomDesignerConstants.SupplierPartNumber, "{label:'Supplier Part Number',xtype:'1',defwidth: 10,seq:51 }");
        InvoiceProductLineMap.put(CustomDesignerConstants.CustomerPartNumber, "{label:'Customer Part Number',xtype:'1',defwidth: 10,seq:52 }");
        InvoiceProductLineMap.put(CustomDesignerConstants.LineItemSubTotalWithTax, "{label:'Sub Total+Tax',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:54 }");
        
        InvoiceProductLineMap.put(CustomDesignerConstants.SpecificCurrencyAmount, "{label:'Specific Currency Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55}");
        InvoiceProductLineMap.put(CustomDesignerConstants.SpecificCurrencyDiscount, "{label:'Specific Currency Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:56}");
        InvoiceProductLineMap.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, "{label:'Specific Currency Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:57}");
        InvoiceProductLineMap.put(CustomDesignerConstants.SpecificCurrencySubTotal, "{label:'Specific Currency SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:58}");
        InvoiceProductLineMap.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, "{label:'Specific Currency SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59}");
        InvoiceProductLineMap.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, "{label:'Specific Currency Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:60}");
        InvoiceProductLineMap.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, "{label:'Specific Currency Unit Price',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61}");
        InvoiceProductLineMap.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "{label:'Exchange Rate (GST)', xtype:'2',isFromUnitPriceAndAmount:false, defwidth:10, seq:63}");
        
    }
    public static TreeMap<String, String> CustomerQuotationProductLineMap = new TreeMap<String, String>();

    static {
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:1,defaulthiddenfield:false}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 35,seq:2,defaulthiddenfield:false}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.Rate, "{label:'Rate',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:3,baserate:true,defaulthiddenfield:false}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.QuantitywithUOM, "{label:'Quantity With UOM',xtype:'1',isFromUnitPriceAndAmount:false,defwidth: 10,seq:4,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.Amount, "{label:'Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:5 , baseamount:true,defaulthiddenfield:false}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.IN_Discount, "{label:'Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:6,baseamount:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.IN_Tax, "{label:'Tax',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:7,baseamount:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.IN_Currency, "{label:'Currency',xtype:'1',defwidth: 10,seq:8}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:9}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.IN_Quantity, "{label:'Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:10 , basequantity:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.IN_UOM, "{label:'UOM',xtype:'1',defwidth: 10,seq:11}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.IN_Loc, "{label:'Loc',xtype:'1',defwidth: 15,seq:12}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.IN_ProductTax, "{label:'Tax Name',xtype:'1',defwidth: 15,seq:13}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.SUBTOTAL, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:14,baseamount:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.Discountname, "{label:'Discount Name',xtype:'1',defwidth: 10,seq:15}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.SerialNumber, "{label:'Serial Number',xtype:'1',defwidth: 10,seq:16}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.BatchNumber, "{label:'Batch No.',xtype:'1',defwidth: 10,seq:17}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.HSCode, "{label:'HS Code',xtype:'1',defwidth: 10,seq:18}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.Link_No, "{label:'Linked SO/DO/CQ/PO/GRO/VQ No.',xtype:'1',defwidth: 10,seq:19}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:20}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1',defwidth: 10,seq:21}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag',defwidth: 10,seq:22}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.AllLinelevelCustomFields, "{label:'All Line Level Custom Fields',xtype:'1',defwidth: 10,seq:23}");
        
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, "{label:'Exchange Rate Unit Price',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:24,baseamount:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, "{label:'Exchange Rate SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:25,baseamount:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, "{label:'Exchange Rate Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:26,baseamount:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, "{label:'Exchange Rate SubTotal-Disc',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:27,baseamount:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemTax, "{label:'Exchange Rate Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:28,baseamount:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, "{label:'Exchange Rate Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:29,baseamount:true}");
        
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.SerialNumberExp, "{label:'Serial Number Expiry',xtype:'1',defwidth: 10,seq:30}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.BatchNumberExp, "{label:'Batch Number Expiry',xtype:'1',defwidth: 8,seq:31}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.BaseQty, "{label:'Base Qty',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 6,seq:32,basequantity:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.BaseQtyWithUOM, "{label:'Base Qty With UOM',xtype:'1',isFromUnitPriceAndAmount:false,defwidth: 6,seq:33,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.RRP, "{label:'RRP',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:34,isNumeric:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.AdditionalDescription, "{label:'Additional Description',xtype:'1',defwidth: 10,seq:35}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.ProductBarcode, "{label:'Product Bar Code',xtype:'1',defwidth: 10,seq:39}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.JobWorkChallanDate, "{label:'Job Work Challan Date',xtype:'1',defwidth: 10,seq:39}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.JobWorkBalanceQty, "{label:'Job Work Balance Qty',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:39}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.JobWorkConsumeQty, "{label:'Job Work Consume Qty',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:39}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.PartialAmount, "{label:'Partial Amount',xtype:'1',defwidth: 10,seq:40}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.ManufacturingDate, "{label:'Manufacturing Date',xtype:'1',defwidth: 8,seq:41}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, "{label:'Amount Before Partial Payment',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:42,baseamount:true}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.RATEINCLUDINGGST, "{label:'Rate Including GST',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:42,baserate:true,defaulthiddenfield:false}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.PRODUCTNETWEIGHT, "{label:'Product Net Weight',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:32}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.LOT_SIZE, "{label:'Lot Size',xtype:'1',defwidth: 10,seq:32}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.DISPLAY_UOM, "{label:'Display UOM',xtype:'1',defwidth: 10,seq:49 }");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.LineItemSubTotalWithTax, "{label:'Sub Total+Tax',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:50 }");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.SpecificCurrencyAmount, "{label:'Specific Currency Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.SpecificCurrencyDiscount, "{label:'Specific Currency Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:56}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, "{label:'Specific Currency Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:57}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.SpecificCurrencySubTotal, "{label:'Specific Currency SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:58}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, "{label:'Specific Currency SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, "{label:'Specific Currency Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:60}");
        CustomerQuotationProductLineMap.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, "{label:'Specific Currency Unit Price',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61}");
    }
    public static HashMap<String, String> InvoiceProductSummaryItems = new HashMap<String, String>();

    static {
        InvoiceProductSummaryItems.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
        InvoiceProductSummaryItems.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true}");
        InvoiceProductSummaryItems.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, "{label:'Total Discount',xtype:'2',isFromUnitPriceAndAmount:true}");
        InvoiceProductSummaryItems.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true}");
        InvoiceProductSummaryItems.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, "{label:'Sub Total+Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
        InvoiceProductSummaryItems.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, "{label:'Total Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
        InvoiceProductSummaryItems.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount in words',xtype:'1',isFromUnitPriceAndAmount:true}");
        InvoiceProductSummaryItems.put(CustomDesignerConstants.CustomDesignTotalTermsDescription_fieldTypeId, "{label:'Product Terms Description',xtype:'1'}");
        InvoiceProductSummaryItems.put(CustomDesignerConstants.SummaryTaxPercent, "{label:'Tax Percent',xtype:'1'}");
        InvoiceProductSummaryItems.put(CustomDesignerConstants.AllLineLevelTax, "{label:'All Line Level Tax',xtype:'1'}");
        InvoiceProductSummaryItems.put(CustomDesignerConstants.AllLineLevelTaxAmount, "{label:'All Line Level Tax Amount',xtype:'1',isFromUnitPriceAndAmount:true,isNumeric:true}");
        InvoiceProductSummaryItems.put(CustomDesignerConstants.AllLineLevelTaxBasic, "{label:'All Line Level Tax Basic',xtype:'1',isFromUnitPriceAndAmount:true,isNumeric:true}");
        InvoiceProductSummaryItems.put(CustomDesignerConstants.GSTAmountInWords, "{label:'GST Amount In Words',xtype:'1',isFromUnitPriceAndAmount:true}");

    }
    public static HashMap<String, String> GROReportDOReportProductSummaryItems = new HashMap<String, String>();

    static {
        GROReportDOReportProductSummaryItems.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
        GROReportDOReportProductSummaryItems.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true}");
        GROReportDOReportProductSummaryItems.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true}");
        GROReportDOReportProductSummaryItems.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, "{label:'Total Discount',xtype:'2',isFromUnitPriceAndAmount:true}");
        GROReportDOReportProductSummaryItems.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, "{label:'Total Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
        GROReportDOReportProductSummaryItems.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount in words',xtype:'1',isFromUnitPriceAndAmount:true}");
        GROReportDOReportProductSummaryItems.put(CustomDesignerConstants.SummaryTaxPercent, "{label:'Tax Percent',xtype:'1'}");
        GROReportDOReportProductSummaryItems.put(CustomDesignerConstants.AllLineLevelTax, "{label:'All Line Level Tax',xtype:'1'}");
        GROReportDOReportProductSummaryItems.put(CustomDesignerConstants.AllLineLevelTaxAmount, "{label:'All Line Level Tax Amount',xtype:'1',isFromUnitPriceAndAmount:true,isNumeric:true}");
        GROReportDOReportProductSummaryItems.put(CustomDesignerConstants.AllLineLevelTaxBasic, "{label:'All Line Level Tax Basic',xtype:'1',isFromUnitPriceAndAmount:true,isNumeric:true}");
        GROReportDOReportProductSummaryItems.put(CustomDesignerConstants.GSTAmountInWords, "{label:'GST Amount In Words',xtype:'1',isFromUnitPriceAndAmount:true}");
    }
    public static HashMap<String, String> PaymentProductSummaryItems = new HashMap<String, String>();

    static {
        PaymentProductSummaryItems.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2'}");
        PaymentProductSummaryItems.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount in words',xtype:'1'}");
        PaymentProductSummaryItems.put(CustomDesignerConstants.SummaryTaxPercent, "{label:'Tax Percent',xtype:'1'}");
    }
    public static TreeMap<String, String> makePaymentLineMap = new TreeMap<String, String>();

    static {
        makePaymentLineMap.put("0", "{label:'Sr No',xtype:'1',defwidth: 10,seq:-1}");
        makePaymentLineMap.put("1", "{label:'Particulars',xtype:'1',defwidth: 60,seq:0}");
        makePaymentLineMap.put("2", "{label:'Amount',xtype:'1',defwidth: 15,seq:1}");
    }
    public static TreeMap<String, String> DOGROLineMap = new TreeMap<String, String>();

    static {
        DOGROLineMap.put("1", "{label:'Product Name',xtype:'1',defwidth: 15,seq:0}");
        DOGROLineMap.put("2", "{label:'Product Description',xtype:'1',defwidth: 35,seq:1}");
        DOGROLineMap.put("4", "{label:'Quantity',xtype:'1',defwidth: 10,seq:3,isNumeric:true}");
    }
    public static TreeMap<String, String> DOGROLineMap_DO = new TreeMap<String, String>();

    static {
        DOGROLineMap_DO.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth: 5,seq:0,defaulthiddenfield:false}");
        DOGROLineMap_DO.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:1,defaulthiddenfield:false}");
        DOGROLineMap_DO.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth:25,seq:2,defaulthiddenfield:false}");
        DOGROLineMap_DO.put(CustomDesignerConstants.AdditionalDescription, "{label:'Additional Description',xtype:'1',defwidth:25,seq:2,defaulthiddenfield:false}");
        DOGROLineMap_DO.put(CustomDesignerConstants.Rate, "{label:'Rate',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:3, baserate:true,defaulthiddenfield:false}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:4}");
        DOGROLineMap_DO.put(CustomDesignerConstants.Amount, "{label:'Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:5, baseamount:true,defaulthiddenfield:false}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_Loc, "{label:'Loc',xtype:'1',defwidth: 15,seq:6}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_ActualQuantityWithUOM, "{label:'Actual Quantity With UOM',xtype:'1',defwidth: 10,seq:7,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_DeliveredQuantityWithUOM, "{label:'Delivered Quantity With UOM',xtype:'1',defwidth: 10,seq:8,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_Remarks, "{label:'Remarks',xtype:'1',defwidth: 10,seq:9}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_SerialNumber, "{label:'Serial Number',xtype:'1',defwidth: 10,seq:10}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_CISONo, "{label:'CI/SO No.',xtype:'1',defwidth: 10,seq:11}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_ActualQuantity, "{label:'Actual Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:12 , basequantity:true}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_DeliveredQuantity, "{label:'Delivered Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:13 , basequantity:true}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_UOM, "{label:'UOM ',xtype:'1',defwidth: 10,seq:14}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_RRP, "{label:'RRP',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:15,isNumeric:true}");
        DOGROLineMap_DO.put(CustomDesignerConstants.SUBTOTAL, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:16,baseamount:true}");
        DOGROLineMap_DO.put(CustomDesignerConstants.IN_Tax, "{label:'Tax',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:17,baseamount:true}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_ProductTax, "{label:'Tax Name',xtype:'1',defwidth: 15,seq:18}");
        DOGROLineMap_DO.put(CustomDesignerConstants.DO_Discount, "{label:'Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:19,baseamount:true}");
        DOGROLineMap_DO.put(CustomDesignerConstants.Discountname, "{label:'Discount Name',xtype:'1',defwidth: 10,seq:20}");
        DOGROLineMap_DO.put(CustomDesignerConstants.BatchNumber, "{label:'Lot#',xtype:'1',defwidth: 10,seq:21}");
        DOGROLineMap_DO.put(CustomDesignerConstants.HSCode, "{label:'HS Code',xtype:'1',defwidth: 10,seq:22}");
        DOGROLineMap_DO.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1',defwidth: 10,seq:23}");
        DOGROLineMap_DO.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag',defwidth: 10,seq:24}");
        DOGROLineMap_DO.put(CustomDesignerConstants.AllLinelevelCustomFields, "{label:'All Line Level Custom Fields',xtype:'1',defwidth: 10,seq:25}");
        DOGROLineMap_DO.put(CustomDesignerConstants.BatchNumberExp, "{label:'Batch Number Expiry',xtype:'1',defwidth: 8,seq:29}");
        DOGROLineMap_DO.put(CustomDesignerConstants.ManufacturingDate, "{label:'Manufacturing Date',xtype:'1',defwidth: 8,seq:30}");
        DOGROLineMap_DO.put(CustomDesignerConstants.ProductBarcode, "{label:'Product Bar Code',xtype:'1',defwidth: 10,seq:31}");
        DOGROLineMap_DO.put(CustomDesignerConstants.PRODUCTNETWEIGHT, "{label:'Product Net Weight',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:32}");
        DOGROLineMap_DO.put(CustomDesignerConstants.PACK_QUANTITY, "{label:'Pack Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:33}");
        DOGROLineMap_DO.put(CustomDesignerConstants.PACK_LOCATION, "{label:'Pack Location',xtype:'1',defwidth: 10,seq:34}");
        DOGROLineMap_DO.put(CustomDesignerConstants.Warehouse, "{label:'warehouse',xtype:'1',defwidth: 15,seq:warehouse}");
        DOGROLineMap_DO.put(CustomDesignerConstants.PartNumber, "{label:'Part Number',xtype:'1',defwidth: 10,seq:50 }");
        DOGROLineMap_DO.put(CustomDesignerConstants.SupplierPartNumber, "{label:'Supplier Part Number',xtype:'1',defwidth: 10,seq:51 }");
        DOGROLineMap_DO.put(CustomDesignerConstants.CustomerPartNumber, "{label:'Customer Part Number',xtype:'1',defwidth: 10,seq:52 }");
        DOGROLineMap_DO.put(CustomDesignerConstants.BatchQuantity, "{label:'Batch Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:53}");
        DOGROLineMap_DO.put(CustomDesignerConstants.LineItemSubTotalWithTax, "{label:'Sub Total+Tax',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:54 }");
        DOGROLineMap_DO.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:16}");
        DOGROLineMap_DO.put(CustomDesignerConstants.QUANTITY_PER_PACKAGE, "{label:'Quantity per Package',xtype:'1',isFromUnitPriceAndAmount:false,defwidth: 10,seq:33}");
        DOGROLineMap_DO.put(CustomDesignerConstants.GROSS_WEIGHT, "{label:'Gross Weight',xtype:'1',defwidth: 10,seq:33}");
        DOGROLineMap_DO.put(CustomDesignerConstants.PACKAGE_MEASUREMENT, "{label:'Package Measurement',xtype:'1',defwidth: 10,seq:33}");
        DOGROLineMap_DO.put(CustomDesignerConstants.PACKAGE_NAME, "{label:'Package Name',xtype:'1',defwidth: 10,seq:33}");
        DOGROLineMap_DO.put(CustomDesignerConstants.RATEINCLUDINGGST, "{label:'Rate Including GST',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:42,baserate:true,defaulthiddenfield:false}");
        DOGROLineMap_DO.put(CustomDesignerConstants.OrderQuantity, "{label:'SO Ordered Quantity',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10}");
        DOGROLineMap_DO.put(CustomDesignerConstants.SI_ORDER_QUANTITY, "{label:'SI Ordered Quantity',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10}");
        
    }
    public static TreeMap<String, String> DOGROLineMap_GR = new TreeMap<String, String>();

    static {
        DOGROLineMap_GR.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        DOGROLineMap_GR.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:1,defaulthiddenfield:false}");
        DOGROLineMap_GR.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth:25,seq:2,defaulthiddenfield:false}");
        DOGROLineMap_GR.put(CustomDesignerConstants.AdditionalDescription, "{label:'Additional Description',xtype:'1',defwidth:25,seq:2,defaulthiddenfield:false}");
        DOGROLineMap_GR.put(CustomDesignerConstants.Rate, "{label:'Rate',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:3 , baserate:true,defaulthiddenfield:false}");
        DOGROLineMap_GR.put(CustomDesignerConstants.GR_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:4}");
        DOGROLineMap_GR.put(CustomDesignerConstants.Amount, "{label:'Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:5 , baseamount:true,defaulthiddenfield:false}");
        DOGROLineMap_GR.put(CustomDesignerConstants.GR_ActualQuantityWithUOM, "{label:'Actual Quantity With UOM',xtype:'1',defwidth: 10,seq:6,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        DOGROLineMap_GR.put(CustomDesignerConstants.GR_ReceivedQuantityWithUOM, "{label:'Received Quantity With UOM',xtype:'1',defwidth: 10,seq:7,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        DOGROLineMap_GR.put(CustomDesignerConstants.GR_Remarks, "{label:'Remarks',xtype:'1',defwidth: 10,seq:8}");
        DOGROLineMap_GR.put(CustomDesignerConstants.GR_SerialNumber, "{label:'Serial Number',xtype:'1',defwidth: 10,seq:9}");
        DOGROLineMap_GR.put(CustomDesignerConstants.GR_VIPONo, "{label:'VI/PO No.',xtype:'1',defwidth: 10,seq:10}");
        DOGROLineMap_GR.put(CustomDesignerConstants.GR_ReceivedQuantity, "{label:'Received Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:11 , basequantity:true}");
        DOGROLineMap_GR.put(CustomDesignerConstants.GR_UOM, "{label:'UOM',xtype:'1',defwidth: 10,seq:12}");
        DOGROLineMap_GR.put(CustomDesignerConstants.GR_ActualQuantity, "{label:'Actual Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:13 , basequantity:true}");
        DOGROLineMap_GR.put(CustomDesignerConstants.HSCode, "{label:'HS Code',xtype:'1',defwidth: 10,seq:14}");
        DOGROLineMap_GR.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1',defwidth: 10,seq:15}");
        DOGROLineMap_GR.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag',defwidth: 10,seq:16}");
        DOGROLineMap_GR.put(CustomDesignerConstants.AllLinelevelCustomFields, "{label:'All Line Level Custom Fields',xtype:'1',defwidth: 10,seq:17}");
        DOGROLineMap_GR.put(CustomDesignerConstants.BatchNumber, "{label:'Lot#',xtype:'1',defwidth: 10,seq:21}");
        DOGROLineMap_GR.put(CustomDesignerConstants.BatchNumberExp, "{label:'Batch Number Expiry',xtype:'1',defwidth: 8,seq:29}");
        DOGROLineMap_GR.put(CustomDesignerConstants.ManufacturingDate, "{label:'Manufacturing Date',xtype:'1',defwidth: 8,seq:30}");
        DOGROLineMap_GR.put(CustomDesignerConstants.GR_Loc, "{label:'Loc',xtype:'1',defwidth: 15,seq:6}");
        DOGROLineMap_GR.put(CustomDesignerConstants.Warehouse, "{label:'warehouse',xtype:'1',defwidth: 15,seq:warehouse}");
        DOGROLineMap_GR.put(CustomDesignerConstants.DISPLAY_UOM, "{label:'Display UOM',xtype:'1',defwidth: 15,seq:31}");
        DOGROLineMap_GR.put(CustomDesignerConstants.PartNumber, "{label:'Part Number',xtype:'1',defwidth: 10,seq:50 }");
        DOGROLineMap_GR.put(CustomDesignerConstants.SupplierPartNumber, "{label:'Supplier Part Number',xtype:'1',defwidth: 10,seq:51 }");
        DOGROLineMap_GR.put(CustomDesignerConstants.CustomerPartNumber, "{label:'Customer Part Number',xtype:'1',defwidth: 10,seq:52 }");
        DOGROLineMap_GR.put(CustomDesignerConstants.LineItemSubTotalWithTax, "{label:'Sub Total+Tax',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:53 }");
        DOGROLineMap_GR.put(CustomDesignerConstants.SUBTOTAL, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:13,baseamount:true}");
        DOGROLineMap_GR.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:16}");
        DOGROLineMap_GR.put(CustomDesignerConstants.GR_Discount, "{label:'Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:6,baseamount:true}");
        DOGROLineMap_GR.put(CustomDesignerConstants.RATEINCLUDINGGST, "{label:'Rate Including GST',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:42,baserate:true,defaulthiddenfield:false}");

    }
    public static TreeMap<String, String> InvoiceProductLineMap_PO = new TreeMap<String, String>();

    static {
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth: 5,seq:0,defaulthiddenfield:false}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:1,defaulthiddenfield:false}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 20,seq:2,defaulthiddenfield:false}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.AdditionalDescription, "{label:'Additional Description',xtype:'1',defwidth: 20,seq:2,defaulthiddenfield:false}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.Rate, "{label:'Rate',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:3, baserate:true,defaulthiddenfield:false}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.QuantitywithUOM, "{label:'Quantity With UOM',xtype:'1',isFromUnitPriceAndAmount:false,defwidth: 10,seq:4,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.Amount, "{label:'Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:5 , baseamount:true,defaulthiddenfield:false}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.IN_Discount, "{label:'Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:6,baseamount:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.IN_Tax, "{label:'Tax',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:7 ,baseamount:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.IN_Currency, "{label:'Currency',xtype:'1',defwidth: 10,seq:8}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.PO_PermitNo, "{label:'Permit No',xtype:'1',defwidth: 10,seq:9}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.PO_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:10}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.PO_Quantity, "{label:'Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:11 ,basequantity:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.PO_UOM, "{label:'UOM',xtype:'1',defwidth: 10,seq:12}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.SUBTOTAL, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:13,baseamount:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.Discountname, "{label:'Discount Name',xtype:'1',defwidth: 10,seq:14}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.HSCode, "{label:'HS Code',xtype:'1',defwidth: 10,seq:15}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:16}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.IN_ProductTax, "{label:'Tax Name',xtype:'1',defwidth: 15,seq:17}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1',defwidth: 10,seq:18}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag',defwidth: 10,seq:19}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.AllLinelevelCustomFields, "{label:'All Line Level Custom Fields',xtype:'1',defwidth: 10,seq:20}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.ProductAvailableQuantity, "{label:'Product Available Quantity',xtype:'1',defwidth: 10,seq:21,basequantitywithuom:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.BaseQty, "{label:'Base Qty',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 6,seq:22,basequantity:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.BaseQtyWithUOM, "{label:'Base Qty With UOM',xtype:'1',isFromUnitPriceAndAmount:false,defwidth: 6,seq:23,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.RATEINCLUDINGGST, "{label:'Rate Including GST',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:42,baserate:true,defaulthiddenfield:false}");
        
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, "{label:'Exchange Rate Unit Price',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:24,baseamount:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, "{label:'Exchange Rate SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:25,baseamount:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, "{label:'Exchange Rate Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:26,baseamount:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, "{label:'Exchange Rate SubTotal-Disc',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:27,baseamount:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.BaseCurrencyLineItemTax, "{label:'Exchange Rate Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:28,baseamount:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, "{label:'Exchange Rate Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:29,baseamount:true}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.DISPLAY_UOM, "{label:'Display UOM',xtype:'1',defwidth: 10,seq:49}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.LineItemSubTotalWithTax, "{label:'Sub Total+Tax',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:50 }");
        /*
         * Balance Qty and Balance Qty with UOM
         */
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.BalanceQty, "{label:'Balance Qty',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 6,seq:30}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.BalanceQtyWithUOM, "{label:'Balance Qty With UOM',xtype:'1',isFromUnitPriceAndAmount:false,defwidth: 6,seq:31,isNumeric:true}");
        
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.SpecificCurrencyAmount, "{label:'Specific Currency Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.SpecificCurrencyDiscount, "{label:'Specific Currency Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:56}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, "{label:'Specific Currency Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:57}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.SpecificCurrencySubTotal, "{label:'Specific Currency SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:58}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, "{label:'Specific Currency SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, "{label:'Specific Currency Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:60}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, "{label:'Specific Currency Unit Price',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61}");
        
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.PartNumber, "{label:'Part Number', xtype:'1', defwidth: 10, seq:63}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.SupplierPartNumber, "{label:'Supplier Part Number', xtype:'1', defwidth: 10, seq:64}");
        InvoiceProductLineMap_PO.put(CustomDesignerConstants.CustomerPartNumber, "{label:'Customer Part Number', xtype:'1', defwidth: 10, seq:65}");
    }
    /*
     * LineLevelTax Map for Indian company
     */
    public static TreeMap<String, String> LineLevelTaxFields_India_USA = new TreeMap<String, String>();
    /*
     * LineLevelTax Fields for Indian company
     */
    static {
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.LineLevelTax, "{label:'Line Level Tax',xtype:'1',defwidth: 10,seq:56}");
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.LineLevelTaxAmount, "{label:'Line Level Tax Amount',xtype:'1',isFromUnitPriceAndAmount:true,defwidth: 10,seq:57}");
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.LineLevelTaxPercent, "{label:'Line Level Tax Percent',xtype:'1',defwidth: 10,seq:58,isNumeric:true}");      
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.HSN_SAC_CODE, "{label:'HSN/SAC Code',xtype:'1',defwidth: 10,seq:59}");      
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.CGSTAMOUNT, "{label:'CGST Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55 }");
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.CGSTPERCENT, "{label:'CGST Percent',xtype:'2',defwidth: 10,seq:56 }");
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.IGSTAMOUNT, "{label:'IGST Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:57 }");
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.IGSTPERCENT, "{label:'IGST Percent',xtype:'2',defwidth: 10,seq:58 }");
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.UTGSTAMOUNT, "{label:'UTGST Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59 }");
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.UTGSTPERCENT, "{label:'UTGST Percent',xtype:'2',defwidth: 10,seq:60 }");
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.SGSTAMOUNT, "{label:'SGST Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61 }");
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.SGSTPERCENT, "{label:'SGST Percent',xtype:'2',defwidth: 10,seq:62 }");
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.CESSPERCENT, "{label:'CESS Percent',xtype:'2',defwidth: 10,seq:63 }");
        LineLevelTaxFields_India_USA.put(CustomDesignerConstants.CESSAMOUNT, "{label:'CESS Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:64 }");
    }
    
    /*
     * New added fields for SO
     */
    public static TreeMap<String, String> InvoiceProductLineMap_SO = new TreeMap<String, String>();
    
    static {
        /*
         * Balance Qty and Balance Qty with UOM
         */
        InvoiceProductLineMap_SO.put(CustomDesignerConstants.BalanceQty, "{label:'Balance Qty',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 6,seq:55}");
        InvoiceProductLineMap_SO.put(CustomDesignerConstants.BalanceQtyWithUOM, "{label:'Balance Qty With UOM',xtype:'1',isFromUnitPriceAndAmount:false,defwidth: 6,seq:56,isNumeric:true}");
    }
    
    public static TreeMap<String, String> InvoiceProductLineMap2 = new TreeMap<String, String>();

    static {
        InvoiceProductLineMap2.put("1", "{label:'Product',xtype:'1',defwidth: 15,seq:0}");
        InvoiceProductLineMap2.put("2", "{label:'Product Description',xtype:'1',defwidth:20,seq:1}");
        InvoiceProductLineMap2.put("3", "{label:'Rate',xtype:'2',defwidth: 10,seq:2}");
        InvoiceProductLineMap2.put("4", "{label:'Quantity',xtype:'1',defwidth: 10,seq:3,isNumeric:true}");
        InvoiceProductLineMap2.put("5", "{label:'Amount',xtype:'2',defwidth: 10,seq:4}");
        InvoiceProductLineMap2.put("6", "{label:'Discount',xtype:'2',defwidth: 10,seq:5}");
        InvoiceProductLineMap2.put("7", "{label:'Tax',xtype:'2',defwidth: 10,seq:6}");
        InvoiceProductLineMap2.put("8", "{label:'Currency',xtype:'1',defwidth: 10,seq:7}");
        InvoiceProductLineMap2.put("9", "{label:'Permit No',xtype:'1',defwidth: 10,seq:8}");
    }
    
    public static TreeMap<String, String> SalesReturnProductLineMap = new TreeMap<String, String>();

    static {
        SalesReturnProductLineMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:1,defaulthiddenfield:false}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SR_SerialNumber, "{label:'Serial Number',xtype:'1',defwidth: 10,seq:2}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 35,seq:3,defaulthiddenfield:false}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.AdditionalDescription, "{label:'Additional Description',xtype:'1',defwidth: 35,seq:3,defaulthiddenfield:false}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SR_ActualQuantitywithUOM, "{label:'Actual Quantity With UOM',xtype:'1',defwidth: 10,seq:4,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SR_ReturnQuantitywithUOM, "{label:'Return Quantity With UOM',xtype:'1',defwidth: 10,seq:5,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SR_ActualQuantity, "{label:'Actual Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:6 , basequantity:true}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SR_ReturnQuantity, "{label:'Return Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:7 , basequantity:true}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.Rate, "{label:'Rate',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:8,baserate:true,defaulthiddenfield:false}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.Amount, "{label:'Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:9 , baseamount:true,defaulthiddenfield:false}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.IN_Tax, "{label:'Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:10 ,baseamount:true}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.IN_Currency, "{label:'Currency',xtype:'1',defwidth: 10,seq:11}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:12}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.IN_UOM, "{label:'UOM',xtype:'1',defwidth: 10,seq:13}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.IN_Loc, "{label:'Loc',xtype:'1',defwidth: 15,seq:14}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SR_ProductTax, "{label:'Tax Name',xtype:'1',defwidth: 10,seq:15 }");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SR_Reason, "{label:'Reason',xtype:'1',defwidth: 10,seq:16}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SR_Remark, "{label:'Remarks',xtype:'1',defwidth: 10,seq:17}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.HSCode, "{label:'HS Code',xtype:'1',defwidth: 10,seq:18}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SerialNumber, "{label:'Product SerialNo',xtype:'1',defwidth: 10,seq:19}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.BatchNumber, "{label:'Product BatchNo',xtype:'1',defwidth: 10,seq:20}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1',defwidth: 10,seq:21}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag',defwidth: 10,seq:22}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.AllLinelevelCustomFields, "{label:'All Line Level Custom Fields',xtype:'1',defwidth: 10,seq:23}");
        
        SalesReturnProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, "{label:'Exchange Rate Unit Price',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:24,baseamount:true}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, "{label:'Exchange Rate SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:25,baseamount:true}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, "{label:'Exchange Rate Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:26,baseamount:true}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, "{label:'Exchange Rate SubTotal-Disc',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:27,baseamount:true}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemTax, "{label:'Exchange Rate Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:28,baseamount:true}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, "{label:'Exchange Rate Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:29,baseamount:true}");
        
        SalesReturnProductLineMap.put(CustomDesignerConstants.SUBTOTAL, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:30,baseamount:true}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:31}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.Discountname, "{label:'Discount Name',xtype:'1',defwidth: 10,seq:32}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.IN_Discount, "{label:'Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:33,baseamount:true}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.PRODUCTNETWEIGHT, "{label:'Product Net Weight',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:32}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.DISPLAY_UOM, "{label:'Display UOM',xtype:'1',defwidth: 10,seq:33}");
        
        SalesReturnProductLineMap.put(CustomDesignerConstants.SpecificCurrencyAmount, "{label:'Specific Currency Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SpecificCurrencyDiscount, "{label:'Specific Currency Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:56}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, "{label:'Specific Currency Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:57}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SpecificCurrencySubTotal, "{label:'Specific Currency SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:58}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, "{label:'Specific Currency SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, "{label:'Specific Currency Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:60}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, "{label:'Specific Currency Unit Price',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.ManufacturingDate_Batch, "{label:'Batch Mfg date',xtype:'3',defwidth: 10,seq:63}");
        SalesReturnProductLineMap.put(CustomDesignerConstants.BatchNumberExp, "{label:'Batch Expiry Date',xtype:'3',defwidth: 10,seq:64}");
    }
    
    public static TreeMap<String, String> PurchaseReturnProductLineMap = new TreeMap<String, String>();

    static {
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:1,defaulthiddenfield:false}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.SR_SerialNumber, "{label:'Serial Number',xtype:'1',defwidth: 10,seq:2}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 35,seq:3,defaulthiddenfield:false}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.AdditionalDescription, "{label:'Additional Description',xtype:'1',defwidth: 35,seq:3,defaulthiddenfield:false}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.SR_ActualQuantitywithUOM, "{label:'Actual Quantity With UOM',xtype:'1',defwidth: 10,seq:4,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.SR_ReturnQuantitywithUOM, "{label:'Return Quantity With UOM',xtype:'1',defwidth: 10,seq:5,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.SR_ActualQuantity, "{label:'Actual Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:6 , basequantity:true}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.SR_ReturnQuantity, "{label:'Return Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:7 , basequantity:true}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.Rate, "{label:'Rate',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:8,baserate:true,defaulthiddenfield:false}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.Amount, "{label:'Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:9 , baseamount:true,defaulthiddenfield:false}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.IN_Tax, "{label:'Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:10 ,baseamount:true}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.IN_Currency, "{label:'Currency',xtype:'1',defwidth: 10,seq:11}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:12}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.IN_UOM, "{label:'UOM',xtype:'1',defwidth: 10,seq:13}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.IN_Loc, "{label:'Loc',xtype:'1',defwidth: 15,seq:14}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.SR_ProductTax, "{label:'Product Tax',xtype:'1',defwidth: 10,seq:15 }");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.SR_Reason, "{label:'Reason',xtype:'1',defwidth: 10,seq:16}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.SR_Remark, "{label:'Remarks',xtype:'1',defwidth: 10,seq:17}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.HSCode, "{label:'HS Code',xtype:'1',defwidth: 10,seq:18}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.SerialNumber, "{label:'Product SerialNo',xtype:'1',defwidth: 10,seq:19}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.BatchNumber, "{label:'Product BatchNo',xtype:'1',defwidth: 10,seq:20}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1',defwidth: 10,seq:21}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag',defwidth: 10,seq:22}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.AllLinelevelCustomFields, "{label:'All Line Level Custom Fields',xtype:'1',defwidth: 10,seq:23}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.SUBTOTAL, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:30,baseamount:true}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:31}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.Discountname, "{label:'Discount Name',xtype:'1',defwidth: 10,seq:32}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.IN_Discount, "{label:'Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:33,baseamount:true}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.DISPLAY_UOM, "{label:'Display UOM',xtype:'1',defwidth: 10,seq:34,baseamount:true}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.ManufacturingDate_Batch, "{label:'Batch Mfg date',xtype:'3',defwidth: 10,seq:63}");
        PurchaseReturnProductLineMap.put(CustomDesignerConstants.BatchNumberExp, "{label:'Batch Expiry Date',xtype:'3',defwidth: 10,seq:64}");
    }
    
//   Stock Request
    public static TreeMap<String, String> StockRequestLineMap = new TreeMap<String, String>();

    static {
        StockRequestLineMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:1,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:2,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.Packaging, "{label:'Packaging',xtype:'1',defwidth: 10,seq:3,basequantitywithuom:true,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.UOM, "{label:'UOM',xtype:'1',defwidth: 10,seq:4,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.OrderQuantity, "{label:'Order Quantity',xtype:'2',defwidth: 10,seq:5 , basequantity:true,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.Quantity, "{label:'Quantity',xtype:'2',defwidth: 10,seq:6 , basequantity:true,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.CostCenter, "{label:'Cost Center',xtype:'1',defwidth: 15,seq:7,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.CostCenterName, "{label:'Cost Center Name',xtype:'1',defwidth: 15,seq:7,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.ProjectNo, "{label:'Project No',xtype:'1',defwidth: 10,seq:8,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.SR_Remark, "{label:'Remark',xtype:'1',defwidth: 10,seq:9,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.Location, "{label:'Location',xtype:'1',defwidth: 15,seq:10}");
        StockRequestLineMap.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag',defwidth: 10,seq:11}");
        StockRequestLineMap.put(CustomDesignerConstants.AdditionalDescription, "{label:'Product Additional Description',xtype:'1',defwidth: 10,seq:12,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 35,seq:13,defaulthiddenfield:false}");
        StockRequestLineMap.put(CustomDesignerConstants.ProductBarcode, "{label:'Product Bar Code',xtype:'1',defwidth: 10,seq:15}");
        
    } 
//   Stock Request
    public static TreeMap<String, String> StockRepairLineMap = new TreeMap<String, String>();

    static {
        StockRepairLineMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        StockRepairLineMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:1,defaulthiddenfield:false}");
        StockRepairLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:2,defaulthiddenfield:false}");
        StockRepairLineMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 35,seq:13,defaulthiddenfield:false}");
        StockRepairLineMap.put(CustomDesignerConstants.Quantity, "{label:'Quantity',xtype:'2',defwidth: 10,seq:6 , basequantity:true,defaulthiddenfield:false}");
        StockRepairLineMap.put(CustomDesignerConstants.UOM, "{label:'UOM',xtype:'1',defwidth: 10,seq:4,defaulthiddenfield:false}");
        StockRepairLineMap.put(CustomDesignerConstants.Location, "{label:'Location',xtype:'1',defwidth: 15,seq:10}");
        StockRepairLineMap.put(CustomDesignerConstants.BatchNumber, "{label:'Batch',xtype:'1',defwidth: 15,seq:11}");
        StockRepairLineMap.put(CustomDesignerConstants.SerialNumber, "{label:'Serials',xtype:'1',defwidth: 15,seq:12}");
        StockRepairLineMap.put(CustomDesignerConstants.SR_Reason, "{label:'Reason',xtype:'1',defwidth: 10,seq:9}");
        StockRepairLineMap.put(CustomDesignerConstants.SR_Remark, "{label:'Remarks',xtype:'1',defwidth: 10,seq:9}");
        StockRepairLineMap.put(CustomDesignerConstants.CostCenter, "{label:'Cost Center',xtype:'1',defwidth: 15,seq:14,defaulthiddenfield:false}");
        StockRepairLineMap.put(CustomDesignerConstants.CostCenterName, "{label:'Cost Center Name',xtype:'1',defwidth: 15,seq:14,defaulthiddenfield:false}");
    } 
    
    public static TreeMap<String, String> StockIssueLineMap = new TreeMap<String, String>();

    static {
        StockIssueLineMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:1,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:2,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.Packaging, "{label:'Packaging',xtype:'1',defwidth: 10,seq:3,basequantitywithuom:true,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.UOM, "{label:'Order UoM',xtype:'1',defwidth: 10,seq:4,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.CostCenter, "{label:'Cost Center',xtype:'1',defwidth: 15,seq:6,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.CostCenterName, "{label:'Cost Center Name',xtype:'1',defwidth: 15,seq:6,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.Quantity, "{label:'Quantity (in Order UOM)',xtype:'2',defwidth: 10,seq:5 , basequantity:true,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.ISSUED_QUANTITY, "{label:'Issued Quantity',xtype:'2',defwidth: 10,seq:5, basequantity:true,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.DELIVERED_QUANTITY, "{label:'Delivered Quantity',xtype:'2',defwidth: 10,seq:5, basequantity:true,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.SR_Remark, "{label:'Remark',xtype:'1',defwidth: 10,seq:8,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.Location, "{label:'Issue Location',xtype:'1',defwidth: 15,seq:9}");
        StockIssueLineMap.put(CustomDesignerConstants.CollectLocation, "{label:'Collect Location',xtype:'1',defwidth: 15,seq:10}");
        StockIssueLineMap.put(CustomDesignerConstants.BatchNumber, "{label:'Batch',xtype:'1',defwidth: 15,seq:11}");
        StockIssueLineMap.put(CustomDesignerConstants.SerialNumber, "{label:'Serials',xtype:'1',defwidth: 15,seq:12}");
        StockIssueLineMap.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag',defwidth: 10,seq:13}");
        StockIssueLineMap.put(CustomDesignerConstants.DO_RRP, "{label:'RRP',xtype:'1',defwidth: 10,seq:14,isNumeric:true}");
        StockIssueLineMap.put(CustomDesignerConstants.ProductBarcode, "{label:'Product Bar Code',xtype:'1',defwidth: 10,seq:15}");
        StockIssueLineMap.put(CustomDesignerConstants.AdditionalDescription, "{label:'Product Additional Description',xtype:'1',defwidth: 10,seq:16,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 35,seq:17,defaulthiddenfield:false}");
        StockIssueLineMap.put(CustomDesignerConstants.ProductBarcode, "{label:'Product Bar Code',xtype:'1',defwidth: 10,seq:18}");
    }
    
       public static TreeMap<String, String> StockAdjustmentLineMap = new TreeMap<String, String>();

    static {
        StockAdjustmentLineMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:1,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:2,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.UOM, "{label:'UoM',xtype:'1',defwidth: 10,seq:3,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.SR_Batch_SubQty_UOM, "{label:'Sub Qty UOM',xtype:'1',defwidth: 18,seq:3,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.CostCenter, "{label:'Cost Center',xtype:'1',defwidth: 15,seq:4,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.CostCenterName, "{label:'Cost Center Name',xtype:'1',defwidth: 15,seq:4,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.AdjustmentType, "{label:'Adjustment Type',xtype:'1',defwidth: 15,seq:5,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.Quantity, "{label:'Quantity',xtype:'2',defwidth: 10,seq:6 , basequantity:true,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.SR_Reason, "{label:'Reason',xtype:'1',defwidth: 10,seq:7,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.SR_Batch_SubQty, "{label:'Batch Sub Quantity',xtype:'1',defwidth: 10,seq:14,defaulthiddenfield:false,isNumeric:true}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.SR_Batch_Exp_Date, "{label:'Batch Expiry Date',xtype:'3',defwidth: 10,seq:15,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.SR_Serial_Exp_Date, "{label:'Serial Expity Date',xtype:'3',defwidth: 10,seq:16,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.SR_Asset, "{label:'Asset',xtype:'1',defwidth: 10,seq:17,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.SR_Remark, "{label:'Remark',xtype:'1',defwidth: 10,seq:8,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.PerUnitPrice, "{label:'Per Unit Price',xtype:'2',defwidth: 10,seq:9,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.Location, "{label:'Location',xtype:'1',defwidth: 15,seq:10}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.BatchNumber, "{label:'Batch',xtype:'1',defwidth: 15,seq:11}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.SerialNumber, "{label:'Serials',xtype:'1',defwidth: 15,seq:12}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag',defwidth: 10,seq:13}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 35,seq:14,defaulthiddenfield:false}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.ProductBarcode, "{label:'Product Bar Code',xtype:'1',defwidth: 10,seq:15}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.ManufacturingDate_Batch, "{label:'Batch Mfg date',xtype:'3',defwidth: 10,seq:16}");
        StockAdjustmentLineMap.put(CustomDesignerConstants.ManufacturingDate_Serial, "{label:'Serial Mfg date',xtype:'3',defwidth: 10,seq:17}");

    }
    
    public static TreeMap<String, String> InterStoreStockTransferLineMap = new TreeMap<String, String>();

    static {
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:1,defaulthiddenfield:false}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:2,defaulthiddenfield:false}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.Packaging, "{label:'Packaging',xtype:'1',defwidth: 10,seq:3,basequantitywithuom:true,defaulthiddenfield:false}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.UOM, "{label:'Transfer UoM',xtype:'1',defwidth: 10,seq:4,defaulthiddenfield:false}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.CostCenter, "{label:'Cost Center',xtype:'1',defwidth: 15,seq:5,defaulthiddenfield:false}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.CostCenterName, "{label:'Cost Center Name',xtype:'1',defwidth: 15,seq:5,defaulthiddenfield:false}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.Quantity, "{label:'Quantity (in Transfer UOM)',xtype:'2',defwidth: 10,seq:6 , basequantity:true,defaulthiddenfield:false}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.SR_Remark, "{label:'Remark',xtype:'1',defwidth: 10,seq:7,defaulthiddenfield:false}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.Location, "{label:'Issue Location',xtype:'1',defwidth: 15,seq:8}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.BatchNumber, "{label:'Batch',xtype:'1',defwidth: 15,seq:9}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.SerialNumber, "{label:'Serials',xtype:'1',defwidth: 15,seq:10}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag',defwidth: 10,seq:11}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 35,seq:12,defaulthiddenfield:false}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.HSCode, "{label:'HS Code',xtype:'1',defwidth: 10,seq:13}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.ProductBarcode, "{label:'Product Bar Code',xtype:'1',defwidth: 10,seq:14}");
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.BatchNumberExp, "{label:'Exp Date',xtype:'1',defwidth: 10,seq:17}"); //xtype is 1 because this field can be have multiple dates
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.ManufacturingDate, "{label:'Mfg Date',xtype:'1',defwidth: 10,seq:18}"); //xtype is 1 because this field can be have multiple dates
        InterStoreStockTransferLineMap.put(CustomDesignerConstants.PerUnitPrice, "{label:'Purchase Price',xtype:'2',defwidth: 10,seq:19}");
    }
    public static TreeMap<String, String> RFQProductLineMap = new TreeMap<String, String>();

    static {
        RFQProductLineMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth: 5,seq:0,defaulthiddenfield:false}");
        RFQProductLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:1,defaulthiddenfield:false}");
        RFQProductLineMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 20,seq:2,defaulthiddenfield:false}");
        RFQProductLineMap.put(CustomDesignerConstants.Rate, "{label:'Rate',xtype:'2',defwidth: 10,seq:3, baserate:true,defaulthiddenfield:false}");
        RFQProductLineMap.put(CustomDesignerConstants.QuantitywithUOM, "{label:'Quantity With UOM',xtype:'1',defwidth: 10,seq:4,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        RFQProductLineMap.put(CustomDesignerConstants.Amount, "{label:'Amount',xtype:'2',defwidth: 10,seq:5 , baseamount:true,defaulthiddenfield:false}");
        RFQProductLineMap.put(CustomDesignerConstants.PO_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:10}");
        RFQProductLineMap.put(CustomDesignerConstants.PO_Quantity, "{label:'Quantity',xtype:'2',defwidth: 10,seq:11 ,basequantity:true}");
        RFQProductLineMap.put(CustomDesignerConstants.PO_UOM, "{label:'UOM',xtype:'1',defwidth: 10,seq:12}");
        RFQProductLineMap.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1',defwidth: 10,seq:18}");
        RFQProductLineMap.put(CustomDesignerConstants.AllLinelevelCustomFields, "{label:'All Line Level Custom Fields',xtype:'1',defwidth: 10,seq:20}");
    }
    
    public static TreeMap<String, String> InterLocationStoreTransferLineMap = new TreeMap<String, String>();

    static {
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:1,defaulthiddenfield:false}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:2,defaulthiddenfield:false}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.Packaging, "{label:'Packaging',xtype:'1',defwidth: 10,seq:3,basequantitywithuom:true,defaulthiddenfield:false}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.UOM, "{label:'Transfer UoM',xtype:'1',defwidth: 10,seq:4,defaulthiddenfield:false}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.CostCenter, "{label:'Cost Center',xtype:'1',defwidth: 15,seq:5,defaulthiddenfield:false}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.CostCenterName, "{label:'Cost Center Name',xtype:'1',defwidth: 15,seq:5,defaulthiddenfield:false}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.Quantity, "{label:'Quantity',xtype:'2',defwidth: 10,seq:6 , basequantity:true,defaulthiddenfield:false}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.SR_Remark, "{label:'Remark',xtype:'1',defwidth: 10,seq:7,defaulthiddenfield:false}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.CollectLocation,"{label:'Collect Location',xtype:'1',defwidth: 15,seq:8}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.BatchNumber, "{label:'Batch',xtype:'1',defwidth: 15,seq:9}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.SerialNumber, "{label:'Serials',xtype:'1',defwidth: 15,seq:10}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag',defwidth: 10,seq:11}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 35,seq:12,defaulthiddenfield:false}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.HSCode, "{label:'HS Code',xtype:'1',defwidth: 10,seq:13}");
        InterLocationStoreTransferLineMap.put(CustomDesignerConstants.ProductBarcode, "{label:'Product Bar Code',xtype:'1',defwidth: 10,seq:14}");
    }
    
    public static TreeMap<String, String> CustomDesignStatementOfAccountLineLevelExtraFieldsMap = new TreeMap<String, String>(); //Request For Quotation ExtraFields

    static {
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerName, "{label:'Customer Name',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOABillDate, "{label:'Date',xtype:'3',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOATransactionID, "{label:'Transaction ID',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOATransactionType, "{label:'Transaction Type',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAJournalEntry, "{label:'Journal Entry',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAMemo, "{label:'Memo',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOADebitAmount, "{label:'Debit Amount',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACreditAmount, "{label:'Credit Amount',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOADebitAmountInBase, "{label:'Debit Amount in Base Currency',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustVendCurrBalanceAmountInBase, "{label:'Balance Amount in Customer Currency',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACreditAmountInBase, "{label:'Credit Amount in Base Currency',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOABalanceAmountInBase, "{label:'Balance Amount in Base Currency',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOADueDate, "{label:'Due Date',xtype:'3',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACurrency, "{label:'Currency',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAPartialPayment, "{label:'Partial Payment',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAPoRefrence, "{label:'Customer PO Reference No',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAOriginalAmount, "{label:'Original Amount',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAOchequeNo, "{label:'Cheque No',xtype:'1',defwidth: 10}}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOABanalanceAmount, "{label:'Amount Due in Base Currency',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAAmountDue, "{label:'Amount Due',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.LINEEXCHANGERATE, "{label:'Exchange Rate',xtype:'2',defwidth: 10}");
        
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOABalanceAmount, "{label:'Balance Amount',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelExtraFieldsMap.put(CustomDesignerConstants.Amount, "{label:'Amount',xtype:'2',defwidth: 10}");
        
    }
    public static TreeMap<String, String> PurchaseRequisitionLineMap = new TreeMap<String, String>();

    static {
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.PO_ProductCode, "{label:'Product Code',xtype:'1',defwidth: 10,seq:1,defaulthiddenfield:false}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth: 15,seq:2,defaulthiddenfield:false}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth: 35,seq:2,defaulthiddenfield:false}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.AdditionalDescription, "{label:'Additional Description',xtype:'1',defwidth: 35,seq:2,defaulthiddenfield:false}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.IN_UOM, "{label:'UOM',xtype:'1',defwidth: 10,seq:11}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.Quantity, "{label:'Quantity',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:6 , basequantity:true,defaulthiddenfield:false}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.QuantitywithUOM, "{label:'Quantity With UOM',xtype:'1',isFromUnitPriceAndAmount:false,defwidth: 10,seq:4,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.BaseQty, "{label:'Base Qty',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 6,seq:32,basequantity:true}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.BaseQtyWithUOM, "{label:'Base Qty With UOM',xtype:'1',isFromUnitPriceAndAmount:false,defwidth: 6,seq:33,basequantitywithuom:true,defaulthiddenfield:false,isNumeric:true}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.Rate, "{label:'Rate',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:8,baserate:true,defaulthiddenfield:false}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.SUBTOTAL, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:30,baseamount:true}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:31}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.IN_Currency, "{label:'Currency',xtype:'1',defwidth: 10,seq:8}");
        PurchaseRequisitionLineMap.put(CustomDesignerConstants.DISPLAY_UOM, "{label:'Display UOM',xtype:'1',defwidth: 10,seq:9}");

    }
    public static TreeMap<String, String> CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap = new TreeMap<String, String>(); //Request For Quotation ExtraFields

    static {
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.SrNO, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorName, "{label:'Vendor Name',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOABillDate, "{label:'Date',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOATransactionID, "{label:'Transaction ID',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOATransactionType, "{label:'Transaction Type',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAJournalEntry, "{label:'Journal Entry',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAMemo, "{label:'Memo',xtype:'1',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOADebitAmount, "{label:'Debit Amount',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACreditAmount, "{label:'Credit Amount',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustVendCurrBalanceAmountInBase, "{label:'Balance Amount in Vendor Currency',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOADebitAmountInBase, "{label:'Debit Amount in Base Currency',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACreditAmountInBase, "{label:'Credit Amount in Base Currency',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOABalanceAmountInBase, "{label:'Balance Amount in Base Currency',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOADueDate, "{label:'Due Date',xtype:'1',defwidth: 10}}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACurrency, "{label:'Currency',xtype:'1',defwidth: 10}}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAPartialPayment, "{label:'Partial Payment',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAOriginalAmount, "{label:'Original Amount',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAOchequeNo, "{label:'Cheque No',xtype:'1',defwidth: 10}}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOABanalanceAmount, "{label:'Amount Due in Base Currency',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAAmountDue, "{label:'Amount Due',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.LINEEXCHANGERATE, "{label:'Exchange Rate',xtype:'2',defwidth: 10}");
        CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap.put(CustomDesignerConstants.SUPPLIER_INVOICE_NO, "{label:'Supplier Invoice No',xtype:'1',defwidth: 10}");
        
    }
    /**
     * Map for line level fields in QA Approval module
     */
    public static TreeMap<String, String> CustomDesign_QA_Approval_LineMap = new TreeMap<String, String>(); //Request For Quotation ExtraFields

    static {
        CustomDesign_QA_Approval_LineMap.put(CustomDesignerConstants.SrNo, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        CustomDesign_QA_Approval_LineMap.put(CustomDesignerConstants.INSPECTION_TEMPLATE_AREA, "{label:'Inspection Area',xtype:'1',defwidth: 10}");
        CustomDesign_QA_Approval_LineMap.put(CustomDesignerConstants.INSPECTION_TEMPLATE_STATUS, "{label:'Status',xtype:'1',defwidth: 10}");
        CustomDesign_QA_Approval_LineMap.put(CustomDesignerConstants.INSPECTION_TEMPLATE_SPECIFIED_FAULTS, "{label:'Specified Faults',xtype:'1',defwidth: 10}");
        CustomDesign_QA_Approval_LineMap.put(CustomDesignerConstants.INSPECTION_TEMPLATE_FAULTS, "{label:'Faults',xtype:'1',defwidth: 10}");
        
    }
    //ERM-26 Line level items map
    public static final TreeMap<String, String> BuildAssemblyReportLineMap = new TreeMap(); //Request For Build Assembly ExtraFields

    static {
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.SrNo, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.UOM, "{label:'UOM',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.PRODUCT_TYPE, "{label:'Product Type',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.QUANTITY_NEEDED, "{label:'Quantity Needed',xtype:'2',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.CustomDesignActualQuantity_fieldTypeId, "{label:'Actual Quantity',xtype:'2',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.INVENTORY_QUANTITY, "{label:'Inventory Quantity',xtype:'2',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.RECYCLE_QUANTITY, "{label:'Recycled Quantity',xtype:'2',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.REMAINING_QUANTITY, "{label:'Remaining Quantity',xtype:'2',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.WASTAGE_QUANTITY, "{label:'Wastage Quantity',xtype:'2',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.Rate, "{label:'Product Cost',xtype:'2',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.BatchNumber, "{label:'Batch Number',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.SerialNumber, "{label:'Serial Number',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.BatchNumberExp, "{label:'Expiry Date',xtype:'3',defwidth:5,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.Location, "{label:'Product Location',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        BuildAssemblyReportLineMap.put(CustomDesignerConstants.Warehouse, "{label:'Product Warehouse',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
    }
    
    /**
     * Details Table maps
     */
    //comman field map for details table
    public static final TreeMap<String, String> BankReconciliationTableMap_DetailsTableCommanFieldsMap = new TreeMap();
    static{
        BankReconciliationTableMap_DetailsTableCommanFieldsMap.put(CustomDesignerConstants.SrNo, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_DetailsTableCommanFieldsMap.put(CustomDesignerConstants.DATE, "{label:'Date',xtype:'3',defwidth:15,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_DetailsTableCommanFieldsMap.put(CustomDesignerConstants.ChequeDate, "{label:'Cheque Date',xtype:'3',defwidth:15,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_DetailsTableCommanFieldsMap.put(CustomDesignerConstants.Chequeno, "{label:'Cheque Number',xtype:'1',defwidth:25,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_DetailsTableCommanFieldsMap.put(CustomDesignerConstants.CUSTOMER_VENDOR_NAME, "{label:'Customer/ Vendor Name',xtype:'1',defwidth:30,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_DetailsTableCommanFieldsMap.put(CustomDesignerConstants.REFERENCE_NO_DESC, "{label:'Reference Number/ Description',xtype:'1',defwidth:30,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_DetailsTableCommanFieldsMap.put(CustomDesignerConstants.JOURNAL_ENTRY_NO, "{label:'Journal Entry No',xtype:'1',defwidth:30,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_DetailsTableCommanFieldsMap.put(CustomDesignerConstants.TRANSACTION_ID, "{label:'Transaction ID',xtype:'1',defwidth:30,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_DetailsTableCommanFieldsMap.put(CustomDesignerConstants.MEMO, "{label:'Memo',xtype:'1',defwidth:30,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_DetailsTableCommanFieldsMap.put(CustomDesignerConstants.DOCUMENT_CURRENCY_SYMBOL, "{label:'Document Currency Symbol',xtype:'1',defwidth:30,seq:0,defaulthiddenfield:false}");
    }
    //Map for Deposits and Other Credits details of Bank Reconciliation module
    public static final TreeMap<String, String> BankReconciliationTableMap_Deposits_and_Other_Credits = new TreeMap();
    static{
        BankReconciliationTableMap_Deposits_and_Other_Credits.put(CustomDesignerConstants.RECEIVED_FROM, "{label:'Received From',xtype:'1',defwidth:25,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_Deposits_and_Other_Credits.put(CustomDesignerConstants.DEBIT_AMOUNT_IN_DOCUMENT_CURRENCY, "{label:'Debit Amount in Document Currency',xtype:'2',defwidth:25,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_Deposits_and_Other_Credits.put(CustomDesignerConstants.DEBIT_AMOUNT_IN_ACCOUNT_CURRENCY, "{label:'Debit Amount in Account Currency',xtype:'2',defwidth:25,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_Deposits_and_Other_Credits.put(CustomDesignerConstants.DEBIT_AMOUNT_IN_BASE_CURRENCY, "{label:'Debit Amount in Base Currency',xtype:'2',defwidth:25,seq:0,defaulthiddenfield:false}");
    }
    //Map for Checks and Payments details of Bank Reconciliation module
    public static final TreeMap<String, String> BankReconciliationTableMap_Checks_and_Payments = new TreeMap();
    static{
        BankReconciliationTableMap_Checks_and_Payments.put(CustomDesignerConstants.PAID_TO, "{label:'Paid To',xtype:'1',defwidth:25,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_Checks_and_Payments.put(CustomDesignerConstants.CREDIT_AMOUNT_IN_DOCUMENT_CURRENCY, "{label:'Credit Amount in Document Currency',xtype:'2',defwidth:25,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_Checks_and_Payments.put(CustomDesignerConstants.CREDIT_AMOUNT_IN_ACCOUNT_CURRENCY, "{label:'Credit Amount in Account Currency',xtype:'2',defwidth:25,seq:0,defaulthiddenfield:false}");
        BankReconciliationTableMap_Checks_and_Payments.put(CustomDesignerConstants.CREDIT_AMOUNT_IN_BASE_CURRENCY, "{label:'Credit Amount in Base Currency',xtype:'2',defwidth:25,seq:0,defaulthiddenfield:false}");
    }
    //MRP Work Order
    //MRP Work Order Details Table Map - Component Availability
    public static final TreeMap<String, String> MRP_WORK_ORDER_MAP_ComponentAvailability = new TreeMap();
    
    static{
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.SrNo, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth:15,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth:20,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth:20,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.PRODUCT_TYPE, "{label:'Product Type',xtype:'1',defwidth:15,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.BALANCE_QUANTITY, "{label:'Balance Quantity',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.REQUIRED_QUANTITY, "{label:'Required Quantity',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.BLOCK_QUANTITY, "{label:'Block Quantity',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.WAREHOUSE, "{label:'Warehouse',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.LOCATION, "{label:'Location',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.MIN_PERCENT_QUANTITY_REQUIRED, "{label:'Min. Per. Qty Required',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.BLOCKED_QTY_BY_OTHER_ORDERS, "{label:'Blocked Qty By Other Orders',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.SHORTFALL_QUANTITY, "{label:'Shortfall Quantity',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_ComponentAvailability.put(CustomDesignerConstants.ORDER_QUANTITY, "{label:'Order Quantity',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
    }
    //MRP Work Order Details Table Map - Consumption
    public static final TreeMap<String, String> MRP_WORK_ORDER_MAP_Consumption = new TreeMap();
    
    static{
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.SrNo, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth:15,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth:20,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth:20,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.PRODUCT_TYPE, "{label:'Product Type',xtype:'1',defwidth:15,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.BALANCE_QUANTITY, "{label:'Balance Quantity',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.REQUIRED_QUANTITY, "{label:'Quantity Required',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.BLOCK_QUANTITY, "{label:'Block Quantity',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.ACTUAL_QUANTITY, "{label:'Actual Quantity',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.REJECTED_QUANTITY, "{label:'Rejected Quantity',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.WASTE_QUANTITY, "{label:'Waste Quantity',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.RECYCLE_QUANTITY, "{label:'Recycle Quantity',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.PRODUCED_QUANTITY, "{label:'Produced Quantity',xtype:'2',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.WAREHOUSE, "{label:'Warehouse',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.LOCATION, "{label:'Location',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.BatchNumber, "{label:'Batch Number',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Consumption.put(CustomDesignerConstants.SerialNumber, "{label:'Serial Number',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        
    }
    //MRP Work Order Details Table Map - Tasks
    public static final TreeMap<String, String> MRP_WORK_ORDER_MAP_Tasks = new TreeMap();
    
    static{
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.SrNo, "{label:'Sr No',xtype:'1',defwidth:5,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.TASK_NAME, "{label:'Task Name',xtype:'1',defwidth:15,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1',defwidth:15,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1',defwidth:20,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1',defwidth:20,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.PRODUCT_TYPE, "{label:'Product Type',xtype:'1',defwidth:15,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.NOTES, "{label:'Notes',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.DURATION, "{label:'Duration',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.START_DATE, "{label:'Start Date',xtype:'3',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.END_DATE, "{label:'End Date',xtype:'3',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.PROGRESS, "{label:'Progress',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.SKILLS, "{label:'Skills',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.PROCESSES, "{label:'Processes',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        MRP_WORK_ORDER_MAP_Tasks.put(CustomDesignerConstants.RESOURCE_NAMES, "{label:'Resource Names',xtype:'1',defwidth:10,seq:0,defaulthiddenfield:false}");
        
    }
    //MRP Work Order Checklist Table Map
    public static final TreeMap<String, String> MRP_WORK_ORDER_MAP_Checklist = new TreeMap();
    
    static{
        MRP_WORK_ORDER_MAP_Checklist.put(CustomDesignerConstants.TASK_NAME, "{label:'Task Name',xtype:'1',seq:1}");
        MRP_WORK_ORDER_MAP_Checklist.put(CustomDesignerConstants.CHECKLIST, "{label:'Checklist',xtype:'1',seq:2}");
        
    }
    /*
    Details table map for Sales invoice
    */
     public static final TreeMap<String, String> DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP = new TreeMap();
    
    static {
        DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP.put(CustomDesignerConstants.HSN_SAC_CODE, "{label:'HSN/SAC Code',xtype:'1',defwidth: 10,seq:59}");      
        DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP.put(CustomDesignerConstants.CGSTAMOUNT, "{label:'CGST Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55 }");
        DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP.put(CustomDesignerConstants.CGSTPERCENT, "{label:'CGST Percent',xtype:'2',defwidth: 10,seq:56 }");
        DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP.put(CustomDesignerConstants.IGSTAMOUNT, "{label:'IGST Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:57 }");
        DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP.put(CustomDesignerConstants.IGSTPERCENT, "{label:'IGST Percent',xtype:'2',defwidth: 10,seq:58 }");
        DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP.put(CustomDesignerConstants.UTGSTAMOUNT, "{label:'UTGST Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59 }");
        DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP.put(CustomDesignerConstants.UTGSTPERCENT, "{label:'UTGST Percent',xtype:'2',defwidth: 10,seq:60 }");
        DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP.put(CustomDesignerConstants.SGSTAMOUNT, "{label:'SGST Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61 }");
        DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP.put(CustomDesignerConstants.SGSTPERCENT, "{label:'SGST Percent',xtype:'2',defwidth: 10,seq:62 }");
        DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP.put(CustomDesignerConstants.CESSPERCENT, "{label:'CESS Percent',xtype:'2',defwidth: 10,seq:63 }");
        DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP.put(CustomDesignerConstants.CESSAMOUNT, "{label:'CESS Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:64 }");
        DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP.put(CustomDesignerConstants.TAXABLE_VALUE, "{label:'Taxable Value',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:64 }");
        
    }
}
    
