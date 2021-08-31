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
package com.krawler.common.util;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.krawler.acc.dm.ExchangeRateDetailInfo;
import com.krawler.acc.dm.ExchangeRateInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.*;

/**
 * A place to keep commonly-used constants.
 */
public class Constants {
    //Global Constants
    public static final String JMS_MAIL_QUEUE = "mailQueue";
    public static final String ExportQueue = "ExportQueue";
    public static final int SMTP_FlOW_ON = 1;
    public static final int TEXT_MAX_LENGHT = 65535;
    public static final int INDONESIA_LANGUAGE_ID = 6;
    public static final String RES_data = "data";
    public static final String RES_count = "count";
    public static final String RES_success = "success";
    public static final String RES_msg = "msg";
    public static final String RES_failure = "failure";
    public static final String RES_timeout = "timeout";
    public static final String jsonView = "jsonView";
    public static final String jsonView_ex = "jsonView_ex";
    public static final String model = "model";
    public static final String ss = "ss";
    public static final String sortstring = "sortstring";
    public static final String remotecomboquery = "query";
    public static final String df = "df";
    public static final String sdf = "sdf";
    public static final String userdf = "userdf";
    public static final String importdf = "importdf";
    public static final String start = "start";
    public static final String limit = "limit";
    public static final String filterNamesKey = "filter_names";
    public static final String filterParamsKey = "filter_params";
    public static final String companyKey = "companyid";
    public static final String ruleid = "ruleid";
    public static final String driverId = "driverid";
    public static final String consolidateFlag = "consolidateFlag";
    public static final String isForTemplate = "isForTemplate";
    public static final String isTradingFlow = "isTradingFlow";
    public static final String requestStatus = "requestStatus";
    public static final String isOutstanding = "isOutstanding";
    public static final String isPendingInvoiced = "isPendingInvoiced";
    public static final String isOuststandingProduct = "isOuststandingproduct";
    public static final String billId = "billId";
    public static final String enableSalesPersonAgentFlow = "enablesalespersonagentflow";
    public static final String dir = "dir";
    public static final String sort = "sort";
    public static final String isPendingApproval = "ispendingapproval";
    public static final String baseURL = "baseURL";
    public static final String currencyKey = "currencyid";
    public static final String useridKey = "userid";
    public static final String creatoridKey = "creatorid";
    public static final String creatorUserName = "creatorUserName";
    public static final String isExport = "isExport";
    public static final String isFromDashBoard = "isFromDashBoard";
    public static final String isEdit = "isEdit";
    public static final String readOnly = "readOnly";
    public static final String initialAvailableQuantity = "initialavailablequantity";
    public static final String inventoryID = "inventoryid";
    public static final String isForProduce = "isForProduce";
    public static final String oldProduceQtyDetail = "oldProduceQtyDetail";
    public static final String wasteMovementFlag = "wastemovementflag";
    public static final String SUPPLIERINVOICENO = "supplierinvoiceno";
    public static final String importExportDeclarationNo = "importexportdeclarationno";
    public static final String isCreditable = "isCreditable";
    public static final String MVATTRANSACTIONNO = "mvattransactionno";
    public static final String isForGSTReport = "isForGSTReport";
    public static final String isForTaxReport = "isForTAXReport";
    public static final String gstMappingColumnNumber = "gstmappingcolnum";
    public static final String orderBy = "order_by";
    public static final String orderType = "order_type";
    public static final String ascending = "ASC";
    public static final String descending = "DESC";
    public static final String userSessionId = "userSessionId";
    public static final String ISFORSALESCOMMISSION = "isForSalesCommission";
    public static final String ISAUTOPOPULATEDEFAULTVALUE = "isautopopulatedefaultvalue";
    public static final String IsForGSTRuleMapping = "isForGST";
    public static final String NONE = "None";
    public static final String NONEID = "1234";
    public static final String NONEID_1 = "-1";
    public static final int HTML_EDITOR = 2;

    public static final String isForKnockOff = "isForKnockOff";
    public static final String requestModuleId = "requestModuleid";
    public static final String linkModuleId = "linkModuleId";
    public static final String isLink = "isLink";
    public static final String showGSTAndExpenseGLAccounts = "showGSTAndExpenseGLAccounts";
    public static final String AccJECustomData = "AccJECustomData";
    public static final String AccJEDetailCustomData = "AccJEDetailCustomData";
    public static final String AccJEDetailsProductCustomData = "AccJEDetailsProductCustomData";
    public static final String BarDelimiter = "|";
    public static final String DeskeraERPVersion = "Deskera v.9.0";
    public static final String GAFFileVersion_1 = "1.0";
    public static final String GAFFileVersion_2 = "2.0";
    public static final String GAFFileVersion_GAFv1 = "GAFv1.0.0";
    public static final String GAFFileVersion_GAFv2 = "GAFv2.0";
    public static final String GAFFileName = "GST_Audit_File_v";
    public static final String reportFlag = "reportflag";
    public static final String REPORT_TYPE = "reporttype";
    public static final int GSTGuideDec2017_Version = 0;
    public static final int GSTGuideMarch2018_Version = 1;
    public static final String ZeroRatedTaxAppliedDateForMalasia = "01-06-2018";//Date in dd-MM-yyyy format. From 1st June 2018 Malaysian GST is zero.    
    
    //CustomData Pojo Names for various modules
    public static final String SalesOrderCustomData = "SalesOrderCustomData";
    public static final String SalesReturnCustomData = "SalesReturnCustomData";
    public static final String QuotationCustomData = "QuotationCustomData";
    public static final String DeliveryOrderCustomData = "DeliveryOrderCustomData";
    public static final String CustomerCustomData = "CustomerCustomData";
    public static final String AccProductCustomData = "AccProductCustomData";
            
    public static final String AGEDAMOUNTSUMMARY = "totalAmountJSON";
    public static final String Customer_Invoice_Key="ci";
    public static final String Customer_Quatation_Key="cq";
    public static final String Sales_Order_Key="so";
    public static final String Delivery_Order_Key="do";
    public static final String Journal_Entry_Key="gl";
    public static final String Debit_Note_Key="dn";
    public static final String Received_Payment_Key="rp";
    public static final String Purchase_Order_Key="po";
    public static final String Request_For_Quotation_Key="rfq";
    public static final String Vendor_Quotation_Key="vq";
    public static final String Credit_Note_Key="cn";
    public static final String Make_Payment_Key="pay";
    public static final String Purchase_Requisition_Key="pr";
    public static final String Purchase_Invoice_Key="pi";
    public static final String Goods_Receipt_Key="grn";
    public static final String country = "country";
    public static final String globalCurrencyKey = "gcurrencyid";
    public static final String POSOFLAG = "sotopolinkflag";
    public static final String VQCQFLAG = "vqtocqlinkflag";
    public static final String REQ_startdate = "startdate";
    public static final String REQ_enddate = "enddate";
    public static final String SUNDRY_CUSTOMER = "Sundry Customer";
    public static final String SUNDRY_VENDOR = "Sundry Vendor";
    public static final String SALES_ACCOUNT = "Sales";
    public static final String UNREALISED_ACCOUNT = "Unrealised Gain/Loss";
    public static final String FOREIGN_EXCHANGE = "Foreign Exchange";
    public static final String PURCHASE_ACCOUNT = "Purchases";
    public static final String CASH_SALE = "Cash Sale";
    public static final String CUSTOMER_INVOICE = "Sales Invoice";
    public static final String CUSTOMERCON_INVOICE = "Consignment Sales Invoice";
    public static final String CASH_PURCHASE = "Cash Purchase";
    public static final String VENDOR_INVOICE = "Purchase Invoice";
    public static final String VENDORCON_INVOICE = "Consignment Purchase Invoice";
    public static final String VENDOR_QUOTATION = "Vendor Quotation";
    public static final String ASSET_VENDOR_QUOTATION = "Asset Vendor Quotation";
    public static final String CUSTOMER_QUOTATION = "Customer Quotation";
    public static final String LEASE_CUSTOMER_QUOTATION = "Lease Customer Quotation";
    public static final String SALESORDER = "Sales Order";
    public static final String SALESCONTRACT = "Sales Contract";
    public static final String ACQUIRED_INVOICE="Asset Acquired Invoice";
    public static final String DISPOSAL_INVOICE="Asset Disposal Invoice";
    public static final String LEASE_INVOICE="Lease Invoice";
    public static final String CREDIT_NOTE = "Credit Note";
    public static final String ACC_PURCHASE_ORDER = "Purchase Order";
    public static final String DEBIT_NOTE = "Debit Note";
    public static final String PAYMENT_RECEIVED = "Payment Received";
    public static final String PAYMENT_MADE = "Payment Made";
    public static final String Assembly_Product_Master = "Product Assembly";
    public static final String Disassemble_Product_Master = "Product Disassembly";
    public static final String Invoice_Write_Off = "Invoice Write Off";
    public static final String Invoice_Write_Off_Reverse = "Invoice Write Off Recover";
    public static final String Delivery_Order = "Delivery Order";
    public static final String Lease_Delivery_Order = "Lease Delivery Order";
    public static final String Asset_Delivery_Order = "Asset Delivery Order";
    public static final String Goods_Receipt = "Goods Receipt";
    public static final String PURCHASE_RETURN = "Purchase Return";
    public static final String Lease_Sales_RETURN = "Lease Sales Return";
    public static final String Asset_Sales_RETURN = "Asset Sales Return";
    public static final String Fixed_Asset_Goods_Receipt = "Asset Goods Receipt Order";
    public static final String PURCHASE_REQUISITION = "Purchase Requisition";
    public static final String ASSET_PURCHASE_REQUISITION = "Asset Purchase Requisition";
    public static final String ASSET_PURCHASE_ORDER = "Asset Purchase Order";
    public static final String ASSET_PURCHASE_RETURN = "Asset Purchase Return";
    public static final String ASSET_PURCHASE_INVOICE = "Asset Purchase Invoice";
    public static final String MRP_WORK_ORDER = "Work Order";
    public static final String isActivateMRPModule = "isActivateMRPModule";
    public static final String MRP_JOBWORK_OUT = "Job Work Out";
    public static final String MRP_JOBWORK_IN = "Job Work IN";
    public static final String JOBWORK_IN_ORDER = "Job Work In Order"; 
    public static final String JOBWORK_OUT_FLOW = "Job Work Out Order";
    public static final String STOCK_ADJUSTMENT = "Stock Adjustment";
    public static final String LOAN_DISBURSEMENT = "Loan Disbursement";
    public static final String TRANSFER = "Transfer";
    public static final String WITHDRAWAL = "Withdrawal";
    public static final String DEPOSIT = "Deposit";
    public static final String CURRENT_USER = "Current User"; 
    public static final String CASH = "2";
    public static final String BANK = "3";
    public static final String CREDIT_CARD = "1";
    public static final String customerid = "customerid";
    public static final String vendorid = "vendorid";
    public static final String MARKED_FAVOURITE = "isfavourite";
    public static final String isLeaseFixedAsset = "isLeaseFixedAsset";
    public static final String MARKED_PRINTED = "isprinted";
    public static final String HEADER_IMAGE_TEMPLATE_ID = "ff80808140a555fe0140a588bcee0007";
    public static final String CashInHand = "Cash in hand";
    public static final String ID = "ID";
    public static final String isRepeatedFlag = "getRepeateInvoice";
    public static final String isPendingJEFlag = "getPendingJEFlag";
    public static final String isRepeatedPaymentFlag = "getRepeatePayment";
    public static final String ADMIN_EMAILID = "admin@deskera.com";
    public static final String GiroFileExtension = ".txt";
    public static final String CIMBGiroFileExtension = ".csv";
    public static final String closedStatus = "Closed";
    public static final String openStatus = "Open";
    public static final String expiredStatus = "Expired";
    public static final String isforeclaim = "isforeclaim";
    public static final String CannotaddnewForeClaim = "Cannot add. Column/Dimension with option true for eClaim is already exists in the following module(s)- <br/>";
    /*Threshould value to write data in CSV-GL Report*/
    public static final int THRESHOLD_VAL_TO_WRITE_INTO_CSV_FILE=50;
    //Adding for Custom column
    public static final String LineLevelCustomData = "linelevelcustomdata";
    public static final String GlobalLevelCustomData = "globallevelcustomdata";
    public static final String customarray = "customarray";
    public static final String modulename = "modulename";
    public static final String moduleprimarykey = "moduleprimarykey";
    public static final String modulerecid = "modulerecid";
    public static final String moduleid = "moduleid";
    public static final String customdataclasspath = "customdataclasspath";
    public static final String formField = "formField";
    public static final String isForProductandService = "isForProductandService";
    public static final String relatedmoduleid = "Relatedmoduleid";
    public static final String RELATED_MODULE_IS_ALLOW_EDIT = "RelatedModuleIsAllowEdit";
    public static final String relatedModuleId = "relatedmoduleid";
    public static final String parentmoduleid = "Parentid";
    public static final String moduleidarray = "moduleidarray";
    public static final String filter_names = "filter_names";
    public static final String filter_values = "filter_values";
    public static final String filter_params = "filter_params";
    public static final String companyid = "companyid";
    public static final String prNumber = "prNumber";
    public static final String fromName = "fromName";
    public static final String hasApprover = "hasApprover";
    public static final String isCash = "isCash";
    public static final String createdBy = "createdBy";
    public static final String amount = "amount";
    public static final String requisiton = "requisiton";
    public static final String fromCreate = "fromCreate";
    public static final String fromEmailID = "fromEmailID";
    public static final String emails = "emails";
    public static final String productArray = "prodcutArray";
    public static final String company = "company.companyID";
    public static final String INmoduleid = "INmoduleid";
    public static final String Custom_Column_Default_value = "null";
    public static final String Custom_Column_Prefix = "Col";
    public static final String Custom_Record_Prefix = "Custom_";
    public static final String Custom_column_Prefix = "col";
    public static final String DefaultTimeZone = "GMT"; // with GMT+00
    public static final String Custom_Column_Sep = ",";
    public static final String data = "data";
    public static final String COLUMN_CONFIGS = "columns";
    public static final String Acc_JE_modulename = "Journalentry";
    public static final String Acc_JEDetail_modulename = "Jedetail";
    public static final String Acc_JEid = "JournalentryId";
    public static final String Acc_StockId = "StockId";
    public static final String Acc_StockAdjustmentId = "StockAdjustmentId";
    public static final String Acc_ISTId = "ISTId";
    public static final String Acc_JEDetailId = "JedetailId";
    public static final String Acc_jedetailId = "jedetailId";
    public static final String NetProfitLossAccountName = "Net Profit/Loss";
    public static final String OpeningStock = "Opening Stock";
    public static final String ClosingStock = "Closing Stock";
    public static final String StockInHand = "Stock in Hand";
    public static final String SequenceformatErrorMsg1 = "Document No. <b>";
    public static final String SequenceformatErrorMsg2 = "</b>  is invalid. In <b> Indian GST </b> length of Document No. should be less than  <b>";
    public static final String SequenceformatErrorMsg3 = "</b> digits and contains only alphanumeric <b> / </b> and <b> - </b> special characters";
    public static final int Acc_Invoice_ModuleId = 2;
    public static final int Acc_Party_Journal_Entry = 0;// When we create Party JE with CN/DN then in JE table transaction module is saved as '0'
    //Stock Request/ issue 
    public static final int GOODS_PENDING_ORDER = 3;
    public static final int FULFILLED_ORDER = 2;
    public static final int STORE_ORDER = 1;
    //Recurring Reports ModuleId
    public static final int Acc_Recurring_SalesInvoice_ModuleId=470;
    public static final int Acc_Recurring_PurchaseInvoice_ModuleId=471;
    public static final int Acc_Recurring_SalesOrder_ModuleId=472;
    public static final int Acc_Recurring_JE_ModuleId=473;
    public static final int Acc_Recurring_MakePayment_ModuleId=474;
    public static final int Acc_Product_ModuleId = 34;
    public static final int Acc_BillingInvoice_ModuleId = 3;
    public static final int Acc_Cash_Sales_ModuleId = 4;
    public static final int Acc_Billing_Cash_Sales_ModuleId = 5;
    public static final int Acc_Vendor_Invoice_ModuleId = 6;
    public static final int Acc_Vendor_BillingInvoice_ModuleId = 7;
    public static final int Acc_Cash_Purchase_ModuleId = 8;
    public static final int Acc_Productcombo_Include_type = 0;
    public static final int Acc_Productcombo_Include_description = 1;
    public static final int Acc_Customer_AccStatement_ModuleId = 60;
    public static final int Acc_Vendor_AccStatement_ModuleId = 61;
    public static final int Acc_BillingCash_Purchase_ModuleId = 9;
    public static final int Acc_Debit_Note_ModuleId = 10;
    public static final int Acc_BillingDebit_Note_ModuleId = 11;
    public static final int Acc_Credit_Note_ModuleId = 12;
    public static final int Acc_BillingCredit_Note_ModuleId = 13;
    public static final int Acc_Make_Payment_ModuleId = 14;
    public static final int Acc_BillingMake_Payment_ModuleId = 15;
    public static final int Acc_Receive_Payment_ModuleId = 16;
    public static final int Acc_BillingReceive_Payment_ModuleId = 17;
    public static final int Acc_Purchase_Order_ModuleId = 18;
    public static final int Acc_BillingPurchase_Order_ModuleId = 19;
    public static final int Acc_Sales_Order_ModuleId = 20;
    public static final int Acc_BillingSales_Order_ModuleId = 21;
    public static final int Acc_Customer_Quotation_ModuleId = 22;
    public static final int Acc_Vendor_Quotation_ModuleId = 23;
    public static final int Acc_GENERAL_LEDGER_ModuleId = 24;
    public static final int Acc_Customer_ModuleId = 25;
    public static final int Acc_Vendor_ModuleId = 26;
    public static final int Acc_Delivery_Order_ModuleId = 27;
    public static final int Acc_Goods_Receipt_ModuleId = 28;
    public static final int Acc_Sales_Return_ModuleId = 29;
    public static final int Acc_Product_Master_ModuleId = 30;
    public static final int Acc_Purchase_Return_ModuleId = 31;
    public static final int Acc_Purchase_Requisition_ModuleId = 32;
    public static final int Acc_RFQ_ModuleId = 33;
    public static final int Account_Statement_ModuleId = 34;
    public static final int Account_Preferences_ModuleId = 122;
    public static final int Only_ProductMaster_ModuleId = 1000;
    public static final int Master_Configuration_ModuleId = 37;
    public static final int Acc_Contract_Order_ModuleId = 35;
    public static final int Acc_Lease_Order_ModuleId = 36;
    public static final int Acc_FixedAssets_DisposalInvoice_ModuleId = 38;
    public static final int Acc_FixedAssets_PurchaseInvoice_ModuleId = 39;
    public static final int GSTR2A_Match_And_Reconcile_Report = 1331;
    public static final int GSTR3B_Summary_Report = 1332;
    public static final int INDONESIA_VAT_OUT_REPORT = 13366; //Report id for Export VAT out Report Indonesia Country
    public static final int GSTRComputationDetailReport = 1435;
    public static final int GSTR3B_DETAIL_REPORT = 1335;
    public static final int LINELEVELTERMFORGST = 39;
    public static final int GSTSTATUS_NEW = 1;
    public static final int GSTSTATUS_OLDNEW = 2;
    public static final int GSTSTATUS_NONE = 3;
    public static final int GST_CONFIG_ISFORMULTIENTITY = 1; // isformultientity = T
    public static final int GST_CONFIG_MANDETORY_FIELD = 2; // if madetory field like Product Tax Class but isformultientity = F
    public static final int GST_CONFIG_ISFORGST = 3; // isforgstrulemapping = 1    
    public static final int GST_CONFIG_CUSTOM_TO_ENTITY = 4; // if field is custom field for Entity i.e. it is there for module 1200 only 
    public static final int GST_CONFIG_HSN_SAC_CODE = 5; // HSN/SAC Code
    public static final int GST_CONFIG_UQC = 6; // Unit Quantity Code
    public static final int EWAYFIELDS_GSTCONFIGTYPE = 8;
    public static final String GST_REGISTRATION_TYPE = "GST Registration Type";
    public static final String GST_CUSTOMER_VENDOR_TYPE = "GST Customer/ Vendor Type";
    public static final int Acc_FixedAssets_GoodsReceipt_ModuleId = 40;
    public static final int Acc_FixedAssets_DeliveryOrder_ModuleId = 41;
    public static final int Acc_FixedAssets_AssetsGroups_ModuleId = 42;
    public static final int Group_ModuleId = 35;
    public static final int MaxThreasholdValue = 200;
    public static final int Asset_Maintenance_ModuleId = 43;
    public static final int Acc_Customer_Address_ModuleId = 44;    
    public static final int Acc_Vendor_Address_ModuleId = 45;    
    public static final int Acc_Transaction_Address_ModuleId = 46;    
    public static final int Acc_DeliveryOrderDetail_ModuleId = 47;    
    public static final int Acc_InvoiceDetail_ModuleId = 48;    
    public static final int Acc_ConsignmentRequest_ModuleId =50;
    public static final int Acc_ConsignmentDeliveryOrder_ModuleId =51;
    public static final int Acc_ConsignmentInvoice_ModuleId =52;
    public static final int Acc_ConsignmentSalesReturn_ModuleId =53;
    public static final int Acc_Consignment_GoodsReceiptOrder_ModuleId =57;
    public static final int Acc_Consignment_GoodsReceipt_ModuleId =58;
    public static final int Acc_ConsignmentPurchaseReturn_ModuleId =59;
    public static final int Acc_ConsignmentVendorRequest_ModuleId=63;
    public static final int SerialWindow_ModuleId =79;
    public static final int Acc_Product_Price_List_ModuleId = 80;
    public static final int Acc_Unit_Of_Measure_ModuleId = 81;
    public static final int SALES_BAD_DEBT_CLAIM_ModuleId  = 86;
    public static final int PURCHASE_BAD_DEBT_CLAIM_ModuleId  = 83;
    public static final int PURCHASE_BAD_DEBT_RECOVER_ModuleId  = 84;
    public static final int SALES_BAD_DEBT_RECOVER_ModuleId  = 85;
    public static final int Acc_Product_opening_stock_ModuleId = 82;
    public static final int Inv_StockIn_ModuleId = 83;
    public static final int Acc_FixedAssets_PurchaseRequisition_ModuleId = 87;
    public static final int Acc_FixedAssets_RFQ_ModuleId = 88;
    public static final int Acc_FixedAssets_Vendor_Quotation_ModuleId = 89;
    public static final int Acc_FixedAssets_Purchase_Order_ModuleId = 90;
    public static final int Acc_Assembly_Product_Master_ModuleId = 91;
    public static final int Inventory_ModuleId = 92;
    public static final int Inventory_Stock_Adjustment_ModuleId = 95;
    public static final int Acc_Lease_Contract = 64;
    public static final int Acc_Lease_Quotation = 65;
    public static final int LEASE_INVOICE_MODULEID = 93;
    public static final int Acc_Lease_DO = 67;
    public static final int Acc_Lease_Return = 68;
    public static final int Acc_Product_Category_ModuleId = 94;
    public static final int Acc_FixedAssets_Purchase_Return_ModuleId = 96;
    public static final int Acc_FixedAssets_Sales_Return_ModuleId = 98;
    public static final int Acc_FixedAssets_Details_ModuleId = 121;
    public static final int Acc_UOM_Schema_ModuleId = 99;
    public static final int Store_Master = 100;
    public static final int Acc_Ledger_ModuleId = 102;
    public static final int Acc_Price_List_Band_ModuleId = 105;
    public static final int Acc_Sales_Invocie_WriteOff_ModuleId = 106;
    public static final int Acc_Sales_Invocie_WriteOff_ModuleId_Reverse = 107;
    public static final int Acc_ConsignmentStockPurchaseRequest_ModuleId =108;
    public static final int Acc_Receipt_WriteOff_ModuleId =109;
    public static final int Acc_Receipt_WriteOff_ModuleId_Reverse =110;
    public static final int Cheque_ModuleId =111;
    public static final int Acc_opening_Sales_Invoice =112;
    public static final int Acc_opening_Prchase_Invoice =113;
    public static final int Acc_opening_Receipt =114;
    public static final int Acc_opening_Payment =115;
    public static final int Acc_opening_Customer_CreditNote =116;
    public static final int Acc_opening_Vendor_CreditNote =117;
    public static final int Acc_opening_Customer_DebitNote =118;
    public static final int Acc_opening_Vendor_DebitNote =119;
    public static final int Acc_Build_Assembly_Product_ModuleId =120;
    public static final int Acc_Unbuild_Assembly_Product_ModuleId =126;
    public static final int Acc_Loan_Management_ModuleId =121;
    public static final int Custom_design_LineItem_DefaultRowsCount = 2;
    public static final int Custom_design_LineItem_FieldType = 11;
    public static final int Acc_Dishonoured_Make_Payment_ModuleId = 122;
    public static final int Acc_Dishonoured_Receive_Payment_ModuleId = 134;
    public static final int Currency_Exchange_ModuleId=123;
    public static final int Tax_Currency_Exchange_ModuleId=126;
    public static final int Bank_Reconciliation_ModuleId=124;
    public static final int DBS_Bank_Details_ModuleId=125;
    public static final int Acc_Dimension_ModuleId=127;
    public static final int Acc_ReconcileNumber_ModuleId =128;
    public static final int Acc_UnReconcileNumber_ModuleId =129;
    public static final int Acc_QA_APPROVAL_MODULE_ID =132;
    public static final int Build_Assembly_Module_Id =133;
    public static final String Mailnotification_Approval_Fieldid ="22";
    public static final String Mailnotification_Rejection_Fieldid ="23";    
    //Check Number
    public static final int CHEQUE_NUMBER_DIGIT_LIMIT =16;
    public static final String CHEQUE_NUMBER_ALLOWED_LETTERS ="[a-zA-Z0-9_]*";
    public static final String ALFA_ALLOWED_LETTERS ="[a-zA-Z_]*";
    /**
     * Packing Delivery Order constant
     */
    public static final int Acc_PackingDO_ModuleId =130;
    /**
     * Shipping Delivery Order constant
     */
    public static final int Acc_ShippingDO_ModuleId =131;
    // Aged Module IDs : Start
    public static final int Acc_AgedReceivables_Summary_ModuleId = 211;
    public static final int Acc_AgedPayables_Summary_ModuleId = 212;
    public static final int Acc_AgedReceivables_ReportView_ModuleId = 213;
    public static final int Acc_AgedPayables_ReportView_ModuleId = 214; 
   public static final int Acc_Packing_ModuleId =554;
   public static final int Acc_Shipping_ModuleId = 854;
    // Aged Module IDs : End
    public static final int AgedDetailBasedOnSalesPerson = 244;
    public static final int Labour_Master=1101;
    public static final int MRP_WORK_CENTRE_MODULEID = 1102;
    public static final int MRP_Machine_Management_ModuleId =1103;
    public static final int MRP_RouteCode=1107;
    public static final int MRP_JOB_WORK_MODULEID = 1104;
    public static final int MRP_WORK_ORDER_MODULEID = 1105;
    public static final int MRP_JOB_WORK_IN_MODULEID = 1109;
    public static final int VENDOR_JOB_WORKORDER_MODULEID = 1114;
    public static final int MRP_WorkOrderBlockQuantityTransactionType = 20;
    public static final int JOB_WORK_OUT_ORDER_MODULEID = 1115;
    public static final int JOB_WORK_STOCK_IN_MODULEID = 1338;
    public static final int JOB_WORK_OUT_STOCK_TRANSFER_MODULEID = 1339;
    public static final int MRP_Contract=1106;
    public static final int GSTModule=1200;
    public static final int Account_Opening_Transaction_ModuleId=1107;
    public static final int Dealer_Excise_RG23DEntry_No=1134;
    public static final int CUSTOMER_ADDRESS_DETAILS_MODULE_ID = 1110;
    public static final int CONVERT_SALES_INVOICE_INTO_CASH_SALES_MODULE_ID = 1111;
    public static final int CUSTOMER_CATEGORY_MODULE_ID = 1112;
    public static final int VENDOR_CATEGORY_MODULE_ID = 1113;
    public static final int REVALUATION_MODULE_ID = 1139;
    public static final int Acc_Stock_Repair_Report_ModuleId = 247;
    public static final int ACC_GENERAL_LEDGER_REPORT_MODULEID=350; //Used for Grid Config setting
    public static final int ACC_GROUP_DETAIL_REPORT_MODULEID=351;   //Used for Grid Config setting
    
    public static final int CONSIGNMENT_SALES_MODULE = 201;
    public static final int CONSIGNMENT_PURCHASE_MODULE = 202;
    public static final int FIXED_ASSET_DISPOSAL_MODULE = 203; // Used to show the Description of Asset Disposal JE
    public static final int FIXED_ASSET_REVERT_DISPOSEDASSET_MODULE = 204; // Used to show the Description of Reverted Disposed Asset
    public static final int All_Transaction_TypeID = 0;// Used in GL for showing all type of transactions
    public static final int Customer_Sync_Batch_Count = 500;// Used for Syncing bustomer In CRM of 100 per batch
    public static final int Acc_SecurityGateEntry_ModuleId = 1116;
    public static final int Acc_Multi_Entity_Dimension_MODULEID=1200;
    public static final int POS_MODULEID=1899;
    
    /**
     * Used in inventory stock valuation DAO method 'accProducImpl.getStockLedger'
     * To fetch product details along with transaction data from database
     */
    public static  final String includeProductDetailsInSelectQuery="includeProductDetailsInSelectQuery";
    
    public static final String Account_ModuleId = "e1e72896-bf85-102d-b644-001e58a64cb6";
    public static final String CUSTOMER_MODULE_UUID = "09508488-c1d2-102d-b048-001e58a64cb6";
    public static final String Vendor_MODULE_UUID = "b8bd81b0-c500-102d-bb0b-001e58a64cb6";
    public static final String Acc_Stock_modulename = "StockRequest";
    public static final String Acc_StockAdjustment_modulename = "StockAdjustment";
    public static final String Acc_InterStoreTransfer_modulename = "ISTRequest";
    public static final String SEQFORMAT = "seqformat";
    public static final String SEQNUMBER = "seqnumber";
    public static final String DATEPREFIX = "dateprefix";
    public static final String DATEAFTERPREFIX = "dateafterprefix";
    public static final String DATESUFFIX = "datesuffix";
    public static final String DOCUMENTID = "documentid";
    public static final String AUTO_ENTRYNUMBER = "autoentrynumber";
    public static final String SEQUENCEFORMATID = "sequenceformatid";
    public static final String JE_DEFAULT_PREFIX = "JE";
    public static final String JE_DEFAULT_FORMAT = "JE000000";
    public static final String CI_DEFAULT_PREFIX = "CI";
    public static final String CI_DEFAULT_FORMAT = "CI000000";
    public static final String MP_DEFAULT_PREFIX = "MP";
    public static final String MP_DEFAULT_FORMAT = "MP000000";
    public static final String Stock_custom_data_classpath = "com.krawler.inventory.model.stock.StockCustomData";
    public static final String Acc_BillInv_custom_data_classpath = "com.krawler.hql.accounting.AccJECustomData";
    public static final String Acc_Product_custom_data_classpath = "com.krawler.hql.accounting.AccProductCustomData";
    public static final String Acc_PurchaseOrder_custom_data_classpath = "com.krawler.hql.accounting.PurchaseOrderCustomData";
    public static final String Acc_PurchaseOrderDetails_custom_data_classpath = "com.krawler.hql.accounting.PurchaseOrderDetailsCustomData";
    public static final String Acc_GoodsReceiptOrderDetailsCustomDate_custom_data_classpath = "com.krawler.hql.accounting.GoodsReceiptOrderDetailsCustomDate";
    public static final String Acc_PurchaseReturnDetailsCustomDate_custom_data_classpath = "com.krawler.hql.accounting.PurchaseReturnDetailCustomDate";
    public static final String Acc_DeliveryOrderDetail_custom_data_classpath = "com.krawler.hql.accounting.DeliveryOrderDetailCustomData";
    public static final String Acc_SalesReturnDetails_custom_data_classpath = "com.krawler.hql.accounting.SalesReturnDetailCustomData";
    public static final String Acc_QuotationDetails_custom_data_classpath = "com.krawler.hql.accounting.QuotationDetailCustomData";
    public static final String Acc_QuotationVersionDetails_custom_data_classpath = "com.krawler.hql.accounting.QuotationVersionDetailCustomData";
    public static final String Acc_VendorQuotationVersionDetails_custom_data_classpath = "com.krawler.hql.accounting.VendorQuotationVersionDetailCustomData";
    public static final String Acc_VendorQuotationDetails_custom_data_classpath = "com.krawler.hql.accounting.VendorQuotationDetailCustomData";
    public static final String Acc_PurchaseOrderVersionDetails_custom_data_classpath = "com.krawler.hql.accounting.PurchaseOrderVersionDetailsCustomData";
    public static final String Acc_PurchaseOrderVersion_custom_data_classpath = "com.krawler.hql.accounting.PurchaseOrderVersionCustomData";
    public static final String Acc_ExpensePOVersionDetail_custom_data_classpath = "com.krawler.hql.accounting.ExpensePOVersionDetailCustomData";
    public static final String Acc_SalesOrder_custom_data_classpath = "com.krawler.hql.accounting.SalesOrderCustomData";
    public static final String Acc_MultiEntityDimension_custom_data_classpath = "com.krawler.common.admin.MultiEntityDimesionCustomData"; 
    public static final String Acc_StockAdjustment_custom_data_classpath = "com.krawler.inventory.model.stockout.StockAdjustmentCustomData";
    public static final String Acc_InterStoreTransfer_custom_data_classpath = "com.krawler.inventory.model.ist.InterStoreTransferCustomData";
    public static final String Acc_Cycle_Count_custom_data_classpath = "com.krawler.inventory.model.cyclecount.CycleCountCustomData";
    public static final String Acc_Contract_Order_custom_data_classpath = "com.krawler.hql.accounting.ContractCustomData";
    public static final String Acc_Contract_Details_custom_data_classpath = "com.krawler.hql.accounting.ContractDetailCustomData";
    public static final String Acc_SalesOrderDetails_custom_data_classpath = "com.krawler.hql.accounting.SalesOrderDetailsCustomData";
    public static final String Acc_SecurityGateDetails_custom_data_classpath = "com.krawler.hql.accounting.SecurityGateDetailsCustomData";
    public static final String Acc_BillInvDetail_custom_data_classpath = "com.krawler.hql.accounting.AccJEDetailCustomData";
    public static final String Acc_JEDetail_Productcustom_data_classpath = "com.krawler.hql.accounting.AccJEDetailsProductCustomData";
    public static final String Acc_PODETAIL_Productcustom_data_classpath = "com.krawler.hql.accounting.PurchaseOrderDetailProductCustomData";
    public static final String Acc_SODETAIL_Productcustom_data_classpath = "com.krawler.hql.accounting.SalesOrderDetailProductCustomData";
    public static final String Acc_DODETAIL_Productcustom_data_classpath = "com.krawler.hql.accounting.DeliveryOrderDetailProductCustomData";
    public static final String Acc_CQDetail_Productcustom_data_classpath = "com.krawler.hql.accounting.QuotationDetailsProductCustomData";
    public static final String Acc_VQDetail_Productcustom_data_classpath = "com.krawler.hql.accounting.VendorQuotationDetailsProductCustomData";
    public static final String Acc_SRDetail_Productcustom_data_classpath = "com.krawler.hql.accounting.SalesReturnDetailProductCustomData";
    public static final String Acc_PRDetail_Productcustom_data_classpath = "com.krawler.hql.accounting.PurchaseReturnDetailProductCustomData";
    public static final String Acc_GRODetail_Productcustom_data_classpath = "com.krawler.hql.accounting.GoodsReceiptOrderProductCustomData";
    public static final String Acc_PurchaseRequisitionDetail_custom_data_classpath = "com.krawler.hql.accounting.PurchaseRequisitionDetailCustomData";
    public static final String Acc_RequestForQuotationDetail_custom_data_classpath = "com.krawler.hql.accounting.RequestForQuotationDetailCustomData";
    public static final String Acc_ExpensePODetail_custom_data_classpath = "com.krawler.hql.accounting.ExpensePODetailCustomData";
    public static final String Acc_SecurityGateEntry_custom_data_classpath = "com.krawler.hql.accounting.SecurityGateEntryCustomData";
    public static final String Acc_custom_field = "fieldname";
    public static final String Acc_custom_field_value = "fieldvalue";
    public static final String Acc_custom_field_mapwithtype = "mapwithtype";
    public static final String Acc_custom_fieldId = "fieldid";
    public static final String field_data_undefined = "undefined";
    public static final String Acc_Serial_custom_data_classpath = "com.krawler.common.admin.SerialCustomData";
    public static final String Acc_FixedAsset_Details_Custom_Data_classpath = "com.krawler.hql.accounting.AssetDetailsCustomData";
    public static final String Recurring_Invoice_Crone_Name = "Repeat Invoice Crone";
    public static final String Recurring_Invoice_Crone_ID = "repeatinvoicecroneid";
    public static final String Recurring_Vendor_Invoice_Crone_Name = "Repeat Vendor Invoice Crone";
    public static final String Recurring_Vendor_Invoice_Crone_ID = "repeatvandorinvoicecroneid";
    public static final String Recurring_Make_Payment_Crone_Name = "Repeat Payment Crone";
    public static final String Recurring_Make_Payment_Crone_ID = "repeatpaymentcroneid";
    public static final int SequenceFormatMaxLength = 16;
    public static final String RichTextFieldChanged="isRichTextFieldChanged";
    //Nature Constants 
    public static final int Liability = 0;
    public static final int Asset = 1;
    public static final int Expences = 2;
    public static final int Income = 3;
    //Master Groups Lists
    public static final List<String> assetGroupList = Arrays.asList("19", "10", "9", "18", "1", "11", "12", "23");
    public static final List<String> costOfGoodsSoldGroupList = Arrays.asList("6");
    public static final List<String> equityGroupList = Arrays.asList("4");
    public static final List<String> expenseGroupList = Arrays.asList("7");
    public static final List<String> incomeGroupList = Arrays.asList("5");
    public static final List<String> liabilityGroupList = Arrays.asList("20", "13", "2", "14", "3", "21", "24");
    public static final List<String> otherExpenseGroupList = Arrays.asList("8");
    public static final List<String> otherIncomeGroupList = Arrays.asList("15");
    //Image Path
    public static final String ImgBasePath = "images/store/";
    private static final String defaultImgPath = "images/defaultuser.png";
    private static final String defaultCompanyImgPath = "images/logo.gif";
    public static final long MILLIS_PER_SECOND = 1000;
    public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    public static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
    public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;
    public static final long MILLIS_PER_WEEK = MILLIS_PER_DAY * 7;
    public static final long MILLIS_PER_MONTH = MILLIS_PER_DAY * 31;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60;
    public static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;
    public static final int SECONDS_PER_WEEK = SECONDS_PER_DAY * 7;
    public static final int SECONDS_PER_MONTH = SECONDS_PER_DAY * 31;
    public static final int MINUTE_PER_HOUR = 60;
    
    public static final String Monthly = "1";
    public static final String Yearly = "2";
    public static final String Weekly = "3";
    public static final String Daily = "4";
    
    //Full MS-OUTLOOK CSV Header
    //public static final String[] CSV_HEADER_MSOUTLOOK = {"Title","First Name","Middle Name","Last Name","Suffix","Company","Department","Job Title","Business Street","Business Street 2","Business Street 3","Business City","Business State","Business Postal Code","Business Country/Region","Home Street","Home Street 2","Home Street 3","Home City","Home State","Home Postal Code","Home Country/Region","Other Street","Other Street 2","Other Street 3","Other City","Other State","Other Postal Code","Other Country/Region","Assistant's Phone","Business Fax","Business Phone","Business Phone 2","Callback","Car Phone","Company Main Phone","Home Fax","Home Phone","Home Phone 2","ISDN","Mobile Phone","Other Fax","Other Phone","Pager","Primary Phone","Radio Phone","TTY/TDD Phone","Telex","Account","Anniversary","Assistant's Name","Billing Information","Birthday","Business Address PO Box","Categories","Children","Directory Server","E-mail Address","E-mail Type","E-mail Display Name","E-mail 2 Address","E-mail 2 Type","E-mail 2 Display Name","E-mail 3 Address","E-mail 3 Type","E-mail 3 Display Name","Gender","Government ID Number","Hobby","Home Address PO Box","Initials","Internet Free Busy","Keywords","Language","Location","Manager's Name","Mileage","Notes","Office Location","Organizational ID Number","Other Address PO Box","Priority","Private","Profession","Referred By","Sensitivity","Spouse","User 1","User 2","User 3","User 4","Web Page"};
    //Header used for our contact export
    public static final String[] CSV_HEADER_MSOUTLOOK = {"\"First Name\"", "\"E-mail Address\"", "\"Business Phone\"", "\"Business Street\""};
    public static final String CURRENCY_DEFAULT = "1";
    public static final String TIMEZONE_DEFAULT = "1";
    public static final String NEWYORK_TIMEZONE_ID = "23";
    // Regex for email and phone
    public static final String emailRegex = "^[\\w-]+([\\w!#$%&'*+/=?^`{|}~-]+)*(\\.[\\w!#$%&'*+/=?^`{|}~-]+)*@[\\w-]+(\\.[\\w-]+)*(\\.[\\w-]+)$";
    public static final String contactRegex = "^(\\(?\\+?[0-9]*\\)?)?[0-9_\\- \\(\\)]*$";
    public static final String[] INVOICE_PDF_FIELDLIST = {"Terms", "Due Date", "Ship Date", "Ship Via", "FOB", "PRODUCT DESCRIPTION", "QUANTITY", "UNIT PRICE", "AMOUNT"};
    /*
       constants to allow edit SO/PO from closed period  
    */
    public static final String isFromPO = "isFromPO";
    public static final String isFromSO = "isFromSO";
    /*
     * Constants for Approval flow
     */
    public static final int LEVEL_ONE = 1;
    public static final int LEVEL_TWO = 2;
    public static final int APPROVED = 0;
    public static final int INVOICEAPPROVED = 11;
    public static final String root = "root";
    public static final String xfield = "xfield";
    public static final String field = "field";
    public static final String xtype = "xtype";
    public static final String searchText = "searchText";
    public static final String blankSearchKey = "fe62f955-6f8c-11e8-b3eb-6045cb6f9ada";
    public static final String blankSearchText = "Blank";
    public static final String blankSearchId = "-9999";
    public static final String equalsTo = " = ";
    public static final String whiteSpace = " '' ";
    public static final String column = "column";
    public static final String iscustomcolumn = "iscustomcolumn";
    public static final String iscustomcolumndata = "iscustomcolumndata";
    public static final String isfrmpmproduct = "isfrmpmproduct";
    public static final String isForProductMasterOnly = "isForProductMasterOnly";
    public static final String isForProductMasterSearch = "isForProductMasterSearch";
    public static final String accProductCustomData = "accproductcustomdata";
    public static final String combo = "combo";
    public static final String fieldtype = "fieldtype";
    public static final String refdbname = "refdbname";
    public static final String Combo = "Combo";
    public static final String Ref = "Ref";
    public static final String dotID = ".ID";
    public static final String dotid = ".id";
    public static final String likeq = " like ? ";
    public static final String NineNine = "99";
    public static final String select = "select";
    public static final String datefield = "datefield";
    public static final String Datefield = "Datefield";
    public static final String numberfield = "numberfield";
    public static final String searchjoin = "searchjoin";
    public static final String or = " or ";
    public static final String and = " and ";
    public static final String MMMMdyyyy = "MMMM d, yyyy";
    public static final String MMMMddyyyy = "MMMM dd,yyyy"; //For cheque date(US Date) format
    public static final String ddMMyyyy = "dd/MM/yyyy";  // For Product View
    public static final String yyyyMMdd = "yyyy-MM-dd";
    public static final String yyyyMMdd_formatid = "2";
    public static final String percent = "%";
    public static final String seven = "7";
    public static final String twelve = "12";
    public static final String Searchjson = "Searchjson";
    public static final String appendCase = "appendCase";
    public static final String myResult = "myResult";
    public static final String Acc_Search_Json = "searchJson";
    public static final String Acc_Search_Json_Invoice = "searchJsonInvoice";
    public static final String Acc_Search_Json_Credit_Note = "searchJsonCreditNote";
    public static final String Acc_Search_Json_Debit_Note = "searchJsonDebitNote";
    public static final String Acc_Search_Json_Make_Payment = "searchJsonMakePayment";
    public static final String Acc_Search_Json_Receive_Payment = "searchJsonReceivePayment";
    public static final String Filter_Criteria = "filterConjuctionCriteria";
    public static final String space = " ";
    public static final String Acc_common_pojo_ref = "AccJECustomData";
    public static final String Acc_common_pojo_refForJEDETAILS = "AccJEDetailCustomData";
    public static final String Acc_common_pojo_refForProductJEDETAILS = "AccJEDetailsProductCustomData";
    public static final String Acc_common_pojo_ref_Query = "accjecustomdata";
    public static final String Acc_common_pojo_VenCust_Query = "AccountCustomData";
    public static final String Acc_common_pojo_Vendor_Query = "VendorCustomData";
    public static final String Acc_common_pojo_Customer_Query = "CustomerCustomData";
    public static final String Acc_StockRequest_Query = "stockCustomData";
    public static final String Acc_StockIssueRequest = "stockcustomdata";
    public static final String Acc_AssetDetail_Query = "assetdetailcustomdata";
    public static final String PurchaseOrder_CustomData_Query = "purchaseordercustomdata";
    public static final String PurchaseOrder_DetailCustomData_Query = "purchaseorderdetailcustomdata";
    public static final String SalesOrder_CustomData_Query = "salesordercustomdata";
    public static final String SalesOrder_DetailCustomData_Query = "salesorderdetailcustomdata";
    public static final String Contract_CustomData_Query = "contractcustomdata";
    public static final String PurchaseReturn_CustomData_Query = "purchasereturncustomdata";
    public static final String PurchaseReturn_DetailCustomData_Query = "prdetailscustomdata";
    public static final String PurchaseRequisition_CustomData_Query = "purchaserequisitioncustomdata";
    public static final String PurchaseRequisition_DetailCustomData_Query = "purchaserequisitiondetailcustomdata";
    public static final String RFQ_CustomData_Query = "RFQCustomData";
    public static final String RFQ_DetailCustomData_Query = "requestforquotationdetailcustomdata";
    public static final String SalesReturn_CustomData_Query = "salesreturncustomdata";
    public static final String SalesReturn_DetailCustomData_Query = "srdetailscustomdata";
    public static final String DeliveryOrder_CustomData_Query = "deliveryordercustomdata";
    public static final String DeliveryOrder_DetailCustomData_Query = "dodetailscustomdata";
    public static final String GoodsReceiptOrder_CustomData_Query = "grordercustomdata";
    public static final String GoodsReceiptOrder_DetailCustomData_Query = "grodetailscustomdata";
    public static final String Quotation_CustomData_Query = "quotationcustomdata";
    public static final String Quotation_DetailCustomData_Query = "quotationdetailscustomdata";
    public static final String VenderQuotation_CustomData_Query = "vendorquotationcustomdata";
    public static final String VenderQuotation_DetailCustomData_Query = "vendorquotationdetailscustomdata";
    public static final String Stock_Adjustment = "in_stockadjustment_customdata";
    public static final String Inter_Store_Location = "in_interstoretransfer_customdata";
    public static final String Labour_MasterClass = "LabourCustomData";
    public static final String WorkCentreCustomData_MasterClass = "WorkCentreCustomData";
    public static final String MachineCustomData_MasterClass = "MachineCustomData";
    public static final String WorkOrderCustomData_MasterClass = "WorkOrderCustomData";
    public static final String MRPContractCustomData_MasterClass = "MRPContractCustomData";
    public static final String MRPRoutingTemplate_MasterClass = "RoutingTemplateCustomData";
    public static final String MRPJobWork_MasterClass = "JobWorkCustomData";
    
    public static final String defaultWOstatus_PLANNED = "3493e865-1e3a-11e6-8206-14dda97927f2";
    public static final String defaultWOstatus_RELEASED = "4074b92a-1e3a-11e6-8206-14dda97927f2";
    public static final String defaultWOstatus_INPROCESS = "4c3f913b-1e3a-11e6-8206-14dda97927f2";
    public static final String defaultWOstatus_BUILT = "5c8e70af-1e3a-11e6-8206-14dda97927f2";
    public static final String defaultWOstatus_CLOSED = "6b5b8ee9-1e3a-11e6-8206-14dda97927f2";
    
    
    public static final String OpeningBalanceInvoiceCustomData_CustomData_Query = "openingbalanceinvoicecustomdata";
    public static final String OpeningBalanceVendorInvoiceCustomData_CustomData_Query = "openingbalancevendorinvoicecustomdata";
    public static final String OpeningBalanceDebitNoteCustomData_CustomData_Query = "OpeningBalanceDebitNoteCustomData";
    public static final String OpeningBalanceCreditNoteCustomData_CustomData_Query = "OpeningBalanceCreditNoteCustomData";
    public static final String OpeningBalanceMakePaymentCustomData_CustomData_Query = "OpeningBalanceMakePaymentCustomData";
    public static final String OpeningBalanceReceiptCustomData_CustomData_Query = "OpeningBalanceReceiptCustomData";
    public static final String isForTradingAndProfitLoss = "isForTradingAndProfitLoss";
    
    public static final String SalesInvoice_ClassPath = "com.krawler.hql.accounting.Invoice";
    public static final String VendorInvoice_ClassPath = "com.krawler.hql.accounting.GoodsReceipt";
    public static final String JournalEntry_ClassPath = "com.krawler.hql.accounting.JournalEntry";
    public static final String SalesOrder_ClassPath = "com.krawler.hql.accounting.SalesOrder";
    public static final String PurchaseOrder_ClassPath = "com.krawler.hql.accounting.PurchaseOrder";
    public static final String CreditNote_ClassPath = "com.krawler.hql.accounting.CreditNote";
    public static final String DebitNote_ClassPath = "com.krawler.hql.accounting.DebitNote";
    public static final String MakePayment_ClassPath = "com.krawler.hql.accounting.Payment";
    public static final String ReceivePayment_ClassPath = "com.krawler.hql.accounting.Receipt";
    public static final String CustomerQuotation_ClassPath = "com.krawler.hql.accounting.Quotation";
    public static final String VendorQuotation_ClassPath = "com.krawler.hql.accounting.VendorQuotation";
    public static final String PurchaseRequisition_ClassPath = "com.krawler.hql.accounting.PurchaseRequisition";
    public static final String RFQ_ClassPath = "com.krawler.hql.accounting.RequestForQuotation";
    public static final String Product_ClassPath = "com.krawler.hql.accounting.Product";
    public static final String DeliveryOrder_ClassPath = "com.krawler.hql.accounting.DeliveryOrder";
    public static final String GoodsReceiptOrder_ClassPath = "com.krawler.hql.accounting.GoodsReceiptOrder";
    public static final String SalesReturn_ClassPath = "com.krawler.hql.accounting.SalesReturn";
    public static final String PurchaseReturn_ClassPath = "com.krawler.hql.accounting.PurchaseReturn";
    public static final String Customer_ClassPath = "com.krawler.hql.accounting.Customer";
    public static final String Vendor_ClassPath = "com.krawler.hql.accounting.Vendor";
    public static final String Contract_ClassPath = "com.krawler.hql.accounting.Contract";
    public static final String Labour_ClassPath = "com.krawler.spring.mrp.labormanagement.Labour";
    public static final String MRPContract_ClassPath = "com.krawler.spring.mrp.contractmanagement.MRPContract";
    public static final String JobWork_ClassPath = "com.krawler.spring.mrp.jobwork.JobWork";
    public static final String WorkOrder_ClassPath = "com.krawler.spring.mrp.WorkOrder.WorkOrder";
    
    public static final String C = "C";
    public static final String c = "c";
    public static final String eight = "8";
    public static final String four = "4";
    public static final String dot = ".";
    public static final String join = " join ";
    public static final String joincdot = " join c. ";
    public static final String isnull = " is null ";
    public static final String cdot = "c.";
    public static final String user_userID = "user.userID";
    public static final String updatedOn = "updatedOn";
    public static final String defaultvalue = "defaultvalue";
    public static final String Fieldtype = "Fieldtype";
    public static final String Refcolnum = "Refcolnum";
    public static final String Colnum = "Colnum";
    public static final String hql = "hql";
    public static final String success1 = "success1";
    public static final String msg1 = "msg1";
    public static final String success2 = "success2";
    public static final String msg2 = "msg2";
    public static final String Cannotaddnew = "Cannot add. Column/Dimension with the same name already exists in the following module(s)- <br/>";
    public static final String Cannoteditnew = "Cannot edit. Column/Dimension with the same name already exists in the following module(s)-<br/>";
    public static final int Custom_Column_Combo_limit = 50;
    public static final int Custom_Column_Master_limit = 10;
    public static final int Custom_Column_User_limit = 5;
    public static final int Custom_Column_Normal_limit = 60;
    public static final int Custom_Column_Date_limit = 10;
    public static final int Custom_Column_Combo_start = 0;
    public static final int Custom_Column_Master_start = 100;
    public static final int Custom_Column_User_start = 110;
    public static final int Custom_Column_Normal_start = 1000;
    public static final int query_batch_count = 50;
    public static final int REMOTE_STORE_PAGE_LIMIT = 15;
    public static final int Custom_Column_Check_start = 2000;
    public static final int Custom_Column_Date_start = 3000; 
    public static final int Custom_Column_Check_limit = 10;
    //field Params
    public static final String Acc_fc = "fieldManager.getComboData";
    public static final String Acc_fieldid = "fieldid";
    public static final String Acc_deleteflag = "deleteflag";
    public static final String Acc_flag = "flag";
    public static final String Acc_id = "id";
    public static final String Acc_name = "name";
    public static final String Acc_FieldComboData = "FieldComboData";
    public static final String Acc_tablename = "tablename";
    public static final String Acc_maxlength = "maxlength";
    public static final String stringInitVal = "";
    public static final String fieldlabel = "fieldlabel";
    public static final String customcolumn = "customcolumn";
    public static final String customfield = "customfield";
    public static final String productcustomfield = "productcustomfield";
    public static final String ismandatory = "ismandatory";
    public static final String dataindex = "dataindex";
    /*
    * Multi Entity Advance search module key
    */
    public static final String multiEntityId = "multiEntityId";
    public static final String multiEntityValue = "multiEntityValue";
    public static final String isMultiEntity = "isMultiEntity";
    public static final String isformultientity = "isformultientity";
    public static final String GST_CONFIG_TYPE = "gstconfigtype";
    public static final String isDimensionCreated = "isDimensionCreated";
    public static final String isAdvanceSearch = "isAdvanceSearch";
    public static final String mySearchFilterString = "mySearchFilterString";
    public static final String invoiceSearchJson = "invoiceSearchJson";
    public static final String purchaseInvoiceSearchJson = "purchaseInvoiceSearchJson";
    public static final String debitNoteSearchJson = "debitNoteSearchJson";
    public static final String creditNoteSearchJson = "creditNoteSearchJson";
    public static final String makePaymentSearchJson = "makePaymentSearchJson";
    public static final String receivePaymentSearchJson = "receivePaymentSearchJson";
    public static final String journalEntrySearchJson = "journalEntrySearchJson";
    public static final String deliveryOrderSearchJson = "deliveryOrderSearchJson";
    public static final String fixedAssetsPurchaseInvoiceSearchJson = "fixedAssetsPurchaseInvoiceSearchJson";
    public static final String fixedAssetsDisposalInvoiceSearchJson = "fixedAssetsDisposalInvoiceSearchJson";
    public static final String fixedAssetsPurchaseInvoiceModuleId = "fixedAssetsPurchaseInvoiceModuleId";
    public static final String fixedAssetsDisposalInvoiceModuleId = "fixedAssetsDisposalInvoiceModuleId";
    public static final String isEWayRelatedFields = "isEWayRelatedFields";
    
    public static final String gstFlag = "gstFlag";
    public static final String MSIC_INVAMT = "invamt";
    public static final String MSIC_INVTAXAMT = "invtaxamount";
    public static final String MSIC_CODE = "MSICCODE";
    public static final String MSIC_OTHRES = "othrestaxamount";
    public static final String MSIC_TOTAL = "totaltaxamount";
    public static final String MSIC_DETAIS_JSON = "msicdetaisl";
    public static final String GSTFORM03_OTHERS_KEY="Lain-lain<br><i> Others </i>";
    public static final String GSTFORM03_TOTAL_KEY="JUMLAH <br><i>TOTAL </i>";
    public static final String isGSTAuditFile="isgstauditfile";
    public static final String isTaxDeactivated = "isTaxDeactivated";
    public static final String includeDeactivatedTax = "includeDeactivatedTax";
    public static final String isUserModifiedTaxAmount = "isUserModifiedTaxAmount";
    public static final String ACTIVATED = "activated";
    
    /*
     * Approval Transaction ids
     */
    public static final String TRANS_AMOUNT = "1";
    public static final String TRANS_PRODUCT = "2";
    public static final String TRANS_DISCOUNT = "3";
    public static final String PURCHASE_ORDER = "1";
    public static final String SALES_ORDER = "2";
    public static final String VENDOR_INVOICE_APPROVAL = "3";
    public static final String CUSTOMER_INVOICE_APPROVAL = "4";
    public static final String JOURNAL_ENTRY_APPROVAL = "5";
    public static final String CUSTOMER_QUOTATION_APPROVAL = "6";
    public static final String VENDOR_QUOTATION_APPROVAL = "7";
    public static final String SALES_ORDER_APPROVAL = "8";
    public static final String PURCHASE_ORDER_APPROVAL = "9";
    public static final String Invoice_APPROVAL = "10";
    public static final String VENDOR_Invoice_APPROVAL = "11";
    public static final String PURCHASE_REQUISITION_APPROVAL = "12";
    public static final String FIXEDASSETS_PURCHASE_REQUISITION_APPROVAL = "13";
    public static final String DELIVERY_ORDER_APPROVAL = "14";
    public static final String GOODS_RECEIPT_APPROVAL = "15";
    public static final String CREDIT_NOTE_APPROVAL = "16";
    public static final String DEBIT_NOTE_APPROVAL = "17";
    public static final String MAKE_PAYMENT_APPROVAL = "18";
    public static final String RECEIVE_PAYMENT_APPROVAL = "19";
    public static final int APPROVED_STATUS_LEVEL = 11;
    
 
    public static final String Acc_JEDetail_recdetailId = "recdetailId";
    public static final String Acc_Product_modulename = "Product";
    public static final String Acc_Productid = "ProductId";
    public static final String productid = "productid";
    public static final String parentProduct = "parentProduct";
    public static final String projectId = "projectId";
    public static final String projectid = "projectid";
    public static final String isShiftProjectStartDate = "isShiftProjectStartDate";
    public static final String pmURL = "pmURL";
    public static final String workorderdate = "workorderdate";
    public static final String productId = "productId";
    public static final String avlQuantity = "avlquantity";
    public static final String bomDetailId = "bomdetailid";
    public static final String isManageQuantity = "isManageQuantity";
    public static final String isBlocked = "isBlocked";
    public static final String isValidToStart = "isValidToStart";
    public static final String isAlreadyBlocked = "isAlreadyBlocked";
    public static final String batchName = "batchname";
    public static final String purchaseBatchId = "purchasebatchid";
    public static final String documentBatchId = "documentbatchid";
    public static final String quantityDue = "quantitydue";
    public static final String MRP_isDirtyProject = "isDirty";
    public static final String MRP_WOStatus = "MRPWOStatus";
    public static final String MRP_TransactionType = "transactionType";
    public static final String parentProductId = "parentproductid";
    public static final String isDisplayUOM = "isDisplayUOM";
    public static final String displayInitialPrice = "displayInitialPrice";
    public static final String customerPoReferenceNo = "customerPoReferenceNo";
    public static final String newcustomerid = "newcustomerid";
    public static final String newvendorid = "newvendorid";
    public static final String account_receivable_groupid = "10";
    public static final String account_payable_groupid = "13";
    public static final String ACC_EQUITY_GROUPNAME = "Equity";
    public static final String WareHouseName = "MAIN";

    //for company specific flags
    public static final int sms_templateflag = 1;
    public static final int senwan_group_templateflag = 2;
    public static final int ferrate_group_templateflag = 3;
    public static final int lsh_templateflag = 4;
    public static final int smsholding_templateflag = 5;
    public static final int vhqpost_tempalteflag = 6;
    public static final int pacific_tec_templateflag = 7;
    public static final int sats_templateflag = 8;
    public static final int senwan_tech_templateflag = 10;
    public static final int spaceTec_templateflag = 11;
    public static final int hengGaon_templateflag = 12;
    public static final int BIT_templateflag = 13;
    public static final int TID_templateflag = 14;
    public static final int TID_Subdomain_templateflag = 141;
    public static final int TIDR_Subdomain_templateflag = 142;
    public static final int CUSCADEN_Subdomain_templateflag = 143;
    public static final int TCD_Subdomain_templateflag = 144;
    public static final int CAMBORNE_Subdomain_templateflag = 145;
    public static final int MITSUI_Subdomain_templateflag = 146;
    public static final int HCIS_templateflag = 15;
    public static final int BuildMate_templateflag = 16;
    public static final int F1Recreation_templateflag = 17;
    public static final int F1RecreationLeasing_templateflag = 171;
    public static final int sustenir_templateflag = 18;
    public static final int Diamond_Aviation_templateflag = 19;
    public static final int Merlion_templateflag = 20;
    public static final int Guan_Chong_templateflag = 21;
    public static final int Guan_ChongBF_templateflag = 2102;
    public static final int Master_Flex_templateflag = 22;
    public static final int Arklife_templateflag = 23;
    public static final int Alfatech_templateFlag = 24;
    public static final int Armada_Rock_Karunia_Transhipment_templateflag = 25;
    public static final int ChyeSengHuatHoldings_TemplateFlag = 261;
    public static final int CSHOrchard_TemplateFlag = 262;
    public static final int DandJ_TemplateFlag = 263; 
    public static final int ChyeSengHuatConstruction_TemplateFlag = 264; 
    public static final int Tony_FiberGlass_templateflag = 27; 
    public static final int SBI_templateflag = 28;
    public static final int Monzone_templateflag = 29;
    public static final int BestSafety_templateflag = 30;
    public static final int FascinaWindows_templateflag = 31;
    public static final int RightSpace_templateflag = 32;
    public static final int RightWork_templateflag = 33;
    public static final int Amcoweld_templateflag = 34;             //http://accounting.deskera.com/a/amcoweldNEW/
    public static final int KimChey_templateflag = 35;
    public static final int FastenEnterprises_templateflag = 36;
    public static final int FastenHardwareEngineering_templateflag = 37;
    public static final int LandPlus_templateflag = 380;            //http://accounting.deskera.com/a/lpn/
    public static final int LandBank_templateflag = 381;            //http://accounting.deskera.com/a/lbp/
    public static final int LandBest_templateflag = 382;            //http://accounting.deskera.com/a/lbr/
    public static final int LandElHome_templateflag = 383;          //http://accounting.deskera.com/a/leh/
    public static final int LandHub_templateflag = 384;             //http://accounting.deskera.com/a/lhp/
    public static final int LandMax_templateflag = 385;             //http://accounting.deskera.com/a/lmp/
    public static final int LandQuest_templateflag = 386;           //http://accounting.deskera.com/a/lqp/
    public static final int LandSelectReality_templateflag = 387;   //http://accounting.deskera.com/a/lsr/
    public static final int LandVin_templateflag = 388;             //http://accounting.deskera.com/a/lvp/
    public static final int LandPlus_Zenn_templateflag = 389;       //http://accounting.deskera.com/a/zenn/
    public static final int LandPlus_Mobility_templateflag = 390;   //http://accounting.deskera.com/a/msrd/ 
    public static final int BakerTilly_templateflag = 39;
    public static final int BakerTilly_templateflag_pcs = 391;
    public static final String BakerTilly_BTC_COMPANYID = "807bc4c7-4473-4aa0-80c5-2731a32709a1";
    public static final String BakerTilly_PCS_COMPANYID = "df8c0204-db0f-4bb6-9ce8-23f33f0c663f";
    public static final String BakerTilly_TFWMS_COMPANYID = "39cd57bc-b153-4354-8225-a4072b6b69de";
    public static final String BakerTilly_BT_COMPANYID = "4e4f0191-bca9-4271-b988-c84c5de08144";
    public static final String BAKERTILLY_BTC_REGISTRATION_NO = "Co.Reg. No. 200700949D";
    public static final String BAKERTILLY_PCS_REGISTRATION_NO = "Co.Reg. No. 19810199C";
    public static final String BAKERTILLY_TFWMS_REGISTRATION_NO = "Co.Reg. No. 198500539E";
    public static final int PrimePartners_templateflag = 40;
    public static final int Swatow_templateflag = 41;
    public static final int Endovation_templateflag = 42;
    public static final int Endovation_cfdn_templateflag = 43;
    public static final int Endovation_cftp_templateflag = 44;
    public static final int Endovation_fved_templateflag = 45;
    public static final int WorldGreen_templateflag = 46;          //http://accounting.deskera.com/a/worldgreenlive/
    public static final int GPlus_templateflag = 47;
    public static final int CleanSolutions_templateflag = 48;
    public static final int GoldBell_templateflag = 49;
    public static final int Sanxing_templateflag = 50;
    public static final int GohYeowSeng_templateflag = 51;
    public static final int hinsitsu_templateflag = 52;          //http://accounting.deskera.com.my/a/hinsitsu/
    public static final int tanejaHomes_templateflag = 53;        
    
    public static final int senwan_tech_short_quotation_flag = 1;   // shortQuoteFlag=1 for short template of senwan tech otherwise it should be 0. [Mayur B]
    public static final int cash_detail_type = 0;
    public static final int card_detail_type = 1;   // detail types for payment  [Mayur B] //PayDetail.getPaymentMethod().getDetailType()
    public static final int bank_detail_type = 2;
    public static final int malaysian_country_id = 137;
    public static final int indian_country_id = 105;
    public static final int INDONESIAN_COUNTRY_ID = 106;
    public static final int USA_country_id = 244;
    public static final int PHILIPPINES_COUNTRY_ID = 182;
    public static final int gst_amountdigitafterdecimal=3;
    //Product Load type
    public static final int Show_all_Products=0;
    public static final int Products_on_type_ahead=1;
    public static final int Products_on_Submit=2;
    //
    public static final int companyMail=0;
    public static final int UserMail=1;
    //Product UOM Type
    public static final int UOMSchema=0;
    public static final int PackagingUOM=1;
    
    //Adding for Custom column for customer/vendor
    public static final String Acc_Opening_Payment_modulename = "Opening Payment";
    public static final String Acc_Opening_Receipt_modulename = "Opening Receipt";
    public static final String Acc_BankReconciliation_modulename = "Bank Reconciliation";
    public static final String Acc_Account_modulename = "Account";
    public static final String Acc_Customer_modulename = "Customer";
    public static final String Acc_Vendor_modulename = "Vendor";
    public static final String Acc_Accountid = "AccountId";
    public static final String Acc_VendorId = "VendorId";
    public static final String Acc_CustomerId = "CustomerId";
    public static final String Acc_Account_custom_data_classpath = "com.krawler.hql.accounting.AccountCustomData";
    public static final String Acc_Customer_custom_data_classpath = "com.krawler.hql.accounting.CustomerCustomData";
    public static final String Acc_Vendor_custom_data_classpath = "com.krawler.hql.accounting.VendorCustomData";
    public static final String CUSTOMER_ADDRESS_DETAILS_MODULE_NAME = "Customer Address Details";
    public static final String CUSTOMER_CATEGORY_MODULE_NAME = "Customer Category";
    public static final String VENDOR_CATEGORY_MODULE_NAME = "Vendor Category";
    public static final String ASSEMBLY_PRODUCT_MODULE_NAME = "Assembly Product";
    
    //Adding for Custom column for MRPMaster
    public static final String Acc_Labour_modulename = "Labour";
    public static final String Acc_MRPWorkCentre_Modulename = "WorkCentre";
    public static final String Acc_MRPMachineMaster_Modulename = "Machine";
    public static final String Acc_MRPWorkOrder_Modulename = "WorkOrder";
    public static final String Acc_MRPMasterContract_Modulename = "MRPContract";
    public static final String Acc_MRPMasterContractDetails_Modulename = "ContractDetails";
    public static final String Acc_MRPRoutingTemplate_Modulename = "RoutingTemplate";
    public static final String Acc_MRPJobWork_Modulename = "JobWork";
    
    public static final String Acc_LabourId = "LabourId";
    public static final String Acc_MRPWorkCentre_Id = "WorkCentreId";
    public static final String Acc_MRPMachineMaster_Id = "MachineId";
    public static final String Acc_MRPWorkOrder_Id = "WorkOrderId";
    public static final String Acc_MRPMasterContract_Id = "ContractId";
    public static final String Acc_MRPMasterContractDetails_Id = "ContractDetailsId";
    public static final String Acc_MRPRoutingTemplate_Id = "RoutingTemplateId";
    public static final String Acc_MRPJobWork_Id = "JobWorkId";
    public static final String CustomField_ColumnCreationMultiEntityDimension = "columncreationmultientitydimension";
    public static final String CustomField_ColumnCreationMultiEntityDimensionParent = "columncreationmultientitydimensionparent";
    public static final String CustomField_ColumnCreationJobWorkOut = "jobworkoutorder";
    public static final String CustomField_ColumnCreationJobWorkOutParent = "jobworkoutorderparent";
    public static final String Title_MultiEntityDimension = "Multi Entity Dimension";
    
    public static final String Acc_Labour_custom_data_classpath = "com.krawler.spring.mrp.labormanagement.LabourCustomData";
    public static final String Acc_MRPWorkCentre_CustomData_classpath = "com.krawler.spring.mrp.workcentremanagement.WorkCentreCustomData";
    public static final String Acc_MRPMachineMaster_CustomData_classpath = "com.krawler.spring.mrp.machinemanagement.MachineCustomData";
    public static final String Acc_MRPWorkOrder_CustomData_classpath = "com.krawler.spring.mrp.WorkOrder.WorkOrderCustomData";
    public static final String Acc_MRPMasterContract_CustomData_classpath = "com.krawler.spring.mrp.contractmanagement.MRPContractCustomData";
    public static final String Acc_MRPMasterContractDetails_CustomData_classpath = "com.krawler.spring.mrp.contractmanagement.MRPContractDetailsCustomData";
    public static final String Acc_MRPRoutingTemplate_CustomData_classpath = "com.krawler.spring.mrp.routingmanagement.RoutingTemplateCustomData";
    public static final String Acc_MRPJobWork_CustomData_classpath = "com.krawler.spring.mrp.jobwork.JobWorkCustomData";
    
    public static final String Acc_DeliveryOrder_modulename = "DeliveryOrder";
    public static final String Acc_DeliveryOrderid = "DeliveryOrderId";
    public static final String packRecord = "packRecord";
    public static final String Acc_DeliveryOrder_custom_data_classpath = "com.krawler.hql.accounting.DeliveryOrderCustomData";
    public static final String Acc_OpeningBalanceInvoice_modulename ="OpeningBalanceInvoice";
    public static final String Acc_OpeningBalanceInvoiceid = "OpeningBalanceInvoiceId";
    public static final String Acc_OpeningBalanceInvoice_custom_data_classpath = "com.krawler.hql.accounting.OpeningBalanceInvoiceCustomData";
    public static final String Acc_OpeningBalanceVendorInvoice_modulename ="OpeningBalanceVendorInvoice";
    public static final String Acc_OpeningBalanceVendorInvoiceid = "OpeningBalanceVendorInvoiceId";
    public static final String Acc_OpeningBalanceVendorInvoice_custom_data_classpath = "com.krawler.hql.accounting.OpeningBalanceVendorInvoiceCustomData";
    public static final String Acc_OpeningBalanceMakePayment_modulename ="OpeningBalanceMakePayment";
    public static final String Acc_OpeningBalanceMakePaymentid = "OpeningBalanceMakePaymentId";
    public static final String Acc_OpeningBalanceMakePayment_custom_data_classpath = "com.krawler.hql.accounting.OpeningBalanceMakePaymentCustomData";
    
    public static final String Acc_OpeningBalanceReceipt_modulename ="OpeningBalanceReceipt";
    public static final String Acc_OpeningBalanceReceiptid = "OpeningBalanceReceiptId";
    public static final String Acc_OpeningBalanceReceipt_custom_data_classpath = "com.krawler.hql.accounting.OpeningBalanceReceiptCustomData";
   
    public static final String Acc_OpeningBalanceCreditNote_modulename ="OpeningBalanceCreditNote";
    public static final String Acc_OpeningBalanceCreditNoteid = "OpeningBalanceCreditNoteId";
    public static final String Acc_OpeningBalanceCreditNote_custom_data_classpath = "com.krawler.hql.accounting.OpeningBalanceCreditNoteCustomData";
   
    public static final String Acc_OpeningBalanceDebitNote_modulename ="OpeningBalanceDebitNote";
    public static final String Acc_OpeningBalanceDebitNoteid = "OpeningBalanceDebitNoteId";
    public static final String Acc_OpeningBalanceDebitNote_custom_data_classpath = "com.krawler.hql.accounting.OpeningBalanceDebitNoteCustomData";
   
    
    public static final String Acc_GoodsReceipt_modulename = "GoodsReceiptOrder";
    public static final String Acc_PurchaseInvoice_modulename = "GoodsReceipt";
    public static final String Acc_GoodsReceiptId = "GoodsReceiptOrderId";
    public static final String Acc_GoodsReceipt_custom_data_classpath = "com.krawler.hql.accounting.GoodsReceiptOrderCustomData";
    public static final String Acc_SalesReturn_modulename = "SalesReturn";
    public static final String Acc_SalesReturnId = "SalesReturnId";
    public static final String Acc_SalesReturn_custom_data_classpath = "com.krawler.hql.accounting.SalesReturnCustomData";
    public static final String Acc_PurchaseReturn_modulename = "PurchaseReturn";
    public static final String Acc_PurchaseReturnId = "PurchaseReturnId";
    public static final String Acc_PurchaseReturn_custom_data_classpath = "com.krawler.hql.accounting.PurchaseReturnCustomData";
    public static final String Acc_Quotation_modulename = "Quotation";
    public static final String Acc_QuotationId = "QuotationId";
    public static final String Acc_Quotation_custom_data_classpath = "com.krawler.hql.accounting.QuotationCustomData";
    public static final String Acc_QuotationVersion_custom_data_classpath = "com.krawler.hql.accounting.QuotationVersionCustomData";
    public static final String Acc_VendorQuotationVersion_custom_data_classpath = "com.krawler.hql.accounting.VendorQuotationVersionCustomData";
    public static final String Acc_VendorQuotation_modulename = "VendorQuotation";
    public static final String Acc_VendorQuotationId = "VendorQuotationId";
    public static final String Acc_VendorQuotation_custom_data_classpath = "com.krawler.hql.accounting.VendorQuotationCustomData";
    public static final String Acc_Purchase_Requisition_modulename = "PurchaseRequisition";
    public static final String Acc_PurchaseRequisitionId = "PurchaseRequisitionId";
    public static final String Acc_PurchaseRequisition_custom_data_classpath = "com.krawler.hql.accounting.PurchaseRequisitionCustomData";
    public static final String Acc_RFQ_modulename = "RequestForQuotation";
    public static final String Acc_RFQId = "rfqId";
    public static final String Acc_RFQ_custom_data_classpath = "com.krawler.hql.accounting.RFQCustomData";
    //Adding QA Approval Status in Purchase Order Details    
    public static final int Approved = 0;
    public static final int Pending_QA_Approval = 1;
    public static final int QA_Rejected = 2;
    //Item Reusability 
    public static final String REUSABLE = "Reusable";
    public static final String CONSUMABLE = "Consumable";
    //Inventory Sysstems Custom Field Names 
    public static final String CUSTOM_STOP_PURCHASE = "Custom_Stop Purchase";
    public static final String CUSTOM_OBSOLETE = "Custom_Obsolete";
    public static final int CUSTOM_CHECKBOX = 11;
    public static final String productCategoryid = "productCategoryid";
    public static final String PRODUCTCATEGORY = "Product Category";
    public static final String PRODUCTBRAND = "Product Brand";
    public static final String salesPersonid = "salesPersonid";
    public static final String PERSONCODE = "personcode";
    public static final String accid = "accid";
    public static final String creditonly = "creditonly";
    public static final String prodfiltercustid = "prodfiltercustid";
    public static final String customerCategoryid = "customerCategoryid";
    public static final String checksoforcustomer = "checksoforcustomer";
    public static final String REQ_costCenterId = "costCenterId";
    public static final String REQ_vendorId = "newvendorid";
    public static final String REQ_customerId = "newcustomerid";
    public static final String BILLING_ADDRESS = "billingAddress";
    public static final String BILLING_COUNTRY = "billingCountry";
    public static final String BILLING_STATE = "billingState";
    public static final String BILLING_COUNTY = "billingCounty";
    public static final String BILLING_CITY = "billingCity";
    public static final String BILLING_POSTAL = "billingPostal";
    public static final String BILLING_EMAIL = "billingEmail";
    public static final String BILLING_FAX = "billingFax";
    public static final String BILLING_MOBILE = "billingMobile";
    public static final String BILLING_PHONE = "billingPhone";
    public static final String BILLING_RECIPIENT_NAME = "billingRecipientName";
    public static final String BILLING_CONTACT_PERSON = "billingContactPerson";
    public static final String BILLING_CONTACT_PERSON_NUMBER = "billingContactPersonNumber";
    public static final String BILLING_CONTACT_PERSON_DESIGNATION = "billingContactPersonDesignation";
    public static final String BILLING_WEBSITE = "billingWebsite";
    public static final String BILLING_ADDRESS_TYPE = "billingAddressType";
    
    public static final String DropShip_BILLING_ADDRESS = "dropshipbillingAddress";
    public static final String DropShip_BILLING_COUNTRY = "dropshipbillingCountry";
    public static final String DropShip_BILLING_STATE = "dropshipbillingState";
    public static final String DropShip_BILLING_COUNTY = "dropshipbillingCounty";
    public static final String DropShip_BILLING_CITY = "dropshipbillingCity";
    public static final String DropShip_BILLING_POSTAL = "dropshipbillingPostal";
    public static final String DropShip_BILLING_EMAIL = "dropshipbillingEmail";
    public static final String DropShip_BILLING_FAX = "dropshipbillingFax";
    public static final String DropShip_BILLING_MOBILE = "dropshipbillingMobile";
    public static final String DropShip_BILLING_PHONE = "dropshipbillingPhone";
    public static final String DropShip_BILLING_RECIPIENT_NAME = "dropshipbillingRecipientName";
    public static final String DropShip_BILLING_CONTACT_PERSON = "dropshipbillingContactPerson";
    public static final String DropShip_BILLING_CONTACT_PERSON_NUMBER = "dropshipbillingContactPersonNumber";
    public static final String DropShip_BILLING_CONTACT_PERSON_DESIGNATION = "dropshipbillingContactPersonDesignation";
    public static final String DropShip_BILLING_WEBSITE = "dropshipbillingWebsite";
    public static final String DropShip_BILLING_ADDRESS_TYPE = "dropshipbillingAddressType";
    
    public static final String VENDOR_BILLING_ADDRESS = "vendorbillingAddressForINDIA";
    public static final String VENDOR_BILLING_COUNTRY = "vendorbillingCountryForINDIA";
    public static final String VENDOR_BILLING_STATE = "vendorbillingStateForINDIA";
    public static final String VENDOR_BILLING_COUNTY = "vendorbillingCountyForINDIA";
    public static final String VENDOR_BILLING_CITY = "vendorbillingCityForINDIA";
    public static final String VENDOR_BILLING_POSTAL = "vendorbillingPostalForINDIA";
    public static final String VENDOR_BILLING_EMAIL = "vendorbillingEmailForINDIA";
    public static final String VENDOR_BILLING_FAX = "vendorbillingFaxForINDIA";
    public static final String VENDOR_BILLING_MOBILE = "vendorbillingMobileForINDIA";
    public static final String VENDOR_BILLING_PHONE = "vendorbillingPhoneForINDIA";
    public static final String VENDOR_BILLING_RECIPIENT_NAME = "vendorbillingRecipientNameForINDIA";
    public static final String VENDOR_BILLING_CONTACT_PERSON = "vendorbillingContactPersonForINDIA";
    public static final String VENDOR_BILLING_CONTACT_PERSON_NUMBER = "vendorbillingContactPersonNumberForINDIA";
    public static final String VENDOR_BILLING_CONTACT_PERSON_DESIGNATION = "vendorbillingContactPersonDesignationForINDIA";
    public static final String VENDOR_BILLING_WEBSITE = "vendorbillingWebsiteForINDIA";
    public static final String VENDOR_BILLING_ADDRESS_TYPE = "vendorbillingAddressTypeForINDIA";
    
    public static final String SHIPPING_ADDRESS = "shippingAddress";
    public static final String SHIPPING_COUNTRY = "shippingCountry";
    public static final String SHIPPING_STATE = "shippingState";
    public static final String SHIPPING_COUNTY = "shippingCounty";
    public static final String SHIPPING_CITY = "shippingCity";
    public static final String SHIPPING_EMAIL = "shippingEmail";
    public static final String SHIPPING_FAX = "shippingFax";
    public static final String SHIPPING_MOBILE = "shippingMobile";
    public static final String SHIPPING_PHONE = "shippingPhone";
    public static final String SHIPPING_POSTAL = "shippingPostal";
    public static final String SHIPPING_CONTACT_PERSON_NUMBER = "shippingContactPersonNumber";
    public static final String SHIPPING_CONTACT_PERSON_DESIGNATION = "shippingContactPersonDesignation";
    public static final String SHIPPING_WEBSITE = "shippingWebsite";
    public static final String SHIPPING_CONTACT_PERSON = "shippingContactPerson";
    public static final String SHIPPING_RECIPIENT_NAME = "shippingRecipientName";
    public static final String SHIPPING_ROUTE = "shippingRoute";    
    public static final String SHIPPING_ADDRESS_TYPE = "shippingAddressType";
    public static final String VENDCUST_SHIPPING_ADDRESS = "vendcustShippingAddress";
    public static final String VENDCUST_SHIPPING_COUNTRY = "vendcustShippingCountry";
    public static final String VENDCUST_SHIPPING_STATE = "vendcustShippingState";
    public static final String VENDCUST_SHIPPING_COUNTY = "vendcustShippingCounty";
    public static final String VENDCUST_SHIPPING_CITY = "vendcustShippingCity";
    public static final String VENDCUST_SHIPPING_EMAIL = "vendcustShippingEmail";
    public static final String VENDCUST_SHIPPING_FAX = "vendcustShippingFax";
    public static final String VENDCUST_SHIPPING_MOBILE = "vendcustShippingMobile";
    public static final String VENDCUST_SHIPPING_PHONE = "vendcustShippingPhone";
    public static final String VENDCUST_SHIPPING_POSTAL = "vendcustShippingPostal";
    public static final String VENDCUST_SHIPPING_CONTACT_PERSON_NUMBER = "vendcustShippingContactPersonNumber";
    public static final String VENDCUST_SHIPPING_CONTACT_PERSON_DESIGNATION = "vendcustShippingContactPersonDesignation";
    public static final String VENDCUST_SHIPPING_WEBSITE = "vendcustShippingWebsite";
    public static final String VENDCUST_SHIPPING_CONTACT_PERSON = "vendcustShippingContactPerson";
    public static final String VENDCUST_SHIPPING_RECIPIENT_NAME = "vendcustShippingRecipientName";
    public static final String VENDCUST_SHIPPING_ADDRESS_TYPE = "vendcustShippingAddressType";
    public static final String CUSTOMER_SHIPPING_ADDRESS = "customerShippingAddress";
    public static final String CUSTOMER_SHIPPING_COUNTRY = "customerShippingCountry";
    public static final String CUSTOMER_SHIPPING_STATE = "customerShippingState";
    public static final String CUSTOMER_SHIPPING_COUNTY = "customerShippingCounty";
    public static final String CUSTOMER_SHIPPING_CITY = "customerShippingCity";
    public static final String CUSTOMER_SHIPPING_EMAIL = "customerShippingEmail";
    public static final String CUSTOMER_SHIPPING_FAX = "customerShippingFax";
    public static final String CUSTOMER_SHIPPING_MOBILE = "customerShippingMobile";
    public static final String CUSTOMER_SHIPPING_PHONE = "customerShippingPhone";
    public static final String CUSTOMER_SHIPPING_POSTAL = "customerShippingPostal";
    public static final String CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER = "customerShippingContactPersonNumber";
    public static final String CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION = "customerShippingContactPersonDesignation";
    public static final String CUSTOMER_SHIPPING_WEBSITE = "customerShippingWebsite";
    public static final String CUSTOMER_SHIPPING_CONTACT_PERSON = "customerShippingContactPerson";
    public static final String CUSTOMER_SHIPPING_RECIPIENT_NAME = "customerShippingRecipientName";
    public static final String CUSTOMER_SHIPPING_ADDRESS_TYPE = "customerShippingAddressType";
    public static final String CUSTOMER_SHIPPING_ROUTE = "customerShippingRoute";
    public static final String IS_IBG_BANK = "isibgbank";
    public static final String IBG_BANK_TYPE = "ibgbanktype";
    public static final String IBG_BANK_DETAIL_ID = "ibgbankdetailid";
    public static final String CIMB_BANK_DETAIL_ID = "cimbbankdetailid";
    public static final String UOB_BANK_DETAIL_ID = "uobbankdetailid";
    public static final String OCBC_BANK_DETAIL_ID = "ocbcbankdetailid";
    public static final String IBG_BANK = "ibgbank";
    public static final String BANK_CODE = "bankCode";
    public static final String BRANCH_CODE = "branchCode";
    public static final String ACCOUNT_NUMBER = "accountNumber";
    public static final String ACCOUNT_NAME = "accountName";
    public static final String SENDERS_COMPANYID = "sendersCompanyID";
    public static final String BANK_DAILY_LIMIT = "bankDailyLimit";
    public static final String ACTIVATE_SERIAL_NO = "Activate Serial No";
    public static final String ACTIVATE_LOCATION = "Activate Location";
    public static final String ACTIVATE_WAREHOUSE = "Activate Warehouse";
    public static final String ACTIVATE_BATCH = "Activate Batch";
    public static final String ACTIVATE_ROW="Activate Row";
    public static final String ACTIVATE_RACK="Activate Rack";
    public static final String ACTIVATE_BIN="Activate Bin";
    public static final int VENDOR = 1;
    public static final int CUSTOMER = 2;
    public static final int CashInHandAccountTye = 4;  
    public static int AMOUNT_DIGIT_AFTER_DECIMAL = 2;
    public static int GSTValue_DIGIT_AFTER_DECIMAL = 2;
    public static int QUANTITY_DIGIT_AFTER_DECIMAL = 4;
    public static int UNITPRICE_DIGIT_AFTER_DECIMAL = 2;
    public static int UOMCONVERSIONRATE_DIGIT_AFTER_DECIMAL = 6;
    public static int EXCHANGERATE_DIGIT_AFTER_DECIMAL_FOR_IMPORT = 14; // For importing exchange rates maximum up to 14 decimal places
    public static int EXCHANGERATE_DIGIT_AFTER_DECIMAL_FOR_TRANSACTION= 16; // For document level exchange rates maximum up to 16 decimal places
    public static final String Acc_Ad_Hoc_InvoiceId = "ff808081434d75f2014351835fc70003";
    public static final String Acc_Marine_InvoiceId = "ff808081434d75f201435182a6270002";
    public static final String Acc_Retail_Invoice_Fixed = "ff808081434d75f20143518400630005";
    public static final String Acc_Retail_Invoice_Variable = "ff808081434d75f20143518438fe0006";
    public static final String Acc_Visitor_Pass_Invoice = "ff808081434d75f201435183b3270004";
    public static final String Acc_Car_Park_Operator = "ff808081434d75f20143518400630008";
    public static final String Acc_Water_Sale ="ff808081434d75f201435183b3270007";
    public static final String Acc_Security_Officer ="ff808081434d75f20143518400630009";
    public static final String Acc_Event = "ff808081434d75f20143518400630010";
    public static final String invoiceamountdue = "invoiceamountdue";
    public static final String cnamountdue = "cnamountdue";
    public static final String claimAmountDue = "claimamountdue";
    public static final String invoiceamount = "invoiceamount";
    public static final String invoiceamountinbase = "invoiceamountinbase";
    public static final String originalOpeningBalanceBaseAmount = "originalOpeningBalanceBaseAmount";
    public static final String openingBalanceBaseAmountDue = "openingBalanceBaseAmountDue";
    public static final String openingBalanceAmountDue = "openingBalanceAmountDue";
    public static final String amountDueDate = "amountduedate";
//    public static final String MALASIAN_GST_SR_TAX_CODE = "GST(SR)";
//    public static final String MALASIAN_GST_ZRE_TAX_CODE = "GST(ZRE)";
    public static final String MALASIAN_GST_AJP1_TAX_CODE = "GST(AJP1)";
    public static final String MALASIAN_GST_AJS1_TAX_CODE = "GST(AJS1)";
    public static final double MALAYSIAN_RETAIL_PURCHASE_TAX_AMOUNT_LIMIT = 30;
    public static final double MALAYSIAN_RETAIL_PURCHASE_INVOICE_AMOUNT_LIMIT = 500;
    public static final String MALAYSIAN_GST_OUTPUT_TAX = "GST(Output)";
    public static final String MALAYSIAN_GST_OUTPUT_TAX_OPTIONAL = "GST(Output tax)";
    public static final String MALAYSIAN_GST_INPUT_TAX = "GST(Input)";
    public static final String MALAYSIAN_GST_INPUT_TAX_OPTIONAL = "GST(Input tax)";
    public static final String MALAYSIAN_GST_CONTROL_TAX = "GST(Control)";
    
    // Malaysian GST Purchase Tax Codes
    
    public static final String MALAYSIAN_GST_TX_TAX_CODE = "GST(TX)";
    public static final String MALAYSIAN_GST_IM_TAX_CODE = "GST(IM)";
    public static final String MALAYSIAN_GST_IS_TAX_CODE = "GST(IS)";
    public static final String MALAYSIAN_GST_BL_TAX_CODE = "GST(BL)";
    public static final String MALAYSIAN_GST_NR_TAX_CODE = "GST(NR)";
    public static final String MALAYSIAN_GST_ZP_TAX_CODE = "GST(ZP)";
    public static final String MALAYSIAN_GST_EP_TAX_CODE = "GST(EP)";
    public static final String MALAYSIAN_GST_OP_TAX_CODE = "GST(OP)";
    public static final String MALAYSIAN_GST_TX_IES_TAX_CODE = "GST(TX-IES)";       //TX-E43 renamed as TX-IES-(ERP-34006)
    public static final String MALAYSIAN_GST_TX_ES_TAX_CODE = "GST(TX-ES)";         //TX-N43 renamed as TX-ES-(ERP-34006)
    public static final String MALAYSIAN_GST_TX_RE_TAX_CODE = "GST(TX-RE)";
    public static final String MALAYSIAN_GST_GP_N43_TAX_CODE = "GST(GP)";
    public static final String MALAYSIAN_GST_AJP_TAX_CODE = "GST(AJP)";
    public static final String MALAYSIAN_GST_TX_CG_TAX_CODE = "GST(TX-CG)";
    /*
     *Added New Purchase Tax RP,TX-FRS,TX-NC & NP
     *Please Refer - ERP-34006
     */
    public static final String MALAYSIAN_GST_RP_TAX_CODE = "GST(RP)";
    public static final String MALAYSIAN_GST_TX_FRS_TAX_CODE = "GST(TX-FRS)";
    public static final String MALAYSIAN_GST_TX_NC_TAX_CODE = "GST(TX-NC)";
    public static final String MALAYSIAN_GST_NP_TAX_CODE = "GST(NP)";
    public static final String MALAYSIAN_GST_IM_CG_CODE = "GST(IM-CG)";
    
    // Malaysian GST Sales Tax Codes
    
    public static final String MALAYSIAN_GST_DS_TAX_CODE = "GST(DS)";
    public static final String MALAYSIAN_GST_SR_TAX_CODE = "GST(SR)";
    public static final String MALAYSIAN_GST_ZRL_TAX_CODE = "GST(ZRL)";
    public static final String MALAYSIAN_GST_ZRE_TAX_CODE = "GST(ZRE)";
    public static final String MALAYSIAN_GST_IES_TAX_CODE = "GST(IES)";            //ES43 renamed as IES-(ERP-34006)
    public static final String MALAYSIAN_GST_OS_TAX_CODE = "GST(OS)";
    public static final String MALAYSIAN_GST_ES_TAX_CODE = "GST(ES)";
    public static final String MALAYSIAN_GST_RS_TAX_CODE = "GST(RS)";
    public static final String MALAYSIAN_GST_GS_TAX_CODE = "GST(GS)";
    public static final String MALAYSIAN_GST_AJS_TAX_CODE = "GST(AJS)";
    /*
     *Added New Sales Tax ZDA,SR-MS,SR-JWS,OS-TXM,NTX & NS 
     * Please Refer - ERP-34006
     */
    public static final String MALAYSIAN_GST_ZDA_TAX_CODE = "GST(ZDA)";
    public static final String MALAYSIAN_GST_SR_MS_TAX_CODE = "GST(SR-MS)";
    public static final String MALAYSIAN_GST_SR_JWS_TAX_CODE = "GST(SR-JWS)";
    public static final String MALAYSIAN_GST_OS_TXM_TAX_CODE = "GST(OS-TXM)";
    public static final String MALAYSIAN_GST_NTX_TAX_CODE = "GST(NTX)";
    public static final String MALAYSIAN_GST_NS_TAX_CODE = "GST(NS)";
    
    //Zero Rated Duplicate Purchase Taxes.
    public static final String MALAYSIAN_GST_TX0_TAX_CODE = "GST(TX)@0%";
    public static final String MALAYSIAN_GST_TX_CG0_TAX_CODE = "GST(TX-CG)@0%";
    public static final String MALAYSIAN_GST_TX_ES0_TAX_CODE = "GST(TX-ES)@0%";
    public static final String MALAYSIAN_GST_TX_IES0_TAX_CODE = "GST(TX-IES)@0%";
    public static final String MALAYSIAN_GST_TX_RE0_TAX_CODE = "GST(TX-RE)@0%";
    public static final String MALAYSIAN_GST_IM0_TAX_CODE = "GST(IM)@0%";
    public static final String MALAYSIAN_GST_IM_CG0_CODE = "GST(IM-CG)@0%";
    public static final String MALAYSIAN_GST_BL0_TAX_CODE = "GST(BL)@0%";
    public static final String MALAYSIAN_GST_TX_NC0_TAX_CODE = "GST(TX-NC)@0%";
    public static final String MALAYSIAN_GST_AJP0_TAX_CODE = "GST(AJP)@0%";
    public static final String MALAYSIAN_GST_TX_FRS0_TAX_CODE = "GST(TX-FRS)@0%";

    //Zero Rated Duplicate Sales Taxes.
    public static final String MALAYSIAN_GST_DS0_TAX_CODE = "GST(DS)@0%";
    public static final String MALAYSIAN_GST_SR0_TAX_CODE = "GST(SR)@0%";
    public static final String MALAYSIAN_GST_AJS0_TAX_CODE = "GST(AJS)@0%";
    public static final String MALAYSIAN_GST_SR_MS0_TAX_CODE = "GST(SR-MS)@0%";
    
    //fieldid constants
    public static final int FIELDID_DATE = 3;
    public static final String ValidFlag = "validflag";
    public static final String BillDate = "billdate";
    public static final String filetype = "filetype";
    public static final String print = "print";
    //Countries List
    public static final String SINGAPOREID = "203";
    public static final String INDONESIAN_COUNTRYID = "106";
    public static final String INDIA_COUNTRYID = "105";
    public static final String DateLabel_MailContent_Placeholder = "#Date_Val#";
    public static final String Due_DateLabel_MailContent_Placeholder = "#Due_Date_Val#";
    public static final String TodayDateLabel_MailContent_Placeholder = "#Date_Today#";
    public static final HashMap<Integer, String> moduleID_NameMap = new HashMap<Integer, String>();
    public static final String ConsignmentSales_DueDate_Passed = "Consignment Loan Due Date Passed";
    //Image Storage Folder Name
    public static final String ProductImages = "ProductImages/";
    
    public static final String isConsignment = "isConsignment";
    public static final String userid = "userId";//SalesOrder & CustomerQuotation Export userId variable
    public static final String isJobOrderFlow = "isJobOrderFlow";
    public static final String SUBTYPE_JOB_ORDER = "3";
    public static final String SUBTYPE_JOB_ORDER_LABEL = "4";
    public static final String CUSTVENTYPE_Export = "Export (WPAY)";
    public static final String CUSTVENTYPE_ExportWOPAY = "Export (WOPAY)";
   public static final String CUSTVENTYPE_Import = "Import";
    public static final String CUSTVENTYPE_SEZ = "SEZ (WPAY)";
    public static final String CUSTVENTYPE_SEZWOPAY = "SEZ (WOPAY)";
    public static final String CUSTVENTYPE_NA = "NA";
    public static final String CUSTVENTYPE_TAXEXEMPT="Tax Exempt";
    public static final String CUSTVENTYPE_DEEMED_EXPORT = "Deemed_Export";
    public static final Map<String, String> CUSTVENTYPE = new HashMap<String, String>();
    static{
        CUSTVENTYPE.put(CUSTVENTYPE_Export,"ac09adba-58b9-11e7-8ead-c03fd5658535");
        CUSTVENTYPE.put(CUSTVENTYPE_ExportWOPAY,"e41d586f-8d30-11e7-b941-6045cb6f9ab5");
        CUSTVENTYPE.put(CUSTVENTYPE_Import,"ac09adba-58b9-11e7-8ead-c03fd5658536");
        CUSTVENTYPE.put(CUSTVENTYPE_SEZ,"ac09adba-58b9-11e7-8ead-c03fd5658537");
        CUSTVENTYPE.put(CUSTVENTYPE_SEZWOPAY,"dc9ff578-a05a-11e7-b9d0-6045cb6f9ab5");
        CUSTVENTYPE.put(CUSTVENTYPE_DEEMED_EXPORT, "33c26e6c-9173-11e7-abc4-cec278b6b50a");
        CUSTVENTYPE.put(CUSTVENTYPE_NA,"47d48400-6789-11e7-b99d-14dda97927f2");
        CUSTVENTYPE.put(CUSTVENTYPE_TAXEXEMPT,"c76dbb92-be25-11e7-a8c4-6045cb6f9ab5");
    }
    static {
        moduleID_NameMap.put(Acc_Invoice_ModuleId, "Sales Invoice");
        moduleID_NameMap.put(Acc_Vendor_Invoice_ModuleId, "Purchase Invoice");
        moduleID_NameMap.put(Acc_Customer_Quotation_ModuleId, "Customer Quotation");
        moduleID_NameMap.put(Acc_Vendor_Quotation_ModuleId, "Vendor Quotation");
        moduleID_NameMap.put(Acc_Purchase_Order_ModuleId, "Purchase Order");
        moduleID_NameMap.put(Acc_Sales_Order_ModuleId, "Sales Order");
        moduleID_NameMap.put(Acc_Delivery_Order_ModuleId, "Delivery Order");
        moduleID_NameMap.put(Acc_GENERAL_LEDGER_ModuleId, "Journal Entry");
        moduleID_NameMap.put(Acc_Goods_Receipt_ModuleId, "Goods Receipt Order");
        moduleID_NameMap.put(Acc_Sales_Return_ModuleId, "Sales Return");
        moduleID_NameMap.put(Acc_Purchase_Return_ModuleId, "Purchase Return");
        moduleID_NameMap.put(Acc_Customer_ModuleId, "Customer");
        moduleID_NameMap.put(Acc_Vendor_ModuleId, "Vendor");
        moduleID_NameMap.put(Acc_Contract_Order_ModuleId, "Contract");
        moduleID_NameMap.put(Asset_Maintenance_ModuleId, "Asset Maintenance");
        moduleID_NameMap.put(Acc_Product_Master_ModuleId, "Product");
        moduleID_NameMap.put(Account_Statement_ModuleId, "Account Statement");
        moduleID_NameMap.put(LEASE_INVOICE_MODULEID, "Lease Invoice");
        moduleID_NameMap.put(Acc_Lease_Quotation, "Lease Quotation");
        moduleID_NameMap.put(Acc_Lease_Order_ModuleId, "Lease Order");
        moduleID_NameMap.put(Acc_Lease_Contract , "Lease Contract");
        moduleID_NameMap.put(Acc_ConsignmentRequest_ModuleId , "Consignment Stock Sales Request");
        moduleID_NameMap.put(Acc_ConsignmentStockPurchaseRequest_ModuleId , "Consignment Stock Purchase Request");
        moduleID_NameMap.put(Acc_ConsignmentDeliveryOrder_ModuleId , "Consignment Stock Sales Delivery Order");
        moduleID_NameMap.put(Acc_ConsignmentInvoice_ModuleId , "Consignment Stock Sales Invoice");
        moduleID_NameMap.put(Acc_ConsignmentSalesReturn_ModuleId , "Consignment Stock Sales Return");
        moduleID_NameMap.put(Acc_Consignment_GoodsReceiptOrder_ModuleId , "Consignment Stock Goods Receipt Order");
        moduleID_NameMap.put(Acc_Consignment_GoodsReceipt_ModuleId , "Consignment Stock Purchase Invoice");
        moduleID_NameMap.put(Acc_ConsignmentPurchaseReturn_ModuleId , "Consignment Stock Purchase Return");
        moduleID_NameMap.put(CONSIGNMENT_SALES_MODULE , "Consignment Sales");
        moduleID_NameMap.put(CONSIGNMENT_PURCHASE_MODULE , "Consignment Purchase");
        moduleID_NameMap.put(Acc_Make_Payment_ModuleId, "Make Payment");
        moduleID_NameMap.put(Acc_Receive_Payment_ModuleId, "Receive Payment");
        moduleID_NameMap.put(Acc_SecurityGateEntry_ModuleId, "Security Gate Entry");
        moduleID_NameMap.put(Acc_Purchase_Requisition_ModuleId,"Purchase Requisition" );
      
    }
    //Currency List
    public static final String SGDID = "6";
    public static final String RMID = "7";
    public static final String dueDate = "Due Date";
    public static final String SO_Date = "Sales Order Date";
    public static final String PO_Date = "Purchase Order Date";
    public static final String DO_Date = "Delivery Order Date";
    public static final String DOEXp_Date = "Delivery Order Exp.Date";
    public static final String GRO_Date = "Goods Receipt Order Date";
    public static final String GROEXp_Date = "Goods Receipt Exp. Order Date";
    public static final String SR_Date = "Sales Return Date";
    public static final String PR_Date = "Purchase Return Date";
    public static final String CUST_CREATION_Date = "Customer Creation Date";
    public static final String VEND_CREATION_Date = "Vendor Creation Date";
    public static final String VEND_Self_Billed_Approval_Expiry_Date = "Self-billed Approval Expiry Date";
    public static final String JE_Date = "Journal Entry Date";
    public static final String GR_DO_Sr_Check_Date = "GR-DO Serials Check Date";
    public static final String CONTRACT_EXPIRY_DATE = "Contract Expiry Date";
    public static final String Asset_Schedule_Start_Date = "Schedule Start Date";
    public static final String Asset_Schedule_End_Date = "Schedule End Date";
    public static final String Product_Purchase_Date = "Product Purchase Date";
    public static final String Product_Expiry_Date = "Product Expiry Date";
    public static final String Product_QA_Inspection_Rejection = "Product QA Inspection Rejection";
    public static final String Product_QA_Inspection_Approval = "Product QA Inspection Approval";
    public static final String Email_Button_From_Report = "Email Button From Report";
    public static final String ON_APPROVAL_EMAIL = "On Approval";
    public static final String ON_REJECTION_EMAIL = "On Rejection";
    public static final String Invoice_Date = "Invoice Date";
    
    public static final String ConsignmentSales_Request_Creation = "Request Creation";
    public static final String ConsignmentSales_Request_Edition = "Request Edition";
    public static final String ConsignmentSales_Request_Approval = "Request Approval";
    public static final String ConsignmentSales_DO_Creation = "Delivery Order Creation";
    public static final String ConsignmentSales_Return_Creation = "Consignment Return Creation";
    public static final String ConsignmentPurchase_Request_Creation = "Vendor Request Creation";
    public static final String ConsignmentPurchase_Request_Edition = "Vendor Request Edition";
    public static final String ConsignmentPurchase_GR_Creation = "GRN Creation";
    public static final String ConsignmentPurchase_Invoice_Creation = "Purchase Invoice Creation";
    public static final String ConsignmentPurchase_Return_Creation = "Purchase Return Creation";
    
    public static final HashMap<String, String> staticGlobalDateFields = new HashMap<String, String>();

    static {
        staticGlobalDateFields.put(dueDate, "{fieldlabel:'Due Date',fieldid:'1'}");
        staticGlobalDateFields.put(JE_Date, "{fieldlabel:'Journal Entry Date',fieldid:'1'}");
        staticGlobalDateFields.put(SO_Date, "{fieldlabel:'Sales Order Date',fieldid:'2'}");
        staticGlobalDateFields.put(PO_Date, "{fieldlabel:'Purchase Order Date',fieldid:'3'}");
        staticGlobalDateFields.put(DO_Date, "{fieldlabel:'Delivery Order Date',fieldid:'4'}");
        staticGlobalDateFields.put(DOEXp_Date, "{fieldlabel:'Delivery Order Exp. Date',fieldid:'10'}");
        staticGlobalDateFields.put(GRO_Date, "{fieldlabel:'Goods Receipt Order Date',fieldid:'5'}");
        staticGlobalDateFields.put(GROEXp_Date, "{fieldlabel:'Goods Receipt Exp.Order Date',fieldid:'11'}");
        staticGlobalDateFields.put(SR_Date, "{fieldlabel:'Sales Return Date',fieldid:'6'}");
        staticGlobalDateFields.put(PR_Date, "{fieldlabel:'Purchase Return Date',fieldid:'7'}");
        staticGlobalDateFields.put(CUST_CREATION_Date, "{fieldlabel:'Customer Creation Date',fieldid:'8'}");
        staticGlobalDateFields.put(VEND_CREATION_Date, "{fieldlabel:'Vendor Creation Date',fieldid:'9'}");
        staticGlobalDateFields.put(VEND_Self_Billed_Approval_Expiry_Date, "{fieldlabel:'Self-billed Approval Expiry Date',fieldid:'21'}");
        staticGlobalDateFields.put(GR_DO_Sr_Check_Date, "{fieldlabel:'GR-DO Serials Check Date',fieldid:'12'}");
        staticGlobalDateFields.put(CONTRACT_EXPIRY_DATE, "{fieldlabel:'Contract Expiry Date',fieldid:'13'}");
        staticGlobalDateFields.put(Asset_Schedule_Start_Date, "{fieldlabel:'Schedule Start Date',fieldid:'14'}");
        staticGlobalDateFields.put(Asset_Schedule_End_Date, "{fieldlabel:'Schedule End Date',fieldid:'15'}");
        staticGlobalDateFields.put(Product_Purchase_Date, "{fieldlabel:'Product Purchase Date',fieldid:'16'}");
        staticGlobalDateFields.put(Product_Expiry_Date, "{fieldlabel:'Product Expiry Date',fieldid:'17'}");
        staticGlobalDateFields.put(Product_QA_Inspection_Rejection, "{fieldlabel:'Product QA Inspection Rejection',fieldid:'18'}");
        staticGlobalDateFields.put(Product_QA_Inspection_Approval, "{fieldlabel:'Product QA Inspection Approval',fieldid:'19'}");
        staticGlobalDateFields.put(Email_Button_From_Report, "{fieldlabel:'Email Button From Report',fieldid:'20'}");
        staticGlobalDateFields.put(ON_APPROVAL_EMAIL, "{fieldlabel:'On Approval',fieldid:'22'}");
        staticGlobalDateFields.put(ON_REJECTION_EMAIL, "{fieldlabel:'On Rejection',fieldid:'23'}");
        staticGlobalDateFields.put(Invoice_Date, "{fieldlabel:'Invoice Date',fieldid:'24'}");
        
        staticGlobalDateFields.put(ConsignmentSales_Request_Creation, "{fieldlabel:'Request Creation',fieldid:'25'}");
        staticGlobalDateFields.put(ConsignmentSales_Request_Edition, "{fieldlabel:'Request Edition',fieldid:'26'}");
        staticGlobalDateFields.put(ConsignmentSales_Request_Approval, "{fieldlabel:'Request Approval',fieldid:'27'}");
        staticGlobalDateFields.put(ConsignmentSales_DO_Creation, "{fieldlabel:'Delivery Order Creation',fieldid:'28'}");
        staticGlobalDateFields.put(ConsignmentSales_Return_Creation, "{fieldlabel:'Consignment Return Creation',fieldid:'29'}");
        staticGlobalDateFields.put(ConsignmentPurchase_Request_Creation, "{fieldlabel:'Vendor Request Creation',fieldid:'30'}");
        staticGlobalDateFields.put(ConsignmentPurchase_Request_Edition, "{fieldlabel:'Vendor Request Edition',fieldid:'31'}");
        staticGlobalDateFields.put(ConsignmentPurchase_GR_Creation, "{fieldlabel:'GRN Creation',fieldid:'32'}");
        staticGlobalDateFields.put(ConsignmentPurchase_Invoice_Creation, "{fieldlabel:'Purchase Invoice Creation',fieldid:'33'}");
        staticGlobalDateFields.put(ConsignmentPurchase_Return_Creation, "{fieldlabel:'Purchase Return Creation',fieldid:'34'}");
        staticGlobalDateFields.put(ConsignmentSales_DueDate_Passed, "{fieldlabel:'Consignment Loan Due Date Passed ',fieldid:'35'}");
    }
    public static final int NoAuthorityToApprove = 999;
    public static boolean InvoiceAmountDueFlag = true;
    public static boolean OpeningBalanceBaseAmountFlag=true;
    public static final int Update_Maintenance_Status_To_CRM=205;
    public static final int DraftedPurchaseRequisitions = -99;
    public static final int MaximumLimitOfLevelsInMultilevelApproval=11;
    
    public static final int AdvancePayment=1;
    public static final int PaymentAgainstInvoice=2;
    public static final int PaymentAgainstCNDN=3;
    public static final int GLPayment=4;
    public static final int BALACEAMOUNT=55;
    public static final int AdvanceLinkedWithInvoicePayment=5;
    public static final int LocalAdvanceTypePayment=6;
    public static final int ExportAdvanceTypePayment=7;
    public static final int AdvanceLinkedWithNotePayment=8;
    public static final int PaymentAgainstLoanDisbursement=9;
    public static final int RefundPaymentAgainstAdvancePayment = 10;
//    public static HashMap<String, Attribute<ExchangeRateInfo, Comparable>> exchangeRateAttributes;
//    public static IndexedCollection<ExchangeRateInfo> exchangeRateInfo = null;
//    public static HashMap<String, Attribute<ExchangeRateDetailInfo, Comparable>> exchangeRateDetailsAttributes;
    public static boolean isNewPaymentStructure= true;
    
    public static final int Make_Payment_to_Vendor=1;
    public static final int Make_Payment_to_Customer=2;
    public static final int Make_Payment_against_GL_Code=3;
    public static final int Receive_Payment_from_Customer=1;
    public static final int Receive_Payment_from_Vendor=2;
    public static final int Receive_Payment_against_GL_Code=3;
    
    
    public static final String Journal_Entry_Type="typeValue";
    public static final int Normal_Journal_Entry=1;
    public static final int Party_Journal_Entry=2;
    public static final int FundTransfer_Journal_Entry=3;
    public static final String cashonly = "cashonly";
    
    public static final int Transaction_Commit_Limit = 100;
    public static final int Transaction_Commit_Limit_50 = 50;
    public static final String termsincludegst = "termsincludegst";
    public static final String usedTerms = "usedTerms";
    public static final String unusedTerms = "unusedTerms";
    public static final String isAddOrEdit = "isAddOrEdit";
    
    public static HashMap<String, Integer> BudgetingTypes = new HashMap<String, Integer>();
    public static HashMap<String, Integer> BudgetingFrequencyTypes = new HashMap<String, Integer>();
    public static final String BudgetForDepartment = "BudgetForDepartment";
    public static final String BudgetForDepartmentAndProduct = "BudgetForDepartmentAndProduct";
    public static final String BudgetForDepartmentAndProductCategory = "BudgetForDepartmentAndProductCategory";
    public static final String MonthlyBudgeting = "MonthlyBudgeting";
    public static final String BiMonthlyBudgeting = "BiMonthlyBudgeting";
    public static final String QuarteryBudgeting = "QuarterlyBudgeting";
    public static final String HalfYearlyBudgeting = "HalfYearlyBudgeting";
    public static final String YearlyBudgeting = "YearlyBudgeting";
    public static final String onlydateformat = "onlydateformat";
    static {
        BudgetingTypes.put(BudgetForDepartment, 0);
        BudgetingTypes.put(BudgetForDepartmentAndProduct, 1);
        BudgetingTypes.put(BudgetForDepartmentAndProductCategory, 2);
        BudgetingFrequencyTypes.put(MonthlyBudgeting, 0);
        BudgetingFrequencyTypes.put(BiMonthlyBudgeting, 1);
        BudgetingFrequencyTypes.put(QuarteryBudgeting, 2);
        BudgetingFrequencyTypes.put(HalfYearlyBudgeting, 3);
        BudgetingFrequencyTypes.put(YearlyBudgeting, 4);
    }
  public static final String Email_Button_From_Report_fieldid="20";
  public static final String APPROVAL_EMAIL="22";
  public static final String REJECTION_EMAIL="23";
  public static final String DATEFORMATINGPATTERN = "E MMM dd 12:30:00 zzz yyyy";
  public static final String DEFAULT_FORMAT_CHECK = "ddMMyy";
  public static final String DEFAULT_FORMATID_CHECK = "12";
  public static final double DAY_MILLIS = 1000.0 * 24.0 * 60.0 * 60.0;
  public static final int DebitNoteAgainstPurchaseInvoice=1;
  public static final int DebitNoteOtherwise=2;
  public static final int DebitNoteAgainstCustomer=4;
  public static final int DebitNoteAgainstCreditNote=5;
  public static final int CreditNoteAgainstSalesInvoice=1;
  public static final int CreditNoteOtherwise=2;
  public static final int CreditNoteAgainstVendor=4;
  public static final int CreditNoteAgainstDebitNote=3;
  public static final int OpeingCreditNoteforCustomers=10;
  public static final int OpeingCreditNoteforVendors=11;
    public static final int DebitNoteForOvercharge = 6;
    public static final int DebitNoteForUndercharge = 5;
    public static final int CreditNoteForOvercharge = 6;
    public static final int CreditNoteForUndercharge = 5;
  /*Document Designer*/
  public static final String Customedlineitems = "customedlineitems";
  public static final String  customizedheaderItems = "customizedheaderItems";
  public static final String lineitemHeight = "lineitemheight";
  public static final String lineitemWidth = "lineitemwidth";
  public static final String isLineItemPresent= "isLineItemPresent";
  /*Details Table constants*/
  public static final String isDetailsTablePresent = "isDetailsTablePresent";
  public static final String customizedDetailsTableCols = "customizedDetailsTableCols";
  public static final String customizedDetailsTableHeaders = "customizedDetailsTableHeaders";
  public static final String detailsTableParentRowID = "detailsTableParentRowID";
  public static final String detailsTableColumns = "detailsTableColumns";
  public static final String detailsTableSubType_id = "detailsTableSubType_id";
  public static final String detailsTableSubType_value = "detailsTableSubType_value";
  public static final String detailsTableID = "detailsTableID";
  public static final String consolidatedfield = "consolidatedfield";
  public static final String summationfields = "summationfields";
  
  public static final String isLineItemSummaryTable= "isLineItemSummaryTable";
  public static String lineitemTableParentRowID = "lineitemTableParentRowID";
  public static String LineItemSummaryTableInfo = "LineItemSummaryTableInfo";
  public static String lineItemColumns = "columndata";
  public static String lineItemFirstRowHTML = "lineItemFirstRowHTML";
  public static String lineItemLastRowHTML = "lineItemLastRowHTML";
  public static String ISFIRSTROWPRESENT = "isFirstRowPresent";
  public static String ISLASTROWPRESENT = "isLastRowPresent";
  public static String GROUPINGITEMS = "groupingItems";
  public static String ISGROUPINGROWPRESENT = "isGroupingRowPresent";
  public static String ISGROUPINGAPPLIED = "isGroupingApplied";
  public static String GROUPINGAFTERITEMS = "groupingAfterItems";
  public static String ISGROUPINGAFTERROWPRESENT = "isGroupingAfterRowPresent";
  public static String ISLINEITEMREPEAT = "islineitemrepeat";
  public static String isExtendLineItem = "isExtendLineItem";
  public static String pageSize = "pageSize";
  public static String pageOrientation = "pageOrientation";
  public static String adjustPageHeight = "adjustPageHeight";
  public static String SORTFIELD = "sortField";
  public static String SORTFIELDXTYPE = "sortFieldXtype";
  public static String SORTORDER = "sortOrder";
  public static String isAgeingTablePresent = "isAgeingTablePresent";
  public static String isChecklistTablePresent = "isChecklistTablePresent";
  public static String isGroupingSummaryTablePresent = "isGroupingSummaryTablePresent";
  /*Detail Term Map Tables: */
  public static final String DODetailTermMap = "deliveryorderdetailtermsmap";
  public static final String InvoiceDetailTermMap = "invoicedetailtermsmap";
  public static final String PODetailTermMap = "purchaseorderdetailstermmap";
  public static final String PRDetailTermMap = "purchasereturndetailtermmap";
  public static final String QuotationDetailTermMap = "quotationdetailtermmap";
  public static final String RADetailTermMap = "receiptadvancedetailstermmap";
  public static final String ReceiptDetailTermMap = "receiptdetailtermsmap";
  public static final String RODetailTermMap = "receiptorderdetailtermsmap";
  public static final String SODetailTermMap = "salesorderdetailtermmap";
  public static final String SRDetailTermMap = "salesreturndetailtermmap";
  public static final String VQDetailTermMap = "vendorquotationdetailstermmap";
  
  public static String DebitTermName = "Debit Term";
  public static String ReceiptNumber = "Receipt Number";
  public static String PaymentNumber = "Payment Number";
  public static String PaidTo = "Paid To";
  public static String ReceivedFrom = "Received From";
  public static String CreationDate = "Creation Date";
  public static String Memo = "Memo";
  public static String PaymentMethodType = "Payment Method Type";
  public static String Cheque = "Cheque";
  public static String ChequeNumber = "Cheque Number";
  public static String BankName = "Bank Name";
  public static String CheckDate = "Check Date";
  public static String Card = "Card";
  public static String CurrencyName = "Currency";
  public static String Tax_Currency_Exchange="Tax Currency Exchange ";
  public static String VendorName = "Vendor";
  public static String CustomerName = "Customer";
  public static int ChequeNoIgnore = 0;
  public static int ChequeNoBlock = 1;
  public static int ChequeNoWarn = 2;
  public static String defaultTemplateCompanyid = "1";
  public static int lineItemSelectFieldType = 0;
  public static int lineItemFormulaFieldType = 4;
  public static int lineItemPreTextType = 1;
  public static int lineItemPostTextType = 2;
  public static int Limitrecordforcsv = 500;
  public static String CreditTermName = "Credit Term";
  public static int MaxLimitOFDocumentsInReceivePayment = 70;
  public static String importproductxls = "importproductxls";
  public static String importassemblyproduct = "importassemblyproductcsv";
  public static String ApproverLevel = "Approver Level";
  public static String APPROVED_DATE_LEVEL = "Approved Date Level";
      
    // Control Accounts Purpose Constant which are used to show in Manual JE Post Settings
  public static final String Customer_Default_Account = "Customer Default Account";
  public static final String Vendor_Default_Account = "Vendor Default Account";
  public static final String Product_Purchase_Account = "Product Purchase Account";
  public static final String Product_Purchase_Return_Account = "Product Purchase Return Account";
  public static final String Product_Sales_Account = "Product Sales Account";
  public static final String Product_Sales_Return_Account = "Product Sales Return Account";
  public static final String Fixed_Asset_Depreciation_GL_Account = "Fixed Asset Depreciation GL Account";
  public static final String Depreciation_Provision_GL_Account = "Depreciation Provision GL Account";
  public static final String Asset_Sales_Account = "Asset Sales Account";
  public static final String Asset_Write_Off_Account = "Asset Write-Off Account";
  public static final String Term_Account = "Term Account";
  public static final String Tax = "Tax";
  public static final String TAXID = "taxid";
  public static final String TAXCODE = "taxcode";
  public static final String TAXNAME = "taxname";
  public static final String TAXTYPE = "taxtype";
  public static final String Tax1099_Account = "Tax1099 Account";
  public static final String Payment_Method = "Payment Method";
  public static final String Discount_Given = "Discount Given";
  public static final String Discount_Received = "Discount Received";
  public static final String Other_Charges = "Other Charges";
  public static final String Cash_Account = "Cash Account";
  public static final String Foreign_Exchange = "Foreign Exchange";
  public static final String Unrealised_Gain_Loss = "Unrealised Gain Loss";
  public static final String Depreciation_Account = "Depreciation Account";
  public static final String Salary_Expense_Account = "Salary Expense Account";
  public static final String Salary_Payable_Account = "Salary Payable Account";
  public static final String Rounding_Off_Difference = "Rounding Off Difference";
  public static final String Sales_Revenue_Recognition_Account = "Sales Revenue Recognition Account";
  public static final String BLOCKED_ITC_Account = "Blocked ITC Account";
  public static final String SALES_INVOICE_WRITEOFF_ACCOUNT = "Sales Invoice Write Off Account";
  public static final String Receipts_Write_Off_Account = "Receipts Write Off Account";
  public static final String Wastage_Default_Account = "Wastage Default Account";
  public static final String Adjustment_Account_Payment = "Adjustment Account Payment ";
  public static final String Adjustment_Account_Receipt = "Adjustment Account Receipt";
  public static final String DefaultHeader_DefaultLineItems = "Default Line Items";
  public static final String DefaultHeader_DefaultGlobalItems = "Default Global Items";
  public static final String DefaultHeader_BatchDetails = "Batch Details";
  
  public static final String Product_Wastage_Account = "Product Wastage Account";
    public static final String PRODUCT_INVENTORY_ACCOUNT = "Product Inventory Account";
    //Inventory Reports Moduleid
  public static final int Acc_Stock_Adjustment_ModuleId = 95;
  public static final int Acc_Stock_Request_ModuleId = 1001;
  public static final int Acc_InterStore_ModuleId = 1002;
  public static final int Acc_InterLocation_ModuleId = 1003;
  public static final int Acc_CycleCount_ModuleId = 1004;
  public static final int Acc_Import_Service_Invoice_Payment_ModuleId = 1005;
  public static final int Acc_Free_Gift_JE_ModuleId = 1006;
  public static final String importproductopeningqty = "importproductopeningqty";
  public static  final String importproductpricecsv="import product price";
  //Permission Related Conatants
  public static final String CUSTOMER_PERMCODE="customer";//key for customer permission code
  public static final int CUSTOMER_VIEWALL_PERMCODE=256;//View all customers record permission.
  public static final String VENDOR_PERMCODE="vendor";//key for Vendor permission code
  public static final int VENDOR_VIEWALL_PERMCODE=256;//View all Vendors records permission.
  public static final String UNITPRICE_AMOUNT_PERMCODE="unitpriceandamount";
  public static final int DISPLAY_UP_AMT_PURCHASE_DOUCUMENT_PERMCODE=1;
  public static final int DISPLAY_UP_AMT_SLAES_DOUCUMENT_PERMCODE=2;  
  
  public static String UPAndAmtDispalyValueNoPerm = "**********";
  public static String importproductcsv = "importproductcsv";
  public static String importproductcategorycsv="importproductcategorycsv";
  
  //Olympus import master
  public static String PRODUCTMASTER_Module="Product Master";
  public static String CUSTOMERBILLINGMASTER_Module="Customer Billing";
  public static String CUSTOMERSHIPPINGMASTER_Module="Customer Shipping";
  public static String LICENSEMASTER_Module="License Master";
  public static String SUPPLLICENSE1_Module="License Master Supplementary 1";
  public static String SUPPLLICENSE2_Module="License Master Supplementary 2";
  public static String STOCKMOVEMENT1_Module="Stock IN (WH-DE)";
  public static String STOCKMOVEMENT2_Module="Stock IN (WH-SS)";
  public static String STOCKMOVEMENT3_Module="Stock IN (WH-KS)";
  public static String STOCKMOVEMENT4_Module="Stock IN (WH-ES)";
  
  public static HashMap<String, Integer> Months_According_To_Indices = new HashMap<String, Integer>();
  public static final String January = "January";
  public static final String February = "February";
  public static final String March = "March";
  public static final String April = "April";
  public static final String May = "May";
  public static final String June = "June";
  public static final String July = "July";
  public static final String August = "August";
  public static final String September = "September";
  public static final String October = "October";
  public static final String November = "November";
  public static final String December = "December";
  static {
      Months_According_To_Indices.put(January, 0);
      Months_According_To_Indices.put(February, 1);
      Months_According_To_Indices.put(March, 2);
      Months_According_To_Indices.put(April, 3);
      Months_According_To_Indices.put(May, 4);
      Months_According_To_Indices.put(June, 5);
      Months_According_To_Indices.put(July, 6);
      Months_According_To_Indices.put(August, 7);
      Months_According_To_Indices.put(September, 8);
      Months_According_To_Indices.put(October, 9);
      Months_According_To_Indices.put(November, 10);
      Months_According_To_Indices.put(December, 11);
  }
  
  public static final int SOA_CUSTOMER_CURRENCY= 1;// In case of SOA customer Vendor currency
  public static final int Statement_OF_Invoice= 2; // Client Baker Tilly Statement of invoice 
  public static final int SOA_Credit_Debit_Split= 3; // Client Gplus SOA
  public static final String TEMPLATE_SUBTYPE_SOA= "0"; // Template Subtype SOA
  public static final String TEMPLATE_SUBTYPE_SOI= "1"; // Template Subtype SOI
  public static final String TEMPLATE_SUBTYPE= "templatesubtype"; // Template Subtype SOI
  
  //Multi Level approval rules applied upon constants
  public static final int All_Conditions= 0;
  public static final int Total_Amount= 1;
  public static final int Journal_Entry_Creator= 2;
  public static final int Profit_Margin_Amount= 3;
  public static final int Specific_Products= 4;
  public static final int Specific_Products_Discount= 5;
  public static final int Specific_Products_Category= 6;
  public static final int SO_CREDIT_LIMIT= 7;                   //ERM-396
  
  public static final int RECURRING_INVOICE_01_APPEND_START_FROM= 1;// when recurring invoice pattern is 0,1 appending then it should start from 1 not from 0.
  public static final String invoiceamountdueinbase = "invoiceamountdueinbase";
  public static final String discountAmount = "discountamount";
  public static final String discountAmountInBase = "discountamountinbase";
  public static final String Checklocktransactiondate = "Checklocktransactiondate";
  public static final String CASH_RECEIVED = "cashReceived";
  public static final String isSaveAsDraft = "isSaveAsDraft";
  
  //Constants for Recurring mail rules
   public static final int RECURRING_DAY= 1;
   public static final int RECURRING_WEEK= 2;
   public static final int RECURRING_MONTH= 3;
   public static final int RECURRING_NEVERENDTYPE= 1;
   public static final int RECURRING_ENDAFTERINTERVAL= 2;
   
    //Report List
    public static final int CUSTOMER_REVENUE_REPORT = 214;
    public static final int SALES_PERSON_COMMISSION_DIMENSION_REPORT = 819;
    public static final int LINK_TABLE_TD = 15;
    public static final String LINK_TABLE_TD_WIDTH = "10%";
    public static final String LINK_TABLE_TD_WIDTH_SALES = "12.5%";
    public static final String SQL_FALSE = "F";
    public static final int LINK_SOURCE_FLAG_0 = 0;
    public static final int LINK_SOURCE_FLAG_1 = 1;
    
    public static final String SQL_DATE_FORMAT = "YYYY-MM-dd HH:MM:SS";
    
    public static final String ENCODING = "UTF-8";
    public static final String SALES_PERSON_LABEL = "Sales Person";
    public static final String REFERRALKEY = "referralkey";
    public static final String DECODE_ENCODE_FORMAT = "UTF-8";
    
    //Constants for SubLedger Report.
    public static final String MAIN_GROUP_VALUE = "mainGroupValue";
    public static final String MAIN_GROUP_HEADER = "mainGroupHeader";
    public static final String SUB_GROUP_VALUE = "subGroupValue";
    public static final String SUB_GROUP_HEADER = "subGroupHeader";
    public static final String IS_SUB_LEDGER_EXPORT ="isSubLedgerExport";
    public static final String LOAD_TRANSACTION_DETAILS ="loadTransactionDetails"; // flag to skip the transaction details in GL Report
    public static final String IS_LINKED_TRANSACTION = "isLinkedTransaction";
    public static final String IS_TAXPAID_TRANSACTION = "isTaxPaidTransaction";
    public static final String HAS_ACCESS ="hasAccess"; // flag to check wheather the vendor/customer is activated or not
    public static final String IS_INVOICE_ALLOW_TO_EDIT = "isAllowToEdit"; // document allowed to edit
    public static final String IS_PYMENT_STATUS_CLEARED = "isPaymentStatusCleared"; // document allowed to edit
    //Maximum record limit for import at a time
    public static final int MAXIMUM_ALLOWED_RECORDS_FOR_IMPORT=1500;
    public static final int MAXIMUM_ALLOWED_RECORDS_FOR_IMPORT_10000=10000;
    public static final int PRICE_LIST_DISCOUNT_AT_FIXED_RATE=2;
    public static final String IMPORT_PRICE_LIST_BAND="Price List - Band";
    public static final String IMPORT_PRODUCT_PRICE="Product Price List";    
    public static final List<String> IMPORT_TIME_10000_MOD_LIST=Arrays.asList(new String[]{IMPORT_PRICE_LIST_BAND,IMPORT_PRODUCT_PRICE});
    // Default Master Item
    public static final String WASTAGE_ID = "b0385c02adf611e5bed9eca86bfcd415";
    
    // Report ID (Report List)
    public static final int CREDIT_NOTE_WITH_ACCOUNT = 822;
    public static final int Sales_By_Service_ProductDetail = 811;
    public static final int customLineDetailsReport = 832;
    public static final int customerRegistryReport=830;
    public static final int vendorRegistryReport=831;
    public static final int dayEndCollectionReport=827;
    public static final int SalesCommissionSchemaReport=1211;
    public static final int inventoryValuation=555;
    public static final int stock_Ledger=204;
    public static final int DefaultBalanceSheetReportId=845;
    public static final int dimensionBasedBalanceSheet=838;
    public static final int dimensionBasedProfitLoss=837;
    public static final int dimensionBasedTrialBalance=839;
    public static final int ActualVsBudgetReportNo=1334;
    public static final int GOODS_PENDING_ORDERS=1337;
    public static final int Store_Transfer_History=237;
    public static final int FULLFILL_ORDERS_REGISTER=238;
    
    // CIMB Bank Related Constans
    
    public static final String SERVICE_CODE = "serviceCode";
    public static final String BANK_Account_Number = "bankAccountNumber";
    public static final String ORDERER_NAME = "ordererName";
    public static final String SETTELEMENT_MODE = "settlementMode";
    public static final String POSTING_INDICATOR = "postingIndicator";
    public static final String SGD_CURRENCY_CODE = "SGD";
    public static final String MYR_CURRENCY_CODE = "MYR";
    public static final String DBS_BANK_NAME = "Development Bank Of Singapore";
    public static final String CIBM_BANK_NAME = "Commerce International Merchant Bankers";
    public static final String Settlement_Mode_Batch_Name = "Batch";
    public static final String Settlement_Mode_Real_Time_Name = "Real Time";
    public static final String Posting_Indicator_Consolidated_Name = "Consolidated";
    public static final String Posting_Indicator_Individual_Name = "Individual";
    public static final int DBS_BANK_Type = 1;
    public static final int CIMB_BANK_Type = 2;
    public static final int Settlement_Mode_Batch = 1;
    public static final int Settlement_Mode_Real_Time = 2;
    public static final int Posting_Indicator_Consolidated = 1;
    public static final int Posting_Indicator_Individual = 2;
    public static final String GOODS_RECEIPT_ORDER = "Goods Receipt Order";
    public static final String SALES_RETURN = "Sales Return";
    
    // UOB bank related constants
    
    public static final String UOB_Originating_BIC_Code = "uobOriginatingBICCode";
    public static final String UOB_Currency_Code = "uobCurrencyCode";
    public static final String UOB_Originating_Account_Number = "uobOriginatingAccountNumber";
    public static final String UOB_Originating_Account_Name = "uobOriginatingAccountName";
    public static final String UOB_Ultimate_Originating_Customer = "uobUltimateOriginatingCustomer";
    public static final String UOB_CompanyID = "uobCompanyId";
    
    // OCBC bank related constants
    public static final int OCBC_BankType = 4;
    public static final String OCBC_FullForm = "Oversea-Chinese Banking Corporation";
    public static final String OCBC_OriginatingBankCode = "ocbcOriginatingBankCode";
    public static final String OCBC_AccountNumber = "ocbcAccountNumber";
    public static final String OCBC_ReferenceNumber = "ocbcReferenceNumber";
    
    public static final String OCBC_IBGDetailId = "ocbcIBGDetailId";
    public static final String OCBC_BankCode = "ocbcBankCode";
    public static final String OCBC_VendorAccountNumber = "ocbcVendorAccountNumber";
    public static final String OCBC_UltimateCreditorName = "ocbcUltimateCreditorName";
    public static final String OCBC_UltimateDebtorName = "ocbcUltimateDebtorName";
    public static final String OCBC_SendRemittanceAdviceVia = "ocbcSendRemittanceAdviceVia";
    public static final String OCBC_RemittanceAdviceSendDetails = "ocbcRemittanceAdviceSendDetails";
    
    public static final int OCBC_TransactionTypeCode = 10;
    //transaction type of Work Order IN
    public static final int WORK_ORDER_TRANSACTION_TYPE_IN = 28;
    
    // GST FOrm 3 Related Constants
    public static final String taxkeyJasper = "taxkeyJasper";
    
    //Account name For the Difference in Opening Balances Account
    public static final String Difference_in_Opening_balances = "Difference in Opening balances"; 
    
    public static Set<Integer> moduleSetForAgedReceivable = new HashSet();

    static {
        moduleSetForAgedReceivable.add(Constants.Acc_Receive_Payment_ModuleId);
        moduleSetForAgedReceivable.add(Constants.Acc_Invoice_ModuleId);
        moduleSetForAgedReceivable.add(Constants.Acc_Credit_Note_ModuleId);
        moduleSetForAgedReceivable.add(Constants.Acc_Debit_Note_ModuleId);
        moduleSetForAgedReceivable.add(Constants.Acc_Receive_Payment_ModuleId);
    }
    
    public static final int Invoice_Claimed = 1;
    public static final int Invoice_Recovered = 2;
    public static final int BadDebtMappingForClaim=0;
    public static final int BadDebtMappingForRecover=1;
    public static String COUNTRY_NAME="countryname";
    public static String COUNTRY_ID="countryid";
    public static String STATE_ID="stateid";
    
    public static final String GST_SUBMISSIONFILE_STORAGE_PATH = "gstsubmissionfile";
    public static final int GST_Monthly_Submission = 0;
    public static final int GST_Quarterly_Submission = 1;
    public static final int Link_PR_TO_PO=11;
    
    
    public static final String NATURE_OF_PAYMENT_194C="7";
    public static final String DEDUCTEE_TYPE_INDIVIDUAL="dfc28c54-015c-11e6-ba66-14dda9792823";
    public static final String DEDUCTEE_TYPE_HUF="ed34cb54-015c-11e6-ba66-14dda9792823";
    
    // Country Language Id's

    public static final int OtherCountryLanguageId = 0;
    public static final int CountryIndiaLanguageId = 1;
    
    // Currency Id
    public static final int CountryUSCurrencyId = 1;
    public static final int CountryIndiaCurrencyId = 5;
    public static final String CountryIndonesianCurrencyId = "13";
    
 //TDS Default Chart of Account Arralist for INDIA Country
    public static final String UnkownDeducteeTypeReportID = "7";
    public static final String PANNotAvailableReportID = "8";
    public static final String NatureOfPaymentWiseReportID = "9";
    public static final String OtherTermNonTaxableAmount = "OtherTermNonTaxableAmount";
    public static List<String> TDSDefaultChartOfAccountsINDIA = new ArrayList<String>();

    static {
        TDSDefaultChartOfAccountsINDIA.add("TDS Payable192");
        TDSDefaultChartOfAccountsINDIA.add("TDS Payable194A");
        TDSDefaultChartOfAccountsINDIA.add("TDS Payable194C");
        TDSDefaultChartOfAccountsINDIA.add("TDS Payable194I");
        TDSDefaultChartOfAccountsINDIA.add("TDS Payable194J");
        TDSDefaultChartOfAccountsINDIA.add("Excise Duty");
        TDSDefaultChartOfAccountsINDIA.add("Service Tax");
        TDSDefaultChartOfAccountsINDIA.add("Swachh Bharat Cess");
        TDSDefaultChartOfAccountsINDIA.add("Krishi Kalyan Cess");
}
    //Default Nature of Payment Group
    public static final String NatureofPaymentGroup = "33";
    public static final String QUENTITY = "quantity";
    public static final String MRP = "mrp";
    public static Map<String, String> connectionThreads = new HashMap<String, String>();
    public static Timer conectionTimer = new Timer();
    public static TimerTask connectionTimerTask;
    
    public static final double DmR_totalESPercentSupplies_Limit = 5000;
    public static final double DmR_figureInPercentage_Limit = 5;
    
    //Rest service contstants
    public static final String RES_ERROR_CODE = "errorcode";
    public static final String RES_MESSAGE = "message";
    public static final String RES_DEF_LANGUAGE="en-US";
    public static final String RES_REQUEST="request";
    public static final String RES_CDOMAIN="cdomain";
    public static final String RES_DATEFORMAT = "dateformat";
    public static final String RES_TOTALCOUNT = "totalCount";
    public static final String PAGED_JSON = "pagedJSON";
    public static final String RES_METADATA = "metadata";
    
    public static final String isdefaultHeaderMap = "isdefaultHeaderMap"; // Flag used to get and save data depending on dataindex saved in default_header table. 
                                                                          //  E.g For mobile application, handle get and set data according to dataindex set in default_header table;
    public static final String lineItemDetails = "lineItemDetails";
    public static final String getlineItemDetailsflag = "getlineItemDetailsflag";
    public static final String globalFields = "globalFields";
    public static final String lineItemFields = "lineItemFields";
    public static final String batchSerialFields = "batchSerialFields";
    public static final String moduleIds = "moduleIds";
    
     //StringUtils Constants
    public static final String language = "language";
    public static final String crmURL = "crmURL";
    public static final String inventoryURL = "inventoryURL";
    public static final String posURL = "posURL";
    public static final String initialized = "initialized";
    public static final String companyname = "company";
    public static final String dateformatid = "dateformatid";
    public static final String browsertz = "browsertz";
    public static final String lid = "lid";
    public static final String timezoneID = "timezoneID";
    public static final String timezonedifference = "timezonedifference";
    public static final String timezoneid = "timezoneid";
    public static final String callwith = "callwith";
    public static final String timeformat = "timeformat";
    public static final String companyPreferences = "companyPreferences";
    public static final String username = "username";
    public static final String usermailId = "usermailId";
    public static final String userdateformat = "userdateformat";
    public static final String roleid = "roleid";
    public static final String reqHeader = "reqHeader";
    public static final String remoteIPAddress = "remoteAddress";
    public static final String realip = "x-real-ip";
    public static final String defaultIp = "0:0:0:0:0:0:0:1";
    public static final String REQUEST_URI="requestURI"; 
    public static final String PAGE_URL="pageURL";
    public static final int SalesCommission_InvoiceNetAmountExcludingTaxMode = 2;
    public static final String SERVLET_PATH="sevletPath";
    public static final String REAL_PATH="realPath";
            
    public static final String channelName = "channelName";
    public static final String currencyName = "currencyName";
    public static final String currencyNameValue = "currencyNameValue";
    public static final String customerName = "customerName";
    public static final String customerNameValue = "customerNameValue";
    public static final String sequenceformat = "sequenceformat";
    public static final String quantitydecimalforcompany = "quantitydecimalforcompany";
    public static final String unitpricedecimalforcompany = "unitpricedecimalforcompany";
    public static final String gstAmountDigitAfterDecimal = "gstamountdigitafterdecimal";
    public static final String amountdecimalforcompany = "amountdecimalforcompany";
    public static final String sequenceformatValue = "sequenceformatValue";
    public static final String detail = "detail";
    public static final String EXPENSE_DETAIL = "expensedetail";
    public static final String userfullname = "userfullname";
    public static final String salesperson = "salesperson";
    public static final String salespersonValue = "salespersonValue";
    public static final String prtaxidValue = "prtaxidValue";
    public static final String taxidValue = "taxidValue";
    public static final String costcenter = "costcenter";
    public static final String costcenterValue = "costcenterValue";
    public static final String billno = "billno";
    public static final String memo = "memo";
    public static final String posttext = "posttext";
    public static final String shipdate = "shipdate";
    public static final String duedate = "duedate";
    public static final String shipvia = "shipvia";
    public static final String fob = "fob";
    public static final String billid = "billid";
    public static final String islineitem = "islineitem";
    public static final String billTo = "billTo";
    public static final String shipTo = "shipTo";
    public static final String autocashsales = "autocashsales";
    public static final String autoinvoice = "autoinvoice";
    public static final String iscustomflag = "iscustomflag";
    public static final String isdimension = "isdimension";
    public static final String defaultId = "defaultId";
    public static final String isForReport = "isForReport";
    public static final String report = "report";
    public static final String dtype = "dtype";
    public static final String isFixedAsset = "isFixedAsset";
    public static final String deletepermanentflag = "deletepermanentflag";
    public static final String linkedTransaction = "linkedTransaction";
    public static final String isreadonly = "isreadonly";
    public static final String isDraft = "isDraft";
    public static final String discountTypeFieldid = "7b2882e0-f843-b318-14dd920de";
    public static final String includeProductTaxFieldid = "7b2882e0-f843-b318-14dd92927de";
    public static final String companyids = "companyids";
    public static final String xls ="xls";
    public static final String detailedXls ="detailedXls";
    public static final String externalcurrencyrate ="externalcurrencyrate";
    public static final String submoduleflag ="submoduleflag";
    public static final String Mode_SalesOrder ="autoso";
    public static final String Mode_SalesInvoice ="autoinvoice";
    public static final String Mode_Customer ="autocustomerid";
    public static final String Mode_CreditNote ="autocreditmemo";
    public static final String Mode_SalesReturn ="autosr";
    public static final String Mode_ReceivePayment ="autoreceipt";
    public static final String Mode_MakePayment ="autopayment";
    public static final String Mode_CashSales ="autocashsales";
    public static final String Mode_VendorQuotation ="autovenquotation";
    public static final String Mode_CustomerQuotation ="autoquotation";
    public static final String Mode_DeliveryOrder ="autodo";
    public static final String Mode_GoodsReceiptOrder ="autogro";
    public static final String Mode_Product ="autoproductid";
    public static final String PermCode_Customer ="permcodecustomer";
    public static final String imageTag ="imageTag";
    public static final String DetailView ="detailView";
    public static final String SummaryView ="summaryView";
    public static final String AddEditView ="addEditView";
    public static final String locale ="locale";
    public static final String isdefault ="isdefault";
    public static final String deleted ="deleted";
    public static final String type ="type";
    public static final String hqlquery ="hqlquery";
    public static final String url ="url";

    // Master Group ID
    public static final String CUSTOMER_CATEGORY_ID = "7";
    public static final String PRODUCT_BRAND_ID = "53";
    public static final String AGENT_ID = "20";
    public static final String DBS_Bank_Module_Name = "DBS Receiving Bank Details";
    public static final String MASTERCONFIG_QUALITY_PARAMETER = "55";
    public static final String COST_OF_MANUFACTURING_ID = "64";
    
    public static final String Acc_parentid = "parentid";
    public static final String Acc_parentname= "fieldid";
    public static final String Acc_level = "level";
    public static final String Acc_leaf = "leaf";
    public static final String DELIVERY_ORDER_STATUS = "Delivery Order Status";
    public static final String QUALITY_GROUP = "Quality Group";
    public static final String QUALITY_PARAMETER = "Quality Parameter";
    public static final String WORK_CENTRE_MANAGER = "Work Centre Manager";
    public static final String WORK_CENTRE_LOCATION = "Work Centre Location";
    public static final String WORK_CENTRE_TYPE = "Work Centre Type";
    public static final String WORK_TYPE= "Work Type";
    public static final String GOODS_RECEIPT_ORDER_STATUS = "Goods Receipt Status";
    public static final String DRIVER = "Driver";
    public static final String VEHICLE_NUMBER = "Vehicle Number";
   
    public static final int PERIODIC_VALUATION_METHOD = 0;
    public static final int PERPETUAL_VALUATION_METHOD = 1;
    public static final String COMPANY_PARAM = "cdomain";
    public static final String COMPANY_SUBDOMAIN = "subdomain";
    
    // Product Type Constants
    public static final String ASSEMBLY = "e4611696-515c-102d-8de6-001cc0794cfa";
    public static final String INVENTORY_PART = "d8a50d12-515c-102d-8de6-001cc0794cfa";
    public static final String NON_INVENTORY_PART = "f071cf84-515c-102d-8de6-001cc0794cfa";
    public static final String SERVICE = "4efb0286-5627-102d-8de6-001cc0794cfa";
    public static final String Inventory_Non_Sales = "ff8080812f5c78bb012f5cfe7edb000c9cfa";
    
    public static final int INPUT_CREDIT_SUMMARY_REPORT = 0;
    public static final int BILL_DATE_WISE_REPORT = 1;
    public static final int REALISATION_DATE_WISE_REPORT = 2;
    public static final int TAX_SERVICE_RECEIVED_REPORT = 3;
    
    public static final int BANKBOOK_VIEW_MAKE_PAYMENT = 1;
    public static final int BANKBOOK_VIEW_RECEIVE_PAYMENT = 2;
    
    public static final String ROUTING_TEMPLATE_ADDED = " added new Routing Template(s) ";
    public static final String ROUTING_TEMPLATE_UPDATED = " updated  Routing Template(s) ";
    public static final String ROUTING_TEMPLATE_DELETED = " deleted  Routing Template(s) ";
    
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String REST_AUTH_TOKEN = "token";

    public static final double ROUND_OFF_NUMBER = 10000000000000000d;
    public static final String REMOTE_API_KEY="remoteapikey";
    public static final String CLIENT_ID = "clientid";
    public static final String CLIENT_SECRET = "clientsecret";
    public static final String PLATFORM_URL = "platformURL";
    
    //Asset Module
    public static final int DEPRECIATION_BASED_ON_FIRST_FINANCIAL_YEAR_DATE = 0;
    public static final int DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE = 1;
    public static final int ASSET_SOLD_FROM_CI = 1;
    public static final int ASSET_SOLD_FROM_DO = 2;
    public static final int DEPRECIATION_LIMIT = 500;
    public static final int BATCH_LIMIT = 50;
    public static final String END_OF_DEPRECIATION_QUEUE = "END_OF_DEPRECIATION_QUEUE";
    
    //Asset Module Report List
    public static final int ACC_FIXED_ASSET_DETAILS_REPORTID = 225;
    public static final int ACC_FIXED_DISPOSED_ASSET_REPORTID = 1165;
    public static final int ACC_FIXED_DEPRECIATION_DETAILS_REPORTID = 802;
    public static final int ACC_FIXED_ASSET_SUMMARY_REPORTID = 1141;
    public static final int ACC_FIXED_ASSET_GENERATE_DEPRECIATION = 239;
    
    // SOA - Report IDs
    public static final int SOA_CUSTOMER_ACCOUNT_STATEMENT_REPORTID = 1227;
    public static final int SOA_VENDOR_ACCOUNT_STATEMENT_REPORTID = 1228;
    
    public static final String INVENTORYJE_SEQ_FORMAT_ID = "inventoryJESeqFormatID";
    public static final String INVENTORYJE_SEQ_NUMBER = "inventoryJESeqNumber";
    public static final String INVENTORYJE_IS_AUTO_GENERATED = "inventoryJEIsAutoGenerated";
    public static final String INVENTORYJE_ENTRYNO = "inventoryJENumber";
    public static final String INVENTORYJE_DATE_PREFIX_VALUE = "inventoryJEDatePrefixValue";
    public static final String INVENTORYJE_DATE_AFTER_PREFIX_VALUE = "inventoryJEDateAfterPrefixValue";
    public static final String INVENTORYJE_DATE_SUFFIX_VALUE = "inventoryJEDateSuffixValue";
    public static final String INVENTORYJE_ID = "inventoryJEId";
    public static final String GLOBAL_PARAMS = "globalParams";
    
    public static final String JRXML_REAL_PATH_KEY = "JRXML_PATH";
    public static final String Opening_Stock_account = "Opening Stock";
    public static final String Cost_of_Goods_Sold_account = "Cost of Goods Sold";
    public static final String Total_for_Cost_of_Goods_Sold_account="Total for Cost of Goods Sold";
    public static final String Closing_Stock_account="Closing Stock";
    public static final String Finish_Products_account="Finish Products (Total Value of \"Inventory Assembly\" products)";
    public static final String Raw_Materials_account="Raw Materials (Total Value of \"Inventory Item\" products)";
    public static final String Total_Closing_Stock_account="Total Closing Stock";
    
    public static final int ADMIN_USER_ROLEID=1;
    public static final String REST_VERSION="RestVersion";
    
    /*Dealer Types for Exice invoice of Indian Company*/
    public static final String First_Stage_Dealer="First Stage Dealer";
    public static final String Second_Stage_Dealer="Second Stage Dealer";
    public static final String Second_Stage_and_Subsequent_Dealer="Second Stage and Subsequent Dealer";
    public static final String Manufacturer_Importer="Manufacturer / Importer";
    public static final String Manufacturer="Manufacturer";
    public static final String From_Agent_of_Manufacturer="From Agent of Manufacturer";
    public static final String Manufacturer_Depot="Manufacturer Depot";
    
    public static final int CUSTOM_LAYOUT_NET_PROFIT_LOSS = 9;
    public static final int CUSTOM_LAYOUT_DEFINE_TOTAL = 5;
    public static final int CUSTOM_LAYOUT_DIFF_OPENING_BALANCE = 8;
    public static final int CUSTOM_LAYOUT_OPENING_STOCK = 6;
    public static final int CUSTOM_LAYOUT_CLOSING_STOCK = 7;
    public static final String IS_PRICE_LIST_BAND_REPORT = "isPriceListBandReport";
    public static final String IS_GROUP_DETAIL_REPORT = "isGroupDetailReport";
    public static final String RES_CREATE_NEW = "createnew";
    public static final int GROUP_DETAIL_REPORT_ID = 1153;
    
    /*Mobile Fields Batch Serials Config*/

    public static final String warehouseTypeFieldid = "warehouseTypeFieldid";
    public static final String locationsTypeFieldid = "locationsTypeFieldid";
    public static final String batchTypeFieldid = "batchTypeFieldid";
    public static final String serialTypeFieldid = "serialTypeFieldid";
    public static final String rackTypeFieldid = "rackTypeFieldid";
    public static final String binTypeFieldid = "binTypeFieldid";
    public static final String rowTypeFieldid = "rowTypeFieldid";
    public static final String availablequantityFieldid = "availablequantityFieldid";
    public static final String quantityFieldid = "quantityFieldid";
    public static final String stockfromFieldid = "stockfromFieldid";
    public static final String expDateFieldid = "expDateFieldid";
    public static final String mfgDateFieldid = "mfgDateFieldid";
    public static final String warrantyValidFromFieldid = "warrantyValidFromFieldid";
    public static final String warrantyExpiresOnFieldid = "warrantyExpiresOnFieldid";
    
    public static Map<String, HashMap> CompanyPreferencePrecisionMap = new HashMap<>();
    public static HashMap<String, String> InventorybatchSerialfieldids = new HashMap<String, String>();

    static {
        InventorybatchSerialfieldids.put(warehouseTypeFieldid, "b533175a-95dc-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(locationsTypeFieldid, "b53319a8-95dc-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(batchTypeFieldid, "b5331aa2-95dc-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(serialTypeFieldid, "b5331b7e-95dc-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(rackTypeFieldid, "b5331c46-95dc-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(binTypeFieldid, "b5331d0e-95dc-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(rowTypeFieldid, "b5331dd6-95dc-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(availablequantityFieldid, "d4b5b376-95dc-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(quantityFieldid, "d4b5b5a6-95dc-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(stockfromFieldid, "d4b5b6b4-95dc-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(expDateFieldid, "25f5b566-95e6-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(mfgDateFieldid, "25f5ba02-95e6-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(warrantyValidFromFieldid, "25f5bb38-95e6-11e6-ae22-56b6b6499611");
        InventorybatchSerialfieldids.put(warrantyExpiresOnFieldid, "25f5bc28-95e6-11e6-ae22-56b6b6499611");
    }
    
    public static Set<Integer> ModulesSetForAccountReceivable = new HashSet();

    static {
        ModulesSetForAccountReceivable.add(Acc_Sales_Order_ModuleId);
        ModulesSetForAccountReceivable.add(Acc_Customer_Quotation_ModuleId);
        ModulesSetForAccountReceivable.add(Acc_Sales_Return_ModuleId);
        ModulesSetForAccountReceivable.add(Acc_Invoice_ModuleId);
        ModulesSetForAccountReceivable.add(Acc_Cash_Sales_ModuleId);
    }
        
    public static Set<String> EnableDisableUnitPriceFields = new HashSet();
    static {
        //SalesReturn= These are the fields which are enabled when unit price option is checked on for SR
        EnableDisableUnitPriceFields.add("d6458c2c-4823-11e6-b3d7-c03fd5aad962");//TaxAmount of Sales Return
        EnableDisableUnitPriceFields.add("c5e9ce02-48cc-11e6-be69-c03fd5aad962");//Unit Price
        EnableDisableUnitPriceFields.add("41048380-48d6-11e6-be69-c03fd5aad962");//Discount
        EnableDisableUnitPriceFields.add("e7b843fa-07a9-44d1-afa4-15ced2760286");//Amount
        EnableDisableUnitPriceFields.add("ae082e7c-482d-11e6-b3d7-c03fd5aad962");//Product Tax
        EnableDisableUnitPriceFields.add("57b1e1ea-a499-11e5-bf7f-feff819cdc9f");//Total Amount in base currency
        EnableDisableUnitPriceFields.add("b68b484c-482b-11e6-b3d7-c03fd5aad962");//Tax Name
        EnableDisableUnitPriceFields.add("113821ae-4988-11e6-bd05-c03fd5aad962");//Tax Amount from custom reportmeasure field
        EnableDisableUnitPriceFields.add("d87d6d92-49be-11e6-b051-c03fd5aad962");//Total Amount
    }
    
    public static final int DimensionBasedProfitLossReport = 772;
    public static final int DimensionBasedBalanceSheetReport = 773;
    public static final int DimensionBasedTrialBalanceReport = 775;
    public static final int profitAndLossMonthlyCustomLayout = 68;
    public static final int balanceSheetMonthlyCustomLayout = 69;
    public static final int dimensionBasedMonthlyPLCustomLayout = 70;
    public static final int GstTapReturnDetailedView = 27;
    public static final int GstReport = 916;
    public static final String BUILD_ASSEMBLY_QA_APPROVAL = "BuildAssemblyQA";
    public static final String amountForExcelFile = "amountForExcelFile";
    public static final String unitpriceForExcelFile = "orderrate";
    public static final int AgedPayableBaseCurrency = 2;
    public static final int AgedPayableOtherthanBaseCurrency = 3;
    public static final double FreeGiftDOMaxLimit = 500;
    public static final double FreeGiftPercentageFigure = 6;
    public static final String isforformulabuilder = "isforformulabuilder";
    public static final int TEXTFIELD = 1;
    public static final int NUMBERFIELD = 2;
    public static final int DATEFIELD = 3;
    public static final int SINGLESELECTCOMBO = 4;
    public static final int TIMEFIELD = 5;
    public static final int MULTISELECTCOMBO = 7;
    public static final int REFERENCECOMBO = 8;
    public static final int AUTONUMBER = 9;
    public static final int CHECKBOX = 11;
    public static final int FIELDSET = 12;
    public static final int TEXTAREA = 13;
    public static final int RICHTEXTAREA = 15;
    
    public static final String CHECKLIST_DOCUMENT_REVALUATION_COMPLETED = "documentRevaluationCompleted";
    public static final String CHECKLIST_ADJUSTMENT_FOR_TRANSACTIONS_COMPLETED = "adjustmentForTransactionCompleted";
    public static final String CHECKLIST_INVENTORY_ADJUSTMENT_COMPLETED = "inventoryAdjustmentCompleted";
    public static final String CHECKLIST_ASSET_DEPRECIATION_COMPLETED = "assetDepreciationPosted";
    public static final String SALES_TAX_PAYABLE = "Sales Tax Payable";
    public static final String INVENTORY_VALUATION_DELIMETER = "|";
    public static boolean isexportledgerflag=true;
    public static boolean FROM_EXPANDER=false;
    
    public static final int UOB_Bank = 3;
    public static final String UOB_FullForm = "United Overseas Bank";
    public static final String Software_Label_For_UOB_GIRO = "Deskera";
    public static final String File_Name_For_UOB_GIRO = "UGBI";
    public static final int UOB_ServiceType_EXPRESS = 1;
    public static final int UOB_ServiceType_NORMAL = 2;
    public static final int UOB_ProcessingMode_Immediate = 2;
    public static final int UOB_ProcessingMode_Batch = 3;
    public static final int UOB_HashIndex_OriginatingBICCode = 11;
    public static final int UOB_HashIndex_OriginatingAccountNo = 34;
    public static final int UOB_HashIndex_OriginatingAccountName = 140;
    
    public static final int UOB_HashIndex_ReceivingBICCode = 11;
    public static final int UOB_HashIndex_ReceivingAccountNo = 34;
    public static final int UOB_HashIndex_ReceivingAccountName = 140;
    public static final int UOB_HashIndex_ReceivingCurrency = 3;
    public static final int UOB_HashIndex_ReceivingAmount = 18;
    public static final int UOB_HashIndex_ReceivingPurposeCode = 4;
    
    public static final int UOB_PaymentCode_Collection = 30;
    public static final int UOB_PaymentCode_Payment = 20;
    
    public static final String GIRO_FILE_STORAGE_PATH = "girofile";
    public static final  String MRPCOSTTYPE_REVENUE="Revenue";
    public static final  String MRPCOSTTYPE_LAOBUR="Labour";
    public static final  String MRPCOSTTYPE_MACHINE="Machine";
    public static final  String MRPCOSTTYPE_MATERIAL="Material";
    public static final  String MRPCOSTTYPE_TOTAL="Total Cost";
    public static final  String MRPCOSTTYPE_PROFIT="Profit";
    public static final  String MRPCOSTTYPE_LOSS="Loss";
    
    
    public static final  int MRPCOSTTYPENUMERIC_ALL=0;
    public static final  int MRPCOSTTYPENUMERIC_LABOUR=1;
    public static final  int MRPCOSTTYPENUMERIC_MACHINE=2;
    public static final  int MRPCOSTTYPENUMERIC_MATERIAL=3;
    
    public static final  int MRP_WASTEMOVEMENTFLAG_STOCKTRANSFER=1;
    public static final  int MRP_WASTEMOVEMENTFLAG_STOCKOUT=2;
    public static final  int MRP_RECYCLEMOVEMENTFLAG_STOCKTRANSFER=1;
    public static final  int MRP_RETURNMOVEMENTFLAG_STOCKTRANSFER=1;
    
    /** MRP : STOCK_MANAGEMENT_FLAG.
     * NON MRP ACTIVATED COMPANY : DEFAULT_STOCK_MANAGEMENT_FLAG 
     * MRP ACTIVATED COMPANY :
                       * CONSUME / PRODUCE MANAGEMENT : MRP_CONSUME_PRODUCE_STOCK_MANAGEMENT_FLAG
                       * WASTE MANAGEMENT : MRP_WASTE_STOCK_MANAGEMENT_FLAG
                       * RECYCLE MANAGEMENT : MRP_RECYCLE_STOCK_MANAGEMENT_FLAG
                       * RETURN MANAGEMENT : MRP_RETURN_STOCK_MANAGEMENT_FLAG
     * This FLAG Used to differentiating or distinguishing between Above FOUR MANAGEMENTS.                  
     */
    public static final  int DEFAULT_STOCK_MANAGEMENT_FLAG=0;
    public static final  int MRP_CONSUME_PRODUCE_STOCK_MANAGEMENT_FLAG=1;
    public static final  int MRP_WASTE_STOCK_MANAGEMENT_FLAG=2;
    public static final  int MRP_RECYCLE_STOCK_MANAGEMENT_FLAG=3;
    public static final  int MRP_RETURN_STOCK_MANAGEMENT_FLAG=4;
    
    
    /*MRP : BOM Type(Component Type)
     * O:NA
     * 1:Component
     * 2:Co-Product
     * 3:Scrap
     */
    public static final  int MRP_DEFAULT_TYPE_NO=0;
    public static final  int MRP_COMPONENT_TYPE_NO=1;
    public static final  int MRP_COPRODUCT_TYPE_NO=2;
    public static final  int MRP_SCRAP_TYPE_NO=3;
    
    public static final  String MRP_DEFAULT_TYPE_NAME="NA";
    public static final  String MRP_COMPONENT_TYPE_NAME="Component";
    public static final  String MRP_COPRODUCT_TYPE_NAME="Co-Product";
    public static final  String MRP_SCRAP_TYPE_NAME="Scrap";
    
    public static final  String MRP_RESOURCETYPE_LABOUR="4";
    public static final  String MRP_RESOURCETYPE_MACHINE="2";
    public static final String contextPath = "contextPath";
    public static final String moduleArray = "moduleArray";
    public static final String permCode = "permCode";
    public static final String documentDesignerprintTemplateUrl = "transaction/printtemplate?request=";
    
    public static final int Customer_Bank_Account_Type_GroupID = 61;
    
    public static final String COMPANYID_HINSINTSU ="820578cb-2cd1-4580-b501-0db06a72f64d";        //"25779641-c639-40ea-a6b5-c2fca0e4432d";    //Sub-domain = hinsitsu
    public static final  String COMPANY_REPORT_CONFIG_GL="GENERAL_LEDGER";
    public static final  String COMPANY_REPORT_CONFIG_SOA="SOA";
    public static final  String COMPANY_REPORT_CONFIG_AR="AR";
    public static final  String INVENTORY_CONSIGNMENT_MODULE="consignment";
    public static final  String INVENTORY_BUILD_ASSEMBLY_MODULE="buildassembly";
    public static final  String INVENTORY_SA_DETAIL_MODULE="saapproval";
    public static final  String INVENTORY_STORE_STOCK_TRANSFER_MODULE="stocktransferrequest";
    public static final  String INVENTORY_INTER_STOCK_TRANSFER_MODULE="interstocktransfer";
    
    
    
    public static final String MATERIAL_IN_OUT_REPORT_BATCH_NAME = "batchName";
    public static final String MATERIAL_IN_OUT_REPORT_LOCATION_NAME = "locationName";
    public static final String MATERIAL_IN_OUT_REPORT_ROW_NAME = "rowName";
    public static final String MATERIAL_IN_OUT_REPORT_RACK_NAME = "rackName";
    public static final String MATERIAL_IN_OUT_REPORT_BIN_NAME = "binName";
    public static final String MATERIAL_IN_OUT_REPORT_SERIAL_NAME = "serialNames";
    public static final String MATERIAL_IN_OUT_REPORT_QUANTITY = "quantity";
    public static final String MASTER_TYPE_GENERAL_LEDGER = "General Ledger";
    public static final String MASTER_TYPE_CASH = "Cash";
    public static final String MASTER_TYPE_BANK = "Bank";
    public static final String MASTER_TYPE_GST = "GST";
    public static final String MASTER_TYPE_DUTIES_AND_TAXES = "Duties & Taxes";
    
    public static final String PIE_CHART = "pie";
    public static final String BAR_CHART = "bar";
    public static final String LINE_CHART = "line";
    public static final String CHART_TYPE = "chartType";
    public static final int MAX_LIMIT_FOR_PIE = 10;
    
    public static final int CONSIGNMENT_SALES_ORDER_TYPE = 3;
    public static final  String isFromEclaim="isFromEclaim"; 

    public static final  String Accrued_Balance="Accrued Balance";
    
    //Constants for record generated from Application
    public static final String RECORD_Mobile_Application = "1";
    public static final String RECORD_WEB_Application = "0";
    public static final String RECORD_POS_Application = "2";
    public static final String generatedSource = "generatedSource";
     //constants used for importing make payment
    public static final String ADVANCE_REFUND = "1";
    public static final String INVOICEIMPORT = "2";
    public static final String CREDITNOTEIMPORT = "3";
    public static final String GLIMPORT = "4";

    public static final String packaging = "packaging";
    public static final int PRODUCT_SEARCH_STARTSWITH = 0;
    public static final int PRODUCT_SEARCH_ANYWHERE = 1;
    public static final String PRODUCT_SEARCH_FLAG = "productsearchingflag";

    
    public static List<String> GET_URL_SKIP_AUTH_LIST = new ArrayList<String>();
    static {
        GET_URL_SKIP_AUTH_LIST.add("/company/url");
    }
    public static final String pmtmethodaccountid ="pmtmethodaccountid";
    public static final String isSquatTransaction ="isSquatTransaction";
    public static final String companyImagePath ="companyImagePath";
    public static final String storeid ="storeid";
    public static final String requestMap ="requestMap";
    
    public static final String RECEIVE_PAYMENT = "Receive Payment";
    public static final String MAKE_PAYMENT = "Make Payment";
    public static final String DISHONOURED_MAKE_PAYMENT = "Dishonoured Make Payment";
    public static final String DISHONOURED_RECEIVE_PAYMENT = "Dishonoured Receive Payment";
    
    public static final String CHECKMARK_IMAGE = "checkmark.png";
    
    //Reconciliation History Details Classfication of Cleared Deposits, Checks & Un-Cleared Deposits & Checks.
    public static final int CLEARED_CHECKS = 1;     //Seleced Checks from Bank Reconciliation / View Reconciliation Report
    public static final int CLEARED_DEPOSITS = 2;   //Seleced Deposits from Bank Reconciliation / View Reconciliation Report
    public static final int UNCLEARED_CHECKS = 3;   //Non-Seleced Checks from Bank Reconciliation / View Reconciliation Report
    public static final int UNCLEARED_DEPOSITS = 4; //Non-Seleced Deposits from Bank Reconciliation / View Reconciliation Report
    public static final String BANK_RECONCILIATION_REPORT = "Bank Reconciliation Report"; 
    public static final String VIEW_RECONCILIATION_REPORT = "View Reconciliation Report"; 
        
    public static final String CREATE_IST_FOR_QC_DELIVERYORDER = "createISTForQCDeliveryOrder";
    public static final String CREATE_IST_FOR_QC_WORKORDER = "createISTForQCWorkOrder";
    
    public static final String CREATE_IST_FOR_REJECT_DELIVERYORDER_FROM_QC= "rejectDeliveryOrderFromQC";
    public static final String CREATE_PICK_IST_FOR_APPROVE_DELIVERYORDER_FROM_REPAIR_STORE= "pickISTForDeliveryOrderFromRepairStore";
    //Constants for MasterGroup
    public static final String MASTERGROUP_PAIDTO= "17";
    public static final String MASTERGROUP_RECEIVEDFROM= "18";
    public static final String TEMPLATEID_KEY= "templateid";
    //Constants for account Master Type
    public static final int ACCOUNT_MASTERTYPE_GL = 1;
    public static final int ACCOUNT_MASTERTYPE_CASH = 2;
    public static final int ACCOUNT_MASTERTYPE_BANK = 3;
    public static final int ACCOUNT_MASTERTYPE_GST = 4;
    
    public static final String ACC_DELIVERY_ORDER_MODULENAME = "deliveryorder";
    public static final String ACC_PACKED_IST_MODULENAME = "PackedISTRequest";
    public static final String ACC_REPAIR_IST_MODULENAME = "RepairISTRequest";
    public static final String currencynotes = "currencynotes";
    
    //From Date & To Date
    public static final String fromDate = "fromDate";
    public static final String toDate = "toDate";
    //Sort Order Date in SOA DD
    public static final String Date = "Date";
    public static final String isForPos = "isForPos";
    public static final String ACCOUNTING_URL = "accURL";
    public static final String isMultiGroupCompanyFlag = "isMultiGroupCompanyFlag";
    public static final String fromLinkComboAutoDO = "fromLinkComboAutoDO";
    public static final String sequenceformatDo = "sequenceformatDo";
    public static final String isCodeBased = "isCodeBased";
    public static final String tableName = "tableName";
    public static final String fetchColumn = "fetchColumn";
    public static final String conditionColumn = "conditionColumn";
    public static final String conditionColumnValue = "conditionColumnValue";
    public static final String companyColumn = "companyColumn";
    
    //Regex constant
    public static final String REGEX_LINE_BREAK = "(\r\n|\n\r|\r|\n)";

    public static final String accountCode = "acccode";
    public static final String auditcheck = "auditcheck";
    public static final String status = "status";
    public static final String isBatchSerial = "isBatchSerial";
    public static final String quantity = "quantity";
    public static final String dquantity = "dquantity";
    
    public static final String DISCOUNT_MASTER_TYPE_PERCENTAGE = "1";
    public static final String DISCOUNT_MASTER_TYPE_FLAT = "0";
    public static final String DISCOUNTMASTER = "discountMaster";
    public static final String DISCOUNT_ON_PAYMENT_TERMS = "discountOnPaymentTerms";
    public static final String IS_POSTING_DATE_CHECK = "isPostingDateCheck";
    public static final String PeriodicJE = "PeriodicJE";
    
    public static final int CNDN_TYPE_FOR_MALAYSIA = 5;
    public static final String SAMPLE_ASSEMBLY_FILE_WITHOUT_BOM_ID="9d920314-3626-11e7-8a21-2c4d544cd7fd";
    
    public static final String FIELDPARAMS_ISACTIVATE = "isActivated";
    public static final String FIELDPARAMS_ISFORGSTMAPPING = "isForGSTRuleMapping";
    
    public static final String NON_ALPHANUMERIC = "nonalphanumeric";
    public static final String IS_EXPORT_REPORT = "isExportReport";
    //stock status Report ID
    public static final int STOCK_STATUS_REPORT_ID = 1166; 
    public static final String REPORT_ID = "reportId";

    public static final String MASTER_CONFIGURATION_GAFFILEVERSION = "1.0";
    public static final int NEW_GST_ONLY = 1;
    public static final int OLD_NEW_GST = 2;
    public static final int NONEGST = 3;
    
    public static final String REQUEST_ID = "request_id";
    public static final String USE_OF_TEMP_TABLE = "useOfTemporaryTable";

    public static final String GSTProdCategory = "Product Tax Class";
    public static final String id1 = "id1";
    public static final String termId = "termId";
    public static final String termName = "termName";
    public static final String term = "term";
    public static final String termamount = "termamount";
    public static final String other = "Other";
    public static final String id2 = "id2";
    public static final String id3 = "id3";
    public static final String percentage = "percentage";
    public static final String lineLevelTerms = "lineLevelTerms";
    public static final String user = "user";
    public static final String GST_UNIT_QUANTITY_CODE = "Unit Quantity Code";
    public static final String GST_E_Commerce_Operator = "E-Commerce Operator";
    /**
     * Add columpref check here
     */
    public static enum columnPref {
        GSTCalculationOnShippingAddress("isGSTCalculationOnShippingAddress"),
        productPaging("allowProductPagingEditing"),
        gstamountdigitafterdecimal("gstamountdigitafterdecimal"),
        undeliveredServiceSOOpen ("undeliveredServiceSOOpen");
        private final String value;
        private columnPref(String value) {
            this.value = value;
        }
        public String get() {
            return value;
        }
        public static List<String> getNames() {
            List<String> list = new ArrayList<String>();
            for (columnPref s : columnPref.values()) {
                list.add(s.get());
            }
            return list;
        }
    }
    public static final String ActiveVersioningInPurchaseOrder = "activeVersioningInPurchaseOrder";
    public static final String RCMApplicable = "RCMApplicable";
    public static final String EWAYApplicable = "EWAYApplicable";
    public static final String ENTITY = "Entity";
    public static final String entity = "entity";
    public static final String appliedDate = "applieddate";
    public static final String STATE = "State";
    public static final String CUSTOM_STATE_NAME = "Custom_State";
    public static final String CITY = "City";
    public static final String COUNTY = "County";
    public static final String GSTIN = "GSTIN";
    public static final String PAN = "PAN";
    public static final String VALUE = "value";
    public static final String HSN_SACCODE = "HSN/SAC Code";
    public static final int Barcode_BatchNumber = 5;
    public static final String sampleAssemblyProductWithoutBOM = "sample_assembly_product_withoutBOM";
    public static final String notinquery = "notinquery";
    public static final String isApplyTaxToTerms = "isApplyTaxToTerms";
    public static final String autoLoadInvoiceTermTaxes = "autoLoadInvoiceTermTaxes";
    public static final String invoicetermsmap = "invoicetermsmap";             //Sales Invoice Module
    public static final String salesordertermmap = "salesordertermmap";         //Sales Order Module
    public static final String quotationtermmap = "quotationtermmap";           //Customer Quotation Module
    public static final String receipttermsmap = "receipttermsmap";             //Purchase Invoice Module
    public static final String goodsreceiptordertermmap = "goodsreceiptordertermmap"; //Goods Receipt Order Module
    public static final String purchaseordertermmap = "purchaseordertermmap";       // Purchase Order Module
    public static final String vendorquotationtermmap = "vendorquotationtermmap";   //Vendor Quotation Module
    public static final String deliveryordertermmap = "deliveryordertermmap";       //Delivery Order Module
    public static final String purchasereturntermsmap = "purchasereturntermsmap";   //Purchase Return Module
    public static final String salesreturntermsmap = "salesreturntermsmap";         //Sales Return Module
    
    public static final String ISSEPARATED = "isseparated";
    public static final int APPLYGST = 0;
    public static final int NOGST = 1;
    public static final int APPLYSOMEGST = 2;
    public static final int APPLYGSTONDATE = 3;
    public static final int APPLY_IGST = 4;
    
    public static final int RECURRINGALL=1;
    public static final int RECURRINGACTIVATED=2;
    public static final int RECURRINGDEACTIVATED=3;
     
    public static final String GST_ENTITY_KEY = "entity";
    public static final String transactionNo = "transactionNo";
    public static final String GST_PRODUCT_CATEGORY_KEY = "producttaxclass";
    public static final String GST_ADDRESS_STATE_KEY = "state";
    public static final String GST_ADDRESS_CITY_KEY = "city";
    public static final String GST_ADDRESS_COUNTY_KEY = "county";
    
    public static final String SALESORPURCHASE_FLAG = "salesOrPurchaseFlag";
    public static final String TERM_TYPE = "termType";
    public static final String GST_MASTERDATA = "masterdata";
    public static final String GST_ENTITYBASED_LINELEVEL_TERMRATE = "entitybasedlineleveltermrates";
    public static final String GST_PRODUCTCATEGORY_GSTRULES_MAPPPINGS = "productcategorygstrulesmapppings";
    public static final int GST_TERM_TYPE = 7;
    public static final String shippedLocation1 = "shiplocation1";
    public static final String shippedLocation2 = "shiplocation2";
    public static final String shippedLocation3 = "shiplocation3";
    public static final String shippedLocation4 = "shiplocation4";
    public static final String shippedLocation5 = "shiplocation5";
    public static final String shippedLoc1 = "shippedLoc1";
    public static final String shippedLoc2 = "shippedLoc2";
    public static final String shippedLoc3 = "shippedLoc3";
    public static final String shippedLoc4 = "shippedLoc4";
    public static final String shippedLoc5 = "shippedLoc5";
    public static final String prodCategory = "prodcategory";
    
    public static final int IMPORT_THREAD_PRIORITY_HIGH=7;
    
    public static final String ALL = "all";
    public static final String FULLY_PAID = "fullypaid";
    
    public static final List<String> AGED_PAYABE_LINE_CD_COMP_LIST=Arrays.asList(new String[]{"chkl"});
    
    public static final String ISNUMERIC = "isNumeric";
    
    public static final int ALLOW_TO_EDIT_PRODUCT_CUSTOMFIELD = 1;              //ERM-177 / ERP-34804
    
    public static final int DONOT_ALLOW_TO_EDIT_PRODUCT_CUSTOMFIELD = 0;        //ERM-177 / ERP-34804
    
    public static final String ONLY = " Only";
    public static final Map<String, String> GSTRegType = new HashMap<String, String>();
    public static final String GSTRegType_Regular = "Regular";
    public static final String GSTRegType_Unregistered = "Unregistered";
    public static final String GSTRegType_Composition = "Composition";
    public static final String GSTRegType_Consumer = "Consumer";
    public static final String GSTRegType_Regular_ECommerce = "Regular E-Commerce";
    public static final String GSTRegType_Composition_ECommerce = "Composition E-Commerce";
    static {
        GSTRegType.put(GSTRegType_Regular, "ac09adba-58b9-11e7-8ead-c03fd5658531");
        GSTRegType.put(GSTRegType_Unregistered, "ac09adba-58b9-11e7-8ead-c03fd5658532");
        GSTRegType.put(GSTRegType_Composition, "ac09adba-58b9-11e7-8ead-c03fd5658533");
        GSTRegType.put(GSTRegType_Consumer, "ac09adba-58b9-11e7-8ead-c03fd5658534");
        GSTRegType.put(GSTRegType_Regular_ECommerce, "420d7db5-7847-11e7-a551-708bcdaa138a");
        GSTRegType.put(GSTRegType_Composition_ECommerce, "13670bb7-7847-11e7-a551-708bcdaa138a");
    }
    public static final double GST_LIMIT_AMT=250000;
    public static final String BALANCE_Amount = "balanceamount";
    //User Preference Option Constants
    public static final String showNewToDeskeraWelcomeMsg = "showNewToDeskeraWelcomeMsg";
    public static final String showPendingApprovalWelcomeMsg = "showPendingApprovalWelcomeMsg";
    
    public static final String cntype = "cntype";
    public static final int CNAgainstSalesInvoice = 1;
    public static final int DNAgainstPurchaseInvoice = 1;
    public static final String creationdate = "creationdate";
    public static final String RoundingAdjustmentEntryID = "roundingAdjustmentEntryID";
    public static final String IsRoundingAdjustmentApplied = "isRoundingAdjustmentApplied";
    public static final String RoundingAdjustmentFlag = "roundingAdjustmentFlag";
    public static final String RoundingAdjustmentAccountID = "roundingAdjustmentAccountID";
    public static final String roundingadjustmentamountinbase = "roundingadjustmentamountinbase";
    public static final String roundingadjustmentamount = "roundingadjustmentamount";
    public static final String JETYPE_NONE="None";
    public static final String JETYPE_TDS="TDS";
    public static final String JETYPE_TCS="TCS";
    public static final String JETYPE_ITC="ITC";
    public static final Map<String, Integer> GSTRJETYPE = new HashMap<String, Integer>();

    static {
        GSTRJETYPE.put(JETYPE_NONE, 0);
        GSTRJETYPE.put(JETYPE_TDS, 1);
        GSTRJETYPE.put(JETYPE_TCS, 2);
        GSTRJETYPE.put(JETYPE_ITC, 3);
    }
    public static final String EXTERNALCURRENCYRATE = "externalCurrencyRate";
    public static final String IS_GST_HISTORY_PRESENT = "isGSTHistoryDataPresent";
    public static final double INDIA_URD_RCM_PI_AMOUNTLIMIT = 5000;
    
    public static final String currency = "currency";
    public static final String debitNoteNumber = "debitNoteNumber";
    public static final String creditNoteNumber = "creditNoteNumber";
    public static final String paymentNumber = "paymentNumber";
    public static final String receiptNumber = "receiptNumber";
    public static final String currencysymbol = "currencysymbol";
    public static final String currencyname = "currencyname";
    public static final String KNOCK_OF_HQL_CONDITION="  and  (((je.transactionModuleid is null or je.transactionModuleid in ('" + Constants.Acc_Party_Journal_Entry + "','" + Constants.Acc_Make_Payment_ModuleId + "','" + Constants.Acc_Receive_Payment_ModuleId + "','" + Constants.Acc_GENERAL_LEDGER_ModuleId + "')) and (jed.isSeparated = false or jed.isSeparated = true)) "
                                + " or (je.transactionModuleid in ('" + Constants.Acc_Invoice_ModuleId + "','" + Constants.Acc_Vendor_Invoice_ModuleId + "','" + Constants.Acc_Debit_Note_ModuleId + "','" + Constants.Acc_Credit_Note_ModuleId + "') and jed.isSeparated = false)) ";
    
    public static final String ENTITY_NAME_EXPORT = "Entity Name ";
    public static final String GST_CURRENCY_RATE = "gstCurrencyRate";

    public static final String COMPANYID_CHKL ="95d79cb9-3efa-49e6-b00e-9df20164cdbd";
    public static final String Companyids_Chkl_And_Marubishi ="95d79cb9-3efa-49e6-b00e-9df20164cdbd,5b207d19-a091-4815-bc81-3175ae7bd6c6";
    public static final String Customer_default_account = "Customer Default Account" ;

    //Inventory QA scrap operation 
    public static final String SCRAP_OPERATION = "Scrap";

    public static final int dueDateFilter = 0;
    /*
     Tax Type Constant
    */
     public static final int PURCHASE_TYPE_TAX= 1;
     public static final int SALES_TYPE_TAX= 2;
     public static final int BOTH_TYPE_TAX= 0;
     
    public static final String marubishi_Company_Id = "5b207d19-a091-4815-bc81-3175ae7bd6c6";
    
    public static final String PRICELIST_UOM_CONDITION_PURCHASE = " and pl1.uomid.ID = p.unitOfMeasure.ID ";//carry in = true
    public static final String PRICELIST_UOM_CONDITION_PURCHASE_INNER = " and uomid.ID = pl1.uomid.ID ";
    public static final String PRICELIST_UOM_CONDITION_SALES = " and pl2.uomid.ID = p.unitOfMeasure.ID ";//carry in = false
    public static final String PRICELIST_UOM_CONDITION_SALES_INNER = " and uomid.ID = pl2.uomid.ID ";
    
    public static final String PRICELIST_UOM_CONDITION_ASSEMBLY_PURCHASE = " and pl1.uomid.ID = pa.subproducts.unitOfMeasure.ID ";
    public static final String PRICELIST_UOM_CONDITION_ASSEMBLY_PURCHASE_INNER = " and uomid.ID = pl1.uomid.ID ";
    public static final String PRICELIST_UOM_CONDITION_ASSEMBLY_SALES = " and pl2.uomid.ID = pa.subproducts.unitOfMeasure.ID ";
    public static final String PRICELIST_UOM_CONDITION_ASSEMBLY_SALES_INNER = " and uomid.ID = pl2.uomid.ID ";
    
    public static final String PRICELIST_UOM_INNER_CONDITION = " and uomid.ID = pl1.uomid.ID ";
    public static final String PRICELIST_UOM_CONDITION = " and pl1.uomid.ID = ? ";
    
    public static final String PRICELIST_UOM_STOCK_LEDGER_CONDITION = " and pl.uomid = p.unitOfMeasure ";
    public static final String PRICELIST_UOM_STOCK_LEDGER_INNER_CONDITION = " and pricelist.uomid = p.unitOfMeasure ";
    
    public static final String NA_UOM = "N/A";
    public static final String NA_UOM_DEFAULTMEASUREOFUOM_ID = "9d4f64ee-f5dc-11e7-a316-708bcdaa138a";
    /**
     * Default column header UUID for Customer/ Vendor addresses
     */
    //Customer 
    public static final String CUSTOMER_BILL_STATE = "6a9b98fa-c95d-11e3-bbf7-001cc066e9f0";
    public static final String CUSTOMER_BILL_CITY = "6a9a8578-c95d-11e3-bbf7-001cc066e9f0";
    public static final String CUSTOMER_BILL_COUNTY = "dc7b4e4b-3ec7-4812-a561-6cc50b7a6606";
    public static final String CUSTOMER_SHIPP_STATE = "e5f619e6-c960-11e3-bbf7-001cc066e9f0";
    public static final String CUSTOMER_SHIPP_CITY = "e5f592a0-c960-11e3-bbf7-001cc066e9f0";
    public static final String CUSTOMER_SHIPP_COUNTY = "5e461787-0625-4763-bbd2-ef3ec3ce1c57";
    //Vendor
    public static final String VENDOR_BILL_STATE = "b17f52de-c962-11e3-bbf7-001cc066e9f0";
    public static final String VENDOR_BILL_CITY = "b17ed264-c962-11e3-bbf7-001cc066e9f0";
    public static final String VENDOR_BILL_COUNTY = "3d768aa4-8bec-46b3-8b73-a078f760fe93";
    public static final String VENDOR_SHIPP_STATE = "41a014b0-d1d4-12e3-9d55-001cc066e9f3";
    public static final String VENDOR_SHIPP_CITY = "31a014b0-d1d4-12e3-9d55-001cc066e9f2";
    public static final String VENDOR_SHIPP_COUNTY = "cac4d08e-45f7-4e1d-8785-662a4e29dba4";
    //Address Import header name mapping with dimension (State, City , County)
    public static final Map<String, String> addressToDimensionMap = new HashMap<String, String>();
    static {
        //Billing Address
        addressToDimensionMap.put("BillingCity", CITY);
        addressToDimensionMap.put("BillingCounty", COUNTY);
        addressToDimensionMap.put("BillingState", STATE);
        //Shipping Adddres
        addressToDimensionMap.put("ShippingCity", CITY);
        addressToDimensionMap.put("ShippingCounty", COUNTY);
        addressToDimensionMap.put("ShippingState", STATE);
    }
     public static final String GSTAddressMapping = "isGSTAddressMapping";
             
     
    public static boolean isOptimized = true;
    /**
     *
     * RCM 5K limit not used now Purchases from URD in India, will be exempt.
     * Without any ceiling. From 13th October 2017 to 31st March 2018 ERP-37398
     * ERP-37398
     */
    public static boolean isRCMPurchaseURD5KLimit = false;

    public static final String opening_Date = "Jan 01, 1970 12:00:00 AM";
    public static final String GSTR1_SHIPPING_PORT = "Port Code";
    public static final String GSTR1_SHIPPING_DATE = "Shipping Bill Date";
    public static final String GSTR1_SHIPPING_BILL_NO = "Shipping Bill Number";
    
    public static final String isPrecisiongDiffInLandedCost = "isPrecisionDiffinLandedCost"; //ERP-31096 precision loss in landed cost
 

    public static final String LANDED_COST_ALLOCATIONTYPE_CUSTOMDUTY = "4";
    //ERM-999 adding Remarks for identifying Pick Pack Generated SA and IST entries
    public static final String pickPack_DO_IST_Memo = "IST created for Pick Pack DO:";
    public static final String pickPack_DO_SA_Memo = "Stock Adjustment created for Pick Pack DO:"; 
    
    //ERM-955 Validation on GSTIN Number for India
    public static final String GSTINFORMAT_REGEX = "^0*([1-9]|[12][0-9]|3[0-7]|97)[A-Z]{5}[0-9]{4}[A-Z]{1}[A-Z0-9][A-Za-z]{1}[A-Z0-9\\d]{1}";
    public static final String GSTN_CODEPOINT_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    // Append "via import" at the end of statement in case of add/edit import
    public static final String auditMessageViaImport = " via import";
    public static final String IMPORT_PAYMENT = "Payment";
    public static final String IMPORT_RECEIPT = "Receipt";
    public static final String IMPORT_INVOICE = "Invoice";
    public static final String IMPORT_CN = "Credit Note";
    public static final String IMPORT_DN = "Debit Note";
    
    public static final int dueDateFilterForAgeing  = 0;
    
    public static final String agedReceivableDateFilter = "agedReceivableDateFilter";
    public static final String agedPayableDateFilter = "agedPayableDateFilter";
    public static final String agedReceivableInterval = "agedReceivableInterval";
    public static final String agedReceivableNoOfInterval = "agedReceivableNoOfInterval";
    public static final String agedPayableInterval = "agedPayableInterval";
    public static final String agedPayableNoOfInterval = "agedPayableNoOfInterval";
    
    public static final int agedDueDate0to30Filter = 2; //Ageing Date Filter For Due Date 0-30 Days
    public static final int agedDueDate1to30Filter = 0; //Ageing Date Filter For Due Date 1-30 Days
    public static final int agedInvoiceDateFilter = 1; //Ageing Date Filter For Invoice Date
    public static final int agedInvoiceDate0to30Filter = 3; //Ageing Date Filter For Invoice Date
//    public static final int agedInvoiceDateFilter = 1; //Ageing Date Filter For Invoice Date
    public static final String agedDueDate1to30Days=" Due Date (1-30 Days)";
    public static final String agedDueDate0to30Days=" Due Date (0-30 Days)";
    public static final String agedInvoiceDate=" Invoice Date (1-30 Days)";
    public static final String agedInvoiceDate0to30=" Invoice Date (0-30 Days)";
    
    public static final String soaAgedPayableDateFilter = "soaAgedPayableDateFilter";
    public static final String soaAgedReceivableDateFilter = "soaAgedReceivableDateFilter";
    public static final String statementOfAccountsFlag = "statementOfAccountsFlag";
    public static final String invalidSeqNumberMsg = "Sequence number is not generated properly.";

    public static final String defaultHeader_CostofGoodsSoldAccount = "Cost of Goods Sold Account"; // Cost of Goods Sold Account [Product Master]
    public static final String defaultHeader_InventoryAccount = "Inventory Account"; // Inventory Account [Product Master]
    public static final String defaultHeader_StockAdjustmentAccount = "Stock Adjustment Account"; // Stock Adjustment Account [Product Master]
    public static final String isMerchantExporter = "isMerchantExporter";
    public static final String additionalMemo = "additionalMemo";
    public static final String additionalMemoName = "additionalMemoName";
    public static final int isUnRealisedJE = 1; //For  Un-Realised JE
    public static final int isRealisedJE = 2; // For Realised JE
    
    /*Journal Entry Constants*/
    public final static int NORMAL_JOURNAL_ENTRY = 1;
    public final static int PARTY_JOURNAL_ENTRY = 2;
    public final static int FUND_TRANSFER_JOURNAL_ENTRY = 3;
    
    public final static int AGED_RECEIVABLES_SUMMARY = 24;
    public final static int AGED_RECEIVABLES_DETAILS = 26;
    public final static int AGED_PAYABLES_SUMMARY = 23;
    public final static int AGED_PAYABLES_DETAILS = 21;
    public final static String asOfDate = "asofdate";
    public final static String curdate = "curdate";
    public static final int Filter_Invoice_WithFullGRN = 11; 
    public static final int Filter_Invoice_WithNoGRN = 12; 
    public static final int Filter_Invoice_WithPartialGRN = 13; 
    
    public static final int DefaultIntervalInDays = 30; 
    public static final int DefaultNoOfIntervals = 7; 
    
    public static final int Filter_Invoice_WithFullDO = 10;
    public static final int Filter_Invoice_WithNoDO = 11;
    public static final int Filter_Invoice_WithPartialDO = 12;

    public static final String isNewGST = "isnewgst";
    public static final String islinkadvanceflag = "islinkadvanceflag";
    
    public static final String CURRENT = "Current";
    
    public static final String barcodeScanning = "barcodeScanning";
    
    public static final String fieldMap = "FieldMap";
    public static final String replaceFieldMap = "replaceFieldMap";
    public static final String dimensionFieldMap = "DimensionFieldMap";
    public static final String lineLevelCustomFieldMap = "LineLevelCustomFieldMap";
    public static final String productLevelCustomFieldMap = "ProductLevelCustomFieldMap";
    
    public static final String displayUnitPriceAndAmountInSalesDocument = "displayUnitPriceAndAmountInSalesDocument";
    public static final String displayUnitPriceAndAmountInPurchaseDocument = "displayUnitPriceAndAmountInPurchaseDocument";
    
    public static final String isLandedCostTermJE = "isLandedCostTermJE";
    public static final String transactionmodule = "transactionmodule"; // for Inventory side Transaction Module values
    public static final int GST_ITCTYPE_DEFAULT= 1;
    public static final int GST_ITCTYPE_BLOCKED = 2;
    public static final int GST_ITCTYPE_REVERSED = 3;
    public static final Map<String, Integer> ITCTYPE = new HashMap<String, Integer>();

    static {
        ITCTYPE.put("ITC is Blocked", GST_ITCTYPE_BLOCKED);
        ITCTYPE.put("ITC Available in Full", GST_ITCTYPE_DEFAULT);
        ITCTYPE.put("ITC to be Reversed", GST_ITCTYPE_REVERSED);
    }
    public static final String GST_DEFAULT_ACCOUNT_ID="'31ce3ba6-5f2b-11e7-907b-a6006ad3dba0','31ce3c78-5f2b-11e7-907b-a6006ad3dba0',"
            + "'31ce3d54-5f2b-11e7-907b-a6006ad3dba0','31ce3e30-5f2b-11e7-907b-a6006ad3dba0'";
    public static final String dbsBankAccount = "dbsBankAccount";
    public static final String deskeraAccountID = "deskeraAccountID";

    public static Set<String> UNIT_PRICE_DISABLE_KEYS = new HashSet();
    static {
        //SalesReturn= These are the fields which are enabled when unit price option is checked on for SR
        UNIT_PRICE_DISABLE_KEYS.add("orderamountwithTax");
        UNIT_PRICE_DISABLE_KEYS.add("orderamount");
        UNIT_PRICE_DISABLE_KEYS.add("amountBeforeTax");
        UNIT_PRICE_DISABLE_KEYS.add("subtotal");
        UNIT_PRICE_DISABLE_KEYS.add("amount");
        UNIT_PRICE_DISABLE_KEYS.add("rate");
        UNIT_PRICE_DISABLE_KEYS.add("rateIncludingGst");
        UNIT_PRICE_DISABLE_KEYS.add("amountinbase");
        UNIT_PRICE_DISABLE_KEYS.add("subtotal");
    }
    public static Set<String> SALES_MODULE_ID = new HashSet();
    static {
        //SalesReturn= These are the fields which are enabled when unit price option is checked on for SR
        SALES_MODULE_ID.add(String.valueOf(Acc_Sales_Return_ModuleId));
        SALES_MODULE_ID.add(String.valueOf(Acc_Invoice_ModuleId));
        SALES_MODULE_ID.add(String.valueOf(Acc_Cash_Sales_ModuleId));
        SALES_MODULE_ID.add(String.valueOf(Acc_Credit_Note_ModuleId));
        SALES_MODULE_ID.add(String.valueOf(Acc_Receive_Payment_ModuleId));
        SALES_MODULE_ID.add(String.valueOf(Acc_Sales_Order_ModuleId));
        SALES_MODULE_ID.add(String.valueOf(Acc_Customer_Quotation_ModuleId));
        SALES_MODULE_ID.add(String.valueOf(Acc_Delivery_Order_ModuleId));
    }
    
    
    public static Set<String> PURCHASE_MODULEID = new HashSet();
    static {
        //SalesReturn= These are the fields which are enabled when unit price option is checked on for SR
        PURCHASE_MODULEID.add(String.valueOf(Acc_Purchase_Return_ModuleId));
        PURCHASE_MODULEID.add(String.valueOf(Acc_Goods_Receipt_ModuleId));
        PURCHASE_MODULEID.add(String.valueOf(Acc_Purchase_Order_ModuleId));
        PURCHASE_MODULEID.add(String.valueOf(Acc_Make_Payment_ModuleId));
        PURCHASE_MODULEID.add(String.valueOf(Acc_Debit_Note_ModuleId));
        PURCHASE_MODULEID.add(String.valueOf(Acc_Vendor_Invoice_ModuleId));
        PURCHASE_MODULEID.add(String.valueOf(Acc_Vendor_Quotation_ModuleId));
    }
    
    public static final int NumberOfRecordsInEachChunk = 9000; 
    public static final String IRASGSTForm5CallBackURL = "irasgstform5esubmissioncallback.jsp"; 
    public static final String IRASGSTTransactionListingCallBackURL = "irastransactionlistingcallback.jsp"; 
    public static final int IRASSubmissionFlag_Pending = 3;
    public static final int IRASSubmissionFlag_Pending_For_Authentication = 0;
    public static final int IRASSubmissionFlag_Success = 1;
    public static final int IRASSubmissionFlag_Failure = 2; 
    public static final String IRASReturnCode_Failure = "30"; 
    public static final String IRASReturnCode_Success = "10";
    
    public static final int GSTForm5SubmissionFlag_Pending_For_Authentication = 0; 
    public static final int GSTForm5SubmissionFlag_Success = 1; 
    public static final int GSTForm5SubmissionFlag_Failure = 2;
    public static final int GSTForm5SubmissionFlag_AbortedByUser = 3;
    public static final int GSTForm5SubmissionFlag_Pending = 4;
    public static final String GSTForm5ReturnCode_Failure = "30"; 
    public static final String GSTForm5ReturnCode_Success = "10"; 
      
    public static final Map<String, String> GST_TAX_CODE = new HashMap<String, String>();
    static {
        GST_TAX_CODE.put("GST(TX-RE)@7.00%", "TX-RE");
        GST_TAX_CODE.put("GST(ESN33)@0.00%", "ESN33");
        GST_TAX_CODE.put("GST(TX-E33)@7.00%", "TX-E33");
        GST_TAX_CODE.put("GST(NR)@0.00%", "NR");
        GST_TAX_CODE.put("GST(SR)@7.00%", "SR");
        GST_TAX_CODE.put("GST(ES33)@0.00%", "ES33");
        GST_TAX_CODE.put("GST(ME)@0.00%", "ME");
        GST_TAX_CODE.put("GST(DS)@7.00%", "DS");
        GST_TAX_CODE.put("GST(EP)@0.00%", "EP");
        GST_TAX_CODE.put("GST(ZR)@0.00%", "ZR");
        GST_TAX_CODE.put("GST(TX-N33)@7.00%", "TX-N33");
        GST_TAX_CODE.put("GST(ZP)@0.00%", "ZP");
        GST_TAX_CODE.put("GST(TX7)@7.00%", "TX");
        GST_TAX_CODE.put("GST(OS)@0.00%", "OS");
        GST_TAX_CODE.put("GST(IGDS)@7.00%", "IGDS");
        GST_TAX_CODE.put("GST(BL)@7.00%", "BL");
        GST_TAX_CODE.put("GST(IM)@7.00%", "IM");
        GST_TAX_CODE.put("GST(OP)@0.00%", "OP");
    }
    
    public static final String extraCompanyPreferences = "extraCompanyPreferences";
    public static final String preferences = "preferences";
    public static final String basecurrencyid = "basecurrencyid";
    
}
