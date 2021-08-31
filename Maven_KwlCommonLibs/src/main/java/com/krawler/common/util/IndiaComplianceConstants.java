/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.util;

import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class IndiaComplianceConstants {

    public static final HashMap<Integer, String> MVAT_COA_CODES = new HashMap<Integer, String>(); // Maharashtra State VAT code for Accounts
    public static final HashMap<Integer, String> MVAT_COA_CODES_PURCHASE_RETURN = new HashMap<Integer, String>(); // Maharashtra State VAT code for Accounts
    public static final HashMap<Integer, String> MVAT_COA_CODES_DEBIT_NOTE = new HashMap<Integer, String>(); // Maharashtra State VAT code for Accounts
    public static final HashMap<Integer, String> MVAT_COA_CODES_SALES_RETURN = new HashMap<Integer, String>(); // Maharashtra State VAT code for Accounts
    public static final HashMap<Integer, String> MVAT_COA_CODES_CREDIT_NOTE = new HashMap<Integer, String>(); // Maharashtra State VAT code for Accounts
    public static final HashMap<String, Integer> MVAT_TRANSCATION_CODES = new HashMap<String, Integer>(); // Maharashtra State VAT code for Accounts

    static {
        // Purchase Transaction Codes
        MVAT_COA_CODES.put(10, "Within the State Purchases from RD");
        MVAT_COA_CODES.put(15, "Within the State Purchases from RD (Capital Asset)");
        MVAT_COA_CODES.put(20, "Within the State URD Purchases");
        MVAT_COA_CODES.put(30, "Inter State Branch Transfer");
        MVAT_COA_CODES.put(35, "Within the State Branch Transfer");
        MVAT_COA_CODES.put(40, "Inter State Purchases against Form C");
        MVAT_COA_CODES.put(45, "Within the State Purchases against Form C (Purchase in transit u/s 6 (2) )");
        MVAT_COA_CODES.put(50, "Inter State Purchases against Form H");
        MVAT_COA_CODES.put(55, "Within the State Purchases against H Form");
        MVAT_COA_CODES.put(60, "Imports (Direct)");
        MVAT_COA_CODES.put(65, "Imports (High Seas)");
        MVAT_COA_CODES.put(70, "Inter State Purchases without Form");
        MVAT_COA_CODES.put(75, "Inter State Purchases against Form I");
        MVAT_COA_CODES.put(80, "Deduction u/s 3(2)");

        // Purchase Goods Return Codes
        MVAT_COA_CODES_PURCHASE_RETURN.put(90, "Purchase Goods Return for Transaction Type 10,15,80");
        MVAT_COA_CODES_PURCHASE_RETURN.put(91, "Purchase Goods Return (Within the State URD) for Transaction Type 20");
        MVAT_COA_CODES_PURCHASE_RETURN.put(31, "Purchase Goods Return (Inter State Branch Transfer) for Transaction Type 30");
        MVAT_COA_CODES_PURCHASE_RETURN.put(36, "Purchase Goods Return (Within the State Branch Transfer) for Transaction Type 35");
        MVAT_COA_CODES_PURCHASE_RETURN.put(41, "Purchase Goods Return ( Inter State Purchases against Form C) for Transaction Type 40");
        MVAT_COA_CODES_PURCHASE_RETURN.put(46, "Purchase Goods Return (Within State Purchase against Form C) for transaction type 45");
        MVAT_COA_CODES_PURCHASE_RETURN.put(51, "Purchase Goods Return (Inter State Purchase against Form H) For Transaction Type 50");
        MVAT_COA_CODES_PURCHASE_RETURN.put(56, "Purchase Goods Return (Within the State Purchases against H Form) for Transaction Type 55");
        MVAT_COA_CODES_PURCHASE_RETURN.put(61, "Purchase Goods Return (Direct Import) for Transaction Type 60");
        MVAT_COA_CODES_PURCHASE_RETURN.put(66, "Purchase Goods Return (High Seas ) For Transaction Type 65");
        MVAT_COA_CODES_PURCHASE_RETURN.put(71, "Purchase Goods Return ( Inter State Purchases without Form) For Transaction Type 70");
        MVAT_COA_CODES_PURCHASE_RETURN.put(76, "Purchase Goods Return (Inter State Purchases against Form I) For Transaction Type 75");
        
        //Purchase Debit Note
        MVAT_COA_CODES_DEBIT_NOTE.put(95, "Purchase Debit Note for Transaction Type 10,15,80");
        MVAT_COA_CODES_DEBIT_NOTE.put(96, "Purchase Debit Note (Within the State URD) for Transaction Type 20");
        MVAT_COA_CODES_DEBIT_NOTE.put(32, "Purchase Debit Note (Inter State Branch Transfer) for Transaction Type 30");
        MVAT_COA_CODES_DEBIT_NOTE.put(37, "Purchase Debit Note( Within the State Branch Transfer) for Transaction Type 35");
        MVAT_COA_CODES_DEBIT_NOTE.put(42, "Purchase Debit Note ( Inter State Purchases against Form C) for Transaction Type 40");
        MVAT_COA_CODES_DEBIT_NOTE.put(47, "Purchase Debit Note (Within State Purchase against Form C) for transaction type 45");
        MVAT_COA_CODES_DEBIT_NOTE.put(52, "Purchase Debit Note (Inter State Purchase against Form H) For Transaction Type 50");
        MVAT_COA_CODES_DEBIT_NOTE.put(57, "Purchase Debit Note (Within the State Purchases against H Form) for Transaction Type 55");
        MVAT_COA_CODES_DEBIT_NOTE.put(62, "Purchase Debit Note (Direct Import) for Transaction Type 60");
        MVAT_COA_CODES_DEBIT_NOTE.put(67, "Purchase Debit Note (High Seas ) For Transaction Type 65");
        MVAT_COA_CODES_DEBIT_NOTE.put(72, "Purchase Debit Note ( Inter State Purchases without Form) For Transaction Type 70");
        MVAT_COA_CODES_DEBIT_NOTE.put(77, "Purchase Debit Note (Inter State Purchases against Form I) For Transaction Type 75");
        //SALES
        MVAT_COA_CODES.put(100, "Sales to TIN Holder (Within the state or interstate excluding against Form / Declaration)");
        MVAT_COA_CODES.put(200, "Branch Transfer / Consignment (Within the State or Inter State)");
        MVAT_COA_CODES.put(300, "Purchase Debit Note (Inter State Branch Transfer) for Transaction Type 30");
        MVAT_COA_CODES.put(800, "Deduction u/s 3(2)");
        MVAT_COA_CODES.put(400, "Composition u/s 42(1), (2)");
        MVAT_COA_CODES.put(450, "Works Contract Composition u/s 42(3), (3A)");
        MVAT_COA_CODES.put(460, "On Going Works Contract");
        MVAT_COA_CODES.put(470, "On Going Lease Contract");
        MVAT_COA_CODES.put(480, "Amount of Sub Contract where Tax Paid by Sub Contractor");
        MVAT_COA_CODES.put(490, "Amount of Sub Contract where Tax Paid by Principal Contractor");
        MVAT_COA_CODES.put(500, "PSI Exempted Sales");
        MVAT_COA_CODES.put(900, "Sales Against C Form");
        MVAT_COA_CODES.put(910, "Sales Outside the State (Sales affected outside the State of Maharashtra)");
        MVAT_COA_CODES.put(920, "Sales in Transit");
        MVAT_COA_CODES.put(930, "Inter State Sales to Consulate");
        MVAT_COA_CODES.put(940, "Export on H Form");
        MVAT_COA_CODES.put(950, "Direct Export");
        MVAT_COA_CODES.put(960, "Import Sales (High Seas)");
        MVAT_COA_CODES.put(970, "Inter State Sales u/s 8(6), Form - I");
        //Goods Returned Transaction Codes
        MVAT_COA_CODES_SALES_RETURN.put(600, "Sales Goods Return for Transaction Types 100, 200");
        MVAT_COA_CODES_SALES_RETURN.put(680, "Sales Goods Return (Branch Transfer / Consignment) for Transaction Type 300");        
        MVAT_COA_CODES_SALES_RETURN.put(610, "Sales Goods Return (Outside the State) for Transaction Type 910");
        MVAT_COA_CODES_SALES_RETURN.put(620, "Sales Goods Return (Sales in Transit ) for Transaction Type 920");
        MVAT_COA_CODES_SALES_RETURN.put(630, "Sales Goods Return (Consulate ) for Transaction Type 930");
        MVAT_COA_CODES_SALES_RETURN.put(640, "Sales Goods Return (Export Against Form H ) for Transaction Type 940");
        MVAT_COA_CODES_SALES_RETURN.put(650, "Sales Goods Return (Direct Export) for Transaction Type 950");
        MVAT_COA_CODES_SALES_RETURN.put(660, "Sales Goods Return (Sale in Case of Import) for Transaction Type 960");
        MVAT_COA_CODES_SALES_RETURN.put(670, "Sales Goods Return (Sale against u/s 8 (6)) for Transaction Type 970");
        //Credit Note Codes
        MVAT_COA_CODES_CREDIT_NOTE.put(700, "Sales Credit Note for Transaction Types 100,200");
        MVAT_COA_CODES_CREDIT_NOTE.put(780, "Sales Credit Note (Branch Transfer) for Transaction Type 300");
        MVAT_COA_CODES_CREDIT_NOTE.put(710, "Sales Credit Note (Outside the State) for Transaction Type 910");
        MVAT_COA_CODES_CREDIT_NOTE.put(720, "Sales Credit Note (Sales in Transit ) for Transaction Type 920");
        MVAT_COA_CODES_CREDIT_NOTE.put(730, "Sales Credit Note (Consulate ) for Transaction Type 930");
        MVAT_COA_CODES_CREDIT_NOTE.put(740, "Sales Credit Note (Export Against Form H ) for Transaction Type 940");
        MVAT_COA_CODES_CREDIT_NOTE.put(750, "Sales Credit Note (Direct Export ) for Transaction Type 950");
        MVAT_COA_CODES_CREDIT_NOTE.put(760, "Sales Credit Note (Sale in Case of Import) for Transaction Type 960");
        MVAT_COA_CODES_CREDIT_NOTE.put(770, "Sales Credit Note(Sale against u/s 8 (6)) for Transaction Type 970");
        
        MVAT_TRANSCATION_CODES.put("231",1);
        MVAT_TRANSCATION_CODES.put("232",1);
        MVAT_TRANSCATION_CODES.put("233",1);
        MVAT_TRANSCATION_CODES.put("234",1);
        MVAT_TRANSCATION_CODES.put("235",1);
        MVAT_TRANSCATION_CODES.put("231_234",1);
        MVAT_TRANSCATION_CODES.put("233_234",1);
        MVAT_TRANSCATION_CODES.put("233_235",1);
        MVAT_TRANSCATION_CODES.put("CST",3);
        MVAT_TRANSCATION_CODES.put("231_CST",3);
        MVAT_TRANSCATION_CODES.put("233_CST",3);
        MVAT_TRANSCATION_CODES.put("234_CST",3);
        MVAT_TRANSCATION_CODES.put("235_CST",3);
        MVAT_TRANSCATION_CODES.put("231_234_CST",3);
        MVAT_TRANSCATION_CODES.put("233_234_CST",3);
        MVAT_TRANSCATION_CODES.put("233_235_CST",3);
    }
        // term type is uesd in indian TAX 1.VAT ,2.Excise Duty,3.CST,4.Service Tax,5.Swachh Bharat Cess,6.Krishi Kalyan Cess
        public static final int LINELEVELTERMTYPE_VAT = 1;
        public static final int LINELEVELTERMTYPE_Excise_DUTY = 2;
        public static final int LINELEVELTERMTYPE_CST = 3;
        public static final int LINELEVELTERMTYPE_SERVICE_TAX = 4;
        public static final int LINELEVELTERMTYPE_SBC = 5;
        public static final int LINELEVELTERMTYPE_KKC = 6;
        public static final int LINELEVELTERMTYPE_GST = 7;
        public static final int LINELEVELTERMTYPE_OTHERS = 0;
        public static final int INVOICE_WITHOUT_FORM = 1;
        public static final int INVOICE_C_FORM = 2;
        public static final int INVOICE_E1_FORM = 3;
        public static final int INVOICE_E2_FORM = 4;
        public static final int INVOICE_F_FORM = 5;
        public static final int INVOICE_H_FORM = 6;
        public static final int INVOICE_I_FORM = 7;
        public static final int INVOICE_J_FORM = 8;
        public static final int TAX_PAID = 1;
        public static final String REGISTERED_DEALER = "1";
        public static final String REGISTERED_VENROR_OR_CUSTOMER = "1";
        public static final String UNREGISTERED_VENROR_OR_CUSTOMER = "2";
        
        public static final String REPORT_NET_RS="Net Rs.";
        public static final String REPORT_TAXABLE_VALUE="Taxable Value OR Value of Composition u/s 42(3) , (3A), (4)";
        public static final String REPORT_TAX_IF_ANY="TAX (If any) Rs.";
        public static final String REPORT_UNDER_CENTRAL_ACT="Under Central Act";
        public static final String REPORT_UNDER_GENERAL_SALES_TAX="Under General Sales Tax/ Value Added Tax law of relevant State";
        public static final String REPORT_REGISTRATION_CERTIFICATE_NO="Registration Certificate No. of the transferor of goods";
        
        public static final String PAN_STATUS_PANNOTAVBL = "2";
        public static final String PAN_STATUS_APPLIEDFOR = "3";
        public static final String PAN_NOT_AVAILABLE="PANNOTAVBL";
        public static final String PAN_APPLIED_FOR="APPLIEDFOR";
        
        public static final String DVAT31_FORM_DVAT_31="Form DVAT 31";
        public static final String DVAT31_DEPARTMENT_VALUE_ADDED_TAX="Department of Value Added Tax";
        public static final String DVAT31_GOVEMENT_NCT_DELHI="Government of NCT of Delhi";
        public static final String DVAT31_SPECIMEN_OF_SALSE="Specimen of Sales / outward Branch Transfer Register";
        public static final String DVAT31_REGISTRATION_NO="Registration Number";
        public static final String DVAT31_NAME_OF_DEALER="Name of dealer";
        public static final String DVAT31_ADDRESS="Address";
        public static final String DVAT31_SALES_TAX_PERIOD="Sales for the tax period";
        public static final String DVAT31_CASH_ACCRUAL="Method of accounting: Cash / Accrual";
        public static final String DVAT31_DETAILS_OF_SALES="Details of Sales";
        public static final String DEDUCTEETYPE_UNKNOWN="unknown";
        public static final String NonLowerDedutionApplicable="1";//If applicable then value = 1
        public static final String Reason_Non_Deduction_or_Lower_Deduction="1";
        public static final String Reason_Non_Deduction_Declaration="2";
        public static final String Reason_Deduction_Transporter="3";
        public static final String Reason_Basic_Exemption_Reached="4";
        public static final String DEDUCTEETYPE_UNKNOWN_ID="79ed9c74-015d-11e6-ba66-14dda9792823";
        
        public static final int MAHARASHTRA_STATE_ID = 4;
        
        /*
        * Ticket :ERP-26370 Check Type of dealer Default Composition Dealer u/s
        * 42(1) ,(2). 1. Composition Dealer u/s 42(3) ,(3A) & (4) 2. Composition
        * Dealer u/s 42(1) ,(2).
        */
        public static final String CompositionDealeru423 = "3";
        public static final String CompositionDealeru421 = "4";
        public static final String TRUE_CHAR_T = "T";
        /**
         * Added Constants for INDIA compliance when 
         * line level terms used as Taxes.
         */
        public static final String ISLINE_LEVELTERM_FLAG = "isLineLevelTermFlag";
        public static final String ISEXCISEAPPLICABLE = "isExciseApplicable";
        public static final String ENABLEVATCST = "enablevatcst";
        public static final String SALES_OR_PURCHASE_FLAG = "salesOrPurchase";
        public static final String COMPANY_LINELEVEL_TERMS = "companyLineLevelTerms";
        public static final String EDUCATION_CESS = "education cess";
        public static final String HCESS = "hcess";
        public static final String HIGHER_EDUCATION_CESS = "higher education cess";
        public static final int ResidentialStatus_Resident = 0;
        public static final int ResidentialStatus_NonResident = 1;
        public static final SimpleDateFormat INDIAN_TEMPLATE_DATE_FORMATTER = new SimpleDateFormat("dd-M-yyyy hh:mm"); 
        
        public static final Map<String, String> valuationType = new HashMap<String, String>();
        static{
        valuationType.put("Ad Valorem Method", "valorem");
        valuationType.put("Quantity", "quantity");
        valuationType.put("Specific basis", "specific");
        valuationType.put("MRP (Maximum Retail Price)", "mrp");
        }
        
        public static final int LINELEVEL_FLAG_ON = 1;
        public static final int LINELEVEL_FLAG_OFF = 0;
        
        /*
        This Flag is used to print Row record Bold and Italic in Excel Export.
        It is generally used to make Sub-Total line Bold and Italic in Excel Export.
        */
        public static final String boldAndItalicFontStyleFlag = "boldAndItalicFont";
        public static final String DEALER_TYPE_REGISTERED = "1";
        public static final String DEALER_TYPE_UNREGISTERED = "2";
        public static final String DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4 = "3";
        public static final String DEALER_TYPE_COMPOSITION_DEALER_42_1_2 = "4";
        
        public static final String DEALER_TYPE_REGISTERED_STR = "Registered Dealer";
        public static final String DEALER_TYPE_UNREGISTERED_STR = "Unregistered Dealer";
        public static final String DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4_STR = "Composition Dealer u/s 42(3) ,(3A) & (4)";
        public static final String DEALER_TYPE_COMPOSITION_DEALER_42_1_2_STR = "Composition Dealer u/s 42(1) ,(2)";
        
        public static final String MANUFACTURER_TYPE_REGULAR = "1";
        public static final String MANUFACTURER_TYPE_SMALLSCALE = "2";
        public static final String MANUFACTURER_TYPE_REGULAR_STR = "Regular";
        public static final String MANUFACTURER_TYPE_SMALLSCALE_STR = "Small Scale Industries(SSI)";
        
        public static final String DEDUCTEE_CODE_COMPANY = "1";
        public static final String DEDUCTEE_CODE_OTHER_THAN_COMPANY = "2";
        public static final String DEDUCTEE_CODE_COMPANY_STR = "Company";
        public static final String DEDUCTEE_CODE_OTHER_THAN_COMPANY_STR = "Other Than Company";
        public static final String DEDUCTEE_CODE_CORPORATE_STR = "Corporate";
        public static final String DEDUCTEE_CODE_NONCORPORATE_STR = "Non-Corporate";
        public static final String SUPER_TECHNICAL_COMPANYID = "7a18cf45-1069-42e6-a08d-78562ba085e9";
        public static final int CREDIT_NOT_AVAILED_EXCISE_INVOICE = 0;
        public static final int CREDIT_AVAILED_EXCISE_INVOICE = 1;
        
        public static final int NOTDSPAID = 0;
        public static final int TDSPAYMENT = 1;
        public static final int TDSINTERESTPAYMENT = 2;
        public static final int TDSANDTDSINTERESTPAYMENT = 3;
        public static final int DEBIT_NOTE_TDS_APPLICABLE = 1;
        
        public static final int Term_TaxType_Flat = 0;
        public static final int Term_TaxType_Percentage = 1;
        
        public static final String DEDUCTEE_MASTERGROUP = "34";
        public static final String NATUREOFPAYMENT_MASTERGROUP = "33";
        public static final String GST_CESS_TYPE = "cessType";
        public static final String GST_CESS_VALUATION_AMOUNT = "valuationAmount";
        /**
         * GST CESS tax Calculation Type
         */
        public static final String NOT_APPLICABLE = "Not Applicable";
        public static final String PERCENTAGES = "Percentage";
        public static final String HIGHER_VALUE_OR_CESSPERCENTAGES = "Value per Thousand or CESS % whichever is higher";
        public static final String VALUE_AND_CESSPERCENTAGES = "Value per Thousand + CESS %";
        public static final String VALUE = "Value per Thousand";
        public static final String CESS = "CESS";
        public static final String DEFAULT_TERMID = "defaultTermID";

        public static final Map<String, String> CESSTYPE = new HashMap<String, String>();
        static {
            CESSTYPE.put(NOT_APPLICABLE, "699b94c6-c84d-11e7-bd73-c03fd5658531");
            CESSTYPE.put(PERCENTAGES, "699b94c6-c84d-11e7-bd73-c03fd5658532");
            CESSTYPE.put(HIGHER_VALUE_OR_CESSPERCENTAGES, "699b94c6-c84d-11e7-bd73-c03fd5658533");
            CESSTYPE.put(VALUE_AND_CESSPERCENTAGES, "699b94c6-c84d-11e7-bd73-c03fd5658534");
            CESSTYPE.put(VALUE, "699b94c6-c84d-11e7-bd73-c03fd5658535");
            
        }
      //  public static final String GST_CUSTOMERTYPEID = "NA, Deemed Export, SEZ (WPAY),SEZ (WOPAY)";
        public static final String GST_CUSTOMER_VENDORTYPEID_NA = "NA";
        public static final String GST_CUSTOMER_VENDORTYPEID_NA_SEZ = "NA, SEZ (WPAY),SEZ (WOPAY)";
        public static final String GST_VENDORTYPEID_NA_SEZWPAY = "NA, SEZ (WPAY)";
        public static final String GST_CUSTOMER_UNREGISTERED_TYPEID = "NA, Export (WPAY), Export (WOPAY)";
        public static final String GST_VENDOR_UNREGISTERED_TYPEID = "NA, Import";
        public static final String GST_REGISTRATION_TYPE = "Composition, Registered, Unregistered";
       // public static final String GST_CUST_UNREG_TYPEID = "NA, Deemed Export, SEZ (WPAY),SEZ (WOPAY),Export (WPAY), Export (WOPAY)";
        public static final String CustVenTypeDefaultMstrID = "CustVenTypeDefaultMstrID";
     
    /**
     * Get Table Joins
     */
    public static final Map<String, String> forModuleJoins = new HashMap<>();

    static {
        forModuleJoins.put(Constants.DODetailTermMap, " dodtm inner join dodetails dod on dodtm.dodetail = dod.id inner join deliveryorder do on dod.deliveryorder = do.id ");
        forModuleJoins.put(Constants.InvoiceDetailTermMap, " invdtm inner join invoicedetails ivd on invdtm.invoicedetail = ivd.id inner join invoice inv on ivd.invoice = inv.id ");
        forModuleJoins.put(Constants.PODetailTermMap, " podtm inner join podetails pod on podtm.podetails=pod.id inner join purchaseorder po on pod.purchaseorder=po.id  ");
        forModuleJoins.put(Constants.PRDetailTermMap, "   prdtm inner join prdetails prd on prdtm.purchasereturndetail = prd.id inner join purchasereturn pr on prd.purchasereturn= pr.id ");
        forModuleJoins.put(Constants.QuotationDetailTermMap, "    qdtm  inner join quotationdetails qd on qdtm.quotationdetail = qd.id inner join quotation q on qd.quotation=q.id ");
        forModuleJoins.put(Constants.RADetailTermMap, " rapdtm inner join receiptadvancedetail rapd on rapdtm.receiptadvancedetail = rapd.id inner join receipt rap on rapd.receipt = rap.id ");
        forModuleJoins.put(Constants.ReceiptDetailTermMap, " grdtm inner join grdetails grd on grdtm.goodsreceiptdetail = grd.id inner join goodsreceipt gr on grd.goodsreceipt = gr.id ");
        forModuleJoins.put(Constants.RODetailTermMap, "   grodtm inner join grodetails grod on grodtm.grodetail = grod.id inner join grorder gro on grod.grorder = gro.id ");
        forModuleJoins.put(Constants.SODetailTermMap, " sodtm inner join sodetails sod on sodtm.salesorderdetail = sod.id inner join salesorder so on sod.salesorder = so.id  ");
        forModuleJoins.put(Constants.SRDetailTermMap, "   srdtm inner join srdetails srd on srdtm.salesreturndetail = srd.id inner join salesreturn sr on srd.salesreturn =sr.id ");
        forModuleJoins.put(Constants.VQDetailTermMap, "  vqdtm inner join vendorquotationdetails vqd on vqdtm.vendorquotationdetails = vqd.id inner join vendorquotation vq on vqd.vendorquotation = vq.id ");
    }
    public static final Map<String, String> forModuleGroupConcat = new HashMap<>();

    static {
        forModuleGroupConcat.put(Constants.DODetailTermMap, " , GROUP_CONCAT(Distinct(do.donumber)) as documentNumber ");
        forModuleGroupConcat.put(Constants.InvoiceDetailTermMap, " , GROUP_CONCAT(Distinct(inv.invoicenumber)) as documentNumber "); //CUSTOMER OR VENDOR INVOICE
        forModuleGroupConcat.put(Constants.PODetailTermMap, " , GROUP_CONCAT(Distinct(po.ponumber)) as documentNumber ");
        forModuleGroupConcat.put(Constants.PRDetailTermMap, ", GROUP_CONCAT(Distinct(pr.prnumber))  as documentNumber ");
        forModuleGroupConcat.put(Constants.QuotationDetailTermMap, " , GROUP_CONCAT(Distinct(q.quotationnumber))  as documentNumber ");
        forModuleGroupConcat.put(Constants.RADetailTermMap, " , GROUP_CONCAT(Distinct(rap.receiptnumber))  as documentNumber ");
        forModuleGroupConcat.put(Constants.ReceiptDetailTermMap, " , GROUP_CONCAT(Distinct(gr.grnumber))  as documentNumber ");
        forModuleGroupConcat.put(Constants.RODetailTermMap, " ,  GROUP_CONCAT(Distinct(gro.gronumber))  as documentNumber ");
        forModuleGroupConcat.put(Constants.SODetailTermMap, " , GROUP_CONCAT(Distinct(so.sonumber))  as documentNumber  ");
        forModuleGroupConcat.put(Constants.SRDetailTermMap, " , GROUP_CONCAT(Distinct(sr.srnumber))  as documentNumber  ");
        forModuleGroupConcat.put(Constants.VQDetailTermMap, " , GROUP_CONCAT(Distinct(vq.quotationnumber))  as documentNumber ");
    }
    public static final String GLOBALTABLENAME = "globaltablename";
    public static final String DOCUMENT_NUMBER_COLUMN = "documentNumberColumn";
    public static final String MODULE_NAME = "modulename";
    public static final Map<Integer, JSONObject> globalTableNames = new HashMap<Integer, JSONObject>();

    static {
        try {
            JSONObject detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "deliveryorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "donumber");
            detailsObj.put(MODULE_NAME, "Delivery Order");
            globalTableNames.put(27, detailsObj);// deliveryordercustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "deliveryorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "donumber");
            detailsObj.put(MODULE_NAME, "Asset Delivery Order");
            globalTableNames.put(41, detailsObj);// deliveryordercustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "deliveryorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "donumber");
            detailsObj.put(MODULE_NAME, "Consignment Stock Delivery Order");
            globalTableNames.put(51, detailsObj);// deliveryordercustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "deliveryorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "donumber");
            detailsObj.put(MODULE_NAME, "Lease Delivery Order");
            globalTableNames.put(67, detailsObj);// deliveryordercustomdata          
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "grorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "gronumber");
            detailsObj.put(MODULE_NAME, "Goods Receipt Order");
            globalTableNames.put(28, detailsObj);// grordercustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "grorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "gronumber");
            detailsObj.put(MODULE_NAME, "Asset Goods Receipt Order");
            globalTableNames.put(40, detailsObj);// grordercustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "grorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "gronumber");
            detailsObj.put(MODULE_NAME, "Consignment Goods Receipt Order");
            globalTableNames.put(57, detailsObj);// grordercustomdata                
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "purchaseorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "ponumber");
            detailsObj.put(MODULE_NAME, "Purchase Order");
            globalTableNames.put(18, detailsObj);// purchaseordercustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "purchaseorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "ponumber");
            detailsObj.put(MODULE_NAME, "Consignment Request");
            globalTableNames.put(63, detailsObj);// purchaseordercustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "purchaseorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "ponumber");
            detailsObj.put(MODULE_NAME, "Asset Purchase Order");
            globalTableNames.put(90, detailsObj);// purchaseordercustomdata   
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "purchaseorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "ponumber");
            detailsObj.put(MODULE_NAME, "Stock Adjustment");
            globalTableNames.put(1115, detailsObj);// purchaseordercustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "salesorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "sonumber");
            detailsObj.put(MODULE_NAME, "Sales Order");
            globalTableNames.put(20, detailsObj);// salesordercustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "salesorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "sonumber");
            detailsObj.put(MODULE_NAME, "Lease Order");
            globalTableNames.put(36, detailsObj);// salesordercustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "salesorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "sonumber");
            detailsObj.put(MODULE_NAME, "Consignment Stock Sales Request");
            globalTableNames.put(50, detailsObj);// salesordercustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "salesorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "sonumber");
            detailsObj.put(MODULE_NAME, "Vendor Job Work Order");
            globalTableNames.put(1114, detailsObj);// salesordercustomdata  
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "vendorquotation");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "quotationnumber");
            detailsObj.put(MODULE_NAME, "Vendor Quotation");
            globalTableNames.put(23, detailsObj);// vendorquotationcustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "vendorquotation");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "quotationnumber");
            detailsObj.put(MODULE_NAME, "Asset Vendor Quotation");
            globalTableNames.put(89, detailsObj);// vendorquotationcustomdata     
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "quotation");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "quotationnumber");
            detailsObj.put(MODULE_NAME, "Quotation");
            globalTableNames.put(22, detailsObj);// quotationcustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "quotation");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "quotationnumber");
            detailsObj.put(MODULE_NAME, "Lease Quotation");
            globalTableNames.put(65, detailsObj);// quotationcustomdata   
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "salesreturn");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "srnumber");
            detailsObj.put(MODULE_NAME, "Sales Return");
            globalTableNames.put(29, detailsObj);// salesreturncustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "salesreturn");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "srnumber");
            detailsObj.put(MODULE_NAME, "Consignment Stock Sales Return");
            globalTableNames.put(53, detailsObj);// salesreturncustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "salesreturn");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "srnumber");
            detailsObj.put(MODULE_NAME, "Lease Return");
            globalTableNames.put(68, detailsObj);// salesreturncustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "salesreturn");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "srnumber");
            detailsObj.put(MODULE_NAME, "Asset Sales Return");
            globalTableNames.put(98, detailsObj);// salesreturncustomdata    
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "purchasereturn");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "prnumber");
            detailsObj.put(MODULE_NAME, "Purchase Return");
            globalTableNames.put(31, detailsObj);// purchasereturncustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "purchasereturn");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "prnumber");
            detailsObj.put(MODULE_NAME, "Consignment Purchase Return");
            globalTableNames.put(59, detailsObj);// purchasereturncustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "purchasereturn");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "prnumber");
            detailsObj.put(MODULE_NAME, "Asset Purchase Return");
            globalTableNames.put(96, detailsObj);// purchasereturncustomdata  
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "purchaserequisition");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "prnumber");
            detailsObj.put(MODULE_NAME, "Purchase Requisition");
            globalTableNames.put(32, detailsObj);// purchaserequisitioncustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "purchaserequisition");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "prnumber");
            detailsObj.put(MODULE_NAME, "Asset Purchase Requisition");
            globalTableNames.put(87, detailsObj);// purchaserequisitioncustomdata    
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "requestforquotation");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "rfqnumber");
            detailsObj.put(MODULE_NAME, "Request For Quotation");
            globalTableNames.put(33, detailsObj);// rfqcustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "requestforquotation");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "rfqnumber");
            detailsObj.put(MODULE_NAME, "Asset Request For Quotation");
            globalTableNames.put(88, detailsObj);// rfqcustomdata  
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "goodsreceipt");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "grnumber");
            detailsObj.put(MODULE_NAME, "Vendor Invoice");
            globalTableNames.put(6, detailsObj);// accjecustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "goodsreceipt");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "grnumber");
            detailsObj.put(MODULE_NAME, "Asset Purchase Invoice");
            globalTableNames.put(39, detailsObj);// accjecustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "goodsreceipt");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "grnumber");
            detailsObj.put(MODULE_NAME, "Consignment Purchase Invoice");
            globalTableNames.put(58, detailsObj);// accjecustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "invoice");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "invoicenumber");
            detailsObj.put(MODULE_NAME, "Customer Invoices");
            globalTableNames.put(2, detailsObj);// accjecustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "invoice");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "invoicenumber");
            detailsObj.put(MODULE_NAME, "Asset Disposal Invoice");
            globalTableNames.put(38, detailsObj);// accjecustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "invoice");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "invoicenumber");
            detailsObj.put(MODULE_NAME, "Consignment Stock Sales Invoice");
            globalTableNames.put(52, detailsObj);// accjecustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "invoice");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "invoicenumber");
            detailsObj.put(MODULE_NAME, "Lease Invoice ");
            globalTableNames.put(93, detailsObj);// accjecustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "payment");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "paymentnumber");
            detailsObj.put(MODULE_NAME, "Payment");
            globalTableNames.put(14, detailsObj);// accjecustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "receipt");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "receiptnumber");
            detailsObj.put(MODULE_NAME, "Receipt");
            globalTableNames.put(16, detailsObj);// accjecustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "debitnote");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "dnnumber");
            detailsObj.put(MODULE_NAME, "Debit Note");
            globalTableNames.put(10, detailsObj);// accjecustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "creditnote");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "cnnumber");
            detailsObj.put(MODULE_NAME, "Credit Note");
            globalTableNames.put(12, detailsObj);// accjecustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "journalentry");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "entryno");
            detailsObj.put(MODULE_NAME, "Journal Entry");
            globalTableNames.put(24, detailsObj);// accjecustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "product");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "productid");
            detailsObj.put(MODULE_NAME, "Product");
            globalTableNames.put(30, detailsObj);// accproductcustomdata
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "product");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "productid");
            detailsObj.put(MODULE_NAME, "Fixed Asset Group");
            globalTableNames.put(42, detailsObj);// accproductcustomdata     
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "fixedassetopening");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "documentnumber");
            detailsObj.put(MODULE_NAME, "Opening Fixed Asset Documents");
            globalTableNames.put(121, detailsObj);// assetdetailcustomdata   
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "mrpcontract");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "contractname");
            detailsObj.put(MODULE_NAME, "Master Contract");
            globalTableNames.put(1106, detailsObj);// mrpcontractcustomdata          
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "machine");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "machinename");
            detailsObj.put(MODULE_NAME, "Machine Master");
            globalTableNames.put(1103, detailsObj);// machinecustomdata                
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "workorder");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "workordername");
            detailsObj.put(MODULE_NAME, "Work Order");
            globalTableNames.put(1105, detailsObj);// workordercustomdata              
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "routing_template");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "routecode");
            detailsObj.put(MODULE_NAME, "Routing Template");
            globalTableNames.put(1107, detailsObj);// routingtemplatecustomdata        
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "workcenter");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "name");
            detailsObj.put(MODULE_NAME, "Work Center Master");
            globalTableNames.put(1102, detailsObj);// workcentrecustomdata   
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "mrp_job_order");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "jobordername");
            detailsObj.put(MODULE_NAME, "Job Work");
            globalTableNames.put(1104, detailsObj);// jobworkcustomdata                
            detailsObj = new JSONObject();
            detailsObj.put(GLOBALTABLENAME, "labour");
            detailsObj.put(DOCUMENT_NUMBER_COLUMN, "empcode");
            detailsObj.put(MODULE_NAME, "Labour");
            globalTableNames.put(1101, detailsObj);// labourcustomdata   
        } catch (JSONException ex) {
            Logger.getLogger(IndiaComplianceConstants.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static final int EWAY_BILL_IMPORT_MODULEID = 1300;
    public static final String EWAY_BILL_IMPORT_MODULENAME = "E-Way Bill details";
}
