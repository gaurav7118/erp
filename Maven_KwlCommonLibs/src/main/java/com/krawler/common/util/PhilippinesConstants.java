/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 */
package com.krawler.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author - Rahul A. Bhawar
 */
public class PhilippinesConstants {

    public static enum VATSummaryReportSections {

        Vatable_Sales_Receipt_Private("12. Vatable Sales/Receipt- Private"),
        Sales_to_Government("13. Sales to Government"),
        Zero_Rated_Sales_Receipts("14. Zero Rated Sales/Receipts"),
        Exempt_Sales_Receipts("15. Exempt Sales/Receipts"),
        Purchase_of_Capital_Goods_not_exceeding_P1Million("18 A/B. Purchase of Capital Goods not exceeding P1Million"),
        Purchase_of_Capital_Goods_exceeding_P1Million("18 C/D. Purchase of Capital Goods exceeding P1Million"),
        Domestic_Purchase_of_Goods_other_than_capital_goods("18 E/F. Domestic Purchase of Goods other than capital goods"),
        Importation_of_Goods_other_than_capital_goods("18 G/H. Importation of Goods other than capital goods"),
        Domestic_Purchase_of_Service_Goods_other_than_capital_goods("18 I/J. Domestic Purchase of Service Goods other than capital goods"),
        Service_rendered_by_Non_residents("18 K/L. Service rendered by Non-residents"),
        Purchases_Not_Qualified_for_Input_Tax("18 M. Purchases Not Qualified for Input Tax"),
        NO_Others("18 N/O. Others"),
        Creditable_Value_Added_Tax_Withheld("23A Creditable Value-Added Tax Withheld"),
        VATwithheld_on_Sales_to_Government("23C VATwithheld on Sales to Government"),
        Advance_Payments_made("23E Advance Payments made"),
        Others("23F Others");
        private final String sectionName;

        private VATSummaryReportSections(String value) {
            this.sectionName = value;
        }

        public String get() {
            return sectionName;
        }

        public static List<String> getNames() {
            List<String> list = new ArrayList<String>();
            for (VATSummaryReportSections section : VATSummaryReportSections.values()) {
                list.add(section.get());
            }
            return list;
        }
    }
    public static String personID = "personid";
    public static String personName = "personname";
    public static String documentID = "documentid";
    public static String documentNumber = "documentNumber";
    public static String documentAmountInBase = "documentAmountInBase";
    public static String rate = "rate";
    public static String quantity = "quantity";
    public static String currencyrate = "currencyrate";
    public static String discountType = "discountType";
    public static String discountValueInBase = "discountValueInBase";
    public static String rowtaxamount = "rowtaxamount";
    public static String documentDetailsID = "documentDetailsID";
    public static String isGlobalTax = "isGlobalTax";
    public static String globalTaxInBase = "globalTaxInBase";
    public static String isPayment = "isPayment";
    public static String detailAmount = "detailAmount";
    /**
     * DataIndex For VAT Summary Report
     */
    public static String taxableAmount = "taxableamount";
    public static String taxAmount = "taxamount";
    public static String totalAmount = "totalamount";
    public static String particulars = "particulars";

    /**
     * Philippines Customer Vendor Type Map
     */
    public static String CUSTOMER_VENDOR_TYPE_Normal = "Normal";
    public static String CUSTOMER_VENDOR_TYPE_Government = "Government";
    public static String CUSTOMER_VENDOR_TYPE_ZeroRated = "Zero rated";
    public static final Map<String, String> CUSTOMER_VENDOR_TYPE = new HashMap<String, String>();

    static {
        CUSTOMER_VENDOR_TYPE.put(CUSTOMER_VENDOR_TYPE_Normal, "8b868c55-7a9f-11e8-80db-eca86bff3faf");
        CUSTOMER_VENDOR_TYPE.put(CUSTOMER_VENDOR_TYPE_Government, "8b868c54-7a9f-11e8-80db-eca86bff3faf");
        CUSTOMER_VENDOR_TYPE.put(CUSTOMER_VENDOR_TYPE_ZeroRated, "8b868c53-7a9f-11e8-80db-eca86bff3faf");
    }
    /**
     * Philippines defaultTax list id Map
     */
    public static String TAX_Input_VAT = "Input VAT";
    public static String TAX_Zero_Rated = "Zero Rated";
    public static String TAX_Exempt = "Exempt";
    public static String TAX_Output_VAT = "Output VAT";
    public static String TAX_Import = "Import";
    public static final Map<String, String> TAX_LIST = new HashMap<String, String>();
    /*
     *  Philippines sales relief summary report columns 
     */
    public static final String client_TIN = "ClientTIN";
    public static final String exempt = "Exempt";
    public static final String zeroRated = "Zerorated";
    public static final String taxableNetofVat = "TaxablenetofVAT";
    public static final String vatRate = "Vatrate";
    public static final String outputVat = "Outputvat";
    public static final String totalSales = "Totalsales";
    public static final String grossTaxable = "Grosstaxable";
    public static final String defaultTaxId = "defaultTaxId";

    static {
        TAX_LIST.put(TAX_Input_VAT, "edabc00a-76e5-11e8-adc0-fa7ae01bbebc");
        TAX_LIST.put(TAX_Zero_Rated, "edabc294-76e5-11e8-adc0-fa7ae01bbebc");
        TAX_LIST.put(TAX_Exempt, "edabc3de-76e5-11e8-adc0-fa7ae01bbebc");
        TAX_LIST.put(TAX_Output_VAT, "f85dc7b0-7a86-11e8-adc0-fa7ae01bbebc");
        TAX_LIST.put(TAX_Import, "19cdf658-8b4f-11e8-9eb6-529269fb1459");
    }
    public static String TITLES = "title";
    public static String HEADERS = "header";
    public static String ALIGNMENT = "align";
    
    public static String isLowerLimitAmount = "isLowerLimitAmount";
    public static String isGreaterLimitAmount = "isGreaterLimitAmount";
    public static String limitAmountValue = "limitAmountValue";
    public static String isAssetDocumentType = "isAssetDocumentType";
    public static double VATReport_LimitAmount = 1000000;
    
    public static String PURCHASES_RELIEF_REPORT="PurchaseReliefSummary";
    public static String SALES_RELIEF_REPORT="SalesReliefSummary";
    
    public static final String vendorId = "vendorid";
    public static final String vendorTIN = "vendortin";
    public static final String companyName="companyname";
    public static final String lastName = "lastname";
    public static final String firstName = "firstname";
    public static final String middleName = "middlename";
    public static final String address1 = "address1";
    public static final String address2 = "address2";
    public static final String jeId = "jeid";
    public static final String entryDate = "entrydate";
    public static final String taxId = "taxid";
    public static final String taxName = "taxname";
    public static final String taxCode = "taxcode";
    public static final String taxType = "taxtype";
    public static final String taxActivated = "taxactivated";
    public static final String invoicenumber = "invoicenumber";
    public static final String isAssetPurchaseInvoice = "isassetpurchaseinvoice";
    public static final String invdetail = "invoicedetails";
    public static final String totalpurchases = "totalpurchasesamt";
    public static final String taxablenetofvat = "taxableamt";
    public static final String capitalGoods = "capitalgoodsamt";
    public static final String inputVAT = "inputvatamt";
    
}
