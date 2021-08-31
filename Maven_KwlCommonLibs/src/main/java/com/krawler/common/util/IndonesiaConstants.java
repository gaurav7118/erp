/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 */
package com.krawler.common.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Rahul A. Bhawar
 */
public class IndonesiaConstants {

    /**
     * Column key constants
     */
    public static String dataRowType = "dataRowType";
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
    public static String taxType = "taxType";
    public static String isSalesInvoice = "isSalesInvoice";
    public static String invoiceDateFromJE = "invoiceDateFromJE";
    public static String NPWP_NUMBER = "NPWP";
    public static String billingAddress = "billingAddress";
    public static String productid = "productid";
    public static String productName = "productName";
    public static String taxPercent = "taxPercent";
    public static String billingPostalCode = "billingpostal";
    public static String billingPhone = "billingphone";
    public static String isTaxApplied = "isTaxApplied";
    public static String Custom_FG_PENGGANTI = "FG_PENGGANTI";
    public static String REPLACEMENT = "replacement"; // FG_PENGGANTI
    /**
     * Export Params
     */
    public static String isExportData = "isExportData";
    public static String TITLES = "title";
    public static String HEADERS = "header";
    public static String ALIGNMENT = "align";
    public static String isCreateHeaderAndFilterRow = "isCreateHeaderRow";
    /**
     * VAT Report Column Details
     */
    public static String COLUMNTYPE = "type";
    public static String COLUMNTYPE_FK = "FK";
    public static String COLUMNTYPE_LT = "LT";
    public static String COLUMNTYPE_OF = "OF";
 /**
     * Data Column INDEX
     * COLUMN1
     * COLUMN2
     * COLUMN3
     * COLUMN4
     * COLUMN5 
     * COLUMN6
     * COLUMN7
     * COLUMN8
     * COLUMN9
     * COLUMN10
     * COLUMN11
     * COLUMN12
     * COLUMN13
     * COLUMN14
     * COLUMN15
     * COLUMN16
     * COLUMN17
     * COLUMN18
     * COLUMN19
     */
    public static enum FK_COLUMN {

        TYPE(COLUMNTYPE_FK),
        KD_TYPE_OF_TRANSACTION("KD_JENIS_TRANSAKSI"), //Customer Tax Type  Linked to customer tax type e.g. 01, 02, 03
        FG_REPLACEMENT("FG_PENGGANTI"), // Replacement FG  0 or 1 Regarding to IS_FP_PENGGANTI in Sales Invoice? if yes then 1 else 0 Default : No
        FACTOR_NUMBER("NOMOR_FAKTUR"), // Tax Invoice Number - 13 digit tax invoice number
        TAX_PERIOD("MASA_PAJAK"), // Reporting period (month) - 2 digit.  Ex : 01
        TAX_YEAR("TAHUN_PAJAK"), // Reporting period (year) - 4 digit.  Ex : 2017
        DATE_OF_INVOICE("TANGGAL_FAKTUR"), // Tax Invoice Date - Invoice Date (dd/MM/yyyy)
        NPWP("NPWP"), // Capture this value from customer master
        CUSTOMER_NAME("NAMA"), // Customer Name
        COMPLETE_ADDRESS("ALAMAT_LENGKAP"), // Customer Address
        DPP_TOTAL_AMOUNT_BEFORE_TAX("JUMLAH_DPP"), // Amount before tax (PPn)
        TOTAL_VAT_AMOUNT("JUMLAH_PPN"), // PPn Tax amount
        TOTAL_PPNBM("JUMLAH_PPNBM"), // Keep this column with 0 as instructed by Sean
        ADDITIONAL_INFORMATION_ID("ID_KETERANGAN_TAMBAHAN"), // Additional Memo. Refer to Filing Rule for more detail. This option should appear on Sales Invoice, and it will depend on the Customer Tax Type.
        FG_ADVANCE("FG_UANG_MUKA"), //Keep this column with 0 as instructed by Sean
        ADVANCES_DP("UANG_MUKA_DPP"), // Keep this column with 0 as instructed by Sean
        ADVANCE_MONEY_VAT("UANG_MUKA_PPN"), //Keep this column with 0 as instructed by Sean
        ADVANCES_OF_ADVANCE("UANG_MUKA_PPNBM"), //Keep this column with 0 as instructed by Sean
        REFERENCE("REFERENSI"); // Sales Invoice Number: Need to append No Invoice in front

        private final String column;

        private FK_COLUMN(String value) {
            this.column = value;
        }

        public String get() {
            return column;
        }

        public static List<String> getNames() {
            List<String> list = new ArrayList<String>();
            for (FK_COLUMN column : FK_COLUMN.values()) {
                list.add(column.get());
            }
            return list;
        }
    }
  /**
     * Data Column INDEX
     * COLUMN1
     * COLUMN2
     * COLUMN3
     * COLUMN4
     * COLUMN5 
     * COLUMN6
     * COLUMN7
     * COLUMN8
     * COLUMN9
     * COLUMN10
     * COLUMN11
     * COLUMN12
     * COLUMN13
     * COLUMN14
     */
    public static enum LT_COLUMN {

        TYPE(COLUMNTYPE_LT),
        NPWP("NPWP"), // Tax Registration Number
        NAME("NAMA"), // Customer Name
        STREET("JALAN"), // Address
        BLOCK("BLOK"),
        NUMBER("NOMOR"),
        RT("RT"),
        RW("RW"),
        DISTRICTS("KECAMATAN"),
        CHILDHOOD("KELURAHAN"),
        DISTRICT("KABUPATEN"),
        PROVISION("PROPINSI"),
        POSTAL_CODE("KODE_POS"),
        PHONE_NUMBER("NOMOR_TELEPON");
        private final String column;

        private LT_COLUMN(String value) {
            this.column = value;
        }

        public String get() {
            return column;
        }

        public static List<String> getNames() {
            List<String> list = new ArrayList<String>();
            for (LT_COLUMN column : LT_COLUMN.values()) {
                list.add(column.get());
            }
            return list;
        }
    }

    /**
     * Data Column INDEX
     * COLUMN1
     * COLUMN2
     * COLUMN3
     * COLUMN4
     * COLUMN5 
     * COLUMN6
     * COLUMN7
     * COLUMN8
     * COLUMN9
     * COLUMN10
     * COLUMN11
     */
    public static enum OF_COLUMN {

        TYPE(COLUMNTYPE_OF),
        PRODUCT_CODE("KODE_OBJEK"), // Product ID
        PRODUCT_NAME("NAMA"), // Product Name
        UNIT_PRICE("HARGA_SATUAN"), // (Unit Price)
        QUANTITY("JUMLAH_BARANG"), //  (Quantity)
        TOTAL_PRICE("HARGA_TOTAL"), //(Price x Quantity)
        DISCOUNT("DISKON"), // (Discount)
        DPP_AMOUNT_BEFORE_TAX("DPP"), // (Amount Before Tax)
        PPN_VAT_AMOUNT("PPN"),
        PPNBM_VAT_RATE_OF_LUXURY_GOODS("TARIF_PPNBM"), // VAT rate for Luxury Goods
        PPNBM_VAT_AMOUNT_OF_LUXURY_GOODS("PPNBM"); // VAT amount for Luxury Goods

        private final String column;

        private OF_COLUMN(String value) {
            this.column = value;
        }

        public String get() {
            return column;
        }

        public static List<String> getNames() {
            List<String> list = new ArrayList<String>();
            for (OF_COLUMN column : OF_COLUMN.values()) {
                list.add(column.get());
            }
            return list;
        }
    }
    /**
     * Column's VAT Out Report
     */
    public static enum VATOUT_REPORT_DATAINDEX {

        COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8,
        COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15,
        COLUMN16, COLUMN17, COLUMN18, COLUMN19;
    }
    /**
     * Amount Columns for "FK" Type ROW
     */
    public static final List<String> Amount_COLUMN_FOR_FK_ROW = Arrays.asList("COLUMN11", "COLUMN12", "COLUMN13", "COLUMN15", "COLUMN16", "COLUMN17", "COLUMN18");

    /**
     * Amount Columns for "LT" Type ROW
     */
    public static final List<String> Amount_COLUMN_FOR_LT_ROW = Arrays.asList();

    /**
     * Amount Columns for "OF" Type ROW
     */
    public static final List<String> Amount_COLUMN_FOR_OF_ROW = Arrays.asList("COLUMN4", "COLUMN5", "COLUMN6", "COLUMN7", "COLUMN8", "COLUMN9", "COLUMN10", "COLUMN11", "COLUMN12", "COLUMN13");

    /**
     * VAT Out Report Date Formatter
     */
    public static Format dayFormatter = new SimpleDateFormat("dd");
    public static Format yearFormatter = new SimpleDateFormat("yyyy"); 
    public static Format dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
}
