/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.gst.services;

import com.krawler.common.util.Constants;
import java.util.HashMap;
import java.util.Map;

public class GSTRConstants {

    public static final String customerid = "customerid";
    public static final String gstin = "gstin";
    public static final String invoiceid = "invoiceid";
    public static final String invoicenumber = "invoicenumber";
    public static final String jeid = "jeid";
    public static final String entrydate = "entrydate";
    public static final String invoicedetailid = "invoicedetailid";
    public static final String rate = "rate";
        public static final String cnrate = "cnrate";
    public static final String quantity = "quantity";
    public static final String cnquantity = "cnquantity";
    public static final String productid = "productid";
    public static final String term = "term";
    public static final String cnterm = "cnterm";
    public static final String supplierinvoiceno = "supplierinvoiceno";
    public static final String vendorname = "vendorname";
    public static final String separator = "--";
    public static final String termamount = "termamount";
    public static final String cntermamount = "cntermamount";
    public static final String invamountinbase = "invamountinbase";
    public static final String hsncode = "hsncode";
    public static final String hsnid = "hsnid";
    public static final String pos = "pos";
    public static final String productdesc = "productdesc";
    public static final String posid = "posid";
    public static final String taxrate = "taxrate";
    public static final String cntaxrate = "cntaxrate";
    public static final String defaultterm = "defaultterm";
    public static final String cndefaultterm = "cndefaultterm";
    public static final String discpercentage = "discpercentage";
    public static final String cndiscpercentage = "cndiscpercentage";
    public static final String discountvalue = "discountvalue";
    public static final String cndiscountvalue = "cndiscountvalue";
    public static final String cndnnumber = "cndnnumber";
    public static final String cndndate = "cndndate";
    public static final String cnid = "cnid";
    public static final String cndid = "cndid";
    public static final String customer = "customer";
    public static final String customername = "customername";
    public static final String CustomerName="Customer Name";
    public static final String receiptnumber = "receiptnumber";
    public static final String receiptid = "receiptid";
    public static final String receiptdate = "receiptdate";
    public static final String cnamountinbase = "cnamountinbase";
    public static final String receiptamount = "receiptamount";
    public static final String receiptamountdue = "receiptamountdue";
    public static final String receiptlinkdate = "receiptlinkdate";
    public static final String receipttaxamount = "receipttaxamount";
    public static final String receiptadvanceid = "receiptadvanceid";
    public static final String adjustedamount = "adjustedamount";
    public static final String refundamount = "refundamount";
    public static final String refunddate = "refunddate";
    public static final String statecode = "stcode";
    public static final String gstregtype = "inv_typ";
    public static final String GSTREG_TYPEID = "gstregtypeid";
    public static final String GSTCustomer_Vendor_TYPEID = "custvendtypeid";
    public static final String GSTDETAILS_TYPEVALUE = "custvendtypeid";
    public static final String exportType = "export_type";
    public static final String gstcusttype = "custType";  
    public static final String SEZWPAYB2B="SEZ supplies with payment";
    public static final String SEZWOPAYB2B="SEZ supplies without payment";
    public static final String DeemedExportB2B="Deemed Exp"; 
    public static final String RegularB2B="Regular";
    public static final String ExportWPAYCDNUR="EXPWP";
    public static final String ExportWOPAYCDNUR="EXPWOP";
    public static final String B2cl="B2CL";
    public static final String supplierName="Supplier Name";
    public static final String supplierGSTIN="Supplier GSTIN/UIN";
    public static final String supplierType="Supplier Type";
    public static final String billNo="Bill Of Entry Number";
    public static final String billDate="Bill Of Entry Date";
    public static final String invoiceDate="Invoice Date";
    public static final String status="Status";
    public static final String voucherNo="Voucher No.";
    public static final String voucherDate="Voucher Date";
    public static final String voucherType="Voucher Type";
    public static final String ITC_available_IGST="ITC Available IGST Amount";
    public static final String ITC_available_CGST="ITC Available CGST Amount";
    public static final String ITC_available_SGST="ITC Available SGST Amount";
    public static final String ITC_available_cess="ITC Available Cess Amount";
    public static final String Doc_Type="documenttype";
    public static final String eltrTaxRate="eltrTaxRate";
    public static final String invTermRate="invTermRate";
    public static final String INV_TERM_TYPE="invtermtype";
    public static final String code="code";
    public static final String otherTerritory= "97-Other Territory";

    public static final String vendorid = "vendorid";
    public static final String dnnumber = "dnnumber";
    public static final String dndate = "dndate";
    public static final String prnumber = "prnumber";
    public static final String reason = "reason";
    public static final String dnamountinbase = "dnamountinbase";
    public static final String Reason_Description="Reason Description";
    public static final String NT_TYPE="nttype";
    public static final String CNDNREASON="cndnreason";
    public static final String SupplierInvoiceNumber="Supplier Invoice Number";

    
    // Export to excel 
    public static final String Summary_For_B2B = "Summary For B2B(4)";
    public static final String Summary_For_B2CL = "Summary For B2CL(5)";
    public static final String Summary_For_B2CS = "Summary For B2CS(7)";
    public static final String Summary_For_CDNR = "Summary For CDNR(9B)";
    public static final String Summary_For_CDNUR = "Summary For CDNUR(9B)";
    public static final String Summary_For_EXP = "Summary For EXP(6)";
    public static final String Summary_For_AT = "Summary For Advance Received (11B)";
    public static final String Summary_For_EXEMP = "Summary For Nil rated, exempted and non GST outward supplies (8)";
    public static final String Summary_For_HSN = "Summary For HSN(12)";
    public static final String Summary_For_DOCS = "Summary of documents issued during the tax period (13)";
    public static final String No_of_Recipients = "No. of Recipients";
    public static final String No_of_Invoices = "No. of Invoices";
    public static final String Total_Invoice_Value = "Total Invoice Value";
    public static final String Total_Taxable_Value = "Total Taxable Value";
    public static final String Total_Tax = "Total Tax";
    public static final String Total_Cess = "Total Cess";
    public static final String GSTIN_UINof_Recipient = "GSTIN/UIN of Recipient";
    public static final String Invoice_Number = "Invoice Number";
    public static final String Receiver_Name ="Receiver Name";
    public static final String docType = "Type";
    public static final String Invoice_Date = "date";
    public static final String Date="Date";
    public static final String TOTALAMT = "Total Amount";
    public static final String JENO = "Journal Entry No";
    public static final String Invoice_Value = "Invoice Value";
    public static final String Place_Of_Supply = "Place Of Supply";
    public static final String Reverse_Charge = "Reverse Charge";
    public static final String Invoice_Type = "Invoice Type";
    public static final String E_Commerce_GSTIN = "E-Commerce GSTIN";
    public static final String Rate = "Rate";
    public static final String Taxable_Value = "Taxable Value";
    public static final String Taxable_Amount = "Taxable Amount";
    public static final String IGST_Value = "Integrated Tax Amount";
    public static final String CGST_Value = "Central Tax Amount";
    public static final String SGST_Value = "State Tax Amount";
    public static final String Cess_Amount = "Cess Amount";
    public static final String TYPE = "Type";
    public static final String No_of_Notes_Vouchers = "No. of Notes/Vouchers";
    public static final String Total_Note_Refund_Voucher_Value = "Total Note/Refund Voucher Value";
    public static final String Invoice_Advance_Receipt_Number = "Invoice/Advance Receipt Number";
    public static final String Invoice_Advance_Receipt_date = "Invoice/Advance Receipt date";
    public static final String Note_Refund_Voucher_Number = "Note/Refund Voucher Number";
    public static final String Note_Refund_Voucher_Date = "Note/Refund Voucher date";
    public static final String Document_Type = "Document Type";
    public static final String Reason_For_Issuing_document = "Reason For Issuing document";
    public static final String Note_Refund_Voucher_Value = "Note/Refund Voucher Value";
    public static final String Pre_GST = "Pre GST";
    public static final String Total_Note_Value = "Total Note Value";
    public static final String UR_Type = "UR Type";
    public static final String No_ofShipping_Bill = "No. of Shipping Bill";
    public static final String Export_Type = "Export Type";
    public static final String Port_Code = "Port Code";
    public static final String Shipping_Bill_Number = "Shipping Bill Number";
    public static final String Shipping_Bill_Date = "Shipping Bill Date";
    public static final String Total_Advance_Received = "Total Advance Received";
    public static final String Gross_Advance_Received = "Gross Advance Received";
    public static final String Total_Advance_Adjusted = "Total Advance Adjusted";
    public static final String Gross_Advance_Adjusted = "Gross Advance Adjusted";
    public static final String Total_Nil_Rated_Supplies = "Total Nil Rated Supplies";
    public static final String Total_Exempted_Supplies = "Total Exempted Supplies";
    public static final String Total_NonGST_Supplies = "Total Non-GST Supplies";
    public static final String Nil_Rated_Supplies = "Nil Rated Supplies";
    public static final String Exempted_other_than_nil = "Exempted (other than nil rated/non GST supply )";
    public static final String NonGST_Supplies = "Non-GST Supplies";
    public static final String Description = "Description";
    public static final String NilRatedSupplies = "Nil Rated Supplies";
    public static final String Exempted = "Exempted (other than nil rated/non GST supply )";
    public static final String TotalNumber  = "Total Number";
    public static final String NonGstSupplies = "Non-GST supplies";
    public static final String No_of_HSN = "No. of HSN";
    public static final String Total_Value = "Total Value";
    public static final String Total_Integrated_Tax = "Total Integrated Tax";
    public static final String Total_Central_Tax = "Total Central Tax";
    public static final String Total_State_UT_Tax = "Total State/UT Tax";
    public static final String HSN = "HSN";
    public static final String UQC = "UQC";
    public static final String Total_Quantity = "Total Quantity";
    public static final String Integrated_Tax_Amount = "Integrated Tax Amount";
    public static final String Central_Tax_Amount = "Central Tax Amount";
    public static final String State_UT_Tax_Amount = "State/UT Tax Amount";
    public static final String Total_Number = "Total Number";
    public static final String Total_Cancelled = "Total Cancelled";
    public static final String Nature_of_Document = "Nature of Document";
    public static final String Sr_No_From = "Sr. No. From";
    public static final String Sr_No_To = "Sr. No. To";
    public static final String Cancelled = "Cancelled";
    public static final String Section = "Section";
    public static final String Headings = "Headings";
    public static final String TypeofSalesPurchases = "Type of Sales/Purchases";
    public static final String Placeofsupply = "Place of supply";
    public static final String TaxableValue = "Taxable Value";
    public static final String IntegratedTaxAmount = "Integrated Tax Amount";
    public static final String CentralTaxAmount = "Central Tax Amount";
    public static final String StateTaxAmount = "State Tax Amount";
    public static final String CessAmount = "Cess Amount";
    public static final String TaxAmount = "Tax Amount";
    public static final String TotalAmount = "Total Amount";
    public static final String ECommerceOperator="ECommerceOperator";
    public static final String ECommerceGstin="ECommerceGstin";
    public static final String From = "Sr.No.From";
    public static final String To = "Sr.No.To";
    public static final String No_of_Documents="Total Number";
    public static final String HSN_code="HSN Code";
    public static final String Total_Tax_Amount="Total Tax Amount";
    public static final String Total_Amount_Incl_Taxes="Total Amount Incl Tax";
    public static final String GSTIN="GSTIN";
    public static final String Debit_Note="Debit Note";
    public static final String Credit_Note="Credit Note";
    public static final String Exempt_otherthan_Nil_rated="Exempted(other than Nil Rated Supplies/non-GST)";
    public static final String Credit_Debit_Note_Number="Credit/Debit Note Number";
    public static final String Credit_Debit_Note_Date="Credit/Debit Note Date";
    
    public static int Comparison_ALL = 0;
    public static int Comparison_MATCHED = 1;
    public static int Comparison_NOT_MATCHED = 2;
    public static int Comparison_PRESENT_IN_SYSTEM = 3;
    public static int Comparison_PRESENT_IN_IMPORT = 4;
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String GET = "GET";
    
    // REST Method names
    public static final String baseURL = "https://test.nsdlgsp.co.in/";
    public static final String GSTR1URL = "NGCSGSP/callApi/taxpayerapi/v0.3/returns/gstr1";
    public static final String RETURNS = "NGCSGSP/callApi/taxpayerapi/v0.3/returns";
    public static final String getKey = "GSPUtility/getKey";
    public static final String AUTHENTICATE = "NGCSGSP/callApi/taxpayerapi/v0.2/authenticate";
    public static final String contentType = "application/json";
    public static final String stateCd = "27";
    public static final String ip = "192.168.0.35";
    public static final String username = "Krawler.MH.TP.2";
    public static final String aspgstinmodifed = "27ALZPD0023E1ZD"; // modified
    public static final String aspgstin = "27GSPMH7112G1ZP"; // Provided by NSDL - registered as ASP
    public static final String aspId = "27AACCK5779R034790";
    public static final String aspSecret = "518wd05a9Ej16H5U0q09Y1gKj82C9SjI";
    
    //sanbox testing data
    public static final String appKey = "XucYdK+0cQamyYwHXsosuLJNRPkaw4l5XVHm6wJ9m7A=";
    public static final String encryptAppKey = "efjVWuijbDJGQpvL+XH756jfYTtI+0IZiRGsU7r37CN3sD8Gu7De3QmZbMwRfKI3EMzBJBO1ynUXzF34mL1VjR69IlPv/fN1YCyxtawQhIwwcQ1sNCjEtpLY5POO6C7GwnSOy8fcDguj5gHID6mmPRoKPgcTVVmhSABvcxOU1tfrW70ehX/ZO2lk/AtlPSxqiMUbUI04vDz1kBaWAOCBxSSp45E8O+EKUjEa0vS2Qmx3htE26B5pf103e8xV9gYohOlSQS19l8mgiNsLRMvzBw6tktXdIcfybdyWNz5xOiRwJQAlr7RIozPIgF2ZyTCaB5M7WpZPsO2QEOsGkY5bug==";

    // values for api call after authtoken
    public static final String authToken = "7d7740e9853c476782f86d1e9245cad0";
    public static final String encryptAspSecret = "aCaFGlT+VzUPee2bQ+yuKLOklAHV1s8UMxvJonGhNL8JUwA4PDVD/lE5aRK5B3Df";
    public static final String sessionId = "FPNHY5UDT6BSXK0MXMX1505294156029";
    public static final String sek = "WoT2QFB2Z4EX7F6DNaVsDEWqGZVisP2stVFVFtEEfHkUjGLcqKMpCbIGJO/LbSmL";
    
    
    public static final String clientid = "l7xxb46ccb7662284129ae2c5ee2f20001d4";
    public static final String clientSecret = "5649bfa3f36549d5b2d3dd5765501617";
    public static final String B2B="B2B Invoices (4A,4B,4C,6B,6C)";
    public static final String B2CL="B2C Large Invoices (5A, 5B)";
    public static final String B2CS="B2C Small Invoices";
    public static final String CDNR="Credit / Debit Note (Registered - 9B)";
    public static final String CDNUR="Credit / Debit Note (UnRegistered - 9B)";
    public static final String EXPORT="Export Invoices (6A)";
    public static final String AT="Tax Liability(Advances received) - 11A(1), 11A(2)";
    public static final String ATADJ="Adjustment of Advances - 11B(1), 11B(2)";
    public static final String EXEMPT="Nil Rated Invoices - 8A, 8B, 8C, 8D";
    public static final String HSNSummary="HSN Wise Summary";
    public static final String DOCSSummary="DOCS";
    public static final String HSN_SC="hsn_sc";
    public static final String ASSET_HSNCOLUMN="assethsncolnum";
    public static final String ASSET_TAXCLASSCOLUMN="assettaxclasscolnum";
    public static final String ASSET_UQCCOLUMN="assetuqccolnum";
    public static final String ASSET_STATE_SALES_COLUMN="assetstatecolnum";
    public static final String IS_PRODUCT_TAX_ZERO="isProductTaxZero";
    public static final String ASSET_STATE_PURCHASE_COLUMN="assetpurchasestatecolnum";
    public static final String ASSET_SALES_INVOICE_ENTITYCOLUMN="assetinvoiceentitycolnum"; // Disposal Invoice
    public static final String ASSET_SALES_INVOICE_ENTIYVALUE="assetinvoiceentityValue"; // Disposal Invoice
    public static final String ASSET_PURCHASE_INVOICE_ENTITYCOLUMN="assetpurchaseinvoiceentitycolnum"; // Acquired Invoice
    public static final String ASSET_PURCHASE_INVOICE_ENTIYVALUE="assetpurchaseinvoiceentityValue"; // Acquired Invoice
    public static final String ASSET_DISPOSAL_STATECOLNUM="assetdisposalstatecolnum";
    public static final String ASSET_DISPOSAL_STATEVALUE="assetdisposalstatevalue";
    public static final String Lease_Sales_STATEVALUE="leasesalesstatevalue";
    public static final String Lease_HSNCOLUMN="leasehsncolnum";
    public static final String Lease_TAXCLASSCOLUMN="assetdisposalstatevalue";
    public static final String Lease_UQCCOLUMN="leaseuqccolnum";
    public static final String Lease_STATE_SALES_COLUMN="leasestatecolnum";
    public static final String Lease_SALES_INVOICE_ENTIYVALUE="leaseinvoiceentityValue";
    public static final String Lease_SALES_INVOICE_ENTITYCOLUMN="leaseinvoiceentitycolnum";
    public static final String CN_STATE_COLUMN="cnstatecolumn";
    public static final String DN_STATE_COLUMN="dnstatecolumn";
    
    public static final String GSTR2_B2B="B2B Invoices - 3, 4A";
    public static final String GSTR2_B2B_unregister="B2BUR Invoices - 4B";
    public static final String GSTR2_CDN="Credit/Debit Notes Regular - 6C";
    public static final String GSTR2_ImpServices="Import of Services - 4C";
    public static final String GSTR2_ImpGoods="Import of Goods - 5";
    public static final String GSTR2_CDN_unregister="Credit/Debit Notes Unregistered - 6C";
    public static final String GSTR2_nilRated="Nil Rated Invoices - 7 - (Summary)";
    public static final String GSTR2_AdvancePaid="Advance Paid -10A - (Summary)";
    public static final String GSTR2_AdvanceAdjust="Adjustment of Advance - 10B - (Summary)";
    public static final String GSTR2_ToBeReconciledWithTheGSTPortal="To be reconciled with the GST portal";
    public static final String GSTR2_ToBeUploadedOnTheGSTPortal="To be uploaded on the GST portal";
    
    public static final String creditNoteNo="Credit Note No.";
    public static final String originalInvoiceDate="Original Invoice Date";
    public static final String InvoiceDate="Invoice Date";
    
    public static final String GSTR3B_INTERSSUPPLY_UNREG_DETAILS = "unreg_details";
    public static final String GSTR3B_INTERSSUPPLY_COMPOSITION_DETAILS = "comp_details";
    public static final String GSTR3B_INTERSSUPPLY_UIN_DETAILS = "uin_details";
    
    public static final String GSTR3B_SECTION_3_1_A = "(3.1.A) Outward taxable supplies (other than zero rated, nil rated and exempted)";
    public static final String GSTR3B_SECTION_3_1_B = "(3.1.B) Outward taxable supplies (zero rated)";
    public static final String GSTR3B_SECTION_3_1_C = "(3.1.C) Other outward supplies, (Nil rated, exempted)";
    public static final String GSTR3B_SECTION_3_1_D = "(3.1.D) Inward supplies (liable to reverse charge)";
    public static final String GSTR3B_SECTION_3_1_E = "(3.1.E) Non GST outward supplies";
    public static final String GSTR3B_SECTION_3_2_A = "(3.2.A) Supplies made to Unregistered Persons";
    public static final String GSTR3B_SECTION_3_2_B = "(3.2.B) Supplies made to Composition Taxable Persons";
    public static final String GSTR3B_SECTION_3_2_C = "(3.2.C) Supplies made to UIN Holders";
    public static final String GSTR3B_SECTION_4_A_1 = "(4.A.1) Import of goods";
    public static final String GSTR3B_SECTION_4_A_2 = "(4.A.2) Import of services";
    public static final String GSTR3B_SECTION_4_A_3 = "(4.A.3) Inward supplies liable to reverse charge (other than 1 & 2 above)";
    public static final String GSTR3B_SECTION_4_A_4 = "(4.A.4) Inward supplies from ISD";
    public static final String GSTR3B_SECTION_4_A_5 = "(4.A.5) All other ITC";
    public static final String GSTR3B_SECTION_4_B_1 = "(4.B.1) As per rules 42 & 43 of CGST Rules";
    public static final String GSTR3B_SECTION_4_B_2 = "(4.B.2) Others";
    public static final String GSTR3B_SECTION_4_D_1 = "(4.D.1) As per section 17(5)";
    public static final String GSTR3B_SECTION_4_D_2 = "(4.D.2) Others";
    public static final String GSTR3B_SECTION_5_1 = "(5.1) Exempted supplies";
    public static final String GSTR3B_SECTION_5_2 = "(5.2) Non GST supplies";
    public static final String GSTR3B_SECTION_5_3 = "(5.3) NIL Rated supplies";
    public static final String GSTR3B_SECTION_5_4 = "(5.4) Composition Supplies";
    
    /**
     * GST Computation sections
     */
    public static final String GST_COMPUTATION_SECTION_SALES_1 = "Reg. sales";
    public static final String GST_COMPUTATION_SECTION_SALES_1_1 = "Reg. sales to RCM";
    public static final String GST_COMPUTATION_SECTION_SALES_1_2 = "Supplies made to  Composition Person (Inter State)";
    public static final String GST_COMPUTATION_SECTION_SALES_1_3 = "Supplies made to  Composition Person (Local/Intra State)";
    public static final String GST_COMPUTATION_SECTION_SALES_2 = "Inter State Un-registered sales more than 2.5 Lac";
    public static final String GST_COMPUTATION_SECTION_SALES_2_2 = "Inter State < or = 2,50,000 , Intra State any Value";
    public static final String GST_COMPUTATION_SECTION_SALES_3 = "Zero rated supplies and Deemed Exports (Unreg)";
    public static final String GST_COMPUTATION_SECTION_SALES_3_1 = "Zero rated supplies and Deemed Exports (Reg)";
    public static final String GST_COMPUTATION_SECTION_SALES_4 = "Nil rated supplies (Include Registered, Un-registered, local, interstate)";
    public static final String GST_COMPUTATION_SECTION_SALES_4_1 = "Exempted supplies (Include Registered, Un-registered, local, interstate)";
    public static final String GST_COMPUTATION_SECTION_SALES_4_2 = "Non GST outward supplies (Include Registered, Un-registered, local, interstate)";
    public static final String GST_COMPUTATION_SECTION_SALES_7 = "(A) Regular-Debit Notes against Sales ";
    public static final String GST_COMPUTATION_SECTION_SALES_7_1 = "(B) Unregistered-Debit Notes against Sales ";
    public static final String GST_COMPUTATION_SECTION_SALES_7_2 = "(C) Composition-Debit Notes against Sales ";
    public static final String GST_COMPUTATION_SECTION_SALES_7_3 = "(A) Regular-Credit Notes against Sales";
    public static final String GST_COMPUTATION_SECTION_SALES_7_4= "(B) Unregistered-Credit Notes against Sales";
    public static final String GST_COMPUTATION_SECTION_SALES_7_5 = "(C) Composition- Credit Notes against Sales";
    public static final String GST_COMPUTATION_SECTION_SALES_8 = "Refund Voucher to Customer";
//    public static final String GST_COMPUTATION_SECTION_SALES_9_4 = "Revised Invoice ";
    public static final String GST_COMPUTATION_SECTION_SALES_9 = "(A) Regular-Advance Receipt (Local, Interstate)";
    public static final String GST_COMPUTATION_SECTION_SALES_9_1 = "(B) Unregistered-Advance Receipt (Local, Interstate)";
    public static final String GST_COMPUTATION_SECTION_SALES_9_2 = "(C) Composition- Advance Receipt (Local, Interstate)";
    public static final String GST_COMPUTATION_SECTION_SALES_10 = "(A) Regular-Adjustment of Advance Receipt with invoices (Local, Interstate)";
    public static final String GST_COMPUTATION_SECTION_SALES_10_1 = "(B) Unregistered-Adjustment of Advance Receipt with invoices (Local, Interstate)";
    public static final String GST_COMPUTATION_SECTION_SALES_10_2 = "(C) Composition- Adjustment of Advance Receipt with invoices (Local, Interstate)";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_1 = "Reg. Purchase invoices";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_1_1 = "Reg. RCM Purchase Invoices";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_2 = "Unregisterred Purchase Invoices";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_3 = "Import Purchase Invoices of Services (out of India)";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_3_1 = "Import Purchase invoices of Input/Capital goods From out of India";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_3_2 = "Import Purchase invoices of Input/Capital goods from SEZ";
//    public static final String GST_COMPUTATION_SECTION_PURCHASES_6_1 = "Amendment in 3 & 4.1, 4.2 & 4.3";
//    public static final String GST_COMPUTATION_SECTION_PURCHASES_6_2 = "Amendment in 5.1 & 5.2";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_4 = "Debit Note";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_5 = "Credit Note";
//    public static final String GST_COMPUTATION_SECTION_PURCHASES_6_5 = "DN/CN (Amended)";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_6 = "Purchases from Composition type of taxable person";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_7 = "Exempted Purchase invoices";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_7_1 = "NIL rated Purchase invoices";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_7_2 = "Non GST Purchase invoices";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_8 = "ISD Invoices";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_8_1 = "ISD CN";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_9 = "Capture TDS related purchase invoices here";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_10 = "Capture TCS related purchase invoices here";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_11 = "Advance Paid amount against RCM (Inter State)";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_11_10 = "ITC  Reversed as per rules 42 & 43 of CGST Rules";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_13_1 = "Ineligible ITC as per section 17(5)";
    public static final String GST_COMPUTATION_SECTION_PURCHASES_12 = "Adjusted advances against invoices mentioned in 4.1, 4.2, 4.3 (Intra/local purchase invoices)";
    
    /**
     * GST Mismatch Report Section Name
     * SDP-14420
     */
    public static final String GSTMisMatch_SECTION_HSNNotAvailable = "HSN Not Available";
    public static final String GSTMisMatch_SECTION_UQCNotAvailable = "UQC Not Available";
    public static final String GSTMisMatch_SECTION_GSTINBlank = "GSTIN Blank";
    public static final String GSTMisMatch_SECTION_GSTINnonBlank = "GSTIN non Blank";
    public static final String GSTMisMatch_SECTION_GSTRegistrationTypeblank = "GST Registration Type blank";
    public static final String GSTMisMatch_SECTION_CustomerVendorTypeblank = "Customer or Vendor Type blank";
    public static final String GSTMisMatch_SECTION_RCMSalestoUnregisteredPerson = "RCM Sales to Unregistered Person";
    public static final String GSTMisMatch_SECTION_ProducttaxMismatch = "Product tax class Mismatch";
    public static final String GSTMisMatch_SECTION_RoundMismatch = " Rounded off Tax Value ";
    public static final String GSTMisMatch_SECTION_StateMismatch = "State Mismatching in Address";
    public static final String GSTMisMatch_SECTION_ManuallyenteredInvoiceNumber = "Manually entered Invoice Number";
    public static final String Sec_GST_History_Not_Present = "GST History Not Present";
    public static final String GSTMisMatch_SECTION_INVALIDCN = "CNDN Without Linking Invoice";
    public static final String GSTMisMatch_SECTION_GSTINInvalid="Invalid GSTIN";
   
    
    public static final int GSTR3B_TRANSACTION_TYPE_INVOICE = 0;
    public static final int GSTR3B_TRANSACTION_TYPE_NOTE = 1;
    public static final int GSTR3B_TRANSACTION_TYPE_PAYMENT_RECEIPT = 2;
    public static final int GSTR3B_TRANSACTION_TYPE_ALL = 3;
    public static final int HSNMaxLength= 8;
    public static final String GSTCNCN_REASON="Reason";
    public static final String GSTR1_SHIPPING_PORT = "Shippingport";
    public static final String GSTR1_SHIPPING_DATE = "Shippingdate";
    public static final String GSTR1_SHIPPING_BILL_NO = "Shippingbillnumber";
    public static final String GSTR1_DATEFORMAT = "dd-MM-YYYY";
    public static final String HSN_NotAvailable = "HSN Code is not available for product";
    public static final String UQC_NotAvailable = "Unit Quantity Code is not available for product";
    public static final String GSTIN_NotAvailable = "GSTIN is not available for customer/vendor";
    public static final String GSTIN_Available = "GSTIN should be NULL for customer/vendor";
    public static final String GST_Registration_NotAvailable = "GST registration type is not available for customer/vendor";
    public static final String GST_Registration_WronglyTagged = "GST registration type is wrongly tagged for customer/vendor";
    public static final String GST_Customertype_WronglyTagged = "GST customer type is wrongly tagged for customer/vendor";
    public static final String GST_TYPE_COLUMN_NAME = "Type(Customer/Vendor Type,GST Registration Type)";
    public static final String GST_Customertype_NotAvailable = "GST customer type is not available for customer/vendor";
    public static final String RCM_Sale= "RCM sale to unregistered customer/vendor";
    public static final String ProductTaxClass_Mismatch = "Product Tax Class is mismatching";
    public static final String BillingState_Mismatch = "Billing State of customer/vendor is mismatching";
    public static final String RoundOff_Mismatch = "Tax amount is rounded Off";
    public static final String Manually_EnteredInvNo = "Invoice Number is not generated using sequence format";
    public static final String GST_HistoryNotPresent = "Document's GST History not present";
    public static final String CNWithoutLinkingInvoice = "CN/DN Without Linking Invoice";
    public static final String GSTIN_Invalid = "GSTIN number for Customer/Vendor is invalid";
    public static final String HSN_Invalid = "Invalid HSN Code (upto 8 digit allowed)";
    public static final String isAddressNotFromVendorMaster = "isAddressNotFromVendorMaster";
      
 
    public static final String ADD_LANDEDCOST_JOIN_FOR_IMPORT_INVOICES="addLandedCostJoin";
    public static final String JEID = "jeid";
    public static final String JENUMBER = "jeno";
    public static final String JEDATE = "jedate";
    public static final String JEDAMOUNT = "jeamount";
    public static final String JE_DEFAULT_ACC = "jedefaultaccount";
    public static final Map<String, String> GST_ACCOUNT_Name = new HashMap<String, String>();

    static {
        GST_ACCOUNT_Name.put("InputCGST", "31ce3ba6-5f2b-11e7-907b-a6006ad3dba0");
        GST_ACCOUNT_Name.put("InputSGST", "31ce3d54-5f2b-11e7-907b-a6006ad3dba0");
        GST_ACCOUNT_Name.put("InputIGST", "31ce3c78-5f2b-11e7-907b-a6006ad3dba0");
        GST_ACCOUNT_Name.put("InputUTGST", "31ce3e30-5f2b-11e7-907b-a6006ad3dba0");
        GST_ACCOUNT_Name.put("InputCESS", "31ce3aca-5f2b-11e7-907b-a6006ad3dba0");
    }
}
